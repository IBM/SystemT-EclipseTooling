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

import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.CommentDialog;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action to add a comment to a given element in the action plan
 * 
 * 
 */
public class AddComment extends Action
{



  protected TreeObject selection;
  protected ActionPlanView plan;
  protected String comment;
  protected boolean newComment;

  public AddComment (TreeObject selection, ActionPlanView plan)
  {
    this.selection = selection;
    this.plan = plan;

    loadComment ();

    if (comment == null || comment.isEmpty ()) {
      setText (Messages.add_comment_text);
      setToolTipText (Messages.add_comment_tootltip);
      newComment = true;
    }
    else {
      setText (Messages.edit_comment_text);
      setToolTipText (Messages.edit_comment_tootltip);
      newComment = false;
    }

    setImageDescriptor (ImageDescriptor.createFromImage (Icons.COMMENT_ICON));
  }

  /**
   * loads a previous saved comment for this node
   */
  private void loadComment ()
  {
    if (selection instanceof AqlNode) {
      AqlNode node = (AqlNode) selection;
      comment = node.getComment ();
    }
    else if (selection instanceof LabelNode) {
      LabelNode node = (LabelNode) selection;
      comment = node.getComment ();
    }
  }

  /**
   * provides a Dialog to define or edit the comment for this element
   */
  @Override
  public void run ()
  {
    Shell shell = AqlProjectUtils.getActiveShell ();

    // make a new dialog to get parameters from the user
    CommentDialog dialog = new CommentDialog (shell, this.getText (), this.getText (), comment, null);

    // open the dialog to request the new comment
    final int rc = dialog.open ();

    // on ok
    if (rc == Window.OK) {
      String value = dialog.getValue ();
      if (value != null) {
        if (selection instanceof AqlNode) {
          AqlNode node = (AqlNode) selection;
          node.setComment (value);
        }
        else if (selection instanceof LabelNode) {
          LabelNode node = (LabelNode) selection;
          node.setComment (value);
        }
        if (plan != null && plan.ready ()) {
          try {
            plan.serialize ();
          }
          catch (UnsupportedEncodingException e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready, e);
            e.printStackTrace ();
          }
          catch (CoreException e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready, e);
          }
        }
      }
    }
  }
}
