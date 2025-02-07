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

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
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

public class LaunchConfigurationModuleNameChange extends Change
{


 
	ILaunchConfiguration launchConfiguration;
  String projectName;
  String oldModuleName;
  String newModuleName;

  public LaunchConfigurationModuleNameChange (ILaunchConfiguration launchConfiguration, String projectName,
    String oldModuleName, String newModuleName)
  {
    this.launchConfiguration = launchConfiguration;
    this.oldModuleName = oldModuleName;
    this.newModuleName = newModuleName;
    this.projectName = projectName;
  }

  @Override
  public String getName ()
  {
    return MessageFormat.format (AQLElementRenameCoreTexts.UpdateLaunchConfigurationModuleName,
      new Object[] { launchConfiguration.getName () });
  }

  @Override
  public void initializeValidationData (IProgressMonitor pm)
  {}

  @Override
  public RefactoringStatus isValid (IProgressMonitor pm) throws CoreException, OperationCanceledException
  {
    if (launchConfiguration.exists ()) {
      String proName = RefactorUtils.getProjectNameFromLaunchConfig (launchConfiguration);
      String moduleName = getModuleNameFromLaunchConfig (launchConfiguration);
      String modNameWithSeperator = oldModuleName + Constants.DATAPATH_SEPARATOR;
      if (projectName.equals (proName) && moduleName.indexOf (modNameWithSeperator) != -1)
        return new RefactoringStatus ();
      else {
        String rawMsg = AQLElementRenameCoreTexts.LaunchConfigurationNoLongerForProject;
        Object[] inserts = new Object[] { launchConfiguration.getName (), projectName };
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
    setNewModuleName (wc);
    launchConfiguration = wc.doSave ();

    // the return object is for Undo. 
    return new LaunchConfigurationModuleNameChange (launchConfiguration, projectName,newModuleName, oldModuleName );
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
  public static boolean lcContainsProjectName (ILaunchConfiguration lc, String projectName)
  {
    String pName = RefactorUtils.getProjectNameFromLaunchConfig (lc);

    // Not a TA project
    if (pName.equals (""))
      return false;

    // reference at project name field
    if (pName.equals (projectName))
      return true;

    return false;
  }
  
  /**
   * Verify if the launch configuration references the project with given name and the module name is selected.
   * 
   * @param lc
   * @param projectName
   * @param modName
   * @return
   */
  public static boolean lcContainsModuleName (ILaunchConfiguration lc, String projectName, String modName)
  {
    String pName = RefactorUtils.getProjectNameFromLaunchConfig (lc);
    String mName = getModuleNameFromLaunchConfig (lc);

    String modNameWithSeperator = modName + Constants.DATAPATH_SEPARATOR;

    // Not a TA project or module name not set
    if (pName.equals ("") || mName.equals (""))
      return false;

    // reference at project name field && module name
    if (pName.equals (projectName) && mName.indexOf (modNameWithSeperator) != -1)
      return true;

    // For pattern discovery, module can also be at the selected output view
    Set<String> allProjects = new HashSet<String>();
    allProjects.add (pName);
    String allDepProjects = ProjectUtils.getProjectDependency (pName);
    if (!StringUtils.isEmpty (allDepProjects)) {
      String[] projs = allDepProjects.split (Constants.DATAPATH_SEPARATOR);
      for (String p : projs) {
        allProjects.add (p.substring (1));
      }
    }

    if (allProjects.contains (projectName) && isPatternDiscoveryLaunchConfig (lc)) {
      try {
        String outputView = lc.getAttribute (IRunConfigConstants.PD_OUTPUT_VIEW_ATTR_NAME, "");
        String modulePrefix = modName + ".";
        if (outputView.contains (modulePrefix))
          return true;
      }
      catch (CoreException e) {
        // Do nothing, just skip checking output view
      }
    }

    // reference at external dictionaries and tables
    if (moduleHasExtDicts(lc, projectName, modName))
      return true;

    String modulePath_Local = getModuleLocalLocation (projectName, modName);
    if (partOfExtDictsOrTablesPath (lc, modulePath_Local))
      return true;

    if (moduleHasExtTables(lc, projectName, modName))
      return true;

    String modulePath_WS = getModuleWorkspaceLocation (projectName, modName);
    if (partOfExtDictsOrTablesPath (lc, modulePath_WS))
      return true;

    return false;
  }

  private static boolean isPatternDiscoveryLaunchConfig (ILaunchConfiguration lc)
  {
    try {
      return (lc != null) &&
             (lc.getType () != null) &&
             (lc.getType ().getIdentifier () != null) &&
             (lc.getType ().getIdentifier ().equals (IRunConfigConstants.PD_LAUNCH_CONFIG_ID) );
    }
    catch (CoreException e) {
      // will return false
    }

    return false;
  }

  private static boolean moduleHasExtDicts (ILaunchConfiguration lc, String projectName, String moduleName)
  {
    String pName = RefactorUtils.getProjectNameFromLaunchConfig (lc);

    // The launch config is associated with projectName
    if (pName.equals (projectName)) {
      Map<String,String> extDicts = getExtDictionariesFromLaunchConfig (lc);

      // The key for external dictionary is <module name>.<dictionary name>
      for (String key : extDicts.keySet ()) {
        if (key.startsWith (moduleName + "."))   //$NON-NLS-1$
          return true;
      }
    }

    return false;
  }

  private static boolean moduleHasExtTables (ILaunchConfiguration lc, String projectName, String moduleName)
  {
    String pName = RefactorUtils.getProjectNameFromLaunchConfig (lc);

    // The launch config is associated with projectName
    if (pName.equals (projectName)) {
      Map<String,String> extTables = getExtTablesFromLaunchConfig (lc);

      // The key for external table is <module name>.<table name>
      for (String key : extTables.keySet ()) {
        if (key.startsWith (moduleName + "."))   //$NON-NLS-1$
          return true;
      }
    }

    return false;
  }

  private static boolean partOfExtDictsOrTablesPath (ILaunchConfiguration lc, String objPath)
  {
    Map<String,String> extDicts = getExtDictionariesFromLaunchConfig (lc);
    Map<String,String> extTables = getExtTablesFromLaunchConfig (lc);

    for (String dict : extDicts.keySet ()) {
      String path = extDicts.get (dict);
      if (path.startsWith (objPath))
        return true;
    }
    for (String extTab : extTables.keySet ()) {
      String path = extTables.get (extTab);
      if (path.startsWith (objPath))
        return true;
    }

  return false;
   
  }

  @SuppressWarnings("unchecked")
  private void setNewModuleName (ILaunchConfigurationWorkingCopy wc)
  {
    try {
      //---------------------------------------------------------------------
      // Set module name of the launch configuration
      //---------------------------------------------------------------------
      String mName = wc.getAttribute (IRunConfigConstants.SELECTED_MODULES, "");
      if (mName.indexOf (oldModuleName + Constants.DATAPATH_SEPARATOR) != -1){
        //Replace old Module name with new Module name.
        mName = replaceModuleInModuleString (oldModuleName, newModuleName, mName);
        wc.setAttribute (IRunConfigConstants.SELECTED_MODULES, mName);
      }

      String oldModulePath_local = getModuleLocalLocation (projectName, oldModuleName);
      String newModulePath_local = getModuleLocalLocation (projectName, newModuleName);
      String oldModulePath_WS = getModuleWorkspaceLocation (projectName, oldModuleName);
      String newModulePath_WS = getModuleWorkspaceLocation (projectName, newModuleName);

      String pName = RefactorUtils.getProjectNameFromLaunchConfig (wc);
      Set<String> allProjects = new HashSet<String>();
      allProjects.add (pName);
      String allDepProjects = ProjectUtils.getProjectDependency (pName);
      if (!StringUtils.isEmpty (allDepProjects)) {
        String[] projs = allDepProjects.split (Constants.DATAPATH_SEPARATOR);
        for (String p : projs) {
          allProjects.add (p.substring (1));
        }
      }

      //---------------------------------------------------------------------
      // Set module name in output view of pattern discovery launch configuration
      //---------------------------------------------------------------------
      if (allProjects.contains (projectName) && isPatternDiscoveryLaunchConfig (wc)) {
        String outputView = wc.getAttribute (IRunConfigConstants.PD_OUTPUT_VIEW_ATTR_NAME, "");
        String oldModulePrefix = oldModuleName + ".";
        String newModulePrefix = newModuleName + ".";
        if (outputView.startsWith (oldModulePrefix))
          wc.setAttribute (IRunConfigConstants.PD_OUTPUT_VIEW_ATTR_NAME, outputView.replace (oldModulePrefix, newModulePrefix));
      }

      //---------------------------------------------------------------------
      // Set module path in location of external dictionaries
      //---------------------------------------------------------------------
      Map<String, String> extDictMap = wc.getAttribute ( IRunConfigConstants.EXTERNAL_DICT_MAP,
                                                         new LinkedHashMap<String, String> () );
      if (!extDictMap.isEmpty ()) {
        Map<String, String> newExtDictMap = new LinkedHashMap<String, String> ();
        boolean modified = false;

        for (String key : extDictMap.keySet ()) {
          String newKey = key;
          String newExtDictPath = extDictMap.get (key);

          if (allProjects.contains (projectName) && newKey.startsWith (oldModuleName + ".")) {
            newKey = newModuleName + "." + newKey.substring (newKey.indexOf (".") + 1);
            modified = true;
          }
          if (newExtDictPath.startsWith (oldModulePath_local)) {
            newExtDictPath = newModulePath_local + newExtDictPath.substring (oldModulePath_local.length ());
            modified = true;
          }
          else if (newExtDictPath.startsWith (oldModulePath_WS)) {
            newExtDictPath = newModulePath_WS + newExtDictPath.substring (oldModulePath_WS.length ());
            modified = true;
          }

          newExtDictMap.put (newKey, newExtDictPath);
        }

        if (modified)
          wc.setAttribute (IRunConfigConstants.EXTERNAL_DICT_MAP, newExtDictMap);
      }

      //---------------------------------------------------------------------
      // Set module path in location of external tables
      //---------------------------------------------------------------------
      Map<String, String> extTabMap = wc.getAttribute ( IRunConfigConstants.EXTERNAL_TABLES_MAP,
                                                        new LinkedHashMap<String, String> () );
      if (!extTabMap.isEmpty ()) {
        Map<String, String> newExtTableMap = new LinkedHashMap<String, String> ();
        boolean modified = false;

        for (String key : extTabMap.keySet ()) {
          String newKey = key;
          String newExtTabPath = extTabMap.get (key);

          if (allProjects.contains (projectName) && newKey.startsWith (oldModuleName + ".")) {
            newKey = newModuleName + "." + newKey.substring (newKey.indexOf (".") + 1);
            modified = true;
          }
          if (newExtTabPath.startsWith (oldModulePath_local)) {
            newExtTabPath = newModulePath_local + newExtTabPath.substring (oldModulePath_local.length ());
            modified = true;
          }
          else if (newExtTabPath.startsWith (oldModulePath_WS)) {
            newExtTabPath = newModulePath_WS + newExtTabPath.substring (oldModulePath_WS.length ());
            modified = true;
          }

          newExtTableMap.put (newKey, newExtTabPath);
        }

        if (modified)
          wc.setAttribute (IRunConfigConstants.EXTERNAL_TABLES_MAP, newExtTableMap);
      }

      
    }
    catch (CoreException e) {
      // do nothing if can't get the attribute value
    }

  }
  
  /**
   * Find and replace old module name with new module name in the string concatenation of module names.
   * @param oldModName  The old module name.
   * @param newModName  The new module name.
   * @param modNameStr  The concatenation of module names, separate by ";".
   * @return The new string concatenation of module names.
   */
  private static String replaceModuleInModuleString (String oldModName, String newModName, String modNameStr)
  {
    String[] modNames = modNameStr.split (Constants.DATAPATH_SEPARATOR);

    String newModNames = "";
    for (String module : modNames) {
      if (module.equals (oldModName))
        newModNames += newModName + Constants.DATAPATH_SEPARATOR; 
      else
        newModNames += oldModName + Constants.DATAPATH_SEPARATOR; 
    }

    return newModNames;
  }

  private static String getModuleNameFromLaunchConfig (ILaunchConfiguration launchConfiguration)
  {
    String modName = "";
    try {
      modName = launchConfiguration.getAttribute (IRunConfigConstants.SELECTED_MODULES, "");

    }
    catch (CoreException e) {
      // will return "".
    }

    return modName;
  }

  @SuppressWarnings("unchecked")
  private static Map<String,String> getExtDictionariesFromLaunchConfig (ILaunchConfiguration launchConfiguration)
  {
    Map<String,String> dictMap = new LinkedHashMap<String,String>();
    try {
      dictMap = launchConfiguration.getAttribute (IRunConfigConstants.EXTERNAL_DICT_MAP, new LinkedHashMap<String,String>());
    }
    catch (CoreException e) {
      // will return empty map.
    }

    return dictMap;
  }

  @SuppressWarnings("unchecked")
  private static Map<String,String> getExtTablesFromLaunchConfig (ILaunchConfiguration launchConfiguration)
  {
    Map<String,String> extTableMap = new LinkedHashMap<String,String>();
    try {
      extTableMap = launchConfiguration.getAttribute (IRunConfigConstants.EXTERNAL_TABLES_MAP, new LinkedHashMap<String,String>());
    }
    catch (CoreException e) {
      // will return empty map.
    }

    return extTableMap;
  }

  /**
   * Get the absolute path to the project with given name.
   */
  private static String getModuleLocalLocation (String projectName, String moduleName)
  {
    IFolder moduleFolder = ProjectUtils.getModuleFolder (projectName, moduleName);
    return moduleFolder.getLocation ().toOSString () + File.separator;
  }

  /**
   * Get the project path relative to the workspace root.<br>
   * Should be '[w]/projectName'
   */
  private static String getModuleWorkspaceLocation (String projectName, String moduleName)
  {
    IFolder moduleFolder = ProjectUtils.getModuleFolder (projectName, moduleName);
    return Constants.WORKSPACE_RESOURCE_PREFIX + moduleFolder.getFullPath () + "/";
  }

}
