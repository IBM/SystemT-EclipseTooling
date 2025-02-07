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
package com.ibm.biginsights.textanalytics.refactor.participants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.IConditionChecker;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import com.ibm.biginsights.textanalytics.indexer.index.TextAnalyticsIndexer;
import com.ibm.biginsights.textanalytics.indexer.proxy.FileReferenceProxy;
import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameCoreTexts;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * AQL File rename re-factoring participant responsible for creating a change object and apply to workspace.
 * 
 *  kalakuntla
 */
public class FileRenameParticipant extends RenameParticipant
{
	@SuppressWarnings("unused")


  private String oldFilePath;
  private String newFilePath;
  private IFile srcFile;
  private Map<IFile, ArrayList<Integer>> aqlFileRefs; // To hold the AQL file references in all other files
  private String oldFileName;
  private String newFileName;
  private String moduleName;
  private String projectName;

  @Override
  protected boolean initialize (Object element)
  {

    aqlFileRefs = new HashMap<IFile, ArrayList<Integer>> ();
    if (!isSupported (element)) { return false; }
    srcFile = (IFile) element;
    oldFilePath = getOldFilePath (srcFile);
    newFilePath = getNewFilePath (oldFilePath);
    oldFileName = srcFile.getName ();
    newFileName = getArguments ().getNewName ();
    projectName = srcFile.getProject ().getName ();
    moduleName = srcFile.getParent ().getName ();

    // Check if both old and new file paths are not empty..
    if (!isValid (oldFilePath, newFilePath)) { return false; }
    return true;
  }

  private boolean isValid (String oldFilePath, String newFilePath)
  {
    return !oldFilePath.equals ("") && !newFilePath.equals ("");//$NON-NLS-1$ //$NON-NLS-1$
  }

  private String getNewFilePath (String oldFilePath)
  {
    String newName = getArguments ().getNewName ();
    int lastIndexOf = oldFilePath.lastIndexOf ("/");//$NON-NLS-1$
    if (lastIndexOf > -1) { return oldFilePath.substring (0, lastIndexOf + 1) + newName; }
    return oldFilePath;
  }

  private String getOldFilePath (IFile file)
  {
    return file.getFullPath ().toOSString ();
  }
  private boolean isSupported (Object element)
  {
    return element instanceof IFile;
  }

  @Override
  public String getName ()
  {
    return "Update files";//$NON-NLS-1$
  }

