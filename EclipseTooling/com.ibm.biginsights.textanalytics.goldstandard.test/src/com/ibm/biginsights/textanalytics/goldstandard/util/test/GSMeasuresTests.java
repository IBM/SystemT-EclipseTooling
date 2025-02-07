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
package com.ibm.biginsights.textanalytics.goldstandard.util.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ibm.biginsights.textanalytics.resultdifferences.gsmeasures.GSMeasureCalculator;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.Constants.GoldStandardType;

/**
 * Tests that the GSMeasures like precision, recall and f-measure are correct when tested with 
 * various scenarios of LC and Result folders
 * 
 *
 */
public class GSMeasuresTests {
	
	private static final String BASE_DIR = "./testData/GSMeasuresTest";
	private static final String LC_BASE_DIR_MODULAR = BASE_DIR + "/labeledCollections/modular";
	private static final String LC_BASE_DIR_NONMODULAR = BASE_DIR + "/labeledCollections/non_modular";
	private static final String RESULT_BASE_DIR_MODULAR = BASE_DIR + "/result/modular";
	private static final String RESULT_BASE_DIR_NONMODULAR = BASE_DIR + "/result/non_modular";
	
	private static final String LC_ADDL_ANN_DIR_MODULAR = LC_BASE_DIR_MODULAR + "/lc-additional-annotations";
  private static final String LC_ALL_NUMBERS_DIR_MODULAR = LC_BASE_DIR_MODULAR + "/lc-all-numbers";
  private static final String LC_LESSER_ANN_DIR_MODULAR = LC_BASE_DIR_MODULAR + "/lc-lesser-annotations";
  private static final String LC_OVERLAPS_DIR_MODULAR = LC_BASE_DIR_MODULAR + "/lc-overlaps";
  
  private static final String LC_ADDL_ANN_DIR_NONMODULAR = LC_BASE_DIR_NONMODULAR + "/lc-additional-annotations";
  private static final String LC_ALL_NUMBERS_DIR_NONMODULAR = LC_BASE_DIR_NONMODULAR + "/lc-all-numbers";
  private static final String LC_LESSER_ANN_DIR_NONMODULAR = LC_BASE_DIR_NONMODULAR + "/lc-lesser-annotations";
  private static final String LC_OVERLAPS_DIR_NONMODULAR = LC_BASE_DIR_NONMODULAR + "/lc-overlaps";
	
	private static final String RESULT_ALL_NUMBERS_DIR_MODULAR = RESULT_BASE_DIR_MODULAR + "/result-all-numbers";
	private static final String RESULT_LESSER_ANN_DIR_MODULAR = RESULT_BASE_DIR_MODULAR + "/result-lesser-annotations";
	  
	private static final String RESULT_ALL_NUMBERS_DIR_NONMODULAR = RESULT_BASE_DIR_NONMODULAR + "/result-all-numbers";
	private static final String RESULT_LESSER_ANN_DIR_NONMODULAR = RESULT_BASE_DIR_NONMODULAR + "/result-lesser-annotations";
	
	private String NON_MODULAR_QUALIFIED_FIELDNAME="Numbers.num";
	private String MODULAR_QUALIFIED_FIELDNAME="GSModule.Numbers.num";
	
	
	/**
	 * Verifies the correctness of precision_exact, recall_exact and fmeasure_exact
	 * @param qualifiedFieldName - For views in modular projects that do not have an alias, the parameter 
	 * should be of the form moduleName.viewName.fieldName
	 * For views in modular projects that have an alias name assigned, the parameter should be of the 
	 * form viewAliasName.fieldName
	 * For views in non-modular projects, the parameters should be of the form viewName.fieldName
	 * @param gsCalculator
	 * @param expectedMeasures
	 */
	private void genericExactMeasureTest(GSMeasureCalculator gsCalculator, double[] expectedMeasures, String qualifiedFieldName){
		double[] actualMeasures = gsCalculator.getGSMeasures(qualifiedFieldName, GoldStandardType.Exact);
		assertTrue(Arrays.equals(expectedMeasures, actualMeasures));
	}
	
