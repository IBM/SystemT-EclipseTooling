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

import com.ibm.biginsights.textanalytics.resultmodel.Messages;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * A field value in a SystemT view.  Each value knows its type.
 */
public abstract class FieldValue {



  public abstract FieldType getType();
  
  public static String toString(FieldValue val, SystemTComputationResult model) {
    switch (val.getType()) {
    case BOOL: return Boolean.toString(((BoolVal) val).val);
    case FLOAT: return Float.toString(((FloatVal) val).val);
    case INT: return Integer.toString(((IntVal) val).val);
    case LIST: return val.toString();
    case SPAN: {
      SpanVal span = (SpanVal) val;
      if (span.start == Constants.SPAN_UNDEFINED_OFFSET) {
        return Messages.getString("FieldValuenull"); //$NON-NLS-1$
      }
      String annotated = StringUtils.normalizeWhitespace(span.getText(model));
      StringBuilder sb = new StringBuilder(annotated);
      sb.append(" ["); //$NON-NLS-1$
      sb.append(Integer.toString(span.start));
      sb.append(',');
      sb.append(Integer.toString(span.end));
      sb.append(']');
      return sb.toString();
    }
    case STRING: return StringUtils.normalizeWhitespace(((StringVal) val).val);
    case TEXT: return StringUtils.normalizeWhitespace(val.toString());
    case NULL: return Messages.getString("FieldValuenull");
    default: return Messages.getString("FieldValueUnknown"); //$NON-NLS-1$
    }
  }

}
