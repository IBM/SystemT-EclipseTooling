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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ExampleNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelsFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.NodesGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.LabelModel;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action that runs the wizard to create a new label
 * 
 * 
 */
public class AddLabelAction extends Action
{



  protected ActionPlanView plan;
  protected ExampleModel exampleModel;
  protected NodesGroup parent;
  protected String labelTitlte;
  protected boolean generateAQLs;

  public AddLabelAction (ActionPlanView plan, ExampleModel exampleText, LabelNode parent, String labelTitle,
    boolean generateAQLs)
  {
    this.plan = plan;
    this.exampleModel = exampleText;
    this.parent = parent;
    this.labelTitlte = labelTitle;
    this.generateAQLs = generateAQLs;

    setActionTextAndImage (Messages.add_label_text, Messages.add_label_tootltip, ImageDescriptor.createFromImage (Icons.LABEL_ICON));
  }

  /**
   * Create label for a AQL group type folder.
   */
  public AddLabelAction (ActionPlanView plan, AqlGroup parent, String labelTitle)
  {
    this.plan = plan;
    this.parent = parent;
    this.labelTitlte = labelTitle;

    // sets the text, tooltip, and image for this action
    String txt, toolTip;
    switch (parent.getAqlType ()) {
      case BASIC:
        txt = Messages.add_bf_label_text;
        toolTip = Messages.add_bf_label_tooltip;
        break;
      case CONCEPT:
        txt = Messages.add_cg_label_text;
        toolTip = Messages.add_cg_label_tooltip;
        break;
      case REFINEMENT:
        txt = Messages.add_fc_label_text;
        toolTip = Messages.add_fc_label_tooltip;
        break;
      case FINALS:
        txt = Messages.add_final_label_text;
        toolTip = Messages.add_final_label_tooltip;
        break;
      default:
        txt = "";
        toolTip = "";
    }
    setActionTextAndImage (txt, toolTip, ImageDescriptor.createFromImage (Icons.LABEL_ICON));
  }

  /**
   * Create label for a Labels folder.
   */
  public AddLabelAction (ActionPlanView plan, LabelsFolderNode parent, String labelTitle)
  {
    this.plan = plan;
    this.parent = parent;
    this.labelTitlte = labelTitle;

    // sets the text for this action
    setActionTextAndImage (Messages.add_label_text, Messages.add_label_tootltip, ImageDescriptor.createFromImage (Icons.LABEL_ICON));
  }

  public AddLabelAction (ActionPlanView plan)
  {
    this.plan = plan;

    // sets the text for this action
    setText (Messages.add_label_text);
    // set the tooltip for this action
    setToolTipText (Messages.add_label_tootltip);
    // set the icon for this action
    setImageDescriptor (ImageDescriptor.createFromImage (Icons.LABEL_ICON));
  }

  @Override
  public void run ()
  {
    // make sure that the action plan is properly initialized
    if (!plan.ready ()) return;

    if (ActionPlanView.projectName == null || ActionPlanView.projectName.isEmpty ()) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready);
      return;
    }

    //--------  create the label node  --------//
    LabelModel labelModel = new LabelModel ();
    labelModel.setName (labelTitlte);
    LabelNode node = new LabelNode (labelModel);

    if (parent != null) {   // we add a sub node
      if (parent instanceof LabelNode)    // it should be a LabelNode
        ((LabelNode)parent).addSubLabel (node, null);   // add as direct sublabel
    }
    else {                  // we're adding a root node
      IContentProvider provider = plan.getViewer ().getContentProvider ();

      // make sure we have the correct provider
      if (provider instanceof ActionPlanView.ViewContentProvider)
        ((ActionPlanView.ViewContentProvider) provider).addChild (node);
    }

    //--------  create the example node  --------//
    if (exampleModel != null) {
      ExampleNode examplenode = new ExampleNode (exampleModel);
      node.addExample (examplenode);
    }

    //--------  Generate AQL if required  --------//
    if (generateAQLs) {
      // get the current project in the action plan view
      IProject project = AqlProjectUtils.getProject (ActionPlanView.projectName);

      // this check should never fail, but just in case
      if (project != null) {
        try {
          node.createAqlFiles (project);
        }
        catch (Exception e) {
          Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
        }
      }
    }

    try {
      plan.serialize ();
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready, e);
    }

    plan.getViewer ().refresh ();

    if (node != null) {
      plan.getViewer ().expandToLevel (node, TreeViewer.ALL_LEVELS);
    }

  }

  private void setActionTextAndImage(String txt, String tooltip, ImageDescriptor image)
  {
    // sets the text for this action
    setText (txt);

    // set the tooltip for this action
    setToolTipText (tooltip);

    // set the icon for this action
    setImageDescriptor (image);
  }
}
