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

import com.ibm.biginsights.textanalytics.workflow.plan.actions.SwitchProjectAction;

/**
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class LoadExistingProjectHandler extends AbstractHandler
{


 
	/**
   * The constructor.
   */
  public LoadExistingProjectHandler ()
  {}

  /**
   * creates a new SwitchProjectAction and run it
   * @see SwitchProjectAction for more info
   */
  public Object execute (ExecutionEvent event) throws ExecutionException
  {
    SwitchProjectAction action = new SwitchProjectAction ();
    action.run ();
    return null;
  }
}
