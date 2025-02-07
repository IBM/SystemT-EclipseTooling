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
package com.ibm.biginsights.textanalytics.explain;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
 
	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.explain.messages"; //$NON-NLS-1$


	public static String ExplainModuleView_FOLDER_NAME_VIEWS;
  public static String ExplainModuleView_FOLDER_COMMENT_VIEWS;
  public static String ExplainModuleView_FOLDER_NAME_DICTIONARIES;
  public static String ExplainModuleView_FOLDER_COMMENT_DICTIONARIES;
  public static String ExplainModuleView_FOLDER_NAME_TABLES;
  public static String ExplainModuleView_FOLDER_COMMENT_TABLES;
  public static String ExplainModuleView_FOLDER_NAME_REQ_FUNCTIONS;
  public static String ExplainModuleView_FOLDER_COMMENT_REQ_FUNCTIONS;
  public static String ExplainModuleView_FOLDER_NAME_REQ_MODULES;
  public static String ExplainModuleView_FOLDER_COMMENT_REQ_MODULES;

  public static String ExplainModuleView_ISREQUIRED;
  public static String ExplainModuleView_ISEXPORTVIEW;
  public static String ExplainModuleView_ISEXPORTDICT;
  public static String ExplainModuleView_ISEXPORTTABLE;
  public static String ExplainModuleView_ISEXPORTFUNC;
  public static String ExplainModuleView_ISEXTERNALVIEW;
  public static String ExplainModuleView_ISEXTERNALDICT;
  public static String ExplainModuleView_ISEXTERNALTABLE;
  public static String ExplainModuleView_ISDETERMINISTIC;
  public static String ExplainModuleView_ISOUTPUTVIEW;

  public static String ExplainModuleView_DICTNAME;
  public static String ExplainModuleView_EXTERNALNAME;
  public static String ExplainModuleView_FILENAME;
  public static String ExplainModuleView_FUNCTIONNAME;
  public static String ExplainModuleView_HOSTNAME;
  public static String ExplainModuleView_MODULENAME;
  public static String ExplainModuleView_OUTPUT_ALIAS;
  public static String ExplainModuleView_TABLENAME;
  public static String ExplainModuleView_USERNAME;
  public static String ExplainModuleView_VIEWNAME;

  public static String ExplainModuleView_CASESENS;
  public static String ExplainModuleView_COMPILETIME;
  public static String ExplainModuleView_COSTRECORD;
  public static String ExplainModuleView_DOCSCHEMA;
  public static String ExplainModuleView_FILE_PICKER_DESCRIPTION;
  public static String ExplainModuleView_LANGUAGE;
  public static String ExplainModuleView_LANGUAGES;
  public static String ExplainModuleView_MODULEPROPS;
  public static String ExplainModuleView_COMMENT_MODULEPROPS;
  public static String ExplainModuleView_PARAMS;
  public static String ExplainModuleView_PLANAOG;
  public static String ExplainModuleView_PRODVER;
  public static String ExplainModuleView_RETURNTYPE;
  public static String ExplainModuleView_RETURNLIKECOL;
  public static String ExplainModuleView_SCHEMA;
  public static String ExplainModuleView_TOKENIZER;


  public static String ExplainModuleView_ERROR_READ_TAM;
  public static String ExplainModuleView_ERROR_OPEN_EMV;

  static {
    // initialize resource bundle
    NLS.initializeMessages (BUNDLE_NAME, Messages.class);
  }

  private Messages ()
  {}
}
