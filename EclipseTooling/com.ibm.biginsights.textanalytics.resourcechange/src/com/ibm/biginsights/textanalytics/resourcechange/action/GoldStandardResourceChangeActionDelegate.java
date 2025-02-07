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

import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resourcechange.Messages;
import com.ibm.biginsights.textanalytics.resourcechange.ResourceChangePlugin;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Handles the following events:
 * a) Add (or) move of GS folder
 * b) Rename of GS Parent folder
 * 
 *  Krishnamurthy
 *
 */
public class GoldStandardResourceChangeActionDelegate extends
		AbstractResourceChangeActionDelegate {


	
	private static ILog logger = LogUtil.getLogForPlugin(ResourceChangePlugin.PLUGIN_ID);

	public GoldStandardResourceChangeActionDelegate(IResource removedResource,
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
			if(GoldStandardUtil.isGoldStandardParentDir(folder)){
				GoldStandardUtil.setGoldStandardParentDir((IFolder)addedResource);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ibm.biginsights.textanalytics.resourcechange.action.AbstractResourceChangeActionDelegate#handleDeleteAction()
	 */
	@Override
	protected void handleDeleteAction() {
		//do nothing, as we are not interested in GS delete events
	}
	
	@Override
	protected void handleAddAction() {
		String GS_PARENT_DIR_SKIP_LIST = ",.aog,bin,src,.temp,.settings,result,"; //$NON-NLS-1$
		final IResource parent = addedResource.getParent();
		if(parent instanceof IProject){
			copyToGSParentFolder(parent, Messages.GoldStandardResourceChangeActionDelegate_CANNOT_COPY_DIRECTLY_UNDER_PROJECT);
		}else if(parent instanceof IFolder){
			IFolder parentFolder = (IFolder)parent;
			if(GS_PARENT_DIR_SKIP_LIST.contains(","+parentFolder.getName()+",")){ //$NON-NLS-1$ //$NON-NLS-2$
				copyToGSParentFolder(parent, Messages.GoldStandardResourceChangeActionDelegate_CANNOT_COPY_TO_RESTRICTED_FOLDER);
			}else {
				IFolder gsParentDir = GoldStandardUtil.getGSParentFolder(addedResource.getProject(), false);
				if(gsParentDir.exists()){
					//LC is copied to a directory other than LC_PARENT_DIR (even though one exists). So, copy to LC_PARENT_DIR
					if(!gsParentDir.getName().equals(parent.getName())){
						copyToGSParentFolder(parent, Messages.GoldStandardResourceChangeActionDelegate_CANNOT_COPY_TO_NON_LC_ROOT_DIR);
					}else{
						//do nothing. The resource is copied to the correct LC_ROOT_DIR only.
						return;
					}
				}else{
					//LC_PARENT_DIR does not exist. So, treat the current GS folder's parent dir as LC_PARENT_DIR
					GoldStandardUtil.setGoldStandardParentDir(parentFolder);
				}
			}//end: else GS_PARENT_DIR_SKIP_LIST.contains(parentFolder.getName())
		}//end: if parent instanceof IFolder
	}
	
	private void copyToGSParentFolder(final IResource parent, final String message) {
	    Display.getDefault ().syncExec(new Runnable () {
	        @Override
	        public void run ()
	        {
				IFolder gsParentFolder = GoldStandardUtil.getGSParentFolder(parent.getProject(), true);
				try {
					IFolder moveToFolder = gsParentFolder.getFolder(addedResource.getName());
					addedResource.move(moveToFolder.getFullPath(), true, null);
					gsParentFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
					if(!StringUtils.isEmpty(message)){
						logger.logAndShowWarning(MessageUtil.formatMessage(message, gsParentFolder.getName()));
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
