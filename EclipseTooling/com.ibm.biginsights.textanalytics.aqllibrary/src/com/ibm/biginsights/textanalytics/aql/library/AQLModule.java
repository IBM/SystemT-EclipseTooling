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
import java.util.LinkedList;
import java.util.List;

/*
 * This AQLModule class is used to hold the Module related information of
 * AQLProject.
 */
public class AQLModule{


  
  private List<AQLFile> aqlFiles;
  private List<AQLModule> importModules;  // To hold the modules which holds the imported modules information..
  private String moduleName;
private String comment;
  
  public List<AQLFile> getAQLFiles() {
    return aqlFiles;
  }
  
  public List<String> getAqlFilePaths(){
    List<String> ret = new LinkedList<String>();
    for(AQLFile file : aqlFiles){
      ret.add(file.filePath);
    }
    return ret;
  }

  public void deleteAllFiles() {
    aqlFiles.clear();
  }

  public void addFile(AQLFile file) {
    //System.out.println(aqlFiles);
    if(aqlFiles == null)
    {
      aqlFiles = new ArrayList<AQLFile>();
    }
    aqlFiles.add(file);   
  }
  
  public void deleteAQLFile(AQLFile file)
  {
    if(aqlFiles != null)
    {
      aqlFiles.remove(file);
    }   
  }
  
  public void setComment(String comment) {
	  this.comment = comment;
  }

  public String getComment() {
	  return this.comment;
  }
  public void setModuleName (String moduleName)
  {
    this.moduleName = moduleName;
  }

  public String getModuleName ()
  {
    return moduleName;
  }
  
}
