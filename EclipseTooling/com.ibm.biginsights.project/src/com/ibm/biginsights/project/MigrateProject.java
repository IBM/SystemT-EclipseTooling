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

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;
import com.ibm.biginsights.project.wizard.AddBINatureWizard;

public class MigrateProject extends AbstractHandler {

	@SuppressWarnings("unused")


	private boolean isLaunchedFromTaskLauncher = false;
	
	public MigrateProject() {
		// zero-arg constructor for launch from task launcher		
	}
	
    // method called from popup menu when migrating project to V1.3
    // show wizard to pick the BI libraries
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IProject project = null;
		
		if (isLaunchedFromTaskLauncher) {		
			// launched from task launcher w/o context - show a dialog with a list of all projects that need to be migrated
			MigratableProjectsDialog dlg = new MigratableProjectsDialog(Activator.getActiveWorkbenchShell());
			if (dlg.open()==Window.OK && dlg.getResult().length==1) {				
				project = (IProject)dlg.getResult()[0];				
			}					
		}
		else {
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
				
			    if (selectedProject instanceof IProject) {
			          project = (IProject) selectedProject;
			    } else if (selectedProject instanceof IAdaptable) {
		            project = (IProject) ((IAdaptable) selectedProject).getAdapter(IProject.class);
		        }
			}
		}
		 
		if (project!=null) {
	    	MigrationTestResult result = isMigrationRequired(project);
	    	if (result.isMigrationRequired) {
	        	  Shell shell =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	        	  AddBINatureWizard wizard = new AddBINatureWizard(project, result.projectVersion, result.bigInsightsVersion);	    
	        	  WizardDialog dialog = new WizardDialog(shell, wizard);
	        	  dialog.create();
	        	  dialog.open();
        	  }	          
        }    
	    return null;
	}
	
	public boolean isEnabled() {
		isLaunchedFromTaskLauncher = false;
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
			if (selection instanceof TreeSelection)
				return ((TreeSelection)selection).size()==1;
			else {
				// when selection is null or StructuredSelection and is empty, command launched from task launcher
				isLaunchedFromTaskLauncher = selection==null || selection.isEmpty();
				return isLaunchedFromTaskLauncher;
			}
		}
		return false;
	}	
	
	public static MigrationTestResult isMigrationRequired(IProject project) {
		// return false if project is not even a BI project
		try {
			if (!project.hasNature(ProjectNature.NATURE_ID))
				return new MigrationTestResult(false, null, null);
		} catch (CoreException e1) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e1.getMessage()));
			return new MigrationTestResult(false, null, null);
		}
		
		// compare the current version of the project and the version of the installed BI plugins to determine if migration is necessary
		String biVersion = BIProjectPreferencesUtil.getInstalledBigInsightsVersion();
		
		if (biVersion!=null) {
			// in V1.2 there was no preferences file, always do migration
			File file = new File(project.getLocation() + File.separator + BIConstants.BIGINSIGHTS_PREF_FILE); 									
			if (!file.exists()) {
				return new MigrationTestResult(true, BIConstants.BIGINSIGHTS_VERSION_V12, biVersion);
			} 
			else {
				// project is higher than V1.2 - check version in .biginsights properties file
				if (file.exists()) {							
					String projectVersion = BIProjectPreferencesUtil.getBigInsightsProjectVersion(project);
					if (projectVersion!=null && !projectVersion.isEmpty()) {
						boolean isMigrationRequired = BIProjectPreferencesUtil.isLower(projectVersion, biVersion);
						return new MigrationTestResult(isMigrationRequired, projectVersion, biVersion);
					}
					else
						return new MigrationTestResult(true, BIConstants.BIGINSIGHTS_VERSION_V12, biVersion);
				}
			}
      	  	
		}

		// only get here if we can't get the biVersion or the project version
		return new MigrationTestResult(false, biVersion, biVersion);
	}

	/**
	 * Create missing Java source folders.<br>
	 * No action if the given project doesn't have Java nature.
	 * @param project
	 * @throws JavaModelException
	 * @throws CoreException
	 */
	public static void createMissingJavaSrcFolders (IProject project) throws CoreException {
    if (!project.hasNature (JavaCore.NATURE_ID))
    	return;

		boolean needRefresh = false;

		// Get the classpaths
		IJavaProject jPrj = JavaCore.create (project);
		IClasspathEntry[] cpEntries = jPrj.getRawClasspath ();

		// Create missing src folders.
		for (IClasspathEntry cpEntry : cpEntries) {
			if (cpEntry.getEntryKind () == IClasspathEntry.CPE_SOURCE) {
				IPath p = cpEntry.getPath ();
				IPath relPath = p.removeFirstSegments (1); 		// The first segment is the project, if not removed,
				IFolder folder = project.getFolder (relPath); // then getFolder() will return <project>/<project>/<src>.
				if (!folder.exists ()) {
					folder.create (true, true, null);
					needRefresh = true;
				}
			}
		}

		if (needRefresh)
			project.refreshLocal (IResource.DEPTH_INFINITE, null);
	}

	public static class MigrationTestResult {
		public boolean isMigrationRequired;
		public String projectVersion; // version with which the BI project was created
		public String bigInsightsVersion; // version of the BI tooling that is currently installed
		
		public MigrationTestResult(boolean isMigrationRequired, String projectVersion, String bigInsightsVersion) {
			this.isMigrationRequired = isMigrationRequired;
			this.projectVersion = projectVersion;
			this.bigInsightsVersion = bigInsightsVersion;
		}
	}

}
