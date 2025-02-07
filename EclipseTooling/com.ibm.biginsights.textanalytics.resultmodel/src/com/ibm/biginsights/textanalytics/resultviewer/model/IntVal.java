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

import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.INT;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * A SystemT view int field value.
 */
@XmlRootElement
public class IntVal extends FieldValue {



  @XmlAttribute
  public Integer val;
  
  public IntVal() {
    super();
  }
  
  public IntVal(Integer i) {
    super();
    this.val = i;
  }
  
  @Override
  public FieldType getType() {
    return INT;
  }
  
  @Override
  public String toString() {
    if (this.val == null) {
      return Constants.NULL_DISPLAY_VALUE;
    }
    return Integer.toString(this.val);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof IntVal) {
      return this.val == ((IntVal) o).val;
    }
    return false;
  }

}
