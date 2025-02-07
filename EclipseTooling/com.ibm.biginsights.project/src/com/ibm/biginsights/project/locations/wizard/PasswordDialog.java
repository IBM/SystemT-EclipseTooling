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
package com.ibm.biginsights.project.locations.wizard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;

public class PasswordDialog extends Dialog {

	private String _userName = null;
	private String _password = null;
	private boolean _savePassword;
	
	private Label _lblLocation;
	private Text _txtUserId;
	private Text _txtPassword;
	private Button _btnSavePassword;
	
	private IBigInsightsLocation location;
	
	public PasswordDialog(Shell parent, IBigInsightsLocation location) {
		super(parent);
		this.location = location;
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Messages.PASSWORDDIALOG_REQUIRED);
	}
	
	public void create() {
		super.create();
		_lblLocation.setText(location.getLocationDisplayString());
		if(location.getUserName() != null){
			_txtUserId.setText(location.getUserName());
			if(_txtUserId.getText() != null && _txtUserId.getText().length()>0){
				_txtUserId.setEditable(false);
				_txtUserId.setEnabled(false);
			}
		}
		if (location.getPassword()!=null){
			_txtPassword.setText(location.getPassword());
		}
		_btnSavePassword.setSelection(location.getSavePassword());
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2,false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 500;
		composite.setLayoutData(gd);
		
		createLocationLabel(composite);
		createUserIdField(composite);
		createPasswordField(composite);
		
		return composite;
	}
	
	private void createLocationLabel(Composite composite) {
		Label lbl = new Label(composite, SWT.NONE);
		lbl.setFont(composite.getFont());
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lbl.setText(Messages.PASSWORDDIALOG_LOCATION_LABEL);
		
		_lblLocation = new Label(composite, SWT.NONE);
		_lblLocation.setFont(composite.getFont());
		_lblLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));		
	}
	
	
	private void createUserIdField(Composite composite)
	{
		Label lblUserId = new Label(composite, SWT.NONE);
		lblUserId.setFont(composite.getFont());
		lblUserId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblUserId.setText(Messages.PASSWORDDIALOG_USER_ID_LABEL);
		
        // user id entry field
		_txtUserId = new Text(composite, SWT.SINGLE | SWT.BORDER);        
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalIndent = 5;
        data.grabExcessHorizontalSpace = true;
        _txtUserId.setLayoutData(data);
        _txtUserId.setFont(composite.getFont());
	}

	private void createPasswordField(Composite composite)
	{
		Label lblPassword = new Label(composite, SWT.NONE);
		lblPassword.setFont(composite.getFont());
		lblPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPassword.setText(Messages.PASSWORDDIALOG_PASSWORD_LABEL);
		
        // port entry field
		_txtPassword = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);        
        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);        
        data.horizontalIndent = 5;
        data.grabExcessHorizontalSpace = true;
        _txtPassword.setLayoutData(data);
        _txtPassword.setFont(composite.getFont());

        // add Save password field
		_btnSavePassword = new Button(composite, SWT.CHECK);
		_btnSavePassword.setText(Messages.PASSWORDDIALOG_DESC);
		_btnSavePassword.setToolTipText(Messages.PASSWORDDIALOG_DESC_TT);
		GridData gdBtn = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);        
		gdBtn.horizontalSpan = 2;
		_btnSavePassword.setLayoutData(gdBtn);							
	}
	
	protected void okPressed() {
		this._userName = _txtUserId.getText();
		this._password = _txtPassword.getText();
		this._savePassword = _btnSavePassword.getSelection();
		
		super.okPressed();
	}

	public String getUserId() {
		return this._userName;		
	}
	
	public String getPassword() {
		return this._password;
	}
	
	public boolean getSavePassword() {
		return this._savePassword;
	}
}
