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
package com.ibm.biginsights.textanalytics.util.common;


/**
 * Container for various constants used in this plugin, and which should NOT be
 * externalized.
 * 
 * 
 * 
 */
public class Constants {

	@SuppressWarnings("unused")


	//Plugin Id for Nature
	public static final String PLUGIN_NATURE_ID = "com.ibm.biginsights.textanalytics.nature";

	/**
	 * Undefined offset and source ID value for NULL span objects which have
	 * special treatment in the Result Viewers
	 **/
	public static int SPAN_UNDEFINED_OFFSET = -1;
	public static int SPAN_UNDEFINED_SOURCE_ID = -1;
	
	/** Value to display for null objects **/
	public static String NULL_DISPLAY_VALUE = "<null>";

	/** Enable debugging for provenance **/
	public static final boolean DEBUG_PROVENANCE = false;

	/** Default encoding for any SystemT-related file handling. */
	public static final String ENCODING = "UTF-8";

	
	/**
	 * using for extensions without the '.'
	 */
	public static final String AQL_FILE_EXTENSION_STRING = "aql";
	public static final String TAM_FILE_EXTENSTION_STRING = "tam";
	public static final String DICTIONARY_FILE_EXTENSION_STRING = "dict";
	public static final String CSV_EXTENSION_STRING = "csv";
	public static final String LAUNCH_CONFIG_EXTENSION_STRING = "launch";
	public static final String EXTRACTION_PLAN_EXTENSION_STRING = "extractionplan";
	public static final String TA_PROPS_EXTENSION_STRING = "textanalytics";
	public static final String CLASSPATH_FILE_STRING = "classpath";
	public static final String MODULE_COMMENT_FILE ="module.info";
	/**
	 * Extensions for various files manipulated in this plugin. IMPORTANT: Do
	 * not remove "." in the extensions as several references to these constants
	 * need the "."
	 */
	public static final String AQL_FILE_EXTENSION = ".aql";
	
	public static final String JAR_FILE_EXTENSION = ".jar";
	
	public static final String ZIP_FILE_EXTENSION = ".zip";

	public static final String AOG_FILE_EXTENSION = ".aog";
	
	public static final String TAM_FILE_EXTENSION = ".tam";

	public static final String DICTIONARY_FILE_EXTENSION = ".dict";

	public static final String TEXT_ANALYTICS_PREF_FILE = ".textanalytics";
	
	public static final String CLASSPATH_FILE = ".classpath";

	public static final String ECLIPSE_PROJECT_FILE_EXTENSION = ".project";

  public static final String BIGINSIGHTS_PROJECT_FILE_EXTENSION = ".biginsights";

	public static final String XML_FILE_EXTENSION_FILTER = "*.xml";
	
	public static final String SUPPORTED_DOC_FORMATS = "zip,tar.gz,tar,tgz,del,txt,htm,html,xml,xhtml,json,csv";
	public static final String SUPPORTED_MODULE_PATH_FORMATS = "zip,jar";
	
	public static final String COLUMN_NAME_INPUT_DOCUMENT = "Input Document";
	
	public static final String FIELD_NAME_PREFIX = "field-name=";
	public static final String FIELD_VALUE_PREFIX = ";field-value=";
	
	public static final String MODULE_COMMENT_FILE_EXTENSION = "info";
	// The temp dir inside the result folder where we write the texts from the
	// output views so
	// we can display them in an editor.
	public static final String TEMP_TEXT_DIR_NAME = ".temp";
	/** Formats for the provenance rewritte AQL and AOG file names. */
	public static final String PROVENANCE_AQL_FILE_NAME_FORMAT = "%s_provenance.aql";
	public static final String PROVENANCE_AOG_FILE_NAME_FORMAT = "%s_provenance.aog";
	public static final String PROVENANCE_AOG_FILE_NAME = "provenance.aog";

	/** Label for the provenance header in the table view. */
	public static final String PROVENANCE_BUTTON_LABEL = "Double-click this column to explain a tuple !";

	/** Label for the progress dialog of a provenance job */
	public static final String PROVENANCE_JOB_PROGRESS_LABEL_FORMAT = "Retrieving provenance for view '%s' on document '%s'.";

	/** Error message for problem encountered when generating provenance. */
	public static final String PROVENANCE_JOB_PROBLEM_FORMAT = "Problem encountered when retrieving provenance for view '%s' on document '%s'.";
  public static final String PROVENANCE_JOB_PROBLEM_FORMAT_WITH_DETAIL = "Provenance for view '%s' on document '%s' cannot be retrieved.%n%n%s";

