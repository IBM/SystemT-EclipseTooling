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
package com.ibm.biginsights.textanalytics.aql.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 *  Babbar
 * 
 */
public class AQLLibrary implements IAQLLibrary {


  
  private static HashMap<String, AQLProject> library = new HashMap<String, AQLProject>();

	//used to storing searchpath hascode, no repeating of searchpath parsing if file is opened from same searchpath
	private static ArrayList<Integer> parsedPath = null;
	
	private static AQLLibrary libInstance = null;
	
	private AQLModel aqlModel = null;
	
	private AQLLibrary () {
	}
	
	public static synchronized AQLLibrary getInstance() {
	  if (libInstance == null) {
	    libInstance = new AQLLibrary();
	    libInstance.aqlModel = new AQLModel(libInstance);
	  }
	  return libInstance;
	}
	
	public HashMap<String, AQLProject> getLibraryMap() {
		return library;
	}
	
	@Override
	public void addParsedPath(Integer pp) {
		if(parsedPath == null)
		{
			parsedPath = new ArrayList<Integer>();
			parsedPath.add(pp);
		}
		else
		{
			parsedPath.add(pp);
		}
	}	
	
	@Override
	public ArrayList<Integer> getParsedPath() {
		return parsedPath;
	}

	@Override
	public List<AQLElement> getDictionaries(String filePath) {
		List<AQLElement> aqlDictionaries = new ArrayList<AQLElement>();
		List<AQLElement> aqlElements = null;
		String projectName;
		Iterator<String> it = library.keySet().iterator();
        while (it.hasNext()) {
            projectName = it.next();
            AQLProject aqlProject = library.get(projectName);
            List<AQLFile> aqlFile = aqlProject.getAQLFiles();
            for (int i = 0; i < aqlFile.size(); i++) {
            	if (aqlFile.get(i).filePath.equals(filePath)) {
            		// got the file, not get elements for this file and filter list
            		//to views and return
            		aqlElements = aqlFile.get(i).getAQLElements();
            		if(aqlElements !=null)
            		{
            		for (int j = 0; j < aqlElements.size(); j++) {
            			AQLElement a = aqlElements.get(j);
            			if (a.type.equals("DICTIONARY")) {
            				aqlDictionaries.add(a);
            			}
            		}
            		}
            		return aqlDictionaries;
            	}
            }		
        }
		return null;
	}

	@Override
	public List<AQLElement> getExternalViews(String filePath) {
		List<AQLElement> aqlExtViews = new ArrayList<AQLElement>();
		List<AQLElement> aqlElements = null;
		String projectName;
		Iterator<String> it = library.keySet().iterator();
        while (it.hasNext()) {
            projectName = it.next();
            AQLProject aqlProject = library.get(projectName);
            List<AQLFile> aqlFile = aqlProject.getAQLFiles();
            for (int i = 0; i < aqlFile.size(); i++) {
            	if (aqlFile.get(i).filePath.equals(filePath)) {
            		// got the file, not get elements for this file and filter list
            		//to views and return
            		aqlElements = aqlFile.get(i).getAQLElements();
            		if(aqlElements !=null)
            		{
            		for (int j = 0; j < aqlElements.size(); j++) {
            			AQLElement a = aqlElements.get(j);
            			if (a.type.equals("EXTERNAL_VIEW")) {
            				aqlExtViews.add(a);
            			}
            		}
            		}
            		return aqlExtViews;
            	}
            }		
        }
		return null;
	}

