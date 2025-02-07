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

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.util.PaginationTracker;
import com.ibm.biginsights.textanalytics.resultviewer.utils.AnnotationExplorerUtil;
import com.ibm.biginsights.textanalytics.tableview.model.IAQLTableViewModel;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;

public class ExportResultsWizard extends Wizard implements IExportWizard
{
	@SuppressWarnings("unused")


	private ExportResultsWizardPage page;
	private String directoryPath;
	private String viewName;
	private Shell shell;
	private int returnCode;
	protected IWorkbench workbench;
	protected IStructuredSelection selection;


	/**
	 * Create the wizard to get output directory where results are exported to.
	 * @param shell
	 * @param viewName Name of the view, result of which is exported. If it is NULL, export results of all views.
	 */
	ExportResultsWizard(Shell shell, String viewName)
	{
		super();
		this.viewName = viewName;
		this.shell = shell;
		setWindowTitle(com.ibm.biginsights.textanalytics.resultviewer.Messages.exportResultsExportResults); 
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		setWindowTitle(com.ibm.biginsights.textanalytics.resultviewer.Messages.exportResultsExportResults); 
	}

	@Override
	public void addPages() {
		super.addPages();
		page = new ExportResultsWizardPage("ExportResultsPage"); //$NON-NLS-1$
		addPage(page);
	}

	@Override
	public boolean performFinish() {

		// This will check whether the Export Location path is not null and
		// a valid path
		if (!page.isDataValid()) {
		  MessageBox errMsgDialog = new MessageBox(getShell(), SWT.ICON_ERROR);
			errMsgDialog.setMessage(page.getErrorMessage());
			errMsgDialog.setText(Messages.error); 
			errMsgDialog.open();
			return false;
		}
		else {
		  page.apply();
			directoryPath = page.getDirectory();

			// Displays a warning, when export path already contains the exported file.
			showWarningToOverwriteExportedFile();
			
			// Based on the return code from the Show Warning, this code will be executed.
			if (returnCode == Dialog.OK) {
			  GenerateHtmlCsv generateHtmlCsv = new GenerateHtmlCsv (directoryPath);
				PaginationTracker tracker = PaginationTracker.getInstance();
				IFolder resultFolder = tracker.getResultFolder ();

				try {
				  List<String> resultFilenames = tracker.getResultFilenames ();
				  for (String filename : resultFilenames) {
				    IFile resFile = resultFolder.getFile (filename);
            IConcordanceModel concordanceModel = AnnotationExplorerUtil.generateConcordanceModelFromFile (resFile, null, null, new NullProgressMonitor());

            // When view name is not given, export all results
            if (StringUtils.isEmpty (viewName))
              generateHtmlCsv.generateForAllViews (concordanceModel, false);

            // Otherwise, export only one view that's currently displayed in table view
            else {
              IAQLTableViewModel aqlTableViewModel = concordanceModel.getViewModel (viewName);
              generateHtmlCsv.generateView (aqlTableViewModel, false);
            }
				  }
				  generateHtmlCsv.writeAllFooters ();
				}
				catch (Exception e) {
					createErrorMsgBox();
					e.printStackTrace();
        }
			}
			else {
				// Return Code is Cancel
				return false;
			}

			return true;
		}
	}

	private void createErrorMsgBox() {
		CustomMessageBox errorMsgBox = CustomMessageBox
				.createErrorMessageBox(
						this.shell,
						Messages.getString("ERROR"),
						Messages.exportResultsWizardErrorExportingViews); //$NON-NLS-1$
		errorMsgBox.open();

	}

	private void showWarningToOverwriteExportedFile() {
		boolean dirExist = false;
		// Checks CSV or HTML folder present in the path
		File csvDir = FileUtils.createValidatedFile(directoryPath, Constants.CSV_DIR);
		File htmlDir = FileUtils.createValidatedFile(directoryPath, Constants.HTML_DIR);
		if (csvDir.exists()) {
			dirExist = true;
		}
		if (htmlDir.exists()) {
			dirExist = true;
		}

		// Get the setting from the Preference plugin
    final TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
    final boolean displayWarning = wprefs.getPrefWarnOverwritingExportResultsFile ();

		if (displayWarning && dirExist) {

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					Shell shell = ProjectUtils.getActiveWorkbenchWindow()
							.getShell();
					MessageDialogWithToggle msgBox = MessageDialogWithToggle.openOkCancelConfirm(
							shell,
							Messages.exportResultsDisplayWarningTitle,
							Messages.exportResultsDisplayWarningMessage,
							Messages.exportResultsDisplayWarningToggleMessage,
							displayWarning, null, null); 
					wprefs.setPrefWarnOverwritingExportResultsFile (msgBox.getToggleState()); 
					wprefs.savePreferences ();
					returnCode = msgBox.getReturnCode();
				}
			});

		}
	}

}
