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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * 
 * 
 * Extends the Properties class by two features:
 * <ul>
 *   <li>String[] args can be handed on (assumed to be the command line arguments). All entries of the form -Vkey=value will
 *   lead to putting key=value.
 *   <li> values can be specified as predicates. In particular one can give key= <=5 >=3. Methods are provided to check for 
 *   values to satisfy the predicates. Semantics: = is always added as an alternative (=x =y will lead to two possible values), 
 *   >x <y will be alternative if x is greater than y and conjunctive otherwise. If several lower or upper limits are specified,
 *   the highest lower and lowest upper limit are taken.
 * </ul>
 * 
 * 
 *
 */
public class ExperimentProperties extends Properties {


	
	private static final long serialVersionUID = 3949209786742931133L;
	private static Pattern predicatePattern = Pattern.compile("((<)|(<=)|(>)|(>=)|(=)|())\\s*([0-9dDE\\.]+)"); //making use of the fact that + is greedy
	
	private LangCode language = LangCode.en;
	private TokenizerConfig tokConfig = null;
	private File aogFile = null;
	private HashMap<String, ArrayList<String>> replaceEntityHashMap;
	private String rootDir = null;
	private String sequenceDB = null;
	
	public LangCode getLanguage(){
		return language;
	}
	
	public void setLanguage(LangCode lang){
		this.language = lang;
	}
	
	public void setLanguage(String lang){
		LangCode temp = LangCode.strToLangCode(lang);
		this.language = temp;
	}
	
	public void setTokenizerConfig(TokenizerConfig tok){
		this.tokConfig = tok;
	}
	
	public TokenizerConfig getTokenizerConfig(){
		return tokConfig;
	}
	
	/**
	 * Set to run a specific AOG rather than from the AQL query
	 * @param aog
	 */
	public void setAogFile(File aog){
		this.aogFile = aog;
	}
	
	/**
	 * Returns the AOG file that was used to run PD
	 * @return
	 */
	public File getAogFile(){
		return this.aogFile;
	}
	
	/**
	 * Get the root directory
	 * @return
	 */
	public String getRootDir(){
		return this.rootDir;
	}
	
	/**
	 * Set the root directory
	 * @param rootDir
	 */
	public void setRootDir(String rootDir){
		this.rootDir = rootDir;
	}
	
	/**
	 * Sets sequence database URL
	 * @param dbURL
	 */
	public void setSequenceDBURL(String dbURL){
		this.sequenceDB = dbURL;
	}
	
	/**
	 * Returns sequence database URL
	 * @return
	 */
	public String getSequenceDBURL(){
		return this.sequenceDB;
	}
	/**
	 * Will set the views and fields replace entity will pull from
	 * 
	 * @param entities
	 */
	public void setReplaceEntityHashMap(HashMap<String, ArrayList<String>> entities){
		this.replaceEntityHashMap = entities;
	}
	/**
	 * Get the entity hash map used to run PD
	 * @return
	 */
	public HashMap<String, ArrayList<String>> getReplaceEntityHashMap(){
		return this.replaceEntityHashMap;
	}
	
	public void putArgs(String[] args){
		for (String arg : args) {
			if(arg.startsWith("-V")){
				String statement = arg.substring(2);
				int split = statement.indexOf(':');
				int split2 =statement.indexOf('=');
				if(split == -1 ||(split2>=0&&split2<split))split = split2; 
				if(split<0) continue;
				String key = statement.substring(0,split);
				String value = statement.substring(split+1,statement.length());
				this.put(key, value);
			}
		}
	}
	
	public boolean checkPredicate(String key, double value){
		return checkPredicate(parse(this.getProperty(key)), value);
	}
	public boolean checkPredicate(ParseResult result, double value){
		if((result.lessThan < result.greaterThan)&&(value < result.lessThan || value > result.greaterThan)) return true;
		if((result.lessThan >= result.greaterThan)&&(value < result.lessThan && value > result.greaterThan)
				&&(result.lessThan!=Double.MAX_VALUE||result.lessThan!=Double.MIN_VALUE)) return true;
		return (arrayCheck(result.mayEqual,value));
	}
	private boolean arrayCheck(double[] a, double v){
		for (double d : a) {
			if (d==v) return true;
		}
		return false;
	}
	public double getLessThan(String key){
		return parse(this.getProperty(key)).lessThan;
	}
	public double getGreaterThan(String key){
		return parse(this.getProperty(key)).greaterThan;
	}
	public double[] mayEqual(String key){
		return parse(this.getProperty(key)).mayEqual;
	}

  public ParseResult parse (String minValue, String maxValue)
  {
    String combinedValue = "";

    if (!StringUtils.isEmpty (minValue)) {
      combinedValue += String.format (">=%s", minValue);
    }
    if (!StringUtils.isEmpty (maxValue)) {
      combinedValue += String.format ("<=%s", maxValue);
    }

    return parse (combinedValue);
  }

	public ParseResult parse(String value){
		List<Double>mayEqual = new ArrayList<Double>();
		ParseResult result = new ParseResult();
		//regex match for individual statements
		Matcher matcher = predicatePattern.matcher(value);
		while(matcher.find()){
			//iterate over them and aggregate
			String operator = matcher.group(1);
			double number = 0;
			try {
				number = Double.parseDouble(matcher.group(matcher.groupCount()));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if(operator.startsWith(">")){
				result.greaterThan = Math.max(result.greaterThan, number);
			}
			if(operator.startsWith("<")){
				result.lessThan = Math.min(result.lessThan, number);
			}
			if(operator.endsWith("=")||operator.length()==0){
				mayEqual.add(number);
			}
		}
		result.mayEqual = new double[mayEqual.size()];
		for (int i = 0; i < result.mayEqual.length; i++) {
			result.mayEqual[i] = mayEqual.get(i);
		}
		return result;
	}
	
	public class ParseResult{
		public double lessThan = Double.MAX_VALUE;
		public double greaterThan = Double.MIN_VALUE;
		public double[] mayEqual = new double[0];
		public double upperBound(){
			double result = lessThan;
			for (double v : mayEqual) {
				result = Math.max(v+0.000001, result);
			}
			return result;
		}
		public double lowerBound(){
			double result = greaterThan;
			for (double v : mayEqual) {
				result = Math.min(v-0.0000001, result);
			}
			return result;
		}

	}
}
