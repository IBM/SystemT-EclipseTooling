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
package com.ibm.biginsights.textanalytics.migration.action;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferenceStore;

import com.ibm.biginsights.textanalytics.migration.MigrationPlugin;
import com.ibm.biginsights.textanalytics.migration.Messages;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class enables migration of text analytics properties of a v2.0 text analytics project to a format appropriate
 * for a v2.1 text analytics project. It is instantiated by the class MigrateTextAnalyticsProject.
 * 
 * 
 */
public class MigrateV20ToV21 extends AbstractTAMigration
{


  
	public MigrateV20ToV21 (IProject project, String oldVersion, String newVersion)
  {
    super (project, oldVersion, newVersion);
  }

  /**
   * Checks if a project is valid text analytics v2.0 project and migrates its properties to a format appropriate for a
   * v2.1 project.
   * <p>
   * In v21 text analytics projects, the module source path and module bin path property values are supposed to be
   * relative to the project, not workspace. Hence when a v2.0 project is imported into a v2.1 workspace or a v2.0
   * workspace is updated to v2.1, the properties need to be migrated.
   * </p>
   */
  @Override
  public void migrateTextAnalyticsPropertyFile ()
  {
    boolean propertiesChanged = false;
    try {
      if (project != null && project.isOpen () && project.hasNature (Constants.PLUGIN_NATURE_ID)) {
        PreferenceStore preferenceStore = ProjectUtils.getPreferenceStore (project);
        // only modular projects are eligible for migration
        if (preferenceStore != null && preferenceStore.getBoolean (Constants.MODULAR_AQL_PROJECT)) {
          String moduleSrcProp = preferenceStore.getString (Constants.MODULE_SRC_PATH);
          String moduleBinProp = preferenceStore.getString (Constants.MODULE_BIN_PATH);
          if (moduleSrcProp.startsWith (Constants.WORKSPACE_RESOURCE_PREFIX)) { // if source path property values starts
                                                                                // with "[W]", it's in old format.
                                                                                // (it used to be
                                                                                // [W]/<projectname>/xyz).
            IPath moduleSrcPath = new Path (moduleSrcProp.replace (Constants.WORKSPACE_RESOURCE_PREFIX, "")).makeRelativeTo (project.getFullPath ()); //$NON-NLS-1$
            preferenceStore.setValue (Constants.MODULE_SRC_PATH,
              Constants.PROJECT_RELATIVE_PATH_PREFIX + moduleSrcPath.toString ()); // new value will begin with [P] e.g.
                                                                                   // [P]textanalytics/src
            propertiesChanged = true;
          }
          if (moduleBinProp.startsWith (Constants.WORKSPACE_RESOURCE_PREFIX)) { // if bin path property values starts
                                                                                // with "[W]", it's in old format.
            IPath moduleBinPath = new Path (moduleBinProp.replace (Constants.WORKSPACE_RESOURCE_PREFIX, "")).makeRelativeTo (project.getFullPath ()); //$NON-NLS-1$
            preferenceStore.setValue (Constants.MODULE_BIN_PATH,
              Constants.PROJECT_RELATIVE_PATH_PREFIX + moduleBinPath.toString ()); // new value will begin with [P]
            propertiesChanged = true;
          }
          if (propertiesChanged) {
            try {
              preferenceStore.save ();
              IResource propFile = project.findMember (Constants.TEXT_ANALYTICS_PREF_FILE);
              if (propFile != null) {
                propFile.refreshLocal (IResource.DEPTH_ZERO, new NullProgressMonitor ()); // Let workspace find out that
                                                                                          // this file has been
                                                                                          // modified.
              }
            }
            catch (IOException e) {
              // This is the only case where you can conclude that a migration attempt has failed. Other cases where
              // false is being returned, it might simply be a case of conditions to trigger migration not being met.
              String[] args = {project.getName()};
              LogUtil.getLogForPlugin (MigrationPlugin.PLUGIN_ID).logWarning (
                Messages.getString ("MigrateV20ToV21_TAPropMigrationV20ToV21FailedMsg", args) + "\n" +  e.getMessage ());  //$NON-NLS-1$ //$NON-NLS-2$
              propertiesChanged = false;
            }
            if (propertiesChanged) {
              String[] args = {project.getName ()};
              LogUtil.getLogForPlugin (MigrationPlugin.PLUGIN_ID).logInfo (
                Messages.getString ("MigrateV20ToV21_PropMigrationV20ToV21CompleteMsg",args)); //$NON-NLS-1$
            }
          }
        }
      }
    }
    catch (CoreException e) {
      String[] args = {project.getName()};
      LogUtil.getLogForPlugin (MigrationPlugin.PLUGIN_ID).logWarning (Messages.getString ("MigrateV20ToV21_TAPropMigrationV20ToV21Skip", args)); //$NON-NLS-1$
    }
  }

  @Override
  public void migrateLaunchConfigs ()
  {
    // No modification required.

  }

  @Override
  public void migrateFolders ()
  {
    // No modification required.

  }

}
