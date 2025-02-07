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
package com.ibm.biginsights.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

public class MigratableProjectsDialog extends SelectionDialog {

	private IProject _project;
	private Combo _cbProjects;
	
	public MigratableProjectsDialog(Shell parent) {
		super(parent);	
		this.setTitle(Messages.MIGRATABLEPROJECTSDIALOG_TITLE);
	}
	
	public IProject getSelectedProject() {
		return _project;
	}
	
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        //TODO: set help
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IIDEHelpContextIds.CONTAINER_SELECTION_DIALOG);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite dlgArea = (Composite) super.createDialogArea(parent);

	    Label lblProject = new Label(dlgArea, SWT.NONE);        
        GridData gdLabel = new GridData(GridData.BEGINNING);
        lblProject.setLayoutData(gdLabel);        
        lblProject.setText(Messages.MIGRATABLEPROJECTSDIALOG_DESC);

        // add project selection
		_cbProjects = new Combo(dlgArea, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        GridData gdProjects = new GridData(GridData.FILL_HORIZONTAL);
        gdProjects.grabExcessHorizontalSpace = true;        
        _cbProjects.setLayoutData(gdProjects);
        _cbProjects.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {	
				onProjectSelectionChanged();
			}
		});

        Label lblDesc = new Label(dlgArea, SWT.WRAP);        
        GridData gdDesc = new GridData(GridData.BEGINNING);
        lblDesc.setLayoutData(gdDesc);   
        
        // retrieve all projects from workspace and only add the projects that have the BI nature, need require migration and are open
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        if (projects !=null && projects.length>0) {
        	for (IProject project:projects) {
        		if (project.isOpen() && MigrateProject.isMigrationRequired(project).isMigrationRequired)
        			_cbProjects.add (project.getName());	            
	        _cbProjects.select(0);
        	}
        }
           
    	if (_cbProjects.getItemCount()==0)
    		lblDesc.setText(Messages.MIGRATABLEPROJECTSDIALOG_MESSAGE);        
        
        updateButtonStatus();
        return dlgArea;
    }
    
    protected void onProjectSelectionChanged() {
		updateSelectedLocation();		
		updateButtonStatus();
    }
    
    public void updateSelectedLocation() {
    	if (_cbProjects.getSelectionIndex()>-1) {
    		String projectName = _cbProjects.getItem(_cbProjects.getSelectionIndex());    		
    		this._project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
    	}
    }
    
    protected void createButtonsForButtonBar(Composite parent) {
    	super.createButtonsForButtonBar(parent);
    	updateButtonStatus();
    }
    
    protected void updateButtonStatus() {
    	boolean enabled = _cbProjects.getItemCount()>0 && this._project!=null;
    	if (getOkButton()!=null)
    		getOkButton().setEnabled(enabled);
    }
    
    protected void okPressed() {
    	List<IProject> result = new ArrayList<IProject>();        
        if (_project != null) {
        	result.add(_project);
		}
        setResult(result);
        super.okPressed();
    }
	
}
