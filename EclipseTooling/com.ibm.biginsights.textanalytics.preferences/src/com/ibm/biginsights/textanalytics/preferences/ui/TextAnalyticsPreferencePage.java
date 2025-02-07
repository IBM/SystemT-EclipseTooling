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
package com.ibm.biginsights.textanalytics.preferences.ui;



import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.preferences.Messages;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;

/**
 * UI page for Text Analytics preference page displayed when the user clicks Windows -> Preferences -> BigInsights -> Text Analytics
 * 
 *
 */
public class TextAnalyticsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	@SuppressWarnings("unused")


	protected Button cbShowAdvancedTab;
	protected Button cbShowEnableProvenance;
	protected Button cbEnableReportProblem;
	protected Button cbValidateInputCollectionEncoding;
	protected Button cbWarnResultsOverwriting;
	protected Button cbLogDebugMessages;
	
	/**
	 * 
	 */
	public TextAnalyticsPreferencePage() {
		super();
		
		setDescription(Messages.TextAnalyticsPreferencePage_TextAnalalyticsWorkspaceSettingsDescription);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, true));
		
		cbShowAdvancedTab = new Button(composite, SWT.CHECK);
		cbShowAdvancedTab.setText(Messages.TextAnalyticsPreferencePage_AdvancedTabCheckBoxLabel);
		
		cbShowEnableProvenance = new Button(composite, SWT.CHECK);
		cbShowEnableProvenance.setText(Messages.TextAnalyticsPreferencePage_ShowEnableProvenanceCheckBoxLabel);

		cbEnableReportProblem = new Button(composite, SWT.CHECK);
		cbEnableReportProblem.setText(Messages.TextAnalyticsPreferencePage_EnableReportProblemCheckBoxLabel);
    
		cbValidateInputCollectionEncoding = new Button(composite, SWT.CHECK);
		cbValidateInputCollectionEncoding.setText(Messages.TextAnalyticsPreferencePage_ValidateInputEncodingCheckBoxLabel);
		cbValidateInputCollectionEncoding.setSelection(true);
		
		cbWarnResultsOverwriting = new Button(composite, SWT.CHECK);
		cbWarnResultsOverwriting.setText(Messages.TextAnalyticsPreferencePage_WarnExportResultsOverwriteCheckBoxLabel);
		cbWarnResultsOverwriting.setSelection(true);
		
		cbLogDebugMessages = new Button(composite, SWT.CHECK);
		cbLogDebugMessages.setText(Messages.TextAnalyticsPreferencePage_LogDebugMessagesCheckBoxLabel);
		cbLogDebugMessages.setSelection (true);
	
		performDefaults();
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.text_analytics"); //$NON-NLS-1$

		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {

	}
	


	/*
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		
		TextAnalyticsWorkspacePreferences prefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
		prefs.setPrefShowAdvancedTab (cbShowAdvancedTab.getSelection ());
		prefs.setPrefShowEnableProvenanceOption (cbShowEnableProvenance.getSelection ());
		prefs.setPrefEnableReportProblem (cbEnableReportProblem.getSelection ());
		prefs.setPrefValidateInputEncoding (cbValidateInputCollectionEncoding.getSelection());
		prefs.setPrefWarnOverwritingExportResultsFile (cbWarnResultsOverwriting.getSelection());
		prefs.setPrefLogDebugMessages (cbLogDebugMessages.getSelection ());
		prefs.savePreferences ();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
	  TextAnalyticsWorkspacePreferences prefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
		
	  //These properties have been initialized properly via PreferencesPlugin and TextAnalyticsWorkspacePreferences.
		boolean showAdvancedTab = prefs.getPrefShowAdvancedTab ();
		boolean showEnableProvenance = prefs.getPrefShowEnableProvenanceOption ();
		boolean enableReportProblem = prefs.getPrefEnableReportProblem ();
		boolean validateInputEncoding = prefs.getPrefValidateInputEncoding ();
		boolean validateResultsOverwritting = prefs.getPrefWarnOverwritingExportResultsFile ();
		boolean logDebugMessages = prefs.getPrefLogDebugMessages ();
		//set the state of checkboxes
		cbShowAdvancedTab.setSelection(showAdvancedTab);
		cbShowEnableProvenance.setSelection(showEnableProvenance);
		cbEnableReportProblem.setSelection(enableReportProblem);
		cbValidateInputCollectionEncoding.setSelection(validateInputEncoding);
		cbWarnResultsOverwriting.setSelection(validateResultsOverwritting);
		cbLogDebugMessages.setSelection (logDebugMessages);
		
		super.performDefaults();
	}

	@Override
	protected IPreferenceStore doGetPreferenceStore() {
		return PreferencesPlugin.getDefault().getPreferenceStore();
	}
	
	
	}
