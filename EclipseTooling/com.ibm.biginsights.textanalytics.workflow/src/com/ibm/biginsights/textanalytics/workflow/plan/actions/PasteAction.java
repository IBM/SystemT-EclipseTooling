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
package com.ibm.biginsights.textanalytics.workflow.plan.actions;

import org.eclipse.jface.action.Action;

import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ExampleNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ExamplesFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelsFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeParent;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;

/**
 * paste action for the extraction plan.
 * 
 * 
 */
public class PasteAction extends Action
{


 
	private TreeObject elementToPaste;
  private TreeObject whereToPaste;

  public PasteAction (TreeObject elementToPaste, TreeObject whereToPaste)
  {
    this.elementToPaste = elementToPaste;
    this.whereToPaste = whereToPaste;
  }

  /**
   * based in the location to be pasted and the current element to be pasted we handle this.
   */
  public void run ()
  {
    ActionPlanView plan = AqlProjectUtils.getActionPlanView ();
    if (plan == null || !plan.ready ()) return;

    if (whereToPaste instanceof TreeParent) {

      //-------- Label --------//
      if (elementToPaste instanceof LabelNode) {
        LabelNode targetLabel = null;
        AqlGroupType grpType = null;

        // Label to Label
        if (whereToPaste instanceof LabelNode) {
          targetLabel = (LabelNode)whereToPaste;
        }
        // Label to Label folder
        if (whereToPaste instanceof LabelsFolderNode) {
          targetLabel = ((LabelsFolderNode)whereToPaste).getParentLabelNode ();
          grpType = ((LabelsFolderNode)whereToPaste).getAqlType ();
        }
        // Label to AQL group
        if (whereToPaste instanceof AqlGroup) {
          targetLabel = ((AqlGroup)whereToPaste).getParentLabelNode ();
          grpType = ((AqlGroup)whereToPaste).getAqlType ();
        }

        if (targetLabel != null)
          targetLabel.addSubLabel ((LabelNode)elementToPaste, grpType);
      }

      //-------- Example --------//
      else if (elementToPaste instanceof ExampleNode) {
        // example to Label node
        if (whereToPaste instanceof LabelNode)
          ((LabelNode) whereToPaste).addExample ((ExampleNode) elementToPaste);

        // example to Examples folder
        else if (whereToPaste instanceof ExamplesFolderNode)
          ((ExamplesFolderNode)whereToPaste).addChild2 ((ExampleNode)elementToPaste);
      }

      //-------- AQL element --------//
      else if (elementToPaste instanceof AqlNode) {
        TreeObject target = whereToPaste;

        if (target instanceof AqlFolderNode)
          target = (AqlGroup)target.getParent ();

        if (target instanceof LabelNode) {
          AqlGroupType grpType = ((AqlNode)elementToPaste).getAqlGroup ();
          target = ((LabelNode)target).getAqlGroup (grpType);
        }

        if (target instanceof AqlGroup)
          ((AqlGroup) target).addChild2 (elementToPaste);
      }
    }

    plan.serializeAndRefresh ();
  }

}
