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
package com.ibm.biginsights.textanalytics.tableview.control;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.biginsights.textanalytics.tableview.model.IAQLTableViewModel;
import com.ibm.biginsights.textanalytics.tableview.model.impl.AQLTableViewModel;

public class CellContentProvider implements IStructuredContentProvider {



  IAQLTableViewModel model = new AQLTableViewModel("AQL Default Result View", null, new String[] {}); //$NON-NLS-1$

  @Override
  public void dispose() {
    this.model = null;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    if (newInput == null) {
      return;
    }
    TableViewer tv = (TableViewer) viewer;
    tv.getTable().setRedraw(false);
    if (newInput instanceof IAQLTableViewModel) {
      this.model = (IAQLTableViewModel) newInput;
    }
    tv.refresh();
    tv.getTable().setRedraw(true);
  }

  @Override
  public Object[] getElements(Object inputElement) {
    return this.model.getElements();
  }

}
