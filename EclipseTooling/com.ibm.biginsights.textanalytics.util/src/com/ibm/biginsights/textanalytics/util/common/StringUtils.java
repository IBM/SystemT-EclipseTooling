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

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.biginsights.textanalytics.util.Activator;
import com.ibm.biginsights.textanalytics.util.Messages;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public abstract class StringUtils
{



  /** Pattern for matching text of the form "create view <MyView> ... ". */
  private static final Pattern CREATE_VIEW_STMT_PATTERN = Pattern.compile ("\\s*create\\s*view\\s*([^\\s]+)",
    Pattern.CASE_INSENSITIVE);

  /** Pattern for matching text of the form "select ... into <MyView> ... ". */
  private static final Pattern SELECT_INTO_STMT_PATTERN = Pattern.compile ("\\s*into\\s*([^\\s]+)",
    Pattern.CASE_INSENSITIVE);

  private static final Pattern OUTPUT_VIEW_STMT_PATTERN = Pattern.compile ("\\s*output\\s*view\\s*([^\\s]+)\\s*;",
    Pattern.CASE_INSENSITIVE);

  public static boolean isEmpty (String str)
  {
    if (str == null) { return true; }
    return (str.trim ().length () == 0);
  }

  public static boolean isEmpty (List<?> list)
  {
    return (list == null || list.isEmpty ());
  }

  /**
   * Return the first view name contained within the input AQL snippet.
   * 
   * @param statement
   * @return the view name, if any found, and null otherwise.
   */
  public static String getViewName (String snippet)
  {

    // Is the snippet of the form "create view MyView ..." ?
    Matcher match = CREATE_VIEW_STMT_PATTERN.matcher (snippet);
    if (match.find ()) { return match.group (1); }

    // Is the snippet of the form "select ... into MyView ..." ?
    match = SELECT_INTO_STMT_PATTERN.matcher (snippet);
    if (match.find ()) { return match.group (1); }

    match = OUTPUT_VIEW_STMT_PATTERN.matcher (snippet);
    if (match.find ()) { return match.group (1); }

    return null;
  }

  /**
   * Replace all whitespace characters (such as newlines) with spaces.
   * 
   * @param s
   * @return
   */
  public static final String normalizeWhitespace (String s)
  {
    return s.replaceAll ("\\s", " ");
  }
  
  /**
   * This method is to check given string is enclosed in double quotes or not
   * @param name to be validated
   * @return true if the given name is enclosed in double quotes else returns false 
   */
  public static boolean isEnclosedInDoublequotes (String name)
  {
    if(name == null){ return false; }
    if(name.startsWith ("\"") && name.endsWith ("\"") ){ //$NON-NLS-1$ //$NON-NLS-1$
      return true;
    }
    return false;
  }

  /**
   * Remove special characters that are not supported as part of a file name or secondary view ID.
   * 
   * Used for encoding the filename so that label returned from system runtime can be used as filename to
   * store the files with some modifications. It currently encodes the following special characters @link {@value #specialCharacters}.
   * (see http://en.wikipedia.org/wiki/Filename#Reserved_characters_and_words)
   * 
   * Also, this method is used to generate a valid view secondary ID from the input string. Eclipse uses colon ':' as separator between primary and secondary
   * ID. Therefore secondary IDs containing ':' are illegal. Replace any ':' with '~'.
   * 
   * @param id input id
   * @return null when the input id is null, otherwise, the input id with any special characters replaced by '~'.
   */
  private static char[] specialCharacters = new char[] { '/', '?', '%', '*', ':', '|', '<', '>', File.separatorChar,
    '"' };

  public static String normalizeSpecialChars (String id)
  {
    
    if (null == id) return null;

    for (int j = 0; j < specialCharacters.length; j++) {
      id = id.replace (specialCharacters[j], '~');
    }
    id = normalizeStartEnd(id);
    return id;
  }

	/**
	 * If the path is greater than 255, then we remove the chars in fileName from right to left such that
	 * the fileName length will be equal to 255.
	 * @param fileName
	 * @return
	 */
	public static String truncatePath(String fileName) {
		int encodedLabelLength = fileName.length();
		if(encodedLabelLength >= 255){
			/**
			 * The length of the filename should be less than 255 characters.
			 * We take only first 244 characters from the file name and 
			 * remaining 11 characters are used for file extension (.strf or .lc etc)
			 * and for versioning. The versioning have the format fileName(count).strf
			 * 
			 * We have 4 characters for count, so max count can be 9999.
			 */
			fileName = fileName.substring(0,241);
			fileName = normalizeStartEnd(fileName);
		}
		return fileName;
	}

	private static String normalizeStartEnd(String fileName) {
		if (fileName.startsWith (Constants.FILENAME_NORMALIZING_CHAR)) {
			 fileName = fileName.replaceFirst (Constants.FILENAME_NORMALIZING_CHAR, "");//$NON-NLS-1$
		    }
		 if (fileName.endsWith (".")) {//$NON-NLS-1$
			 fileName = fileName.substring (0, fileName.length () - 1);
		    }
		return fileName;
	}

/**
 * Utility method to escape Invalid XML characters. See here for a list of the actual invalid characters:
 *  http://xmlconf.sourceforge.net/xml/reports/report-xerces-jnv.html 
 *  (Go to the section entitled "Documents which are not Well-Formed")
 * This method takes a string and replaces the invalid XML characters with "0xFFFD"  
 * @param contents
 * @return
 */
  public static String escapeInvalidXMLChars(String inputDocName, String contents) 
  {
	  boolean replaceOccured = false;
	  if(contents==null) return null;
	  StringBuffer s = new StringBuffer();
	  char replaceChar = 0xFFFD;
	  for (char c : contents.toCharArray()) 
	  {
	    // If the character c is not equal to 0x0 and within a XML character boundary
	    // then append that character.
		  if ((c != 0x0) && ((c == 0x9) || (c == 0xA) || (c == 0xD)
		   || ((c >= 0x20) && (c <= 0xD7FF))
		   || ((c >= 0xE000) && (c <= 0xFFFD))
		   || ((c >= 0x10000) && (c <= 0x10FFFF)))) 
		   {
			   s.append(c);
		   }
		  else
		  {
			  replaceOccured = true;
			  s.append(replaceChar);
		  }
	  }
	  if (replaceOccured)
	  {
		  	String msg = Messages.getString("StringUtils.ESCAPED_XML_CHARS");
			String formattedMsg = MessageUtil.formatMessage(msg, new String[]{inputDocName});//$NON-NLS-1$
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logInfo(formattedMsg); 
	  }
	  return s.toString();
  }

}
