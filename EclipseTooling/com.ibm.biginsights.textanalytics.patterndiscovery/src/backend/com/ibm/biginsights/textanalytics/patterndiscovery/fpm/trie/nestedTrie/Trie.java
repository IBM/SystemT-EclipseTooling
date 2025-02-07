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
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.nestedTrie;

import java.util.Collection;

/**
 * Trie for integers that stores successors in an array according to a hash
 * value.
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *  Kleb (kleb@fzi.de) - extended to general nested trie's
 * 
 * ----- NOTE: Unfortunatly we can't use any generics here. This is based on the
 * lack of depth of the nested statements !!! -----
 * 
 * @version 0.alpha
 * 
 */
@SuppressWarnings("unchecked")
public interface Trie {

	public static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$
	
	/** presentation of a nested Collection */
	public void present(Collection list);

	/** incontinuous presentation of a nested Collection */
	public void presentIncontinuous(Collection list, int depth);

	/** count support value of given collection */
	public int getCount(Collection list);

	/** delete all support values included in tree */
	public void flushCounts();

	/** return root node */
	public TrieNode getRoot();

	/** prune all nodes with support value < minCount */
	public void pruneMinCount(double minCount);
}
