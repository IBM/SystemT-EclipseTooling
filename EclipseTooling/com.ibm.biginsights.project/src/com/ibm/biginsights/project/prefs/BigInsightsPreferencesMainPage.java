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
package com.ibm.biginsights.project.prefs;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.util.BIConstants;

public class BigInsightsPreferencesMainPage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	private Button checkBoxJaqlMarkers;

	public BigInsightsPreferencesMainPage() {	
	}

	public BigInsightsPreferencesMainPage(String title) {
		super(title);
	}

	public BigInsightsPreferencesMainPage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.project.help.biginsights_preferences"); //$NON-NLS-1$
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);
		
		Group group = new Group(composite, SWT.NONE);
		group.setText(Messages.ProjectPreferences_Jaql_Settings); 
		group.setLayout(new GridLayout(1, false));
		group.setFont(parent.getFont());
    	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		gd.grabExcessHorizontalSpace=true;
		
		
		checkBoxJaqlMarkers = new Button(group, SWT.CHECK);
		checkBoxJaqlMarkers.setText(Messages.ProjectPreferences_Jaql_Settings_ProblemMarkers);
		
		//get checkbox saved setting
		boolean checkedStore = PlatformUI.getPreferenceStore().getBoolean(BIConstants.STORE_MARKER_KEY);
		checkBoxJaqlMarkers.setSelection(checkedStore);

		return composite;
	}
	
	private void saveSettings(){
		boolean showMarkers = checkBoxJaqlMarkers.getSelection();
		//save the setting
		PlatformUI.getPreferenceStore().setValue(BIConstants.STORE_MARKER_KEY, showMarkers);
		
		//notify any open editors
		if(showMarkers){			
			PlatformUI.getPreferenceStore().firePropertyChangeEvent(BIConstants.STORE_MARKER_KEY, Boolean.FALSE, Boolean.TRUE);
		}else{			
			PlatformUI.getPreferenceStore().firePropertyChangeEvent(BIConstants.STORE_MARKER_KEY, Boolean.TRUE, Boolean.FALSE);
		}
		
		//update menu item if showing
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command toggleMargersCommand = commandService.getCommand(BIConstants.MARKER_TOGGLE_COMMANDID);		

		State state = toggleMargersCommand.getState(BIConstants.MARKER_TOGGLE_STATE);
		Boolean checkedState = (Boolean)state.getValue();
		if( ! checkedState.equals(Boolean.valueOf(showMarkers)) ){
			state.setValue(showMarkers ? Boolean.TRUE : Boolean.FALSE);
			commandService.refreshElements(BIConstants.MARKER_TOGGLE_COMMANDID, null);
		}	
	}
	
	@Override
	protected void performApply() {
		saveSettings();
	}
	
	@Override
	protected void performDefaults() {
		checkBoxJaqlMarkers.setSelection(true);
		saveSettings();
		super.performDefaults();
    }
	
	@Override
	public boolean performOk() {
		performApply();
		return super.performOk();
	}
}
