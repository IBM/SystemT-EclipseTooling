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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.junit.Before;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;

public class FileDirectoryPickerTest {

	Composite composite;
	
	@Before
	public void setUp() throws Exception {
		IWorkbenchWindow window = ProjectPreferencesUtil.getActiveWorkbenchWindow();
		composite = window.getShell();
	}
//	@After
//	public void tearDown() throws Exception {
//	}


	@Test
	public void testSetFileDirValue() {
		FileDirectoryPicker fileDirPicker = new FileDirectoryPicker(composite);
		TextModifyListener listener = new TextModifyListener();
		fileDirPicker.addModifyListenerForFileDirTextField(listener);
		
		String origValue = "PhoneBook/aql/personPhone-simple.aql";
		String prefixedValue = "[W]PhoneBook/aql/personPhone-simple.aql";
		
		fileDirPicker.setFileDirValue(origValue, true);
		
		assertEquals("Listener does not receive correct value for text field", origValue, listener.value);
		assertEquals("workspace prefixed value is not received as expected", prefixedValue, fileDirPicker.getFileDirValue());
	}
	
	
	private class TextModifyListener implements ModifyListener{
		String value;
		
		@Override
		public void modifyText(ModifyEvent event) {
			Text text = (Text) event.getSource();
			value = text.getText();
		}
	}
}
