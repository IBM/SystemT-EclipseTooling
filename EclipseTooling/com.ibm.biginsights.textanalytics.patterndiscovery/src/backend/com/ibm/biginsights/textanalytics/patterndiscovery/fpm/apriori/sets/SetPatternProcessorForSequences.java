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
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sets;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.Database;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.Pattern;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.PatternProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.DefaultTrie;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.DefaultTrieNode;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.Trie;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.TrieNode;
import com.ibm.biginsights.textanalytics.patterndiscovery.tools.SingletonList;


/**
 * Implements basic pattern operations for a scenario where transactions 
 * are modelled as sequences. In particular: <ul>
*   <li>The alphabet is a subset of the integers. Specified by a max integer</li>
*   <li>The initial candidates are sets containing exactly one integer.</li>*      
* </ul>
 * 
 *  Kleb (kleb@fzi.de)
 *
 */
@SuppressWarnings("unchecked")
public class SetPatternProcessorForSequences implements PatternProcessor {
	
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
      "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
   
	public static IPDLog logger = PDLogger.getLogger(SetPatternProcessorForSequences.class.getName()); 
	
	private static final double MAX_HASH_WIDTH = 40;
	//private static final double MIN_HASH_WIDTH = 20;
	Database db;

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.PatternProcessor#computeSupport(java.util.List, edu.unika.aifb.fpm.trie.Trie)
	 */
	public void computeSupport(List patterns, Trie candidateTrie, int patternLength) {
		//flush counts in candidateTrie
		candidateTrie.flushCounts();
		//iterate over database
		for (Object transactionO : db) {
			//present each item
			SetTransaction transaction = (SetTransaction) transactionO;
			candidateTrie.presentIncontinuous(transaction.getContent(), patternLength);
		}
		//iterate over patterns
		for (Object patternO : patterns) {
			//get for each pattern the count out of the trie and assign it
			SetPattern pattern = (SetPattern)patternO;
			double support = (double) candidateTrie.getCount(pattern.getContent());
			pattern.setSupport(support);
		}
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.PatternProcessor#createCandidateTrie(java.util.List)
	 */
	public Trie createCandidateTrie(List patterns) {
		Trie result = new DefaultTrie((int)Math.round(Math.min(Math.sqrt(db.getMaxItemNumber()),MAX_HASH_WIDTH)));
		//Trie result = new DefaultTrie(7);
		for (Object patternO : patterns) {
			SetPattern pattern = (SetPattern) patternO;
			result.present(pattern.content);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.PatternProcessor#generateInitialCandidates()
	 */
	public List<Pattern> generateInitialCandidates() {
		List<Pattern> result = new ArrayList<Pattern>(db.getMaxItemNumber()+1);
		for(int i = 0; i <= db.getMaxItemNumber();i++){
			result.add(new SetPattern(new SingletonList<Integer>(new Integer(i))));
			//System.out.println(result.toString());
		}
		return result;
	}
	
	
	
	/**
	 * kDistance:
	 * Default value 1
	 */
	int kDistance = 1;
	
	public void setKDistance(int kDistance){
		this.kDistance = kDistance;
	}
	
	public int getKDistance() {
		return kDistance;
	}


	
	
	
	
	public List generateCandidatesForTheNextLevel(Trie candidateTrie) {
		HashSet resultSet = new HashSet();
		resultSet.addAll(generateCandidatesForTheNextLevel((DefaultTrieNode)candidateTrie.getRoot(),new LinkedList<Integer>()));
		return new LinkedList<Pattern>(resultSet);
	}
	
	/**
	 * ATTENTION:
	 * candidateTree have to be at least of depth 1!
	 * @param prefixTreeContainingAllFrequentSetsUpToLevelD
	 * @param totalDeptOfInputTree
	 * @return
	 */	
	public List<Pattern> generateCandidatesForTheNextLevel(DefaultTrieNode currentNode, List<Integer> patternOfCurrentNode) {
		LinkedList<Pattern> result = new LinkedList<Pattern>();

		// for all nodes one layer above the leaves (which represent sequence X). 
		LinkedList<TrieNode> children = currentNode.getChildern();
		
		// This line assumes that all leaves are at the same depth.
		if(children != null && children.size()!=0 && children.getFirst().isLeaf()){
			
			// for x elem X (we only have a part of elem X here, defined by parent node!)		
			//for(TrieNode nodeX: children) {
				// nodeX is last node of end(x)
				// y := end(x)
				// nodeY is last node of start(x_2,...,x_n-1)
				//TrieNode nodeY = nodeX.getEndReference();			
				//List<TrieNode> childrenOfY = nodeY.getChildern();
//				for(TrieNode childY: childrenOfY) {
//					// no existence check here !!!
//					// last step of countSupportValues results in a removal of all trieNodes with support == 0 (of course only leave nodes)				
//					LinkedList<Integer> part = new LinkedList<Integer>();					
//					part.add(nodeX.getLabel());
//					part.add(nodeY.getLabel());
//					
//					// kDistance = 1 is DEFAULT
////					if (kDistance == 1) {
//						LinkedList<Integer> patternToTest = new LinkedList<Integer>();
//						patternToTest.addAll(patternOfCurrentNode);
//						patternToTest.addAll(part);
//						db.contains(patternToTest);
//						//logger.info("created pattern: "+patternToTest);
//						TrieNode child = nodeX.present(part);
//						child.setEndReference(nodeY);
//					} else {
//						/** @TODO: IMPLEMENT! */
//						int sizeOfPatternOfCurrentNode = patternOfCurrentNode.size();
//						int sizeOfPatternOfCurrentNodeFirstPart = sizeOfPatternOfCurrentNode-kDistance;
//						LinkedList<Integer> patternToTestFirstPart = new LinkedList<Integer>();
//						for(int i=0; i<sizeOfPatternOfCurrentNodeFirstPart;i++) {							
//								patternToTestFirstPart.add(patternOfCurrentNode.get(i));
//						}
//						LinkedList<Integer> patternToTestLastPart = new LinkedList<Integer>();
//						patternToTestLastPart.add(patternOfCurrentNode.get(patternOfCurrentNode.size()-1));
//						part.add(nodeX.getLabel());
//						patternToTestLastPart.add(nodeY.getLabel());
						/*
						 * suche nach db Eintr�gen, die mit patternToTestFirstPart anfangen u nd mit patternToTestLastPart enden und dazwischen kDistance Elemente haben!  
						 */
//					}
					
	//			}
			//}
			
		}else{
			//recurr on children 
			LinkedList<Integer> newPatternOfCurrentNode = new LinkedList<Integer>(patternOfCurrentNode);
			newPatternOfCurrentNode.add(currentNode.getLabel());
			for (TrieNode child : children) {
				result.addAll(
						generateCandidatesForTheNextLevel((DefaultTrieNode)child, newPatternOfCurrentNode)
				);
			}
		}
		//recurr on siblings
		if(((DefaultTrieNode)currentNode).sibling != null){
			result.addAll(
					generateCandidatesForTheNextLevel(currentNode.sibling,patternOfCurrentNode)
			);
		}
		return result;
	}
	
//	public void (db)
	
	public Trie countSupportValues(Trie prefixTree) {
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/** TODO: the duplicate elemination done here should not be neccessary: Bug?? Judging from the time it occurrend, the bug seems to have to do with the sibling technology
	 * @see com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.PatternProcessor#merge(java.util.List, com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.Trie)
	 */
	public List merge(List inputPatterns, Trie candidateTrie, double minSupport) {
		HashSet resultSet = new HashSet();
		resultSet.addAll(mergeR(inputPatterns, candidateTrie, (DefaultTrieNode)candidateTrie.getRoot(),new LinkedList<Integer>()));
		return new LinkedList<Pattern>(resultSet);
	}
	
	private List mergeR(List inputPatternsR, Trie candidateTrie, DefaultTrieNode currentNode, List<Integer> above){
		LinkedList<Pattern> result = new LinkedList<Pattern>();

//		for all nodes one layer above the leaves (which represent sequence X). 
		LinkedList<TrieNode> children = currentNode.getChildern();
		if(children != null && children.size()!=0 && children.getFirst().isLeaf()){//	This line assumes that all leaves are at the same depth.
			//for all non-equal combinations (a,b)
			for (TrieNode nodeA : children) {
				for (TrieNode nodeB : children) {
					if(nodeA.getLabel()>=nodeB.getLabel()) continue; //always nodeA < nodeB
					//check if there exists a candidate node for each subsequence of <X, a, b> that skips exactly one element
					LinkedList<Integer> part1 = new LinkedList<Integer>();
					LinkedList<Integer> part2 = new LinkedList<Integer>(above);
					Integer skipped = null;
					boolean allSupported = true;
					while(! part2.isEmpty() && skipped == null){
						if(skipped != null){
							part1.addLast(skipped);
						}
						if(! part2.isEmpty()){
							skipped = part2.removeFirst();
						}else{
							skipped = null;
						}
						//concatenate part1, part2, nodeA.label and nodeB.label
						LinkedList<Integer> toPresent = new LinkedList<Integer>();
						toPresent.addAll(part1);
						toPresent.addAll(part2);
						toPresent.add(new Integer(nodeA.getLabel()));
						toPresent.add(new Integer(nodeB.getLabel()));
						if(candidateTrie.getCount(toPresent)==0){
							allSupported = false;
							break;
						}
					}
					if(allSupported){
						LinkedList<Integer> toReturn = new LinkedList<Integer>(above);
						//remove the root node and add the current node (if it's not the root)
						if(above.size()>0){
							toReturn.removeFirst();
							toReturn.add(new Integer(currentNode.getLabel()));
						}
						toReturn.add(new Integer(nodeA.getLabel()));
						toReturn.add(new Integer(nodeB.getLabel()));
						result.add(new SetPattern(toReturn));
					}
				}
			}
		}else{
			//recurr on children 
			LinkedList<Integer> newAbove = new LinkedList<Integer>(above);
			newAbove.add(currentNode.getLabel());
			for (TrieNode child : children) {
				result.addAll(
						mergeR(inputPatternsR,candidateTrie,(DefaultTrieNode)child,newAbove)
				);
			}
		}
		//recurr on siblings
		if(((DefaultTrieNode)currentNode).sibling != null){
			result.addAll(
				mergeR(inputPatternsR,candidateTrie,currentNode.sibling,above)
			);
		}
		return result;

	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.PatternProcessor#prune(java.util.List, edu.unika.aifb.fpm.trie.Trie)
	 */
	public List prune(List inputPatterns, Trie candidateTrie) {
		// in the above implementation of merge, pruning is included. Thus, we return (a copy of) the input
		return new LinkedList<Pattern>(inputPatterns);
	}

	public void setDatabase(Database db) {
		if (! (db instanceof SetDatabase)){
			if(Constants.DEBUG)
				System.err.println("Database has to be of type SetDatabase");
			return;
		}
		this.db = db;
	}

	/** This method bears an important optimization particular
	 * to things like text processing with a heavy-tailed item distribution:
	 * 
	 * (cf. Lars' Diss page 19)
	 * 
	 * The output is the set of frequent patterns of size one. An array 
	 * of HashSets is kept that carry the items found once, twice 
	 * etc. 
	 * 
	 * @see com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.PatternProcessor#iterationOne(java.util.List)
	 */
	public List iterationOne(List candidates, int minSupport) {
		HashSet<Integer>[] buckets =  new HashSet[minSupport];
		for(int bucketNr = 0;bucketNr<buckets.length;bucketNr++){
			buckets[bucketNr] = new HashSet<Integer>();
		}
		for (Object transactionO : db) {
			SetTransaction transaction = (SetTransaction)transactionO;
			for (int integer : transaction.getContent()) {
				boolean found = false;
				for(int bucketNr = 0;!found&&bucketNr<buckets.length-1;bucketNr++){
					if(buckets[bucketNr].contains(integer)){
						found = true;
						buckets[bucketNr+1].add(integer);
						buckets[bucketNr].remove(integer);
					}
				}
				if(!found)buckets[0].add(integer);
			}
		}		
		LinkedList<SetPattern> result = new LinkedList<SetPattern>();
		// Horror-Loop :-)
		for (int integer : buckets[minSupport-1]) {
			LinkedList<Integer> content = new LinkedList<Integer>();
			content.add(integer);
			result.add(new SetPattern(content));
		}
		return result;
	}
	
	public List iterationTwo(List candidates, int minSupport) {
		//counting datastructure: HashMap of 2D Points to counts
		HashMap<Point, Integer> matrix = new HashMap<Point,Integer>();
		//construct HashSet that allows checking if an integer is an iteration 2 candidate
		HashSet<Integer> presentInts= new HashSet<Integer>();
		for (Object candidate : candidates) {
			presentInts.add(((TreeSet<Integer>)((SetPattern)candidate).getContent()).first());
		}
		//iterate over all pairs of items in each transaction (smaller value first)
		for (Object transactionO : db) {
			SetTransaction transaction = (SetTransaction)transactionO;
			for (int x : transaction.getContent()) {
				for (int y : transaction.getContent()) {
					if(x<y){
						Point key = new Point(x,y);
						Integer count = matrix.get(key);
						//if not present in HashMap, add with count 1
						if(count==null){
							matrix.put(key, 1);
						}else{
							//otherwise increase count
							matrix.put(key, count.intValue()+1);
						}
					}
				}
			}
		}
		
		//iterate over keys in hashmap, output all pairs that exceed minSupport
		List<Pattern>result = new LinkedList<Pattern>();
		for (Point key : matrix.keySet()) {
			if(matrix.get(key).intValue()>=minSupport){
				List<Integer>pair = new LinkedList<Integer>();
				pair.add(key.x);
				pair.add(key.y);
				result.add(new SetPattern(pair));
			}
		}
		return result;
	}
//	public List iterationOne(List candidates, int minSupport) {
//		HashSet<Integer> used = new HashSet<Integer>();
//		for (Object transactionO : db) {
//			SetTransaction transaction = (SetTransaction)transactionO;
//			for (int integer : transaction.getContent()) {
//				used.add(integer);
//			}
//		}
//		LinkedList<SetPattern> result = new LinkedList<SetPattern>();
//		for (int integer : used) {
//			LinkedList<Integer> content = new LinkedList<Integer>();
//			content.add(integer);
//			result.add(new SetPattern(content));
//		}
//		return result;
//	}
	/**
	 takes the output of an apriori run and returns only those Patterns that are
	 * unique in the sense that they are not a shortened version of another pattern.
	 * This criterion is determined by a trie containing all patterns of all lengths.
	 * A pattern is returned if it is represented by a leaf node or by a node that has
	 * for which the sum of the supports of the children is by more than a given tolerance
	 * lower than that of the node. 
	 * 
	 * Note: the tolerance is currently ignored
		 */
	public static List cleanSubsumption(List[] input, double tolerance) {
		List<Pattern>result = new LinkedList<Pattern>();
		// von klein nach gro�. 
		for(int i = 2;i<input.length;i++){
			Trie patternTrie = new DefaultTrie(20);
			//Von alle patterns der gr��e n, alle varianten mit einer L�schung generieren.
			//diese in trie einz�hlen
			if(input[i]==null){
				if(input[i-1]!=null){
					result.addAll(input[i-1]);
				}
				continue;
			}
			for (Object patternO : input[i]) {
				SetPattern pattern = (SetPattern)patternO;
				for (Integer toDelete : pattern.getContent()) {
					List<Integer> thisSequence = new ArrayList<Integer>(pattern.getContent());
					thisSequence.remove(toDelete);
					patternTrie.present(thisSequence);
				}
			}
			//nur diejenigen patterns der l�nge n-1 behalten, die nicht im Trie enthalten sind
			for (Iterator iter = input[i-1].iterator(); iter.hasNext();) {
				SetPattern pattern = (SetPattern) iter.next();
				if(patternTrie.getCount(pattern.getContent())==0){
					result.add(pattern);
				}
			}
		}
		//von den gr��ten patterns alle behalten
		if(input[input.length-1]!=null){
			result.addAll(input[input.length-1]);
		}
		return result;
	}
//	public static List cleanSubsumption(List[] input, double tolerance) {
//		Trie patternTrie = new DefaultTrie(20);
//		List<Pattern>result = new LinkedList<Pattern>();
//		for (List<Pattern> list : input) {
//			if(list == null) continue;
//			for (Pattern pattern : list) {
//				SetPattern setPattern = (SetPattern)pattern;
//				patternTrie.present(setPattern.getContent());
//			}
//		}
//		for (TrieNode node : patternTrie.getRoot().getChildern()) {
//			collectSubsumptionR(node,new LinkedList<Integer>(),result,tolerance);
//		}
//		return result;
//		
//	}
//	private static void collectSubsumptionR(TrieNode node, List<Integer>above, List<Pattern>target, double tolerance){
//		List<Integer> newAbove = new LinkedList<Integer>();
//		newAbove.addAll(above);
//		newAbove.add(node.getLabel());
//		if(node.isLeaf()){
//			target.add(new SetPattern(newAbove));
//			return;
//		}
//		int supportBelow = 0;
//		for (TrieNode childNode : node.getChildern()) {
//			supportBelow += childNode.getCount();
//		}
//		if(node.getCount()- supportBelow>node.getCount()*tolerance){
//			target.add(new SetPattern(newAbove));
//		}
//		for (TrieNode childNode : node.getChildern()) {
//			collectSubsumptionR(childNode, newAbove, target, tolerance);
//		}
//	}


}
