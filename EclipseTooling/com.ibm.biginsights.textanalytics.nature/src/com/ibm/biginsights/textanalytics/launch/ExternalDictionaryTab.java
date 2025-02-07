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
package com.ibm.biginsights.textanalytics.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.algebra.util.string.StringUtils;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ExternalDictionary;
import com.ibm.biginsights.textanalytics.nature.utils.FileDirectoryPickerUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ModuleLoadListener;
import com.ibm.biginsights.textanalytics.nature.utils.ModuleMetadataLoader;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * , sanjeev
 */
public class ExternalDictionaryTab extends AbstractLaunchConfigurationTab implements ModuleLoadListener
{
  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  private String projectName = ""; //$NON-NLS-1$
  private Table table;
  private Text tabMsg;

  /** Map of extDictName Vs the dict file */
  private Map<String, String> filePathMap = new LinkedHashMap<String, String> ();

  /*
   * List of external dictionary objects referenced by the modules in the metadata cache. Used to determine whether an
   * external dictionary is mandatory or not
   */
  private List<ExternalDictionary> extDictRefs = new ArrayList<ExternalDictionary> ();
  private FileDirectoryPicker dictPicker;
  private boolean ignoreEvents = false;
  private String selectedModules = ""; //$NON-NLS-1$
  private Map<String, String> projectSrcMap = new LinkedHashMap<String, String> ();
  private static boolean isCurrProjModular;

  private static String iconPath = "icons/full/etool16/externalDictionary.gif"; //$NON-NLS-1$
  private static String mandatoryFieldNotSetIcon = "icons/full/etool16/required_not_set.png"; //$NON-NLS-1$
  private static String mandatoryFieldIcon = "icons/full/etool16/required.png"; //$NON-NLS-1$

