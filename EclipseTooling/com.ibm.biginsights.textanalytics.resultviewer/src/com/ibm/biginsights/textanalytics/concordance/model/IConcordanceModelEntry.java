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
package com.ibm.biginsights.textanalytics.concordance.model;

import java.util.List;

import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;

public interface IConcordanceModelEntry {

	public static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
  
  String getDocId();
  
  String getAnnotationText();
  
  String getAnnotationType();
  
  String getLeftContext();
  
  String getRightContext();
  
  /**
   * 
   * @return The underlying text that the entry annotates
   */
  String getBaseText();
  
  /**
   * 
   * @return The annotation offsets for this entry.  Useful for highlighting.
   */
  List<Integer> getOffsets();
 
  /**
   * 
   * @return The internal ID of the text this span annotates.  Required to identify editors.
   */
  int getTextSourceID();
  
  /**
   * 
   * @return The name of the doc schema of the span so we can use it in the editor title.
   */
  String getDocSchemaName();
  
  /**
   * 
   * @return Get the model for the document of the entry.
   */
  SystemTComputationResult getModel();
  
  /**
   * 
   * @return Get the base concordance model.
   */
  IConcordanceModel getConcordanceModel();
  
  /**
   * Get the output view model for this entry.
   * @return
   */
  OutputView getOutputView();
  
  /**
   * Get the output view row model for this entry.
   * @return
   */
  OutputViewRow getRow();
  
  /**
   * Get the Index for the row.
   * @return
   */
  public int getRowIndex();
}
