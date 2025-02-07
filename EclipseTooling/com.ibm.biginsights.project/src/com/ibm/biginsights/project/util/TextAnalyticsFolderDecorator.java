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

package com.ibm.biginsights.project.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * Folder decorator - to identify the Module Source folder and Module folder
 * 
 *  Simon
 */
public class TextAnalyticsFolderDecorator extends LabelProvider implements ILabelDecorator
{

  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  @SuppressWarnings("unused")
  private static final String PARSEERROR_TYPE = "com.ibm.biginsights.textanalytics.aqleditor.parseerror";
  private static final String COMPILEERROR_TYPE = "com.ibm.biginsights.textanalytics.aqleditor.compileerror";

  // Icon for the text analytics source folder
  private static final Image IMG_SRC_FOLDER = Activator.getImageDescriptor ("/icons/ModuleSrc.png").createImage (); //$NON-NLS-1$

  // Icon for module folder that contains at least one AQL file
  private static final Image IMG_NON_EMPTY_MODULE = Activator.getImageDescriptor ("/icons/Module.png").createImage (); //$NON-NLS-1$

  // Icon for module folder that does not contain any AQL files
  private static final Image IMG_EMPTY_MODULE = Activator.getImageDescriptor ("/icons/ModulewithNoAQL.png").createImage (); //$NON-NLS-1$

  public TextAnalyticsFolderDecorator ()
  {
    super ();
  }

  /**
   * Decorates a given folder with a suitable image. The icon to decorate with is determined based on whether the folder
   * contains any errors or not. The following folders are handled:
   * <ol>
   * <li>$project/textAnalytics/$src</li>
   * <li>$project/textAnalytics/$src/$module</li>
   * </ol>
   */
  @Override
  public Image decorateImage (Image image, Object object)
  {

    if (false == isValidForDecoration (object)) { return null; }

    // once validation passes through, it is guaranteed that the object is an instance of IFolder
    IFolder folderToDecorate = (IFolder) object;

    // process source folder
    if (true == ProjectUtils.isConfiguredSrcFolder (folderToDecorate)) {
      return decorateSourceFolder (folderToDecorate);
    }

    // process module folder
    else if (true == ProjectUtils.isModuleFolder (folderToDecorate)) { return decorateModuleFolder (folderToDecorate); }

    // Return null to indicate no decoration
    return null;
  }

  private Image decorateModuleFolder (IFolder folderToDecorate)
  {
    try {
      if (true == isEmptyModule (folderToDecorate)) {
        return IMG_EMPTY_MODULE;
      }
      else {
        if (folderContainsErrors (folderToDecorate)) {
          return getErrorImageForModuleFolder ();
        }
        else {
          return IMG_NON_EMPTY_MODULE;
        }
      }
    }
    catch (CoreException e) {
      return null;
    }
  }

  private boolean isEmptyModule (IFolder folderToDecorate) throws CoreException
  {
    IResource[] resources = folderToDecorate.members ();
    for (IResource resource : resources) {
      if (resource instanceof IFile && Constants.AQL_FILE_EXTENSION_STRING.equals (resource.getFileExtension ())) { return false; }
    }

    return true;
  }

  private Image decorateSourceFolder (IFolder folderToDecorate)
  {
    // Return an error icon only if the bin folder exists && folder contains errors
    if (true == isBinLocPresent (folderToDecorate) && true == folderContainsErrors (folderToDecorate)) {
      return getErrorImageForSrcFolder ();
    }
    // If the bin folder is not present then return a normal src folder.
    else {
      return IMG_SRC_FOLDER;
    }
  }

  /**
   * Checks if the configured bin location of the project exists.
   * 
   * @param folderToDecorate the IResource that is being considered to be decorated.
   * @return true if bin location found else false.
   */
  private boolean isBinLocPresent (IFolder folderToDecorate)
  {
    IProject project = folderToDecorate.getProject ();
    IFolder binFolder = ProjectUtils.getConfiguredModuleBinDir (project);
    return binFolder != null && binFolder.exists ();
  }

  private Image getErrorImageForSrcFolder ()
  {
    ImageDescriptor descriptor = JFaceResources.getImageRegistry ().getDescriptor (
      "org.eclipse.jface.fieldassist.IMG_DEC_FIELD_ERROR");//$NON-NLS-1$
    DecorationOverlayIcon ovrlImageDescriptor = null;
    ovrlImageDescriptor = new DecorationOverlayIcon (IMG_SRC_FOLDER, descriptor, IDecoration.BOTTOM_LEFT);
    return ovrlImageDescriptor.createImage ();
  }

  private Image getErrorImageForModuleFolder ()
  {
    ImageDescriptor descriptor = JFaceResources.getImageRegistry ().getDescriptor (
      "org.eclipse.jface.fieldassist.IMG_DEC_FIELD_ERROR");//$NON-NLS-1$
    DecorationOverlayIcon ovrlImageDescriptor = null;
    ovrlImageDescriptor = new DecorationOverlayIcon (IMG_NON_EMPTY_MODULE, descriptor, IDecoration.BOTTOM_LEFT);
    return ovrlImageDescriptor.createImage ();
  }

  /**
   * Determines if the given object is valid and should be decorated. The conditions that determine object decoration
   * are:
   * <ol>
   * <li>The project must be of BigInsights nature</li>
   * <li>The project must be modular</li>
   * <li>The object must be either a textAnalytics src folder (or) a module src folder</li>
   * <li>Add more...</li>
   * </ol>
   * 
   * @param object The object for which validation of decoration is sought.
   * @return <code>true</code> if the specified object should be decorated; <code>false<code> otherwise.
   */
  private boolean isValidForDecoration (Object object)
  {
    // validation #1: Ensure that the object is not null and is a folder
    if (object == null || false == object instanceof IFolder) { return false; }

    // validation #2: Ensure that the project is of BigInsights nature
    IFolder folder = ((IFolder) object);
    IProject project = folder.getProject ();
    try {
      if (false == project.hasNature (Constants.PLUGIN_NATURE_ID)) { return false; }
    }
    catch (CoreException e) {
      return false;
    }

    // validation #3: Ensure that the project is modular
    if (false == ProjectUtils.isModularProject (project)) { return false; }

    // all validations have passed
    return true;
  }

  @Override
  public String decorateText (String label, Object object)
  {
    // return null to specify no decoration
    return null;
  }

  /**
   * Determines if the folder must be marked with an error icon, by traversing through all error markers
   * 
   * @param folder
   * @return true if there are errors on the folder, else false.
   */
  private boolean folderContainsErrors (IFolder folder)
  {
    int depth = IResource.DEPTH_INFINITE;

    try {
      if (folder.findMaxProblemSeverity (COMPILEERROR_TYPE, true, depth) == IMarker.SEVERITY_ERROR) { return true; }
    }
    catch (CoreException e) {
      Activator.getDefault ().getLog ().log (new Status (IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage ()));
    }
    return false;
  }

}
