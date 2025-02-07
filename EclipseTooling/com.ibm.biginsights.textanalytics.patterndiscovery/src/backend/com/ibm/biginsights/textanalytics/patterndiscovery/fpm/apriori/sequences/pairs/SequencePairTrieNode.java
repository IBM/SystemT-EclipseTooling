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
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.pairs;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.DefaultTrieNode;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.TrieNode;

/**
 * 
 * Maintains extra information for paired sequence mining.
 * 
 * There are two types of subsumption: 
 * -- By trie children: Can be aggregated by by trie traversal. No need to store subsumed matches
 * -- By backrefs: Can be reflected by storing backref-subsumed support separately (note that at each position, the subsumer is unique) 
 * 
 * DONE: store the support set of each sequence 
 * DONE: store the position matched in each sequence
 * DONE: un-subsume matches when a child is deleted
 * DONE: store matches subsumed by endrefs separately
 * 
 * TODO: (later) can we get rid of some of the following to save space: 
 * 	- the parent pointer
 *  - the depth
 *  - the support counter
 *  - the "maxPartnerSupport"
 *  
 *  
 * @see com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.pairs.SequencePairTrie
 */
public class SequencePairTrieNode extends DefaultTrieNode {



	private int depth;
	private SequencePairTrieNode parent;
//	//partner by position
//	HashMap<Point,Collection<SequencePairTrieNode>>transaction2partners;//to be lazy-created
//	//counts of partners
//	HashCount<SequencePairTrieNode>partnerShipCount = new HashCount<SequencePairTrieNode>();
	//maximum counts of partners
	//the support and subsumed counts are being lazy-created
	HashSet<IntPair> support; //set of texts  it is contained in while there is no longer sequence currently represented in the trie together with their position
	HashSet<IntPair> endRefSubsumed; //stores support items that have been subsumed by a node that represents an incomig backref. 
									//an element is considered a subsumed support if (it is present in support AND endRefSubsumed) OR in the support of a descendant
	
	int maxPartnerSupport = 0;
	
