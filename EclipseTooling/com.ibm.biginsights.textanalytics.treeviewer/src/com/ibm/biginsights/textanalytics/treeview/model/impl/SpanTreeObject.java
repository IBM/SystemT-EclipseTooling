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

import com.ibm.biginsights.textanalytics.treeview.model.TreeObjectType;

public class SpanTreeObject extends AbstractTreeObject {


  
  private final int start;
  
  private final int end;
  
  private final String text;
  
  private final IFile file;
  
  public SpanTreeObject(int start, int end, String text, IFile file) {
    super(TreeObjectType.SPAN);
    this.start = start;
    this.end = end;
    this.text = text;
    this.file = file;
  }
  
  @Override
  public int getStart() {
    return this.start;
  }
  
  @Override
  public int getEnd() {
    return this.end;
  }
  
  @Override
  public String getText() {
    return this.text;
  }
  
  @Override
  public String toString() {
    return getText() + " [" + getStart() + "-" + getEnd() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  @Override
  public IFile getFile() {
    return this.file;
  }
  
  @Override
  public String resetId(){
	this.id = parent.getId()+this.start+this.end;
	return this.id;
  }
  
}
