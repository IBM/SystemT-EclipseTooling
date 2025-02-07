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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
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

import com.ibm.biginsights.textanalytics.aql.editor.syntax.AQLSyntaxElements;
import com.ibm.biginsights.textanalytics.indexer.index.TextAnalyticsIndexer;
import com.ibm.biginsights.textanalytics.indexer.proxy.ModuleReferenceProxy;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;
import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameCoreTexts;
import com.ibm.biginsights.textanalytics.refactor.core.LaunchConfigurationModuleNameChange;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * AQL Module rename re-factoring participant responsible for creating a change object and apply to workspace.
 * 
 *  kalakuntla
 */
public class ModuleRenameParticipant extends RenameParticipant
{
	@SuppressWarnings("unused")


  private String oldModuleName;
  private String newModuleName;
  private String projectName;
  private IFolder moduleFolder;
  private Map<IFile, ArrayList<Integer>> aqlModuleRefs; // To hold the AQL module references in all other files
  private Map<IFile, List<Integer>> aqlFolderRefs; // To hold the folder references
  private String QUOTE_STRING = "\"";//$NON-NLS-1$
  private boolean isModuleFolder;
  private IFile taRefFile;

  @Override
  protected boolean initialize (Object element)
  {
    aqlModuleRefs = new HashMap<IFile, ArrayList<Integer>> ();
    aqlFolderRefs = new HashMap<IFile, List<Integer>> ();
    if (!isSupported (element)) { return false; }
    IFolder module = (IFolder) element;
    moduleFolder = module;
    oldModuleName = moduleFolder.getName ();
    newModuleName = getArguments ().getNewName ();
    projectName = moduleFolder.getProject ().getName ();
    
    isModuleFolder = ProjectUtils.isValidModule(moduleFolder.getProject(), module.getName());

    if (!initSuccessful ()) { return false; }
    return true;
  }

  private boolean initSuccessful ()
  {
    return oldModuleName.length () > 1 && newModuleName.length () > 1;
  }

  private boolean isSupported (Object element)
  {
    return element instanceof IFolder;
  }

  @Override
  public String getName ()
  {
    return "Update Module";
  }

