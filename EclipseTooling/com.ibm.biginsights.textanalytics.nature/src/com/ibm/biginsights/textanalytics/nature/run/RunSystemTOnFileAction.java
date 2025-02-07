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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.biginsights.textanalytics.nature.Activator;

public class RunSystemTOnFileAction implements IObjectActionDelegate {



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
      IFile file = null;
      if (element instanceof IFile) {
        file = (IFile) element;
      } else if (element instanceof IAdaptable) {
        file = (IFile) ((IAdaptable) element).getAdapter(IFile.class);
      } else {
        return;
      }
      if ("aql".equals(file.getFileExtension())) { //$NON-NLS-1$
        IProject project = file.getProject();
        try {
          if (project.hasNature(Activator.NATURE_ID)) {
            runSystemtOnFile(file);
          }
        } catch (CoreException e) {
          e.printStackTrace();
        }
      }
      return;
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

  private void runSystemtOnFile(IFile file) {
//    Job systemtJob = new SystemtRunJob("Running SystemT on file " + file, file);
//    systemtJob.setUser(true);
//    systemtJob.schedule();
  }

}
