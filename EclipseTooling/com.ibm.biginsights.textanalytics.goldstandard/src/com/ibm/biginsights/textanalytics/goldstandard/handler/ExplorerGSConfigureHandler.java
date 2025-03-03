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
package com.ibm.biginsights.textanalytics.goldstandard.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.goldstandard.ui.ConfigurationDialog;

/**
 * This class handles the click event on the popup menu from package or project explorer.
 * This class is invoked when the context menu 'Labeled Document Collection -> Configure' 
 * is clicked on a gsFolder.
 *
 *  Krishnamurthy
 *
 */
public class ExplorerGSConfigureHandler extends GSActionHandler implements IHandler {



	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(super.execute(event) == null){
			return null;
		}
		ConfigurationDialog dialog = new ConfigurationDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.open();
		return null;
		
	}
}
