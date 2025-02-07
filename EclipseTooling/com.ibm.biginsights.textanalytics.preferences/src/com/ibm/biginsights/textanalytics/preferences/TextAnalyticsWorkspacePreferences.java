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
package com.ibm.biginsights.textanalytics.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Class handling retrieval and storage of general Text Analytics preferences.
 * 
 *
 */
public class TextAnalyticsWorkspacePreferences
{
	@SuppressWarnings("unused")


  private String bundleName;
  private IPreferenceStore preferenceStore;
  
  public static final String PREF_SHOW_ADVANCED_TAB = "preference.showAdvancedTab";
  public static final String PREF_SHOW_ENABLE_PROVENANCE = "preference.showEnableProvenance";
  public static final String PREF_ENABLE_REPORT_PROBLEM = "preference.enableReportProblem";
  public static final String PREF_VALIDATE_INPUT_ENCODING = "preference.validateInputEncoding";
  public static final String PREF_WARN_OVERWRITING_EXPORT_RESULTS_FILE = "preference.warnOverwritingExportResultsFile";
  public static final String PREF_TOGGLE_SPAN_TOOLTIP = "preference.toggleSpanTooltip";
  public static final String PREF_SHOW_FILTER = "preference.showFilter";
  public static final String PREF_LOG_DEBUG_MESSAGES = "preference.logDebugMessages";
  
  private boolean showAdvancedTab = false;
  private boolean showEnableProvenance = false;
  private boolean enableReportProblem = true;
  private boolean validateInputEncoding = true;
  private boolean warnOverwritingExportResultsFile = true;
  private boolean toggleSpanToolTip = false;
  private boolean showFilter = false;
  private boolean logDebugMessages = false;
  
  public TextAnalyticsWorkspacePreferences (IPreferenceStore store, String bundleName)
  {
    preferenceStore = store;
    this.bundleName = bundleName;

    // Assign values to settings if they are not found in preference store.
    // Set values as string because if a setting is assigned boolean value false, it does not get saved.
    if (!preferenceStore.contains (PREF_ENABLE_REPORT_PROBLEM)) {
      store.setValue (PREF_ENABLE_REPORT_PROBLEM, "true");
    }

    if (!preferenceStore.contains (PREF_SHOW_ADVANCED_TAB)) {
      store.setValue (PREF_SHOW_ADVANCED_TAB, "false");
    }

    if (!preferenceStore.contains (PREF_SHOW_ENABLE_PROVENANCE)) {
      store.setValue (PREF_SHOW_ENABLE_PROVENANCE, "false");
    }

    if (!preferenceStore.contains (PREF_VALIDATE_INPUT_ENCODING)) {
      store.setValue (PREF_VALIDATE_INPUT_ENCODING, "true");
    }

    store.setValue (PREF_SHOW_FILTER, "false"); // always initialize with false

    if (!preferenceStore.contains (PREF_WARN_OVERWRITING_EXPORT_RESULTS_FILE)) {
      store.setValue (PREF_WARN_OVERWRITING_EXPORT_RESULTS_FILE, "true");
    }

    if (!preferenceStore.contains (PREF_TOGGLE_SPAN_TOOLTIP)) {
      store.setValue (PREF_TOGGLE_SPAN_TOOLTIP, "true");
    }

    if (!preferenceStore.contains (PREF_LOG_DEBUG_MESSAGES)) {
      store.setValue (PREF_LOG_DEBUG_MESSAGES, "false");
    }

    if (preferenceStore.needsSaving ()) {
      savePreferences ();
    }

    initCache ();
  }
  
  /**
   * Persist workspace text analytics settings.
   */
  public void savePreferences ()
  {
    try {
      InstanceScope.INSTANCE.getNode (bundleName).flush ();
    }
    catch (BackingStoreException e) {
      System.out.println (PreferencesPlugin.PLUGIN_ID + " : " + e.getLocalizedMessage ()); // Using System.out instead
                                                                                           // of LogUtil to prevent
                                                                                           // cyclic dependency with
                                                                                           // text analytics util plugin
      e.printStackTrace ();
    }
  }
  
  /**
   * Cache the settings in class fields.
   */
  private void initCache() {
    showAdvancedTab = preferenceStore.getBoolean (PREF_SHOW_ADVANCED_TAB);
    showEnableProvenance = preferenceStore.getBoolean (PREF_SHOW_ENABLE_PROVENANCE);
    enableReportProblem = preferenceStore.getBoolean (PREF_ENABLE_REPORT_PROBLEM);
    validateInputEncoding = preferenceStore.getBoolean (PREF_VALIDATE_INPUT_ENCODING);
    warnOverwritingExportResultsFile = preferenceStore.getBoolean (PREF_WARN_OVERWRITING_EXPORT_RESULTS_FILE);
    toggleSpanToolTip = preferenceStore.getBoolean (PREF_TOGGLE_SPAN_TOOLTIP);
    showFilter = preferenceStore.getBoolean (PREF_SHOW_FILTER);
    logDebugMessages = preferenceStore.getBoolean (PREF_LOG_DEBUG_MESSAGES);
  }
  