	@Override
	public List<AQLElement> getFunctions(String filePath) {
		List<AQLElement> aqlFunctions = new ArrayList<AQLElement>();
		List<AQLElement> aqlElements = null;
		String projectName;
		Iterator<String> it = library.keySet().iterator();
        while (it.hasNext()) {
            projectName = it.next();
            AQLProject aqlProject = library.get(projectName);
            List<AQLFile> aqlFile = aqlProject.getAQLFiles();
            for (int i = 0; i < aqlFile.size(); i++) {
            	if (aqlFile.get(i).filePath.equals(filePath)) {
            		// got the file, not get elements for this file and filter list
            		//to views and return
            		aqlElements = aqlFile.get(i).getAQLElements();
            		if(aqlElements !=null)
            		{
            		for (int j = 0; j < aqlElements.size(); j++) {
            			AQLElement a = aqlElements.get(i);
            			if (a.type.equals("FUNCTION")) {
            				aqlFunctions.add(a);
            			}
            		}
            		}
            		return aqlFunctions;
            	}
            }		
        }
		return null;
	}

	@Override
	public List<AQLElement> getIncludedFiles(String filePath) {
		List<AQLElement> aqlIncludedFiles = new ArrayList<AQLElement>();
		List<AQLElement> aqlElements = null;
		String projectName;
		Iterator<String> it = library.keySet().iterator();
        while (it.hasNext()) {
            projectName = it.next();
            AQLProject aqlProject = library.get(projectName);
            List<AQLFile> aqlFile = aqlProject.getAQLFiles();
            for (int i = 0; i < aqlFile.size(); i++) {
            	if (aqlFile.get(i).filePath.equals(filePath)) {
            		// got the file, not get elements for this file and filter list
            		//to views and return
            		aqlElements = aqlFile.get(i).getAQLElements();
            		if(aqlElements !=null)
            		{
            		for (int j = 0; j < aqlElements.size(); j++) {
            			AQLElement a = aqlElements.get(j);
            			if (a.type.equals("INCLUDE")) {
            				aqlIncludedFiles.add(a);
            			}
            		}
            		}
            		return aqlIncludedFiles;
            	}
            }		
        }
		return null;
	}

	@Override
	public List<AQLElement> getSelects(String filePath) {
		List<AQLElement> aqlSelects = new ArrayList<AQLElement>();
		List<AQLElement> aqlElements = null;
		String projectName;
		Iterator<String> it = library.keySet().iterator();
        while (it.hasNext()) {
            projectName = it.next();
            AQLProject aqlProject = library.get(projectName);
            List<AQLFile> aqlFile = aqlProject.getAQLFiles();
            for (int i = 0; i < aqlFile.size(); i++) {
            	if (aqlFile.get(i).filePath.equals(filePath)) {
            		// got the file, not get elements for this file and filter list
            		//to views and return
            		aqlElements = aqlFile.get(i).getAQLElements();
            		if(aqlElements !=null)
            		{
            		for (int j = 0; j < aqlElements.size(); j++) {
            			AQLElement a = aqlElements.get(j);
            			if (a.type.equals("SELECT")) {
            				aqlSelects.add(a);
            			}
            		}
            		}
            		return aqlSelects;
            	}
            }		
        }
		return null;
	}

	@Override
	public List<AQLElement> getTables(String filePath) {
		List<AQLElement> aqlTables = new ArrayList<AQLElement>();
		List<AQLElement> aqlElements = null;
		String projectName;
		Iterator<String> it = library.keySet().iterator();
        while (it.hasNext()) {
            projectName = it.next();
            AQLProject aqlProject = library.get(projectName);
            List<AQLFile> aqlFile = aqlProject.getAQLFiles();
            for (int i = 0; i < aqlFile.size(); i++) {
            	if (aqlFile.get(i).filePath.equals(filePath)) {
            		// got the file, not get elements for this file and filter list
            		//to views and return            		
            		aqlElements = aqlFile.get(i).getAQLElements();
            		if(aqlElements !=null)
            		{
            		for (int j = 0; j < aqlElements.size(); j++) {
            			AQLElement a = aqlElements.get(j);
            			if (a.type.equals("TABLE")) {
            				aqlTables.add(a);
            			}
            		}
            		}
            		return aqlTables;
            	}
            }		
        }
		return null;
	}

