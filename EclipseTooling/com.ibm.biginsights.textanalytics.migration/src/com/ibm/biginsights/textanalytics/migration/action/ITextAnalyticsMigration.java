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
package com.ibm.biginsights.textanalytics.migration.action;

/**
 *  Krishnamurthy
 *
 */
public interface ITextAnalyticsMigration {

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
	
	/**
	 * Entry point for migration
	 */
	public void migrate();
	
	/**
	 * Subclasses are expected to implement migration of .textanalytics file
	 */
	public void migrateTextAnalyticsPropertyFile();
	
	/**
	 * Subclasses are expected to implemnet migration of run and profiler launch config files
	 */
	public void migrateLaunchConfigs();
	
	/**
	 * Subclasses are expected to implement any folder level migration activity. Example: result folder
	 */
	public void migrateFolders();
	
	/**
	 * This method is to run some migration action not handled by any of the migrate*() methods. Implementation of this method is optional.
	 * As and when there is a need to add more migration*() methods to this interface, the code from customMigration() could be moved over to migrate*() methods.
	 */
	public void customMigration();
}
