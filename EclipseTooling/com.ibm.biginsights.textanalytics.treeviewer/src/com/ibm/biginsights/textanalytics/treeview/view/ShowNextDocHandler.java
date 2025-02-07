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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.util.common.Constants;

public class ShowNextDocHandler extends AbstractTreeViewHandler {



	/**
	 * This handler class is called when the user clicks on 'Show Next Document'
	 * For now this is based on the sorting of the input document
	 * names.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		long l1 = System.currentTimeMillis();
		initialize(event);
		if (metaModel != null) {
			IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
			if (part instanceof AQLResultTreeView) {
				AQLResultTreeView treeView = (AQLResultTreeView) part;
				String thisDoc = treeView.getTitle();
				if (thisDoc.contains(Constants.ALL_DOCS)) {
					ShowNextAnnotHandler handler = new ShowNextAnnotHandler();
					handler.setAllDocsMode(true);
					handler.execute(event);
					return null;
				} else {
					if (isLC) {
						thisDoc = replaceLCExtn(lcFile.getName());
					} else {
						thisDoc = partName;
					}
					// the parameter true indicates show next
					// the parameter null is passed because we need to show next
					// irrespective
					// of whether it contains the selected annotation or not
					// Constrast this with the ShowNextDocWithAnnotationHandler.

					result = metaModel.getNextPrevious(thisDoc, true, null,
							docSchema);
					showEditorForResult(result);
				}
			}
		}
		long l2 = System.currentTimeMillis();
//		System.out.println("#####################Time taken for ShowNextDocHandler" + (l2-l1) + " m-secs");
		return null;
	}
	
	public void refresh() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		if (commandService != null) {
			commandService
					.refreshElements(
							"com.ibm.biginsights.textanalytics.treeview.commands.ShowNext",
							null);//$NON-NLS-1$
		}
	}
}
