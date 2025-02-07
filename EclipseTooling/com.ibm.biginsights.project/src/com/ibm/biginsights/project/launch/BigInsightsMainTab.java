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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.ProjectNature;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;

public class BigInsightsMainTab extends JavaMainTab {
	
	protected Combo cbLocation;	
	protected Text txtDeleteDir; 
	protected boolean supportDeleteDirectory = true;
	
	
	public BigInsightsMainTab(String message) {
		super();
		setMessage(message);		
	}

	public BigInsightsMainTab(String message, boolean supportDeleteDirectory) {
		super();
		setMessage(message);	
		this.supportDeleteDirectory = supportDeleteDirectory;
	}

	protected void createLocationSelection(Composite parent) {
	    Label lblLocation = new Label(parent, SWT.NONE);        
        GridData gdLabel = new GridData(GridData.BEGINNING);
        lblLocation.setLayoutData(gdLabel);        
        lblLocation.setText(Messages.BIGINSIGHTSMAINTAB_LOCATION_LABEL);

        // add location selection
		cbLocation = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        GridData gdLocation = new GridData(GridData.FILL_HORIZONTAL);
        gdLocation.grabExcessHorizontalSpace = true;        
        cbLocation.setLayoutData(gdLocation);
        cbLocation.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	protected void createBigInsightsProperties(Composite parent) {
		if (supportDeleteDirectory) {
	        //delete directory
	        Label lblDeleteDir = new Label(parent, SWT.NONE);        
	        GridData gdLblDeleteDir= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
	        lblDeleteDir.setLayoutData(gdLblDeleteDir);        
	        lblDeleteDir.setText(Messages.BIGINSIGHTSMAINTAB_DELETEDIR_LABEL);
	        
	        txtDeleteDir = new Text(parent, SWT.BORDER);
	        GridData gdTxtDeleteDir = new GridData(SWT.FILL, SWT.BEGINNING, true, false);                    
	        txtDeleteDir.setLayoutData(gdTxtDeleteDir);      
	        txtDeleteDir.setToolTipText(Messages.BIGINSIGHTSMAINTAB_DELETEDIR_TOOLTIP);
	        txtDeleteDir.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updateLaunchConfigurationDialog();
				}
			});
		}        	
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);				
		new Label((Composite)getControl(), SWT.FILL);	
		
	   	Group grpBigInsights = new Group((Composite)getControl(), SWT.NONE);
	   	grpBigInsights.setLayout(new GridLayout(2, false));
	   	grpBigInsights.setText(Messages.BIGINSIGHTSMAINTAB_GROUP_LABEL);
	   	grpBigInsights.setFont(parent.getFont());
    	GridData gdGroup = new GridData(SWT.FILL,SWT.FILL,true,true);
    	grpBigInsights.setLayoutData(gdGroup);
    	
    	createLocationSelection(grpBigInsights);
               
        // init with default values
        // future: filter by version of the BI project of the selected project
        Collection<IBigInsightsLocation> locations = LocationRegistry.getInstance().getLocations();
        for (IBigInsightsLocation loc:locations) {
        	cbLocation.add(loc.getLocationDisplayString());
        }
        cbLocation.select(0);                     
        
        //Create a new composite and set a layout so that the parent layout is not affected.
        Composite cmpBigInsights = new Composite(grpBigInsights,SWT.FILL);
		GridLayout gdLayout = new GridLayout(2,false);
		cmpBigInsights.setLayout(gdLayout);
		GridData gdGrpBigInsights = new GridData(GridData.FILL_HORIZONTAL);
		gdGrpBigInsights.grabExcessHorizontalSpace = true;
		gdGrpBigInsights.horizontalSpan = 2;
		cmpBigInsights.setLayoutData(gdGrpBigInsights);

        createBigInsightsProperties(cmpBigInsights);
        
        boolean done=false;
        while(done){
        	Composite c = parent.getParent();
        	if(c==null){
        		done=true;
        	}else if(c instanceof ViewForm){
        		((ViewForm)c).computeSize(1280, 800, true);
        	}
        }
	}

	public void initializeFrom(ILaunchConfiguration config) {		
		super.initializeFrom(config);

		try {
			for (int i=0; i<cbLocation.getItemCount(); i++) {
				String locationDisplayName = cbLocation.getItem(i);
				IBigInsightsLocation location = LocationRegistry.getInstance().getLocationByDisplayName(locationDisplayName);				
					if (location.getLocationName().equals(config.getAttribute(BIConstants.BIGINSIGHTS_LOCATION_KEY,""))) { //$NON-NLS-1$
						cbLocation.select(i);
						break;
					}
			}			
			if (supportDeleteDirectory)
				txtDeleteDir.setText(config.getAttribute(BIConstants.JOB_DELETEDIR, "")); //$NON-NLS-1$
		} catch (CoreException e) {

		}
	}

	@SuppressWarnings("restriction")
	protected void handleProjectButtonSelected() {
		IProject project =  chooseJavaProject();
		if (project == null) {
			return;
		}
		
		String projectName = project.getName();
		fProjText.setText(projectName);			
	}
	
	/**
	 * chooses a project for the type of java launch config that it is
	 * @return
	 */
	protected IProject chooseJavaProject() {		
		ILabelProvider labelProvider= new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(Messages.BIGINSIGHTSMAINTAB_PROJ_DLG_TITLE);  
		dialog.setMessage(Messages.BIGINSIGHTSMAINTAB_PROJ_DLG_DESC); 
		
		dialog.setElements(getBIProjects());
		
		IProject BIproject= getBIProject();
		if (BIproject != null) {
			dialog.setInitialSelections(new Object[] { BIproject });
		}
		if (dialog.open() == Window.OK) {	
			IProject tt = (IProject) dialog.getFirstResult();
			return tt;
		}		
		return null;		
	}
	
	protected IProject[] getBIProjects()
	{
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ArrayList<IProject>tempArray = new ArrayList<IProject>();
		for (IProject project:projects)
		{
			try {
				if (project.hasNature(ProjectNature.NATURE_ID))				
					tempArray.add(project);				
			} catch (CoreException e) {
				// don't put the project into the list				
			}
		}
				
		IProject[]result = new IProject[tempArray.size()>0 ? tempArray.size() : 0];
		tempArray.toArray(result);
		return result;
		
	}
	
	@SuppressWarnings("restriction")
	protected IProject getBIProject() {
		String projectName = fProjText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		
		IProject result = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		try {
			if (!result.hasNature(ProjectNature.NATURE_ID))				
				result = null;				
		} catch (CoreException e) {
			// return null
			result = null;			
		}
		return result;	
	}	
	
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		// set location
		if (cbLocation.getSelectionIndex()>-1) {
			String locationDisplayName = cbLocation.getItem(cbLocation.getSelectionIndex());
			IBigInsightsLocation location = LocationRegistry.getInstance().getLocationByDisplayName(locationDisplayName);
			if (location!=null) {
				config.setAttribute(BIConstants.BIGINSIGHTS_LOCATION_KEY, location.getLocationName());
			}
		}
	
		// set delete dir
		if (supportDeleteDirectory && txtDeleteDir!=null)	
			config.setAttribute(BIConstants.JOB_DELETEDIR, txtDeleteDir.getText().trim());
		
		super.performApply(config);
	}
	
	public boolean isValid(ILaunchConfiguration launchConfig) {
		/* tab is invalid if:
		 * - super.isValid returns false (no project or no main class selected)
		 * - if no location is selected
		 * - project is not a BI project
		 * - BI version of selected project is not compatible with version of selected BI server
		 */			
		
		setErrorMessage(null);
		IProject project = fProjText.getText().isEmpty() ? null : BIProjectPreferencesUtil.getProject(fProjText.getText());
		boolean result = project!=null && project.exists();	// need to check for empty project because super.isValid allows empty project
		if (!result) {
			if (project!=null)
				setErrorMessage(Messages.bind(Messages.BIGINSIGHTSMAINTAB_PROJECT_INVALID, fProjText.getText()));
			else
				setErrorMessage(Messages.BIGINSIGHTSMAINTAB_PROJECT_MISSING);
		}
		else {
			// check if project is a BI project
			try {
				if (!project.hasNature(ProjectNature.NATURE_ID)) {
					setErrorMessage(Messages.BASETEMPLATEWIZARDPAGE_NOT_BI_PROJECT_ERROR);
					result = false;
				}
			} catch (CoreException e) {
			}
		}
		
		if (result) {
			result = verifyFileName(fMainText.getText().trim());
		}	 
		
		if (result && cbLocation.isEnabled()) { // don't check server selection if it's disabled (will be the case in local mode)
			// only check for selected location if otherwise the page is valid
			result = cbLocation.getSelectionIndex()>-1;
			if (!result)
				setErrorMessage(Messages.BIGINSIGHTSMAINTAB_LOCATION_MISSING);
			else {
				// if a location is selected, check if version matches the BI version associated with the project
				String locationDisplayName = cbLocation.getItem(cbLocation.getSelectionIndex());
				IBigInsightsLocation location = LocationRegistry.getInstance().getLocationByDisplayName(locationDisplayName);
				if (!BIProjectPreferencesUtil.areProjectAndServerVersionsEqual(project, location)) {
					result = false;
					setErrorMessage(Messages.BIGINSIGHTSMAINTAB_WRONG_SERVER_VERSION);
				}
				else
					result = verifyLocation(location);
			}
		}			
		
		return result;
		
	}
	
	protected boolean verifyFileName(String fileName) {	
		boolean result = true;
		if (fileName.length() == 0) {
			setErrorMessage(LauncherMessages.JavaMainTab_Main_type_not_specified_16); // checks main class 
			result = false;
		}
		return result;
	}
	
	protected boolean verifyLocation(IBigInsightsLocation location) {
		return true;
	}
	
}
