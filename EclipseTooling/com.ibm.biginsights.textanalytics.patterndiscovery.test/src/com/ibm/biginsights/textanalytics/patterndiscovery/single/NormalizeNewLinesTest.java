package com.ibm.biginsights.textanalytics.patterndiscovery.single;

import java.io.File;

import org.junit.Test;

import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.util.AQLTestUtils;

public class NormalizeNewLinesTest
{
  private static final String PROPS_FILE_PATH = "regression/properties/default.properties";
  private static final String DOC_DIR = "regression/data/";
  private static final String DOC_FILE = "normalize_newlines_data";
  private static final String OUTPUT_DIR = "regression/actual/newlinestest/";
  private static final String AQL_DIR = "regression/aql/";
  private static final String AQL_FILE = AQL_DIR + "normalize_newlines.aql";
  private static final String VIEW_NAME = "Normalize_Newlines.context";
  private static final String GROUP_FILED = "match";

  private static final String MODULE_NAME = "Normalize_Newlines";
  private static final String MODULE_PATH = "regression/modules/";

  private static final String ACTUAL_PATH = OUTPUT_DIR + DOC_FILE;
  private static final String EXPECTED_PATH = "regression/expected/newlinestest/" + DOC_FILE;

  ExperimentProperties properties;
  GroupByNewProcessor processor;
  
  
  
  public NormalizeNewLinesTest () throws PatternDiscoveryException, TextAnalyticsException
  {
    processor = new GroupByNewProcessor ();
    properties = processor.loadProperties (PROPS_FILE_PATH);

    tokenizerConfig = new TokenizerConfig.Standard();

    // set properties for these tests
    properties.setTokenizerConfig (tokenizerConfig);

    properties.setLanguage ("en");
    properties.setProperty (PropertyConstants.AQL_VIEW_NAME, VIEW_NAME);
    properties.setProperty (PropertyConstants.GROUP_BY_FIELD_NAME, GROUP_FILED);
    properties.setProperty (PropertyConstants.INPUT_DOCUMENT_NAME, DOC_FILE);
    properties.setProperty (PropertyConstants.DOCUMENT_COLLECTION_DIR, DOC_DIR);
    properties.setProperty (PropertyConstants.AQL_INCLUDES_DIR, AQL_DIR);
    properties.setProperty (PropertyConstants.AQL_QUERY_FILE, AQL_FILE);
    properties.setProperty (PropertyConstants.FILE_ROOT_DIR, OUTPUT_DIR);

    properties.setProperty (PropertyConstants.TEST_MODULE, MODULE_NAME);

    File modulePath = new File (MODULE_PATH);
    properties.setProperty (PropertyConstants.TEST_TAM_PATH, modulePath.toURI ().toString ());
  }
  
  @Test
  public void runTest() throws Exception{
    processor.run ();
    // test all tables in the database to contain the expected results
    String actual = new File (ACTUAL_PATH).getAbsolutePath ();
    String expected = new File (EXPECTED_PATH).getAbsolutePath ();
    // we want to test that the sequences are the expected
    SequenceMiningTest test1 = new SequenceMiningTest (expected, actual);
    test1.test_compare_method ();
    test1.runTest ();
  }
}
