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
package com.ibm.biginsights.textanalytics.workflow.plan;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlBasicsTypes;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlConceptTypes;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlFinalsTypes;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlRefinementTypes;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlTypes;
import com.ibm.biginsights.textanalytics.workflow.util.Styles;

/**
 * Dialog that allows to define an AQL element. Based in the AQL type provided a set of options will be provided that
 * will allow the user to define the wanted element and insert a basic template for it.
 * 
 * 
 */
public class CreateAQLConceptDialog extends Dialog
{


 
	private static final String aql_extension = ".aql";
  private final String title = Messages.create_aql_statement_dialog_title;

  private final String view_name_label = Messages.create_aql_statement_dialog_name;
  private final String dict_name_label = Messages.create_aql_statement_dialog_name_dict;
  private final String func_name_label = Messages.create_aql_statement_dialog_name_func;
  private final String tabl_name_label = Messages.create_aql_statement_dialog_name_table;

  private final String module_name_label = Messages.create_aql_statement_module_name;
  private final String aql_file_name_label = Messages.create_aql_statement_aqlfile_name;
  private final String stmt_type_label = Messages.create_aql_statement_dialog_type;

  private String view_name = "";        // $NON-NLS-1$

  private String module_name = "";      // $NON-NLS-1$
  private String aql_file_name = "";    // $NON-NLS-1$
  private AqlGroup parent;
  private AqlGroupType aqltype;     // bf, cg, fc, finals
  private AqlTypes stmt_type;       // dictionary, regex, par of speech...
  private boolean doOutput;
  private boolean doExport;

  private CreateAQLConceptDialogInputValidator validator;
  protected Button okButton;

  private Composite data_section;
  private Label    lblNewLabel;

  private Text  view_name_txt;
  private Combo module_name_combo;
  private Combo aql_file_combo;
  private Combo stmt_type_combo;

  private Text errorMessageText;
  private String errorMessage;

  private Button outputBtn;
  private Button exportBtn;

  private boolean isModular;

  public CreateAQLConceptDialog (Shell parentShell, AqlGroup parent)
  {
    super (parentShell);
    validator = new CreateAQLConceptDialogInputValidator ();
    this.parent = parent;
    this.aqltype = parent.getAqlType ();
    setDoOutput (false);
    setDoExport (false);
    isModular = ProjectUtils.isModularProject (ActionPlanView.projectName);
  }

  public CreateAQLConceptDialog (Shell parentShell, AqlFolderNode parent)
  {
    this (parentShell, (AqlGroup)parent.getParent ());
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected void buttonPressed (int buttonId)
  {
    if (buttonId == IDialogConstants.OK_ID) {
      view_name = view_name_txt.getText ();
      stmt_type = Enumerations.getType (stmt_type_combo.getText ());
      setDoOutput (outputBtn.getSelection ());

      if (isModular)
        setDoExport (exportBtn.getSelection ());
    }
    else {
      view_name = null;
    }
    super.buttonPressed (buttonId);
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets .Shell)
   */
  protected void configureShell (Shell shell)
  {
    super.configureShell (shell);
    if (title != null) {
      shell.setText (title);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse .swt.widgets.Composite)
   */
  protected void createButtonsForButtonBar (Composite parent)
  {
    // create OK and Cancel buttons by default
    okButton = createButton (parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton (parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    // do this here because setting the text will set enablement on the ok
    // button
    view_name_txt.setFocus ();
    if (view_name != null) {
      view_name_txt.setText (view_name);
      view_name_txt.selectAll ();
    }
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected Control createDialogArea (Composite parent)
  {
    Composite composite = (Composite) super.createDialogArea (parent);

    data_section = new Composite (composite, SWT.NONE);
    data_section.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    data_section.setLayout (new GridLayout (2, false));

    lblNewLabel = new Label (data_section, SWT.NONE);
    lblNewLabel.setText (view_name_label);

    view_name_txt = new Text (data_section, getInputTextStyle ());
    view_name_txt.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

    if (isModular) {

      Label module_label = new Label (data_section, SWT.RIGHT);
      module_label.setText (module_name_label);
      module_name_combo = new Combo (data_section, SWT.READ_ONLY | SWT.BORDER);
      module_name_combo.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
      populateModuleNameCombo ();

      Label file_label = new Label (data_section, SWT.RIGHT);
      file_label.setText (aql_file_name_label);
      aql_file_combo = new Combo (data_section, SWT.BORDER);
      aql_file_combo.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
      populateFileNameCombo ();
    }

    Label type_label = new Label (data_section, SWT.NONE);
    type_label.setText (stmt_type_label);

    stmt_type_combo = new Combo (data_section, SWT.READ_ONLY | SWT.BORDER);
    stmt_type_combo.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));

    loadTypeOptions ();

    view_name_txt.addModifyListener (new ModifyListener () {
      public void modifyText (ModifyEvent e)
      {
        validateInput ();
      }
    });

    new Label (data_section, SWT.NONE);
    setOutputBtn (new Button (data_section, SWT.CHECK));
    getOutputBtn ().setText (Messages.create_aql_statement_dialog_output_message);

    if (isModular) {
      new Label (data_section, SWT.NONE);
      setExportBtn (new Button (data_section, SWT.CHECK));
      getExportBtn ().setText (Messages.create_aql_statement_dialog_export_message);

      if (aqltype == AqlGroupType.FINALS)
        getExportBtn ().setEnabled (false);

      addListenerToModularControls();
    }

    errorMessageText = new Text (composite, SWT.READ_ONLY | SWT.WRAP);
    errorMessageText.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    errorMessageText.setForeground (Styles.ATTENTION_RED);
    errorMessageText.setBackground (errorMessageText.getDisplay ().getSystemColor (SWT.COLOR_WIDGET_BACKGROUND));
    // Set the error message text
    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
    setErrorMessage (errorMessage);

    applyDialogFont (composite);
    return composite;
  }

  @Override
  protected boolean isResizable()
  {
    return true;
  }

  private void addListenerToModularControls ()
  {
    module_name_combo.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText (ModifyEvent e)
      {
        module_name = module_name_combo.getText ();
        populateFileNameCombo ();
      }
    });

    aql_file_combo.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText (ModifyEvent e)
      {
        aql_file_name = aql_file_combo.getText ();
        if (!aql_file_name.endsWith (aql_extension))
          aql_file_name += aql_extension;

        validateInput ();
      }
    });

