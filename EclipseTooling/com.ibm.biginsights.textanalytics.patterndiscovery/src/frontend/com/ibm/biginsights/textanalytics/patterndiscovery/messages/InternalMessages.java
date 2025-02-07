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

import org.eclipse.osgi.util.NLS;

public class InternalMessages extends NLS {



	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.patterndiscovery.messages.InternalMessages"; //$NON-NLS-1$

//	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
//			.getBundle(BUNDLE_NAME);

	private InternalMessages() {
	}

//	public static String getString(String key) {
//		try {
//			return RESOURCE_BUNDLE.getString(key);
//		} catch (MissingResourceException e) {
//			return '!' + key + '!';
//		}
//	}
	
	public static String PROPERTIES_PATH_LABEL;
	public static String DATA_PATH_LABEL;

	public static String BASIC_TAB_LABEL;
	public static String BASIC_TAB_TOOLTIP;

	public static String BASIC_GROUP_LABEL;
	public static String BASIC_GROUP_TOOLTIP;

	public static String ADVANCED_TAB_LABEL;
	public static String ADVANCED_TAB_TOOLTIP;

	// ADVANCED INTERNAL PROPERTIES

	// State: 1-active, 0-inactive
	// Types: i-textInput, tf-trueOrFalse, m-multiple, d-dropDown
	// Validation : range, min, max, FILEPATH, DIRPATH, EMPTY, custom, INT, regex
	public static String ADVANCED_TABS;

	// Large Module Switches

	public static String LARGE_MODULES_SECTION_LABEL;
	public static String LARGE_MODULES_SECTION_TOOLTIP;
	public static String LARGE_MODULES_SECTION_NAME;
	public static String LARGE_MODULES_SECTION_PROPERTIES;

	public static String PROCESS_MECHTURK_RESULTS_PROP;
	public static String PROCESS_MECHTURK_RESULTS_LABEL;
	public static String PROCESS_MECHTURK_RESULTS_TOOLTIP;
	public static String PROCESS_MECHTURK_RESULTS_STATE;
	public static String PROCESS_MECHTURK_RESULTS_TYPE;
	public static String PROCESS_MECHTURK_RESULTS_VALIDATION;
		
	public static String IMPORT_DATA_FROM_FILE_PROP;
	public static String IMPORT_DATA_FROM_FILE_LABEL;
	public static String IMPORT_DATA_FROM_FILE_TOOLTIP;
	public static String IMPORT_DATA_FROM_FILE_STATE;
	public static String IMPORT_DATA_FROM_FILE_TYPE;
	public static String IMPORT_DATA_FROM_FILE_VALIDATION;
		
	public static String TURN_PATTERNDISCOVERY_OFF_PROP;
	public static String TURN_PATTERNDISCOVERY_OFF_LABEL;
	public static String TURN_PATTERNDISCOVERY_OFF_TOOLTIP;
	public static String TURN_PATTERNDISCOVERY_OFF_STATE;
	public static String TURN_PATTERNDISCOVERY_OFF_TYPE;
	public static String TURN_PATTERNDISCOVERY_OFF_VALIDATION;

	// END
		
	// Small module switches
	public static String SMALL_MODULES_SW_SECTION_LABEL;
	public static String SMALL_MODULES_SW_SECTION_TOOLTIP;
	public static String SMALL_MODULES_SW_SECTION_NAME;
	public static String SMALL_MODULES_SW_SECTION_PROPERTIES;
		
	public static String DISABLE_SEQUENCE_MINING_PROP;
	public static String DISABLE_SEQUENCE_MINING_LABEL;
	public static String DISABLE_SEQUENCE_MINING_TOOLTIP;
	public static String DISABLE_SEQUENCE_MINING_STATE;
	public static String DISABLE_SEQUENCE_MINING_TYPE;
	public static String DISABLE_SEQUENCE_MINING_VALIDATION;
		
	public static String DISABLE_STORE_DICTIONARY_PROP;
	public static String DISABLE_STORE_DICTIONARY_LABEL;
	public static String DISABLE_STORE_DICTIONARY_TOOLTIP;
	public static String DISABLE_STORE_DICTIONARY_STATE;
	public static String DISABLE_STORE_DICTIONARY_TYPE;
	public static String DISABLE_STORE_DICTIONARY_VALIDATION;
		
