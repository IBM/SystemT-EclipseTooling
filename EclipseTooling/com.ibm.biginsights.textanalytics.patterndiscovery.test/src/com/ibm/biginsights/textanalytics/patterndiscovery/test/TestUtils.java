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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.algebra.util.string.StringUtils;
import com.ibm.avatar.algebra.util.test.MemoryProfiler;
import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;

public class TestUtils {

	/** Name of the system property that may hold the base directory for tests. */
	public static final String TEST_DIR_PROPNAME = "TestData/JUnitWorkspace/PD_Backend";

	// The LW data path relative to the installation directory of the plugin
	private static final String lwDataPathName = "languageware";
	// LW desc file name relative to the LW data path
	private static final String lwDescFileName = "LanguageWare-7.2.0.2/langware.xml";
	
	public static final String TEST_WORKING_DIR;

	static IWorkspace workspace = ResourcesPlugin.getWorkspace(); 
	
	static {
		// Use the user-specified working directory if possible.
		if (null != System.getProperty(TEST_DIR_PROPNAME)) {
			TEST_WORKING_DIR = System.getProperty(TEST_DIR_PROPNAME);
		} else {
			TEST_WORKING_DIR = ".";
		}
	}
	
	public static final String INPUT_WORKING_DIR = "../";

	public static final String DEFAULT_OUTPUT_DIR = TEST_WORKING_DIR
			+ "/regression/actual";

	public static final String DEFAULT_EXPECTED_DIR = TEST_WORKING_DIR
			+ "/regression/expected";

	public static final String DEFAULT_DICTS_DIR = INPUT_WORKING_DIR
			+ "/PD_Backend/core";

	public static final String DEFAULT_UDFJARS_DIR = INPUT_WORKING_DIR
			+ "/PD_Backend/core";

	public static Logger log = Logger.getLogger("JUnit Log");
	
	/**
	 * String that we add to the end of an HTML dump file after truncating it.
	 */
	public static final String HTML_TAIL = "</body>\n</html>\n";

	/**
	 * Where to put the output HTML files.
	 */
	private String outputDir = DEFAULT_OUTPUT_DIR;

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getOutputDir() {
		return outputDir;
	}

	/**
	 * Directory containing expected contents of the HTML output files.
	 */
	private String expectedDir = DEFAULT_EXPECTED_DIR;

	public void setExpectedDir(String expectedDir) {
		this.expectedDir = expectedDir;
	}

	public String getExpectedDir() {
		return expectedDir;
	}

	/**
	 * Base directory for test files.
	 */

	/**
	 * Does runAOGFile() produce HTML output?
	 */
	private boolean generateHTML = true;

	public void setGenerateHTML(boolean generateHTML) {
		this.generateHTML = generateHTML;
	}

	public boolean getOutputEnabled() {
		return generateHTML;
	}

	/**
	 * Does runAOGFile generate unabridged (e.g. no snippets) HTML output when
	 * generateHTML is true?
	 */
	private boolean generateFullHTML = false;

	/**
	 * @param val
	 *            true to generate HTML with the entire, unabridged document
	 *            text, instead of snippets
	 */
	public void setGenerateFullHTML(boolean val) {
		generateFullHTML = val;
	}

	/**
	 * Does runGraph() add a character set header to its generated HTML?
	 */
	private boolean writeCharsetInfo = true;

	public void setWriteCharsetInfo(boolean b) {
		this.writeCharsetInfo = b;
	}

	/**
	 * Does runGraph() produce tuple tables in its HTML output?
	 */
	private boolean generateTables = false;

	public void setGenerateTables(boolean generateTables) {
		this.generateTables = generateTables;
	}

	/**
	 * Does runGraph() print memory usage?
	 */
	boolean printMemory = false;

	public void setPrintMemory(boolean val) {
		printMemory = val;
	}

	/**
	 * Used for tracing memory usage in runAOGFile()
	 */
	private MemoryProfiler memProfiler;

	public void setMemProfiler(MemoryProfiler memProfiler) {
		this.memProfiler = memProfiler;
	}

