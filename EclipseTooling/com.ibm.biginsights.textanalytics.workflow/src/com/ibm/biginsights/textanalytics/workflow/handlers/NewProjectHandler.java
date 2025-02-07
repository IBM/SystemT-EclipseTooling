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
package com.ibm.biginsights.textanalytics.workflow.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.project.wizard.NewBIProjectWizard;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class NewProjectHandler extends AbstractHandler
{


 
	/**
   * The constructor.
   */
  public NewProjectHandler ()
  {}

  /**
   * creates a new project using the @see NewBIProjectWizard wizard, adds the TextAnalytics nature to it and opens it in
   * the Action Plan View
   * 
   * @see AqlProjectUtils#addTextAnalyticsConfiguration(org.eclipse.core.resources.IProject)
   * @see AqlProjectUtils#openActionPlan(String)
   */
  public Object execute (ExecutionEvent event) throws ExecutionException
  {
    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked (event);
    Shell shell = window.getShell ();

    NewBIProjectWizard projW = new NewBIProjectWizard ();
    WizardDialog dialog = new WizardDialog (shell, projW);

    final int rc = dialog.open ();
    if (rc == Window.OK) {

      final String projectName = projW.getProjectName ();

      WorkspaceJob job = new WorkspaceJob ("...") {
        @Override
        public IStatus runInWorkspace (IProgressMonitor monitor) throws CoreException
        {
          try {
            AqlProjectUtils.addTextAnalyticsConfiguration (AqlProjectUtils.getProject (projectName));

            Display.getDefault ().asyncExec (new Runnable () {

              @Override
              public void run ()
              {
                try {
                  AqlProjectUtils.openActionPlan (projectName);
                }
                catch (WorkbenchException e) {
                  e.printStackTrace ();
                }
              }
            });
          }
          catch (Exception e1) {
            e1.printStackTrace ();
          }
          return Status.OK_STATUS;
        }
      };

      job.schedule ();

    }
    return null;
  }
}
