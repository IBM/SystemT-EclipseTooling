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
package com.ibm.biginsights.textanalytics.nature.run;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class RunSystemTAction implements IObjectActionDelegate {



  private ISelection selection;

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(IAction action) {
    if (this.selection instanceof IStructuredSelection) {
      IStructuredSelection ssel = (IStructuredSelection) this.selection;
      if (ssel.size() != 1) {
        return;
      }
      Object element = ssel.getFirstElement();
      IProject project = null;
      if (element instanceof IProject) {
        project = (IProject) element;
      } else if (element instanceof IAdaptable) {
        project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
      }
      if (project != null) {
        boolean hasAqlNature = false;
        try {
          hasAqlNature = project.hasNature(Activator.NATURE_ID);
        } catch (CoreException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        if (hasAqlNature) {
          runSystemtOnProject(project);
        } else {
          LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
              Messages.getString("RunSystemTAction_ERR_CANNOT_RUN_ON_NON_SYSTEMT_PROJECT")); //$NON-NLS-1$
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(IAction action, ISelection sel) {
    this.selection = sel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
   * org.eclipse.ui.IWorkbenchPart)
   */
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    // do nothing
  }

  private void runSystemtOnProject(IProject project) {
//    Job systemtJob = new SystemtRunJob("Running SystemT on project " + project.getName(), project);
//    systemtJob.setUser(true);
//    systemtJob.schedule();
  }

}
