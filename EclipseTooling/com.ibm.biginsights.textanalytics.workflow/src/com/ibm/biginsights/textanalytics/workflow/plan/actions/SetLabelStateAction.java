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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;

import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action to set the state of a label. we can set the state of a label either to working or done. this is helpful in
 * order to know which nodes need work to be completed
 * 
 * 
 */
public class SetLabelStateAction extends Action
{



  protected boolean state;
  protected LabelNode node;
  private ActionPlanView plan;

  public SetLabelStateAction (boolean state, LabelNode node, ActionPlanView plan)
  {
    super ();
    this.state = state;
    this.node = node;
    this.plan = plan;

    String text = (state) ? Messages.label_done_message : Messages.label_working_message;
    setText (text);
    if (state)
      setImageDescriptor (ImageDescriptor.createFromImage (Icons.DONE_ICON));
    else
      setImageDescriptor (ImageDescriptor.createFromImage (Icons.SWITCH_ICON));
  }

  public void run ()
  {
    node.setDone (state);
    node.initIconFromType ();
    plan.serializeAndCollapse (node, TreeViewer.ALL_LEVELS);
  }

}
