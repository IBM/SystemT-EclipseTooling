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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis.SequenceLoader;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DiscoveryConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.FileBasedInsert;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.Insert;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.pairs.HashBuckets;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.pairs.IntPair;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.pairs.IntStringPair;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.nlp.WordIntegerMapping;

/**
 * TODO: choice of database: if stuck with Derby, try MySQL (preliminarily) or DB2 the following processing is done The
 * creation of token_map as well as iterations one and two are done in memory. Schema of token_map: token INT, doc INT,
 * pos INT Then, a table is created that will contain the data during mining and serve as output. Schema: seqID INT, doc
 * INT, m_start INT, m_end INT, length INT, unsub SMALLINT TODO: how do I map sequence IDs back to sequences (Options:
 * Do it in Java, Hash, Faithful hash->Compressed String->kann nicht passen, token_map!) I decided to implement it as a
 * sort-select plus Java processing because: - I was not able to express in Derby to take the next free sequence id for
 * all previous ID+next token pairs of a kind - I was not able to express an update statement in derby which checks if
 * there is a subsuming sequence (no EXISTS or other reference on other tables in update), may be possible through
 * SELECT FOR UPDATE though - The only query I could come up with for direct update is sort of an ugly double-self join
 * thing and I'm not sure if the DB would be able to do it properly. - And anyway, subsumption update would have meant
 * re-processing the entire data again which might have been inefficient DONE: Derby inserts are so incredibly slow
 * except if a CSV is imported, so I decided to use cache all inserts to file (cf. TestUtils for a comparison to JDBC
 * Batch insert) TODO: Do I need to re-index in every iteration? DONE: Test class DONE: reuse test case from Paired
 * Sequence Miner TODO: large-scale test Potential for optimization: TODO: delete index prior to insertion, re-build
 * them afterwards
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *  Chu Made major changes to subsumed (removed entirely) Added in debugging files given debug property
 *         Multiple bug fixes and cleanup
 */
public class DBSequenceMiner
{


  
	// TODO: field
  // TODO: the URL of the database the mining is done in
  private String dbUrl;
  private DebugDBProcessor db;
  int minSupport;
  private IPDLog logger;
  String dbUser;
  String dbPassword;

  WordIntegerMapping mapping;

  // For debugging
  private boolean debug;
  private String debugDir;

  // Column numbers for database results
  private final int ID = 2;
  private final int SEQ_ID = 1;
  @SuppressWarnings("unused")
  private final int SEQ_STRING = 0;
  private final int M_START = 3;
  private final int M_END = 4;
  private final int UNSUBSUMED = 5;

  // query templates
  final static String ITERATION = "SELECT inst.seqID, inst.doc,inst.m_start, nextTok.token FROM sequence_instances inst, token_map nextTok "
    + "WHERE " + "inst.length= %d " + // establish iteration
    "AND nextTok.doc = inst.doc AND nextTok.pos = inst.m_end " + // join
    // to
    // get
    // next
    // char
    "ORDER BY inst.seqID, nextTok.token"; // order

  final static String SUBSUMPTION_UPDATE_TEMPLATE = "UPDATE sequence_instances SET unsub=0 WHERE length= ? AND doc=? AND (m_start=? OR m_end=?)";

  // TODO: build a db backed WordIntegerMapping
  // final static String MAP_INSERT_TEMPLATE=
  // "INSERT INTO token_map (token,doc,pos) VALUES (?,?,?)";
  // final static String SEQ_INSERT_TEMPLATE=
  // "INSERT INTO sequence_instances (seqID, doc, m_start, m_end, length, unsub) VALUES (?, ?, ?, ?, ?, ?)";

  /**
   * Constructure
   */
  public DBSequenceMiner (String dbUrl, String dbUser, String dbPassword)
  {
    this.dbUrl = dbUrl;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;
  }

