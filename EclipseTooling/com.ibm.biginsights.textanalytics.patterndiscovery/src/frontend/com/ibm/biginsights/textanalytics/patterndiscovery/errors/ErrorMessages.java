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
package com.ibm.biginsights.textanalytics.patterndiscovery.errors;

import org.eclipse.osgi.util.NLS;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;

/**
 * error messages for pattern discovery UI
 * 
 *
 */
public class ErrorMessages extends NLS {


	
	public static void LogErrorMessage(String message, Throwable t){
	  PDLogger.getLogger ().logAndShowError(message + PATTERN_DISCOVERY_COMMON_MESSAGE, t);
	}
	
	public static void LogError(String message, Throwable t){
	  PDLogger.getLogger ().logError(message, t);
	}
	
	public static void ShowErrorMessage(String message){
	  PDLogger.getLogger ().logAndShowError(message);
	}
	
	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.patterndiscovery.errors.messages"; //$NON-NLS-1$

	public static String PATTERN_DISCOVERY_COMMON_MESSAGE;
	public static String PATTERN_DISCOVERY_PROJECT_ERR;
	public static String PATTERN_DISCOVERY_INVALID_POSITION_MESSAGE;
	public static String PATTERN_DISCOVERY_CONFIG_ERR;
	public static String PATTERN_DISCOVERY_LOADING_MESSAGE;
	public static String PATTERN_DISCOVERY_PROCESSING_MESSAGE;
	public static String PATTERN_DISCOVERY_PROCESSING_ERR;
	public static String PATTERN_DISCOVERY_PARAMETERS_VALIDATION_ERROR_HEADER;
	public static String PATTERN_DISCOVERY_VALIDATION_DATADIR;
	public static String PATTERN_DISCOVERY_VALIDATION_AOG;
	public static String PATTERN_DISCOVERY_VALIDATION_AQL;
	public static String PATTERN_DISCOVERY_VALIDATION_PROJECT;
	public static String PATTERN_DISCOVERY_PROJECT_DOES_NOT_EXIST;
	public static String PATTERN_DISCOVERY_VALIDATION_BASIC_PROPERTIES;
	public static String PATTERN_DISCOVERY_ERROR_GENERAL;
	public static String PATTERN_DISCOVERY_ERROR_GENERATING_UI;
	public static String PATTERN_DISCOVERY_ERROR_OPENING_COMMON_SIGNATURE_VIEW;
	public static String PATTERN_DISCOVERY_ERROR_OPENING_SEMANTIC_SIGNATURE_VIEW;
	public static String PATTERN_DISCOVERY_ERROR_OPENING_PREFUSE_VIEW;
	public static String PATTERN_DISCOVERY_ERROR_READING_DATA_FROM_DB;
	public static String PATTERN_DISCOVERY_ERROR_GETTING_OUTPUT_VIEWS;
	public static String PATTERN_DISCOVERY_ERROR_LOADING_PREFUSE_APPLET;
	public static String PATTERN_DISCOVERY_ERROR_CREATING_TOKENIZER;
	public static String PATTERN_DISCOVERY_ERROR_LOADING_SPAN_FIELDS;
	public static String PATTERN_DISCOVERY_ERROR_INVALID_NUMBER;
	public static String PATTERN_DISCOVERY_ERROR_INVALID_RANGE;
	public static String PATTERN_DISCOVERY_ERROR_INVALID_VALUE;
	public static String PATTERN_DISCOVERY_VALIDATION_SELECT_PROJECT_WITH_OUTPUT_VIEWS;
	public static String PATTERN_DISCOVERY_VALIDATION_SELECT_GROUPON;
  public static String PATTERN_DISCOVERY_VALIDATION_SELECT_PROJECT;
  public static String PATTERN_DISCOVERY_VALIDATION_SELECT_OUTPUT_VIEW;
  public static String PATTERN_DISCOVERY_VALIDATION_SELECT_LANGUAGE;
  public static String PATTERN_DISCOVERY_NO_HISTORY;
  public static String PATTERN_DISCOVERY_ERROR_GETTING_HISTORY;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, ErrorMessages.class);
	}

	private ErrorMessages() {

	}
}
