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

import java.beans.PropertyChangeEvent;
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
 * advanced Input tab for the run configuration wizard of pd
 * 
 * 
 */
@SuppressWarnings("restriction")
public class AdvancedInputTab extends PropsTab
{



  protected TrueFalse norm_wht_sp, norm_nl, case_ins_an;
  protected PropertyChangeListener ws_listener;

  public AdvancedInputTab (CTabFolder parent, int style, String text, String tooltip, Image image, Properties properties, LaunchConfigurationsDialog lcDialog)
  {
    super (parent, style, text, tooltip, image, properties, lcDialog);
  }

  @Override
  protected void buildUI (Composite composite)
  {
    norm_wht_sp = new TrueFalse (composite, SWT.NONE, Messages.IGNORE_EXTRA_WHITESPACES_PROP, properties, "true",
      Messages.IGNORE_EXTRA_WHITESPACES_LABEL, Messages.IGNORE_EXTRA_WHITESPACES_TOOLTIP);

    norm_nl = new TrueFalse (composite, SWT.NONE, Messages.IGNORE_EXTRA_NEWLINES_PROP, properties, "true",
      Messages.IGNORE_EXTRA_NEWLINES_LABEL, Messages.IGNORE_EXTRA_NEWLINES_TOOLTIP);

    case_ins_an = new TrueFalse (composite, SWT.NONE, Messages.INPUT_TO_LOWERCASE_PROP, properties, "false",
      Messages.INPUT_TO_LOWERCASE_LABEL, Messages.INPUT_TO_LOWERCASE_TOOLTIP);
    
    initSpecialListener ();
    norm_wht_sp.addPropertyChangeListener (ws_listener);
    norm_wht_sp.setFocus ();
  }

  @Override
  public void addPropertyChangeListener (PropertyChangeListener listener)
  {
    norm_wht_sp.addPropertyChangeListener (listener);
    norm_nl.addPropertyChangeListener (listener);
    case_ins_an.addPropertyChangeListener (listener);
  }

  @Override
  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    norm_wht_sp.removePropertyChangeListener (listener);
    norm_nl.removePropertyChangeListener (listener);
    case_ins_an.removePropertyChangeListener (listener);
  }

  @Override
  public void setValuesFromProperties (Properties props)
  {
    String norm_wht_sp_val = props.getProperty (Messages.IGNORE_EXTRA_WHITESPACES_PROP, "");
    String norm_nl_val = props.getProperty (Messages.IGNORE_EXTRA_NEWLINES_PROP, "");
    String case_ins_an_val = props.getProperty (Messages.INPUT_TO_LOWERCASE_PROP, "");

    if (!norm_wht_sp_val.isEmpty ()) {
      norm_wht_sp.setValue (norm_wht_sp_val);
    }
    if (!norm_nl_val.isEmpty ()) {
      norm_wht_sp.setValue (norm_wht_sp_val);
    }
    if (!case_ins_an_val.isEmpty ()) {
      case_ins_an.setValue (case_ins_an_val);
    }
  }

  private void initSpecialListener ()
  {
    ws_listener = new PropertyChangeListener () {

      @Override
      public void propertyChange (PropertyChangeEvent evt)
      {
        String val = (String) evt.getNewValue ();
        Boolean selected = Boolean.parseBoolean (val);
        if(!selected){
          norm_nl.setValue ("False");
          norm_nl.setEnabled (false);
        }else{
          norm_nl.setEnabled (true);
        }
      }
    };
  }

}
