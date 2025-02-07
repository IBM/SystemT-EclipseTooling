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
package com.ibm.biginsights.textanalytics.util.common.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 *  Babbar
 *
 */

public class CustomWizardDialog extends WizardDialog{



	private IWizard wizard;
	
	public CustomWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
		this.wizard = newWizard;
	}


	@Override
	protected void createButtonsForButtonBar(Composite parent) 
	{
		super.createButtonsForButtonBar(parent);
		Button finishButton = getButton(IDialogConstants.FINISH_ID);
		Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
		if(wizard.getClass().getSimpleName().equals("ExprBuilderWizard"))
		{
			//cancelButton.setText(IDialogConstants.CLOSE_LABEL);
			cancelButton.setEnabled(true);
			finishButton.setVisible(true);
			finishButton.setEnabled(true);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Control ctrl = super.createDialogArea(parent);
		getProgressMonitor();
		return ctrl;		
	}
	
	@Override
	protected IProgressMonitor getProgressMonitor()
	{
		ProgressMonitorPart monitor = (ProgressMonitorPart)super.getProgressMonitor();
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 0;
		monitor.setLayoutData(gd);
		monitor.setVisible(false);
		return monitor;
	}
	
}
