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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ui.FilteredFileDirectoryDialog;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlGroup;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action to change the aql file for a given group. each group has an aql file associated to it, this file is where each
 * of the newly created elements of this group get inserted to.
 * 
 * 
 */
public class ChangeAQLFileAction extends Action
{



  ActionPlanView plan;
  AqlGroup group;

  public ChangeAQLFileAction (ActionPlanView plan, AqlGroup group)
  {
    this.plan = plan;
    this.group = group;

    setText (Messages.change_aql_file_text);
    setToolTipText (Messages.change_aql_file_tootltip);
    setImageDescriptor (ImageDescriptor.createFromImage (Icons.AQL_FILE_ICON));
  }

  /**
   * request the new file and update this in the action plan. serialize the action plan to store the changes
   */
  @Override
  public void run ()
  {
    // TODO rewrite this action
//    Shell shell = AqlProjectUtils.getActiveShell ();
//    IFile file = requestPath (shell);
//    if (file != null && file.exists ()) {
//      group.updateAqlFile (Constants.WORKSPACE_RESOURCE_PREFIX + file.getFullPath ().toString ());
//      try {
//        plan.serialize ();
//        MessageDialog.openWarning (shell, Messages.aql_file_added_title, Messages.aql_file_added_warn);
//      }
//      catch (Exception e) {
//        e.printStackTrace ();
//      }
//    }
  }

  /**
   * request the new file's path from the user
   * 
   * @param shell
   * @return
   */
  private IFile requestPath (Shell shell)
  {
    FilteredFileDirectoryDialog dialog = new FilteredFileDirectoryDialog (shell, new WorkbenchLabelProvider (),
      new WorkbenchContentProvider (), Constants.FILE_ONLY);

    dialog.setTitle (Messages.missing_file_title);
    dialog.setMessage (Messages.select_file_message);

    dialog.setAllowedExtensions ("aql");

    IResource iResource = dialog.getSelectedResource ();

    if (iResource != null && iResource instanceof IFile) { return (IFile) iResource; }
    return null;
  }
}
