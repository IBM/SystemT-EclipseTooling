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

import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.LIST;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A SystemT view list field value.
 */
@XmlRootElement
public class ListVal extends FieldValue {



  public List<FieldValue> values;

  public boolean isNull() {
    return this.values == null;
  }
  
  @Override
  public FieldType getType() {
    return LIST;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    boolean first = true;
    if (this.values != null) {
      for (FieldValue v : this.values) {
        if (first) {
          first = false;
        } else {
          sb.append(", "); //$NON-NLS-1$
        }
        sb.append(v.toString());
      }
    }
    sb.append(']');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ListVal) {
      ListVal val = (ListVal) o;
      if (this.values == null) {
        return val.values == null;
      }
      if (this.values.size() != val.values.size()) {
        return false;
      }
      for (int i = 0; i < this.values.size(); i++) {
        if (!this.values.get(i).equals(val.values.get(i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
