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

package com.ibm.biginsights.textanalytics.indexer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.indexer.Constants;
import com.ibm.biginsights.textanalytics.indexer.index.TextAnalyticsIndexer;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class IndexerUtilTest
{
  private static final String PERSON_NAME_FILTER_CONSOLIDATE_PERSON_FINAL = "PersonName_FilterConsolidate.PersonFinal";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_FINALS_FINAL_AQL = "/TestWF2/textAnalytics/src/PersonName_Finals/final.aql";//$NON-NLS-1$
  private static final String METRICS_INDICATOR_FEATURES_AMOUNT_ABSOLUTE = "metricsIndicator_features.AmountAbsolute";//$NON-NLS-1$
  private static final String METRICS_INDICATOR_FEATURES_NUMBER = "metricsIndicator_features.Number";//$NON-NLS-1$
  private static final String METRICS_INDICATOR_EXTERNAL_TYPES_EMAIL_METADATA = "metricsIndicator_externalTypes.EmailMetadata";//$NON-NLS-1$
  private static final String PERSON_NAME_CANDIDATE_GENERATION_PERSON = "PersonName_CandidateGeneration.Person";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_FILTER_CONSOLIDATE_REFINEMENT_AQL = "/TestWF2/textAnalytics/src/PersonName_FilterConsolidate/refinement .aql";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_MAIN_MAIN_AQL = "/TestWF2/textAnalytics/src/main/main.aql";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_FEATURES_CONCEPTS_AQL = "/TestWF2/textAnalytics/src/metricsIndicator_features/concepts.aql";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_FEATURES_BASICS_AQL = "/TestWF2/textAnalytics/src/metricsIndicator_features/basics.aql";//$NON-NLS-1$
  private static final String METRICS_INDICATOR_DICTIONARIES_METRICS = "metricsIndicator_dictionaries.metrics";//$NON-NLS-1$
  private static final String METRICS_INDICATOR_UDFS_UDF_TO_UPPER_CASE = "metricsIndicator_udfs.udfToUpperCase";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_MODULE1_TEST_AQL = "/TestWF2/textAnalytics/src/module1/test.aql";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_UDFS_UDFS_AQL = "/TestWF2/textAnalytics/src/metricsIndicator_udfs/udfs.aql";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_EXTERNAL_TYPES_EXTERNAL_VIEW_AQL = "/TestWF2/textAnalytics/src/metricsIndicator_externalTypes/externalView.aql";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_DICTIONARIES_DICTIONARIES_AQL = "/TestWF2/textAnalytics/src/metricsIndicator_dictionaries/dictionaries.aql";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_BASIC_FEATURES_BASICS_AQL = "/TestWF2/textAnalytics/src/PersonName_BasicFeatures/basics.aql";//$NON-NLS-1$
  private static final String PERSON_VIEW = "PersonView";//$NON-NLS-1$
  private static final String CONSOLIDATE = "consolidate";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_CANDIDATE_GENERATION_CANDIDATE_AQL = "/TestWF2/textAnalytics/src/PersonName_CandidateGeneration/candidate.aql";//$NON-NLS-1$
  private static final String TEST_WF2 = "TestWF2";//$NON-NLS-1$

  IProject project;
  IWorkspaceRoot root;

  @Before
  public void setUp () throws Exception
  {
    root = ResourcesPlugin.getWorkspace ().getRoot ();
    project = root.getProject (TEST_WF2);

    String workspacePath = root.getLocation ().makeAbsolute ().toOSString ();
    File directory = new File (workspacePath + Constants.INDEX_PATH);
    directory.deleteOnExit ();
  }

  @Test
  public void testCalculateOffset ()
  {
    // Test the calculateOffset API
    IFile file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_CANDIDATE_GENERATION_CANDIDATE_AQL));
    int offset = IndexerUtil.calculateOffset (file, 1, 15); // 14
    assertEquals (14, offset);
    int offset9 = IndexerUtil.calculateOffset (file, 9, 4); // 307
    assertEquals (307, offset9);
  }

  @Test
  public void testIsAQLKeyword ()
  {
    // Test the isAQLKeyword API
    boolean isKeyWord1 = IndexerUtil.isAQLKeyword (CONSOLIDATE);
    assertTrue (isKeyWord1);
    boolean isKeyWord2 = IndexerUtil.isAQLKeyword (PERSON_VIEW);
    assertFalse (isKeyWord2);

  }
  
  @Test
  public void testDetectElementType () throws Exception
  {
    // Test the detectElementType API
    TextAnalyticsIndexer.getInstance ().reindex ();
    IFile file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_CANDIDATE_GENERATION_CANDIDATE_AQL));
    int offset = IndexerUtil.calculateOffset (file, 9, 13);
    ElementType type = IndexerUtil.detectElementType (file, offset);
    assertEquals (ElementType.VIEW.toString (), type.toString ());

    offset = IndexerUtil.calculateOffset (file, 21, 13);
    type = IndexerUtil.detectElementType (file, offset);
    assertEquals (ElementType.VIEW.toString (), type.toString ());

    offset = IndexerUtil.calculateOffset (file, 1, 13);
    type = IndexerUtil.detectElementType (file, offset);
    assertEquals (ElementType.UNKNOWN.toString (), type.toString ());

    offset = IndexerUtil.calculateOffset (file, 1, 8);
    type = IndexerUtil.detectElementType (file, offset);
    assertEquals (ElementType.MODULE.toString (), type.toString ());

    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_BASIC_FEATURES_BASICS_AQL));
    offset = IndexerUtil.calculateOffset (file, 8, 19);
    type = IndexerUtil.detectElementType (file, offset);
    assertEquals (ElementType.DICTIONARY.toString (), type.toString ());
    
    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_DICTIONARIES_DICTIONARIES_AQL));
    offset = IndexerUtil.calculateOffset (file, 29, 28);
    type = IndexerUtil.detectElementType (file, offset);
    assertEquals (ElementType.DICTIONARY.toString (), type.toString ());

    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_EXTERNAL_TYPES_EXTERNAL_VIEW_AQL));
    offset = IndexerUtil.calculateOffset (file, 35, 22);
    type = IndexerUtil.detectElementType (file, offset);
    assertEquals (ElementType.VIEW.toString (), type.toString ());

    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_UDFS_UDFS_AQL));
    offset = IndexerUtil.calculateOffset (file, 34, 17);
    type = IndexerUtil.detectElementType (file, offset);
    assertEquals (ElementType.FUNCTION.toString (), type.toString ());

    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_MODULE1_TEST_AQL));
    offset = IndexerUtil.calculateOffset (file, 20, 23);
    type = IndexerUtil.detectElementType (file, offset);
    assertEquals (ElementType.TABLE.toString (), type.toString ());

  }

  @Test
  public void testGetCurrentToken () throws Exception
  {
    // Test the getCurrentToken API
    TextAnalyticsIndexer.getInstance ().reindex ();
    IFile file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_UDFS_UDFS_AQL));
    int offset = IndexerUtil.calculateOffset (file, 41, 23);
    String elemName = IndexerUtil.getCurrentToken (file, offset);
    assertEquals (METRICS_INDICATOR_UDFS_UDF_TO_UPPER_CASE, elemName);

    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_FEATURES_BASICS_AQL));
    offset = IndexerUtil.calculateOffset (file, 24, 23);
    elemName = IndexerUtil.getCurrentToken (file, offset);
    assertEquals (METRICS_INDICATOR_DICTIONARIES_METRICS, elemName);

    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_FEATURES_CONCEPTS_AQL));
    offset = IndexerUtil.calculateOffset (file, 32, 8);
    elemName = IndexerUtil.getCurrentToken (file, offset);
    assertEquals (METRICS_INDICATOR_FEATURES_NUMBER, elemName);

    offset = IndexerUtil.calculateOffset (file, 52, 22);
    elemName = IndexerUtil.getCurrentToken (file, offset);
    assertEquals (METRICS_INDICATOR_FEATURES_AMOUNT_ABSOLUTE, elemName);

    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_METRICS_INDICATOR_EXTERNAL_TYPES_EXTERNAL_VIEW_AQL));
    offset = IndexerUtil.calculateOffset (file, 39, 21);
    elemName = IndexerUtil.getCurrentToken (file, offset);
    assertEquals (METRICS_INDICATOR_EXTERNAL_TYPES_EMAIL_METADATA, elemName);

    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_MAIN_MAIN_AQL));
    offset = IndexerUtil.calculateOffset (file, 30, 21);
    elemName = IndexerUtil.getCurrentToken (file, offset);
    assertEquals (METRICS_INDICATOR_UDFS_UDF_TO_UPPER_CASE, elemName);

    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_FILTER_CONSOLIDATE_REFINEMENT_AQL));
    offset = IndexerUtil.calculateOffset (file, 13, 70);
    elemName = IndexerUtil.getCurrentToken (file, offset);
    assertEquals (PERSON_NAME_CANDIDATE_GENERATION_PERSON, elemName);

    offset = IndexerUtil.calculateOffset (file, 18, 16);
    elemName = IndexerUtil.getCurrentToken (file, offset);
    assertEquals (PERSON_NAME_FILTER_CONSOLIDATE_PERSON_FINAL, elemName);

    file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_FINALS_FINAL_AQL));
    offset = IndexerUtil.calculateOffset (file, 9, 45);
    elemName = IndexerUtil.getCurrentToken (file, offset);
    assertEquals (PERSON_NAME_FILTER_CONSOLIDATE_PERSON_FINAL, elemName);

  }

  @Test 
  public void testGetSrcOffset () throws Exception { 
    // Test the getCurrentToken API 
    project = root.getProject
    (TEST_WF2); 
    IFolder file = ProjectUtils.getTextAnalyticsSrcFolder (project); 
    int offset = IndexerUtil.getSrcOffset (file);
    assertEquals (128, offset); }

  @Test 
  public void testGetTamOffset () throws Exception { 
    // Test the getCurrentToken API 
    project = root.getProject(TEST_WF2); 
    IFolder file = ProjectUtils.getTextAnalyticsSrcFolder (project); int offset = IndexerUtil.getTamOffset (file);
    assertEquals (256, offset); }
}
