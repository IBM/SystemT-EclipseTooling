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
package com.ibm.biginsights.textanalytics.concordance.ui.filter;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.textanalytics.resultviewer.Messages;

/**
 * This class provides the dialog for specifying text pattern filter
 * for various filters in the Filter Panel for Annotation explorer.
 */
public class TextPatternInputDialog extends InputDialog {


  
  private boolean isRegexChecked = false;
  
  private final RegexPatternInputValidator inputValidator;

  public TextPatternInputDialog(Shell parentShell, String dialogTitle, String dialogMessage,
      String initialValue, IInputValidator validator, boolean isRegex) {
    super(parentShell, dialogTitle, dialogMessage, initialValue, validator);
    this.isRegexChecked = isRegex;
    this.inputValidator = (RegexPatternInputValidator) validator;
  }
  
  /**
   * {@inheritDoc}
   * A checkbox is added to the existing widgets in the dialog.
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite = (Composite) super.createDialogArea(parent);
    
    GridData gridData = new GridData();
    gridData.horizontalIndent = 10;
    Button regexCheckbox = new Button(composite, SWT.CHECK);
    regexCheckbox.setText (Messages.TextPatternInputDialog_FullRegexCheckbox);
    regexCheckbox.setLayoutData (gridData);
    regexCheckbox.setSelection(this.isRegexChecked);
    regexCheckbox.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        TextPatternInputDialog.this.toggleRegex();
        TextPatternInputDialog.this.inputValidator.toggleRegex();
        validateInput();
      }
    });
    
    return composite;
  }
  
  private void toggleRegex() {
    if (this.isRegexChecked) {
      this.isRegexChecked = false;
    } else {
      this.isRegexChecked = true;
    }
  }
  
  public boolean isRegex() {
    return this.isRegexChecked;
  }
  
}
