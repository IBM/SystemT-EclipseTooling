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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.IContainer;
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
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.wizards.Activator;

/**
 * This is new AQL Module wizard. Its role is to create a new aql module
 * resource in the provided container. If the container resource (a folder or a
 * project) is selected in the workspace when the wizard is opened, it will
 * accept it as the target container.
 */

public class NewAQLModuleWizard extends Wizard implements INewWizard {



	private NewAQLModuleWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for NewAQLModuleWizard.
	 */
	public NewAQLModuleWizard() {
		super();

		setNeedsProgressMonitor(true);
		setWindowTitle("New AQL Module");//$NON-NLS-1$
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		IProject project = ProjectUtils.getSelectedProject();
		try {
			if (project==null) {
				page = new NewAQLModuleWizardPage(selection);
				addPage(page);
				return;
			}
			if (!project.hasNature(Constants.PLUGIN_NATURE_ID)) {
				MessageDialog.openError(getShell(), Messages.NewAQLModuleWizard_ERROR, 
						MessageFormat.format (Messages.NewAQLModuleWizard_DO_NOT_HAVE_BI_NATURE,
						new Object[]{project.getName()}));
				return;
			}
		} catch (CoreException e) {
			Activator
			.getDefault()
			.getLog()
			.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
					.getMessage()));
		}
		PreferenceStore store = ProjectUtils.getPreferenceStore(project);
		if (store != null && store.getBoolean(Constants.MODULAR_AQL_PROJECT)) {
			page = new NewAQLModuleWizardPage(selection);
			addPage(page);

		} else {
			MessageDialog.openError(getShell(), Messages.NewAQLModuleWizard_ERROR, 
					MessageFormat.format (Messages.NewAQLModuleWizard_NOT_CONFIGURED_FOR_MODULAR_FEATURE, 
							new Object[]{project.getName()}));
			return;

		}

	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String moduleName = page.getModuleName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					doFinish(containerName, moduleName, monitor);
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
			MessageDialog.openError(getShell(), Messages.NewAQLModuleWizard_ERROR, 
					realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */

	private void doFinish(String containerName, String moduleName,
			IProgressMonitor monitor) throws CoreException {
		// create a module
		monitor.beginTask(Messages.NewAQLModuleWizard_CREATING_MODULE + moduleName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException(MessageFormat.format (Messages.NewAQLModuleWizard_CONTAINER_DO_NOT_EXIST,  
					new Object[]{containerName}));

		}

		IProject proj = ProjectUtils.getProject(containerName);
		String defaultSrcPath = ProjectUtils.getConfiguredModuleSrcPath (proj);

		if ( defaultSrcPath != null) {
  		IResource taModuleResource = root.findMember(new Path(defaultSrcPath
  				+ File.separator + moduleName));
  		if (taModuleResource != null
  				&& (!taModuleResource.exists() || !(taModuleResource instanceof IFolder))) {
  			throwCoreException(MessageFormat.format (Messages.NewAQLModuleWizard_MODULE_ALREADY_EXIST,
  					new Object[]{moduleName}));
  		}
  
  		IFolder folderResource = root.getFolder(new Path(defaultSrcPath
  				+ File.separator + moduleName));
  		folderResource.create(true, true, monitor);
  
  		monitor.worked(1);
  		
		} else {
		  throwCoreException (Messages.NewAQLWizardModulePage_MODULE_SRC_FOLDER_DETERMINATION_ERROR);
		}
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR,Activator.PLUGIN_ID
				, IStatus.OK,
				message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}
