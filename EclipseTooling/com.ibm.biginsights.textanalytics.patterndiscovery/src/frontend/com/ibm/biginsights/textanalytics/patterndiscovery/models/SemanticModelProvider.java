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
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;

/**
 * provider for the semantic contexts
 */
public class SemanticModelProvider
{



  private List<SemanticContext> snippets;
  private PatternDiscoveryJob job;

  public SemanticModelProvider (PatternDiscoveryJob job)
  {
    this.job = job;
    snippets = new ArrayList<SemanticContext> ();
  }

  public List<SemanticContext> getSnippets (String context, String jsignature, String signature)
  {
    getTable (context, jsignature, signature);
    return snippets;
  }

  private String getCleanEntities (String original, String viewname)
  {
    String ret = "";

    String[] allEntities = original.split (",");

    for (String entity : allEntities) {
      String parts[] = entity.split (PDConstants.VIEW_SPAN_SEPARATOR);
      if (parts.length > 1) {
        if (parts[0].trim ().equals (viewname)) {
          ret += String.format (", %s", parts[1]);
        }
      }
    }

    // ret = ret.trim();
    // if(ret.charAt(ret.length()-1) == ','){
    // ret = ret.substring(0, ret.length());
    // }
    //
    // if(!ret.isEmpty())
    // ret = "," + ret;

    return ret;
  }

  public void getTable (String context, String jsignature, String signature)
  {

    ResultSet result = null;

    try {
      String entity = job.getProperty (Messages.ENTITY_FIELD_NAMES_PROP);
      String tableName = job.getTableName ();
      String viewname = DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME;
      String snippetField = job.getProperty (Messages.SNIPPET_FIELD_NAME_PROP);

      entity = getCleanEntities (entity, viewname);

      String sql = String.format ("select %s%s " + "from %s " + "where jsequence='%s'  " + "and sequence='%s' "
        + "and %s='%s' " + "group by jsequence, " + "sequence, %s, %s, id%s " + "order by %s%s", snippetField, entity,
        tableName, jsignature.replaceAll ("'", "''"), signature.replaceAll ("'", "''"), viewname,
        context.replaceAll ("'", "''"), viewname, snippetField, entity, snippetField, entity);

      // System.err.println(String.format("SQL : %s", sql));

      result = job.readFromDB (sql);

      ResultSetMetaData rsmd = result.getMetaData ();
      int numOfCol = rsmd.getColumnCount ();

      // int row = 0;

      while (result.next ()) {

        String snippet = "";
        String phone = "";

        for (int j = 0; j < numOfCol; j++) {
          // System.out.print(result.getString(j + 1));

          if (j == 0)
            snippet = result.getString (j + 1);
          else if (j == 1) phone = result.getString (j + 1);
        }

        snippets.add (new SemanticContext (snippet, phone));

        // row++;
      }

      result.close ();
      job.shutDownDB ();
    }
    catch (SQLException e) {
      ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_READING_DATA_FROM_DB, e);
    }

  }
}
