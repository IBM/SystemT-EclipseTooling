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
package com.ibm.biginsights.textanalytics.indexer.exceptions;

/**
 * Thrown when an element's type could not be determined
 * 
 *  Krishnamurthy
 */
public class ElementTypeDeterminationException extends Exception
{


 
	private static final long serialVersionUID = -4318272358889908241L;

  public ElementTypeDeterminationException ()
  {
    super ();
  }

  public ElementTypeDeterminationException (String message, Throwable cause)
  {
    super (message, cause);
  }

  public ElementTypeDeterminationException (String message)
  {
    super (message);
  }

  public ElementTypeDeterminationException (Throwable cause)
  {
    super (cause);
  }

}
