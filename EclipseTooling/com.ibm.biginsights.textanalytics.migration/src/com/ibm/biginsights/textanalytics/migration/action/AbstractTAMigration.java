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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.biginsights.textanalytics.migration.Messages;
import com.ibm.biginsights.textanalytics.migration.MigrationPlugin;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Abstract base class for all TA migration classes.
 * Provides concrete implementation for migrate() method
 * 
 *  Krishnamurthy
 *
 */
public abstract class AbstractTAMigration implements ITextAnalyticsMigration {



	protected ILog logger = LogUtil.getLogForPlugin(MigrationPlugin.PLUGIN_ID);
	
	protected IProject project;
	protected String oldVersion;
	protected String newVersion;
	
	public AbstractTAMigration(IProject project, String oldVersion, String newVersion) {
		this.project = project;
		this.oldVersion = oldVersion;
		this.newVersion = newVersion;
	}

	/* (non-Javadoc)
	 * @see com.ibm.biginsights.textanalytics.migration.action.ITextAnalyticsMigration#migrate()
	 */
	@Override
	public void migrate() {
		migrateTextAnalyticsPropertyFile();
		migrateLaunchConfigs();
		migrateFolders();
		customMigration();
		
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			logger.logAndShowError(Messages.AbstractTAMigration_UNABLE_TO_REFRESH_PROJECT);
		}
	}

	@Override
	public void customMigration() {
		//do nothing. Merely a place holder for optional implementation by sub classes.
		//Provided an empty implementation here, so that subclasses are not forced to implement this method
	}
	
	
}
