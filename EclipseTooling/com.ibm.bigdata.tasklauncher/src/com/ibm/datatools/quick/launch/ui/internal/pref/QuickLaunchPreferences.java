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
package com.ibm.datatools.quick.launch.ui.internal.pref;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.ibm.datatools.quick.launch.ui.Activator;
import com.ibm.datatools.quick.launch.ui.i18n.IAManager;

/** Used to manipulate the quick launch preferences. Intended to be used internally by quick launch
* 
* @since 2010May05
*/
public class QuickLaunchPreferences extends PreferencePage implements IWorkbenchPreferencePage
{



    public static final String QUERY_CHANGE_PERSPECTIVE = "com.ibm.datatools.quick.launch.ui.changePerspective.query"; //$NON-NLS-1$ 

    public static final String CHANGE_PERSPECTIVE = "com.ibm.datatools.quick.launch.ui.changePerspective.action"; //$NON-NLS-1$ 

    public static final String QUERY_SHOW_HELP = "com.ibm.datatools.quick.launch.ui.showHelp.query"; //$NON-NLS-1$ 

    public static final String SHOW_HELP = "com.ibm.datatools.quick.launch.ui.showHelp.action"; //$NON-NLS-1$ 

    public static final String QUERY_ON_CLOSE = "com.ibm.datatools.quick.launch.ui.onClose.information"; //$NON-NLS-1$ 
    
    public static final String SHOW_HELP_WINDOW = "com.ibm.datatools.quick.launch.ui.showHelpWindow.action"; //$NON-NLS-1$
    
    public static final String SHOW_TASKLAUNCHER = "com.ibm.datatools.quick.launch.ui.showTaskLauncher"; //$NON-NLS-1$

    IWorkbench m_workbench;

    //Thomas: no perspective change in August
//    Button m_queryChangePerspective;

//    Button m_alwaysChangePerspective;

//    Button m_neverChangePerspective;

    //Button m_queryShowHelp;

    Button m_alwaysShowHelp;

    Button m_neverShowHelp;

    Button m_neverPrompOnEditorClose;
    
    Button m_showHelpWindow;

