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
package com.ibm.biginsights.textanalytics.indexer.index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import com.ibm.biginsights.textanalytics.indexer.Activator;
import com.ibm.biginsights.textanalytics.indexer.DebugMsgConstants;
import com.ibm.biginsights.textanalytics.indexer.cache.ElementCache;
import com.ibm.biginsights.textanalytics.indexer.cache.FileCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ModuleCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ProjectCache;
import com.ibm.biginsights.textanalytics.indexer.impl.AQLFileIndexer;
import com.ibm.biginsights.textanalytics.indexer.impl.ClasspathFileIndexer;
import com.ibm.biginsights.textanalytics.indexer.impl.ExtractionPlanIndexer;
import com.ibm.biginsights.textanalytics.indexer.impl.FileIndexer;
import com.ibm.biginsights.textanalytics.indexer.impl.ModuleScopedElements;
import com.ibm.biginsights.textanalytics.indexer.impl.TAPropertyFileIndexer;
import com.ibm.biginsights.textanalytics.indexer.model.ElementDefinition;
import com.ibm.biginsights.textanalytics.indexer.model.ModuleReference;
import com.ibm.biginsights.textanalytics.indexer.proxy.ElementReferenceProxy;
import com.ibm.biginsights.textanalytics.indexer.proxy.FileReferenceProxy;
import com.ibm.biginsights.textanalytics.indexer.proxy.ModuleReferenceProxy;
import com.ibm.biginsights.textanalytics.indexer.proxy.ProjectReferenceProxy;
import com.ibm.biginsights.textanalytics.indexer.resourcechange.ResourceChangeQueue;
import com.ibm.biginsights.textanalytics.indexer.resourcechange.ResourceChangeReindexThread;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Gateway to the indexer component. Singleton class. Use getInstance() method to get a reference to the singleton
 * instance.
 * 
 *  Krishnamurthy, Nisanth Simon
 */
public class TextAnalyticsIndexer
{
	@SuppressWarnings("unused")


