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

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByPreprocessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.goldenStandard.MechTurkPostProcessing;

/**
 * All configuration settings to run Pattern Discovery
 * 
 * 
 *  Chu
 *
 */
public class PropertyConstants {



	/**
	 * Large Module Switches
	 * 
	 * These switches control the main modules of Pattern Discovery.  
	 * The modules are broken into the following categories:
	 * 
	 * 1. Amazon Mechanical Turk Raw Results Processing - processes 
	 * the raw results and creates a "GoldStandard.csv" that contains
	 * the labels (Positive, Negative) for each input entry
	 * 
	 * 		Ex. "John can be called at 555-5555" - Yes (Positive) 
	 * 	
	 * {@link MechTurkPostProcessing}
	 * Not fully Supported Currently
	 *
	 * 2. SystemT AQL processing - given a corpus and an AQL query,
	 * compiles the AQL (stores the aog), and create the inputs in 
	 * CSV format to be inserted into the database (aqlOuptut.csv)
	 * 
	 * 2a. Add gold standard - add labels from step 1 to raw inputs created from AQL 
	 * or an input file
	 * Not fully Supported Currently
	 * 
	 * 2b. Load Input - Once there is an input (aqlOutput.csv) - import the CSV
	 * into the database.  See {@link GroupByPreprocessor#runFileAQL} for
	 * expected CSV file structure
	 * 
	 * 3. PatternDiscovery - perform PD on the imported results from AQL.  Results
	 * in a final database where groups are stored with the following structure:
	 * 
	 * COLUMN_NAME         |TYPE_NAME|DEC&|NUM&|COLUM&|COLUMN_DEF|CHAR_OCTE&|IS_NULL&
	 * ------------------------------------------------------------------------------
	 * COUNT               |INTEGER  |0   |10  |10    |NULL      |NULL      |YES     
	 * HASHID              |INTEGER  |0   |10  |10    |NULL      |NULL      |YES     
	 * PHONECONTEXT        |VARCHAR  |NULL|NULL|5000  |NULL      |65000     |YES     
	 * ID                  |VARCHAR  |NULL|NULL|255   |NULL      |510       |YES     
	 * SEQUENCE            |VARCHAR  |NULL|NULL|255   |NULL      |510       |YES     
	 * JSEQUENCE           |VARCHAR  |NULL|NULL|255   |NULL      |510       |YES     
	 * ENTITY              |VARCHAR  |NULL|NULL|255   |NULL      |510       |YES     
	 * SNIPPET             |VARCHAR  |NULL|NULL|5000  |NULL      |65000     |YES     
	 * 
	 * Where "PHONECONTEXT" and "ENTITY" are unique values specified by user during
	 * runtime
	 * 
	 * 
	 * 4. Post Processing - Applies labels processed from step 1, to groupings generated
	 * in step 3.  Also performs statistics (precision) on the resulting groups
	 * Not fully Supported Currently
	 * 
	 * 5. Debugging - for each procedure in Grouping, the intermediate steps can be stored in 
	 * external CSV files with text labels.
	 * 
	 */
	
	/* Set "true" when there is a new gold standard results file from Mechanical Turk
	 * 
	 * Form: "true" or "false" only
	 * Default "false" - assume gold standard does not exist
	 * Currently not officially supported
	 */
	public static final String PROCESS_MECHTURK_RESULTS = "processMechTurkResults";
	
	/* Set "true" when results are already inside the database and there are no new inputs
	 * 
	 * Form: "true" or "false" only
	 * Default: "false" - run SystemT on a document either from AQL query or aog
	 */
	public static final String USE_EXISTING_DB_DATA = "disableRunAQL";
	
	/* Set "true" when results are to be loaded into database from an existing file
	 * 
	 * Form: "true" or "false" only
	 * Default: "false" - database empty - load a file as input
	 */
	public static final String IMPORT_DATA_FROM_FILE = "disableCompileAQL";
	
	/* Set to "true" when there is a gold standard to add to raw input and final grouping file
	 * 
	 * Form: "true" or "false" only
	 * Default: "false" - assume gold standard does not exist
	 * Currently not officially supported
	 */
	public static final String ADD_GOLD_STANDARD = "addGoldStandard";
	
	/* Set "true" when groupings do not need to be recomputed (all intermediate steps are not run)
	 * 
	 * Form: "true" or "false" only
	 * Default: "false" - database does not contain groupings - group inputs
	 */
	public static final String TURN_PATTERNDISCOVERY_OFF = "disablePatternDiscovery";
	
