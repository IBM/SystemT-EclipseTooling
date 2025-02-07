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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * defines a Radio Input SWT widget
 * 
 *
 */
public class TrueFalse extends Widget
{



  protected String text;
  protected String tooltip;
  protected Button widget;
  private Listener eventListener;

  public TrueFalse (Composite parent, int style, String propertyName, Properties properties, String value, String text,
    String tooltip)
  {
    super (parent, style, propertyName, properties);
    this.text = text;
    this.tooltip = tooltip;
    this.value = value;

    // --- init the listener ---
    eventListener = new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        widget.removeListener (SWT.Selection, this);
        String newValue = Boolean.toString (widget.getSelection ());
        valueChanged (newValue);
        widget.addListener (SWT.Selection, this);
      }
    };

    // --- build the widget ---
    buildWidget ();
    setValue (value);
  }

  @Override
  public void buildWidget ()
  {
    widget = new Button (this, SWT.CHECK);
    widget.setText (text);
    widget.setToolTipText (tooltip);
    widget.addListener (SWT.Selection, eventListener);
  }

  @Override
  public void setEnabled (boolean enabled)
  {
    widget.setEnabled (enabled);
  }

  @Override
  public void setValue (String newValue)
  {
    try {
      valueChanged (newValue);
      widget.setSelection (Boolean.parseBoolean (newValue));
    }
    catch (Exception e) {
      e.printStackTrace ();
      // TODO: handle exception
    }
  }

}
