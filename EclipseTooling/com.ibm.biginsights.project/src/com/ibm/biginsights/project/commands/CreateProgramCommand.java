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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.ui.internal.views.HelpView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;

public class CreateProgramCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		
		List<ArtifactType>artifacts = new ArrayList<ArtifactType>();
		ArtifactType _selectedArtifactType = null;
		
		// read the contributors to the extension point and create a dialog for selection
    	IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("com.ibm.biginsights.artifacts.create"); //$NON-NLS-1$
		for (IConfigurationElement element:config)
		{			
			String name = element.getAttribute("name"); //$NON-NLS-1$
			String desc = element.getAttribute("desc"); //$NON-NLS-1$
			String newWizardId = element.getAttribute("newWizardId"); //$NON-NLS-1$
			String helpUrl = element.getAttribute("helpUrl"); //$NON-NLS-1$
			ArtifactType artifactType = new ArtifactType(name, desc, newWizardId, helpUrl);
			artifacts.add(artifactType);
		}

		// show dialog with all possible create program types
		ArtifactTypeSelectionDialog dlg = new ArtifactTypeSelectionDialog(Activator.getActiveWorkbenchShell(), 
				Messages.CREATEPROGRAMCOMMAND_DLG_TITLE, Messages.CREATEPROGRAMCOMMAND_DLG_DESC, artifacts);		
		
		if (dlg.open()==Window.OK) {
			_selectedArtifactType = (ArtifactType)dlg.getResult()[0];
		}

		if (_selectedArtifactType!=null) {
			// then open the help and call the new wizard for the selected type            
			if (_selectedArtifactType.helpUrl!=null) {
				// option to show context sensitive help 
//				IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
//				helpSystem.displayHelp("com.ibm.biginsights.jaql.help.run_config_jaql");
				
				// option to launch in external help window
//				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(helpUrl);
				
				// option to launch in help view
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				IViewPart view;
				try {
					view = page.showView( "org.eclipse.help.ui.HelpView" ); //$NON-NLS-1$
		            if ( view instanceof HelpView )
		            {
		                ((HelpView)view).showHelp(_selectedArtifactType.helpUrl);
		            }
				} catch (PartInitException e) {
					MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.LAUNCHSHORTCUT_ERROR_DLG_TTILE, e.getStatus().getMessage());
				} 
				
			}

			try {	
				final String actionId = _selectedArtifactType.actionId;
				final ExecutionEvent execEvent = event;
				Display.getDefault().asyncExec(new Runnable() {								
					public void run() {
						try {
							final IHandlerService handlerService = (IHandlerService)HandlerUtil.getActiveWorkbenchWindow(execEvent).getService(IHandlerService.class);
							ICommandService commandService = (ICommandService)HandlerUtil.getActiveWorkbenchWindow(execEvent).getService(ICommandService.class);
				
							Command command = commandService.getCommand("org.eclipse.ui.newWizard"); //$NON-NLS-1$
							IParameter wizardIdParm = command.getParameter("newWizardId"); //$NON-NLS-1$
							Parameterization parmWizardId = new Parameterization(wizardIdParm, actionId); 			
							final ParameterizedCommand parmCommand = new ParameterizedCommand(command, new Parameterization[] { parmWizardId });

							handlerService.executeCommand(parmCommand, null);
						}
						catch (Exception ex) {
							Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage()));
						}
					}});
			}
			catch (Exception ex) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage()));
			}
		}		
		
		return null;
	}

}