	/**
	 * Working directory for loading auxiliary (e.g. dictionary) files from
	 * runAOGFile()
	 */
	private String dictionaryPath = DEFAULT_DICTS_DIR;

	public void setDictionaryPath(String dictPath) {
		this.dictionaryPath = dictPath;
	}

	public String getDictionaryPath() {
		return dictionaryPath;
	}

	private String udfJarPath = DEFAULT_UDFJARS_DIR;

	public void setUDFJarPath(String udfJarPath) {
		this.udfJarPath = udfJarPath;
	}

	public String getUDFJarPath() {
		return udfJarPath;
	}

	/** Include path for loading auxiliary AQL files. */
	private String includePath = null;

	public void setIncludePath(String includePath) {
		this.includePath = includePath;
	}

	public String getIncludePath() {
		return includePath;
	}

	/**
	 * The expected output files are truncated to save space. This constant
	 * holds the number of lines the files are truncated to.
	 */
	public static int EXPECTED_RESULTS_FILE_NUM_LINES = 1000;

	/**
	 * How often we report progress (in number of documents) when running AOG
	 * annotation specs.
	 */
	private int progressInterval = 1000;

	public void setProgressInterval(int i) {
		this.progressInterval = i;
	}

	public int getProgressInterval() {
		return this.progressInterval;
	}

	/**
	 * If non-null, the factory object that knows how to produce custom
	 * tokenizers to be used for all tokenization in jobs spawned by this object
	 */
	private TokenizerConfig tokenizerCfg = null;

	/**
	 * Use an custom tokenizer
	 * 
	 * @param tokenizerCfg
	 *            If non-null, the factory object that knows how to produce
	 *            custom tokenizers to be used for all tokenization in jobs
	 *            spawned by this object
	 */
	public void useExternalTokenizer(TokenizerConfig tokenizerCfg) {
		this.tokenizerCfg = tokenizerCfg;
	}

	/**
	 * Set this flag to true to skip comparison against expected output files in
	 * regression tests; this setting is useful for regenerating the "expected"
	 * output files.
	 */
	private boolean skipFileComparison = false;

	// private boolean writeStatus = true;

	/**
	 * @deprecated Use {@link #truncateExpectedFiles()} or
	 *             {@link #truncateExpectedFile(String)} instead.
	 */
	@Deprecated
	public void setSkipFileComparison(boolean skipFileComparison) {
		this.skipFileComparison = skipFileComparison;
	}

	/**
	 * Compare two files. At some point, this should be replaced with the
	 * Netbeans JUnit module, which includes file comparison functionality.
	 * 
	 * @param linesToSkip
	 *            how many lines to ignore (from both input files) before
	 *            beginning the comparison; for use when the files are expected
	 *            to differ slightly in the header
	 * 
	 * @param linesToCompare
	 *            how many lines of the file to compare with each other, or -1
	 *            to look at both files to the end
	 * 
	 * @throws Exception
	 */
	public static void compareFiles(File expectedFile, File actualFile,
			int linesToSkip, int linesToCompare) throws Exception {

		final String ENCODING = "UTF-8";

		System.err.printf("Comparing file '%s' against expected file '%s'\n",
				actualFile.getPath(), expectedFile.getPath());

		BufferedReader expectedIn = new BufferedReader(new InputStreamReader(
				new FileInputStream(expectedFile), ENCODING));

		BufferedReader actualIn = new BufferedReader(new InputStreamReader(
				new FileInputStream(actualFile), ENCODING));

		int lineno = 0;

		if (-1 == linesToCompare) {
			linesToCompare = Integer.MAX_VALUE;
		}

		while (expectedIn.ready() && lineno < linesToCompare) {
			if (!actualIn.ready()) {
				// Ran out of input.
				throw new Exception(String.format(
						"File '%s' truncated at line %d", actualFile.getPath(),
						lineno));
			}

			// Surround our read operations with try blocks so that we can
			// report more helpful error messages.
			String expectedLine, actualLine;
			try {
				expectedLine = expectedIn.readLine();
			} catch (Throwable t) {
				throw new Exception(String.format(
						"Error reading line %d of 'expected' file '%s'",
						lineno, expectedFile.getPath()), t);
			}

			try {
				actualLine = actualIn.readLine();
			} catch (Throwable t) {
				throw new Exception(String.format(
						"Error reading line %d of 'actual' file '%s'", lineno,
						actualFile.getPath()), t);
			}

			if ((lineno >= linesToSkip) && !expectedLine.equals(actualLine)) {
				log.info("	Expected line:'" +StringUtils
						.escapeForPrinting(expectedLine)+"'");
				log.info("  Actual line:'"+StringUtils
						.escapeForPrinting(actualLine)+"'");
//				log.debug("Expected line:'%s'", StringUtils
//						.escapeForPrinting(expectedLine));
//				log.debug("  Actual line:'%s'", StringUtils
//						.escapeForPrinting(actualLine));
				throw new Exception(String.format(
						"File '%s' differs from comparison file '%s'"
								+ " on line %d", actualFile.getPath(),
						expectedFile.getPath(), lineno));
			}
			lineno++;
		}

		// Check whether the results file has too many lines.
		if (lineno < linesToCompare && actualIn.ready()) {
			throw new Exception(String.format(
					"File '%s' has more lines than comparison file '%s'",
					actualFile.getPath(), expectedFile.getPath()));
		}
		expectedIn.close();
		actualIn.close();
	}

