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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.util.BIConstants;

public class BrowserHandler {

	public static void openWebConsole(IBigInsightsLocation location) {
		URL url = null;
		try {
			url = new URL(location.generateAbsoluteURL(BIConstants.URL_DIRECT_ROOT+BIConstants.URL_INDEX_HTML));
			openBrowserInstance("BI_"+location.getLocationName(), Messages.BROWSERHANDLER_VIEW_WEBCONSOLE,   //$NON-NLS-1$
					Messages.bind(Messages.BROWSERHANDLER_VIEW_WEBCONSOLE_SERVER, location.getLocationDisplayString()), url);
		} catch (MalformedURLException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));			
		}

	}

	public static void openFilebrowserURL(IBigInsightsLocation location, String path) {
		URL url = null;
		try {
			url = new URL(location.generateAbsoluteURL(BIConstants.URL_DIRECT_ROOT+"/redirect-files.html"+(path!=null && !path.isEmpty() ? "?path="+path :"")));			 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			openBrowserInstance("DFS_"+location.getLocationName(), Messages.BROWSERHANDLER_BROWSE_DFS,   //$NON-NLS-1$
					Messages.bind(Messages.BROWSERHANDLER_BROWSE_DFS_SERVER, location.getLocationDisplayString()), url);
		} catch (MalformedURLException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));			
		}

	}

	// view and manage app have the same URL - whether we see the manage app UI elements depends on the user id that was used to connect	
	public static void openViewAppURL(IBigInsightsLocation location, String appId, boolean isManage) {
		URL url = null;
		try {						
			String parameters = "?"; //$NON-NLS-1$ 
			parameters += isManage ? "show=manage" :""; //$NON-NLS-1$ //$NON-NLS-2$
			if (appId!=null && !appId.isEmpty()) {
				if (isManage)
					parameters += "&"; //$NON-NLS-1$ 
				parameters += "appID="+appId;  //$NON-NLS-1$ 
				
			}			
			url = new URL(location.generateAbsoluteURL(BIConstants.URL_DIRECT_ROOT+"/redirect-app.html"+parameters)); //$NON-NLS-1$ 
			openBrowserInstance("App_"+location.getLocationName(), Messages.BROWSERHANDLER_VIEW_APPLICATION, //$NON-NLS-1$
					Messages.bind(Messages.BROWSERHANDLER_VIEW_APPLICATION_SERVER,location.getLocationDisplayString()), url);
		} catch (MalformedURLException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));			
		}
		
	}

	public static void openJobIdURL(IBigInsightsLocation location, String jobId) {
		URL url = null;
		try {
			String msg = jobId!=null && !jobId.isEmpty() ? 
						Messages.bind(Messages.BROWSERHANDLER_JOB_DETAILS_SERVER_AND_JOB, location.getLocationDisplayString(),jobId) :
						Messages.bind(Messages.BROWSERHANDLER_JOB_DETAILS_SERVER, location.getLocationDisplayString());
			url = new URL(location.generateAbsoluteURL(BIConstants.URL_DIRECT_ROOT+"/redirect-work.html"+(jobId!=null && !jobId.isEmpty() ? "?jobID="+jobId :"?show=jobs")));			 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			openBrowserInstance("Jobs_"+location.getLocationName(), 	//$NON-NLS-1$
								Messages.BROWSERHANDLER_JOB_DETAILS,			
								msg, url); 
		} catch (MalformedURLException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));			
		}

	}

	public static void openBrowserInstance(String id, String name, String tooltip, URL url) { 
		//Disable internal browser checking.(defect 54191) This does not seem to be necessary for eclipse 4.2.2
		//if(!Activator.getDefault().isInternalBrowserOK()) return;
		
		// this will either open an internal web browser launched as an editor window or
		// open up an external web browser depending on the users preferences setting
		try {
			IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
				IWorkbenchBrowserSupport.PERSISTENT | IWorkbenchBrowserSupport.AS_EDITOR | IWorkbenchBrowserSupport.STATUS , id, name, tooltip);
			browser.openURL(url);
		} catch (PartInitException e) {			
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
	}	

}
