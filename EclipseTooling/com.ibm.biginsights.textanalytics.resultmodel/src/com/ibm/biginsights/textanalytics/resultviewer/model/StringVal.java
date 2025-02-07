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

import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.STRING;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * A SystemT view String field value.
 */
@XmlRootElement
public class StringVal extends FieldValue {



  @XmlAttribute
  public String val;
  
  public StringVal() {
    super();
  }
  
  public StringVal(String s) {
    super();
    this.val = s;
  }
  
  @Override
  public FieldType getType() {
    return STRING;
  }
  
  @Override
  public String toString() {
    if (this.val == null) {
      return Constants.NULL_DISPLAY_VALUE;
    }
    return this.val;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof StringVal) {
      return this.val == ((StringVal) o).val;
    }
    return false;
  }

}
