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
package com.ibm.biginsights.textanalytics.nature.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.ibm.avatar.algebra.datamodel.FieldType;
import com.ibm.avatar.algebra.datamodel.TupleSchema;
import com.ibm.avatar.algebra.oldscan.DBDumpFileScan;
import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.ExternalTypeInfo;
import com.ibm.avatar.api.ExternalTypeInfoFactory;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.api.tam.ModuleMetadataFactory;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.nature.AQLBuilder;
import com.ibm.biginsights.textanalytics.nature.AQLNature;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.run.SystemTRunConfig;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * 
 * 
 */
public class ProjectPreferencesUtil {

	@SuppressWarnings("unused")


  private static final String FILE_PATH_SEPARATOR = "/"; //$NON-NLS-1$

  public static String getPath(String path) {
    return ProjectUtils.getPath (path);
  }

  public static String getAbsolutePath(String path) {
    if (StringUtils.isEmpty(path)) {
      return ""; //$NON-NLS-1$
    } else {
      String[] entries = path.split(Constants.DATAPATH_SEPARATOR);
      String absolutePath = deduceAbsolutePath(entries[0]);
      for (int i = 1; i < entries.length; ++i) {
        absolutePath = absolutePath + Constants.DATAPATH_SEPARATOR
        + deduceAbsolutePath(entries[i]);
      }
      return absolutePath;
    }

  }

  /**
   * Return whether the File object of specified path exists and is a file.
   * @param path The given path -- workspace path begins with [W], non-workspace path is absolute.
   * @return TRUE if the file exists, FALSE if the path is null, or empty, or the file doesn't exist.
   */
  public static boolean isExistingFile (String path) {
    String absPath = ProjectUtils.deduceAbsolutePath (path);

    if (StringUtils.isEmpty (absPath) == false) {
      File f = new File (absPath);
      return (f.exists () && f.isFile ());
    }

    return false;
  }

  public static String deduceAbsolutePath(String path) {
    return ProjectUtils.deduceAbsolutePath (path);
  }

  public static String getTamPathStr (String projectName) throws CoreException
  {
    return getTamPathStr (ProjectUtils.getProject (projectName));
  }

  // This logic to get tamPathStr is copied from SystemtRunJob.
  // If something is modified here, please also compare with the code in
  // SystemtRunJob and make similar changes if necessary.
  public static String getTamPathStr (IProject iProject) throws CoreException
  {
    if (iProject == null) return ""; //$NON-NLS-1$

    SystemTProperties projectProps = getSystemTProperties (iProject.getName ());

    String tamPathStr = ""; //$NON-NLS-1$

    if (ProjectUtils.isModularProject (iProject)) {
      // getAbsolutePath() will no longer work on SystemTProperties.getModuleBinPath() and
      // SystemTProperties.getModuleSrcPath() - their values will be relative to project.
      // Using another way to get absolute path for module bin directory.
      IResource tamDir = iProject.findMember (projectProps.getModuleBinPath ().replace (
        Constants.PROJECT_RELATIVE_PATH_PREFIX, "")); //$NON-NLS-1$
      if (tamDir != null) {
        tamPathStr = tamDir.getLocation ().toString () + Constants.DATAPATH_SEPARATOR;
      }
      tamPathStr = new File (tamPathStr).toURI ().toString ();

      String tamPath = projectProps.getTamPath ();
      if (!StringUtils.isEmpty (tamPath)) {
        String tamPaths[] = tamPath.split (Constants.DATAPATH_SEPARATOR);
        for (String path : tamPaths) {
        	if (ProjectPreferencesUtil.getAbsolutePath (path) != null)
        		tamPathStr += new File (ProjectPreferencesUtil.getAbsolutePath (path)).toURI ().toString () + Constants.DATAPATH_SEPARATOR;
        }
      }

      IProject refProject[] = iProject.getReferencedProjects ();

      IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
      String depProjects[] = ProjectUtils.getProjectDependencyPaths (root, refProject);
      for (String depProject : depProjects) {
        if (depProject != null)
          tamPathStr += new File (depProject).toURI ().toString () + Constants.DATAPATH_SEPARATOR;
      }
    }
    else {
      tamPathStr = new File (ProjectPreferencesUtil.getAbsolutePath (projectProps.getAogPath ())).toURI ().toString ()
        + Constants.DATAPATH_SEPARATOR;
    }

    return tamPathStr;
  }

  /**
   * Get the tam path of a single project, not including those of dependent projects.
   * @param iProject The project
   * @return The tam paths concatenated together with ";"
   * @throws CoreException
   */
  public static String getProjectTamPathStr (String projectName) throws CoreException
  {
    return getProjectTamPathStr (ProjectUtils.getProject (projectName));
  }

