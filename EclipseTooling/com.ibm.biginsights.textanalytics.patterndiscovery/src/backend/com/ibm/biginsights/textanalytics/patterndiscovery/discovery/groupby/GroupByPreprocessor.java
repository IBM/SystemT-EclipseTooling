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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.avatar.algebra.datamodel.AbstractTupleSchema;
import com.ibm.avatar.algebra.datamodel.FieldGetter;
import com.ibm.avatar.algebra.datamodel.FieldType;
import com.ibm.avatar.algebra.datamodel.Pair;
import com.ibm.avatar.algebra.datamodel.Span;
import com.ibm.avatar.algebra.datamodel.SpanText;
import com.ibm.avatar.algebra.datamodel.TLIter;
import com.ibm.avatar.algebra.datamodel.Text;
import com.ibm.avatar.algebra.datamodel.Tuple;
import com.ibm.avatar.algebra.datamodel.TupleList;
import com.ibm.avatar.algebra.datamodel.TupleSchema;
import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.DocReader;
import com.ibm.avatar.api.ExternalTypeInfo;
import com.ibm.avatar.api.OperatorGraph;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.biginsights.textanalytics.nature.utils.ExternalDictionary;
import com.ibm.biginsights.textanalytics.nature.utils.ExternalTable;
import com.ibm.biginsights.textanalytics.nature.utils.ModuleMetadataLoader;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DiscoveryConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.UniqueIdentifier;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.nlp.WordIntegerMapping;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.AQLUtils;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;

/**
 *  Runs the old pipeline for sequence mining, rule generation and hashing. The old pipeline is based
 *         on the standard sequence mining algorithm plus separate overlap and subsumption computation. Processing can
 *         be configured by a property file. For a commented example of a property file for this class see
 *         groupby/data/titles/aqlGroupBy.properties It is a sister-program to {@link GroupByNewProcessor}.
 *  Chu Removed many old functions and old pipeline Check old project (PatternDiscovery-executable) if
 *         needed Uses newest AQL jar
 */
public class GroupByPreprocessor
{
  
  @SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

  private static final IPDLog logger = PDLogger.getLogger ("GroupByPreprocessor");  //$NON-NLS-1$
  private static HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> storeEntities;

//  OperatorGraph og = null;
//
  /**
   * Runs the AQL query specified as aqlQuery in the property file and stores the output in the database given as
   * aomDbName.
   * 
   * @param properties
   * @return
   * @throws PatternDiscoveryException
   * @throws Exception
   */
  public static boolean runAQL (ExperimentProperties properties, GroupByNewProcessor processor) throws PatternDiscoveryException
  {
    logger.info ("Starting runAQL");

    // Renice the current thread to avoid locking up the entire system.
    Thread.currentThread ().setPriority (Thread.MIN_PRIORITY);

    // Get database to store aomdata into
    String derbyAomDbName = properties.getProperty (PropertyConstants.RESULTS_DB_NAME);

    // initialize directory structure
    String baseDir = properties.getRootDir ();

    // Directory where SystemT outputs are stored into
    String outputDir = baseDir + properties.getProperty (PropertyConstants.INPUT_FILE_DIR);

    // PostProcessDir
    String postProcessDir = baseDir + properties.getProperty (PropertyConstants.POST_PROCESS_DIR);
    String mechTurkDir = baseDir + properties.getProperty (PropertyConstants.MECHANICAL_TURK_DIR);
    String mechTurkAqlQuery = properties.getProperty (PropertyConstants.FILE_ROOT_DIR)
      + properties.getProperty (PropertyConstants.MECHANICAL_TURK_AQL_QUERY);

    // Information about the aql query
    String aqlFieldName = properties.getProperty (PropertyConstants.GROUP_BY_FIELD_NAME);
    String relationshipFields = properties.getProperty (PropertyConstants.ENTITY_FIELD_NAMES);
    Boolean addGold = "true".equalsIgnoreCase (properties.getProperty (PropertyConstants.ADD_GOLD_STANDARD));
    // Boolean overLap = "true".equalsIgnoreCase(properties
    // .getProperty(PropertyConstants.REPLACE_ENTITY));
    Boolean overLap = !relationshipFields.isEmpty ();

    String viewName = properties.getProperty (PropertyConstants.AQL_VIEW_NAME);

    if (processor.isCancelling ()) { return false; }

    // Store entities in an ArrayList - only if there exists entities
    ArrayList<String> entities = new ArrayList<String> ();
    if (!relationshipFields.isEmpty ()) {
      // Create a map for all other entities not in the current View
      HashMap<String, ArrayList<String>> entitiesMap = new HashMap<String, ArrayList<String>> ();
      // Split the input based on ","
      String[] viewAndRelField = relationshipFields.split ("\\s*,\\s*");  //$NON-NLS-1$
      for (String t : viewAndRelField) {
        // Split the view name from the field name
        String fName = t.substring (t.lastIndexOf (PDConstants.VIEW_SPAN_SEPARATOR) + 1);
        String vName = t.substring (0, t.lastIndexOf (PDConstants.VIEW_SPAN_SEPARATOR));

        if (vName.equalsIgnoreCase (viewName)) {
          entities.add (fName); // store the field name specific to
          // current view
        }
        else {
          ArrayList<String> fieldNames;
          if (entitiesMap.containsKey (vName)) {
            fieldNames = entitiesMap.get (vName);
          }
          else {
            fieldNames = new ArrayList<String> ();
          }
          fieldNames.add (fName);
          entitiesMap.put (vName, fieldNames);
        }
      }
      // store in properties the mapping
      properties.setReplaceEntityHashMap (entitiesMap);
    }

    if (processor.isCancelling ()) { return false; }

    // Only will compile entire AQL if needed, otherwise use old
    // aqlOutput.csv
    if (!"true".equalsIgnoreCase (properties.getProperty (PropertyConstants.IMPORT_DATA_FROM_FILE))) {

      // Store entities by documentID - then by fieldName
      storeEntities = new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> ();

      // Location of aql query, input, output, and dictionaries
      // Specific for SystemT
      String inputDir = properties.getProperty (PropertyConstants.DOCUMENT_COLLECTION_DIR);
      File inputFile = new File (inputDir + properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME));
      String aqlQuery = properties.getProperty (PropertyConstants.AQL_QUERY_FILE);
      String dictDir = properties.getProperty (PropertyConstants.AQL_DICTIONARY_DIR);
      String include = properties.getProperty (PropertyConstants.AQL_INCLUDES_DIR);
      String jarDir = properties.getProperty (PropertyConstants.AQL_JAR_DIR);
      String snippetField = properties.getProperty (PropertyConstants.SNIPPET_FIELD_NAME);
      String mechTurkTypeName = properties.getProperty (PropertyConstants.MECHANICAL_TURK_VIEW_NAME);
      String rawFileName = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
      LangCode languageCode = properties.getLanguage ();
      TokenizerConfig tokConfig = properties.getTokenizerConfig ();
      File aogFile = properties.getAogFile ();

      // Runs aql query and outputs into a csv file in "input" dir
      try {
        boolean state = runAQL (outputDir, inputDir, inputFile, aqlQuery, aqlFieldName, dictDir, include, jarDir,
          viewName, entities, overLap, rawFileName, languageCode, tokConfig, aogFile, snippetField, properties,
          processor);
        if (!state) return false;
      }
      catch (Exception e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RUN_AQL_ERR);
      }

