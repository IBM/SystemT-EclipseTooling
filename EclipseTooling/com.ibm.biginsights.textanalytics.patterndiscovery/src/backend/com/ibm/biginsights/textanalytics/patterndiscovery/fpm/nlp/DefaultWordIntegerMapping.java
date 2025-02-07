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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;

/**
 * Maps words to integer numbers according to a frequency ranking that has previously
 * been loaded. 
 * 
 * loads a frequency ranking where each line contains a word with decreasing frequency optionally followed by a tab and a count which is not used.
 * resolves strings to integer using a HashMap and vice versa using an ArrayList
 * when queried for a word that is not in the mapping with the next free integer as number
 * 
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
public class DefaultWordIntegerMapping  implements WordIntegerMapping{



	protected HashMap<String,Integer> wordMap = new HashMap<String,Integer>();
	protected ArrayList<String>words = new ArrayList<String>();
	int nextInt = 0;
	
	public void loadMapping(File file) throws IOException{

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(file),
				GroupByNewProcessor.ENCODING));
		while(in.ready()){
			String line = in.readLine();
			String first = line.split("\t")[0];
			intForWord(first);
		}
		in.close();
		//System.out.println("Size of alphabet:"+nextInt);
	}
	
	public int intForWord(String s){
		Integer integer = wordMap.get(s);
		
		if(integer != null){
			return integer;
		}else{
			int newValue = nextInt++;
			wordMap.put(s, newValue);
			words.add(s);
			return newValue;
		}
	}
	public String wordForInt(int integer){
		return words.get(integer);
	}

	/**
	 * @return the nextInt
	 */
	public int getNextInt() {
		return nextInt;
	}
	public int getAlphabetSize(){
		return nextInt;
	}

	/* (non-Javadoc)
	 * @see edu.unika.aifb.fpm.nlp.WordIntegerMapping#size()
	 */
	//@Override
	public int size() {		
		return words.size();
	}
}
