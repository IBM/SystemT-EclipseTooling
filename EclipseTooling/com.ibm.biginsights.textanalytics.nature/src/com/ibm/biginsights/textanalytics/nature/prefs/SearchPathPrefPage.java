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
/**
 * 
 */
package com.ibm.biginsights.textanalytics.nature.prefs;

import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * Provides UI for 'Search path' section of SystemT property sheet
 * 
 * 
 *
 */
public class SearchPathPrefPage extends PrefPageAdapter{



	protected DataPathEditor dataPathEditor;
	protected String errorMessage = null;
	SystemTProjectPreferences projectPreferences;
	
	public SearchPathPrefPage(Composite parent, SystemTProjectPreferences projectPreferences, String descText, boolean isModular){
		super(projectPreferences);
		this.projectPreferences = projectPreferences;
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if(isModular)
			dataPathEditor = new DataPathEditor(composite, "", DataPathEditor.PROJECT_AND_EXTERNAL_FOLDERS, DataPathEditor.EDIT_AND_REMOVE, 
					Constants.FILE_OR_DIRECTORY, 
					Constants.SUPPORTED_MODULE_PATH_FORMATS);
		else
			dataPathEditor = new DataPathEditor(composite);
		dataPathEditor.setDescriptionLabelText(descText); //$NON-NLS-1$
		restoreDefaults();
		
		if(projectPreferences.consumer == Constants.CONSUMER_RUN_CONFIG){
			dataPathEditor.enableUI(false);
		}
	}
	
	public Control getControl(){
		return dataPathEditor;
	}

	@Override
	public void apply() {
		if(projectPreferences.isModularProject){
			setValue( Constants.TAM_PATH, dataPathEditor.getDataPath());
			projectPreferences.getProjectProperties().setTamPath(preferenceStore.getString(Constants.TAM_PATH));

		}else{
			setValue( Constants.SEARCHPATH_DATAPATH, dataPathEditor.getDataPath());
			projectPreferences.getProjectProperties().setSearchPath(preferenceStore.getString(Constants.SEARCHPATH_DATAPATH));
		}
	}

	@Override
	public void restoreDefaults() {
		if(preferenceStore == null){
			return;
		}
		dataPathEditor.clearDataPaths();
		if(projectPreferences.isModularProject){
			String tamPath = preferenceStore.getDefaultString(Constants.TAM_PATH);
			if(tamPath != null && tamPath.trim().length() > 0){
				String[] datapaths = tamPath.split(Constants.DATAPATH_SEPARATOR);
				if(datapaths != null){
					for (String datapath : datapaths) {
						dataPathEditor.addDataPath(ProjectPreferencesUtil.getPath(datapath), ProjectPreferencesUtil.isWorkspaceResource(datapath));
					}
				}
			}

		}else{
			String defaultDataPath = preferenceStore.getDefaultString(Constants.SEARCHPATH_DATAPATH);
			if(defaultDataPath != null && defaultDataPath.trim().length() > 0){
				String[] datapaths = defaultDataPath.split(Constants.DATAPATH_SEPARATOR);
				if(datapaths != null){
					for (String datapath : datapaths) {
						dataPathEditor.addDataPath(ProjectPreferencesUtil.getPath(datapath), ProjectPreferencesUtil.isWorkspaceResource(datapath));
					}
				}
			}
		}
		
	}

	@Override
	public void restoreToProjectProperties(SystemTProperties properties) {
		dataPathEditor.clearDataPaths();
		if(projectPreferences.isModularProject){
			String defaultTamPath = properties.getTamPath();
			if(defaultTamPath != null && defaultTamPath.trim().length() > 0){
				String[] datapaths = defaultTamPath.split(Constants.DATAPATH_SEPARATOR);
				if(datapaths != null){
					for (String datapath : datapaths) {
						dataPathEditor.addDataPath(ProjectPreferencesUtil.getPath(datapath), ProjectPreferencesUtil.isWorkspaceResource(datapath));
					}
				}
			}

		}else{
			String defaultDataPath = properties.getSearchPath();
			if(defaultDataPath != null && defaultDataPath.trim().length() > 0){
				String[] datapaths = defaultDataPath.split(Constants.DATAPATH_SEPARATOR);
				if(datapaths != null){
					for (String datapath : datapaths) {
						dataPathEditor.addDataPath(ProjectPreferencesUtil.getPath(datapath), ProjectPreferencesUtil.isWorkspaceResource(datapath));
					}
				}
			}
		}
		
	}
	
	public void addDataPathChangeListener(PropertyChangeListener listener){
		dataPathEditor.addDataPathChangeListener(listener);
	}
	
	public void removeDataPathChangeListener(PropertyChangeListener listener){
		dataPathEditor.addDataPathChangeListener(listener);
	}
	
	public boolean isValid(){
		String searchPath = dataPathEditor.getDataPath();
		
		if(!projectPreferences.isModularProject && StringUtils.isEmpty(searchPath)){
			setErrorMessage(Messages.getString("SearchPathPrefPage.ERR_SEARCH_PATH_CANNOT_BE_EMPTY")); //$NON-NLS-1$
			return false;
		}
		
		setErrorMessage(null);
		return true;
	}
	
	public String getErrorMessage(){
		return errorMessage;
	}
	
	protected void setErrorMessage(String errorMsg){
		this.errorMessage = errorMsg;
	}
	
	public void enableUI(boolean enabled){
		dataPathEditor.enableUI(enabled);
	}

}
