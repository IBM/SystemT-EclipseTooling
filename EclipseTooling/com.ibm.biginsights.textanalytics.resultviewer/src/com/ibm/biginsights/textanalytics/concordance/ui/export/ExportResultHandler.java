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
package com.ibm.biginsights.textanalytics.concordance.ui.export;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.concordance.ui.ConcordanceView;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.tableview.view.AQLResultView;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;

/**
 * Command handler to Export the results from Annotation Explorer or Results
 * Viewer.
 */

public class ExportResultHandler extends AbstractHandler {

	@SuppressWarnings("unused")
private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		Shell shell = HandlerUtil.getActiveShell(event);
		
    // User exports results of Annotation Explorer.
    if (part instanceof ConcordanceView) {
      ConcordanceView annotExplView = (ConcordanceView)part;
      if (annotExplView.getTableViewer ().getTable ().getItemCount () > 0) {
        ExportResultsWizard wizard = new ExportResultsWizard (shell, null);
        WizardDialog dialog = new WizardDialog (shell, wizard);
        dialog.setPageSize (40, 120);
        dialog.open ();
      }
      else {
        CustomMessageBox errorMsgBox = CustomMessageBox.createErrorMessageBox (shell, Messages.error,
          Messages.exportResultsWizardErrorMsg);
        errorMsgBox.open ();
        return null;
      }
    }
    // User clicks Export button from Table Viewer, export only the result of the view of Table Viewer.
    else if (part instanceof AQLResultView) {
      AQLResultView aqlResultView = (AQLResultView) part;

      if (aqlResultView.getTableViewer ().getTable ().getItemCount () > 0) {
        ExportResultsWizard wizard = new ExportResultsWizard (shell, aqlResultView.getModel ().getName ());
        WizardDialog dialog = new WizardDialog (shell, wizard);
        dialog.setPageSize (40, 120);
        dialog.open ();
      }
      else {
        CustomMessageBox errorMsgBox = CustomMessageBox.createErrorMessageBox (shell, Messages.error,
          Messages.exportResultsWizardErrorMsg2);
        errorMsgBox.open ();
        return null;
      }
    }

		return null;
	}

}
