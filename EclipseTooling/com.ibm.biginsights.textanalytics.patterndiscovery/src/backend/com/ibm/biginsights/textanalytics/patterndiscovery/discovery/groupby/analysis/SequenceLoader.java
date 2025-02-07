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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.avatar.algebra.util.tokenize.BaseOffsetsList;
import com.ibm.avatar.algebra.util.tokenize.Tokenizer;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.AQLGroupByPersist;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DiscoveryConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.Transaction;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.SequenceTransaction;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.nlp.DefaultWordIntegerMapping;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.nlp.WordIntegerMapping;

/**
 * Loads sequences into numeric representation. Thus, translates the input AQL matches to integer sequences and storing
 * the mapping {@link WordIntegerMapping}
 * 
 * 
 */
public class SequenceLoader
{


  
	private WordIntegerMapping mapping;
  private Map<Integer, int[]> sequenceMap = new HashMap<Integer, int[]> ();
  private Map<String, String> extractIDMap = new HashMap<String, String> ();
  public static Map<String, ArrayList<String>> entitiesMap;

  // Pattern for entities
  private static Pattern ENTITY_PATTERN = Pattern.compile ("(\\w+)ENTITY(\\w+)");

  private static String SYMBOLS_PATTERN = "[\"!#@\\(\\);:\\-=,%<>()\\/\\+\\*\\]\\[\\.\\p{P}]";

  private static Tokenizer lwTok;

  public static Transaction toTransaction (int[] ints)
  {
    List<Integer> seq = new ArrayList<Integer> (ints.length);
    for (int i : ints) {
      seq.add (new Integer (i));
    }
    return new SequenceTransaction (seq);
  }

  public SequenceLoader (Tokenizer lwTok)
  {
    SequenceLoader.lwTok = lwTok;
  }

  public void setEntitiesMap (Map<String, ArrayList<String>> entities)
  {
    entitiesMap = entities;
  }

