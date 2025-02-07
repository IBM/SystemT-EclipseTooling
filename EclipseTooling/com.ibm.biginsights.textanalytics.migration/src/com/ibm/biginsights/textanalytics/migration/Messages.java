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
package com.ibm.biginsights.textanalytics.migration;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {



	private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.migration.messages"; //$NON-NLS-1$
	
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
	    .getBundle(BUNDLE_NAME);
	
	public static String AbstractTAMigration_UNABLE_TO_REFRESH_PROJECT;
	public static String MigrateToV13_ERROR_MIGRATING_RESULT_FOLDER;
	public static String MigrateToV13_MIGRATION_OF_PROFILER_CONFIG_FAILED;
	public static String MigrateToV13_MIGRATION_OF_RUN_CONFIG_FAILED;
	public static String MigrateToV13_TA_FILE_NOT_FOUND;
	public static String MigrateToV13_UNABLE_TO_READ_TA_FILE;
	public static String MigrateV20ToV21_PropMigrationV20ToV21CompleteMsg;
	public static String MigrateV20ToV21_TAPropMigrationV20ToV21FailedMsg;
	public static String MigrateV20ToV21_TAPropMigrationV20ToV21Skip;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
  
  public static String getString(String key, Object[] params) {
    try {
      return MessageFormat.format(RESOURCE_BUNDLE.getString(key),params);
    } catch (MissingResourceException e) {
      return '!' + key + "!";
    }
  }
  
	private Messages() {
	}
}
