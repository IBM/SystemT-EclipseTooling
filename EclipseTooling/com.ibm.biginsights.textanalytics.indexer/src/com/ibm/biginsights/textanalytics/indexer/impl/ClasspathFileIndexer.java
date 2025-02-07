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

/**
 * Indexes .classpath file
 * 
 *  Simon
 */
public class ClasspathFileIndexer extends FileIndexer
{


 
	private static final String CLOSETAG = "\"/>";
  private static final String KIND_SRC = "kind=\"src\"";
  private static final String CLASSPATHENTRY = "<classpathentry";
  private static final String PATH = "path=\"/";

  @Override
  protected void parseAndIndex ()
  {

    for (int i = 0; i < fileContents.length; ++i) {
      int lineNum = i + 1;
      String line = fileContents[i];
      String trimmedLine = line.trim ();

      // Sample line that contains the Project entry
      // <classpathentry kind="src" path="/test1"/>
      if (trimmedLine.startsWith (CLASSPATHENTRY) && trimmedLine.contains (PATH) && trimmedLine.contains (KIND_SRC)) {
        int idx = line.indexOf (PATH) + PATH.length ();
        String projectName = line.substring (idx, line.indexOf (CLOSETAG)).trim ();
        int beginCol = line.indexOf (projectName) + 1;
        addProjectReference (projectName, fileToIndex, lineNum, beginCol);
      }
    }

  }

}
