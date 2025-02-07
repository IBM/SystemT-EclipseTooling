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
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.BrowserHandler;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.apps.AppSelectionDialog;
import com.ibm.biginsights.project.locations.apps.BigInsightsApp;
import com.ibm.biginsights.project.locations.apps.BigInsightsAppFolder;
import com.ibm.biginsights.project.locations.apps.IBigInsightsApp;

public class ViewAppAction extends Action {
	
	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;

	public ViewAppAction(){
		// zero-arg constructor for launch from task launcher
	}
	
	public ViewAppAction(IWorkbenchPage page, ISelectionProvider selectionProvider)
	{
		super();				
		setImageDescriptor(Activator.getImageDescriptor("/icons/globe.gif")); //$NON-NLS-1$
		this.page = page;
		this.selectionProvider = selectionProvider;		
	}
	
	public String getText() {
		String result = null;
		IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
		if (structuredSelection.getFirstElement() instanceof BigInsightsAppFolder){
			result = Messages.Location_ActionViewApps;		
		}
		else
			result = Messages.Location_ActionViewApp;
		return result;
	}

	
	public void run() {		
		IBigInsightsLocation location = null;
		String appId = null;
		
		if (selectionProvider==null) {
			// launched from task launcher w/o context - show a dialog to select a location	
			AppSelectionDialog dlg = new AppSelectionDialog(Activator.getActiveWorkbenchShell(), Messages.Location_ActionViewApp, null, null, (IBigInsightsLocation)null);
			if (dlg.open()==Window.OK && dlg.getResult().length==2) {
				
				location = (IBigInsightsLocation)dlg.getResult()[0];
				IBigInsightsApp app = (IBigInsightsApp)dlg.getResult()[1];
				if (app!=null)
					appId = app.getId();
			}					
		}
		else {
			// launched from tree
		
			IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
				
			// if app folder is selected, view app opens just the app tab
			// if an app is selected, the app id is passed in as well and the app is selected within the tab
			if (structuredSelection.getFirstElement() instanceof BigInsightsAppFolder){
				BigInsightsAppFolder appFolder = (BigInsightsAppFolder)structuredSelection.getFirstElement();
				location = appFolder.getParent();		
			}
			else if (structuredSelection.getFirstElement() instanceof BigInsightsApp) {
				BigInsightsApp app = (BigInsightsApp)structuredSelection.getFirstElement();
				appId = app.getId();
				location = app.getLocation();
			}
		}
		
		if (location!=null)
			BrowserHandler.openViewAppURL(location, appId, true);
	}
	
	public boolean isEnabled() {
		boolean result = false;
	    IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
	    if (structuredSelection.size() == 1)
	    {
	    	// if element is BigInsightsAppFolder or an app itself, then enable view apps
	    	result = structuredSelection.getFirstElement() instanceof BigInsightsAppFolder ||
			 		 structuredSelection.getFirstElement() instanceof BigInsightsApp;	    
	    }
	    return result;
	}

}
