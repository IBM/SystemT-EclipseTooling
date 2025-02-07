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
package com.ibm.biginsights.textanalytics.aql.editor.callhierarchy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.Messages;
import com.ibm.biginsights.textanalytics.aql.editor.common.FileTraversal;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class holds holds useful methods for other Call hierarchy classes. They can be consolidated later in a more
 * general Util class.
 * 
 * 
 */
public class CallHierarchyUtil
{

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  /**
   * Given a string, determines whether it is a view name It does this by obtaining from aql library, all view elements
   * and trying to match the token with their names.
   * 
   * @param token
   * @return
   */
  public static boolean isView (String token)
  {
    String projectName = getProjectName ();
    IAQLLibrary aqllib = ProjectUtils.isModularProject (projectName) ? Activator.getModularLibrary ()
      : Activator.getLibrary ();

    List<AQLElement> views = aqllib.getViews (getAllAQLFilePaths (projectName, true));
    if (views == null) { return false; }
    Iterator<AQLElement> lIterator = views.iterator ();
    if (!ProjectUtils.isModularProject (projectName)) { // In non-modular projects, if a '.' is present, it should be
                                                        // considered as part of element name.
      while (lIterator.hasNext ()) {
        if (lIterator.next ().getName ().equals (token)) { return true; }
      }
    }
    else {
      while (lIterator.hasNext ()) {
        AQLElement view = lIterator.next ();
        String[] candidateParts = ProjectUtils.splitQualifiedAQLElementName (token); //$NON-NLS-1$
        if (candidateParts.length == 2) {
          if (candidateParts[0].matches (view.getModuleName ()) && candidateParts[1].matches (view.getName ())) { return true; }
        }
        else {
          String[] projectModules = ProjectUtils.getModules (ProjectUtils.getProject (projectName));
          for (String module : projectModules) {
            if (module.matches (view.getModuleName ()) && token.matches (view.getName ())) { return true; }
          }
        }
      }
      // might be an alias

      if (!token.contains (".")) {
        List<AQLElement> importedViews = aqllib.getImportedViews (getAllAQLFilePaths (projectName, false));
        if (importedViews != null) {
          for (AQLElement imported : importedViews) {
            if (imported.getAliasName () != null && imported.getAliasName ().equals (token)) { return true; }
          }
        }
      }
    }
    return false;
  }

  /**
   * Given a string, determines whether it is an AQL element name. It does this by obtaining from aql library, all AQL elements
   * and trying to match the token with their names.
   * 
   * @param token
   * @return
   */
  public static boolean isAQLElement (String token)
  {
    String projectName = getProjectName ();
    IAQLLibrary aqllib = ProjectUtils.isModularProject (projectName) ? Activator.getModularLibrary ()
      : Activator.getLibrary ();

    List<AQLElement> aqlElems = aqllib.getElements (getAllAQLFilePaths (projectName, true));

    if (aqlElems == null || aqlElems.isEmpty ()) { return false; }

    Iterator<AQLElement> lIterator = aqlElems.iterator ();
    while (lIterator.hasNext ()) {
      if (lIterator.next ().getName ().equals (token)) {
        return true;
      }
    }

    while (lIterator.hasNext ()) {
      if (lIterator.next ().getName ().equals (token)) { return true; }
    }

    return false;
  }

  /**
   * Finds the name of the project that contains the file currently opened by AQL Editor
   * 
   * @return
   */
  protected static String getProjectName ()
  {
    IEditorInput genericInput = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getActiveEditor ().getEditorInput ();
    if (!(genericInput instanceof IFileEditorInput)) { return ""; }
    IFileEditorInput input = (IFileEditorInput) genericInput;
    return input.getFile ().getProject ().getName ();
  }

