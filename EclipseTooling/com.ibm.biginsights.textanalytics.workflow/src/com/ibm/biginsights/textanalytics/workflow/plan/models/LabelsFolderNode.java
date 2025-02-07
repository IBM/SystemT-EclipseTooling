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

import com.ibm.biginsights.textanalytics.workflow.plan.serialize.LabelModel;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.GroupType;

public class LabelsFolderNode extends NodesGroup
{



  private LabelsFolderNode (String name, GroupType groupType, TreeParent parent)
  {
    super (name, groupType, parent);
    setAqlType(null);
    buildChildren ();
  }

  public LabelsFolderNode (String name, LabelNode parent) // direct sub-labels
  {
    super (name, GroupType.TAG_FOLDER, parent);
    setAqlType(null);
    buildChildren ();
  }

  public LabelsFolderNode (AqlGroup aqlGroup) // indirect sub-labels
  {
    super ("Labels", GroupType.TAG_FOLDER, aqlGroup);
    setAqlType(aqlGroup.getAqlType ());
    buildChildren ();
  }

  private void buildChildren ()
  {
    TreeParent parent = getParent ();
    LabelModel parentLabelModel = null;

    if (parent instanceof LabelNode)
      parentLabelModel = ((LabelNode)parent).toModel ();
    else
      parentLabelModel = ((AqlGroup)parent).toModel ();

    for (LabelModel lm : parentLabelModel.getSubLabels (getAqlType ())) {
      addChild2 (new LabelNode(lm));
    }
  }

  public LabelModel toModel()
  {
    if (parent != null) {
      if (parent instanceof AqlFolderNode)
        return ((AqlFolderNode) parent).toModel ();
      else if (parent instanceof AqlGroup)
        return ((AqlGroup) parent).toModel ();
      else if (parent instanceof LabelNode)
        return ((LabelNode) parent).toModel ();
    }

    return null;
  }

  /**
   * Add the child to parent both in the tree and in the underlying model.<br>
   * The method addChild(..) only add child tree object.
   * @param child
   */
  public void addChild2(TreeObject child) {
    if (child != null && child instanceof LabelNode) {

      // add tree child object
      LabelNode subLabelNode = (LabelNode)child;
      super.addChild (child);

      // add model child object
      LabelModel labelModel = toModel ();
      if (labelModel != null)
        labelModel.addSubLabel (subLabelNode.toModel (), aqlGroupType);
    }
  }

}
