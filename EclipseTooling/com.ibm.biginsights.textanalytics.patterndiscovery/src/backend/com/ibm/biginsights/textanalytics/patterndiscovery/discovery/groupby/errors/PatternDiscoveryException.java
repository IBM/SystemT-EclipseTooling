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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;

public class PatternDiscoveryException extends Exception{



	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String error;
	private Throwable exception;
	
	//----------------------------------------------
	// Default constructor - initializes instance variable to unknown
	  public PatternDiscoveryException()
	  {
	    super();             // call superclass constructor
	    this.error = "Pattern Discovery has encountered an exception.";
	  }
	  
	//-----------------------------------------------
	// Constructor receives some kind of message that is saved in an instance variable.
	  public PatternDiscoveryException(String err)
	  {
	    super(err);     // call super class constructor
	    this.error = err;  // save message
	  }
	  
	  public PatternDiscoveryException(Throwable e, String err){
		  super(e);
		  this.error = err;
		  this.exception = e;
		  if(Constants.DEBUG)
				 System.err.println(err+ e.getMessage());
	  }
	  
	//------------------------------------------------  
	// public method, callable by exception catcher. It returns the error message.
	  public String getError()
	  {
	    return error;
	  }

	  public Throwable getException(){
		  return exception;
	  }
}
