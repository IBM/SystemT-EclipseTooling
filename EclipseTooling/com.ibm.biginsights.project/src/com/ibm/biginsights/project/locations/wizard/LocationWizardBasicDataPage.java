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

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BigInsightsLocation;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;
import com.ibm.biginsights.project.locations.BigInsightsLocation.TestConnectionResult;
import com.ibm.biginsights.project.util.BIConstants;

public class LocationWizardBasicDataPage extends WizardPage implements Listener {	
	
	private boolean isUpdateMode = false; // will be true when updating existing location
	private String _origUpdateName;	// holds the name of the location that is being updated; used when validating the locationName
	private String _locationname = null; 
	private String _hostname = null; 
	private Integer _port = null;
	private String _userId = null;
	private String _password= null;
	private boolean _savePassword = false;
	private boolean _useSSL = false;
	private Button _btnTestConnection = null;	
	private Button _btnSavePassword = null;
	private Text _txtLocationname = null;
	private String _url = null;
	private List roles = new ArrayList();
		
	private ControlDecoration errorLocationName;
	private ControlDecoration errorURL;
	
    public LocationWizardBasicDataPage(String pageName) {
    	super(pageName);
    	
	    setPageComplete(false);
	    setTitle(Messages.LOCATIONWIZARDBASICDATAPAGE_TITLE);
    }

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.FILL);
		composite.setFont(parent.getFont());
		// 2 column layout, columns have different width
		composite.setLayout(new GridLayout(2,false));
		
		createURLField(composite);	
		createLocationNameField(composite);
		createUserIdField(composite);
		createPasswordField(composite);
		createTestConnectionAndStatusLabel(composite);
						
		setControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.getControl(), "com.ibm.biginsights.project.help.create_bigi_server"); //$NON-NLS-1$		
		
		updatePageStatus();
	}
	
	public static void setRequiredControlDecoration(Control control) {
		ControlDecoration decoration = new ControlDecoration(control, SWT.LEFT);
		Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED).getImage();
		decoration.setImage(image);
		decoration.setDescriptionText(Messages.LOCATIONWIZARDBASICDATAPAGE_REQUIRED);
	}
	
	public ControlDecoration addErrorDecoration(Control control, String message) {
		ControlDecoration decoration = new ControlDecoration(control, SWT.LEFT);
		Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
		decoration.setImage(image);
		decoration.setDescriptionText(message);
		decoration.hide();
		return decoration;
	}
	
	public void initForUpdate(IBigInsightsLocation updateLocation) {
		isUpdateMode = true;
		_origUpdateName = updateLocation.getLocationName();
		setLocationname(updateLocation.getLocationName());
    	setHostname(updateLocation.getHostName());
    	setPort(updateLocation.getPort());
    	setUserId(updateLocation.getUserName());
    	setPassword(updateLocation.getPassword());
    	setSavePassword(updateLocation.getSavePassword());
    	setUseSSL(updateLocation.getUseSSL());
    	setURL(updateLocation.getURL());
	}
	
	private void updatePageStatus() {
		
		// always call validate location and port because in the if-statement below it would stop as soon as one is false		
		boolean validLocation = validateLocationName();
		boolean validateURL = validateURL();				
		// all fields are mandatory
		boolean result = false;
		if (validLocation && validateURL &&
			getHostname()!=null && !getHostname().isEmpty())// &&
			// user id and pw are not mandatory
//			getUserId()!=null && !getUserId().isEmpty() &&
//			getPassword()!=null && !getPassword().isEmpty())
		{	
//			IBigInsightsLocation tempLocation = new BigInsightsLocation(getHostname(), getURL(), getUserId(), getPassword(), false);	
//			if(tempLocation.isAuthenticationNeeded()){
//				if(getUserId()!=null && getPassword()!=null){
//					result = true;
//				}
//			}else{
				result = true;
//			}
		}
		
		if (_btnTestConnection!=null)
			_btnTestConnection.setEnabled(result);		
		setPageComplete(result);
	}
	
	private boolean validateLocationName() {
		boolean result = getLocationName()!=null && !getLocationName().isEmpty();
		// make sure no other location with the same name exists already		
		for (IBigInsightsLocation loc:LocationRegistry.getInstance().getLocations())
		{
			if (loc.getLocationName().equals(getLocationName()))
			{
				// either it's the same name when creating a new connection, or in update mode it's the same name 
				// as an existing connection but not the original name
				if (!isUpdateMode || isUpdateMode && !getLocationName().equals(_origUpdateName))
				{
					result = false;					
				}				
			}
		}
		
		if (errorLocationName!=null) {
			if (result) {
				errorLocationName.hide();
			}
			else {
				// don't show the message if the location field is empty
				if (getLocationName()!=null && !getLocationName().isEmpty())
				{
					String message = Messages.LOCATIONWIZARDBASICDATAPAGE_NAME_ERROR;
					errorLocationName.setDescriptionText(message);
					errorLocationName.show();
				}
			}
		}

		return result;
	}	
	
	private boolean validateURL() {
		boolean result = getURL()!=null;
		String errorMessage = null;
		if (result) {
			try {
				URL checkURL = new URL(this.getURL());		
				this.setHostname(checkURL.getHost());
				int port = checkURL.getPort();
				if (port==-1) {
					// default ports: HTTP: 80; HTTPS: 443
					this.setPort(checkURL.getProtocol().equals(BIConstants.HTTPS) ? 443 : 80);
				}
				else { // negative ports are covered by the URL parser, need to check for 
					if (port>65535) {
						result = false;
						errorMessage = Messages.LOCATIONWIZARDBASICDATAPAGE_PORT_ERROR;
					}					
					else
						this.setPort(checkURL.getPort());
				}
				this.setUseSSL(checkURL.getProtocol().equals(BIConstants.HTTPS));				
			} catch (MalformedURLException e) {
				result = false;		
				errorMessage = e.getMessage();
			}
		}
		
		if (errorURL!=null) {
			if (result) {
				errorURL.hide();
			}
			else {
				if (getURL()!=null) { // don't show the error message if URL is empty					
					errorURL.setDescriptionText(errorMessage);
					errorURL.show();
				}
			}
		}

		return result;
	}

	private void createURLField(Composite composite)
	{
		Label lblUrl = new Label(composite, SWT.NONE);
		lblUrl.setFont(composite.getFont());
		lblUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblUrl.setText(Messages.LOCATIONWIZARDBASICDATAPAGE_URL_LABEL);
		
        // location name entry field
		Text txtURL = new Text(composite, SWT.SINGLE | SWT.BORDER);        
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalIndent = 5;
        data.grabExcessHorizontalSpace = true;
        txtURL.setLayoutData(data);
        txtURL.setFont(composite.getFont());
        txtURL.setToolTipText(Messages.LOCATIONWIZARDBASICDATAPAGE_URL_LABEL_HELP);
        ModifyListener listener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				_url = ((Text)event.widget).getText();
				updatePageStatus();
			}
        };
        FocusListener focusListener = new FocusListener() {			
			@Override
			public void focusLost(FocusEvent e) {				
				// set a default name for the location based on host name
				setDefaultLocationName();					
			}			
			
			@Override
			public void focusGained(FocusEvent e) {
				// don't do anything				
			}
		};
        
        setRequiredControlDecoration(txtURL);
        errorURL = addErrorDecoration(txtURL, ""); //$NON-NLS-1$
        txtURL.addModifyListener(listener);
        txtURL.addFocusListener(focusListener);
        
        // init with existing values
        if (_url!=null)
        	txtURL.setText(_url);
	}
	
	private void setDefaultLocationName() {
		// only set a default name for the location if no location name has been set yet
		if (this._url!=null && (this.getLocationName()==null || this.getLocationName().isEmpty())) {
			URL checkURL;
			try {
				checkURL = new URL(this._url);
				String _hostName = checkURL.getHost();
				// check if a location with the name already exists - if so, add a counter to the name
				boolean isUnique = false;
				int counter = 0;
				String nameToTest = null;
				while (!isUnique) {
					nameToTest = counter==0 ? _hostName:_hostName+" ("+counter+")"; //$NON-NLS-1$ //$NON-NLS-2$
					isUnique = isUniqueLocationName(nameToTest);				
					counter++;
				}
				_txtLocationname.setText(nameToTest); 
				
			} catch (MalformedURLException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			}
		}
	}
	
	private boolean isUniqueLocationName(String nameToTest) {
		boolean isUnique = true;		
		for (IBigInsightsLocation loc:LocationRegistry.getInstance().getLocations()) {
			if (loc.getLocationName().equalsIgnoreCase(nameToTest)) {
				isUnique = false;
				break;
			}
		}
		
		return isUnique;
	}

	private void createLocationNameField(Composite composite)
	{
		Label lblLocationname = new Label(composite, SWT.NONE);
		lblLocationname.setFont(composite.getFont());
		lblLocationname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblLocationname.setText(Messages.LOCATIONWIZARDBASICDATAPAGE_LOCATION_LABEL);
		
        // location name entry field
		_txtLocationname = new Text(composite, SWT.SINGLE | SWT.BORDER);        
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalIndent = 5;
        data.grabExcessHorizontalSpace = true;
        _txtLocationname.setLayoutData(data);
        _txtLocationname.setFont(composite.getFont());
        ModifyListener listener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				_locationname = ((Text)event.widget).getText();
				updatePageStatus();
			}
        };
        
        setRequiredControlDecoration(_txtLocationname);
        errorLocationName = addErrorDecoration(_txtLocationname, ""); //$NON-NLS-1$
        _txtLocationname.addModifyListener(listener);
        
        // init with existing values
        if (_locationname!=null)
        	_txtLocationname.setText(_locationname);
	}
	
	private void createUserIdField(Composite composite)
	{
		Label lblUserId = new Label(composite, SWT.NONE);
		lblUserId.setFont(composite.getFont());
		lblUserId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblUserId.setText(Messages.LOCATIONWIZARDBASICDATAPAGE_USER_LABEL);
		
        // user id entry field
		Text txtUserId = new Text(composite, SWT.SINGLE | SWT.BORDER);        
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.horizontalIndent = 5;
        data.grabExcessHorizontalSpace = true;
        txtUserId.setLayoutData(data);
        txtUserId.setFont(composite.getFont());
        ModifyListener listener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				_userId = ((Text)event.widget).getText().isEmpty() ? null : ((Text)event.widget).getText();
				updatePageStatus();
			}
        };
        // user id is not mandatory
        //setRequiredControlDecoration(txtUserId);
        txtUserId.addModifyListener(listener);	
        
        // init with existing values
        if (_userId!=null)
        	txtUserId.setText(_userId);
	}

	private void createPasswordField(Composite composite)
	{
		Label lblPassword = new Label(composite, SWT.NONE);
		lblPassword.setFont(composite.getFont());
		lblPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPassword.setText(Messages.LOCATIONWIZARDBASICDATAPAGE_PASSWORD_LABEL);
		
        // port entry field
		Text txtPassword = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);        
        GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);        
        data.horizontalIndent = 5;
        data.grabExcessHorizontalSpace = true;
        txtPassword.setLayoutData(data);
        txtPassword.setFont(composite.getFont());
        ModifyListener listener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent event) {
				_password = ((Text)event.widget).getText();
				updatePageStatus();
			}
        };
        // password is not mandatory
