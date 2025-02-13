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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.algebra.util.string.StringUtils;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ExternalDictionary;
import com.ibm.biginsights.textanalytics.nature.utils.ExternalTable;
import com.ibm.biginsights.textanalytics.nature.utils.FileDirectoryPickerUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ModuleLoadListener;
import com.ibm.biginsights.textanalytics.nature.utils.ModuleMetadataLoader;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.ProjectBrowser;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class provides the 'Main' tab of SystemT run configuration page
 * 
 * , sanjeev
 */
public class SystemTMainTab extends AbstractLaunchConfigurationTab implements ModuleLoadListener
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

  private static final String PROPERTY_NAME_PROJECT_NAME = "projectName"; //$NON-NLS-1$
  protected FileDirectoryPicker inputCollectionPicker;
  protected String inputCollectionValue;
  protected Label lbDelimiter;
  protected Combo cbDelimiter;
  protected Text txtCustomDelimiter;

  protected Table moduleTable;
  protected ProjectBrowser projectBrowser;
  protected Combo cbLanguage;
  protected Composite composite;
  protected Composite modulePanel;
  // A panel to contain buttons to select and de-select modules
  protected Composite moduleButtonPanel;
  protected Button selectAll;
  protected Button clear;
  protected Composite projBrowserPanel;
  protected Composite parent;
  protected Color white;
  protected String selectedModules;
  protected String configName = null;
  protected String currentProject;
  protected ILaunchConfiguration mainTabConfiguration;
  // A Set to hold modules that have build errors in them
  protected Set<String> modulesWithErrors = new HashSet<String> ();

  // A progress bar for displaying status of the load operation
  protected ProgressBar pgBar;

  // status message for the progress bar indicating load operation
  protected Label pgBarLabel;

  // loads metadata for all selected modules and their dependencies
  ModuleMetadataLoader metadataCache = null;

  /**
   * The purpose of this flag is to determine if property change event / widget selection event should be handled or not
   * When initializeFrom() restores the state of the UI, we know that the widgets are undergoing a state change, hence
   * no need to process events related to UI state change at that point of time. Ensure that initializeFrom() sets this
   * flag to true at the beginning of the method and sets it back to false at the end of the method
   */
  protected boolean ignoreEvents = false;

  @Override
  public void createControl (Composite parent)
  {
    this.parent = parent;
    composite = new Composite (parent, SWT.NONE);
    composite.setLayout (new GridLayout ());

    createProjectBrowserPanel (composite);

    createModulePanel (composite);

    createLanguagePanel (composite);

    createInputCollectionPanel (composite);

    setControl (composite);

    parent.getShell ().setMinimumSize (500, 480);

    PlatformUI.getWorkbench ().getHelpSystem ().setHelp (parent,
      "com.ibm.biginsights.textanalytics.tooling.help.run_textanalytics");//$NON-NLS-1$

    createProgressBarPanel ();

    // instantiate module metadata cache
    metadataCache = ModuleMetadataLoader.getInstance (pgBarLabel, pgBar);

    // listener to indicate the status of load operation.
    metadataCache.addListener (this);
  }

  private void createProgressBarPanel ()
  {
    // A Label to explain the process going on for the progress bar.
    pgBarLabel = new Label (composite, SWT.NONE);
    pgBarLabel.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));
    // pgBarLabel.setText (Messages.getString ("SystemTMainTab.PROGRESSBAR_LABEL"));
    pgBarLabel.setVisible (false);

    // A progress bar to show the status for loading modules
    pgBar = new ProgressBar (composite, SWT.NONE);
    pgBar.setMaximum (100);
    pgBar.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));
    pgBar.setVisible (false);
  }

  private void createInputCollectionPanel (Composite parentComposite)
  {
    inputCollectionPicker = new FileDirectoryPicker (parentComposite, Constants.FILE_OR_DIRECTORY,
                                                     FileDirectoryPicker.WORKSPACE_OR_EXTERNAL);
    inputCollectionPicker.setTitle (Messages.getString ("SystemTMainTab.INPUT_COLLECTION_SELECTION"));                  //$NON-NLS-1$
    inputCollectionPicker.setMessage (Messages.getString ("SystemTMainTab.SELECT_FILE_DIR_INPUT_COLLECTION"));          //$NON-NLS-1$
    inputCollectionPicker.setDescriptionLabelText (Messages.getString ("SystemTMainTab.INPUT_COLLECTION_TO_ANALYZE"));  //$NON-NLS-1$
    inputCollectionPicker.setEditable (true);
    inputCollectionPicker.setAllowedFileExtensions (Constants.SUPPORTED_DOC_FORMATS);
    inputCollectionPicker.setEnableShowAllFilesOption (true);
    inputCollectionPicker.setAllowMultipleSelection (false);

    inputCollectionPicker.addModifyListenerForFileDirTextField (new InputCollectionListener ());

    Composite delimComposite = new Composite (parentComposite, SWT.NONE);
    GridLayout layout = new GridLayout (3, false);
    delimComposite.setLayout (layout);
    delimComposite.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    lbDelimiter = new Label (delimComposite, SWT.NONE);
    lbDelimiter.setText (Constants.DELIMITER); //$NON-NLS-1$
    lbDelimiter.setEnabled (false);

    cbDelimiter = new Combo (delimComposite, SWT.READ_ONLY);
    cbDelimiter.setItems (Constants.COMMON_DELIMS);
    cbDelimiter.setEnabled (false);
    cbDelimiter.addSelectionListener (new DelimiterSelectionListener ());

    txtCustomDelimiter = new Text (delimComposite, SWT.BORDER);
    txtCustomDelimiter.setTextLimit (1);
    GridData gd = new GridData ();
    gd.widthHint = 15;
    txtCustomDelimiter.setLayoutData (gd);
    txtCustomDelimiter.addModifyListener (new SpecialDelimModifyListener ());
  }

  private void createLanguagePanel (Composite parentComposite)
  {
    Composite languagePanel = new Composite (parentComposite, SWT.NONE);
    GridLayout layout = new GridLayout (2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 10;
    languagePanel.setLayout (layout);
    languagePanel.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label lbLanguage1 = new Label (languagePanel, SWT.NONE);
    lbLanguage1.setText (Messages.getString ("GeneralPrefPage.LANGUAGE")); //$NON-NLS-1$

    cbLanguage = new Combo (languagePanel, SWT.READ_ONLY);

    // Languages are in synch with the list of languages supported by the
    // SystemT Runtime
    for (LangCode language : LangCode.values ()) {
      cbLanguage.add (language.name ());
    }
    cbLanguage.select (cbLanguage.indexOf (LangCode.en.name ()));
    cbLanguage.addSelectionListener (new LanguageSelectionListener ());
  }

  private void createProjectBrowserPanel (Composite parentComposite)
  {
    projBrowserPanel = new Composite (parentComposite, SWT.NONE);
    GridLayout projLayout = new GridLayout ();
    projBrowserPanel.setLayout (projLayout);
    projBrowserPanel.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    projectBrowser = new ProjectBrowser (projBrowserPanel, SWT.NONE);
    projectBrowser.addModifyListenerForProjectTextField (new ProjectChangedListener ());
    PropertyChangeListener listener = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent event)
      {
        if (event.getPropertyName () != null && PROPERTY_NAME_PROJECT_NAME.equals (event.getPropertyName ())) {
          // When ever there is property change due to renaming of configuration then unload the old projects modules
          // from the cache. This will ensure that only metadata of current project's modules are in the cache.
          String oldProject = (String) event.getOldValue ();
          unloadModules (oldProject);

          // Load the changed configuration's modules
          String newProject = (String) event.getNewValue ();
          populateModules (newProject);
        }
      }

      /**
       * Unloads all the previously loaded modules of the project from the cache.
       * 
       * @param projectName The project name whose modules have to be unloaded.
       */
      private void unloadModules (String projectName)
      {
        if (false == StringUtils.isNullOrWhiteSpace (projectName)) {
          String[] modules = ProjectUtils.getAllModules (projectName);
          for (String module : modules) {
            // since ModuleMetadataLoader will unload only those modules that were previously loaded there
            // is no harm in calling unload for all modules of the project.
            metadataCache.unLoad (module);
          }
        }
      }
    };
    projectBrowser.addPropertyChangeListener (listener);
  }

  private void createModulePanel (Composite parentComposite)
  {
    white = parentComposite.getDisplay ().getSystemColor (SWT.COLOR_WHITE);
    moduleButtonPanel = new Composite (parentComposite, SWT.NONE);
    GridLayout secondlayout = new GridLayout (2, false);
    moduleButtonPanel.setLayout (secondlayout);
    moduleButtonPanel.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label lbLanguage = new Label (moduleButtonPanel, SWT.NONE);
    lbLanguage.setText (Messages.getString ("SystemTMainTab.SELECT_Modules")); //$NON-NLS-1$
    final Composite moduleComposite = new Composite (moduleButtonPanel, SWT.NONE);
    GridLayout layout = new GridLayout (2, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    moduleComposite.setLayout (layout);
    moduleComposite.setLayoutData (new GridData (GridData.FILL_BOTH));

    modulePanel = new Composite (moduleComposite, SWT.NONE);
    moduleTable = new Table (modulePanel, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    GridLayout moduleLayout = new GridLayout (1, false);
    moduleLayout.marginHeight = 0;
    moduleLayout.marginWidth = 10;
    modulePanel.setLayout (moduleLayout);

    Accessible lbLanguageLabel = lbLanguage.getAccessible ();
    Accessible moduleTableAcc = moduleTable.getAccessible ();
    lbLanguageLabel.addRelation (ACC.RELATION_LABEL_FOR, moduleTableAcc);
    moduleTableAcc.addRelation (ACC.RELATION_LABELLED_BY, lbLanguageLabel);

    GridData gd = new GridData (GridData.FILL_BOTH);
    // gd.grabExcessHorizontalSpace = true;
    // gd.grabExcessVerticalSpace = true;
    gd.widthHint = 250;
    gd.heightHint = 100;
    moduleTable.setLayoutData (gd);
    modulePanel.layout ();

    Composite secinnPanel = new Composite (moduleComposite, SWT.NONE);
    GridLayout secinnlayout = new GridLayout ();
    secinnlayout.marginHeight = 1;
    secinnlayout.marginWidth = 10;
    secinnPanel.setLayout (secinnlayout);
    secinnPanel.setLayoutData (new GridData (GridData.FILL_VERTICAL));
    selectAll = new Button (secinnPanel, SWT.PUSH);
    selectAll.setSize (160, 130);
    selectAll.setText (Messages.getString ("WizardPage.SELECT_ALL"));//$NON-NLS-1$
    selectAll.addSelectionListener (new SelectionAdapter () {
      public void widgetSelected (SelectionEvent e)
      {
        Control ctrl[] = modulePanel.getChildren ();
        TableItem[] tItems = ((Table) ctrl[0]).getItems ();
        for (TableItem tItem : tItems) {
          tItem.setChecked (true);

          // load metadata for all the items selected previously while loading for the first time only when module panel
          // is not disabled.
          if (modulePanel.isEnabled ()) {
            loadItemMetadata (tItem);
          }
        }
        if (!ignoreEvents) {
          setDirty (true);
          updateLaunchConfigurationDialog ();
        }

      }

    });
    clear = new Button (secinnPanel, SWT.PUSH);
    clear.setSize (150, 90);
    clear.setText ("   Clear    ");//$NON-NLS-1$
    clear.addSelectionListener (new SelectionAdapter () {
      public void widgetSelected (SelectionEvent e)
      {
        Control ctrl[] = modulePanel.getChildren ();
        TableItem[] tItems = ((Table) ctrl[0]).getItems ();
        for (TableItem tItem : tItems) {
          tItem.setChecked (false);

          // unload unchecked items from cache when clear button is selected.
          unLoadItemMetadata (tItem);

        }

        if (!ignoreEvents) {
          setDirty (true);
          updateLaunchConfigurationDialog ();
        }
      }
    });

  }

  /**
   * method to unload metadata for a TableItem that was unchecked in the module panel
   * 
   * @param tItem TableItem element that triggered the event due to being unchecked.
   */
  private void unLoadItemMetadata (TableItem tItem)
  {
    // Unload metadata when ever clear all is selected in the module panel.
    // getting the item that triggered the event
    Widget moduleTableItem = tItem;

    // get the TableItem vlaue from the item, this value is populated as per the module
    // panel in the run config gui
    TableItem moduleTable = ((TableItem) moduleTableItem);

    // check if the module was un checked and was set as checked
    if (false == moduleTable.getChecked ()) {

      // get the text associated with the unchecked element,
      // which is actually the module name
      String moduleToUnload = moduleTable.getText ();

      // perform metadata unload for the selected module
      metadataCache.unLoad (moduleToUnload);
    }
  }

  /**
   * method to load metadata for a TableItem that was selected in the module panel
   * 
   * @param tItem TableItem element that triggered the event due to being checked.
   */
  private void loadItemMetadata (TableItem tItem)
  {

    // Load metadata when ever select all is made in the module panel.
    // getting the item that triggered the event
    Widget moduleTableItem = tItem;

    // get the TableItem vlaue from the item, this value is
    // populated as per the module panel in the run config gui
    TableItem moduleTable = ((TableItem) moduleTableItem);

    // check if the module was checked and was set as checked
    if (moduleTable.getChecked ()) {

      // get the text associated with the checked element, which is actually the module name
      String moduleName = moduleTable.getText ();

      // Do not load modules that have build errors
      if (modulesWithErrors.contains (moduleName)) { return; }

      // adding module name to a array of String
      String[] modulesToLoad = new String[] { moduleName };

      // perform metadata load for the selected module

      metadataCache.load (projectBrowser.getProject (), modulesToLoad);
    }

  }

  @Override
  public String getName ()
  {
    return Messages.getString ("SystemTMainTab.MAIN"); //$NON-NLS-1$
  }

  @Override
  public void initializeFrom (ILaunchConfiguration configuration)
  {
    if (configuration == mainTabConfiguration)
      return;

    mainTabConfiguration = configuration;
    ignoreEvents = true;
    try {
      // Initialize the UI with data from ILaunchConfigurationCopy
      String projectName = configuration.getAttribute (IRunConfigConstants.PROJECT_NAME, ""); //$NON-NLS-1$

      String lang = configuration.getAttribute (IRunConfigConstants.LANG, "en"); //$NON-NLS-1$
      cbLanguage.select (getIndex (lang));

      String inputCollection = configuration.getAttribute (IRunConfigConstants.INPUT_COLLECTION, ""); //$NON-NLS-1$
      if (inputCollection.isEmpty () == false) {
        String path = ProjectPreferencesUtil.getPath (inputCollection);
        boolean isWS = ProjectPreferencesUtil.isWorkspaceResource (inputCollection);
        inputCollectionPicker.setFileDirValue (path, isWS);

        // If this is a CSV file, enable the CSV fields
        // and fill out with the saved values.
        if (inputCollection.endsWith (Constants.CSV_EXTENSION) &&
            ProjectPreferencesUtil.isExistingFile (inputCollection)) {

          lbDelimiter.setEnabled (true);
          cbDelimiter.setEnabled (true);

          String delim = configuration.getAttribute (IRunConfigConstants.DELIMITER, ""); //$NON-NLS-1$
          if (delim.isEmpty () == false) {
            cbDelimiter.setText (delim);
            if (delim.equals (cbDelimiter.getText ()) == false) {  // if it is NOT a common delimiter
              cbDelimiter.setText (Constants.CUSTOM);
              txtCustomDelimiter.setEnabled (true);
              txtCustomDelimiter.setText (delim);
            }
          }
          else {
            cbDelimiter.setText (Constants.COMMA);
          }
        }
      }

      // Reload project modules metadata because run configuration changed
      metadataCache.clear ();

      selectedModules = configuration.getAttribute (IRunConfigConstants.SELECTED_MODULES, "");
      if (selectedModules.isEmpty () == false &&
          projectName.isEmpty () == false) {

        metadataCache.load (projectName, selectedModules.split (";"), false);

        // Remove redundant external dictionaries and tables
        if (configuration instanceof ILaunchConfigurationWorkingCopy) {
          List<ExternalDictionary> extDicts = metadataCache.getAllReferencedExternalDicts ();
          List<ExternalTable> extTables = metadataCache.getAllReferredExternalTables ();
          boolean changed = LaunchConfigrationUtils.removeRedundantsAddNews ((ILaunchConfigurationWorkingCopy)configuration, extDicts, extTables);
          if (changed) {
            setDirty (true);
            updateLaunchConfigurationDialog ();
          }
        }
      }

      // Set project name last so populateModules() has information about selectedModules.
      projectBrowser.setProject (projectName);
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
        com.ibm.biginsights.textanalytics.aql.library.Messages.ERROR_REQ_MODULES_GEN_ERROR, e); //$NON-NLS-1$
    }

    ignoreEvents = false;
  }

  /**
   * Identifies modules in the given project and populate them in the module panel in the run configuration main tab.
   * The module panel is disabled for closed and non existent projects, in these cases no module names are displayed in
   * module panel. For projects with build errors, modules are loaded but they will remain disabled till errors are
   * corrected.
   * 
   * @param projectName project to run
   */
  private void populateModules (String projectName)
  {
    if (projectName == null || projectName.isEmpty ()) {
      if (modulePanel == null || modulePanel.isDisposed ()) {
        createModulePanel (composite);
        moduleButtonPanel.moveBelow (projBrowserPanel);
        return;
      }

      if (modulePanel != null || !modulePanel.isDisposed ()) {
        Control ctrl[] = modulePanel.getChildren ();
        TableItem[] tItems = ((Table) ctrl[0]).getItems ();
        for (TableItem tItem : tItems) {
          tItem.dispose ();
        }
        modulePanel.redraw ();
        modulePanel.pack ();
      }
      return;
    }

    IProject project = ProjectUtils.getProject (projectName);

    // Disable all the module panel UI if the project is closed or does not exist.
    if (project.isOpen () == false || project.exists () == false) {
      disableModulePanelUI ();
      return;
    }
    else {
      // Enable the module panel if previous run configuration had disabled it.
      if (modulePanel.isEnabled () == false || moduleButtonPanel.isEnabled () == false) {
        enableModulePanelUI ();
      }
    }

    boolean isModular = ProjectUtils.isModularProject (project);
    if (!isModular) {
      if (moduleButtonPanel != null && !moduleButtonPanel.isDisposed ()) {
        // hide module panel if the project it is non modular. Not disposing the module panel as switching between
        // configurations of older and newer versions would cause the module panel to be disabled.
        hideModulePanel ();
      }
    }
    else {// start modular project

      if (moduleButtonPanel == null || moduleButtonPanel.isDisposed ()) {
        modulePanel.dispose ();
        createModulePanel (composite);
        moduleButtonPanel.moveBelow (projBrowserPanel);
      }
      // The module display panel may have been made invisible when switching from project from 1.x to 2.x.
      if (false == moduleButtonPanel.isVisible () || false == modulePanel.isVisible ()) {
        showModulePanel ();
      }

    }// end modular project
    String modules[] = null;
    if (isModular) {
      modules = ProjectUtils.getModules (project);
    }
    if (moduleButtonPanel != null && !moduleButtonPanel.isDisposed () && modulePanel != null
      && !modulePanel.isDisposed ()) {
      if (modules == null) {
        Control[] control1 = moduleButtonPanel.getChildren ();
        for (int i = 0; i < control1.length; i++) {
          if (control1[i] instanceof Composite) {
            Control[] control2 = ((Composite) control1[i]).getChildren ();
            for (int j = 0; j < control2.length; j++) {
              if (control2[j] instanceof Table && !control2[j].isDisposed ()) {
                TableItem[] tItems = ((Table) control2[j]).getItems ();
                for (TableItem tItem : tItems) {
                  tItem.setGrayed (true);
                }
                // ((Table) control2[j]).setEnabled (false);
              }
            }
          }
        }
        Control ctrl[] = modulePanel.getChildren ();
        if (ctrl != null) {
          TableItem[] tItems = ((Table) ctrl[0]).getItems ();
          for (TableItem tItem : tItems) {
            tItem.setGrayed (true);
          }
        }
      }
    }

    if (modules != null && !modulePanel.isDisposed ()) {
      Control ctrl[] = modulePanel.getChildren ();
      TableItem[] tItems = ((Table) ctrl[0]).getItems ();
      for (TableItem tItem : tItems) {
        tItem.dispose ();
      }
      Set<String> selectedModulesSet = null;
      if (selectedModules != null) {
        selectedModulesSet = new HashSet<String> ();
        String mod[] = selectedModules.split (Constants.DATAPATH_SEPARATOR);
        for (String string : mod) {
          selectedModulesSet.add (string);
        }
      }

      // Check if project has error markers on it(not including module level error markers).
      boolean projectHasError = ProjectUtils.hasProjectErrors (project);

      for (String module : modules) {
        TableItem item = new TableItem (moduleTable, SWT.NONE);
        item.setText (module);
        if (selectedModulesSet != null && selectedModulesSet.contains (module)) {
          item.setChecked (true);

          // adding module name to a array of String
          String[] modulesToLoad = new String[] { module };

          // Do not load metadata if project has project level error markers.
          if (false == projectHasError) {
            // Perform metadata load for the selected modules.
            // We don't want to run it in a separate thread because we want metadata loaded before continuing.
            metadataCache.load (projectBrowser.getProject (), modulesToLoad, false);
          }
        }
      }

      moduleTable.addSelectionListener (new ModuleSelectionListener ());
      modulePanel.layout ();
      if (moduleButtonPanel != null && !moduleButtonPanel.isDisposed ()) {
        Control[] control1 = moduleButtonPanel.getChildren ();
        for (int i = 0; i < control1.length; i++) {
          if (control1[i] instanceof Composite) {
            Control[] control2 = ((Composite) control1[i]).getChildren ();
            for (int j = 0; j < control2.length; j++) {
              if (control2[j] instanceof Table && !control2[j].isDisposed ()) {
                ((Table) control2[j]).setEnabled (true);
              }
            }
          }
        }
      }
      modulePanel.redraw ();
    }

    if (moduleButtonPanel != null && !moduleButtonPanel.isDisposed ()) moduleButtonPanel.redraw ();

    composite.redraw ();
    parent.redraw ();
    parent.pack (true);
    this.getLaunchConfigurationDialog ().updateButtons ();
    selectedModules = null;
  }

  /**
   * Sets the module panel visible and moves it below the project browser. This is necessary when switching between
   * configurations for projects in 1.X to 2.X happens. or also when changing projects by using browse button. (where
   * the project changes from 1.X to 2.X)
   */
  private void showModulePanel ()
  {
    modulePanel.setVisible (true);
    moduleButtonPanel.setVisible (true);
    moduleButtonPanel.moveBelow (projBrowserPanel);
    enableModulePanelUI ();

  }

  /**
   * Hides the module panel from the user and moves it below the input collection picker. This is done to maintain the
   * original UI for the non modular projects.
   */
  private void hideModulePanel ()
  {
    Composite parentComposite = moduleButtonPanel.getParent ();
    modulePanel.setVisible (false);
    moduleButtonPanel.setVisible (false);
    // move the module panel below to maintain the original dialog display for non modular projects.
    moduleButtonPanel.moveBelow (inputCollectionPicker);
    disableModulePanelUI ();
    parentComposite.pack (true);
  }

  /**
   * Enables the module panel UI if it was disabled by previous run configuration.
   */
  private void enableModulePanelUI ()
  {
    moduleTable.setEnabled (true);
    modulePanel.setEnabled (true);
    moduleButtonPanel.setEnabled (true);
    selectAll.setEnabled (true);
    clear.setEnabled (true);
  }

  /**
   * Disables the module panel UI. The user will not be able to select module panel elements.
   */
  private void disableModulePanelUI ()
  {
    moduleTable.setEnabled (false);
    modulePanel.setEnabled (false);
    moduleButtonPanel.setEnabled (false);
    selectAll.setEnabled (false);
    clear.setEnabled (false);
  }

  @Override
  public void performApply (ILaunchConfigurationWorkingCopy configuration)
  {
  	String projName = projectBrowser.getProject ();
    configuration.setAttribute (IRunConfigConstants.PROJECT_NAME, projName);

    // Defect 51990: We need to set the mapped resources to the associated project
    // The 'closed projects' filter relies on the mapped resources to find out the
    // related projects.
    if (projName != null && !projName.isEmpty ()) {
    	IProject proj = ProjectUtils.getProject (projName);
    	if (proj != null)
    		configuration.setMappedResources (new IResource [] { proj });
    }

    String selectedModules = "";

    if (modulePanel != null && !modulePanel.isDisposed ()
      && ProjectUtils.isModularProject (projectBrowser.getProject ())) {
      Control ctrl[] = modulePanel.getChildren ();
      for (Control control : ctrl) {
        if (control instanceof Table) {
          Table modTable = (Table) control;
          TableItem[] tabItems = modTable.getItems ();
          for (TableItem tabItem : tabItems) {

            if (tabItem.getChecked ()) {
              selectedModules += tabItem.getText () + Constants.DATAPATH_SEPARATOR;
            }
          }
        }

      }

    }
    else {
      selectedModules = "genericModule";
    }
    configuration.setAttribute (IRunConfigConstants.SELECTED_MODULES, selectedModules);

    if (!ignoreEvents) {
      inputCollectionValue = inputCollectionPicker.getFileDirValue ();
    }

    inputCollectionValue = FileDirectoryPickerUtil.getPath (inputCollectionValue);
    configuration.setAttribute (IRunConfigConstants.INPUT_COLLECTION, inputCollectionValue);

    if (cbDelimiter.isEnabled ()) {
      String delim = cbDelimiter.getText ();
      if (delim.equals (Constants.CUSTOM))
        delim = txtCustomDelimiter.getText ();

      configuration.setAttribute (IRunConfigConstants.DELIMITER, delim);
    }
    else {
      configuration.setAttribute (IRunConfigConstants.DELIMITER, (String)null);
    }

    configuration.setAttribute (IRunConfigConstants.LANG, cbLanguage.getItem (cbLanguage.getSelectionIndex ()));
  }

  @Override
  public void setDefaults (ILaunchConfigurationWorkingCopy config)
  {
    IProject project = ProjectPreferencesUtil.getSelectedProject ();
    if (project != null) {
      String selectedProject = project.getName ();
      config.setAttribute (IRunConfigConstants.PROJECT_NAME, selectedProject);
    }
  }

  @Override
  public Image getImage ()
  {
    return ProjectPreferencesUtil.getImage ("icons/full/etool16/main.gif");

  }

  @Override
  public boolean isValid (ILaunchConfiguration launchConfig)
  {
    setErrorMessage (null);
    setMessage (null);

    boolean valid = false;
    try {
      // -------- Validate project name --------//
      String projectName = launchConfig.getAttribute (IRunConfigConstants.PROJECT_NAME, ""); //$NON-NLS-1$
      valid = (projectName.trim ().length () != 0);
      if (!valid) {
        setErrorMessage (Messages.getString ("SystemTMainTab.ERR_PROJECT_FIELD_CANNOT_BE_EMPTY")); //$NON-NLS-1$
        return false;
      }

      // -------- Validate project existence --------//
      IProject proj = ProjectPreferencesUtil.getProject (projectName);
      if (proj == null || !proj.exists ()) {
        setErrorMessage (Messages.getString ("SystemTMainTab.PROJECT_NOT_EXIST", new Object[] { projectName }));//$NON-NLS-1$
        return false;
      }

      // -----------Validate if project is open--------//
      if (false == proj.isOpen ()) {
        setErrorMessage (Messages.getString ("SystemTMainTab.PROJECT_IS_CLOSED", new Object[] { projectName }));//$NON-NLS-1$
        return false;
      }

      // -----------ValidateProject Errors-------------//
      // If the project has error markers(other than the ones on modules, like reference project not found) then show
      // error message.
      if (ProjectUtils.hasProjectErrors (proj)) {
        setErrorMessage (Messages.getString ("SystemTMainTab.PROJECT_HAS_ERROR_MARKERS", new Object[] { projectName }));//$NON-NLS-1$
        disableModulePanelUI ();
        return false;
      }

      // -------- Validate module selection --------//
      List<String> selectedModules = new ArrayList<String> ();
      // A List to hold all the selected modules that have errors in them
      List<String> errorModulesSelected = new ArrayList<String> ();
      if (modulePanel != null && !modulePanel.isDisposed () && ProjectUtils.isModularProject (proj)) {
        Control ctrl[] = modulePanel.getChildren ();
        boolean containsVal = false;
        for (Control control : ctrl) {
          if (control instanceof Table) {
            Table modTable = (Table) control;
            if (modTable.getItemCount () >= 1) {
              containsVal = true;
            }
            for (TableItem tItem : modTable.getItems ()) {
              if (tItem.getChecked ()) {
                selectedModules.add (tItem.getText ());
                // Check if the checked module has errors in them, If yes add it to the list
                if (modulesWithErrors.contains (tItem.getText ())) {
                  errorModulesSelected.add (tItem.getText ());
                }
              }
            }
          }
        }
        if (!containsVal) {
          setErrorMessage (Messages.getString ("SystemTMainTab.ERR_MSG2"));//$NON-NLS-1$
          return false;
        }
        if (selectedModules.isEmpty ()) {
          setErrorMessage (Messages.getString ("SystemTMainTab.ERR_MSG1"));//$NON-NLS-1$
          return false;
        }
        // If modules having errors are selected then display error message.
        if (errorModulesSelected.isEmpty () == false) {
          setErrorMessage (Messages.getString (
            "SystemTMainTab.SELECTED_MODULE_HAS_ERRORS", new Object[] { errorModulesSelected.toString () }));//$NON-NLS-1$
          return false;
        }
      }

      // -------- Validate input collection --------//
      String inputCollection = launchConfig.getAttribute (IRunConfigConstants.INPUT_COLLECTION, ""); //$NON-NLS-1$
      valid = (inputCollection.trim ().length () != 0);
      if (!valid) {
        setErrorMessage (Messages.getString ("SystemTMainTab.ERR_INPUT_COLLECTION_CANNOT_BE_EMPTY")); //$NON-NLS-1$
        return false;
      }
      boolean pathValid = FileDirectoryPickerUtil.isPathValid (inputCollection);
      if (!pathValid) {
        String pathNotExist = inputCollection;
        if (pathNotExist.startsWith (Constants.WORKSPACE_RESOURCE_PREFIX)) {
          pathNotExist = pathNotExist.replace (Constants.WORKSPACE_RESOURCE_PREFIX, ""); //$NON-NLS-1$
        }
        setErrorMessage (Messages.getString (
          "SystemtRunMainTab.ERR_INPUT_COLLECTION_NOT_FOUND", new Object[] { pathNotExist })); //$NON-NLS-1$
        return false;
      }
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
    }
    catch (Exception cause) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (cause.getMessage ());
    }
    return valid;
  }

  private int getIndex (String item)
  {
    String[] items = cbLanguage.getItems ();
    if (items != null && items.length > 0) {
      for (int i = 0; i < items.length; ++i) {
        if (item.equals (items[i])) { return i; }
      }
    }
    return -1;
  }

  /**
   * This method is overridden to clear the metadata cache when the run config window is closed
   */
  @Override
  public void dispose ()
  {
    // Clearing cache metadata cache and its instance
    // operation
    if (metadataCache != null) {
      metadataCache.dispose ();
      metadataCache = null;

    }

  }

  // ////////////////////////////////////////////////////

  private class ProjectChangedListener implements ModifyListener
  {

    @Override
    public void modifyText (ModifyEvent event)
    {

      // Get all the modules in the project that have build errors in them. This is done every time the project changes.
      if (false == StringUtils.isNullOrWhiteSpace (projectBrowser.getProject ())) {
        modulesWithErrors = ProjectUtils.getModulesWithError (ProjectUtils.getProject (projectBrowser.getProject ()));
      }

      if (!ignoreEvents) {
        setDirty (true);

        // populate AQL library because we may need it.
        if (!projectBrowser.getProject ().equals (currentProject)) {
          currentProject = projectBrowser.getProject ();
          AQLLibraryUtil.populateAQLLibrary (ProjectUtils.getProject (currentProject));
        }

        updateLaunchConfigurationDialog ();
      }
    }

  }

  /**
   * A listener to listen to changes done to the input collection location in the main tab.
   * 
   * 
   */
  private class InputCollectionListener implements ModifyListener
  {
    @Override
    public void modifyText (ModifyEvent event)
    {
      File selFile = inputCollectionPicker.getSelectedFile ();
      if (selFile != null && selFile.isFile () && selFile.getName ().endsWith (Constants.CSV_EXTENSION)) {
        lbDelimiter.setEnabled (true);
        cbDelimiter.setEnabled (true);
        if (cbDelimiter.getText ().isEmpty ()) {
          cbDelimiter.select (cbDelimiter.indexOf (Constants.COMMA));
          txtCustomDelimiter.setEnabled (false);
        }
        else {
          if (cbDelimiter.getText ().equals (Constants.CUSTOM))
            txtCustomDelimiter.setEnabled (true);
          else
            txtCustomDelimiter.setEnabled (false);
        }
      }
      else {
        lbDelimiter.setEnabled (false);
        cbDelimiter.setEnabled (false);
        cbDelimiter.deselectAll ();
        txtCustomDelimiter.setEnabled (false);
        txtCustomDelimiter.setText ("");
      }

      if (!ignoreEvents) {
        setDirty (true);
        updateLaunchConfigurationDialog ();
      }
    }

  }

  private class ModuleSelectionListener implements SelectionListener
  {

    @Override
    public void widgetDefaultSelected (SelectionEvent arg0)
    {
      if (!ignoreEvents) {
        setDirty (true);
        updateLaunchConfigurationDialog ();
      }

    }

    @Override
    public void widgetSelected (SelectionEvent event)
    {
      if (!ignoreEvents) {
        setDirty (true);

        // Load metadata when ever a new selection is made in the module panel.
        // getting the item that triggered the event
        Widget item = event.item;

        // get the TableItem vlaue from the item, this value is populated as per the module
        // panel in the run config gui.
        TableItem moduleTableItem = ((TableItem) item);

        // get the text associated with the checked element, which is actually the module name
        String moduleName = moduleTableItem.getText ();

        // check if the module was checked and was set as checked
        if (true == moduleTableItem.getChecked ()) {
          if (moduleName != null) {

            // If the module selected contains errors then do not load it.
            if (modulesWithErrors.contains (moduleName)) {
              // Calling update launch configuration as isValid() will set the error message.
              updateLaunchConfigurationDialog ();
              return;
            }
            // adding module name to a array of String
            String[] modulesToLoad = new String[] { moduleName };

            // perform metadata load for the selected module
            metadataCache.load (projectBrowser.getProject (), modulesToLoad);
            // Calling update launch configuration as isValid() will change the error message once a module is loaded
            updateLaunchConfigurationDialog ();
          }
        }
        else { // element unchecked
          metadataCache.clear ();
          String[] selModules = getCheckedModules ();
          if (selModules.length > 0)
            metadataCache.load (projectBrowser.getProject (), selModules);

          updateLaunchConfigurationDialog ();
        }
      }
    }
  }

  private String[] getCheckedModules ()
  {
    List<String> checkedItems = new ArrayList<String> ();

    for (TableItem ti : moduleTable.getItems ()) {
      if (ti.getChecked ())
        checkedItems.add (ti.getText ());
    }

    return checkedItems.toArray (new String[0]);
  }

  private class DelimiterSelectionListener extends SelectionAdapter
  {
    @Override
    public void widgetSelected (SelectionEvent e)
    {
      if (!ignoreEvents) {
        if (cbDelimiter.getText ().equals (Constants.CUSTOM)) {
          txtCustomDelimiter.setEnabled (true);
        }
        else {
          txtCustomDelimiter.setEnabled (false);
          txtCustomDelimiter.setText ("");
        }

        setDirty (true);
        updateLaunchConfigurationDialog ();
      }
    }
  }

  private class SpecialDelimModifyListener implements ModifyListener
  {
    @Override
    public void modifyText (ModifyEvent e)
    {
      setDirty (true);
      updateLaunchConfigurationDialog ();
    }
  }

  private class LanguageSelectionListener extends SelectionAdapter
  {
    @Override
    public void widgetSelected (SelectionEvent e)
    {
      if (!ignoreEvents) {
        setDirty (true);
        updateLaunchConfigurationDialog ();
      }
    }
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

          // update the UI state
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

          // notify other tabs about the changes
          updateLaunchConfigurationDialog ();
        }
      }
    }

  }

}
