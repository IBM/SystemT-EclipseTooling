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
package com.ibm.biginsights.project;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;
import com.ibm.biginsights.project.wizard.AddBINatureWizard;

@SuppressWarnings("restriction")
public class ProjectSupport extends AbstractHandler {	
	
	// create a new project, assign the BigInsights nature and process extension point to add additional natures
	// method called by performFinish() in NewWizard class 
    public static IProject createProject(String projectName, URI location, IClasspathEntry bigInsightsLibraries) {
        Assert.isNotNull(projectName);
        Assert.isTrue(projectName.trim().length() > 0);

        IProject baseProject = createBaseProject(projectName, location);        
        try {        	
        	addBIProjectNature(baseProject, bigInsightsLibraries);
        } catch (CoreException e) {
        	Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
            baseProject = null;
        }

        return baseProject;
    }
    
    // method to add BI project nature and adding the provided BI libraries
    // called from ProjectSupport.createProject and AddBINatureWizard.performFinish
    public static void addBIProjectNature(IProject project, IClasspathEntry bigInsightsLibraries) throws CoreException {        
    	// add BigInsights nature
        addNature(project, ProjectNature.NATURE_ID);
        // add BI libraries to project classpath
        addBigInsightsLibraryToProjectClasspath(project, bigInsightsLibraries);
    }
    
    // method to migrate existing BI project to a newer version
    public static void migrateProject(IProject project, IClasspathEntry bigInsightsLibraries, String oldVersion, String newVersion) throws CoreException {
    	if (oldVersion.equals(BIConstants.BIGINSIGHTS_VERSION_V12)) {
	    	// create default preferences 
	    	ProjectNature.createDefaultPreferences(project);
	    	// add Java nature (won't be added twice if it already exists)
		    ProjectNature.addJavaNature(project);	  
    	}

    	// add BI libraries to project classpath
        addBigInsightsLibraryToProjectClasspath(project, bigInsightsLibraries);

        // update the BI version in the properties file to the new version
        ProjectNature.setCurrentBIVersionInProperties(project);
        
        // add builders if old project wasn't a 2.0 project and new project is at least version 2
	    if (BIProjectPreferencesUtil.isLower(oldVersion, BIConstants.BIGINSIGHTS_VERSION_V2) && 
	    	BIProjectPreferencesUtil.isAtLeast(newVersion, BIConstants.BIGINSIGHTS_VERSION_V2))

	    // process extension points for migration
	    ProjectNature.processMigrateExtensions(project, oldVersion, newVersion);
	    project.refreshLocal (IResource.DEPTH_INFINITE, null); // Added to refresh the project after migration
    }

