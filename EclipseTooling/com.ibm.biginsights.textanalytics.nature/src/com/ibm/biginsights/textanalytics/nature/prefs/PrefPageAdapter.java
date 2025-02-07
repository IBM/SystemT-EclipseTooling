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
/**
 * 
 */
package com.ibm.biginsights.textanalytics.nature.prefs;

import java.util.HashMap;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * PrefPageAdapter is the common base class for all preference pages.
 * This class provides implmentations for certain abstract methods inherited from IPrefPage interfaces
 * 
 * 
 *
 */
public abstract class PrefPageAdapter implements IPrefPage {


	
	protected PreferenceStore preferenceStore;
	protected SystemTProjectPreferences projectPreferences;
	
	protected HashMap<String, String> oldValues = new HashMap<String, String>(); 
		
	protected PrefPageAdapter(SystemTProjectPreferences projectPreferences){
		this.projectPreferences = projectPreferences;
		this.preferenceStore = projectPreferences.getPreferenceStoreCopy();
		if(preferenceStore != null){
			preferenceStore.addPropertyChangeListener(new PreferenceStoreListener());
		}
	}

	public String getDefaultValue(String key) {
		if (preferenceStore == null) 
			{
				return key;
			}
		return preferenceStore.getDefaultString(key);
	}

	public void setValue(String key, String value) {
		if (preferenceStore != null) 
		{
			preferenceStore.setValue(key, value);
		}
	}
	
	public String getOldValue(String key){
		return oldValues.get(key);
	}
	
	public boolean isDirty(String key){
		return oldValues.containsKey(key);
	}
	
	private class PreferenceStoreListener implements IPropertyChangeListener{

		public void propertyChange(PropertyChangeEvent event) {
			String oldValue = String.valueOf(event.getOldValue());
			String propertyName = event.getProperty();
			
			oldValues.put(propertyName, oldValue);
		}
	}
}
