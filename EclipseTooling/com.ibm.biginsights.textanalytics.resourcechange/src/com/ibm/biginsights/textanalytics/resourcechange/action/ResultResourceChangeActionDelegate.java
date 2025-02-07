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
package com.ibm.biginsights.textanalytics.resourcechange.action;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;

import com.ibm.biginsights.textanalytics.resourcechange.Messages;
import com.ibm.biginsights.textanalytics.resourcechange.ResourceChangePlugin;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Handles the following events
 * a) add or move of result folders
 * b) rename of result root dir
 * 
 *  Krishnamurthy
 *
 */
public class ResultResourceChangeActionDelegate extends
		AbstractResourceChangeActionDelegate {



	private static ILog logger = LogUtil.getLogForPlugin(ResourceChangePlugin.PLUGIN_ID);
	
	/**
	 * @param removedResource
	 * @param addedResource
	 */
	public ResultResourceChangeActionDelegate(IResource removedResource,
			IResource addedResource) {
		super(removedResource, addedResource);
	}

	/* (non-Javadoc)
	 * @see com.ibm.biginsights.textanalytics.resourcechange.action.AbstractResourceChangeActionDelegate#handleRenameAction()
	 */
	@Override
	protected void handleRenameAction() {
		if(removedResource instanceof IFolder){
			IFolder folder = (IFolder)removedResource;
			if(ProjectUtils.isResultRootDir(folder)){
				ProjectUtils.setResultRootDir((IFolder)addedResource);
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.ibm.biginsights.textanalytics.resourcechange.action.AbstractResourceChangeActionDelegate#handleDeleteAction()
	 */
	@Override
	protected void handleDeleteAction() {
		// do nothing, as we are not interested in result folder delete events

	}

	@Override
	protected void handleAddAction() {

		String RESULT_PARENT_DIR_SKIP_LIST = ",.aog,bin,src,.temp,.settings,labeledCollections,"; //$NON-NLS-1$
		final IResource parent = addedResource.getParent();
		if(parent instanceof IProject){
			copyToResultParentFolder(parent, Messages.ResultResourceChangeActionDelegate_CANNOT_COPY_DIRECTLY_UNDER_PROJECT);
		}else if(parent instanceof IFolder){
			IFolder parentFolder = (IFolder)parent;
			if(RESULT_PARENT_DIR_SKIP_LIST.contains(","+parentFolder.getName()+",")){ //$NON-NLS-1$ //$NON-NLS-2$
				copyToResultParentFolder(parent, Messages.ResultResourceChangeActionDelegate_CANNOT_COPY_UNDER_RESTRICTED_FOLDER);
			}else {
				IFolder resultParentDir = ProjectUtils.getRootResultFolder(addedResource.getProject());
				if(resultParentDir.exists()){
					//Result is copied to a directory other than RESULT_ROOT_DIR (even though one exists). So, copy to RESULT_ROOT_DIR
					if(!resultParentDir.getName().equals(parent.getName())){
						copyToResultParentFolder(parent, Messages.ResultResourceChangeActionDelegate_CANNOT_COPY_TO_NON_RESULT_ROOT_DIR);
					}else{
						//do nothing. The resource is copied to the correct RESULT_ROOT_DIR only.
						return;
					}
				}else{
					//RESULT_ROOT_DIR does not exist. So, treat the current result folder's parent dir as RESULT_ROOT_DIR
					ProjectUtils.setResultRootDir(parentFolder);
				}
			}//end: else RESULT_PARENT_DIR_SKIP_LIST(parentFolder.getName())
		}//end: if parent instanceof IFolder
	}
	
	private void copyToResultParentFolder(final IResource parent, final String message) {
	    Display.getDefault ().syncExec(new Runnable () {
	        @Override
	        public void run ()
	        {
				IFolder resultRootDir = ProjectUtils.getRootResultFolder(parent.getProject());
				try {
					IFolder moveToFolder = resultRootDir.getFolder(addedResource.getName());
					addedResource.move(moveToFolder.getFullPath(), true, null);
					resultRootDir.refreshLocal(IResource.DEPTH_INFINITE, null);
					if(!StringUtils.isEmpty(message)){
						logger.logAndShowWarning(MessageUtil.formatMessage(message, resultRootDir.getName()));
					}
				} catch (CoreException e) {
					logger.logAndShowError(e.getLocalizedMessage(), e);
				}
	        }
	    	}); 
	}
	
	public void run(){
		
		super.run();
		
		boolean valid = validateResourceChangeEvent();
		if(!valid){
			return;
		}
		
		switch(resourceChangeType){
		case Constants.RESCHNG_ADD:
			handleAddAction();
			break;
		case Constants.RESCHNG_DELETE:
			handleDeleteAction();
			break;
		case Constants.RESCHNG_RENAME:
			handleRenameAction();
			break;
		}
	}

}