	/**
	 * Truncate an HTML dump file to the indicated number of lines, then add
	 * some HTML code to close out the file.
	 * 
	 * @param f
	 *            the file to truncate
	 * @param nlines
	 *            how many lines to leave after the initial truncate operation;
	 *            the final number of lines in the file will be about 3 more,
	 *            due to the additional "tail" code added.
	 * @throws IOException
	 */
	public static void truncateHTML(File f, int nlines) throws IOException {

		BufferedReader in = new BufferedReader(new FileReader(f));

		// Create a temp file
		File tmp = File.createTempFile("truncate", ".htm");

		FileWriter out = new FileWriter(tmp);

		int lineno = 0;
		boolean keepGoing = true;
		while (keepGoing && lineno < nlines) {

			// System.err.printf("Read line %d/%d of input file '%s'\n",
			// lineno, nlines, f.getName());

			String line = in.readLine();
			if (null == line) {
				keepGoing = false;
			} else {
				out.write(line + "\n");
				lineno++;
			}
		}

		// If we truncated the file, add the tail that keeps the HTML valid.
		if (keepGoing) {
			out.write(HTML_TAIL);
		}
		out.close();
		in.close();

		// Now we can replace the original file with the temp file.
		// Need to do a second copy in order for this to work cross-platform.
		// System.err.printf("Moving %s to %s\n", tmp, f);

		boolean ret = f.delete();
		if (false == ret) {
			// System.err.printf("Couldn't delete file '%s'\n", f);
			throw new RuntimeException("Couldn't delete file " + f);
		}

		copyFile(tmp, f);
		tmp.delete();
		// if (!tmp.renameTo(f)) {
		// throw new IOException(String.format("Couldn't rename '%s' to '%s'",
		// tmp, f));
		// }
	}

	/**
	 * Copy a file from one location to another; will not replace its target.
	 * 
	 * @param src
	 *            source
	 * @param dest
	 *            destination location; must NOT exist
	 * @throws IOException
	 */
	private static void copyFile(File src, File dest) throws IOException {
		if (dest.exists()) {
			throw new IOException("Destination " + dest + " exists");
		}

		// Use FileInputStream and FileOutputStream to ensure a byte-for-byte
		// copy.
		FileInputStream in = new FileInputStream(src);
		FileOutputStream out = new FileOutputStream(dest);

		byte[] buf = new byte[1024];
		int nread;
		while (0 < (nread = in.read(buf))) {
			out.write(buf, 0, nread);
		}
		out.close();
		in.close();
	}

