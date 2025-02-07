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
package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.sequences.nested;

import java.util.Collection;
import java.util.LinkedList;

import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.Pattern;
import com.ibm.biginsights.textanalytics.patterndiscovery.fpm.apriori.Transaction;


/**
 * Represents a pattern instance for a scenario where transactions 
 * are modelled as simple sets.
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
@SuppressWarnings("unchecked")
public class SequencePattern extends SequenceTransaction implements Pattern {



	public double support = 0;

	public SequencePattern(Collection content) {
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

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.Pattern#subsumes(edu.unika.aifb.fpm.apriori.Pattern)
	 */
	public boolean subsumes(Pattern p) {
		SequencePattern sp = (SequencePattern) p;
		return sp.content.containsAll(this.content);
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.Pattern#subsumes(edu.unika.aifb.fpm.apriori.Transaction)
	 */
	public boolean subsumes(Transaction t) {
		throw new RuntimeException(
				"subsume-test should not be needed when doing support-count via the trie.");
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.sets.SetTransaction#compareTo(java.lang.Object)
	 */
	//@Override
	public int compareTo(Object o) {
		if (!(o instanceof SequencePattern)) {
			throw new RuntimeException("comparing different types");
		}
		// compare is based on support value
		// highest support value should be on first postion
		SequencePattern foreignSetPattern = (SequencePattern) o;
		if (foreignSetPattern.getSupport() < this.getSupport())
			return -1;
		return 1;
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.apriori.sets.SetTransaction#equals(java.lang.Object)
	 */
	//@Override
	public boolean equals(Object o) {
		if (!(o instanceof SequencePattern)) {
			throw new RuntimeException("comparing different types");
		}
		SequencePattern foreignSetPattern = (SequencePattern) o;
		if (foreignSetPattern.getSupport() == this.getSupport())
			return true;
		return false;
	}

	int depth = 0;
	int aktualElementPointer = 0;

	public LinkedList moveRight(LinkedList pattern) {
		depth = 0;
		/** 
		 * TODO: change this! The object should be changed via the object as return parameter
		 * 
		 */
		aktualElementPointer = returnFirstElement(pattern);
		return pattern;
	}


	private Integer returnFirstElement(LinkedList pattern) {
		Integer firstInteger = -1;
		if(pattern.size() < 1) return firstInteger;		
		if (pattern.getFirst() instanceof LinkedList) {
			LinkedList first_PatternPart = (LinkedList) pattern.getFirst();
			depth = depth +1;
			firstInteger = returnFirstElement(first_PatternPart);
            if(first_PatternPart.size() == 0) {
            	pattern.remove();
            	depth = depth -1;
            }
		} else {
			if (pattern.getFirst() instanceof Integer) {
				firstInteger = (Integer) pattern.getFirst();				
				pattern.remove();
				if (pattern.size() == 0) pattern = null;
				return firstInteger;
			}
		}
		return firstInteger;
	}
	

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return this.depth;
	}

	/**
	 * @param depth the depth to set
	 */
	public void setDepth(int depth) {
		this.depth = depth;
	}

}
