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

package com.ibm.biginsights.textanalytics.tableview.ui.pagination;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import com.ibm.biginsights.textanalytics.tableview.Activator;
import com.ibm.biginsights.textanalytics.tableview.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.util.PaginationTracker;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class ShowNextPageHandler extends AbstractHandler {


	
	private static final String resultViewerCommandShowNextPage = "com.ibm.biginsights.textanalytics.resultviewer.commands.ShowNextPage"; //$NON-NLS-1$
	private static final String resultViewerNextPageCommandParameter = "com.ibm.biginsights.textanalytics.resultviewer.np.originatingview"; //$NON-NLS-1$
	private static final String tableViewCommandShowNextPage = "com.ibm.biginsights.textanalytics.tableview.ShowNextPage"; //$NON-NLS-1$
	private static final String tableViewCommandShowPrevPage = "com.ibm.biginsights.textanalytics.tableview.ShowPrevPage"; //$NON-NLS-1$

	private PaginationTracker tracker = PaginationTracker.getInstance();
	
	@Override
	public Object execute(ExecutionEvent arg0) {
		try {
		IParameter param;
		IViewSite site = (IViewSite) HandlerUtil.getActiveSiteChecked(arg0);
		String tableViewName = site.getSecondaryId();

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(arg0);
		ICommandService cServ = (ICommandService) window.getService(ICommandService.class);
		Command concordanceShowNextPage = cServ.getCommand(resultViewerCommandShowNextPage);
		
		param = concordanceShowNextPage.getParameter(resultViewerNextPageCommandParameter);

		Parameterization params = new Parameterization(param,tableViewName);
		ParameterizedCommand pCommand = new ParameterizedCommand(concordanceShowNextPage,new Parameterization[] {params});
		
		IHandlerService hServ = (IHandlerService) window.getService(IHandlerService.class);
		hServ.executeCommand(pCommand, null);
		} catch (NotDefinedException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.getString("error"), e);
		} catch (NotEnabledException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.getString("error"), e);
		} catch (NotHandledException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.getString("error"), e);
		} catch (ExecutionException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.getString("error"), e);
		}
		
		refreshButtonState();
		
		return Status.OK_STATUS;
		
	}
	
	private void refreshButtonState() {
		  ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		  Command command = commandService.getCommand(tableViewCommandShowNextPage);
		  IHandler nextHandler =command.getHandler();
		  nextHandler.isEnabled();
		  Command command2 = commandService.getCommand(tableViewCommandShowPrevPage);
		  IHandler prevHandler = command2.getHandler();
		  prevHandler.isEnabled();
	}
	
	/**
	 * This method is called explicitly to enable/disable the NextPage button. If the current page is less then the total number of pages, the button is enabled.
	 */
	public boolean isEnabled()
	{
		  if (tracker.getCurrentPage() < tracker.getTotalNumberOfPages())
		  {
			  this.setBaseEnabled(true);
			  return true;
		  }
		  else
		  {
			  this.setBaseEnabled(false);
			  return false;
		  }
	}

}
