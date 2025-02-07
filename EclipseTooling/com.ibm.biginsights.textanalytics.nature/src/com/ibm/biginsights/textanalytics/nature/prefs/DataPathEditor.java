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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ui.ExternalFileFolderDialog;
import com.ibm.biginsights.textanalytics.util.common.ui.FilteredFileDirectoryDialog;

/**
 * DataPathEditor provides a UI to select datapaths from Workspace and/or
 * external file system Workspace paths are prefixed with [W]. Use methods of
 * ProjectPreferencesUtil.java to get selected paths in different variations
 * (like absolute path, relative paths etc)
 * 
 * 
 */
public class DataPathEditor extends Composite {



	/** Key used by Widget **/
	private static final String KEY_OPTION_FILE_DIR_PROJECT = "optionFileDirProject";
	private static final String KEY_ALLOWED_FILE_EXTENSIONS = "allowedFileExtensions";

	/** folder options */
	public final static int PROJECT_AND_EXTERNAL_FOLDERS = 1;
	public final static int PROJECT_FOLDER_ONLY = 2;
	public final static int EXTERNAL_FOLDER_ONLY = 3;

	/** Edit and Remove button options */
	public final static int EDIT_AND_REMOVE = 4;
	public final static int EDIT_ONLY = 5;
	public final static int REMOVE_ONLY = 6;

	private static final String WORKSPACE_RESOURCE_PREFIX = "[W]";
	private static final String PROPERTY_DATA_PATH = "datapath";

	protected PropertyPage page;
	protected Label description;
	protected Composite buttonArea;
	protected TableViewer dataPathViewer;
	protected Table dataPathTable;

	protected Button bRemove;
	// protected Button bEdit;
	protected Button bAddProjFolder;
	protected Button bAddExtFolder;

	protected int folderOption;
	protected int editRemoveOption;
	protected String allowedFileExtensions;
	protected int optionFileDirProject;

	// Adding property change support for dataPath property since TableViewer
	// and Table does not seem to provide listeners for data change
	protected PropertyChangeSupport propertyChangeSupport;

	protected ArrayList<String> workspaceDataPaths = new ArrayList<String>();

	public DataPathEditor(Composite parent) {
		this(parent,
				"", PROJECT_AND_EXTERNAL_FOLDERS, EDIT_AND_REMOVE, -1, null); //$NON-NLS-1$
	}