  /**
   * Get the tam path of a single project, not including those of dependent projects.
   * @param iProject The project
   * @return The tam paths concatenated together with ";"
   * @throws CoreException
   */
  public static String getProjectTamPathStr (IProject iProject) throws CoreException
  {
    if (iProject == null)
      return ""; //$NON-NLS-1$

    SystemTProperties projectProps = getSystemTProperties(iProject.getName ());
    String tamPathStr = ""; //$NON-NLS-1$

    if(ProjectUtils.isModularProject (iProject)) {
      String tamPath = projectProps.getTamPath();
      if ( ! StringUtils.isEmpty (tamPath) ) {
        String tamPaths[] = tamPath.split(Constants.DATAPATH_SEPARATOR);
        for (String path : tamPaths) {
          tamPathStr += getPathForTamPathStr (path);
        }
      }
    }
    else {
      String aogPath = projectProps.getAogPath();
      if ( ! StringUtils.isEmpty (aogPath) )
        tamPathStr = getPathForTamPathStr (aogPath);
    }

    return tamPathStr;
  }

  /**
   * Get the set of projects whose tam path points to this resource.
   * @param resource
   * @return
   * @throws CoreException
   */
  public static Set<IProject> getReferencingProjects (IResource resource) throws CoreException
  {
    Set<IProject> refProjects = new HashSet<IProject> ();

    // tam path only can point to a folder or a zip/jar file 
    if (resource instanceof IFolder ||
        resource instanceof IProject ||
        resource.getName ().endsWith (".zip") ||    //$NON-NLS-1$
        resource.getName ().endsWith (".jar")) {    //$NON-NLS-1$

      // Loop thru all projects
      IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for (IProject p : allProjects) {
        if (!p.isOpen ())
          continue;

        if (isInProjectTamPath(resource, p)) {
          refProjects.add (p);
        }
      }
    }

    return refProjects;
  }

  public static boolean isInProjectTamPath (IResource res, IProject project) throws CoreException
  {
    if (project == null ||
        res == null ||
        res.getLocation () == null)
      return false;

    String resPathStr = res.getLocation ().toFile ().toURI().toString();

    String tamPath = getProjectTamPathStr(project);

    if ( ! StringUtils.isEmpty (tamPath) ) {
      String tamPaths[] = tamPath.split(Constants.DATAPATH_SEPARATOR);
      for (String path : tamPaths) {
        if (path.equals (resPathStr))
          return true;
      }
    }

    return false;
  }

  private static String getPathForTamPathStr (String path)
  {
    String absPath = getAbsolutePath(path);

    if (absPath != null) {
    	File file = FileUtils.createValidatedFile(absPath);
    	if (absPath != null && file.exists ())
    		return file.toURI().toString() + Constants.DATAPATH_SEPARATOR;
    }

    return "";
  }

  public static boolean isWorkspaceResource(String path) {
    return ProjectUtils.isWorkspaceResource (path);
  }

  public static IProject getSelectedProject() {
    return ProjectUtils.getSelectedProject();
  }

  public static IWorkbenchWindow getActiveWorkbenchWindow() {
    return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
  }

  public static IWorkbenchPage getActivePage() {
    IWorkbenchWindow w = getActiveWorkbenchWindow();
    if (w != null) {
      return w.getActivePage();
    }
    return null;
  }

  public static IProject getProject(String projectName) {
    return ProjectUtils.getProject(projectName);
  }

  public static SystemTProperties getSystemTProperties(String projectName) {
    return getSystemTProperties(getProject(projectName));
  }

  public static SystemTProperties getSystemTProperties(IProject project) {
    PreferenceStore prefStore = ProjectUtils.getPreferenceStore(project);
    return createSystemTProperties(prefStore, project.getName());
  }

  public static String getMainAqlPath (IProject project)
  {
    if (project != null) {
      PreferenceStore prefStore = ProjectUtils.getPreferenceStore(project);
      if (prefStore != null)
        return prefStore.getString (Constants.GENERAL_MAINAQLFILE);
    }

    return null;
  }

  public static String getMainAqlPath (String projectName)
  {
    IProject project = ProjectUtils.getProject (projectName);
    return getMainAqlPath (project);
  }

