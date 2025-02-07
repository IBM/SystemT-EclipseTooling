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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Abstract base class for all Gold Standard action handlers.
 * This class carries out validations such as verifying if a project is selected or not.
 * This class also creates the goldstandard folder, if it does not exist.
 * 
 *
 */
public abstract class GSActionHandler extends AbstractHandler {


	
	protected IProject selectedProject;
	protected IFolder gsParentFolder;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		selectedProject = GoldStandardUtil.getSelectedProject();
		
		if(selectedProject == null){
			CustomMessageBox msgBox = CustomMessageBox.createErrorMessageBox(
					ProjectUtils.getActiveWorkbenchWindow().getShell(), 
					Messages.GSActionHandler_ERROR, Messages.GSActionHandler_PLEASE_SELECT_PROJECT);
			msgBox.open();
			return null;
		}
		
		gsParentFolder = GoldStandardUtil.getGSParentFolder(selectedProject, true);
		if(gsParentFolder == null){
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(Messages.GSActionHandler_GSFOLDER_NULL);
			return null;
		}else if(!gsParentFolder.exists()){
			try {
				gsParentFolder.create(false, true, null);
				GoldStandardUtil.setGoldStandardParentDir(gsParentFolder);
			} catch (CoreException e) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(Messages.GSActionHandler_GSFOLDER_CREATION_FAILED+e.getLocalizedMessage());
				
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

				return null;
			}
		}
		return ""; //$NON-NLS-1$
	}

}
