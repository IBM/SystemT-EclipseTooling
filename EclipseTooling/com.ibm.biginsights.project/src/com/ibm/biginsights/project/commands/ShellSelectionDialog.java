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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.project.BigInsightsLibraryContainerInitializer;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationSelectionDialog;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;

public class ShellSelectionDialog extends LocationSelectionDialog {

	private List<ArtifactType> _artifactTypes;
	private ArtifactType _artifactType;
	private Label lblDescription;
	private ArrayList<Button> radioButtons = new ArrayList<Button>(); // list for radio buttons for each shell; needed to disable/enable based on selected BI server
	
	public ShellSelectionDialog(Shell parent, IBigInsightsLocation initialLocation, List<ArtifactType> artifactTypes) {
		super(parent, Messages.SHELLSELECTIONDIALOG_TITLE, Messages.SHELLSELECTIONDIALOG_DESC, initialLocation);
		this._artifactTypes = artifactTypes;
	}

    protected Control createDialogArea(Composite parent) {
        Composite cmpParent = (Composite) super.createDialogArea(parent);
        
        Composite dlgArea = new Composite(cmpParent, SWT.NONE); 
        GridLayout layout = new GridLayout(2, false);
        dlgArea.setLayout(layout);
        GridData gdDlgArea = new GridData(SWT.FILL, SWT.FILL, true, true);
        gdDlgArea.horizontalSpan = 2;
        dlgArea.setLayoutData(gdDlgArea);
                
	    Label lblType = new Label(dlgArea, SWT.NONE);        
        GridData gdType = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);        
        lblType.setLayoutData(gdType);        
        lblType.setText(Messages.SHELLSELECTIONDIALOG_TYPE);

	    Label lblDescTitle = new Label(dlgArea, SWT.NONE);        
        GridData gdDesc = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);        
        lblDescTitle.setLayoutData(gdDesc);        
        lblDescTitle.setText(Messages.SHELLSELECTIONDIALOG_TYPE_DESC);

        Composite cmpButtons = new Composite(dlgArea, SWT.NONE);
        GridLayout lButtons = new GridLayout();
        cmpButtons.setLayout(lButtons);
        GridData gdCmpButtons = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true);
        cmpButtons.setLayoutData(gdCmpButtons);
        
        for (ArtifactType type:_artifactTypes) {
        	Button btnType = new Button(cmpButtons, SWT.RADIO);
        	btnType.setText(type.name);     
        	btnType.setData(type);
        	btnType.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					onTypeSelectionChanged((ArtifactType)((Button)e.getSource()).getData());					
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					// 					
				}
			});
            // add radio buttons for each artifact type
        	radioButtons.add(btnType);

        	GridData gdBtn = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false);
        	gdBtn.horizontalIndent = 5;
        	btnType.setLayoutData(gdBtn);
        }
        
    	// add description field
    	lblDescription = new Label(dlgArea, SWT.WRAP | SWT.BORDER);
    	GridData gdDescription = new GridData(SWT.FILL, SWT.FILL, true, true);    	    	
    	lblDescription.setLayoutData(gdDescription);
    	Display display = Display.getCurrent();    
    	if (!display.getHighContrast())
    		lblDescription.setBackground(display.getSystemColor(SWT.COLOR_WHITE));    	
    	gdDescription.widthHint = 400;
    	gdDescription.heightHint = 125;
	    	
    	if (!radioButtons.isEmpty())
    		setDefaultValues(radioButtons.get(0));    	
    	
        return dlgArea;
    }
    
    protected void updateSelectedLocation() {
    	super.updateSelectedLocation();
    	// check version of selected BI server and disable not supported shells
    	if (this.getSelectedLocation()!=null) {
    		String version = this.getSelectedLocation().getVersion();
    		boolean isLocationSupported = BigInsightsLibraryContainerInitializer.getInstance().mapVersionToContainerVersion(this.getSelectedLocation().getVersionWithVendor())!=null;
    		//remove the v if it is there
    		if(version.startsWith("v")){ //$NON-NLS-1$
    			version = version.substring(1);
    		}    
    		enableButtonsByVersion(version, isLocationSupported);
    	}
    }    
    
    private void enableButtonsByVersion(String minVersion, boolean isLocationSupported) {
    	for (Button btn:radioButtons) {
    		ArtifactType type = (ArtifactType)btn.getData();
    		btn.setEnabled(isLocationSupported && (type.minBIVersion==null || BIProjectPreferencesUtil.isAtLeast(minVersion, type.minBIVersion)));    	
    	}
    }
    
    protected void updateButtonStatus() {
    	super.updateButtonStatus();
    	// check if any of the enabled radio buttons are selected, otherwise can't finish
    	boolean selectedButtonIsEnabled = false;
    	for (Button btn:radioButtons) {
    		if (btn.isEnabled() && btn.getSelection()) {
    			selectedButtonIsEnabled = true;
    			break;
    		}
    	}
    	if (getOkButton()!=null)
    		getOkButton().setEnabled(selectedButtonIsEnabled);
    }
    
    private void setDefaultValues(Button btnType) {
    	// select first button
    	btnType.setSelection(true);
    	onTypeSelectionChanged(_artifactTypes.get(0));
    }
        
    protected void onTypeSelectionChanged(ArtifactType type) {
		this._artifactType = type;	
		if (lblDescription!=null)
			lblDescription.setText(type.desc);
		updateButtonStatus();
    }

    /**
     * index 0: selected BigInsights Server
     * index 1: selected shell type
     */
    protected void okPressed() {    	    	
    	ArrayList<Object> result = new ArrayList<Object>();   
    	if (this.getSelectedLocation()!=null)
    		result.add(this.getSelectedLocation());
        if (_artifactType != null) {
        	result.add(_artifactType);
		}
        setResult(result);
        // don't call super.okPressed because that will override the result object with just the BigInsights server
    	setReturnCode(OK);
		close();
    }
	
}