	/** Error message for problem encountered when displaying a provenance view. */
	public static final String PROVENANCE_VIEW_PROBLEM_FORMAT = "Problem encountered when displaying provenance view '%s'.";

  /** Error message for problem encountered when generating provenance. */
  public static final String PROVENANCE_INFO_NOT_AVAILABLE = "Provenance information is not available.";

	// Project preferences
	// IMPORTANT note: If you add or remove properties to .textanalytics file,
	// do remember to update setDefaultValuesInPreferenceStore() method
	// constants for General tab
	public static final String GENERAL_LANGUAGE = "general.language";
	public static final String GENERAL_MAINAQLFILE = "general.mainAQLFile";
	public static final String GENERAL_AOGFILENAME = "general.aogName";
	
	public static final String DEFAULT_PROVENANCE_FOLDER=".provenanceRewrite";
	public static final String PROVENANCE_SRC="src";
	public static final String PROVENANCE_BIN="bin";
	
	public static final String DELIMITER = "Delimiter";
  public static final String TAB = "TAB";
	public static final String SPACE = "SPACE";
	public static final String CUSTOM = "Custom";
	public static final String COMMA = ",";
	public static final String PIPE = "|";
	public static final String COLON = ":";
	public static final String SEMICOLON = ";";
	public static final String[] COMMON_DELIMS = new String[] { Constants.COMMA,
                                                              Constants.PIPE,
                                                              Constants.COLON,
                                                              Constants.SEMICOLON,
                                                              Constants.TAB,
                                                              Constants.SPACE,
                                                              Constants.CUSTOM };

	//Labeled Collection and Result root directories
	public static final String LC_ROOT_DIR = "lc.rootDir";
	public static final String RESULT_ROOT_DIR = "result.rootDir";
	
	//Retaining GENERAL_AOGPATH and GENERAL_RESULTDIR properties for reference during migration from v1.2 to v1.3
	//Ensure that the following two properties are referenced ONLY in migration plugin
	public static final String GENERAL_AOGPATH = "general.aogPath";
	public static final String GENERAL_RESULTDIR = "general.resultDir";

	// constants for Search path tab
	public static final String SEARCHPATH_DATAPATH = "searchPath.dataPath";
	
	// constant for the Dependent project
	public static final String MODULE_DEPENDENTPROJECT = "module.dependentProject";
	
	//constant for TAM Path
	public static final String MODULE_TAMPATH = "module.TAMPath";
	
	
	//constants for annotation explorer pagination settings
	public static final String PAGINATION_ENABLED = "pagination.isEnabled";
	public static final String PAGINATION_FILES_PER_PAGE = "pagination.numFilesPerPage";
	public static final int PAGINATION_FILES_PER_PAGE_DEFAULT_VALUE = 50;
	public static final boolean PAGINATON_ENABLED_DEFAULT_VALUE = true;
	
	// constants for Modular AQL Project
	public static final String MODULAR_AQL_PROJECT = "UseModularAQL";
	public static final String MODULE_SRC_PATH = "srcTextAnalyticsPath";
	public static final String MODULE_BIN_PATH = "tamTextAnalyticsPath";
	public static final String TAM_PATH = "module.TAMPath";
	public static final String DEPENDENT_PROJECT = "module.dependentProject";

	//Source path for Modules
	public static final String DEFAULT_MODULE_SRC = "src";
	public static final String DEFAULT_MODULE_BIN = "bin";
	public static final String DEFAULT_MODULE_PATH = "textAnalytics";
	
	// constants for tokenizer choice
	public static final int TOKENIZER_CHOICE_WHITESPACE = 0;
	// NOTE: no custom tokenizer currently implemented
	public static final int TOKENIZER_CHOICE_CUSTOM = 1;

	// constants for provenance
	public static final String GENERAL_PROVENANCE = "general.provenance";
	public static final boolean GENERAL_DEFAULT_VALUE_ENABLE_PROVENANCE = false;

	// File directory picker constants
	public static final int FILE_ONLY = 0;
	public static final int DIRECTORY_ONLY = 1;
	public static final int PROJECT_ONLY = 2;
	public static final int FILE_OR_DIRECTORY = 3;
	public static final int FILE_OR_DIRECTORY_OR_PROJECT = 4;
	public static final int DIRECTORY_OR_PROJECT = 5;

