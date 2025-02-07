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
 * model for a common context. common context are the ones that group elements into the same bubble
 * 
 * 
 */
public class CommonContext
{


  
	private String contextString;
  private int contextCount;
  private String signature;

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport (this);

  public CommonContext ()
  {}

  public CommonContext (String contextString, int contextCount, String signature)
  {
    super ();
    this.contextString = contextString;
    this.contextCount = contextCount;
    this.signature = signature;
  }

  public void addPropertyChangeListener (String propertyName, PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener (propertyName, listener);
  }

  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    propertyChangeSupport.removePropertyChangeListener (listener);
  }

  public String getContextString ()
  {
    return contextString;
  }

  public int getContextCount ()
  {
    return contextCount;
  }

  public String getSignature ()
  {
    return signature;
  }

  public void setContextString (String contextString)
  {
    propertyChangeSupport.firePropertyChange ("contextString", this.contextString, this.contextString = contextString);
  }

  public void setContextCount (int contextCount)
  {
    propertyChangeSupport.firePropertyChange ("contextCount", this.contextCount, this.contextCount = contextCount);
  }

  public void setSignature (String signature)
  {
    propertyChangeSupport.firePropertyChange ("signature", this.signature, this.signature = signature);
  }

  @Override
  public String toString ()
  {
    return contextString + ":" + contextCount + ":" + signature;
  }

}
