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
package com.ibm.biginsights.textanalytics.nature.run;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.algebra.oldscan.DBDumpFileScan;
import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.DocReader;
import com.ibm.avatar.api.ExternalTypeInfo;
import com.ibm.avatar.api.ExternalTypeInfoFactory;
import com.ibm.avatar.api.OperatorGraph;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.avatar.api.exceptions.UnrecognizedFileFormatException;
import com.ibm.biginsights.textanalytics.concordance.ui.ConcordanceView;
import com.ibm.biginsights.textanalytics.nature.AQLBuilder;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.resultviewer.util.PaginationTracker;
import com.ibm.biginsights.textanalytics.resultviewer.utils.AnnotationExplorerUtil;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.treeview.util.AQLTreeViewUtility;
import com.ibm.biginsights.textanalytics.treeview.view.AQLResultTreeView;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class SystemtRunJob extends Job
{
	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

  private final IProject project;
  private final IWorkspaceRoot workspaceRoot;
  private ArrayList<String> strfFilesThatExist = new ArrayList<String> ();

  private final SystemTRunConfig runConfig;

  private final String currentTimeStamp;
  private File ioFileToAnalyze = null;
  private String filesToAnalyze = null;
  private LangCode langCode = null;
  private int tokenizer;
  private String tamPath = null;
  private IFolder resultFolder;
  private IFolder rootResultFolder;
  private String resultDirPath;
  private List<String> filesToIgnore;
  TokenizerConfig tokenizerConfig = null;

  private ProvenanceRunParams provenanceRunParams;
  private String tempDir;
  boolean isModularProject = false;
  private String[] selectedModules;
  boolean isJsonFile = false;
  boolean isCsvFile = false;

  public SystemtRunJob (String jobName, IProject project, SystemTRunConfig runConfig)
  {
    super (jobName);
    this.project = project;
    this.workspaceRoot = project.getWorkspace ().getRoot ();
    this.runConfig = runConfig;
    SimpleDateFormat sdf = new SimpleDateFormat ("MM-dd-yyyy-HHmmss");//$NON-NLS-1$
    this.currentTimeStamp = sdf.format (new Date ());
    isModularProject = ProjectUtils.isModularProject (project);
  }

  private IStatus validateModularInputs () throws CoreException, TextAnalyticsException
  {
    // Read preferences for run
    tamPath = getBinPath ();
    rootResultFolder = ProjectUtils.getRootResultFolder (project);
    resultDirPath = getResultDirPath ();
    String languageName = getLanguageName ();
    tokenizer = this.runConfig.getTokenizerChoice ();
    String strInputCollection = getAnalyzeInput ();
    filesToAnalyze = ProjectPreferencesUtil.getAbsolutePath (strInputCollection);

    filesToIgnore = getFilesToIgnore ();

    // Validate location of TAM. If null, default to project root.
    if (tamPath == null) {
      return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_TAM_FOLDER_DOES_NOT_EXIST")); //$NON-NLS-1$
    }
    else {
      if (!isValidPath (tamPath)) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_TAM_FOLDER_DOES_NOT_EXIST")); //$NON-NLS-1$
      }
    }
    // ----------------Validate if there are any project level error markers--------------------//
    if (ProjectUtils.hasProjectErrors (project)) { return createErrorStatus ((Messages.getString (
      "SystemTMainTab.PROJECT_HAS_ERROR_MARKERS", new Object[] { project.getName () }))); }
    
    // Get all the modules in the project that have build errors in them.
    Set<String> modulesWithErrors = ProjectUtils.getModulesWithError (project);

    // ---------Validate if modules have build errors------//
    List<String> errorModulesSelected = new ArrayList<String> ();
    for (String module : selectedModules) {
      // Check if the checked module has errors in them, If yes add it to the list
      if (modulesWithErrors.contains (module)) {
        errorModulesSelected.add (module);
      }
    }
    // If modules having errors are selected then display error message.
    if (errorModulesSelected.isEmpty () == false) { return createErrorStatus ((Messages.getString (
      "SystemTMainTab.SELECTED_MODULE_HAS_ERRORS", new Object[] { errorModulesSelected.toString () }))); }

    /**
     * Get the selected modules, and check whether the TAM files are generated. If there is no TAM, then call the
     * Builder.
     */
    for (String selMod : selectedModules) {

      if (!ProjectPreferencesUtil.isCompiledTAMExist (tamPath, selMod)) {
        try {
          project.build (IncrementalProjectBuilder.FULL_BUILD, null);

          // After building, the module may still not exist for some reason, eg., bad tokenizer
          // config as in defect 29613. We need to check if compiled TAM exists one more time here.
          // The method verifyModularBuildErrors() that we call below doesn't work in this case
          // because there is no file with error.
          if (!ProjectPreferencesUtil.isCompiledTAMExist (tamPath, selMod)) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_TAM_FOLDER_DOES_NOT_EXIST")); //$NON-NLS-1$
          }

          break;
        }
        catch (CoreException e1) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e1.getMessage ());
        }
      }
    }

    // Verify Build Status
    IStatus buildVerifyStatus = verifyModularBuildErrors ();
    if (buildVerifyStatus != null) { return buildVerifyStatus; }

    // Validate language
    if ((languageName == null) || (languageName.length () == 0)) {
      // Default language to English
      languageName = "en";//$NON-NLS-1$
    }
    try {
      langCode = LangCode.strToLangCode (languageName);
    }
    catch (final IllegalArgumentException e) {
      return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_UNKNOWN_LANG_CODE") + languageName); //$NON-NLS-1$
    }
    // Validate result dir. Result dir must be a workspace resource.
    if (!rootResultFolder.exists ()) {
      rootResultFolder.create (false, true, new NullProgressMonitor ());
      ProjectUtils.setResultRootDir (rootResultFolder);

    }
    resultFolder = this.workspaceRoot.getFolder (new Path (resultDirPath));
    if (!resultFolder.exists ()) {
      resultFolder.create (false, true, new NullProgressMonitor ());
      // Setting the encoding explicitly to UTF-8 so that highlighting in the tree viewer
      // shows up fine without offsets that creeps in with the default windows cp1252 charset/encoding
      resultFolder.setDefaultCharset (Constants.ENCODING, new NullProgressMonitor ());
    }

    if (tokenizer == Constants.TOKENIZER_CHOICE_CUSTOM) {

    }

    // Validate Input Collections
    if (StringUtils.isEmpty (strInputCollection)) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_INVALID_INPUT_COLLECTION")); //$NON-NLS-1$
    }
    if (StringUtils.isEmpty (filesToAnalyze)) {
      String pathNotExist = strInputCollection;
      if (pathNotExist.startsWith (Constants.WORKSPACE_RESOURCE_PREFIX)) {
        pathNotExist = pathNotExist.replace (Constants.WORKSPACE_RESOURCE_PREFIX, "");//$NON-NLS-1$
      }

      return createErrorStatus (Messages.getString (
        "SystemtRunJob.ERR_INPUT_COLLECTION_NOT_FOUND", new Object[] { pathNotExist })); //$NON-NLS-1$
    }
    ioFileToAnalyze = FileUtils.createValidatedFile (filesToAnalyze);
    if (!ioFileToAnalyze.exists ()) { return createErrorStatus (Messages.getString (
      "SystemtRunJob.ERR_INPUT_COLLECTION_NOT_FOUND", new Object[] { ioFileToAnalyze.getAbsolutePath () })); //$NON-NLS-1$
    }
    if (ioFileToAnalyze.isDirectory ()) {
      File[] fileList = ioFileToAnalyze.listFiles ();
      if (fileList.length == 0) { return createWarningStatus (Messages.getString ("SystemtRunJob.INFO_INPT_COLL")
        + filesToAnalyze + Messages.getString ("SystemtRunJob.INFO_IS_EMPTY")); }
    }
    if (ioFileToAnalyze.isDirectory ()) {
      checkIfInputCollContainsInvalidFileFormats ();
    }
    for (Map.Entry<String, String> extDictMapping : runConfig.getExternalDictionariesFileMapping ().entrySet ()) {
      String filePath = extDictMapping.getValue ();
      if (!filePath.trim ().isEmpty ()) {
        String dictPath = ProjectPreferencesUtil.deduceAbsolutePath (filePath);
        // dictPath will be null if workspaceRoot.findMember could not find this file. This is when the path is chosen
        // through browse workspace.
        if (dictPath == null) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_DICT_FILE_NOT_FOUND") + filePath); //$NON-NLS-1$
        }
        File dictFile = FileUtils.createValidatedFile (ProjectPreferencesUtil.deduceAbsolutePath (filePath));
        if (!dictFile.exists ()) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_DICT_FILE_NOT_FOUND") + dictFile.getAbsolutePath ()); //$NON-NLS-1$
        }
      }
    }
    for (Map.Entry<String, String> extTableMapping : runConfig.getExternalTablesFileMapping ().entrySet ()) {
      String filePath = extTableMapping.getValue ();
      if (!filePath.trim ().isEmpty ()) {
        String tablePath = ProjectPreferencesUtil.deduceAbsolutePath (filePath);
        // tablePath will be null if workspaceRoot.findMember could not find this file. This is when the path is chosen
        // through browse workspace.
        if (tablePath == null) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_TABLE_FILE_NOT_FOUND") + filePath); //$NON-NLS-1$
        }
        File tableFile = FileUtils.createValidatedFile (ProjectPreferencesUtil.deduceAbsolutePath (filePath));
        if (!tableFile.exists ()) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_TABLE_FILE_NOT_FOUND") + tableFile.getAbsolutePath ()); //$NON-NLS-1$
        }
      }
    }

    IPath resPath = new Path (ProjectPreferencesUtil.getPath (strInputCollection));
    IResource resource = this.workspaceRoot.getFileForLocation (resPath); // somehow findMember() can't locate the
                                                                          // resource but getFileForLocation() can.
    if (resource == null) resource = this.workspaceRoot.getFolder (resPath);
    if (resource != null && resource.exists ()) {
      boolean isUTF8 = ProjectUtils.isUTF8Encoding (resource);
      if (!isUTF8) showWarningForNonUTF8Encoding ();
    }
    if (ioFileToAnalyze.isFile () && ioFileToAnalyze.getName ().endsWith (".csv")) { //$NON-NLS-1$
      isCsvFile = true;
    }
    if (ioFileToAnalyze.isFile () && ioFileToAnalyze.getName ().endsWith (".json")) { //$NON-NLS-1$
      isJsonFile = true;
    }
    if (ioFileToAnalyze.isFile () && ioFileToAnalyze.getName ().endsWith (".del")) { return validateDelFiles (); }//$NON-NLS-1$
    if (ioFileToAnalyze.isFile () && ioFileToAnalyze.length () <= 0) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_INPUT_COLLECTION_SIZE_INVALID") + ioFileToAnalyze.getAbsolutePath ()); //$NON-NLS-1$
    }
    return new Status (IStatus.OK, Activator.PLUGIN_ID, "");
  }

  private IStatus validateNonModularInputs () throws CoreException, TextAnalyticsException
  {
    // Read preferences for run
    tamPath = getBinPath ();
    rootResultFolder = ProjectUtils.getRootResultFolder (project);
    resultDirPath = getResultDirPath ();
    String languageName = getLanguageName ();
    tokenizer = runConfig.getTokenizerChoice ();
    String strInputCollection = getAnalyzeInput ();
    filesToAnalyze = ProjectPreferencesUtil.getAbsolutePath (strInputCollection);

    filesToIgnore = getFilesToIgnore ();

    // Validate location of AOG plan. If null, default to project root.
    if (tamPath == null) {
      tamPath = this.project.getFullPath ().toOSString ();
    }
    else {
      tamPath = ProjectPreferencesUtil.getAbsolutePath (tamPath);
      if (!isValidPath (tamPath)) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_TAM_FOLDER_DOES_NOT_EXIST")); //$NON-NLS-1$
      }
    }

    if (!ProjectPreferencesUtil.isCompiledTAMExist (getBinPath (), Constants.GENERIC_MODULE)) {
      project.build (IncrementalProjectBuilder.FULL_BUILD, null);
    }

    // Verify Build Status
    IStatus buildVerifyStatus = verifyNonModularBuildErrors ();
    if (buildVerifyStatus != null) { return buildVerifyStatus; }

    // Validate language
    if ((languageName == null) || (languageName.length () == 0)) {
      // Default language to English
      languageName = "en";//$NON-NLS-1$
    }
    try {
      langCode = LangCode.strToLangCode (languageName);
    }
    catch (final IllegalArgumentException e) {
      return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_UNKNOWN_LANG_CODE") + languageName); //$NON-NLS-1$
    }
    // Validate result dir. Result dir must be a workspace resource.
    if (!rootResultFolder.exists ()) {
      rootResultFolder.create (false, true, new NullProgressMonitor ());
      ProjectUtils.setResultRootDir (rootResultFolder);

    }
    resultFolder = this.workspaceRoot.getFolder (new Path (resultDirPath));
    if (!resultFolder.exists ()) {
      resultFolder.create (false, true, new NullProgressMonitor ());
      // Setting the encoding explcitly to UTF-8 so that highlighting in the treeviewer
      // shows up fine without offsets that creeps in with the default windows cp1252 charset/encoding
      resultFolder.setDefaultCharset (Constants.ENCODING, new NullProgressMonitor ());
    }

    // Validate LW data path and UIMA descriptor.
    if (tokenizer == Constants.TOKENIZER_CHOICE_CUSTOM) {

    }

    // Validate Input Collections
    if (StringUtils.isEmpty (strInputCollection)) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_INVALID_INPUT_COLLECTION")); //$NON-NLS-1$
    }
    if (StringUtils.isEmpty (filesToAnalyze)) {
      String pathNotExist = strInputCollection;
      if (pathNotExist.startsWith (Constants.WORKSPACE_RESOURCE_PREFIX)) {
        pathNotExist = pathNotExist.replace (Constants.WORKSPACE_RESOURCE_PREFIX, ""); //$NON-NLS-1$
      }

      return createErrorStatus (Messages.getString (
        "SystemtRunJob.ERR_INPUT_COLLECTION_NOT_FOUND", new Object[] { pathNotExist })); //$NON-NLS-1$

    }
    ioFileToAnalyze = FileUtils.createValidatedFile (filesToAnalyze);
    if (!ioFileToAnalyze.exists ()) { return createErrorStatus (Messages.getString (
      "SystemtRunJob.ERR_INPUT_COLLECTION_NOT_FOUND", new Object[] { ioFileToAnalyze.getAbsolutePath () })); //$NON-NLS-1$
    }
    if (ioFileToAnalyze.isDirectory ()) {
      File[] fileList = ioFileToAnalyze.listFiles ();
      if (fileList.length == 0) { return createWarningStatus (Messages.getString ("SystemtRunJob.INFO_INPT_COLL")
        + filesToAnalyze + Messages.getString ("SystemtRunJob.INFO_IS_EMPTY")); }
    }
    if (ioFileToAnalyze.isDirectory ()) {
      checkIfInputCollContainsInvalidFileFormats ();
    }

    IPath resPath = new Path (ProjectPreferencesUtil.getPath (strInputCollection));
    IResource resource = this.workspaceRoot.getFileForLocation (resPath); // somehow findMember() can't locate the
                                                                          // resource but getFileForLocation() can.
    if (resource == null) resource = this.workspaceRoot.getFolder (resPath);
    if (resource != null && resource.exists ()) {
      boolean isUTF8 = ProjectUtils.isUTF8Encoding (resource);
      if (!isUTF8) showWarningForNonUTF8Encoding ();
    }

    if (ioFileToAnalyze.isFile () && ioFileToAnalyze.getName ().endsWith (".del")) {
      validateDelFiles ();
    }
    if (ioFileToAnalyze.isFile () && ioFileToAnalyze.length () <= 0) { return createErrorStatus (Messages.getString ("SystemtRunJob.ERR_INPUT_COLLECTION_SIZE_INVALID") + ioFileToAnalyze.getAbsolutePath ()); //$NON-NLS-1$
    }
    return new Status (IStatus.OK, Activator.PLUGIN_ID, "");
  }

  /**
   * private method to show dialog box if the encoding doesnot match UTF-8. This method is also there in GS code
   */

  private void showWarningForNonUTF8Encoding ()
  {
    final TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
    final boolean displayWarning = wprefs.getPrefValidateInputEncoding ();
    if (displayWarning) {
      Display.getDefault ().syncExec (new Runnable () {
        @Override
        public void run ()
        {
          Shell shell = ProjectUtils.getActiveWorkbenchWindow ().getShell ();
          MessageDialogWithToggle msgBox = MessageDialogWithToggle.openWarning (shell,
            Messages.getString ("SystemtRunJob.IncorrectEncoding"),
            Messages.getString ("SystemtRunJob.IncorrectEncodingNotUTF8"),
            Messages.getString ("SystemtRunJob.ContinueToWarn"), displayWarning, null, null);
          wprefs.setPrefValidateInputEncoding (msgBox.getToggleState ());
          wprefs.savePreferences ();
        }
      });

    }
  }

  @Override
  public IStatus run (IProgressMonitor monitor)
  {
    hidePrevConcordanceView ();

    /*
     * Displaying of progress will be in two steps as follows. Step1 will be MAINTASK_PREPROCESSING_INPUT_DATA of input
     * collection data . This will contain 2 sub steps of (a) SUBTASK_PREPROCESSING_VALIDATING_DATA and
     * (b)SUBTASK_PREPROCESSING_COMPUTING_SIZE Step2 will be MAINTASK_RUNNING_SYSTEMT. This will contain 3 sub steps of
     * (a) Preparing to execute text analytics (PREPARE) - This is needed because the first document to annotate takes a
     * lot of time for the runtime operator graph etc to be set up), (b)Executing text analytics and persisting results
     * (ANNOTATE) and (c) Displaying results (DISPLAY) So the progress bar will reset to 0% at the beginning of second
     * step. It made sense to break it down this way - because the second step relies on document collection size and in
     * the first step we do not know the size yet. Strictly speaking you need to call the begintask only once on a
     * monitor. But thats ok for this special case and there is no detrimental effect.
     */
    monitor.beginTask (Messages.getString ("SystemTLaunchConfigurationDelegate.MAINTASK_PREPROCESSING_INPUT_DATA"), 3);
    monitor.subTask (""); // This is just dummy to show some progress bars.
    monitor.worked (1);

    monitor.subTask (Messages.getString ("SystemTLaunchConfigurationDelegate.SUBTASK_PREPROCESSING_VALIDATING_DATA"));
    try {

      if (runConfig.getSelectedModules () == null || runConfig.getSelectedModules ().isEmpty ()) {
        // The user did not selected any modules in Run Configuration.
        return createErrorStatus (Messages.getString ("SystemtRunJob.MODULE_NOT_SELECTED")); //$NON-NLS-1$
      }
      else {
        /**
         * For 2.0 projects, the User will select the modules to be executed thru the Launch UI. For 1.3 projects, the
         * selectedModule will be genericModule. This is set by the tooling code for 1.3 projects.
         */
        selectedModules = runConfig.getSelectedModules ().split (Constants.DATAPATH_SEPARATOR);
      }

      // -------- Validate project existence --------//
      if (project == null || !project.exists ()) { return createErrorStatus (Messages.getString (
        "SystemTMainTab.PROJECT_NOT_EXIST", new Object[] { project.getName () })); //$NON-NLS-1$
      }

      // -----------Validate if project is open--------//
      if (false == project.isOpen ()) { return createErrorStatus (Messages.getString (
        "SystemTMainTab.PROJECT_IS_CLOSED", new Object[] { project.getName () }));//$NON-NLS-1$
      }

      IStatus validateStatus = null;
      if (isModularProject) {
        validateStatus = validateModularInputs ();
      }
      else {
        validateStatus = validateNonModularInputs ();
      }
      if (validateStatus.getSeverity () != IStatus.OK) { return validateStatus; }

      final IFolder tmpFolder = resultFolder.getFolder (Constants.TEMP_TEXT_DIR_NAME);
      if (tmpFolder.exists ()) {
        tmpFolder.delete (true, false, new NullProgressMonitor ());
      }
      tempDir = tmpFolder.getFullPath ().toString ();

      String tamPathStr = "";
      if (isModularProject) {
        // getAbsolutePath() will no longer work on SystemTProperties.getModuleBinPath() and
        // SystemTProperties.getModuleSrcPath() - their values will be relative to project.
        // Using another way to get absolute path for module bin directory.
        IResource tamDir = ProjectUtils.getTextAnalyticsBinFolder (runConfig.getProjectName ());
        if (tamDir != null) {
          tamPathStr = tamDir.getLocation ().toString () + Constants.DATAPATH_SEPARATOR;
        }
        tamPathStr = new File (tamPathStr).toURI ().toString ();
        String tamPath = runConfig.getTamPath ();
        if (!StringUtils.isEmpty (tamPath)) {
          String tamPaths[] = tamPath.split (Constants.DATAPATH_SEPARATOR);
          for (String path : tamPaths) {
            if (path != null && !path.isEmpty ()) {
              String absPat = ProjectPreferencesUtil.getAbsolutePath (path);
              if (absPat != null) {
                File absTAMfile = FileUtils.createValidatedFile (absPat);
                if (absTAMfile.exists ()) {
                  tamPathStr += absTAMfile.toURI ().toString () + Constants.DATAPATH_SEPARATOR;
                }
                else {
                  // Adds a warning to the project that the TAM path is invalid.
                  /**
                   * There can be a scenario where the user may have deleted the folders or tam from file system and
                   * since eclipse do not get such notification, the build will not be invoked. In such cases, while
                   * running if tam path do not exist, then we put a warning to the project. Then user needs to update
                   * the TA Properties, that will remove the warnings.
                   */
                  String msg = String.format (
                    Messages.getString ("SystemtRunJob.ERR_PATH_INVALID_FOR_PRECOMPILED_TAM"),
                    path.replace (Constants.WORKSPACE_RESOURCE_PREFIX, ""));

                  ProjectUtils.addMarker (project, AQLBuilder.COMPILE_MARKER_TYPE, IMarker.SEVERITY_WARNING,
                    IMarker.PRIORITY_LOW, msg);

                }
              }
              else {
                String msg = String.format (Messages.getString ("SystemtRunJob.ERR_PATH_INVALID_FOR_PRECOMPILED_TAM"),
                  path.replace (Constants.WORKSPACE_RESOURCE_PREFIX, ""));

                ProjectUtils.addMarker (project, AQLBuilder.COMPILE_MARKER_TYPE, IMarker.SEVERITY_WARNING,
                  IMarker.PRIORITY_LOW, msg);
              }
            }
          }

        }
        IProject refProject[] = project.getReferencedProjects ();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
        String depProjects[] = ProjectUtils.getProjectDependencyPaths (root, refProject);
        if (depProjects != null) {
          for (String depProject : depProjects) {
            if (depProject != null)
              tamPathStr += new File (depProject).toURI ().toString () + Constants.DATAPATH_SEPARATOR;
          }
        }
      }
      else {
        tamPathStr = new File (ProjectPreferencesUtil.getAbsolutePath (runConfig.getAogPath ())).toURI ().toString ()
          + Constants.DATAPATH_SEPARATOR;
      }

      // mapping filepaths to external tables and dictionaries. Shouldn't cause problems with 1.3.
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

      if (tokenizer == Constants.TOKENIZER_CHOICE_CUSTOM) {
    	  
      }
      else {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (
          Messages.getString ("SystemtRunJob.INFO_USING_WHITESPC_TKNZR")); //$NON-NLS-1$
      }

      // Wait for the auto build operation to complete before creating operator graph
      Job.getJobManager ().join (ResourcesPlugin.FAMILY_AUTO_BUILD, null);

      OperatorGraph og = OperatorGraph.createOG (selectedModules, tamPathStr, extEnt, tokenizerConfig);
      if (og.getOutputTypeNames ().isEmpty ()) { return createWarningStatus (Messages.getString ("SystemtRunJob.ERR_NO_OUTPUTVIEWS_IN_INPUT_COLL")); //$NON-NLS-1$
      }

      // Provenance
      final boolean enableProvenance = this.getIsProvenanceEnabled ();
      provenanceRunParams = new ProvenanceRunParams (selectedModules, tamPath, langCode, extEnt, tokenizerConfig,
        enableProvenance);
      provenanceRunParams.setExtViewNames (og.getExternalViewNames ());

      initializePaginationTracker (); // should always come after provenanceRunParams have been initialized.
      // PaginationTracker is a singleton that retains provenanceRunParams object,
      // and uses it while updating annotation explorer and result tables on changing pages.

      char csvDelimiterChar = runConfig.getCsvDelimiterChar ();

      DocReader docreader = null;
      if (isJsonFile) {
        docreader = new DocReader (FileUtils.createValidatedFile (filesToAnalyze), og.getDocumentSchema (),
          ProjectUtils.getExternalViewsSchema (og));
      }
      else {
        docreader = new DocReader (FileUtils.createValidatedFile (filesToAnalyze), og.getDocumentSchema (), null, csvDelimiterChar);  // TODO
      }
      docreader.overrideLanguage (langCode);

      if (monitor.isCanceled ()) { return renameResultFolderForCancel (
        Messages.getString ("SystemTLaunchConfigurationDelegate.SUBTASK_PREPROCESSING_VALIDATING_DATA"),
        "SystemRunJob.CANCEL_FOLDER_RENAME"); }
      monitor.worked (1);
      monitor.subTask (Messages.getString ("SystemTLaunchConfigurationDelegate.SUBTASK_PREPROCESSING_COMPUTING_SIZE"));
      int docsSize;
      docsSize = docreader.size ();
      /*
       * if(isJsonFile) { docsSize = 1; } else { docsSize = docreader.size(); }
       */
      if (monitor.isCanceled ()) { return renameResultFolderForCancel (
        Messages.getString ("SystemTLaunchConfigurationDelegate.SUBTASK_PREPROCESSING_COMPUTING_SIZE"),
        "SystemRunJob.CANCEL_FOLDER_RENAME"); }
      monitor.worked (1);
      monitor.done ();

      // The total work is taken as docsSize + 25% of docs size. This is because, we want to show some progress bars
      // to the user. Note that progress bars are to show some indication of activity to user and need not be entirely
      // accurate.
      int finishedWork = (int) (0.25 * docsSize);

      int totalWork = docsSize + finishedWork;
      monitor.beginTask (Messages.getString ("SystemTLaunchConfigurationDelegate.MAINTASK_RUNNING_SYSTEMT"), totalWork);
      monitor.worked (finishedWork); // pre-processing - step1 of 2 done

      boolean containsAnnotations;
      DocumentAnnotator docAnnotator = null;
      if (isJsonFile) {
        docAnnotator = new JsonDocAnnotator (project, runConfig, og, filesToAnalyze, resultFolder, tempDir, langCode, provenanceRunParams);
      }
      else if (isCsvFile) {
        docAnnotator = new CsvDocAnnotator (project, runConfig, og, filesToAnalyze, resultFolder, tempDir, langCode, provenanceRunParams);
      }
      else {
        docAnnotator = new TextDocAnnotator (project, runConfig, og, filesToAnalyze, resultFolder, tempDir, langCode, provenanceRunParams);
      }
      // annotateDocs method returns true if the annotator produced one or more output view rows
      // returns false otherwise.
      containsAnnotations = docAnnotator.annotateDocs (og, filesToIgnore, monitor);

      if (strfFilesThatExist.isEmpty () == false) {
        // we are not createWarningStatus here because then the method execution would halt.
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowWarning (
          Messages.getString ("SystemtRunJob.ERR_COLLISIONS_OCCURRED") + strfFilesThatExist.toString ()); //$NON-NLS-1$ //$NON-NLS-2$
      }

      if (!containsAnnotations) { return createWarningStatus (Messages.getString ("SystemtRunJob.ERR_NO_ANNOTS_IN_INPUT_COLL")
        + filesToAnalyze + "'"); }

      if (monitor.isCanceled ()) { return renameResultFolderForCancel (
        Messages.getString ("SystemTLaunchConfigurationDelegate.MAINTASK_RUNNING_SYSTEMT"),
        "SystemRunJob.CANCEL_FOLDER_RENAME"); }

      IStatus validateEndResultsStatus = new Status (IStatus.OK, Activator.PLUGIN_ID, "");
      if (validateEndResultsStatus.getSeverity () != IStatus.OK) { return validateEndResultsStatus; }
      monitor.subTask (Messages.getString ("SystemTLaunchConfigurationDelegate.DISPLAY")); //$NON-NLS-1$
      if (docAnnotator.firstPageDisplayed == false) {
        // In case the first page has not been displayed during execution because the number of serialized files is less
        // than the page size, now display the page.
        AnnotationExplorerUtil.displayFirstPage (project, resultFolder, tempDir, provenanceRunParams);
      }
      hideProvenanceViews ();
      AQLTreeViewUtility.clearSelectedItems ();
      // System.out.println(" Free Memory is " + Runtime.getRuntime().freeMemory());
    }
    catch (UnrecognizedFileFormatException ioe) {
      String formattedMsg = MessageUtil.formatMessage (Messages.getString ("SystemRunJob.UNRECOGNIZED_FILE_FORMAT"),
        ioFileToAnalyze.getName ());
      return createErrorStatus (formattedMsg);
    }
    catch (TextAnalyticsException e) { // TextAnalyticsException thrown by DocReader constructors and factory methods.
      Throwable cause = e.getCause ();
      if (cause == null) { // cause can be null for some TextAnalyticsExceptions
        cause = e;
      }
      return createErrorStatus (
        Messages.getString ("SystemtRunJob.ERR_RUNNING_TXT_ANALYSIS_PROJECT") + this.project.getName (), cause); //$NON-NLS-1$

    }
    catch (final Exception e) {
      e.printStackTrace ();
      try {
        renameResultFolderForCancel (
          Messages.getString ("SystemTLaunchConfigurationDelegate.MAINTASK_RUNNING_SYSTEMT"),
          "SystemRunJob.FAILED_EXECUTION_FOLDER_RENAME");
      }
      catch (CoreException e1) {
        e1.printStackTrace ();
      }

      IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
      String workspacePath = root.getLocation ().makeAbsolute ().toOSString ();
      String filePath = workspacePath + File.separator + ".metadata" + File.separator + ".log"; //$NON-NLS-1$ //$NON-NLS-2$

      Exception exp = new Exception (Messages.getString ("SystemRunJob.RUNTIME_ERROR")//$NON-NLS-1$
        + FileUtils.createValidatedFile (filesToAnalyze).getName () + ". \n\n" + "" //$NON-NLS-1$ 
        + Messages.getString ("SystemRunJob.RUNTIME_ERROR_REFER_LOGS", new Object[] { filePath }) //$NON-NLS-1$
        + " \n", e); //$NON-NLS-1$

      return createErrorStatus (
        Messages.getString ("SystemtRunJob.ERR_RUNNING_TXT_ANALYSIS_PROJECT") + this.project.getName (), exp); //$NON-NLS-1$
    }
    finally {
      monitor.done ();
    }
    return Status.OK_STATUS;
  }

  private IStatus verifyNonModularBuildErrors ()
  {
    if (ProjectUtils.hasBuildErrors (project)) { return createErrorStatus (Messages.getString ("General.ERR_PROJECT_HAS_BUILD_ERRORS"));//$NON-NLS-1$
    }
    return null;
  }

  private IStatus verifyModularBuildErrors ()
  {
    Set<String> errorModSet = ProjectUtils.getModulesWithError (project);
    String mod[] = ProjectUtils.getAllModules (project);
    if (!errorModSet.isEmpty () && errorModSet.size () == mod.length) { return createErrorStatus (Messages.getString ("General.ERR_PROJECT_HAS_BUILD_ERRORS"));//$NON-NLS-1$
    }
    if (!errorModSet.isEmpty () && errorModSet.size () > 0) {
      Set<String> tempModSet = new HashSet<String> ();
      for (String tempMod : selectedModules) {
        if (errorModSet.contains (tempMod)) continue;
        tempModSet.add (tempMod);
      }

      selectedModules = tempModSet.toArray (new String[0]);

      if (selectedModules == null || selectedModules.length == 0)
        return createErrorStatus (Messages.getString ("General.ERR_PROJECT_HAS_BUILD_ERRORS"));//$NON-NLS-1$

      String errorMsg = String.format (Messages.getString ("SystemtRunJob.PARTIAL_RUNNING_OF_SELECTED_MODULES"), //$NON-NLS-1$
        errorModSet.toString (), tempModSet.toString ());
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowWarning (errorMsg); //$NON-NLS-1$

    }
    return null;
  }

  private void hidePrevConcordanceView ()
  {
    Display.getDefault ().asyncExec (new Runnable () {

      @Override
      public void run ()
      {
        final IWorkbenchWindow window = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ();
        final ConcordanceView view = (ConcordanceView) window.getActivePage ().findView (
          "com.ibm.biginsights.textanalytics.concordance.view");
        final IWorkbenchPage wbPage = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
        wbPage.hideView (view);

        // view.dispose();
        IViewReference[] views = wbPage.getViewReferences ();

        IWorkbenchPart part = null;
        AQLResultTreeView aqlTreeView = null;
        for (IViewReference treeView : views) {
          part = treeView.getPart (true);
          if (part instanceof AQLResultTreeView) {
            aqlTreeView = (AQLResultTreeView) part;
            String secondaryID = aqlTreeView.getTitle ();
            secondaryID = secondaryID.replaceAll (":", "");
            IEditorInput ieInput = AQLResultTreeView.getEditorForId (secondaryID);
            if (ieInput != null) {
              IEditorPart ePart = wbPage.findEditor (ieInput);
              if (ePart != null) {
                wbPage.closeEditor (ePart, false);
              }
            }

            wbPage.hideView (treeView);
          }
          treeView = null;
          part = null;
        }
      }
    });
  }

  /**
   * Hide all previously open provenance views.
   */
  private void hideProvenanceViews ()
  {
    Display.getDefault ().asyncExec (new Runnable () {

      @Override
      public void run ()
      {
        final IWorkbenchPage wbPage = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();

        IViewReference[] viewRefs = wbPage.getViewReferences ();
        for (int i = 0; i < viewRefs.length; i++) {
          IViewReference viewRef = viewRefs[i];
          if ("com.ibm.biginsights.textanalytics.provenance.view".equals (viewRef.getId ())) wbPage.hideView (viewRef);
        }
      }
    });
  }

  @SuppressWarnings("unused")
  private List<IFile> getResultFiles () throws CoreException
  {
    final IResource[] resources = resultFolder.members ();
    final List<IFile> resFileList = new ArrayList<IFile> (resources.length);
    for (final IResource resource : resources) {
      if ((resource.getType () == IResource.FILE) && resource.getName ().endsWith (".strf")) { //$NON-NLS-1$
        resFileList.add ((IFile) resource);
      }
    }
    return resFileList;
  }

  private IStatus validateDelFiles ()
  {
    try {
      DBDumpFileScan dbDump = (DBDumpFileScan) DBDumpFileScan.makeFileScan (ioFileToAnalyze);
      if (!dbDump.getHaveLabelCol ()) { return createErrorStatus (Messages.getString ("SystemtRunJob.InvalidDelFormat")
        + ioFileToAnalyze);
      // LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError
      // (ioFileToAnalyze+Messages.getString("SystemtRunJob.InvalidDelFormat"));
      }
    }
    catch (Exception e) {
      e.printStackTrace ();
    }
    return new Status (IStatus.OK, Activator.PLUGIN_ID, "");
  }

  private void checkIfInputCollContainsInvalidFileFormats ()
  {
    String[] invalidFiles = ioFileToAnalyze.list (new FilenameFilter () {
      @Override
      public boolean accept (File dir, String name)
      {
        if (name.endsWith (".json") || name.endsWith (".zip") || name.endsWith ("tar.gz") || name.endsWith (".tar") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          || name.endsWith (".tgz") || name.endsWith (".del") || name.endsWith (".csv")) return true; //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
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

  /**
   * path of the bin directory as specified in runConfig.
   * 
   * @return Absolute path of bin directory for modular projects, the aog path as specified in properties for
   *         non-modular projects (without any modification).
   */
  private String getBinPath ()
  {
    if (isModularProject) {
      IFolder binDir = ProjectUtils.getTextAnalyticsBinFolder (runConfig.getProjectName ());
      if (binDir != null) {
        return binDir.getLocation ().toString ();
      }
      else {
        return null;
      }
    }
    else {
      return runConfig.getAogPath ();
    }
  }

  private List<String> getFilesToIgnore ()
  {
    return this.runConfig.getFilesToIgnore ();
  }

  private String getLanguageName ()
  {
    return this.runConfig.getLang ();
  }

  private boolean getIsProvenanceEnabled ()
  {
    return this.runConfig.getEnableProvenance ();
  }

  private String getResultDirPath ()
  {
    String defaultFolderName = rootResultFolder.getFullPath ().toOSString ();
    return defaultFolderName + "/result-" + currentTimeStamp; // Prefix
    // string
    // "result"
    // for every
    // result
    // directory

  }

  private String getAnalyzeInput ()
  {
    return this.runConfig.getInputCollection ();
  }

  private boolean isValidPath (String dataPath)
  {
    if (StringUtils.isEmpty (dataPath)) { return false; }
    final String[] entries = dataPath.split (Constants.DATAPATH_SEPARATOR);
    for (final String entry : entries) {
      if (!FileUtils.createValidatedFile (entry).exists ()) { return false; }
    }
    return true;
  }

  private static final IStatus createErrorStatus (String message, Throwable e)
  {
    // Passing false in prepareDetailedMessage() as underlying error messages have to be displayed in multiple lines
    return new Status (IStatus.ERROR, Activator.PLUGIN_ID,
      ProjectUtils.prepareDetailedMessage (e, false).getMessage (), e);
  }

  private static final IStatus createErrorStatus (String message)
  {
    return new Status (IStatus.ERROR, Activator.PLUGIN_ID, message);
  }

  private static final IStatus createWarningStatus (String message)
  {
    LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowWarning (message);
    return new Status (IStatus.WARNING, Activator.PLUGIN_ID, message);
  }

  /**
   * This is a private method to rename the result folder in the case of cancel - This it for the convenience of the
   * user
   * 
   * @throws CoreException
   */
  private IStatus renameResultFolderForCancel (String logMessage, String suffixRename) throws CoreException
  {
    if (resultFolder.members ().length == 0) {
      resultFolder.delete (true, new NullProgressMonitor ());
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (
        Messages.getString ("SystemtRunJob.INFO_USER_CANCELED_NO_STORE") + logMessage);
    }
    else {
      String name = resultFolder.getName ();
      CharSequence seq1 = name;
      String modName = (name + (Messages.getString (suffixRename)).trim ()); //$NON-NLS-1$
      CharSequence seq2 = modName;
      String fullpath = resultFolder.getFullPath ().toString ();
      fullpath = fullpath.replace (seq1, seq2);
      resultFolder.move (new Path (fullpath), true, new NullProgressMonitor ());
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (
        Messages.getString ("SystemtRunJob.INFO_USER_CANCELED_PARTIAL_STORE") + logMessage);
    }
    // Hiding the previous result so that it is not confusing to the user
    // hidePrevConcordanceView();
    return Status.CANCEL_STATUS;
  }

  /**
   * The method is called to initialize the pagination tracker
   */
  private void initializePaginationTracker ()
  {
    PaginationTracker tracker = PaginationTracker.getInstance ();
    tracker.setCurrentPage (1);
    tracker.setProvParams (provenanceRunParams);
    // Moving pagination preferences to project level
    if (this.runConfig.isPaginationEnabled ())
      tracker.setFilesPerPageCount (this.runConfig.getNumFilesPerPage ());
    else
      tracker.setFilesPerPageCount (0);
  }

}
