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
package com.ibm.biginsights.textanalytics.aql.editor.refactoring;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import com.ibm.biginsights.textanalytics.aql.editor.Messages;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 *  Babbar
 */
public class ResourceChangeListener implements IResourceChangeListener
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

  DeltaVisitor deltaVisitor = new DeltaVisitor ();
  protected Set<IResource> addedResources = new HashSet<IResource> ();
  protected Set<IResource> removedResources = new HashSet<IResource> ();
  protected Set<IResource> changedResources = new HashSet<IResource> ();
  protected boolean bTextAnalyticsProject = false;
  protected boolean bDeleteEvent = false;
  protected boolean bAddedEvent = false;
  protected boolean bChangedEvent = false;
  ArrayList<String> changedFileList;

  // Added for handling removed project type...
  String remProjectLocation = "";//$NON-NLS-1$
  String remProjectSearchPath = "";//$NON-NLS-1$
  boolean isRemProjModular = true;

  public ResourceChangeListener ()
  {}

  @Override
  public void resourceChanged (IResourceChangeEvent event)
  {
    switch (event.getType ()) {
      case IResourceChangeEvent.PRE_DELETE:
        IResource resource = event.getResource ();
        if (resource != null && resource instanceof IProject) {
          IProject project = (IProject) resource;
          try {
            if (project.isOpen () && project.hasNature (Constants.PLUGIN_NATURE_ID)
              && ProjectUtils.isModularProject (project)) {
              remProjectLocation = project.getLocation ().toOSString ();
              isRemProjModular = ProjectUtils.isModularProject (project);
              remProjectSearchPath = getSearchPath (project);
            }
          }
          catch (CoreException e) {
            e.printStackTrace ();
          }
        }
      break;
      case IResourceChangeEvent.POST_CHANGE:
        // Defect 64419
        Set<IProject> affectedProjects = getAffectedProjects (event);
        if (!affectedProjects.isEmpty ()) ProjectUtils.cleanProjects (affectedProjects); // Clean the affected projects
                                                                                         // so they are rebuilt.

        try {
          addedResources = new HashSet<IResource> (); // given time, this can probably be consolidated into a single
                                                      // attribute
          removedResources = new HashSet<IResource> ();
          changedResources = new HashSet<IResource> ();

          event.getDelta ().accept (deltaVisitor); // deltaVisitor sets above three attributes

          if (!removedResources.isEmpty () || !addedResources.isEmpty () || !changedResources.isEmpty ()) {
            new ResourceChangeActionThread (removedResources, addedResources, changedResources).start ();
          }
        }
        catch (CoreException e) {
          logger.logError (e.getMessage ());
        }
        finally {
          bTextAnalyticsProject = false;
          bDeleteEvent = false;
          bAddedEvent = false;
          bChangedEvent = false;
        }
      break;
    }
  }

  private Set<IProject> getAffectedProjects (IResourceChangeEvent event)
  {
    IResourceDelta resDelta = event.getDelta ();
    IResourceDelta[] resDeltas = new IResourceDelta[] { resDelta };

    Set<IProject> affProjects = null;
    try {
      affProjects = getAffectedProjects (resDeltas);
    }
    catch (CoreException e) {
      affProjects = new HashSet<IProject> ();
    }

    return affProjects;
  }

  private Set<IProject> getAffectedProjects (IResourceDelta[] resDeltas) throws CoreException
  {
    Set<IProject> projects = new HashSet<IProject> ();

    for (IResourceDelta resDelta : resDeltas) {
      if (resDelta == null) continue;

      IResource resource = resDelta.getResource ();

      // Modified resource is a folder containing a modified tam file or a zip/jar file
      if (isFolderWithModifiedTam (resDelta) || resource.getName ().endsWith (".zip")
        || resource.getName ().endsWith (".jar")) {
        Set<IProject> refProjects = ProjectPreferencesUtil.getReferencingProjects (resource);
        projects.addAll (refProjects);
      }
      // continue looking at modified children
      else {
        IResourceDelta[] childResDeltas = resDelta.getAffectedChildren ();
        Set<IProject> refProjects = getAffectedProjects (childResDeltas);
        projects.addAll (refProjects);
      }
    }

    return projects;
  }

  private boolean isFolderWithModifiedTam (IResourceDelta resDelta)
  {
    IResource resource = resDelta.getResource ();

    if (resource instanceof IFolder || resource instanceof IProject) { // project is also a folder, but IProject does
                                                                       // not subclass/implement IFolder
      IResourceDelta[] childResDeltas = resDelta.getAffectedChildren ();
      if (childResDeltas != null) {
        for (IResourceDelta crd : childResDeltas) {
          IResource r = crd.getResource ();
          if (r.getName ().endsWith (".tam")) return true;
        }
      }
    }

    return false;
  }

  private String getSearchPath (IProject project) throws CoreException
  {
    String searchPath = "";
    if (!isRemProjModular) {
      if (project.isOpen () && project.hasNature (com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID)) {
        SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties (project.getName ());
        searchPath = ProjectPreferencesUtil.getAbsolutePath (properties.getSearchPath ());
      }
    }
    else {
      IResource srcDir = ProjectUtils.getTextAnalyticsSrcFolder (project);
      if (srcDir != null) {
        searchPath = srcDir.getLocation ().toString ();
      }
    }
    return searchPath;
  }

  public void buildProjectContainingResource (final IResource resource)
  {
    WorkspaceJob touchJob = new WorkspaceJob (Messages.ResourceChangeListener_BUILD_JOB_NAME) {
      @Override
      public IStatus runInWorkspace (IProgressMonitor progressMonitor) throws CoreException
      {
        try {
          if (progressMonitor != null) progressMonitor.beginTask ("", 1); //$NON-NLS-1$
          IProject project = resource.getProject ();
          if (project != null && project.isOpen () && project.getWorkspace ().isAutoBuilding ()) // start build if auto
                                                                                                 // build is on
            project.build (IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
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
   * Checks if the resource is a folder that is part of TA project's source
   * 
   * @param resource
   * @return true if resource is a folder in a modular project's aql source directory or in non modular project's search
   *         path
   */
  protected static final boolean isResourceAFolderInAQLSrcDir (IResource resource)
  {
    if (resource instanceof IFolder) {
      IFolder folder = (IFolder) resource;
      IProject project = folder.getProject ();
      if (ProjectUtils.isModularProject (project)) {
        String srcDir = ProjectUtils.getConfiguredModuleSrcPath (project);
        if (srcDir != null && !srcDir.trim ().isEmpty ()) {
          IPath srcDirPath = new Path (srcDir);
          IPath folderPath = folder.getFullPath ();
          if (srcDirPath.isPrefixOf (folderPath)) { return true; }
        }
      }
    }
    return false;
  }

  private class DeltaVisitor implements IResourceDeltaVisitor
  {

    @Override
    public boolean visit (IResourceDelta delta) throws CoreException
    {
      IResource res = delta.getResource ();

      // If not part of a TA project, skip and don't bother to look at its children.
      // The case where a resource in a non-TA project is referenced by a TA project
      // is handled separately.
      if ((res instanceof IProject || res instanceof IFile || res instanceof IFolder)
        && !ProjectPreferencesUtil.isTextAnalyticsProject (res.getProject ()))
        return false;

      // If not AQL resource, skip and look at its children.
      else if ((res instanceof IFile || res instanceof IFolder) && !ProjectPreferencesUtil.isAqlBuilderResource (res))
        return true;

      switch (delta.getKind ()) {
        case IResourceDelta.ADDED:
          addedResources.add (res);
          bAddedEvent = true;
          if (res instanceof IProject) // When a project is added, we don't need to handle every single file of it.
            return false;
        break;
        case IResourceDelta.REMOVED:
          removedResources.add (res);
          bDeleteEvent = true;
        break;
        case IResourceDelta.CHANGED:
          int resourceChangeFlag = delta.getFlags ();
          if (res instanceof IProject) {
            if ((resourceChangeFlag & IResourceDelta.OPEN) != 0) { // Indicates resource change is either opening or
                                                                   // closing of a project.
              if (((IProject) res).isOpen ()) { // Resource change is opening of a project.
                addedResources.add (res); // Opening a project can be considered analogous to adding it.
                bAddedEvent = true;
              }
              else { // else resource change is closing of a project.
                removedResources.add (res); // closing a project can be considered analogous to removing it.
                bDeleteEvent = true;
              }
            }
          }
          else if (ProjectPreferencesUtil.isMgtFile (res)) {
            changedResources.add (res);
            bChangedEvent = true;
          }
          else {
            if (res instanceof IFile && res.getName ().equals (Constants.MODULE_COMMENT_FILE)) {
              IPath path = res.getLocation ();
              String commentFilePath = path.toOSString ();
              IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
              IProject project = res.getProject ();
              String relativeSRCPath = ProjectUtils.getConfiguredModuleSrcPath (project.getName ());
              // If the relativeSRCPath is null, then we cannot determine the module path, hence we return false
              if (relativeSRCPath == null) return false;
              IFolder srcPathRes = root.getFolder (new Path (relativeSRCPath));
              String absSrcPath = srcPathRes.getLocation ().toOSString ();
              Set<String> moduleSet = new HashSet<String> ();
              IResource[] allFiles = srcPathRes.members ();
              for (IResource resrc : allFiles) {
                if (resrc instanceof IFolder) moduleSet.add (resrc.getName ());
              }
              String[] modules = null;
              if (!moduleSet.isEmpty ()) {
                modules = moduleSet.toArray (new String[0]);
              }
              if (modules != null) {
                for (String module : modules) {
                  String modPath = absSrcPath + File.separator + module + File.separator;
                  int occuranceIndex = commentFilePath.indexOf (modPath);

                  if (occuranceIndex != -1 &&
                  // Check whether the module.info is under Module Folder.
                  // Will not consider the module.info under sub-folders
                    commentFilePath.replace (modPath, "").indexOf (File.separator) == -1) {
                    changedResources.add (res);
                    bChangedEvent = true;
                  }
                }
              }
            }
          }
        break;
      }

      return true;
    }

  }

}
