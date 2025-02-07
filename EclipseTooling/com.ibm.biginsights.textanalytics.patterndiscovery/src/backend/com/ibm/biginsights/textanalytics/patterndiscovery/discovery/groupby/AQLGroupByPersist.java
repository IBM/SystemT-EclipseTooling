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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.avatar.algebra.datamodel.FieldSetter;
import com.ibm.avatar.algebra.datamodel.FieldType;
import com.ibm.avatar.algebra.datamodel.Text;
import com.ibm.avatar.algebra.datamodel.Tuple;
import com.ibm.avatar.algebra.datamodel.TupleList;
import com.ibm.avatar.algebra.datamodel.TupleSchema;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis.ComputeCorrelation;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.goldenStandard.JaccardObject;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.CharIgnoreHasher;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.DictIgnoreHasher;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.HashFactory;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.RuleBasedHasher;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.SimpleHasher;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.StemmingDictIgnoreHasher;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DiscoveryConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IntPair;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.pairs.HashBuckets;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;

/**
 * Runs AQL matching plus rule-base grouping. Where AQL output is persisted in a database. database table. Some code
 * taken from @see com.ibm.avatar.discovery.test.AQLDerbyTests
 */
public class AQLGroupByPersist
{

  @SuppressWarnings("unused")


  private static final IPDLog logger = PDLogger.getLogger ("AQLGroupByPersist");    //$NON-NLS-1$

  // field names in the tables
  private final static String ID = "id";
  private final static String DOCID = "docid";

  /** Directory where misc. regression test results for this class go. */
  public static final String MISC_OUTPUT_DIR = "groupBy/output";

  public final static String SCHEMA = "aomdata";

  /**
   * Documents are limited to 15000 characters in the current AOM implementation
   */
  public static final int MAX_DOCTEXT_LENGTH = 15000;

  /** Name of the database instance where we keep our AOM data. */
  public static final String DERBY_AOM_DBNAME = "aom";
  private final static String DERBY_DB_URL_PREFIX = "jdbc:derby:";

  /**
   * Name of the annotation type that is persisted by the various "persist phone" tests.
   */
  // public static final String PHONE_NUMBER_TYPE_NAME = "PhoneNumber";
  // public static final String COMPANY_TYPE_NAME = "Company";
  // public static final String COUNTRY_TYPE_NAME = "Company";
  /** Name of the annotation type created in testPersistSecondAnnot(). */
  public static final String PHONE_NUMBER_COPY_ANNOT_NAME = "PhoneNumberCopy";

  /** Name of the document type we create and use within the AOM store. */
  public static final String DOC_TYPE_NAME = "Email";

  /**
   * How many documents we expect to have in the DB. Can be anything <= the number in the Derby database.
   */
  // public int NDOCS = 5000000; // Sebastian: was 37k something. do we
  // actually
  // need this number?
  // public static final int NDOCS = 10000;
  private ExperimentProperties properties;

  /**
   * How often (in number of documents) to give progress feedback.
   */
  public static final int FEEDBACK_INTERVAL_DOCS = 1000;

  public AQLGroupByPersist (Properties properties)
  {
    this.properties = (ExperimentProperties) properties;
    // this.NDOCS =
    // Integer.parseInt(properties.getProperty("expectedDocs"));
  }

  /**
   * Runs AQL+Grouping based on a properties file passed as parameter.
   * 
   * @param args String array containing the name of the property file as the first entry.
   * @throws FileNotFoundException
   * @throws IOException
   * @throws SQLException
   */
  public static void main (String[] args) throws FileNotFoundException, IOException, SQLException
  {
    ExperimentProperties properties = new ExperimentProperties ();
    properties.load (new FileInputStream (args[0]));

    // Directory where SystemT outputs are stored into
    String rule = properties.getRootDir () + properties.getProperty (PropertyConstants.RULE_DIR) + File.separator
      + properties.getProperty (PropertyConstants.APPLIED_RULE_FILES);

    File ruleFileGiven = new File (rule);

    if (!ruleFileGiven.isDirectory ()) {
      runGroupBy (properties, rule);
    }
    else {
      for (File file : ruleFileGiven.listFiles ()) {
        if (file.getName ().endsWith ("-rule.csv")) {
          runGroupBy (properties, file.getAbsolutePath ());
        }
      }
    }
  }

  /**
   * Creates a HashFactory as specified in properties and then calls runGroupBy(properties, ruleFile,hasher);
   * 
   * @param properties
   * @param ruleFile
   * @throws IOException
   * @throws SQLException
   */
  public static void runGroupBy (Properties properties, String ruleFile) throws IOException, SQLException
  {
    // HashFactory hasher = makeHasher(properties, ruleFile);
    // runGroupBy(properties, ruleFile,hasher);
  }

