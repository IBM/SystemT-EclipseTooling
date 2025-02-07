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
package com.ibm.biginsights.project.locations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.wizard.LocationWizard;

public class LocationSelectionDialog extends SelectionDialog {

	private IBigInsightsLocation _location;
	private Combo _cbLocation;
	private Button createLocButton;
	private Label lblDesc=null;
	private String _description = null;
	
	public LocationSelectionDialog(Shell parent, String title, String description, IBigInsightsLocation initialLocation) {
		super(parent);
		this._location = initialLocation;		
		this.setTitle(title);
		this._description = description;
	}
	
	public IBigInsightsLocation getSelectedLocation() {
		return _location;
	}
	
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        //TODO: set help
        //PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IIDEHelpContextIds.CONTAINER_SELECTION_DIALOG);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite dlgArea = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        dlgArea.setLayout(layout);
        
	    Label lblDescription = new Label(dlgArea, SWT.WRAP);        
        GridData gdLblDescription = new GridData(SWT.FILL, SWT.BEGINNING, true, false);   	    
        gdLblDescription.horizontalSpan = 2;
        gdLblDescription.widthHint = 400;
	    lblDescription.setLayoutData(gdLblDescription);
        lblDescription.setText(this._description==null ? "" : this._description); //$NON-NLS-1$

        Label lblSpacer = new Label(dlgArea, SWT.NONE);
        GridData gdLblSpacer = new GridData();
        gdLblSpacer.heightHint = 5;
        lblSpacer.setLayoutData(gdLblSpacer);
        
	    Label lblLocation = new Label(dlgArea, SWT.NONE);        
        GridData gdLabel = new GridData(GridData.BEGINNING);
        gdLabel.horizontalSpan=2;
        lblLocation.setLayoutData(gdLabel);        
        lblLocation.setText(Messages.LOCATIONSELECTIONDIALOG_DESCRIPTION);

        // add location selection
		_cbLocation = new Combo(dlgArea, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        GridData gdLocation = new GridData(GridData.FILL_HORIZONTAL);
        gdLocation.grabExcessHorizontalSpace = true; 
        gdLocation.widthHint=400;
        _cbLocation.setLayoutData(gdLocation);
        _cbLocation.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {	
				onLocationSelectionChanged();
			}
		});

        //refresh locations in combo
	    refreshLocations();
	    
        // create button
        createLocButton = new Button(dlgArea, SWT.PUSH);
        createLocButton.setText(Messages.CREATE_BUTTON);
        GridData dataButton = new GridData(GridData.END);
        dataButton.grabExcessHorizontalSpace = false;
        createLocButton.setLayoutData(dataButton);
        Font font = parent.getFont();
        createLocButton.setFont(font);
        initializeDialogUnits(createLocButton);
        setButtonLayoutData(createLocButton);
        SelectionListener listenerCreateButton = new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {//nothing
			}
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//launch location wizard
				LocationWizard wizard = new LocationWizard();
				    WizardDialog dialog = new WizardDialog(getShell(), wizard);
				    dialog.create();
				    dialog.open();
				    //refresh locations in combo
				    refreshLocations();
				    
				    //hide message to create a server if some exist
				    if(lblDesc != null){
					    if (_cbLocation.getItemCount()>0) {
					    	lblDesc.setVisible(false);
					    }else{
					    	lblDesc.setVisible(true);
					    }
				    }
			}

	
        };
        createLocButton.addSelectionListener(listenerCreateButton);
        
        //if combo box is empty show message
        if (_cbLocation.getItemCount()==0) {
    	    lblDesc = new Label(dlgArea, SWT.WRAP);        
            GridData gdDesc = new GridData(GridData.BEGINNING);
            gdDesc.horizontalSpan=2;
            lblDesc.setLayoutData(gdDesc); 
            lblDesc.setText(Messages.LOCATIONSELECTIONDIALOG_WARNING);
        }
		
        updateButtonStatus();
        return dlgArea;
    }
    
	private void refreshLocations() {
		Collection<IBigInsightsLocation> locations = LocationRegistry.getInstance().getLocations();
        int defaultIndex = -1;
        int counter = 0;
        
        _cbLocation.removeAll();
        
        for (IBigInsightsLocation loc:locations) {
        	if (this._location!=null && loc.getLocationName().equals(this._location.getLocationName())) {
        		defaultIndex = counter;
        	}
        	_cbLocation.add(loc.getLocationDisplayString());
        	counter++;
        }
        if (defaultIndex>-1)
        	_cbLocation.select(defaultIndex);
        

	}		
    
    protected void onLocationSelectionChanged() {
		updateSelectedLocation();		
		updateButtonStatus();
    }
    
    protected void updateSelectedLocation() {
    	if (_cbLocation.getSelectionIndex()>-1) {
    		String locDisplayName = _cbLocation.getItem(_cbLocation.getSelectionIndex());    		
    		this._location = LocationRegistry.getInstance().getLocationByDisplayName(locDisplayName);
    	}
    }
    
    protected void createButtonsForButtonBar(Composite parent) {
    	super.createButtonsForButtonBar(parent);
    	updateButtonStatus();
    }
    
    protected void updateButtonStatus() {
    	boolean enabled = _cbLocation.getItemCount()>0 && this._location!=null;
    	if (getOkButton()!=null)
    		getOkButton().setEnabled(enabled);
    }
    
    protected void okPressed() {
    	List<IBigInsightsLocation> result = new ArrayList<IBigInsightsLocation>();        
        if (_location != null) {
        	result.add(_location);
		}
        setResult(result);
        super.okPressed();
    }
	
}
