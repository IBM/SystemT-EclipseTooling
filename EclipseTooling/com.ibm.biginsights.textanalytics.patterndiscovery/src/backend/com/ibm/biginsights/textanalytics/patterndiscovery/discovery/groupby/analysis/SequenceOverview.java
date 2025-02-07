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

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByPreprocessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties.ParseResult;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IntPair;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;

/**
 *  Output for each correlation measure: top, median and bottom ranked correlations with directed
 *         conditional probabilities and translations
 *  Added in sequenceMap for faster sequenceID to sequence lookup
 */
public class SequenceOverview
{


 
	private static HashMap<Integer, Integer> sequenceCounts;
  private static HashMap<IntPair, Integer> coCounts;

  /**
   * Output for each correlation measure: top, median and bottom ranked correlations with directed conditional
   * probabilities and translations With this signature, the queries can be freely chosen. See below, what they should
   * return:
   * 
   * @param properties
   * @param measureFile
   * @param outputFile
   * @param logger
   * @param db
   * @param coCountsQuery columns: sequence 1, sequence 2, count
   * @param sequenceCountQuery columns: sequence 1, sequence 2, count
   * @param overlap
   * @param disregardSet
   * @throws IOException
   * @throws FileNotFoundException
   * @throws SQLException
   */

