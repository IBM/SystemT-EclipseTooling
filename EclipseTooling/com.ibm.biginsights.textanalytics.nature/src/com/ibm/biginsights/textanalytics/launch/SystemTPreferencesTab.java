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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;

public class SystemTPreferencesTab extends AbstractLaunchConfigurationTab implements SelectionListener, ModifyListener, PropertyChangeListener{



	private static final String ICON_SYSTEMT = "icons/full/etool16/textAnalyticsProp.gif"; //$NON-NLS-1$
	
	protected RunPreferences preferences;
	
	/**
	 * The purpose of this flag is to determine if property change event / widget selection event should be handled or not.
	 * When initializeFrom() restores the state of the UI, we know that the widgets are undergoing a state change, 
	 * hence no need to process events related to UI state change at that point of time.
	 * Ensure that initializeFrom() sets this flag to true at the beginning of the method and sets it back to false at the end of the method
	 */	
	protected boolean ignoreEvents = false;
	
	protected Composite container;
	
	/**
	 * Determines if a project refresh has happened. Whenever a project refresh happens, the systemT tab should be refreshed with new project values.
	 * The setProjectName() method is responsible for changing the value of this flag.
	 * The initializeFrom() method consumes this flag to check if it should initialize the UI from project properties or launch config file
	 */
	protected boolean projectRefresh = false;
	
	protected String projectName;
	
	public SystemTPreferencesTab(){

	}
	
	private void setupSystemTPrefTab(){
		if(preferences == null){
			preferences = new RunPreferences(projectName);
			setupUI();
		}else{
			preferences.restoreToProjectProperties(projectName);
		}
	}
	
	public void setProjectName(String projName, boolean projectRefresh){
		this.projectRefresh = projectRefresh;
		this.projectName = projName;
		setupSystemTPrefTab();
	}
	
	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		
		this.setControl(container);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.run_textanalytics");
	}
	
	protected void setupUI(){
		if(container != null){
			preferences.createContents(container);
			
			preferences.getGeneralPrefPage().addModifyListeners(this);
			preferences.getGeneralPrefPage().addSelectionListeners(this);
		}
	}

	@Override
	public String getName() {
		return Messages.getString("SystemTPreferencesTab.PROJECT_PREFERENCES"); //$NON-NLS-1$
	}

	/**
	 * This tab never initializes from the launch configuration file. It always initializes from the project properties.
	 * This is due to the requirement that latest changes to project properties must be picked up by Run Configuration.
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		ignoreEvents = true;

		if(preferences != null){
			PreferenceStore prefStore = preferences.getPreferenceStoreCopy();
			SystemTProperties projProps = ProjectPreferencesUtil.createSystemTProperties(prefStore, projectName);
			preferences.setProjectProperties(projProps);
			try {
				/**
				 * Fix for defect 14025
				 * NOTE: I am not sure if type casting to ILaunchConfigurationWorkingCopy is the right way obtain an editable configuration object.
				 * I tried using ILaunchConfiguration.getWorkingCopy() to obtain a working copy, but that does not save changes to the original
				 * configuration file. So, I am left with no option but to type cast. This may cause a problem in future if there are changes to the behavior
				 * of ILaunchConfiguration
				 */
				if(configuration instanceof ILaunchConfigurationWorkingCopy){
					ILaunchConfigurationWorkingCopy configWorkingCopy = (ILaunchConfigurationWorkingCopy)configuration;
					setConfigAttributes(configWorkingCopy, projProps);
					configWorkingCopy.doSave();
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		ignoreEvents = false;
		setDirty(true);
		updateLaunchConfigurationDialog();
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		
		if(preferences != null){
			preferences.performApply(false);
			
			SystemTProperties properties = preferences.getProjectProperties();
			setConfigAttributes(configuration, properties);
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		try {
			projectName = config.getAttribute(IRunConfigConstants.PROJECT_NAME, ""); //$NON-NLS-1$
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IProject project = ProjectPreferencesUtil.getProject(projectName);
		config.setAttribute(IRunConfigConstants.RESULT_DIR, ProjectPreferencesUtil.getDefaultResultDir(project));
		config.setAttribute(IRunConfigConstants.AOG_PATH,  ProjectPreferencesUtil.getDefaultAOGPath(project));
	}

	protected void setConfigAttributes(ILaunchConfigurationWorkingCopy config, SystemTProperties properties){
		config.setAttribute(IRunConfigConstants.PROJECT_NAME, properties.getProjectName());
		config.setAttribute(IRunConfigConstants.ENABLE_PROVENANCE, properties.getEnableProvenance());
		config.setAttribute(IRunConfigConstants.MAIN_AQL, properties.getMainAQLFile());
		config.setAttribute(IRunConfigConstants.SEARCH_PATH, properties.getSearchPath());
		
		config.setAttribute(IRunConfigConstants.AOG_PATH, properties.getAogPath());
		config.setAttribute(IRunConfigConstants.RESULT_DIR, properties.getResultDir());
	}
	
	
	@Override
	public Image getImage() {
		return ProjectPreferencesUtil.getImage(ICON_SYSTEMT);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		if(preferences == null){
			//there is nothing to validate
			return true;
		}
		
		try {
			if(launchConfig.getAttribute(IRunConfigConstants.PROJECT_NAME, "").length() == 0){ //$NON-NLS-1$
				//project name not set yet. So, there is no configuration to validate
				return true;
			}
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return true;
	}
	
	@Override
	public void modifyText(ModifyEvent event) {
		if(!ignoreEvents){
			setDirty(true);
			updateLaunchConfigurationDialog();
		}
	}
		
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(!ignoreEvents){
			setDirty(true);
			updateLaunchConfigurationDialog();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if(!ignoreEvents){
			setDirty(true);
			updateLaunchConfigurationDialog();
		}		
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		if(!ignoreEvents){
			setDirty(true);
			updateLaunchConfigurationDialog();
		}		
	}
	
	public void refreshData(){
		setDirty(true);
		updateLaunchConfigurationDialog();
	}
}
