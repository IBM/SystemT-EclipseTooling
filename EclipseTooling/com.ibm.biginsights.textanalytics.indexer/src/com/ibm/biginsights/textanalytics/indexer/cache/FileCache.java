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
package com.ibm.biginsights.textanalytics.indexer.cache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.biginsights.textanalytics.indexer.Activator;
import com.ibm.biginsights.textanalytics.indexer.Constants;
import com.ibm.biginsights.textanalytics.indexer.impl.ExtractionPlanIndexer;
import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.model.FileReference;
import com.ibm.biginsights.textanalytics.indexer.types.FileType;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Cache of file names and their references
 * 
 *  Krishnamurthy
 */
public class FileCache extends Cache
{
	@SuppressWarnings("unused")


  /**
   * Singleton instance.
   */
  private static FileCache cacheInstance = null;

  /**
   * Key: File Id. <br/>
   * Value: Filetype.Project.[module.]filename
   */
  protected Map<Integer, String> fileMaster = new HashMap<Integer, String> ();

  /**
   * Key: Filetype.Project.[module.]filename <br/>
   * Value: File Id. <br/>
   */
  protected Map<String, Integer> fileMasterReverseLookup = new HashMap<String, Integer> ();

  /**
   * Collection of all file references found in workspace. <br/>
   * Key: File Reference Id <br/>
   * Value: FileRefernce Object <br/>
   */
  protected Map<Integer, FileReference> fileReferenceMaster = new HashMap<Integer, FileReference> ();

  /**
   * Collection of all references to a given file. This map is used for computing list of references to a given file. <br/>
   * Key: elementId <br/>
   * Value: A list of locations where the given file is referenced. i.e list of fileRefIds <br/>
   */
  protected Map<Integer, List<Integer>> referenceMap = new HashMap<Integer, List<Integer>> ();

  /**
   * Returns the singleton instance of the FileCache.
   * 
   * @return FileCache instance
   */
  public static synchronized FileCache getInstance ()
  {
    if (cacheInstance == null) {
      cacheInstance = new FileCache ();
    }

    return cacheInstance;
  }

  /**
   * Updates the Project Name
   * 
   * @param oldProject
   * @param newProject
   */
  public synchronized void updateProjectName (String oldProject, String newProject)
  {
    Set<String> keys = fileMasterReverseLookup.keySet ();

    // temporary map to store new key Vs Id. This is required because updating fileMasterReverseLookup within the
    // iteration below will lead to ConcurrentModificationException
    Map<String, Integer> tempFileMasterRevLookup = new HashMap<String, Integer> ();

    String component = String.format (Constants.QUALIFIED_NAME_SEPARATOR + "%s" + Constants.QUALIFIED_NAME_SEPARATOR,
      oldProject);

    for (Iterator<String> iterator = keys.iterator (); iterator.hasNext ();) {
      String key = (String) iterator.next ();
      if (key.indexOf (component) != -1) {
        Integer fileId = fileMasterReverseLookup.get (key);
        String newKey = key.replace (component,
          String.format (Constants.QUALIFIED_NAME_SEPARATOR + "%s" + Constants.QUALIFIED_NAME_SEPARATOR, newProject));

        fileMaster.put (fileId, newKey);
        iterator.remove ();
        tempFileMasterRevLookup.put (newKey, fileId);
      }
    }// end: for each key

    // copy all items from tempFileMasterRevLookup to fileMasterReverseLookup
    if (false == tempFileMasterRevLookup.isEmpty ()) {
      for (String key : tempFileMasterRevLookup.keySet ()) {
        fileMasterReverseLookup.put (key, tempFileMasterRevLookup.get (key));
      }
    }
  }

