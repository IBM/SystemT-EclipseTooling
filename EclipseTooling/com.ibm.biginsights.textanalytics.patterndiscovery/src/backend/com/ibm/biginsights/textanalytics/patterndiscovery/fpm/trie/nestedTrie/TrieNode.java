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
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.nestedTrie;

import java.util.LinkedList;

/**
 * Represents one node in a trie. Thus holds the label of the node, a frequency
 * counter and references
 * to its children. 
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 * 
 *  Kleb (kleb@fzi.de) - extended to general nested trie's
 * 
 * ----- NOTE: Unfortunatly we can't use any generics here. This is based on the
 * lack of depth of the nested statements !!! -----
 *
 */
@SuppressWarnings("unchecked")
public interface TrieNode {

	public static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
	
	/** the actual node value (Note: The nesting depth is included in the children implementation) */
	public int getLabel();
	
	/** get nesting depth of node */
	public int getDepth();
	
	/** set nesting depth of node */
	public void setDepth(int depth);
	
	/** return support value */
	public int getCount();
	
	/** set support value */
	public void setCount(int count);
	
	/** get support of "rest" pattern */ 
	public int getCount(LinkedList list);
	
	/** present collection */
	public TrieNode present(LinkedList list);
	
	/** incontinuous present collection */
	public void presentIncontinuous(LinkedList list, int depth);
	
	/** check if it's a leaf node */
	public boolean isLeaf();
	
	/** flush support value */
	public void flushCounts();
	
	/** list of childrens according their nesting depth 
	 */
	public LinkedList<TrieNode> getChildern();
	
	/** This method assumes that for each node A holds A.count >= A.child.count */
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
