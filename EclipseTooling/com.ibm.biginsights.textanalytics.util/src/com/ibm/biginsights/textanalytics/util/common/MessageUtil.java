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
package com.ibm.biginsights.textanalytics.util.common;

/**
 * Provides utilities to format messages
 * 
 *  Krishnamurthy
 *
 */
public class MessageUtil {



	/**
	 * Store indices in a constant so that the index string {n} need not be built everytime.
	 * Currently, supports up to 10 parameters.
	 */
	private static final String[] paramIndices = new String[] {
		"{0}","{1}","{2}","{3}","{4}",
		"{5}","{6}","{7}","{8}","{9}"
	};
	
	public static String formatMessage(String message, String... paramValues){
		if(StringUtils.isEmpty(message)){
			return "";
		}
		StringBuilder buf = new StringBuilder(message);
		if(paramValues == null){
			return buf.toString();
		}
		
		int start = 0;
		int end = 0;
		for (int i = 0; i < paramValues.length; i++) {
			start = buf.indexOf(paramIndices[i]);
			if(start >=0){
				end = start + 3;//add offset 3, one each for {,n,}
				buf.replace(start, end, paramValues[i]);
			}
		}
		return buf.toString();
	}
}
