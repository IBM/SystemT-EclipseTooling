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
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.eclat;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.DefaultTrie;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.DefaultTrieNode;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.Trie;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.TrieNode;
import com.ibm.biginsights.textanalytics.patterndiscovery.sets.Database;
import com.ibm.biginsights.textanalytics.patterndiscovery.sets.Pattern;
import com.ibm.biginsights.textanalytics.patterndiscovery.sets.PatternProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.sets.SetDatabase;
import com.ibm.biginsights.textanalytics.patterndiscovery.sets.SetPattern;
import com.ibm.biginsights.textanalytics.patterndiscovery.sets.SetTransaction;
import com.ibm.biginsights.textanalytics.patterndiscovery.tools.SingletonList;


/**
 * Implements basic pattern operations for a scenario where transactions are
 * modelled as simple sets. In particular:
 * <ul>
 * <li>The alphabet is a subset of the integers. Specified by a max integer</li>
 * <li>The initial candidates are sets containing exactly one integer.</li>
 * <li>Transactions and Patterns are sequence representations of integer sets
 * ordered by the natural order over the integers</li>
 * 
 * </ul>
 * 
 * 
 *  Kleb (kleb@fzi.de)
 * @ToDO: Add Comments
 */
@SuppressWarnings("unchecked")
public class SetPatternProcessor implements PatternProcessor {

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
   
	/** log4j */
	static IPDLog logger = PDLogger.getLogger(PatternProcessor.class.getName());

