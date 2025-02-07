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
package com.ibm.biginsights.textanalytics.workflow.popup.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.WorkbenchException;

import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;

public class OpenActionPlan implements IObjectActionDelegate
{



  private IProject project;

  /**
   * Constructor for Action1.
   */
  public OpenActionPlan ()
  {
    super ();
  }

  /**
   * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
   */
  public void setActivePart (IAction action, IWorkbenchPart targetPart)
  {}

  /**
   * @see IActionDelegate#run(IAction)
   */
  public void run (IAction action)
  {
    if (project != null) {
      try {
        AqlProjectUtils.openActionPlan (project.getName ());
      }
      catch (WorkbenchException e) {
        // the static method above handles errors messages and logs, so we don;t need to worry about them here
        e.printStackTrace ();
      }
    }
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged (IAction action, ISelection selection)
  {
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection sel = (IStructuredSelection) selection;

      @SuppressWarnings("rawtypes")
      Iterator iter = sel.iterator ();

      while (iter.hasNext ()) {
        Object obj = iter.next ();
        if (obj instanceof IProject) {
          project = (IProject) obj;
        }
      }
    }
  }

}
