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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.eclipse.core.resources.IFile;

public class FileUtils
{
  @SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+         //$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

	/**
	 * Read the contents of a file into a Java string. Disregards whether the
	 * resource is in synch with the file system.
	 * 
	 * @param file
	 *            the file to read
	 * @param encoding
	 *            encoding to use when reading the file
	 */
	public static String fileToStr(IFile file, String encoding)
			throws Exception {

		StringBuilder sb = new StringBuilder();
		char[] buf = new char[1024];

		InputStreamReader in = new InputStreamReader(file.getContents(true),
				encoding);
		int nread;
		while (0 < (nread = in.read(buf))) {
			sb.append(buf, 0, nread);
		}
		in.close();
		String fileContents = sb.toString();
		return fileContents;
	}

	/**
	 * Read the contents of a file into a Java string.
	 * 
	 * @param file
	 *            the file to read
	 * @param encoding
	 *            encoding to use when reading the file
	 */
	public static String fileToStr(File file, String encoding)
			throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), encoding));
		StringBuilder strBuffer = new StringBuilder();
		char[] buf = new char[10000];
		int charsRead;
		try {
			while ((charsRead = br.read(buf)) >= 0) {
				strBuffer.append(buf, 0, charsRead);
			}
		} finally {
			br.close();
		}
		return strBuffer.toString();
	}

	/**
	 * Load a property file from disk.
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static Properties loadProperties(String path) throws Exception {
		FileInputStream in = new FileInputStream(com.ibm.avatar.algebra.util.file.FileUtils.createValidatedFile (path));

		Properties p = new Properties();
		p.load(in);

		return p;
	}

	/**
	 * reads the content of an input stream an output it as a String
	 * 
	 * @param stream
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static String streamToStr(InputStream stream, String encoding)
			throws Exception {

		StringBuilder sb = new StringBuilder();
		char[] buf = new char[1024];

		InputStreamReader in = new InputStreamReader(stream, encoding);
		int nread;
		while (0 < (nread = in.read(buf))) {
			sb.append(buf, 0, nread);
		}
		in.close();
		String fileContents = sb.toString();
		return fileContents;
	}

	/**
	 * Convert a delimiter string into the delimiter char to use in a CSV file.<br>
	 * The conversion follows the rules below:<ul>
	 * <li>"TAB" -> '\t'
	 * <li>"SPACE" -> ' '
	 * <li>a non-empty string -> first character of the string.
	 * <li>null and empty string -> the default value ','
	 * </ul>
	 * @param csvDelimiterString
	 * @return
	 */
	public static char getCsvDelimiterChar (String csvDelimiterString)
	{
	   if (csvDelimiterString != null) {
	      if (csvDelimiterString.equals (Constants.TAB))
	        return '\t';
	      else if (csvDelimiterString.equals (Constants.SPACE))
	        return ' ';
	      else if (csvDelimiterString.length () >= 1)
	        return csvDelimiterString.charAt (0);
	    }

	    // All other cases, return default value ','.
	    return ',';
	}
}
