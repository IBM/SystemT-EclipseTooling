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
package com.ibm.biginsights.textanalytics.resultdifferences.gsmeasures;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.biginsights.textanalytics.goldstandard.model.GoldStandardModel;
import com.ibm.biginsights.textanalytics.goldstandard.model.OutputViewModel;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.compute.DocSpanDifference;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.Constants.GoldStandardType;

/**
 *  Krishnamurthy
 *
 */
public class GSMeasureCalculator {


	
	private static final DecimalFormat twoDecimalFormat = new DecimalFormat("#.##");
	
	/**
	 * Key: fileName
	 * Value: SystemTComputationResult
	 */
	private Map<String,SystemTComputationResult> modelMap_expected;
	
	/**
	 * Key: fileName
	 * Value: SystemTComputationResult
	 */
	private Map<String,SystemTComputationResult> modelMap_actual;
	
	/**
	 * Map used to store count of exact spans for a given actualFileName, expectedFileName and viewNameDotFileName
	 * Key = actualFileName+";"+expectedFileName+";"+viewNameDotFieldName
	 * Value = Integer, count of exact spans
	 */
	private HashMap<String, Integer> cache_exactSpanCount = new HashMap<String, Integer>();
	
	/**
	 * Map used to store count of exact spans for a given actualFileName, expectedFileName and viewNameDotFileName
	 * Key = actualFileName+";"+expectedFileName+";"+viewNameDotFieldName
	 * Value = Integer, count of partial spans
	 */
	private HashMap<String, Integer> cache_partialSpanCount = new HashMap<String, Integer>();

	/**
	 * Map that caches exact GS Measure of a viewNameDotFieldName. If the UI requests for these measures again, fetch from the cache
	 * Key = viewNameDotFieldName
	 * Value = double[] of length=3, containing precision, recall and f-measure
	 */
	private HashMap<String, double[]> cache_exactGSMeasure = new HashMap<String, double[]>();
	
	/**
	 * Map that caches relaxed GS Measure of a viewNameDotFieldName. If the UI requests for these measures again, fetch from the cache
	 * Key = viewNameDotFieldName
	 * Value = double[] of length=3, containing precision, recall and f-measure
	 */
	private HashMap<String, double[]> cache_relaxedGSMeasure = new HashMap<String, double[]>();
	
	/**
	 * Map that caches partial GS Measure of a viewNameDotFieldName. If the UI requests for these measures again, fetch from the cache
	 * Key = viewNameDotFieldName
	 * Value = double[] of length=3, containing precision, recall and f-measure
	 */
	private HashMap<String, double[]> cache_partialGSMeasure = new HashMap<String, double[]>();
	
	/**
	 * Caches the DocSpanDifference objects for a given set of actualFile, expectedFile and viewNameDotFieldName
	 * Key = actualFileName+";"+expectedFileName+";"+viewNameDotFieldName
	 * Value = DocSpanDifference object for the given key
	 */
	private HashMap<String, DocSpanDifference> cache_DocSpanDiff = new HashMap<String, DocSpanDifference>();
	
	private ArrayList<String> superSetOfFileNames = new ArrayList<String>();

	
	/**
	 * Stores the keys of modelMap_expected and modelMap_actual in sorted order.
	 * @see sortKeys()
	 */
	//private ArrayList<String> sortedFileNames_expected;
	//private ArrayList<String> sortedFileNames_actual;
	
	protected int actual_size;
	protected int expected_size;
	
