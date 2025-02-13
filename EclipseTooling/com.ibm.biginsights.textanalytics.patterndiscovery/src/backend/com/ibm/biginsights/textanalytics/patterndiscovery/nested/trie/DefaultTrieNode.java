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
package com.ibm.biginsights.textanalytics.patterndiscovery.nested.trie;

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
  
private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";
   
	static IPDLog logger = PDLogger.getLogger(DefaultTrieNode.class.toString());

	/**
	 * Type defines the if child is a within the sequence (here type="WithinSequence") or starts a new sequence within the sequence (here type="NewSequence")
	 * Standard is within the sequence
	 */
	public Type type = Type.WithinSequence;

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	private int label;
	private int count = 0;
	private DefaultTrieNode[] childrenWithinSequence;
	private DefaultTrieNode[] childrenNewSequence;
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
	public DefaultTrieNode(int label, Type type, int hashWidth) {
		this.label = label;
		this.hashWidth = hashWidth;
		this.type = type;
	}

	/** number of visits */
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount(LinkedList<Integer> listIn) {
		// checks if the nodes included in the given list are also included in
		// the children array		
		LinkedList list = innerCopy(listIn);
		LinkedList cloneList = innerCopy(list);
		int value = returnFirstElement(list);
		if (value == label) {
			if (list.size() == 1)
				return count;
			boolean patternClosedSave = patternClosed;
			cloneList = innerCopy(list);
			value = returnFirstElement(list);
			int childID = ((Integer) value).hashCode() % hashWidth;
			if (patternClosedSave) {
				if (childrenNewSequence != null && childrenNewSequence[childID] != null) {
					return childrenNewSequence[childID].getCount(cloneList);
				}
			} else {
				if (childrenWithinSequence != null && childrenWithinSequence[childID] != null) {
					return childrenWithinSequence[childID].getCount(cloneList);
				}
			}
		}
		// try next node on same tree level
		if (sibling != null)
			return sibling.getCount(cloneList);
		return 0;

	}

	/** node label */
	public int getLabel() {
		return label;
	}

	/** true if node is leaf */
	public boolean isLeaf() {
		return (childrenWithinSequence == null && childrenNewSequence == null); // for leafes, no child array is created
	}

	/** 
	 * present given sequence 
	 */
	public TrieNode present(LinkedList<Integer> listIn) {
		// actual label value is included in list
		DefaultTrieNode child = null;
		// get value and sequence information
//		LinkedList cloneList = innerCopy(list);
		LinkedList list = innerCopy(listIn);
		LinkedList cloneList = innerCopy(list);	
		int value = returnFirstElement(list);
		if (value == label) {
			count++;
			if (list.size() == 0)
				return this;
			boolean patternClosedSave = patternClosed;
			cloneList = innerCopy(list);
			Integer childValue = returnFirstElement(cloneList);
			int childID = childValue.hashCode() % hashWidth;
			// new sequence will start
			if (patternClosedSave) {
				if (childrenNewSequence == null) childrenNewSequence = new DefaultTrieNode[hashWidth];
				if (childrenNewSequence[childID] == null) {
					child = new DefaultTrieNode(childValue, Type.NewSequence,
							hashWidth);
					childrenNewSequence[childID] = child;
				}
				return childrenNewSequence[childID].present(list);
			} else {
				// within given sequence
				if (childrenWithinSequence == null) childrenWithinSequence = new DefaultTrieNode[hashWidth];
				if (childrenWithinSequence[childID] == null) {
					child = new DefaultTrieNode(childValue, Type.WithinSequence,
							hashWidth);
					childrenWithinSequence[childID] = child;
				}
				return childrenWithinSequence[childID].present(list);
			}

		}
		// try with next node on same tree level
		if (sibling != null) {
			return sibling.present(cloneList);

		} else {
			sibling = new DefaultTrieNode(value, type, hashWidth);
//			System.out.println("List: "+cloneList);
			return sibling.present(cloneList);
		}

	}

	// flush all counts on siblings on the same tree level and childs
	public void flushCounts() {
		this.count = 0;
		if (sibling != null)
			sibling.flushCounts();
		if (childrenWithinSequence != null) {
			for (DefaultTrieNode child : childrenWithinSequence) {
				if (child != null) {
					child.flushCounts();
				}
			}
		}
		if (childrenNewSequence != null) {
			for (DefaultTrieNode child : childrenNewSequence) {
				if (child != null) {
					child.flushCounts();
				}
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
			if (childrenNewSequence != null) {
				// System.out.print(list.getFirst()+", ");
				boolean matchBelow = false;
				for (DefaultTrieNode child : childrenNewSequence) {
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
			if (childrenWithinSequence != null) {
				// System.out.print(list.getFirst()+", ");
				boolean matchBelow = false;
				for (DefaultTrieNode child : childrenWithinSequence) {
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

	public LinkedList<TrieNode> getChildernWithinSequence() {
		LinkedList<TrieNode> result = new LinkedList<TrieNode>();
		if (childrenWithinSequence == null)
			return result;
		for (DefaultTrieNode child : childrenWithinSequence) {
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

	public LinkedList<TrieNode> getChildernNewSequence() {
		LinkedList<TrieNode> result = new LinkedList<TrieNode>();
		if (childrenWithinSequence == null)
			return result;
		for (DefaultTrieNode child : childrenNewSequence) {
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
		if (childrenWithinSequence == null) {
			for (int i = 0; i < childrenWithinSequence.length; i++) {
				if (!(childrenWithinSequence[i] == null)) {
					while (childrenWithinSequence[i] != null && childrenWithinSequence[i].count < minCount) {
						childrenWithinSequence[i] = childrenWithinSequence[i].sibling;
					}
					if (childrenWithinSequence[i] != null) {
						childrenWithinSequence[i].pruneMinCount(minCount);
						DefaultTrieNode furtherChild = childrenWithinSequence[i];
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
		if (childrenNewSequence == null) {
			for (int i = 0; i < childrenNewSequence.length; i++) {
				if (!(childrenNewSequence[i] == null)) {
					while (childrenNewSequence[i] != null && childrenNewSequence[i].count < minCount) {
						childrenNewSequence[i] = childrenNewSequence[i].sibling;
					}
					if (childrenNewSequence[i] != null) {
						childrenNewSequence[i].pruneMinCount(minCount);
						DefaultTrieNode furtherChild = childrenNewSequence[i];
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
	}

	public String toString() {
		return "label=" + label + " count=" + count + " hasSiblings="
				+ (!(sibling == null)) + " isLeaf=" + isLeaf();
	}

	public String toDebugString(String indent) {
		String result = indent + this.toString() + "\n";
		result += "ChildrenNewSequence: ";
		for (TrieNode child : this.getChildernNewSequence()) {			
			result += ((DefaultTrieNode) child).toDebugString(indent + " ");
		}
		result += "ChildrenWithinSequence: ";
		for (TrieNode child : this.getChildernWithinSequence()) {			
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
//	public void removeChild(int label) {
//		if (children == null)
//			return;
//		for (int i = 0; i < children.length; i++) {
//			if (!(children[i] == null)) {
//				if (children[i] != null && children[i].label == label) {
//					children[i] = children[i].sibling;
//				}
//				DefaultTrieNode furtherChild = children[i];
//				while (furtherChild != null && furtherChild.sibling != null) {
//					if (furtherChild.sibling.label == label) {
//						furtherChild.sibling = furtherChild.sibling.sibling;
//					} else {
//						furtherChild = furtherChild.sibling;
//					}
//				}
//			}
//		}
//	}

	// utility -------------------------------------------------------------------------------------------------------------------------
	private int stepIn = 0;
	// if patternClosed == 1 the actual subsecquence is finished and next element will be in a newSequence
	private boolean patternClosed = false;

	private Integer returnFirstElement(LinkedList pattern) {
		patternClosed = false;
		//		System.out.println("Pattern in: "+pattern);
		Integer firstInteger = -1;
		if (pattern.size() < 1)
			return firstInteger;
		if (pattern.getFirst() instanceof LinkedList) {
			LinkedList first_PatternPart = (LinkedList) pattern.getFirst();
			//			System.out.println("first: "+first_PatternPart);
			//			System.out.println("depth-IN: "+depth);
			stepIn = stepIn + 1;
			firstInteger = returnFirstElement(first_PatternPart);
			if (first_PatternPart.size() == 0) {
				pattern.remove();
				patternClosed = true;
			}
		} else {
			if (pattern.getFirst() instanceof Integer) {
				firstInteger = (Integer) pattern.getFirst();
				pattern.remove();
				//				System.out.println("pattern in int: "+firstInteger);
				if (pattern.size() == 0)
					pattern = null;
				return firstInteger;
			}
		}
		return firstInteger;
	}
	
	private LinkedList innerCopy(LinkedList src){
		LinkedList dest = new LinkedList();
		if (src.getFirst() instanceof LinkedList) {
			
			// make a shallow copy of each included list
			// NOTE: that's enough here. the included integeres are primitive datatypes and so they will be fully new instances also by a shallow copy			
			for(Object innerListO: src) {
				LinkedList innerList = (LinkedList) innerListO;
				dest.add((LinkedList) innerList.clone());
			}
		} else {
			dest = (LinkedList) src.clone();
		}
		return dest;		
	}

}
