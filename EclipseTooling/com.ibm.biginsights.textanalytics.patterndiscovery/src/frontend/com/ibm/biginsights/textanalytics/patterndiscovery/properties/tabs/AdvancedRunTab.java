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
import java.util.Properties;

import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.patterndiscovery.newwidgets.TrueFalse;

/**
 * advanced Run tab for the run configuration wizard of pd
 * 
 * 
 */
@SuppressWarnings("restriction")
public class AdvancedRunTab extends PropsTab
{



  protected TrueFalse reuseStats;

  public AdvancedRunTab (CTabFolder parent, int style, String text, String tooltip, Image image, Properties properties, LaunchConfigurationsDialog lcDialog)
  {
    super (parent, style, text, tooltip, image, properties, lcDialog);
  }

  @Override
  protected void buildUI (Composite composite)
  {
    reuseStats = new TrueFalse (composite, SWT.NONE, Messages.USE_EXISTING_DB_DATA_PROP, properties, "false",
      Messages.USE_EXISTING_DB_DATA_LABEL, Messages.USE_EXISTING_DB_DATA_TOOLTIP);
  }

  @Override
  public void addPropertyChangeListener (PropertyChangeListener listener)
  {
    reuseStats.addPropertyChangeListener (listener);
  }

  @Override
  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    reuseStats.removePropertyChangeListener (listener);
  }

  @Override
  public void setValuesFromProperties (Properties props)
  {
    String reuseStats_val = props.getProperty (Messages.USE_EXISTING_DB_DATA_PROP, "");
    if (!reuseStats_val.isEmpty ()) reuseStats.setValue (reuseStats_val);
  }

}
