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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.Pattern;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.SequencePattern;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.SequenceTransaction;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.SetPatternProcessorForSequences;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.DefaultTrieNode;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.Trie;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.TrieNode;


/**
 * Uses the sequence mining infrastructure to mine frequently co-occurring 
 * pairs of sequences which are defined as follows:
 * 
 *  All (s_x,s_y)= ((x1...xi),(y1...yj)) for which exists a support set S(s_x,s_y)) of 
 *  transactions that has at least a given size. A transaction t belongs to the support
 *  set if:
 *  - t matches both s_x and s_y 
 *  - there is no longer pair of sequences that is an extension of (s_x,s_y) that also 
 *  matches t.
 *  - if the matches of (x1...xi) and (y1...yj) in t are non-overlapping
 *  
 *  General idea: 
 *  - Keep track of all transactions that contain a given sequence pair. Pruning of sequences 
 *  that do not contribute to a S(s_x,s_y)) of all frequent sequences found so far.
 *  - Know, the position where something matches during recursion and avoid counting in overlapping matches. 
 *  - When a (x1...xi) is counted in and it co-occurrs with s_y in t then retract the co-occurrence of (x1...xi-1) and s_y 
 *  from t.
 *   
 *   TODO: the support set maintained at each node makes it unneccesary to go through the data for support check
 *     can we make candidate generation and support counting one step?
 *     compute and set support during candidate generation, do nothing during computeSupport
 *     TODO: get subsumption right
 *   TODO: this data structure will hold all the interesting matching data in the end. Allow it to output
 *     
 *   
 *   
 *   TODO(later): the newNodes datastructure is not needed if we prune by an iteration over the tree (probably a
 *   space time trade-off) 
 * 
 *
 */
