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

public interface PatternDiscoveryTest
{
  final String ENCODING = "UTF-8";

  public void runTest () throws Exception;

  public void test_compare_method () throws Exception;

  public void test (String sql) throws Exception;

  public void test (String path1, String path2, int[] cols_compare) throws Exception;

  public void shutdown ();

  public void initLogger ();
}
