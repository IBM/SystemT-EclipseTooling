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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
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
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;
import com.ibm.biginsights.project.util.BIConstants;

public class HBaseLaunchDelegate extends BigInsightsLaunchDelegate {
	
	public static final String HBASE_LAUNCH_CONFIG_TYPE_ID = "com.ibm.biginsights.project.launchConfigurationHBaseShell"; //$NON-NLS-1$
	
	public void launch(ILaunchConfiguration configuration, String mode,	ILaunch launch, IProgressMonitor monitor) throws CoreException {	
		//call cygwin check
		if(!com.ibm.biginsights.project.Activator.getDefault().isChmodInstalled()){
			return;
		}
		super.launch(configuration, mode, launch, monitor);
	}
		
	public String getVMArguments(ILaunchConfiguration configuration) throws CoreException {
	  String vmArguments = super.getVMArguments(configuration);

	  if (vmArguments!=null && !vmArguments.isEmpty())
	    vmArguments += " "; //$NON-NLS-1$

	  // The latest hirb.rb needs this system variable.
	  String hbaseSources = LocationRegistry.getHBasePath(location.getVersionWithVendor()) + "/lib/ruby";  //$NON-NLS-1$
	  vmArguments += "-Dhbase.ruby.sources=\"" + hbaseSources + "\"";                                               //$NON-NLS-1$

	  return vmArguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getMainTypeName(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public String getMainTypeName(ILaunchConfiguration configuration) throws CoreException {
		return BIConstants.HBASE_SHELL_EXEC;
	}
	
	public String getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		String programArguments = super.getProgramArguments(configuration);
		String additionalProgramArguments = ""; //$NON-NLS-1$
		// get launch type: by default it's running HBase shell
		String launchType = configuration.getAttribute(BIConstants.JOB_LAUNCHTYPE, BIConstants.JOB_LAUNCHTYPE_SHELL);
		
		if (BIConstants.JOB_LAUNCHTYPE_SHELL.equals(launchType)) {
			String hbaseLoc = LocationRegistry.getHBasePath(location.getVersionWithVendor());
															
			additionalProgramArguments = "\""+hbaseLoc+"/bin/hirb.rb\""; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (!additionalProgramArguments.isEmpty()) {
			if (!programArguments.isEmpty())
				programArguments +=" "; //$NON-NLS-1$
			programArguments += additionalProgramArguments;
		}
		
		return programArguments;

	}
	
	public static void launchHBaseShell(String locationName)
	{		
		if (com.ibm.biginsights.project.Activator.getDefault().isChmodInstalled()){				
			try {
				ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
				ILaunchConfigurationType configType = launchManager.getLaunchConfigurationType(HBASE_LAUNCH_CONFIG_TYPE_ID);		 
	
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
	}
}
