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

package com.ibm.biginsights.textanalytics.explain.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.explain.Activator;
import com.ibm.biginsights.textanalytics.explain.Messages;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class OpenExplainModule implements IObjectActionDelegate
{

  @SuppressWarnings("unused")
private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

  IFile tamFile = null;

  public OpenExplainModule ()
  {
  }

  @Override
  public void run (IAction action)
  {
    IWorkbench wb = PlatformUI.getWorkbench ();
    IWorkbenchWindow window = wb.getActiveWorkbenchWindow ();
    IWorkbenchPage page = window.getActivePage ();

    if (tamFile != null) {
      try {
        ExplainModuleView emView = (ExplainModuleView)page.findView (ExplainModuleView.viewID);
        if (emView != null) {
          String tamPath = tamFile.getFullPath ().toString ();
          emView.setModuleToExplain (tamPath, true);
        }
        else {
          ExplainModuleView.tamFile = tamFile;
          page.showView ("com.ibm.biginsights.textanalytics.explain.views.ExplainModuleView");
        }
      }
      catch (PartInitException e) {
        MessageDialog.openError (Display.getDefault ().getActiveShell (), "title", Messages.ExplainModuleView_ERROR_OPEN_EMV);
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.ExplainModuleView_ERROR_OPEN_EMV, e);
      }
    }

  }

  @Override
  public void selectionChanged (IAction action, ISelection selection)
  {
    tamFile = null;

    if (selection instanceof IStructuredSelection) {
      IStructuredSelection sel = (IStructuredSelection) selection;

      if (sel.size () == 1) {
        Object selObj = sel.getFirstElement ();
        if (selObj instanceof IFile && ((IFile)selObj).getName ().toLowerCase ().endsWith (".tam")) {
          tamFile = (IFile)selObj;
        }
      }
    }
  }

  @Override
  public void setActivePart (IAction action, IWorkbenchPart targetPart)
  {
  }

}
