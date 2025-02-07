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
package com.ibm.biginsights.project.wizard;

import java.util.Collection;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;
import com.ibm.biginsights.project.locations.wizard.LocationWizard;

public class LocationPage extends WizardPage implements Listener {

	private Combo _cbLocation;
	private Label _lblLocationName;
	private Button _btnAddNewLocation = null;
	private Collection<IBigInsightsLocation>locations;
	private IBigInsightsLocation selectedLocation;

	
	public LocationPage(String title) {
		super(title);
	}
    
	public void initPage(Collection<IBigInsightsLocation>locations, IBigInsightsLocation selectedLocation)
	{
		this.locations = locations;
		this.selectedLocation = selectedLocation;
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.FILL);
		composite.setFont(parent.getFont());
		// 3 column layout, columns have different width
		composite.setLayout(new GridLayout(3,false));
		
		Label lblLocation = new Label(composite, SWT.NONE);
		lblLocation.setFont(composite.getFont());
		GridData gdLabel = new GridData();				
		lblLocation.setLayoutData(gdLabel);		
		lblLocation.setText(Messages.LOCATIONPAGE_LOCATION_LABEL);
		
		_cbLocation = new Combo(composite, SWT.READ_ONLY);
		_cbLocation.setFont(composite.getFont());
		
		GridData gdCombo = new GridData(SWT.FILL, SWT.CENTER, true, false);   		
		_cbLocation.setLayoutData(gdCombo);
				
		_cbLocation.addModifyListener(new ModifyListener() {			
			
			@Override
			public void modifyText(ModifyEvent e) {
				changeLocation();				
			}			
		});

        // add new location button
        _btnAddNewLocation = new Button(composite, SWT.PUSH);
        _btnAddNewLocation.setText(Messages.LOCATIONPAGE_ADD_BUTTON_LABEL);
        _btnAddNewLocation.setFont(composite.getFont());
        _btnAddNewLocation.addListener(SWT.Selection, this);  
		GridData gdButton = new GridData(SWT.RIGHT, SWT.CENTER, false, false);    
		_btnAddNewLocation.setLayoutData(gdButton);

        // new line: show display location name
		_lblLocationName = new Label(composite, SWT.WRAP);
		_lblLocationName.setFont(composite.getFont());
		GridData gdLocationName = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gdLocationName.horizontalSpan = 3;
		_lblLocationName.setLayoutData(gdLocationName);						
				
        
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), "com.ibm.biginsights.project.help.create_bigi_server"); //$NON-NLS-1$
		populateUI();
		updateCompleteState();
	}
	
	private void populateUI() {
		// populate the locations
		if (locations!=null && locations.size()>0)
		{			
			_cbLocation.removeAll();
			for (IBigInsightsLocation loc:locations)
				_cbLocation.add(loc.getLocationName());
		}

		// set the selected item
		if (this.selectedLocation!=null)
		{
			for (String s:_cbLocation.getItems())
			{
				if (s.equals(this.selectedLocation.getLocationName()))
				{
					_cbLocation.select(_cbLocation.indexOf(s));
				}
			}			
		}


	}
	
	@Override
	public void handleEvent(Event event) {
        Widget source = event.widget;

        if (source == _btnAddNewLocation) {
        	IWorkbench workbench = PlatformUI.getWorkbench();
    	    Shell shell = workbench.getActiveWorkbenchWindow().getShell();
    	    LocationWizard wizard = new LocationWizard();	    
    	    WizardDialog dialog = new WizardDialog(shell, wizard);
    	    dialog.create();
    	    int result = dialog.open();
    	    if (result==Window.OK)
    	    {
    	    	// refresh list of locations
    	    	this.locations = LocationRegistry.getInstance().getLocations();
    	    	populateUI();
    	    }
		}
		
	}
	
	
	
	protected void changeLocation() {		
		
		if (_cbLocation!=null && _cbLocation.getSelectionIndex()>=0)
		{
			String sel = _cbLocation.getItem(_cbLocation.getSelectionIndex());
			for (IBigInsightsLocation loc:locations)
			{
				if (loc.getLocationName().equals(sel))
				{
					this.selectedLocation = loc;
					this._lblLocationName.setText(loc.getLocationDisplayString());
					break;
				}
			}
		}		
		updateCompleteState();
		
	}	
	
	private void updateCompleteState()
	{
		setPageComplete(this.selectedLocation!=null);
	}
	
	public IBigInsightsLocation getSelectedLocation()
	{
		return this.selectedLocation;
	}

}
