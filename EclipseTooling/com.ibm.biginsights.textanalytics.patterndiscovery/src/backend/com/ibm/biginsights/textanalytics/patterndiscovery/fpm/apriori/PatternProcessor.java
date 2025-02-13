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
public interface PatternProcessor<T> {

 public static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

	public List<T> generateInitialCandidates();
	public List<T> merge(List<T> inputPatterns, Trie candidateTrie, double minSupport);
	public List<T> prune(List<T> inputPatterns, Trie candidateTrie);
	public void computeSupport(List<T> patterns, Trie candidateTrie, int patternLength);
	public Trie  createCandidateTrie(List<T> patterns);
	@SuppressWarnings("unchecked")
	public void setDatabase(Database db);
	
	/**
	 * Because it is an overkill to do the first iteration with a Trie 
	 * (cf. Lars' Diss page 19)
	 * @param candidates
	 * @param minSupport
	 * @return
	 */
	public List<T> iterationOne(List<T> candidates, int minSupport);
	/**
	 * Because it is an overkill to do the second iteration with a Trie 
	 * (cf. Lars' Diss page 19)
	 * @param candidates
	 * @param minSupport
	 * @return
	 */
	public List<T> iterationTwo(List<T> candidates, int minSupport);
		
		

}
