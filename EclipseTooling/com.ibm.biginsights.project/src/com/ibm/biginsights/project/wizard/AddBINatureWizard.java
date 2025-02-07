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
package com.ibm.biginsights.project.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.ProjectSupport;

public class AddBINatureWizard extends Wizard {

	public static enum WizardMode {ADD, MIGRATE};
	
	private BigInsightsLibraryContainerPage _libPage;
	private IProject project = null;
	private WizardMode _wizardMode = WizardMode.ADD;
	private String _oldVersion;
	private String _newVersion;
	
	public AddBINatureWizard(IProject project, String oldVersion, String newVersion) {
		this._wizardMode = WizardMode.MIGRATE;
		setWindowTitle(Messages.ADDBINATUREWIZARD_MIGRATE_TITLE);
		this.project = project;
		this._oldVersion = oldVersion;
		this._newVersion = newVersion;
	}
	
	public AddBINatureWizard(IProject project) {
		this._wizardMode = WizardMode.ADD;
		setWindowTitle(Messages.ADDBINATUREWIZARD_TITLE);		
		this.project = project;
	}
	
	public void addPages() {
	    super.addPages();	
	    _libPage = new BigInsightsLibraryContainerPage();	    	    
	    addPage(_libPage);
	}

	public boolean performFinish() {

	    try {
	    	if (_wizardMode==WizardMode.ADD)
	    		ProjectSupport.addBIProjectNature(project, _libPage.getSelection());
	    	else if (_wizardMode==WizardMode.MIGRATE)
	    		ProjectSupport.migrateProject(project, _libPage.getSelection(), _oldVersion, _newVersion);
		} catch (CoreException e) {			
			MessageDialog.openError(this.getShell(), Messages.ADDBINATUREWIZARD_ERROR_TITLE, 
					(_wizardMode==WizardMode.ADD ? Messages.ADDBINATUREWIZARD_ERROR_DESC : Messages.MIGRATE_ERROR_DESC)
					+"\n"+ //$NON-NLS-1$
					e.getLocalizedMessage());			
		}
	    return true;
	}

}