	/**
	 * @param modelMap_expected
	 * @param modelMap_actual
	 */
	public GSMeasureCalculator(
			Map<String, SystemTComputationResult> expectedFileModelMap,
			Map<String, SystemTComputationResult> actualFileModelMap, ArrayList<String> pSuperSetOfFileNames) {
		super();
		this.modelMap_expected = expectedFileModelMap;
		this.modelMap_actual = actualFileModelMap;
		this.superSetOfFileNames = pSuperSetOfFileNames;
		//sortedFileNames_expected = new ArrayList<String>();
		//sortedFileNames_actual = new ArrayList<String>();
		//copyAndSortFileNames();
	}

/*	private void copyAndSortFileNames() {
		Set<String> keys_expected = modelMap_expected.keySet();
		copySetToList(keys_expected, sortedFileNames_expected);
		Collections.sort(sortedFileNames_expected);
		
		Set<String> keys_actual = modelMap_actual.keySet();
		copySetToList(keys_actual, sortedFileNames_actual);
		Collections.sort(sortedFileNames_actual);
	}
	private void copySetToList(Set<String> keys, ArrayList<String> keyList) {
		for (String key : keys) {
			keyList.add(key);
		}
	}
*/
	public double[] getGSMeasures(String viewNameDotFieldName, GoldStandardType gsType){
		actual_size = calculateActualSize(viewNameDotFieldName);
		expected_size = calculateExpectedSize(viewNameDotFieldName);
		
		if(GoldStandardType.Exact.equals(gsType)){
			return getGSMeasures_Exact(viewNameDotFieldName);
		}else if(GoldStandardType.Relaxed.equals(gsType)){
			return getGSMeasures_Relaxed(viewNameDotFieldName);
		}else if(GoldStandardType.Partial.equals(gsType)){
			return getGSMeasures_Partial(viewNameDotFieldName);
		}
		
		return new double[0];
	}

	private double[] getGSMeasures_Partial(String viewNameDotFieldName) {
		double[] gsMeasures = cache_partialGSMeasure.get(viewNameDotFieldName);
		if(gsMeasures == null){
			GSMeasure corpusLevelGSMeasure = new GSMeasure();
			Iterator<String> iter = superSetOfFileNames.iterator();
			String fileName = null;
			while (iter.hasNext())
			{
				fileName = iter.next();
				GSMeasure fileLevelGSMeasure = new GSMeasure();
				calculateExactSpans(fileName, fileName, fileLevelGSMeasure, viewNameDotFieldName);
				calculatePartialScore(fileName,fileName, fileLevelGSMeasure, viewNameDotFieldName);
				addGSMeasures(corpusLevelGSMeasure, fileLevelGSMeasure);
			}
/*			for(int i=0;i<sortedFileNames_expected.size();++i){
				GSMeasure fileLevelGSMeasure = new GSMeasure();
				calculateExactSpans(sortedFileNames_expected.get(i), sortedFileNames_actual.get(i), fileLevelGSMeasure, viewNameDotFieldName);
				calculatePartialScore(sortedFileNames_expected.get(i), sortedFileNames_actual.get(i), fileLevelGSMeasure, viewNameDotFieldName);
				addGSMeasures(corpusLevelGSMeasure, fileLevelGSMeasure);
			}
*/			
			double precision_partial = 100.0;
			if (actual_size != 0)
			{
				precision_partial = (corpusLevelGSMeasure.exact+corpusLevelGSMeasure.partial_score)*100.0/(double)actual_size;
			} 
			
			double recall_partial = 100.0;
			if (expected_size != 0)
			{
				recall_partial = (corpusLevelGSMeasure.exact+corpusLevelGSMeasure.partial_score)*100.0/(double)expected_size;
			} 
			
			double fmeasure_partial = 0.0;
			if (precision_partial+recall_partial != 0)
			{
				fmeasure_partial = 2*precision_partial*recall_partial/(precision_partial+recall_partial);
			}
			gsMeasures = new double[]{round(precision_partial), round(recall_partial), round(fmeasure_partial)};
			cache_partialGSMeasure.put(viewNameDotFieldName, gsMeasures);	
		}
		return gsMeasures;
	}

