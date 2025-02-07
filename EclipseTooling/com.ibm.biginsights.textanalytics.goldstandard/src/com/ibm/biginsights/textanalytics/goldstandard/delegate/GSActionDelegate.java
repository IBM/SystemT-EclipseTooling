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
package com.ibm.biginsights.textanalytics.goldstandard.delegate;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Abstract base class for all GoldStandard related action delegate classes.
 * Provides support for creating GS_PARENT_DIR, if one does not exist.
 * 
 *  Krishnamurthy
 *
 */
public abstract class GSActionDelegate implements IObjectActionDelegate {


	
	protected IProject selectedProject;
	protected IFolder gsParentFolder;
	protected boolean error = false;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		selectedProject = GoldStandardUtil.getSelectedProject();
		
		if(selectedProject == null){
			CustomMessageBox msgBox = CustomMessageBox.createErrorMessageBox(
					ProjectUtils.getActiveWorkbenchWindow().getShell(), 
					Messages.GSActionHandler_ERROR, Messages.GSActionHandler_PLEASE_SELECT_PROJECT);
			msgBox.open();
			error = true;
			return;
		}
		
		gsParentFolder = GoldStandardUtil.getGSParentFolder(selectedProject, true);
		if(gsParentFolder == null){
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(Messages.GSActionHandler_GSFOLDER_NULL);
			error = true;
			return;
		}else if(!gsParentFolder.exists()){
			try {
				gsParentFolder.create(false, true, null);
				GoldStandardUtil.setGoldStandardParentDir(gsParentFolder);
			} catch (CoreException e) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(Messages.GSActionHandler_GSFOLDER_CREATION_FAILED+e.getLocalizedMessage());
				error = true;
				
				//At this point of time, gsParentFolder might exist, if create()succeeded, but setPersistentProperty() failed.
				//So, clean it up.
				if(gsParentFolder.exists()){
					try {
						gsParentFolder.delete(true, null);
					} catch (CoreException e1) {
						LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
								MessageUtil.formatMessage(Messages.GSActionDelegate_UNABLE_TO_DELETE_GS_PARENT_DIR, gsParentFolder.getName()), e1);
					}
				}
				return;
			}
			
		}
		


	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
