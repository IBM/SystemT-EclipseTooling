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
package com.ibm.biginsights.textanalytics.goldstandard.ui;

import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.ui.prefpages.GSAnnotationTypesPage;
import com.ibm.biginsights.textanalytics.goldstandard.ui.prefpages.GSGeneralPage;
import com.ibm.biginsights.textanalytics.goldstandard.ui.prefpages.GoldStandardPrefPage;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * UI for Gold standard configuration dialog
 * 
 *
 */
public class ConfigurationDialog extends PreferenceDialog {


	
	protected PreferenceNode root;

	public ConfigurationDialog(Shell parentShell){
		this(parentShell, null);
	}

	public ConfigurationDialog(Shell parentShell, IFolder gsFolder) {
		super(parentShell, new PreferenceManager());
	
		if(gsFolder == null){
			//gsFolder is null. So, detect the selection from UI and show config dialog accordingly
			ISelection selection = ProjectUtils.getSelection();
			if(selection != null && selection instanceof IStructuredSelection){
				IStructuredSelection structSelection = (IStructuredSelection) selection;
				Object element = structSelection.getFirstElement();
				if(element instanceof IFolder){
					IFolder selectedFolder = (IFolder) element;
					if(GoldStandardUtil.isGoldStandardParentDir(selectedFolder)){
						IFolder gsParentFolder = selectedFolder;
						try {
							loadGSConfigPages(gsParentFolder);
						} catch (CoreException e) {
							LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
						}
					}else if(GoldStandardUtil.isGoldStandardFolder(selectedFolder)){
						createGSConfigPage(selectedFolder);
					}
				}
			}
		}else{
			//show gsConfig page for selected gold standard folder
			createGSConfigPage(gsFolder);
		}
	}

	protected void loadGSConfigPages(IFolder gsParentFolder) throws CoreException{
		IResource[] members = gsParentFolder.members();
		ArrayList<IFolder> gsFolders = new ArrayList<IFolder>();
		for (IResource member : members) {
			if(member instanceof IFolder){
				IResource[] dirContents =((IFolder)member).members();
				
				//Look for atleast one gs file within the folder to treat it as a GS folder
				for (IResource dirContent : dirContents) {
					if(dirContent.getName().endsWith(Constants.GS_FILE_EXTENSION_WITH_DOT)){
						gsFolders.add((IFolder) member);
						break;//break from iterating over current directory
					}
				}
			}
		}
		
		for (IFolder gsFolder : gsFolders) {
			createGSConfigPage(gsFolder);
		}
	}
	
	protected void createGSConfigPage(IFolder gsFolder){
		String gsFolderName = gsFolder.getName();
		PreferenceNode gsNode = new PreferenceNode(gsFolderName); //$NON-NLS-1$
		GoldStandardPrefPage gsPrefPage = new GoldStandardPrefPage();
		gsPrefPage.setTitle(gsFolderName);
		gsNode.setPage(gsPrefPage);
		getPreferenceManager().addToRoot(gsNode);
		populateGSConfigChildNodes(gsNode, gsFolder);
	}
	
	protected void populateGSConfigChildNodes(PreferenceNode parent, IFolder gsFolder){
		//add Annotation types page
		PreferenceNode annTypesPrefNode = new PreferenceNode("annotationTypes"); //$NON-NLS-1$
		GSAnnotationTypesPage annTypesPage = new GSAnnotationTypesPage(gsFolder.getProject(), gsFolder);
		annTypesPage.setTitle(Messages.ConfigurationDialog_ANNOTATION_TYPES);
		annTypesPrefNode.setPage(annTypesPage);
		getPreferenceManager().addTo(parent.getId(), annTypesPrefNode);
		
		//add General page
		PreferenceNode generalPrefNode = new PreferenceNode("general"); //$NON-NLS-1$
		GSGeneralPage genPage = new GSGeneralPage(gsFolder.getProject(), gsFolder);
		genPage.setTitle(Messages.ConfigurationDialog_GENERAL);
		generalPrefNode.setPage(genPage);
		getPreferenceManager().addTo(parent.getId(), generalPrefNode);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(control, "com.ibm.biginsights.textanalytics.tooling.help.configuring_labeled_collection");
		return control;
	}
	
	

}
