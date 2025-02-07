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
package com.ibm.biginsights.project.locations.apps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.util.Authenticator;
import com.ibm.biginsights.project.util.BIConstants;

public class ImportAppJob extends Job
{
  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
      "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

	public HttpClient _client;
	public String urlZip;
	public String localZipLocation;
	public IBigInsightsLocation loc;

	public ImportAppJob() {
		super(Messages.IMPORTAPPJOB_TITLE);
	}

	
	public IStatus run(IProgressMonitor monitor) {
		
		monitor.beginTask(Messages.Info_AppJob_Running, IProgressMonitor.UNKNOWN);
		
		GetMethod method = new GetMethod(urlZip);		
		Authenticator.setSessionCookie(method, loc);		
		
		try {
			int statusCode = _client.executeMethod(method);
			
			if (statusCode == HttpStatus.SC_OK) {
				//make sure that logon page is not returned
				Header contentType  = method.getResponseHeader("Content-Type"); //$NON-NLS-1$
				if(contentType != null && contentType.getValue().startsWith("text/html")){ //$NON-NLS-1$
					//error not zip, must be login page returned
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
							 Messages.Error_AppJob_1+ " " + Messages.LOCATIONSERVERAUTHFAILED); //$NON-NLS-1$
				}
				
				InputStream response = method.getResponseBodyAsStream();
                return unzip(response);
			
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
			monitor.done();
		}
	}
	

	private IStatus unzip(InputStream in) {
		try {
			//write out the zip file
			File file = FileUtils.createValidatedFile (localZipLocation + "/"+BIConstants.APP_ZIP_NAME); //$NON-NLS-1$
			FileOutputStream out = new FileOutputStream(file);
			
			
			if (in != null) {
			    final int BUF_SIZE = 1024;
			    byte[] buf = new byte[BUF_SIZE];
			    int len = in.available();
			    while ( (len=in.read(buf)) != -1  ) {
			    	out.write(buf, 0, len);
			    }
			}
			out.flush();
			out.close();
			in.close();
			
			//read and unzip files
			ZipFile zipfile = new ZipFile(localZipLocation + "/"+BIConstants.APP_ZIP_NAME); //$NON-NLS-1$
		    Enumeration entries = zipfile.entries();
		    while (entries.hasMoreElements()) {
		      ZipEntry entry = (ZipEntry) entries.nextElement();
		      
		      //create directories if not there
		      File entryfile = FileUtils.createValidatedFile (localZipLocation +"/"+BIConstants.APP_FOLDER+"/"+entry.getName()); //$NON-NLS-1$ //$NON-NLS-2$		     
		      
		      if(entryfile.isDirectory() || entry.getName().endsWith("/") || entry.getName().endsWith("\\")){ //$NON-NLS-1$ //$NON-NLS-2$
		    	  new File(entryfile.getAbsolutePath()).mkdirs(); 
		      }else{
		    	  new File(entryfile.getParent()).mkdirs();
		    	  
			      //writ out file
			      out = new FileOutputStream(localZipLocation +"/"+BIConstants.APP_FOLDER+"/"+entry.getName()); //$NON-NLS-1$ //$NON-NLS-2$
			      in = zipfile.getInputStream(entry);
			      if (in != null) {
					    final int BUF_SIZE = 1024;
					    byte[] buf = new byte[BUF_SIZE];
					    int len ;
					    while ( (len=in.read(buf)) != -1  ) {
					    	out.write(buf, 0, len);
					    }
					}
					out.flush();
					out.close();
					in.close();
		      }
		    }
		    
		    //delete zip file after unzipping
		    file.delete();
		    
		} catch (FileNotFoundException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
			          e.getMessage());
		} catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					e.getMessage());
		}
		
		
		return Status.OK_STATUS;
	}


}
