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
public interface WordIntegerMapping {

 public static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

	
	public void loadMapping(File file) throws IOException;
	
	public int intForWord(String s);
	public String wordForInt(int integer);

	/**
	 * @return the nextInt
	 */
	public int getNextInt();
	public int getAlphabetSize();
	
	/**
	 * size of mapping array
	 */
	public int size();
}
