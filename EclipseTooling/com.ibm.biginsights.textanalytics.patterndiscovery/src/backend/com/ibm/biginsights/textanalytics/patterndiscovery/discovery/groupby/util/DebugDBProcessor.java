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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;

// import com.ibm.avatar.discovery.readers.DBProcessor;

/**
 * Like {@link DBProcessor} but with individual connection object for each instance and a separate statement object for
 * each query. Some convenience methods have been added.
 * 
 *  (author of original DBProcessor)
 * 
 */

public class DebugDBProcessor
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
 
	// url to database to be queried
  private String dburl = "";
  private String userName;
  private String password;
  private IPDLog logger = PDLogger.getLogger ("DebugDBProcessor");

  // jdbc connection
  private Connection conn = null;
  private Statement readStmt = null;
  private Statement writeStmt = null;

  // query results
  private ResultSet results;
  @SuppressWarnings("unused")
  private ResultSetMetaData rsmd;

  // properties information
  private ExperimentProperties properties;

  /*
   * default constructor
   */
  public DebugDBProcessor (String dburl)
  {
    this.dburl = dburl;
    this.createConnection ();
  }

  public DebugDBProcessor (String dburl, String userName, String password, String rootDir)
  {
    this.userName = userName;
    this.password = password;
    this.dburl = dburl;

    if (isMySQL ()) {
      try {
        Class.forName ("com.mysql.jdbc.Driver");
      }
      catch (ClassNotFoundException e) {
        e.printStackTrace ();
      }
    }
    this.createConnection ();
  }

  /**
   * Setter - sets the experiment properties for the DBProcessor
   * 
   * @param prop
   */
  public void setProperties (ExperimentProperties prop)
  {
    this.properties = prop;
    String baseDir = properties.getProperty (PropertyConstants.FILE_ROOT_DIR);
    String inputFile = properties.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME);
    if (inputFile.contains (".")) inputFile = inputFile.substring (0, inputFile.indexOf ('.'));
    baseDir = baseDir + inputFile + File.separator;
  }

  /*
   * insert data into the database
   */
  public void writeToDB (String query, String table, String... columns) throws SQLException
  {
    // invalidate indices by parameters
    if (table != null && query.matches ("(?i).*\\b(UPDATE|TRUNCATE|DELETE|INSERT)\\b.*")) {
      invalidateIndices (table, columns);
    }

    writeStmt = conn.createStatement ();

    logger.info (query);
    writeStmt.execute (query);
  }

  final static String EXISTS_DERBY = "select * from sys.systables where tablename='%s'";    // not sure if it should rather be like in db2.
  final static String EXISTS_DB2 = "select * from sysibm.tables where table_name='%s'";
  final static String EXISTS_MYSQL = "SELECT table_name FROM information_schema.tables WHERE table_name = '%s'";

  public boolean tableExsists (String tableName) throws SQLException
  {
    ResultSet results;
    if (conn == null) createConnection ();

    readStmt = conn.createStatement ();
    String query = null;
    if (isDerby ()) {
      query = String.format (EXISTS_DERBY, tableName.toUpperCase ());
    }
    if (isMySQL ()) {
      query = String.format (EXISTS_MYSQL, tableName);
    }
    if (isDB2 ()) {
      query = String.format (EXISTS_DB2, tableName.toUpperCase ());
    }
    logger.info (query);
    try {
      results = readStmt.executeQuery (query);
    }
    catch (Exception e) {
      return false;
    }
    boolean ret = results.next ();
    results.close ();

    return ret;

  }

  public void reCreateTable (String tableName, String columns) throws SQLException
  {
    if (tableExsists (tableName)) {
      writeToDB ("DROP TABLE " + tableName, tableName, columns.split (","));
    }
    writeToDB (String.format ("CREATE TABLE %s (%s)", tableName, columns), tableName, columns.split (","));
  }

  /*
   * start query the database
   */
  public ResultSet readFromDB (String query) throws SQLException
  {
    readStmt = conn.createStatement ();
    results = readStmt.executeQuery (query);

    return results;
  }

  public ResultSet readFromDBForUpdate (String query) throws SQLException
  {
    readStmt = conn.createStatement (ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
    logger.info (query);
    results = readStmt.executeQuery (query);

    return results;
  }

  /*
   * establish database connection
   */
  private void createConnection ()
  {
    try {
      this.shutdown ();

      // Load Derby's embedded driver
      Class.forName ("org.apache.derby.jdbc.EmbeddedDriver").newInstance (); // Get
      // a
      // connection
      if (userName != null && password != null) {
        try {
          conn = DriverManager.getConnection (dburl);
        }
        catch (Exception e) {
          // try creating the DB
          conn = DriverManager.getConnection (dburl + ";create=true");
        }
      }
      else {
        try {
          conn = DriverManager.getConnection (dburl);
        }
        catch (Exception e) {
          // try creating the DB
          conn = DriverManager.getConnection (dburl + ";create=true");
        }

      }
      Statement st = conn.createStatement ();
      st.execute ("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('derby.locks.waitTimeout', '120')");
      // conn.setAutoCommit(false);
    }
    catch (Exception except) {
      except.printStackTrace ();
    }
  }

  private Connection getNewConnection ()
  {
    try {
      Class.forName ("org.apache.derby.jdbc.EmbeddedDriver").newInstance ();
      if (userName != null && password != null) {
        return DriverManager.getConnection (dburl);
      }
      else {
        return DriverManager.getConnection (dburl);
      }
    }
    catch (InstantiationException e) {
      e.printStackTrace ();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace ();
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace ();
    } // Get a connection
    catch (SQLException e) {
      e.printStackTrace ();
    }
    return null;

  }

  public PreparedStatement prepareStatement (String query, String table, String... columns) throws SQLException
  {
    // invalidate indices by parameters
    if (table != null) {
      invalidateIndices (table, columns);
    }
    logger.info ("PreparedStatement: " + query);
    return getNewConnection ().prepareStatement (query);
  }

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

  /*
   * shut down database connection
   */
  public void shutdown ()
  {
    try {
      this.stop ();
      if (conn != null) {
        conn.close ();
        conn = null;
        DriverManager.getConnection (dburl + ";shutdown=true");
      }
    }
    catch (SQLException sqlExcept) {
      // The shutdown command always raises a SQLException
      // See http://db.apache.org/derby/docs/10.2/devguide/
    }
    System.gc ();
  }

  /*
   * stop reading data from database
   */
  public void stop ()
  {

    try {
      if (results != null) {
        results.close ();
      }

      if (readStmt != null) {
        logger.info ("DebugDBProcessor is closing the read statement");
        readStmt.close ();
      }

      if (writeStmt != null) {
        logger.info ("DebugDBProcessor is closing the write statement");
        writeStmt.close ();
      }
    }
    catch (SQLException sqlExcept) {
      sqlExcept.printStackTrace ();
    }
  }

  /**
   * Loads a CSV file into a table. The table is created previously. If it exists, it can optionally be dropped prior to
   * import. Because it seems that at least in derby insert time grows more than linear from a certain size on. A chunk
   * size can be provided and imports are split into chunks at this number of lines.
   * 
   * @param schema The schema in which the target table is to be located.
   * @param tableName The name of the target table.
   * @param columnDefinitions The definition of the target table columns as provided to a Derby CREATE TABLE statement.
   * @param sourceTargetColumnMapping The names of the target columns in the order they are found in the input. Format:
   *          'NAME,YEAR,AMOUNT' may be "null" if the input matches the target table exactly.
   * @param sourceColumnChoice The columns of the input file that are to be imported. Format: '2,4,3' may be "null" if
   *          the input matches the target table exactly.
   * @param sourceFileName The path to the source file.
   * @param dropIfExists If true, the table fill be dropped if it exists already.
   * @throws SQLException
   */
  final static int DEFAULT_CHUNKSIZE = 20000000;

  public void importCSV (String schema, String tableName, String columnDefinitions, String sourceTargetColumnMapping,
    String sourceColumnChoice, File sourceFile, boolean dropIfExists) throws SQLException, IOException
  {
    importCSV (schema, tableName, columnDefinitions, sourceTargetColumnMapping, sourceColumnChoice, sourceFile,
      dropIfExists, DEFAULT_CHUNKSIZE);
  }

  public void importCSV (String schema, String tableName, String columnDefinitions, String sourceTargetColumnMapping,
    String sourceColumnChoice, File sourceFile, boolean dropIfExists, int chunkSize) throws SQLException, IOException
  {
    // invalidate all indices
    invalidateIndices (tableName);
    // String schema = "APP";
    if (isDB2 ()) schema = userName.toUpperCase ();
    if (isDerby () || isDB2 ()) {
      importCSVDerbyDB2 (schema, tableName, columnDefinitions, sourceTargetColumnMapping, sourceColumnChoice,
        sourceFile, dropIfExists, chunkSize);
    }
    else {
      importCSVLoad (schema, tableName, columnDefinitions, sourceTargetColumnMapping, sourceColumnChoice, sourceFile,
        dropIfExists, chunkSize);
    }

  }

  private static final String LOAD_IMPORT_STATEMENT_MYSQL = " LOADDATA INFILE '%s' INTO TABLE %s %s %s";
  private final static String CSV_FORMAT = " FIELDS TERMINATED BY ',' ENCLOSED BY '\"' ESCAPED BY '\\\\' LINES TERMINATED BY '\\n' STARTING BY '' ";

  private void importCSVLoad (String schema, String tableName, String columnDefinitions,
    String sourceTargetColumnMapping, String sourceColumnChoice, File sourceFile, boolean dropIfExists, int chunkSize) throws SQLException, IOException
  {
    // realize dropIfExists
    if (tableExsists (tableName)) {
      if (dropIfExists) {
        writeToDB (String.format ("DROP TABLE %s", tableName), tableName);
      }
    }
    // if not existing, create table with given column Definition
    String fileColumns = "";
    if (!tableExsists (tableName)) {
      writeToDB (String.format ("CREATE TABLE %s (%s)", tableName, columnDefinitions), tableName);
    }
    // realize sourceTargetColumnMapping
    if (!sourceTargetColumnMapping.equalsIgnoreCase ("NULL")) {
      fileColumns = "(" + sourceTargetColumnMapping + ")";
    }
    else {

    }
    // realize sourceColumnChoice
    if (!sourceColumnChoice.equalsIgnoreCase ("NULL")) {
      // parse schema def from table into array
      String[] labels;
      int maxSourceCol = 0;
      if (sourceTargetColumnMapping.equalsIgnoreCase ("NULL")) {
        ResultSet rs = readFromDB ("SELECT * FROM " + tableName);
        ResultSetMetaData md = rs.getMetaData ();
        int cc = md.getColumnCount ();
        labels = new String[cc + 1];
        for (int i = 1; i <= cc; i++) {
          labels[i] = md.getColumnName (i);
        }
      }
      else {
        labels = sourceTargetColumnMapping.split ("\\s*,\\s*");
      }
      for (String choice : sourceColumnChoice.split ("\\s*,\\s*")) {
        maxSourceCol = Math.max (maxSourceCol, Integer.parseInt (choice));
      }
      for (int i = 1; i <= maxSourceCol; i++) {// for each position in the
        // file
        // look at which point it has been mentioned in
        // sourceTargetColumnChoice
        int pos = -1;
        int ii = 1;
        for (@SuppressWarnings("unused")
        String choice : sourceColumnChoice.split ("\\s*,\\s*")) {
          pos = ii;
          ii++;
        }
        // put column name or dummy column name accordingly
        if (fileColumns.length () > 0) fileColumns += ",";
        if (pos > 0) {
          fileColumns += labels[pos];
        }
        else {
          fileColumns += "@dummy";
        }
      }
      fileColumns = "(" + fileColumns + ")";
    }
    // write statement
    // specify row and column delimiters and escape chars
    String query = String.format (LOAD_IMPORT_STATEMENT_MYSQL, sqlEscape (sourceFile.getAbsolutePath ()), tableName,
      CSV_FORMAT, fileColumns);

    // System.out.println(query);
    writeToDB (query, tableName);

  }

  public String sqlEscape (String s)
  {
    s = s.replaceAll ("\\\\", "\\\\\\\\");
    s = s.replaceAll ("([^\\\\])'", "$1\\\\'");
    s = s.replaceAll ("([^\\\\])'", "$1\\\\'");// needed for directly
    // sequenced quotes
    s = s.replaceAll ("([^\\\\])'", "$1\\\\'");
    s = s.replaceAll ("'", "''");// quotes in the beginning
    // if (s.length()>255) s = s.substring(0,254);
    // s = s.replaceAll(";","\\;");
    return s;
  }

  private static final String LOAD_IMPORT_STATEMENT_DB2 = "LOAD FROM '%s' OF DEL %s INSERT INTO %s %s";

  private void importCSVDerbyDB2 (String schema, String tableName, String columnDefinitions,
    String sourceTargetColumnMapping, String sourceColumnChoice, File sourceFile, boolean dropIfExists, int chunkSize) throws SQLException, IOException
  {
    // createConnection ();
    // parameter touch-up
    if (!"NULL".equalsIgnoreCase (sourceColumnChoice)) {
      sourceColumnChoice = "'" + sourceColumnChoice.toUpperCase () + "'";
    }
    if (!"NULL".equalsIgnoreCase (sourceTargetColumnMapping)) {
      sourceTargetColumnMapping = "'" + sourceTargetColumnMapping.toUpperCase () + "'";
    }
    // drop table if exists
    if (tableExsists (tableName)) {
      if (dropIfExists) {
        writeToDB (String.format ("DROP TABLE %s.%s", schema.toUpperCase (), tableName.toUpperCase ()), tableName);
        String query = String.format ("CREATE TABLE %s.%s (%s )", schema, tableName, columnDefinitions);
        // System.out.println(query);
        writeToDB (query, tableName);
      }
    }
    else {
      String query = String.format ("CREATE TABLE %s.%s (%s )", schema, tableName, columnDefinitions);
      // System.out.println(query );
      writeToDB (query, tableName);
    }
    // chunked loading
    int overallLineCount = 0;
    BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (sourceFile),
      GroupByNewProcessor.ENCODING));

    File tempFile = FileUtils.createValidatedFile (sourceFile.getAbsolutePath () + "-importcsv-temp.csv");

    while (br.ready ()) {// produce chunks while the file is not done
      int lineCount = 0;
      BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (tempFile),
        GroupByNewProcessor.ENCODING));
      // write chunk
      while (br.ready () && lineCount < chunkSize) {// process one chunk
        bw.write (br.readLine () + "\n");
        lineCount++;
      }
      bw.close ();
      // import chunk
      if (isDerby ()) {
        String query;
        query = String.format ("CALL SYSCS_UTIL.SYSCS_IMPORT_DATA ('%s', '%s',%s,%s,'%s',null, null,'%s', 0)",
          schema.toUpperCase (), tableName.toUpperCase (), sourceTargetColumnMapping, sourceColumnChoice,
          tempFile.getAbsolutePath (), GroupByNewProcessor.ENCODING);
        // System.out.println(query);
        writeToDB (query, tableName);
      }
      else {// DB2
        if (sourceColumnChoice.equalsIgnoreCase ("'NULL'")) {
          sourceColumnChoice = String.format (" METHOD P (%s) ", sourceColumnChoice);
        }
        else {
          sourceColumnChoice = " ";
        }
        if (sourceTargetColumnMapping.equalsIgnoreCase ("'NULL'")) {
          sourceTargetColumnMapping = String.format (" (%s) ", sourceTargetColumnMapping);
        }
        else {
          sourceTargetColumnMapping = " ";
        }
        // NOTE: the below requires DB2_FORCE_APP_ON_MAX_LOG to be set
        // to true (non-default) to avoid problems with log file
        // overflow (Error 964)
        // command: db2set DB2_FORCE_APP_ON_MAX_LOG=true
        // cf.
        // http://publib.boulder.ibm.com/infocenter/db2luw/v9/index.jsp?topic=/com.ibm.db2.udb.apdv.samptop.doc/doc/r0007620.htm
        String query = String.format (LOAD_IMPORT_STATEMENT_DB2, sqlEscape (sourceFile.getAbsolutePath ()),
          sourceColumnChoice, tableName, sourceTargetColumnMapping);
        runDB2Command (query);
      }

      overallLineCount += lineCount;
    }
    logger.info (String.format ("Imported %d lines in chunks of %d", overallLineCount, chunkSize));
    br.close ();
    tempFile.delete ();
  }

  public void runDB2Command (String command) throws SQLException
  {
    logger.info (command);
    try {
      String sql = "CALL SYSPROC.ADMIN_CMD(?)";
      CallableStatement callStmt1 = this.conn.prepareCall (sql);
      callStmt1.setString (1, command);
      callStmt1.execute ();
    }
    catch (Exception e) {
      e.printStackTrace ();
      logger.info ("===========\n" + e.getMessage () + "\n===========");
    }
  }

  private boolean isDerby ()
  {
    return dburl.startsWith ("jdbc:derby");
  }

  private boolean isMySQL ()
  {
    return dburl.startsWith ("jdbc:mysql");
  }

  private boolean isDB2 ()
  {
    return dburl.startsWith ("jdbc:db2");
  }

  /**
   * Exports a given query into a file of the specified name.
   * 
   * @param query
   * @param fileName
   * @throws SQLException
   */
  public void exportQuery (String query, String fileName) throws SQLException
  {
    // delete file, if present (Derby will complain otherwise)
    (FileUtils.createValidatedFile (fileName)).delete ();
    // run export
    if (isDerby ()) {
      writeToDB (String.format ("CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY('%s','%s',null,null,'%s')", query, fileName,
        GroupByNewProcessor.ENCODING), null);
    }
    else {
      if (isDB2 ()) {
        String command = String.format ("EXPORT to '%s' of DEL %s ", sqlEscape (fileName), query);
        runDB2Command (command);
      }
      else {
        writeToDB (query + " INTO OUTFILE '" + sqlEscape (fileName) + "'" + CSV_FORMAT, null);
      }
    }
  }

  public void queryToNewTable (String schema, String targetTable, String columnDefinitions, String query) throws SQLException, IOException
  {
    queryToNewTable (schema, targetTable, columnDefinitions, query, true);
  }

  public void queryToNewTable (String schema, String targetTable, String columnDefinitions, String query, boolean delete) throws SQLException, IOException
  {
    // invalidate all columns.
    invalidateIndices (targetTable);
    File tempFile = FileUtils.createValidatedFile (targetTable + "-temp.csv");
    tempFile.delete ();
    exportQuery (query, tempFile.getAbsolutePath ());
    importCSV (schema, targetTable, columnDefinitions, "NULL", "NULL", tempFile, true);
    if (delete) tempFile.delete ();
  }

  public File queryToInsert (String query, String schema, String table, String columnDefs, boolean dropIfExists) throws SQLException, IOException
  {
    // invalidate all columns.
    invalidateIndices (table);
    File tempFile = FileUtils.createValidatedFile (table + "-temp.csv");
    tempFile.delete ();
    // logger.info(table+"-exporting: "+query);
    logger.info (table + "-exporting: " + query);
    this.exportQuery (query, tempFile.getAbsolutePath ());
    // logger.info(table+"-importing "+(tempFile.length()/1024)+" Kbytes");
    logger.info (table + "-importing " + (tempFile.length () / 1024) + " Kbytes");
    // load csv
    this.importCSV (schema, table, columnDefs, "NULL", "NULL", tempFile, dropIfExists);
    return tempFile;
  }

  /**
   * Trigers index (re-)creation if the index does not yet exist or has been invalidated If an up to date index exists
   * that covers all the mentioned columns and would be appropriate for use with those columns the method does nothing.
   * In order to be appropriate, the columns need to appear in the index specification in the given order as the first
   * columns. The state of the indices are stored in this object. Thus, all external changes are not reflected.
   * 
   * @param table
   */

  HashMap<String, Collection<List<String>>> validIndices = new HashMap<String, Collection<List<String>>> ();

  public void requireIndex (String table, boolean desc, String... columns) throws SQLException
  {
    if (validIndices.get (table) == null) {
      validIndices.put (table, new HashSet<List<String>> ());
    }
    if (table.equalsIgnoreCase ("co_inst")) {
      System.out.println ("co_inst");
    }
    boolean found = false;
    for (List<String> indexSpec : validIndices.get (table)) {
      if (indexSpec.subList (0, columns.length - 1).containsAll (Arrays.asList (columns))) {
        found = true;
        break;
      }
    }
    if (!found) {
      String columnNames = "";

      validIndices.get (table).add (Arrays.asList (columns));
      for (String columnName : columns) {
        if (columnNames.length () > 0) columnNames += ",";
        columnNames += columnName;
      }
      // delete index if existing
      dropSimpleIndex (table, columnNames);
      // create new
      createSimpleIndex (table, columnNames);
    }
  }

  /**
   * Invalidates all indices that contain any of the columns given. If no columns are specified, all indices of this
   * table are invalidated.
   * 
   * @param table
   * @param columns
   */
  public void invalidateIndices (String table, String... columns)
  {
    Collection<List<String>> indexSpecs = validIndices.get (table);
    if (indexSpecs == null) return;
    if (columns.length == 0) {
      validIndices.remove (table);
    }
    else {
      outer: for (Iterator<List<String>> iterator = indexSpecs.iterator (); iterator.hasNext ();) {
        List<String> list = (List<String>) iterator.next ();
        for (String column : columns) {
          if (list.contains (column)) {
            iterator.remove ();
            continue outer;
          }
        }
      }
    }
  }

  /**
   * Creates an index on a single column called $tableName_$column_index. If an index of that name previously existed it
   * will be dropped first.
   * 
   * @param tableName
   * @param column
   * @throws SQLException
   */
  @SuppressWarnings("unused")
  private void reCreateSimpleIndex (String tableName, String column) throws SQLException
  {
    dropSimpleIndex (tableName, column);
    createSimpleIndex (tableName, column);
  }

  public void dropSimpleIndex (String tableName, String column) throws SQLException
  {
    try {
      if (isDerby ()) {
        writeToDB (String.format ("drop index %s_%s_index", tableName, column.replace (',', '_')), tableName,
          column.split (","));
      }
      else {
        writeToDB (String.format ("drop index %s_%s_index on %s", tableName, column.replace (',', '_'), tableName),
          tableName, column.split (","));
      }

    }
    catch (SQLException e) {
      // probably didn't exist in the first place
    }
  }

  private void createSimpleIndex (String tableName, String column) throws SQLException
  {
    String query = String.format ("create index %s_%s_index on %s(%s)", tableName, column.replace (',', '_'),
      tableName, column);
    logger.info (query);
    writeToDB (query, null);

  }

  public String getDburl ()
  {
    return dburl;
  }

  public void reconnect ()
  {
    createConnection ();

  }

  public void setLogger (IPDLog logger)
  {
    this.logger = logger;
  }

  public IPDLog getLogger ()
  {
    return logger;
  }

  public void printQueryResult (String query)
  {
    try {
      ResultSet result = readFromDB (query);
      int numCols = result.getMetaData ().getColumnCount ();

      System.out.println ("Columns:");
      for (int i = 1; i <= numCols; i++) {
        System.out.print (result.getMetaData ().getColumnLabel (i) + "-->");
      }
      System.out.println ("\nData:");
      while (result.next ()) {
        for (int i = 1; i <= numCols; i++) {
          System.out.print (result.getString (i) + "-->");
        }
        System.out.println ("");
      }
    }
    catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }

  }

}
