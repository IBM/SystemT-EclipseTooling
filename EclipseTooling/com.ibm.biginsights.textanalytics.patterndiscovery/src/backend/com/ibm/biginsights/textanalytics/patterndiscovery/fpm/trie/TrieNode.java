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
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie;

import java.util.LinkedList;

/**
 * Represents one node in a trie. Thus holds the label of the node, a frequency
 * counter and references
 * to its children. 
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
public interface TrieNode {

	public static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
	
	public int getLabel();
	public int getCount();
	public void setCount(int count);
	public TrieNode present(LinkedList<Integer> list);
	public void presentIncontinuous(LinkedList<Integer>list, int depth);
	public int getCount(LinkedList<Integer> list);
	public boolean isLeaf();
	public void flushCounts();
	public LinkedList<TrieNode> getChildern();
	public void pruneMinCount(double minCount);
	
	/** 
	 * getter for endReference (see variable for description) 
	 *  
	 */
	public TrieNode getEndReference();
	
	/**
	 * setter for endReference (see variable for description)
	 * @param endReference
	 */
	public void setEndReference(TrieNode endReference);
	
	/**
	 * Removes child from children
	 * @param child
	 * 			- child to remove
	 * @return
	 * 		- true if child was in children and have been removed
	 */
	public void removeChild(int label);
	
}
