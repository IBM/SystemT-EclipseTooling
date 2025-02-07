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

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceStore;

import com.ibm.biginsights.textanalytics.indexer.cache.ElementCache;
import com.ibm.biginsights.textanalytics.indexer.cache.FileCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ModuleCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ProjectCache;
import com.ibm.biginsights.textanalytics.indexer.impl.AQLFileIndexer;
import com.ibm.biginsights.textanalytics.indexer.index.IDManager;
import com.ibm.biginsights.textanalytics.indexer.index.TextAnalyticsIndexer;
import com.ibm.biginsights.textanalytics.indexer.resourcechange.ResourceChangeReindexThread;
import com.ibm.biginsights.textanalytics.indexer.resourcechange.listeners.TextAnalyticsResourceChangeListener;
import com.ibm.biginsights.textanalytics.indexer.types.IndexingStatus;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class is responsible for loading the indexer from the file system. When we close the workspace, the index
 * details from the memory, will be written to file system.
 */
public class StartupIndexer
{



  /**
   * Hold the latest modified timestamp for the workspace. Whenever the workspace closes, the system will update the
   * timestamp with the close time stamp. Whenever the system starts, will pick this timestamp and see any change
   * happened after it.
   */
  long timeStamp;

  ResourceChangeReindexThread resourceChangeReindexThread;

  public void start ()
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    String workspacePath = root.getLocation ().makeAbsolute ().toOSString ();
    File directory = new File (workspacePath + Constants.INDEX_PATH);
    File file = new File (directory, Constants.INDEXING_STATUS_FILE);
    boolean isExist = file.exists ();

    if (isExist) {
      // Use the Index file and populate the index.
      PreferenceStore prefStore = IndexerUtil.getInitailsIndexStore ();
      String indexStatus = prefStore.getString ("IndexStatus");
      timeStamp = prefStore.getLong ("timeStamp");

      if (IndexingStatus.LOADED_TO_FILE_SYSTEM_COMPLETED.toString ().equals (indexStatus)) {
        // Closing of workspace successful. So, load saved index files
        IDManager.getInstance ().load ();
        ProjectCache.getInstance ().load ();
        ModuleCache.getInstance ().load ();
        FileCache.getInstance ().load ();
        ElementCache.getInstance ().load ();
      }
      else {
        // Workspace Crashed. So, indexes were not saved properly. Re-index the whole workspace.
        try {
          TextAnalyticsIndexer.getInstance ().reindex ();
        }
        catch (Exception e) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError ("Error indexing workspace", e);
        }
      }

    }
    // if index status file does not exist
    else {
      // Create index from scratch
      try {
        TextAnalyticsIndexer.getInstance ().reindex ();
      }
      catch (Exception e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError ("Error indexing workspace", e);
      }
    }

    // Update the status and timestamp.
    updateInitialIndexingStatus (IndexingStatus.LOADED_TO_MEMORY.toString (), System.currentTimeMillis ());

    // Start the Resource Thread
    resourceChangeReindexThread = new ResourceChangeReindexThread ();
    resourceChangeReindexThread.start ();

    ResourcesPlugin.getWorkspace ().addResourceChangeListener (new TextAnalyticsResourceChangeListener (),
      IResourceChangeEvent.POST_CHANGE | IResourceChangeEvent.PRE_DELETE);

  }

  public void stop ()
  {
    try {
      ProjectCache.getInstance ().write ();
      ModuleCache.getInstance ().write ();
      FileCache.getInstance ().write ();
      ElementCache.getInstance ().write ();
      IDManager.getInstance ().write ();

      // Update the status and timestamp.
      updateInitialIndexingStatus (IndexingStatus.LOADED_TO_FILE_SYSTEM_COMPLETED.toString (),
        System.currentTimeMillis ());
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
    }
  }

  /**
   * reIndex if the timestamp of the AQL Files is greater than the stored timestamp
   * 
   * @param root
   */
  private void reindexUsingAQLFile (IWorkspaceRoot root, long timeStamp)
  {
    AQLFileIndexer fileIndexer = new AQLFileIndexer ();
    IProject projects[] = root.getProjects ();
    for (IProject project : projects) {
      try {

        if (project.isOpen ()
          && project.hasNature (com.ibm.biginsights.textanalytics.util.common.Constants.PLUGIN_NATURE_ID)
          && ProjectUtils.isModularProject (project)) {

          IFolder srcFolder = ProjectUtils.getTextAnalyticsSrcFolder (project);// Get the source
          IResource modules[] = srcFolder.members ();
          for (IResource module : modules) {// iterate thru the modules
            if (module instanceof IFolder) {
              IResource aqlFiles[] = ((IFolder) module).members ();
              for (IResource aqlFile : aqlFiles) {// iterate thru the AQL
                // files in a module
                if (aqlFile instanceof IFile && "aql".equals (aqlFile.getFileExtension ())
                  && aqlFile.getModificationStamp () > timeStamp) {
                  try {
                    fileIndexer.indexFileContents ((IFile) aqlFile);
                  }
                  catch (Exception e) {
                    LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
                      String.format ("Error indexing file: %s", aqlFile.getName ()), e);
                  }
                }
              }
            }
          }// END - iterate thru the modules
        }
      }
      catch (CoreException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          String.format ("Error loading project: %s", project.getName ()), e);
      }

    }
  }

  private void updateInitialIndexingStatus (String status, long timeStamp)
  {

    PreferenceStore prefStore = IndexerUtil.getInitailsIndexStore ();
    if (prefStore != null) {
      prefStore.setValue ("IndexStatus", status);
      prefStore.setValue ("timeStamp", timeStamp);
      try {
        prefStore.save ();
      }
      catch (IOException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());

      }
    }
  }

}
