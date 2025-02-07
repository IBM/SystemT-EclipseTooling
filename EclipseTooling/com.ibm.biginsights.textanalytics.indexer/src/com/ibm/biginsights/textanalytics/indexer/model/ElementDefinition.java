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

import java.util.HashSet;
import java.util.Set;

import com.ibm.biginsights.textanalytics.indexer.types.ElementType;

public class ElementDefinition
{



  private Integer projectId;
  private Integer moduleId;
  private Integer elementId;
  private ElementType type;
  private ElementLocation location;
  private Set<Integer> referenceList; // Provides the Dependency hieraarchy

  public ElementDefinition (Integer projectId, Integer moduleId, Integer elementId, ElementType type,
    ElementLocation location)
  {
    super ();
    this.projectId = projectId;
    this.moduleId = moduleId;
    this.elementId = elementId;
    this.type = type;
    this.location = location;
    this.referenceList = new HashSet<Integer> ();
  }

  public void removeElementReference (Integer refId)
  {
    referenceList.remove (refId);
  }

  /**
   * Captures the fact that the current element is dependent on another element, and the reference to the other element
   * (i.e the referenceId, and NOT the elementId) is captured in the referenceList
   * 
   * @param refId
   */
  public void addReference (Integer refId)
  {
    referenceList.add (refId);
  }

  public void setLocation (ElementLocation location)
  {
    this.location = location;
  }

  public Integer getProjectId ()
  {
    return projectId;
  }

  public Integer getModuleId ()
  {
    return moduleId;
  }

  public Integer getElementId ()
  {
    return elementId;
  }

  public ElementType getType ()
  {
    return type;
  }

  /**
   * Returns fully qualified name. PMTE (Project.Module.Type.ElementName)
   * 
   * @return
   */
  public String getName ()
  {
    return null;
  }

  public ElementLocation getLocation ()
  {
    return location;
  }

  public Set<Integer> getReferenceList ()
  {
    return referenceList;
  }

}
