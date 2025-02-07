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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ExampleNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action to delete an element from the action plan
 * 
 * 
 */
public class DeleteAction extends Action
{



  List<TreeObject> toDeleteList;
  ActionPlanView plan;
  boolean doConfirm = true;

  public DeleteAction (List<TreeObject> toDeleteList, ActionPlanView plan)
  {
    this.toDeleteList = toDeleteList;
    this.plan = plan;

    setText (Messages.delete_children_text);
    setToolTipText (Messages.delete_children_tootltip);
    setImageDescriptor (ImageDescriptor.createFromImage (Icons.DELETE_ICON));
  }

  @SuppressWarnings("unchecked")
  public DeleteAction(IStructuredSelection selection, ActionPlanView plan)
  {
    this(selection.toList(), plan);
  }

  public DeleteAction (TreeObject toDeleteObj)
  {
    this (Arrays.asList (new TreeObject[] { toDeleteObj }), AqlProjectUtils.getActionPlanView ());
  }

  public DeleteAction (TreeObject[] toDeleteObjects)
  {
    this (Arrays.asList (toDeleteObjects), AqlProjectUtils.getActionPlanView ());
  }

/**
   * removed the element and all of its children from the action plan -> store the action plan into the serialized file
   */
  public void run ()
  {
    // make sure that the action plan is properly initialized
    if (toDeleteList == null || plan == null || !plan.ready ()) return;

    Shell shell = plan.getViewer ().getControl ().getShell ();

    if (doConfirm) {
      boolean cont = false;
      if (toDeleteList.size () == 1)
        cont = MessageDialog.openConfirm (shell, Messages.delete_children_title, Messages.delete_child_message);
      else if (toDeleteList.size () > 1) {
        String msg = MessageUtil.formatMessage (Messages.delete_children_message, "" + toDeleteList.size ());
        cont = MessageDialog.openConfirm (shell, Messages.delete_children_title, msg);
      }

      if (!cont)
        return;
    }

    for (TreeObject deletedObject : toDeleteList) {

      //-------- Remove model object
      LabelNode parentLabelNode = deletedObject.getParentLabelNode ();
      if (parentLabelNode == null) {
        if (deletedObject instanceof LabelNode)
          plan.getContentProvider ().removeRootLabel ((LabelNode)deletedObject);
      }
      else {
        Object deletedModelObject = null;
        if (deletedObject instanceof AqlNode)
          deletedModelObject = ((AqlNode)deletedObject).toModel ();
        else if (deletedObject instanceof LabelNode)
          deletedModelObject = ((LabelNode)deletedObject).toModel ();
        else if (deletedObject instanceof ExampleNode)
          deletedModelObject = ((ExampleNode)deletedObject).toModel ();

        parentLabelNode.toModel ().removeChild(deletedModelObject);
      }

      //-------- Remove tree object
      if (deletedObject.getParent () != null)
        deletedObject.getParent ().removeChild (deletedObject);
    }

    plan.serializeAndRefresh ();
  }

  public void setConfirm (boolean confirm)
  {
    doConfirm = confirm;
  }
}
