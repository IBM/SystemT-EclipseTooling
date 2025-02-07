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
package com.ibm.biginsights.textanalytics.concordance.ui;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.impl.ConcordanceModel;

public class ConcordanceViewContentProvider implements IStructuredContentProvider {


  
  private IConcordanceModel model = new ConcordanceModel();

  public ConcordanceViewContentProvider() {
    super();
  }

  public Object[] getElements(Object inputElement) {
    return this.model.getEntries();
  }

  public void dispose() {
    this.model = null;
  }

  public void inputChanged(Viewer tv, Object oldInput, Object newInput) {
    if (newInput == null) {
      return;
    }
    TableViewer viewer = (TableViewer) tv;
    viewer.getTable().setRedraw(false);
    if (newInput instanceof IConcordanceModel) {
      this.model = (IConcordanceModel) newInput;
    }
    viewer.refresh();
    viewer.getTable().setRedraw(true);
  }
}
