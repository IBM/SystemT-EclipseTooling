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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

public class NonModularPrefPage
{



  protected SystemTProjectPreferences projectPreferences;
  // pages
  protected GeneralPrefPage generalPrefPage;

  // TAB IMAGES
  private static final String ICON_GENERAL = "icons/full/etool16/general.gif";//$NON-NLS-1$

  // tab indexes
  private static final int TAB_IDX_GENERAL = 0;

  public NonModularPrefPage (SystemTProjectPreferences projectPreferences)
  {
    super ();
    this.projectPreferences = projectPreferences;
    this.generalPrefPage = projectPreferences.generalPrefPage;
  }

  public void setDefaultValuesInPreferenceStore ()
  {

    String[] keys = new String[] { Constants.GENERAL_PROVENANCE, Constants.GENERAL_MAINAQLFILE,
      Constants.SEARCHPATH_DATAPATH, Constants.PAGINATION_ENABLED, Constants.PAGINATION_FILES_PER_PAGE};

    for (String key : keys) {
      projectPreferences.preferenceStore.setDefault (key, projectPreferences.preferenceStore.getString (key));
    }
  }

  public void populateTabGeneral (TabFolder tabFolder)
  {
    // Create tab titled 'General'
    TabItem tabGeneral = new TabItem (tabFolder, SWT.NONE);
    tabGeneral.setText (Messages.getString ("SystemTProjectPreferences.GENERAL")); //$NON-NLS-1$
    tabGeneral.setImage (ProjectPreferencesUtil.getImage (ICON_GENERAL));

    createTabGeneral (tabFolder, tabGeneral);
    tabGeneral.setControl (generalPrefPage.getControl ());
    generalPrefPage.addDataPathChangeListener (projectPreferences);
    generalPrefPage.addModifyListeners (projectPreferences);
    generalPrefPage.addSelectionListeners (projectPreferences);
  }

  protected void createTabGeneral (TabFolder tabFolder, TabItem tabGeneral)
  {
    generalPrefPage = new GeneralPrefPage (tabFolder, projectPreferences);
  }

  public boolean savePreferenceStore ()
  {
    // Save can be invoked ONLY by PropertySheet consumer
    if (projectPreferences.consumer != Constants.CONSUMER_PROPERTY_SHEET) { return false; }

    try {
      if (projectPreferences.preferenceStore.needsSaving ()) {
        projectPreferences.preferenceStore.save ();

        try {
          // Added this code for supporting compilation
          // whenever the system t properties gets changed
          // The compilation is invoked only if the main aql file or
          // search path is changed.
          if (generalPrefPage.isDirty (Constants.GENERAL_MAINAQLFILE)
            || generalPrefPage.isDirty (Constants.SEARCHPATH_DATAPATH)
            // Also, compilation needs to happen whenever the
            // user has enabled the provenance.
            || (generalPrefPage.isDirty (Constants.GENERAL_PROVENANCE) && generalPrefPage.cbProvenance.getSelection ())) {
            IProject project = ProjectPreferencesUtil.getSelectedProject ();
            if (generalPrefPage.isDirty (Constants.GENERAL_MAINAQLFILE)) {
              String oldMainAQLFilePath = generalPrefPage.getOldValue (Constants.GENERAL_MAINAQLFILE);
              if (oldMainAQLFilePath != null) {
                // If the MainAQL file is changed, then remove
                // the marker errors from the old main AQL file
                // so that it does not cause any confusion. This
                // confusion happens especially when the old AQL
                // file
                // is now an included file of the new main AQL
                // file.
                String absOldMainAQLPath = ProjectPreferencesUtil.getAbsolutePath (oldMainAQLFilePath);
                if (absOldMainAQLPath != null) {
                  IFile mainAQLFileResource = (project.getWorkspace ().getRoot ()).getFileForLocation (new Path (
                    absOldMainAQLPath));
                  if (mainAQLFileResource != null) {
                    mainAQLFileResource.deleteMarkers (IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
                  }
                }
              }
            }
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

    if (!generalPrefPage.isValid ()) {
      projectPreferences.setErrorMessage (generalPrefPage.getErrorMessage ());
      return false;
    }

    projectPreferences.setErrorMessage (null);
    return true;
  }

  public void restoreToProjectProperties (SystemTProperties props)
  {
    generalPrefPage.restoreToProjectProperties (props);
  }

  public void performDefaults ()
  {

    switch (projectPreferences.tabFolder.getSelectionIndex ()) {
      case TAB_IDX_GENERAL:
        generalPrefPage.restoreDefaults ();
      break;
    }

    isDataValid ();
  }

  public void performApply (boolean savePrefStore)
  {
    switch (projectPreferences.tabFolder.getSelectionIndex ()) {
      case TAB_IDX_GENERAL:
        generalPrefPage.apply ();
        if (savePrefStore) {
          savePreferenceStore ();
        }
      break;
    }
  }

  public void performApplyAll ()
  {
    if (isDataValid ()) {
      generalPrefPage.apply ();
    }
  }

  public void performApply ()
  {
    if (StringUtils.isEmpty (generalPrefPage.searchPathPrefPage.dataPathEditor.getDataPath ())) {
      generalPrefPage.searchPathPrefPage.dataPathEditor.addDataPath (
        ProjectPreferencesUtil.getPath (ProjectPreferencesUtil.getDefaultDataPath (projectPreferences.getProject ())),
        true);
    }
    if (isDataValid ()) {
      performApply (true);
    }
  }
}
