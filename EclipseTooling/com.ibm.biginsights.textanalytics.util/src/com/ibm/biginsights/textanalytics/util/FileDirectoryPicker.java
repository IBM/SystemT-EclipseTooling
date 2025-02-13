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
package com.ibm.biginsights.textanalytics.util;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IFolderFilter;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.ExternalFileFolderDialog;
import com.ibm.biginsights.textanalytics.util.common.ui.FilteredFileDirectoryDialog;

/**
 * FileDirectoryPicker is a UI component that allows users to choose either a
 * file or directory from workspace or from file system
 * 
 * 
 */

public class FileDirectoryPicker extends Composite {

	@SuppressWarnings("unused")
private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

	/** folder location options */
	public static final int WORKSPACE_ONLY = 0;
	public static final int EXTERNAL_ONLY = 1;
	public static final int WORKSPACE_OR_EXTERNAL = 2;

	protected Text tfFileDir;
	protected Button bWorkspace;
	protected Button bExternal;
	protected Button bClear;
	protected Label descriptionLabel;
	protected IResource iResourceSelected = null;
	// flags
	protected int optionWorkspaceExternal = WORKSPACE_ONLY;
	protected int optionFileDirProject = Constants.FILE_ONLY;
	protected boolean isWorkspaceResource = false;


	protected String allowedFileExtensions;

	protected String[] excludedFolders;
	protected IFolderFilter folderFilter;

	protected String title;
	protected String message;
	
	private boolean allowMultipleSelection = true;
	protected boolean enableShowAllFilesOption = false;

	private boolean enableCreateNewFileOption = false;
	private String  createNewFileLabel = null;
	private String  newFileBaseName = null;
	private String  newFileDefaultExtension = null;

	private boolean workspaceButtonClicked = false;

	public FileDirectoryPicker(Composite parent) {
		this(parent, Constants.FILE_ONLY, WORKSPACE_ONLY);
	}

	public FileDirectoryPicker(Composite parent, int optionFileDirProject) {
		this(parent, optionFileDirProject, WORKSPACE_ONLY);
	}