	/**
	 * Compare an HTML output file from a regression test against the expected
	 * output stored in CVS.
	 * 
	 * @param filename
	 *            name of the output file, not containing the directory it's in;
	 *            file is assumed to be in {@link #outputDir}.
	 * @throws IOException
	 * @throws Exception
	 */
	public void compareAgainstExpected(String filename) throws IOException,
			Exception {
		compareAgainstExpected(filename, true);
	}

	/**
	 * Compare an HTML output file from a regression test against the expected
	 * output stored in CVS.
	 * 
	 * @param filename
	 *            name of the output file, not containing the directory it's in;
	 *            file is assumed to be in {@link #outputDir}.
	 * @param truncate
	 *            true to truncate the files being compared
	 * @throws IOException
	 * @throws Exception
	 */
	public void compareAgainstExpected(String filename, boolean truncate)
			throws IOException, Exception {
		if (truncate) {
			truncateHTML(new File(outputDir, filename),
					EXPECTED_RESULTS_FILE_NUM_LINES);

			if (skipFileComparison) {
				System.err.printf("Skipping file comparison for %s, "
						+ "but still truncating the file to %d lines.\n",
						filename, EXPECTED_RESULTS_FILE_NUM_LINES);
			}
		}

		if (!skipFileComparison) {
			compareFiles(new File(expectedDir, filename), new File(outputDir,
					filename), 0, -1);
		}
	}

	/**
	 * Compare every file in the "expected" with the corresponding file in the
	 * "output" dir.
	 * 
	 * @param truncate
	 *            true to truncate the files being compared to 1000 lines
	 */
	public void compareAgainstExpected(boolean truncate) throws Exception {

		File[] expectedFiles = (new File(expectedDir)).listFiles();
		if (null == expectedFiles) {
			throw new Exception("Expected dir " + expectedDir
					+ " does not exist");
		}

		for (File file : expectedFiles) {
			// Skip directories (including the CVS directory)
			if (file.isFile()) {
				// SPECIAL CASE: Ignore NFS temporary files
				compareAgainstExpected(file.getName(), truncate);
			}
		}

	}

	/**
	 * Parse and execute an operator tree as encoded in an AOG file over a
	 * document corpus.
	 * 
	 * @param docscan
	 *            scan operator for retrieving documents to annotate
	 * @param filename
	 *            name of the file containing the AOG annotator spec
	 */
/*	public void runAOGFile(DocScan docscan, String filename) throws Exception {

		if (printMemory) {
			long mem = Runtime.getRuntime().totalMemory()
					- Runtime.getRuntime().freeMemory();
			System.err
					.printf("Before parsing, memory usage is %d bytes\n", mem);
		}

		AOGRunner runner = AOGRunner.compileFile(new File(filename), "UTF-8",
				dictionaryPath);

		// System.err.printf("Plan is:\n");
		// runner.dumpPlan(System.err);

		runGraph(docscan, runner);
	}*/

	/*public void runAOGFile(DocScan docscan, String filename, String encodingName)
			throws Exception {
		if (printMemory) {
			long mem = Runtime.getRuntime().totalMemory()
					- Runtime.getRuntime().freeMemory();
			System.err
					.printf("Before parsing, memory usage is %d bytes\n", mem);
		}

		long before = System.currentTimeMillis();

		AOGRunner runner = AOGRunner.compileFile(new File(filename),
				encodingName, dictionaryPath);

		long after = System.currentTimeMillis();

		System.err.printf("Parsed %s in %d ms\n", filename, after - before);

		runGraph(docscan, runner);
	}*/

	/** Shared code for running AOG files/strings; used by all the run* methods. */
//	public void runGraph(DocScan docscan, OperatorGraphRunner runner)
//			throws Exception {
//		runner.setDocscanInput(docscan);
//		runner.setFeedbackInterval(this.progressInterval);
//		runner.setPrintMemory(this.printMemory);
//		runner.setMemProfiler(this.memProfiler);
//
//		runner.useExternalTokenizer(tokenizerCfg);
//
//		// if (null != aogWorkingDir) {
//		// runner.setWorkingDir(aogWorkingDir);
//		// }
//
//		if (generateHTML) {
//			runner.setHTMLOutput(outputDir, generateTables, writeCharsetInfo,
//					generateFullHTML);
//		} else {
//			runner.setNoOutput();
//		}
//
//		runner.run();
//
//	}

