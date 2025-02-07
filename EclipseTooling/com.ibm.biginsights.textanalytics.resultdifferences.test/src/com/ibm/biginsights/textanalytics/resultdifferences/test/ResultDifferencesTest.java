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
package com.ibm.biginsights.textanalytics.resultdifferences.test;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.compute.DifferencesComputer;
public class ResultDifferencesTest {

	private IFolder expectedFolder;
  private IFolder actualFolder;
	private String resultDirPath;
	
  private void  getResultDirPath() {
    resultDirPath=ProjectPreferencesUtil.getDefaultResultDir("PhoneBook");
    System.out.println("resultDirPath is " + resultDirPath);
    resultDirPath=resultDirPath.substring(3); // remove the [W] prefix
  }


 	@Test
	public void testResultDifference() throws Exception
	{
 	  getResultDirPath();
    IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
    IProject project = workspaceRoot.getProject("PhoneBook");
      actualFolder = project.getWorkspace().getRoot().getFolder(new Path("\\PhoneBook\\result\\result-removedCindy-addedEric"));
      System.out.println ("actualFolder name is " + actualFolder.getName());
      expectedFolder= project.getWorkspace().getRoot().getFolder(new Path("\\PhoneBook\\result\\result-noEric-no-Jose"));
      System.out.println ("expectedFolder name is " + expectedFolder.getName ());

      DifferencesComputer computer = DifferencesComputer.getInstance(expectedFolder,actualFolder,true);
      IFile expectedFile =expectedFolder.getFile ("%5C10045.strf");
      System.out.println("ResultDifferencesTest expectedFile" + expectedFile);
      IFile actualFile =actualFolder.getFile ("%5C10045.strf");

      System.out.println("ResultDifferencesTest actualFile" + actualFile);
      String[] diffTypes = computer.getDiffTypes ();
      for (int j=0;j<diffTypes.length;j++)
      {
        System.out.println("diffTypes" + j + ":"+diffTypes[j]);
      }
      assertTrue(diffTypes.length == 3);

      String viewNameDotFieldName = "PersonSimple.person";
      int missingCount = computer.getDeletedAnnotationsCountInExpected (expectedFile, actualFile, viewNameDotFieldName);
      System.out.println("Missing Count is " + missingCount);
      assertTrue(missingCount == 2);

      expectedFile =expectedFolder.getFile ("%5C1006.strf");
      actualFile =actualFolder.getFile ("%5C1006.strf");
      
      int newCount = computer.getNewAnnotationsCountInActual (expectedFile, actualFile, viewNameDotFieldName);
      System.out.println("New Count is " + newCount);

      assertTrue(newCount == 6);
	}
	

}
