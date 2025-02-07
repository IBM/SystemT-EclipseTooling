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
 * export only the bubbles currentlty being displayed
 * 
 * 
 */
public class ExportCurrent extends ExportToCSV
{


  
	public static final String ACTION_ID = "com.ibm.biginsights.textanalytics.patterndiscovery.export.exportcurrent";

  public ExportCurrent (PatternDiscoveryJob pdjob)
  {
    super (pdjob);
    // TODO Auto-generated constructor stub
  }

  /**
   * @see ExportToCSV
   */
  @Override
  protected void initActionProperties ()
  {
    setText ("Export Current"); // FIXME create constant
    setToolTipText ("Export the current patterns displayed"); // FIXME create constant
    setImageDescriptor (Activator.getImageDescriptor ("exportCurPattern.gif"));
  }

  /**
   * @see ExportToCSV
   */
  @Override
  protected void loadBubblesModel () throws SQLException
  {
    bubbles = new ArrayList<BubbleCSVRowModel> ();

    String query = String.format (
      "SELECT T.COUNT, T.JSEQUENCE, T.SEQUENCE, T.GROUPBYCONTEXT, G.DOCID FROM %s AS T LEFT JOIN %s AS G ON T.ID = G.ID WHERE T.JSEQUENCE != '}' AND T.COUNT >= %d AND T.COUNT <= %d ORDER BY T.COUNT DESC",
      pdjob.getTableName (), pdjob.getTypeTableName (), pdjob.getMin (), pdjob.getMax ());

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
