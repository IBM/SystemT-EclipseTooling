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

package com.ibm.biginsights.textanalytics.aql.editor.navigate;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * NavigationHandler class is responsible for all F3 navigations, this class contains the logic to calculate the offsets
 * for the definitions of AQL constructs and reveal them in editor.
 * 
 *  Babbar
 *  Kalakuntla
 *  Simon
 */
public class NavigationHandler extends AbstractHandler
{

  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  public static final String EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.AQLEditor"; //$NON-NLS-1$
  public static final String DICT_EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.DictEditor"; //$NON-NLS-1$
  public static final String KEYWORD_TOKEN = "AQL_Keyword";//$NON-NLS-1$

  @Override
  public Object execute (ExecutionEvent event)
  {
    IEditorPart editor = HandlerUtil.getActiveEditor (event);
    Assert.isNotNull (editor);
    Assert.isLegal (editor instanceof AQLEditor);
    AQLEditor aqlEditor = (AQLEditor) editor;
    IEditorInput genericInput = editor.getEditorInput ();
    if (!(genericInput instanceof IFileEditorInput)) { return null; }
    IFileEditorInput input = (IFileEditorInput) genericInput;
    IFile currentFile = input.getFile ();
    ISelection selection = HandlerUtil.getCurrentSelection (event);
    Assert.isNotNull (selection);
    Assert.isTrue (selection instanceof ITextSelection);
    ITextSelection textSelection = (ITextSelection) selection;
    AQLNavigator navigator = null;
    if (ProjectUtils.isModularProject (currentFile.getProject ())) {
      navigator = new ModularAQLNavigator (aqlEditor, currentFile, textSelection);
    }
    else {
      navigator = new NonModularAQLNavigator (aqlEditor, currentFile, textSelection);
    }
    navigator.navigateToDefinition ();
    return null;
  }

}