	public static String DISABLE_LOAD_SUPPORT_SEQUENCE_PROP;
	public static String DISABLE_LOAD_SUPPORT_SEQUENCE_LABEL;
	public static String DISABLE_LOAD_SUPPORT_SEQUENCE_TOOLTIP;
	public static String DISABLE_LOAD_SUPPORT_SEQUENCE_STATE;
	public static String DISABLE_LOAD_SUPPORT_SEQUENCE_TYPE;
	public static String DISABLE_LOAD_SUPPORT_SEQUENCE_VALIDATION;
		
	public static String DISABLE_COUNTING_PROP;
	public static String DISABLE_COUNTING_LABEL;
	public static String DISABLE_COUNTING_TOOLTIP;
	public static String DISABLE_COUNTING_STATE;
	public static String DISABLE_COUNTING_TYPE;
	public static String DISABLE_COUNTING_VALIDATION;
		
	public static String DISABLE_COMPUTE_MEASURE_SEQUENCE_PROP;
	public static String DISABLE_COMPUTE_MEASURE_SEQUENCE_LABEL;
	public static String DISABLE_COMPUTE_MEASURE_SEQUENCE_TOOLTIP;
	public static String DISABLE_COMPUTE_MEASURE_SEQUENCE_STATE;
	public static String DISABLE_COMPUTE_MEASURE_SEQUENCE_TYPE;
	public static String DISABLE_COMPUTE_MEASURE_SEQUENCE_VALIDATION;
		
	public static String DISABLE_RULE_GENERATION_PROP;
	public static String DISABLE_RULE_GENERATION_LABEL;
	public static String DISABLE_RULE_GENERATION_TOOLTIP;
	public static String DISABLE_RULE_GENERATION_STATE;
	public static String DISABLE_RULE_GENERATION_TYPE;
	public static String DISABLE_RULE_GENERATION_VALIDATION;
		
	public static String DISABLE_GROUPING_PROP;
	public static String DISABLE_GROUPING_LABEL;
	public static String DISABLE_GROUPING_TOOLTIP;
	public static String DISABLE_GROUPING_STATE;
	public static String DISABLE_GROUPING_TYPE;
	public static String DISABLE_GROUPING_VALIDATION;
		
	public static String DISABLE_FUZZY_GROUPING_PROP;
	public static String DISABLE_FUZZY_GROUPING_LABEL;
	public static String DISABLE_FUZZY_GROUPING_TOOLTIP;
	public static String DISABLE_FUZZY_GROUPING_STATE;
	public static String DISABLE_FUZZY_GROUPING_TYPE;
	public static String DISABLE_FUZZY_GROUPING_VALIDATION;
		
	// END
		
	// File System Specifics	
		
	public static String FILE_SYSTEM_SECTION_LABEL;
	public static String FILE_SYSTEM_SECTION_TOOLTIP;
	public static String FILE_SYSTEM_SECTION_NAME;
	// Don't need to display configurations that are part of the basic view	
	//  DOCUMENT_COLLECTION_DIR | INPUT_DOCUMENT_NAME
	public static String FILE_SYSTEM_SECTION_PROPERTIES;
		
	public static String DOCUMENT_COLLECTION_DIR_PROP;
	public static String DOCUMENT_COLLECTION_DIR_LABEL;
	public static String DOCUMENT_COLLECTION_DIR_TOOLTIP;
	public static String DOCUMENT_COLLECTION_DIR_STATE;
	public static String DOCUMENT_COLLECTION_DIR_TYPE;
	public static String DOCUMENT_COLLECTION_DIR_VALIDATION;
		
	public static String INPUT_DOCUMENT_NAME_PROP;
	public static String INPUT_DOCUMENT_NAME_LABEL;
	public static String INPUT_DOCUMENT_NAME_TOOLTIP;
	public static String INPUT_DOCUMENT_NAME_STATE;
	public static String INPUT_DOCUMENT_NAME_TYPE;
	public static String INPUT_DOCUMENT_NAME_VALIDATION;
		
