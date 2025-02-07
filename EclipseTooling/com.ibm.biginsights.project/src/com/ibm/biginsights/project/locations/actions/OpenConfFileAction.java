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
package com.ibm.biginsights.project.locations.actions;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;

public class OpenConfFileAction extends Action {
	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;

	public OpenConfFileAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
		setText(Messages.OPENCONFFILEACTION_TITLE);
		this.page = page;
		this.selectionProvider = selectionProvider;
		
	}
	
	public boolean isEnabled() {
		ISelection selection = selectionProvider.getSelection();

		boolean result = false;
	    IStructuredSelection structuredSelection = (IStructuredSelection) selection;	    
	    if (structuredSelection.size() >= 1)
	    {
	    	// if all elements in the selection are File objects, open them
	    	result = true;
			for (Iterator<Object> it = structuredSelection.iterator(); it.hasNext();) {
				Object o = it.next();
				if (!(o instanceof File) || ((File)o).isDirectory()) {
					result = false;
					break;
				}
			}
	    }

		return result;
	}
	
	public void run() {		
		ISelection selection = selectionProvider.getSelection();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		for (Iterator<Object> it = structuredSelection.iterator(); it.hasNext();) {
			Object o = it.next();
			if (o instanceof File && ((File)o).isFile()) {
				openFile(page, (File)o);
			}
		}
	}
	
	public static void openFile(IWorkbenchPage page, File file) {
		IPath path = new Path(file.getAbsolutePath());
		IFileStore fileStore =  EFS.getLocalFileSystem().getStore(path);	
		if (page==null) {
			// when launched from double-click
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window!=null)
				page = window.getActivePage();
		}
		try {		
			// since openEditor will reuse an existing editor and I didn't find a way of refreshing the editor programmatically
			// (the file is not an IResource because they are not workspace resources)
			// I have to open it once and then close it and reopen it to make sure it has the latest content	
			IEditorPart editor = IDE.openEditorOnFileStore(page, fileStore);
			page.closeEditor(editor, false);
			IDE.openEditorOnFileStore(page, fileStore);
		} catch (PartInitException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
	}
}
