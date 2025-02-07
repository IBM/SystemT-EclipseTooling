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

/**
 * like DefaultWordIntegerMapping but reducing the alphabet by
 * 	- ignoring case
 *  - mapping all sequences of length > 1 containing an &, <, >, = or � to "<MARKUP/>"
 *  - map all numbers to <YEAR/> if between 1700 and 2199 (also if surrounded by [[]]) and otherwise to <NUMBER/>
 *  - : do something about: Township''' or ''The  (but mind 's)
 *  TODO: replace links of the form [[March 17]] by <DATE/>
 * 
 *  Blohm (blohm@aifb.uni-karlsruhe.de)
 *
 */
public class NoMarkupWordIntegerMapping extends DefaultWordIntegerMapping  implements WordIntegerMapping{



	public final static String MARKUP = "{markup}";
	public final static String YEAR = "{year}";
	public final static String NUMBER = "{number}";
	public final static String yearPattern = "(\\[\\[)?((1[789])|(2[01]))\\d\\d(\\]\\])?";
	public final static String numberPattern = "[0-9.]*";
	public final static String markupPattern = "";
	protected boolean eliminateMarkup = true;
	
	public NoMarkupWordIntegerMapping(boolean eliminateMarkup){
		intForWord(MARKUP); // make sure, markup is the first word
		intForWord(NUMBER);
		intForWord(YEAR);
		this.eliminateMarkup = eliminateMarkup;
	}
	
	public int intForWord(String s){
		s = normalizePrime(s,eliminateMarkup);
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
	public static String normalize(String s){
		String result = normalizePrime(s,true);
//		if(result.equals(MARKUP)) System.out.println("replacing "+s+" with <MARKUP/>");
//		if(result.equals(NUMBER)) System.out.println("replacing "+s+" with <NUMBER/>");
//		if(result.equals(YEAR)) System.out.println("replacing "+s+" with <YEAR/>");
		return result;
	}
	protected static String normalizePrime(String s, boolean eliminateMarkup){
		if(s.length()==1) return s;
		if(s.equals(MARKUP))return MARKUP;
		if(s.equals(NUMBER))return NUMBER;
		if(s.equals(YEAR))return YEAR;
		s = s.toLowerCase();
		s = s.trim();
		//TODO: pre-compile regex
		if(s.matches(yearPattern)){
			return YEAR;
		}
		if(s.matches(numberPattern)){
			return NUMBER;
		}
		if(!eliminateMarkup) return s;
		if(s.contains("<")) return MARKUP;
		if(s.contains(">")) return MARKUP;
		if(s.contains("&")) return MARKUP;
		if(s.contains("{")) return MARKUP;
		if(s.contains("}")) return MARKUP;
		if(s.contains("=")) return MARKUP;
		if(s.equals("'s")) return "'s";
		if(s.equals("I'm")) return "I'm";
		while(s.charAt(0)=='\''){
			s = s.substring(1);
			if(s.length()==0) return MARKUP;
		}
		while(s.charAt(0)=='*'){
			s = s.substring(1);
			if(s.length()==0) return MARKUP;
		}
		while(s.charAt(s.length()-1)=='\''){
			s = s.substring(0,s.length()-1);
			if(s.length()==0) return MARKUP;
		}
		
		if(s.endsWith("'s")) return s;
		if(s.endsWith("'re")) return s;
		if(s.contains("'")) return MARKUP;
		if(s.contains("*")) return MARKUP;
		if(s.matches(".*\\dpx.*")) return MARKUP;
		
		return s;
		
	}

}
