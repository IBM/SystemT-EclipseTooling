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

import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.RunProfilerAction;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.ExtractionTasksExamplesLinksPaths;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;
import com.ibm.biginsights.textanalytics.workflow.util.Styles;

public class Step5 extends Composite
{



  /**
   * Create the composite.
   * 
   * @param parent
   * @param style
   */
  public Step5 (Composite parent, int style)
  {
    super (parent, style);

    setLayout (new GridLayout (2, false));

    final Button profilerButton = new Button (this, SWT.PUSH);
    profilerButton.setImage (Icons.PROFILER_ICON);
    GridData gd_img = new GridData (SWT.LEFT, SWT.TOP, false, false, 1, 1);
    gd_img.horizontalIndent = 15;
    profilerButton.setLayoutData (gd_img);
    profilerButton.setToolTipText (Messages.run_profiler_tootltip);

    // accessibility support
    profilerButton.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = Messages.step_5_text2;
      }
    });

    final Cursor cursor = parent.getDisplay ().getSystemCursor (SWT.CURSOR_HAND);
    profilerButton.setCursor (cursor);

    profilerButton.addListener (SWT.MouseDown, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        RunProfilerAction openProfiler = new RunProfilerAction ();
        openProfiler.run ();
      }
    });

    profilerButton.addKeyListener (new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e)
      {
        if (profilerButton.isFocusControl ()) {
          char key = e.character;
          if (key == ' ') {
            RunProfilerAction openProfiler = new RunProfilerAction ();
            openProfiler.run ();
          }
        }
      }
    });

    profilerButton.addListener (SWT.MouseEnter, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        profilerButton.setBackground (Styles.TAB_SELECTED_BG);
      }
    });

    profilerButton.addListener (SWT.MouseExit, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        profilerButton.setBackground (Styles.DEFAULT_BG);
      }
    });

    Link link1 = new Link (this, SWT.WRAP);
    GridData gd_link1 = new GridData (SWT.LEFT, SWT.TOP, true, false, 1, 1);
    gd_link1.horizontalIndent = 5;
    link1.setLayoutData (gd_link1);
    link1.setText (Messages.step_5_text1);
    link1.setToolTipText (Messages.open_help_tooltip);
    link1.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e) {
        if ( e.keyCode == SWT.CR)
          AqlProjectUtils.openHelpUrl(ExtractionTasksExamplesLinksPaths.STEP_5_profiler);
      }
    });
    link1.addListener (SWT.MouseDown, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        AqlProjectUtils.openHelpUrl (ExtractionTasksExamplesLinksPaths.STEP_5_profiler);
      }
    });

    Label clearLast = new Label (this, SWT.NONE);
    GridData gd_clear = new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd_clear.verticalIndent = 15;
    clearLast.setLayoutData (gd_clear);
  }

  @Override
  protected void checkSubclass ()
  {
    // Disable the check that prevents subclassing of SWT components
  }

}
