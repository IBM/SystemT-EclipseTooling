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

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;

/**
 * Trie node that stores successors in an array according to a hash value. To
 * realize the hash-buckets in a memory-efficient way, each node has an
 * additional reference called sibling. This way, the hash-array, directly holds
 * nodes. Should after hash lookup the node found not match the label searched
 * for, the matcher can iterative follow the sibling links.
 * 
 * IDEA: If the node was aware of the maximal depth underneath it could stop
 * incontinuous matching if an insufficient number of tokens is left in the
 * pattern.
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 * 
 */
@SuppressWarnings("unchecked")
public class DefaultTrieNode implements TrieNode, Comparable {
  
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
      "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
   
	static IPDLog logger = PDLogger.getLogger(DefaultTrieNode.class.getName());
	private int label;
	private int count = 0;
	protected DefaultTrieNode[] children;
	// each node is aware of one sibling
	public DefaultTrieNode sibling;
	protected int hashWidth;

	// /*
	// * The following two variables take part in a cycle check mechanism for
	// recursive methods.
	// * To check if this node, has been visited in this recursion, runNr and
	// processedRun are
	// * compared. If they are equal, a visit has taken place. runNr static and
	// can thus be
	// * easily increased for all instances of this object when a new recursion
	// is started.
	// * Obviously, this is not thread safe.
	// */
	//	
	// static int runNr = 0;
	// int processedRun = 0;
	public DefaultTrieNode(int label, int hashWidth) {
		this.label = label;
		this.hashWidth = hashWidth;

	}

	/** number of visits */
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount(LinkedList<Integer> list) {
		// checks if the nodes included in the given list are also included in
		// the children array
		if (list.get(0) == label) {
			if (list.size() == 1)
				return count;
			list.remove();
			int childID = list.get(0).hashCode() % hashWidth;
			if (children != null && children[childID] != null) {
				return children[childID].getCount(list);
			}
		}
		// try next node on same tree level
		if (sibling != null)
			return sibling.getCount(list);
		return 0;

	}

	/** node label */
	public int getLabel() {
		return label;
	}

	/** true if node is leaf */
	public boolean isLeaf() {
		return (children == null); // for leafes, no child array is created
	}

	/**
	 * 
	 */
	public TrieNode present(LinkedList<Integer> list) {
		// actual label value is included in list
		DefaultTrieNode child = null;
		if (list.get(0) == label) {
			count++;
			if (list.size() == 1)
				return this;

			if (children == null) {
				children = new DefaultTrieNode[hashWidth];
			}
			list.remove();
			int childID = list.get(0).hashCode() % hashWidth;
			if (children[childID] == null) {
				child = new DefaultTrieNode(list.get(0).intValue(), hashWidth);
				children[childID] = child;
			}
			return children[childID].present(list);
		}
		// try with next node on same tree level
		if (sibling != null) {
			return sibling.present(list);

		} else {
			sibling = new DefaultTrieNode(list.get(0), hashWidth);
			return sibling.present(list);
		}

	}

	// flush all counts on siblings on the same tree level and childs
	public void flushCounts() {
		this.count = 0;
		if (sibling != null)
			sibling.flushCounts();
		if (children == null)
			return;
		for (DefaultTrieNode child : children) {
			if (child != null) {
				child.flushCounts();
			}
		}

	}

	private static int iteration; // for repetition check

	public void presentIncontinuous(LinkedList<Integer> list, int depth) {
		iteration++;
		presentIncontinuousR(list, false, depth);

	}

	/**
	 * @param list
	 * @param isSiblingCopy
	 *            If the list is provided to a sibling, the sibling is not
	 *            supposed to recurr on in by taking it apart (as this will be
	 *            done by its greater sibling passing down a new copy if need
	 *            by)
	 * @param depth
	 *            the length of the incontinuous sequences to be presented.
	 *            TODO: beim Rekursionsaufstieg z�hlen und zwar nur, wenn wir
	 *            unten angekommen sind. TODO: Nur einmal pro pr�sentierter
	 *            Sequenz z�hlen.
	 */
	private int lastIterationIncreased = -1; // for repetition check

	private boolean presentIncontinuousR(LinkedList<Integer> list,
			boolean isSiblingCopy, int depth) {
		logger.info("This: " + this);
		logger.info("List: " + list);
		boolean result = false;
		// directly recurr on siblings
		if (sibling != null) {
			result |= sibling.presentIncontinuousR(list, true, depth);
		}
		// recurr by truncating the list
		if (!isSiblingCopy && list.size() > 1) {
			LinkedList listCopy = new LinkedList(list);
			listCopy.removeFirst();
			result |= presentIncontinuousR(listCopy, false, depth);
		}
		// check if first list element matches the node (has to be done last
		// because it may return without running the others)
		if (list.getFirst().equals(this.getLabel())) {
			// if so, increase the counter
			// recurr with the children, if not at right depth
			if (depth == 1) {
				if (lastIterationIncreased != iteration) {
					this.count++;
				}
				lastIterationIncreased = iteration;
				return true;
			}
			if (children != null) {
				// System.out.print(list.getFirst()+", ");
				boolean matchBelow = false;
				for (DefaultTrieNode child : children) {
					if (child != null && list.size() > 1) {
						LinkedList listCopy = new LinkedList(list);
						listCopy.removeFirst();
						matchBelow |= child.presentIncontinuousR(listCopy,
								false, depth - 1);
						result |= matchBelow;
					}
				}
				if (matchBelow) {
					this.count++;
				}
			}
		}

		return result;
	}

