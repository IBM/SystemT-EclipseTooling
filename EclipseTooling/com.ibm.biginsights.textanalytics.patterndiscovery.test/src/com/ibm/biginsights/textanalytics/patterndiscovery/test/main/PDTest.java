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
package com.ibm.biginsights.textanalytics.patterndiscovery.test.main;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.single.ApplyGroupingTest;
import com.ibm.biginsights.textanalytics.patterndiscovery.single.LoadByCommonSignatureTest;
import com.ibm.biginsights.textanalytics.patterndiscovery.single.LoadBySemanticSignatureTest;
import com.ibm.biginsights.textanalytics.patterndiscovery.single.LoadGroupsTest;
import com.ibm.biginsights.textanalytics.patterndiscovery.single.LoadRangeTest;
import com.ibm.biginsights.textanalytics.patterndiscovery.single.NormalizeNewLinesTest;
import com.ibm.biginsights.textanalytics.patterndiscovery.single.SequenceMiningTest;
import com.ibm.biginsights.textanalytics.patterndiscovery.single.SequenceSignatureReplacementTest;
import com.ibm.biginsights.textanalytics.patterndiscovery.util.AQLTestUtils;

public class PDTest
{
  private static final String PROPS_FILE_PATH = "regression/properties/default.properties";
  private static final String DOC_DIR = "regression/data/";
  private static final String DOC_FILE = "default_data";
  private static final String OUTPUT_DIR = "regression/actual/maintest/";
  private static final String AQL_DIR = "regression/aql/";
  private static final String AQL_FILE = AQL_DIR + "main.aql";
  private static final String VIEW_NAME = "MainModule.Comment";
  private static final String GROUP_FIELD = "match";

  private static final String MODULE_NAME = "MainModule";
  private static final String MODULE_PATH = "regression/modules/";

  private static final String ACTUAL_PATH = OUTPUT_DIR + DOC_FILE;
  private static final String EXPECTED_PATH = "regression/expected/maintest/" + DOC_FILE;

  ExperimentProperties properties;
  GroupByNewProcessor processor;

  /**
   * @throws PatternDiscoveryException
   * @throws IOException
   */
  public void reloadDefaultProperties () throws PatternDiscoveryException, IOException
  {

  }

  /**
   * @throws PatternDiscoveryException
   * @throws IOException
   */
  @Before
  public void setup () throws PatternDiscoveryException, TextAnalyticsException
  {
    PDLogger.setTest (true);

    processor = new GroupByNewProcessor ();
    properties = processor.loadProperties (PROPS_FILE_PATH);

    tokenizerConfig = new TokenizerConfig.Standard();

    // set properties for these tests
    properties.setTokenizerConfig (tokenizerConfig);

    properties.setLanguage ("en");
    properties.setProperty (PropertyConstants.AQL_VIEW_NAME, VIEW_NAME);
    properties.setProperty (PropertyConstants.GROUP_BY_FIELD_NAME, GROUP_FIELD);
    properties.setProperty (PropertyConstants.INPUT_DOCUMENT_NAME, DOC_FILE);
    properties.setProperty (PropertyConstants.DOCUMENT_COLLECTION_DIR, DOC_DIR);
    properties.setProperty (PropertyConstants.AQL_INCLUDES_DIR, AQL_DIR);
    properties.setProperty (PropertyConstants.AQL_QUERY_FILE, AQL_FILE);
    properties.setProperty (PropertyConstants.FILE_ROOT_DIR, OUTPUT_DIR);

    File modulePath = new File (MODULE_PATH);
    properties.setProperty (PropertyConstants.TEST_MODULE, MODULE_NAME);
    properties.setProperty (PropertyConstants.TEST_TAM_PATH, modulePath.toURI ().toString ());
  }

  /**
   * 
   */
  @After
  public void teardown ()
  {}

  /**
   * @throws Exception
   */
  @Test
  public void sequence_generation () throws Exception
  {
    processor.run ();
    // test all tables in the database to contain the expected results
    String actual = new File (ACTUAL_PATH).getAbsolutePath ();
    String expected = new File (EXPECTED_PATH).getAbsolutePath ();
    SequenceMiningTest test = new SequenceMiningTest (expected, actual);
    test.test_compare_method ();
    test.runTest ();
  }

//  /**
//   * 
//   */
//  @Test
//  public void uncertainity_coefficient ()
//  {
//    // TODO for some reason the current values produced are not always the expected (Ynyao feedback).
//  }
//
//  /**
//   * 
//   */
//  @Test
//  public void rules_based_on_threshold_correlation_measure ()
//  {
//
//  }
//
//  /**
//   * 
//   */
//  @Test
//  public void rules_confidence_score ()
//  {
//
//  }
//
//  /**
//   * 
//   */
//  @Test
//  public void apply_drop_rules ()
//  {
//
//  }

  /**
   * 
   */
  @Test
  public void signature_replacement ()
  {
    new SequenceSignatureReplacementTest ().runTest ();
  }

  /**
   * @throws Exception
   */
  @Test
  public void apply_grouping () throws Exception
  {
    // test all tables in the database to contain the expected results
    String actual = new File (ACTUAL_PATH).getAbsolutePath ();
    String expected = new File (EXPECTED_PATH).getAbsolutePath ();
    ApplyGroupingTest test = new ApplyGroupingTest (expected, actual, VIEW_NAME);
    test.runTest ();
  }

  /**
   * @throws Exception
   */
  @Test
  public void load_range () throws Exception
  {
    String actual = new File (ACTUAL_PATH).getAbsolutePath ();
    String expected = new File (EXPECTED_PATH).getAbsolutePath ();
    LoadRangeTest test = new LoadRangeTest (expected, actual, properties);
    test.runTest ();
  }

  /**
   * @throws Exception
   */
  @Test
  public void load_groups () throws Exception
  {
    String actual = new File (ACTUAL_PATH).getAbsolutePath ();
    String expected = new File (EXPECTED_PATH).getAbsolutePath ();
    LoadGroupsTest test = new LoadGroupsTest (expected, actual, properties);
    test.runTest ();
  }

  /**
   * @throws Exception
   */
  @Test
  public void load_by_semantic_signature () throws Exception
  {
    String actual = new File (ACTUAL_PATH).getAbsolutePath ();
    String expected = new File (EXPECTED_PATH).getAbsolutePath ();
    LoadBySemanticSignatureTest test = new LoadBySemanticSignatureTest (expected, actual, properties);
    test.runTest ();
  }

  /**
   * @throws Exception
   */
  @Test
  public void load_by_common_signature () throws Exception
  {
    String actual = new File (ACTUAL_PATH).getAbsolutePath ();
    String expected = new File (EXPECTED_PATH).getAbsolutePath ();
    LoadByCommonSignatureTest test = new LoadByCommonSignatureTest (expected, actual, properties);
    test.runTest ();
  }

  /**
   * This test case is intended to be used to test the functionality of the normalize new lines functionality. This
   * functionality is intended to normalize cases when multiple \n characters appears in the same token
   * @throws Exception 
   */
  @Test
  public void normalize_newlines () throws Exception
  {
    NormalizeNewLinesTest test = new NormalizeNewLinesTest ();
    test.runTest ();
  }

}
