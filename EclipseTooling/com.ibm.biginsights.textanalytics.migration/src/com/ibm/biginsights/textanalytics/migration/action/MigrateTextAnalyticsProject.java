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

import org.eclipse.core.resources.IProject;

import com.ibm.biginsights.project.IBigInsightsMigration;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;

/**
 * Migrates Text Analytics project from old version to new version. The version numbers are passed in as parameters
 * to the migrate() method.
 *  Krishnamurthy
 *
 */
public class MigrateTextAnalyticsProject implements IBigInsightsMigration {



	@Override
	public void migrate(IProject project, String oldVersion, String newVersion) {
		/**
		 * Check for newVersion and oldVersion (if required) to instantiate a suitable migration action class
		 */
		ITextAnalyticsMigration migration = null;
		
		if(BIConstants.BIGINSIGHTS_VERSION_V13.equals(newVersion)){
			migration = new MigrateToV13(project, oldVersion, newVersion);
    }
	if (BIProjectPreferencesUtil.isAtLeast(newVersion, BIConstants.BIGINSIGHTS_VERSION_V21)
      && BIConstants.BIGINSIGHTS_VERSION_V2.equals (oldVersion)) {
      migration = new MigrateV20ToV21 (project, oldVersion, newVersion);
    }
		
		if(migration != null){
			migration.migrate();
		}
	}

}