	private static final double MAX_HASH_WIDTH = Double.MAX_VALUE;
	// private static final double MIN_HASH_WIDTH = 20;
	Database db;

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unika.aifb.fpm.apriori.PatternProcessor#computeSupport(java.util.List,
	 *      edu.unika.aifb.fpm.trie.Trie)
	 */
	public void computeSupport(List patterns, Trie candidateTrie,
			int patternLength) {
		// flush counts in candidateTrie
		candidateTrie.flushCounts();
		// iterate over database
		for (Object transactionO : db) {
			// present each item
			SetTransaction transaction = (SetTransaction) transactionO;
			candidateTrie.presentIncontinuous(transaction.getContent(),
					patternLength);
		}
		// iterate over patterns
		for (Object patternO : patterns) {
			// get for each pattern the count out of the trie and assign it
			SetPattern pattern = (SetPattern) patternO;
			double support = (double) candidateTrie.getCount(pattern
					.getContent());
			pattern.setSupport(support);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unika.aifb.fpm.apriori.PatternProcessor#createCandidateTrie(java.util.List)
	 */
	public Trie createCandidateTrie(List patterns) {
		Trie result = new DefaultTrie((int) Math.round(Math.min(Math.sqrt(db
				.getMaxItemNumber()), MAX_HASH_WIDTH)));
		// Trie result = new DefaultTrie(7);
		for (Object patternO : patterns) {
			SetPattern pattern = (SetPattern) patternO;
			result.present(pattern.getContent());
			logger.info(pattern.toString());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unika.aifb.fpm.apriori.PatternProcessor#generateInitialCandidates()
	 */
	public List<Pattern> generateInitialCandidates() {
		List<Pattern> result = new ArrayList<Pattern>(db.getMaxItemNumber() + 1);
		for (int i = 0; i <= db.getMaxItemNumber(); i++) {
			result.add(new SetPattern(
					new SingletonList<Integer>(new Integer(i))));
			// System.out.println(result.toString());
		}
		return result;
	}

	/**
	 * TODO: the duplicate elemination done here should not be neccessary: Bug??
	 * Judging from the time it occurrend, the bug seems to have to do with the
	 * sibling technology
	 * 
	 * @see com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.PatternProcessor#merge(java.util.List,
	 *      com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.Trie)
	 */
	public List merge(List inputPatterns, Trie candidateTrie, double minSupport) {
		HashSet resultSet = new HashSet();
		resultSet.addAll(mergeR(inputPatterns, candidateTrie,
				(DefaultTrieNode) candidateTrie.getRoot(),
				new LinkedList<Integer>()));
		return new LinkedList<Pattern>(resultSet);
	}

	Trie candidateTrie = null;

	// if positive add to lastNodePattern
	// NOTE: For simple processing no tree is needed
	// optimize: check if possibleExtensionElement has any extension
	//
	//
	public List<Pattern> overallAllPattern = new LinkedList<Pattern>();

	/**
	 * @ToDO: GERMAN: Speicherung erfolgt momentan nicht in einem TREE! Hier
	 *        muss das Hinzuf�gen von F_n zu fOverall dementsprechend
	 *        ber�cksichtigt werden. Die Kandidatenauswahl jedoch profitiert bei
	 *        Eclat nicht von der Treespeicherung. Der wesentliche Punkt liegt
	 *        in der �bergabe der Extension des Vorherigen Patterns und der
	 *        HashMap der Pattern, welche nur aus einem Element bestehen. Hier
	 *        k�nnte man zur Optimierung diese als Trie organisieren um Speicher
	 *        zu sparen.
	 */
	public void addFrequentPatternSuperset(Pattern patternToAnalyse,
			List<Integer> extensionCandidates,
			List<Pattern> extensionsOfPatternToAnalyse) {
		logger.info("addFrequentPatternSuperset\n\t" + patternToAnalyse
				+ "\n\t" + extensionCandidates + "\n\t"
				+ extensionsOfPatternToAnalyse);
		// sort extensionCandidates
		overallAllPattern.add(patternToAnalyse);
		TreeSet<Integer> extensionCandidatesSet = new TreeSet<Integer>(
				extensionCandidates);
		extensionCandidates = new LinkedList<Integer>(extensionCandidatesSet);
		int lastElementOfPattern = 0;
		if (patternToAnalyse.getContent().size() != 0) {
			lastElementOfPattern = patternToAnalyse.getContent().toArray(
					new Integer[patternToAnalyse.getContent().size()])[patternToAnalyse
					.getContent().size() - 1];
		}
		if (extensionCandidates != null && extensionCandidates.size() != 0) {
			// from highest to lowest
			ListIterator<Integer> iterExtensionCandidates = extensionCandidates
					.listIterator(extensionCandidates.size());
			List<Integer> nextExtensionCandidates = new LinkedList<Integer>();
			while (iterExtensionCandidates.hasPrevious()) {
				int possibleExtensionElement = iterExtensionCandidates
						.previous();
				logger.info(Integer.toString(lastElementOfPattern));
				logger.info(Integer.toString(possibleExtensionElement));
				if (lastElementOfPattern >= possibleExtensionElement)
					continue;
				List<Integer> cand = new LinkedList<Integer>();
				cand.add(possibleExtensionElement);
				Pattern canPattern = new SetPattern(cand);
				SetPattern combinedPattern = null;
				try {
					combinedPattern = ((SetPattern) patternToAnalyse).clone();
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				combinedPattern
						.extendPatternByContentOfForeignPattern(canPattern);
				logger.info("Candidate Pattern: [" + combinedPattern + "]");
				List<Pattern> extensionOfCombinedPattern = extension(
						extensionsOfPatternToAnalyse, canPattern);
				int supportOfCombinedPattern = extensionOfCombinedPattern
						.size();
				combinedPattern.setSupport(supportOfCombinedPattern);
				if (supportOfCombinedPattern >= minSupport) {
					nextExtensionCandidates.add(possibleExtensionElement);
					addFrequentPatternSuperset(combinedPattern,
							nextExtensionCandidates, extensionOfCombinedPattern);
				}
			}
		}
	}

	public void addFrequentPatternSuperset(List<Pattern> extensionCandidates) {
		List<Integer> extensionCandidatesInt = new LinkedList<Integer>();
		for (Pattern pat : extensionCandidates) {
			extensionCandidatesInt
					.add(pat.getContent().toArray(new Integer[1])[0]);
		}
		addFrequentPatternSuperset(new SetPattern(new LinkedList<Integer>()),
				extensionCandidatesInt, null);
	}

	private List mergeR(List inputPatterns, Trie candidateTrie,
			DefaultTrieNode currentNode, List<Integer> above) {
		LinkedList<Pattern> result = new LinkedList<Pattern>();

		// for all nodes one layer above the leaves (which represent sequence
		// X).
		LinkedList<TrieNode> children = currentNode.getChildern();
		if (children != null && children.size() != 0
				&& children.getFirst().isLeaf()) {// This line assumes that
			// all leaves are at the
			// same depth.
			// for all non-equal combinations (a,b)
			for (TrieNode nodeA : children) {
				for (TrieNode nodeB : children) {
					if (nodeA.getLabel() >= nodeB.getLabel())
						continue; // always nodeA < nodeB
					// check if there exists a candidate node for each
					// subsequence of <X, a, b> that skips exactly one element
					LinkedList<Integer> part1 = new LinkedList<Integer>();
					LinkedList<Integer> part2 = new LinkedList<Integer>(above);
					Integer skipped = null;
					boolean allSupported = true;
					while (!part2.isEmpty() && skipped == null) {
						if (skipped != null) {
							part1.addLast(skipped);
						}
						if (!part2.isEmpty()) {
							skipped = part2.removeFirst();
						} else {
							skipped = null;
						}
						// concatenate part1, part2, nodeA.label and nodeB.label
						LinkedList<Integer> toPresent = new LinkedList<Integer>();
						toPresent.addAll(part1);
						toPresent.addAll(part2);
						toPresent.add(new Integer(nodeA.getLabel()));
						toPresent.add(new Integer(nodeB.getLabel()));
						if (candidateTrie.getCount(toPresent) == 0) {
							allSupported = false;
							break;
						}
					}
					if (allSupported) {
						LinkedList<Integer> toReturn = new LinkedList<Integer>(
								above);
						// remove the root node and add the current node (if
						// it's not the root)
						if (above.size() > 0) {
							toReturn.removeFirst();
							toReturn.add(new Integer(currentNode.getLabel()));
						}
						toReturn.add(new Integer(nodeA.getLabel()));
						toReturn.add(new Integer(nodeB.getLabel()));
						result.add(new SetPattern(toReturn));
					}
				}
			}
		} else {
			// recurr on children
			LinkedList<Integer> newAbove = new LinkedList<Integer>(above);
			newAbove.add(currentNode.getLabel());
			for (TrieNode child : children) {
				result.addAll(mergeR(inputPatterns, candidateTrie,
						(DefaultTrieNode) child, newAbove));
			}
		}
		// recurr on siblings
		if (((DefaultTrieNode) currentNode).sibling != null) {
			result.addAll(mergeR(inputPatterns, candidateTrie,
					currentNode.sibling, above));
		}
		return result;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.unika.aifb.fpm.apriori.PatternProcessor#prune(java.util.List,
	 *      edu.unika.aifb.fpm.trie.Trie)
	 */
	public List prune(List inputPatterns, Trie candidateTrie) {
		// in the above implementation of merge, pruning is included. Thus, we
		// return (a copy of) the input
		return new LinkedList<Pattern>(inputPatterns);
	}

	public void setDatabase(Database db) {
		if (!(db instanceof SetDatabase)) {
			if(Constants.DEBUG)
				System.err.println("Database has to be of type SetDatabase");
			return;
		}
		this.db = db;
	}

	// ExtensionSet according one element of candidateSet consisting of patterns
	// with size 1
	public List<Pattern> iterationOne(List<Pattern> candidates, int minSupport) {
		List<Pattern> c = new LinkedList<Pattern>();
		for (Pattern oneSizeCandidatePattern : candidates) {
			List<Pattern> extensionListOfGivenCandidatePattern = extension(oneSizeCandidatePattern);
			logger.info(extensionListOfGivenCandidatePattern.toString());
			if (extensionListOfGivenCandidatePattern.size() >= minSupport)
				c.add(oneSizeCandidatePattern);
		}
		return c;
	}

	public List iterationTwo(List candidates, int minSupport) {
		// counting datastructure: HashMap of 2D Points to counts
		HashMap<Point, Integer> matrix = new HashMap<Point, Integer>();
		// construct HashSet that allows checking if an integer is an iteration
		// 2 candidate
		HashSet<Integer> presentInts = new HashSet<Integer>();
		for (Object candidate : candidates) {
			presentInts.add(((TreeSet<Integer>) ((SetPattern) candidate)
					.getContent()).first());
		}
		// iterate over all pairs of items in each transaction (smaller value
		// first)
		for (Object transactionO : db) {
			SetTransaction transaction = (SetTransaction) transactionO;
			for (int x : transaction.getContent()) {
				for (int y : transaction.getContent()) {
					if (x < y) {
						Point key = new Point(x, y);
						Integer count = matrix.get(key);
						// if not present in HashMap, add with count 1
						if (count == null) {
							matrix.put(key, 1);
						} else {
							// otherwise increase count
							matrix.put(key, count.intValue() + 1);
						}
					}
				}
			}
		}

		// iterate over keys in hashmap, output all pairs that exceed minSupport
		List<Pattern> result = new LinkedList<Pattern>();
		for (Point key : matrix.keySet()) {
			if (matrix.get(key).intValue() >= minSupport) {
				List<Integer> pair = new LinkedList<Integer>();
				pair.add(key.x);
				pair.add(key.y);
				result.add(new SetPattern(pair));
			}
		}
		return result;
	}

	// public List iterationOne(List candidates, int minSupport) {
	// HashSet<Integer> used = new HashSet<Integer>();
	// for (Object transactionO : db) {
	// SetTransaction transaction = (SetTransaction)transactionO;
	// for (int integer : transaction.getContent()) {
	// used.add(integer);
	// }
	// }
	// LinkedList<SetPattern> result = new LinkedList<SetPattern>();
	// for (int integer : used) {
	// LinkedList<Integer> content = new LinkedList<Integer>();
	// content.add(integer);
	// result.add(new SetPattern(content));
	// }
	// return result;
	// }
	/**
	 * takes the output of an apriori run and returns only those Patterns that
	 * are unique in the sense that they are not a shortened version of another
	 * pattern. This criterion is determined by a trie containing all patterns
	 * of all lengths. A pattern is returned if it is represented by a leaf node
	 * or by a node that has for which the sum of the supports of the children
	 * is by more than a given tolerance lower than that of the node.
	 * 
	 * Note: the tolerance is currently ignored
	 */
	public static List cleanSubsumption(List[] input, double tolerance) {
		List<Pattern> result = new LinkedList<Pattern>();
		// von klein nach gro�.
		for (int i = 2; i < input.length; i++) {
			Trie patternTrie = new DefaultTrie(20);
			// Von alle patterns der gr��e n, alle varianten mit einer L�schung
			// generieren.
			// diese in trie einz�hlen
			if (input[i] == null) {
				if (input[i - 1] != null) {
					result.addAll(input[i - 1]);
				}
				continue;
			}
			for (Object patternO : input[i]) {
				SetPattern pattern = (SetPattern) patternO;
				for (Integer toDelete : pattern.getContent()) {
					List<Integer> thisSequence = new ArrayList<Integer>(pattern
							.getContent());
					thisSequence.remove(toDelete);
					patternTrie.present(thisSequence);
				}
			}
			// nur diejenigen patterns der l�nge n-1 behalten, die nicht im Trie
			// enthalten sind
			for (Iterator iter = input[i - 1].iterator(); iter.hasNext();) {
				SetPattern pattern = (SetPattern) iter.next();
				if (patternTrie.getCount(pattern.getContent()) == 0) {
					result.add(pattern);
				}
			}
		}
		// von den gr��ten patterns alle behalten
		if (input[input.length - 1] != null) {
			result.addAll(input[input.length - 1]);
		}
		return result;
	}

	// public static List cleanSubsumption(List[] input, double tolerance) {
	// Trie patternTrie = new DefaultTrie(20);
	// List<Pattern>result = new LinkedList<Pattern>();
	// for (List<Pattern> list : input) {
	// if(list == null) continue;
	// for (Pattern pattern : list) {
	// SetPattern setPattern = (SetPattern)pattern;
	// patternTrie.present(setPattern.getContent());
	// }
	// }
	// for (TrieNode node : patternTrie.getRoot().getChildern()) {
	// collectSubsumptionR(node,new LinkedList<Integer>(),result,tolerance);
	// }
	// return result;
	//		
	// }
	// private static void collectSubsumptionR(TrieNode node,
	// List<Integer>above, List<Pattern>target, double tolerance){
	// List<Integer> newAbove = new LinkedList<Integer>();
	// newAbove.addAll(above);
	// newAbove.add(node.getLabel());
	// if(node.isLeaf()){
	// target.add(new SetPattern(newAbove));
	// return;
	// }
	// int supportBelow = 0;
	// for (TrieNode childNode : node.getChildern()) {
	// supportBelow += childNode.getCount();
	// }
	// if(node.getCount()- supportBelow>node.getCount()*tolerance){
	// target.add(new SetPattern(newAbove));
	// }
	// for (TrieNode childNode : node.getChildern()) {
	// collectSubsumptionR(childNode, newAbove, target, tolerance);
	// }
	// }

	// -------------------------------- NEW METHODS 
	// Kleb (kleb@fzi.de) ------------------------------------------

	/**
	 * function extension() calculates all Pattern (Transactions) that are
	 * available according the underlying database
	 * 
	 * @param candidate -
	 *            mandatory element(s) and base for all extensions
	 * 
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * NOTE: It's a design decision to hold only the
	 * "extensionSetForOneSizeCandidates" in memory. We could do the same for
	 * every processed pattern with the consequence to blow up the system (at
	 * least the memory)
	 * 
	 * +++ advantage: Only a limited map is hold in memory --- disadvantage:
	 * Every time we begin the extension process from the very beginning -->
	 * high amount of equality operations and list iterations
	 * 
	 * @TODO: Test alternative implementation with a HashMap<Integer
	 *        (pattern.size), <HashMap<Pattern, List<Pattern>>>> holding every
	 *        processed combination
	 * 
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 */
	HashMap<Pattern, List<Pattern>> extensionSetForOneSizeCandidates = new HashMap<Pattern, List<Pattern>>();

	public List<Pattern> extension(Pattern candidate) {
		logger.info("Method: extension(Pattern)");
		logger.info("Candidate: " + candidate);
		try {
			// Holds an interal memory HashMap of patternsOfOneSize
			// ext([1,4]) = ext(1) intersection ext(4)
			if (candidate.getContent().size() == 1) {
				logger.info("candidatePattern is of size 1");
				return extensionOfCandidatePatternWithOneElement(candidate);
			}
			LinkedList<List<Pattern>> listOffAllOneElementExtensionsOfGivenPattern = new LinkedList<List<Pattern>>();
			for (Integer patternElement : candidate.getContent()) {
				List integerList = new LinkedList<Integer>();
				integerList.add(patternElement);
				Pattern oneElementPattern = new SetPattern(
						new LinkedList<Integer>(integerList));
				listOffAllOneElementExtensionsOfGivenPattern
						.add(extensionOfCandidatePatternWithOneElement(oneElementPattern));
			}
			// perform intersection
			List<Pattern> extensionListOfGivenInputPattern = new LinkedList<Pattern>();
			boolean firstIteration = true;
			for (List<Pattern> patternListOfOneElementExtension : listOffAllOneElementExtensionsOfGivenPattern) {
				if (firstIteration) {
					extensionListOfGivenInputPattern
							.addAll(patternListOfOneElementExtension);
					logger.info("patternListOfOneElementExtension: "
							+ patternListOfOneElementExtension);
					logger.info("extensionListOfGivenInputPattern "
							+ extensionListOfGivenInputPattern);
					firstIteration = false;
					continue;
				}
				logger.info("patternListOfOneElementExtension: "
						+ patternListOfOneElementExtension);
				extensionListOfGivenInputPattern
						.retainAll(patternListOfOneElementExtension);
				logger.info("extensionListOfGivenInputPattern "
						+ extensionListOfGivenInputPattern);
			}
			return extensionListOfGivenInputPattern;
		} catch (Exception e) {
			// only thrown if pattern includes more than one elment (should
			// never hapen in this method :-) )
			e.fillInStackTrace();
		}
		return null;
	}

	/**
	 * function extension() calculates all Pattern (Transactions) that are
	 * available according the underlying database
	 * 
	 * @param candidate -
	 *            mandatory element(s) and base for all extensions
	 */
	public List<Pattern> extensionOfCandidatePatternWithOneElement(
			Pattern candidatePatternWithOneElement) throws Exception {
		logger
				.info("Method: extensionOfCandidatePatternWithOneElement(Pattern)");
		logger.info("Candidate: " + candidatePatternWithOneElement);
		if (candidatePatternWithOneElement.getContent().size() > 1)
			throw new Exception(
					"Only patterns including at most one element are supported");

		List<Pattern> extensionsOfGivenCandidate = new LinkedList<Pattern>();
		if (extensionSetForOneSizeCandidates
				.get(candidatePatternWithOneElement) != null)
			return extensionSetForOneSizeCandidates
					.get(candidatePatternWithOneElement);
		for (Object transactionO : db) {
			// use of SetPattern because of the subsume functionality!
			Pattern transaction = new SetPattern(
					((SetTransaction) transactionO).getContent());
			// logger.info("Transaction: "+transaction);
			// check if it's a possible extension
			if (candidatePatternWithOneElement.subsumes(transaction)) {
				// // check if candidate is on last position of set (--> no
				// extension is possible)
				// int candidateInteger = candidatePatternWithOneElement
				// .getContent().toArray(new Integer[1])[0];
				// if (transaction.getContent().toArray(
				// new Integer[transaction.getContent().size()])[transaction
				// .getContent().size() - 1] != candidateInteger)
				extensionsOfGivenCandidate.add(transaction);
			}
		}
		extensionSetForOneSizeCandidates.put(candidatePatternWithOneElement,
				extensionsOfGivenCandidate);
		logger.info("Extensions for " + candidatePatternWithOneElement + "\t"
				+ extensionsOfGivenCandidate);
		return extensionsOfGivenCandidate;
	}

	/**
	 * function extension() calculates all Pattern (Transactions) that are
	 * available according the underlying database, but only the next element of
	 * following of the initial patternElements e.g. Transactions (1,4,6),
	 * (1,3,5), (1,3,7),(5,8,9) candidate (1) return (1,4),(1,3)
	 * 
	 * @param candidate -
	 *            mandatory element(s) and base for all extensions
	 */
	public List<Pattern> minimumExtension(SetPattern candidate) {
		logger.info("Candidate: " + candidate);
		HashSet<Pattern> minimumExtensionsOfGivenCandidate = new HashSet<Pattern>();
		for (Object transactionO : db) {
			// use of SetPattern because of the subsume functionality!
			SetPattern transaction = new SetPattern(
					((SetTransaction) transactionO).getContent());
			// check if it's a possible extension
			if (candidate.subsumes((Pattern) transaction)) {
				SetPattern minimumExtendedCandidate = null;
				try {
					minimumExtendedCandidate = candidate.clone();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// candidate the following element
				SortedSet<Integer> partOfTransactionAfterLastElementOfCandidate = ((TreeSet<Integer>) transaction
						.getContent()).tailSet(candidate.getContent().toArray(
						(new Integer[candidate.getContent().size()]))[candidate
						.getContent().size() - 1]);
				partOfTransactionAfterLastElementOfCandidate
						.remove(partOfTransactionAfterLastElementOfCandidate
								.first());
				logger.info("partOfTransactionAfterLastElementOfCandidate: "
						+ partOfTransactionAfterLastElementOfCandidate);
				if (partOfTransactionAfterLastElementOfCandidate.size() > 0) {
					minimumExtendedCandidate.getContent().add(
							partOfTransactionAfterLastElementOfCandidate
									.first());
					logger.info(minimumExtendedCandidate.toString());
					minimumExtensionsOfGivenCandidate
							.add(minimumExtendedCandidate);
				}
			}
		}
		List<Pattern> returnList = new LinkedList<Pattern>();
		returnList.addAll(minimumExtensionsOfGivenCandidate);
		return returnList;
	}

	private int minSupport;

	public double getMinSupport() {
		return minSupport;
	}

	public void setMinSupport(double minSupport) {
		this.minSupport = (new Double(minSupport)).intValue();
	}

	/**
	 * Extension is calculated by subsumption of patternTwo extension with given
	 * pattern list
	 * 
	 * @param extensionOfPatternOne
	 * @param patternTwo
	 * @return
	 * @throws Exception
	 */
	public List<Pattern> extension(List<Pattern> extensionOfPatternOne,
			Pattern patternTwo) {
		if (patternTwo.getContent().size() > 1)
			return null;
		try {
			if (extensionOfPatternOne == null)
				return extensionOfCandidatePatternWithOneElement(patternTwo);
			List<Pattern> extensionOfPatternTwo = extensionOfCandidatePatternWithOneElement(patternTwo);
			extensionOfPatternOne.retainAll(extensionOfPatternTwo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return extensionOfPatternOne;
	}

}
