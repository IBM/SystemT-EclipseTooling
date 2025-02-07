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
package com.ibm.biginsights.textanalytics.concordance.ui.pagination;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.ui.ConcordanceView;
import com.ibm.biginsights.textanalytics.resultviewer.util.PaginationTracker;
import com.ibm.biginsights.textanalytics.resultviewer.utils.AnnotationExplorerUtil;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * Abstract base class for Pagination feature - subclassed by ShowNextPageHandler and ShowPrevPageHandler
 *  Madiraju
 *
 */
public abstract class AbstractPageHandler extends AbstractHandler {


	
	protected static final String resultViewerCommandShowNextPage = "com.ibm.biginsights.textanalytics.resultviewer.commands.ShowNextPage"; //$NON-NLS-1$
	protected static final String resultViewerCommandShowPrevPage = "com.ibm.biginsights.textanalytics.resultviewer.commands.ShowPrevPage"; //$NON-NLS-1$

	 protected IFolder tempFolder = null;
	 protected IFolder resultFolder = null;
	 protected PaginationTracker tracker = PaginationTracker.getInstance();

	 /**
	  * This method displays the page in the ConcordanceView with result files of that page.
	  * @param resFiles
	  * @param originTableView - Name of the table view if the command was issued from a table view, else an empty string.
	  * @throws CoreException
	  */
	  protected void displayPage(List<IFile> resFiles, String originTableView) throws CoreException
	  {
		    resultFolder = tracker.getResultFolder();
			final IFolder tmpFolder = resultFolder.getFolder(Constants.TEMP_TEXT_DIR_NAME);
			if (tmpFolder.exists()) {
				tmpFolder.delete(true, false, new NullProgressMonitor());
			}
			final String tempDir = tmpFolder.getFullPath().toString();
			final ProvenanceRunParams provenanceRunParams = (ProvenanceRunParams)tracker.getProvParams(); 
			final IConcordanceModel concModel2 = AnnotationExplorerUtil.generateConcordanceModelFromFiles (resFiles,tempDir, provenanceRunParams, new NullProgressMonitor());
			concModel2.setProject(resultFolder.getProject());
			final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			final ConcordanceView view = (ConcordanceView) window.getActivePage().showView(	"com.ibm.biginsights.textanalytics.concordance.view", null, //$NON-NLS-1$
							IWorkbenchPage.VIEW_ACTIVATE);
			view.setInput(concModel2,originTableView);
	  }
	 
	  /**
	   * This method is called in the base classes to ensure the prev and next page buttons are enabled appropriately.
	   * Note that the method calls the isEnabled method of both the handlers for every call to next/prev page.
	   * isEnabled() of the subclasses in turn will have the logic whether the buttons should be enabled or not.
	   */
	  public void enableDisableButtons(){
		  ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		  Command command = commandService.getCommand(resultViewerCommandShowNextPage);
		  IHandler nextHandler =command.getHandler();
		  nextHandler.isEnabled();
		  Command command2 = commandService.getCommand(resultViewerCommandShowPrevPage);
		  IHandler prevHandler = command2.getHandler();
		  prevHandler.isEnabled();
	  }
}
