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
package com.ibm.biginsights.textanalytics.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PreferencesPlugin extends AbstractUIPlugin implements IStartup
{

  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  // The plug-in ID
  public static final String PLUGIN_ID = "com.ibm.biginsights.textanalytics.preferences";

  // The shared instance
  private static PreferencesPlugin plugin;
  private static TextAnalyticsWorkspacePreferences taPreferences = null;

  /**
   * The constructor
   */
  public PreferencesPlugin ()
  {
    super ();
    if (plugin == null) {
      plugin = this;
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start (BundleContext context) throws Exception
  {
    super.start (context);
    if (plugin == null) {
      plugin = this;
    }
    
    IPreferenceStore store = plugin.getPreferenceStore ();
    if (taPreferences == null) { //Initialise workspace level text analytics preferences.
      taPreferences = new TextAnalyticsWorkspacePreferences (store, plugin.getBundle ().getSymbolicName ());
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop (BundleContext context) throws Exception
  {
    plugin = null;
    super.stop (context);
  }

  /**
   * Returns the shared instance
   * 
   * @return the shared instance
   */
  public static PreferencesPlugin getDefault ()
  {
    return plugin;
  }

  /**
   * Get Workspace level text analytics preferences.
   * @return TextAnalyticsWorkspacePreferences object that allows getting and setting text analytics preferences.
   */
  public static TextAnalyticsWorkspacePreferences getTextAnalyticsWorkspacePreferences ()
  {
    return taPreferences;
  }

  @Override
  public void earlyStartup ()
  {
    //Some plugins requiring the preferences may load before the code in this method gets to run.
    //All initialization code moved to start(BundleContext)
  }

}
