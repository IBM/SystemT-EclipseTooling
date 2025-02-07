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
package com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DebugDBProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties.ParseResult;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;

/**
 * 
 * 
 *         Scripts to compute correlation measures in the analysis database.
 * 
 * 
 * 
 *         Added in additional correlation measures and edits
 * 
 */
public class ComputeCorrelation {



	/**
	 * /** Computes the correlation between sequence occurrences in the data.
	 * The required counts are taken from a database.
	 * 
	 * With this signature, the method assumes a set of default queries on the
	 * databases.
	 * 
	 * @param properties
	 *            The ExperimentProperties
	 * @param measures
	 *            the identifiers for the correlation measures (currently mi:
	 *            Mutual Information, cxy: Uncertainty Coefficient on the first
	 *            entry, cyx Uncertainty Coefficient on the second entry, jc:
	 *            Jaccard distance, x2: Chi-Squared distance, re: redundancy
	 *            (normalized Mutual Information))
	 * @param targetDir
	 *            The results are stored in a file sequence_MM.csv in the
	 *            targetDir. MM stands for the code of the measure.
	 * @param minCo
	 *            . Only sequence pairs co-occurring at least minCo times are
	 *            considered.
	 * @param coOccurrenceQuery
	 *            columns: sequence 1, sequence 2, count
	 * @param matchCountQuery
	 *            colum: matchCount (=total number of transactions)
	 * @param sequenceCountQuery
	 *            columns: sequenceID, count
	 * @return The names of the files the information was stored to.
	 * @throws SQLException
	 * @throws IOException
	 */