	public LinkedList<TrieNode> getChildern() {
		LinkedList<TrieNode> result = new LinkedList<TrieNode>();
		if (children == null)
			return result;
		for (DefaultTrieNode child : children) {
			if (child != null) {
				result.add(child);
				DefaultTrieNode furtherChild = child.sibling;
				while (furtherChild != null) {
					result.add(furtherChild);
					furtherChild = furtherChild.sibling;
				}
			}
		}
		return result;
	}

	protected TrieNode[] getChildrenArray(){
		return children;
	}
	protected void setChildren(DefaultTrieNode[] children){
		this.children = children;
	}
	/**
	 * This method assumes that for each node A holds A.count >= A.child.count
	 * 
	 * Note: in deleting children, we do not update the leaf status of the node.
	 * Reason: Some methods assume that all leaves are at the same depth which
	 * is the case for all leaves created during presentation.
	 * 
	 * @see com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.TrieNode#pruneMinCount(double)
	 */
	public void pruneMinCount(double minCount) {
		if (children == null)
			return;
		for (int i = 0; i < children.length; i++) {
			if (!(children[i] == null)) {
				while (children[i] != null && children[i].count < minCount) {
					children[i] = children[i].sibling;
				}
				if (children[i] != null) {
					children[i].pruneMinCount(minCount);
					DefaultTrieNode furtherChild = children[i];
					while (furtherChild.sibling != null) {
						furtherChild.sibling.pruneMinCount(minCount);
						if (furtherChild.sibling.count < minCount) {
							furtherChild.sibling = furtherChild.sibling.sibling;
						} else {
							furtherChild = furtherChild.sibling;
						}
					}
				}
			}
		}
	}

	public String toString() {
		return "label=" + label + " count=" + count + " hasSiblings="
				+ (!(sibling == null)) + " isLeaf=" + isLeaf() + " hasChilds: "+children;
	}

	public String toDebugString(String indent) {
		String result = indent + this.toString() + "\n";
		for (TrieNode child : this.getChildern()) {
			result += ((DefaultTrieNode) child).toDebugString(indent + " ");
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	//@Override
	public int compareTo(Object o) {
		// Compare is based on label
		if (o instanceof DefaultTrieNode) {
			DefaultTrieNode foreignObject = (DefaultTrieNode) o;
			if (this.label > foreignObject.label)
				return 1;
			return -1;
		}
		return 0;
	}

	/**
	 * pointer to endRefernce: This is the last node of the squence "end(x)=
	 * (x_2, ..., x_n)" e.g. x={1,4,5} the endReference points to 5 in the
	 * sequence {4,5}
	 */
	public TrieNode endReference = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unika.aifb.fpm.trie.TrieNode#getEndReference()
	 */
	//@Override
	public TrieNode getEndReference() {
		return endReference;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unika.aifb.fpm.trie.TrieNode#setEndReference()
	 */
	//@Override
	public void setEndReference(TrieNode endReference) {
		this.endReference = endReference;
	}

	/**
	 * @return the hashWidth
	 */
	public int getHashWidth() {
		return this.hashWidth;
	}

	/**
	 * @param hashWidth
	 *            the hashWidth to set
	 */
	public void setHashWidth(int hashWidth) {
		this.hashWidth = hashWidth;
	}

//	public boolean removeChild(TrieNode childToRemove) {
//		if (children != null
//				& children.length < new Integer(childToRemove.getLabel())
//						.hashCode()
//						% hashWidth
//				& children[new Integer(childToRemove.getLabel()).hashCode()
//						% hashWidth] != null) {
//			DefaultTrieNode child = children[childToRemove.getLabel()];
//			if (child.getLabel() == childToRemove.getLabel())
//				children[new Integer(childToRemove.getLabel()).hashCode()
//						% hashWidth] = null;
//			DefaultTrieNode childBefore = null;
//			while (child.getLabel() != childToRemove.getLabel()) {
//				childBefore = child;
//				child = child.sibling;
//			}
//
//			// delete reference
//			childBefore.sibling = null;
//			return true;
//		}
//		return false;
//	}
	public void removeChild(int label)  {
		if (children == null)
			return;
		for (int i = 0; i < children.length; i++) {
			if (!(children[i] == null)) {
				if (children[i] != null && children[i].label == label) {				 
					children[i] = children[i].sibling;
				}
					DefaultTrieNode furtherChild = children[i];					
					while (furtherChild != null && furtherChild.sibling != null) {						
						if (furtherChild.sibling.label == label) {
							furtherChild.sibling = furtherChild.sibling.sibling;
						} else {
							furtherChild = furtherChild.sibling;
						}
					}
				}
			}
		}
	
}
