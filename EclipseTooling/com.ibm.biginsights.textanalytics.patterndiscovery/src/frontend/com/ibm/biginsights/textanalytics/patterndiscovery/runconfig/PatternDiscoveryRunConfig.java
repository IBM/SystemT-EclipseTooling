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
package com.ibm.biginsights.textanalytics.patterndiscovery.runconfig;

import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.InternalMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.patterndiscovery.properties.PropertiesContainer;

/**
 * defines the run configuration for pd
 * 
 * 
 */
public class PatternDiscoveryRunConfig
{

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  PropertiesContainer propsContainer;

  public PatternDiscoveryRunConfig (ILaunchConfiguration config) throws TextAnalyticsException, CoreException
  {
    TokenizerConfig lwTokenizerConfig = null;

    String projectName = config.getAttribute ("projectName", "");
    if (!projectName.isEmpty ())
      lwTokenizerConfig = ProjectPreferencesUtil.getTokenizerConfig (projectName);
    else
      throw new TextAnalyticsException (Messages.PROJECT_EMPTY);

    Properties props = new Properties ();

    SystemTPatternDiscoveryTabGroup.copyLaunchConfig2Properties (config, props);

    // we need to handle some properties here
    props.setProperty (InternalMessages.DOCUMENT_COLLECTION_DIR_PROP,
      ProjectPreferencesUtil.getAbsolutePath (props.getProperty (InternalMessages.DOCUMENT_COLLECTION_DIR_PROP)));

    propsContainer = new PropertiesContainer (props, lwTokenizerConfig);
  }

  public PropertiesContainer getPropsContainer ()
  {
    return propsContainer;
  }

}