@SuppressWarnings("unchecked")
public class BackupOfSequencePairPatternProcessor extends
		SetPatternProcessorForSequences {


	
	SequencePairTrie lastTrie;
	//for current transaction
	private int currentTransaction;//its ID
		//node, parent, start, end information
//	List<SequencePairTrieNode>currentNodes;
	List<SequencePairTrieNode>currentParents;
//	List<Integer>currentStarts;
//	List<Integer>currentEnds;
		//for shorter matches: node, parent, start, end information
//	List<SequencePairTrieNode>currentShorterNodes;
//	List<SequencePairTrieNode>currentShorterParents;
//	List<Integer>currentShorterStarts;
//	List<Integer>currentShorterEnds;
	
	//the new nodes for pruning (along with their parents, to enable pruning)
	//HashMap<SequencePairTrieNode,SequencePairTrieNode>newNodes;
	HashSet<SequencePairTrieNode>pairNodes = new HashSet<SequencePairTrieNode>();
	private int minSupport;
	List<Pattern>iterationTwoPatterns;
	HashMap<Integer, SequencePairTrieNode> l1Nodes;
	private int hashWidth;
	
	/**
	 * Notify the counting datastructure that a new transaction is processed.
	 * @param transactionID
	 */
	public void startTransaction(int transactionID){
		currentTransaction = transactionID;
//		currentNodes = new ArrayList<SequencePairTrieNode>();
		currentParents = new ArrayList<SequencePairTrieNode>();
//		currentStarts = new ArrayList<Integer>();
//		currentEnds = new ArrayList<Integer>();
//		currentShorterNodes = new ArrayList<SequencePairTrieNode>();
//		currentShorterParents = new ArrayList<SequencePairTrieNode>();
//		currentShorterStarts = new ArrayList<Integer>();
//		currentShorterEnds = new ArrayList<Integer>();
	}
	
//	public void newMatch(SequencePairTrieNode newNode, SequencePairTrieNode parentNode, int start, int end){
//		System.out.println("Transaction: "+currentTransaction+" matches: "+newNode.sequenceDenoted()+ "("+start+","+end+")");
//		//add node to new nodes
//		currentNodes.add(newNode);
//		currentParents.add(parentNode);
//		currentStarts.add(start);
//		currentEnds.add(end);
//		
//	}
//	public void newShorterMatch(SequencePairTrieNode newNode, SequencePairTrieNode parentNode, int start, int end){
//		System.out.println("Shorter match found: "+newNode.sequenceDenoted()+ "("+start+","+end+")");
//		currentShorterNodes.add(newNode);
//		currentShorterParents.add(parentNode);
//		currentShorterStarts.add(start);
//		currentShorterEnds.add(end);
//	}
	/**
	 * overridden to:
	 * - add support set info to nodes
	 * - notify endrefs about subsumed matches
	 * 
	 * @see com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.SetPatternProcessorForSequences#computeSupport(java.util.List, com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.Trie, int)
	 */
	public void computeSupport(List patterns, Trie candidateTrie,
			int depth) {
		//newNodes = new HashMap<SequencePairTrieNode,SequencePairTrieNode>();
		//NOTE: nothing is needed because the support is now counted during candidate generation 
		//if(depth > 2)
				return;
//		//TODO: the below can be optimized for short patterns.
//		//createCandidateTrie(patterns);
//		lastTrie = (SequencePairTrie)candidateTrie;
//		//do recursion, use other trie object
//		//bookkeeping and callback-methods for pair counts
//		// flush counts in candidateTrie
//		candidateTrie.flushCounts();
//		newNodes = new HashMap<SequencePairTrieNode,SequencePairTrieNode>();
//
//		List<TrieNode> children = candidateTrie.getRoot().getChildern();
//		// iterate over database
//		//logger.info("Children: "+children);
//		int transactionID = 0;
//		for (Object transactionO : db) {
//			startTransaction(transactionID);
//			// present each item
//			SequenceTransaction transaction = (SequenceTransaction) transactionO;
//			//logger.info(transaction);
//			//: we have to visit more here than in standard sequence mining in order to get the shorter partners
//			for (int i = 0; i < (transaction.getContent().size()); i++) {
//				//logger.info("newRun: "
//				//		+ ((LinkedList<Integer>) transaction.getContent())
//				//				.get(i));
//				for (TrieNode child : children) {
//					//logger.info(child.getLabel());
//					if (child.getLabel() == ((LinkedList<Integer>) transaction
//							.getContent()).get(i)) {
//						// proceed with (rest) transaction
//						List<Integer> newList = (List<Integer>) ((List<Integer>) transaction
//								.getContent()).subList(i + 1, transaction
//								.getContent().size());
//						//logger.info("PatternToTry: " + newList);
//						computeSupport(child, depth - 1, newList, i,i+depth-1,candidateTrie.getRoot());
//						break; 
//					}
//				}
//			}
//			transactionID++;
//			//doneTransaction(transactionID++);
//		}
//		// iterate over patterns
//		for (Object patternO : patterns) {
//			// get for each pattern the count out of the trie and assign it
//			SequencePattern pattern = (SequencePattern) patternO;
//			double support = (double) candidateTrie.getCount(pattern
//					.getContent());
//			//logger.info("Pattern: " + pattern + "\tSupport: " + support);
//			pattern.setSupport(support);
//		}
	}
	@SuppressWarnings("unused")
	private void computeSupport(TrieNode child, int depth, List<Integer> transaction, int start, int end, TrieNode parent) {
		
//			logger.info("iteration: " + iteration++);
//			logger.info("recursive computeSupport!" + "\tTransaction: "
//					+ transaction + "\tdepth: " + depth);
		if (depth == 0) {
			// increase support by one
			child.setCount(child.getCount() + 1);
			//update support set 
			//remove sequence from parent's support
			int transactionID = currentTransaction;
			((SequencePairTrieNode)parent).removeSupport(new IntPair(transactionID,start));
			//add sequence to this support
			((SequencePairTrieNode)child).addSupport(new IntPair(transactionID,start));
			((SequencePairTrieNode)child.getEndReference()).addEndRefSubsumed(new IntPair(transactionID,start+1));
		} else {
			for (int i = 0; i <= Math.min(kDistance - 1, transaction.size()
					- depth+1); i++) {
				List<TrieNode> childs = child.getChildern();
				if (childs != null && transaction.size() > 0) {
					//logger.info("run: " + transaction.get(i));
					for (TrieNode childOfChild : childs) {
						//logger.info("childOfChild: " + childOfChild.getLabel());
						if (childOfChild.getLabel() == transaction.get(i)) {
							// proceed with rest transaction
							List<Integer> newList = (List<Integer>) transaction
									.subList(i + 1, transaction.size());
							//logger.info("PatternToTryInRec: " + newList);
							computeSupport(childOfChild, depth - 1, newList, start, end, child);
							break;
						}
					}
				}
			}
		}
	}

	public Trie pruneMinCountLeafs(int layerOfLeafsToPrune, Trie candidateTrie,
			double minSupport) {
		lastTrie = (SequencePairTrie)candidateTrie;
		return lastTrie;
		//run standard pruning 
//		Trie result = super.pruneMinCountLeafs(layerOfLeafsToPrune, candidateTrie, minSupport);
//		
//		//run pair-pruning
//		for (SequencePairTrieNode newNode : newNodes.keySet()) {
//			if(!hasPairSupport(newNode, minSupport)){
//				SequencePairTrieNode parent = newNodes.get(newNode);
//				//prune newNode from parent
//				parent.removeChild(newNode.getLabel());
//				System.out.println("pruning "+newNode);
//			}else{
//				pairNodes.add(newNode);
//			}
//		}
//		//debug output
//		//getFrequentMatchPairs((SequencePairTrie)candidateTrie);
////		for (FrequentMatchPair frequentMatchPair : pairs) {
////			System.out.println(frequentMatchPair);
////		}
//		return result;
	}
	
	/**
	 * Notify the counting datastructure that all matches of a transaction have been found.
	 * At this point, the set of matching pairs is updated. Subsumed matches are retracted. 
	 * @param transactionID
	 */

//	public void doneTransaction(int transactionID){
//		System.out.println("Done Transaction: "+transactionID);
//		for (int i = 0; i < currentNodes.size(); i++) {
//			SequencePairTrieNode iNode = currentNodes.get(i);
//			System.out.println("\tNode: "+iNode.sequenceDenoted());
//			int iStart = currentStarts.get(i);
//			int iEnd = currentEnds.get(i);
//			SequencePairTrieNode iParent = currentParents.get(i);
//			newNodes.put(iNode,iParent);
//			//overlap check
//			Collection<SequencePairTrieNode> iNodePartners = new LinkedList<SequencePairTrieNode>();
//			//get the shorter nodes matching in the same transaction and consider them for partners
//			//shorter ones need to be added first because they me be retracted by longer ones.
//			for (int j = 0; j < currentShorterNodes.size(); j++) {
//				SequencePairTrieNode jNode = currentShorterNodes.get(j);
//				int jStart = currentShorterStarts.get(j);
//				int jEnd = currentShorterEnds.get(j);
//				if(
//						(jStart>=iStart&&jStart<=iEnd)||
//						(jEnd>=iStart&&jEnd<=iEnd)){
//					continue;
//				}
//				iNodePartners.add(jNode);
//			}
//			if(iNodePartners.size()>0){
//				//remove pair from parent node
//				iParent.retractMatch(transactionID,iStart);
//				iParent.retractMatch(transactionID,iStart+1); //shorter matches may appear at 2 places in the sequence (repititions of the same entry)
//				iNode.retractMatch(transactionID,iStart);
//				//retract from all matches of short sequences that occurr
//				for (int j = 0; j < currentShorterNodes.size(); j++) {
//					SequencePairTrieNode jNode = currentShorterNodes.get(j);
//					int jStart = currentShorterStarts.get(j);
//					int jEnd = currentShorterEnds.get(j);
//					if(
//							(jStart>=iStart&&jStart<=iEnd)&&
//							(jEnd>=iStart&&jEnd<=iEnd)){
//						jNode.retractMatch(transactionID, jStart);
//					}
//				}
//				//add partnerships to nodes
//					//unless a longer one has been found in the same iteration
//				iParent.retractMatch(transactionID, iStart);
//				
//				iNode.addMatch(transactionID, iStart, iNodePartners);
//			}
//			iNodePartners = new LinkedList<SequencePairTrieNode>();
//			//partners of the same length
//			for (int j = 0; j < currentNodes.size(); j++) {
//				if(j==i) continue; //only consider each pair once
//				SequencePairTrieNode jNode = currentNodes.get(j);
//				int jStart = currentStarts.get(j);
//				int jEnd = currentEnds.get(j);
//				//drop all pairs of shorter sequences where one is in the range of the i node and one in the range of the j node
//				//retract from all matches of short sequences that occurr
////				for (int k = 0; k < currentShorterNodes.size(); k++) {
////					SequencePairTrieNode kNode = currentShorterNodes.get(k);
////					int kStart = currentShorterStarts.get(k);
////					int kEnd = currentShorterEnds.get(k);
////					for (int l = 0; l < currentShorterNodes.size(); l++) {
////						SequencePairTrieNode lNode = currentShorterNodes.get(l);
////						int lStart = currentShorterStarts.get(l);
////						int lEnd = currentShorterEnds.get(l);
////
////						if(
////								(kStart>=iStart&&kStart<=iEnd)&&
////								(kEnd>=iStart&&kEnd<=iEnd)&&
////								(lStart>=jStart&&lStart<=jEnd)&&
////								(lEnd>=jStart&&lEnd<=jEnd))
////						{
////							System.out.println("k-node:"+kNode+":"+kStart);
////							System.out.println("l-node:"+lNode+":"+lStart);
////							kNode.retractMatch(transactionID, kStart);
////							lNode.retractMatch(transactionID, lStart);
////						}
////					}
////				}
//				if(
//						(jStart>=iStart&&jStart<=iEnd)||
//						(jEnd>=iStart&&jEnd<=iEnd)){
//					continue;
//				}
//				iNodePartners.add(jNode);
//
//			}
//
//			
//			if(iNodePartners.size()>0){
//				//remove pair from parent node
//				iParent.retractMatch(transactionID,iStart);
//				//iParent.retractMatch(transactionID,iStart+1); //shorter matches may appear at 2 places in the sequence
//				//iNode.retractMatch(transactionID,iStart);
//				//iNode.retractMatch(transactionID,iStart);
//				//add partnerships to nodes
//					//unless a longer one has been found in the same iteration
//				iNode.addMatch(transactionID, iStart, iNodePartners);
//			}
//		}
//	}
	/**
	 * Check if a given node should be pruned because it doesn't contribute to 
	 * sufficiently many pairs. 
	 * 
	 * @param node
	 * @return
	 */
	
	public boolean hasPairSupport(SequencePairTrieNode node, double minSupport){
		//look up partners
		//count them
		return (node.maxPartnerSupport()>=minSupport);
	}
	
	/**
	 * Serialize Trie to database in DFS-manner
	 * 
	 * Schema: frequent sequence string, frequent sequence hash, supporting transaction (TODO: ID or value?), matchStart, matchEnd, unsubsumed (0/1)
	 * @param trie
	 * @return
	 */
	//   
	static final String PAIR_OUTPUT_TEMPLATE = "\"%s\",%d,%d,%d,%d,%d\n";
	public void getFrequentMatchPairs(SequencePairTrie trie){
		//HashMap<Integer, FrequentMatchPair> result = new HashMap<Integer,FrequentMatchPair>();
		File outputFile = new File("getFrequentMatchPairs.csv");
		
		// recurr and fill result
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile),
					GroupByNewProcessor.ENCODING));
			getFrequentMatchPairsR((SequencePairTrieNode)trie.getRoot(),bw,new LinkedList<Integer>());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//TODO: other output?
	}
	public Collection<IntPair> getFrequentMatchPairsR(SequencePairTrieNode node, Writer writer, List<Integer>sequenceAbove ) throws IOException{
		List<Integer>sequenceAboveChild = new LinkedList<Integer>(sequenceAbove);
		sequenceAboveChild.add(node.getLabel());
		if(node.toString().equals("[393, 721, 238]")){
			System.out.println("here we are");
		}
		//recurr
		Collection<IntPair> supportBelow = new HashSet<IntPair>();
		for (TrieNode child : node.getChildern()) {
			supportBelow.addAll(
					getFrequentMatchPairsR((SequencePairTrieNode)child,writer,sequenceAboveChild)
					);
			
		}
		//subsumed by children
		for (IntPair supportItem : supportBelow) {
			if(sequenceAbove.size()==0)continue;
			writePairOutputLine(writer, sequenceAboveChild, supportItem.x, supportItem.y, supportItem.y+node.getDepth(), true);
		}
		
		//subsumed by endrefs
		if(node.endRefSubsumed!=null){
			for (IntPair supportItem : node.endRefSubsumed) {
				if(sequenceAbove.size()==0)continue;
				if(supportBelow.add(supportItem)){
					writePairOutputLine(writer, sequenceAboveChild, supportItem.x, supportItem.y, supportItem.y+node.getDepth(), true);
				}
			}
		}
		//unsubsumed
		if(node.support!=null&&sequenceAbove.size()>0){
			for (IntPair supportItem : node.support) {
				boolean isSubsumed = !((node.endRefSubsumed==null)||!node.endRefSubsumed.contains(supportItem));
				if(supportBelow.add(supportItem)){
					writePairOutputLine(writer, sequenceAboveChild, supportItem.x, supportItem.y, supportItem.y+node.getDepth(), isSubsumed);
				}
			}
		}
		//returns the support at node and its descendants
		return supportBelow;
	}
	
	private void writePairOutputLine(Writer writer, List<Integer> sequence, int transaction, int start, int end, boolean subsumed) throws IOException{
		String sequenceString = "";
		boolean first = true;
		for (int item : sequence) {
			if(first){first=false;continue;} //skip first entry = root
			if(sequenceString.length()>0) sequenceString+="-";
			sequenceString+=item;
		}

		int hash = 0;
		for (int item : sequence) {
			hash = hash*1023 + item;
		}
		if(sequenceString.equals("393-721-238")){
			System.out.println(String.format(PAIR_OUTPUT_TEMPLATE, 
					sequenceString, hash, transaction,start,end,subsumed?0:1
			));
		}
		writer.write(String.format(PAIR_OUTPUT_TEMPLATE, 
				sequenceString, hash, transaction,start,end,subsumed?0:1
		));
	}
	public List generateCandidatesForTheNextLevel(Trie candidateTrie,
			int processintDepth) {
		logger.info("generating candidates at processing depth "+processintDepth);
		HashSet resultSet = new HashSet();
		resultSet.addAll(generateCandidatesForTheNextLevel(
				(DefaultTrieNode) candidateTrie.getRoot(),
				new LinkedList<Integer>(), 0, processintDepth));
		//newNodes = new HashMap<SequencePairTrieNode, SequencePairTrieNode>();
		return new LinkedList<Pattern>(resultSet);
	}
	/**
	 * ATTENTION: candidateTree have to be at least of depth 1!
	 * 
	 * TODO: Overridden to only generate candidates with a non-empty support set based on the back references
	 * @param prefixTreeContainingAllFrequentSetsUpToLevelD
	 * @param totalDeptOfInputTree
	 * @return
	 */
	static int setIntersections = 0;
	static int supportedFound = 0;
	public List<Pattern> generateCandidatesForTheNextLevel(
			DefaultTrieNode currentNode, List<Integer> patternOfCurrentNode,
			int actualDepth, int processintDepth) {
//		logger.info("currentNode: " + currentNode);
//		logger.info("patternOfCurrentNode: " + patternOfCurrentNode);
//		logger.info("actualDepth: " + actualDepth);
//		logger.info("processintDepth: " + processintDepth);
		LinkedList<Pattern> result = new LinkedList<Pattern>();

		// for all nodes one layer above the leaves (which represent sequence
		// X).
		LinkedList<TrieNode> children = currentNode.getChildern();

		//Sebastian: The following is a modification in order to enable working with the "paired" sequence mining.
		//The below conditions check, if the recursion has reached the pre-leaf level and then processes 
		//the end references of the children. The paired miner keeps the trie over iterations so this check needs to be done differently
		//only targeting leafs of processingDepth-1
		if (actualDepth == processintDepth) //recursion is too deep. Return empty list
			return new LinkedList<Pattern>();
		
		if (children != null && children.size() != 0
				&& children.getFirst().isLeaf() &&actualDepth == processintDepth - 1) {
			// for x elem X (we only have a part of elem X here, defined by
			// parent node!)
			for (TrieNode nodeX : children) {

				// nodeX is last node of end(x)
				// y := end(x)
				// nodeY is last node of start(x_2,...,x_n-1)
				TrieNode nodeY = nodeX.getEndReference();
				//TODO: if there is no joint support between nodeX and nodeY, we don't need to consider any of the children of y
					//TODO: is this optimization actually possible given that the support is always pushed to the children?
				
				List<TrieNode> childrenOfY = nodeY.getChildern();
//				logger.info("NodeX: " + nodeX);
//				logger.info("NodeY: " + nodeY);
//				logger.info("ChildrenOfY:_" + childrenOfY);
				Collection<IntPair> nodeXSupport = ((SequencePairTrieNode)nodeX).getSupport();
				for (TrieNode childY : childrenOfY) {
					if(setIntersections%100000==0)logger.info("set intersections: "+setIntersections);
					if(supportedFound%10000==0)logger.info("supportedFound: "+supportedFound);
					if(supportedFound%10000==0)logger.info("depth "+actualDepth);
					// no existence check here !!!
					// last step of countSupportValues results in a removal of
					// all trieNodes with support == 0 (of course only leave
					// nodes)
					//check support here
					
					Collection<IntPair> childYSupport = ((SequencePairTrieNode)childY).getSupport();
					if(childYSupport==null || nodeXSupport == null) 
						continue;
					int supportCount = 0;
					for (IntPair ySupportItem : childYSupport) {
						IntPair xSupportItem = new IntPair(ySupportItem.x,ySupportItem.y-1);
						if(nodeXSupport.contains(xSupportItem)){
							supportCount++;
						}
					}
					setIntersections++;
					if(supportCount<minSupport)
						continue;
					//lazy-create this (only if the support count is sufficiently high!)
					HashSet<IntPair> jointSupport = new HashSet<IntPair>();
					HashSet<IntPair> subsumedByJointSupport = new HashSet<IntPair>();
					for (IntPair ySupportItem : childYSupport) {
						IntPair xSupportItem = new IntPair(ySupportItem.x,ySupportItem.y-1);
						if(nodeXSupport.contains(xSupportItem)){
							jointSupport.add(xSupportItem);
							subsumedByJointSupport.add(ySupportItem);
						}
					}
					supportedFound++;
					int label = childY.getLabel();
					//TODO: shouldn't part be always of length 2?
					//TODO: cant we replace the below by an addNode?
					LinkedList<Integer> part = new LinkedList<Integer>();
					part.add(nodeX.getLabel());
					part.add(childY.getLabel());
					//logger.info("part: " + part);
					LinkedList<Integer> patternToTest = new LinkedList<Integer>();
					patternToTest.addAll(patternOfCurrentNode);
					patternToTest.add(currentNode.getLabel());
					patternToTest.addAll(part);
					patternToTest.remove();
					//logger.info("patternToTest: " + patternToTest);
					//db.contains(patternToTest);

					//logger.info("created pattern: " + patternToTest);
					//SequencePairTrieNode child = (SequencePairTrieNode)nodeX.present(part);
					
					SequencePairTrieNode child = new SequencePairTrieNode(label,(SequencePairTrieNode)nodeX,hashWidth,actualDepth+2);
					//hook under root
					((SequencePairTrieNode)nodeX).addChild(child);
					//set support
					child.setCount(supportCount);
					//TODO: implement HashBuckets with hashset so that the below is not a copy
					child.setSupport(jointSupport);
					l1Nodes.put(label, child);
					//add the child as a new node
					
					//newNodes.put(label,(SequencePairTrieNode)nodeX);
					//TODO: note the subsumed support
					if(((SequencePairTrieNode)nodeY).endRefSubsumed== null){
						((SequencePairTrieNode)nodeY).endRefSubsumed = new HashSet<IntPair>();
					}
					((SequencePairTrieNode)nodeY).endRefSubsumed.addAll(subsumedByJointSupport);
					if(((SequencePairTrieNode)childY).endRefSubsumed== null){
						((SequencePairTrieNode)childY).endRefSubsumed = new HashSet<IntPair>();
					}
					((SequencePairTrieNode)childY).endRefSubsumed.addAll(subsumedByJointSupport);
					
					//System.out.println("new node: "+child);
					//set child's support 
					child.setCount(jointSupport.size());
					HashSet<IntPair> newChildSupport = child.getSupport();
					if(newChildSupport == null){
						newChildSupport = new HashSet<IntPair>();
						child.setSupport(newChildSupport);
					}
					newChildSupport.addAll(jointSupport);
					Pattern newPattern = new SequencePattern(patternToTest);
					newPattern.setSupport(jointSupport.size());
					result.add(newPattern);
					if(result.size()%1000==0)logger.info(result.size()+" results under "+nodeX);
					child.setEndReference(childY);

				}
			}

		} else {
			// recurr on children

			LinkedList<Integer> newPatternOfCurrentNode = new LinkedList<Integer>(
					patternOfCurrentNode);
			newPatternOfCurrentNode.add(currentNode.getLabel());
			for (TrieNode child : children) {
//				logger.info("from recurr on children: "
//						+ currentNode.getLabel() + "\t" + child + "\t"
//						+ newPatternOfCurrentNode);
				result.addAll(generateCandidatesForTheNextLevel(
						(DefaultTrieNode) child, newPatternOfCurrentNode,
						actualDepth + 1, processintDepth));
				if(result.size()%1000==0&&result.size()>0)logger.info(result.size()+" results");
			}
		}
		// recurr on siblings
		if (((DefaultTrieNode) currentNode).sibling != null) {
//			logger.info("from recurr on siblings: " + currentNode.getLabel()
//					+ "\t" + currentNode.sibling + "\t" + patternOfCurrentNode);
			result.addAll(generateCandidatesForTheNextLevel(
					currentNode.sibling, patternOfCurrentNode, actualDepth - 1,
					processintDepth));
		}
		return result;
	}

