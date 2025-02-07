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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import com.ibm.biginsights.project.Activator;

public class ProjectCreationPage extends WizardNewProjectCreationPage {

	public ProjectCreationPage(String pageName) {
		super(pageName);
		this.setImageDescriptor(Activator.getImageDescriptor("/icons/wiz_bigInsight.gif")); //$NON-NLS-1$
	}
	
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), "com.ibm.biginsights.project.help.create_ bigi_proj"); //$NON-NLS-1$
	}
	
	
}
