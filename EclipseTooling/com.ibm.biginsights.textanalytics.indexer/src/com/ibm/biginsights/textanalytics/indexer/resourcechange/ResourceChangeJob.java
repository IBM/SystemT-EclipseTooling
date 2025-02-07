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
package com.ibm.biginsights.textanalytics.indexer.resourcechange;

import org.eclipse.core.resources.IResource;

import com.ibm.biginsights.textanalytics.indexer.types.ResourceAction;
/**
 * The Class holds the Job Details.
 * The resource resSrc & resDest will not be null, if the action is ResourceAction.MOVED
 * The resDest will be null for rest of the actions. 
 *
 */
public class ResourceChangeJob {



	IResource resSrc;
	IResource resDest;
	ResourceAction action;
	
	public ResourceChangeJob(IResource resSrc, ResourceAction action) {
		super();
		this.resSrc = resSrc;
		this.action = action;
	}

	public ResourceChangeJob(IResource resSrc, IResource resDest,
			ResourceAction action) {
		super();
		this.resSrc = resSrc;
		this.resDest = resDest;
		this.action = action;
	}

	public IResource getResSrc() {
		return resSrc;
	}

	public IResource getResDest() {
		return resDest;
	}

	public ResourceAction getAction() {
		return action;
	}

	


}
