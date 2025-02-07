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

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BILocationContentProvider;
import com.ibm.biginsights.project.locations.BigInsightsLocation;
import com.ibm.biginsights.project.locations.IBigInsightsLocationNode;
import com.ibm.biginsights.project.util.BIConnectionException;
import com.ibm.biginsights.project.util.BIConstants;

public class BigInsightsAppTypeFolder implements IBigInsightsLocationNode {
	BigInsightsLocation location;
	BILocationContentProvider provider=null;
	private String type=""; //$NON-NLS-1$
	private String subtype=""; //$NON-NLS-1$
	
	public BigInsightsAppTypeFolder(BigInsightsLocation location, String type, String subtype){
		this.location = location;
		this.type = type;
		this.subtype = subtype;
	}
	
	public IBigInsightsLocationNode[] getChildren(){
		if(location.isVersion2orAbove()){ 
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

				ApplicationProvider appProvider = new ApplicationProvider();
				Collection<IBigInsightsApp> apps = appProvider.getPublishedApplicationsByType(location.getHostName(), location.getHttpClient(), location, type, subtype);
				return apps.toArray(new BigInsightsApp[]{});
				
			} catch (BIConnectionException e) {
				location.handleBIConnectionExceptionFromThread(e);
			}
		}
		return new BigInsightsApp[0];
	}
	
	
	public String getType(){
		return type;
	}
	
	public void setType(String t){
		type = t;
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

	public String getTypeName() {
		if(type.equals(BIConstants.VIEWBY_TYPE_WORKFLOW)) return Messages.Location_Type_Workflow;
		if(type.equals(BIConstants.VIEWBY_TYPE_BIGSHEETSPLUGIN)) return Messages.Location_Type_BS;
		if(type.equals(BIConstants.VIEWBY_TYPE_JAQLMODULE)) return Messages.Location_Type_JaqlModule;
		if(type.equals(BIConstants.VIEWBY_TYPE_WORKFLOW_BIGSHEETSPLUGIN) && subtype.equals(BIConstants.VIEWBY_SUBTYPE_TEXTANALYTICS)) return Messages.Location_Type_TextAnalytics;
		return null;
	}
	
	
}
