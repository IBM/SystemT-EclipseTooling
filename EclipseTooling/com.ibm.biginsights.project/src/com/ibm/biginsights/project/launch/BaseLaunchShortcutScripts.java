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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.util.BIConstants;

public abstract class BaseLaunchShortcutScripts  implements ILaunchShortcut2 {
	
	protected abstract String getScriptName();
	protected abstract String getFileExtension();
	protected abstract String getLaunchConfigId();
	
	
	protected static Shell getShell() {
		return Activator.getActiveWorkbenchShell();
	}
	
	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {			
			searchAndLaunch(((IStructuredSelection)selection).toArray(), mode);
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		IResource resource = (IResource)input.getAdapter(IResource.class);
		
		if (resource!=null) {			
			searchAndLaunch(new Object[]{resource}, mode);
		}
		
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		// let the framework resolve configurations based on resource mapping
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		// let the framework resolve configurations based on resource mapping
		return null;
	}

	@Override
	public IResource getLaunchableResource(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object element = ss.getFirstElement();
				if (element instanceof IAdaptable) {
					return getLaunchableResource((IAdaptable)element);
				}
			}
		}
		return null;
	}
	
	public IResource getLaunchableResource(IEditorPart editorpart) {
		return getLaunchableResource(editorpart.getEditorInput());
	}

	private IResource getLaunchableResource(IAdaptable adaptable) {
		IResource resource = (IResource) adaptable.getAdapter(IResource.class);
		if (resource != null) {
			return resource;
		}
		return null;
	}
	
//	private IResource chooseResourceFile(IResource[]resources, String mode)
//	{
//		return null;
//	}		
//	
	protected void searchAndLaunch(Object[] objects, String mode)
	{
		IResource[] resources = null;
		if (objects!=null)
		{
			resources = findResourceFiles(objects);
			
			IResource resource = null;
			if (resources.length==0)
			{
				MessageDialog.openInformation(getShell(), Messages.bind(Messages.LAUNCHSHORTCUT_LAUNCH_DLG_TITLE, getScriptName()), 
														  Messages.bind(Messages.LAUNCHSHORTCUT_LAUNCH_DLG_ERROR, getScriptName()));
			}
			else if (resources.length>1)
			{
				//TODO: handle when more than one resource with the same name was found
				//resource = chooseResourceFile(objects, mode);
			}
			else
			{
				resource = resources[0];
			}
			if (resource!=null)
				launch(resource, mode);
		}
		
	}
	
	private IResource[] findResourceFiles(Object[]objects)
	{
		ArrayList<IResource>tempArray = new ArrayList<IResource>();
		
		for (Object o:objects)
		{			
			if (o instanceof IFile && ((IFile)o).getFileExtension().equals(getFileExtension())) 
			{ 
				tempArray.add((IFile)o);				
			}							
		}

		IResource[]result = new IResource[tempArray.size()>0 ? tempArray.size() : 0];
		return tempArray.toArray(result);
	}
	
	protected void launch(IResource resource, String mode)
	{
		ILaunchConfiguration config = findLaunchConfiguration(resource);
		boolean launchConfig = config!=null;
		boolean isNewConfig = false;
		if (config == null) {
			// force opening of Create Launch config (otherwise no location and JAQL path will be set)
			ILaunchConfigurationWorkingCopy workingCopy = createConfigurationWorkingCopy(resource); 			
			
			int result = DebugUITools.openLaunchConfigurationDialog(getShell(), workingCopy, BIConstants.RUN_LAUNCH_GROUP_ID, null);
			launchConfig = result==Window.OK;
			// save the config only if the user clicked OK
			if (launchConfig) {
				try {
					isNewConfig = true;
					config = workingCopy.doSave();
				} catch (CoreException e) {					
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			}
		}
		if (config != null && launchConfig && !isNewConfig) {			
			DebugUITools.launch(config, mode);
		}					
	}
	
	protected ILaunchConfiguration findLaunchConfiguration(IResource resource)
	{
		List<ILaunchConfiguration> candidateConfigs = null;
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(getConfigurationType());
			candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				if (config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "").equals(resource.getProjectRelativePath().toOSString())) { //$NON-NLS-1$
					if (config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(resource.getProject().getName())) { //$NON-NLS-1$
						candidateConfigs.add(config);
					}
				}
			}
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		int candidateCount = candidateConfigs.size();
		if (candidateCount == 1) {
			return (ILaunchConfiguration) candidateConfigs.get(0);
		} else if (candidateCount > 1) {
			return chooseConfiguration(candidateConfigs);
		}
		return null;
	}
	
	protected ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configList) {
		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle(Messages.LAUNCHSHORTCUT_CONF_DLG_TITLE);  
		dialog.setMessage(Messages.LAUNCHSHORTCUT_CONF_DLG_DESC);
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == Window.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;		
	}
		
	protected ILaunchConfigurationType getConfigurationType() {
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		return launchManager.getLaunchConfigurationType(getLaunchConfigId());		 
	}

	protected ILaunchConfigurationWorkingCopy createConfigurationWorkingCopy(IResource resource) {
		ILaunchConfigurationWorkingCopy workingCopy = null;
		try {
			ILaunchConfigurationType configType = getConfigurationType();
			workingCopy = configType.newInstance(null, DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName(resource.getName()));
			
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, resource.getProjectRelativePath().toOSString());
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, resource.getProject().getName());
															
		} catch (CoreException ce) {
			MessageDialog.openError(Activator.getActiveWorkbenchShell(), Messages.LAUNCHSHORTCUT_ERROR_TITLE, ce.getStatus().getMessage());	
		}
		return workingCopy;
	}
}
