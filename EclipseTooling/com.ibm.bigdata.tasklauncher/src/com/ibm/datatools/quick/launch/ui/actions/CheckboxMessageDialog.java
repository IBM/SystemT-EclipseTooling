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
package com.ibm.datatools.quick.launch.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class CheckboxMessageDialog extends MessageDialog implements SelectionListener {


	//See also com.ibm.datatools.dsws.tooling.ui.dialogs
    
    private String detailMessage = null;
    
    private CheckBoxDefinition[]checkboxes;

    /**
     * Creates a message dialog and allows the user to remember the decision
     * 
     * @param Shell parentShell
     * @param int type (MessageDialog.ERROR, MessageDialog.INFORMATION, MessageDialog.WARNING, ...)
     * @param String propertiesKey            
     * @param String title
     * @param String message
     * @param String detailMessage
     * @param String[] buttonsLabel
     * @param int defaultButtonIndex
     * @param CheckBoxDefinition checkboxes
     */
    public CheckboxMessageDialog(Shell parentShell, int type, String title, String message, String detailMessage, 
            String[] buttonsLabel, int defaultButtonIndex, CheckBoxDefinition... checkboxes) {
        super(parentShell, title, null, message, type, buttonsLabel, defaultButtonIndex);
        this.detailMessage = detailMessage;
        this.checkboxes = checkboxes;
        setBlockOnOpen(true);
    }

    public Control createCustomArea(Composite parent) {
    	
    	Composite cmpButtons = new Composite(parent, SWT.NONE);	    
	    cmpButtons.setLayout(new GridLayout());

	    for (CheckBoxDefinition checkbox:checkboxes) {
            GridData gd = new GridData();
            gd.heightHint = 20;
            gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
            //gd.verticalIndent = 20;
            gd.horizontalIndent = 43;

            Button btnCheckbox = new Button(cmpButtons, SWT.CHECK);
            btnCheckbox.setData(checkbox);
            btnCheckbox.setText(checkbox.label);
            btnCheckbox.setSelection(checkbox.checkboxSelected);
            btnCheckbox.setLayoutData(gd);
            btnCheckbox.addSelectionListener(this);	        
        }
        
        if (detailMessage!=null) {
	        Label lblDetailMessage = new Label(cmpButtons, SWT.WRAP);
	        GridData gdLbl = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
	        gdLbl.horizontalIndent = 43;
	        gdLbl.verticalIndent = 8;
	        lblDetailMessage.setLayoutData(gdLbl);
	        lblDetailMessage.setText(detailMessage);
        }
                
        return (cmpButtons);
    }

    public boolean isCheckboxSelected(String propertiesKey) {
    	for (CheckBoxDefinition checkbox:checkboxes) {
    		if (checkbox.propertiesKey.equals(propertiesKey))
    			return checkbox.checkboxSelected;
    	}
        return false;
    }

    public void widgetDefaultSelected(SelectionEvent event) {
    }

    public void widgetSelected(SelectionEvent e) {
    	Object source = e.getSource();
    	if (source instanceof Button) {
    		Object data = ((Button) source).getData();
    		if (data!=null && data instanceof CheckBoxDefinition) {
    			((CheckBoxDefinition)data).checkboxSelected = ((Button)source).getSelection();
    		}
    	}
    }

    public static class CheckBoxDefinition {
    	public String label;
    	public String propertiesKey;
    	public boolean checkboxSelected;
    	
    	public CheckBoxDefinition(String label, String propertiesKey, boolean checkboxSelected) {
    		this.label = label;
    		this.propertiesKey = propertiesKey;
    		this.checkboxSelected = checkboxSelected;
    	}
    }
}
