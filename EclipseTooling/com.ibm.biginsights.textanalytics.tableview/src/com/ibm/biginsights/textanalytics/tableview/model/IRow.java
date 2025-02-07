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
package com.ibm.biginsights.textanalytics.tableview.model;

import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;

public interface IRow {

	public static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
  
  String getLabelForCell(int i);
  
  FieldValue getValueForCell(int col);

  boolean isProvenanceCell(int i);
  
  String getInputDocName();
  
  String getInputDocText();
  
  /**
   * Returns source id for the row
   * @param i
   * @return
   */
  int getSourceId(int i);
  
  /**
   * Returns Text for the Source Id
   * @param sourceId
   * @return source Text
   */
  String getSourceText(int sourceId);
  
  String getDocSchemaName(int i);
  
  /**
   * Get the position of the row according to the current sorting scheme.
   * @return
   */
  int getPosition();
  
  /**
   * Set the position of the row according to the current sorting scheme.
   * @param pos
   */
  void setPosition(int pos);
  
}
