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
import java.util.List;

import com.ibm.biginsights.project.locations.BILocationContentProvider;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.IBigInsightsLocationNode;

public class BigInsightsApp implements IBigInsightsApp, IBigInsightsLocationNode {
	private String appName=""; //$NON-NLS-1$
	private String creator=""; //$NON-NLS-1$
	private String description=""; //$NON-NLS-1$
	private String id=""; //$NON-NLS-1$
	private String status="";	 //$NON-NLS-1$
	private String type="";	 //$NON-NLS-1$
	private String subtype="";	 //$NON-NLS-1$

	private BILocationContentProvider provider=null;
	private IBigInsightsLocation location = null;
	private String zipPath=""; //$NON-NLS-1$
	private List<String> categories = new ArrayList();
	
	public IBigInsightsLocationNode[] getChildren(){
		//No children
		return null;
	}
	
	public boolean hasChildren(){
		//No children
		return false;
	}
	
	public void refresh(){
		//no refresh
		return;
	}
	
	public String getAppName(){
		return appName;
	}
	
	public void setAppName(String value){
		appName = value;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public BILocationContentProvider getContentProvider() {		
		return provider;
	}

	@Override
	public void setContentProvider(BILocationContentProvider biProvider) {
		provider = biProvider;		
	}

	public IBigInsightsLocation getLocation() {
		return location;
	}

	public void setLocation(IBigInsightsLocation location) {
		this.location = location;
	}

	public String getZipPath() {
		return zipPath;
	}

	public void setZipPath(String zipPath) {
		this.zipPath = zipPath;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public List<String> getCategories(){
		return categories;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}
	
}
