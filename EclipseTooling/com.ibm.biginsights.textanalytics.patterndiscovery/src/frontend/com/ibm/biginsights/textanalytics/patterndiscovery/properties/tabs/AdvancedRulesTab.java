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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.patterndiscovery.newwidgets.RangeInput;

/**
 * advanced Rules tab for the run configuration wizard of pd
 * 
 * 
 */
@SuppressWarnings("restriction")
@Deprecated
public class AdvancedRulesTab extends PropsTab
{



  protected RangeInput seq_coorelation;

  public AdvancedRulesTab (CTabFolder parent, int style, String text, String tooltip, Image image, Properties properties, LaunchConfigurationsDialog lcDialog)
  {
    super (parent, style, text, tooltip, image, properties, lcDialog);
  }

  @Override
  protected void buildUI (Composite composite)
  {
    Composite internal = new Composite (composite, SWT.NONE);
    internal.setLayout (new GridLayout (2, false));
    internal.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label seq_coorelationLabel = new Label (internal, SWT.NONE);
    seq_coorelationLabel.setText (Messages.CORRELATION_MEASURE_RANGE_LABEL);

//    seq_coorelation = new RangeInput (internal, SWT.NONE, Messages.CORRELATION_MEASURE_RANGE_PROP, properties,
//      Messages.CORRELATION_MEASURE_RANGE_TOOLTIP, "", 0, 1);
//    seq_coorelation.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));
//    seq_coorelation.setDialog (lcDialog);
  }

  @Override
  public void addPropertyChangeListener (PropertyChangeListener listener)
  {
    seq_coorelation.addPropertyChangeListener (listener);
  }

  @Override
  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    seq_coorelation.removePropertyChangeListener (listener);
  }

  @Override
  public void setValuesFromProperties (Properties props)
  {
//    String seq_coorelation_val = props.getProperty (Messages.CORRELATION_MEASURE_RANGE_PROP, "");
//    if (!seq_coorelation_val.isEmpty ()) seq_coorelation.setValue (seq_coorelation_val);
  }

}
