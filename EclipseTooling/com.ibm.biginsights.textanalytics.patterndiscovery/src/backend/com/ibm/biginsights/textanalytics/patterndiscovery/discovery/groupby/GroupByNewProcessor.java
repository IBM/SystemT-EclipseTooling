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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringBufferInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.avatar.algebra.util.tokenize.Tokenizer;
import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis.ComputeCorrelation;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis.Correlation2Rules;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis.MineDBDumpPaired;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis.SequenceLoader;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis.SequenceOverview;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.goldenStandard.GoldStandardLabeling;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.goldenStandard.MechTurkPostProcessing;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.goldenStandard.Statistics;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.HashFactory;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.RuleBasedHasher;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DiscoveryConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties.ParseResult;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.FindFuzzyGroups;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IntPair;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.pairs.SequencePairPatternProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.nlp.DefaultWordIntegerMapping;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.nlp.WordIntegerMapping;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDBubbleUtils;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;

/**
 * Runs the new pipeline for sequence mining, rule generation and hashing. The new pipeline is based on the paired
 * sequence mining algorithm. Processing can be configured by a property file. For a commented example of a property
 * file for this class see groupby/data/titles/aqlGroupBy.properties It is a sister-program to
 * {@link GroupByPreprocessor}.
 * 
 * 
 *  Chu Edit/Clean/Add Functions 9/24/2010
 */

@SuppressWarnings("deprecation")
public class GroupByNewProcessor
{
  
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
      "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  /**
   * Global Variables
   */

  // Properties that controls settings for the experiment
  public static ExperimentProperties properties;

  // Store word integer mapping
  private static WordIntegerMapping mapping = null;

  // store sequence array to sequenceID mapping
  private static Map<Integer, String> sequenceMap = null;

  public static final String ENCODING = "UTF-8";            //$NON-NLS-1$

  public static final String COL_SEPARATOR = "SEPERATOR";   //$NON-NLS-1$

  private volatile Boolean do_cancel = false;

  public static IProgressMonitor monitor;

  private IPDLog logger = PDLogger.getLogger ("GroupByNewProcessor");   //$NON-NLS-1$


  /**
   * Given a configuration file, loads into the system as an ExperimentProperty file
   * 
   * @param propertiesFile
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   * @throws PatternDiscoveryException
   */
  public ExperimentProperties loadProperties (String propertiesFile) throws PatternDiscoveryException
  {

    // Create a new ExperimentProperties - store globally
    properties = new ExperimentProperties ();

    try {
      // Load Experiment Properties
      properties.load (new FileInputStream (propertiesFile));
    }
    catch (FileNotFoundException FNFE) {
      throw new PatternDiscoveryException (FNFE, ErrorMessages.PATTERN_DISCOVERY_PROPERTIES_MISSING_ERR);
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_PROPERTIES_INVALID_ERR);
    }

