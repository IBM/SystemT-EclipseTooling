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
/**
 * 
 */
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;

/**
 * Hashes the string by respecting a dictionary of word the 
 * presence of which should not have an influence on the hash value.
 * 
 * Word boundaries are determined by a split expression which can be any regular expression
 * determining boundaries.
 * 
 * 
 *
 */
public class DictIgnoreHasher implements HashFactory {


	
	public final static String STANDARD_TOKEN_SPLIT = "\\W+";
	
	protected String splitEx;
	protected Pattern dictPattern;
	public DictIgnoreHasher(String[] dict, String splitEx){
		this.splitEx = splitEx;
		//compile dict to regex
		String regex = "";
		for (String word : dict) {
			if(regex.length()>0&&word.length()>0) regex +="|";
			regex +=regexEscape(word);
		}
		dictPattern = Pattern.compile(regex); 
	}

	/* (non-Javadoc)
	 * @see com.ibm.avatar.discovery.groupby.HashFactory#hash(java.lang.String)
	 */
	public int hash(String s) {
		//split
		String[] tokens = toTokens(s);
		int h = 0;
		//iterate over elements
		for (String token : tokens) {
			//test dictionary membership
			if(ignorable(token))continue;
			//if appropriate, add hash score
		    h = 31*h + token.hashCode();
		}
		return h;
	}
	
	public Collection<Integer> getSeqSet(String s, ExperimentProperties properties,
			List<String[]> toWriteList) {
		//split
		String[] tokens = toTokens(s);
		Collection<Integer> sSet = new ArrayList<Integer>();
		//iterate over elements
		for (String token : tokens) {
			//test dictionary membership
			if(ignorable(token))continue;
			//if appropriate, add hash score
		    sSet.add(token.hashCode());
		}
		return sSet;
	}
	
	public String[] toTokens(String s){
		String[] tokens = s.split(splitEx);
		return tokens;
	}
	
	private boolean ignorable(String s){
		Matcher matcher = dictPattern.matcher(s);
		return matcher.matches();
	}
	/**
	 * Note: this is incomplete and untested
	 * @param s
	 * @return
	 */
	protected String regexEscape(String s){
		s = s.replaceAll("\\\\", "\\\\");
		s = s.replaceAll("\\[", "\\\\");
		s = s.replaceAll("\\]", "\\\\");
		s = s.replaceAll("\\*", "\\*");
		s = s.replaceAll("\\+", "\\+");
		s = s.replaceAll("\\.", "\\.");
		return s;
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
