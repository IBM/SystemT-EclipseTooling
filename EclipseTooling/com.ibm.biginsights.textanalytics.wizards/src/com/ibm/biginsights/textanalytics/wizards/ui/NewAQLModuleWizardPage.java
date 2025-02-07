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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.wizards.Activator;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (aql).
 */

public class NewAQLModuleWizardPage extends WizardPage {


	
	private Text containerText;
	private Text moduleText;

	private ISelection selection;
	
	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewAQLModuleWizardPage(ISelection selection) {
		super("NewAQLModuleWizardPage"); //$NON-NLS-1$
		setTitle(Messages.NewAQLModuleWizardPage_TITLE);
		setDescription(Messages.NewAQLModuleWizardPage_DESCRIPTION);
		setImageDescriptor(Activator.getImageDescriptor("/icons/AQLScript_wizard.png")); //$NON-NLS-1$
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.new_aqlModule"); //$NON-NLS-1$
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText(Messages.NewAQLModuleWizardPage_PROJECT);

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		

		Button button = new Button(container, SWT.PUSH);
		button.setText(Messages.NewAQLModuleWizardPage_BROWSE);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		
	
		label = new Label(container, SWT.NULL);
		label.setText(Messages.NewAQLModuleWizardPage_MODULE_NAME);

		moduleText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		moduleText.setLayoutData(gd);

		initialize();
		setPageComplete(false);
		
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		moduleText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IProjectNature) {
				containerText.setText(((IProjectNature) obj).getProject().getFullPath().toString());
			}else if (obj instanceof IJavaElement) {
				containerText.setText(((IJavaElement) obj).getJavaProject().getProject()
						.getFullPath().toString());
			}else if (obj instanceof IResource) {
				if (obj instanceof IProject){
					containerText.setText(((IContainer)obj).getFullPath().toString());
				}else if(obj instanceof IFolder){
					
					containerText.setText((((IFolder)obj).getProject()).getFullPath().toString());
				}
				else{
					containerText.setText(((IResource) obj).getParent().getFullPath().toString());
				}
				
			}
		}
		moduleText.setText(""); //$NON-NLS-1$
		
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), 
				new WorkbenchContentProvider());
		dialog.setTitle(Messages.NewAQLModuleWizardPage_PROJECTION_SELECTION);
		dialog.setMessage(Messages.NewAQLModuleWizardPage_PROJECTS);
		dialog.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IProject) {
					String projectName = ((IProject)element).getName();
					IProject project = ProjectUtils.getProject(projectName);
						try {
							return project.hasNature(Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject(project);
						} catch (CoreException e) {
							return false;
						}
				}
				return false;
			}
		});
		dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				if(result[0] instanceof IProject)
					containerText.setText("/" + ((IProject) result[0]).getName()); //$NON-NLS-1$
				else
					containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		setPageComplete(false);
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
			.findMember(new Path(getContainerName()));
		String moduleName = getModuleName();

		if (getContainerName().length() == 0) {
			updateStatus(Messages.NewAQLModuleWizardPage_SPECIFY_PROJECT); 
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus(Messages.NewAQLModuleWizardPage_PROJECT_MUST_EXIST);
			return;
		}
		if (!container.isAccessible()) {
			updateStatus(Messages.NewAQLModuleWizardPage_PROJECT_MUST_BE_WRITABLE);
			return;
		}
		if (moduleName.length() == 0) {
			updateStatus(Messages.NewAQLModuleWizardPage_SPECIFY_A_MODULE_NAME);
			return;
		}
		if (moduleName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus(Messages.NewAQLModuleWizardPage_MODULE_NAME_MUST_BE_VALID);
			return;
		}
		
		if (!ProjectUtils.isValidName(getModuleName())) {
			updateStatus(Messages.NewAQLModuleWizardPage_MODULE_NAME_VALIDATION);
			return;
		}
		
		if (ProjectUtils.isAQLKeyword (getModuleName())) {
      updateStatus(Messages.NewAQLModuleWizardPage_MODULE_NAME_IS_AQL_KEYWORD);
      return;
    }
		
		if (moduleName.contains(" ")) { //$NON-NLS-1$
	      updateStatus(Messages.NewAQLModuleWizardPage_MODULE_NAME_CONTAINS_SPACES);
          return;
        }
			
		IProject proj = ProjectUtils.getProject(getContainerName());
		String defaultSrcPath = ProjectUtils.getConfiguredModuleSrcPath (proj);
		if (null != defaultSrcPath) {
		  String fileFullPath = defaultSrcPath + File.pathSeparator + moduleName;
	    if(ResourcesPlugin.getWorkspace().getRoot()
	        .findMember(new Path(fileFullPath)) != null){
	      updateStatus(Messages.NewAQLModuleWizardPage_MODULE_ALREADY_EXIST);
	      return;
	    }
		} else {
		  updateStatus(Messages.NewAQLWizardModulePage_MODULE_SRC_FOLDER_DETERMINATION_ERROR);
		}

		
		
		setPageComplete(true);
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getModuleName() {
		return moduleText.getText();
	}
	
	
}
