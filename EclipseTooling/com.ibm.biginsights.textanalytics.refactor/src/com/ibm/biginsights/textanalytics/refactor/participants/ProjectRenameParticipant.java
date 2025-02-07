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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
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
import com.ibm.biginsights.textanalytics.indexer.proxy.ProjectReferenceProxy;
import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameCoreTexts;
import com.ibm.biginsights.textanalytics.refactor.core.LaunchConfigurationProjectNameChange;
import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * AQL Project rename re-factoring participant responsible for creating a change object and apply to workspace.
 * 
 *  kalakuntla
 */
public class ProjectRenameParticipant extends RenameParticipant
{
	@SuppressWarnings("unused")


  private String oldProjectName;
  private String newProjectName;
  private IProject project;
  private boolean taFileUpdateRequired = false;

  private Map<IFile, ArrayList<Integer>> aqlProjectRefs; // To hold the AQL project references in all other files

  @Override
  protected boolean initialize (Object element)
  {

    aqlProjectRefs = new HashMap<IFile, ArrayList<Integer>> ();
    if (!isSupported (element)) { return false; }
    project = (IProject) element;
    oldProjectName = project.getName ();
    newProjectName = getArguments ().getNewName ();
    
    if (!initSuccessful ()) { return false; }
    return true;
  }

  private boolean initSuccessful ()
  {
    return oldProjectName.length () > 1 && newProjectName.length () > 1;
  }

  private boolean isSupported (Object element)
  {
    return element instanceof IProject;
  }

  @Override
  public String getName ()
  {
    return "Update Project";
  }

