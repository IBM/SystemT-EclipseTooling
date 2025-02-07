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

import java.util.Arrays;
import java.util.HashMap;

import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * Model for Gold Standard labeling UI. Extends SystemTComputationResult and 
 * provides APIs to add a single OutputView at a time to the model.
 * 
 *  Krishnamurthy
 *
 */
public class GoldStandardModel extends SystemTComputationResult {



	private HashMap<String, OutputViewModel> outputViewModelMap = new HashMap<String, OutputViewModel>();
	
	/**
	 * Copy constructor that copies contents of SystemTComputationResult into GoldStandardModel
	 * @param resultModel
	 */
	public GoldStandardModel(SystemTComputationResult resultModel){
		setDocumentID(resultModel.getDocumentID());
		setInputTextID(resultModel.getInputTextID());
		setOutputViews(resultModel.getOutputViews());
		setTextMap(resultModel.getTextValueMap());
		gsComplete = resultModel.gsComplete;
	}
	
	/**
	 * Looks up the outputView map and returns the entry for the given viewName
	 * @param viewName
	 * @return OutputViewModel, if one exists in the map, or null otherwise
	 */
	public OutputViewModel getOutputViewByViewName(String viewName) {
		return outputViewModelMap.get(viewName);
	}

	@Override
	public void setOutputViews(OutputView[] outputViews) {
		outputViewModelMap.clear();
		if(outputViews != null && outputViews.length > 0){
			super.setOutputViews(outputViews);
			for (OutputView outputView : outputViews) {
				outputViewModelMap.put(outputView.getName(), new OutputViewModel(outputView));
			}
		}
	}

	/**
	 * Adds the specified outputView to the list of output views maintained by the model
	 * @param view
	 */
	public void addOutputViewModel(OutputViewModel viewModel) {
		OutputView[] outputViews = super.getOutputViews();
		if(outputViews == null){
			outputViews = new OutputView[1];
			outputViews[0] = viewModel.getView();
		}else{
			int length = outputViews.length;
			outputViews = Arrays.copyOf(outputViews, length+1);
			outputViews[length] = viewModel.getView();
		}
		super.setOutputViews(outputViews);
		outputViewModelMap.put(viewModel.getName(), viewModel);
	}

	/**
	 * Removes the span identified by the annotationType and spanStart and spanEnd parameters.
	 * @param outputViewName
	 * @param annotationType
	 * @param spanStart
	 * @param spanEnd
	 * @return true if the specified span is found and deleted, false otherwise
	 */
	public boolean removeSpan(String outputViewName, String annotationType, int spanStart, int spanEnd) {
		OutputView viewNode = getOutputViewByViewName(outputViewName).getView();
		OutputViewRow[] rows = viewNode.getRows();
		String fieldName = annotationType.replace(" (SPAN)", "");
		int fieldNameIndex = getFieldNameIndex(viewNode.getFieldNames(), fieldName);
		if(fieldNameIndex >= 0){
			int rowIndex = findRowContainingSpan(rows, fieldNameIndex, spanStart, spanEnd);
			if(rowIndex >= 0){
				deleteSpan(viewNode, rowIndex, fieldNameIndex);
				return true;
			}			
		}
		return false;
	}

	private void deleteSpan(OutputView viewNode, int rowIndex, int fieldNameIndex) {
		OutputViewRow row = viewNode.getRows()[rowIndex];
		if(row.fieldValues == null || row.fieldValues.length == 0 || row.fieldValues.length == 1){
			removeRow(viewNode, rowIndex);
		}else{
			FieldValue[] modifiedFieldValues = new FieldValue[row.fieldValues.length -1];
			int index = 0;
			for (int i = 0; i < row.fieldValues.length; i++) {
				if(i != fieldNameIndex){
					modifiedFieldValues[index] = row.fieldValues[i];
					index++;
				}
			}
			row.fieldValues = modifiedFieldValues;
		}
		
	}

	private void removeRow(OutputView viewNode, int rowIndex) {
		OutputViewRow[] rows = viewNode.getRows();
		if(rows == null || rows.length == 0 || rowIndex < 0){
			return;
		}
		
		if(rows.length == 1){
			viewNode.setRows(new OutputViewRow[] {});
			return;
		}
		OutputViewRow[] modifiedRows = new OutputViewRow[rows.length-1];
		int index = 0;
		for (int i = 0; i < rows.length; i++) {
			if(i != rowIndex){
				modifiedRows[index] = rows[i];
				index++;
			}
		}
		viewNode.setRows(modifiedRows);
		
	}

	private int findRowContainingSpan(OutputViewRow[] rows, int fieldNameIndex,int spanStart, int spanEnd) {
		for (int i = 0; i < rows.length; i++) {
			OutputViewRow row = rows[i];
			FieldValue fieldValue = row.fieldValues[fieldNameIndex];
			if(fieldValue instanceof SpanVal){
				SpanVal spanVal = (SpanVal)fieldValue;
				if(spanVal.start == spanStart && spanVal.end == spanEnd){
					return i; 
				}
			}
		}
		return -1;
	}

	public int getFieldNameIndex(String[] fieldNames, String fieldName) {
		if(fieldNames == null || StringUtils.isEmpty(fieldName) || fieldNames.length == 0){
			return -1;
		}else{
			for (int i = 0; i < fieldNames.length; i++) {
				if(fieldNames[i].equals(fieldName)){
					return i;
				}
			}
		}
		return -1;
	}
}
