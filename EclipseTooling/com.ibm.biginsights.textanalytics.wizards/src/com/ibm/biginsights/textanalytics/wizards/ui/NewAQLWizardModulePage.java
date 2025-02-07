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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.wizards.Activator;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (aql).
 */

public class NewAQLWizardModulePage extends WizardPage {


	
	private static final ILog logger = LogUtil
	.getLogForPlugin(Activator.PLUGIN_ID);

	private static final String PATH_SEPERATOR = "/";//$NON-NLS-1$
	private Text containerText;
	private Text moduleText;
	private Text fileText;

	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewAQLWizardModulePage(ISelection selection) { //This is supposed to work with null for ISelection too.
		super("NewAQLFileWizardPage"); //$NON-NLS-1$
		setTitle(Messages.NewAQLWizardModulePage_TITLE);
		setDescription(Messages.NewAQLWizardModulePage_DESCRIPTION);
		setImageDescriptor(Activator.getImageDescriptor("/icons/AQLScript_wizard.png")); //$NON-NLS-1$
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		PlatformUI
		.getWorkbench()
		.getHelpSystem()
		.setHelp(parent,
				"com.ibm.biginsights.textanalytics.tooling.help.new_aqlfile");//$NON-NLS-1$
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText(Messages.NewAQLWizardModulePage_PROJECT);

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);

		Button button = new Button(container, SWT.PUSH);
		button.setText(Messages.NewAQLWizardModulePage_BROWSE);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText(Messages.NewAQLWizardModulePage_MODULE_NAME);
		moduleText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		moduleText.setLayoutData(gd);
		Button moduleButton = new Button(container, SWT.PUSH);
		moduleButton.setText(Messages.NewAQLWizardModulePage_BROWSE);
		moduleButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleModuleBrowse();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText(Messages.NewAQLWizardModulePage_FILE_NAME);

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
		moduleText.addModifyListener(new ModifyListener() {
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

	public String getModuleName() {
		return moduleText.getText();
	}

	private void handleModuleBrowse() {

		LabelProvider labelProvider = new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				Image moduleWithAQLFileImage = Activator.getImageDescriptor(
				"/icons/Module.png").createImage(); //$NON-NLS-1$

				Image moduleWithoutAQLFileImage = Activator.getImageDescriptor(
				"/icons/ModulewithNoAQL.png").createImage(); //$NON-NLS-1$

				if (element instanceof IFolder) {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
					.getRoot();
					IFolder pathRes = root.getFolder(((IFolder) element)
							.getFullPath());

					IResource[] subFolderResource = null;
					try {
						subFolderResource = pathRes.members();
					} catch (CoreException e) {
						logger.logError(e.getMessage());
					}
					for (IResource iResource : subFolderResource) {
						if (iResource instanceof IFile
								&& Constants.AQL_FILE_EXTENSION_STRING
								.equals(iResource.getFileExtension()))
							return moduleWithAQLFileImage;
					}

					return moduleWithoutAQLFileImage;
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				String name = super.getText(element);
				name = name.substring(name.lastIndexOf(PATH_SEPERATOR) + 1);
				return name;
			}

		};

		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
				getShell(), labelProvider, new WorkbenchContentProvider());
		dialog.setTitle(Messages.NewAQLWizardModulePage_MODULE_SELECTION);
		dialog.setMessage(Messages.NewAQLWizardModulePage_MODULES);
		dialog.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IFolder) {
					String name = ((IFolder) element).getName();
					String path = ((IFolder) element).getFullPath().toString();

					String defaultSrcPath = ProjectUtils
					.getConfiguredModuleSrcPath(((IFolder) element)
							.getProject().getName());

					if (defaultSrcPath != null && path.indexOf(defaultSrcPath) != -1
							&& path.length() > defaultSrcPath.length()) {
						IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
						.getRoot();
						IFolder folderResource = root.getFolder(new Path(
								defaultSrcPath));
						try {
							IResource[] subFolderResource = folderResource
							.members();
							for (IResource iResource : subFolderResource) {
								if (iResource instanceof IFolder
										&& iResource.getName().equals(name)) {
									return true;
								}
							}

						} catch (CoreException e) {
							logger.logError(e.getMessage());
						}
					}

					if (defaultSrcPath != null && defaultSrcPath.indexOf(path) != -1) {
						return true;
					}

				}

