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
import com.ibm.biginsights.textanalytics.nature.utils.ExternalTable;
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
public class ExternalTablesTab extends AbstractLaunchConfigurationTab implements ModuleLoadListener
{
  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  private String projectName = ""; //$NON-NLS-1$
  private Table table;
  private Text tabMsg;

  /** Map of extTableName Vs the table file */
  private Map<String, String> filePathMap = new LinkedHashMap<String, String> ();

  /*
   * List of external table objects referenced by the modules in the metadata cache. Used to determine whether an
   * external table is mandatory or not
   */
  private List<ExternalTable> extTableRefs = new ArrayList<ExternalTable> ();
  private FileDirectoryPicker tablePicker;
  private boolean ignoreEvents = false;
  private String selectedModules = ""; //$NON-NLS-1$
  private Map<String, String> projectSrcMap = new LinkedHashMap<String, String> ();
  private static boolean isCurrProjModular;

  private static String iconPath = "icons/full/etool16/externalTables.gif"; //$NON-NLS-1$
  private static String mandatoryFieldNotSetIcon = "icons/full/etool16/required_not_set.png"; //$NON-NLS-1$
  private static String mandatoryFieldIcon = "icons/full/etool16/required.png"; //$NON-NLS-1$

  @Override
  public void createControl (Composite parent)
  {
    ignoreEvents = true;
    Composite container = new Composite (parent, SWT.NULL);
    container.setLayout (new GridLayout ());
    tabMsg = new Text (container, SWT.LEFT);
    tabMsg.setText (Messages.getString ("ExternalTablesTab.LABEL")); //$NON-NLS-1$
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

        // whenever the user selects a table entry, update the tablePicker with the corresponding table File name
        updateTablePicker (selection[0].getText ());

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
    tablePicker = new FileDirectoryPicker (dataPathComposite, Constants.FILE_ONLY,
      FileDirectoryPicker.WORKSPACE_OR_EXTERNAL);
    tablePicker.setAllowedFileExtensions (Constants.CSV_EXTENSION_STRING);
    tablePicker.setTitle (Messages.getString ("ExternalTablesTab.FILE_PICKER_DESC")); //$NON-NLS-1$
    tablePicker.setDescriptionLabelText (Messages.getString ("ExternalTablesTab.FILE_PICKER_DESC")); //$NON-NLS-1$
    tablePicker.addModifyListenerForFileDirTextField (new ModifyListener () {
      public void modifyText (ModifyEvent e)
      {

        if (!ignoreEvents) {
          // Update filePathMap with the revised entry in tablePicker
          saveTableFileEntryIntoFileMap ();

          setDirty (true);
          updateLaunchConfigurationDialog ();
        }
      }
    });

    parent.getShell ().setMinimumSize (800, 600);
    sashForm.setWeights (new int[] { 20, 80 });
    setControl (container);

    PlatformUI.getWorkbench ().getHelpSystem ().setHelp (parent, getHelpId ());
    tablePicker.setFocus ();

    // add module load listener to check for completion of metadata load operation of modules
      ModuleMetadataLoader.getInstance ().addListener (this);
  }

  protected String getHelpId ()
  {
    return "com.ibm.biginsights.textanalytics.tooling.help.run_textanalytics"; //$NON-NLS-1$
  }

  /**
   * Update the table picker with the table file path for a given tableName. This method looks up the global data
   * structure that maintains the "tableName->fileName" map (i.e {@link #filePathMap}) to fetch the table file
   * corresponding to the specified tableName.
   * 
   * @param tableName name of the table for which tablePicker text field should be populated with the external table
   *          file path.
   */
  public void updateTablePicker (String tableName)
  {
    String filePath = filePathMap.get (tableName);
    if (filePath == null) {
      filePath = ""; //$NON-NLS-1$
    }

    // this is a system generated event. Event handlers can safely ignore this.
    ignoreEvents = true;
    tablePicker.setFileDirValue (ProjectPreferencesUtil.getPath (filePath),
      ProjectPreferencesUtil.isWorkspaceResource (filePath));
    ignoreEvents = false;
  }

  @Override
  public String getName ()
  {
    return Messages.getString ("ExternalTablesTab.TITLE"); //$NON-NLS-1$
  }

