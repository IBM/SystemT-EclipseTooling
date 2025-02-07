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

package com.ibm.biginsights.textanalytics.resultviewer.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

import au.com.bytecode.opencsv.CSVReader;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.ui.export.GenerateHtmlCsv;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.resultviewer.utils.AnnotationExplorerUtil;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;

public class GenerateHtmlCsvTest {

	private IFolder actualExportResults;
	private IFolder expectedExportResults;
	private String resultDirPath;
	private IFolder resFolder;
	private List<IFile> resFileList;
	final String ENCODING = "UTF-8";
	@Test
	public void testExport() throws Exception {
		//Test Person Phone AQL.
		test("resultPersonPhoneSimple-02-09-2012-174203", "PersonPhoneSimple");
	}
	
	@Test
	public void test1Export() throws Exception {
		//Test where the View conatins List and Span. Here if a View contains only List then no Highlighting is done on HTML.
		//If a View contains List and Spans then Last Span is highlighted in HTML.
		test("resultViewWithListAndSpan-02-13-2012-143413", "ViewWithListAndSpan");
	}
	
	@Test
	public void test2Export() throws Exception {
		//Test for Detaged Documents.
		test("resultDetag-02-13-2012-145126", "Detag");
	}



	private void test(String dataFolder, String outputFolder) throws Exception {
		resultDirPath = ProjectPreferencesUtil.getAbsolutePath("ExportResults"
				+ File.separator + "data");
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject("ExportResults");

		// Path for strf files
		resFolder = project
		.getWorkspace()
		.getRoot()
		.getFolder(
				new Path(resultDirPath + File.separator
						+ dataFolder));
		final IResource[] resources = resFolder.members();
		resFileList = new ArrayList<IFile>(resources.length);
		for (IResource resource : resources) {
			if ((resource.getType() == IResource.FILE)
					&& resource.getName().endsWith(".strf")) { //$NON-NLS-1$
				resFileList.add((IFile) resource);
			}
		}

		actualExportResults = project
		.getWorkspace()
		.getRoot()
		.getFolder(
				new Path(File.separator + "ExportResults"
						+ File.separator + "actual" + File.separator
						+ outputFolder));
		String actualResultPath = ProjectPreferencesUtil.getAbsolutePath("[W]"
				+ actualExportResults.getFullPath().toOSString());

		expectedExportResults = project
		.getWorkspace()
		.getRoot()
		.getFolder(
				new Path(File.separator + "ExportResults"
						+ File.separator + "expected" + File.separator
						+ outputFolder));
		String expectedResultPath = ProjectPreferencesUtil
		.getAbsolutePath("[W]"
				+ expectedExportResults.getFullPath().toOSString());

		//TODO - Need to remove it as we fix the test cases. Export do not use the 
		// Provenance. Do not need to pass the same.
		final ProvenanceRunParams provenanceRunParams = new ProvenanceRunParams(null, 
				null, null, null, null, true);

		IConcordanceModel concModel = AnnotationExplorerUtil.generateConcordanceModelFromFiles (resFileList, null,
				provenanceRunParams, new NullProgressMonitor());
		GenerateHtmlCsv generateHtmlCsv = new GenerateHtmlCsv(actualResultPath);
		generateHtmlCsv.generateForAllViews(concModel);

		// Processing CSV Files
		File expectedCSV = new File(expectedResultPath + File.separator + "csv");
		File actualCSV = new File(actualResultPath + File.separator + "csv");
		String[] expectedCSVFiles = expectedCSV.list();

		Assert.assertEquals(expectedCSVFiles.length, actualCSV.list().length);

		for (int i = 0; i < expectedCSVFiles.length; i++) {

			Assert.assertTrue(compareCSVFiles(expectedCSV + File.separator
					+ expectedCSVFiles[i], actualCSV + File.separator
					+ expectedCSVFiles[i]));

		}

		// Processing HTML Files
		File expectedHTML = new File(expectedResultPath + File.separator
				+ "html");
		File actualHTML = new File(actualResultPath + File.separator + "html");
		String[] expectedHTMLFiles = expectedHTML.list();

		Assert.assertEquals(expectedHTMLFiles.length, actualHTML.list().length);

		for (int i = 0; i < expectedHTMLFiles.length; i++) {
			Assert.assertTrue(compareHTMLFiles(expectedHTML + File.separator
					+ expectedHTMLFiles[i], actualHTML + File.separator
					+ expectedHTMLFiles[i]));
		}

	}

	private boolean compareCSVFiles(String expected, String actual)
	throws IOException {
		BufferedReader expectedReader = new BufferedReader(
				new InputStreamReader(new FileInputStream(expected), ENCODING));
		BufferedReader actualReader = new BufferedReader(new InputStreamReader(
				new FileInputStream(actual), ENCODING));

		CSVReader expected_r = new CSVReader(expectedReader);
		CSVReader actual_r = new CSVReader(actualReader);

		String[] exp_row, act_row;

		exp_row = expected_r.readNext();
		act_row = actual_r.readNext();

		// == compare the values at the specified indices of each row ==
		while (exp_row != null && act_row != null) {
			for (int i = 0; i < act_row.length; i++) {
				if (!exp_row[i].equals(act_row[i]))
					return false;
			}
			exp_row = expected_r.readNext();
			act_row = actual_r.readNext();
		}

		// == check that both files have the same number of rows ==
		if (exp_row != null | act_row != null)
			return false;

		expected_r.close();
		actual_r.close();

		expectedReader.close();
		actualReader.close();

		return true;
	}

	private boolean compareHTMLFiles(String expected, String actual)
	throws IOException {
		String expectedContent = readFile(expected);
		String actualContent = readFile(actual);
		if (actualContent != null && expectedContent.equals(actualContent))
			return true;

		return false;
	}

	private String readFile(String fileName) throws IOException {

		FileInputStream fileInputStream = new FileInputStream(fileName);
		DataInputStream dataInputStream = new DataInputStream(fileInputStream);
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(dataInputStream));
		String line, contents = "";
		while ((line = bufferedReader.readLine()) != null) {
			contents += line;
		}
		dataInputStream.close();
		return contents;
	}

}
