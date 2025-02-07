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
package com.ibm.biginsights.textanalytics.refactor.util;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ltk.core.refactoring.participants.RefactoringProcessor;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.indexer.impl.ExtractionPlanIndexer;
import com.ibm.biginsights.textanalytics.indexer.index.TextAnalyticsIndexer;
import com.ibm.biginsights.textanalytics.indexer.proxy.ElementReferenceProxy;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;
import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameInfo;
import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameProcessor;
import com.ibm.biginsights.textanalytics.refactor.core.AQLElementRenameRefactoring;
import com.ibm.biginsights.textanalytics.refactor.ui.wizards.AQLElementRenameWizard;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;

public class RefactorUtils
{



  public static void openRenameWizard (String aqlFilePath, int offset, String projectName, String moduleName,
    String viewName, ElementType eleType)
  {
    IPath location = Path.fromOSString (aqlFilePath);
    IFile aqlFile = ResourcesPlugin.getWorkspace ().getRoot ().getFileForLocation (location);

    // create AQLElementRenameInfo object
    AQLElementRenameInfo info = new AQLElementRenameInfo ();
    info.setNewName (viewName);
    info.setOldName (viewName);
    info.setOffset (offset);
    info.setSourceFile (aqlFile);
    info.setUpdateProject (false);
    info.setUpdateWorkspace (true);
    info.setProject (projectName);
    info.setModule (moduleName);
    info.setEleType (eleType);

    RefactoringProcessor processor = new AQLElementRenameProcessor (info);
    AQLElementRenameRefactoring ref = new AQLElementRenameRefactoring (processor);
    AQLElementRenameWizard wizard = new AQLElementRenameWizard (ref, info);
    RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation (wizard);

    try {
      Shell shell = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getShell ();
      String titleForFailedChecks = ""; //$NON-NLS-1$
      op.run (shell, titleForFailedChecks);
    }
    catch (final InterruptedException irex) {
      // operation was cancelled
    }
  }

  public static void openRenameWizard (String aqlFilePath, int lineNum, int colOffset, String projectName,
    String moduleName, String viewName, ElementType eleType) throws IOException
  {
    IPath location = Path.fromOSString (aqlFilePath);

    IFile aqlFile = ResourcesPlugin.getWorkspace ().getRoot ().getFileForLocation (location);
    if (!aqlFile.exists ()) return;

    int fileOffset = IndexerUtil.calculateOffset (aqlFile, lineNum, colOffset);

    RefactorUtils.openRenameWizard (aqlFilePath, fileOffset, projectName, moduleName, viewName, eleType);
  }

  /**
   * Get the extraction plan file affected based on the view name before changed. Therefore, it must be called BEFORE
   * the refactor action happens.
   * 
   * @param info The AQLElementRenameInfo object of the element being changed.
   * @return Extraction plan affected, if any; null, otherwise.
   */
  public static IFile getAffectedExtractionPlan (AQLElementRenameInfo info)
  {
    TextAnalyticsIndexer aqlIndex = TextAnalyticsIndexer.getInstance ();
    List<ElementReferenceProxy> aqlEleRefList = aqlIndex.getElementReferences (info.getProject (),
      info.getModule (), info.getEleType (), info.getOldName ());
    for (ElementReferenceProxy refObj : aqlEleRefList) {
      IFile file = refObj.getFile ();

      // Extraction plan doesn't keep views of different projects, so there should only be one extraction plan affected.
      if (file != null && file.getName ().equals (ExtractionPlanIndexer.epFileName)) return refObj.getFile ();
    }

    return null;
  }

  public static String getProjectNameFromLaunchConfig (ILaunchConfiguration launchConfiguration)
  {
    String projName = "";

    try {
      projName = launchConfiguration.getAttribute (IRunConfigConstants.PROJECT_NAME, "");

      // If it's a Pattern Discovery launch config we have to get project name from a different attribute.
      if (projName.equals (""))
        projName = launchConfiguration.getAttribute (IRunConfigConstants.PD_PROJECT_NAME, "");
    }
    catch (CoreException e) {
      // will return "".
    }

    return projName;
  }

}
