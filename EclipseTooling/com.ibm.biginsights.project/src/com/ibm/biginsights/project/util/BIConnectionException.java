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

import com.ibm.biginsights.project.Messages;



public class BIConnectionException extends Exception{
	
	private static final long serialVersionUID = 712985488015806798L;
	
	public int reason = -1;
	public int httpReturnCode = -1;
	public String httpReturnMessage = ""; //$NON-NLS-1$
	
	public static int REASON_INVALID_USERIDPW = 1;
	public static int REASON_INVALID_ROLE = 2;
	public static int REASON_INVALID_HTTPERROR = 3;
	
	
	
	public BIConnectionException(int reason, int httpReturnCode,String httpReturnMessage) {
		super();
		this.reason = reason;
		this.httpReturnCode = httpReturnCode;
		this.httpReturnMessage = httpReturnMessage;
	}

	public BIConnectionException(int reason) {
		super();
		this.reason = reason;
	}
	
	public BIConnectionException() {
		super();
	}

	public int getReason() {
		return reason;
	}
	
	public void setReason(int reason) {
		this.reason = reason;
	}
	
	public int getHttpReturnCode() {
		return httpReturnCode;
	}
	
	public void setHttpReturnCode(int httpReturnCode) {
		this.httpReturnCode = httpReturnCode;
	}
	
	public String getHttpReturnMessage() {
		return httpReturnMessage;
	}
	
	public void setHttpReturnMessage(String httpReturnMessage) {
		this.httpReturnMessage = httpReturnMessage;
	}
	
	public String getMessage() {
		String result = null;
		if (this.reason == BIConnectionException.REASON_INVALID_USERIDPW){
			result = Messages.LOCATIONSERVERAUTHFAILED;
		} else if(this.reason == BIConnectionException.REASON_INVALID_ROLE){
			result = Messages.LOCATIONWIZARDBASICDATAPAGE_ROLE_ERROR;
		} else if(this.reason == BIConnectionException.REASON_INVALID_HTTPERROR){
			result = Messages.LOCATIONWIZARDBASICDATAPAGE_CONN_ERROR_DESC;
			if(this.httpReturnCode > 0){
				result += " "+ this.httpReturnCode;  //$NON-NLS-1$
			}
			result += " " + this.httpReturnMessage; //$NON-NLS-1$
		}
		return result;
	}
	
}
