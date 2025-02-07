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

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.util.BIConstants;

public class CheckDTPAction extends Action {
	
	public void run() {		
		boolean foundDTP = false;
		if (Platform.getBundleGroupProviders().length>0) {
			IBundleGroup[] bundleGroups = Platform.getBundleGroupProviders()[0].getBundleGroups();
			for (IBundleGroup bundleGroup:bundleGroups) {
				if (bundleGroup.getIdentifier().equals(BIConstants.ORG_ECLIPSE_DATATOOLS_SQLDEVTOOLS_FEATURE_ID)) {
					foundDTP = true;
					String version = bundleGroup.getVersion();	
					// only print major, minor and fixpack version
					String[]versionNumbers = version.split("\\."); //$NON-NLS-1$
					String printVersion = versionNumbers[0]+"."+versionNumbers[1]+"."+versionNumbers[2]; //$NON-NLS-1$ //$NON-NLS-2$
					if (isLower(version, BIConstants.ORG_ECLIPSE_DATATOOLS_SQLDEVTOOLS_FEATURE_MIN_VERSION)) {
						MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.CHECKDTPACTION_TITLE, 
									Messages.bind(Messages.CHECKDTPACTION_VERSION_TOO_LOW, printVersion, BIConstants.ORG_ECLIPSE_DATATOOLS_SQLDEVTOOLS_FEATURE_MIN_VERSION)); 
					}
					else {
						MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.CHECKDTPACTION_TITLE, 
									Messages.bind(Messages.CHECKDTPACTION_SUCCESSFUL, printVersion)); 
					}
					
				}
				
			}
		}
		if (!foundDTP) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.CHECKDTPACTION_TITLE, 
					Messages.bind(Messages.CHECKDTPACTION_NOT_INSTALLED, BIConstants.ORG_ECLIPSE_DATATOOLS_SQLDEVTOOLS_FEATURE_MIN_VERSION));
		}
	}
	
	private static boolean isLower(String version1, String version2) {
		// returns true if project needs to be upgraded
		String[]version1Numbers = version1.split("\\."); //$NON-NLS-1$
		String[]version2Numbers = version2.split("\\."); //$NON-NLS-1$
		
		// compare major version
		if (isLowerNumber(version1Numbers[0], version2Numbers[0]) ||  
		   (version1Numbers[0].equals(version2Numbers[0]) && isLowerNumber(version1Numbers[1], version2Numbers[1])) ||
		   (version1Numbers[0].equals(version2Numbers[0]) && version1Numbers[1].equals(version2Numbers[1]) && isLowerNumber(version1Numbers[2], version2Numbers[2]))) 
		{
			return true;
		}			
		return false;	
	}
	
	private static boolean isLowerNumber(String sA, String sB) {
		int A = new Integer(sA);
		int B = new Integer(sB);
		return A<B ? true : false;
	}
}
