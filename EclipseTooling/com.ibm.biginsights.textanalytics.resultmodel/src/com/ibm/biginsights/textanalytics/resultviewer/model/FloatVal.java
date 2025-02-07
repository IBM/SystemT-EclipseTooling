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

import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.FLOAT;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * A SystemT view float field value.
 */
@XmlRootElement
public class FloatVal extends FieldValue {



  @XmlAttribute
  public Float val;
  
  public FloatVal() {
    super();
  }
  
  public FloatVal(Float f) {
    super();
    this.val = f;
  }
  
  @Override
  public FieldType getType() {
    return FLOAT;
  }
  
  @Override
  public String toString() {
    if (this.val == null) {
      return Constants.NULL_DISPLAY_VALUE;
    }
    return Float.toString(this.val);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof FloatVal) {
      return this.val == ((FloatVal) o).val;
    }
    return false;
  }

}
