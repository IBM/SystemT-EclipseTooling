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
package com.ibm.biginsights.textanalytics.aql.editor.common;

import java.io.File;
import java.io.IOException;

/**
 * Saw this class being defined in many files. Creating a separate definition in common.
 * This class is meant to be extended with either method onDirectory or onFile, or both being overridden.
 * 
 */
public class FileTraversal
{


  
	/**
   * Traverse recursively through the file path provided, 
   * if it is a directory and contains files and subdirectories
   * @param f
   * @throws IOException
   */
  public final void traverse( final File f ) throws IOException {
    if (f.isDirectory()) {
      onDirectory(f);
      final File[] childs = f.listFiles();
      for( File child : childs ) {
        traverse(child);
      }
      return;
    }
    onFile(f); 
  }

  public void onDirectory( final File d ) {
  }

  public void onFile( final File f ) {
  }
}
