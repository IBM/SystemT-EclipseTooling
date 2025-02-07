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

package com.ibm.biginsights.textanalytics.indexer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import com.ibm.biginsights.textanalytics.indexer.index.TextAnalyticsIndexer;
import com.ibm.biginsights.textanalytics.indexer.proxy.ModuleReferenceProxy;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * This class get the Referencing Module Names for a Module.
 * 
 *  Simon
 */
public class TextAnalyticsIndexerHandler extends AbstractHandler
{



  @Override
  public Object execute (ExecutionEvent event) throws ExecutionException
  {
    String ModuleName = event.getParameter (Constants.CMD_GET_ALL_REFERENCING_MODULE_PARAM_MODULE_NAME);
    String ProjectName = event.getParameter (Constants.CMD_GET_ALL_REFERENCING_MODULE_PARAM_PROJ_NAME);

    if ((ProjectName == null || ProjectName.isEmpty ()) && (ModuleName != null && ModuleName.isEmpty ())) return null;
    String refProjectName = "";//$NON-NLS-1$
    Set<String> referencedModuleSet = new HashSet<String> ();

    TextAnalyticsIndexer indexer = TextAnalyticsIndexer.getInstance ();
    if(indexer.isIndexing())
    	return referencedModuleSet;
    
    List<ModuleReferenceProxy> references = indexer.getModuleReferences (ProjectName, ModuleName);
    for (ModuleReferenceProxy moduleReferenceProxy : references) {
      IFile iFile = moduleReferenceProxy.getFile ();
      refProjectName = iFile.getProject ().getName ();
      if (ProjectName.equals (refProjectName) && Constants.AQL_FILE_EXTENSION_STRING.equals (iFile.getFileExtension ())) {
        IFolder refMod = ProjectUtils.getModule4AqlFile (iFile);
        if (refMod != null && refMod.exists () && !ModuleName.equals (refMod.getName ())) {
          referencedModuleSet.add (refMod.getName ());
        }
      }
    }
    if (referencedModuleSet.isEmpty ())
      return null;
    else
      return referencedModuleSet;
  }

}
