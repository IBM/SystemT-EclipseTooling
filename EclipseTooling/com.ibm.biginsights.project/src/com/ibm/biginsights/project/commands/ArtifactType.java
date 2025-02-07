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
package com.ibm.biginsights.project.commands;

public class ArtifactType {
	public String name;
	public String desc;
	public String actionId;
	public String helpUrl;	
	public String minBIVersion;
	
	public ArtifactType(String name, String desc, String actionId, String helpUrl) {
		this.name = name;
		this.desc = desc;
		this.actionId = actionId;
		this.helpUrl = helpUrl;
	}

	public ArtifactType(String name, String desc, String actionId, String helpUrl, String minBIVersion) {
		this(name, desc, actionId, helpUrl);
		this.minBIVersion = minBIVersion;
	}
}