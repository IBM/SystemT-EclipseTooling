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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import com.ibm.biginsights.textanalytics.launch.ExternalDictionaryTab;
import com.ibm.biginsights.textanalytics.launch.ExternalTablesTab;
import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;

/**
 * defines the tab group used by the pd run config wizard
 * 
 */
@SuppressWarnings("restriction")
public class SystemTPatternDiscoveryTabGroup extends AbstractLaunchConfigurationTabGroup
{

	@SuppressWarnings("unused")

 
	Properties properties;

  public SystemTPatternDiscoveryTabGroup ()
  {
    properties = new Properties();
  }

  @Override
  public void createTabs (ILaunchConfigurationDialog dialog, String mode)
  {
    ExternalTablesTab extTablesTab = new ExternalTablesTab () {
      protected String getProjectName (ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(PDConstants.PD_PROJECT_NAME_PROP, ""); //$NON-NLS-1$
      }
      protected String getHelpId () {
        return "com.ibm.biginsights.textanalytics.tooling.help.pattern_discovery";   //$NON-NLS-1$
      }
    };

    ExternalDictionaryTab extDictionariesTab = new ExternalDictionaryTab () {
      protected String getProjectName (ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(PDConstants.PD_PROJECT_NAME_PROP, ""); //$NON-NLS-1$
      }
      protected String getHelpId () {
        return "com.ibm.biginsights.textanalytics.tooling.help.pattern_discovery";   //$NON-NLS-1$
      }
    };

    ILaunchConfigurationTab[] tabs =
      new ILaunchConfigurationTab[] { new SystemTPatternDiscoveryMainTab (this),
                                      extTablesTab, extDictionariesTab,
                                      new AdvancedPatternDiscoveryConfigTab(this, (LaunchConfigurationsDialog)dialog),
                                      new EnvironmentTab(),
                                      new CommonTab()
                                    };

    setTabs (tabs);
  }

  public Properties getProperties ()
  {
    return properties;
  }

  /*-------------------------------------------------------------------------------------------------------------+
   | The following 2 methods are for copying pattern discovery specific objects only; do not use them elsewhere. |
   +-------------------------------------------------------------------------------------------------------------*/

  public static void copyLaunchConfig2Properties (ILaunchConfiguration config, Properties props)
  {
    try {
      for (Object keyObj : config.getAttributes ().keySet ()) {
        if ( ! (keyObj instanceof String) )
          continue;

        String key = (String)keyObj;

        if ( key.equals (IRunConfigConstants.EXTERNAL_DICT_MAP) ||
             key.equals (IRunConfigConstants.EXTERNAL_TABLES_MAP) )
          props.put (key, config.getAttribute (key, new HashMap<String,String>()));

        else if ( key.equals (IRunConfigConstants.EXT_TABLE_REQ_VAL_LIST) ||
                  key.equals (IRunConfigConstants.EXT_DICT_REQ_VAL_LIST) ||
                  key.equals ("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS")||
                  key.equals ("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES") )
          props.put (key, config.getAttribute (key, new ArrayList<String>()));

        else
          props.setProperty (key, config.getAttribute (key, ""));
      }
    }
    catch (CoreException e) {
      ErrorMessages.LogErrorMessage(ErrorMessages.PATTERN_DISCOVERY_ERROR_GENERATING_UI, e);
    }
  }

  @SuppressWarnings("unchecked")
	public static void copyProperties2LaunchConfig (Properties props, ILaunchConfigurationWorkingCopy config)
  {
    for (Object keyObj : props.keySet ()) {

      if ( ! (keyObj instanceof String) )
        continue;

      String key = (String)keyObj;

      // Do not copy properties to RunConfig for these cases. 'properties' doesn't contain info about
      // external dictionaries and tables, copy will in fact clears those info.
      if ( (key.equals (IRunConfigConstants.EXTERNAL_DICT_MAP) || key.equals (IRunConfigConstants.EXTERNAL_TABLES_MAP) ||
           (key.equals (IRunConfigConstants.EXT_TABLE_REQ_VAL_LIST) || key.equals (IRunConfigConstants.EXT_DICT_REQ_VAL_LIST)) ) )
        continue;

      if ( key.equals ("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS")||
           key.equals ("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES") )
      	config.setAttribute (key, (List<String>)props.get (key));
      else
      	config.setAttribute (key, props.getProperty (key));
    }
    
  }
}
