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

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.ibm.datatools.quick.launch.ui.Copyright;

/** Action to open a change perspectives. Intended to be used internally by quick launch
* 
* @since 2010May05
*/
public class OpenNewWizardCommandAction extends AbstractCommandHandlerAction
{


    public static final String OPEN_NEW_WIZARD = "org.eclipse.ui.newWizard"; //$NON-NLS-1$
    public static final String NEW_WIZARD_ID = "newWizardId"; //$NON-NLS-1$


    private String wizardId;

    public OpenNewWizardCommandAction( String wizardId )
    {
        this.wizardId = wizardId;
    }

    @Override
    protected void execute( IWorkbenchPage page ) throws ExecutionException,
            NotDefinedException, NotEnabledException, NotHandledException
    {

        ICommandService commandService = (ICommandService)page.getActivePart().getSite()
                .getService( ICommandService.class );

        if ( commandService != null )
        {
            Command command = commandService.getCommand( OPEN_NEW_WIZARD );
            
            IParameter urlParm = command
                    .getParameter( NEW_WIZARD_ID );
            Parameterization parm = new Parameterization( urlParm, this.wizardId );
            ParameterizedCommand parmCommand = new ParameterizedCommand( command,
                    new Parameterization[]
                        {
                            parm
                        } );

            IHandlerService handlerService = (IHandlerService)page.getActivePart().getSite()
                    .getService( IHandlerService.class );

            if ( handlerService != null )
            {
                handlerService.executeCommand( parmCommand, null );
            }
        }
    }

}