	public static String DEBUG_DIR_PROP;
	public static String DEBUG_DIR_LABEL;
	public static String DEBUG_DIR_TOOLTIP;
	public static String DEBUG_DIR_STATE;
	public static String DEBUG_DIR_TYPE;
	public static String DEBUG_DIR_VALIDATION;
		
	public static String RULE_DIR_PROP;
	public static String RULE_DIR_LABEL;
	public static String RULE_DIR_TOOLTIP;
	public static String RULE_DIR_STATE;
	public static String RULE_DIR_TYPE;
	public static String RULE_DIR_VALIDATION;
		
	public static String GROUPING_DIR_PROP;
	public static String GROUPING_DIR_LABEL;
	public static String GROUPING_DIR_TOOLTIP;
	public static String GROUPING_DIR_STATE;
	public static String GROUPING_DIR_TYPE;
	public static String GROUPING_DIR_VALIDATION;
		
	public static String INPUT_FILE_DIR_PROP;
	public static String INPUT_FILE_DIR_LABEL;
	public static String INPUT_FILE_DIR_TOOLTIP;
	public static String INPUT_FILE_DIR_STATE;
	public static String INPUT_FILE_DIR_TYPE;
	public static String INPUT_FILE_DIR_VALIDATION;
		
	public static String POST_PROCESS_DIR_PROP;
	public static String POST_PROCESS_DIR_LABEL;
	public static String POST_PROCESS_DIR_TOOLTIP;
	public static String POST_PROCESS_DIR_STATE;
	public static String POST_PROCESS_DIR_TYPE;
	public static String POST_PROCESS_DIR_VALIDATION;
		
	public static String MECHANICAL_TURK_DIR_PROP;
	public static String MECHANICAL_TURK_DIR_LABEL;
	public static String MECHANICAL_TURK_DIR_TOOLTIP;
	public static String MECHANICAL_TURK_DIR_STATE;
	public static String MECHANICAL_TURK_DIR_TYPE;
	public static String MECHANICAL_TURK_DIR_VALIDATION;
		
	// END

	// Rule Generation Configuration Settings
		
	public static String RULE_CONFIGS_SECTION_LABEL;
	public static String RULE_CONFIGS_SECTION_TOOLTIP;
	public static String RULE_CONFIGS_SECTION_NAME;
	public static String RULE_CONFIGS_SECTION_PROPERTIES;

	public static String CORRELATION_MEASURES_PROP;
	public static String CORRELATION_MEASURES_LABEL;
	public static String CORRELATION_MEASURES_TOOLTIP;
	public static String CORRELATION_MEASURES_STATE;
	public static String CORRELATION_MEASURES_TYPE;
	public static String CORRELATION_MEASURES_OPTIONS;
	public static String CORRELATION_MEASURES_VALIDATION;

	public static String DROP_RULE_TYPES_PROP;
	public static String DROP_RULE_TYPES_LABEL;
	public static String DROP_RULE_TYPES_TOOLTIP;
	public static String DROP_RULE_TYPES_STATE;
	public static String DROP_RULE_TYPES_TYPE;
	public static String DROP_RULE_TYPES_OPTIONS;
	public static String DROP_RULE_TYPES_VALIDATION;

		
	// Grouping Configuration Settings
		
	public static String GROUPING_CONFIGS_SECTION_LABEL;
	public static String GROUPING_CONFIGS_SECTION_TOOLTIP;
	public static String GROUPING_CONFIGS_SECTION_NAME;
	public static String GROUPING_CONFIGS_SECTION_PROPERTIES;
		
	public static String APPLIED_RULE_FILES_PROP;
	public static String APPLIED_RULE_FILES_LABEL;
	public static String APPLIED_RULE_FILES_TOOLTIP;
	public static String APPLIED_RULE_FILES_STATE;
	public static String APPLIED_RULE_FILES_TYPE;
	public static String APPLIED_RULE_FILES_VALIDATION;
		
	public static String HASH_FACTORY_PROP;
	public static String HASH_FACTORY_LABEL;
	public static String HASH_FACTORY_TOOLTIP;
	public static String HASH_FACTORY_STATE;
	public static String HASH_FACTORY_TYPE;
	public static String HASH_FACTORY_OPTIONS;
	public static String HASH_FACTORY_VALIDATION;
		
