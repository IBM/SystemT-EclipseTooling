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

import java.util.ArrayList;

/**
 * Utility class that tracks the list of listeners registered for module metadata load events and helps the
 * ModuleMetadataLoader to fire events to registered listeners.
 * 
 * 
 */
public class ModuleLoadListenerSupport
{

  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  private ArrayList<ModuleLoadListener> listeners = new ArrayList<ModuleLoadListener> ();

  /**
   * method to add listener for the load and unload events.
   * 
   * @param listener for capturing the event.
   */
  public synchronized void addListener (ModuleLoadListener listener)
  {
    listeners.add (listener);
  }

  /**
   * method to remove listener for the load and unload events.
   * 
   * @param listener listener for capturing the event.
   */
  public synchronized void removeListener (ModuleLoadListener listener)
  {
    listeners.remove (listener);
  }

  /**
   * Utility to fire an event to all ModuleLoadListeners stating that a given set of modules were loaded
   * 
   * @param moduleNames list of modules loaded
   * @throws Exception While uploading metadata to cache
   */
  public synchronized void fireModuleLoadedEvent (String[] moduleNames) throws Exception
  {

    for (ModuleLoadListener listener : listeners) {
      listener.moduleLoaded (moduleNames);
    }
  }

  /**
   * Utility to fire an event to all ModuleLoadListeners stating that a given module was unloaded
   * 
   * @param moduleToUnload module that was unloaded
   * @throws Exception while removing entries from the metadata cache
   */
  public synchronized void fireModuleUnLoadedEvent (String moduleToUnload) throws Exception
  {
    for (ModuleLoadListener listener : listeners) {
      listener.moduleUnLoaded (moduleToUnload);
    }
  }
}