	// constants for SystemTProjectPreferences consumer
	public static final int CONSUMER_PROPERTY_SHEET = 0;
	public static final int CONSUMER_RUN_CONFIG = 1;
	public static final int CONSUMER_EXPORT_AOG = 2;

	public static final String DATAPATH_SEPARATOR = ";";
	
	// Constants for Annotation Explorer 
	public static final String CSV_DIR = "csv";
	public static final String HTML_DIR = "html";
	public static final String CSV_EXTENSION = ".csv";
	public static final String HTML_EXTENSION = ".html";
	

	// COnstants for the AQL refeinement plugin
	public static final String REFINER_CONFIG_PATH_PROP = "refiner.configPath";
	public static final String REFINER_DATA_PATH_PROP = "refiner.dataPath";
	public static final String REFINER_LABEL_PATH_PROP = "refiner.labelPath";
	public static final String REFINER_PROJECT_NAME_PROP = "refiner.projectName";
	public static final String REFINER_VIEW_NAME_PROP = "refiner.viewName";

	public static final String REFINER_CONSOLE_NAME = "AQL Refiner Console";
	
	public static final String WORKSPACE_RESOURCE_PREFIX = "[W]";
	public static final String PROJECT_RELATIVE_PATH_PREFIX = "[P]"; //In Text Analytics properties file, indicates path is relative to a project.
	public static final String DEFAULT_RESULT_DIR_PREFIX = "result";
	public static final String DEFAULT_AOG_DIR = ".aog";
	public static final String GENERIC_MODULE = "genericModule";
	
	public static final String FILENAME_NORMALIZING_CHAR = "~";

	//GoldStandard properties
	public static final String GS_DEFAULT_ANNOTATION_TYPE = "defaultAnnotationType";
	public static final String GS_DETECT_WORD_BOUNDARIES = "detectWordBoundaries";
	public static final String GS_LANGUAGE = "language";
	public static final String GS_ANNOTATION_TYPES = "annotationTypes";
	public static final String GS_ANNOTATION_TYPES_REAPED_FROM = "annotationTypesReapedFrom";
	
	public static final String GS_DIR_PREFIX = "lc";
	public static final String GS_FILE_EXTENSION = "lc";
	public static final String GS_FILE_EXTENSION_WITH_DOT = ".lc";
	public static final String GS_DEFAULT_PARENT_DIR = "labeledCollections";
	public static final String GS_FIELD_MATCH = "match";
	public static final String GS_DOCUMENT_DOT_TEXT = "Document.text";

	public static final String EXTRACTION_PLAN_FILE_NAME = ".extractionplan";
	
	public static final String STRF_FILE_EXTENSION="strf";
	public static final String STRF_FILE_EXTENSION_WITH_DOT=".strf";
	
	public static enum GoldStandardType {
	    Exact, Partial, Relaxed
	  }	
	public static final String GS_PREF_FILE = "lc.prefs";
	public static final String GS_EDITOR_ID = "com.ibm.biginsights.textanalytics.goldstandard.editor";
	
	public static final String RUN_CONFIG_TYPE = "com.ibm.biginsights.textanalytics.nature.systemTApplication";
	public static final String PROFILE_CONFIG_TYPE = "com.ibm.biginsights.textanalytics.profile.systemTApplication";
	
	public static final String DEFAULT_ROOT_RESULT_DIR = "result";
	public static final String V12_DEFAULT_ROOT_RESULT_DIR = ".result";
	public static String PARENT_DIR_SKIP_LIST = ",.aog,bin,src,.temp,"; //$NON-NLS-1$
	
	//Constants related to resource change actions
	public static final int RESCHNG_DELETE = 1;
	public static final int RESCHNG_RENAME = 2;
	public static final int RESCHNG_ADD = 3;
	
	//AQL Error Constant for Marker
	public static String ALL_DOCS = "All_Documents";
	public static String ALL_DOCS_WITH_ANNOTS = "All_Documents_With_Selected_Annotations";
	public static final String AQL_PARSE_ERROR_TYPE = "com.ibm.biginsights.textanalytics.aqleditor.parseerror";
	public static final String AQL_COMPILE_ERROR_TYPE = "com.ibm.biginsights.textanalytics.aqleditor.compileerror";
	
