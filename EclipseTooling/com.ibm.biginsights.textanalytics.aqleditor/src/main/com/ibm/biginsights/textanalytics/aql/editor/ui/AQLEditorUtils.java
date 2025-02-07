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
package com.ibm.biginsights.textanalytics.aql.editor.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.Messages;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.AQLModule;
import com.ibm.biginsights.textanalytics.aql.library.AQLModuleProject;
import com.ibm.biginsights.textanalytics.aql.library.AQLProject;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.launch.AQLLibraryUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.ViewEditorInput;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class AQLEditorUtils
{



  /**
   * Find the AQL file containing a given view, open it, and highlight the view.
   * @param projectName The project containing the view.
   * @param viewName The view name
   */
  public static boolean openAQLViewInEditor (String projectName, String viewName)
  {
    IProject project = ResourcesPlugin.getWorkspace ().getRoot ().getProject (projectName);
    if (project == null)
      return true;

    String location = project.getLocation ().toOSString ();
    AQLProject aqlProject = Activator.getLibrary ().getLibraryMap ().get (location);

    if (aqlProject == null)
      return true;

    List<String> aqlFilePaths = aqlProject.getAqlFilePaths ();

    return openAQLViewInEditor (aqlFilePaths, Activator.getLibrary (), viewName);
  }

  /**
   * Find the AQL file containing a given view, open it, and highlight the view.
   * @param projectName The project containing the view.
   * @param viewName The view name
   */
  public static boolean openAQLViewInEditor (String projectName, String moduleName, String viewName)
  {
    IProject project = ProjectUtils.getProject (projectName);
    if (project == null)
      return false;

    String projectLocation = project.getLocation ().toOSString ();

    IAQLLibrary aqlLib = Activator.getModularLibrary ();

    AQLModuleProject aqlModuleProject = aqlLib.getModuleLibraryMap ().get (projectLocation);
    if (aqlModuleProject == null || aqlModuleProject.getAQLModules () == null)
      return false;

    IFolder moduleFolder = ProjectUtils.getModuleFolder (projectName, moduleName);
    if (moduleFolder == null || moduleFolder.getLocation () == null)
      return false;

    String moduleLocation = moduleFolder.getLocation ().toOSString ();

    AQLModule aqlModule = aqlModuleProject.getAQLModules ().get (moduleLocation);
    if (aqlModule != null) {
      List<String> aqlFilePaths = aqlModule.getAqlFilePaths ();
      return openAQLViewInEditor (aqlFilePaths, aqlLib, viewName);
    }

    return false;
  }

  public static boolean openAQLViewInEditor_nonModular (String projectName, String aqlFilePath, String viewName) throws PartInitException
  {
    String absPath = ProjectPreferencesUtil.deduceAbsolutePath (aqlFilePath);
    File jFile = new File(absPath);
    if (jFile.exists ()) {
      String p = jFile.getAbsolutePath ();  // Use this format so it can be compared in AQL library. absPath's format may not be correct.
      List<String> aqlFilePaths = new ArrayList<String> ();
      aqlFilePaths.add (p);

      // open the editor so  AQL library is loaded (in case it is not)
      IFile file = getFile (absPath);
      PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().openEditor (new FileEditorInput (file), AQLEditor.EDITOR_ID);

      return openAQLViewInEditor (aqlFilePaths, Activator.getLibraryForProject (projectName), viewName);
    }

    return false;
  }

  private static boolean openAQLViewInEditor (List<String> aqlFilePaths, IAQLLibrary aqlibrary, String viewName)
  {
    int offset;
    int line;
    int length;

    for (String filepath : aqlFilePaths) {

      List<AQLElement> views = aqlibrary.getViews (filepath);

      if (views != null)
        for (AQLElement view : views) {
          if (view.getName ().equals (viewName)) {
            offset = view.getBeginOffset ();
            line = view.getBeginLine ();
            length = view.getEndOffset () - offset + 1;

            AQLEditor editor;
            try {
              IFile file = getFile (filepath);
              if (file.exists ()) {
                editor = (AQLEditor) PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().openEditor (
                  new FileEditorInput (file), AQLEditor.EDITOR_ID);
                editor.setCursorAndMoveTo (line, offset);
                editor.selectAndReveal (editor.getCaretOffset (), length);
                return true;
              }
            }
            catch (PartInitException e) {
              Activator.getDefault ().getLog ().log (new Status (IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage ()));
            }
          }
        }
    }

    LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.AQLEditor_MISSING_VIEW_IN_AQL_FILES);
    return false;
  }

  public static int[] getViewLocationInFile (String projectName, String aqlFilePath, String viewName)
  {
    IProject project = ProjectUtils.getProject (projectName);
    if (project == null || StringUtils.isEmpty (aqlFilePath) || StringUtils.isEmpty (viewName))
      return null;

    int[] info = new int[2];

    AQLLibraryUtil.populateAQLLibrary (project);
    IAQLLibrary aqlibrary = Activator.getModularLibrary ();
    List<AQLElement> views = aqlibrary.getViews (aqlFilePath);

    if (views != null && !views.isEmpty ()) {
      for (AQLElement view : views) {
        if (view.getName ().equals (viewName)) {
          info[0] = view.getBeginLine ();
          info[1] = view.getBeginOffset ();
          return info;
        }
      }
    }

    return null;
  }

  public static IFile getFile (String path)
  {
    return ResourcesPlugin.getWorkspace ().getRoot ().getFileForLocation ((IPath) new Path (path));
  }

  /**
   * This method is for ViewEditorInput, an extension of FileEditorInput. The ViewEditorInput is used to open
   * an AQL file containing a given view and highlight it when, for some reason eg. plugin cycle dependency,
   * that the method openAQLViewInEditor() can't be used.
   * 
   * The ViewEditorInput object initially contains only project name and view name. This method will find and
   * populate it with info about the AQL file and location of the view in it.
   *
   * @param input The ViewEditorInout object
   */
  public static void populateAqlInfoForViewEditorInput (ViewEditorInput input)
  {
    String projectName = input.getProjectName ();
    IProject project = ResourcesPlugin.getWorkspace ().getRoot ().getProject (projectName);
    if (project == null)
      return;

    IAQLLibrary aqlLib = Activator.getLibraryForProject (projectName);

    String location = project.getLocation ().toOSString ();
    List<String> aqlFilePaths = new ArrayList<String> ();
    if (ProjectUtils.isModularProject (project)) {
      AQLModuleProject aqlProject = aqlLib.getModuleLibraryMap ().get (location);
      if (aqlProject == null)
        return;

      aqlFilePaths = aqlProject.getAqlModuleFilePaths ();
    }
    else {
      AQLProject aqlProject = aqlLib.getLibraryMap ().get (location);
      if (aqlProject == null)
        return;

      aqlFilePaths = aqlProject.getAqlFilePaths ();
    }

    for (String filepath : aqlFilePaths) {
      List<AQLElement> views = aqlLib.getViews (filepath);
      if (views != null) {
        for (AQLElement view : views) {
          if (getQualifiedViewName (view).equals (input.getViewName ())) {
            input.setOffset (view.getBeginOffset ());
            input.setLine (view.getBeginLine ());
            input.setLength (view.getEndOffset () - view.getBeginOffset () + 1);

            IFile file = getFile (filepath);
            if (file.exists ()) {
              FileEditorInput fei = new FileEditorInput (file);
              ((ViewEditorInput) input).setFileEditorInput (fei);
              return;
            }
          }
        } // end for-loop through all AQLElements
      }
    }

    // If can't find the sub view, look for the main view. It should be there.
    for (String filepath : aqlFilePaths) {
      List<AQLElement> views = aqlLib.getViews (filepath);
      if (views != null) {
        for (AQLElement view : views) {
          if (getQualifiedViewName (view).equals (input.getMainViewName ())) {
            input.setOffset (view.getBeginOffset ());
            input.setLine (view.getBeginLine ());
            input.setLength (view.getEndOffset () - view.getBeginOffset () + 1);

            IFile file = getFile (filepath);
            if (file.exists ()) {
              FileEditorInput fei = new FileEditorInput (file);
              ((ViewEditorInput) input).setFileEditorInput (fei);
              return;
            }
          }
        } // end for-loop through all AQLElements
      }
    }
  }

  private static String getQualifiedViewName (AQLElement view)
  {
    String modulePrefix = (StringUtils.isEmpty (view.getModuleName ())) ? "" : view.getModuleName () + ".";
    return modulePrefix + view.getName ();
  }
}
