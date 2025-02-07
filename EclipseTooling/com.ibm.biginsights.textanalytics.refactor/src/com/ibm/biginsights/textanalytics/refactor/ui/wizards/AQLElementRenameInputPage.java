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

package com.ibm.biginsights.textanalytics.refactor.ui.wizards;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameInfo;
import com.ibm.biginsights.textanalytics.refactor.ui.AQLElementRenameUITexts;


public class AQLElementRenameInputPage extends UserInputWizardPage {



  private static final String UPDATE_PROJECT = "UPDATE_PROJECT"; //$NON-NLS-1$
  private static final String UPDATE_WORKSPACE  = "ALL_PROJECTS"; //$NON-NLS-1$

  private final AQLElementRenameInfo info;
  
  private IDialogSettings dialogSettings;
  private Text txtNewName;
  //private Button cbUpdateProject; // Useful if we want to provide option for single project update
  private Button cbUpdateWorkspace;


  public AQLElementRenameInputPage( final AQLElementRenameInfo info ) {
    super( AQLElementRenameInputPage.class.getName() );
    this.info = info;
    initDialogSettings();
  }

// This method is used to create the Rename input dialog box
  public void createControl( final Composite parent ) {
    Composite composite = createRootComposite( parent );
    setControl( composite );
    createLblNewName( composite );
    createTxtNewName( composite );
    //createCbUpdateBundle( composite );
    createCbAllProjects( composite );
    
    validate();
  }

  private Composite createRootComposite( final Composite parent ) {
    Composite result = new Composite( parent, SWT.NONE );
    GridLayout gridLayout = new GridLayout( 2, false );
    gridLayout.marginWidth = 10;
    gridLayout.marginHeight = 10;
    result.setLayout( gridLayout );
    initializeDialogUnits( result );
    Dialog.applyDialogFont( result );
    return result;
  }
  
  // Create and adds the new name label to dialog 
  private void createLblNewName( final Composite composite ) {
    Label lblNewName = new Label( composite, SWT.NONE );
    lblNewName.setText( AQLElementRenameUITexts.AQLElementRenameInputPage_lblNewName );
  }

  //Create and adds the new name text filed to dialog 
  private void createTxtNewName(Composite composite) {
    txtNewName = new Text( composite, SWT.BORDER );
    txtNewName.setText( info.getOldName() );
    txtNewName.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
    txtNewName.selectAll();
    txtNewName.addKeyListener( new KeyAdapter() {
      public void keyReleased( final KeyEvent e ) {
        info.setNewName( txtNewName.getText() );
        validate();
      }
    } );
  }

  //Useful if we want to provide option for single project update
/*
  private void createCbUpdateProject( final Composite composite ) {
    String texts = AQLElementRenameUITexts.AQLElementRenameInputPage_cbUpdateProject;
    cbUpdateProject = createCheckbox( composite, texts );
    cbUpdateProject.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( final SelectionEvent event ) {
        boolean selected = cbUpdateProject.getSelection();
        dialogSettings.put( UPDATE_PROJECT, selected );
        info.setUpdateProject( selected );
      }
    } );
    initUpdateProjectOption();
  }
 */ 
  // It create and add the check box for all workspace resource update to dialog
  private void createCbAllProjects( final Composite composite ) {
    String text = AQLElementRenameUITexts.AQLElementRenameInputPage_cbAllProjects;
    cbUpdateWorkspace = createCheckbox( composite, text );
    cbUpdateWorkspace.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( final SelectionEvent event ) {
        boolean selected = cbUpdateWorkspace.getSelection();
        dialogSettings.put( UPDATE_WORKSPACE, selected );
        info.setUpdateWorkspace( selected );
        // We enforce the preview for re-factorings that span the entire workspace
        getRefactoringWizard().setForcePreviewReview( selected );
      }
    } );
    initAllProjectsOption();
  }

  private Button createCheckbox( final Composite composite, 
                                 final String text ) {
    Button result = new Button( composite, SWT.CHECK );
    result.setText( text );
    
    GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
    gridData.horizontalSpan = 2;
    result.setLayoutData( gridData );
    
    return result;
  }
  
  private void initDialogSettings() {
	    AbstractUIPlugin plugin =
	      (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	    IDialogSettings workbenchSettings = plugin.getDialogSettings();
	    dialogSettings = workbenchSettings.getSection("AQLElementRenameInputPage"); //$NON-NLS-1$
	    if( dialogSettings == null ) {
	      dialogSettings = workbenchSettings.addNewSection("AQLElementRenameInputPage" );
	      // init default values
	      dialogSettings.put( UPDATE_PROJECT, true );
	      dialogSettings.put( UPDATE_WORKSPACE, true );
	    }
	  }
  
  private void validate() {
    String txt = txtNewName.getText();
    setPageComplete( txt.length() > 0 && !txt.equals( info.getOldName() ) );
  }

  // Useful if we want to provide option for single project update
  /*
  private void initUpdateProjectOption() {
    boolean updateRefs = dialogSettings.getBoolean( UPDATE_PROJECT );
    cbUpdateProject.setSelection( updateRefs );
    info.setUpdateProject( updateRefs );
  }
  */
  private void initAllProjectsOption() {
    boolean allProjects = dialogSettings.getBoolean( UPDATE_WORKSPACE );
    cbUpdateWorkspace.setSelection( allProjects );
    info.setUpdateWorkspace( allProjects );
  }
}