	//Show view in editor commands..and params.. added for navigating from annotation/tree/result table to aql editor..
	public static final String CMD_SHOW_VIEW_IN_EDITOR_ID = "com.ibm.biginsights.textanalytics.aql.editor.command.openViewInAQLEditor"; //$NON-NLS-1$
	public static final String CMD_SHOW_VIEW_IN_EDITOR_ID_PARAM_VIEW_NAME = "com.ibm.biginsights.textanalytics.aql.editor.commandParam.viewName"; //$NON-NLS-1$
	public static final String CMD_SHOW_VIEW_IN_EDITOR_ID_PARAM_PROJ_NAME ="com.ibm.biginsights.textanalytics.aql.editor.commandParam.currProject"; //$NON-NLS-1$
	
	//Commands to get the module references for a module
	public static final String CMD_GET_ALL_REFERENCING_MODULE = "com.ibm.biginsights.textanalytics.indexer.getAllReferencingModule"; //$NON-NLS-1$
	public static final String CMD_GET_ALL_REFERENCING_MODULE_PARAM_MODULE_NAME = "com.ibm.biginsights.textanalytics.indexer.getAllReferencingModule.commandParam.moduleName"; //$NON-NLS-1$
	public static final String CMD_GET_ALL_REFERENCING_MODULE_PARAM_PROJ_NAME ="com.ibm.biginsights.textanalytics.indexer.getAllReferencingModule.commandParam.currProject"; //$NON-NLS-1$

	
	public static final String UNABLE_TO_OPEN_EDITOR_MESSAGE = "Unable to open editor"; //$NON-NLS-1$
	public static final String UNABLE_TO_PROCESS_THE_CONTENT_ASSISTANCE_INFO = "Unable to process the content assistance request"; //$NON-NLS-1$
	public static final String UNABLE_TO_PROCESS_AQL_DOC_COMMENT = "Unable to process the AQL doc comment"; //$NON-NLS-1$
	public static final String UNABLE_TO_CHANGE_THE_RESOURCE = "Unable to change the resource"; //$NON-NLS-1$
	public static final String ERROR_OCCURED_DURING_RECONCILE ="Error occured while reconciling the file";
	
	public static final String AQL_ELEMENT_TYPE_VIEW = "VIEW"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_SELECT ="SELECT"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_DICT = "DICTIONARY"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_EXTERNAL_DICT = "EXTERNAL_DICTIONARY"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_DETAG = "DETAG"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_FUNC ="FUNCTION"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_TABLE = "TABLE"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_EXTERNAL_TABLE = "EXTERNAL_TABLE"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_EXTERNAL_VIEW ="EXTERNAL_VIEW"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_INCLUDE = "INCLUDE"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_OUTPUT_VIEW = "OUTPUT_VIEW"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_MODULE = "MODULE"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_IMPORT_MODULE = "IMPORT_MODULE"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_IMPORT_VIEW = "IMPORT_VIEW"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_IMPORT_DICTIONARY = "IMPORT_DICTIONARY"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_IMPORT_FUNCTION = "IMPORT_FUNCTION"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_IMPORT_TABLE = "IMPORT_TABLE"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_REQUIRE_DOCUMENT = "REQUIRE_DOCUMENT"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_EXPORT_FUNCTION = "EXPORT_FUNCTION"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_EXPORT_DICTIONARY = "EXPORT_DICTIONARY"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_EXPORT_TABLE = "EXPORT_TABLE"; //$NON-NLS-1$
	public static final String AQL_ELEMENT_TYPE_EXPORT_VIEW = "EXPORT_VIEW"; //$NON-NLS-1$
	
	public static final char MODULE_ELEMENT_SEPARATOR = '.'; //$NON-NLS-1$

  public static String REFRESH_EP_COMMAND_ID = "com.ibm.biginsights.textanalytics.workflow.refreshExtractionPlan";  //$NON-NLS-1$
  public static String REFRESH_EP_PROJECT_PARAM_ID = "com.ibm.biginsights.textanalytics.workflow.refreshEP.param.projectName";  //$NON-NLS-1$
  public static String REFRESH_EP_NEW_PROJECT_PARAM_ID = "com.ibm.biginsights.textanalytics.workflow.refreshEP.param.newProjectName";  //$NON-NLS-1$
  public static String WORKFLOW_PLUGIN_ID = "com.ibm.biginsights.textanalytics.workflow"; //$NON-NLS-1$
}
