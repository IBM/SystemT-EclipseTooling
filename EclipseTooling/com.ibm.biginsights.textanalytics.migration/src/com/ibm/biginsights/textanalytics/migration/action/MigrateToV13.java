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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.ibm.biginsights.textanalytics.migration.Messages;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * Migrates projects from an older version to v1.3 BigInsights
 *  Krishnamurthy
 *
 */
public class MigrateToV13 extends AbstractTAMigration{



	public MigrateToV13(IProject project, String oldVersion, String newVersion) {
		super(project, oldVersion, newVersion);
	}

	@Override
	public void migrateTextAnalyticsPropertyFile() {
		Properties props = new Properties();
		File taFile = null;
		try {
			taFile =  ProjectUtils.getPreferenceStoreFile(project);
			if(taFile != null){
				props.load(new FileInputStream(taFile));
				
				//properties to add
				props.put(Constants.LC_ROOT_DIR, Constants.GS_DEFAULT_PARENT_DIR);
				props.put(Constants.RESULT_ROOT_DIR, Constants.DEFAULT_ROOT_RESULT_DIR);
				
				//properties to remove
				props.remove(Constants.GENERAL_AOGPATH);
				props.remove(Constants.GENERAL_RESULTDIR);
				props.remove(Constants.GENERAL_LANGUAGE);
				props.store(new FileOutputStream(taFile), null);
			}else{
				logger.logAndShowError(Messages.MigrateToV13_TA_FILE_NOT_FOUND);
			}
		} catch (FileNotFoundException e) {
			logger.logAndShowError(Messages.MigrateToV13_TA_FILE_NOT_FOUND);
			return;
		} catch (IOException e) {
			logger.logAndShowError(Messages.MigrateToV13_UNABLE_TO_READ_TA_FILE);
			return;
		}
	}

	@Override
	public void migrateLaunchConfigs() {
		try {
			ILaunchConfiguration[] runConfigs = ProjectUtils.getRunConfigsByProject(project.getName());
			removeV12Properties(runConfigs);
		} catch (Exception e) {
			logger.logAndShowError(Messages.MigrateToV13_MIGRATION_OF_RUN_CONFIG_FAILED);
			logger.logError(e.getMessage());
		}
		
		try {
			ILaunchConfiguration[] profileConfigs = ProjectUtils.getProfileConfigsByProject(project.getName());
			removeV12Properties(profileConfigs);
		} catch (Exception e) {
			logger.logAndShowError(Messages.MigrateToV13_MIGRATION_OF_PROFILER_CONFIG_FAILED);
			logger.logError(e.getMessage());
		}
	}
	
	private void removeV12Properties(ILaunchConfiguration[] configs) throws CoreException{
		for (ILaunchConfiguration config : configs) {
			ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
			workingCopy.removeAttribute(IRunConfigConstants.AOG_PATH);
			workingCopy.removeAttribute(IRunConfigConstants.ENABLE_PROVENANCE);
			workingCopy.removeAttribute(IRunConfigConstants.MAIN_AQL);
			workingCopy.removeAttribute(IRunConfigConstants.RESULT_DIR);
			workingCopy.removeAttribute(IRunConfigConstants.SEARCH_PATH);
			if(workingCopy.isDirty()){
				workingCopy.doSave();
			}
		}
	}

	@Override
	public void migrateFolders() {
		try {
			//STEP1: create result folder, if it does not exist
			IFolder resultFolder = ProjectUtils.getRootResultFolder(project);
			if(!resultFolder.exists()){
				resultFolder.create(true, true, null);
			}
			
			//STEP 2: migrate .result folder to result
			IFolder dotResultFolder = project.getFolder(Constants.V12_DEFAULT_ROOT_RESULT_DIR); //$NON-NLS-1$
			if(dotResultFolder.exists()){
				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy-HHmmss");
				String lastModifiedTimeStamp = sdf.format(new Date(dotResultFolder.getLocalTimeStamp()));
				IPath renamedFolderPath = resultFolder.getFolder(Constants.DEFAULT_RESULT_DIR_PREFIX+lastModifiedTimeStamp).getFullPath(); //$NON-NLS-1$
				dotResultFolder.move(renamedFolderPath, true, null);
	      // Setting the encoding explcitly to UTF-8 so that highlighting in the treeviewer 
	      // shows up fine without offsets that creeps in with the default windows cp1252 charset/encoding
				resultFolder.setDefaultCharset (Constants.ENCODING, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			logger.logAndShowError(Messages.MigrateToV13_ERROR_MIGRATING_RESULT_FOLDER);
			logger.logError(e.getMessage());
		}
	}
	

}
