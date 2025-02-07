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

import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig.Standard;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;
import com.ibm.biginsights.textanalytics.patterndiscovery.properties.PropertiesContainer;
import com.ibm.biginsights.textanalytics.patterndiscovery.util.DbNames;

public abstract class PDJobBasedTest extends DbBasedTest
{

  protected PropertiesContainer properties;
  protected PatternDiscoveryJob job;

  public PDJobBasedTest (String expected_path, String actual_path, ExperimentProperties properties)
  {
    super (expected_path, actual_path, DbNames.AOM_FOLDER_NAME);
    setupJobProps (properties);
  }

  protected void setupJobProps (ExperimentProperties props)
  {
    properties = new PropertiesContainer (props, (Standard) props.getTokenizerConfig ());
    properties.setProperty (PDConstants.PD_LANGUAGE_PROP, props.getLanguage ().toString ());

    if (props.getAogFile () != null)
      properties.setProperty (PDConstants.PD_AOG_PATH_PROP, props.getAogFile ().getAbsolutePath ());

    properties.setProperty (
      PropertyConstants.DOCUMENT_COLLECTION_DIR,
      props.getProperty (PropertyConstants.DOCUMENT_COLLECTION_DIR)
        + props.getProperty (PropertyConstants.INPUT_DOCUMENT_NAME));
  }
}
