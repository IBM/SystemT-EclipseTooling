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

import org.eclipse.core.resources.IFile;

import com.ibm.biginsights.textanalytics.tableview.model.CellType;
import com.ibm.biginsights.textanalytics.tableview.model.ICellElement;
import com.ibm.biginsights.textanalytics.tableview.model.IScalarList;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public abstract class AbstractCellElement implements ICellElement {



  private final CellType type;

  public AbstractCellElement(CellType type) {
    super();
    this.type = type;
  }

  @Override
  public CellType getType() {
    return this.type;
  }

  @Override
  public int getStart() {
    return Constants.SPAN_UNDEFINED_OFFSET;
  }

  @Override
  public int getEnd() {
    return Constants.SPAN_UNDEFINED_OFFSET;
  }

  @Override
  public String getText() {
    return null;
  }

  @Override
  public float getFloat() {
    return -1.0f;
  }

  @Override
  public int getInt() {
    return -1;
  }

  @Override
  public IScalarList getList() {
    return null;
  }

  @Override
  public boolean getBoolean() {
    return false;
  }
  
  @Override
  public IFile getFile() {
    return null;
  }

}
