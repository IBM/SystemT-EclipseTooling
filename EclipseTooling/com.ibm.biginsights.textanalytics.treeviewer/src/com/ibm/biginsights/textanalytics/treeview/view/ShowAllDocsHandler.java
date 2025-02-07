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

import java.util.ArrayList;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class ShowAllDocsHandler extends AbstractTreeViewHandler {



	/**
	 * This handler is called when the user clicks on 'Show All Documents'
	 * menu on the treeview. It uses the ConcordanceMetaModel to construct the mergedModel for all documents  
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		long l1 = System.currentTimeMillis();
		initialize(event);
		if (metaModel != null) {
			// IWorkbenchPart activePart =
			// HandlerUtil.getActivePartChecked(event);
			String lExtn = null;
			if (isLC) {
				lExtn = Constants.GS_FILE_EXTENSION_WITH_DOT
						+ Constants.STRF_FILE_EXTENSION_WITH_DOT;
			} else {
				lExtn = Constants.STRF_FILE_EXTENSION_WITH_DOT;
			}
			// seperateAndDetermineDocumentSchemaInfoFromTitle(activePart.getTitle());
			// // we are doing this just to get the docschema in this case,
			// String docID = Constants.ALL_DOCS+lExtn;
			// System.out.println("In ShowAllDocsHandler, the docSchema is "
			// +docSchema);
			result = metaModel.getMergedModelForAllDocuments(
					new ArrayList<String>(), Constants.ALL_DOCS + lExtn,
					docSchema);
			if (result.getInputText() == null) {
				LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowWarning(
						Messages.NoResultToDisplay);
				return null;
			} else {
				IEditorPart part = showEditorForResult(result);
				serializeResultFile(result);
				annotateDocumentNamesInAllDocs(part.getTitle(),
						result.getInputText());
			}
		}
		long l2 = System.currentTimeMillis();
//		System.out.println("#####################Time taken for ShowAllDocsHanlder" + (l2-l1) + "m- secs");
		return null;
	}
	
	public void refresh() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
		if (commandService != null) {
			commandService
					.refreshElements(
							"com.ibm.biginsights.textanalytics.treeview.commands.ShowAllDocs",
							null);//$NON-NLS-1$
		}
	}
    
    
}
