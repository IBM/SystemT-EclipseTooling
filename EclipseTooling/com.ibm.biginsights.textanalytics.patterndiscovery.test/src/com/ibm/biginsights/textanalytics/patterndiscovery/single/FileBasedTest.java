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
package com.ibm.biginsights.textanalytics.patterndiscovery.single;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import junit.framework.Assert;

import com.ibm.biginsights.textanalytics.patterndiscovery.util.AQLTestUtils;

public abstract class FileBasedTest implements PatternDiscoveryTest
{
  protected Logger logger;
  protected String[] expected_files;
  protected String[] actual_files;
  protected int[][] cols_to_compare;

  public FileBasedTest (String[] expected_files, String[] actual_files, int[][] cols_to_compare)
  {
    this.expected_files = expected_files;
    this.actual_files = actual_files;
    this.cols_to_compare = cols_to_compare;
  }

  @Override
  public void runTest () throws Exception
  {
    for (int i = 0; i < expected_files.length; i++) {
      String expected_path = expected_files[i];
      String actual_path = actual_files[i];
      test (expected_path, actual_path, cols_to_compare[i]);
    }
  }

  /**
   * before we test anything with the compare files method, we first must verify that it actually works. we do so by
   * calling it with the same file twice. we will do this only once here in the first test
   */
  @Override
  public void test_compare_method () throws Exception
  {
    test (expected_files[0], expected_files[0], cols_to_compare[0]);
  }

  @Override
  public void test (String path1, String path2, int[] cols_compare) throws Exception
  {
    File expected = new File (path1);
    File actual = new File (path2);

    BufferedReader expectedReader = new BufferedReader (
      new InputStreamReader (new FileInputStream (expected), ENCODING));
    BufferedReader actualReader = new BufferedReader (new InputStreamReader (new FileInputStream (actual), ENCODING));

    Assert.assertTrue (AQLTestUtils.compareCSVFiles (expectedReader, actualReader, cols_compare));

    expectedReader.close ();
    actualReader.close ();
  }

  @Override
  public void test (String sql) throws Exception
  {
    throw new Exception ("Method Not Used");
  }

  @Override
  public void shutdown ()
  {}

  @Override
  public abstract void initLogger ();
}
