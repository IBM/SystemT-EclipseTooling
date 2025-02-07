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
package com.ibm.biginsights.project.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.BigInsightsLibraryContainerInitializer;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.ProjectNature;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;

@SuppressWarnings("restriction")
public class BIProjectPreferencesUtil
{	
  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+          //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$
	
	public static IProject getSelectedProject() {
		ISelection selection = getActivePage().getSelection();
		IProject project = null;
		
		if(selection instanceof IStructuredSelection){
			IStructuredSelection structSelection = (IStructuredSelection) selection;
			Object element = structSelection.getFirstElement();

			if (element instanceof IProject) {
		          project = (IProject) element;
		        } else if (element instanceof IAdaptable) {
		          project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
		        }
		}
		return project;
	}
	
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}	
	
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null) {
			return w.getActivePage();
		}
		return null;
	}
	
	public static PreferenceStore getPreferenceStore(IProject project){
		String BIPreferencesFile = project.getLocation() + File.separator + BIConstants.BIGINSIGHTS_PREF_FILE;
		PreferenceStore preferenceStore = new PreferenceStore(BIPreferencesFile);
		try {
			preferenceStore.load();
			return preferenceStore;
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		return null;
	}
	
	public static IProject getProject(String projectName){
		IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
		
		try{
			IProject project = workspaceRoot.getProject(projectName);
			return project;
		}catch(Exception e){
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			return null;
		}
	}
	
	public static PreferenceStore initPreferenceStore(IProject project) throws IOException{		
		if(project != null){
			return BIProjectPreferencesUtil.getPreferenceStore(project);
		}
		return null;
	}
	
	public static String getJaqlPath(IProject project) throws IOException {
		PreferenceStore pref = BIProjectPreferencesUtil.getPreferenceStore(project);
		if (pref!=null)
			return  pref.getString(BIConstants.SEARCHPATH_JAQLPATH);
		return null;
	}
	
	/**
	 * Method returns the version of the BI tooling that is currently installed.
	 * Value can be used to determine whether project migration is required.
	 * Value is retrieved from biginsights.version file in com.ibm.biginsights.project plugin:
	 * 		product.version = 2.0.0.0
	 * @return BigInsights version that is currently installed
	 */
	public static String getInstalledBigInsightsVersion() {
		String result = null;
		Properties properties = new Properties();
		try {
			Bundle bundle = Platform.getBundle("com.ibm.biginsights.project"); ////$NON-NLS-1$
			if (bundle!=null) {
				URL versionFileURL = bundle.getResource(BIConstants.BIGINSIGHTS_VERSION_FILE);
				if (versionFileURL!=null) {
					versionFileURL = FileLocator.toFileURL(versionFileURL);
					Path versionFileURLPath = new Path(URLDecoder.decode(versionFileURL.getPath(), BIConstants.UTF8));
					properties.load(new FileInputStream(versionFileURLPath.toOSString()));
					result = (String)properties.get(BIConstants.BIGINSIGHTS_VERSION_ID);					
				}					
			}
		}
		catch(IOException e) {
			// can't retrieve BI version
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		return result;
	}
	
	/**
	 * Method returns BI version with which the project was created.
	 * Value can be used to determine whether project migration is required.
	 * Value is retrieved from .biginsights file:
	 * 		com.ibm.biginsights.version=2.0.0.0
	 * Compare with constants in BIConstants file:
	 * 	public static final String BIGINSIGHTS_VERSION_V12 = "1.2.0.0"; 
	 *	public static final String BIGINSIGHTS_VERSION_V13 = "1.3.0.0"; 
	 *	public static final String BIGINSIGHTS_VERSION_V1301 = "1.3.0.1";
	 *	public static final String BIGINSIGHTS_VERSION_V1302 = "1.3.0.2";
	 *	public static final String BIGINSIGHTS_VERSION_V1400 = "1.4.0.0";
	 *	public static final String BIGINSIGHTS_VERSION_V1401 = "1.4.0.1";
	 *	public static final String BIGINSIGHTS_VERSION_V2 = "2.0.0.0"; 
	 * @param project
	 * @return BigInsights version with which the project was created
	 */
	public static String getBigInsightsProjectVersion(IProject project) {
		String result = null;
		try {
			if (project.hasNature(ProjectNature.NATURE_ID)) {
				PreferenceStore pref = BIProjectPreferencesUtil.getPreferenceStore(project);			
				result = pref.getString(BIConstants.BIGINSIGHTS_FEATURE_VERSION);				
			}
		} catch (CoreException e) {
			// result will be null
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		return result;
	}	
	
	/**
	 * Returns true if version1 is lower version than version2
	 * @param version1
	 * @param version2
	 * @return true if version1<version2
	 */
	public static boolean isLower(String version1, String version2) {
		// normalize versions first to ensure vendor is removed
		
		Version projectVersion = new Version(removeVendorFromVersion(version1));
		Version biVersion = new Version(removeVendorFromVersion(version2));
		
		return projectVersion.compareTo(biVersion)<0;
	}

	/**
	 * Returns true if version1 is at least version2
	 * @param version1
	 * @param version2
	 * @return true if version1>=version2
	 */
	public static boolean isAtLeast(String version1, String version2) {
		Version projectVersion = new Version(removeVendorFromVersion(version1));
		Version biVersion = new Version(removeVendorFromVersion(version2));
		
		return projectVersion.compareTo(biVersion)>=0;
	}
	
	private static String removeVendorFromVersion(String version) {		
		String[]parts = version.split(" ");
		return parts[0];		
	}

	/**
	 * returns BI version associated with the BI project. If no BI classpath entry is found, returns default container .
	 * @param version of BI associated with project
	 * @return
	 */
	public static String  getBigInsightsLibrariesVersion(IProject project) {
		String version = null;	
		// get version of BI container from classpath (used to be in .biginsights file)
		IJavaProject javaProject = null;
		try {
	        if (project.isNatureEnabled(JavaCore.NATURE_ID)) {
	        	javaProject = JavaCore.create(project);
	        	// find the BIGINSIGHTS_LIBS_CONTAINER entry in the classpath 
	        	for (IClasspathEntry entry:javaProject.getRawClasspath())
	        	{
	        		if (entry.getPath().toOSString().contains(BIConstants.CONTAINER_ID))
	        		{        			
	        			String entryVersion = entry.getPath().segment(1);
	        			if (BIConstants.CONTAINER_ID_DEFAULT.equals(entryVersion.toUpperCase())) {
	        				// get the default container definition and return entries from there
	        				version = "V"+BigInsightsLibraryContainerInitializer.getInstance().getDefaultBigInsightsContainerVersion(); //$NON-NLS-1$
	        			}
	        			else {
	        				version = entryVersion.substring(entryVersion.lastIndexOf("V")); //$NON-NLS-1$
	        			}
	        			break;
	        		}
	        	}
	        }
		}
		catch (Exception ex) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.getMessage()));
		}
			
		// if version is null because the classpath entry for BigInsights was removed from the project, assume the default container, so no null is returned
		if (version==null)
			version = BigInsightsLibraryContainerInitializer.getInstance().getDefaultBigInsightsContainerVersion();

		return version;	
	}
	
	/**
	 * Checks if the projects's library versions match the server version
	 * @param project
	 * @param location
	 * @return
	 */
	public static boolean areProjectAndServerVersionsEqual(IProject project, IBigInsightsLocation location){
		if(project == null || location == null){
			return false;
		}
		
		//server version
		String serverBIVersion = location.getVersionWithVendor();
		//remove the v if it is there
		if(serverBIVersion.startsWith("v")){ //$NON-NLS-1$
			serverBIVersion = serverBIVersion.substring(1);
		}
					 
		//project library version
		String projectBIVersion = BIProjectPreferencesUtil.getBigInsightsLibrariesVersion(project);
		if(projectBIVersion.startsWith("v") || projectBIVersion.startsWith("V")){ //$NON-NLS-1$ //$NON-NLS-2$
			projectBIVersion = projectBIVersion.substring(1);
		}
	
		if(projectBIVersion.equals("1.3") || projectBIVersion.isEmpty()){ //$NON-NLS-1$
			//make it 4 part name
			projectBIVersion = "1.3.0.0"; //$NON-NLS-1$
		}
		
		//Activator.getDefault().getLog().log(new Status(IStatus.OK, Activator.PLUGIN_ID, serverBIVersion));
		//Activator.getDefault().getLog().log(new Status(IStatus.OK, Activator.PLUGIN_ID, projectBIVersion));

		if(!location.isYarn() && serverBIVersion.equals(projectBIVersion)){
			return true;
		}
    else if (location.isYarn() && projectBIVersion.equals(serverBIVersion + " " + BIConstants.BIGINSIGHTS_VERSION_QUALIFIER_YARN)) { //$NON-NLS-1$
      return true;
    }

		return false;
	}
	
	public static IProject[] getBIProjects()
	{
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ArrayList<IProject>tempArray = new ArrayList<IProject>();
		for (IProject project:projects)
		{
			try {
				if (project.hasNature(ProjectNature.NATURE_ID))				
					tempArray.add(project);				
			} catch (CoreException e) {
				// don't put the project into the list				
			}
		}
				
		IProject[]result = new IProject[tempArray.size()>0 ? tempArray.size() : 0];
		tempArray.toArray(result);
		return result;
		
	}

	public static boolean switchToBigInsightsPerspective(Shell shell, String message) {
		// switch to BI perspective if we are not in yet 
		boolean result = false;
		try {			
			if (!BIConstants.BI_PERSPECTIVE_ID.equals(Activator.getActiveWorkbenchWindow().getActivePage().getPerspective().getId())) {
				// get the setting for switching perspectives
				String promptForPerspectiveSetting = IDEWorkbenchPlugin.getDefault().getPreferenceStore().getString(IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE);
				MessageDialogWithToggle msgBox = null;
				if (IDEInternalPreferences.PSPM_PROMPT.equals(promptForPerspectiveSetting))
				{					
					msgBox = MessageDialogWithToggle.openYesNoQuestion(
							shell,
							Messages.SWITCH_PERSPECTIVE_TITLE,
							message,
							Messages.NEWBIPROJECTWIZARD_QUESTION,
							false, null, null); //$NON-NLS-1$
					
					if (msgBox.getToggleState()) {
						// remember decision
						IDEWorkbenchPlugin.getDefault().getPreferenceStore().setValue(
								IDEInternalPreferences.PROJECT_SWITCH_PERSP_MODE, 
								msgBox.getReturnCode()==IDialogConstants.YES_ID ? IDEInternalPreferences.PSPM_ALWAYS : IDEInternalPreferences.PSPM_NEVER);
					}
				}
				
				if (IDEInternalPreferences.PSPM_ALWAYS.equals(promptForPerspectiveSetting) || (msgBox!=null && msgBox.getReturnCode()==IDialogConstants.YES_ID)) {					
					Activator.getActiveWorkbenchWindow().getWorkbench().showPerspective(BIConstants.BI_PERSPECTIVE_ID, Activator.getActiveWorkbenchWindow());
					result = true;											
				}
			}
			else 
				result = true;
		} catch (Exception e) {
			// don't switch to perspective
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		return result;
	}
	
	public static IStatus assembleJar(String jarName, String[] jarContentFiles, final String[] jarContentJars, final IProgressMonitor monitor) {
		final JarPackageData jarPackage = new JarPackageData();
		
		jarPackage.setExportJavaFiles(false);
		jarPackage.setExportClassFiles(true);		
		jarPackage.setOverwrite(true);	
		jarPackage.setCompress(true);

		final File jarFile = FileUtils.createValidatedFile(jarName);
		jarPackage.setJarLocation(new Path(jarFile.getAbsolutePath()));
		
		// add the files into the JAR
		IFile[] files = new IFile[jarContentFiles.length];
		
		// first add the files from the list of files
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		int counter = 0;
		for (String fileName:jarContentFiles) {
			if (fileName!=null && !fileName.isEmpty()) {
				// all resource path are relative to the workspace root
				IFile file = root.getFile(new Path(fileName));
				if (file.exists()) {				
					files[counter] = file;
					counter++;
				}
			}
		}		
						
		jarPackage.setElements(files);

		// execute
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				IJarExportRunnable runnable = jarPackage.createJarExportRunnable(Display.getDefault().getActiveShell());
			    try {
					runnable.run(monitor);
				} catch (Exception e) {
					IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage());
					Activator.getDefault().getLog().log(status);									
				} 
			}
		});
		
		// add additional JARs under the lib folder inside the JAR (will be extracted by hadoop RunJar and put into the classpath)
		if (jarContentJars!=null && jarContentJars.length>0) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {				
					try {
						addFilesToJar(jarFile, jarContentJars);
					} catch (IOException e) {					
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
					}
				}
			});
		}

		return new Status(IStatus.OK, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
	}
	
	private static void addFilesToJar(File jarFile, String[] jarContentJars) throws IOException {

		// need to create a copy of the jar from which we will read and then write again into the original jar
		String jarLocation = jarFile.getAbsolutePath().substring(0, jarFile.getAbsolutePath().indexOf(jarFile.getName()));
		File tempFile = FileUtils.createValidatedFile(jarLocation+UUID.randomUUID()+BIConstants.JAR_FILE_EXTENSION);			
		
		if (!jarFile.renameTo(tempFile))
			throw new IOException("Could not rename jar to temp file "+tempFile.getAbsolutePath()); //$NON-NLS-1$

		byte[] readBuffer= new byte[4096];
		
		ZipInputStream inputStream = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(jarFile));

		// write the original content
		ZipEntry entry = inputStream.getNextEntry();
		while (entry!=null) {
			String name = entry.getName();
			outputStream.putNextEntry(new ZipEntry(name));
			int len;
			while ((len=inputStream.read(readBuffer))>0) {
				outputStream.write(readBuffer, 0, len);
			}
			entry = inputStream.getNextEntry();
		}
		inputStream.close();
		
		// write the additional files
		for (String jarFileString:jarContentJars) {
			File newFile = FileUtils.createValidatedFile(jarFileString);
			InputStream fileInputStream = new FileInputStream(newFile);
			outputStream.putNextEntry(new ZipEntry(BIConstants.JAR_LIB_FOLDER+"/"+newFile.getName())); //$NON-NLS-1$
			int len;
			while ((len=fileInputStream.read(readBuffer))>0) {
				outputStream.write(readBuffer, 0, len);
			}	
			outputStream.closeEntry();
			fileInputStream.close();
		}
				
		outputStream.close();
		// delete temp file
		tempFile.delete();
		
	}
}