	/* Set "false" when a gold standard exists and labels should be applied to groupings
	 * 
	 * Form: "true" or "false" only
	 * Default: "true" - assume gold standard does not exist
	 */
	public static final String DISABLE_POST_PROCESSSING = "disablePostProcessing";
	
	/* Set "true" to see intermediate steps of Pattern Discovery in external files
	 * 
	 * Form: "true" or "false" only
	 * Default: "false" - Save space/time by not generating unnecessary files
	 */
	public static final String ENABLE_DEBUGGING = "enableDebug";
	
	/**
	 * Small module switches 
	 * 
	 * These switches control the small modules during grouping.
	 * To run from scratch or given a new set of inputs
	 * 		-> modules 1-5 must run and cannot be disabled
	 * 
	 * The modules are broken into the following categories:
	 * 
	 * 1. Mining - sequences are pulled from the input
	 * 2. Dictionary - store mappings from text to sequence
	 * 3. Loading - store sequences into database
	 * 4. Counting - perform all counts/co-counts
	 * 5. Measure - compute measures
	 * 6. Rule Generation - generate rules from statistics
	 * 7. Grouping - Groups based on signatures
	 * 8. Fuzzy Grouping - currently not supported
	 * 9. Gold Standard Statistics - not supported
	 */
	
	/* Pull sequences from existing database
	 * 
	 * Form: "true" or "false"
	 * Default: "false" - recompute sequences
	 */
	public static final String DISABLE_SEQUENCE_MINING = "disableMining";
	
	/* Do not store dictionary
	 * 		- mappings from text to sequence
	 * 
	 * Form: "true" or "false"
	 * Default: "true"
	 */
	public static final String DISABLE_STORE_DICTIONARY = "disableStoreDictionary";
	
	/* Store additonal "supporting" sequence information in database
	 * 
	 * Form: "true" or "false"
	 * Default: "false" - store in database
	 */
	public static final String DISABLE_LOAD_SUPPORT_SEQUENCE = "disableSupportLoading";
	
	/* Pull counts from existing database
	 *  - counts and co-counts
	 * 		Ex. {can} = count: 1
	 * 			{can} & {be} = co-count: 2
	 * 
	 * Form: "true" or "false"
	 * Default: "false" - count sequences
	 */
	public static final String DISABLE_COUNTING = "disableCounting";
	
	/* Do not recompute statistics - pull from existing DB
	 * 		- Statistics (Correlation Measures)
	 * 
	 * Form: "true" or "false"
	 * Default: "false" - calculate measures
	 */
	public static final String DISABLE_COMPUTE_MEASURE_SEQUENCE = "disableComputeMeasureSequenceSequence";
	
	/* Do not regenerate rules - use existing rule files
	 * 
	 * Form: "true" or "false"
	 * Default: "false" - regenerate rule files
	 */
	public static final String DISABLE_RULE_GENERATION = "disableRuleGeneration";
	
	/* Do not regroup - use existing grouping files
	 * 
	 * Form: "true" or "false"
	 * Default: "false" - do grouping
	 */
	public static final String DISABLE_GROUPING = "disableGrouping";
	
	/* Do not run fuzzy grouping
	 * 
	 * Form: "true" or "false"
	 * Default: "true" - fuzzy grouping currently not supported
	 */
	public static final String DISABLE_FUZZY_GROUPING = "disableNewGrouping";
	
	/* Statistics are performed (Recall/Precision)
	 * 		- Must have gold standard
	 * 
	 * Form: "true" or "false"
	 * Default: "false" - assume gold standard does not exist
	 */
	public static final String RUN_GOLDSTANDARD_STATISTICS = "runStatistics";
	
	
	/**
	 * File System Specifics - where debug and temp files are stored relative to a
	 * root path.  All paths are created on startup if they do not exist.  User does not
	 * need to have the existing structure to run
	 */
	
	/* Base file directory - the root of all results and files
	 * 
	 * Default: groupby/data/
	 */
	public static final String FILE_ROOT_DIR = "rootDir";
		
	/* Document directory - location of all document collections
	 * Absolute path (or relative to install home) required
	 * Ex. groupBy/data/personphone/documents/
	 * 
	 * Default: No default (Needs to be machine specific to run)
	 */
	public static final String DOCUMENT_COLLECTION_DIR = "docDir";
	
