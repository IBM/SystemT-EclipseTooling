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
import java.util.List;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BILocationContentProvider;
import com.ibm.biginsights.project.locations.BigInsightsLocation;
import com.ibm.biginsights.project.locations.IBigInsightsLocationNode;
import com.ibm.biginsights.project.util.BIConnectionException;

public class BigInsightsAppCategoryFolder implements IBigInsightsLocationNode {
	BigInsightsLocation location;
	BILocationContentProvider provider=null;
	private String categoryName=""; //$NON-NLS-1$
	private BigInsightsAppFolder parentFolder;
	
	public BigInsightsAppCategoryFolder(BigInsightsLocation location, String categoryName, BigInsightsAppFolder parentFolder){
		this.location = location;
		this.categoryName = categoryName;
		this.parentFolder = parentFolder;
	}
	
	public IBigInsightsLocationNode[] getChildren(){
		 
		try {
			if(location.isConnectionStale()){
				
				if(location.isClearChildren()){					
					location.setClearChildren(false);
					//return empty list
					return new BigInsightsApp[0];
				}
				
				//start thread to refresh connection and return
				final RefreshConnectionJob job = new RefreshConnectionJob(this, location);				
				job.schedule(); //run asynch do not wait
				
				//return one dummpy app
				BigInsightsApp dummyapp = new BigInsightsApp();
				dummyapp.setAppName(Messages.Location_loading);				
				return new BigInsightsApp[]{dummyapp};
			}
			
			if(location.isVersion1301orAbove()){
				ApplicationProvider appProvider = new ApplicationProvider();
				Collection<IBigInsightsApp> apps = appProvider.getPublishedApplicationsByCategory(location.getHostName(), location.getHttpClient(), location, categoryName);
				return apps.toArray(new BigInsightsApp[]{});
			}else{
				if(parentFolder._130AppsByCategory.containsKey(categoryName)){
					List<IBigInsightsApp> apps = parentFolder._130AppsByCategory.get(categoryName);
					return apps.toArray(new BigInsightsApp[]{});
				}else{
					return new BigInsightsApp[0];
				}
			}
		} catch (BIConnectionException e) {
			location.handleBIConnectionExceptionFromThread(e);
			return new BigInsightsApp[0];
		}
	}
	
	
	public String getCategoryName(){
		return categoryName;
	}
	
	public void setCategoryName(String name){
		 categoryName = name;
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

	public BigInsightsLocation getLocation() {
		return location;
	}
	
	
}
