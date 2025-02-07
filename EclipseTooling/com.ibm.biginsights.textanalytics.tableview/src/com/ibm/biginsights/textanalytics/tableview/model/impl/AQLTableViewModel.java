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

import com.ibm.biginsights.textanalytics.tableview.model.IAQLTableViewModel;
import com.ibm.biginsights.textanalytics.tableview.model.IRow;

public class AQLTableViewModel implements IAQLTableViewModel {


  
  private final int numCols;
  
  private final String name;
  
  private final IRow[] table;
  
  private final String[] headers;
  
  public AQLTableViewModel(String name, IRow[] rows, String[] headerNames) {
    super();
    this.name = name;
    this.numCols = headerNames.length;
    this.headers = headerNames;
    this.table = rows;
  }
  
  @Override
  public int getNumColumns() {
    return this.numCols;
  }

  @Override
  public int getNumRows() {
    return this.table.length;
  }

  @Override
  public String[] getHeaders() {
    return this.headers;
  }

  @Override
  public IRow[] getElements() {
    return this.table;
  }
  
  @Override
  public String getName() {
    return this.name;
  }

}
