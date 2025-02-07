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

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.ibm.biginsights.textanalytics.tableview.Messages;
import com.ibm.biginsights.textanalytics.tableview.model.IRow;

public class CellLabelProvider extends LabelProvider implements ITableLabelProvider {



  @Override
  public Image getColumnImage(Object element, int columnIndex) {
    return null;
  }

  @Override
  public String getColumnText(Object element, int columnIndex) {
    if (!(element instanceof IRow)) {
      return Messages.getString("CellLabelProvider.ERR_INTERNAL_ERROR"); //$NON-NLS-1$
    }
    IRow row = (IRow) element;
    return row.getLabelForCell(columnIndex);
  }

}
