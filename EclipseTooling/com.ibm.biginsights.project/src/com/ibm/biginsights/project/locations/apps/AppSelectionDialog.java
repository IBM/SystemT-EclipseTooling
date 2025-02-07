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
package com.ibm.biginsights.project.locations.apps;

import java.util.ArrayList;
import java.util.Collection;
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
import com.ibm.biginsights.project.util.BIConnectionException;

public class AppSelectionDialog extends LocationSelectionDialog {

	private IBigInsightsApp _app;
	private Combo _cbApp;
	private Label lblDesc;
	private ApplicationProvider _appProvider;
	private HashMap<IBigInsightsLocation, Collection<IBigInsightsApp>>locationAppMappingList;
	private String _appStatusFilter = null;
	private String _errorMessage = null;
	private String _appName = null;	
	
	public AppSelectionDialog(Shell parent, String title, String description, String applicationName) {
		super(parent, title, description, null);
		this._appName = applicationName;		
		_appProvider = new ApplicationProvider();
		locationAppMappingList = new HashMap<IBigInsightsLocation, Collection<IBigInsightsApp>>();
	}
	
	public AppSelectionDialog(Shell parent, String title, String description, String errorMessage, IBigInsightsLocation initialLocation) {
		super(parent, title, description, initialLocation);		
		_appProvider = new ApplicationProvider();
		this._errorMessage = errorMessage;
		locationAppMappingList = new HashMap<IBigInsightsLocation, Collection<IBigInsightsApp>>();
	}

	/**
	 * 
	 * @param parent
	 * @param initialLocation
	 * @param initialApp
	 * @param filterStatus: Messages.Decoration_published, Messages.Decoration_deployed
	 */
	public AppSelectionDialog(Shell parent, String title, String description, String _errorMessage, IBigInsightsLocation initialLocation, IBigInsightsApp initialApp, String appStatusFilter) {
		this(parent, title, description, _errorMessage, initialLocation);
		this._app = initialApp;		
		this._appStatusFilter = appStatusFilter;
	}
	
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        //TODO: set help
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IIDEHelpContextIds.CONTAINER_SELECTION_DIALOG);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite dlgArea = (Composite) super.createDialogArea(parent);

	    Label lblApp = new Label(dlgArea, SWT.NONE);        
        GridData gdLabel = new GridData(GridData.BEGINNING);
        gdLabel.horizontalSpan = 2;
        lblApp.setLayoutData(gdLabel);        
        lblApp.setText(Messages.APPSELECTIONDIALOG_DESC);

        if (this._appName!=null) {
        	lblApp.setText(Messages.bind(Messages.APPSELECTIONDIALOG_STATUS,this._appName));
        	// show label with app name and status field
	        lblDesc = new Label(dlgArea, SWT.WRAP);        
	        GridData gdDesc = new GridData(GridData.BEGINNING | GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
	        gdDesc.heightHint = 70;
	        gdDesc.horizontalSpan = 2;	        
	        lblDesc.setLayoutData(gdDesc);   	        
        }
        else {
	        // add app selection
			_cbApp = new Combo(dlgArea, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
	        GridData gdApp = new GridData(GridData.FILL_HORIZONTAL);
	        gdApp.grabExcessHorizontalSpace = true;        
	        _cbApp.setLayoutData(gdApp);
	        _cbApp.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {	
					updateSelectedApp();		
					updateButtonStatus();
				}
			});
	
	        lblDesc = new Label(dlgArea, SWT.WRAP);        
	        GridData gdDesc = new GridData(GridData.BEGINNING | GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
	        gdDesc.heightHint = 40;
	        gdDesc.horizontalSpan = 2;	        
	        lblDesc.setLayoutData(gdDesc);   	        
   
	
	        if (this.getSelectedLocation()!=null) {
	        	// only populate list of apps when default selection is set
	        	retrieveAppsForLocation(this.getSelectedLocation()); 
	        }        
        }

        updateButtonStatus();
        return dlgArea;
    }
    
    protected void onLocationSelectionChanged() {
    	super.onLocationSelectionChanged();
    	if (_cbApp!=null || this._appName!=null) { // check for cbApp to make sure the dialog is created already
	    	// also update the app selection
	    	IBigInsightsLocation selectedLoc = this.getSelectedLocation();
	    	if (selectedLoc!=null) { 
	    		retrieveAppsForLocation(selectedLoc);
	    	}
    	}
    }
    
    private void retrieveAppsForLocation(IBigInsightsLocation location) {
    	try {
			if (_cbApp!=null)
				_cbApp.removeAll();
			this._app = null;
			Collection<IBigInsightsApp> apps = _appProvider.getPublishedApplications(location.getHostName(), location.getHttpClient(), location);
			locationAppMappingList.put(location, apps);		
			for (IBigInsightsApp app:apps) {
				if (this._appName!=null && app.getAppName().equals(this._appName)) { // look for specific up and put status on label
					this._app = app;
					if (app.getStatus().equals(Messages.Decoration_published))
						this.lblDesc.setText(Messages.bind(Messages.APPSELECTIONDIALOG_NOT_DEPLOYED, this._appName));
					else if (app.getStatus().equals(Messages.Decoration_deployed))
						this.lblDesc.setText(Messages.bind(Messages.APPSELECTIONDIALOG_RUN, this._appName));
					break;
				}
				if (_cbApp!=null && (this._appStatusFilter==null || this._appStatusFilter.equals(app.getStatus())))
					_cbApp.add(app.getAppName());	// put all apps in combobox
			}
		} catch (BIConnectionException e) {
			location.handleBIConnectionExceptionFromThread(e);
		}  
		finally {
			if (lblDesc!=null && _cbApp!=null) {
				if (_cbApp.getItemCount()==0)
					lblDesc.setText(this._errorMessage); 
			    else {
			        lblDesc.setText(""); //$NON-NLS-1$
			        _cbApp.select(0);
			    }
			}
			else if (this._appName!=null) {				
				if (this._app==null) {
					this._app = null;
					lblDesc.setText(Messages.bind(Messages.APPSELECTIONDIALOG_NOT_AVAILABLE,this._appName));
				}
				updateButtonStatus();
			}
		}
    }
    
    private void updateSelectedApp() {
    	if (_cbApp!=null && _cbApp.getSelectionIndex()>-1) {
    		String appName = _cbApp.getItem(_cbApp.getSelectionIndex()); 
    		Collection<IBigInsightsApp> apps = locationAppMappingList.get(this.getSelectedLocation());
    		if (apps!=null && !apps.isEmpty()) {    			
    			for (IBigInsightsApp app:apps) {
    				if (app.getAppName().equals(appName)) {
    					this._app = app;
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
    	boolean enabled = (_cbApp!=null && _cbApp.getItemCount()>0 && this._app!=null) || // an app was selected from the combobox
    					(this._appName!=null && this._app!=null); // found the app whose name was passed in
    	if (getOkButton()!=null)
    		getOkButton().setEnabled(enabled);
    }
    
    // index 0: selected location
    // index 1: selected app
    protected void okPressed() {    	    	
    	ArrayList<Object> result = new ArrayList<Object>();   
    	if (this.getSelectedLocation()!=null)
    		result.add(this.getSelectedLocation());
        if (_app != null) {
        	result.add(_app);
		}
        setResult(result);
        // don't call super.okPressed because that will override the result object with just the location
    	setReturnCode(OK);
		close();
    }
	
}
