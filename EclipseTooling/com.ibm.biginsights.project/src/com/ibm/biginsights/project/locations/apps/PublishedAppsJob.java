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
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
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
import com.ibm.icu.text.Collator;

public class PublishedAppsJob extends Job {
	public HttpClient _client;
	private IBigInsightsLocation location;
	
	List<IBigInsightsApp> appList = new ArrayList<IBigInsightsApp>();

	public PublishedAppsJob(String hostname, String name, IBigInsightsLocation location) {
		super(name);
		this.location = location;
	}

	
	public IStatus run(IProgressMonitor monitor) {
		
		monitor.beginTask(Messages.Info_AppJob_Running, IProgressMonitor.UNKNOWN);
			
		String uri = location.generateAbsoluteURL(BIConstants.URL_APPLICATIONS);	//test error
		
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
                return parseApps(response);
			
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
	

	private IStatus parseApps(InputStream response) {
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
				int colNumber=0; 
				BigInsightsApp app = new BigInsightsApp();
				app.setLocation(location);
				
				for(int j=0; j < columnNodes.getLength() ;j++){
					Node columnNode = columnNodes.item(j);					
					if(columnNode.getNodeName().equals("column")){ //$NON-NLS-1$
						colNumber++;
						if(colNumber == 1){ //id
							String appID = columnNode.getTextContent();
							if(appID != null && appID.trim().length() > 0){
								app.setId(appID.trim());
							}
						}else if(colNumber == 2){ //name
							String appName = columnNode.getTextContent();
							if(appName != null && appName.trim().length() > 0){
								app.setAppName(appName.trim());
							}
						}else if(colNumber == 3){ //description
								String appDesc = columnNode.getTextContent();
								if(appDesc != null && appDesc.trim().length() > 0){
									app.setDescription(appDesc.trim());
								}
						}else if(colNumber == 4){ //creator
								String appCreator = columnNode.getTextContent();
								if(appCreator != null && appCreator.trim().length() > 0){
									app.setCreator(appCreator.trim());
								}
						}else if(colNumber == 6){ //zip path
							String zip = columnNode.getTextContent();
							if(zip != null && zip.trim().length() > 0){
								IPath zipPath = new Path(zip.trim());
								app.setZipPath(zipPath.toPortableString());
							}
						}else if(colNumber == 7){ //categories
							String categoriesString = columnNode.getTextContent();
							if(categoriesString != null && categoriesString.trim().length() > 0){
								List<String> categories = parseCategories(categoriesString.trim());
								app.getCategories().addAll(categories);
							}else{
								app.getCategories().add(Messages.Location_Category_Uncategorized);
							}
						}else if(colNumber == 8){ //status
							String status = columnNode.getTextContent();
							String displayStatus = Messages.Decoration_unknown;
							if(status != null && status.trim().length() > 0){
								status = status.trim();
								if(status.equals(BIConstants.APP_STATUS_UNDEPLOYED)){
									displayStatus = Messages.Decoration_published;
								}else if(status.equals(BIConstants.APP_STATUS_DEPLOYED)){
									displayStatus = Messages.Decoration_deployed;
								}
							}
							app.setStatus(displayStatus);
						}
					}					
				}
				appList.add(app);
			}
		}
		Collections.sort(appList, new Sorter());
		return Status.OK_STATUS;
	}

	private List<String> parseCategories(String categoriesString) {
		List<String> categories = new ArrayList<String>();
		
		if(categoriesString==null || categoriesString.length()==0){
			categories.add(Messages.Location_Category_Uncategorized);
			return categories;
		}
		
		StringTokenizer st = new StringTokenizer(categoriesString, ","); //$NON-NLS-1$
		while(st.countTokens()>0){
			String tag = st.nextToken().trim();					
			categories.add(tag);
		}		
		
		return categories;
	}


	public Collection<IBigInsightsApp> getApps() {
		return appList;
	}
	
	private class Sorter implements Comparator<IBigInsightsApp> {
		@Override
		public int compare(IBigInsightsApp p1, IBigInsightsApp p2) {					
			if(p1 instanceof BigInsightsApp &&
			   p2 instanceof BigInsightsApp ){
				
				String s1 = ((BigInsightsApp)p1).getAppName();
				String s2 = ((BigInsightsApp)p2).getAppName();
				
				Collator c = Collator.getInstance();
				return c.compare(s1, s2);						
			}					
			return 0;
		}        		
	}

}
