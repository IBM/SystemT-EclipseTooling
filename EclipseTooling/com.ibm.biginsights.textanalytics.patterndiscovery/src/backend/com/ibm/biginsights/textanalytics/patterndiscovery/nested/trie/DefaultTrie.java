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
import java.util.LinkedList;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;

/**
 * Trie for integers that stores successors in an array according to a 
 * hash value. 
 * 
 * IDEA: The implementation could be made much faster by not using List 
 * objects but arrays.
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
public class DefaultTrie implements Trie{



	private DefaultTrieNode root;
	
	public DefaultTrie(int hashWidth){
		if(Constants.DEBUG)
			System.out.println("creating trie with hashWidth"+hashWidth);
		root = new DefaultTrieNode(0, Type.WithinSequence, hashWidth);
	}
	
	@SuppressWarnings("unchecked")
	public void present(Collection<Integer> list){
//		System.out.println("Presenting: "+list);
		LinkedList listCopy = new LinkedList(list);
		root.present(listCopy);
//		System.out.println("Trie: \n"+root.toDebugString(""));
	}
	@SuppressWarnings("unchecked")
	public void presentIncontinuous(Collection<Integer> list, int depth){
//		System.out.println("Presenting incontinously: "+list);
		LinkedList listCopy = new LinkedList(list);		
		root.presentIncontinuous(listCopy, depth+1);
//		System.out.println("Trie: \n"+root.toDebugString(""));
	}
	public int getCount(Collection<Integer> list){
		LinkedList<Integer> listCopy = new LinkedList<Integer>(list);		
		return root.getCount(listCopy);
	}
	public void pruneMinCount(double minCount){
		root.pruneMinCount(minCount);
	}
	public TrieNode getRoot(){
		return root;
	}

	public void flushCounts() {
		root.flushCounts();
		
	}

}