    public static void addBigInsightsLibraryToProjectClasspath(IProject project, IClasspathEntry bigInsightsLibraries) throws CoreException {
        // adding the nature added the default BI container - add the container with the right version
    	boolean needUpdate = true;
    	// if current classpath is set to DEFAULT and the new library version is the default version, don't change the classpath
    	// (will be the case for sample app projects)
    	// otherwise: if user changes the lib version to a non-default version, need to always change it to the new version
    	//            and it's ok if we override the DEFAULT container

        IJavaProject javaProject = null;
        
        if (project.isNatureEnabled(JavaCore.NATURE_ID)) {
        	javaProject = JavaCore.create(project);
        }
        if (javaProject!=null)
        {
        	// build new classpath w/o the default BI library container
        	Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
        	for (IClasspathEntry entry:javaProject.getRawClasspath())
        	{
        		if (!entry.getPath().toOSString().contains(BIConstants.CONTAINER_ID))
        		{
        			entries.add(entry);
        		}
        		else {
        			// check the version of the associated lib container and if it's the default version and
        			String newContainerVersion = bigInsightsLibraries.getPath().segment(1);
        			if (newContainerVersion.startsWith("v") || newContainerVersion.startsWith("V")) //$NON-NLS-1$ //$NON-NLS-2$
        				newContainerVersion = newContainerVersion.substring(1);
        			String entryPathVersion = entry.getPath().segment(1);
        			if (BIConstants.CONTAINER_ID_DEFAULT.equals(entryPathVersion) &&
        				newContainerVersion.equals(BigInsightsLibraryContainerInitializer.getInstance().getDefaultBigInsightsContainerVersion()))
        			{
        				needUpdate = false;
        			}
        		}
        	}
        	
        	if (needUpdate) {
	        	// add the BI container that the user selected
	        	if (bigInsightsLibraries!=null)
	            	entries.add(bigInsightsLibraries);
	        	javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), true, new NullProgressMonitor());
	        	project.touch(null);
	        	project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        	}
        	
        }    	
    }
    
    // create a basic project in the workspace
    private static IProject createBaseProject(String projectName, URI location) {
        IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

        if (!newProject.exists()) {
            URI projectLocation = location;
            IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
            if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) {
                projectLocation = null;
            }

            desc.setLocationURI(projectLocation);
            try {
                newProject.create(desc, null);
                if (!newProject.isOpen()) {
                    newProject.open(null);
                }
            } catch (CoreException e) {  
            	String msg = Messages.bind(Messages.PROBJECT_NAME_INVALID, e.getMessage());
				MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), Messages.LAUNCHSHORTCUT_ERROR_TITLE, null,  
						msg, MessageDialog.ERROR, 
						new String[]{Messages.CLOSE_BUTTON},0);
				dialog.open();
            }
        }

        return newProject;
    }

    // add any nature to a project
    public static void addNature(IProject project, String natureId) throws CoreException {
        if (!project.hasNature(natureId)) {
            IProjectDescription description = project.getDescription();
            String[] prevNatures = description.getNatureIds();
            String[] newNatures = new String[prevNatures.length + 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
            newNatures[prevNatures.length] = natureId;
            description.setNatureIds(newNatures);

            IProgressMonitor monitor = null;
            project.setDescription(description, monitor);            
        }
    }    
    
    // method called from popup menu when adding BigInsights nature to existing project
    // show wizard to pick the BI libraries
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {	
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = null;
		IWorkbenchPage page = null;
		if (workbench!=null) 
			window = workbench.getActiveWorkbenchWindow();	
		if (window!=null)
			page = window.getActivePage();
		ISelection selection = page.getSelection();
		if (selection instanceof TreeSelection) {
			Object selectedProject = ((TreeSelection) selection).iterator().next();
			IProject project = null;
		    if (selectedProject instanceof IProject) {
		          project = (IProject) selectedProject;
		    } else if (selectedProject instanceof IAdaptable) {
	            project = (IProject) ((IAdaptable) selectedProject).getAdapter(IProject.class);
	        }
		    if (project != null) {
		    	Shell shell =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		    	AddBINatureWizard wizard = new AddBINatureWizard(project);	    
		    	WizardDialog dialog = new WizardDialog(shell, wizard);
		    	dialog.create();
		    	dialog.open();
		    }		        	    
		}
	    return null;
	}

	public boolean isEnabled() {
		boolean result = false;
		// disable if more than one project is selected
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = null;
		IWorkbenchPage page = null;
		if (workbench!=null) 
			window = workbench.getActiveWorkbenchWindow();	
		if (window!=null)
			page = window.getActivePage();
		if (page!=null) {
			ISelection selection = page.getSelection();
			if (selection instanceof TreeSelection && (((TreeSelection)selection).size()==1)) {
				Object selectedProject = ((TreeSelection) selection).iterator().next();
				IProject project = null;
			    if (selectedProject instanceof IProject) {
			          project = (IProject) selectedProject;
			    } else if (selectedProject instanceof IAdaptable) {
		            project = (IProject) ((IAdaptable) selectedProject).getAdapter(IProject.class);
		        }
			    try {
			    	result = !project.hasNature(ProjectNature.NATURE_ID);
				} catch (CoreException e) {
					// result will be false
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}			
			}			
		}		 
		return result;
	}
}
