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

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BigInsightsLocationRoot;
import com.ibm.biginsights.project.locations.wizard.LocationWizard;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;

public class LocationNewAction extends Action {

	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;
	
	public LocationNewAction() {
		// zero-arg constructor for launch from task launcher
	}
	
	public LocationNewAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
		setText(Messages.LOCATIONNEWACTION_TITLE);
		setImageDescriptor(Activator.getImageDescriptor("/icons/new_server.gif")); //$NON-NLS-1$
		this.page = page;
		this.selectionProvider = selectionProvider;
		
	}
	
	public boolean isEnabled() {
		if(getId().equals(BIConstants.LOCATIONS_NEWBUTTONID)){
			return true;
		}
		ISelection selection = selectionProvider.getSelection();		
	    IStructuredSelection structuredSelection = (IStructuredSelection) selection;	    
	    Iterator itSel = structuredSelection.iterator();
	    boolean hasRootSelected = false;
	    while (itSel.hasNext())
	    {
	    	Object selectedItem = itSel.next();
	    	if (selectedItem instanceof BigInsightsLocationRoot)
	    	{
	    		hasRootSelected = true;
	    		break;
	    	}
	    }
	    	    
	    // only enable New if one item is selected and it is the root
	    return (hasRootSelected && structuredSelection.size()==1);
	}
	
	public void run() {				
	    Shell shell = Activator.getActiveWorkbenchShell();
	    
	    // if the user switches to BI perspective, then also make sure the BigInsights server view is open
	    if (BIProjectPreferencesUtil.switchToBigInsightsPerspective(shell, Messages.LOCATIONWIZARD_SWITCH_PERSPECTIVE_DESC)) {
		    try {
				Activator.getActiveWorkbenchWindow().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(BIConstants.LOCATIONS_VIEW_ID);
			} catch (Exception e) {
				// if any of the pages are null, don't open it			
			}
	    }	    
	    
	    LocationWizard wizard = new LocationWizard();	    
	    WizardDialog dialog = new WizardDialog(shell, wizard);
	    dialog.create();
	    dialog.open();
	}
}
