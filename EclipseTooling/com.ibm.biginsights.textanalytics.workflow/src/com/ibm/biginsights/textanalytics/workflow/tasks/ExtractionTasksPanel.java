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
package com.ibm.biginsights.textanalytics.workflow.tasks;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Listener;

import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.tasks.steps.Step1;
import com.ibm.biginsights.textanalytics.workflow.tasks.steps.Step2;
import com.ibm.biginsights.textanalytics.workflow.tasks.steps.Step3;
import com.ibm.biginsights.textanalytics.workflow.tasks.steps.Step4;
import com.ibm.biginsights.textanalytics.workflow.tasks.steps.Step5;
import com.ibm.biginsights.textanalytics.workflow.tasks.steps.Step6;
import com.ibm.icu.text.MessageFormat;

public class ExtractionTasksPanel extends Composite
{



  Step1 p1;
  Step2 p2;
  Step3 p3;
  Step4 p4;
  Step5 p5;
  Step6 p6;

  private ExpandItem xpndtmS1;
  private ExpandItem xpndtmS2;
  private ExpandItem xpndtmS3;
  private ExpandItem xpndtmS4;
  private ExpandItem xpndtmS5;
  private ExpandItem xpndtmS6;

  /**
   * Create the composite.
   * 
   * @param parent
   * @param style
   */
  public ExtractionTasksPanel (Composite parent, int style)
  {
    super (parent, style);
    setLayout (new GridLayout (1, false));

    final ScrolledComposite scroller = new ScrolledComposite (this, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
    GridData ld = new GridData (SWT.FILL, SWT.FILL, true, true, 1, 1);
    ld.widthHint = 150;
    scroller.setLayoutData (ld);
    scroller.setExpandHorizontal (true);
    scroller.setExpandVertical (true);

    final ExpandBar expandBar = new ExpandBar (scroller, SWT.NONE);
    GridData expbarld = new GridData (SWT.LEFT, SWT.TOP, true, true, 1, 1);
    expandBar.setLayoutData (expbarld);

    // ----------------
    xpndtmS1 = new ExpandItem (expandBar, SWT.NONE);
    xpndtmS1.setText (Messages.step_1_title);

    p1 = new Step1 (expandBar, SWT.NONE);
    xpndtmS1.setControl (p1);

    xpndtmS2 = new ExpandItem (expandBar, SWT.NONE);
    xpndtmS2.setText (Messages.step_2_title);

    p2 = new Step2 (expandBar, SWT.NONE);
    xpndtmS2.setControl (p2);

    xpndtmS3 = new ExpandItem (expandBar, SWT.NONE);
    xpndtmS3.setText (Messages.step_3_title);

    p3 = new Step3 (expandBar, SWT.NONE);
    xpndtmS3.setControl (p3);

    xpndtmS4 = new ExpandItem (expandBar, SWT.NONE);
    xpndtmS4.setText (Messages.step_4_title);

    p4 = new Step4 (expandBar, SWT.NONE);
    xpndtmS4.setControl (p4);

    xpndtmS5 = new ExpandItem (expandBar, SWT.NONE);
    xpndtmS5.setText (Messages.step_5_title);

    p5 = new Step5 (expandBar, SWT.NONE);
    xpndtmS5.setControl (p5);

    xpndtmS6 = new ExpandItem (expandBar, SWT.NONE);
    xpndtmS6.setText (Messages.step_6_title);

    p6 = new Step6 (expandBar, SWT.NONE);
    xpndtmS6.setControl (p6);

    scroller.setContent (expandBar);

    // ----------------

    p1.addListener (SWT.Resize, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        xpndtmS1.setHeight (p1.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
      }
    });

    p2.addListener (SWT.Resize, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        xpndtmS2.setHeight (p2.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
      }
    });

    p3.addListener (SWT.Resize, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        xpndtmS3.setHeight (p3.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
      }
    });

    p4.addListener (SWT.Resize, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        xpndtmS4.setHeight (p4.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
      }
    });

    p5.addListener (SWT.Resize, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        xpndtmS5.setHeight (p5.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
      }
    });

    p6.addListener (SWT.Resize, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        xpndtmS6.setHeight (p6.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
      }
    });

    xpndtmS1.setHeight (p1.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
    xpndtmS2.setHeight (p2.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
    xpndtmS3.setHeight (p3.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
    xpndtmS4.setHeight (p4.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
    xpndtmS5.setHeight (p5.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);
    xpndtmS6.setHeight (p6.computeSize (expandBar.getClientArea ().width, SWT.DEFAULT).y);

    setExpandedAll (true);
    scroller.setMinSize (expandBar.computeSize (SWT.DEFAULT, SWT.DEFAULT));

    setExpandedAll (false);
    // transictionState4();
  }

  public void setExpanded (int n, boolean state)
  {
    switch (n) {
      case 1:
        xpndtmS1.setExpanded (state);
      break;

      case 2:
        xpndtmS2.setExpanded (state);
      break;

      case 3:
        xpndtmS3.setExpanded (state);
      break;

      case 4:
        xpndtmS4.setExpanded (state);
      break;

      case 5:
        xpndtmS5.setExpanded (state);
      break;

      case 6:
        xpndtmS6.setExpanded (state);
      break;
    }
  }

  public void setExpandedAll (boolean state)
  {
    xpndtmS1.setExpanded (state);
    xpndtmS2.setExpanded (state);
    xpndtmS3.setExpanded (state);
    xpndtmS4.setExpanded (state);
  }

  public void transictionState1 ()
  {
    xpndtmS1.setExpanded (false);
    xpndtmS2.setExpanded (true);
  }

  public void transictionState2 ()
  {
    xpndtmS2.setExpanded (false);
    xpndtmS3.setExpanded (true);
  }

  public void transictionState3 ()
  {
    xpndtmS3.setExpanded (false);
    xpndtmS4.setExpanded (true);
  }

  public void transictionState4 ()
  {
    xpndtmS4.setExpanded (false);
    xpndtmS1.setExpanded (true);
  }

  /**
   * @param path
   * @param langCode
   */
  public void refresh (String path, String langCode)
  {
    if (path != null) {
      try {
        p1.clear ();
        p1.loadNewFile (path);
      }
      catch (Exception e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (e.getMessage ());
        ActionPlanView.collection.setPath ("");
        ActionPlanView.serializePlan ();
      }
    }

    if (langCode != null && !langCode.isEmpty ()) {
      try {
        LangCode.validateLangStr (langCode.trim ());
        p1.setLangCode (langCode.trim ());
      }
      catch (Exception e) {
        String msg = MessageFormat.format (Messages.extraction_plan_collection_language_not_supported, new Object[] { langCode });
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (msg);
        ActionPlanView.collection.setLangCode ("");
        ActionPlanView.serializePlan ();
      }
    }

    setExpanded (1, true);
  }

  public void reset ()
  {
    p1.reset ();
    setExpandedAll (false);
    setExpanded (1, true);
  }

  public List<String> getSelectedFiles ()
  {
    return p1.getSelectedFiles ();
  }

  public void showCollectionDialog ()
  {
    p1.showCollectionDialog ();
    setExpanded (1, true);
  }

  @Override
  protected void checkSubclass ()
  {
    // Disable the check that prevents subclassing of SWT components
  }
}
