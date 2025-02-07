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

import java.util.Iterator;

import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.ListVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.tableview.Messages;
import com.ibm.biginsights.textanalytics.tableview.model.IRow;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public class Row implements IRow {



  // Keep the sorting position in the model itself.  This gets around the stupid secondary sort
  // order stuff because we can use a stable sort ourselves.
  private int pos;
  
  final boolean doProvenance;

  final SystemTComputationResult model;

  final OutputViewRow row;

  public String getDocSchemaName(int i) {
    if (i < this.row.fieldValues.length) {
      FieldValue val = this.row.fieldValues[i];
      if (val != null) {
        if (val instanceof SpanVal) {
          SpanVal span = (SpanVal) val;
          return span.parentSpanName;
        }
      }
    }
    return "";
  }

  public Row(SystemTComputationResult model, OutputViewRow row, boolean doProvenance) {
    super();
    this.doProvenance = doProvenance;
    this.row = row;
    this.model = model;
  }

  private String getSpanDisplayValue(FieldValue val) {
    SpanVal span = (SpanVal) val;
    if (Constants.SPAN_UNDEFINED_OFFSET == span.start
        || Constants.SPAN_UNDEFINED_SOURCE_ID == span.sourceID) {
      return Constants.NULL_DISPLAY_VALUE;
    }
    return span.getText(this.model) + " [" + span.start + "-" + span.end + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  @Override
  public String getLabelForCell(int i) {
    if (i < this.row.fieldValues.length) {
      FieldValue val = this.row.fieldValues[i];
      if (val != null) {
        if (val instanceof SpanVal) {
          return getSpanDisplayValue(val);
        } else if (val instanceof ListVal) {
          ListVal listValue = (ListVal) val;
          if (listValue.values != null) {
            Iterator<FieldValue> iter = listValue.values.iterator();
            StringBuffer sb = new StringBuffer(""); //$NON-NLS-1$
            while (iter.hasNext()) {
              FieldValue fieldVal = iter.next();
              if (fieldVal instanceof SpanVal) {
                sb.append(getSpanDisplayValue(fieldVal) + ","); //$NON-NLS-1$
              } else {
                sb.append(fieldVal.toString() + ","); //$NON-NLS-1$
              }
            }
            sb.deleteCharAt(sb.length() - 1);
            return sb.toString();
          }
          return Constants.NULL_DISPLAY_VALUE;
        }

        return this.row.fieldValues[i].toString();
      }
    }
    if (i == this.row.fieldValues.length) {
      // The input document name
      return this.model.getDocumentID();
    }
    return Messages.getString("ProvenanceCellElement_EXPLAIN"); //$NON-NLS-1$
  }

  @Override
  public boolean isProvenanceCell(int i) {
    return (i == this.row.fieldValues.length + 1);
  }

  @Override
  public String getInputDocName() {
    return this.model.getDocumentID();
  }

  @Override
  public String getInputDocText() {
    return this.model.getInputText();
  }


  // Only the FeildValue of type SpanVal will have the source Id. Returns -1 if the FeildValue is
  // not SpanVal
  @Override
  public int getSourceId(int i) {
    if (i < this.row.fieldValues.length) {
      FieldValue val = this.row.fieldValues[i];
      if (val != null) {
        if (val instanceof SpanVal) {
          SpanVal span = (SpanVal) val;
          return span.sourceID;
        }
      }
    }
    return -1;
  }

  @Override
  public String getSourceText(int sourceId) {
    return this.model.getTextValueMap().get(Integer.valueOf(sourceId));
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.tableview.model.IRow#getPosition()
   */
  @Override
  public int getPosition() {
    return this.pos;
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.tableview.model.IRow#setPosition(int)
   */
  @Override
  public void setPosition(int pos) {
    this.pos = pos;
  }

  public SystemTComputationResult getModel ()
  {
    return model;
  }

  /* (non-Javadoc)
   * @see com.ibm.biginsights.textanalytics.tableview.model.IRow#getValueForCell(int)
   */
  @Override
  public FieldValue getValueForCell(int col) {
    return this.row.fieldValues[col];
  }

}
