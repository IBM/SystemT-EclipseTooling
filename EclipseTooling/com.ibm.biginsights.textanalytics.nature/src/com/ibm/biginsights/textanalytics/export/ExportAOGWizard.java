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
package com.ibm.biginsights.textanalytics.export;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.api.exceptions.ModuleNotFoundException;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectDependencyUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class ExportAOGWizard extends Wizard implements IExportWizard
{
	@SuppressWarnings("unused")
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

  private static final ILog logger = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);

  protected IWorkbench workbench;
  protected IStructuredSelection selection;
  protected ExportAOGWizardPage page;
  protected ExportProjectSelectionPage projectSelectionPage;
  protected IProject project;

  @Override
  public boolean performFinish ()
  {
    if (!page.isDataValid ()) {
      MessageBox errMsgDialog = new MessageBox (getShell (), SWT.ICON_ERROR);
      errMsgDialog.setMessage (page.getErrorMessage ());
      errMsgDialog.setText (Messages.getString ("ExportAOGWizard.ERROR")); //$NON-NLS-1$
      errMsgDialog.open ();
      return false;
    }
    else {
      exportAOG (page.getExportPath (), page.getModules (), page.getArchiveFileName (), page.isExportDependentModules);
      return true;
    }

  }

  private boolean hasBuildErrors (String projectName)
  {
    if (ProjectUtils.hasBuildErrors (ProjectPreferencesUtil.getProject (projectName))) { return true; }
    return false;
  }

  @Override
  public void addPages ()
  {

    try {
      project = ProjectUtils.getSelectedProject ();
      if (project == null) {
        ExportProjectSelectionPage projectSelectionPage = new ExportProjectSelectionPage ();
        addPage (projectSelectionPage);
        return;
      }
      else {
        if(!project.isOpen ()){
          CustomMessageBox errorMsgBox = CustomMessageBox.createErrorMessageBox (getShell (),
            Messages.getString ("General.ERROR"), Messages.getString ("General.ERR_NOT_OPENED_PROJ", //$NON-NLS-1$ //$NON-NLS-2$
              new Object[]{project.getName ()})); 
          errorMsgBox.open ();
          ExportProjectSelectionPage projectSelectionPage = new ExportProjectSelectionPage ();
          addPage (projectSelectionPage);
          return;
          
        }else if (project.hasNature (Activator.NATURE_ID)) {
          page = new ExportAOGWizardPage ("page", project.getName ());//$NON-NLS-1$
          addPage (page);
        }
        else {
          CustomMessageBox errorMsgBox = CustomMessageBox.createErrorMessageBox (getShell (),
            Messages.getString ("General.ERROR"), Messages.getString ("General.ERR_NOT_TEXT_ANAYLTICS_PROJ")); //$NON-NLS-1$ //$NON-NLS-2$
          errorMsgBox.open ();
          return;
        }
      }
    }
    catch (CoreException e) {
      logger.logError (e.getMessage ());
    }
  }

  @Override
  public void init (IWorkbench workbench, IStructuredSelection selection)
  {
    this.workbench = workbench;
    this.selection = selection;

    setWindowTitle (Messages.getString ("ExportAOGWizard.EXPORT_ANNOTATOR")); //$NON-NLS-1$
  }

  private void exportAOG (String exportLocation, String[] modules, String archiveFileName,
    boolean isExportDependentModules)
  {
    if (project != null) {
      try {
        project.build (IncrementalProjectBuilder.FULL_BUILD, null);
      }
      catch (CoreException e1) {
        logger.logError (e1.getMessage ());
      }

      if (hasBuildErrors (project.getName ())) {
        CustomMessageBox errorMsgBox = CustomMessageBox.createErrorMessageBox (getShell (),
          Messages.getString ("ExportAOGWizard.ERR_EXPORTING_AOG"), //$NON-NLS-1$
          Messages.getString ("General.ERR_PROJECT_HAS_BUILD_ERRORS")); //$NON-NLS-1$
        errorMsgBox.open ();
        return;
      }
      try {

        if (!isExportDependentModules) {

          String binPath = "";//$NON-NLS-1$
          if (ProjectUtils.isModularProject (project)) {
            IFolder binDir = ProjectUtils.getTextAnalyticsBinFolder (project);
            if (binDir != null) {
              binPath = binDir.getLocation ().toString ();
            }
          }
          else {
            binPath = ProjectPreferencesUtil.getSystemTProperties (project).getAogPath ();
          }

          binPath = ProjectPreferencesUtil.getAbsolutePath (binPath);

          if (archiveFileName == null || archiveFileName.isEmpty ()) {
            for (String module : modules) {
              File sourceFile = FileUtils.createValidatedFile (binPath, module + Constants.TAM_FILE_EXTENSION);

              File targetAogOutputFile = FileUtils.createValidatedFile (exportLocation, module + Constants.TAM_FILE_EXTENSION);

              fileCopy (sourceFile, targetAogOutputFile);

            }
          }
          else if (archiveFileName.indexOf (Constants.ZIP_FILE_EXTENSION) != -1) {
            bundleToZip (exportLocation, modules, archiveFileName, binPath);

          }
          else if (archiveFileName.indexOf (Constants.JAR_FILE_EXTENSION) != -1) {

            bundleToJar (exportLocation, modules, archiveFileName, binPath);

          }
        }
        else {
          ProjectDependencyUtil.bundleDependentModules (exportLocation, archiveFileName, project, modules);

        }
        
        refreshExportLocation (exportLocation, IResource.DEPTH_ONE);
        
        CustomMessageBox msgBox = CustomMessageBox.createInfoMessageBox (getShell (),
          Messages.getString ("ExportAOGWizard.EXPORT_ANNOTATOR"), //$NON-NLS-1$
          Messages.getString ("ExportAOGWizard.INFO_AOG_EXPORT_SUCCESS") + exportLocation); //$NON-NLS-1$
        msgBox.open ();

      }
      catch (IOException e) {
        logger.logError (e.getMessage ());
        CustomMessageBox errorMsgBox = CustomMessageBox.createInfoMessageBox (getShell (),
          Messages.getString ("ExportAOGWizard.EXPORT_ANNOTATOR"), //$NON-NLS-1$
          Messages.getString ("ExportAOGWizard.ERR_EXPORTING_AOG") + e.getMessage ()); //$NON-NLS-1$
        errorMsgBox.open ();
        return;
      }
      catch (ModuleNotFoundException e) {
        logger.logError (e.getMessage ());
        CustomMessageBox errorMsgBox = CustomMessageBox.createInfoMessageBox (getShell (),
          Messages.getString ("ExportAOGWizard.EXPORT_ANNOTATOR"), //$NON-NLS-1$
          Messages.getString ("ExportAOGWizard.ERR_EXPORTING_AOG") + e.getMessage ()); //$NON-NLS-1$
        errorMsgBox.open ();
        return;
      }
      catch (Exception e) {

        logger.logError (e.getMessage ());
      }

    }
  }

  /**
   * Refresh the export location. 
   * If the export location is in the tooling workspace, then renaming a project from the workspace
   * results in Out of sync. 
   * @param exportLocation
   * @throws CoreException
   */
  private void refreshExportLocation (String exportLocation, int depth) throws CoreException
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    IProject projects[] =  root.getProjects ();
    String iProjectLoc = ""; //$NON-NLS-1$
    String exportLoc = ""; //$NON-NLS-1$
    //Get all the projects in the workspace
    for (IProject iProject : projects) {
      iProjectLoc = iProject.getLocation ().toOSString ().replace ('\\', IPath.SEPARATOR); //$NON-NLS-1$
      exportLoc = exportLocation.replace ('\\', IPath.SEPARATOR); //$NON-NLS-1$
      //checks whether the export location starts with the project location.
      if(exportLoc.startsWith (iProjectLoc)){
        // Get the Eclipse resource
        IPath iPath = new Path (exportLoc.replace (iProjectLoc, "")); //$NON-NLS-1$
        IResource resource = iProject.findMember (iPath);
        if(resource != null){
          // Refresh the files.
          resource.refreshLocal (depth, null);
        }
        break;
      }

    }
  }

  private void bundleToJar (String exportLocation, String[] modules, String archiveFileName, String binPath) throws IOException
  {
    File file = FileUtils.createValidatedFile (exportLocation, archiveFileName);
    JarOutputStream jarOutputStream = new JarOutputStream (new FileOutputStream (file));
    for (String module : modules) {
      File sourceTAMFile = FileUtils.createValidatedFile (binPath, module + Constants.TAM_FILE_EXTENSION);
      FileInputStream in = new FileInputStream (sourceTAMFile);
      JarEntry newEntry = new JarEntry (module + Constants.TAM_FILE_EXTENSION);
      jarOutputStream.putNextEntry (newEntry);
      BufferedInputStream bufferedInputStream = new BufferedInputStream (in);
      while (bufferedInputStream.available () > 0) {
        jarOutputStream.write (bufferedInputStream.read ());
      }
      jarOutputStream.closeEntry ();
      bufferedInputStream.close ();
      in.close ();
    }
    jarOutputStream.finish ();
    jarOutputStream.close ();
  }

  private void bundleToZip (String exportLocation, String[] modules, String archiveFileName, String binPath) throws IOException
  {
    File file = FileUtils.createValidatedFile (exportLocation, archiveFileName);
    ZipOutputStream zipOutputStream = new ZipOutputStream (new FileOutputStream (file));
    for (String module : modules) {
      File sourceTAMFile = FileUtils.createValidatedFile (binPath, module + Constants.TAM_FILE_EXTENSION);
      FileInputStream in = new FileInputStream (sourceTAMFile);
      ZipEntry newEntry = new ZipEntry (module + Constants.TAM_FILE_EXTENSION);
      zipOutputStream.putNextEntry (newEntry);
      BufferedInputStream bufferedInputStream = new BufferedInputStream (in);
      while (bufferedInputStream.available () > 0) {
        zipOutputStream.write (bufferedInputStream.read ());
      }
      zipOutputStream.closeEntry ();
      bufferedInputStream.close ();
      in.close ();
    }
    zipOutputStream.finish ();
    zipOutputStream.close ();

  }

  private void fileCopy (File sourceAogFile, File targetAogOutputFile) throws IOException
  {
    FileInputStream in = new FileInputStream (sourceAogFile);
    FileOutputStream out = new FileOutputStream (targetAogOutputFile);

    byte buffer[] = new byte[10240]; // 10 KB is an optimal size
    int len = 0;
    while ((len = in.read (buffer)) > 0) {
      out.write (buffer, 0, len);
    }

    in.close ();
    out.close ();
  }

}
