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
package com.ibm.biginsights.textanalytics.goldstandard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {


	
	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.goldstandard.messages"; //$NON-NLS-1$
	public static String AbstractExplorerMarkCompleteIncompleteActionHandler_NOT_MARKED_COMPLETE_AS_ALREADY_MARKED_SO;
	public static String AbstractExplorerMarkCompleteIncompleteActionHandler_NOT_MARKED_INCOMPLETE_AS_ALREADY_MARKED_SO;
	public static String AbstractMarkCompleteIncompleteActionDelegate_PROCESSING_FILE;
	public static String AbstractMarkCompleteIncompleteActionDelegate_SELECT_GS_FOLDER_OR_FILE;
	public static String AbstractMarkCompleteIncompleteActionDelegate_SELET_GS_FILE;
	public static String AnnotateAsHandler_ALREADY_MARKED_COMPLETE;
	public static String AnnotateAsHandler_ANNOTATION_ALREADY_EXISTS_FOR_CURRENT_TUPLE;
	public static String AnnotateAsHandler_INCORRECT_ORDER_OF_ANNOTATIONS;
	public static String AnnotateAsHandler_NO_ANNOTATION_TYPE_DEFINED;
	public static String AnnotateAsHandler_SELECT_A_SPAN_OR_CONFIGURE_AUTO_DETECT_WORD_BOUNDARY;
	public static String AnnotationImporter_ANNOTATION_TYPES_AUTO_IMPORTED;
	public static String AnnotationImporter_ANNOTATION_TYPES_NO_SPAN_DOCUMENT_TYPES;
	public static String AnnotationImporter_IMPORTING_ANNOTATION_TYPES;
	public static String AnnotationImporter_IMPORTING_ANNOTATION_TYPES_FROM;
	public static String BootstrapAQLResultHandler_BOOTSTRAP_FROM_RESULT;
	public static String BootstrapAQLResultHandler_IMPORTING_FILE;
	public static String BootstrapAQLResultHandler_IMPORTING_FROM_RESULT_DIR;
	public static String BootstrapAQLResultHandler_INFO_IMPORTING_FROM_RESULT;
	public static String BootstrapAQLResultHandler_LIST_OF_FILES_NOT_IMPORTED;
	public static String BootstrapAQLResultHandler_RESULT_IMPORTED_SUCCESSFULLY;
	public static String BootstrapAQLResultHandler_SELECT_RESULT_DIR;
	public static String ConfigurationDialog_ANNOTATION_TYPES;
	public static String ConfigurationDialog_GENERAL;
	public static String ConfigurationDialog_GOLD_STANDARD;
	public static String DeleteAnnotationHandler_ALREADY_MARKED_COMPLETE;
	public static String DeleteAnnotationHandler_SELECT_A_SPAN_OR_CONFIGURE_AUTO_DETECT_WORD_BOUNDARY;
	public static String DELFileImporter_DEL_FILES_WITHOUT_LABEL_NOT_SUPPORTED;
	public static String DELFileImporter_INCORRECT_FIELD_LENGTH;
	public static String ExplorerImportResultHandler_RESULT_NOT_IMPORTED_SINCE_IT_CONTAINS_NON_SPAN_FIELDS;
	public static String GenericPrefPage_ERROR;
	public static String GenericPrefPage_SELECT_PROJECT;
	public static String GoldStandardPrefPage_GS_DESCRIPTION;
	public static String GoldStandardUtil_GS_NOT_CONFIGURED;
	public static String GoldStandardUtil_UNABLE_TO_DECIDE_IF_GS_FOLDER;
	public static String GoldStandardUtil_UNABLE_TO_DECIDE_IF_GSPARENT_DIR;
	public static String GoldStandardUtil_UNABLE_TO_FIND_GS_PARENT_DIR;
	public static String GSActionDelegate_UNABLE_TO_DELETE_GS_PARENT_DIR;
	public static String GSActionHandler_ERROR;
	public static String GSActionHandler_GSFOLDER_CREATION_FAILED;
	public static String GSActionHandler_GSFOLDER_NULL;
	public static String GSActionHandler_PLEASE_SELECT_PROJECT;
	
	public static String GSAnnotationTypesPage_CONFIRM_DELETION_MESSAGE;
	public static String GSAnnotationTypesPage_FIELD_NAME;
	public static String GSAnnotationTypesPage_CONFIRM_DELETION;
	public static String GSAnnotationTypesPage_COPY;
	public static String GSAnnotationTypesPage_DELETE;
	public static String GSAnnotationTypesPage_DUPLICATE_ANNOTATION_TYPE_EXISTS;
	public static String GSAnnotationTypesPage_ENABLED;
	public static String GSAnnotationTypesPage_ERROR;
	public static String GSAnnotationTypesPage_HIGHLIGHT_COLOR;
	public static String GSAnnotationTypesPage_NEW;
	public static String GSAnnotationTypesPage_SAVE;
	public static String GSAnnotationTypesPage_SHORTCUT_KEY;
	public static String GSAnnotationTypesPage_UNTITLED;
	public static String GSAnnotationTypesPage_VIEW_NAME;
	public static String GSGeneralPage_DEFAULT_ANNOTATION_TYPE;
	public static String GSGeneralPage_DETECT_WORD_BOUNDARIES;
	public static String GSGeneralPage_LANGUAGE;
	public static String GSGeneralPage_TIP_DEFAULT_ANNOTATION;
	public static String ImportCorpusActionDelegate_ContinueToWarn;
	public static String ImportCorpusActionDelegate_EncodingNotUTF8;
	public static String ImportCorpusActionDelegate_ERROR_IMPORTING_DOC_COLLECTION;
	public static String ImportCorpusActionDelegate_IMPORT_DOC_COLLECTION;
	public static String ImportCorpusActionDelegate_IMPORT_SUCCESSFUL_CONFIRM_CONFIG_DIALOG_OPEN;
	public static String ImportCorpusActionDelegate_IncorrectEncoding;
	public static String ImportCorpusHandler_IMPORT_CORPUS;
	public static String ImportCorpusHandler_IMPORTING;
	public static String ImportCorpusHandler_IMPORTING_CORPUS;
	public static String ImportCorpusHandler_IMPORTING_FILE;
	public static String ImportCorpusHandler_SELECT_INPUT_COLLECTION;
	public static String ImportResultActionDelegate_ImportResultSuccessful;
	public static String ImportResultActionDelegate_NoExtractorResults;
	public static String MarkCompleteActionDelegate_ALREADY_MARKED_COMPLETE;
	public static String MarkCompleteActionDelegate_MARKING_GS_FILE_AS_COMPLETE;
	public static String MarkCompleteHandler_MARKED_AS_COMPLETE_TO_ADD_OR_DEL_MARK_AS_INCOMPLETE;
	public static String MarkIncompleteActionDelegate_ALREADY_MARKED_INCOMPLETE;
	public static String MarkIncompleteActionDelegate_MARK_GS_FILE_AS_INCOMPLETE;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