  public static SystemTProperties createSystemTProperties(
      PreferenceStore prefStore, String projectName) {
    if (prefStore == null) {
      return new SystemTProperties();
    }
    boolean enableProvenance = prefStore
    .getBoolean(Constants.GENERAL_PROVENANCE);
    String mainAQLFile = prefStore.getString(Constants.GENERAL_MAINAQLFILE);
    String searchPath = prefStore.getString(Constants.SEARCHPATH_DATAPATH);
    boolean isModularProject = prefStore
    .getBoolean(Constants.MODULAR_AQL_PROJECT);

    String moduleSrcPath = prefStore.getString(Constants.MODULE_SRC_PATH);
    String moduleBinPath = prefStore.getString(Constants.MODULE_BIN_PATH);
    String tamPath = prefStore.getString(Constants.TAM_PATH);
    String dependentProject = prefStore
    .getString(Constants.DEPENDENT_PROJECT);

    int tokenizerChoice = Constants.TOKENIZER_CHOICE_WHITESPACE;

    boolean enablePagination = prefStore
    .getBoolean(Constants.PAGINATION_ENABLED);
    if (!prefStore.contains(Constants.PAGINATION_ENABLED))
      enablePagination = Constants.PAGINATON_ENABLED_DEFAULT_VALUE; // Ensuring
    // default
    // values
    // are
    // applied
    // to
    // existing
    // pre
    // 1.5
    // projects
    int numFilesPerPage = prefStore
    .getInt(Constants.PAGINATION_FILES_PER_PAGE);
    if (!prefStore.contains(Constants.PAGINATION_FILES_PER_PAGE))
      numFilesPerPage = Constants.PAGINATION_FILES_PER_PAGE_DEFAULT_VALUE;

    return new SystemTProperties(projectName, isModularProject,
        moduleSrcPath, moduleBinPath, enableProvenance, mainAQLFile,
        searchPath, tamPath, dependentProject, tokenizerChoice,
        null, null, enablePagination, numFilesPerPage);
  }