	private double[] getGSMeasures_Relaxed(String viewNameDotFieldName) {
		double[] gsMeasures = cache_relaxedGSMeasure.get(viewNameDotFieldName);
		if(gsMeasures == null){
			GSMeasure corpusLevelGSMeasure = new GSMeasure();
			Iterator<String> iter = superSetOfFileNames.iterator();
			String fileName = null;
			while (iter.hasNext())
			{
				fileName = iter.next();
				GSMeasure fileLevelGSMeasure = new GSMeasure();
				calculateExactSpans(fileName, fileName, fileLevelGSMeasure, viewNameDotFieldName);
				calculatePartialSpans(fileName,fileName, fileLevelGSMeasure, viewNameDotFieldName);
				addGSMeasures(corpusLevelGSMeasure, fileLevelGSMeasure);
			}
/*			for(int i=0;i<sortedFileNames_expected.size();++i){
				GSMeasure fileLevelGSMeasure = new GSMeasure();
				calculateExactSpans(sortedFileNames_expected.get(i), sortedFileNames_actual.get(i), fileLevelGSMeasure, viewNameDotFieldName);
				calculatePartialSpans(sortedFileNames_expected.get(i), sortedFileNames_actual.get(i), fileLevelGSMeasure, viewNameDotFieldName);
				addGSMeasures(corpusLevelGSMeasure, fileLevelGSMeasure);
			}
*/			double precision_relaxed = 100.0;
			if (actual_size != 0) {
				precision_relaxed = (corpusLevelGSMeasure.exact+corpusLevelGSMeasure.partial)*100.0/(double)actual_size;
			} 
			
			double recall_relaxed = 100.0;
			if (expected_size != 0) {
				recall_relaxed = (corpusLevelGSMeasure.exact+corpusLevelGSMeasure.partial)*100.0/(double)expected_size;
			} 
			
			double fmeasure_relaxed = 0.0;
			if (precision_relaxed+recall_relaxed != 0) {
				fmeasure_relaxed = 2*precision_relaxed*recall_relaxed/(precision_relaxed+recall_relaxed);
			} 
			gsMeasures = new double[]{round(precision_relaxed), round(recall_relaxed), round(fmeasure_relaxed)};
			cache_relaxedGSMeasure.put(viewNameDotFieldName, gsMeasures);
		}
		return gsMeasures;
	}

	private double[] getGSMeasures_Exact(String viewNameDotFieldName) {
		double[] gsMeasures = cache_exactGSMeasure.get(viewNameDotFieldName);
		if(gsMeasures == null){
			GSMeasure corpusLevelGSMeasure = new GSMeasure();
			Iterator<String> iter = superSetOfFileNames.iterator();
			String fileName = null;
			while (iter.hasNext())
			{
				fileName = iter.next();
				GSMeasure fileLevelGSMeasure = new GSMeasure();
				calculateExactSpans(fileName, fileName, fileLevelGSMeasure, viewNameDotFieldName);
				addGSMeasures(corpusLevelGSMeasure, fileLevelGSMeasure);
			}
/*			for(int i=0;i<sortedFileNames_expected.size();++i){
				GSMeasure fileLevelGSMeasure = new GSMeasure();
				calculateExactSpans(sortedFileNames_expected.get(i), sortedFileNames_actual.get(i), fileLevelGSMeasure, viewNameDotFieldName);
				addGSMeasures(corpusLevelGSMeasure, fileLevelGSMeasure);
			}
*/			double precision_exact = 100.0;
			if (actual_size !=0)
			{	
				precision_exact = corpusLevelGSMeasure.exact*100.0/(double)actual_size;
			}
			
			double recall_exact = 100.0;
			if (expected_size != 0)
			{
				recall_exact = corpusLevelGSMeasure.exact*100.0/(double)expected_size;
			} 
			
			double fmeasure_exact = 0.0;
			if (precision_exact+recall_exact !=0)
			{
				fmeasure_exact = 2*precision_exact*recall_exact/(precision_exact+recall_exact);
			}
			gsMeasures = new double[]{round(precision_exact), round(recall_exact), round(fmeasure_exact)};
			cache_exactGSMeasure.put(viewNameDotFieldName, gsMeasures);
		}
		return gsMeasures;
	}

	private int calculateExpectedSize(String viewNameDotFieldName) {
		int expectedSize = 0;
		
		Iterator<String> iter = superSetOfFileNames.iterator();
		String fileName = null;
		while (iter.hasNext())
		{
			fileName = iter.next();
			SystemTComputationResult model = modelMap_expected.get(fileName);
			expectedSize += calculateSize(viewNameDotFieldName, model);
		}
		return expectedSize;
	}

	private int calculateActualSize(String viewNameDotFieldName) {
		int actualSize = 0;
		Iterator<String> iter = superSetOfFileNames.iterator();
		String fileName = null;
		while (iter.hasNext())
		{
			fileName = iter.next();
			SystemTComputationResult model = modelMap_actual.get(fileName);
			actualSize += calculateSize(viewNameDotFieldName, model);
		}
		return actualSize;
	}
	
