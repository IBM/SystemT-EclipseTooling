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

import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;

/**
 * action executed upon double click into any element of the action plan. each element implements its own double click
 * handler based on its type
 * 
 * 
 */
public class DoubleClickAction extends Action
{



  ActionPlanView plan;
  TreeObject element;

  public DoubleClickAction (ActionPlanView plan, TreeObject element)
  {
    this.plan = plan;
    this.element = element;
  }

  public void run ()
  {
    // make sure that teh action plan is properly initialized
    if (!plan.ready ()) return;

    element.doubleClick ();
  }

}
