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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.run.SystemTRunConfig;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.profile.SystemTProfileJob;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action to run the systemt profiler @see SystemTProfileJob
 * 
 * 
 */
public class RunProfilerAction extends Action
{
  @SuppressWarnings("unused")

 
	public static final int DEFAULT_TIME = 60;

  public RunProfilerAction ()
  {
    setImageDescriptor (ImageDescriptor.createFromImage (Icons.PROFILER_ICON));
    setText (Messages.run_profiler_text);
    setToolTipText (Messages.run_profiler_tootltip);
  }

  /**
   * run the systemt profiler @see SystemTProfileJob for a fixed time of #DEFAULT_TIME seconds
   */
  public void run ()
  {
    ActionPlanView plan = AqlProjectUtils.getActionPlanView ();
    if (plan == null || !plan.ready ()) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready);
      return;
    }

    String inputCollection = "";
    String collectionLang = "en";
    int tokenizer = ProjectUtils.getTokenizerChoice (ActionPlanView.projectName);

    if (ActionPlanView.collection != null) {
      inputCollection = ActionPlanView.collection.getPath ();
      collectionLang = ActionPlanView.collection.getLangCode ();
    }
    else {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_collection_error,
        new Exception (Messages.extractionplan_not_ready));
      return;
    }

    if (inputCollection == null || collectionLang == null || inputCollection.trim ().isEmpty ()
      || collectionLang.trim ().isEmpty ()) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_collection_error);
      return;
    }

    IProject project = AqlProjectUtils.getProject (ActionPlanView.projectName);

    if (project == null) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready);
      return;
    }

    if (ProjectUtils.hasBuildErrors (project)) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.compiler_error_in_aql_files__profiler);
      return;
    }

    SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties (project);
    SystemTRunConfig profilerConfig = new SystemTRunConfig (collectionLang, inputCollection, "", properties);

    // Set tokenizer
    if (tokenizer == Constants.TOKENIZER_CHOICE_WHITESPACE)
      profilerConfig.setTokenizerChoice (tokenizer, null, null);

    // Set selected modules (all modules)
    String[] modules = ProjectUtils.getModules (ActionPlanView.projectName);
    if (modules == null || modules.length == 0)   // No module to run
      return;

    String allModules = modules[0];
    for (int i = 1; i < modules.length; i++) {
      allModules += Constants.DATAPATH_SEPARATOR + modules[i];
    }
    profilerConfig.setSelectedModules (allModules);

    final SystemTProfileJob job = new SystemTProfileJob (project.getName (), ActionPlanView.projectName,
      profilerConfig, DEFAULT_TIME);

    Job runJob = new Job (Messages.run_profiler_title) {
      @Override
      protected IStatus run (IProgressMonitor monitor)
      {
        monitor.beginTask (Messages.run_profiler_message, 100);
        job.run (monitor);
        monitor.done ();
        return Status.OK_STATUS;
      }
    };

    runJob.setRule (ResourcesPlugin.getWorkspace ().getRoot ());
    runJob.schedule ();

    // DebugPlugin.getDefault().getLaunchManager()
    // Shell shell = AqlProjectUtils.getActiveShell();
    // String id = "org.eclipse.debug.ui.launchGroup.profile";
    // DebugUITools.openLaunchConfigurationDialogOnGroup(shell, null, id);
  }

}
