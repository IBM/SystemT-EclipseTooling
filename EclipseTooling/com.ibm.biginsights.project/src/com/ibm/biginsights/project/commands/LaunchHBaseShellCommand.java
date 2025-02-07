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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.BigInsightsLibraryContainerInitializer;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.launch.HBaseLaunchDelegate;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;
import com.ibm.biginsights.project.util.BIConstants;

public class LaunchHBaseShellCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		
		if (Activator.getDefault().isChmodInstalled()){				
			try {
				String locationName = event.getParameter(BIConstants.LOCATION_PARM);
				
				ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
				ILaunchConfigurationType configType = launchManager.getLaunchConfigurationType(HBaseLaunchDelegate.HBASE_LAUNCH_CONFIG_TYPE_ID);		 
	
				ILaunchConfigurationWorkingCopy workingCopy = configType.newInstance(null, "hbaseshell"); //$NON-NLS-1$
				
				workingCopy.setAttribute(BIConstants.JOB_LAUNCHTYPE, BIConstants.JOB_LAUNCHTYPE_SHELL);			
				
				IBigInsightsLocation location = LocationRegistry.getInstance().getLocation(locationName);
				IClasspathEntry libsContainer = BigInsightsLibraryContainerInitializer.getInstance().getClasspathEntryByVersion(location.getVersionWithVendor());
				
				List<String> classpath = new ArrayList<String>();
				// hadoop and other libraries
				IRuntimeClasspathEntry bigInsightsLibEntries = JavaRuntime.newRuntimeContainerClasspathEntry(libsContainer.getPath(), IRuntimeClasspathEntry.USER_CLASSES);
				if (bigInsightsLibEntries!=null)
					classpath.add(bigInsightsLibEntries.getMemento());
				workingCopy.setAttribute(BIConstants.BIGINSIGHTS_LOCATION_KEY, locationName);
				
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
				workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
				 
				// now run the config
				DebugUITools.launch(workingCopy, ILaunchManager.RUN_MODE);
							
			} catch (CoreException ce) {
				MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.Error_Title, ce.getStatus().getMessage());	
			}
		}	
		
		return null;
	}

}