    return properties;
  }

  /**
   * Will create an ExperimentProperty without a file - assume user will set actual properties
   * 
   * @return
   */
  public ExperimentProperties initializeEmptyProperty ()
  {
    properties = new ExperimentProperties ();
    return properties;
  }

  /**
   * Simple function to delete an entire Directory recursively
   * 
   * @param dir
   * @return
   */
  public static boolean deleteDir (File dir)
  {
    if (dir.isDirectory ()) {
      String[] children = dir.list ();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir (new File (dir, children[i]));
        if (!success) { return false; }
      }
    }
    return dir.delete ();
  }

  public String getBaseDirPath ()
  {
    String baseDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR);

    String inputFile = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);

    if (inputFile.contains (".")) inputFile = inputFile.substring (0, inputFile.indexOf ('.'));
    baseDir = baseDir + inputFile + File.separator;
    properties.setRootDir (baseDir);

    return baseDir;
  }

  private void cleanDirs ()
  {
    File f = dirsExist ();
    if (f != null) {
      deleteDir (f);
    }
  }

  public File dirsExist ()
  {
    File baseFile = new File (getBaseDirPath ());
    if (baseFile.exists ()) return baseFile;
    return null;
  }

  public boolean run () throws PatternDiscoveryException
  {
    return run (null);
  }

  /**
   * Main function to run Pattern Discovery Includes creating all necessary directories
   * 
   * @throws PatternDiscoveryException
   * @throws Exception
   */
  public boolean run (IProgressMonitor amonitor) throws PatternDiscoveryException
  {
    monitor = amonitor;
    if (monitor != null && !monitor.isCanceled ()) // we are going to have PD_PROCESS_TOTAL (100) increment steps
      monitor.beginTask (Messages.PD_STARTING, Messages.PD_PROCESS_TOTAL);

    String baseDirPath = getBaseDirPath ();
    /** Get database information ready **/
    String dbUrl = properties.getProperty (PropertyConstants.DB_PREFIX) + properties.getRootDir ()
      + properties.getProperty (PropertyConstants.SEQUENCE_DB_NAME);
    // store in Experiment Properties
    properties.setSequenceDBURL (dbUrl);

    boolean resequence = Boolean.parseBoolean (properties.getProperty (PropertyConstants.RECOMPUTE_SEQUENCES));
    if (resequence) {
      // Create sequence db if it does not exist:
      DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
      db.setProperties (properties); // Pass in information to store txt files and database
      return doProcessingNext (db);
    }

    // initialize directory structure

    boolean reuseDirs = Boolean.getBoolean (properties.getProperty (PropertyConstants.USE_EXISTING_DB_DATA));

    if (!reuseDirs) {
      cleanDirs ();
    }
    else {
      // if the user wants to reuse the data we have to make sure first that the data exists otherwise we will need to
      // recreate this to ensure the expected results. For example a good case for this is when the user runs and cancel
      // and then try to re run and expect previous data to exist
      if (dirsExist () == null) properties.setProperty (PropertyConstants.USE_EXISTING_DB_DATA, "false");
    }

    /** Creates Directories Required **/
    // debug directories
    String debugDir = baseDirPath + properties.getProperty (PropertyConstants.DEBUG_DIR);
    File base = new File (debugDir);
    base.mkdirs ();

    // grouping storage directories
    File groupDir = new File (baseDirPath + properties.getProperty (PropertyConstants.GROUPING_DIR));
    groupDir.mkdirs ();

    // rule storage directories
    File ruleDir = new File (baseDirPath + properties.getProperty (PropertyConstants.RULE_DIR));
    ruleDir.mkdirs ();

    // input storage directories
    File inputDir = new File (baseDirPath + properties.getProperty (PropertyConstants.INPUT_FILE_DIR));
    inputDir.mkdir ();

    // Post processing storage directories (including MechTurk Dirs)
    File postProcessDir = new File (baseDirPath + properties.getProperty (PropertyConstants.MECHANICAL_TURK_DIR));
    postProcessDir.mkdirs ();

    // initializing log dir
    File logDir = new File (baseDirPath + "log");
    logDir.mkdirs ();

    /** Start running processes **/

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // Processes raw mechTurk results and stores into goldStandard.csv
    if ("true".equalsIgnoreCase (properties.getProperty (PropertyConstants.PROCESS_MECHTURK_RESULTS))) {
      processMechTurkResults ();
    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // Runs groupby main components
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.TURN_PATTERNDISCOVERY_OFF))) {
      boolean state = doProcessing ();
      if (!state) {
        resetCancell ();
        return false;
      }
    }

    // PostProcesses results - add gold standard/get statistics
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_POST_PROCESSSING))) {
      doPostProcessing ();
    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    DebugDBProcessor.tearDown ();
    return true;
  }

  public void processMechTurkResults () throws PatternDiscoveryException
  {

    logger.info ("Starting Gold Standard Processing");

    // Get root directory for gold standard
    String baseDir = properties.getRootDir ();

    // PostProcessDir
    String postProcessDir = baseDir + properties.getProperty (PropertyConstants.POST_PROCESS_DIR);
    String mechTurkResultsDir = baseDir + properties.getProperty

    (PropertyConstants.MECHANICAL_TURK_RESULTS_FILE);

    // Once MechTurk golden standard has come in, post process it and store
    // into DB
    try {
      MechTurkPostProcessing.processRawResults (mechTurkResultsDir, postProcessDir + "goldStandard.csv");
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_MECHTURK_RESULTS_PROCESSING_ERR);
    }
  }

  /**
   * Manages the processing pipeline by dispatching to sub-routines.
   * 
   * @param args
   * @throws PatternDiscoveryException
   * @throws Exception
   */
  public boolean doProcessing () throws PatternDiscoveryException
  {
    // Run AQL and store output into DB
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.USE_EXISTING_DB_DATA))) {
      boolean state = GroupByPreprocessor.runAQL (properties, this);
      if (!state) return false;
    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // Create sequence db if it does not exist:
    String dbUrl = properties.getSequenceDBURL ();
    DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
    db.setProperties (properties); // Pass in information to store txt files and database
    // Grab root directory for all folders
    String rootDir = properties.getRootDir ();

    // Store rule directory
    String ruleDir = rootDir + properties.getProperty (PropertyConstants.RULE_DIR);

    // match sequences and store SEQUENCES and SEQUENCE_MATCHES
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_SEQUENCE_MINING))) {
      setProgressMonitorMessage (Messages.PD_SEQUENCE_MINING, 0);
      SequenceLoader loader = mineSequences (properties);
      // Store word to ID Map
      mapping = loader.getMapping ();
      workProgressMonitor (Messages.PD_SEQUENCE_MINING_STEP3_WORK);
    }

    // Create and store Dictionary
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_STORE_DICTIONARY))) {
      if (mapping == null) { throw new RuntimeException (
        "cannot do storeDictionary without prior mining (null Dictionary)"); }
      // Might be brokeN?
      try {
        GroupByPreprocessor.storeDictionary (properties, mapping, logger);
      }
      catch (SQLException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_STORE_DICTIONARY_DB_ERR);
      }
      catch (IOException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_STORE_DICTIONARY_WRITE_ERR);
      }
    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // load db with sequence_instance and store into sequence support
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_LOAD_SUPPORT_SEQUENCE))) {
      logger.info ("loading mined sequence support");
      try {
        supportLoading ();
      }
      catch (SQLException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_STORE_DICTIONARY_WRITE_ERR);
      }
      catch (IOException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_STORE_DICTIONARY_DB_ERR);
      }
    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // Load sequenceID to sequence String from sequence support
    try {
      sequenceMap = SequenceLoader.loadSequence2IDMap (dbUrl, properties);
    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (ErrorMessages.PATTERN_DISCOVERY_LOAD_SEQUENCES_MAPPING_ERR);

    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // Create files for counting
    File countFile = new File (ruleDir + "/counts.csv");
    File coCountFile = new File (ruleDir + "/coCounts.csv");
    File sequenceFile = new File (ruleDir + "/sequences.csv");
    // queries to extract information for measure computation
    // schema: sequence 1, sequence 2, count
    String coOccurrenceQuery = String.format ("SELECT * FROM %s", "SEQUENCE_NEW_CO_COUNT");
    // schema: matchCount (=total number of transactions)
    String matchCountQuery = String.format ("SELECT count(distinct id) from %s", "SEQUENCES_SUPPORT");
    // schema: sequenceID, count
    String sequenceCountQuery = "SELECT seq, count FROM SEQUENCE_NEW_SEQ_COUNT";

    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_COUNTING))) {
      logger.info ("generating co-occurrence counts");
      try {
        counting (properties, countFile, coCountFile, sequenceFile);
      }
      catch (SQLException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_COUNT_DB_ERR);
      }
      catch (IOException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_COUNT_WRITE_ERR);
      }
      logger.info ("done counting");

    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // compute measure
    String[] measureFiles = null;
    if (!"true".equalsIgnoreCase (properties.getProperty

    (PropertyConstants.DISABLE_COMPUTE_MEASURE_SEQUENCE))) {
      logger.info ("starting computeMeasures");
      measureFiles = computeMeasures (countFile, coCountFile, coOccurrenceQuery, matchCountQuery, sequenceCountQuery);
      logger.info ("done computeMeasures");
    }
    else {
      measureFiles = createMeasureFileList ();
    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // File outputs and mappings needed for debugging
    if (properties.getProperty (PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase ("true")) {
      // creates files cocount and count with debug info
      try {
        generateDebugFiles (db, properties, sequenceMap);
      }
      catch (Exception e) {

      }
    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // Derive Rules
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_RULE_GENERATION))) {
      // re-index sequences (if supportLoading is disabled)
      if ("true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_LOAD_SUPPORT_SEQUENCE))) {

        try {
          reIndexSequences (db, logger);
          reIndexDictionary (db, logger);
        }
        catch (SQLException e) {
          throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_LOAD_SEQUENCE_REINDEX_ERR);
        }
      }

      logger.info ("starting ruleGeneration");
      setProgressMonitorMessage (Messages.PD_RULES_GENERATION, Messages.PD_RULES_GENERATION_STEP1_WORK);
      ruleGeneration (properties, coOccurrenceQuery, sequenceCountQuery, measureFiles, sequenceMap);
      logger.info ("done ruleGeneration");
      workProgressMonitor (Messages.PD_RULES_GENERATION_STEP3_WORK);
    }

    return doProcessingNext (db);
  }

  /**
   * @return
   * @throws PatternDiscoveryException
   */
  public boolean doProcessingNext (DebugDBProcessor db) throws PatternDiscoveryException
  {
    setProgressMonitorMessage (Messages.PD_GROUP, 0);
    // Grab root directory for all folders
    String rootDir = properties.getRootDir ();

    // Store rule directory
    String ruleDir = rootDir + properties.getProperty (PropertyConstants.RULE_DIR);

    // Directory where sequence outputs are stored into
    String relevantSeqDir = rootDir + properties.getProperty (PropertyConstants.RULE_DIR)
      + "preprocessing-relevant.csv";

    String dbUrl = properties.getSequenceDBURL ();

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // compute "relevant sequences" -> those sequences which (regardless of
    // their presence in rules) should be used during hashing. In this
    // implementation.
    // those are the individual words.
    try {
      GroupByPreprocessor.computeRelevantSequences (dbUrl, relevantSeqDir, properties, logger);
    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RELEVANT_SEQUENCE_DB_ERR);
    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // grouping
    boolean hashInDB = "db".equalsIgnoreCase (properties.getProperty (PropertyConstants.FUZZY_GROUPING_ALGORITHM));
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_GROUPING))) {
      logger.info ("starting grouping");
      grouping (properties, hashInDB, sequenceMap);
      logger.info ("done grouping");
    }
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_FUZZY_GROUPING))) {
      if (mapping == null) {
        mapping = loadMappingFromDictionary (new DebugDBProcessor (dbUrl));
      }
      logger.info ("starting new grouping");
      newGrouping (properties, hashInDB, mapping);
      logger.info ("done new grouping");
    }

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // Group by distance of sequences
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_DISTANCE_MERGING))) {
      if (mapping == null) {
        mapping = loadMappingFromDictionary (new DebugDBProcessor (dbUrl));
      }
      logger.info ("starting sequence distance");
      sequenceGroup (properties, sequenceMap);
      logger.info ("done sequence distance");
    }
    
    workProgressMonitor (Messages.PD_GROUP_STEPS_WORK);

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    // TODO this is never used. I (acf) would delete it.
    // Added in extra debugging
    if (properties.getProperty (PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase ("true")) {
      logger.info ("Start addd Debug");

      String databaseTest = properties.getProperty (PropertyConstants.DB_PREFIX) + properties.getRootDir ()
        + properties.getProperty (PropertyConstants.RESULTS_DB_NAME);

      try {
        DebugDBProcessor dbTest = new DebugDBProcessor (databaseTest);
        if (Constants.DEBUG) System.out.println (databaseTest);
        String tableName = "aomdata.type_"
          + properties.getProperty (PropertyConstants.AQL_VIEW_NAME).replace (".", "__");
        String testQuery = "SELECT * FROM " + tableName;

        ResultSet rsTest = dbTest.readFromDB (testQuery);
        List<String[]> toWriteListTest = new ArrayList<String[]> ();
        String debugDir = rootDir + properties.getProperty (PropertyConstants.DEBUG_DIR);

        CSVWriter writerTest = new CSVWriter (new OutputStreamWriter (new FileOutputStream (debugDir + "/seqOnly.csv"),
          GroupByNewProcessor.ENCODING), ',', '"');

        // Create Hasher to determine what is happening for computeS
        File ruleFileGivenTest = new File (ruleDir + properties.getProperty

        (PropertyConstants.APPLIED_RULE_FILES));
        String ruleFileTest = ruleFileGivenTest.getAbsolutePath ();
        RuleBasedHasher hasher = (RuleBasedHasher) AQLGroupByPersist.makeHasher (properties, ruleFileTest, sequenceMap);
        // WordIntegerMapping map = hasher.getMapping();

        String firstline[] = { "docID", "ID", "original string", "sPrime wordMap", "sPrime seqMap" };
        toWriteListTest.add (firstline);
        while (rsTest.next ()) {
          String docID = rsTest.getString (1);
          String ID = rsTest.getString (3);
          String value = rsTest.getString (4);
          value = value.toLowerCase ();
          value = value.trim ();
          Collection<Integer> set = hasher.computeSdebug (value);

          // Convert int to text
          Iterator<Integer> getSeqInt = set.iterator ();
          // String sequenceID = "";
          String sequenceID2 = "";
          while (getSeqInt.hasNext ()) {
            int seq = getSeqInt.next ();
            // sequenceID = sequenceID + map.wordForInt(seq) + ";";
            sequenceID2 = sequenceID2 + sequenceMap.get (seq) + ";";
          }
          String line[] = { docID, ID, value, set.toString (), sequenceID2 };
          toWriteListTest.add (line);
        }
        writerTest.writeAll (toWriteListTest);
        writerTest.flush ();
        writerTest.close ();
        toWriteListTest.clear ();
        logger.info ("done extra debug");
      }
      catch (IOException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_DEBUG_WRITE_ERR);
      }
      catch (SQLException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_DEBUG_DB_ERR);
      }
    }

    db.shutdown ();

    if (do_cancel) {
      resetCancell ();
      return false;
    }

    setProgressMonitorMessage (Messages.PD_ENDING, 99);

    return true;
  }

  /**
   * This method will try to replace all the signatures with an ordered signature that can be better understood by the
   * user. It will load all the unique signatures from the final table and replace them with the new one created
   * 
   * @throws SQLException
   * @throws IOException
   */
  public void rebuildSignatures () throws SQLException, IOException
  {
    logger.info ("Organizing sequences to display");

    // create db connection
    String dbUrl = properties.getProperty (PropertyConstants.DB_PREFIX) + properties.getRootDir ()
      + properties.getProperty (PropertyConstants.RESULTS_DB_NAME);
    DebugDBProcessor db = new DebugDBProcessor (dbUrl);
    db.setProperties (properties);

    String GROUPS_TABLE_NAME = "APP.groupingjaccard_" + properties.getProperty (Messages.AQL_VIEW_NAME_PROP);
    GROUPS_TABLE_NAME = GROUPS_TABLE_NAME.replace (".", "__");

    String getAllQuery = "SELECT GROUPBYCONTEXT, SEQUENCE, JSEQUENCE FROM " + GROUPS_TABLE_NAME
      + " WHERE SEQUENCE != '}' AND JSEQUENCE != '}' GROUP BY GROUPBYCONTEXT, SEQUENCE, JSEQUENCE";

    String updateSignatureQuery = String.format ("UPDATE %s SET SEQUENCE=? WHERE SEQUENCE=?", GROUPS_TABLE_NAME);
    PreparedStatement updateSignaturePrepStmt = db.prepareStatement (updateSignatureQuery, GROUPS_TABLE_NAME,
      "SEQUENCE");

    String updateJSignatureQuery = String.format ("UPDATE %s SET JSEQUENCE=? WHERE JSEQUENCE=?", GROUPS_TABLE_NAME);
    PreparedStatement updateJSignaturePrepStmt = db.prepareStatement (updateJSignatureQuery, GROUPS_TABLE_NAME,
      "JSEQUENCE");

    String updateRuleHistorySeqBefore = String.format ("UPDATE %s SET %s=? WHERE %s=?",
      DiscoveryConstants.RULESHISTORY_TBL_NAME, DiscoveryConstants.SEQ_BEFORE_RULE_COL_NAME,
      DiscoveryConstants.SEQ_BEFORE_RULE_COL_NAME);
    PreparedStatement updateRuleHistorySeqBeforePrepStmt = db.prepareStatement (updateRuleHistorySeqBefore,
      DiscoveryConstants.RULESHISTORY_TBL_NAME, DiscoveryConstants.SEQ_BEFORE_RULE_COL_NAME);

    String updateRuleHistorySeqAfter = String.format ("UPDATE %s SET %s=? WHERE %s=?",
      DiscoveryConstants.RULESHISTORY_TBL_NAME, DiscoveryConstants.SEQ_AFTER_RULE_COL_NAME,
      DiscoveryConstants.SEQ_AFTER_RULE_COL_NAME);
    PreparedStatement updateRuleHistorySeqAfterPrepStmt = db.prepareStatement (updateRuleHistorySeqAfter,
      DiscoveryConstants.RULESHISTORY_TBL_NAME, DiscoveryConstants.SEQ_AFTER_RULE_COL_NAME);

    ResultSet result = db.readFromDB (getAllQuery);

    String debugDir = properties.getRootDir () + properties.getProperty (PropertyConstants.DEBUG_DIR);;
    File outputFile = new File (debugDir + "/replacedSequences.csv");
    CSVWriter writer = new CSVWriter (new FileWriter (outputFile, false));

    while (result.next ()) {
      String context = result.getString (1);
      String signature = result.getString (2);
      String jsignature = result.getString (3);

      String newSignature = PDBubbleUtils.fixSignature (signature, context);
      String newJSignature = PDBubbleUtils.fixSignature (jsignature, context);

      boolean writerow = false;

      if (!newSignature.equals (signature)) {
        updateSignaturePrepStmt.setString (1, newSignature);
        updateSignaturePrepStmt.setString (2, signature);
        updateSignaturePrepStmt.execute ();
        writerow = true;
        // update the rule history sequences for before column
        updateRuleHistorySeqBeforePrepStmt.setString (1, newSignature);
        updateRuleHistorySeqBeforePrepStmt.setString (2, signature);
        updateRuleHistorySeqBeforePrepStmt.execute ();
        // update the rule history sequences for after column
        updateRuleHistorySeqAfterPrepStmt.setString (1, newSignature);
        updateRuleHistorySeqAfterPrepStmt.setString (2, signature);
        updateRuleHistorySeqAfterPrepStmt.execute ();
      }

      if (!newJSignature.equals (jsignature)) {
        updateJSignaturePrepStmt.setString (1, newJSignature);
        updateJSignaturePrepStmt.setString (2, jsignature);
        updateJSignaturePrepStmt.execute ();
        writerow = true;
        // update the rule history sequences for before column
        updateRuleHistorySeqBeforePrepStmt.setString (1, newSignature);
        updateRuleHistorySeqBeforePrepStmt.setString (2, signature);
        updateRuleHistorySeqBeforePrepStmt.execute ();
        // update the rule history sequences for after column
        updateRuleHistorySeqAfterPrepStmt.setString (1, newSignature);
        updateRuleHistorySeqAfterPrepStmt.setString (2, signature);
        updateRuleHistorySeqAfterPrepStmt.execute ();
      }

      // write row only if there was any change
      if (writerow) {
        String[] row = { context, signature, newSignature, jsignature, newJSignature };
        writer.writeNext (row);
      }
    }

    // close the writer
    writer.flush ();
    writer.close ();

    // shut down the connection
    db.shutdown ();
    logger.info ("DONE Organizing sequences to display");
  }

  /**
   * @throws PatternDiscoveryException
   */
  public void doPostProcessing () throws PatternDiscoveryException
  {

    logger.info ("Starting PostProcessing");

    // PostProcessDir
    String postProcessDir = properties.getRootDir () + properties.getProperty (PropertyConstants.POST_PROCESS_DIR);

    // Create sequence db if it does not exist:
    String dbUrl = properties.getProperty (PropertyConstants.DB_PREFIX) + properties.getRootDir ()
      + properties.getProperty (PropertyConstants.RESULTS_DB_NAME);
    DebugDBProcessor db = new DebugDBProcessor (dbUrl);
    db.setProperties (properties); // Pass in information to store txt files
    // and database

    // ONLY HERE FOR SPECIAL EVALUATOR - needs gold standard in XML form
    if ("true".equalsIgnoreCase (properties.getProperty (PropertyConstants.PROCESS_MECHTURK_RESULTS))) {
      try {
        MechTurkPostProcessing.createGoldenStandardForEvaluator (postProcessDir + "goldStandard.csv", properties);
      }
      catch (Exception e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GOLD_CREATE_ERR);
      }
    }

    if ("true".equalsIgnoreCase (properties.getProperty (PropertyConstants.ADD_GOLD_STANDARD))) {
      try {
        GoldStandardLabeling.labelAQLResults (properties);
        GoldStandardLabeling.createGoldenStandardMapping (properties);
        GoldStandardLabeling.createID2EntitiesMapping (properties);
        GoldStandardLabeling.applyMappingtoGrouping (properties);
        GoldStandardLabeling.applyJaccardGrouping (properties);
      }
      catch (Exception e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GOLD_APPLY_ERR);
      }

    }

    // Run statistics on data
    if (properties.getProperty (PropertyConstants.RUN_GOLDSTANDARD_STATISTICS).equalsIgnoreCase ("true")) {
      try {
        Statistics.calculatePrecision (db, properties);
      }
      catch (Exception e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GOLD_STATS_ERR);
      }
    }

    db.shutdown ();

  }

  /**
   * @param db
   * @return
   * @throws PatternDiscoveryException
   */
  private static WordIntegerMapping loadMappingFromDictionary (DebugDBProcessor db) throws PatternDiscoveryException
  {
    WordIntegerMapping result = new DefaultWordIntegerMapping ();
    String query = "select * from DICTIONARY ORDER BY wordID ASC";
    try {
      ResultSet rs = db.readFromDB (query);
      while (rs.next ()) {
        int wordID = rs.getInt ("wordID");
        String surface = rs.getString ("surface");
        if (wordID > result.getNextInt ()) {
          result.intForWord ("PLACEHOLDER_WAITNG_FOR" + wordID);
        }
        if (wordID < result.getNextInt ()) { throw new RuntimeException ("double-wordid in dictionary. exiting loading"); }
        result.intForWord (surface);
      }
    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_LOAD_DICTIONARY_DB_ERR);
    }
    return result;
  }

  /**
   * The rule generation consists of three steps. - First, for all possible pairs, the correlation measure is computed.
   * The results are stored in a file sequence_MM.csv in the rule directory. MM stands for the code of the measure. -
   * Then a file -correlations.csv file is computed. Which gets for all correlation values in the desired range,
   * co-occurence counts are stored. - Finally, the rules are generated from this. and stored in a -rule.csv rule. This
   * method constitutes the second and the third step.
   * 
   * @see SequenceOverview
   * @see Correlation2Rules
   * @param properties
   * @param db
   * @param coOccurrenceQuery
   * @param sequenceCountQuery
   * @param measureFiles
   * @throws PatternDiscoveryException
   */
  private void ruleGeneration (ExperimentProperties properties, String coOccurrenceQuery,
    String sequenceCountQuery, String[] measureFiles, Map<Integer, String> sequenceIDMap) throws PatternDiscoveryException
  {
    // Store rule directory
    String ruleDir = properties.getRootDir () + properties.getProperty (PropertyConstants.RULE_DIR);

    for (String measureFile : measureFiles) {
      String correlationFile = measureFile + "-correlations.csv";
      String ruleFile = measureFile + "-rule.csv";
      logger.info ("Starting to compute " + correlationFile);
      List<Integer> discard = null;
      // correlation files
      if (discard == null) discard = new ArrayList<Integer> ();

      // Outputs
      try {
        SequenceOverview.displayCorrelated (properties, measureFile, correlationFile, logger, coOccurrenceQuery,
          sequenceCountQuery, new HashSet<IntPair> (), new HashSet<Integer> (), sequenceIDMap);
      }
      catch (IOException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_CORRELATION_WRITE_ERR);
      }
      catch (SQLException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_CORRELATION_DB_ERR);
      }
      
      workProgressMonitor (Messages.PD_RULES_GENERATION_STEP2_WORK);

      // rule files
      logger.info ("Starting to compute " + ruleFile);
      List<String> ruleTypes = Arrays.asList (properties.getProperty (PropertyConstants.DROP_RULE_TYPES).split (
        "\\s*,\\s*"));
      try {
        Correlation2Rules.correlation2Rules (properties, correlationFile, ruleFile, ruleTypes);
      }
      catch (IOException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RULES_WRITE_ERR);
      }
    }

    try {
      // combine cxy and cyx-output
      BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (ruleDir
        + "/cxyAndcyx-rule.csv"), GroupByNewProcessor.ENCODING));
      HashSet<IntPair> seen = new HashSet<IntPair> ();

      BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (ruleDir
        + "/sequence_cxy.csv-rule.csv"), GroupByNewProcessor.ENCODING));

      while (br.ready ()) {
        String line = br.readLine ();
        if (line.trim ().startsWith ("#")) {
          bw.write (line + "\n");
        }
        else {
          HashRule dr = new HashRule (line);
          if (dr.type.equals (HashRule.Type.DROP)) {
            seen.add (new IntPair (dr.condition[0], dr.condition[1]));
          }
          bw.write (line + "\n");
        }
      }

      br.close ();

      br = new BufferedReader (new InputStreamReader (new FileInputStream (ruleDir + "/sequence_cyx.csv-rule.csv"),
        GroupByNewProcessor.ENCODING));

      String comment = "";
      while (br.ready ()) {
        String line = br.readLine ();
        if (line.trim ().startsWith ("#")) {
          comment += line + "\n";
        }
        else {
          HashRule dr = new HashRule (line);
          if (dr.type.equals (HashRule.Type.DROP) && seen.contains (new IntPair (dr.condition[0], dr.condition[1]))) {
            comment = "";
            continue;
          }
          bw.write (comment + line + "\n");
          comment = "";
        }
      }
      br.close ();
      bw.close ();
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RULES_WRITE_JOIN_ERR);
    }
  }

  /**
   * Uses a SequenceLoader to retrieve AQL matching results from the database aomDbName and feeds them into a paired
   * frequent sequence mining process as defined in MineDBDumpPaired.
   * 
   * @param properties
   * @return
   * @throws SQLException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws PatternDiscoveryException
   */
  private SequenceLoader mineSequences (ExperimentProperties properties) throws PatternDiscoveryException
  {

    Tokenizer lwTok = null;

    TokenizerConfig tokConfig = properties.getTokenizerConfig ();
    if (tokConfig != null) {
      try {
        lwTok = tokConfig.makeTokenizer ();
      }
      catch (Exception e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_MINE_SEQUENCE_CREATE_TOK_ERR);
      }
    }

    SequenceLoader loader = new SequenceLoader (lwTok);

    // Will be filled during sequence loading
    ArrayList<String> transactionIDs = new ArrayList<String> ();

    logger.info ("calling loadAOMDB"); // fills mapping with tokens found in
    // aql output

    String relationshipFields = properties.getProperty (PropertyConstants.ENTITY_FIELD_NAMES);
    HashMap<String, ArrayList<String>> entitiesMap = new HashMap<String, ArrayList<String>> ();

    if (!relationshipFields.isEmpty ()) {
      // Split the input based on ","
      String[] viewAndRelField = relationshipFields.split ("\\s*,\\s*");
      for (String t : viewAndRelField) {
        // Split the view name from the field name
        String[] temp = t.split (PDConstants.VIEW_SPAN_SEPARATOR_REGEX);
        {
          ArrayList<String> fieldNames;
          if (entitiesMap.containsKey (temp[0])) {
            fieldNames = entitiesMap.get (temp[0]);
          }
          else {
            fieldNames = new ArrayList<String> ();
          }
          fieldNames.add (temp[1]);
          entitiesMap.put (temp[0], fieldNames);
        }
      }
    }

    loader.setEntitiesMap (entitiesMap);

    // Returns a mapping of AQL ID to wordID array
    Map<String, int[]> wordTokens = loader.loadAOMDB (transactionIDs, properties);

    // Storing mapping of ID to actual value of annotation
    Map<String, String> extractedValues = loader.getExtractIDMapping ();

    System.gc ();
    logger.info ("calling mineDBDump");

    workProgressMonitor (Messages.PD_SEQUENCE_MINING_STEP1_WORK);
    try {
      MineDBDumpPaired.mineDBDumpInDB (wordTokens, extractedValues, transactionIDs, loader.getMapping (), properties,
        logger, true, false);
    }
    catch (Exception e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_MINE_SEQUENCE_ERR);
    }

    return loader;

  }

  /**
   * loads the content of the database stored by the paired sequence miner.
   * 
   * @see MineDBDumpPaired
   * @see SequencePairPatternProcessor
   * @param db
   * @throws SQLException
   * @throws IOException
   */
  private void supportLoading () throws SQLException, IOException
  {
    String dbUrl = properties.getSequenceDBURL ();
    DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
    db.setProperties (properties);

    // import mining output

    // initialize directory structure

    String groupDir = properties.getRootDir () + properties.getProperty (PropertyConstants.GROUPING_DIR);

    File miningOutput = new File (groupDir + "/" + properties.getProperty (PropertyConstants.AQL_VIEW_NAME) + "-"
      + "frequentMatchPairs.csv");

    db.importCSV ("APP", "sequences_support", "sequenceString " + DiscoveryConstants.SEQUENCE_STRING_COLUMN_TYPE
      + ", sequence int, id " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE + ", mstart int, mend int, unsubsumed int",
      "null", "1,2,3,4,5,6", miningOutput, true);

    // create indices
    for (String column : new String[] { "sequence", "id", "mstart", "mend", "unsubsumed" }) {
      logger.info ("creating index on colum " + column);
      String tableName = "sequences_support";
      db.requireIndex (tableName, false, column);
    }
    // create sequences table
    // write allSequences.csv
    File allSequencesFile = new File (groupDir + "/allSequences.csv");
    allSequencesFile.delete ();
    String query = "SELECT DISTINCT sequenceString, sequence FROM sequences_support";
    ResultSet rs = db.readFromDB (String.format (query));

    BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (allSequencesFile),
      GroupByNewProcessor.ENCODING));

    // bw.write("id,index,wordID,token\n"); cannot include, messes with
    // import
    while (rs.next ()) {
      String[] sequenceString = rs.getString ("sequenceString").split (" ");
      int id = rs.getInt ("sequence");
      int index = 0;
      for (String token : sequenceString) {
        int wordID = mapping.intForWord (token);
        bw.write (String.format ("%d,%d,%d,%s\n", id, index, wordID, token));
        index++;
      }
    }
    bw.close ();

    // import allSequences.csv
    db.importCSV ("APP", "sequences", "sequenceID int, pos smallint, wordID int, word "
      + DiscoveryConstants.WORD_MAPPING_COLUMN_TYPE, "null", "null", allSequencesFile, true);
    // create indices
    reIndexSequences (db, logger);
  }

  /**
   * Stores sequence co-occurrence counts, sequence occurrence counts and sequence content in the specified files.
   * 
   * @param db
   * @param countFile
   * @param coCountFile
   * @param sequenceFile
   * @throws SQLException
   * @throws IOException
   */
  private void counting (ExperimentProperties properties, File countFile, File coCountFile, File sequenceFile) throws SQLException, IOException
  {

    String dbUrl = properties.getSequenceDBURL ();
    DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
    db.setProperties (properties);

    coCountFile.delete ();
    // Compute Measures on DB
    String query = String.format ("SELECT "
      + // cannot output actual
      "max(seq1.sequence) ,  max(seq2.sequence) , count(*), sum(seq1.unsubsumed*seq2.unsubsumed) "
      + // count only those where both are unsubsumed
      "FROM " + "%s seq1, %s seq2 " + "WHERE " + "seq1.sequence<seq2.sequence AND " + "seq2.id = seq1.id AND "
      + "((seq2.mstart >= seq1.mend)OR(seq1.mstart >= seq2.mend)) " + "GROUP BY " + "seq1.sequence,seq2.sequence " +
      // TODO: once we moved to hashing only on unsubsumed
      // matches, the following can be commented back in
      "HAVING " + "sum(seq1.unsubsumed*seq2.unsubsumed)>0" + "", "SEQUENCES_SUPPORT", "SEQUENCES_SUPPORT",
      "SEQUENCES_SUPPORT", "SEQUENCES_SUPPORT");
    logger.info (query);
    String fileName = coCountFile.getAbsolutePath ();
    db.exportQuery (query, fileName);

    countFile.delete ();
    // Compute Measures on DB
    query = String.format ("SELECT " + // cannot output actual
      "max(seq1.sequence), count(*), sum(seq1.unsubsumed)" + "FROM " + "%s seq1 " + "GROUP BY " + "seq1.sequence " +
      // TODO: once we moved to hashing only on unsubsumed matches,
      // the following can be commented back in
      "HAVING " + "sum(seq1.unsubsumed)>0", "SEQUENCES_SUPPORT");
    logger.info (query);
    db.exportQuery (query, countFile.getAbsolutePath ());

    // store mapping sequenceID-> sequence
    sequenceFile.delete ();
    // Compute Measures on DB
    query = String.format ("SELECT DISTINCT " + // cannot output actual
      "seq1.sequence, seq1.sequenceString " + "FROM " + "%s seq1 ORDER BY sequence", "SEQUENCES_SUPPORT");
    logger.info (query);
    db.exportQuery (query, sequenceFile.getAbsolutePath ());
    db.importCSV ("APP", "sequenceMap", "seqID int, sequenceStr " + DiscoveryConstants.SEQUENCE_STRING_COLUMN_TYPE,
      "null", "null", sequenceFile, true);

    String tempQuery = "select count(*), SEQUENCESTRING from SEQUENCES_SUPPORT GROUP BY SEQUENCESTRING";
    File debugSequence = new File (sequenceFile.getParent () + File.separator + "debugSequence.csv");
    debugSequence.delete ();
    db.exportQuery (tempQuery, debugSequence.getAbsolutePath ());
    db.shutdown ();
  }

  /**
   * Called when debugging is turned on and creates debug files for cocount and count
   * 
   * @param db
   * @param properties
   * @param sequenceMap
   * @throws SQLException
   * @throws IOException
   */
  private static void generateDebugFiles (DebugDBProcessor db, ExperimentProperties properties,
    Map<Integer, String> sequenceMap) throws SQLException, IOException
  {
    // Debug outputs count files

    // CoCounts File
    String debugCoCountQuery = "SELECT * from SEQUENCE_NEW_CO_COUNT ORDER BY cocount desc";
    ResultSet lines = db.readFromDB (debugCoCountQuery);

    List<String[]> toWriteList = new ArrayList<String[]> ();
    String rootDir = properties.getRootDir ();
    String debugDir = rootDir + properties.getProperty (PropertyConstants.DEBUG_DIR);

    CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (debugDir + "/co_count_debug.csv"),
      GroupByNewProcessor.ENCODING), ',', '"');

    // Add header to file
    String[] line = { "seq1ID: seq1Str", "seq2ID: seq2Str", "count", "count subsumed" };
    toWriteList.add (line);

    while (lines.next ()) {
      int seq1 = lines.getInt (1); // seq1
      int seq2 = lines.getInt (2); // seq2
      int count = lines.getInt (3); // count
      int countSub = lines.getInt (4); // count subsumed
      String seq1str = sequenceMap.get (seq1);
      String seq2str = sequenceMap.get (seq2);

      String[] addline = { seq1 + ": " + seq1str, seq2 + ": " + seq2str, Integer.toString (count),
        Integer.toString (countSub) };
      toWriteList.add (addline);
    }
    writer.writeAll (toWriteList);
    writer.flush ();
    writer.close ();
    toWriteList.clear (); // clear out list

    // Count File
    String debugCountQuery = "SELECT * from SEQUENCE_NEW_SEQ_COUNT ORDER BY count desc";
    ResultSet linesCount = db.readFromDB (debugCountQuery);

    CSVWriter writerCount = new CSVWriter (new OutputStreamWriter (
      new FileOutputStream (debugDir + "/count_debug.csv"), GroupByNewProcessor.ENCODING), ',', '"');

    // Add header to file
    String[] lineCount = { "seqID: seqString", "count", "count subsumed" };
    toWriteList.add (lineCount);

    while (linesCount.next ()) {
      int seq = linesCount.getInt (1); // seq
      int count = linesCount.getInt (2); // count
      int countSub = linesCount.getInt (3); // count subsumed
      String seq1str = sequenceMap.get (seq);

      String[] addline = { seq + ": " + seq1str, Integer.toString (count), Integer.toString (countSub) };
      toWriteList.add (addline);
    }
    writerCount.writeAll (toWriteList);
    writerCount.flush ();
    writerCount.close ();
    toWriteList.clear (); // clear out list
  }

  /**
   * The rule generation consists of three steps. - First, for all possible pairs, the correlation measure is computed.
   * The results are stored in a file sequence_MM.csv in the rule directory. MM stands for the code of the measure. -
   * Then a file -correlations.csv file is computed. Which gets for all correlation values in the desired range,
   * co-occurence counts are stored. - Finally, the rules are generated from this. This method constitutes the first
   * step. Multiple measures can be applied with one call to this method. It returns, the list of files it generated.
   * 
   * @param properties
   * @param db
   * @param countFile
   * @param coCountFile
   * @param coOccurrenceQuery
   * @param matchCountQuery
   * @param sequenceCountQuery
   * @return
   * @throws PatternDiscoveryException
   */
  private String[] computeMeasures (File countFile, File coCountFile, String coOccurrenceQuery,
    String matchCountQuery, String sequenceCountQuery) throws PatternDiscoveryException
  {
    String dbUrl = properties.getSequenceDBURL ();
    DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
    db.setProperties (properties);
    // Store rule directory
    String rootDir = properties.getRootDir ();

    String ruleDir = rootDir + properties.getProperty (PropertyConstants.RULE_DIR);

    String[] measureFiles = null;
    // load the counts into the DB
    // loading SEQUENCE_NEW_CO_COUNT
    logger.info ("loading sequence_new_co_count");
    try {
      db.importCSV ("APP", "sequence_new_co_count", "seq1 int, seq2 int, coCount int, unSubsumed int", "null", "null",
        coCountFile, true);

      // loading SEQUENCE_NEW_SEQ_COUNT
      String tableName = "sequence_new_seq_count";
      String schema = "APP";
      String columnDefinitions = "seq int, count int, unSubsumed int";
      String sourceTargetColumnMapping = "SEQ,COUNT,UNSUBSUMED";
      String sourceColumnChoice = "1,2,3";
      logger.info ("loading sequence_new_seq_count");
      db.importCSV (schema, tableName, columnDefinitions, sourceTargetColumnMapping, sourceColumnChoice, countFile,
        true);
      // TODO: load dictionary
    }
    catch (Exception e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_COUNT_LOAD_ERR);
    }
    double minCo = 1;

    if (properties.getProperty (PropertyConstants.CO_COUNT_RANGE) != null) {
      ParseResult coPR = properties.parse (properties.getProperty (PropertyConstants.CO_COUNT_RANGE));
      minCo = Math.ceil (coPR.lowerBound ());
    }

    String[] measures = properties.getProperty (PropertyConstants.CORRELATION_MEASURES).split ("\\s*,\\s*");

    // use above parameters
    try {
      measureFiles = ComputeCorrelation.computeMeasureSequenceSequence (properties, measures, ruleDir, minCo,
        coOccurrenceQuery, matchCountQuery, sequenceCountQuery, sequenceMap);
    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_STATISTICS_DB_ERR);
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_STATISTICS_WRITE_ERR);
    }

    db.shutdown ();
    return measureFiles;
  }

  /**
   * If the measure computation is disabled, the following steps still need a list with the measure files. This method
   * lists all the files with the corresponding pattern.
   * 
   * @param properties
   * @return
   */
  private static String[] createMeasureFileList ()
  {

    // Store rule directory
    String baseDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR);
    String fileName = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
    if (fileName.contains (".")) fileName = fileName.substring (0, fileName.indexOf ('.'));
    String ruleDir = baseDir + fileName + File.separator + properties.getProperty (PropertyConstants.RULE_DIR);

    String[] measureFiles;
    String[] measures = properties.getProperty (PropertyConstants.CORRELATION_MEASURES).split ("\\s*,\\s*");
    String targetDir = ruleDir;
    List<String> measureFileList = new ArrayList<String> ();
    for (String measure : measures) {
      measureFileList.add (targetDir + "/sequence_" + measure + ".csv");
    }
    measureFiles = measureFileList.toArray (new String[measureFileList.size ()]);
    return measureFiles;
  }

  /**
   * compute index table on SEQUENCE table.
   * 
   * @param dbUrl
   * @param logger
   * @throws SQLException
   */
  private static void reIndexSequences (DebugDBProcessor db, IPDLog logger) throws SQLException
  {
    logger.info ("starting re-indexing SEQUENCES");
    // drop index sequences_sequenceID_index;
    // db.reCreateSimpleIndex("sequences", "sequenceID");
    // db.reCreateSimpleIndex("sequences", "wordID");
    db.requireIndex ("sequences", false, "sequenceID");
    db.requireIndex ("sequences", false, "wordID");
    // db.writeToDB("DROP INDEX sequence_sequenceIDpos_index")
    try {
      db.writeToDB ("DROP INDEX sequence_sequenceIDpos_index", null);
    }
    catch (Exception e) {/* leise weinend... */
    }
    try {
      db.writeToDB ("create index sequence_sequenceIDpos_index on sequences(sequenceID,pos)", null);
    }
    catch (Exception e) {/* leise weinend... */
    }

    logger.info ("done re-indexing SEQUENCES");
  }

  /**
   * compute index table on DICTIONARY table.
   * 
   * @param dbUrl
   * @param logger
   * @throws SQLException
   */
  private static void reIndexDictionary (DebugDBProcessor db, IPDLog logger) throws SQLException
  {
    logger.info ("starting re-indexing DICTIONARY");
    // drop index sequences_sequenceID_index;
    // db.reCreateSimpleIndex("dictionary", "surface");
    // db.reCreateSimpleIndex("dictionary", "wordID");
    db.requireIndex ("dictionary", false, "surface");
    db.requireIndex ("dictionary", false, "wordID");
    logger.info ("done re-indexing DICTIONARY");
  }

  /**
   * Executes the actual grouping process. Relies on a separate AQL matching process followed by an application of a
   * RuleBasedHasher on the matches which uses the previously generated rule files. Thus, this method simulates the
   * execution of the hashing totally separated from the rule generation.
   * 
   * @see RuleBasedHasher
   * @param properties
   * @param hashInDB
   * @throws PatternDiscoveryException
   * @throws FileNotFoundException
   */
  private static void grouping (ExperimentProperties properties, boolean hashInDB, Map<Integer, String> seqMap) throws PatternDiscoveryException
  {

    String baseDir = properties.getRootDir ();
    // Directory where sequence outputs are stored into
    String relevantSeqDir = baseDir + properties.getProperty (PropertyConstants.RULE_DIR)
      + "preprocessing-relevant.csv";
    String ruleDir = baseDir + properties.getProperty (PropertyConstants.RULE_DIR) + File.separator;
    {
      boolean findFuzzy = !"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_FUZZY_GROUPING));
      int pivot = 0;
      String compare = null;
      HashFactory testHash = null;
      boolean useInfrequentWords = !"false".equalsIgnoreCase (properties.getProperty (PropertyConstants.USE_INFREQUENT_WORDS));
      if (findFuzzy) {
        pivot = Integer.parseInt (properties.getProperty (PropertyConstants.FUZZY_HASHID_COLUMN));
        compare = properties.getProperty (PropertyConstants.FUZZY_GROUPING_COLUMN);
        String dbUrl = properties.getProperty (PropertyConstants.DB_PREFIX) + properties.getRootDir ()
          + properties.getProperty

          (PropertyConstants.SEQUENCE_DB_NAME);
        if (Constants.DEBUG) System.err.println (dbUrl);
        try {
          testHash = new RuleBasedHasher (dbUrl, new StringBufferInputStream (""),
            new FileInputStream (relevantSeqDir), useInfrequentWords, properties, sequenceMap);
        }
        catch (FileNotFoundException e) {
          throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RELEVANT_SEQUENCE_WRITE_ERR);
        }

      }

      File ruleFileGiven = new File (ruleDir + properties.getProperty (PropertyConstants.APPLIED_RULE_FILES));
      if (!ruleFileGiven.isDirectory ()) {
        String ruleFile = ruleFileGiven.getAbsolutePath ();
        String groupingFile = ruleDir + properties.getProperty (PropertyConstants.APPLIED_RULE_FILES) + "-grouping.txt";
        String fuzzyFile = ruleDir + properties.getProperty (PropertyConstants.APPLIED_RULE_FILES)
          + "-grouping.txt.fuzzy.txt";

        groupBy (properties, findFuzzy, pivot, compare, testHash, ruleFileGiven, ruleFile, groupingFile, fuzzyFile,
          hashInDB, seqMap);

      }
      else {
        for (File file : ruleFileGiven.listFiles ()) {
          if (file.getName ().endsWith ("-rule.csv")) {
            String ruleFile = ruleFileGiven.getAbsolutePath ();
            String groupingFile = file.getAbsolutePath () + "-grouping.txt";
            String fuzzyFile = file.getAbsolutePath () + "-grouping.txt.fuzzy.txt";
            groupBy (properties, findFuzzy, pivot, compare, testHash, ruleFileGiven, ruleFile, groupingFile, fuzzyFile,
              hashInDB, seqMap);
          }
        }
      }
    }
  }

  /**
   * Performs the grouping directly based on the support positions computed during mining. Proceeds as follows: - load
   * the rules into a Trie like in the RuleBasedHasher - order the table SEQUENCES_SUPPORT by transaction getting only
   * the unsubsumed - traverse this list and per sequence, apply the rules, hash and output.
   * 
   * @param properties
   * @param hashInDB
   * @throws PatternDiscoveryException
   */
  private void newGrouping (ExperimentProperties properties, boolean hashInDB, WordIntegerMapping mapping) throws PatternDiscoveryException
  {

    // initialize directory structure
    String baseDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR);
    String fileName = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
    if (fileName.contains (".")) fileName = fileName.substring (0, fileName.indexOf ('.'));
    baseDir = baseDir + fileName + File.separator;
    String ruleDir = baseDir + File.separator + properties.getProperty (PropertyConstants.RULE_DIR);

    // Directory where sequence outputs are stored into
    String relevantSeqDir = baseDir + properties.getProperty (PropertyConstants.RULE_DIR)
      + "preprocessing-relevant.csv";

    boolean useInfrequentWords = !"false".equalsIgnoreCase (properties.getProperty (PropertyConstants.USE_INFREQUENT_WORDS));
    File ruleFileGiven = new File (ruleDir + properties.getProperty (PropertyConstants.APPLIED_RULE_FILES));
    boolean findFuzzy = !"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.DISABLE_FUZZY_GROUPING));
    int pivot = 0;
    String compare = null;
    HashFactory testHash = null;
    if (findFuzzy) {
      pivot = Integer.parseInt (properties.getProperty (PropertyConstants.FUZZY_HASHID_COLUMN));
      compare = properties.getProperty (PropertyConstants.FUZZY_GROUPING_COLUMN);
      try {
        testHash = new RuleBasedHasher (properties.getProperty (PropertyConstants.DB_PREFIX)
          + properties.getProperty (PropertyConstants.SEQUENCE_DB_NAME), new StringBufferInputStream (""),
          new FileInputStream (relevantSeqDir), useInfrequentWords, properties, sequenceMap);
      }
      catch (FileNotFoundException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RULES_READ_ERR);
      }
      ((RuleBasedHasher) testHash).setMapping (mapping);

    }
    // TODO: iterate over the rule files and apply
    if (!ruleFileGiven.isDirectory ()) {
      String ruleFile = ruleFileGiven.getAbsolutePath ();
      String groupingFile = ruleDir + properties.getProperty (PropertyConstants.APPLIED_RULE_FILES) + "-grouping2.txt";
      String fuzzyFile = ruleDir + properties.getProperty (PropertyConstants.APPLIED_RULE_FILES)
        + "-grouping2.txt.fuzzy.txt";

      newGroupBy (properties, findFuzzy, pivot, compare, testHash, ruleFileGiven, ruleFile, groupingFile, fuzzyFile,
        hashInDB, mapping);

    }
    else {
      for (File file : ruleFileGiven.listFiles ()) {
        if (file.getName ().endsWith ("-rule.csv")) {
          String ruleFile = ruleFileGiven.getAbsolutePath ();
          String groupingFile = file.getAbsolutePath () + "-grouping2.txt";
          String fuzzyFile = file.getAbsolutePath () + "-grouping2.txt.fuzzy.txt";
          newGroupBy (properties, findFuzzy, pivot, compare, testHash, ruleFileGiven, ruleFile, groupingFile,
            fuzzyFile, hashInDB, mapping);
        }
      }
    }

  }

  private void newGroupBy (ExperimentProperties properties, boolean findFuzzy, int pivot, String compare,
    HashFactory testHash, File file, String ruleFile, String groupingFile, String fuzzyFile, boolean hashInDB,
    WordIntegerMapping mapping) throws PatternDiscoveryException
  {

    // (ExperimentProperties properties, WordIntegerMapping mapping,String
    // ruleFile, String relevantSequences, boolean useInfrequentWords)
    // throws FileNotFoundException, SQLException, IOException{

    boolean useInfrequentWords = !"false".equalsIgnoreCase (properties.getProperty (PropertyConstants.USE_INFREQUENT_WORDS));

    // initialize a hasher
    String relevantSequences = properties.getRootDir () + properties.getProperty (PropertyConstants.RULE_DIR)
      + "preprocessing-relevant.csv";

    RuleBasedHasher hasher;
    try {
      hasher = new RuleBasedHasher (new FileInputStream (ruleFile), new FileInputStream (relevantSequences),
        useInfrequentWords, properties);
      hasher.setMapping (mapping);
    }
    catch (FileNotFoundException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RELEVANT_SEQUENCE_WRITE_ERR);
    }

    // load the sequences as strings (for later output)
    // if unmatched parts are taken into account, load the sequences as
    // integers
    Map<String, int[]> transactions;
    List<String> texts = new ArrayList<String> ();

    Tokenizer lwTok = null;

    TokenizerConfig tokConfig = properties.getTokenizerConfig ();
    if (tokConfig != null) {
      try {
        lwTok = tokConfig.makeTokenizer ();
      }
      catch (Exception e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_MINE_SEQUENCE_CREATE_TOK_ERR);
      }
    }

    SequenceLoader loader = new SequenceLoader (lwTok);

    loader.setMapping (mapping);
    String aomDbUrl = properties.getProperty (PropertyConstants.DB_PREFIX)
      + properties.getProperty (PropertyConstants.RESULTS_DB_NAME);
    String tableName = AQLGroupByPersist.SCHEMA + "." + "type_"
      + properties.getProperty (PropertyConstants.AQL_VIEW_NAME).replace (".", "__");
    String fields = DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME.replaceAll ("\\.", "__");
    String separator = GroupByNewProcessor.COL_SEPARATOR;

    ArrayList<String> transactionIDs = new ArrayList<String> ();
    try {
      transactions = loader.loadAOMDB (aomDbUrl, tableName, fields, transactionIDs, separator, texts, properties);
    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_LOAD_SEQUENCES_DB_ERR);
    }
    Map<String, String> extractIDMap = loader.getExtractIDMapping ();
    // order the table SEQUENCES_SUPPORT by transaction getting only the
    // unsubsumed
    DebugDBProcessor db = new DebugDBProcessor (properties.getProperty (PropertyConstants.DB_PREFIX)
      + properties.getRootDir () + properties.getProperty

      (PropertyConstants.SEQUENCE_DB_NAME));
    db.setProperties (properties); // Pass in information to store txt files
    // and database
    String query = "SELECT * FROM SEQUENCES_SUPPORT WHERE unsubsumed = 1 ORDER BY ID, MSTART ASC";
    // traverse this list and per sequence, apply the rules, hash and
    // output.

    String lastTransaction = "";
    Collection<Integer> foundSequences = new HashSet<Integer> ();
    Collection<IntPair> foundStartEnd = new LinkedList<IntPair> ();
    // Collection<String>sequenceStringsFound = new
    // LinkedList<String>();//TODO: debug only

    File tempFile = new File (properties.getRootDir () + properties.getProperty (PropertyConstants.GROUPING_DIR)
      + "/newHashingTemp.csv");

    try {
      CSVWriter out = new CSVWriter (new OutputStreamWriter (new FileOutputStream (tempFile),
        GroupByNewProcessor.ENCODING), ',', '"');

      int width = DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME.split (",").length;

      ResultSet rs = db.readFromDB (query);
      while (rs.next ()) {
        int sequence = rs.getInt ("sequence");
        String transaction = rs.getString ("id");
        int start = rs.getInt ("mstart");
        int end = rs.getInt ("mend");

        if (lastTransaction == "") {
          lastTransaction = transaction;
        }

        if (lastTransaction != transaction) {
          int[] thisTransaction = transactions.get (lastTransaction);
          if (thisTransaction == null) logger.info ("WARNING: Transaction not found: " + transaction);
          // output sequence as original vs. sequence as reconstructed
          // from transaction
          // logger.info("Original sequence:"+transactions.get(lastTransaction));
          // logger.info("Original sequence:"+texts.get(lastTransaction));
          //
          // String reconstructed = "";
          // for (String sequenceString : sequenceStringsFound) {
          // String[] sequenceContentStrings =
          // sequenceString.split("-");
          // for (String item : sequenceContentStrings) {
          // int itemNo = Integer.parseInt(item);
          // reconstructed+= mapping.wordForInt(itemNo)+" ";
          // }
          // reconstructed+=", ";
          // }
          //
          // logger.info("Sequence from transaction:"+reconstructed);

          // take care of unmatched parts
          for (IntPair intPair : foundStartEnd) {
            for (int i = intPair.x; i < intPair.y; i++) {
              if (i >= thisTransaction.length) continue;
              thisTransaction[i] = -1;
            }
          }
          Collection<Integer> unMatched = new LinkedList<Integer> ();
          if (useInfrequentWords) {
            for (int item : thisTransaction) {
              if (item != -1) unMatched.add (item);
            }
          }
          // compute hash
          // TODO: log found sequences
          // String foundSequenceSet = "";
          // for (Integer sequenceID : foundSequences) {
          // foundSequenceSet +=
          // FindFuzzyGroups.sequence2String(sequenceID, db)+" + ";
          // }
          // logger.info(foundSequenceSet);
          int hashCode = hasher.hash (foundSequences, unMatched);
          // output
          // String theText = texts.get(lastTransaction);
          String theText = extractIDMap.get (lastTransaction);
          // if(transaction>200)break;
          // String theText = foundSequenceSet +
          // " IN: "+texts.get(transaction);
          // : split text by separator and create output array
          String[] columns = theText.split (" " + separator + " ");

          String[] columnsWithHash = new String[width + 1];
          columnsWithHash[0] = "" + hashCode;
          for (int i = 0; i < width; i++) {
            String column;
            if (i < columns.length)
              column = columns[i];
            else
              column = " xxx ";
            if (column.length () >= 255) column = column.substring (0, 253);
            column = column.replace ('\n', ' ');
            column = GroupByPreprocessor.sqlEscape (column);
            columnsWithHash[i + 1] = column;
          }
          out.writeNext (columnsWithHash);
          lastTransaction = transaction;
          foundStartEnd = new LinkedList<IntPair> ();
          foundSequences = new HashSet<Integer> ();
        }
        // sequenceStringsFound = new LinkedList<String>();
        foundSequences.add (sequence);
        foundStartEnd.add (new IntPair (start, end));
        // sequenceStringsFound.add(rs.getString("sequenceString"));
      }
      out.close ();
      String columns = "hashCode INT";
      for (int i = 0; i < properties.getProperty (PropertyConstants.GROUP_BY_FIELD_NAME).split (",").length; i++) {
        columns += ", column" + i + " " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE;
      }
      db.importCSV ("APP", "hash_output", columns, "null", "null", tempFile, true);
      db.writeToDB ("create index hash_output_index on hash_output(hashCode)", null);
      // tempFile.delete();
      (new File (groupingFile)).delete ();
      db.exportQuery ("SELECT (SELECT count(*) FROM hash_output hashtable2 WHERE "
        + "hashtable2.hashCode = hashtable.hashCode) as n, hashtable.* FROM hash_output hashtable ORDER BY "
        + "n DESC, hashtable.hashCode " + "DESC ", groupingFile);

    }
    catch (SQLException e) {
      db.shutdown ();
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GROUPING_DB_ERR);
    }
    catch (IOException e) {
      db.shutdown ();
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GROUPING_WRITE_ERR);
    }

    // findFuzzy
    // TODO: this does not yet work because the hasher would be used in the
    // wrong way.
    if (findFuzzy) {
      FindFuzzyGroups.findFuzzy (properties, groupingFile, fuzzyFile, pivot, compare, null, testHash);
    }

    db.shutdown ();
  }

  /**
   * Starts the hashing, grouping and fuzzy-group detection for an individual rule file.
   * 
   * @param properties
   * @param findFuzzy
   * @param pivot
   * @param compare
   * @param testHash
   * @param file
   * @param ruleFile
   * @param groupingFile
   * @param fuzzyFile
   * @param hashInDB
   * @throws PatternDiscoveryException
   */
  private static void groupBy (ExperimentProperties properties, boolean findFuzzy, int pivot, String compare,
    HashFactory testHash, File file, String ruleFile, String groupingFile, String fuzzyFile, boolean hashInDB,
    Map<Integer, String> seqMap) throws PatternDiscoveryException
  {

    RuleBasedHasher hasher = null;
    // Commented out hashinDB, not functioning in this version
    // if(hashInDB){
    // HashInDB.doHash(ruleFile, groupingFile, properties, logger);

    // }else{
    hasher = (RuleBasedHasher) AQLGroupByPersist.makeHasher (properties, ruleFile, seqMap);

    AQLGroupByPersist.runGroupBy (properties, file.getAbsolutePath (), hasher, seqMap);

    // }
    if (findFuzzy) {
      if (hasher == null) hasher = (RuleBasedHasher) AQLGroupByPersist.makeHasher (properties, ruleFile, seqMap);
      FindFuzzyGroups.findFuzzy (properties, groupingFile, fuzzyFile, pivot, compare, hasher, testHash);
    }
  }

  private static void sequenceGroup (ExperimentProperties properties, Map<Integer, String> seqMap) throws PatternDiscoveryException
  {
    try {
      AQLGroupByPersist.sequenceGroup (properties, seqMap);
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_COMMON_SEQ_GROUPING_WRITE_ERR);
    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_COMMON_SEQ_GROUPING_DB_ERR);
    }
  }

  public void cancel ()
  {
    do_cancel = true;
  }

  public void resetCancell ()
  {
    // since, we can not for sure know that at the state the processing was stopped is secure for reuse we, clean the
    // dirs
    setProgressMonitorMessage (Messages.PD_CANCELED, 99);
    DebugDBProcessor.tearDown ();
    cleanDirs ();
    do_cancel = false;
  }

  public boolean isCancelling ()
  {
    return do_cancel;
  }

  public static void setProgressMonitorMessage (String message, int work)
  {
    if (monitor != null && !monitor.isCanceled ()) {
      monitor.setTaskName (message);
      monitor.worked (work);
    }
  }
  
  public static void workProgressMonitor (int work)
  {
    if (monitor != null && !monitor.isCanceled ()) {
      monitor.worked (work);
    }
  }

}
