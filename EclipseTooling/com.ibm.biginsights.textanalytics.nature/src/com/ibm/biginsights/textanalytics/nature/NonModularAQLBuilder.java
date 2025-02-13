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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferenceStore;

import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.CompilationSummary;
import com.ibm.avatar.api.CompileAQL;
import com.ibm.avatar.api.CompileAQLParams;
import com.ibm.avatar.api.exceptions.CompilerException;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.compiler.CompilerWarning;
import com.ibm.avatar.provenance.AQLProvenanceRewriter;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class NonModularAQLBuilder extends AbstractAQLBuilder
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

  private static boolean isBuildRequired = false;

  public NonModularAQLBuilder (AQLBuilder aqlBuilder)
  {
    super ();
    this.aqlBuilder = aqlBuilder;
  }

  private class AQLDeltaVisitor implements IResourceDeltaVisitor
  {
    public boolean visit (IResourceDelta delta)
    {
      IResource resource = delta.getResource ();
      switch (delta.getKind ()) {
        case IResourceDelta.ADDED:
          // handle added resource
          if (ProjectPreferencesUtil.isAqlBuilderResource (resource)) {
            isBuildRequired = true;
            return false;
          }
        break;
        case IResourceDelta.REMOVED:
          // handle removed resource
          if (ProjectPreferencesUtil.isAqlBuilderResource (resource)) {
            isBuildRequired = true;
            return false;
          }
        break;
        case IResourceDelta.CHANGED:
          // handle changed resource
          if (ProjectPreferencesUtil.isAqlBuilderResource (resource)) {
            isBuildRequired = true;
            return false;
          }
        break;
      }

      // For folders and strf files, do not set isBuildRequired to false
      // but keep its
      // current value. This is for fixing defect 20018, in which the
      // files and folder
      // created by running the run config are visited last and make the
      // modified aql
      // not built.
      if (!(resource instanceof IFolder)
        && !(resource instanceof IFile && resource.getName ().endsWith (Constants.STRF_FILE_EXTENSION_WITH_DOT)))
        isBuildRequired = false;

      // return true to continue visiting children.
      return true;
    }
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
    return getProject ().getReferencedProjects ();
  }

  /*
   * This is the method of interest in this class. It is where the AQLParser gets invoked to check for parsing errors
   * Each ParsingException is handled by an error handler which in turn will add markers to the aql file.
   */
  private void build (IProject project)
  {
    IFile mainAQLFileResource = null;
    String aogStorePath = null;
    try {
      String systemTPreferencesFile = project.getLocation () + File.separator + Constants.TEXT_ANALYTICS_PREF_FILE;
      PreferenceStore preferenceStore = new PreferenceStore (systemTPreferencesFile);
      try {
        preferenceStore.load ();
      }
      catch (IOException e) {
        if (e instanceof FileNotFoundException) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
            Messages.getString ("TEXT_ANALYTICS_FILE_NOT_FOUND")//$NON-NLS-1$
              + project.getName ());
          return;

        }
        else {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage (), e);
        }
      }
      // Get the AOG Store path
      // Create the .aog folder if it does not exist yet (the user might
      // have deleted it)
      ProjectPreferencesUtil.createDefaultAOGDir (project);
      project.refreshLocal (1, new NullProgressMonitor ());
      // String aogStorePath =
      // preferenceStore.getString(Constants.GENERAL_AOGPATH);
      aogStorePath = ProjectPreferencesUtil.getDefaultAOGPath (project);
      String absAOGStorePath = ProjectPreferencesUtil.getAbsolutePath (aogStorePath);
      // Get the absolute search path
      String searchDataPath = preferenceStore.getString (Constants.SEARCHPATH_DATAPATH);
      String absSearchPath = ProjectPreferencesUtil.getAbsolutePath (searchDataPath);
      // Get the absolute Main AQL path
      String mainAQLPath = preferenceStore.getString (Constants.GENERAL_MAINAQLFILE);
      // Is provenance enabled ?
      boolean enableProvenance = preferenceStore.getBoolean (Constants.GENERAL_PROVENANCE);
      if (!StringUtils.isEmpty (mainAQLPath)) {
        String absMainAQLPath = ProjectPreferencesUtil.getAbsolutePath (mainAQLPath);
        if (StringUtils.isEmpty (absMainAQLPath)) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
            Messages.getString ("SystemtRunJob.ERR_MAIN_AQL_FILE_DOES_NOT_EXIST")//$NON-NLS-1$
              + ProjectPreferencesUtil.getPath (mainAQLPath));
          return;
        }
        // Whatever AQL file is changed in the project - compile only
        // the main aql and show the
        // errors against that one
        // So get the reference to the mainAQL File
        mainAQLFileResource = FileBuffers.getWorkspaceFileAtLocation (new Path (absMainAQLPath));
        if (mainAQLFileResource != null) {
          deleteMarkers (mainAQLFileResource, searchDataPath);
          // Delete the existing AOGs in the path - so as not to cause
          // confusion
          deleteExistingAOGs (absAOGStorePath);

          TokenizerConfig tokenizerConfig = null;
          tokenizerConfig = new TokenizerConfig.Standard();
          
          if (absAOGStorePath != null) {

            File file = new File (absAOGStorePath);
            CompileAQLParams params = new CompileAQLParams (new File (absMainAQLPath), file.toURI ().toString (),
              absSearchPath);
            if (tokenizerConfig != null) params.setTokenizerConfig (tokenizerConfig);
            
            CompilationSummary compileSummary = CompileAQL.compile (params);
            /*
             * Get all warnings returned by the compile API when the compiled AQLs contain only warnings, and not errors.
             */
            List<CompilerWarning> warnList = compileSummary.getCompilerWarning ();
            AQLErrorHandler warningReporter = new AQLErrorHandler ();
            for(CompilerWarning cw : warnList){
              warningReporter.handleWarning (cw, project);
            }
            
          }

          // Provenance Rewriting
          if (enableProvenance) {
            persistProvenanceAOG (project, absMainAQLPath, absSearchPath, tokenizerConfig);
          }
        }
      }

    }

    catch (CompilerException pe1) {
      AQLErrorHandler reporter = new AQLErrorHandler ();

      List<Exception> compilerException = pe1.getAllCompileErrors ();
      Iterator<Exception> itr = compilerException.iterator ();
      while (itr.hasNext ()) {
        try {
          Exception ce = itr.next ();
          if (ce instanceof ParseException) {
            ParseException pe = (ParseException) ce;
            reporter.handleError (pe, IMarker.SEVERITY_ERROR, mainAQLFileResource);
          }
          else if (ce instanceof RuntimeException) {
            RuntimeException re = (RuntimeException) ce;
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
              Messages.getString ("AQLBuilder.COMPILER_EXCEPTION"),//$NON-NLS-1$
              re);
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
              Messages.getString ("AQLBuilder.COMPILER_EXCEPTION") + "\n\n" + ce.getMessage ()); //$NON-NLS-1$ //$NON-NLS-2$

          }
          else {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
              Messages.getString ("AQLBuilder.COMPILER_EXCEPTION"),//$NON-NLS-1$
              ce);
          }

        }
        catch (Exception e) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage (), e);
        }
      }
      
      /*
       * Get all warnings returned by the compile API when the compiled AQLs contain warnings and errors. 
       * Add these warning information to the Problems view. 
       */
      CompilationSummary compileSummary = pe1.getCompileSummary ();
      List<CompilerWarning> warnList = compileSummary.getCompilerWarning ();
      for(CompilerWarning cw : warnList){
        reporter.handleWarning (cw, project);
      }
      
    }
    catch (Exception e) {
      // TODO - Some valid exceptions are not returning the line numbers -
      // Need to fix this.
      // need to add these to statement list and returned as parsed
      // exception, runtime defect.
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage (), e);
    }
    finally {
      IResource iresource = ResourcesPlugin.getWorkspace ().getRoot ().findMember (
        new Path (aogStorePath.replace (Constants.WORKSPACE_RESOURCE_PREFIX, ""))); //$NON-NLS-1$
      refreshBinFolder ((IFolder) iresource);
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
  private void persistProvenanceAOG (IProject project, String absMainAQLPath, String searchPath,
    TokenizerConfig tokenizerConfig)
  {

    // Enable debugging ?
    if (Constants.DEBUG_PROVENANCE)
      AQLProvenanceRewriter.debug = true;
    else
      AQLProvenanceRewriter.debug = false;

    ProjectPreferencesUtil.createDefaultProvenanceDir (project);

    File aqlFilePathFile = new File (absMainAQLPath);
    String rewrittenAQLDir = ProjectPreferencesUtil.getAbsolutePath (Constants.WORKSPACE_RESOURCE_PREFIX
      + project.getFullPath ().toOSString ())
      + File.separator + Constants.DEFAULT_PROVENANCE_FOLDER + File.separator + Constants.PROVENANCE_SRC;
    String rewrittenTAMDir = ProjectPreferencesUtil.getAbsolutePath (Constants.WORKSPACE_RESOURCE_PREFIX
      + project.getFullPath ().toOSString ())
      + File.separator + Constants.DEFAULT_PROVENANCE_FOLDER + File.separator + Constants.PROVENANCE_BIN;

    File rewrittenAQLDirFile = new File (rewrittenAQLDir);
    // Delete all the files in the rewritten directory
    delete (rewrittenAQLDirFile);

    CompileAQLParams params = new CompileAQLParams (aqlFilePathFile, rewrittenAQLDirFile.toURI ().toString (),
      searchPath);
    params.setTokenizerConfig (tokenizerConfig);
    AQLProvenanceRewriter rewriter = new AQLProvenanceRewriter ();
    try {
      // Log INFO
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (
        String.format (
          Messages.getString ("AQLBuilder.INFO_PROV_REWRITE_GOING_ON") + Messages.getString ("AQLBuilder.INFO_SEARCH_PATH") //$NON-NLS-1$ //$NON-NLS-2$
            + Messages.getString ("AQLBuilder.INFO_REWRITTEN_TAM"), searchPath, //$NON-NLS-1$
          rewrittenTAMDir));

      rewriter.rewriteAQL (params, null);

      // Refresh the AOG folder because for some reason the provenance
      // utility cannot access the provenance AOG otherwise
      if (rewrittenAQLDir != null) {
        project.refreshLocal (IResource.DEPTH_INFINITE, null);

        // Log INFO
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (Messages.getString ("AQLBuilder.INFO_PROV_REWRITE_DONE")); //$NON-NLS-1$
      }

      params = new CompileAQLParams ();
      File modules[] = rewrittenAQLDirFile.listFiles ();
      List<String> moduleList = new ArrayList<String> ();
      for (File file : modules) {
        if (file.isDirectory ()) {
          moduleList.add (file.toURI ().toString ());
        }
      }
      String inputModuleURI[] = moduleList.toArray (new String[0]);
      params.setInputModules (inputModuleURI);

      File rewrittenTAMDirFile = new File (rewrittenTAMDir);
      // Delete all the files in the rewritten directory
      delete (rewrittenTAMDirFile);

      params.setOutputURI (rewrittenTAMDirFile.toURI ().toString ());

      params.setTokenizerConfig (tokenizerConfig);

      CompileAQL.compile (params);

    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
        String.format (Messages.getString ("AQLBuilder.ERR_PROV_REWRITE_PROBLEM"), //$NON-NLS-1$
          absMainAQLPath), e);
    }
    finally {
      try {
        IResource iresource = ResourcesPlugin.getWorkspace ().getRoot ().findMember (
          new Path (project.getFullPath ().toOSString () + File.separator + Constants.DEFAULT_PROVENANCE_FOLDER
            + File.separator + Constants.PROVENANCE_BIN));
        if (iresource != null && iresource.exists ())
          iresource.refreshLocal (IResource.DEPTH_ONE, null);
      }
      catch (CoreException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
      }

    }

  }

  private void deleteExistingAOGs (String absAOGStorePath)
  {
    if (absAOGStorePath != null) {
      File aogDir = new File (absAOGStorePath);
      File[] filesInAOGDir = aogDir.listFiles ();
      if (filesInAOGDir != null) {
        for (int k = 0; k < filesInAOGDir.length; k++) {
          String fileName = filesInAOGDir[k].getName ();
          if (fileName.contains (Constants.AOG_FILE_EXTENSION) || fileName.contains (Constants.TAM_FILE_EXTENSION)) {
            filesInAOGDir[k].delete ();
          }
        }
      }
    }
  }

  protected void fullBuild (final IProgressMonitor monitor)
  {
    monitor.beginTask (Messages.getString ("AQLBuilder.BUILDING_PROJECT") + getProject ().getName (), //$NON-NLS-1$
      IProgressMonitor.UNKNOWN);
    build (getProject ());
    monitor.done ();
  }

  protected void incrementalBuild (IResourceDelta delta, IProgressMonitor monitor) throws CoreException
  {
    monitor.beginTask (Messages.getString ("AQLBuilder.BUILDING_PROJECT") + getProject ().getName (), //$NON-NLS-1$
      IProgressMonitor.UNKNOWN);
    // the visitor does the work.
    delta.accept (new AQLDeltaVisitor ());
    if (isBuildRequired) {
      build (getProject ());
    }

    monitor.done ();
  }

  protected void deleteMarkers (IFile file, String dataPath)
  {
    try {
      if (file != null) {
        // Choosing the DEPTH_INFINITE because we want to delete all the
        // problem markers in the
        // entire project before compilation
        // If you don't delete across project - some markers against
        // individual aql files in the
        // project will not be deleted
        // when the problem is fixed

        // search the searchpath, if it contains other projects as well,
        // then
        // delete compile errors from those files as well.
        ArrayList<String> searchPathList = getSearchPathList (dataPath);
        Iterator<String> searchPathIterator = searchPathList.iterator ();
        while (searchPathIterator.hasNext ()) {
          String searchPath = searchPathIterator.next ();
          // check if workspace resource
          if (ProjectPreferencesUtil.isWorkspaceResource (searchPath)) {
            searchPath = ProjectPreferencesUtil.getPath (searchPath);
            IResource iresource = ResourcesPlugin.getWorkspace ().getRoot ().findMember (new Path (searchPath));
            if (iresource != null && iresource.getProject () != null) {
              iresource.getProject ().deleteMarkers (null, false, IResource.DEPTH_INFINITE); // Removes both compiler error and warning markers.
            }
          }
          else {
            // do nothing for external resources
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

}
