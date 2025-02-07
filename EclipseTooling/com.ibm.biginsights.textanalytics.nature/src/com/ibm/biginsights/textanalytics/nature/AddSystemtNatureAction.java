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
package com.ibm.biginsights.textanalytics.nature;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class AddSystemtNatureAction implements IObjectActionDelegate {



  private ISelection selection;

  public void run(IAction action) {
    if (this.selection instanceof IStructuredSelection) {
      for (@SuppressWarnings("rawtypes")
      Iterator it = ((IStructuredSelection) this.selection).iterator(); it.hasNext();) {
        Object element = it.next();
        IProject project = null;
        if (element instanceof IProject) {
          project = (IProject) element;
        } else if (element instanceof IAdaptable) {
          project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
        }
        if (project != null) {
          try {
            IProjectDescription projectDescription = project.getDescription();
            String[] projectNatures = projectDescription.getNatureIds();
            for (String nature : projectNatures) {
              if (Activator.NATURE_ID.equals(nature)) {
                return;
              }
            }
            String[] natures = new String[projectNatures.length + 1];
            System.arraycopy(projectNatures, 0, natures, 0, projectNatures.length);
            natures[projectNatures.length] = Activator.NATURE_ID;
            projectDescription.setNatureIds(natures);
            project.setDescription(projectDescription, new NullProgressMonitor());
          } catch (CoreException e) {
            e.printStackTrace();
          }
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


}
