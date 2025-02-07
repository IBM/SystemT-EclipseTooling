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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.indexer.Constants;
import com.ibm.biginsights.textanalytics.indexer.index.TextAnalyticsIndexer;
import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.model.FileReference;

public class FileCacheTest
{
  private static final String METRICS_INDICATOR_UDFS = "metricsIndicator_udfs";//$NON-NLS-1$
  private static final String REFINEMENT_RENAMED_AQL = "refinementRenamed.aql";//$NON-NLS-1$
  private static final String CONCEPTS_RENAMED_AQL = "conceptsRenamed.aql";//$NON-NLS-1$
  private static final String BASICS_RENAMED_AQL = "basicsRenamed.aql";//$NON-NLS-1$
  private static final String REFINEMENT_AQL = "refinement.aql";//$NON-NLS-1$
  private static final String CONCEPTS_AQL = "concepts.aql";//$NON-NLS-1$
  private static final String BASICS_AQL = "basics.aql";//$NON-NLS-1$
  private static final String METRICS_INDICATOR_FEATURES_RENAMED = "metricsIndicator_featuresRenamed";//$NON-NLS-1$
  private static final String METRICS_INDICATOR_FEATURES = "metricsIndicator_features";//$NON-NLS-1$
  private static final String AQL_PATH3 = "/TestWF2/textAnalytics/src/metricsIndicator_features/refinement.aql";//$NON-NLS-1$
  private static final String AQL_PATH2 = "/TestWF2/textAnalytics/src/metricsIndicator_features/concepts.aql";//$NON-NLS-1$
  private static final String AQL_PATH1 = "/TestWF2/textAnalytics/src/metricsIndicator_features/basics.aql";//$NON-NLS-1$
  private static final String FINAL_AQL = "final.aql";//$NON-NLS-1$
  private static final String PERSON_NAME_FINALS = "PersonName_Finals";//$NON-NLS-1$
  private static final String PROJECT_TEST_WF2_RENAMED = "TestWF2Renamed";//$NON-NLS-1$
  private static final String TEST_WF2_EXTRACTIONPLAN = "/TestWF2/.extractionplan";//$NON-NLS-1$
  private static final String TEST_WF2_TEXTANALYTICS = "/TestWF2/.textanalytics";//$NON-NLS-1$
  private static final String TEST_WF2_CLASSPATH = "/TestWF2/.classpath";//$NON-NLS-1$
  private static final String AQL_PATH = "/TestWF2/textAnalytics/src/PersonName_Finals/final.aql";//$NON-NLS-1$
  private static final String PROJECT_TEST_WF2 = "TestWF2";//$NON-NLS-1$

  IProject project;
  IWorkspaceRoot root;
  FileCache cache = FileCache.getInstance ();

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
  public void testAddFile ()
  {
    /**
     * Add the file to File Cache Call getXXXFileId() to get the File Ids based on AQL or .classpath or .extarctionplan
     * or .textanalytics
     */
    cache.clear ();
    IFile file = root.getFile (new Path (AQL_PATH));
    Integer fileId = cache.addFile (file);
    assertEquals (fileId, cache.getAQLFileId (file));

    file = root.getFile (new Path (TEST_WF2_CLASSPATH));
    fileId = cache.addFile (file);
    assertEquals (fileId, cache.getClasspathFileId (project.getName ()));

    file = root.getFile (new Path (TEST_WF2_TEXTANALYTICS));
    fileId = cache.addFile (file);
    assertEquals (fileId, cache.getTextAnalyticsPropertiesFileId (project.getName ()));

    file = root.getFile (new Path (TEST_WF2_EXTRACTIONPLAN));
    fileId = cache.addFile (file);
    assertEquals (fileId, cache.getExtractionPlanFileId (project.getName ()));

  }

  @Test
  public void testUpdateProjectName ()
  {
    /**
     * Test the Project Rename in the file Cache.
     */
    IFile file = root.getFile (new Path (AQL_PATH));
    Integer aqlFileId = cache.getFileId (file);

    file = root.getFile (new Path (TEST_WF2_CLASSPATH));
    Integer claFileId = cache.getClasspathFileId (project.getName ());

    file = root.getFile (new Path (TEST_WF2_TEXTANALYTICS));
    Integer taFileId = cache.getTextAnalyticsPropertiesFileId (project.getName ());

    file = root.getFile (new Path (TEST_WF2_EXTRACTIONPLAN));
    Integer epFileId = cache.getExtractionPlanFileId (project.getName ());

    cache.updateProjectName (PROJECT_TEST_WF2, PROJECT_TEST_WF2_RENAMED);

    assertEquals (aqlFileId, cache.getAQLFileId (PROJECT_TEST_WF2_RENAMED, PERSON_NAME_FINALS, FINAL_AQL));
    assertEquals (claFileId, cache.getClasspathFileId (PROJECT_TEST_WF2_RENAMED));
    assertEquals (taFileId, cache.getTextAnalyticsPropertiesFileId (PROJECT_TEST_WF2_RENAMED));
    assertEquals (epFileId, cache.getExtractionPlanFileId (PROJECT_TEST_WF2_RENAMED));
  }