  private static final ILog LOGGER = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);

  private boolean debug = false;

  protected ElementCache elemCache = ElementCache.getInstance ();
  protected ProjectCache projectCache = ProjectCache.getInstance ();
  protected ModuleCache moduleCache = ModuleCache.getInstance ();
  protected FileCache fileCache = FileCache.getInstance ();
  protected IDManager idManager = IDManager.getInstance ();

  /**
   * Singleton instance
   */
  private static TextAnalyticsIndexer instance = null;

  /**
   * Private constructor
   */
  private TextAnalyticsIndexer ()
  {

  }

  public static synchronized TextAnalyticsIndexer getInstance ()
  {
    if (instance == null) {
      instance = new TextAnalyticsIndexer ();
    }

    return instance;
  }

  /**
   * Returns the status of indexing. Returns true, If indexing is still going on.
   * 
   * @return
   */
  public synchronized boolean isIndexing ()
  {
    boolean isQueueEmpty = ResourceChangeQueue.getInstance ().isEmpty ();
    if (!isQueueEmpty) {
      return true; // Queue not empty, so indexing is still going on.
    }
    else {
      boolean isReindexing = ResourceChangeReindexThread.isRenindexing ();
      if (isReindexing) {
        return true; // Indexing is going on.
      }
      else
        return false;
    }

  }

  /**
   * Returns the list of AQL element references for the given element
   * 
   * @param project
   * @param module
   * @param type
   * @param elementName unqualified
   * @return ElementReferenceProxy instance.
   */
  public List<ElementReferenceProxy> getElementReferences (String project, String module, ElementType type,
    String elementName)
  {
    List<ElementReferenceProxy> ret = new ArrayList<ElementReferenceProxy> ();

    Integer elementId = elemCache.getElementId (project, module, type, elementName);
    if (elemCache.isElementActive (elementId)) { //check if the id corresponds to an 'active' element.
      Set<Integer> elemRefs = elemCache.getReferenceIdsForElementDef (elementId);
      if (null != elemRefs) {
        for (Integer elRef : elemRefs) {      
            ret.add (new ElementReferenceProxy (elRef));            
        }
      }
    }
    
    return ret;
  }

  /**
   * Searches the workspace for references to elements defined in the given file
   * 
   * @param file File containing element definitions, whose references are to be searched in the entire workspace
   * @return List of matching references
   * @throws Exception
   */
  public List<ElementReferenceProxy> getReferencesToElemsDefinedInFile (IFile file) throws Exception
  {
    List<ElementReferenceProxy> ret = new ArrayList<ElementReferenceProxy> ();

    // Step 1: Get all element definitions in the given file
    List<ElementDefinition> elemDefsInFile = elemCache.getElementDefinitionsInFile (file);

    for (ElementDefinition elemDef : elemDefsInFile) {
      Integer elemId = elemDef.getElementId ();

      // Step 2: Get all references to the given element definition id
      Set<Integer> refIds = elemCache.getReferenceIdsForElementDef (elemId);

      if(refIds != null){
        // Step 3: Create ElementReferenceProxy for each reference id
        for (Integer refId : refIds) {
          ret.add (new ElementReferenceProxy (refId));
        }
      }
    }
    return ret;
  }

  /**
   * Returns the list of AQL file references for the given AQL file
   * 
   * @param project
   * @param module
   * @param file
   * @return FileReferenceProxy instance.
   */
  public List<FileReferenceProxy> getFileReferences (IFile file)
  {
    List<FileReferenceProxy> ret = new ArrayList<FileReferenceProxy> ();

    if (file == null) return ret;

    Integer fileId = fileCache.getFileId (file);
    List<Integer> referenceIds = fileCache.getReferences (fileId);

    if (null != referenceIds) {
      for (Integer refId : referenceIds) {
        ret.add (new FileReferenceProxy (refId));
      }
    }

    return ret;
  }

  /**
   * Returns the list of AQL module references for the given module. The scope of search is entire workspace.
   * 
   * @param project Name of the project that the module belongs to
   * @param module Name of the module whose references are requested for
   * @return A list of ModuleReferenceProxy objects.
   */
  public List<ModuleReferenceProxy> getModuleReferences (String project, String module)
  {
    List<ModuleReferenceProxy> ret = new ArrayList<ModuleReferenceProxy> ();

    IFolder mFolder = ProjectUtils.getModuleFolder (project, module);
    if (mFolder != null && mFolder.exists ()) {
      Integer moduleId = moduleCache.getModuleId (project, module);
      Set<Integer> referenceIds = moduleCache.getReferences (moduleId);

      if (null != referenceIds) {
        for (Integer refId : referenceIds) {
          ret.add (new ModuleReferenceProxy (refId));
        }
      }
    }
    

    return ret;
  }

  /**
   * Returns the list of AQL module references for a given module, in a given file. The scope of search is within the
   * given file.
   * 
   * @param project Name of the project that the module belongs to
   * @param module Name of the module whose references are requested for
   * @param file File within which module references are to be searched for
   * @return A list of ModuleReferenceProxy objects
   */
  public List<ModuleReferenceProxy> getModuleReferences (String project, String module, IFile file) throws Exception
  {
    List<ModuleReferenceProxy> ret = new ArrayList<ModuleReferenceProxy> ();

    Integer fileId = fileCache.getFileId (file);
    if (fileId == null) { throw new Exception (String.format ("The specified file %s is not indexed yet.", //$NON-NLS-1$
      file.getLocation ().toOSString ())); }
    List<ModuleReferenceProxy> list = getModuleReferences (project, module);
    for (ModuleReferenceProxy moduleRef : list) {
      if (fileId.equals (moduleRef.getFileId ())) {
        ret.add (moduleRef);
      }
    }
    return ret;
  }

  /**
   * Returns the list of AQL project references for the given project
   * 
   * @param project
   * @return ProjectReferenceProxy instance.
   */
  public List<ProjectReferenceProxy> getProjectReferences (String project)
  {
    List<ProjectReferenceProxy> ret = new ArrayList<ProjectReferenceProxy> ();

    IProject prjkt = ProjectUtils.getProject (project);
    if (prjkt != null && prjkt.exists ()) {
      Integer projectId = projectCache.getProjectId (project);
      List<Integer> referenceIds = projectCache.getReferences (projectId);

      if (null != referenceIds) {
        for (Integer refId : referenceIds) {
          ret.add (new ProjectReferenceProxy (refId));
        }
      }
    }

    return ret;
  }

  /**
   * Internal method for indexing / re-indexing
   * 
   * @throws Exception
   */
  public void reindex () throws Exception
  {
    elemCache.clear ();
    projectCache.clear ();
    moduleCache.clear ();
    fileCache.clear ();

    String event = "Indexer caches discarded. Re-indexing"; //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, event));
    // reindex projects
    reindexProjects ();

    LOGGER.logDebug (String.format(DebugMsgConstants.END_EVENT, event)); //$NON-NLS-1$
    
    LOGGER.logInfo ("Indexing of workspace completed"); //$NON-NLS-1$

  }


  /**
   * Reindexes all projects in current workspace
   * 
   * @throws Exception
   */
  private void reindexProjects () throws Exception
  {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace ().getRoot ();
    IProject projects[] = workspaceRoot.getProjects ();
    for (IProject project : projects) {
      // We have to index only modular projects
      if (true == project.isOpen ()
        && true == project.hasNature (com.ibm.biginsights.textanalytics.util.common.Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject (project)) {
        reindexProject (project);
      }

    }
  }

  protected void reindexProject (IProject project)
  {
    LOGGER.logDebug (String.format(DebugMsgConstants.BEGIN_INDEX,"Project:"+project.getName ())); //$NON-NLS-1$
    // reindex modules
    reindexModuleFolderContents (project);

    // reindex extraction plan
    reindexExtractionPlan (project);

    // reindex property sheet
    reindexTAProperties (project);

    // reindex the classpath
    reindexClasspath (project);

    // Launch Configuration updates are taken care by LaunchConfigurationProjectNameChange class.

    LOGGER.logDebug(String.format (DebugMsgConstants.END_INDEX,"Project:"+project.getName ())); //$NON-NLS-1$
  }

  private void reindexClasspath (IProject project)
  {
    String indexItemLabel = "Classpath for project:"+project.getName (); //$NON-NLS-1$
    
    LOGGER.logDebug (String.format(DebugMsgConstants.BEGIN_INDEX,indexItemLabel));
    //indexedFiles Set will contain a list of files which are indexed.
    Set<String> indexedFiles = new HashSet<String> ();
    try {
      IFile classPathFile = project.getFile (Constants.CLASSPATH_FILE);
      if (classPathFile.exists ()) {
        reindexFile (classPathFile, indexedFiles);
      }
    }
    catch (Exception e) {
      LOGGER.logError (String.format ("Error indexing .classpath file for project: %s", project.getName ()), e); //$NON-NLS-1$
      return;
    }

    LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEX, indexItemLabel));
  }

  private void reindexTAProperties (IProject project)
  {
    String indexItemLabel = "Text Analytics properties for project:"+project.getName (); //$NON-NLS-1$
    
    //indexedFiles Set will contain a list of files which are indexed.
    LOGGER.logDebug (String.format(DebugMsgConstants.BEGIN_INDEX,indexItemLabel));
    Set<String> indexedFiles = new HashSet<String> ();
    try {
      IFile taPropsFile = project.getFile (Constants.TEXT_ANALYTICS_PREF_FILE);
      if (taPropsFile.exists ()) {
        reindexFile (taPropsFile, indexedFiles);
      }
    }
    catch (Exception e) {
      LOGGER.logError (String.format ("Error indexing .textanalytics file for project: %s", project.getName ()), e); //$NON-NLS-1$
      return;
    }

    LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEX, indexItemLabel));
  }



  private void reindexExtractionPlan (IProject project)
  {
    String indexItemLabel = "Extraction plan for project:"+project.getName (); //$NON-NLS-1$
    
    LOGGER.logDebug (String.format(DebugMsgConstants.BEGIN_INDEX,indexItemLabel));
    //indexedFiles Set will contain a list of files which are indexed.
    Set<String> indexedFiles = new HashSet<String> ();
    try {
      IFile epFile = project.getFile (ExtractionPlanIndexer.epFileName);
      if (epFile.exists ()) {
        reindexFile (epFile, indexedFiles);
      }
    }
    catch (Exception e) {
      LOGGER.logError (String.format ("Error indexing extraction plan for project: %s", project.getName ()), e); //$NON-NLS-1$
      return;
    }

    LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEX, indexItemLabel)); //$NON-NLS-1$
  }

  private void reindexModuleFolderContents (IProject project)
  {
    try {

      IFolder srcFolder = ProjectUtils.getTextAnalyticsSrcFolder (project);// Get the source folder1
      if (null != srcFolder && srcFolder.exists ()) {
        IResource members[] = null;
        try {
          members = srcFolder.members ();
        }
        catch (CoreException e) {
          LOGGER.logError (String.format ("Unable to get members of folder: %s", srcFolder.getName ()), e); //$NON-NLS-1$
          return;
        }

        for (IResource member : members) {
          if (member instanceof IFolder) {
            IFolder moduleFolder = (IFolder) member;
            reindexModuleFolder (moduleFolder, false); //Do not re-index referring projects here.
          }
        }
      }
    }
    catch (Exception e) {
      LOGGER.logError (String.format ("Error indexing module folder contents for project: %s", project.getName ()), e); //$NON-NLS-1$
      return;
    }
  }

  /**
   * Reindexes module folder
   * 
   * @param moduleFolder module folder to be reindexed.
   * @param reIndexReferringProjects boolean value for whether referring projects should be reindexed. Do not set to true
   *          unless absolutely required.
   * @throws Exception
   */
  public void reindexModuleFolder (IFolder moduleFolder, boolean reIndexReferringProjects)
  {
    String indexItemLabel = "Module:"+moduleFolder.getFullPath ().toString (); //$NON-NLS-1$
    
    LOGGER.logDebug (String.format(DebugMsgConstants.BEGIN_INDEX,indexItemLabel));
    try {
      String moduleName = moduleFolder.getName ();
      ModuleScopedElements.clearModule (moduleName);
      //indexedFiles Set will contain a list of files which are indexed.
      Set<String> indexedFiles = new HashSet<String> ();
      IResource files[] = null;
      try {
        files = moduleFolder.members ();
      }
      catch (CoreException e) {
        LOGGER.logError (String.format ("Unable to get members of folder: %s", moduleFolder.getName ()), e); //$NON-NLS-1$
        return;
      }
      for (IResource file : files) {
        if (file instanceof IFile) {
          indexedFiles.clear ();
          reindexFile ((IFile) file, indexedFiles);
        }
      }
      
      if (reIndexReferringProjects) {
        IProject[] refProjects = moduleFolder.getProject().getReferencingProjects ();
        if (refProjects.length > 0) {
          String msg = "Indexing projects that may refer to module:"+moduleFolder.getFullPath ().toString (); //$NON-NLS-1$
          LOGGER.logDebug (String.format(DebugMsgConstants.BEGIN_EVENT, msg));
          
          for (IProject ref : refProjects) {
            if (ref.exists () && ProjectUtils.isModularProject (ref)) { //Checks if the project is a BI project and is modular.
              reindexProject(ref);
            }
          }
          
          LOGGER.logDebug (String.format(DebugMsgConstants.END_EVENT, msg));
        }
        
      }
    }
    catch (Exception e) {
      LOGGER.logError (String.format ("Error indexing module folder: %s", moduleFolder.getName ()), e); //$NON-NLS-1$
      return;
    }

    LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEX, indexItemLabel));
  }

  /**
   * Re-indexes the given file
   * 
   * @param file File to re-index. Can be an aql file, extraction plan, .textanalytics file, .classpath file.
   * @throws Exception
   */
  private void reindexFile (IFile file, Set<String> indexedFiles)
  {
    String indexItemLabel = "File:"+file.getFullPath ().toString (); //$NON-NLS-1$
    
    LOGGER.logDebug (String.format(DebugMsgConstants.BEGIN_INDEX,indexItemLabel));
    // refresh the file first, to get the latest copy of the file.
    try {
      file.refreshLocal (IResource.DEPTH_ZERO, null);
    }
    catch (CoreException e) {
      LOGGER.logError (String.format ("Unable to refresh file %s", file.getName ()), e); //$NON-NLS-1$
    }

    // if file is already indexed
    if (fileCache.getFileId (file) != null) {

      // Pass 1: delete any element references in current file
      elemCache.deleteReferencesInFile (file);
      moduleCache.deleteReferencesInFile (file);

      // Pass 2: Deactivate any element definitions in current file
      elemCache.deactivateDefinitionsInFile (file);
    }

    // Pass 3: Re-index current file
    FileIndexer indexer = null;

    if (Constants.AQL_FILE_EXTENSION_STRING.equals (file.getFileExtension ())) {
      indexer = new AQLFileIndexer ();
    }
    else if (ExtractionPlanIndexer.epFileName.equals (file.getName ())) {
      indexer = new ExtractionPlanIndexer ();
    }
    else if (Constants.TEXT_ANALYTICS_PREF_FILE.equals (file.getName ())) {
      indexer = new TAPropertyFileIndexer ();
    }
    else if (Constants.CLASSPATH_FILE.equals (file.getName ())) {
      indexer = new ClasspathFileIndexer ();
    }

    if (indexer != null && indexedFiles != null) {
      try {
        indexer.indexFileContents (file);
        // Adding the file that was indexed to the indexedFiles Set, such that the file will not
        // be indexed again
        indexedFiles.add (file.getLocation ().toOSString ());
      }
      catch (Exception e) {
        LOGGER.logError (String.format ("Error indexing file: %s", file.getFullPath ().toString ()), e); //$NON-NLS-1$
      }
    }
    else {
      if (indexer == null) {
        LOGGER.logDebug ("Did not index file " + file.getFullPath ().toString ()); //$NON-NLS-1$
      }
    }

    LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEX, indexItemLabel));
  }

  // ================== BEGIN: Event handlers ================================
  
  /**
   * This API got invoked, when there is file rename operation.
   * 
   * @param oldFile
   * @param newFile
   * @throws Exception
   */
  public void fileRenamed (IFile oldFile, IFile newFile) throws Exception
  {
    String indexEventLabel = String.format ("File Renamed : %s to %s", oldFile.getFullPath ().toString (),
      newFile.getFullPath ().toString ());
    
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    
    // Update the file references in the file cache to new file name.
    fileCache.updateFileName (oldFile, newFile);
    
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * This API got invoked, when there is file added operation.
   * 
   * @param file
   * @throws Exception
   */
  public void fileAdded (IFile file) throws Exception
  {
    String indexEventLabel = String.format("File Added : %s",file.getFullPath ()); //$NON-NLS-1$
    LOGGER.logDebug (String.format(DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    
    reindexFile (file, new HashSet<String>()); 

    LOGGER.logDebug (String.format(DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * Indexer action for when an file is deleted.
   * 
   * @param file IFile instance of the AQL file that got deleted.
   * @throws Exception 
   */
  public void fileDeleted (IFile file) throws Exception
  {
    String qualifiedFileName = file.getFullPath ().toString ();
    String indexEventLabel = "File Deleted : " + qualifiedFileName; //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));

    Integer fileId = fileCache.getFileId (file);
    // if file is already indexed
    if (fileId != null) {
      fileDeleted (fileId);
    }

    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * Indexer action for when an file with given Id is deleted.
   * 
   * @param fileId Id of the file as stored in File cache.
   * @throws Exception
   */
  private void fileDeleted (Integer fileId) throws Exception
  {
    String indexEventLabel = "File Deleted : " + fileId; //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    if (fileId != null) {

      // Pass 1: delete any element references in current file
      elemCache.deleteReferencesInFile (fileId);
      moduleCache.deleteReferencesInFile (fileId);
      fileCache.deleteReferencesInFile (fileId);
      LOGGER.logDebug ("Removed all element references made in file with id : " + fileId); //$NON-NLS-1$

      // Pass 2: Deactivate any element definitions in current file
      elemCache.deactivateDefinitionsInFile (fileId);
      LOGGER.logDebug ("Deactivated all element definitions made in file with id : " + fileId); //$NON-NLS-1$

      // Do not remove the entry for this file from file cache. If this file is added again,
      // we will reuse the entry.
    }
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));

    // NOTE: This method does not remove references made in file to the project name. References to the project name
    // would have been made in Text analytics properties file, class path file and the extraction plan.
  }

  /**
   * This API got invoked, when an AQL file got updated.
   * 
   * @param file
   * @throws Exception
   */
  public void fileUpdated (IFile file) throws Exception
  {
    String indexEventLabel = "File updated : "+file.getFullPath ().toString (); //$NON-NLS-1$
    //indexedFiles Set will contain a list of files which are indexed.
    Set<String> indexedFiles = new HashSet<String> ();
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel)); //$NON-NLS-1$
    
    reindexFile (file, indexedFiles);
    
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * This API got invoked, when there is file move operation.
   * 
   * @param oldFile
   * @param newFile
   * @throws Exception
   */
  public void fileMoved (IFile oldFile, IFile newFile) throws Exception
  {
    String indexEventLabel = String.format ("File moved : %s to %s", oldFile.getFullPath ().toString (), //$NON-NLS-1$
      newFile.getFullPath ().toString ());
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    // The file move event is deleting the reference of old file and adding the
    // the references of new file. Hence we call fileDelete then call fileAdded.
    fileDeleted (oldFile);
    fileAdded (newFile);

    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * This API got invoked, when a project gets added to the workspace.
   * 
   * @param project
   * @throws Exception
   */
  public void projectAdded (IProject project) throws Exception
  {
    String indexEventLabel = "Project Added : "+project.getName(); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    //Checks whether Project is opened and has TextAnalytics Nature.
    if (true == project.isOpen ()
      && true == project.hasNature (com.ibm.biginsights.textanalytics.util.common.Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject (project)) {

      projectSrcAdded(project);

      projectExtractionPlanAdded (project);
      
      projectPropertiesAdded (project);

    }
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }
  
  public void projectSrcAdded(IProject project) throws CoreException{
    String indexEventLabel = "Project source added : "+project.getName(); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    if (true == project.isOpen ()
        && true == project.hasNature (com.ibm.biginsights.textanalytics.util.common.Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject (project)) {
      reindexModuleFolderContents (project);
      
      
    }
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }
  
  public void projectExtractionPlanAdded(IProject project) throws CoreException{
    String indexEventLabel = "Project extraction plan added : "+project.getName(); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    if (true == project.isOpen ()
        && true == project.hasNature (com.ibm.biginsights.textanalytics.util.common.Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject (project)) {
      reindexExtractionPlan (project);
    }
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }
  
  public void projectPropertiesAdded(IProject project) throws CoreException{
    String indexEventLabel = "Project properties files added : "+project.getName(); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    if (true == project.isOpen ()
        && true == project.hasNature (com.ibm.biginsights.textanalytics.util.common.Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject (project)) {
      reindexTAProperties (project);
      reindexClasspath (project);
    }
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * This API got invoked, when a project got deleted in workspace.
   * 
   * @param project
   * @throws Exception
   */
  public void projectDeleted (IProject project) throws Exception
  {
    String indexEventLabel = "Project deleted : " + project.getName (); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));

    projectSrcDeleted (project);
    
    projectExtractionPlanAndPropertiesDeleted (project);

    // Do not delete the project entries from Project Master Cache.
    // If a project is brought back, the entry will be reused.

    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }
  
  /**
   * Indexer action when the text analytics source directory of a project is deleted.
   * 
   * @param project
   * @throws Exception
   */
  public void projectSrcDeleted (IProject project) throws Exception
  {
    String indexEventLabel = "Project source deleted : " + project.getName (); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    
    IFolder srcFolder = ProjectUtils.getTextAnalyticsSrcFolder (project);
    if (srcFolder != null) {
      for (IResource module : srcFolder.members ()) {
        if (module instanceof IFolder) {
          moduleDeleted ((IFolder) module); // call module deleted action on each module in text analytics src folder
        }
      }
    }
    
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }
  
  /**
   * Indexer action when the extraction plan, the text analytics properties file and the classpath file within the
   * project are deleted.
   * 
   * @param project the project whose files got deleted.
   * @throws Exception
   */
  public void projectExtractionPlanAndPropertiesDeleted (IProject project) throws Exception
  {
    String indexEventLabel = "Project properties deleted : " + project.getName (); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    
    // Delete references made within the extraction plan to other elements.
    Integer fileId = fileCache.getExtractionPlanFileId (project.getName ());
    if (fileId != null) {
      fileDeleted (fileId);
    }
    // Text analytics properties file and class files would have only added project references.
    // So no need to call fileDeleted for them.

    // Project references would have come from extraction plan and the properties files.
    // As all have been removed, remove all project references within this project.
    projectCache.deleteReferencesInProject (project.getName ());
    
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }
  
  /**
   * Indexer action for when project src is moved.
   * @param project
   * @throws Exception
   */
  public void projectSrcMoved (IProject project) throws Exception
  {
    String indexEventLabel = "Project source moved : "+project.getName (); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    projectSrcDeleted (project);
    projectSrcAdded (project);
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * This API got invoked, when a project is closed in a workspace.
   * 
   * @param project
   * @throws Exception
   */
  public void projectClosed (IProject project) throws Exception
  {
    String indexEventLabel = "Project closed : "+project.getName (); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel)); 
    // When a project get closed, we call projectDeleted API to remove all the references from the caches.
    projectDeleted (project);
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * This API got invoked, when a project is closed in a workspace.
   * 
   * @param project
   * @throws Exception
   */
  public void projectOpened (IProject project) throws Exception
  {
    String indexEventLabel = "Project opened : "+project.getName (); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel)); 
    // When a project gets opened, we call project added to add references to the cache.
    projectAdded (project);
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * This API got invoked, when a project is renamed.
   * 
   * @param oldProject
   * @param newProject
   * @throws Exception
   */
  public void projectRenamed (IProject oldProject, IProject newProject) throws Exception
  {
    String indexEventLabel = String.format ("Project renamed : %s to %s", oldProject.getName (), newProject.getName ()); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));

    LOGGER.logDebug ("Simulate rename action by calling on delete action for old project name followed by add action for new project name.");
    
    projectDeleted (oldProject);
    refreshProjectRename (newProject);
    projectAdded (newProject);

    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));

  }

  /**
   * Refresh the files when a project gets renamed.
   * 
   * @param newProject
   */
  private void refreshProjectRename (IProject newProject)
  {
    try {
      /**
       * The fix for defect 35610. If we refresh the entire project using 
       * newProject.refreshLocal (IResource.DEPTH_INFINITE, null);
       * This may take long time and effects the performance. So we selectively
       * refresh the Project at level 1 and We refresh the entire src folder and
       * refresh the tam path at level of 1.
       * 
       * Tested it many times, if we get the Resource Sync issue, we may need to
       * use 
       * newProject.refreshLocal (IResource.DEPTH_INFINITE, null);
       */
      
      // Refresh the project at level 1.
      newProject.refreshLocal (IResource.DEPTH_ONE, null);
      
      // Refresh the files in src folder.
      IFolder srcFolder = ProjectUtils.getTextAnalyticsSrcFolder (newProject);
      if(srcFolder != null)
        newProject.refreshLocal (IResource.DEPTH_INFINITE, null);
      
     // Refresh the tams in bin folder.
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
      String path = ProjectUtils.getConfiguredModuleBinPath (newProject);
      if (null != path && !path.isEmpty ()){
        IFolder tamPathRes = root.getFolder (new Path (path));
        tamPathRes.refreshLocal (IResource.DEPTH_INFINITE, null);
      }
      
      // Refresh the provenance folder
      IFolder provenanceDir = ProjectUtils.getDefaultProvenanceDir (newProject);
      provenanceDir.refreshLocal (IResource.DEPTH_INFINITE, null);

      /**
       * End of fix for defect 35610.
       */
    }
    catch (CoreException e) {
      LOGGER.logError (String.format ("Unable to refresh file %s", newProject.getName ()), e); //$NON-NLS-1$
    }
  }

  /**
   * Indexer action for when a module is added.
   * 
   * @param folder
   * @throws Exception
   */
  public void moduleAdded (IFolder folder) throws Exception
  {
    String indexEventLabel = "Module added  : " + folder.getFullPath ().toString (); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel)); //$NON-NLS-1$
    
    reindexModuleFolder (folder, true); 
    // Passing 'true' to reindex referring projects to cover an edge case.
    // Consider projects A, B & C. Project C is dependent on project A & B.
    // Let X be the module in A, that C is dependent on.
    // On moving module X from A to B (or deleting in A and adding it in B), we do not delete references to X in C, nor
    // drop the X's entries in module cache, and reindex X.
    // We expect reindex of X to reuse the entries in module cache but that does not happen (because the key in module
    // cache is project.module, and in this case, the project has changed), and X is assigned a new id.
    // The references in C are left dangling. We have to reindex C so that it uses X's new id.
    // This step is not required elsewhere, because in all other cases, user will have to make edits
    // in the referring resources to make it compile correctly, and trigger re-index anyway.

    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * This API got invoked, when a module is renamed
   * 
   * @param oldFolder
   * @param newFolder
   * @throws Exception
   */
  public void moduleRenamed (IFolder oldFolder, IFolder newFolder) throws Exception
  {
    String indexEventLabel = String.format("Module renamed : %s to %s", oldFolder.getName (), newFolder.getName ()); //$NON-NLS-1$
    //indexedFiles Set will contain a list of files which are indexed.
    Set<String> indexedFiles = new HashSet<String> ();
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    
    String projectName = newFolder.getProject ().getName ();
    String oldModuleName = oldFolder.getName ();
    String newModuleName = newFolder.getName ();
    Set<Integer> fileIdSet = new HashSet<Integer> ();
    
    /**
     * We need to get all the referenced files for old module name. These referenced files will be
     * reindexed again to update the references.
     */
    Set<Integer> references = moduleCache.getReferences (moduleCache.getModuleId (projectName, oldModuleName));
    if(references != null){
      for (Integer reference : references) {
        ModuleReference moduleReference = moduleCache.getModuleReference (reference);
        fileIdSet.add (moduleReference.getLocation ().getFileId ());
      }
    }

    // Pass 1: Update the caches with the new name.
    moduleCache.updateModuleName (projectName, oldModuleName, newModuleName);
    fileCache.updateModuleName (projectName, oldModuleName, newModuleName);
    elemCache.updateModuleName (projectName, oldModuleName, newModuleName);
    
    // Pass 2: Reindex the files under the module
    IResource resources[] = newFolder.members ();
    for (IResource resource : resources) {
      if(resource instanceof IFile && Constants.AQL_FILE_EXTENSION_STRING.equals (((IFile)resource).getFileExtension ())){
        indexedFiles.clear ();
        reindexFile ((IFile)resource, indexedFiles);
      }
    }
    // Pass 3: Reindexing the reference files
    for (Integer filId : fileIdSet) {
      IFile iFile = FileCache.getInstance ().getFile (filId);
      if(iFile != null && Constants.AQL_FILE_EXTENSION_STRING.equals (iFile.getFileExtension ())){
        indexedFiles.clear ();
        reindexFile (iFile, indexedFiles);
      }
    }
    
    LOGGER.logDebug (String.format(DebugMsgConstants.END_EVENT, indexEventLabel));

  }

  /**
   * This API got invoked, when a module is deleted.
   * 
   * @param folder
   * @throws Exception
   */
  public void moduleDeleted (IFolder folder) throws Exception
  {
    String indexEventLabel = "Module deleted : "+folder.getFullPath ().toString (); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel)); 

    String projectName = folder.getProject ().getName ();
    String moduleName = folder.getName ();
    
    // Pass 1: Get all the AQL files in a module.
    Integer[] fileIds = fileCache.getAQLFileIds (projectName, moduleName);
    
    // Pass 2: Call file deleted action for the aql files
    if (fileIds != null) {
      for (Integer fileId : fileIds) {
        fileDeleted (fileId);
      }
    }
    
    //Do not remove the entry for the module from module cache.
    //If the module is added again, the entry will be reused.
    
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * This API got invoked, when a module is moved from one location to other.
   * 
   * @param oldFolder
   * @param newFolder
   * @throws Exception
   */
  public void moduleMoved (IFolder oldFolder, IFolder newFolder) throws Exception
  {
    String indexEventLabel = String.format ("Module moved : %s to %s", oldFolder.getFullPath ().toString (), //$NON-NLS-1$
      newFolder.getFullPath ().toString ());
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));
    moduleDeleted (oldFolder);
    moduleAdded (newFolder);
    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  /**
   * Indexer action for when the text analytics properties for a project are modified.
   * 
   * @param file IFile instance of the .textanalytics file for the project
   * @throws Exception
   */
  public void projectPropertiesUpdated (IFile file) throws Exception
  {
    String indexEventLabel = "Text Analytics properties updated : " + file.getFullPath ().toString (); //$NON-NLS-1$
    LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_EVENT, indexEventLabel));

    // For now, re-index the project. Revisit this code to see if it's possible to detect what changed in the properties
    // file.
    LOGGER.logDebug ("Removing old references in project");
    projectDeleted (file.getProject ());

    LOGGER.logDebug ("Indexing the project again");
    projectAdded (file.getProject ());

    LOGGER.logDebug (String.format (DebugMsgConstants.END_EVENT, indexEventLabel));
  }

  // ================== END: Event handlers ================================
}
