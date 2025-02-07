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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors;

import org.eclipse.osgi.util.NLS;

import com.ibm.biginsights.textanalytics.patterndiscovery.Activator;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class ErrorMessages extends NLS{



	public static void LogErrorMessage(String message, Throwable t){
		LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(message + PATTERN_DISCOVERY_COMMON_MESSAGE, t);
	}
	
	public static void LogError(String message, Throwable t){
		LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logError(message + PATTERN_DISCOVERY_COMMON_MESSAGE, t);
	}
	
	public static void ShowErrorMessage(String message){
		LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(message);
	}
	
	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.messages"; //$NON-NLS-1$
	
	public static String PATTERN_DISCOVERY_COMMON_MESSAGE;
	
	public static String PATTERN_DISCOVERY_PROPERTIES_MISSING_ERR;
	public static String PATTERN_DISCOVERY_PROPERTIES_INVALID_ERR;
	public static String PATTERN_DISCOVERY_MECHTURK_RESULTS_PROCESSING_ERR;
	public static String PATTERN_DISCOVERY_MECHTURK_INPUT_CREATION_ERR;
	public static String PATTERN_DISCOVERY_MECHTURK_INPUT_CREATION_DB_ERR;
	public static String PATTERN_DISCOVERY_MECHTURK_INPUT_CREATION_WRITE_ERR;
	public static String PATTERN_DISCOVERY_LOGGER_CREATION_ERR;
	public static String PATTERN_DISCOVERY_RUN_AQL_ERR;
	public static String PATTERN_DISCOVERY_RUN_AQL_CREATE_DEFAULT_SNIPPET_ERR;
	public static String PATTERN_DISCOVERY_RUN_AQL_WRITE_ERR;
	public static String PATTERN_DISCOVERY_RUN_AQL_REPLACE_ENTITY_WRITE_ERR;
	public static String PATTERN_DISCOVERY_RUN_AQL_GOLDSTANDARD_ERR;
	public static String PATTERN_DISCOVERY_RUN_AQL_FILE_TO_DB_ERR;
	public static String PATTERN_DISCOVERY_MINE_SEQUENCE_TOKENIZER_ERR;
	public static String PATTERN_DISCOVERY_MINE_SEQUENCE_LOAD_DB_ERR;
	public static String PATTERN_DISCOVERY_MINE_SEQUENCE_ERR;
	public static String PATTERN_DISCOVERY_STORE_DICTIONARY_WRITE_ERR;
	public static String PATTERN_DISCOVERY_STORE_DICTIONARY_DB_ERR;
	public static String PATTERN_DISCOVERY_LOAD_DICTIONARY_DB_ERR;
	public static String PATTERN_DISCOVERY_LOAD_SEQUENCES_MAPPING_ERR;
	public static String PATTERN_DISCOVERY_LOAD_SEQUENCE_TRIE_DB_ERR;
	public static String PATTERN_DISCOVERY_COUNT_WRITE_ERR;
	public static String PATTERN_DISCOVERY_COUNT_DB_ERR;
	public static String PATTERN_DISCOVERY_STATISTICS_WRITE_ERR;
	public static String PATTERN_DISCOVERY_STATISTICS_DB_ERR;
	public static String PATTERN_DISCOVERY_COUNT_LOAD_ERR;
	public static String PATTERN_DISCOVERY_LOAD_SEQUENCE_REINDEX_ERR;
	public static String PATTERN_DISCOVERY_MINE_SEQUENCE_CREATE_TOK_ERR;
	public static String PATTERN_DISCOVERY_RULES_WRITE_ERR;
	public static String PATTERN_DISCOVERY_CORRELATION_WRITE_ERR;
	public static String PATTERN_DISCOVERY_CORRELATION_DB_ERR;
	public static String PATTERN_DISCOVERY_RULES_WRITE_JOIN_ERR;
	public static String PATTERN_DISCOVERY_RULES_READ_ERR;
	public static String PATTERN_DISCOVERY_RELEVANT_SEQUENCE_DB_ERR;
	public static String PATTERN_DISCOVERY_RELEVANT_SEQUENCE_WRITE_ERR;
	public static String PATTERN_DISCOVERY_FUZZY_GROUPING_WRITE_ERR;
	public static String PATTERN_DISCOVERY_LOAD_SEQUENCES_DB_ERR;
	public static String PATTERN_DISCOVERY_GROUPING_DB_ERR;
	public static String PATTERN_DISCOVERY_GROUPING_WRITE_ERR;
	public static String PATTERN_DISCOVERY_COMMON_SEQ_GROUPING_DB_ERR;
	public static String PATTERN_DISCOVERY_COMMON_SEQ_GROUPING_WRITE_ERR;
	public static String PATTERN_DISCOVERY_DEBUG_WRITE_ERR;
	public static String PATTERN_DISCOVERY_DEBUG_DB_ERR;
	public static String PATTERN_DISCOVERY_GROUPING_HASHER_CREATE_ERR;
	public static String PATTERN_DISCOVERY_GROUPING_HASHER_WRITE_ERR;
	public static String PATTERN_DISCOVERY_GOLD_CREATE_ERR;
	public static String PATTERN_DISCOVERY_GOLD_APPLY_ERR;
	public static String PATTERN_DISCOVERY_GOLD_STATS_ERR;
	public static String PATTERN_DISCOVERY_GROUPING_SEQ_MAP_NULL_POINTER;
	

	static {
		NLS.initializeMessages(BUNDLE_NAME, ErrorMessages.class);
	}

	private ErrorMessages() {

	}
	
}
