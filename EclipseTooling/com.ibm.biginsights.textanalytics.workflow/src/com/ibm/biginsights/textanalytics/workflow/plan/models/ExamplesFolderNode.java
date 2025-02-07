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

import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.LabelModel;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.GroupType;

public class ExamplesFolderNode extends NodesGroup
{



  public ExamplesFolderNode (String name, LabelNode parent)
  {
    super (name, GroupType.EXAMPLES, parent);
    buildChildren ();
  }

  private void buildChildren ()
  {
    LabelModel labelModel = toModel();
    if (labelModel == null)
      return;

    for (ExampleModel exModel : labelModel.getExamples ()) {
      addChild(new ExampleNode(exModel));
    }
  }

  public LabelModel toModel()
  {
    if (parent != null)
      return ((LabelNode)parent).toModel ();
    else
      return null;
  }

  public boolean removeChild(TreeObject child)
  {
    if (!(child instanceof ExampleNode))
      return false;

    ExampleNode example = (ExampleNode)child;

    if (super.removeChild (child)) {  // this only removes from the tree, not from model.
      LabelModel labelModel = toModel();
      if (labelModel != null)
        return labelModel.removeExample (example.toModel ());
    }

    return false;
  }

  /**
   * Add the child to parent both in the tree and in the underlying model.<br>
   * The method addChild(..) only add child tree object.
   * @param child
   */
  public void addChild2(ExampleNode child) {
    if (child != null) {

      // add tree child object
      super.addChild (child);

      // add model child object
      LabelModel labelModel = toModel ();
      if (labelModel != null)
        labelModel.addExample (child.toModel ());
    }
  }

}
