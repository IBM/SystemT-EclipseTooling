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
package com.ibm.biginsights.textanalytics.patterndiscovery.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * 
 * Counts large amounts of objects over based on a HashMap datastructur
 *
 */
public class HashCount<T> {


	
	HashMap<T, Integer> map = new HashMap<T, Integer>();
	public int increment(T object){
		return update(object,1);
	}
	public int decrement(T object){
		return update(object,-1);
	}
	public int update(T object, int delta){
		Integer count = map.get(object);
		if(count == null){
			map.put(object, delta);
			return delta;
		}else{
			map.put(object, count+delta);
			return count+delta;
		}
	}
	public int getCount(T object){
		Integer count = map.get(object);
		if(count == null){
			return 0;
		}else 
			return count;
	}
	public Set<T>getCounted(){
		return map.keySet();
	}
	public Collection<Integer>values(){
		return map.values();
	}
}