      if (processor.isCancelling ()) { return false; }

      // Create mechTurk output if enabled
      if ("true".equalsIgnoreCase (properties.getProperty (PropertyConstants.CREATE_MECHANICAL_TURK_INPUT))) {
        try {
          createMechTurkOutput (mechTurkDir, inputDir, inputFile, mechTurkAqlQuery, aqlFieldName, dictDir, include,
                                jarDir, mechTurkTypeName, entities, overLap, properties);
        }
        catch (Exception e) {
          throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_MECHTURK_INPUT_CREATION_ERR);
        }

        try {
          makeUnqiueOutput (mechTurkDir + "mechTurkInput.csv", mechTurkDir + "mechTurkUniqueInput.csv", entities,
            properties.getProperty (PropertyConstants.DB_PREFIX) + derbyAomDbName,
            properties.getProperty (PropertyConstants.AQL_VIEW_NAME));
        }
        catch (SQLException e) {
          throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_MECHTURK_INPUT_CREATION_DB_ERR);
        }
        catch (IOException e) {
          throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_MECHTURK_INPUT_CREATION_WRITE_ERR);
        }
      }
    }

    if (processor.isCancelling ()) { return false; }

    // Replaces entities if turned on - massages raw data
    if (overLap) {
      String outputFile = "";
      if (addGold) {
        outputFile = outputDir + "/aqlOutputPreGold.csv";
      }
      else {
        outputFile = outputDir + "/aqlOutput.csv";
      }

      try {
        replaceEntities (outputDir + "/aqlRawOutput.csv", outputFile, properties.getReplaceEntityHashMap (), viewName);
      }
      catch (IOException e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RUN_AQL_REPLACE_ENTITY_WRITE_ERR);
      }
    }

    if (addGold) {
      try {
        addGoldStandard (postProcessDir + "goldStandard.csv", outputDir + "/aqlOutputPreGold.csv", outputDir
          + "/aqlOutput.csv", properties.getProperty (PropertyConstants.DB_PREFIX) + derbyAomDbName, entities,
          aqlFieldName, properties);
      }
      catch (Exception e) {
        throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RUN_AQL_GOLDSTANDARD_ERR);
      }
    }

    if (processor.isCancelling ()) { return false; }

    // Takes output file and imports into database
    String resultFile = outputDir + "/aqlOutput.csv";
    runFileAQL (resultFile, properties.getProperty (PropertyConstants.DB_PREFIX) + properties.getRootDir ()
      + derbyAomDbName, properties.getProperty (PropertyConstants.AQL_VIEW_NAME), entities, properties);

    return true;
  }

  /**
   * Takes resultsFile (CSV) and imports into database
   * 
   * @param resultFile
   * @param dbUrl
   * @throws PatternDiscoveryException
   */
  public static void runFileAQL (String resultFile, String dbUrl, String typeName,
    ArrayList<String> relationshipFields, ExperimentProperties properties) throws PatternDiscoveryException
  {
    logger.info (dbUrl);

    DebugDBProcessor db = null;
    try {
      logger.info ("Adding AQL results to DB");
      File rawAQLFile = new File (resultFile);

      // Insert data file into database: database name: type_viewName
      db = new DebugDBProcessor (dbUrl);
      db.setProperties (properties); // Pass in information to store txt
      // files and database
      String tableName = "type_" + typeName;
      tableName = tableName.replace (".", "__");

      String query = "docID " + DiscoveryConstants.DOCID_COLUMN_TYPE + ", viewName "
        + DiscoveryConstants.VIEW_NAME_COLUMN_TYPE + ", " + "id " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE
        + ", tempID int, " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME + " "
        + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE;

      db.importCSV (AQLGroupByPersist.SCHEMA, tableName, query, "null", "null", rawAQLFile, true);

      // Store relationships in an iterator
      Iterator<String> fieldItr = relationshipFields.iterator ();

      // Import entire raw input including entities and snippets

      String entireQuery = "docID " + DiscoveryConstants.DOCID_COLUMN_TYPE + ", viewName "
        + DiscoveryConstants.VIEW_NAME_COLUMN_TYPE + ", " + "uniqueID " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE
        + ", tempID INT, " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME + " "
        + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE;

      String snippetFieldName = properties.getProperty (PropertyConstants.SNIPPET_FIELD_NAME);

      // Pull from properties File - all relationship fields expected
      while (fieldItr.hasNext ()) {
        entireQuery = entireQuery + ", " + DiscoveryConstants.ENTITY_COLUMN_PREFIX + fieldItr.next () + " "
          + DiscoveryConstants.ENTITY_COLUMN_TYPE;
      }
      if (!snippetFieldName.isEmpty ()) {
        entireQuery = entireQuery + ", " + snippetFieldName + " " + DiscoveryConstants.SNIPPET_COLUMN_TYPE;
      }

      // import entire Raw Input File
      db.importCSV ("APP", "allRawResults", entireQuery, "null", "", rawAQLFile, true);

      db.shutdown ();

    }
    catch (Exception e) {
      if (db != null) db.shutdown ();
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RUN_AQL_WRITE_ERR);
    }
  }

  /**
   * Stores the mapping from words to integer IDs in a database table called DICTIONARY. If Debug mode then store
   * wordIDMap.csv into debug dir
   * 
   * @param properties
   * @param mapping
   * @param logger
   * @throws SQLException
   * @throws IOException
   */
  public static void storeDictionary (Properties properties, WordIntegerMapping mapping, IPDLog logger) throws SQLException, IOException
  {

    logger.info ("storing dictionary");   //$NON-NLS-1$

    String rootDir = ((ExperimentProperties) properties).getRootDir ();
    DebugDBProcessor db = new DebugDBProcessor (properties.getProperty (PropertyConstants.DB_PREFIX) + rootDir
      + properties.getProperty (PropertyConstants.SEQUENCE_DB_NAME));
    db.setProperties ((ExperimentProperties) properties); // Pass in
    // information
    // to store txt
    // files and
    // database

    // if debug true then store wordID to string in debug dir

    String debugDir = rootDir + properties.getProperty (PropertyConstants.DEBUG_DIR);
    String fileName = "wordIDMap.csv";
    fileName = debugDir + File.separator + fileName;

    File dictMap = new File (fileName);

    CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (fileName),
      GroupByNewProcessor.ENCODING), ',', '"');

    int maxAlph = mapping.getAlphabetSize ();
    for (int key = 0; key < maxAlph; key++) {
      String word = mapping.wordForInt (key);
      word = sqlEscape (word);
      writer.writeNext (new String[] { "" + key, "" + word });
      if ((key % 6500) == 0) writer.flush ();
    }
    writer.close ();

    // store mapping
    db.importCSV ("APP", "dictionary", "wordID INT, surface " + DiscoveryConstants.WORD_MAPPING_COLUMN_TYPE
      + ", PRIMARY KEY (wordID)", "null", "null", dictMap, true);

    // Remove dictionary mapping if not in debug mode
    if (properties.getProperty (PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase ("false")) {
      dictMap.delete ();
    }
    logger.info ("done storing dictionary - creating indices");
    db.requireIndex ("dictionary", false, "wordID");
    db.requireIndex ("dictionary", false, "surface");

    db.shutdown ();
    logger.info ("done creating indices");
  }

  /**
   * Compute "relevant sequences" -> those sequences which (regardless of their presence in rules) should be used during
   * hashing. In this implementation. those are the individual words.
   * 
   * @param dbUrl
   * @param targetFileName
   * @param logger
   * @throws SQLException
   */
  public static void computeRelevantSequences (String dbUrl, String targetFileName, ExperimentProperties properties,
    IPDLog logger) throws SQLException
  {
    logger.info ("starting computeRelevantSequences");
    DebugDBProcessor db = new DebugDBProcessor (dbUrl);
    db.setProperties (properties); // Pass in information to store txt files
    // and database

    File targetFile = new File (targetFileName);
    targetFile.delete ();

    db.writeToDB (
      String.format (
        "CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY('%s','%s',null,null,null)",
        "SELECT DISTINCT sequenceID FROM sequences seq1 WHERE NOT EXISTS (SELECT * FROM sequences seq2 WHERE seq1.sequenceID=seq2.sequenceID AND seq2.pos=1)",
        targetFile.getAbsolutePath ()), null);
    logger.info ("done computeRelevantSequences");
    db.shutdown ();
  }

  /**
   * Returns JSONArray where AQL query was performed on given dataset
   * 
   * @return
   * @throws Exception
   */
  private static boolean runAQL (String outputDir, String inputDir, File inputFile, String aqlQuery,
    String aqlFieldName, String dictDir, String includes, String jarDir, String viewNameProp,
    ArrayList<String> relationshipFields, Boolean replaceEntity, String fileName, LangCode languageCode,
    TokenizerConfig tokConfig, File aogFile, String snippetField, ExperimentProperties properties,
    GroupByNewProcessor processor) throws Exception
  {

    boolean normalizeWhiteSpaces = Boolean.parseBoolean (properties.getProperty (PropertyConstants.IGNORE_EXTRA_WHITESPACES, "false"));
    boolean normalizeNewLines    = Boolean.parseBoolean (properties.getProperty (PropertyConstants.IGNORE_EXTRA_NEWLINES, "false"));

    GroupByNewProcessor.setProgressMonitorMessage (Messages.PD_READ_INPUT, Messages.PD_READ_INPUT_WORK);

    if (processor.isCancelling ()) { return false; }

    // Instantiate SystemT
    OperatorGraph operGraph = getOG (properties);
    if (operGraph == null)
      return false;

    // Open a scan over some documents, and get some information about their schema.
    logger.info (String.format ("Opening DocReader on '%s'", inputFile));

    char delim = FileUtils.getCsvDelimiterChar (properties.getProperty (IRunConfigConstants.DELIMITER));
    DocReader docs = getDocReaderForFile (operGraph, inputFile, delim);
    if (docs == null)
      return false;

    TupleSchema docSchema = operGraph.getDocumentSchema ();

    String labelField = com.ibm.avatar.api.Constants.LABEL_COL_NAME;
    FieldGetter<Text> labelGetter = docSchema.containsField (labelField) && docSchema.getFieldTypeByName (labelField).getIsText ()?
                                    docSchema.textAcc (com.ibm.avatar.api.Constants.LABEL_COL_NAME) : null;

    docs.overrideLanguage (languageCode);

    // Keep track of what document and what tuple currently annotating
    String ndoc = "0";
    int tupCount = 0;

    // Create CSV file
    CSVWriter writer;

    try {
      if (replaceEntity) {
        writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (outputDir + "aqlRawOutput.csv"),
          GroupByNewProcessor.ENCODING), ',', '"');
      }
      else {
        writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (outputDir + "aqlOutput.csv"),
          GroupByNewProcessor.ENCODING), ',', '"');
      }
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RUN_AQL_WRITE_ERR);
    }

    // we are going to provide a progress step here so we need to know the exact size of the collection and based on
    // this we know every how many documents we send an increment to the monitor
    int total = 25; // FIXME this value should be gotten from the doc reader

    // here we need to use the total to predict the step size

    double step_per_doc = (double) Messages.PD_READ_INPUT_STEPS_WORK / (double) total;

    double counter = 0;
    long  docIx = 0;
    // Process the documents one at a time.
    while (docs.hasNext ()) {

      docIx++;

      if (processor.isCancelling ()) {
        writer.close ();
      	return false;
      }

      // JSONObject ob = new JSONObject();
      Tuple doc = docs.next ();

      // If docSchema contains 'label' field, use its content as doc name; otherwise, use docId as doc name.
      String docName = (labelGetter != null) ? labelGetter.getVal (doc).getText ()
                                             : (doc.getOid () != null) ? doc.getOid ().toString ()
                                                                       : "doc_" + docIx;

      ndoc = "\"" + docName + "\"";

      // Store docID with hashmap of fieldName/Val pairs
      HashMap<String, HashMap<String, ArrayList<String>>> allEntities = new HashMap<String, HashMap<String, ArrayList<String>>> ();

      // Index of document within the collection, AKA "document ID"
      // ob.put("DocumentID", ndoc);
      // System.err.printf("\n***** Document %d:\n", ndoc);

      // Document text: comment in if entire text is wanted
      // String docText = textGetter.getVal(doc).getText();
      // ob.put("DocumentText", docText);

      // Annotate the current document, generating every single output
      // type that the annotator produces.
      // Final argument is an optional list of what output types to
      // generate.
      Map<String, TupleList> annots = operGraph.execute (doc, null, null);    // TODO should limit on the selected output view only; TODO How about the last param, external views???

      if (!annots.keySet ().contains (viewNameProp)) {
        if (Constants.DEBUG) System.err.printf ("\n%s is not an output view in %s\n", viewNameProp, aqlQuery);
      }

      // Get the mapping of possible viewNames + fieldNames to replace
      // when grouping
      HashMap<String, ArrayList<String>> replaceEntityMap = properties.getReplaceEntityHashMap ();

      // check if nothing was sent - if empty, create a mapping
      if (replaceEntityMap == null) {
        replaceEntityMap = new HashMap<String, ArrayList<String>> ();
      }

      // Add in the grouping view entities - if any
      replaceEntityMap.put (viewNameProp, relationshipFields);
      properties.setReplaceEntityHashMap (replaceEntityMap);

      // Go through each view and each field and store in all entity
      // mapping
      if (!(replaceEntityMap == null) && (replaceEntity == true)) {
        for (String viewName : replaceEntityMap.keySet ()) {
          populateEntities (annots, viewName, replaceEntityMap.get (viewName), allEntities);
        }
      }

      // Grab tuples associated with the viewName and tuple schema
      TupleList tups = annots.get (viewNameProp);
      AbstractTupleSchema schema = tups.getSchema ();

      // System.err.printf("Output View %s:\n", viewName);
      // ob.put(viewNameProp, ""); //store viewName in json

      TLIter itr = tups.iterator ();
      // JSONArray jtupAr = new JSONArray(); //store each tup specific
      // value

      // Iterate through each tuple in document for the specified view
      while (itr.hasNext ()) {

        if (processor.isCancelling ())
        {
          writer.close ();
        	return false;
        }

        Tuple tup = itr.next ();

        // Get values for what groupings will occur over
        FieldType myFieldtype = new FieldType (schema.getFieldTypeByName (aqlFieldName));
        FieldGetter<Object> myGetter = schema.genericGetter (aqlFieldName, myFieldtype);
        Object groupObj = myGetter.getVal (tup);
        if (groupObj == null)
          continue;

        Span groupSpan = (Span) groupObj;

        HashMap<String, String> fieldNameMap = new HashMap<String, String> ();

        UniqueIdentifier uniqueID = new UniqueIdentifier (docName, tupCount);

        // Go through each relation field in the tuple - store
        Iterator<String> fieldItr = relationshipFields.iterator ();
        while (fieldItr.hasNext ()) {
          String fieldName = fieldItr.next ();
          myFieldtype = new FieldType (schema.getFieldTypeByName (fieldName));
          myGetter = schema.genericGetter (fieldName, myFieldtype);
          groupObj = myGetter.getVal (tup);
          if (groupObj == null)
            continue;

          groupSpan = (Span) groupObj;

          // Add entity span to uniqueID
          uniqueID.addEntitiesSpan (groupSpan);
          String val = groupSpan.getText ();
          fieldNameMap.put (fieldName, val);
        }

        // Add grouping text span to uniqueID
        uniqueID.setGroupingSpan (groupSpan);

        String val = groupSpan.getText (); // group by text

        if (normalizeWhiteSpaces && normalizeNewLines)
          val = val.replaceAll ("([\t\r\n]+[ \t\r\t]*[\t\r\n]+)|([\t\r\n]+)", "\n");

        val = val.replaceAll ("\n", " /n ");

        val = val.replaceAll (",", ";");

        val = val.replace ("\\", "\\\\");		// Escape the backward slash to avoid it combining with
        																		// the following character into an unexpected character.

        fieldNameMap.put (aqlFieldName, val);
        String snippet = "";
        // Get values for where groupings came from "snippet"
        if (!snippetField.isEmpty ()) {
          if (snippetField.equalsIgnoreCase (DiscoveryConstants.SNIPPET_DEFAULT_VALUE)) { // default
            // setting

            // Grab the actual text from the document tuple
            // Text documentText = textGetter.getVal(doc);
            Text documentText = groupSpan.getDocTextObj ();

            // Default grabs 25 characters left and right of the
            // grouping text
            int snippetSpanBegin = groupSpan.getBegin () - 25;
            int snippetSpanEnd = groupSpan.getEnd () + 25;

            // check if the span ends up going past the beginning of
            // the document
            if (snippetSpanBegin < 0) {
              snippetSpanBegin = 0;
            }

            // check if the span ends up going past the end of the
            // document
            if (snippetSpanEnd > documentText.getLength ()) {
              snippetSpanEnd = documentText.getLength ();
            }

            // Create span from created span values
            Span snippetSpan = null;
            try {
              // snippetSpan = Span.makeSpan(myFieldtype, doc,
              // snippetSpanBegin, snippetSpanEnd);
              snippetSpan = Span.makeBaseSpan (groupSpan, snippetSpanBegin, snippetSpanEnd);
              snippet = snippetSpan.getText ();
              snippet = snippet.replace ("\n", " /n ");
              if (snippet.length () > 1000) {
                snippet = snippet.substring (0, 999);
              }
              snippet = "... " + snippet + " ...";
            }
            catch (Exception e) {
              writer.close ();
              throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RUN_AQL_CREATE_DEFAULT_SNIPPET_ERR);
            }
          }
          else {
            FieldType mySnippetType = new FieldType (schema.getFieldTypeByName (snippetField));
            FieldGetter<Object> mySnippetGetter = schema.genericGetter (snippetField, mySnippetType);
            Object SnippetObj = mySnippetGetter.getVal (tup);
            SpanText SnippetSpan = (SpanText) SnippetObj;
            snippet = SnippetSpan.getText ();
            snippet = snippet.replace ("\n", " /n ");
            if (snippet.length () > 1000) {
              snippet = snippet.substring (0, 999);
            }
            snippet = "... " + snippet + " ...";
          }
        }

        String uniqueIDstr = uniqueID.getUniqueID ();

        // jtupSchema.put("type", myFieldtype.toString());
        // jtupSchema.put("value", val);
        // jtupSchema.put("name", aqlFieldName);

        ArrayList<String> inputs = new ArrayList<String> ();
        inputs.add (ndoc);
        inputs.add (viewNameProp);
        inputs.add (uniqueIDstr);
        inputs.add (Integer.toString (tupCount));
        inputs.add (val);

        fieldItr = relationshipFields.iterator ();
        while (fieldItr.hasNext ()) {
          inputs.add (fieldNameMap.get (fieldItr.next ()));
        }
        if (!snippetField.isEmpty ()) {
          inputs.add (snippet);
        }
        inputs.toArray ();
        String[] input = new String[inputs.size ()];
        inputs.toArray (input);
        writer.writeNext (input);

        tupCount++;
        // fields.add(jtupSchema);
        // jtupAr.add(fields);
        // ob.put(viewNameProp, jtupAr);
      }
      // Store all pulled entities with the documentID
      storeEntities.put (ndoc, allEntities);

      // ndoc++;
      // ret.add(ob);

      counter += step_per_doc;
      GroupByNewProcessor.workProgressMonitor ((int) counter);
    }

    // sometimes we may be off by a few increments -- due to the fact that the implementation of the doc reader is not
    // ready yet and I am using an estimate number --
    int missing_increment = Messages.PD_READ_INPUT_STEPS_WORK - (int) counter;
    // therefore we want to catch this here and add needed percent
    GroupByNewProcessor.workProgressMonitor (missing_increment);

    try {
      writer.flush ();
      writer.close ();
    }
    catch (IOException e) {
      throw new PatternDiscoveryException (e, ErrorMessages.PATTERN_DISCOVERY_RUN_AQL_WRITE_ERR);
    }

    if (Constants.DEBUG) System.out.println ("Finished Writing to: " + outputDir);

    if (processor.isCancelling ()) { return false; }
    return true;
  }

  /**
   * Given a view and a list of fields in the view, fills the given mapping of entities to be used later as replacements
   * in the grouping text
   * 
   * @param annots
   * @param viewName
   * @param fieldNames
   * @param allEntities
   */
  public static void populateEntities (Map<String, TupleList> annots, String viewName, ArrayList<String> fieldNames,
    HashMap<String, HashMap<String, ArrayList<String>>> allEntities)
  {

    // initialize hashmap for each entity
    // Default - only stores the fields within the current view
    allEntities.put (viewName, new HashMap<String, ArrayList<String>> ());
    HashMap<String, ArrayList<String>> fieldStore = new HashMap<String, ArrayList<String>> ();

    Iterator<String> fieldItr = fieldNames.iterator ();
    while (fieldItr.hasNext ()) {
      fieldStore.put (fieldItr.next (), new ArrayList<String> ());
    }
    // Store the fieldStore with the viewName
    allEntities.put (viewName, fieldStore);

    // Grab tuples associated with the viewName and tuple schema
    TupleList tups = annots.get (viewName);
    AbstractTupleSchema schema = tups.getSchema ();

    TLIter itr = tups.iterator ();
    while (itr.hasNext ()) {

      Tuple tup = itr.next ();

      // Go through each relation field in the tuple - store
      // for (int i = 0; i< relFields.length; i++)
      fieldItr = fieldNames.iterator ();
      while (fieldItr.hasNext ()) {
        String fieldName = fieldItr.next ();
        FieldType myFieldtype = new FieldType (schema.getFieldTypeByName (fieldName));
        FieldGetter<Object> myGetter = schema.genericGetter (fieldName, myFieldtype);
        Object groupObj = myGetter.getVal (tup);
        if (groupObj == null)
          continue;
        SpanText groupSpan = (SpanText) groupObj;
        String val = groupSpan.getText ();
        val = val.trim ();

        // Store all values in list for each field
        HashMap<String, ArrayList<String>> viewStore = allEntities.get (viewName);
        ArrayList<String> allVals = viewStore.get (fieldName);
        // Check if field already has been added
        if (!allVals.contains (val) && !val.isEmpty ()) allVals.add (val);
        viewStore.put (viewName, allVals);
        allEntities.put (fieldName, viewStore);
      }
    }

  }

  /**
   * Given an input file with docID as the first column and the groupby String in the 4th column - will replace all
   * known entities
   * 
   * @param inputFile
   * @param outputFile
   * @param relationshipFields
   * @throws IOException
   */
  public static void replaceEntities (String inputFile, String outputFile,
    HashMap<String, ArrayList<String>> replaceEntities, String viewNameProp) throws IOException
  {

    final int csv_docid = 0; // Location of the docid in the CSV file
    final int csv_groupBy = 4; // Location of the groupBy string in the CSV
    // file

    // Create a CSV Reader to read input file
    char separator = ',';
    CSVReader reader = new CSVReader (new InputStreamReader (new FileInputStream (inputFile),
      GroupByNewProcessor.ENCODING), separator);

    // Extract all the arguments from the grouping file
    List<String[]> outputList = reader.readAll ();

    reader.close ();

    CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (outputFile),
      GroupByNewProcessor.ENCODING), ',', '"');

    // List to output
    ArrayList<String> writeList = new ArrayList<String> ();

    // Go through all the lines of the CSV file
    for (String[] line : outputList) {

      String documentID = line[csv_docid];
      String groupBy = line[csv_groupBy];

      // Go through the stored entities and replace
      HashMap<String, HashMap<String, ArrayList<String>>> entities = storeEntities.get (documentID);

      // Go through each view and each field name for this particular row
      for (String view : replaceEntities.keySet ()) {
        HashMap<String, ArrayList<String>> viewStore = entities.get (view);
        Iterator<String> fieldItr = replaceEntities.get (view).iterator ();

        while (fieldItr.hasNext ()) {
          String field = fieldItr.next ();
          for (String entity : viewStore.get (field)) {
            groupBy = replaceEntityWithType (groupBy, entity, String.format ("<%s.%s>", view, field));
          }
        }
      }

      // Store values back into array to write the line
      for (int i = 0; i < line.length; i++) {
        if (i == csv_groupBy) {
          writeList.add (groupBy);
        }
        else {
          writeList.add (line[i]);
        }
      }
      String[] lineOut = new String[writeList.size ()];
      writeList.toArray (lineOut);
      writer.writeNext (lineOut);
      writeList.clear ();
    }
    writer.flush ();
    writer.close ();
  }

  /**
   * Replace all entities in the given string with its entity type, except
   * when the entity is part of an entity type in the source string.
   * @param sourceString
   * @param entityString
   * @param typeString The entity type, eg. "&lt;person.phone&gt;"
   * @return
   */
  private static String replaceEntityWithType (String sourceString, String entityString, String typeString)
  {
    int entityStart = sourceString.indexOf (entityString);
    while (entityStart >= 0) {
      int endOfTypeString = isPartOfTypeString (entityString, entityStart, sourceString);

      // The entity substring is part of an existing type string, can't replace it.
      // Look for another entity substring starting from the end of the type String.
      if (endOfTypeString > 0)
        entityStart = sourceString.indexOf (entityString, endOfTypeString);

      // The entity substring is not part of an existing type string so we can replace
      // it with the given type string.
      else {
        // Replace
        String beforeEntityString = sourceString.substring (0, entityStart);
        String afterEntityString = sourceString.substring (entityStart + entityString.length ());
        sourceString = beforeEntityString + typeString + afterEntityString;
        // Look for another entity substring to replace
        entityStart = sourceString.indexOf (entityString, entityStart + typeString.length ());
      }
    }
    return sourceString;
  }

  /**
   * Verify if the entity string at certain position of the source string is contained inside
   * an entity type string with format "&lt;[view].[attribute]&gt;". When it is part of an
   * entity type string, this method returns the end position of the containing entity type
   * string. This value is used for skipping the entity type string.
   * @param entityString
   * @param entityStart
   * @param sourceString
   * @return -1 if it is not part of an entity type string;<br>
   *         the end position of the entity type string containing it.
   */
  private static int isPartOfTypeString (String entityString, int entityStart, String sourceString)
  {
    String beforeEntity = sourceString.substring (0, entityStart);
    String afterEntity = sourceString.substring (entityStart + entityString.length ());

    int locationOfLessSignBeforeEntity = beforeEntity.lastIndexOf ("<");
    int locationOfGreaterSignBeforeEntity = beforeEntity.lastIndexOf (">");

    // Has a '<' in front
    if (locationOfLessSignBeforeEntity > locationOfGreaterSignBeforeEntity) {

      int locationOfLessSignAfterEntity = afterEntity.indexOf ("<");
      int locationOfGreaterSignAfterEntity = afterEntity.indexOf (">");

      // Has a '>' behind
      if ( locationOfGreaterSignAfterEntity > 0 &&
           (locationOfLessSignAfterEntity < 0 || locationOfLessSignAfterEntity > locationOfGreaterSignAfterEntity) ) {

        // has a '.' in between "<" and ">"
        int actualGreaterSignLocAfterEntity = entityStart + entityString.length () + locationOfGreaterSignAfterEntity;
        String theEntityType = sourceString.substring (locationOfLessSignBeforeEntity, actualGreaterSignLocAfterEntity);
        int dotLocation = theEntityType.indexOf (".");
        if (dotLocation >= 0)
          return actualGreaterSignLocAfterEntity;
      }
    }
      
    return -1;
  }

  /**
   * Create an output consumable by MechTurk
   * 
   * @param outputDir
   * @param inputDir
   * @param inputFile
   * @param aqlQuery
   * @param aqlFieldName
   * @param dictDir
   * @param includes
   * @param jarDir
   * @param viewNameProp
   * @param entityType
   * @param entityOverlapName
   * @param relationshipFields
   * @param replaceEntity
   * @throws Exception
   */
  private static void createMechTurkOutput (String outputDir, String inputDir, File inputFile, String aqlQuery,
    String aqlFieldName, String dictDir, String includes, String jarDir, String viewNameProp,
    ArrayList<String> relationshipFields, Boolean replaceEntity, ExperimentProperties properties) throws Exception
  {
    // Open a scan over some documents, and get some information about their schema.
    DocReader docs = new DocReader (inputFile);

    // Instantiate the operator graph and create an instance of the SystemT Runtime.
    OperatorGraph operGraph = getOG (properties);

    // Keep track of what document and what tuple currently annotating
    int ndoc = 0;

    // Create CSV file
    CSVWriter writer = new CSVWriter (new OutputStreamWriter (new FileOutputStream (outputDir + "mechTurkInput.csv"),
      GroupByNewProcessor.ENCODING), ',', '"');

    // Process the documents one at a time.
    while (docs.hasNext ()) {

      // JSONObject ob = new JSONObject();
      Tuple doc = docs.next ();

      // Index of document within the collection, AKA "document ID"
      // This might need to be changed later if exact docID needs to
      // be preserved from input
      // ob.put("DocumentID", ndoc);
      // System.err.printf("\n***** Document %d:\n", ndoc);

      // Document text: comment in if entire text is wanted
      // String docText = textGetter.getVal(doc).getText();

      // Annotate the current document, generating every single output
      // type that the annotator produces.
      // Final argument is an optional list of what output types to
      // generate.
      Map<String, TupleList> annots = operGraph.execute (doc, null, null);

      if (!annots.keySet ().contains (viewNameProp)) {
        if (Constants.DEBUG) System.err.printf ("\n%s is not an output view in %s\n", viewNameProp, aqlQuery);
      }

      // Grab tuples associated with the viewName and tuple schema
      TupleList tups = annots.get (viewNameProp);
      AbstractTupleSchema schema = tups.getSchema ();

      // System.err.printf("Output View %s:\n", viewName);

      TLIter itr = tups.iterator ();

      // Iterate through each tuple in document for the specified view
      while (itr.hasNext ()) {

        Tuple tup = itr.next ();

        HashMap<String, String> fieldNameMap = new HashMap<String, String> ();

        String spanForID = "";
        // Go through each relation field in the tuple - store
        Iterator<String> fieldItr = relationshipFields.iterator ();
        while (fieldItr.hasNext ()) {
          String fieldName = fieldItr.next ();
          FieldType myFieldtype = new FieldType (schema.getFieldTypeByName (fieldName));
          FieldGetter<Object> myGetter = schema.genericGetter (fieldName, myFieldtype);
          Object groupObj = myGetter.getVal (tup);

          SpanText groupSpan = (SpanText) groupObj;
          if (groupSpan instanceof Span)
            spanForID = spanForID + ((Span)groupSpan).getBegin () + ":" + ((Span)groupSpan).getEnd () + ":";
          else  // groupSpan instanceof Text
            spanForID = spanForID + 0 + ":" + ((Text)groupSpan).getLength () + ":";

          String val = groupSpan.getText ();
          fieldNameMap.put (fieldName, val);
        }
        spanForID = spanForID.substring (0, spanForID.length () - 1);

        // Get values for what groupings will occur over
        FieldType myFieldtype = new FieldType (schema.getFieldTypeByName (aqlFieldName));
        FieldGetter<Object> myGetter = schema.genericGetter (aqlFieldName, myFieldtype);
        Object groupObj = myGetter.getVal (tup);
        SpanText groupSpan = (SpanText) groupObj;

        String val = groupSpan.getText (); // group by text
        // val = val.replaceAll("\n", "<br></br>");
        val = val.replaceAll ("\r", "");
        val = val.replaceAll ("ï¿½", "e");
        val = val.replaceAll ("ï¿½", "a");
        val = val.replaceAll ("ï¿½", "");
        val = val.replaceAll ("ï¿½", "u");
        val = val.replaceAll ("ï¿½", "o");
        val = val.replaceAll ("ï¿½", "i");
        val = val.replaceAll ("\t", "  ");
        // val = val.replaceAll(",", " ; ");
        val = val.replaceAll ("	", "   ");

        fieldNameMap.put (aqlFieldName, val);

        String uniqueID = Integer.toString (ndoc) + "-" + spanForID;

        ArrayList<String> inputs = new ArrayList<String> ();
        inputs.add (Integer.toString (ndoc));
        inputs.add (uniqueID);

        fieldItr = relationshipFields.iterator ();
        while (fieldItr.hasNext ()) {
          inputs.add (fieldNameMap.get (fieldItr.next ()));
        }
        inputs.add (val);
        inputs.toArray ();
        String[] input = new String[inputs.size ()];
        inputs.toArray (input);

        writer.writeNext (input);

        // tupCount++;
      }
      ndoc++;
    }
    writer.flush ();
    writer.close ();
    if (Constants.DEBUG) System.out.println ("Finished Writing to: " + outputDir);
  }

  private static OperatorGraph getOG (ExperimentProperties properties) throws TextAnalyticsException
  {
    if (properties.getProperty (PropertyConstants.TEST_MODULE) != null)
      return getOG4Test (properties);

    String projectName = properties.getProperty (PDConstants.PD_PROJECT_NAME_PROP);
    String moduleName = getModuleName (properties);

    TokenizerConfig tokenCfg = ProjectPreferencesUtil.getTokenizerConfig (projectName);

    ExternalTypeInfo eti = getExternalTypeInfo (properties);

    OperatorGraph operGraph = AQLUtils.getOperatorGraph ( projectName, moduleName, eti, tokenCfg );
    return operGraph;
  }

  private static String getModuleName (ExperimentProperties properties)
  {
    String moduleName = properties.getProperty (PropertyConstants.AQL_MODULE_NAME);

    if (moduleName == null) {
      String viewName = properties.getProperty (PropertyConstants.AQL_VIEW_NAME);
      if (viewName.contains ("."))
        moduleName = viewName.substring (0, viewName.lastIndexOf ("."));
    }

    return moduleName;
  }

  @SuppressWarnings("unchecked")
  private static ExternalTypeInfo getExternalTypeInfo (ExperimentProperties properties) throws TextAnalyticsException
  {
  	// Get project and module name.
    String projectName = properties.getProperty (PDConstants.PD_PROJECT_NAME_PROP);
    String moduleName = getModuleName (properties);

    // Create ModuleMetadataLoader to get the ext dicts and tables
    ModuleMetadataLoader moduleCache = ModuleMetadataLoader.getInstance ();
    moduleCache.load (projectName, new String[] { moduleName }, false);	// Do not run on UI thread.

    // get all referenced ext dictionaries.
		Map<String, String> extDicts = (Map<String, String>)properties.get (IRunConfigConstants.EXTERNAL_DICT_MAP);
    List<ExternalDictionary> moduleExtDicts = moduleCache.getAllReferencedExternalDicts ();

    // get only the ext dictionaries referenced by the module.
    Map<String, String> extDicts2 = new HashMap<String, String>();
    for (String edName : extDicts.keySet ()) {
    	if ( containsDict (moduleExtDicts, edName) )
    		extDicts2.put (edName, extDicts.get (edName));
    }

    // get all referenced ext tables.
		Map<String, String> extTables = (Map<String, String>)properties.get (IRunConfigConstants.EXTERNAL_TABLES_MAP);
    List<ExternalTable> moduleExtTables = moduleCache.getAllReferredExternalTables ();

    // get only the ext tables referenced by the module.
    Map<String, String> extTables2 = new HashMap<String, String>();
    for (String etName : extTables.keySet ()) {
    	if ( containsTable (moduleExtTables, etName) )
    		extTables2.put (etName, extTables.get (etName));
    }

    ExternalTypeInfo eti = ProjectPreferencesUtil.createExternalTypeInfo (extDicts2, extTables2);
    return eti;
  }

  private static boolean containsDict (List<ExternalDictionary> dictList, String dictName)
  {
  	for (ExternalDictionary ed : dictList) {
  		if (dictName.equals (ed.getDictName ()))
  			return true;
  	}

  	return false;
  }

  private static boolean containsTable (List<ExternalTable> tableList, String tableName)
  {
  	for (ExternalTable et : tableList) {
  		if (tableName.equals (et.getTableName ()))
  			return true;
  	}

  	return false;
  }

  /**
   * JUnit test doesn't have project, so we have to get OG in different way.
   */
  private static OperatorGraph getOG4Test (ExperimentProperties properties)
  {
    String moduleName = properties.getProperty (PropertyConstants.TEST_MODULE);
    String[] modules = new String[] { moduleName };
    String modulePath = properties.getProperty (PropertyConstants.TEST_TAM_PATH);
    TokenizerConfig tokenizerConfig = properties.getTokenizerConfig ();

    ExternalTypeInfo eti = null;
		try {
			eti = getExternalTypeInfo (properties);
		}
		catch (TextAnalyticsException e) {
			// this function is used for testing, so it's ok to print stack trace here
			e.printStackTrace();
		}

    return AQLUtils.getOperatorGraph (modules, modulePath, eti, tokenizerConfig);
  }

  private static DocReader getDocReaderForFile (OperatorGraph operGraph, File file, char delimiter) throws Exception
  {
    if (operGraph != null && file != null && file.exists ()) {
      if(file.getName ().toLowerCase ().endsWith (".json"))
        return new DocReader (file, operGraph.getDocumentSchema (), getExternalViewsSchema (operGraph));
      else
        return new DocReader (file, operGraph.getDocumentSchema (), null, delimiter);
    }

    return null;
  }

  /**
   * @return map of external view name vs their tuple schema.Where external view name pair consist of: <br>
   *         (1) view's aql name as defined in 'create external view ...' statement <br>
   *         (2) view's external name as defined in external_name clause
   * @throws Exception
   */
  private static Map<Pair<String, String>, TupleSchema> getExternalViewsSchema (OperatorGraph og) throws Exception
  {
    Map<Pair<String, String>, TupleSchema> retVal = new HashMap<Pair<String, String>, TupleSchema> ();
    String[] externalViewNames = og.getExternalViewNames ();
    for (String evn : externalViewNames) {
      Pair<String, String> evnPair = new Pair<String, String> (evn, og.getExternalViewExternalName (evn));
      retVal.put (evnPair, og.getExternalViewSchema (evn));
    }

    return retVal;
  }

  /**
   * Takes all of the possible context strings with their entities pairs and surrounding text and only grabs the
   * distinct values to send to mechTurk TODO: NOT WORKING
   * 
   * @param inFile
   * @param outFile
   * @param relationshipFields
   * @param dbUrl
   * @param groupByName
   * @throws SQLException
   * @throws IOException
   */
  private static void makeUnqiueOutput (String inFile, String outFile, ArrayList<String> relationshipFields,
    String dbUrl, String groupByName) throws SQLException, IOException
  {
    /*
     * logger.info( "Making unique output of aql results: Warning may take 15 minutes"); DebugDBProcessor db = new
     * DebugDBProcessor(dbUrl); db.setProperties(properties); //Pass in information to store txt files and database
     * System.out.println("start import of raw AQL output with snippets"); // Import raw aql output with entire snippet
     * db.importCSV("APP", "inputData", "docid int, id "+DiscoveryConstants.DOCID_COLUMN_TYPE+", "+
     * fields[0]+" "+DiscoveryConstants.ENTITY_COLUMN_TYPE+", "+
     * fields[1]+" "+DiscoveryConstants.ENTITY_COLUMN_TYPE+", "+ DiscoveryConstants
     * .GROUPBY_CONTEXT_COLUMN_NAME+" "+DiscoveryConstants. GROUPBY_CONTEXT_COLUMN_TYPE, "null","1,2,3,4,5", new
     * File(inFile), true); db.writeToDB("create index idx_inputData on inputData(id, docid, " +
     * fields[0]+", "+fields[1]+", "+groupByName+")", null); System.out.println("done importing raw AQL");
     * System.out.println("Start exporting CSV consumable by mechTurk"); String query =
     * "select docid, id, "+fields[0]+", "+fields[1]+", "+groupByName +" from inputdata " +
     * "where id IN (select Min(id) from inputdata group by "+ fields[0]+", "+fields[1]+", "
     * +groupByName+") order by docid, id ASC"; db.exportQuery(query, outFile);
     * System.out.println("Done exporting CSV by mechTurk at: "+outFile);
     */
  }

  /**
   * Takes a post processed gold standard - and applies labels to the result depending on uniqueID (docID+spans)
   * 
   * @param goldStandardFile
   * @param inputFile
   * @param outputFile
   * @param dbUrl
   * @param relationshipFields
   * @param groupByName
   * @throws SQLException
   * @throws IOException
   */
  private static void addGoldStandard (String goldStandardFile, String inputFile, String outputFile, String dbUrl,
    ArrayList<String> relationshipFields, String groupByName, ExperimentProperties properties) throws SQLException, IOException
  {

    logger.info ("Adding gold standard to AQL results");

    DebugDBProcessor db = new DebugDBProcessor (dbUrl);
    db.setProperties (properties); // Pass in information to store txt files
    // and database

    String snippetFieldName = properties.getProperty (PropertyConstants.SNIPPET_FIELD_NAME);
    String importGoldQuery = "uniqueID " + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE;
    if (!snippetFieldName.isEmpty ()) {
      importGoldQuery = importGoldQuery + ", " + snippetFieldName + " " + DiscoveryConstants.SNIPPET_COLUMN_TYPE;
    }

    // Add entities to the import query
    Iterator<String> fieldItr = relationshipFields.iterator ();
    while (fieldItr.hasNext ()) {
      importGoldQuery = importGoldQuery + ", " + DiscoveryConstants.ENTITY_COLUMN_PREFIX + fieldItr.next () + " "
        + DiscoveryConstants.ENTITY_COLUMN_TYPE;
    }

    // Add labeling for entities into query
    fieldItr = relationshipFields.iterator ();
    while (fieldItr.hasNext ()) {
      importGoldQuery = importGoldQuery + ", is" + fieldItr.next () + " " + DiscoveryConstants.ENTITY_COLUMN_TYPE;
    }

    // Add isRelated, answers and docid to the query
    importGoldQuery = importGoldQuery + "isRelated varchar(255), answer "
      + DiscoveryConstants.MECH_TURK_CUM_STATISTIC_COLUMN_TYPE + ", docID " + DiscoveryConstants.DOCID_COLUMN_TYPE;

    // Make sure goldenStandard is imported into the db
    db.importCSV ("APP", "goldenStandard", importGoldQuery, "null", "", new File (goldStandardFile), true);

    // Create index on gold standard
    String createIndexQuery = "create index idx_goldenStandard on goldenStandard(docid";

    fieldItr = relationshipFields.iterator ();
    while (fieldItr.hasNext ()) {
      createIndexQuery = createIndexQuery + ", " + fieldItr.next ();
    }
    createIndexQuery = createIndexQuery + ")";
    db.writeToDB (createIndexQuery, null);

    // Create query to import raw results prior to labeling
    String preGoldQuery = "docID INT, viewName " + DiscoveryConstants.VIEW_NAME_COLUMN_TYPE + ", id "
      + DiscoveryConstants.UNIQUE_ID_COLUMN_TYPE + ", tempID int, " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME
      + " " + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_TYPE;

    fieldItr = relationshipFields.iterator ();
    while (fieldItr.hasNext ()) {
      preGoldQuery = preGoldQuery + ", " + DiscoveryConstants.ENTITY_COLUMN_PREFIX + fieldItr.next () + " "
        + DiscoveryConstants.ENTITY_COLUMN_TYPE;
    }

    // Call to db to import raw results prior to labeling
    db.importCSV ("APP", "AQLPreGold", preGoldQuery, "null", "null", new File (inputFile), true);

    // Create index on raw results
    String createRawIndex = "create index idx_AQLPreGold on AQLPreGold(docid";

    fieldItr = relationshipFields.iterator ();
    while (fieldItr.hasNext ()) {
      createRawIndex = createRawIndex + ", " + fieldItr.next ();
    }
    createRawIndex = createRawIndex + ")";
    db.writeToDB (createRawIndex, null);

    // Do the actual join based on uniqueIDs - sort by count (tempID)
    String query = "SELECT AQLPreGold.*, " + "goldenStandard.isRelated, goldenStandard.answer "
      + "from AQLPreGold LEFT JOIN goldenStandard ON "
      + "(AQLPreGold.id=goldenStandard.uniqueID) order by AQLPreGold.tempID";

    db.exportQuery (query, outputFile);
    db.shutdown ();
  }

  /**
   * Clears out databases from aom schemas when running runAQL
   * 
   * @param dbUrl
   * @throws Exception
   */
  @SuppressWarnings("unused")
  private static void clearOutDB (String dbUrl, ExperimentProperties properties) throws Exception
  {

    // in the target database, clear all the contents of the aom* schemas
    DebugDBProcessor db = new DebugDBProcessor (dbUrl);
    db.setProperties (properties); // Pass in information to store txt files
    // and database

    ResultSet rs = db.readFromDB ("select myschema.schemaname, mytable.tablename from sys.systables mytable, sys.sysschemas myschema where mytable.schemaid=myschema.schemaid AND myschema.schemaname like 'AOM%'");
    while (rs.next ()) {
      String schema = rs.getString (1);
      String table = rs.getString (2);
      String query = String.format ("DROP TABLE %s.%s", schema, table);
      logger.info (query);
      db.writeToDB (query, table);
    }

    db.shutdown ();
  }

  /**
   * Method adapted from com.ibm.avatar.discovery.test.AQLDerbyTests
   */
  public static void tearDown ()
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

  public static String sqlEscape (String s)
  {
    s = s.replaceAll ("([^\\\\])'", "$1\\\\'");
    s = s.replaceAll ("([^\\\\])'", "$1\\\\'");// needed for directly
    // sequenced quotes
    s = s.replaceAll ("([^\\\\])'", "$1\\\\'");
    s = s.replaceAll ("'", "''");// quotes in the beginning
    // if (s.length()>255) s = s.substring(0,254);
    // s = s.replaceAll(";","\\;");
    return s;
  }

  // Deletes all files and subdirectories under dir.
  // Returns true if all deletions were successful.
  // If a deletion fails, the method stops attempting to delete and returns
  // false.
  public static boolean deleteDir (File dir)
  {
    if (dir.isDirectory ()) {
      String[] children = dir.list ();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir (new File (dir, children[i]));
        if (!success) { return false; }
      }
    }

    // The directory is now empty so delete it
    return dir.delete ();
  }

}