  /**
   * Loads the wordTokens from a database containing AQL matching output. The table has to contain one or more columns
   * with the desired textual content plus a column docid assigning a unique number to the AQL match (corresponds to
   * matchid above)
   * 
   * @param dbURL
   * @param maxItemNumber
   * @param tableName
   * @param fields
   * @param transactionIDs
   * @param separator
   * @return
   * @throws SQLException
   * @throws PatternDiscoveryException
   */
  public Map<String, int[]> loadAOMDB (ArrayList<String> transactionIDs, ExperimentProperties properties) throws PatternDiscoveryException
  {

    String tableName = AQLGroupByPersist.SCHEMA + "." + "type_"
      + properties.getProperty (PropertyConstants.AQL_VIEW_NAME).replace (".", "__");

    String fields = DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME.replaceAll ("\\.", "__");

    String aomDbUrl = properties.getProperty (PropertyConstants.DB_PREFIX) + properties.getRootDir ()
      + properties.getProperty (PropertyConstants.RESULTS_DB_NAME);

    String separator = GroupByNewProcessor.COL_SEPARATOR;
    // String separator = properties.getProperty("separator");

    try {
      return loadAOMDB (aomDbUrl, tableName, fields, transactionIDs, separator, null,
        properties);
    }
    catch (SQLException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_MINE_SEQUENCE_LOAD_DB_ERR);
    }
  }

  /**
   * like the above method except that it allows giving a list of texts, into which the textual representations of the
   * sequences will be loaded. The numbers are consecutive and reflect the ID assigned by AQL.
   * 
   * @param dbURL
   * @param maxItemNumber
   * @param tableName
   * @param fields
   * @param transactionIDs
   * @param separator
   * @param texts
   * @return
   * @throws SQLException
   * @throws PatternDiscoveryException
   */
  public Map<String, int[]> loadAOMDB (String dbURL, String tableName,
    String fields, ArrayList<String> transactionIDs, String separator, List<String> texts,
    ExperimentProperties properties) throws SQLException, PatternDiscoveryException
  {

    // Mapping of unique extract ID to word token numbers
    Map<String, int[]> result = new HashMap<String, int[]> ();

    if (mapping == null) mapping = new DefaultWordIntegerMapping ();

    // Read from aom database, grab ID -> AQL Match
    if (Constants.DEBUG) System.out.println ("DB: " + dbURL);
    DebugDBProcessor db = new DebugDBProcessor (dbURL);
    db.setProperties (properties); // Pass in information to store txt files
    // and database
    String sql = String.format ("SELECT %s, ID FROM %s", fields, tableName);
    ResultSet lines = db.readFromDB (sql);
    ResultSetMetaData rsmd = lines.getMetaData ();

    // Check if string is to be normalized: capitalizations and whitespace
    // trim
    boolean removeUppercase = properties.getProperty (PropertyConstants.INPUT_TO_LOWERCASE).equalsIgnoreCase ("true");
    boolean trim = properties.getProperty (PropertyConstants.IGNORE_EXTRA_WHITESPACES).equalsIgnoreCase ("true");

    int columnCount = rsmd.getColumnCount ();
    while (lines.next ()) {
      String text = "";
      String ID = lines.getString ("ID");
      for (int i = 1; i <= columnCount; i++) {
        if (rsmd.getColumnName (i).equals ("ID")) continue;
        if (text.length () > 0) text += " " + separator + " ";
        text += lines.getString (i);

        if (removeUppercase) text = text.toLowerCase ();

        if (trim) text = text.trim ();
      }
      // System.out.println("Adding: "+text);
      int[] thisTransaction = stringToSequence (text, mapping);
      result.put (ID, thisTransaction);
      extractIDMap.put (ID, text); // store extract mapping
      transactionIDs.add (ID);
      if (texts != null) texts.add (text);
    }

    db.shutdown ();
    return result;

  }

  /**
   * Loads Sequence ID to sequence String into mapping
   * 
   * @param dbURL
   * @param maxItemNumber
   * @return
   * @throws SQLException
   */
  public static Map<Integer, String> loadSequence2IDMap (String dbURL, ExperimentProperties properties) throws SQLException
  {
    Map<Integer, String> result = new HashMap<Integer, String> ();

    // Read from DB sequences
    if (Constants.DEBUG) System.out.println ("DB: " + dbURL);
    DebugDBProcessor db = new DebugDBProcessor (dbURL);
    db.setProperties (properties); // Pass in information to store txt files
    // and database
    String query = "SELECT DISTINCT sequenceString, sequence FROM sequences_support";
    ResultSet lines = db.readFromDB (String.format (query));

    // Store into Map
    while (lines.next ()) {
      String sequenceString = lines.getString ("sequenceString");
      int id = lines.getInt ("sequence");
      result.put (id, sequenceString);
    }
    db.shutdown ();
    return result;
  }

  /**
   * Loads the sequences from the specified columns of a CSV file.
   * 
   * @param fileName
   * @param maxItemNumber
   * @param fields
   * @return
   * @throws IOException
   * @throws PatternDiscoveryException
   */
  public Map<String, int[]> loadDBDump (String fileName, String fields) throws IOException, PatternDiscoveryException
  {
    return loadDBDump (fileName, fields, -1);
  }

  public Map<String, int[]> loadDBDump (String fileName, String fields,
    int docIDField) throws IOException, PatternDiscoveryException
  {
    Map<String, int[]> result = new HashMap<String, int[]> ();
    if (mapping == null) mapping = new DefaultWordIntegerMapping ();

    CSVReader reader = new CSVReader (new InputStreamReader (new FileInputStream (fileName),
      GroupByNewProcessor.ENCODING));

    String[] record = reader.readNext ();
    String[] fieldSplit = fields.split (",");
    int rowNum = 0;
    int[] fieldNums = new int[fieldSplit.length];
    for (int i = 0; i < fieldSplit.length; i++) {
      String j = fieldSplit[i];
      fieldNums[i] = Integer.parseInt (j);
    }
    while (record != null) {
      if (record.length >= 2) {
        String text = "";
        for (int i : fieldNums) {
          text += record[i];
        }
        // tokenize
        int[] thisTransaction = stringToSequence (text, mapping);
        String docID = rowNum + "";
        if (docIDField > 0) docID = record[docIDField];
        result.put (docID, thisTransaction);
      }
      record = reader.readNext ();
      rowNum++;
    }
    reader.close ();
    return result;
  }

  /**
   * Translates a string to numeric IDs. Storing the mapping if required.
   * 
   * @param maxItemNumber
   * @param text
   * @param mapping
   * @return
   * @throws PatternDiscoveryException
   */
  public static int[] stringToSequence (String text, WordIntegerMapping mapping) throws PatternDiscoveryException
  {

    for (String viewName : entitiesMap.keySet ()) {
      for (String entity : entitiesMap.get (viewName)) {
        String toReplace = String.format ("<%s.%s>", viewName, entity);
        String newText = String.format ("%sENTITY%s", viewName, entity);
        text = text.replaceAll (toReplace.toLowerCase (), newText);
      }
    }

    // Store all tokens (integer form)
    ArrayList<Integer> intTokens = new ArrayList<Integer> ();
    int count = 0;

    if (lwTok != null) {
      BaseOffsetsList lwTokens = new BaseOffsetsList ();
      String inputStr = text.replaceAll (SYMBOLS_PATTERN, " ");
      CharSequence input = inputStr; // Convert input text into char
      // sequence

      try {

        lwTok.tokenizeStr (input, GroupByNewProcessor.properties.getLanguage (), lwTokens);

        // Go through each token and pull begin, end
        for (int i = 0; i < lwTokens.size (); i++) {
          String token = inputStr.substring (lwTokens.begin (i), lwTokens.end (i));
          Matcher mat = ENTITY_PATTERN.matcher (token);
          if (mat.find ()) {
            token = String.format ("<%s.%s>", mat.group (1), mat.group (2));
          }

          if (token != null && token.length () > 0) {
            intTokens.add (mapping.intForWord (token.trim ()));
            count++;
          }
          // if (count >= maxItemNumber) break;
        }
      }
      catch (Exception e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_MINE_SEQUENCE_TOKENIZER_ERR);
      }
    }
    else { // If tokenizer does not exist - go with default tokenizer
      if (Constants.DEBUG) System.err.println ("Warning: Using default tokenizer - not LW");
      String[] tokens = text.split ("\\W");
      for (String token : tokens) {

        // Special case - entity matching
        Matcher mat = ENTITY_PATTERN.matcher (token);
        if (mat.find ()) {
          token = String.format ("<%s.%s>", mat.group (1), mat.group (2));
        }

        if (token != null && token.length () > 0) {
          intTokens.add (mapping.intForWord (token.trim ()));
          count++;
        }
        // if (count >= maxItemNumber) break;
      }
    }
    // generate array and add
    int[] thisTransaction = new int[intTokens.size ()];
    Iterator<Integer> iter = intTokens.iterator ();
    for (int i = 0; i < intTokens.size (); i++) {
      thisTransaction[i] = iter.next ().intValue ();
    }
    return thisTransaction;
  }

  /**
   * Stores AOM output as a table with the following schema: docid INT, doctext DiscoveryConstants.GROUP_BY_FIELD_NAME
   * The doctext is computed from the AOM output as specified in the properties file by taking the columns listed in the
   * groupBy property and concatenating them putting the seperator in between.
   * 
   * @param properties
   * @throws IOException
   * @throws SQLException
   */
  public static void exportAOM (ExperimentProperties properties, File targetFile) throws IOException, SQLException
  {
    DebugDBProcessor aomDB = new DebugDBProcessor (properties.getProperty (PropertyConstants.DB_PREFIX)
      + properties.getProperty (PropertyConstants.RESULTS_DB_NAME));
    aomDB.setProperties (properties);
    String inTableName = "AOMDATA.TYPE_" + properties.getProperty (PropertyConstants.AQL_VIEW_NAME);
    String fields = DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME.replaceAll ("\\.", "__");
    String separator = GroupByNewProcessor.COL_SEPARATOR;

    // String separator = properties.getProperty("separator");
    // in the line below, concatenate the fields (reuse code from loadAOMDB)
    String sql = String.format ("SELECT %s, ID FROM %s", fields, inTableName);

    CSVWriter bw = new CSVWriter (new OutputStreamWriter (new FileOutputStream (targetFile),
      GroupByNewProcessor.ENCODING), ',', '"');

    if (Constants.DEBUG) System.out.println (aomDB.getDburl ());
    ResultSet lines = aomDB.readFromDB (sql);
    ResultSetMetaData rsmd = lines.getMetaData ();
    int columnCount = rsmd.getColumnCount ();
    while (lines.next ()) {
      String text = "";
      int ID = lines.getInt ("ID");
      for (int i = 1; i <= columnCount; i++) {
        if (rsmd.getColumnName (i).equals ("ID")) continue;
        if (text.length () > 0) text += " " + separator + " ";
        text += lines.getString (i);
      }
      bw.writeNext (new String[] { ID + "", text });
    }
    bw.close ();
    aomDB.shutdown ();
  }

  /**
   * Translates the numeric sequence representation back to a string based on the specified mapping.
   * 
   * @param sequence
   * @param mapping
   * @return
   */
  public static String sequenceToString (int[] sequence, WordIntegerMapping mapping)
  {
    String result = "";
    for (int token : sequence) {
      if (result.length () > 0) result += " ";
      result += mapping.wordForInt (token);
    }
    return result;
  }

  public WordIntegerMapping getMapping ()
  {
    return mapping;
  }

  public void setMapping (WordIntegerMapping mapping)
  {
    this.mapping = mapping;
  }

  public Map<Integer, int[]> getSequenceMapping ()
  {
    return sequenceMap;
  }

  public void setSequenceMapping (Map<Integer, int[]> map)
  {
    this.sequenceMap = map;
  }

  public Map<String, String> getExtractIDMapping ()
  {
    return extractIDMap;
  }

  public void setExtractIDMapping (Map<String, String> map)
  {
    this.extractIDMap = map;
  }

}
