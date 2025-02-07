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
package com.ibm.biginsights.textanalytics.util.common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.ibm.avatar.algebra.datamodel.Pair;
import com.ibm.avatar.algebra.datamodel.TupleSchema;
import com.ibm.avatar.api.OperatorGraph;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.avatar.api.tam.DictionaryMetadata;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.api.tam.ModuleMetadataFactory;
import com.ibm.avatar.api.tam.TableMetadata;
import com.ibm.biginsights.textanalytics.util.Activator;
import com.ibm.biginsights.textanalytics.util.Messages;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

@SuppressWarnings("deprecation")
public class ProjectUtils
{
  @SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  private static final ILog logger = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);
  private static final String FILE_SEPERATOR = File.separator;

  public static final String[] KEYWORDS = { "and", "all", "allow_empty", "allow empty_fileset", "always", "annotate",
    "as", "ascending", "ascii", "attribute", "between", "blocks", "both", "by", "called", "case", "cast", "ccsid",
    "character", "characters", "columns", "consolidate", "content_type", "count", "create", "default", "descending",
    "detag", "detect", "deterministic", "dictionary", "dictionaries", "document", "element", "else", "entries",
    "exact", "export", "external", "external_name", "extract", "false", "fetch", "file", "first", "flags", "folding",
    "from", "function", "group", "having", "import", "in", "include", "inline_match", "input", "into", "insensitive",
    "java", "language", "left", "lemma_match", "like", "limit", "matchingRegex", "mapping", "minus", "module", "name",
    "never", "not", "null", "on", "only", "order", "output", "part_of_speech", "parts_of_speech", "parameter",
    "pattern", "point", "points", "priority", "regex", "regexes", "require", "required", "retain", "return", "right", "rows",
    "select", "separation", "set", "specific", "split", "table", "tagger", "then", "token", "tokens", "Token", "true",
    "up", "unicode", "union", "using", "values", "view", "views", "when", "where", "with" };

  public static final String[] TYPES = { "Text", "Span", "Integer", "Float", "String", "Boolean", "ScalarList" };

  public static final String[] RESERVED_NAMES = { "Dictionary", "Regex", "Consolidate", "Block", "BlockTok",
    "Sentence", "Tokenize", "RegexTok", "PosTag", "Document" };

  public static final String[] BUILT_IN_FUNCS = { "And", "Contains", "ContainsDict", "ContainsRegex", "Equals",
    "Follows", "FollowsTok", "GreaterThan", "MatchesDict", "MatchesRegex", "Not", "NotNull", "Or", "Overlaps", "Chomp",
    "CombineSpans", "GetBegin", "GetEnd", "GetLanguage", "GetLength", "GetLengthTok", "GetString", "GetText",
    "LeftContext", "RightContext", "LeftContextTok", "RightContextTok", "Remap", "SpanBetween", "SpanIntersection",
    "SubSpanTok", "ToLowerCase", "Avg", "Count", "List", "Max", "Min", "Sum" };

  /**
   * Returns a set of module names that have error markers set in their AQL's. In this method Even if there is a project
   * level errors(such as bin folder not found or referenced project not found) will be ignored and only module level
   * error will be checked.
   * 
   * @param project the project whose modules will be checked for error markers.
   * @return a set of module names that have error markers set in them.
   */
  public static Set<String> getModulesWithErrorMarkers (IProject project)
  {
    // A set to hold error module names.
    Set<String> ret = new HashSet<String> ();
    // Get IFolder resource of the src path of the project.
    IFolder srcPathRes = getTextAnalyticsSrcFolder (project);
    // Get each module of the project and check for error markers.
    String modules[] = ProjectUtils.getModules (project);
    for (String module : modules) {
      IFolder moduleDir = srcPathRes.getFolder (module);
      try {
        IMarker[] markers = moduleDir.findMarkers (IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        // If error markers found then add to the set.
        if (markers != null && markers.length > 0) {
          ret.add (module);
        }
      }
      // If found any exception for current module then continue with next module.
      catch (CoreException e) {
        continue;
      }
    }
    return ret;
  }

  /**
   * Returns <code>true</code>, only if there are error markers specifically at the Project level, discounting the
   * module level project markers. Remember, that the module level error markers are also propagated up to the project
   * level. However, this method is interested only to find out if the project has any additional errors, than the ones
   * Propagated from its modules. Examples of such Project level errors include: Referenced project not found;
   * textAnalytics bin path not found; Java src folder not configured etc
   * 
   * @param project that has to be verified for project level error markers.
   * @return true if project level error markers exist other wise false.
   */
  public static boolean hasProjectErrors (IProject project)
  {
    if (ProjectUtils.isModularProject (project)) {
      // count of all error markers on project including its modules.
      int projectErrorMarkerCount = 0;
      // count of error markers on all modules of the project.
      int modulesErrorMarkerCount = 0;
      try {
        modulesErrorMarkerCount = getModuleLevelErrorMarkers (project).size ();
        // Find count of all the error markers of the project
        IMarker[] projMarkers = project.findMarkers (IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        // Add only error markers and not warnings to the project error markers count
        for (IMarker iMarker : projMarkers) {
          // the value of error marker is 2 that is why the comparison is made between numeric 2.
          if (iMarker.getAttribute (IMarker.SEVERITY).equals (IMarker.SEVERITY_ERROR)) {
            projectErrorMarkerCount += 1;
          }
        }

      }
      catch (CoreException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
      }
      // If the count remains same for both module level errors and project level errors then return false
      if (modulesErrorMarkerCount < projectErrorMarkerCount) {
        return true;
      }
      else {
        return false;
      }
    }
    return false;
  }

  /**
   * Gets all the error markers that are at the module level. Only those markers associated with IFolder resource of
   * module is fetched.
   * 
   * @param project for which the module level errors have to be found out.
   * @return a list of all the error markers at the module level for all the modules in the project.
   * @throws CoreException during finding error markers for IResource.
   */
  public static List<IMarker> getModuleLevelErrorMarkers (IProject project) throws CoreException
  {
    // Get IFolder resource of the src path of the project.
    IFolder srcPathRes = getTextAnalyticsSrcFolder (project);
    // Array of markers to store all the markers of the modules in the project
    List<IMarker> markers = new ArrayList<IMarker> ();
    // Get each module of the project and check for error markers.
    String modules[] = ProjectUtils.getModules (project);

		if (modules != null) {
			// Add all the error marker counts for each module in this project.
			for (String module : modules) {
				IFolder moduleDir = srcPathRes.getFolder (module);
				// get all markers of the module
				IMarker[] moduleMarkers = moduleDir.findMarkers (IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				// add only error markers and not warnings.
				for (IMarker iMarker : moduleMarkers) {
					if (iMarker.getAttribute (IMarker.SEVERITY).equals (IMarker.SEVERITY_ERROR)) {
						markers.add (iMarker);
					}
				}
			}
		}
    return markers;
  }

  public static boolean hasBuildErrors (IProject project)
  {
    String errorType = null;
    try {
      IMarker[] markers = project.findMarkers (IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
      if (markers != null) {
        for (IMarker marker : markers) {
          Integer severity = (Integer) marker.getAttribute (IMarker.SEVERITY);
          errorType = marker.getType ();
          if (severity.intValue () == IMarker.SEVERITY_ERROR
            && (Constants.AQL_PARSE_ERROR_TYPE.equals (errorType) || Constants.AQL_COMPILE_ERROR_TYPE.equals (errorType))) { return true; }
        }
      }
    }
    catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    return false;
  }

  public static Set<String> getModulesWithError (IProject project)
  {
    String errorType = null;
    Set<String> moduleSet = new HashSet<String> ();
    try {
      IMarker[] markers = project.findMarkers (IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
      if (markers != null) {

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
        String relativeSRCPath = ProjectUtils.getConfiguredModuleSrcPath (project);
        if (relativeSRCPath == null) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
            (MessageFormat.format (Messages.getString ("ProjectUtils.MODULE_SRC_FOLDER_DETERMINATION_ERROR"),
              new Object[] { project.getName () })));
          return moduleSet;
        }
        IFolder srcPathRes = root.getFolder (new Path (relativeSRCPath));
        String absSrcPath = srcPathRes.getLocation ().toOSString ();
        String modules[] = ProjectUtils.getModules (project);

        if (modules == null) return moduleSet;

        for (IMarker marker : markers) {
          Integer severity = (Integer) marker.getAttribute (IMarker.SEVERITY);
          errorType = marker.getType ();
          if (severity.intValue () == IMarker.SEVERITY_ERROR
            && (Constants.AQL_PARSE_ERROR_TYPE.equals (errorType) || Constants.AQL_COMPILE_ERROR_TYPE.equals (errorType))) {
            IResource resource = marker.getResource ();
            // If the error is marked on the Project then the Tooling
            // will not be able to find the module name. In this case, we return
            // all modules in that project.
            if (resource instanceof IProject) {
              moduleSet.addAll (Arrays.asList (getAllModules (project)));
              return moduleSet;
            }

            if (resource instanceof IFile) {
              IPath path = resource.getLocation ();
              for (String module : modules) {
                IPath modulePath = new Path (absSrcPath + IPath.SEPARATOR + module + IPath.SEPARATOR);
                if (modulePath.isPrefixOf (path)) {
                  moduleSet.add (module);
                }
              }

            }

          }
        }
      }
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
    }
    return moduleSet;
  }

  public static IProject getSelectedProject ()
  {
    ISelection selection = getActivePage ().getSelection ();
    IProject project = null;

    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structSelection = (IStructuredSelection) selection;
      Object element = structSelection.getFirstElement ();

      if (element instanceof IProject) {
        project = (IProject) element;
      }
      else if (element instanceof IJavaElement) {
        project = ((IJavaElement) element).getJavaProject ().getProject ();
      }
      else {

        if (element instanceof IFolder || element instanceof IFile) {
          return ((IResource) element).getProject ();
        }
        else if (element instanceof IAdaptable) {
          try {
            project = (IProject) ((IAdaptable) element).getAdapter (IProject.class);
          }
          catch (Exception e) {
            project = ((IResource) element).getProject ();
            e.printStackTrace ();
          }
        }
      }
    }

    return project;
  }

  public static ISelection getSelection ()
  {
    return getActivePage ().getSelection ();
  }

  public static IResource getSelectedResource ()
  {
    ISelection selection = ProjectUtils.getSelection ();
    if (selection != null && selection instanceof IStructuredSelection) {
      IStructuredSelection structSelection = (IStructuredSelection) selection;
      Object element = structSelection.getFirstElement ();
      if (element instanceof IResource) {
        IResource selectedResource = (IResource) element;
        return selectedResource;
      }
    }
    return null;
  }

  public static IWorkbenchPage getActivePage ()
  {
    IWorkbenchWindow w = getActiveWorkbenchWindow ();
    if (w != null) { return w.getActivePage (); }
    return null;
  }

  public static IWorkbenchWindow getActiveWorkbenchWindow ()
  {
    return PlatformUI.getWorkbench ().getActiveWorkbenchWindow ();
  }

  public static IProject getProject (String projectName)
  {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace ().getRoot ();

    try {
      IProject project = workspaceRoot.getProject (projectName);
      return project;
    }
    catch (Exception e) {
      // e.printStackTrace();
      return null;
    }
  }

  public static boolean isUTF8Encoding (IResource selectedResource)
  {
    Charset utf8 = Charset.forName (Constants.ENCODING);
    try {
      String encodingName = null;
      if (selectedResource instanceof IFile) {
        encodingName = ((IFile) selectedResource).getCharset ();
      }
      else if (selectedResource instanceof IFolder) {
        encodingName = ((IFolder) selectedResource).getDefaultCharset ();
      }

      if (encodingName == null) { return false; }
      return utf8.equals (Charset.forName (encodingName));
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
    }

    return false;
  }

  /**
   * Pass a file path to this method to get the project within which this file is contained
   * 
   * @param aqlFilePath
   * @return IProject
   */
  public static IProject getProjectFromFilePath (String aqlFilePath)
  {
    IPath path = new Path (aqlFilePath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation (path);
    if (file == null)
      return null;
    else {
      return file.getProject ();
    }
  }

  public static ILaunchConfiguration[] getRunConfigsByProject (String projectName)
  {
    try {
      ILaunchManager launchManager = DebugPlugin.getDefault ().getLaunchManager ();
      ILaunchConfigurationType runType = launchManager.getLaunchConfigurationType (Constants.RUN_CONFIG_TYPE);
      ILaunchConfiguration[] runConfigs = launchManager.getLaunchConfigurations (runType);
      return filterByProjectName (runConfigs, projectName);
    }
    catch (Exception e) {
      return new ILaunchConfiguration[0];
    }
  }

  public static ILaunchConfiguration[] getProfileConfigsByProject (String projectName)
  {
    try {
      ILaunchManager launchManager = DebugPlugin.getDefault ().getLaunchManager ();
      ILaunchConfigurationType profileType = launchManager.getLaunchConfigurationType (Constants.PROFILE_CONFIG_TYPE);
      ILaunchConfiguration[] profileConfigs = launchManager.getLaunchConfigurations (profileType);
      return filterByProjectName (profileConfigs, projectName);
    }
    catch (Exception e) {
      return new ILaunchConfiguration[0];
    }
  }

  private static ILaunchConfiguration[] filterByProjectName (ILaunchConfiguration[] configs, String projectName) throws CoreException
  {

    if (configs == null) {
      return new ILaunchConfiguration[0];
    }
    else {
      ArrayList<ILaunchConfiguration> filteredConfigs = new ArrayList<ILaunchConfiguration> ();
      for (ILaunchConfiguration config : configs) {
        String projNameProperty = config.getAttribute (IRunConfigConstants.PROJECT_NAME, ""); //$NON-NLS-1$
        if (projNameProperty.equals (projectName)) {
          filteredConfigs.add (config);
        }
      }
      return filteredConfigs.toArray (new ILaunchConfiguration[filteredConfigs.size ()]);
    }
  }

  /**
   * This method searches for the root result folder in the project. This is called from SystemRunJob so as to not
   * create it again on execution if user has renamed the default one.
   * 
   * @param project
   * @return
   */
  public static IFolder getRootResultFolder (IProject project)
  {
    IFolder folder;
    boolean setResultParent = false;
    String resultRootDir = null;

    PreferenceStore prefStore = ProjectUtils.getPreferenceStore (project);
    if (prefStore != null) {
      resultRootDir = prefStore.getString (Constants.RESULT_ROOT_DIR);
    }
    if (StringUtils.isEmpty (resultRootDir)) {
      resultRootDir = Constants.DEFAULT_ROOT_RESULT_DIR;
      setResultParent = true;
    }

    folder = project.getFolder (resultRootDir);
    try {
      if (!folder.exists ()) {
        folder.create (true, true, null);
      }
    }
    catch (CoreException e) {
      logger.logAndShowError (e.getLocalizedMessage (), e);
    }

    if (setResultParent) {
      setResultRootDir (folder);
    }

    return folder;
  }

  /**
   * Checks whether the input folder is a result folder or not
   * 
   * @param folder
   * @return true, if the input folder is a result folder, false otherwise
   */
  public static boolean isResultFolder (IFolder folder)
  {
    if (folder == null || !folder.exists ()) { return false; }

    // Checking the current folder is under result folder or not
    String parentFolderName = folder.getParent ().getName ();
    String resultRootFolder = "";//$NON-NLS-1$
    try {
      PreferenceStore prefStore = getPreferenceStore (folder.getProject ());
      resultRootFolder = (prefStore.getString (Constants.RESULT_ROOT_DIR));
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logWarning (
        Messages.getString ("ProjectUtils_UNABLE_TO_DECIDE_IF_ROOT_RESULT_DIR") + folder.getName ()); //$NON-NLS-1$
      return false;
    }
    boolean isResultFolder = (parentFolderName.equals (Constants.DEFAULT_ROOT_RESULT_DIR) || (parentFolderName.equals (resultRootFolder)));
    if (!isResultFolder) { return false; }

    IResource[] members;
    try {
      members = folder.members ();
    }
    catch (CoreException e) {
      logger.logError (e.getMessage (), e);
      return false;
    }
    for (IResource member : members) {
      if (member instanceof IFile) {
        if (Constants.STRF_FILE_EXTENSION.equals (((IFile) member).getFileExtension ())) { return true; }
      }
    }
    return false;
  }

  /**
   * Checks if the folder is a module folder in the src folder.
   * 
   * @param folder to be checked to be a module under src or not.
   * @return true if the folder is a module within a Text Analytics project, else false
   */
  public static boolean isModuleFolder (IFolder folder)
  {
    IFolder srcPath = getTextAnalyticsSrcFolder (folder.getProject ());
    // compare the paths of folders parent with the configured src folder of the project.
    if (srcPath != null) {
      return folder.getParent ().getLocation ().equals (srcPath.getLocation ());
    } else {
      return false;
    }   
  }

  /**
   * Returns the IFolder resource of the configured bin directory of the project.
   * 
   * @param project whose bin folder has to be found.
   * @return IFolder resource of the configured bin path.
   */
  public static IFolder getConfiguredModuleBinDir (IProject project)
  {
    String configuredBinPath = ProjectUtils.getConfiguredModuleBinPath (project);
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    IFolder binFolder = root.getFolder (new Path (configuredBinPath));
    return binFolder;
  }

  /**
   * Checks whether the input folder is a module's bin folder
   * 
   * @param folder
   * @return true, if the input folder is a module's bin folder, false otherwise
   */
  public static boolean isConfiguredBinFolder (IFolder folder)
  {
    if (folder == null) { return false; }
    IFolder moduleBinPath = getTextAnalyticsBinFolder (folder.getProject ());
    if (moduleBinPath == null) return false;
    if (moduleBinPath.getLocation ().equals (folder.getLocation ())) { return true; }
    return false;
  }

  /**
   * Checks whether the input folder is valid module's folder
   * 
   * @param folder
   * @return true, if the input folder is a module's folder, false otherwise
   */
  public static boolean isIntrestedModuleFolder (IFolder folder)
  {
    IPath path = folder.getLocation ();
    String aqlFilePath = path.toOSString ();
    if (aqlFilePath == null) return false;
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    IProject project = folder.getProject ();
    String relativeSRCPath = ProjectUtils.getConfiguredModuleSrcPath (project.getName ());
    // If the relativeSRCPath is null, then we cannot determine the module path, hence we return false
    if (null == relativeSRCPath) return false;
    IFolder srcPathRes = root.getFolder (new Path (relativeSRCPath));
    if (!srcPathRes.exists ()) return false;

    String absSrcPath = srcPathRes.getLocation ().toOSString ();
    if (absSrcPath == null) return false;
    if (aqlFilePath.startsWith (absSrcPath)
      && aqlFilePath.replace (absSrcPath + FILE_SEPERATOR, "").indexOf (FILE_SEPERATOR) == -1) { return true; } //$NON-NLS-1$
    return false;
  }

  /**
   * Checks whether the input folder is a textAnalytics source path folder
   * 
   * @param folder
   * @return true, if the input folder is a textAnalytics source path folder, false otherwise
   */
  public static boolean isConfiguredSrcFolder (IFolder folder)
  {
    if (folder == null) { return false; }
    IFolder moduleSrcPath = getTextAnalyticsSrcFolder (folder.getProject ());
    if (moduleSrcPath == null) return false;
    if (moduleSrcPath.getLocation ().equals (folder.getLocation ())) { return true; }
    return false;
  }

  /**
   * Checks whether the input folder is a root result directory folder or not
   * 
   * @param folder
   * @return true, if the input folder is a root result directory, false otherwise
   */
  public static boolean isResultRootDir (IFolder folder)
  {
    try {
      /*
       * Note: Do not check for folder.exists() because during delete event of result root folder the removedResource
       * might not exist, when this method is invoked.
       */
      if (folder == null) { return false; }
      PreferenceStore prefStore = getPreferenceStore (folder.getProject ());
      return folder.getName ().equals (prefStore.getString (Constants.RESULT_ROOT_DIR));
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logWarning (
        Messages.getString ("ProjectUtils_UNABLE_TO_DECIDE_IF_ROOT_RESULT_DIR") + folder.getName ()); //$NON-NLS-1$
      return false;
    }
  }

  public static void setResultRootDir (IFolder folder)
  {
    if (folder == null || !folder.exists ()) { return; }
    try {
      PreferenceStore prefStore = getPreferenceStore (folder.getProject ());
      if (prefStore != null) {
        prefStore.setValue (Constants.RESULT_ROOT_DIR, folder.getName ());
        prefStore.save ();
      }
    }
    catch (IOException e) {
      String formattedMsg = MessageUtil.formatMessage (
        Messages.getString ("ProjectUtils.CANNOT_SET_AS_RESULT_ROOT_DIR"), //$NON-NLS-1$
        folder.getName ());
      logger.logAndShowError (formattedMsg, e);
    }
  }

  public static PreferenceStore getPreferenceStore (IProject project)
  {
    try {
      if (true == project.isOpen ()
        && true == project.hasNature (com.ibm.biginsights.textanalytics.util.common.Constants.PLUGIN_NATURE_ID)) {
        PreferenceStore preferenceStore = new PreferenceStore (getPreferenceStoreFile (project).getAbsolutePath ());
        preferenceStore.load ();
        return preferenceStore;
      }
    }
    catch (Exception e) {
      logger.logWarning (e.getMessage ());
    }
    return null;
  }

  /**
   * From the given array of referenced projects, derive the Text Analytics ones and
   * return an array of paths to their configured bin directory. Return an empty array
   * if there is no Text Analytics projects in the given array.
   * @param root
   * @param refProject
   * @return
   */
  public static String[] getProjectDependencyPaths (IWorkspaceRoot root, IProject[] refProject)
  {
    String dependents[] = new String[] {};
    try {
      if (refProject != null) {
        List<String> depList = new ArrayList<String> ();
        for (IProject iProject : refProject) {
          if (iProject.hasNature (Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject (iProject)) {
            String binPath = ProjectUtils.getConfiguredModuleBinPath (iProject);
            if (binPath != null) {
              IFolder srcTempPathRes = root.getFolder (new Path (binPath));
              String absTempBinPath = srcTempPathRes.getLocation ().toOSString ();
              depList.add (absTempBinPath);
            }
          }
        }
        if (!depList.isEmpty ()) {
          dependents = depList.toArray (new String[0]);
        }
      }
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
    }
    return dependents;
  }

  public static boolean isModularProject (String project)
  {
    return isModularProject (getProject (project));
  }

  public static boolean isModularProject (IProject project)
  {
    PreferenceStore preferenceStore = getPreferenceStore (project);
    if (preferenceStore != null) { return preferenceStore.getBoolean (Constants.MODULAR_AQL_PROJECT); }
    return false;
  }

  /**
   * Verifies if the given project name & module name is a valid combination
   * 
   * @param project IProject where the given module is to be found
   * @param moduleName Name of the module to find
   * @return true, if the given module exists under the specified project. Else, returns false;
   */
  public static boolean isValidModule (IProject project, String moduleName)
  {
    if (project == null || moduleName == null) { return false; }

    IFolder folder = getTextAnalyticsSrcFolder (project);
    if (null == folder) return false;

    try {
      return project.isOpen () && isModularProject (project)
        && getTextAnalyticsSrcFolder (project).getFolder (moduleName).exists ();
    }
    catch (Exception e) {
      return false;
    }
  }

  /**
   * A valid AQL file in v2.0 means having extension .aql and parent is a module. aql files that are in sub folder of a
   * module are not considered valid.
   * 
   * @param aqlFile
   * @return
   */
  public static boolean isValidAQLFile20 (IFile aqlFile)
  {
    if (aqlFile != null && aqlFile.getName ().toLowerCase ().endsWith (Constants.AQL_FILE_EXTENSION_STRING)) {

      IProject project = aqlFile.getProject ();
      if (!isModularProject (project)) // we don't have this 'valid' concept in pre-2.0, so return true.
        return true;

      IContainer parent = aqlFile.getParent ();
      if (parent instanceof IFolder && isValidModule (project, parent.getName ())) return true;

    }

    return false;
  }

  /**
   * Verifies if the given project name & module name is a valid combination
   * 
   * @param projectName Name of the project where the given module is to be found
   * @param moduleName Name of the module to find
   * @return true, if the given module exists under the specified project. Else, returns false;
   */
  public static boolean isValidModule (String projectName, String moduleName)
  {
    return isValidModule (getProject (projectName), moduleName);
  }

  /**
   * Reads a project's text analytics properties to find source folder for the project's text analytics modules. It
   * assumes the path stored in properties is relative to the project.
   * 
   * @param project Name of the project
   * @return String representation of the text analytics source folder location, relative to the workspace root.
   */
  public static String getConfiguredModuleSrcPath (String project)
  {
    IProject proj = getProject (project);
    return getConfiguredModuleSrcPath (proj);
  }

  /**
   * Reads a project's text analytics properties to find source folder for the project's text analytics modules. It
   * assumes the path stored in properties is relative to the project.
   * 
   * @param project IProject instance
   * @return String representation of the text analytics source folder location, relative to the workspace root.
   */
  public static String getConfiguredModuleSrcPath (IProject project)
  {
    if (project == null || !(project.exists ())) { return null; }
    PreferenceStore store = ProjectUtils.getPreferenceStore (project);
    if (store == null) return null;

    String srcPath = store.getString (Constants.MODULE_SRC_PATH);
    IFolder srcFolder = null;
    if (!StringUtils.isEmpty (srcPath) && srcPath.startsWith (Constants.PROJECT_RELATIVE_PATH_PREFIX))
      srcFolder = project.getFolder (srcPath.replace (Constants.PROJECT_RELATIVE_PATH_PREFIX, "")); //$NON-NLS-1$

    if (srcFolder == null) {
      return null;
    }
    else {
      return srcFolder.getFullPath ().toString ();
    }
  }

  public static String getProjectDependency (String project)
  {
    IProject proj = getProject (project);

    PreferenceStore store = ProjectUtils.getPreferenceStore (proj);
    if (store != null) { // store can be null; e.g., when project is closed.
      String dep = store.getString (Constants.DEPENDENT_PROJECT);
      if (dep != null)
        return store.getString (Constants.DEPENDENT_PROJECT).replace (Constants.WORKSPACE_RESOURCE_PREFIX, ""); //$NON-NLS-1$
    }

    return null;
  }

  public static String getImportedTams (String project)
  {
    IProject proj = getProject (project);
    return getImportedTams (proj);
  }

  public static String getImportedTams (IProject project)
  {
    PreferenceStore store = getPreferenceStore (project);
    String tams = store.getString (Constants.TAM_PATH);
    if (tams != null) {
      // return tams.replace (Constants.WORKSPACE_RESOURCE_PREFIX, "");
      return tams;
    }
    else {
      return null;
    }
  }

  /**
   * Converts relative paths to uris, with ; as delimiter
   * 
   * @param paths paths relative to workspace, with ; as delimiter. Each path should be prefixed with the workspace
   *          resource prefix
   * @see com.ibm.biginsights.textanalytics.util.common.Constants#WORKSPACE_RESOURCE_PREFIX
   * @return
   */
  public static String getURIsFromWorkspacePaths (String paths)
  {
    String[] relativePaths = paths.split (Constants.DATAPATH_SEPARATOR);
    String uriString = ""; //$NON-NLS-1$
    for (int i = 0; i < relativePaths.length; i++) {
      if (!relativePaths[i].isEmpty ()) {
        String absolutePath = deduceAbsolutePath (relativePaths[i]);
        if (absolutePath != null && !absolutePath.isEmpty ())
          uriString += new File (absolutePath).toURI ().toString () + Constants.DATAPATH_SEPARATOR;
      }
    }
    if (uriString.endsWith (Constants.DATAPATH_SEPARATOR)) {
      uriString = uriString.substring (0, uriString.length () - 1);
    }
    return uriString;
  }

  public static String deduceAbsolutePath (String path)
  {
    if (StringUtils.isEmpty (path)) {
      return ""; //$NON-NLS-1$
    }
    else {
      if (isWorkspaceResource (path)) {
        path = getPath (path);
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace ().getRoot ();
        IResource resource = workspaceRoot.findMember (new Path (path));
        if (resource == null) { return null; }
        return resource.getLocation ().toString ();
      }
      else {
        // it is already an absolute path, so just return it
        return path;
      }
    }
  }

  public static boolean isWorkspaceResource (String path)
  {
    if (path == null) {
      return false;
    }
    else {
      return path.startsWith (Constants.WORKSPACE_RESOURCE_PREFIX);
    }
  }

  public static String getPath (String path)
  {
    if (StringUtils.isEmpty (path)) {
      return ""; //$NON-NLS-1$
    }
    else {
      return path.replace (Constants.WORKSPACE_RESOURCE_PREFIX, ""); //$NON-NLS-1$
    }
  }

  public static void updateProjectReferences (IProject project, String[] addProject, boolean isAddOperation)
  {
    IJavaProject javaProject = JavaCore.create (project);
    for (int i = 0; i < addProject.length; i++) {
      IProject refProj = getProject (addProject[i]);
      IClasspathEntry cpe = JavaCore.newProjectEntry (refProj.getFullPath ());
      if (isAddOperation)
        addToClasspath (javaProject, cpe);
      else
        removeFromClasspath (javaProject, cpe);
    }
  }

  private static void addToClasspath (IJavaProject jproject, IClasspathEntry cpe)
  {
    try {
      IClasspathEntry[] oldEntries = jproject.getRawClasspath ();
      for (int i = 0; i < oldEntries.length; i++) {
        if (oldEntries[i].equals (cpe)) { return; }
      }
      int nEntries = oldEntries.length;
      IClasspathEntry[] newEntries = new IClasspathEntry[nEntries + 1];
      System.arraycopy (oldEntries, 0, newEntries, 0, nEntries);
      newEntries[nEntries] = cpe;
      jproject.setRawClasspath (newEntries, null);
    }
    catch (JavaModelException e) {
      logger.logError (e.getMessage ());
    }
  }

  private static void removeFromClasspath (IJavaProject jproject, IClasspathEntry cpe)
  {
    try {
      IClasspathEntry[] oldEntries = jproject.getRawClasspath ();
      Set<IClasspathEntry> entry = new HashSet<IClasspathEntry> ();
      if (oldEntries != null) {
        for (IClasspathEntry classpathEntry : oldEntries) {
          entry.add (classpathEntry);
        }
        entry.remove (cpe);
      }
      IClasspathEntry[] newEntries = entry.toArray (new IClasspathEntry[entry.size ()]);
      jproject.setRawClasspath (newEntries, null);
    }
    catch (JavaModelException e) {
      logger.logError (e.getMessage ());
    }
  }

  public static String getConfiguredModuleBinPath (String project)
  {
    IProject proj = getProject (project);
    return getConfiguredModuleBinPath (proj);
  }

  public static String getConfiguredModuleBinPath (IProject project)
  {
    if (project == null || !(project.exists ())) { return null; }
    PreferenceStore store = ProjectUtils.getPreferenceStore (project);
    if (store == null) { return null; }

    String binPath = store.getString (Constants.MODULE_BIN_PATH);
    IFolder binFolder = null;
    if (!StringUtils.isEmpty (binPath)) {
      // New format starts with "[P]x/y/z" relative to the project. "P" means "project".
      if (binPath.startsWith (Constants.PROJECT_RELATIVE_PATH_PREFIX)) {
        binFolder = project.getFolder (store.getString (Constants.MODULE_BIN_PATH).replace (
          Constants.PROJECT_RELATIVE_PATH_PREFIX, "")); //$NON-NLS-1$
      }
      // Old format starts with "[W]/<projectname>/x/y/z". "W" means "workspace".
      else if (binPath.startsWith (Constants.WORKSPACE_RESOURCE_PREFIX)) {
        IPath moduleSrcPath = new Path (binPath.replace (Constants.WORKSPACE_RESOURCE_PREFIX, "")).makeRelativeTo (project.getFullPath ()); //$NON-NLS-1$
        binFolder = project.getFolder (moduleSrcPath);
      }
    }

    if (binFolder == null) {
      return null;
    }
    else {
      return binFolder.getFullPath ().toString ();
    }
  }

  public static File getPreferenceStoreFile (IProject project)
  {
    return new File (project.getLocation () + File.separator + Constants.TEXT_ANALYTICS_PREF_FILE);
  }

  public static String[] getAqlFilesOfModule (String projectName, String moduleName)
  {
    IFolder srcPathRes = getTextAnalyticsSrcFolder (projectName);
    List<String> aqlFileList = new ArrayList<String> ();

    try {
      if (srcPathRes != null && srcPathRes.exists ()) {
        IResource[] subFolderResource = srcPathRes.members ();
        for (IResource module : subFolderResource) {
          if (module instanceof IFolder && module.getName ().equals (moduleName)) {
            IResource[] aqlResources = ((IFolder) module).members ();
            for (IResource iResource : aqlResources) {
              if (iResource instanceof IFile
                && Constants.AQL_FILE_EXTENSION_STRING.equals (iResource.getFileExtension ())) {
                aqlFileList.add (iResource.getName ());
              }
            }
            break;
          }
        }
      }
    }
    catch (CoreException e) {
      logger.logError (e.getMessage ());
    }

    return aqlFileList.toArray (new String[0]);
  }

  /**
   * Look for a file in the given project that has the given absolute pathname.<br>
   * Note: Do not converting Pathname -> File -> IFile because that way doesn't
   * always work properly when the IFile objects are linked resources.
   * @param project The project
   * @param absPathname The absolute pathname of the AQL file to look for.
   * @return The IFile object of an AQL file whose physical location is given.
   */
  public static IFile getFileWithAbsPath (IProject project, String absPathname)
  {
    if (project == null || absPathname == null)
      return null;

    IFolder srcPathRes = getTextAnalyticsSrcFolder (project);

    try {
      if (srcPathRes != null && srcPathRes.exists ()) {
        IResource[] subFolderResource = srcPathRes.members ();
        for (IResource module : subFolderResource) {
          if (module instanceof IFolder) {
            IResource[] aqlResources = ((IFolder) module).members ();
            for (IResource iResource : aqlResources) {
              if (iResource instanceof IFile &&
                  iResource.getLocation () != null &&
                  iResource.getLocation ().toFile ().getAbsolutePath ().equals (absPathname)) {
                return (IFile)iResource;
              }
            }
          }
        }
      }
    }
    catch (CoreException e) {
      logger.logError (e.getMessage ());
    }

    return null;
  }

  public static IFolder getTextAnalyticsSrcFolder (String projectName)
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    String modSrc = ProjectUtils.getConfiguredModuleSrcPath (projectName);
    if (null == modSrc || modSrc.isEmpty ()) return null;
    IFolder srcPathRes = root.getFolder (new Path (modSrc));
    return srcPathRes;
  }

  public static IFolder getTextAnalyticsSrcFolder (IProject project)
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    String path = ProjectUtils.getConfiguredModuleSrcPath (project);
    if (null == path || path.isEmpty ()) return null;
    IFolder srcPathRes = root.getFolder (new Path (path));
    return srcPathRes;
  }

  public static IFolder getTextAnalyticsBinFolder (IProject project)
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    String path = ProjectUtils.getConfiguredModuleBinPath (project);
    if (null == path || path.isEmpty ()) return null;
    IFolder binPathRes = root.getFolder (new Path (path));
    return binPathRes;
  }

  public static IFolder getTextAnalyticsBinFolder (String projectName)
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    String path = ProjectUtils.getConfiguredModuleBinPath (projectName);
    if (null == path || path.isEmpty ()) return null;
    IFolder binPathRes = root.getFolder (new Path (path));
    return binPathRes;
  }

  public static int getTokenizerChoice (String projectName)
  {
    return getTokenizerChoice (getProject (projectName));
  }

  public static int getTokenizerChoice (IProject project)
  {
	  return Constants.TOKENIZER_CHOICE_WHITESPACE;
  }

  public static IFolder getProvenanceFolder (String projectName)
  {
    IProject iProject = getProject (projectName);
    return iProject.getFolder (Constants.DEFAULT_PROVENANCE_FOLDER);
  }

  public static IFolder getProvenanceSrcFolder (String projectName)
  {
    return getProvenanceFolder (projectName).getFolder (Constants.PROVENANCE_SRC);
  }

  public static IFolder getProvenanceBinFolder (String projectName)
  {
    return getProvenanceFolder (projectName).getFolder (Constants.PROVENANCE_BIN);
  }

  public static String getProvenanceBinFolderURI (String projectName)
  {
    IFolder provBinFolder = getProvenanceFolder (projectName).getFolder (Constants.PROVENANCE_BIN);
    return provBinFolder.getLocation ().toFile ().toURI ().toString ();
  }

  /**
   * Get provenance setting of a project.
   * 
   * @param projectName The name of project to get provenance setting.
   * @return TRUE if project has TextAnalytics nature and provenance is enabled; FALSE otherwise.
   */
  public static boolean isProvenanceEnabled (String projectName)
  {
    IProject project = getProject (projectName);
    if (project != null) {
      PreferenceStore store = getPreferenceStore (project);
      if (store != null) return store.getBoolean (Constants.GENERAL_PROVENANCE);
    }

    return false;
  }

  /**
   * Given a path string that is concatenation of paths, in workspace or local format, separated by ';', this method
   * removes duplicate resources in it.
   * 
   * @param paths
   * @return
   */
  public static String removeDuplicateResources (String paths)
  {
    if (StringUtils.isEmpty (paths)) return "";

    String newPaths = "";
    Set<String> resources = new HashSet<String> (); // use Set to avoid duplicates

    String pathArray[] = paths.split (Constants.DATAPATH_SEPARATOR);
    for (String p : pathArray) {

      String absPath = deduceAbsolutePath (p);
      if (!resources.contains (absPath)) {
        // I want to keep original path format even though
        // using all abs paths is probably fine.
        if (newPaths.length () == 0)
          newPaths = p;
        else
          newPaths += Constants.DATAPATH_SEPARATOR + p;

        resources.add (absPath);
      }
    }

    return newPaths;
  }

  public static IFolder getModuleFolder (String projectName, String moduleName)
  {
    IFolder srcFolder = getTextAnalyticsSrcFolder (projectName);
    if (srcFolder != null) return srcFolder.getFolder (moduleName);

    return null;
  }

  public static IFile getAqlFile (String projectName, String moduleName, String aqlFileName)
  {
    IFolder moduleFolder = getModuleFolder (projectName, moduleName);
    if (moduleFolder != null) return moduleFolder.getFile (aqlFileName);
    return null;
  }

  /**
   * Get the non-epmty modules of a project.
   * 
   * @param project
   * @return An array of non-empty modules
   */
  public static String[] getModules (String projectName)
  {
    return getModules (getProject (projectName));
  }

  /**
   * Get the non-empty modules of a project.
   * 
   * @param project
   * @return An array of non-empty modules
   */
  public static String[] getModules (IProject project)
  {
    if (!isModularProject (project)) { return new String[] { Constants.GENERIC_MODULE }; }

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    String path = ProjectUtils.getConfiguredModuleSrcPath (project.getName ());
    if (null == path || path.isEmpty ()) return null;
    IFolder srcPathRes = root.getFolder (new Path (path));
    List<String> moduleList = new ArrayList<String> ();
    try {
      if (srcPathRes != null && srcPathRes.exists ()) {
        IResource[] subFolderResource = srcPathRes.members ();
        for (IResource module : subFolderResource) {
          if (module instanceof IFolder) {
            IResource[] aqlResources = ((IFolder) module).members ();
            for (IResource iResource : aqlResources) {
              if (iResource instanceof IFile
                && Constants.AQL_FILE_EXTENSION_STRING.equals (iResource.getFileExtension ())) {
                moduleList.add (module.getName ());
                break;
              }
            }

          }
        }
      }
    }
    catch (CoreException e) {
      logger.logError (e.getMessage ());
    }
    if (!moduleList.isEmpty ()) return moduleList.toArray (new String[0]);
    return null;
  }

  /**
   * Get all modules of a project, including empty ones.<br>
   * This is infact getting all sub-folders of the src folder.
   * 
   * @param project
   * @return An array of names of modules, both empty and non-empty ones
   */
  public static String[] getAllModules (IProject project)
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    List<String> moduleList = new ArrayList<String> ();
    String moduleSrcPath = ProjectUtils.getConfiguredModuleSrcPath (project.getName ());
    if (moduleSrcPath != null) {
      IFolder srcPathRes = root.getFolder (new Path (moduleSrcPath));
      try {
        if (srcPathRes != null && srcPathRes.exists ()) {
          IResource[] subFolderResource = srcPathRes.members ();
          for (IResource module : subFolderResource) {
            if (module instanceof IFolder) {
              moduleList.add (module.getName ());
            }
          }
        }
      }
      catch (CoreException e) {
        logger.logError (e.getMessage ());
      }
    }

    if (!moduleList.isEmpty ()) return moduleList.toArray (new String[0]);

    return new String[] {};
  }

  /**
   * Get all modules of a project, including empty ones.<br>
   * This is in fact getting all sub-folders of the src folder.
   * 
   * @param projectName Name of a project
   * @return An array of names of modules, both empty and non-empty ones.
   */
  public static String[] getAllModules (String projectName)
  {
    if (projectName != null)
      return getAllModules (getProject (projectName));
    else
      return new String[] {};
  }

  public static IProject getProjectForEditor (String aqlFilePath)
  {
    IPath path = new Path (aqlFilePath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation (path);
    if (file == null)
      return null;
    else {
      return file.getProject ();
    }
  }

  public static void syncCreateFolder (final IFolder folder)
  {
    if (folder != null && !folder.exists ()) {
      Display.getDefault ().syncExec (new Runnable () {
        @Override
        public void run ()
        {
          try {
            folder.create (true, true, null);
          }
          catch (CoreException e) {
            logger.logAndShowError (e.getLocalizedMessage (), e);
          }
        }
      });
    }
  }

  public static IFolder getModule4AqlFile (IFile aqlFile)
  {
    if (aqlFile == null) return null;

    IProject proj = aqlFile.getProject ();
    IFolder srcFolder = getTextAnalyticsSrcFolder (proj.getName ());

    // Module is a folder under src folder, so up until we meet a folder whose parent is the src folder.
    IResource resource = aqlFile;
    while (resource.getParent () != null) {

      // parent is project -> module not exists
      if (resource.getParent () instanceof IProject)
        return null;

      // 'resource' is a folder whose parent is the src folder -> this is the module
      else if (resource.getParent ().equals (srcFolder) && resource instanceof IFolder)
        return (IFolder) resource;

      // continue going up
      else
        resource = resource.getParent ();
    }

    return null;
  }

  /**
   * Returns an array of projects that have text analytics nature
   * 
   * @return
   */
  public static IProject[] getTextAnalyticsProjectsList ()
  {
    IProject[] projects = ResourcesPlugin.getWorkspace ().getRoot ().getProjects ();
    ArrayList<IProject> filterProjects = new ArrayList<IProject> ();
    for (int i = 0; i < projects.length; i++) {
      try {
        if (projects[i].isOpen () && projects[i].hasNature (Constants.PLUGIN_NATURE_ID)) {
          filterProjects.add (projects[i]);
        }
      }
      catch (CoreException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          "Unable to retrieve project nature. Excluding this project from TA projects list.", e); //$NON-NLS-1$
      }
    }
    return filterProjects.toArray (new IProject[0]);
  }

  /**
   * Splits qualified element name into an array containing 2 strings - module name and element name
   * <p>
   * e.g.<br/>
   * mymodule.myview -> [mymodule, myview]<br/>
   * my.module.myview -> [my.module, myview]<br/>
   * myview -> [myview]
   * </p>
   * 
   * @param elemName qualified element name
   * @return
   */
  public static String[] splitQualifiedAQLElementName (String elemName)
  {
    ArrayList<String> nameParts = new ArrayList<String> ();
    if (!StringUtils.isEmpty (elemName)) {
      int lastIndex = elemName.lastIndexOf (Constants.MODULE_ELEMENT_SEPARATOR);
      int firstIndex = elemName.indexOf (Constants.MODULE_ELEMENT_SEPARATOR);
      // '.' cannot be the first character or last character and should occur
      // at least once anywhere in between
      if (lastIndex > 0 && firstIndex > 0 && lastIndex < elemName.length () - 1) {
        nameParts.add (elemName.substring (0, lastIndex));
        nameParts.add (elemName.substring (lastIndex + 1));
      }
      else {
        nameParts.add (elemName);
      }
    }
    return nameParts.toArray (new String[0]);
  }

  /**
   * Get default directory where provenance rewrite is kept.<br>
   * Note: This directory may not exist yet.
   */
  public static IFolder getDefaultProvenanceDir (IProject project)
  {
    if (project != null)
      return project.getFolder (Constants.DEFAULT_PROVENANCE_FOLDER);
    else
      return null;
  }

  /**
   * Get default directory where compiled provenance rewrite modules are kept.<br>
   * Note: This directory may not exist yet, or exists but empty.
   */
  public static IFolder getDefaultProvenanceBinDir (IProject project)
  {
    IFolder provDir = getDefaultProvenanceDir (project);
    return provDir.getFolder (Constants.PROVENANCE_BIN);
  }

  /**
   * Get default directory where provenance rewrite module sources are kept.<br>
   * Note: This directory may not exist yet.
   */
  public static IFolder getDefaultProvenanceSrcDir (IProject project)
  {
    IFolder provDir = getDefaultProvenanceDir (project);
    return provDir.getFolder (Constants.PROVENANCE_SRC);
  }

  /**
   * This method is used to validate the given AQL module or script name.
   * 
   * @param newName
   * @return
   */
  public static boolean isValidName (String newName)
  {
    if (StringUtils.isEmpty (newName)) { return false; }

    Pattern p = Pattern.compile ("[a-zA-Z_]([a-zA-Z0-9_])*"); //$NON-NLS-1$
    Matcher m = p.matcher (newName);
    if (!m.matches ()) { return false; }

    return true;
  }

  /**
   * This method is used to validate the given string is AQL keyword / reserved word / built-in Function name or not
   * 
   * @param name
   * @return true or false based on the name being passed to this method.
   */
  public static boolean isAQLKeyword (String name)
  {
    if (StringUtils.isEmpty (name)) { return false; }

    List<String> keyWordList = Arrays.asList (KEYWORDS);
    if (keyWordList.contains (name)) { return true; }

    List<String> reservedWordList = Arrays.asList (TYPES);
    if (reservedWordList.contains (name)) { return true; }

    List<String> reservedNameList = Arrays.asList (RESERVED_NAMES);
    if (reservedNameList.contains (name)) { return true; }

    List<String> builtInFunsLList = Arrays.asList (BUILT_IN_FUNCS);
    if (builtInFunsLList.contains (name)) { return true; }

    return false;
  }

  /**
   * @return map of external view name vs their tuple schema.
   * @throws Exception
   */
  public static Map<Pair<String, String>, TupleSchema> getExternalViewsSchema (OperatorGraph og) throws Exception
  {
    Map<Pair<String, String>, TupleSchema> retVal = null;
    retVal = new HashMap<Pair<String, String>, TupleSchema> ();
    String[] externalViewNames = og.getExternalViewNames ();
    for (String evn : externalViewNames) {
      Pair<String, String> evnPair = new Pair<String, String> (evn, og.getExternalViewExternalName (evn));
      retVal.put (evnPair, og.getExternalViewSchema (evn));
    }

    return retVal;
  }

  /**
   * This method creates a marker with the given message and severity and adds it against the given project's directory.
   * 
   * @param project
   * @param errorMessage
   * @param severity
   */
  public static void addMarker (IProject project, String markerType, int severity, int priority, String errorMessage)
  {
    try {
      // putting a check to see if the same marker exist,
      // if yes then return otherwise create the marker
      boolean exist = false;
      IMarker[] problems = null;
      int depth = IResource.DEPTH_INFINITE;
      try {
        problems = project.findMarkers (markerType, true, depth);
      }
      catch (CoreException e) {
        logger.logError (e.getMessage ());
        problems = new IMarker[0];
      }
      for (int i = 0; i < problems.length; i++) {

        if (problems[i].getAttribute (IMarker.MESSAGE).equals (errorMessage)) {
          exist = true;
        }
      }
      if (!exist) {
        IMarker marker = project.createMarker (markerType);
        marker.setAttribute (IMarker.MESSAGE, errorMessage);
        marker.setAttribute (IMarker.PRIORITY, priority);
        marker.setAttribute (IMarker.SEVERITY, severity);
      }
    }
    catch (CoreException e) {
      logger.logError (e.getMessage ());
    }
  }

  /**
   * Reload the extraction plan of a project if it is being opened.
   * 
   * @param projectName The project whose EP is refreshed if it is being opened.
   */
  public static void refreshExtractionPlan (final String projectName)
  {
    refreshExtractionPlan (projectName, projectName);
  }

  /**
   * Reload the extraction plan of a project if it is being opened.
   * 
   * @param projectName The project whose EP is refreshed if it is being opened.
   * @param newProjectName The new project name, in case project is renamed. Pass null or same <b>projectName</b> if
   *          project name does not change.
   */
  public static void refreshExtractionPlan (final String projectName, final String newProjectName)
  {

    Display.getDefault ().asyncExec (new Runnable () {
      @Override
      public void run ()
      {
        IWorkbenchWindow wbWindow = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ();
        IHandlerService handlerService = (IHandlerService) wbWindow.getService (IHandlerService.class);
        ICommandService commandService = (ICommandService) wbWindow.getService (ICommandService.class);

        try {

          ArrayList<Parameterization> parameters = new ArrayList<Parameterization> ();
          Command openCommand = commandService.getCommand (Constants.REFRESH_EP_COMMAND_ID);

          IParameter projNameParam = openCommand.getParameter (Constants.REFRESH_EP_PROJECT_PARAM_ID);
          Parameterization projNameParmeterization = new Parameterization (projNameParam, projectName);
          parameters.add (projNameParmeterization);

          IParameter newProjNameParam = openCommand.getParameter (Constants.REFRESH_EP_NEW_PROJECT_PARAM_ID);
          Parameterization newProjNameParmeterization = new Parameterization (newProjNameParam, newProjectName);
          parameters.add (newProjNameParmeterization);

          ParameterizedCommand parmCommand = new ParameterizedCommand (openCommand,
            parameters.toArray (new Parameterization[parameters.size ()]));
          handlerService.executeCommand (parmCommand, null);

        }
        catch (Exception e) {
          String errorTitle = Messages.getString ("Workflow.ERROR_REFRESH_EXTRACTION_PLAN"); //$NON-NLS-1$
          LogUtil.getLogForPlugin (Constants.WORKFLOW_PLUGIN_ID).logError (errorTitle, e);
        }
      }
    });
  }

  /**
   * Fetch a list of pre-compiled modules that are imported by AQL source of a given project and its dependents. The
   * assumption made here is that all the modules are built without any errors and all its tam files are available.
   * 
   * @param project for which the set of pre-compiled imported modules have to be found out
   * @return Set of modules in pre-compiled module locations which are referenced by src modules in given project and
   *         its referenced projects.
   * @throws TextAnalyticsException could occur during reading metadata of modules
   * @throws CoreException this can occur during fetching referenced projects of a given project.
   */

  public static Set<String> getTamsImportedBySrc (IProject project, String modulePath) throws TextAnalyticsException, CoreException
  {
    // Set to hold src modules of all required projects
    Set<String> srcModules = new HashSet<String> ();

    // Set to hold imported modules in all the src modules of required projects
    Set<String> referredModules = new HashSet<String> ();

    // A list of projects containing current Project plus its referenced projects.
    ArrayList<IProject> requiredProjects = new ArrayList<IProject> ();

    // Add current project and its references to the list.
    requiredProjects.add (project);
    List<IProject> refProjects = Arrays.asList (project.getReferencedProjects ());
    requiredProjects.addAll (refProjects);

    for (IProject proj : requiredProjects) {

      // for each project get its src modules and add it to the modules list
      String[] modulesPerProj = ProjectUtils.getModules (proj);
      srcModules.addAll (Arrays.asList (modulesPerProj));

      // Get the metadata for all modules in the project
      ModuleMetadata[] md = ModuleMetadataFactory.readMetaData (modulesPerProj, modulePath);

      // From each metadata get all the dependent modules and add it to the imported modules list
      for (ModuleMetadata moduleMetadata : md) {
        referredModules.addAll (moduleMetadata.getDependentModules ());
      }
    }

    // Remove all the src modules from the list
    // This makes sure that all the remaining modules are purely pre-compiled modules.
    referredModules.removeAll (srcModules);

    return referredModules;
  }

  /**
   * Prepares a new exception with message that contains all the causes of given exception recursively added to it.
   * There can be scenarios where the cumulative error message is shown in single line, in this case semicolon is added
   * as delimiter other wise new line is added after each cause.
   * 
   * @param e the exception that is caught.
   * @param singleLine true if the error message is to be displayed in a single line.
   * @return a new exception that has a detailed message of the causes of orignal exception.
   */
  public static Throwable prepareDetailedMessage (Throwable e, boolean singleLine)
  {
    Throwable ret = e;
    StringBuilder sb = new StringBuilder ();

    // initially append the message of top level exception
    sb.append (e.getMessage ());
    // If the display is not in single line add new line character or else add semicolon.
    if (false == singleLine) {
      sb.append ("\n");
    }
    else {
      sb.append (Constants.DATAPATH_SEPARATOR);
    }

    // recursively append the underlying causes if they exist
    Throwable cause = e;
    while ((cause = cause.getCause ()) != null) {
      String message = cause.getMessage ();
      if (message != null) {
        sb.append (message);
        if (false == singleLine) {
          sb.append ("\n");
        }
        else {
          sb.append (Constants.DATAPATH_SEPARATOR);
        }
      }
    }

    if (sb.length () != 0) {
      ret = new Exception (sb.toString ());
    }

    return ret;
  }

  /**
   * Clean the given projects.
   * @param projects
   */
  public static void cleanProjects (Set<IProject> projects)
  {
    // Get open projects. We don't clean closed projects.
    final List<IProject> openProjects = new ArrayList<IProject> ();
    for (IProject p : projects) {
      if (p != null && p.isOpen ())
        openProjects.add (p);
    }

    if (openProjects.isEmpty ())  // Nothing to clean
      return;

    // Clean the open projects.
    WorkspaceJob touchJob = new WorkspaceJob (Messages.getString ("ProjectUtils.CLEAN_PROJECTS")) {
      @Override
      public IStatus runInWorkspace (IProgressMonitor progressMonitor) throws CoreException
      {
        try {
          if (progressMonitor != null) progressMonitor.beginTask ("", 1); //$NON-NLS-1$

          for (IProject p : openProjects) {
            p.build (IncrementalProjectBuilder.CLEAN_BUILD, null);
          }
        }
        finally {
          if (progressMonitor != null) progressMonitor.done ();
        }
        return Status.OK_STATUS;
      }
    };

    touchJob.schedule ();
  }

  /**
   * Get the list of all launch configuration names.
   * @return
   */
  public static List<String> getAllLaunchConfigNames ()
  {
    List<String> lcList = new ArrayList<String> ();

    ILaunchConfiguration[] configs;
    try {
      configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();

      for(int i = 0; i < configs.length; i++) {
        lcList.add(configs[i].getName ());
      }
    }
    catch (CoreException e) {
      return null;
    }

    return lcList;
  }

  public static boolean isTableRequired (TableMetadata tableMD)
  {
    return isElementRequired (tableMD.isRequired (), tableMD.isAllowEmpty ());
  }

  public static boolean isDictRequired (DictionaryMetadata dictMD)
  {
    return isElementRequired (dictMD.isRequired (), dictMD.isAllowEmpty ());
  }

  /**
   * Tell whether an element is required or not based on the value of 2 Boolean objects.
   * The 1st Boolean tells if the element is required and has higher priority than the
   * 2nd one, allowEmpty, which tells if the element is NOT required.<br>
   * If both of them are null, return TRUE by default.
   * @param required
   * @param allowEmpty
   * @return If the 'required' Boolean object is not null, return its boolean value.<br>
   * If the 'required' Boolean is null and the 'allowEmpty' Boolean is not null, return
   * the negate boolean value of the 'allowEmpty' Boolean object.<br>
   * If the 'allowEmpty' Boolean is also null, return TRUE by default. One of the flags
   * must be declared, it won't reach here anyway.
   */
  public static boolean isElementRequired (Boolean required, Boolean allowEmpty)
  {
    if (required != null)
      return required;
    else {
      if (allowEmpty != null)
        return !allowEmpty;  // Since the flag is "NOT required", return the negate of it.
      else
        return true;    // One of the flags, allow_empty or required, must be declared, so it should not reach this line.
    }
  }
}
