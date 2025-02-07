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
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.Transaction;


/**
 * Represents a transaction instance for a scenario where transactions 
 * are modelled as simple sets.
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
@SuppressWarnings("unchecked")
public class SequenceTransaction implements Transaction, Comparable{



	protected List<Integer> content;
	public SequenceTransaction(Collection<Integer> content){
		this.content = new LinkedList<Integer>(content);
	}
	public Collection<Integer> getContent(){
		return content;
	}
	public boolean equals(Object o){
		if (! (o instanceof SequenceTransaction)){
			return false;
		}
		SequenceTransaction so = (SequenceTransaction) o;
		return this.content.equals(so.content);
	}
	public int compareTo(Object o) {
		if (! (o instanceof SequenceTransaction)){
			throw new RuntimeException("comparing different types");
		}				
		throw new RuntimeException("comparing not yet implemented");
	}
	public String toString(){
		return content.toString();
	}
	public int hashCode(){
		return content.hashCode();
	}
	
}
