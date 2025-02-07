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
package com.ibm.datatools.quick.launch.ui.actions;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.datatools.quick.launch.ui.Activator;
import com.ibm.datatools.quick.launch.ui.Copyright;
import com.ibm.datatools.quick.launch.ui.actions.CheckboxMessageDialog.CheckBoxDefinition;
import com.ibm.datatools.quick.launch.ui.i18n.IAManager;
import com.ibm.datatools.quick.launch.ui.internal.pref.QuickLaunchPreferences;

/** Action to open a change perspectives. Intended to be used internally by quick launch
* 
* @since 2010May05
*/
public class OpenPerspectiveAction extends Action
{


    private String m_perspectiveId;

    public OpenPerspectiveAction( String id )
    {
        m_perspectiveId = id;
    }

    public void run()
    {
        try
        {

            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();

            if ( page != null )
            {

                IPerspectiveDescriptor perspective = workbench.getPerspectiveRegistry()
                        .findPerspectiveWithId( m_perspectiveId );

                if ( page.getPerspective() != perspective )
                {
                    Preferences prefs = Activator.getDefault().getPluginPreferences();

                    boolean bPrompt = prefs
                            .getBoolean( QuickLaunchPreferences.QUERY_CHANGE_PERSPECTIVE );

                    if ( bPrompt )
                    {
                        String question = MessageFormat
                                .format(
                                        IAManager.OpenPerspectiveAction_CHANGE_PERSPECTIVE_QUERY,
                                        new Object[]
                                            {
                                                perspective.getLabel()
                                            } );
                        PerspectiveSwitchMessage switcher = new PerspectiveSwitchMessage( window
                                .getShell(), MessageDialog.QUESTION, 
                                IAManager.OpenPerspectiveAction_CHANGE_PERSPECTIVE_TITLE, question, null,
                                new String[]
                                    {
                                            IAManager.OpenPerspectiveAction_YES_BTN, IAManager.OpenPerspectiveAction_NO_BTN
                                    }, 0,  new CheckBoxDefinition(IAManager.OpenPerspectiveAction_REMEMBER_CHOICE, m_perspectiveId, !bPrompt));                       
                        if ( switcher.open() == Dialog.OK )
                        {
                            page.setPerspective( perspective );
                        }
                        prefs.setValue( QuickLaunchPreferences.QUERY_CHANGE_PERSPECTIVE, !switcher
                                .isCheckboxSelected(m_perspectiveId) );
                        prefs.setValue( QuickLaunchPreferences.CHANGE_PERSPECTIVE, switcher
                                .getReturnCode() == Dialog.OK );

                    }
                    else if ( prefs.getBoolean( QuickLaunchPreferences.CHANGE_PERSPECTIVE ) )
                    {
                        page.setPerspective( perspective );

                    }
                }
                else {
                	MessageDialog.openInformation(window.getShell(), IAManager.OpenPerspectiveAction_CHANGE_PERSPECTIVE_TITLE, 
                			IAManager.bind(IAManager.OpenPerspectiveAction_ALREADY_OPEN, perspective.getLabel()));
                }
            }
        }
        catch ( Exception e )
        {
            Activator.log( e );
        }
    }

    class PerspectiveSwitchMessage extends CheckboxMessageDialog
    {

        public PerspectiveSwitchMessage( Shell parentShell, int type, 
                String title, String message, String detailMessage, String[] buttonsLabel,
                int defaultButtonIndex, CheckBoxDefinition... checkboxes )
        {
            super( parentShell, type, title, message, detailMessage, buttonsLabel, defaultButtonIndex, checkboxes );

        }

    }


}
