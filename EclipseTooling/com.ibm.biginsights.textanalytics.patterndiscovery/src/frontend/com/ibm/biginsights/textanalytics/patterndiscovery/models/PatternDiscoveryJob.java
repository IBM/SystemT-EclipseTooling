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
package com.ibm.biginsights.textanalytics.patterndiscovery.models;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.algebra.datamodel.Pair;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.patterndiscovery.Activator;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.properties.PropertiesContainer;
import com.ibm.biginsights.textanalytics.patterndiscovery.views.CommonSignatureTableView;
import com.ibm.biginsights.textanalytics.patterndiscovery.views.PatternDiscoveryTableView;
import com.ibm.biginsights.textanalytics.patterndiscovery.views.PatternDiscoveryView;
import com.ibm.biginsights.textanalytics.patterndiscovery.views.SemanticSignatureTableView;

/**
 * this class is the one that communicates with the back end and execute most of the action called from the ui
 * 
 * 
 */
public class PatternDiscoveryJob extends Job
{



  // TODO: describe variables
  public static final String PART_NAME = "PrefuseJob";
  private String dbName;
  private String driverName;
  private String rootDir;
  private ExperimentProperties properties;
  private GroupByNewProcessor processor;
  private String docDir;
  private String docFile;
  private int minSize, maxSize, available;
  private boolean doProcess, doReset;
  private int minRange, maxRange;
  private int limit;
  private int minSeqLength, maxSeqLength;

  private boolean test;

  public static PatternDiscoveryJob job;

  /**
   * TODO: add descriptions
   * 
   * @param name
   * @param currentSelection
   * @throws Exception
   * @throws
   */
  public PatternDiscoveryJob (String name, PropertiesContainer params, int minSize, int maxSize, int limit)
  {
    super (name);
    // this variable is used when we want to redraw without running the
    // GroupByNewProcessor
    doProcess = true;
    doReset = true;
    // this is the minimum size of the bubbles that we want to display

    setMinSize (minSize);
    setMaxSize (maxSize);
    setLimit (limit);

    reloadProcessor (params);
    job = this;
  }

  public PatternDiscoveryJob (String name, PropertiesContainer params, int minSize, int maxSize, int limit, boolean test)
  {
    super (name);
    // this variable is used when we want to redraw without running the
    // GroupByNewProcessor
    doProcess = true;
    doReset = true;
    // this is the minimum size of the bubbles that we want to display

    setMinSize (minSize);
    setMaxSize (maxSize);
    setLimit (limit);

    this.test = test;

    reloadProcessor (params);
    job = this;
  }

  public ExperimentProperties getProperties ()
  {
    return properties;
  }

  /**
   * reloads the processor (backend handler) and sets all the needed parameters
   * 
   * @param params
   */
  public void reloadProcessor (PropertiesContainer params)
  {
    processor = new GroupByNewProcessor ();
    properties = processor.initializeEmptyProperty ();

    // added this check because in the test case we do not have this property set by default
    if (params.getProperty (PDConstants.PD_AOG_PATH_PROP) != null)
      properties.setAogFile (new File (params.getProperty (PDConstants.PD_AOG_PATH_PROP)));

    properties.setLanguage (params.getProperty (PDConstants.PD_LANGUAGE_PROP));

    properties.setProperty (PropertyConstants.AQL_VIEW_NAME, params.getProperty (PropertyConstants.AQL_VIEW_NAME));

    Properties propsObjec = params.getPropertiesObject ();

    for (Object prop_name : propsObjec.keySet ()) {
      if ( prop_name instanceof String )
        properties.put (prop_name, propsObjec.get (prop_name));
    }

    File dataFolder = new File (params.getProperty (PropertyConstants.DOCUMENT_COLLECTION_DIR));

    docFile = dataFolder.getName ();

    String pathSeparator = System.getProperty ("file.separator");

    docDir = dataFolder.getParentFile ().getAbsolutePath () + pathSeparator;

    properties.setProperty (PropertyConstants.INPUT_DOCUMENT_NAME, docFile);
    properties.setProperty (PropertyConstants.DOCUMENT_COLLECTION_DIR, docDir);

    String tempName = docFile;
    if (tempName.contains (".")) tempName = tempName.substring (0, tempName.indexOf ('.'));

    rootDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR) + tempName + PDConstants.FILE_SEPARATOR;

    dbName = properties.getProperty (PropertyConstants.RESULTS_DB_NAME);
    driverName = properties.getProperty (PropertyConstants.DB_PREFIX);

