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
package com.ibm.datatools.quick.launch.ui.i18n;

import org.eclipse.osgi.util.NLS;

/**
 * Handles Information Architecture Resources
 * 
 * 
 * @version 1.0.0
 * 
 *          Created on Mqy 4, 2010
 */
public class IAManager extends NLS
{



	private static final String BUNDLE_NAME = "com.ibm.datatools.quick.launch.ui.i18n.messages"; //$NON-NLS-1$


	static
	{
		NLS.initializeMessages(BUNDLE_NAME, IAManager.class);
	}

    public static String OpenHelpAction_GET_HELP_QUERY;
    public static String OpenHelpAction_HELP_DIALOG_TITLE;
    public static String OpenHelpAction_HELP_REMEMBER_CHOICE;
    public static String OpenHelpAction_NO_BTN;
    public static String OpenHelpAction_YES_BTN;
    public static String OpenPerspectiveAction_ALREADY_OPEN;
    public static String OpenPerspectiveAction_CHANGE_PERSPECTIVE_QUERY;
    public static String OpenPerspectiveAction_CHANGE_PERSPECTIVE_TITLE;
    public static String OpenPerspectiveAction_NO_BTN;
    public static String OpenPerspectiveAction_REMEMBER_CHOICE;
    public static String OpenPerspectiveAction_YES_BTN;
    public static String QuickLaunchEditorInput_QUICK_LAUNCH_INPUT;
    public static String QuickLaunchEditorInput_QUICK_LAUNCH_TOOLTIP;
    public static String OpenSQLEditor_SCRIPT_EXT;
    public static String OpenSQLEditor_SCRIPT_FILE_BASE;
    public static String QuickLaunchEditor_DEFAULT_FORM;
	public static String QuickLaunchEditor_Preferences;
	public static String QuickLaunchEditor_TaskLauncher;
	public static String QuickLaunchPreferences_AlwaysChangePerspectives;
	public static String QuickLaunchPreferences_AlwaysShowHelp;
	public static String QuickLaunchPreferences_CloseLauncher;
	public static String QuickLaunchPreferences_TaskLauncher;
	public static String QuickLaunchPreferences_TaskLauncher_Desc;
	public static String QuickLaunchPreferences_DoNotShowDialog;
	public static String QuickLaunchPreferences_NeverChangePerspective;
	public static String QuickLaunchPreferences_NeverShowHelp;
	public static String QuickLaunchPreferences_PerspectivePrompt;
	public static String QuickLaunchPreferences_PromptShowHelp;
	public static String QuickLaunchPreferences_ShowExternalHelp;
	public static String QuickLaunchPreferences_ShowHelp;
	public static String QuickLaunchPreferences_SwitchPerspectives;
	public static String QuickLaunchSolutionManager_CloseDialogButton;
	public static String QuickLaunchSolutionManager_CloseDialogMessage;
	public static String QuickLaunchSolutionManager_CloseDialogMessage2;
	public static String QuickLaunchSolutionManager_CloseDialogPrompt;
	public static String QuickLaunchSolutionManager_CloseDialogPrompt2;
	public static String QuickLaunchSolutionManager_CloseDialogTitle;
}
