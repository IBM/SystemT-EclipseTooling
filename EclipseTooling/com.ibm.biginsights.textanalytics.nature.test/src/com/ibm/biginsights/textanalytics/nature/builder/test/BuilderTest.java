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
package com.ibm.biginsights.textanalytics.nature.builder.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.junit.Before;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;

public class BuilderTest {

	IProject project;
	IWorkspaceRoot workspaceRoot;
	
	@Before
	public void setUp() throws Exception {
		workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
	}

	@Test
	/*
	 * This is a positive test for build.
	 * Under test data there is a project called PhoneBook with no compilation errors in aql.
	 * This test makes sure that the aog is generated correctly.
	 * Refer to the readme in test data to understand how to import it for the junit testing
	 */
	public void testGoodProjectBuild() throws Exception {
		project = workspaceRoot.getProject("PhoneBook");
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		project.build(IncrementalProjectBuilder.AUTO_BUILD, null);
		String aogPath = ProjectPreferencesUtil.getSystemTProperties(project).getAogPath();
		String mainAQLFilePath = ProjectPreferencesUtil.getSystemTProperties(project).getMainAQLFile();
		String mainAQLFileName = new File(mainAQLFilePath).getName();
		mainAQLFileName = mainAQLFileName.replaceAll(".aql", ".aog");
		File directoryAOG = new File(ProjectPreferencesUtil.getAbsolutePath(aogPath)+ File.separatorChar+mainAQLFileName);
		assertTrue(directoryAOG.length()>0);
	}

	@Test
	/*
	 * This is a negative test for build.
	 * Under test data there is a project called BadPhoneBook with some compilation errors in aql.
	 * This test makes sure that the aog is not generated
	 * Refer to the readme in test data to understand how to import it for the junit testing
	 */
	public void testBadProjectBuild() throws Exception {
		project = workspaceRoot.getProject("BadPhoneBook");
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		project.build(IncrementalProjectBuilder.AUTO_BUILD, null);
		String aogPath = ProjectPreferencesUtil.getSystemTProperties(project).getAogPath();
		String mainAQLFilePath = ProjectPreferencesUtil.getSystemTProperties(project).getMainAQLFile();
		String mainAQLFileName = new File(mainAQLFilePath).getName();
		mainAQLFileName = mainAQLFileName.replaceAll(".aql", ".aog");
		System.out.println("aogPath is " + ProjectPreferencesUtil.getAbsolutePath(aogPath)+mainAQLFileName);
		File directoryAOG = new File(ProjectPreferencesUtil.getAbsolutePath(aogPath)+ File.separatorChar+mainAQLFileName);
		assertTrue(directoryAOG.length() <= 0);
	}

	
}