  /**
   * Returns absolute paths of aql files belonging to the specified project and the projects mentioned in its required
   * list.
   * 
   * @param projectName
   * @param includeRequiredProjects true if files from required projects are also needed. This parameter is ignored for
   *          non modular projects
   * @return List of absolute paths
   */
  public static List<String> getAllAQLFilePaths (String projectName, boolean includeRequiredProjects)
  {
    if (projectName != null && !projectName.trim ().isEmpty ()) {
      SystemTProperties projectProperties = ProjectPreferencesUtil.getSystemTProperties (projectName);
      final Set<String> aqlFileSet = new LinkedHashSet<String> ();
      String[] searchPathList = {};
      if (projectProperties.isModularProject ()) {
        // this will work as as long as initialReconcile in ReconcilingStrategy
        // continues to parse all files in a project as well as referenced projects.
        IFolder srcDir = ProjectUtils.getTextAnalyticsSrcFolder (projectName);
        StringBuilder searchPath;
        if (srcDir != null) {
          searchPath = new StringBuilder (srcDir.getLocation ().toString ());
          try {
            if (includeRequiredProjects) {
              for (IProject ref : ProjectUtils.getProject (projectName).getReferencedProjects ()) {
                searchPath.append (Constants.DATAPATH_SEPARATOR);
                srcDir = ProjectUtils.getTextAnalyticsSrcFolder (ref);
                if (srcDir != null) {
                  searchPath.append (srcDir.getLocation ().toString ());
                }
              }
            }
          }
          catch (CoreException e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.AQLEditor_ERR_PROJ_REF);
          }
          searchPathList = searchPath.toString ().split (
            Constants.DATAPATH_SEPARATOR);
        }
      }
      else {
        String searchPath = projectProperties.getSearchPath ();
        searchPathList = ProjectPreferencesUtil.getAbsolutePath (searchPath).split (Constants.DATAPATH_SEPARATOR);
      }
      for (int i = 0; i < searchPathList.length; i++) {
        IPath path = new Path (searchPathList[i]).makeAbsolute ();
        try {
          new FileTraversal () {
            public void onFile (final File f)
            {
              if (f.getName ().endsWith (Constants.AQL_FILE_EXTENSION)) {
                aqlFileSet.add (f.getAbsolutePath ().toString ());
              }
            }
          }.traverse (new File (path.toOSString ()));
        }
        catch (IOException e) {
          // continue with the loop
        }
      }
      ArrayList<String> filePathList = new ArrayList<String> ();
      filePathList.addAll (aqlFileSet);
      return filePathList;
    }
    else {
      return new ArrayList<String> ();
    }

  }

  /**
   * Finds the smallest section in editor where the cursor has been placed, which is enclosed by quotes or double
   * quotes. Note: This method doesn't differentiate between single and double quotes. It assumes the document partition
   * on which the cursor has been placed is an AQL_String partition.
   * 
   * @param sentence Single line where the cursor has been placed.
   * @param cursorLocation Position of the cursor relative to start of the line.
   * @return
   */
  public static String getCurrentTokenBetweenQuotes (String sentence, int cursorLocation)
  {
    String curr = "";
    int start = 0, end = 0, startQuotes = 0, endQuotes = 0;
    int ln = sentence.length ();
    String prev = sentence.substring (0, cursorLocation);
    // cursor at first position or at least one character after the sentence
    if ((cursorLocation == 0) || (cursorLocation > ln)) {
      curr = "";
    }
    else {

      if (prev.lastIndexOf ("'") != -1) {
        start = prev.lastIndexOf ("'") + 1;
      }
      else {
        start = 0;
      }
      // System.out.println("prev is: " + prev + start);
      String next = sentence.substring (cursorLocation);

      if (next.indexOf ("'") != -1) {
        end = next.indexOf ("'") + cursorLocation;
      }
      else {
        end = sentence.length () - 1;
      }

      // now check for double quotes
      if (prev.lastIndexOf ("\"") != -1) {
        startQuotes = prev.lastIndexOf ("\"") + 1;
      }
      else {
        startQuotes = 0;
      }
      if (next.indexOf ("\"") != -1) {
        endQuotes = next.indexOf ("\"") + cursorLocation;
      }
      else {
        endQuotes = sentence.length () - 1;
      }
    }
    start = Math.max (start, startQuotes);
    end = Math.min (end, endQuotes);
    curr = sentence.substring (start, end);
    return curr;
  }

  /**
   * Given a line in editor and the position of the cursor on that line, it detects the token on which the cursor has
   * been placed. Here, a token is a sequence of alphanumeric characters and dots, hyphens and underscores.
   * 
   * @param sentence Single line where the cursor has been placed.
   * @param cursorLocation Position of the cursor relative to the start of the line.
   * @return
   */
  public static String getCurrentToken (String sentence, int cursorLocation)
  {
    String curr = "";
    int start = 0, end = 0;
    int ln = sentence.length ();
    // If cursor is at first position or at least one character beyond the sentence - no text will be selected.
    if (cursorLocation == 0 || cursorLocation > ln) {
      curr = "";
    }
    else {
      int pos = cursorLocation - 1;
      while (pos >= 0) { // we look at characters to the left of the cursor, one by one
        char ch = sentence.charAt (pos);
        if (Character.isLetterOrDigit (ch) || ch == '_' || ch == '-' || ch == '.') {  // Acceptable characters  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          if (pos == 0) // Already reach beginning of the line
            break;
          else
            pos--;
        }
        else {    // Not an acceptable character
          pos++;  // move back 1 character to the beginning of the token
          break;
        }
      }
      start = pos; // starting point of the token in the given sentence

      pos = cursorLocation;
      while (pos < ln) { // we look at characters to the right of the cursor, one by one
        char ch = sentence.charAt (pos);
        if (!Character.isLetterOrDigit (ch)) {
          if (ch == '_' || ch == '-' || ch == '.') {  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            pos++;
          }
          else {
            break; // if character is not alphanumeric or any of the accepted special characters, stop looking right.
          }
        }
        else {
          if (pos == sentence.length () - 1)  // Already reach the end of the line
            break;
          else
            pos++;
        }
      }
      end = pos; // end point of the token in the given sentence
    }
    curr = sentence.substring (start, end);
    curr = trimDeducedToken (curr);
    return curr;
  }

  /**
   * Removes enclosing single quotes if present, removes surrounding whitespace
   * 
   * @param tkn
   * @return
   */
  public static String trimDeducedToken (String tkn)
  {
    if (tkn.length () > 0) {
      if (tkn.indexOf ("'") != -1) {
        tkn = tkn.substring (tkn.indexOf ("'") + 1);
        tkn = tkn.substring (0, tkn.indexOf ("'"));
      }
      tkn = tkn.trim ();
    }
    return tkn;
  }
}
