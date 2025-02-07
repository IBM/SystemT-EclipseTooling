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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * defines a Combo Input SWT widget
 * 
 * 
 */
public class ComboInput extends Widget
{


 
	Combo combo;
  Listener comboListener;
  Map<String, Integer> optionsMap;
  boolean readOnly;

  public ComboInput (Composite parent, int style, String propertyName, Properties properties, ArrayList<String> opts,
    String value, boolean readOnly)
  {
    super (parent, style, propertyName, properties);
    this.readOnly = readOnly;
    optionsMap = new HashMap<String, Integer> ();
    buildWidget ();
    setOptions (opts);
    comboListener = new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        valueChanged (combo.getText ());
      }
    };
    combo.addListener (SWT.Selection, comboListener);
    setSelection (value);
  }

  @Override
  public void buildWidget ()
  {
    if (!readOnly)
      combo = new Combo (this, SWT.BORDER | SWT.DROP_DOWN | SWT.FILL);
    else
      combo = new Combo (this, SWT.BORDER | SWT.DROP_DOWN | SWT.FILL | SWT.READ_ONLY);
    combo.setLayoutData (new GridData (GridData.FILL_BOTH));
  }

  @Override
  public void setEnabled (boolean enabled)
  {
    combo.setEnabled (enabled);
  }

  /**
   * @param opts
   */
  public void setOptions (ArrayList<String> opts)
  {
    optionsMap.clear ();
    combo.removeAll ();
    for (int i = 0; i < opts.size (); i++) {
      optionsMap.put (opts.get (i), i);
      combo.add (opts.get (i), i);
    }
  }

  public void setOptions (ArrayList<String> opts, boolean bNotify)
  {
    if (!bNotify)
      combo.removeListener (SWT.Selection, comboListener);

    setOptions (opts);

    if (!bNotify)
      combo.addListener (SWT.Selection, comboListener);
  }

  /**
   * @param sel
   */
  protected void setSelection (String sel)
  {
    Integer i = optionsMap.get (sel);
    if (i != null)
      combo.select (i);
    else
      combo.deselectAll ();
  }

  @Override
  public void setValue (String newValue)
  {
    setSelection (newValue);
  }

  public void setValue (String newValue, boolean bNotify)
  {
    if (!bNotify)
      combo.removeListener (SWT.Selection, comboListener);

    setValue (newValue);

    if (!bNotify)
      combo.addListener (SWT.Selection, comboListener);
  }

  public Combo getCombo ()
  {
    return combo;
  }

  @Override
  public String getValue()
  {
    return combo.getText ();
  }
}
