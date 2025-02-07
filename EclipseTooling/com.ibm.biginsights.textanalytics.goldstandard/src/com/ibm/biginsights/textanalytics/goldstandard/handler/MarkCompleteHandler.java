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
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.model.GoldStandardModel;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class handles the click event on popup menu item on the GS editor: 'MarkComplete'. 
 * This action marks the gs file as complete and no more edits are possible.
 * 
 * There is another way to mark a gs file or gsfolder as complete, which is accessed through the context menu of a gsFolder or a gsFile 
 * in the project or package explorer. The handler for that scenario is defined in ExplorerMarkCompleteHandler.
 * 
 *  Krishnamurthy
 *
 */
public class MarkCompleteHandler extends AbstractHandler implements IHandler {



	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		EditorInput editorInput = (EditorInput) GoldStandardUtil.getGSEditor().getEditorInput();
		GoldStandardModel model = (GoldStandardModel) editorInput.getModel();
		if(model.gsComplete){
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowInfo(Messages.MarkCompleteActionDelegate_ALREADY_MARKED_COMPLETE);
			return null;
		}
		
		//mark it as complete
		model.gsComplete = true;
		boolean success = GoldStandardUtil.serializeModel(model, (IFile)editorInput.getUserData());
		if(success){
			String msg = Messages.MarkCompleteHandler_MARKED_AS_COMPLETE_TO_ADD_OR_DEL_MARK_AS_INCOMPLETE;
			String formattedMsg = MessageUtil.formatMessage(msg, ((IFile)editorInput.getUserData()).getName());
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowInfo(formattedMsg);
		}else{
			//revert gsComplete value
			model.gsComplete = false;
		}
		
		IFile gsFile = (IFile)editorInput.getUserData();
		GoldStandardUtil.reopenGSEditor(gsFile, editorInput);
				
		return null;
	}

}
