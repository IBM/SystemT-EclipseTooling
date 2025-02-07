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

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.indexer.Constants;
import com.ibm.biginsights.textanalytics.indexer.index.IDManager;
import com.ibm.biginsights.textanalytics.indexer.model.ElementDefinition;
import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.model.ElementReference;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;

public class ElementCacheTest
{

  private static final String MODULE_METRICS_INDICATOR_FEATURES_RENAMED = "metricsIndicator_featuresRenamed"; //$NON-NLS-1$
  private static final String PROJECT_TEST_WF2_RENAMED = "TestWF2Renamed"; //$NON-NLS-1$
  private static final String VIEW_UNIT = "Unit";//$NON-NLS-1$
  private static final String METRICS_INDICATOR_FEATURES_METRIC = "metricsIndicator_features.Metric";//$NON-NLS-1$
  private static final String VIEW = "VIEW";//$NON-NLS-1$
  private static final String VIEW_METRIC = "Metric";//$NON-NLS-1$
  private static final String PATH_TO_BASICS_AQL = "/TestWF2/textAnalytics/src/metricsIndicator_features/basics.aql";//$NON-NLS-1$
  private static final String MODULE_METRICS_INDICATOR_FEATURES = "metricsIndicator_features";//$NON-NLS-1$
  private static final String PROJECT_TEST_WF2 = "TestWF2";//$NON-NLS-1$

  IProject project;
  IWorkspaceRoot root;
  ElementCache elemCache = ElementCache.getInstance ();
  ProjectCache projectCache = ProjectCache.getInstance ();
  ModuleCache moduleCache = ModuleCache.getInstance ();
  FileCache fileCache = FileCache.getInstance ();
  IDManager idManager = IDManager.getInstance ();

  @Before
  public void setUp () throws Exception
  {
    root = ResourcesPlugin.getWorkspace ().getRoot ();
    project = root.getProject (PROJECT_TEST_WF2);

    String workspacePath = root.getLocation ().makeAbsolute ().toOSString ();
    File directory = new File (workspacePath + Constants.INDEX_PATH);
    directory.deleteOnExit ();
  }

  @Test
  public void testAddElementDefintion () throws Exception
  {
    /**
     * Testing the insertion of Element definition to the Cache and test the Get API's also.
     */

    projectCache.clear ();
    moduleCache.clear ();
    fileCache.clear ();
    elemCache.clear ();

    Integer projectId = projectCache.getProjectId (project.getName ());
    Integer moduleId = moduleCache.getModuleId (project.getName (), MODULE_METRICS_INDICATOR_FEATURES);

    IFile file = root.getFile (new Path (PATH_TO_BASICS_AQL));
    Integer fileId = fileCache.addFile (file);
    Integer elementId = elemCache.getElementId (project.getName (), MODULE_METRICS_INDICATOR_FEATURES,
      ElementType.VIEW, VIEW_METRIC);
    int offset = IndexerUtil.calculateOffset (file, 33, 13);
    ElementLocation location = new ElementLocation (fileId.intValue (), offset);
    ElementDefinition elemDef = new ElementDefinition (projectId, moduleId, elementId, ElementType.VIEW, location);
    String qualifiedName = idManager.createQualifiedKey (ElementType.VIEW.toString (), project.getName (),
      MODULE_METRICS_INDICATOR_FEATURES, VIEW_METRIC);
    elemCache.addElementDefintion (elemDef, qualifiedName);

    List<ElementDefinition> defList = elemCache.getElementDefinitionsInFile (file);
    ElementDefinition def = defList.get (0);
    assertEquals (projectId, def.getProjectId ());
    assertEquals (ElementType.VIEW, def.getType ());
    assertEquals (moduleId, def.getModuleId ());
    assertEquals (elementId, def.getElementId ());
    assertEquals (fileId.intValue (), def.getLocation ().getFileId ());
    assertEquals (offset, def.getLocation ().getOffset ());

    def = elemCache.getElementDefinition (elementId);
    assertEquals (projectId, def.getProjectId ());
    assertEquals (ElementType.VIEW, def.getType ());
    assertEquals (moduleId, def.getModuleId ());
    assertEquals (elementId, def.getElementId ());
    assertEquals (fileId.intValue (), def.getLocation ().getFileId ());
    assertEquals (offset, def.getLocation ().getOffset ());

    String elemName = elemCache.getElementName (elementId);
    assertEquals (VIEW + Constants.QUALIFIED_NAME_SEPARATOR + PROJECT_TEST_WF2 + Constants.QUALIFIED_NAME_SEPARATOR
      + MODULE_METRICS_INDICATOR_FEATURES + Constants.QUALIFIED_NAME_SEPARATOR + VIEW_METRIC, elemName);

    elemName = elemCache.getElementNameInAQL (elementId);
    assertEquals (METRICS_INDICATOR_FEATURES_METRIC, elemName);

    Integer id = elemCache.getElementId (project.getName (), MODULE_METRICS_INDICATOR_FEATURES, ElementType.VIEW,
      VIEW_METRIC);
    assertEquals (elementId, id);

    id = elemCache.lookupElement (project.getName (), MODULE_METRICS_INDICATOR_FEATURES, ElementType.VIEW, VIEW_METRIC);
    assertEquals (elementId, id);

    ElementType type = elemCache.getElementType (file, offset);
    assertEquals (ElementType.VIEW.toString (), type.toString ());

  }

