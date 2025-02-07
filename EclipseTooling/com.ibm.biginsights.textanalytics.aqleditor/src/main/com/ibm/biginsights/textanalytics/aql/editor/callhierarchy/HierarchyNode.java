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
package com.ibm.biginsights.textanalytics.aql.editor.callhierarchy;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class constitue each node in
 * Dependency Hierarchy and Reference Hierarchy views'
 * trees.
 * 
 *
 */
public class HierarchyNode
{


 
	private int beginOffset;
  private int endOffset;
  private String text;
  private String image;
  private boolean hasChildren;
  private boolean nodeDependsOnChildrenNodes; //true when node is part of AQL Dependency Hierarchy, false when part of AQL Reference Hierarchy
  private String projectName;
  private String moduleName;
  private String filePath;
  private List<HierarchyNode> children = new ArrayList<HierarchyNode>();
  HierarchyNode parent;
  HierarchyRootNode root;

  public HierarchyNode (int start, int end, String text, String image, boolean dependentOnChildren, 
    String projectName, String moduleName, String filePath, HierarchyRootNode root)
  {    
    this.beginOffset = start;
    this.endOffset = end;
    this.text = text;
    this.image = image;
    this.hasChildren = true;
    this.nodeDependsOnChildrenNodes = dependentOnChildren;
    this.projectName = projectName;
    this.moduleName = moduleName;
    this.filePath = filePath;
    this.root = root;
  }
  
  public int getStart(){
    return this.beginOffset;
  }
  
  public int getEnd(){
    return this.endOffset;
  }
  
  public String getText(){
    return this.text;
  }
  
  public void remove(HierarchyNode node)
  {
    this.children.remove(node);
  }
  

  public void addChild(HierarchyNode node){
    this.children.add(node);
  }

  public boolean hasChildren() {
    return this.hasChildren;
  }

  public void setHasChildren(boolean value) {
    this.hasChildren = value;
  }

  public HierarchyNode[] getChildren(){
    if (this.hasChildren && this.children.size ()==0) {
      if (this.nodeDependsOnChildrenNodes) {
        root.populateTreeWithRequiredElementsForNode (this);
      } else {
        root.populateTreeWithDependentElementsForNode (this);
      }
      if (this.children.size() == 0) {
        setHasChildren(false);
      }
    }
    
    return this.children.toArray (new HierarchyNode[0]);
  }
  
  public void setParent(HierarchyNode parent){
    this.parent = parent;
  }

  public HierarchyNode getParent(){
    return this.parent;
  }

  public String toString(){
    return this.text;
  }

  public String getImage(){
    return this.image;
  }
  
  public String getProjectName() {
    return this.projectName;
  }
  public String getModuleName() {
    return moduleName;
  }
  
  public String getFilePath() {
    return filePath;
  }

}
