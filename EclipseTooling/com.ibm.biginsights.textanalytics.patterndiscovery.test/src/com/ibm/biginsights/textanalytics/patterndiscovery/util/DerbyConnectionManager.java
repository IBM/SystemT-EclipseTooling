package com.ibm.biginsights.textanalytics.patterndiscovery.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DerbyConnectionManager
{
  public static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
  public static final String PROTOCOL = "jdbc:derby:";
  public static final String SHUTDOWN = ";shutdown=true";
  public static final String STARTUP = ";create=%s";
  public static final String DB_NAME = "%s";
  public static final String CONNECTION_URL = PROTOCOL + DB_NAME + STARTUP;

  private Connection conn;
  private Logger logger;

  public DerbyConnectionManager (String db_location, boolean create)
  {
    logger = Logger.getLogger (DerbyConnectionManager.class.getName ());
    try {
      Class.forName (DRIVER).newInstance ();
      conn = DriverManager.getConnection (String.format (CONNECTION_URL, db_location, create));
      logger.info ("created connection : " + conn.toString ());
    }
    catch (Exception e) {
      e.printStackTrace ();
    }
  }

  /**
   * shutdown the specified database
   * 
   * @param dbname
   * @throws SQLException
   */
  public void shutdown (String dbname) throws SQLException
  {
    DriverManager.getConnection (String.format ("%s%s%s", PROTOCOL, dbname, SHUTDOWN));
    System.gc ();
    logger.info ("Successfully shutted down db : " + dbname);
  }

  /**
   * shutdown all databases
   * 
   * @throws SQLException
   */
  public void shutdown ()
  {
    try {
      DriverManager.getConnection (String.format ("%s%s", PROTOCOL, SHUTDOWN));
    }
    catch (Exception e) {
      // this always happens
    }
    finally {
      System.gc ();
      logger.info ("Successfully shutted down connection");
    }
  }

  /**
   * close this connection
   * 
   * @throws SQLException
   */
  public void close () throws SQLException
  {
    conn.close ();
    logger.info ("Successfully closed connection");
  }

  /**
   * execute this sql query against the database. Please make sure that you call rs.close() after reading the results
   * 
   * @param sql
   * @return
   * @throws SQLException
   */
  public ResultSet executeQuery (String sql) throws SQLException
  {
    logger.info ("executeQuery : " + sql);
    Statement st = conn.createStatement ();
    return st.executeQuery (sql);
  }

  /**
   * execute this insert, update, delete sql against the database and return the number of rows affected
   * 
   * @param sql
   * @return
   * @throws Exception
   */
  public int create (String sql) throws Exception
  {
    logger.info ("create : " + sql);
    Statement st = conn.createStatement ();
    try {
      int m = st.executeUpdate (sql);
      return m;
    }
    catch (Exception e) {
      conn.rollback ();
      logger.info ("create failed");
      throw e;
    }
  }

  /**
   * execute the given sql
   * 
   * @param sql
   * @return
   * @throws Exception
   */
  public boolean execute (String sql) throws Exception
  {
    logger.info ("execute : " + sql);
    Statement st = conn.createStatement ();
    try {
      boolean m = st.execute (sql);
      return m;
    }
    catch (Exception e) {
      conn.rollback ();
      logger.info ("execute failed");
      throw e;
    }
  }

  /**
   * @param stmt
   * @return
   * @throws Exception
   */
  public boolean executePreparedStatement (PreparedStatement stmt) throws Exception
  {
    logger.info ("executePreparedStatement : " + stmt);
    try {
      boolean m = stmt.execute ();
      return m;
    }
    catch (Exception e) {
      conn.rollback ();
      logger.info ("executePreparedStatement failed");
      throw e;
    }
  }

  /**
   * @param stmt
   * @return
   * @throws Exception
   */
  public ResultSet executeQueryPreparedStatement (PreparedStatement stmt) throws Exception
  {
    logger.info ("executeQueryPreparedStatement : " + stmt);
    try {
      ResultSet m = stmt.executeQuery ();
      return m;
    }
    catch (Exception e) {
      conn.rollback ();
      logger.info ("executeQueryPreparedStatement failed");
      throw e;
    }
  }

  /**
   * creates a prepared statement for the given sql
   * 
   * @param sql
   * @return
   * @throws SQLException
   */
  public PreparedStatement createPreparedStatement (String sql) throws SQLException
  {
    logger.info ("createPreparedStatement : " + sql);
    return conn.prepareStatement (sql);
  }

  public PreparedStatement createPreparedStatementGetKey (String sql) throws SQLException
  {
    logger.info ("createPreparedStatementGetKey : " + sql);
    return conn.prepareStatement (sql, Statement.RETURN_GENERATED_KEYS);
  }

  /**
   * check if the connection is alive
   * 
   * @return
   * @throws SQLException
   */
  public boolean isAlive () throws SQLException
  {
    if (conn == null || conn.isClosed ()) return false;
    return true;
  }

}