	/* Corpus Name - name of directory or archived file (zip, tar)
	 * 
	 * Default: No default (needs to know what file name to run on)
	 */
	public static final String INPUT_DOCUMENT_NAME = "docFile";
	
	/* Debug directory - relative to base file dir - stores all generated debug files
	 * 
	 * Default: temp/debug/
	 */
	public static final String DEBUG_DIR = "debugDirectory";

	/* Rule generation directory - relative to base file dir - stores all generated rule files
	 * 
	 * Default: temp/rules/
	 */
	public static final String RULE_DIR = "ruleDirectory";
	
	/* Grouping directory - relative to base file dir - stores all generated grouping files
	 * 
	 * Default: temp/group/
	 */
	public static final String GROUPING_DIR = "groupDirectory";
	
	/* Input directory - relative to base file dir - stores all generated input files
	 * Where raw file input should be stored when importing from file only
	 * 
	 * Default: input/
	 */
	public static final String INPUT_FILE_DIR = "inputDirectory";
	
	/* Post process directory - relative to base file dir - stores all post process results
	 * 
	 * Default: postprocess/
	 */
	public static final String POST_PROCESS_DIR = "postprocessDir";
	
	/* Mechanical Turk directory - relative to base dile dir - stores all mech turk results
	 * 
	 * Default: postprocess/mechTurk/
	 */
	public static final String MECHANICAL_TURK_DIR = "MechTurkDir";
	
	/**
	 * SystemT AQL Configuration Settings
	 * 
	 * These settings tell the system what views and fields from the AQL
	 * query or aog file, to extract text from and group from
	 */
	
	/* AQL output view name - grouping field must exist in this view
	 * Ex. PhoneContext
	 * 
	 * Default: no default - must provide a view name
	 */
	public static final String AQL_VIEW_NAME = "typeName";
  public static final String AQL_MODULE_NAME = "moduleName";
	
	/* Field name that is grouped on from AQL query or AOG file
	 * Ex. PhoneContext.leftCtx
	 * 
	 * Default: no default - must provide a field name
	 */
	public static final String GROUP_BY_FIELD_NAME = "groupBy";
	
	/* Entities (ie person,phone) that is associated with the grouping field
	 * Must be specified as shown in the example - comma delineated values
	 * Can have any number of entities (including none - left blank)
	 * 
	 * Ex. person,phone 
	 * 
	 * Default: blank
	 */
	public static final String ENTITY_FIELD_NAMES = "relationshipAQLFields";
	
	/* The field from which a "snippet" is generated from.  A snippet is the 
	 * text that encompasses the input that grouping is performed on.
	 * 
	 * If left blank - no snippet will be generated
	 * If a field is specified - will use the value of the field
	 * Else - "default" will return 25 chars left and right of the input
	 * 
	 * Default: "default"
	 */
	public static final String SNIPPET_FIELD_NAME = "snippetFieldName";
	
	/**
	 * Input Configuration Settings
	 * 
	 * Once given an input (either from AQL or raw CSV file) - process the data
	 * according to the settings below
	 */
	
	/* Will ignore additional white spaces in grouping text
	 * 
	 * Form: "true" or "false" only
	 * Default: "true"
	 */
	public static final String IGNORE_EXTRA_WHITESPACES = "removeWhiteSpace";
	
	public static final String IGNORE_EXTRA_NEWLINES = "removeNewLines";
	 
	
	/* All inputs (context) will ignore casing and process as lower case
	 * 
	 * Form: "true" or "false" only
	 * Default: "true"
	 */
	public static final String INPUT_TO_LOWERCASE = "removeUpperCase";
	
	/* If text contains an "entity", replace the real text with a placeholder
	 * Note: entity must be specified in ENTITY_FIELD_NAMES
	 * 
	 * Ex. "John can be reached" -> "<person> can be reached"
	 * 
	 * Form: "true" or "false" only
	 * Default: "true"
	 */
	public static final String REPLACE_ENTITY = "replaceEntity";
	
	/**
	 * Sequence Mining Configuration Settings
	 * 
	 * These settings are used to define what is a sequence when mining
	 * For example: the size of a sequence and how often a sequence needs to appear
	 * to be considered frequent
	 */
	
