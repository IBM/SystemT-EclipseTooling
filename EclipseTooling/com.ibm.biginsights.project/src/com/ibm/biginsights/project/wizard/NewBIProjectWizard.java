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

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.ProjectSupport;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;

public class NewBIProjectWizard extends Wizard implements INewWizard, IExecutableExtension {

	private IConfigurationElement _configurationElement;
	private WizardNewProjectCreationPage _namePage;
//	private LocationPage _locationPage;
	private BigInsightsLibraryContainerPage _libPage;

	protected String projectName;
	
	public NewBIProjectWizard() {
		setWindowTitle(Messages.NewWizard_Title);
		setNeedsProgressMonitor(true);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// nothing to do

	}

	@Override
	public boolean performFinish() {
		
		IRunnableWithProgress job = new IRunnableWithProgress()
		{

			@Override
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				
				monitor.beginTask(Messages.NewWizard_CreateJob, IProgressMonitor.UNKNOWN);
				
				String name = _namePage.getProjectName();
			    setProjectName(name);
			    URI location = null;
			    if (!_namePage.useDefaults()) {
			        location = _namePage.getLocationURI();
			    } // else location == null	    	    

			    IProject project = ProjectSupport.createProject(name, location, _libPage.getSelection());
			    if(project == null){
			    	Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.ERROR_CREATING_PROJECT));
			    	throw new RuntimeException(Messages.ERROR_CREATING_PROJECT);
			    }
			    //setting default encoding to UTF-8 during BI project creation
			    try {
					project.setDefaultCharset(BIConstants.UTF8, null);
				} catch (CoreException e1) {
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e1.getMessage()));
					throw new RuntimeException(e1.getMessage());
				}
			    BasicNewProjectResourceWizard.updatePerspective(_configurationElement);
			    
				// switch to BI perspective if we are not in yet
			    BIProjectPreferencesUtil.switchToBigInsightsPerspective(
			    		Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), 
			    		Messages.NEWBIPROJECTWIZARD_DESC);

				
				monitor.done();
				return;
				
			}
			
		};
		
		try {
			getContainer().run(false, false, job);
		} catch (InvocationTargetException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		} catch (InterruptedException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}		
	    
	    return true;
	}

	public void addPages() {
	    super.addPages();
	    _namePage = new ProjectCreationPage(Messages.NewWizard_Page1_Title);
	    _namePage.setTitle(Messages.NewWizard_Page1_Title);
	    _namePage.setDescription(Messages.NewWizard_Page1_Desc);
	    addPage(_namePage);
	    	    
	    _libPage = new BigInsightsLibraryContainerPage();	    	    
	    addPage(_libPage);
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
		_configurationElement = config;
		
	}

	public String getProjectName(){
		return projectName;
	}
	
	public void setProjectName(String projectName){
		this.projectName = projectName;
	}
}
