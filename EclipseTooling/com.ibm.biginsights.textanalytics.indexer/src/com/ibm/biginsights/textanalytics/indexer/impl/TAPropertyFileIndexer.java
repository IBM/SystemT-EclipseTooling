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
package com.ibm.biginsights.textanalytics.indexer.impl;

import java.util.StringTokenizer;

import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * Indexes text analytics property file
 * 
 *  Krishnamurthy, Rajeshwar Kalakuntla
 */
public class TAPropertyFileIndexer extends FileIndexer
{


  
	private static final String PropertyNameValuePairSeparator = "=";

  @Override
  protected void parseAndIndex ()
  {
    final String WS_PREFIX = "[W]/";

    for (int i = 0; i < fileContents.length; ++i) {
      int lineNum = i + 1;
      String line = fileContents[i];
      String trimmedLine = line.trim ();
      /* 
       * COMMENT: Commenting out this part from indexing as searchPath.datapath property belongs to older version of projects and we dont 
       * consider it for modular projects. Rather removing just commenting this part, we can reuse if it requires anytime in future.

      if (trimmedLine.startsWith (Constants.SEARCHPATH_DATAPATH)) {
        int idx = line.indexOf (PropertyNameValuePairSeparator);
        String RHS = line.substring (idx + 1).trim ();
        StringTokenizer strTok = new StringTokenizer (RHS, "/");
        if (strTok.countTokens () >= 2) {
          strTok.nextToken ();// skip first token. It is [W]
          String projectName = strTok.nextToken ();

          // Add 1 because, String index begins from 0, but column numbers begin from 1
          int beginCol = line.indexOf (WS_PREFIX + projectName) + WS_PREFIX.length () + 1;
          addProjectReference (projectName, fileToIndex, lineNum, beginCol);
        }
      } */

      if (trimmedLine.startsWith (Constants.MODULE_DEPENDENTPROJECT)
          || trimmedLine.startsWith (Constants.MODULE_TAMPATH)) {
        int idx = line.indexOf (PropertyNameValuePairSeparator);
        String RHS = line.substring (idx + 1).trim ();
        String projectsPaths[] = RHS.split (Constants.DATAPATH_SEPARATOR);
        int cursor = 0;  // defined the cursor to point to the starting index of the line(tamPath) to search for project path
        for (String projectPath : projectsPaths) {
          /*
           * When we have multiple tamPaths, and one of them is substring of other, then the index of 
           * the project path in line gives us the same offset(defect 51524) for different tamPaths. 
           * To fix this, we are updating the searching window in line by moving the cursor appropriately. 
           */
          line = line.substring (cursor);  
          StringTokenizer strTok = new StringTokenizer (projectPath, "/");
          if (strTok.countTokens () >= 2) {
            strTok.nextToken ();// skip first token. It is [W]
            String projectName = strTok.nextToken ();
            // Add 1 because, String index begins from 0, but indexer's column numbers begin from 1
            int beginCol = cursor + line.indexOf (projectPath) + WS_PREFIX.length () + 1;
            addProjectReference (projectName, fileToIndex, lineNum, beginCol);
          }
          cursor = cursor + projectPath.length ();  // This line is added to calculate the proper project column offsets when we have multiple references in the tam path file.
        }
      }
    }

  }

}