  @Override
  public void createControl (Composite parent)
  {
    Composite container = new Composite (parent, SWT.NULL);
    container.setLayout (new GridLayout ());
    tabMsg = new Text (container, SWT.LEFT);
    tabMsg.setText (Messages.getString ("ExternalDictionaryTab.LABEL")); //$NON-NLS-1$
    tabMsg.setEditable (false);
    tabMsg.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));
    SashForm sashForm = new SashForm (container, SWT.HORIZONTAL);
    sashForm.setLayoutData (new GridData (GridData.FILL_BOTH));
    Composite viewNames = new Composite (sashForm, SWT.BORDER);
    GridLayout layout = new GridLayout ();
    layout.marginWidth = layout.marginHeight = 1;
    viewNames.setLayout (layout);

    table = new Table (viewNames, SWT.V_SCROLL | SWT.H_SCROLL);
    table.addListener (SWT.Selection, new Listener () {
      public void handleEvent (Event e)
      {
        TableItem[] selection = table.getSelection ();

        // whenever the user selects a table entry, update the dictPicker with the corresponding dict File name
        updateDictPicker (selection[0].getText ());
      }
    });

    GridData gd = new GridData (GridData.FILL_BOTH);
    gd.widthHint = 125;
    gd.heightHint = 100;
    table.setLayoutData (gd);

    Composite viewProperties = new Composite (sashForm, SWT.BORDER);
    viewProperties.setLayout (new GridLayout ());
    Composite dataPathComposite = new Composite (viewProperties, SWT.NONE);
    GridLayout layout1 = new GridLayout ();
    layout1.marginHeight = 0;
    layout1.marginWidth = 10;
    dataPathComposite.setLayout (layout1);
    GridData gridData = new GridData (GridData.FILL_HORIZONTAL);
    dataPathComposite.setLayoutData (gridData);
    dictPicker = new FileDirectoryPicker (dataPathComposite, Constants.FILE_ONLY,
      FileDirectoryPicker.WORKSPACE_OR_EXTERNAL);
    dictPicker.setAllowedFileExtensions (""); //$NON-NLS-1$
    dictPicker.setTitle (Messages.getString ("ExternalDictionaryTab.FILE_PICKER_DESC")); //$NON-NLS-1$
    dictPicker.setDescriptionLabelText (Messages.getString ("ExternalDictionaryTab.FILE_PICKER_DESC")); //$NON-NLS-1$
    dictPicker.addModifyListenerForFileDirTextField (new ModifyListener () {
      public void modifyText (ModifyEvent e)
      {

        if (!ignoreEvents) {
          // Update filePathMap with the revised entry in dictPicker
          saveDictFileEntryIntoFileMap ();

          setDirty (true);
          updateLaunchConfigurationDialog ();

        }
      }
    });

    parent.getShell ().setMinimumSize (800, 600);
    setControl (container);

    sashForm.setWeights (new int[] { 20, 80 });

    PlatformUI.getWorkbench ().getHelpSystem ().setHelp (parent, getHelpId ());
    dictPicker.setFocus ();

    // add module load listener to check for completion of metadata load operation of modules
    ModuleMetadataLoader.getInstance ().addListener (this);
  }

  protected String getHelpId ()
  {
    return "com.ibm.biginsights.textanalytics.tooling.help.run_textanalytics"; //$NON-NLS-1$
  }

  /**
   * Update the dict picker with the dictionary file path for a given dictName. This method looks up the global data
   * structure that maintains the "dictName->fileName" map (i.e {@link #filePathMap}) to fetch the dict file
   * corresponding to the specified dictName.
   * 
   * @param dictName name of the dictionary for which dictPicker text field should be populated with the external
   *          dictionary file path.
   */
  public void updateDictPicker (String dictName)
  {
    String filePath = filePathMap.get (dictName);
    if (filePath == null) {
      filePath = ""; //$NON-NLS-1$
    }

    // this is a system generated event. Event handlers can safely ignore this.
    ignoreEvents = true;
    dictPicker.setFileDirValue (ProjectPreferencesUtil.getPath (filePath),
      ProjectPreferencesUtil.isWorkspaceResource (filePath));
    ignoreEvents = false;
  }

  @Override
  public String getName ()
  {
    return Messages.getString ("ExternalDictionaryTab.TITLE"); //$NON-NLS-1$
  }

  @SuppressWarnings("unchecked")
  @Override
  public void initializeFrom (ILaunchConfiguration config)
  {
    try {
      projectName = getProjectName (config);
      // Clear External Dictionary references when ever run configuration is changed
      extDictRefs.clear ();
      Map<String, String> extDictMap = config.getAttribute (IRunConfigConstants.EXTERNAL_DICT_MAP,
        new LinkedHashMap<String, String> ());
      // copy over the map, since Eclipse caches the map read from .launch file, resulting in our code updating
      // Eclipse's cached map, causing isDirty() to return false always, due to which Apply button won't be enabled even
      // when the user updates an entry in UI
      copyOverMap (extDictMap);
      projectSrcMap.clear ();
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
      boolean isExist = root.exists (new Path (projectName));

      if (!projectName.isEmpty () && isExist && ProjectUtils.isModularProject (projectName)) {
        AQLLibraryUtil.populateAQLLibrary (ProjectUtils.getProject (projectName));
        AQLLibraryUtil.getAllRelatedProjectSrcPaths (projectName, projectSrcMap);
        selectedModules = config.getAttribute (IRunConfigConstants.SELECTED_MODULES, ""); //$NON-NLS-1$

        isCurrProjModular = true;
        enableUI (true);
      }
      else {
        selectedModules = ""; //$NON-NLS-1$
        isCurrProjModular = false;

        // If there are no selected modules or if the project is not modular,
        // this tab is not relevant. Hence disabling the ui for this tab.
        enableUI (false);
      }
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
        Messages.getString ("ExternalEntTab.LAUNCH_CONFIG_PARAM_ERROR"), e); //$NON-NLS-1$
    }
    reInitFileMap ();
    populateExtDictPanel ();
  }

  /**
   * Copy contents from extDictMap to the filePathMap
   * 
   * @param extDictMap initial map values from the configuration
   */
  private void copyOverMap (Map<String, String> extDictMap)
  {
    filePathMap = new HashMap<String, String> ();

    for (String key : extDictMap.keySet ()) {
      filePathMap.put (key, extDictMap.get (key));
    }

  }

  protected String getProjectName (ILaunchConfiguration configuration) throws CoreException
  {
    return configuration.getAttribute (IRunConfigConstants.PROJECT_NAME, ""); //$NON-NLS-1$
  }

  @Override
  public void performApply (ILaunchConfigurationWorkingCopy config)
  {
    // Add the fileMap entries to the configuration attributes for later use.
    config.setAttribute (IRunConfigConstants.EXTERNAL_DICT_MAP, filePathMap);

  }

  /**
   * Saves the dict file path specified in {@link #dictPicker} into {@link #filePathMap}. At the same time, determines
   * if mandatory icon should be updated.
   */
  private void saveDictFileEntryIntoFileMap ()
  {

    if (table.getItemCount () > 0) {
      String path = dictPicker.getFileDirValue ();

      // if more than one table item is selected, associate the dictPicker entry with only the first selected item
      TableItem selectedItem = table.getSelection ()[0];

      String dictName = selectedItem.getText ();
      ExternalDictionary extDict = getExternalDict (dictName);

      // SPECIAL CASE: extDict is null
      if (extDict == null) return;
      // END: SPECIAL CASE

      // update filePathMap with entry in dictPicker
      filePathMap.put (selectedItem.getText (), path);

      // update icon
      updateMandatoryIcon (selectedItem, extDict, path);

    }
  }

  /**
   * gets the external dictionary object for the name of the dictionary passed, otherwise returns null.
   * 
   * @param extDictName name of the external dictionary
   * @return external dictionary object
   */
  private ExternalDictionary getExternalDict (String extDictName)
  {
    if (StringUtils.isNullOrWhiteSpace (extDictName)) return null;
    for (ExternalDictionary extDict : extDictRefs) {
      if (extDictName.equals (extDict.getDictName ())) { return extDict; }
    }

    return null;
  }

  /**
   * Sets the mandatory * mark on the dictionary name entries of the dictionary panel entries based on if the
   * dictionaries are mandatory or not.
   * 
   * @param selectedItem the dictionary name that is selected in the UI.
   * @param extDict External dictionary object corresponding to the selected item.
   * @param path location of the selected dictionary in the UI.
   */
  private void updateMandatoryIcon (TableItem selectedItem, ExternalDictionary extDict, String path)
  {
    // check if the dictionary name is present in the entry of select-able table of external dictionary tab.
    if (selectedItem.getText ().contains (extDict.getDictName ())) {

      // set the icon only if the dictionary is mandatory
      if (extDict.isMandatory ()) {

        // check if the path of the mandatory dictionary is set or not before setting the icon
        if (path.isEmpty ()) {
          selectedItem.setImage (ProjectPreferencesUtil.getImage (mandatoryFieldNotSetIcon));
        }
        else {
          selectedItem.setImage (ProjectPreferencesUtil.getImage (mandatoryFieldIcon));
        }
      }
    }

  }

  @Override
  public void setDefaults (ILaunchConfigurationWorkingCopy config)
  {
    config.setAttribute (IRunConfigConstants.EXTERNAL_DICT_MAP, new LinkedHashMap<String, String> ());
    config.setAttribute (IRunConfigConstants.EXT_DICT_REQ_VAL_LIST, new ArrayList<String> ());
  }

  @Override
  public Image getImage ()
  {
    return ProjectPreferencesUtil.getImage (iconPath);
  }

  /**
   * Whenever the External Dictionaries tab is visited by the user (or) a module is loaded (or) unloaded, refresh the
   * list of external dictionary names for which the user will have to specify a .dict file. This method will ensure
   * that the External Dictionary tab is always in sync with the list of modules selected in the main tab.
   */
  public void reInitFileMap ()
  {

    if (isCurrProjModular) {
      try {

        // instantiate the MetadataLoader class to access the loaded metadata.
        ModuleMetadataLoader metadataLoader = ModuleMetadataLoader.getInstance ();

        // Check if the metadata is loaded
        if (false == metadataLoader.isMetadataLoaded ()) { return; }

        // get latest required external dictionary list, for the set of modules selected in the Main tab
        this.extDictRefs = metadataLoader.getAllReferencedExternalDicts ();

        // if an entry in extDictName does not exist in filePathMap, initialize with empty string
        for (ExternalDictionary extDict : extDictRefs) {
          String extDictName = extDict.getDictName ();

          if (null == filePathMap.get (extDictName)) {
            // initialize an empty file name for the dict, expecting the user to populate it later on
            filePathMap.put (extDictName, "");
          }
        }
      }
      catch (Exception e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          Messages.getString ("ExternalDictionaryTab.ERR_MODULE_METADATA"), e); //$NON-NLS-1$
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean isValid (ILaunchConfiguration launchConfig)
  {
    setErrorMessage (null);

    // Use the dictionary map set in the argument 'launchConfig' instead of 'filePathMap'.
    // Info in filePathMap may not be up-to-date when another tab changes and causes this
    // function to get called.
    Map<String, String> extDictMap = new LinkedHashMap<String, String> ();
    try {
      extDictMap = launchConfig.getAttribute (IRunConfigConstants.EXTERNAL_DICT_MAP, new LinkedHashMap<String, String> ());
    }
    catch (CoreException e) {
      setErrorMessage (Messages.getString ("ExternalDictionaryTab.LAUNCH_CONFIG_PARAM_ERROR")); //$NON-NLS-1$
      return false;
    }

    for (ExternalDictionary extEntName : extDictRefs) {
      if (extEntName.isMandatory ()) {
        // check if the dictionary map has the external dictionary entry in it.
        String filepath = extDictMap.get (extEntName.getDictName ());
        if (StringUtils.isNullOrWhiteSpace (filepath)) {
          setErrorMessage (Messages.getString ("ExternalDictionaryTab.ERR_REQUIRED_FIELDS_EMPTY")); //$NON-NLS-1$
          return false;
        }
      }
    }

    for (String setPath : extDictMap.values ()) {
      if (!setPath.trim ().isEmpty ()) {
        if (!FileDirectoryPickerUtil.isPathValid (setPath)) {
          setErrorMessage (Messages.getString ("ExternalDictionaryTab.ERR_INVALID_FILE")); //$NON-NLS-1$
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Clear the tab display of all entries and display the new list.
   */
  public void populateExtDictPanel ()
  {
    ignoreEvents = true;
    table.removeAll ();
    updateDictPicker (""); //$NON-NLS-1$
    if (isCurrProjModular) {

      // No modules selected, nothing to populate
      if (selectedModules.length () == 0)
        return;

      // check metadata and load if needed
      ModuleMetadataLoader metadataLoader = ModuleMetadataLoader.getInstance ();
      if (false == metadataLoader.isMetadataLoaded ()) { return; }

      // create a table entry for each item in extDictRefs
      for (ExternalDictionary extDict : extDictRefs) {
        TableItem item = new TableItem (table, SWT.NULL);

        String extDictName = extDict.getDictName ();
        boolean isMandatory = extDict.isMandatory ();

        // set icons for mandatory fields
        if (isMandatory) {
          item.setImage (ProjectPreferencesUtil.getImage (mandatoryFieldIcon));

          // Highlight if a mandatory field is not set
          if (StringUtils.isNullOrWhiteSpace (filePathMap.get (extDictName))) {
            item.setImage (ProjectPreferencesUtil.getImage (mandatoryFieldNotSetIcon));
          }
        }

        item.setText (extDictName);
      }
    }

    // select the 0th element and populate the text field with the path of the specified dict file for 0th element
    if (table.getItemCount () > 0) {
      // if there are more than one item in the dict table, let the dictPicker display the file name of first entry
      table.select (0);
      updateDictPicker (table.getItem (0).getText ());
    }

    ignoreEvents = false;
  }

  /**
   * Provides a way to enable/disable the ui in this tab. On getting disabled, the ui will appear shaded out and will
   * not be interactable.
   * 
   * @param value
   */
  private void enableUI (boolean value)
  {
    tabMsg.setEnabled (value);
    table.setEnabled (value);
    dictPicker.enableUI (value);
  }

  /**
   * Whenever a module is selected in the Main tab, update data structures that track the list of external dictionary
   * references.
   */
  @Override
  public void moduleLoaded (String[] moduleNames) throws Exception
  {
    if (false == getControl ().isDisposed ()) {
      // Current project is set as modular because all load operations are for modules. The previous loaded
      // run configuration can be non modular so isCurrProjModular could be set as false in previous iterations.
      isCurrProjModular = true;

      reInitFileMap ();
      populateExtDictPanel ();

      setDirty (true);
      updateLaunchConfigurationDialog ();
    }
  }

  /**
   * Whenever a module is selected for unloading in the Main tab, update data structures that track the list of external
   * dictionary references.
   */
  @Override
  public void moduleUnLoaded (String moduleToUnload) throws Exception
  {
    if (false == getControl ().isDisposed ()) {
      // Current project is set as modular because all load operations are for modules. The previous loaded
      // run configuration can be non modular so isCurrProjModular could be set as false in previous iterations.
      isCurrProjModular = true;

      reInitFileMap ();
      removeExcessDictsInFileMap ();
      populateExtDictPanel ();

      setDirty (true);
      updateLaunchConfigurationDialog ();
    }

  }

  /**
   * Changes the external dictionary and its path values in the filePathMap based on module unload operation and also
   * when the file location (i e path information) of the external dictionary is changed in the UI.
   */
  private void removeExcessDictsInFileMap ()
  {
    // Step 1: Set the external dictionaries from the metadata loaded in the cache.
    ArrayList<String> extDictNames = getExtDicts ();

    /**
     * Step 2: Remove any external dictionaries that became obsolete due to unloading of some modules.<br/>
     * This is done in two steps: <br/>
     * a) identify keys of filePathMap that are in excess of extDictNames <br/>
     * b) Remove excess keys from filePathMap
     */

    // Step 2 (a): Identify excess keys in filePathMap
    Set<String> keys = filePathMap.keySet ();
    ArrayList<String> excessKeys = new ArrayList<String> (keys);
    // eliminate all the currently loaded dict names from the list
    excessKeys.removeAll (extDictNames);

    // Step 2(b): Remove excess keys from filePathMap
    for (String excessDict : excessKeys) {
      filePathMap.remove (excessDict);
    }

  }

  /**
   * gets all the external dictionary names as a list.
   * 
   * @return a list of external dictionary names
   */
  private ArrayList<String> getExtDicts ()
  {
    ArrayList<String> extDictNames = new ArrayList<String> ();
    for (ExternalDictionary extDict : extDictRefs) {
      extDictNames.add (extDict.getDictName ());
    }
    return extDictNames;
  }

}