	@Override
	public List<AQLElement> getViews(String filePath) {
		List<AQLElement> aqlViews = new ArrayList<AQLElement>();
		List<AQLElement> aqlElements = null;
		String projectName;
		Iterator<String> it = library.keySet().iterator();
        while (it.hasNext()) {
            projectName = it.next();
            AQLProject aqlProject = library.get(projectName);
            List<AQLFile> aqlFile = aqlProject.getAQLFiles();
            for (int i = 0; i < aqlFile.size(); i++) {
            	if (aqlFile.get(i).filePath.equals(filePath)) {
            		// got the file, not get elements for this file and filter list
            		//to views and return
            		//System.out.println("printing: " + filePath);
            		aqlElements = aqlFile.get(i).getAQLElements();
            		if(aqlElements !=null)
            		{
            			for (int j = 0; j < aqlElements.size(); j++) {
            				AQLElement a = aqlElements.get(j);
            				if (a.type.equals("VIEW")) {
            					aqlViews.add(a);
            				}
            			}
            		}
            		return aqlViews;
            	}
            }		
        }
		return null;
	}

	@Override
	public List<AQLElement> getElements(String filePath) {
		List<AQLElement> aqlElements;
		String projectName;
		Iterator<String> it = library.keySet().iterator();
        while (it.hasNext()) {
            projectName = it.next();
            AQLProject aqlProject = library.get(projectName);
            List<AQLFile> aqlFile = aqlProject.getAQLFiles();
            for (int i = 0; i < aqlFile.size(); i++) {
              if (aqlFile.get(i).filePath.equals(filePath)) {
                // got the file, not get elements for this file
                aqlElements = aqlFile.get(i).getAQLElements();
                return aqlElements;
              }
            }   
        }
		return null;
	}

	@Override
	public List<AQLElement> getViews(List<String> searchPath) {

		List<AQLElement> allElements = new ArrayList<AQLElement>();
		for (int i = 0; i < searchPath.size(); i++) {
			String path = searchPath.get(i);
			if((getViews(path) != null))
			allElements.addAll(getViews(path));
		}
		return allElements;
	}

	@Override
	public List<AQLElement> getTables(List<String> searchPath) {

		List<AQLElement> allElements = new ArrayList<AQLElement>();
		for (int i = 0; i < searchPath.size(); i++) {
			String path = searchPath.get(i);
			if((getTables(path) != null))
			allElements.addAll(getTables(path));
		}
		return allElements;
	}

	@Override
	public List<AQLElement> getDictionaries(List<String> searchPath) {

		List<AQLElement> allElements = new ArrayList<AQLElement>();
		for (int i = 0; i < searchPath.size(); i++) {
			String path = searchPath.get(i);
			if((getDictionaries(path) != null))
			allElements.addAll(getDictionaries(path));
		}
		return allElements;
	}

	@Override
	public List<AQLElement> getSelects(List<String> searchPath) {

		List<AQLElement> allElements = new ArrayList<AQLElement>();
		for (int i = 0; i < searchPath.size(); i++) {
			String path = searchPath.get(i);
			if((getSelects(path) != null))
			allElements.addAll(getSelects(path));
		}
		return allElements;
	}

	@Override
	public List<AQLElement> getFunctions(List<String> searchPath) {

		List<AQLElement> allElements = new ArrayList<AQLElement>();
		for (int i = 0; i < searchPath.size(); i++) {
			String path = searchPath.get(i);
			if((getFunctions(path) != null))
			allElements.addAll(getFunctions(path));
		}
		return allElements;
	}

	@Override
	public List<AQLElement> getExternalViews(List<String> searchPath) {

		List<AQLElement> allElements = new ArrayList<AQLElement>();
		for (int i = 0; i < searchPath.size(); i++) {
			String path = searchPath.get(i);
			if((getExternalViews(path) != null))
			allElements.addAll(getExternalViews(path));
		}
		return allElements;
	}

	@Override
	public List<AQLElement> getIncludedFiles(List<String> searchPath) {

		List<AQLElement> allElements = new ArrayList<AQLElement>();
		for (int i = 0; i < searchPath.size(); i++) {
			String path = searchPath.get(i);
			if((getIncludedFiles(path) != null))
			allElements.addAll(getIncludedFiles(path));
		}
		return allElements;
	}

