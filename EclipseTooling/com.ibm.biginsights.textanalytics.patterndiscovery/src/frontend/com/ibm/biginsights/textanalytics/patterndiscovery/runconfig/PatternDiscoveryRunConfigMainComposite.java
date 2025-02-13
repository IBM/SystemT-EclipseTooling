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
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.AQLUtils;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.PDConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.InternalMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.messages.Messages;
import com.ibm.biginsights.textanalytics.patterndiscovery.newwidgets.ComboInput;
import com.ibm.biginsights.textanalytics.patterndiscovery.newwidgets.ListInput;
import com.ibm.biginsights.textanalytics.patterndiscovery.properties.tabs.AdvancedTab;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.ProjectBrowser;

/**
 * defines the main composite that holds the pd run configuration wizard
 * 
 * 
 */
public class PatternDiscoveryRunConfigMainComposite extends Composite
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

  Properties properties;
  PropertyChangeSupport propertyChangeSupport;

  private ProjectBrowser projectBrowser;
  private FileDirectoryPicker inputCollection;
  protected Label lbDelimiter;
  protected ComboInput cbDelimiter;
  protected Text txtCustomDelimiter;

  private PropertyChangeListener projectListener;
  private ModifyListener inputCollectionListener, contextListener, txtCustDelListener;
  private SelectionListener delimiterComboListener;

  private ComboInput outputViewCombo, contextCombo, snippetCombo;
  private ListInput entityList;

  private SelectionListener entityListListener;

  // Map from full output view name to its span fields.
  // "Full view name" is explained below. For aliases, runtime does
  // not prefixed with module name. Adding module prefix to avoid
  // ambiguity when more than one module contains duplicate alias.
  protected Map<String, ArrayList<String>> entitySpanFields;

  // Map from full output view name to its ViewInfo object.
  // "full view Name" means it is always prefixed with module name, including when
  // it is an alias. We must use full view name as key because view can be an alias
  // defined in different modules. Another reason is so we can find out which module
  // the view (alias) belongs to.
  protected HashMap<String, ViewInfo> outputViewInfo;

  protected AdvancedTab advInpTab;
  private PropertyChangeListener outputViewListener;
  private ComboInput langCombo;

  private ArrayList<String> fields = new ArrayList<String> ();

  public PatternDiscoveryRunConfigMainComposite (Composite parent, int style, Properties properties)
  {
    super (parent, style);

    this.properties = properties;
    this.propertyChangeSupport = new PropertyChangeSupport (this);

    outputViewInfo = new HashMap<String, ViewInfo> ();

    entitySpanFields = new HashMap<String, ArrayList<String>> ();

    setLayout (new GridLayout (1, true));
    setLayoutData (new GridData (GridData.FILL_BOTH));

    buildUI ();

    PlatformUI.getWorkbench ().getHelpSystem ().setHelp (parent,
      "com.ibm.biginsights.textanalytics.tooling.help.pattern_discovery"); //$NON-NLS-1$

    outputViewCombo.setFocus ();
  }

  private void buildUI ()
  {
    // -- init the listeners to be assigned to the ui elements --
    initListeners ();

    // -- build ui --

    projectBrowser = new ProjectBrowser (this, SWT.NONE);
    projectBrowser.addPropertyChangeListener (projectListener);

    Composite outputViewComposite = new Composite (this, SWT.NONE);
    outputViewComposite.setLayout (new GridLayout (2, false));
    outputViewComposite.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label outputViewLabel = new Label (outputViewComposite, SWT.NONE);
    outputViewLabel.setText ("Output View : "); //$NON-NLS-1$
    outputViewLabel.setToolTipText (Messages.GROUP_BY_FIELD_NAME_TOOLTIP);

    outputViewCombo = new ComboInput (outputViewComposite, SWT.NONE, Messages.AQL_VIEW_NAME_PROP, properties,
      new ArrayList<String> (), "", false);
    outputViewCombo.addPropertyChangeListener (outputViewListener);

    Group basicPropsGroup = new Group (this, SWT.NONE);
    basicPropsGroup.setLayout (new GridLayout (1, true));
    basicPropsGroup.setText (Messages.BASIC_GROUP_LABEL);
    basicPropsGroup.setToolTipText (Messages.BASIC_GROUP_TOOLTIP);
    basicPropsGroup.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Composite internalComposite = new Composite (basicPropsGroup, SWT.NONE);
    internalComposite.setLayout (new GridLayout (2, false));
    internalComposite.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label contextLabel = new Label (internalComposite, SWT.NONE);
    contextLabel.setText (Messages.GROUP_BY_FIELD_NAME_LABEL);
    contextLabel.setToolTipText (Messages.GROUP_BY_FIELD_NAME_TOOLTIP);

    contextCombo = new ComboInput (internalComposite, SWT.NONE, Messages.GROUP_BY_FIELD_NAME_PROP, properties,
      new ArrayList<String> (), "", true);
    contextCombo.getCombo ().addModifyListener (contextListener);

    Label entityLabel = new Label (internalComposite, SWT.NONE);
    entityLabel.setText (Messages.ENTITY_FIELD_NAMES_LABEL);
    entityLabel.setToolTipText (Messages.ENTITY_FIELD_NAMES_TOOLTIP);

    entityList = new ListInput (internalComposite, SWT.NONE, Messages.ENTITY_FIELD_NAMES_PROP, properties, "",
      new ArrayList<String> ());
    entityList.getList ().addSelectionListener (entityListListener);

    Label snippetLabel = new Label (internalComposite, SWT.NONE);
    snippetLabel.setText (Messages.SNIPPET_FIELD_NAME_LABEL);
    snippetLabel.setToolTipText (Messages.SNIPPET_FIELD_NAME_TOOLTIP);

    snippetCombo = new ComboInput (internalComposite, SWT.NONE, Messages.SNIPPET_FIELD_NAME_PROP, properties,
      new ArrayList<String> (), "", true);

    Group collectionPropsGroup = new Group (this, SWT.NONE);
    GridLayout layout = new GridLayout (1, true);
    layout.marginLeft = 0;
    collectionPropsGroup.setLayout (layout);
    collectionPropsGroup.setText ("Input Collection");
    collectionPropsGroup.setToolTipText (Messages.BASIC_GROUP_TOOLTIP);
    collectionPropsGroup.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Composite collectioninternalComposite = new Composite (collectionPropsGroup, SWT.NONE);
    collectioninternalComposite.setLayout (new GridLayout (1, false));
    collectioninternalComposite.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Composite languageComposite = new Composite (collectioninternalComposite, SWT.NONE);
    languageComposite.setLayout (new GridLayout (2, false));
    languageComposite.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    Label languageLabel = new Label (languageComposite, SWT.NONE);
    languageLabel.setText (Messages.LANGUAGE_LABEL);

    langCombo = new ComboInput (languageComposite, SWT.NONE, PDConstants.PD_LANGUAGE_PROP, properties, getLanguages (),
      "", true);

    createInputCollectionPanel (collectioninternalComposite);
  }

  private void createInputCollectionPanel (Composite parentComposite)
  {
    inputCollection = new FileDirectoryPicker (parentComposite, Constants.FILE_OR_DIRECTORY,
      FileDirectoryPicker.WORKSPACE_OR_EXTERNAL);
    inputCollection.setDescriptionLabelText (InternalMessages.DOCUMENT_COLLECTION_DIR_LABEL);
    inputCollection.setEditable (true);
    inputCollection.setAllowedFileExtensions (Constants.SUPPORTED_DOC_FORMATS);
    inputCollection.setEnableShowAllFilesOption (true);
    inputCollection.setAllowMultipleSelection (false);
    inputCollection.addModifyListenerForFileDirTextField (inputCollectionListener);

    Composite delimComposite = new Composite (parentComposite, SWT.NONE);
    GridLayout layout = new GridLayout (3, false);
    delimComposite.setLayout (layout);
    delimComposite.setLayoutData (new GridData (GridData.FILL_HORIZONTAL));

    lbDelimiter = new Label (delimComposite, SWT.NONE);
    lbDelimiter.setText (Constants.DELIMITER); //$NON-NLS-1$
    lbDelimiter.setEnabled (false);

    ArrayList<String> delimList = new ArrayList<String> (Arrays.asList (Constants.COMMON_DELIMS));
    cbDelimiter = new ComboInput (delimComposite, SWT.READ_ONLY, IRunConfigConstants.DELIMITER, properties, delimList, "", true);
    cbDelimiter.setLayoutData (new GridData ());
    cbDelimiter.setEnabled (false);
    cbDelimiter.getCombo ().addSelectionListener (delimiterComboListener);

    txtCustomDelimiter = new Text (delimComposite, SWT.BORDER);
    txtCustomDelimiter.setTextLimit (1);
    GridData gd = new GridData ();
    gd.widthHint = 15;
    txtCustomDelimiter.setLayoutData (gd);
    txtCustomDelimiter.addModifyListener (txtCustDelListener);
  }

  /**
   * @param projectName
   */
  public void setProject (String projectName)
  {
    if (projectName.isEmpty ()) return;
    IProject project = ResourcesPlugin.getWorkspace ().getRoot ().getProject (projectName);
    if (project == null) {
      handleMissingProject ();
      return;
    }
    projectBrowser.setProject (projectName);
    projectChanged ();
  }

  /**
   * init all the internal listeners for this class
   */
  private void initListeners ()
  {
    projectListener = new PropertyChangeListener () {
      @Override
      public void propertyChange (PropertyChangeEvent event)
      {
        if (event.getPropertyName ().equals (PDConstants.PD_PROJECT_NAME_PROP)
          && !projectBrowser.getProject ().isEmpty ()) {
          projectChanged ();
        }
      }
    };

    outputViewListener = new PropertyChangeListener () {

      @Override
      public void propertyChange (PropertyChangeEvent evt)
      {
        updateOutputViewRelatedProps ();
      }
    };

    contextListener = new ModifyListener () {

      @Override
      public void modifyText (ModifyEvent e)
      {
        entityList.setOptions (getEntityOptions ());
        updateSnippetField ();
      }

    };

    entityListListener = new SelectionListener () {

      @Override
      public void widgetSelected (SelectionEvent e)
      {
        updateSnippetField ();
      }

      @Override
      public void widgetDefaultSelected (SelectionEvent e)
      {}
    };

    inputCollectionListener = new ModifyListener () {
      @Override
      public void modifyText (ModifyEvent e)
      {
        File selFile = inputCollection.getSelectedFile ();
        if (selFile != null && selFile.isFile () && selFile.getName ().endsWith (Constants.CSV_EXTENSION)) {
          lbDelimiter.setEnabled (true);
          cbDelimiter.setEnabled (true);
          if (cbDelimiter.getValue ().isEmpty ()) {
            cbDelimiter.setValue (Constants.COMMA);
            txtCustomDelimiter.setEnabled (false);
          }
          else {
            if (cbDelimiter.getValue ().equals (Constants.CUSTOM)) {
              txtCustomDelimiter.setEnabled (true);
            }
            else {
              txtCustomDelimiter.setEnabled (false);
            }
          }
        }
        else {
          lbDelimiter.setEnabled (false);
          cbDelimiter.setEnabled (false);
          cbDelimiter.setValue (null);
          txtCustomDelimiter.setEnabled (false);
          txtCustomDelimiter.setText ("");
        }

        properties.setProperty (InternalMessages.DOCUMENT_COLLECTION_DIR_PROP, inputCollection.getFileDirValue ());
      }
    };

    delimiterComboListener = new SelectionAdapter ()
    {
      @Override
      public void widgetSelected (SelectionEvent e)
      {
        String delim = cbDelimiter.getValue ();
        if (delim.equals (Constants.CUSTOM)) {
          txtCustomDelimiter.setEnabled (true);
          delim = txtCustomDelimiter.getText ();
        }
        else {
          txtCustomDelimiter.setText ("");
          txtCustomDelimiter.setEnabled (false);
        }
        properties.setProperty (IRunConfigConstants.DELIMITER, delim);
      }
    };

    txtCustDelListener = new ModifyListener() {
      @Override
      public void modifyText (ModifyEvent e)
      {
        if (txtCustomDelimiter.isEnabled ())
          properties.setProperty (IRunConfigConstants.DELIMITER, txtCustomDelimiter.getText ());
      }
    };
  }

  private void updateSnippetField ()
  {
    String currentSnippet = snippetCombo.getValue ();
    ArrayList<String> snippets = getSnippetOptions ();

    snippetCombo.setOptions (snippets);

    if (snippets.contains (currentSnippet))
      snippetCombo.setValue (currentSnippet);
    else
      snippetCombo.setValue (PDConstants.PD_DEFAULT_SNIPPET);
  }

  /**
   * runnable that process the current view and build a system-t object from this project from which gets the fields
   * related to it
   */
  private void updateOutputViewRelatedProps ()
  {
    try {
      fields = new ArrayList<String> ();
      String outputView = outputViewCombo.getValue ();
      setOutputViewProperty (outputView);

      if (outputView != null && !outputView.isEmpty ()) fields = entitySpanFields.get (outputView);

      // loadSpanFields will return null in case no span fields are
      // found - handle it gracefully here
      if (fields != null) {
        resetBasicPropertiesChoices (fields);

        // If old context is one of the new ones, keep it.
        // Otherwise, clear it.
        String context = properties.getProperty (Messages.GROUP_BY_FIELD_NAME_PROP, "");
        if (fields.contains (context))
          contextCombo.setValue (context);
        else
          contextCombo.setValue ("");
      }
    }
    catch (Exception e) {
      ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_PROCESSING_MESSAGE, e);
    }
  }

  private void setOutputViewProperty (String outputView)
  {
    properties.setProperty (Messages.AQL_VIEW_NAME_PROP, "");
    properties.setProperty (Messages.AQL_MODULE_NAME_PROP, "");

    if (!StringUtils.isEmpty (outputView)) {
      ViewInfo vi = outputViewInfo.get (outputView);
      if (vi != null) {
        properties.setProperty (Messages.AQL_VIEW_NAME_PROP, vi.getOriginalViewName ());
        properties.setProperty (Messages.AQL_MODULE_NAME_PROP, vi.getModuleName ());
      }
    }
  }

  private void refreshOutputViewsAndSpanFields (String project)
  {
    ModuleMetadata[] moduleMDs = ProjectPreferencesUtil.getModuleMetadata (project);

    entitySpanFields.clear ();
    outputViewInfo.clear ();

    for (ModuleMetadata md : moduleMDs) {

      ModuleMetadata[] module = new ModuleMetadata[] { md };
      ArrayList<String> moduleOutputViews = (ArrayList<String>) ProjectPreferencesUtil.getOutputViews (module);

      for (String outputView : moduleOutputViews) {
        String fullOutputViewName = outputView;
        if (!outputView.contains (".")) fullOutputViewName = md.getModuleName () + "." + outputView;

        outputViewInfo.put (fullOutputViewName, new ViewInfo (fullOutputViewName, outputView, md.getModuleName ()));
        entitySpanFields.put (fullOutputViewName, ProjectPreferencesUtil.getSpanFields (md, outputView));
      }
    }
  }

  /**
   * we use this method to alert the user when the validation for a project fails. the reasons may be that the project
   * name provided doesn't belong to any project in the workspace
   */
  private void handleMissingProject ()
  {
    resetUI ();
    ErrorMessages.ShowErrorMessage (ErrorMessages.PATTERN_DISCOVERY_VALIDATION_PROJECT);
  }

  /**
   * We cannot run pattern discovery in projects with compilation errors
   * 
   * @param t
   */
  private void handleProjectErrors (Throwable t)
  {
    resetUI ();
    ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_PROJECT_ERR, t);
  }

  /**
   * action called when the project is changed
   */
  private void projectChanged ()
  {
    String projectName = projectBrowser.getProject ();
    if (StringUtils.isEmpty (projectName)) return;
    IProject project = ProjectUtils.getProject (projectBrowser.getProject ());

    // If Project is closed then return, as error is thrown in the pattern discovery config dialog itself
    if (false == project.isOpen ()) { return; }

    // continue if project exists
    if (project.exists ()) {
      try {
        IMarker[] markes = project.findMarkers (IMarker.PROBLEM, false, IResource.DEPTH_INFINITE);
        if (markes.length > 0) {
          handleProjectErrors (new Exception (ErrorMessages.PATTERN_DISCOVERY_PROJECT_ERR));
          return;
        }
      }
      catch (CoreException e1) {
        handleProjectErrors (e1);
        return;
      }
      updateProjectLocationAndRelatedProps ();
    }

  }

  /**
   * this function creates an array of options for the entity field. these options are composed of the view name and the
   * span field, separated by the specific separator defined as a constant
   * 
   * @return
   */
  private ArrayList<String> getEntityOptions ()
  {
    ArrayList<String> ret = new ArrayList<String> ();

    String selectedOutputView = outputViewCombo.getValue ();
    if (StringUtils.isEmpty (selectedOutputView)) return ret;

    String moduleName = selectedOutputView.substring (0, selectedOutputView.lastIndexOf ("."));
    String selectedGroupOn = contextCombo.getValue ();

    for (String view : entitySpanFields.keySet ()) {
      // Only show fields of output views of the module to be run
      String module = view.substring (0, view.lastIndexOf ("."));
      if (!module.equals (moduleName)) continue;

      for (String span : entitySpanFields.get (view)) {
        if (!view.equals (selectedOutputView) || !span.equals (selectedGroupOn)) {
          // Store original view name, ie. no module prefix if view is alias, so it can
          // be used to match with what returned by runtime. We limit to the module
          // of the selected output view, so the alias has to be unique.
          String origView = outputViewInfo.get (view).getOriginalViewName ();
          ret.add (String.format ("%s%s%s", origView, PDConstants.VIEW_SPAN_SEPARATOR, span)); //$NON-NLS-1$
        }
      }
    }
    return ret;
  }

  /**
   * Create the list of allowable options for snippets. This list includes "Default_Snippet"and fields of the selected
   * output view, excluding Group On and "Entity to consider type only" fields.
   */
  private ArrayList<String> getSnippetOptions ()
  {
    String selectedOutputView = outputViewCombo.getValue ();
    String selectedGroupOn = contextCombo.getValue ();
    String[] selectedEntities = entityList.getList ().getSelection ();
    List<String> selectedEntityList = Arrays.asList (selectedEntities);

    ArrayList<String> snippets = new ArrayList<String> ();
    if (entitySpanFields != null && entitySpanFields.get (selectedOutputView) != null)
      snippets.addAll (entitySpanFields.get (selectedOutputView));

    for (Iterator<String> iter = snippets.iterator (); iter.hasNext ();) {
      String field = iter.next ();
      if (field.equals (selectedGroupOn)
        || selectedEntityList.contains (selectedOutputView + PDConstants.VIEW_SPAN_SEPARATOR + field)) iter.remove ();
    }

    snippets.add (PDConstants.PD_DEFAULT_SNIPPET);
    return snippets;
  }

  /**
   * @param outputview
   */
  public void setOutputView (String outputview)
  {
    if (outputview.isEmpty ()) return;

    outputViewCombo.setValue (outputview);
    updateOutputViewRelatedProps ();
  }

  /**
   * @param props
   */
  public void setValuesFromProperties (Properties props)
  {
    // we set this one step before, this way the options for the fields that
    // depend on this get generated
    String outputViewName = getFullOutputViewameFromProperties (properties);
    // -- read props wanted --
    String context = props.getProperty (Messages.GROUP_BY_FIELD_NAME_PROP, "");
    String entities = props.getProperty (Messages.ENTITY_FIELD_NAMES_PROP, "");
    String snippet = props.getProperty (Messages.SNIPPET_FIELD_NAME_PROP, "");
    String language = props.getProperty (PDConstants.PD_LANGUAGE_PROP, "");
    String inputCollection = props.getProperty (InternalMessages.DOCUMENT_COLLECTION_DIR_PROP, "");

    if (!outputViewName.isEmpty ()) {
      outputViewCombo.setValue (outputViewName);
      if (!context.isEmpty ()) contextCombo.setValue (context);
      if (!entities.isEmpty ()) entityList.setValue (entities);
      if (!snippet.isEmpty ()) snippetCombo.setValue (snippet);
    }
    if (!language.isEmpty ()) langCombo.setValue (language);
    // else
    // langCombo.setValue (LangCode.DEFAULT_LANG_CODE.toString ());
    if (!inputCollection.isEmpty ())
      this.inputCollection.setFileDirValue (ProjectPreferencesUtil.getPath (inputCollection), true);

  }

  /**
   * Get output view name stored in the properties object and qualify it with module name prefix if necessary (in case
   * view is an alias.)
   * 
   * @param props
   * @return
   */
  private String getFullOutputViewameFromProperties (Properties props)
  {
    String outputViewName = props.getProperty (Messages.AQL_VIEW_NAME_PROP, "");
    if (!outputViewName.contains (".")) {
      String module = props.getProperty (Messages.AQL_MODULE_NAME_PROP, "");
      if (!module.equals ("")) outputViewName = module + "." + outputViewName;
    }
    return outputViewName;
  }

  public void addPropertyChangeListener (PropertyChangeListener listener)
  {
    propertyChangeSupport.addPropertyChangeListener (listener);
    outputViewCombo.addPropertyChangeListener (listener);
    contextCombo.addPropertyChangeListener (listener);
    entityList.addPropertyChangeListener (listener);
    snippetCombo.addPropertyChangeListener (listener);
    langCombo.addPropertyChangeListener (listener);
    cbDelimiter.addPropertyChangeListener (listener);
  }

  public void removePropertyChangeListener (PropertyChangeListener listener)
  {
    propertyChangeSupport.removePropertyChangeListener (listener);
    outputViewCombo.removePropertyChangeListener (listener);
    contextCombo.removePropertyChangeListener (listener);
    entityList.removePropertyChangeListener (listener);
    snippetCombo.removePropertyChangeListener (listener);
    langCombo.removePropertyChangeListener (listener);
    cbDelimiter.removePropertyChangeListener (listener);
  }

  public void addModifyListener (ModifyListener listener)
  {
    inputCollection.addModifyListenerForFileDirTextField (listener);
//    cbDelimiter.addModifyListener (listener);
    txtCustomDelimiter.addModifyListener (listener);
  }

  public void removeModifyListener (ModifyListener listener)
  {
    inputCollection.removeModifyListenerForFileDirTextField (listener);
//    cbDelimiter.removeModifyListener (listener);
    txtCustomDelimiter.removeModifyListener (listener);
  }

  /**
   * runnable that process the current view and build a system-t object from this project from which gets the fields
   * related to it
   */
  private void updateProjectLocationAndRelatedProps ()
  {
    String projectName = projectBrowser.getProject ();
    IProject project = ProjectUtils.getProject (projectName);
    String projectRoot = project.getLocation ().toString ();

    AQLUtils.createLibraryForProject (projectName);

    setSelectedModules (projectName);

    String oldProjectName = properties.getProperty (PDConstants.PD_PROJECT_NAME_PROP);
    properties.setProperty (PDConstants.PD_PROJECT_NAME_PROP, projectName);

    File baseDir = new File (projectRoot, PDConstants.PATTERN_DISCOVERY_TEMP_DIR_NAME);

    if (!baseDir.exists ()) {
      baseDir.mkdir ();
    }

    properties.setProperty (Messages.FILE_ROOT_DIR_PROP, baseDir.getAbsolutePath () + PDConstants.FILE_SEPARATOR);

    refreshOutputViewsAndSpanFields (projectBrowser.getProject ());

    Set<String> outputviewList = outputViewInfo.keySet ();

    if (outputviewList != null) outputViewCombo.setOptions (new ArrayList<String> (outputviewList));

    propertyChangeSupport.firePropertyChange (PDConstants.PD_PROJECT_NAME_PROP, oldProjectName, projectName);

    // If the new project doesn't have saved outputview, set with the first view.
    if (outputviewList != null && outputviewList.size () > 0) {
      String newOutputView = getFullOutputViewameFromProperties (properties);

      if (newOutputView.equals ("") || !outputviewList.contains (newOutputView))
        newOutputView = (String) outputviewList.toArray ()[0];

      setOutputView (newOutputView);
      propertyChangeSupport.firePropertyChange (Messages.AQL_VIEW_NAME_PROP, null, newOutputView);
    }
  }

  private void setSelectedModules (String projectName)
  {
    String selectedModules = "";

    if (ProjectUtils.isModularProject (projectName)) {
      IProject proj = ProjectUtils.getProject (projectName);
      String[] modules = ProjectUtils.getModules (proj);

      // select all modules
      if (modules != null && modules.length > 0) {
        for (String module : modules)
          selectedModules += module + Constants.DATAPATH_SEPARATOR;
      }
    }
    else {
      selectedModules = "genericModule"; //$NON-NLS-1$
    }

    properties.setProperty (IRunConfigConstants.SELECTED_MODULES, selectedModules);
  }

  /**
   * reset the basic properties in the user interface
   */
  private void resetUI ()
  {
    entitySpanFields.clear ();
    resetBasicPropertiesChoices (new ArrayList<String> ());
    projectBrowser.setProject ("");
    inputCollection.clearFileDirValue ();
  }

  /**
   * reset the options for the basic property fields
   */
  private void resetBasicPropertiesChoices (ArrayList<String> fields)
  {
    // ArrayList<String> fields = getSpanFields();
    contextCombo.setOptions (fields);
    entityList.setOptions (getEntityOptions ());
    snippetCombo.setOptions (getSnippetOptions ());
    snippetCombo.setValue (PDConstants.PD_DEFAULT_SNIPPET);
  }

  /**
   * Set the UI with values from properties.
   */
  public void setUI2Properties_noNotify (Properties props)
  {
    projectBrowser.setProject (props.getProperty (PDConstants.PD_PROJECT_NAME_PROP, ""), false);
    outputViewCombo.setValue (getFullOutputViewameFromProperties (props), false);
    contextCombo.setValue (props.getProperty (Messages.GROUP_BY_FIELD_NAME_PROP, ""), false);
    snippetCombo.setValue (props.getProperty (Messages.SNIPPET_FIELD_NAME_PROP, ""), false);
    langCombo.setValue (props.getProperty (PDConstants.PD_LANGUAGE_PROP, ""), false);
    String rawPath = props.getProperty (InternalMessages.DOCUMENT_COLLECTION_DIR_PROP, "");
    String path = ProjectPreferencesUtil.getPath (rawPath);
    inputCollection.setFileDirValue (path, true);

    // If this is a CSV file, enable the CSV fields
    // and fill out with the saved values.
    if (rawPath.endsWith (Constants.CSV_EXTENSION) &&
        ProjectPreferencesUtil.isExistingFile (rawPath)) {

      lbDelimiter.setEnabled (true);
      cbDelimiter.setEnabled (true);

      String delim = props.getProperty (IRunConfigConstants.DELIMITER, ""); //$NON-NLS-1$
      if (delim.isEmpty () == false) {
        cbDelimiter.setValue (delim);
        if (delim.equals (cbDelimiter.getValue ()) == false) {  // if it is NOT a common delimiter
          cbDelimiter.setValue (Constants.CUSTOM);
          txtCustomDelimiter.setEnabled (true);
          txtCustomDelimiter.setText (delim);
        }
      }
      else {
        cbDelimiter.setValue (Constants.COMMA);
      }
    }
    this.layout ();
  }

  public String getSelectedOutputView ()
  {
    if (outputViewCombo != null)
      return outputViewCombo.getValue ();
    else
      return null;
  }

  // ======================
  // === static methods ===
  // ======================
  /**
   * @return
   */
  public static ArrayList<String> getLanguages ()
  {
    ArrayList<String> ret = new ArrayList<String> ();
    for (LangCode lang : LangCode.values ()) {
      ret.add (lang.toString ());
    }
    return ret;
  }

  public ArrayList<String> getFields ()
  {
    return fields;
  }

  class ViewInfo
  {
    String viewName;
    String originalViewName;
    String moduleName;

    public ViewInfo (String viewName, String originalViewName, String moduleName)
    {
      super ();
      this.viewName = viewName;
      this.originalViewName = originalViewName;
      this.moduleName = moduleName;
    }

    public String getViewName ()
    {
      return viewName;
    }

    public String getOriginalViewName ()
    {
      return originalViewName;
    }

    public String getModuleName ()
    {
      return moduleName;
    }
  }
}
