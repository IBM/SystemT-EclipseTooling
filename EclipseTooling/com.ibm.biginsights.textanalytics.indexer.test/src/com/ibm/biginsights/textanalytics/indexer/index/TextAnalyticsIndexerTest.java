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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.indexer.Constants;
import com.ibm.biginsights.textanalytics.indexer.cache.ElementCache;
import com.ibm.biginsights.textanalytics.indexer.cache.FileCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ModuleCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ProjectCache;

public class TextAnalyticsIndexerTest
{

  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_FINALS_FINAL_AQL = "/TestWF2/textAnalytics/src/PersonName_Finals/final.aql";//$NON-NLS-1$
  private static final String TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_FINALS = "/TestWF2/textAnalytics/src/PersonName_Finals";//$NON-NLS-1$
  private static final String PERSON_NAME_FINALS = "PersonName_Finals";//$NON-NLS-1$
  private static final String TEXTANALYTICS = ".textanalytics";//$NON-NLS-1$
  private static final String TEST_WF2 = "TestWF2";//$NON-NLS-1$

  ElementCache elemCache = ElementCache.getInstance ();
  ProjectCache projectCache = ProjectCache.getInstance ();
  ModuleCache moduleCache = ModuleCache.getInstance ();
  FileCache fileCache = FileCache.getInstance ();
  IDManager idManager = IDManager.getInstance ();
  TextAnalyticsIndexer textAnalyticsIndexer = TextAnalyticsIndexer.getInstance ();
  IProject project;
  IWorkspaceRoot root;

  @Before
  public void setUp () throws Exception
  {
    root = ResourcesPlugin.getWorkspace ().getRoot ();
    project = root.getProject (TEST_WF2);

    String workspacePath = root.getLocation ().makeAbsolute ().toOSString ();
    File directory = new File (workspacePath + Constants.INDEX_PATH);
    directory.deleteOnExit ();
  }

  @Test
  public void testProjectAdded () throws Exception
  {
    elemCache.clear ();
    projectCache.clear ();
    moduleCache.clear ();
    fileCache.clear ();
    textAnalyticsIndexer.projectAdded (project);

    Integer projId = projectCache.getProjectId (project.getName ());
    String proName = projectCache.getProjectName (projId);
    assertEquals (project.getName (), proName);
  }

  @Test
  public void testProjectDeleted () throws Exception
  {
    Integer projId = projectCache.getProjectId (project.getName ());
    textAnalyticsIndexer.projectDeleted (project);
    Integer id = projectCache.getProjectId (project.getName ());
    assertNotSame (projId, id);
  }

  @Test
  public void testProjectOpened () throws Exception
  {
    textAnalyticsIndexer.projectOpened (project);

    Integer projId = projectCache.getProjectId (project.getName ());
    String proName = projectCache.getProjectName (projId);
    assertEquals (project.getName (), proName);
    List<Integer> refList = projectCache.getReferences (projId);


  }

  @Test
  public void testProjectClosed () throws Exception
  {
    Integer projId = projectCache.getProjectId (project.getName ());
    textAnalyticsIndexer.projectDeleted (project);
    Integer id = projectCache.getProjectId (project.getName ());
    assertNotSame (projId, id);

  }

  @Test
  public void testModuleDeleted () throws Exception
  {
    Integer modId = moduleCache.getModuleId (TEST_WF2, PERSON_NAME_FINALS);
    IFolder folder = root.getFolder (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_FINALS));
    textAnalyticsIndexer.moduleDeleted (folder);
    Integer id = moduleCache.getModuleId (TEST_WF2, PERSON_NAME_FINALS);
    assertNotSame (modId, id);
  }

  @Test
  public void testModuleAdded () throws Exception
  {
    Integer modId = moduleCache.getModuleId (TEST_WF2, PERSON_NAME_FINALS);
    IFolder folder = root.getFolder (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_FINALS));
    textAnalyticsIndexer.moduleAdded (folder);
    Integer id = moduleCache.getModuleId (TEST_WF2, PERSON_NAME_FINALS);
    assertEquals (modId, id);
  }

  @Test
  public void testFileDeleted () throws Exception
  {
    IFile file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_FINALS_FINAL_AQL));
    Integer filId = fileCache.getAQLFileId (file);
    assertNotNull (filId);
    textAnalyticsIndexer.fileDeleted (file);
    Integer id = fileCache.getAQLFileId (file);
    assertNull (id);

  }

  @Test
  public void testFileAdded () throws Exception
  {
    IFile file = root.getFile (new Path (TEST_WF2_TEXT_ANALYTICS_SRC_PERSON_NAME_FINALS_FINAL_AQL));
    textAnalyticsIndexer.fileAdded (file);
    Integer id = fileCache.getAQLFileId (file);
    assertNotNull (id);
  }

}
