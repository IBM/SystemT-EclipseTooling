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
import org.eclipse.ui.IWorkbenchPage;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BigInsightsConfFolder;
import com.ibm.biginsights.project.locations.LocationRegistry;

public class ConfFileRefreshAction extends Action {
	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;

	public ConfFileRefreshAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
		setText(Messages.CONFFILEREFRESHACTION_TITLE);
		setImageDescriptor(Activator.getImageDescriptor("/icons/action_refresh.gif")); //$NON-NLS-1$
		this.page = page;
		this.selectionProvider = selectionProvider;
		
	}
	
	public boolean isEnabled() {
		ISelection selection = selectionProvider.getSelection();

		boolean result = false;
	    IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	    if (structuredSelection.size() == 1)
	    {
	    	// if element is IBigInisightsLocation, then enable refresh
	    	if (structuredSelection.getFirstElement() instanceof BigInsightsConfFolder)
	    		result = true;
	    }

		return result;
	}
	
	public void run() {		
		ISelection selection = selectionProvider.getSelection();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		
		if(structuredSelection.getFirstElement() instanceof BigInsightsConfFolder){
			BigInsightsConfFolder confFolder = (BigInsightsConfFolder)structuredSelection.getFirstElement();
			// submit the request to get the new files
			confFolder.getParent().retrieveConfigurationFiles();
			//get the hive port
			confFolder.getParent().retrieveHivePort();
			// update bigsql information
			if (confFolder.getParent().isVersion2100orAbove())
				confFolder.getParent().retrieveBigSQLNodeAndPort();
			LocationRegistry.getInstance().save(confFolder.getParent());  
			// then refresh the folder
			confFolder.refresh();
		}
	}
}
