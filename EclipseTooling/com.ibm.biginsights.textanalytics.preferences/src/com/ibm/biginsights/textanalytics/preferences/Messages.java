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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	@SuppressWarnings("unused")


  private static final String BUNDLE_NAME = "com.ibm.biginsights.textanalytics.preferences.messages"; //$NON-NLS-1$
  public static String TextAnalyticsPreferencePage_AdvancedTabCheckBoxLabel;
  public static String TextAnalyticsPreferencePage_EnableReportProblemCheckBoxLabel;
  public static String TextAnalyticsPreferencePage_LogDebugMessagesCheckBoxLabel;
  public static String TextAnalyticsPreferencePage_ShowEnableProvenanceCheckBoxLabel;
  public static String TextAnalyticsPreferencePage_TextAnalalyticsWorkspaceSettingsDescription;
  public static String TextAnalyticsPreferencePage_ValidateInputEncodingCheckBoxLabel;
  public static String TextAnalyticsPreferencePage_WarnExportResultsOverwriteCheckBoxLabel;
  static {
    // initialize resource bundle
    NLS.initializeMessages (BUNDLE_NAME, Messages.class);
  }

  private Messages ()
  {}
}
