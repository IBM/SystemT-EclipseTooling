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
import java.util.List;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.Trie;


/**
 * Implementation of the apriori algorithm. Independant of the representation of
 * items, datasets and patterns. 
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
public class GeneralApriori {


	
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
	public GeneralApriori(String patternProcessorImpl, int maxPatternSize, double minSupport) throws InstantiationException, IllegalAccessException, ClassNotFoundException, ClassCastException{
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
		//System.out.println("Done generating candidates");
		f = new List[maxPatternSize+1];
		// call heuristic method for first iterations
		if(Constants.DEBUG)
			System.out.println("starting loop n = "+1);
		c = pp.iterationOne(c, (int)Math.floor(minSupport));
		f[1] = c;
		if(Constants.DEBUG)
			System.out.println("starting loop n = "+2);
		c = pp.iterationTwo(c, (int)Math.floor(minSupport));
		f[2]=c;
		Trie candidateTrie = pp.createCandidateTrie(c);
		//construct merged candidates
		c = pp.merge(c, candidateTrie,minSupport);
		//prune
		c = pp.prune(c, candidateTrie);
		int n = 3; //current candidate size
		
		//iterate while the candidate set C is non-empty
		for(;!c.isEmpty()&&n<=maxPatternSize;n++){
			long t0 = System.currentTimeMillis();
			if(Constants.DEBUG)
				System.out.println("starting loop n = "+n);
			//System.out.println(c);
			f[n] = c;
			candidateTrie = pp.createCandidateTrie(c);
			//count support for all candidates in C
			pp.computeSupport(c, candidateTrie, n);
			//filter out candidates with too small support
			// joa: strongly recommended to move this into the prune method!
			for (Iterator iter = c.iterator(); iter.hasNext();) {
				Pattern candidate = (Pattern) iter.next();
				if(candidate.getSupport()<minSupport)
					iter.remove();
			}
			//update trie to the new set of patterns
			candidateTrie = null;System.gc();//they say, explicit nulling not useful but maybe it is because pp is reflection-loaded 
			candidateTrie = pp.createCandidateTrie(c);
			//construct merged candidates
			c = pp.merge(c, candidateTrie,minSupport);
			//prune
			c = pp.prune(c, candidateTrie);
//			collect garbage
			candidateTrie=null;
			System.gc();
			System.out.println("iteration took "+((System.currentTimeMillis()-t0)/1000)+"s");
			
		}
		return f;
	}
}