    stmt_type_combo.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText (ModifyEvent e)
      {
        String aqlType = stmt_type_combo.getText ();
        String dictType = Enumerations.getString (AqlFinalsTypes.EXPORTDICTIONARY.toString ());
        String viewType = Enumerations.getString (AqlFinalsTypes.EXPORTVIEW.toString ());
        String funcType = Enumerations.getString (AqlFinalsTypes.EXPORTFUNCTION.toString ());
        String tablType = Enumerations.getString (AqlFinalsTypes.EXPORTTABLE.toString ());

        if (aqlType.equals (dictType))
          lblNewLabel.setText (dict_name_label);
        else if (aqlType.equals (viewType))
          lblNewLabel.setText (view_name_label);
        else if (aqlType.equals (funcType))
          lblNewLabel.setText (func_name_label);
        else if (aqlType.equals (tablType))
          lblNewLabel.setText (tabl_name_label);

        if (aqlType.equals (dictType) || aqlType.equals (funcType) || aqlType.equals (tablType))
          getOutputBtn ().setEnabled (false);
        else
          getOutputBtn ().setEnabled (true);

        data_section.layout ();
        
        validateInput ();
      }
    });

  }

  private void populateModuleNameCombo ()
  {
    if (!StringUtils.isEmpty (ActionPlanView.projectName)) {
      String[] moduleNames = ProjectUtils.getAllModules (ProjectUtils.getProject (ActionPlanView.projectName));
      if (moduleNames != null) {
        module_name_combo.setItems (moduleNames);

        // Depending on where the dialog is launched, pre-select the module.
        LabelNode rootLabel = parent.getRootLabel ();
        String preferredModuleName = rootLabel.getGeneratedModuleName (aqltype);
        module_name_combo.setText (preferredModuleName);

        module_name = module_name_combo.getText ();
      }
    }
  }

  private void populateFileNameCombo ()
  {
    aql_file_combo.removeAll ();
    if (!StringUtils.isEmpty (module_name)) {
      String[] aqlFiles = ProjectUtils.getAqlFilesOfModule (ActionPlanView.projectName, module_name);
      aql_file_combo.setItems (aqlFiles);
    }
  }

  private void loadTypeOptions ()
  {
    stmt_type_combo.removeAll ();

    switch (aqltype) {
      case BASIC:
        for (AqlBasicsTypes type : AqlBasicsTypes.values ()) {
          stmt_type_combo.add (Enumerations.getString (type.toString ()));
        }
        break;

      case CONCEPT:
        for (AqlConceptTypes type : AqlConceptTypes.values ()) {
          stmt_type_combo.add (Enumerations.getString (type.toString ()));
        }
        break;

      case REFINEMENT:
        for (AqlRefinementTypes type : AqlRefinementTypes.values ()) {
          stmt_type_combo.add (Enumerations.getString (type.toString ()));
        }
        break;

      default:  // FINALS
        for (AqlFinalsTypes type : AqlFinalsTypes.values ()) {
          stmt_type_combo.add (Enumerations.getString (type.toString ()));
        }
        break;
    }

    stmt_type_combo.select (0);
  }

  public String getName ()
  {
    return view_name;
  }

  public AqlTypes getType ()
  {
    return stmt_type;
  }

  protected void validateInput ()
  {
    String errorMessage = null;
    if (validator != null) {
      errorMessage = validator.isValid (view_name_txt.getText ());

      // for 2.0, also validate if module name and aql file name are empty
      if (errorMessage == null &&
          ProjectUtils.isModularProject (ActionPlanView.projectName)) {

        errorMessage = validator.isValidComboInput (module_name_combo);

        if (errorMessage == null)
          errorMessage = validator.isValidComboInput (aql_file_combo);
      }
    }

    setErrorMessage (errorMessage);
  }

  /**
   * Sets or clears the error message. If not <code>null</code>, the OK button is disabled.
   * 
   * @param errorMessage the error message, or <code>null</code> to clear
   * @since 3.0
   */
  public void setErrorMessage (String errorMessage)
  {
    this.errorMessage = errorMessage;
    if (errorMessageText != null && !errorMessageText.isDisposed ()) {
      errorMessageText.setText (errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
      // Disable the error message text control if there is no error, or
      // no error text (empty or whitespace only). Hide it also to avoid
      // color change.
      // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
      boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces (errorMessage)).length () > 0;
      errorMessageText.setEnabled (hasError);
      errorMessageText.setVisible (hasError);
      errorMessageText.getParent ().update ();
      // Access the ok button by id, in case clients have overridden
      // button creation.
      // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
      Control button = getButton (IDialogConstants.OK_ID);
      if (button != null) {
        button.setEnabled (errorMessage == null);
      }
    }
  }

  protected int getInputTextStyle ()
  {
    return SWT.SINGLE | SWT.BORDER;
  }

  public void setOutputBtn (Button outputBtn)
  {
    this.outputBtn = outputBtn;
  }

  public Button getOutputBtn ()
  {
    return outputBtn;
  }

  public void setDoOutput (boolean doOutput)
  {
    this.doOutput = doOutput;
  }

  public boolean isDoOutput ()
  {
    return doOutput;
  }

  public void setExportBtn (Button exportBtn)
  {
    this.exportBtn = exportBtn;
  }

  public Button getExportBtn ()
  {
    return exportBtn;
  }

  public void setDoExport (boolean doExport)
  {
    this.doExport = doExport;
  }

  public boolean isDoExport ()
  {
    return doExport;
  }

  /**
   * @return the module_name
   */
  public String getModuleName ()
  {
    return module_name;
  }

  /**
   * @return the aql_file_name
   */
  public String getAqlFileName ()
  {
    return aql_file_name;
  }

  /**
   * @return the aqltype
   */
  public AqlGroupType getAqlGrouptype ()
  {
    return aqltype;
  }

  class CreateAQLConceptDialogInputValidator implements IInputValidator
  {

    @Override
    public String isValid (String newText)
    {
      if (newText == null || newText.isEmpty ())
        return Messages.create_aql_statement_dialog_validation_message;
      else
        return null;
    }

    public String isValidComboInput (Combo inputCombo)
    {
      // Verify module name
      if ( inputCombo == module_name_combo && !ProjectUtils.isValidName (inputCombo.getText ()) )
        return Messages.create_aql_statement_dialog_validation_module_message;

      // Verify AQL script name
      else if (inputCombo == aql_file_combo) {
        String aqlFileName = inputCombo.getText ();

        // Strip extension if it exists.
        if (aqlFileName.endsWith (".aql"))     //$NON-NLS-1$
          aqlFileName = aqlFileName.substring (0, aqlFileName.length () - 4);

        if (!ProjectUtils.isValidName (aqlFileName))
          return Messages.create_aql_statement_dialog_validation_aqlfile_message;
      }

      return null;
    }

  }

}
