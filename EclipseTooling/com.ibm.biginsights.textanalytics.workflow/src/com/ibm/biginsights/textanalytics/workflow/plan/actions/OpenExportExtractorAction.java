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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.textanalytics.export.ExportAOGWizard;
import com.ibm.biginsights.textanalytics.export.ExportAOGWizardPage;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action to export the extractor being edited in the action plan
 * @see ExportAOGWizard
 * 
 * 
 */
public class OpenExportExtractorAction extends Action
{



  public OpenExportExtractorAction ()
  {
    setImageDescriptor (ImageDescriptor.createFromImage (Icons.EXPORT_AOG_ICON));
    setText (Messages.open_export_text);
    setToolTipText (Messages.open_export_tootltip);
  }

  /**
   * 
   */
  public void run ()
  {
    ActionPlanView plan = AqlProjectUtils.getActionPlanView ();
    if (plan == null || !plan.ready ()) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready);
      return;
    }

    IProject project = AqlProjectUtils.getProject (ActionPlanView.projectName);

    if (project == null) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready);
      return;
    }

    ExportAOG wizard = new ExportAOG (project);
    Shell shell = AqlProjectUtils.getActiveShell ();
    WizardDialog dialog = new WizardDialog (shell, wizard);
    dialog.open ();
  }

  /**
   * 
   * 
   *
   */
  class ExportAOG extends ExportAOGWizard
  {
    public ExportAOG (IProject project)
    {
      this.setWindowTitle (com.ibm.biginsights.textanalytics.nature.Messages.getString ("ExportAOGWizard.EXPORT_ANNOTATOR"));
      this.project = project;
    }

    @Override
    public void addPages ()
    {

      try {
        if (project.hasNature (Activator.NATURE_ID)) {
          page = new ExportAOGWizardPage ("page", project.getName ());
          addPage (page);
          //exportPref = page.getPreferences ();
        }
        else {
          CustomMessageBox errorMsgBox = CustomMessageBox.createErrorMessageBox (
            getShell (),
            com.ibm.biginsights.textanalytics.nature.Messages.getString ("General.ERROR"), com.ibm.biginsights.textanalytics.nature.Messages.getString ("General.ERR_NOT_TEXT_ANAYLTICS_PROJ")); //$NON-NLS-1$ //$NON-NLS-2$
          errorMsgBox.open ();
          return;
        }
      }
      catch (CoreException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready, e);
      }
    }
  }
}
