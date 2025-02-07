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

package com.ibm.biginsights.textanalytics.refactor.core;

import org.eclipse.core.resources.IFile;

import com.ibm.biginsights.textanalytics.indexer.types.ElementType;



public class AQLElementRenameInfo {



  // the offset of the AQLElement to be renamed in the file
  private int offset;
  // the new name for the AQLElement
  private String newName;
  // the old name of the AQLElement (as selected by the user)
  private String oldName;
  // the file that contains the AQLElement to be renamed
  private IFile sourceFile;

  private boolean updateProject;
  
  private boolean updateWorkspace;
  
  private String project;
  
  private String module;
  
  private ElementType eleType;
  
  public int getOffset() {
    return offset;
  }
  
  public void setOffset( final int offset ) {
    this.offset = offset;
  }

  public String getNewName() {
    return newName;
  }

  public void setNewName( final String newName ) {
    this.newName = newName;
  }

  public String getOldName() {
    return oldName;
  }

  public void setOldName( final String oldName ) {
    this.oldName = oldName;
  }

  public IFile getSourceFile() {
    return sourceFile;
  }

  public void setSourceFile( final IFile sourceFile ) {
    this.sourceFile = sourceFile;
  }

  public boolean isUpdateWorkspace() {
    return updateWorkspace;
  }

  public void setUpdateWorkspace( final boolean allProjects ) {
    this.updateWorkspace = allProjects;
  }

  public boolean isUpdateProject() {
    return updateProject;
  }

  public void setUpdateProject( final boolean updateBundle ) {
    this.updateProject = updateBundle;
  }

  public String getProject ()
  {
    return project;
  }

  public void setProject (String project)
  {
    this.project = project;
  }

  public String getModule ()
  {
    return module;
  }

  public void setModule (String module)
  {
    this.module = module;
  }

  public ElementType getEleType ()
  {
    return eleType;
  }

  public void setEleType (ElementType eleType)
  {
    this.eleType = eleType;
  }

}
