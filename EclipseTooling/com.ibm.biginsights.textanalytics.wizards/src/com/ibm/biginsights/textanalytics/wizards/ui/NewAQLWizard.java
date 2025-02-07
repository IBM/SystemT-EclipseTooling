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
package com.ibm.biginsights.textanalytics.wizards.ui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.ibm.biginsights.textanalytics.aql.editor.syntax.AQLSyntaxElements;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * This is new AQL file wizard. Its role is to create a new aql file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "aql". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class NewAQLWizard extends Wizard implements INewWizard {



	NewAQLWizardPage page = null;
	private ISelection selection;
	NewAQLWizardModulePage modularPage = null;
	NewAQLWizardProjectSelectionPage projectSelectionPage = null;
	

	/**
	 * Constructor for NewAQLWizard.
	 */
	public NewAQLWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("New AQL script");//$NON-NLS-1$
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		IProject project = ProjectUtils.getSelectedProject();
		if (project==null) {
		  /*
       * When no project has been selected in project explorer or package explorer,
       * first show a wizard page for project selection.
       * Add both NewAQLWizardModulePage(#1) and NewAQLWizardPage(#2) instances as pages for the wizard.
       * Based on project selected on first page, go to either #1 or #2
       */
		  projectSelectionPage = new NewAQLWizardProjectSelectionPage();
		  addPage(projectSelectionPage);
		  modularPage = new NewAQLWizardModulePage(null);
		  page = new NewAQLWizardPage(null);
		  addPage(page);
		  addPage(modularPage);
		  return;
		}
		if(ProjectUtils.isModularProject(project)){
			modularPage = new NewAQLWizardModulePage(selection);
			addPage(modularPage);
		}else{
			page = new NewAQLWizardPage(selection);
			addPage(page);
		}
	}
	
	@Override
	public boolean canFinish ()
	{
	  IProject project = ProjectUtils.getSelectedProject (); //Since addPages uses this to determine if a project has been selected, using it here too
	  if (project != null) {
	    return super.canFinish ();
	  } else {
	    /*
	     * When no project has been selected in project explorer or package explorer,
	     * expect one of NewAQLWizardModulePage or NewAQLWizardPage's isPageComplete()
	     * to return true when correct data has been provided.
	     */
	    return modularPage.isPageComplete () || page.isPageComplete ();
	  }
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		IProject project = ProjectUtils.getSelectedProject();
		if (project != null) {
  		boolean isModularProject = ProjectUtils.isModularProject(project);
  		if(isModularProject)
  			return processFinish();
  		else
  			return processNonModularFinish();
		} else {
		  /*
		   * When no project has been selected in project/package explorer,
		   * the project selection page would have been presented. Based on the 
		   * selection there wizard would have proceeded to either NewAQLWizardPage
		   * or NewAQLWizardModulePage instance. By default, their isPageComplete()
		   * would be false. We can rely on this method to determine which one has
		   * user input.
		   */
		  if (page != null && page.isPageComplete ()) {
		    return processNonModularFinish();
		  } else if (modularPage != null && modularPage.isPageComplete ()){
		    return processFinish();
		  } else {
		    return false;
		  }
		}
	}
	
	//Start of Non Modular Code
	private boolean processNonModularFinish(){

		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doNonModularFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), Messages.NewAQLWizard_ERROR, realException.getMessage());
			return false;
		}
		return true;
	
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doNonModularFinish(
		String containerName,
		String fileName,
		IProgressMonitor monitor)
		throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2); //$NON-NLS-1$
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException(MessageFormat.format (Messages.NewAQLWizard_CONTAINER_DO_NOT_EXIST,
						new Object[]{containerName}));
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(null);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");//$NON-NLS-1$
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
	//End of Non Modular Code
	
	private boolean processFinish(){

		final String containerName = modularPage.getContainerName();
		final String fileName = modularPage.getFileName();
		final String moduleName = modularPage.getModuleName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, moduleName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), Messages.NewAQLWizard_ERROR, realException.getMessage());
			return false;
		}
		return true;
	
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */
	private void doFinish(
			String containerName,
			String moduleName,
			String fileName,
			IProgressMonitor monitor)
			throws CoreException {
			// create a sample file
			monitor.beginTask("Creating " + fileName + " in Module " + moduleName, 2); //$NON-NLS-1$ //$NON-NLS-2$
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource resource = root.findMember(new Path(containerName));
			if (!resource.exists() || !(resource instanceof IContainer)) {
				throwCoreException(MessageFormat.format (Messages.NewAQLWizard_CONTAINER_DO_NOT_EXIST,
						new Object[]{containerName}));
			}
			String moduleSrcPath = ProjectUtils.getConfiguredModuleSrcPath(containerName);
			if ( null == moduleSrcPath) {
			  throwCoreException(MessageFormat.format (Messages.NewAQLWizardModulePage_MODULE_SRC_FOLDER_DETERMINATION_ERROR,
          new Object[]{moduleName}));
			}
			IResource moduleResource = root.findMember(new Path(moduleSrcPath +File.separator+ moduleName));
			if (moduleResource == null || !moduleResource.exists() || !(moduleResource instanceof IFolder)) {
				IFolder folderResource = root.getFolder(new Path(ProjectUtils.getConfiguredModuleSrcPath(containerName)+File.separator+ moduleName));
				folderResource.create(true, true, monitor);
				moduleResource = folderResource;
			}
			
			IFolder folderResource = (IFolder) moduleResource;
			
			final IFile file = folderResource.getFile(new Path(fileName));
			try {
				InputStream stream = openContentStream(moduleName);
				if (file.exists()) {
					file.setContents(stream, true, true, monitor);
				} else {
					file.create(stream, true, monitor);
				}
				stream.close();
			} catch (IOException e) {
			}
			monitor.worked(1);
			monitor.setTaskName("Opening file for editing...");//$NON-NLS-1$
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage page =
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, file, true);
					} catch (PartInitException e) {
					}
				}
			});
			monitor.worked(1);
		}

	
	/**
	 * We will initialize file contents with a sample text.
	 */

	private InputStream openContentStream(String moduleName) {
		String contents = ""; //$NON-NLS-1$
		List<String> keyWordList = Arrays.asList (AQLSyntaxElements.KEYWORDS);
		if(moduleName != null){
			if(moduleName.indexOf('.') != -1 || keyWordList.contains (moduleName))
				contents = "module \"" + moduleName + "\"; \n";//$NON-NLS-1$ //$NON-NLS-2$
			else
				contents = "module " + moduleName + "; \n";//$NON-NLS-1$ //$NON-NLS-2$
		}
		 contents +=
			"-- TODO: Add AQL content here \n";//$NON-NLS-1$
		
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "com.ibm.biginsights.textanalytics.wizards", IStatus.OK, message, null); //$NON-NLS-1$
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
