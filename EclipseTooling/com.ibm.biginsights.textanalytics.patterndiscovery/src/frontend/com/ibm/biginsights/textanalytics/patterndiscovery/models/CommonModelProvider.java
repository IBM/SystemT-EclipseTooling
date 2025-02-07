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
package com.ibm.biginsights.textanalytics.patterndiscovery.models;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DiscoveryConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;

/**
 * given a sequence this class will load all the elements in that match the same signature
 * 
 * 
 */
public class CommonModelProvider
{



  private List<CommonContext> contexts;
  private String jsequence = "";
  private PatternDiscoveryJob job;

  public CommonModelProvider (String jsequence, PatternDiscoveryJob job)
  {
    contexts = new ArrayList<CommonContext> ();
    this.jsequence = jsequence;
    this.job = job;
  }

  public List<CommonContext> getContexts ()
  {
    return contexts;
  }

  public String getJSignature ()
  {
    return jsequence;
  }

  public void setJsequence (String jsequence)
  {
    this.jsequence = jsequence;
  }

  /**
   * loads the elements from the db
   * 
   * @param jsequence
   */
  public void getTable (String jsequence)
  {
    setJsequence (jsequence);

    // System.err.println(jsequence);

    contexts.clear ();
    ResultSet result = null;

    try {

      String query = "select count(*)," + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME + ", sequence from "
        + job.getTableName () + " where jsequence='" + jsequence.replaceAll ("'", "''") + "' group by "
        + DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME + ", sequence order by sequence desc, count(*) desc";

      result = job.readFromDB (query);

      ResultSetMetaData rsmd = result.getMetaData ();
      int numOfCol = rsmd.getColumnCount ();

      // int row = 0;
      while (result.next ()) {
        String contextString = "";
        String contextCount = "";
        String signature = "";

        for (int j = 0; j < numOfCol; j++) {
          if (j == 0)
            contextCount = result.getString (j + 1);
          else if (j == 1)
            contextString = result.getString (j + 1);
          else if (j == 2) signature = result.getString (j + 1);
        }

        // row++;

        contexts.add (new CommonContext (contextString, Integer.valueOf (contextCount), signature));
      }

      result.close ();
      job.shutDownDB ();

    }
    catch (SQLException e) {
      ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_READING_DATA_FROM_DB, e);
    }
  }

}
