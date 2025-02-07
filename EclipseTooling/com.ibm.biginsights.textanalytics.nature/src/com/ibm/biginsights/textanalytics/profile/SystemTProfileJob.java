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
package com.ibm.biginsights.textanalytics.profile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.algebra.oldscan.DBDumpFileScan;
import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.AQLProfiler;
import com.ibm.avatar.api.AQLProfiler.ProfileSummary;
import com.ibm.avatar.api.ExternalTypeInfo;
import com.ibm.avatar.api.ExternalTypeInfoFactory;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.run.SystemTRunConfig;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectDependencyUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class SystemTProfileJob extends Job
{

  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  protected SystemTRunConfig runConfig;
  protected int minSeconds;
  protected String projectName;
  protected AQLProfiler profiler;
  protected IProgressMonitor monitor;
  boolean isModularProject = false;

  private static final String newLine = "\n"; //$NON-NLS-1$

  public SystemTProfileJob (String projectName, String name, SystemTRunConfig runConfig, int minSeconds)
  {
    super (name);

    this.runConfig = runConfig;
    this.minSeconds = minSeconds;
    this.projectName = projectName;
    isModularProject = ProjectUtils.isModularProject (projectName);
  }

  @Override
  public IStatus run (IProgressMonitor monitor)
  {
    this.monitor = monitor;

    // -------- Validate project existence --------//
    IProject project = ProjectPreferencesUtil.getProject (projectName);
    if (project == null || false == project.exists ()) { return createErrorStatus (Messages.getString (
      "SystemTMainTab.PROJECT_NOT_EXIST", new Object[] { projectName })); //$NON-NLS-1$
    }

    // -----------Validate if project is open--------//
    if (false == project.isOpen ()) { return createErrorStatus (Messages.getString (
      "SystemTMainTab.PROJECT_IS_CLOSED", new Object[] { projectName }));//$NON-NLS-1$
    }

    /**
     * Check whether the TAM is created. If no TAM then trigger a build.
     */
    String selectedModules = runConfig.getSelectedModules ();
    String modules[] = selectedModules.split (Constants.DATAPATH_SEPARATOR);
    String tamFolder = "";
    if (isModularProject) {

      // ----------------Validate if there are any project level error markers--------------------//
      if (ProjectUtils.hasProjectErrors (project)) { return createErrorStatus ((Messages.getString (
        "SystemTMainTab.PROJECT_HAS_ERROR_MARKERS", new Object[] { project.getName () }))); }

      // ---------Validate if modules have build errors------//
      // Get all the modules in the project that have build errors in them.
      Set<String> modulesWithErrors = ProjectUtils.getModulesWithError (project);
      List<String> errorModulesSelected = new ArrayList<String> ();
      for (String module : modules) {
        // Check if the checked module has errors in them, If yes add it to the list
        if (modulesWithErrors.contains (module)) {
          errorModulesSelected.add (module);
        }
      }
      // If modules having errors are selected then display error message.
      if (errorModulesSelected.isEmpty () == false) { return createErrorStatus ((Messages.getString (
        "SystemTMainTab.SELECTED_MODULE_HAS_ERRORS", new Object[] { errorModulesSelected.toString () }))); }

      // getAbsolutePath() will no longer work on SystemTProperties.getModuleBinPath() and
      // SystemTProperties.getModuleSrcPath() - their values will be relative to project.
      // Using another way to get absolute path for module bin directory.
      IResource tamDir = ProjectUtils.getTextAnalyticsBinFolder (runConfig.getProjectName ());
      if (tamDir != null) {
        tamFolder = tamDir.getLocation ().toString () + Constants.DATAPATH_SEPARATOR;
      }
    }
    else {
      tamFolder = ProjectPreferencesUtil.getAbsolutePath (runConfig.getAogPath ());
    }
    if (selectedModules != null) {

      for (int i = 0; i < modules.length; i++) {
        if (!ProjectPreferencesUtil.isCompiledTAMExist (tamFolder, modules[i])) {
          try {
            project.build (IncrementalProjectBuilder.FULL_BUILD, null);
            break;
          }
          catch (CoreException e1) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e1.getMessage ());
          }
        }
      }

    }

    IStatus buildVerifyStatus = verifyNoBuildErrors ();
    if (buildVerifyStatus != null) { return buildVerifyStatus; }

    String tamPathStr = "";//$NON-NLS-1$
    if (isModularProject) {
      tamPathStr = ProjectDependencyUtil.populateProjectDependencyPath (project);

    }
    else {
      tamPathStr = new File (ProjectPreferencesUtil.getAbsolutePath (runConfig.getAogPath ())).toURI ().toString ()
        + Constants.DATAPATH_SEPARATOR;
    }

    TokenizerConfig tokenizerConfig = null;
 
    LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (
    		Messages.getString ("SystemtRunJob.INFO_USING_WHITESPC_TKNZR")); //$NON-NLS-1$
    
    ExternalTypeInfo extEnt = ExternalTypeInfoFactory.createInstance ();
    for (Map.Entry<String, String> extTableMapping : runConfig.getExternalTablesFileMapping ().entrySet ()) {
      String filePath = extTableMapping.getValue ();
      if (!filePath.trim ().isEmpty ()) {
        File f = FileUtils.createValidatedFile (ProjectPreferencesUtil.deduceAbsolutePath (filePath));
        extEnt.addTable (extTableMapping.getKey (), f.toURI ().toString ());
      }
    }

    for (Map.Entry<String, String> extDictMapping : runConfig.getExternalDictionariesFileMapping ().entrySet ()) {
      String filePath = extDictMapping.getValue ();
      if (!filePath.trim ().isEmpty ()) {
        File f = FileUtils.createValidatedFile (ProjectPreferencesUtil.deduceAbsolutePath (filePath));
        extEnt.addDictionary (extDictMapping.getKey (), f.toURI ().toString ());
      }
    }

    profiler = AQLProfiler.createCompiledModulesProfiler (modules, tamPathStr, extEnt, tokenizerConfig);
    profiler.setMinRuntimeSec (minSeconds);

    String strInputCollection = ProjectPreferencesUtil.getAbsolutePath (runConfig.getInputCollection ());

    IStatus validationStatus = validateInput (strInputCollection, runConfig.getLang ());

    if (validationStatus != null) { return validationStatus; }

    final StringBuffer viewOutput = new StringBuffer (5120);
    StringBuffer strBuffer = new StringBuffer (1024);

    // Print out info on the input
    strBuffer = new StringBuffer (1024);

    if (!isModularProject) {
      SystemTProperties prop = ProjectPreferencesUtil.getSystemTProperties (project);
      strBuffer.append (String.format ("Compiling '%s'\n",
        ProjectPreferencesUtil.getAbsolutePath (prop.getMainAQLFile ())));
      // aqlFile.getCanonicalPath()));
      strBuffer.append (String.format ("Data Path: '%s'\n",
        ProjectPreferencesUtil.getAbsolutePath (prop.getSearchPath ())));

      viewOutput.append (strBuffer.toString ());
      viewOutput.append (newLine);

    }
    else {
      strBuffer.append (String.format ("Compiling '%s'\n", selectedModules));
      viewOutput.append (strBuffer.toString ());
      viewOutput.append (newLine);
    }

    try {

      monitor.beginTask (Messages.getString ("SystemTProfileJob.INFO_PROFILING_AQL"), minSeconds + 2);// 2 for dumping
                                                                                                      // the results in
                                                                                                      // the Profiler
      // View
      // Start a background thread that monitors for the user's cancel
      // requests,
      // and communicates the cancel request to the AQLProfiler
      ProgressAndCancelMonitorThread proCanMonThread = new ProgressAndCancelMonitorThread ();

      // The background thread will keep running
      proCanMonThread.start ();

      // Start profiling. The background thread will watch for cancel
      // requests
      File docsFile = FileUtils.createValidatedFile (strInputCollection);
      profiler.profile (docsFile, LangCode.strToLangCode (runConfig.getLang ()), null, runConfig.getCsvDelimiterChar ());

      if (monitor.isCanceled ()) {
        hideProfilerView ();
        return Status.CANCEL_STATUS;
      }

      // Don't compute profiler statistics if the job has been canceled

      ProfileSummary summary = profiler.collectSummary ();

      // How many statements did we compile ? Print out compiler warnings
      strBuffer = new StringBuffer (1024);
      strBuffer.append (String.format ("Info: Compiled %d AQL statements\n", summary.numStmts));
      profiler.dumpWarnings (strBuffer);
      strBuffer.append (String.format ("Info: Profiler started. Will run for at least %d seconds.\n", minSeconds));

      strBuffer.append (String.format ("Info: Gathered %d samples in %.2f seconds\n", summary.numSamples,
        profiler.getRuntimeSec ()));
      viewOutput.append (strBuffer.toString ());
      viewOutput.append (newLine);

      // Top 25 views
      strBuffer = new StringBuffer (1024);
      profiler.dumpTopViews (strBuffer);
      viewOutput.append (strBuffer.toString ());
      viewOutput.append (newLine);

      // Top documents by running time
      strBuffer = new StringBuffer (1024);
      profiler.dumpTopDocuments (strBuffer);
      viewOutput.append (strBuffer.toString ());
      viewOutput.append (newLine);

      // Throughput info
      strBuffer = new StringBuffer (1024);
      String throughputLine = String.format ("\nProcessed %d characters in %1.2f sec --> Throughput %1.2f kb/sec\n",
        profiler.getTotalChar (), profiler.getRuntimeSec (), //
        profiler.getCharPerSec () / (double) 1024);

      for (int i = 0; i < throughputLine.length (); i++)
        strBuffer.append ("-");
      strBuffer.append (throughputLine);
      for (int i = 0; i < throughputLine.length (); i++)
        strBuffer.append ("-");

      viewOutput.append (strBuffer.toString ());

      // Don't print the profiler output if the job has been canceled
      if (monitor.isCanceled ()) return Status.CANCEL_STATUS;

      Display.getDefault ().asyncExec (new Runnable () {

        @Override
        public void run ()
        {
          try {
            final IWorkbenchWindow window = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ();
            final ProfileView view = (ProfileView) window.getActivePage ().showView (ProfileView.VIEW_ID, null,
              IWorkbenchPage.VIEW_ACTIVATE);
            view.setTextContent (viewOutput.toString ());
            view.setViewName ("Profiler View - " + projectName);
          }
          catch (final PartInitException e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError ("Profile View could not be opened", e);
          }
        }
      });

      // Don't log any profiler output if the job has been canceled
      if (monitor.isCanceled ()) return Status.CANCEL_STATUS;

      // Log top 25 operators to Eclipse log
      strBuffer = new StringBuffer (1024);
      strBuffer.append ("\n");
      profiler.dumpTopOperators (strBuffer);
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (strBuffer.toString ());
      /*
       * LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logInfo( String.format
       * ("Processed %d char in %1.2f sec --> %1.2f kb/sec\n", profiler.getTotalChar(), profiler.getRuntimeSec(), //
       * profiler.getCharPerSec() / (double) 1024));
       */
      monitor.worked (2); // assuming that this job took some time to print
      // output to viewer. The count of 2 alloted
      // earlier is done now.
      return Status.OK_STATUS;

    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError ("Error profiling AQL", e);
      return createErrorStatus (Messages.getString ("SystemTProfileJob.ERR_PROFILING_AQL")); //$NON-NLS-1$
    }
    finally {
      profiler = null;
      monitor.done ();
      this.monitor = null;
    }
  }

  private IStatus validateInput (String strInputCollection, String languageName)
  {

    // Validate the language
    try {
      LangCode.strToLangCode (languageName);
    }
    catch (final IllegalArgumentException e) {
      return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_UNKNOWN_LANG_CODE") + languageName); //$NON-NLS-1$
    }

    if (strInputCollection == null) { return createErrorStatus (Messages.getString ("General.ERR_INPUT_COLLECTION_NOT_FOUND")
      + ": " + runConfig.getInputCollection ().replace (Constants.WORKSPACE_RESOURCE_PREFIX, ""));//$NON-NLS-1$
    }
    File inputFile = FileUtils.createValidatedFile (strInputCollection);
    if (!inputFile.exists ()) { return createErrorStatus (Messages.getString ("General.ERR_INPUT_COLLECTION_NOT_FOUND")//$NON-NLS-1$
      + ": " + strInputCollection); }
    if (inputFile.isDirectory ()) {
      String[] invalidFiles = inputFile.list (new FilenameFilter () {
        @Override
        public boolean accept (File dir, String name)
        {
          if (name.endsWith (".json") || name.endsWith (".zip") || name.endsWith ("tar.gz") || name.endsWith (".tar") || name.endsWith (".tgz") || name.endsWith (".del") || name.endsWith (".csv")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
            return true;
          return false;
        }
      });

      if (invalidFiles != null && invalidFiles.length > 0) {
        String invalidFileFormats = invalidFiles[0];
        for (int i = 1; i < invalidFiles.length; i++) {
          invalidFileFormats = invalidFileFormats + "," + invalidFiles[i];
        }
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowWarning (
          Messages.getString ("SystemtRunJob.InputCollectionContainsInvalidFilesMessage") + invalidFileFormats); //$NON-NLS-1$
      }
    }
    // Validation for 3 column .del files. Shows an error message if the given .del file is invalid.
    if (inputFile.isFile () && inputFile.getName ().endsWith (".del")) {
      try {
        DBDumpFileScan dbDump = (DBDumpFileScan) DBDumpFileScan.makeFileScan (inputFile);
        if (!dbDump.getHaveLabelCol ()) { return createErrorStatus (Messages.getString ("SystemtRunJob.InvalidDelFormat")
          + inputFile); }
      }
      catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
    }
    if (inputFile.isFile () && inputFile.length () <= 0) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_INPUT_COLLECTION_SIZE_INVALID") + inputFile.getAbsolutePath ()); //$NON-NLS-1$
    }
    if (isModularProject) {
      for (Map.Entry<String, String> extDictMapping : runConfig.getExternalDictionariesFileMapping ().entrySet ()) {
        String filePath = extDictMapping.getValue ();
        if (!filePath.trim ().isEmpty ()) {
          File dictFile = FileUtils.createValidatedFile (ProjectPreferencesUtil.deduceAbsolutePath (filePath));
          if (!dictFile.exists ()) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_DICT_FILE_NOT_FOUND") + dictFile.getAbsolutePath ()); //$NON-NLS-1$
          }
        }
      }
      for (Map.Entry<String, String> extTableMapping : runConfig.getExternalTablesFileMapping ().entrySet ()) {
        String filePath = extTableMapping.getValue ();
        if (!filePath.trim ().isEmpty ()) {
          File tableFile = FileUtils.createValidatedFile (ProjectPreferencesUtil.deduceAbsolutePath (filePath));
          if (!tableFile.exists ()) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_TABLE_FILE_NOT_FOUND") + tableFile.getAbsolutePath ()); //$NON-NLS-1$
          }
        }
      }
    }
    return null;
  }

  private static final IStatus createErrorStatus (String message)
  {
    return new Status (IStatus.ERROR, Activator.PLUGIN_ID, message);
  }

  private static final IStatus createErrorStatus (String message, Throwable e)
  {
    return new Status (IStatus.ERROR, Activator.PLUGIN_ID, message, e);
  }

  private IStatus verifyNoBuildErrors ()
  {
    // do not check if the project is modular as we are checking the modular level error markers in run method.
    if (isModularProject == false) {
      if (ProjectUtils.hasBuildErrors (ProjectPreferencesUtil.getProject (projectName))) { return createErrorStatus (Messages.getString ("General.ERR_PROJECT_HAS_BUILD_ERRORS"));//$NON-NLS-1$
      }
    }
    return null;
  }

  /**
   * Background thread that watches the ProgressMonitor for cancel requests and communicates them to the AQLProfiler.
   * 
   * 
   */
  private class ProgressAndCancelMonitorThread extends Thread
  {

    /*
     * It is not guaranteed that the ProgressAndCancelMonitorThread starts at the same time as profiler thread. Hence
     * when checking if the timeElapsed > minSecs, we should ideally give some allowance for the time delay in
     * scheduling the current thread. Fixing it to 5 seconds for now.
     */
    private static final int THREAD_START_DELAY_ALLOWANCE = 5; // seconds

    int timeElapsed = 0;

    @Override
    public void run ()
    {
      try {
        boolean timeLapseMessageShown = false;

        // Keep going while the monitor has not been canceled
        while (null != monitor && !monitor.isCanceled () && null != profiler) {
          Thread.sleep (2000);
          timeElapsed += 2;
          if (monitor != null) {// check for null, just in case
            // monitor became null by the time
            // current thread woke up!
            monitor.worked (2);
            if (!timeLapseMessageShown && (timeElapsed > (minSeconds + THREAD_START_DELAY_ALLOWANCE))) {
              String msg = Messages.getString ("SystemTProfileJob.INFO_MIN_TIME_ELAPSED");//$NON-NLS-1$
              monitor.subTask (msg);
              timeLapseMessageShown = true;
            }
          }
          else {
            break;
          }

        }
      }
      catch (InterruptedException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          Messages.getString ("SystemTProfileJob.ERR_INTERRUPTED_CANCEL_MONITOR_THREAD"), e);
      }

      // At this point, either the monitor has been canceled,
      // or the profiler job finished by itself
      if (monitor != null && monitor.isCanceled () && profiler != null) {
        profiler.stop ();
      }
    }
  };

  private void hideProfilerView ()
  {
    Display.getDefault ().asyncExec (new Runnable () {

      @Override
      public void run ()
      {
        final IWorkbenchWindow window = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ();
        final ProfileView view = (ProfileView) window.getActivePage ().findView (ProfileView.VIEW_ID);
        final IWorkbenchPage wbPage = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
        wbPage.hideView (view);
      }

    });
  }
}
