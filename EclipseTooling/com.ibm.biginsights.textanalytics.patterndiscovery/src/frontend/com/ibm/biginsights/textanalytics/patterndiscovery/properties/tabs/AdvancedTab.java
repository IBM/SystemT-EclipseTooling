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

import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;

/**
 * advanced tab that contains all the other sub tabs for the run configuration wizard of pd
 * 
 *
 */
@SuppressWarnings("restriction")
public class AdvancedTab extends PropsTab
{



  protected AdvancedRunTab runT;
  protected AdvancedInputTab inputT;
  protected AdvancedSeqMinTab seqminT;
  protected AdvancedRulesTab rulesT;

  public AdvancedTab (CTabFolder parent, int style, String text, String tooltip, Image image, Properties properties, LaunchConfigurationsDialog lcDialog)
  {
    super (parent, style, text, tooltip, image, properties, lcDialog);
  }

  @Override
  protected void buildUI (Composite composite)
  {
    // --- tab container ---
    CTabFolder tabFolder = new CTabFolder (composite, SWT.NONE);
    tabFolder.setLayout (new GridLayout ());
    tabFolder.setLayoutData (new GridData (SWT.FILL, SWT.FILL, true, true));

    // --- sub tabs ---
    // ===============
    // === Run tab ===
    // ===============

    runT = new AdvancedRunTab (tabFolder, SWT.NONE, Messages.LARGE_MODULES_SECTION_NAME,
      Messages.LARGE_MODULES_SECTION_TOOLTIP, null, properties, lcDialog);

    // ===============
    // === Input tab ===
    // ===============

    inputT = new AdvancedInputTab (tabFolder, SWT.NONE, Messages.INPUT_CONFIG_SECTION_NAME,
      Messages.INPUT_CONFIG_SECTION_TOOLTIP, null, properties, lcDialog);

    // ===============
    // === Sequence Mining tab ===
    // ===============

    seqminT = new AdvancedSeqMinTab (tabFolder, SWT.NONE, Messages.SEQ_MINING_SECTION_NAME,
      Messages.SEQ_MINING_SECTION_TOOLTIP, null, properties, lcDialog);

    // ===============
    // === Rules tab ===
    // ===============

    rulesT = new AdvancedRulesTab (tabFolder, SWT.NONE, Messages.RULE_CONFIGS_SECTION_NAME,
      Messages.RULE_CONFIGS_SECTION_TOOLTIP, null, properties, lcDialog);

    // === select default tab ===
    tabFolder.setSelection (runT);
  }

  @Override
  public void addPropertyChangeListener (PropertyChangeListener listener)
  {
    runT.addPropertyChangeListener (listener);
    inputT.addPropertyChangeListener (listener);
    seqminT.addPropertyChangeListener (listener);
    rulesT.addPropertyChangeListener (listener);
  }

  @Override
  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    runT.removePropertyChangeListener (listener);
    inputT.removePropertyChangeListener (listener);
    seqminT.removePropertyChangeListener (listener);
    rulesT.removePropertyChangeListener (listener);
  }

  @Override
  public void setValuesFromProperties (Properties props)
  {
    runT.setValuesFromProperties (props);
    inputT.setValuesFromProperties (props);
    seqminT.setValuesFromProperties (props);
    rulesT.setValuesFromProperties (props);
  }

}
