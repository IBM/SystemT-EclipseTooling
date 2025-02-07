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
package com.ibm.biginsights.textanalytics.resultdifferences.handler;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.resultdifferences.Activator;
import com.ibm.biginsights.textanalytics.resultdifferences.Messages;
import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollDiffModel;
import com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
/*
 * Handler that is used to compare the selected analysis result with another
 * analysis result and display the analysis differences view. When this action is
 * executed a dialog shows up that contains the other analysis results that are
 * available for comparison.
 */

public class CompareWithOtherResultHandler extends AbstractHandler {


	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// get the chosen result folder
		final IFolder leftFolder = (IFolder)ProjectUtils.getSelectedResource();

		// get the analysis result collection folder as input for the dialog
	    IFolder resultFolder = (IFolder) leftFolder.getParent();
	    IResource[] members = null;
	    try {
			members = resultFolder.members();
		} catch (CoreException e1) {
			//do nothing
		}
	    if(members == null || members.length == 1){
	    	//This means that there is no other result folder to compare with.
	    	LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowWarning(Messages.getString("CompareWithLabelledCollectionHandler_NoFolderToCompare"));
	    	return null;
    }
	    
	    ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(ProjectUtils.getActiveWorkbenchWindow().getShell(), 
				new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		//IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
	    dialog.setInput(ProjectUtils.getRootResultFolder(leftFolder.getProject()));
	    dialog.setMessage(Messages.getString("ResultDifferencesViewAction_Select_result_folder")); //$NON-NLS-1$
		dialog.setTitle(Messages.getString("ResultDifferencesViewAction_Select_result_folder")); //$NON-NLS-1$
		

		
		dialog.addFilter(new ViewerFilter() {

		      public boolean select(Viewer viewer, Object parentElement, Object element) {
		    	  String resourceName = ((IResource)element).getName();
		    	  if ((element instanceof IFolder) && (ProjectUtils.isResultFolder((IFolder)element)) && (resourceName.equals(leftFolder.getName()) != true))  //$NON-NLS-1$
		    	  {
		    		  // Show only folders, that too only if they qualify as result folders
		    		  // Also dont show the same folder as the one against which comparison was invoked
		            return true;
		    	  }
		    	  else
		    	  {
			        return false;
			      }
	    	  }

		    });
			int dialogStatus = dialog.open();
		    if (dialogStatus == Window.OK) {
		    	ResultDifferencesUtil.hidePrevCollDiffView();
		
		        if (dialog.getFirstResult() instanceof IFolder) {
		
			        IFolder rightFolder = (IFolder)dialog.getFirstResult();

			        // check if the view is visible, otherwise open
			        // it
			        CollectionDifferencesMainView collDiffView = null;
			        try {
			          collDiffView = (CollectionDifferencesMainView) ProjectUtils.getActivePage().showView(
			              CollectionDifferencesMainView.ID);
			          collDiffView.setComparedWithGoldStandard(false);

			        } catch (PartInitException e) {
			        	e.printStackTrace();
			        }
			        
			        if (ResultDifferencesUtil.checkFilesInBothFoldersAreAccessible(leftFolder,rightFolder) == false)
			    	{
				    	LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowWarning(Messages.getString("ResultDifferencesViewAction_non_matching_result_folders"));
				    	return null;
			    	}
			        else{
			        
					        CollDiffModel collDiffModel = CollDiffModel.getInstance(rightFolder, leftFolder);
					        // initialize the view
					        if (collDiffView != null) {
					          collDiffView.init(collDiffModel);
					        }
			        }
		        }
		}
		return Status.OK_STATUS;
	}
}