//	public void getFrequentMatchPairsR(SequencePairTrieNode node, HashMap<Integer, FrequentMatchPair> result){
//		//create pairs
//		if(node.transaction2partners!=null){
//			for (Point partnerTransaction : node.transaction2partners.keySet()) {
//				for(SequencePairTrieNode partner : node.transaction2partners.get(partnerTransaction)){
//					int nodeHashCode = node.hashCode();
//					int partnerHashCode = partner.hashCode();
//					if(nodeHashCode>partnerHashCode)continue; //to avoid double adding
//					int pairCode = nodeHashCode+partnerHashCode;
//					FrequentMatchPair matchPair = result.get(pairCode);
////					if((node.getLabel()==3&&partner.getLabel()==6)||(node.getLabel()==6&&partner.getLabel()==3))
////						System.out.println("pairs, "+pairCode + ", "+node.sequenceDenoted()+"," +partner.sequenceDenoted()+","+partnerTransaction);
//					if(matchPair==null){
//						matchPair = new FrequentMatchPair(node.getCount(),partner.getCount(),node,partner,node.sequenceDenoted(),partner.sequenceDenoted());
//						result.put(pairCode, matchPair);
//					}
//					
//					matchPair.addPartnerTransaction(partnerTransaction.x, 1, 1);//do we need the position info at that point?
//				}
//			}
//		}
//
//		//recurr on children
//		List<TrieNode> children = node.getChildern();
//		if (children != null) {
//			for (TrieNode child : children) {
//				getFrequentMatchPairsR((SequencePairTrieNode)child,result);
//			}
//		}
//	}
	////////Trie creations with new type
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unika.aifb.fpm.apriori.PatternProcessor#createCandidateTrie(java.util.List)
	 */
	public Trie createCandidateTrie(List patterns) {
//		Trie result = new SequencePairTrie((int) Math.round(Math.min(Math.sqrt(db
//				.getMaxItemNumber()), MAX_HASH_WIDTH)), this);
		Trie result = lastTrie;
		return createCandidateTrie(patterns, result);
	}

	public List iterationOne(List candidates, int minSupport) {
		this.minSupport = minSupport;
		//TODO: construct two datastructures
		//token -> supportitems
		HashBuckets<Integer, IntPair> singleItemSupport = new  HashBuckets<Integer, IntPair>(); 
		//TODO: iterate over the DB
		int transactionID = 0;
		for (Object transactionO : db) {
			SequenceTransaction transaction = (SequenceTransaction) transactionO;
			int pos = 0;
			for (Integer token: transaction.getContent()) {
				IntPair l1SupportItem = new IntPair(transactionID,pos);
				singleItemSupport.put(token, l1SupportItem);
				
				pos++;
			}
			transactionID++;
			if(transactionID% 10000==0)logger.info("iteration 1: "+transactionID+" transactions counted");
		}
		logger.info("iteration 1: done counting");
		hashWidth = (int) Math.round(Math.min(Math.sqrt(db.getMaxItemNumber()), MAX_HASH_WIDTH));
		//int hashWidth = 1000;
		System.gc();
		logger.info("total depth 1 items: "+singleItemSupport.keySet().size());
		List<Pattern>result = new ArrayList<Pattern>(singleItemSupport.keySet().size());
		//create root
		//TODO: //lastTrie = new SequencePairTrie(hashWidth, this); HACK (COMMENTED OUT!)
		SequencePairTrieNode root = (SequencePairTrieNode)lastTrie.getRoot();
		
		l1Nodes = new HashMap<Integer, SequencePairTrieNode>();
		//delete infrequent entries
		for (Iterator<Integer> iterator = singleItemSupport.keySet().iterator(); iterator.hasNext();) {
			Integer singleItem = (Integer) iterator.next();
			Collection<IntPair> support = singleItemSupport.get(singleItem);
			int supportSize = support.size();
			if(supportSize<minSupport){
				iterator.remove();
			}
		}
		logger.info("frequent depth 1 items: "+singleItemSupport.keySet().size());
		System.gc();
			//create length one nodes and create a hashmap to access them
		for (Integer singleItem : singleItemSupport.keySet()) {
			Collection<IntPair> support = singleItemSupport.get(singleItem);
			int supportSize = support.size();
			//create node
			int label = singleItem.intValue();
			SequencePairTrieNode newL1Node = new SequencePairTrieNode(label,root,hashWidth,1);
			//hook under root
			root.addChild(newL1Node);
			//set support
			newL1Node.setCount(supportSize);
			//TODO: implement HashBuckets with hashset so that the below is not a copy
			newL1Node.setSupport(new HashSet<IntPair>(support));
			l1Nodes.put(label, newL1Node);
			// create pattern
			SequencePattern pattern = new SequencePattern(Arrays.asList(new Integer[]{label}));
			result.add(pattern);
		}
		logger.info("done depth 1");
		// create length two nodes 
		System.gc();
		return result;
	}
	public List iterationTwo(List candidates, int minSupport) {
		//(lastToken,token)->supportitems
		HashBuckets<IntPair, IntPair> doubleItemSupport = new  HashBuckets<IntPair, IntPair>();
		System.gc();
		int transactionID = 0;
		//TODO: to save space in the doubleItemSupport we can only focus on parts of l1Nodes as token (not lastToken) at a time.
		
		for (Object transactionO : db) {
			SequenceTransaction transaction = (SequenceTransaction) transactionO;
			int lastToken = -1;
			boolean lastTokenFrequent = false;
			int pos = 0;
			for (Integer token: transaction.getContent()) {
				IntPair l2SupportItem = new IntPair(transactionID,pos-1);
				//TODO: check if individual items were frequent
				boolean tokenFrequent = l1Nodes.containsKey(token);
				if(lastTokenFrequent&&tokenFrequent){
					IntPair tokenPair = new IntPair(lastToken,token);
					doubleItemSupport.put(tokenPair, l2SupportItem);
				}
				lastToken = token;
				lastTokenFrequent = tokenFrequent;
				pos++;
			}
			transactionID++;
			if(transactionID% 10000==0)logger.info("iteration 2: "+transactionID+" transactions counted");
		}
		iterationTwoPatterns = new ArrayList<Pattern>(doubleItemSupport.keySet().size());

		logger.info("total depth 2 items: "+doubleItemSupport.keySet().size());
		//delete infrequent entries
		for (Iterator<IntPair> iterator = doubleItemSupport.keySet().iterator(); iterator.hasNext();) {
			IntPair itemPair = (IntPair) iterator.next();
			Collection<IntPair> support = doubleItemSupport.get(itemPair);
			int supportSize = support.size();
			if(supportSize<minSupport){
				iterator.remove();
			}
		}
		
		logger.info("frequent depth 2 items: "+doubleItemSupport.keySet().size());
		System.gc();
		//construct trie-content for frequent pairs
		
		for (IntPair itemPair : doubleItemSupport.keySet()) {
			Collection<IntPair> support = doubleItemSupport.get(itemPair);
			int supportSize = support.size();
			int label = itemPair.y;
			//create node
			SequencePairTrieNode parent = l1Nodes.get(itemPair.x);
			SequencePairTrieNode newL2Node = new SequencePairTrieNode(label,parent,parent.getHashWidth(),2);
			//hook under parent
			parent.addChild(newL2Node);
			//set support
			newL2Node.setCount(supportSize);
			//TODO: implement HashBuckets with hashset so that the below is not a copy
			newL2Node.setSupport(new HashSet<IntPair>(support));
			// put backrefs in place
			SequencePairTrieNode endRef = l1Nodes.get(itemPair.y);
			newL2Node.setEndReference(endRef);
			// take care of subsumption
			if(parent.support==null) parent.support = new HashSet<IntPair>();
			parent.getSupport().removeAll(support);
			if(endRef.endRefSubsumed==null) endRef.endRefSubsumed = new HashSet<IntPair>();
			for (IntPair supportItem : support) {
				endRef.addEndRefSubsumed(new IntPair(supportItem.x,supportItem.y+1));
			}
			SequencePattern pattern = new SequencePattern(Arrays.asList(new Integer[]{itemPair.x,itemPair.y}));
			iterationTwoPatterns.add(pattern);
		}
		l1Nodes.clear();
		System.gc();
		return iterationTwoPatterns;
	}
	public Trie createCandidateTrieAfterIterationOne(List patterns) {
		//Note: it is all taken care of in  createCandidateTrieAfterIterationTwo
//		Trie result = new SequencePairTrie((int) Math.round(Math.min(Math.sqrt(db
//				.getMaxItemNumber()), MAX_HASH_WIDTH)), this);
//		lastTrie = (SequencePairTrie)result;
//		return createCandidateTrieAfterIterationOne(patterns, result);
		return lastTrie;
	}
	public Trie createCandidateTrieAfterIterationTwo(List patterns) {

		return lastTrie;
	}
