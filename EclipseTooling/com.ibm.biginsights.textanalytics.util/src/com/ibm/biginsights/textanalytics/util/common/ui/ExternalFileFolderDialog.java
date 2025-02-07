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

package com.ibm.biginsights.textanalytics.util.common.ui;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.internal.ide.dialogs.FileFolderSelectionDialog;

import com.ibm.biginsights.textanalytics.util.Activator;
import com.ibm.biginsights.textanalytics.util.Messages;

@SuppressWarnings("restriction")
public class ExternalFileFolderDialog extends FileFolderSelectionDialog{


	
	protected String allowedFileExtensions;
	protected String fileName;
	protected boolean viewAllFiles;
	protected String tempAllowedExtensions;
	protected boolean enableShowAllFilesOption = false;

	public ExternalFileFolderDialog(Shell parent, int mode) {
		super(parent, false, mode);
	}

	public void setAllowedFileExtensions(String allowedFileExtensions) {
		this.allowedFileExtensions = allowedFileExtensions;
	}

	public void setEnableShowAllFilesOption(boolean enableShowAllFilesOption) {
		this.enableShowAllFilesOption = enableShowAllFilesOption;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}


	public void createDialog(String title, String message){
		File file[] = File.listRoots();
		IFileStore fileStore = EFS.getLocalFileSystem().getStore(
				new Path(file[0].getAbsolutePath()));
		setInput(fileStore);
		setTitle(title);
		setMessage(message);
		
		addFilter(new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof LocalFile) {
						LocalFile file = (LocalFile) element;
						if(file.fetchInfo().isDirectory()){
							return true;
						}
						if(allowedFileExtensions != null && !allowedFileExtensions.isEmpty()){
							String fileName = file.getName();
							String extension = fileName.substring(fileName.indexOf(".") +1); //$NON-NLS-1$
								if (matchesExtension(extension)) {
									return true;	
								}
								else{
									return false;
								}
						}else{
							return true;
						}
						}
				return false;
			}
			
			private boolean matchesExtension(String extension){
				if(extension == null){
					return allowedFileExtensions.contains("NULL");//$NON-NLS-1$
				}else {
					
					String allowedExtArr[] = allowedFileExtensions.split(",");//$NON-NLS-1$
					for (int i = 0; i < allowedExtArr.length; i++) {
						if(extension.equals(allowedExtArr[i]))
							return true;
					}
					return false;
				}
			}

				
		});
		setValidator(new FileSelectionValidator());
		open();
		if(getFirstResult() != null){
			LocalFile result = (LocalFile) getResult()[0];
			String fileName = result.toString();
			if (fileName != null && fileName.trim().length() > 0) {
				this.fileName = fileName;
			}
		}
	
	}
	
	@Override	
	protected void cancelPressed() {
		super.cancelPressed();
	}
	
	@Override
	protected TreeViewer createTreeViewer(Composite parent) {
		TreeViewer treeViewer = super.createTreeViewer(parent);
		
		if(enableShowAllFilesOption){
			
			/**
			 * Check Box Button to display all files. If the user selects the check box, then we reset the allowed extension 
			 * and refresh the tree viewer. If user unselects the check box, then we pass the allowed extensions and refresh
			 * the tree viewer
			 */
			Button viewAllFilesButton = new Button(parent, SWT.CHECK);
			viewAllFilesButton.setText(Messages.getString("BrowseWorkspace.VIEW_ALL_FILES"));//$NON-NLS-1$
			tempAllowedExtensions = allowedFileExtensions;
			viewAllFilesButton.addSelectionListener(new SelectionAdapter(){
	
				@Override
				public void widgetSelected(SelectionEvent e) {
					viewAllFiles = !viewAllFiles;
					if(viewAllFiles){
						allowedFileExtensions = "";//$NON-NLS-1$
					}else{
						allowedFileExtensions = tempAllowedExtensions;
					}
					
					TreeViewer treeViewer = getTreeViewer();
					treeViewer.refresh();
					
				}
			});
		}
		
		return treeViewer;

	}



	class FileSelectionValidator implements ISelectionStatusValidator {
		@Override
		public IStatus validate(Object[] arg0) {
			Status fCurrStatus = new Status(IStatus.INFO, Activator.PLUGIN_ID,
					IStatus.INFO, "", null);//$NON-NLS-1$
			return fCurrStatus;
		}
	}
}
