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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;

import com.ibm.datatools.quick.launch.ui.Activator;
import com.ibm.datatools.quick.launch.ui.Copyright;

/** Action to show, hide, minimize, maximize, or restore a view. Intended to be used internally by quick launch
* 
* @since 2010May05
*/
@SuppressWarnings("restriction")
public class ShowViewAction extends Action
{


    String m_viewId = null;

    int m_state;

    boolean m_show;

    public ShowViewAction( String p_viewId, String p_state )
    {
        m_viewId = p_viewId;
        setState( p_state );
    }

    protected void setState( String p_state )
    {
        if ( p_state != null )
        {
            String state = p_state.trim();
            m_show = true;
            if ( "minimize".equals( state ) ) //$NON-NLS-1$
            {
                m_state = IWorkbenchPage.STATE_MINIMIZED;
            }
            else if ( "maximize".equals( state ) ) //$NON-NLS-1$
            {
                m_state = IWorkbenchPage.STATE_MAXIMIZED;
            }
            else if ( "hide".equals( state ) ) //$NON-NLS-1$
            {
                m_show = false;
                m_state = IWorkbenchPage.STATE_MAXIMIZED;
            }
            else
            // show or restore
            {
                m_state = IWorkbenchPage.STATE_RESTORED;
            }
        }
        else
        {
            m_state = IWorkbenchPage.STATE_RESTORED;
            m_show = true;
        }
    }

    @Override
    public void run()
    {
        try
        {
            if ( PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null )
            {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                IWorkbenchPage pg = window.getActivePage();
                if ( pg instanceof WorkbenchPage )
                {
                    WorkbenchPage page = (WorkbenchPage)pg;
                    IViewPart view = page.findView( m_viewId );
                    if ( m_show )
                    {
                        if ( view == null )
                        {
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                                    .showView( m_viewId ); //$NON-NLS-1$
                            view = page.findView( m_viewId );
                        }

                        if ( view != null )
                        {
                            IViewReference ref = page.findViewReference( m_viewId );
                            
                            // Fast view commented out to make it compile in 4.2.2 framework
                            //boolean fastview = page.isFastView( ref );
                            int state = page.getPartState( ref );

                            // System.out.println("beg state: c="+state+" n="+m_state+" f="+page.isFastView(
                            // ref ));
                            // System.out.println("\tid="+m_viewId);

                           /* if ( m_state == IWorkbenchPage.STATE_RESTORED &)
                            {
                                page.removeFastView( ref );
                            } */

                            if ( state != m_state )
                            {

                                if ( m_state != IWorkbenchPage.STATE_MINIMIZED )
                                {
                                    page.setPartState( ref, m_state );
                                }

                            }

                            if ( m_state != IWorkbenchPage.STATE_MINIMIZED )
                            {
                                page.setPartState( ref, m_state );
                                page.bringToTop( view );
                            }

                            /*if ( m_state == IWorkbenchPage.STATE_MINIMIZED && !fastview )
                            {
                                page.addFastView( ref );
                            }*/
                            // System.out.println("part "+part+" cur="+state+" req="+m_state);
                        }
                    }
                    else
                    {
                        if ( view != null )
                        {
                            page.hideView( view );
                        }
                    }

                }
            }
        }
        catch ( PartInitException e )
        {
        	Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
        }
    }



}
