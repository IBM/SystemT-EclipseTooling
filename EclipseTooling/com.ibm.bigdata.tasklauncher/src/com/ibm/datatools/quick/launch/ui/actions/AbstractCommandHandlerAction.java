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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

import com.ibm.datatools.quick.launch.ui.Activator;
import com.ibm.datatools.quick.launch.ui.Copyright;
/** Abstract command action super class.
* 
* @since 2010May05
*/

public abstract class AbstractCommandHandlerAction extends Action
{


    @Override
    public void run()
    {

        super.run();
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if ( window != null )
        {
            IWorkbenchPage page = window.getActivePage();

            if ( page != null )
            {

                         try
                        {
                            execute( page );
                        }
                        catch ( ExecutionException e )
                        {
                            Activator.log( e );
                        }
                        catch ( NotDefinedException e )
                        {
                            Activator.log( e );
                        }
                        catch ( NotEnabledException e )
                        {
                            Activator.log( e );
                        }
                        catch ( NotHandledException e )
                        {
                            Activator.log( e );
                        }

            }
        }
    }
    
    protected void execute( IWorkbenchPage page ) throws ExecutionException, NotDefinedException,
            NotEnabledException, NotHandledException
    {
        IHandlerService handlerService = (IHandlerService)page.getActivePart().getSite()
                .getService( IHandlerService.class );

        if ( handlerService != null )
        {
            execute( handlerService );
        }
    }

    protected void execute( IHandlerService handlerService ) throws ExecutionException,
            NotDefinedException, NotEnabledException, NotHandledException
    {
    }
    
}
