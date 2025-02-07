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
import com.ibm.biginsights.textanalytics.indexer.model.ModuleReference;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Cache for Module definition and its references.
 * 
 *  Krishnamurthy
 */
public class ModuleCache extends Cache
{



  private static ModuleCache cacheInstance = null;

  /**
   * Key : Module Id <br/>
   * Value: Project.Module
   */
  Map<Integer, String> moduleMaster = new HashMap<Integer, String> ();

  /**
   * Key : Project.Module <br/>
   * Value: Module Id
   */
  Map<String, Integer> moduleMasterReverseLookup = new HashMap<String, Integer> ();

  /**
   * Key: Module Id <br/>
   * Value: List of all references to this module
   */
  Map<Integer, Set<Integer>> referenceMap = new HashMap<Integer, Set<Integer>> ();

  /**
   * Key: Module Reference Id <br/>
   * Value : ModuleReference object
   */
  Map<Integer, ModuleReference> moduleReferenceMaster = new HashMap<Integer, ModuleReference> ();

  public static synchronized ModuleCache getInstance ()
  {
    if (cacheInstance == null) {
      cacheInstance = new ModuleCache ();
    }

    return cacheInstance;
  }

  /**
   * Updates the Project name
   * 
   * @param oldProject
   * @param newProject
   */
  public synchronized void updateProjectName (String oldProject, String newProject)
  {
    Set<String> keys = moduleMasterReverseLookup.keySet ();
    Map<String, Integer> tempModuleMasterReverseLookup = new HashMap<String, Integer> ();
    List<String> keysToBeRemoved = new ArrayList<String> ();
    for (String key : keys) {
      String components[] = key.split (String.valueOf (Constants.QUALIFIED_NAME_SEPARATOR));
      if (components[1].equals (oldProject)) {
        Integer moduleId = moduleMasterReverseLookup.get (key);
        String newKey = idManager.createQualifiedKey (ElementType.MODULE.toString (), newProject, components[2]);

        moduleMaster.put (moduleId, newKey);
        keysToBeRemoved.add (key);
        tempModuleMasterReverseLookup.put (newKey, moduleId);
        // moduleMasterReverseLookup.remove(key);
        // moduleMasterReverseLookup.put(newKey, moduleId);
      }
    }
    if (!keysToBeRemoved.isEmpty ()) {
      for (String key : keysToBeRemoved) {
        moduleMasterReverseLookup.remove (key);
      }
    }
    if (!tempModuleMasterReverseLookup.isEmpty ()) {
      for (String newKey : tempModuleMasterReverseLookup.keySet ()) {
        moduleMasterReverseLookup.put (newKey, tempModuleMasterReverseLookup.get (newKey));
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
    String newKey = idManager.createQualifiedKey (ElementType.MODULE.toString (), projectName, newModule);
    /**
     * In some cases the eclipse listener do not capture events in a proper order, in such cases
     * the module caches will already have indexed the changed file. 
     * 
     *  Sample scenario is Module Rename, the order should be 
     *  1) Call Module Rename event
     *  2) Update the AQL files
     *  
     *  In some cases the eclipse captures (2) first then (1). Then processing of (2) already populated the
     *  Module Cache. So we remove the old values from the cache. 
     */
    if(moduleMasterReverseLookup.containsKey (newKey)){
      
      String oldKey = idManager.createQualifiedKey (ElementType.MODULE.toString (), projectName, oldModule);
      Integer modId = moduleMasterReverseLookup.get (oldKey);
      moduleMaster.remove (modId);
      moduleMasterReverseLookup.remove (oldKey);
      Integer oldModId = getModuleId (projectName, oldModule);
      Set<Integer> refIds = referenceMap.get (oldModId);
      referenceMap.remove (oldModId);
      if(null != refIds){
        for (Integer refId : refIds) {
          moduleReferenceMaster.remove (refId);
        }
      }
      return;
    }
    
    Set<String> keys = moduleMasterReverseLookup.keySet ();
    Map<String, Integer> tempModuleMasterReverseLookup = new HashMap<String, Integer> ();
    List<String> keysToBeRemoved = new ArrayList<String> ();

    for (String key : keys) {
      String components[] = key.split (String.valueOf (Constants.QUALIFIED_NAME_SEPARATOR));
      if (components[1].equals (projectName) && components[2].equals (oldModule)) {
        Integer moduleId = moduleMasterReverseLookup.get (key);

        moduleMaster.put (moduleId, newKey);
        keysToBeRemoved.add (key);
        tempModuleMasterReverseLookup.put (newKey, moduleId);

        // moduleMasterReverseLookup.remove(key);
        // moduleMasterReverseLookup.put(newKey, moduleId);
      }
    }
    if (!keysToBeRemoved.isEmpty ()) {
      for (String key : keysToBeRemoved) {
        moduleMasterReverseLookup.remove (key);
      }
    }
    if (!tempModuleMasterReverseLookup.isEmpty ()) {
      for (String key : tempModuleMasterReverseLookup.keySet ()) {
        moduleMasterReverseLookup.put (key, tempModuleMasterReverseLookup.get (key));
      }
    }

  }

  /**
   * Returns the Module ID
   * 
   * @param project
   * @param module
   * @return
   */
  public synchronized Integer getModuleId (String project, String module)
  {
    IDManager idManager = IDManager.getInstance ();
    String key = idManager.createQualifiedKey (ElementType.MODULE.toString (), project, module);
    Integer moduleId = moduleMasterReverseLookup.get (key);
    if (moduleId == null) {
      moduleId = idManager.generateNextSequenceId ();
      moduleMaster.put (moduleId, key);
      moduleMasterReverseLookup.put (key, moduleId);
    }
    return moduleId;
  }

  /**
   * Removes the Module details from the Module Cache
   * 
   * @param project
   * @param module
   */
  public synchronized void removeModule (String project, String module)
  {
    IDManager idManager = IDManager.getInstance ();
    String key = idManager.createQualifiedKey (ElementType.MODULE.toString (), project, module);
    Integer moduleId = moduleMasterReverseLookup.get (key);
    if(moduleId != null){
    	moduleMasterReverseLookup.remove(key);
    	moduleMaster.remove(moduleId);
    }
  }

  /**
   * Removes all the Module details for a project from the Module Cache
   * 
   * @param project
   * @param module
   */
  public synchronized void removeAllModules (String project)
  {
    IDManager idManager = IDManager.getInstance ();
    String searchKey = idManager.createQualifiedKey (ElementType.MODULE.toString (), project);
    
    Set<String> keys = moduleMasterReverseLookup.keySet();
    Set<String> keysToBeRemoved = new HashSet<String> ();
    
    for (String key : keys) {
      if(key.indexOf(searchKey)!= -1){
        Integer moduleId = moduleMasterReverseLookup.get (key);
        if(moduleId != null){
          keysToBeRemoved.add(key);
          moduleMaster.remove(moduleId);
        }
      }
    }
    
    for (String string : keysToBeRemoved) {
      moduleMasterReverseLookup.remove (string);
    }
  }

  /**
   * Adds a module reference to the module cache
   * 
   * @param moduleRef
   */
  public synchronized void addModuleReference (ModuleReference moduleRef)
  {
    Integer moduleRefId = moduleRef.getModuleRefId ();

    // add to moduleReferenceMaster
    moduleReferenceMaster.put (moduleRefId, moduleRef);

    // add to reference hierarchy
    Integer referencedModuleID = moduleRef.getModuleId ();
    addToReferenceMap (referencedModuleID, moduleRefId);
  }

  /**
   * Adds an entry to the referenceMap map
   * 
   * @param referencedModuleID
   * @param moduleRefId
   */
  private void addToReferenceMap (Integer referencedModuleID, Integer moduleRefId)
  {
    Set<Integer> references = referenceMap.get (referencedModuleID);

    // Create an entry, if no references exit yet
    if (references == null) {
      references = new HashSet<Integer> ();
      referenceMap.put (referencedModuleID, references);
    }

    // finally, add a reference
    references.add (moduleRefId);
  }

  /**
   * Determines if the token at the given offset is a module reference
   * 
   * @param file IFile instance where the token appears
   * @param offset Begin offset of the token
   * @return <code>true</code>, if the given token is a module reference. Returns <code>false</code> otherwise
   */
  public synchronized boolean isModuleReference (IFile file, int offset)
  {

    boolean isModuleRef = false;
    Integer fileId = FileCache.getInstance ().getFileId (file);
    Collection<ModuleReference> moduleRefs = moduleReferenceMaster.values ();
    for (ModuleReference ref : moduleRefs) {
      if (fileId != null && ref.getLocation() != null &&
    		  ref.getLocation ().getFileId () == fileId.intValue () && ref.getLocation ().getOffset () == offset) {
        isModuleRef = true;
        break;
      }
    }

    return isModuleRef;
  }

  private String toXML ()
  {
    StringBuffer buff = new StringBuffer ();
    buff.append ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    buff.append ("<moduleCache>");
    if (moduleMaster != null && !moduleMaster.isEmpty ()) {
      buff.append ("<moduleMasterConfig>");
      for (Integer key : moduleMaster.keySet ()) {
        buff.append ("<moduleMaster id=\"" + key.toString () + "\">");
        buff.append (moduleMaster.get (key));
        buff.append ("</moduleMaster>");
      }
      buff.append ("</moduleMasterConfig>");
    }

    Set<Integer> keySet = null;
    if (referenceMap != null && !referenceMap.isEmpty ()) {
      keySet = referenceMap.keySet ();
      buff.append ("<referenceMapConfig>");
      for (Integer key : keySet) {
        buff.append (Constants.XML_ELEMENT_ID_ATTR + key.toString () + Constants.XML_CLOSE_TAG);
        Set<Integer> refList = referenceMap.get (key);
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

    if (moduleReferenceMaster != null && !moduleReferenceMaster.isEmpty ()) {
      keySet = moduleReferenceMaster.keySet ();
      ModuleReference moduleReference = null;
      buff.append ("<moduleReferenceMasterConfig>");
      for (Integer key : keySet) {
        moduleReference = moduleReferenceMaster.get (key);
        buff.append ("<moduleReferenceMaster id=\"" + key.toString () + Constants.XML_CLOSE_TAG);

        buff.append ("<moduleId>");
        buff.append (moduleReference.getModuleId ());
        buff.append ("</moduleId>");

        buff.append (Constants.XML_LOCATION);

        buff.append ("<offset>");
        buff.append (moduleReference.getLocation ().getOffset ());
        buff.append ("</offset>");

        buff.append ("<fileId>");
        buff.append (moduleReference.getLocation ().getFileId ());
        buff.append ("</fileId>");

        buff.append (Constants.XML_LOCATION_END);

        buff.append ("</moduleReferenceMaster>");
      }
      buff.append ("</moduleReferenceMasterConfig>");
    }
    buff.append ("</moduleCache>");
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

    File projectMasterConfigFile = new File (directory, Constants.MODULE_CACHE_IDX);
    FileOutputStream output = null;
    try {
      output = new FileOutputStream (projectMasterConfigFile);
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

    File moduleMasterConfigFile = new File (directory, Constants.MODULE_CACHE_IDX);

    moduleMaster.clear ();
    moduleMasterReverseLookup.clear ();
    moduleReferenceMaster.clear ();
    referenceMap.clear ();

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance ();
    Document doc = null;
    try {
      InputStream in = new FileInputStream (moduleMasterConfigFile);
      byte[] bytes = new byte[(int) moduleMasterConfigFile.length ()];
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
    NodeList listProjectMaster = null;
    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (0) != null) {
      listProjectMaster = doc.getChildNodes ().item (0).getChildNodes ().item (0).getChildNodes ();
      for (int i = 0; i < listProjectMaster.getLength (); i++) {
        Element modMasterProject = (Element) listProjectMaster.item (i);
        Integer moduleId = Integer.parseInt (modMasterProject.getAttribute (Constants.ID));
        String module = modMasterProject.getTextContent ();
        moduleMaster.put (moduleId, module);
        moduleMasterReverseLookup.put (module, moduleId);
      }
    }

    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (1) != null) {
      listProjectMaster = doc.getChildNodes ().item (0).getChildNodes ().item (1).getChildNodes ();
      Set<Integer> refList = null;
      for (int i = 0; i < listProjectMaster.getLength (); i++) {
        refList = new HashSet<Integer> ();
        Element modMasterProject = (Element) listProjectMaster.item (i);
        Integer refId = Integer.parseInt (modMasterProject.getAttribute (Constants.ID));

        NodeList refIdlist = modMasterProject.getElementsByTagName (Constants.ELEMENT_REF_ID);
        for (int j = 0; j < refIdlist.getLength (); j++) {
          refList.add (Integer.parseInt (refIdlist.item (j).getTextContent ()));
        }
        referenceMap.put (refId, refList);
      }
    }

    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (2) != null) {
      listProjectMaster = doc.getChildNodes ().item (0).getChildNodes ().item (2).getChildNodes ();
      NodeList list = null;
      ModuleReference moduleReference = null;
      for (int i = 0; i < listProjectMaster.getLength (); i++) {
        Element modMasterProject = (Element) listProjectMaster.item (i);
        Integer refId = Integer.parseInt (modMasterProject.getAttribute (Constants.ID));
        list = modMasterProject.getElementsByTagName ("moduleId");
        int moduleId = Integer.parseInt (list.item (0).getTextContent ());

        NodeList locList = modMasterProject.getElementsByTagName (Constants.ELEMENT_LOCATION);
        list = ((Element) locList.item (0)).getElementsByTagName ("offset");
        int offset = Integer.parseInt (list.item (0).getTextContent ());

        list = ((Element) locList.item (0)).getElementsByTagName ("fileId");
        int fileId = Integer.parseInt (list.item (0).getTextContent ());

        moduleReference = new ModuleReference (refId, moduleId, new ElementLocation (fileId, offset));
        moduleReferenceMaster.put (refId, moduleReference);

      }
    }
  }

  @Override
  public synchronized void clear ()
  {
    moduleMaster.clear ();
    moduleMasterReverseLookup.clear ();
    moduleReferenceMaster.clear ();
    referenceMap.clear ();
  }

  public synchronized Set<Integer> getReferences (Integer moduleId)
  {
    return referenceMap.get (moduleId);
  }

  public synchronized ModuleReference getModuleReference (Integer refId)
  {
    return moduleReferenceMaster.get (refId);
  }

  /**
   * Deletes all module ID references in the given file
   * 
   * @param file
   */
  public synchronized void deleteReferencesInFile (IFile file)
  {
    FileCache fileCache = FileCache.getInstance ();
    Integer fileId = fileCache.getFileId (file);

    if (fileId == null) return;

    deleteReferencesInFile (fileId);
  }

  /**
   * Deletes all module ID references in the given file
   * 
   * @param fileId
   */
  public synchronized void deleteReferencesInFile (Integer fileId)
  {
    Collection<ModuleReference> moduleRefs = moduleReferenceMaster.values ();
    
    // Pass 1: Prepare a list of module references to remove
    List<ModuleReference> toRemove = new ArrayList<ModuleReference> ();
    if(moduleRefs != null){
      for (ModuleReference moduleRef : moduleRefs) {
        if (moduleRef.getLocation ().getFileId () == fileId) {
          toRemove.add (moduleRef);
        }
      }
    }
    
    // Pass 2: Remove the module references. Two pass approach is required, otherwise ConcurrentModificationException
    // will be thrown
    for (ModuleReference moduleRef : toRemove) {
      removeModuleRef (moduleRef);
    }
    
  }

  private void removeModuleRef (ModuleReference moduleRef)
  {
    Integer moduleRefId = moduleRef.getModuleRefId ();

    // remove from master table
    moduleReferenceMaster.remove (moduleRefId);

    // remove from child table: referenceMap
    Collection<Set<Integer>> refMapValues = referenceMap.values ();
    for (Set<Integer> list : refMapValues) {
      list.remove (moduleRefId);
    }

  }

  /*
   * public void test() { moduleMaster = new HashMap<Integer, String>(); moduleMaster.put(1, "proj1");
   * moduleMaster.put(2, "proj2"); moduleMaster.put(3, "proj3"); moduleMasterReverseLookup = new HashMap<String,
   * Integer>(); moduleMasterReverseLookup.put("proj1", 1); moduleMasterReverseLookup.put("proj2", 2);
   * moduleMasterReverseLookup.put("proj3", 3); moduleReferenceMaster = new HashMap<Integer, ModuleReference>();
   * moduleReferenceMaster.put(4, new ModuleReference(4, 1, new ElementLocation(1, 1))); moduleReferenceMaster.put(5,
   * new ModuleReference(5, 2, new ElementLocation(1, 1))); moduleReferenceMaster.put(6, new ModuleReference(6, 3, new
   * ElementLocation(1, 1))); referenceMap = new HashMap<Integer, List<Integer>>(); List<Integer> i = new
   * ArrayList<Integer>(); i.add(23); i.add(333); referenceMap.put(7, i); try { write(); } catch (Exception e) { // TODO
   * Auto-generated catch block e.printStackTrace(); } }
   */

}