  /**
   * Updates the Module Name
   * 
   * @param projectName
   * @param oldModule
   * @param newModule
   */
  public synchronized void updateModuleName (String projectName, String oldModule, String newModule)
  {
    Set<String> keys = fileMasterReverseLookup.keySet ();
    List<String> newModulekeys = new ArrayList<String> ();

    /**
     * In some cases the eclipse listener do not capture events in a proper order, in such cases
     * the file caches will already have indexed the changed file. 
     * 
     *  Sample scenario is Module Rename, the order should be 
     *  1) Call Module Rename event
     *  2) Update the AQL files
     *  
     *  In some cases the eclipse captures (2) first then (1). Then processing of (2) already populated the
     *  File Cache. So we remove the old values from the cache. 
     */
    String newKeyComponent = String.format (Constants.QUALIFIED_NAME_SEPARATOR + "%s" + Constants.QUALIFIED_NAME_SEPARATOR //$NON-NLS-1$
      + "%s" + Constants.QUALIFIED_NAME_SEPARATOR, projectName, newModule); //$NON-NLS-1$

    String oldKeyComponent = String.format (Constants.QUALIFIED_NAME_SEPARATOR + "%s" + Constants.QUALIFIED_NAME_SEPARATOR //$NON-NLS-1$
      + "%s" + Constants.QUALIFIED_NAME_SEPARATOR, projectName, oldModule); //$NON-NLS-1$

    if(keys != null){
      for (String key : keys) {
        if (key.indexOf (newKeyComponent) != -1) {
          newModulekeys.add (key);
        }
      }
    }
    for (String oldModuleKey : newModulekeys) {
      oldModuleKey = oldModuleKey.replace (newKeyComponent, oldKeyComponent);
      Integer fileId = fileMasterReverseLookup.get (oldModuleKey);
      fileMaster.remove (fileId);
      fileMasterReverseLookup.remove (oldModuleKey);
      List<Integer> refIds = referenceMap.get (fileId);
      referenceMap.remove (fileId);
      if(null != refIds){
        for (Integer refId : refIds) {
          fileReferenceMaster.remove (refId);
        }
      }
    }

    if(!newModulekeys.isEmpty ()){
      return;
    }
    
    
    Map<String, Integer> tempfileMasterReverseLookup = new HashMap<String, Integer> ();
    List<String> keysToBeRemoved = new ArrayList<String> ();

    if(keys != null){
      for (String key : keys) {
        if (key.indexOf (oldKeyComponent) != -1) {
          Integer fileId = fileMasterReverseLookup.get (key);
  
          String newKey = key.replace (
            oldKeyComponent,
            String.format (Constants.QUALIFIED_NAME_SEPARATOR + "%s" + Constants.QUALIFIED_NAME_SEPARATOR + "%s" //$NON-NLS-1$ $NON-NLS-2$
              + Constants.QUALIFIED_NAME_SEPARATOR, projectName, newModule));
          fileMaster.put (fileId, newKey);
          keysToBeRemoved.add (key);
          tempfileMasterReverseLookup.put (newKey, fileId);
  
          // fileMasterReverseLookup.remove(key);
          // fileMasterReverseLookup.put(newKey, fileId);
        }
      }
    }

    if (!keysToBeRemoved.isEmpty ()) {
      for (String key : keysToBeRemoved) {
        fileMasterReverseLookup.remove (key);
      }
    }
    if (!tempfileMasterReverseLookup.isEmpty ()) {
      for (String newKey : tempfileMasterReverseLookup.keySet ()) {
        fileMasterReverseLookup.put (newKey, tempfileMasterReverseLookup.get (newKey));
      }
    }

  }

