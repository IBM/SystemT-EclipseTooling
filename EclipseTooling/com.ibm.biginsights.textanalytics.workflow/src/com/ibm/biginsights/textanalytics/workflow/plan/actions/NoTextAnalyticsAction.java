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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;

/**
 * action to display an error message stating that a project doesn't have the text analytics nature
 * 
 * 
 */
public class NoTextAnalyticsAction extends Action
{


 
	public void run ()
  {
    Shell shell = AqlProjectUtils.getActiveShell ();
    MessageDialog.openError (shell, Messages.missing_text_analytics_nature_title,
      Messages.missing_text_analytics_nature_message);
  }
}
