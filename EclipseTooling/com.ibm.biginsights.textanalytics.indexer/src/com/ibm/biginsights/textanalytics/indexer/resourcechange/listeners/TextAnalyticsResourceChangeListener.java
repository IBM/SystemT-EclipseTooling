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
package com.ibm.biginsights.textanalytics.indexer.resourcechange.listeners;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.ibm.biginsights.textanalytics.indexer.impl.ExtractionPlanIndexer;
import com.ibm.biginsights.textanalytics.indexer.types.ResourceAction;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * This class is responsible for checking any resource changes.
 */
public class TextAnalyticsResourceChangeListener implements IResourceChangeListener
{
	@SuppressWarnings("unused")


  protected IResource addedResource;
  protected IResource removedResource;
  protected IResource changedResource;
  protected boolean hasRenameOccurred = false;

  @Override
  public void resourceChanged (IResourceChangeEvent event)
  {

    switch (event.getType ()) {
/*   Do not delete this commented section.  
     case IResourceChangeEvent.PRE_DELETE:
        IResource resource = event.getResource ();
        if (resource != null && resource instanceof IProject) {
          IProject project = (IProject) resource;
          ResourceAction resourceChangeType = null;
          try {
            if (project.isOpen () && project.hasNature (Constants.PLUGIN_NATURE_ID)
                && ProjectUtils.isModularProject (project) ) {
              // Project is Removed
              resourceChangeType = ResourceAction.DELETED;
              new TextAnalyticsResourceActionThread (resource, null, null, resourceChangeType).start ();
            }
          }
          catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace ();
          }
        }

        break;
*/      case IResourceChangeEvent.POST_CHANGE:
        try {

          addedResource = null;
          removedResource = null;
          changedResource = null;
          hasRenameOccurred = false;
          event.getDelta ().accept (new PostChangeDeltaVisitor ());

        }
        catch (CoreException e) {
          // TODO Auto-generated catch block
          e.printStackTrace ();
        }
        break;
    }

  }

  private class PostChangeDeltaVisitor implements IResourceDeltaVisitor
  {

    private final String FILE_SEPERATOR = File.separator;

    private boolean isIntrestedProject (IProject project)
    {
      try {
        if (project != null && project.isOpen () && project.hasNature (Constants.PLUGIN_NATURE_ID)
            && ProjectUtils.isModularProject (project)) return true;
      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
      return false;

    }

    private boolean isIntrestedAQL (IFile file)
    {
      IPath path = file.getLocation ();
      String aqlFilePath = path.toOSString ();
      String extension = "." + path.getFileExtension ();//$NON-NLS-1$
      if ((extension.endsWith (Constants.AQL_FILE_EXTENSION))) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
        IProject project = file.getProject ();
        String relativeSRCPath = ProjectUtils.getConfiguredModuleSrcPath (project.getName ());
        //If the relativeSRCPath is null, then we cannot determine the module path, hence we return false
        if (null == relativeSRCPath) return false;
        IFolder srcPathRes = root.getFolder (new Path (relativeSRCPath));
        String absSrcPath = srcPathRes.getLocation ().toOSString ();
        String modules[] = ProjectUtils.getModules (project);
        if (modules != null) {
          for (String module : modules) {
            String modPath = absSrcPath + FILE_SEPERATOR + module + FILE_SEPERATOR;
            int occuranceIndex = aqlFilePath.indexOf (modPath);

            if (occuranceIndex != -1 &&
                // Check whether the AQL is under Module Folder.
                // Will not consider the AQL inder sub-folders
                aqlFilePath.replace (modPath, "").indexOf (FILE_SEPERATOR) == -1) //$NON-NLS-1$
              return true;
          }
        }
      }
      return false;
    }
/*
    private boolean isIntrestedModuleFolder (IFolder folder)
    {
      IPath path = folder.getLocation ();
      String aqlFilePath = path.toOSString ();
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
      IProject project = folder.getProject ();
      String relativeSRCPath = ProjectUtils.getDefaultModuleSrcPath (project.getName ());
      IFolder srcPathRes = root.getFolder (new Path (relativeSRCPath));
      if(!srcPathRes.exists())
    	  return false;
      
      String absSrcPath = srcPathRes.getLocation ().toOSString ();

      if (aqlFilePath.startsWith (absSrcPath)
          && aqlFilePath.replace (absSrcPath + FILE_SEPERATOR, "").indexOf (FILE_SEPERATOR) == -1) { return true; }
      return false;
    } */

    private boolean isInterestedEP (IFile resource)
    {
      if (resource == null) return false;

      IProject iProject = resource.getProject ();
      return (resource.getParent ().equals (iProject) && resource.getName ().equals (ExtractionPlanIndexer.epFileName));
    }

    private boolean isIntrestedResource (IResource resource)
    {
      IProject project = resource.getProject ();
      boolean isModular = false;
      try {
        if (project.isOpen () && project.hasNature (Constants.PLUGIN_NATURE_ID)
            && ProjectUtils.isModularProject (project)) isModular = true;

      }
      catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }

