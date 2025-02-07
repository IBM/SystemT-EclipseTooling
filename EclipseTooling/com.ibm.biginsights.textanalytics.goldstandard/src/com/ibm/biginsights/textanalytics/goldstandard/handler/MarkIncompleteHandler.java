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
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class handles the click event on menu item 'Text Analytics -> Gold Standard -> Completeness -> Mark Incomplete'
 * This marks the gs file as incomplete allowing for further labeling
 *  Krishnamurthy
 *
 */
public class MarkIncompleteHandler extends AbstractHandler implements IHandler {



	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		EditorInput editorInput = (EditorInput) GoldStandardUtil.getGSEditor().getEditorInput();
		GoldStandardModel model = (GoldStandardModel) editorInput.getModel();
		if(!model.gsComplete){
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowInfo(Messages.MarkIncompleteActionDelegate_ALREADY_MARKED_INCOMPLETE);
			return null;
		}
		
		//mark it as incomplete
		model.gsComplete = false;
		boolean success = GoldStandardUtil.serializeModel(model, (IFile)editorInput.getUserData());
		if(!success){
			//revert gsComplete value
			model.gsComplete = true;
		}
		
		IFile gsFile = (IFile)editorInput.getUserData();
		GoldStandardUtil.reopenGSEditor(gsFile, editorInput);
		return null;
	}

}
