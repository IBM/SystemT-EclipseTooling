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

import java.util.ArrayList;
import java.util.Collection;

import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.Pattern;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.Transaction;


/**
 * Represents a pattern instance for a scenario where transactions 
 * are modelled as simple sets.
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
public class SequencePattern extends SequenceTransaction implements Pattern {


	
	public double support = 0;

	public SequencePattern(Collection<Integer> content) {
		super(content);
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.Pattern#getSupport()
	 */
	public double getSupport() {
		return support;
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.Pattern#setSupport(double)
	 */
	public void setSupport(double support) {
		this.support = support;
	}

	/* Note: this is a rather naive implementation. Room for improvement there.
	 * @see edu.unika.aifb.fpm.apriori.Pattern#subsumes(edu.unika.aifb.fpm.apriori.Pattern)
	 */
	public boolean subsumes(Pattern p) {
		return subsumes((Transaction) p);
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.Pattern#subsumes(edu.unika.aifb.fpm.apriori.Transaction)
	 */
	public boolean subsumes(Transaction t) {
		//TODO: subsumption should be checked ina sequential manner!
		SequenceTransaction sp = (SequenceTransaction)t;
		ArrayList<Integer> superS = new ArrayList<Integer>(sp.content);
		ArrayList<Integer> subS = new ArrayList<Integer>(this.content);
		int subLength = subS.size();
		for(int matchStart = 0;matchStart <= superS.size()-subLength;matchStart++){
			int superX = superS.get(matchStart);
			int subX = subS.get(0);
			int superPos = 0;
			int subPos = 0;
			while(superX==subX){
				superPos++;
				subPos++;
				if(subPos>=subLength)return true;
				superX = superS.get(matchStart+superPos);
				subX = subS.get(subPos);
			}
		}
		return false;

	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.sets.SetTransaction#compareTo(java.lang.Object)
	 */
	//@Override
	public int compareTo(Object o) {
		if (! (o instanceof SequencePattern)){
			throw new RuntimeException("comparing different types");
		}
		// compare is based on support value
		// highest support value should be on first postion
		SequencePattern foreignSetPattern = (SequencePattern) o;
		if(foreignSetPattern.getSupport() < this.getSupport()) return -1;
		return 1;
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.sets.SetTransaction#equals(java.lang.Object)
	 */
	//@Override
	public boolean equals(Object o) {	
		if (! (o instanceof SequencePattern)){
			throw new RuntimeException("comparing different types");
		}
		SequencePattern foreignSetPattern = (SequencePattern) o;
		if(foreignSetPattern.getSupport() == this.getSupport()) return true;
		return false;
	}
	


}
