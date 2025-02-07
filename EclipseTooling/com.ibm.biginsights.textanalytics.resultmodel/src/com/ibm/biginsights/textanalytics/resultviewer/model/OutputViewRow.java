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

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A row in a SystemT view.
 */
@XmlRootElement
public class OutputViewRow {



  // This is JAXB magic to make the serialization of field values come out right. JAXB is not
  // particularly good at handling inheritance.
  @XmlElementRefs({ @XmlElementRef(type = BoolVal.class), @XmlElementRef(type = IntVal.class),
      @XmlElementRef(type = FloatVal.class), @XmlElementRef(type = TextVal.class),
      @XmlElementRef(type = SpanVal.class), @XmlElementRef(type = ListVal.class), 
      @XmlElementRef(type = StringVal.class),})
  public FieldValue[] fieldValues;

  public OutputViewRow() {
    super();
  }
  
  public OutputViewRow(int size) {
    super();
    this.fieldValues = new FieldValue[size];
  }
  
  public void put(int i, FieldValue val) {
    this.fieldValues[i] = val;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof OutputViewRow) {
      OutputViewRow row = (OutputViewRow) o;
      if (this.fieldValues.length != row.fieldValues.length) {
        return false;
      }
      for (int i = 0; i < this.fieldValues.length; i++) {
        if (!this.fieldValues[i].equals(row.fieldValues[i])) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

}