	/* Sequences cannot be longer than this specified number
	 * 
	 * Ex. mining.maxItemNumber=2 
	 * 		"can be reached" -> {can;be;reached;can be; be reached} 
	 * 		({can be reached} not possible > 2)
	 * 
	 * Range: Any integer value greater than 0
	 * Default: 15 
	 */
	public static final String SEQUENCE_MAX_SIZE = "mining.maxItemNumber";
	public static final String SEQUENCE_MIN_SIZE = "mining.minItemNumber";
	
	/* The minimum number of times a sequence needs to appear before being 
	 * considered "frequent"
	 * 
	 * Ex. mining.minSupport=10
	 * 		"can" -> count 15		(kept as a sequence to be used in signature)
	 * 		"he" -> count 5			(removed)
	 * 
	 * Range: Any integer value
	 * Defaults: 5 -> recommended for small size corpus (~100 entries)
	 * 			 10 -> recommended for medium size corpus (~5000 entries)
	 * 			 15 -> (~10,000 entries)
	 * 			 50 -> (~100k entries)
	 */
	public static final String SEQUENCE_MIN_FREQUENCY_SIZE = "mining.minSupport";
	
	/**
	 * Rule Generation Configuration Settings
	 * 
	 * These settings are used to compute statistics from mined sequences.
	 * These settings also determine from these statistics, when a rule
	 * should be generated 
	 * 
	 * The range values and min/max values must ALL be satisfied for a rule
	 * to be generated.  If one setting is satisfied, it is possible that
	 * another setting will not, and that rule will NOT be generated
	 * 
	 * Rules are in the format:
	 * 		- always drop the sequence to the LEFT of the "AND"
	 * 
	 * "DROP {sequence X} IF {sequence X} AND {sequence Y}"
	 * "DROP {sequence Y} IF {sequence Y} AND {sequence X}"
	 */
	
	/* Select which correlation measure to apply when generating rules
	 * 
	 * Correlation Measures - these correlation algorithms determine how similar two
	 * 						  sequences are to another and how important a sequence
	 * 						  is in within the entire corpus
	 * 
	 * Ex. measures=cxy,cyx
	 * Any number of measures - comma separated - only from list below
	 * 
	 * Possible Measures:	cxy - Uncertainty Coefficient U(X|Y) {@link ComputeCorrelation#uncertainCoefficient}
	 * 						cyx - Uncertainty Coefficient U(Y|X) {@link ComputeCorrelation#uncertainCoefficient}
	 * 						mi 	- Mutual Information {@link ComputeCorrelation#mutualInformation}
	 * 						jc	- Jaccard {@link ComputeCorrelation#jaccard}
	 * 						x2	- chiSquare {@link ComputeCorrelation#chiSquare}
	 * 						re	- Redundancy {@link ComputeCorrelation#redundancy}
	 * Default: cxy,cyx
	 */
	public static final String CORRELATION_MEASURES = "measures";
	
	/* Range of correlation measure to generate a rule.  Correlation measure is between
	 * two sequences (see CORRELATION_MEASURES description)
	 * 
	 * Ex. measure: >0.2<1
	 * 		Correlation between {can} & {be} = 0.7
	 * 		Correlation between {can} & {reached} = 0.1
	 * 
	 * A rule is generated for {can} & {be} - nothing is generated for {can} & {reached}
	 * 
	 * Valid symbols: 	">" - greater than, "<" less than, 
	 * 					">=" - greater than and equal, "<=" less than and equal 
	 * Form: ">doubleMin<doubleMax"			Ex. >0.2<1	
	 * 		 ">doubleMin"					Ex. >0.5		
	 * 		 "<doubleMax"					Ex. <0.6		
	 * 
	 * Range: Any value between 0 and 1
	 * Default: >0.2<1
	 */
  public static final String CORRELATION_MEASURE_MIN = "measure_min";
  public static final String CORRELATION_MEASURE_MAX = "measure_max";
	
	/* The range of co-occurring sequences that are used to generate statistics
	 * Co-occurence: The number of times sequence X appears with Sequence Y 
	 * 				 within the same input text
	 * Ex. coocurrence = >1
	 * 		Input 1: "can be reached"
	 * 		Input 2: "can be called"
	 * 	
	 * 		sequence X: {can},	sequence Y: {be}		=	co-occurrence: 2
	 * 		sequence X: {can},	sequence Y: {reached}	=	co-occurrence: 1
	 * 
	 * Will not generate a rule with {can} and {reached} because its co-occurrence
	 * is less than the threshold.
	 * 
	 * Valid symbols: 	">" - greater than, "<" less than, 
	 * 					">=" - greater than and equal, "<=" less than and equal 
	 * Form: ">integerMin<integerMax"		Ex. >1<3	(values = 2)
	 * 		 ">integerMin"					Ex. >2		(all values greater than 2)
	 * 		 "<integerMax"					Ex. <3		(values = 1,2)
	 * 
	 * Range: Any integer value greater than 0
	 * Default: >1
	 */
	public static final String CO_COUNT_RANGE = "coocurrence";
	