	public static String DISABLE_DISTANCE_MERGING_PROP;
	public static String DISABLE_DISTANCE_MERGING_LABEL;
	public static String DISABLE_DISTANCE_MERGING_TOOLTIP;
	public static String DISABLE_DISTANCE_MERGING_STATE;
	public static String DISABLE_DISTANCE_MERGING_TYPE;
	public static String DISABLE_DISTANCE_MERGING_VALIDATION;

	// END
		
	// Database Information
	// Useful for swapping databases or finding database name - basic developer does not need to see
	public static String DB_INFO_SECTION_LABEL;
	public static String DB_INFO_SECTION_TOOLTIP;
	public static String DB_INFO_SECTION_NAME;
	public static String DB_INFO_SECTION_PROPERTIES;
		
	public static String DB_PREFIX_PROP;
	public static String DB_PREFIX_LABEL;
	public static String DB_PREFIX_TOOLTIP;
	public static String DB_PREFIX_STATE;
	public static String DB_PREFIX_TYPE;
	public static String DB_PREFIX_VALIDATION;
		
	public static String RESULTS_DB_NAME_PROP;
	public static String RESULTS_DB_NAME_LABEL;
	public static String RESULTS_DB_NAME_TOOLTIP;
	public static String RESULTS_DB_NAME_STATE;
	public static String RESULTS_DB_NAME_TYPE;
	public static String RESULTS_DB_NAME_VALIDATION;
		
	public static String SEQUENCE_DB_NAME_PROP;
	public static String SEQUENCE_DB_NAME_LABEL;
	public static String SEQUENCE_DB_NAME_TOOLTIP;
	public static String SEQUENCE_DB_NAME_STATE;
	public static String SEQUENCE_DB_NAME_TYPE;
	public static String SEQUENCE_DB_NAME_VALIDATION;
		
	public static String SEQUENCE_DB_USER_PROP;
	public static String SEQUENCE_DB_USER_LABEL;
	public static String SEQUENCE_DB_USER_TOOLTIP;
	public static String SEQUENCE_DB_USER_STATE;
	public static String SEQUENCE_DB_USER_TYPE;
	public static String SEQUENCE_DB_USER_VALIDATION;
		
	public static String SEQUENCE_DB_PASSWORD_PROP;
	public static String SEQUENCE_DB_PASSWORD_LABEL;
	public static String SEQUENCE_DB_PASSWORD_TOOLTIP;
	public static String SEQUENCE_DB_PASSWORD_STATE;
	public static String SEQUENCE_DB_PASSWORD_TYPE;
	public static String SEQUENCE_DB_PASSWORD_VALIDATION;

	// END

	// Gold Standard Configuration Settings and Switches
	public static String GOLD_STANDARD_SECTION_LABEL;
	public static String GOLD_STANDARD_SECTION_TOOLTIP;
	public static String GOLD_STANDARD_SECTION_NAME;
	public static String GOLD_STANDARD_SECTION_PROPERTIES;

	public static String ADD_GOLD_STANDARD_PROP;
	public static String ADD_GOLD_STANDARD_LABEL;
	public static String ADD_GOLD_STANDARD_TOOLTIP;
	public static String ADD_GOLD_STANDARD_STATE;
	public static String ADD_GOLD_STANDARD_TYPE;
	public static String ADD_GOLD_STANDARD_VALIDATION;
		
	public static String RUN_GOLDSTANDARD_STATISTICS_PROP;
	public static String RUN_GOLDSTANDARD_STATISTICS_LABEL;
	public static String RUN_GOLDSTANDARD_STATISTICS_TOOLTIP;
	public static String RUN_GOLDSTANDARD_STATISTICS_STATE;
	public static String RUN_GOLDSTANDARD_STATISTICS_TYPE;
	public static String RUN_GOLDSTANDARD_STATISTICS_VALIDATION;

	public static String DISABLE_POST_PROCESSSING_PROP;
	public static String DISABLE_POST_PROCESSSING_LABEL;
	public static String DISABLE_POST_PROCESSSING_TOOLTIP;
	public static String DISABLE_POST_PROCESSSING_STATE;
	public static String DISABLE_POST_PROCESSSING_TYPE;
	public static String DISABLE_POST_PROCESSSING_VALIDATION;