	/**
	 * Parse and execute an operator tree as encoded in a string in AOG format.
	 * 
	 * @param docscan
	 *            scan operator for retrieving documents to annotate
	 * @param aog
	 *            the AOG annotator spec
	 */
/*	public void runAOGStr(DocScan docscan, String aog) throws Exception {

		if (printMemory) {
			long mem = Runtime.getRuntime().totalMemory()
					- Runtime.getRuntime().freeMemory();
			System.err
					.printf("Before parsing, memory usage is %d bytes\n", mem);
		}

		AOGRunner runner;
		try {
			runner = AOGRunner.compileStr(aog, dictionaryPath);
		} catch (Exception e) {
			// Intercept compile errors and dump the plan.
			File tmp = File.createTempFile("plan", ".aog");

			System.err.printf("Caught exception while compiling AOG string.\n"
					+ "Dumping AOG to %s\n", tmp);
			FileWriter out = new FileWriter(tmp);
			out.append(aog);
			out.close();

			throw e;
		}

		runGraph(docscan, runner);
	}*/

	/**
	 * Utility function to shorten a string to at most the indicated length,
	 * optionally escaping newlines.
	 */
	public static String shorten(String in, int maxlen, boolean stripNewlines) {
		if (stripNewlines) {
			in = in.replace("\n", "\\n");
			in = in.replace("\r", "\\r");
		}

		if (maxlen >= in.length()) {
			return in;
		} else {
			String ellipsis = "...";
			String head = in.substring(0, (maxlen - ellipsis.length()));
			return head + ellipsis;
		}
	}

	public static String shorten(String in, int maxlen) {
		return shorten(in, maxlen, false);
	}

	/**
	 * Remove anything currently at the output directory, and create it if it
	 * doesn't exist.
	 */
	public void cleanOutputDir() {
		File outDir = new File(outputDir);
		if (outDir.exists()) {
			FileUtils.deleteDirectory(outDir);
		}
		outDir.mkdirs();
	}

	/** Utility function to compile (with default options) and run an AQL file. */
//	public void runAQLFile(DocScan scan, String aqlFileName) throws Exception {
//		OperatorGraphRunner runner = AQLRunner.compileFile(
//				new File(aqlFileName), "UTF-8", includePath, dictionaryPath,
//				udfJarPath);
//		if (dumpPlan) {
//			runner.dumpPlan(System.err);
//		}
//		runGraph(scan, runner);
//	}
//
//	/**
//	 * Utility function to compile (with default options) and run an AQL file in
//	 * a given input encoding..
//	 */
//	public void runAQLFile(DocScan scan, String aqlFileName, String encodingName)
//			throws Exception {
//		OperatorGraphRunner runner = AQLRunner.compileFile(
//				new File(aqlFileName), encodingName, includePath);
//		if (dumpPlan) {
//			runner.dumpPlan(System.err);
//		}
//		runGraph(scan, runner);
//	}

	private boolean dumpPlan = false;

	/**
	 * @param dumpPlan
	 *            true to print out plans when running AQL
	 */
	public void setDumpPlan(boolean dumpPlan) {
		this.dumpPlan = dumpPlan;
	}

	/**
	 * Truncate a files in the "expected output" directory to
	 * EXPECTED_RESULTS_FILE_NUM_LINES lines in length.
	 */
	public void truncateExpectedFile(String name) throws IOException {

		File file = new File(expectedDir, name);

		if (file.exists() && file.isFile()) {
			truncateHTML(file, EXPECTED_RESULTS_FILE_NUM_LINES);
		} else {
			throw new IOException("File " + file.getPath() + " does not exist");
		}

	}

