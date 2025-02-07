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

package com.ibm.biginsights.textanalytics.patterndiscovery.controlers;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.jface.action.Action;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DiscoveryConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.RuleApplied;
import com.ibm.biginsights.textanalytics.patterndiscovery.ruleshistory.RulesHistoryJob;

/**
 * handle the action of the user trying to see the history of patterns dropped for a given bubble
 * 
 * 
 */
public class SeePatternHistoryListener extends Action implements MouseListener
{



  String signature;
  PatternDiscoveryJob job;

  public SeePatternHistoryListener (String signature, PatternDiscoveryJob job)
  {
    this.job = job;
    this.signature = signature;
  }

  /**
   * when a user try to see the history for a given bubble we load all the sequences that currently have the same
   * signature that it. for each of these rules we recursively load its previous signature before applying a drop rule.
   */
  @Override
  public void run ()
  {
    loadRules (signature);
  }

  @Override
  public void mouseClicked (MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseEntered (MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseExited (MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void mousePressed (MouseEvent e)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void mouseReleased (MouseEvent e)
  {
    run ();
  }

  public void loadRules (String signature)
  {
    RuleApplied root = new RuleApplied (signature, null);

    try {
      recursivelyLoadAllRulesApplied (root);

      if (root.getChildren ().size () < 1) root = null;
      RulesHistoryJob historyjob = new RulesHistoryJob ("Rules History", root);
      historyjob.schedule ();
    }
    catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }

  }

  /**
   * recursively loads all the rules that have been applied until we got to the RuleApplied as parameter
   * 
   * @param ra
   * @throws SQLException
   */
  private void recursivelyLoadAllRulesApplied (RuleApplied ra) throws SQLException
  {
    String sql = String.format ("SELECT %s, %s FROM %s WHERE %s = '%s' and %s != %s group by %s, %s",
      DiscoveryConstants.RULE_AS_STR_COL_NAME, DiscoveryConstants.SEQ_BEFORE_RULE_COL_NAME,
      DiscoveryConstants.RULESHISTORY_TBL_NAME, DiscoveryConstants.SEQ_AFTER_RULE_COL_NAME, ra.getSignature (),
      DiscoveryConstants.SEQ_BEFORE_RULE_COL_NAME, DiscoveryConstants.SEQ_AFTER_RULE_COL_NAME,
      DiscoveryConstants.RULE_AS_STR_COL_NAME, DiscoveryConstants.SEQ_BEFORE_RULE_COL_NAME);

    ResultSet result = job.readFromDB (sql);

    while (result.next ()) {

      // String context = result.getString (1);
      String ruleString = result.getString (DiscoveryConstants.RULE_AS_STR_COL_NAME);
      String before = result.getString (DiscoveryConstants.SEQ_BEFORE_RULE_COL_NAME);
      // String after = result.getString (4);

      RuleApplied child = new RuleApplied (before, ruleString);

      if (ra.getChildren ().add (child)) {
        recursivelyLoadAllRulesApplied (child);
      }
    }
  }

}
