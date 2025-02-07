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
package com.ibm.biginsights.textanalytics.indexer.model;

/**
 * Class that represents reference to an AQL element
 * 
 *  Krishnamurthy
 */
public class ElementReference extends Reference
{



  /**
   * Primary key for the ElementReference
   */
  Integer elementRefId;

  /**
   * Referenced element's ID
   */
  Integer elementId;

  public ElementReference (Integer elementRefId, Integer elementId, ElementLocation location)
  {
    super ();
    this.elementRefId = elementRefId;
    this.elementId = elementId;
    super.location = location;
  }

  public Integer getElementRefId ()
  {
    return elementRefId;
  }

  public Integer getElementId ()
  {
    return elementId;
  }

}
