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
package com.ibm.biginsights.project.locations.apps;

import java.util.Collection;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BigInsightsLocation;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;


public class ApplicationProvider implements ITreeContentProvider{
	
	/**
	 * Get a list of applications already published. 
	 * 
	 * @param client
	 * @param protocol
	 * @param hostname
	 * @param parent 
	 * @param port
	 * @return
	 *
	 */
	public Collection<IBigInsightsApp> getPublishedApplications( String hostname, HttpClient client, IBigInsightsLocation location){
		
		final PublishedAppsJob job = new PublishedAppsJob(hostname, Messages.Info_AppJob_Running, location);
		job._client = client;
		
		
		try {
			job.schedule();
			job.join(); //wait for it to complete
			IStatus status = job.getResult();
			if(status.getCode() != 0){
				showErrorMessage(status.getMessage());
			}
		} catch (InterruptedException e) {
		}	
		
		return job.getApps();
	}
	
	public Collection<IBigInsightsApp> getPublishedApplicationsByCategory( String hostname, HttpClient client, IBigInsightsLocation location, String category){
		
		final PublishedAppsByCategoryJob job = new PublishedAppsByCategoryJob(hostname, Messages.Info_AppJob_Running, location, category);
		job._client = client;
		
		
		try {
			job.schedule();
			job.join(); //wait for it to complete
			IStatus status = job.getResult();
			if(status.getCode() != 0){
				showErrorMessage(status.getMessage());
			}
		} catch (InterruptedException e) {
		}	
		
		return job.getApps();
	}
	
	public Collection<IBigInsightsApp> getPublishedApplicationsByType( String hostname, HttpClient client, 
			                       IBigInsightsLocation location, String type, String subtype){
		
		final PublishedAppsByTypeJob job = new PublishedAppsByTypeJob(hostname, Messages.Info_AppJob_Running, location, type, subtype);
		job._client = client;
		
		
		try {
			job.schedule();
			job.join(); //wait for it to complete
			IStatus status = job.getResult();
			if(status.getCode() != 0){
				showErrorMessage(status.getMessage());
			}
		} catch (InterruptedException e) {
		}	
		
		return job.getApps();
	}
	
	public Collection<String> getApplicationsCategories( String hostname, HttpClient client, IBigInsightsLocation location){
		
		final AppCategoriesJob job = new AppCategoriesJob(hostname, Messages.Info_AppJob_Running, location);
		job._client = client;
		
		
		try {
			job.schedule();
			job.join(); //wait for it to complete
			IStatus status = job.getResult();
			if(status.getCode() != 0){
				showErrorMessage(status.getMessage());
			}
		} catch (InterruptedException e) {
		}	
		
		return job.getCategories();
	}
	
	//for use on a 1.3.0 server that dose not have the rest api to get category names
	public Collection<String> getApplicationsCategories130(String hostName,
			HttpClient httpClient, BigInsightsLocation location) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Get a list of applications already published. 
	 * 
	 * @param client
	 * @param location 
	 * @param protocol
	 * @param hostname
	 * @param port
	 * @return
	 *
	 */
	public void getZipFile( HttpClient client,  String urlZip, String localZipLocation, IBigInsightsLocation location){
		
		final ImportAppJob job = new ImportAppJob();
		job._client = client;
		job.loc = location;
		job.urlZip = urlZip;
		job.localZipLocation = localZipLocation;
		
		try {
			job.schedule();
			job.join(); //wait for it to complete
			IStatus status = job.getResult();
			if(status.getCode() != 0){
				showErrorMessage(status.getMessage());
			}
		} catch (InterruptedException e) {
		}	
		
		return;
	}
	
	
	
	private void showErrorMessage(String localizedMsg) {
		// show dialog
		final String msg = localizedMsg;		

		MessageBox messageBox = new MessageBox(Display.getCurrent().getActiveShell(), SWT.ERROR);
		messageBox.setText(Messages.Error_Title);
		messageBox.setMessage(msg);
		messageBox.open();
		
		// log error
		IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg);
		com.ibm.biginsights.project.Activator.getDefault().getLog().log(status);
	}


	@Override
	public Object[] getChildren(Object parentElement) {
	    return null;
	}


	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean hasChildren(Object element) {
		return false;
	}


	@Override
	public Object[] getElements(Object inputElement) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void dispose() {
		
	}


	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

	

	
}
