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

import java.util.List;

import org.junit.Assert;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.SemanticContext;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.SemanticModelProvider;

public class LoadBySemanticSignatureTest extends PDJobBasedTest
{

  public LoadBySemanticSignatureTest (String expected_path, String actual_path, ExperimentProperties properties)
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

    String context = "can be reached at";
    String jsignature = "{can;reached;be reached;at}";
    String signature  = "{can;reached;be reached;at}";

    SemanticModelProvider model = new SemanticModelProvider (job);
    List<SemanticContext> snippets = model.getSnippets (context, jsignature, signature);

    Assert.assertTrue (snippets.size () == 1);
    shutdown ();
  }

}
