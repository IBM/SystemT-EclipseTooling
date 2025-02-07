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
package com.ibm.biginsights.project.prefs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.BigInsightsLibraryContainerInitializer;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.MigrateProject;
import com.ibm.biginsights.project.MigrateProject.MigrationTestResult;
import com.ibm.biginsights.project.ProjectSupport;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;
import com.ibm.biginsights.project.wizard.AddBINatureWizard;

public class ProjectProperties extends PropertyPage implements
		IWorkbenchPropertyPage {

	private IProject _project;
	private MigrationTestResult _migrationTestResult;
	private Combo _cbLibrary;	
	private String _containerName;
	
	protected String projectName;
	
	public ProjectProperties() {
		super();
		_project = BIProjectPreferencesUtil.getSelectedProject();  	  	
  	  	// check if migration is required which means we show a different UI
		_migrationTestResult = MigrateProject.isMigrationRequired(_project);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());		
		
		if (_migrationTestResult.isMigrationRequired) {
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.numColumns = 1;
			composite.setLayout(layout);
			
			Label lblMigrate = new Label(composite, SWT.WRAP);
			lblMigrate.setFont(composite.getFont());
//			lblMigrate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			lblMigrate.setText(Messages.PROJECTPROPERTIES_MIGRATE_DESC);
			
			final Button btnMigrate = new Button(composite, SWT.PUSH); 	
			btnMigrate.setText(Messages.PROJECTPROPERTIES_BUTTON_LABEL);
			btnMigrate.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent e) {
	        	  Shell shell =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	        	  IProject project = BIProjectPreferencesUtil.getSelectedProject();
	        	  AddBINatureWizard wizard = new AddBINatureWizard(project, _migrationTestResult.projectVersion, _migrationTestResult.bigInsightsVersion);	    
	        	  WizardDialog dialog = new WizardDialog(shell, wizard);
	        	  dialog.create();
	        	  if (dialog.open()==Window.OK) {	        		  	        		 
	        		  // check if it was successful and if so, tell the user to reopen the properties
	        		  _migrationTestResult = MigrateProject.isMigrationRequired(_project);	        		  
	        		  if (!_migrationTestResult.isMigrationRequired) {
	        			  btnMigrate.setEnabled(false); // disable migrate button
	        			  MessageDialog.openInformation(shell, Messages.PROJECTPROPERTIES_MSG_SUCCESS_TITLE, Messages.PROJECTPROPERTIES_MSG_SUCCESS_DESC);
	        		  }
	        	  }
				}
			});
			btnMigrate.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
		      public void getName (AccessibleEvent e) {
		        e.result = Messages.PROJECTPROPERTIES_MIGRATE_DESC +"  " +Messages.PROJECTPROPERTIES_BUTTON_LABEL;
		      }
		    });
			this.noDefaultAndApplyButton();

		}
		else {			 
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.numColumns = 2;
			composite.setLayout(layout);
			
			Label lblVersion = new Label(composite, SWT.NONE);
			lblVersion.setFont(composite.getFont());
			lblVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			lblVersion.setText(Messages.PROJECTPROPERTIES_LOCATION_LABEL);
			
			_cbLibrary = new Combo(composite, SWT.READ_ONLY);
			_cbLibrary.setFont(composite.getFont());		
			_cbLibrary.setItems(BigInsightsLibraryContainerInitializer.getInstance().getSupportedContainers());	
			
			GridData gdCombo = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1);
			gdCombo.widthHint = SWT.DEFAULT;
			gdCombo.widthHint = 300;
			_cbLibrary.setLayoutData(gdCombo);
					
			_cbLibrary.addModifyListener(new ModifyListener() {			
				
				@Override
				public void modifyText(ModifyEvent e) {
					changeLibrarySelection();				
				}			
			});
			
			this.noDefaultAndApplyButton();
			this.setInitialSelection();
		}
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.project.help.project_properties_biginsights"); //$NON-NLS-1$
		return composite;
	}

	protected void changeLibrarySelection() {				
		if (_cbLibrary!=null) { 		
			_containerName = _cbLibrary.getItem(_cbLibrary.getSelectionIndex());			
		}						
	}	
	
	public boolean performOk() {

		if (_containerName!=null && !_containerName.isEmpty()) {  // after migration, the UI has no combobox, but classpath is set already, so can close without updating the classpath
	        IClasspathEntry bigInsightsLibraries = BigInsightsLibraryContainerInitializer.getInstance().getClasspathEntryByName( _containerName);
	        try {
				ProjectSupport.addBigInsightsLibraryToProjectClasspath(_project, bigInsightsLibraries);
				return true;
			} catch (CoreException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				return false;
			}
		}
		else
			return true;
	}	
	
	public void setInitialSelection() {
		// get BI library version from classpath of project 
		String version = BIProjectPreferencesUtil.getBigInsightsLibrariesVersion(_project);		
		String name = BigInsightsLibraryContainerInitializer.getInstance().getFullContainerNameByVersion(version);
		int selected = 0;
		for (int i=0; i<_cbLibrary.getItemCount(); i++) {
			String libName = _cbLibrary.getItem(i);
			if (libName.equals(name))	{
				selected = i;
				break;
			}
		}

		if (_cbLibrary !=null)
			_cbLibrary.select(selected);
	}
	
}
