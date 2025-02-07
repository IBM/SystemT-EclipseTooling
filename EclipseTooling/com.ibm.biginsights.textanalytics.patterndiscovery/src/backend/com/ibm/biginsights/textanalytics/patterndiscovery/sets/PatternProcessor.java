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
package com.ibm.biginsights.textanalytics.patterndiscovery.sets;

import java.util.List;

import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.Trie;


/**
 * Wraps the functionality of Apriori processing that is specific to the
 * item and pattern data structure. Also provides the interface to the database.
 * The operations include:
 * <ul>
 *  <li>Generate an initial set of candidates</li>
*   <li>Merge pairs of patterns to get patterns of increased size</li>
*   <li>Prune the pattern set</li>
*   <li>Count support for all patterns in a given database</li>
* </ul>
* 
* TODO: type restrict T
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
@SuppressWarnings("unchecked")
public interface PatternProcessor {

	public static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
	
	public List<Pattern> generateInitialCandidates();
	public List<Pattern> merge(List<Pattern> inputPatterns, Trie candidateTrie, double minSupport);
	public List<Pattern> prune(List<Pattern> inputPatterns, Trie candidateTrie);
	public void computeSupport(List<Pattern> patterns, Trie candidateTrie, int patternLength);
	public Trie  createCandidateTrie(List<Pattern> patterns);
	public void setDatabase(Database db);
	
	
	/**
	 * Because it is an overkill to do the first iteration with a Trie 
	 * (cf. Lars' Diss page 19)
	 * @param candidates
	 * @param minSupport
	 * @return
	 */
	
	public List<Pattern> iterationOne(List<Pattern> candidates, int minSupport);
	/**
	 * Because it is an overkill to do the second iteration with a Trie 
	 * (cf. Lars' Diss page 19)
	 * @param candidates
	 * @param minSupport
	 * @return
	 */
	public List<Pattern> iterationTwo(List<Pattern> candidates, int minSupport);
	
	public List<Pattern> extension(Pattern candidate);
	
	public List<Pattern> minimumExtension(SetPattern candidate);
	
	public void setMinSupport(double minSupport);
	
	public double getMinSupport();
		
		

}
