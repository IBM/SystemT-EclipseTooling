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

import org.eclipse.core.resources.IProject;

import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.tableview.model.IAQLTableViewModel;

public interface IConcordanceModel {

	public static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  public static enum StringFilterType {
    ANNOTATION_TEXT, LEFT_CONTEXT, RIGHT_CONTEXT
  }

  public static final int COLUMN_FILE_ID = 0;

  public static final int COLUMN_LEFT_CONTEXT = 1;

  public static final int COLUMN_ANNOTATION_TEXT = 2;

  public static final int COLUMN_RIGHT_CONTEXT = 3;

  public static final int COLUMN_ANNOTATION_TYPE = 4;

  public static final int NUMBER_OF_COLUMNS = 5;

  int size();

  // IConcordanceModelEntry getEntry(int i);

  IConcordanceModelEntry[] getEntries();

  String[] getTableColumnTitles();

  int[] getTableColumnWidths();

  int[] getTableColumnOrientation();
  
  public List<SystemTComputationResult> getSTCRModels();

  ITypes getTypes();

  IFiles getFiles();

  IStringFilter getStringFilter(StringFilterType filterType);

  void setStringFilter(StringFilterType filterType, IStringFilter filter);

  void resetStringFilter(StringFilterType filterType);

  IProject getProject() ;
	  
  void setProject(IProject project);
	  /**
   * 
   * @return The list of output views so we can display a menu.
   */
  String[] getOutputViewNames();

  /**
   * 
   * @param viewName
   *          The view for which we want the model.
   * @return A model to run a table view off of.
   */
  IAQLTableViewModel getViewModel(String viewName);

  /**
   * 
   * @return Get the path to the temp dir where the text files needed display in an editor can be
   *         written.
   */
  String getTempDirPath();
  
  /**
   * 
   * @return The provenance run parameters.  May be <code>null</code> when read from disk.
   */
  ProvenanceRunParams getProvenanceParams();
}
