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
package com.ibm.biginsights.textanalytics.tableview.model.impl;

import java.util.ArrayList;
import java.util.List;

import com.ibm.biginsights.textanalytics.tableview.model.CellType;
import com.ibm.biginsights.textanalytics.tableview.model.ICellElement;
import com.ibm.biginsights.textanalytics.tableview.model.IScalarList;

public class ScalarList implements IScalarList {


  
  private final CellType type;
  
  List<ICellElement> list = new ArrayList<ICellElement>();
  
  public ScalarList(CellType type) {
    super();
    this.type = type;
  }

  @Override
  public CellType getType() {
    return this.type;
  }

  @Override
  public List<ICellElement> getList() {
    return this.list;
  }
  
  public void addElement(ICellElement element) {
    this.list.add(element);
  }

}
