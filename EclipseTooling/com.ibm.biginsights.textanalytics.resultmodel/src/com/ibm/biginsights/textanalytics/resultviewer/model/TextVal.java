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

import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.TEXT;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * A SystemT view text field value.
 */
@XmlRootElement
public class TextVal extends FieldValue {



  @XmlAttribute
  public String val;
  
  public TextVal() {
    super();
  }
  
  public TextVal(String text) {
    super();
    this.val = text;
  }
  
  @Override
  public FieldType getType() {
    return TEXT;
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
    if (o instanceof TextVal) {
      return this.val.equals(((TextVal) o).val);
    }
    return false;
  }

}
