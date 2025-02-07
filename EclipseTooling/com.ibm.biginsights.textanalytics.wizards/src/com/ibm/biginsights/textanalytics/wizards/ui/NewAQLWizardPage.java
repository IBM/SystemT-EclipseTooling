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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
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
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.wizards.Activator;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (aql).
 */

public class NewAQLWizardPage extends WizardPage {


	
	private Text containerText;
	private static final String PATH_SEPERATOR = "/";//$NON-NLS-1$
	private Text fileText;

	private ISelection selection;
	
	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewAQLWizardPage(ISelection selection) { //This is supposed to work with null for ISelection too.
		super("NewAQLFileWizardPage"); //$NON-NLS-1$
		setTitle(Messages.NewAQLWizardPage_TITLE);
		setDescription(Messages.NewAQLWizardPage_DESCRIPTION);
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.new_aqlfile"); //$NON-NLS-1$
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText(Messages.NewAQLWizardPage_CONTAINER);

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		

		Button button = new Button(container, SWT.PUSH);
		button.setText(Messages.NewAQLWizardPage_BROWSE);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText(Messages.NewAQLWizardPage_FILE_NAME);

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);

		initialize();
		setPageComplete(false);
		
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		fileText.addModifyListener(new ModifyListener() {
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
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			}
		}
		fileText.setText(""); //$NON-NLS-1$
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {

		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
				getShell(), new WorkbenchLabelProvider(),
				new WorkbenchContentProvider());
		dialog.setTitle(Messages.NewAQLWizardPage_PROJECT_SELECTION);
		dialog.setMessage(Messages.NewAQLWizardPage_PROJECTS);
		dialog.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IProject) {
					String projectName = ((IProject) element).getName();
					IProject project = ProjectUtils.getProject(projectName);
					try {
						return project.hasNature(Constants.PLUGIN_NATURE_ID) && !ProjectUtils.isModularProject(project);
					} catch (CoreException e) {
						return false;
					}
				}
				if (element instanceof IFolder) {
					IProject project = ((IFolder) element).getProject();
					
					try {
						return project.hasNature(Constants.PLUGIN_NATURE_ID) && !ProjectUtils.isModularProject(project);
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
				if (result[0] instanceof IProject)
					containerText.setText(PATH_SEPERATOR
							+ ((IProject) result[0]).getName());
				else
					containerText.setText(((IFolder) result[0]).getFullPath().toString());
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
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus(Messages.NewAQLWizardPage_SPECIFY_A_CONTAINER);
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus(Messages.NewAQLWizardPage_SPECIFY_EXISTING_CONTAINER);
			return;
		}
		if (!container.isAccessible()) {
			updateStatus(Messages.NewAQLWizardPage_PROJECT_MUST_BE_WRITABLE);
			return;
		}
		if (fileName.length() == 0) {
			updateStatus(Messages.NewAQLWizardPage_SPECIFY_FILE_NAME);
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus(Messages.NewAQLWizardPage_SPECIFY_VALID_FILE_NAME);
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		int firstDot = fileName.indexOf ('.');
		
		//check if only one "." is present
		if(dotLoc != firstDot)
		{
      updateStatus(Messages.NewAQLWizardPage_SPECIFY_VALID_FILE_NAME);
      return;
		}
		
		if(dotLoc == -1){
			updateStatus(Messages.NewAQLWizardPage_EXTENSION_SHOULD_BE_AQL);
			return;
		}
		
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			String name = fileName.substring (0,dotLoc);
			if (name.contains(" ")) { //$NON-NLS-1$
				updateStatus(Messages.NewAQLWizardPage_FILE_NAME_CANNOT_CONTAIN_SPACES);
				return;
			}
			if (!ProjectUtils.isValidName(name)) {
				updateStatus(Messages.NewAQLWizardModulePage_FILENAME_NOT_VALID);
				return;
			}
			if ("aql".equals(ext) == false) {//$NON-NLS-1$
				updateStatus(Messages.NewAQLWizardPage_FILE_NAME_EXTENSION_MUST_BE_AQL);
				return;
			}			
		}		
		if (dotLoc == 0) {
      updateStatus(Messages.NewAQLWizardPage_FILE_NAME_CANNOT_BE_EMPTY);
        return;
    }
		
		String fileFullPath = container.getFullPath().toString()+"/"+fileName; //$NON-NLS-1$
		if(ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(fileFullPath)) != null){
			updateStatus(Messages.NewAQLWizardPage_FILE_ALREADY_EXIST);
			return;
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

	public String getFileName() {
		return fileText.getText();
	}
	
	@Override
	public boolean canFlipToNextPage ()
	{
	  //Always return false as this page will be the last page.
    //This would disable the next button on this page.
	  return false;
	}
	
	/**
   * Sets the project field of the page if the given project is a text analytics project.
   * @param proj
   */
	void setProjectSelection(IProject proj) {
    try {
      if (proj != null && proj.isOpen () && proj.hasNature (Constants.PLUGIN_NATURE_ID)) {
        containerText.setText (proj.getName ());
      }
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.NewAQLWizardPage_PROJECT_NATURE_DETERMINATION_ERROR);
    }
  }
}
