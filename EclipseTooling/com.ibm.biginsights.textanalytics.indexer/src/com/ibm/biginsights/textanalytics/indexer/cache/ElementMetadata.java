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
package com.ibm.biginsights.textanalytics.indexer.cache;

/**
 * Metadata class for entries within element cache. At the moment it contains only one property - elementActiveState.
 * <p>
 * elementActiveState indicates if the element with which the metadata object is associated, actually exists.
 * </p>
 * 
 * 
 */
public class ElementMetadata
{
	@SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" +
      "US Government Users Restricted Rights - Use, duplication  disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";

	/**
   * Indicates whether the element is active and available.
   */
  private boolean elementActiveState;

  /**
   * Returns ElementMetadata object with default values (elementActiveState = true)
   */
  public ElementMetadata ()
  {
    elementActiveState = true;
  }

  /**
   * Returns ElementMetadata object with user specified values.
   * 
   * @param state value for elementActiveState
   */
  public ElementMetadata (boolean state)
  {
    elementActiveState = state;
  }

  /**
   * Get the value of elementActiveState metadata property.
   * 
   * @return
   */
  public boolean getElementActiveState ()
  {
    return elementActiveState;
  }

  /**
   * Set the value of elementActiveState metadata property.
   * 
   * @param state
   */
  public void setElementActiveState (boolean state)
  {
    elementActiveState = state;
  }
}
