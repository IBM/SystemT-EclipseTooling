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
/**
 * 
 * License restrictions apply
 * 
 * Universitaet Karlsruhe
 * Institut AIFB (http://www.aifb.uni-karlsruhe.de)
 * Sebastian Blohm (seb@aifb.uni-karlsruhe.de)
 */
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.nestedTrie;

import java.util.HashMap;
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
 *  Kleb (kleb@fzi.de) - extended to general nested trie's
 * 
 * ----- NOTE: Unfortunatly we can't use any generics here. This is based on the
 * lack of depth of the nested statements !!! -----
 */
@SuppressWarnings("unchecked")
public class DefaultTrieNode implements TrieNode, Comparable {
	static IPDLog logger = PDLogger.getLogger(DefaultTrieNode.class.getName());
	private int label;
	// note depth 0 is only for ROOT node
	private int depth;
	private int count = 0;	
	private HashMap<Integer, DefaultTrieNode[]> children = new HashMap<Integer, DefaultTrieNode[]>();
	// each node is aware of one sibling
	public DefaultTrieNode sibling;
	private int hashWidth;

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
	public DefaultTrieNode(int label, int depth, int hashWidth) {
		this.label = label;
		this.depth = depth;
		this.hashWidth = hashWidth;
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.trie.nestedTrie.TrieNode#getDepth()
	 */	
	public int getDepth() {
		return depth;
	}
	
	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.trie.nestedTrie.TrieNode#setDepth(int)
	 */	
	public void setDepth(int depth) {
		this.depth = depth;
		
	}

	/** number of visits */
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	/** @TODO */
//	public int getCount(LinkedList list) {
//		// checks if the nodes included in the given list are also included in
//		// the children array
//		if (list.size() == 1 && (list.get(0) instanceof Integer)) {
//			if ((Integer) list.get(0) == label) {
//			if (list.size() == 1)
//				return count;
//			list.remove();
//			int childID = list.get(0).hashCode() % hashWidth;
//			if (children != null && children[childID] != null) {
//				return children[childID].getCount(list);
//			}
//		}
//		}
//		// try next node on same tree level
//		if (sibling != null)
//			return sibling.getCount(list);
//		return 0;
//
//	}

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
	@SuppressWarnings("unused")
	public TrieNode present(LinkedList list, int depth) {
		logger.info("Input in present Method_ "+list+"depth: "+depth);
		
		DefaultTrieNode child = null;
		LinkedList restPattern = moveRight(list);
		logger.info("Restpattern: "+restPattern);
		// primatives are allways cloned
		int childDepth = this.childDepth;
		int depthToUseForStoringChild = childDepth;
		if (childDepth < depth) {
			depthToUseForStoringChild=1;
		} else {
			if (childDepth >= depth) depthToUseForStoringChild = 0;
		}
		//TODO:
		return null;
	}

	/** @TODO */
	// flush all counts on siblings on the same tree level and childs
//	public void flushCounts() {
//		this.count = 0;
//		if (sibling != null)
//			sibling.flushCounts();
//		if (children == null)
//			return;
//		for (DefaultTrieNode child : children) {
//			if (child != null) {
//				child.flushCounts();
//			}
//		}
//
//	}

	@SuppressWarnings("unused")
	private static int iteration; // for repetition check

	/** @TODO */
//	public void presentIncontinuous(LinkedList<Integer> list, int depth) {
//		iteration++;
//		presentIncontinuousR(list, false, depth);
//
//	}

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
	@SuppressWarnings("unused")
	private int lastIterationIncreased = -1; // for repetition check

	/** @TODO */
//	private boolean presentIncontinuousR(LinkedList<Integer> list,
//			boolean isSiblingCopy, int depth) {
//		logger.info("This: " + this);
//		logger.info("List: " + list);
//		boolean result = false;
//		// directly recurr on siblings
//		if (sibling != null) {
//			result |= sibling.presentIncontinuousR(list, true, depth);
//		}
//		// recurr by truncating the list
//		if (!isSiblingCopy && list.size() > 1) {
//			LinkedList listCopy = new LinkedList(list);
//			listCopy.removeFirst();
//			result |= presentIncontinuousR(listCopy, false, depth);
//		}
//		// check if first list element matches the node (has to be done last
//		// because it may return without running the others)
//		if (list.getFirst().equals(this.getLabel())) {
//			// if so, increase the counter
//			// recurr with the children, if not at right depth
//			if (depth == 1) {
//				if (lastIterationIncreased != iteration) {
//					this.count++;
//				}
//				lastIterationIncreased = iteration;
//				return true;
//			}
//			if (children != null) {
//				// System.out.print(list.getFirst()+", ");
//				boolean matchBelow = false;
//				for (DefaultTrieNode child : children) {
//					if (child != null && list.size() > 1) {
//						LinkedList listCopy = new LinkedList(list);
//						listCopy.removeFirst();
//						matchBelow |= child.presentIncontinuousR(listCopy,
//								false, depth - 1);
//						result |= matchBelow;
//					}
//				}
//				if (matchBelow) {
//					this.count++;
//				}
//			}
//		}
//
//		return result;
//	}

	/** @TODO */
//	public LinkedList<TrieNode> getChildern() {
//		LinkedList<TrieNode> result = new LinkedList<TrieNode>();
//		if (children == null)
//			return result;
//		for (DefaultTrieNode child : children) {
//			if (child != null) {
//				result.add(child);
//				DefaultTrieNode furtherChild = child.sibling;
//				while (furtherChild != null) {
//					result.add(furtherChild);
//					furtherChild = furtherChild.sibling;
//				}
//			}
//		}
//		return result;
//	}

	/**
	 * This method assumes that for each node A holds A.count >= A.child.count
	 * 
	 * Note: in deleting children, we do not update the leaf status of the node.
	 * Reason: Some methods assume that all leaves are at the same depth which
	 * is the case for all leaves created during presentation.
	 * 
	 * @see edu.unika.aifb.fpm.trie.TrieNode#pruneMinCount(double)
	 */
	/** @TODO */
//	public void pruneMinCount(double minCount) {
//		if (children == null)
//			return;
//		for (int i = 0; i < children.length; i++) {
//			if (!(children[i] == null)) {
//				while (children[i] != null && children[i].count < minCount) {
//					children[i] = children[i].sibling;
//				}
//				if (children[i] != null) {
//					children[i].pruneMinCount(minCount);
//					DefaultTrieNode furtherChild = children[i];
//					while (furtherChild.sibling != null) {
//						furtherChild.sibling.pruneMinCount(minCount);
//						if (furtherChild.sibling.count < minCount) {
//							furtherChild.sibling = furtherChild.sibling.sibling;
//						} else {
//							furtherChild = furtherChild.sibling;
//						}
//					}
//				}
//			}
//		}
//	}

	public String toString() {
		return "label=" + label + " count=" + count + " hasSiblings="
				+ (!(sibling == null)) + " isLeaf=" + isLeaf();
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
	private TrieNode endReference = null;

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

	
	/** @TODO */
//	public void removeChild(int label)  {
//		if (children == null)
//			return;
//		for (int i = 0; i < children.length; i++) {
//			if (!(children[i] == null)) {
//				if (children[i] != null && children[i].label == label) {				 
//					children[i] = children[i].sibling;
//				}
//					DefaultTrieNode furtherChild = children[i];					
//					while (furtherChild != null && furtherChild.sibling != null) {						
//						if (furtherChild.sibling.label == label) {
//							furtherChild.sibling = furtherChild.sibling.sibling;
//						} else {
//							furtherChild = furtherChild.sibling;
//						}
//					}
//				}
//			}
//		}

	
	// ---------------------------- UTIL SECTION ------------------------------------->>>
	int childDepth = 0;
	int actualElementPointer = 0;

	public LinkedList moveRight(LinkedList pattern) {
		childDepth = 0;
		/** 
		 * TODO: change this! The object should be changed via the object as return parameter
		 * 
		 */
		actualElementPointer = returnFirstElement(pattern);
		return pattern;
	}


	private Integer returnFirstElement(LinkedList pattern) {
		Integer firstInteger = -1;
		if(pattern.size() < 1) return firstInteger;		
		if (pattern.getFirst() instanceof LinkedList) {
			LinkedList first_PatternPart = (LinkedList) pattern.getFirst();
			childDepth = childDepth +1;
			firstInteger = returnFirstElement(first_PatternPart);
            if(first_PatternPart.size() == 0) {
            	pattern.remove();
            	childDepth = childDepth -1;
            }
		} else {
			if (pattern.getFirst() instanceof Integer) {
				firstInteger = (Integer) pattern.getFirst();				
				pattern.remove();
				if (pattern.size() == 0) pattern = null;
				return firstInteger;
			}
		}
		return firstInteger;
	}
	// <<<---------------------------- UTIL SECTION -------------------------------------

	
	/** @TODO 
	 * 
	 * DELETE EVERYTHING HERE TILL BOTTOM
	 * */
	
	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.trie.nestedTrie.TrieNode#flushCounts()
	 */
	//@Override
	public void flushCounts() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.trie.nestedTrie.TrieNode#getChildern()
	 */
	//@Override
	public LinkedList<TrieNode> getChildern() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.trie.nestedTrie.TrieNode#getCount(java.util.LinkedList)
	 */
	//@Override
	public int getCount(LinkedList list) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.trie.nestedTrie.TrieNode#present(java.util.LinkedList)
	 */
	//@Override
	public TrieNode present(LinkedList list) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.trie.nestedTrie.TrieNode#presentIncontinuous(java.util.LinkedList)
	 */
	//@Override
	public void presentIncontinuous(LinkedList list, int depth) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.trie.nestedTrie.TrieNode#pruneMinCount(double)
	 */
	//@Override
	public void pruneMinCount(double minCount) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.trie.nestedTrie.TrieNode#removeChild(int)
	 */
	//@Override
	public void removeChild(int label) {
		// TODO Auto-generated method stub
		
	}
	
}
