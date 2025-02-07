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

package com.ibm.biginsights.textanalytics.aql.editor.callhierarchy;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.common.AbstractEditorHandler;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLPartitionScanner;

/**
 * This class is an base class which contains common code for ChildHierarchyHandler and ParentHierarchyHandler
 * 
 *  Madiraju
 */
public class CallHierarchyHandler extends AbstractEditorHandler
{



  @Override
  public Object execute (ExecutionEvent event)
  {
    try {

      // Make sure its an editor
      editor = HandlerUtil.getActiveEditor (event);
      Assert.isNotNull (editor);
      Assert.isLegal (editor instanceof AQLEditor);
      aqlEditor = (AQLEditor) editor;

      // Get the document provider
      TextFileDocumentProvider docProvider = (TextFileDocumentProvider) aqlEditor.getDocumentProvider ();
      IDocument doc = docProvider.getDocument (aqlEditor.getEditorInput ());

      // Get the selection
      ISelection selection = HandlerUtil.getCurrentSelection (event);
      Assert.isNotNull (selection);
      Assert.isTrue (selection instanceof ITextSelection);
      ITextSelection textSelection = (ITextSelection) selection;

      int startLine = -1;
      int caretOffset = aqlEditor.getCaretOffset ();
      if ((textSelection == null) || textSelection.getText () == null || textSelection.getText ().length () == 0) {

        startLine = doc.getLineOfOffset (caretOffset);
      }
      else {
        startLine = textSelection.getStartLine ();
      }
      s = (StyledText) aqlEditor.getAdapter (Control.class);
      String sentence = doc.get (doc.getLineOffset (startLine), doc.getLineLength (startLine));
      int cursorLocation = s.getCaretOffset () - doc.getLineOffset (startLine);

      // GET CURRENT TOKEN
      ITextSelection textSelection1 = (ITextSelection) aqlEditor.getSelectionProvider ().getSelection ();
      ITypedRegion typedRegion = doc.getPartition (textSelection1.getOffset ());

      // if it is quoted string, it might have spaces and special characters, parse them differently
      if (typedRegion.getType () == AQLPartitionScanner.AQL_STRING) {
        currToken = CallHierarchyUtil.getCurrentTokenBetweenQuotes (sentence, cursorLocation);
      }
      else {
        currToken = CallHierarchyUtil.getCurrentToken (sentence, cursorLocation);
      }
    }
    catch (BadLocationException e) {
      e.printStackTrace ();
    }
    return null;
  }

}
