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
package com.ibm.biginsights.textanalytics.resourcechange.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

import com.ibm.biginsights.project.MigrateProject;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.resourcechange.Messages;
import com.ibm.biginsights.textanalytics.resourcechange.ResourceChangePlugin;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ActionPlanModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.CollectionModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.LabelModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.Serializer;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;

public class AQLResourceChangeActionDelegate extends AbstractResourceChangeActionDelegate
{



  public AQLResourceChangeActionDelegate (IResource removedResource, IResource addedResource)
  {
    super (removedResource, addedResource);
  }

  public void run ()
  {

    super.run ();

    boolean valid = validateResourceChangeEvent ();
    if (!valid) { return; }

    switch (resourceChangeType) {
      case Constants.RESCHNG_DELETE:
        handleDeleteAction ();
      break;
      case Constants.RESCHNG_RENAME:
        handleRenameAction ();
      break;
      case Constants.RESCHNG_ADD:
        handleAddAction ();
      break;
    }
  }

  /**
   * The following events are supported: 1) ADD event on projects (caused during import of a project and Copy-paste of
   * project) 2) RENAME event on projects 3) RENAME event on folders 4) RENAME event on main AQL
   */
  @Override
  protected boolean validateResourceChangeEvent ()
  {

    if (super.validateResourceChangeEvent () == false) { return false; }

    // The legacy resource change listeners are supported only for non-modular projects. Modular projects are handled by
    // Refactoring framework
    // BEGIN: SPECIAL CASE
    if (addedResource != null) { return (false == ProjectUtils.isModularProject (addedResource.getProject ())); }
    if (removedResource != null) { return (false == ProjectUtils.isModularProject (removedResource.getProject ())); }
    // END: SPECIAL CASE

    if (resourceChangeType == Constants.RESCHNG_ADD) {
      // we support ADD events for only projects
      if (addedResource != null && addedResource.getType () == IResource.PROJECT) {
        return true;
      }
      else {
        return false;
      }
    }

    // Validation for RENAME event
    switch (removedResource.getType ()) {
      case IResource.FILE:
        // the extrction plan needs to handle any file that gets renamed
        // since it may be part of any user data
        // collection
        if (resourceChangeType != Constants.RESCHNG_DELETE) break;

        // Validation 3.1: If event is not related to AQL files, return
        if (!eventRelatedToAQLFiles ()) { return false; }

        // Validation 3.2: If event is not related to mainAQL file, return.
        // This is a bit costlier validation, and hence
        // the initial check in 3.1
        if (!isMainAQLFile (removedResource)) { return false; }
      break;
      case IResource.FOLDER:
        // Validation 3.3: If event is related to aog path or result
        // directory, we are not interested,
        // because Text Analytics tooling will always write aog files to
        // $PROJECT_HOME/.aog and results to
        // $PROJECT_HOME/<result.rootDir>
        if (isAogPathOrResultDir (removedResource)) { return false; }
      case IResource.PROJECT:
        // No validation. WorkspaceResourceChangeListener has already
        // checked if it is a text analytics project or not
      break;
    }

    return true;
  }

  @Override
  protected void handleRenameAction ()
  {

    switch (removedResource.getType ()) {

      case IResource.FILE:
        updateFileExtractionPlan ();
        // Validation 3.1: If event is not related to AQL files, return
        if (!eventRelatedToAQLFiles ()) { return; }

        // Validation 3.2: If event is not related to mainAQL file, return.
        // This is a bit costlier validation, and hence
        // the initial check in 3.1
        if (!isMainAQLFile (removedResource)) { return; }

        String[] affectedProjects1 = modifyTextAnalyticsFile (IResource.FILE, false);
        String[] affectedLaunchConfigs1 = modifyLaunchConfigFiles (IResource.FILE, false);

        displayMessage (affectedProjects1, affectedLaunchConfigs1, true);
      break;

      case IResource.FOLDER:
        updateDirectoryExtractionPlan ();
        String[] affectedProjects2 = modifyTextAnalyticsFile (IResource.FOLDER, false);
        String[] affectedLaunchConfigs2 = modifyLaunchConfigFiles (IResource.FOLDER, false);

        displayMessage (affectedProjects2, affectedLaunchConfigs2, true);
      break;

      case IResource.PROJECT:
        updateDirectoryExtractionPlan ();

        String[] affectedProjects3 = modifyTextAnalyticsFile (IResource.PROJECT, false);
        String[] affectedLaunchConfigs3 = modifyLaunchConfigFiles (IResource.PROJECT, false);

        displayMessage (affectedProjects3, affectedLaunchConfigs3, true);
      break;
    }

  }

