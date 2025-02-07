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
package com.ibm.biginsights.textanalytics.goldstandard.ui.prefpages;

import java.io.IOException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.IWorkbench;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Base class for all gold standard preference pages. Provides APIs to load annotation types from preference store.
 * Provides basic implementations for Apply and OK button handlers.
 * 
 *  Krishnamurthy
 *
 */
public abstract class GenericPrefPage extends PreferencePage {



	protected IWorkbench workbench;
	protected IProject project;
	protected IFolder gsFolder;
	
	
	protected String PARAM_DEFAULT_ANNOTATION_TYPE;
	protected String PARAM_DETECT_WORD_BOUNDARIES;
	protected String PARAM_LANGUAGE;
	protected String PARAM_ANNOTATION_TYPES;

	public GenericPrefPage(IProject project, IFolder gsFolder){
		this.gsFolder = gsFolder;
		setProject(project);
	}
	

	protected void setProject(IProject project) {
		if(project == null){
			CustomMessageBox msgBox = CustomMessageBox.createErrorMessageBox(
					ProjectUtils.getActiveWorkbenchWindow().getShell(), 
					Messages.GenericPrefPage_ERROR, Messages.GenericPrefPage_SELECT_PROJECT);
			msgBox.open();
			return;
		}
		this.project = project;	
		setParamNames();

	}

	private void setParamNames() {
		/* Originally, the param names were computed by concatenating project name, gsName and param name.
		 * Though it is now simplified to just the actual parameter names (like annotationTypes, language)
		 * the setParamNames() method is still retained, just in case we bring back some other format for 
		 * param names in lc.prefs in the future
		 */
		PARAM_DEFAULT_ANNOTATION_TYPE = Constants.GS_DEFAULT_ANNOTATION_TYPE; //$NON-NLS-1$
		PARAM_DETECT_WORD_BOUNDARIES =  Constants.GS_DETECT_WORD_BOUNDARIES; //$NON-NLS-1$
		PARAM_LANGUAGE = Constants.GS_LANGUAGE;
		PARAM_ANNOTATION_TYPES = Constants.GS_ANNOTATION_TYPES; //$NON-NLS-1$
	}
	
	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return GoldStandardUtil.getGSPrefStore(gsFolder);
	}
	

	@Override
	protected void performApply() {
		PreferenceStore prefStore = (PreferenceStore) getPreferenceStore();
		if(prefStore.needsSaving()){
			
			try {
				prefStore.save();
				gsFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
			} catch (IOException e) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
			}
		}
	}
	
	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if(visible){
			refreshData();
		}
	}
	
	protected abstract void refreshData();


}
