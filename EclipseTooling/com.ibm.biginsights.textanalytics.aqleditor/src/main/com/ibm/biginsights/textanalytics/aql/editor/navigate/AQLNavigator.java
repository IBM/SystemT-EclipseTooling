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
package com.ibm.biginsights.textanalytics.aql.editor.navigate;

import java.io.File;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.DictionaryEditor;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLPartitionScanner;
import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * Base class for aql code navigation (triggered by F3) in text analytics projects.
 * 
 * 
 */
public abstract class AQLNavigator
{
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  public static final String EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.AQLEditor"; //$NON-NLS-1$
  public static final String DICT_EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.DictEditor"; //$NON-NLS-1$
  public static final String KEYWORD_TOKEN = "AQL_Keyword";//$NON-NLS-1$

  StyledText styledText;
  String currToken;
  AQLEditor aqlEditor, newEditor;
  DictionaryEditor newDictEditor;
  IDocument doc;
  ITextSelection textSelection;
  IFile selectionEventSourceFile;

  public AQLNavigator (AQLEditor aqlEditor, IFile selectionSource, ITextSelection selection)
  {
    this.aqlEditor = aqlEditor;
    textSelection = selection;
    styledText = (StyledText) aqlEditor.getAdapter (Control.class);
    selectionEventSourceFile = selectionSource;
    // Get the document provider
    TextFileDocumentProvider docProvider = (TextFileDocumentProvider) aqlEditor.getDocumentProvider ();
    doc = docProvider.getDocument (aqlEditor.getEditorInput ());
  }

  /**
   * Detects selected token, finds its definition and navigates to that code.
   */
  public abstract void navigateToDefinition ();

  /**
   * Finds the starting line and ending line containing the selected text
   * 
   * @return An integer array containing the start line and ending line numbers for the selection.
   * @throws BadLocationException
   */
  protected int[] getSelectionOffsets () throws BadLocationException
  {

    int startLine = -1, endLine = -1;
    if ((textSelection == null) || textSelection.getText ().length () == 0) {
      int caretOffset = aqlEditor.getCaretOffset ();
      startLine = doc.getLineOfOffset (caretOffset);
      endLine = startLine;
    }
    else {
      startLine = textSelection.getStartLine ();
      endLine = textSelection.getEndLine ();
    }
    return new int[] { startLine, endLine };
  }

  /**
   * Checks if the selection is in commented code
   * 
   * @param doc IDocument instance for file containing the selection
   * @param start start offset of selection
   * @param end end offset of selection
   * @return true if at any part of selection is outside comments, false otherwise.
   * @throws BadLocationException
   */
  protected boolean isSelectionOutsideComments (IDocument doc, int start, int end) throws BadLocationException
  {
    for (int i = start; i <= end; i++) {
      String line = getLineText (doc, i).trim ();
      if (!line.startsWith ("--") && !line.startsWith ("/*")) { return true; }
    }
    return false;
  }

  // Get the text for a given line
  private String getLineText (IDocument doc, int line) throws BadLocationException
  {
    return doc.get (doc.getLineOffset (line), doc.getLineLength (line));
  }

  /**
   * Gets the selected token from the Editor based on the cursor location.
   * 
   * @param doc
   * @param startLine
   * @param cursorLocation
   * @return
   * @throws BadLocationException
   */
  protected String getSelectedToken (IDocument doc, int startLine, int cursorLocation) throws BadLocationException
  {
    String token = null;
    ITextSelection textSelection1 = (ITextSelection) aqlEditor.getSelectionProvider ().getSelection ();
    ITypedRegion typedRegion = doc.getPartition (textSelection1.getOffset ());
    String sentence = doc.get (doc.getLineOffset (startLine), doc.getLineLength (startLine));
    // if it is quoted string, it might have spaces, parse them differently
    if (typedRegion.getType () == AQLPartitionScanner.AQL_STRING) {
      token = getCurrentTokenBetweenQuotes (sentence, cursorLocation);
    }
    else {
      token = getCurrentToken (sentence, cursorLocation);
    }

    return token;
  }

