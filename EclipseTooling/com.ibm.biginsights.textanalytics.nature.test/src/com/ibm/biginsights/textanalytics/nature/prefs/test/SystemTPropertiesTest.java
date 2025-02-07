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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;

public class SystemTPropertiesTest {

	@Test
	public void testRemoveWorkspacePrefix() {
		/** Test 1: Positive test case to verify if isWorkspaceResource() works fine */
		SystemTProperties props = new SystemTProperties(
				"PhoneBook",false,null, null, true, "[W]mainAQL", "searchPath", null, null, 0, null, null, false,1);
		assertTrue(ProjectPreferencesUtil.isWorkspaceResource(props.getMainAQLFile()));
		assertFalse(ProjectPreferencesUtil.isWorkspaceResource(props.getSearchPath()));
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		/** Test 2: Remove workspace prefix and verify if the resources are marked as non-workspace*/
		props.removeWorkspacePrefix();
		assertFalse(ProjectPreferencesUtil.isWorkspaceResource(props.getMainAQLFile()));
		assertFalse(ProjectPreferencesUtil.isWorkspaceResource(props.getSearchPath()));
	}

	@Test
	public void testClone() {
		/** Test 1: Create a SystemTProperties object, clone it and verify if both are equal */
		SystemTProperties original = new SystemTProperties(
				"PhoneBook",false,null, null, true, "mainAQL", "searchPath", null, null, 0, null, null, false,1);
		SystemTProperties cloned = null;
		try {
			cloned = (SystemTProperties) original.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(original.equals(cloned));
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		/** Test 2: modify the cloned object and verify that it is different from original */
		cloned.setMainAQLFile("newMainAQLFile");
		assertFalse(original.equals(cloned));
	}

	@Test
	public void testEqualsObject() {
		/** Test 1: Create two identical objects and verify if equals() returns true */
		SystemTProperties p1 = new SystemTProperties(
			"PhoneBook",false,null, null, true, "mainAQL",  "searchPath", null, null, 0, null, null, false,1);

		SystemTProperties p2 = new SystemTProperties(
				"PhoneBook",false,null, null, true, "mainAQL",  "searchPath", null, null, 0, null, null ,false,1);

		assertTrue(p1.equals(p2));
		
		////////////////////////////////////////////////////////////////////////////////////////////////////
		/** Test 2: Create almost identical object - except for enableProvenance */
		SystemTProperties almostIdentical = new SystemTProperties(
				"PhoneBook",false,null, null, false, "mainAQL",  "searchPath", null, null, 0, null, null, false,1);
		assertFalse(p1.equals(almostIdentical));
		
		////////////////////////////////////////////////////////////////////////////////////////////////////		
		/** Test 3: Create almost identical object - except for mainAQL */
		almostIdentical =  new SystemTProperties(
				"PhoneBook",false,null, null, true, "newMainAQL",  "searchPath", null, null, 0, null, null, false,1);
		assertFalse(p1.equals(almostIdentical));
		
		////////////////////////////////////////////////////////////////////////////////////////////////////		
		/** Test 4: Create almost identical object - except for searchPath */
		almostIdentical = new SystemTProperties(
				"PhoneBook",false,null, null, true, "mainAQL",  "newSearchPath", null, null, 0, null, null, false,1);
		assertFalse(p1.equals(almostIdentical));
				
	}

}
