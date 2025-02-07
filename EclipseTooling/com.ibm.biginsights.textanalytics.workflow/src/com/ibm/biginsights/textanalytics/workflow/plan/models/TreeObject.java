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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;

public class TreeObject implements IAdaptable {



	protected String label;
	protected TreeParent parent;
	protected Image icon;

	public TreeObject() {
		label = "";
	}

	public TreeObject(String label, Image icon) {
		this.label = label;
		this.icon = icon;
	}

	public Image getIconImage() {
		return icon;
	}

	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label){
		this.label = label;
	}

	public void setParent(TreeParent parent) {
		this.parent = parent;
	}

	public TreeParent getParent() {
		return parent;
	}

	public String toString() {
		return getLabel();
	}

	public void doubleClick() {

	}

	public void doclick() {

	}
	
	public TreeObject deepCopy(){
		TreeObject obj = null;
		
		if(this instanceof LabelNode){
			obj = new LabelNode(((LabelNode)this).toModel());
		}else if(this instanceof ExampleNode){
			obj = new ExampleNode(((ExampleNode)this).toModel());
		}else if(this instanceof AqlNode){
			obj = new AqlNode(((AqlNode)this).toModel(), ((AqlNode)this).getAqlGroup ());
		}else{
			obj = new TreeObject(label, icon);
		}
		
		return obj;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class key) {
		return null;
	}

	public boolean isDisplayed(){
	  return true;
	}

	public LabelNode getParentLabelNode()
	{
	  if (this instanceof LabelNode &&
	      ((LabelNode)this).isRootLabel ())
	    return null;
	    
	  if (getParent() instanceof LabelNode)
	    return (LabelNode)getParent();
	  else
	    return getParent ().getParentLabelNode ();
	}

	public LabelNode getRootLabel ()
	{
    if (this instanceof LabelNode &&
        ((LabelNode)this).isRootLabel ())
      return (LabelNode)this;

    LabelNode parentLabel = getParentLabelNode ();

    if (parentLabel.isRootLabel ())
      return parentLabel;
    else
      return parentLabel.getRootLabel ();
	}

}
