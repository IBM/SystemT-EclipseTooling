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
package com.ibm.biginsights.textanalytics.workflow.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;

import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.AddElementAction;
import com.ibm.biginsights.textanalytics.workflow.tasks.models.DataFile;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Pair;
import com.ibm.biginsights.textanalytics.workflow.util.StringInput;

/**
 * 
 */
public class TaggingEditor extends TextEditor
{

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  public static final String EDITOR_ID = "com.ibm.biginsights.textanalytics.workflow.editors.TaggingEditor";

  /**
	 * 
	 */
  public TaggingEditor ()
  {
    super ();
    this.setHelpContextId("com.ibm.biginsights.textanalytics.tooling.help.tagging_editor");
  }

  /**
   * Loads the text from the opened editor and compares the string from offset with the provided expected text.
   * Returns true if they match, false otherwise.
   * @param offset
   * @param length
   * @param expected
   * @return
   */
  private boolean textMatch (int offset, String expected)
  {
    if (offset < 0 || expected == null)
      return false;

    StyledText st = getSourceViewer ().getTextWidget ();
    String txt = st.getText (offset, offset + expected.length () - 1);

    return (txt != null && txt.equals (expected));
  }

  /**
   * before the context menu is shown we want to make sure that we display the choices that are available based in the
   * selection. From this menu we will provide the choices of tagging an example to an existing label, or just create a
   * new label and add the example to it. we display the 'add to label' choice if and only if the the current Action
   * Plan has at least one label. Also, if the Action Plan is not open we do not provide any choice.
   */
  public void editorContextMenuAboutToShow (IMenuManager menu)
  {
    // super.editorContextMenuAboutToShow(menu);

    ActionPlanView apview = AqlProjectUtils.getActionPlanView ();
    if (apview != null) {
      if (!apview.ready ()) return;
    }

    try {
      IEditorPart editor = (TaggingEditor) PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getActiveEditor ();

      if (editor instanceof TaggingEditor) {
        TaggingEditor tagEditor = (TaggingEditor) editor;

        ITextSelection textSelection = (ITextSelection) tagEditor.getSelectionProvider ().getSelection ();

        String text = textSelection.getText ();
        int offset = textSelection.getOffset ();
        int length = textSelection.getLength ();

        if (length < 1) return;

        if (text != null && !text.isEmpty ()) {

          IEditorInput input = editor.getEditorInput ();
          String path = "";
          String label = "";

          // TODO make sure that the input is part of the collection
          // of the current project in the action plan

          if (input instanceof StringInput) {
            if (((StringInput) input).getStorage () instanceof DataFile) {
              DataFile dfile = (DataFile) ((StringInput) input).getStorage ();
              path = dfile.getPath ();
              label = dfile.getLabel ();
            }
          }
          else {
            IFile file = ((IFile) input.getAdapter (IFile.class));
            path = file.getLocation ().toOSString ();
            label = file.getName ();
          }

          // If the extraction plan view is not opened, open it. 
          if (apview == null) {
            apview = (ActionPlanView)editor.getSite().getPage().showView(ActionPlanView.ID);
          }

          menu.add (apview.getQuickLabelingMenuItems (text, path, label, offset, length));
          menu.add (new AddElementAction (text, path, label, offset, length));
        }
      }

      menu.add (new Separator ());

    }
    catch (Exception e) {
      e.printStackTrace ();
      // TODO: handle exception
    }

  }

  /**
   * out of the set of opened editors this method check if any of them is currently editing the file described by the
   * provided path and file's name
   * 
   * @param filePath
   * @param fileLabel
   * @return
   */
  public static TaggingEditor getOpenedEditorWithFile (String filePath, String fileLabel)
  {
    IEditorReference[] editorReferences = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getEditorReferences ();

    for (IEditorReference editorRef : editorReferences) {
      IEditorPart editor = editorRef.getEditor (true);
      if (editor instanceof TaggingEditor) {
        TaggingEditor wanted = ((TaggingEditor) editor).isFileOpened (filePath, fileLabel);
        if (wanted != null) return wanted;
      }
    }

    return null;
  }

