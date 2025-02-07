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
package com.ibm.biginsights.textanalytics.concordance.ui.pagination;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;

import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class is the handler class called when "next page" is clicked on the
 * Annotation Explorer.
 * 
 *  Madiraju
 * 
 */
public class ShowNextPageHandler extends AbstractPageHandler{


	
	private static final String resultViewerNextPageCommandParameter = "com.ibm.biginsights.textanalytics.resultviewer.np.originatingview"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) {

		try {
			tracker.setCurrentPage(tracker.getCurrentPage()+ 1);
			final List<IFile> resFiles = tracker.getFiles(tracker.getCurrentPage());
			String originTableView = event.getParameter(resultViewerNextPageCommandParameter); //Value present when Show next page command is called from Table view.
			if (originTableView == null) {
				originTableView = ""; //Show next page command issued from Annotation Explorer does not provide any parameter; Setting it to "".
			}
			super.displayPage(resFiles,originTableView);
			enableDisableButtons();
		} catch (PartInitException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.error, e);
		} catch (CoreException e) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Messages.error, e);
		}
		return Status.OK_STATUS;
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
