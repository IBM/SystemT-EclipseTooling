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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.biginsights.textanalytics.indexer.Activator;
import com.ibm.biginsights.textanalytics.indexer.Constants;
import com.ibm.biginsights.textanalytics.indexer.index.IDManager;
import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.model.ProjectReference;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Maintains list of projects and their references.
 * 
 *  Krishnamurthy
 */
public class ProjectCache extends Cache
{
	@SuppressWarnings("unused")

 
	/**
   * Singleton instance of the cache
   */
  private static ProjectCache cacheInstance = null;

  /**
   * Key: projectId <br/>
   * Value: Project name <br/>
   */
  protected Map<Integer, String> projectMaster = new HashMap<Integer, String> ();

  /**
   * Key: Project name <br/>
   * Value: projectId <br/>
   */
  protected Map<String, Integer> projectMasterReverseLookup = new HashMap<String, Integer> ();

  /**
   * Key: Project Reference Id <br/>
   * Value : ProjectReference object
   */
  Map<Integer, ProjectReference> projectReferenceMaster = new HashMap<Integer, ProjectReference> ();

  /**
   * Key: Module Id <br/>
   * Value: List of all references to this module
   */
  Map<Integer, List<Integer>> referenceMap = new HashMap<Integer, List<Integer>> ();

  public static synchronized ProjectCache getInstance ()
  {
    if (cacheInstance == null) {
      cacheInstance = new ProjectCache ();
    }
    return cacheInstance;
  }

  /**
   * Looks up the project cache and returns an id for the name. Creates an entry if none exists.
   * 
   * @param projectName
   * @return
   */
  public synchronized Integer getProjectId (String projectName)
  {
    Integer projectId = projectMasterReverseLookup.get (projectName);
    if (projectId == null) {
      projectId = addProject (projectName);
    }
    return projectId;
  }

  /**
   * Returns the project name for a given project id.
   * 
   * @param id
   * @return
   */
  public synchronized String getProjectName (Integer projectID)
  {
    return projectMaster.get (projectID);
  }

  /**
   * Adds an entry to the projectMaster and projectMasterReverseLookup maps
   * 
   * @param projectName name of the project to add
   */
  public synchronized Integer addProject (String projectName)
  {
    Integer projectId = projectMasterReverseLookup.get (projectName);
    // return existing project id, if any
    if (null != projectId) { return projectId; }

    // create a new entry, if one does not exist
    projectId = IDManager.getInstance ().generateNextSequenceId ();
    projectMaster.put (projectId, projectName);
    projectMasterReverseLookup.put (projectName, projectId);
    return projectId;
  }

  /**
   * Updates the existing project with new project name.
   * 
   * @param oldProjectName
   * @param newProjectName
   */
  public synchronized void updateProjectName (String oldProjectName, String newProjectName)
  {
    Integer projectId = projectMasterReverseLookup.get (oldProjectName);
    // Just return if the given project name is not already indexed
    if (null == projectId) { return; }

    // Update the existing entry, if one found
    projectMasterReverseLookup.remove (oldProjectName);
    projectMasterReverseLookup.put (newProjectName, projectId);
    // overwrite the entry in project master
    projectMaster.put (projectId, newProjectName);
  }

  /**
   * Delete the project from cache
   * 
   * @param projectName
   */
  public synchronized void deleteProject (String projectName)
  {
    Integer projectId = projectMasterReverseLookup.get (projectName);

    // Just return if the given project name is not already indexed
    if (null == projectId) { return; }

    // delete the entry from both projectMaster and projectMasterReverseLookup
    projectMasterReverseLookup.remove (projectName);
    projectMaster.remove (projectId);
  }
  
  /**
   * Deletes all element ID references made to the given project
   * 
   * @param project
   */
  public synchronized void deleteReferences (String projectName)
  {
    
    Integer projectId = projectMasterReverseLookup.get (projectName);

    if (projectId == null) return;

    Collection<ProjectReference> projRefs = projectReferenceMaster.values ();

    // Pass 1: Prepare a list of element references to remove
    List<ProjectReference> toRemove = new ArrayList<ProjectReference> ();
    if(projRefs != null){
      for (ProjectReference projRef : projRefs) {
        if (projRef.getProjectId().intValue () == projectId.intValue ()) {
          toRemove.add (projRef);
        }
      }
    }

    // Pass 2: Remove the element references. Two pass approach is required, otherwise ConcurrentModificationException
    // will be thrown
    for (ProjectReference projRef : toRemove) {
    	removeProjectRef (projRef);
    }
  }
  
