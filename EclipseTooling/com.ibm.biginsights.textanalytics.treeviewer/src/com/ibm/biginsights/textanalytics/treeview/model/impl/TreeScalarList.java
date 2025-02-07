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
package com.ibm.biginsights.textanalytics.treeview.model.impl;

import java.util.ArrayList;
import java.util.List;

import com.ibm.biginsights.textanalytics.treeview.model.IScalarList;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.TreeObjectType;

public class TreeScalarList implements IScalarList {


  
  private final TreeObjectType type;
  
  List<ITreeObject> list = new ArrayList<ITreeObject>();
  
  public TreeScalarList(TreeObjectType type) {
    super();
    this.type = type;
  }

  @Override
  public TreeObjectType getType() {
    return this.type;
  }

  @Override
  public List<ITreeObject> getList() {
    return this.list;
  }
  
  public void addElement(ITreeObject element) {
    this.list.add(element);
  }

}
