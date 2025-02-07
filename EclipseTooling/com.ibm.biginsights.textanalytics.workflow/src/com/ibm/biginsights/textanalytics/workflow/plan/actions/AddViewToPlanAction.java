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

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action to add a view to the action plan
 * 
 * 
 */
public class AddViewToPlanAction extends Action
{



  protected String viewName;
  protected AqlGroupType type;
  protected LabelNode node;
  protected AqlGroup group;
  protected METHOD method;

  /**
   * defines that ways that the view can be added
   * 
   * 
   */
  protected enum METHOD
  {
    ADD_TO_LABELNODE, ADD_TO_AQLGROUP
  };

  /**
   * Constructor that identifies the
   * 
   * @param viewName name of the view to be created
   * @param type the group type of the view being created
   * @param node the parent label for this view
   */
  public AddViewToPlanAction (String viewName, AqlGroupType type, LabelNode node)
  {
    this.viewName = viewName;
    this.type = type;
    this.node = node;

    setText (getLabel ());
    setImageDescriptor (getIcon ());
    method = METHOD.ADD_TO_LABELNODE;
  }

  /**
   * Constructor ...
   * 
   * @param viewName
   * @param group
   */
  public AddViewToPlanAction (String viewName, AqlGroup group)
  {

    this.viewName = viewName;
    this.group = group;
    this.type = group.getAqlType ();

    setText (getLabel ());
    setImageDescriptor (getIcon ());
    method = METHOD.ADD_TO_AQLGROUP;
  }

  @Override
  public void run ()
  {
    ActionPlanView plan = AqlProjectUtils.getActionPlanView ();
    if (plan == null || !plan.ready ()) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready);
      return;
    }

    switch (method) {
      case ADD_TO_LABELNODE:
        runCase1 ();
      break;
      case ADD_TO_AQLGROUP:
        runCase2 ();
      break;
    }

    plan.serializeAndRefresh ();
  }

  /**
   * in this case we know the label node that we are adding the view so based on the type of the view we add it to it
   */
  private void runCase1 ()
  {
    AqlNode aqlNode = new AqlNode (viewName);
    switch (type) {
      case BASIC:
        node.addBasicFeature (aqlNode);
      break;

      case CONCEPT:
        node.addConcept (aqlNode);
      break;

      case REFINEMENT:
        node.addRefinement (aqlNode);
      break;
    }
  }

  /**
   * in this case we know the exact group that we are adding this view to, so we just add it as a child of that group
   */
  private void runCase2 ()
  {
    AqlNode aqlNode = new AqlNode (viewName);
    group.addChild (aqlNode);
  }

  private String getLabel ()
  {
    switch (type) {
      case BASIC:
        return Messages.add_view_basic_message;

      case CONCEPT:
        return Messages.add_view_candidate_message;

      case REFINEMENT:
        return Messages.add_view_refinement_message;
    }

    return Messages.add_view_default_message;
  }

  private ImageDescriptor getIcon ()
  {
    switch (type) {
      case BASIC:
        return ImageDescriptor.createFromImage (Icons.BASIC_FEATURES_ICON);

      case CONCEPT:
        return ImageDescriptor.createFromImage (Icons.CONCEPTS_ICON);

      case REFINEMENT:
        return ImageDescriptor.createFromImage (Icons.REFINEMENT_ICON);
    }
    return null;
  }
}
