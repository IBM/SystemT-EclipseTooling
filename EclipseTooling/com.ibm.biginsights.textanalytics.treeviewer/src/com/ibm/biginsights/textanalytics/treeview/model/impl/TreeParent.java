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
package com.ibm.biginsights.textanalytics.treeview.model.impl;

import java.util.HashMap;

import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.TreeObjectType;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;


public class TreeParent extends AbstractTreeObject {


	
	private ITreeObject[] children;
	private String name;
	
	protected HashMap<String, AbstractTreeObject> treeObjects = new HashMap<String, AbstractTreeObject>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public TreeParent(TreeObjectType cellType, String name, ITreeObject[] children) {
		super(cellType);
		this.children = children;
		this.name=name;
		
		if(StringUtils.isEmpty(id)){
			id = "1"; //$NON-NLS-1$
		}
		for (int i =0; i<children.length; i++)
		{	
			AbstractTreeObject child = (AbstractTreeObject)children[i];
			child.setParent(this);
			if((child instanceof SpanTreeObject) || (child instanceof TreeParent)){
				treeObjects.put(child.getId(), child);
			}
		}
	}
	
	public ITreeObject[] getChildren() {
		return children;
	}
	
	public boolean hasChildren() {
		return children.length>0;
	}
	
	public boolean isEndParentNode()
	{
		for (int i=0;i<children.length;i++)
		{
			if (children[i] instanceof TreeParent)
			{
				return false;
			}
		}
		return true;
	}
	
	public ITreeObject findById(String id){
		ITreeObject item = treeObjects.get(id);
		if(item == null){
			for (ITreeObject child : children) {
				if(child instanceof TreeParent){
					item = ((TreeParent)child).findById(id);
					if(item != null){
						return item;
					}
				}
			}
		}
		
		return item;
	}
	
	@Override
	public String resetId() {
		this.id = parent.getId() + name;
		if(hasChildren()){
			for (ITreeObject item : children) {
				AbstractTreeObject child = (AbstractTreeObject) item;
				if( (child instanceof SpanTreeObject) || (child instanceof TreeParent) ){
					String oldId = child.getId();
					String newId = child.resetId();
					treeObjects.remove(oldId);
					treeObjects.put(newId, child);
				}
			}
		}
		
		return this.id;
	}
	

}
