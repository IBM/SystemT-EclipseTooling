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
import com.ibm.biginsights.project.BigInsightsLibraryContainerInitializer;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.launch.HBaseLaunchDelegate;
import com.ibm.biginsights.project.locations.BigInsightsLocation.FILESYSTEM;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;

public class LocationHBaseShellAction extends Action {
	private ISelectionProvider selectionProvider;

	public LocationHBaseShellAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
		setText(Messages.LOCATIONHBASESHELLACTION_TITLE);
		setImageDescriptor(Activator.getImageDescriptor("/icons/JaqlShell.gif")); //$NON-NLS-1$
		this.selectionProvider = selectionProvider;
		
	}
	
	public boolean isEnabled() {
		ISelection selection = selectionProvider.getSelection();

		boolean result = false;
	    IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	    if (structuredSelection.size() == 1)
	    {
	    	// if element is IBigInisightsLocation, then enable hbaseShell unless the location is on GPFS or the GPFS cluster is mounted
	    	if (structuredSelection.getFirstElement() instanceof IBigInsightsLocation) {
	    		IBigInsightsLocation loc = (IBigInsightsLocation)structuredSelection.getFirstElement();
	    		// also check if we have the right plugins installed, i.e. you can only connect to a V1.3GA driver if you also have the plugins from GA
	    		String supportedContainerVersion = BigInsightsLibraryContainerInitializer.getInstance().mapVersionToContainerVersion(loc.getVersionWithVendor());	    		
	    		result = supportedContainerVersion!=null && (!FILESYSTEM.GPFS.equals(loc.getFileSystem()) || loc.isGPFSMounted());
	    	}
	    }

		return result;
	}
	
	public void run() {		
		ISelection selection = selectionProvider.getSelection();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		if (structuredSelection.getFirstElement() instanceof IBigInsightsLocation) {
			IBigInsightsLocation loc = (IBigInsightsLocation)structuredSelection.getFirstElement();			
			HBaseLaunchDelegate.launchHBaseShell(loc.getLocationName());
		}
	}
}
