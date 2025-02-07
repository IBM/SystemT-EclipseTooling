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
package com.ibm.biginsights.project.locations.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.apps.BigInsightsAppFolder;

public class AppsRefreshAction extends Action {

	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;
	private ICommonViewerWorkbenchSite commonViewerSite;
	
	public AppsRefreshAction(IWorkbenchPage page, ISelectionProvider selectionProvider, ICommonViewerWorkbenchSite commonViewerSite) {
		setText(Messages.Location_ActionRefesh);
		setImageDescriptor(Activator.getImageDescriptor("/icons/action_refresh.gif")); //$NON-NLS-1$
		this.page = page;
		this.selectionProvider = selectionProvider;
		this.commonViewerSite = commonViewerSite;
	}
	
	public boolean isEnabled() {
		ISelection selection = selectionProvider.getSelection();

		boolean result = false;
	    IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	    if (structuredSelection.size() == 1)
	    {
	    	// if element is IBigInisightsLocation, then enable refresh
	    	if (structuredSelection.getFirstElement() instanceof BigInsightsAppFolder)
	    		result = true;
	    }

		return result;
	}
	
	public void run() {		
		ISelection selection = selectionProvider.getSelection();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		
		if(structuredSelection.getFirstElement() instanceof BigInsightsAppFolder){
			BigInsightsAppFolder appFolder = (BigInsightsAppFolder)structuredSelection.getFirstElement();
			appFolder.refresh();
		}
		
		//remove menu items that are not applicable
		IMenuManager viewMenu = commonViewerSite.getActionBars().getMenuManager();
		viewMenu.removeAll();
	}
}
