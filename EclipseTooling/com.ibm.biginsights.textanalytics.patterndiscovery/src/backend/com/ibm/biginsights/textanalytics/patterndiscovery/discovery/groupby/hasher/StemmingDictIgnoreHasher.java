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
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Uses the Lucene implementation of the Porter Stemmer for hash computation.
 * 
 *
 */
public class StemmingDictIgnoreHasher extends DictIgnoreHasher {



	public StemmingDictIgnoreHasher(String[] dict, String splitEx) {
		super(dict,splitEx);
		//compile dict to regex
		String regex = "";
		for (String word : dict) {
			if(regex.length()>0&&word.length()>0) regex +="|";
			regex +=regexEscape(porterStem(word));
		}
		dictPattern = Pattern.compile(regex); 
	}
	/* (non-Javadoc)
	 * @see com.ibm.avatar.discovery.groupby.DictIgnoreHasher#toTokens(java.lang.String)
	 */
	@Override
	public String[] toTokens(String s){
		String[] tokens = null;
		try {
			Reader reader = new StringReader(s);
			PorterStemFilter filter = new PorterStemFilter(new LowerCaseTokenizer(org.apache.lucene.util.Version.LUCENE_31,reader));
			CharTermAttribute termAttr = filter.getAttribute(CharTermAttribute.class);
			
			ArrayList<String> tokenList = new ArrayList<String>();
			
			while (filter.incrementToken()){
				tokenList.add(termAttr.toString());
			}
			tokens = tokenList.toArray(new String[tokenList.size()]);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return tokens;
	}
	String porterStem(String s){
		try {
			Reader reader = new StringReader(s);
			PorterStemFilter filter = new PorterStemFilter(new LowerCaseTokenizer(org.apache.lucene.util.Version.LUCENE_31,reader));
			CharTermAttribute termAttr = filter.getAttribute(CharTermAttribute.class);
			String result = "";
			
			while (filter.incrementToken()){
				if(result.length()>0) result += " ";
				result += termAttr.toString();
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
