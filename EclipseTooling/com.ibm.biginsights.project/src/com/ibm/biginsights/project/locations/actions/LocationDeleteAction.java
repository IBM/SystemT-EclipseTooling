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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BigInsightsLocationRoot;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;

public class LocationDeleteAction extends Action {

	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;

	public LocationDeleteAction(IWorkbenchPage page, ISelectionProvider selectionProvider)
	{
		super();		
		setText(Messages.LOCATIONDELETEACTION_DESC);
		setImageDescriptor(Activator.getImageDescriptor("/icons/action_delete.gif")); //$NON-NLS-1$
		this.page = page;
		this.selectionProvider = selectionProvider;
		
	}
	
	public void run() {		
		IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
		String messageText = structuredSelection.size()>1 ? 
						Messages.LOCATIONDELETEACTION_CONFIRM_SERVERS :
						Messages.LOCATIONDELETEACTION_CONFIRM_SERVER+
							((IBigInsightsLocation)structuredSelection.getFirstElement()).getLocationDisplayString();
	    if (MessageDialog.openConfirm(null, Messages.LOCATIONDELETEACTION_DELETE_TITLE, messageText)) {
	    	Iterator<IBigInsightsLocation> itSelection = structuredSelection.iterator();
	    	while (itSelection.hasNext())
	    	{
	    		LocationRegistry.getInstance().removeLocation(itSelection.next());
	    	}
	    	
        }

	}
	
	public boolean isEnabled() {
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
	    
	    // only enable delete if the root is not selected also
		return (!hasRootSelected && structuredSelection.size()>0);
	}
}
