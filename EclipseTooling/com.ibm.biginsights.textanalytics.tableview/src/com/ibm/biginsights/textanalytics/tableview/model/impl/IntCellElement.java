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

import com.ibm.biginsights.textanalytics.tableview.model.CellType;

public class IntCellElement extends AbstractCellElement {


  
  private final int i;
  
  public IntCellElement(int i) {
    super(CellType.INT);
    this.i = i;
  }
  
  @Override
  public int getInt() {
    return this.i;
  }

  @Override
  public String toString() {
    return Integer.toString(this.i);
  }
}
