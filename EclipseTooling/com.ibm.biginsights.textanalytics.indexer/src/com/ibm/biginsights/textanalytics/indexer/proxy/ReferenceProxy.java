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
package com.ibm.biginsights.textanalytics.indexer.proxy;

import org.eclipse.core.resources.IFile;

import com.ibm.biginsights.textanalytics.indexer.cache.ElementCache;
import com.ibm.biginsights.textanalytics.indexer.cache.FileCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ModuleCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ProjectCache;
import com.ibm.biginsights.textanalytics.indexer.model.Reference;

/**
 * Abstract base class for all reference proxies. This class transforms reference IDs in XXXReference to their actual
 * objects.
 * 
 *  Krishnamurthy
 */
public abstract class ReferenceProxy
{



	protected ElementCache elemCache = ElementCache.getInstance ();
  protected ProjectCache projectCache = ProjectCache.getInstance ();
  protected ModuleCache moduleCache = ModuleCache.getInstance ();
  protected FileCache fileCache = FileCache.getInstance ();

  /**
   * Lets the subclasses return the reference object
   * 
   * @return Reference object
   */
  protected abstract Reference getReference ();

  /**
   * Returns the IFile where the reference occurs
   * 
   * @return IFile where the reference occurs
   */
  public IFile getFile ()
  {
    return fileCache.getFile (getReference ().getLocation ().getFileId ());
  }

  /**
   * Returns the offset where the reference occurs
   * 
   * @return offset where the reference occurs
   */
  public int getOffset ()
  {
    return getReference ().getLocation ().getOffset ();
  }

  /**
   * Returns the file id where the reference occurs
   * 
   * @return file id where the reference occurs
   */
  public Integer getFileId ()
  {
    return getReference ().getLocation ().getFileId ();
  }
}
