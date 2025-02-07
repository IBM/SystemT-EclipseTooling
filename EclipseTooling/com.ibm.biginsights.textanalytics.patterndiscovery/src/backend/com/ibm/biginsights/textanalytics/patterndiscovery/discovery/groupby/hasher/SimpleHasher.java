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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher;

import java.util.Collection;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;

/**
 * Uses the standard String.hashCode() method for hash computations.
 * 
 * 
 *
 */
public class SimpleHasher implements HashFactory {



	/* (non-Javadoc)
	 * @see com.ibm.avatar.discovery.groupby.HashFactory#hash(java.lang.String)
	 */
	public int hash(String s) {
		return s.hashCode();
	}
	
	public Collection<Integer> getSeqSet(String s, ExperimentProperties properties,
			CSVWriter writer) throws PatternDiscoveryException {
		RuleBasedHasher test;
		Collection<Integer> sSet = null;
		test = new RuleBasedHasher(null, null, false, properties);
		sSet = test.computeS(s, properties, writer);
		
		return sSet;
	}

	@Override
	public int hash(String s, ExperimentProperties properties,
			CSVWriter writer){
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int hashScore(Collection<Integer> sSet) {
		// TODO Auto-generated method stub
		return 0;
	}

}
