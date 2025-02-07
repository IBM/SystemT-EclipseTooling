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
package com.ibm.biginsights.textanalytics.patterndiscovery.single;

import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Assert;

import com.ibm.biginsights.textanalytics.patterndiscovery.util.AQLTestUtils;
import com.ibm.biginsights.textanalytics.patterndiscovery.util.DbNames;
import com.ibm.biginsights.textanalytics.patterndiscovery.util.DerbyConnectionManager;
import com.ibm.biginsights.textanalytics.util.log.ILog;

public abstract class DbBasedTest implements PatternDiscoveryTest
{

  ILog logger;

  DerbyConnectionManager actual_conn, expected_conn;
  protected String[] tables_to_test;

  public DbBasedTest (String expected_path, String actual_path, String db_folder_name)
  {
    expected_conn = new DerbyConnectionManager (expected_path + "/" + db_folder_name, false);
    actual_conn = new DerbyConnectionManager (actual_path + "/" + db_folder_name, false);
    initLogger ();
  }

  /**
   * @throws Exception
   */
  public void runTest () throws Exception
  {
    for (String tableName : tables_to_test) {
      String sql = "select * from " + tableName;
      test (sql);
    }

    shutdown ();
  }

  /**
   * make sure we close the db connections when we are done
   */
  public void shutdown ()
  {
    expected_conn.shutdown ();
    actual_conn.shutdown ();
  }

  /**
   * before we test anything with the compare results set method, we first must verify that it actually works. we do so
   * by calling it with the same result set twice we will do this only once here in the first test
   * 
   * @throws Exception
   */
  public void test_compare_method () throws Exception
  {
    String sql = "select * from " + DbNames.DICTIONARY;

    try {
      ResultSet expected = expected_conn.executeQuery (sql);
      ResultSet expected_copy = expected_conn.executeQuery (sql);

      Assert.assertTrue (AQLTestUtils.comparteResulSets (expected, expected_copy));
      expected.close ();
      expected_copy.close ();
    }
    catch (SQLException e) {
      shutdown ();
      throw e;
    }
  }

  /**
   * given a sql query this method will load the results from both db and the compare the results
   * 
   * @param sql
   * @throws Exception
   */
  public void test (String sql) throws Exception
  {
    try {
      ResultSet actual = actual_conn.executeQuery (sql);
      ResultSet expected = expected_conn.executeQuery (sql);

      Assert.assertTrue (AQLTestUtils.comparteResulSets (expected, actual));

      actual.close ();
      expected.close ();
    }
    catch (SQLException e) {
      logger.logInfo ("test failed. SQL : " + sql);
      shutdown ();
      throw e;
    }
  }

  @Override
  public void test (String path1, String path2, int[] cols_compare) throws Exception
  {
    throw new Exception ("Method Not Used");
  }

  public abstract void initLogger ();

}
