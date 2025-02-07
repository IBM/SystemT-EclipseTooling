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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.wizard.LocationWizard;

public class LocationUpdateAction extends Action {

	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;

	public LocationUpdateAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
		setText(Messages.LOCATIONUPDATEACTION_LABEL);
		setImageDescriptor(Activator.getImageDescriptor("/icons/update.gif")); //$NON-NLS-1$
		this.page = page;
		this.selectionProvider = selectionProvider;
		
	}
	
	public boolean isEnabled() {
		ISelection selection = selectionProvider.getSelection();

		boolean result = false;
	    IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	    if (structuredSelection.size() == 1)
	    {
	    	// if element is IBigInisightsLocation, then enable update
	    	if (structuredSelection.getFirstElement() instanceof IBigInsightsLocation)
	    		result = true;
	    }

		return result;
	}
	
	public void run() {		
		ISelection selection = selectionProvider.getSelection();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		
		
	    Shell shell = page.getActivePart().getSite().getShell();
	    LocationWizard wizard = structuredSelection.getFirstElement()!=null ?
	    	new LocationWizard((IBigInsightsLocation)structuredSelection.getFirstElement()) :
	    	new LocationWizard();
	    WizardDialog dialog = new WizardDialog(shell, wizard);
	    dialog.create();
	    dialog.open();
	}
}
