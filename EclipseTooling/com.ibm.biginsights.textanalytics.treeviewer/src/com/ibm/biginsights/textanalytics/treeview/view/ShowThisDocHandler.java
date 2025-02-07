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
package com.ibm.biginsights.textanalytics.treeview.view;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class ShowThisDocHandler extends AbstractTreeViewHandler {



	/**
	 * This handler is called when user clicks on a particular document.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String inputDocName = event
				.getParameter("com.ibm.biginsights.textanalytics.treeview.gotodocpulldown"); //$NON-NLS-1$
		initialize(event);
		if (inputDocName == null && metaModel != null)
		{
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowInfo(
					Messages.ShowThisDocHandler_SelectDocument);
			return null;
		}
		if (metaModel != null) {
			SystemTComputationResult result = metaModel.getResult(inputDocName);
			showEditorForResult(result);
		}	
		return null;
	}
	
	public void refresh() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		if (commandService != null) {
			commandService
					.refreshElements(
							"com.ibm.biginsights.textanalytics.treeview.commands.GoToDoc",
							null);//$NON-NLS-1$
		}
	}
}
