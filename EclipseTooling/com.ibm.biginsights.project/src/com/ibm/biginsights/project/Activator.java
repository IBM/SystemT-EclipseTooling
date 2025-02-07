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
package com.ibm.biginsights.project;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.ibm.biginsights.project.MigrateProject.MigrationTestResult;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;
import com.ibm.biginsights.project.util.ImageDecorator;
import com.ibm.biginsights.project.util.PrereqChecker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.ibm.biginsights.project"; //$NON-NLS-1$
	public static final String PREFERENCES_NODE = "/BigInsights/"; ////$NON-NLS-1$
	public static String DECORATED_IMAGE_APPFOLDER_DEPLOYED = "DECORATED_IMAGE_APPFOLDER_DEPLOYED"; //$NON-NLS-1$
	
	private HashMap<String, Image> images = new HashMap<String, Image>();
	private Boolean isChmodInstalled = null;
	private Boolean isInternalBrowserOK = null;
	
	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		// launch project migration if necessary
		Job job = new Job(Messages.ACTIVATOR_MIGRATION_JOB_NAME) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final ArrayList<String> projects = new ArrayList<String>();
				final ArrayList<String> closedProjects = new ArrayList<String>();		
				final ArrayList<String> errorProjects = new ArrayList<String>();
				boolean showTAMessageForProjects = false;
				// on start-up check whether any of the BI projects need to be migrated
				IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
				for (IProject project:workspaceRoot.getProjects()) {
					if (!project.isOpen()) {
						closedProjects.add(project.getName());
					}
					else {
						try {							
							if (project.hasNature(ProjectNature.NATURE_ID)) {
								MigrationTestResult result = MigrateProject.isMigrationRequired(project);
								if (!showTAMessageForProjects) // only show TA message related to modular AQL, if coming from projects lower than 2.1
									showTAMessageForProjects = BIProjectPreferencesUtil.isLower(result.projectVersion, BIConstants.BIGINSIGHTS_VERSION_V21);
								if (result.isMigrationRequired) {
									ProjectSupport.migrateProject(project, 
											// always set the lib container to the latest version
											BigInsightsLibraryContainerInitializer.getInstance().getDefaultBigInsightsContainerEntry(), 
											result.projectVersion, result.bigInsightsVersion);
									projects.add(project.getName());
								}
							}
						}
						catch (Exception ex) {
							errorProjects.add(project.getName());
						}
					}
				}
				final boolean showTAMessage = showTAMessageForProjects;
				if (!projects.isEmpty() || !errorProjects.isEmpty()) { // show message if migration was necessary and either was successful or failed
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openInformation(getActiveWorkbenchShell(), Messages.ACTIVATOR_MIGRATION_DLG_TITLE, 
									(projects.isEmpty() ? "" : Messages.ACTIVATOR_MIGRATION_DLG_MIGRATED+ //$NON-NLS-1$
									StringUtils.join(projects, ", ")+"\n\n")+ //$NON-NLS-1$ //$NON-NLS-2$+
									((projects.isEmpty() || !showTAMessage) ? "" : (Messages.ACTIVATOR_MIGRATION_TA+"\n\n"))+//$NON-NLS-1$ //$NON-NLS-2$+
									(errorProjects.isEmpty() ? "" : Messages.ACTIVATOR_MIGRATION_DLG_MIGRATE_ERROR+ //$NON-NLS-1$
									StringUtils.join(errorProjects, ", ")+"\n\n")+ //$NON-NLS-1$ //$NON-NLS-2$
									(closedProjects.isEmpty() ? "" : Messages.ACTIVATOR_MIGRATION_DLG_CLOSED+ //$NON-NLS-1$
									StringUtils.join(closedProjects, ", "))); //$NON-NLS-1$
						}});
				}

			return Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.schedule();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		disposeImages();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}	
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}	

	public static ImageDescriptor getImageDescriptor(String path) {
		ImageDescriptor descriptor = imageDescriptorFromPlugin(PLUGIN_ID, path);
		if (descriptor==null) {
			descriptor = createImageDescriptor(Activator.getDefault().getBundle(), path);
			if (descriptor!=null) {
				plugin.getImageRegistry().put(path, descriptor);				
			}
		}
		return descriptor;
		
	}
	
	private static ImageDescriptor createImageDescriptor(Bundle bundle, String path) {
		URL url = getDefault().getClass().getResource(path);
		if (url!=null) {
			return ImageDescriptor.createFromURL(url);
		}
		return null;
	}
	
	public Image getImage(String path){
		if(images.containsKey(path)){
			return images.get(path);
		}
		Image newImage = getImageDescriptor(path).createImage();
		images.put(path, newImage);
		return newImage;
	}
	
	public void disposeImages(){
		for(Iterator<String> itr=images.keySet().iterator(); itr.hasNext();){
			String path = itr.next();
			Image image = images.get(path);
			if(!image.isDisposed()){
				image.dispose();
			}
		}
	}
	
	public Image getDecoratedImage(String type){
		if(images.containsKey(type)){
			return images.get(type);
		}
		if(type.equals(DECORATED_IMAGE_APPFOLDER_DEPLOYED)){
			Image baseImage = Activator.getDefault().getImage("/icons/defaultApp_16x.gif"); //$NON-NLS-1$
			
			Image decoratorImage = Activator.getDefault().getImage("/icons/greencircle_arrow_ovr.gif"); //$NON-NLS-1$
			ImageDecorator decorator = new ImageDecorator(baseImage, decoratorImage, 9, 8);

			Image compositeImage = decorator.createImage();
			images.put(DECORATED_IMAGE_APPFOLDER_DEPLOYED, compositeImage);
			return compositeImage;
		}
		return null;
	}
	
	// checks on a Windows platform that chmod command is available
	// need to install CYGWIN or other tool to get linux commands
	public boolean isChmodInstalled(){
		
		if(isChmodInstalled == null){
			if(System.getProperty("os.name").toLowerCase().indexOf("win") == -1){ //$NON-NLS-1$ //$NON-NLS-2$
				//if not windows platform then check not needed
				isChmodInstalled = new Boolean(true); 
			}else{
				isChmodInstalled = new Boolean(PrereqChecker.isChmodInstalled());
			}
		}
		
		if(!isChmodInstalled.booleanValue()){
			Thread uithread = Display.getDefault().getThread();
			if(Thread.currentThread().getId() == uithread.getId()){
				MessageDialog.openError(getActiveWorkbenchShell(), Messages.Error_Title, Messages.PREREQ_CHMOD_NOTINSTALLED);
			}else{
				Display.getDefault().syncExec(new Runnable(){
					@Override
					public void run() {
						MessageDialog.openError(getActiveWorkbenchShell(), Messages.Error_Title, Messages.PREREQ_CHMOD_NOTINSTALLED);					
					}
				});
			}
		}
		return isChmodInstalled.booleanValue();
	}
	
	public boolean isInternalBrowserOK(){
		
		if(isInternalBrowserOK == null){
			if(System.getProperty("os.name").toLowerCase().indexOf("win") == -1){ //$NON-NLS-1$ //$NON-NLS-2$
				isInternalBrowserOK = new Boolean(PrereqChecker.isXULRunnerPathSet());
			}else{
				isInternalBrowserOK = new Boolean(true);
//				isInternalBrowserOK = new Boolean(PlatformUI.getWorkbench().getBrowserSupport().isInternalWebBrowserAvailable());
			}
		}
		
		
		return isInternalBrowserOK.booleanValue();
	}
}