	private int calculateSize(String viewNameDotFieldName, SystemTComputationResult model){
	  // viewNameDotFieldName will be of the format moduleName.viewName.fieldName hence
    // splitting with the .lastIndexOf(".").
		int indexOfDot = viewNameDotFieldName.lastIndexOf('.');
		String viewName = viewNameDotFieldName.substring(0, indexOfDot);
		String fieldName = viewNameDotFieldName.substring(indexOfDot+1);
		if (model == null)
		{
			return 0;
		}
		GoldStandardModel gsModel = new GoldStandardModel(model);
		OutputViewModel view = gsModel.getOutputViewByViewName(viewName);
		if (view == null)
		{
			return 0;
		}
		int fieldIndex = gsModel.getFieldNameIndex(view.getFieldNames(), fieldName);
		OutputViewRow[] rows = view.getRows();
		int size = 0;
		for (OutputViewRow row : rows) {
			if(row.fieldValues != null && row.fieldValues.length > fieldIndex){
				size ++;
			}
		}
		return size;
	}
	
	private void calculatePartialScore(String fileName_expected, String fileName_actual,
			GSMeasure fileLevelGSMeasure, String viewNameDotFieldName) {
		DocSpanDifference dsd = getDocSpanDifference(fileName_expected, fileName_actual, viewNameDotFieldName);
		if(dsd == null || dsd.getActualPartial() == null || dsd.getExpectedPartial()== null){
			fileLevelGSMeasure.partial_score = 0.0;
		}else{
			SystemTComputationResult modAct = modelMap_actual.get(fileName_actual);
			SystemTComputationResult modExp = modelMap_expected.get(fileName_expected);
			if ((modAct ==null) || (modExp == null))
			{
				fileLevelGSMeasure.partial_score = 0.0;
				return;
			}
			List<Integer> actualPartial = dsd.getActualPartial();
			List<Integer> expectedPartial = dsd.getExpectedPartial();

		  // viewNameDotFieldName will be of the format moduleName.viewName.fieldName hence
	    // splitting with the .lastIndexOf(".").
			int indexOfDot = viewNameDotFieldName.lastIndexOf('.');
			String viewName = viewNameDotFieldName.substring(0, indexOfDot);
			String fieldName = viewNameDotFieldName.substring(indexOfDot+1);

			GoldStandardModel modelActual = new GoldStandardModel(modAct);
			GoldStandardModel modelExpected = new GoldStandardModel(modExp);
			OutputViewModel viewActual = modelActual.getOutputViewByViewName(viewName);
			OutputViewModel viewExpected = modelExpected.getOutputViewByViewName(viewName);
			int expectedFieldIndex = -1;
			if (viewExpected != null)
			{
				expectedFieldIndex = GoldStandardUtil.getFieldIndex(viewExpected.getFieldNames(), fieldName);
			}
			int actualFieldIndex = -1;
			if (viewActual != null)
			{
				actualFieldIndex = GoldStandardUtil.getFieldIndex(viewActual.getFieldNames(), fieldName);
			}
			if ((expectedFieldIndex == -1) || (actualFieldIndex == -1))
			{
				return ;
			}
			for (int i=0;i<actualPartial.size();++i){
				int idxActual = actualPartial.get(i).intValue();
				int idxExpected = expectedPartial.get(i).intValue();
				OutputViewRow rowActual = viewActual.getRows()[idxActual];
				OutputViewRow rowExpected = viewExpected.getRows()[idxExpected];
				SpanVal spanValActual = (SpanVal) rowActual.fieldValues[actualFieldIndex];
				SpanVal spanValExpected = (SpanVal) rowExpected.fieldValues[expectedFieldIndex];
				
				int lengthActual = spanValActual.end - spanValActual.start;
				int lengthExpected = spanValExpected.end - spanValExpected.start;
				int maxLen = (lengthActual >= lengthExpected) ? lengthActual : lengthExpected;
				
				int greaterOfStartIndex = (spanValActual.start >= spanValExpected.start) ? spanValActual.start : spanValExpected.start;
				int lesserOfEndIndex = (spanValActual.end <= spanValExpected.end) ? spanValActual.end : spanValExpected.end;
				
				int overlapping = lesserOfEndIndex - greaterOfStartIndex;
				fileLevelGSMeasure.partial_score = (double)overlapping/(double)maxLen; 
			}
		}
		
	}
	private void calculatePartialSpans(String fileName_expected, String fileName_actual,
			GSMeasure fileLevelGSMeasure, String viewNameDotFieldName) {
		String key = concatenate(fileName_actual, fileName_expected, viewNameDotFieldName, ";");
		Integer partialSpanCount = cache_partialSpanCount.get(key);
		if(partialSpanCount == null){
			DocSpanDifference dsd = getDocSpanDifference(fileName_expected, fileName_actual, viewNameDotFieldName);
			if(dsd == null || dsd.getActualPartial() == null){
				fileLevelGSMeasure.partial = 0;	
			}else{
				fileLevelGSMeasure.partial = dsd.getActualPartial().size();			
			}
			cache_partialSpanCount.put(key, new Integer(fileLevelGSMeasure.partial));
		}else{
			fileLevelGSMeasure.partial = partialSpanCount.intValue();
		}
	}

