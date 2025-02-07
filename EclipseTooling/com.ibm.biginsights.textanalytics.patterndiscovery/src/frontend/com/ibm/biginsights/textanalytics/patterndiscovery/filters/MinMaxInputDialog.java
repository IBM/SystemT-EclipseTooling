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
package com.ibm.biginsights.textanalytics.patterndiscovery.filters;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.textanalytics.patterndiscovery.filters.FilterBySizeAction.MinMaxInputValidator;

/**
 * creates input dialog windows that request min and max bubble sizes from the user
 * 
 * 
 */
public class MinMaxInputDialog extends Dialog
{



  private String minMessage, maxMessage, errorMessage, title, minValue, maxValue;
  private Text minInput, maxInput, errorMessageText;
  private MinMaxInputValidator validator;
  private Button okButton;

  // private IInputValidator validator;
  // private Button okButton;

  public MinMaxInputDialog (Shell parentShell, String dialogTitle, String minMessage, String maxMessage,
    String minInitialValue, String maxInitialValue, MinMaxInputValidator validator)
  {
    super (parentShell);

    this.validator = validator;

    this.minMessage = minMessage;
    this.maxMessage = maxMessage;

    this.title = dialogTitle;

    minValue = (minInitialValue == null) ? "" : minInitialValue;
    maxValue = (maxInitialValue == null) ? "" : maxInitialValue;
  }

  @Override
  protected void configureShell (Shell shell)
  {
    super.configureShell (shell);
    if (title != null) {
      shell.setText (title);
    }
  }

  @Override
  protected void createButtonsForButtonBar (Composite parent)
  {
    // create OK and Cancel buttons by default
    okButton = createButton (parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton (parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    // do this here because setting the text will set enablement on the ok
    // button
    minInput.setFocus ();
    if (minValue != null) {
      minInput.setText (minValue);
      minInput.selectAll ();
    }
    maxInput.setFocus ();
    if (maxValue != null) {
      maxInput.setText (maxValue);
      maxInput.selectAll ();
    }
  }

  @Override
  protected void buttonPressed (int buttonId)
  {
    if (buttonId == IDialogConstants.OK_ID) {
      minValue = minInput.getText ();
      maxValue = maxInput.getText ();
    }
    else {
      minValue = null;
      maxValue = null;
    }
    super.buttonPressed (buttonId);
  }

  public String getMin ()
  {
    return minValue;
  }

  public String getMax ()
  {
    return maxValue;
  }

  protected Button getOkButton ()
  {
    return okButton;
  }

  protected int getInputTextStyle ()
  {
    return SWT.SINGLE | SWT.BORDER;
  }

  @Override
  protected Control createDialogArea (Composite parent)
  {
    // create composite
    Composite composite = (Composite) super.createDialogArea (parent);

    // create message
    if (minMessage != null) {
      Label minlabel = new Label (composite, SWT.WRAP);
      minlabel.setText (minMessage);
      GridData data = new GridData (GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
        | GridData.VERTICAL_ALIGN_CENTER);
      data.widthHint = convertHorizontalDLUsToPixels (IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
      minlabel.setLayoutData (data);
      minlabel.setFont (parent.getFont ());
    }

    Composite minSection = new Composite (composite, SWT.NONE);
    minSection.setLayout (new GridLayout (2, false));
    GridData minLayoutdata = new GridData (GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
      | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
    minLayoutdata.widthHint = convertHorizontalDLUsToPixels (IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
    minSection.setLayoutData (minLayoutdata);

    if (maxMessage != null) {
      Label maxlabel = new Label (composite, SWT.WRAP);
      maxlabel.setText (maxMessage);
      GridData data = new GridData (GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
        | GridData.VERTICAL_ALIGN_CENTER);
      data.widthHint = convertHorizontalDLUsToPixels (IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
      maxlabel.setLayoutData (data);
      maxlabel.setFont (parent.getFont ());
    }

    Composite maxSection = new Composite (composite, SWT.NONE);
    maxSection.setLayout (new GridLayout (2, false));
    GridData maxLayoutdata = new GridData (GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL
      | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
    maxLayoutdata.widthHint = convertHorizontalDLUsToPixels (IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
    maxSection.setLayoutData (minLayoutdata);

    minInput = new Text (minSection, getInputTextStyle ());
    minInput.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    minInput.addModifyListener (new ModifyListener () {
      @Override
      public void modifyText (ModifyEvent e)
      {
        validateInput ();
      }
    });

    Label minHelp = new Label (minSection, SWT.NONE);
    minHelp.setText (String.format ("( larger or equal to %d)", validator.getMin ()));

    maxInput = new Text (maxSection, getInputTextStyle ());
    maxInput.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    maxInput.addModifyListener (new ModifyListener () {
      @Override
      public void modifyText (ModifyEvent e)
      {
        validateInput ();
      }
    });

    Label maxHelp = new Label (maxSection, SWT.NONE);
    maxHelp.setText (String.format ("( smaller or equal to %s)", validator.getMax ()));

    errorMessageText = new Text (composite, SWT.READ_ONLY | SWT.WRAP);
    errorMessageText.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    errorMessageText.setBackground (errorMessageText.getDisplay ().getSystemColor (SWT.COLOR_WIDGET_BACKGROUND));
    // Set the error message text
    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
    setErrorMessage (errorMessage);

    applyDialogFont (composite);
    return composite;
  }

  protected void validateInput ()
  {
    String errorMessage = null;
    if (validator != null) {
      errorMessage = validator.isValid (minInput.getText (), maxInput.getText ());
    }

    // Bug 16256: important not to treat "" (blank error) the same as null
    // (no error)
    setErrorMessage (errorMessage);
  }

  public void setErrorMessage (String errorMessage)
  {
    this.errorMessage = errorMessage;
    if (errorMessageText != null && !errorMessageText.isDisposed ()) {
      errorMessageText.setText (errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
      // Disable the error message text control if there is no error, or
      // no error text (empty or whitespace only). Hide it also to avoid
      // color change.
      // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
      boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces (errorMessage)).length () > 0;
      errorMessageText.setEnabled (hasError);
      errorMessageText.setVisible (hasError);
      errorMessageText.getParent ().update ();
      // Access the ok button by id, in case clients have overridden
      // button creation.
      // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
      Control button = getButton (IDialogConstants.OK_ID);
      if (button != null) {
        button.setEnabled (errorMessage == null);
      }
    }
  }

}
