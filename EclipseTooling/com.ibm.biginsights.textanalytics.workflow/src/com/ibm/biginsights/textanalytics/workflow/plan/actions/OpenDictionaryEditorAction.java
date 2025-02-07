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
package com.ibm.biginsights.textanalytics.workflow.plan.actions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.ibm.biginsights.textanalytics.aql.editor.DictionaryEditor;
import com.ibm.biginsights.textanalytics.aql.editor.navigate.NavigationHandler;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;
import com.ibm.biginsights.textanalytics.workflow.util.WorkspaceFileSelectionDialog;

/**
 * Action to open Regular Expression Generator for a selection of examples.
 */
public class OpenDictionaryEditorAction extends Action
{


  
	List<String> clues;

  public OpenDictionaryEditorAction (IStructuredSelection selection)
  {
    setImageDescriptor (ImageDescriptor.createFromImage (Icons.DICTIONARY_ICON));
    setText (Messages.open_dictionary_text);
    setClues(selection);
  }

  /**
   * Open the dictionary editor and append the selected examples to it.
   */
  @Override
  public void run ()
  {
    Shell shell = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getShell ();
    WorkspaceFileSelectionDialog fileDialog = new WorkspaceFileSelectionDialog (shell);
    fileDialog.setCreateNewFileParameters ( true,       // to display Create New File button
                                            com.ibm.biginsights.textanalytics.util.Messages.getString("FileDirectoryPicker.CREATE_NEW_DICTIONARY"),
                                            com.ibm.biginsights.textanalytics.util.Messages.getString("FileDirectoryPicker.NEW_DICTIONARY_BASE_NAME"),
                                            com.ibm.biginsights.textanalytics.util.Messages.getString("FileDirectoryPicker.NEW_DICTIONARY_DEFAULT_EXTENSION"),
                                            com.ibm.biginsights.textanalytics.util.Messages.getString("FileDirectoryPicker.SELECT_DICTIONARY") );
    
    final int rc = fileDialog.open ();
    if (rc == Window.OK) {

      String filePath = fileDialog.getFilePath ();

      // Load the dictionary and append the clues to it
      try {
        IEditorPart editor = loadDictionary (filePath);

        if (editor != null && editor instanceof DictionaryEditor)
          appendCluesToEditor ((DictionaryEditor)editor);
      }
      catch (Exception e) {
        Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
      }
    }
  }

  private IEditorPart loadDictionary (String fileDirValue) throws IOException, CoreException
  {
    IFile dictFile = getdictionaryFile (fileDirValue);
    if (dictFile == null)
      return null;

    FileEditorInput iFileEditorInput = new FileEditorInput(dictFile);

    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    IEditorPart editor = page.openEditor (iFileEditorInput, NavigationHandler.DICT_EDITOR_ID);

    return editor;
  }

  private IFile getdictionaryFile (String fileDirValue) throws CoreException
  {
    IFile dictFile = null;

    // This method only gets an existing file.
    String absPath = ProjectPreferencesUtil.getAbsolutePath (fileDirValue);

    // The dictionary file exists
    if (absPath != null) {

      File file = new File (absPath);
      dictFile  = AqlProjectUtils.fileToIFile (file);

    }
    // The dictionary file does not exist
    else {

      // Get the relative path string by removing "[W]" prefix.
      String pathStr = fileDirValue.substring (Constants.WORKSPACE_RESOURCE_PREFIX.length ());

      Path path = new Path (pathStr);
      dictFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

      if (!dictFile.exists ()) {    // should not exist; otherwise we don't reach here.
        dictFile.create (new ByteArrayInputStream(new byte[] {}), true, null);
      }

    }

    return dictFile;
  }

  private void appendCluesToEditor (TextEditor editor)
  {
    IDocumentProvider dp = editor.getDocumentProvider();
    IDocument doc = dp.getDocument(editor.getEditorInput());
    doc.set (doc.get () + getCluesText ());
  }

  private void setClues (IStructuredSelection selection)
  {
    clues = new ArrayList<String> ();
    if ( selection != null && !selection.isEmpty () )
    {
      for ( Object selTO : selection.toList () ) {
        clues.add (((TreeObject)selTO).getLabel ());
      }
    }
  }

  private String getCluesText ()
  {
    String osNewLine = System.getProperty("line.separator");    // $NON-NLS-1$
    String txt = osNewLine;
    for (String clue : clues) {
      if (clue != null && !clue.isEmpty ())
        txt += clue.trim () + osNewLine;
    }
    return txt;
  }
}
