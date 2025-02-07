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

package com.ibm.biginsights.textanalytics.concordance.ui.filter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.concordance.ui.ConcordanceView;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class ShowFiltersHandler extends AbstractHandler {

	@SuppressWarnings("unused")


	protected static ILog log = LogUtil.getLogForPlugin(Activator.PLUGIN_ID);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
		IWorkbenchWindow window = part.getSite().getWorkbenchWindow();
	  final TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
		try {
		      ConcordanceView cv = (ConcordanceView) window.getActivePage().showView(
		              "com.ibm.biginsights.textanalytics.concordance.view", null, IWorkbenchPage.VIEW_VISIBLE);
		      cv.toggleFilterViewer();
		      wprefs.setPrefShowFilter (cv.getFilterViewer ().isDisplayed ());
		      wprefs.savePreferences ();
		      return cv;
//			return window.getActivePage().showView(
//					"com.ibm.biginsights.textanalytics.concordance.filterview",
//					null, IWorkbenchPage.VIEW_VISIBLE);
		} catch (PartInitException e) {
			log.logAndShowError("Concordance View could not be opened", e);
		}
		return null;
	}

}