	@Override
	public List<AQLElement> getElements(List<String> searchPath) {

		List<AQLElement> allElements = new ArrayList<AQLElement>();
		for (int i = 0; i < searchPath.size(); i++) {
			String path = searchPath.get(i);
			//System.out.println("path is: " + path + " " + getElements(path));
			if((getElements(path) != null))
			allElements.addAll(getElements(path));
			
		}
		return allElements;
	}

	@Override
	public AQLElement getElement(List<String> searchPath, String elementName) {
		List<AQLElement> allElements = getElements(searchPath);
		Iterator<AQLElement> iterator1 = allElements.iterator();
		while(iterator1.hasNext())
      	{
      		AQLElement temp =iterator1.next();
      		if(temp.name.equals(elementName))
      		{
      			return temp;
      		}
      	}
		return null;
	}
	
	@Override
	public AQLElement getElement(String filePath, String elementName) {
		List<AQLElement> allElements = getElements(filePath);
		Iterator<AQLElement> iterator1 = allElements.iterator();
		while(iterator1.hasNext())
      	{
      		AQLElement temp =iterator1.next();
      		if(temp.name.equals(elementName))
      		{
      			return temp;
      		}
      	}
		return null;
	}

	@Override
	public List<String> getElementsThatThisElementDependsOn(String element, String filePath) {
		List<AQLElement> allElements = getElements(filePath);
		Iterator<AQLElement> iterator1 = allElements.iterator();
		while(iterator1.hasNext())
      	{
      		AQLElement temp =iterator1.next();
      		if(temp.name.equals(element))
      		{
      			return temp.dependsOnElement;
      		}
      	}
		return null;
	}

	@Override
	public List<String> getElementsThatThisElementDependsOn(String elementName,List<String> searchPath) {
		List<AQLElement> allElements = getElements(searchPath);
		Iterator<AQLElement> iterator1 = allElements.iterator();
		while(iterator1.hasNext())
      	{
      		AQLElement temp =iterator1.next();
      		if(temp.name.equals(elementName))
      		{
      			return temp.dependsOnElement;
      		}
      	}
		return null; //PersonPhone - Person & Phone
	}
	
	@Override
	public List<String> getElementsThatDependOnThis(String elementName,List<String> searchPath) {
		List<AQLElement> allElements = getElements(searchPath);
		Iterator<AQLElement> iterator1 = allElements.iterator();
		List<String> parents = new ArrayList<String>();
		while(iterator1.hasNext())
      	{
      		AQLElement temp =iterator1.next();
      		ArrayList<String> childs= temp.dependsOnElement;
      		if(childs == null)
      			continue;
    		Iterator<String> iterator2 = childs.iterator();
    		while(iterator2.hasNext())
    		{
    			String child =iterator2.next();
    			if(child.equals(elementName))
    			{
    				parents.add(temp.getName());
    				break;
    			}
    		}
      	}
		return parents; // Return PersonPhoneFinal, Final2, Final3 - Give me what depends on PersonPhone
	}

  @Override
  public HashMap<String, AQLModuleProject> getModuleLibraryMap ()
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<String> getImportedModules (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getImportedViews (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getImportedDictionaries (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getImportedTables (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getImportedFunctions (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExportedViews (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExportedDictionaries (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExportedTables (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExportedFunctions (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getRequireDocSchema (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExternalTables (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExternalDictionaries (String filePath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<String> getImportedModules (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getImportedViews (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getImportedDictionaries (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getImportedTables (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getImportedFunctions (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExportedViews (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExportedDictionaries (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExportedTables (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExportedFunctions (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getRequireDocSchema (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExternalTables (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }

  @Override
  public List<AQLElement> getExternalDictionaries (List<String> searchPath)
  {
    // This returns null for non modular projects..
    return null;
  }
  
  @Override
  public Object getAQLModel ()
  {
    return this.aqlModel;
  }
	
}
