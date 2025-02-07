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

package com.ibm.biginsights.textanalytics.refactor.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;
import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameInfo;
import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameProcessor;
import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameRefactoring;
import com.ibm.biginsights.textanalytics.refactor.ui.AQLElementRenameUITexts;
import com.ibm.biginsights.textanalytics.refactor.ui.wizards.AQLElementRenameWizard;
import com.ibm.biginsights.textanalytics.refactor.util.RefactorUtils;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 *  
 *  Kalakuntla
 * 
 */
public class AQLElementRename extends AbstractHandler {



  private static final String FILE_EXT = "aql"; //$NON-NLS-1$
  private ISelection selection;
  private IEditorPart targetEditor;
  private boolean isAqlFile;
  private AQLElementRenameInfo info = new AQLElementRenameInfo ();
  private IFile srcFile;
  int offset;
  StyledText s;
  String selectedToken ="";//$NON-NLS-1$
  public static final String EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.AQLEditor"; //$NON-NLS-1$
  ExecutionEvent event = null;
  Shell shell = null; 
  
  ITextSelection txtSelection = null;

  @Override
  public Object execute(ExecutionEvent event) {
    try
    {
      this.event = event;
      targetEditor = HandlerUtil.getActiveEditor(event);
      selection = HandlerUtil.getCurrentSelection(event);
      Assert.isNotNull(targetEditor);
      Assert.isLegal(targetEditor instanceof AQLEditor);
      isAqlFile = false;
      srcFile = getFile ();
      if (srcFile != null && srcFile.getFileExtension ().equals (FILE_EXT)) {
        isAqlFile = true;
      }
      
      if (!isAqlFile) {
        refuse ();
      }
      else {
        if (selection != null && selection instanceof ITextSelection) {
          applySelection ((ITextSelection) selection);
          if (saveAll ()) {
            openWizard ();
          }
        }
      }
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

  /*
   * This method is used to collect the information from the current selection and update AQLElementRenameInfo object
   */
  @SuppressWarnings("unused")
  private void applySelection (final ITextSelection textSelection)
  {
    AQLEditor  aqlEditor = null;
    int startLine = -1, endLine = -1;
    String sentence =""; //$NON-NLS-1$
    int cursorLocation = 0;
    if ((textSelection == null) || textSelection.getText().length() == 0) {
      if (targetEditor instanceof AQLEditor) {
        aqlEditor = (AQLEditor) targetEditor;
      }
      TextFileDocumentProvider docProvider = (TextFileDocumentProvider) aqlEditor.getDocumentProvider();
      IDocument doc = docProvider.getDocument(aqlEditor.getEditorInput());
      s = (StyledText) aqlEditor.getAdapter(Control.class);
      int caretOffset = aqlEditor.getCaretOffset();
      try {
        startLine = doc.getLineOfOffset(caretOffset);
        endLine = startLine;
        sentence = doc.get(doc.getLineOffset(startLine), doc.getLineLength(startLine)); 
        cursorLocation = s.getCaretOffset() - doc.getLineOffset(startLine);
        offset = getCurrentTokenOffset(sentence, cursorLocation, s.getCaretOffset ());
        selectedToken = getCurrentToken(sentence, cursorLocation, s.getCaretOffset());
      }
      catch (BadLocationException e) {
        e.printStackTrace ();
      }
    } else {
      offset = textSelection.getOffset ();
      selectedToken = textSelection.getText ();
    }
    ElementType eleType = IndexerUtil.detectElementType (getFile (), offset);
    info.setOldName (selectedToken);
    info.setNewName (selectedToken);
    info.setOffset (offset);
    info.setSourceFile (getFile ()); //To fix some issue with file rename and then element rename refactoring..
    String project = srcFile.getProject ().getName ();
    String module = srcFile.getParent ().getName ();
    info.setProject (project);
    info.setModule (module);
    info.setEleType (eleType);
  }
  
  /**
   * This method will find the current token offset where the cursor is placed. 
   */
  private int getCurrentTokenOffset (String sentence, int cursorLocation, int caretOffset)
  {
    String curr = "";//$NON-NLS-1$
    int start = 0;
    String prev = sentence.substring(0, cursorLocation);

    if((cursorLocation == 0)){ 
      curr = "";
    }
    else{
      if(prev.lastIndexOf(" ") != -1){//$NON-NLS-1$
        start = prev.lastIndexOf(" ");//$NON-NLS-1$
      }
    }
    curr = sentence.substring(start, cursorLocation);
    int length = curr.length ();

    return caretOffset - length + 1;
  }

  /*
   * This method is used to identify the current word where cursor is placed...
   */
  private String getCurrentToken(String sentence, int cursorLocation, int caretOffset) {
    String curr = "";//$NON-NLS-1$
    int start = 0, end = 0;
    String prev = sentence.substring(0, cursorLocation);
    
    if((cursorLocation == 0)){
      curr = "";//$NON-NLS-1$
    } else {
      if (prev.lastIndexOf("(") != -1){//$NON-NLS-1$
        start = prev.lastIndexOf("(") + 1;//$NON-NLS-1$
        prev = sentence.substring(start, cursorLocation);//$NON-NLS-1$
        if(prev.lastIndexOf(" ") != -1){//$NON-NLS-1$
          start = prev.lastIndexOf(" ");//$NON-NLS-1$
        }
      }else if(prev.lastIndexOf(" ") != -1){//$NON-NLS-1$
        start = prev.lastIndexOf(" ");//$NON-NLS-1$
      }
      else {
        start = 0;
      }
      String next = sentence.substring(cursorLocation);

      if (next.indexOf("(") != -1){//$NON-NLS-1$
        end = next.indexOf("(") + cursorLocation;//$NON-NLS-1$
      }else if (next.indexOf(")") != -1){//$NON-NLS-1$
        end = next.indexOf(")") + cursorLocation;//$NON-NLS-1$
        next = sentence.substring(cursorLocation, end);
        if(next.lastIndexOf(" ") != -1){//$NON-NLS-1$
          end = next.lastIndexOf(" ") + cursorLocation;//$NON-NLS-1$
        }
      }else if (next.indexOf(" ") != -1){//$NON-NLS-1$
        end = next.indexOf(" ") + cursorLocation;//$NON-NLS-1$
      }
      else{
        end = sentence.length() - 1;
      }
    }
    curr = sentence.substring(start, end);
    curr = trimFileName(curr);

    return curr;
  }

  // This method is to trim the string based on the 
  private String trimFileName(String fn2) {
    if(fn2.length() > 0 )
    {
      if(fn2.indexOf("'") != -1)//$NON-NLS-1$
      {
        fn2 = fn2.substring(fn2.indexOf("'")+1);//$NON-NLS-1$
        fn2 = fn2.substring(0, fn2.indexOf("'"));//$NON-NLS-1$
      }
      fn2 = fn2.replace(";", "");//$NON-NLS-1$
      fn2 = fn2.trim();
    }
    return fn2;
  }
  
  /*
   * This method is to display the error dialog to user saying re-factor is not possible
   */
  private void refuse ()
  {
    String title = AQLElementRenameUITexts.AQLElementRename_refuseDlg_title;
    String message = AQLElementRenameUITexts.AQLElementRename_refuseDlg_message;
    MessageDialog.openInformation (getShell (), title, message);
  }

  // Method to save all unsaved resources in workspace
  private static boolean saveAll ()
  {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace ().getRoot ();
    // Right now saving all the unsaved resource in workspace before launching the re-factoring wizard..
    return IDE.saveAllEditors (new IResource[] { workspaceRoot }, false);
  }

  // Method to launch the element re-factor wizard
  private void openWizard ()
  {
    RefactoringProcessor processor = new AQLElementRenameProcessor (info);
    AQLElementRenameRefactoring ref = new AQLElementRenameRefactoring (processor);
    AQLElementRenameWizard wizard = new AQLElementRenameWizard (ref, info);
    RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation (wizard);
    IFile epFile = RefactorUtils.getAffectedExtractionPlan (info);
    try {
      String titleForFailedChecks = ""; //$NON-NLS-1$
      operation.run (getShell (), titleForFailedChecks);

      // refresh extraction plan being open if it is affected
      if (epFile != null)
        ProjectUtils.refreshExtractionPlan (epFile.getProject ().getName ());
    }
    catch (final InterruptedException irex) {
      // operation was canceled
    }
  }

  private Shell getShell ()
  {
    Shell result = null;
    if (targetEditor != null) {
      result = targetEditor.getSite ().getShell ();
    }
    else {
      result = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getShell ();
    }
    return result;
  }

  // This method returns the currently working file instance.
  private final IFile getFile ()
  {
    IFile result = null;
    if (targetEditor instanceof ITextEditor) {
      ITextEditor editor = (ITextEditor) targetEditor;
      IEditorInput input = editor.getEditorInput ();
      if (input instanceof IFileEditorInput) {
        result = ((IFileEditorInput) input).getFile ();
      }
    }
    return result;
  }

}

