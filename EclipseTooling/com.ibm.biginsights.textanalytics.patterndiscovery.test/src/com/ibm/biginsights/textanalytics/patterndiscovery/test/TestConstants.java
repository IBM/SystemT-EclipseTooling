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
package com.ibm.biginsights.textanalytics.patterndiscovery.test;

public class TestConstants {
	
	public static final String TEST_WORKING_DIR;

	static {
		// Use the user-specified working directory if possible.
		if (null != System.getProperty(TestUtils.TEST_DIR_PROPNAME)) {
			TEST_WORKING_DIR = System.getProperty(TestUtils.TEST_DIR_PROPNAME);
		} else {
			TEST_WORKING_DIR = "../";
		}
	}

	public static final String TESTDATA_DIR = TEST_WORKING_DIR + "/TestData/JUnitWorkspace/PD_Backend";

	public static final String PROPERTIES_DIR = TESTDATA_DIR + "/properties";
	
	public static final String AOG_DIR = TESTDATA_DIR + "/aog";

	/** Directory containing AQL files for testing purposes. */
	public static final String AQL_DIR = TESTDATA_DIR + "/aql";
	
	public static final String AQL_INCLUDES_DIR = TESTDATA_DIR;

	public static final String TEST_DOCS_DIR = TESTDATA_DIR + "/Datasets";

	public static final String DUMPS_DIR = TESTDATA_DIR + "/Datasets";

	/** A single-document input file, for memory profiling. */
	public static final String ENRON_1_DUMP = DUMPS_DIR + "/enron1.del";

	public static final String ENRON_100_DUMP = DUMPS_DIR + "/enron100.del";

	public static final String ENRON_1K_DUMP = DUMPS_DIR + "/enron1k.del";

	/**
	 * Dump file containing the first 10000 documents of the Enron data set;
	 * created by {@link CreateEnronDumps#main(String[])}
	 */
	public static final String ENRON_10K_DUMP = DUMPS_DIR + "/enron10k.del";

	public static final String ENRON_37939_DUMP = DUMPS_DIR + "/enron38k.del";

	/**
	 * Zip archive containing a sample of the enron dataset; note that this is a
	 * *different* sample from the one in {@link #ENRON_37939_DUMP}.
	 */
	public static final String ENRON_SAMPLE_ZIP = DUMPS_DIR + "/ensample.zip";

	/**
	 * Sub-sample of {@link #ENRON_SAMPLE_ZIP}
	 */
	public static final String ENRON_SMALL_ZIP = DUMPS_DIR + "/ensmall.zip";

	/**
	 * Tarfile of four HTML documents from imdb.com.
	 */
	public static final String IMDB_TARFILE = DUMPS_DIR + "/imdb.tar.gz";


}
