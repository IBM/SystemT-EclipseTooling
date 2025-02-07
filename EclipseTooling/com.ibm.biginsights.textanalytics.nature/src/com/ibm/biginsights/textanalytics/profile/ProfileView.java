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
package com.ibm.biginsights.textanalytics.profile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class ProfileView extends ViewPart {


	
	public static final String VIEW_ID = "com.ibm.biginsights.textanalytics.profile.view";
	
	protected Text tfProfileView;

	public ProfileView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		tfProfileView = new Text(parent, SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
		tfProfileView.setFont(new Font(tfProfileView.getDisplay(), new FontData("Courier", 10, SWT.NORMAL)));
		tfProfileView.setBackground(new Color(tfProfileView.getDisplay(), 255, 255, 255));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.profiler_view");//$NON-NLS-1$
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	public void setTextContent(String content){
		tfProfileView.setText(content);
	}
	
	public void setViewName(String name){
		setPartName(name);
	}

}
