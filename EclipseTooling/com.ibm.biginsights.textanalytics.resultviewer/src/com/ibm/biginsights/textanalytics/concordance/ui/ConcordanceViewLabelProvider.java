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

import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry;

public class ConcordanceViewLabelProvider extends LabelProvider implements ITableLabelProvider {



  public Image getColumnImage(Object element, int columnIndex) {
    return null;
  }

  public String getColumnText(Object element, int columnIndex) {
    IConcordanceModelEntry entry = (IConcordanceModelEntry) element;
    switch (columnIndex) {
    case IConcordanceModel.COLUMN_FILE_ID: return entry.getDocId();
    case IConcordanceModel.COLUMN_LEFT_CONTEXT: return entry.getLeftContext();
    case IConcordanceModel.COLUMN_ANNOTATION_TEXT: 
    	{
    		List<Integer> offsets = entry.getOffsets();
    		String returningText = entry.getAnnotationText();
    		if (offsets != null)
    		{
    			returningText = returningText + " ["+ offsets.get(0)+ "-" + offsets.get(1) + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    		}
    		return returningText;
    	}
    case IConcordanceModel.COLUMN_RIGHT_CONTEXT: return entry.getRightContext();
    case IConcordanceModel.COLUMN_ANNOTATION_TYPE: return entry.getAnnotationType();
    default: return ""; //$NON-NLS-1$
    }
  }


}
