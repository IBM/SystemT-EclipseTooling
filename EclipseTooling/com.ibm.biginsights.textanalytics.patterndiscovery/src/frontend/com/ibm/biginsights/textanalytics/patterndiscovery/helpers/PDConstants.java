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
package com.ibm.biginsights.textanalytics.patterndiscovery.helpers;

import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;

/**
 * common constanst for PD
 * 
 * 
 */
public class PDConstants
{



  public static final String FILE_SEPARATOR = System.getProperty ("file.separator");

  public static final String PROPERTIES_FILE = "com.ibm.biginsights.textanalytics.patterndiscovery.properties.aqlGroupByEnronClean";

  public static final String PATTERN_DISCOVERY_TEMP_DIR_NAME = ".patterndiscovery";

  public static final String VIEW_SPAN_SEPARATOR = ".";
  public static final String VIEW_SPAN_SEPARATOR_REGEX = "\\.";

  public static final String PD_PROJECT_NAME_PROP = "projectName";
  public static final String PD_PROP_PATH_PROP = "propertiesPath";
  public static final String PD_AOG_PATH_PROP = "aogPath";
  public static final String PD_MAIN_AQL_PATH_PROP = "mainAqlPath";

  public static final String PD_LANGUAGE_PROP = "lang";

  public static final String PD_DEFAULT_SNIPPET = Messages.SNIPPET_FIELD_DEFAULT_VALUE;

}
