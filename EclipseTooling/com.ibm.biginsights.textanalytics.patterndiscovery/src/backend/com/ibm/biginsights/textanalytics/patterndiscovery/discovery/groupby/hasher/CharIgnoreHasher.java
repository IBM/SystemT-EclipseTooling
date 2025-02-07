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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;


/**
 * Hasher that ignores a given set of characters during hash score computation.
 * 
 * 
 *
 */
public class CharIgnoreHasher implements HashFactory {


	
	char[]ignore;
	public CharIgnoreHasher(char[] ignore){
		Arrays.sort(ignore);
		this.ignore = ignore;
	}
	
	private boolean ignorable(char c){
		for (char ignoreChar : ignore) {
			if(c< ignoreChar) return false;
			if(c==ignoreChar) return true;
		}
		return false;
	}
	/* (non-Javadoc)
	 * @see com.ibm.avatar.discovery.groupby.HashFactory#hash(java.lang.String)
	 */
	public int hash(String s) {
		// cf. http://www.informatics.susx.ac.uk/courses/dats/notes/html/node114.html
		int h = 0;
		for (char c : s.toCharArray()) {
			if(ignorable(c))continue;
		    h = 31*h + c;
		}
		return h;

	}
	
	public Collection<Integer> getSeqSet(String s, ExperimentProperties properties,
			List<String[]> toWriteList) {
		int h = 0;
		Collection<Integer> sSet = new ArrayList<Integer>();
		for (char c : s.toCharArray()) {
			if(ignorable(c))continue;
		    h = 31*h + c;
		    sSet.add(Character.digit(c, 10));
		}
		return sSet;
	}

	@Override
	public int hashScore(Collection<Integer> sSet) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<Integer> getSeqSet(String s,
			ExperimentProperties properties, CSVWriter writer)
			{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hash(String s, ExperimentProperties properties, CSVWriter writer)
			throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	

}