  /**
   * given the path and label of a file checks if them match those of the current opened file
   * 
   * @param filePath
   * @param fileLabel
   * @return
   */
  public TaggingEditor isFileOpened (String filePath, String fileLabel)
  {
    IEditorInput input = getEditorInput ();
    String path = "";
    String label = "";

    if (input instanceof StringInput) {
      if (((StringInput) input).getStorage () instanceof DataFile) {
        DataFile dfile = (DataFile) ((StringInput) input).getStorage ();
        path = dfile.getPath ();
        label = dfile.getLabel ();
      }
      if (path.equals (filePath) && label.equals (fileLabel)) { return this; }
    }
    else {
      IFile afile = ((IFile) input.getAdapter (IFile.class));
      path = afile.getFullPath ().toString ();
      if (path.equals (filePath)) { return this; }
    }
    return null;
  }

  /**
   * given a list of offsets and texts do highlights all of them
   * 
   * @param values
   */
  public void highlight (List<SelectionInfo> selections)
  {
    StyledText st = getSourceViewer ().getTextWidget ();

    // clear old highlights
    st.replaceStyleRanges (0, st.getText ().length (), new StyleRange[] {});

    // add the new Highlight
    Color yellow = st.getDisplay ().getSystemColor (SWT.COLOR_YELLOW);
    Color black = st.getDisplay ().getSystemColor (SWT.COLOR_BLACK);

    boolean show = true;
    for (SelectionInfo sel : selections) {
      int offset = findOffset (sel.getOffset (), sel.getSelectedText ());
      int length = sel.getSelectedText ().length ();

      StyleRange style = new StyleRange (offset, length, black, yellow);
      style.fontStyle = SWT.BOLD;

      st.setStyleRange (style);

      // If multiple examples are selected, show the first one
      if (offset >= 0 && show) {
        st.setSelection (offset);
        show = false;
      }
    }

  }

  public void highlight (SelectionInfo selection)
  {
    List<SelectionInfo> list = new ArrayList<SelectionInfo> ();
    list.add (selection);
    highlight (list);
  }

  private int findOffset (int suggestedOffset, String selectedText)
  {
    if (textMatch (suggestedOffset, selectedText))
      return suggestedOffset;

    Pair<Integer> closestOffsets = getClosestOffsets (suggestedOffset, selectedText);

    // Only one offset found, return it.
    if (closestOffsets.first < 0 && closestOffsets.second >= 0)
      return closestOffsets.second;
    else if (closestOffsets.first >= 0 && closestOffsets.second < 0)
      return closestOffsets.first;

    // found 2 offsets, before and behind.
    else if (closestOffsets.first >= 0 && closestOffsets.second >= 0) {
      // If this is Windows env, the mismatched offset is likely generated on Linux, so the correct offset is likely smaller.
      if (AqlProjectUtils.isWindowsOS ())
        return closestOffsets.first;

      // If this is Linux env, the mismatched offset is likely generated on Windows, so the correct offset is likely larger.
      else
        return closestOffsets.second;
    }

    return -1;
  }

  private Pair<Integer> getClosestOffsets (int suggestedOffset, String selectedText)
  {
    // The given offset is not the offset of selectedText, find the closest match. 
    String fullText = getSourceViewer ().getTextWidget ().getText ();

    // find the first offset
    int offset1 = fullText.indexOf (selectedText);

    if (offset1 < 0)
      return new Pair<Integer> (-1, -1);

    if (offset1 >= suggestedOffset)
      return new Pair<Integer> (-1, offset1);

    while (offset1 < suggestedOffset) {

      int offset2 = fullText.indexOf (selectedText, offset1 + 1);

      if (offset2 < 0)  // no more match
        return new Pair<Integer> (offset1, -1);

      else if (offset2 >= suggestedOffset)  // found an offset that is larger than suggestedOffset
        return new Pair<Integer> (offset1, offset2);

      else  // offset2 < suggestedOffset, keep finding
        offset1 = offset2;
    }

    return new Pair<Integer> (-1, -1);
  }

  /**
	 * 
	 */
  public void dispose ()
  {
    super.dispose ();
  }

}
