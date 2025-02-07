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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.json.java.JSONArray;

public class SampleProjectsProvider {

	public JSONArray getSampleProjects(IBigInsightsLocation location, String projectSuffix){
		
		final SampleProjectsJob job = new SampleProjectsJob(location);		
		
		try {
			job.schedule();
			job.join(); //wait for it to complete
			IStatus status = job.getResult();
			if(status.getCode() != 0){
				showErrorMessage(status.getMessage());
			}
		} catch (InterruptedException e) {
		}	
		
		return job.getProjects();
	}

	public IStatus importSampleProject(IBigInsightsLocation location, String name, String projectSuffix, String fileLocation){
				
		final SampleProjectImportJob job = new SampleProjectImportJob(location, name, projectSuffix, fileLocation);		
		IStatus status = null; 
			
		try {
			job.schedule();
			job.join(); //wait for it to complete
			status = job.getResult();
			if(status.getCode() != 0){
				showErrorMessage(status.getMessage());
			}			
		} catch (InterruptedException e) {
			status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					 Messages.Error_AppJob_1+ " " + Messages.LOCATIONSERVERAUTHFAILED); //$NON-NLS-1$
		}
		
		return status;
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
}
