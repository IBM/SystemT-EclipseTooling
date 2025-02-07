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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 *  Krishnamurthy
 *
 */
public abstract class AbstractMarkCompleteIncompleteActionDelegate extends
		GSActionDelegate {



	@Override
	public void run(IAction action) {
		super.run(action);
		if(error){
			return;
		}
		
		IResource selectedResource = ProjectUtils.getSelectedResource();
		if(selectedResource == null){
			error = true;
			return;
		}
		if(selectedResource instanceof IFolder){
			IFolder folder = (IFolder)selectedResource;
			if(!GoldStandardUtil.isGoldStandardFolder(folder)){
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
				Messages.AbstractMarkCompleteIncompleteActionDelegate_SELECT_GS_FOLDER_OR_FILE);
				error = true;
				return;
			}else{
				//doExecute(folder);
				Job systemtJob = new MarkerJob(getActivityDescription(), folder);
				systemtJob.setUser(true);
				systemtJob.schedule();
			}
		}else if(selectedResource instanceof IFile){
			IFile file = (IFile)selectedResource;
			if(file.getFileExtension().equals(Constants.GS_FILE_EXTENSION)){
				doExecute(file);
			}else{
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
						Messages.AbstractMarkCompleteIncompleteActionDelegate_SELET_GS_FILE);
				error = true;
				return;
			}
		}
	}

	protected abstract String getActivityDescription();

	protected abstract void doExecute(IFile file);


	
	private class MarkerJob extends Job {
		IFolder folder;
		
		public MarkerJob(String name, IFolder folder) {
			super(name);
			this.folder = folder;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			doExecute(monitor);
			return Status.OK_STATUS;
		}
		
		protected void doExecute(IProgressMonitor monitor) {

			try {
				IResource[] members = folder.members();
				if(members!= null){
					monitor.beginTask(getName(), members.length);
					for (IResource member : members) {
						if(member instanceof IFile){
							IFile file = (IFile)member;
							monitor.subTask(Messages.AbstractMarkCompleteIncompleteActionDelegate_PROCESSING_FILE+file.getName());
							if(file.getFileExtension().equals(Constants.GS_FILE_EXTENSION)){
								AbstractMarkCompleteIncompleteActionDelegate.this.doExecute(file);
							}
						}
						monitor.worked(1);
					}
				}

			} catch (CoreException e) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
			}
			
		}
	}
	
}