  @Test
  public void testUpdateProjectName () throws Exception
  {
    /**
     * Testing the Update Project name. There will be 2 elements in the element cache and test the Get API's also.
     */
    Integer elemId1 = elemCache.getElementId (project.getName (), MODULE_METRICS_INDICATOR_FEATURES, ElementType.VIEW,
      VIEW_METRIC);

    Integer projectId = projectCache.getProjectId (project.getName ());
    Integer moduleId = moduleCache.getModuleId (project.getName (), MODULE_METRICS_INDICATOR_FEATURES);
    IFile file = root.getFile (new Path (PATH_TO_BASICS_AQL));
    Integer fileId = fileCache.addFile (file);
    Integer elemId2 = elemCache.getElementId (project.getName (), MODULE_METRICS_INDICATOR_FEATURES, ElementType.VIEW,
      VIEW_UNIT);
    int offset = IndexerUtil.calculateOffset (file, 53, 13);
    ElementLocation location = new ElementLocation (fileId.intValue (), offset);
    ElementDefinition elemDef = new ElementDefinition (projectId, moduleId, elemId2, ElementType.VIEW, location);
    String qualifiedName = idManager.createQualifiedKey (ElementType.VIEW.toString (), project.getName (),
      MODULE_METRICS_INDICATOR_FEATURES, VIEW_UNIT);
    elemCache.addElementDefintion (elemDef, qualifiedName);

    elemCache.updateProjectName (PROJECT_TEST_WF2, PROJECT_TEST_WF2_RENAMED);

    Integer id = elemCache.lookupElement (PROJECT_TEST_WF2, MODULE_METRICS_INDICATOR_FEATURES, ElementType.VIEW,
      VIEW_METRIC);
    assertNull (id);

    id = elemCache.lookupElement (PROJECT_TEST_WF2_RENAMED, MODULE_METRICS_INDICATOR_FEATURES, ElementType.VIEW,
      VIEW_METRIC);
    assertEquals (elemId1.toString (), id.toString ());

    String elemName = elemCache.getElementName (elemId1);
    assertEquals (VIEW + Constants.QUALIFIED_NAME_SEPARATOR + PROJECT_TEST_WF2_RENAMED
      + Constants.QUALIFIED_NAME_SEPARATOR + MODULE_METRICS_INDICATOR_FEATURES + Constants.QUALIFIED_NAME_SEPARATOR
      + VIEW_METRIC, elemName);

    List<ElementDefinition> defList = elemCache.getElementDefinitionsInFile (file);
    assertEquals (2, defList.size ());

  }

