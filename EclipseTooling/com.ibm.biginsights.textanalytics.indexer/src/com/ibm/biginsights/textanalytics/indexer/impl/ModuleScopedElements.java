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
package com.ibm.biginsights.textanalytics.indexer.impl;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Maintains a list of valid element definitions and imported elements of a given module
 * 
 *  Krishnamurthy
 */
public class ModuleScopedElements
{


 
	/**
   * List of Create dictionary statements for inline & table based dicts
   */
  protected ArrayList<String> inlineAndTableDicts = new ArrayList<String> ();

  /**
   * List of Create dict statments based on dict file
   */
  protected ArrayList<String> dictFileReferences = new ArrayList<String> ();

  protected ArrayList<String> importedDicts = new ArrayList<String> ();

  protected ArrayList<String> importedViews = new ArrayList<String> ();

  protected ArrayList<String> importedTables = new ArrayList<String> ();

  protected ArrayList<String> importedFunctions = new ArrayList<String> ();

  /**
   * List of create function statements
   */
  protected ArrayList<String> functionDefs = new ArrayList<String> ();

  /**
   * List of create view statements
   */
  protected ArrayList<String> viewDefs = new ArrayList<String> ();

  protected ArrayList<String> tableDefs = new ArrayList<String> ();
  
  private static HashMap<String, ModuleScopedElements> instances = new HashMap<String, ModuleScopedElements> ();

  private String moduleName;
  
  public ModuleScopedElements (String moduleName)
  {
    this.moduleName = moduleName;
  }

  public static ModuleScopedElements getInstance (String moduleName)
  {
    ModuleScopedElements instance = instances.get(moduleName);
    if(instance == null){
      instance = new ModuleScopedElements (moduleName);
      instances.put (moduleName, instance);
    }
    return instance;
  }

  public static void clearModule (String moduleName)
  {
    if(instances.containsKey (moduleName)){
      instances.remove (moduleName);
    }
    
  }
}
