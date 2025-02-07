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
package com.ibm.biginsights.textanalytics.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.FileEditorInput;

/**
 * This class is used to open an AQL file containing a given view and highlight the view.
 * Initially an instance of this class contains only project name and view name. At initialization
 * step of AQL editor ( init (IEditorSite site, IEditorInput input) ) the following information
 * are populated:
 * - AQL file containing the view.
 * - Line number , offset, and length of the view.
 * Most methods are just wrapper of FileEditorInput when the AQL file is found.
 */
public class ViewEditorInput implements IFileEditorInput, IEditorInput
{


 
	private String projectName;
  private String viewName;
  private String mainViewName;

  private int offset = 0;
  private int line = 1;
  private int length = 0;

  FileEditorInput fileEditorInput;


  public ViewEditorInput (String projectName, String viewName, String mainViewName)
  {
    super ();
    this.projectName = projectName;
    this.viewName = viewName;
    this.mainViewName = mainViewName;
  }

  @Override
  public IStorage getStorage () throws CoreException
  {
    if (fileEditorInput != null)
      return fileEditorInput.getStorage ();
    else
      return null;
  }

  @Override
  public boolean exists ()
  {
    if (fileEditorInput != null)
      return fileEditorInput.exists ();
    else
      return false;
  }

  @Override
  public ImageDescriptor getImageDescriptor ()
  {
    if (fileEditorInput != null)
      return fileEditorInput.getImageDescriptor ();
    else
      return null;
  }

  @Override
  public String getName ()
  {
    if (fileEditorInput != null)
      return fileEditorInput.getName ();
    else
      return "";
  }

  @Override
  public IPersistableElement getPersistable ()
  {
    if (fileEditorInput != null)
      return fileEditorInput.getPersistable ();
    else
      return null;
  }

  @Override
  public String getToolTipText ()
  {
    if (fileEditorInput != null)
      return fileEditorInput.getToolTipText ();
    else
      return null;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter (Class adapter)
  {
    if (fileEditorInput != null)
      return fileEditorInput.getAdapter (adapter);
    else
      return null;
  }

  @Override
  public IFile getFile ()
  {
    if (fileEditorInput != null)
      return fileEditorInput.getFile ();
    else
      return null;
  }

  public String getProjectName ()
  {
    return projectName;
  }

  public void setProjectName (String projectName)
  {
    this.projectName = projectName;
  }

  public String getViewName ()
  {
    return viewName;
  }

  public void setViewName (String viewName)
  {
    this.viewName = viewName;
  }

  public String getMainViewName ()
  {
    return mainViewName;
  }

  public void setMainViewName (String mainViewName)
  {
    this.mainViewName = mainViewName;
  }

  public FileEditorInput getFileEditorInput ()
  {
    return fileEditorInput;
  }

  public void setFileEditorInput (FileEditorInput fileEditorInput)
  {
    this.fileEditorInput = fileEditorInput;
  }

  public int getOffset ()
  {
    return offset;
  }

  public void setOffset (int offset)
  {
    this.offset = offset;
  }

  public int getLine ()
  {
    return line;
  }

  public void setLine (int line)
  {
    this.line = line;
  }

  public int getLength ()
  {
    return length;
  }

  public void setLength (int length)
  {
    this.length = length;
  }

}
