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
package com.ibm.biginsights.textanalytics.aql.editor.outline;

import java.util.ArrayList;
import java.util.List;

/**
 *  
 *  Babbar
 * 
 */
public class AQLNode {


	
	private int beginOffset;
	private int endOffset;
	private String text;
	private String image;
	private List<AQLNode> children = new ArrayList<AQLNode>();
	private AQLNode parent;

	public AQLNode(int start, int end, String text, String image){
		this.beginOffset = start;
		this.endOffset = end;
		this.text = text;
		this.image = image;
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
	
	public void remove(AQLNode node)
	{
		this.children.remove(node);
	}
	

	public void addChild(AQLNode node){
		this.children.add(node);
	}

	public AQLNode[] getChildren(){
		return this.children.toArray(new AQLNode[this.children.size()]);
	}

	public void setParent(AQLNode parent){
		this.parent = parent;
	}

	public AQLNode getParent(){
		return this.parent;
	}

	public String toString(){
		return this.text;
	}

	public String getImage(){
		return this.image;
	}
}
