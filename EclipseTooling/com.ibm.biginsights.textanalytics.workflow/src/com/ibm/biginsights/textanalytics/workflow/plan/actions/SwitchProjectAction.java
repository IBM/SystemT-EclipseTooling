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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.WorkbenchException;

import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.ChooseProjectDialog;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action to switch the current project in the action plan view
 * 
 * 
 */
public class SwitchProjectAction extends Action
{



  ActionPlanView plan;

  public SwitchProjectAction ()
  {
    setText (Messages.change_project_text);
    setToolTipText (Messages.change_project_tootltip);
    setImageDescriptor (ImageDescriptor.createFromImage (Icons.SWITCH_ICON));
  }

  /**
   * switch the current project in the action plan view by requesting a new one from the user throw a project dialog
   */
  public void run ()
  {
    Shell shell = AqlProjectUtils.getActiveShell ();
    ChooseProjectDialog dialog = new ChooseProjectDialog (shell);
    final int state = dialog.open ();
    if (state == Window.OK) {
      String projectName = dialog.getName ();
      try {
        AqlProjectUtils.openActionPlan (projectName);
      }
      catch (WorkbenchException e) {
        e.printStackTrace ();
      }
    }
  }

}
