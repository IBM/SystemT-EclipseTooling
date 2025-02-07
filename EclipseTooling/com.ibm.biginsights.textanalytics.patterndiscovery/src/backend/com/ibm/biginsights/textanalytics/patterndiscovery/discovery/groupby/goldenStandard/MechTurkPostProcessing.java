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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.Constants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.GroupByNewProcessor;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.DiscoveryConstants;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.ExperimentProperties;
import com.ibm.biginsights.textanalytics.patterndiscovery.discovery.groupby.util.PropertyConstants;

/**
 * Post processing class for golden standards in MechTurk <insert description
 * about output formats> The input file (from mechTurk) must have the structure
 * specified below with static variables.
 * <p>
 * Currently expects the following fields:<br>
 * <br>
 * <b>Unique_ID</b> - String (docid-Span1Begin:Span1End:Span2Begin:Span2End)<br>
 * <b>Context</b> - String (Snippet containing entity1, entity2, and between)<br>
 * <b>entityOne</b> - String (First entity)<br>
 * <b>entityTwo</b> - String (Second entity)<br>
 * <b>isEntityOne</b> - String (YES/NO/PARTIALLY - is the first entity really
 * that entity)<br>
 * - ex: isPerson? - Yes/No/Partially<br>
 * <b> isEntityTwo</b> - String (YES/NO/PARTIALLY)<br>
 * <b>isRelated</b> - String (YES/NO/PARTIALLY - is entity1 related to entity2?
 * - partially means 'N/A')<br>
 * - ex. Does the phone (entity2) belong to person (entity1) - Yes/No/Partially
 * <br>
 * - Partially - either person/phone was not actually a person/phone<br>
 * <b>Doc_id</b> - Integer (document ID)<br>
 * <b>rejection_time</b> - String (time answer rejected)<br>
 * <b>approve_rate</b> - String (rating for mechTurk user -
 * ["XX% (Approve/Reject)"]<br>
 * 
 *  Chu
 * 
 */
public class MechTurkPostProcessing {



	/**
	 * Static variables - column IDs for the resulting mechTurk File
	 */
	final private static int UNIQUE_ID = 16;
	final private static int CONTEXT = 20;
	final private static int ENTITYONE = 18;
	final private static int ENTITYTWO = 19;
	final private static int ISENTITYONE = 21;
	final private static int ISENTITYTWO = 23;
	final private static int ISRELATED = 22;
	final private static int DOC_ID = 17;
	final private static int REJECTION_TIME = 12;
	final private static int APPROVE_RATE = 15;

	/**
	 * Location for answer key
	 */
	final private static int YES = 0;
	final private static int NO = 1;
	final private static int PARTIALLY = 2;

	/**
	 * Given an raw result file from mechTurk, will process the file into a for
	 * consumable to apply labels to groups
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @throws IOException
	 */