  /**
   * Main function that mines sequences and stores into DB. Iterates maxPatternSize. Each iteration checks frequency of
   * sequence, adds a seqID, and adds to sequence_instance DB
   * 
   * @param wordTokens
   * @param extractedValues
   * @param mapping
   * @param minPatternSize
   * @param maxPatternSize
   * @param minSupport
   * @param properties
   * @param externalLogger
   * @throws SQLException
   * @throws IOException
   * @throws PatternDiscoveryException
   */
  public void doMine (Map<String, int[]> wordTokens, Map<String, String> extractedValues, WordIntegerMapping mapping,
    int maxPatternSize, int minPatternSize, int minSupport, ExperimentProperties properties, IPDLog externalLogger) throws SQLException, IOException, PatternDiscoveryException
  {

    // Logging setup
    this.logger = externalLogger;

    // Store property information
    this.minSupport = minSupport;
    this.debug = properties.getProperty (PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase ("true");
    String baseDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR);
    String inputFile = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
    if (inputFile.contains (".")) inputFile = inputFile.substring (0, inputFile.indexOf ('.'));
    String debugDir = baseDir + inputFile + File.separator + properties.getProperty (PropertyConstants.DEBUG_DIR);
    this.debugDir = debugDir;
    this.mapping = mapping;

    // create DBProcessor
    logger.info ("connecting to: " + dbUrl);
    db = new DebugDBProcessor (dbUrl, dbUser, dbPassword, properties.getRootDir ());
    setupDB ();

    // create token_map content
    createTokenMap (wordTokens, properties);
    // frequent size 1 sequences (in memory)
    HashBuckets<Integer, IntStringPair> singles = iteration1 (wordTokens);
    // frequent size 2 sequences (in memory)
    HashBuckets<IntPair, IntStringPair> doubles = iteration2 (wordTokens, singles);
    // write sequence_instances with only iter1 and iter2
    writeSeqInst (singles, doubles, extractedValues, properties);

    // Find rest of sequences (iter3, iter4, etc) up to maxPatternSize:
    // writes to DB
    for (int n = 3; n <= maxPatternSize; n++) {
      boolean more = iteration (n, properties);
      if (!more) break;
    }

    // export result
    ResultSet rs = db.readFromDB ("SELECT seqID, doc, m_start, m_end, unsub FROM sequence_instances");

    String groupDir = baseDir + inputFile + File.separator + properties.getProperty (PropertyConstants.GROUPING_DIR);

    BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (groupDir + "/"
      + properties.getProperty (PropertyConstants.AQL_VIEW_NAME) + "-" + "frequentMatchPairs.csv"),
      GroupByNewProcessor.ENCODING));

    // For debugging purposes only
    String[] header = { "seqID", "ID", "mstart", "mend", "unsub", "origString" };

    CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (debugDir
      + "frequentMatchPairsDebug.csv"), GroupByNewProcessor.ENCODING), ',', '"');

    List<String[]> toWriteList = new ArrayList<String[]> ();
    toWriteList.add (header);

    CSVWriter writer2 = new CSVWriter (new OutputStreamWriter (new FileOutputStream (debugDir
      + "frequentMatchPairsSubDebug.csv"), GroupByNewProcessor.ENCODING), ',');

    List<String[]> toWriteListSub = new ArrayList<String[]> ();
    toWriteListSub.add (header);

    // Start going through all entries in sequence_instances and print to
    // CSV file
    while (rs.next ()) {
      String docID = "\"" + rs.getString (ID) + "\"";
      String extractTemp = extractedValues.get (docID);
      int[] sequenceTemp = SequenceLoader.stringToSequence (extractTemp, mapping);
      ArrayList<Integer> intTokens = new ArrayList<Integer> ();

      // make sure that the number of tokens for this pattern are in the wanted range provided by the user
      int m_start = rs.getInt (M_START);
      int m_end = rs.getInt (M_END);
      int n_tolkens = m_end - m_start;
      if (n_tolkens <= maxPatternSize && n_tolkens >= minPatternSize) {
        for (int k = m_start; k < rs.getInt (M_END); k++) {
          intTokens.add (sequenceTemp[k]);
        }
        int[] sequenceSec = new int[intTokens.size ()];
        Iterator<Integer> iter = intTokens.iterator ();
        for (int i = 0; i < intTokens.size (); i++) {
          sequenceSec[i] = iter.next ().intValue ();
        }
        String seqString = SequenceLoader.sequenceToString (sequenceSec, mapping);
        bw.write ("\"" + seqString + "\"," + rs.getInt (SEQ_ID) + "," + docID + "," + rs.getInt (M_START) + ","
          + rs.getInt (M_END) + "," + rs.getInt (UNSUBSUMED) + "\n");

        // Debugging purposes only (See String outputs for frequent match
        // pairs)
        String extract = "";
        if (debug) {
          extract = extractedValues.get (rs.getString (ID));
          String[] line = { seqString, Integer.toString (rs.getInt (SEQ_ID)), docID,
            Integer.toString (rs.getInt (M_START)), Integer.toString (rs.getInt (M_END)),
            Integer.toString (rs.getInt (UNSUBSUMED)), extract };
          if (rs.getInt (UNSUBSUMED) == 0) {
            toWriteListSub.add (line);
          }
          else {
            toWriteList.add (line);
          }
        }
      }
    }

    // Debugging write only
    writer.writeAll (toWriteList);
    writer.flush ();
    writer.close ();
    bw.flush ();
    bw.close ();
    writer2.writeAll (toWriteListSub);
    writer2.flush ();
    writer2.close ();

    logger.info ("indexing table sequence_instances by seqID and doc");
    db.requireIndex ("sequence_instances", false, "seqID");
    db.requireIndex ("sequence_instances", false, "doc");

    db.shutdown ();
    logger.info ("done indexing table sequence_instances by seqID and doc");
  }

  /**
   * Write sequences to sequence database: sequence_instances Also gives all sequences a seqID
   * 
   * @param singles
   * @param doubles
   * @param extractedValues
   * @throws SQLException
   * @throws IOException
   */
  private void writeSeqInst (HashBuckets<Integer, IntStringPair> singles, HashBuckets<IntPair, IntStringPair> doubles,
    Map<String, String> extractedValues, ExperimentProperties properties) throws SQLException, IOException
  {

    // re-index
    logger.info ("indexing table sequence_instances");
    db.requireIndex ("sequence_instances", false, "length", "doc", "m_end");
    db.requireIndex ("sequence_instances", false, "length", "doc", "m_start");
    db.requireIndex ("token_map", false, "doc", "pos");
    db.requireIndex ("token_map", false, "token");
    logger.info ("done indexing table sequence_instances");
    int added = 0;
    int seqID = 0;

    // Debugging - print sequence_instances to file
    List<String[]> toWriteList = new ArrayList<String[]> ();
    String[] header = { "seqID", "ID", "original string", "mstart", "mend", "word", "length", "subsumed?" };
    toWriteList.add (header);

    Insert insert = new FileBasedInsert (db, properties.getRootDir ()
      + properties.getProperty (PropertyConstants.DEBUG_DIR));
    for (int sequence : singles.keySet ()) {
      for (IntStringPair docPos : singles.get (sequence)) {
        added++;
        if (debug) {
          String[] line = { "" + seqID, "" + docPos.x, extractedValues.get (docPos.x), "" + docPos.y,
            "" + (docPos.y + 1), mapping.wordForInt (sequence), "" + 1, "" + 1 };
          toWriteList.add (line);
        }
        insert.insert ("" + seqID, "" + docPos.x, "" + docPos.y, "" + (docPos.y + 1), "" + 1, "" + 1);
      }
      if (added > 100000) {
        logger.info ("sequence_instance (singles) creation flushing " + added);
        added = 0;
        insert.flush ();
      }
      seqID++;
    }
    for (IntPair sequence : doubles.keySet ()) {
      for (IntStringPair docPos : doubles.get (sequence)) {
        added++;
        if (debug) {
          String[] line = { "" + seqID, "" + docPos.x, extractedValues.get (docPos.x), "" + docPos.y,
            "" + (docPos.y + 2), mapping.wordForInt (sequence.x) + " " + mapping.wordForInt (sequence.y), "" + 2,
            "" + 1 };
          toWriteList.add (line);
        }
        insert.insert ("" + seqID, "" + docPos.x, "" + docPos.y, "" + (docPos.y + 2), "" + 2, "" + 1);
      }
      if (added > 100000) {
        logger.info ("sequence_instance (doubles) creation flushing " + added);
        added = 0;
        insert.flush ();
      }
      seqID++;
    }
    // Debugging - print sequence_instances to file
    if (debug) {
      CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (debugDir
        + "/support_instance_debug.csv"), GroupByNewProcessor.ENCODING), ',', '"');

      writer.writeAll (toWriteList);
      writer.flush ();
      writer.close ();
    }

    logger.info ("writing table sequence_instances");
    insert.doneInserting ();
    insert.load ("APP", "sequence_instances", "seqID, doc, m_start, m_end, length, unsub");
    logger.info ("done writing table sequence_instances");

    // free memory
    singles.clear ();
    doubles.clear ();
    toWriteList.clear ();
    System.gc ();
  }

  /**
   * Token Map generation - creates database table: token_map in sequenceDB TODO: why is token map creation so slow? --
   * delete index
   */
  private void createTokenMap (Map<String, int[]> wordTokens, ExperimentProperties properties) throws SQLException, IOException
  {
    db.dropSimpleIndex ("token_map", "doc,pos");
    db.dropSimpleIndex ("token_map", "token");
    logger.info ("starting createTokenMap");
    // TODO: create indices here (more efficient than later)
    // String sql = MAP_INSERT_TEMPLATE;
    Insert insert = new FileBasedInsert (db, properties.getRootDir ()
      + properties.getProperty (PropertyConstants.DEBUG_DIR));
    int count = 0;
    for (String sequenceID : wordTokens.keySet ()) {
      int[] doc = wordTokens.get (sequenceID);
      for (int pos = 0; pos < doc.length; pos++) {
        int token = doc[pos];
        insert.insert ("" + token, "" + sequenceID, "" + pos);
      }

      count++;
      if (count % 10000 == 0) {// experimentally determined to be a good
        // amount to insert. 100 takes twice as
        // long 10000 1.5 times
        logger.info ("docs mapped:" + count);
        insert.flush ();
      }
    }
    insert.doneInserting ();
    logger.info ("loading");
    insert.load ("APP", "token_map", "token,doc,pos");
    logger.info ("index doc,pos");
    db.requireIndex ("token_map", false, "doc", "pos");
    logger.info ("index token");
    db.requireIndex ("token_map", false, "token");
  }

  /**
   * Iteration 1: Go through all words and store into HashBuckets to determine Frequency Then only store words that have
   * frequency larger than specified
   * 
   * @param sequences
   * @return
   * @throws SQLException
   * @throws IOException
   */
  private HashBuckets<Integer, IntStringPair> iteration1 (Map<String, int[]> wordTokens) throws SQLException, IOException
  {

    logger.info ("starting iteration1: start counting");
    HashBuckets<Integer, IntStringPair> singleItemSupport = new HashBuckets<Integer, IntStringPair> ();

    // iterate over the DB - transactionID = ID (unique to each annotation)
    for (String transactionID : wordTokens.keySet ()) {
      int[] doc = wordTokens.get (transactionID);
      // Take the count portion of the uniqueID to hash with
      // int transactionID2hash =
      // Integer.parseInt(transactionID.split("#")[1]);
      for (int pos = 0; pos < doc.length; pos++) {
        int token = doc[pos];
        IntStringPair l1SupportItem = new IntStringPair (transactionID, pos);
        singleItemSupport.put (token, l1SupportItem);
      }
      // transactionID2hash++;
      // if(transactionID2hash%
      // 100000==0)logger.info("iteration 1: "+transactionID+" transactions counted");
    }
    logger.info ("iteration 1: done counting");
    System.gc ();
    logger.info ("total depth 1 items before prune: " + singleItemSupport.keySet ().size ());

    if (debug) { // Added in debugging - count number of times a word
      // appears
      writeCSVDebugIter1 ("preWordCount.csv", singleItemSupport);
    }

    logger.info ("removing all words with count less than: " + minSupport);
    // delete infrequent entries
    for (Iterator<Integer> iterator = singleItemSupport.keySet ().iterator (); iterator.hasNext ();) {
      Integer singleItem = (Integer) iterator.next ();
      Collection<IntStringPair> support = singleItemSupport.get (singleItem);
      int supportSize = support.size ();
      if (supportSize < minSupport) {
        iterator.remove ();
      }
    }
    logger.info ("frequent depth 1 items after prune: " + singleItemSupport.keySet ().size ());

    if (debug) {// count number of times a word appears after it has been
      // removed
      writeCSVDebugIter1 ("frequentWordCount.csv", singleItemSupport);
    }

    System.gc ();
    return singleItemSupport;
  }

  /**
   * Iteration 2: Go through all words pairs and store into HashBuckets to determine Frequency Then only words pairs
   * that have frequency larger than specified are kept
   * 
   * @param sequences
   * @param singleItemSupport
   * @return
   * @throws IOException
   */
  private HashBuckets<IntPair, IntStringPair> iteration2 (Map<String, int[]> wordTokens,
    HashBuckets<Integer, IntStringPair> singleItemSupport) throws IOException
  {
    HashBuckets<IntPair, IntStringPair> doubleItemSupport = new HashBuckets<IntPair, IntStringPair> ();
    System.gc ();

    // Store word pairs into HashBuckets to count frequency
    for (String transactionID : wordTokens.keySet ()) {
      int[] doc = wordTokens.get (transactionID);
      int lastToken = -1;
      boolean lastTokenFrequent = false;
      // Take the count portion of the uniqueID to hash with
      // int transactionID2hash =
      // Integer.parseInt(transactionID.split("#")[1]);
      for (int pos = 0; pos < doc.length; pos++) {
        int token = doc[pos];
        IntStringPair l2SupportItem = new IntStringPair (transactionID, pos - 1);
        // check if individual items were frequent
        boolean tokenFrequent = singleItemSupport.get (token) != null;
        // for splitting: require that token or lastToken are in Q
        if (lastTokenFrequent && tokenFrequent) {
          IntPair tokenPair = new IntPair (lastToken, token);
          doubleItemSupport.put (tokenPair, l2SupportItem);
        }
        lastToken = token;
        lastTokenFrequent = tokenFrequent;
      }
      // transactionID2hash++;
      // if(transactionID2hash% 100000==0)logger.info("iteration 2: "+
      // transactionID+" transactions counted");
    }
    logger.info ("total depth 2 items before prune: " + doubleItemSupport.keySet ().size ());

    if (debug) { // Added in debugging - count number of times a word
      // appears
      writeCSVDebugIter2 ("preWordCountDoubles.csv", doubleItemSupport);
    }

    logger.info ("pruning depth 2 where item has less than: " + minSupport);
    // delete infrequent entries
    for (Iterator<IntPair> iterator = doubleItemSupport.keySet ().iterator (); iterator.hasNext ();) {
      IntPair itemPair = (IntPair) iterator.next ();
      Collection<IntStringPair> support = doubleItemSupport.get (itemPair);
      int supportSize = support.size ();
      if (supportSize < minSupport) {
        iterator.remove ();
      }
    }
    logger.info ("frequent depth 2 items after prune: " + doubleItemSupport.keySet ().size ());

    if (debug) {// count number of times a word appears after it has been
      // removed
      writeCSVDebugIter2 ("preWordCountDoubles.csv", doubleItemSupport);
    }

    return doubleItemSupport;
  }

  /**
   * Iteration 3+
   * 
   * @param iteration
   * @return true if at least one new sequence has been found
   * @throws SQLException
   * @throws IOException
   */
  private boolean iteration (int iteration, ExperimentProperties properties) throws SQLException, IOException
  {
    boolean result = false;
    // get all sequences with following character
    String query = String.format (ITERATION, iteration - 1);
    logger.info (query);
    ResultSet rs = db.readFromDB (query);

    List<SequenceInstance> toAdd = new LinkedList<SequenceInstance> ();
    List<SequenceInstance> currentInstances = new LinkedList<SequenceInstance> ();
    int lastTok = -1;
    int lastSeq = -1;
    int currentSeqID = firstFreeSeqID ();
    PreparedStatement stmt = db.prepareStatement (SUBSUMPTION_UPDATE_TEMPLATE, "sequence_instances", "unsub");
    Insert insert = new FileBasedInsert (db, properties.getRootDir ()
      + properties.getProperty (PropertyConstants.DEBUG_DIR));

    // process, count, make insertion candidates
    logger.info ("process, count, make insertion candidates");

    String doc = "";
    int seq = -1, start = -1, end = -1, tok = -1;

    while (rs.next ()) {
      seq = rs.getInt (1);
      doc = "\"" + rs.getString (2) + "\"";
      // int doc = rs.getInt(2);
      start = rs.getInt (3);
      end = start + iteration - 1;
      tok = rs.getInt (4);
      if (lastTok != tok || lastSeq != seq) {
        if (currentInstances.size () >= minSupport) {
          toAdd.addAll (currentInstances);
          stmt.setInt (1, iteration - 1);
          // stmt.setInt(2, doc);
          stmt.setString (2, doc);
          stmt.setInt (3, start);
          stmt.setInt (4, end + 1);
          stmt.addBatch ();
          currentSeqID++; // moved outside out if statement or else
          // sequences end up with similar IDs
          if (lastSeq != seq) {
            result = true;
          }
        }
        currentInstances = new LinkedList<SequenceInstance> ();
      }
      lastTok = tok;
      lastSeq = seq;
      currentInstances.add (new SequenceInstance (start, end + 1, doc, currentSeqID, true));
      // bulk-insert
      if (toAdd.size () > 10000) {
        logger.info ("inserting anoter " + toAdd.size () + " instances");
        bulkInsert (toAdd, insert);
        toAdd.clear ();
      }
    }

    // we need to check one more time to make sure after the last value was added

    if (currentInstances.size () >= minSupport) {
      toAdd.addAll (currentInstances);
      stmt.setInt (1, iteration - 1);
      // stmt.setInt(2, doc);
      stmt.setString (2, doc);
      stmt.setInt (3, start);
      stmt.setInt (4, end + 1);
      stmt.addBatch ();
      currentSeqID++; // moved outside out if statement or else
      // sequences end up with similar IDs
      if (lastSeq != seq) {
        result = true;
      }
    }

    logger.info ("updating subsumption");
    // stmt.executeBatch(); //Removed how subsumption is done
    logger.info ("inserting");
    bulkInsert (toAdd, insert);
    insert.doneInserting ();
    insert.load ("APP", "sequence_instances", "seqID, doc, m_start, m_end, length, unsub");
    return result;
  }

  /**
   * Returns first available sequenceID
   * 
   * @return
   * @throws SQLException
   */
  private int firstFreeSeqID () throws SQLException
  {
    ResultSet rs = db.readFromDB ("SELECT max(seqID) FROM sequence_instances");
    if (rs.next ()) {
      return rs.getInt (1) + 1;
    }
    else {
      return 0;
    }
  }

  /**
   * Insert a list of sequence instances into the corresponding table
   */
  private void bulkInsert (List<SequenceInstance> toAdd, Insert insert) throws SQLException, IOException
  {
    if (toAdd.size () == 0) return;
    // String sql = SEQ_INSERT_TEMPLATE;
    // PreparedStatement stmt = db.getNewConnection().prepareStatement(sql);

    for (SequenceInstance si : toAdd) {
      insert.insert ("" + si.seqID, "" + si.doc, "" + si.start, "" + si.end, "" + (si.end - si.start), ""
        + (si.unsub ? 1 : 0));
    }
    insert.flush ();

  }

  /**
   * Table setup
   */
  private void setupDB () throws SQLException
  {

    // create or wipe tables
    // TODO: put back in
    // doc = ID
    logger.info ("createOrWipe - token_map");
    // The "doc" field is really the uniqueID field (legacy from prior code)
    createOrWipe ("token_map", "token INT, doc " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE + ", pos INT");
    logger.info ("createOrWipe - sequence_instances");
    createOrWipe ("sequence_instances", "seqID INT, doc " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE
      + ", m_start INT, m_end INT, length INT, unsub SMALLINT");
    logger.info ("done");
  }

  /**
   * Given a fileName and singleItemSupport List, will write to specified file name
   * 
   * @param fileName
   * @param toWriteList
   * @throws IOException
   */
  private void writeCSVDebugIter1 (String fileName, HashBuckets<Integer, IntStringPair> singleItemSupport) throws IOException
  {

    String[] header = { "word", "wordID", "count" };
    List<String[]> toWriteList = new ArrayList<String[]> ();
    toWriteList.add (header);

    for (int key : singleItemSupport.keySet ()) {
      String seqWord = mapping.wordForInt (key);
      Collection<IntStringPair> countPair = singleItemSupport.get (key);
      String[] line = { seqWord, Integer.toString (key), Integer.toString (countPair.size ()) };
      toWriteList.add (line);
    }

    CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (debugDir + "/" + fileName),
      GroupByNewProcessor.ENCODING), ',', '"');

    writer.writeAll (toWriteList);
    writer.flush ();
    writer.close ();
    toWriteList.clear ();
  }

  /**
   * Given a fileName and doubleItemSupport List, will write to specified file name
   * 
   * @param fileName
   * @param doubleItemSupport
   * @throws IOException
   */
  private void writeCSVDebugIter2 (String fileName, HashBuckets<IntPair, IntStringPair> doubleItemSupport) throws IOException
  {

    String[] header = { "sequence", "wordID1 , wordID2", "count" };
    List<String[]> toWriteList = new ArrayList<String[]> ();
    toWriteList.add (header);
    for (IntPair key : doubleItemSupport.keySet ()) {
      String seqWord1 = mapping.wordForInt (key.x);
      String seqWord = seqWord1 + " " + mapping.wordForInt (key.y);
      Collection<IntStringPair> countPair = doubleItemSupport.get (key);
      String[] line = { seqWord, Integer.toString (key.x) + " : " + Integer.toString (key.y),
        Integer.toString (countPair.size ()) };
      toWriteList.add (line);
    }

    CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (debugDir + "/" + fileName),
      GroupByNewProcessor.ENCODING), ',', '"');

    writer.writeAll (toWriteList);
    writer.flush ();
    writer.close ();
    toWriteList.clear ();
  }

  /**
   * @param name
   * @param columns
   * @throws SQLException
   */
  private void createOrWipe (String name, String columns) throws SQLException
  {
    db.reCreateTable (name, columns);
  }

  /**
   * Returns sequence from database given a document, start, and end Sequence in integers as string WARNING: SLOW
   * because each sequence lookup is a database lookup WARNING: NOT FINAL SEQ Array - only returns orig string from AQL
   * 
   * @param doc
   * @param start
   * @param end
   * @return
   * @throws SQLException
   */
  final static String GET_SEQUENCE_QUERY = "SELECT token from token_map WHERE doc=%d AND pos>=%d AND pos<%d ORDER BY pos";

  public String getSequence (String doc, int start, int end) throws SQLException
  {
    String result = "";
    ResultSet rs = db.readFromDB (String.format (GET_SEQUENCE_QUERY, doc, start, end));
    while (rs.next ()) {
      if (result.length () > 0) result += "-";
      result += rs.getInt (1);
    }
    return result;
  }

  /**
   * Returns sequence from database given a document, start, and end wordToken converted into actual Seq String based
   * off word mapping WARNING: SLOW because each sequence lookup is a database lookup WARNING: NOT FINAL SEQ STRING -
   * only returns orig string from AQL
   * 
   * @param doc
   * @param start
   * @param end
   * @param mapping
   * @return
   * @throws SQLException
   */
  public String getSequenceString (int doc, int start, int end, WordIntegerMapping mapping) throws SQLException
  {
    String result = "";
    ResultSet rs = db.readFromDB (String.format (GET_SEQUENCE_QUERY, doc, start, end));
    while (rs.next ()) {
      if (result.length () > 0) result += "-";
      result += mapping.wordForInt (rs.getInt (1));
    }
    return result;
  }

  /**
   * Data store for sequence instance
   * 
   * 
   */
  private class SequenceInstance
  {
    public int start;
    public int end;
    public String doc;
    public int seqID;
    public boolean unsub;

    public SequenceInstance (int start, int end, String doc, int seqID, boolean unsub)
    {
      this.start = start;
      this.end = end;
      this.doc = doc;
      this.seqID = seqID;
      this.unsub = unsub;
    }

    public String toString ()
    {
      try {
        return getSequence (doc, start, end);
      }
      catch (SQLException e) {
        e.printStackTrace ();
        return "ERR";
      }
    }
  }

}
