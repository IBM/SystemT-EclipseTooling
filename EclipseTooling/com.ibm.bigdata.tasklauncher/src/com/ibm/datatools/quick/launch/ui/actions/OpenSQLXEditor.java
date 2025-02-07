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
import org.eclipse.ui.handlers.IHandlerService;

import com.ibm.datatools.quick.launch.ui.Copyright;
/** Action to open the object list editor for connections. Intended to be used internally by quick launch
* 
* @since 2010May05
*/

public class OpenSQLXEditor extends AbstractCommandHandlerAction
{


    public static final String NEW_SQL_SCRIPT_LIST_ID = "com.ibm.datatools.sqlxeditor.extensions.commands.NewSQLScript";//$NON-NLS-1$
    
    
    @Override
    protected void execute( IHandlerService handlerService ) throws ExecutionException,
            NotDefinedException, NotEnabledException, NotHandledException
    {
        handlerService.executeCommand( NEW_SQL_SCRIPT_LIST_ID, null );
    }
    



}