  /**
   * Deletes references to other projects, made within this project.
   * 
   * @param projectName name of the project
   */
  public synchronized void deleteReferencesInProject (String projectName)
  {
    Integer projectId = projectMasterReverseLookup.get (projectName);
    if (projectId == null) { return; }

    Collection<ProjectReference> projRefs = projectReferenceMaster.values ();

    List<ProjectReference> toRemove = new ArrayList<ProjectReference> ();
    if (projRefs != null) {
      for (ProjectReference projRef : projRefs) {
        int fileId = projRef.getLocation ().getFileId ();
        IFile file = FileCache.getInstance ().getFile (fileId);
        if (file != null) {
          Integer containingProjectId = projectMasterReverseLookup.get (file.getProject ().getName ());
          if (containingProjectId != null && projectId.equals (containingProjectId)) {
            toRemove.add (projRef);
          }
        }
      }
    }

    // Pass 2: Remove the element references. Two pass approach is required, otherwise ConcurrentModificationException
    // will be thrown
    for (ProjectReference projRef : toRemove) {
      removeProjectRef (projRef);
    }
  }

  private void removeProjectRef (ProjectReference projRef)
  {
    Integer projectRefId = projRef.getProjectRefId ();

    // remove from master table
    projectReferenceMaster.remove (projectRefId);

    // remove from child table: referenceMap
    Collection<List<Integer>> refMapValues = referenceMap.values ();
    if(refMapValues != null){
      for (List<Integer> list : refMapValues) {
        list.remove (projectRefId);
      }
    }

  }

  /**
   * Adds a project reference to the project cache
   * 
   * @param projRef the reference object to cache.
   */
  public synchronized void addProjectReference (ProjectReference projRef)
  {
    List<Integer> refList = referenceMap.get (projRef.getProjectId ());
    if(refList != null){
      for (Integer ref : refList) {
        ProjectReference reference = projectReferenceMaster.get (ref);
        if(reference.getLocation ().getFileId () == projRef.getLocation ().getFileId () &&
            reference.getLocation ().getOffset () == projRef.getLocation ().getOffset ()){
          // Project Reference is already indexed. So no need to index it again.
          return;
        }
        
      }
    }
    
    Integer projectRefId = projRef.getProjectRefId ();

    // add to projectReferenceMaster
    projectReferenceMaster.put (projectRefId, projRef);

    // add to reference map
    Integer referencedProjectID = projRef.getProjectId ();
    addToReferenceMap (referencedProjectID, projectRefId);
  }

  /**
   * Adds an entry to the referenceMap
   * 
   * @param referencedProjectID
   * @param projRefId
   */
  private void addToReferenceMap (Integer referencedProjectID, Integer projRefId)
  {
    List<Integer> references = referenceMap.get (referencedProjectID);

    // Create an entry, if no references exit yet
    if (references == null) {
      references = new ArrayList<Integer> ();
      referenceMap.put (referencedProjectID, references);
    }

    // finally, add a reference
    references.add (projRefId);
  }

  /**
   * Looks up projectReferenceMaster and returns the ProjectReference object
   * 
   * @param projRefId
   * @return
   */
  public synchronized ProjectReference getProjectReference (Integer projRefId)
  {
    return projectReferenceMaster.get (projRefId);
  }