	/* Range for the relative frequency for sequence X within two sequences (X,Y)
	 * If relative frequency is not in range, rule is not generated
	 * 
	 * Relative frequency is found by taking the occurrences of X (count) over the
	 * co-occurrences with X&Y.
	 * 
	 * Ex. xRFRange:>0.6
	 * 		Input 1: "can be reached"			count {can} = 2
	 * 		Input 2: "can be called"			count {reached} = 1
	 * 	
	 * 		sequence X: {can},	sequence Y: {be}		=	co-occurrence: 2
	 * 			xRFRange {can} = 2/2 = 1.0
	 * 
	 * 		sequence X: {reached},	sequence Y: {can}	=	co-occurrence: 1
	 * 			xRFRange {reached} = 1/2 = 0.5 					
	 * 
	 * 		Rule with {reached} and {can} will not be generated
	 * 
	 * Form: ">doubleMin<doubleMax"			Ex. >0.2<1	
	 * 		 ">doubleMin"					Ex. >0.5		
	 * 		 "<doubleMax"					Ex. <0.6		
	 * 
	 * Range: Any value greater than 0
	 * Default: >0.1
	 */
	public static final String SEQ_X_RELATIVE_FREQUENCY_RANGE = "xRFRange";
	
	/* Range for the relative frequency for sequence Y within two sequences (X,Y)
	 * If relative frequency is not in range, rule is not generated
	 * 
	 * Relative frequency is found by taking the occurrences of Y (count) over the
	 * co-occurrences with X&Y.
	 * 
	 * Ex. yRFRange:<3.0
	 * 		Input 1: "can be reached"			count {can} = 2
	 * 		Input 2: "can be called"			count {reached} = 1
	 * 	
	 * 		sequence X: {can},	sequence Y: {be}		=	co-occurrence: 2
	 * 			yRFRange {be} = 2/2 = 1.0
	 * 
	 * 		sequence X: {reached},	sequence Y: {can}	=	co-occurrence: 1
	 * 			yRFRange {can} = 2/1 = 2.0					
	 * 
	 * 		Rule with {reached} and {can} will not be generated
	 * 
	 * Form: ">doubleMin<doubleMax"			Ex. >0.2<1	
	 * 		 ">doubleMin"					Ex. >0.5		
	 * 		 "<doubleMax"					Ex. <0.6		
	 * 
	 * Range: Any value greater than 0
	 * Default: >0.1
	 */
	public static final String SEQ_Y_RELATIVE_FREQUENCY_RANGE = "yRFRange";
	
	/* Range of the count for the sequence to be dropped
	 * 		(Right of the "AND" - see Rule Configuration Comments)
	 * If the sequence is NOT in the range - a drop rule will not be generated
	 * 
	 * The count of a sequence is the number of appearances of that sequence
	 * within the ENTIRE corpus
	 * 
	 * Ex. dropableCount > 6
	 * 		{can} 		- count: 10
	 * 		{reached} 	- count: 5
	 * 
	 * 		IF {can} AND {reached} - Drop rule will NOT be generated
	 * 		IF {reached} AND {can} - Drop rule can possibly be generated
	 * 
	 * Form: ">integerMin<integerMax"		Ex. >1<3	(values = 2)
	 * 		 ">integerMin"					Ex. >2		(all values greater than 2)
	 * 		 "<integerMax"					Ex. <3		(values = 1,2)
	 * 
	 * Range: Any integer value greater than 0
	 * Default: >0		
	 */
	public static final String DROP_SEQ_COUNT_RANGE = "dropableCount";
	
