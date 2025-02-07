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

package com.ibm.biginsights.textanalytics.refactor.core;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import com.ibm.biginsights.textanalytics.refactor.util.RefactorUtils;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.icu.text.MessageFormat;

public class LaunchConfigurationProjectNameChange extends Change
{
	@SuppressWarnings("unused")

 
	ILaunchConfiguration launchConfiguration;
  String oldProjectName;
  String newProjectName;

  public LaunchConfigurationProjectNameChange (ILaunchConfiguration launchConfiguration, String oldProjectName, String newProjectName)
  {
    this.launchConfiguration = launchConfiguration;
    this.oldProjectName = oldProjectName;
    this.newProjectName = newProjectName;
  }

  @Override
  public String getName ()
  {
    return MessageFormat.format (AQLElementRenameCoreTexts.UpdateLaunchConfigurationProjectName,
      new Object[] { launchConfiguration.getName () });
  }

  @Override
  public void initializeValidationData (IProgressMonitor pm)
  {}

  @Override
  public RefactoringStatus isValid (IProgressMonitor pm) throws CoreException, OperationCanceledException
  {
    if (launchConfiguration.exists ()) {
      String projectName = RefactorUtils.getProjectNameFromLaunchConfig (launchConfiguration);
      if (oldProjectName.equals (projectName))
        return new RefactoringStatus ();
      else {
        String rawMsg = AQLElementRenameCoreTexts.LaunchConfigurationNoLongerForProject;
        Object[] inserts = new Object[] { launchConfiguration.getName (), oldProjectName };
        String msg = MessageFormat.format (rawMsg, inserts);
        return RefactoringStatus.createWarningStatus (msg);
      }
    }

    String rawMsg = AQLElementRenameCoreTexts.LaunchConfigurationNoLongerExists;
    Object[] inserts = new Object[] { launchConfiguration.getName () };
    String msg = MessageFormat.format (rawMsg, inserts);
    return RefactoringStatus.createFatalErrorStatus (msg);
  }

  @Override
  public Change perform (IProgressMonitor pm) throws CoreException
  {
    final ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy ();
    setNewProjectName (wc);
    launchConfiguration = wc.doSave ();

    // the return object is for Undo. 
    return new LaunchConfigurationProjectNameChange (launchConfiguration, newProjectName, oldProjectName);
  }

  @Override
  public Object getModifiedElement ()
  {
    return launchConfiguration;
  }

