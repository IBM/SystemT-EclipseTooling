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
package com.ibm.biginsights.project.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.BrowserHandler;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.apps.AppSelectionDialog;
import com.ibm.biginsights.project.locations.apps.IBigInsightsApp;
import com.ibm.biginsights.project.util.BIConstants;

public class ViewAppCommand extends AbstractHandler  {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		IBigInsightsLocation location = null;
		String appId = null;		
		String commandId = event.getCommand().getId();		
		boolean isManage = commandId.equals("com.ibm.biginsights.project.deployAppCommand"); //$NON-NLS-1$
		
		if (commandId.equals("com.ibm.biginsights.project.OpenDataAcquisitionAppCommand")) {						 //$NON-NLS-1$
			AppSelectionDialog dlgDataAcquisitionApp = new AppSelectionDialog(Activator.getActiveWorkbenchShell(), 
					Messages.VIEWAPPCOMMAND_DOWNLOAD_SAMPLE_DATA_TITLE, 
					Messages.bind(Messages.VIEWAPPCOMMAND_DOWNLOAD_SAMPLE_DATA_DESC, BIConstants.DATA_ACQUISITION_APP_NAME),
					BIConstants.DATA_ACQUISITION_APP_NAME);
			if (dlgDataAcquisitionApp.open()==Window.OK && dlgDataAcquisitionApp.getResult().length==2) {
				
				location = (IBigInsightsLocation)dlgDataAcquisitionApp.getResult()[0];
				IBigInsightsApp app = (IBigInsightsApp)dlgDataAcquisitionApp.getResult()[1];
				if (app!=null) {
					appId = app.getId();	
					// if the app is only published, go to manage page, otherwise it will go to the run page
					isManage = app.getStatus().equals(Messages.Decoration_published);
				}
			}								 			
		}			
		else if (commandId.equals("com.ibm.biginsights.project.deployAppCommand") || commandId.equals("com.ibm.biginsights.project.runAppCommand")) { //$NON-NLS-1$ //$NON-NLS-2$
			// show dialog to select app for a specific BI server
			String title = commandId.equals("com.ibm.biginsights.project.deployAppCommand") ? //$NON-NLS-1$
					Messages.VIEWAPPCOMMAND_DEPLOY_TITLE : Messages.VIEWAPPCOMMAND_RUN_TITLE;
			String desc = commandId.equals("com.ibm.biginsights.project.deployAppCommand") ? //$NON-NLS-1$
					Messages.VIEWAPPCOMMAND_DEPLOY_DESC : Messages.VIEWAPPCOMMAND_RUN_DESC;
			String errorMessage = commandId.equals("com.ibm.biginsights.project.deployAppCommand") ? //$NON-NLS-1$
					Messages.VIEWAPPCOMMAND_DEPLOY_ERROR : Messages.VIEWAPPCOMMAND_RUN_ERROR;
			AppSelectionDialog dlg = new AppSelectionDialog(Activator.getActiveWorkbenchShell(), title, desc, errorMessage, null, null, 
					commandId.equals("com.ibm.biginsights.project.deployAppCommand") ? Messages.Decoration_published : Messages.Decoration_deployed); //$NON-NLS-1$
			if (dlg.open()==Window.OK && dlg.getResult().length==2) {
				
				location = (IBigInsightsLocation)dlg.getResult()[0];
				IBigInsightsApp app = (IBigInsightsApp)dlg.getResult()[1];
				if (app!=null)
					appId = app.getId();
			}					
		}
		
		if (location!=null && appId!=null)
			BrowserHandler.openViewAppURL(location, appId, isManage);

		return null;
	}

}