//        setRequiredControlDecoration(txtPassword);
        txtPassword.addModifyListener(listener);	
        
        // init with existing values
        if (_password!=null)
        	txtPassword.setText(_password);
        
        // add Save password field
		_btnSavePassword = new Button(composite, SWT.CHECK);
		_btnSavePassword.setText(Messages.LOCATIONWIZARDBASICDATAPAGE_PASSWORD_DESC);
		_btnSavePassword.setToolTipText(Messages.LOCATIONWIZARDBASICDATAPAGE_PASSWORD_DESC_TT);
		GridData gdBtn = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);        
		gdBtn.horizontalSpan = 2;
		_btnSavePassword.setLayoutData(gdBtn);
		_btnSavePassword.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				_savePassword = _btnSavePassword.getSelection();				
			}			
		});
		
		// init with existing values
        _btnSavePassword.setSelection(_savePassword);
	}

	private void createTestConnectionAndStatusLabel(Composite composite)
	{
        // test connection button
        _btnTestConnection = new Button(composite, SWT.PUSH);
        _btnTestConnection.setText(Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_LABEL);
        _btnTestConnection.setFont(composite.getFont());
        _btnTestConnection.setLayoutData(new GridData(SWT.NONE));
        _btnTestConnection.addListener(SWT.Selection, this);         
	}
	
	@Override
	public void handleEvent(Event event) {
        Widget source = event.widget;

        if (source == _btnTestConnection) {
			handleTestConnection(true);
		}
		
	}
	
	class TestConnectionJob implements IRunnableWithProgress{
		TestConnectionResult result;

		@Override
		public void run(IProgressMonitor monitor)  {
			monitor.beginTask(Messages.Location_connect, IProgressMonitor.UNKNOWN);
			try{
				class ConnectJob extends Job{
					boolean done=false;
					
					ConnectJob (){
						super("connect"); //$NON-NLS-1$
					}
					@Override
					protected IStatus run(IProgressMonitor monitor) {								
						result = BigInsightsLocation.testConnection(getURL(), getUserId(), getPassword());
						done=true;	
						return Status.OK_STATUS;
					}
		
					public boolean isDone(){
						return done;
					}
				};
				ConnectJob job = new ConnectJob();

				// Start the Job
				job.schedule();
				
				while(!job.isDone()){
					if(monitor.isCanceled()){
						throw new RuntimeException(new InterruptedException(Messages.SERVER_CALL_CANCELED));
					}
				}
				
			}finally{
				monitor.done();
			}
			
			return;
		}
		public TestConnectionResult getResults(){
			return result;
		}
	};
	
	
	public boolean handleTestConnection(boolean showSuccessfulMessage)
	{		
		TestConnectionJob job = new TestConnectionJob();
		
		try {
			getWizard().getContainer().run(true, true, job);
		} catch (InvocationTargetException e) {
			if(e.getTargetException().getCause() instanceof InterruptedException){
				MessageDialog.openInformation(this.getShell(), Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_ERROR_TITLE, Messages.SERVER_CALL_CANCELED);			
			}else{			
				MessageDialog.openError(this.getShell(), Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_ERROR_TITLE, e.getMessage()); //$NON-NLS-1$
			}
			return false;
		} catch (InterruptedException e) {
			MessageDialog.openInformation(this.getShell(), Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_ERROR_TITLE, Messages.SERVER_CALL_CANCELED);			
			return false;
		}
		TestConnectionResult result = job.getResults();
		
		if(result == null) return false;
		
		if (result.success) {
			if(result.templocation != null && result.templocation.getUserName() != null && result.templocation.getPassword() != null){
				this.setUserId(result.templocation.getUserName());
				this.setPassword(result.templocation.getPassword());
				this.setRoles(result.templocation.getRoles());
			}
			if (showSuccessfulMessage)
				MessageDialog.openInformation(this.getShell(), Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_SUCCESS_TITLE, Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_SUCCESS_DESC);			
		}
		else {
			String msg = Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_ERROR_DESC;
			
			if(result.statusCode>-1 && result.statusMessage != null) msg += " " +result.statusCode + ": " + result.statusMessage; //$NON-NLS-1$ //$NON-NLS-2$
			else if(result.statusCode>-1) msg += " " +result.statusCode; //$NON-NLS-1$
			else if(result.statusMessage != null) msg += " " +result.statusMessage; //$NON-NLS-1$
			
			MessageDialog.openError(this.getShell(), Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_ERROR_TITLE, msg); //$NON-NLS-1$
		}

		return result.success;
	}

	public String getLocationName()	{
		return _locationname;
	}

	public String getHostname() {
		return _hostname;
	}
	
	public Integer getPort() {
		return _port;
	}
	
	public String getUserId() {
		return _userId;
	}
	
	public String getPassword() {
		return _password;
	}
	
	public boolean getSavePassword() {
		return _savePassword;
	}
	
	public boolean getUseSSL() {
		return _useSSL;
	}
	
	public String getURL() {
		return _url;
	}
	
	public void setLocationname(String _locationname) {
		this._locationname = _locationname;
	}

	public void setHostname(String _hostname) {
		this._hostname = _hostname;
	}

	public void setPort(Integer _port) {
		this._port = _port;
	}

	public void setUserId(String _userId) {
		this._userId = _userId;
	}

	public void setPassword(String _password) {
		this._password = _password;
	}
	
	public void setSavePassword(boolean value) {
		this._savePassword = value;
	}
	
	public void setUseSSL(boolean value) {
		this._useSSL = value;
	}
		
	public void setURL(String value) {
		this._url = value;
	}

	public List getRoles() {
		return roles;
	}

	public void setRoles(List roles) {
		this.roles = roles;
	}
	
}
