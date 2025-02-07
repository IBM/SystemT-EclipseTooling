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

package com.ibm.biginsights.textanalytics.resultdifferences.compute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

import com.ibm.biginsights.textanalytics.resultdifferences.Activator;
import com.ibm.biginsights.textanalytics.resultdifferences.Messages;
import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.gsmeasures.GSMeasureCalculator;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.model.Util;
import com.ibm.biginsights.textanalytics.util.common.Constants.GoldStandardType;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This is the main class that is "registered" with the UI views used for
 * computation of the differences count and spans.
 */
public class DifferencesComputer {



	private RunResultDiff rrd;
	private Map<String, SystemTComputationResult> expectedFileModelMap = new HashMap<String, SystemTComputationResult>();
	private Map<String, SystemTComputationResult> actualFileModelMap = new HashMap<String, SystemTComputationResult>();
	private Map<String, DocSpanDifference> dsdMap = new HashMap<String, DocSpanDifference>();
	private static HashMap<String, DifferencesComputer> computers = new HashMap<String, DifferencesComputer>();
	private ArrayList<String> modelNullFiles = new ArrayList<String>();
	private ArrayList<String> superSetFileNames = new ArrayList<String>();

	/**
	 * Caches the GSMeasureCalculator object for a given expectedFolder,
	 * rightFolder combination, so that only one calculator object is used for
	 * multiple viewNameDotFieldNames. Key: expectedFolder+";"+rightFolder
	 * Value: GSMeasureCalculator object for this key
	 */
	private static HashMap<String, GSMeasureCalculator> gsCalculatorMap = new HashMap<String, GSMeasureCalculator>();

	/**
	 * Key for gsCalculatorMap. Format: expectedFolder+";"+rightFolder
	 */
	private String gsCalculatorKey;

	// Read all the result folders and compute models and store them in a
	// hashmap
	// So that reading of files need not be done everytime
	private Map<String, SystemTComputationResult> expectedDocIDModelMap = new HashMap<String, SystemTComputationResult>();
	// Read all the result folders and compute models and store them in a
	// hashmap
	// So that reading of files need not be done everytime
	private Map<String, SystemTComputationResult> actualDocIDModelMap = new HashMap<String, SystemTComputationResult>();

	private void clearCache() {
		expectedFileModelMap = new HashMap<String, SystemTComputationResult>();
		actualFileModelMap = new HashMap<String, SystemTComputationResult>();
		dsdMap = new HashMap<String, DocSpanDifference>();
		computers = new HashMap<String, DifferencesComputer>();
		gsCalculatorMap = new HashMap<String, GSMeasureCalculator>();
		expectedDocIDModelMap = new HashMap<String, SystemTComputationResult>();
		actualDocIDModelMap = new HashMap<String, SystemTComputationResult>();
		ResultDifferencesUtil.clearCache();
	}

	/*
	 * Private constructor to control the number of instances of Differences
	 * Computer. This is because this is a heavy class and reads in all the
	 * result files. and computes heavy comparison functions.
	 */

