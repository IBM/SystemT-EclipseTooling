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

package com.ibm.biginsights.textanalytics.aql.editor.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.DictionaryEditor;

/**
 * This is the abstract common class called for Navigation Handler and Call Hierarchy Handler
 * 
 *  Madiraju
 */
public abstract class AbstractEditorHandler extends AbstractHandler
{



  protected String currToken = null;
  protected AQLEditor aqlEditor;
  protected StyledText s;
  protected IEditorPart editor;
  // String currToken;
  protected Set<String> fileSet;

  protected ArrayList<String> searchPathList;
  protected List<String> dictionarySearchPathList;

  protected ArrayList<String> fileList;
  protected AQLEditor newEditor;
  protected DictionaryEditor newDictEditor;
  protected String currentFileLocation;

  public Object execute (ExecutionEvent event)
  {
    editor = HandlerUtil.getActiveEditor (event);
    Assert.isNotNull (editor);
    fileSet = new HashSet<String> ();
    fileList = new ArrayList<String> ();
    Assert.isLegal (editor instanceof AQLEditor);
    aqlEditor = (AQLEditor) editor;
    IFileEditorInput input = (IFileEditorInput) editor.getEditorInput ();
    IFile currentFile = input.getFile ();
    // Get the document provider and the selection
    TextFileDocumentProvider docProvider = (TextFileDocumentProvider) aqlEditor.getDocumentProvider ();
    IDocument doc = docProvider.getDocument (aqlEditor.getEditorInput ());
    ISelection selection = HandlerUtil.getCurrentSelection (event);
    Assert.isNotNull (selection);
    Assert.isTrue (selection instanceof ITextSelection);
    ITextSelection textSelection = (ITextSelection) selection;
    return null;
  }

  protected static final boolean determineCommentDirection (IDocument doc, int start, int end) throws BadLocationException
  {
    for (int i = start; i <= end; i++) {
      String line = getLineText (doc, i).trim ();
      if (!line.startsWith ("--")) { return true; }
    }
    return false;
  }

  // Get the text for a given line
  private static final String getLineText (IDocument doc, int line) throws BadLocationException
  {
    return doc.get (doc.getLineOffset (line), doc.getLineLength (line));
  }

}
