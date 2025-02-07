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
package com.ibm.biginsights.textanalytics.workflow.plan.models;

import java.util.ArrayList;
import java.util.List;

import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.AQLNodeModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.LabelModel;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.GroupType;

public class AqlFolderNode extends NodesGroup
{


 
	public AqlFolderNode (String name, AqlGroup parent)
  {
    super (name, GroupType.AQL_FOLDER, parent);
    setAqlType(parent.getAqlType ());
    buildChildren ();
  }

  private void buildChildren ()
  {
    if ( parent == null ||
         parent.getParent () == null ||
         ((LabelNode) parent.getParent ()).toModel () == null )
      return;

    LabelModel labelModel = ((LabelNode)parent.getParent ()).toModel ();
    for (AQLNodeModel aqlNodeModel : labelModel.getAqlObjects (getAqlType())) {
      AqlNode aqlNode = new AqlNode (aqlNodeModel, this);
//      aqlNode.setAqlfilepath (labelModel.getAqlFilePath (getAqlGroupType()));
      addChild (aqlNode);
    }
  }

  public LabelModel toModel()
  {
    if ( parent != null &&
         parent.getParent () != null )
      return ((LabelNode) parent.getParent ()).toModel ();
    else
      return null;
  }

  /**
   * Add the child to parent both in the tree and in the underlying model.<br>
   * The method addChild(..) only add child tree object.
   * @param child
   */
  public void addChild2(TreeObject child) {
    if (child != null && child instanceof AqlNode) {

      // add tree child object
      AqlNode aqlNode = (AqlNode)child;
      super.addChild (child);

      // add model child object
      LabelModel labelModel = toModel ();
      if (labelModel != null)
        labelModel.addAqlNodeModel (aqlNode.toModel (), aqlGroupType);
    }
  }

  public boolean removeChild(TreeObject child) {
    if (super.removeChild (child))  // this only removes from the tree, not from model.
      return ((AqlGroup)getParent ()).removeChild (child);

    return false;
  }

  public Object[] getDisplayedChildren(){

    //-------- Get displayed children normally
    if (!ActionPlanView.isSimplifiedView ())
      return super.getDisplayedChildren ();

    //-------- get children for simplified view
    List<AqlNode> allDescendentsAqlNodes = new ArrayList<AqlNode>();

    // At first get all child aql nodes
    for (TreeObject to : getChildren ()){
      allDescendentsAqlNodes.add ((AqlNode)to);
    }

    // then combine with aql children of peer labels (labels of the same aql group type)
    AqlGroup aqlGroupParent = (AqlGroup)getParent();
    LabelsFolderNode peerLabelsFolder = aqlGroupParent.getLabelsFolder ();
    List<TreeObject> peerLabels = peerLabelsFolder.getChildren ();

    for (TreeObject lbl : peerLabels) {
      allDescendentsAqlNodes.addAll (((LabelNode)lbl).getAllAqlNodes ());
    }

    return allDescendentsAqlNodes.toArray ();
  }

}
