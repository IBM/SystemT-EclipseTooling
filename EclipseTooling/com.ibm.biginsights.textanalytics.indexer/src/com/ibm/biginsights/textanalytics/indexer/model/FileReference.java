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
package com.ibm.biginsights.textanalytics.indexer.model;

/**
 * References to files (AQL file, dictionary file etc) will be captured by this class.
 * 
 *  Krishnamurthy
 */
public class FileReference extends Reference
{


 
	/**
   * Primary key of the ModuleReference object
   */
  Integer fileRefId;

  /**
   * ID of the referenced module
   */
  Integer fileId;

  public FileReference (Integer fileRefId, Integer fileId, ElementLocation location)
  {
    super ();
    this.fileRefId = fileRefId;
    this.fileId = fileId;
    this.location = location;
  }

  public Integer getFileRefId ()
  {
    return fileRefId;
  }

  public Integer getFileId ()
  {
    return fileId;
  }

}