  /**
   * Verify if the launch configuration references the project with given name.
   * Reference can be project name, location of input document, location of
   * external dictionaries and tables.
   * @param lc
   * @param projectName
   * @return
   */
  @SuppressWarnings("unchecked")
  public static boolean lcContainsProjectName (ILaunchConfiguration lc, String projectName)
  {
    // Get the project paths as "browse workspace" and as "browse local file system".
    // These paths include the file separator at the end to avoid the case
    // the project name is the beginning substring of another project name.
    String oldProjectAsLocal = getProjectLocalLocation (projectName);
    String oldProjectAsWS    = getProjectWorkspaceLocation (projectName);

    // same paths as above but without file separator ending.
    String oldProjectAsLocal_noSepEnding = oldProjectAsLocal.substring (0, oldProjectAsLocal.length () - 1);
    String oldProjectAsWS_noSepEnding    = oldProjectAsWS.substring (0, oldProjectAsWS.length () - 1);

    try {
      String pName = RefactorUtils.getProjectNameFromLaunchConfig (lc);

      // Not a TA project
      if (pName.equals (""))
        return false;

      // reference at project name field
      if (pName.equals (projectName))
        return true;

      // Project name can be in location of input document
      String inDocLocation = lc.getAttribute (IRunConfigConstants.INPUT_COLLECTION, "");
      if ( inDocLocation.startsWith (oldProjectAsWS) ||
           oldProjectAsWS_noSepEnding.equals (inDocLocation) ||
           inDocLocation.startsWith (oldProjectAsLocal) ||
           oldProjectAsLocal_noSepEnding.equals (inDocLocation) )
        return true;

      inDocLocation = lc.getAttribute (IRunConfigConstants.PD_INPUT_COLLECTION, "");
      if ( inDocLocation.startsWith (oldProjectAsWS) ||
           oldProjectAsWS_noSepEnding.equals (inDocLocation) ||
           inDocLocation.startsWith (oldProjectAsLocal) ||
           oldProjectAsLocal_noSepEnding.equals (inDocLocation) )
        return true;

      // Project name can be in a location of external dictionaries
      Map<String, String> extDictMap = lc.getAttribute ( IRunConfigConstants.EXTERNAL_DICT_MAP,
                                                         new LinkedHashMap<String, String> () );
      if (!extDictMap.isEmpty ()) {
        for (String loc : extDictMap.values ()) {
          if ( loc.startsWith (oldProjectAsLocal) ||
               loc.startsWith (oldProjectAsWS) )
            return true;
        }
      }

      // Project name can be in a location of external tables
      Map<String, String> extTabMap = lc.getAttribute ( IRunConfigConstants.EXTERNAL_TABLES_MAP,
                                                        new LinkedHashMap<String, String> () );
      if (!extTabMap.isEmpty ()) {
        for (String loc : extTabMap.values ()) {
          if ( loc.startsWith (oldProjectAsLocal) ||
               loc.startsWith (oldProjectAsWS) )
            return true;
        }
      }
    }
    catch (CoreException e) {
      // will return false anyway
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  private void setNewProjectName (ILaunchConfigurationWorkingCopy wc)
  {
    try {
      //---------------------------------------------------------------------
      // Set project name of the launch configuration
      //---------------------------------------------------------------------
      String pName = wc.getAttribute (IRunConfigConstants.PROJECT_NAME, "");
      if (pName.equals (oldProjectName))
        wc.setAttribute (IRunConfigConstants.PROJECT_NAME, newProjectName);
      else {
        pName = wc.getAttribute (IRunConfigConstants.PD_PROJECT_NAME, "");
        if (pName.equals (oldProjectName))
          wc.setAttribute (IRunConfigConstants.PD_PROJECT_NAME, newProjectName);
      }

      //---------------------------------------------------------------------
      // Set project name in location of input document
      //---------------------------------------------------------------------
      replaceProjectNameInPath (wc, IRunConfigConstants.INPUT_COLLECTION);
      replaceProjectNameInPath (wc, IRunConfigConstants.PD_INPUT_COLLECTION);

      //---------------------------------------------------------------------
      // Set project name in location of external dictionaries
      //---------------------------------------------------------------------
      Map<String, String> extDictMap = wc.getAttribute ( IRunConfigConstants.EXTERNAL_DICT_MAP,
                                                         new LinkedHashMap<String, String> () );
      if (!extDictMap.isEmpty ()) {

        for (String key : extDictMap.keySet ()) {
          String extDictPath = extDictMap.get (key);
          String newExtDictPath = replaceProjectNameInPath (extDictPath);
          extDictMap.put (key, newExtDictPath);
        }

        wc.setAttribute (IRunConfigConstants.EXTERNAL_DICT_MAP, extDictMap);
      }

      //---------------------------------------------------------------------
      // Similarly, set project name in location of external tables
      //---------------------------------------------------------------------
      Map<String, String> extTabMap = wc.getAttribute ( IRunConfigConstants.EXTERNAL_TABLES_MAP,
                                                        new LinkedHashMap<String, String> () );
      if (!extTabMap.isEmpty ()) {

        for (String key : extTabMap.keySet ()) {
          String extTabPath = extTabMap.get (key);
          String newExtTabPath = replaceProjectNameInPath (extTabPath);
          extTabMap.put (key, newExtTabPath);
        }

        wc.setAttribute (IRunConfigConstants.EXTERNAL_TABLES_MAP, extTabMap);
      }
      
      //-----------------------------------------------------------------------------------------------------
      // Update mapped resources with new project name. This property is read by Launch configuration manager
      // to filter out closed or unavailable projects.
      // Refer com.ibm.biginsights.textanalytics.launch.SystemTMainTab#performApply()
      //-----------------------------------------------------------------------------------------------------
      IProject project = ProjectUtils.getProject (newProjectName);
      if (project != null) {
        wc.setMappedResources (new IResource[] {project});
      }
      
    }
    catch (CoreException e) {
      // do nothing if can't get the attribute value
    }

  }

  private void replaceProjectNameInPath (ILaunchConfigurationWorkingCopy wc, String attr2GetPath) throws CoreException
  {
    String oldPath = wc.getAttribute (attr2GetPath, (String) null);
    if (oldPath == null)  // Nothing to replace
      return;

    String newPath = replaceProjectNameInPath (oldPath);
    if (!newPath.equals (oldPath))
      wc.setAttribute (attr2GetPath, newPath);
  }

  private String replaceProjectNameInPath (String oldPath) throws CoreException
  {
    // Get the project paths as "browse workspace" and as "browse local file system".
    // These paths include the file separator at the end to avoid the case
    // the project name is the beginning substring of another project name.
    String oldProjectAsLocal = getProjectLocalLocation (oldProjectName);
    String oldProjectAsWS    = getProjectWorkspaceLocation (oldProjectName);

    String newPath = oldPath;

    if (!StringUtils.isEmpty (oldPath)) {

      // Normal case where path is given by "browse workspace"
      if (oldPath.startsWith (oldProjectAsWS)) {
        newPath = getProjectWorkspaceLocation (newProjectName) + oldPath.substring (oldProjectAsWS.length ());
      }
      // Abnormal case -- oldPath is the path of project. In this case the check
      // oldPath.startsWith (..) doesn't work because oldProjectAsWS has "/" ending.
      else if (oldPath.equals (oldProjectAsWS.substring (0, oldProjectAsWS.length () - 1))) {
        String newProjectPath = getProjectWorkspaceLocation (newProjectName);
        newPath = newProjectPath.substring (0, newProjectPath.length () - 1); // Remove the "/" ending.
      }

      // Normal case where path is given by "browse local file system"
      else if (oldPath.startsWith (oldProjectAsLocal)) {
        newPath = getProjectLocalLocation (newProjectName) + oldPath.substring (oldProjectAsLocal.length ());
      }
      // Abnormal case -- oldPath is the path of project. In this case the check
      // oldPath.startsWith (..) doesn't work because oldProjectAsLocal has file separator ending.
      else if (oldPath.equals (oldProjectAsLocal.substring (0, oldProjectAsLocal.length () - 1))) {
        String newProjectPath = getProjectLocalLocation (newProjectName);
        newPath = newProjectPath.substring (0, newProjectPath.length () - 1); // Remove the file separator ending.
      }

    }

    return newPath;
  }

  /**
   * Get the absolute path to the project with given name.
   */
  private static String getProjectLocalLocation (String projectName)
  {
    IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace ().getRoot ();
    String osFileSeparator = System.getProperty ("file.separator"); //$NON-NLS-1$
    return wsRoot.getLocation ().toOSString () + osFileSeparator + projectName + osFileSeparator;
  }

  /**
   * Get the project path relative to the workspace root.<br>
   * Should be '[w]/projectName'
   */
  private static String getProjectWorkspaceLocation (String projectName)
  {
    return Constants.WORKSPACE_RESOURCE_PREFIX + "/" + projectName + "/";
  }
}
