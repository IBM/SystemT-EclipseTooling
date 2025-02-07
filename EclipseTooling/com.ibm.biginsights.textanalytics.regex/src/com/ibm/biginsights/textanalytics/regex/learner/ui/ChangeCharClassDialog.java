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
package com.ibm.biginsights.textanalytics.regex.learner.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.textanalytics.regex.Messages;
import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterClass;
import com.ibm.biginsights.textanalytics.regex.learner.characterhierarchy.CharacterHierarchy;

/**
 * This class implements the graphical user interface of the Change Character Class dialog. TODO:
 * implement with checkboxes and OK button... TODO: implement this dialog such that it is assembled
 * dynamically depending on the character hierarchy configured
 * 
 * 
 * 
 */

public class ChangeCharClassDialog extends Dialog {



  Object result = null;

  Shell shell;

  // character class buttons
  Button any;

  Button alpha_numeric;

  Button non_alpha_numeric;

  Button letter;

  Button upperCase;

  Button lowerCase;

  Button other;

  Button whitespace;

  Button digit;

  // CONSTRUCTORS
  public ChangeCharClassDialog(Shell parent, int style) {
    super(parent, style);
  }

  public ChangeCharClassDialog(Shell parent) {
    this(parent, 0);
  }

  public Object open() {
    final Shell parent = getParent();
    this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    this.shell.setText(Messages.ChangeCharClassDialog_LABEL_CHANGE_CHAR_CLASS);
    //System.out.println();
    if(System.getProperty("os.name").startsWith("Windows"))
    {
    	this.shell.setSize(340, 200);
    }
    else
    {
    	this.shell.setSize(420, 230);
    }
    this.shell.setLayout(new GridLayout(9, true));

    final Label label = new Label(this.shell, SWT.NONE);
    label.setText(Messages.ChangeCharClassDialog_LABEL_CHOOSE_CHAR_CLASS);
    label.setFont(new Font(this.shell.getDisplay(), Messages.ChangeCharClassDialog_FONT_SANS_SERIF, 10, SWT.BOLD));
    GridData gridData = new GridData();
    gridData.horizontalSpan = 9;
    label.setLayoutData(gridData);

    CharacterClass charClass;

    this.any = new Button(this.shell, SWT.PUSH);
    charClass = CharacterHierarchy.getCharClassByName(CharacterHierarchy.ANY_CHARACTER);
    this.any.setToolTipText(charClass.getExplanation());
    this.any.setText(Messages.ChangeCharClassDialog_ANY_CHAR);
    gridData = new GridData();
    gridData.horizontalSpan = 9;
    gridData.horizontalAlignment = SWT.CENTER;
    this.any.setLayoutData(gridData);
    this.any.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        ChangeCharClassDialog.this.result = CharacterHierarchy.ANY_CHARACTER;
        ChangeCharClassDialog.this.shell.close();
      }
    });

    this.alpha_numeric = new Button(this.shell, SWT.PUSH);
    charClass = CharacterHierarchy.getCharClassByName(CharacterHierarchy.ALPHA_NUMERIC);
    this.alpha_numeric.setText(Messages.ChangeCharClassDialog_ALPHANUMERIC);
    this.alpha_numeric.setToolTipText(charClass.getExplanation());
    gridData = new GridData();
    gridData.horizontalSpan = 5;
    gridData.horizontalAlignment = SWT.CENTER;
    gridData.verticalIndent = 10;
    this.alpha_numeric.setLayoutData(gridData);
    this.alpha_numeric.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        ChangeCharClassDialog.this.result = CharacterHierarchy.ALPHA_NUMERIC;
        ChangeCharClassDialog.this.shell.close();
      }
    });

    this.non_alpha_numeric = new Button(this.shell, SWT.PUSH);
    this.non_alpha_numeric.setToolTipText(CharacterHierarchy.getCharClassByName(
        CharacterHierarchy.NON_ALPHA_NUMERIC).getExplanation());
    this.non_alpha_numeric.setText(Messages.ChangeCharClassDialog_NON_ALPHANUMERIC);
    gridData = new GridData();
    gridData.horizontalSpan = 3;
    gridData.horizontalAlignment = SWT.CENTER;
    gridData.verticalIndent = 10;
    this.non_alpha_numeric.setLayoutData(gridData);
    this.non_alpha_numeric.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        ChangeCharClassDialog.this.result = CharacterHierarchy.NON_ALPHA_NUMERIC;
        ChangeCharClassDialog.this.shell.close();
      }
    });

    this.letter = new Button(this.shell, SWT.PUSH);
    this.letter.setToolTipText(CharacterHierarchy.getCharClassByName(CharacterHierarchy.LETTER)
        .getExplanation());
    this.letter.setText(Messages.ChangeCharClassDialog_LETTER);
    gridData = new GridData();
    gridData.horizontalSpan = 9;
    gridData.horizontalAlignment = SWT.LEFT;
    gridData.horizontalIndent = 20;
    gridData.verticalIndent = 10;
    this.letter.setLayoutData(gridData);
    this.letter.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        ChangeCharClassDialog.this.result = CharacterHierarchy.LETTER;
        ChangeCharClassDialog.this.shell.close();
      }
    });

    this.upperCase = new Button(this.shell, SWT.PUSH);
    this.upperCase.setToolTipText(CharacterHierarchy.getCharClassByName(CharacterHierarchy.UPPER)
        .getExplanation());
    this.upperCase.setText(Messages.ChangeCharClassDialog_UPPERCASE);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    gridData.verticalIndent = 10;
    this.upperCase.setLayoutData(gridData);
    this.upperCase.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        ChangeCharClassDialog.this.result = CharacterHierarchy.UPPER;
        ChangeCharClassDialog.this.shell.close();
      }
    });

    this.lowerCase = new Button(this.shell, SWT.PUSH);
    this.lowerCase.setToolTipText(CharacterHierarchy.getCharClassByName(CharacterHierarchy.LOWER)
        .getExplanation());
    this.lowerCase.setText(Messages.ChangeCharClassDialog_LOWERCASE);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    gridData.verticalIndent = 10;
    this.lowerCase.setLayoutData(gridData);
    this.lowerCase.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        ChangeCharClassDialog.this.result = CharacterHierarchy.LOWER;
        ChangeCharClassDialog.this.shell.close();
      }
    });

    this.digit = new Button(this.shell, SWT.PUSH);
    this.digit.setToolTipText(CharacterHierarchy.getCharClassByName(CharacterHierarchy.DIGIT)
        .getExplanation());
    this.digit.setText(Messages.ChangeCharClassDialog_DIGIT);
    gridData = new GridData();
    gridData.horizontalSpan = 1;
    gridData.verticalIndent = 10;
    this.digit.setLayoutData(gridData);
    this.digit.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        ChangeCharClassDialog.this.result = CharacterHierarchy.DIGIT;
        ChangeCharClassDialog.this.shell.close();
      }
    });

    this.whitespace = new Button(this.shell, SWT.PUSH);
    this.whitespace.setToolTipText(CharacterHierarchy.getCharClassByName(
        CharacterHierarchy.WHITESPACE).getExplanation());
    this.whitespace.setText(Messages.ChangeCharClassDialog_WHITESPACE);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    gridData.verticalIndent = 10;
    this.whitespace.setLayoutData(gridData);
    this.whitespace.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        ChangeCharClassDialog.this.result = CharacterHierarchy.WHITESPACE;
        ChangeCharClassDialog.this.shell.close();
      }
    });

    this.other = new Button(this.shell, SWT.PUSH);
    charClass = CharacterHierarchy.getCharClassByName(CharacterHierarchy.OTHER);
    this.other.setToolTipText(charClass.getExplanation());
    this.other.setText(Messages.ChangeCharClassDialog_OTHER);
    gridData = new GridData();
    gridData.horizontalSpan = 2;
    gridData.verticalIndent = 10;
    this.other.setLayoutData(gridData);
    this.other.setToolTipText(charClass.getExplanation());
    this.other.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent event) {
        ChangeCharClassDialog.this.result = CharacterHierarchy.OTHER;
        ChangeCharClassDialog.this.shell.close();
      }
    });

    // draw lines
    this.shell.addPaintListener(new PaintListener() {
      @Override
      public void paintControl(PaintEvent e) {
        e.gc.drawLine(
            ChangeCharClassDialog.this.any.getLocation().x
                + ChangeCharClassDialog.this.any.getSize().x / 2,
            ChangeCharClassDialog.this.any.getLocation().y
                + ChangeCharClassDialog.this.any.getSize().y,
            ChangeCharClassDialog.this.alpha_numeric.getLocation().x
                + ChangeCharClassDialog.this.alpha_numeric.getSize().x / 2,
            ChangeCharClassDialog.this.alpha_numeric.getLocation().y);
        e.gc.drawLine(
            ChangeCharClassDialog.this.any.getLocation().x
                + ChangeCharClassDialog.this.any.getSize().x / 2,
            ChangeCharClassDialog.this.any.getLocation().y
                + ChangeCharClassDialog.this.any.getSize().y,
            ChangeCharClassDialog.this.non_alpha_numeric.getLocation().x
                + ChangeCharClassDialog.this.non_alpha_numeric.getSize().x / 2,
            ChangeCharClassDialog.this.non_alpha_numeric.getLocation().y);
        e.gc.drawLine(
            ChangeCharClassDialog.this.alpha_numeric.getLocation().x
                + ChangeCharClassDialog.this.alpha_numeric.getSize().x / 2,
            ChangeCharClassDialog.this.alpha_numeric.getLocation().y
                + ChangeCharClassDialog.this.alpha_numeric.getSize().y,
            ChangeCharClassDialog.this.letter.getLocation().x
                + ChangeCharClassDialog.this.letter.getSize().x / 2,
            ChangeCharClassDialog.this.letter.getLocation().y);
        e.gc.drawLine(
            ChangeCharClassDialog.this.alpha_numeric.getLocation().x
                + ChangeCharClassDialog.this.alpha_numeric.getSize().x / 2,
            ChangeCharClassDialog.this.alpha_numeric.getLocation().y
                + ChangeCharClassDialog.this.alpha_numeric.getSize().y,
            ChangeCharClassDialog.this.digit.getLocation().x
                + ChangeCharClassDialog.this.digit.getSize().x / 2,
            ChangeCharClassDialog.this.digit.getLocation().y);
        e.gc.drawLine(
            ChangeCharClassDialog.this.letter.getLocation().x
                + ChangeCharClassDialog.this.letter.getSize().x / 2,
            ChangeCharClassDialog.this.letter.getLocation().y
                + ChangeCharClassDialog.this.letter.getSize().y,
            ChangeCharClassDialog.this.upperCase.getLocation().x
                + ChangeCharClassDialog.this.upperCase.getSize().x / 2,
            ChangeCharClassDialog.this.upperCase.getLocation().y);
        e.gc.drawLine(
            ChangeCharClassDialog.this.letter.getLocation().x
                + ChangeCharClassDialog.this.letter.getSize().x / 2,
            ChangeCharClassDialog.this.letter.getLocation().y
                + ChangeCharClassDialog.this.letter.getSize().y,
            ChangeCharClassDialog.this.lowerCase.getLocation().x
                + ChangeCharClassDialog.this.lowerCase.getSize().x / 2,
            ChangeCharClassDialog.this.lowerCase.getLocation().y);
        e.gc.drawLine(ChangeCharClassDialog.this.non_alpha_numeric.getLocation().x
            + ChangeCharClassDialog.this.non_alpha_numeric.getSize().x / 2,
            ChangeCharClassDialog.this.non_alpha_numeric.getLocation().y
                + ChangeCharClassDialog.this.non_alpha_numeric.getSize().y,
            ChangeCharClassDialog.this.whitespace.getLocation().x
                + ChangeCharClassDialog.this.whitespace.getSize().x / 2,
            ChangeCharClassDialog.this.whitespace.getLocation().y);
        e.gc.drawLine(
            ChangeCharClassDialog.this.non_alpha_numeric.getLocation().x
                + ChangeCharClassDialog.this.non_alpha_numeric.getSize().x / 2,
            ChangeCharClassDialog.this.non_alpha_numeric.getLocation().y
                + ChangeCharClassDialog.this.non_alpha_numeric.getSize().y,
            ChangeCharClassDialog.this.other.getLocation().x
                + ChangeCharClassDialog.this.other.getSize().x / 2,
            ChangeCharClassDialog.this.other.getLocation().y);
      }
    });

    this.shell.open();
    final Display display = parent.getDisplay();
    while (!this.shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    // System.out.println("after while");
    return this.result;
  }

}
