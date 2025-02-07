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
package com.ibm.biginsights.textanalytics.refinement.command;

import java.util.Properties;

import org.eclipse.core.runtime.Assert;

/**
 * This class facilitates the communication of view name and suggested
 * refinements between the AQL editor and the Refinement Wizard. The refinement
 * Wizard accepts such a container as input with a view name. Eventually the
 * Refinement Wizard should update the container with a list of suggested
 * refinements.
 */
public class RefinerContainer {


	
	Properties props = new Properties();

	public void setProperty(String name, String value) {
		Assert.isTrue(value != null);
		props.put(name, value);
	}

	public String getProperty(String name) {
		return props.getProperty(name);
	}

}
