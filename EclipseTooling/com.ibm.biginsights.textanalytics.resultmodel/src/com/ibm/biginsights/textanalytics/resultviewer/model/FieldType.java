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

import javax.xml.bind.annotation.XmlEnum;

/**
 * The various SystemtT view field types.
 */
@XmlEnum
public enum FieldType {

  TEXT, SPAN, INT, FLOAT, BOOL, LIST, STRING, NULL;



  public static FieldType forOrdinal(int ord) {
    switch (ord) {
    case 0:
      return TEXT;
    case 1:
      return SPAN;
    case 3:
      return INT;
    case 4:
      return FLOAT;
    case 5:
      return BOOL;
    case 6:
      return LIST;
    case 7:
      return STRING;
    case 8:
      return NULL;
    default:
      return null;
    }
  }

}
