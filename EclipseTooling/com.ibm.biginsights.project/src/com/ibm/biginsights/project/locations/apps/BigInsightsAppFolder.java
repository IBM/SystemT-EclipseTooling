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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BILocationContentProvider;
import com.ibm.biginsights.project.locations.BigInsightsLocation;
import com.ibm.biginsights.project.locations.IBigInsightsLocationNode;
import com.ibm.biginsights.project.util.BIConnectionException;
import com.ibm.biginsights.project.util.BIConstants;

public class BigInsightsAppFolder implements IBigInsightsLocationNode {
	BigInsightsLocation location;
	BILocationContentProvider provider=null;
	
	List<String> _130Categories = new ArrayList<String>();
	HashMap<String, List<IBigInsightsApp>> _130AppsByCategory = new HashMap<String, List<IBigInsightsApp>>();
	
	public BigInsightsAppFolder(BigInsightsLocation location){
		this.location = location;
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
			
			ApplicationProvider appProvider = new ApplicationProvider();
			//show apps by name
			if(location.getViewBy()==BIConstants.VIEWBY_NAME ){
				Collection<IBigInsightsApp> apps = appProvider.getPublishedApplications(location.getHostName(), location.getHttpClient(), location);
				return apps.toArray(new BigInsightsApp[]{});
			}
			
			//show apps by category
			if(location.getViewBy()==BIConstants.VIEWBY_CATEGORY ){
				
				List<BigInsightsAppCategoryFolder> categoryFolders = new ArrayList<BigInsightsAppCategoryFolder>();
				Collection<String> categoryNames;
				if(location.isVersion1301orAbove()){ //version 1.3.0.1 or greater
					categoryNames = appProvider.getApplicationsCategories(location.getHostName(), location.getHttpClient(), location);
	
					for(String categoryName : categoryNames){
						BigInsightsAppCategoryFolder catFolder = new BigInsightsAppCategoryFolder(location, categoryName, this);
						categoryFolders.add(catFolder);
					}
				}else{
					_130Categories.clear();
					_130AppsByCategory.clear();
					Collection<IBigInsightsApp> apps = appProvider.getPublishedApplications(location.getHostName(), location.getHttpClient(), location);
					sortAppsByCategory(apps);
					for(String categoryName : _130Categories){
						BigInsightsAppCategoryFolder catFolder = new BigInsightsAppCategoryFolder(location, categoryName, this);
						categoryFolders.add(catFolder);
					}
				}
				return categoryFolders.toArray(new BigInsightsAppCategoryFolder[]{});
			}
			
			//show apps by type
			if(location.getViewBy()==BIConstants.VIEWBY_TYPE){
				List<BigInsightsAppTypeFolder> typeFolders = new ArrayList<BigInsightsAppTypeFolder>();
				typeFolders.add(new BigInsightsAppTypeFolder(location, BIConstants.VIEWBY_TYPE_WORKFLOW, "")); //$NON-NLS-1$
				typeFolders.add(new BigInsightsAppTypeFolder(location, BIConstants.VIEWBY_TYPE_BIGSHEETSPLUGIN, "")); //$NON-NLS-1$
				typeFolders.add(new BigInsightsAppTypeFolder(location, BIConstants.VIEWBY_TYPE_JAQLMODULE, "")); //$NON-NLS-1$
				typeFolders.add(new BigInsightsAppTypeFolder(location,  BIConstants.VIEWBY_TYPE_WORKFLOW_BIGSHEETSPLUGIN, BIConstants.VIEWBY_SUBTYPE_TEXTANALYTICS));
				return typeFolders.toArray(new BigInsightsAppTypeFolder[]{});
			}
			
			return new BigInsightsApp[0]; //empty app
		} catch (BIConnectionException e) {
			location.handleBIConnectionExceptionFromThread(e);
			return new BigInsightsApp[0];
		}
	}
	
	private void sortAppsByCategory(Collection<IBigInsightsApp> apps) {
		for(IBigInsightsApp app : apps){
			List<String> categories = app.getCategories();
			for(String category : categories){
				if(_130Categories.contains(category)){
					List<IBigInsightsApp> appsInCategory =  _130AppsByCategory.get(category);
					if(!appsInCategory.contains(app)){
						appsInCategory.add(app);
					}
				}else{
					//new category
					_130Categories.add(category);
					List<IBigInsightsApp> appsInCategory = new ArrayList<IBigInsightsApp>();
					appsInCategory.add(app);
					_130AppsByCategory.put(category, appsInCategory);
				}
			}
		}
		
	}

	@Override
	public String toString() {
		return Messages.Location_AppFolderName;	
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
		return location;
	}
	
	public BigInsightsLocation getLocation() {
		return location;
	}
	
	
}