  @Test
  public void testUpdateModuleName ()
  {
    /**
     * Test the Module Rename in the File cache
     */
    cache.clear ();
    IFile file = root.getFile (new Path (AQL_PATH1));
    Integer aqlFileId1 = cache.addFile (file);

    file = root.getFile (new Path (AQL_PATH2));
    Integer aqlFileId2 = cache.addFile (file);

    file = root.getFile (new Path (AQL_PATH3));
    Integer aqlFileId3 = cache.addFile (file);

    cache.updateProjectName (PROJECT_TEST_WF2, PROJECT_TEST_WF2_RENAMED);
    cache.updateModuleName (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES, METRICS_INDICATOR_FEATURES_RENAMED);

    assertEquals (aqlFileId1,
      cache.getAQLFileId (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES_RENAMED, BASICS_AQL));
    assertEquals (aqlFileId2,
      cache.getAQLFileId (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES_RENAMED, CONCEPTS_AQL));
    assertEquals (aqlFileId3,
      cache.getAQLFileId (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES_RENAMED, REFINEMENT_AQL));

  }

  @Test
  public void testUpdateFileName1 ()
  {
    /**
     * Test the file Rename in the File cache
     */

    cache.clear ();
    IFile file = root.getFile (new Path (AQL_PATH1));
    Integer aqlFileId1 = cache.addFile (file);

    file = root.getFile (new Path (AQL_PATH2));
    Integer aqlFileId2 = cache.addFile (file);

    file = root.getFile (new Path (AQL_PATH3));
    Integer aqlFileId3 = cache.addFile (file);

    cache.updateProjectName (PROJECT_TEST_WF2, PROJECT_TEST_WF2_RENAMED);
    cache.updateModuleName (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES, METRICS_INDICATOR_FEATURES_RENAMED);
    cache.updateFileName (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES_RENAMED, BASICS_AQL, BASICS_RENAMED_AQL);
    cache.updateFileName (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES_RENAMED, CONCEPTS_AQL,
      CONCEPTS_RENAMED_AQL);
    cache.updateFileName (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES_RENAMED, REFINEMENT_AQL,
      REFINEMENT_RENAMED_AQL);

    assertEquals (aqlFileId1,
      cache.getAQLFileId (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES_RENAMED, BASICS_RENAMED_AQL));
    assertEquals (aqlFileId2,
      cache.getAQLFileId (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES_RENAMED, CONCEPTS_RENAMED_AQL));
    assertEquals (aqlFileId3,
      cache.getAQLFileId (PROJECT_TEST_WF2_RENAMED, METRICS_INDICATOR_FEATURES_RENAMED, REFINEMENT_RENAMED_AQL));

  }

  @Test
  public void testUpdateFileName2 ()
  {
    /**
     * Test the File Rename in the File cache
     */

    cache.clear ();
    IFile oldFile = root.getFile (new Path (AQL_PATH1));
    Integer aqlFileId1 = cache.addFile (oldFile);

    IFile newFile = root.getFile (new Path (AQL_PATH2));
    cache.updateFileName (oldFile, newFile);

    assertEquals (aqlFileId1, cache.getAQLFileId (PROJECT_TEST_WF2, METRICS_INDICATOR_FEATURES, CONCEPTS_AQL));

  }

  @Test
  public void testRemoveFile1 ()
  {
    /**
     * Test the removal of File from the File cache
     */

    cache.clear ();
    IFile file = root.getFile (new Path (AQL_PATH1));
    cache.addFile (file);
    cache.removeFile (file);
    Integer aqlFileId1 = cache.getAQLFileId (file);
    assertNull (aqlFileId1);

  }

  @Test
  public void testRemoveFile2 ()
  {
    /**
     * Test the removal of File from the File cache
     */

    cache.clear ();
    IFile file = root.getFile (new Path (AQL_PATH1));
    Integer aqlFileId1 = cache.addFile (file);
    cache.removeFile (aqlFileId1);
    Integer id = cache.getAQLFileId (file);
    assertNull (id);

  }