  public static void displayCorrelated (ExperimentProperties properties, String measureFile, String outputFile,
    IPDLog logger, String coCountsQuery, String sequenceCountQuery, Set<IntPair> overlap,
    HashSet<Integer> disregardSet, Map<Integer, String> sequenceIDMap) throws IOException, SQLException
  {

    String dbUrl = properties.getSequenceDBURL ();
    DebugDBProcessor db = new DebugDBProcessor (dbUrl + ";create=true");
    db.setProperties (properties); // Pass in information to store txt files and database
    
    if (coCounts == null) {
      coCounts = new HashMap<IntPair, Integer> ();
    }
    else {
      coCounts.clear ();
    }
    if (coCounts.isEmpty ()) {
      logger.info ("precaching coCounts");
      ResultSet co = db.readFromDB (coCountsQuery);
      while (co.next ()) {
        coCounts.put (new IntPair (co.getInt (1), co.getInt (2)), co.getInt (3));
      }
    }
    if (sequenceCounts == null) {
      sequenceCounts = new HashMap<Integer, Integer> ();
    }
    else {
      sequenceCounts.clear (); // needed for some reason?
    }
    // prepare counts for conditional analysis
    if (sequenceCounts.isEmpty ()) {
      logger.info ("precaching sequenceCounts");
      ResultSet wc = db.readFromDB (sequenceCountQuery);
      while (wc.next ()) {
        int word = wc.getInt (1);
        int count = wc.getInt (2);
        sequenceCounts.put (word, count);
      }
    }
    // prepare writer
    BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (outputFile),
      GroupByNewProcessor.ENCODING));

    // prepare predicates
    ParseResult measureRange = properties.parse (properties.getProperty (PropertyConstants.CORRELATION_MEASURE_MIN),
                                                 properties.getProperty (PropertyConstants.CORRELATION_MEASURE_MAX));
    ParseResult coRange = properties.parse (properties.getProperty (PropertyConstants.CO_COUNT_RANGE));
    ParseResult xRFRange = properties.parse (properties.getProperty (PropertyConstants.SEQ_X_RELATIVE_FREQUENCY_RANGE));
    ParseResult yRFRange = properties.parse (properties.getProperty (PropertyConstants.SEQ_Y_RELATIVE_FREQUENCY_RANGE));
    // create distance measure
    FileIDDistanceMeasure measure = new FileIDDistanceMeasure (new FileInputStream (measureFile), 0, false);
    // new FileInputStream("groupby/data/titles/sequence_mi.csv"), 0,true);
    // new FileInputStream("groupby/data/titles/sequence_jc.csv"), 0,false);
    // get ranked results (method in distance measure)
    // TODO: avoid sorting below
    List<IntPair> pairs = measure.firstK (Integer.MAX_VALUE, true);

    int printCount = 0;
    logger.info ("computing correlation file");
    for (IntPair pair : pairs) {

      // skip pairs where one side is on the disregard
      if (disregardSet.contains (pair.x) || disregardSet.contains (pair.y)) {
        continue;
      }
      if (pair.x == pair.y) continue;
      // enforce measure range relative frequency and co-occurrence range
      double distance = measure.distance (pair.x, pair.y);
      if (!properties.checkPredicate (measureRange, distance)) continue;
      // if(coCounts == null) coCounts = new HashMap<IntPair, Integer>();//seems to happen for some reason
      Integer coCountInteger = coCounts.get (pair);
      int coCount = coCountInteger == null ? 0 : coCountInteger.intValue ();
      Integer xCountInteger = sequenceCounts.get (pair.x);
      int xCount = xCountInteger == null ? 0 : xCountInteger.intValue ();
      Integer yCountInteger = sequenceCounts.get (pair.y);
      int yCount = yCountInteger == null ? 0 : yCountInteger.intValue ();
      if (!properties.checkPredicate (coRange, coCount)) continue;
      if (!properties.checkPredicate (xRFRange, xCount * 1.0 / coCount)) continue;
      if (!properties.checkPredicate (yRFRange, yCount * 1.0 / coCount)) continue;

      // eliminate overlapping sequences
      if (overlap.contains (pair)) continue;
      // write sequence, correlation and conditional relative frequencies
      String line = "";
      line = constructLine (db, pair, distance, line, sequenceIDMap);
      bw.write (line + "\n");
      // System.out.println(line);
      printCount++;
      // if(printCount==numRules) break;
      if (printCount % 100 == 0) bw.flush ();
      // if(printCount%1000 == 0)System.out.println(printCount);;
    }
    bw.close ();
    db.shutdown ();
  }

  /**
   * Subroutine for output.
   * 
   * @param db
   * @param pair
   * @param distance
   * @param line
   * @return
   * @throws SQLException
   */
  private static String constructLine (DebugDBProcessor db, IntPair pair, double distance, String line,
    Map<Integer, String> sequenceMap) throws SQLException
  {
    line += distance + ",";
    line += "seqX: {" + sequenceMap.get (pair.x) + "}" + " (" + pair.x + ")" + "," + pair.x + ",";
    line += "seqY: {" + sequenceMap.get (pair.y) + "}" + " (" + pair.y + ")" + "," + pair.y + ",";

    line += sequenceConditionalAnalysis (pair.x, pair.y, sequenceCounts.get (pair.x), sequenceCounts.get (pair.y),
      coCounts.get (pair), db);
    line += "," + sequenceCounts.get (pair.x) + "," + sequenceCounts.get (pair.y);
    return line;
  }

  /**
   * Return the sequence ID for a given one word sequence using the SEQUENCES table.
   * 
   * @param word
   * @param dbUrl
   * @return
   * @throws SQLException
   */
  public static int oneWordSequence (String word, String dbUrl) throws SQLException
  {
    DebugDBProcessor db = new DebugDBProcessor (dbUrl);

    ResultSet rs = db.readFromDB (String.format (
      "SELECT DISTINCT seq1.sequenceID FROM sequences seq1, dictionary dict WHERE seq1.wordID = dict.wordID AND dict.surface='%s' AND NOT EXISTS (SELECT * FROM sequences seq2 WHERE seq1.sequenceID=seq2.sequenceID AND seq2.pos=1)",
      GroupByPreprocessor.sqlEscape (word)));
    if (!rs.next ()) return -1;
    int ret = rs.getInt (1);
    db.shutdown ();
    return ret;
  }

  /**
   * Return the sequence ID for a given one word sequence using the SEQUENCES table.
   * 
   * @param word
   * @param dbUrl
   * @return
   * @throws SQLException
   */
  public static int oneWordSequence (int wordID, ExperimentProperties properties) throws SQLException
  {
    DebugDBProcessor db = new DebugDBProcessor (properties.getProperty (PropertyConstants.DB_PREFIX)
      + properties.getRootDir () + properties.getProperty (PropertyConstants.SEQUENCE_DB_NAME));
    db.setProperties (properties);
    ResultSet rs = db.readFromDB (String.format (
      "SELECT DISTINCT seq1.sequenceID FROM sequences seq1 WHERE seq1.wordID = %d AND NOT EXISTS (SELECT * FROM sequences seq2 WHERE seq1.sequenceID=seq2.sequenceID AND seq2.pos=1)",
      wordID));
    if (!rs.next ()) return -1;

    int ret = rs.getInt (1);
    db.shutdown ();
    return ret;
  }

  /**
   * Subroutine for output.
   * 
   * @param seq1
   * @param seq2
   * @param m1Count
   * @param m2Count
   * @param jointCount
   * @param db
   * @return
   * @throws SQLException
   */
  public static String sequenceConditionalAnalysis (int seq1, int seq2, int m1Count, int m2Count, int jointCount,
    DebugDBProcessor db) throws SQLException
  {

    double m1Rate = (jointCount * 1.0 / m1Count);
    double m2Rate = (jointCount * 1.0 / m2Count);
    return jointCount + "," + (m1Rate) + "," + (m2Rate) + "";
  }

}
