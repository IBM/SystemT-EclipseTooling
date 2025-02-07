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

import org.eclipse.jface.wizard.Wizard;

import com.ibm.biginsights.textanalytics.refinement.command.RefinerContainer;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public class RefinerWizard extends Wizard {


	
	RefinerWizardPage page;
	RefinerContainer container;

    public RefinerWizard(RefinerContainer container) {
		this.container = container;
		this.setWindowTitle("AQL Refinement Wizard");
	}

	public void addPages() {
             page = new RefinerWizardPage(container.getProperty(Constants.REFINER_VIEW_NAME_PROP));
             page.setRefinerContainer(container);
             addPage(page);
    }

	@Override
	public boolean performFinish() {
		page.applySettings();
		return true;
	}

}
