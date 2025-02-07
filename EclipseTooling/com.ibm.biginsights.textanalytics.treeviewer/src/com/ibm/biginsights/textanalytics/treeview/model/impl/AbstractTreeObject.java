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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;

import com.ibm.biginsights.textanalytics.treeview.model.IScalarList;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.TreeObjectType;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public abstract class AbstractTreeObject implements ITreeObject, IAdaptable {



  private final TreeObjectType type;
  protected TreeParent parent;
  
  /**
   * A unique id to represent the TreeObject.
   */
  protected String id;

  public AbstractTreeObject(TreeObjectType type) {
    super();
    this.type = type;
  }

  @Override
  public TreeObjectType getType() {
    return this.type;
  }

  @Override
  public int getStart() {
    return Constants.SPAN_UNDEFINED_OFFSET;
  }

  @Override
  public int getEnd() {
    return Constants.SPAN_UNDEFINED_OFFSET;
  }

  @Override
  public String getText() {
    return null;
  }
  
  @Override
  public String getString() {
    return null;
  }

  @Override
  public float getFloat() {
    return -1.0f;
  }

  @Override
  public int getInt() {
    return -1;
  }

  @Override
  public IScalarList getList() {
    return null;
  }

  @Override
  public boolean getBoolean() {
    return false;
  }
  
  @Override
  public IFile getFile() {
    return null;
  }
	
	public void setParent(TreeParent parent) {
		this.parent = parent;
		resetId();
	}
	public TreeParent getParent() {
		return parent;
	}

	public Object getAdapter(Class key) {
		return null;
	}
	
	public String getId(){
		return id;
	}
	
	public String resetId(){
		//do nothing. Dummy implementation.
		//Expecting TreeParent and SpanTreeObject classes to implement this
		return this.id;
	}

}
