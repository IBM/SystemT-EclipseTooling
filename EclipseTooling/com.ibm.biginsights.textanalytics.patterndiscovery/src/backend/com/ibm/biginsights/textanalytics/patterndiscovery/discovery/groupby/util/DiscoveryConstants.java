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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util;

/**
 * Constants used throughout the Pattern Discovery Project
 * 
 * 
 *
 */
public class DiscoveryConstants {



	//Default expected value in the properties file for PropertyConstants.SNIPPET_FIELD_NAME
	public static final String SNIPPET_DEFAULT_VALUE = "Default_Snippet";
	
	//Type of "snippet" field
	public static final String SNIPPET_COLUMN_TYPE = "VARCHAR(5000)";
	
	//Type of "entities" field - ex. "phone"
	public static final String ENTITY_COLUMN_TYPE = "VARCHAR(500)";
	
	public static final String ENTITY_COLUMN_PREFIX = "ENTITY_";
	
	//The column that pattern discovery is run on - set to a large maximum
	public static final String GROUPBY_CONTEXT_COLUMN_TYPE = "VARCHAR(32000)";
	
	public static final String GROUPBY_CONTEXT_COLUMN_NAME = "groupByContext";
	
	//Type of "sequence" field - ex {enron;n}
	public static final String SEQUENCE_STRING_COLUMN_TYPE = "VARCHAR(500)";
	
	//Type of "jsequence" field - ex {enron} with {enron;n}
	public static final String COMMON_SEQUENCE_STRING_COLUMN_TYPE = "VARCHAR(500)";
	
	//Type for the unique id column (some names include id) - ex docid-span1:span2
	public static final String UNIQUE_ID_COLUMN_TYPE = "VARCHAR(255)";
	
	//Type for AQL view name column (name of resulting database) - ex. PhoneContext
	public static final String VIEW_NAME_COLUMN_TYPE = "VARCHAR(255)";
	
	//DocumentID Column Type
	public static final String DOCID_COLUMN_TYPE = "VARCHAR(255)";
	
	//Type for "word" column - every single word pulled from input - actual word
	//For example: "call", "me", "at" - each a value in this column
	public static final String WORD_MAPPING_COLUMN_TYPE = "VARCHAR(255)";
	
	//This column is only used in the golden standard analysis
	//Values are of the form: [Person:[X,X,X],Phone:[X,X,X],Related:[X,X,X]]
	public static final String MECH_TURK_CUM_STATISTIC_COLUMN_TYPE = "VARCHAR(6500)";
	
	// --- CONSTANTS FOR THE RULES HISTORY TABLE ---
	public static final String RULESHISTORY_TBL_NAME = "RULESHISTORY";
	// --- COLUMN NAMES ---
	public static final String RULE_AS_STR_COL_NAME = "RULEASSTR";
	public static final String SEQ_BEFORE_RULE_COL_NAME = "SEQBEFORE";
	public static final String SEQ_AFTER_RULE_COL_NAME = "SEQAFTER";
	// --- COLUMN TYPES ---
	public static final String RULE_AS_STR_COL_TYPE = "VARCHAR(1000)";
  public static final String SEQ_BEFORE_RULE_COL_TYPE = "VARCHAR(1000)";
  public static final String SEQ_AFTER_RULE_COL_TYPE = "VARCHAR(1000)";
  
  // -- CONSTANTS FOR THE SEQUENCES REPLACED TABLE --
  public static final String SEQREP_TBL_NAME = "SEQREP";
  // --- COLUMN NAMES ---
  //  context, signature, newSignature, jsignature, newJSignature
  public static final String CONTEXT_COL_NAME = "CONTEXT";
  public static final String SIGNATURE_COL_NAME = "SIGNATURE";
  public static final String NEW_SIGNATURE_COL_NAME = "NEWSIGNATURE";
  public static final String JSIGNATURE_COL_NAME = "JSIGNATURE";
  public static final String NEW_JSIGNATURE_COL_NAME = "NEWJSIGNATURE";
  // -- COLUMN TYPES ---
  public static final String CONTEXT_COL_TYPE = GROUPBY_CONTEXT_COLUMN_TYPE ;
  public static final String SIGNATURE_COL_TYPE = COMMON_SEQUENCE_STRING_COLUMN_TYPE;
  public static final String NEW_SIGNATURE_COL_TYPE = COMMON_SEQUENCE_STRING_COLUMN_TYPE;
  public static final String JSIGNATURE_COL_TYPE = SEQUENCE_STRING_COLUMN_TYPE;
  public static final String NEW_JSIGNATURE_COL_TYPE = SEQUENCE_STRING_COLUMN_TYPE;
}