  @Test
  public void testAddFileReference ()
  {
    /**
     * Call the addFileReference and retrieve the same using getReferences and getFileReference API
     */
    cache.clear ();
    IFile file = root.getFile (new Path (AQL_PATH1));
    Integer aqlFileId1 = cache.addFile (file);
    ElementLocation location1 = new ElementLocation (aqlFileId1, 12);
    FileReference fileRef = new FileReference (111, aqlFileId1, location1);
    cache.addFileReference (fileRef);

    location1 = new ElementLocation (aqlFileId1, 45);
    fileRef = new FileReference (222, aqlFileId1, location1);
    cache.addFileReference (fileRef);

    location1 = new ElementLocation (aqlFileId1, 90);
    fileRef = new FileReference (333, aqlFileId1, location1);
    cache.addFileReference (fileRef);

    List<Integer> refList = cache.getReferences (aqlFileId1);
    assertEquals (3, refList.size ());

    FileReference fileRefAct = cache.getFileReference (111);

    assertEquals (111, fileRefAct.getFileRefId ().intValue ());
    assertEquals (aqlFileId1.intValue (), fileRefAct.getFileId ().intValue ());
    assertEquals (12, fileRefAct.getLocation ().getOffset ());
    assertEquals (aqlFileId1.intValue (), fileRefAct.getLocation ().getFileId ());

  }

  @Test
  public void testDeleteReferencesInFile ()
  {
    /**
     * Delete the references for a File using file ID
     */
    IFile file = root.getFile (new Path (AQL_PATH1));
    Integer aqlFileId = cache.getAQLFileId (file);
    cache.deleteReferencesInFile (aqlFileId);
    List<Integer> refList = cache.getReferences (aqlFileId);
    assertEquals (0, refList.size ());
    FileReference fileRefAct = cache.getFileReference (111);
    assertNull (fileRefAct);

  }

  @Test
  public void testDeleteReferencesInFile1 ()
  {
    /**
     * Delete the references for a File using IFile
     */

    cache.clear ();
    IFile file = root.getFile (new Path (AQL_PATH1));
    Integer aqlFileId1 = cache.addFile (file);
    ElementLocation location1 = new ElementLocation (aqlFileId1, 12);
    FileReference fileRef = new FileReference (111, aqlFileId1, location1);
    cache.addFileReference (fileRef);

    location1 = new ElementLocation (aqlFileId1, 45);
    fileRef = new FileReference (222, aqlFileId1, location1);
    cache.addFileReference (fileRef);

    location1 = new ElementLocation (aqlFileId1, 90);
    fileRef = new FileReference (333, aqlFileId1, location1);
    cache.addFileReference (fileRef);

    cache.deleteReferencesInFile (file);
    List<Integer> refList = cache.getReferences (aqlFileId1);
    assertEquals (0, refList.size ());
    FileReference fileRefAct = cache.getFileReference (111);
    assertNull (fileRefAct);

  }

  @Test
  public void testGetAQLFileIds () throws Exception
  {
    /**
     * Get all AQLs for a Project
     */
    TextAnalyticsIndexer.getInstance ().reindex ();
    Integer fileIds[] = cache.getAQLFileIds (project.getName ());
    assertEquals (12, fileIds.length);
  }

  @Test
  public void testGetAQLFileIds1 ()
  {
    /**
     * Get all AQLs for a Module in a project
     */

    Integer fileIds[] = cache.getAQLFileIds (project.getName (), METRICS_INDICATOR_FEATURES);
    assertEquals (3, fileIds.length);
    fileIds = cache.getAQLFileIds (project.getName (), METRICS_INDICATOR_UDFS);
    assertEquals (1, fileIds.length);

  }

  @Test
  public void testWriteAndLoad () throws Exception
  {
    /**
     * Testing the Write and Load API
     */
    cache.clear ();
    IFile file = root.getFile (new Path (AQL_PATH1));
    Integer aqlFileId1 = cache.addFile (file);
    ElementLocation location1 = new ElementLocation (aqlFileId1, 12);
    FileReference fileRef = new FileReference (111, aqlFileId1, location1);
    cache.addFileReference (fileRef);

    location1 = new ElementLocation (aqlFileId1, 45);
    fileRef = new FileReference (222, aqlFileId1, location1);
    cache.addFileReference (fileRef);

    location1 = new ElementLocation (aqlFileId1, 90);
    fileRef = new FileReference (333, aqlFileId1, location1);
    cache.addFileReference (fileRef);

    cache.write ();
    cache.clear ();
    cache.load ();

    List<Integer> refList = cache.getReferences (aqlFileId1);
    assertEquals (3, refList.size ());

    FileReference fileRefAct = cache.getFileReference (111);

    assertEquals (111, fileRefAct.getFileRefId ().intValue ());
    assertEquals (aqlFileId1.intValue (), fileRefAct.getFileId ().intValue ());
    assertEquals (12, fileRefAct.getLocation ().getOffset ());
    assertEquals (aqlFileId1.intValue (), fileRefAct.getLocation ().getFileId ());

  }

}
