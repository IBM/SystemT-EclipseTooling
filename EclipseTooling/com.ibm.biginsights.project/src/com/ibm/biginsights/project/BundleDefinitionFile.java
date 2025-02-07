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
import java.util.Collections;

public class BundleDefinitionFile {
	public String file;		
	public boolean isRuntimeOnly;	// boolean indicates whether this is should be added to the classpath only during runtime
	public Collection<String> supportedRuntimeVendors; // list of supported vendors for runtime: if empty, assume it always applies
	// vendors in BIConstant: LOCATION_XML_VENDOR_IBM = "ibm", LOCATION_XML_VENDOR_SYMPHONY = "psmr" 

	
	public BundleDefinitionFile(String file, boolean isRuntimeOnly) {
		this.file = file;			
		this.isRuntimeOnly = isRuntimeOnly;
		supportedRuntimeVendors = new ArrayList<String>();
	}

	public BundleDefinitionFile(String file, boolean isRuntimeOnly, String...runtimeVendors) {
		this(file, isRuntimeOnly);			
		if (runtimeVendors!=null && runtimeVendors.length>0) {				
			Collections.addAll(supportedRuntimeVendors, runtimeVendors);
		}		
	}
	
	public boolean supportsRuntimeVendor(String vendor) {
		boolean result = 
			   vendor==null || 
			   supportedRuntimeVendors==null || 
			   supportedRuntimeVendors.isEmpty() ||
			   supportedRuntimeVendors.contains(vendor);
		//System.out.println(file +" supports vendor "+vendor+"? "+result);
		return result;
	}

}