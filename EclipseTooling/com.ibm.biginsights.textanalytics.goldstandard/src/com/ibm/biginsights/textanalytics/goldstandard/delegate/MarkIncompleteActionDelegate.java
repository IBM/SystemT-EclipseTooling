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
import org.eclipse.core.runtime.CoreException;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 *  Krishnamurthy
 *
 */
public class MarkIncompleteActionDelegate extends AbstractMarkCompleteIncompleteActionDelegate {


	
	@Override
	protected void doExecute(IFile file) {
		try {
			Serializer ser = new Serializer();
			SystemTComputationResult model = ser.getModelForInputStream(file.getContents());
			if(model != null){
				if(model.gsComplete == true){
					model.gsComplete = false;
					GoldStandardUtil.serializeModel(model,file);
				}else{
					LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowInfo(Messages.MarkIncompleteActionDelegate_ALREADY_MARKED_INCOMPLETE);
					return;
				}
			}

		} catch (CoreException e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
		}
	}
	
	@Override
	protected String getActivityDescription() {
		return Messages.MarkIncompleteActionDelegate_MARK_GS_FILE_AS_INCOMPLETE;
	}

}
