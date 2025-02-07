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

import java.io.File;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.apps.AppSelectionDialog;
import com.ibm.biginsights.project.locations.apps.ApplicationProvider;
import com.ibm.biginsights.project.locations.apps.BigInsightsApp;
import com.ibm.biginsights.project.locations.apps.IBigInsightsApp;
import com.ibm.biginsights.project.util.BIConnectionException;

public class ImportAppAction extends Action {
	
	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;

	public ImportAppAction() {
		// zero-arg constructor for launch from task launcher
	}

	public ImportAppAction(IWorkbenchPage page, ISelectionProvider selectionProvider)
	{
		super();		
		setText(Messages.Location_ActionImportApp);
		setImageDescriptor(Activator.getImageDescriptor("/icons/importApp_16x.gif")); //$NON-NLS-1$
		this.page = page;
		this.selectionProvider = selectionProvider;		
	}
	
	public void run() {		
		IBigInsightsApp app = null;
		IBigInsightsLocation location = null;		

		if (selectionProvider==null) {
			// launched from task launcher w/o context - show a dialog to select a location	and an app
			AppSelectionDialog dlg = new AppSelectionDialog(Activator.getActiveWorkbenchShell(), Messages.Location_ActionImportApp, 
					Messages.IMPORTAPPACTION_DESC, 
					Messages.IMPORTAPPACTION_ERROR, (IBigInsightsLocation)null);
			dlg.create();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(dlg.getShell(), "com.ibm.biginsights.project.help.import_app"); //$NON-NLS-1$
			dlg.setHelpAvailable(true);
			if (dlg.open()==Window.OK && dlg.getResult().length==2) {
				
				location = (IBigInsightsLocation)dlg.getResult()[0];
				app = (IBigInsightsApp)dlg.getResult()[1];			
				page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			}					
		}
		else {
			// launched from tree
			IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
			
			if(structuredSelection.getFirstElement() instanceof BigInsightsApp){
				app = (BigInsightsApp)structuredSelection.getFirstElement();
				location = ((BigInsightsApp)app).getLocation();		
			}
		}
		
		if (app!=null && location!=null) {			
			//example: http://localhost:8050/enterpriseconsole/catalogStore/archive/6b8909b7-8f71-4efd-84d5-3b35212b2ec0.zip
			String url = location.generateAbsoluteURL("/"+((BigInsightsApp)app).getZipPath()); //$NON-NLS-1$
			
			//get the location to put the zip
			ContainerSelectionDialog dialog = new ContainerSelectionDialog(page.getActivePart().getSite().getShell(),
					ResourcesPlugin.getWorkspace().getRoot(), true, Messages.IMPORTAPPACTION_TITLE);
			dialog.create();
			PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), "com.ibm.biginsights.project.help.import_app"); //$NON-NLS-1$
			dialog.setHelpAvailable(true);
			dialog.open();
			Object[] results = dialog.getResult();
			if(results == null || results.length == 0){
				return; //nothing selected in dialog
			}
			
			String localZipLocation = null;
			IProject project = null;
			if(results[0] instanceof IPath){
				IPath resultPath = ((IPath)results[0]);
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(resultPath.segment(0));
				if(project != null){
					IPath locPath = project.getRawLocation();
					if(locPath == null){
						locPath = project.getLocation();
						if(locPath == null){
							//invalid project
							MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.BIGINSIGHTSLOCATION_CONF_ERROR_TITLE, Messages.ERROR_INVALID_PROJECT );
							return;
						}
					}
					if(!locPath.isAbsolute() || !(new File(locPath.toString()).exists())){
						//invalid project
						MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.BIGINSIGHTSLOCATION_CONF_ERROR_TITLE, Messages.ERROR_INVALID_PROJECT );
						return;
					}
					localZipLocation = locPath.toPortableString();
					if(resultPath.segmentCount()>1){
						localZipLocation += "/" + resultPath.removeFirstSegments(1).toPortableString(); //$NON-NLS-1$
					}
				}else{
					//invalid project
					MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.BIGINSIGHTSLOCATION_CONF_ERROR_TITLE, Messages.ERROR_INVALID_PROJECT );
					return;
				}
			}
			
			//get the zip and unzip it
			ApplicationProvider provider = new ApplicationProvider();
			
			HttpClient httpClient = null;
			try {
				httpClient = location.getHttpClient();
			} catch (BIConnectionException e) {
				location.handleBIConnectionExceptionFromThread(e);
			}
			
			if(httpClient == null){
				return;
			}
			provider.getZipFile(httpClient, url, localZipLocation, location); 
			
			//refresh the project
			try {			
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
			}
		}	    

	}
	
	public boolean isEnabled() {
		boolean result = false;
	    IStructuredSelection structuredSelection = (IStructuredSelection) selectionProvider.getSelection();
	    if (structuredSelection.size() == 1)
	    {
	    	// if element is BigInsightsApp, then enable update
	    	if (structuredSelection.getFirstElement() instanceof BigInsightsApp)
	    		result = true;
	    }
	    return result;
	}

}