    Button m_showTaskLauncher;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse
     * .swt.widgets.Composite)
     */
    protected Control createContents( Composite parent )
    {

        Composite page = new Composite( parent, SWT.NONE );
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        page.setLayout( gridLayout );

        createEditorPreferences( page );

        return page;
    }

    private void createEditorPreferences( Composite p_parent )
    {
        TableWrapLayout layout = new TableWrapLayout();
        p_parent.setLayout( layout );
        // ---------------------------------------------------------------------------
        // select which editor to be a preferred editor for changing
        // perspectives
        // ---------------------------------------------------------------------------
        //Thomas: commented out perspective change group for August
        /*
        Group perspectiveGroup = new Group( p_parent, SWT.NONE );
        perspectiveGroup.setText( IAManager.QuickLaunchPreferences_SwitchPerspectives );
        perspectiveGroup.setLayout( new GridLayout( 1, false ) );
        perspectiveGroup.setLayoutData( new TableWrapData( TableWrapData.FILL ) );

        boolean promptPerspective = getPreferences().getBoolean( QUERY_CHANGE_PERSPECTIVE );
        boolean changePerspective = getPreferences().getBoolean( CHANGE_PERSPECTIVE );

        // prompt change perspective
        m_queryChangePerspective = new Button( perspectiveGroup, SWT.RADIO );
        m_queryChangePerspective.setText( IAManager.QuickLaunchPreferences_PerspectivePrompt );
        m_queryChangePerspective.setSelection( promptPerspective );
        // always change perspective
        m_alwaysChangePerspective = new Button( perspectiveGroup, SWT.RADIO );
        m_alwaysChangePerspective.setText( IAManager.QuickLaunchPreferences_AlwaysChangePerspectives );
        m_alwaysChangePerspective.setSelection( !promptPerspective && changePerspective );
        // never change perspective
        m_neverChangePerspective = new Button( perspectiveGroup, SWT.RADIO );
        m_neverChangePerspective.setText( IAManager.QuickLaunchPreferences_NeverChangePerspective );
        m_neverChangePerspective.setSelection( !promptPerspective && !changePerspective );
		*/
        // ---------------------------------------------------------------------------
        // select which editor to be a preferred editor for showing help
        // ---------------------------------------------------------------------------
        Group showHelpGroup = new Group( p_parent, SWT.NONE );
        showHelpGroup.setText( IAManager.QuickLaunchPreferences_ShowHelp );
        showHelpGroup.setLayout( new GridLayout( 1, false ) );
        showHelpGroup.setLayoutData( new TableWrapData( TableWrapData.FILL ) );

       // boolean promptShowHelp = getPreferences().getBoolean( QUERY_SHOW_HELP );
        boolean showHelp = getPreferences().getBoolean( SHOW_HELP );
        boolean showHelpWindow = getPreferences().getBoolean( SHOW_HELP_WINDOW );        

        // prompt show help
//        m_queryShowHelp = new Button( showHelpGroup, SWT.RADIO );
//        m_queryShowHelp.setText( IAManager.QuickLaunchPreferences_PromptShowHelp );
//        m_queryShowHelp.setSelection( promptShowHelp );
        // always show help
        m_alwaysShowHelp = new Button( showHelpGroup, SWT.RADIO );
        m_alwaysShowHelp.setText( IAManager.QuickLaunchPreferences_AlwaysShowHelp );
        m_alwaysShowHelp.setSelection(showHelp );
        // never show help
        m_neverShowHelp = new Button( showHelpGroup, SWT.RADIO );
        m_neverShowHelp.setText( IAManager.QuickLaunchPreferences_NeverShowHelp );
        m_neverShowHelp.setSelection(!showHelp );
        
        m_showHelpWindow = new Button( showHelpGroup, SWT.CHECK );
        m_showHelpWindow.setText( IAManager.QuickLaunchPreferences_ShowExternalHelp );
        m_showHelpWindow.setSelection( showHelpWindow );

        SelectionListener activate = new ActivateExternalHelpOptionListener();
       // m_queryShowHelp.addSelectionListener(activate);
        m_alwaysShowHelp.addSelectionListener(activate);
        SelectionListener deactivate = new DeactivateExternalHelpOptionListener();
        m_neverShowHelp.addSelectionListener(deactivate);
        
        // ---------------------------------------------------------------------------
        // select whether to popup a dialog explaining how to get quick launch
        // back
        // ---------------------------------------------------------------------------
        Group onCloseGroup = new Group( p_parent, SWT.NONE );
        onCloseGroup.setText( IAManager.QuickLaunchPreferences_CloseLauncher );
        onCloseGroup.setLayout( new GridLayout( 1, false ) );
        onCloseGroup.setLayoutData( new TableWrapData( TableWrapData.FILL ) );

        boolean neverPromptOnCloseEditor = getPreferences().getBoolean( QUERY_ON_CLOSE );

        // prompt show help
        m_neverPrompOnEditorClose = new Button( onCloseGroup, SWT.CHECK );
        m_neverPrompOnEditorClose.setText( IAManager.QuickLaunchPreferences_DoNotShowDialog );
        m_neverPrompOnEditorClose.setSelection( neverPromptOnCloseEditor );

        // ---------------------------------------------------------------------------
        // select whether to show the task launcher or not
        // ---------------------------------------------------------------------------
        Group grpShowTaskLauncher = new Group( p_parent, SWT.NONE );
        grpShowTaskLauncher.setText( IAManager.QuickLaunchPreferences_TaskLauncher );
        grpShowTaskLauncher.setLayout( new GridLayout( 1, false ) );
        grpShowTaskLauncher.setLayoutData( new TableWrapData( TableWrapData.FILL ) );

        boolean showTaskLauncher = getPreferences().getBoolean( SHOW_TASKLAUNCHER );

        // prompt show help
        m_showTaskLauncher = new Button( grpShowTaskLauncher, SWT.CHECK );
        m_showTaskLauncher.setText( IAManager.QuickLaunchPreferences_TaskLauncher_Desc );
        m_showTaskLauncher.setSelection( showTaskLauncher );

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk()
    {
        Preferences p = getPreferences();
        //Thomas: no perspective change in August
//        p.setValue( QUERY_CHANGE_PERSPECTIVE, m_queryChangePerspective.getSelection() );
//        p.setValue( CHANGE_PERSPECTIVE, !m_neverChangePerspective.getSelection() );
       // p.setValue( QUERY_SHOW_HELP, m_queryShowHelp.getSelection() );
        p.setValue( SHOW_HELP, !m_neverShowHelp.getSelection() );
        p.setValue( QUERY_ON_CLOSE, m_neverPrompOnEditorClose.getSelection() );
        p.setValue( SHOW_HELP_WINDOW, m_showHelpWindow.getSelection() );
        p.setValue( SHOW_TASKLAUNCHER, m_showTaskLauncher.getSelection() );

        return true;
    }

    private Preferences getPreferences()
    {
        return Activator.getDefault().getPluginPreferences();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults()
    {
    	// Thomas: no perspective change in August
//        this.m_queryChangePerspective.setSelection( true );
//        this.m_alwaysChangePerspective.setSelection( false );
//        this.m_neverChangePerspective.setSelection( false );
    	
    	//this.m_queryShowHelp.setSelection( false ); 
    	this.m_alwaysShowHelp.setSelection( true ); 
    	this.m_neverShowHelp.setSelection( false );
    	this.m_neverPrompOnEditorClose.setSelection( false );
    	this.m_showHelpWindow.setSelection(false);
    	this.m_showTaskLauncher.setSelection(true); 
    }
    
    private class ActivateExternalHelpOptionListener extends SelectionAdapter
    {	
    	@Override
		public void widgetSelected(SelectionEvent e) {
			m_showHelpWindow.setEnabled(true);
		}
    	
    }
    private class DeactivateExternalHelpOptionListener extends SelectionAdapter
    {	
    	@Override
		public void widgetSelected(SelectionEvent e) {
			m_showHelpWindow.setEnabled(false);
		}
    	
    }
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init( IWorkbench p_workbench )
    {
        m_workbench = p_workbench;

    }



}
