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
package com.ibm.biginsights.textanalytics.indexer.resourcechange;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import com.ibm.biginsights.textanalytics.indexer.Activator;
import com.ibm.biginsights.textanalytics.indexer.DebugMsgConstants;
import com.ibm.biginsights.textanalytics.indexer.index.TextAnalyticsIndexer;
import com.ibm.biginsights.textanalytics.indexer.types.ResourceAction;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class is responsible for re-indexing the AQL. Scheduled to be active every 5 sec. Picks the Job from the Queue
 * process the jobs, then go for sleep.
 */
public class ResourceChangeReindexThread extends Thread
{
	@SuppressWarnings("unused")


  private static final int SLEEP_SECONDS = 5;
  private static boolean reindexing;
  
  private static final ILog LOGGER = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);
  
  public static boolean isRenindexing(){
	  return reindexing;
  }
  @Override
  public void run ()
  {
    TextAnalyticsIndexer indexer = TextAnalyticsIndexer.getInstance ();
    ResourceChangeQueue resourceChangeQueue = ResourceChangeQueue.getInstance ();
    while (true) {
      if (!resourceChangeQueue.isEmpty ()) {
        processQueue (indexer, resourceChangeQueue);
      }
      else {
        try {
          Thread.sleep (SLEEP_SECONDS * 1000);
        }
        catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace ();
        }
      }

    }

  }

  private void processQueue (TextAnalyticsIndexer indexer, ResourceChangeQueue resourceChangeQueue)
  {
	  reindexing = true;
    // Move to seperate method
    while (!resourceChangeQueue.isEmpty ()) {
      // It is assumed that resourceChangeQueue would contain indexing jobs only on relevant resources - module folders
      // in Text Analytics projects, aql files in valid modules, text analytics projects, etc.
      // These checks have been conducted in TextAnalyticsResourceChangeListener.
      ResourceChangeJob resourceChangeJob = resourceChangeQueue.getNextJob ();
      IResource resSrc = resourceChangeJob.getResSrc ();
      try {

        if (resourceChangeJob.getAction () == ResourceAction.ADDED) {
          if (resSrc instanceof IFile) {
            LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT, "Added File:"
              + resSrc.getFullPath ().toString ()));
            indexer.fileAdded ((IFile) resSrc);
            LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT, "Added File:"
              + resSrc.getFullPath ().toString ()));
          }
          else if (resSrc instanceof IFolder) {
            if (ProjectUtils.isConfiguredSrcFolder ((IFolder) resSrc)) {
              LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT,
                "Added Text Analytics src folder" + resSrc.getFullPath ().toString ()));
              indexer.projectSrcAdded (resSrc.getProject ());
              LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT,
                "Added Text Analytics src folder" + resSrc.getFullPath ().toString ()));
            }
            else {
              LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT, "Added Module:"
                + resSrc.getFullPath ().toString ()));
              indexer.moduleAdded ((IFolder) resSrc);
              LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT, "Added Module:"
                + resSrc.getFullPath ().toString ()));
            }
          }
          else if (resSrc instanceof IProject) {
            LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT,
              "Added Project:" + resSrc.getName ()));
            indexer.projectAdded ((IProject) resSrc);
            LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT,
              "Added Project:" + resSrc.getName ()));
          }
        }

        if (resourceChangeJob.getAction () == ResourceAction.DELETED) {
          if (resSrc instanceof IFile) {
            LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT, "Deleted File:"
              + resSrc.getFullPath ().toString ()));
            indexer.fileDeleted ((IFile) resSrc);
            LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT, "Deleted File:"
              + resSrc.getFullPath ().toString ()));
          }
          else if (resSrc instanceof IFolder) {
            if (ProjectUtils.isConfiguredSrcFolder ((IFolder) resSrc)) {
              LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT,
                "Deleted Text Analytics src folder" + resSrc.getFullPath ().toString ()));
              indexer.projectSrcDeleted (resSrc.getProject ());
              LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT,
                "Deleted Text Analytics src folder" + resSrc.getFullPath ().toString ()));
            }
            else {
              LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT, "Deleted Module:"
                + resSrc.getFullPath ().toString ()));
              indexer.moduleDeleted ((IFolder) resSrc);
              LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT, "Deleted Module:"
                + resSrc.getFullPath ().toString ()));
            }
          }
          else if (resSrc instanceof IProject) {
            LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT,
              "Deleted Project:" + resSrc.getName ()));
            indexer.projectDeleted ((IProject) resSrc);
            LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT,
              "Deleted Project:" + resSrc.getName ()));
          }
        }

        if (resourceChangeJob.getAction () == ResourceAction.RENAMED) {
          if (resSrc instanceof IFile) {
            LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT, "Renamed File:"
              + resSrc.getFullPath ().toString ()));
            indexer.fileRenamed ((IFile) resSrc, (IFile) resourceChangeJob.getResDest ());
            LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT, "Renamed File:"
              + resSrc.getFullPath ().toString ()));
          }
          else if (resSrc instanceof IFolder) {
            if (ProjectUtils.isConfiguredSrcFolder ((IFolder) resourceChangeJob.getResDest ())) {
//              LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT,
//                "Renamed Text Analytics source folder:" + resSrc.getFullPath ().toString ()));
              //Do nothing here. Text analytics source folder rename would also cause text analytics
              //properties file to change. That would cause the project to be indexed again.
//              LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT,
//                "Renamed Text Analytics source folder:" + resSrc.getFullPath ().toString ()));
            }
            else if (ProjectUtils.isIntrestedModuleFolder ((IFolder) resourceChangeJob.getResDest ())){
              LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT, "Renamed module:"
                + resSrc.getFullPath ().toString ()));
              indexer.moduleRenamed ((IFolder) resSrc, (IFolder) resourceChangeJob.getResDest ());
              LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT, "Renamed module:"
                + resSrc.getFullPath ().toString ()));
            }

          }
          else if (resSrc instanceof IProject) {
            LOGGER.logDebug (String.format (DebugMsgConstants.BEGIN_INDEXER_RES_CHG_EVENT, "Renamed project:"
              + ((IProject) resSrc).getName ()));
            indexer.projectRenamed ((IProject) resSrc, (IProject) resourceChangeJob.getResDest ());
            LOGGER.logDebug (String.format (DebugMsgConstants.END_INDEXER_RES_CHG_EVENT, "Renamed project:"
              + ((IProject) resSrc).getName ()));
          }
        }

        if (resourceChangeJob.getAction () == ResourceAction.MOVED) {
          if (resSrc instanceof IFile) {
            indexer.fileMoved ((IFile) resSrc, (IFile) resourceChangeJob.getResDest ());
          }
          else if (resSrc instanceof IFolder) {
            if (ProjectUtils.isConfiguredSrcFolder ((IFolder) resourceChangeJob.getResDest ())) {
              indexer.projectSrcMoved (resSrc.getProject ());
            }
            else {
              indexer.moduleMoved ((IFolder) resSrc, (IFolder) resourceChangeJob.getResDest ());
            }
          }
        }

        // Contents in the AQL File updated.
        if (resourceChangeJob.getAction () == ResourceAction.UPDATED) {
          if (resSrc instanceof IFile) {
            if (Constants.TEXT_ANALYTICS_PREF_FILE.equals (resSrc.getName ()) && resSrc.getParent ().equals (resSrc.getProject ())) {
              indexer.projectPropertiesUpdated ((IFile) resSrc);
            }
            indexer.fileUpdated ((IFile) resSrc);
          }
        }

        if (resourceChangeJob.getAction () == ResourceAction.OPEN) {
          indexer.projectOpened ((IProject) resSrc);
        }

        if (resourceChangeJob.getAction () == ResourceAction.CLOSE) {
          indexer.projectClosed ((IProject) resSrc);
        }
      }
      catch (Exception e) {
        LOGGER.logDebug (
          "Encountered exception while processing resource change indexing task. Proceeding to next task.", e); //$NON-NLS-1$
      }

    }
    
    reindexing = false;
  }

}
