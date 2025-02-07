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
package com.ibm.biginsights.textanalytics.workflow.plan.actions;

import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLEditorUtils;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.refactor.util.RefactorUtils;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.AQLNodeModel;
import com.ibm.biginsights.textanalytics.workflow.plan.validators.NoEmptyComponentInputValidator;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;

/**
 * action to rename a given node in the action plan
 * 
 * 
 */
public class RenameAction extends Action
{



  protected ActionPlanView plan;
  protected TreeObject element;

  public RenameAction (TreeObject element, ActionPlanView plan)
  {
    this.element = element;
    this.plan = plan;

    setText (Messages.rename_text);
    setToolTipText (Messages.rename_tootltip);
    setImageDescriptor (ImageDescriptor.createFromImage (PlatformUI.getWorkbench ().getSharedImages ().getImage (
      ISharedImages.IMG_ETOOL_PRINT_EDIT)));
  }

  public void run ()
  {
    Shell shell = plan.getViewer ().getControl ().getShell ();

    if ( element instanceof AqlNode ) {
       if ( openRenameDialog ((AqlNode)element) )
        return;
       else
         ;    // TODO create special dialog for renaming the AQL node
              //      for now, just share the dialog below with label
    }

    // make a new dialog to get parameters from the user
    InputDialog dialog = new InputDialog (shell, Messages.rename_input_title, Messages.rename_input_message,
      element.getLabel (), new NoEmptyComponentInputValidator ());

    // open the dialog to request the new name
    final int rc = dialog.open ();

    // on ok
    if (rc == Window.OK) {
      String value = dialog.getValue ();
      if (value != null && !value.trim ().isEmpty ()) {

        rename (value);
      }
    }
  }

  private void rename (String value)
  {
    element.setLabel (value);
    plan.serializeAndRefresh ();
  }

  private boolean openRenameDialog (AqlNode aqlNode)
  {
    AQLNodeModel model = aqlNode.getAQLNodeModel ();
    int[] viewLoc = null;

    String filePath = aqlNode.getAqlfilepathFromModuleAndFile (model.getModuleName (), model.getFileName ());

    if ( filePath != null &&
         (viewLoc = AQLEditorUtils.getViewLocationInFile (ActionPlanView.projectName, filePath, model.getViewname ())) != null ) {

      try {
        String projectName = ActionPlanView.projectName;
        String moduleName = model.getModuleName ();
        String viewName = model.getViewname ();
        RefactorUtils.openRenameWizard(filePath, viewLoc[0], viewLoc[1], projectName, moduleName, viewName, ElementType.VIEW);
        AqlProjectUtils.reloadExtractionPlanForProject (projectName);
        return true;
      }
      catch (IOException e) {
        // Do nothing. A 'false' will be returned.
      }
    }

    return false;
  }
}
