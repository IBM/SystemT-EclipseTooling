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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.goldenStandard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis.ComputeCorrelation;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DiscoveryConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IntPair;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.pairs.HashBuckets;

public class GoldStandardLabeling
{



  private static final IPDLog logger = PDLogger.getLogger ("GoldStandardLabeling");   //$NON-NLS-1$

  /**
   * HashMaps for storing mappings for golden standards for values not present in data Keys are the entities mashed into
   * a string with a "," divider
   */
  private static HashMap<String, Integer[]> goldMapping = new HashMap<String, Integer[]> ();
  private static HashMap<String, String> snippetMapping = new HashMap<String, String> ();
  private static HashMap<String, String> entityMapping = new HashMap<String, String> ();

  /**
   * Given a golden standard created earlier in preprocess, will join the values with the final grouping results from
   * the sequence mining portion of the project. Assumes that the grouping table is already in the database with the
   * name of finaloutput
   * 
   * @param db
   * @param properties
   * @throws SQLException
   * @throws IOException
   */
  public static void labelAQLResults (ExperimentProperties properties) throws SQLException, IOException
  {
    logger.info ("Adding gold standard to final grouping");

    String dbUrl = properties.getSequenceDBURL ();
    DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
    db.setProperties (properties);

    // initialize directory structure
    String baseDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR);
    String inputFile = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
    if (inputFile.contains (".")) inputFile = inputFile.substring (0, inputFile.indexOf ('.'));
    baseDir = baseDir + inputFile + File.separator;

    // PostProcessDir
    String postProcessDir = baseDir + properties.getProperty (PropertyConstants.POST_PROCESS_DIR);

    File goldStandard = new File (postProcessDir + "goldStandard.csv");
    String outputFile = postProcessDir + "groupingGolden.csv";

    String snippetFieldName = properties.getProperty (PropertyConstants.SNIPPET_FIELD_NAME);
    // TODO: fix to take in all fields (currently does not work)
    String[] fields = properties.getProperty (PropertyConstants.ENTITY_FIELD_NAMES).split ("\\s*,\\s*");

    String goldStandardQuery = "uniqueID " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE;

    // If there is a snippet view specified - add to results
    if (!snippetFieldName.isEmpty ()) {
      goldStandardQuery = goldStandardQuery + ", " + snippetFieldName + " " + DiscoveryConstants.SNIPPET_COLUMN_TYPE;
    }
    for (String field : fields) {
      goldStandardQuery = goldStandardQuery + ", " + DiscoveryConstants.ENTITY_COLUMN_PREFIX + field + " "
        + DiscoveryConstants.ENTITY_COLUMN_TYPE;
    }
    for (String field : fields) {
      goldStandardQuery = goldStandardQuery + ", is" + field + " " + DiscoveryConstants.ENTITY_COLUMN_TYPE;
    }
    goldStandardQuery = goldStandardQuery + ", isRelated varchar(255), answer "
      + DiscoveryConstants.MECH_TURK_CUM_STATISTIC_COLUMN_TYPE + ", docID " + DiscoveryConstants.DOCID_COLUMN_TYPE;

    // Make sure goldenStandard is imported into the db
    db.importCSV ("APP", "goldenStandard", goldStandardQuery, "null", "", goldStandard, true);

    db.writeToDB ("create index idx_goldenStandard on goldenStandard(docid, " + fields[0] + ", " + fields[1] + ")",
      null);

    String query = "SELECT finaloutput.*";
    for (String field : fields) {
      query = query + ", goldStandard." + field;
    }
    query = query + ", goldenStandard.isRelated";

    // Check if snippet field exists - if so add
    if (!snippetFieldName.isEmpty ()) {
      query = query + ", goldenStandard." + snippetFieldName;
    }

    query = query + ", goldenStandard.answer " + "from finaloutput LEFT JOIN goldenStandard ON "
      + "(finaloutput.id=goldenStandard.uniqueID) order by finaloutput.count DESC";

    db.exportQuery (query, outputFile);

    String importQuery = "count int, hashID int, " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME + " "
      + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE + ", " + "id " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE
      + ", sequence " + DiscoveryConstants.SEQUENCE_STRING_COLUMN_TYPE;

    for (String field : fields) {
      importQuery = importQuery + ", " + DiscoveryConstants.ENTITY_COLUMN_PREFIX + field + " "
        + DiscoveryConstants.ENTITY_COLUMN_TYPE;
    }
    importQuery = importQuery + ", isRelated varchar(255)";