  @SuppressWarnings("deprecation")
  @Override
  public RefactoringStatus checkConditions (IProgressMonitor pm, CheckConditionsContext checkConditionContext) throws OperationCanceledException
  {

    RefactoringStatus result = new RefactoringStatus ();
    if (moduleFolder == null || !moduleFolder.exists ()) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRenameDelegate_noSourceFile);
    }
    else if (moduleFolder.isReadOnly ()) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLElementRenameDelegate_roFile);
    }
    else if ((isProvananceFolder (moduleFolder.getProject (), moduleFolder))) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLModuleRename_provananceFolder);
    }
    else if((isModuleFolder)&&(!(ProjectUtils.isValidName (newModuleName))))  {
      result.addFatalError (AQLElementRenameCoreTexts.AQLModuleRename_notValidFolderName);
    }
    else if ((isModuleFolder)&&(ProjectUtils.isAQLKeyword (newModuleName))) {
      result.addFatalError (AQLElementRenameCoreTexts.AQLModuleNameCanNotBeKeyword);
    }
    // Check if the workspace is indexed or not? If indexer thread is still running then stop re-factoring
    boolean isReindexing = TextAnalyticsIndexer.getInstance ().isIndexing ();
    if (isReindexing) {
      result.addWarning (AQLElementRenameCoreTexts.ToolongWorkpaceBeingIndexed, null);
      return result;
    }
    //ProjectUtils.isTextAnalyticsFolder (moduleFolder) ||
    if(ProjectUtils.isConfiguredSrcFolder (moduleFolder) || ProjectUtils.isConfiguredBinFolder (moduleFolder)){
    	isModuleFolder = false;
    	pm.beginTask (AQLElementRenameCoreTexts.AQLElementRenameDelegate_checking, 100);
        // Call Indexer for folder references 
        fetchAllReferencesForFolder();

        pm.worked (50);

        if (checkConditionContext != null) {
          IFile[] folderRefFiles = null;
          if (aqlFolderRefs.size () > 0) {
        	  folderRefFiles = new IFile[aqlFolderRefs.size ()];
            aqlFolderRefs.keySet ().toArray (folderRefFiles);
          }
          IConditionChecker checker = checkConditionContext.getChecker (ValidateEditChecker.class);
          ValidateEditChecker editChecker = (ValidateEditChecker) checker;
          if ((folderRefFiles != null) && (folderRefFiles.length > 0)) {
            editChecker.addFiles (folderRefFiles);
          }
        }
        pm.done ();
        return result;
    }
    
    pm.beginTask (AQLElementRenameCoreTexts.AQLElementRenameDelegate_checking, 100);
    // This is to replace/update all module references in workspace
    // Call Indexer for AQL module references by passing all the required params and get references into aqlModuleRefs
    // object
    fetchAllReferencesForAQLModule ();

    pm.worked (50);

    if (checkConditionContext != null) {
      IFile[] moduleRefFiles = null;
      if (aqlModuleRefs.size () > 0) {
        moduleRefFiles = new IFile[aqlModuleRefs.size ()];
        aqlModuleRefs.keySet ().toArray (moduleRefFiles);
      }
      IConditionChecker checker = checkConditionContext.getChecker (ValidateEditChecker.class);
      ValidateEditChecker editChecker = (ValidateEditChecker) checker;
      if ((moduleRefFiles != null) && (moduleRefFiles.length > 0)) {
        editChecker.addFiles (moduleRefFiles);
      }
    }
    pm.done ();
    return result;
  }

  // Checks if the newModule name is the keyword or not
  private boolean isNewModuleNameKeyword (String newName)
  {
    List<String> keyWordList = Arrays.asList (AQLSyntaxElements.KEYWORDS);
    if (keyWordList.contains (newName)) { return true; }
    return false;
  }

  private boolean isProvananceFolder (IProject project, IFolder moduleFolder)
  {
    IFolder provananceFolder = ProjectUtils.getProvenanceFolder (project.getName ());
    if (provananceFolder == null) { return false; }
    if (provananceFolder.getName ().equals (moduleFolder.getParent ().getParent ().getName ())) {
      return true;
    }
    else if (moduleFolder.getLocation ().toOSString ().contains (Constants.DEFAULT_PROVENANCE_FOLDER)) { return true; }
    return false;
  }

  private void fetchAllReferencesForAQLModule ()
  {
    // Empty the content of the aqlModuleRefs map..
    aqlModuleRefs.clear ();

    IFile refFile = null;
    int refOffset = 0;
    ArrayList<Integer> offSetList = null;
    List<ModuleReferenceProxy> aqlModuleRefList = null;

    boolean isValidModuleFolderResource = ProjectUtils.isIntrestedModuleFolder (moduleFolder);
    // Call the indexer and fetch all the references for the AQL module..
    TextAnalyticsIndexer aqlIndex = TextAnalyticsIndexer.getInstance ();
    if (isValidModuleFolderResource) {
      aqlModuleRefList = aqlIndex.getModuleReferences (projectName, oldModuleName);
    }
    else {
      aqlModuleRefList = new ArrayList<ModuleReferenceProxy> ();
    }

    for (ModuleReferenceProxy refObj : aqlModuleRefList) {
      // Load the AQL module references map
      refFile = refObj.getFile ();
      refOffset = refObj.getOffset ();
      // Push into hash map
      if (refFile != null) {
        offSetList = aqlModuleRefs.get (refFile);
        if (offSetList == null) {
          offSetList = new ArrayList<Integer> ();
          offSetList.add (refOffset);
          aqlModuleRefs.put (refFile, offSetList);
        }
        else {
          offSetList.add (refOffset);
          aqlModuleRefs.put (refFile, offSetList);
        }
      }
      
    }
  }
  
  /**
   *  This method is used to fetch all the references for the folder to be renamed.
   *  These references will be used by the createFolderChangesForWorkspace API. 
   */
  private void fetchAllReferencesForFolder ()
  {
	// Empty the content of the aqlModuleRefs map..
    aqlFolderRefs.clear ();
    IFile refFile = null;
    int refOffset = 0;
    File taFile = ProjectUtils.getPreferenceStoreFile (moduleFolder.getProject());
    IPath taFilePath = Path.fromOSString((taFile.getAbsolutePath()).toString());
    refFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(taFilePath);
    taRefFile = refFile;
    ArrayList<Integer> offSetList = new ArrayList<Integer> ();
    if(ProjectUtils.isConfiguredSrcFolder (moduleFolder)){
    	refOffset = IndexerUtil.getSrcOffset(moduleFolder);
    	offSetList.add(refOffset);
    }else if(ProjectUtils.isConfiguredBinFolder(moduleFolder)){
    	refOffset = IndexerUtil.getTamOffset(moduleFolder);
    	offSetList.add(refOffset);
    	/**
    	 * Looking for the bin folder references in other projects i.e identify the module.TAMPath
    	 * references in TA files and add them to folder reference map with proper offsets 
    	 */
    	Map<IFile, Integer> taFileRefOffsetPairs = IndexerUtil.getBinOffSetInPreCompiledTamPropertyOfOtherProjects (moduleFolder.getProject ());
    	List<Integer> refTAFileOffSetList;
    	for (Map.Entry<IFile, Integer> taFileRefOffsetPair : taFileRefOffsetPairs.entrySet ()) {
    	  refTAFileOffSetList = new ArrayList<Integer> ();
    	  refTAFileOffSetList.add (taFileRefOffsetPair.getValue ());
        aqlFolderRefs.put (taFileRefOffsetPair.getKey (), refTAFileOffSetList);
      }
    }
    aqlFolderRefs.put (taRefFile, offSetList);
  }
  
  @Override
  public Change createPreChange (IProgressMonitor pm) throws CoreException, OperationCanceledException
  {
	CompositeChange result;
	if(isModuleFolder){
		result = new CompositeChange ("Rename module and update references");	//$NON-NLS-1$
	}else{
		result = new CompositeChange ("Rename folder and update references");  //$NON-NLS-1$
	}
	createChange (pm, result);
    return result;
  }

  @Override
  public Change createChange (IProgressMonitor pm) throws CoreException, OperationCanceledException
  {
    // CompositeChange result = new CompositeChange( "Rename Module" );
    // createChange( pm, result );
    // return result;
    return null;
  }

  void createChange (final IProgressMonitor pm, final CompositeChange rootChange)
  {
    try {
      pm.beginTask ("Collecting changes for module..", 100);
      if(isModuleFolder){
    	  rootChange.addAll (createChangesForWorkspace ());  
      }else{
    	  rootChange.addAll (createFolderChangesForWorkspace ());
      }
      
      pm.worked (90);
    }
    finally {
      pm.done ();
    }
  }

  /**
   * This method will create the change for renaming aql module and returns the Change Objects
   */
  private Change[] createChangesForWorkspace ()
  {
    List<Change> result = new ArrayList<Change> ();
    ArrayList<Integer> offsets = null;
    int oldModuleNameLength = oldModuleName.length ();
    String newModuleNameToReplace = "";
    // Added if conditions to handle the offsets when module name enclosed in double quotes.
    if (isNewModuleNameKeyword (newModuleName) && (!(isNewModuleNameKeyword (oldModuleName)))) {
      newModuleNameToReplace = QUOTE_STRING + newModuleName + QUOTE_STRING;
    }
    else if (isNewModuleNameKeyword (oldModuleName) && (!(isNewModuleNameKeyword (newModuleName)))) {
      newModuleNameToReplace = " " + newModuleName; // Adding spaces to remove quotes..
    }
    else {
      newModuleNameToReplace = newModuleName;
    }

    Iterator<IFile> aqlFileItr = aqlModuleRefs.keySet ().iterator ();
    while (aqlFileItr.hasNext ()) {
      IFile refFile = (IFile) aqlFileItr.next ();

      TextFileChange refFileChanges = new TextFileChange (refFile.getName (), refFile);
      MultiTextEdit fileChangeRootEdit = new MultiTextEdit ();

      offsets = getKeyOffsetsForModuleReferences (refFile);

      // Adding multiple elements occurrences to MultiTextEdit object as child's
      for (Integer offset : offsets) {
        // Added if conditions to handle the offsets when module name enclosed in double quotes.
        if (isNewModuleNameKeyword (oldModuleName) && (!(isNewModuleNameKeyword (newModuleName)))) {
          fileChangeRootEdit.addChild (new ReplaceEdit (offset - 1, oldModuleNameLength + 3, newModuleNameToReplace));
        }
        else if (isNewModuleNameKeyword (oldModuleName) && (isNewModuleNameKeyword (newModuleName))) {
          fileChangeRootEdit.addChild (new ReplaceEdit (offset + 1, oldModuleNameLength + 1, newModuleNameToReplace
            + "\""));//$NON-NLS-1$  // append quote to new name.. 
        }
        else {
          fileChangeRootEdit.addChild (new ReplaceEdit (offset, oldModuleNameLength, newModuleNameToReplace));
        }
      }
      // Edit object for the text replacement in the file, this is the only child
      refFileChanges.setEdit (fileChangeRootEdit);
      result.add (refFileChanges);

    }
    
    // Add Change objects from launch configurations
    result.addAll (getLaunchConfigChanges (projectName, oldModuleName, newModuleName));

    
    // Return all the change object containing all the required changes.
    return (Change[]) result.toArray (new Change[result.size ()]);
  }
  
  /**
   * Get the 'Change' objects, one for each launch config that contains something related
   * to the project name and old Module.
   */
  private List<Change> getLaunchConfigChanges (String projectName, String oldModuleName, String newModuleName)
  {
    try {
      List<Change> list = new ArrayList<Change> ();

      ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
      for(int i = 0; i < configs.length; i++) {
        if(LaunchConfigurationModuleNameChange.lcContainsModuleName (configs[i], projectName, oldModuleName))
          list.add(new LaunchConfigurationModuleNameChange (configs[i], projectName, oldModuleName, newModuleName));
      }
      return list;
    }
    catch(CoreException e) {
      // we'll return 'no change' when there is trouble getting the run configs.
    }

    return new ArrayList<Change> ();
  }
  
  /**
   * This method will create the changes for folder
   */
  private Change[] createFolderChangesForWorkspace ()
  {
    List<TextFileChange> result = new ArrayList<TextFileChange> ();
    List<Integer> offsets = null;

    Iterator<IFile> aqlFileItr = aqlFolderRefs.keySet ().iterator ();
    while (aqlFileItr.hasNext ()) {
      IFile refFile = (IFile) aqlFileItr.next ();
      TextFileChange refFileChanges = new TextFileChange (refFile.getName (), refFile);
      //TextFileChange refFileChanges = new TextFileChange (taRefFile.getName (), taRefFile);
      MultiTextEdit fileChangeRootEdit = new MultiTextEdit ();

      offsets = getKeyOffsetsForFolderReferences (refFile);

      // Adding multiple elements occurrences to MultiTextEdit object as child's
      for (Integer offset : offsets) {
        // Added if conditions to handle the offsets when module name enclosed in double quotes.
          fileChangeRootEdit.addChild (new ReplaceEdit (offset, oldModuleName.length(), newModuleName));
      }
      // Edit object for the text replacement in the file, this is the only child
      refFileChanges.setEdit (fileChangeRootEdit);
      result.add (refFileChanges);

    }
    // Return all the change object containing all the required changes.
    return (Change[]) result.toArray (new Change[result.size ()]);
  }

  private ArrayList<Integer> getKeyOffsetsForModuleReferences (final IFile file)
  {
    return aqlModuleRefs.get (file);
  }
  
  private List<Integer> getKeyOffsetsForFolderReferences (final IFile file)
  {
    return aqlFolderRefs.get (file);
  }

}