	/**
	 * Verifies the correctness of precision_partial, recall_partial and fmeasure_partial
	 * @param qualifiedFieldName - For views in modular projects that do not have an alias, the parameter 
   * should be of the form moduleName.viewName.fieldName
   * For views in modular projects that have an alias name assigned, the parameter should be of the 
   * form viewAliasName.fieldName
   * For views in non-modular projects, the parameters should be of the form viewName.fieldName
	 * @param gsCalculator
	 * @param expectedMeasures
	 */
	private void genericPartialMeasureTest(GSMeasureCalculator gsCalculator, double[] expectedMeasures, String qualifiedFieldName){
		double[] actualMeasures = gsCalculator.getGSMeasures(qualifiedFieldName, GoldStandardType.Partial);
		assertTrue(Arrays.equals(expectedMeasures, actualMeasures));
	}
	
	/**
	 * Verifies the correctness of precision_relaxed, recall_relaxed and fmeasure_relaxed
	 * @param qualifiedFieldName - For views in modular projects that do not have an alias, the parameter 
   * should be of the form moduleName.viewName.fieldName
   * For views in modular projects that have an alias name assigned, the parameter should be of the 
   * form viewAliasName.fieldName
   * For views in non-modular projects, the parameters should be of the form viewName.fieldName
	 * @param gsCalculator
	 * @param expectedMeasures
	 */
	private void genericRelaxedMeasureTest(GSMeasureCalculator gsCalculator, double[] expectedMeasures, String qualifiedFieldName){
		double[] actualMeasures = gsCalculator.getGSMeasures(qualifiedFieldName, GoldStandardType.Relaxed);
		assertTrue(Arrays.equals(expectedMeasures, actualMeasures));
	}
	
	/**
	 * Generic test method to check correctness of precision, recall and fmeasure values. 
	 * Invoked by individual test cases
	 * @param qualifiedFieldName - For views in modular projects that do not have an alias, the parameter 
   * should be of the form moduleName.viewName.fieldName
   * For views in modular projects that have an alias name assigned, the parameter should be of the 
   * form viewAliasName.fieldName
   * For views in non-modular projects, the parameters should be of the form viewName.fieldName
	 * @param lcFolder
	 * @param resultFolder
	 * @param exactMeasures
	 * @param partialMeasures
	 * @param relaxedMeasures
	 * @throws Exception
	 */
	private void genericGSMeasuresTest(String lcFolder, String resultFolder,
			double[] exactMeasures, double[] partialMeasures, double[] relaxedMeasures, String qualifiedFieldName) throws Exception{
		Serializer ser = new Serializer();
		
		Map<String, SystemTComputationResult> lcModelMap = new HashMap<String, SystemTComputationResult>();
		Map<String, SystemTComputationResult> resultModelMap = new HashMap<String, SystemTComputationResult>();
		
		//load lc models
		File lcDir = new File(lcFolder);
		File[] lcFiles = lcDir.listFiles(new FileFilter("lc"));
		for (File lcFile : lcFiles) {
			SystemTComputationResult lcModel = ser.getModelForInputStream(new FileInputStream(lcFile));
			lcModelMap.put(removeExtn(lcFile.getName()), lcModel);
			lcFolder = lcFile.getParent();
		}
		
		//load result models
		File resultDir = new File(resultFolder);
		File[] resultFiles = resultDir.listFiles(new FileFilter("strf"));
		for (File resultFile : resultFiles) {
			SystemTComputationResult resultModel = ser.getModelForInputStream(new FileInputStream(resultFile));
			resultModelMap.put(removeExtn(resultFile.getName()), resultModel);
		}
		ArrayList<String> superSetOfFileNames = getSuperSetNamesInBothFolders(lcFiles, resultFiles);
		GSMeasureCalculator gsCalculator = new GSMeasureCalculator(lcModelMap, resultModelMap,superSetOfFileNames);
		
		genericExactMeasureTest(gsCalculator, exactMeasures, qualifiedFieldName);
		genericPartialMeasureTest(gsCalculator, partialMeasures, qualifiedFieldName);
		genericRelaxedMeasureTest(gsCalculator, relaxedMeasures, qualifiedFieldName);
	}
	
	
	private ArrayList<String> getSuperSetNamesInBothFolders(File[] lcFiles, File[] resultFiles)
	{
		ArrayList<String> supersetnames = new ArrayList<String>();
		for (int i = 0;i<lcFiles.length;i++)
		{
			String lcFileName = lcFiles[i].getName();
			lcFileName = removeExtn(lcFileName);
			supersetnames.add(lcFileName);
		}
		
		for (int i = 0;i<resultFiles.length;i++)
		{
			String resultFileName = resultFiles[i].getName();
			resultFileName = removeExtn(resultFileName);
			if (supersetnames.contains(resultFileName) == false)
			{
				supersetnames.add(resultFileName);
			}
		}
		return supersetnames;
	}
	
	
	private String removeExtn(String fileName)
	{
		fileName = fileName.replaceAll(Constants.STRF_FILE_EXTENSION_WITH_DOT, "");
		fileName = fileName.replaceAll(Constants.GS_FILE_EXTENSION_WITH_DOT,"");
		return fileName;
	}
	
