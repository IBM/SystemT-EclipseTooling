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

public class Step3 extends Composite {



	Action openRegexGeneratorHelp, openPatternDiscoveryHelp;
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public Step3(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		Composite content = new Composite(this, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		Label mainLabel = new Label(content, SWT.WRAP);
		mainLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		mainLabel.setText(Messages.step_3a_title);
		mainLabel.setFont(Styles.LABEL_FONT);

		Label tip0 = new Label(content, SWT.WRAP);
		GridData gd_tip0 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_tip0.horizontalIndent = 15;
		gd_tip0.verticalIndent = 10;
		tip0.setLayoutData(gd_tip0);
		tip0.setText (Messages.step_3_inline_text);
		
		Link link1 = new Link(content, SWT.WRAP);
		GridData gd_link1 = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 2);
		gd_link1.horizontalIndent = 15;
		link1.setLayoutData(gd_link1);
		link1.setText(Messages.step_3a_example);
		link1.setToolTipText (Messages.open_help_tooltip);
		link1.addKeyListener (new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e) {
        if ( e.keyCode == SWT.CR)
          AqlProjectUtils.openHelpUrl(ExtractionTasksExamplesLinksPaths.STEP_3_example);
      }
		});
		link1.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
        AqlProjectUtils.openHelpUrl(ExtractionTasksExamplesLinksPaths.STEP_3_example);
			}
		});
    link1.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = Messages.step_3a_title + Messages.step_3_inline_text + Messages.see_example;
      }
    });

		Label tip1 = new Label(content, SWT.WRAP);
		GridData gd_tip1 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_tip1.horizontalIndent = 15;
		gd_tip1.verticalIndent = 10;
		tip1.setLayoutData(gd_tip1);
		tip1.setText(Messages.step_3a_tip1);
		
		Link tip2 = new Link(content, SWT.WRAP);
		GridData gd_tip2 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_tip2.horizontalIndent = 15;
		gd_tip2.verticalIndent = 3;
		tip2.setLayoutData(gd_tip2);
		tip2.setText(Messages.step_3a_tip2);
		tip2.setToolTipText (Messages.open_help_tooltip);
		tip2.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e) {
        if ( e.keyCode == SWT.CR)
          openRegexGeneratorHelp.run();
			}
		});
    tip2.addListener(SWT.MouseDown, new Listener() {
      @Override
      public void handleEvent(Event event) {
        openRegexGeneratorHelp.run();
      }
    });
    tip2.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = Messages.step_3a_tip1 + Messages.step_3a_tip2_noLink;
      }
    });
		
		Link tip3 = new Link(content, SWT.WRAP);
		GridData gd_tip3 = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_tip3.horizontalIndent = 15;
		gd_tip3.verticalIndent = 3;
		tip3.setLayoutData(gd_tip3);
		tip3.setText(Messages.step_3a_tip3);
		tip3.setToolTipText (Messages.open_help_tooltip);
    tip3.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e) {
        if ( e.keyCode == SWT.CR)
          openPatternDiscoveryHelp.run();
      }
    });
		tip3.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				openPatternDiscoveryHelp.run();
			}
		});
    tip3.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = Messages.step_3a_tip3_noLink;
      }
    });
		
		Label clearLast = new Label(this, SWT.NONE);
		GridData gd_clear = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_clear.verticalIndent = 15;
		clearLast.setLayoutData(gd_clear);
		
		createActions();
	}
	
	private void createActions(){
	  
		openRegexGeneratorHelp = new Action(){
			public void run(){
				AqlProjectUtils.openHelp(ExtractionTasksExamplesLinksPaths.STEP_3_regexgenerator);
			}
		};
		
		openPatternDiscoveryHelp = new Action(){
			public void run(){
				AqlProjectUtils.openHelp(ExtractionTasksExamplesLinksPaths.STEP_3_patterndiscovery);
			}
		};
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
