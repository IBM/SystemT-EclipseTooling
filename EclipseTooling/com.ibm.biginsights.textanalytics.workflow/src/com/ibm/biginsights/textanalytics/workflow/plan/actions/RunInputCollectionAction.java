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

import org.eclipse.jface.resource.ImageDescriptor;

import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * run the extractor in the entire document collection
 * 
 *
 */
public class RunInputCollectionAction extends RunAbstract {



  RunAllAction run;

  public RunInputCollectionAction(ActionPlanView plan) {
		super (plan);

		setText(Messages.run_input_collection_text);
		setToolTipText(Messages.run_input_collection_tootltip);
		setImageDescriptor(ImageDescriptor.createFromImage(Icons.RUN_ON_COLLECTION_ICON));

		run = new RunAllAction(plan);
  }

	public RunInputCollectionAction (ActionPlanView plan, String moduleName)   {
    super (plan);

    setText (MessageUtil.formatMessage (Messages.run_module_on_input_collection_text, moduleName));
    setToolTipText (MessageUtil.formatMessage (Messages.run_module_on_input_collection_text, moduleName));
    setImageDescriptor(ImageDescriptor.createFromImage(Icons.RUN_ON_COLLECTION_ICON));

    run = new RunAllAction(plan, moduleName);
  }

  /**
	 * run the extractor in the entire document collection
	 */
	public void run() {
		run.run();
	}

}