	/* Range of the relative frequency for the sequence to be dropped
	 * 		(Right of the "AND" - see Rule Configuration Comments)
	 * If the sequence is NOT in the range - a drop rule will not be generated
	 * 
	 * Relative frequency is found by taking the occurrences of the sequence to
	 * to be dropped over the co-occurrences with the first sequence.
	 * 
	 * Ex. indicatorRF:<3.0		(In this example seq Y is the "dropped sequence")
	 * 		Input 1: "can be reached"			count {can} = 2
	 * 		Input 2: "can be called"			count {reached} = 1
	 * 	
	 * 		sequence X: {reached},	sequence Y: {can}	=	co-occurrence: 1
	 * 			indicatorRF {can} = 2/1 = 2.0					
	 * 
	 * 		Rule with "IF {reached} AND {can}" will not be generated
	 * 
	 * Form: ">doubleMin<doubleMax"			Ex. >0.2<1	
	 * 		 ">doubleMin"					Ex. >0.5		
	 * 		 "<doubleMax"					Ex. <0.6		
	 * 
	 * Range: Any value greater than 0
	 * Default: >0
	 */
	public static final String DROP_SEQ_RELATIVE_FREQUENCY_RANGE = "dropableRF";
	
	/* Range of the count for the indicator sequence
	 * 		(Left of the "AND" - see Rule Configuration Comments)
	 * If the sequence is NOT in the range - a drop rule will not be generated
	 * 
	 * The count of a sequence is the number of appearances of that sequence
	 * within the ENTIRE corpus
	 * 
	 * Ex. indicatorCount > 6
	 * 		{can} 		- count: 10
	 * 		{reached} 	- count: 5
	 * 
	 * 		IF {can} AND {reached} - Drop rule can possibly be generated
	 * 		IF {reached} AND {can} - Drop rule will NOT be generated
	 * 
	 * Form: ">integerMin<integerMax"		Ex. >1<3	(values = 2)
	 * 		 ">integerMin"					Ex. >2		(all values greater than 2)
	 * 		 "<integerMax"					Ex. <3		(values = 1,2)
	 * 
	 * Range: Any integer value greater than 0
	 * Default: >0		
	 */
	public static final String INDICATOR_SEQ_COUNT_RANGE = "indicatorCount";
	
	/* Range of the relative frequency for the indicator sequence
	 * 		(Left of the "AND" - see Rule Configuration Comments)
	 * If the sequence is NOT in the range - a drop rule will not be generated
	 * 
	 * Relative frequency is found by taking the occurrences of the sequence to
	 * to be dropped over the co-occurrences with the first sequence.
	 * 
	 * Ex. indicatorRF:>1.5		(In this example seq X is the "indicator sequence")
	 * 		Input 1: "can be reached"			count {can} = 2
	 * 		Input 2: "can be called"			count {reached} = 1
	 * 
	 * 		sequence X: {reached},	sequence Y: {can}	=	co-occurrence: 1
	 * 			indicatorRF {reached} = 1/1 = 1.0					
	 * 
	 * 		Rule with "IF {reached} AND {can}" will not be generated
	 * 
	 * Form: ">doubleMin<doubleMax"			Ex. >0.2<1	
	 * 		 ">doubleMin"					Ex. >0.5		
	 * 		 "<doubleMax"					Ex. <0.6		
	 * 
	 * Range: Any value greater than 0
	 * Default: >0
	 */
	public static final String INDICATOR_SEQ_RELATIVE_FREQUENCY_RANGE = "indicatorRF";
	
	/* What types of rules are to be generated by Correlation2Rules
	 * for cxy and cyx only: 
	 * 	- TYPE_ADD_PREDICTOR 	(Creates and add rule that will add the independent variable 
	 * 							of the uncertain coefficient if the dependant is found)
	 * 
	 * 	- TYPE_DROP_PREDICTOR 	(Creates and drop rule that will drop the independent variable 
	 * 							of the uncertain coefficient if the both of them are found)
	 * 
	 * 	- TYPE_ADD_PREDICTOR 	(Creates and add rule that will add the dependent variable of 
	 * 							the uncertain coefficient if the independant is found)
	 * 
	 * 	- TYPE_DROP_PREDICTOR 	(Creates and drop rule that will drop the dependent variable of 
	 * 							the uncertain coefficient if the both of them are found)
	 * 
	 * for the other measures:
	 * 	- TYPE_ADD_MORE_SPECIFIC 	(creates an add rule that will add the sequence with the lower 
	 * 								relative frequency if that with the higher is found)
	 * 
	 * 	- TYPE_ADD_LESS_SPECIFIC 	(creates an add rule that will add the sequence with the higher 
	 * 								relative frequency if that with the lower is found)
	 * 
	 * 	- TYPE_DROP_MORE_SPECIFIC 	(creates an add rule that will add the sequence with the lower 
	 * 								relative frequency if both sequences are found)
	 * 
	 * 	- TYPE_DROP_LESS_SPECIFIC 	(creates an add rule that will add the sequence with the higher 
	 * 								relative frequency if both sequences are found)
	 * 
	 * Form: Comma separated list - select two rule types
	 * Default: TYPE_DROP_DEPENDANT, TYPE_DROP_LESS_SPECIFIC
	 */
	public static final String DROP_RULE_TYPES = "ruleTypes";
	
