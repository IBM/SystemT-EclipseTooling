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

import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * GeneralPrefPage provides the UI for 'General' tab of SystemT project
 * preferences. This tab is used for Modular Projects.
 * 
 */
public class ModularGeneralPrefPage extends PrefPageAdapter {

	@SuppressWarnings("unused")


	private Composite topLevel;
	private SearchPathPrefPage searchPathPrefPage;
	private PaginationPrefPanel paginationPrefPanel;
	private String errorMessage = null;
	protected Button cbProvenance;

	public ModularGeneralPrefPage(Composite parent,
			SystemTProjectPreferences projectPreferences) {
		super(projectPreferences);

		topLevel = new Composite(parent, SWT.NONE);
		GridLayout gLayout = new GridLayout();
		gLayout.marginLeft = 0;
		topLevel.setLayout(gLayout);
		
		createEnableProvenancePanel();
		createModulePathPanel();
		
		createPaginationPrefPanel();
		
		restoreDefaults();
	}

	public Control getControl() {
		return topLevel;
	}

	@Override
	public void apply() {
		if (isEnableProvenanceVisible()) {
			boolean enableProvenance = cbProvenance.getSelection();
			super.setValue(Constants.GENERAL_PROVENANCE,
					Boolean.toString(enableProvenance));
		}
		try {
			projectPreferences.getProjectProperties().setProvenance(
					Boolean.parseBoolean(preferenceStore
							.getString(Constants.GENERAL_PROVENANCE)));
		} catch (Exception e) {
			Activator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
							.getMessage()));
		}

		searchPathPrefPage.apply();
		paginationPrefPanel.apply();
	}

	/**
	 * Rstores the values from preferenceStore. This method is invoked when
	 * 'Restore defaults' button of property sheet is clicked
	 */
	@Override
	public void restoreDefaults() {
		if (preferenceStore == null) {
			return;
		}

		if (isEnableProvenanceVisible()) {
			Boolean enableProvenance = preferenceStore
					.getDefaultBoolean(Constants.GENERAL_PROVENANCE);
			cbProvenance.setSelection(enableProvenance);
		}

		searchPathPrefPage.restoreDefaults();
		paginationPrefPanel.restoreDefaults();
	}

	private void createEnableProvenancePanel() {
		if (projectPreferences.consumer == Constants.CONSUMER_PROPERTY_SHEET
				|| projectPreferences.consumer == Constants.CONSUMER_RUN_CONFIG) {
			// Row 1: Language choice
			Composite enableProvenancePanel = new Composite(topLevel, SWT.NONE);
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 10;
			layout.marginWidth = 10;
			enableProvenancePanel.setLayout(layout);
			enableProvenancePanel.setLayoutData(new GridData(
					GridData.FILL_HORIZONTAL));

			if (isEnableProvenanceVisible()) {
				// Row 1.b: Provenance
				Label lbProvenance = new Label(enableProvenancePanel, SWT.NONE);
				lbProvenance.setText(Messages
						.getString("GeneralPrefPage.ENABLE_PROVENANCE")); //$NON-NLS-1$
				cbProvenance = new Button(enableProvenancePanel, SWT.CHECK);
				cbProvenance.setText(""); //$NON-NLS-1$
				cbProvenance.setEnabled(true);
				if (projectPreferences.consumer == Constants.CONSUMER_RUN_CONFIG) {
					cbProvenance.setEnabled(false);
				}
				cbProvenance.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
	        public void getName (AccessibleEvent e) {
	          e.result = Messages.getString("GeneralPrefPage.ENABLE_PROVENANCE");
	        }
	      });
			}
		}
	}
	
	protected void createPaginationPrefPanel() {
		paginationPrefPanel = new PaginationPrefPanel(topLevel,projectPreferences);
		
	}

	public void addDataPathChangeListener(PropertyChangeListener listener) {
		searchPathPrefPage.addDataPathChangeListener(listener);
	}

	public void removeDataPathChangeListener(PropertyChangeListener listener) {
		searchPathPrefPage.addDataPathChangeListener(listener);
	}

	/**
	 * Restores the UI form fields to values picked from properties argument
	 */
	@Override
	public void restoreToProjectProperties(SystemTProperties properties) {
		if (isEnableProvenanceVisible()) {
			cbProvenance.setSelection(properties.getEnableProvenance());
		}

		searchPathPrefPage.restoreToProjectProperties(properties);
		paginationPrefPanel.restoreToProjectProperties(properties);
	}

	/**
	 * validates values entered in the UI
	 * 
	 * @return true if the entries in the UI are valid, false otherwise
	 */
	public boolean isValid() {

		if (!searchPathPrefPage.isValid()) {
			setErrorMessage(searchPathPrefPage.getErrorMessage());
			return false;
		}
		
		if (!paginationPrefPanel.isValid()) {
			setErrorMessage(paginationPrefPanel.getErrorMessage());
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	protected void setErrorMessage(String errorMsg) {
		this.errorMessage = errorMsg;
	}

	public void addSelectionListeners(SelectionListener listener) {
		if (cbProvenance != null) {
			this.cbProvenance.addSelectionListener(listener);
		}
	}

	private boolean isEnableProvenanceVisible() {
		if (projectPreferences.consumer == Constants.CONSUMER_PROPERTY_SHEET
				|| projectPreferences.consumer == Constants.CONSUMER_RUN_CONFIG) {
			TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
			return wprefs.getPrefShowEnableProvenanceOption ();
		} else {
			return false;
		}
	}

	private void createModulePathPanel() {
		searchPathPrefPage = new SearchPathPrefPage(topLevel,
				projectPreferences,
				Messages
				.getString("ModularGeneralPrefPage.Path_Label"), true); //$NON-NLS-1$
	}

}
