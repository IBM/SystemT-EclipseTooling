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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.IBigInsightsLocationNode;
import com.ibm.biginsights.project.util.BIConnectionException;

public class RefreshConnectionJob extends Job {
	private IBigInsightsLocationNode callbacknode = null;
	private IBigInsightsLocation location;
	
	public RefreshConnectionJob(IBigInsightsLocationNode node, IBigInsightsLocation loc) {
		super(Messages.Location_connect);
		callbacknode = node;
		location = loc;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(Messages.Location_connect, IProgressMonitor.UNKNOWN);
		try{
			location.cancelableGetHttpClient(monitor);
			
			//if the connection is bad then remove all children
			if(location.isConnectionStale()){
				location.setClearChildren(true);
			}
			
			//refresh the children
			RefreshChildrenCallbackJob job = new RefreshChildrenCallbackJob(callbacknode, location);
			job.schedule();
		} catch (RuntimeException e) {
			if(monitor.isCanceled() || e.getCause() instanceof InterruptedException ){
				location.setClearChildren(true); //remove all children
				location.resetHttpClient();
				
				//refresh the children
				RefreshChildrenCallbackJob job = new RefreshChildrenCallbackJob(callbacknode, location);
				job.schedule();
				
				return Status.CANCEL_STATUS;
			}else{
				throw e;
			}
		} catch (BIConnectionException e) {
			//show the error message
			location.handleBIConnectionExceptionFromThread(e);
			location.resetHttpClient();
			
			//the connection is bad so remove all children
			location.setClearChildren(true);
			
			//refresh the children
			RefreshChildrenCallbackJob job = new RefreshChildrenCallbackJob(callbacknode, location);
			job.schedule();
			
			if(monitor.isCanceled()){
				return Status.CANCEL_STATUS;
			}
						
			return Status.OK_STATUS;
		}finally{
			monitor.done();
		}
		
		return Status.OK_STATUS;
	}

}