  /**
   * Updates the AQL File
   * 
   * @param projectName
   * @param moduleName
   * @param oldFilename
   * @param newFileName
   */
  public synchronized void updateFileName (String projectName, String moduleName, String oldFilename, String newFileName)
  {
    Set<String> keys = fileMasterReverseLookup.keySet ();
    String component = String.format (Constants.QUALIFIED_NAME_SEPARATOR + "%s" + Constants.QUALIFIED_NAME_SEPARATOR //$NON-NLS-1$
      + "%s" + Constants.QUALIFIED_NAME_SEPARATOR + "%s" , projectName, moduleName, //$NON-NLS-1$ $NON-NLS-2$
      oldFilename);

    // temporary map to store new key Vs Id. This is required because updating fileMasterReverseLookup within the
    // iteration below will lead to ConcurrentModificationException
    Map<String, Integer> tempFileMasterRevLookup = new HashMap<String, Integer> ();

    for (Iterator<String> iterator = keys.iterator (); iterator.hasNext ();) {
      String key = (String) iterator.next ();
      // AQL files: File type, ProjectName, module name, file name
      if (key.indexOf (component) != -1) {
        Integer fileId = fileMasterReverseLookup.get (key);
        String newKey = key.replace (component, String.format (Constants.QUALIFIED_NAME_SEPARATOR + "%s" //$NON-NLS-1$
          + Constants.QUALIFIED_NAME_SEPARATOR + "%s" + Constants.QUALIFIED_NAME_SEPARATOR + "%s" //$NON-NLS-1$ $NON-NLS-2$
          , projectName, moduleName, newFileName));
        fileMaster.put (fileId, newKey);
        iterator.remove ();
        tempFileMasterRevLookup.put (newKey, fileId);

      }
    }

    // copy all items from tempFileMasterRevLookup to fileMasterReverseLookup
    if (false == tempFileMasterRevLookup.isEmpty ()) {
      for (String key : tempFileMasterRevLookup.keySet ()) {
        fileMasterReverseLookup.put (key, tempFileMasterRevLookup.get (key));
      }
    }
  }

  /**
   * Adds the given file to the cache.
   * 
   * @param file IFile object representing the file
   * @return FileID
   */
  public synchronized Integer addFile (IFile file)
  {
    String key = constructQualifiedFileName (file);
    // check if file is already added to cache
    if (fileMasterReverseLookup.containsKey (key)) {
      return fileMasterReverseLookup.get (key);
    }

    // create a new one if not already added
    else {
      Integer fileId = idManager.generateNextSequenceId ();
      addFileInternal (fileId, key);
      return fileId;
    }
  }

  /**
   * Removes the given file from the cache.
   * 
   * @param file IFile object representing the file
   */
  public synchronized void removeFile (IFile file)
  {
    String key = constructQualifiedFileName (file);
    Integer fileId = fileMasterReverseLookup.get(key);
    fileMasterReverseLookup.remove(key);
    fileMaster.remove(fileId);
  }
  
  /**
   * Removes the given file from the cache.
   * 
   * @param file IFile object representing the file
   */
  public synchronized void removeFile (Integer fileId)
  {
    String key = fileMaster.get(fileId);
    fileMasterReverseLookup.remove(key);
    fileMaster.remove(fileId);
  }

  /**
   * Deletes all module ID references in the given file
   * 
   * @param file
   */
  public synchronized void deleteReferencesInFile (IFile file)
  {
    String key = constructQualifiedFileName (file);
    Integer fileId = fileMasterReverseLookup.get(key);
    
    if (fileId == null) return;
    
    deleteReferencesInFile (fileId);
  }

  /**
   * Deletes all File ID references in the given file
   * 
   * @param fileId
   */
  public synchronized void deleteReferencesInFile (Integer fileId)
  {
    Collection<FileReference> fileRefs = fileReferenceMaster.values();

    // Pass 1: Prepare a list of file references to remove
    List<FileReference> toRemove = new ArrayList<FileReference> ();
    if(fileRefs != null){
      for (FileReference fileRef : fileRefs) {
        if (fileRef.getLocation ().getFileId () == fileId.intValue ()) {
          toRemove.add (fileRef);
        }
      }
    }

    // Pass 2: Remove the file references. Two pass approach is required, otherwise ConcurrentModificationException
    // will be thrown
    for (FileReference fileRef : toRemove) {
      removeFileRef (fileRef);
    }
  }

  private void removeFileRef (FileReference fileRef)
  {
    Integer fileRefId = fileRef.getFileRefId();

    // remove from master table
    fileReferenceMaster.remove (fileRefId);

    // remove from child table: referenceMap
    Collection<List<Integer>> refMapValues = referenceMap.values ();
    if(refMapValues != null){
      for (List<Integer> list : refMapValues) {
        list.remove (fileRefId);
      }
    }

  }

