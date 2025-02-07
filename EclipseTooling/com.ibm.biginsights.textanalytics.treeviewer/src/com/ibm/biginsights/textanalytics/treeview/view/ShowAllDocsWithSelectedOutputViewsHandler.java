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

public class ShowAllDocsWithSelectedOutputViewsHandler extends
		AbstractTreeViewHandler {


	
	/**
	 * This handler is called when the user clicks on 'Show All Documents With Annotations'
	 * menu on the treeview. It uses the ConcordanceMetaModel to construct the mergedModel for all documents
	 * that have the selected annotations  
	 */
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		long l1 = System.currentTimeMillis();
		initialize(event);
		//seperateAndDetermineDocumentSchemaInfoFromTitle(activePart.getTitle());
		ArrayList<String> al = getOutputViewsToBeShown(event);
		if (al.isEmpty() && metaModel != null)
		{
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowWarning(
					Messages.SelectSomething);
			return null;
			
		}
		if (metaModel != null) {
			String lExtn = null;
			if (isLC) {
				lExtn = Constants.GS_FILE_EXTENSION_WITH_DOT
						+ Constants.STRF_FILE_EXTENSION_WITH_DOT;
			} else {
				lExtn = Constants.STRF_FILE_EXTENSION_WITH_DOT;
			}

			// The method getMergedModel is used by both this handler as well as the ShowAllDocsHandler
			// We need to have a different id to indicate depending on the checkboxes selected
			// Hence we pass the hashcode of the arraylist as part of the identifier.
			// System.out.println("In ShowAllDocsWithSelectedOutputViewsHandler, the docSchema is "+docSchema);
			result = metaModel.getMergedModelForAllDocuments(al,
					Constants.ALL_DOCS_WITH_ANNOTS + lExtn, docSchema);
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
//		System.out.println("#####################Time taken for ShowAllDocsWithSelectedOutputViewsHandler" + (l2-l1) + "m- secs");
		return null;
	}

		public void refresh() {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
			if (commandService != null) {
				commandService
						.refreshElements(
								"com.ibm.biginsights.textanalytics.treeview.commands.ShowAllDocsWithAnnotations",
								null);//$NON-NLS-1$
			}
		}
}