	/**
	 * Generic test method to check if LC and Result folder are equivalent
   * Invoked by modular and non_modular test cases
	 * @throws Exception
	 */
	private void lcEqualsResultTest(String lcFolder, String resultFolder, String viewNameDotFieldName)throws Exception{
		double[] exactMeasures = new double[] {100.0, 100.0, 100.0};
		double[] partialMeasures = new double[] {100.0, 100.0, 100.0};
		double[] relaxedMeasures = new double[] {100.0, 100.0, 100.0};
		genericGSMeasuresTest(lcFolder, resultFolder, exactMeasures, partialMeasures, relaxedMeasures, viewNameDotFieldName);
	}
	
	/**
	 * Generic test method to check if the result misses 2 annotations
   * Invoked by modular and non_modular test cases
   * @throws Exception
   */
  private void missingInResultTest(String lcFolder, String resultFolder, String viewNameDotFieldName) throws Exception{
    double[] exactMeasures = new double[] {100.0, 83.33, 90.91};
    double[] partialMeasures = new double[] {100.0, 83.33, 90.91};
    double[] relaxedMeasures = new double[] {100.0, 83.33, 90.91};
    genericGSMeasuresTest(lcFolder, resultFolder, exactMeasures, partialMeasures, relaxedMeasures, viewNameDotFieldName);
  }
  
  /**
   * Generic test method to check if few SPANs in LC and result overlap with each other.
   * Invoked by modular and non_modular test cases
   * @throws Exception
   */
  private void overlappingAnnotationsTest(String lcFolder, String resultFolder, String viewNameDotFieldName) throws Exception{
    double[] exactMeasures = new double[] {80.0, 80.0, 80.0};
    double[] partialMeasures = new double[] {91.67, 91.67, 91.67};
    double[] relaxedMeasures = new double[] {100.0, 100.0, 100.0};
    genericGSMeasuresTest(lcFolder, resultFolder, exactMeasures, partialMeasures, relaxedMeasures, viewNameDotFieldName);
  }
  
  /**
   * Generic test method to check if the result has 2 extra annotations than LC.
   * Invoked by modular and non_modular test cases
   * @throws Exception
   */
  private void spuriousAnnotationsInResultTest(String lcFolder, String resultFolder, String viewNameDotFieldName) throws Exception{
    double[] exactMeasures = new double[] {80.0, 100.0, 88.89};
    double[] partialMeasures = new double[] {80.0, 100.0, 88.89};
    double[] relaxedMeasures = new double[] {80.0, 100.0, 88.89};
    genericGSMeasuresTest(lcFolder, resultFolder, exactMeasures, partialMeasures, relaxedMeasures, viewNameDotFieldName);
  }
  
