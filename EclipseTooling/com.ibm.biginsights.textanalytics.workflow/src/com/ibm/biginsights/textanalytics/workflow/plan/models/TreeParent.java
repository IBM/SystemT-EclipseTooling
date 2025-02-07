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

import com.ibm.biginsights.textanalytics.workflow.util.ResourceManager;



/**
 * this class represent a general node in the Action Plan three that can
 * have children
 * 
 * 
 * 
 */
public class TreeParent extends TreeObject {


  
	public static int LOCATION_NONE = -1;
  public static int LOCATION_BEFORE = 0;
  public static int LOCATION_AFTER = 1;

  protected ArrayList<TreeObject> children;

  private static boolean displayAll = false;

  public TreeParent(String name) {
		super(name, ResourceManager.getMissingImage());
		children = new ArrayList<TreeObject>();
	}

  public TreeParent(String name, TreeParent parent) {
    this(name);
    this.parent = parent;
  }

	/**
	 * This method only add a child object in the tree.<br>
	 * Caller is responsible for adding object model to parent model.
	 * @param child
	 */
	public void addChild(TreeObject child) {
	  if (child != null && !getChildren().contains (child)) {
	    getChildren().add(child);
	    child.setParent(this);
	  }
	}

  public ArrayList<TreeObject> getChildren() {
    return children;
  }

  public TreeObject[] getChildArray() {
    return getChildren().toArray(new TreeObject[getChildren().size()]);
  }

	public boolean removeChild(TreeObject child) {
	  boolean removed = false;

	  if (child != null) {
	    removed = getChildren().remove(child);
	    if (removed)
	      child.setParent(null);
    }

	  return removed;
	}

  /**
   * Move a child from its position to before or after a target child.<br>
   * relativePosToTarget can only be LOCATION_BEFORE or LOCATION_AFTER
   * @param child The moved child object
   * @param targetChild The target child object
   * @param relativePosToTarget The position relative to the target 
   */
  public void moveChild(TreeObject child, TreeObject targetChild, int relativePosToTarget) {
    if ( getChildren().contains (child) &&
         getChildren().contains (targetChild) &&
         child != targetChild &&
         (relativePosToTarget == LOCATION_BEFORE || relativePosToTarget == LOCATION_AFTER)) {

      getChildren().remove (child);

      int pos = getChildren().indexOf (targetChild) + relativePosToTarget;
      getChildren().add (pos, child);
    }
  }

  public TreeObject getChildrenByLabel(String label) {

    for (TreeObject child : getChildren()) {
      if (child.getLabel ().equals (label))
        return child;
    }
      
    return null;
  }

	public boolean hasChildren() {
		return getChildren().size() > 0;
	}
	
	public boolean isSubElement(TreeObject obj){
		for(TreeObject to : getChildren()) {
			if(obj.equals(to))
				return true;
			if(to instanceof TreeParent) {
				if(((TreeParent)to).isSubElement(obj))
					return true;
			}
		}
		return false;
	}

  public boolean isDisplayed() {
    return isDisplayAll () || (getDisplayedChildren().length > 0);
  }

  public Object[] getDisplayedChildren() {
    List<TreeObject> displayedChildren = new ArrayList<TreeObject>();

    for (TreeObject to : getChildren ()){
      if (isDisplayAll () || to.isDisplayed ())
        displayedChildren.add (to);
    }

    return displayedChildren.toArray ();
  }

  public static boolean isDisplayAll ()
  {
    return displayAll;
  }

  public static void setDisplayAll (boolean dsplAll)
  {
    displayAll = dsplAll;
  }

  public static void toggleDisplayAll ()
  {
    displayAll = !displayAll;
  }
}