  /*
   * Creates an XML for persisting the index.
   */
  private String toXML ()
  {
    StringBuffer buff = new StringBuffer ();
    buff.append ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

    buff.append ("<projectCache>");
    if (projectMaster != null && !projectMaster.isEmpty ()) {
      buff.append ("<projectMasterConfig>");
      for (Integer key : projectMaster.keySet ()) {
        buff.append ("<projectMaster id=\"" + key.toString () + "\">");
        buff.append (projectMaster.get (key));
        buff.append ("</projectMaster>");
      }
      buff.append ("</projectMasterConfig>");
    }
    Set<Integer> keySet = null;

    if (projectReferenceMaster != null && !projectReferenceMaster.isEmpty ()) {
      keySet = projectReferenceMaster.keySet ();
      ProjectReference projectReference = null;
      buff.append ("<projectReferenceMasterConfig>");
      for (Integer key : keySet) {
        projectReference = projectReferenceMaster.get (key);
        buff.append ("<projectReferenceMaster id=\"" + key.toString () + Constants.XML_CLOSE_TAG);

        buff.append ("<projectId>");
        buff.append (projectReference.getProjectId ());
        buff.append ("</projectId>");

        buff.append (Constants.XML_LOCATION);

        buff.append ("<offset>");
        buff.append (projectReference.getLocation ().getOffset ());
        buff.append ("</offset>");

        buff.append ("<fileId>");
        buff.append (projectReference.getLocation ().getFileId ());
        buff.append ("</fileId>");

        buff.append (Constants.XML_LOCATION_END);

        buff.append ("</projectReferenceMaster>");
      }
      buff.append ("</projectReferenceMasterConfig>");
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
    buff.append ("</projectCache>");
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

    File projectMasterConfigFile = new File (directory, Constants.PROJECT_CACHE_IDX);
    FileOutputStream output = null;
    try {
      output = new FileOutputStream (projectMasterConfigFile);
      String content = toXML ();
      output.write (content.getBytes ());
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

    File projectMasterConfigFile = new File (directory, Constants.PROJECT_CACHE_IDX);

    projectMaster.clear ();
    projectMasterReverseLookup.clear ();
    projectReferenceMaster.clear ();
    referenceMap.clear ();

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance ();
    Document doc = null;
    try {
      InputStream in = new FileInputStream (projectMasterConfigFile);
      byte[] bytes = new byte[(int) projectMasterConfigFile.length ()];
      in.read (bytes);
      String fileContent = new String (bytes);
      InputStream is = new ByteArrayInputStream (fileContent.getBytes ());

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

    NodeList listProjectMaster = null;
    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (0) != null) {
      listProjectMaster = doc.getChildNodes ().item (0).getChildNodes ().item (0).getChildNodes ();
      for (int i = 0; i < listProjectMaster.getLength (); i++) {
        Element modMasterProject = (Element) listProjectMaster.item (i);
        Integer projectId = Integer.parseInt (modMasterProject.getAttribute (Constants.ID));
        String project = modMasterProject.getTextContent ();
        projectMaster.put (projectId, project);
        projectMasterReverseLookup.put (project, projectId);
      }
    }

    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (1) != null) {
      listProjectMaster = doc.getChildNodes ().item (0).getChildNodes ().item (1).getChildNodes ();
      NodeList list = null;
      ProjectReference projectReference = null;
      for (int i = 0; i < listProjectMaster.getLength (); i++) {
        Element modMasterProject = (Element) listProjectMaster.item (i);
        Integer refId = Integer.parseInt (modMasterProject.getAttribute (Constants.ID));
        list = modMasterProject.getElementsByTagName ("projectId");
        int projectId = Integer.parseInt (list.item (0).getTextContent ());

        NodeList locList = modMasterProject.getElementsByTagName (Constants.ELEMENT_LOCATION);
        list = ((Element) locList.item (0)).getElementsByTagName ("offset");
        int offset = Integer.parseInt (list.item (0).getTextContent ());

        list = ((Element) locList.item (0)).getElementsByTagName ("fileId");
        int fileId = Integer.parseInt (list.item (0).getTextContent ());

        projectReference = new ProjectReference (refId, projectId, new ElementLocation (fileId, offset));
        projectReferenceMaster.put (refId, projectReference);

      }
    }

    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (2) != null) {
      listProjectMaster = doc.getChildNodes ().item (0).getChildNodes ().item (2).getChildNodes ();
      List<Integer> refList = null;
      for (int i = 0; i < listProjectMaster.getLength (); i++) {
        refList = new ArrayList<Integer> ();
        Element modMasterProject = (Element) listProjectMaster.item (i);
        Integer refId = Integer.parseInt (modMasterProject.getAttribute (Constants.ID));

        NodeList refIdlist = modMasterProject.getElementsByTagName (Constants.ELEMENT_REF_ID);
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
    projectMaster.clear ();
    projectMasterReverseLookup.clear ();
    projectReferenceMaster.clear ();
    referenceMap.clear ();
  }

  public synchronized List<Integer> getReferences (Integer projectId)
  {
    return referenceMap.get (projectId);
  }

  /*
   * public void test() { projectMaster = new HashMap<Integer, String>(); projectMaster.put(1, "proj1");
   * projectMaster.put(2, "proj2"); projectMaster.put(3, "proj3"); projectMasterReverseLookup = new HashMap<String,
   * Integer>(); projectMasterReverseLookup.put("proj1", 1); projectMasterReverseLookup.put("proj2", 2);
   * projectMasterReverseLookup.put("proj3", 3); projectReferenceMaster = new HashMap<Integer, ProjectReference>();
   * projectReferenceMaster.put(4, new ProjectReference(4, 1, new ElementLocation(1, 1))); projectReferenceMaster.put(5,
   * new ProjectReference(5, 2, new ElementLocation(1, 1))); projectReferenceMaster.put(6, new ProjectReference(6, 3,
   * new ElementLocation(1, 1))); referenceMap = new HashMap<Integer, List<Integer>>(); List<Integer> i = new
   * ArrayList<Integer>(); i.add(23); i.add(333); referenceMap.put(7, i); try { write(); } catch (Exception e) { // TODO
   * Auto-generated catch block e.printStackTrace(); } } public static void main(String a[]) throws Exception{
   * ProjectCache c = new ProjectCache(); c.test(); }
   */
}