  /**
   * Get token at given position in the sentence, accounting for double quotes
   * 
   * @param sentence
   * @param cursorLocation
   * @return
   */
  private String getCurrentTokenBetweenQuotes (String sentence, int cursorLocation)
  {
    String curr = "";
    int start = 0, end = 0, startQuotes = 0, endQuotes = 0;
    int ln = sentence.length ();
    String prev = sentence.substring (0, cursorLocation);
    // cursor at first position
    if ((cursorLocation == 0) || (cursorLocation == ln - 1)) {
      curr = "";
    }
    else {

      if (prev.lastIndexOf ("'") != -1) {
        start = prev.lastIndexOf ("'") + 1;
      }
      else {
        start = 0;
      }
      // System.out.println("prev is: " + prev + start);
      String next = sentence.substring (cursorLocation);

      if (next.indexOf ("'") != -1) {
        end = next.indexOf ("'") + cursorLocation;
      }
      else {
        end = sentence.length () - 1;
      }

      // now check for double quotes
      if (prev.lastIndexOf ("\"") != -1) {
        startQuotes = prev.lastIndexOf ("\"") + 1;
      }
      else {
        startQuotes = 0;
      }
      if (next.indexOf ("\"") != -1) {
        endQuotes = next.indexOf ("\"") + cursorLocation;
      }
      else {
        endQuotes = sentence.length () - 1;
      }
    }
    start = Math.max (start, startQuotes);
    end = Math.min (end, endQuotes);
    curr = sentence.substring (start, end);
    return curr;
  }

  /**
   * Get token at given position in sentence
   * 
   * @param sentence
   * @param cursorLocation
   * @return
   */
  private String getCurrentToken (String sentence, int cursorLocation)
  {
    String curr = "";
    int start = 0, end = 0;
    String prev = sentence.substring (0, cursorLocation);
    // cursor at first position
    if ((cursorLocation == 0)) // || (cursorLocation == ln)) // changing this to fix some issues with selecting empty
                               // string..
    {
      curr = "";
    }
    else {

      if (prev.lastIndexOf ("(") != -1) {
        start = prev.lastIndexOf ("(") + 1;
        prev = sentence.substring (start, cursorLocation);
        if (prev.lastIndexOf (" ") != -1) {
          // changed Nish
          /*
           * Defect: 33944: Start index is added along with the lastIndex to get the offset properly. The sample
           * sentence can be "\t(select X.* from Ptn_no___2_topics_0001_en_output1 X)"
           */
          start = prev.lastIndexOf (" ") + start;
        }
      }
      /**
       * Added the condition prev.lastIndexOf (",") != -1 to fix the defect 38122. If the variable prev has value
       * "from Type T,Domain D;" then the previous code do not handle the comma (Type T,Domain D) and was looking for
       * space so the start variable will have index stating from T (T,Domain D). As part of the fix we check whether
       * the prev has comma or space and it checks which comes last and assign the last index to the start variable.
       */
      else if (prev.lastIndexOf (" ") != -1 || prev.lastIndexOf (",") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
        int lastIndexSpace = prev.lastIndexOf (" "); //$NON-NLS-1$
        int lastIndexComma = prev.lastIndexOf (","); //$NON-NLS-1$
        if (lastIndexComma > lastIndexSpace)
          start = lastIndexComma + 1; // Add 1, so that we get the name after comma.
        else
          start = lastIndexSpace;
      }
      else {
        start = 0;
      }
      String next = sentence.substring (cursorLocation);

      if (next.indexOf ("(") != -1) {
        end = next.indexOf ("(") + cursorLocation;
      }
      else if (next.indexOf (")") != -1) {
        end = next.indexOf (")") + cursorLocation;
        next = sentence.substring (cursorLocation, end);
        if (next.lastIndexOf (" ") != -1) {
          end = next.lastIndexOf (" ") + cursorLocation;;
        }
      }
      else if (next.indexOf (" ") != -1) {
        end = next.indexOf (" ") + cursorLocation;
      }
      else {
        end = sentence.length () - 1;
      }
    }

    curr = sentence.substring (start, end);
    curr = trimFileName (curr);
    // After trimming if it till contain any spl char's trim again..
    if ((curr.indexOf (" ") != -1) || (curr.indexOf ("(") != -1) || (curr.indexOf (")") != -1)
      || (curr.indexOf (",") != -1) || (curr.indexOf ('.', curr.indexOf ('.') + 1) != -1)) {
      // Set the current token as selection..
      curr = textSelection.getText ();
      return curr;
    }
    return curr;
  }

