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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class SelectionListener implements ISelectionChangedListener {


  
  private final CellMouseListener cml;
  private final CellKeyListener ckl;
  
  public SelectionListener(CellMouseListener cml, CellKeyListener ckl) {
    super();
    this.cml = cml;
    this.ckl = ckl;
  }

  @Override
  public void selectionChanged(SelectionChangedEvent event) {
    this.cml.setSelection(event.getSelection());
    this.ckl.setCellMouseListener (cml);
  }

}
