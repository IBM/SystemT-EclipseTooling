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

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.ui.internal.views.HelpView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.datatools.quick.launch.ui.Activator;
import com.ibm.datatools.quick.launch.ui.Copyright;
import com.ibm.datatools.quick.launch.ui.QuickLaunchConstants;
import com.ibm.datatools.quick.launch.ui.internal.pref.QuickLaunchPreferences;


/** Action to open help side bar
* 
* @since 2010May05
*/

@SuppressWarnings("restriction")
public class OpenHelpTaskAction extends Action
{


    String m_url = null;

    public OpenHelpTaskAction( String p_url)//, boolean p_internal )
    {
        m_url = p_url;
    }

    @Override
    public void run()
    {
        if ( PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null )
        {
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage();
            if ( page != null )
            {                
               launchHelp( page );  
            }
        }

    }

    private void launchHelp( IWorkbenchPage page )
    {
        try
        {
            Preferences prefs = Activator.getDefault().getPluginPreferences();

            boolean windowPref = prefs
                    .getBoolean( QuickLaunchPreferences.SHOW_HELP_WINDOW );

        	if( !windowPref)
        	{
	            IViewPart view = page.showView( QuickLaunchConstants.HELP_VIEW_ID ); //$NON-NLS-1$
	            if ( view instanceof HelpView )
	            {
	                ((HelpView)view).showHelp( m_url );
	            }
	        }
        	else{
        		PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(m_url );//+ "?noframes=true");
        	}
        }
        catch ( PartInitException e )
        {
            Activator.log( e );
        }
    }

    private void hideHelp( IWorkbenchPage page )
    {
        IAction hideView = new ShowViewAction( QuickLaunchConstants.HELP_VIEW_ID, "hide" ); //$NON-NLS-1$ 
        {
            hideView.run();
        }
    }
}