  @Override
  protected void handleDeleteAction ()
  {
    // DELETE action will not result in modification of .textanalytics and launch config files
    // We leave it up to the users to do the clean up, since that's the way JDT handles deletion of resources

    switch (removedResource.getType ()) {
      case IResource.PROJECT:
        adjustExtractionPlanOnDelete ();
      break;
    }
  }

  /**
   * this function will make sure that if the project deleted is the same that the one we have open in the extraction
   * plan, then we need to clear the extraction plan
   */
  private void adjustExtractionPlanOnDelete ()
  {
    Display.getDefault ().syncExec (new Runnable () {
      @Override
      public void run ()
      {
        AqlProjectUtils.resetExtractionPlanForProject (removedResource.getProject ().getName ());
      }
    });
  }

  private void reloadExtractionPlanForProject ()
  {
    WorkbenchJob job = new WorkbenchJob ("Refreshing Extraction Plan") {
      @Override
      public IStatus runInUIThread (IProgressMonitor monitor)
      {
        AqlProjectUtils.reloadExtractionPlanForProject (null);
        return Status.OK_STATUS;
      }
    };

    job.schedule ();
  }

  private void displayMessage (String[] projects, String[] launchConfigs, boolean popupMessage)
  {
    String strAffectedProjects = ""; //$NON-NLS-1$
    if (projects != null) {
      for (String project : projects) {
        if (project != null) {
          strAffectedProjects += project + "\n"; //$NON-NLS-1$
        }
      }
    }

    String strAffectedLaunchConfigs = ""; //$NON-NLS-1$
    if (launchConfigs != null) {
      for (String launchConfig : launchConfigs) {
        if (launchConfig != null) {
          strAffectedLaunchConfigs += launchConfig + "\n"; //$NON-NLS-1$
        }
      }
    }

    String message = ""; //$NON-NLS-1$
    if (strAffectedProjects.trim ().length () > 0) {
      message += MessageUtil.formatMessage (Messages.AQLResourceChangeActionDelegate_PROPERTIES_OF_PROJECTS_UPDATED,
        strAffectedProjects) + "\n"; //$NON-NLS-2$
    }

    if (strAffectedLaunchConfigs.trim ().length () > 0) {
      message += MessageUtil.formatMessage (Messages.AQLResourceChangeActionDelegate_ATTRIBS_OF_LAUNCH_CONFIGS_UPDATED,
        strAffectedLaunchConfigs) + "\n"; //$NON-NLS-1$
    }

    if (message.trim ().length () > 0) {
      message += Messages.AQLResourceChangeActionDelegate_VISIT_RESOURCES_TO_ENSURE_CORRECTNESS;
      if (popupMessage) {
        LogUtil.getLogForPlugin (ResourceChangePlugin.PLUGIN_ID).logAndShowInfo (message);
      }
      else {
        LogUtil.getLogForPlugin (ResourceChangePlugin.PLUGIN_ID).logInfo (message);
      }
    }
  }

