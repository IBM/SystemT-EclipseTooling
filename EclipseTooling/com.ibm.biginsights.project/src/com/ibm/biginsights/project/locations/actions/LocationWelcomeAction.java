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
import com.ibm.biginsights.project.locations.LocationSelectionDialog;

public class LocationWelcomeAction extends Action {
	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;

	public LocationWelcomeAction(){
		// zero-arg constructor for launch from task launcher
	}
	
	public LocationWelcomeAction(IWorkbenchPage page, ISelectionProvider selectionProvider)
	{
		super();				
		setImageDescriptor(Activator.getImageDescriptor("/icons/globe.gif")); //$NON-NLS-1$
		this.page = page;
		this.selectionProvider = selectionProvider;		
	}
	
	public String getText() {
		return Messages.LOCATIONWELCOMEACTION_NAME;
	}

	
	public void run() {		
		IBigInsightsLocation location = null;
		
		if (selectionProvider==null) {
			// launched from task launcher w/o context - show a dialog to select a location	
			LocationSelectionDialog dlg = new LocationSelectionDialog(Activator.getActiveWorkbenchShell(), Messages.LOCATIONWELCOMEACTION_NAME, 
					Messages.LOCATIONWELCOMEACTION_DESC, null);
			if (dlg.open()==Window.OK && dlg.getResult().length>0) {
				location = (IBigInsightsLocation)dlg.getResult()[0];				
			}					
		}
		else {
			// launched from tree
			IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
			location = (IBigInsightsLocation)structuredSelection.getFirstElement();
		}
		
		if (location!=null) 
			BrowserHandler.openWebConsole(location);				
	}
	
	public boolean isEnabled() {
		boolean result = false;
	    IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
	    if (structuredSelection.size() == 1)
	    {
	    	// if element is IBigInsightsLocation
	    	result = structuredSelection.getFirstElement() instanceof IBigInsightsLocation; 
	    }
	    return result;
	}
}
