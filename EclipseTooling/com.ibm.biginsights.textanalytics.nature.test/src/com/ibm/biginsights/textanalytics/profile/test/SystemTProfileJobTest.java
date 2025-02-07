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
package com.ibm.biginsights.textanalytics.profile.test;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.junit.Before;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.run.SystemTRunConfig;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.profile.SystemTProfileJob;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public class SystemTProfileJobTest {

	SystemTProfileJob job;
	
	@Before
	public void setUp() throws Exception {
		IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject("PhoneBook");
		
		String inputCollection = "[W]/PhoneBook/input";
		SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties(project);
		SystemTRunConfig profileConfig = new SystemTRunConfig("en", inputCollection, "", properties);
		profileConfig.setTokenizerChoice(Constants.TOKENIZER_CHOICE_WHITESPACE, "", "");
		job = new SystemTProfileJob(project.getName(), "Job name", profileConfig, 60);
	}

	@Test
	public void testRunIProgressMonitor() {
		IStatus status = job.run(new NullProgressMonitor());
		assertEquals(Status.OK_STATUS, status);
	}

}