  /**
   * Creates a HashFactory as specified in properties. The property values used are: hashFactory which may be
   * char-default (CharIgnoreHasher), simple (SimpleHasher), stem-default (StemmingDictIgnoreHasher) rules
   * (RuleBasedHasher). In the latter case, also the values for useInfrequentWords, sequenceDB and relevantSequences are
   * read.
   * 
   * @param properties
   * @param ruleFile
   * @param sequenceMap
   * @return
   * @throws IOException
   * @throws SQLException
   * @throws PatternDiscoveryException
   */
  public static HashFactory makeHasher (ExperimentProperties properties, String ruleFile,
    Map<Integer, String> sequenceMap) throws PatternDiscoveryException
  {
    HashFactory hasher = null;

    // Directory where sequence outputs are stored into
    String relevantSeqDir = properties.getRootDir () + properties.getProperty (PropertyConstants.RULE_DIR)
      + "preprocessing-relevant.csv";
    String hashFactory = properties.getProperty (PropertyConstants.HASH_FACTORY);

    try {
      if (hashFactory.equals ("char-default")) {
        hasher = new CharIgnoreHasher (new char[] { ' ', '\n', '\'', ',', '&', '!', '-', ')', '(', '|', '/', '@', '"' });
      }
      if (hashFactory.equals ("simple")) {
        hasher = new SimpleHasher ();
      }
      if (hashFactory.equals ("dict-default")) {

        BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (
          "groupBy/data/stopwords-many.txt"), GroupByNewProcessor.ENCODING));

        ArrayList<String> stopwords = new ArrayList<String> ();
        while (br.ready ()) {
          stopwords.add (br.readLine ());
        }
        String[] dict = stopwords.toArray (new String[stopwords.size ()]);
        hasher = new DictIgnoreHasher (dict, DictIgnoreHasher.STANDARD_TOKEN_SPLIT);
        br.close ();
      }
      if (hashFactory.equals ("stem-default")) {
        BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (
          "groupBy/data/stopwords-many.txt"), GroupByNewProcessor.ENCODING));

        ArrayList<String> stopwords = new ArrayList<String> ();
        while (br.ready ()) {
          stopwords.add (br.readLine ());
        }
        String[] dict = stopwords.toArray (new String[stopwords.size ()]);
        hasher = new StemmingDictIgnoreHasher (dict, DictIgnoreHasher.STANDARD_TOKEN_SPLIT);
        br.close ();
      }
      if (hashFactory.equals ("rules")) {
        boolean useInfrequentWords = !"false".equalsIgnoreCase (properties.getProperty (PropertyConstants.USE_INFREQUENT_WORDS));

        hasher = new RuleBasedHasher (properties.getProperty (PropertyConstants.DB_PREFIX)
          + ((ExperimentProperties) properties).getRootDir ()
          + properties.getProperty (PropertyConstants.SEQUENCE_DB_NAME), new FileInputStream (ruleFile),
          new FileInputStream (relevantSeqDir), useInfrequentWords, (ExperimentProperties) properties, sequenceMap);
      }
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GROUPING_HASHER_WRITE_ERR);
    }
    return hasher;
  }

  /**
   * Runs the AQL processing plus grouping for one rule file. The output is stored in a file called
   * $ruleFile-grouping.txt
   * 
   * @param properties
   * @param ruleFile
   * @param hasher
   * @throws PatternDiscoveryException
   */
  public static void runGroupBy (ExperimentProperties properties, String ruleFile, HashFactory hasher,
    Map<Integer, String> seqMap) throws PatternDiscoveryException
  {

    try {
      String dbURL = DERBY_DB_URL_PREFIX + DERBY_AOM_DBNAME;
      if (properties.getProperty (PropertyConstants.RESULTS_DB_NAME) != null)
        dbURL = properties.getProperty (PropertyConstants.DB_PREFIX)
          + ((ExperimentProperties) properties).getRootDir ()
          + properties.getProperty (PropertyConstants.RESULTS_DB_NAME);

      // System.out.println("Press return to continue.");
      // System.in.read();

      AQLGroupByPersist t = new AQLGroupByPersist (properties);

      long startMS = System.currentTimeMillis ();

      @SuppressWarnings("unused")
      TupleList grouped = t.groupBy (properties.getProperty (PropertyConstants.AQL_VIEW_NAME), dbURL, hasher, seqMap,
        DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME.split (","));

      long endMS = System.currentTimeMillis ();
      t.tearDown ();

      double elapsedSec = (double) (endMS - startMS) / 1000.0;

      logger.fine (String.format ("Grouping took %1.3f sec.\n", elapsedSec));

      String storeGroupingsFile = ruleFile + "-grouping.txt";
      String storeGroupingsFileCSV = ruleFile + "-grouping.csv";
      (new File (storeGroupingsFile)).delete ();
      (new File (storeGroupingsFileCSV)).delete ();
      DebugDBProcessor db = new DebugDBProcessor (dbURL);

      // Pass in information to store txt files and database
      db.setProperties ((ExperimentProperties) properties);

      try {
        // Add column dup_ID to later be able to include ID in join
        db.writeToDB ("ALTER TABLE aomdata.type_"
          + properties.getProperty (PropertyConstants.AQL_VIEW_NAME).replaceAll ("\\.", "__") + " ADD COLUMN dup_ID "
          + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE, null);
        // Populate dup_ID with values from ID
        db.writeToDB (
          "UPDATE aomdata.type_" + properties.getProperty (PropertyConstants.AQL_VIEW_NAME).replaceAll ("\\.", "__")
            + " SET dup_ID = ID", null);
      }
      catch (SQLException e) {
        // System.err.print(e);
        logger.fine ("Column dup_ID already exists");
      }
      // db.writeToDB("alter table aomdata.type_header add column sequence long varchar default 'text'",
      // null);
      // db.writeToDB("insert into aomdata.type_header (sequence) (select sequence from groupbytmp where groupbytmp.hash = hashtable.hash)","groupbytmp");
      String storeTemplate = "CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY('%s','%s',null,null,'%s')";
      String query = "SELECT (SELECT count(*) FROM groupbytmp WHERE groupbytmp.hash = hashtable.hash),hashtable.hash, "
        + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME.replaceAll ("\\.", "__") + " , dup_ID"
        + " ,sequence FROM aomdata.type_"
        + properties.getProperty (PropertyConstants.AQL_VIEW_NAME).replaceAll ("\\.", "__")
        + " datatable, groupbytmp hashtable WHERE datatable.docid = hashtable.docid AND "
        + "datatable.id = hashtable.id ORDER BY (SELECT count(*) FROM groupbytmp "
        + "WHERE groupbytmp.hash = hashtable.hash) DESC, hashtable.hash DESC";
      String sql = String.format (storeTemplate, query, storeGroupingsFile, GroupByNewProcessor.ENCODING);
      db.writeToDB (sql, "groupbytmp");

      // write to csv file
      String sqlCSV = String.format (storeTemplate, query, storeGroupingsFileCSV, GroupByNewProcessor.ENCODING);
      db.writeToDB (sqlCSV, "groupbytmp");

      db.importCSV ("APP", "finaloutput", "count int, hashID int, origstring "
        + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE + ", " + "ID " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE
        + ", sequence " + DiscoveryConstants.SEQUENCE_STRING_COLUMN_TYPE, "null", "null", new File (
        storeGroupingsFileCSV), true);
      // }

      db.shutdown ();

    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GROUPING_DB_ERR);
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GROUPING_WRITE_ERR);
    }
  }

  /**
   * Returns a List of Annotations which are grouped (rather sorted actually) by the values of the fields specified in
   * the parameters.
   * 
   * @throws IOException
   * @throws PatternDiscoveryException
   */
  public TupleList groupBy (String annotationType, String dbURL, HashFactory hasher, Map<Integer, String> seqMap,
    String... fields) throws PatternDiscoveryException
  {
    // connect to db
    logger.fine ("starting groupby");
    String tableName = "type_" + annotationType.replace (".", "__");

    DebugDBProcessor typeDB = null;

    TupleSchema schema = deduceSchema (tableName, dbURL, properties);
    
    //Create accessors for corresponding fields in the tuple schema
    ArrayList<FieldSetter<Object>> fieldSetters = new ArrayList<FieldSetter<Object>>();
    FieldType fType;
    String fName;
    for(int ix = 0; ix < schema.size(); ix++)
    {
    	fName = schema.getFieldNameByIx(ix);
    	fType = schema.getFieldTypeByIx(ix);
    	fieldSetters.add(schema.genericSetter(fName, fType));
    }
	
    TupleList tupleList = new TupleList (schema);
    try {
      String[] dbFields = new String[fields.length];
      for (int i = 0; i < fields.length; i++) {
        // String field = fields[i];
        dbFields[i] = fields[i].replaceAll ("\\.", "__");
      }

      typeDB = new DebugDBProcessor (dbURL);
      typeDB.setProperties (properties);
      // query for type

      String query = String.format ("select * from %s.%s", SCHEMA, tableName);

      logger.fine (query);
      // System.out.println(query);
      ResultSet results = typeDB.readFromDB (query);

      // create a temp table for the hashes and primary key on the
      // annotation table
      // drop table if it exists already
      if (typeDB.tableExsists ("groupbytmp")) {
        typeDB.writeToDB ("DROP TABLE groupbytmp", "groupbytmp");
      }
      // create indices for faster joining
      typeDB.writeToDB (String.format ("create index typetable_ids_" + annotationType.replace (".", "__")
        + " on %s.%s(docid,id)", SCHEMA, tableName), null);

      typeDB.writeToDB ("CREATE TABLE groupbytmp (hash int, docid " + DiscoveryConstants.DOCID_COLUMN_TYPE + ", "
        + "id " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE + ", sequence "
        + DiscoveryConstants.SEQUENCE_STRING_COLUMN_TYPE + ")", "groupbytmp");

      // drop the rules history table if it exists
      if (typeDB.tableExsists (DiscoveryConstants.RULESHISTORY_TBL_NAME)) {
        typeDB.writeToDB ("DROP TABLE " + DiscoveryConstants.RULESHISTORY_TBL_NAME,
          DiscoveryConstants.RULESHISTORY_TBL_NAME);
      }

      String sqlToCreateRulesHistoryTable = String.format ("CREATE TABLE %s (%s %s, %s %s, %s %s, %s %s)",
        DiscoveryConstants.RULESHISTORY_TBL_NAME, DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME,
        DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE, DiscoveryConstants.RULE_AS_STR_COL_NAME,
        DiscoveryConstants.RULE_AS_STR_COL_TYPE, DiscoveryConstants.SEQ_BEFORE_RULE_COL_NAME,
        DiscoveryConstants.SEQ_BEFORE_RULE_COL_TYPE, DiscoveryConstants.SEQ_AFTER_RULE_COL_NAME,
        DiscoveryConstants.SEQ_AFTER_RULE_COL_TYPE);

      typeDB.writeToDB (sqlToCreateRulesHistoryTable, DiscoveryConstants.RULESHISTORY_TBL_NAME);

      // for each entry
      logger.fine ("hashing");
      // at most maxIndexSize matches will be indexed
      int maxIndexSize = Integer.MAX_VALUE;

      /*
       * Currently not being used if (properties.getProperty("maxIndexSize") != null) { maxIndexSize =
       * Integer.parseInt(properties .getProperty("maxIndexSize")); }
       */
      logger.fine ("MaxIndexSize: " + maxIndexSize);
      File tempFile = new File (properties.getRootDir () + "temp" + File.separator + "debug" + File.separator
        + "groupbytmp.csv");

      // BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
      // new FileOutputStream(tempFile),
      // GroupByNewProcessor.ENCODING));

      CSVWriter bw = new CSVWriter (new OutputStreamWriter (new FileOutputStream (tempFile),
        GroupByNewProcessor.ENCODING), ',', '"');

      boolean removeUpper = (properties.getProperty (PropertyConstants.INPUT_TO_LOWERCASE).equalsIgnoreCase ("true"));
      boolean trim = (properties.getProperty (PropertyConstants.IGNORE_EXTRA_WHITESPACES).equalsIgnoreCase ("true"));

      String debugDir = properties.getRootDir () + properties.getProperty (PropertyConstants.DEBUG_DIR);

      File ruleHistoryFile = new File (debugDir + "/ruleHistory.csv");
      CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (ruleHistoryFile),
        GroupByNewProcessor.ENCODING), ',', '"');

      if (properties.getProperty (PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase ("true")) {
        String firstline[] = { "String", "RuleType", "Effect", "EffectSeq", "rule", "ruleStr", "before", "beforeSeq",
          "after", "afterSeq" };

        writer.writeNext (firstline);
      }

      RuleBasedHasher ruleHasher = (RuleBasedHasher) hasher;

      int count = 0;
      while (results.next ()) {
        int hashValue = 0;
        // compute hash (in an external class)
        String toHash = "";
        for (String fieldName : dbFields) {
          if (toHash.length () > 0) toHash += " " + GroupByNewProcessor.COL_SEPARATOR + " ";
          toHash += results.getString (fieldName);
        }

        if (removeUpper) toHash = toHash.toLowerCase ();
        if (trim) toHash = toHash.trim ();

        // Get sequence set from hasher
        Collection<Integer> sequenceSet = hasher.getSeqSet (toHash, properties, writer);

        ArrayList<Integer> sortedSequenceSet = new ArrayList<Integer> ();
        sortedSequenceSet.addAll (sequenceSet);
        Collections.sort (sortedSequenceSet);

        hashValue += hasher.hashScore (sequenceSet);

        String context = results.getString (DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME);        
        String sequenceID = ruleHasher.sequence2String (sortedSequenceSet, seqMap, context);

        // store hash
        String docid = results.getString (DOCID);
        String id = results.getString (ID);
        // String sql = "insert into groupbytmp values (" + hashValue
        // + "," + docid + "," + id + ")";
        // // System.out.println(sql);
        // typeDB.writeToDB(sql);
        String[] output = { Integer.toString (hashValue), docid, id, sequenceID };

        bw.writeNext (output);
        // bw.write(String.format("%d,%s,%s,%s\n", hashValue, docid, id,
        // sequenceID));
        count++;
        if (count >= maxIndexSize) break;
      }
      // Write to file after getSeqSet fills file
      // if (properties.getProperty (PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase ("true")) {
      writer.flush ();
      writer.close ();
      // }
      bw.flush ();
      bw.close ();

      // we want to add the droped rules to the db so we can provide this analysis later to the user
      String sqlToWriteRulesHistoryTable = String.format (
        "call SYSCS_UTIL.SYSCS_IMPORT_TABLE ('%s','%s','%s',null,null,'%s', 0)", "APP",
        DiscoveryConstants.RULESHISTORY_TBL_NAME, ruleHistoryFile.getAbsolutePath (), GroupByNewProcessor.ENCODING);
      typeDB.writeToDB (sqlToWriteRulesHistoryTable, DiscoveryConstants.RULESHISTORY_TBL_NAME);

      // FIXME delete this test line
      // typeDB.printQueryResult("select * from " + DiscoveryConstants.RULESHISTORY_TBL_NAME);

      logger.info ("writing output");
      // import
      typeDB.writeToDB (String.format ("call SYSCS_UTIL.SYSCS_IMPORT_TABLE ('%s','%s','%s',null,null,'%s', 0)", "APP",
        "GROUPBYTMP", tempFile.getAbsolutePath (), GroupByNewProcessor.ENCODING), "GROUPBYTMP");
      // tempFile.delete();
      logger.fine ("IndexSize: " + count);
      results.close ();
      logger.fine ("writing index on hash table");
      typeDB.writeToDB ("CREATE INDEX HASH_INDEX ON groupbytmp (hash DESC) ", null);
      typeDB.writeToDB ("CREATE INDEX groupbytmp_ids ON groupbytmp (docid,id DESC) ", null);

      // logger.fine("done hashing, writing histogram");
      // computeGroupHistogram(typeDB, tableName);
      // logger.fine("done computing histogram");
      // group on hash values
      // directly join here (is this performant?)
      // TODO: sort biggest groups first (add count(hash) to order if
      // possible)
      String sql = "SELECT * FROM " + SCHEMA + "." + tableName + " datatable," + " groupbytmp hashtable " + "WHERE "
        + "datatable.docid = hashtable.docid AND " + "datatable.id = hashtable.id " +
        // "ORDER BY (SELECT count(*) FROM groupbytmp WHERE
        // groupbytmp.hash = hashtable.hash) DESC, hashtable.hash
        // DESC " +
        "ORDER BY hashtable.hash DESC " + "";
      // is this one faster? (NO: it takes ~8s as opposed to ~7s in the
      // above)
      // String sql = "SELECT * FROM "+SCHEMA+"."+tableName+" AS datatable
      // JOIN" +
      // " groupbytmp AS hashtable " +
      // "ON datatable.docid = hashtable.docid AND datatable.id =
      // hashtable.id " +
      // // "WHERE datatable.RC__VALUE like '%Per Diem%' " +
      // "ORDER BY hashtable.hash DESC " +
      // "";

      logger.fine (sql);
      ResultSet hashes = typeDB.readFromDB (sql);
      // create and output annotation objects accordingly
      int resultCount = 0;
      while (hashes.next ()) {
        tupleList.add (result2Tuple (hashes, schema, fieldSetters, ""));
        resultCount++;
      }
      logger.info ("Number of results found: " + resultCount);
      hashes.close ();
      // typeDB.writeToDB("DROP TABLE groupbytmp");
    }
    catch (SQLException e) {

      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GROUPING_DB_ERR);
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GROUPING_WRITE_ERR);
    }
    finally {
      if (typeDB != null) typeDB.shutdown ();
    }

    return tupleList;
  }

  /**
   * Joins raw AQL output with all entities and text surrounding the groupings with the final grouping file
   * 
   * @param db
   * @param properties
   * @throws SQLException
   * @throws IOException
   */
  public static void joinAQLGrouping (DebugDBProcessor db, ExperimentProperties properties) throws SQLException, IOException
  {
    logger.info ("Join Raw AQL with Grouping File");

    // PostProcessDir
    String debugDir = properties.getRootDir () + properties.getProperty (PropertyConstants.DEBUG_DIR);

    String outputFile = debugDir + "AllGroupings.csv";

    // Store entities in an ArrayList - only if there exists entities
    String relationshipFields = properties.getProperty (PropertyConstants.ENTITY_FIELD_NAMES);
    ArrayList<String> entities = new ArrayList<String> ();
    if (!relationshipFields.isEmpty ()) {
      // Split the input based on ","
      String[] viewAndRelField = relationshipFields.split ("\\s*,\\s*");
      for (String t : viewAndRelField) {
        // Split the view name from the field name
        String[] temp = t.split (PDConstants.VIEW_SPAN_SEPARATOR_REGEX);
        if (temp[0].equalsIgnoreCase (properties.getProperty (PropertyConstants.AQL_VIEW_NAME))) {
          entities.add (temp[1]); // store the field name specific to
          // current view
        }
      }
    }

    String snippetFieldName = properties.getProperty (PropertyConstants.SNIPPET_FIELD_NAME);

    // Create index for final group and raw results
    db.writeToDB ("CREATE INDEX group_index ON finaloutput (id) ", null);
    db.writeToDB ("CREATE INDEX allRawResults_index ON allRawResults (uniqueID) ", null);

    String query = "SELECT finaloutput.*";

    // Get entity names
    Iterator<String> fieldItr = entities.iterator ();
    while (fieldItr.hasNext ()) {
      query = query + ", allRawResults." + DiscoveryConstants.ENTITY_COLUMN_PREFIX + fieldItr.next ();
    }
    if (!snippetFieldName.isEmpty ()) {
      query = query + ", allRawResults." + snippetFieldName;
    }
    query = query + " from finaloutput LEFT JOIN allRawResults ON (finaloutput.id=allRawResults.uniqueID)"
      + " order by finaloutput.count DESC";

    db.exportQuery (query, outputFile);

    String goldQuery = "count INT, hashID INT, " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME + " "
      + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE + ", " + "uniqueID " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE
      + ", sequence " + DiscoveryConstants.SEQUENCE_STRING_COLUMN_TYPE;

    fieldItr = entities.iterator ();
    while (fieldItr.hasNext ()) {
      goldQuery = goldQuery + ", " + DiscoveryConstants.ENTITY_COLUMN_PREFIX + fieldItr.next () + " "
        + DiscoveryConstants.ENTITY_COLUMN_TYPE;
    }
    if (!snippetFieldName.isEmpty ()) {
      goldQuery = goldQuery + ", " + snippetFieldName + " " + DiscoveryConstants.SNIPPET_COLUMN_TYPE;
    }

    // Make sure goldenStandard is imported into the db
    db.importCSV ("APP", "AllGroupings", goldQuery, "null", "", new File (outputFile), true);
  }

  /**
   * Given a grouping file with all the additional fields, converts it into a grouping file with Jaccard Distances
   * 
   * @param db
   * @param properties
   * @throws SQLException
   * @throws IOException
   */

  public static void applyJaccardGrouping (DebugDBProcessor db, ExperimentProperties properties) throws SQLException, IOException
  {

    logger.info ("Apply Jaccard Distance Grouping");

    // initialize directory structure
    String baseDir = properties.getRootDir ();

    // PostProcessDir
    String debugDir = baseDir + properties.getProperty (PropertyConstants.DEBUG_DIR);

    String outputFile = baseDir + "GroupingJaccard.csv";
    String outputScoreFile = debugDir + "JaccardScores.csv";

    int groupingLow = Integer.parseInt (properties.getProperty (PropertyConstants.GROUPING_LOW));
    int groupingHigh = Integer.parseInt (properties.getProperty (PropertyConstants.GROUPING_HIGH));
    double jaccardScore = Double.parseDouble (properties.getProperty (PropertyConstants.JACCARD_SCORE));

    String sql = "SELECT * FROM " + "AllGroupings " + "ORDER BY count DESC";
    logger.fine (sql);
    ResultSet outputGroup = db.readFromDB (sql);

    ArrayList<JaccardObject> storeList = new ArrayList<JaccardObject> ();

    // Store entities in an ArrayList - only if there exists entities
    String relationshipFields = properties.getProperty (PropertyConstants.ENTITY_FIELD_NAMES);
    ArrayList<String> entities = new ArrayList<String> ();
    if (!relationshipFields.isEmpty ()) {
      // Split the input based on ","
      String[] viewAndRelField = relationshipFields.split ("\\s*,\\s*");
      for (String t : viewAndRelField) {
        // Split the view name from the field name
        String[] temp = t.split (PDConstants.VIEW_SPAN_SEPARATOR_REGEX);
        if (temp[0].equalsIgnoreCase (properties.getProperty (PropertyConstants.AQL_VIEW_NAME))) {
          entities.add (temp[1]); // store the field name specific to
          // current view
        }
      }
    }

    String snippetFieldName = properties.getProperty (PropertyConstants.SNIPPET_FIELD_NAME);

    // Store grouping into an ArrayList
    while (outputGroup.next ()) {

      int sigCount = outputGroup.getInt ("count");
      int hashID = outputGroup.getInt ("hashid");
      String context = outputGroup.getString (DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME);
      String uniqueID = outputGroup.getString ("uniqueID");
      String signature = outputGroup.getString ("sequence");
      String snippet = "";
      if (!snippetFieldName.isEmpty ()) {
        snippet = outputGroup.getString (snippetFieldName);
      }

      ArrayList<String> pulledFields = new ArrayList<String> ();
      Iterator<String> fieldItr = entities.iterator ();
      while (fieldItr.hasNext ()) {
        pulledFields.add (outputGroup.getString (DiscoveryConstants.ENTITY_COLUMN_PREFIX + fieldItr.next ()));
      }
      String[] fieldResults = new String[pulledFields.size ()];
      pulledFields.toArray (fieldResults);

      JaccardObject temp = new JaccardObject (sigCount, hashID, context, uniqueID, signature, fieldResults, "NO", // currently
        // remove
        // gold
        // standard
        snippet);
      storeList.add (temp);
    }

    // Store all groups greater than 9
    // TODO: Make the group size variable depending on properties file
    String largeSQL = "SELECT distinct (sequence), hashid FROM " + "AllGroupings " + "WHERE count > " + groupingHigh
      + " group by sequence, hashid";
    logger.fine (largeSQL);
    ResultSet largeGroups = db.readFromDB (largeSQL);

    ArrayList<JaccardObject> largeList = new ArrayList<JaccardObject> ();
    while (largeGroups.next ()) {
      JaccardObject temp = new JaccardObject ();
      temp.setID (largeGroups.getInt (2));
      temp.setSignature (largeGroups.getString (1));
      largeList.add (temp);
    }

    // Store all groups less than 3
    String smallSQL = "SELECT * FROM " + "AllGroupings " + "WHERE count < " + groupingLow + " ORDER BY count DESC";
    logger.fine (smallSQL);
    ResultSet smallGroups = db.readFromDB (smallSQL);

    ArrayList<JaccardObject> smallList = new ArrayList<JaccardObject> ();

    while (smallGroups.next ()) {

      int sigCount = smallGroups.getInt ("count");
      int hashID = smallGroups.getInt ("hashid");
      String context = smallGroups.getString (DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME);
      String uniqueID = smallGroups.getString ("uniqueID");
      String signature = smallGroups.getString ("sequence");
      String snippet = "";
      if (!snippetFieldName.isEmpty ()) {
        snippet = smallGroups.getString (snippetFieldName);
      }

      ArrayList<String> pulledFields = new ArrayList<String> ();
      Iterator<String> fieldItr = entities.iterator ();
      while (fieldItr.hasNext ()) {
        pulledFields.add (smallGroups.getString (DiscoveryConstants.ENTITY_COLUMN_PREFIX + fieldItr.next ()));
      }
      String[] fieldResults = new String[pulledFields.size ()];
      pulledFields.toArray (fieldResults);

      JaccardObject temp = new JaccardObject (sigCount, hashID, context, uniqueID, signature, fieldResults, "NO",
        snippet);
      smallList.add (temp);
    }

    HashBuckets<Integer, JaccardObject> storeNew = new HashBuckets<Integer, JaccardObject> ();

    // Store jaccard distances to output later
    HashMap<IntPair, Double> storeAllScores = new HashMap<IntPair, Double> ();
    HashMap<Integer, Double> storeScores = new HashMap<Integer, Double> ();

    // Compare each small group to large group signature
    Object[] largeArray = largeList.toArray ();
    Iterator<JaccardObject> smallIter = smallList.iterator ();
    while (smallIter.hasNext ()) {
      JaccardObject cur = smallIter.next ();
      double bestScore = jaccardScore; // smaller distance is closest
      int newID = cur.getID (); // default start with orig hashID

      for (Object group : largeArray) {
        // jaccard distance used here, can be changed at anytime
        // TODO: make this a property setting
        double curScore = ComputeCorrelation.jaccard (cur.getSignature (), ((JaccardObject) group).getSignature ());
        IntPair hashes = new IntPair (cur.getID (), ((JaccardObject) group).getID ());
        storeAllScores.put (hashes, curScore);
        if (curScore < bestScore) {
          bestScore = curScore;
          newID = ((JaccardObject) group).getID ();
        }
      }
      if (bestScore != 1.0) {
        storeNew.put (newID, cur); // only store if a match was found
      }
      storeScores.put (cur.getID (), bestScore);
    }

    // Prepare to write to file
    List<String[]> toWriteList = new ArrayList<String[]> ();

    // Grab first object HashID
    int curHashID = 0;
    String curSig = "";
    if (!storeList.isEmpty ()) {
      JaccardObject firstOb = (JaccardObject) storeList.toArray ()[0];
      curHashID = firstOb.getID ();
      curSig = firstOb.getSignature ();
    }

    ArrayList<Integer> hashIDs = new ArrayList<Integer> ();

    for (Object ob : storeList.toArray ()) {
      // write old and existing group data
      if (curHashID == ((JaccardObject) ob).getID () && !hashIDs.contains (((JaccardObject) ob).getID ())) {

        ArrayList<String> lineArr = new ArrayList<String> ();
        lineArr.add (Integer.toString (((JaccardObject) ob).getCount ()));
        lineArr.add (Integer.toString (((JaccardObject) ob).getID ()));
        lineArr.add (((JaccardObject) ob).getOriginalExtract ());
        lineArr.add (((JaccardObject) ob).getUniqueID ());
        lineArr.add (((JaccardObject) ob).getSignature ());
        lineArr.add (((JaccardObject) ob).getSignature ());

        // Allows for dynamically changing fields
        String[] storedFields = ((JaccardObject) ob).getEntities ();
        for (String field : storedFields) {
          lineArr.add (field);
        }
        if (!snippetFieldName.isEmpty ()) {
          lineArr.add (((JaccardObject) ob).getSnippet ());
        }

        String[] line = new String[lineArr.size ()];
        lineArr.toArray (line);
        toWriteList.add (line);
        curSig = ((JaccardObject) ob).getSignature ();
      }
      else {
        if (storeNew.containsKey (curHashID)) { // check to see if hash
          // exists in newStore
          for (Object match : storeNew.get (curHashID).toArray ()) {

            ArrayList<String> lineArr = new ArrayList<String> ();
            lineArr.add (Integer.toString (((JaccardObject) match).getCount ()));
            lineArr.add (Integer.toString (((JaccardObject) match).getID ()));
            lineArr.add (((JaccardObject) match).getOriginalExtract ());
            lineArr.add (((JaccardObject) match).getUniqueID ());
            lineArr.add (((JaccardObject) match).getSignature ());
            lineArr.add (curSig);

            // Allows for dynamically changing fields
            String[] storedFields = ((JaccardObject) match).getEntities ();
            for (String field : storedFields) {
              lineArr.add (field);
            }
            if (!snippetFieldName.isEmpty ()) {
              lineArr.add (((JaccardObject) match).getSnippet ());
            }

            String[] line = new String[lineArr.size ()];
            lineArr.toArray (line);
            toWriteList.add (line);
            // if hashID isn't in "used" list, add to the list of
            // IDs grouped
            if (!hashIDs.contains (((JaccardObject) match).getID ())) hashIDs.add (((JaccardObject) match).getID ());
          }
        }
        if (!hashIDs.contains (((JaccardObject) ob).getID ())) {
          // Store the current object after the "new" set has been
          // written

          ArrayList<String> lineArr = new ArrayList<String> ();
          lineArr.add (Integer.toString (((JaccardObject) ob).getCount ()));
          lineArr.add (Integer.toString (((JaccardObject) ob).getID ()));
          lineArr.add (((JaccardObject) ob).getOriginalExtract ());
          lineArr.add (((JaccardObject) ob).getUniqueID ());
          lineArr.add (((JaccardObject) ob).getSignature ());
          lineArr.add (((JaccardObject) ob).getSignature ());

          // Allows for dynamically changing fields
          String[] storedFields = ((JaccardObject) ob).getEntities ();
          for (String field : storedFields) {
            lineArr.add (field);
          }
          if (!snippetFieldName.isEmpty ()) {
            lineArr.add (((JaccardObject) ob).getSnippet ());
          }

          String[] line = new String[lineArr.size ()];
          lineArr.toArray (line);
          toWriteList.add (line);

        }
      }
      curHashID = ((JaccardObject) ob).getID ();
      curSig = ((JaccardObject) ob).getSignature ();
    }

    // Writing all Jaccard scores to separate file.
    CSVWriter writerScore = new CSVWriter (new OutputStreamWriter (new FileOutputStream (outputScoreFile),
      GroupByNewProcessor.ENCODING), ',', '"');

    for (IntPair pair : storeAllScores.keySet ()) {
      String[] line = { Integer.toString (pair.x), Integer.toString (pair.y),
        Double.toString (storeAllScores.get (pair)) };
      writerScore.writeNext (line);
    }
    writerScore.flush ();
    writerScore.close ();

    // Writing results of grouping
    CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (outputFile),
      GroupByNewProcessor.ENCODING), ',', '"');

    writer.writeAll (toWriteList);
    if (Constants.DEBUG) System.out.println ("Finished Writing File: " + outputFile);
    writer.flush ();
    writer.close ();

    String query = "count int, hashID int, " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME + " "
      + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE + ", " + "id " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE
      + ", sequence " + DiscoveryConstants.SEQUENCE_STRING_COLUMN_TYPE + ", jsequence "
      + DiscoveryConstants.COMMON_SEQUENCE_STRING_COLUMN_TYPE;

    // For all the fields specified in properties - load into database
    Iterator<String> fieldItr = entities.iterator ();
    while (fieldItr.hasNext ()) {
      query = query + ", " + DiscoveryConstants.ENTITY_COLUMN_PREFIX + fieldItr.next () + " "
        + DiscoveryConstants.ENTITY_COLUMN_TYPE;
    }
    if (!snippetFieldName.isEmpty ()) {
      query = query + ", " + snippetFieldName + " " + DiscoveryConstants.SNIPPET_COLUMN_TYPE;
    }

    String tableName = "GroupingJaccard_" + properties.getProperty (PropertyConstants.AQL_VIEW_NAME);
    tableName = tableName.replace (".", "__");
    db.importCSV ("APP", tableName, query, "null",
      "", new File (outputFile), true);
  }

  public final static int DB_COUNT = 1;
  public final static int DB_HASHID = 2;
  public final static int DB_UNIQUECOUNT = 3;
  public final static int DB_CONTEXT = 4;
  public final static int DB_ID = 5;
  public final static int DB_SIG = 6;

  /**
   * Will generate grouping based on distance on a easy to read (unique context grouped)
   * 
   * @param properties
   * @param seqMap
   * @throws IOException
   * @throws SQLException
   */
  public static void sequenceGroup (ExperimentProperties properties, Map<Integer, String> seqMap) throws IOException, SQLException
  {

    String dbURL = DERBY_DB_URL_PREFIX + DERBY_AOM_DBNAME;
    if (properties.getProperty (PropertyConstants.RESULTS_DB_NAME) != null)
      dbURL = properties.getProperty (PropertyConstants.DB_PREFIX) + properties.getRootDir ()
        + properties.getProperty (PropertyConstants.RESULTS_DB_NAME);

    // Output grouping files with proper counts
    outputFiles (properties, dbURL);

    DebugDBProcessor db = new DebugDBProcessor (dbURL);
    db.setProperties (properties);

    // Required for final grouping finals
    joinAQLGrouping (db, properties);
    applyJaccardGrouping (db, properties);

    String sql = "SELECT * FROM " + "finalgroup " + "ORDER BY count DESC";
    logger.fine (sql);
    ResultSet outputGroup = db.readFromDB (sql);

    ArrayList<OutputObject> storeList = new ArrayList<OutputObject> ();

    // Store grouping into an ArrayList
    while (outputGroup.next ()) {
      OutputObject temp = new OutputObject (outputGroup.getInt (DB_COUNT), outputGroup.getInt (DB_UNIQUECOUNT),
        outputGroup.getInt (DB_HASHID), outputGroup.getString (DB_CONTEXT), outputGroup.getString (DB_SIG),
        outputGroup.getString (DB_ID));
      storeList.add (temp);
    }

    int GROUPING_LOW = Integer.parseInt (properties.getProperty (PropertyConstants.GROUPING_LOW));
    int GROUPING_HIGH = Integer.parseInt (properties.getProperty (PropertyConstants.GROUPING_HIGH));

    // Store all groups greater than 9
    // TODO: Make the group size variable depending on properties file
    String largeSQL = "SELECT distinct (sequence), hashid FROM " + "finalgroup " + "WHERE count > " + GROUPING_HIGH
      + " group by sequence, hashid";
    logger.fine (largeSQL);
    ResultSet largeGroups = db.readFromDB (largeSQL);

    ArrayList<OutputObject> largeList = new ArrayList<OutputObject> ();
    while (largeGroups.next ()) {
      OutputObject temp = new OutputObject ();
      temp.setID (largeGroups.getInt (2));
      temp.setSignature (largeGroups.getString (1));
      largeList.add (temp);
    }

    // Store all groups less than 3
    String smallSQL = "SELECT * FROM " + "finalgroup " + "WHERE count < " + GROUPING_LOW + " ORDER BY count DESC";
    logger.fine (smallSQL);
    ResultSet smallGroups = db.readFromDB (smallSQL);

    ArrayList<OutputObject> smallList = new ArrayList<OutputObject> ();

    while (smallGroups.next ()) {
      OutputObject temp = new OutputObject (smallGroups.getInt (DB_COUNT), smallGroups.getInt (DB_UNIQUECOUNT),
        smallGroups.getInt (DB_HASHID), smallGroups.getString (DB_CONTEXT), smallGroups.getString (DB_SIG),
        smallGroups.getString (DB_ID));
      smallList.add (temp);
    }

    HashBuckets<Integer, OutputObject> storeNew = new HashBuckets<Integer, OutputObject> ();
    // Store jaccard distances to output later
    HashMap<Integer, Double> storeScores = new HashMap<Integer, Double> ();

    // Compare each small group to large group signature
    Object[] largeArray = largeList.toArray ();
    Iterator<OutputObject> smallIter = smallList.iterator ();
    while (smallIter.hasNext ()) {
      OutputObject cur = smallIter.next ();
      double requestedBestScore = Double.parseDouble (properties.getProperty (PropertyConstants.JACCARD_SCORE));
      double bestScore = requestedBestScore; // smaller distance is closest
      int newID = cur.getID (); // default start with orig hashID

      for (Object group : largeArray) {
        // jaccard distance used here, can be changed at anytime
        // TODO: make this a property setting
        double curScore = ComputeCorrelation.jaccard (cur.getSignature (), ((OutputObject) group).getSignature ());
        if (curScore < bestScore) {
          bestScore = curScore;
          newID = ((OutputObject) group).getID ();
        }
      }
      if (bestScore != requestedBestScore) {
        storeNew.put (newID, cur); // only store if a match was found
      }
      storeScores.put (cur.getID (), bestScore);
    }

    // Prepare to write to file
    String baseDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR);
    String inputFile = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
    if (inputFile.contains (".")) inputFile = inputFile.substring (0, inputFile.indexOf ('.'));

    String OUTPUT_DIR = baseDir + File.separator + inputFile;
    List<String[]> toWriteList = new ArrayList<String[]> ();

    // Grab first object HashID
    int curHashID = 0;
    String curSig = "";
    if (!storeList.isEmpty ()) {
      OutputObject firstOb = (OutputObject) storeList.toArray ()[0];
      curHashID = firstOb.getID ();
      curSig = firstOb.getSignature ();
    }

    ArrayList<Integer> hashIDs = new ArrayList<Integer> ();

    for (Object ob : storeList.toArray ()) {
      // write old and existing group data
      if (curHashID == ((OutputObject) ob).getID () && !hashIDs.contains (((OutputObject) ob).getID ())) {
        String[] line = { Integer.toString (((OutputObject) ob).getCount ()),
          Integer.toString (((OutputObject) ob).getID ()), Integer.toString (((OutputObject) ob).getNewCount ()),
          ((OutputObject) ob).getOriginalExtract (), ((OutputObject) ob).getUniqueID (),
          ((OutputObject) ob).getSignature (), ((OutputObject) ob).getSignature (), "0.0" };
        toWriteList.add (line);
        curSig = ((OutputObject) ob).getSignature ();
      }
      else {
        if (storeNew.containsKey (curHashID)) { // check to see if hash
          // exists in newStore
          for (Object match : storeNew.get (curHashID).toArray ()) {
            String[] line = { Integer.toString (((OutputObject) match).getCount ()),
              Integer.toString (((OutputObject) match).getID ()),
              Integer.toString (((OutputObject) match).getNewCount ()), ((OutputObject) match).getOriginalExtract (),
              ((OutputObject) match).getUniqueID (), ((OutputObject) match).getSignature (), curSig,
              Double.toString (storeScores.get (((OutputObject) match).getID ())) };
            toWriteList.add (line);
            // if hashID isn't in "used" list, add to the list of
            // IDs grouped
            if (!hashIDs.contains (((OutputObject) match).getID ())) hashIDs.add (((OutputObject) match).getID ());
          }
        }
        if (!hashIDs.contains (((OutputObject) ob).getID ())) {
          // Store the current object after the "new" set has been
          // written
          String[] line = { Integer.toString (((OutputObject) ob).getCount ()),
            Integer.toString (((OutputObject) ob).getID ()), Integer.toString (((OutputObject) ob).getNewCount ()),
            ((OutputObject) ob).getOriginalExtract (), ((OutputObject) ob).getUniqueID (),
            ((OutputObject) ob).getSignature (), ((OutputObject) ob).getSignature (), "0.0" };
          toWriteList.add (line);

        }
      }
      curHashID = ((OutputObject) ob).getID ();
      curSig = ((OutputObject) ob).getSignature ();
    }

    // Collections.sort(toWriteList);
    CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (OUTPUT_DIR + "/groupDistance.csv"),
      GroupByNewProcessor.ENCODING), ',', '"');

    writer.writeAll (toWriteList);
    if (Constants.DEBUG) System.out.println ("Finished Writing File");
    writer.flush ();
    writer.close ();
    db.shutdown ();
  }

  /**
   * Takes grouping file - and removes duplicates (easier to read)
   * 
   * @param properties
   * @param dbURL
   * @throws SQLException
   * @throws IOException
   */
  private static void outputFiles (ExperimentProperties properties, String dbURL) throws SQLException, IOException
  {

    if (properties.getProperty (PropertyConstants.RESULTS_DB_NAME) != null)
      dbURL = properties.getProperty (PropertyConstants.DB_PREFIX) + properties.getRootDir ()
        + properties.getProperty (PropertyConstants.RESULTS_DB_NAME);

    if (Constants.DEBUG) System.err.println (dbURL);
    DebugDBProcessor db = new DebugDBProcessor (dbURL);
    db.setProperties (properties); // Pass in information to store txt files
    // and database
    String sql = "SELECT * FROM " + "finaloutput " + "ORDER BY count DESC";

    logger.fine (sql);
    ResultSet outputAll = db.readFromDB (sql);

    // initialize directory structure

    String OUTPUT_DIR = properties.getRootDir ();

    // HashMap to store IDs with another hashmap of unique extracts
    HashMap<Integer, HashMap<String, OutputObject>> storeGrouping = new HashMap<Integer, HashMap<String, OutputObject>> ();

    // HashMap to store IDs with another hashmap of unique ID count
    HashMap<Integer, Integer> storeGroupingIDs = new HashMap<Integer, Integer> ();

    int IDCount;

    while (outputAll.next ()) {
      // Grab ID and Signature from the line
      int ID = outputAll.getInt (2);
      String extract = outputAll.getString (3);
      String uniqueID = outputAll.getString (4);

      int newCount = 1;

      // Grab HashMap for the extract
      HashMap<String, OutputObject> uniqueExtract = storeGrouping.get (ID);

      // Create HashMap if doesn't exist
      if (uniqueExtract == null) {// first instance of that ID
        uniqueExtract = new HashMap<String, OutputObject> ();
        IDCount = 0;
      }
      else {
        IDCount = storeGroupingIDs.get (ID);
      }

      // Grab OutputObject for the given Extract
      OutputObject store = uniqueExtract.get (extract);
      if (store == null) { // if outputObject doesn't exist (first
        // instance of extract)
        String sig = outputAll.getString (5);
        int count = outputAll.getInt (1);
        OutputObject ob = new OutputObject (count, newCount, ID, extract, sig, uniqueID);
        uniqueExtract.put (extract, ob);
      }
      else {
        int appears = store.getNewCount ();
        appears++; // increase the times that extract has been seen
        store.setNewCount (appears);
        uniqueExtract.put (extract, store);
      }
      IDCount++;

      storeGrouping.put (ID, uniqueExtract);
      storeGroupingIDs.put (ID, IDCount);

    }

    Set<Integer> IDs = storeGrouping.keySet ();
    Set<Integer> IDCountKey = storeGroupingIDs.keySet ();
    // System.out.println(IDs.size());
    List<String[]> toWriteList = new ArrayList<String[]> ();
    List<String[]> toWriteListCount = new ArrayList<String[]> ();

    for (int idKey : IDs) {
      // System.out.println(idKey);
      HashMap<String, OutputObject> uniqueString = storeGrouping.get (idKey);
      Set<String> extracts = uniqueString.keySet ();
      for (String extract : extracts) {
        // System.out.println(extract);
        OutputObject ob = uniqueString.get (extract);
        String[] line = ob.printLine ();
        toWriteList.add (line);
      }
    }

    // Go through the Count Hash Table
    for (int idcountK : IDCountKey) {

      String[] lineCount = { Integer.toString (idcountK), Integer.toString (storeGroupingIDs.get (idcountK)) };
      toWriteListCount.add (lineCount);
    }

    // Collections.sort(toWriteList);
    CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (OUTPUT_DIR + "/grouping.csv"),
      GroupByNewProcessor.ENCODING), ',', '"');

    writer.writeAll (toWriteList);
    if (Constants.DEBUG) System.out.println ("Finished Writing File");
    writer.flush ();
    writer.close ();

    // Store into DB
    db.importCSV ("APP", "finalGroup", "count int, hashID int, " + "newcount int, origstring "
      + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE + ", uniqueID " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE
      + ", " + "sequence " + DiscoveryConstants.SEQUENCE_STRING_COLUMN_TYPE, "null", "null", new File (OUTPUT_DIR
      + "/grouping.csv"), true);

    // Writing to Count file
    CSVWriter writerCount = new CSVWriter (new OutputStreamWriter (new FileOutputStream (OUTPUT_DIR
      + "/groupingCount.csv"), GroupByNewProcessor.ENCODING), ',', '"');

    writerCount.writeAll (toWriteListCount);
    if (Constants.DEBUG) System.out.println ("Finished Writing Count File");
    writerCount.flush ();
    writerCount.close ();
    db.shutdown ();
  }

  /**
   * Takes as input the table with the hash scores and outputs a histogram on the group sizes after clustering.
   * 
   * @param typeDB
   * @param tableName
   * @throws SQLException
   */
  @SuppressWarnings("unused")
  private void computeGroupHistogram (DebugDBProcessor typeDB, String tableName) throws SQLException
  {
    try {
      BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream ("groupBy/logs/"
        + properties.getProperty (PropertyConstants.AQL_VIEW_NAME) + "rules" + ".histogram.txt"),
        GroupByNewProcessor.ENCODING));

      String sql = "SELECT count(*) AS n, hash FROM groupbytmp GROUP BY hash ORDER BY n DESC";
      ResultSet counts = typeDB.readFromDB (sql);
      int ones = 0;
      int groups = 0;
      while (counts.next ()) {
        int count = counts.getInt (1);
        int hash = counts.getInt (2);
        if (count > 1) {
          groups++;
          bw.write (count + "");
          ResultSet context = typeDB.readFromDB ("SELECT * FROM " + SCHEMA + "." + tableName + " datatable,"
            + " groupbytmp hashtable "
            + "WHERE datatable.docid = hashtable.docid AND datatable.id = hashtable.id AND hashtable.hash=" + hash);
          context.next ();
          bw.write (", " + context.getString ("LC__VALUE") + "||" + context.getString ("RC__VALUE"));
          bw.write ("\n");

        }
        else
          ones++;
      }
      bw.write ("Number of groups:" + groups);
      bw.write ("Number of singletons:" + ones);
      bw.close ();
    }
    catch (IOException e) {
      e.printStackTrace ();
    }

  }

  /**
   * @param tableName
   * @param dbURL
   * @param properties
   * @return
   * @throws PatternDiscoveryException
   */
  private static TupleSchema deduceSchema (String tableName, String dbURL, ExperimentProperties properties) throws PatternDiscoveryException
  {
    DebugDBProcessor db;
    db = new DebugDBProcessor (dbURL);
    db.setProperties (properties);
    TupleSchema schema = null;

    // dirty hack follows
    try {
      ResultSet columns = db.readFromDB (String.format ("SELECT * FROM %s.%s WHERE 0=1", SCHEMA, tableName));
      ArrayList<String> columnNames = new ArrayList<String> ();
      ArrayList<FieldType> fieldTypes = new ArrayList<FieldType> ();
      ResultSetMetaData rsmd = columns.getMetaData ();
      int numberCols = rsmd.getColumnCount ();

      for (int i = 1; i <= numberCols; i++) {
        String col = rsmd.getColumnLabel (i);
        columnNames.add (col);
        int sqlType = rsmd.getColumnType (i);
        FieldType type = null;
        if (sqlType == Types.INTEGER) type = FieldType.INT_TYPE;
        if (sqlType == Types.VARCHAR) type = FieldType.STRING_TYPE;
        if (sqlType == Types.BOOLEAN) type = FieldType.BOOL_TYPE;
        fieldTypes.add (type);
      }
      columns.close ();

      schema = new TupleSchema ((String[]) columnNames.toArray (new String[columnNames.size ()]),
        (FieldType[]) fieldTypes.toArray (new FieldType[columnNames.size ()]));
    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_GROUPING_DB_ERR);
    }
    db.shutdown ();
    return schema;
  }

  private Tuple result2Tuple (ResultSet results, TupleSchema schema, ArrayList<FieldSetter<Object>> fieldSetters, String colPrefix) throws SQLException, PatternDiscoveryException
  {
    Tuple tuple = schema.createTup ();
	
    int numFields = schema.size ();
    for (int i = 0; i < numFields; i++) {
      String field = schema.getFieldNameByIx (i);
      Object value = null;
      String column = colPrefix + schema2column (field);
      if (schema.getFieldTypeByIx (i).equals (FieldType.BOOL_TYPE)) {
    	  value = results.getBoolean (column);
      }
      else if (schema.getFieldTypeByIx (i).equals (FieldType.INT_TYPE)) {
    	  value = results.getInt (column);
      }
      else {
    	  value = new Text (results.getString (column), properties.getLanguage ());
      }

      fieldSetters.get(i).setVal(tuple, value);
    }
    return tuple;
  }

  private String schema2column (String field)
  {
    // TODO: probably some .s have to be replaces by _s
    return field;
  }

  /**
   * Scan over the Enron database; set up by setUp() and cleaned out by tearDown()
   */

  // @Before
  public void setUp () throws Exception
  {
    // Renice the current thread to avoid locking up the entire system.
    Thread.currentThread ().setPriority (Thread.MIN_PRIORITY);
  }

  // @After
  public void tearDown ()
  {

    // Deregister the Derby driver so that other tests can connect to other
    // databases. This is probably not necessary (we don't currently use
    // derby), but we do it just in case.
    try {
      DriverManager.getConnection ("jdbc:derby:;shutdown=true");
    }
    catch (SQLException e) {
      // The shutdown command always raises a SQLException
      // See http://db.apache.org/derby/docs/10.2/devguide/
    }
    System.gc ();
  }

}