	/**
	 * Truncate all files in the "expected output" directory to
	 * EXPECTED_RESULTS_FILE_NUM_LINES lines in length.
	 */
	public void truncateExpectedFiles() throws IOException {
		File[] expectedFiles = (new File(expectedDir)).listFiles();
		if (null == expectedFiles) {
			throw new IOException("Expected dir " + expectedDir
					+ " does not exist");
		}

		for (File file : expectedFiles) {
			// Skip directories (including the CVS directory)
			if (file.isFile()) {
				truncateHTML(file, EXPECTED_RESULTS_FILE_NUM_LINES);
			}
		}

	}

	/**
	 * Process a set of arguments, in the form "-x", "-x [value]", or
	 * "--word [value]", to a main method.
	 * 
	 * @param args
	 *            the raw arguments to the main method
	 * @param possibleFlags
	 *            a list of all the flags (in the form "-x" or "--word") that
	 *            the program expects to see
	 * @param argsExpected
	 *            true if the corresponding flag should be followed by an
	 *            argument string
	 * @return a map from each flag to its value (if no value is provided, as in
	 *         "-x", then the value returned is null)
	 * @throws Exception
	 *             on error
	 */
	public static TreeMap<String, String> parseArgs(String[] args,
			final String[] possibleFlags, final boolean[] argsExpected)
			throws Exception {
		TreeMap<String, String> ret = new TreeMap<String, String>();

		// We can be in one of two states while running through the arguments
		// lisxt.
		final int READING_FLAG = 0;
		final int READING_FLAG_ARG = 1;
		int state = READING_FLAG;

		int flagIx = -1;

		int i = 0;
		while (i < args.length) {
			String arg = args[i];

			if (READING_FLAG == state) {

				// We're looking for one of the flags to be passed to the
				// program.
				boolean foundMatch = false;
				for (int j = 0; j < possibleFlags.length; j++) {
					if (possibleFlags[j].equals(arg)) {
						foundMatch = true;
						flagIx = j;
					}
				}

				if (false == foundMatch) {
					throw new Exception(String.format(
							"Don't understand argument '%s'", arg));
				}

				String flag = possibleFlags[flagIx];
				if (ret.containsKey(flag)) {
					throw new Exception(String.format(
							"Flag '%s' specified twice", flag));
				}

				// If we get here, we found a flag; advance to the next
				// argument.
				i++;
				if (argsExpected[flagIx]) {
					if (i >= args.length) {
						throw new Exception(String.format("Ran off end of "
								+ "program args looking for argument"
								+ " to '%s' flag", flag));
					}

					state = READING_FLAG_ARG;
				} else {

					// No arguments expected for this flag.
					ret.put(possibleFlags[flagIx], null);
				}

			} else if (READING_FLAG_ARG == state) {
				// Looking for argument to the flag.
				String flag = possibleFlags[flagIx];
				ret.put(flag, arg);
				i++;
				state = READING_FLAG;

			} else {
				throw new RuntimeException("Don't know about state " + state);
			}
		}

		return ret;

	}

	// public void setWriteStatus(boolean b) {
	// this.writeStatus = b;
	// }

	/**
	 * Returns the path of the given resource from the given classpath
	 * 
	 * @param classpath
	 * @param resourceName
	 * @return
	 */
	public static String getResourcePath(String classpath, String resourceName) {

		String[] paths = classpath.split(";");
		for (String path : paths) {
			File f = new File(path);
			if (f.getName().equalsIgnoreCase(resourceName))
				return path;
		}
		return null;

	}

	/**
	 * Returns the Default Language Ware Configuration File
	 * @return
	 */
	public static File getDefaultLWConfigFile() {
		return new File(getDefaultLWDataPath(), lwDescFileName);
	}

	public static File getDefaultLWDataPath() {
		Bundle systemtRuntimeBundle = com.ibm.biginsights.textanalytics.runtime.Activator
				.getDefault().getBundle();
		URL url = FileLocator.find(systemtRuntimeBundle, new Path(
				lwDataPathName), null);
		try {
			return new File(FileLocator.toFileURL(url).getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Simple function to delete an entire Directory recursively
	 * 
	 * @param dir
	 * @return
	 */
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
		return dir.delete();
	}
	
}