	public SequencePairTrieNode(int label, SequencePairTrieNode parent, int hashWidth, int depth) {
		super(label, hashWidth);
		this.depth = depth;
		this.parent = parent;
		//System.out.println("New node: "+this.sequenceDenoted());
	}
	
//	public void addMatch(int transaction, int pos, Collection<SequencePairTrieNode>partners){
//		if(transaction2partners==null)
//			transaction2partners = new HashMap<Point, Collection<SequencePairTrieNode>>();
//		
//		transaction2partners.put(new Point(transaction,pos), partners);
//		for (SequencePairTrieNode partner : partners) {
//			System.out.println("\t\tadding: "+this.sequenceDenoted()+" , "+partner.sequenceDenoted());
//			int newVal = partnerShipCount.increment(partner);
//			maxPartnerSupport = Math.max(maxPartnerSupport, newVal);
//		}
//	}
//	/**
//	 * Important: Always retract before adding matches at the same position. The hashMap will overwrite otherwise
//	 * @param transaction
//	 * @param pos
//	 */
//	public void retractMatch(int transaction, int pos){
//		if(transaction2partners==null)
//			transaction2partners = new HashMap<Point, Collection<SequencePairTrieNode>>();
//		Collection<SequencePairTrieNode> obsoletePartners = transaction2partners.remove(new Point(transaction,pos));
//		if(obsoletePartners==null)return;
//		for (SequencePairTrieNode exPartner : obsoletePartners) {
//			System.out.println("\t\tretracting: "+this.sequenceDenoted()+" , "+exPartner.sequenceDenoted());
//			int newVal = partnerShipCount.decrement(exPartner);
//			if(newVal<0)partnerShipCount.update(exPartner, -newVal);//don't have negative counts
//			if(newVal+1==maxPartnerSupport){
//				maxPartnerSupport= 0;
//				for (Integer partnerSupport : partnerShipCount.values()) {
//					maxPartnerSupport = Math.max(maxPartnerSupport, partnerSupport);
//				}
//			}
//		}
//	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unika.aifb.fpm.trie.TrieNode#setEndReference()
	 */
	//@Override
	public void setEndReference(TrieNode endReference) {
		//System.out.println("Setting end reference from "+this.sequenceDenoted()+" to "+((SequencePairTrieNode)endReference).sequenceDenoted());
		this.endReference = endReference;
		
	}
	/** 
	 * Overridden to integrate support-set updates. 
	 * Also improved: DefaultTrieNode removeChild iterates over all the children[] even though one could use the label to find out which element the remove.
	 * @see com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.DefaultTrieNode#removeChild(int)
	 */
	//@Override
	public void removeChild(int label)  {
		//Overridden to integrate support-set updates
		// add support back to parents
		//notify endref about deletion to correct subsumed
		if (getChildrenArray() == null)
			return;
		int childHash = (new Integer(label)).hashCode() % hashWidth;
			DefaultTrieNode child = (DefaultTrieNode)getChildrenArray()[childHash];
		if (!(child == null)) {
			
			if (child != null && child.getLabel() == label) {
				if(((SequencePairTrieNode)child).support != null){
					for (IntPair supportItem : ((SequencePairTrieNode)child).support) {
						((SequencePairTrieNode)child.endReference).removeEndRefSubsumed(supportItem);
						this.addSupport(supportItem);
					}
				}
				getChildrenArray()[childHash] = child.sibling;
				child = child.sibling;
			}
			DefaultTrieNode furtherChild = child;					
			while (furtherChild != null && furtherChild.sibling != null) {						
				if (furtherChild.sibling.getLabel() == label) {
					if(((SequencePairTrieNode)furtherChild.sibling).support != null){
						for (IntPair supportItem : ((SequencePairTrieNode)furtherChild.sibling).support) {
							((SequencePairTrieNode)furtherChild.sibling.endReference).removeEndRefSubsumed(supportItem);
							this.addSupport(supportItem);
						}
					}
					furtherChild.sibling = furtherChild.sibling.sibling;
				} else {
					furtherChild = furtherChild.sibling;
				}
			}
			}
		}
	/**
	 * This method is used is used during trie generation
	 * The support count information computed here is flushed during computeSupport
	 * 
	 * @see com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.DefaultTrieNode#present(java.util.LinkedList)
	 */
	public TrieNode present(LinkedList<Integer> list) {
		// actual label value is included in list
		SequencePairTrieNode child = null;
		if (list.get(0) == getLabel()) {
			setCount(getCount()+1);

			if (list.size() == 1){
				//if match, call SequencePairPatternProcessor.newMatch --change signature
				//NO: this is done during support counting! processor.newMatch(this, parent, start, start+depth);
				return this;	
			}
			if (getChildrenArray() == null) {
				setChildren(new DefaultTrieNode[getHashWidth()]);
			}
			list.remove();
			int childID = list.get(0).hashCode() % getHashWidth();
			if (getChildrenArray()[childID] == null) {
				child = new SequencePairTrieNode(list.get(0).intValue(), this, getHashWidth(),depth+1);
				getChildrenArray()[childID] = child;
			}
			return ((SequencePairTrieNode)getChildrenArray()[childID]).present(list);
		}
		// try with next node on same tree level
		if (sibling != null) {
			return ((SequencePairTrieNode)sibling).present(list);

		} else {
			sibling = new SequencePairTrieNode(list.get(0), parent,getHashWidth(),depth);
			return ((SequencePairTrieNode)sibling).present(list);
		}
		
	}
	public void addSupport(IntPair transaction){
		if(support == null) support = new HashSet<IntPair>();
		support.add(transaction);
	}
	public void removeSupport(IntPair transaction){
		if(support == null) return;
		support.remove(transaction);
	}
	public void addEndRefSubsumed(IntPair transaction){
		if(endRefSubsumed == null) endRefSubsumed = new HashSet<IntPair>();
		endRefSubsumed.add(transaction);
	}
	public void removeEndRefSubsumed(IntPair transaction){
		if(endRefSubsumed == null) return;
		endRefSubsumed.remove(transaction);
	}
	
	public void presentIncontinuous(LinkedList<Integer> list, int depth) {
//		throw new NotImplementedException();
	}
	public List<Integer>sequenceDenoted(){
		List<Integer>result = new LinkedList<Integer>();
		SequencePairTrieNode node = this;
		do {
			result.add(0, node.getLabel());
			node = node.parent;
		} while(node!=null&&node.parent!=null);
		return result;
	}
	public int maxPartnerSupport() {
		return maxPartnerSupport;
	}
	
	public int hashCode(){
		int result = ((int)Math.pow(31,this.getLabel())+2047*this.depth+4095);
		if(parent!= null) result *=parent.hashCode();
		return result;
	}
	
	public String toString(){
		return ""+this.sequenceDenoted();
	}

	public int getDepth() {
		return depth;
	}

	public HashSet<IntPair> getSupport() {
		return support;
	}

	public void setSupport(HashSet<IntPair> support) {
		this.support = support;
	}
	/**Warning: no presence-check will be done
	 * @param child
	 */
	public void addChild(SequencePairTrieNode child){
		child.parent = this;
		int childHash = (new Integer(child.getLabel())).hashCode() % hashWidth;
		if(children==null)children = new DefaultTrieNode[hashWidth];
		DefaultTrieNode otherChild = (DefaultTrieNode)children[childHash];
		getChildrenArray()[childHash] = child;
		if (!(otherChild == null)) {
			child.sibling = otherChild;
		}
	}
}
