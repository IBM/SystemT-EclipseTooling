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

package com.ibm.biginsights.textanalytics.patterndiscovery.test;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;

/**
 * Tests that extract information from the enron data set using AQL.
 */
public class BackendEndTests {

	/**
	 * Directory where AQL files referenced in this class's regression tests are
	 * located.
	 */
	public static final String AQL_FILES_DIR = TestConstants.AQL_DIR + File.separator + "backendtests";

	public static final String AOG_DIR = TestConstants.AOG_DIR + File.separator + "backendtests";

	/** Directory where regression test results for this class go. */
	public static final String OUTPUT_DIR = TestUtils.DEFAULT_OUTPUT_DIR + File.separator + "backendtests";

	/** Corresponding directory for holding expected test results. */
	public static final String EXPECTED_DIR = TestUtils.DEFAULT_EXPECTED_DIR + File.separator + "backendtests";

	public static final String PROPERTIES_FILE = TestConstants.PROPERTIES_DIR + File.separator + "aqlGroupByEnronClean.properties";

	Logger log = Logger.getLogger("JUnit Log");

	private File lwDataPath;
	private File lwDescFile;

	public static void main(String[] args) {
		try {

			BackendEndTests t = new BackendEndTests();

			t.setUp();

			long startMS = System.currentTimeMillis();

			// t.simpleJapaneseTest();

			long endMS = System.currentTimeMillis();

			t.tearDown();

			double elapsedSec = (double) (endMS - startMS) / 1000.0;

			System.err.printf("Test took %1.3f sec.\n", elapsedSec);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Scan over the Enron database; set up by setUp() and cleaned out by
	 * tearDown()
	 */
	private TestUtils util = null;

	@Before
	public void setUp() throws Exception {

		// Renice the current thread to avoid locking up the entire system.
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

		util = new TestUtils();
		// Put default output directories into place.
		// Individual tests may override these choices.
		util.setOutputDir(OUTPUT_DIR);
		util.setExpectedDir(EXPECTED_DIR);

		// For now, don't put any character set information into the header of
		// our output HTML.
		util.setWriteCharsetInfo(false);

		File outDir = new File(OUTPUT_DIR);
		if (false == outDir.exists()) {
			outDir.mkdirs();
		} else {
			TestUtils.deleteDir(outDir);
			outDir.mkdir();
		}

		// Setup languageWare
		lwDataPath = TestUtils.getDefaultLWDataPath();
		lwDescFile = TestUtils.getDefaultLWConfigFile();

		// Make sure our log messages get printed!
		// log.enableAllMsgTypes();
	}

	@After
	public void tearDown() {
		// Deregister the Derby driver so that other tests can connect to other
		// databases. This is probably not necessary (we don't currently use
		// derby), but we do it just in case.
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			// The shutdown command always raises a SQLException
			// See http://db.apache.org/derby/docs/10.2/devguide/
		}
		System.gc();

		log.info("Done.");
	}

	/**
	 * Runs PersonPhone AQL for PD Language - English - (white space tokenizer)
	 * 
	 * Tests with a given AOG file Also tests illegal groupbyName
	 * 
	 * @throws Exception
	 */
	@Test
	public void illegalGroupByNameAndAOGTest() throws Exception {

		GroupByNewProcessor personPhonePDTest = new GroupByNewProcessor();

		ExperimentProperties prop = personPhonePDTest.loadProperties(PROPERTIES_FILE);

		generalPropertySetup(null, "PhoneContext1", "match", "enron-450.zip", prop, "personphone.aog", "illegalGroupByNameAndAOGTest");

		personPhonePDTest.run();

		// comparePDFiles(prop, "illegalGroupByNameAndAOGTest");

	}

	/**
	 * Simple Japanese PD Test
	 * 
	 * @throws Exception
	 */
	@Test
	public void simpleJapaneseTest() throws Exception {

		GroupByNewProcessor japaneseTest = new GroupByNewProcessor();
		ExperimentProperties prop = japaneseTest.loadProperties(PROPERTIES_FILE);

		generalPropertySetup("phone.aql", "Tok", "num", "japanese.del", prop, null, "simpleJapaneseTest");

		prop.setLanguage("ja");

		// Sets frequency size small to accomodate the smaller dataset
		prop.setProperty(PropertyConstants.SEQUENCE_MIN_FREQUENCY_SIZE, "3");

		japaneseTest.run();

		comparePDFiles(prop, "simpleJapaneseTest");
	}

	/**
	 * Simple Chinese Test Only checks for the existence of one group
	 * 
	 * @throws Exception
	 */
	@Test
	public void simpleChineseTest() throws Exception {

		GroupByNewProcessor simpleChineseTest = new GroupByNewProcessor();
		ExperimentProperties prop = simpleChineseTest.loadProperties(PROPERTIES_FILE);

		generalPropertySetup("phone.aql", "Tok", "num", "ChineseTest.zip", prop, null, "simpleChineseTest");

		prop.setLanguage("zh");

		simpleChineseTest.run();

		comparePDFiles(prop, "simpleChineseTest");

	}

	/**
	 * Runs PersonPhone AQL for PD Language - English - (white space tokenizer)
	 * 
	 * Tests with AQL files
	 * 
	 */
	@Test
	public void personPhoneTest() throws Exception {

		GroupByNewProcessor personPhonePDTest = new GroupByNewProcessor();

		ExperimentProperties prop = personPhonePDTest.loadProperties(PROPERTIES_FILE);

		generalPropertySetup("phone.aql", "PhoneContext1", "match", "enron-450.zip", prop, null, "personPhoneTest");

		prop.setLanguage("en");

		personPhonePDTest.run();

		comparePDFiles(prop, "personPhoneTest");

	}

	/**
	 * Replace one entity
	 * 
	 * @throws Exception
	 */
	@Test
	public void replaceOneEntityTest() throws Exception {

		GroupByNewProcessor replaceOneEntityTest = new GroupByNewProcessor();

		ExperimentProperties prop = replaceOneEntityTest.loadProperties(PROPERTIES_FILE);

		generalPropertySetup("phone.aql", "PhoneContext1", "match", "enron-100.zip", prop, null, "replaceOneEntityTest");

		// Sets the replace Entity Name
		prop.setProperty(PropertyConstants.ENTITY_FIELD_NAMES, "PhoneContext1.phone");
		// Sets frequency size small to accomodate the smaller dataset
		prop.setProperty(PropertyConstants.SEQUENCE_MIN_FREQUENCY_SIZE, "5");
		replaceOneEntityTest.run();

		comparePDFiles(prop, "replaceOneEntityTest");
	}

	/**
	 * Replace two entities at once
	 * 
	 * @throws Exception
	 */
	@Test
	public void replaceTwoEntityTest() throws Exception {

		GroupByNewProcessor replaceTwoEntityTest = new GroupByNewProcessor();

		ExperimentProperties prop = replaceTwoEntityTest.loadProperties(PROPERTIES_FILE);

		generalPropertySetup("phone.aql", "PersonsPhoneAll", "context", "enron-100.zip", prop, null, "replaceTwoEntityTest");

		// Sets the replace Entity Name
		prop.setProperty(PropertyConstants.ENTITY_FIELD_NAMES, "PersonsPhoneAll.phone,PersonsPhoneAll.person");
		// Sets frequency size small to accomodate the smaller dataset
		prop.setProperty(PropertyConstants.SEQUENCE_MIN_FREQUENCY_SIZE, "3");
		replaceTwoEntityTest.run();

		comparePDFiles(prop, "replaceTwoEntityTest");
	}

	/**
	 * Replace two entities with the same field name: ex.
	 * PersonConsolidated.name & Surname.name
	 * 
	 * TODO: fill in - does not yet work because multiple entity change has not
	 * been made
	 * 
	 * @throws Exception
	 */
	@Test
	public void replaceEntitySameNameTest() throws Exception {

	}

	/**
	 * Test to make sure when entity is given an "illegal" name - names that are
	 * reserved for derby db, everything is fine
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void replaceEntityIllegalNameTest() throws Exception {

		GroupByNewProcessor replaceEntityIllegalNameTest = new GroupByNewProcessor();

		ExperimentProperties prop = replaceEntityIllegalNameTest.loadProperties(PROPERTIES_FILE);

		generalPropertySetup("phone.aql", "PhoneContext1", "match", "enron-450.zip", prop, null, "replaceEntityIllegalNameTest");

		// Sets the replace Entity Illegal Name
		prop.setProperty(PropertyConstants.ENTITY_FIELD_NAMES, "PhoneContext1.sort");

		prop.setLanguage("en");

		replaceEntityIllegalNameTest.run();

		comparePDFiles(prop, "replaceEntityIllegalNameTest");

	}

	/**
	 * Tests to makes sure when enable Debug is set to true, no errors are
	 * thrown. do not need to check for correctness of file
	 * 
	 * @throws Exception
	 */
	@Test
	public void enableDebugTest() throws Exception {

		GroupByNewProcessor enableDebugTest = new GroupByNewProcessor();

		ExperimentProperties prop = enableDebugTest.loadProperties(PROPERTIES_FILE);

		generalPropertySetup("phone.aql", "PersonsPhoneAll", "context", "enron-450.zip", prop, null, "enableDebugTest");

		prop.setProperty(PropertyConstants.ENABLE_DEBUGGING, "true");
		enableDebugTest.run();

	}

	/**
	 * Tests that when Use Infrequent Words is enabled the results are as
	 * expected.
	 * 
	 * @throws Exception
	 */
	@Test
	public void useInfrequentSequenceTest() throws Exception {

		GroupByNewProcessor useInfrequentSequenceTest = new GroupByNewProcessor();

		ExperimentProperties prop = useInfrequentSequenceTest.loadProperties(PROPERTIES_FILE);

		generalPropertySetup("phone.aql", "PersonsPhoneAll", "context", "enron-100.zip", prop, null, "useInfrequentSequenceTest");

		// Sets frequency size small to accomodate the smaller dataset
		prop.setProperty(PropertyConstants.SEQUENCE_MIN_FREQUENCY_SIZE, "3");

		prop.setProperty(PropertyConstants.USE_INFREQUENT_WORDS, "true");
		useInfrequentSequenceTest.run();

		comparePDFiles(prop, "useInfrequentSequenceTest");
	}

	/**
	 * Test to see if an illegal character such as "," in the document name will
	 * throw an error
	 * 
	 * @throws Exception
	 */
	@Test
	public void illegalCharacterInDocumentNameTest() throws Exception {

		GroupByNewProcessor illegalCharacterInDocumentNameTest = new GroupByNewProcessor();

		ExperimentProperties prop = illegalCharacterInDocumentNameTest.loadProperties(PROPERTIES_FILE);

		generalPropertySetup("phone.aql", "LeftPerson", "lc", "CIA-Countries.zip", prop, null, "illegalCharacterInDocumentNameTest");

		prop.setLanguage("en");

		illegalCharacterInDocumentNameTest.run();

	}

	/**
	 * Set up pattern discovery to run
	 * 
	 * Sets important information with AQL, output files, input document and
	 * Tokenizer
	 * 
	 * @param aqlFileName
	 * @param outputViewName
	 * @param groupByField
	 * @param inputFileName
	 * @param prop
	 * @param aogFile
	 * @throws Exception
	 */
	public void generalPropertySetup(String aqlFileName, String outputViewName, String groupByField, String inputFileName, ExperimentProperties prop,
			String aogFileName, String testName) throws Exception {

		String filename = AQL_FILES_DIR + File.separator + aqlFileName;

		if (aqlFileName == null) {
			prop.setAogFile(new File(AOG_DIR + File.separator + aogFileName));
		} else {
			prop.put(PropertyConstants.AQL_INCLUDES_DIR, TestConstants.AQL_INCLUDES_DIR);
			prop.put(PropertyConstants.AQL_DICTIONARY_DIR, TestConstants.AQL_INCLUDES_DIR + "/core/GenericNE/dictionaries");
			prop.put(PropertyConstants.AQL_JAR_DIR, TestConstants.AQL_INCLUDES_DIR);
			prop.setProperty(PropertyConstants.AQL_QUERY_FILE, filename);
		}

		prop.put(PropertyConstants.AQL_VIEW_NAME, outputViewName);
		prop.put(PropertyConstants.GROUP_BY_FIELD_NAME, groupByField);
		prop.put(PropertyConstants.DOCUMENT_COLLECTION_DIR, TestConstants.TEST_DOCS_DIR);
		prop.put(PropertyConstants.INPUT_DOCUMENT_NAME, File.separator + inputFileName);
		prop.put(PropertyConstants.FILE_ROOT_DIR, OUTPUT_DIR + File.separator + testName);

		try {
			tokenizerConfig = new TokenizerConfig.Standard();
			prop.setTokenizerConfig(tokenizerConfig);

		} catch (TextAnalyticsException e) {
			ErrorMessages.LogErrorMessage(ErrorMessages.PATTERN_DISCOVERY_ERROR_CREATING_TOKENIZER, e);
		}

	}

	/**
	 * Wrapper to compare PD files
	 * 
	 * Whatever testName given must be part of the file name.
	 * 
	 * Ex. test: personPhoneTest Expected file name in regression/expected -
	 * personPhoneTest-GroupingJaccard.csv
	 * 
	 * @param prop
	 * @throws Exception
	 */
	public void comparePDFiles(ExperimentProperties prop, String testName) throws Exception {

		File actualResults = new File(prop.getRootDir() + File.separator + "GroupingJaccard.csv");
		File expectedResults = new File(EXPECTED_DIR + File.separator + testName + "-GroupingJaccard.csv");

		TestUtils.compareFiles(expectedResults, actualResults, 0, -1);
	}

}
