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
package com.ibm.biginsights.textanalytics.patterndiscovery.newwidgets;

import java.util.Properties;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;

/**
 * defines an Input SWT widget where a min and max text inputs are provided specific validators are provided to ensure a
 * valid range is enetered by the user
 * 
 * 
 */
public class RangeInput extends Widget
{


 
	TitleAreaDialog titleAreaDialog = null;

  private static final String minLabelTxt = "min", maxLabelTxt = "max";
  protected Text min, max;
  private Listener minListener, maxListener;
  protected RangeBasicValidator basicValidator;
  protected RangeMinMaxValidator minLessMaxValidator;
  protected double minPossible, maxPossible;

  /**
   * default constructor
   * 
   * @param parent
   * @param style
   * @param propertyName
   * @param properties
   * @param tooltip
   * @param value
   * @param minPossible
   * @param maxPossible
   */
  public RangeInput (Composite parent, int style, String propertyName, Properties properties, String tooltip,
    String value, double minPossible, double maxPossible)
  {
    super (parent, style, propertyName, properties);
    setToolTipText (tooltip);
    this.value = value;
    this.minPossible = minPossible;
    this.maxPossible = maxPossible;

    basicValidator = new RangeBasicValidator ();
    minLessMaxValidator = new RangeMinMaxValidator (minPossible, maxPossible);

    // --- init listeners ---
    minListener = new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        min.removeListener (SWT.Modify, this);

        String str = validateAndSet (min.getText ());
        if (str != null)
          min.setSelection (min.getText ().length ());

        min.addListener (SWT.Modify, this);
      }
    };

    maxListener = new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        max.removeListener (SWT.Modify, this);

        String str = validateAndSet (max.getText ());
        if (str != null)
          max.setSelection (max.getText ().length ());

        max.addListener (SWT.Modify, this);
      }
    };
    // --- end ---

    buildWidget ();
    setValue (value);

    // --- add listeners ---
    min.addListener (SWT.Modify, minListener);
    max.addListener (SWT.Modify, maxListener);
  }

  @Override
  public void buildWidget ()
  {
    setLayout (new GridLayout (2, true));
    setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    // First half for the min input area = min Label + min Text
    Composite minHalf = new Composite (this, SWT.NONE);
    minHalf.setLayout (new GridLayout (2, false));
    minHalf.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label minlabel = new Label (minHalf, SWT.NONE);
    minlabel.setText (minLabelTxt);

    min = new Text (minHalf, SWT.BORDER);
    min.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    // Second half for the max input area = max Label + max Text
    Composite maxHalf = new Composite (this, SWT.NONE);
    maxHalf.setLayout (new GridLayout (2, false));
    maxHalf.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label maxLabel = new Label (maxHalf, SWT.NONE);
    maxLabel.setText (maxLabelTxt);

    max = new Text (maxHalf, SWT.BORDER);
    max.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));
  }

  @Override
  public void setEnabled (boolean enabled)
  {
    min.setEnabled (enabled);
    max.setEditable (enabled);
  }

  // ---
  // --- Helper methods ---
  // ---

  /**
   * @param valueChanged
   */
  public String validateAndSet (String valueChanged)
  {
    String errMessage = basicValidator.isValid (valueChanged);
    if (errMessage != null) {
      showErrorMessage(errMessage);
      return null;
    }

    errMessage = minLessMaxValidator.isValid (min.getText (), max.getText ());
    if (errMessage != null) {
      showErrorMessage(errMessage);
      return null;
    }

    clearErrorMessage();
    return valueChanged (buildMinMax ());
  }

  private void showErrorMessage (String errMessage)
  {
    // If this widget is part of a TitleAreaDialog, post the error message at the dialog's message area,
    if (titleAreaDialog != null)
      titleAreaDialog.setErrorMessage (errMessage);

    // else pop up an error dialog.
    else
      ErrorMessages.ShowErrorMessage (errMessage);
  }

  private void clearErrorMessage ()
  {
    if (titleAreaDialog != null)
      titleAreaDialog.setErrorMessage (null);
  }

  /**
   * @return
   */
  protected String buildMinMax ()
  {
    String ret = "";

    if (!min.getText ().isEmpty ()) {
      ret += String.format (">=%s", min.getText ());
    }
    if (!max.getText ().isEmpty ()) {
      ret += String.format ("<=%s", max.getText ());
    }

    return ret;
  }

  /**
   * @param value
   * @param c
   * @return
   */
  public static String extractDoubleFrom (String value, char c)
  {
    int pos = value.indexOf (c);

    String ret = "";

    if (pos >= 0) {
      // skip = in cases of >= or <=
      if (value.charAt (pos + 1) == '=') {
        pos++;
      }
      for (int i = pos + 1; i < value.length (); i++) {
        // if (value.charAt(i) == '.'
        // || Character.isDigit(value.charAt(i)))
        if (value.charAt (i) == '>' || value.charAt (i) == '<') return ret;

        ret += value.charAt (i);
        // else
        // return ret;
      }
    }

    return ret;
  }

  /**
   * @param value
   * @return
   */
  public static String[] getMaxMin (String value)
  {
    String[] ret = new String[2];

    ret[0] = extractDoubleFrom (value, '>');
    ret[1] = extractDoubleFrom (value, '<');

    return ret;
  }

  // ---
  // --- Validators -----
  // ---

  /**
   * 
   */
  class RangeBasicValidator implements IInputValidator
  {
    @Override
    public String isValid (String newText)
    {
      try {
        if (newText != null && !newText.isEmpty ()) {

          double val = Double.parseDouble (newText);
          if (val >= minPossible && val <= maxPossible)
            return null;
        }
      }
      catch (Exception e) {
        // Do nothing, will return error anyway.
      }

      return ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_VALUE;
    }
  }

  /**
   * 
   */
  class RangeMinMaxValidator implements IInputValidator
  {
    double minP, maxP;

    public RangeMinMaxValidator (double min, double max)
    {
      this.minP = min;
      this.maxP = max;
    }

    /**
     * @param minTxt
     * @param maxTxt
     * @return
     */
    public String isValid (String minTxt, String maxTxt)
    {
      if (minTxt == null || maxTxt == null || minTxt.isEmpty () || maxTxt.isEmpty ()) return null;
      try {
        double min = Double.parseDouble (minTxt);
        double max = Double.parseDouble (maxTxt);
        if (min > max) return ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_RANGE;
        if (min < minP || max > maxP) return ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_VALUE;
      }
      catch (Exception e) {
        return ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_VALUE;
      }

      return null;
    }

    @Override
    public String isValid (String newText)
    {
      return null;
    }
  }

  @Override
  public void setValue (String newValue)
  {
    try {
      String[] minmax = getMaxMin (newValue);
      min.setText (minmax[0]);
      max.setText (minmax[1]);
      valueChanged (newValue);
    }
    catch (Exception e) {
      e.printStackTrace ();
      // TODO: handle exception
    }
  }

  public void setDialog (TitleAreaDialog dialog)
  {
    this.titleAreaDialog = dialog;
  }

}
