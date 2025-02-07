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
package com.ibm.biginsights.project.sampleProjects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.util.Authenticator;
import com.ibm.biginsights.project.util.BIConnectionException;
import com.ibm.biginsights.project.util.BIConstants;

@SuppressWarnings("restriction")
public class SampleProjectImportJob extends Job implements IOverwriteQuery
{
  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
      "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

	private IBigInsightsLocation _location;
	private String _name;			
	private String _fileLocation;
	private String _projectSuffix;

	public SampleProjectImportJob(IBigInsightsLocation location, String name, String projectSuffix, String fileLocation) {
		super(Messages.SAMPLEPROJECTIMPORTJOB_RETRIEVING_PROJECTS_JOB_NAME); 
		this._location = location;
		this._name = name;
		this._fileLocation = fileLocation;
		this._projectSuffix = projectSuffix;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		monitor.beginTask(Messages.SAMPLEPROJECTIMPORTJOB_RETRIEVING_PROJECTS_JOB_NAME, IProgressMonitor.UNKNOWN);

		String urlZip = _location.generateAbsoluteURL(BIConstants.URL_FILEDOWNLOAD+"?name="+_name+"&target="+_fileLocation+"&fstype=local"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String zipLocation = Platform.getLocation().toOSString()+File.separator+this._name;
		
		GetMethod method = new GetMethod(urlZip);		
		Authenticator.setSessionCookie(method, _location);		
		
		try {
			HttpClient httpClient = null;
			try {
				httpClient = _location.getHttpClient();
			} catch (BIConnectionException e) {
				_location.handleBIConnectionExceptionFromThread(e);
			}

			int statusCode = httpClient.executeMethod(method);
			
			if (statusCode == HttpStatus.SC_OK) {
				//make sure that logon page is not returned
				Header contentType  = method.getResponseHeader("Content-Type"); //$NON-NLS-1$
				if(contentType != null && contentType.getValue().startsWith("text/html")){ //$NON-NLS-1$
					//error not zip, must be login page returned
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
							 Messages.Error_AppJob_1+ " " + Messages.LOCATIONSERVERAUTHFAILED); //$NON-NLS-1$
				}
				
				InputStream response = method.getResponseBodyAsStream();
				
				//create zip file
				try {
					//write out the zip file					
					File file = FileUtils.createValidatedFile (zipLocation); //$NON-NLS-1$
					FileOutputStream out = new FileOutputStream(file);
					
					
					if (response != null) {
					    final int BUF_SIZE = 1024;
					    byte[] buf = new byte[BUF_SIZE];
					    int len = response.available();
					    while ( (len=response.read(buf)) != -1  ) {
					    	out.write(buf, 0, len);
					    }
					}
					out.flush();
					out.close();
					response.close();
					
					// retrieve actual project name from .project file
					String projectName = null;										
					ZipFile zipfile = new ZipFile(zipLocation); //$NON-NLS-1$
					ZipLeveledStructureProvider provider = new ZipLeveledStructureProvider(zipfile);
					
					// all our projects have the .project file in the root
					ZipEntry projectEntry = zipfile.getEntry(".project");		//$NON-NLS-1$			
					InputStream stream = provider.getContents(projectEntry);
					if (stream!=null) {
						IProjectDescription projDesc = IDEWorkbenchPlugin.getPluginWorkspace().loadProjectDescription(stream);
						stream.close();
						projectName = projDesc.getName();
					}					
				
					final IWorkspace workspace = ResourcesPlugin.getWorkspace();								
					final IProject project = workspace.getRoot().getProject(projectName!=null ? projectName : this._name.substring(0, this._name.indexOf(this._projectSuffix)));
					if (project.exists()) {
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
								 Messages.SAMPLEPROJECTSJOB_ERROR+ " "+Messages.SAMPLEPROJECTSJOB_PROJECT_EXISTS); 						 //$NON-NLS-1$
					}
					else {						
							
						ImportOperation op = new ImportOperation(project.getFullPath(), provider.getRoot(), provider, this, provider.getChildren(provider.getRoot()));
						op.run(monitor);
						zipfile.close();						
						return Status.OK_STATUS;
					}
					
				}
				catch(Exception ex) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
							 Messages.SAMPLEPROJECTSJOB_ERROR+ " "+ex.getMessage()); //$NON-NLS-1$
				}
			
			 }else{
				 //error
				 return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
						 Messages.Error_AppJob_1+ " " + statusCode); //$NON-NLS-1$
			 }
			 
		} catch (HttpException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					Messages.Error_AppJob_1+ " " + e.getMessage()); //$NON-NLS-1$
		} catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					Messages.Error_AppJob_1+ " " + e.getMessage()); //$NON-NLS-1$
		}finally{ 
			method.releaseConnection();	
			// in any case try to delete the imported file if it exists
			File f = FileUtils.createValidatedFile (zipLocation);
			if (f.exists()) {
				f.delete();
			}
		}			
	}


	@Override
	public String queryOverwrite(String pathString) {
		// we don't allow import if project already exists
		return IOverwriteQuery.NO;
	}
}
