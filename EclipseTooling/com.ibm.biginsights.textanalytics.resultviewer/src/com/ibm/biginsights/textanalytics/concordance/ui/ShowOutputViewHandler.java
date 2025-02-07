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
package com.ibm.biginsights.textanalytics.concordance.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowOutputViewHandler extends AbstractHandler {



  @Override
  public Object execute(ExecutionEvent event) {
    String viewName = event
        .getParameter("com.ibm.biginsights.textanalytics.resultviewer.views.view"); //$NON-NLS-1$
    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
    IViewReference[] viewRefs = window.getActivePage().getViewReferences();
    for (IViewReference ref : viewRefs) {
      if (ref.getId().equals(ConcordanceView.VIEW_ID)) {
        // We know the view is visible, otherwise the menu could not have been triggered
        ((ConcordanceView) ref.getView(false)).showTableView(viewName, null, null);
      }
    }
    return null;
  }

}
