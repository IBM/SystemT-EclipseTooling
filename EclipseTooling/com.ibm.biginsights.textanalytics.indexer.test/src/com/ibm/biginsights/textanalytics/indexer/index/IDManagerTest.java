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

import static org.junit.Assert.*;

import org.junit.Test;

import com.ibm.biginsights.textanalytics.indexer.Constants;

public class IDManagerTest
{
  private static final String TEST_AQL = "test.aql";//$NON-NLS-1$

  private static final String MODULE = "module";//$NON-NLS-1$

  private static final String TEST_PRO = "testPro";//$NON-NLS-1$

  IDManager idMgr = IDManager.getInstance ();

  // Test the createQualifiedKey API
  private static String expected = TEST_PRO + Constants.QUALIFIED_NAME_SEPARATOR + MODULE
    + Constants.QUALIFIED_NAME_SEPARATOR + TEST_AQL;

  @Test
  public void testGenerateNextSequenceId ()
  {
    // Test the generateNextSequenceId API
    int expected = idMgr.generateNextSequenceId () + 1;
    Integer actual = idMgr.generateNextSequenceId ();
    assertEquals (expected, actual.intValue ());
  }

  @Test
  public void testCreateQualifiedKey ()
  {
    String actual = idMgr.createQualifiedKey (TEST_PRO, MODULE, TEST_AQL);
    assertEquals (expected, actual);
  }

  @Test
  public void testWriteAndLoad () throws Exception
  {
    // Test the write & load API
    int expected = idMgr.generateNextSequenceId ().intValue () + 2;
    idMgr.generateNextSequenceId ();
    idMgr.write ();
    idMgr.load ();
    Integer actual = idMgr.generateNextSequenceId ();
    assertEquals (expected, actual.intValue ());

  }

}
