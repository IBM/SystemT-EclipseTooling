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
package com.ibm.biginsights.textanalytics.patterndiscovery.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * defines the model for a semantic context element
 * 
 * 
 */
public class SemanticContext
{


 
	private String snippet;
  private String phone;

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport (this);

  public SemanticContext ()
  {}

  public SemanticContext (String snippet, String phone)
  {
    super ();
    this.snippet = snippet;
    this.phone = phone;
  }

  public void addPropertyChangeListener (String propertyName, PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener (propertyName, listener);
  }

  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    propertyChangeSupport.removePropertyChangeListener (listener);
  }

  public String getSnippet ()
  {
    return snippet;
  }

  public String getPhone ()
  {
    return phone;
  }

  public void setSnippet (String snippet)
  {
    propertyChangeSupport.firePropertyChange ("snippet", this.snippet, this.snippet = snippet);
  }

  public void setPhone (String phone)
  {
    propertyChangeSupport.firePropertyChange ("phone", this.phone, this.phone = phone);
  }

}
