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

package com.ibm.biginsights.textanalytics.nature.prefs;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

// import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class ModularPrefPage
{



  // tab indexes
  private static final int TAB_IDX_GENERAL = 0;
  private static final int TAB_IDX_SRC = 1;
  private static final int TAB_IDX_PROJECT = 2;

  private static final ILog LOGGER = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);
  
  SystemTProjectPreferences projectPreferences;

  protected ModularGeneralPrefPage modularGeneralPrefPage;
  protected ModularSourcePrefPage modularSourcePrefPage;
  protected ModularProjectPrefPage modularProjectPrefPage;

  // TAB IMAGES
  private static final String ICON_GENERAL = "icons/full/etool16/general.gif";//$NON-NLS-1$
  private static final String ICON_PROJECT = "icons/full/etool16/projectFolder.png";//$NON-NLS-1$
  private static final String ICON_SOURCE = "icons/full/etool16/ModuleSrc.png";//$NON-NLS-1$

  public ModularPrefPage (SystemTProjectPreferences projectPreferences)
  {
    super ();
    this.projectPreferences = projectPreferences;
  }

  public void setDefaultValuesInPreferenceStore ()
  {

    String[] keys = new String[] { Constants.GENERAL_PROVENANCE, Constants.TAM_PATH, Constants.MODULE_SRC_PATH,
      Constants.MODULE_BIN_PATH, Constants.DEPENDENT_PROJECT, Constants.PAGINATION_ENABLED, Constants.PAGINATION_FILES_PER_PAGE };

    for (String key : keys) {
      projectPreferences.preferenceStore.setDefault (key, projectPreferences.preferenceStore.getString (key));
    }
  }

  public void populateModularTabGeneral (TabFolder tabFolder)
  {
    // Create tab titled 'General'
    TabItem tabGeneral = new TabItem (tabFolder, SWT.NONE);
    tabGeneral.setText (Messages.getString ("SystemTProjectPreferences.GENERAL")); //$NON-NLS-1$
    tabGeneral.setImage (ProjectPreferencesUtil.getImage (ICON_GENERAL));

    modularGeneralPrefPage = new ModularGeneralPrefPage (tabFolder, projectPreferences);
    tabGeneral.setControl (modularGeneralPrefPage.getControl ());
    modularGeneralPrefPage.addDataPathChangeListener (projectPreferences);
    modularGeneralPrefPage.addSelectionListeners (projectPreferences);

  }

  public void populateModularSourcePrefPage (TabFolder tabFolder)
  {
    // Create tab titled 'General'
    TabItem tabGeneral = new TabItem (tabFolder, SWT.NONE);
    tabGeneral.setText (Messages.getString ("SystemTProjectPreferences.Source")); //$NON-NLS-1$
    tabGeneral.setImage (ProjectPreferencesUtil.getImage (ICON_SOURCE));

    modularSourcePrefPage = new ModularSourcePrefPage (tabFolder, projectPreferences);
    tabGeneral.setControl (modularSourcePrefPage.getControl ());
  }

  public void populateModularProjectPrefPage (TabFolder tabFolder)
  {
    // Create tab titled 'General'
    TabItem tabGeneral = new TabItem (tabFolder, SWT.NONE);
    tabGeneral.setText (Messages.getString ("SystemTProjectPreferences.Project")); //$NON-NLS-1$
    tabGeneral.setImage (ProjectPreferencesUtil.getImage (ICON_PROJECT));

    modularProjectPrefPage = new ModularProjectPrefPage (tabFolder, projectPreferences);
    tabGeneral.setControl (modularProjectPrefPage.getControl ());
  }

  public void populateModularAdvancedPrefPage (TabFolder tabFolder)
  {
    TabItem tabGeneral = new TabItem (tabFolder, SWT.NONE);
    tabGeneral.setText (Messages.getString ("SystemTProjectPreferences.ADVANCED")); //$NON-NLS-1$
  }

  public boolean savePreferenceStore ()
  {
    // Save can be invoked ONLY by PropertySheet consumer
    if (projectPreferences.consumer != Constants.CONSUMER_PROPERTY_SHEET) { return false; }

    try {
      if (projectPreferences.preferenceStore.needsSaving ()) {
        projectPreferences.preferenceStore.save ();

        IProject project = ProjectPreferencesUtil.getSelectedProject ();

        String projectDependencyStr = ProjectUtils.getProjectDependency (project.getName ());
        if (projectDependencyStr != null && !projectDependencyStr.isEmpty ()) {
          String dep[] = projectDependencyStr.split (";");
          ProjectUtils.updateProjectReferences (project, dep, true);
        }

        try {
          // Added this code for supporting compilation
          // whenever the system t properties gets changed
          // The compilation is invoked only if the main aql file or
          // search path is changed.
          if (modularGeneralPrefPage.isDirty (Constants.TAM_PATH)
            || modularSourcePrefPage.isDirty (Constants.MODULE_SRC_PATH)
            || modularSourcePrefPage.isDirty (Constants.MODULE_BIN_PATH)
            || modularProjectPrefPage.isDirty (Constants.DEPENDENT_PROJECT)
            // Also, compilation needs to happen whenever the
            // user has enabled the provenance.
            || (modularGeneralPrefPage.isDirty (Constants.GENERAL_PROVENANCE) && modularGeneralPrefPage.cbProvenance.getSelection ())
          )
            {
            project.build (IncrementalProjectBuilder.CLEAN_BUILD, null);
          }
        }
        catch (CoreException e) {
          Activator.getDefault ().getLog ().log (new Status (IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage ()));
        }
      }
      return true;
    }
    catch (IOException e) {
      Activator.getDefault ().getLog ().log (new Status (IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage ()));

      return false;
    }
  }

  public boolean isDataValid ()
  {

    if (!modularGeneralPrefPage.isValid ()) {
      projectPreferences.setErrorMessage (modularGeneralPrefPage.getErrorMessage ());
      return false;
    }
    if (!modularSourcePrefPage.isValid ()) {
      projectPreferences.setErrorMessage (modularSourcePrefPage.getErrorMessage ());
      return false;
    }
    if (!modularProjectPrefPage.isValid ()) {
      projectPreferences.setErrorMessage (modularProjectPrefPage.getErrorMessage ());
      return false;
    }
    projectPreferences.setErrorMessage (null);
    return true;
  }

  public void restoreToProjectProperties (SystemTProperties props)
  {
    modularGeneralPrefPage.restoreToProjectProperties (props);
    modularSourcePrefPage.restoreToProjectProperties (props);
    modularProjectPrefPage.restoreToProjectProperties (props);
  }

  public void performDefaults ()
  {

    switch (projectPreferences.tabFolder.getSelectionIndex ()) {
      case TAB_IDX_GENERAL:
        modularGeneralPrefPage.restoreDefaults ();
      break;
      case TAB_IDX_SRC:
        modularSourcePrefPage.restoreDefaults ();
      break;
      case TAB_IDX_PROJECT:
        modularProjectPrefPage.restoreDefaults ();
      break;
    }

    isDataValid ();
  }

  public void performApply (boolean savePrefStore)
  {
    switch (projectPreferences.tabFolder.getSelectionIndex ()) {
      case TAB_IDX_GENERAL:
        modularGeneralPrefPage.apply ();
        if (savePrefStore) {
          savePreferenceStore ();
        }
      break;
      case TAB_IDX_SRC:
        modularSourcePrefPage.apply ();
        if (savePrefStore) {
          savePreferenceStore ();
        }
      break;
      case TAB_IDX_PROJECT:
        modularProjectPrefPage.apply ();
        if (savePrefStore) {
          savePreferenceStore ();
        }
      break;
    }
    //calling refresh method to refresh the resources 
    refreshResource();
  }

  public void performApplyAll ()
  {
    if (isDataValid ()) {
      modularGeneralPrefPage.apply ();
      modularSourcePrefPage.apply ();
      modularProjectPrefPage.apply ();
    }
    //calling refresh method to refresh the resources 
    refreshResource();
  }
  
  private void refreshResource(){
    IProject project = ProjectPreferencesUtil.getSelectedProject ();
    try {
      project.refreshLocal (IResource.DEPTH_INFINITE, null);
    }
    catch (CoreException e) {
      LOGGER.logError (String.format ("Unable to refresh the resources %s", project.getName ()), e); //$NON-NLS-1$
    }
  }

}
