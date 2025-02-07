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
package com.ibm.biginsights.project.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BigInsightsMainTabNotSupported extends
		AbstractLaunchConfigurationTab {

	private String tabName;
	private String desc; 
	
	public BigInsightsMainTabNotSupported(String tabName, String desc) {
		super();
		this.tabName = tabName;
		this.desc = desc;
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));    	
    	GridData gdComp = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);		
    	comp.setLayoutData(gdComp);

		((GridLayout)comp.getLayout()).verticalSpacing = 0;

		Label error = new Label(comp, SWT.NONE);
		GridData gdLabel = new GridData(GridData.BEGINNING);
		error.setLayoutData(gdLabel);        
		error.setText(desc);

		setControl(comp);		
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// nothing to do

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		// nothing to do

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// nothing to do

	}

	@Override
	public String getName() {
		return tabName;
	}
	
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return false;
	}

}
