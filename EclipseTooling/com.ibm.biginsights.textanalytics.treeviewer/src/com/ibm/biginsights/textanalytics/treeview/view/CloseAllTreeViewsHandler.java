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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

public class CloseAllTreeViewsHandler extends AbstractTreeViewHandler {



	/**
	 * This handler is called when the user clicks on 'Show All Documents'
	 * menu on the treeview. It uses the ConcordanceMetaModel to construct the mergedModel for all documents  
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IWorkbenchPage page = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage();
		IViewReference[] views  = page.getViewReferences();

		IWorkbenchPart part = null;
		AQLResultTreeView aqlTreeView = null;
		for(IViewReference view: views)
		{
			part = view.getPart(true);
			if (part instanceof AQLResultTreeView)
			{
				aqlTreeView = (AQLResultTreeView)part;
				String secondaryID = aqlTreeView.getTitle();
				secondaryID = secondaryID.replaceAll(":", "");
				IEditorInput ieInput = AQLResultTreeView.getEditorForId(secondaryID);
		        if (ieInput != null) {
		            IEditorPart ePart = page.findEditor(ieInput);
		            if (ePart != null) {
		              page.closeEditor(ePart, false);
		            }
		          }
				page.hideView(view);
			}
		}
		return null;
	}
	
	@Override
	public void partActivated(IWorkbenchPart part) {
		this.enabled = true;
		setBaseEnabled(this.enabled);
	}
	
	@Override
	public void partOpened(IWorkbenchPart part) {
		this.enabled = true;
		setBaseEnabled(this.enabled);
	}
	
	public void refresh() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		if (commandService != null) {
			commandService
					.refreshElements(
							"com.ibm.biginsights.textanalytics.treeview.commands.CloseAll",
							null);//$NON-NLS-1$
		}
	}
}
