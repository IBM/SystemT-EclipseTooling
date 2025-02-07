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
package com.ibm.biginsights.project.locations;

import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.ibm.biginsights.project.util.Authenticator;
import com.ibm.biginsights.project.util.BIConnectionException;

public class BigInsightsLocationSession {
  
  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+            //$NON-NLS-1$
      "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

  private Boolean isAuthenticationEnabled = null;
	private StringBuffer cookies = new StringBuffer();
	private Date lastServerRequest = null;
	private String sslprotocol = "TLS"; //$NON-NLS-1$
	
	public boolean isAuthenticationNeeded(IBigInsightsLocation loc) {
	  return hasSessionExpired();

	  // We no longer allow non-secured install, so authentication is now always needed.

//		if(isAuthenticationEnabled == null) {
//			try {
//				//try unauthenticated rest call 
//				HttpClient httpClient = new HttpClient();
//				String uri = loc.generateAbsoluteURL( BIConstants.URL_USERINFO);
//				GetMethod getMethod = new GetMethod(uri);
//				//DO NOT SET SESSION COOKIE FOR THIS CALL
//
//				int statusCode = 0;
//				
//				try{					
//					statusCode = httpClient.executeMethod(getMethod);
//				}catch(SSLHandshakeException e){
//					statusCode = Authenticator.retrySSLExecute(getMethod, loc);
//				}
//						
//				if(statusCode==HttpStatus.SC_OK){
//					//check the content type
//					Header contentType = getMethod.getResponseHeader("Content-Type"); //$NON-NLS-1$
//					
//					if(contentType != null && contentType.getValue().startsWith("text/xml") /** || contentType == null  **/){ //data returned //$NON-NLS-1$
//						//no authentication needed for this server
//						isAuthenticationEnabled = Boolean.FALSE;
//					}else{
//						//otherwise html is returned with a form to logon
//						isAuthenticationEnabled = Boolean.TRUE;
//					}
//					
//					return isAuthenticationEnabled;
//				}else{
//					throw new BIConnectionException(BIConnectionException.REASON_INVALID_HTTPERROR, statusCode, ""); //$NON-NLS-1$
//				}
//			} catch (HttpException e) {
//				throw new BIConnectionException(BIConnectionException.REASON_INVALID_HTTPERROR, e.getReasonCode(), 
//						e.getMessage()!= null ? e.getMessage() : ""); //$NON-NLS-1$
//			} catch (IOException e) {
//				throw new BIConnectionException(BIConnectionException.REASON_INVALID_HTTPERROR, -1, 
//						e.getMessage()!= null ? e.getMessage() : ""); //$NON-NLS-1$
//			}
//		}
//		
//		if(isAuthenticationEnabled.booleanValue() && hasSessionExpired()){
//			 return true;
//		}
//		loc.getHttpSession().setLastServerRequest(new Date());
//		return false;
	}


	public String getCookies() {
		// return null if no cookies are set
		return (cookies.length()==0) ? null : cookies.toString();
	}
	
	public void setCookies(String cookies) {
		if (cookies==null || cookies.isEmpty()) {
			// if empty cookies are sent, empty out string buffer
			this.cookies.setLength(0);
		}
		else
			this.cookies.append(cookies);
	}
	
	public Date getLastServerRequest() {
		return lastServerRequest;
	}

	public void setLastServerRequest(Date lastServerRequest) {
		this.lastServerRequest = lastServerRequest;
	}
	
	public boolean hasSessionExpired(){
		Date now = new Date();
		if(lastServerRequest != null && 
		   (now.getTime() - lastServerRequest.getTime()) < 600000){ //600,000 milliseconds  = 10 minutes
			return false;
		}
		return true;
	}
	
	public void authenticate(HttpClient client, BigInsightsLocation loc ) throws BIConnectionException{
		
		if(loc.getUserName() == null || loc.getUserName().length()==0 || 
		   loc.getPassword() == null || loc.getPassword().length()==0 ){
			
			Thread uithread = Display.getDefault().getThread();
			if(Thread.currentThread().getId() == uithread.getId()){
				//set userid and pw
				loc.showUserIdPasswordDialog();
			}else{				
				Display.getDefault().syncExec(new Show(loc));
			}
			if(loc.getPassword()==null || loc.getPassword().length()==0 ||
			   loc.getUserName()==null || loc.getUserName().length()==0){
				  throw new BIConnectionException(BIConnectionException.REASON_INVALID_USERIDPW);
			}
		}
		
		//authenticate to sever
		Authenticator.authenticate(client, loc);
		
	}
	
	public class Show implements Runnable{
		private BigInsightsLocation _loc;
		public Show (BigInsightsLocation __loc){
			_loc = __loc;
		}
		@Override
		public void run() {
			int result = _loc.showUserIdPasswordDialog();
			if(result!=Window.OK){
				return;
			}
			
		}
		
	}

	public String getSSLprotocol() {
		return sslprotocol;
	}

	public void setSSLprotocol(String sslprotocol) {
		this.sslprotocol = sslprotocol;
	}
	
	public Boolean getAuthEnabled(){
		return isAuthenticationEnabled;
	}
}
