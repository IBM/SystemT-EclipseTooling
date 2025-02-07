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
package com.ibm.biginsights.project.launch;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import com.ibm.biginsights.project.Activator;

public class FilteredScriptFilesDialog extends FilteredItemsSelectionDialog {

	private static final String SETTINGS_ID = FilteredScriptFilesDialog.class.getCanonicalName();
	
	private final List<IFile> scriptFiles;
	public FilteredScriptFilesDialog(Shell shell, List<IFile>scriptFiles)
	{
		super(shell);	
		this.scriptFiles = scriptFiles;
		setListLabelProvider(getListLabelProvider());
		setDetailsLabelProvider(getDetailsLabelProvider());
		setSelectionHistory(new ScriptFileHistory());
		
	}
	
	@Override
	protected Control createExtendedContentArea(Composite parent) {
		return null;
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = Activator.getDefault().getDialogSettings().getSection(SETTINGS_ID);

		if (settings == null) {
			settings = Activator.getDefault().getDialogSettings().addNewSection(SETTINGS_ID);
		}

		return settings;
	}

	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;		
	}

	@Override
	protected ItemsFilter createFilter() {		
		return new ScriptFileFilter();
	}

	@Override
	protected Comparator<IFile> getItemsComparator() {		
		return new Comparator<IFile>() {

			public int compare(IFile file1, IFile file2) {
				// first only compare the file name, not the full path, 
				// otherwise names don't show up alphabetically
				int result = file1.getName().compareTo(file2.getName()); 
				// if the names are identical, compare the full path also
				if (result==0)
					result = file1.getFullPath().toOSString().compareTo(file2.getFullPath().toOSString()); 
				return result;            	       	               
			}

		};
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
			throws CoreException {
		for (IFile file:scriptFiles)
		{
			contentProvider.add(file, itemsFilter);
		}

	}

	@Override
	public String getElementName(Object item) {
		return null;
	}
	
	private ILabelProvider getListLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				if(element == null)
					return ""; //$NON-NLS-1$
				return ((IFile)element).getName();
			}
		};
	}
	
	private ILabelProvider getDetailsLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				 
				return ((IFile)element).getProject().getLocation().toOSString().substring(1)+" - "+((IFile)element).getProjectRelativePath().toOSString(); //$NON-NLS-1$
			}
		};
	}
	
	class ScriptFileHistory extends SelectionHistory{

		@Override
		protected Object restoreItemFromMemento(IMemento memento) {
			
			return null;
		}

		@Override
		protected void storeItemToMemento(Object item, IMemento memento) {
			
		}
		
	}
	
	class ScriptFileFilter extends ItemsFilter {

		@Override
		public boolean isConsistentItem(Object item) {
			return true; 
		}

		@Override
		public boolean matchItem(Object item) {
			return matches(((IFile) item).getName());
		}

	}

}
