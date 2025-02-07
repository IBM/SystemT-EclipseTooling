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
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.patterndiscovery.newwidgets.NumberInput;

/**
 * advanced Sequence Mining tab for the run configuration wizard of pd
 * 
 * 
 */
@SuppressWarnings("restriction")
public class AdvancedRulesTab2 extends PropsTab
{



  protected NumberInput corrMin, corrMax;

  public AdvancedRulesTab2 (CTabFolder parent, int style, String text, String tooltip, Image image,
    Properties properties, LaunchConfigurationsDialog lcDialog)
  {
    super (parent, style, text, tooltip, image, properties, lcDialog);
  }

  @Override
  protected void buildUI (Composite composite)
  {
    Composite internal = new Composite (composite, SWT.NONE);
    internal.setLayout (new GridLayout (2, false));
    internal.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Text seq_coorelationLabel = new Text (internal, SWT.NONE);
    seq_coorelationLabel.setText (Messages.CORRELATION_MEASURE_RANGE_LABEL);
    seq_coorelationLabel.setEditable (false);

    Composite range = new Composite (internal, SWT.NONE);
    range.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));
    range.setLayout (new GridLayout (2, true));


    // First half for the min input area = min Label + min Text
    Composite minHalf = new Composite (range, SWT.NONE);
    minHalf.setLayout (new GridLayout (2, false));
    minHalf.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label minlabel = new Label (minHalf, SWT.NONE);
    minlabel.setText (Messages.CORRELATION_MEASURE_MIN_LABEL);

    corrMin = new NumberInput (minHalf, SWT.NONE, Messages.CORRELATION_MEASURE_MIN_PROP, properties, 0, 1);
    corrMin.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    // Second half for the max input area = max Label + max Text
    Composite maxHalf = new Composite (range, SWT.NONE);
    maxHalf.setLayout (new GridLayout (2, false));
    maxHalf.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label maxLabel = new Label (maxHalf, SWT.NONE);
    maxLabel.setText (Messages.CORRELATION_MEASURE_MAX_LABEL);

    corrMax = new NumberInput (maxHalf, SWT.NONE, Messages.CORRELATION_MEASURE_MAX_PROP, properties, 0, 1);
    corrMax.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    internal.setFocus ();
  }

  @Override
  public void addPropertyChangeListener (PropertyChangeListener listener)
  {
    corrMin.addPropertyChangeListener (listener);
    corrMax.addPropertyChangeListener (listener);
  }

  @Override
  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    corrMin.removePropertyChangeListener (listener);
    corrMax.removePropertyChangeListener (listener);
  }

  @Override
  public void setValuesFromProperties (Properties props)
  {
    String minSeqLen_val = props.getProperty (Messages.CORRELATION_MEASURE_MIN_PROP, "");
    String maxSeqLen_val = props.getProperty (Messages.CORRELATION_MEASURE_MAX_PROP, "");

    if (!minSeqLen_val.isEmpty ()) corrMin.setValue (minSeqLen_val);
    if (!maxSeqLen_val.isEmpty ()) corrMax.setValue (maxSeqLen_val);
  }

}