  private String[] modifyLaunchConfigFiles (int resourceType, boolean deleteAction)
  {
    switch (resourceType) {
      case IResource.FILE:
        String newMainAQLFile = ""; //$NON-NLS-1$
        if (!deleteAction) {
          // it's then a rename action. So, derive the name of new main
          // aql file
          newMainAQLFile = Constants.WORKSPACE_RESOURCE_PREFIX + addedResource.getFullPath ().toString ();
        }
        return handleModifyLaunchConfig (IResource.FILE, new String[] { IRunConfigConstants.MAIN_AQL }, newMainAQLFile);
      case IResource.FOLDER:
        String newFolder = ""; //$NON-NLS-1$
        if (!deleteAction) {
          // it's then a rename action. So, derive the name of new folder
          newFolder = Constants.WORKSPACE_RESOURCE_PREFIX + addedResource.getFullPath ().toString ();
        }

        String[] list1 = handleModifyLaunchConfig (IResource.FOLDER, new String[] { IRunConfigConstants.MAIN_AQL,
          IRunConfigConstants.SEARCH_PATH, IRunConfigConstants.INPUT_COLLECTION }, newFolder);
        String[] list2 = handleModifyProfileConfig (IResource.FOLDER,
          new String[] { IRunConfigConstants.INPUT_COLLECTION }, newFolder);
        return combine (list1, list2);
      case IResource.PROJECT:
        String newProject = ""; //$NON-NLS-1$
        if (deleteAction) {
          deleteLaunchConfigsForProject (removedResource.getName ());
        }
        else {
          // it's then a rename action. So, derive the name of new project
          newProject = Constants.WORKSPACE_RESOURCE_PREFIX + addedResource.getFullPath ().toString ();
        }

        String[] list3 = handleModifyLaunchConfig (IResource.PROJECT, new String[] { IRunConfigConstants.AOG_PATH,
          IRunConfigConstants.RESULT_DIR, IRunConfigConstants.MAIN_AQL, IRunConfigConstants.SEARCH_PATH,
          IRunConfigConstants.INPUT_COLLECTION }, newProject);
        String[] list4 = handleModifyProfileConfig (IResource.PROJECT,
          new String[] { IRunConfigConstants.INPUT_COLLECTION }, newProject);

        return combine (list3, list4);
    }
    return null;
  }

  private String[] combine (String[] list1, String[] list2)
  {
    HashSet<String> combinedList = new HashSet<String> ();
    if (list1 != null) {
      for (String item : list1) {
        combinedList.add (item);
      }
    }

    if (list2 != null) {
      for (String item : list2) {
        combinedList.add (item);
      }
    }

    return combinedList.toArray (new String[combinedList.size ()]);
  }

  private String[] modifyTextAnalyticsFile (int refactorMode, boolean deleteAction)
  {
    switch (refactorMode) {
      case IResource.FILE:
        String newMainAQLFile = ""; //$NON-NLS-1$
        if (!deleteAction) {
          newMainAQLFile = Constants.WORKSPACE_RESOURCE_PREFIX + addedResource.getFullPath ().toString ();
        }
        return handleModifyTextAnalyticsFile (new String[] { Constants.GENERAL_MAINAQLFILE }, newMainAQLFile);
      case IResource.FOLDER:
        String newFolder = ""; //$NON-NLS-1$
        if (!deleteAction) {
          newFolder = Constants.WORKSPACE_RESOURCE_PREFIX + addedResource.getFullPath ().toString ();
        }
        return handleModifyTextAnalyticsFile (new String[] { Constants.GENERAL_MAINAQLFILE,
          Constants.SEARCHPATH_DATAPATH }, newFolder);
      case IResource.PROJECT:
        if (deleteAction) {
          // Do nothing, becuase .textanalytics file also gets deleted
          // along with the project
          return null;
        }
        String newProject = Constants.WORKSPACE_RESOURCE_PREFIX + addedResource.getFullPath ().toString ();
        return handleModifyTextAnalyticsFile (new String[] { Constants.GENERAL_MAINAQLFILE,
          Constants.SEARCHPATH_DATAPATH }, newProject);
    }

    return null;
  }

  /**
	 * 
	 */
  private void addNewProject ()
  {
    IProject project = addedResource.getProject ();

    // TODO for next release: move general code for importing projects into BI project plugin.
    if (MigrateProject.isMigrationRequired (project).isMigrationRequired) {
      /**
       * This portion of code gets invoked when the user imports a project from a previous BI version into a workspace
       * with the current version of BigInsights eclipse tooling. Ideally, migration action should be taken here.
       * However, attempting to migrate during copy action would result in a CoreException since the resource tree is
       * locked for modifications. So, we would just display a message asking the user to invoke migration explicitly,
       * once the copy or import action is complete.
       */
      Display.getDefault ().syncExec (new Runnable () {
        @Override
        public void run ()
        {
          Shell shell = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getShell ();
          CustomMessageBox msgBox = CustomMessageBox.createInfoMessageBox (shell,
            Messages.AQLResourceChangeActionDelegate_WARNING, Messages.AQLResourceChangeActionDelegate_MANUALLY_MIGRATE);
          msgBox.open ();
        }
      });
      return;
    }
  }

