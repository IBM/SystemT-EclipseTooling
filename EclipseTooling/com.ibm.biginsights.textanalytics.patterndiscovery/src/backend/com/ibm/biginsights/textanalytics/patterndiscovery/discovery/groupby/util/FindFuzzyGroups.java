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

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.errors.PatternDiscoveryException;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.HashFactory;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.hasher.RuleBasedHasher;

/**
 * Accepts dumps in csv format. Outputs lines that have an equal value in one
 * ("pivot") column but have distinct values in other ("compare") columns.
 * Assumes the file to be sorted by pivot.
 * 
 * Syntax FindFuzzyGroups infile outfile pivot compare(compare format: 1,2,3)
 * 
 * 
 * 
 * 
 */
public class FindFuzzyGroups {



	private static HashMap<Integer, String> sequence2StringCache = new HashMap<Integer, String>();

	/**
	 * @param properties
	 * @param inFile
	 * @param outFile
	 * @param pivot
	 * @param compare
	 * @param ruleFile
	 *            may be null, if summary generation is not needed.
	 * @param testHash
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws SQLException
	 * @throws PatternDiscoveryException
	 */
	public static void findFuzzy(ExperimentProperties properties, String inFile, String outFile, int pivot, String compare, RuleBasedHasher summaryHasher,
			HashFactory testHash) throws PatternDiscoveryException {
		try {
			DebugDBProcessor db = new DebugDBProcessor(properties.getProperty(PropertyConstants.DB_PREFIX) + properties.getRootDir()
					+ properties.getProperty(PropertyConstants.SEQUENCE_DB_NAME));

			db.setProperties(properties); // Pass in information to store txt
											// files and database
			String separator = GroupByNewProcessor.COL_SEPARATOR;

			// String separator = properties.getProperty("separator");
			CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(inFile), GroupByNewProcessor.ENCODING), ',', '"');

			String[] fieldSplit = compare.split(",");
			int[] fieldNums = new int[fieldSplit.length];
			for (int i = 0; i < fieldSplit.length; i++) {
				String j = fieldSplit[i];
				fieldNums[i] = Integer.parseInt(j);
			}
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), GroupByNewProcessor.ENCODING));

			String lastPivot = "";
			String[] record = reader.readNext();
			String[] groupLine = record;

			boolean fuzzyGroup = false; // becomes true when within a fuzzy
										// group. In that case the previous line
										// does not need to be output

			CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outFile + ".csv"), GroupByNewProcessor.ENCODING), ',', '"');

			while (record != null) {
				if (!lastPivot.equals(record[pivot])) {
					lastPivot = record[pivot];
					groupLine = record;
					fuzzyGroup = false;
				} else {
					if (!equivalent(groupLine, record, separator, fieldNums, testHash, properties)) {
						// if this is the first output with this hash
						String toHash = "";
						for (int i : fieldNums) {
							if (toHash.length() > 0)
								toHash += " " + GroupByNewProcessor.COL_SEPARATOR + " ";
							toHash += record[i];
						}
						if (!fuzzyGroup) {
							// write a summary
							if (summaryHasher != null) {
								bw.write("== " + summary(summaryHasher.computeS(toHash, properties, writer), db) + " ==\n");
							} else {
								bw.write("== next group ===");
							}
							// write the first line
							// bw.write(toOneString(groupLine)+"---"+summary(summaryHasher.computeS(toHash),
							// db)+"\n");
							bw.write(toOneString(groupLine) + "\n");
						}
						bw.write(toOneString(record) + "\n");
						groupLine = record;
						fuzzyGroup = true;
					}
				}
				record = reader.readNext();
			}
			reader.close();
			bw.flush();
			bw.close();
			db.shutdown ();
		} catch (IOException e) {
			throw new PatternDiscoveryException(e, ErrorMessages.PATTERN_DISCOVERY_FUZZY_GROUPING_WRITE_ERR);
		}
	}

	private static String summary(Collection<Integer> computeS, DebugDBProcessor db) throws PatternDiscoveryException {
		String result = "";
		for (Integer id : computeS) {
			result += FindFuzzyGroups.sequence2String(id, db) + ", ";
		}
		return result;
	}

	private static String toOneString(String[] groupLine) {
		String result = "";
		for (String string : groupLine) {
			if (result.length() > 0)
				result += ",";
			result += "\"" + string + "\"";
		}
		result = result.replace("\n", " ");
		return result;
	}

	private static boolean equivalent(String[] line1, String[] line2, String separator, int[] fieldNums, HashFactory hasher, ExperimentProperties properties)
			throws IOException, PatternDiscoveryException {

		String toHash1 = "";
		String toHash2 = "";
		for (int i : fieldNums) {
			if (toHash1.length() > 0)
				toHash1 += " " + separator + " ";
			if (toHash2.length() > 0)
				toHash2 += " " + separator + " ";
			toHash1 += line1[i];
			toHash2 += line2[i];
		}
		return (hasher.hash(toHash1, properties, null) == hasher.hash(toHash2, properties, null));
	}

	public static String sequence2String(int sequenceID, DebugDBProcessor db) throws PatternDiscoveryException {
		String sequenceQueryTemplate = "SELECT dict.surface FROM SEQUENCES sequence, DICTIONARY dict WHERE sequence.wordid=dict.wordid AND sequence.sequenceID=%d ORDER BY sequence.pos";
		// cache
		if (sequence2StringCache.size() > 65000)
			sequence2StringCache.clear();
		String result = sequence2StringCache.get(sequenceID);
		if (result != null)
			return result;
		result = "";
		String sql = String.format(sequenceQueryTemplate, sequenceID);
		try {
			ResultSet rs = db.readFromDB(sql);
			result += rs2Line(rs, " ") + "(" + sequenceID + ")";
		} catch (SQLException e) {
			throw new PatternDiscoveryException(e, ErrorMessages.PATTERN_DISCOVERY_LOAD_SEQUENCES_MAPPING_ERR);
		}
		return result;
	}

	public static String rs2Line(ResultSet rs, String separator) throws SQLException {
		String result = "";
		while (rs.next()) {
			if (result.length() > 0)
				result += separator;
			result += rs.getString(1);
		}
		return result;
	}

}
