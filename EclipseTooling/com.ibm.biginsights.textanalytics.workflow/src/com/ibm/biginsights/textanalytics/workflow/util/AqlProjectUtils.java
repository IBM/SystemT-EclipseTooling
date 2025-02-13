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
package com.ibm.biginsights.textanalytics.workflow.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.avatar.algebra.datamodel.Tuple;
import com.ibm.avatar.api.DocReader;
import com.ibm.biginsights.project.ProjectNature;
import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.refactoring.ResourceChangeActionThread;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.run.SystemTRunConfig;
import com.ibm.biginsights.textanalytics.nature.run.SystemtRunJob;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.editors.TaggingEditor;
import com.ibm.biginsights.textanalytics.workflow.perspectives.TextAnalyticsPerspective;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.tasks.ExtractionTasksView;
import com.ibm.biginsights.textanalytics.workflow.tasks.models.DataFile;

@SuppressWarnings("deprecation")
public class AqlProjectUtils
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
 
	public static final String BASIC_FEATURES_FILE = "basic_features.aql";
  public static final String CONCEPTS_FILE = "concepts.aql";
  public static final String REFINEMENT_FILE = "refinement.aql";

  public static final String dataPath = "searchPath.dataPath";
  public static final String mainAql = "general.mainAQLFile";

  public static final String MAIN_AQL_FILENAME = "main.aql";
  public static final String MAIN_AQL_FOLDERNAME = "aql";

  public static boolean actionPlanOpening = false;

  /**
   * this method will create a new project in the current workspace
   * 
   * @param projectName
   * @throws CoreException
   */
  public static IProject createProject (String projectName) throws CoreException
  {
    IProgressMonitor progressMonitor = new NullProgressMonitor ();
    IWorkspaceRoot root = getWorkspaceRoot ();
    IProject project = root.getProject (projectName);
    project.create (progressMonitor);
    project.open (progressMonitor);
    return project;
  }

  public static IProject createTextAnalyticsProject (String projectName) throws CoreException, IOException
  {
    IProject project = createProject (projectName);
    addProjectNature (project, new String[] { ProjectNature.NATURE_ID });
    addTextAnalyticsConfiguration (project);
    return project;
  }

  public static IProject getProject (String projectName)
  {
    IWorkspaceRoot root = getWorkspaceRoot ();
    return root.getProject (projectName);
  }

  public static void refreshAndBuild (IProject project) throws CoreException
  {
    refresh (project);
    build (project);
  }

  public static void refresh (IProject project) throws CoreException
  {
    project.refreshLocal (IResource.DEPTH_INFINITE, null);
  }

  public static void build (IProject project) throws CoreException
  {
    project.build (IResource.BACKGROUND_REFRESH, new NullProgressMonitor ());
  }

  /**
   * gets a handler to the workspace root
   * 
   * @return
   */
  public static IWorkspaceRoot getWorkspaceRoot ()
  {
    return ResourcesPlugin.getWorkspace ().getRoot ();
  }

  public static Shell getActiveShell ()
  {
    return PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getActivePart ().getSite ().getShell ();
  }

  /**
   * adds a given array of nature ids to the given project
   * 
   * @param project
   * @param natureIds
   * @throws CoreException
   */
  public static void addProjectNature (IProject project, String[] natureIds) throws CoreException
  {

    IProgressMonitor progressMonitor = new NullProgressMonitor ();
    IProjectDescription pdesc = project.getDescription ();
    pdesc.setNatureIds (natureIds);
    project.setDescription (pdesc, progressMonitor);
    refresh (project);
  }

  public static boolean hasBINature (IProject project)
  {
    try {
      return project.hasNature (ProjectNature.NATURE_ID);
    }
    catch (CoreException e) {
      Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
      return false;
    }
  }

  /**
   * adds the text-analytics nature and create all the needed file structure.
   * 
   * @param project
   * @throws CoreException
   * @throws IOException
   */
  public static void addTextAnalyticsConfiguration (IProject project) throws CoreException, IOException
  {
    if (!ProjectUtils.isModularProject (project)) {
      IFolder aql_folder = createAqlFolder (project, project.getFolder (MAIN_AQL_FOLDERNAME));

      SystemTProperties props = ProjectPreferencesUtil.getSystemTProperties (project);
      String mainAql = props.getMainAQLFile ();
      File mainAqlFile = new File (ProjectPreferencesUtil.getAbsolutePath (mainAql));

      if (mainAqlFile == null || !mainAqlFile.exists ()) {
        IFile includes_aql = createAqlFile (project, aql_folder, MAIN_AQL_FILENAME);
        setMainAqlFile (project, includes_aql);
      }

      refreshAndBuild (project);
    }
  }

  public static void openMainAQL (String projectName) throws PartInitException
  {

    SystemTProperties props = ProjectPreferencesUtil.getSystemTProperties (projectName);
    String mainaqlpath = props.getMainAQLFile ();

    if (mainaqlpath == null || mainaqlpath.isEmpty ()) return;

    IPath path = new Path (ProjectPreferencesUtil.getPath (mainaqlpath));
    IFile mainAqlFile = getWorkspaceRoot ().getFile (path);

    if (mainAqlFile != null && mainAqlFile.exists () && mainAqlFile.getType () == IResource.FILE) {
      openAqlFileAndMoveCursor (mainAqlFile, 0, 0);
    }
  }

  /**
   * sets the provided file as the main aql file for the project provided
   * 
   * @param project
   * @param ifile
   * @throws IOException
   * @throws CoreException
   */
  public static void setMainAqlFile (IProject project, IFile ifile) throws IOException, CoreException
  {
    Properties props = loadTextAnalyticsProperties (project);
    String mainAql = Constants.WORKSPACE_RESOURCE_PREFIX + ifile.getFullPath ().toString ();
    props.setProperty (AqlProjectUtils.mainAql, mainAql);
    saveTextAnalyticsProperties (project, props);
  }

  /**
   * loads the current text analytics properties from the file for the given project
   * 
   * @param project
   * @return
   * @throws IOException
   * @throws CoreException
   */
  public static Properties loadTextAnalyticsProperties (IProject project) throws IOException, CoreException
  {
    IFile ifile = project.getFile (Constants.TEXT_ANALYTICS_PREF_FILE);
    InputStream is = ifile.getContents ();

    Properties props = new Properties ();
    props.load (is);
    is.close ();
    return props;
  }

  public static InputStream stringToStream (String str) throws UnsupportedEncodingException
  {
    return new ByteArrayInputStream (str.getBytes (Constants.ENCODING));
  }

  /**
   * saves the set of properties to the .textanalytics file (it actually overwrites any previous existing property)
   * 
   * @param project
   * @param props
   * @throws IOException
   * @throws CoreException
   */
  public static void saveTextAnalyticsProperties (IProject project, Properties props) throws IOException, CoreException
  {
    File f = project.getFile (Constants.TEXT_ANALYTICS_PREF_FILE).getLocation ().toFile ();
    FileOutputStream out = new FileOutputStream (f);
    props.store (out, null);
    out.close ();
    refresh (project);
  }

  /**
   * returns the folder that stores this project in the file system
   * 
   * @param project
   * @return
   */
  public static File getProjectRoot (IProject project)
  {
    return project.getLocation ().toFile ();
  }

  /**
   * @param iPath
   * @throws CoreException
   * @throws IOException
   */
  public static void addPathToDataPath (IProject project, IPath iPath) throws IOException, CoreException
  {
    Properties props = loadTextAnalyticsProperties (project);
    String dataPath = props.getProperty (AqlProjectUtils.dataPath);

    String[] actualPaths = dataPath.split (Constants.DATAPATH_SEPARATOR);
    String newPath = String.format ("%s%s", Constants.WORKSPACE_RESOURCE_PREFIX, iPath.toString ());

    for (String path : actualPaths) {
      if (path.equals (newPath)) {
        // path already exist
        return;
      }
    }
    // add the new path
    dataPath = String.format ("%s%s%s", dataPath, Constants.DATAPATH_SEPARATOR, newPath);

    props.setProperty (AqlProjectUtils.dataPath, dataPath);
    saveTextAnalyticsProperties (project, props);
  }

  /**
   * @param project
   * @param iPath
   * @throws CoreException
   * @throws IOException
   */
  public static void removePathFromDataPath (IProject project, IPath iPath) throws IOException, CoreException
  {
    Properties props = loadTextAnalyticsProperties (project);

    String dataPath = props.getProperty (AqlProjectUtils.dataPath);
    String[] entries = dataPath.split (Constants.DATAPATH_SEPARATOR);

    String path = Constants.WORKSPACE_RESOURCE_PREFIX + iPath.toOSString ();
    LinkedList<String> res = new LinkedList<String> ();

    for (String entry : entries) {
      if (!entry.equals (path) && !entry.isEmpty ()) {
        res.add (entry);
      }
    }
    dataPath = "";

    Iterator<String> ter = res.iterator ();
    if (ter.hasNext ()) dataPath = ter.next ();

    while (ter.hasNext ())
      dataPath = String.format ("%s%s%s", dataPath, Constants.DATAPATH_SEPARATOR, ter.next ());

    props.setProperty (AqlProjectUtils.dataPath, dataPath);
    saveTextAnalyticsProperties (project, props);
  }

  /**
   * creates an aql file in a given folder of the project
   * 
   * @param project
   * @param folderName
   * @param filename
   * @throws IOException
   * @throws CoreException
   */
  public static IFile createAqlFile (IProject project, IFolder folder, String filename) throws IOException, CoreException
  {

    InputStream inputStream = null;

    inputStream = Templates.getTemplate (Templates.DEFAULT_AQL_TLP);

    IFile ifile = folder.getFile (filename);

    if (!ifile.exists ()) ifile.create (inputStream, false, null); // false means don't create
    // the
    // file if the file already
    // exists

    refresh (project);

    return ifile;
  }

  /**
   * creates an aql folder and add it to the aql include path
   * 
   * @param project
   * @param folderPath
   * @throws CoreException
   * @throws IOException
   */
  public static IFolder createAqlFolder (IProject project, IFolder folder) throws CoreException, IOException
  {
    if (!folder.exists ()) folder.create (false, true, new NullProgressMonitor ());
    refresh (project);
    addPathToDataPath (project, folder.getFullPath ());
    return folder;
  }

  public static void removeAqlFolder (IFolder folder)
  {
    try {
      folder.delete (true, new NullProgressMonitor ());
    }
    catch (CoreException e) {
      Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
    }
  }

  /**
   * @param filename
   * @throws CoreException
   */
  public static void appendIncludeToMainAql (IProject project, String filename) throws CoreException
  {
    SystemTProperties props = ProjectPreferencesUtil.getSystemTProperties (project);
    String mainAql = props.getMainAQLFile ();
    File mainAqlFile = new File (ProjectPreferencesUtil.getAbsolutePath (mainAql));

    if (mainAqlFile.exists ()) {
      try {
        if (mainAqlFile.canWrite ()) {
          BufferedWriter br = new BufferedWriter (new FileWriter (mainAqlFile, true));
          br.write (String.format ("\ninclude '%s';", filename));
          br.close ();
        }
      }
      catch (IOException e) {
        Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
      }

    }
  }

  /**
   * @param project
   * @param filename
   * @throws Exception
   */
  public static void removeIncludeToMainAql (String projectName, String filename) throws Exception
  {
    SystemTProperties props = ProjectPreferencesUtil.getSystemTProperties (getProject (projectName));
    String mainAql = props.getMainAQLFile ();

    File mainAqlFile = new File (ProjectPreferencesUtil.getAbsolutePath (mainAql));

    if (mainAqlFile.exists ()) {
      try {
        if (mainAqlFile.canRead ()) {

          String content = FileUtils.fileToStr (mainAqlFile, Constants.ENCODING);
          content = content.replaceAll ("(\\n)?" + Pattern.quote (String.format ("include '%s';", filename)), "");

          mainAqlFile.delete ();
          mainAqlFile.createNewFile ();

          if (mainAqlFile.canWrite ()) {
            BufferedWriter br = new BufferedWriter (new FileWriter (mainAqlFile, false));
            br.write (content);
            br.close ();
          }
        }
      }
      catch (IOException e) {
        Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
      }

    }
  }

  /**
   * runs the systemt in the provided job and the set of documents in the run configuration
   * 
   * @param jobName
   * @param project
   * @param runConfig
   */
  public static void runSystemT (final String jobName,final IProject project,final SystemTRunConfig runConfig)
  {
  	SystemtRunJob job = new SystemtRunJob (jobName, project, runConfig);
  	job.schedule ();
  }

  /**
   * opens a data file in the editor with id provided
   * 
   * @param ifile
   * @throws PartInitException
   */
  public static IEditorPart openFile (IFile ifile, String editor_id) throws PartInitException
  {
    IWorkbenchPage page = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
    return page.openEditor (new FileEditorInput (ifile), editor_id);
  }

  /**
   * @param input
   * @param editor_id
   * @return
   * @throws PartInitException
   */
  public static IEditorPart openFile (IStorageEditorInput input, String editor_id) throws PartInitException
  {
    IWorkbenchPage page = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
    return page.openEditor (input, editor_id);
  }

  /**
   * opens an aql file, highlights the section specified and move the cursor to the beginning of this
   * 
   * @param ifile
   * @param offset
   * @param length
   * @return
   * @throws PartInitException
   */
  public static IEditorPart openAqlFileAndMoveCursor (IFile ifile, int offset, int length) throws PartInitException
  {
    IEditorPart editor = openFile (ifile, AQLEditor.EDITOR_ID);

    if (editor instanceof AQLEditor) ((AQLEditor) editor).setHighlightRange (offset, length, true);

    return editor;
  }

  /**
   * @param file
   * @return
   */
  public static IFile fileToIFile (File file)
  {
    IWorkspace workspace = ResourcesPlugin.getWorkspace ();
    IPath location = Path.fromOSString (file.getAbsolutePath ());
    IFile ifile = workspace.getRoot ().getFileForLocation (location);
    return ifile;
  }

  /**
   * @return
   */
  public static ActionPlanView getActionPlanView ()
  {
    ActionPlanView view = null;

    IWorkbench wb = PlatformUI.getWorkbench ();
    IWorkbenchPage page = wb.getActiveWorkbenchWindow ().getActivePage ();

    // --------------
    if (page != null) view = (ActionPlanView) page.findView (ActionPlanView.ID);

    return view;
  }
  
  public static IViewPart showView(String viewId){
    IWorkbench wb = PlatformUI.getWorkbench ();
    IWorkbenchPage page = wb.getActiveWorkbenchWindow ().getActivePage ();
    
    try {
      return page.showView (viewId);
    }
    catch (PartInitException e) {
      return null;
    }
  }
  
  public static void hideView(IViewPart view){
    IWorkbench wb = PlatformUI.getWorkbench ();
    IWorkbenchPage page = wb.getActiveWorkbenchWindow ().getActivePage ();
    
    page.hideView (view);
  }

  /**
   * @return
   */
  public static ExtractionTasksView getExtractionTasksView ()
  {
    ExtractionTasksView view = null;

    IWorkbench wb = PlatformUI.getWorkbench ();
    IWorkbenchPage page = wb.getActiveWorkbenchWindow ().getActivePage ();

    // --------------
    view = (ExtractionTasksView) page.findView (ExtractionTasksView.ID);

    return view;
  }

  public static void openHelp (String id)
  {
    PlatformUI.getWorkbench ().getHelpSystem ().displayHelp (id);
  }

  public static void openHelpUrl (String href)
  {
    com.ibm.datatools.quick.launch.ui.actions.OpenHelpAction helpAction = new com.ibm.datatools.quick.launch.ui.actions.OpenHelpAction (
      href);
    helpAction.run ();
  }

  /**
   * @param file
   * @return
   * @throws Exception
   */
  public static Map<String, String> getFilesContent (File file) throws Exception
  {

    DocReader docs = new DocReader (file);
    Map<String, String> result = new HashMap<String, String> ();

    while (docs.hasNext ()) {
      final Tuple docTuple = docs.next ();
      String contString = docs.getTextAcc ().getVal (docTuple).getText ();
      String relativeDocPath = docs.getLabelAcc ().getVal (docTuple).getText ();

      String docLabel = getUniqueLabel (result.keySet (), relativeDocPath);
      result.put (docLabel, contString);
    }

    return result;
  }

  /**
   * @param file
   * @param relativeDocPath
   * @return
   */
  public static String getDataFileContent (File file, String relativeDocPath)
  {
    String linuxPath = relativeDocPath.replaceAll ("\\\\", "/");

    try {
      Map<String, String> temp = getFilesContent (file);
      if (temp.containsKey (relativeDocPath))
        return temp.get (relativeDocPath);
      else if (temp.containsKey (linuxPath)) return temp.get (linuxPath);
    }
    catch (Exception e) {
      Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage())); // else that document cannot be located
      return null;
    }
    return null;
  }

  public static boolean isCollection (String path)
  {
    File file = com.ibm.avatar.algebra.util.file.FileUtils.createValidatedFile (path);
    if (file.isDirectory () || (path.matches (".*(\\.tar\\.gz|\\.tgz|\\.tar|\\.del|\\.zip)"))) { return true; }

    return false;
  }

  public static boolean isCollection (File file)
  {
    if (file.isDirectory () || (file.getAbsolutePath ().matches (".*(\\.tar\\.gz|\\.tgz|\\.tar|\\.del|\\.zip)"))) { return true; }
    return false;
  }

  public static List<String> getFilesFromCollection (File file) throws Exception
  {

    List<String> result = new LinkedList<String> ();

    if (!isCollection (file)) {
      result.add (file.getName ());
      return result;
    }

    DocReader docs = new DocReader (file);

    while (docs.hasNext ()) {
      final Tuple docTuple = docs.next ();
      String relativeDocPath = docs.getLabelAcc ().getVal (docTuple).getText ();
      String docLabel = getUniqueLabel (result, relativeDocPath);
      result.add (docLabel);
    }

    return result;
  }

  private static String getUniqueLabel (Collection<String> existingLabels, String newLabel)
  {
    int index = 1;
    String docLabel = new String (newLabel);

    while (existingLabels.contains (docLabel)) {
      docLabel = newLabel + "(" + ++index + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }

    return docLabel;
  }

  public static boolean resetExtractionPlanForProject (String projectname)
  {

    if (ActionPlanView.projectName == null) return false;

    if (ActionPlanView.projectName.equals (projectname)) {
      ActionPlanView plan = getActionPlanView ();
      if (plan != null) {
        plan.reset ();
      }
      else {
        ActionPlanView.projectName = null;
        ActionPlanView.isReady = false;
      }
      ExtractionTasksView tasks = getExtractionTasksView ();
      if (tasks != null) tasks.reset ();

      return true;
    }

    return false;
  }

  public static String getExtractionPlanProjectName ()
  {
    return ActionPlanView.projectName != null ? ActionPlanView.projectName : "";
  }

  /**
   * Reload the extraction plan of a project if it is being opened.
   * @param projName The name of project whose extraction plan is reloaded if currently being opened.
   * @return TRUE if reload happens; FALSE otherwise.
   */
  public static boolean reloadExtractionPlanForProject (String projName)
  {
    return reloadExtractionPlanForProject (projName, projName);
  }

  /**
   * Reload the extraction plan of a project if it is being opened. The newProjectName
   * is likely null or the same as projectName; it is different only when project is renamed.
   * @param projName The name of project whose extraction plan is reloaded if currently being opened.<br>
   *        If projName is null, the current project in extraction plan wil be reloaded.
   * @param newProjName The new project name, needed when project is renamed.<br>
   *        If newProjName is null, it will be interpretted as load the same project as projname. 
   * @return TRUE if reload happens; FALSE otherwise.
   */
  public static boolean reloadExtractionPlanForProject (String projName, String newProjName)
  {
    ActionPlanView plan = getActionPlanView ();
    if (plan != null && plan.ready ()) {

      if (projName == null)     // will reload existing EP
        projName = ActionPlanView.projectName;

      if (newProjName == null)  // will reload the old EP
        newProjName = projName;

      // reload when the old EP is the one being loaded
      if (ActionPlanView.projectName.equals (projName)) {
        plan.refreshView (newProjName);
        return true;
      }
    }

    return false;
  }

  public static ExampleModel getDataFileForTaggingEditor (String txt)
  {
    IEditorPart ieditor = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getActiveEditor ();

    ExampleModel model = null;

    if (ieditor instanceof TaggingEditor) {
      TaggingEditor editor = (TaggingEditor) ieditor;
      IEditorInput input = editor.getEditorInput ();

      ITextSelection textSelection = (ITextSelection) editor.getSelectionProvider ().getSelection ();

      int offset = textSelection.getOffset ();
      int length = textSelection.getLength ();
      String text = textSelection.getText ();

      if (input instanceof StringInput) {
        if (((StringInput) input).getStorage () instanceof DataFile) {
          DataFile dfile = (DataFile) ((StringInput) input).getStorage ();
          model = new ExampleModel (text, dfile.getPath (), dfile.getLabel (), offset, length);
        }
      }
      else {
        IFile afile = ((IFile) input.getAdapter (IFile.class));
        String path = afile.getFullPath ().toString ();
        DataFile dfile = new DataFile (afile.getName (), path);
        model = new ExampleModel (text, dfile.getPath (), dfile.getLabel (), offset, length);
      }
    }

    if (model != null) {
      if (!model.getText ().equals (txt)) return null;
    }

    return model;
  }

  public static boolean isWindowsOS ()
  {
    String OS = System.getProperty("os.name").toLowerCase();
    return OS.contains ("win");   //$NON-NLS-1$
  }

  public static String[] getViewInCurrentEditor (String viewName)
  {
    IEditorPart editor = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getActiveEditor ();

    if (editor instanceof AQLEditor) {
      IFile aqlFile = ((IFileEditorInput)editor.getEditorInput ()).getFile();

      if ( aqlFile != null &&
           aqlFile.getProject () != null &&
           aqlFile.getProject ().getName ().equals (ActionPlanView.projectName) ) {

        String [] info = new String [] { viewName, aqlFile.getName (), "" };

        // modular project
        if (ProjectUtils.isModularProject (aqlFile.getProject ())) {
          String moduleName = aqlFile.getParent ().getName ();

          // verify that this module exists
          IFolder moduleFolder = ProjectUtils.getModuleFolder (ActionPlanView.projectName, moduleName);
          if ( moduleFolder != null && moduleFolder.exists () )
            info [2] = moduleName;
          else
            info = null;
        }
        // If this is a non-modular project, keep the full path
        else {
          info [2] = aqlFile.getLocation ().toFile ().getAbsolutePath ();
        }

        return info;

      }
    }

    return null;
  }

  public static void createLibraryForProject (String projectName)
  {
    new ResourceChangeActionThread (null, null, ResourcesPlugin.getWorkspace ().getRoot ().findMember (
      new File (projectName).getName ())).start ();
  }

  public static void openActionPlan (String projectName) throws WorkbenchException
  {
    actionPlanOpening = true;

    try {
      // --------------
      IWorkbench wb = PlatformUI.getWorkbench ();
      IWorkbenchWindow window = wb.getActiveWorkbenchWindow ();
      // --------------

      wb.showPerspective (TextAnalyticsPerspective.ID, window);
      IWorkbenchPage page = window.getActivePage ();

      // --------------
      ActionPlanView a_view = (ActionPlanView) page.findView (ActionPlanView.ID);

      boolean next = true;
      if (a_view != null) {
        // the is already opened, handle this here
        next = a_view.refreshView (projectName);
      }
      if (next){
        ActionPlanView.projectName = projectName;
        a_view = (ActionPlanView) page.showView (ActionPlanView.ID);
      }

      // Open Extraction Tasks view
      page.showView (ExtractionTasksView.ID);

      // --------------
      if ( a_view != null  &&
           a_view.ready () &&
           !ProjectUtils.isModularProject (ActionPlanView.projectName) ) {
        a_view.openMainAQL ();
      }
      // --------------
      // --------------
    }
    catch (WorkbenchException e) {
      throw e;
    }
    finally {
      actionPlanOpening = false;
    }
  }

  public static boolean isActionPlanOpening ()
  {
    return actionPlanOpening;
  }
}
