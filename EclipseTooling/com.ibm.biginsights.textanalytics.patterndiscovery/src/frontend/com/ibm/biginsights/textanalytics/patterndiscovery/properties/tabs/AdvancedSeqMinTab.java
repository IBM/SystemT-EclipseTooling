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
import com.ibm.biginsights.textanalytics.patterndiscovery.newwidgets.NumberInput;

/**
 * advanced Sequence Mining tab for the run configuration wizard of pd
 * 
 * 
 */
@SuppressWarnings("restriction")
public class AdvancedSeqMinTab extends PropsTab
{



  protected NumberInput minSeqLen, maxSeqLen, minSeqFreq;

  public AdvancedSeqMinTab (CTabFolder parent, int style, String text, String tooltip, Image image,
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

    Label minSeqLenLabel = new Label (internal, SWT.NONE);
    minSeqLenLabel.setText (Messages.SEQUENCE_MIN_SIZE_LABEL);

    minSeqLen = new NumberInput (internal, SWT.NONE, Messages.SEQUENCE_MIN_SIZE_PROP, properties, 1, Double.MAX_VALUE);
    minSeqLen.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label maxSeqLenLabel = new Label (internal, SWT.NONE);
    maxSeqLenLabel.setText (Messages.SEQUENCE_MAX_SIZE_LABEL);

    maxSeqLen = new NumberInput (internal, SWT.NONE, Messages.SEQUENCE_MAX_SIZE_PROP, properties, 1, Double.MAX_VALUE);
    maxSeqLen.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label minSeqFreqLabel = new Label (internal, SWT.NONE);
    minSeqFreqLabel.setText (Messages.SEQUENCE_MIN_FREQUENCY_SIZE_LABEL);

    minSeqFreq = new NumberInput (internal, SWT.NONE, Messages.SEQUENCE_MIN_FREQUENCY_SIZE_PROP, properties, 1,
      Double.MAX_VALUE);
    minSeqFreq.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    minSeqLen.setFocus ();
  }

  @Override
  public void addPropertyChangeListener (PropertyChangeListener listener)
  {
    minSeqLen.addPropertyChangeListener (listener);
    maxSeqLen.addPropertyChangeListener (listener);
    minSeqFreq.addPropertyChangeListener (listener);
  }

  @Override
  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    minSeqLen.removePropertyChangeListener (listener);
    maxSeqLen.removePropertyChangeListener (listener);
    minSeqFreq.removePropertyChangeListener (listener);
  }

  @Override
  public void setValuesFromProperties (Properties props)
  {
    String minSeqLen_val = props.getProperty (Messages.SEQUENCE_MIN_SIZE_PROP, "");
    String maxSeqLen_val = props.getProperty (Messages.SEQUENCE_MAX_SIZE_PROP, "");
    String minSeqFreq_val = props.getProperty (Messages.SEQUENCE_MIN_FREQUENCY_SIZE_PROP, "");

    if (!minSeqLen_val.isEmpty ()) minSeqLen.setValue (minSeqLen_val);
    if (!maxSeqLen_val.isEmpty ()) maxSeqLen.setValue (maxSeqLen_val);
    if (!minSeqFreq_val.isEmpty ()) minSeqFreq.setValue (minSeqFreq_val);
  }

}
