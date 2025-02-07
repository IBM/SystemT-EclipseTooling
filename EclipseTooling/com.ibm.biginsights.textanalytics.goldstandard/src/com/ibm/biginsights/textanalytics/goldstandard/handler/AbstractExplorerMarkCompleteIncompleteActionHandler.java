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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Abstract base class for mark complete/incomplete actions executed from package or project explorer
 *  Krishnamurthy
 *
 */
public abstract class AbstractExplorerMarkCompleteIncompleteActionHandler extends GSActionHandler {



	protected int ACTION_TYPE;
	
	protected static final int ACTION_TYPE_MARK_COMPLETE = 0;
	protected static final int ACTION_TYPE_MARK_INCOMPLETE = 1;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(super.execute(event) == null){
			return null;
		}
		
		IResource selectedResource = ProjectUtils.getSelectedResource();
		if(selectedResource == null){
			return null;
		}
		if(selectedResource instanceof IFolder){
			IFolder folder = (IFolder)selectedResource;
			if(!GoldStandardUtil.isGoldStandardFolder(folder)){
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
				Messages.AbstractMarkCompleteIncompleteActionDelegate_SELECT_GS_FOLDER_OR_FILE);
				return null;
			}else{
				/**
				 * Note: Do not spawn a new thread for executing the following action on a folder.
				 * Marking a file as complete or incomplete requires reloading of already opened .lc files
				 * in order to refresh the gsComplete flag within the in-memory model. However, if we spawn a new
				 * thread to run this action on a folder, then it can not access Eclipse's active workbench window
				 * since only UI threads are allowed access to Eclipse's active window.
				 */
				doExecute(folder);
			}
		}else if(selectedResource instanceof IFile){
			IFile file = (IFile)selectedResource;
			if(file.getFileExtension().equals(Constants.GS_FILE_EXTENSION)){
				doExecute(file);
			}else{
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
						Messages.AbstractMarkCompleteIncompleteActionDelegate_SELET_GS_FILE);
				return null;
			}
		}
		
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * Subclasses override this method to specify a description of activity carried out by them.
	 * The description returned by this method is used for displaying a UI message to the user
	 * @return description of activity executed by concrete subclasses.
	 */
	protected abstract String getActivityDescription();

	/**
	 * Template method to be implemented by the subclass.
	 * Known subclasses are ExplorerMarkCompleteHander and ExplorerMarkIncompleteHandler 
	 * @param file the file to be marked as complete or incomplete.
	 * @return true, if the mark complete/incomplete task is successful. false, otherwise.
	 */
	protected abstract boolean doExecute(IFile file);
	
	private void doExecute(IFolder folder) {
		try {
			IResource[] members = folder.members();
			if(members!= null){
				for (IResource member : members) {
					if(member instanceof IFile){
						IFile file = (IFile)member;
						if(file.getFileExtension().equals(Constants.GS_FILE_EXTENSION)){
							doExecute(file);
						}
					}
				}//end: for each member
			}
		} catch (CoreException e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
		}
		
	}
}
