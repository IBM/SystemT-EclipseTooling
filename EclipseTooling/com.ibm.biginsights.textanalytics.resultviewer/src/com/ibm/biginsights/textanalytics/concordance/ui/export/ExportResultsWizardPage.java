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
package com.ibm.biginsights.textanalytics.concordance.ui.export;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public class ExportResultsWizardPage extends WizardPage {



	protected FileDirectoryPicker directoryPicker;
	protected Label descriptionLabel;
	protected Text fileNameText;
	protected String directory;

	public ExportResultsWizardPage(String pageName) {
		super(pageName);
		setTitle(Messages.exportResultsWizardPageTitle); 
		setDescription(Messages.ExportResultsWizardPage_Description);
		setImageDescriptor(Activator.getImageDescriptor("export_wizard.png"));
	}

	@Override
	public void createControl(Composite arg0) {
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(arg0, "com.ibm.biginsights.textanalytics.tooling.help.export_results");//$NON-NLS-1$

		Composite stepA = new Composite(arg0, SWT.NONE);
		stepA.setLayout(new GridLayout());
		stepA.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));
		stepA.setFont(arg0.getFont());

		directoryPicker = new FileDirectoryPicker(stepA,
				Constants.DIRECTORY_ONLY, FileDirectoryPicker.EXTERNAL_ONLY);
		directoryPicker.setDescriptionLabelText(Messages
				.exportResultsWizardPageDirectoryLabel); 
		directoryPicker.setEditable(true);

		Composite fieldsPanel = new Composite(stepA, SWT.NONE);
		fieldsPanel.setLayout(new GridLayout());
		fieldsPanel.setFont(stepA.getFont());
		fieldsPanel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));

		setControl(stepA);
	}

	public void apply() {
		directory = directoryPicker.getFileDirValue();
	}

	public boolean isDataValid() {

		if (directoryPicker.getFileDirValue() == "") {//$NON-NLS-1$
			setErrorMessage(Messages
					.exportResultsWizardPageErrorDirectoryBlank);
			return false;
		}

		File file = new File(directoryPicker.getFileDirValue());
		if (!file.exists()) {
			setErrorMessage(Messages
					.exportResultsWizardPageErrorDirectoryInvalid);
			return false;
		}

		return true;
	}

	public String getDirectory() {
		return directory;
	}
}