  @SuppressWarnings("unchecked")
  public static SystemTRunConfig createRunConfiguration(IProject project,
      ILaunchConfiguration config) {
    try {
      String lang = config.getAttribute(IRunConfigConstants.LANG, "en");//$NON-NLS-1$
      String inputCollection = config.getAttribute(IRunConfigConstants.INPUT_COLLECTION, ""); //$NON-NLS-1$

      String csvDelimiterStr = config.getAttribute (IRunConfigConstants.DELIMITER, "");

      String projectName = config.getAttribute(IRunConfigConstants.PROJECT_NAME, ""); //$NON-NLS-1$
      SystemTProperties systemTProperties = getSystemTProperties(projectName);
      
      // Default tokenizer configuration
      int tokenizerChoice = Constants.TOKENIZER_CHOICE_WHITESPACE;

      String lwConfig = ""; //$NON-NLS-1$
      String lwDataPath = ""; //$NON-NLS-1$

      String selectedModules = config.getAttribute(
          IRunConfigConstants.SELECTED_MODULES, "");//$NON-NLS-1$

      Map<String, String> extDictFileMapping = config.getAttribute(
          IRunConfigConstants.EXTERNAL_DICT_MAP,
          new HashMap<String, String>());
      Map<String, String> extTableFileMapping = config.getAttribute(
          IRunConfigConstants.EXTERNAL_TABLES_MAP,
          new HashMap<String, String>());

      SystemTRunConfig runConfig = new SystemTRunConfig(lang,
          inputCollection, csvDelimiterStr, tokenizerChoice, lwConfig, lwDataPath,
          selectedModules, extDictFileMapping, extTableFileMapping,
          systemTProperties);

      return runConfig;
    } catch (CoreException e) {
      Activator
      .getDefault()
      .getLog()
      .log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
          .getMessage()));
    }

    return null;
  }

  public static TokenizerConfig getTokenizerConfig (String projectName) throws TextAnalyticsException
  {
    int tokenizerChoice = ProjectUtils.getTokenizerChoice (projectName);
    if (tokenizerChoice == Constants.TOKENIZER_CHOICE_WHITESPACE)
      return new TokenizerConfig.Standard();
    else
      return null;  // We don't know about the custom tokenizer config, so just return null
  }


  public static Image getImage(String filepath) {
    Bundle bundle = com.ibm.biginsights.textanalytics.nature.Activator
    .getDefault().getBundle();
    URL url = FileLocator.find(bundle, new Path(filepath), null);
    ImageDescriptor descriptor = ImageDescriptor.createFromURL(url);
    return descriptor.createImage();
  }

  // //////////////////////////////////////////////////////////////////////////////////////
  /**
   * BEGIN: Methods related to creating default directories
   */

  private static final String DIR_AOG = ".aog"; //$NON-NLS-1$

  // private static final String DIR_RESULT = "result";

  public static String getDefaultDataPath(IProject project) {
    String projectBaseLocation = project.getFullPath().toPortableString();

    return Constants.WORKSPACE_RESOURCE_PREFIX + projectBaseLocation;
  }

  public static String getDefaultAOGPath(String projectName) {
    return getDefaultAOGPath(getProject(projectName));
  }

  public static String getDefaultAOGPath(IProject project) {
    try {
      String projectBaseLocation = project.getFullPath()
      .toPortableString();
      StringBuilder strBuilder = new StringBuilder(100);
      strBuilder.append(Constants.WORKSPACE_RESOURCE_PREFIX)
      .append(projectBaseLocation).append(FILE_PATH_SEPARATOR)
      .append(DIR_AOG);

      return strBuilder.toString();
    } catch (Exception e) {
      return ""; //$NON-NLS-1$
    }
  }

  public static String getDefaultResultDir(String projectName) {
    return getDefaultResultDir(getProject(projectName));
  }

  public static String getDefaultResultDir(IProject project) {
    return ProjectUtils.getRootResultFolder(project).getFullPath()
    .toPortableString();
  }

  public static void createDefaultDirs(IProject project) {
    String projectPath = ProjectPreferencesUtil
    .getAbsolutePath(Constants.WORKSPACE_RESOURCE_PREFIX
        + project.getFullPath().toString());
    File projectDir = new File(projectPath);

    if (AQLNature.MODULAR_AQL_PROJECT_DEFAULT_VALUE) {
      File modulePath = FileUtils.createValidatedFile(projectDir,
          Constants.DEFAULT_MODULE_PATH);
      File src = FileUtils.createValidatedFile(modulePath, Constants.DEFAULT_MODULE_SRC);
      File bin = FileUtils.createValidatedFile(modulePath, Constants.DEFAULT_MODULE_BIN);
      createIfNotExists(modulePath, src, bin);
    }

    /*
     * IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); try {
     * IFolder folderResource = root.getFolder(new Path(project.getName() +
     * Constants.MODULE_SRC_PATH )); if(folderResource == null)
     * folderResource.create(true, true, null); IJavaProject javaProject=
     * JavaCore.create(project); IPackageFragmentRoot srcFolder=
     * javaProject.getPackageFragmentRoot(folderResource);
     * 
     * IClasspathEntry cpe= JavaCore.newSourceEntry(srcFolder.getPath());
     * boolean alreadyAdded = false;
     * 
     * IClasspathEntry[] oldEntries= javaProject.getRawClasspath(); for (int
     * i= 0; i < oldEntries.length; i++) { if (oldEntries[i].equals(cpe)) {
     * alreadyAdded = true; } } if(!alreadyAdded){ int nEntries=
     * oldEntries.length; IClasspathEntry[] newEntries= new
     * IClasspathEntry[nEntries + 1]; System.arraycopy(oldEntries, 0,
     * newEntries, 0, nEntries); newEntries[nEntries]= cpe;
     * javaProject.setRawClasspath(newEntries, null); } } catch
     * (JavaModelException e) {
     * 
     * e.printStackTrace(); } catch (CoreException e) {
     * 
     * e.printStackTrace(); }
     */

    // IClasspathEntry javaProject = JavaCore.newSourceEntry(new
    // Path(resource.getName() + MODULE_SRC_PATH));

    // }

  }

  public static void createDefaultAOGDir(IProject project) {
    String projectPath = ProjectPreferencesUtil
    .getAbsolutePath(Constants.WORKSPACE_RESOURCE_PREFIX
        + project.getFullPath().toString());
    File projectDir = FileUtils.createValidatedFile(projectPath);

    File aogDir = FileUtils.createValidatedFile(projectDir, DIR_AOG);

    createIfNotExists(aogDir);
  }

  public static void createDefaultProvenanceDir(IProject project) {
    String projectPath = ProjectPreferencesUtil
    .getAbsolutePath(Constants.WORKSPACE_RESOURCE_PREFIX
        + project.getFullPath().toString());
    File projectDir = FileUtils.createValidatedFile(projectPath);
    File rewrittenModules = FileUtils.createValidatedFile(projectDir,
        Constants.DEFAULT_PROVENANCE_FOLDER);
    createIfNotExists(rewrittenModules);

    File src = FileUtils.createValidatedFile(rewrittenModules, Constants.PROVENANCE_SRC);
    createIfNotExists(src);
    File bin = FileUtils.createValidatedFile(rewrittenModules, Constants.PROVENANCE_BIN);
    createIfNotExists(bin);
  }

  private static void createIfNotExists(File... dirs) {
    for (File dir : dirs) {
      if (!dir.exists()) {
        dir.mkdirs();
      }
    }
  }

  public static String createDefaultModuleSrcPath(IProject project) {
    try {
      StringBuilder strBuilder = new StringBuilder(150);
      strBuilder.append(Constants.PROJECT_RELATIVE_PATH_PREFIX)
      .append(Constants.DEFAULT_MODULE_PATH)
      .append(FILE_PATH_SEPARATOR)
      .append(Constants.DEFAULT_MODULE_SRC);

      return strBuilder.toString();
    } catch (Exception e) {
      return ""; //$NON-NLS-1$
    }
  }

  public static String createDefaultModuleBinPath(IProject project) {
    try {
      StringBuilder strBuilder = new StringBuilder(150);
      strBuilder.append(Constants.PROJECT_RELATIVE_PATH_PREFIX)
      .append(Constants.DEFAULT_MODULE_PATH)
      .append(FILE_PATH_SEPARATOR)
      .append(Constants.DEFAULT_MODULE_BIN);

      return strBuilder.toString();
    } catch (Exception e) {
      return ""; //$NON-NLS-1$
    }
  }

  public static boolean isCompiledTAMExist(String tamParentDir, String tamFile) {
    final String absTAMFile = getAbsolutePath(tamParentDir)
    + File.separator + tamFile + Constants.TAM_FILE_EXTENSION;
    if (FileUtils.createValidatedFile(absTAMFile).exists())
      return true;
    return false;
  }

  /*
   * END: Methods related to creating default directories
   */

  public static boolean isAdvancedTabVisible() {
    TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
    return wprefs.getPrefShowAdvancedTab ();
  }

  /**
   * Given a file location and project instance, it checks if the file is
   * from that project's text analytics source folder(s). Works for both
   * non-modular and modular code.
   * @param project Project instance. Should not be null. Should be a text analytics project.
   * @param aqlLoc Location(absolute) of the file.
   * @return <ul><li>true if the file is in one of the search paths of a non-modular text analytics project.</li>
   * <li>true if the file is in the source folder of a modular project.</li>
   * <li>false if the project is not a text analytics project.</li>
   * <li>false when invalid arguments are provided to the method.</li></ul>
   */
  public static boolean isAQLInSearchPath(IProject project, String aqlLoc) {
    boolean isInSearchPath = false;
    if (project == null || aqlLoc == null) {
      return false;
    }
    try {
    if (!project.hasNature (Constants.PLUGIN_NATURE_ID)) {
      return false; //If project is not a text analytics project, return false.
    }
    } catch (CoreException e) {
      return false;
    }
    
    IPath projPath = project.getLocation();
    if (projPath == null) {
      Object[] params = {aqlLoc,project.getName ()};
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logWarning (Messages.getString("ProjectPreferencesUtil.ISAQLINSEARCHPATH_WARN", params)); //$NON-NLS-1$ //$NON-NLS-2$
      return false; //projPath can be null only if given project's path in indeterminable. Not sure when that can happen, but it has happened occasionally. Refer defect 34937.
    }
    aqlLoc = aqlLoc.replace(FILE_PATH_SEPARATOR, File.separator);
    
    // If the aqlLoc is inside .provenance, then do not consider it.
    if(aqlLoc.startsWith(projPath.toOSString () + File.separator + Constants.DEFAULT_PROVENANCE_FOLDER))
      return false;
    
    boolean isModularProject = ProjectUtils.isModularProject(project);
    String searchPath = ""; //$NON-NLS-1$
    if (!isModularProject) {
      SystemTProperties properties = ProjectPreferencesUtil
      .getSystemTProperties(project.getName());
      if(properties.getSearchPath() != null)
      searchPath = ProjectPreferencesUtil.getAbsolutePath(properties
          .getSearchPath());
    } else {
      IFolder srcDir = ProjectUtils.getTextAnalyticsSrcFolder (project);
      if (srcDir != null) {
        IPath srcDirPath = srcDir.getLocation ();
        if (srcDirPath == null) {
          Object[] params = {aqlLoc,project.getName ()};
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logWarning (Messages.getString("ProjectPreferencesUtil.ISAQLINSEARCHPATH_WARN", params)); //$NON-NLS-1$ //$NON-NLS-2$
          return false;
        }
        searchPath = srcDirPath.toString ();
      }
    }

    if (searchPath != null && !searchPath.isEmpty()) {
      String tempPath[] = searchPath.split(Constants.DATAPATH_SEPARATOR);

      for (String path : tempPath) {
        path = path.replace(FILE_PATH_SEPARATOR, File.separator);
        if (aqlLoc.startsWith(path)) {
          isInSearchPath = true;
          break;
        }
      }

    }

    return isInSearchPath;

  }


  /**
   * Converts relative paths to uris, with ; as delimiter
   * 
   * @param paths
   *            paths relative to workspace, with ; as delimiter. Each path
   *            should be prefixed with the workspace resource prefix
   * @see com.ibm.biginsights.textanalytics.util.common.Constants#WORKSPACE_RESOURCE_PREFIX
   * @return
   */
  public static String getURIsFromWorkspacePaths(String paths) {
    return ProjectUtils.getURIsFromWorkspacePaths (paths);
  }

  public static ExternalTypeInfo createExternalTypeInfo (Map<String,String> extDictionaryMappings, Map<String,String> extTableMappings)
  {
    ExternalTypeInfo extEnt = ExternalTypeInfoFactory.createInstance ();

    if (extTableMappings != null) {
      for (Map.Entry<String, String> extTableMapping : extTableMappings.entrySet ()) {
        String filePath = extTableMapping.getValue ();
        if (!filePath.trim ().isEmpty ()) {
          File f = FileUtils.createValidatedFile (deduceAbsolutePath (filePath));
          extEnt.addTable (extTableMapping.getKey (), f.toURI ().toString ());
        }
      }
    }

    if (extDictionaryMappings != null) {
      for (Map.Entry<String, String> extDictMapping : extDictionaryMappings.entrySet ()) {
        String filePath = extDictMapping.getValue ();
        if (!filePath.trim ().isEmpty ()) {
          File f = FileUtils.createValidatedFile (deduceAbsolutePath (filePath));
          extEnt.addDictionary (extDictMapping.getKey (), f.toURI ().toString ());
        }
      }
    }

    return extEnt;
  }

  /**
   * @param projectName
   * @return A list of fully qualified output view names
   */
  public static List<String> getOutputViews (String projectName)
  {
    if (projectName != null)
      return getOutputViews (ProjectUtils.getProject (projectName));
    else
      return new ArrayList<String> ();
  }

  /**
   * @param project
   * @return A list of fully qualified output view names
   */
  public static List<String> getOutputViews (IProject project)
  {
    ModuleMetadata[] moduleMDs = getModuleMetadata (project);
    return getOutputViews (moduleMDs);
  }

  /**
   * @param moduleMDs
   * @return A list of fully qualified output view names
   */
  public static List<String> getOutputViews (ModuleMetadata[] moduleMDs)
  {
    List<String> outViews = new ArrayList<String> ();

    for (ModuleMetadata md : moduleMDs) {
      for (String view : md.getOutputViews ()) {
        String fullOV = view;    //$NON-NLS-1$ //ModuleMetadata api will return qualified names now.
        outViews.add (fullOV);
      }
    }

    return outViews;
  }

  public static ModuleMetadata[] getModuleMetadata (IProject project)
  {
    if (project != null) {
      try {
        String[] modules = ProjectUtils.getModules (project);
        String binPath = getTamPathStr (project);
        return ModuleMetadataFactory.readMetaData (modules, binPath);
      }
      catch (Exception e) {
        Activator.getDefault ().getLog ().log (new Status (IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage ()));
      }
    }

    return new ModuleMetadata[] {};
  }

  public static ModuleMetadata[] getModuleMetadata (String projectName)
  {
    if (projectName != null) {
      IProject project = ProjectUtils.getProject (projectName);
      return getModuleMetadata (project);
    }

    return new ModuleMetadata[] {};
  }

  public static ArrayList<String> getSpanFields (ModuleMetadata moduleMD, String viewName)
  {
    TupleSchema schema = moduleMD.getViewMetadata (viewName).getViewSchema ();
    String[] fields = schema.getFieldNames ();

    Map<String, FieldType> schemaFields = new HashMap<String, FieldType>();

    for (String str : fields) {
      schemaFields.put(str, schema.getFieldTypeByName(str));
    }

    return getSpanFields(schemaFields);
  }

  public static ArrayList<String> getSpanFields (ModuleMetadata[] moduleMDs, String viewName)
  {
    String moduleName = Constants.GENERIC_MODULE;

    if (viewName.contains (".") && !viewName.endsWith (".")) //$NON-NLS-1$ //$NON-NLS-2$
      moduleName = viewName.substring (0, viewName.lastIndexOf ('.'));

    // find the module containing the view and call getSpanFields (moduleMetadata, view-name);
    // Assume module name is unique in the given array
    for (ModuleMetadata md : moduleMDs) {
      if (md.getModuleName ().equals (moduleName)) {
        return getSpanFields (md, viewName);
      }
    }

    return new ArrayList<String> ();
  }

  /**
   * gets the columns of the view which are able to be used as the options for
   * the basic properties fields
   * 
   * @return
   */
  public static ArrayList<String> getSpanFields(Map<String, FieldType> schemaFields)
  {
    ArrayList<String> ret = new ArrayList<String>();

    for (String str : schemaFields.keySet()) {
      if (schemaFields.get(str).getIsSpan()) {
        ret.add(str);
      }
    }

    return ret;
  }
  
  /**
   * Migrates System T project properties for non modular projects (v1.3/v1.4)
   * to those appropriate for modular projects (v2.0).
   * It does this by adding the new properties introduced in v2.0 along with 
   * their default values to the properties file.
   * It also makes changes to the project directory structure, add the 
   * default src and bin directories for modular projects and removing the 
   * hidden aog directory required for non modular projects.
   * @return true if migration was successful
   */
  public static boolean migrateTAPropertiesToModularFormat(IProject project) {
    Properties props = new Properties();
    Deque<IFolder> newFolders = new LinkedList<IFolder>();
    File taFile = null;
    if (project != null) {
      try {
        taFile =  FileUtils.createValidatedFile(project.getLocation() + File.separator
          + Constants.TEXT_ANALYTICS_PREF_FILE);
        if(taFile.exists()){
          props.load(new FileInputStream(taFile));

          props.put (Constants.MODULE_SRC_PATH, ProjectPreferencesUtil.createDefaultModuleSrcPath (project));
          props.put (Constants.MODULE_BIN_PATH, ProjectPreferencesUtil.createDefaultModuleBinPath (project));
          props.put (Constants.MODULAR_AQL_PROJECT, Boolean.toString (AQLNature.MODULAR_AQL_PROJECT_DEFAULT_VALUE));

          if (!props.containsKey (Constants.PAGINATION_ENABLED)) {
            props.put (Constants.PAGINATION_ENABLED, Boolean.toString (Constants.PAGINATON_ENABLED_DEFAULT_VALUE));
          }
          if (!props.containsKey (Constants.PAGINATION_FILES_PER_PAGE)) {
            props.put (Constants.PAGINATION_FILES_PER_PAGE, Integer.toString (Constants.PAGINATION_FILES_PER_PAGE_DEFAULT_VALUE));
          }

          //Removes the Property : general.mainAQLFile from Property file.
          //Modular Project do not need the Main AQL File.
          props.remove (Constants.GENERAL_MAINAQLFILE);
          
          newFolders = changeDirectoryStructuretoModularProjectStructure (project);
          
          props.store (new FileOutputStream(taFile), null);
          
          try {
            //Post migration, we need to make sure old error markers are not present.
            //Current aql builder will skip compilation if there are no modules in textanalytics source directory.
            //As that would be case immediately after migration, the old markers would still be present.
            //So, deleting all compile error markers in the project here itself.
            //If there is code in designated textanalytics src folders, build will compile them and in case of errors,
            //mark them again.
            project.deleteMarkers (AQLBuilder.COMPILE_MARKER_TYPE, true, IResource.DEPTH_INFINITE);
          } catch (CoreException e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logWarning (Messages.getString("GeneralPrefPage.MigrationMarkerWarning")); //$NON-NLS-1$
          }
        } else {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.getString("GeneralPrefPage.MigrationFailedMessage") //$NON-NLS-1$
            + Messages.getString ("GeneralPrefPage.MigrationErrorMissingTextAnalyticsPropertiesFile")); //$NON-NLS-1$
          return false;
        }
      } catch (IOException e) { //This will happen only when properties' write-to-file fails
        while (!newFolders.isEmpty ()) {
          IFolder dir = newFolders.pop ();
          try {
            if (dir.exists ()) {
              dir.delete (true, new NullProgressMonitor());
            }
          } catch (CoreException ee) {
            //Do nothing. Deletion of these folders is not critical.
          }
        }
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.getString("GeneralPrefPage.MigrationFailedMessage"),e); //$NON-NLS-1$
        return false;
      }
    } else {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.getString ("GeneralPrefPage.MigrationFailedMessage") //$NON-NLS-1$
        + Messages.getString ("GeneralPrefPage.MigrationErrorNullProject")); //$NON-NLS-1$
      
      return false;
    }
    return true;
  }
  
  /**
   * Adds the default text analytics src and bin directories,
   * if they don't already exist. Removes .aog directory.
   * @param project A non modular text analytics project is expected
   * @param props Text analytics properties for the project
   * @return Stack of folders that got newly added, in order of creation.
   */
  private static Deque<IFolder> changeDirectoryStructuretoModularProjectStructure(IProject project) {
    String taSrcDir = Constants.DEFAULT_MODULE_PATH + File.separator + Constants.DEFAULT_MODULE_SRC;
    String taBinDir = Constants.DEFAULT_MODULE_PATH + File.separator + Constants.DEFAULT_MODULE_BIN;
    //String taAogPath = ProjectPreferencesUtil.getPath(Constants.DEFAULT_AOG_DIR);
    Deque<IFolder> addedFolders = new LinkedList<IFolder>();
    
    try {
    IFolder srcLvl1 = project.getFolder (Constants.DEFAULT_MODULE_PATH);
    if (!srcLvl1.exists ()) {
      srcLvl1.create (IResource.NONE, true, new NullProgressMonitor());
      addedFolders.push (srcLvl1);
    }
    
    IFolder taSrc = project.getFolder (taSrcDir);
    if (!taSrc.exists ()) {
      taSrc.create (false, true, new NullProgressMonitor ());
      addedFolders.push (taSrc);
    }
    
    IFolder taBin = project.getFolder (taBinDir);
    if (!taBin.exists ()) {
      taBin.create (false, true, new NullProgressMonitor());
      addedFolders.push (taBin);
    }
    
//    IFolder taAogDir = project.getFolder (taAogPath);
//    if (taAogDir.exists ()) {
//      taAogDir.delete (true, new NullProgressMonitor());
//    }
    } catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.getString("GeneralPrefPage.MigrationErrorChangingDirStructure"), e); //$NON-NLS-1$
      return addedFolders;
    }
    return addedFolders;
  }
  
  /**
   * Accepts a project and determines if its text analytics properties 
   * require migration to properties appropriate for a v2.0 project
   * @param project
   * @return true if the project is using v2.x BigInsights library, has aql nature, and is not a modular aql project.
   */
  public static boolean isMigrationRequiredForProjectPropertiesToModular(IProject project) {
    String version =  BIProjectPreferencesUtil.getBigInsightsProjectVersion (project);
    if (version != null) {
      try {
      return project.isOpen () && project.hasNature (Activator.NATURE_ID) 
          && BIProjectPreferencesUtil.isAtLeast(version, BIConstants.BIGINSIGHTS_VERSION_V2) //checking if version is V2.0.0.0 or greater
          && !ProjectUtils.isModularProject (project); 
      } catch (CoreException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError ("Error determining project nature. Disabling Text Analytics migration option for project."); //$NON-NLS-1$
        return false;
      }
    } else {
      return false;
    }
  }

  /**
   * Find out whether a file is a .del one with non supported format.
   * @param file The given file
   * @return TRUE if that is a .del file with non supported format, including file corrupted;
   *         FALSE for all other cases , not a .del file, a .del with supported format...
   * @throws Exception
   */
  public static boolean isNonSupportedDelFile (File file)
  {
    if ( file != null &&
         file.isFile () &&
         file.getName ().endsWith (".del") ) { //$NON-NLS-1$

      DBDumpFileScan dbDump;
      try {
        dbDump = (DBDumpFileScan) DBDumpFileScan.makeFileScan (file);
        if (dbDump.getHaveLabelCol ())
          return false;
      }
      catch (Exception e) {
        // will return true anyway
      }

      return true;
    }

    return false;
  }

  public static boolean isTextAnalyticsProject (IProject project)
  {
    try {
      return ( project != null &&
               project.isOpen () &&
               project.hasNature (Constants.PLUGIN_NATURE_ID) );
    }
    catch (CoreException e) {
      return false;
    }
  }

  public static final boolean isAqlBuilderResource(IResource resource)
  {
    return isTextAnalyticsProject(resource.getProject ()) &&
           ( isSourceFile (resource)  ||
             isMgtFile (resource) ||
             canBeRef (resource) );
  }

  public static boolean isSourceFile (IResource resource)
  {
    if (resource == null || resource.getLocation () == null)
      return false;

    String resourceName = resource.getName();

    String aqlLoc = resource.getLocation ().toOSString ();
    IProject project = resource.getProject ();
    boolean isInSourcePath = ProjectPreferencesUtil.isAQLInSearchPath (project, aqlLoc);

    return ( isInSourcePath &&
             (resourceName.endsWith(Constants.AQL_FILE_EXTENSION) ||
              resourceName.endsWith(Constants.DICTIONARY_FILE_EXTENSION)) ||
              resourceName.endsWith(Constants.MODULE_COMMENT_FILE) );

  }

  public static boolean isMgtFile(IResource resource) 
  {
    if (resource == null)
      return false;

    String resourceName = resource.getName();

    return resourceName != null &&
           ( resourceName.endsWith(Constants.ECLIPSE_PROJECT_FILE_EXTENSION) ||
             resourceName.endsWith(Constants.BIGINSIGHTS_PROJECT_FILE_EXTENSION) ||
             resourceName.endsWith(Constants.TEXT_ANALYTICS_PREF_FILE) );
  }

  public static boolean canBeRef(IResource resource) 
  {
    if (resource == null)
      return false;

    String resourceName = resource.getName();
    return resourceName != null &&
           ( resourceName.endsWith(Constants.JAR_FILE_EXTENSION) ||
             resourceName.endsWith(Constants.ZIP_FILE_EXTENSION) ||
             resourceName.endsWith(Constants.TAM_FILE_EXTENSION) );
  }

}
