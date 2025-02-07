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
package com.ibm.biginsights.project;

import java.util.ArrayList;
import java.util.Collection;

public class BundleDefinition{
	public String bundleId;
	public String version;
	public Collection<BundleDefinitionFile>files;
	
	public BundleDefinition(String bundleId, String version) {
		this.bundleId = bundleId;
		this.version = version;
		files = new ArrayList<BundleDefinitionFile>();	
	}
	
	public BundleDefinition(String bundleId, String version, BundleDefinitionFile file) {
		this(bundleId, version);
		this.files.add(file);
	}
	
	public BundleDefinition(String bundleId, String version, Collection<BundleDefinitionFile>files) {
		this(bundleId, version);
		this.files = files;
	}
	
	public void addBundleDefinitionFile(BundleDefinitionFile file) {
		if (file!=null) {
			if (files==null)
				this.files = new ArrayList<BundleDefinitionFile>();
			this.files.add(file);
		}
	}	
}