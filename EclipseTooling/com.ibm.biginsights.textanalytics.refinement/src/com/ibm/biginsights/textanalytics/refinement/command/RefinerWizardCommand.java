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
package com.ibm.biginsights.textanalytics.refinement.command;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLPartitionScanner;
import com.ibm.biginsights.textanalytics.refinement.Activator;
import com.ibm.biginsights.textanalytics.refinement.run.RefinerJob;
import com.ibm.biginsights.textanalytics.refinement.ui.RefinerWizard;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

public class RefinerWizardCommand extends AbstractHandler {



	public RefinerWizardCommand() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {

			// IProject project = ProjectPreferencesUtil.getSelectedProject();
			// System.err.printf("Project '%s'\n", project.getName());

			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();

			IEditorPart editor = HandlerUtil.getActiveEditor(event);

			Assert.isNotNull(editor);
			Assert.isLegal(editor instanceof AQLEditor);
			AQLEditor aqlEditor = (AQLEditor) editor;
			// We use the document partitioning to find out if we're inside a
			// create view statement
			ITypedRegion region = ViewDefinitionHandlerUtil.getRegion(event,
					aqlEditor);
			if (region.getType() == AQLPartitionScanner.AQL_VIEW_NAME) {
				// Obtain the text of the view name
				IDocument doc = aqlEditor.getDocumentProvider().getDocument(
						aqlEditor.getEditorInput());
				String aqlSnippet = doc.get(region.getOffset(),
						region.getLength());
				String viewName = StringUtils.getViewName(aqlSnippet);

				System.err.printf("Clicked on view '%s'\n", viewName);

				RefinerContainer refinerContainer = new RefinerContainer();
				refinerContainer.setProperty(Constants.REFINER_VIEW_NAME_PROP,
						viewName);

				// Call the regex builder wizard with the regex we found in the
				// text
				final RefinerWizard w = new RefinerWizard(refinerContainer);
				final WizardDialog dialog = new WizardDialog(shell, w);
				final int rc = dialog.open();
				if (rc == Window.OK) {
					// Set the resulting regex.
					String vname = refinerContainer
							.getProperty(Constants.REFINER_VIEW_NAME_PROP);
					Assert.isNotNull(vname);

					// Printout result to console
					MessageConsole myConsole = findConsole(Constants.REFINER_CONSOLE_NAME);					
					MessageConsoleStream out = myConsole.newMessageStream();
					
					out.println("\nStarting AQL Refinement ...");
							
					out.println("Project: "
							+ refinerContainer
									.getProperty(Constants.REFINER_PROJECT_NAME_PROP));
					
					out.println("View name: "
							+ refinerContainer
									.getProperty(Constants.REFINER_VIEW_NAME_PROP));
					
					out.println("Configuration: "
							+ refinerContainer
									.getProperty(Constants.REFINER_CONFIG_PATH_PROP));

					out.println("Train data path: "
							+ refinerContainer
									.getProperty(Constants.REFINER_DATA_PATH_PROP));
					out.println("Label path: "
							+ refinerContainer
									.getProperty(Constants.REFINER_LABEL_PATH_PROP));					

					RefinerJob job = new RefinerJob("Generating refinements",
							refinerContainer, out);
					job.schedule();
				}

			} else {
				final IStatus status = new Status(IStatus.ERROR,
						Activator.PLUGIN_ID,
						"Place the cursor inside a view name to open the AQL Refiner builder.");
				ErrorDialog.openError(shell, "Error opening AQL refiner.",
						null, status);
			}

			return null;

		} catch (BadLocationException e) {
			throw new ExecutionException("Bad location starting AQL Refiner.",
					e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExecutionException(
					"Starting AQL Refiner caused exception", e);
		}
	}

	
	/**
	 * Create a handle to the console view.
	 * 
	 * @param name
	 * @return
	 * @throws PartInitException
	 */
	private MessageConsole findConsole(String name) throws PartInitException {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		
		// Show the console view
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view = (IConsoleView) page.showView(id);
		view.display(myConsole);					  
		
		return myConsole;
	}

}
