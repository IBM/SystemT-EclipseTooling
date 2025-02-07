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
package com.ibm.biginsights.textanalytics.tableview.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.tableview.view.AQLResultView;

public class OpenTextEditorForSpanHandler extends    AbstractHandler
                                          implements ISelectionChangedListener, IPartListener2
{


 
	private boolean enabled = false;
  private boolean init = true;
  private IStructuredSelection currentSelection = null;
  private AQLResultView aqlResultView = null;

  @Override
  public Object execute (ExecutionEvent event) throws ExecutionException
  {
    if (currentSelection != null) {
      aqlResultView.getCellMouseListener ().showEditorForEntry();
    }

    return null;
  }

  @Override
  public boolean isEnabled() {
    if (this.init) {
      this.init = false;
      IPartService ps = (IPartService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
          .getService(IPartService.class);
      ps.addPartListener(this);
    }

    return this.enabled;
  }

  @Override
  public void selectionChanged (SelectionChangedEvent event)
  {
    boolean old_enabled = this.enabled;
    enabled = false;
    currentSelection = null;

    ISelection selection = event.getSelection();
    if (selection != null && selection instanceof IStructuredSelection) {
      currentSelection = (IStructuredSelection) selection;

      if (currentSelection.size () > 0)
        enabled = true;
    }

    if (old_enabled != enabled)
      fireHandlerChanged(new HandlerEvent(this, true, false));
  }


  /*------------------  IPartListener2  ------------------*/

  @Override
  public void partOpened (IWorkbenchPartReference partRef)
  {
    IWorkbenchPart part = partRef.getPart (true);
    if (part instanceof AQLResultView) {
      aqlResultView = (AQLResultView) part;
      if(aqlResultView.getTableViewer () != null)
    	  aqlResultView.getTableViewer ().addSelectionChangedListener(this);
    }
  }

  @Override
  public void partClosed (IWorkbenchPartReference partRef)
  {
    IWorkbenchPart part = partRef.getPart (true);
    if (part == aqlResultView) {
    	 if(aqlResultView.getTableViewer () != null)
    		 aqlResultView.getTableViewer ().removeSelectionChangedListener(this);
      aqlResultView = null;
    }
  }

  @Override
  public void partActivated (IWorkbenchPartReference partRef)
  {}

  @Override
  public void partBroughtToTop (IWorkbenchPartReference partRef)
  {}

  @Override
  public void partDeactivated (IWorkbenchPartReference partRef)
  {}

  @Override
  public void partHidden (IWorkbenchPartReference partRef)
  {}

  @Override
  public void partInputChanged (IWorkbenchPartReference partRef)
  {}

  @Override
  public void partVisible (IWorkbenchPartReference partRef)
  {}

}
