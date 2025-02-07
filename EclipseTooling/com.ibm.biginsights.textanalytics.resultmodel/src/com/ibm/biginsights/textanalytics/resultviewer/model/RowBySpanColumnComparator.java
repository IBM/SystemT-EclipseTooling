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
package com.ibm.biginsights.textanalytics.resultviewer.model;

import static com.ibm.biginsights.textanalytics.resultviewer.model.Util.spanOrderCompare;

import java.util.Comparator;

public class RowBySpanColumnComparator implements Comparator<OutputViewRow> {



  private final int c;

  public RowBySpanColumnComparator(int spanColId) {
    super();
    this.c = spanColId;
  }

  @Override
  public int compare(OutputViewRow row1, OutputViewRow row2) {
    if(row1 == null || row2 == null){
      return -1;
    }

    if(row1.fieldValues == null || row2.fieldValues == null){
      return -1;
    }

    if(row1.fieldValues.length != row2.fieldValues.length){
      return -1;
    }

    SpanVal span1 = null;
    SpanVal span2 = null;
    if(this.c < row1.fieldValues.length) {
      if (row1.fieldValues[this.c] instanceof SpanVal)
        span1 = (SpanVal)row1.fieldValues[this.c];
    }
    if(this.c < row2.fieldValues.length) {
      if (row2.fieldValues[this.c] instanceof SpanVal)
        span2 = (SpanVal)row2.fieldValues[this.c];
    }

    if(span1 == null) return -1;
    if(span2 == null) return -1;

    return spanOrderCompare(span1, span2);
  }

}
