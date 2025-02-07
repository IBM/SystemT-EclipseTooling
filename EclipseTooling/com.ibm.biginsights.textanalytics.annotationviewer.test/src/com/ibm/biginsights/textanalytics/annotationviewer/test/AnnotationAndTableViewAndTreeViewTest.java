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
package com.ibm.biginsights.textanalytics.annotationviewer.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferenceStore;
import org.junit.Test;

import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProjectPreferences;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.resultviewer.utils.AnnotationExplorerUtil;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.tableview.model.IAQLTableViewModel;
import com.ibm.biginsights.textanalytics.treeview.control.ResultEditorListener;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeParent;
import com.ibm.biginsights.textanalytics.util.common.Constants;
public class AnnotationAndTableViewAndTreeViewTest {
	
	private String aogPath; 
	private boolean provenance ;
	private LangCode lang;
	private IFolder resFolder;
	private String resultDirPath;
	private ProvenanceRunParams provenanceRunParams;
	private List<IFile> resFileList;
	private String tempDir;

	private void  getProvenanceParams() {
		SystemTProjectPreferences preferences = new SystemTProjectPreferences(Constants.CONSUMER_PROPERTY_SHEET, "PhoneBook");
		PreferenceStore pref = preferences.getPreferenceStoreCopy();
		resultDirPath=ProjectPreferencesUtil.getDefaultResultDir("PhoneBook");
		System.out.println("resultDirPath is " + resultDirPath);
		resultDirPath=resultDirPath.substring(3); // remove the [W] prefix
		aogPath = ProjectPreferencesUtil.getAbsolutePath(ProjectPreferencesUtil.getDefaultAOGPath("PhoneBook"));
		provenance = pref.getBoolean(Constants.GENERAL_PROVENANCE);
	      try {
	        lang = LangCode.strToLangCode(pref.getString(Constants.GENERAL_LANGUAGE));
	      } catch (final IllegalArgumentException e) {
	    	  e.printStackTrace();
	      }
	     provenanceRunParams = new ProvenanceRunParams(null, aogPath, lang, null, null, provenance);
	}

	
	 private void getResultFiles() throws CoreException {
			IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
			IProject project = workspaceRoot.getProject("PhoneBook");
		    resFolder = project.getWorkspace().getRoot().getFolder(new Path(resultDirPath + File.separator+ "result10-03-2011-201145"));
		    final IResource[] resources = resFolder.members();
		    resFileList = new ArrayList<IFile>(resources.length);
		    for (IResource resource : resources) {
		      if ((resource.getType() == IResource.FILE) && resource.getName().endsWith(".strf")) { //$NON-NLS-1$
		        resFileList.add((IFile) resource);
		      }
		    }
          System.out.println("resFiles are " + resFileList);
          final IFolder tmpFolder = resFolder.getFolder(Constants.TEMP_TEXT_DIR_NAME);
          if (tmpFolder.exists()) {
            tmpFolder.delete(true, false, new NullProgressMonitor());
          }
          tempDir = tmpFolder.getFullPath().toString();
		  }

 	@Test
	public void testAnnotationExplorerAndTableModelAndTreeModel() throws Exception
	{
 		  getProvenanceParams();
          getResultFiles();
          IConcordanceModel concModel = AnnotationExplorerUtil.generateConcordanceModelFromFiles (resFileList, tempDir,provenanceRunParams, new NullProgressMonitor());

          // Validating the annotation explorer view model
          IConcordanceModelEntry[] entries = concModel.getEntries();
          System.out.println("Number of Entries in AnnotationExplorerView Model :" + entries.length);
          assertTrue(entries.length > 0);

          // Validating the table view model
          String[] outputViewNames = concModel.getOutputViewNames();
          for(int j=0;j<outputViewNames.length;j++)
          {
              IAQLTableViewModel model= concModel.getViewModel(outputViewNames[j]);
              System.out.println("Number of Rows in TableView Model:" + outputViewNames[j]+  " is "+ model.getNumRows());
          }
          assertTrue(outputViewNames.length > 0);
          
          //Validating the tree view model
          TreeParent treeParent = ResultEditorListener.modelTreeForView(entries[0].getModel(),0);
          // The second parameter passed is the value from the strf file. But since we are building the object in unit test and not from
          // a file, we are passing a dummy value of 0
          ITreeObject[] annotNode = treeParent.getChildren();
          System.out.println("Annotations Node:" + ((TreeParent)annotNode[0]).getName());
          ITreeObject[] outputViewNodes = ((TreeParent)annotNode[0]).getChildren();
          System.out.println("OutputView Node" + ((TreeParent)outputViewNodes[0]).getName());
          ITreeObject[] attributeNode = ((TreeParent)outputViewNodes[0]).getChildren();
          System.out.println("attributeNode" + ((TreeParent)attributeNode[0]).getName());
          ITreeObject[] leafNodes = ((TreeParent)attributeNode[0]).getChildren();
          for(int j=0;j<leafNodes.length;j++)
          {
        	  System.out.println("leafnode "+ j + " is:" + leafNodes[j].getText());
          }
          assertTrue(leafNodes.length > 0);

	}
	

}
