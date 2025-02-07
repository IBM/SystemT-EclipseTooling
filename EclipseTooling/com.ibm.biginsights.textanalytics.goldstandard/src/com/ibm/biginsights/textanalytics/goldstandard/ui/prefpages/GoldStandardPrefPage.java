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
package com.ibm.biginsights.textanalytics.goldstandard.ui.prefpages;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ibm.biginsights.textanalytics.goldstandard.Messages;

/**
 * Preference page for the root node of the Gold standard configuration dialog.
 * Presently, just displays a label with a generic gold standard description.
 * In future, it would have ability to create multiple gold standards. 
 * 
 *  Krishnamurthy
 *
 */
public class GoldStandardPrefPage extends PreferencePage implements IWorkbenchPreferencePage{



	@Override
	protected Control createContents(Composite parent) {
		noDefaultAndApplyButton();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		Text tfCaption = new Text(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
		tfCaption.setBackground(composite.getBackground());
		tfCaption.setText(Messages.GoldStandardPrefPage_GS_DESCRIPTION);
		GridDataFactory
			.fillDefaults()
			.align(SWT.FILL, SWT.CENTER)
			.grab(true, false)
			.hint(
					convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
					SWT.DEFAULT).applyTo(tfCaption);
		return composite;
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub
		
	}

	
}