  /**
   * Get 'Show Advanced Tab' setting in Window->Preferences->BigInsights->TextAnalytics
   * @return boolean true if checked, false otherwise
   */
  public boolean getPrefShowAdvancedTab() {
    return showAdvancedTab;
  }
  
  /**
   * Set 'Show Advanced Tab' setting in preference store
   * @param showAdvancedTab boolean
   */
  public void setPrefShowAdvancedTab(boolean showAdvancedTab) {
    this.showAdvancedTab = showAdvancedTab;
    preferenceStore.setValue (PREF_SHOW_ADVANCED_TAB, String.valueOf (showAdvancedTab));
  }
  
  /**
   * Get 'Show Enable Provenance' setting in Window->Preferences->BigInsights->TextAnalytics
   * @return boolean true if checked, false otherwise
   */
  public boolean getPrefShowEnableProvenanceOption() {
    return showEnableProvenance;
  }
  
  /**
   * Set 'Show Enable Provenance' setting in preference store
   * @param showEnableProvenance boolean
   */
  public void setPrefShowEnableProvenanceOption(boolean showEnableProvenance) {
    this.showEnableProvenance = showEnableProvenance;
    preferenceStore.setValue (PREF_SHOW_ENABLE_PROVENANCE, String.valueOf (showEnableProvenance));
  }
  
  /**
   * Get 'Enable Problem Reporting' setting in Window->Preferences->BigInsights->TextAnalytics
   * @return boolean
   */
  public boolean getPrefEnableReportProblem() {
    return enableReportProblem;
  }
  
  /**
   * Set 'Enable Problem Reporting' setting in preference store
   * @param enableReportProblem boolean
   */
  public void setPrefEnableReportProblem(boolean enableReportProblem) {
    this.enableReportProblem = enableReportProblem;
    preferenceStore.setValue (PREF_ENABLE_REPORT_PROBLEM, String.valueOf (enableReportProblem));
  }
  
  /**
   * Get 'Validate Input Encoding' setting in Window->Preferences->BigInsights->TextAnalytics
   * @return boolean
   */
  public boolean getPrefValidateInputEncoding() {
    return validateInputEncoding;
  }
  
  /**
   * Set 'Validate Input Encoding' setting in preference store
   * @param validateInputEncoding boolean
   */
  public void setPrefValidateInputEncoding(boolean validateInputEncoding) {
    this.validateInputEncoding = validateInputEncoding;
    preferenceStore.setValue (PREF_VALIDATE_INPUT_ENCODING, String.valueOf (validateInputEncoding));
  }
  
  /**
   * Get 'Warn when overwriting export result files' setting in Window->Preferences->BigInsights->TextAnalytics
   * @return boolean
   */
  public boolean getPrefWarnOverwritingExportResultsFile() {
    return warnOverwritingExportResultsFile;
  }
  
  /**
   * Set 'Warn when overwriting export result files' setting in preference store
   * @param warn boolean
   */
  public void setPrefWarnOverwritingExportResultsFile(boolean warn) {
    this.warnOverwritingExportResultsFile = warn;
    preferenceStore.setValue (PREF_WARN_OVERWRITING_EXPORT_RESULTS_FILE, String.valueOf (warn));
  }
  
  /**
   * Get 'Span Tooltip' setting in Annotation explorer's action bar
   * @return boolean
   */
  public boolean getPrefToggleSpanToolTip() {
    return toggleSpanToolTip;
  }
  
  /**
   * Set 'Span Tooltip' setting in preference store
   * @param toggleSpanToolTip
   */
  public void setPrefToggleSpanToolTip(boolean toggleSpanToolTip) {
    this.toggleSpanToolTip = toggleSpanToolTip;
    preferenceStore.setValue (PREF_TOGGLE_SPAN_TOOLTIP, String.valueOf (toggleSpanToolTip));
  }
  
  /**
   * Get 'Show Filter' setting in Annotation explorer's action bar menu
   * @return boolean
   */
  public boolean getPrefShowFilter() {
    return showFilter;
  }
  
  /**
   * Set 'Show Filter' setting in preference store
   * @param showFilter boolean
   */
  public void setPrefShowFilter(boolean showFilter) {
    this.showFilter = showFilter;
    preferenceStore.setValue (PREF_SHOW_FILTER, String.valueOf (showFilter));
  }
  
  /**
   * Get 'Allow debug messages in log' setting in Window->Preferences->BigInsights->TextAnalytics
   * @return boolean
   */
  public boolean getPrefLogDebugMessages() {
    return logDebugMessages;
  }
  
  /**
   * Set 'Allow debug messages in log' setting in preference store
   * @param logDebugMessages boolean
   */
  public void setPrefLogDebugMessages(boolean logDebugMessages) {
    this.logDebugMessages = logDebugMessages;
    preferenceStore.setValue (PREF_LOG_DEBUG_MESSAGES, String.valueOf (logDebugMessages));
  }
}
