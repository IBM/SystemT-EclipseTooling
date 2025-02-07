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

package com.ibm.biginsights.textanalytics.refactor.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.IConditionChecker;
import org.eclipse.ltk.core.refactoring.participants.ValidateEditChecker;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import com.ibm.biginsights.textanalytics.indexer.index.TextAnalyticsIndexer;
import com.ibm.biginsights.textanalytics.indexer.proxy.ElementReferenceProxy;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * AQL element rename re-factoring delegate responsible for creating a change object and apply to workspace.
 * 
 *  kalakuntla
 */
class AQLElementRenameDelegate
{
	@SuppressWarnings("unused")


  private final AQLElementRenameInfo aqlElementRenameInfo;

  private final Map<IFile, ArrayList<Integer>> aqlElementRefs; // To hold the AQL element references in all files

  /**
   * Constructor to initialize the AQL element rename info object and aqlElement Ref files
   */
  AQLElementRenameDelegate (final AQLElementRenameInfo info)
  {
    this.aqlElementRenameInfo = info;
    aqlElementRefs = new HashMap<IFile, ArrayList<Integer>> ();
  }

  /**
   * Checks some initial conditions based on the element to be refactored. The refactoring is considered as not being
   * executable if the returned status has the severity of RefactoringStatus#FATAL.
   * This method is called by the AQL Rename processor.
   * @return Outcome of condition check
   */
  RefactoringStatus checkInitialConditions ()
  {
    RefactoringStatus result = new RefactoringStatus ();
    IFile sourceFile = aqlElementRenameInfo.getSourceFile ();
    if (sourceFile == null || !sourceFile.exists ()) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRenameDelegate_noSourceFile);
    }
    else if (aqlElementRenameInfo.getSourceFile ().isReadOnly ()) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRenameDelegate_roFile);
    }
    else if (!(ProjectUtils.isModularProject (sourceFile.getProject ()))) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRefactoringNotSupportedForNonModularProjects);
    } // check if the token being renamed corresponds to an aql element in its defining statement. If not, return error.
    else if (!isEmpty (aqlElementRenameInfo.getOldName ())
      && !IndexerUtil.isElementDefinition (aqlElementRenameInfo.getEleType (), aqlElementRenameInfo.getProject (),
        aqlElementRenameInfo.getModule (), aqlElementRenameInfo.getOldName (), aqlElementRenameInfo.getSourceFile (),
        aqlElementRenameInfo.getOffset ())) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRenameDelegate_notValidRefactorCandidate);
    }
    else if (isEmpty (aqlElementRenameInfo.getOldName ())
      || !isAQLElement (aqlElementRenameInfo.getEleType ().toString ())) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRenameDelegate_noAQLElement);
    }
    return result;
  }

  RefactoringStatus checkFinalConditions (final IProgressMonitor pm, final CheckConditionsContext checkConditionContext)
  {
    RefactoringStatus result = new RefactoringStatus ();
    
    if(ProjectUtils.isAQLKeyword (aqlElementRenameInfo.getNewName ())){
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementNameCanNotBeKeyword);
    }
    //Check if the workspace is indexed or not? If indexer thread is still running then stop re-factoring 
    boolean isReindexing = TextAnalyticsIndexer.getInstance().isIndexing();
    if(isReindexing){
       result.addWarning (AQLElementRenameCoreTexts.ToolongWorkpaceBeingIndexed, null);
       return result;
    }
    
    pm.beginTask (AQLElementRenameCoreTexts.AQLElementRenameDelegate_checking, 100);

    // This is to replace/update the references in .AQL files..
    // Call Indexer for AQL Element file references by passing all the required params and get references into
    // aqlElementRefs object
    fetchAQLfileReferences ();
    pm.worked (90);

    if (checkConditionContext != null) {
      IFile[] aqlElementRefFiles = null;
      if (aqlElementRefs.size () > 0) {
        aqlElementRefFiles = new IFile[aqlElementRefs.size ()];
        aqlElementRefs.keySet ().toArray (aqlElementRefFiles);
      }
      IConditionChecker checker = checkConditionContext.getChecker (ValidateEditChecker.class);
      ValidateEditChecker editChecker = (ValidateEditChecker) checker;
      if ((aqlElementRefFiles != null) && (aqlElementRefFiles.length > 0)) {
        editChecker.addFiles (aqlElementRefFiles);
      }
    }

    pm.done ();
    return result;
  }

  private void fetchAQLfileReferences ()
  {
    // Empty the content of the aqlElementRefs map..
    aqlElementRefs.clear ();

    String projName = aqlElementRenameInfo.getProject ();
    String moduleName = aqlElementRenameInfo.getModule ();
    ElementType eleType = aqlElementRenameInfo.getEleType ();
    String eleName = aqlElementRenameInfo.getOldName ();
    //If the element Name is enclosed in double quotes, then for the indexer
    //to identify the element we have to strip of the enclosing doubles quotes.
    if (eleName.startsWith ("\"") && eleName.endsWith ("\""))
      eleName = eleName.substring (1, eleName.length () - 1);
    IFile refFile = null;
    int refOffset = 0;
    ArrayList<Integer> offSetList = null;

    // Call the indexer and fetch all the references for the AQL element..
    TextAnalyticsIndexer aqlIndex = TextAnalyticsIndexer.getInstance ();
    List<ElementReferenceProxy> aqlEleRefList = aqlIndex.getElementReferences (projName, moduleName, eleType, eleName);
    for (ElementReferenceProxy refObj : aqlEleRefList) {
      // Load the AQLElement references map
      refFile = refObj.getFile ();
      if (refFile != null) {
        refOffset = refObj.getOffset ();
        // Push into hash map
        offSetList = aqlElementRefs.get (refFile);
        if (offSetList == null) {
          offSetList = new ArrayList<Integer> ();
          offSetList.add (refOffset);
          aqlElementRefs.put (refFile, offSetList);
        }
        else {
          offSetList.add (refOffset);
          aqlElementRefs.put (refFile, offSetList);
        }
      }    
    }
  }

  void createChange (final IProgressMonitor pm, final CompositeChange rootChange)
  {
    try {
      pm.beginTask (AQLElementRenameCoreTexts.AQLElementRenameDelegate_collectingChanges, 100);
      if (aqlElementRenameInfo.isUpdateProject ()) {
        rootChange.addAll (createChangesForWorkSpace ());
      }
      pm.worked (90);
      if (aqlElementRenameInfo.isUpdateWorkspace ()) {
        rootChange.addAll (createChangesForWorkSpace ());
      }
      pm.worked (90);
    }
    finally {
      pm.done ();
    }
  }

  /**
   * This method will create the change for renaming aql element and returns the Change Objects
   */
  private Change[] createChangesForWorkSpace ()
  {
    List<TextFileChange> result = new ArrayList<TextFileChange> ();
    ArrayList<Integer> offsets = null;
    int oldEleNameLength = aqlElementRenameInfo.getOldName ().length ();
    String newEleNameToReplace = aqlElementRenameInfo.getNewName ();

    Iterator<IFile> aqlFileItr = aqlElementRefs.keySet ().iterator ();
    while (aqlFileItr.hasNext ()) {
      IFile aqlFile = (IFile) aqlFileItr.next ();
      TextFileChange aqlFileChanges = new TextFileChange (aqlFile.getName (), aqlFile);
      MultiTextEdit fileChangeRootEdit = new MultiTextEdit ();

      offsets = getKeyOffsetsForEleReferences (aqlFile);

      // Adding multiple elements occurrences to MultiTextEdit object as childs
      for (Integer offset : offsets) {
        fileChangeRootEdit.addChild (new ReplaceEdit (offset, oldEleNameLength, newEleNameToReplace));
      }

      // Edit object for the text replacement in the file, this is the only child
      aqlFileChanges.setEdit (fileChangeRootEdit);
      result.add (aqlFileChanges);
    }
    // Return all the change object containing all the required changes.
    return (Change[]) result.toArray (new Change[result.size ()]);
  }

  private boolean isEmpty (final String candidate)
  {
    return candidate == null || candidate.trim ().length () == 0;
  }

  private boolean isAQLElement (String elementType)
  {
    boolean result = false;
    // Need to check all other conditions(type of aql elements) as well..
    if (elementType.equals (Constants.AQL_ELEMENT_TYPE_VIEW) || (elementType.equals (Constants.AQL_ELEMENT_TYPE_DICT))
      || (elementType.equals (Constants.AQL_ELEMENT_TYPE_FUNC))
      || (elementType.equals (Constants.AQL_ELEMENT_TYPE_TABLE))) {
      result = true;
    }
    return result;
  }

  private ArrayList<Integer> getKeyOffsetsForEleReferences (final IFile file)
  {
    return aqlElementRefs.get (file);
  }

}
