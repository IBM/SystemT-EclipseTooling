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
package com.ibm.biginsights.textanalytics.goldstandard.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ibm.biginsights.textanalytics.goldstandard.model.AnnotationType;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 *  Krishnamurthy
 *
 */
public class GoldStandardUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		IFolder folder = getGSParentFolder().getFolder("lc2");
		if(folder.exists()){
			folder.delete(true, null);
		}
		
		folder = getGSParentFolder().getFolder("lc3");
		if(folder.exists()){
			folder.delete(true, null);
		}
		
		folder = getGSParentFolder().getFolder("lc4");
		if(folder.exists()){
			folder.delete(true, null);
		}
		
		folder = getGSParentFolder().getFolder("lc5");
		if(folder.exists()){
			folder.delete(true, null);
		}		
	}
	
	
	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#splitAnnotationTypes(java.lang.String)}.
	 */
	@Test
	public void testSplitAnnotationTypes() {
		
		//TEST 1: test with null
		AnnotationType[] annTypes0 = GoldStandardUtil.splitAnnotationTypes(null);
		assertNotNull("Expected zero length array", annTypes0);
		assertEquals("Expected zero length array", 0, annTypes0.length);
		
		//TEST 2: test with empty string
		AnnotationType[] annTypes1 = GoldStandardUtil.splitAnnotationTypes("");
		assertNotNull("Expected zero length array", annTypes1);
		assertEquals("Expected zero length array", 0, annTypes1.length);

		//TEST 3: test with proper string
		String strAnnotationTypes2 = "{PersonSimple,person,true,0};{PhoneNumber,num,false,1};{PhoneNumber,lc,true,2};";
		AnnotationType[] annTypes2 = GoldStandardUtil.splitAnnotationTypes(strAnnotationTypes2);
		assertNotNull(annTypes2);
		assertEquals("Incorrect length of annotationTypes array after splitting annotationTypes", 3, annTypes2.length);
		assertEquals("Incorrect AnnotationType attribute","PersonSimple",  annTypes2[0].getViewName());
		assertEquals("Incorrect AnnotationType attribute", "person", annTypes2[0].getFieldName());
		assertEquals("Incorrect AnnotationType attribute", true, annTypes2[0].isEnabled());
		assertEquals("Incorrect AnnotationType attribute", 0, annTypes2[0].getShortcutKey());
		
		assertEquals("Incorrect AnnotationType attribute","PhoneNumber",  annTypes2[1].getViewName());
		assertEquals("Incorrect AnnotationType attribute", "num", annTypes2[1].getFieldName());
		assertEquals("Incorrect AnnotationType attribute", false, annTypes2[1].isEnabled());
		assertEquals("Incorrect AnnotationType attribute", 1, annTypes2[1].getShortcutKey());
		
		assertEquals("Incorrect AnnotationType attribute","PhoneNumber",  annTypes2[2].getViewName());
		assertEquals("Incorrect AnnotationType attribute", "lc", annTypes2[2].getFieldName());
		assertEquals("Incorrect AnnotationType attribute", true, annTypes2[2].isEnabled());
		assertEquals("Incorrect AnnotationType attribute", 2, annTypes2[2].getShortcutKey());
		
		//TEST 4: test with empty string with blanks
		AnnotationType[] annTypes3 = GoldStandardUtil.splitAnnotationTypes("    ");
		assertNotNull("Expected zero length array", annTypes3);
		assertEquals("Expected zero length array", 0, annTypes3.length);
		
		//TEST 5: test with invalid string literal
		AnnotationType[] annTypes4 = GoldStandardUtil.splitAnnotationTypes(" abcde; efg=hij; klmn=pqrst;   ");
		assertNotNull("Expected zero length array", annTypes4);
		assertEquals("Expected zero length array", 0, annTypes4.length);
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#getAnnotationTypes(org.eclipse.jface.preference.PreferenceStore, java.lang.String)}.
	 */
	@Test
	public void testGetAnnotationTypesPreferenceStoreString() {
		//TEST 1 : positive test
		PreferenceStore prefStore = GoldStandardUtil.getPreferenceStore("PhoneBook", "lc1");
		AnnotationType[] annTypes = GoldStandardUtil.getAnnotationTypes(prefStore, "lc1");
		testAnnotationTypes(annTypes);
		
		//TEST 2: Negative test: Pass null perfStore
		annTypes = GoldStandardUtil.getAnnotationTypes(null, "lc1");
		assertNotNull(annTypes);
		assertEquals(0, annTypes.length);
		
		//TEST 3: Negative test: Pass null gsName
		annTypes = GoldStandardUtil.getAnnotationTypes(prefStore, null);
		assertNotNull(annTypes);
		assertEquals(0, annTypes.length);
		
		//TEST 4: Negative test: Pass invalid gsName
		annTypes = GoldStandardUtil.getAnnotationTypes(prefStore, null);
		assertNotNull(annTypes);
		assertEquals(0, annTypes.length);
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#getAnnotationTypesByProjectName(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetAnnotationTypesStringString() {
		//TEST 1: Positive test
		AnnotationType[] annTypes = GoldStandardUtil.getAnnotationTypesByProjectName("PhoneBook", "lc1");
		assertNotNull(annTypes);
		testAnnotationTypes(annTypes);
		
		//Test 2: Negative test. Pass null for project name
		annTypes = GoldStandardUtil.getAnnotationTypesByProjectName(null, "lc1");
		assertNotNull(annTypes);
		assertEquals(0, annTypes.length);
		
		//Test 3: Negative test. Pass null for gsName
		annTypes = GoldStandardUtil.getAnnotationTypesByProjectName("PhoneBook", null);
		assertNotNull(annTypes);
		assertEquals(0, annTypes.length);
		
		//Test 4: Negative test. Pass invalid value for gsName
		annTypes = GoldStandardUtil.getAnnotationTypesByProjectName("PhoneBook", "qwertyasdfghj");
		assertNotNull(annTypes);
		assertEquals(0, annTypes.length);
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#getAnnotationTypes(org.eclipse.core.resources.IFolder)}.
	 */
	@Test
	public void testGetAnnotationTypesIFolder() {
		IFolder folder = getGSParentFolder().getFolder("lc1");
		AnnotationType[] annTypes = GoldStandardUtil.getAnnotationTypes(folder);
		testAnnotationTypes(annTypes);
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#getGoldStandardFolder(org.eclipse.core.resources.IFile)}.
	 */
	@Test
	public void testGetGoldStandardFolder() {
		IFile file = getGSParentFolder().getFolder("lc1").getFile("00144.lc");
		IFolder folder = GoldStandardUtil.getGoldStandardFolder(file);
		assertEquals("lc1", folder.getName());
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#createDefaultGSFolder(org.eclipse.core.resources.IFolder, org.eclipse.core.runtime.IProgressMonitor)}.
	 * @throws CoreException 
	 */
	@Test
	public void testCreateDefaultGSFolder() throws CoreException {
		
		IFolder gsParentDir = getGSParentFolder();
		
		IFolder newGSFolder = GoldStandardUtil.createDefaultGSFolder(gsParentDir, null);
		assertEquals("lc2", newGSFolder.getName());
		
		newGSFolder = GoldStandardUtil.createDefaultGSFolder(gsParentDir, null);
		assertEquals("lc3", newGSFolder.getName());
		
		newGSFolder = GoldStandardUtil.createDefaultGSFolder(gsParentDir, null);
		assertEquals("lc4", newGSFolder.getName());
		
		newGSFolder = GoldStandardUtil.createDefaultGSFolder(gsParentDir, null);
		assertEquals("lc5", newGSFolder.getName());
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#getGSPrefStoreFile(org.eclipse.core.resources.IFolder)}.
	 */
	@Test
	public void testGetGSPrefStoreFile() {
		File file = GoldStandardUtil.getGSPrefStoreFile(getGSParentFolder().getFolder("lc1"));
		assertEquals("lc.prefs", file.getName());
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#getGSPrefStore(org.eclipse.core.resources.IFolder)}.
	 */
	@Test
	public void testGetGSPrefStore() {
		PreferenceStore prefStore = GoldStandardUtil.getGSPrefStore(getGSParentFolder().getFolder("lc1"));
		validatePrefStoreContents(prefStore);
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#getPreferenceStore(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetPreferenceStore() {
		PreferenceStore prefStore = GoldStandardUtil.getPreferenceStore("PhoneBook", "lc1");
		validatePrefStoreContents(prefStore);
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#getFieldIndex(java.lang.String[], java.lang.String)}.
	 */
	@Test
	public void testGetFieldIndex() {
		String[] fields = {"zero", "one", "two", "three", "four", "five"};
		assertEquals(0, GoldStandardUtil.getFieldIndex(fields, "zero"));
		assertEquals(3, GoldStandardUtil.getFieldIndex(fields, "three"));
		assertEquals(5, GoldStandardUtil.getFieldIndex(fields, "five"));
		assertEquals(-1, GoldStandardUtil.getFieldIndex(fields, "six"));
		assertEquals(-1, GoldStandardUtil.getFieldIndex(fields, "7"));
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#isGoldStandardFolder(org.eclipse.core.resources.IFolder)}.
	 */
	@Test
	public void testIsGoldStandardFolder() {
		assertEquals(false, GoldStandardUtil.isGoldStandardFolder(getGSParentFolder()));
		assertEquals(true, GoldStandardUtil.isGoldStandardFolder(getGSParentFolder().getFolder("lc1")));
		assertEquals(false, GoldStandardUtil.isGoldStandardFolder(ProjectUtils.getProject("PhoneBook").getFolder("result")));
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#isGoldStandardParentDir(org.eclipse.core.resources.IFolder)}.
	 */
	@Test
	public void testIsGoldStandardParentDir() {
		assertEquals(true, GoldStandardUtil.isGoldStandardParentDir(getGSParentFolder()));
		assertEquals(false, GoldStandardUtil.isGoldStandardParentDir(getGSParentFolder().getFolder("lc1")));
		assertEquals(false, GoldStandardUtil.isGoldStandardParentDir(ProjectUtils.getProject("PhoneBook").getFolder("result")));
	}

	/**
	 * Test method for {@link com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil#getGSParentFolder(org.eclipse.core.resources.IProject)}.
	 */
	@Test
	public void testFindGSParentFolder() {
		IFolder actual = GoldStandardUtil.getGSParentFolder(ProjectUtils.getProject("PhoneBook"), true);
		IFolder expected = getGSParentFolder();
		assertEquals(expected.getName(), actual.getName());
	}
	
	private void testAnnotationTypes(AnnotationType[] annTypes){
		assertEquals("Incorrect length of annotationTypes array after splitting annotationTypes", 3, annTypes.length);
		assertEquals("Incorrect AnnotationType attribute","PersonSimple",  annTypes[0].getViewName());
		assertEquals("Incorrect AnnotationType attribute", "person", annTypes[0].getFieldName());
		assertEquals("Incorrect AnnotationType attribute", true, annTypes[0].isEnabled());
		assertEquals("Incorrect AnnotationType attribute", 0, annTypes[0].getShortcutKey());
		
		assertEquals("Incorrect AnnotationType attribute","PhoneNumber",  annTypes[1].getViewName());
		assertEquals("Incorrect AnnotationType attribute", "num", annTypes[1].getFieldName());
		assertEquals("Incorrect AnnotationType attribute", true, annTypes[1].isEnabled());
		assertEquals("Incorrect AnnotationType attribute", 1, annTypes[1].getShortcutKey());
		
		assertEquals("Incorrect AnnotationType attribute","PhoneNumber",  annTypes[2].getViewName());
		assertEquals("Incorrect AnnotationType attribute", "lc", annTypes[2].getFieldName());
		assertEquals("Incorrect AnnotationType attribute", true, annTypes[2].isEnabled());
		assertEquals("Incorrect AnnotationType attribute", 2, annTypes[2].getShortcutKey());
	}
	
	private IFolder getGSParentFolder(){
		return ProjectUtils.getProject("PhoneBook").getFolder("labeledCollections");
	}
	
	private void validatePrefStoreContents(PreferenceStore prefStore){
		assertEquals("en", prefStore.getString(Constants.GS_LANGUAGE));
		assertEquals(true, prefStore.getBoolean(Constants.GS_DETECT_WORD_BOUNDARIES));
		assertEquals("{PersonSimple,person,true,0};{PhoneNumber,num,true,1};{PhoneNumber,lc,true,2};", prefStore.getString(Constants.GS_ANNOTATION_TYPES));
	}

}
