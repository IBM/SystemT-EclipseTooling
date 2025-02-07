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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchWindow;
import org.junit.Before;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.nature.prefs.DataPathEditor;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;

public class DataPathEditorTest {

	Composite composite;
	
	@Before
	public void setUp() throws Exception {
		IWorkbenchWindow window = ProjectPreferencesUtil.getActiveWorkbenchWindow();
		composite = window.getShell();
	}


	@Test
	public void testClearDataPaths() {
		DataPathEditor editor = new DataPathEditor(composite);
		editor.addDataPath("c:/temp", false);
		editor.addDataPath("PhoneBook/docs", true);
		
		editor.clearDataPaths();
		String dataPath = editor.getDataPath();
		assertEquals("Datapath is not empty even after clearDataPath", "", dataPath);
	}

	@Test
	public void testAddDataPath() {
		DataPathEditor editor = new DataPathEditor(composite);
		editor.addDataPath("c:/temp", false);
		editor.addDataPath("PhoneBook/docs", true);
		
		String dataPath = editor.getDataPath();
		boolean result = dataPath.contains("c:/temp") && dataPath.contains("[W]PhoneBook/docs");
		assertTrue("dataPath does not contain either or both of 'c:/temp' and 'PhoneBook/docs'", result);
	}

	@Test
	public void testPropertyChangeListeners(){
		DataPathEditor editor = new DataPathEditor(composite);
		DataPathChangeListener listener = new DataPathChangeListener();
		editor.addDataPathChangeListener(listener);
		
		//Test 1: Add an external file system location and verify that the listener received proper values
		String oldDataPath = editor.getDataPath();
		editor.addDataPath("c:/temp", false);
		String newDataPath = editor.getDataPath();
		
		assertEquals(oldDataPath, listener.oldValue);
		assertEquals(newDataPath, listener.newValue);
		
		//Test 2: Add a workspace location and verify that the listener received proper values
		oldDataPath = editor.getDataPath();
		editor.addDataPath("PhoneBook/docs", true);
		newDataPath = editor.getDataPath();
		
		assertEquals(oldDataPath, listener.oldValue);
		assertEquals(newDataPath, listener.newValue);		

		//Test 3: clear all datapaths and verify that the listener received proper values
		oldDataPath = editor.getDataPath();
		editor.clearDataPaths();
		newDataPath = editor.getDataPath();
		
		assertEquals(oldDataPath, listener.oldValue);
		assertEquals(newDataPath, listener.newValue);	
	}
	
	private class DataPathChangeListener implements PropertyChangeListener{
		
		protected String oldValue;
		protected String newValue;
		
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			oldValue = (String) event.getOldValue();
			newValue = (String) event.getNewValue();
		}
		
	}
	
}