	public DataPathEditor(Composite parent, String labelText, int folderOption,
			int editRemoveOption, int optionFileDirProject,
			String allowedFileExtensions) {
		super(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		super.setLayout(layout);
		super.setLayoutData(new GridData(GridData.FILL_BOTH));

		description = new Label(this, SWT.WRAP);
		// description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		description.setFont(this.getFont());

		Composite tableAndButtons = new Composite(this, SWT.NONE);
		tableAndButtons.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		tableAndButtons.setLayout(layout);

		dataPathViewer = new TableViewer(tableAndButtons, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		dataPathViewer.addSelectionChangedListener(new TableViewerAdapter());

		dataPathTable = dataPathViewer.getTable();
		dataPathTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		dataPathTable.setFont(tableAndButtons.getFont());

		Accessible descriptionLabel = description.getAccessible();
    Accessible dataPathTableAcc = dataPathViewer.getTable ().getAccessible ();
    descriptionLabel.addRelation(ACC.RELATION_LABEL_FOR, dataPathTableAcc);
    dataPathTableAcc.addRelation(ACC.RELATION_LABELLED_BY, descriptionLabel);

		propertyChangeSupport = new PropertyChangeSupport(this);

		// button area
		buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setFont(tableAndButtons.getFont());
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		setDescriptionLabelText(labelText);
		setFolderOption(folderOption);
		setEditRemoveOption(editRemoveOption);

		this.allowedFileExtensions = allowedFileExtensions;
		this.optionFileDirProject = optionFileDirProject;

		if (folderOption == PROJECT_AND_EXTERNAL_FOLDERS) {
			createBrowseWorkspaceButton(buttonArea);
			createAddExternalFolderButton(buttonArea);
		} else if (folderOption == PROJECT_FOLDER_ONLY) {
			createBrowseWorkspaceButton(buttonArea);
		} else if (folderOption == EXTERNAL_FOLDER_ONLY) {
			createAddExternalFolderButton(buttonArea);
		}

		if (editRemoveOption == EDIT_AND_REMOVE) {
			createEditButton(buttonArea);
			createRemoveButton(buttonArea);
		} else if (editRemoveOption == EDIT_ONLY) {
			createEditButton(buttonArea);
		} else if (editRemoveOption == REMOVE_ONLY) {
			createRemoveButton(buttonArea);
		}
	}

	private void createRemoveButton(Composite panel) {
		bRemove = createButton(panel,
				Messages.getString("DataPathEditor.REMOVE")); //$NON-NLS-1$
		bRemove.setEnabled(false);
		bRemove.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Object[] elements = getSelectedElements().toArray();
				removeDataPath(elements);
			}

		});
	}

	/**
	 * This method is not implemented for now
	 * 
	 * @param panel
	 */
	private void createEditButton(Composite panel) {
		// bEdit = createButton(panel, "Edit");
		// bEdit.setEnabled(false);
		// bEdit.addSelectionListener(new SelectionAdapter(){
		//
		// public void widgetSelected(SelectionEvent e) {
		//
		// }
		//
		// });
	}

	private void createAddExternalFolderButton(Composite panel) {
		bAddExtFolder = createButton(panel,
				Messages.getString("DataPathEditor.BROWSE_FILE_SYSTEM")); //$NON-NLS-1$
		bAddExtFolder.setEnabled(true);
		bAddExtFolder.setData(KEY_ALLOWED_FILE_EXTENSIONS,
				allowedFileExtensions);
		bAddExtFolder
				.setData(KEY_OPTION_FILE_DIR_PROJECT, optionFileDirProject);
		bAddExtFolder.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				String allowedFileExtensions = (String) e.widget
						.getData(KEY_ALLOWED_FILE_EXTENSIONS);
				int optionFileDirProject = (Integer) e.widget
						.getData(KEY_OPTION_FILE_DIR_PROJECT);
				if (allowedFileExtensions != null
						&& optionFileDirProject == Constants.FILE_OR_DIRECTORY) {
					ExternalFileFolderDialog pick = new ExternalFileFolderDialog(getShell(), IResource.FILE | IResource.FOLDER);
					pick.setAllowedFileExtensions(allowedFileExtensions);
					pick.setEnableShowAllFilesOption(true);
					pick.createDialog(
							Messages.getString("DataPathEditor.DATA_PATH_SELECTION"),
							Messages.getString("DataPathEditor.SELECT_DATA_PATH"));
					if (pick.getFileName() != null)
						addDataPath(pick.getFileName(), false);
				} else {
					DirectoryDialog dirDialog = new DirectoryDialog(getShell(),
							SWT.SINGLE);
					// set defaults
					dirDialog.setMessage(Messages
							.getString("DataPathEditor.SELECT_DATA_PATH")); //$NON-NLS-1$
					dirDialog.setText(Messages
							.getString("DataPathEditor.DATA_PATH_SELECTION")); //$NON-NLS-1$

					String dirName = dirDialog.open();
					if (dirName != null && dirName.trim().length() > 0) {
						addDataPath(dirName, false);
					}
				}
			}

		});
	}

	private void createBrowseWorkspaceButton(Composite panel) {
		bAddProjFolder = createButton(panel,
				Messages.getString("DataPathEditor.BROWSE_WORKSPACE")); //$NON-NLS-1$
		bAddProjFolder.setEnabled(true);
		bAddProjFolder.setData(KEY_ALLOWED_FILE_EXTENSIONS,
				allowedFileExtensions);
		bAddProjFolder.setData(KEY_OPTION_FILE_DIR_PROJECT,
				optionFileDirProject);
		bAddProjFolder.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				FilteredFileDirectoryDialog dialog = null;
				String allowedFileExtensions = (String) e.widget
						.getData(KEY_ALLOWED_FILE_EXTENSIONS);
				int optionFileDirProject = (Integer) e.widget
						.getData(KEY_OPTION_FILE_DIR_PROJECT);
				if (allowedFileExtensions != null
						&& optionFileDirProject == Constants.FILE_OR_DIRECTORY) {
					dialog = new FilteredFileDirectoryDialog(getShell(),
							new WorkbenchLabelProvider(),
							new WorkbenchContentProvider(),
							Constants.FILE_OR_DIRECTORY);
					dialog.setAllowedExtensions(allowedFileExtensions);
				} else {
					dialog = new FilteredFileDirectoryDialog(getShell(),
							new WorkbenchLabelProvider(),
							new WorkbenchContentProvider(),
							Constants.DIRECTORY_OR_PROJECT);
				}
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				dialog.setAllowMultipleSelection(true);

				dialog.setInput(root);

				dialog.setMessage("Select a data path"); //$NON-NLS-1$
				dialog.setTitle("Data path selection"); //$NON-NLS-1$

				String paths[] = dialog.getAllSelectedPath();
				if (paths != null) {
					for (String path : paths) {
						addDataPath(path, true);
					}

				}
			}
		});
	}

	private Button createButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(label);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		button.setLayoutData(data);
		return button;
	}

	@SuppressWarnings("unchecked")
	private List<String> getSelectedElements() {
		ArrayList<String> result = new ArrayList<String>();
		ISelection selection = dataPathViewer.getSelection();

		if (selection instanceof IStructuredSelection) {
			Iterator<String> iter = ((IStructuredSelection) selection)
					.iterator();
			while (iter.hasNext()) {
				result.add(iter.next());
			}
		}

		return result;
	}

	public void setDescriptionLabelText(String text) {
		description.setText(text);
	}

	protected void setFolderOption(int folderOption) {
		this.folderOption = folderOption;
	}

	protected void setEditRemoveOption(int editRemoveOption) {
		this.editRemoveOption = editRemoveOption;
	}

	private void enableEditButton(boolean enabled) {
		// bEdit.setEnabled(enabled);
	}

	private void enableRemoveButton(boolean enabled) {
		bRemove.setEnabled(enabled);
	}

	public void clearDataPaths() {
		if (dataPathTable.getItemCount() > 0) {
			String oldDataPath = getDataPath();
			String newDataPath = ""; //$NON-NLS-1$

			dataPathTable.removeAll();
			if (!newDataPath.equals(oldDataPath)) {
				propertyChangeSupport.firePropertyChange(PROPERTY_DATA_PATH,
						oldDataPath, newDataPath);
			}
		}
	}

	public void addDataPath(String element, boolean isWorkspaceResource) {
		String oldDataPath = getDataPath();
		dataPathViewer.add(element);

		if (isWorkspaceResource) {
			workspaceDataPaths.add(element);
		}
		String newDataPath = getDataPath();
		propertyChangeSupport.firePropertyChange(PROPERTY_DATA_PATH,
				oldDataPath, newDataPath);
	}

	protected void removeDataPath(Object[] elements) {
		String oldDataPath = getDataPath();

		for (Object element : elements) {
			dataPathViewer.remove(element);

			// remove, if exists
			workspaceDataPaths.remove(element);
		}
		String newDataPath = getDataPath();
		propertyChangeSupport.firePropertyChange(PROPERTY_DATA_PATH,
				oldDataPath, newDataPath);
	}

	public String getDataPath() {
		int count = dataPathViewer.getTable().getItemCount();
		String dataPath = ""; //$NON-NLS-1$
		if (count > 0) {
			dataPath = dataPathViewer.getElementAt(0).toString();
			dataPath = addWorkspaceResourcePrefix(dataPath);
			for (int i = 1; i < count; ++i) {
				String str = dataPathViewer.getElementAt(i).toString();
				str = addWorkspaceResourcePrefix(str);
				dataPath = dataPath + Constants.DATAPATH_SEPARATOR + str;
			}
		}
		return dataPath;
	}

	private String addWorkspaceResourcePrefix(String path) {
		if (isWorkspaceResource(path)) {
			path = WORKSPACE_RESOURCE_PREFIX + path;
		}
		return path;
	}

	public void setExternalFolderButtonEnabled(boolean enabled) {
		bAddExtFolder.setEnabled(enabled);
	}

	public void setProjectFolderButtonEnabled(boolean enabled) {
		bAddProjFolder.setEnabled(enabled);
	}

	private boolean isWorkspaceResource(String path) {
		return workspaceDataPaths.contains(path);
	}

	/**
	 * This method is used to enable or disable the datapath editor UI based on
	 * a user event
	 * 
	 * @param enabled
	 */
	public void enableUI(boolean enabled) {
		dataPathViewer.getTable().setEnabled(enabled);
		bAddExtFolder.setEnabled(enabled);
		bAddProjFolder.setEnabled(enabled);
		bRemove.setEnabled(enabled);
	}

	/**
	 * adds listener to handle events related to change of datapath value
	 * 
	 * @param listener
	 */
	public void addDataPathChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removeDataPathChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	// ////////////////////////////// INNER CLASS
	// ////////////////////////////////////////////

	/**
	 * TableViewerAdapter handles selection events of the tableviewer used by
	 * Datapath editor
	 */
	private class TableViewerAdapter implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			boolean bEditButtonEnabled = false;
			boolean bRemoveButtonEnabled = false;

			ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection) {
				int selectionCount = ((IStructuredSelection) selection).size();
				if (selectionCount == 1) {
					bEditButtonEnabled = true;
					bRemoveButtonEnabled = true;
				}
				if (selectionCount > 1) {
					bEditButtonEnabled = false;
					bRemoveButtonEnabled = true;
				}
			}

			enableEditButton(bEditButtonEnabled);
			enableRemoveButton(bRemoveButtonEnabled);
		}
	}
}
