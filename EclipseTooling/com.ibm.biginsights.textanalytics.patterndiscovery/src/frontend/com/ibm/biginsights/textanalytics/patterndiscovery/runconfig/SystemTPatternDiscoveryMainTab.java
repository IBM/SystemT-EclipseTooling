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
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.FileDirectoryPickerUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ModuleLoadListener;
import com.ibm.biginsights.textanalytics.nature.utils.ModuleMetadataLoader;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.patterndiscovery.Activator;
import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.InternalMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * defines the main tab for the pd run config wizard
 * 
 * 
 */
public class SystemTPatternDiscoveryMainTab extends AbstractLaunchConfigurationTab implements ModifyListener, PropertyChangeListener, ModuleLoadListener
{
  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

  protected boolean ignoreEvents = false;
  private PatternDiscoveryRunConfigMainComposite tabContent;
  private Properties properties;
  private ILaunchConfiguration currentConfiguration = null;

  private static Properties defaultProperties = null;
  private static String selectedProject = null;
  private static boolean newConfig = false;

  // loads metadata for all selected modules and their dependencies
  ModuleMetadataLoader metadataCache = null;

  // Pass to the constructor the TabGroup instead of Properties because later we may need more than just Properties.
  public SystemTPatternDiscoveryMainTab (SystemTPatternDiscoveryTabGroup launchConfigTabGroup)
  {
    this.properties = launchConfigTabGroup.getProperties ();
  }

  @Override
  public void createControl (Composite parent)
  {
    parent.getShell ().setMinimumSize (750, 800);

    tabContent = new PatternDiscoveryRunConfigMainComposite (parent, SWT.NONE, properties);
    tabContent.addPropertyChangeListener (this);
    tabContent.addModifyListener (this);

    metadataCache = ModuleMetadataLoader.getInstance (null, null);
    metadataCache.addListener (this);

    setControl (tabContent);
  }

  /**
   * sets the default values which get loaded from the default properties file
   */
  @Override
  public void setDefaults (ILaunchConfigurationWorkingCopy configuration)
  {
		setLaunchConfigurationAttributes (configuration, getDefaultProperties ());
    newConfig = true;

    IProject project = ProjectPreferencesUtil.getSelectedProject ();
    if (project != null)
      setSelectedProject (project.getName ());
    else
      setSelectedProject (null);
  }

  private void setLaunchConfigurationAttributes (ILaunchConfigurationWorkingCopy config, Properties props)
  {
	  Map<String, Object> attrMap = new Hashtable<String, Object>();
	  for (Object key : props.keySet()) {
		  attrMap.put((String)key, props.get(key));
	  }

	  config.setAttributes(attrMap);
  }

  /**
   * initialize the run config form
   */
  @Override
  public void initializeFrom (ILaunchConfiguration configuration)
  {
    // This method is called not only when we switch configs but also when we switch tabs.
    // We want to initialize the form only when config is switched.
    if (currentConfiguration == configuration) return;

    currentConfiguration = configuration;
    ignoreEvents = true;

    if (newConfig) {

      // This is a new run config, set properties with default values.
      // The selected project, if any, is used as default. If no selected project
      // use the one in the run config being selected (again, if any.)
      String defaultProject = getSelectedProject ();
      if (StringUtils.isEmpty (defaultProject))
        defaultProject = properties.getProperty (PDConstants.PD_PROJECT_NAME_PROP, "");

      Properties newProperties = getDefaultProperties ();
      for (Object key : properties.keySet ())
        if (key instanceof String) properties.setProperty ((String) key, newProperties.getProperty ((String) key, ""));

      newConfig = false;

      if (!StringUtils.isEmpty (defaultProject)) {
        if (verifyProject (defaultProject)) {
          properties.setProperty (PDConstants.PD_PROJECT_NAME_PROP, defaultProject);
          tabContent.setProject (defaultProject);
        }
      }
    }
    else {
      SystemTPatternDiscoveryTabGroup.copyLaunchConfig2Properties (configuration, properties);

      String projectName = properties.getProperty (PDConstants.PD_PROJECT_NAME_PROP, "");
      tabContent.setProject (projectName);
    }

    tabContent.setValuesFromProperties (properties);

    // Update the UI.
    tabContent.setUI2Properties_noNotify (properties);

    // Update the launch config.
    if (isPropAndLaunchConfigDifferent (properties, configuration)) handlePropertiesChange ();

    ignoreEvents = false;
  }

