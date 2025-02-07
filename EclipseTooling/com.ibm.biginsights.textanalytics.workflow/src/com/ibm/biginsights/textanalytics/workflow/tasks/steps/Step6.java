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

import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.OpenExportExtractorAction;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.ExtractionTasksExamplesLinksPaths;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;
import com.ibm.biginsights.textanalytics.workflow.util.Styles;

public class Step6 extends Composite {



  Action openExportExtractorHelp;
  
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public Step6(Composite parent, int style) {
		super(parent, style);

		setLayout(new GridLayout(2, false));
		initActions ();
    
		final Button exportButton = new Button (this, SWT.PUSH);
		exportButton.setImage (Icons.EXPORT_AOG_ICON);
		
		GridData gd_img = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
    gd_img.horizontalIndent = 15;
    exportButton.setLayoutData (gd_img);
    exportButton.setToolTipText (Messages.open_export_tootltip);

    // accessibility support
    exportButton.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = Messages.step_6_text2;
      }
    });

	  final Cursor cursor = parent.getDisplay ().getSystemCursor(SWT.CURSOR_HAND);
	  exportButton.setCursor (cursor);
		
	  exportButton.addListener(SWT.MouseDown, new Listener() {
      @Override
      public void handleEvent(Event event) {
        OpenExportExtractorAction action = new OpenExportExtractorAction();
        action.run();
      }
    });

	  exportButton.addKeyListener (new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e)
      {
        if (exportButton.isFocusControl ()) {
          char key = e.character;
          if (key == ' ') {
            OpenExportExtractorAction action = new OpenExportExtractorAction();
            action.run();
          }
        }
      }
    });

	  exportButton.addListener (SWT.MouseEnter, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        exportButton.setBackground (Styles.TAB_SELECTED_BG);
      }
    });

	  exportButton.addListener (SWT.MouseExit, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        exportButton.setBackground (Styles.DEFAULT_BG);
      }
    });
		
		Link link1 = new Link(this, SWT.WRAP);
		GridData gd_link1 = new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 1);
		gd_link1.horizontalIndent = 5;
		link1.setLayoutData(gd_link1);
		link1.setText(Messages.step_6_text1);
		link1.setToolTipText (Messages.open_help_tooltip);
    link1.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased (KeyEvent e) {
        if ( e.keyCode == SWT.CR)
          openExportExtractorHelp.run ();
      }
    });
		link1.addListener (SWT.MouseDown, new Listener() {  
      @Override
      public void handleEvent (Event event)
      {
        openExportExtractorHelp.run ();
      }
    });
		
		Label clearLast = new Label(this, SWT.NONE);
		GridData gd_clear = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_clear.verticalIndent = 15;
		clearLast.setLayoutData(gd_clear);
	}
	
	private void initActions(){
	  openExportExtractorHelp = new Action() {
	    public void run() {
	      AqlProjectUtils.openHelpUrl (ExtractionTasksExamplesLinksPaths.STEP_6_export);
	    };  
	  };
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
