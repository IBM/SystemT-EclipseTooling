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
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;

public class OpenExtractionPlan extends AbstractHandler
{



  /**
   * opens the action plan for the selected project
   * 
   * @see AqlProjectUtils#openActionPlan(String)
   */
  @Override
  public Object execute (ExecutionEvent event) throws ExecutionException
  {
    IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection (event);

    Object firstElement = selection.getFirstElement ();
    if (firstElement instanceof IProject || firstElement instanceof IJavaProject) {

      IProject project = null;

      if (firstElement instanceof IJavaProject)
        project = ((IJavaProject) firstElement).getProject ();
      else
        project = (IProject) firstElement;

      try {
        AqlProjectUtils.openActionPlan (project.getName ());
      }
      catch (WorkbenchException e) {
        e.printStackTrace ();
      }

    }

    return null;
  }
}