	public FileDirectoryPicker(Composite parent, 
			int optionFileDirProject,
			int optionWorkspaceExternal) {
		super(parent, SWT.NONE);


		this.optionFileDirProject = optionFileDirProject;
		if (optionFileDirProject == Constants.PROJECT_ONLY) {
			this.optionWorkspaceExternal = WORKSPACE_ONLY;
		} else {
			this.optionWorkspaceExternal = optionWorkspaceExternal;
		}

		switch (optionFileDirProject) {
		case Constants.FILE_ONLY:
			title = Messages.getString("FileDirectoryPicker.FILE_SELECTION"); //$NON-NLS-1$
			message = Messages.getString("FileDirectoryPicker.SELECT_FILE"); //$NON-NLS-1$
			break;

		case Constants.DIRECTORY_ONLY:
			title = Messages
					.getString("FileDirectoryPicker.DIRECTORY_SELECTION"); //$NON-NLS-1$
			message = Messages
					.getString("FileDirectoryPicker.SELECT_A_DIRECTORY"); //$NON-NLS-1$
			break;

		case Constants.PROJECT_ONLY:
			title = Messages.getString("FileDirectoryPicker.PROJECT_SELECTION"); //$NON-NLS-1$
			message = Messages.getString("FileDirectoryPicker.SELECT_PROJECT"); //$NON-NLS-1$
			break;

		case Constants.FILE_OR_DIRECTORY:
			title = Messages
					.getString("FileDirectoryPicker.FILE_DIR_SELECTION"); //$NON-NLS-1$
			message = Messages.getString("FileDirectoryPicker.SELECT_FILE_DIR"); //$NON-NLS-1$
			break;

		case Constants.FILE_OR_DIRECTORY_OR_PROJECT:
			title = Messages
					.getString("FileDirectoryPicker.FILE_DIR_PROJECT_SELECTION"); //$NON-NLS-1$
			message = Messages
					.getString("FileDirectoryPicker.SELECT_FILE_DIR_PROJECT"); //$NON-NLS-1$
			break;
		}

		GridLayout gl = new GridLayout();
		gl.marginWidth = 0;
		this.setLayout(gl);
		this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite fieldsPanel = new Composite(this, SWT.NONE);
		fieldsPanel.setLayout(new GridLayout(1, true));
		fieldsPanel.setFont(this.getFont());
		fieldsPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		descriptionLabel = new Label(fieldsPanel, SWT.WRAP);
		descriptionLabel.setText(Messages
				.getString("FileDirectoryPicker.VALUE")); //$NON-NLS-1$
    GridData gd = new GridData();
    gd.horizontalAlignment = GridData.BEGINNING;
    gd.grabExcessHorizontalSpace = true;
		descriptionLabel.setLayoutData(gd);

		tfFileDir = new Text(fieldsPanel, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		tfFileDir.setBackground(new Color(tfFileDir.getDisplay(), 255, 255, 255));
		tfFileDir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		tfFileDir.addModifyListener (new ModifyListener() {
//      @Override
//      public void modifyText (ModifyEvent e)
//      {
//        // The text can be modified due to (1) Workspace button clicked, (2) File System button
//        // clicked or (3) manually changed.
//        if (workspaceButtonClicked == false)
//          iResourceSelected = null;
//
//        // TODO Auto-generated method stub
//        // TODO //////////////////////////////////////
//      }
//    });

		Composite buttonArea = new Composite(this, SWT.FILL);
		buttonArea.setLayout(new RowLayout());
		buttonArea.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
				false));
		if ((optionWorkspaceExternal == WORKSPACE_ONLY)
				|| (optionWorkspaceExternal == WORKSPACE_OR_EXTERNAL)) {
			bWorkspace = new Button(buttonArea, SWT.PUSH | SWT.RIGHT);
			bWorkspace.setText(Messages
					.getString("FileDirectoryPicker.BROWSE_WORKSPACE")); //$NON-NLS-1$
			bWorkspace.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					showProjectFileDirectoryDialog();
				}
			});
		}

		if ((optionWorkspaceExternal == EXTERNAL_ONLY)
				|| (optionWorkspaceExternal == WORKSPACE_OR_EXTERNAL)) {
			bExternal = new Button(buttonArea, SWT.PUSH | SWT.RIGHT);
			bExternal.setText(Messages
					.getString("FileDirectoryPicker.BROWSE_FILE_SYSTEM")); //$NON-NLS-1$
			bExternal.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					showExternalFileDirectoryDialog();
				}
			});
		}

		bClear = new Button(buttonArea, SWT.PUSH | SWT.RIGHT);
		bClear.setText(Messages.getString("FileDirectoryPicker.CLEAR")); //$NON-NLS-1$
		bClear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				clearFileDirValue();
			}
		});
	}

	public String getAllowedFileExtensions() {
		return allowedFileExtensions;
	}

	/**
	 * Accepts a comma separated list of file extensions that are allowed by the
	 * FileDirectoryPicker
	 * 
	 * @param allowedFileExtension
	 */
	public void setAllowedFileExtensions(String allowedFileExtension) {
		this.allowedFileExtensions = allowedFileExtension;
	}
	
	

	public void setEnableShowAllFilesOption(boolean enableShowAllFilesOption) {
		this.enableShowAllFilesOption = enableShowAllFilesOption;
	}

	public void setAllowMultipleSelection(boolean allowMultipleSelection) {
		this.allowMultipleSelection = allowMultipleSelection;
	}

	private void showProjectFileDirectoryDialog() {
		FilteredFileDirectoryDialog dialog = new FilteredFileDirectoryDialog(
				getShell(), new WorkbenchLabelProvider(),
				new WorkbenchContentProvider(), optionFileDirProject);

		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setEnableShowAllFilesOption(enableShowAllFilesOption);
		dialog.setAllowMultipleSelection(allowMultipleSelection);
		dialog.setCreateNewFileParameters (enableCreateNewFileOption, createNewFileLabel, newFileBaseName, newFileDefaultExtension);
		if (optionFileDirProject == Constants.FILE_ONLY
				|| optionFileDirProject == Constants.FILE_OR_DIRECTORY
				|| optionFileDirProject == Constants.FILE_OR_DIRECTORY_OR_PROJECT) {

			dialog.setAllowedExtensions(allowedFileExtensions);
		}

		if (excludedFolders != null) {
			dialog.excludeFolders(excludedFolders);
		}

		if (folderFilter != null) {
			dialog.setFolderFilter(folderFilter);
		}

		iResourceSelected = dialog.getSelectedResource();
		if (iResourceSelected != null) {
		  workspaceButtonClicked = true;
			setFileDirValue(iResourceSelected.getFullPath().toString(), true);
		}
	}

	public void showCollectionDialog() {
		if ((optionWorkspaceExternal == WORKSPACE_ONLY)
				|| (optionWorkspaceExternal == WORKSPACE_OR_EXTERNAL)) {
			showProjectFileDirectoryDialog();
		}

		else if ((optionWorkspaceExternal == EXTERNAL_ONLY)
				|| (optionWorkspaceExternal == WORKSPACE_OR_EXTERNAL)) {
			showExternalFileDirectoryDialog();
		}
	}

	private void showExternalFileDirectoryDialog() {
		if (optionFileDirProject == Constants.FILE_ONLY) {
			FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
			String extensions[] = {};
			if (allowedFileExtensions != null && !allowedFileExtensions.isEmpty())
			  extensions = this.allowedFileExtensions.split (","); //$NON-NLS-1$
			for (int i=0; i<extensions.length; i++) {
			  extensions[i] = "*." + extensions[i].substring (extensions[i].indexOf (".") + 1); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (extensions.length == 0) {
			  extensions = new String[] {"*.*"}; //$NON-NLS-1$
			}
			fileDialog.setText(Messages
					.getString("FileDirectoryPicker.FILE_SELECTION")); //$NON-NLS-1$
			fileDialog
					.setFilterExtensions(extensions); //this change might be causing an issue, where allowedFileExtensions was not set to .xml and filtering on xml files is not expected
			fileDialog.open();

			String pathName = fileDialog.getFileName();
			if (pathName != null && pathName.trim().length() > 0) {
			  iResourceSelected = null;
			  workspaceButtonClicked = false;
				setFileDirValue((fileDialog.getFilterPath() + File.separator + pathName), false);
			}
		} else if (optionFileDirProject == Constants.DIRECTORY_ONLY
				|| optionFileDirProject == Constants.DIRECTORY_OR_PROJECT) {
			DirectoryDialog dirDialog = new DirectoryDialog(getShell(),
					SWT.SINGLE);
			String currentValue = tfFileDir.getText();

			// set defaults
			if (StringUtils.isEmpty (title)) { //set default values only if current values are empty.
			  title = Messages.
			      getString("FileDirectoryPicker.DIRECTORY_SELECTION"); //$NON-NLS-1$
			}
			if (StringUtils.isEmpty (message)) {
			  message = Messages
			      .getString("FileDirectoryPicker.SELECT_A_DIRECTORY"); //$NON-NLS-1$
			}
			
			dirDialog.setMessage(message);
			dirDialog.setText(title);
			if (currentValue != null && currentValue.trim().length() > 0) {
				dirDialog.setFilterPath(currentValue);
			}

			String pathName = dirDialog.open();
			if (pathName != null && pathName.trim().length() > 0) {
			  iResourceSelected = null;
        workspaceButtonClicked = false;
				setFileDirValue(pathName, false);
			}
		} else if (optionFileDirProject == Constants.FILE_OR_DIRECTORY) {
			ExternalFileFolderDialog pick = new ExternalFileFolderDialog(getShell(), IResource.FILE | IResource.FOLDER);
			pick.setAllowedFileExtensions(allowedFileExtensions);
			pick.setEnableShowAllFilesOption(enableShowAllFilesOption);
			pick.createDialog(title, message);
		  String pathName = pick.getFileName();
			if(pathName != null) {
        iResourceSelected = null;
        workspaceButtonClicked = false;
				setFileDirValue(pathName, false);
			}
		}
	}

	public void setEditable(boolean flag) {
		tfFileDir.setEditable(flag);
	}

	public void setDescriptionLabelText(String text) {
		descriptionLabel.setText(text);
	}

	/*public void apply() {
		String value = getFileDirValue();
		configPage.setValue(fieldKey, value);
	}*/

	public void setFileDirValue(String str, boolean isWorkspaceResource) {
		this.isWorkspaceResource = isWorkspaceResource;
		tfFileDir.setText(str);
	}

	/**
	 * Get the workspace resource selected through the File Browser dialog.
	 * If the selected file is local, not workspace resource, this method returns null.
	 * @return The selected workspace resource, or null if the selected file is local.
	 */
	public IResource getSelectedResource() 
	{
	  return iResourceSelected;
	}

  /**
   * @return The selected file.
   */
  public File getSelectedFile() 
  {
    if (workspaceButtonClicked && iResourceSelected != null)
      return iResourceSelected.getLocation ().toFile ();
    else if (tfFileDir.getText ().isEmpty () == false)
      return new File (tfFileDir.getText ());
    else
      return null;
  }

	public String getFileDirValue() {
		String fileDirValue = tfFileDir.getText();
		if (fileDirValue.trim().length() != 0) {
			return (isWorkspaceResource ? (Constants.WORKSPACE_RESOURCE_PREFIX + fileDirValue)
					: fileDirValue);
		} else {
			return ""; //$NON-NLS-1$
		}
	}

	public void clearFileDirValue() {
		setFileDirValue("", false); //$NON-NLS-1$
	}
	
	
	/**
	 * Enables certain components of UI based on certain attributes of this
	 * class.
	 * 
	 * @param enabled
	 */
	public void enableUI(boolean enabled) {
		tfFileDir.setEnabled(enabled);
		if (enabled) {
			// set it to white
			tfFileDir.setBackground(new Color(tfFileDir.getDisplay(), 255, 255,
					255));
		} else {
			// set it to light gray
			tfFileDir.setBackground(new Color(tfFileDir.getDisplay(), 212, 208,
					200));
		}
		bClear.setEnabled(enabled);
		if (optionWorkspaceExternal == WORKSPACE_ONLY) {
			bWorkspace.setEnabled(enabled);
		} else if (optionWorkspaceExternal == EXTERNAL_ONLY) {
			bExternal.setEnabled(enabled);
		} else if (optionWorkspaceExternal == WORKSPACE_OR_EXTERNAL) {
			bWorkspace.setEnabled(enabled);
			bExternal.setEnabled(enabled);
		}
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void addModifyListenerForFileDirTextField(ModifyListener listener) {
		tfFileDir.addModifyListener(listener);
	}

	public void removeModifyListenerForFileDirTextField(ModifyListener listener) {
		tfFileDir.removeModifyListener(listener);
	}

	public void setFileDirTooltip(String tooltipText) {
		tfFileDir.setToolTipText(tooltipText);
	}

	public void excludeFolders(String... folders) {
		this.excludedFolders = folders;
	}

	public void setFolderFilter(IFolderFilter folderFilter) {
		this.folderFilter = folderFilter;
	}

  public void setCreateNewFileParameters (boolean enableCreateNewFileOption, String createNewFileLabel, String newFileBaseName, String newFileDefaultExtension)
  {
    this.enableCreateNewFileOption = enableCreateNewFileOption;
    this.createNewFileLabel = createNewFileLabel;
    this.newFileBaseName = newFileBaseName;
    this.newFileDefaultExtension = newFileDefaultExtension;
    if (enableCreateNewFileOption && !StringUtils.isEmpty (newFileDefaultExtension))
      this.allowedFileExtensions = (newFileDefaultExtension.startsWith (".") ? newFileDefaultExtension.substring (1) : newFileDefaultExtension); //$NON-NLS-1$
  }

}
