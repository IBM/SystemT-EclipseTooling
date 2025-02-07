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


/**
 * 
 *
 */
public class IntPair{


	
	public int x;
	public int y;
	public IntPair(int x,int y){
		this.x = x;
		this.y = y;
	}
	//@Override
	public int hashCode(){
		//int result = y+ 0x9e3779b9 + (x<<6)+(x>>2);
		//return x^y;
		return y+ 0x9e3779b9 + (x<<6)+(x>>2);
		//return x + 5423*y;
	}
	//@Override
	public boolean equals(Object o){
		if(!(o instanceof IntPair)) return false;
		IntPair p = (IntPair) o;
		return p.x==x &&p.y==y;
	}
	//@Override
	public String toString(){
		return "("+x+","+y+")";
	}
}