  /**
   * Adds a file reference to fileReferenceMaster and referenceMap
   * 
   * @param fileRef FileReference object
   */
  public synchronized void addFileReference (FileReference fileRef)
  {

    // add to fileReferenceMaster
    fileReferenceMaster.put (fileRef.getFileRefId (), fileRef);

    // add to referenceMap
    List<Integer> refList = referenceMap.get (fileRef.getFileId ());
    if (refList == null) {
      refList = new ArrayList<Integer> ();
      referenceMap.put (fileRef.getFileId (), refList);
    }
    refList.add (fileRef.getFileRefId ());
  }

  /**
   * Creates a concatenated file name from the list of components passed to it
   * 
   * @param components Parts of qualified file name, such as project name, module name, file type etc. Since each file
   *          type has its own set of components, this method accepts variable number of arguments. Given below is the
   *          list of valid components:<br/>
   *          AQL files: File type, ProjectName, module name, file name<br/>
   *          Extraction plan files: File type, Project name, file name<br/>
   *          LaunchConfig files: File type, Project name, file name<br/>
   * @return Concatenated file name. i.e fully qualified file name
   */
  protected synchronized Integer getFileId (String... components)
  {
    String key = idManager.createQualifiedKey (components);
    return fileMasterReverseLookup.get (key);
    // if (fileId == null) {
    // fileId = idManager.generateNextSequenceId ();
    // addFileInternal (fileId, key);
    // }
    // return fileId;
  }

  /**
   * Returns the ID of given AQL file. Creates one, if the file is not already indexed.
   * 
   * @param project Project that the file belongs to
   * @param module Module that the file belongs to
   * @param fileName Name of the AQL file
   * @return ID of the file
   */
  public synchronized Integer getAQLFileId (String project, String module, String fileName)
  {
    return getFileId (FileType.AQL.toString (), project, module, fileName);
  }

  /**
   * Returns the AQL File IDs for a given module.
   * 
   * @param project Project that the file belongs to
   * @param module Module that the file belongs to
   * @return IDs of the file under module folder
   */
  public synchronized Integer[] getAQLFileIds (String project, String module)
  {
	  Set<Integer> fileIdSet = new HashSet<Integer>();
	  Set<String> keys = fileMasterReverseLookup.keySet();
	  
	  String searchKey = idManager.createQualifiedKey (FileType.AQL.toString (), project, module);
	  
	  if(keys != null){
  	  for (String key : keys) {
  		  if(key.indexOf(searchKey)!= -1)
  			  fileIdSet.add(fileMasterReverseLookup.get(key));
  	  }
	  }
	  
	  if(fileIdSet.isEmpty())
		  return null;
	  
	  
    return fileIdSet.toArray(new Integer[]{});
  }

  /**
   * Returns the AQL File IDs for a given project.
   * 
   * @param project Project that the file belongs to
   * @return IDs of the file under module folder
   */
  public synchronized Integer[] getAQLFileIds (String project)
  {
	  Set<Integer> fileIdSet = new HashSet<Integer>();
	  Set<String> keys = fileMasterReverseLookup.keySet();
	  
	  String searchKey = idManager.createQualifiedKey (FileType.AQL.toString (), project);
	  
	  for (String key : keys) {
		  if(key.indexOf(searchKey)!= -1)
			  fileIdSet.add(fileMasterReverseLookup.get(key));
	}
	  
	  if(fileIdSet.isEmpty())
		  return null;
	  
	  
    return fileIdSet.toArray(new Integer[]{});
  }

  public synchronized Integer getExtractionPlanFileId (String project)
  {
    // do not include file name because the file begins with a "." causing confusion
    return getFileId (FileType.EPL.toString (), project, ExtractionPlanIndexer.epFileName);
  }

