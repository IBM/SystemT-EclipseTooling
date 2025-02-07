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
package com.ibm.biginsights.textanalytics.patterndiscovery.runconfig;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.patterndiscovery.Activator;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.patterndiscovery.properties.tabs.AdvancedInputTab;
import com.ibm.biginsights.textanalytics.patterndiscovery.properties.tabs.AdvancedRulesTab2;
import com.ibm.biginsights.textanalytics.patterndiscovery.properties.tabs.AdvancedSeqMinTab;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;

@SuppressWarnings("restriction")
public class AdvancedPatternDiscoveryConfigTab extends    AbstractLaunchConfigurationTab
                                               implements ModifyListener, PropertyChangeListener 
{

	@SuppressWarnings("unused")

 
	LaunchConfigurationsDialog lcDialog = null;

// Defect 20435: Take out temporarily for fp1
//  protected AdvancedRunTab runT;
  protected AdvancedInputTab inputT;
  protected AdvancedSeqMinTab seqminT;
  protected AdvancedRulesTab2 rulesT;

  private boolean ignoreEvents = false;

  private Properties properties;

  // Pass to the constructor the TabGroup instead of Properties because later we may need more than just Properties.
  public AdvancedPatternDiscoveryConfigTab (SystemTPatternDiscoveryTabGroup launchConfigTabGroup, LaunchConfigurationsDialog dialog)
  {
    this.properties = launchConfigTabGroup.getProperties();
    this.lcDialog = dialog;
  }

  @Override
  public void createControl (Composite parent)
  {
    // --- tab container ---
    CTabFolder tabFolder = new CTabFolder (parent, SWT.NONE);
    tabFolder.setLayout (new GridLayout ());
    tabFolder.setLayoutData (new GridData (SWT.FILL, SWT.FILL, true, true));

    // --- sub tabs ---
    // ===============
    // === Run tab ===
    // ===============

// Defect 20435: Take out temporarily for fp1
//    runT = new AdvancedRunTab (tabFolder, SWT.NONE, Messages.LARGE_MODULES_SECTION_NAME,
//      Messages.LARGE_MODULES_SECTION_TOOLTIP, null, properties, lcDialog);

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

    rulesT = new AdvancedRulesTab2 (tabFolder, SWT.NONE, Messages.RULE_CONFIGS_SECTION_NAME,
      Messages.RULE_CONFIGS_SECTION_TOOLTIP, null, properties, lcDialog);

    // === select default tab ===
// Defect 20435: Replace with inputT temporarily for fp1
//    tabFolder.setSelection (runT);
    tabFolder.setSelection (inputT);

    addPropertyChangeListener();

    setControl(tabFolder);

    PlatformUI.getWorkbench ().getHelpSystem ().setHelp (parent,
        "com.ibm.biginsights.textanalytics.tooling.help.pattern_discovery");  //$NON-NLS-1$

    tabFolder.setFocus ();
  }

  public void addPropertyChangeListener ()
  {
// Defect 20435: Take out temporarily for fp1
//    runT.addPropertyChangeListener (this);
    inputT.addPropertyChangeListener (this);
    seqminT.addPropertyChangeListener (this);
    rulesT.addPropertyChangeListener (this);
  }

  public void removePropertyChangeListener ()
  {
// Defect 20435: Take out temporarily for fp1
//    runT.removePropertyChangeListener (this);
    inputT.removePropertyChangeListener (this);
    seqminT.removePropertyChangeListener (this);
    rulesT.removePropertyChangeListener (this);
  }

  public void setValuesFromProperties (Properties props)
  {
// Defect 20435: Take out temporarily for fp1
//    runT.setValuesFromProperties (props);
    inputT.setValuesFromProperties (props);
    seqminT.setValuesFromProperties (props);
    rulesT.setValuesFromProperties (props);
  }

  @Override
  public String getName ()
  {
    return Messages.ADVANCED_TAB_LABEL;
  }

  @Override
  public Image getImage ()
  {
    return Activator.getImageDescriptor ("advanced.gif").createImage ();    //$NON-NLS-1$
  }

  @Override
  public void initializeFrom (ILaunchConfiguration configuration)
  {
    ignoreEvents = true;

    try {
      getPropertiesFromConfiguration(configuration);
      setValuesFromProperties (properties);
    }
    catch (CoreException e) {
      e.printStackTrace ();
    }
    finally {
      ignoreEvents = false;
    }
  }

  private void getPropertiesFromConfiguration (ILaunchConfiguration configuration) throws CoreException
  {
    // These properties shouldn't be copied from launch configuration.
    // The 'properties' may be changed by the other tabs, we don't want to lose those changes.
    List<String> doNotCopy = Arrays.asList (new String[] {
      Messages.AQL_VIEW_NAME_PROP,        // output view
      PDConstants.PD_PROJECT_NAME_PROP,   // project name
      Messages.FILE_ROOT_DIR_PROP,        // root dir
      PDConstants.PD_AOG_PATH_PROP,       // aog dir
      IRunConfigConstants.EXTERNAL_DICT_MAP,
      IRunConfigConstants.EXTERNAL_TABLES_MAP,
      IRunConfigConstants.EXT_DICT_REQ_VAL_LIST,
      IRunConfigConstants.EXT_TABLE_REQ_VAL_LIST
    });

    for (Object key : configuration.getAttributes ().keySet ()) {
      if (key instanceof String && !doNotCopy.contains ((String)key)) {
      	if (key.equals ("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS") ||
      			key.equals ("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES") )
          properties.put ((String) key, configuration.getAttribute ((String) key, new ArrayList<String> ()));
      	else
      		properties.setProperty ((String) key, configuration.getAttribute ((String) key, ""));
      }
    }
  }

  @Override
  public void performApply (ILaunchConfigurationWorkingCopy configuration)
  {
    SystemTPatternDiscoveryTabGroup.copyProperties2LaunchConfig (properties, configuration);
  }

  @Override
  public void setDefaults (ILaunchConfigurationWorkingCopy configuration)
  {
  }

  @Override
  public void modifyText(ModifyEvent e) {
    if (!ignoreEvents) {
      setDirty(true);
      updateLaunchConfigurationDialog();
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (!ignoreEvents) {
      setDirty(true);
      updateLaunchConfigurationDialog();
    }
  }

}