				return false;
			}

		});

		// Here we are creating Input only for the folders under Module Src Path
		IProject proj = ProjectUtils.getProject(getContainerName());
		String moduleSrcPath = ProjectUtils.getConfiguredModuleSrcPath(
      proj.getName());
		//If the getDefaultModuleSrcPath is null, then we update the status message in the NewAQLWizardPage and log the status.
		if (moduleSrcPath == null) {
		  updateStatus(Messages.NewAQLWizardModulePage_MODULE_SRC_FOLDER_DETERMINATION_ERROR);
		  logger.logError (Messages.NewAQLWizardModulePage_MODULE_SRC_FOLDER_DETERMINATION_ERROR);
		  return;
		}
		String relPath = moduleSrcPath.replaceFirst(PATH_SEPERATOR, "");//$NON-NLS-1$
		relPath = relPath
		.replaceFirst(
				getContainerName()
				.replaceFirst(PATH_SEPERATOR, "").concat(PATH_SEPERATOR), "");//$NON-NLS-1$ //$NON-NLS-2$

		dialog.setInput(proj.getFolder(relPath));
		dialog.setValidator(new FileSelectionValidator());
		if (dialog.open() == Window.OK) {

			Object[] result = dialog.getResult();
			if (result.length == 1) {

				moduleText.setText(((IFolder) result[0]).getName());
			}
		}
	}

	class FileSelectionValidator implements ISelectionStatusValidator {
		@Override
		public IStatus validate(Object[] element) {
			Status fCurrStatus = null;

			if (element != null && element.length > 0
					&& element[0] instanceof IProject) {
				fCurrStatus = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						IStatus.ERROR, Messages.NewAQLWizardModulePage_SELECT_VALID_PROJECT, null);
			} else if (element != null && element.length > 0
					&& element[0] instanceof IFolder) {
				String path = ((IFolder) element[0]).getFullPath()
				.toPortableString();
				String srcPath = ProjectUtils
				.getConfiguredModuleSrcPath(getContainerName());
				if (srcPath == null) {
				  fCurrStatus = new Status(IStatus.ERROR,
          Activator.PLUGIN_ID, IStatus.ERROR,Messages.NewAQLWizardModulePage_MODULE_SRC_FOLDER_DETERMINATION_ERROR, null);
				} else if (path.indexOf(srcPath) != -1
						&& path.length() <= srcPath.length()) {
					fCurrStatus = new Status(IStatus.ERROR,
							Activator.PLUGIN_ID, IStatus.ERROR,
							Messages.NewAQLWizardModulePage_SELECT_VALID_MODULE, null);
				} else {
					fCurrStatus = new Status(IStatus.INFO, Activator.PLUGIN_ID,
							IStatus.INFO, "", null);//$NON-NLS-1$
				}
			} else {

				fCurrStatus = new Status(IStatus.INFO, Activator.PLUGIN_ID,
						IStatus.INFO, "", null);//$NON-NLS-1$
			}
			return fCurrStatus;
		}
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
				containerText.setText(((IProjectNature) obj).getProject()
						.getFullPath().toString());
			} else if (obj instanceof IJavaElement) {
				containerText.setText(((IJavaElement) obj).getJavaProject()
						.getProject().getFullPath().toString());
			} else if (obj instanceof IResource) {
				if (obj instanceof IProject) {
					containerText.setText(((IContainer) obj).getFullPath()
							.toString());
				} else if (obj instanceof IFolder) {
					String projectName = (((IFolder) obj).getProject())
					.getFullPath().toString();
					String moduleName = ((IFolder) obj).getName();
					containerText.setText((((IFolder) obj).getProject())
							.getFullPath().toString());
					if(ProjectUtils.isValidModule(projectName, moduleName)) {
						moduleText.setText(moduleName);
					}
					
				} else {
					IProject proj = ProjectUtils
					.getProjectForEditor(((IResource) obj)
							.getFullPath().toOSString());
					containerText.setText(proj.getName());
				}

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
		dialog.setTitle(Messages.NewAQLWizardModulePage_PROJECT_SELECTION);
		dialog.setMessage(Messages.NewAQLWizardModulePage_PROJECTS);
		dialog.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IProject) {
					String projectName = ((IProject) element).getName();
					IProject project = ProjectUtils.getProject(projectName);
					try {
						return project.hasNature(Constants.PLUGIN_NATURE_ID)
						&& ProjectUtils.isModularProject(project);
					} catch (CoreException e) {
						logger.logError(e.getMessage());
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
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus(Messages.NewAQLWizardModulePage_SPECIFY_PROJECT);
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus(Messages.NewAQLWizardModulePage_SPECIFY_EXISTING_PROJECT);
			return;
		}
		if (!container.isAccessible()) {
			updateStatus(Messages.NewAQLWizardModulePage_PROJECT_MUST_BE_WRITABLE);
			return;
		}
		if (getModuleName() == null || getModuleName().isEmpty()) {
			updateStatus(Messages.NewAQLWizardModulePage_SPECIFY_MODULE_NAME);
			return;
		}
		
		if (!ProjectUtils.isValidName(getModuleName())) {
			updateStatus(Messages.NewAQLWizardModulePage_MODULE_NAME_NOT_VALID);
			return;
		}
		
		if (ProjectUtils.isAQLKeyword (getModuleName())) {
      updateStatus(Messages.NewAQLModuleWizardPage_MODULE_NAME_IS_AQL_KEYWORD);
      return;
    }
    


		if (fileName.length() == 0) {
			updateStatus(Messages.NewAQLWizardModulePage_SPECIFY_A_FILE_NAME);
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus(Messages.NewAQLWizardModulePage_SPECIFY_A_VALID_FILE_NAME);
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		int firstDot = fileName.indexOf('.');

		// check if only one "." is present
		if (dotLoc != firstDot) {
			updateStatus(Messages.NewAQLWizardModulePage_SPECIFY_A_VALID_FILE_NAME);
			return;
		}

		if (dotLoc == -1) {
			updateStatus(Messages.NewAQLWizardModulePage_FILE_EXTENSION_ERROR);
			return;
		}

		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			String name = fileName.substring(0, dotLoc);
			if (name.contains(" ")) { //$NON-NLS-1$
				updateStatus(Messages.NewAQLWizardModulePage_FILE_NAME_CANNOT_CONTAIN_SPACES);
				return;
			}
			if (!ProjectUtils.isValidName(name)) {
				updateStatus(Messages.NewAQLWizardModulePage_FILENAME_NOT_VALID);
				return;
			}
			if ("aql".equals(ext) == false) { //$NON-NLS-1$
				updateStatus(Messages.NewAQLWizardModulePage_EXTENSION_MUST_BE_AQL);
				return;
			}
		}
		if (dotLoc == 0) {
			updateStatus(Messages.NewAQLWizardModulePage_FILE_NAME_NOT_EMPTY);
			return;
		}
		String moduleSrcPath = ProjectUtils.getConfiguredModuleSrcPath(getContainerName());
		if (null == moduleSrcPath) {
		  updateStatus(Messages.NewAQLWizardModulePage_MODULE_SRC_FOLDER_DETERMINATION_ERROR);
      return;
		}
		String fileFullPath = moduleSrcPath + File.separator
		+ getModuleName() + File.separator + fileName;

		if (ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(fileFullPath)) != null) {
			updateStatus(Messages.NewAQLWizardModulePage_FILE_ALREADY_EXIST);
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
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.NewAQLWizardModulePage_PROJECT_NATURE_DETERMINATION_ERROR);
    }
	}
}
