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
package com.ibm.biginsights.textanalytics.nature;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.CompilationSummary;
import com.ibm.avatar.api.CompileAQL;
import com.ibm.avatar.api.CompileAQLParams;
import com.ibm.avatar.api.exceptions.CircularDependencyException;
import com.ibm.avatar.api.exceptions.CompilerException;
import com.ibm.avatar.api.exceptions.ModuleNotFoundException;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.avatar.api.tam.ModuleMetadataFactory;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.compiler.CompilerWarning;
import com.ibm.avatar.provenance.AQLProvenanceRewriter;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectDependencyUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

@SuppressWarnings("unused")
public class ModularAQLBuilder extends AbstractAQLBuilder
{

  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  private static final String MARKER_BUILDPATH_PROBLEM = "org.eclipse.jdt.core.buildpath_problem";

  private static final ILog logger = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);

  public ModularAQLBuilder (AQLBuilder aqlBuilder)
  {
    super ();
    this.aqlBuilder = aqlBuilder;
  }

  /**
   * Used during incremental build, to look through the resource delta and determine which modules contain changes and
   * need to be rebuilt and if a build is required at all.
   */
  private class AQLDeltaVisitor implements IResourceDeltaVisitor
  {
    private Set<String> modulesToBeBuilt=new HashSet<String>();
    private boolean isBuildRequired=false;
    
    public boolean visit (IResourceDelta delta)
    {
      IResource resource = delta.getResource ();
      switch (delta.getKind ()) {
        case IResourceDelta.ADDED:
          // handle added resource
          if (ProjectPreferencesUtil.isAqlBuilderResource (resource)) {
            prepareForBuilding (resource);
            return false;
          }
        break;
        case IResourceDelta.REMOVED:
          // handle removed resource
          if (ProjectPreferencesUtil.isAqlBuilderResource (resource)) {
            prepareForBuilding (resource);
            return false;
          }
        break;
        case IResourceDelta.CHANGED:
          // handle changed resource
          if (ProjectPreferencesUtil.isAqlBuilderResource (resource)) {
            prepareForBuilding (resource);
            return false;
          }
        break;
      }

      // return true to continue visiting children.
      return true;
    }

    private void prepareForBuilding (IResource resource) {
      isBuildRequired = true;

      // For jars, do a full build.
      // Defect 64419 shows that even though the modified jar is not in any module, nor in the defined tam path,
      // it still can be referenced by any module using relative paths. Therefore, we should rebuild all modules.
      if (ProjectPreferencesUtil.canBeRef (resource) ||
          ProjectPreferencesUtil.isMgtFile (resource)) {
        String[] modules = ProjectUtils.getModules (resource.getProject ());
        if (modules != null && modules.length > 0)
          modulesToBeBuilt.addAll (Arrays.asList (modules));  // Add all modules to do a full rebuild.
      }
      else {
        String module = getModule (resource.getLocation ().toOSString ());
        if (module != null)
          modulesToBeBuilt.add (module);
      }
    }

    public Set<String> getModulesToBeBuilt() {
      return modulesToBeBuilt;
    }
    
    public boolean isBuildRequired() {
      return isBuildRequired;
    }
  }

  private static String getModule (String aqlFilePath)
  {
    IPath path = new Path (aqlFilePath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation (path);
    if (file == null)
      return null;
    else {
      String extension = "." + path.getFileExtension ();//$NON-NLS-1$
      if ( extension.endsWith (Constants.AQL_FILE_EXTENSION)
           || extension.endsWith (Constants.DICTIONARY_FILE_EXTENSION)
           || extension.endsWith (Constants.MODULE_COMMENT_FILE_EXTENSION) ) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
        IProject project = file.getProject ();
        String relativeSRCPath = ProjectUtils.getConfiguredModuleSrcPath (project.getName ());
        if (relativeSRCPath == null) return null;
        IFolder srcPathRes = root.getFolder (new Path (relativeSRCPath));
        String absSrcPath = srcPathRes.getLocation ().toOSString ();
        String modules[] = ProjectUtils.getModules (project);
        if (modules == null) return null;
        for (String module : modules) {
          IPath modulePath = new Path (absSrcPath + IPath.SEPARATOR + module + IPath.SEPARATOR);
          if (modulePath.isPrefixOf (path)) { return module; }
        }
      }
      else {
        return null;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  @SuppressWarnings("rawtypes")
  protected IProject[] build (int kind, Map args, IProgressMonitor monitor) throws CoreException
  {
    if (kind == AQLBuilder.FULL_BUILD) {
      fullBuild (monitor);
    }
    else {
      IResourceDelta delta = aqlBuilder.getDelta (getProject ());
      if (delta == null) {
        fullBuild (monitor);
      }
      else {
        incrementalBuild (delta, monitor);
      }
    }
    return null;
  }

  /**
   * Check any cyclic dependency is there.
   * 
   * @return
   * @throws CoreException
   */
  public boolean isCyclicProjectDependency (IProject proj) throws CoreException
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    int projectCount = root.getProjects ().length;

    List<IProject> refProjects = ProjectDependencyUtil.getRefdProjects (proj);
    while (!refProjects.isEmpty ()) {
      if (refProjects.contains (proj) || projectCount == 0)
        return true;
      else {
        refProjects = ProjectDependencyUtil.getRefdProjects (refProjects);
        projectCount--;
      }
    }

    return false;
  }

  /**
   * This is the method of interest in this class. It is where the AQLParser gets invoked to check for parsing errors
   * Each ParsingException is handled by an error handler which in turn will add markers to the aql file.
   * @param project IProject instance of the project to be built
   * @param modulesToBeBuilt Set of names of modules to be build in case of an incremental build. For triggering a full build, provide null.
   * @throws CoreException
   */
  private void buildInternal (IProject project, Set<String> modulesToBeBuilt) throws CoreException
  {
    IFolder binPathRes = null;

    // Checks any cyclic dependency for the project. If so, the user has to fix the cyclic dependency.
    if (isCyclicProjectDependency (project)) {
      logger.logAndShowError (Messages.getString ("ModularAQLBuilder.CIRCULAR_DEPENDENCY_DETECTED",
        new Object[] { project.getName () }));
      return;
    }

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();

    // Building the Depending Projects First.
    List<IProject> refProjects = null;
    try {
      refProjects = ProjectDependencyUtil.getRefdProjects (project);

      /**
       * This condition builds the referenced Project first. Checks if there is a tam generated for the referenced
       * project. When we are building the referencing project, we do not want to build its refered project, Hence we
       * use the flag isReferencedProjectToBeBuild. This variable is set to false when the AQLBuilder builds the
       * referencing project.
       */
      if (isReferencedProjectToBeBuild && refProjects != null) {
        for (IProject iProject : refProjects) {
          if (iProject.hasNature (Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject (iProject)) {
            String binPath = ProjectUtils.getConfiguredModuleBinPath (iProject);
            if (null == binPath) {
              logger.logError (Messages.getString ("ModularAQLBuilder.MODULE_BIN_PATH_DETERMINATION_ERROR",
                new Object[] { project.getName () }));
              return;
            }
            IFolder depBinPathRes = root.getFolder (new Path (binPath));
            if (!depBinPathRes.exists ()) continue;
            String absTempBinPath = depBinPathRes.getLocation ().toOSString ();
            String[] tempModules = ProjectUtils.getModules (iProject);
            if (tempModules != null) {
              boolean isBuildRequired = false;

              for (String mod : tempModules) {
                File file = new File (absTempBinPath + "/" + mod + Constants.TAM_FILE_EXTENSION);//$NON-NLS-1$
                if (!file.exists ()) {
                  isBuildRequired = true;
                  break;
                }
              }
              if (isBuildRequired) {
                buildInternal (iProject, null);
              }
            }
          }
        }
      }

    }
    catch (CoreException e) {
      logger.logError (e.getMessage ());
    }

    warnIgnoredAqls (project);

    String modules[] = null;
    if (modulesToBeBuilt == null) { 
      //If no set of modules to be built has been provided, assume a full build.
      modules = ProjectUtils.getModules (project);
    }
    else {
      // We are doing an incremental build. So we need to get all the modules in a project
      // referenced by the build module and build the same.
      modules = getReferenceModulesToBeBuilt (project, modulesToBeBuilt);
    }
    String moduleBinPath = ProjectUtils.getConfiguredModuleBinPath (project.getName ());
    if (null == moduleBinPath) {
      logger.logError (Messages.getString ("ModularAQLBuilder.MODULE_BIN_PATH_DETERMINATION_ERROR",
        new Object[] { project.getName () }));
      return;
    }
    binPathRes = root.getFolder (new Path (moduleBinPath));
    String absBinPath;
    if (binPathRes.getLocation () != null) {
      absBinPath = binPathRes.getLocation ().toOSString ();
    }
    else {
      absBinPath = root.getLocation ().append (binPathRes.getFullPath ()).toOSString ();
    }
    deleteExistingTams (modules, project);

    if (modules == null) { return; }

    aqlBuilder.setBuildModules (modules);
    Set<String> moduleSet = new HashSet<String> (Arrays.asList (modules));

    String relativeSRCPath = ProjectUtils.getConfiguredModuleSrcPath (project.getName ());
    if (null == relativeSRCPath) {
      logger.logError (Messages.getString ("ModularAQLBuilder.MODULE_SRC_FOLDER_DETERMINATION_ERROR",
        new Object[] { project.getName () }));
      return;
    }
    IFolder srcPathRes = root.getFolder (new Path (relativeSRCPath));
    String absSrcPath;
    if (srcPathRes.getLocation () != null) {
      absSrcPath = srcPathRes.getLocation ().toOSString ();
    }
    else {
      absSrcPath = root.getLocation ().append (srcPathRes.getFullPath ()).toOSString ();
    }

    deleteMarkers (project, modules);

    warnIgnoredAqls (project, modules);
    checkForInvalidTAMPath (project);
    Display.getDefault ().asyncExec (new Runnable () {
      @Override
      public void run ()
      {
        PlatformUI.getWorkbench ().getDecoratorManager ().update (TEXT_ANALYTICS_FOLDER_DECORATOR_ID);
      }
    });

    String modulePath = ProjectDependencyUtil.populateProjectDependencyPath (project);
    // Checks same module name exist in module path
    Map<String, List<String>> map = ProjectDependencyUtil.isDuplicateTamExist (project, modulePath);
    if (map != null && !map.isEmpty ()) {
      AQLErrorHandler reporter = new AQLErrorHandler ();
      for (String module : map.keySet ()) {
        List<String> list = map.get (module);
        if (!list.isEmpty ()) {
          String paths = "";//$NON-NLS-1$
          for (String path : list) {
            paths = paths + path + Constants.DATAPATH_SEPARATOR;
          }
          String msg = String.format ("The project can not be build. Module " + "%s" + " of project " + "%s" //$NON-NLS-1$
            + " clashes with similarly named dependent module found in project build path." //$NON-NLS-1$
          , module, project.getName ());

          // Log INFO
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (msg);
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (String.format ("Clashing path: %s", paths));//$NON-NLS-1$

          reporter.addMarker (project, msg, IMarker.SEVERITY_ERROR);
        }
      }
      return;
    }

    try {
      CompileAQLParams params = new CompileAQLParams ();
      File outputFile = new File (absBinPath);
      if (!outputFile.exists ()) {
        String path = absBinPath.replace (project.getLocation ().toOSString (), "");
        AQLErrorHandler reporter = new AQLErrorHandler ();
        String msg = String.format ("The build output directory %s of project %s does not exist. "
          + "Specify a valid build output directory in Text Analytics property sheet.", path, project.getName ());
        reporter.addMarker (project, msg, IMarker.SEVERITY_ERROR);
        return;
      }
      String outputURI = outputFile.toURI ().toString ();
      params.setOutputURI (outputURI);
      String[] inputURIs = new String[moduleSet.size ()];
      int i = 0;
      for (String temp : moduleSet) {
        inputURIs[i] = new File (absSrcPath + "/" + temp).toURI ().toString ();//$NON-NLS-1$
        i++;
      }
      params.setInputModules (inputURIs);

      params.setModulePath (modulePath);

      PreferenceStore store = ProjectUtils.getPreferenceStore (project);
      TokenizerConfig tokenizerConfig = null;
 
      // The only choice is the Standard Tokenizer
      tokenizerConfig = new TokenizerConfig.Standard();
      
      params.setTokenizerConfig (tokenizerConfig);

      CompilationSummary compileSummary = CompileAQL.compile (params);
      /*
       * Get all warnings returned by the compile API when the compiled AQLs contain only warnings, and not errors.
       */
      List<CompilerWarning> warnList = compileSummary.getCompilerWarning ();
      AQLErrorHandler warningReporter = new AQLErrorHandler ();
      for (CompilerWarning cw : warnList) {
        warningReporter.handleWarning (cw, project);
      }

      // Is provenance enabled ?
      boolean enableProvenance = ProjectPreferencesUtil.getSystemTProperties (project).getEnableProvenance ();

      // Provenance Rewriting
      if (enableProvenance) {

        persistProvenanceAOG (project, inputURIs, modulePath, tokenizerConfig);
      }
      else {
        /**
         * If provenance is disabled, ensure that we delete the contents related to provenence rewrite and provenance
         * bin.
         */
        String rewrittenAQLDir = project.getLocation ().toOSString () + File.separator
          + Constants.DEFAULT_PROVENANCE_FOLDER + File.separator + Constants.PROVENANCE_SRC;
        File rewrittenAQLDirFile = new File (rewrittenAQLDir);
        String rewrittenTAMDir = project.getLocation ().toOSString () + File.separator
          + Constants.DEFAULT_PROVENANCE_FOLDER + File.separator + Constants.PROVENANCE_BIN;
        File rewrittenTAMDirFile = new File (rewrittenTAMDir);

        // Delete the files in the directory
        delete (rewrittenAQLDirFile);
        delete (rewrittenTAMDirFile);
      }

    }
    catch (CompilerException pe1) {
      handleCompilerException (pe1);
    }
    catch (TextAnalyticsException e) {
      // This is the other exception thrown by CompileAQL.compile(...)
      // An exception that interrupts compilation. Need not be due to incorrect aql.
      // Anyway, surfacing to user. Hopefully messages are informative enough.

      // Adding to log. Not throwing an error window. don't want to vex user during autobuild.
      logger.logError (Messages.getString ("AQLBuilder.COMPILER_EXCEPTION") + "\n" + e.getMessage (), e);
      AQLErrorHandler errorMarker = new AQLErrorHandler ();
      errorMarker.addMarker (project, e.getMessage (), IMarker.SEVERITY_ERROR); // adding to problems view, at project
                                                                                // level
    }
    catch (Exception e) {
      String msg = Messages.getString ("AQLBuilder.COMPILER_EXCEPTION") + ": " + e.getMessage ();
      logger.logError (msg, e);
      AQLErrorHandler errorMarker = new AQLErrorHandler ();
      errorMarker.addMarker (project, msg, IMarker.SEVERITY_ERROR);
    }
    finally {
      try {
        /**
         * This is part of fix 35610. Refresh the Provenance folder, so that when we rename a project, it will not throw
         * the out of sync error.
         */
        ProjectUtils.getDefaultProvenanceDir (project).refreshLocal (IResource.DEPTH_INFINITE, null);
      }
      catch (CoreException e) {
        logger.logError (e.getMessage ());
      }
      refreshBinFolder (binPathRes);
      try {
        // Warn if any pre-compiled tam file references modules in source folder in workspace
        verifyTamDepOnSrc (project);
      }
      catch (TextAnalyticsException e) {
        // This is just a validation of the modules references, so there is no need to show error dialog.
        // so just logging the error.
        logger.logError (e.getMessage ());
      }
      catch (Exception e) {
        // This is just a validation so just logging the errors.
        logger.logError (e.getMessage ());
      }
    }
  }

  /**
   * CompilerException instance thrown by compiler will contain a list of compilation errors. This method extracts and
   * reports these errors to problems view.
   * 
   * @param pe1 CompilerException instance
   */
  private void handleCompilerException (CompilerException pe1)
  {
    AQLErrorHandler reporter = new AQLErrorHandler ();
    List<String> compilerExceptionMessages = new ArrayList<String> ();

    // loop thru the list of compiler exceptions to set the error marker
    // and collect error messages to show all at once.
    List<Exception> compilerException = pe1.getAllCompileErrors ();
    Iterator<Exception> itr = compilerException.iterator ();
    while (itr.hasNext ()) {
      Exception ce = itr.next ();

      // mark errors to the project
      if (ce instanceof ParseException) {
        ParseException pe = (ParseException) ce;
        reporter.handleError (pe, IMarker.SEVERITY_ERROR, project);
      }
      else if (ce instanceof CircularDependencyException) {
        CircularDependencyException pe = (CircularDependencyException) ce;
        reporter.addMarker (project, pe.getMessage (), IMarker.SEVERITY_ERROR);
      }
      else {
        // report any and all exceptions in list to problems view.
        // passing true as one of the parameters for prepareDetailedMessage since error message is required in a single
        // line.
        reporter.addMarker (project, ProjectUtils.prepareDetailedMessage (ce, true).getMessage (),
          IMarker.SEVERITY_ERROR);
      }

      // Collect error message.
      // Same problem may cause error multiple times, so messages may repeat but we don't want to show that.
      if (!compilerExceptionMessages.contains (ce.getMessage ())) compilerExceptionMessages.add (ce.getMessage ());
    }

    /*
     * Get all warnings returned by the compile API when the compiled AQLs contain warnings and errors. Add these
     * warning information to the Problems view.
     */
    CompilationSummary compileSummary = pe1.getCompileSummary ();
    List<CompilerWarning> warnList = compileSummary.getCompilerWarning ();
    for (CompilerWarning cw : warnList) {
      reporter.handleWarning (cw, project);
    }

    /*
     * To fix the task 34397, removed code logging the compilation error messages to the log file. As we are reporting
     * these problems into problems view we should not log these messages into logs.
     */

  }

  /**
   * Checks if the TAM paths specified for the project are valid and they exists. For each path that is invalid or
   * non-existent, it will add an error marker at project level.
   * 
   * @param project
   */
  private void checkForInvalidTAMPath (IProject project)
  {
    SystemTProperties projectProps = ProjectPreferencesUtil.getSystemTProperties (project);
    String tamPath = projectProps.getTamPath ();
    if (tamPath != null) {
      String tamPaths[] = tamPath.split (Constants.DATAPATH_SEPARATOR);
      for (String path : tamPaths) {
        if (path != null && !path.isEmpty ()) {
          String absPat = ProjectPreferencesUtil.getAbsolutePath (path);
          String msg = String.format (Messages.getString ("SystemtRunJob.ERR_PATH_INVALID_FOR_PRECOMPILED_TAM"),//$NON-NLS-1$
            path.replace (Constants.WORKSPACE_RESOURCE_PREFIX, ""));

          if (absPat == null) {
            ProjectUtils.addMarker (project, AQLBuilder.COMPILE_MARKER_TYPE, IMarker.SEVERITY_ERROR,
              IMarker.PRIORITY_LOW, msg);

          }
          else {
            File absTAMfile = new File (absPat);
            if (!absTAMfile.exists ()) {
              ProjectUtils.addMarker (project, AQLBuilder.COMPILE_MARKER_TYPE, IMarker.SEVERITY_ERROR,
                IMarker.PRIORITY_LOW, msg);

            }
          }
        }
      }
    }
  }

  private void warnIgnoredAqls (IProject project)
  {
    warnIgnoredAqls (project, null);
  }

  private void warnIgnoredAqls (IProject project, String[] modules)
  {
    IFolder srcFolder = ProjectUtils.getTextAnalyticsSrcFolder (project);

    if (modules == null) modules = ProjectUtils.getAllModules (project);

    if (modules == null || modules.length == 0) return;

    try {
      for (String m : modules) {
        IFolder mFolder = srcFolder.getFolder (m);
        for (IResource res : mFolder.members ()) {
          if (res instanceof IFolder) warnIgnoredAqls ((IFolder) res);
        }
      }
    }
    catch (CoreException e) {
      // Just ignore if we can't mark a warning sign to the file.
    }
  }

  private void warnIgnoredAqls (IFolder folder) throws CoreException
  {
    if (folder == null) return;

    for (IResource resource : folder.members ()) {

      if (resource instanceof IFolder) warnIgnoredAqls ((IFolder) resource);

      if (resource instanceof IFile && ((IFile) resource).getName ().toLowerCase ().endsWith (".aql")) {
        // Remove AQL errors
        resource.deleteMarkers (AQLBuilder.PARSE_MARKER_TYPE, true, IResource.DEPTH_INFINITE);
        resource.deleteMarkers (AQLBuilder.COMPILE_MARKER_TYPE, true, IResource.DEPTH_INFINITE);

        // add warning that AQL file is not compiled
        IMarker marker = resource.createMarker (AQLBuilder.COMPILE_MARKER_TYPE);
        marker.setAttribute (IMarker.MESSAGE,
          com.ibm.biginsights.textanalytics.aql.library.Messages.WRN_AQL_FILE_NOT_COMPILED);
        marker.setAttribute (IMarker.PRIORITY, IMarker.PRIORITY_LOW);
        marker.setAttribute (IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
      }

    }
  }


  /**
   * Looks up all modules that reference the given set of modules, so that may be rebuilt too.
   * 
   * @param project Project containing the modules
   * @param modulesToBeBuilt set of the modules that are going to be built.
   * @return Set of names of modules that reference the given set of modules. If any of the modules in the given set
   *         contained errors, all the modules in the project will added to the set, to be rebuilt.
   */
  @SuppressWarnings("unchecked")
  private String[] getReferenceModulesToBeBuilt (IProject project, Set<String> modulesToBeBuilt)
  {
    String modules[] = null;
    if (modulesToBeBuilt != null && !modulesToBeBuilt.isEmpty ()) {

      // If the error marker is on Project then, we need to build all the modules in a Project.
      IMarker problems[] = null;
      try {
        problems = project.findMarkers (AQLBuilder.COMPILE_MARKER_TYPE, true, IResource.DEPTH_INFINITE);
      }
      catch (CoreException e) {
        logger.logError (e.getMessage ());
      }
      if (problems != null) {
        for (IMarker problem : problems) {
          IResource resource = problem.getResource ();
          if (resource instanceof IProject) { return ProjectUtils.getModules (project); }
        }
      }

      /*
       * The below code gets all the Modules referencing the build Module in a project. Whenever we build a module all
       * the referencing modules need to be build.
       */
      String projectName = project.getName ();
      Set<String> refMod = null;
      for (String module : modulesToBeBuilt) {
        IWorkbench window = PlatformUI.getWorkbench ();
        IHandlerService handlerService = (IHandlerService) window.getService (IHandlerService.class);
        try {
          ArrayList<Parameterization> parameters = new ArrayList<Parameterization> ();
          ICommandService commandService = (ICommandService) window.getService (ICommandService.class);
          Command openCommand = commandService.getCommand (Constants.CMD_GET_ALL_REFERENCING_MODULE);

          IParameter moduleParam = openCommand.getParameter (Constants.CMD_GET_ALL_REFERENCING_MODULE_PARAM_MODULE_NAME);
          Parameterization moduleParmeterization = new Parameterization (moduleParam, module);
          parameters.add (moduleParmeterization);

          IParameter projectParam = openCommand.getParameter (Constants.CMD_GET_ALL_REFERENCING_MODULE_PARAM_PROJ_NAME);
          Parameterization projParmeterization = new Parameterization (projectParam, projectName);
          parameters.add (projParmeterization);

          ParameterizedCommand parmCommand = new ParameterizedCommand (openCommand,
            parameters.toArray (new Parameterization[parameters.size ()]));
          refMod = (Set<String>) handlerService.executeCommand (parmCommand, null);

        }
        catch (Exception ex) {
          logger.logError (ex.getMessage ());
        }

      }
      if (refMod != null && refMod.isEmpty ()) {
        return ProjectUtils.getModules (project);
      }
      if (refMod != null && !refMod.isEmpty ()) {
        modulesToBeBuilt.addAll (refMod);
      }

      modules = modulesToBeBuilt.toArray (new String[0]);

    }
    return modules;
  }

  /**
   * Get the module URI from dependent project for a project. This makes a recursive calls to go thru the dependency
   * hierarchy to get the all the modules from all dependent projects.
   * 
   * @param proj
   * @param inputModuleURI
   * @return
   * @throws CoreException
   */
  private Set<String> getDependentProject (IProject proj, Set<String> inputModuleURI) throws CoreException
  {
    IProject referencedProjectArr[];
    referencedProjectArr = proj.getReferencedProjects ();

    if (referencedProjectArr == null || referencedProjectArr.length == 0) return inputModuleURI;
    // Iterate thru the dependent project
    for (IProject iProject : referencedProjectArr) {
      IFolder srcFolder = ProjectUtils.getTextAnalyticsSrcFolder (iProject);
      String srcAbsPath = srcFolder.getLocation ().toOSString ();
      String modules[] = ProjectUtils.getModules (iProject);
      // Get the modules for a project and add it to the set
      for (String module : modules) {
        inputModuleURI.add (new File (srcAbsPath + "/" + module).toURI ().toString ());//$NON-NLS-1$
      }
      getDependentProject (iProject, inputModuleURI);
    }
    return inputModuleURI;
  }

  /**
   * Get the external imported TAM URI for a project and its dependent. This makes a recursive calls to go thru the
   * dependency hierarchy to get the all the imported tams.
   * 
   * @param proj
   * @param importedTamURI
   * @return
   * @throws CoreException
   */
  private Set<String> getImportedTams (IProject proj, Set<String> importedTamURI) throws CoreException
  {
    // Get the tam for current project.
    getTams (proj, importedTamURI);
    IProject referencedProjectArr[];
    referencedProjectArr = proj.getReferencedProjects ();

    if (referencedProjectArr == null || referencedProjectArr.length == 0) return importedTamURI;

    // Iterate thru the project
    for (IProject iProject : referencedProjectArr) {
      getTams (iProject, importedTamURI);
      getImportedTams (iProject, importedTamURI);
    }
    return importedTamURI;

  }

  /**
   * Get the external tams for a project. This will not get the external tams for the dependent project.
   * 
   * @param proj
   * @param importedTamURI
   */
  private void getTams (IProject proj, Set<String> importedTamURI)
  {
    String importedTAMS = ProjectUtils.getImportedTams (proj);
    if (importedTAMS != null && !importedTAMS.isEmpty ()) {
      String tams[] = importedTAMS.split (Constants.DATAPATH_SEPARATOR);
      for (int i = 0; i < tams.length; i++) {
        String tempAbsPath = ProjectPreferencesUtil.getAbsolutePath (tams[i]);
        if (tempAbsPath != null) importedTamURI.add (new File (tempAbsPath).toURI ().toString ());
      }
    }
  }

  /**
   * Perform provenance rewrite. Rewrite the input AQL file for provenance, compile the rewritten AQL and store the
   * resulting AOG at the location indicated by the AOG directory project property.
   * 
   * @param aqlFile Input AQL file
   * @param searchPath Search path for the input AQL file
   * @param aogDir Directoy for persisting the provenance rewrite compiled AOG.
   */
  @SuppressWarnings("restriction")
  private void persistProvenanceAOG (IProject project, String[] inputURIs, String modulePath,
    TokenizerConfig tokenizerConfig)
  {

    String inputModuleURI[] = null;
    // Enable debugging ?
    if (Constants.DEBUG_PROVENANCE)
      AQLProvenanceRewriter.debug = true;
    else
      AQLProvenanceRewriter.debug = false;

    ProjectPreferencesUtil.createDefaultProvenanceDir (project);
    String rewrittenAQLDir = ProjectPreferencesUtil.getAbsolutePath (Constants.WORKSPACE_RESOURCE_PREFIX
      + project.getFullPath ().toOSString ())
      + File.separator + Constants.DEFAULT_PROVENANCE_FOLDER + File.separator + Constants.PROVENANCE_SRC;
    File rewrittenAQLDirFile = new File (rewrittenAQLDir);
    String rewrittenTAMDir = ProjectPreferencesUtil.getAbsolutePath (Constants.WORKSPACE_RESOURCE_PREFIX
      + project.getFullPath ().toOSString ())
      + File.separator + Constants.DEFAULT_PROVENANCE_FOLDER + File.separator + Constants.PROVENANCE_BIN;
    File rewrittenTAMDirFile = new File (rewrittenTAMDir);

    // Delete the files in the directory
    delete (rewrittenAQLDirFile);
    delete (rewrittenTAMDirFile);

    try {

      /**
       * Logic of Provenance Rewrite: We create the file URI for all the modules (this includes the current projects and
       * its dependent projects). Then do a rewrite for all the modules to currentProject/.provenance/src Then we
       * generate the TAMS from currentProject/.provenance/src and pass the module path as all the imported tams in
       * current and its dependent project.
       */
      // Call to get the modules for dependent project in File URI format.
      Set<String> rewrittenDepProjPathSet = getDependentProject (project, new HashSet<String> ());

      // Add the file URI got from the dependent project.
      if (!rewrittenDepProjPathSet.isEmpty ()) {
        String[] tempInputURIs = new String[rewrittenDepProjPathSet.size () + inputURIs.length];
        int i = 0;
        for (String inpURI : rewrittenDepProjPathSet) {
          tempInputURIs[i] = inpURI;
          i++;
        }
        for (String inpURI : inputURIs) {
          tempInputURIs[i] = inpURI;
          i++;
        }
        inputURIs = tempInputURIs;
      }

      CompileAQLParams params = new CompileAQLParams ();
      params.setInputModules (inputURIs);
      params.setModulePath (modulePath);
      params.setOutputURI (rewrittenAQLDirFile.toURI ().toString ());
      params.setTokenizerConfig (tokenizerConfig);
      AQLProvenanceRewriter rewriter = new AQLProvenanceRewriter ();

      // Log INFO
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (
        String.format (Messages.getString ("AQLBuilder.INFO_PROV_REWRITE_GOING_ON") //$NON-NLS-1$ 
          + Messages.getString ("AQLBuilder.INFO_REWRITTEN_TAM"), //$NON-NLS-1$
          rewrittenTAMDirFile.getAbsolutePath ()));

      rewriter.rewriteAQL (params, null);

      // Refresh the AOG folder because for some reason the provenance
      // utility cannot access the provenance AOG otherwise
      if (rewrittenAQLDir != null) {
        IFile aogFolder = getProject ().getWorkspace ().getRoot ().getFileForLocation (new Path (rewrittenAQLDir));
        aogFolder.refreshLocal (IResource.DEPTH_INFINITE, null);
      }

      params = new CompileAQLParams ();
      File modules[] = rewrittenAQLDirFile.listFiles ();
      List<String> moduleList = new ArrayList<String> ();
      for (File file : modules) {
        if (file.isDirectory ()) {
          moduleList.add (file.toURI ().toString ());
        }
      }
      inputModuleURI = moduleList.toArray (new String[0]);
      params.setInputModules (inputModuleURI);

      // Call to get all the imported Tams for current project and dependent project.
      Set<String> tamPathSet = getImportedTams (project, new HashSet<String> ());
      String modulePathURI = "";
      for (String tamPath : tamPathSet) {
        modulePathURI = modulePathURI + tamPath + Constants.DATAPATH_SEPARATOR;
      }

      if (modulePathURI != null && !modulePathURI.isEmpty ()) {
        params.setModulePath (modulePathURI);
      }

      params.setOutputURI (rewrittenTAMDirFile.toURI ().toString ());

      params.setTokenizerConfig (tokenizerConfig);

      CompileAQL.compile (params);

      // Log INFO
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (Messages.getString ("AQLBuilder.INFO_PROV_REWRITE_DONE")); //$NON-NLS-1$

    }
    catch (Exception e) {

      String msg = String.format (Messages.getString ("AQLBuilder.ERR_PROV_REWRITE_MODULE_PROBLEM")); //$NON-NLS-1$
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (msg);

      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
    }

  }

  /**
   * Deletes from the project's bin directory, tams for the specified modules as well as tams without a corresponding
   * module in source.
   * 
   * @param modules
   * @param project
   */
  private void deleteExistingTams (String modules[], IProject project)
  {
    if (project != null) {
      String binPath = ProjectUtils.getConfiguredModuleBinPath (project);
      if (binPath != null && !binPath.trim ().isEmpty ()) {
        IFolder binPathRes = ResourcesPlugin.getWorkspace ().getRoot ().getFolder (new Path (binPath));
        Set<String> tamsToDelete = new HashSet<String> ();
        // First collect the names of tams in project's bin directory
        try {
          for (IResource mem : binPathRes.members ()) {
            String fileExtension = mem.getFileExtension ();
            if (mem instanceof IFile && fileExtension!=null && fileExtension.equalsIgnoreCase (Constants.TAM_FILE_EXTENSTION_STRING)) {
              tamsToDelete.add (mem.getName ().substring (0, mem.getName ().length () - 4)); // don't need the file
                                                                                             // extension
            }
          }
        }
        catch (CoreException e) {
          // do nothing
        }
        // Remove the names of tams from above collection, which have corresponding modules in source directory
        Set<String> modulesInProject = new HashSet<String> ();
        String[] modulesWithSrc = ProjectUtils.getModules (project); // will pick up only modules with aql files in them
        if (modulesWithSrc != null) {
          modulesInProject.addAll (Arrays.asList (modulesWithSrc));
        }
        tamsToDelete.removeAll (modulesInProject);

        // Add back to the collection, the modules in source which are about to be built
        if (modules != null) {
          tamsToDelete.addAll (Arrays.asList (modules));
        }
        if (binPathRes.getLocation () != null) {
          String absBinPath = binPathRes.getLocation ().toOSString ();
          for (String tamName : tamsToDelete) {
            File tamFile = new File (absBinPath + File.separator + tamName + Constants.TAM_FILE_EXTENSION);
            if (tamFile.exists ()) {
              tamFile.delete ();
            }
          }
        }
      }
    }
  }

  protected void fullBuild (final IProgressMonitor monitor) throws CoreException
  {
    monitor.beginTask (Messages.getString ("AQLBuilder.BUILDING_PROJECT") + getProject ().getName (), //$NON-NLS-1$
      IProgressMonitor.UNKNOWN);
    buildInternal (getProject (), null);
    monitor.done ();
  }

  protected void incrementalBuild (IResourceDelta delta, IProgressMonitor monitor) throws CoreException
  {
    monitor.beginTask (Messages.getString ("AQLBuilder.BUILDING_PROJECT") + getProject ().getName (), //$NON-NLS-1$
      IProgressMonitor.UNKNOWN);
    // the visitor does the work.
    AQLDeltaVisitor visitor = new AQLDeltaVisitor();
    delta.accept (visitor);
    if (visitor.isBuildRequired()) {
      buildInternal (getProject (), visitor.getModulesToBeBuilt ());
    }

    monitor.done ();
  }

  protected void deleteMarkers (IProject project, String[] modules)
  {
    try {

      IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
      String relativeSRCPath = ProjectUtils.getConfiguredModuleSrcPath (project.getName ());
      if (null == relativeSRCPath) {
        logger.logError (Messages.getString ("ModularAQLBuilder.MODULE_SRC_FOLDER_DETERMINATION_ERROR",
          new Object[] { project.getName () }));
        return;
      }
      IFolder srcPathRes = root.getFolder (new Path (relativeSRCPath));
      String absSrcPath = srcPathRes.getLocation ().toOSString ();
      IMarker problems[] = project.findMarkers (null, true, IResource.DEPTH_INFINITE); // Finds both compiler error and
                                                                                       // warning markers.
      if (modules != null && modules.length == ProjectUtils.getModules (project).length) {
        // This condition handles the error marked to the project. If there is error marker for a project the
        // all the modules need to be build. If this condition matches, deletes all markers for a project.
        // Deleting markers against project directories. Compiler exceptions without filename information associated to
        // them will be marked against the project directory.
        // see AbstractAQLBuilder#AQLErrorHandler
        for (IMarker problem : problems) {
          if (problem.getResource ().getProject ().equals (project)) {
            problem.delete ();
          }
        }
      }
      else if (modules != null) {
        // Deletes the markers for a particular module.
        for (String module : modules) {
          IPath modulePath = new Path (absSrcPath + IPath.SEPARATOR + module);
          for (IMarker problem : problems) {
            IPath errorFilePath = problem.getResource ().getLocation ();
            if (modulePath.isPrefixOf (errorFilePath)) {
              problem.delete ();
            }
          }
        }
      }

    }
    catch (CoreException ce) {
      System.err.println (Messages.getString ("AQLBuilder.ERR_UNABLE_DELETE_MARKERS") //$NON-NLS-1$
        + ce.getMessage ());
    }
    catch (NullPointerException ce) {
      System.err.println (Messages.getString ("AQLBuilder.ERR_UNABLE_DELETE_MARKERS") //$NON-NLS-1$
        + ce.getMessage ());
    }

  }

  /**
   * Verifies that the pre-compiled modules consumed by the specified project and its dependent projects do not refer
   * back to source modules in the workspace. For any such instances of pre-compiled tams referring to source, a project
   * level WARN marker is set.
   * 
   * @param project text analytics project for which dependency has to be checked
   * @throws Exception this can arise while reading metdata of a module.
   */
  private void verifyTamDepOnSrc (IProject project) throws Exception
  {

    // list to hold all the projects needed for verifying the module dependencies
    ArrayList<IProject> requiredProjects = new ArrayList<IProject> ();

    // Set to hold src modules of all required projects
    Set<String> srcModules = new HashSet<String> ();

    // add current project and its dependent projects as required projects
    requiredProjects.add (project);
    IProject[] refProjects = project.getReferencedProjects ();
    requiredProjects.addAll (Arrays.asList (refProjects));

    // for each project in required project list get all the src module names
    for (IProject proj : requiredProjects) {
      String[] modulesPerProj = ProjectUtils.getModules (proj);
      srcModules.addAll (Arrays.asList (modulesPerProj));
    }

    // get all the pre-compiled module locations of all the required projects
    String preCompiledModuleLocs = getPreCompiledModuleLocs (requiredProjects);

    // verify the module dependencies for each project ie current project and its referenced projects.
    for (IProject proj : requiredProjects) {
      if (!ProjectUtils.hasBuildErrors(proj))
        verifyModuleDeps (proj, preCompiledModuleLocs, srcModules);
    }

  }

  /**
   * Fetches a semicolon separated string containing only pre-compiled module locations for all the projects in project
   * List
   * 
   * @param projectsList an Array list of projects.
   * @return a string containing pre-compiled module locations for all pojects in the projectslist
   * @throws CoreException exception thrown while getting the projects tam path.
   */
  private String getPreCompiledModuleLocs (ArrayList<IProject> projectsList) throws CoreException
  {
    StringBuilder preCompiledModuleLocs = new StringBuilder ();

    // for each project get only the pre-compiled modules locations not the bin paths
    for (IProject project : projectsList) {
      preCompiledModuleLocs.append (ProjectPreferencesUtil.getProjectTamPathStr (project));
    }
    return preCompiledModuleLocs.toString ();
  }

  /**
   * Verifies pre-compiled modules dependencies on modules in source. If this scenario occurs then project level warning
   * marker is set.
   * 
   * @param project to verify the module deps in all the pre-compiled modules located in this project.
   * @param modulePath pre-compiled modules location of given project and its referenced projects.
   * @param srcModules a set of all modules in workspace whose source is available.
   * @throws Exception this can occur while reading metadata of a module.
   */
  private void verifyModuleDeps (IProject project, String modulePath, Set<String> srcModules) throws Exception
  {
    // get all the tam and bin paths referenced in the project and its dependent projects
    String projectModulePath = ProjectPreferencesUtil.getTamPathStr (project);

    // get recursively all the pre-compiled modules referenced in the given project, located in one of its module path
    // or its referenced projects
    Set<String> modulesList = ProjectUtils.getTamsImportedBySrc (project, projectModulePath);

    // for each module read the metadata and try to load it using only pre-compiled module locations of projects.
    for (String module : modulesList) {
      try {
        String[] moduleArray = { module };
        ModuleMetadataFactory.readAllMetaData (moduleArray, modulePath);
      }
      catch (ModuleNotFoundException e) {
        // verify if the module name exists as one of the src modules
        if (srcModules.contains (e.getModuleName ())) {
          // If a module is not found then set a project level WARN marker with referenced and referencing module
          // details.
          String referencedModule = String.format ("'%s'", e.getModuleName ());
          String referencingModule = String.format ("'%s'", module);
          Object[] params = { referencingModule, referencedModule };
          String msg = String.format (Messages.getString ("ModularAQLBuilder.INVALID_MODULE_REFERENCE", params));//$NON-NLS-1$
          AQLErrorHandler reporter = new AQLErrorHandler ();
          reporter.addMarker (project, msg, IMarker.SEVERITY_WARNING);
        }
        else {
          // If the pre-compiled module reference to src is not found in srcModule set then log error to
          // let the user know about src module not being found. This will not happen as all he src modules
          // will be available and in case modules are not found then error markers would have been set already.
          logger.logError (e.getMessage ());
        }
      }
    }
  }
}
