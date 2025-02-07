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
package com.ibm.biginsights.textanalytics.regex.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLPartitionScanner;
import com.ibm.biginsights.textanalytics.regex.Activator;
import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.learner.ui.RegexLearnerWizard;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomWizardDialog;

public class RegexLearnerWizardCommand extends AbstractHandler {



	public RegexLearnerWizardCommand() {
		super();
	}

	//  /**
	//   * @throws ExecutionException Not at this time 
	//   */
	//  @Override
	//  public Object execute(ExecutionEvent arg0) throws ExecutionException {
	//    final RegexLearnerWizard w = new RegexLearnerWizard(null);
	//    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	//    final WizardDialog dialog = new WizardDialog(shell, w);
	//    dialog.open();
	////    RuleSection.this.mEditor.setRuleModelDirty();
	////    RuleSection.this.mListener.sectionUpdated();
	////    refresh(RuleSection.this.mRuleWrapper);
	//    return null;
	//  }

	public Object execute(ExecutionEvent event) throws ExecutionException {
    	try {
    		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    		if((event.getTrigger().toString().indexOf("StyledText") != -1) || (event.getTrigger().toString().indexOf("MenuItem") != -1)) //$NON-NLS-1$ //$NON-NLS-2$
        	{
    			// Get the editor from the event
    			IEditorPart editor = HandlerUtil.getActiveEditor(event);
    			Assert.isNotNull(editor);
    			Assert.isLegal(editor instanceof AQLEditor);
    			AQLEditor aqlEditor = (AQLEditor) editor;
    			// 	We use the document partitioning to find out if we're inside a regex
    			ITypedRegion region = RegexHandlerUtil.getRegexRegion(event, aqlEditor);
    			if (region.getType() == AQLPartitionScanner.AQL_REGEX) {
    				// Obtain the text of the regex
    				IDocument doc = aqlEditor.getDocumentProvider().getDocument(aqlEditor.getEditorInput());
    				String initialRegex = doc.get(region.getOffset(), region.getLength());
    				// Strip off leading and trailing /
    				initialRegex = initialRegex.substring(1, initialRegex.length() - 1);
    				RegexContainer regexContainer = new RegexContainer();
    				regexContainer.setRegex(initialRegex);
    				// Call the regex builder wizard with the regex we found in the text
    				final RegexLearnerWizard w = new RegexLearnerWizard(regexContainer);
    				final WizardDialog dialog = new WizardDialog(shell, w){
    					@Override
    					protected Control createDialogArea(Composite parent)
    					{
    						Control ctrl = super.createDialogArea(parent);
    						getProgressMonitor();
    						return ctrl;		
    					}
    					
    					@Override
    					protected IProgressMonitor getProgressMonitor()
    					{
    						ProgressMonitorPart monitor = (ProgressMonitorPart)super.getProgressMonitor();
    						GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    						gd.heightHint = 0;
    						monitor.setLayoutData(gd);
    						monitor.setVisible(false);
    						return monitor;
    					}
    					
    				};
    				final int rc = dialog.open();
    				if (rc == Window.OK) {
    					// Set the resulting regex.
    					String resultRegex = regexContainer.getRegex();
    					Assert.isNotNull(resultRegex);
    					if (resultRegex.equals(initialRegex)) {
    						return null;
    					}
    					// 	Add the regex delimiters that we removed before
    					resultRegex = RegexHandlerUtil.wrapStringAsRegex(resultRegex);
    					// Replace the regex region with the new regex in the text
    					doc.replace(region.getOffset(), region.getLength(), resultRegex);
    				}
    			} 
    			else {
    				// Give status message to the effect that the wizard can't be started here.
    				// IActionBars actionBars = aqlEditor.getEditorSite().getActionBars();
    				// actionBars.getStatusLineManager()
    				// .setErrorMessage("Cursor not inside a regular expression.");
    				// actionBars.updateActionBars();
    				final IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
    				Messages.RegexLearnerWizardCommand_CURSOR_ERROR);
    				ErrorDialog.openError(shell, Messages.RegexLearnerWizardCommand_REGEX_OPEN_ERROR, null, status);
    			}
        	}
    		else if(event.getTrigger().toString().indexOf("ToolItem") != -1) //$NON-NLS-1$
        	{
    			RegexContainer regexContainer = new RegexContainer();
    			regexContainer.setRegex(""); //$NON-NLS-1$
				RegexLearnerWizard w = new RegexLearnerWizard(regexContainer);
        		WizardDialog dialog = new CustomWizardDialog(shell, w);
        	    final Clipboard cb = new Clipboard(shell.getDisplay());
        		
        		final int rc = dialog.open();
				if (rc == Window.OK) {
					// Set the resulting regex.
					String resultRegex = regexContainer.getRegex();
					
					if(resultRegex.equals(null) || resultRegex.equals(""))
		    		{
		    			//TODO
		    		}
		    		else
		    		{
					
					// 	Add the regex delimiters that we removed before
					resultRegex = RegexHandlerUtil.wrapStringAsRegex(resultRegex);
					// Copy regex to clipboard
		    		TextTransfer textTransfer = TextTransfer.getInstance();
		    	    cb.setContents(new Object[] { resultRegex }, new Transfer[] { textTransfer });
		    	    //MessageDialog.openInformation(shell, title, resultRegex + "\n\n" + message);				
		    	    CustomMessageBox msgBox = CustomMessageBox.createInfoMessageBox(shell, Messages.RegexLearnerWizard_REGEX_GENERATOR, resultRegex + "\n\n" + Messages.ExprBuilderWizard_INFO_COPY_CLIPBOARD); //$NON-NLS-1$ //$NON-NLS-2$
					msgBox.open();    
		    		}
				}
        	}    		
    		
    		// Must return null
    		return null;
        } 
    	catch (BadLocationException e) {
    		throw new ExecutionException(Messages.RegexLearnerWizardCommand_BAD_LOCATION_EXCEPTION_MSG, e);
    	}
    }
}
