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
package com.ibm.biginsights.textanalytics.workflow.tasks.steps;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.RunAbstract;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.RunInFilesLabeledAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.RunInFilesSelectedAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.RunInputCollectionAction;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.ExtractionTasksExamplesLinksPaths;
import com.ibm.biginsights.textanalytics.workflow.util.Styles;

public class Step4 extends Composite
{


 
	/**
   * Create the composite.
   * 
   * @param parent
   * @param style
   */
  public Step4 (Composite parent, int style)
  {
    super (parent, style);
    setLayout (new GridLayout (1, false));
    generateStepA ();
    // separator
    Label label = new Label (this, SWT.SEPARATOR | SWT.HORIZONTAL);
    label.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    //
    generateStepB ();

    Label clearLast = new Label (this, SWT.NONE);
    GridData gd_clear = new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd_clear.verticalIndent = 15;
    clearLast.setLayoutData (gd_clear);
  }

  private void generateStepA ()
  {
    Composite stepA = new Composite (this, SWT.NONE);
    stepA.setLayout (new GridLayout (1, false));
    stepA.setBounds (0, 0, 64, 64);
    stepA.setLayoutData (new GridData (SWT.LEFT, SWT.FILL, true, false, 1, 1));

    Text labelA = new Text (stepA, SWT.WRAP);
    labelA.setEditable (false);
    labelA.setBounds (0, 0, 49, 13);
    labelA.setText (Messages.step_4a_title);
    labelA.setFont (Styles.LABEL_FONT);
    labelA.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = Messages.step_4a_title + Messages.step_4a_tip1;
      }
    });

    Label tip1 = new Label (stepA, SWT.WRAP);
    GridData gd_tip1 = new GridData (SWT.CENTER, SWT.CENTER, true, false, 1, 1);
    gd_tip1.horizontalIndent = 15;
    tip1.setLayoutData (gd_tip1);
    tip1.setText (Messages.step_4a_tip1);
  
    createAction (stepA, new RunInputCollectionAction (AqlProjectUtils.getActionPlanView ()));
    createAction (stepA, new RunInFilesSelectedAction (AqlProjectUtils.getActionPlanView ()));
    createAction (stepA, new RunInFilesLabeledAction (AqlProjectUtils.getActionPlanView ()));

    Text tip2 = new Text (stepA, SWT.WRAP);
    tip2.setEditable (false);
    GridData gd_tip2 = new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd_tip2.horizontalIndent = 15;
    gd_tip2.verticalIndent = 5;
    tip2.setLayoutData (gd_tip2);
    tip2.setText (Messages.step_4a_tip2);
  }

  private void generateStepB ()
  {
    Composite stepB = new Composite (this, SWT.NONE);
    stepB.setLayout (new GridLayout (1, false));
    stepB.setBounds (0, 0, 64, 64);
    stepB.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Text labelB = new Text (stepB, SWT.WRAP);
    labelB.setEditable (false);
    labelB.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    labelB.setFont (Styles.LABEL_FONT);
    labelB.setText (Messages.step_4b_title);
    labelB.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = Messages.step_4b_title + Messages.step_4b_tip1 + Messages.step_4b_link1_noLink +
                   Messages.step_4b_tip2 + Messages.step_4b_tip3 + Messages.see_example;
      }
    });

    Label tip1 = new Label (stepB, SWT.WRAP);
    GridData gd_tip1 = new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd_tip1.horizontalIndent = 15;
    tip1.setLayoutData (gd_tip1);
    tip1.setText (Messages.step_4b_tip1);

    Link link1 = new Link (stepB, SWT.WRAP);
    GridData gd_link1 = new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd_link1.verticalIndent = 5;
    gd_link1.horizontalIndent = 15;
    link1.setLayoutData (gd_link1);
    link1.setText (Messages.step_4b_link1);
    link1.setToolTipText (Messages.open_help_tooltip);
    link1.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e) {
        if ( e.keyCode == SWT.CR)
          AqlProjectUtils.openHelpUrl(ExtractionTasksExamplesLinksPaths.STEP_4B_provenanceviewer);
      }
    });
    link1.addListener (SWT.MouseDown, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        AqlProjectUtils.openHelpUrl (ExtractionTasksExamplesLinksPaths.STEP_4B_provenanceviewer);
      }
    });

    Link tip2 = new Link (stepB, SWT.WRAP);
    GridData gd_tip2 = new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd_tip2.verticalIndent = 5;
    gd_tip2.horizontalIndent = 15;
    tip2.setLayoutData (gd_tip2);
    tip2.setText (Messages.step_4b_tip2);

    Link tip3 = new Link (stepB, SWT.WRAP);
    GridData gd_tip3 = new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd_tip3.verticalIndent = 5;
    gd_tip3.horizontalIndent = 15;
    tip3.setLayoutData (gd_tip3);
    tip3.setText (Messages.step_4b_tip3);

    Link example = new Link (stepB, SWT.WRAP);
    GridData example_gd = new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1);
    example_gd.verticalIndent = 15;
    example_gd.horizontalIndent = 15;
    example.setLayoutData (example_gd);
    example.setText (Messages.step_1_example_txt);
    example.setToolTipText (Messages.open_help_tooltip);
    example.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e) {
        if ( e.keyCode == SWT.CR)
          AqlProjectUtils.openHelpUrl(ExtractionTasksExamplesLinksPaths.STEP_4B_example);
      }
    });
    example.addListener (SWT.MouseDown, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        AqlProjectUtils.openHelpUrl (ExtractionTasksExamplesLinksPaths.STEP_4B_example);
      }
    });
  }

  private void createAction (Composite parent, final Action action)
  {
    Composite composite = new Composite (parent, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    composite.setBounds (0, 0, 64, 64);
    composite.setLayout (new GridLayout (2, false));

    final Button icon_button = new Button (composite, SWT.PUSH);
    icon_button.setImage (action.getImageDescriptor ().createImage ());
    icon_button.setToolTipText (action.getText ());   // set tooltip of button the same as the displayed text so screen reader can read it.
    GridData gd_img = new GridData (SWT.LEFT, SWT.TOP, false, false, 1, 1);
    gd_img.horizontalIndent = 15;
    icon_button.setLayoutData (gd_img);

    // accessibility support
    icon_button.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = action.getText ();
      }
    });

    final Cursor cursor = getParent ().getDisplay ().getSystemCursor (SWT.CURSOR_HAND);
    icon_button.setCursor (cursor);

    icon_button.addListener (SWT.MouseDown, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        if (action instanceof RunAbstract) {
          ((RunAbstract) action).setActionPLan (AqlProjectUtils.getActionPlanView ());
        }
        action.run ();
      }
    });

    icon_button.addKeyListener (new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e)
      {
        if (icon_button.isFocusControl ()) {

          char key = e.character;

          if (key == ' ') {
            if (action instanceof RunAbstract) {
              ((RunAbstract) action).setActionPLan (AqlProjectUtils.getActionPlanView ());
            }
            action.run ();
          }

          super.keyReleased (e);
        }
      }
    });

    icon_button.addListener (SWT.MouseEnter, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        icon_button.setBackground (Styles.TAB_SELECTED_BG);
      }
    });

    icon_button.addListener (SWT.MouseExit, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        icon_button.setBackground (null);
      }
    });
    
    Link label = new Link (composite, SWT.WRAP);
    GridData gd_lbl = new GridData (SWT.LEFT, SWT.TOP, true, false, 1, 1);
    gd_lbl.horizontalIndent = 5;
    label.setLayoutData (gd_lbl);
    label.setText (action.getText ());
    label.setToolTipText (action.getToolTipText ());

    label.addListener (SWT.MouseDown, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        action.run ();
      }
    });

  }

  @Override
  protected void checkSubclass ()
  {
    // Disable the check that prevents subclassing of SWT components
  }

}
