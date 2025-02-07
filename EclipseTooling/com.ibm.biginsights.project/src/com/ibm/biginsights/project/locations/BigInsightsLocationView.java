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
package com.ibm.biginsights.project.locations;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;

public class BigInsightsLocationView extends CommonNavigator {

	
	public BigInsightsLocationView() {
		super();
	}

	protected IAdaptable getInitialInput()
	{ 
		//remove menu items that are not applicable
		IMenuManager viewMenu = getViewSite().getActionBars().getMenuManager();
		viewMenu.removeAll();
		
		return new BILocationContentProvider().treeRoot;
	}	

	protected ActionGroup createCommonActionGroup() {
		ActionGroup actionGroup = new LocationActionGroup();
		return actionGroup;
	}
	
	public void createPartControl(Composite parent) {	
		super.createPartControl(parent);				
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getCommonViewer().getControl(), "com.ibm.biginsights.project.help.biginsights_server_view"); //$NON-NLS-1$
	}
}
