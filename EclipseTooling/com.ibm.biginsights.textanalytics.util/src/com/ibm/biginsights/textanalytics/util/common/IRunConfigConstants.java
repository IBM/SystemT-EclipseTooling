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

public interface IRunConfigConstants {

 public static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

	public static final String PLUGIN_ID = "com.ibm.biginsights.textanalytics.nature";
	
	public static final String INPUT_COLLECTION = PLUGIN_ID + ".INPUT_COLLECTION";
  public static final String PD_INPUT_COLLECTION = "docDir";

  public static final String DELIMITER = PLUGIN_ID + ".DELIMITER";

  public static final String LANG = PLUGIN_ID + ".LANG";
	public static final String ENABLE_PROVENANCE = PLUGIN_ID + ".ENABLE_PROVENANCE";
	public static final String RESULT_DIR = PLUGIN_ID + ".RESULT_DIR";
	public static final String MAIN_AQL = PLUGIN_ID + ".MAIN_AQL";
	public static final String AOG_PATH = PLUGIN_ID + ".AOG_PATH";
	public static final String SEARCH_PATH = PLUGIN_ID + ".SEARCH_PATH";
	
	public static final String PROJECT_NAME = PLUGIN_ID + ".PROJECT_NAME";
  public static final String PD_PROJECT_NAME = "projectName";
	
	public static final String SELECTED_MODULES = PLUGIN_ID + ".SELECTED_MODULES";

	
	public static final String MIN_SECONDS_TO_RUN = PLUGIN_ID + ".MIN_SECONDS_TO_RUN";
  
  public static final String BI_SERVER_NAME = PLUGIN_ID + ".BI_SERVER_NAME";
  public static final String EXTRACTOR_APPNAME = PLUGIN_ID + ".EXTRACTOR_APPNAME";
  public static final String INPUT_DATASET_NAME = PLUGIN_ID + ".INPUT_DATASET_NAME";

  public static final String PD_LAUNCH_CONFIG_ID = "com.ibm.biginsights.textanalytics.patterndiscovery.runconfig.systemTApplication";   //$NON-NLS-1$
  public static final String PD_OUTPUT_VIEW_ATTR_NAME = "typeName";   //$NON-NLS-1$

  /**
   * This attribute holds a map with external tables required by selected modules
   * in the config being the key and relative/absolute paths of files assigned to them as values
   */
  public static final String EXTERNAL_TABLES_MAP = PLUGIN_ID + ".EXTERNAL_TABLES";
  
  /**
   * This attribute holds a map with external dictionaries required by selected modules
   * in the config being the key and relative/absolute paths of files assigned to them as values
   */
  public static final String EXTERNAL_DICT_MAP = PLUGIN_ID + ".EXTERNAL_DICTIONARIES";
  
  /**
   * This attribute holds a list of external tables of selected modules that are 
   * by definition required to have values assigned to them.
   */
  public static final String EXT_TABLE_REQ_VAL_LIST = PLUGIN_ID + ".EXTERNAL_TABLE_REQ_VAL";
  
  /**
   * This attribute holds a list of external dictionaries of selected modules that are 
   * by definition required to have values assigned to them.
   */
  public static final String EXT_DICT_REQ_VAL_LIST = PLUGIN_ID + ".EXTERNAL_DICT_REQ_VAL";
}