	/**
	 * Grouping Configuration Settings
	 * 
	 * Settings that change the way a signature is found - thus affecting the
	 * grouping outcome
	 * 
	 */
	
	/* Specify what rule files to apply during grouping
	 * If none specified, all files in ruleDirectory ending in rule.csv are taken
	 * Currently does not support blank
	 * TODO: empty pulls all rules in rule directory
	 * 
	 * Ex. ruleFile=cxyAndcyx-rule.csv
	 * 
	 * Form: comma separated list
	 * Default: cxyAndcyx-rule.csv
	 */
	public static final String APPLIED_RULE_FILES = "ruleFile";
	
	/* Words that do not satisfy frequency constraint will still be considered when
	 * generating the semantic signature
	 * 		- Allows for more specific (smaller) groups
	 * 
	 * Form: "true" or "false" only
	 * Default: "false"
	 */
	public static final String USE_INFREQUENT_WORDS = "useInfrequentWords";
	
	/* Generated rules will be applied on semantic signature again
	 * after tokenizing the signature
	 * 
	 * Ex.	Rule: DROP {he} if {he} AND {reached}
	 * 		First pass: {he can be;reached;he} -(APPLY RULES)-> {he can be;reached}
	 *		Second pass: {he can be;reached} -(TOKENIZE)-> 	{he;can;be;reached} 
	 *					 {he;can;be;reached} -(APPLY RULES)-> {can be; reached}
	 * 
	 * Results: Input: {he can be;reached;he}
	 * 			Disabled (false) = 	{he can be;reached}
	 * 			Enabled (true) = 	{can be;reached}
	 * 
	 * Form: "true" or "false" only
	 * Default: "true"
	 */
	public static final String APPLY_RULES_ON_SIGNATURE = "enablePruneSequence";
	
	/* HashFactory to performing hashing when grouping
	 * 
	 * The possible options are:
	 * 
	 * 		hashFactory - char-default {@link CharIgnoreHasher},
	 * 		simple - {@link SimpleHasher}
	 * 		stem-default - {@link StemmingDictIgnoreHasher} 
	 * 		rules - {@link RuleBasedHasher}
	 * 			  - values for useInfrequentWords, sequenceDB and relevantSequences are read.
	 * 
	 * Default: rules
	 */
	public static final String HASH_FACTORY = "hashFactory";
	
	/* Groupings are merged based on the distance between semantic signature
	 * Currently uses Jaccard Distance
	 * 		- Groups smaller than 3 are merged with groups larger than 9
	 * 		- TODO: make configurable
	 * 
	 * Ex.  {reached}		size: 10
	 * 		{be reached}	size: 1
	 * 
	 * 		Become one group with group name: {reached}
	 * 
	 * Form: "true" or "false" only
	 * Default: "true"
	 */
	public static final String DISABLE_DISTANCE_MERGING = "disableSequenceDistance";
	
	/**
	 * SystemT Configuration Settings
	 * 
	 * More backend SystemT settings - jars, includes, and dictionary locations
	 * These do not need to be set when passing in an AOG file
	 */
	
	/* Absolute path (or relative to install home) to AQL Query File
	 * Ex. groupBy/data/personphone/AQLqueries/phone.aql
	 * No default - required if running SystemT from AQL query
	 */
	public static final String AQL_QUERY_FILE = "aqlQuery";
	
	/* Absolute path (or relative to install home) to AQL dictionary directory
	 * Ex. groupBy/data/personphone/core/GenericNE/dictionaries/
	 * No default - required if running SystemT from AQL query
	 */
	public static final String AQL_DICTIONARY_DIR = "aqlDictionaryDir";
	
	/* Absolute path (or relative to install home) to AQL jars directory
	 * Ex. groupBy/data/personposition/core/GenericNE/udfjars/
	 * No default - required if running SystemT from AQL query
	 */
	public static final String AQL_JAR_DIR = "aqlJarDir";
	
