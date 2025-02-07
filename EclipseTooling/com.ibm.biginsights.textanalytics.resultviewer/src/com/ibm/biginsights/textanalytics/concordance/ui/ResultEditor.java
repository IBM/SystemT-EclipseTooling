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
package com.ibm.biginsights.textanalytics.concordance.ui;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextEditor;

public class ResultEditor extends TextEditor {



	public ResultEditor() {
		super();
		this.setHelpContextId("com.ibm.biginsights.textanalytics.tooling.help.text_analytics_result_editor");
	}
	
  public int getWidgetOffset(int modelOffset) {
    ISourceViewer sourceViewer = getSourceViewer();
    return modelOffset2WidgetOffset(sourceViewer, modelOffset);
  }
  
  /*
   * Overriding this method because the next/prev annotation needs to work only if 
   * it is TA annotation type
   * @see org.eclipse.ui.texteditor.AbstractDecoratedTextEditor#isNavigationTarget(org.eclipse.jface.text.source.Annotation)
   */
  
  protected boolean isNavigationTarget(Annotation annotation) {
		if (annotation.getType().startsWith("com.ibm.biginsights.textanalytics.aql.annot"))
			return true;
		else
			return false;
	}

  /**
   * This is a customized goToAnnotation method of AbstractTextEditor. It needed
   * to be customized because when in AllDocs mode, the next/previous should go to the input document
   * annotation
   * @param allDocsMode
   * @param forward
   */
	public Annotation gotoMyAnnotation(boolean allDocsMode, boolean forward) {

	  Annotation nextAnnotation = null;

	  if (allDocsMode == false)
		{
	    nextAnnotation = super.gotoAnnotation(forward);
		}
		else
		{
			ITextSelection selection=  null;
			Position position= new Position(0, 0);
			// When in AllDocs mode + show next/prev doc mode , go to next annotation where 
			// the annotation is of type document marker.
			while (position.getOffset() >= 0 )
			{
				selection= (ITextSelection) getSelectionProvider().getSelection();
				Annotation annotation= findAnnotation(selection.getOffset(), selection.getLength(), forward, position);
        nextAnnotation = super.gotoAnnotation(forward);
				if (annotation.getType().equals("com.ibm.biginsights.textanalytics.aql.annot-document-marker"))
					break;
			}
		}

	  return nextAnnotation;
	}

}
