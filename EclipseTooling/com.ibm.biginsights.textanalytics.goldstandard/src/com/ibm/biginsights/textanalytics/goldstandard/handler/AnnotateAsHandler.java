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
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.model.AnnotationType;
import com.ibm.biginsights.textanalytics.goldstandard.model.GoldStandardModel;
import com.ibm.biginsights.textanalytics.goldstandard.model.OutputViewModel;
import com.ibm.biginsights.textanalytics.goldstandard.model.Span;
import com.ibm.biginsights.textanalytics.goldstandard.ui.GSFileViewer;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.goldstandard.util.WordDetector;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldType;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.SpanTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeParent;
import com.ibm.biginsights.textanalytics.treeview.view.AQLResultTreeView;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class handles the click event on popup menu item 'Annotate As -> Annotation type'
 *  Krishnamurthy
 *
 */
public class AnnotateAsHandler extends AbstractHandler {


	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		EditorInput editorInput = (EditorInput) GoldStandardUtil.getGSEditor().getEditorInput();
		GoldStandardModel model = (GoldStandardModel) editorInput.getModel();
		if(model.gsComplete){
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
					Messages.AnnotateAsHandler_ALREADY_MARKED_COMPLETE);
			return null;
		}
		IFile gsFile = (IFile) editorInput.getUserData();
		IFolder gsFolder = GoldStandardUtil.getGoldStandardFolder(gsFile);
		
		Event trigger = (Event) event.getTrigger();
		String annotationViewName = null;
		String annotationType = null;
		if(trigger.widget instanceof MenuItem){
			MenuItem menuItem = (MenuItem) trigger.widget;
			String modulePlusviewPlusType = "";
			if(menuItem.getText().contains("\t")){ // strip keyboard shortcut that appears after tab character
			  modulePlusviewPlusType = menuItem.getText().substring(0, menuItem.getText().indexOf("\t")); //$NON-NLS-1$
			}else{
			  modulePlusviewPlusType = menuItem.getText();
			}
			int indexOfDot = modulePlusviewPlusType.lastIndexOf ("."); //$NON-NLS-1$
			annotationViewName = modulePlusviewPlusType.substring(0, indexOfDot);
			annotationType = modulePlusviewPlusType.substring(indexOfDot+1);
		}else if(trigger.widget instanceof StyledText){
			//The user has pressed CTRL+<number>
			try {
				String cmdName = event.getCommand().getName();
				if(!StringUtils.isEmpty(cmdName)){
					
					
					
					if("annTypeDefault".equals(cmdName)){ //$NON-NLS-1$
						String PARAM_DEFAULT_ANNOTATION_TYPE = Constants.GS_DEFAULT_ANNOTATION_TYPE;  //$NON-NLS-1$
						String defAnnType = GoldStandardUtil.getGSPrefStore(gsFolder).getString(PARAM_DEFAULT_ANNOTATION_TYPE);
						if(StringUtils.isEmpty(defAnnType)){
							annotationViewName = null;
							annotationType = null;
						}else{
							String viewPlusType = defAnnType;
							int indexOfDot = viewPlusType.indexOf("."); //$NON-NLS-1$
							annotationViewName = viewPlusType.substring(0, indexOfDot);
							annotationType = viewPlusType.substring(indexOfDot+1);
						}
					}else{
						int keyPressed = trigger.keyCode-48;
						AnnotationType[] annTypes = GoldStandardUtil.getAnnotationTypes(gsFolder);
						if(annTypes == null || annTypes.length==0){
							LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(Messages.AnnotateAsHandler_NO_ANNOTATION_TYPE_DEFINED);
							return null;
						}
						for (AnnotationType annType : annTypes) {
							if(StringUtils.isEmpty(annType.getShortcutKey())){
								continue;
							}
							if(Integer.parseInt(annType.getShortcutKey()) == keyPressed){
								annotationViewName = annType.getViewName();
								annotationType = annType.getFieldName();
								break;
							}
						}
					}
				}
				
			} catch (NotDefinedException e) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
			}
		}
		
		if(annotationViewName != null && annotationType != null){
		    WordDetector wordDetector = WordDetector.getInstance(
		    		((IFile) editorInput.getUserData()),
		    		model.getInputText());
		    Span span = wordDetector.getCurrentSpan();
		    
		    if(span == null){
		    	LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
		    			Messages.AnnotateAsHandler_SELECT_A_SPAN_OR_CONFIGURE_AUTO_DETECT_WORD_BOUNDARY);
		    	return null;
		    }
			int spanBegin = span.getBegin();
			int spanEnd = span.getEnd();
			String text = span.getText();
			
			gsFile = annotateAndSave(annotationViewName, annotationType, spanBegin, spanEnd, text);
			if(gsFile == null){
				return null;
			}

			AQLResultTreeView treeView = GoldStandardUtil.getTreeView(editorInput.getName());
			Object[] checkedElements = GoldStandardUtil.getCheckedElements(treeView);
		
			// Fix for bug #17355: Labeled collection editor jumps to beginning of the file when annotating a long document
			//GoldStandardUtil.reopenGSEditor(gsFile, editorInput);
			GoldStandardUtil.reopenGSEditor(gsFile, editorInput, spanBegin, spanEnd);
			
			//get a fresh tree view again, as the editor is reopened
			treeView = GoldStandardUtil.getTreeView(editorInput.getName());
			
			GoldStandardUtil.restoreCheckedElements(treeView, checkedElements);
			selectNewlyAnnotatedElement(treeView, annotationViewName, annotationType, spanBegin, spanEnd, text);
			GoldStandardUtil.expandAllManualAnnotationTypes(treeView);
		}
		 
		return null;
	}

	/*
	 * Marks the newly added annotation elements as checked
	 */
	private void selectNewlyAnnotatedElement(AQLResultTreeView treeView, String annotationViewName, String annotationType, int spanBegin, int spanEnd, String text) {
		TreeParent root = (TreeParent)treeView.getViewer().getInput();
		TreeParent tpAnnotations = (TreeParent) root.getChildren()[0];
		ITreeObject[] viewNodes = tpAnnotations.getChildren();
		String viewName = annotationViewName;
		
		TreeParent viewNode = null;
		for (ITreeObject node : viewNodes) {
			viewNode = (TreeParent)node;
			if(viewName.equals(viewNode.getName())){
				break;
			}else{
				viewNode = null;
			}
		}
		
		if(viewNode != null){
			ITreeObject[] annotationTypes = viewNode.getChildren();
			for (ITreeObject item : annotationTypes) {
				TreeParent annType = (TreeParent) item;
				if(annType.getName().equals(annotationType+ " (SPAN)")){ //$NON-NLS-1$
					ITreeObject[] annotations = annType.getChildren();
					for (ITreeObject label : annotations) {
						SpanTreeObject annotation = (SpanTreeObject) label;
						if(annotation.getStart() == spanBegin
								&& annotation.getEnd() == spanEnd
								&& annotation.getText().equals(text)){
							treeView.getViewer().setChecked(annotation, true);
							CheckStateChangedEvent event = new CheckStateChangedEvent((ICheckable) treeView.getViewer(), annotation, true);
							treeView.fireCheckStateChanged(event);
						}
					}
				}
			}
		}
	}

	/*
	 * Annotates the selected text with the specified annotation type and 
	 * saves the annotations to the .gs file 
	 */
	private IFile annotateAndSave(String annotationViewName, String fieldName, int spanBegin, int spanEnd, String text) {
		EditorInput editorInput = GoldStandardUtil.getActiveEditorInput();
		GoldStandardModel model = (GoldStandardModel) editorInput.getModel();
		String viewName = annotationViewName;
		OutputViewModel outputViewModel = model.getOutputViewByViewName(viewName);
		if(outputViewModel == null){
			outputViewModel = new OutputViewModel();
			outputViewModel.setName(viewName);
			outputViewModel.setFieldTypes(new FieldType[]{FieldType.SPAN});
			outputViewModel.setFieldNames(new String[] {fieldName});
			model.addOutputViewModel(outputViewModel);
		}
		
		int fieldIndex = GoldStandardUtil.getFieldIndex(outputViewModel.getFieldNames(), fieldName);
		OutputViewRow row = acquireOutputViewRow(outputViewModel, fieldIndex, text);
		
		if(row == null){
			return null;
		}
		
		SpanVal spanVal = new SpanVal(spanBegin, spanEnd, model.getInputTextID());
		spanVal.parentSpanName = "Document.text"; //$NON-NLS-1$
		row.put(fieldIndex, spanVal);
		
		IFile gsFile = (IFile)editorInput.getUserData();
		GoldStandardUtil.serializeModel(model, gsFile);
		return gsFile;
		
	}
	
	private String getFieldNameOrder(String[] fieldNames) {
		if(fieldNames == null || fieldNames.length == 0){
			return ""; //$NON-NLS-1$
		}
		
		if(fieldNames.length == 1){
			return fieldNames[0];
		}
		
		StringBuilder buf = new StringBuilder(fieldNames[0]);
		for (int i = 1; i < fieldNames.length; i++) {
			buf.append(", "); //$NON-NLS-1$
			buf.append(fieldNames[i]);
		}
		
		return buf.toString();
	}



	/**
	 * Fetches an existing outputViewRow, if it is incomplete (i.e some more fields can be filled in to the row).
	 * If all existing outputViewRows are full, it creates a new OutputViewRow
	 * @param outputViewModel
	 * @param fieldIndex 
	 * @param text 
	 * @return
	 */
	private OutputViewRow acquireOutputViewRow(OutputViewModel outputViewModel, int fieldIndex, String text){
		int fieldCount = (outputViewModel.getFieldNames() == null ) ? 0 : outputViewModel.getFieldNames().length;
		OutputViewRow row = null;
		OutputViewRow[] existingRows = outputViewModel.getRows();
		
		if(existingRows != null){
			for (OutputViewRow outputViewRow : existingRows) {
				if(outputViewRow.fieldValues == null){
					row = outputViewRow;
					break;
				}
				
				if(outputViewRow.fieldValues.length < fieldCount){
					if(fieldIndex != outputViewRow.fieldValues.length){
						String message = Messages.AnnotateAsHandler_ANNOTATION_ALREADY_EXISTS_FOR_CURRENT_TUPLE;
						String formattedMessage = MessageUtil.formatMessage(
								message, 
								outputViewModel.getFieldNames()[fieldIndex], 
								getCurrentAnnotations(outputViewModel, outputViewRow),
								outputViewModel.getFieldNames()[outputViewRow.fieldValues.length]);
						LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(formattedMessage);
						return null;
					}
					row = outputViewRow;
					
					FieldValue[] newArray = new FieldValue[row.fieldValues.length+1];
					for (int i = 0; i < row.fieldValues.length; i++) {
						newArray[i] = row.fieldValues[i];
					}
					row.fieldValues = newArray;
					break;
				}
			}			
		}

		if(row == null){
			if(fieldIndex != 0){
				String message = Messages.AnnotateAsHandler_INCORRECT_ORDER_OF_ANNOTATIONS;
				String formattedMsg = MessageUtil.formatMessage(
						message, 
						outputViewModel.getFieldNames()[fieldIndex],
						getFieldNameOrder(outputViewModel.getFieldNames()));
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(formattedMsg);
				return null;
			}else{
				row = new OutputViewRow(fieldCount);
				outputViewModel.addRow(row);
			}
		}
		
		return row;
	}

	private String getCurrentAnnotations(OutputViewModel outputViewModel, OutputViewRow row) {
		GSFileViewer gsViewer = GoldStandardUtil.getGSEditor();
		IDocument doc = gsViewer.getDocumentProvider().getDocument(gsViewer.getEditorInput());
		String text = doc.get();

		String[] fieldNames = outputViewModel.getFieldNames();
		StringBuilder buf = new StringBuilder(fieldNames[0]+"=>"+getAnnotatedText(text, row.fieldValues[0])); //$NON-NLS-1$
		for (int i = 1; i < row.fieldValues.length; i++) {
			buf.append(", "); //$NON-NLS-1$
			buf.append(fieldNames[i]).append("=>").append(getAnnotatedText(text, row.fieldValues[i])); //$NON-NLS-1$
		}
		return buf.toString();
	}
	
	private String getAnnotatedText(String sourceText, FieldValue fieldValue){
		SpanVal spanVal = (SpanVal)fieldValue;
		String labeledText = sourceText.substring(spanVal.start, spanVal.end);
		return labeledText;
	}

}
