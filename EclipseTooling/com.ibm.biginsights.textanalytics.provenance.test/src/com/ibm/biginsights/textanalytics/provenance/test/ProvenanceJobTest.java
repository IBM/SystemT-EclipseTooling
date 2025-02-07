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
package com.ibm.biginsights.textanalytics.provenance.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.junit.Before;
import org.junit.Test;

import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.provenance.run.ProvenanceJob;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;

public class ProvenanceJobTest {

	ProvenanceJob job;
	
	@Before
	public void setUp() throws Exception {
		IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject("PhoneBook");

		SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties(project);
		
		String inputDocName = "1.txt";
		String inputDocText = "Blah";
		String viewName = "PersonSimple";
		HashMap<String, String> fieldNameValuePairs = new HashMap<String, String>(); 
		fieldNameValuePairs.put ("label", "1.txt");
    fieldNameValuePairs.put ("text", "I wonder what this big red button does?");
		String aogPath = ProjectPreferencesUtil.getAbsolutePath(properties.getAogPath());
		
		ProvenanceRunParams params = new ProvenanceRunParams(new String[]{"genericModule"}, aogPath, LangCode.en, null, null, true);
		System.err.println(aogPath);
	
		job = new ProvenanceJob("Provenance job", inputDocName, inputDocText, fieldNameValuePairs, viewName, project.getName(), params);
	}

	@Test
	public void testRunIProgressMonitor() {
		IStatus status = job.run(new NullProgressMonitor());
		assertEquals(Status.OK_STATUS, status);
	}

}