	public static void processRawResults(String inputFile, String outputFile) throws IOException {

		if (Constants.DEBUG)
			System.out.println("\nStart Post Processing of MechTurk Results");

		// Create a CSV Reader to read input file
		char separator = ',';
		CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(inputFile), GroupByNewProcessor.ENCODING), separator);

		// Extract all the arguments from the grouping file
		@SuppressWarnings("unchecked")
		List<String[]> outputList = reader.readAll();

		// Grab the header from the result file - remove from list
		String[] header = outputList.remove(0);
		Integer[] questionLocations = { ISENTITYONE, ISENTITYTWO, ISRELATED };

		CSVWriter writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(outputFile), GroupByNewProcessor.ENCODING), ',', '"');

		HashMap<String, MechTurkObject> toWrite = new HashMap<String, MechTurkObject>();

		for (String[] line : outputList) {

			// Skip rejected results
			if (line[REJECTION_TIME].length() > 10)
				continue;

			String uniqueID = line[UNIQUE_ID];
			int docID = Integer.parseInt(line[DOC_ID]);
			String phone = line[ENTITYTWO];
			String person = line[ENTITYONE];
			String context = line[CONTEXT];
			String[] approvalRate = ((line[APPROVE_RATE].split("\\(")[1]).split("\\)")[0]).split("/");
			Double approvalRateD = Double.parseDouble(approvalRate[0]) / Double.parseDouble(approvalRate[1]);

			if (toWrite.containsKey(uniqueID)) {
				MechTurkObject ob = toWrite.get(uniqueID);
				HashMap<String, Double[]> answer = ob.getAnswers();
				answer = matchAnswer(answer, line, approvalRateD, header, questionLocations);
				ob.setAnswer(answer);
				toWrite.put(uniqueID, ob);
			} else {
				HashMap<String, Double[]> answer = new HashMap<String, Double[]>();
				for (int question : questionLocations) {
					Double[] empty = { 0.0, 0.0, 0.0 };
					answer.put(header[question], empty);
				}
				answer = matchAnswer(answer, line, approvalRateD, header, questionLocations);
				MechTurkObject ob = new MechTurkObject(uniqueID, docID, phone, person, context, answer);
				toWrite.put(uniqueID, ob);
			}
		}

		// Write results to file
		for (String id : toWrite.keySet()) {
			MechTurkObject ob = toWrite.get(id);
			String[] writeLine = { ob.getUniqueID(), ob.getText(), ob.getEntity1(), ob.getEntity2(), majorityVote(ob.getAnswers(), header[ISENTITYONE]),
					majorityVote(ob.getAnswers(), header[ISENTITYTWO]), majorityVote(ob.getAnswers(), header[ISRELATED]), printAnswers(ob.getAnswers()),
					Integer.toString(ob.getDocID()) };
			writer.writeNext(writeLine);
		}
		writer.flush();
		writer.close();
		reader.close();
		if (Constants.DEBUG)
			System.out.println("Finished Writing to: " + outputFile);
	}

	/**
	 * Given the results header, answers hashmap, approval rate, and question
	 * locations in the CSV, will find the question answer, and add the proper
	 * value given the user's approval rating to the final answer hashmap
	 * 
	 * @param answers
	 * @param line
	 * @param approvalRate
	 * @param header
	 * @param questions
	 * @return
	 */
	private static HashMap<String, Double[]> matchAnswer(HashMap<String, Double[]> answers, String[] line, Double approvalRate, String[] header,
			Integer[] questions) {

		for (int questionLocation : questions) {
			String question = header[questionLocation];
			String answer = line[questionLocation];
			Double[] allAnswers = answers.get(question);
			if (answer.equalsIgnoreCase("yes")) {
				allAnswers[YES] = allAnswers[YES] + approvalRate;
			} else if (answer.equalsIgnoreCase("no")) {
				allAnswers[NO] = allAnswers[NO] + approvalRate;
			} else {
				allAnswers[PARTIALLY] = allAnswers[PARTIALLY] + approvalRate;
			}
			answers.put(question, allAnswers);
		}
		return answers;
	}

	/**
	 * Given a hashmap with questions mapped to an array with YES,NO, PARTIALLY,
	 * pull the answer for the question given
	 * 
	 * @param answers
	 * @param question
	 * @return
	 */
	private static String majorityVote(HashMap<String, Double[]> answers, String question) {
		if ((answers.get(question)[YES] > answers.get(question)[NO]) & (answers.get(question)[YES] > answers.get(question)[PARTIALLY])) {
			return "Yes";
		} else if ((answers.get(question)[NO] > answers.get(question)[YES]) & (answers.get(question)[NO] > answers.get(question)[PARTIALLY])) {
			return "No";
		} else if ((answers.get(question)[PARTIALLY] > answers.get(question)[YES]) & (answers.get(question)[PARTIALLY] > answers.get(question)[NO])) {
			return "Partially";
		} else {
			return "ERROR";
		}
	}

	private static String printAnswers(HashMap<String, Double[]> ans) {
		String toPrint = "{";
		for (String question : ans.keySet()) {
			toPrint = toPrint + "'" + question + "': [" + ans.get(question)[YES] + ", " + ans.get(question)[NO] + ", " + ans.get(question)[PARTIALLY] + "], ";
		}
		return toPrint.substring(0, toPrint.length() - 1) + "}";
	}

	/**
	 * Once the golden standard is created, then create the XML files required
	 * to run com.ibm.avatar.annotatortester
	 * 
	 * @param inputFile
	 * @param outputFile
	 * @param properties
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void createGoldenStandardForEvaluator(String inputFile, ExperimentProperties properties) throws ParserConfigurationException,
			TransformerException, IOException {

		// Create a CSV Reader to read input file
		char separator = ',';
		CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(inputFile), GroupByNewProcessor.ENCODING), separator);

		// Extract all the arguments from the grouping file
		List<String[]> outputList = reader.readAll();
		
		reader.close();

		// Store obs that need to be printed in XML key
		HashMap<Integer, ArrayList<MechTurkObject>> storeKey = new HashMap<Integer, ArrayList<MechTurkObject>>();

		for (String[] line : outputList) {
			String isRelated = line[6];

			// Only care if the result is positive
			if (isRelated.equalsIgnoreCase("YES")) {
				MechTurkObject ob = new MechTurkObject();
				int docid = Integer.parseInt(line[8]);
				ob.setUniqueID(line[0]);
				ob.setText(line[1]);
				ob.setEntity1(line[2]);
				ob.setEntity2(line[3]);
				ob.setRelated(isRelated);
				ob.setDocID(docid);

				if (storeKey.containsKey(docid)) {
					ArrayList<MechTurkObject> list = storeKey.get(docid);
					list.add(ob);
					storeKey.put(docid, list);
				} else {
					ArrayList<MechTurkObject> list = new ArrayList<MechTurkObject>();
					list.add(ob);
					Collections.sort(list, new MechTurkSpanComparator());
					storeKey.put(docid, list);
				}
			}
		}

		/**
		 * Building mapping for uniqueID to context String
		 */
		// Create a CSV Reader to read input file

		// initialize directory structure
		String baseDir = properties.getProperty(PropertyConstants.FILE_ROOT_DIR);
		String fileName = properties.getProperty(PropertyConstants.INPUT_DOCUMENT_NAME);
		if (fileName.contains("."))
			fileName = fileName.substring(0, fileName.indexOf('.'));
		baseDir = baseDir + fileName + File.separator;
		// Directory where SystemT outputs are stored into

		String contextMap = baseDir + properties.getProperty(PropertyConstants.INPUT_FILE_DIR) + "aqlOutput.csv";
		CSVReader readerContext = new CSVReader(new InputStreamReader(new FileInputStream(contextMap), GroupByNewProcessor.ENCODING), separator);

		// Extract all the arguments from the grouping file
		List<String[]> outputContextList = readerContext.readAll();
		
		readerContext.close();

		HashMap<String, String> contextMapping = new HashMap<String, String>();

		for (String[] line : outputContextList) {
			// Store uniqueID with context value
			contextMapping.put(line[2], line[4]);
		}

		String outputDir = properties.getProperty(PropertyConstants.DOCUMENT_COLLECTION_DIR) + fileName + "_xmlKey/";
		;

		// Makes sure to check if existing XML dir exists - if so remove
		File checkExist = new File(outputDir);
		if (checkExist.exists()) {
			Boolean del = deleteDir(checkExist);
			if (!del) {

				if (Constants.DEBUG)
					System.out.println("DELETE FAILED");
			}
		}

		for (int docid : storeKey.keySet()) {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.newDocument();

			String fieldName = DiscoveryConstants.GROUPBY_CONTEXT_COLUMN_NAME;
			String viewName = properties.getProperty(PropertyConstants.AQL_VIEW_NAME);

			Element rootElement = document.createElement("annotations");
			document.appendChild(rootElement);

			Element subRootElement = document.createElement("text");
			rootElement.appendChild(subRootElement);

			File xmlFile = new File(outputDir + fileName + "_" + docid + ".xml");
			xmlFile.getParentFile().mkdirs();
			BufferedWriter outSingle = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xmlFile), GroupByNewProcessor.ENCODING));

			ArrayList<MechTurkObject> toParse = storeKey.get(docid);
			Collections.sort(toParse, new MechTurkSpanComparator());

			for (MechTurkObject ob : toParse) {
				Element entity = document.createElement(viewName);
				subRootElement.appendChild(entity);

				String uniqueID = ob.getUniqueID();
				String[] spans = uniqueID.split(":");

				Element startEm = document.createElement("start");
				startEm.appendChild(document.createTextNode(spans[1]));
				entity.appendChild(startEm);

				Element endEm = document.createElement("end");
				endEm.appendChild(document.createTextNode(spans[2]));
				entity.appendChild(endEm);

				Element annot = document.createElement("annotation");
				annot.appendChild(document.createTextNode(contextMapping.get(uniqueID)));
				entity.appendChild(annot);

				Element type = document.createElement("typeinfo");
				type.appendChild(document.createTextNode(fieldName));
				entity.appendChild(type);
			}

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(outSingle);
			DOMSource source = new DOMSource(document);
			transformer.transform(source, result);
			outSingle.flush();
			outSingle.close();
		}
	}

	// Deletes all files and subdirectories under dir.
	// Returns true if all deletions were successful.
	// If a deletion fails, the method stops attempting to delete and returns
	// false.
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}
}

class MechTurkSpanComparator implements Comparator<Object> {

	@Override
	public int compare(Object arg0, Object arg1) {
		// TODO Auto-generated method stub

		String uniqueID1 = ((MechTurkObject) arg0).getUniqueID();
		String uniqueID2 = ((MechTurkObject) arg1).getUniqueID();

		String[] spans1 = uniqueID1.split(":");
		String[] spans2 = uniqueID2.split(":");

		int start1 = Integer.parseInt(spans1[1]);
		int end1 = Integer.parseInt(spans1[2]);

		int start2 = Integer.parseInt(spans2[1]);
		int end2 = Integer.parseInt(spans2[2]);

		if (start1 < start2) {
			return -1;
		} else if (start1 > start2) {
			return 1;
		} else {
			if (end1 < end2) {
				return -1;
			} else if (end1 > end2) {
				return 1;
			} else {
				return 0;
			}
		}

	}

}
