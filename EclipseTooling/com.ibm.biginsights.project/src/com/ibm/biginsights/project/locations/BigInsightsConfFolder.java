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
package com.ibm.biginsights.project.locations;

import java.io.File;

import com.ibm.biginsights.project.Messages;


public class BigInsightsConfFolder implements IBigInsightsLocationNode {

	BigInsightsLocation parent;
	BILocationContentProvider provider=null;
	
	public BigInsightsConfFolder(BigInsightsLocation parent){
		this.parent = parent;		
	}
	
	public Object[] getChildren(){	
		File locationConfFiles = new File(LocationRegistry.getLocationTargetDirectory(parent));
		return provider.getChildren(locationConfFiles);	    
	}
	
	@Override
	public String toString() {
		return Messages.BIGINSIGHTSCONFFOLDER_LABEL;	
	}
	
	public boolean hasChildren(){
		return true;
	}
	
	public void refresh(){		
		if (this.provider!=null)
			this.provider.refresh(this);
	}
	
	@Override
	public BILocationContentProvider getContentProvider() {		
		return provider;
	}

	@Override
	public void setContentProvider(BILocationContentProvider biProvider) {
		provider = biProvider;		
	}

	public BigInsightsLocation getParent() {
		return parent;
	}
	
}
