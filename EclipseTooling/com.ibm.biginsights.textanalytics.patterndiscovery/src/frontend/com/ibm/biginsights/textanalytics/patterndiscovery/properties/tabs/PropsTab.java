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
package com.ibm.biginsights.textanalytics.patterndiscovery.properties.tabs;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;

import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * define a property tab. this class is to be extended by all the other tabs in the run configuration of pd
 * 
 * 
 */
@SuppressWarnings("restriction")
public abstract class PropsTab extends CTabItem
{


  
	Properties properties;
  PropertyChangeSupport propertyChangeSupport;

  protected LaunchConfigurationsDialog lcDialog = null;

  public PropsTab (CTabFolder parent, int style, String text, String tooltip, Image image, Properties properties, LaunchConfigurationsDialog lcDialog)
  {
    super (parent, style);

    this.properties = properties;
    this.propertyChangeSupport = new PropertyChangeSupport (this);
    this.lcDialog = lcDialog;

    setText (text);
    setToolTipText (tooltip);
    setImage (image);

    ScrolledComposite scroll = new ScrolledComposite (parent, SWT.H_SCROLL | SWT.V_SCROLL);
    scroll.setExpandHorizontal (true);
    scroll.setExpandVertical (true);

    Composite view = new Composite (scroll, SWT.NONE);
    view.setLayout (new GridLayout (1, true));
    view.setLayoutData (new GridData (GridData.FILL_BOTH));

    buildUI (view);

    scroll.setContent (view);
    scroll.setMinSize (view.computeSize (SWT.DEFAULT, SWT.DEFAULT));
    setControl (scroll);
  }

  /**
   * @return
   */
  public Properties getProperties ()
  {
    return properties;
  }

  protected abstract void buildUI (Composite composite);

  /**
   * adds a listener to this widget
   * 
   * @param listener
   */
  public abstract void addPropertyChangeListener (PropertyChangeListener listener);

  /**
   * remove a listener from the widget
   * 
   * @param listener
   */
  public abstract void removePropertyChangeListener (PropertyChangeListener listener);

  /**
   * apply the properties to the widgets in this tab
   * 
   * @param props
   */
  public abstract void setValuesFromProperties (Properties props);
}