  private boolean verifyProject (String projName)
  {
    try {
      IProject project = ResourcesPlugin.getWorkspace ().getRoot ().getProject (projName);
      if (project == null) // shouldn't happen
        return false;

      // Verify marked errors
      IMarker[] markes = project.findMarkers (IMarker.PROBLEM, false, IResource.DEPTH_INFINITE);
      if (markes.length > 0) {
        ErrorMessages.ShowErrorMessage (ErrorMessages.PATTERN_DISCOVERY_PROJECT_ERR);
        return false;
      }

      SystemTProperties projectProps = ProjectPreferencesUtil.getSystemTProperties (projName);

      if (!ProjectUtils.isModularProject (project)) {
        // Verify aql error
        String _aqlPath = ProjectPreferencesUtil.getAbsolutePath (projectProps.getMainAQLFile ());
        if (StringUtils.isEmpty (_aqlPath)) {
          ErrorMessages.ShowErrorMessage (ErrorMessages.PATTERN_DISCOVERY_VALIDATION_AQL);
          return false;
        }

        // Verify aog error
        String _aogPath = ProjectPreferencesUtil.getAbsolutePath (projectProps.getAogPath ());
        if (StringUtils.isEmpty (_aogPath)) {
          ErrorMessages.ShowErrorMessage (ErrorMessages.PATTERN_DISCOVERY_VALIDATION_AOG);
          return false;
        }
      }
    }
    catch (Exception e1) {
      ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_PROJECT_ERR, e1);
      return false;
    }

