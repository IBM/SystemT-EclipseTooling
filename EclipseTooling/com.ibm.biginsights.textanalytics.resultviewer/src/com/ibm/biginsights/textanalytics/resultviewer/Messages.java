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
package com.ibm.biginsights.textanalytics.resultviewer;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {


 
	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.resultviewer.messages"; //$NON-NLS-1$
  
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
	.getBundle(BUNDLE_NAME);

  public static String ConcordanceModel_0;

  public static String ConcordanceModel_inputdoc;

  public static String ConcordanceModel_inputdocname;

  public static String ConcordanceModel_leftcontext;

  public static String ConcordanceModel_rightcontext;

  public static String ConcordanceModel_spanattrname;

  public static String ConcordanceModel_spanval;

  public static String resultTextFileExtension;
  public static String selectOutputViewNameFromDropdownMessage;

  public static String selectOutputViewNameMessage;

  public static String showOutputViewMessage1;

  public static String showOutputViewMessage2;

  public static String unableToOpenEditorMessage;

  public static String viewDescription;
  
  public static String parentResultFolderDoesNotExist;
  
  public static String spanTooltipEnabled;
  
  public static String spanTooltipDisabled;
  
  public static String spanSelectionMessage;
  
  public static String labelShowViewInEditor;
  
  public static String error;
  
  public static String exportResultsWizardErrorMsg;
  
  public static String exportResultsWizardErrorMsg2;
  
  public static String exportResultsWizardErrorExportingViews;
  
  public static String exportResultsExportResults;
  
  public static String exportResultsDisplayWarningTitle;
  
  public static String exportResultsDisplayWarningMessage;
  
  public static String exportResultsDisplayWarningToggleMessage;
  
  public static String exportResultsWizardPageTitle;
  
  public static String exportResultsWizardPageErrorDirectoryInvalid;
  
  public static String exportResultsWizardPageErrorDirectoryBlank;
  
  public static String exportResultsWizardPageDirectoryLabel;
  
  public static String FilterView_spanattrname;

  public static String FilterView_spanattrvalue;

  public static String FilterView_inputdoc;

  public static String FilterView_leftcontext;

  public static String FilterView_rightcontext;

  public static String FilterView_filter;

  public static String FilterView_values;

  public static String FilterView_title;

  public static String FilterView_notconfigured;
  public static String FilterView_checkboxApplyTooltip;
  
  public static String FilterView_checkboxRemoveTooltip;
  
  public static String FilterView_clearButtonTooltip;
  
  public static String FilterView_configButtonTooltip;
  
  public static String FilterPanel_cb1_AccessibilityMessage;
	
  public static String inputDocumentTitle;
  
  public static String inputDocumentMessage;

  public static String addButtonLabel;
  
  public static String removeButtonLabel; 
  
  public static String filterSelectionLabel; 
  
  public static String spanAttributeTitle;
  
  public static String spanAttributeMessage;

  public static String ShowThisDocHandler_SelectDocument;
  
  public static String NoResultToDisplay;
  
  public static String SelectSomething;

  public static String AnonymousTooltipDisabled;
  
  public static String ConcordanceView_Header;
  
  public static String ConcordanceView_Header_NoSpansInPage;
  
  public static String ConcordanceView_Name;

  public static String ExportResultsWizardPage_Description;

  public static String TextPatternInputDialog_FullRegexCheckbox;
  
  public static String ConcordanceUtil_openConcordanceViewError;
  
  public static String ConcordanceUtil_invalidInputDocumentsError;
 
  static {
    // initialize resource bundle
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
  
  public static String getString(String key, Object[] params) {
		try {
			return MessageFormat.format(RESOURCE_BUNDLE.getString(key),params);
		} catch (MissingResourceException e) {
			return '!' + key + "!";
		}
	}
  
  private Messages() {
    // not used
  }
}
