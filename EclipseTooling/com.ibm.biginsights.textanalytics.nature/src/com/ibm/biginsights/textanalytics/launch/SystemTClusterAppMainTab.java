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

package com.ibm.biginsights.textanalytics.launch;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;
import com.ibm.biginsights.project.locations.apps.ApplicationProvider;
import com.ibm.biginsights.project.locations.apps.IBigInsightsApp;
import com.ibm.biginsights.project.util.BIConnectionException;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.ProjectBrowser;

public class SystemTClusterAppMainTab extends AbstractLaunchConfigurationTab
{


 
	private ProjectBrowser projectBrowser;
  private Combo serverCombo, extractorCombo, inputDataCombo;
  private Collection<IBigInsightsLocation> locations = null;

  private ModifyListener commonListener = new ModifyListener () {
    @Override
    public void modifyText(ModifyEvent event) {
        setDirty(true);
        updateLaunchConfigurationDialog();
    }
  };

  private ModifyListener serverListener = new ModifyListener () {
    @Override
    public void modifyText(ModifyEvent event) {
        setDirty(true);
        updateExtractorAndDataCombos();
        updateLaunchConfigurationDialog();
    }
  };

  @Override
  public void createControl (Composite parent)
  {
    parent.getShell().setMinimumSize(800, 500);

    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout());

    // Project browser
    Composite projBrowserPanel = new Composite(composite, SWT.NONE);
    GridLayout projLayout = new GridLayout();
    projBrowserPanel.setLayout(projLayout);
    projBrowserPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    projectBrowser = new ProjectBrowser(projBrowserPanel, SWT.NONE);
    projectBrowser.addModifyListenerForProjectTextField (commonListener);

    // Combo boxes
    Composite infoPanel = new Composite(composite, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    layout.marginHeight = 10;
    layout.marginWidth = 10;
    layout.verticalSpacing = 20;
    infoPanel.setLayout(layout);
    infoPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    Label svrLabel = new Label(infoPanel, SWT.NONE);
    svrLabel.setText(Messages.getString("SystemTClusterAppMainTab.SERVER")); // $NON-NLS-1$
    svrLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    serverCombo = new Combo(infoPanel, SWT.READ_ONLY);
    serverCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    serverCombo.addModifyListener(serverListener);

    Label extrLabel = new Label(infoPanel, SWT.NONE);
    extrLabel.setText(Messages.getString("SystemTClusterAppMainTab.EXTRACTOR")); // $NON-NLS-1$
    extrLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    extractorCombo = new Combo(infoPanel, SWT.READ_ONLY);
    extractorCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    extractorCombo.addModifyListener(commonListener);

    Label dataLabel = new Label(infoPanel, SWT.NONE);
    dataLabel.setText(Messages.getString("SystemTClusterAppMainTab.INPUTDATA")); // $NON-NLS-1$
    dataLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
    inputDataCombo = new Combo(infoPanel, SWT.READ_ONLY);
    inputDataCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    inputDataCombo.addModifyListener(commonListener);

    setControl(composite);
  }

  protected void updateExtractorAndDataCombos ()
  {
    String selectedServer = serverCombo.getText ();

    if ( locations == null ||
         StringUtils.isEmpty (selectedServer) )
      return;

    IBigInsightsLocation server = null;
    for (IBigInsightsLocation loc : locations) {
      if (loc.getLocationDisplayString ().equals (selectedServer)) {
        server = loc;
        break;
      }
    }

    try {
      ApplicationProvider appProvider = new ApplicationProvider();
      Collection<IBigInsightsApp> apps = appProvider.getPublishedApplications(server.getHostName(), server.getHttpClient(), server);
      extractorCombo.removeAll ();
      if ( apps != null && !apps.isEmpty () ) {
        for (IBigInsightsApp app : apps) {
          if ( app.getStatus ().equals (com.ibm.biginsights.project.Messages.Decoration_deployed) )
            extractorCombo.add (app.getAppName ());
        }
      }
    }
    catch (BIConnectionException e) {
      server.handleBIConnectionExceptionFromThread(e);
    }
  }

  @Override
  public void setDefaults (ILaunchConfigurationWorkingCopy configuration)
  {
    IProject project = ProjectPreferencesUtil.getSelectedProject();
    if (project != null) {
      String selectedProject = project.getName();
      configuration.setAttribute(IRunConfigConstants.PROJECT_NAME, selectedProject);
    }
  }

  @Override
  public void initializeFrom (ILaunchConfiguration configuration)
  {
    // populate server combo with list of registered BI servers
    locations = LocationRegistry.getInstance().getLocations();
    if (locations != null) {
      for (IBigInsightsLocation loc : locations) {
        serverCombo.add (loc.getLocationDisplayString ());
      }
    }

    // select the last configured values
    try {
      String projectName = configuration.getAttribute(IRunConfigConstants.PROJECT_NAME, "");    // $NON-NLS-1$
      projectBrowser.setProject(projectName);

      String serverName = configuration.getAttribute(IRunConfigConstants.BI_SERVER_NAME, "");    // $NON-NLS-1$
      selectComboItem (serverCombo, serverName);

      String extrName = configuration.getAttribute(IRunConfigConstants.EXTRACTOR_APPNAME, "");    // $NON-NLS-1$
      selectComboItem (extractorCombo, extrName);

      String datasetName = configuration.getAttribute(IRunConfigConstants.INPUT_DATASET_NAME, "");    // $NON-NLS-1$
      selectComboItem (inputDataCombo, datasetName);
    }
    catch (CoreException e) {
      // couldn't find existing project, just don't select any.
    }      
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration)
  {
    // save user selected values to 'configuration'
    configuration.setAttribute(IRunConfigConstants.PROJECT_NAME, projectBrowser.getProject());
    configuration.setAttribute(IRunConfigConstants.BI_SERVER_NAME, serverCombo.getText());
    configuration.setAttribute(IRunConfigConstants.EXTRACTOR_APPNAME, extractorCombo.getText());
    configuration.setAttribute(IRunConfigConstants.INPUT_DATASET_NAME, inputDataCombo.getText());
  }

  @Override
  public String getName ()
  {
    return Messages.getString("SystemTMainTab.MAIN"); //$NON-NLS-1$
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig)
  {
    try {
      String projectName = launchConfig.getAttribute(IRunConfigConstants.PROJECT_NAME, "");      //$NON-NLS-1$
      String biServerName = launchConfig.getAttribute(IRunConfigConstants.BI_SERVER_NAME, "");      //$NON-NLS-1$
      String extractorName = launchConfig.getAttribute(IRunConfigConstants.EXTRACTOR_APPNAME, "");      //$NON-NLS-1$
      String datasetName = launchConfig.getAttribute(IRunConfigConstants.INPUT_DATASET_NAME, "");      //$NON-NLS-1$
      if ( StringUtils.isEmpty(projectName) ||
           StringUtils.isEmpty(biServerName) ||
           StringUtils.isEmpty(extractorName) ||
           StringUtils.isEmpty(datasetName) )
        return false;
    }
    catch (CoreException e) {
      // TODO handle exception
      e.printStackTrace();
      return false;
    }

    return true;
  }

  private void selectComboItem (Combo cmb, String item)
  {
    if (cmb == null || item == null)
      return;

    int index = cmb.indexOf (item);
    if (index > -1)
      cmb.select (index);
  }
}