  /**
   * Opens file in aql or dictionary editor. Sets newEditor or newDictEditor class fields.
   * 
   * @param fullpath
   * @param offset
   */
  protected void openFile (String fullpath, int offset)
  {
    IPath path = new Path (fullpath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation (path);
    openFile (file, offset);
  }

  /**
   * Opens file in aql or dictionary editor. Sets newEditor or newDictEditor class fields.
   * 
   * @param file
   * @param offset
   */
  protected void openFile (IFile file, int offset)
  {
    if (file == null) { return; }
    // 17886: On F3 the following code will open the files in their
    // respective editors or if its not a aql or .dict file then it will
    // look for its default editor from eclipse.
    try {
      if (file.getName ().endsWith (Constants.AQL_FILE_EXTENSION)) {
        newEditor = (AQLEditor) PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().openEditor (
          new FileEditorInput (file), EDITOR_ID);
        styledText = (StyledText) newEditor.getAdapter (Control.class);
      }
      else if (file.getName ().endsWith (Constants.DICTIONARY_FILE_EXTENSION)) {
        newDictEditor = (DictionaryEditor) PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().openEditor (
          new FileEditorInput (file), DICT_EDITOR_ID);
        styledText = (StyledText) newDictEditor.getAdapter (Control.class);
      }
      else {
        IEditorDescriptor desc = PlatformUI.getWorkbench ().getEditorRegistry ().getDefaultEditor (file.getName ());
        TextEditor te = (TextEditor) PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().openEditor (
          new FileEditorInput (file), desc.getId ());
        styledText = (StyledText) te.getAdapter (Control.class);
      }
      styledText.setCaretOffset (offset);
    }
    catch (PartInitException e) {
      e.printStackTrace ();
    }
  }

  /**
   * Checks if a file editor in eclipse is open with the given file name.
   * 
   * @param name
   * @return true if given file name has been opened in any file editor.
   */
  protected boolean didEditorOpen (String name)
  {
    IEditorInput genericInput = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getActiveEditor ().getEditorInput ();
    if (!(genericInput instanceof IFileEditorInput)) { return false; }
    IFileEditorInput ei = (IFileEditorInput) genericInput;
    if (name.equals (ei.getFile ().getName ())) {
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Opens a file outside eclipse workspace in an editor.
   * 
   * @param f
   */
  protected void openExternalEditor (File f)
  {
    IFileStore fileStore = EFS.getLocalFileSystem ().getStore (new Path (f.getAbsolutePath ()));
    if (!fileStore.fetchInfo ().isDirectory () && fileStore.fetchInfo ().exists ()) {
      IWorkbenchPage page = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
      try {
        IDE.openEditorOnFileStore (page, fileStore);
      }
      catch (PartInitException e) {}
    }
  }

  /**
   * Removes surrounding quotes, whitespace and semi colons from given string.
   * 
   * @param fn2
   * @return
   */
  protected String trimFileName (String fn2)
  {
    if (fn2.length () > 0) {
      if (fn2.indexOf ("'") != -1) {
        fn2 = fn2.substring (fn2.indexOf ("'") + 1);
        fn2 = fn2.substring (0, fn2.indexOf ("'"));
      }
      fn2 = fn2.replace (";", "");
      fn2 = fn2.trim ();
    }
    return fn2;
  }
}
