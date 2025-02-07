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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.util.Authenticator;
import com.ibm.biginsights.project.util.BIConstants;

public class AppCategoriesJob extends Job {
	public HttpClient _client;
	private IBigInsightsLocation location;
	
	List<String> categoryList = new ArrayList<String>();

	public AppCategoriesJob(String hostname, String name, IBigInsightsLocation location) {
		super(name);
		this.location = location;
	}

	
	public IStatus run(IProgressMonitor monitor) {
		
		monitor.beginTask(Messages.Info_AppJob_Running, IProgressMonitor.UNKNOWN);
			
		String uri = location.generateAbsoluteURL(BIConstants.URL_APPLICATIONS_CATEGORIES);	//test error
		
		GetMethod method = new GetMethod(uri);
		Authenticator.setSessionCookie(method, location);
		try {
			int statusCode = _client.executeMethod(method);
			
			if (statusCode == HttpStatus.SC_OK) {
				//make sure that xml is returned
				Header contentType  = method.getResponseHeader("Content-Type"); //$NON-NLS-1$
				if(contentType != null && !contentType.getValue().startsWith("text/xml")){ //$NON-NLS-1$
					//error not xml, must be login page returned
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
							 Messages.Error_AppJob_1+ " " + Messages.LOCATIONSERVERAUTHFAILED); //$NON-NLS-1$
				}
				InputStream response = method.getResponseBodyAsStream();
                return parseCategories(response);
			
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
	

	private IStatus parseCategories(InputStream response) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(response);
		} catch (ParserConfigurationException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					Messages.Error_AppJob_1+ " " + e.getMessage()); //$NON-NLS-1$
		} catch (SAXException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					Messages.Error_AppJob_1+ " " + e.getMessage()); //$NON-NLS-1$
		} catch (IOException e) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					Messages.Error_AppJob_1+ " " + e.getMessage()); //$NON-NLS-1$
		}
			
		
		if(doc != null){
			NodeList rowNodes = doc.getElementsByTagName("row"); //$NON-NLS-1$
			for(int i=0; i < rowNodes.getLength() ;i++){
				Node rowNode = rowNodes.item(i);
				NodeList columnNodes = rowNode.getChildNodes();				
				
				for(int j=0; j < columnNodes.getLength() ;j++){
					Node columnNode = columnNodes.item(j);					
					if(columnNode.getNodeName().equals("column")){ //$NON-NLS-1$						
						String category = columnNode.getTextContent();
						categoryList.add(category);
					}					
				}
			}
		}
		categoryList.add(Messages.Location_Category_Uncategorized);
		Collections.sort(categoryList);
		return Status.OK_STATUS;
	}

	public Collection<String> getCategories() {
		return categoryList;
	}
	

}