    // Check if snippet field exists - if so add
    if (!snippetFieldName.isEmpty ()) {
      importQuery = importQuery + ", " + snippetFieldName + " " + DiscoveryConstants.SNIPPET_COLUMN_TYPE;
    }

    db.importCSV ("APP", "goldenGrouping", importQuery, "null", "", new File (outputFile), true);

    String importGold = "count int, hashID int, " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME + " "
      + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE + ", " + "id " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE
      + ", sequence " + DiscoveryConstants.SEQUENCE_STRING_COLUMN_TYPE;

    for (String field : fields) {
      importGold = importGold + ", " + DiscoveryConstants.ENTITY_COLUMN_PREFIX + field + " "
        + DiscoveryConstants.ENTITY_COLUMN_TYPE;
    }
    importGold = importGold + "isRelated varchar(255)";
    // Check if snippet field exists - if so add
    if (!snippetFieldName.isEmpty ()) {
      importGold = importGold + ", " + snippetFieldName + " " + DiscoveryConstants.SNIPPET_COLUMN_TYPE;
    }
    importGold = importGold + ", " + snippetFieldName + " +" + DiscoveryConstants.SNIPPET_COLUMN_TYPE + ", answers "
      + DiscoveryConstants.MECH_TURK_CUM_STATISTIC_COLUMN_TYPE;
    db.importCSV ("APP", "goldenGroupingAllVals", importGold, "null", "", new File (outputFile), true);