	public static String[] computeMeasureSequenceSequence(ExperimentProperties properties, String[] measures, String targetDir, double minCo,
			String coOccurrenceQuery, String matchCountQuery, String sequenceCountQuery, Map<Integer, String> seqMap) throws SQLException, IOException {
		String dbUrl = properties.getProperty(PropertyConstants.DB_PREFIX) + properties.getRootDir()
				+ properties.getProperty(PropertyConstants.SEQUENCE_DB_NAME);
		ParseResult measureRange = properties.parse(properties.getProperty(PropertyConstants.CORRELATION_MEASURE_MIN),
		                                            properties.getProperty(PropertyConstants.CORRELATION_MEASURE_MAX));
		DebugDBProcessor db = new DebugDBProcessor(dbUrl);
		db.setProperties(properties); // Pass in information to store txt files
										// and database

		// pre-cache word counts
		if (Constants.DEBUG)
			System.out.println("pre-cache word counts");
		HashMap<Integer, Integer> sequenceCounts = new HashMap<Integer, Integer>();
		ResultSet wc = db.readFromDB(sequenceCountQuery);
		while (wc.next()) {
			int word = wc.getInt(1);
			int count = wc.getInt(2);
			sequenceCounts.put(word, count);
		}
		ResultSet cos = db.readFromDB(coOccurrenceQuery);
		ResultSet mc = db.readFromDB(matchCountQuery);
		mc.next();
		int matchCount = mc.getInt(1);
		mc.close();
		int writeCount = 0;

		// sad but true: derby is x times slower in inserting the data than
		// writing it to afile
		List<String> fileNames = new ArrayList<String>();
		HashMap<String, BufferedWriter> writers = new HashMap<String, BufferedWriter>();
		for (String measure : measures) {
			String fileName = targetDir + "/sequence_" + measure + ".csv";
			fileNames.add(fileName);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), GroupByNewProcessor.ENCODING));
			writers.put(measure, bw);
		}

		// Setup writers to print out debugging file information
		List<String> fileNamesDebug = new ArrayList<String>();
		HashMap<String, BufferedWriter> writersDebug = new HashMap<String, BufferedWriter>();
		if (properties.getProperty(PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase("true")) {
			for (String measure : measures) {
				String baseDir = properties.getProperty(PropertyConstants.FILE_ROOT_DIR);
				String inputFile = properties.getProperty(PropertyConstants.INPUT_DOCUMENT_NAME);
				if (inputFile.contains("."))
					inputFile = inputFile.substring(0, inputFile.indexOf('.'));
				String debugDir = baseDir + inputFile + File.separator + properties.getProperty(PropertyConstants.DEBUG_DIR);
				String fileName = debugDir + "/sequence_" + measure + "_debug.csv";
				fileNamesDebug.add(fileName);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), GroupByNewProcessor.ENCODING));
				writersDebug.put(measure, bw);
			}
		}

		while (cos.next()) {
			try {
				int word1 = cos.getInt(1);
				int word2 = cos.getInt(2);

				if (word1 == word2)
					continue;
				Integer w1CountInteger = sequenceCounts.get(word1);
				if (w1CountInteger == null)
					continue;
				int w1Count = w1CountInteger.intValue();
				if (w1Count > matchCount)
					w1Count = matchCount;// may happen because co-occurences can
											// be multiple per sequence
											// (currently problem is ignored)
				Integer w2CountInteger = sequenceCounts.get(word2);
				if (w2CountInteger == null)
					continue;
				int w2Count = w2CountInteger.intValue();
				if (w2Count > matchCount)
					w2Count = matchCount;// may happen because co-occurences can
											// be multiple per sequence
											// (currently problem is ignored)
				double co = cos.getDouble(3);

				// minCoocurrence threshold
				if (co < minCo)
					continue;
				for (String measure : measures) {
					if (writeCount == 0 && properties.getProperty(PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase("true")) {
						writersDebug.get(measure).write("seq1ID : seq1String, seq2ID : seq2String, Uncertainty Coefficient\n");
					}
					writeMeasure(measureRange, properties, word1, word2, measure, writers, matchCount, w1Count, w2Count, co, seqMap, writersDebug);
				}

				writeCount++;
				if (writeCount % 1000 == 0) {
					if (Constants.DEBUG)
						System.out.println("writeCount: " + writeCount);
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (Constants.DEBUG)
			System.out.println("done");

//		for (String measure : measures) {
//			writers.get(measure).close();
//			if (properties.getProperty(PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase("true")) {
//				writersDebug.get(measure).close();
//			}
//		}
		
		for(BufferedWriter wr : writers.values()){
			wr.flush();
			wr.close();
		}
		for(BufferedWriter wr : writersDebug.values()){
			wr.flush();
			wr.close();
		}

		return fileNames.toArray(new String[fileNames.size()]);
	}

	/**
	 * Writes the scores to a the writers
	 * 
	 * @param measureRange
	 * @param properties
	 * @param word1
	 * @param word2
	 * @param measure
	 * @param writers
	 * @param denominator
	 * @param countX
	 * @param countY
	 * @param countXandY
	 * @throws IOException
	 */
	private static void writeMeasure(ParseResult measureRange, ExperimentProperties properties, int word1, int word2, String measure,
			HashMap<String, BufferedWriter> writers, double denominator, double countX, double countY, double countXandY, Map<Integer, String> seqMap,
			HashMap<String, BufferedWriter> writersDebug) throws IOException {
		double value = Double.NaN;
		if ("mi".equals(measure)) {
			value = mutualInformation(denominator, countX, countY, countXandY);
		}
		if ("jc".equals(measure)) {
			value = jaccard(denominator, countX, countY, countXandY);
		}
		if ("x2".equals(measure)) {
			value = chiSquare(denominator, countX, countY, countXandY);
		}
		if ("re".equals(measure)) {
			value = redundancy(denominator, countX, countY, countXandY);
		}
		if ("cxy".equals(measure)) {
			value = uncertainCoefficient(denominator, countX, countY, countXandY);
		}
		if ("cyx".equals(measure)) {
			value = uncertainCoefficient(denominator, countY, countX, countXandY);
		}
		// measure threshold
		if (!properties.checkPredicate(measureRange, value))
			return;

		// Write to debug file
		if (properties.getProperty(PropertyConstants.ENABLE_DEBUGGING).equalsIgnoreCase("true")) {
			String word1str = seqMap.get(word1);
			String word2str = seqMap.get(word2);
			writersDebug.get(measure).write(word1 + " : " + word1str + "," + word2 + " : " + word2str + "," + value + "\n");
		}

		writers.get(measure).write(word1 + "," + word2 + "," + value + "\n");
		if (Double.isNaN(value)) {
			throw new RuntimeException("Invalid measure or NaN value of measure: " + measure);
		}

	}

	/**
	 * Calculates Mutual Information
	 * 
	 * @param denominator
	 *            the denominator to compute probabilities from frequencies
	 * @param countX
	 * @param countY
	 * @param XANDY
	 * @return p(x^y)*log2(p(x^y)/(p(x)*p(y))
	 */
	public static double mutualInformation(double denominator, double countX, double countY, double countXandY) {
		double result = 0;
		result += oneOutcome(denominator, countX, countY, countXandY); // x
																		// present,
																		// y
																		// present

		return result;

	}

	/**
	 * 
	 * @param denominator
	 * @param countX
	 * @param countY
	 * @param countXandY
	 * @return
	 */
	private static double oneOutcome(double denominator, double countX, double countY, double countXandY) {
		double pxy = countXandY / (denominator);
		if (pxy == 0)
			return 0;
		double px = countX / denominator;
		double py = countY / denominator;
		double result = pxy * ((Math.log(pxy / (px * py))) / Math.log(2));
		if (Double.isNaN(result))
			if (Constants.DEBUG)
				System.out.println("Nan!");
		// if(result < 0)
		// System.out.println("Result below 0");

		return result;

	}

	/**
	 * "Redundacy" as a normalized version of mutual information normalized by
	 * the sum of the entropies of the two random variables. Cf.
	 * http://en.wikipedia.org/wiki/Mutual_information#Normalized_variants
	 * 
	 * @param denominator
	 * @param countX
	 * @param countY
	 * @param countXandY
	 * @return
	 */
	public static double redundancy(double denominator, double countX, double countY, double countXandY) {
		return mutualInformation(denominator, countX, countY, countXandY) / (entropy(denominator, countX) + entropy(denominator, countY));
	}

	/**
	 * NOTE: APPROXIMATION TODO: FIXUP actual algorithm
	 * 
	 * @param denominator
	 * @param countX
	 * @return
	 */
	private static double entropy(double denominator, double countX) {
		return (-1 * (countX / denominator) * Math.log(countX / denominator) / Math.log(2)
		// when not doing it pointwise:
		// +-1*((denominator-countX)/denominator)*Math.log((denominator-countX)/denominator)/Math.log(2)
		);
	}

	/**
	 * "coefficients of constraint (Coombs, Dawes & Tversky 1970) or uncertainty coefficient (Press & Flannery 1988)"
	 * I(X;Y)/H(Y) cf.
	 * http://en.wikipedia.org/wiki/Mutual_information#Normalized_variants
	 * 
	 * Interpretation: The uncertainty in the distribution of the Ys given the
	 * Xs.
	 * 
	 * @param denominator
	 * @param countX
	 * @param countY
	 * @param countXandY
	 * @return
	 */
	public static double uncertainCoefficient(double denominator, double countX, double countY, double countXandY) {
		return mutualInformation(denominator, countX, countY, countXandY) / (entropy(denominator, countY) + 0.00000001);
	}

	/**
	 * Cf. Brin paper for an application in sequence analysis and a description
	 * of the two-word case
	 * 
	 * @param allCount
	 * @param countX
	 * @param countY
	 * @param countXandY
	 * @return
	 */
	private static double chiSquare(double allCount, double countX, double countY, double countXandY) {
		double observedX1Y1 = countXandY;
		double observedX0Y1 = countY - countXandY;
		double observedX1Y0 = countX - countXandY;
		double observedX0Y0 = allCount - observedX1Y1 - observedX0Y1 - observedX1Y0;
		double expectedX1Y1 = countX * countY / allCount;
		double expectedX0Y1 = (allCount - countX) * countY / allCount;
		double expectedX1Y0 = (allCount - countY) * countX / allCount;
		double expectedX0Y0 = (allCount - countY) * (allCount - countX) / allCount;

		return oVsE(observedX1Y1, expectedX1Y1) + oVsE(observedX1Y0, expectedX1Y0) + oVsE(observedX0Y1, expectedX0Y1) + oVsE(observedX0Y0, expectedX0Y0);
	}

	/**
	 * 
	 * @param observed
	 * @param expected
	 * @return
	 */
	private static double oVsE(double observed, double expected) {
		return Math.pow(observed - expected, 2) / expected;
	}

	/**
	 * Jaccard distance
	 * 
	 * @param allCount
	 * @param countX
	 * @param countY
	 * @param countXandY
	 * @return
	 */
	public static double jaccard(double allCount, double countX, double countY, double countXandY) {
		return 1 - (countXandY / (countX + countY - countXandY));
	}

	/**
	 * Jaccard distance between two sequences
	 * 
	 * @param seq1
	 * @param seq2
	 * @return
	 */
	public static double jaccard(String seq1, String seq2) {

		// Remove sequence specific string modifiers
		seq1 = seq1.replace(";", " ").replace("{", "").replace("}", "");
		seq2 = seq2.replace(";", " ").replace("{", "").replace("}", "");

		StringTokenizer seqTok1 = new StringTokenizer(seq1);
		StringTokenizer seqTok2 = new StringTokenizer(seq2);

		// Use of hashmaps guarantees tokens are a unique set
		HashMap<String, Integer> intersect = new HashMap<String, Integer>();
		HashMap<String, Integer> seq1map = new HashMap<String, Integer>();
		HashMap<String, Integer> seq2map = new HashMap<String, Integer>();

		double numer = 0.0; // Store total number of times token overlaps

		// Store all tokens for sequence1 into seq1 map and add to
		// hashmap of all tokens in seq1 and seq2
		while (seqTok1.hasMoreTokens()) {
			String tok = seqTok1.nextToken();
			seq1map.put(tok, 1);
			intersect.put(tok, 1);
		}

		// Store all tokens for sequence2 into seq2 map and add to
		// hashmap of all tokens in seq1 and seq2
		while (seqTok2.hasMoreTokens()) {
			String tok = seqTok2.nextToken();
			intersect.put(tok, 1);
			seq2map.put(tok, 1);
		}

		// Go through hashmaps and count overlap
		for (String key : seq1map.keySet()) {
			for (String key2 : seq2map.keySet()) {
				if (key.equalsIgnoreCase(key2)) {
					numer++;
				}
			}
		}

		double denom = intersect.size();

		return 1.0 - numer / denom;
	}

}
