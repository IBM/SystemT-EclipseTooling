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

import java.util.List;

import com.ibm.biginsights.textanalytics.treeview.model.IScalarList;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.TreeObjectType;

public class ScalarListTreeObject extends AbstractTreeObject {


  
  private final IScalarList list;
  
  public ScalarListTreeObject(IScalarList list) {
    super(TreeObjectType.LIST);
    this.list = list;
  }
  
  public IScalarList getList() {
    return this.list;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(100);
    sb.append("Scalar list of " + this.list.getType() + ": [ "); //$NON-NLS-1$ //$NON-NLS-2$
    List<ITreeObject> l = this.list.getList();
    if (l.size() > 0) {
      sb.append(l.get(0).toString());
      for (int i = 1; i < l.size(); i++) {
        sb.append(", "); //$NON-NLS-1$
        sb.append(l.get(i).toString());
      }
    }
    sb.append(" ]"); //$NON-NLS-1$
    return sb.toString();
  }

}
