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
package com.ibm.biginsights.textanalytics.aql.editor;

import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {


	
	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.aql.editor.messages"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	public static String AQLEditor_ENCODING_WARNING;
	public static String ANTLRTest_COMMON_TREE;
	public static String ANTLRTest_PARSE_TREE_LABEL;
	public static String ANTLRTest_SUBTREE_NULL;
	public static String ANTLRTest_UNKNOWN_TREE_TYPE_LABEL;
	public static String AQLEditor_ADDITIONS;
	public static String AQLEditor_EDITOR_SCOPE;
  
	public static String AQLEditor_REGEX_BUILDER;
	public static String AQLEditor_REGEX_BUILDER_ICON;
	public static String AQLEditor_REGEX_BUILDER_MENU;
	public static String AQLEditor_REGEX_BUILDER_WIZARD_COMMAND;
	public static String AQLEditor_REGEX_GEN_ICON;
	public static String AQLEditor_REGEX_GEN_MENU;
	public static String AQLEditor_REGEX_GEN_WIZARD_COMMAND;
	public static String AQLEditor_REGEX_GENERATOR;
	public static String AQLEditor_PATTERN_DISCOVERY_ICON;
	public static String AQLEditor_PATTERN_DISCOVERY;
	public static String AQLEditor_PATTERN_DISCOVERY_WIZARD_COMMAND;
	public static String AQLEditor_OPEN_DECLARATION_COMMAND;
	public static String AQLEditor_ELEMENT_RENAME_COMMAND;
	public static String AQLEditor_OPEN_DECLARATION;
	public static String AQLEditor_NO_BIGINSIGHTS_NATURE;

	public static String AQLEditor_MISSING_VIEW_IN_AQL_FILES;
	public static String AQLEditor_MISSING_SOURCE_IN_AQL_FILES;
	public static String AQLEditor_SELECTED_TOKEN_IS_AQL_KEYWORD;
	public static String AQLEditor_SOURCE_NOT_FOUND;
	public static String AQLEditor_AQL_KEYWORD_TOKEN;
	public static String AQLEditor_ERR_PROJ_REF;
  public static String AQLEditor_NOT_VIEW;
  public static String AQLEditor_OPEN_DEPENDENCY_HIERARCHY;
  public static String AQLEditor_OPEN_REFERENCE_HIERARCHY;
  public static String AQLEditor_HOVER_TIP;
  public static String ChildHierarchyHandler_SHOW_VIEW_FAIL;
  public static String DependencyHierarchy_DESC;
  public static String HierarchyRootNode_FILE_NOT_FOUND;
  public static String HierarchyRootNode_PARSE_ERROR;
  public static String ParentHierarchyHandler_SHOW_VIEW_FAIL;
  public static String ReferenceHierarchy_DESC;
  public static String ResourceChangeListener_BUILD_JOB_NAME;
  public static String AQL_SRC_MODULE_PATH_NOT_CONFIGURED;

  public static String ModularAQLNavigator_OffsetError;
  public static String ResourceChangeActionDelegate_WARNING;
  public static String ProjectAddedActionMessage_MIGRATION_WARNING;
  
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
	
	public static ResourceBundle getResourceBundle(){
		return RESOURCE_BUNDLE;
	}
}
