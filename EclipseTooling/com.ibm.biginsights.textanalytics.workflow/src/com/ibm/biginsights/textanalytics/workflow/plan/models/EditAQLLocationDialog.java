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
package com.ibm.biginsights.textanalytics.workflow.plan.models;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;

/**
 * Dialog that allows to determine the AQL file of a view in a modular project.
 */
public class EditAQLLocationDialog extends Dialog
{


 
	private final String title = Messages.create_aql_statement_dialog_title;

  private final String module_name_label = Messages.create_aql_statement_module_name;
  private final String aql_file_name_label = Messages.create_aql_statement_aqlfile_name;

  private String module_name = "";      // $NON-NLS-1$
  private String aql_file_name = "";    // $NON-NLS-1$
  private String projectName;

  protected Button okButton;

  private Combo module_name_combo;
  private Combo aql_file_combo;

  private Text errorMessageText;
  private String errorMessage;

  public EditAQLLocationDialog (Shell parentShell, String projectName)
  {
    super (parentShell);
    this.projectName = ActionPlanView.projectName;
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected void buttonPressed (int buttonId)
  {
    if (buttonId == IDialogConstants.OK_ID) {
      module_name = module_name_combo.getText ();
      aql_file_name = aql_file_combo.getText ();
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
  }

  /*
   * (non-Javadoc) Method declared on Dialog.
   */
  protected Control createDialogArea (Composite parent)
  {
    Composite composite = (Composite) super.createDialogArea (parent);

    Composite data_section = new Composite (composite, SWT.NONE);
    data_section.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    data_section.setLayout (new GridLayout (2, false));

    CLabel module_label = new CLabel (data_section, SWT.RIGHT);
    module_label.setText (module_name_label);
    module_name_combo = new Combo (data_section, SWT.READ_ONLY | SWT.BORDER);
    module_name_combo.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    populateModuleNameCombo ();

    CLabel file_label = new CLabel (data_section, SWT.RIGHT);
    file_label.setText (aql_file_name_label);
    aql_file_combo = new Combo (data_section, SWT.READ_ONLY | SWT.BORDER);
    aql_file_combo.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    populateFileNameCombo ();

    addListenerToModularControls ();

    errorMessageText = new Text (composite, SWT.READ_ONLY | SWT.WRAP);
    errorMessageText.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    errorMessageText.setBackground (errorMessageText.getDisplay ().getSystemColor (SWT.COLOR_WIDGET_BACKGROUND));
    // Set the error message text
    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
    setErrorMessage (errorMessage);

    applyDialogFont (composite);
    return composite;
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
      }
    });
  }

  private void populateModuleNameCombo ()
  {
    module_name_combo.removeAll ();
    module_name = "";
    if (!StringUtils.isEmpty (projectName)) {
      String[] moduleNames = ProjectUtils.getModules (ProjectUtils.getProject (projectName));
      if (moduleNames != null) {
        module_name_combo.setItems (moduleNames);
      }
    }
  }

  private void populateFileNameCombo ()
  {
    aql_file_combo.removeAll ();
    aql_file_name = "";
    if (!StringUtils.isEmpty (module_name)) {
      String[] aqlFiles = ProjectUtils.getAqlFilesOfModule (projectName, module_name);
      aql_file_combo.setItems (aqlFiles);
    }
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

      // Disable the error message text control if there is no error, or no error text (empty or whitespace only).
      // Hide it also to avoid color change.
      // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
      boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces (errorMessage)).length () > 0;
      errorMessageText.setEnabled (hasError);
      errorMessageText.setVisible (hasError);
      errorMessageText.getParent ().update ();

      // Access the ok button by id, in case clients have overridden button creation.
      // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
      Control button = getButton (IDialogConstants.OK_ID);
      if (button != null) {
        button.setEnabled (errorMessage == null);
      }
    }
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

  public IFile getAqlFile ()
  {
    if ( !StringUtils.isEmpty(projectName) && !StringUtils.isEmpty(module_name) && !StringUtils.isEmpty(aql_file_name) )
      return ProjectUtils.getAqlFile (projectName, module_name, aql_file_name);
    else
      return null;
  }
}
