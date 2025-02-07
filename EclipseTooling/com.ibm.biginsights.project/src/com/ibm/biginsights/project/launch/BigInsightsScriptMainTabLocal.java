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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BigInsightsLocation.FILESYSTEM;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;

public class BigInsightsScriptMainTabLocal extends BigInsightsMainTabLocal {

	private String _scriptName;
	private String _fileExtension;
	
	public BigInsightsScriptMainTabLocal(String mode, String scriptName, String fileExtension, boolean supportDeleteDirectory) {
		super(Messages.bind(Messages.MAINTAB_SCRIPT_TAB_MESSAGE, scriptName), mode, supportDeleteDirectory);
		this._scriptName = scriptName;
		this._fileExtension = fileExtension;
	}
	
	public String getName() {
		return Messages.bind(Messages.MAINTAB_SCRIPT_TAB_TITLE, _scriptName);
	}
	
	protected void createMainTypeExtensions(Composite parent) {
		// JavaMainTab creates lots of buttons for main type selection - need to make them invisible but have to create them, otherwise NPE in initialzeFrom method
		super.createMainTypeExtensions(parent);
		for (int i=0; i<parent.getChildren().length; i++) {
			Control c = parent.getChildren()[i];
			if (i>1 && c instanceof Button) {
				Button b = (Button)c;
				b.setVisible(false);
				if (b.getLayoutData() instanceof GridData)
					((GridData)b.getLayoutData()).exclude = true;			
			}
			parent.layout(true);
		}
		// change label of main class
		if (fMainText.getParent() instanceof Group)
			((Group)fMainText.getParent()).setText(Messages.bind(Messages.MAINTAB_SCRIPT_FILE_TITLE, _scriptName));		
	}
		
	protected void handleSearchButtonSelected() {
		IProject project = getBIProject();
		IProject[] projects = null;
		if ((project == null) || !project.exists()) {
			projects = this.getBIProjects();
		}
		else {
			projects = new IProject[]{project};
		}
		if (projects == null) {
			projects = new IProject[]{};
		}
		
		ArrayList<IFile>filesToSearch = new ArrayList<IFile>();
		for (IProject p:projects)
		{
			try {
				IResource[] members = p.members();
				filesToSearch.addAll(getFiles(members, _fileExtension));
			} catch (CoreException e) {
				Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			}				
		}
		
		FilteredScriptFilesDialog filteredScriptFilesDlg = new FilteredScriptFilesDialog(getShell(), filesToSearch);
		filteredScriptFilesDlg.setTitle(Messages.bind(Messages.MAINTAB_SELECT_FILE_TITLE, _scriptName));
		filteredScriptFilesDlg.setInitialPattern("**"); //$NON-NLS-1$
		if (filteredScriptFilesDlg.open() == Window.CANCEL) {
			return;
		}
		Object[] results = filteredScriptFilesDlg.getResult();	
		IFile file = (IFile)results[0];
		if (file != null) {
			fMainText.setText(file.getProjectRelativePath().toOSString());
			fProjText.setText(file.getProject().getName());
		}
	}
	
	protected boolean verifyFileName(String fileName) {
		boolean result = true;		
		IProject project = BIProjectPreferencesUtil.getProject(fProjText.getText().trim());
		
		if (fileName.length() == 0) {
			setErrorMessage(Messages.bind(Messages.MAINTAB_SCRIPT_FILE_MISSING, _scriptName));  
			result = false;
		}
		else {			
			IResource resource = project.findMember(fileName);
			if (resource==null || !resource.exists()) {
				setErrorMessage(Messages.bind(Messages.MAINTAB_SCRIPT_FILE_INVALID, _scriptName));				
				result = false;
			}
		}

		return result;
	}
	
	// retrieves all resources with the given file extension
	private ArrayList<IFile> getFiles(IResource[] resources, String extension)
	{
		ArrayList<IFile>resultArray = new ArrayList<IFile>();
		for (IResource resource:resources)
		{
			if (resource instanceof IFolder && !((IFolder)resource).isDerived()) // need to check for isDerived to not include files in bin
			{
				IResource[] files = null;
				try {
					files = ((IFolder)resource).members();
				} catch (CoreException e) {
					// nothing to do, just log error		 
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
				if (files!=null)
					resultArray.addAll(getFiles(files, extension));
			}
			else if (resource instanceof IFile && ((IFile)resource).getFileExtension()!=null &&((IFile)resource).getFileExtension().equals(extension))
			{ 
				resultArray.add((IFile)resource);
			}

		}
		return resultArray;
	}

	protected boolean verifyLocation(IBigInsightsLocation location) {
		boolean result = true;
		if (FILESYSTEM.GPFS.equals(location.getFileSystem()) && !location.isGPFSMounted()) {
			result = false;
			setErrorMessage(Messages.MAINTAB_NOT_SUPPORT_GPFS);						
		}
		return result;
	}
	
	protected void initializeMainTypeAndName(IJavaElement javaElement, ILaunchConfigurationWorkingCopy config) {
		// don't init main type and name for script files, otherwise a Java main class is selected
	}
	

}
