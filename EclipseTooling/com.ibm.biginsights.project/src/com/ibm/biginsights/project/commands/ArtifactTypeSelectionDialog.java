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
import org.eclipse.ui.dialogs.SelectionDialog;

import com.ibm.biginsights.project.Messages;

public class ArtifactTypeSelectionDialog extends SelectionDialog {

	private List<ArtifactType> _artifactTypes;
	private String _dlgDesc; 
	private ArtifactType _artifactType;
	private Label lblDescription;
	
	public ArtifactTypeSelectionDialog(Shell parent, String title, String desc, List<ArtifactType> artifactTypes) {
		super(parent);
		this._artifactTypes = artifactTypes;		
		this.setTitle(title);
		this._dlgDesc = desc;
	}
	
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite dlgArea = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        dlgArea.setLayout(layout);
                
	    Label lblLocation = new Label(dlgArea, SWT.WRAP);        
        GridData gdLabel = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false);   
        gdLabel.horizontalSpan = 2;
        lblLocation.setLayoutData(gdLabel);        
        lblLocation.setText(this._dlgDesc);

	    Label lblType = new Label(dlgArea, SWT.NONE);        
        GridData gdType = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);        
        lblType.setLayoutData(gdType);        
        lblType.setText(Messages.ARTIFACTTYPESELECTIONDIALOG_TYPE);

	    Label lblDescTitle = new Label(dlgArea, SWT.NONE);        
        GridData gdDesc = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);        
        lblDescTitle.setLayoutData(gdDesc);        
        lblDescTitle.setText(Messages.ARTIFACTTYPESELECTIONDIALOG_DESC);

        Composite cmpButtons = new Composite(dlgArea, SWT.NONE);
        GridLayout lButtons = new GridLayout();
        cmpButtons.setLayout(lButtons);
        GridData gdCmpButtons = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, true);
        cmpButtons.setLayoutData(gdCmpButtons);

        Button firstElement = null;
        // add radio buttons for each artifact type
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
					// TODO Auto-generated method stub					
				}
			});
        	
        	GridData gdBtn = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false);
        	gdBtn.horizontalIndent = 5;
        	btnType.setLayoutData(gdBtn);
        	// save the first button so we can initialize the default selection when the dialog comes up
        	if (firstElement==null)
        		firstElement = btnType;
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
	    	
    	if (firstElement!=null) {
    		firstElement.setSelection(true);
    		onTypeSelectionChanged(_artifactTypes.get(0));
    	}
        return dlgArea;
    }
        
    protected void onTypeSelectionChanged(ArtifactType type) {
		this._artifactType = type;	
		if (lblDescription!=null)
			lblDescription.setText(type.desc);
    }
    
    protected void okPressed() {
    	List<ArtifactType> result = new ArrayList<ArtifactType>();        
        result.add(this._artifactType);		
        setResult(result);
        super.okPressed();
    }
}
