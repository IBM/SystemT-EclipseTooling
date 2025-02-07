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
package com.ibm.biginsights.textanalytics.aqleditor.parser.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import com.ibm.avatar.aql.AQLParser;
import com.ibm.avatar.aql.StatementList;
import com.ibm.biginsights.textanalytics.aql.editor.ui.*;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.AQLModel;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;


/**
 *  Babbar
 * 
 */

public class ParserTest {

	String searchPath = "";
	ArrayList<String> searchPathList;
	
	private IAQLLibrary aqlLibrary;
	private AQLModel model;
	private StatementList statementList;
	private ReconcilingStrategy rs;
	private HashSet<String> fileList;
	private AQLParser parser;

	@Before
	public void setUp() throws Exception {
		IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
		IProject project = workspaceRoot.getProject("PhoneBook");
		SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties(project);
		searchPath = ProjectPreferencesUtil.getAbsolutePath(properties.getSearchPath());
	}

	@Test
	public void checkSearchPath() {
		assertFalse(searchPath == null);
	}
	
	@Test
	public void testParseResponse() throws IOException {
		rs = new ReconcilingStrategy();
		searchPathList = new ArrayList<String>();
		fileList = new HashSet<String>();
		searchPathList = rs.getSearchPathList(searchPath);
		Iterator<String> iterator1 = searchPathList.iterator();
		while(iterator1.hasNext())
	    {
	    	String temp =iterator1.next();
	    	IPath path = new Path(temp).makeAbsolute(); 
			new FileTraversal() {
			public void onFile( final File f ) {
				if(f.getName().endsWith(".aql"))
					{
						fileList.add(f.getAbsolutePath().toString());
					}
				}
			}.traverse(new File(path.toOSString()));
		}
		Iterator<String> iterator2 = fileList.iterator();
	    while(iterator2.hasNext())
	    {	      	
	     		String aqlFilePath = iterator2.next();
	     	
				parser = new AQLParser(rs.readFileAsString(aqlFilePath),aqlFilePath);
		   		statementList = parser.parse();     
		    	String file = new File(aqlFilePath).getName(); 
		   		//3 files with no parse errors and correct no. of parse nodes
		   		if(file.equals("phone-simple.aql"))
		   		{
		   			assertEquals(statementList.getParseTreeNodes().size(), 1);
		   			assertEquals(statementList.getParseErrors().size(), 0);
		   		}
		   		else if(file.equals("personPhone-simple.aql"))
		   		{
		   			assertEquals(statementList.getParseTreeNodes().size(), 4);
		   			assertEquals(statementList.getParseErrors().size(), 0);
		   		}
		   		else if(file.equals("person-simple.aql"))
		   		{
		   			assertEquals(statementList.getParseTreeNodes().size(), 21);
		   			assertEquals(statementList.getParseErrors().size(), 0);
		   		}
		    		
	    }
	    Activator.getLibrary().addParsedPath(searchPath.hashCode());
	}
	
	@Test
	public void testAQLLibrary() throws IOException {
		rs = new ReconcilingStrategy();
		aqlLibrary = Activator.getLibrary();
		model = new AQLModel(aqlLibrary);
		fileList = new HashSet<String>();
		searchPathList = rs.getSearchPathList(searchPath);
		Iterator<String> iterator1 = searchPathList.iterator();

		while(iterator1.hasNext())
	    {
	    	String temp =iterator1.next();
	    	IPath path = new Path(temp).makeAbsolute(); 
			new FileTraversal() {
			public void onFile( final File f ) {
				if(f.getName().endsWith(".aql"))
					{
						fileList.add(f.getAbsolutePath().toString());
					}
				}
			}.traverse(new File(path.toOSString()));
		}
		
		Iterator<String> iterator2 = fileList.iterator();
		while(iterator2.hasNext())
	    {	      	
				String aqlFilePath = iterator2.next();
				parser = new AQLParser(rs.readFileAsString(aqlFilePath),aqlFilePath);
				statementList = parser.parse(); 
	      		String prjName = rs.getProjectName(aqlFilePath.toString());
	     		model.create(aqlFilePath, prjName, statementList);
	     		
	     		//now try to get this from aqllibrary API
	     		
	     		List<AQLElement> elements = aqlLibrary.getElements(aqlFilePath);
		    	String file = new File(aqlFilePath).getName(); 

	     		if(file.equals("phone-simple.aql"))
		   		{			   		
		   			assertEquals(elements.size(), 1);
		   		}
		   		else if(file.equals("personPhone-simple.aql"))
		   		{			   	
		   			assertEquals(elements.size(), 4);
		   		}
		   		else if(file.equals("person-simple.aql"))
		   		{
		   			assertEquals(elements.size(), 21);
		   		}
	    }
	}
}

class FileTraversal {
	public final void traverse(final File f) throws IOException {
      if (f.isDirectory()) {
              onDirectory(f);
              final File[] childs = f.listFiles();
              for( File child : childs ) {
                      traverse(child);
              }
              return;
      }
      onFile(f);   
	}
	public void onDirectory( final File d ) {
    }
	public void onFile(final File f) {
	}
}
