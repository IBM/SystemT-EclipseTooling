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
import com.ibm.biginsights.textanalytics.goldstandard.model.Span;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.goldstandard.util.WordDetector;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.SpanTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeParent;
import com.ibm.biginsights.textanalytics.treeview.view.AQLResultTreeView;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * 
 *
 */
public class DeleteAnnotationHandler extends AbstractHandler implements
		IHandler {



	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		EditorInput editorInput = GoldStandardUtil.getActiveEditorInput();
		if(editorInput.getModel().gsComplete){
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
					Messages.DeleteAnnotationHandler_ALREADY_MARKED_COMPLETE);
			return null;
		}
		
		IFile gsFile = (IFile)editorInput.getUserData();
		GoldStandardModel model = (GoldStandardModel) editorInput.getModel();
		
		WordDetector wordDetector = WordDetector.getInstance(gsFile, model.getInputText());
		Span span = wordDetector.getCurrentSpan();
	    if(span == null){
	    	LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
	    			Messages.DeleteAnnotationHandler_SELECT_A_SPAN_OR_CONFIGURE_AUTO_DETECT_WORD_BOUNDARY);
	    	return null;
	    }
		int spanBegin = span.getBegin();
		int spanEnd = span.getEnd();
		String text = span.getText();

		
		AQLResultTreeView treeView = GoldStandardUtil.getTreeView(editorInput.getName());
	
		boolean deleteOccured = deleteAnnotation(model, treeView, spanBegin, spanEnd, text);
		
		if(deleteOccured){
			GoldStandardUtil.serializeModel(model, gsFile);
			//Fix for bug #17355: Labeled collection editor jumps to beginning of the file when annotating a long document
			//refreshTreeViewerAndEditor(treeView, gsFile, editorInput);
			refreshTreeViewerAndEditor(treeView, gsFile, editorInput, spanBegin, spanEnd);
		}
		return null;
	}
	
	
	private void refreshTreeViewerAndEditor(AQLResultTreeView treeView, IFile origFile, EditorInput editorInput,  int spanBegin, int spanEnd) {
		Object[] checkedElements = GoldStandardUtil.getCheckedElements(treeView);
		//Fix for bug #17355: Labeled collection editor jumps to beginning of the file when annotating a long document
		GoldStandardUtil.reopenGSEditor(origFile, editorInput, spanBegin, spanEnd);
		
		//get a fresh tree view again, as the editor is reopened
		treeView = GoldStandardUtil.getTreeView(editorInput.getName());
		
		GoldStandardUtil.restoreCheckedElements(treeView, checkedElements);
		GoldStandardUtil.expandAllManualAnnotationTypes(treeView);
	}

	private boolean deleteAnnotation(GoldStandardModel model, AQLResultTreeView treeView, int spanBegin, int spanEnd, String text) {
		boolean deleteOccured = false;
		TreeParent root = (TreeParent)treeView.getViewer().getInput();
		if(root != null && root.getChildren() != null){
			TreeParent tpAnnotations = (TreeParent) root.getChildren()[0];
			ITreeObject[] viewNodes = tpAnnotations.getChildren();
			
			TreeParent viewNode = null;
			for (ITreeObject node : viewNodes) {
				viewNode = (TreeParent)node;
				ITreeObject[] annotationTypes = viewNode.getChildren();
				for (ITreeObject item : annotationTypes) {
					TreeParent annType = (TreeParent) item;
					if(annType.getName().endsWith("(SPAN)")){ //$NON-NLS-1$
						ITreeObject[] annotations = annType.getChildren();
						for (ITreeObject label : annotations) {
							SpanTreeObject annotation = (SpanTreeObject) label;
							if(annotation.getStart() == spanBegin
									&& annotation.getEnd() == spanEnd
									&& annotation.getText().equals(text)){
								treeView.getViewer().remove(annotation);
								String annotationType = annotation.getParent().getName();
								String outputView = annotation.getParent().getParent().getName();
								deleteOccured = model.removeSpan(outputView, annotationType, spanBegin, spanEnd);
								break;
							}
						}//end:for each annotation
					}//end: if annType endsWith (SPAN)
				}//end: for each annotationTypes
			}//end:for each viewNodes
		}//end: if root != null && root.getChildren() != null
		
		return deleteOccured;
	}
}
