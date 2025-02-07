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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

/**
 * defines a List Input SWT widget
 * 
 * 
 */
public class ListInput extends Widget
{


 
	List list;
  Listener listListener;
  Map<String, Integer> optionsmap;

  public static final String propsSeparator = "\\,";

  public ListInput (Composite parent, int style, String propertyName, Properties properties, String value,
    ArrayList<String> options)
  {
    super (parent, style, propertyName, properties);
    buildWidget ();
    this.value = value;
    optionsmap = new HashMap<String, Integer> ();
    setOptions (options);
    listListener = new Listener () {
      @Override
      public void handleEvent (Event event)
      {
        valueChanged (arrayToString (list.getSelection ()));
      }
    };
    list.addListener (SWT.Selection, listListener);
    setSelection (stringToArray (value, propsSeparator));
  }

  @Override
  public void buildWidget ()
  {
    list = new List (this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
    GridData datLayout = new GridData (GridData.FILL_BOTH);
    datLayout.heightHint = 50;
    list.setLayoutData (datLayout);
  }

  @Override
  public void setEnabled (boolean enabled)
  {
    list.setEnabled (enabled);
  }

  /**
   * @param options
   */
  public void setOptions (ArrayList<String> options)
  {
    optionsmap.clear ();
    list.removeAll ();

    for (int i = 0; i < options.size (); i++) {
      optionsmap.put (options.get (i), i);
      list.add (options.get (i), i);
    }
  }

  public void setOptions (ArrayList<String> opts, boolean bNotify)
  {
    if (!bNotify)
      list.removeListener (SWT.Selection, listListener);

    setOptions (opts);

    if (!bNotify)
      list.addListener (SWT.Selection, listListener);
  }

  /**
   * @param sels
   */
  protected void setSelection (String[] sels)
  {
    int[] indices = new int[sels.length];
    for (int i = 0; i < sels.length; i++) {
      Integer ind = optionsmap.get (sels[i]);
      if (ind != null) indices[i] = ind;
    }
    list.select (indices);
  }

  /**
   * @param str
   * @param separator
   * @return
   */
  public static String[] stringToArray (String str, String separator)
  {
    return str.split (separator);
  }

  /**
   * @param list
   * @return
   */
  public static String arrayToString (String[] list)
  {
    String ret = "";
    for (String str : list) {
      ret += str + ",";
    }
    if (ret.length () > 0) return ret.substring (0, ret.length () - 1);// remove extra comma
    return ret;
  }

  public static boolean isContained (String value, String option)
  {
    String[] vals = stringToArray (value, propsSeparator);
    for (String str : vals) {
      if (str.equals (option)) return true;
    }
    return false;
  }

  @Override
  public void setValue (String newValue)
  {
    String[] vals = stringToArray (newValue, propsSeparator);
    if (vals.length > 0) setSelection (vals);
  }

  public void setValue (String newValue, boolean bNotify)
  {
    if (!bNotify)
      list.removeListener (SWT.Selection, listListener);

    setValue (newValue);

    if (!bNotify)
      list.addListener (SWT.Selection, listListener);
  }

  public List getList ()
  {
    return list;
  }

}
