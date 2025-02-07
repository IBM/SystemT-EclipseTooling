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
package com.ibm.biginsights.textanalytics.nature.prefs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Preferences page for SystemT projects. This class is used to collect project
 * properties from SystemT developer
 * 
 * 
 */
public class SystemTProjectPreferences extends PropertyPage implements
		SelectionListener, ModifyListener, PropertyChangeListener {



	protected GeneralPrefPage generalPrefPage;

	// fields
	protected TabFolder tabFolder;
	protected PreferenceStore preferenceStore;
	protected String systemTPreferencesFile;
	protected Composite composite;
	protected boolean isModularProject;

	// project properties
	protected SystemTProperties properties;

	protected String projectName;

	protected boolean ignoreEvents = false;

	// consumer
	protected int consumer = Constants.CONSUMER_PROPERTY_SHEET; // defaults to
																// property
																// sheet

	// Controller that handles Modular Pages
	ModularPrefPage modularPrefPage;
	// Controller that handles Non Modular Pages
	NonModularPrefPage nonModularPrefPage;

	public SystemTProjectPreferences() {
		super();
		init();
	}

	public SystemTProjectPreferences(int consumer, String projectName) {

		this.consumer = consumer;
		this.projectName = projectName;
		init();
	}

	protected void init() {
		try {
			initPreferenceStore();
		} catch (IOException e) {
			if (e instanceof FileNotFoundException) {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(
								IStatus.ERROR,
								Activator.PLUGIN_ID,
								Messages.getString("SystemTProjectPreferences.WARN_SYSTEMT_FILE_NOT_FOUND")));//$NON-NLS-1$
				LogUtil.getLogForPlugin(Activator.PLUGIN_ID)
				.logAndShowError(Messages.getString ("TEXT_ANALYTICS_FILE_NOT_FOUND")//$NON-NLS-1$
				  + projectName);
				return;
			} else {
				Activator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
								.getMessage()));
			}
		}

		this.properties = ProjectPreferencesUtil.createSystemTProperties(
				preferenceStore, projectName);
	}

	protected void initPreferenceStore() throws IOException {
		IProject project = getProject();

		if (project != null) {

			if (projectName == null) {
				projectName = project.getName();
			}

			this.isModularProject = ProjectUtils.isModularProject(projectName);

			systemTPreferencesFile = project.getLocation() + File.separator
					+ Constants.TEXT_ANALYTICS_PREF_FILE;
			setPreferenceStore(new PreferenceStore(systemTPreferencesFile));
			preferenceStore = (PreferenceStore) getPreferenceStore();
			preferenceStore.load();
			if (isModularProject) {
				modularPrefPage = new ModularPrefPage(this);
				modularPrefPage.setDefaultValuesInPreferenceStore();
			} else {
				nonModularPrefPage = new NonModularPrefPage(this);
				nonModularPrefPage.setDefaultValuesInPreferenceStore();
			}
		}
	}

	public PreferenceStore getPreferenceStoreCopy() {
		return preferenceStore;
	}

	protected IProject getProject() {
		if (projectName == null) {
			IProject project = ProjectPreferencesUtil.getSelectedProject();
			return project;
		} else {
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			return project;
		}

	}

	protected void createTabGeneral(TabFolder tabFolder, TabItem tabGeneral) {
	}

	@Override
	public Control createContents(Composite parent) {
		ignoreEvents = true;

		if(modularPrefPage == null && nonModularPrefPage == null){
			return parent;
		}
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL
				| SWT.V_SCROLL);
		sc.setLayout(new GridLayout(1, true));
		sc.setLayoutData(new GridData(GridData.FILL_BOTH));

		composite = new Composite(sc, SWT.NONE);
		sc.setContent(composite);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create tab folder to contain tabs
		tabFolder = new TabFolder(composite, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2,
				1));
		tabFolder.setFont(composite.getFont());
		if (isModularProject) {
			modularPrefPage.populateModularTabGeneral(tabFolder);
			modularPrefPage.populateModularSourcePrefPage(tabFolder);
			modularPrefPage.populateModularProjectPrefPage(tabFolder);
		} else{
			nonModularPrefPage.populateTabGeneral(tabFolder);
		}

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		ignoreEvents = false;

		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(
						parent,
						"com.ibm.biginsights.textanalytics.tooling.help.project_properties_text_analytics");

		return sc;
	}

	@Override
	protected void performApply() {
		if (isDataValid()) {
		  //This would work for both modular and non-modular projects
			performApply(true);
		}
	}

	/**
	 * This method is made public as it is invoked by Run Config tab
	 * (SystemTPreferencesTab) as well
	 * 
	 * @param savePrefStore
	 */
	public void performApply(boolean savePrefStore) {
		if (isModularProject)
			modularPrefPage.performApply(savePrefStore);
		else
			nonModularPrefPage.performApply(savePrefStore);
	}

	public void performApplyAll() {
		if (isModularProject)
			modularPrefPage.performApplyAll();
		else
			nonModularPrefPage.performApplyAll();
	}

	@Override
	protected void performDefaults() {

		if (isModularProject) {
			modularPrefPage.performDefaults();
		} else {
			nonModularPrefPage.performDefaults();
		}
	}

	public void restoreToProjectProperties(String projectName) {
		try {
			this.projectName = projectName;
			initPreferenceStore();
			setProjectProperties(ProjectPreferencesUtil
					.createSystemTProperties(getPreferenceStoreCopy(),
							projectName));
		} catch (IOException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
							.getMessage()));
		}
		isDataValid();
	}

	@Override
	public boolean performOk() {
			performApplyAll();
			// If getErrorMessage() returns null, then performApplyAll did not have any validation issues 
			if (getErrorMessage() != null) {
			  CustomMessageBox msgBox = CustomMessageBox.createErrorMessageBox(
          getShell(), "Error in Text Analytics properties page",
          getErrorMessage());
			  msgBox.open();
			  return false;
			}
			if (isModularProject) {
				return modularPrefPage.savePreferenceStore();
			}	
			else {
				return nonModularPrefPage.savePreferenceStore();
			}	
	}

	public void setProjectProperties(SystemTProperties props) {
		// assign a safe copy, by cloning the object
		try {
			this.properties = (SystemTProperties) props.clone();
		} catch (CloneNotSupportedException e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
							.getMessage()));
		}

		if (isModularProject) {
			modularPrefPage.restoreToProjectProperties(properties);
		} else
			nonModularPrefPage.restoreToProjectProperties(properties);
	}

	public SystemTProperties getProjectProperties() {
		return properties;
	}

	public GeneralPrefPage getGeneralPrefPage() {
		return nonModularPrefPage.generalPrefPage;
	}

	public ModularGeneralPrefPage getModularGeneralPrefPage() {
		return modularPrefPage.modularGeneralPrefPage;
	}

	@Override
	public boolean okToLeave() {
		return true;
	}

	public boolean isDataValid() {
		boolean valid = false;
		if (isModularProject)
			valid = modularPrefPage.isDataValid();
		else
			valid = nonModularPrefPage.isDataValid();
		return valid;
	}

	@Override
	public void modifyText(ModifyEvent event) {
		if (!ignoreEvents) {
			isDataValid();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!ignoreEvents) {
			isDataValid();
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (!ignoreEvents) {
			isDataValid();
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		if (!ignoreEvents) {
			isDataValid();
		}
	}

}
