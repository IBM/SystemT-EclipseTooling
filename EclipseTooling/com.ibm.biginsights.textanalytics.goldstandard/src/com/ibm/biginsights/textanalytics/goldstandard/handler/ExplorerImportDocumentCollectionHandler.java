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
package com.ibm.biginsights.textanalytics.goldstandard.handler;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.handler.util.InputCollectionImporter;
import com.ibm.biginsights.textanalytics.goldstandard.ui.ConfigurationDialog;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.FilteredFileDirectoryDialog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class handles the click event on popup menu item 'Labeled Document Collection -> Import from document collection'
 * on a gsParentFolder or Project from the project explorer or package explorer.
 * 
 * This class imports the specified corpus into labeledCollections directory.
 *  Krishnamurthy
 *
 */
public class ExplorerImportDocumentCollectionHandler extends GSActionHandler implements
		IHandler {
	@SuppressWarnings("unused")


	public ExplorerImportDocumentCollectionHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (super.execute(event) == null) {
			return null;
		}
		
		FilteredFileDirectoryDialog fileDialog = new FilteredFileDirectoryDialog(
				ProjectUtils.getActiveWorkbenchWindow().getShell(),
				new WorkbenchLabelProvider(), new WorkbenchContentProvider(),
				Constants.FILE_OR_DIRECTORY);
		fileDialog.setAllowMultipleSelection(false);
		fileDialog.setAllowedExtensions(Constants.SUPPORTED_DOC_FORMATS); 
		fileDialog.setEnableShowAllFilesOption(true);
		fileDialog.setTitle(Messages.ImportCorpusHandler_IMPORT_CORPUS);
		fileDialog.setMessage(Messages.ImportCorpusHandler_SELECT_INPUT_COLLECTION);
		
		fileDialog.setContextHelpId("com.ibm.biginsights.textanalytics.tooling.help.import_document_collection");

		IResource selectedResource = fileDialog.getSelectedResource();
	
		if(selectedResource != null){
			boolean isUTF8 = ProjectUtils.isUTF8Encoding(selectedResource);
			if(!isUTF8){
				showWarningForNonUTF8Encoding();
			}
			
			String title = MessageUtil.formatMessage(Messages.ImportCorpusHandler_IMPORTING_CORPUS, selectedResource.getName());
			Job systemtJob = new ImportCorpusJob(title, gsParentFolder,
					selectedResource);
			systemtJob.setUser(true);
			systemtJob.schedule();
		}
		
		return null;

	}

	/**
	 * This method cannot get into com.ibm.biginsights.textanalytics.util plugin, 
	 * since it introduces dependency on PreferencesPlugin, which is not desired for the util plugin.
	 * 
	 */
	private void showWarningForNonUTF8Encoding(){
		final TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
		final boolean displayWarning = wprefs.getPrefValidateInputEncoding ();
		if(displayWarning){
			Display.getDefault().syncExec(new Runnable() {
		        @Override
		        public void run() {
					Shell shell = ProjectUtils.getActiveWorkbenchWindow().getShell();
					MessageDialogWithToggle msgBox = MessageDialogWithToggle.openWarning(shell, Messages.ImportCorpusActionDelegate_IncorrectEncoding, 
							Messages.ImportCorpusActionDelegate_EncodingNotUTF8, 
							Messages.ImportCorpusActionDelegate_ContinueToWarn, displayWarning, null, null);
					/**
					 * Note: Always store boolean values as Strings in IPreferenceStore, as false values are 
					 * automatically removed from the preference store while saving
					 */
					wprefs.setPrefValidateInputEncoding (msgBox.getToggleState());
					wprefs.savePreferences ();
		        }});
			
		}
	}
	
	/////////////////////////////////////////////////////////////

	private class ImportCorpusJob extends Job {

		IFolder gsParentFolder;
		IResource selectedResource;
		IProgressMonitor progressMonitor;

		public ImportCorpusJob(String name, IFolder gsParentFolder,
				IResource selectedResource) {
			super(name);
			this.gsParentFolder = gsParentFolder;
			this.selectedResource = selectedResource;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			this.progressMonitor = monitor;
			boolean success = false;
			try {
				final IFolder gsFolder = GoldStandardUtil.createDefaultGSFolder(gsParentFolder, progressMonitor);
				File inputCollection = selectedResource.getLocation().toFile();

				success = new InputCollectionImporter(inputCollection, gsFolder, progressMonitor).importCorpus();
				gsParentFolder.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				
				if(success){
					Display.getDefault().syncExec(new Runnable() {
				        @Override
				        public void run() {
							String msg = Messages.ImportCorpusActionDelegate_IMPORT_SUCCESSFUL_CONFIRM_CONFIG_DIALOG_OPEN; 
							String formattedMsg = MessageUtil.formatMessage(msg, gsFolder.getFullPath().toString());
							Shell shell = ProjectUtils.getActiveWorkbenchWindow().getShell();
							boolean confirm = MessageDialog.openConfirm(shell,Messages.ImportCorpusActionDelegate_IMPORT_DOC_COLLECTION, formattedMsg);
							if(confirm){
								ConfigurationDialog dialog = new ConfigurationDialog(shell, gsFolder);
								dialog.open();
							}
				        }});
				}else{
					String msg = Messages.ImportCorpusActionDelegate_ERROR_IMPORTING_DOC_COLLECTION;
					String formattedMsg = MessageUtil.formatMessage(msg, selectedResource.getName());
					LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(formattedMsg);
					gsFolder.delete(true, monitor);
				}
			} catch (CoreException e) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(e.getLocalizedMessage());
			}
			return Status.OK_STATUS;
		}
	}//end: class ImportCorpusJob

}//end: class ExplorerImportDocumentCollectionHandler