  /**
   * Returns the fileID of the given file.
   * 
   * @param file IFile representation of the file whose ID is requested for.
   * @return fileID
   */
  public synchronized Integer getFileId (IFile file)
  {
    String extension = file.getFileExtension ();
    if (extension != null) {
      if (extension.equals (com.ibm.biginsights.textanalytics.util.common.Constants.AQL_FILE_EXTENSION_STRING)) {
        String project = file.getProject ().getName ();
        String module = file.getParent ().getName ();
        String fileName = file.getName ();
        return getAQLFileId (project, module, fileName);
      }
      else if (extension.equals (com.ibm.biginsights.textanalytics.util.common.Constants.EXTRACTION_PLAN_EXTENSION_STRING)) {
        String project = file.getProject ().getName ();
        return getExtractionPlanFileId (project);
      }
      else if (extension.equals (com.ibm.biginsights.textanalytics.util.common.Constants.TA_PROPS_EXTENSION_STRING)) {
        String project = file.getProject ().getName ();
        return getTextAnalyticsPropertiesFileId (project);
      }
      else if (extension.equals (com.ibm.biginsights.textanalytics.util.common.Constants.LAUNCH_CONFIG_EXTENSION_STRING)) {
        return getLaunchConfigFileId (file.getName ());
      }
      else if (extension.equals (com.ibm.biginsights.textanalytics.util.common.Constants.CLASSPATH_FILE_STRING)) {
        String project = file.getProject ().getName ();
        return getClasspathFileId (project);
      }
    }

    // throw new RuntimeException (String.format ("Unknown file type: %s", extension));

    return null;
  }

  /**
   * Returns the ID of given AQL file. Creates one, if the file is not already indexed.
   * 
   * @param project Project that the file belongs to
   * @param fileName Name of the LaunchConfig file
   * @return ID for the launch configuration file
   */
  public synchronized Integer getLaunchConfigFileId (String fileName)
  {
    return getFileId (FileType.LCG.toString (), fileName);
  }

  public synchronized Integer getTextAnalyticsPropertiesFileId (String project)
  {
    return getFileId (FileType.TAP.toString (), project,
      com.ibm.biginsights.textanalytics.util.common.Constants.TEXT_ANALYTICS_PREF_FILE);
  }

  public synchronized Integer getClasspathFileId (String project)
  {
    return getFileId (FileType.CLP.toString (), project,
      com.ibm.biginsights.textanalytics.util.common.Constants.CLASSPATH_FILE);
  }

  /**
   * Returns the IFile object corresponding to given fileID in file cache, if it exists.
   * 
   * @param fileID Id for file as stored in file cache.
   * @return IFile object for file corresponding to the given id if it exists, else returns null
   */
  public synchronized IFile getFile (Integer fileID)
  {
    IFile ret = null;
    IProject project = null;
    
    // qualifiedFile name is of the format Filetype.Project.[module.]filename
    String qualifiedFileName = fileMaster.get (fileID);
    /**There is some special scenario where the qualifiedFileName will be null. 
       This scenario was not reproducible consistently. This happens for large project 
       that have more than 1000 views and refactoring throws NPE at some point of time. */
    if(qualifiedFileName == null){
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
        String.format ("Could't find the AQL file in File Cache for the file ID %s ", fileID));//$NON-NLS-1$
      return null;
    }
      
