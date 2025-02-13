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
import java.util.List;


/**
 *  Babbar
 * 
 */
public interface IAQLLibrary {

 public static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";
  
  /**
   * Returns the list of views defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type views
   */
	public List<AQLElement> getViews(String filePath);

  /**
   * Returns the list of dictionaries defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type dictionaries
   */
	public List<AQLElement> getDictionaries(String filePath);

  /**
   * Returns the list of tables defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type tables
   */
	public List<AQLElement> getTables(String filePath);

  /**
   * Returns the list of functions defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type functions
   */
	public List<AQLElement> getFunctions(String filePath);

  /**
   * Returns the list of external views defined in the given AQL file  
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type external views
   */
	public List<AQLElement> getExternalViews(String filePath);

  /**
   * Returns the list of selects defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type selects
   */
	public List<AQLElement> getSelects(String filePath);

  /**
   * Returns the list of included files defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type included files
   */
	public List<AQLElement> getIncludedFiles(String filePath);

  /**
   * Returns the list of AQL elements defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type aql elements
   */
	public List<AQLElement> getElements(String filePath);
	
  /**
   * Returns the list of views defined in the given AQL file that depends on the given view
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type views that depends on the given view
   */
	public List<String> getElementsThatThisElementDependsOn(String view, String filePath);
	
  /**
   * Returns the list of views defined in the given AQL file list that depends on the given view 
   * 
   * @param List of strings representing aql file paths
   * 
   * @return List of AQLElements of type views
   */
	public List<String> getElementsThatThisElementDependsOn(String view, List<String> searchPath);

  /**
   * Returns the Hash map of AQL library instance for non modular projects
   * 
   * @return hash map of project name vs non modular project instances
   */
	public HashMap<String, AQLProject> getLibraryMap();

  /**
   * Returns the Hash map of AQL library instance for modular projects 
   * 
   * @return hash map of project name vs  modular project instances
   */
	public HashMap<String, AQLModuleProject> getModuleLibraryMap();
	
	// Interfaces for new AQL constructs..
  /**
   * Returns the list of string consists imported module names defined in the given AQL file  
   * 
   * @param String representing aql file path 
   * 
   * @return List of string of imported module names
   */
	public List<String> getImportedModules(String filePath);
	
  /**
   * Returns the list of imported views defined in the given AQL file  
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type imported views
   */
	public List<AQLElement> getImportedViews(String filePath);

  /**
   * Returns the list of imported dictionaries in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type imported dictionaries 
   */
  public List<AQLElement> getImportedDictionaries(String filePath);

  /**
   * Returns the list of imported tables in the given AQL file  
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type imported tables 
   */
  public List<AQLElement> getImportedTables(String filePath);

  /**
   * Returns the list of imported functions defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type imported functions
   */
  public List<AQLElement> getImportedFunctions(String filePath);

  /**
   * Returns the list of exported views defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type exported views
   */
  public List<AQLElement> getExportedViews(String filePath);

  /**
   * Returns the list of exported dictionaries defined in the given AQL file  
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type exported dictionaries
   */
  public List<AQLElement> getExportedDictionaries(String filePath);

  /**
   * Returns the list of exported tables defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type exported tables
   */
  public List<AQLElement> getExportedTables(String filePath);

  /**
   * Returns the list of exported functions defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type exported functions
   */
  public List<AQLElement> getExportedFunctions(String filePath);
  
  /**
   * Returns the list of document schemas defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type RequireDocSchema
   */
  public List<AQLElement> getRequireDocSchema(String filePath);
  
  /**
   * Returns the list of external tables defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type external tables
   */
  public List<AQLElement> getExternalTables(String filePath);
  
  /**
   * Returns the list of external dictionaries defined in the given AQL file 
   * 
   * @param String representing aql file path 
   * 
   * @return List of AQLElements of type external dictionaries
   */
  public List<AQLElement> getExternalDictionaries(String filePath);

  /**
   * Returns the list of integers to represent the hash codes of parsed file paths 
   * 
   * @return List of Integers represents hash codes
   */
  public ArrayList<Integer> getParsedPath();
	
