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
import java.util.Collection;
import java.util.LinkedList;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.trie.DefaultTrie;


/**
 * 
 * Maintains extra information for paired sequence mining in each node:<ul>
 * <li>A reference to the parent node
 * <li>Its depth in the trie
 * <li>The transactions it matches and at which position.
 * </ul>
 * 
 * 
 * 
 * 
 */
public class SequencePairTrie extends DefaultTrie{


	
	SequencePairPatternProcessor processor;

	public SequencePairTrie(int hashWidth, SequencePairPatternProcessor processor) {
		super(hashWidth);
		if(Constants.DEBUG)
			System.out.println("creating trie with hashWidth"+hashWidth);
		this.root = new SequencePairTrieNode(0,null,hashWidth,0);
		this.processor = processor;
	}
	public void present(Collection<Integer> list) {
		LinkedList<Integer> listCopy = new LinkedList<Integer>(list);
		listCopy.addFirst(new Integer(0));
		((SequencePairTrieNode)root).present(listCopy);
	}
	
	
	public SequencePairPatternProcessor getProcessor() {
		return processor;
	}
	public void setProcessor(SequencePairPatternProcessor processor) {
		this.processor = processor;
	}

}