    String[] components = qualifiedFileName.split (String.valueOf (Constants.QUALIFIED_NAME_SEPARATOR));
    FileType fileType = FileType.strToFileType (components[0]);
    switch (fileType) {
      case AQL:
        String projectName = components[1];
        String moduleName = components[2];
        String fileName = components[3];
        ret = ProjectUtils.getAqlFile (projectName, moduleName, fileName);
      break;

      case LCG:
        // TODO: find launch config file
      break;
      case EPL:
        String projName = components[1];
        IProject iProj = ProjectUtils.getProject (projName);
        ret = iProj.getFile (ExtractionPlanIndexer.epFileName);
        // TODO: find Extraction plan file
      break;
      case TAP:
        projectName = components[1];
        project = ProjectUtils.getProject (projectName);
        ret = project.getFile (com.ibm.biginsights.textanalytics.util.common.Constants.TEXT_ANALYTICS_PREF_FILE);
      break;
      case CLP:
        projectName = components[1];
        project = ProjectUtils.getProject (projectName);
        ret = project.getFile (com.ibm.biginsights.textanalytics.util.common.Constants.CLASSPATH_FILE);
      break;

    }
    if (ret != null && ret.exists ()) {
      // Since we no longer remove an entry for a file from file cache when the file is deleted, while returning an
      // IFile for a file corresponding to an id in cache, we check if the file actually exists.
      // This is done so that consumers of file cache (e.g. refactoring, f3, etc.) get only valid IFile instances.
      return ret;
    }
    else {
      return null;
    }  
  }

  /**
   * Helper method that places qualified file name and its id into the cache.
   * 
   * @param fileId
   * @param key
   */
  private void addFileInternal (Integer fileId, String key)
  {
    fileMaster.put (fileId, key);
    fileMasterReverseLookup.put (key, fileId);
  }

  /**
   * Creates qualified name of the file
   * 
   * @param file Input file whose qualified name is requested for
   * @return qualified name depending on the file type
   */
  private String constructQualifiedFileName (IFile file)
  {
    String project = file.getProject ().getName ();
    String module = file.getParent ().getName ();
    FileType fileTypeObj = getFileType (file.getFileExtension ());
    String fileType = fileTypeObj.toString ();
    String fileName = file.getName ();
    switch (fileTypeObj) {
      case AQL:
        return idManager.createQualifiedKey (fileType, project, module, fileName);
      case EPL:
        return idManager.createQualifiedKey (fileType, project, fileName);
      case LCG:
        return idManager.createQualifiedKey (fileType, fileName);
      case TAP:
        return idManager.createQualifiedKey (fileType, project, fileName);
      case CLP:
        return idManager.createQualifiedKey (fileType, project, fileName);
    }

    return "";
  }

  /**
   * Converts file extension to FileType object
   * 
   * @param fileExtension Valid extensions are aql, extractionplan, launch, textanalytics
   * @return FileType object of the input file extension
   */
  private FileType getFileType (String fileExtension)
  {
    if (com.ibm.biginsights.textanalytics.util.common.Constants.AQL_FILE_EXTENSION_STRING.equals (fileExtension)) { return FileType.AQL; }

    if (com.ibm.biginsights.textanalytics.util.common.Constants.EXTRACTION_PLAN_EXTENSION_STRING.equals (fileExtension)) { return FileType.EPL; }

    if (com.ibm.biginsights.textanalytics.util.common.Constants.LAUNCH_CONFIG_EXTENSION_STRING.equals (fileExtension)) { return FileType.LCG; }

    if (com.ibm.biginsights.textanalytics.util.common.Constants.TA_PROPS_EXTENSION_STRING.equals (fileExtension)) { return FileType.TAP; }

    if (com.ibm.biginsights.textanalytics.util.common.Constants.CLASSPATH_FILE_STRING.equals (fileExtension)) { return FileType.CLP; }

    return null;
  }

  /*
   * Creates an XML for persisting the index.
   */
  private synchronized String toXML ()
  {
    StringBuffer buff = new StringBuffer ();
    buff.append ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

    buff.append ("<fileCache>");
    if (fileMaster != null && !fileMaster.isEmpty ()) {
      buff.append ("<fileMasterConfig>");
      for (Integer key : fileMaster.keySet ()) {
        buff.append ("<fileMaster id=\"" + key.toString () + "\">");
        buff.append (fileMaster.get (key));
        buff.append ("</fileMaster>");
      }
      buff.append ("</fileMasterConfig>");
    }
    Set<Integer> keySet = null;

    if (fileReferenceMaster != null && !fileReferenceMaster.isEmpty ()) {
      keySet = fileReferenceMaster.keySet ();
      FileReference fileReference = null;
      buff.append ("<fileReferenceMasterConfig>");
      for (Integer key : keySet) {
        fileReference = fileReferenceMaster.get (key);
        buff.append ("<fileReferenceMaster id=\"" + key.toString () + Constants.XML_CLOSE_TAG);

        buff.append ("<fileId>");
        buff.append (fileReference.getFileId ());
        buff.append ("</fileId>");

        buff.append (Constants.XML_LOCATION);

        buff.append ("<offset>");
        buff.append (fileReference.getLocation ().getOffset ());
        buff.append ("</offset>");

        buff.append ("<fileId>");
        buff.append (fileReference.getLocation ().getFileId ());
        buff.append ("</fileId>");

        buff.append (Constants.XML_LOCATION_END);

        buff.append ("</fileReferenceMaster>");
      }
      buff.append ("</fileReferenceMasterConfig>");
    }

    if (referenceMap != null && !referenceMap.isEmpty ()) {
      keySet = referenceMap.keySet ();
      buff.append ("<referenceMapConfig>");
      for (Integer key : keySet) {
        buff.append (Constants.XML_ELEMENT_ID_ATTR + key.toString () + Constants.XML_CLOSE_TAG);
        List<Integer> refList = referenceMap.get (key);
        if (refList != null) {
          for (Integer refId : refList) {
            buff.append (Constants.XML_REF_ID);
            buff.append (refId);
            buff.append (Constants.XML_REF_ID_END);

          }
        }

        buff.append (Constants.XML_ELEMENT_END);
      }
      buff.append ("</referenceMapConfig>");
    }
    buff.append ("</fileCache>");
    return buff.toString ();
  }

  public synchronized void write () throws Exception
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    String workspacePath = root.getLocation ().makeAbsolute ().toOSString ();
    File directory = new File (workspacePath + Constants.INDEX_PATH);
    if (!directory.exists ()) {
      directory.mkdir ();
    }

    File fileMasterConfigFile = new File (directory, Constants.FILE_CACHE_IDX);
    FileOutputStream output = null;
    try {
      output = new FileOutputStream (fileMasterConfigFile);
      String content = toXML ();
      output.write (content.getBytes (Constants.UTF_8));
    }
    catch (IOException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
      throw e;
    }
    finally {
      try {
        if(output != null){
          output.close ();
        }
      }
      catch (IOException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
        throw e;
      }
    }

  }

  public synchronized void load ()
  {

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    String workspacePath = root.getLocation ().makeAbsolute ().toOSString ();
    File directory = new File (workspacePath + Constants.INDEX_PATH);

    File fileMasterConfigFile = new File (directory, Constants.FILE_CACHE_IDX);

    fileMaster.clear ();
    fileMasterReverseLookup.clear ();
    fileReferenceMaster.clear ();
    referenceMap.clear ();

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance ();
    Document doc = null;
    try {
      InputStream in = new FileInputStream (fileMasterConfigFile);
      byte[] bytes = new byte[(int) fileMasterConfigFile.length ()];
      in.read (bytes);
      String fileContent = new String (bytes, Constants.UTF_8);
      InputStream is = new ByteArrayInputStream (fileContent.getBytes (Constants.UTF_8));

      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder ();
      doc = docBuilder.parse (is);
    }
    catch (ParserConfigurationException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
    }
    catch (SAXException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
    }
    catch (IOException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
    }
    if(doc != null && doc.getDocumentElement () != null){
      doc.getDocumentElement ().normalize ();
    }

    NodeList listFileMaster = null;

    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (0) != null) {
      listFileMaster = doc.getChildNodes ().item (0).getChildNodes ().item (0).getChildNodes ();
      for (int i = 0; i < listFileMaster.getLength (); i++) {
        Element modMasterFile = (Element) listFileMaster.item (i);
        Integer fileId = Integer.parseInt (modMasterFile.getAttribute (Constants.ID));
        String file = modMasterFile.getTextContent ();
        fileMaster.put (fileId, file);
        fileMasterReverseLookup.put (file, fileId);
      }
    }

    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (1) != null) {
      listFileMaster = doc.getChildNodes ().item (0).getChildNodes ().item (1).getChildNodes ();
      NodeList list = null;
      FileReference fileReference = null;
      for (int i = 0; i < listFileMaster.getLength (); i++) {
        Element modMasterFile = (Element) listFileMaster.item (i);
        Integer refId = Integer.parseInt (modMasterFile.getAttribute (Constants.ID));
        list = modMasterFile.getElementsByTagName ("fileId");
        int fileId = Integer.parseInt (list.item (0).getTextContent ());

        NodeList locList = modMasterFile.getElementsByTagName (Constants.ELEMENT_LOCATION);
        list = ((Element) locList.item (0)).getElementsByTagName ("offset");
        int offset = Integer.parseInt (list.item (0).getTextContent ());

        list = ((Element) locList.item (0)).getElementsByTagName ("fileId");
        int loc_fileId = Integer.parseInt (list.item (0).getTextContent ());

        fileReference = new FileReference (refId, fileId, new ElementLocation (loc_fileId, offset));
        fileReferenceMaster.put (refId, fileReference);

      }
    }

    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (2) != null) {
      listFileMaster = doc.getChildNodes ().item (0).getChildNodes ().item (2).getChildNodes ();
      List<Integer> refList = null;
      for (int i = 0; i < listFileMaster.getLength (); i++) {
        refList = new ArrayList<Integer> ();
        Element modMasterFile = (Element) listFileMaster.item (i);
        Integer refId = Integer.parseInt (modMasterFile.getAttribute (Constants.ID));

        NodeList refIdlist = modMasterFile.getElementsByTagName (Constants.ELEMENT_REF_ID);
        for (int j = 0; j < refIdlist.getLength (); j++) {
          refList.add (Integer.parseInt (refIdlist.item (j).getTextContent ()));
        }
        referenceMap.put (refId, refList);
      }
    }

  }

  @Override
  public synchronized void clear ()
  {
    fileMaster.clear ();
    fileMasterReverseLookup.clear ();
    fileReferenceMaster.clear ();
    referenceMap.clear ();
  }

  public synchronized List<Integer> getReferences (Integer fileId)
  {
    return referenceMap.get (fileId);
  }

  /**
   * Looks up fileReferenceMaster and returns an FileReference, if one exists.
   * 
   * @param fileRefID
   * @return
   */
  public synchronized FileReference getFileReference (Integer fileRefID)
  {
    return fileReferenceMaster.get (fileRefID);
  }

  public synchronized Integer getAQLFileId (IFile fileToIndex)
  {
    String project = fileToIndex.getProject ().getName ();
    String module = fileToIndex.getParent ().getName ();
    String fileName = fileToIndex.getName ();

    return getAQLFileId (project, module, fileName);
  }

  public synchronized void updateFileName (IFile oldFile, IFile newFile)
  {
    String oldProject = oldFile.getProject ().getName ();
    String oldModule = oldFile.getParent ().getName ();
    String oldFileName = oldFile.getName ();

    Integer fileId = getFileId (oldFile);

    String oldFileType = getFileType (oldFile.getFileExtension ()).toString ();
    String oldKey = idManager.createQualifiedKey (oldFileType, oldProject, oldModule, oldFileName);

    String newProject = newFile.getProject ().getName ();
    String newModule = newFile.getParent ().getName ();
    String newFileName = newFile.getName ();

    String newFileType = getFileType (newFile.getFileExtension ()).toString ();
    String newKey = idManager.createQualifiedKey (newFileType, newProject, newModule, newFileName);

    fileMaster.put (fileId, newKey);
    fileMasterReverseLookup.put (newKey, fileId);
    fileMasterReverseLookup.remove (oldKey);

  }

  /*
   * public void test() { fileMaster = new HashMap<Integer, String>(); fileMaster.put(1, "proj1"); fileMaster.put(2,
   * "proj2"); fileMaster.put(3, "proj3"); fileMasterReverseLookup = new HashMap<String, Integer>();
   * fileMasterReverseLookup.put("proj1", 1); fileMasterReverseLookup.put("proj2", 2);
   * fileMasterReverseLookup.put("proj3", 3); fileReferenceMaster = new HashMap<Integer, FileReference>();
   * fileReferenceMaster.put(4, new FileReference(4, 1, new ElementLocation(1, 1))); fileReferenceMaster.put(5, new
   * FileReference(5, 2, new ElementLocation(1, 1))); fileReferenceMaster.put(6, new FileReference(6, 3, new
   * ElementLocation(1, 1))); referenceMap = new HashMap<Integer, List<Integer>>(); List<Integer> i = new
   * ArrayList<Integer>(); i.add(23); i.add(333); referenceMap.put(7, i); try { write(); } catch (Exception e) { // TODO
   * Auto-generated catch block e.printStackTrace(); } }
   */
}
