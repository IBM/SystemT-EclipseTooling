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

package com.ibm.biginsights.textanalytics.aqlimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

import com.ibm.biginsights.project.BigInsightsLibraryContainerInitializer;
import com.ibm.biginsights.project.ProjectSupport;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.fasterxml.jackson.databind.node.ArrayNode;

@SuppressWarnings("unused")
public class ImportExtractorWizard extends Wizard implements IImportWizard
{

private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

  private static final String TAMS_DIR = "tams";
  private static final String TAMS_DIR_PATH = "[W]/{0}/" + Constants.DEFAULT_MODULE_PATH + "/" + TAMS_DIR;
  private static final String CONCEPT_REQ_URI = "/TextAnalyticsWeb/controller/g2t/export/eclipsetooling";
  private static final String EXTRACTOR_ZIP = "extractors.zip";

  private IWorkbench workbench;
  private IStructuredSelection selection;
  private ImportExtractorWizardPage aqlImpPage;
  private IProject project;

  public ImportExtractorWizard ()
  {
  }

  @Override
  public void init (IWorkbench workbench, IStructuredSelection selection)
  {
    this.workbench = workbench;
    this.selection = selection;

    setWindowTitle (Messages.getString ("ImportExtractorWizard.TITLE"));  //$NON-NLS-1$
  }

  @Override
  public void addPages ()
  {
    aqlImpPage = new ImportExtractorWizardPage (Messages.getString ("ImportExtractorWizard.TITLE"));   //$NON-NLS-1$
    addPage (aqlImpPage);
  }

  @Override
  public boolean performFinish ()
  {
    buildProjectForExtractors ();

    importExtractors ();

    return true;
  }

  private void buildProjectForExtractors ()
  {
    final String projectName = aqlImpPage.getProjectName ();

    String defaultContainer = BigInsightsLibraryContainerInitializer.getInstance().getDefaultBigInsightsContainerEntryName();
    final IClasspathEntry defaultClasspath = BigInsightsLibraryContainerInitializer.getInstance().getClasspathEntryByName(defaultContainer);

    // Thread to create BI project 
    IRunnableWithProgress job = new IRunnableWithProgress () {
      @Override
      public void run (IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
      {
        monitor.beginTask (com.ibm.biginsights.project.Messages.NewWizard_CreateJob, IProgressMonitor.UNKNOWN);

        project = ProjectSupport.createProject (projectName, null, defaultClasspath);
        if (project == null) {
          Activator.getDefault ().getLog ().log (
            new Status (IStatus.ERROR, Activator.PLUGIN_ID, com.ibm.biginsights.project.Messages.ERROR_CREATING_PROJECT));
          throw new RuntimeException (com.ibm.biginsights.project.Messages.ERROR_CREATING_PROJECT);
        }

        // setting default encoding to UTF-8 during BI project creation
        try {
          project.setDefaultCharset (BIConstants.UTF8, null);
        }
        catch (CoreException e1) {
          Activator.getDefault ().getLog ().log (new Status (IStatus.ERROR, Activator.PLUGIN_ID, e1.getMessage ()));
          throw new RuntimeException (e1.getMessage ());
        }

        monitor.done ();
        return;
      }
    };

    try {
      getContainer ().run (false, false, job);
    }
    catch (Exception e) {
      String errorMsg = Messages.getString ("ImportExtractorWizard.ERROR_CREATING_PROJECT", new Object[] { projectName });
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (errorMsg, e);
    }
  }

  /**
   * Send a request to G2T server for extractors corresponding to a list of concept IDs.<br>
   * Create a new BI project and deploy the extractors to it. Set the tam path appropriately.
   * @param selectedConceptIDs
   * @return The zip file that G2T server returns.
   */
  private void importExtractors ()
  {
    // Get the zip file containing the extractors from G2T server
    File zipFileFromServer = getZipFromG2T ();
    if (zipFileFromServer == null)
      return;

    try {
      // Extract the zip file to <project-folder>/textAnalytics/
      File textAnalyticsDir = new File (project.getLocation ().toFile (), Constants.DEFAULT_MODULE_PATH);
      extractZipToFolder (zipFileFromServer, textAnalyticsDir);

      // after extracting the file, remove the zip and refresh the project.
      boolean delResult = zipFileFromServer.delete ();

      // Get preference store to configure tokenizer choice and tam path
      PreferenceStore projPrefStore = ProjectUtils.getPreferenceStore (project);

      // IEWT currently works on standard tokenizer only
      projPrefStore.setValue (null, Constants.TOKENIZER_CHOICE_WHITESPACE);

      // Configure project tam path if the there are dependent tams.
      File tamsDir = new File (textAnalyticsDir, TAMS_DIR);
      if (tamsDir.exists () && tamsDir.list ().length > 0) {
        String tamPath = MessageFormat.format (TAMS_DIR_PATH, project.getName ());
        projPrefStore.setValue (Constants.MODULE_TAMPATH, tamPath);
      }

      projPrefStore.save ();

      project.refreshLocal (IResource.DEPTH_INFINITE, null);
    }
    catch (Exception e) {
      String errorMsg = Messages.getString ("ImportExtractorWizard.ERROR_CREATING_PROJECT",
                                            new Object[] { project.getName () });
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (errorMsg, e);
    }
  }

  private void extractZipToFolder (File zipFile, File targetDir) throws IOException
  {
    ZipInputStream zipInputStream = null;

    zipInputStream = new ZipInputStream (new FileInputStream (zipFile));
    ZipEntry zipEntry = null;
    byte[] buffer = new byte[1024];

    while ((zipEntry = zipInputStream.getNextEntry ()) != null) {
      File newFile = new File (targetDir.getAbsolutePath () + File.separator + zipEntry.getName ());

      // Zip entry is a directory, create it in the target directory.
      if (zipEntry.isDirectory ()) {
        if (newFile.exists () == false) 
          newFile.mkdirs ();
      }
      // Zip entry is a file.
      else {
        // zip entries are given in random order. Sometimes a file is given before its parent folder; in
        // that case, we need to make sure the parent directory and other upper directories are created.
        if (newFile.getParent () != null) {
          File parentDir = new File (newFile.getParent ());
          if (parentDir.exists () == false)
            parentDir.mkdirs ();
        }
        extractFile (zipInputStream, newFile);
      }
    }
  }

  private void extractFile (ZipInputStream zis, File targetFile) throws IOException
  {
    FileOutputStream fos = new FileOutputStream (targetFile);

    byte[] buffer = new byte[1024];
    int read = 0;
    while ((read = zis.read (buffer)) != -1) {
      fos.write (buffer, 0, read);
    }

    fos.close ();
    zis.closeEntry ();
  }

  /**
   * Get the extractor zip file from G2T server and copy to <project-folder>/extractors.zip.
   */
  private File getZipFromG2T ()
  {
    File zipFile = new File (aqlImpPage.getExtrZipFilePath ());
    return zipFile;
  }

}
