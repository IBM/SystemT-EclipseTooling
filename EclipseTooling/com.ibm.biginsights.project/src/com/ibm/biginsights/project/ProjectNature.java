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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.preference.PreferenceStore;
import org.osgi.framework.Bundle;

import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;

public class ProjectNature implements IProjectNature {

	public static final String NATURE_ID = "com.ibm.biginsights.projectNature"; //$NON-NLS-1$
	public static final String DEFAULT_SRC_FOLDER = "src"; //$NON-NLS-1$
	
	private IProject project;
	
	@Override
	public void configure() throws CoreException {			
	    createDefaultPreferences(project);
	    
	    addJavaNature(project);
	    
        processExtensions(project);
	}


	public static void addJavaNature(IProject project) throws CoreException {
	    // add Java nature if not there yet
	    IJavaProject javaProject = null;
        // set classpath entries for project
        Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
        
	    if (project.hasNature(JavaCore.NATURE_ID)) {  
	    	// existing project that already has the Java nature
	    	javaProject = JavaCore.create(project);
	    	for (IClasspathEntry entry:javaProject.getRawClasspath())
	    		entries.add(entry);
	    }
	    else {
	        // create folders first (need to exist before composing classpath
	        String[] paths = { DEFAULT_SRC_FOLDER }; //$NON-NLS-1$ 
	        addToProjectStructure(project, paths);
	
	        // also make the project a Java project to support Java Map/Reduce development            
	        ProjectSupport.addNature(project, JavaCore.NATURE_ID);
	        javaProject = JavaCore.create(project);

	        // add source folder (by default it's root of the project)
	        IFolder sourceFolder = project.getFolder(DEFAULT_SRC_FOLDER); //$NON-NLS-1$
	        entries.add(JavaCore.newSourceEntry(sourceFolder.getFullPath()));
	        
	        // add JRE container to classpath
	        entries.add(JavaRuntime.getDefaultJREContainerEntry());	       	        
	    }
                       
        // add default BigInsights library container - will be overridden in ProjectSupport.addBIProjectNature
       	entries.add(BigInsightsLibraryContainerInitializer.getInstance().getDefaultBigInsightsContainerEntry());            

        // now add all classpath entries to the Java project 
        javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);		
	}


    // creates a list of folders under the project
    private static void addToProjectStructure(IProject newProject, String[] paths) throws CoreException {
        for (String path : paths) {
            IFolder etcFolders = newProject.getFolder(path);
            createFolder(etcFolders);
        }
    }
    
    // method to create a folder - called by addFolderStructure
    private static void createFolder(IFolder folder) throws CoreException {
        IContainer parent = folder.getParent();
        if (parent instanceof IFolder) {
            createFolder((IFolder) parent);
        }
        if (!folder.exists()) {
            folder.create(false, true, null);
        }
    }

	@Override
	public void deconfigure() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}
	
	public static void setCurrentBIVersionInProperties(IProject project) {
		PreferenceStore preferenceStore = BIProjectPreferencesUtil.getPreferenceStore(project);
		// set the version of the BI feature that is currently active
		String version = null;
		Properties properties = new Properties();
		try {
			Bundle bundle = Platform.getBundle("com.ibm.biginsights.project"); ////$NON-NLS-1$
			if (bundle!=null) {
				// read version from the biginsights.version file that is stored in the com.ibm.biginsights.project plugin
				URL versionFileURL = bundle.getResource(BIConstants.BIGINSIGHTS_VERSION_FILE);
				if (versionFileURL!=null) {
					versionFileURL = FileLocator.toFileURL(versionFileURL);
					Path versionFileURLPath = new Path(URLDecoder.decode(versionFileURL.getPath(), BIConstants.UTF8));
					properties.load(new FileInputStream(versionFileURLPath.toOSString()));
					version = (String)properties.get(BIConstants.BIGINSIGHTS_VERSION_ID);
				}					
			}
		}
		catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
			version = BigInsightsLibraryContainerInitializer.getInstance().getDefaultBigInsightsContainerVersion();
		}

		preferenceStore.setValue(BIConstants.BIGINSIGHTS_FEATURE_VERSION, version);					

		try {
			preferenceStore.save();
		}
		catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
	}
	
	public static void createDefaultPreferences(IProject project) {
		String preferencesFile = project.getLocation() + File.separator + BIConstants.BIGINSIGHTS_PREF_FILE;
		PreferenceStore preferenceStore = new PreferenceStore(preferencesFile);
				
		//preferenceStore.setValue(BIConstants.SEARCHPATH_JAQLPATH, BIConstants.JAQL_PATH_BIGINSIGHTS_MODULES);		  
		try {
			preferenceStore.save();
		}
		catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
		}
		
		// set the current BI version
		setCurrentBIVersionInProperties(project);
	}

    // process extension point to add natures of plugins that contribute to the BigInsights nature
    private static void processExtensions(IProject project)
    {
    	IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("com.ibm.biginsights.contributions"); //$NON-NLS-1$
		for (IConfigurationElement element:config)
		{			
			String natureId = element.getAttribute("NatureId"); //$NON-NLS-1$
			if (natureId!=null && !natureId.isEmpty())
			{
			
				try {
					ProjectSupport.addNature(project, natureId);
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			}
		}    	    
    }
    
    // process extension point to migrate project to a newer version
    public static void processMigrateExtensions(IProject project, String oldVersion, String newVersion)
    {
    	IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor("com.ibm.biginsights.contributions"); //$NON-NLS-1$
		for (IConfigurationElement element:config)
		{			
			if (element.getAttribute("MigrationClass")!=null) { //$NON-NLS-1$
				try {
					
					Object migrationClass = (IBigInsightsMigration)element.createExecutableExtension("MigrationClass"); //$NON-NLS-1$
					if (migrationClass!=null && migrationClass instanceof IBigInsightsMigration) { // migrationClass is optional
						((IBigInsightsMigration)migrationClass).migrate(project, oldVersion, newVersion);	
					}				 
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
				}
			}
		}
    	    	
    }
    
    public static boolean doesProjectSourceFolderExist(IProject project) {
    	IFolder folder = null;
    	String srcFolder = getProjectSourceFolderFromClasspath(project);
    	if (srcFolder!=null) {
    		folder = project.getFolder(srcFolder);   
    	}
    	return folder!=null && folder.exists();    	
    }
    
	public static String getProjectSourceFolderFromClasspath(IProject project) {
		String result = null;
	
		IJavaProject javaProject = JavaCore.create(project);
    	
		IClasspathEntry[] classpathEntries;
		try {
			classpathEntries = javaProject.getResolvedClasspath(true);
			for (IClasspathEntry entry:classpathEntries) {
				if (IPackageFragmentRoot.K_SOURCE == entry.getContentKind()) {					
					result = entry.getPath().removeFirstSegments(1).toString();
					break; // if there are multiple source path, just return the first one
				}
			}
		} catch (JavaModelException e) {
			// something wrong with the classpath			
		}
		return result;
	}
	
}
