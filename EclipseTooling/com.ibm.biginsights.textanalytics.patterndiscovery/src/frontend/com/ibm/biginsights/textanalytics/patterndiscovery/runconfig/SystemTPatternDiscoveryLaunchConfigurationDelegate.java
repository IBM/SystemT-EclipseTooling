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
package com.ibm.biginsights.textanalytics.patterndiscovery.runconfig;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * delegate for the pd run config
 * 
 * 
 */
public class SystemTPatternDiscoveryLaunchConfigurationDelegate extends LaunchConfigurationDelegate
{



  @Override
  public void launch (ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException
  {
    final String validationMessage = validateLaunchConfig (config);
    if (validationMessage != null) {
      PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
        public void run() {
          Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
          MessageDialog.openError (activeShell, Messages.PD_DIALOG_TITLE, validationMessage);
        }
      });
      return;
    }

    PatternDiscoveryRunConfig runConfig;
    try {
      runConfig = new PatternDiscoveryRunConfig (config);
      Job systemtProfilerJob = new PatternDiscoveryJob (ErrorMessages.PATTERN_DISCOVERY_LOADING_MESSAGE,
        runConfig.getPropsContainer (), -1, -1, 10);
      systemtProfilerJob.schedule ();
    }
    catch (TextAnalyticsException e) {
      ErrorMessages.LogError (ErrorMessages.PATTERN_DISCOVERY_ERROR_GENERAL, e);
    }
  }

  private boolean validMainAqlPath (ILaunchConfiguration config) throws CoreException
  {
    String configMainAqlPath = config.getAttribute (PDConstants.PD_MAIN_AQL_PATH_PROP, "");

    String projectName = config.getAttribute (PDConstants.PD_PROJECT_NAME_PROP, "");
    String currentMainAqlPath = ProjectPreferencesUtil.getMainAqlPath (projectName);

    if ( !StringUtils.isEmpty (currentMainAqlPath) && !StringUtils.isEmpty (configMainAqlPath) && !currentMainAqlPath.equals (configMainAqlPath))
      return false;
    else
      return true;
  }

  /**
   * validate each field of the run config
   * @throws CoreException 
   */
  public String validateLaunchConfig(ILaunchConfiguration launchConfig) throws CoreException
  {
    String projectName = launchConfig.getAttribute (PDConstants.PD_PROJECT_NAME_PROP, "");

    // ============================
    // -- validate project existence --
    // ============================
    IProject project = ProjectUtils.getProject (projectName);
    if (project == null || !project.exists ())
      return Messages.PROJECT_NOT_EXIST;

    // ============================
    // -- validate main.aql path --
    // ============================
    if (!ProjectUtils.isModularProject (projectName) && !validMainAqlPath (launchConfig))
      return Messages.PROJECT_MAIN_AQL_PATH_CHANGED;

    // ============================
    // -- validate 'Group On' field --
    // ============================
    String context = launchConfig.getAttribute (Messages.GROUP_BY_FIELD_NAME_PROP, "");
    if (StringUtils.isEmpty (context))
      return ErrorMessages.PATTERN_DISCOVERY_VALIDATION_SELECT_GROUPON;

    return null;
  }

}
