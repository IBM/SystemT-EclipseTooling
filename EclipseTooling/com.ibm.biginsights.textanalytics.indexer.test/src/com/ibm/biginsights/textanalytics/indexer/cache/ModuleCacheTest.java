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
import static org.junit.Assert.assertNotSame;

import java.util.Set;

import org.junit.Test;

import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.model.ModuleReference;

public class ModuleCacheTest
{
  private static final String MOD_RENAMED1 = "modRenamed1";//$NON-NLS-1$
  private static final String TEST_PRO_RENAMED = "TestProRenamed";//$NON-NLS-1$
  private static final String MOD2 = "mod2";//$NON-NLS-1$
  private static final String MOD1 = "mod1";//$NON-NLS-1$
  private static final String TEST_PRO = "TestPro";//$NON-NLS-1$

  ModuleCache cache = ModuleCache.getInstance ();

  @Test
  public void testGetModuleId ()
  {
    /**
     * Test the retrieval of Module ID
     */
    Integer expectedModId = cache.getModuleId (TEST_PRO, MOD1);
    Integer actualModId = cache.getModuleId (TEST_PRO, MOD1);
    assertEquals (expectedModId, actualModId);
  }

  @Test
  public void testRemoveModule ()
  {
    /**
     * Test removing module details from cache
     */
    Integer modId = cache.getModuleId (TEST_PRO, MOD1);
    cache.removeModule (TEST_PRO, MOD1);
    Integer id = cache.getModuleId (TEST_PRO, MOD1);
    assertNotSame (modId, id);
  }

  @Test
  public void testRemoveAllModules ()
  {
    // Remove all modules for a project
    Integer modId = cache.getModuleId (TEST_PRO, MOD2);
    cache.removeAllModules (TEST_PRO);
    Integer id = cache.getModuleId (TEST_PRO, MOD2);
    assertNotSame (modId, id);

  }

  @Test
  public void testUpdateProjectName ()
  {
    // Updating the project name
    Integer modId = cache.getModuleId (TEST_PRO, MOD1);
    cache.updateProjectName (TEST_PRO, TEST_PRO_RENAMED);
    Integer id = cache.getModuleId (TEST_PRO_RENAMED, MOD1);
    assertEquals (modId, id);

  }

  @Test
  public void testUpdateModuleName ()
  {
    // Update module name
    Integer modId = cache.getModuleId (TEST_PRO, MOD1);
    cache.updateModuleName (TEST_PRO, MOD1, MOD_RENAMED1);
    Integer id = cache.getModuleId (TEST_PRO, MOD_RENAMED1);
    assertEquals (modId, id);

  }

  @Test
  public void testUpdateModuleName1 ()
  {
    // Update module name
    cache.clear ();
    Integer modId = cache.getModuleId (TEST_PRO, MOD1);
    Integer modIdRen = cache.getModuleId (TEST_PRO, MOD_RENAMED1);
    cache.updateModuleName (TEST_PRO, MOD1, MOD_RENAMED1);
    Integer id = cache.getModuleId (TEST_PRO, MOD1);
    Integer idRenamed = cache.getModuleId (TEST_PRO, MOD_RENAMED1);

    assertEquals (modIdRen, idRenamed);
    assertNotSame (modId, id);

  }

  @Test
  public void testAddModuleReference ()
  {
    // Test the insertion to module reference
    cache.clear ();
    Integer modId = cache.getModuleId (TEST_PRO, MOD1);

    ElementLocation location1 = new ElementLocation (111, 1234);
    ModuleReference moduleRef1 = new ModuleReference (444, modId, location1);

    ElementLocation location2 = new ElementLocation (222, 890);
    ModuleReference moduleRef2 = new ModuleReference (555, modId, location2);

    cache.addModuleReference (moduleRef1);
    cache.addModuleReference (moduleRef2);

    Set<Integer> refList = cache.getReferences (modId);
    assertEquals (2, refList.size ());

    ModuleReference modActualRef1 = cache.getModuleReference (444);
    ModuleReference modActualRef2 = cache.getModuleReference (555);

    assertEquals (modId, modActualRef1.getModuleId ());
    assertEquals (modId, modActualRef2.getModuleId ());
    assertEquals (new Integer (444), modActualRef1.getModuleRefId ());
    assertEquals (new Integer (555), modActualRef2.getModuleRefId ());
  }

  @Test
  public void testGetModuleReference ()
  {
    // Test the retrieval of Module Reference
    Integer modId = cache.getModuleId (TEST_PRO, MOD1);
    ModuleReference modActualRef1 = cache.getModuleReference (444);
    assertEquals (modId, modActualRef1.getModuleId ());
    assertEquals (new Integer (444), modActualRef1.getModuleRefId ());
  }

  @Test
  public void testGetReferences ()
  {
    // Get the references
    Integer modId = cache.getModuleId (TEST_PRO, MOD1);
    Set<Integer> refList = cache.getReferences (modId);
    assertEquals (2, refList.size ());

  }

  @Test
  public void testWriteAndLoad () throws Exception
  {
    // Write the data to the file system and load the same to teh cache
    cache.clear ();
    Integer modId = cache.getModuleId (TEST_PRO, MOD1);

    ElementLocation location1 = new ElementLocation (111, 1234);
    ModuleReference moduleRef1 = new ModuleReference (444, modId, location1);

    ElementLocation location2 = new ElementLocation (222, 890);
    ModuleReference moduleRef2 = new ModuleReference (555, modId, location2);

    cache.addModuleReference (moduleRef1);
    cache.addModuleReference (moduleRef2);

    cache.write ();
    cache.clear ();
    cache.load ();

    Set<Integer> refList = cache.getReferences (modId);
    assertEquals (2, refList.size ());

    ModuleReference modActualRef1 = cache.getModuleReference (444);
    ModuleReference modActualRef2 = cache.getModuleReference (555);

    assertEquals (modId, modActualRef1.getModuleId ());
    assertEquals (modId, modActualRef2.getModuleId ());
    assertEquals (new Integer (444), modActualRef1.getModuleRefId ());
    assertEquals (new Integer (555), modActualRef2.getModuleRefId ());

  }

  @Test
  public void testDeleteReferencesInFile ()
  {
    // test deletion of references.
    cache.clear ();
    Integer modId = cache.getModuleId (TEST_PRO, MOD1);

    ElementLocation location1 = new ElementLocation (111, 1234);
    ModuleReference moduleRef1 = new ModuleReference (444, modId, location1);

    ElementLocation location2 = new ElementLocation (111, 890);
    ModuleReference moduleRef2 = new ModuleReference (555, modId, location2);

    cache.addModuleReference (moduleRef1);
    cache.addModuleReference (moduleRef2);

    cache.deleteReferencesInFile (111);

    Set<Integer> refList = cache.getReferences (modId);
    assertEquals (0, refList.size ());

  }

}
