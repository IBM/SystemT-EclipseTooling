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

package com.ibm.biginsights.textanalytics.patterndiscovery.fpm.nlp;

import java.io.File;
import java.io.IOException;

/**
 * Reduces the alphabet by using only those words of a loaded alphabet 
 * that are actually present in the corpus. To this end it has a present method
 * that allows presenting a given word. When reduce is called, all words not presented
 * since the last reset are deleted. 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
public class CompactWordIntegerMapping extends NoMarkupWordIntegerMapping {


	
	protected Integer[] intForWordIndex;
	protected Integer[] wordForIntIndex;
	protected Boolean[] seen;
	
	public CompactWordIntegerMapping(boolean eliminateMarkup){
		super(eliminateMarkup);
		int size = (int)Math.max(200000,super.getAlphabetSize()*2);
		seen = new Boolean[size];
	}

	public int intForWord(String s) {
		s = normalizePrime(s,eliminateMarkup);
		if(intForWordIndex== null)return super.intForWord(s);
		int index = super.intForWord(s);
		if (index >= intForWordIndex.length)
			throw new RuntimeException("alphabet should not be extended after reduction");
		if(intForWordIndex[index]==-1)
			throw new RuntimeException("'"+s+ "' is not in reduced alphabet");
		return intForWordIndex[index];
	}

	public void loadMapping(File file) throws IOException {
		super.loadMapping(file);
		reset();
	}

	public String wordForInt(int integer) {
		if(wordForIntIndex==null)return super.wordForInt(integer);
		return words.get(wordForIntIndex[integer]);
	}
	
	public void present(String s){
		seen[this.intForWord(s)] = true;		
	}
	public void reduce(){
		present(MARKUP);//as known constants they may be queried without being in the presented set.
		present(YEAR);
		present(NUMBER);
		intForWordIndex = new Integer[super.nextInt];
		int localIndex = 0;
		for (int i = 0; i < intForWordIndex.length; i++) {
			if(seen[i]!=null&&seen[i]){
				intForWordIndex[i]=localIndex++;
			}else{
				intForWordIndex[i]= -1;
			}
		}
		wordForIntIndex = new Integer[localIndex];
		for (int i = 0; i < intForWordIndex.length; i++) {
			if(intForWordIndex[i]>=0)
			wordForIntIndex[intForWordIndex[i]]= i;
		}
		//System.out.println("reduced alphabet size: "+localIndex);
	}
	public void reset(){
		int size = (int)Math.max(200000,super.getAlphabetSize()*2);
		seen = new Boolean[size];
		intForWordIndex = null;
		wordForIntIndex = null;
	}
	public int getAlphabetSize(){
		return wordForIntIndex.length;
	}
}
