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
package com.ibm.biginsights.textanalytics.resourcechange.listeners;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.resourcechange.action.ResourceChangeActionDelegate;

/**
 * 
 */
public class WorkspaceResourceChangeListener implements IResourceChangeListener
{



	//DeltaVisitor deltaVisitor = ;
  protected IResource addedResource;
  protected IResource removedResource;
  protected boolean hasRenameOccurred = false;
  protected boolean bTextAnalyticsProject = false;
  protected boolean bDeleteEvent = false;

  /*
   * Keep this method as simple as possible. It is recommended to exit from this method as soon as possible, as there
   * may be several events fired, resulting in several invocations of this method. Do not perform resource intensive
   * tasks. Create a new ResourceChangeActionThread and leave it to the new thread to process the event. Doing resource
   * intensive activities in this method may lead to blocking of SWT thread, resulting in hung eclipse workbench.
   * IMPORTANT NOTE: We are interested only in rename or delete events of Text Analytics project. The resourceChanged()
   * method will be invoked twice for every deletion or rename. Once for PRE_DELETE event and once for POST_CHANGE
   * event. A rename event is just a combination of REMOVE and ADD events. We make use of the PRE_DELETE event to check
   * whether the resource is of Text Analytics nature. We can't do this check anywhere else in the code, because if the
   * deleted resource is a Text Analytics project, then by the time POST_CHANGE event is invoked, the project is already
   * deleted and hence can't query the nature of project.
   */
  @Override
  public void resourceChanged (IResourceChangeEvent event)
  {
    switch (event.getType ()) {
      case IResourceChangeEvent.PRE_DELETE:
        bDeleteEvent = true;
        try {
          bTextAnalyticsProject = false;
          IProject project = event.getResource ().getProject ();
          if (project.hasNature (Activator.NATURE_ID)) {
            bTextAnalyticsProject = true;
          }
        }
        catch (CoreException e1) {
          // do nothing
        }
      break;
      case IResourceChangeEvent.POST_CHANGE:
		try {
			addedResource = null;
			removedResource = null;
			hasRenameOccurred = false;
			event.getDelta().accept(new DeltaVisitor());    
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			bTextAnalyticsProject = false;
			bDeleteEvent = false;
		}
		break;
    }
  }

  private boolean isTextAnalyticsNature ()
  {
    if (bDeleteEvent) {
      return bTextAnalyticsProject;
    }
    else {// it's a rename or add event
      try {
        IProject project = (removedResource != null) ? removedResource.getProject () : addedResource.getProject ();
        if (project.hasNature (Activator.NATURE_ID)) { return true; }
      }
      catch (Exception e) {
        return false;
      }
    }
    return false;
  }

  private boolean unInterestedEvents ()
  {
    if (removedResource == null && addedResource == null) {
      // invalid event. So, not interested
      return true;
    }
    // else if(removedResource == null && addedResource != null){
    // //addition event. So, not interested.
    // return true;
    // }
    else
      return !isTextAnalyticsNature ();
  }

  private class DeltaVisitor implements IResourceDeltaVisitor
  {

	@Override
    public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		
		switch (delta.getKind()) {
//		case IResourceDelta.CHANGED:
//			if(delta.getResource().getName().equals(".textanalytics"))
//			{
//				System.out.println("display message here once after multiple events");
//            	
//			}
//			return true;
		case IResourceDelta.ADDED:
        		addedResource = resource;
        		//if this is not handled on rename then it is a copy or add event
        		if(!hasRenameOccurred)
        		{
        			new ResourceChangeActionDelegate(null, addedResource).run();
        		}
                return false;
            case IResourceDelta.REMOVED:                
            	removedResource = resource;
            	//IResource p = (IResource) delta.getMovedToPath();
            	IResource iresource= ResourcesPlugin.getWorkspace().getRoot().findMember(delta.getMovedToPath());
            		//rename event
            	if (delta.getMovedToPath() != null){
            		if(!unInterestedEvents()){
                        new ResourceChangeActionDelegate(removedResource, iresource).run();
                        hasRenameOccurred=true;
                        }
            	}
            	else
            	{
            		//delete event
            		if(!unInterestedEvents()){
                        new ResourceChangeActionDelegate(removedResource, null).run();
                        }
            	}
				return false;
            
           }
		
		return true; // visit the children
    	}
	
	}
}