	/* Absolute path (or relative to install home) to AQL includes directory
	 * Ex. groupBy/data/personphone/
	 * No default - required if running SystemT from AQL query
	 */
	public static final String AQL_INCLUDES_DIR = "aqlRootDir";
	
	/**
	 * Database Information
	 * 
	 * Two databases are required for PatternDiscovery.  Details for the two databases
	 * including driver and names are specified below
	 * 
	 */
	
	/* Depending on what DB driver chosen, this prefix needs to be set
	 * For Apache Derby Embedded DB - default value is: "jdbc:derby:"
	 */
	public static final String DB_PREFIX = "dbUrlPrefix";
	
	/* Name of the database that stores the AQL results as well as the final grouping results
	 * Example: aom-personPhonesEnron
	 */
	public static final String RESULTS_DB_NAME = "aomDbName";
	
	/* Name of the database that stores sequence and stat information
	 * Example: personPhonesEnron
	 */
	public static final String SEQUENCE_DB_NAME = "sequenceDB";
	
	/* User name for sequence database 
	 * Currently not required but can be set if using db2
	 * Ex. db2user
	 * Default: Commented out
	 */
	public static final String SEQUENCE_DB_USER = "sequenceDBUser";
	
	/* Password for sequence database
	 * Currently not required but can be set if using db2
	 * Ex. db2password
	 * Default: Commented out
	 */
	public static final String SEQUENCE_DB_PASSWORD = "sequenceDBPassword";
	
	/**
	 * Gold Standard Configuration Settings and Switches
	 * 
	 * Currently not fully supported
	 * Includes Amazon Mechanical Turk Settings
	 */
	
	/* Creates a CSV file to be fed into Amazon Mechanical Turk
	 * More specifically - creates the necessary fields (entities, snippets, id)
	 *  - Also removes duplicate entries (based on entities, and snippet)
	 * Note: Large datasets take ~45 minutes
	 * Default: "false" - only need to generated once
	 */
	public static final String CREATE_MECHANICAL_TURK_INPUT = "createMechTurk";
	
	/* Absolute path (or relative to install home) of AQL query used to create
	 * Mechanical Turk input
	 * Ex. AQLqueries/mechTurk.aql
	 * Default: No default
	 */
	public static final String MECHANICAL_TURK_AQL_QUERY = "mechTurkAqlQuery";
	
	/* View name that contains the fields to be pulled from from Mech Turk AQL query
	 * Ex. JoinedSentence
	 * Expects to have fields (sentence, entities)
	 * Default: No default - required to create Mechanical Turk Input
	 */
	public static final String MECHANICAL_TURK_VIEW_NAME = "mechTurkTypeName";
	
	/* Absolute path (or relative to install home) of raw results from Mechanical Turk
	 * See {@link MechTurkPostProcessing} for structure expected for results 
	 * Ex. postprocess/mechTurk/rawResults.csv
	 * Default: No default - requires rawResults file location
	 */
	public static final String MECHANICAL_TURK_RESULTS_FILE = "MeckTurkResultsFile";
	
	/**
	 * Fuzzy Grouping Settings
	 * 
	 * Currently not supported
	 */
	
	/* Grouping algorithm used during fuzzy grouping
	 * Two options: db or linewise
	 * 
	 * 		db: 		group inside database
	 * 		linewise: 	by txt file
	 * 
	 * Default: linewise
	 * Fuzzy Grouping currently not supported
	 */
	public static final String FUZZY_GROUPING_ALGORITHM = "groupingAlgo";
	
	/* Column in grouping files that contain grouped information
	 * 
	 * Default: 2
	 */
	public static final String FUZZY_GROUPING_COLUMN = "findfuzzy.compare";
	
	/* Column in grouping file that contain hash value
	 * 
	 * Default: 1
	 */
	public static final String FUZZY_HASHID_COLUMN = "findfuzzy.pivot";
	
	public static final String GROUPING_LOW = "groupingLow";
	public static final String GROUPING_HIGH = "groupingHigh";
	public static final String JACCARD_SCORE = "jaccardScore";
	public static final String RECOMPUTE_SEQUENCES = "recompSequences";

	 public static final String TEST_MODULE = "testModuleName";
   public static final String TEST_TAM_PATH = "pathToTestTam";

}
