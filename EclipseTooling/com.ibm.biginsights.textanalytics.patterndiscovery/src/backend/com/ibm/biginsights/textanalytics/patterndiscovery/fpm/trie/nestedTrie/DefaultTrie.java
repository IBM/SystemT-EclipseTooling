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
import java.util.LinkedList;

/**
 * Trie for integers that stores successors in an array according to a hash
 * value.
 * 
 * IDEA: The implementation could be made much faster by not using List objects
 * but arrays.
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *  Kleb (kleb@fzi.de) - extended to general nested trie's
 * 
 * ----- NOTE: Unfortunatly we can't use any generics here. This is based on the
 * lack of depth of the nested statements !!! -----
 * 
 */
@SuppressWarnings("unchecked")
public class DefaultTrie implements Trie {



	private DefaultTrieNode root;

	public DefaultTrie(int hashWidth) {
		root = new DefaultTrieNode(0, 0, hashWidth);
	}

	public void present(Collection list) {
		// System.out.println("Presenting: "+list);
		LinkedList listCopy = new LinkedList(list);
		// the root value by definition is 0
		listCopy.addFirst(new Integer(0));
		root.present(listCopy, 0);
		// System.out.println("Trie: \n"+root.toDebugString(""));
	}

	public void presentIncontinuous(Collection list, int depth) {
		// System.out.println("Presenting incontinously: "+list);
		LinkedList listCopy = new LinkedList(list);
		// the root value by definition is 0
		listCopy.addFirst(new Integer(0));
		root.presentIncontinuous(listCopy, depth + 1);
		// System.out.println("Trie: \n"+root.toDebugString(""));
	}

	public int getCount(Collection list) {
		LinkedList<Integer> listCopy = new LinkedList<Integer>(list);
		// the root value by definition is 0
		listCopy.addFirst(new Integer(0));
		return root.getCount(listCopy);
	}

	public void pruneMinCount(double minCount) {
		root.pruneMinCount(minCount);
	}

	public TrieNode getRoot() {
		return root;
	}

	public void flushCounts() {
		root.flushCounts();

	}

}