    db.shutdown ();
  }

  /**
   * For the missing golden value standards, additional processing needs to occur to join the golden standards to the
   * table. Returns the final grouping with gold standards file
   * 
   * @param db
   * @param properties
   * @throws SQLException
   * @throws IOException
   */
  public static void applyMappingtoGrouping (ExperimentProperties properties) throws SQLException, IOException
  {

    String dbUrl = properties.getSequenceDBURL ();
    DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
    db.setProperties (properties);
    // initialize directory structure
    String baseDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR);
    String inputFile = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
    if (inputFile.contains (".")) inputFile = inputFile.substring (0, inputFile.indexOf ('.'));
    baseDir = baseDir + inputFile + File.separator;

    // PostProcessDir
    String postProcessDir = baseDir + properties.getProperty (PropertyConstants.POST_PROCESS_DIR);

    String outputFile = postProcessDir + "groupingGoldenGood.csv";
    String groupByName = properties.getProperty (PropertyConstants.AQL_VIEW_NAME);
    String[] fields = properties.getProperty (PropertyConstants.ENTITY_FIELD_NAMES).split ("\\s*,\\s*");

    String snippetFieldName = properties.getProperty (PropertyConstants.SNIPPET_FIELD_NAME);

    String query = "SELECT * FROM goldenGrouping where isRelated is null";
    logger.fine (query);
    ResultSet goldStandard = db.readFromDB (query);

    while (goldStandard.next ()) {
      int count = goldStandard.getInt ("count");
      int hashID = goldStandard.getInt ("hashid");
      String context = goldStandard.getString (groupByName).replace ("'", "");
      String uniqueID = goldStandard.getString ("ID");
      String sequence = goldStandard.getString ("sequence");
      String entity = entityMapping.get (uniqueID);
      String snippet = snippetMapping.get (entity).replace ("'", "");
      String entity1 = entity.split ("\\s*,\\s*")[0].replace ("'", "");
      String entity2 = entity.split ("\\s*,\\s*")[1].replace ("'", "");
      String isRelated = getMajority (goldMapping.get (entity));

      String values = "(" + count + ", " + hashID + ", '" + context + "', '" + uniqueID + "', '" + sequence + "', '"
        + entity1 + "', '" + entity2 + "', '" + isRelated + "', '" + snippet + "')";
      String insert = "INSERT INTO goldenGrouping VALUES " + values;
      db.writeToDB (insert, null);
    }
    db.writeToDB ("Delete from goldenGrouping where isRelated is null", "null");

    db.exportQuery ("SELECT * from goldenGrouping order by count DESC", outputFile);

    System.out.println ("Done writing grouping file with golden standard at: " + outputFile);

    db.importCSV ("APP", "goldenGroupingFinal", "count int, hashID int, "
      + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME + " " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE + ", "
      + "id " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE + ", sequence "
      + DiscoveryConstants.SEQUENCE_STRING_COLUMN_TYPE + ", " + fields[0] + " " + DiscoveryConstants.ENTITY_COLUMN_TYPE
      + " ," + fields[1] + " " + DiscoveryConstants.ENTITY_COLUMN_TYPE + " ," + "isRelated varchar(255), "
      + snippetFieldName + " " + DiscoveryConstants.SNIPPET_COLUMN_TYPE, "null", "", new File (outputFile), true);
    db.shutdown ();
  }

  public final static int DB_COUNT = 1;
  public final static int DB_HASHID = 2;
  public final static int DB_CONTEXT = 3;
  public final static int DB_ID = 4;
  public final static int DB_SIG = 5;
  public final static int DB_ENTITY1 = 6;
  public final static int DB_ENTITY2 = 7;
  public final static int DB_RELATED = 8;
  public final static int DB_SNIPPET = 9;

  /**
   * Given a grouping file with all the golden standards applied, converts it into a grouping file with Jaccard
   * Distances
   * 
   * @param db
   * @param properties
   * @throws SQLException
   * @throws IOException
   */
  public static void applyJaccardGrouping (ExperimentProperties properties) throws SQLException, IOException
  {

    String dbUrl = properties.getSequenceDBURL ();
    DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
    db.setProperties (properties);

    logger.info ("Apply Jaccard Distance Grouping");

    // initialize directory structure
    String baseDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR);
    String inputFile = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
    if (inputFile.contains (".")) inputFile = inputFile.substring (0, inputFile.indexOf ('.'));
    baseDir = baseDir + inputFile + File.separator;

    // PostProcessDir
    String postProcessDir = baseDir + properties.getProperty (PropertyConstants.POST_PROCESS_DIR);

    String outputFile = postProcessDir + "groupingJaccardGolden.csv";
    String outputScoreFile = postProcessDir + "JaccardScores.csv";

    String snippetFieldName = properties.getProperty (PropertyConstants.SNIPPET_FIELD_NAME);

    String sql = "SELECT * FROM " + "goldenGroupingFinal " + "ORDER BY count DESC";
    logger.fine (sql);
    ResultSet outputGroup = db.readFromDB (sql);

    ArrayList<JaccardObject> storeList = new ArrayList<JaccardObject> ();
    String[] fields = properties.getProperty (PropertyConstants.ENTITY_FIELD_NAMES).split ("\\s*,\\s*");

    // Store grouping into an ArrayList
    while (outputGroup.next ()) {

      int sigCount = outputGroup.getInt ("count");
      int hashID = outputGroup.getInt ("hashid");
      String context = outputGroup.getString (DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME);
      String uniqueID = outputGroup.getString ("uniqueID");
      String signature = outputGroup.getString ("sequence");
      String snippet = outputGroup.getString (snippetFieldName);

      ArrayList<String> pulledFields = new ArrayList<String> ();
      for (String field : fields) {
        pulledFields.add (outputGroup.getString (field));
      }
      String[] fieldResults = new String[pulledFields.size ()];
      pulledFields.toArray (fieldResults);

      JaccardObject temp = new JaccardObject (sigCount, hashID, context, uniqueID, signature, fieldResults, "NO", // currently
                                                                                                                  // remove
        // gold standard
        snippet);
      storeList.add (temp);
    }

    // Store all groups greater than 9
    // TODO: Make the group size variable depending on properties file
    String largeSQL = "SELECT distinct (sequence), hashid FROM " + "goldenGroupingFinal " + "WHERE count > 9 "
      + "group by sequence, hashid";
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
    String smallSQL = "SELECT * FROM " + "goldenGroupingFinal " + "WHERE count < 3 " + "ORDER BY count DESC";
    logger.fine (smallSQL);
    ResultSet smallGroups = db.readFromDB (smallSQL);

    ArrayList<JaccardObject> smallList = new ArrayList<JaccardObject> ();

    while (smallGroups.next ()) {

      int sigCount = smallGroups.getInt ("count");
      int hashID = smallGroups.getInt ("hashid");
      String context = smallGroups.getString (DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME);
      String uniqueID = smallGroups.getString ("uniqueID");
      String signature = smallGroups.getString ("sequence");
      String snippet = smallGroups.getString (snippetFieldName);

      ArrayList<String> pulledFields = new ArrayList<String> ();
      for (String field : fields) {
        pulledFields.add (smallGroups.getString (field));
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
      double bestScore = 1.0; // smaller distance is closest
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
    JaccardObject firstOb = (JaccardObject) storeList.toArray ()[0];
    int curHashID = firstOb.getID ();
    String curSig = firstOb.getSignature ();

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
        lineArr.add (((JaccardObject) ob).getRelated ().toUpperCase ());
        lineArr.add (((JaccardObject) ob).getSnippet ());
        lineArr.add (Integer.toString (((JaccardObject) ob).getID ()));
        lineArr.add ("0.0");

        String[] line = new String[lineArr.size ()];
        lineArr.toArray (line);
        /*
         * String[] line = {Integer.toString(((JaccardObject)ob).getCount()),
         * Integer.toString(((JaccardObject)ob).getID()), ((JaccardObject)ob).getOriginalExtract(),
         * ((JaccardObject)ob).getUniqueID(), ((JaccardObject)ob).getSignature(), ((JaccardObject)ob).getSignature(),
         * ((JaccardObject)ob).getEntity1(), ((JaccardObject)ob).getEntity2(),
         * ((JaccardObject)ob).getRelated().toUpperCase(), ((JaccardObject)ob).getSnippet(),
         * Integer.toString(((JaccardObject)ob).getID()), //jaccardID "0.0"};
         */
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
            lineArr.add (((JaccardObject) match).getRelated ().toUpperCase ());
            lineArr.add (((JaccardObject) match).getSnippet ());
            lineArr.add (Integer.toString (curHashID));
            lineArr.add (Double.toString (storeScores.get (((JaccardObject) match).getID ())));

            String[] line = new String[lineArr.size ()];
            lineArr.toArray (line);
            /*
             * String[] line = {Integer.toString(((JaccardObject)match).getCount()),
             * Integer.toString(((JaccardObject)match).getID()), ((JaccardObject)match).getOriginalExtract(),
             * ((JaccardObject)match).getUniqueID(), ((JaccardObject)match).getSignature(), curSig,
             * ((JaccardObject)match).getEntity1(), ((JaccardObject)match).getEntity2(),
             * ((JaccardObject)match).getRelated().toUpperCase(), ((JaccardObject)match).getSnippet(),
             * Integer.toString(curHashID), Double.toString(storeScores .get(((JaccardObject)match).getID()))};
             */
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
          lineArr.add (((JaccardObject) ob).getRelated ().toUpperCase ());
          lineArr.add (((JaccardObject) ob).getSnippet ());
          lineArr.add (Integer.toString (((JaccardObject) ob).getID ()));
          lineArr.add ("0.0");

          String[] line = new String[lineArr.size ()];
          lineArr.toArray (line);

          /*
           * String[] line = {Integer.toString(((JaccardObject)ob).getCount()),
           * Integer.toString(((JaccardObject)ob).getID()), ((JaccardObject)ob).getOriginalExtract(),
           * ((JaccardObject)ob).getUniqueID(), ((JaccardObject)ob).getSignature(), ((JaccardObject)ob).getSignature(),
           * ((JaccardObject)ob).getEntity1(), ((JaccardObject)ob).getEntity2(),
           * ((JaccardObject)ob).getRelated().toUpperCase(), ((JaccardObject)ob).getSnippet(),
           * Integer.toString(((JaccardObject)ob).getID()), "0.0"};
           */
          toWriteList.add (line);

        }
      }
      curHashID = ((JaccardObject) ob).getID ();
      curSig = ((JaccardObject) ob).getSignature ();
    }

    // Writing all Jaccard scores to separate file
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

    for (String field : fields) {
      query = query + ", " + DiscoveryConstants.ENTITY_COLUMN_PREFIX + field + " "
        + DiscoveryConstants.ENTITY_COLUMN_TYPE;
    }

    query = query + "isRelated varchar(255), " + snippetFieldName + " " + DiscoveryConstants.SNIPPET_COLUMN_TYPE
      + ", jhashID int, jaccardDistance double";

    db.importCSV ("APP", "goldenJaccard", query, "null", "", new File (outputFile), true);
    db.shutdown ();
  }

  /**
   * UniqueIDs are mapped to entity key entity key = entity1+","+entity2
   * 
   * @param db
   * @param properties
   * @return
   * @throws SQLException
   * @throws IOException
   */
  public static HashMap<String, String> createID2EntitiesMapping (ExperimentProperties properties) throws SQLException, IOException
  {
    String dbUrl = properties.getSequenceDBURL ();
    DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
    db.setProperties (properties);
    logger.info ("Creating mappings for entities to uniqueID");

    // initialize directory structure
    String baseDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR);
    String fileName = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
    if (fileName.contains (".")) fileName = fileName.substring (0, fileName.indexOf ('.'));
    baseDir = baseDir + fileName + File.separator;

    String inputFile = baseDir + properties.getProperty (PropertyConstants.INPUT_FILE_DIR) + "/aqlOutput.csv";

    String[] fields = properties.getProperty (PropertyConstants.ENTITY_FIELD_NAMES).split ("\\s*,\\s*");

    // import data with entity1, entity2 mapping to uniqueID
    String importQuery = "docID INT, viewName " + DiscoveryConstants.VIEW_NAME_COLUMN_TYPE + "," + "id "
      + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE + ", tempID int, " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME
      + " " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE + ", " + fields[0] + " "
      + DiscoveryConstants.ENTITY_COLUMN_TYPE + ", " + fields[1] + " " + DiscoveryConstants.ENTITY_COLUMN_TYPE;

    db.importCSV ("APP", "rawAQLAll", importQuery, "null", "null", new File (inputFile), true);

    String query = "SELECT * FROM rawAQLAll";
    logger.fine (query);
    ResultSet rawAQL = db.readFromDB (query);

    while (rawAQL.next ()) {
      String entities = rawAQL.getString (fields[0]) + "," + rawAQL.getString (fields[1]);
      String uniqueID = rawAQL.getString ("id");

      entityMapping.put (uniqueID, entities);
    }
    db.shutdown ();
    return entityMapping;
  }

  /**
   * Additonal golden standard values are mapped to entity key entity key = entity1+","+entity2
   * 
   * @param db
   * @param properties
   * @return
   * @throws SQLException
   */
  public static HashMap<String, Integer[]> createGoldenStandardMapping (ExperimentProperties properties) throws SQLException
  {
    String dbUrl = properties.getSequenceDBURL ();
    DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
    db.setProperties (properties);
    logger.info ("Creating mappings for golden standard");

    String[] fields = properties.getProperty (PropertyConstants.ENTITY_FIELD_NAMES).split ("\\s*,\\s*");
    String snippetFieldName = properties.getProperty (PropertyConstants.SNIPPET_FIELD_NAME);
    String query = "SELECT * FROM goldenStandard";
    logger.fine (query);
    ResultSet goldStandard = db.readFromDB (query);

    while (goldStandard.next ()) {

      String entities = goldStandard.getString (fields[0]) + "," + goldStandard.getString (fields[1]);
      String isRelated = goldStandard.getString ("isRelated");
      String snippet = goldStandard.getString (snippetFieldName);

      if (goldMapping.containsKey (entities)) {
        Integer[] count = tallyLabel (goldMapping.get (entities), isRelated);
        if (snippet != null) {
          snippetMapping.put (entities, snippet);
        }
        goldMapping.put (entities, count);
      }
      else {
        Integer[] empty = { 0, 0, 0 };
        Integer[] count = tallyLabel (empty, isRelated);
        if (snippet != null) {
          snippetMapping.put (entities, snippet);
        }
        goldMapping.put (entities, count);
      }

    }
    db.shutdown ();
    return goldMapping;
  }

  /**
   * Takes an int array of [yes,no,partial] and a label and updates the array dependent on the value of the label
   * 
   * @param count
   * @param label
   * @return
   */
  private static Integer[] tallyLabel (Integer[] count, String label)
  {

    int yes = count[0];
    int no = count[1];
    int partial = count[2];

    if (label.equalsIgnoreCase ("yes")) {
      yes++;
    }
    else if (label.equalsIgnoreCase ("no")) {
      no++;
    }
    else if (label.equalsIgnoreCase ("partially")) {
      partial++;
    }
    Integer[] toReturn = { yes, no, partial };
    return toReturn;
  }

  /**
   * Returns the value given an int array of [yes,no,partial] of the label with the highest value
   * 
   * @param answers
   * @return
   */
  private static String getMajority (Integer[] answers)
  {

    int yes = answers[0];
    int no = answers[1];
    int partial = answers[2];

    if ((yes > no) & (yes > partial)) {
      return "yes";
    }
    else if ((no > yes) & (no > partial)) {
      return "no";
    }
    else {
      return "partially";
    }
  }
}
