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

package com.ibm.biginsights.textanalytics.concordance.ui.export.model;

import java.util.List;

/**
 * Represents the Spans and Schema for a document and for a view. This is used
 * to generate the CSV and HTMLs from the Annotation Explorer Results.
 * 
 *  Simon
 * 
 */
public class TableView {



	private String docName;
	private int sourceId;
	private String viewName;
	private String viewSchema[];
	private List<TableData[]> cellValue;

	
	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public List<TableData[]> getCellValue() {
		return cellValue;
	}

	public void setCellValue(List<TableData[]> cellValue) {
		this.cellValue = cellValue;
	}

	public String[] getViewSchema() {
		return viewSchema;
	}

	public void setViewSchema(String[] viewSchema) {
		this.viewSchema = viewSchema;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

}
