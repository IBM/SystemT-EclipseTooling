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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.net.ssl.SSLHandshakeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.BigInsightsLocation;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.locations.LocationRegistry;

public class Authenticator {
  
  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+            //$NON-NLS-1$
      "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	public static void authenticate(HttpClient httpclient, BigInsightsLocation loc) throws BIConnectionException{

		boolean isAuthenticated = false;
		String userId = loc.getUserName();
		String password = loc.getPassword();		
	
		try {
			URL authCheckUrl = new URL(loc.generateAbsoluteURL(BIConstants.URL_USERINFO));
			
			isAuthenticated = performAuthenticationCheck(httpclient, authCheckUrl, loc);
									
			if (!isAuthenticated && userId != null && password != null) {				
				isAuthenticated = performAuthentication(loc, httpclient, authCheckUrl, userId, password);
				if (isAuthenticated) {					
					isAuthenticated = performAuthenticationCheck(httpclient, authCheckUrl, loc);
					if (isAuthenticated) {
						loc.getHttpSession().setLastServerRequest(new Date());
					}
				}else{					
					loc.setPassword(null);
					loc.resetHttpClient();
					throw new BIConnectionException(BIConnectionException.REASON_INVALID_HTTPERROR, 200, Messages.LOCATIONSERVERAUTHFAILED);
				}
			}
		} catch (MalformedURLException e) {
			throw new BIConnectionException(BIConnectionException.REASON_INVALID_HTTPERROR, -1, 
					e.getMessage()!= null ? e.getMessage() : ""); //$NON-NLS-1$
		} catch (HttpException e) {
			throw new BIConnectionException(BIConnectionException.REASON_INVALID_HTTPERROR, e.getReasonCode(), 
					e.getMessage()!= null ? e.getMessage() : ""); //$NON-NLS-1$
		} catch (IOException e) {
			throw new BIConnectionException(BIConnectionException.REASON_INVALID_HTTPERROR, -1, 
					e.getMessage()!= null ? e.getMessage() : ""); //$NON-NLS-1$
		}
		
	}
	
	
	private static boolean performAuthentication(BigInsightsLocation loc, HttpClient httpclient, java.net.URL authCheckUrl, String userId, String password) throws HttpException, IOException, BIConnectionException {
		boolean isAuthenticated = false;		
		int port           = authCheckUrl.getPort();
		String protocol    = authCheckUrl.getProtocol();
		String host        = authCheckUrl.getHost();
		String contextRoot = getContextRoot(authCheckUrl);
				
		java.net.URL authUrl = new java.net.URL(protocol + "://" + host +':' + Integer.toString(port) + contextRoot + '/' + "j_security_check"); //$NON-NLS-1$ //$NON-NLS-2$
		
		PostMethod httpPostAuth = new PostMethod(authUrl.toString());
		
		// ensure that we do NOT follow re-directs!
		httpPostAuth.setFollowRedirects(false);
		
		try {						
			setSessionCookie(httpPostAuth, loc);
			NameValuePair userCredentials[] = new NameValuePair[]{new NameValuePair(), new NameValuePair()};
			userCredentials[0].setName("j_username"); //$NON-NLS-1$
			userCredentials[0].setValue(userId);
			userCredentials[1].setName("j_password"); //$NON-NLS-1$
			userCredentials[1].setValue(password);
			httpPostAuth.setRequestBody(userCredentials);
			
			httpclient.executeMethod(httpPostAuth);
			
			int statusCode  = httpPostAuth.getStatusCode();
			Header location = httpPostAuth.getResponseHeader("Location"); //$NON-NLS-1$
						
			Header setCookieHeader = httpPostAuth.getResponseHeader("Set-Cookie");
			if (setCookieHeader!=null) {
				String cookies = parseSessionHeader(httpPostAuth.getResponseHeader("Set-Cookie"));			
				if (cookies!=null) {					
					// append new cookies to existing ones
					loc.getHttpSession().setCookies(cookies);
				}
			}
			
			if(location == null){
				throw new BIConnectionException(BIConnectionException.REASON_INVALID_USERIDPW);
			}
			java.net.URL locValue = new java.net.URL(location.getValue());
			
			if (statusCode == 302 && authCheckUrl.getHost().equals(locValue.getHost()) && 
          ( locValue.getPath().equals("/data/controller/AuthenticationAction") ||
			      locValue.getPath().equals("/data/") )) {
				isAuthenticated = true;
			}			
		} finally {
			httpPostAuth.releaseConnection();
		}

		return isAuthenticated;
	}
	
	private static boolean performAuthenticationCheck(HttpClient httpclient, java.net.URL authCheckUrl, BigInsightsLocation loc) throws HttpException, IOException {
		boolean isAuthenticated = false;		

		GetMethod httpGetAuthCheck = new GetMethod(authCheckUrl.toString());
		try {
			
		  try {
			  setSessionCookie(httpGetAuthCheck, loc);
			  httpclient.executeMethod(httpGetAuthCheck);
		  }catch(SSLHandshakeException e){
			  retrySSLExecute(httpGetAuthCheck, loc);
		  }
		  
		  int statusCode                  = httpGetAuthCheck.getStatusCode();
		  Header contentType              = httpGetAuthCheck.getResponseHeader("Content-Type"); //$NON-NLS-1$
		  Header setCookieHeaderValue     = httpGetAuthCheck.getResponseHeader("Set-Cookie"); //$NON-NLS-1$
		  if (setCookieHeaderValue!=null) {			  
			  String setCookieValue = parseSessionHeader(setCookieHeaderValue);
			  if (setCookieValue!=null) {
				  loc.getHttpSession().setCookies(setCookieValue);
			  }
		  }
		  
		  if (statusCode == 200) {
			  if (contentType.getValue().startsWith("text/xml") &&		 //$NON-NLS-1$
				  httpGetAuthCheck.getPath().equals("/data/controller/AuthenticationAction")) { //$NON-NLS-1$
				  
				  // set roles when authenticated successfully
				  setRoles(httpGetAuthCheck, loc);
				  isAuthenticated = true;
				  loc.getHttpSession().setLastServerRequest(new Date());
			  }			  
		  }
		  
		} finally {
			httpGetAuthCheck.releaseConnection();
		}
		
		return isAuthenticated;
	}
	
	private static String parseSessionHeader(Header setCookieHeader) {	
		if (setCookieHeader==null)
			return null;
		
		StringBuffer sessionCookieBuffer = new StringBuffer();
		HeaderElement elements[] = setCookieHeader.getElements();
		
		// retrieve all cookies sent in header
		for (HeaderElement he : elements) {
			String name = he.getName();
			String value = he.getValue();
			if (name!=null && value!=null) {
				sessionCookieBuffer.append(name);
				sessionCookieBuffer.append("=");
				sessionCookieBuffer.append(value);
				sessionCookieBuffer.append("; ");
			}
		  }
				
		return sessionCookieBuffer.length()==0 ? null : sessionCookieBuffer.toString();
	  }
	
	private static String getContextRoot(java.net.URL url) {
		String path = url.getPath();
		String contextRoot = null;
		if (path != null) { 
			int ctxRootDelPos = path.indexOf('/', 1);
			if (ctxRootDelPos > -1) {
				contextRoot = path.substring(0, ctxRootDelPos);
			} else {
				contextRoot = path;
			}
		}
		return contextRoot;
	}
	
	public static void setSessionCookie(HttpMethod method, IBigInsightsLocation loc){		
		if(loc.getHttpSession().getCookies() != null){
			method.setRequestHeader("Cookie", loc.getHttpSession().getCookies()); //$NON-NLS-1$
		}
	}
	
	public static void setSessionCookie(HttpMethod method, String hostname, String userid){
		IBigInsightsLocation loc = 	LocationRegistry.getInstance().getLocationByHostName(hostname, userid);
		if (loc!=null)
			setSessionCookie(method, loc);
	}
	
	public static void prepareSelfSigningSSL(int port, String sslProtocol) {	
		Protocol.unregisterProtocol("https"); //$NON-NLS-1$
		Protocol simplehttps = new Protocol("https", (ProtocolSocketFactory)new EasySSLProtocolSocketFactory(sslProtocol), port);		 //$NON-NLS-1$
		Protocol.registerProtocol("https", simplehttps); //$NON-NLS-1$
		
	}
	
	public static int retrySSLExecute(HttpMethod method, IBigInsightsLocation loc) throws HttpException, IOException{
		int statuscode = 0;
		
		method.releaseConnection();
		HttpClient httpclient = new HttpClient();
		prepareSelfSigningSSL(loc.getPort(), loc.getHttpSession().getSSLprotocol()); 
		try{
			statuscode = httpclient.executeMethod(method);
		}catch(SSLHandshakeException e){
			method.releaseConnection();
			httpclient = new HttpClient();
			prepareSelfSigningSSL(loc.getPort(), loc.getHttpSession().getSSLprotocol()); 
			statuscode = httpclient.executeMethod(method);
		}
		return statuscode;
	}
	
	private static void setRoles(GetMethod method, BigInsightsLocation loc){
		
		Document doc = null;
		try {	
			InputStream response = method.getResponseBodyAsStream();		
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(response);
		} catch (ParserConfigurationException e) {
			 throw new RuntimeException(e);
		} catch (SAXException e) {
			 throw new RuntimeException(e);
		} catch (IOException e) {
			 throw new RuntimeException(e);
		}
			
		
		if(doc != null){
			NodeList roleNodes = doc.getElementsByTagName("role"); //$NON-NLS-1$
			if(roleNodes.getLength() > 0)
			for(int i=0; i < roleNodes.getLength() ;i++){
				Node roleNode = roleNodes.item(i);
				String role = roleNode.getTextContent();
				if(role != null && role.length()>0){
					loc.getRoles().add(role);
				}
			}
		}
	}
	
}