	// END

	// Mechanical Turk Configuration Settings and Switches
	// Currently not fully supported - do not display	
	public static String MECH_TURK_SECTION_LABEL;
	public static String MECH_TURK_SECTION_TOOLTIP;
	public static String MECH_TURK_SECTION_NAME;
	public static String MECH_TURK_SECTION_PROPERTIES;

	public static String CREATE_MECHANICAL_TURK_INPUT_PROP;
	public static String CREATE_MECHANICAL_TURK_INPUT_LABEL;
	public static String CREATE_MECHANICAL_TURK_INPUT_TOOLTIP;
	public static String CREATE_MECHANICAL_TURK_INPUT_STATE;
	public static String CREATE_MECHANICAL_TURK_INPUT_TYPE;
	public static String CREATE_MECHANICAL_TURK_INPUT_VALIDATION;
		
	public static String MECHANICAL_TURK_AQL_QUERY_PROP;
	public static String MECHANICAL_TURK_AQL_QUERY_LABEL;
	public static String MECHANICAL_TURK_AQL_QUERY_TOOLTIP;
	public static String MECHANICAL_TURK_AQL_QUERY_STATE;
	public static String MECHANICAL_TURK_AQL_QUERY_TYPE;
	public static String MECHANICAL_TURK_AQL_QUERY_VALIDATION;
		
	public static String MECHANICAL_TURK_VIEW_NAME_PROP;
	public static String MECHANICAL_TURK_VIEW_NAME_LABEL;
	public static String MECHANICAL_TURK_VIEW_NAME_TOOLTIP;
	public static String MECHANICAL_TURK_VIEW_NAME_STATE;
	public static String MECHANICAL_TURK_VIEW_NAME_TYPE;
	public static String MECHANICAL_TURK_VIEW_NAME_VALIDATION;
		
	public static String MECHANICAL_TURK_RESULTS_FILE_PROP;
	public static String MECHANICAL_TURK_RESULTS_FILE_LABEL;
	public static String MECHANICAL_TURK_RESULTS_FILE_TOOLTIP;
	public static String MECHANICAL_TURK_RESULTS_FILE_STATE;
	public static String MECHANICAL_TURK_RESULTS_FILE_TYPE;
	public static String MECHANICAL_TURK_RESULTS_FILE_VALIDATION;

	// END
		
	// Fuzzy Grouping Settings
	// Do not show because not supported	
	public static String FUZZY_GROUP_SECTION_LABEL;
	public static String FUZZY_GROUP_SECTION_TOOLTIP;
	public static String FUZZY_GROUP_SECTION_NAME;
	public static String FUZZY_GROUP_SECTION_PROPERTIES;
		
	public static String FUZZY_GROUPING_ALGORITHM_PROP;
	public static String FUZZY_GROUPING_ALGORITHM_LABEL;
	public static String FUZZY_GROUPING_ALGORITHM_TOOLTIP;
	public static String FUZZY_GROUPING_ALGORITHM_STATE;
	public static String FUZZY_GROUPING_ALGORITHM_TYPE;
	public static String FUZZY_GROUPING_ALGORITHM_OPTIONS;
	public static String FUZZY_GROUPING_ALGORITHM_VALIDATION;
		
	public static String FUZZY_GROUPING_COLUMN_PROP;
	public static String FUZZY_GROUPING_COLUMN_LABEL;
	public static String FUZZY_GROUPING_COLUMN_TOOLTIP;
	public static String FUZZY_GROUPING_COLUMN_STATE;
	public static String FUZZY_GROUPING_COLUMN_TYPE;
	public static String FUZZY_GROUPING_COLUMN_VALIDATION;
		
	public static String FUZZY_HASHID_COLUMN_PROP;
	public static String FUZZY_HASHID_COLUMN_LABEL;
	public static String FUZZY_HASHID_COLUMN_TOOLTIP;
	public static String FUZZY_HASHID_COLUMN_STATE;
	public static String FUZZY_HASHID_COLUMN_TYPE;
	public static String FUZZY_HASHID_COLUMN_VALIDATION;

	// END

	// END ADVANCED INTERNAL PROPERTIES
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, InternalMessages.class);
	}

}