	private DifferencesComputer(IFolder expectedFolder, IFolder rightFolder) {

		gsCalculatorKey = calculateKey(expectedFolder, rightFolder);
		// clear the cached entry
		gsCalculatorMap.remove(gsCalculatorKey);

		try {
			if (expectedFolder != null) {
				IResource[] resources = expectedFolder.members();
				SystemTComputationResult model = null;
				for (IResource resource : resources) {
					if ((resource.getType() == IResource.FILE)
							&& (ResultDifferencesUtil
									.isValidFile((IFile) resource))) {

						model = ResultDifferencesUtil
								.getModelFromSTRFFile((IFile) resource);
						if (model != null) {
							expectedDocIDModelMap.put(model.getDocumentID(),
									model);
							expectedFileModelMap.put(ResultDifferencesUtil
									.removeExtension((IFile) resource), model);
						} else {
							modelNullFiles.add(((IFile) resource).getName());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (modelNullFiles.isEmpty() != true) {
			String msg = Messages
					.getString("DifferencesComputer_INVALID_INPUT");
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowWarning(
					msg + modelNullFiles);
		}
		try {
			if (rightFolder != null) {
				IResource[] resources = rightFolder.members();
				SystemTComputationResult model = null;
				for (IResource resource : resources) {
					if ((resource.getType() == IResource.FILE)
							&& ResultDifferencesUtil
									.isValidFile((IFile) resource)) { //$NON-NLS-1$
						model = ResultDifferencesUtil
								.getModelFromSTRFFile((IFile) resource);
						if (model != null) {
							actualDocIDModelMap.put(model.getDocumentID(),
									model);
							actualFileModelMap.put(ResultDifferencesUtil
									.removeExtension((IFile) resource), model);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		superSetFileNames = ResultDifferencesUtil
				.getSuperSetNamesInBothFolders(expectedFolder, rightFolder);
		// This is used to compute the common viewname.fieldnames
		rrd = new RunResultDiff(expectedDocIDModelMap, actualDocIDModelMap);

	}

	/**
	 * Singleton access to the DifferencesComputer getInstance() is called only
	 * from two places now. 1) From the CollectionDifferencesMainView - This is
	 * when the comparison is launched the first time 2) From the
	 * FileSideBySideDifferencesview - This is the reason we have the computers
	 * cache in the first place so that reading need not be done again.
	 * 
	 * @param expectedFolder
	 * @param rightFolder
	 * @return
	 */

	public static DifferencesComputer getInstance(IFolder expectedFolder,
			IFolder rightFolder, boolean callingFirstTime) {
		String expFolderPath = "";
		if (expectedFolder != null) {
			expFolderPath = expectedFolder.getFullPath().toString();
		}
		String actFolderPath = "";
		if (rightFolder != null) {
			actFolderPath = rightFolder.getFullPath().toString();
		}
		String key = expFolderPath + actFolderPath;
		DifferencesComputer cmptr = null;
		if (computers.containsKey(key)) {
			cmptr = computers.get(key);
			if (callingFirstTime) {
				cmptr.clearCache();
			} else {
				return cmptr;
			}
		}
		cmptr = new DifferencesComputer(expectedFolder, rightFolder);
		computers.put(key, cmptr);
		return cmptr;
	}

	/**
	 * get all types which were found in the two analysis runs which are
	 * compared in the result folders This is a string array with
	 * "viewname.fieldname" structure This is the one called from the
	 * CollDiffMainView to display against each document on the right hand side.
	 * 
	 * @return
	 */
	public String[] getDiffTypes() {
		return rrd.getDiffSpanFieldNames();
	}

	/**
	 * Private Utility method to create the objects for computation for two
	 * files and keep them in a hashmap so that compuation need not be done
	 * everytime.
	 * @param expectedFile IFile instance of the expected file
	 * @param actualFile IFile instance of the actual file
	 * @param qualifiedViewNameDotFieldName
	 *            this will be of the form moduleName.viewName.fieldName
	 * @return DocSpanDifference object that contains the encoded difference between two runs for a specific document, view and column.
	 */
	private DocSpanDifference getDocSpanDifference(IFile expectedFile,
			IFile actualFile, String qualifiedViewNameDotFieldName) {
		SystemTComputationResult expectedModel = null;
		String expectedFilePath = "";
		if (expectedFile != null) {
			expectedFilePath = expectedFile.getFullPath().toString();
			expectedModel = expectedFileModelMap.get(ResultDifferencesUtil
					.removeExtension(expectedFile));
		}
		SystemTComputationResult actualModel = null;
		String actualFilePath = "";
		if (actualFile != null) {
			actualFilePath = actualFile.getFullPath().toString();
			actualModel = actualFileModelMap.get(ResultDifferencesUtil
					.removeExtension(actualFile));
		}
		// qualifiedViewNameDotFieldName will be of the format moduleName.viewName.fieldName hence
		// splitting with the .lastIndexOf(".")
		int index = qualifiedViewNameDotFieldName.lastIndexOf("."); //$NON-NLS-1$
		String qualifiedViewName = qualifiedViewNameDotFieldName.substring(0,
				index);
		String fieldName = qualifiedViewNameDotFieldName.substring(index + 1);
		String key = expectedFilePath + actualFilePath
				+ qualifiedViewNameDotFieldName;
		DocSpanDifference dsd = null;
		if (dsdMap.containsKey(key)) {
			dsd = dsdMap.get(key);
		} else {
			dsd = DocSpanDifference.computeDifference(expectedModel,
					actualModel, qualifiedViewName, fieldName, fieldName);
			dsdMap.put(key, dsd);
		}
		return dsd;
	}

	/**
	 * Returns a list of SpanVal objects for the given list of row IDs.
	 * 
	 * @param results
	 *            list of IDs of OutputView rows of the model from expected
	 *            file, if <code>expected</code> is <code>true</code>, or from
	 *            the actual file if <code>expected</code> is <code>false</code>
	 * @param expectedFile IFile instance of the expected file
	 * @param actualFile IFile instance of the actual file
	 * @param qualifiedViewNameDotFieldName
	 *            this will be of the form moduleName.viewName.fieldName
	 * @param expected
	 *            , true if the spans are to be got from the expected file false
	 *            otherwise
	 * @return Arraylist of SpanVal objects.
	 */
	private ArrayList<SpanVal> getSpanValListFromIntegerResultsList(
			List<Integer> results, IFile expectedFile, IFile actualFile,
			String qualifiedViewNameDotFieldName, boolean expected) {
		SystemTComputationResult expectedModel = null;
		if (expectedFile != null) {
			expectedModel = expectedFileModelMap.get(ResultDifferencesUtil
					.removeExtension(expectedFile));
		}
		SystemTComputationResult actualModel = null;
		if (actualFile != null) {
			actualModel = actualFileModelMap.get(ResultDifferencesUtil
					.removeExtension(actualFile));
		}
		// qualifiedViewNameDotFieldName will be of the format moduleName.viewName.fieldName hence
		// splitting with the .lastIndexOf(".").
		int index = qualifiedViewNameDotFieldName.lastIndexOf("."); //$NON-NLS-1$
		String qualifiedViewName = qualifiedViewNameDotFieldName.substring(0,
				index);
		String fieldName = qualifiedViewNameDotFieldName.substring(index + 1);
		SystemTComputationResult modelToBeUsed;
		if (expected) {
			modelToBeUsed = expectedModel;
		} else {
			modelToBeUsed = actualModel;
		}
		ArrayList<SpanVal> spanList = new ArrayList<SpanVal>();
		OutputView view = Util.getViewForName(modelToBeUsed, qualifiedViewName);
		if (view != null) {
			OutputViewRow[] rows = view.getRows();
			if (rows != null) {
				int fieldID = Util.getFieldIdForName(view, fieldName);
				for (Integer resultInteger : results) {
					OutputViewRow row = rows[resultInteger.intValue()];
					spanList.add((SpanVal) row.fieldValues[fieldID]);
				}
			}
		}
		return spanList;

	}

	/**
	 * This method is called from the UI to display changed annotations count
	 * 
	 * @param expectedFile
	 * @param actualFile
	 * @param viewNameDotFieldName
	 * @return
	 */
	public int getChangedAnnotationsCount(IFile expectedFile, IFile actualFile,
			String viewNameDotFieldName) {
		DocSpanDifference dsd = getDocSpanDifference(expectedFile, actualFile,
				viewNameDotFieldName);
		List<Integer> results = dsd.getExpectedPartial();
		return results.size();
	}

	/**
	 * This method is called from FileDifferencesView UI to display the
	 * overlapping annotations in the expected file
	 * 
	 * @param expectedFile
	 * @param actualFile
	 * @param viewNameDotFieldName
	 * @return
	 */
	public ArrayList<SpanVal> getOverlappingSpansInExpectedFile(
			IFile expectedFile, IFile actualFile, String viewNameDotFieldName) {
		DocSpanDifference dsd = getDocSpanDifference(expectedFile, actualFile,
				viewNameDotFieldName);
		List<Integer> results = dsd.getExpectedPartial();
		return getSpanValListFromIntegerResultsList(results, expectedFile,
				actualFile, viewNameDotFieldName, true);

	}

	/**
	 * This method is called from the FileDifferencesView UI to display the
	 * overlapping annotations in the right file.
	 * 
	 * @param expectedFile
	 * @param actualFile
	 * @param viewNameDotFieldName
	 * @return
	 */
	public ArrayList<SpanVal> getOverlappingSpansInActualFile(
			IFile expectedFile, IFile actualFile, String viewNameDotFieldName) {
		DocSpanDifference dsd = getDocSpanDifference(expectedFile, actualFile,
				viewNameDotFieldName);
		List<Integer> results = dsd.getActualPartial();
		return getSpanValListFromIntegerResultsList(results, expectedFile,
				actualFile, viewNameDotFieldName, false);
	}

	/**
	 * This method is called from the FileDifferencesView UI to display the new
	 * annotations count in the right file
	 * 
	 * @param expectedFile
	 * @param actualFile
	 * @param viewNameDotFieldName
	 * @return
	 */
	public int getNewAnnotationsCountInActual(IFile expectedFile,
			IFile actualFile, String viewNameDotFieldName) {
		DocSpanDifference dsd = getDocSpanDifference(expectedFile, actualFile,
				viewNameDotFieldName);
		List<Integer> results = dsd.getActualSpurious();
		return results.size();
	}

	/**
	 * This method is called from the FileDifferencesView UI to display the new
	 * annotations span values in the right file
	 * 
	 * @param expectedFile
	 * @param actualFile
	 * @param viewNameDotFieldName
	 * @return
	 */
	public ArrayList<SpanVal> getNewAnnotationsSpansInActual(
			IFile expectedFile, IFile actualFile, String viewNameDotFieldName) {
		DocSpanDifference dsd = getDocSpanDifference(expectedFile, actualFile,
				viewNameDotFieldName);
		List<Integer> results = dsd.getActualSpurious();
		return getSpanValListFromIntegerResultsList(results, expectedFile,
				actualFile, viewNameDotFieldName, false);
	}

	/**
	 * This method is called from the FileDifferencesView UI to display the old
	 * annotations count in the expected file
	 * 
	 * @param expectedFile
	 * @param actualFile
	 * @param viewNameDotFieldName
	 * @return
	 */
	public int getDeletedAnnotationsCountInExpected(IFile expectedFile,
			IFile actualFile, String viewNameDotFieldName) {
		DocSpanDifference dsd = getDocSpanDifference(expectedFile, actualFile,
				viewNameDotFieldName);
		List<Integer> results = dsd.getExpectedMissing();
		return results.size();
	}

	/**
	 * This method is called from the FileDifferencesView UI to display the old
	 * annotations span values in the expected file
	 * 
	 * @param expectedFile
	 * @param actualFile
	 * @param viewNameDotFieldName
	 * @return
	 */
	public ArrayList<SpanVal> getDeletedAnnotationsSpanInExpected(
			IFile expectedFile, IFile actualFile, String viewNameDotFieldName) {
		DocSpanDifference dsd = getDocSpanDifference(expectedFile, actualFile,
				viewNameDotFieldName);
		List<Integer> results = dsd.getExpectedMissing();
		return getSpanValListFromIntegerResultsList(results, expectedFile,
				actualFile, viewNameDotFieldName, true);
	}

	/**
	 * This method is called from the FileDifferencesView UI to display the
	 * unchanged annotations in the expected and right file
	 * 
	 * @param expectedFile
	 * @param actualFile
	 * @param viewNameDotFieldName
	 * @return
	 */

	public ArrayList<SpanVal> getUnchangedAnnotationsSpanInActualFile(
			IFile expectedFile, IFile actualFile, String viewNameDotFieldName) {
		DocSpanDifference dsd = getDocSpanDifference(expectedFile, actualFile,
				viewNameDotFieldName);
		List<Integer> results = dsd.getActualCorrect();
		return getSpanValListFromIntegerResultsList(results, expectedFile,
				actualFile, viewNameDotFieldName, false);
	}

	/**
	 * This method is called from the FileDifferencesView UI to display the
	 * unchanged annotations in the expected and right file
	 * 
	 * @param expectedFile
	 * @param actualFile
	 * @param viewNameDotFieldName
	 * @return
	 */

	public ArrayList<SpanVal> getUnchangedAnnotationsSpanInExpectedFile(
			IFile expectedFile, IFile actualFile, String viewNameDotFieldName) {
		DocSpanDifference dsd = getDocSpanDifference(expectedFile, actualFile,
				viewNameDotFieldName);
		List<Integer> results = dsd.getExpectedCorrect();
		return getSpanValListFromIntegerResultsList(results, expectedFile,
				actualFile, viewNameDotFieldName, true);
	}

	public int getDiffProcessingTime(int xcasId1, int xcasId2) {
		return 70; // TODO
	}

	public int[] getFileLevelTotalCounts(IFile expectedFile, IFile actualFile) {
		String[] vNameDotFNames = getDiffTypes();
		int[] fileLevelCounts = new int[3];
		int fileNewCount = 0;
		int fileDeleteCount = 0;
		int fileChangedCount = 0;
		for (int i = 0; i < vNameDotFNames.length; i++) {
			fileNewCount += getNewAnnotationsCountInActual(expectedFile,
					actualFile, vNameDotFNames[i]);
			fileDeleteCount += getDeletedAnnotationsCountInExpected(
					expectedFile, actualFile, vNameDotFNames[i]);
			fileChangedCount += getChangedAnnotationsCount(expectedFile,
					actualFile, vNameDotFNames[i]);
		}
		fileLevelCounts[0] = fileNewCount;
		fileLevelCounts[1] = fileDeleteCount;
		fileLevelCounts[2] = fileChangedCount;
		return fileLevelCounts;
	}

	/**
	 * Calculates gold standard measures
	 * 
	 * @param gsType
	 * @param viewNameDotFieldName
	 * @return gold standard measures for exact, partial and relaxed gold
	 *         standard types
	 */
	// Note: Algorithm to calculate gold standard measures is attached to work
	// item 13003 in RTC
	public double[] getGoldStandardMeasures(GoldStandardType gsType,
			String viewNameDotFieldName) {
		GSMeasureCalculator gsCalculator = gsCalculatorMap.get(gsCalculatorKey);
		if (gsCalculator == null) {
			gsCalculator = new GSMeasureCalculator(expectedFileModelMap,
					actualFileModelMap, superSetFileNames);
			gsCalculatorMap.put(gsCalculatorKey, gsCalculator);
		}
		return gsCalculator.getGSMeasures(viewNameDotFieldName, gsType);
	}

	private String calculateKey(IFolder expected, IFolder actual) {
		String expectedFullPath = "";
		String actualFullPath = "";
		if (expected != null) {
			expectedFullPath = expected.getFullPath().toString();
		}
		if (actual != null) {
			actualFullPath = actual.getFullPath().toString();
		}
		return expectedFullPath + ";" + actualFullPath; //$NON-NLS-1$
	}

}
