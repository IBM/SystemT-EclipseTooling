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

package com.ibm.biginsights.textanalytics.concordance.ui;

import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Serializer class for FilterCondtion 
 * 
 */
@XmlRootElement(namespace = "http://www.ibm.com/systemt/filtercondition")
public class FilterConditionQuery {



	@XmlElementWrapper(name="activeTypes")
	@XmlElement(name="type") 
	public String [] types = new String[]{};
	
	@XmlElementWrapper(name="activeFiles")
	@XmlElement(name="file") 
	public String [] files = new String[]{};

	Map <Boolean, String> inputDoc, leftContext, rightContext;

	/**
	 * constructor
	 */
	public FilterConditionQuery() {
		types = new String[] {};
		files = new String[] {};
		inputDoc = new TreeMap<Boolean, String>();
		leftContext = new TreeMap<Boolean, String>();
		rightContext = new TreeMap<Boolean, String>();
	}
	
	public Map<Boolean, String> getInputDoc() {
		return inputDoc;
	}

	public void setInputDoc(Map<Boolean, String> inputDoc) {
		this.inputDoc = inputDoc;
	}

	public Map<Boolean, String> getLeftContext() {
		return leftContext;
	}

	public void setLeftContext(Map<Boolean, String> leftContext) {
		this.leftContext = leftContext;
	}

	public Map<Boolean, String> getRightContext() {
		return rightContext;
	}

	public void setRightContext(Map<Boolean, String> rightContext) {
		this.rightContext = rightContext;
	}

}