  /**
   * Generic test method to check if there are few missing annotations in result; Few overlapping with LC
   * Invoked by modular and non_modular test cases
   * @throws Exception
   */
  private void lesserAnnotationsInResultAndOverlappingWithLC(String lcFolder, String resultFolder, String viewNameDotFieldName) throws Exception{
    double[] exactMeasures = new double[] {75.0, 60.0, 66.67};
    double[] partialMeasures = new double[] {89.58, 71.67, 79.63};
    double[] relaxedMeasures = new double[] {100.0, 80.0, 88.89};
    genericGSMeasuresTest(lcFolder, resultFolder, exactMeasures, partialMeasures, relaxedMeasures, viewNameDotFieldName);
  }
	
	 /**
   * Scenario: LC and Result folder are equivalent for non_modular results
   * LC has 10 annotations
   * Result has 10 matching annotations
   * 
   * @throws Exception
   */
  @Test
  public void lcEqualsResultNonModularTest()throws Exception{
    String lcFolder = LC_ALL_NUMBERS_DIR_NONMODULAR;
    String resultFolder = RESULT_ALL_NUMBERS_DIR_NONMODULAR;
    lcEqualsResultTest (lcFolder, resultFolder, NON_MODULAR_QUALIFIED_FIELDNAME);
  }
  
  /**
   * Scenario: Result misses 2 annotations for non_modular results
   * LC has 12 annotations
   * Result has 10 exactly matching annotations
   * 
   * @throws Exception
   */
  @Test
  public void missingInResultNonModularTest() throws Exception{
    String lcFolder = LC_ADDL_ANN_DIR_NONMODULAR;
    String resultFolder = RESULT_ALL_NUMBERS_DIR_NONMODULAR;
    missingInResultTest (lcFolder, resultFolder, NON_MODULAR_QUALIFIED_FIELDNAME);
  }
  
  /**
   * Scenario: Few SPANs in LC and result overlap with each other for non_modular results. Both have 10 annotations each.
   * LC has 10 annotations
   * Result has 10 annotations; 8 of them are exact matches with LC; 2 of them overlapping with LC
   * partial score (for overlap) = 1.167
   * 
   * @throws Exception
   */
  @Test
  public void overlappingAnnotationsNonModularTest() throws Exception{
    String lcFolder = LC_OVERLAPS_DIR_NONMODULAR;
    String resultFolder = RESULT_ALL_NUMBERS_DIR_NONMODULAR;
    overlappingAnnotationsTest (lcFolder, resultFolder, NON_MODULAR_QUALIFIED_FIELDNAME);
  }
  
  /**
   * Scenario: Result has 2 extra annotations than LC for non_modular results
   * LC has 8 annotations
   * Result has 10 annotations; 8 of them match exactly with LC; 2 extra. 
   * 
   * @throws Exception
   */
  @Test
  public void spuriousAnnotationsInResultNonModularTest() throws Exception{
    String lcFolder = LC_LESSER_ANN_DIR_NONMODULAR;
    String resultFolder = RESULT_ALL_NUMBERS_DIR_NONMODULAR;
    spuriousAnnotationsInResultTest (lcFolder, resultFolder, NON_MODULAR_QUALIFIED_FIELDNAME);
  }
  