    minSeqLength = Integer.parseInt (properties.getProperty (PropertyConstants.SEQUENCE_MIN_SIZE));
    maxSeqLength = Integer.parseInt (properties.getProperty (PropertyConstants.SEQUENCE_MAX_SIZE));
  }

  /**
   * load the min and max groups size available
   */
  public void loadRange ()
  {

    ResultSet result = null;

    String query = String.format (
      "SELECT * FROM (SELECT count(*) size FROM %s WHERE JSEQUENCE != '}' GROUP BY JSEQUENCE) AS T ORDER BY size DESC",
      getTableName ());

    minRange = 0;
    maxRange = 0;

    try {
      result = readFromDB (query);

      available = 0;

      if (result.next ()) {
        available++;
        maxRange = Integer.valueOf (result.getString (1));
        minRange = maxRange;
      }

      while (result.next ()) {
        available++;
        minRange = Integer.valueOf (result.getString (1));
      }

      result.close ();
      job.shutDownDB ();
    }
    catch (SQLException e) {
      ErrorMessages.LogError (ErrorMessages.PATTERN_DISCOVERY_ERROR_READING_DATA_FROM_DB, e);
    }
  }

  /**
   * @return the min and max sequence length used specified by the user as a Pair
   */
  public Pair<Integer, Integer> getMinMaxSeqLength ()
  {
    return new Pair<Integer, Integer> (minSeqLength, maxSeqLength);
  }

  /**
   * @return the number of groups available
   */
  public int getAvailableBubbles ()
  {
    return available;
  }

  /**
   * sets the limit of groups to be loaded in the ui
   * 
   * @param limit
   */
  public void setLimit (int limit)
  {
    this.limit = limit;
  }

  /**
   * @return the limit of groups to be loaded in the ui
   */
  public int getLimit ()
  {
    return limit;
  }

  /**
   * sets the smallest size of a group to display
   * 
   * @param minSize
   */
  public void setMinSize (int minSize)
  {
    this.minSize = minSize;
  }

  /**
   * the smallest size of a group to display
   * 
   * @return
   */
  public int getMin ()
  {
    return minSize;
  }

  /**
   * sets the largest size of a group to display
   * 
   * @param maxSize
   */
  public void setMaxSize (int maxSize)
  {
    this.maxSize = maxSize;
  }

  /**
   * @return the largest size of a group to display
   */
  public int getMax ()
  {
    return maxSize;
  }

  /**
   * sets if the pd processor should be ran again
   * 
   * @param doProcess
   */
  public void setProcessLevel (boolean doProcess)
  {
    this.doProcess = doProcess;
  }

  /**
   * @return if the pd processor should be ran again
   */
  public boolean getProcessLevel ()
  {
    return doProcess;
  }

  /**
   * tells to reset min and max groups size in the next run
   * 
   * @param doReset
   */
  public void setResetMinMax (boolean doReset)
  {
    this.doReset = doReset;
  }

  /**
   * resets the min and max group sizes to the default values
   */
  public void resetMinMax ()
  {
    minSize = -1;
    maxSize = -1;
  }

  /**
   * @param key
   * @return the value for the property with key key
   */
  public String getProperty (String key)
  {
    return properties.getProperty (key);
  }

  /**
   * sets the property with key key
   * 
   * @param key
   * @param value
   */
  public void setProperty (String key, String value)
  {
    properties.setProperty (key, value);
  }

  public String getDbUrl ()
  {
    return driverName + rootDir + dbName;
  }

  public String getDBName ()
  {
    return dbName;
  }

  public String getTableName ()
  {
    return "APP.groupingjaccard_" + getViewName ().replace (".", "__");
  }

  public String getTypeTableName ()
  {
    return "AOMDATA.type_" + getViewName ().replace (".", "__");
  }

  public String getViewName ()
  {
    return properties.getProperty (PropertyConstants.AQL_VIEW_NAME);
  }

  /**
   * run the GroupBy Processor
   * 
   * @throws Exception
   */
  public boolean runProcessor (IProgressMonitor monitor) throws PatternDiscoveryException
  {
    return processor.run (monitor);
  }

  private void doCancel ()
  {
    processor.cancel ();
  }

  /**
   * @param query
   * @throws SQLException
   */
  public void writeToDb (String query) throws SQLException
  {
    DebugDBProcessor db = new DebugDBProcessor (getDbUrl ());
    db.setProperties (properties);
    db.writeToDB (query, null);
    db.shutdown ();
  }

  /**
   * @param query
   * @return
   * @throws SQLException
   */
  public ResultSet readFromDB (String query) throws SQLException
  {
    DebugDBProcessor db = new DebugDBProcessor (getDbUrl ());
    db.setProperties (properties);
    return db.readFromDB (query);
  }

  /**
   * this is only used for debugging purposes
   * 
   * @param query
   */
  public void printQueryResults (String query)
  {
    DebugDBProcessor db = new DebugDBProcessor (getDbUrl ());
    db.setProperties (properties);
    db.printQueryResult (query);
    db.shutdown ();
  }

  public void shutDownDB ()
  {
    DebugDBProcessor db = new DebugDBProcessor (getDbUrl ());
    db.setProperties (properties);
    db.shutdown ();
    if (!test) refreshProjectFiles ();
  }

  /**
   * gets the minimum and the maximum size of the bubbles
   * 
   * @return an array containing the minimum in position 0 and maximum in position 1
   */
  public int[] getRange ()
  {
    return new int[] { minRange, maxRange };
  }

  /**
   * send the run action to the back end and waits for the results, which are passed to the user interface that creates
   * the apple to display them
   */
  @Override
  protected IStatus run (final IProgressMonitor monitor)
  {
    try {
      if (doProcess) {

        String msg = validateInput();
        if (msg != null)
          return new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg);

        PDCancel flag = new PDCancel (monitor);
        flag.start ();

        boolean status = false;

        try {
          status = runProcessor (monitor);
        }
        catch (PatternDiscoveryException e) {
          ErrorMessages.LogErrorMessage (e.getError (), e);
        }

        flag.done ();
        // make sure that even if the user cancel the operation still we refresh the pattern discovery directory
        refreshProjectFiles ();

        if (!status) return Status.CANCEL_STATUS;

        loadRange ();
        if (minSize < 0) minSize = minRange;
        if (maxSize < 0) maxSize = maxRange;
      }

      final PatternDiscoveryJob job = this;

      Display.getDefault ().asyncExec (new Runnable () {
        @Override
        public void run ()
        {

          try {
            IWorkbenchPage wbPage = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();

            IViewPart cst = wbPage.findView (CommonSignatureTableView.VIEW_ID);
            if (cst != null) wbPage.hideView (cst);

            IViewPart sst = wbPage.findView (SemanticSignatureTableView.VIEW_ID);
            if (sst != null) wbPage.hideView (sst);

            PatternDiscoveryView prevView = (PatternDiscoveryView) wbPage.findView (PatternDiscoveryView.VIEW_ID);

            PatternDiscoveryView.setPatternDiscoveryJob (job);

            if (prevView != null) {
              prevView.updateView (doReset);
            }

            wbPage.showView (PatternDiscoveryView.VIEW_ID);

            PatternDiscoveryTableView tabView = (PatternDiscoveryTableView)wbPage.findView (PatternDiscoveryView.TABLEVIEW_ID);
            if (tabView != null) {
              wbPage.hideView (tabView);
              wbPage.showView (PatternDiscoveryView.TABLEVIEW_ID);
            }

            setProcessLevel (true);
            setResetMinMax (true);

          }
          catch (Exception e) {
            IWorkbenchPage wbPage = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();

            IViewPart part = wbPage.findView (PatternDiscoveryView.VIEW_ID);
            if (part != null) wbPage.hideView (part);

            ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_OPENING_PREFUSE_VIEW, e);
          }
        }
      });
    }
    catch (Exception e1) {
      ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_PROCESSING_ERR, e1);
    }

    return Status.OK_STATUS;
  }

  private String validateInput ()
  {
    // For now, only validate the .del files. (defect 14620)
    String docPath = properties.getProperty (PropertyConstants.DOCUMENT_COLLECTION_DIR) + properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
    File docFile = new File(docPath);

    if (docFile.isFile ()) {
      if (ProjectPreferencesUtil.isNonSupportedDelFile (docFile))
        return Messages.getString ("SystemtRunJob.InvalidDelFormat");
    }
    else if (docFile.isDirectory ()) {
      for (File f : docFile.listFiles ()) {
        if (f.isFile () && ProjectPreferencesUtil.isNonSupportedDelFile (f))
          return Messages.getString ("SystemtRunJob.InvalidDelFormat");
      }
    }

    return null;
  }

  /**
   * when files are modified programatically eclipse may not be aware of this, therefore is good to refresh them
   */
  private void refreshProjectFiles ()
  {
    String projectName = properties.getProperty (PDConstants.PD_PROJECT_NAME_PROP);
    IProject project = ResourcesPlugin.getWorkspace ().getRoot ().getProject (projectName);
    try {
      project.refreshLocal (IResource.DEPTH_INFINITE, new NullProgressMonitor ());
      IFolder folder = project.getFolder (PDConstants.PATTERN_DISCOVERY_TEMP_DIR_NAME);
      folder.refreshLocal (IResource.DEPTH_INFINITE, new NullProgressMonitor ());
    }
    catch (CoreException e) {
      ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_PROCESSING_ERR, e);
    }
  }

  /**
   * thread used to cancel the current pattern discovery processing operation. this listen for the IProgressMonitor
   * cancel operation or when the done() method is called
   * 
   * 
   */
  class PDCancel extends Thread
  {

    IProgressMonitor monitor;
    Boolean state;

    public PDCancel (IProgressMonitor monitor)
    {
      this.monitor = monitor;
      this.state = false;
    }

    public void done ()
    {
      this.state = true;
    }

    @Override
    public void run ()
    {
      while (!monitor.isCanceled ()) {
        if (state) return;
        try {
          Thread.sleep (2000);
        }
        catch (InterruptedException e) {
          e.printStackTrace ();
        }
        if (monitor == null) return; // in case the monitor's activity concludes in the 2 secs we were waiting
      }
      doCancel ();
    }
  }

}
