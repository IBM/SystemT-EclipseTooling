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

import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ExampleNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * adds a example from a selection to a given label
 * 
 * 
 */
public class AddExampleToLabelAction extends Action
{



  protected ActionPlanView plan;
  protected ExampleModel model;
  protected LabelNode node;

  public AddExampleToLabelAction (ExampleModel model, LabelNode node, ActionPlanView plan)
  {
    this.model = model;
    this.node = node;
    this.plan = plan;

    this.setText (node.getLabel ());
    this.setImageDescriptor (ImageDescriptor.createFromImage (Icons.LABEL_ICON));
  }

  public void run ()
  {
    if (model != null) {
      ExampleNode enode = new ExampleNode (model);
      node.addExample2 (enode);

      plan.serializeAndExpand (enode, TreeViewer.ALL_LEVELS);
    }
  }
}