  @Override
  public RefactoringStatus checkConditions (IProgressMonitor pm, CheckConditionsContext checkConditionContext) throws OperationCanceledException
  {

    RefactoringStatus result = new RefactoringStatus ();
    if (project == null || !project.exists ()) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRenameDelegate_noSourceFile);
    }
    else if (project.getResourceAttributes ().isReadOnly ()) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRenameDelegate_roFile);
    }
    //Check if the workspace is indexed or not? If indexer thread is still running then stop re-factoring 
    boolean isReindexing = TextAnalyticsIndexer.getInstance().isIndexing();
    if(isReindexing){
      result.addWarning (AQLElementRenameCoreTexts.ToolongWorkpaceBeingIndexed, null);
      return result;
    }
    
    pm.beginTask (AQLElementRenameCoreTexts.AQLElementRenameDelegate_checking, 100);
    // This is to replace/update all project references in workspace
    // Call Indexer for AQL project references by passing all the required params and get references into aqlProjectRefs
    // object
    fetchAllReferencesForAQLProject ();

    pm.worked (50);

    if (checkConditionContext != null) {
      IFile[] projectRefFiles = null; // new IFile[ aqlFiles.size() ];
      if (aqlProjectRefs.size () > 0) {
        projectRefFiles = new IFile[aqlProjectRefs.size ()];
        aqlProjectRefs.keySet ().toArray (projectRefFiles);
      }
      IConditionChecker checker = checkConditionContext.getChecker (ValidateEditChecker.class);
      ValidateEditChecker editChecker = (ValidateEditChecker) checker;
      if (projectRefFiles != null && projectRefFiles.length > 0) {
        editChecker.addFiles (projectRefFiles);
      }
    }
    pm.done ();

    return result;

  }

  private void fetchAllReferencesForAQLProject ()
  {
    // Empty the content of the aqlProjectRefs map..
    aqlProjectRefs.clear ();

    IFile refFile = null;
    int refOffset = 0;
    ArrayList<Integer> offSetList = null;

    // Call the indexer and fetch all the references for the AQL project..
    TextAnalyticsIndexer aqlIndex = TextAnalyticsIndexer.getInstance ();
    List<ProjectReferenceProxy> aqlProjectRefList = aqlIndex.getProjectReferences (oldProjectName);

    for (ProjectReferenceProxy refObj : aqlProjectRefList) {
      // Load the AQL project references map
      refFile = refObj.getFile ();
      if (refFile != null) {
        if(refFile.getFileExtension ().equalsIgnoreCase (Constants.TA_PROPS_EXTENSION_STRING)){
          taFileUpdateRequired = true;
        }
        refOffset = refObj.getOffset ();
        // Push into hash map
        offSetList = aqlProjectRefs.get (refFile);
        if (offSetList == null) {
          offSetList = new ArrayList<Integer> ();
          offSetList.add (refOffset);
          aqlProjectRefs.put (refFile, offSetList);
        }
        else {
          offSetList.add (refOffset);
          aqlProjectRefs.put (refFile, offSetList);
        }
      }  
    }
  }

  @Override
  public Change createPreChange (IProgressMonitor pm) throws CoreException, OperationCanceledException
  {
    CompositeChange result;
    if(taFileUpdateRequired){
      result = new CompositeChange (AQLElementRenameCoreTexts.AQLProjectUpdateReferences);
    }else{
      result = new CompositeChange (AQLElementRenameCoreTexts.AQLUpdateProjectRefsWithoutPropertyFile);  
    }
    createChange (pm, result);
    return result;
  }

  @Override
  public Change createChange (IProgressMonitor pm) throws CoreException, OperationCanceledException
  {
    return null;
  }

  void createChange (final IProgressMonitor pm, final CompositeChange rootChange)
  {
    try {
      pm.beginTask ("Collecting changes for module..", 100);
      rootChange.addAll (createChangesForWorkspace ());
      pm.worked (90);
    }
    finally {
      pm.done ();
    }
  }

  /**
   * This method will create the change for renaming aql project and returns the Change Objects
   */
  private Change[] createChangesForWorkspace ()
  {
    List<Change> result = new ArrayList<Change> ();
    ArrayList<Integer> offsets = null;
    int oldProjectNameLength = oldProjectName.length ();
    String newProjectNameToReplace = newProjectName;

    Iterator<IFile> aqlFileItr = aqlProjectRefs.keySet ().iterator ();
    while (aqlFileItr.hasNext ()) {
      IFile refFile = (IFile) aqlFileItr.next ();

      TextFileChange refFileChanges = new TextFileChange (refFile.getName (), refFile);
      MultiTextEdit fileChangeRootEdit = new MultiTextEdit ();

      offsets = getKeyOffsetsForProjectReferences (refFile);

      // Adding multiple elements occurrences to MultiTextEdit object as child's
      for (Integer offset : offsets) {
        fileChangeRootEdit.addChild (new ReplaceEdit (offset, oldProjectNameLength, newProjectNameToReplace));
      }
      // Edit object for the text replacement in the file, this is the only child
      refFileChanges.setEdit (fileChangeRootEdit);
      result.add (refFileChanges);

    }

    // Add Change objects from launch configurations
    result.addAll (getLaunchConfigChanges (oldProjectName, newProjectName));

    // Return all the change object containing all the required changes.
    return (Change[]) result.toArray (new Change[result.size ()]);
  }

  private ArrayList<Integer> getKeyOffsetsForProjectReferences (final IFile file)
  {
    return aqlProjectRefs.get (file);
  }

  /**
   * Get the 'Change' objects, one for each launch config that contains something related
   * to the (old) project name, e.g., reference external dictionaries/tables inside the project.
   */
  private List<Change> getLaunchConfigChanges (String oldProjectName, String newProjectName)
  {
    try {
      List<Change> list = new ArrayList<Change> ();

      ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
      for(int i = 0; i < configs.length; i++) {
        if(LaunchConfigurationProjectNameChange.lcContainsProjectName (configs[i], oldProjectName))
          list.add(new LaunchConfigurationProjectNameChange (configs[i], oldProjectName, newProjectName));
      }
      return list;
    }
    catch(CoreException e) {
      // we'll return 'no change' when there is trouble getting the run configs.
    }

    return new ArrayList<Change> ();
  }

}
