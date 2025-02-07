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

import java.util.Collection;

/**
 * Trie for integers that stores successors in an array according to a 
 * hash value. 
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
public interface Trie {

	public static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
	
	public void present(Collection<Integer> list);
	public void presentIncontinuous(Collection<Integer>list, int depth);
	public int getCount(Collection<Integer> list);
	public void flushCounts();
	public TrieNode getRoot();
	public void pruneMinCount(double minCount);
}
