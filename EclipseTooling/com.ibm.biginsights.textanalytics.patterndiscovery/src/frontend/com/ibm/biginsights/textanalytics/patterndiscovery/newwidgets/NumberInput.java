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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;

/**
 * defines a Text Input SWT widget where only numbers can be entered
 * 
 * 
 */
public class NumberInput extends Widget
{



  Text input;

  public NumberInput (Composite parent, int style, String propertyName, Properties properties, double min, double max)
  {
    super (parent, style, propertyName, properties);
    buildWidget ();
    addValidator (new RangeValidator (min, max));
    input.addListener (SWT.Modify, new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        input.removeListener (SWT.Modify, this);
        setValue (input.getText ());
        input.setSelection (value.length ());
        input.addListener (SWT.Modify, this);
      }
    });
  }

  @Override
  public void buildWidget ()
  {
    input = new Text (this, SWT.SINGLE | SWT.BORDER);
    input.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));
  }

  @Override
  public void setEnabled (boolean enabled)
  {
    input.setEnabled (enabled);
  }

  class RangeValidator implements IInputValidator
  {
    double min, max;

    public RangeValidator (double min, double max)
    {
      this.min = min;
      this.max = max;
    }

    @Override
    public String isValid (String newText)
    {
      try {
        double val = Double.parseDouble (newText);
        if (val < min || val > max) return ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER;
      }
      catch (Exception e) {
        return ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER;
      }
      return null;
    }

  }

  @Override
  public void setValue (String newValue)
  {
    String val = valueChanged (newValue);
    if (val != null) input.setText (val);
  }

}
