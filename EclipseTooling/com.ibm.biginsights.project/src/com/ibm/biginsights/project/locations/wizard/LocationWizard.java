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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.IPostServerCreation;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BigInsightsLocation;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;

public class LocationWizard extends Wizard implements INewWizard {

	private LocationWizardBasicDataPage _dataPage = null;
	private IBigInsightsLocation _updateLocation = null;
	
	public LocationWizard() {
		super();
		setNeedsProgressMonitor(true);
		_updateLocation = null;	
		this.setWindowTitle(Messages.LOCATIONWIZARD_TITLE_ADD);
	}
	
	public LocationWizard(IBigInsightsLocation location) {
		super();
		setNeedsProgressMonitor(true);
		this._updateLocation = location;
		this.setWindowTitle(Messages.LOCATIONWIZARD_TITLE_UPDATE);
	}
	
	public void addPages() {
	    super.addPages();
	    _dataPage = new LocationWizardBasicDataPage(Messages.LOCATIONWIZARD_PAGE_TITLE);
	    _dataPage.setImageDescriptor(Activator.getImageDescriptor("/icons/addBIServer.gif")); //$NON-NLS-1$
	    _dataPage.setDescription(Messages.LOCATIONWIZARD_PAGE_DESC);
	    addPage(_dataPage);	    
	    // if _updateLocation is not null, we are in update mode
	    if (_updateLocation!=null)
	    {
	    	_dataPage.initForUpdate(_updateLocation);
	    }
	}
	
	@Override
	public boolean performFinish() {
		// first do a test connection and show message if we can't connect
		if (_dataPage.handleTestConnection(false))
		{
			
			IBigInsightsLocation location = new BigInsightsLocation(_dataPage.getLocationName(), _dataPage.getURL(), 
					_dataPage.getUserId(), _dataPage.getPassword(), 
					_dataPage.getSavePassword());
			
			if (_updateLocation!=null) {
				// need to init with all values from existing connection, otherwise we lose some information
				location.initWithLocation(_updateLocation);
				location.setLocationName(_dataPage.getLocationName());
				location.setURL(_dataPage.getURL());
				location.setUserName(_dataPage.getUserId());
				location.setPassword(_dataPage.getPassword());
				location.setSavePassword(_dataPage.getSavePassword());				
			}
			
			location.getRoles().clear();
			location.getRoles().addAll(_dataPage.getRoles());
			
			boolean sucessful = false;
			if (_updateLocation==null)
				sucessful =  LocationRegistry.getInstance().addLocation(location);
			else
				sucessful =  LocationRegistry.getInstance().updateLocation(_updateLocation.getLocationName(), location);
			
			if(!sucessful){
				MessageDialog.openError(this.getShell(), Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_ERROR_TITLE, 
						Messages.LOCATIONWIZARDBASICDATAPAGE_ROLE_ERROR); 
			}
			else {
				
				if (_updateLocation==null)
				{
					// process extension point to create Hive Connection profile if needed.
					// only do this when creating a new BI server
					processPostServerCreationExtensions(location);
				}
				// switch to BI perspective if we are not in yet 
				BIProjectPreferencesUtil.switchToBigInsightsPerspective(this.getShell(), Messages.LOCATIONWIZARD_SWITCH_PERSPECTIVE_DESC);
			}
			
			return sucessful;
		}
		else 
			return false;
	}
	
	public boolean canFinish()
	{
		return super.canFinish();
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// nothing to do
		
	}
	
    public void processPostServerCreationExtensions(IBigInsightsLocation location)
    {
	    // process extension point to check if creating hive connection profile is needed
    	IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("com.ibm.biginsights.contributions"); //$NON-NLS-1$
		for (IConfigurationElement element:config)
		{			
			if (element.getAttribute("PostServerCreationClass")!=null) { //$NON-NLS-1$
				try {
					
					Object postServerCreationClass = (IPostServerCreation)element.createExecutableExtension("PostServerCreationClass"); //$NON-NLS-1$
					if (postServerCreationClass!=null && postServerCreationClass instanceof IPostServerCreation) { // migrationClass is optional
						((IPostServerCreation)postServerCreationClass).process(location);	
					}				 
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			}
		}
    	    	
    }	
    
}