//	public Trie createCandidateTrieAfterIterationTwo(List patterns) {
//		Trie result = lastTrie;
////		Trie result = new SequencePairTrie((int) Math.round(Math.min(Math.sqrt(db
////				.getMaxItemNumber()), MAX_HASH_WIDTH)), this);
//		result = createCandidateTrieAfterIterationTwo(patterns, result);
//		//somehow the following is missing in the apriori implementation.
//		computeSupport(patterns,result,2);
//		return result;
//	}

	public class FrequentMatchPair{
		public int nodeXSupport;
		public int nodeYSupport;
		public SequencePairTrieNode nodeX;
		public SequencePairTrieNode nodeY;
		public List<Integer> sequenceX;
		public List<Integer> sequenceY;
		public List<Integer> coOccurrenceTransactions = new LinkedList<Integer>();
		public List<Integer> coOccurrenceSeqXStarts = new LinkedList<Integer>();
		public List<Integer> coOccurrenceSeqYStarts = new LinkedList<Integer>();
		
		public FrequentMatchPair(
				int nodeXSupport,
				int nodeYSupport,
				SequencePairTrieNode nodeX,
				SequencePairTrieNode nodeY,
				List<Integer> sequenceX,
				List<Integer> sequenceY
		){
			this.nodeXSupport = nodeXSupport;
			this.nodeYSupport = nodeYSupport;
			this.nodeX = nodeX;
			this.nodeY = nodeY;
			this.sequenceX = sequenceX;
			this.sequenceY = sequenceY;
			
		}
		public void addPartnerTransaction(int transaction, int xStart, int yStart){
			this.coOccurrenceTransactions.add(transaction); 
			this.coOccurrenceSeqXStarts.add(xStart);
			this.coOccurrenceSeqYStarts.add(yStart);
		}
		public int hashCode(){
			return (nodeXSupport+nodeYSupport)*1023+(nodeX.hashCode()+nodeY.hashCode())*2047+
			(sequenceX.hashCode()+sequenceY.hashCode())*4095+coOccurrenceTransactions.hashCode()*8191+
			(coOccurrenceSeqXStarts.hashCode()+coOccurrenceSeqYStarts.hashCode())*16393;
		}
		public boolean equals(Object o){
			return (o.hashCode()==this.hashCode());
		}
		public String toString(){
			return "FMP: ("+sequenceX+","+sequenceY+") c="+coOccurrenceTransactions;
		}
	}
//	private class NodeMatch{
//		public int start;
//		public int end;
//		public int transaction;
//		public SequencePairTrieNode node;
//		public NodeMatch(SequencePairTrieNode node, int transaction, int start, int end ){
//			this.start = start;
//			this.end = end;
//			this.transaction = transaction;
//			this.node = node;
//		}
//		public boolean equals(Object o){
//			if(!(o instanceof NodeMatch)) return false;
//			NodeMatch nm = (NodeMatch) o;
//			if(nm.start != this.start)return false;
//			if(nm.end != this.end)return false;
//			if(nm.transaction != this.transaction)return false;
//			if(this.node.equals(nm.node())) return false;
//			return true;
//		}
//		public int hashCode(){
//			return 1023 * start+ 2045 *end+4091*transaction+8093*node.hashCode(); 
//		}
//	}
//	private class 

	public int getCurrentTransaction() {
		return currentTransaction;
	}

	public SequencePairTrie getLastTrie() {
		return lastTrie;
	}
}
