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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.goldenStandard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.IPDLog;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PDLogger;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;

public class Statistics {



	private static final IPDLog logger = PDLogger.getLogger("Statistics");

	public static void calculatePrecision(DebugDBProcessor db,
			ExperimentProperties properties) throws SQLException, IOException {
		logger.info("Calculating Grouping Statistics");

		// initialize directory structure
		String baseDir = properties
				.getProperty(PropertyConstants.FILE_ROOT_DIR);
		String inputFile = properties
				.getProperty(PropertyConstants.INPUT_DOCUMENT_NAME);
		if (inputFile.contains("."))
			inputFile = inputFile.substring(0, inputFile.indexOf('.'));
		baseDir = baseDir + inputFile + File.separator;

		String outputDir = baseDir
				+ properties.getProperty(PropertyConstants.POST_PROCESS_DIR);

		String query = "SELECT * FROM goldenJaccard";
		logger.fine(query);
		ResultSet grouping = db.readFromDB(query);

		HashMap<String, Integer[]> storeCounts = new HashMap<String, Integer[]>();

		HashMap<String, Integer[]> storeJCounts = new HashMap<String, Integer[]>();

		int yesTotal = 0;
		int noTotal = 0;
		int partialTotal = 0;

		while (grouping.next()) {

			int hash = grouping.getInt("hashID");
			int groupCount = grouping.getInt("count");
			String sequence = grouping.getString("sequence");
			String jsequence = grouping.getString("jsequence");
			String isRelated = grouping.getString("isRelated");
			int jhash = grouping.getInt("jhashID");
			String key = hash + "," + sequence + "," + groupCount;
			String key2 = jhash + "," + jsequence;

			if (storeCounts.containsKey(key)) {
				Integer[] count = tallyLabel(storeCounts.get(key), isRelated);
				storeCounts.put(key, count);
			} else {
				Integer[] empty = { 0, 0, 0 };
				Integer[] count = tallyLabel(empty, isRelated);
				storeCounts.put(key, count);
			}

			if (storeJCounts.containsKey(key2)) {
				Integer[] jcount = tallyLabel(storeJCounts.get(key2), isRelated);
				storeJCounts.put(key2, jcount);
			} else {
				Integer[] empty = { 0, 0, 0 };
				Integer[] jcount = tallyLabel(empty, isRelated);
				storeJCounts.put(key2, jcount);
			}
		}

		CSVWriter writer = new CSVWriter(new OutputStreamWriter(
				new FileOutputStream(outputDir + "SignatureStats.csv"),
				GroupByNewProcessor.ENCODING), ',', '"');

		for (String hashSeq : storeCounts.keySet()) {
			String[] hashSeqAr = hashSeq.split(",");
			String hashID = hashSeqAr[0];
			String sequence = hashSeqAr[1];
			String groupCount = hashSeqAr[2];

			Integer[] counts = storeCounts.get(hashSeq);
			yesTotal = yesTotal + counts[0];
			noTotal = noTotal + counts[1];
			partialTotal = partialTotal + counts[2];

			Double precision = calcPrecision(counts[0], counts[1], counts[2]);

			String[] line = { groupCount, hashID, sequence,
					Double.toString(precision) };
			writer.writeNext(line);
		}

		Double totalPrecision = calcPrecision(yesTotal, noTotal, partialTotal);
		String[] finalStats = { "Total Precision",
				Double.toString(totalPrecision) };

		writer.writeNext(finalStats);
		writer.flush();
		writer.close();

		CSVWriter writerJ = new CSVWriter(new OutputStreamWriter(
				new FileOutputStream(outputDir + "JSignatureStats.csv"),
				GroupByNewProcessor.ENCODING), ',', '"');

		for (String jsequence : storeJCounts.keySet()) {
			String[] jhashSeqAr = jsequence.split(",");

			Integer[] counts = storeJCounts.get(jsequence);
			String jhashID = jhashSeqAr[0];
			String jsequenceStr = jhashSeqAr[1];

			Double precision = calcPrecision(counts[0], counts[1], counts[2]);

			String[] line = { jhashID, jsequenceStr, Double.toString(precision) };
			writerJ.writeNext(line);
		}

		writerJ.writeNext(finalStats);
		writerJ.flush();
		writerJ.close();
		if(Constants.DEBUG)
			System.out.println("Done with basic group stats");

	}

	private static Integer[] tallyLabel(Integer[] count, String label) {

		int yes = count[0];
		int no = count[1];
		int partial = count[2];

		if (label.equalsIgnoreCase("yes")) {
			yes++;
		} else if (label.equalsIgnoreCase("no")) {
			no++;
		} else if (label.equalsIgnoreCase("partially")) {
			partial++;
		}
		Integer[] toReturn = { yes, no, partial };
		return toReturn;
	}

	public static double calcPrecision(int yes, int no, int partial) {
		double precision = 0.0;
		double newYes = yes * 1.0;
		double newNo = no * 1.0;
		double newPartial = partial * 1.0;

		precision = newYes / (newYes + newNo + newPartial);

		return precision;
	}

}
