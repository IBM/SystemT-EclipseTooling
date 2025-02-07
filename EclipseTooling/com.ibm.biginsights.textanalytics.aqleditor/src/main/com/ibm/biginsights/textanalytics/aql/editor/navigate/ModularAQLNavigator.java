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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.avatar.aql.tam.ModuleUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.Messages;
import com.ibm.biginsights.textanalytics.indexer.cache.ElementCache;
import com.ibm.biginsights.textanalytics.indexer.cache.FileCache;
import com.ibm.biginsights.textanalytics.indexer.model.ElementDefinition;
import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;

/**
 * Provides navigation (triggered by F3) for modular text analytics projects.
 * 
 * 
 */
public class ModularAQLNavigator extends AQLNavigator
{
  @SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  public ModularAQLNavigator (AQLEditor aqlEditor, IFile selectionSource, ITextSelection selection)
  {
    super (aqlEditor, selectionSource, selection);
  }

  @Override
  public void navigateToDefinition ()
  {
    try {
      // find offsets correpsonding to the selection made in editor.
      int[] selectionOffsets = getSelectionOffsets ();
      int startLine = selectionOffsets[0];
      int endLine = selectionOffsets[0];
      boolean notInComments = isSelectionOutsideComments (doc, startLine, endLine); // check if selection is in
                                                                                    // commented section
      if (notInComments) {
        // position relative to start of line.
        int cursorPosition = styledText.getCaretOffset () - doc.getLineOffset (startLine);

        ElementDefinition def = IndexerUtil.getDefForElemRefAtLocation (selectionEventSourceFile,
          IndexerUtil.calculateOffset (selectionEventSourceFile, startLine + 1, cursorPosition));
        if (def != null) {
          ElementLocation loc = def.getLocation ();
          ElementCache elemCache = ElementCache.getInstance ();
          String elementName = elemCache.getElementNameInAQL (def.getElementId ());
          String unQualifiedName = ModuleUtils.getUnqualifiedElementName (elementName);
          int fileId = loc.getFileId ();
          IFile file = FileCache.getInstance ().getFile (fileId); // find file containing element definition.
          if (file != null && file.exists ()) { //getFile in FileCache can return null.
            int offset = def.getLocation ().getOffset ();
            openFile (file, 0);
            if (didEditorOpen (file.getName ())) {
              // highlight text at element defintion's offset.
              highlightText (newEditor, offset, unQualifiedName.length ());
            }
          }
        }
      }
    }
    catch (BadLocationException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.ModularAQLNavigator_OffsetError, e);
    }
  }

  /**
   * Highlights text on editor, as specified by offset and length
   * 
   * @param editorPart
   * @param offset
   * @param length
   */
  private void highlightText (IEditorPart editorPart, int offset, int length)
  {
    if (!(editorPart instanceof ITextEditor) || offset <= 0) { return; }
    ITextEditor editor = (ITextEditor) editorPart;
    IDocument document = editor.getDocumentProvider ().getDocument (editor.getEditorInput ());
    if (document != null) {
      editor.selectAndReveal (offset, length);
    }
  }

}
