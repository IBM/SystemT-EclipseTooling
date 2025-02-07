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
package com.ibm.biginsights.textanalytics.launch;

import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.prefs.GeneralPrefPage;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProjectPreferences;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.run.SystemTRunConfig;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class RunConfigGeneralPage extends GeneralPrefPage {



	protected FileDirectoryPicker resultDirPicker;
	
	public RunConfigGeneralPage(Composite parent,
			SystemTProjectPreferences projectPreferences) {
		super(parent, projectPreferences);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void createCustomPanel(){
		resultDirPicker = new FileDirectoryPicker(topLevel, Constants.DIRECTORY_ONLY);
		resultDirPicker.setDescriptionLabelText(Messages.getString("GeneralPrefPage.LOCATION_RESULT_DIR")); //$NON-NLS-1$
		resultDirPicker.enableUI(false);
	}
	
	@Override
	public void apply(){
		setValue(ProjectPreferencesUtil.getDefaultResultDir(ProjectUtils.getSelectedProject()), resultDirPicker.getFileDirValue());
	}
	
	@Override
	public void restoreDefaults() {
		super.restoreDefaults();
		String resultDir = ProjectPreferencesUtil.getDefaultResultDir(ProjectUtils.getSelectedProject());
		resultDirPicker.setFileDirValue(ProjectPreferencesUtil.getPath(resultDir), ProjectPreferencesUtil.isWorkspaceResource(resultDir));
	}

//	@Override
//	public boolean isValid() {
//		if(!super.isValid()){
//			return false;
//		}
//		String resultDir = resultDirPicker.getFileDirValue();
//		if(StringUtils.isEmpty(resultDir)){
//			setErrorMessage(Messages.getString("GeneralPrefPage.ERR_RESULT_DIR_CANNOT_BE_BLANK")); //$NON-NLS-1$
//			return false;
//		}
//		return true;
//	}

	@Override
	public void addModifyListeners(ModifyListener listener) {
		super.addModifyListeners(listener);
		resultDirPicker.addModifyListenerForFileDirTextField(listener);
	}

	@Override
	public void removeModifyListeners(ModifyListener listener) {
		super.removeModifyListeners(listener);
		resultDirPicker.removeModifyListenerForFileDirTextField(listener);
	}

	@Override
	public void restoreToProjectProperties(SystemTProperties properties) {
		super.restoreToProjectProperties(properties);
		if(properties instanceof SystemTRunConfig){
			SystemTRunConfig runConfig = (SystemTRunConfig)properties;
			String resultDir = runConfig.getResultDir();
			resultDirPicker.setFileDirValue(ProjectPreferencesUtil.getPath(resultDir), ProjectPreferencesUtil.isWorkspaceResource(resultDir));			
		}

	}
	
	public String getLanguage(){
		return null;
	}
	
	public String getInputCollection(){
		return null;
	}
	
}
