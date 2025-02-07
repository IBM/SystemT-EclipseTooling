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
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sets;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.Transaction;


/**
 * Represents a transaction instance for a scenario where transactions 
 * are modelled as simple sets.
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
@SuppressWarnings("unchecked")
public class SetTransaction implements Transaction, Comparable{



	protected SortedSet<Integer> content;
	public SetTransaction(Collection<Integer> content){
		this.content = new TreeSet<Integer>(content);
	}
	public Collection<Integer> getContent(){
		return content;
	}
	public boolean equals(Object o){
		if (! (o instanceof SetTransaction)){
			return false;
		}
		SetTransaction so = (SetTransaction) o;
		return this.content.equals(so.content);
	}
	public int compareTo(Object o) {
		if (! (o instanceof SetTransaction)){
			throw new RuntimeException("comparing different types");
		}
		SetTransaction so = (SetTransaction) o;
		@SuppressWarnings("unused")
		SortedSet<Integer> ocontent = (SortedSet)so.getContent();
		throw new RuntimeException("comparing not yet implemented");
	}
	public String toString(){
		return content.toString();
	}
	public int hashCode(){
		return content.hashCode();
	}
	
}