  /**
   * Scenario: Few missing annotations in result; Few overlapping with LC, Tests non_modular results
   * LC has 10 annotations
   * Result has 8 annotations (i.e 2 missing). Out of the 8 annotations in result, 6 are exact matches; 2 of them overlap with LC
   * @throws Exception
   */
  @Test
  public void lesserAnnotationsInResultAndOverlappingWithLCNonModularTest() throws Exception{
    String lcFolder = LC_OVERLAPS_DIR_NONMODULAR;
    String resultFolder = RESULT_LESSER_ANN_DIR_NONMODULAR;
    lesserAnnotationsInResultAndOverlappingWithLC (lcFolder, resultFolder, NON_MODULAR_QUALIFIED_FIELDNAME);
  }
  
  /**
   * Scenario: LC and Result folder are equivalent for modular results
   * LC has 10 annotations
   * Result has 10 matching annotations
   * 
   * @throws Exception
   */
  @Test
  public void lcEqualsResultModularTest()throws Exception{
    String lcFolder = LC_ALL_NUMBERS_DIR_MODULAR;
    String resultFolder = RESULT_ALL_NUMBERS_DIR_MODULAR;
    lcEqualsResultTest (lcFolder, resultFolder, MODULAR_QUALIFIED_FIELDNAME);
  }
	
  /**
   * Scenario: Result misses 2 annotations for modular results
   * LC has 12 annotations
   * Result has 10 exactly matching annotations
   * 
   * @throws Exception
   */
  @Test
  public void missingInResultModularTest() throws Exception{
    String lcFolder = LC_ADDL_ANN_DIR_MODULAR;
    String resultFolder = RESULT_ALL_NUMBERS_DIR_MODULAR;
    missingInResultTest (lcFolder, resultFolder, MODULAR_QUALIFIED_FIELDNAME);
  }
	
	/**
   * Scenario: Few SPANs in LC and result overlap with each other for modular results. Both have 10 annotations each.
   * LC has 10 annotations
   * Result has 10 annotations; 8 of them are exact matches with LC; 2 of them overlapping with LC
   * partial score (for overlap) = 1.167
   * 
   * @throws Exception
   */
  @Test
  public void overlappingAnnotationsModularTest() throws Exception{
    String lcFolder = LC_OVERLAPS_DIR_MODULAR;
    String resultFolder = RESULT_ALL_NUMBERS_DIR_MODULAR;
    overlappingAnnotationsTest (lcFolder, resultFolder, MODULAR_QUALIFIED_FIELDNAME);
  }
	
	/**
   * Scenario: Result has 2 extra annotations than LC for modular results
   * LC has 8 annotations
   * Result has 10 annotations; 8 of them match exactly with LC; 2 extra. 
   * 
   * @throws Exception
   */
  @Test
  public void spuriousAnnotationsInResultModularTest() throws Exception{
    String lcFolder = LC_LESSER_ANN_DIR_MODULAR;
    String resultFolder = RESULT_ALL_NUMBERS_DIR_MODULAR;
    spuriousAnnotationsInResultTest (lcFolder, resultFolder, MODULAR_QUALIFIED_FIELDNAME);
  }
    
  /**
   * Scenario: Few missing annotations in result; Few overlapping with LC, Tests modular results
   * LC has 10 annotations
   * Result has 8 annotations (i.e 2 missing). Out of the 8 annotations in result, 6 are exact matches; 2 of them overlap with LC
   * @throws Exception
   */
  @Test
  public void lesserAnnotationsInResultAndOverlappingWithLC_Modular() throws Exception{
    String lcFolder = LC_OVERLAPS_DIR_MODULAR;
    String resultFolder = RESULT_LESSER_ANN_DIR_MODULAR;
    lesserAnnotationsInResultAndOverlappingWithLC (lcFolder, resultFolder, MODULAR_QUALIFIED_FIELDNAME);
  }
	
	/**
	 * Filters the files based on extension 
	 * 
	 *
	 */
	private class FileFilter implements FilenameFilter{
		
		String ext;
	
		public FileFilter(String ext) {
			super();
			this.ext = ext;
		}

		@Override
		public boolean accept(File dir, String name) {
			return name.endsWith(ext);
		}
		
	}
}