  @Override
  public RefactoringStatus checkConditions (IProgressMonitor pm, CheckConditionsContext checkConditionContext) throws OperationCanceledException
  {

    RefactoringStatus result = new RefactoringStatus ();
    if (srcFile == null || !srcFile.exists ()) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRenameDelegate_noSourceFile);
    }
    else if (srcFile.isReadOnly ()) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRenameDelegate_roFile);
    }
    if((srcFile.getFileExtension () != null) && (srcFile.getFileExtension ().equalsIgnoreCase (Constants.AQL_FILE_EXTENSION_STRING))){
    //check if file name is valid name 
      boolean isValidScriptName = ProjectUtils.isValidName (newFileName.split ("\\.")[0]);//$NON-NLS-1$
      if (!isValidScriptName) {
        result.addFatalError (AQLElementRenameCoreTexts.AQLScriptRename_notValidScriptName);
      }
    }
    // Check if the workspace is indexed or not? If indexer thread is still running then stop re-factoring
    boolean isReindexing = TextAnalyticsIndexer.getInstance ().isIndexing ();
    if (isReindexing) {
      result.addWarning (AQLElementRenameCoreTexts.ToolongWorkpaceBeingIndexed, null);
      return result;
    }

    pm.beginTask (AQLElementRenameCoreTexts.AQLElementRenameDelegate_checking, 100);

    // This is to replace/update all file references in workspace
    // Call Indexer for AQL file references by passing all the required params and get references into aqlFileRefs
    // object
    
    fetchAllReferencesForAQLFile ();

    pm.worked (50);

    if (checkConditionContext != null) {
      IFile[] aqlFileRefFiles = null;
      if (aqlFileRefs.size () > 0) {
        aqlFileRefFiles = new IFile[aqlFileRefs.size ()];
        aqlFileRefs.keySet ().toArray (aqlFileRefFiles);
      }
      IConditionChecker checker = checkConditionContext.getChecker (ValidateEditChecker.class);
      ValidateEditChecker editChecker = (ValidateEditChecker) checker;
      if ((aqlFileRefFiles != null) && (aqlFileRefFiles.length > 0)) {
        editChecker.addFiles (aqlFileRefFiles);
      }

    }

    pm.done ();
    return result;
  }

  @Override
  public Change createChange (IProgressMonitor pm) throws CoreException, OperationCanceledException
  {

    CompositeChange result = new CompositeChange ("Rename File");//$NON-NLS-1$
    createChange (pm, result);
    return result;
  }

  void createChange (final IProgressMonitor pm, final CompositeChange rootChange)
  {
    try {
      pm.beginTask ("Collecting changes for file..", 100);//$NON-NLS-1$
      rootChange.addAll (createChangesForWorkspace ());
      pm.worked (90);
    }
    finally {
      pm.done ();
    }
  }

  /**
   * This method will create the change for renaming aql file and returns the Change Objects
   */
  private Change[] createChangesForWorkspace ()
  {
    List<TextFileChange> result = new ArrayList<TextFileChange> ();
    ArrayList<Integer> offsets = null;
    int oldFileNameLength = oldFileName.length ();
    String newFileNameToReplace = newFileName;

    Iterator<IFile> aqlFileItr = aqlFileRefs.keySet ().iterator ();
    while (aqlFileItr.hasNext ()) {
      IFile refFile = (IFile) aqlFileItr.next ();

      TextFileChange refFileChanges = new TextFileChange (refFile.getName (), refFile);
      MultiTextEdit fileChangeRootEdit = new MultiTextEdit ();

      offsets = getKeyOffsetsForFileReferences (refFile);

      // Adding multiple elements occurrences to MultiTextEdit object as childs
      for (Integer offset : offsets) {
        fileChangeRootEdit.addChild (new ReplaceEdit (offset, oldFileNameLength, newFileNameToReplace));
      }
      // Edit object for the text replacement in the file, this is the only child
      refFileChanges.setEdit (fileChangeRootEdit);
      result.add (refFileChanges);
    }
    // Return all the change object containing all the required changes.
    return (Change[]) result.toArray (new Change[result.size ()]);
  }

  private void fetchAllReferencesForAQLFile ()
  {
    // Empty the content of the aqlFileRefs map..
    aqlFileRefs.clear ();
    
    IFile refFile = null;
    int refOffset = 0;
    ArrayList<Integer> offSetList = null;
    List<FileReferenceProxy> aqlFileRefList = null;

    // Call the indexer and fetch all the references for the AQL file..
    TextAnalyticsIndexer aqlIndex = TextAnalyticsIndexer.getInstance ();
    if((srcFile.getFileExtension () != null) && (srcFile.getFileExtension ().equalsIgnoreCase (Constants.AQL_FILE_EXTENSION_STRING))){
      aqlFileRefList = aqlIndex.getFileReferences (ProjectUtils.getAqlFile (projectName,
        moduleName, oldFileName));
    }else{
      aqlFileRefList = new ArrayList<FileReferenceProxy> ();
    }
    
    for (FileReferenceProxy refObj : aqlFileRefList) {
      // Load the AQL File references map
      refFile = refObj.getFile ();
      refOffset = refObj.getOffset ();
      // Push into hash map
      if (refFile != null) {
        offSetList = aqlFileRefs.get (refFile);
        if (offSetList == null) {
          offSetList = new ArrayList<Integer> ();
          offSetList.add (refOffset);
          aqlFileRefs.put (refFile, offSetList);
        }
        else {
          offSetList.add (refOffset);
          aqlFileRefs.put (refFile, offSetList);
        }
      }      
    }
  }

  private ArrayList<Integer> getKeyOffsetsForFileReferences (final IFile file)
  {
    return aqlFileRefs.get (file);
  }

}
