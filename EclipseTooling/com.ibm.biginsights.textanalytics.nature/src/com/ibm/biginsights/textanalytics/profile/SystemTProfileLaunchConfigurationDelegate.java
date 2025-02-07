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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.run.SystemTRunConfig;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class SystemTProfileLaunchConfigurationDelegate extends
		LaunchConfigurationDelegate {



	@Override
	public void launch(ILaunchConfiguration config, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String projectName = config.getAttribute(
				IRunConfigConstants.PROJECT_NAME, ""); //$NON-NLS-1$

		int minSeconds = config.getAttribute(
				IRunConfigConstants.MIN_SECONDS_TO_RUN, 60);

		SystemTRunConfig runConfig = ProjectPreferencesUtil.createRunConfiguration(ProjectUtils.getProject(projectName), config);
		
		Job systemtProfilerJob = new SystemTProfileJob(
				projectName,
				Messages.getString("SystemTProfileLaunchConfigurationDelegate.INFO_RUNNING_PROFILER_ON_PROJECT") + projectName, runConfig, minSeconds); //$NON-NLS-1$
		systemtProfilerJob.setUser(true);
		systemtProfilerJob.schedule();
	}
}
