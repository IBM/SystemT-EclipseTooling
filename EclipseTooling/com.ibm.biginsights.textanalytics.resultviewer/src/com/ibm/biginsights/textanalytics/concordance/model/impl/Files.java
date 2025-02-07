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
package com.ibm.biginsights.textanalytics.concordance.model.impl;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ibm.biginsights.textanalytics.concordance.model.IFiles;

public class Files implements IFiles {


  
  // Map types to an active/inactive flag.
  private final SortedMap<String, Boolean> fileMap = new TreeMap<String, Boolean>();
  
  public void put(String type, Boolean value) {
    this.fileMap.put(type, value);
  }

  public Set<String> getActiveFiles() {
    final SortedSet<String> set = new TreeSet<String>();
    for (Map.Entry<String, Boolean> entry : this.fileMap.entrySet()) {
      // Requires that only the Boolean constants are used.
      if (entry.getValue() == TRUE) {
        set.add(entry.getKey());
      }
    }
    return set;
  }

  public Set<String> getFiles() {
    return this.fileMap.keySet();
  }

  public void setActiveFiles(String[] activeFiles) {
    // To set the active types, we first need to set all types to inactive.
    for (String type : getFiles()) {
      this.fileMap.put(type, FALSE);
    }
    for (String type : activeFiles) {
      this.fileMap.put(type, TRUE);
    }
  }

  public boolean isActiveFile(String type) {
    return this.fileMap.get(type) == TRUE;
  }

  public void setAllActive() {
    for (String file : getFiles()) {
      put(file, TRUE);
    }
  }

}
