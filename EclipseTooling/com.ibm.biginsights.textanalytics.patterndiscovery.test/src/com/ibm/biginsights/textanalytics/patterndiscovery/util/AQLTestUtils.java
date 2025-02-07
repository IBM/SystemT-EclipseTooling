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
package com.ibm.biginsights.textanalytics.patterndiscovery.util;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import au.com.bytecode.opencsv.CSVReader;

public class AQLTestUtils
{

  // The LW data path relative to the installation directory of the plugin
  public static final String lwDataPathName = "regression/languageware";
  // LW desc file name relative to the LW data path
  public static final String lwDescFileName = "LanguageWare-7.1.1.4/langware.xml";

  /**
   * @return
   */
  public static File getDefaultLWConfigFile ()
  {
    return new File (getDefaultLWDataPath (), lwDescFileName);
  }

  /**
   * @return
   */
  public static File getDefaultLWDataPath ()
  {
    return new File (lwDataPathName);
  }

  /**
   * given two result sets it make sure that they contain the same metadata and records
   * 
   * @param expected
   * @param actual
   * @return
   * @throws SQLException
   */
  public static boolean comparteResulSets (ResultSet expected, ResultSet actual) throws SQLException
  {
    ResultSetMetaData expected_meta = expected.getMetaData ();
    ResultSetMetaData actual_meta = actual.getMetaData ();

    // == check they have the same number of columns ==
    if (expected_meta.getColumnCount () != actual_meta.getColumnCount ()) return false;

    // == check they have the same columns ==
    for (int i = 1; i <= expected_meta.getColumnCount (); i++) {
      if (!expected_meta.getColumnLabel (i).equals (actual_meta.getColumnLabel (i))) return false;
      if (expected_meta.getColumnType (i) != actual_meta.getColumnType (i)) return false;
    }
    // == check the values in the tables ==
    while (expected.next () && actual.next ()) {
      for (int i = 1; i <= expected_meta.getColumnCount (); i++) {
        if (!expected.getString (i).equals (actual.getString (i))) return false;
      }
    }

    // == check none of them has more rows ==
    if (expected.next () || actual.next ()) return false;

    return true;
  }

  /**
   * @param expected
   * @param actual
   * @param cols_to_compare
   * @return
   * @throws IOException
   */
  public static boolean compareCSVFiles (Reader expected, Reader actual, int[] cols_to_compare) throws IOException
  {
    CSVReader expected_r = new CSVReader (expected);
    CSVReader actual_r = new CSVReader (actual);

    String[] exp_row, act_row;

    exp_row = expected_r.readNext ();
    act_row = actual_r.readNext ();

    // == compare the values at the specified indices of each row ==
    while (exp_row != null && act_row != null) {
      for (int i : cols_to_compare) {
        if (!exp_row[i].equals (act_row[i])) return false;
      }
      exp_row = expected_r.readNext ();
      act_row = actual_r.readNext ();
    }

    // == check that both files have the same number of rows ==
    if (exp_row != null | act_row != null) return false;

    expected_r.close ();
    actual_r.close ();

    return true;
  }
}
