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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog that allows to enter a comment into a text area
 * 
 * 
 */
public class CommentDialog extends Dialog
{


 
	/**
   * The title of the dialog.
   */
  private String title;

  /**
   * The message to display, or <code>null</code> if none.
   */
  private String message;

  /**
   * The input value; the empty string by default.
   */
  private String value = "";//$NON-NLS-1$

  /**
   * The input validator, or <code>null</code> if none.
   */
  private IInputValidator validator;

  /**
   * Ok button widget.
   */
  private Button okButton;

  /**
   * Input text widget.
   */
  private Text text;

  /**
   * Error message label widget.
   */
  private Text errorMessageText;

  /**
   * Error message string.
   */
  private String errorMessage;
  private ScrolledComposite scrolledComposite;
  private Composite composite_1;

  /**
   * Creates an input dialog with OK and Cancel buttons. Note that the dialog will have no visual representation (no
   * widgets) until it is told to open.
   * <p>
   * Note that the <code>open</code> method blocks for input dialogs.
   * </p>
   * 
   * @param parentShell the parent shell, or <code>null</code> to create a top-level shell
   * @param dialogTitle the dialog title, or <code>null</code> if none
   * @param dialogMessage the dialog message, or <code>null</code> if none
   * @param initialValue the initial input value, or <code>null</code> if none (equivalent to the empty string)
   * @param validator an input validator, or <code>null</code> if none
   */
  public CommentDialog (Shell parentShell, String dialogTitle, String dialogMessage, String initialValue,
    IInputValidator validator)
  {
    super (parentShell);
    this.title = dialogTitle;
    message = dialogMessage;
    if (initialValue == null) {
      value = "";//$NON-NLS-1$
    }
    else {
      value = initialValue;
    }
    this.validator = validator;
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected void buttonPressed (int buttonId)
  {
    if (buttonId == IDialogConstants.OK_ID) {
      value = text.getText ();
    }
    else {
      value = null;
    }
    super.buttonPressed (buttonId);
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  protected void configureShell (Shell shell)
  {
    super.configureShell (shell);
    if (title != null) {
      shell.setText (title);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  protected void createButtonsForButtonBar (Composite parent)
  {
    // create OK and Cancel buttons by default
    okButton = createButton (parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton (parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    // do this here because setting the text will set enablement on the ok
    // button
    text.setFocus ();
    if (value != null) {
      text.setText (value);
      text.selectAll ();
    }
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected Control createDialogArea (Composite parent)
  {
    // create composite
    Composite composite = (Composite) super.createDialogArea (parent);
    // create message
    if (message != null) {
      Label label = new Label (composite, SWT.WRAP);
      label.setText (message);
      GridData data = new GridData (GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
        | GridData.VERTICAL_ALIGN_CENTER);
      data.widthHint = convertHorizontalDLUsToPixels (IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
      label.setLayoutData (data);
      label.setFont (parent.getFont ());
    }

    scrolledComposite = new ScrolledComposite (composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    GridData gd_scrolledComposite = new GridData (SWT.FILL, SWT.TOP, false, false, 1, 1);
    gd_scrolledComposite.widthHint = 450;
    gd_scrolledComposite.heightHint = 100;
    scrolledComposite.setLayoutData (gd_scrolledComposite);
    scrolledComposite.setExpandHorizontal (true);
    scrolledComposite.setExpandVertical (true);

    composite_1 = new Composite (scrolledComposite, SWT.NONE);
    composite_1.setLayout (new FillLayout (SWT.HORIZONTAL));

    text = new Text (composite_1, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
    text.addModifyListener (new ModifyListener () {
      public void modifyText (ModifyEvent e)
      {
        validateInput ();
      }
    });

    scrolledComposite.setContent (composite_1);
    scrolledComposite.setMinSize (composite_1.computeSize (SWT.DEFAULT, SWT.DEFAULT));

    errorMessageText = new Text (composite, SWT.READ_ONLY | SWT.WRAP);
    errorMessageText.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    errorMessageText.setBackground (errorMessageText.getDisplay ().getSystemColor (SWT.COLOR_WIDGET_BACKGROUND));
    // Set the error message text
    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
    setErrorMessage (errorMessage);

    applyDialogFont (composite);

    return composite;
  }

  /**
   * Returns the error message label.
   * 
   * @return the error message label
   * @deprecated use setErrorMessage(String) instead
   */
  protected Label getErrorMessageLabel ()
  {
    return null;
  }

  /**
   * Returns the ok button.
   * 
   * @return the ok button
   */
  protected Button getOkButton ()
  {
    return okButton;
  }

  /**
   * Returns the text area.
   * 
   * @return the text area
   */
  protected Text getText ()
  {
    return text;
  }

  /**
   * Returns the validator.
   * 
   * @return the validator
   */
  protected IInputValidator getValidator ()
  {
    return validator;
  }

  /**
   * Returns the string typed into this input dialog.
   * 
   * @return the input string
   */
  public String getValue ()
  {
    return value;
  }

  /**
   * Validates the input.
   * <p>
   * The default implementation of this framework method delegates the request to the supplied input validator object;
   * if it finds the input invalid, the error message is displayed in the dialog's message line. This hook method is
   * called whenever the text changes in the input field.
   * </p>
   */
  protected void validateInput ()
  {
    String errorMessage = null;
    if (validator != null) {
      errorMessage = validator.isValid (text.getText ());
    }
    // Bug 16256: important not to treat "" (blank error) the same as null
    // (no error)
    setErrorMessage (errorMessage);
  }

  /**
   * Sets or clears the error message. If not <code>null</code>, the OK button is disabled.
   * 
   * @param errorMessage the error message, or <code>null</code> to clear
   * @since 3.0
   */
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
      // Access the ok button by id, in case clients have overridden button creation.
      // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
      Control button = getButton (IDialogConstants.OK_ID);
      if (button != null) {
        button.setEnabled (errorMessage == null);
      }
    }
  }

  /**
   * Returns the style bits that should be used for the input text field. Defaults to a single line entry. Subclasses
   * may override.
   * 
   * @return the integer style bits that should be used when creating the input text
   * @since 3.4
   */
  protected int getInputTextStyle ()
  {
    return SWT.SINGLE | SWT.BORDER;
  }
}
