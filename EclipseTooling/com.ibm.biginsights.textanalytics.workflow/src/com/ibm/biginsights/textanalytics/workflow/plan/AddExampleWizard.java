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
package com.ibm.biginsights.textanalytics.workflow.plan;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.LabelModel;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;

/**
 * wizard used to create a new example for the Action Plan
 * 
 *
 */
@SuppressWarnings("unused")
public class AddExampleWizard extends Wizard
{



  private ExampleModel model;

  CreateLabelPage label;

  private boolean createAQLs;
  private String title;

  boolean canFinish;

  private String newLabelName = null;
  private LabelNode newLabelParentLabel = null;

  /**
   * constructor that sets the model for the example to be created
   * @param model 
   */
  public AddExampleWizard (ExampleModel model)
  {
    this.setWindowTitle (Messages.example_wizard_window_title);
    this.model = model;
  }

  /**
   * sets if this example will need to create its own AQL files
   * @param createAQLs
   */
  public void setCreateAQLs (boolean createAQLs)
  {
    this.createAQLs = createAQLs;
  }

  /**
   * sets the title
   * @param title
   */
  public void setTitle (String title)
  {
    this.title = title;
  }
  
  @Override
  public void addPages ()
  {
    IWorkbenchPage page = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();

    ActionPlanView view = (ActionPlanView) page.findView (ActionPlanView.ID);
    if (view != null) {
      label = new CreateLabelPage ("#label", this);
      addPage (label);
      canFinish = false;
    }
  }

  /**
   * creates the 
   */
  public void execute ()
  {
    ActionPlanView plan = AqlProjectUtils.getActionPlanView ();
    if (plan == null || !plan.ready () || StringUtils.isEmpty (newLabelName))
      return;

    LabelNode newLabelNode = new LabelNode (new LabelModel (newLabelName));

    if (newLabelParentLabel != null)
      newLabelParentLabel.addSubLabel (newLabelNode, null);
    else
      plan.getContentProvider ().addChild (newLabelNode);

    // add example if it exists
    if (newLabelNode != null && model != null)
      plan.addExample (model, newLabelNode);

    plan.serializeAndExpand (null);
  }

  public void setCanFinish (boolean canFinish)
  {
    this.canFinish = canFinish;
  }

  public boolean canFinish ()
  {
    return canFinish;
  }

  @Override
  public boolean performFinish ()
  {
    return true;
  }

  /**
   * @param newLabelName the newLabelName to set
   */
  public void setNewLabelName (String newLabelName)
  {
    this.newLabelName = newLabelName;
  }

  /**
   * @param newLabelParentLabel the newLabelParentLabel to set
   */
  public void setNewLabelParentLabel (LabelNode newLabelParentLabel)
  {
    this.newLabelParentLabel = newLabelParentLabel;
  }

}
