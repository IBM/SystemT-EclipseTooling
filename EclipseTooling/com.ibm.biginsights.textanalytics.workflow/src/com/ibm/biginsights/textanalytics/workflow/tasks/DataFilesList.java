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
package com.ibm.biginsights.textanalytics.workflow.tasks;

import java.util.LinkedList;

import com.ibm.biginsights.textanalytics.workflow.tasks.models.DataFile;

public class DataFilesList extends LinkedList<DataFile> {



	/**
	 * 
	 */
	private static final long serialVersionUID = 4529548488215177440L;

	public DataFilesList(){
		super();
	}
	
	@Override
	public boolean contains(Object o) {
		
		if(o instanceof DataFile){
			for(Object df : this.toArray()){
				if(((DataFile)o).equals((DataFile)df)){
					return true;
				}
			}
		}
		
		return false;
	}
	
}