  /**
   * It adds the hash code of search path to AQL library 
   * 
   * @param Hash code  
   * 
   */
  public void addParsedPath(Integer pp);

  /**
   * Returns the list of AQL elements defined in the given AQL file list  
   * 
   * @param List of strings representing aql file paths
   * 
   * @return List of AQLElements of type AQL elements
   */
	public List<AQLElement> getElements(List<String> searchPath);

  /**
   * Returns the list of views defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths
   * 
   * @return List of AQLElements of type views
   */
	public List<AQLElement> getViews(List<String> searchPath);
  
	/**
   * Returns the list of functions defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type functions
   */
	public List<AQLElement> getFunctions(List<String> searchPath);
	
	/**
   * Returns the list of selects defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type selects
   */
	public List<AQLElement> getSelects(List<String> searchPath);

	/**
   * Returns the list of tables defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type tables
   */
	public List<AQLElement> getTables(List<String> searchPath);

	/**
   * Returns the list of dictionaries defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type dictionaries
   */
	public List<AQLElement> getDictionaries(List<String> searchPath);

	/**
   * Returns the list of external views defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type external views
   */
	public List<AQLElement> getExternalViews(List<String> searchPath);

	/**
   * Returns the list of included files defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type included files
   */
	public List<AQLElement> getIncludedFiles(List<String> searchPath);
	
	/**
   * Returns the list of strings of imported module names defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of string of imported module names
   */
	public List<String> getImportedModules(List<String> searchPath);
  
	/**
   * Returns the list of imported views defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type imported views
   */
	public List<AQLElement> getImportedViews(List<String> searchPath);

	/**
   * Returns the list of imported dictionaries defined in the given AQL file list  
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type imported dictionaries 
   */
  public List<AQLElement> getImportedDictionaries(List<String> searchPath);

  /**
   * Returns the list of imported tables defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type imported tables 
   */
  public List<AQLElement> getImportedTables(List<String> searchPath);

  /**
   * Returns the list of imported functions defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type imported functions
   */
  public List<AQLElement> getImportedFunctions(List<String> searchPath);
  
  /**
   * Returns the list of exported views defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type exported views
   */
  public List<AQLElement> getExportedViews(List<String> searchPath);

  /**
   * Returns the list of exported dictionaries defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type exported dictionaries
   */
  public List<AQLElement> getExportedDictionaries(List<String> searchPath);
  
  /**
   * Returns the list of exported tables defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type exported tables
   */
  public List<AQLElement> getExportedTables(List<String> searchPath);

  /**
   * Returns the list of exported functions defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type exported functions
   */
  public List<AQLElement> getExportedFunctions(List<String> searchPath);
  
  /**
   * Returns the list of document schemas defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type RequireDocSchema
   */
  public List<AQLElement> getRequireDocSchema(List<String> searchPath);
  
  /**
   * Returns the list of external tables defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type external tables
   */
  public List<AQLElement> getExternalTables(List<String> searchPath);
  
  /**
   * Returns the list of external dictionaries defined in the given AQL file list 
   * 
   * @param List of strings representing aql file paths 
   * 
   * @return List of AQLElements of type external dictionaries
   */
  public List<AQLElement> getExternalDictionaries(List<String> searchPath);
  
  /**
   * Returns the AQL element that matches the given element name from the list of AQL files 
   * 
   * @param list of strings representing aql file paths
   * @param String representing the element name 
   * 
   * @return List of AQLElements of type views
   */
  public AQLElement getElement(List<String> searchPath, String elementName);

  /**
   * Returns the AQL element that matches the given element name from the AQL file 
   * 
   * @param String representing aql file path
   * @param String representing the element name  
   * 
   * @return List of AQLElements of type views
   */
	public AQLElement getElement(String filePath, String elementName);

	List<String> getElementsThatDependOnThis(String elementName, List<String> searchPath);
	/**
   * It returns the aql model 
   */
	public Object getAQLModel();
}
