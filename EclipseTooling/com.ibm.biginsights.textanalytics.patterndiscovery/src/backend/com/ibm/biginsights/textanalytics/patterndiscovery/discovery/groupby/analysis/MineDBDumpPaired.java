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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.DBSequenceMiner;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.nlp.WordIntegerMapping;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;

/**
 * Loads a DBDump and mines it for frequent subsequences using the paired sequence miner. - Creating a data structure as
 * required by the itemset miner - Initialize and run the paired sequence miner according to the specification in the
 * parameters - The miner outputs a table with information on the relevant sequences and their occurrences into a file
 * named getFrequentMatchPairs.csv The output has the following columns: - frequent sequence e.g. 3-65-77. The actual
 * sequence -frequent sequence hash: an (almost certainly) unique number for that sequence. (sequence ID) -supporting
 * transaction ID. - matchStart start position of the match in the transaction - matchEnd end position of the match in
 * the transaction - unsubsumed 1 if there is no longer match that contains this match at the given position.
 * 
 * 
 */
public class MineDBDumpPaired
{


  
	private static IPDLog logger;

  /**
   * - Creating a data structure as required by the itemset miner - Initialize and run the paired sequence miner
   * according to the specification in the parameters - The miner outputs a table with information on the relevant
   * sequences and their occurrences into a file named getFrequentMatchPairs.csv The output has the following columns: -
   * frequent sequence e.g. 3-65-77. The actual sequence -frequent sequence hash: an (almost certainly) unique number
   * for that sequence. (sequence ID) -supporting transaction ID. - matchStart start position of the match in the
   * transaction - matchEnd end position of the match in the transaction - unsubsumed 1 if there is no longer match that
   * contains this match at the given position. saves memory by directly performing the mining task on a database table.
   * 
   * @param sequences
   * @param transactionIDs
   * @param mapping
   * @param properties
   * @param externalLogger
   * @param toDB
   * @param analyseFrequency
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws SQLException
   * @throws PatternDiscoveryException
   */
  public static void mineDBDumpInDB (Map<String, int[]> sequences, Map<String, String> extractedValues,
    ArrayList<String> transactionIDs, WordIntegerMapping mapping, ExperimentProperties properties,
    IPDLog externalLogger, boolean toDB, boolean analyseFrequency) throws SQLException, IOException, PatternDiscoveryException
  {

    logger = externalLogger;

    // Grab properties information
    int maxSequenceSize = Integer.parseInt (properties.getProperty (PropertyConstants.SEQUENCE_MAX_SIZE));
    int minSequenceSize = Integer.parseInt (properties.getProperty (PropertyConstants.SEQUENCE_MIN_SIZE));
    int minSupport = Integer.parseInt (properties.getProperty (PropertyConstants.SEQUENCE_MIN_FREQUENCY_SIZE));

    // initialize and run FIM
    // delete tables
    logger.info ("deleting old data");
    String dbURL = properties.getProperty (PropertyConstants.DB_PREFIX) + properties.getRootDir ()
      + properties.getProperty (PropertyConstants.SEQUENCE_DB_NAME);
    if (Constants.DEBUG) System.err.println (dbURL);
    DebugDBProcessor db = new DebugDBProcessor (dbURL, properties.getProperty (PropertyConstants.SEQUENCE_DB_PASSWORD),
      properties.getProperty (PropertyConstants.SEQUENCE_DB_PASSWORD), properties.getRootDir ());

    db.setProperties (properties); // Pass in information to store txt files and database
    try {
      db.writeToDB ("DROP TABLE SEQUENCE_INSTANCES", "SEQUENCE_INSTANCES");
    }
    catch (Exception e) {// probably wasn't there in the first place
    }
    try {
      db.writeToDB ("DROP TABLE TOKEN_MAP", "TOKEN_MAP");
    }
    catch (Exception e) {// probably wasn't there in the first place
    }

    logger.info ("starting mining. DB: " + dbURL);
    DBSequenceMiner dbm = new DBSequenceMiner (dbURL, properties.getProperty (PropertyConstants.SEQUENCE_DB_USER),
      properties.getProperty (PropertyConstants.SEQUENCE_DB_PASSWORD));

    // release resources used by db
    db.shutdown ();
    
    GroupByNewProcessor.workProgressMonitor (Messages.PD_SEQUENCE_MINING_STEP2_WORK);

    dbm.doMine (sequences, extractedValues, mapping, maxSequenceSize, minSequenceSize, minSupport, properties, logger);

    // translate back to words and output
    logger.info ("done mining");

  }

}
