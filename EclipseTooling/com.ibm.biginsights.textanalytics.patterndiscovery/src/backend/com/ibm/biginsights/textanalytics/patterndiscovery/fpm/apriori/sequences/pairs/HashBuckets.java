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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows to store values identified by a hash key. Multiple 
 * values can be mapped to the same key. A collection of the
 * respective elements is returned.
 * 
 * The data structure is realized by a HashMap of collection of values.
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
public class HashBuckets<K,V>{


	
	private ConcurrentHashMap<K,Collection<V>> map = new ConcurrentHashMap<K,Collection<V>>();
	/**
	 * 
	 */
	public HashBuckets() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	public int size() {
		int result = 0;
		for (K key : map.keySet()) {
			result += map.get(key).size();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(Object arg0) {
		for (K key : map.keySet()) {
			if(map.get(key).contains(arg0)){
				return true;
			}
		}
		return false;
	}
	/**
	 * @param key
	 * @return true if the argument is used as a key for a bucket.
	 */
	public boolean containsKey(K key){
		return map.containsKey(key);
	}
	/**
	 * Same as contains .
	 * @param value
	 * @return same as contains 
	 */
	public boolean containsValue(V value){
		return this.contains(value);
	}

	/**
	 * Store the given value under the given key.
	 * @param key
	 * @param value
	 */
	public void put(K key, V value){
		Collection<V> target;
		if(map.containsKey(key)){
			target = map.get(key);
		}else{
			target = new ArrayList<V>();
			map.put(key,target);
		}
		target.add(value);
	}
	
	/**
	 * Returns all values stored for that key.
	 * @param key
	 * @return null if nothing exists for the key
	 */
	public Collection<V>get(K key){
		return map.get(key);		
	}


	public Collection<K>keySet(){
		return map.keySet();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@SuppressWarnings("unchecked")
	public boolean containsAll(Collection arg0) {
		for (Object object : arg0) {
			if(!this.contains(object)){
				return false;
			}
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		map = new ConcurrentHashMap<K,Collection<V>>();
	}
	
	/**
	 * Empties the particular bucket specified by the key.
	 * @param key
	 */
	public void clearBucket(K key){
		map.get(key).clear();
	}
	
	/**
	 * 
	 * @param key
	 */
	public void removeBucket(K key){
		map.remove(key);
	}
}
