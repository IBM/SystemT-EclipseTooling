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

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.FilteredFileDirectoryDialog;

/**
 * ModularProjectPrefPage provides the UI for 'Project' tab of SystemT project
 * preferences
 * 
 */
public class ModularProjectPrefPage extends PrefPageAdapter {



	private Composite topLevel;
	private Button bRemove;
	private Button bAddProjFolder;
	private TableViewer dataPathViewer;
	private PropertyChangeSupport propertyChangeSupport;
	private Set<String> addedDataPaths = new HashSet<String>();
	private Set<String> removedDataPaths = new HashSet<String>();
	private static final String PROPERTY_DATA_PATH = "datapath";//$NON-NLS-1$
	private String errorMessage = null;
	private IProject project;
	private Table dataPathTable;

	public ModularProjectPrefPage(Composite parent,
			SystemTProjectPreferences projectPreferences) {
		super(projectPreferences);

		topLevel = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		topLevel.setLayout(layout);
		topLevel.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label description = new Label(topLevel, SWT.WRAP);
		description.setFont(topLevel.getFont());

		Composite tableAndButtons = new Composite(topLevel, SWT.NONE);
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
    Accessible dataPathTableAcc = dataPathTable.getAccessible ();
    descriptionLabel.addRelation(ACC.RELATION_LABEL_FOR, dataPathTableAcc);
    dataPathTableAcc.addRelation(ACC.RELATION_LABELLED_BY, descriptionLabel);
    
		propertyChangeSupport = new PropertyChangeSupport(this);

		// button area
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setFont(tableAndButtons.getFont());
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		description.setText(Messages
				.getString("ModularProjectPrefPage.Description"));//$NON-NLS-1$
		createBrowseWorkspaceButton(buttonArea);
		createRemoveButton(buttonArea);

		initialize();
	}

	public Control getControl() {
		return topLevel;
	}

	@Override
	public void restoreDefaults() {
		if (preferenceStore == null) {
			return;
		}

		if (!addedDataPaths.isEmpty()) {
			String elements[] = addedDataPaths
			.toArray(new String[addedDataPaths.size()]);
			ProjectUtils.updateProjectReferences(project, elements, false);
		}

		String dependency = ProjectPreferencesUtil.getPath(preferenceStore
				.getDefaultString(Constants.DEPENDENT_PROJECT));
		dataPathTable.removeAll();
		addedDataPaths.clear();
		removedDataPaths.clear();

		if (dependency != null && !dependency.isEmpty()) {
			String projects[] = dependency.split(Constants.DATAPATH_SEPARATOR);
			for (String project : projects) {
				addDataPath(project);
			}
			ProjectUtils.updateProjectReferences(project, projects, true);
		}

	}

	public boolean isValid() {
		return true;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	protected void setErrorMessage(String errorMsg) {
		this.errorMessage = errorMsg;
	}

	public void apply() {
		setValue(Constants.DEPENDENT_PROJECT, getDataPath());
		projectPreferences.getProjectProperties().setDependentProject(
				preferenceStore.getString(Constants.DEPENDENT_PROJECT));
		if (!addedDataPaths.isEmpty()) {
			String elements[] = addedDataPaths
			.toArray(new String[addedDataPaths.size()]);
			ProjectUtils.updateProjectReferences(project, elements, true);
		}
		if (!removedDataPaths.isEmpty()) {
			String elements[] = removedDataPaths
			.toArray(new String[removedDataPaths.size()]);
			ProjectUtils.updateProjectReferences(project, elements, false);
		}
	}

	@Override
	public void restoreToProjectProperties(SystemTProperties properties) {
		String dependency = properties.getDependentProject();
		if (dependency != null && !dependency.isEmpty()) {
			String projects[] = dependency.split(Constants.DATAPATH_SEPARATOR);
			for (String project : projects) {
				addDataPath(ProjectPreferencesUtil.getPath(project));
			}
		}
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

	private void createBrowseWorkspaceButton(Composite panel) {
		bAddProjFolder = createButton(panel,
				Messages.getString("ModularProjectPrefPage.Add")); //$NON-NLS-1$
		bAddProjFolder.setEnabled(true);
		bAddProjFolder.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				FilteredFileDirectoryDialog dialog = new FilteredFileDirectoryDialog(
						e.display.getActiveShell(),
						new WorkbenchLabelProvider(),
						new WorkbenchContentProvider(), Constants.PROJECT_ONLY);
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				dialog.setAllowMultipleSelection(true);
				dialog.addFilter(new ViewerFilter() {
					public boolean select(Viewer viewer, Object parentElement,
							Object element) {

						if (element instanceof IProject) {
							IProject project = (IProject)element;
							try {
								if(project.hasNature(Constants.PLUGIN_NATURE_ID)){
									return true;
								}
							} catch (CoreException e) {
								return false;
							}
								return false;
						}

						return true;
					}

				});
				dialog.setInput(root);

				dialog.setMessage(Messages
						.getString("ModularProjectPrefPage.Add_Message")); //$NON-NLS-1$
				dialog.setTitle(Messages
						.getString("ModularProjectPrefPage.Add_Title")); //$NON-NLS-1$

				String paths[] = dialog.getAllSelectedPath();
				if (paths != null) {
					for (String path : paths) {
						addDataPath(path);
					}

				}
			}
		});
	}

	private void addDataPath(String element) {
		String oldDataPath = getDataPath();
		dataPathViewer.remove(element);
		dataPathViewer.add(element);
		addedDataPaths.add(element);
		removedDataPaths.remove(element);
		String newDataPath = getDataPath();
		propertyChangeSupport.firePropertyChange(PROPERTY_DATA_PATH,
				oldDataPath, newDataPath);
	}

	private void removeDataPath(Object[] elements) {
		String oldDataPath = getDataPath();
		for (Object element : elements) {
			dataPathViewer.remove(element);
			removedDataPaths.add((String) element);
			addedDataPaths.remove((String) element);
		}

		String newDataPath = getDataPath();
		propertyChangeSupport.firePropertyChange(PROPERTY_DATA_PATH,
				oldDataPath, newDataPath);
	}

	private String getDataPath() {
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

	private String addWorkspaceResourcePrefix(String path) {
		path = Constants.WORKSPACE_RESOURCE_PREFIX + path;
		return path;
	}

	private void createRemoveButton(Composite panel) {
		bRemove = createButton(panel,
				Messages.getString("ModularProjectPrefPage.Remove")); //$NON-NLS-1$
		bRemove.setEnabled(false);
		bRemove.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Object[] elements = getSelectedElements().toArray();
				removeDataPath(elements);
			}

		});
	}

	private void initialize() {
		project = ProjectPreferencesUtil.getSelectedProject();
		String dependency = ProjectUtils
		.getProjectDependency(project.getName());
		if (dependency != null && !dependency.isEmpty()) {
			String projects[] = dependency.split(Constants.DATAPATH_SEPARATOR);
			for (String project : projects) {
				addDataPath(project);
			}
		}
	}

	/**
	 * TableViewerAdapter handles selection events of the tableviewer used by
	 * Datapath editor
	 */
	private class TableViewerAdapter implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			boolean bRemoveButtonEnabled = false;

			ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection) {
				int selectionCount = ((IStructuredSelection) selection).size();
				if (selectionCount == 1) {
					bRemoveButtonEnabled = true;
				}
				if (selectionCount > 1) {
					bRemoveButtonEnabled = true;
				}
			}
			bRemove.setEnabled(bRemoveButtonEnabled);
		}
	}

}
