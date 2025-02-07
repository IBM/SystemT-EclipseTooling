/*******************************************************************************
* Copyright IBM
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.ibm.biginsights.textanalytics.patterndiscovery.helpers;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.ExternalTypeInfo;
import com.ibm.avatar.api.OperatorGraph;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.refactoring.ResourceChangeActionThread;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.AQLProject;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectDependencyUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.BubbleModel;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * Util methods used through the plugin
 * 
 * 
 */
public class AQLUtils
{



   /**
   * @param project
   * @return
   */
  public static AQLProject getAQLProject (IProject project)
  {
    if (project == null) return null;

    String location = project.getLocation ().toOSString ();
    AQLProject aqlProject = Activator.getLibrary ().getLibraryMap ().get (location);

    return aqlProject;
  }

  /**
   * @param project
   * @return a list of all the views for this project
   */
  public static List<AQLElement> getProjectViews (IProject project)
  {
    AQLProject aqlProject = getAQLProject (project);
    if (aqlProject == null) return null;

    List<String> aqlFilePaths = aqlProject.getAqlFilePaths ();
    List<AQLElement> views = Activator.getLibrary ().getViews (aqlFilePaths);
    return views;
  }

  /**
   * @param project
   * @return a list of all the output views defined in this project
   */
  public static List<AQLElement> getProjectOutputViews (IProject project)
  {
    AQLProject aqlProject = getAQLProject (project);
    if (aqlProject == null) return null;

    List<String> aqlFilePaths = aqlProject.getAqlFilePaths ();
    List<AQLElement> elements = Activator.getLibrary ().getElements (aqlFilePaths);
    List<AQLElement> outputViews = new ArrayList<AQLElement> ();
    for (AQLElement view : elements) {
      if (view.getType ().equals ("OUTPUT_VIEW")) outputViews.add (view);
    }
    return outputViews;
  }

  /**
   * @param elements
   * @return the list of all the aql elements provided as Strings
   */
  public static List<String> aqlElementTosStr (List<AQLElement> elements)
  {
    List<String> ret = new ArrayList<String> ();
    for (AQLElement element : elements)
      ret.add (element.getName ());

    return ret;
  }

  /**
   * make sure that the AQL elements for this project gets loaded into the AQL library. this parse the AQL of this
   * project into a structure that allows you to access them later. (this is the same functionality implemented to be
   * used by the outline of AQL files)
   * 
   * @param projectName
   */
  public static void createLibraryForProject (String projectName)
  {
    Thread t = new ResourceChangeActionThread (null, null, ResourcesPlugin.getWorkspace ().getRoot ().findMember (
      new File (projectName).getName ()));
    t.start ();
    while (t.isAlive ()) {
      // lets wait for this to finish
    }
  }

  /**
   * this method pulls the nodes to be displayed from the database. it query the database and use the result set
   * returned to create a list of models to be used to create the display
   */
  public static int getNodesFromDB (PatternDiscoveryJob job, ArrayList<BubbleModel> table, int minSize, int maxSize)
  {
    int row = 0;
    ResultSet result = null;

    try {
      String query = String.format (
        "SELECT * FROM (SELECT count(*) size, JSEQUENCE FROM %s WHERE JSEQUENCE != '}' GROUP BY JSEQUENCE) AS T WHERE T.size >= %d AND T.size <= %d ORDER BY size DESC",
        job.getTableName (), minSize, maxSize);

      result = job.readFromDB (query);

      ResultSetMetaData rsmd = result.getMetaData ();
      int numOfCol = rsmd.getColumnCount ();

      String temp;

      // we never display more than 100 bubbles
      int limit = 100;
      // however the first time the user runs Pattern Discovery we want to
      // display it nicely so we limit it to the defined limit
      if (job.getProcessLevel ()) {
        limit = job.getLimit ();
      }

      while (result.next () && row < limit) {

        BubbleModel model = new BubbleModel ();

        for (int j = 0; j < numOfCol; j++) {
          temp = result.getString (j + 1);

          if (j == 0)
            model.size = Integer.valueOf (temp);

          else if (j == 1) {
            model.originalSignature = "";
            // deep copy
            for (char c : temp.toCharArray ()) {
              model.originalSignature += c;
            }

            model.signature = temp;
          }
        }

        model.id = row + 1;
        model.signature = changeStr (model.signature);

        // if (!model.signature.equals("/n")) {
        table.add (model);
        row++;
        // }
      }

      // System.err.println("Number of results : " + row);
      result.close ();
      job.shutDownDB ();
    }
    catch (SQLException e) {
      ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_READING_DATA_FROM_DB, e);
    }

