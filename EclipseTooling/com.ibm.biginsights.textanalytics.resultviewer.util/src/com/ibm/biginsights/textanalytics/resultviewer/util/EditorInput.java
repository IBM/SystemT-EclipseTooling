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
package com.ibm.biginsights.textanalytics.resultviewer.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;

public class EditorInput implements IFileEditorInput {



  private final FileEditorInput fileEditorInput;
  
  private final String name;
  
  private String selectedAnnotationType = null;
  
  private  String currentProjectReference = null; 

public String getSelectedAnnotationType() {
	return selectedAnnotationType;
}

  
  /**
   * Added by Jayatheerthan Krishnamurthy
   * This field holds any additional data that the consumer would like to set.
   * 
   */
  private Object userData;
    
  private final int aqlTextID;
  
  public int getAqlTextID() {
    return this.aqlTextID;
  }

  private SystemTComputationResult model;
  
  public EditorInput(IFile file, int id, String name, SystemTComputationResult model,String opViewDotAttributeName) {
    super();
    this.aqlTextID = id;
    this.name = name;
    this.model = model;
    this.fileEditorInput = new FileEditorInput(file);
    selectedAnnotationType = opViewDotAttributeName;
  }
  
  public SystemTComputationResult getModel() {
    return this.model;
  }
  
  public void dispose() {
    this.model = null;
  }
   
  @Override
  public boolean exists() {
    return false;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    return this.fileEditorInput.getImageDescriptor();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public IPersistableElement getPersistable() {
    // Editor can't be persisted
    return null;
  }

  @Override
  public String getToolTipText() {
    return getName();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter(Class adapter) {
    return this.fileEditorInput.getAdapter(adapter);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof EditorInput) {
      return this.aqlTextID == ((EditorInput) obj).aqlTextID;
    }
    return false;
  }

  @Override
  public IFile getFile() {
    return this.fileEditorInput.getFile();
  }

  @Override
  public IStorage getStorage() {
    return this.fileEditorInput.getStorage();
  }
  

  public Object getUserData() {
  	return userData;
  }

  public void setUserData(Object userData) {
  	this.userData = userData;
  }

public void setCurrentProjectReference(String currentProjectReference) {
	this.currentProjectReference = currentProjectReference;
}

public String getCurrentProjectReference() {
	return currentProjectReference;
}

    
   
}
