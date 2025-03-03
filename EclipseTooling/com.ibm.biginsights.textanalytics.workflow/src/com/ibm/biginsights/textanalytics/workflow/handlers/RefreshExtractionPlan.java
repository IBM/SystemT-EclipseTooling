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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.WorkbenchJob;

import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;

public class RefreshExtractionPlan extends AbstractHandler
{



  @Override
  public Object execute (ExecutionEvent event) throws ExecutionException
  {
    // No extraction plan is opened yet -> nothing to refresh.
    if (ActionPlanView.projectName == null)
      return null;

    final String projectName = event.getParameter(Constants.REFRESH_EP_PROJECT_PARAM_ID);
    final String newProjectName = event.getParameter(Constants.REFRESH_EP_NEW_PROJECT_PARAM_ID);

    WorkbenchJob job = new WorkbenchJob (Messages.refresh_extraction_plan_job) {
      @Override
      public IStatus runInUIThread (IProgressMonitor monitor)
      {
        AqlProjectUtils.reloadExtractionPlanForProject (projectName, newProjectName);
        return Status.OK_STATUS;
      }
    };

    job.schedule ();

    return null;
  }

}