    return row;
  }

  /**
   * Replaces n with /n
   * @param str
   * @return Replaced string
   */
  public static String changeStr (String str)
  {

    StringTokenizer tokenizer = tokenize (str, ";{}", " \r\n");
    String newStr = "";
    String next;

    while (tokenizer.hasMoreTokens ()) {
      next = tokenizer.nextToken ();
      if (next.equals ("n"))
        newStr += "\\n";
      else
        newStr += next + " ";
    }

    newStr = newStr.trim ();

    if (newStr.charAt (0) == '{' && newStr.charAt (newStr.length () - 1) == '}') {
      newStr = newStr.substring (1, newStr.length () - 1);
    }

    return newStr;
  }

  public static StringTokenizer tokenize (String line, String seperator, String delimiter)
  {
    StringTokenizer tokenizer = new StringTokenizer (line, seperator, true);
    String newLine = "";

    while (tokenizer.hasMoreTokens ()) {
      newLine += tokenizer.nextToken () + " ";
    }

    tokenizer = new StringTokenizer (newLine, delimiter);

    return tokenizer;
  }

  public static OperatorGraph getOperatorGraph (String projectName)
  {
    return getOperatorGraph (projectName, null, null);
  }

  public static OperatorGraph getOperatorGraph (String projectName, ExternalTypeInfo eti, TokenizerConfig lwTokenizerConfig)
  {
    return getOperatorGraph (projectName, (String[])null, eti, lwTokenizerConfig);
  }

  public static OperatorGraph getOperatorGraph (String projectName, String moduleName, ExternalTypeInfo eti, TokenizerConfig lwTokenizerConfig)
  {
    if (moduleName != null)
      return getOperatorGraph (projectName, new String[] { moduleName }, eti, lwTokenizerConfig);
    else
      return getOperatorGraph (projectName, eti, lwTokenizerConfig);
  }

  public static OperatorGraph getOperatorGraph (String projectName, String[] modules, ExternalTypeInfo eti, TokenizerConfig lwTokenizerConfig)
  {
    if (StringUtils.isEmpty (projectName))
      return null;

    OperatorGraph og = null;

    try {

      IProject project = ProjectUtils.getProject (projectName);

      if (modules == null) {
        if (ProjectUtils.isModularProject (project))
          modules = ProjectUtils.getModules (project);
        else
          modules = new String[] { Constants.GENERIC_MODULE };
      }

      String tamPathStr = getTamPathStrURI (project);

      og = OperatorGraph.createOG (modules, tamPathStr, eti, lwTokenizerConfig);
    }
    catch (Exception e) {
      ErrorMessages.LogErrorMessage(ErrorMessages.PATTERN_DISCOVERY_PROCESSING_ERR, e);
    }

    return og;
  }

  public static OperatorGraph getOperatorGraph (String[] modules, String modulePath, ExternalTypeInfo eti, TokenizerConfig lwTokenizerConfig)
  {
    OperatorGraph og = null;

    try {
      og = OperatorGraph.createOG (modules, modulePath, eti, lwTokenizerConfig);
    }
    catch (Exception e) {
      ErrorMessages.LogErrorMessage(ErrorMessages.PATTERN_DISCOVERY_PROCESSING_ERR, e);
    }

    return og;
  }

  private static String getTamPathStrURI (IProject project)
  {
    if (ProjectUtils.isModularProject (project)) {
      // Path of the current project's tams, dependent projects tams and the imported tams are returned.
      return ProjectDependencyUtil.populateProjectDependencyPath (project);
    }
    else {
      String defaultAogPath = ProjectPreferencesUtil.getDefaultAOGPath (project);
      String absoluteAogPath = ProjectPreferencesUtil.getAbsolutePath(defaultAogPath);
      return new File(absoluteAogPath).toURI().toString();
    }
  }

}
