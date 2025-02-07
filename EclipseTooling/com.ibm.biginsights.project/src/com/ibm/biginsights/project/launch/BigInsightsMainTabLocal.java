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
package com.ibm.biginsights.project.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;
import com.ibm.biginsights.project.util.BIConstants;

public class BigInsightsMainTabLocal extends BigInsightsMainTab {
	
	private Label lblLocation;
	protected Button btnCluster;
	protected Button btnLocal;
	private String _mode;
	private FormData fd_grpExecutionMode;
	private FormData fd_cbLocation;
	
	public BigInsightsMainTabLocal(String message, String mode, boolean supportDeleteDirectory) {
		super(message, supportDeleteDirectory);
		this._mode = mode;
	}

	protected void createLocationSelection(final Composite parent) {
		//Create a new composite as the composite parent is not in FormLayout
		Composite cmpExecutionMode = new Composite(parent,SWT.NONE);
		cmpExecutionMode.setLayout(new FormLayout());
		Group grpExecutionMode = new Group(cmpExecutionMode, SWT.NONE);
		grpExecutionMode.setText(Messages.BIGINSIGHTSMAINTAB_EXEC_MODE);
		grpExecutionMode.setLayout(new FormLayout());
		fd_grpExecutionMode = new FormData();
		fd_grpExecutionMode.bottom = new FormAttachment(0, 110);
		//fd_grpExecutionMode.right = new FormAttachment(0, 1024);
		fd_grpExecutionMode.top = new FormAttachment(0, 10);
		fd_grpExecutionMode.left = new FormAttachment(0, 0);
		grpExecutionMode.setLayoutData(fd_grpExecutionMode);
		
		btnCluster = new Button(grpExecutionMode, SWT.RADIO);
		FormData fd_btnCluster = new FormData();
		fd_btnCluster.top = new FormAttachment(0, 8);
		fd_btnCluster.left = new FormAttachment(0, 7);
		btnCluster.setLayoutData(fd_btnCluster);
		btnCluster.setText(Messages.BIGINSIGHTSMAINTAB_EXEC_MODE_CLUSTER);
		if (ILaunchManager.RUN_MODE.equals(this._mode))
    		btnCluster.setSelection(true); // by default we run on the cluster in run mode
		btnCluster.setEnabled(ILaunchManager.RUN_MODE.equals(this._mode));
		btnCluster.addSelectionListener( new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {//nothing
			}			
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableServerSelection(btnCluster.getSelection());
				updateLaunchConfigurationDialog();
			}
        });
		
		btnLocal = new Button(grpExecutionMode, SWT.RADIO);
		FormData fd_btnLocal = new FormData();
		fd_btnLocal.top = new FormAttachment(0, 55);
		fd_btnLocal.left = new FormAttachment(0, 7);
		btnLocal.setLayoutData(fd_btnLocal);
		btnLocal.setText(Messages.BIGINSIGHTSMAINTAB_EXEC_MODE_LOCAL);
		btnLocal.addSelectionListener( new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {//nothing
			}			
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableServerSelection(!btnLocal.getSelection());
				updateLaunchConfigurationDialog();
			}
        });

		
		lblLocation = new Label(grpExecutionMode, SWT.NONE);
		FormData fd_lblLocation = new FormData();
		//fd_lblLocation.right = new FormAttachment(100,0);
		fd_lblLocation.top = new FormAttachment(0, 30);
		fd_lblLocation.left = new FormAttachment(0, 32);
		lblLocation.setLayoutData(fd_lblLocation);
		lblLocation.setText(Messages.BIGINSIGHTSMAINTAB_LOCATION_LABEL);
		
		cbLocation = new Combo(grpExecutionMode, SWT.READ_ONLY|SWT.BORDER|SWT.SINGLE);
		fd_cbLocation = new FormData();
		fd_cbLocation.right = new FormAttachment(95,0);
		fd_cbLocation.top = new FormAttachment(0, 27);
		fd_cbLocation.left = new FormAttachment(lblLocation,5,SWT.RIGHT);
		cbLocation.setLayoutData(fd_cbLocation);
		cbLocation.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		
		parent.addListener(SWT.Resize, new Listener(){

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				int width = parent.getClientArea().width;
				//fd_grpExecutionMode.right = new FormAttachment(0,width -20);
				fd_grpExecutionMode.width = width-20;
				fd_cbLocation.width = fd_grpExecutionMode.width -20;
				parent.redraw();
				
			}
			
		});
	}
	
	protected void enableServerSelection(boolean enable) {
		lblLocation.setEnabled(enable);
		cbLocation.setEnabled(enable);
	}
	
	public void initializeFrom(ILaunchConfiguration config) {		
		super.initializeFrom(config);		
		try {
			if (ILaunchManager.RUN_MODE.equals(this._mode)) {
				// execution mode is not set when creating a new run config; by default assume we run on cluster 
				String execMode = config.getAttribute(BIConstants.JOB_EXECUTION_MODE, BIConstants.JOB_EXECUTION_MODE_CLUSTER);
				btnCluster.setSelection((BIConstants.JOB_EXECUTION_MODE_CLUSTER.equals(execMode)));
				enableServerSelection(BIConstants.JOB_EXECUTION_MODE_CLUSTER.equals(execMode));
				btnLocal.setSelection(BIConstants.JOB_EXECUTION_MODE_LOCAL.equals(execMode));
			}	
			else {
				enableServerSelection(false);
				btnLocal.setSelection(true);
			}
		} catch (CoreException e) {

		}
	}
		
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		// set execution mode and server in case of cluster execution
		if (ILaunchManager.RUN_MODE.equals(this._mode)) {
			if (btnCluster.getSelection()) {
				config.setAttribute(BIConstants.JOB_EXECUTION_MODE, BIConstants.JOB_EXECUTION_MODE_CLUSTER);
				// set location
				if (cbLocation.getSelectionIndex()>-1) {
					String locationDisplayName = cbLocation.getItem(cbLocation.getSelectionIndex());
					IBigInsightsLocation location = LocationRegistry.getInstance().getLocationByDisplayName(locationDisplayName);
					if (location!=null) {
						config.setAttribute(BIConstants.BIGINSIGHTS_LOCATION_KEY, location.getLocationName());
					}
				}
			}
			else {
				config.setAttribute(BIConstants.JOB_EXECUTION_MODE, BIConstants.JOB_EXECUTION_MODE_LOCAL);			
			}
		}				
		
		super.performApply(config);
	}
	
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return super.isValid(launchConfig);
	}
}
