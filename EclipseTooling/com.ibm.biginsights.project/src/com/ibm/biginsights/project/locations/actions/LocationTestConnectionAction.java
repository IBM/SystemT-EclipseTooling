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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.CommonViewer;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.BigInsightsLocation.TestConnectionResult;

public class LocationTestConnectionAction extends Action {
	
	private ISelectionProvider selectionProvider;
	private IWorkbenchPage page;

	public LocationTestConnectionAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
		this.page = page;
		setText(Messages.LOCATIONTESTCONNECTIONACTION_LABEL);	
		setImageDescriptor(Activator.getImageDescriptor("/icons/connection_obj.gif")); //$NON-NLS-1$
		this.selectionProvider = selectionProvider;
		
	}
	
	public boolean isEnabled() {
		ISelection selection = selectionProvider.getSelection();

		boolean result = false;
	    IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	    if (structuredSelection.size() == 1)
	    {
	    	// if element is IBigInisightsLocation, then enable test connection
	    	if (structuredSelection.getFirstElement() instanceof IBigInsightsLocation)
	    		result = true;
	    }

		return result;
	}
	
	public void run() {		
		
		final CommonViewer viewer;
		if (selectionProvider instanceof CommonViewer)
			viewer = (CommonViewer)selectionProvider;			
		else
			viewer = null;
		
		ISelection selection = selectionProvider.getSelection();		
		final IStructuredSelection structuredSelection = (IStructuredSelection) selection;

		final Job testConnectionThread = new Job(Messages.LOCATIONTESTCONNECTIONACTION_JOB_NAME) {
			public IStatus run(IProgressMonitor monitor) {
				IStatus status;
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (viewer!=null)
							viewer.refresh();
					}});
				// execute test connection
				final TestConnectionResult result = ((IBigInsightsLocation)structuredSelection.getFirstElement()).testConnection(true);
				
				// show the message dialog
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (viewer!=null)
							viewer.refresh();
						if (result!=null) {
							if (result.success) {
								MessageDialog.openInformation(page.getActivePart().getSite().getShell(), Messages.LOCATIONTESTCONNECTIONACTION_SUCCESS_TITLE, Messages.LOCATIONTESTCONNECTIONACTION_SUCCESS_DESC);			
							}
							else {
								MessageDialog.openError(page.getActivePart().getSite().getShell(), Messages.LOCATIONTESTCONNECTIONACTION_ERROR_TITLE, 
									Messages.LOCATIONTESTCONNECTIONACTION_ERROR_DESC+"\n"+ //$NON-NLS-1$
									(result.statusCode>-1 ? (result.statusCode+": "+result.statusMessage) : result.statusMessage)); //$NON-NLS-1$
							}
						}
					}
				});
				
				return Status.OK_STATUS;
			}
		};
		
		testConnectionThread.schedule();
		
	}	
	
}
