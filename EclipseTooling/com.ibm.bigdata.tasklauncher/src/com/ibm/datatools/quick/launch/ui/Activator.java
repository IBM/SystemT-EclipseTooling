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
package com.ibm.datatools.quick.launch.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @since 2010May05
 */
public class Activator extends AbstractUIPlugin
{



    // The plug-in ID
    public static final String PLUGIN_ID = "com.ibm.bigdata.tasklauncher"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    public static PerspectiveAdapter perspectiveListener;

    public static IPartListener2 partListener;

    /**
     * The constructor
     */
    public Activator()
    {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
        plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        super.stop( context );
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault()
    {
        return plugin;
    }

    public static void log( Throwable e )
    {
        String message = e.getMessage();
        Throwable old = e;
        Throwable error = e;
        IStatus status = null;
        if ( e instanceof InvocationTargetException )
        {
            error = ((InvocationTargetException)e).getTargetException();
            if ( error == null )
            {
                error = old;
            }
            else
            {
                if ( error.getMessage() != null )
                {
                    message = error.getMessage();
                }
            }
        }
        if ( error instanceof CoreException )
            status = ((CoreException)error).getStatus();
        else
        {
            if ( null == message )
            {
                message = ""; //$NON-NLS-1$
            }
            status = new Status( IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, error );
        }
        getDefault().getLog().log( status );
    }

    public static void log( String message )
    {
        log( new Status( IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null ) );
    }

    public static void log( IStatus status )
    {
        ResourcesPlugin.getPlugin().getLog().log( status );
    }



}