  @SuppressWarnings("unchecked")
  @Override
  public void initializeFrom (ILaunchConfiguration configuration)
  {
    try {
      projectName = getProjectName (configuration);
      // Clear External Table references when ever run configuration is changed
      extTableRefs.clear ();
      Map<String, String> extTableMap = configuration.getAttribute (IRunConfigConstants.EXTERNAL_TABLES_MAP,
        new LinkedHashMap<String, String> ());
      // copy over the map, since Eclipse caches the map read from .launch file, resulting in our code updating
      // Eclipse's cached map, causing isDirty() to return false always, due to which Apply button won't be enabled even
      // when the user updates an entry in UI
      copyOverMap (extTableMap);
      projectSrcMap.clear ();
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
      boolean isExist = root.exists (new Path (projectName));

      if (!projectName.isEmpty () && isExist && ProjectUtils.isModularProject (projectName)) {
        AQLLibraryUtil.populateAQLLibrary (ProjectUtils.getProject (projectName));
        AQLLibraryUtil.getAllRelatedProjectSrcPaths (projectName, projectSrcMap);
        selectedModules = configuration.getAttribute (IRunConfigConstants.SELECTED_MODULES, ""); //$NON-NLS-1$

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
    populateExtTablePanel ();
  }

  /**
   * Copy contents from extTableMap to the filePathMap
   * 
   * @param extTableMap initial map values from the configuration
   */
  private void copyOverMap (Map<String, String> extTableMap)
  {
    filePathMap = new HashMap<String, String> ();

    for (String key : extTableMap.keySet ()) {
      filePathMap.put (key, extTableMap.get (key));
    }

  }

  protected String getProjectName (ILaunchConfiguration configuration) throws CoreException
  {
    return configuration.getAttribute (IRunConfigConstants.PROJECT_NAME, ""); //$NON-NLS-1$
  }

  @Override
  public void performApply (ILaunchConfigurationWorkingCopy configuration)
  {
    // Add the fileMap entries to the configuration attributes for later use.
    configuration.setAttribute (IRunConfigConstants.EXTERNAL_TABLES_MAP, filePathMap);

  }

  /**
   * Saves the table file path specified in {@link #tablePicker} into {@link #filePathMap}. At the same time, determines
   * if mandatory icon should be updated..
   */
  private void saveTableFileEntryIntoFileMap ()
  {

    if (table.getItemCount () > 0) {
      String path = tablePicker.getFileDirValue ();

      // if more than one table item is selected, associate the tablePicker entry with only the first selected item
      TableItem selectedItem = table.getSelection ()[0];

      String tableName = selectedItem.getText ();
      ExternalTable extTable = getExternalTable (tableName);

      // SPECIAL CASE: extTable is null
      if (extTable == null) return;
      // END: SPECIAL CASE

      // update filePathMap with entry in tablePicker
      filePathMap.put (selectedItem.getText (), path);

      // update icon
      updateMandatoryIcon (selectedItem, extTable, path);

    }
  }

  /**
   * gets the external table object for the name of the table passed, otherwise returns null
   * 
   * @param extTableName name of the external table
   * @return external table object
   */
  private ExternalTable getExternalTable (String extTableName)
  {
    if (StringUtils.isNullOrWhiteSpace (extTableName)) return null;
    for (ExternalTable extTable : extTableRefs) {
      if (extTableName.equals (extTable.getTableName ())) { return extTable; }
    }

    return null;
  }

  /**
   * Sets the mandatory * mark on the table name entries of the table panel entries based on if the tables are mandatory
   * or not.
   * 
   * @param selectedItem the table name that is selected in the UI.
   * @param extTable External table object corresponding to the selected item.
   * @param path location of the selected table in the UI.
   */
  private void updateMandatoryIcon (TableItem selectedItem, ExternalTable extTable, String path)
  {
    // check if the table name is present in the entry of select-able table of external tables tab.
    if (selectedItem.getText ().contains (extTable.getTableName ())) {

      // set the icon only if the table is mandatory
      if (extTable.isMandatory ()) {

        // check if the path of the mandatory table is set or not before setting the icon
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
    config.setAttribute (IRunConfigConstants.EXTERNAL_TABLES_MAP, new LinkedHashMap<String, String> ());
    config.setAttribute (IRunConfigConstants.EXT_TABLE_REQ_VAL_LIST, new ArrayList<String> ());
  }

  @Override
  public Image getImage ()
  {
    return ProjectPreferencesUtil.getImage (iconPath);
  }

  /**
   * Whenever the External tables tab is visited by the user (or) a module is loaded (or) unloaded, refresh the list of
   * external dtable names for which the user will have to specify a .csv file. This method will ensure that the
   * External tables tab is always in sync with the list of modules selected in the main tab.
   */
  public void reInitFileMap ()
  {

    if (isCurrProjModular) {
      try {

        // instantiate the MetadataLoader class to access the loaded metadata.
        ModuleMetadataLoader metadataLoader = ModuleMetadataLoader.getInstance ();

        // Check if the metadata is loaded
        if (false == metadataLoader.isMetadataLoaded ()) { return; }

        // get latest required external tables list, for the set of modules selected in the Main tab
        this.extTableRefs = metadataLoader.getAllReferredExternalTables ();

        // if an entry in extTableName does not exist in filePathMap, initialize with empty string
        for (ExternalTable extTable : extTableRefs) {
          String extTableName = extTable.getTableName ();

          if (null == filePathMap.get (extTableName)) {
            // initialize an empty file name for the table, expecting the user to populate it later on
            filePathMap.put (extTableName, "");
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
    Map<String, String> extTableMap = new LinkedHashMap<String, String> ();
    try {
      extTableMap = launchConfig.getAttribute (IRunConfigConstants.EXTERNAL_TABLES_MAP, new LinkedHashMap<String, String> ());
    }
    catch (CoreException e) {
      setErrorMessage (Messages.getString ("ExternalDictionaryTab.LAUNCH_CONFIG_PARAM_ERROR")); //$NON-NLS-1$
      return false;
    }

    for (ExternalTable extEntName : extTableRefs) {
      if (extEntName.isMandatory ()) {
        // check if the filePathMap has the external table entry in it.
        String filepath = extTableMap.get (extEntName.getTableName ());
        if (StringUtils.isNullOrWhiteSpace (filepath)) {
          setErrorMessage (Messages.getString ("ExternalTablesTab.ERR_REQUIRED_FIELDS_EMPTY")); //$NON-NLS-1$
          return false;
        }
      }
    }

    for (String setPath : filePathMap.values ()) {
      if (!setPath.trim ().isEmpty ()) {
        if (!FileDirectoryPickerUtil.isPathValid (setPath)) {
          setErrorMessage (Messages.getString ("ExternalTablesTab.ERR_INVALID_FILE")); //$NON-NLS-1$
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Clear the tab display of all entries and display the new list.
   */
  public void populateExtTablePanel ()
  {
    ignoreEvents = true;
    table.removeAll ();
    updateTablePicker (""); //$NON-NLS-1$
    if (isCurrProjModular) {

      // No modules selected, nothing to populate
      if (selectedModules.length () == 0)
        return;

      // check metadata and load if needed
      ModuleMetadataLoader metadataLoader = ModuleMetadataLoader.getInstance ();
      if (false == metadataLoader.isMetadataLoaded ()) { return; }

      // create a table entry for each item in extTableRefs
      for (ExternalTable extTable : extTableRefs) {
        TableItem item = new TableItem (table, SWT.NULL);

        String extTableName = extTable.getTableName ();
        boolean isMandatory = extTable.isMandatory ();

        // set icons for mandatory fields
        if (isMandatory) {
          item.setImage (ProjectPreferencesUtil.getImage (mandatoryFieldIcon));

          // Highlight if a mandatory field is not set
          if (StringUtils.isNullOrWhiteSpace (filePathMap.get (extTableName))) {
            item.setImage (ProjectPreferencesUtil.getImage (mandatoryFieldNotSetIcon));
          }
        }

        item.setText (extTableName);
      }
    }

    // select the 0th element and populate the text field with the path of the specified table file for 0th element
    if (table.getItemCount () > 0) {
      // if there are more than one item in the external table, let the tablePicker display the file name of first entry
      table.select (0);
      updateTablePicker (table.getItem (0).getText ());
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
    tablePicker.enableUI (value);
  }

  /**
   * Whenever a module is selected in the Main tab, update data structures that track the list of external tables
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
      populateExtTablePanel ();

      setDirty (true);
      updateLaunchConfigurationDialog ();
    }
  }

  /**
   * Whenever a module is selected for unloading in the Main tab, update data structures that track the list of external
   * table references.
   */
  @Override
  public void moduleUnLoaded (String moduleToUnload) throws Exception
  {
    if (false == getControl ().isDisposed ()) {
      // Current project is set as modular because all load operations are for modules. The previous loaded
      // run configuration can be non modular so isCurrProjModular could be set as false in previous iterations.
      isCurrProjModular = true;

      reInitFileMap ();
      removeExcessTableInFileMap ();
      populateExtTablePanel ();

      setDirty (true);
      updateLaunchConfigurationDialog ();
    }

  }

  /**
   * Changes the external table and its path values in the filePathMap based on module unload operation and also when
   * the file location (i e path information) of the external table is changed in the UI.
   */
  private void removeExcessTableInFileMap ()
  {
    // Step 1: Set the external tables from the metadata loaded in the cache.
    ArrayList<String> extTableNames = getExtTables ();

    /**
     * Step 2: Remove any external tables that became obsolete due to unloading of some modules.<br/>
     * This is done in two steps: <br/>
     * a) identify keys of filePathMap that are in excess of extTableNames <br/>
     * b) Remove excess keys from filePathMap
     */

    // Step 2 (a): Identify excess keys in filePathMap
    Set<String> keys = filePathMap.keySet ();
    ArrayList<String> excessKeys = new ArrayList<String> (keys);
    // eliminate all the currently loaded table names from the list
    excessKeys.removeAll (extTableNames);

    // Step 2(b): Remove excess keys from filePathMap
    for (String excessTable : excessKeys) {
      filePathMap.remove (excessTable);
    }

  }

  /**
   * gets all the external table names as a list.
   * 
   * @return a list of external table names
   */
  private ArrayList<String> getExtTables ()
  {
    ArrayList<String> extTableNames = new ArrayList<String> ();
    for (ExternalTable extTable : extTableRefs) {
      extTableNames.add (extTable.getTableName ());
    }
    return extTableNames;
  }

}
