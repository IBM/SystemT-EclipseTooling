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
package com.ibm.biginsights.textanalytics.nature.prefs.test;

import static org.junit.Assert.*;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.junit.Before;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProjectPreferences;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public class SystemTProjectPreferencesTest {
	@SuppressWarnings("unused")



	Composite composite;
	
	@Before
	public void setUp() throws Exception {
		IWorkbenchWindow window = ProjectPreferencesUtil.getActiveWorkbenchWindow();
		composite = window.getShell();
		
		TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
		wprefs.setPrefShowAdvancedTab (true);
		wprefs.setPrefShowEnableProvenanceOption (true);
		wprefs.savePreferences ();
	}
 
	@Test
	public void testProjectPreferences() {
		SystemTProjectPreferences preferences = new SystemTProjectPreferences(Constants.CONSUMER_PROPERTY_SHEET, "PhoneBook");
		PreferenceStore pref = preferences.getPreferenceStoreCopy();
		
		boolean provenance = pref.getBoolean(Constants.GENERAL_PROVENANCE);
		//String aogPath = pref.getString(Constants.GENERAL_AOGPATH);
		String datapath = pref.getString(Constants.SEARCHPATH_DATAPATH);
		//String lang = pref.getString(Constants.GENERAL_LANGUAGE);
		String mainaql = pref.getString(Constants.GENERAL_MAINAQLFILE);
		//String resultDir = pref.getString(Constants.GENERAL_RESULTDIR);
		
		assertEquals(true, provenance);
		//assertEquals("[W]/PhoneBook/.aog", aogPath);
		assertEquals("[W]/PhoneBook/aql;[W]/PhoneBook/aql/dict", datapath);
		//assertEquals("en", lang);
		assertEquals("[W]/PhoneBook/aql/personPhone-simple.aql", mainaql);
		//assertEquals("[W]/PhoneBook/output/.result", resultDir);
	}

	@Test
	public void testRestoreToProjectProperties() {
		SystemTProjectPreferences preferences = new SystemTProjectPreferences(Constants.CONSUMER_PROPERTY_SHEET, "PhoneBook");
		preferences.createContents(composite);
		
		SystemTProperties properties = preferences.getProjectProperties();
		properties.setMainAQLFile("");
		preferences.setProjectProperties(properties);
		assertFalse(preferences.isDataValid());
		
		preferences.restoreToProjectProperties("PhoneBook");
		assertTrue(preferences.isDataValid());
	}


	@Test
	public void testIsDataValid() {
		SystemTProjectPreferences preferences = new SystemTProjectPreferences(Constants.CONSUMER_PROPERTY_SHEET, "PhoneBook");
		preferences.createContents(composite);
		
		assertTrue(preferences.isDataValid());
		
		SystemTProperties originalProperties = preferences.getProjectProperties();

		SystemTProperties properties = null;
		
		//Test 1: Set main aql to empty
		properties = getClone(originalProperties);
		properties.setMainAQLFile("");
		preferences.setProjectProperties(properties);
		assertFalse(preferences.isDataValid());

		//Test 2: Set searchpath to empty
		properties = getClone(originalProperties);
		properties.setSearchPath("");
		preferences.setProjectProperties(properties);
		assertFalse(preferences.isDataValid());
		
		//Test 3: Restore proper values
		preferences.setProjectProperties(originalProperties);
		assertTrue(preferences.isDataValid());
	}
	
	private SystemTProperties getClone(SystemTProperties originalProperties){
		SystemTProperties properties = null;
		try {
			properties = (SystemTProperties)originalProperties.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
	}
	
	@Test
	public void testRestoreDefaults(){
		SystemTProjectPreferences preferences = new SystemTProjectPreferences(Constants.CONSUMER_PROPERTY_SHEET, "PhoneBook");
		preferences.createContents(composite);
		
		//Test 0: Verify that the default preference page is valid 
		assertTrue(preferences.isDataValid());

		//Test 1: restore to defaults and verify that prefpage is now valid
		verifyRestoreDefaults(preferences);
		
		//Test 2: corrupt preferenceStore and verify that it becomes invalid
		PreferenceStore prefStore = preferences.getPreferenceStoreCopy();
		prefStore.setValue(Constants.GENERAL_MAINAQLFILE, "");
		preferences.setProjectProperties(
				ProjectPreferencesUtil.createSystemTProperties(prefStore, "PhoneBook"));
		assertFalse(preferences.isDataValid());
		
		//Test 3: restore to defaults and verify that prefpage is now valid
		verifyRestoreDefaults(preferences);
		
		//Test 4: verify if preferencestore is refreshed with proper data after restoreDefaults
		preferences.performApplyAll();
		String mainAQL = preferences.getPreferenceStoreCopy().getString(Constants.GENERAL_MAINAQLFILE);
		assertFalse(mainAQL.trim().length() == 0);
		
		//Test 5: restore to defaults and verify that prefpage is now valid
		verifyRestoreDefaults(preferences);
		//perform apply to restore prefStore to valid values
		preferences.performApplyAll();
		
		//Test 6: corrupt preferenceStore and verify that it becomes invalid
		prefStore = preferences.getPreferenceStoreCopy();
		prefStore.setValue(Constants.SEARCHPATH_DATAPATH, "");
		preferences.setProjectProperties(
				ProjectPreferencesUtil.createSystemTProperties(prefStore, "PhoneBook"));
		assertFalse(preferences.isDataValid());
		
		//Test 7: restore to defaults and verify that prefpage is now valid
		verifyRestoreDefaults(preferences);
		//perform apply to restore prefStore to valid values
		preferences.performApplyAll();
		
		//Test 8: restore to defaults and verify that prefpage is now valid
		verifyRestoreDefaults(preferences);
		//perform apply to restore prefStore to valid values
		preferences.performApplyAll();		
	}
	
	private void verifyRestoreDefaults(SystemTProjectPreferences preferences){
		preferences.getGeneralPrefPage().restoreDefaults();
		assertTrue(preferences.isDataValid());
	}
	
	@Test
	public void testSyncPrefStoreAndProjectProperties() throws Exception{
		SystemTProjectPreferences preferences = new SystemTProjectPreferences(Constants.CONSUMER_PROPERTY_SHEET, "PhoneBook");
		preferences.createContents(composite);
		
		//Test 0: Verify that the default preference page is valid 
		assertTrue(preferences.isDataValid());

		//Test 1: change project properties and verify if prefStore is changed
		
		SystemTProperties properties = preferences.getProjectProperties();
		properties.setSearchPath("abc");
		
		preferences.setProjectProperties(properties);
		preferences.performApplyAll();
		SystemTProperties propsFromPrefStore = ProjectPreferencesUtil.createSystemTProperties(
				preferences.getPreferenceStoreCopy(), "PhoneBook");
		SystemTProperties newProps = preferences.getProjectProperties();

		//Note: ideally we should be invoking assertEquals. But since SystemTProperties.java does not implement hashCode(), assertEquals would not work as expected
		assertTrue(properties.equals(newProps));
		assertTrue(properties.equals(propsFromPrefStore));
		assertTrue(newProps.equals(propsFromPrefStore));
		
		//restore defaults
		verifyRestoreDefaults(preferences);
		//perform apply to restore prefStore to valid values
		preferences.performApplyAll();
		
		//Test 2: change prefStore and verify if project properties is changed
		SystemTProperties propBeforeChanges = (SystemTProperties)preferences.getProjectProperties().clone();
		
		PreferenceStore prefStore = preferences.getPreferenceStoreCopy();
		prefStore.setValue(Constants.GENERAL_MAINAQLFILE, "abc.aql");
		preferences.setProjectProperties(
				ProjectPreferencesUtil.createSystemTProperties(prefStore, "PhoneBook"));
		SystemTProperties propAfterChanges = preferences.getProjectProperties();

		assertFalse(propBeforeChanges.equals(propAfterChanges));
		
		//restore defaults
		preferences.getGeneralPrefPage().restoreDefaults();
		preferences.performApplyAll();
		
		SystemTProperties propAfterRestore = preferences.getProjectProperties();
		assertTrue(propBeforeChanges.equals(propAfterRestore));
		
	}

}
