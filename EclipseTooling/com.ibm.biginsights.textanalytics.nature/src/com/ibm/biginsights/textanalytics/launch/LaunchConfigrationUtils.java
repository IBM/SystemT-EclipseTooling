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
package com.ibm.biginsights.textanalytics.launch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ExternalDictionary;
import com.ibm.biginsights.textanalytics.nature.utils.ExternalTable;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class LaunchConfigrationUtils
{
  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

  /**
   * User may modify AQL code and make some external dictionaries/tables obsolete.
   * This method removes the redundant and adds new elements from launch configuration.
   * @param config The LaunchConfigurationWorkingCopy object
   * @param extDicts Currently required dictionaries
   * @param extTables Currently required tables
   */
  public static boolean removeRedundantsAddNews (ILaunchConfigurationWorkingCopy config,
                                        List<ExternalDictionary> extDicts,
                                        List<ExternalTable> extTables)
  {
    //----- dictionary -----//
    List<String> extDictNames = new ArrayList<String> ();
    for (ExternalDictionary extDict : extDicts) {
      extDictNames.add (extDict.getDictName ());
    }
    boolean dictMapUpdated = updateExternalElements ((ILaunchConfigurationWorkingCopy)config,
                                                     extDictNames,
                                                     IRunConfigConstants.EXTERNAL_DICT_MAP);

    //----- table -----//
    List<String> extTableNames = new ArrayList<String> ();
    for (ExternalTable extTable : extTables) {
      extTableNames.add (extTable.getTableName ());
    }
    boolean tableMapUpdated = updateExternalElements ((ILaunchConfigurationWorkingCopy)config,
                                                      extTableNames,
                                                      IRunConfigConstants.EXTERNAL_TABLES_MAP);

    return (dictMapUpdated || tableMapUpdated);
  }

  /**
   * User may modify AQL code and make some external dictionaries/tables obsolete.
   * This method removes the redundant and adds new elements from launch configuration.
   * @param config The LaunchConfigurationWorkingCopy object
   * @param currentElements Currently required dictionaries/tables
   * @param keyToGetMap IRunConfigConstants.EXTERNAL_DICT_MAP or IRunConfigConstants.EXTERNAL_TABLES_MAP
   * @return TRUE if there exists difference and config is updated; FALSE otherwiese.
   */
  private static boolean updateExternalElements (ILaunchConfigurationWorkingCopy config,
                                          List<String> currentElements,
                                          String keyToGetMap)
  {
    boolean updated = false;

    try {
      List<String> newElements = new ArrayList<String> (currentElements);

      // Get external elements in config
      @SuppressWarnings("unchecked")
      Map<String, String> extObjectMap = config.getAttribute (keyToGetMap, new LinkedHashMap<String, String> ());

      // Create a copy of extObjectMap which is actually the one inside config.
      Map<String, String> extObjectMap2 = new LinkedHashMap<String, String> ();
      for (String key : extObjectMap.keySet ()) {
        extObjectMap2.put (key, extObjectMap.get (key));
      }

      //-------- add new elements to the map (so we can update config later)
      // new required elements that were not in old configuration
      newElements.removeAll (extObjectMap.keySet ());

      if (newElements.isEmpty () == false) {
        for (String elemName : newElements) {
          extObjectMap2.put (elemName, "");
        }
        updated = true;
      }

      //-------- remove redundant external elements from configuration
      // redundant elements that were in old configuration and not needed now.
      List<String> extraConfigExtObjNames = new ArrayList<String> (extObjectMap.keySet ());
      extraConfigExtObjNames.removeAll (currentElements);

      if (extraConfigExtObjNames.isEmpty () == false) {
        for (String elemName : extraConfigExtObjNames) {
          extObjectMap2.remove (elemName);
        }
        updated = true;
      }

      // Update config with the new external object map. 
      if (updated)
        config.setAttribute (keyToGetMap, extObjectMap2);
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
        Messages.getString ("ExternalEntTab.LAUNCH_CONFIG_PARAM_ERROR"), e); //$NON-NLS-1$
    }

    return updated;
  }

}
