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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class handles the click event on popup menu 'Labeled Document Collection -> Mark Complete'
 * invoked on a gsFolder or gsFile from the package or project explorer. 
 *
 *  Krishnamurthy
 *
 */
public class ExplorerMarkCompleteHandler extends
	AbstractExplorerMarkCompleteIncompleteActionHandler {


	
	public ExplorerMarkCompleteHandler() {
		super();
		super.ACTION_TYPE = ACTION_TYPE_MARK_COMPLETE;
	}

	@Override
	protected boolean doExecute(IFile file) {
		try {
			Serializer ser = new Serializer();
			SystemTComputationResult model = ser.getModelForInputStream(file.getContents());
			if(model != null){
				if(model.gsComplete == false){
					model.gsComplete = true;
					GoldStandardUtil.serializeModel(model,file);
					
					//reopen editor if already open, so that the gsComplete flag is refreshed 
					//in the in-memory model of already opened editor
					GoldStandardUtil.reopenGSFileIfAlreadyOpened(file);
					return true;
				}else{
					return false;
				}
			}
		} catch (CoreException e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage(), e);
		}
		return false;
	}



	@Override
	protected String getActivityDescription() {
		return Messages.MarkCompleteActionDelegate_MARKING_GS_FILE_AS_COMPLETE;
	}

}
