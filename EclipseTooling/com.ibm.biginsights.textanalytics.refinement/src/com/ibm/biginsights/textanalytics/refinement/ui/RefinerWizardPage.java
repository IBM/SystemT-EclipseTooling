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
package com.ibm.biginsights.textanalytics.refinement.ui;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.biginsights.textanalytics.refinement.command.RefinerContainer;
import com.ibm.biginsights.textanalytics.refinement.messages.Messages;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ui.ProjectBrowser;

public class RefinerWizardPage extends WizardPage {



	RefinerContainer container;
	
	// TODO: figure out the right way to get at the active project
	ProjectBrowser projectBrowser;
	protected FileDirectoryPicker configPicker;
	protected FileDirectoryPicker dataDirPicker;
	protected FileDirectoryPicker labelDirPicker;

	protected RefinerWizardPage(String viewName) {
		super("");
		setTitle("AQL Refinement Properties");
		setDescription("Refining view " + viewName);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		setControl(composite);
		
		projectBrowser = new ProjectBrowser(composite, SWT.NONE);
		
		configPicker = new FileDirectoryPicker(composite, 
				Constants.FILE_ONLY,
				FileDirectoryPicker.WORKSPACE_OR_EXTERNAL);
		configPicker.setDescriptionLabelText(Messages
				.getString("RefinerWizardPage.CONFIG_PICKER_LABEL"));
		configPicker.setAllowedFileExtensions("properties");

		dataDirPicker = new FileDirectoryPicker(composite, 
				Constants.DIRECTORY_ONLY,
				FileDirectoryPicker.WORKSPACE_OR_EXTERNAL);
		
		dataDirPicker.setDescriptionLabelText(Messages
				.getString("RefinerWizardPage.DATA_DIR_PICKER_LABEL"));
		
		labelDirPicker = new FileDirectoryPicker(composite, 
				Constants.DIRECTORY_ONLY,
				FileDirectoryPicker.WORKSPACE_OR_EXTERNAL);

		labelDirPicker.setDescriptionLabelText(Messages
				.getString("RefinerWizardPage.LABEL_DIR_PICKER_LABEL"));
	}

	public void setRefinerContainer(RefinerContainer container) {
		this.container = container;
	}

	
	/**
	 * Remember the user's selections.
	 * 
	 */
	public void applySettings() {
		container.setProperty(Constants.REFINER_CONFIG_PATH_PROP, configPicker.getFileDirValue());
		container.setProperty(Constants.REFINER_DATA_PATH_PROP, dataDirPicker.getFileDirValue());
		container.setProperty(Constants.REFINER_LABEL_PATH_PROP, labelDirPicker.getFileDirValue());
		container.setProperty(Constants.REFINER_PROJECT_NAME_PROP, projectBrowser.getProject());
	}

}
