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

import com.ibm.biginsights.textanalytics.resultviewer.model.FieldType;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;

/**
 * A wrapper on top of <tt>OutputView</tt>, that offers the facility to add individual rows
 * to the output view. 
 * 
 * 
 *
 */
public class OutputViewModel{


	
	OutputView view;
	
	public OutputViewModel(OutputView view){
		this.view = view;
	}

	public OutputViewModel() {
		this.view = new OutputView();
	}

	public void addRow(OutputViewRow row) {
		OutputViewRow rows[] = this.view.getRows();
		if(rows == null){
			rows = new OutputViewRow[1];
			rows[0] = row;
		}else{
			int length = rows.length;
			rows = copyArray(rows, length+1);
			rows[length] = row;
		}
		this.view.setRows(rows);
	}

	public void removeRow(int rowIndex){
		OutputViewRow[] rows = this.view.getRows();
		if(rows == null || rows.length == 0){
			return;
		}else{
			OutputViewRow[] modifiedRows = new OutputViewRow[rows.length-1];
			int index = 0;
			for (int i = 0; i < rows.length; i++) {
				if(i != rowIndex){
					modifiedRows[index] = rows[i];
					index++;
				}
			}
			this.setRows(modifiedRows);
		}
	}

	private OutputViewRow[] copyArray(OutputViewRow[] rows, int count) {
		OutputViewRow[] copiedArray = new OutputViewRow[count];
		if(rows != null){
			for (int i = 0; i < rows.length; i++) {
				copiedArray[i]=rows[i];
			}
		}
		return copiedArray;
	}

	public String[] getFieldNames() {
		return this.view.getFieldNames();
	}


	public void setFieldNames(String[] fieldNames) {
		this.view.setFieldNames(fieldNames);
	}


	public FieldType[] getFieldTypes() {
		return this.view.getFieldTypes();
	}


	public void setFieldTypes(FieldType[] fieldTypes) {
		this.view.setFieldTypes(fieldTypes);
	}


	public OutputViewRow[] getRows() {
		return this.view.getRows();
	}


	public void setRows(OutputViewRow[] rows) {
		this.view.setRows(rows);
	}


	public void setName(String name) {
		this.view.setName(name);
	}


	public String getName() {
		return this.view.getName();
	}
	
	public OutputView getView(){
		return view;
	}

	public void removeFieldType(int fieldIndex) {
		FieldType[] fTypes = getFieldTypes();
		FieldType[] modifiedFTypes = new FieldType[fTypes.length - 1];
		int index = 0;
		for (int i = 0; i < fTypes.length; i++) {
			if(i != fieldIndex){
				modifiedFTypes[index] = fTypes[i];
				index++;
			}
		}
		setFieldTypes(modifiedFTypes);
	}

	public void removeFieldName(int fieldIndex) {
		String[] fNames = getFieldNames();
		String[] modifiedFNames = new String[fNames.length - 1];
		int index = 0;
		for (int i = 0; i < fNames.length; i++) {
			if(i != fieldIndex){
				modifiedFNames[index] = fNames[i];
				index++;
			}
		}
		setFieldNames(modifiedFNames);		
	}
	
	
	
}
