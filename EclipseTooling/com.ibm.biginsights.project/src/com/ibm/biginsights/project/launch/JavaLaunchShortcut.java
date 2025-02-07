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
package com.ibm.biginsights.project.launch;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaApplicationLaunchShortcut;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.util.BIConstants;

public class JavaLaunchShortcut extends JavaApplicationLaunchShortcut {

	public static final String LAUNCH_CONFIG_TYPE_ID = "com.ibm.biginsights.project.launchConfigurationTypeJava"; //$NON-NLS-1$
	
	protected ILaunchConfigurationType getConfigurationType() {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		return launchManager.getLaunchConfigurationType(LAUNCH_CONFIG_TYPE_ID);		 
	}
	
	protected ILaunchConfigurationWorkingCopy createConfigurationWorkingCopy(IType type) {		
		ILaunchConfigurationWorkingCopy wc = null;
		try {
			ILaunchConfigurationType configType = getConfigurationType();
			wc = configType.newInstance(null, DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(type.getTypeQualifiedName('.')));
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, type.getJavaProject().getElementName());
			wc.setMappedResources(new IResource[] {type.getUnderlyingResource()});			
		} catch (CoreException exception) {
			MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.LAUNCHSHORTCUT_ERROR_TITLE, exception.getStatus().getMessage());	
		} 
		return wc;
	}

	protected void launch(IType type, String mode) {
		ILaunchConfiguration config = findLaunchConfiguration(type, getConfigurationType());
		boolean launchConfig = config!=null;
		boolean isNewConfig = false;
		if (config == null) {
			// force opening of Create Launch config (otherwise no location will be set)
			ILaunchConfigurationWorkingCopy workingCopy = createConfigurationWorkingCopy(type);
			int result = DebugUITools.openLaunchConfigurationDialog(getShell(), workingCopy, BIConstants.RUN_LAUNCH_GROUP_ID, null);
			launchConfig = result == Window.OK;	
			// save the config only if user clicked OK
			if (launchConfig) {
				isNewConfig = true; // openLaunchConfigurationDialog already launches DebugUITools.launch
				try {
					config = workingCopy.doSave();					
				} catch (CoreException e) {					
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			}
		}
		if (config != null && launchConfig && !isNewConfig) { // openLaunchConfigurationDialog already launches DebugUITools.launch, so no need to execute again
			DebugUITools.launch(config, mode);
		}			
	}

}
