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
package com.ibm.biginsights.project.sampleProjects;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.util.Authenticator;
import com.ibm.biginsights.project.util.BIConnectionException;
import com.ibm.biginsights.project.util.BIConstants;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SampleProjectsJob extends Job {

	private IBigInsightsLocation _location;		
	private ArrayNode _projects = null;

	public SampleProjectsJob(IBigInsightsLocation location) {
		super(Messages.SAMPLEPROJECTSJOB_RETRIEVING_PROJECTS_JOB_NAME);
		this._location = location;
	}
	

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(Messages.SAMPLEPROJECTSJOB_RETRIEVING_PROJECTS, IProgressMonitor.UNKNOWN);
		
		String uri = _location.generateAbsoluteURL(BIConstants.URL_ECLIPSE_PROJECTS);	//test error
		
		GetMethod method = new GetMethod(uri);
		Authenticator.setSessionCookie(method, _location);
		try {
			int statusCode = _location.getHttpClient().executeMethod(method);
			
			if (statusCode == HttpStatus.SC_OK) {
				//make sure that json is returned
				Header contentType  = method.getResponseHeader("Content-Type"); //$NON-NLS-1$
				if(contentType != null && !contentType.getValue().startsWith("text/xml")){ //$NON-NLS-1$
					//error not json, must be login page returned
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
							 Messages.Error_AppJob_1+ " " + Messages.LOCATIONSERVERAUTHFAILED); //$NON-NLS-1$
				}
				InputStream response = method.getResponseBodyAsStream();	
				ObjectMapper mapper = new ObjectMapper();
				JsonNode returnJson = mapper.readTree(response);
				if (returnJson.get("status").equals("SUCCESS")) { //$NON-NLS-1$ //$NON-NLS-2$
					_projects = (ArrayNode)returnJson.get("sampleapps");	 //$NON-NLS-1$
					return Status.OK_STATUS;
				}
				else return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
						 Messages.Error_AppJob_1+ " " + returnJson.get("statusdetail")); //$NON-NLS-1$ //$NON-NLS-2$
				
			 }else{
				 //error
				 return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
						 Messages.Error_AppJob_1+ " " + statusCode); //$NON-NLS-1$
			 }
			 
		} catch (HttpException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					Messages.Error_AppJob_1+ " " + e.getMessage()); //$NON-NLS-1$
		} catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					Messages.Error_AppJob_1+ " " + e.getMessage()); //$NON-NLS-1$
		} catch (BIConnectionException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					Messages.Error_AppJob_1+ " " + e.getMessage()); //$NON-NLS-1$
		}finally{
			method.releaseConnection();
			monitor.done();
		}
	}
	
	public ArrayNode getProjects() {
		return _projects;
	}	

}