	/**
	 * Calculates exact span count and stores in output parameter fileLevelGSMeasure
	 * @param fileName_expected
	 * 		Name of the file containing expected spans
	 * @param fileName_actual
	 * 		Name of the file containing actual spans
	 * @param fileLevelGSMeasure
	 * 		Output parameter. File level GS measure (exact) is stored in the output parameter
	 * @param viewNameDotFieldName
	 */
	private void calculateExactSpans(String fileName_expected, String fileName_actual,
			GSMeasure fileLevelGSMeasure, String viewNameDotFieldName) {
		String key = concatenate(fileName_actual, fileName_expected, viewNameDotFieldName, ";");
		Integer exactSpanCount = cache_exactSpanCount.get(key);
		if(exactSpanCount == null){
			DocSpanDifference dsd = getDocSpanDifference(fileName_expected, fileName_actual, viewNameDotFieldName);
			if(dsd == null || dsd.getActualCorrect() == null){
				fileLevelGSMeasure.exact = 0;	
			}else{
				fileLevelGSMeasure.exact = dsd.getActualCorrect().size();			
			}
			cache_exactSpanCount.put(key, new Integer(fileLevelGSMeasure.exact));
		}else{
			fileLevelGSMeasure.exact = exactSpanCount.intValue();
		}

	}
	
	

	private DocSpanDifference getDocSpanDifference(String fileName_expected,
			String fileName_actual, String viewNameDotFieldName) {
		
		String key = concatenate(fileName_actual, fileName_expected, viewNameDotFieldName, ";");
		DocSpanDifference dsd = cache_DocSpanDiff.get(key);
		if(dsd == null){
		  // viewNameDotFieldName will be of the format moduleName.viewName.fieldName hence
	    // splitting with the .lastIndexOf(".").
			int indexOfDot = viewNameDotFieldName.lastIndexOf('.');
			String viewName = viewNameDotFieldName.substring(0, indexOfDot);
			String fieldName = viewNameDotFieldName.substring(indexOfDot+1);
			
			SystemTComputationResult modelExpected = modelMap_expected.get(fileName_expected);
			SystemTComputationResult modelActual = modelMap_actual.get(fileName_actual);

			dsd = DocSpanDifference.computeDifference(modelExpected, modelActual, viewName, fieldName, fieldName);
			cache_DocSpanDiff.put(key, dsd);
		}
		return dsd;
	}

	private String concatenate(String fileName_actual,
			String fileName_expected, String viewNameDotFieldName, String separator) {
		StringBuilder buf = new StringBuilder(fileName_actual);
		buf.append(separator);
		buf.append(fileName_expected);
		buf.append(separator);
		buf.append(viewNameDotFieldName);
		return buf.toString();
	}

	/**
	 * Add gs1 and gs2 as per the following formula
	 * gs1 = gs1 + gs2
	 * @param gs1
	 * @param gs2
	 */
	private void addGSMeasures(GSMeasure gs1, GSMeasure gs2) {
		gs1.exact = gs1.exact + gs2.exact;
		gs1.partial = gs1.partial + gs2.partial;
		gs1.partial_score = gs1.partial_score + gs2.partial_score;
	}
	
	private double round(double value){
		try {
			return Double.valueOf(twoDecimalFormat.format(value));
		} catch (Exception e) {
			return Double.NaN;
		}
	}
	//////////////////////////// PRIVATE INNER CLASS //////////////////////////////////////////
	private class GSMeasure{
		int exact; //a.k.a correct
		int partial;
		double partial_score;
	}
}
