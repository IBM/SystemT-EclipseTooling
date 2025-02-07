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
package com.ibm.biginsights.textanalytics.workflow.plan.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.tasks.ExtractionTasksView;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * action to open a document collection in the extraction tasks view
 * 
 * 
 */
public class OpenCollectionAction extends Action
{



  public OpenCollectionAction ()
  {
    setImageDescriptor (ImageDescriptor.createFromImage (Icons.OPEN_ICON));
    setText (Messages.open_collection_text);
    setToolTipText (Messages.open_collection_tootltip);
  }

  /**
   * makes sure that the Extraction Plan View is opened and try to load a new collection through a wizard
   */
  @Override
  public void run ()
  {
    IWorkbenchWindow window = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ();
    IWorkbenchPage page = window.getActivePage ();
    ExtractionTasksView t_view = (ExtractionTasksView) page.findView (ExtractionTasksView.ID);
    if (t_view != null) {
      page.hideView (t_view);
    }
    try {
      t_view = (ExtractionTasksView) page.showView (ExtractionTasksView.ID);
      if (t_view != null) t_view.showCollectionDialog ();
    }
    catch (PartInitException e) {
      e.printStackTrace ();
    }
  }

}
