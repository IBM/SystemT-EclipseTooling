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

package com.ibm.biginsights.textanalytics.refactor.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class OpenRenameDialogHandler extends AbstractHandler
{



  @Override
  public Object execute (ExecutionEvent event) throws ExecutionException
  {
    // Comment out for now since ew haven't used it yet. Will need more params in order to use.
//    String aqlFilePath = event.getParameter("com.ibm.biginsights.textanalytics.refactor.openRenameDialog.aqlFile"); //$NON-NLS-1$
//    String lineNumStr = event.getParameter("com.ibm.biginsights.textanalytics.refactor.openRenameDialog.lineNumber"); //$NON-NLS-1$
//    String viewOffset = event.getParameter("com.ibm.biginsights.textanalytics.refactor.openRenameDialog.viewOffset"); //$NON-NLS-1$
//    String viewName = event.getParameter("com.ibm.biginsights.textanalytics.refactor.openRenameDialog.viewName"); //$NON-NLS-1$
//
//    int offset = Integer.parseInt (viewOffset);
//
//    try {
//      if (lineNumStr != null) {
//        int lineNum = Integer.parseInt (lineNumStr);
//        RefactorUtils.openRenameWizard(aqlFilePath, lineNum, offset, viewName);
//      }
//      else
//        RefactorUtils.openRenameWizard(aqlFilePath, offset, viewName);
//    }
//    catch (Exception e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }

    return null;
  }

}
