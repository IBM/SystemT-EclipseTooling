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
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.help.ui.internal.views.HelpView;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.util.BIConstants;

public class CreateRunConfigCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		
		List<ArtifactType>artifacts = new ArrayList<ArtifactType>();
		ArtifactType _selectedArtifactType = null;
		
		// read the contributors to the extension point and create a dialog for selection
    	IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("com.ibm.biginsights.artifacts.run"); //$NON-NLS-1$
		for (IConfigurationElement element:config)
		{			
			String name = element.getAttribute("name"); //$NON-NLS-1$
			String desc = element.getAttribute("desc"); //$NON-NLS-1$
			String launchConfigId = element.getAttribute("launchConfigId"); //$NON-NLS-1$
			String helpUrl = element.getAttribute("launchHelpUrl"); //$NON-NLS-1$
			ArtifactType artifactType = new ArtifactType(name, desc, launchConfigId, helpUrl);
			artifacts.add(artifactType);
		}

		// show dialog with all possible run config types
		ArtifactTypeSelectionDialog dlg = new ArtifactTypeSelectionDialog(Activator.getActiveWorkbenchShell(), 
				Messages.CREATERUNCONFIGCOMMAND_DLG_TITLE, Messages.CREATERUNCONFIGCOMMAND_DLG_DESC, artifacts);		
		
		if (dlg.open()==Window.OK) {
			_selectedArtifactType = (ArtifactType)dlg.getResult()[0];
		}

		if (_selectedArtifactType!=null) {
			// then create a run config for the selected type
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(_selectedArtifactType.actionId);		 
			
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

			ILaunchConfigurationWorkingCopy wc;
			try {
				wc = type.newInstance(null, DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(Messages.CREATENEWRUNCONFIGACTION_NEW_CONFIG_NAME));
				int result = DebugUITools.openLaunchConfigurationDialog(Activator.getActiveWorkbenchShell(), wc, BIConstants.RUN_LAUNCH_GROUP_ID, null);
				if (result==Window.OK) {
					wc.doSave();
					
				}
			} catch (CoreException e) {
				MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.LAUNCHSHORTCUT_ERROR_DLG_TTILE, e.getStatus().getMessage());
			}	
		}		
		
		return null;
	}

}
