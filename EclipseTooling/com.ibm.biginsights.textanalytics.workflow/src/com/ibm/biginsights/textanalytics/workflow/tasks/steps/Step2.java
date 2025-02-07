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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.ExtractionTasksExamplesLinksPaths;
import com.ibm.biginsights.textanalytics.workflow.util.Styles;

public class Step2 extends Composite {


	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public Step2(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		generateStepA();

		Label label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));

		generateStepB();
		
		Label clearLast = new Label(this, SWT.NONE);
		GridData gd_clear = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_clear.verticalIndent = 15;
		clearLast.setLayoutData(gd_clear);
	}

	private void generateStepA() {
		Composite stepA = new Composite(this, SWT.NONE);
		stepA.setLayout(new GridLayout(1, false));
		stepA.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label stepALabel = new Label(stepA, SWT.WRAP);
		stepALabel.setBounds(0, 0, 49, 13);
		stepALabel.setText(Messages.step_2a_title);
		stepALabel.setFont(Styles.LABEL_FONT);

		Composite stepAContent = new Composite(stepA, SWT.NONE);
		GridData gd_stepAContent = new GridData(SWT.LEFT, SWT.CENTER, true,
				false, 1, 1);
		gd_stepAContent.horizontalIndent = 15;
		stepAContent.setLayoutData(gd_stepAContent);
		stepAContent.setLayout(new GridLayout(1, false));

		Label tip1 = new Label(stepAContent, SWT.WRAP);
		tip1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		tip1.setText(Messages.step_2a_inline_text_1);

		Link link = new Link(stepAContent, SWT.NONE);
		link.setText(Messages.step_2a_example1_link);
		link.setToolTipText (Messages.open_help_tooltip);
    link.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e) {
        if ( e.keyCode == SWT.CR)
          AqlProjectUtils.openHelpUrl(ExtractionTasksExamplesLinksPaths.STEP_2A_example);
      }
    });
		link.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
			  AqlProjectUtils.openHelpUrl(ExtractionTasksExamplesLinksPaths.STEP_2A_example);
			}
		});
		link.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = Messages.step_2a_title + Messages.step_2a_inline_text_1 + Messages.see_example + Messages.step_2a_tip2;
      }
    });

		Label tip2 = new Label(stepAContent, SWT.WRAP);
		tip2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		tip2.setText(Messages.step_2a_tip2);
	}

	private void generateStepB() {
		Composite stepB = new Composite(this, SWT.NONE);
		stepB.setLayout(new GridLayout(1, false));
		stepB.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label stepBLabel = new Label(stepB, SWT.WRAP);
		stepBLabel.setBounds(0, 0, 49, 13);
		stepBLabel.setText(Messages.step_2b_title);
		stepBLabel.setFont(Styles.LABEL_FONT);

		Composite stepBContent = new Composite(stepB, SWT.NONE);
		GridData gd_stepBContent = new GridData(SWT.LEFT, SWT.CENTER, true,
				false, 1, 1);
		gd_stepBContent.horizontalIndent = 15;
		stepBContent.setLayoutData(gd_stepBContent);
		stepBContent.setLayout(new GridLayout(1, false));
		
		Label tip1 = new Label(stepBContent, SWT.WRAP);
		tip1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		tip1.setText(Messages.step_2b_inline_text_1);

		Link link1 = new Link(stepBContent, SWT.NONE);
		link1.setText(Messages.step_2b_insideClue_example);
		link1.setToolTipText (Messages.open_help_tooltip);
    link1.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e) {
        if ( e.keyCode == SWT.CR)
          AqlProjectUtils.openHelpUrl(ExtractionTasksExamplesLinksPaths.STEP_2B_example);
      }
    });
		link1.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
			  AqlProjectUtils.openHelpUrl(ExtractionTasksExamplesLinksPaths.STEP_2B_example);
			}
		});
    link1.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = Messages.step_2b_title + Messages.step_2b_inline_text_1 + Messages.see_example;
      }
    });
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
