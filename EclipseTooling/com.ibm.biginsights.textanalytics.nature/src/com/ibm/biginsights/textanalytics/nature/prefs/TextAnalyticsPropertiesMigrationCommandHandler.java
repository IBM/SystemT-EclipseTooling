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
package com.ibm.biginsights.textanalytics.nature.prefs;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * Handler for Text Analytics properties migration command that is available as an option in project explorer's context menu
 * It will enable this command only if the project has Text Analytics nature and is non modular.
 * 
 *
 */
public class TextAnalyticsPropertiesMigrationCommandHandler extends AbstractHandler
{


  
	public final String commandId = "com.ibm.biginsights.textanalytics.nature.migrateTAProperties"; //$NON-NLS-1$

  @Override
  public Object execute (ExecutionEvent event) throws ExecutionException
  {
    IProject project = ProjectUtils.getSelectedProject ();
    if (project != null) {
      MessageBox confirmationDialog = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell (), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
      confirmationDialog.setMessage (Messages.getString ("GeneralPrefPage.MigrationInfo") //$NON-NLS-1$
        + "\n" + Messages.getString("TextAnalyticsPropertiesMigrationCommandHandler.ConfirmationText")); //$NON-NLS-1$
      confirmationDialog.setText (Messages.getString ("TextAnalyticsPropertiesMigrationCommandHandler.DialogBoxTitle")); //$NON-NLS-1$
      int returnCode = confirmationDialog.open ();
      if ( returnCode == SWT.OK ) {
        boolean success = ProjectPreferencesUtil.migrateTAPropertiesToModularFormat (project);
        if (success) {
          MessageBox successInfoDialog = new MessageBox(PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getShell (),
            SWT.ICON_INFORMATION|SWT.OK );
          successInfoDialog.setText(Messages.getString ("TextAnalyticsPropertiesMigrationCommandHandler.DialogBoxTitle")); //$NON-NLS-1$
          successInfoDialog.setMessage(Messages.getString ("GeneralPrefPage.MigrationSuccessMessage")); //$NON-NLS-1$
          successInfoDialog.open ();
        }
        /*
         * Not doing anything here if migration failed, because
         * migrateTAPropertiesToModularFormat(project) would
         * display the failure message along with reason.
         */
      }
    }
    return null;
  }

  @Override
  public boolean isEnabled ()
  {
    IProject project = ProjectUtils.getSelectedProject ();
    if (project == null) {
      return false;
    } else {
      return ProjectPreferencesUtil.isMigrationRequiredForProjectPropertiesToModular (project);
    }
  }
}
