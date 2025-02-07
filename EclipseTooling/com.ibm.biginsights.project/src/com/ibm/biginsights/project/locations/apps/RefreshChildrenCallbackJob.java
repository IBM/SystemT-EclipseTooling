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

public class RefreshChildrenCallbackJob extends Job {
	private IBigInsightsLocationNode callbacknode = null;
	private IBigInsightsLocation location;
	
	public RefreshChildrenCallbackJob(IBigInsightsLocationNode node, IBigInsightsLocation loc) {
		super(Messages.Location_refreshcallback);
		callbacknode = node;
		location = loc;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		callbacknode.refresh();
		if(monitor.isCanceled()){
			return Status.CANCEL_STATUS;
		}
		
		return Status.OK_STATUS;
	}

}
