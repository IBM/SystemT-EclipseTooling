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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.run.SystemTRunConfig;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * run all aql files over the entire document collection
 * 
 * 
 */
public class RunAllAction extends RunAbstract
{

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
 
	private String moduleName = null;

  public RunAllAction (ActionPlanView plan)
  {
    super (plan);

    setText (Messages.run_default_text);
    setToolTipText (Messages.run_default_tootltip);
    setImageDescriptor (ImageDescriptor.createFromImage (Icons.RUN_ICON));
  }

  public RunAllAction (ActionPlanView plan, String moduleName)
  {
    super (plan);

    setText (MessageUtil.formatMessage (Messages.run_module_default_text));
    setToolTipText (MessageUtil.formatMessage (Messages.run_module_on_input_collection_text, moduleName));
    setImageDescriptor(ImageDescriptor.createFromImage(Icons.RUN_ON_COLLECTION_ICON));

    this.moduleName = moduleName;
  }

  /**
   * run all aql files over the entire document collection
   */
  public void run ()
  {
    if (plan == null || !plan.ready ()) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready);
      return;
    }

    if (ActionPlanView.projectName == null || ActionPlanView.collection == null
      || ActionPlanView.collection.getPath ().isEmpty () || ActionPlanView.collection.getLangCode ().isEmpty ()) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_collection_error,
        new Exception (Messages.extractionplan_not_ready));
      return;
    }

    IProject project = AqlProjectUtils.getProject (ActionPlanView.projectName);
    if (project == null) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready);
      return;
    }

    SystemTProperties systemTProperties = ProjectPreferencesUtil.getSystemTProperties (project);

    if (systemTProperties == null) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready);
      return;
    }

    List<String> ignore = new LinkedList<String> ();

    SystemTRunConfig runConfig = new SystemTRunConfig (ActionPlanView.collection.getLangCode (),
      ActionPlanView.collection.getPath (), "", systemTProperties, ignore);

    if (moduleName != null)
      runConfig.setSelectedModules (moduleName);
    else {  // moduleName = null means running all modules
      String[] modules = ProjectUtils.getModules (ActionPlanView.projectName);
      String modulesStr = "";
      for (String m : modules) {
        modulesStr += m + Constants.DATAPATH_SEPARATOR;
      }
      runConfig.setSelectedModules (modulesStr);
    }

    AqlProjectUtils.runSystemT (Messages.running_message, project, runConfig);

  }

}
