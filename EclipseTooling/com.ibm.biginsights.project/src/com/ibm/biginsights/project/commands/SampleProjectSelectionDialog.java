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
package com.ibm.biginsights.project.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationSelectionDialog;
import com.ibm.biginsights.project.sampleProjects.SampleProjectsProvider;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class SampleProjectSelectionDialog extends LocationSelectionDialog {

	private JSONObject _project;
	private Combo _cbProjects;
	private Label lblDesc;	
	private String _projectSuffix;
	private String _projectType;
	private SampleProjectsProvider _projectsProvider;
	private HashMap<IBigInsightsLocation, JSONArray>locationProjectsMappingList;
	
	public SampleProjectSelectionDialog(Shell parent, IBigInsightsLocation initialLocation, JSONObject initialProject, String projectSuffix, String projectType) {
		super(parent, Messages.APPSELECTIONDIALOG_TITLE,
				projectType==null ? Messages.SAMPLEPROJECTSELECTIONDIALOG_DESC:Messages.bind(Messages.SAMPLEPROJECTSELECTIONDIALOG_DESC_TYPE, projectType), initialLocation);
		this._project = initialProject;		
		this._projectSuffix = projectSuffix;
		this._projectType = projectType;
		_projectsProvider = new SampleProjectsProvider();
		locationProjectsMappingList = new HashMap<IBigInsightsLocation, JSONArray>();
	}
	
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        //TODO: set help
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IIDEHelpContextIds.CONTAINER_SELECTION_DIALOG);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite dlgArea = (Composite) super.createDialogArea(parent);

	    Label lblProjects = new Label(dlgArea, SWT.NONE);        
        GridData gdLabel = new GridData(GridData.BEGINNING);
        gdLabel.horizontalSpan = 2;
        lblProjects.setLayoutData(gdLabel);        
        lblProjects.setText(Messages.SAMPLEPROJECTSELECTIONDIALOG_LABEL);

        // add project selection
		_cbProjects = new Combo(dlgArea, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        GridData gdApp = new GridData(GridData.FILL_HORIZONTAL);
        gdApp.grabExcessHorizontalSpace = true;        
        _cbProjects.setLayoutData(gdApp);
        _cbProjects.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {	
				updateSelectedProject();		
				updateButtonStatus();
			}
		});

        lblDesc = new Label(dlgArea, SWT.WRAP);        
        GridData gdDesc = new GridData(GridData.BEGINNING | GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        gdDesc.heightHint = 40;
        gdDesc.horizontalSpan = 2;	
        lblDesc.setLayoutData(gdDesc);   

        if (this.getSelectedLocation()!=null) {
        	// only populate list of projects when default selection is set
        	retrieveProjectsForLocation(this.getSelectedLocation()); 
        }        
                                   
        updateButtonStatus();
        return dlgArea;
    }
    
    protected void onLocationSelectionChanged() {
    	super.onLocationSelectionChanged();
    	if (_cbProjects!=null) { // check for cbApp to make sure the dialog is created already
	    	// also update the project selection
	    	IBigInsightsLocation selectedLoc = this.getSelectedLocation();
	    	if (selectedLoc!=null) { 
	    		retrieveProjectsForLocation(selectedLoc);
	    	}
    	}
    }
    
    private void retrieveProjectsForLocation(IBigInsightsLocation location) {
		_cbProjects.removeAll();
		JSONArray projects = _projectsProvider.getSampleProjects(location, this._projectSuffix);
		if (projects!=null) {
			ArrayList<String> projArray = new ArrayList<String>();
			locationProjectsMappingList.put(location, projects);		
			for (Object obj:projects) {
				JSONObject project = (JSONObject)obj;
				String projFile = (String)project.get("name"); //$NON-NLS-1$
				if (projFile.endsWith(this._projectSuffix))
					projArray.add(projFile);
			}
			if (!projArray.isEmpty()) {
				lblDesc.setText(""); //$NON-NLS-1$
				// add sorted list to combobox
				Collections.sort(projArray);
				for (String element:projArray) {
					_cbProjects.add(element);
				}
				// select first entry
				_cbProjects.select(0);
			}
			else {
				lblDesc.setText(this._projectType==null ? Messages.SAMPLEPROJECTSELECTIONDIALOG_WARNING_DESC:
					Messages.bind(Messages.SAMPLEPROJECTSELECTIONDIALOG_WARNING_DESC_TYPE, this._projectType));
			}
			updateButtonStatus();
		}
    }
    
    private void updateSelectedProject() {
    	if (_cbProjects.getSelectionIndex()>-1) {
    		String selectedProjName = _cbProjects.getItem(_cbProjects.getSelectionIndex()); 
    		JSONArray projects = locationProjectsMappingList.get(this.getSelectedLocation());
    		if (projects!=null && !projects.isEmpty()) {    
    			for (Object obj:projects) {    			
    				JSONObject project = (JSONObject)obj;
    				if (project.get("name").equals(selectedProjName)) { //$NON-NLS-1$
    					this._project = project;
    					break;
    				}
    			}
    		}    		
    	}
    }
    
    protected void createButtonsForButtonBar(Composite parent) {
    	super.createButtonsForButtonBar(parent);
    	updateButtonStatus();
    }
    
    protected void updateButtonStatus() {
    	super.updateButtonStatus();    	
    	boolean enabled = _cbProjects!=null && _cbProjects.getItemCount()>0 && this._project!=null;
    	if (getOkButton()!=null)
    		getOkButton().setEnabled(enabled);
    }
    
    // index 0: selected location
    // index 1: selected app
    protected void okPressed() {    	    	
    	ArrayList<Object> result = new ArrayList<Object>();   
    	if (this.getSelectedLocation()!=null)
    		result.add(this.getSelectedLocation());
        if (_project != null) {
        	result.add(_project);
		}
        setResult(result);
        // don't call super.okPressed because that will override the result object with just the location
    	setReturnCode(OK);
		close();
    }
	
}
