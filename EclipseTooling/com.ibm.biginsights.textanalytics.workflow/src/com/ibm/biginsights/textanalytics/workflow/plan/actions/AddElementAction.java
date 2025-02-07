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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.AddExampleWizard;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action to add a new example to a given label, or define a new label for it
 * 
 * 
 */
public class AddElementAction extends Action
{



  public static final Image icon = Icons.EXAMPLE_ICON;
  public static final String text = Messages.add_element_text;
  public static final String tooltiptext = Messages.add_element_tootltip;

  private ExampleModel model;

  public AddElementAction (String exampleText, String filepath, String label, int offset, int length)
  {
    super (text, ImageDescriptor.createFromImage (icon));
    setToolTipText (tooltiptext);

    model = new ExampleModel (exampleText, filepath, label, offset, length);
  }

  public AddElementAction (String _text, String _tooltip, Image _icon)
  {
    super (_text, ImageDescriptor.createFromImage (_icon));
    setToolTipText (_tooltip);

    model = null;
  }

  public void run ()
  {
    if (ActionPlanView.projectName == null)
      return;

    Shell shell = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getShell ();

    final AddExampleWizard w = new AddExampleWizard (model);

    final WizardDialog dialog = new WizardDialog (shell, w);
    final int rc = dialog.open ();
    if (rc == Window.OK) {
      w.execute ();
    }
  }
}
