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
package com.ibm.biginsights.project;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IVMInstall;

import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;
import com.ibm.biginsights.project.util.BIConstants;

public class RuntimeClasspathEntryResolver1 implements
		IRuntimeClasspathEntryResolver {

	@Override
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration)
			throws CoreException {
		
		ArrayList<IRuntimeClasspathEntry>tempArray = new ArrayList<IRuntimeClasspathEntry>();		
		// by default set the vendor to IBM, so anything that is run locally, runs with Apache M/R
		String vendor = BIConstants.LOCATION_XML_VENDOR_IBM; 		 
		// is execution in local mode?				
		boolean isLocalMode = BIConstants.JOB_EXECUTION_MODE_LOCAL.equals(configuration.getAttribute(BIConstants.JOB_EXECUTION_MODE, "")); //$NON-NLS-1$
		// check for Java map/reduce because we need to use vendor=ibm for Java M/R always (needed to ensure that debug mode also runs with Apache M/R because exeuction mode might be set to cluster in debug)
		boolean isJavaMapReduce = configuration.getType().getIdentifier().equals("com.ibm.biginsights.mapreduce.launch.launchConfigurationType");
		if (!isLocalMode && !isJavaMapReduce) {
			// get the vendor from the location object in the run config
			String locationKey = configuration.getAttribute(BIConstants.BIGINSIGHTS_LOCATION_KEY, ""); //$NON-NLS-1$				
			IBigInsightsLocation location = (locationKey!=null && !locationKey.isEmpty()) ? 
											LocationRegistry.getInstance().getLocation(locationKey) : null;
			if (location!=null) 
				vendor = location.getVendor();
		}
		IClasspathEntry[] entries = BigInsightsLibraryContainerInitializer.getInstance().
			getClasspathContainerByVersion(entry.getPath().segment(1), true, vendor).getClasspathEntries();
		for (IClasspathEntry classpathEntry:entries) {
			tempArray.add(new RuntimeClasspathEntry(classpathEntry));
		}
		
		IRuntimeClasspathEntry[] result = new IRuntimeClasspathEntry[tempArray.size()];
		result = (IRuntimeClasspathEntry[])tempArray.toArray(result);
		return result;
	}

	@Override
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project)
			throws CoreException {		
		return resolveRuntimeClasspathEntry(entry, (ILaunchConfiguration)null);
	}

	@Override
	public IVMInstall resolveVMInstall(IClasspathEntry entry) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}
