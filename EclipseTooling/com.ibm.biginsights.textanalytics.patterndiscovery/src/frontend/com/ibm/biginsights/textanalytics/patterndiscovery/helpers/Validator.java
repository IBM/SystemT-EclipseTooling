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
package com.ibm.biginsights.textanalytics.patterndiscovery.helpers;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.avatar.algebra.util.file.FileUtils;

public class Validator
{
  @SuppressWarnings("unused")

	
	public static boolean validate(){
		
		return true;
	}
	

	/**
	 * validate that a file exits and is a file not a directory
	 * 
	 * @param absolutePath
	 * @return
	 */
	public static boolean validateFile(String absolutePath) {
		if(absolutePath == null || absolutePath.isEmpty())
			return false;
		File file = FileUtils.createValidatedFile(absolutePath);
		return (file.exists() && file.isFile());
	}

	/**
	 * validate that the provided absolute path is a valid directory
	 * 
	 * @param absolutePath
	 * @return
	 */
	public static boolean validateDirectory(String absolutePath) {
		if(absolutePath == null || absolutePath.isEmpty())
			return false;
		File file = FileUtils.createValidatedFile(absolutePath);
		return (file.exists() && file.isDirectory());
	}

	/**
	 * validate the provided path is a valid path to either a directory or a
	 * file
	 * 
	 * @param absolutePath
	 * @return
	 */
	public static boolean validateFileOrDir(String absolutePath) {
		if(absolutePath == null || absolutePath.isEmpty())
			return false;
		File file = FileUtils.createValidatedFile(absolutePath);
		return file.exists();
	}

	/**
	 * validate that a string is a valid representation of a boolean
	 * 
	 * @param input
	 *            the boolean as a String. ex. "True", "true", "FALSE"
	 * @return 1 if it is true, 0 if it is a false, and -1 if the provided
	 *         string is not a valid boolean option
	 */
	public static int validateBoolean(String input) {
		if(input == null || input.isEmpty())
			return -1;
		
		if (input.compareToIgnoreCase("true") == 0)
			return 1;
		if (input.compareToIgnoreCase("false") == 0)
			return 0;

		return -1;
	}

	/**
	 * validate that an integer number is in the range of min and max (inclusive)
	 * 
	 * @param value
	 *            the value to validate
	 * @param min
	 *            the minimum value allowed
	 * @param max
	 *            the maximum value allowed
	 * @return
	 */
	public static boolean validateIntInRange(int value, int min, int max) {
		if (value >= min && value <= max)
			return true;
		return false;
	}

	/**
	 * validate that a double number is in the range of min and max (inclusive)
	 * 
	 * @param value
	 *            the value to validate
	 * @param min
	 *            the minimum value allowed
	 * @param max
	 *            the maximum value allowed
	 * @return
	 */
	public static boolean validateDoubleInRange(double value, double min,
			double max) {
		if (value >= min && value <= max)
			return true;
		return false;
	}
	
	public static boolean validateRegex(String value, String regex){
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(value);
		return m.matches();
	}

}
