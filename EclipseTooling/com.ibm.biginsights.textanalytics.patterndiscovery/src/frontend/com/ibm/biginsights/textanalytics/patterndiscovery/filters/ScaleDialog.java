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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;

/**
 * defines a scale dialog that allows the user to drag instead of entering a value
 * 
 * 
 */
public class ScaleDialog extends Dialog
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
   * The input validator, or <code>null</code> if none.
   */
  private IInputValidator validator;

  /**
   * Ok button widget.
   */
  private Button okButton;

  /**
   * Error message label widget.
   */
  private Text errorMessageText;

  /**
   * Error message string.
   */
  private String errorMessage;

  private Integer value;

  private int minimum;

  private int maximum;

  private int increment;

  private Composite mainComposite;

  private Scale scale;

  private Label lblMinimumSequenceLength;

  private int pageIncrement;

  private Button decrease, increase;

  public ScaleDialog (Shell parentShell, String dialogTitle, String dialogMessage, int initialValue, int minimum,
    int maximum, IInputValidator validator)
  {
    super (parentShell);
    this.title = dialogTitle;
    message = dialogMessage;

    value = initialValue;
    this.validator = validator;

    this.minimum = minimum;
    this.maximum = maximum;
    this.increment = 1;
    this.pageIncrement = 1;
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected void buttonPressed (int buttonId)
  {
    if (buttonId == IDialogConstants.OK_ID) {
      value = scale.getSelection ();
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
    scale.setFocus ();
    if (value != null) {
      scale.setSelection (value);
    }
  }

  private void increase ()
  {
    if (scale.getSelection () < scale.getMaximum ()) scale.setSelection (scale.getSelection () + 1);
  }

  private void decrease ()
  {
    if (scale.getSelection () > scale.getMinimum ()) scale.setSelection (scale.getSelection () - 1);
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected Control createDialogArea (Composite parent)
  {
    // create composite
    mainComposite = (Composite) super.createDialogArea (parent);
    // create message
    if (message != null) {
      Label label = new Label (mainComposite, SWT.WRAP);
      label.setText (message);
      GridData data = new GridData (GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
        | GridData.VERTICAL_ALIGN_CENTER);
      data.widthHint = convertHorizontalDLUsToPixels (IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
      label.setLayoutData (data);
      label.setFont (parent.getFont ());
    }

    Composite sliderComposite = new Composite (mainComposite, SWT.NONE);
    sliderComposite.setLayout (new GridLayout (3, false));
    sliderComposite.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL
      | GridData.VERTICAL_ALIGN_CENTER));

    decrease = new Button (sliderComposite, SWT.PUSH);
    decrease.setText ("<");
    decrease.addListener (SWT.MouseDown, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        decrease ();
        validateInput ();
      }
    });

    scale = new Scale (sliderComposite, getInputSliderStyle ());
    scale.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    scale.setMinimum (minimum);
    scale.setMaximum (maximum);
    scale.setIncrement (increment);
    scale.setPageIncrement (pageIncrement);
    scale.setSelection (value);
    scale.addSelectionListener (new SelectionListener () {

      @Override
      public void widgetSelected (SelectionEvent e)
      {
        validateInput ();
      }

      @Override
      public void widgetDefaultSelected (SelectionEvent e)
      {
        validateInput ();
      }
    });

    increase = new Button (sliderComposite, SWT.PUSH);
    increase.setText (">");
    increase.addListener (SWT.MouseDown, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        increase ();
        validateInput ();
      }
    });

    lblMinimumSequenceLength = new Label (mainComposite, SWT.NONE);
    lblMinimumSequenceLength.setText (String.format ("%s: %d", Messages.SEQUENCE_MIN_SIZE_LABEL, value));

    errorMessageText = new Text (mainComposite, SWT.READ_ONLY | SWT.WRAP);
    errorMessageText.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    errorMessageText.setBackground (errorMessageText.getDisplay ().getSystemColor (SWT.COLOR_WIDGET_BACKGROUND));
    // Set the error message text
    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
    setErrorMessage (errorMessage);

    applyDialogFont (mainComposite);
    return mainComposite;
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
  protected Scale getScale ()
  {
    return scale;
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
  public Integer getValue ()
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
      errorMessage = validator.isValid (Integer.toString (scale.getSelection ()));
    }

    lblMinimumSequenceLength.setText (String.format ("%s: %d", Messages.SEQUENCE_MIN_SIZE_LABEL, scale.getSelection ()));
    mainComposite.layout ();
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
  protected int getInputSliderStyle ()
  {
    return SWT.NONE;
  }

}
