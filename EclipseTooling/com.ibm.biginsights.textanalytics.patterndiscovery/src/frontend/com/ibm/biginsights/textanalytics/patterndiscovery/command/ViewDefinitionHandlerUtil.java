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
package com.ibm.biginsights.textanalytics.patterndiscovery.command;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;

/**
 * Utilities for the pattern discovery command handlers.
 */
public class ViewDefinitionHandlerUtil {



  /**
   * Get the region of the selection of cursor position where the event was triggered. This method
   * will always return a valid region, even when there is no selection.
   * 
   * @param event
   * @return
   */
  public static final ITypedRegion getRegion( ExecutionEvent event, AQLEditor editor )
      throws ExecutionException 
  {
    // We can safely cast to a text selection since these commands will only be called from a text
    // editor.
    ITextSelection selection = (ITextSelection) HandlerUtil.getCurrentSelection(event);
    
    // Get the document from the editor
    TextFileDocumentProvider docProvider = (TextFileDocumentProvider) editor.getDocumentProvider();
    IDocument doc = docProvider.getDocument(editor.getEditorInput());
    int offset = 0;
    // Check if we have a valid selection. We don't check that the whole selection is in a regex, we
    // just take the start position.
    if (selection != null && selection.getLength() > 0) 
    {
      offset = selection.getOffset();
    } 
    else {
      // No valid selection, so use the caret position instead. This should be the normal case.
      offset = editor.getCaretOffset();
    }
    ITypedRegion result = null;
    try {
      result = doc.getPartition(offset);
    } 
    catch (BadLocationException e) {
      throw new ExecutionException("Error executing pattern discovery command", e);
    }
    
    return result;
  }
}