    return true;
  }

  @Override
  public void performApply (ILaunchConfigurationWorkingCopy configuration)
  {
    String projectName = properties.getProperty (PDConstants.PD_PROJECT_NAME_PROP, "");
    if (StringUtils.isEmpty (projectName)) { return; }

    // Defect 51990: We need to set the mapped resources to the associated project
    // The 'closed projects' filter relies on the mapped resources to find out the
    // related projects.
  	IProject proj = ProjectUtils.getProject (projectName);
    configuration.setMappedResources (new IResource [] { proj });

    // Store location of main aql file (defect 20169)
    String mainAqlPath = ProjectPreferencesUtil.getMainAqlPath (projectName);
    if (mainAqlPath != null) {
      properties.setProperty (PDConstants.PD_MAIN_AQL_PATH_PROP, mainAqlPath);
    }

    String docDir = properties.getProperty (InternalMessages.DOCUMENT_COLLECTION_DIR_PROP, "");
    if (!StringUtils.isEmpty (docDir)) {
      docDir = FileDirectoryPickerUtil.getPath (docDir);
    }
    properties.setProperty (InternalMessages.DOCUMENT_COLLECTION_DIR_PROP, docDir);
    SystemTPatternDiscoveryTabGroup.copyProperties2LaunchConfig (properties, configuration);
  }

  /**
   * validate each field of the run config
   */
  @Override
  public boolean isValid (ILaunchConfiguration launchConfig)
  {
    setErrorMessage (null);
    setMessage (null);

    try {
      // ============================
      // -- basic properties --
      // ============================
      // -- validate project --
      // ============================
      String projectName = launchConfig.getAttribute (PDConstants.PD_PROJECT_NAME_PROP, "");
      if (StringUtils.isEmpty (projectName)) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_VALIDATION_SELECT_PROJECT);
        return false;
      }
      IProject proj = ProjectPreferencesUtil.getProject (projectName);
      if (proj == null || !proj.exists ()) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_PROJECT_DOES_NOT_EXIST + projectName);//$NON-NLS-1$
        return false;
      }

      // =================================
      // -- validate if project is open --
      // =================================
      if (false == proj.isOpen ()) {
        setErrorMessage (com.ibm.biginsights.textanalytics.nature.Messages.getString (
          "SystemTMainTab.PROJECT_IS_CLOSED", new Object[] { projectName }));//$NON-NLS-1$
        return false;
      }

      // ====================================================
      // -------Validate if project has build errors-----//
      // ====================================================
      boolean containsErrors = ProjectUtils.hasBuildErrors (proj);
      if (containsErrors == true) {
        // display error message if the project has error marker on them.
        setErrorMessage (com.ibm.biginsights.textanalytics.nature.Messages.getString ("General.ERR_PROJECT_HAS_BUILD_ERRORS"));//$NON-NLS-1$        
        return false;
      }

      // ============================
      // -- validate output view --
      // ============================
      String outputViewName = tabContent.getSelectedOutputView ();
      if (StringUtils.isEmpty (outputViewName)) {

        if (ProjectPreferencesUtil.getOutputViews (projectName).isEmpty ())
          setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_VALIDATION_SELECT_PROJECT_WITH_OUTPUT_VIEWS);
        else
          setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_VALIDATION_SELECT_OUTPUT_VIEW);

        return false;
      }

      // ============================
      // -- validate context field --
      // ============================
      String context = launchConfig.getAttribute (Messages.GROUP_BY_FIELD_NAME_PROP, "");
      if (StringUtils.isEmpty (context) || (!tabContent.getFields ().contains (context))) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_VALIDATION_SELECT_GROUPON);
        return false;
      }

      // ==============================================================
      // -- We don't need to validate entity and snippet fields because
      // -- . they are optional
      // -- . we programmatically ensure they and group on are different.
      // ==============================================================

      // ============================
      // -- validate language --
      // ============================
      String language = launchConfig.getAttribute (PDConstants.PD_LANGUAGE_PROP, "");
      if (StringUtils.isEmpty (language)) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_VALIDATION_SELECT_LANGUAGE);
        return false;
      }

      // ============================
      // -- validate input collection --
      // ============================
      String inputCollection = launchConfig.getAttribute (InternalMessages.DOCUMENT_COLLECTION_DIR_PROP, "");
      if (StringUtils.isEmpty (inputCollection)) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_VALIDATION_DATADIR);
        return false;
      }

      boolean pathValid = FileDirectoryPickerUtil.isPathValid (inputCollection);
      if (!pathValid) {
        setErrorMessage (com.ibm.biginsights.textanalytics.nature.Messages.getString ("INPUT_COLLECTION.ERR_INPUT_COLLECTION_PATH_INVALID")); //$NON-NLS-1$
        return false;
      }

      // ============================
      // -- advanced properties --
      // ============================
      // ============================
      // -- validate input min seq length --
      // ============================
      String minSeqLen = launchConfig.getAttribute (Messages.SEQUENCE_MIN_SIZE_PROP, "");
      int minSeqLen_val;
      if (StringUtils.isEmpty (minSeqLen)) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER);
        return false;
      }
      else {
        try {
          minSeqLen_val = Integer.parseInt (minSeqLen);
        }
        catch (Exception e) {
          setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER);
          return false;
        }
      }

      // ============================
      // -- validate input max seq length --
      // ============================
      String maxSeqLen = launchConfig.getAttribute (Messages.SEQUENCE_MAX_SIZE_PROP, "");
      int maxSeqLen_val;
      if (StringUtils.isEmpty (maxSeqLen)) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER);
        return false;
      }
      else {
        try {
          maxSeqLen_val = Integer.parseInt (maxSeqLen);
        }
        catch (Exception e) {
          setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER);
          return false;
        }
      }

      // ============================
      // -- validate input min seq length against max seq length --
      // ============================
      if (minSeqLen_val > maxSeqLen_val) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_RANGE);
        return false;
      }

      // ============================
      // -- validate input min seq freq --
      // ============================
      String minSeqFreq = launchConfig.getAttribute (Messages.SEQUENCE_MAX_SIZE_PROP, "");
      if (StringUtils.isEmpty (minSeqFreq)) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER);
        return false;
      }
      else {
        try {
          Integer.parseInt (minSeqFreq);
        }
        catch (Exception e) {
          setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER);
          return false;
        }
      }

      // ============================
      // -- validate min correlation measure --
      // ============================
      String corrMin = launchConfig.getAttribute (Messages.CORRELATION_MEASURE_MIN_PROP, "");
      double corrMin_val;
      if (StringUtils.isEmpty (corrMin)) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER);
        return false;
      }
      else {
        try {
          corrMin_val = Double.parseDouble (corrMin);
        }
        catch (Exception e) {
          setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER);
          return false;
        }
      }

      // ============================
      // -- validate max correlation measure --
      // ============================
      String corrMax = launchConfig.getAttribute (Messages.CORRELATION_MEASURE_MAX_PROP, "");
      double corrMax_val;
      if (StringUtils.isEmpty (corrMax)) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER);
        return false;
      }
      else {
        try {
          corrMax_val = Double.parseDouble (corrMax);
        }
        catch (Exception e) {
          setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_NUMBER);
          return false;
        }
      }

      // ============================
      // -- validate input min correlation measure against max correlation measure --
      // ============================
      if (corrMin_val > corrMax_val) {
        setErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_INVALID_RANGE);
        return false;
      }

      // load metadata for the modules under the project
      String[] modules = ProjectUtils.getModules (projectName);
      ModuleMetadataLoader.getInstance ().load (projectName, modules);
    }
    catch (CoreException e) {
      e.printStackTrace ();
      return false;
    }

    return true;
  }

  /**
   * This method is overridden to clear the metadata cache when the run config window is closed
   */
  @Override
  public void dispose ()
  {
    // Clearing caches objects and array list on close of window or on run
    // operation
    if (metadataCache != null) {
      metadataCache.dispose ();
      metadataCache = null;
    }
  }

  @Override
  public String getName ()
  {
    return Messages.BASIC_TAB_LABEL;
  }

  public static String getSelectedProject ()
  {
    return selectedProject;
  }

  public static void setSelectedProject (String selProject)
  {
    selectedProject = selProject;
  }

  public static Properties getDefaultProperties ()
  {
    if (defaultProperties == null) {
      defaultProperties = new Properties ();
      PropertyResourceBundle props = (PropertyResourceBundle) ResourceBundle.getBundle (PDConstants.PROPERTIES_FILE);

      for (Object p : props.keySet ()) {
        String key = (String) p;
        String value = props.getString (key);
        defaultProperties.setProperty (key, value);
      }
    }

    return defaultProperties;
  }

  @Override
  public Image getImage ()
  {
    return Activator.getImageDescriptor ("general.gif").createImage (); //$NON-NLS-1$
  }

  @Override
  public void modifyText (ModifyEvent e)
  {
    if (!ignoreEvents) {
      handlePropertiesChange ();
    }
  }

  @Override
  public void propertyChange (PropertyChangeEvent evt)
  {
    if (!ignoreEvents) {
      handlePropertiesChange ();
    }
  }

  private void handlePropertiesChange ()
  {
    setDirty (true);
    updateLaunchConfigurationDialog ();
  }

  private boolean isPropAndLaunchConfigDifferent (Properties props, ILaunchConfiguration launchConfig)
  {
    try {
      for (Object k : props.keySet ()) {
        String key = (String) k;
        String s1 = props.getProperty (key, "");
        String s2 = launchConfig.getAttribute (key, "");
        if (!s1.equals (s2)) return true;
      }
    }
    catch (CoreException e) {
      // If we can't get value from launchConfig, just return false.
    }

    return false;
  }

  /**
   * Whenever a module is selected in the Main tab, update data structures that track the list of external dictionary
   * references.
   */
  @Override
  public void moduleLoaded (String[] moduleNames) throws Exception
  {
    if (!ignoreEvents) {
      if (moduleNames != null && moduleNames.length > 0) {
        setDirty (true);
        if (false == getControl ().isDisposed ()) {
          updateLaunchConfigurationDialog ();
        }
      }
    }

  }

  /**
   * Whenever a module is selected for unloading in the Main tab, update data structures that track the list of external
   * dictionary references.
   */
  @Override
  public void moduleUnLoaded (String moduleToUnload) throws Exception
  {
    if (!ignoreEvents) {
      if (moduleToUnload != null) {
        setDirty (true);
        if (false == getControl ().isDisposed ()) {
          updateLaunchConfigurationDialog ();
        }
      }
    }

  }
}