  @Test
  public void testUpdateModuleName () throws Exception
  {
    /**
     * Update the Module Name in the element Cache and test the Get API's also.
     */
    elemCache.updateModuleName (PROJECT_TEST_WF2_RENAMED, MODULE_METRICS_INDICATOR_FEATURES,
      MODULE_METRICS_INDICATOR_FEATURES_RENAMED);

    Integer id = elemCache.lookupElement (PROJECT_TEST_WF2_RENAMED, MODULE_METRICS_INDICATOR_FEATURES,
      ElementType.VIEW, VIEW_UNIT);
    assertNull (id);

    Integer elemId2 = elemCache.getElementId (PROJECT_TEST_WF2_RENAMED, MODULE_METRICS_INDICATOR_FEATURES_RENAMED,
      ElementType.VIEW, VIEW_UNIT);

    id = elemCache.lookupElement (PROJECT_TEST_WF2_RENAMED, MODULE_METRICS_INDICATOR_FEATURES_RENAMED,
      ElementType.VIEW, VIEW_UNIT);
    assertEquals (elemId2.toString (), id.toString ());

    String elemName = elemCache.getElementName (elemId2);
    assertEquals (VIEW + Constants.QUALIFIED_NAME_SEPARATOR + PROJECT_TEST_WF2_RENAMED
      + Constants.QUALIFIED_NAME_SEPARATOR + MODULE_METRICS_INDICATOR_FEATURES_RENAMED
      + Constants.QUALIFIED_NAME_SEPARATOR + VIEW_UNIT, elemName);

    IFile file = root.getFile (new Path (PATH_TO_BASICS_AQL));
    List<ElementDefinition> defList = elemCache.getElementDefinitionsInFile (file);
    assertEquals (2, defList.size ());

    List<ElementReference> refList = elemCache.getElementReferencesInFile (file);
    assertEquals (2, refList.size ());

    id = elemCache.lookupElement (PROJECT_TEST_WF2_RENAMED, MODULE_METRICS_INDICATOR_FEATURES_RENAMED,
      ElementType.VIEW, VIEW_METRIC);
    ElementReference expectedElemRef = null;
    if (refList.get (0).getElementId () == id)
      expectedElemRef = refList.get (0);
    else
      expectedElemRef = refList.get (1);

    ElementReference elemRef = elemCache.getElementReference (expectedElemRef.getElementRefId ());
    assertEquals (expectedElemRef.getElementId (), elemRef.getElementId ());
    assertEquals (expectedElemRef.getLocation ().getFileId (), elemRef.getLocation ().getFileId ());
    assertEquals (expectedElemRef.getLocation ().getOffset (), elemRef.getLocation ().getOffset ());

    refList = elemCache.getReferencesForElemDef (elemRef.getElementId ());
    assertEquals (1, refList.size ());
    assertEquals (id, refList.get (0).getElementId ());

    Set<Integer> refIdSet = elemCache.getReferenceIdsForElementDef (id);
    assertEquals (1, refIdSet.size ());
    assertEquals (refList.get (0).getElementRefId (), refIdSet.iterator ().next ());

  }

  @Test
  public void testWriteAndLoad () throws Exception
  {
    /**
     * Test the write and load of the cache
     */
    elemCache.write ();

    elemCache.clear ();

    elemCache.load ();

    IFile file = root.getFile (new Path (PATH_TO_BASICS_AQL));
    List<ElementDefinition> defList = elemCache.getElementDefinitionsInFile (file);
    assertEquals (2, defList.size ());

    List<ElementReference> refList = elemCache.getElementReferencesInFile (file);
    assertEquals (2, refList.size ());

    ElementReference elemRef = elemCache.getElementReference (refList.get (0).getElementRefId ());
    Integer id = elemCache.lookupElement (PROJECT_TEST_WF2_RENAMED, MODULE_METRICS_INDICATOR_FEATURES_RENAMED,
      ElementType.VIEW, VIEW_METRIC);
    assertEquals (id, elemRef.getElementId ());
    assertEquals (defList.get (0).getLocation ().getFileId (), elemRef.getLocation ().getFileId ());
    assertEquals (defList.get (0).getLocation ().getOffset (), elemRef.getLocation ().getOffset ());

    refList = elemCache.getReferencesForElemDef (elemRef.getElementId ());
    assertEquals (1, refList.size ());
    assertEquals (id, refList.get (0).getElementId ());

    Set<Integer> refIdSet = elemCache.getReferenceIdsForElementDef (id);
    assertEquals (1, refIdSet.size ());
    assertEquals (refList.get (0).getElementRefId (), refIdSet.iterator ().next ());

  }

  @Test
  public void testDelete () throws Exception
  {
    /**
     * Test the deletion of references and definition.
     */
    IFile file = root.getFile (new Path (PATH_TO_BASICS_AQL));
    elemCache.deleteReferencesInFile (file);
    List<ElementReference> refList = elemCache.getElementReferencesInFile (file);
    assertEquals (0, refList.size ());

    elemCache.deleteDefinitionsInFile (file);
    List<ElementDefinition> defList = elemCache.getElementDefinitionsInFile (file);
    assertEquals (0, defList.size ());

  }

}
