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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Properties;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;

/**
 * defines an Input SWT widget
 * 
 * 
 */
public abstract class Widget extends Composite
{


  
	public static final String PROPERTY_FIRED = "VALUE";
  protected PropertyChangeSupport propertyChangeSupport;
  protected String propertyName;
  protected ArrayList<IInputValidator> validators;
  protected String value;
  Properties properties;

  public Widget (Composite parent, int style, String propertyName, Properties properties)
  {
    super (parent, style);
    validators = new ArrayList<IInputValidator> ();

    setLayout (new GridLayout (1, true));
    setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    propertyChangeSupport = new PropertyChangeSupport (this);

    this.propertyName = propertyName;
    this.properties = properties;

  }

  /**
   * @param newValue
   * @return
   */
  public String validate (String newValue)
  {
    if (validators != null) for (IInputValidator validator : validators) {
      String errMessage = validator.isValid (newValue);
      if (errMessage != null) return errMessage;
    }
    return null;
  }

  /**
   * adds a listener to this widget
   * 
   * @param listener
   */
  public void addPropertyChangeListener (PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener (listener);
  }

  /**
   * remove a listener from the widget
   * 
   * @param listener
   */
  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    propertyChangeSupport.removePropertyChangeListener (listener);
  }

  /**
   * @param newValue
   * @return
   */
  protected String valueChanged (String newValue)
  {
    String errMessage = validate (newValue);
    if (errMessage == null) {
      properties.setProperty (propertyName, newValue);
      propertyChangeSupport.firePropertyChange (PROPERTY_FIRED, value, newValue);
      value = newValue;
    }
    else {
      ErrorMessages.ShowErrorMessage (errMessage);
      return null;
    }

    return value;
  }

  /**
   * adds a validator for this widget
   * 
   * @param validator
   */
  public void addValidator (IInputValidator validator)
  {
    validators.add (validator);
  }

  /**
   * removes the first instance of this validator
   * 
   * @param validator
   */
  public void removeValidator (IInputValidator validator)
  {
    validators.remove (validator);
  }

  /**
   * 
   */
  public abstract void buildWidget ();

  /**
   * @param state
   */
  public abstract void setEnabled (boolean enabled);

  public abstract void setValue (String newValue);
  
  public String getValue(){
    return value;
  }
}
