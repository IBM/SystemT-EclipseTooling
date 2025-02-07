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
package com.ibm.biginsights.textanalytics.resultviewer.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.impl.ConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.ui.ConcordanceView;
import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.util.PaginationTracker;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Contains common utility methods relevant to Concordance (Annotation Explorer) view.
 * 
 *
 */
public class AnnotationExplorerUtil
{
  /**
   * Generates a ConcordanceModel instance for a given result file (.strf).
   * @param resFile the .strf result file
   * @param tempDirPath Temporary directory path, to be passed to concordance model constructor
   * @param provParams Provenance parameters, to be passed to concordance model constructor
   * @param monitor Progress monitor instance, to be passed to concordance model constructor
   * @return ConcordanceModel instance
   * @throws CoreException
   */
  public static IConcordanceModel generateConcordanceModelFromFile ( IFile resFile,
                                                                      String tempDirPath,
                                                                      ProvenanceRunParams provParams,
                                                                      IProgressMonitor monitor)
                                                                          throws CoreException
  {
    List<IFile> singleFileList = new ArrayList<IFile> ();
    singleFileList.add (resFile);

    return generateConcordanceModelFromFiles (singleFileList, tempDirPath, provParams, monitor);
  }

  /**
   * Given a list of files containing serialized extraction results (strf files), it deserializes them and 
   * generates a ConcordanceModel instance
   * @param modelFiles List of strf files
   * @param tempDirPath Temporary directory path, to be passed to concordance model constructor
   * @param provParams Provenance parameters, to be passed to concordance model constructor
   * @param monitor Progress monitor instance, to be passed to concordance model constructor
   * @return ConcordanceModel instance
   * @throws CoreException
   */
  public static IConcordanceModel generateConcordanceModelFromFiles (List<IFile> modelFiles, String tempDirPath,
    ProvenanceRunParams provParams, IProgressMonitor monitor) throws CoreException
  {
    List<SystemTComputationResult> resultModels = readModelFiles (modelFiles, monitor);
    return new ConcordanceModel (resultModels, tempDirPath, provParams, monitor);
  }

  /**
   * Deserializes strf files to get extraction results from a particular run
   * @param fileList List of strf files
   * @param monitor Progress monitor instance
   * @return List of extraction results (SystemTComputationResult instances)
   * @throws CoreException
   */
  public static List<SystemTComputationResult> readModelFiles (List<IFile> fileList, IProgressMonitor monitor) throws CoreException
  {
    List<SystemTComputationResult> modelList = new ArrayList<SystemTComputationResult> (fileList.size ());
    Serializer ser = new Serializer ();
    List<String> failedInput = new ArrayList<String> (fileList.size ());
    for (IFile file : fileList) {
      if (monitor.isCanceled ()) { return null; }
      SystemTComputationResult res = ser.getModelForInputStream (file.getContents (true));
      if (res == null) {
        failedInput.add (file.getFullPath ().toString ());
      }
      else {
        modelList.add (res);
      }
      monitor.worked (1);
    }
    if (failedInput.size () > 0) {
      StringBuilder sb = new StringBuilder (Messages.ConcordanceUtil_invalidInputDocumentsError+"\n"); //$NON-NLS-1
      for (String filename : failedInput) {
        sb.append (filename);
        sb.append ('\n');
      }
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (sb.toString ());
    }
    return modelList;
  }

  /**
   * This method is called to display the first page in Annotation explorer. It may be called as soon as the result
   * files required to satisfy the first page is serialized or at the end of execution (for the case when the size of
   * results < page size)
   * 
   * @param project The project being run
   * @param resultFolder The directory holding the serialized extraction results
   * @param tempDir temporay directory
   * @param provenanceRunParams provenance parameters
   * @param firstPageResults List containing models for annotation results on input documents for the first page.
   * @param numOFiles Number of documents in first page.
   * @return Returns true on completion
   * @throws CoreException
   */
  public static boolean displayFirstPage (IProject project, IFolder resultFolder, String tempDir,
    ProvenanceRunParams provenanceRunParams, List<SystemTComputationResult> firstPageResults, int numOfFiles) throws CoreException
  {
    PaginationTracker tracker = PaginationTracker.getInstance ();
    tracker.setResultFolder (resultFolder, numOfFiles);
    final ConcordanceModel concModel = new ConcordanceModel (firstPageResults, tempDir, provenanceRunParams,
      new NullProgressMonitor ());
    concModel.setProject (project);
    showConcordanceView (concModel);
    return true;
  }

  /**
   * This method is called to display the first page in Annotation explorer. It may be called as soon as the result
   * files required to satisfy the first page is serialized or at the end of execution (for the case when the size of
   * results < page size). It will retrieve the files for the first page and uses them to generate ConcordanceModel and
   * Annotation Explorer view.
   * 
   * @param project The project being run
   * @param resultFolder The directory holding the serialized extraction results
   * @param tempDir temporay directory
   * @param provenanceRunParams provenance parameters
   * @return Returns true on completion
   * @throws CoreException
   */
  public static boolean displayFirstPage (IProject project, IFolder resultFolder, String tempDir,
    ProvenanceRunParams provenanceRunParams) throws CoreException
  {
    PaginationTracker tracker = PaginationTracker.getInstance ();
    tracker.setResultFolder (resultFolder);
    List<IFile> pagedFiles = tracker.getFiles (1); // gets file list
    final IConcordanceModel concModel = generateConcordanceModelFromFiles (pagedFiles, tempDir, provenanceRunParams,
      new NullProgressMonitor ());
    concModel.setProject (project);
    showConcordanceView (concModel);
    return true;
  }

  /**
   * This method refreshes the AnnotationExplorer to display the latest calculated value of "total number of pages" as
   * the execution happens in the background. Number of files in the result folder is explicitly set in order to avoid
   * expensive api calls to retrieve the files in result folder and calculate file count.
   * 
   * @param numOfFiles Number of files in the result folder
   * @throws CoreException
   */
  public static void updatePageDescription (IFolder resultFolder, int numOfFiles) throws CoreException
  {
    PaginationTracker tracker = PaginationTracker.getInstance ();
    tracker.setResultFolder (resultFolder, numOfFiles);
    refreshPageCountInAnnotationExplorer ();
  }

  /**
   * Displays the Annotation explorer view after setting given parameter as the data model.
   * @param concModel ConcordanceModel instance
   */
  public static void showConcordanceView (final IConcordanceModel concModel)
  {
    Display.getDefault ().asyncExec (new Runnable () {

      @Override
      public void run ()
      {
        try {
          final IWorkbenchWindow window = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ();
          final ConcordanceView view = (ConcordanceView) window.getActivePage ().showView (
            "com.ibm.biginsights.textanalytics.concordance.view", null, //$NON-NLS-1$
            IWorkbenchPage.VIEW_ACTIVATE);
          view.setInput (concModel);
        }
        catch (final PartInitException e) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
            Messages.ConcordanceUtil_openConcordanceViewError, e); //$NON-NLS-1$
        }
      }
    });
  }

  /**
   * Refreshes the description on the Annotation explorer view
   */
  private static void refreshPageCountInAnnotationExplorer ()
  {
    Display.getDefault ().asyncExec (new Runnable () {

      @Override
      public void run ()
      {
        final IWorkbenchWindow window = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ();
        final ConcordanceView view = (ConcordanceView) window.getActivePage ().findView (ConcordanceView.VIEW_ID);
        if (view != null) // Annotation Explorer view can be null if it's not opened yet
          view.refreshDescription ();
      }
    });
  }
}
