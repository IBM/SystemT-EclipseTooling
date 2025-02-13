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
package com.ibm.biginsights.textanalytics.nature.utils;

/**
 * A listener interface with call back method to notify the implementing listener about the completion of module
 * metadata load operation
 * 
 * 
 */
public interface ModuleLoadListener
{

public static final String _COPYRIGHT = "Copyright IBM\n"+
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
   * intimates the registered listener that the metadata of a given set of modules were loaded by the
   * ModuleMetadataLoader
   * 
   * @param moduleNames list of modules that were loaded by the ModuleMetadataLoader
   * @throws Exception while load operation notification
   */
  public void moduleLoaded (String[] moduleNames) throws Exception;

  /**
   * notifies the registered listeners that the metadata of a given set of modules were loaded by the
   * ModuleMetadataLoader
   * 
   * @param moduleToUnload module name of the module that is unloaded
   * @throws Exception while removing entry in the cache
   */
  public void moduleUnLoaded (String moduleToUnload) throws Exception;
}
