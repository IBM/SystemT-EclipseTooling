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
package com.ibm.biginsights.textanalytics.concordance.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.util.PaginationTracker;
import com.ibm.biginsights.textanalytics.resultviewer.utils.AnnotationExplorerUtil;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class is the handler class called when a result folder is right clicked and Show Result in Annotation Explorer is selected.
 *  Madiraju
 *
 */
/**
 * 
 *
 */
public class ShowResultInAnnotationExplorer extends AbstractHandler {


	
	private static final String resultViewerCommandShowNextPage = "com.ibm.biginsights.textanalytics.resultviewer.commands.ShowNextPage"; //$NON-NLS-1$
	private static final String resultViewerCommandShowPrevPage = "com.ibm.biginsights.textanalytics.resultviewer.commands.ShowPrevPage"; //$NON-NLS-1$
  private static final String resultFolderToShow = "com.ibm.biginsights.textanalytics.concordance.commandParam.resultFolder"; //$NON-NLS-1$

	private IFolder resultFolder = null;
	@Override
	public Object execute(ExecutionEvent event) {
		
		
		try {
	    String resultFolderName = event.getParameter(resultFolderToShow);
	    if (resultFolderName != null) {
	      String[] pathElems = resultFolderName.split (":");             //$NON-NLS-1$
	      IProject project = ProjectUtils.getProject (pathElems[0]);
	      IFolder mainResultFolder = project.getFolder ("result");       //$NON-NLS-1$
	      resultFolder = mainResultFolder.getFolder (pathElems[1]);
	    }
	    else {
	      // get the chosen result folder
	      resultFolder = (IFolder)ProjectUtils.getSelectedResource();
	    }

	    if (resultFolder == null || !resultFolder.exists ())
	      return null;
			
			boolean isPaginationEnabled;
			int filesPerPage;

			IPreferenceStore projectPrefStore = ProjectUtils.getPreferenceStore(resultFolder.getProject());
			if (projectPrefStore == null)
			  return null;

			isPaginationEnabled = projectPrefStore.getBoolean(Constants.PAGINATION_ENABLED);
			filesPerPage = projectPrefStore.getInt(Constants.PAGINATION_FILES_PER_PAGE);

			PaginationTracker tracker = PaginationTracker.getInstance();
			if (isPaginationEnabled) {
				tracker.setFilesPerPageCount(filesPerPage);
			} else {
				tracker.setFilesPerPageCount(0);
			}
			tracker.setResultFolder(resultFolder);
			tracker.setCurrentPage(1);
			
			
			final List<IFile> resFiles = tracker.getFiles(1);
			final IFolder tmpFolder = resultFolder.getFolder(Constants.TEMP_TEXT_DIR_NAME);
			if (tmpFolder.exists()) {
				tmpFolder.delete(true, false, new NullProgressMonitor());
			}
			final String tempDir = tmpFolder.getFullPath().toString();
			
			final ProvenanceRunParams provenanceRunParams = null; // Provenance is not required when showing the result of a past execution
			
			final IConcordanceModel concModel = AnnotationExplorerUtil.generateConcordanceModelFromFiles (resFiles,
					tempDir, provenanceRunParams, new NullProgressMonitor());
			
			concModel.setProject(resultFolder.getProject()); 
			
			final IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			/*
			 * Below snippet(4 lines) is added to fix the issue mentioned in RTC defect-51021. This is due to the view refresh problem. 
			 * drop down content is not updated when we open the results in Annotation Explorer. Below code refreshes the complete AE window
			 */
			IWorkbenchPage page = window.getActivePage ();
			IViewReference vRef = page.findViewReference (ConcordanceView.VIEW_ID);
			if(vRef!=null){
			  page.hideView (vRef);
			}
			final ConcordanceView view = (ConcordanceView) window
					.getActivePage()
					.showView(
							"com.ibm.biginsights.textanalytics.concordance.view", null, //$NON-NLS-1$
							IWorkbenchPage.VIEW_VISIBLE); //no need to give focus away. ACTIVE was causing problems while opening project preferences immediately after this.
			view.setInput(concModel);
			if (isPaginationEnabled) {
				refreshPaginationButtons();
			}
		} catch (PartInitException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.error, e);
		} catch (CoreException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.error, e);
		}
		return Status.OK_STATUS;
	}
	
	@SuppressWarnings("unused")
  private List<IFile> getResultFiles() throws CoreException {
		final IResource[] resources = resultFolder.members();
		final List<IFile> resFileList = new ArrayList<IFile>(resources.length);
		for (final IResource resource : resources) {
			if ((resource.getType() == IResource.FILE)
					&& resource.getName().endsWith(".strf")) { //$NON-NLS-1$
				if (resource.getName().startsWith(Constants.ALL_DOCS) == false)
				{
					resFileList.add((IFile) resource);
				}
			}
		}
		return resFileList;
	}
	
	/**
	 * Refresh the state of the page navigation buttons in Annotation explorer view
	 */
	private void refreshPaginationButtons() {
		 ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		  Command command = commandService.getCommand(resultViewerCommandShowNextPage);
		  IHandler nextHandler =command.getHandler();
		  nextHandler.isEnabled();
		  Command command2 = commandService.getCommand(resultViewerCommandShowPrevPage);
		  IHandler prevHandler = command2.getHandler();
		  prevHandler.isEnabled();
	}
	
}
