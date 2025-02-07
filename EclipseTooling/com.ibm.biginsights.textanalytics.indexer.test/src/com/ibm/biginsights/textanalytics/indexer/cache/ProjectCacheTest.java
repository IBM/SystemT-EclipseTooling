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

import java.util.List;

import org.junit.Test;

import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.model.ProjectReference;

public class ProjectCacheTest
{

  private static final String TEST_PRO3 = "TestPro3";//$NON-NLS-1$
  private static final String TEST_PRO2 = "TestPro2";//$NON-NLS-1$
  private static final String TEST_PRO1 = "TestPro1";//$NON-NLS-1$

  ProjectCache proCache = ProjectCache.getInstance ();
  Integer testPro1;

  @Test
  public void testGetProjectId ()
  {
    // Test the getProjectId API
    testPro1 = proCache.addProject (TEST_PRO1);
    Integer testId = proCache.getProjectId (TEST_PRO1);
    assertEquals (testPro1, testId);
  }

  @Test
  public void testGetProjectName ()
  {
    // Test the getProjectName API
    testPro1 = proCache.getProjectId (TEST_PRO1);
    String projName = proCache.getProjectName (testPro1);
    assertEquals (TEST_PRO1, projName);
  }

  @Test
  public void testAddProject ()
  {
    // Test the addProject API
    Integer testPro2 = proCache.addProject (TEST_PRO2);
    Integer testId = proCache.getProjectId (TEST_PRO2);
    assertEquals (testPro2, testId);
    String projName = proCache.getProjectName (testId);
    assertEquals (TEST_PRO2, projName);

  }

  @Test
  public void testUpdateProjectName ()
  {
    // Test the updateProjectName API
    Integer testId = proCache.getProjectId (TEST_PRO2);
    proCache.updateProjectName (TEST_PRO2, TEST_PRO3);
    Integer id = proCache.getProjectId (TEST_PRO3);
    assertEquals (testId, id);

  }

  @Test
  public void testDeleteProject ()
  {
    // Test the deleteProject API
    Integer testId = proCache.getProjectId (TEST_PRO3);
    assertNotNull (testId);
    proCache.deleteProject (TEST_PRO3);
    String name = proCache.getProjectName (testId);
    assertNull (name);

  }

  @Test
  public void testAddProjectReference ()
  {
    // Test the getProjectReference API
    testPro1 = proCache.getProjectId (TEST_PRO1);

    ElementLocation location1 = new ElementLocation (111, 1234);
    ProjectReference projRef1 = new ProjectReference (444, testPro1, location1);

    ElementLocation location2 = new ElementLocation (222, 890);
    ProjectReference projRef2 = new ProjectReference (555, testPro1, location2);

    proCache.addProjectReference (projRef1);
    proCache.addProjectReference (projRef2);

    List<Integer> refList = proCache.getReferences (testPro1);
    assertEquals (2, refList.size ());

    ProjectReference projActualRef1 = proCache.getProjectReference (444);
    ProjectReference projActualRef2 = proCache.getProjectReference (555);

    assertEquals (testPro1, projActualRef1.getProjectId ());
    assertEquals (testPro1, projActualRef2.getProjectId ());
    assertEquals (new Integer (444), projActualRef1.getProjectRefId ());
    assertEquals (new Integer (555), projActualRef2.getProjectRefId ());

  }

  @Test
  public void testGetProjectReference ()
  {
    // Test the getProjectReference API
    testPro1 = proCache.getProjectId (TEST_PRO1);
    ProjectReference projActualRef1 = proCache.getProjectReference (444);
    assertEquals (testPro1, projActualRef1.getProjectId ());
    assertEquals (new Integer (444), projActualRef1.getProjectRefId ());

  }

  @Test
  public void testGetReferences ()
  {
    // Test the getReferences API
    testPro1 = proCache.getProjectId (TEST_PRO1);
    List<Integer> refList = proCache.getReferences (testPro1);
    assertEquals (2, refList.size ());
  }

  @Test
  public void testDeleteReferences ()
  {
    /**
     * Test the Delete References API
     */
    testPro1 = proCache.getProjectId (TEST_PRO1);
    List<Integer> refList = proCache.getReferences (testPro1);
    assertEquals (2, refList.size ());

    proCache.deleteReferences (TEST_PRO1);
    List<Integer> refActualList = proCache.getReferences (testPro1);
    assertEquals (0, refActualList.size ());

  }

  @Test
  public void testWriteAndLoad () throws Exception
  {
    /**
     * Test the Writing of the ProjectCache to the file system and reading it back from the file system to the caches.
     */
    proCache.clear ();
    testPro1 = proCache.addProject (TEST_PRO1);

    ElementLocation location1 = new ElementLocation (111, 1234);
    ProjectReference projRef1 = new ProjectReference (444, testPro1, location1);

    ElementLocation location2 = new ElementLocation (222, 890);
    ProjectReference projRef2 = new ProjectReference (555, testPro1, location2);

    proCache.addProjectReference (projRef1);
    proCache.addProjectReference (projRef2);

    proCache.write ();
    proCache.clear ();
    proCache.load ();

    Integer testId = proCache.getProjectId (TEST_PRO1);
    assertEquals (testPro1, testId);
    List<Integer> refList = proCache.getReferences (testPro1);
    assertEquals (2, refList.size ());

    ProjectReference projActualRef1 = proCache.getProjectReference (444);
    ProjectReference projActualRef2 = proCache.getProjectReference (555);

    assertEquals (testPro1, projActualRef1.getProjectId ());
    assertEquals (testPro1, projActualRef2.getProjectId ());
    assertEquals (new Integer (444), projActualRef1.getProjectRefId ());
    assertEquals (new Integer (555), projActualRef2.getProjectRefId ());

  }

}
