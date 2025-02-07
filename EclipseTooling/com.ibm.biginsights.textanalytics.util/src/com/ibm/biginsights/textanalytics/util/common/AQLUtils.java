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
package com.ibm.biginsights.textanalytics.util.common;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;

import com.ibm.biginsights.textanalytics.util.ViewEditorInput;

/**
 * Some utilities to manipulate AQL view names.
 */
public class AQLUtils {



  private static final String AQL_EDITOR_EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.AQLEditor";

  public static final String getQualifiedFieldName(String viewName, String fieldName) {
    StringBuilder sb = new StringBuilder(viewName.length() + fieldName.length() + 1);
    sb.append(viewName);
    sb.append('.');
    sb.append(fieldName);
    return sb.toString();
  }

  public static void openAQLEditorForView (String projectName, String viewName, String mainViewName)
  {
    final ViewEditorInput vei = new ViewEditorInput (projectName, viewName, mainViewName);

    // We need to get WorkbenchWinfdow this way; otherwise PlatformUI.getWorkbench ().getActiveWorkbenchWindow ()
    // may return null when the Display does not run in the active window. 
    Display.getDefault ().asyncExec (new Runnable () {
      @Override
      public void run ()
      {
        IWorkbenchWindow iw = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ();
        try {
          TextEditor editor = (TextEditor) iw.getActivePage ().openEditor (vei, AQL_EDITOR_EDITOR_ID);
          if (editor != null && editor instanceof IAQLEditor) {
            int line = vei.getLine ();
            int lineOffset = vei.getOffset ();
            int len = vei.getLength ();

            ((IAQLEditor)editor).setCursorAndMoveTo (line, lineOffset);
            editor.selectAndReveal (((IAQLEditor)editor).getCaretOffset (), len);
          }
        }
        catch (PartInitException e1) {
        }
      }
    });
  }
}
