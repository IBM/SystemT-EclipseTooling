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

package com.ibm.biginsights.textanalytics.nature.prefs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.Activator;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.FilteredFileDirectoryDialog;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * GeneralPrefPage provides the UI for 'Source' tab of SystemT project
 * preferences
 * 
 */
public class ModularSourcePrefPage extends PrefPageAdapter {



	private Composite topLevel;
	private Button srcButton;
	private Button binButton;
	private Text srcText;
	private Text binText;
	private String errorMessage = null;
	private IProject project;
	private static final ILog logger = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);

	public ModularSourcePrefPage(Composite parent,
			SystemTProjectPreferences projectPreferences) {
		super(projectPreferences);

		topLevel = new Composite(parent, SWT.NONE);
		GridLayout gLayout = new GridLayout(2, false);
		gLayout.marginLeft = 0;
		topLevel.setLayout(gLayout);
		GridData gridData = new GridData(GridData.BEGINNING, GridData.CENTER,
				false, false);
		topLevel.setLayoutData(gridData);

		Label label = new Label(topLevel, SWT.NULL);
		label.setText(Messages
				.getString("ModularSourcePrefPage.Src_Label")); //$NON-NLS-1$

		Label templabel = new Label(topLevel, SWT.NULL);
		templabel.setText("");//$NON-NLS-1$

		srcText = new Text(topLevel, SWT.BORDER | SWT.SINGLE);
		srcText.setEditable(false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		srcText.setLayoutData(gd);

		srcButton = new Button(topLevel, SWT.PUSH);
		srcButton.setText(Messages
				.getString("General.Browse")); //$NON-NLS-1$

		label = new Label(topLevel, SWT.NULL);
		label.setText(Messages
				.getString("ModularSourcePrefPage.Bin_Label")); //$NON-NLS-1$

		templabel = new Label(topLevel, SWT.NULL);
		templabel.setText("");//$NON-NLS-1$

		binText = new Text(topLevel, SWT.BORDER | SWT.SINGLE);
		binText.setEditable(false);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		binText.setLayoutData(gd);

		binButton = new Button(topLevel, SWT.PUSH);
		binButton.setText(Messages
				.getString("General.Browse")); //$NON-NLS-1$
		createSourceListener();
		createOutputListener();

		initialize();

	}

	public Control getControl() {
		return topLevel;
	}

	private void updateDecorator() {
		IWorkbench workbench=PlatformUI.getWorkbench();
		workbench.getDecoratorManager().
		update("com.ibm.biginsights.project.decorator.TextAnalyticsFolderDecorator");//$NON-NLS-1$
	}
	
	@Override
	public void restoreDefaults() {
		if (preferenceStore == null) {
			return;
		}
		String srcPath = preferenceStore.getDefaultString(Constants.MODULE_SRC_PATH);
		String binPath = preferenceStore.getDefaultString(Constants.MODULE_BIN_PATH);
		if (srcPath != null && srcPath.startsWith (Constants.PROJECT_RELATIVE_PATH_PREFIX)) {
		  srcText.setText(new Path (srcPath.replace (Constants.PROJECT_RELATIVE_PATH_PREFIX, "")).toString ());
		} else {
		  //If the srcPath does not start with the project relative path i.e [p], then it is not a valid path.
		  //In this case we set an empty path.
		  srcText.setText ("");
		}
		if (binPath != null && binPath.startsWith (Constants.PROJECT_RELATIVE_PATH_PREFIX)) {
		  binText.setText(new Path (binPath.replace (Constants.PROJECT_RELATIVE_PATH_PREFIX, "")).toString ());
		} else {
		  //If the binPath does not start with the project relative path i.e [p], then it is not a valid path.
      //In this case we set an empty path.
		  binText.setText ("");
		}
		updateDecorator();
	}

	public boolean isValid() {
		if (null == getSrcText() || getSrcText().isEmpty()) {
			setErrorMessage(Messages.getString("ModularSourcePrefPage.Err_Src")); //$NON-NLS-1$
			return false;
		}
		if (null == getBinText() || getBinText().isEmpty()) {
			setErrorMessage(Messages.getString("ModularSourcePrefPage.Err_Bin")); //$NON-NLS-1$
			return false;
		}
		setErrorMessage(null);
		return true;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void apply() {
		setValue(Constants.MODULE_SRC_PATH, Constants.PROJECT_RELATIVE_PATH_PREFIX
				+ getSrcText());
		projectPreferences.getProjectProperties().setModuleSrcPath(
				preferenceStore.getString(Constants.MODULE_SRC_PATH));
		setValue(Constants.MODULE_BIN_PATH, Constants.PROJECT_RELATIVE_PATH_PREFIX
				+ getBinText());
		projectPreferences.getProjectProperties().setModuleBinPath(
				preferenceStore.getString(Constants.MODULE_BIN_PATH));
		
		updateDecorator();

	}

  @Override
  public void restoreToProjectProperties (SystemTProperties properties)
  {
    // Using project relative paths as-is
    srcText.setText (new Path (properties.getModuleSrcPath ().replace (Constants.PROJECT_RELATIVE_PATH_PREFIX, "")).toString ());
    binText.setText (new Path (properties.getModuleBinPath ().replace (Constants.PROJECT_RELATIVE_PATH_PREFIX, "")).toString ());
  }

	protected void setErrorMessage(String errorMsg) {
		this.errorMessage = errorMsg;
	}

	private void initialize() {
		project = ProjectPreferencesUtil.getSelectedProject();
		String moduleSrcPath = ProjectUtils.getConfiguredModuleSrcPath(project.getName());
		if (moduleSrcPath == null) {
		  logger.logError (Messages.getString ("ModularAQLBuilder.MODULE_SRC_FOLDER_DETERMINATION_ERROR",
        new Object[]{project.getName ()}));
		  return;
		}
		srcText.setText(new Path(moduleSrcPath).makeRelativeTo (project.getFullPath ()).toString ());
		String moduleBinPath = ProjectUtils.getConfiguredModuleBinPath(project.getName());
		if (moduleBinPath == null) {
		  logger.logError (Messages.getString ("ModularAQLBuilder.MODULE_BIN_PATH_DETERMINATION_ERROR",
        new Object[]{project.getName ()}));
      return;
    }
		binText.setText(new Path(moduleBinPath).makeRelativeTo (project.getFullPath ()).toString ());
	}

	private String getSrcText() {
		return srcText.getText();
	}

	private String getBinText() {
		return binText.getText();
	}

	private void createOutputListener() {
		binButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				FilteredFileDirectoryDialog dialog = new FilteredFileDirectoryDialog(
						getControl().getShell(), new WorkbenchLabelProvider(),
						new WorkbenchContentProvider(),
						Constants.DIRECTORY_ONLY);
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				dialog.setAllowMultipleSelection(false);
				dialog.addFilter(new ViewerFilter() {
					public boolean select(Viewer viewer, Object parentElement,
							Object element) {

						if (element instanceof IProject) {
							String projectName = ((IProject) element).getName();
							String srcProjectName = project.getName();
							if (projectName.equals(srcProjectName)) {
								return true;
							} else
								return false;

						}

						return true;
					}

				});
				dialog.setInput(root);

				dialog.setMessage(Messages.getString("ModularSourcePrefPage.Folder_Msg")); //$NON-NLS-1$
				dialog.setTitle(Messages.getString("ModularSourcePrefPage.Output_Folder_Title")); //$NON-NLS-1$

				String paths[] = dialog.getAllSelectedPath();
				if (paths != null) {
				  IPath path = new Path(paths[0]); //only one selection allowed
					binText.setText(path.makeRelativeTo (project.getFullPath ()).toString ());
				}
			}
		});
	}

	private void createSourceListener() {
		srcButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				FilteredFileDirectoryDialog dialog = new FilteredFileDirectoryDialog(
						getControl().getShell(), new WorkbenchLabelProvider(),
						new WorkbenchContentProvider(),
						Constants.DIRECTORY_ONLY);
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				dialog.setAllowMultipleSelection(false);
				dialog.addFilter(new ViewerFilter() {
					public boolean select(Viewer viewer, Object parentElement,
							Object element) {

						if (element instanceof IProject) {
							String projectName = ((IProject) element).getName();
							String srcProjectName = project.getName();
							if (projectName.equals(srcProjectName)) {
								return true;
							} else
								return false;

						}
						return true;
					}

				});
				dialog.setInput(root);

				dialog.setMessage(Messages.getString("ModularSourcePrefPage.Folder_Msg")); //$NON-NLS-1$
				dialog.setTitle(Messages.getString("ModularSourcePrefPage.Src_Folder_Title")); //$NON-NLS-1$

				String paths[] = dialog.getAllSelectedPath();
				if (paths != null) {
				  IPath path = new Path(paths[0]); //only one selection allowed
					srcText.setText(path.makeRelativeTo (project.getFullPath ()).toString ());
				}
			}
		});
	}


}
