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
package com.ibm.biginsights.textanalytics.aql.editor.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;


/**
 * 
 *  Babbar
 */

public class CommentBlockHandler extends AbstractHandler {



  // The implementation is made somewhat complicated by the fact that there may be a selection, or
  // not. When there is a selection, we act on the lines of the selection. When there is no
  // selection, we act on the line where the caret is positioned.
  @Override
  public Object execute(ExecutionEvent event) {
    // Obtain the editor
    IEditorPart editor = HandlerUtil.getActiveEditor(event);
    Assert.isNotNull(editor);
    Assert.isLegal(editor instanceof AQLEditor);
    AQLEditor aqlEditor = (AQLEditor) editor;
    // Get the document provider and the selection
    TextFileDocumentProvider docProvider = (TextFileDocumentProvider) aqlEditor
        .getDocumentProvider();
    IDocument doc = docProvider.getDocument(aqlEditor.getEditorInput());
    ISelection selection = HandlerUtil.getCurrentSelection(event);
    Assert.isNotNull(selection);
    Assert.isTrue(selection instanceof ITextSelection);
    ITextSelection textSelection = (ITextSelection) selection;
    int startLine = -1, endLine = -1;
    try {
      // If there is no selection, use the caret position instead
      if ((textSelection == null) || textSelection.getText().length() == 0) {
        int caretOffset = aqlEditor.getCaretOffset();
        startLine = doc.getLineOfOffset(caretOffset);
        endLine = startLine;
      } else {
        startLine = textSelection.getStartLine();
        endLine = textSelection.getEndLine();
      }
      // Check if we're commenting out (adding line comments) or commenting in (removing comments)
      boolean commentOut = determineCommentDirection(doc, startLine, endLine);
      // Now do the actual text replace, line by line
      for (int i = startLine; i <= endLine; i++) {
        if (commentOut) {
          doc.replace(doc.getLineOffset(i), 0, "--"); //$NON-NLS-1$
        } else {
          // For commenting in, we first need to determine where the comment chars are exactly
          int commentOffset = getLineText(doc, i).indexOf("--"); //$NON-NLS-1$
          doc.replace(doc.getLineOffset(i) + commentOffset, 2, ""); //$NON-NLS-1$
        }
      }
    } catch (BadLocationException e) {
      // Document has changed in the meantime, abandon ship.
      return null;
    }
    // As of Eclipse 3.6, this method must return null
    return null;
  }

  // Determine if we should comment out lines, or remove comments from lines. Model this on the
  // Eclipse Java comment behavior: if there is at least one line that is not commented, add
  // comments to everything. Only if the whole section we're looking at is commented out, do we
  // remove comments.
  private static final boolean determineCommentDirection(IDocument doc, int start, int end)
      throws BadLocationException {
    for (int i = start; i <= end; i++) {
      String line = getLineText(doc, i).trim();
      if (!line.startsWith("--")) { //$NON-NLS-1$
        return true;
      }
    }
    return false;
  }

  // Get the text for a given line
  private static final String getLineText(IDocument doc, int line) throws BadLocationException {
    return doc.get(doc.getLineOffset(line), doc.getLineLength(line));
  }

}
