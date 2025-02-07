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
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.concordance.ui.ResultEditor;

public class ShowNextAnnotHandler extends AbstractTreeViewHandler {


	

	/**
	 * This handler is called when user clicks on show next annotation on a
	 * particular document.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

    IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
    if ( ! (part instanceof AQLResultTreeView) )
      return null;

    AQLResultTreeView aqlResultTreeView = (AQLResultTreeView)part;

    // When user selects something in the dropdown menu, we only do that and not navigate to next annotation.
    String outputViewTypePair = event.getParameter("com.ibm.biginsights.textanalytics.treeview.GoToNextAnnotPullDown"); //$NON-NLS-1$
    if (outputViewTypePair != null) {
      aqlResultTreeView.toggleCheckMarkInNextPrevDropdown(outputViewTypePair);
    }
    else {
      if (aqlResultTreeView.hasCheckedViewAttrsForNextPrev () || allDocsMode) {    // user may already unselect all

        ResultEditor editor = getEditorForThisTreeView(event);
        if (null != editor) {
	        Annotation nextAnnot = editor.gotoMyAnnotation (allDocsMode, true); // 'true' makes it go forward
	
	        // Since we now allow filtering the annotations thru the dropdown 'Next' menu, loop
	        // until reach the appropriate annotation.
	        while (!(nextAnnot == null || aqlResultTreeView.isAnnotationOKToNavigate (nextAnnot))) {
	          nextAnnot = editor.gotoMyAnnotation (allDocsMode, true); // true makes it go forward
	        }
        }
      }
    }

    return null;
	}
	
	public void refresh() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		if (commandService != null) {
			commandService
					.refreshElements(
							"com.ibm.biginsights.textanalytics.treeview.commands.GoToNextAnnot",
							null);//$NON-NLS-1$
		}
	}
}
