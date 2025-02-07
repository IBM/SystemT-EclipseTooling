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

/**
 * Holds two of a kind
 * 
 * 
 *
 */
@SuppressWarnings("unchecked")
public class Pair<T> {



	public T e1;
	public T e2;
	public Pair(T e1, T e2){
		this.e1 = e1;
		this.e2 = e2;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	//@Override
	public boolean equals(Object o){
		if(! (o instanceof Pair)) return false;
		Pair p2 = (Pair) o;
		return e1.equals(p2.e1) && e2.equals(p2.e2);
	}
	//@Override
	public int hashCode(){
		return e1.hashCode()+3*e2.hashCode();
	}
	//@Override
	public String toString(){
		return "("+e1+", "+e2+")";
	}

}
