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
package com.ibm.datatools.quick.launch.ui.internal.pref;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import com.ibm.datatools.quick.launch.ui.Activator;

/** Used to set defaults for the quick launch preferences. Intended to be used internally by quick launch
* 
* @since 2010May05
*/
public class QuickLaunchPreferenceInitializer extends AbstractPreferenceInitializer
{



    @Override
    public void initializeDefaultPreferences()
    {
        Preferences prefs = getPreferences();
        prefs.setDefault( QuickLaunchPreferences.QUERY_CHANGE_PERSPECTIVE, false );
        prefs.setDefault( QuickLaunchPreferences.CHANGE_PERSPECTIVE, true );
       //prefs.setDefault( QuickLaunchPreferences.QUERY_SHOW_HELP, false );
        prefs.setDefault( QuickLaunchPreferences.SHOW_HELP, true );
        prefs.setDefault( QuickLaunchPreferences.QUERY_ON_CLOSE, false );
        prefs.setDefault( QuickLaunchPreferences.SHOW_HELP_WINDOW, false );
        prefs.setDefault( QuickLaunchPreferences.SHOW_TASKLAUNCHER, true);
    }

    private Preferences getPreferences()
    {
        return Activator.getDefault().getPluginPreferences();
    }


}
