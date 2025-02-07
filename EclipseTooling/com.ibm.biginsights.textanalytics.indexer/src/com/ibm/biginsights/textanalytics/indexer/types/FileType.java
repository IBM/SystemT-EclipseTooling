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
package com.ibm.biginsights.textanalytics.indexer.types;

public enum FileType
{
  //AQL file
  AQL,
  
  //Extraction plan file
  EPL,
  
  //Launch Config file
  LCG,
  
  //Text Analytics Properties file
  TAP,
  
  // Class Path File
  CLP;


  
  /**
   * Converts String to FileType
   * @param type
   * @return
   */
  public static FileType strToFileType(String type){
    if(type == null){
      return null;
    }else if(type.equals (AQL.toString ())){
      return AQL;
    }else if (type.equals (EPL.toString ())){
      return EPL;
    }else if(type.equals (LCG.toString ())){
      return LCG;
    }else if (type.equals (TAP.toString ())){
      return TAP;
    }else if (type.equals (CLP.toString ())){
      return CLP;
    }else{
      //default
      return null;
    }
  }
}
