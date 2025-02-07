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

package com.ibm.biginsights.textanalytics.resourcechange.action;

import org.eclipse.core.resources.IResource;

import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 *  Krishnamurthy
 */
public abstract class AbstractResourceChangeActionDelegate
{



  protected IResource removedResource;
  protected IResource addedResource;
  
  /**
   * Represents the type of resource change event, viz add, delete or rename
   * Can take up one of the following values:
   * 	Constants.RESCHNG_DELETE
   * 	Constants.RESCHNG_RENAME
   * 	Constants.RESCHNG_ADD
   */
  protected int resourceChangeType;

  public AbstractResourceChangeActionDelegate (IResource removedResource, IResource addedResource)
  {
    this.removedResource = removedResource;
    this.addedResource = addedResource;
  }

  public void run ()
  {
    // Detect action mode: deleteAction or renameAction
    if (addedResource == null && removedResource != null) {
      resourceChangeType = Constants.RESCHNG_DELETE;
    }else if(addedResource != null && removedResource == null){
    	resourceChangeType = Constants.RESCHNG_ADD;
    }else if(addedResource != null && removedResource != null){
    	if(addedResource.getParent().equals(removedResource.getParent())){
    		resourceChangeType = Constants.RESCHNG_RENAME;
    	}else{
    		resourceChangeType = Constants.RESCHNG_ADD;
    	}
    }
  }

  protected boolean validateResourceChangeEvent ()
  {
    if (removedResource == null && addedResource == null) { return false; }
    
    // this is the case for a resource added. we need to handle this since we need to handle copy of projects
    if (removedResource == null && addedResource != null) { return true; }

    // Validation 2: If addedResource and removedResource are not of same type, return
    if (addedResource != null && removedResource != null) {
      if (addedResource.getType () != removedResource.getType ()) { return false; }
    }

    return true;
  }

  protected abstract void handleRenameAction ();

  protected abstract void handleDeleteAction ();

  protected abstract void handleAddAction();
}
