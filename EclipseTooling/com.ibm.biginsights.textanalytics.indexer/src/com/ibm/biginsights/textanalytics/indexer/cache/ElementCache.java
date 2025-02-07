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
import com.ibm.biginsights.textanalytics.indexer.model.ElementDefinition;
import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.model.ElementReference;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Maintains indexes related to all AQL elements in workspace
 * 
 *  Krishnamurthy
 */
public class ElementCache extends Cache
{
	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  private static ElementCache cacheInstance = null;

  // //////////// Attributes
  /**
   * Master table for list of all element names.<br/>
   * Key: element Id <br/>
   * Value: Project.Module.Type.ElementName <br/>
   */
  protected Map<Integer, String> elementMaster = new HashMap<Integer, String> ();
  
  /**
   * Map from elementId to its metadata.
   */
  protected Map<Integer, ElementMetadata> elementMetadataMap = new HashMap<Integer, ElementMetadata> ();

  /**
   * Reverse mapping of the elementMaster <br/>
   * Key: Project.Module.Type.ElementName <br/>
   * Value: element Id <br/>
   */
  protected Map<String, Integer> elementMasterReverseLookup = new HashMap<String, Integer> ();

  /**
   * Master table for all element definitions. This map can be used to compute Dependency Hierarchy <br/>
   * Key: element Id <br/>
   * Value: ElementDefintion object <br/>
   */
  protected Map<Integer, ElementDefinition> elementDefinitionMaster = new HashMap<Integer, ElementDefinition> ();

  /**
   * Collection of all element references found in workspace. <br/>
   * Key: Element Reference Id <br/>
   * Value: ElementRefernce Object <br/>
   */
  protected Map<Integer, ElementReference> elementReferenceMaster = new HashMap<Integer, ElementReference> ();

  /**
   * Collection of all references to a given element. This map is used for computing ReferenceHierarchy. <br/>
   * Key: elementId <br/>
   * Value: A list of locations where the given element is referenced. i.e list of elementRefIds <br/>
   */
  protected Map<Integer, Set<Integer>> referenceMap = new HashMap<Integer, Set<Integer>> ();

  // ///////////// Factory method
  public static synchronized ElementCache getInstance ()
  {
    if (cacheInstance == null) {
      cacheInstance = new ElementCache ();
    }

    return cacheInstance;
  }

  // //////////// Operations

