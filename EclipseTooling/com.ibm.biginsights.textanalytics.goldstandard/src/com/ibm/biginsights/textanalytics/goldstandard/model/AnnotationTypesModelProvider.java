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
package com.ibm.biginsights.textanalytics.goldstandard.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;

/**
 * This class provides the model for AnnotationTypes table of Gold Standard configuration dialog
 *   
 *  Krishnamurthy
 *
 */
public class AnnotationTypesModelProvider {



	private static HashMap<String, AnnotationTypesModelProvider> modelProviders = new HashMap<String, AnnotationTypesModelProvider>();
	
	private List<AnnotationType> listAnnotationTypes;
	protected String projectName;
	protected String gsName;
	
	private AnnotationTypesModelProvider(String projectName, String gsName){
		listAnnotationTypes = new ArrayList<AnnotationType>();
		this.projectName = projectName;
		this.gsName = gsName;
		refreshAnnotationTypes();
	}
	
	public List<AnnotationType> getAnnotationTypes(){
		refreshAnnotationTypes();
		return listAnnotationTypes;
	}
	
	public static AnnotationTypesModelProvider getInstance(String projectName, String gsName){
		String key = projectName+"."+gsName;
		AnnotationTypesModelProvider modelProvider = modelProviders.get(key);
		if(modelProvider == null){
			modelProvider = new AnnotationTypesModelProvider(projectName, gsName);
			modelProviders.put(key, modelProvider);
		}
		
		return modelProvider;
	}
	
	public void refreshAnnotationTypes(){
		listAnnotationTypes.clear();
		AnnotationType[] annTypes =  GoldStandardUtil.getAnnotationTypesByProjectName(projectName, gsName);
		if(annTypes != null){
			for (AnnotationType annotationType : annTypes) {
				listAnnotationTypes.add(annotationType);
			}
		}
	}
}
