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

import org.junit.Assert;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.CommonModelProvider;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;

public class LoadByCommonSignatureTest extends PDJobBasedTest
{

  public LoadByCommonSignatureTest (String expected_path, String actual_path, ExperimentProperties properties)
  {
    super (expected_path, actual_path, properties);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void initLogger ()
  {

  }

  @Override
  public void runTest () throws Exception
  {
    job = new PatternDiscoveryJob ("test pattern discovery", properties, 1, 2, 10, true);
    job.loadRange ();

    String jsequence0 = "{be reached;at}";
    String jsequence1 = "{can;reached;be reached;at}";

    CommonModelProvider model = new CommonModelProvider (jsequence0, job);
    model.getTable (jsequence0);
    Assert.assertTrue (model.getContexts ().size () == 1);

    model.getTable (jsequence1);
    Assert.assertTrue (model.getContexts ().size () == 2);
    Assert.assertTrue (model.getContexts ().get (0).getContextString ().equals ("can be reached at her cell"));

    shutdown ();
  }

}