      if ((isModular && resource instanceof IFolder && ProjectUtils.isIntrestedModuleFolder ((IFolder) resource))) { 
    	  return true; 
      }
      
      //Not checking if the resource is a text analytics source or bin folder. Rename of these directories would cause a 
      //change in the text analytics properties file too, which in turn would trigger a project re-index. So no need to
      //look out for this particular change.

      if (isModular && resource instanceof IFile
          && (isIntrestedAQL ((IFile) resource) || isInterestedEP ((IFile) resource))) { return true; }
      return false;
  
    }

    @Override
    public boolean visit (IResourceDelta delta) throws CoreException
    {
      IResource resource = delta.getResource ();

      /**
       * Handles all the events related to Project. The Deletion of Project is handled as part of PRE_DELETE EVENT.
       */
      if (resource instanceof IProject) {
        int flag = delta.getFlags ();
        int kind = delta.getKind ();
        IPath movedFromPath = delta.getMovedFromPath ();
        IPath movedToPath = delta.getMovedToPath ();
        IProject project = (IProject) resource;
        IResource iresource = null;
        if (movedToPath != null) iresource = ResourcesPlugin.getWorkspace ().getRoot ().findMember (movedToPath);

        ResourceAction resourceChangeType = null;
        if ((flag & IResourceDelta.OPEN) != 0 && kind == IResourceDelta.CHANGED && project.isOpen ()) {
          // Project is opened
          resourceChangeType = ResourceAction.OPEN;
          new TextAnalyticsResourceActionThread (project, resourceChangeType).start ();
          return false;
        }
        else if ((flag & IResourceDelta.OPEN) != 0 && kind == IResourceDelta.CHANGED && !project.isOpen ()) {
          // Project is closed
          resourceChangeType = ResourceAction.CLOSE;
          new TextAnalyticsResourceActionThread (project, resourceChangeType).start ();
          return false;
        }
        else if (kind == IResourceDelta.ADDED && movedFromPath == null && movedToPath == null
            && isIntrestedProject (project)) {
          // Project is Added or created
          resourceChangeType = ResourceAction.ADDED;
          new TextAnalyticsResourceActionThread (null, resource, null, resourceChangeType).start ();
          return false;
        }
        else if (kind == IResourceDelta.REMOVED && movedFromPath == null && movedToPath != null && iresource != null
            && iresource instanceof IProject && isIntrestedProject ((IProject) iresource)) {
          // Project is Renamed
          resourceChangeType = ResourceAction.RENAMED;
          new TextAnalyticsResourceActionThread (resource, (IProject) iresource, null, resourceChangeType).start ();
          return false;
        }
        else if (kind == IResourceDelta.REMOVED && movedFromPath == null && movedToPath == null && iresource != null
            && iresource instanceof IProject && isIntrestedProject ((IProject) iresource)) {
          // Project is deleted
          resourceChangeType = ResourceAction.DELETED;
          new TextAnalyticsResourceActionThread (resource, (IProject) iresource, null, resourceChangeType).start ();
          return false;
        }

      }

      /**
       * Handles event related to FILE or FOLDER.
       */
      switch (delta.getKind ()) {
        case IResourceDelta.ADDED:
          addedResource = resource;
          // ADD Event
          if (delta.getMovedFromPath () == null && isIntrestedResource (resource) && !hasRenameOccurred) {
            new TextAnalyticsResourceActionThread (null, addedResource, null).start ();
            return false;
          }

          break;
        case IResourceDelta.REMOVED:
          removedResource = resource;
          IResource iresource = ResourcesPlugin.getWorkspace ().getRoot ().findMember (delta.getMovedToPath ());
          // Rename Event or Move Event
          if (delta.getMovedToPath () != null) {
            if (isIntrestedResource (removedResource) || isIntrestedResource (iresource)) {
              new TextAnalyticsResourceActionThread (removedResource, iresource, null).start ();
              hasRenameOccurred = true;
              return false;
            }
          }
          else {
            // Delete Event
            if (isIntrestedResource (removedResource)) {
              new TextAnalyticsResourceActionThread (removedResource, null, null).start ();
              return false;
            }
          }
          break;
        case IResourceDelta.CHANGED:
          // Considered only for AQL Changes
          int flags = delta.getFlags ();
          if (resource instanceof IFile
            && ((flags & IResourceDelta.REPLACED) != 0 || (flags & IResourceDelta.CONTENT) != 0)
            && isIntrestedResource (resource)) {
            changedResource = delta.getResource ();
            new TextAnalyticsResourceActionThread (null, null, changedResource).start ();
            return false;
          }
          else if (resource instanceof IFile && Constants.TEXT_ANALYTICS_PREF_FILE.equals (resource.getName ())
            && (flags & IResourceDelta.CONTENT) != 0 && isIntrestedProject (resource.getProject ())) {
            new TextAnalyticsResourceActionThread (null, null, resource).start ();
            return false;

          }
        break;
      }

      if (addedResource == null || removedResource == null || changedResource == null) {
        return true;
      }
      else {
        return false;
      }
    }

  }
}
