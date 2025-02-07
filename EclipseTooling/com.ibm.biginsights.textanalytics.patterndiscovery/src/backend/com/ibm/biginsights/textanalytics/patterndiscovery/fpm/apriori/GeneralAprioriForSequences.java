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
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.SetPatternProcessorForSequences;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.pairs.SequencePairPatternProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.Trie;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.TrieNode;


/**
 * Implementation of the apriori algorithm. Independant of the representation of
 * items, datasets and patterns. Special case of Agrawal and Sirkant (1995) for Sequences.
 * 
 *  Kleb (kleb@fzi.de)
 *
 */
public class GeneralAprioriForSequences {



	// logger 
	static IPDLog logger = PDLogger.getLogger(GeneralAprioriForSequences.class.getName());
	
	//parameters
		//maximal pattern size
	private int maxPatternSize;
		//min support
	private double minSupport;
			//name of pattern processor
			//name of candidate trie implementation
			
	//computing components
		//pattern merger, pruner and candidate set generator
	@SuppressWarnings("unchecked")
	private PatternProcessor pp;
		//Candidate Trie
			//is actually a local variable only the class has to be defined
				
	
	//Variables
		//Set of current candidates
	List<Pattern> c;
		//resulting candidates per size
	List<Pattern>[] f;
	/**
	 * @param patternProcessorImpl
	 * @param maxPatternSize
	 * @param minSupport
	 * @throws InstantiationException When patternProcessorImpl or candidateTrieImpl cannot be instantiated.
	 * @throws IllegalAccessException When patternProcessorImpl or candidateTrieImpl cannot be accessed.
	 * @throws ClassNotFoundException When patternProcessorImpl or candidateTrieImpl cannot be found.
	 * @throws ClassCastException When patternProcessorImpl or candidateTrieImpl are of the wrong type.
	 */
	@SuppressWarnings("unchecked")
	public GeneralAprioriForSequences(String patternProcessorImpl, int maxPatternSize, double minSupport) throws InstantiationException, IllegalAccessException, ClassNotFoundException, ClassCastException{
		//set parameters
		this.maxPatternSize = maxPatternSize;
		this.minSupport = minSupport;
		//reflection-load implementations
		pp = (PatternProcessor) Class.forName(patternProcessorImpl).newInstance();
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Pattern>[] doWork(Database db){
		pp.setDatabase(db);
		//initialize candidate set C
		c = pp.generateInitialCandidates();
		//logger.info("Done generating candidates");
		logger.info("InitialCandidates: "+c.size());
		f = new List[maxPatternSize+1];

		logger.info("starting loop n = "+1);
		c = pp.iterationOne(c, (int)Math.floor(minSupport));
		logger.info("IterationOneCandidates: "+c.size());
		f[1] = c;
		// first iteration needs to consider the backreferences!
		Trie candidateTrie = ((SetPatternProcessorForSequences) pp).createCandidateTrieAfterIterationOne(c);		
		pp.computeSupport(c, candidateTrie, 1);
		logger.info("MaxPatternSize: "+maxPatternSize);
		//optimized sectiond iteration
		c = pp.iterationTwo(c, (int)Math.floor(minSupport));
		logger.info("Iteration 2 done: "+c.size());
		f[2]=c;
		candidateTrie = ((SetPatternProcessorForSequences) pp).createCandidateTrieAfterIterationTwo(c);
		logger.info("createCandidateTrieAfterIterationTwo done: "+c.size());
		//iterate while the candidate set C is non-empty
		for(int n=2;!c.isEmpty()&&n<maxPatternSize;n++){		
			c=((SetPatternProcessorForSequences) pp).generateCandidatesForTheNextLevel(candidateTrie, n);
			logger.info("Iteration: "+n+" Number of candidates for the next Level: "+c.size());
			//logger.info(trieToString(candidateTrie));
			pp.computeSupport(c, candidateTrie, n+1);
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				Pattern candidate = (Pattern) iter.next();
//				logger.info("Pattern: "+candidate+"\tSupport: "+candidate.getSupport());
				if(candidate.getSupport()<minSupport)
					iter.remove();
			}
			logger.info("size of c:\t"+c.size());
			candidateTrie = ((SetPatternProcessorForSequences) pp).pruneMinCountLeafs(n+1, candidateTrie, minSupport);
			f[n+1] = c;
		}
		
		// @TODO: delete commented section :-)
//		System.exit(-1);
//		
//		
//		c = ((SetPatternProcessorForSequences) pp).generateCandidatesForTheNextLevel(candidateTrie, 1);
//		logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		logger.info(trieToString(candidateTrie));
//		logger.info("c:\t"+c);
//		
//		
//		logger.info("\n");
//		pp.computeSupport(c, candidateTrie, 2);
////		for (Iterator iter = c.iterator(); iter.hasNext();) {
////			Pattern candidate = (Pattern) iter.next();
////			logger.info(candidate+"\t"+candidateTrie.getCount(((SequencePattern) candidate).getContent()));
////		}
//		
//		logger.info("c:\t"+c);
//	
//		//filter out candidates with too small support
//		// joa: strongly recommended to move this into the prune method!
//		for (Iterator iter = c.iterator(); iter.hasNext();) {
//			Pattern candidate = (Pattern) iter.next();
//			logger.info("Pattern: "+candidate+"\tSupport: "+candidate.getSupport());
//			if(candidate.getSupport()<minSupport)
//				iter.remove();
//		}
//		logger.info("c:\t"+c);
//		logger.info("!!!!!!!HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//		logger.info(trieToString(candidateTrie));
//		candidateTrie = ((SetPatternProcessorForSequences) pp).pruneMinCountLeafs(2, candidateTrie, minSupport);
//		logger.info(trieToString(candidateTrie));
//		logger.info("\n");logger.info("\n");logger.info("\n");logger.info("\n");logger.info("\n");logger.info("\n");
//		c = ((SetPatternProcessorForSequences) pp).generateCandidatesForTheNextLevel(candidateTrie, 2);
//		logger.info("c:\t"+c);
//		pp.computeSupport(c, candidateTrie, 3);
//		//filter out candidates with too small support
//		// joa: strongly recommended to move this into the prune method!
//		for (Iterator iter = c.iterator(); iter.hasNext();) {
//			Pattern candidate = (Pattern) iter.next();
//			if(candidate.getSupport()<minSupport)
//				iter.remove();
//		}
//		logger.info("c:\t"+c);
//		
//		
//		
//		logger.info(trieToString(candidateTrie));
//		candidateTrie = ((SetPatternProcessorForSequences) pp).pruneMinCountLeafs(3, candidateTrie, minSupport);
//		logger.info(trieToString(candidateTrie));
//		logger.info("\n");logger.info("\n");logger.info("\n");logger.info("\n");logger.info("\n");logger.info("\n");
//		c = ((SetPatternProcessorForSequences) pp).generateCandidatesForTheNextLevel(candidateTrie, 3);
//		logger.info("c:\t"+c);
//		pp.computeSupport(c, candidateTrie, 4);
//		//filter out candidates with too small support
//		// joa: strongly recommended to move this into the prune method!
//		for (Iterator iter = c.iterator(); iter.hasNext();) {
//			Pattern candidate = (Pattern) iter.next();
//			if(candidate.getSupport()<minSupport)
//				iter.remove();
//		}
//		logger.info("c:\t"+c);
//		
//		
//		//construct merged candidates
////		c = pp.merge(c, candidateTrie,minSupport);
////		//prune
////		c = pp.prune(c, candidateTrie);
////		int n = 3; //current candidate size
////		
////		//iterate while the candidate set C is non-empty
////		for(;!c.isEmpty()&&n<=maxPatternSize;n++){
////			long t0 = System.currentTimeMillis();
////			logger.info("starting loop n = "+n);
////			//logger.info(c);
////			f[n] = c;
////			candidateTrie = pp.createCandidateTrie(c);
////			//count support for all candidates in C
////			pp.computeSupport(c, candidateTrie, n);
////			//filter out candidates with too small support
////			// joa: strongly recommended to move this into the prune method!
////			for (Iterator iter = c.iterator(); iter.hasNext();) {
////				Pattern candidate = (Pattern) iter.next();
////				if(candidate.getSupport()<minSupport)
////					iter.remove();
////			}
////			//update trie to the new set of patterns
////			candidateTrie = null;System.gc();//they say, explicit nulling not useful but maybe it is because pp is reflection-loaded 
////			candidateTrie = pp.createCandidateTrie(c);
////			//construct merged candidates
////			c = pp.merge(c, candidateTrie,minSupport);
////			//prune
////			c = pp.prune(c, candidateTrie);
//////			collect garbage
////			candidateTrie=null;
////			System.gc();
////			logger.info("iteration took "+((System.currentTimeMillis()-t0)/1000)+"s");
////			
////		}
		if (pp instanceof SequencePairPatternProcessor) {
			SequencePairPatternProcessor spp = (SequencePairPatternProcessor) pp;
			spp.getFrequentMatchPairs(spp.getLastTrie());
		}
		return f;
	}
	
	public String trieToString(Trie candidateTrie) {
		StringBuffer result = new StringBuffer();
		result = trieToString(candidateTrie.getRoot(), result);
		return result.toString();
	}
	
	public StringBuffer trieToString(TrieNode parent, StringBuffer result) {
		result.append("Parent: "+parent+"\n");
		LinkedList<TrieNode> childs = parent.getChildern();
		result.append("ChildOfParent: "+childs+"\n");
		for(TrieNode child: childs) {
			if(!child.getChildern().isEmpty())trieToString(child, result);
		}
		return result;
	}
}
