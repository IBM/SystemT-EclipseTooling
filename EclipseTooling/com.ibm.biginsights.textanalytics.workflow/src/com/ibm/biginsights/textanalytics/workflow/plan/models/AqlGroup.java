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

import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.LabelModel;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.GroupType;

/**
 * 
 */
public class AqlGroup extends NodesGroup
{


 
	private LabelModel labelModel;

  protected LabelsFolderNode subTags;
  protected AqlFolderNode    aqls;

  public AqlGroup (String name, GroupType groupType, LabelNode parent)  // TODO we don't need 'name' param, we can derive
  {                                                                     // it from the LabelMode of LabelNode parent.
    super (name, groupType, parent);
    setAqlType (groupType);
    labelModel = parent.toModel ();
    buildChildren();
  }

  private void buildChildren ()
  {
    aqls = new AqlFolderNode ("AQL Statements", this);
    subTags = new LabelsFolderNode (this);
  }

  /**
   * @return
   */
  public AqlGroupType getAqlType ()
  {
    return aqlGroupType;
  }

  /**
   * @param type
   */
  public void setAqlType (AqlGroupType type)
  {
    this.aqlGroupType = type;
  }

  /**
   * @param type
   */
  public void setAqlType (GroupType type)
  {
    switch (type) {
    case BASIC_FEATURES:
      this.aqlGroupType = AqlGroupType.BASIC;
      break;
    case CONCEPTS:
      this.aqlGroupType = AqlGroupType.CONCEPT;
      break;
    case REFINEMENT:
      this.aqlGroupType = AqlGroupType.REFINEMENT;
      break;
    case FINALS:
      this.aqlGroupType = AqlGroupType.FINALS;
      break;
    }
  }

//  public void setAqlfile (String aqlfile)
//  {
//    this.aqlfile = aqlfile;
//  }
//
//  public void updateAqlFile (String newpath)
//  {
//    this.aqlfile = newpath;
//    if (parent.getParent () instanceof LabelNode) {
//      switch (groupType) {
//        case BASIC_FEATURES:
//          ((LabelNode) parent.getParent ()).setBasicsfile (newpath);
//        break;
//
//        case CONCEPTS:
//          ((LabelNode) parent.getParent ()).setConceptsfile (newpath);
//        break;
//
//        case REFINEMENT:
//          ((LabelNode) parent.getParent ()).setRefinementsfile (newpath);
//        break;
//      }
//    }
//  }
//
//  public String getAqlfile ()
//  {
//    return aqlfile;
//  }
//
//  public List<AQLElement> getViewElements ()
//  {
//    return AqlHelper.aqlLibrary.getViews (aqlfile);
//  }
//
//  public void loadViewElements ()
//  {
//    List<AQLElement> elements = getViewElements ();
//    if (elements != null) for (AQLElement el : elements) {
//      AqlNode node = new AqlNode (el.getName ());
//      // String content = el.toString();
//      this.addChild (node);
//      // TODO implement utility that give you the type of this view
//    }
//  }

  public LabelsFolderNode getLabelsFolder ()
  {
    return subTags;
  }

  public ArrayList<AqlNode> getChildAqlNodes ()
  {
    ArrayList<AqlNode> aqlNodeChildren = new ArrayList<AqlNode> ();

    for (TreeObject to : aqls.getChildren ()) {
      aqlNodeChildren.add ((AqlNode)to);
    }

    return aqlNodeChildren;
  }

  public AqlFolderNode getAqlStatementsFolder ()
  {
    return aqls;
  }

  public ArrayList<LabelNode> getChildLabelNodes ()
  {
    ArrayList<LabelNode> labelNodeChildren = new ArrayList<LabelNode> ();

    for (TreeObject to : subTags.getChildren ()) {
      labelNodeChildren.add ((LabelNode)to);
    }

    return labelNodeChildren;
  }

  public LabelModel toModel ()
  {
    return labelModel;
  }

  public void setLabelModel (LabelModel labelModel)
  {
    this.labelModel = labelModel;
  }

  public boolean removeChild(TreeObject child) {
    if (super.removeChild (child)) {  // super only removes from the tree, not from model.
      if (child instanceof AqlNode)
        return labelModel.removeAqlNodeModel (((AqlNode)child).toModel ());
      else if (child instanceof LabelNode)
        return labelModel.removeSubLabel (((LabelNode)child).toModel ());
      else if (child instanceof ExampleNode)
        return labelModel.removeExample (((ExampleNode)child).toModel ());
    }

    return false;
  }

  /**
   * Add the child to parent both in the tree and in the underlying model.<br>
   * The method addChild(..) only add child tree object.
   * @param child
   */
  public void addChild2(TreeObject child) {
    if (child == null)
      return;

    if (child instanceof AqlNode) {
      ((AqlNode)child).setAqlGroup (getAqlType());
      aqls.addChild (child);
      labelModel.addAqlNodeModel (((AqlNode)child).getAQLNodeModel (), getAqlType());
    }
    else if (child instanceof LabelNode) {
      subTags.addChild (child);
      labelModel.addSubLabel (((LabelNode)child).toModel (), getAqlType());
    }
  }

  public Object[] getDisplayedChildren(){

    //-------- Get displayed children normally
    if (!ActionPlanView.isSimplifiedView ())
      return super.getDisplayedChildren ();

    //-------- get children for simplified view
    return getAqlStatementsFolder ().getDisplayedChildren ();
  }
}
