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

import java.util.Collection;

import org.eclipse.core.runtime.PlatformObject;

import com.ibm.biginsights.project.Messages;

public class BigInsightsLocationRoot extends PlatformObject implements IBigInsightsLocationNode, IBigInsightsLocationChangeListener {
	
	private BILocationContentProvider contentProvider;
	
	BigInsightsLocationRoot(BILocationContentProvider contentProvider) {
		this.contentProvider = contentProvider;
		LocationRegistry.getInstance().addListener(this);
		this.refresh();
	}
	  
	@Override
	public IBigInsightsLocationNode[] getChildren() {
		return LocationRegistry.getInstance().getLocations().toArray(new IBigInsightsLocationNode[LocationRegistry.getInstance().getLocations().size()]);
	}

	@Override
	public void refresh() {		
		if (this.contentProvider!=null)
			this.contentProvider.refresh(this);
	}
		
	@Override
	public String toString() {
		return Messages.BIGINSIGHTSLOCATIONROOT_LABEL;	
	}

	@Override
	public boolean hasChildren() {
		return LocationRegistry.getInstance().getLocations().size()>0;
	}

	@Override
	public void change(LocationChangeEventType eventType, Collection<IBigInsightsLocation> locs) {
		switch (eventType) {
			case LOCATION_ADDED:
			case LOCATION_UPDATED:
			case LOCATION_DELETED: 
			{    	  
				this.contentProvider.refresh(this);				
				break;
			}
		}
		
	}
	
	@Override
	public BILocationContentProvider getContentProvider() {		
		return contentProvider;
	}

	@Override
	public void setContentProvider(BILocationContentProvider biProvider) {
		//already set
		this.contentProvider = biProvider;
	}
}
