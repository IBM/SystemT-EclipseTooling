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
package com.ibm.biginsights.textanalytics.concordance.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.ibm.biginsights.textanalytics.concordance.model.impl.ConcordanceModel;
import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class ShowConcordanceViewAction implements IObjectActionDelegate {



  // This is an arbitrary number as we don't know what the view update is doing internally
  private static final int TIME_TO_UPDATE_VIEW = 5;

  // Run the model building as a job so the UI stays responsive
  private class ModelBuilder extends Job {

    public ModelBuilder() {
      super("Building concordance");
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
      // final ConcordanceModel model = createModel(this.xcasFolder, monitor);
      final ConcordanceModel model = null;
      if (monitor.isCanceled()) {
        return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, "Concordance view canceled");
      }
      monitor.subTask("Updating view.");
      // Since the model building is running in a non-UI thread, and we need to update the UI when
      // done, we need to employ the usual trick of submitting the UI update as a UI job.
      Display.getDefault().asyncExec(new Runnable() {
        public void run() {
          ShowConcordanceViewAction.this.view.setInput(model);
        }
      });
      monitor.worked(TIME_TO_UPDATE_VIEW);
      monitor.done();
      return Status.OK_STATUS;
    }
  }

  private IWorkbenchPart targetPart;

  private ConcordanceView view;

  protected static ILog log = LogUtil.getLogForPlugin(Activator.PLUGIN_ID);

  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    this.targetPart = targetPart;
  }

  public void run(IAction action) {
    this.view = showConcordanceView();
    Job job = new ModelBuilder();
    job.setUser(true);
    job.schedule();
  }

  public void selectionChanged(IAction action, ISelection sel) {
    // do nothing
  }

  private ConcordanceView showConcordanceView() {

    final IWorkbenchPartSite site = this.targetPart.getSite();
    final IWorkbenchWindow window = site.getWorkbenchWindow();

    try {

      return (ConcordanceView) window.getActivePage().showView(
          "com.ibm.biginsights.textanalytics.concordance.view", null, IWorkbenchPage.VIEW_VISIBLE);
    } catch (final PartInitException e) {
      log.logAndShowError("Concordance View could not be opened", e);
    }
    return null;

  }

}
