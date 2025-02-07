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

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;

public class LaunchPreferencesAction extends Action {
	
	public LaunchPreferencesAction() {
		// zero-arg constructor for launch from task launcher
	}
	
	public void run() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IHandlerService handlerService = (IHandlerService)page.getActivePart().getSite().getService(IHandlerService.class);
		ICommandService commandService = (ICommandService)page.getActivePart().getSite().getService(ICommandService.class);
		try {					
			// launch JAQL shell launch command with location name as parameter
			ParameterizedCommand pcmd = commandService.deserialize("org.eclipse.ui.window.preferences("+ //$NON-NLS-1$
					"preferencePageId=org.eclipse.datatools.connectivity.internal.ui.preferences.DriverPreferences"+ //$NON-NLS-1$
					")"); //$NON-NLS-1$
			handlerService.executeCommand(pcmd, null);
		} catch (Exception e) {			
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
			Activator.getDefault().getLog().log(status);
			ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.LAUNCHPREFERENCESACTION_TITLE, Messages.LAUNCHPREFERENCESACTION_MESSAGE, status);
		}

	}
}
