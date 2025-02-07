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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 *  Kalakuntla
 * 
 * This class is to hold the modular aql project information...
 */
public class AQLModuleProject {



  private HashMap<String, AQLModule> aqlModules;
  
  public  HashMap<String, AQLModule>getAQLModules() {
    return aqlModules;
  }
  
  public List<String> getAqlModuleFilePaths(){
    List<String> ret = new LinkedList<String>();
    String moduleName;
    Iterator<String> it = aqlModules.keySet().iterator();
    while (it.hasNext()) {
      moduleName = it.next();
       AQLModule aqlModule = aqlModules.get(moduleName);
       List<AQLFile> aqlFiles = aqlModule.getAQLFiles();
       for(AQLFile file : aqlFiles){
         ret.add(file.filePath);
       }
    }
    return ret;
  }

  public void deleteAllModules() {
    aqlModules.clear();
  }

  public void addModule(AQLModule module) {
    //System.out.println(aqlFiles);
    if(aqlModules == null)
    {
      aqlModules = new HashMap<String, AQLModule> ();
    }
    aqlModules.put(module.getModuleName (),module);   
  }
  
  public void deleteAQLModule(AQLModule module)
  {
    if(aqlModules != null)
    {
      aqlModules.remove(module);
    }   
  }
}
