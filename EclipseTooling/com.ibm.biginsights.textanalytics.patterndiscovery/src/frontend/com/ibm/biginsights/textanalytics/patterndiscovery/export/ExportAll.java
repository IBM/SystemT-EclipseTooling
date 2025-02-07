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
package com.ibm.biginsights.textanalytics.patterndiscovery.export;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.ibm.biginsights.textanalytics.patterndiscovery.Activator;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;

/**
 * export all bubbles to a csv file
 * 
 *
 */
public class ExportAll extends ExportToCSV
{


  
	public static final String ACTION_ID = "com.ibm.biginsights.textanalytics.patterndiscovery.export.exportall";

  public ExportAll (PatternDiscoveryJob pdjob)
  {
    super (pdjob);
  }

  /**
   * init the parameters for the action defined by this class
   */
  @Override
  protected void initActionProperties ()
  {
    setText ("Export All"); // FIXME create constant
    setToolTipText ("Export all the patterns"); // FIXME create constant
    setImageDescriptor (Activator.getImageDescriptor ("exportAllPatterns.gif"));
  }

  /**
   * loads all the bubble models from the db
   */
  @Override
  protected void loadBubblesModel () throws SQLException
  {
    bubbles = new ArrayList<BubbleCSVRowModel> ();

    String query = String.format (
      "SELECT T.COUNT, T.JSEQUENCE, T.SEQUENCE, T.GROUPBYCONTEXT, G.DOCID FROM %s AS T LEFT JOIN %s AS G ON T.ID = G.ID WHERE T.JSEQUENCE != '}' ORDER BY T.COUNT DESC",
      pdjob.getTableName (), pdjob.getTypeTableName ());

    ResultSet result = pdjob.readFromDB (query);
    while (result.next ()) {
      String COUNT = result.getString ("COUNT");
      String JSEQUENCE = result.getString ("JSEQUENCE");
      String SEQUENCE = result.getString ("SEQUENCE");
      String GROUPBYCONTEXT = result.getString ("GROUPBYCONTEXT");
      String DOCID = result.getString ("DOCID");
      bubbles.add (new BubbleCSVRowModel (COUNT, JSEQUENCE, SEQUENCE, GROUPBYCONTEXT, DOCID));
    }
  }
}
