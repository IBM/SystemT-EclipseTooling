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
package com.ibm.biginsights.textanalytics.patterndiscovery.messages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {


	
	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.patterndiscovery.messages.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	public static String RULE_CONFIGS;
	public static String GENERAL_SEQUENCE_LABEL;
	public static String SPECIFIC_SEQUENCE_LABEL;
	
	public static String PROJECT_EMPTY;
  public static String PROJECT_NOT_EXIST;
  public static String PROJECT_MAIN_AQL_PATH_CHANGED;

	public static String LANGUAGE_LABEL;

	public static String PROPERTIES_PATH_LABEL;

	public static String BASIC_TAB_LABEL;
	public static String BASIC_TAB_TOOLTIP;

	public static String BASIC_GROUP_LABEL;
	public static String BASIC_GROUP_TOOLTIP;

	public static String ADVANCED_TAB_LABEL;
	public static String ADVANCED_TAB_TOOLTIP;

	// Please make sure you select a valid project and a data folder.

	// ADVANCED PROPERTIES

	// State: 1-active, 0-inactive
	// Types: i-textInput, tf-trueOrFalse, m-multiple, d-dropDown
	// Validation : range, min, max, FILEPATH, DIRPATH, EMPTY, custom, INT,
	// regex
	// AQL_SPECIFICS | SYSTEMT_CONFIG
	public static String ADVANCED_TABS;

	// Large Module Switches

	public static String LARGE_MODULES_SECTION_LABEL;
	public static String LARGE_MODULES_SECTION_TOOLTIP;
	public static String LARGE_MODULES_SECTION_NAME;
	public static String LARGE_MODULES_SECTION_PROPERTIES;
	// FILE_ROOT_DIR

	public static String USE_EXISTING_DB_DATA_PROP;
	public static String USE_EXISTING_DB_DATA_LABEL;
	public static String USE_EXISTING_DB_DATA_TOOLTIP;
	public static String USE_EXISTING_DB_DATA_STATE;
	public static String USE_EXISTING_DB_DATA_TYPE;
	public static String USE_EXISTING_DB_DATA_VALIDATION;

	public static String ENABLE_DEBUGGING_PROP;
	public static String ENABLE_DEBUGGING_LABEL;
	public static String ENABLE_DEBUGGING_TOOLTIP;
	public static String ENABLE_DEBUGGING_STATE;
	public static String ENABLE_DEBUGGING_TYPE;
	public static String ENABLE_DEBUGGING_VALIDATION;

	public static String FILE_ROOT_DIR_PROP;
	public static String FILE_ROOT_DIR_LABEL;
	public static String FILE_ROOT_DIR_TOOLTIP;
	public static String FILE_ROOT_DIR_STATE;
	public static String FILE_ROOT_DIR_TYPE;
	public static String FILE_ROOT_DIR_VALIDATION;

	// END

	// SystemT AQL Configuration Settings
	public static String AQL_SPECIFICS_SECTION_LABEL;
	public static String AQL_SPECIFICS_SECTION_TOOLTIP;
	// Don't need to display configurations that are part of the basic view
	// Snippet is an AQL specific but will be displayed under INPUT_CONFIGS
	// GROUP_BY_FIELD_NAME | AQL_VIEW_NAME | ENTITY_FIELD_NAMES |
	// SNIPPET_FIELD_NAME
	public static String AQL_SPECIFICS_SECTION_PROPERTIES;

	public static String GROUP_BY_FIELD_NAME_PROP;
	public static String GROUP_BY_FIELD_NAME_LABEL;
	public static String GROUP_BY_FIELD_NAME_TOOLTIP;
	public static String GROUP_BY_FIELD_NAME_STATE;
	public static String GROUP_BY_FIELD_NAME_TYPE;
	public static String GROUP_BY_FIELD_NAME_OPTIONS;
	public static String GROUP_BY_FIELD_NAME_VALIDATION;

  public static String AQL_MODULE_NAME_PROP;

	public static String AQL_VIEW_NAME_PROP;
	public static String AQL_VIEW_NAME_LABEL;
	public static String AQL_VIEW_NAME_TOOLTIP;
	public static String AQL_VIEW_NAME_STATE;
	public static String AQL_VIEW_NAME_TYPE;
	public static String AQL_VIEW_NAME_VALIDATION;

	public static String ENTITY_FIELD_NAMES_PROP;
	public static String ENTITY_FIELD_NAMES_LABEL;
	public static String ENTITY_FIELD_NAMES_TOOLTIP;
	public static String ENTITY_FIELD_NAMES_STATE;
	public static String ENTITY_FIELD_NAMES_TYPE;
	public static String ENTITY_FIELD_NAMES_OPTIONS;
	public static String ENTITY_FIELD_NAMES_VALIDATION;

	public static String SNIPPET_FIELD_NAME_PROP;
	public static String SNIPPET_FIELD_NAME_LABEL;
	public static String SNIPPET_FIELD_NAME_TOOLTIP;
	public static String SNIPPET_FIELD_NAME_STATE;
	public static String SNIPPET_FIELD_NAME_TYPE;
	public static String SNIPPET_FIELD_NAME_OPTIONS;
	public static String SNIPPET_FIELD_NAME_VALIDATION;
	public static String SNIPPET_FIELD_DEFAULT_VALUE;

	// END

	// Input Configuration Settings

	public static String INPUT_CONFIG_SECTION_LABEL;
	public static String INPUT_CONFIG_SECTION_TOOLTIP;
	public static String INPUT_CONFIG_SECTION_NAME;
	public static String INPUT_CONFIG_SECTION_PROPERTIES;

	public static String IGNORE_EXTRA_WHITESPACES_PROP;
	public static String IGNORE_EXTRA_WHITESPACES_LABEL;
	public static String IGNORE_EXTRA_WHITESPACES_TOOLTIP;
	public static String IGNORE_EXTRA_WHITESPACES_STATE;
	public static String IGNORE_EXTRA_WHITESPACES_TYPE;
	public static String IGNORE_EXTRA_WHITESPACES_VALIDATION;
	
	public static String IGNORE_EXTRA_NEWLINES_PROP;
  public static String IGNORE_EXTRA_NEWLINES_LABEL;
  public static String IGNORE_EXTRA_NEWLINES_TOOLTIP;
  public static String IGNORE_EXTRA_NEWLINES_STATE;
  public static String IGNORE_EXTRA_NEWLINES_TYPE;
  public static String IGNORE_EXTRA_NEWLINES_VALIDATION;

	public static String INPUT_TO_LOWERCASE_PROP;
	public static String INPUT_TO_LOWERCASE_LABEL;
	public static String INPUT_TO_LOWERCASE_TOOLTIP;
	public static String INPUT_TO_LOWERCASE_STATE;
	public static String INPUT_TO_LOWERCASE_TYPE;
	public static String INPUT_TO_LOWERCASE_VALIDATION;

	public static String REPLACE_ENTITY_PROP;
	public static String REPLACE_ENTITY_LABEL;
	public static String REPLACE_ENTITY_TOOLTIP;
	public static String REPLACE_ENTITY_STATE;
	public static String REPLACE_ENTITY_TYPE;
	public static String REPLACE_ENTITY_VALIDATION;

	// END

	// Sequence Mining Configuration Settings

	public static String SEQ_MINING_SECTION_LABEL;
	public static String SEQ_MINING_SECTION_TOOLTIP;
	public static String SEQ_MINING_SECTION_NAME;
	public static String SEQ_MINING_SECTION_PROPERTIES;

	public static String SEQUENCE_MIN_SIZE_PROP;
  public static String SEQUENCE_MIN_SIZE_LABEL;
  public static String SEQUENCE_MIN_SIZE_TOOLTIP;
  public static String SEQUENCE_MIN_SIZE_STATE;
  public static String SEQUENCE_MIN_SIZE_TYPE;
  public static String SEQUENCE_MIN_SIZE_VALIDATION;
  
	public static String SEQUENCE_MAX_SIZE_PROP;
	public static String SEQUENCE_MAX_SIZE_LABEL;
	public static String SEQUENCE_MAX_SIZE_TOOLTIP;
	public static String SEQUENCE_MAX_SIZE_STATE;
	public static String SEQUENCE_MAX_SIZE_TYPE;
	public static String SEQUENCE_MAX_SIZE_VALIDATION;

	public static String SEQUENCE_MIN_FREQUENCY_SIZE_PROP;
	public static String SEQUENCE_MIN_FREQUENCY_SIZE_LABEL;
	public static String SEQUENCE_MIN_FREQUENCY_SIZE_TOOLTIP;
	public static String SEQUENCE_MIN_FREQUENCY_SIZE_STATE;
	public static String SEQUENCE_MIN_FREQUENCY_SIZE_TYPE;
	public static String SEQUENCE_MIN_FREQUENCY_SIZE_VALIDATION;

  public static String SEQUENCE_MIN_SIZE_UI_LABEL;
  public static String SEQUENCE_MIN_SIZE_UI_TOOLTIP;
  public static String SEQUENCE_MIN_SIZE_UI_DIALOG_MESSAGE;
  public static String SEQUENCE_MIN_SIZE_UI_ERROR_MESSAGE;
  public static String SEQUENCE_MIN_SIZE_UI_INVALID_VALUE;

	// END

	// Rule Generation Configuration Settings

	public static String RULE_CONFIGS_SECTION_LABEL;
	public static String RULE_CONFIGS_SECTION_TOOLTIP;
	public static String RULE_CONFIGS_SECTION_NAME;
	public static String RULE_CONFIGS_SECTION_PROPERTIES;

  public static String CORRELATION_MEASURE_MIN_PROP;
  public static String CORRELATION_MEASURE_MIN_LABEL;
  public static String CORRELATION_MEASURE_MAX_PROP;
  public static String CORRELATION_MEASURE_MAX_LABEL;
	public static String CORRELATION_MEASURE_RANGE_LABEL;
	public static String CORRELATION_MEASURE_RANGE_TOOLTIP;
	public static String CORRELATION_MEASURE_RANGE_STATE;
	public static String CORRELATION_MEASURE_RANGE_TYPE;
	public static String CORRELATION_MEASURE_RANGE_VALIDATION;

	public static String CO_COUNT_RANGE_PROP;
	public static String CO_COUNT_RANGE_LABEL;
	public static String CO_COUNT_RANGE_TOOLTIP;
	public static String CO_COUNT_RANGE_STATE;
	public static String CO_COUNT_RANGE_TYPE;
	public static String CO_COUNT_RANGE_VALIDATION;

	public static String SEQ_X_RELATIVE_FREQUENCY_RANGE_PROP;
	public static String SEQ_X_RELATIVE_FREQUENCY_RANGE_LABEL;
	public static String SEQ_X_RELATIVE_FREQUENCY_RANGE_TOOLTIP;
	public static String SEQ_X_RELATIVE_FREQUENCY_RANGE_STATE;
	public static String SEQ_X_RELATIVE_FREQUENCY_RANGE_TYPE;
	public static String SEQ_X_RELATIVE_FREQUENCY_RANGE_VALIDATION;

	public static String SEQ_Y_RELATIVE_FREQUENCY_RANGE_PROP;
	public static String SEQ_Y_RELATIVE_FREQUENCY_RANGE_LABEL;
	public static String SEQ_Y_RELATIVE_FREQUENCY_RANGE_TOOLTIP;
	public static String SEQ_Y_RELATIVE_FREQUENCY_RANGE_STATE;
	public static String SEQ_Y_RELATIVE_FREQUENCY_RANGE_TYPE;
	public static String SEQ_Y_RELATIVE_FREQUENCY_RANGE_VALIDATION;

	public static String DROP_SEQ_COUNT_RANGE_PROP;
	public static String DROP_SEQ_COUNT_RANGE_LABEL;
	public static String DROP_SEQ_COUNT_RANGE_TOOLTIP;
	public static String DROP_SEQ_COUNT_RANGE_STATE;
	public static String DROP_SEQ_COUNT_RANGE_TYPE;
	public static String DROP_SEQ_COUNT_RANGE_VALIDATION;

	public static String DROP_SEQ_RELATIVE_FREQUENCY_RANGE_PROP;
	public static String DROP_SEQ_RELATIVE_FREQUENCY_RANGE_LABEL;
	public static String DROP_SEQ_RELATIVE_FREQUENCY_RANGE_TOOLTIP;
	public static String DROP_SEQ_RELATIVE_FREQUENCY_RANGE_STATE;
	public static String DROP_SEQ_RELATIVE_FREQUENCY_RANGE_TYPE;
	public static String DROP_SEQ_RELATIVE_FREQUENCY_RANGE_VALIDATION;

	public static String INDICATOR_SEQ_COUNT_RANGE_PROP;
	public static String INDICATOR_SEQ_COUNT_RANGE_LABEL;
	public static String INDICATOR_SEQ_COUNT_RANGE_TOOLTIP;
	public static String INDICATOR_SEQ_COUNT_RANGE_STATE;
	public static String INDICATOR_SEQ_COUNT_RANGE_TYPE;
	public static String INDICATOR_SEQ_COUNT_RANGE_VALIDATION;

	public static String INDICATOR_SEQ_RELATIVE_FREQUENCY_RANGE_PROP;
	public static String INDICATOR_SEQ_RELATIVE_FREQUENCY_RANGE_LABEL;
	public static String INDICATOR_SEQ_RELATIVE_FREQUENCY_RANGE_TOOLTIP;
	public static String INDICATOR_SEQ_RELATIVE_FREQUENCY_RANGE_STATE;
	public static String INDICATOR_SEQ_RELATIVE_FREQUENCY_RANGE_TYPE;
	public static String INDICATOR_SEQ_RELATIVE_FREQUENCY_RANGE_VALIDATION;

	// END

	// Grouping Configuration Settings

	public static String GROUPING_CONFIGS_SECTION_LABEL;
	public static String GROUPING_CONFIGS_SECTION_TOOLTIP;
	public static String GROUPING_CONFIGS_SECTION_NAME;
	public static String GROUPING_CONFIGS_SECTION_PROPERTIES;

	public static String USE_INFREQUENT_WORDS_PROP;
	public static String USE_INFREQUENT_WORDS_LABEL;
	public static String USE_INFREQUENT_WORDS_TOOLTIP;
	public static String USE_INFREQUENT_WORDS_STATE;
	public static String USE_INFREQUENT_WORDS_TYPE;
	public static String USE_INFREQUENT_WORDS_VALIDATION;

	public static String APPLY_RULES_ON_SIGNATURE_PROP;
	public static String APPLY_RULES_ON_SIGNATURE_LABEL;
	public static String APPLY_RULES_ON_SIGNATURE_TOOLTIP;
	public static String APPLY_RULES_ON_SIGNATURE_STATE;
	public static String APPLY_RULES_ON_SIGNATURE_TYPE;
	public static String APPLY_RULES_ON_SIGNATURE_VALIDATION;

	// END

	// SystemT Configuration Settings
	// Don't need to display ever - part of the eclipse tool backend
	public static String SYSTEMT_CONFIG_SECTION_LABEL;
	public static String SYSTEMT_CONFIG_SECTION_TOOLTIP;
	public static String SYSTEMT_CONFIG_SECTION_PROPERTIES;

	public static String AQL_QUERY_FILE_PROP;
	public static String AQL_QUERY_FILE_LABEL;
	public static String AQL_QUERY_FILE_TOOLTIP;
	public static String AQL_QUERY_FILE_STATE;
	public static String AQL_QUERY_FILE_TYPE;
	public static String AQL_QUERY_FILE_VALIDATION;

	public static String AQL_DICTIONARY_DIR_PROP;
	public static String AQL_DICTIONARY_DIR_LABEL;
	public static String AQL_DICTIONARY_DIR_TOOLTIP;
	public static String AQL_DICTIONARY_DIR_STATE;
	public static String AQL_DICTIONARY_DIR_TYPE;
	public static String AQL_DICTIONARY_DIR_VALIDATION;

	public static String AQL_JAR_DIR_PROP;
	public static String AQL_JAR_DIR_LABEL;
	public static String AQL_JAR_DIR_TOOLTIP;
	public static String AQL_JAR_DIR_STATE;
	public static String AQL_JAR_DIR_TYPE;
	public static String AQL_JAR_DIR_VALIDATION;

	public static String AQL_INCLUDES_DIR_PROP;
	public static String AQL_INCLUDES_DIR_LABEL;
	public static String AQL_INCLUDES_DIR_TOOLTIP;
	public static String AQL_INCLUDES_DIR_STATE;
	public static String AQL_INCLUDES_DIR_TYPE;
	public static String AQL_INCLUDES_DIR_VALIDATION;
	
	// -- pd progress reporting messages --
	
	public static String PD_STARTING;
  public static String PD_ENDING;
  public static String PD_CANCELED;

  public static final int PD_PROCESS_TOTAL = 100;

  public static final int PD_READ_INPUT_WORK = 2; // prev steps does 2 % of work
  public static String PD_READ_INPUT;
  public static final int PD_READ_INPUT_STEPS_WORK = 24;

  public static final int PD_SEQUENCE_MINING_STEP1_WORK = 25; // this step takes forever so I have decided to give it a
                                                              // higher value
  public static final int PD_SEQUENCE_MINING_STEP2_WORK = 6;
  public static final int PD_SEQUENCE_MINING_STEP3_WORK = 6;
  public static String PD_SEQUENCE_MINING;

  public static final int PD_RULES_GENERATION_STEP1_WORK = 4;
  public static final int PD_RULES_GENERATION_STEP2_WORK = 4;
  public static final int PD_RULES_GENERATION_STEP3_WORK = 4;
  public static String PD_RULES_GENERATION;

  public static final int PD_GROUP_STEPS_WORK = 23;
  public static String PD_GROUP;

  public static String TV_COL_HEADER_ID;
  public static String TV_COL_HEADER_SIZE;
  public static String TV_COL_HEADER_SIG;
  public static String TV_COL_HEADER_ORISIG;
  public static String TV_TITLE;
  public static String TV_TOOLTIP;
  public static String PD_VIEW_TITLE;
  public static String PD_DIALOG_TITLE;
  public static String PD_VIEW_TOOLTIP;
  public static String PD_VIEW_STATISTIC;
  public static String PD_SEE_HISTORY;
  public static String PD_NO_PATTERNS;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {

	}
}