  /**
   * Updates the Project Name
   * 
   * @param oldProject
   * @param newProject
   */
  public synchronized void updateProjectName (String oldProject, String newProject)
  {
    // Key: Project.Module.Type.ElementName
    Set<String> keys = elementMasterReverseLookup.keySet ();
    Map<String, Integer> tempElementMasterReverseLookup = new HashMap<String, Integer> ();
    List<String> keysToBeRemoved = new ArrayList<String> ();
    String component = String.format (Constants.QUALIFIED_NAME_SEPARATOR + "%s" + Constants.QUALIFIED_NAME_SEPARATOR,
      oldProject);
    for (String key : keys) {
      if (key.indexOf (component) != -1) {
        Integer id = elementMasterReverseLookup.get (key);
        // AQL files: File type, ProjectName, module name, ElementName
        String newKey = key.replace (component,
          String.format (Constants.QUALIFIED_NAME_SEPARATOR + "%s" + Constants.QUALIFIED_NAME_SEPARATOR, newProject));
        elementMaster.put (id, newKey);
        keysToBeRemoved.add (key);
        tempElementMasterReverseLookup.put (newKey, id);
        // elementMasterReverseLookup.remove(key);
        // elementMasterReverseLookup.put(newKey, id);
      }
    }
    if (!keysToBeRemoved.isEmpty ()) {
      for (String key : keysToBeRemoved) {
        elementMasterReverseLookup.remove (key);
      }
    }
    if (!tempElementMasterReverseLookup.isEmpty ()) {
      for (String newKey : tempElementMasterReverseLookup.keySet ()) {
        elementMasterReverseLookup.put (newKey, tempElementMasterReverseLookup.get (newKey));
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
    Set<String> keys = elementMasterReverseLookup.keySet ();

    List<String> newModulekeys = new ArrayList<String> ();

    /**
     * In some cases the eclipse listener do not capture events in a proper order, in such cases the caches will already
     * have indexed the changed file. Sample scenario is Module Rename, the order should be 1) Call Module Rename event
     * 2) Update the AQL files In some cases the eclipse captures (2) first then (1). Then processing of (2) already
     * populated the Cache. So we remove the old values from the cache.
     */
    String newKeyComponent = String.format (Constants.QUALIFIED_NAME_SEPARATOR
      + "%s" + Constants.QUALIFIED_NAME_SEPARATOR //$NON-NLS-1$
      + "%s" + Constants.QUALIFIED_NAME_SEPARATOR, projectName, newModule); //$NON-NLS-1$

    String oldKeyComponent = String.format (Constants.QUALIFIED_NAME_SEPARATOR
      + "%s" + Constants.QUALIFIED_NAME_SEPARATOR //$NON-NLS-1$
      + "%s" + Constants.QUALIFIED_NAME_SEPARATOR, projectName, oldModule); //$NON-NLS-1$

    if (keys != null) {
      for (String key : keys) {
        if (key.indexOf (newKeyComponent) != -1) {
          newModulekeys.add (key);
        }
      }
    }
    for (String oldModuleKey : newModulekeys) {
      oldModuleKey = oldModuleKey.replace (newKeyComponent, oldKeyComponent);
      Integer fileId = elementMasterReverseLookup.get (oldModuleKey);
      elementMaster.remove (fileId);
      elementMasterReverseLookup.remove (oldModuleKey);
      Set<Integer> refIds = referenceMap.get (fileId);
      referenceMap.remove (fileId);
      if (null != refIds) {
        for (Integer refId : refIds) {
          elementReferenceMaster.remove (refId);
        }
      }
    }

    if (!newModulekeys.isEmpty ()) { return; }

    Map<String, Integer> tempElementMasterReverseLookup = new HashMap<String, Integer> ();
    List<String> keysToBeRemoved = new ArrayList<String> ();
    if (keys != null) {
      for (String key : keys) {
        if (key.indexOf (oldKeyComponent) != -1) {
          Integer id = elementMasterReverseLookup.get (key);
          String newKey = key.replace (oldKeyComponent,
            String.format (Constants.QUALIFIED_NAME_SEPARATOR + "%s" + Constants.QUALIFIED_NAME_SEPARATOR + "%s" //$NON-NLS-1$ $NON-NLS-2$
              + Constants.QUALIFIED_NAME_SEPARATOR, projectName, newModule));
          elementMaster.put (id, newKey);
          keysToBeRemoved.add (key);
          tempElementMasterReverseLookup.put (newKey, id);
          // elementMasterReverseLookup.remove(key);
          // elementMasterReverseLookup.put(newKey, id);
        }
      }
    }

    if (!keysToBeRemoved.isEmpty ()) {
      for (String key : keysToBeRemoved) {
        elementMasterReverseLookup.remove (key);
      }
    }
    if (!tempElementMasterReverseLookup.isEmpty ()) {
      for (String newKey : tempElementMasterReverseLookup.keySet ()) {
        elementMasterReverseLookup.put (newKey, tempElementMasterReverseLookup.get (newKey));
      }
    }
  }

  /**
   * Adds an element definition details to elementMaster,elementMasterReverseLookup and elementDefinitionMaster.
   * Overwrites element metadata to default values.
   * 
   * @param qualifiedName
   */
  public synchronized void addElementDefintion (ElementDefinition elementDef, String qualifiedName)
  {
    Integer elemId = elementDef.getElementId ();

    // add to elementDefinitionMaster
    elementDefinitionMaster.put (elemId, elementDef);

    addElementIdInternal (elemId, qualifiedName);
    
    elementMetadataMap.put (elemId, new ElementMetadata (true)); //pre-existing values will be overwritten

    // add Element Reference for the Create XXX statement - i.e self reference
    Integer elemRefId = IDManager.getInstance ().generateNextSequenceId ();
    ElementReference elemRef = new ElementReference (elemRefId, elementDef.getElementId (), elementDef.getLocation ());
    addElementReference (elemRef, elementDef.getElementId ());
  }

  /**
   * Deletes all element ID references in the given file
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
   * Deletes all element ID references in the given file
   * 
   * @param fileId
   */
  public synchronized void deleteReferencesInFile (Integer fileId)
  {
    Collection<ElementReference> elemRefs = elementReferenceMaster.values ();

    // Pass 1: Prepare a list of element references to remove
    List<ElementReference> toRemove = new ArrayList<ElementReference> ();
    if (elemRefs != null) {
      for (ElementReference elemRef : elemRefs) {
        if (elemRef.getLocation ().getFileId () == fileId.intValue ()) {
          toRemove.add (elemRef);
        }
      }
    }

    // Pass 2: Remove the element references. Two pass approach is required, otherwise ConcurrentModificationException
    // will be thrown
    for (ElementReference elemRef : toRemove) {
      removeElemRef (elemRef);
    }
  }

  /**
   * Deletes all element definitions in given file
   * 
   * @param file
   */
  public synchronized void deleteDefinitionsInFile (IFile file)
  {
    FileCache fileCache = FileCache.getInstance ();
    Integer fileId = fileCache.getFileId (file);

    if (fileId == null) return;

    deleteDefinitionsInFile (fileId);
  }
  
  /**
   * Marks all element definitions in the given file as inactive, via their metadata objects. Note - They are not
   * deleted from elementMaster.
   * 
   * @param file
   */
  public synchronized void deactivateDefinitionsInFile (IFile file )
  {
    FileCache fileCache = FileCache.getInstance ();
    Integer fileId = fileCache.getFileId (file);
    if (fileId == null) {
      return;
    }
    deactivateDefinitionsInFile (fileId);
  }

  /**
   * Deletes all element definitions in given file
   * 
   * @param file
   */
  public synchronized void deleteDefinitionsInFile (Integer fileId)
  {
    Collection<ElementDefinition> elemDefs = elementDefinitionMaster.values ();
    List<Integer> toRemove = new ArrayList<Integer> ();
    if (elemDefs != null) {
      for (ElementDefinition elemDef : elemDefs) {
        if (fileId.intValue () == elemDef.getLocation ().getFileId ()) {
          Integer elemDefId = elemDef.getElementId ();
          toRemove.add (elemDefId);

          // remove from elementMaster and elementMasterReverseLookup
          String key = elementMaster.get (elemDefId);
          elementMaster.remove (elemDefId);
          elementMasterReverseLookup.remove (key);
        }
      }
    }

    // remove from elementDefinitionMaster
    for (Integer elemDefId : toRemove) {
      elementDefinitionMaster.remove (elemDefId);
    }
  }
  
  /**
   * Marks all element definitions in the given file as inactive, via their metadata objects. Note - They are not
   * deleted from elementMaster.
   * 
   * @param fileId Id of a file, as stored in File cache.
   */
  public synchronized void deactivateDefinitionsInFile (Integer fileId)
  {
    Collection<ElementDefinition> elemDefs = elementDefinitionMaster.values ();
    if (elemDefs != null) {
      for (ElementDefinition elemDef : elemDefs) {
        if (fileId.intValue () == elemDef.getLocation ().getFileId ()) {
          Integer elemDefId = elemDef.getElementId ();
          ElementMetadata emd = elementMetadataMap.get (elemDefId);
          if (emd != null) {
            emd.setElementActiveState (false);
          }
          else {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logDebug (
              "ElementCache.deactivateDefintionsInFile(): Metadata object not found for element with id " + elemDefId); //$NON-NLS-1$
          }
        }
      }
    }
  }

  private void removeElemRef (ElementReference elementRef)
  {
    Integer elemRefId = elementRef.getElementRefId ();

    // remove from master table
    elementReferenceMaster.remove (elemRefId);

    // remove from child table: elementDefinitionMaster
    for (ElementDefinition elemDef : elementDefinitionMaster.values ()) {
      Set<Integer> refIdList = elemDef.getReferenceList ();
      refIdList.remove (elemRefId);
    }

    // remove from child table: referenceMap
    Collection<Set<Integer>> refMapValues = referenceMap.values ();
    for (Set<Integer> list : refMapValues) {
      list.remove (elemRefId);
    }
  }

  private void addElementIdInternal (Integer elemId, String qualifiedName)
  {

    // add to elementMaster
    elementMaster.put (elemId, qualifiedName);
    
    // add to elementReverseLookupMaster
    elementMasterReverseLookup.put (qualifiedName, elemId);

  }

  /**
   * Given an element id, if an entry is found for it in element master map, the entry is updated with the new name
   * provided as argument. Entry corresponding to the old name in element master reverse lookup map is removed and
   * replaced by an entry with the new name.
   * 
   * @param elemId element id
   * @param qualifiedName new name to be associated with the element id. Should be in the format -
   *          projectname.modulename.elementtype.elementname
   */
  private void updateElementIdInternal (Integer elemId, String qualifiedName)
  {
    String oldName = elementMaster.get (elemId);
    // add to elementMaster
    if (oldName != null) {
      elementMaster.put (elemId, qualifiedName);

      elementMasterReverseLookup.remove (oldName);
      elementMasterReverseLookup.put (qualifiedName, elemId);
    }
  }

  /**
   * Adds an element reference to the cache. Updates three maps: <br/>
   * 1) elementReferenceMaster <br/>
   * 2) ElementDefinition in elementDefinitionMaster with the new reference <br/>
   * 3) referenceMap updating the referencedElement's list of references
   * 
   * @param elemReference
   * @param referencedByElemID elementID of the element definition that contained the elemReference
   */
  public synchronized void addElementReference (ElementReference elemReference, Integer referencedByElemID)
  {
    Integer elemRefId = elemReference.getElementRefId ();

    // add to elementReferenceMaster
    elementReferenceMaster.put (elemRefId, elemReference);

    // check if the element is referenced within another element definition. referencedByElemID could be null if an
    // element is referenced in an import statement or output statement.
    if (referencedByElemID != null) {
      // add a reference to elementDefinition
      ElementDefinition elemDef = getElementDefinition (referencedByElemID);
      if (elemDef != null) elemDef.addReference (elemRefId);
    }

    // add the elemRefID to referenceMap
    Integer referencedElemID = elemReference.getElementId ();
    addToReferenceHierarchyMap (referencedElemID, elemRefId);
  }

  private void addToReferenceHierarchyMap (Integer elemID, Integer elemRefId)
  {
    Set<Integer> references = referenceMap.get (elemID);

    // Create an entry, if no references exit yet
    if (references == null) {
      references = new HashSet<Integer> ();
      referenceMap.put (elemID, references);
    }

    // finally, add the reference
    references.add (elemRefId);
  }

  /**
   * Looks up the elementMasterReverseLookup for an entry with PMTE. Returns the id, if found. Else, creates a new entry
   * and returns the id. 
   * 
   * @param project
   * @param module
   * @param type
   * @param elementName
   * @return
   */
  public synchronized Integer getElementId (String project, String module, ElementType type, String elementName)
  {
    if (type == ElementType.VIEW || type == ElementType.TABLE || type == ElementType.VIEW_OR_TABLE) {
      return getElementIdForViewOrTable (project, module, type, elementName);
    }
    else {
      IDManager idManager = IDManager.getInstance ();
      String qualifiedName = idManager.createQualifiedKey (type.toString (), project, module, elementName);

      Integer elementId = elementMasterReverseLookup.get (qualifiedName);

      // We want the getElementId() method to create element Ids if not already cached, because the order of indexing of
      // AQL files is not as per compilation order and hence a referencing file may be indexed first instead of the
      // defining file.
      if (elementId == null) {
        elementId = idManager.generateNextSequenceId ();
        addElementIdInternal (elementId, qualifiedName);
      }
      return elementId;
    }

  }
  
  /**
   * Looks up the elementMasterReverseLookup for an entry with PMTE. Returns the id, if found. Else, creates a new entry
   * and returns the id. Contains additional logic for handling ambiguity in VIEWS and TABLES.
   * 
   * @param project
   * @param module
   * @param type
   * @param elementName
   * @return
   */
  private synchronized Integer getElementIdForViewOrTable (String project, String module, ElementType type, String elementName) {
    IDManager idManager = IDManager.getInstance ();
    String qualifiedName = idManager.createQualifiedKey (type.toString (), project, module, elementName);

    // If type parameter is VIEW_OR_TABLE, look for element definitions with type VIEW or TABLE first. If anything is
    // found, use that id.
    if (type == ElementType.VIEW_OR_TABLE) {
      String name = idManager.createQualifiedKey (ElementType.VIEW.toString (), project, module, elementName);
      Integer id = elementMasterReverseLookup.get (name);
      if (id == null) {
        name = idManager.createQualifiedKey (ElementType.TABLE.toString (), project, module, elementName);
        id = elementMasterReverseLookup.get (name);
      }
      if (id != null) { return id; }
    }

    Integer elementId = elementMasterReverseLookup.get (qualifiedName);

    // We want the getElementId() method to create element Ids if not already cached, because the order of indexing of
    // AQL files is not as per compilation order and hence a referencing file may be indexed first instead of the
    // defining file.
    if (elementId == null) {
      if (type == ElementType.VIEW || type == ElementType.TABLE) {
        // When looking for element ids with type VIEW OR TABLE, seek for ids of type VIEW_OR_TABLE too.
        // If such an id is found in elementMaster maps, return that id and update the maps so that the id
        // corresponds to the type sought (VIEW/TABLE) here.
        // This is because while indexing from-lists in select statements, if the definition/declaration statements
        // for the elements in the from-list have not already been indexed, we would have no way to know whether the
        // elements were of type VIEW or TABLE and hence created element references pointing to an element ids
        // associated
        // with type VIEW_OR_TABLE. i.e. The qualified name associated with that id would have type VIEW_OR_TABLE in it.
        String altLookupName = idManager.createQualifiedKey (ElementType.VIEW_OR_TABLE.toString (), project, module,
          elementName);
        Integer id = elementMasterReverseLookup.get (altLookupName);
        if (id != null) {
          updateElementIdInternal (id, qualifiedName);
          return id;
        } // if no entry found with alt name too, then go ahead and create a new element id.
      }
      elementId = idManager.generateNextSequenceId ();
      addElementIdInternal (elementId, qualifiedName);
    }
    return elementId;
  }

  /**
   * Looks up elementMaster and returns the Type*Project*Module*ElementName name of the element. Returns null if id is
   * not found in the cache, or if the element is marked as inactive.
   * 
   * @param elementID
   * @return
   */
  public synchronized String getElementName (Integer elementID)
  {
    String elementName = elementMaster.get (elementID);
    if (elementName != null &&
        elementMetadataMap.get (elementID) != null &&
        elementMetadataMap.get (elementID).getElementActiveState () == true) {
      return elementName;
    } else {
      return null;
    }
  }

  /**
   * Returns the AQL element name of the fashion moduleName.ElementName
   * 
   * @param elementID
   * @return
   */
  public synchronized String getElementNameInAQL (Integer elementID)
  {
    String elemNameKey = getElementName (elementID);
    if (elemNameKey != null) {
      int idxBeforeElemName = elemNameKey.lastIndexOf (Constants.QUALIFIED_NAME_SEPARATOR);
      String prefix = elemNameKey.substring (0, idxBeforeElemName);
      String elemName = elemNameKey.substring (idxBeforeElemName + 1);

      int idxBeforeModuleName = prefix.lastIndexOf (Constants.QUALIFIED_NAME_SEPARATOR);
      String moduleName = prefix.substring (idxBeforeModuleName + 1);

      return String.format ("%s.%s", moduleName, elemName);
    }

    return null;
  }

  /**
   * Looks up elementDefinitionMaster and returns an ElementDefintion object.
   * 
   * @param elementID
   * @return
   */
  public synchronized ElementDefinition getElementDefinition (Integer elementID)
  {
    return elementDefinitionMaster.get (elementID);
  }

  /**
   * Returns the list of element definitions in given file
   * 
   * @param file File whose element definitions are to be returned
   * @return List of element definitions in given file
   * @throws Exception if the given file is not already indexed
   */
  public synchronized List<ElementDefinition> getElementDefinitionsInFile (IFile file) throws Exception
  {
    List<ElementDefinition> ret = new ArrayList<ElementDefinition> ();

    // Step 1: Get file id
    Integer fileId = FileCache.getInstance ().getFileId (file);

    // Step 2: If file id is not found, throw an error
    if (fileId == null) { throw new Exception (String.format ("The specified file %s is not indexed yet.",
      file.getLocation ().toOSString ())); }

    // Step 3: Pick all elementDefs in given file
    Collection<ElementDefinition> elemDefs = elementDefinitionMaster.values ();
    for (ElementDefinition elemDef : elemDefs) {
      if (elemDef.getLocation ().getFileId () == fileId.intValue ()) {
        ret.add (elemDef);
      }
    }

    return ret;
  }

  /**
   * Looks up elementReferenceMaster and returns an ElementReference, if one exists.
   * 
   * @param elemRefID
   * @return
   */
  public synchronized ElementReference getElementReference (Integer elemRefID)
  {
    return elementReferenceMaster.get (elemRefID);
  }

  /**
   * Return all dependents of a given elementID
   * 
   * @param elementID elementID whose dependents are requested for
   * @return
   */
  public synchronized List<ElementReference> getDependents (Integer elementID)
  {

    try {
      Set<Integer> references = getElementDefinition (elementID).getReferenceList ();
      return convertIDToElementReference (references);
    }
    catch (Exception e) {
      // do nothing
    }

    return null;
  }

  /**
   * Get a list of ElementReference objects for a given element definition ID.
   * 
   * @param elementID Element definition id
   * @return list of ElementReference objects for a given element definition ID.
   */
  public synchronized List<ElementReference> getReferencesForElemDef (Integer elementID)
  {
    Set<Integer> references = referenceMap.get (elementID);
    return convertIDToElementReference (references);
  }

  /**
   * helper method to convert elementReferenceIDs to ElementReference objects
   * 
   * @param references list of elementReferenceIDs
   * @return
   */
  private List<ElementReference> convertIDToElementReference (Set<Integer> references)
  {
    List<ElementReference> ret = new ArrayList<ElementReference> ();

    if (references != null) {
      for (Integer refId : references) {
        ret.add (getElementReference (refId));
      }
    }
    return ret;
  }

  /**
   * Looks up elementMasterReverseLookup to check if an entry exists.
   * 
   * @param projectName Name of the project where the element is potentially defined
   * @param moduleName Name of the module
   * @param type Element type
   * @param elementName Unqualified name of the element
   * @return ID of the element, if one exists in cache. Returns null, otherwise.
   */
  public synchronized Integer lookupElement (String projectName, String moduleName, ElementType type, String elementName)
  {
    String key = IDManager.getInstance ().createQualifiedKey (type.toString (), projectName, moduleName, elementName);
    return elementMasterReverseLookup.get (key);
  }

  /**
   * Returns a list of reference IDs for a given element definition ID
   * 
   * @param elementId
   * @return
   */
  public synchronized Set<Integer> getReferenceIdsForElementDef (Integer elementId)
  {
    return referenceMap.get (elementId);
  }

  /**
   * Determines the element type of the token at the given offset. If the selected token does not fall under any of the
   * valid AQL element types, then it returns ElementType.UNKNOWN.
   * 
   * @param file IFile instance where the token appears
   * @param offset Begin offset of the token (started from beginning of the file)
   * @return Element type of the token identified by offset or ElementType.UNKNOWN if the type can not be determined
   */
  public synchronized ElementType getElementType (IFile file, int offset)
  {
    Integer fileId = FileCache.getInstance ().getFileId (file);

    // Find an element reference at the given offset
    ElementReference elemRef = null;
    Collection<ElementReference> refs = elementReferenceMaster.values ();
    for (ElementReference ref : refs) {
      
       if (fileId != null && ref.getLocation () != null && ref.getLocation ().getFileId () == fileId.intValue ()
        && ref.getLocation ().getOffset () == offset) {
        elemRef = ref;
        break;
      }
    }

    // if an element reference is found, then determine the ElementDefintion's type
    if (elemRef != null) {
      ElementDefinition elemDef = elementDefinitionMaster.get (elemRef.getElementId ());
      if(elemDef == null)
        return ElementType.UNKNOWN;
      
      return elemDef.getType ();
    }

    return ElementType.UNKNOWN;
  }

  /*
   * Creates an XML for persisting the index.
   */
  private String toXML ()
  {
    StringBuffer buff = new StringBuffer ();
    buff.append (Constants.XML_VERSION_ENCODING);
    buff.append (Constants.XML_ELEMENT_CACHE + Constants.ELEMENT_CACHE_LATEST_VERSION+ Constants.XML_CLOSE_TAG); 
                                               //^Include element cache version
    
    Set<Integer> keySet = null;
    if (elementMaster != null && !elementMaster.isEmpty ()) {
      keySet = elementMaster.keySet ();
      buff.append (Constants.XML_ELEMENT_MASTER_CONFIG);
      for (Integer key : keySet) {
        buff.append (Constants.XML_ELEMENT_MASTER_ID + key.toString () + Constants.XML_CLOSE_TAG);
        buff.append (elementMaster.get (key));
        buff.append (Constants.XML_ELEMENT_MASTER_END);
      }
      buff.append (Constants.XML_ELEMENT_MASTER_CONFIG_END);
    }

    ElementDefinition elementDefinition = null;
    if (elementDefinitionMaster != null && !elementDefinitionMaster.isEmpty ()) {
      keySet = elementDefinitionMaster.keySet ();

      buff.append (Constants.XML_ELEMENT_DEFENITION_MASTER_CONFIG);
      for (Integer key : keySet) {
        elementDefinition = elementDefinitionMaster.get (key);
        buff.append (Constants.XML_ELEMENT_DEF_MASTER_ID + key.toString () + Constants.XML_CLOSE_TAG);

        buff.append (Constants.XML_PROJECT_ID);
        buff.append (elementDefinition.getProjectId ());
        buff.append (Constants.XML_PROJECT_ID_END);

        buff.append (Constants.XML_MODULE_ID);
        buff.append (elementDefinition.getModuleId ());
        buff.append (Constants.XML_MODULE_ID_END);

        buff.append (Constants.XML_ELEMENT_TYPE);
        buff.append (elementDefinition.getType ().name ());
        buff.append (Constants.XML_ELEMENT_TYPE_END);

        buff.append (Constants.XML_NAME);
        buff.append (elementDefinition.getName ());
        buff.append (Constants.XML_NAME_END);

        buff.append (Constants.XML_LOCATION);

        buff.append ("<offset>");
        buff.append (elementDefinition.getLocation ().getOffset ());
        buff.append ("</offset>");

        buff.append ("<fileId>");
        buff.append (elementDefinition.getLocation ().getFileId ());
        buff.append ("</fileId>");

        buff.append (Constants.XML_LOCATION_END);

        Set<Integer> elementReferenceIdList = elementDefinition.getReferenceList ();
        if (elementReferenceIdList != null && !elementReferenceIdList.isEmpty ()) {
          buff.append (Constants.XML_ELEMENT_REFERENCE_LIST);

          for (Integer elementReference : elementReferenceIdList) {
            buff.append (Constants.XML_ID);
            buff.append (elementReference);
            buff.append (Constants.XML_ID_END);

          }
          buff.append (Constants.XML_ELEMENT_REFERENCE_LIST_END);
        }

        buff.append (Constants.XML_ELEMENT_DEF_MASTER_END);
      }
      buff.append (Constants.XML_ELEMENT_DEFENITION_MASTER_CONFIG_END);
    }

    if (elementReferenceMaster != null && !elementReferenceMaster.isEmpty ()) {
      keySet = elementReferenceMaster.keySet ();
      ElementReference elementReference = null;
      buff.append (Constants.XML_ELEMENT_REFERENCE_MASTER_CONFIG);
      for (Integer key : keySet) {
        elementReference = elementReferenceMaster.get (key);
        buff.append (Constants.XML_ELEMENT_REFERENCE_MASTER_ID + key.toString () + Constants.XML_CLOSE_TAG);

        buff.append (Constants.XML_ELEMENT_ID);
        buff.append (elementReference.getElementId ());
        buff.append (Constants.XML_ELEMENT_ID_END);

        buff.append (Constants.XML_LOCATION);

        buff.append ("<offset>");
        buff.append (elementReference.getLocation ().getOffset ());
        buff.append ("</offset>");

        buff.append ("<fileId>");
        buff.append (elementReference.getLocation ().getFileId ());
        buff.append ("</fileId>");

        buff.append (Constants.XML_LOCATION_END);

        buff.append (Constants.XML_ELEMENT_REFERENCE_MASTER_END);
      }
      buff.append (Constants.XML_ELEMENT_REFERENCE_MASTER_CONFIG_END);
    }

    if (referenceMap != null && !referenceMap.isEmpty ()) {
      keySet = referenceMap.keySet ();
      buff.append (Constants.XML_REFERENCE_HIERARCHY_MAP_CONFIG);
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
      buff.append (Constants.XML_REFERENCE_HIERARCHY_MAP_CONFIG_END);
    }
    
    if (elementMetadataMap != null && !elementMetadataMap.isEmpty ()) {
      keySet = elementMetadataMap.keySet ();
      buff.append (Constants.XML_ELEMENT_METADATA_MAP_CONFIG);
      for (Integer key: keySet) {
        ElementMetadata emd = elementMetadataMap.get (key);
        buff.append (Constants.XML_ELEMENT_METADATA_ID + key.toString () + Constants.XML_CLOSE_TAG);
        
        buff.append (Constants.XML_ELEMENT_METADATA_ACTIVESTATE);
        buff.append (emd.getElementActiveState ());
        buff.append (Constants.XML_ELEMENT_METADATA_ACTIVESTATE_END);
        
        buff.append (Constants.XML_ELEMENT_METADATA_END);
      }
      buff.append (Constants.XML_ELEMENT_METADATA_MAP_CONFIG_END);
    }

    buff.append (Constants.XML_ELEMENT_CACHE_END);
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

    File elementMasterConfigFile = new File (directory, Constants.ELEMENT_CACHE_IDX);
    FileOutputStream output = null;
    try {
      output = new FileOutputStream (elementMasterConfigFile);
      String content = toXML ();
      output.write (content.getBytes (Constants.UTF_8));
    }
    catch (IOException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
      throw e;
    }
    finally {
      try {
        if (output != null) {
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

    File elementMasterConfigFile = new File (directory, Constants.ELEMENT_CACHE_IDX);

    clear (); // reset all maps within cache

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance ();
    Document doc = null;
    InputStream in = null;
    try {
      in = new FileInputStream (elementMasterConfigFile);
      byte[] bytes = new byte[(int) elementMasterConfigFile.length ()];
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
    finally {
      try {
        if (in != null) {
          in.close ();
        }
      }
      catch (IOException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logDebug ("Failed to close element index file after loading.", e); //$NON-NLS-1$
      }
    }
    if (doc != null && doc.getDocumentElement () != null) {
      doc.getDocumentElement ().normalize ();
    }
    NodeList listElementMaster = null;
    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (0) != null) {
      listElementMaster = doc.getChildNodes ().item (0).getChildNodes ().item (0).getChildNodes ();
      for (int i = 0; i < listElementMaster.getLength (); i++) {
        Element modMasterElement = (Element) listElementMaster.item (i);
        Integer elementId = Integer.parseInt (modMasterElement.getAttribute (Constants.ID));
        String element = modMasterElement.getTextContent ();
        elementMaster.put (elementId, element);
        elementMasterReverseLookup.put (element, elementId);
      }
    }

    NodeList list = null;
    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (1) != null) {
      listElementMaster = doc.getChildNodes ().item (0).getChildNodes ().item (1).getChildNodes ();

      ElementDefinition elementDefinition = null;
      for (int i = 0; i < listElementMaster.getLength (); i++) {
        Element modMasterElement = (Element) listElementMaster.item (i);
        Integer elementId = Integer.parseInt (modMasterElement.getAttribute (Constants.ID));
        list = modMasterElement.getElementsByTagName (Constants.ELEMENT_PROJECT_ID);
        int projectId = Integer.parseInt (list.item (0).getTextContent ());

        list = modMasterElement.getElementsByTagName (Constants.ELEMENT_MODULE_ID);
        int moduleId = Integer.parseInt (list.item (0).getTextContent ());

        list = modMasterElement.getElementsByTagName (Constants.ELEMENT_TYPE);
        ElementType type = ElementType.valueOf (list.item (0).getTextContent ());

        list = modMasterElement.getElementsByTagName (Constants.ELEMENT_NAME);
        String name = list.item (0).getTextContent ();

        NodeList locList = modMasterElement.getElementsByTagName (Constants.ELEMENT_LOCATION);
        list = ((Element) locList.item (0)).getElementsByTagName ("offset");
        int offset = Integer.parseInt (list.item (0).getTextContent ());

        list = ((Element) locList.item (0)).getElementsByTagName ("fileId");
        int fileId = Integer.parseInt (list.item (0).getTextContent ());

        elementDefinition = new ElementDefinition (projectId, moduleId, elementId, type, new ElementLocation (fileId,
          offset));

        NodeList refList = modMasterElement.getElementsByTagName (Constants.ELEMENT_REFERENCE_LIST);
        if (refList != null && refList.item (0) != null) {
          list = ((Element) refList.item (0)).getElementsByTagName (Constants.ID);
          for (int j = 0; j < list.getLength (); j++) {
            elementDefinition.addReference (Integer.parseInt (list.item (j).getTextContent ()));
          }
        }

        elementDefinitionMaster.put (elementId, elementDefinition);
      }
    }

    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (2) != null) {
      listElementMaster = doc.getChildNodes ().item (0).getChildNodes ().item (2).getChildNodes ();
      ElementReference elementReference = null;
      for (int i = 0; i < listElementMaster.getLength (); i++) {
        Element modMasterElement = (Element) listElementMaster.item (i);
        Integer refId = Integer.parseInt (modMasterElement.getAttribute (Constants.ID));
        list = modMasterElement.getElementsByTagName (Constants.ELEMENT_ID);
        int elementId = Integer.parseInt (list.item (0).getTextContent ());

        NodeList locList = modMasterElement.getElementsByTagName (Constants.ELEMENT_LOCATION);
        list = ((Element) locList.item (0)).getElementsByTagName ("offset");
        int offset = Integer.parseInt (list.item (0).getTextContent ());

        list = ((Element) locList.item (0)).getElementsByTagName ("fileId");
        int fileId = Integer.parseInt (list.item (0).getTextContent ());

        elementReference = new ElementReference (refId, elementId, new ElementLocation (fileId, offset));
        elementReferenceMaster.put (refId, elementReference);

      }
    }

    if (doc != null && doc.getChildNodes ().item (0).getChildNodes ().item (3) != null) {
      listElementMaster = doc.getChildNodes ().item (0).getChildNodes ().item (3).getChildNodes ();
      Set<Integer> refList = null;
      for (int i = 0; i < listElementMaster.getLength (); i++) {
        refList = new HashSet<Integer> ();
        Element modMasterElement = (Element) listElementMaster.item (i);
        Integer refId = Integer.parseInt (modMasterElement.getAttribute (Constants.ID));

        NodeList refIdlist = modMasterElement.getElementsByTagName (Constants.ELEMENT_REF_ID);
        for (int j = 0; j < refIdlist.getLength (); j++) {
          refList.add (Integer.parseInt (refIdlist.item (j).getTextContent ()));
        }
        referenceMap.put (refId, refList);
      }
    }
    
    loadElementMetadata (doc);
    
    if (isElementCacheMigrationRequired (doc)) {
      migrateElementCache ();
    }
  }
  
  /**
   * Initialises elementMetadataMap with values from persisted xml document representing element cache.
   * @param doc xml document
   */
  private synchronized void loadElementMetadata(Document doc) {
    if (doc !=  null && doc.getChildNodes ().item (0).getChildNodes ().item (4) != null) {
      NodeList listMetadataElements = doc.getChildNodes ().item (0).getChildNodes ().item (4).getChildNodes ();
      for (int i = 0; i < listMetadataElements.getLength (); i++) {
        Element modMetadataElement = (Element) listMetadataElements.item (i);
        Integer elementId = Integer.parseInt (modMetadataElement.getAttribute (Constants.ID));
        NodeList nl = modMetadataElement.getElementsByTagName (Constants.ELEMENT_METADATA_ACTIVESTATE);
        String val = nl.item (0).getTextContent ();
        boolean elementActiveState = Boolean.parseBoolean (val);
        ElementMetadata elementMeta = new ElementMetadata(elementActiveState);
        elementMetadataMap.put (elementId, elementMeta);
      }
    }
  }

  @Override
  public synchronized void clear ()
  {
    elementMaster.clear ();
    elementMasterReverseLookup.clear ();
    elementDefinitionMaster.clear ();
    elementReferenceMaster.clear ();
    referenceMap.clear ();
    elementMetadataMap.clear ();
  }
  
  /**
   * Looks at version attribute in given xml doc for element cache, determines if migration is required.
   * @param doc xml document representing the persisted element cache.
   * @return true if version attribute's value is less than the value hard coded for element cache.
   * @see com.ibm.biginsights.textanalytics.indexer.Constants#ELEMENT_CACHE_LATEST_VERSION 
   */
  private synchronized boolean isElementCacheMigrationRequired(Document doc) {
   //retrieve version attribute of tag 'elementCache'
   String version = ((Element)doc.getChildNodes().item (0)).getAttribute (Constants.ELEMENT_CACHE_VERSION_ATTRIBUTE);
   if (version != null && !version.isEmpty ()) {
     if (version.compareTo (Constants.ELEMENT_CACHE_LATEST_VERSION) < 0) {
       return true;
     } else {
       return false;
     }
   } else {
     return true; //return true if version attribute is not found.
   }
  }
  
  /**
   * Migrate element cache to version compatible with tooling plugin v2120
   */
  private synchronized void migrateElementCache() {
    // For versions less than 2120 to 2120, the element cache would be elementMetadata entries.
    // Create an elementMetadata object containing activeState property set to true for each element id in
    // elementMaster.
    elementMetadataMap.clear ();
    for (Integer elemId : elementMaster.keySet ()) {
      elementMetadataMap.put (elemId, new ElementMetadata (true));
    }
    LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo ("Element cache migrated."); //$NON-NLS-1$
  }

  /**
   * Returns the list of element references in given file
   * 
   * @param file File whose element references are to be returned
   * @return List of element references in given file
   * @throws Exception if the given file is not already indexed
   */
  public synchronized List<ElementReference> getElementReferencesInFile (IFile file) throws Exception
  {
    List<ElementReference> ret = new ArrayList<ElementReference> ();

    // Step 1: Get file id
    Integer fileId = FileCache.getInstance ().getFileId (file);

    // Step 2: If file id is not found, throw an error
    if (fileId == null) { throw new Exception (String.format ("The specified file %s is not indexed yet.",
      file.getLocation ().toOSString ())); }

    // Step 3: Pick all elementDefs in given file
    Collection<ElementReference> elemRefs = elementReferenceMaster.values ();
    for (ElementReference elemRef : elemRefs) {
      if (elemRef.getLocation ().getFileId () == fileId.intValue ()) {
        ret.add (elemRef);
      }
    }

    return ret;
  }
  
  /**
   * Checks if element with given id is active, by look at its metadata object.
   * @param id element id
   * @return true if element is active, false if otherwise, or if the element's metadata is not found.
   */
  public synchronized boolean isElementActive(Integer id) {
    ElementMetadata emd = elementMetadataMap.get (id);
    if (emd != null) {
      return emd.getElementActiveState ();
    } else {
      return false;
    }
  }

}