  private void deleteLaunchConfigsForProject (String projectName)
  {

    // Step1: Get All RunConfigs and delete
    ILaunchConfigurationWorkingCopy runConfigs[] = getAllRunConfigs ();
    for (ILaunchConfigurationWorkingCopy runConfig : runConfigs) {
      try {
        if (projectName.equals (runConfig.getAttribute (IRunConfigConstants.PROJECT_NAME, ""))) { //$NON-NLS-1$
          runConfig.getOriginal ().delete ();
        }
      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
    }

    // Step2: Get All ProfileConfigs and delete
    ILaunchConfigurationWorkingCopy profileConfigs[] = getAllProfileConfigs ();
    for (ILaunchConfigurationWorkingCopy profileConfig : profileConfigs) {
      try {
        if (projectName.equals (profileConfig.getAttribute (IRunConfigConstants.PROJECT_NAME, ""))) { //$NON-NLS-1$
          profileConfig.getOriginal ().delete ();
        }
      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
    }

  }

  private String[] handleModifyTextAnalyticsFile (String[] attributeNames, String newValue)
  {
    ProjectPreferenceStore[] prefStores = getAllPreferenceStores ();

    if (prefStores.length > 0) {
      String oldValue = Constants.WORKSPACE_RESOURCE_PREFIX + removedResource.getFullPath ().toString ();
      return findReplace (prefStores, attributeNames, oldValue, newValue);
      // buildProject(removedResource.getProject());
    }
    return null;
  }

  private String[] handleModifyLaunchConfig (int resourceType, String attributeNames[], String newValue)
  {
    ILaunchConfigurationWorkingCopy[] launchConfigs = getAllRunConfigs ();

    String oldValue = Constants.WORKSPACE_RESOURCE_PREFIX + removedResource.getFullPath ().toString ();
    return findReplace (resourceType, launchConfigs, attributeNames, oldValue, newValue);
  }

  private String[] handleModifyProfileConfig (int resourceType, String attributeNames[], String newValue)
  {
    ILaunchConfigurationWorkingCopy[] profileConfigs = getAllProfileConfigs ();

    String oldValue = Constants.WORKSPACE_RESOURCE_PREFIX + removedResource.getFullPath ().toString ();
    return findReplace (resourceType, profileConfigs, attributeNames, oldValue, newValue);
  }

  // /////////////////////////// UTILITY METHODS

  private String[] findReplace (int resourceType, ILaunchConfigurationWorkingCopy[] launchConfigs,
    String[] attributeNames, String oldValue, String newValue)
  {
    ArrayList<String> modifiedLaunchFiles = new ArrayList<String> ();

    for (ILaunchConfigurationWorkingCopy launchConfig : launchConfigs) {
      for (String attributeName : attributeNames) {
        try {
          handleFindReplace (launchConfig, attributeName, oldValue, newValue);
        }
        catch (CoreException e) {
          // TODO Auto-generated catch block
          e.printStackTrace ();
        }
      }// end: while attribs.hasNext()

      // PROJET_NAME need to be handled separately, since the newValue
      // contains a '/' prefix.
      if (resourceType == IResource.PROJECT) {
        try {
          String storedProjectName = launchConfig.getAttribute (IRunConfigConstants.PROJECT_NAME, ""); //$NON-NLS-1$
          String projectName = (oldValue.length () == 0) ? "" : oldValue.substring (4); //$NON-NLS-1$
          String newProjectName = (newValue.length () == 0) ? "" : newValue.substring (4); //$NON-NLS-1$
          if (storedProjectName.equals (projectName)) {
            launchConfig.setAttribute (IRunConfigConstants.PROJECT_NAME, newProjectName);
          }
        }
        catch (CoreException e) {
          // TODO Auto-generated catch block
          e.printStackTrace ();
        }
      }

      try {
        if (launchConfig.isDirty ()) {
          modifiedLaunchFiles.add (launchConfig.getName ());
          launchConfig.doSave ();
        }
      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
    }// end: for each launch config
    Collections.sort (modifiedLaunchFiles);
    return new HashSet<String> (modifiedLaunchFiles).toArray (new String[modifiedLaunchFiles.size ()]);
  }

  private String[] findReplace (ProjectPreferenceStore[] projPrefStores, String[] attributeNames, String oldValue,
    String newValue)
  {
    ArrayList<String> modifiedProjects = new ArrayList<String> ();

    for (ProjectPreferenceStore projPrefStore : projPrefStores) {
      PreferenceStore prefStore = projPrefStore.prefStore;
      if (prefStore != null) {
        for (String attributeName : attributeNames) {
          handleFindReplace (prefStore, attributeName, oldValue, newValue);
        }// end: foreach attributeNames

        try {
          if (prefStore.needsSaving ()) {
            modifiedProjects.add (projPrefStore.projectName);
            prefStore.save ();
          }
        }
        catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace ();
        }
      }

    }// end: for each prefStore
    Collections.sort (modifiedProjects);
    return new HashSet<String> (modifiedProjects).toArray (new String[modifiedProjects.size ()]);
  }

  private void handleFindReplace (PreferenceStore prefStore, String attributeName, String oldValue, String newValue)
  {
    if (prefStore == null || !prefStore.contains (attributeName)) { return; }
    String storedValue = prefStore.getString (attributeName);
    if (StringUtils.isEmpty (storedValue)) { return; }

    IPath oldPath = new Path (ProjectPreferencesUtil.getPath (oldValue));
    String replaceValue = null;

    if (Constants.SEARCHPATH_DATAPATH.equals (attributeName)) {
      String[] strStoredPaths = storedValue.split (Constants.DATAPATH_SEPARATOR);
      String str = ""; //$NON-NLS-1$

      for (int i = 0; i < strStoredPaths.length; i++) {
        String replacedPath = replacePath (strStoredPaths[i], oldPath, newValue);
        if (!StringUtils.isEmpty (replacedPath)) {
          str += replacedPath;
          if ((i + 1) < strStoredPaths.length) {
            str += Constants.DATAPATH_SEPARATOR;
          }
        }
      }
      if (str.trim ().length () > 0) {
        replaceValue = str;
      }

    }
    else {
      // It is one of the below:
      // Constants.GENERAL_AOGPATH, Constants.GENERAL_MAINAQLFILE,
      // Constants.GENERAL_RESULTDIR
      // So, just replace without worrying about DATAPATH_SEPARATOR
      replaceValue = replacePath (storedValue, oldPath, newValue);
    }
    if (!storedValue.equals (replaceValue)) {
      prefStore.setValue (attributeName, replaceValue);
    }
  }

  private void handleFindReplace (ILaunchConfigurationWorkingCopy launchConfig, String attributeName, String oldValue,
    String newValue) throws CoreException
  {
    String storedValue = launchConfig.getAttribute (attributeName, ""); //$NON-NLS-1$
    if (StringUtils.isEmpty (storedValue)) { return; }

    IPath oldPath = new Path (ProjectPreferencesUtil.getPath (oldValue));
    String replaceValue = null;

    if (IRunConfigConstants.SEARCH_PATH.equals (attributeName)) {
      String[] strStoredPaths = storedValue.split (Constants.DATAPATH_SEPARATOR);
      String str = ""; //$NON-NLS-1$

      for (int i = 0; i < strStoredPaths.length; i++) {
        String replacedPath = replacePath (strStoredPaths[i], oldPath, newValue);
        if (!StringUtils.isEmpty (replacedPath)) {
          str += replacedPath;
          if ((i + 1) < strStoredPaths.length) {
            str += Constants.DATAPATH_SEPARATOR;
          }
        }
      }
      if (str.trim ().length () > 0) {
        replaceValue = str;
      }
    }
    else {
      // It is one of the below:
      // IRunConfigConstants.AOG_PATH, IRunConfigConstants.MAIN_AQL,
      // IRunConfigConstants.RESULT_DIR,
      // IRunConfigConstants.INPUT_COLLECTION,
      // So, just replace without worrying about DATAPATH_SEPARATOR
      replaceValue = replacePath (storedValue, oldPath, newValue);
    }
    if (!storedValue.equals (replaceValue)) {
      launchConfig.setAttribute (attributeName, replaceValue);
    }
  }

  private String replacePath (String strStoredPath, IPath findPath, String replacePath)
  {
    if (!ProjectPreferencesUtil.isWorkspaceResource (strStoredPath)) {
      // strStoredPath is an external file system path. So, just return it
      // as is.
      return strStoredPath;
    }

    IPath storedPath = new Path (ProjectPreferencesUtil.getPath (strStoredPath));
    if (findPath.isPrefixOf (storedPath)) {
      int segmentCount = findPath.segmentCount ();
      IPath newPath = new Path (ProjectPreferencesUtil.getPath (replacePath));

      IPath resourcePath = storedPath.removeFirstSegments (segmentCount);
      String replaceValue = newPath.append (resourcePath).toString ();
      IResource iResource = ResourcesPlugin.getWorkspace ().getRoot ().findMember (new Path (replaceValue));

      // check if replaced path is empty or not a valid workspace resource
      if (StringUtils.isEmpty (replaceValue) || iResource == null) {
        return ""; //$NON-NLS-1$
      }
      else {
        return Constants.WORKSPACE_RESOURCE_PREFIX + replaceValue;
      }

    }
    else {
      // strStoredPath does not contain findPath. So, return strStoredPath
      // as is.
      return strStoredPath;
    }
  }

  private ILaunchConfigurationWorkingCopy[] getAllRunConfigs ()
  {
    ILaunchManager launchManager = DebugPlugin.getDefault ().getLaunchManager ();
    ILaunchConfigurationType runType = launchManager.getLaunchConfigurationType ("com.ibm.biginsights.textanalytics.nature.systemTApplication"); //$NON-NLS-1$
    ILaunchConfigurationWorkingCopy[] workingCopies = null;
    try {
      ILaunchConfiguration[] runConfigs = launchManager.getLaunchConfigurations (runType);
      workingCopies = new ILaunchConfigurationWorkingCopy[runConfigs.length];
      for (int i = 0; i < runConfigs.length; i++) {
        workingCopies[i] = runConfigs[i].getWorkingCopy ();
      }
    }
    catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    return workingCopies;
  }

  /**
   * gets the array of projects that contain a .extractionplan file
   * 
   * @return
   */
  private IProject[] getProjectsWithExtractionPlan ()
  {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace ().getRoot ();
    IProject projects[] = workspaceRoot.getProjects ();

    ArrayList<IProject> projs = new ArrayList<IProject> ();
    for (IProject project : projects) {
      try {
        if (project.isOpen () && project.hasNature (Activator.NATURE_ID)
          && project.getFile (Constants.EXTRACTION_PLAN_FILE_NAME).exists ()) {
          projs.add (project);
        }
      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
    }

    return projs.toArray (new IProject[projs.size ()]);
  }

  private boolean isAogPathOrResultDir (IResource resource)
  {
    if (resource == null) { return false; }

    if (!(resource instanceof IFolder)) { return false; }
    return Constants.DEFAULT_AOG_DIR.equals (resource.getName ()) || ProjectUtils.isResultRootDir ((IFolder) resource);
  }

  private boolean isMainAQLFile (IResource resource)
  {
    if (!(resource instanceof IFile)) { return false; }

    IProject[] referringProjects = getProjectsWithMainAQL (resource);
    return (referringProjects != null && referringProjects.length > 0);
  }

  private boolean eventRelatedToAQLFiles ()
  {
    if (removedResource != null && "aql".equals (removedResource.getFileExtension ())) { return true; } //$NON-NLS-1$

    if (addedResource != null && "aql".equals (addedResource.getFileExtension ())) { return true; } //$NON-NLS-1$

    return false;
  }

  /**
   * updates the references to an aql file paths that may need to update: Collection, Examples, Labels
   */
  private void updateFileExtractionPlan ()
  {
    IProject[] projects = getProjectsWithExtractionPlan ();

    for (IProject project : projects) {
      ActionPlanModel model = loadExtractionPlan (project);

      String newValue = Constants.WORKSPACE_RESOURCE_PREFIX + addedResource.getFullPath ().toString ();
      String oldValue = Constants.WORKSPACE_RESOURCE_PREFIX + removedResource.getFullPath ().toString ();

      // update the collection
      CollectionModel collection = model.getCollection ();
      if (collection.getPath ().equals (oldValue)) collection.setPath (newValue);

      // update the labels and their examples
      for (LabelModel label : model.getRoots ()) {
        label = updateLabelPaths (label, oldValue, newValue);
      }

      serializeExtractionPlan (model, project);
    }
  }

  /**
   * updates the references to a folder paths that may need to update: Collection, Examples, Labels
   */
  private void updateDirectoryExtractionPlan ()
  {
    IProject[] projects = getProjectsWithExtractionPlan ();

    for (IProject project : projects) {
      ActionPlanModel model = loadExtractionPlan (project);

      String newValue = Constants.WORKSPACE_RESOURCE_PREFIX + addedResource.getFullPath ().toString ();
      String oldValue = Constants.WORKSPACE_RESOURCE_PREFIX + removedResource.getFullPath ().toString ();

      if (project == addedResource.getProject ()) model.setName (project.getName ());

      // update the collection
      CollectionModel collection = model.getCollection ();
      IPath oldPath = new Path (ProjectPreferencesUtil.getPath (oldValue));
      collection.setPath (replacePath (collection.getPath (), oldPath, newValue));

      // update the labels and their examples
      for (LabelModel label : model.getRoots ()) {
        label = updateLabelPaths (label, oldPath, newValue);
      }

      serializeExtractionPlan (model, project);
    }
  }

  /**
   * recursively updates the paths in a given label model. this method is used to update a label when a folder or
   * project gets renamed
   * 
   * @param label
   * @param oldPath
   * @param newValue
   * @return
   */
  private LabelModel updateLabelPaths (LabelModel label, IPath oldPath, String newValue)
  {
    // sets the basic file
    label.setBasicfilepath (replacePath (label.getBasicfilepath (), oldPath, newValue));
    // sets the concepts file
    label.setConceptfilepath (replacePath (label.getConceptfilepath (), oldPath, newValue));
    // sets the refinements file
    label.setRefinementfilepath (replacePath (label.getRefinementfilepath (), oldPath, newValue));

    // update the paths and files of each example
    for (ExampleModel example : label.getExamples ()) {
      example.setFilePath (replacePath (example.getFilePath (), oldPath, newValue));
    }

    // recurse into the children labels
    for (LabelModel sublabel : label.getSubTags ()) {
      sublabel = updateLabelPaths (sublabel, oldPath, newValue);
    }
    // return the modified object
    return label;
  }

  /**
   * recursively updates the paths in a given label model. this method is used to update a label when a file gets
   * renamed
   * 
   * @param label
   * @param oldValue
   * @param newValue
   * @return
   */
  private LabelModel updateLabelPaths (LabelModel label, String oldValue, String newValue)
  {
    // sets the basic file
    if (label.getBasicfilepath ().equals (oldValue)) label.setBasicfilepath (newValue);
    // sets the concepts file
    if (label.getConceptfilepath ().equals (oldValue)) label.setConceptfilepath (newValue);
    // sets the refinements file
    if (label.getRefinementfilepath ().equals (oldValue)) label.setRefinementfilepath (newValue);

    // update the paths and files of each example
    for (ExampleModel example : label.getExamples ()) {

      IPath oldPath = new Path (ProjectPreferencesUtil.getPath (oldValue));
      IPath newPath = new Path (ProjectPreferencesUtil.getPath (newValue));

      String oldDir = Constants.WORKSPACE_RESOURCE_PREFIX + oldPath.removeLastSegments (1).toString ();
      String oldFile = "\\" + oldPath.lastSegment (); //$NON-NLS-1$

      // case we have a archive file
      if (example.getFilePath ().equals (oldValue)) {
        example.setFilePath (newValue);
      }
      else if (example.getFilePath ().equals (oldDir) && example.getFileLabel ().equals (oldFile)) {
        // this is the case when the collection is a directory and
        // therefore renaming a file inside this collection make
        // us rename the file
        example.setFileLabel (newPath.lastSegment ());
      }
    }

    // recurse into the children labels
    for (LabelModel sublabel : label.getSubTags ()) {
      sublabel = updateLabelPaths (sublabel, oldValue, newValue);
    }

    // return the modified object
    return label;
  }

  /**
   * loads the extraction plan for a given project
   * 
   * @param project
   * @return null if the project doesn't have an extraction plan
   */
  private ActionPlanModel loadExtractionPlan (IProject project)
  {
    try {
      if (project.isOpen () && !project.hasNature (Activator.NATURE_ID)) { return null; }

      IFile file = project.getFile (Constants.EXTRACTION_PLAN_FILE_NAME);
      if (file.exists ()) {
        try {
          Serializer serializer = new Serializer ();
          ActionPlanModel model = serializer.getModelForInputStream (file.getContents ());
          return model;
        }
        catch (CoreException e) {
          e.printStackTrace ();
        }
      }
    }
    catch (CoreException e1) {

    }
    return null;
  }

  private ILaunchConfigurationWorkingCopy[] getAllProfileConfigs ()
  {
    ILaunchManager launchManager = DebugPlugin.getDefault ().getLaunchManager ();
    ILaunchConfigurationType profileType = launchManager.getLaunchConfigurationType ("com.ibm.biginsights.textanalytics.profile.systemTApplication"); //$NON-NLS-1$
    ILaunchConfigurationWorkingCopy[] workingCopies = null;
    try {
      ILaunchConfiguration[] profileConfigs = launchManager.getLaunchConfigurations (profileType);
      workingCopies = new ILaunchConfigurationWorkingCopy[profileConfigs.length];
      for (int i = 0; i < profileConfigs.length; i++) {
        workingCopies[i] = profileConfigs[i].getWorkingCopy ();
      }
    }
    catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    return workingCopies;
  }

  private ProjectPreferenceStore[] getAllPreferenceStores ()
  {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace ().getRoot ();
    IProject projects[] = workspaceRoot.getProjects ();

    ArrayList<ProjectPreferenceStore> taPrefStores = new ArrayList<ProjectPreferenceStore> ();
    for (IProject project : projects) {
      try {
        if (project.isOpen () && project.hasNature (Activator.NATURE_ID)) {
          PreferenceStore prefStore = ProjectUtils.getPreferenceStore (project);
          taPrefStores.add (new ProjectPreferenceStore (project.getName (), prefStore));
        }
      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
    }

    return taPrefStores.toArray (new ProjectPreferenceStore[taPrefStores.size ()]);
  }

  private IProject[] getProjectsWithMainAQL (IResource removedMainAQL)
  {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace ().getRoot ();
    IProject projects[] = workspaceRoot.getProjects ();
    String strRemovedMainAQL = removedMainAQL.getFullPath ().toString ();

    ArrayList<IProject> taProjects = new ArrayList<IProject> ();
    for (IProject project : projects) {
      try {
        if (project.isOpen () && project.hasNature (Activator.NATURE_ID)) {
          PreferenceStore prefStore = ProjectUtils.getPreferenceStore (project);
          String mainAQL = prefStore.getString (Constants.GENERAL_MAINAQLFILE);
          if (mainAQL != null && mainAQL.contains (strRemovedMainAQL)) {
            taProjects.add (project);
          }
        }
      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
    }

    return taProjects.toArray (new IProject[taProjects.size ()]);
  }

  /**
   * stores the serialized extraction plan for the given project
   * 
   * @param model
   * @param project
   */
  private void serializeExtractionPlan (final ActionPlanModel model, final IProject project)
  {
    WorkspaceJob job = new WorkspaceJob ("refactoring") {
      @Override
      public IStatus runInWorkspace (IProgressMonitor monitor) throws CoreException
      {
        IFile file = project.getFile (Constants.EXTRACTION_PLAN_FILE_NAME);
        Serializer serializer = new Serializer ();
        serializer.writeModelToFile (file, model);

        // in order to keep the Extraction Plan View synchronized with the workspace we refresh it upon the change
        if (project.getName ().equals (AqlProjectUtils.getExtractionPlanProjectName ()))
          reloadExtractionPlanForProject ();

        return Status.OK_STATUS;
      }

    };

    job.schedule ();
  }

  // ///////////////////////// PRIVATE INNER CLASS
  // /////////////////////////////////////
  private class ProjectPreferenceStore
  {
    String projectName;
    PreferenceStore prefStore;

    ProjectPreferenceStore (String projectName, PreferenceStore prefStore)
    {
      super ();
      this.projectName = projectName;
      this.prefStore = prefStore;
    }
  }

  @Override
  protected void handleAddAction ()
  {
    if (removedResource == null) {
      if (addedResource.getType () != IResource.PROJECT) return;
      IProject project = addedResource.getProject ();
      try {
        // handle new projects
        if (project.isOpen () && project.hasNature (Activator.NATURE_ID)) {
          /**
           * we want to run this in a separate thread. the reason is that the workspace is actually copying all the
           * files from the new resource. Even though the project has been added, not all the files belonging to it are
           * ready to be used in the workspace. So, this new thread won't be able to start until the workspace is ready
           */
          WorkspaceJob addNewProjectJob = new WorkspaceJob ("Importing Project") {

            @Override
            public IStatus runInWorkspace (IProgressMonitor monitor) throws CoreException
            {
              addNewProject ();
              // this job cannot be canceled, so we always complete OK
              return Status.OK_STATUS;
            }
          };

          addNewProjectJob.schedule ();
        }
      }
      catch (CoreException e) {
        // if cannot get the nature just return since this is not the target kind of project
        // e.printStackTrace();
        return;
      }
      return;
    }
  }
}
