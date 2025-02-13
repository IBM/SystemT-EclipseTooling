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
package com.ibm.biginsights.textanalytics.concordance.ui;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry;

/**
 * Handler for the "open span in text editor" toolbar button of the annotation explorer.
 */
public class OpenTextEditorForSpanHandler extends AbstractHandler implements
    ISelectionChangedListener, IPartListener {

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

	private static final String AE_VIEW_ID = "com.ibm.biginsights.textanalytics.concordance.view";

	// Enabled status, updated on selection events in the annotation explorer
  private boolean enabled = false;

  private boolean init = true;

  private ISelection currentSelection = null;

  private ConcordanceView cv = null;

  public OpenTextEditorForSpanHandler() {
    super();
  }

  @Override
  public Object execute(ExecutionEvent event) {
    // We can't use the selection of the event since this is the selection of the concordance view.
    // What we need is the selection of the table viewer inside the concordance view.
    if (this.currentSelection != null) {
      if (this.currentSelection instanceof IStructuredSelection) {
        IStructuredSelection ssel = (IStructuredSelection) this.currentSelection;
        if (ssel.size() > 0 && ssel.getFirstElement() instanceof IConcordanceModelEntry) {
          this.cv.showEditorForEntry((IConcordanceModelEntry) ssel.getFirstElement());
        }
      }
    }
    // Return null required
    return null;
  }

  @Override
  public boolean isEnabled() {
    if (this.init) {
      this.init = false;

      IWorkbenchWindow wb = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

      // Add selection listener to annotation explorer if it's not done yet.
      // Adding listener at partOpened() is not always working because this handler can be instantiated when
      // annotation explorer is already opened -> partOpened() not called.
      if (this.cv == null) {
        IViewPart aeView = wb.getActivePage ().findView (AE_VIEW_ID);
        if (aeView != null) {
          this.cv = (ConcordanceView) aeView;
          this.cv.addSelectionListener(this);
        }
      }

      IPartService ps = (IPartService) wb.getService(IPartService.class);
      ps.addPartListener(this);
    }
    return this.enabled;
  }

  @Override
  public void dispose() {
    super.dispose();
    if (this.cv != null) {
      this.cv.removeSelectionChangedListener(this);
    }
  }

  @Override
  public void selectionChanged(SelectionChangedEvent event) {
    boolean old = this.enabled;
    this.enabled = false;
    ISelection selection = event.getSelection();
    if (selection != null) {
      if (selection instanceof IStructuredSelection) {
        IStructuredSelection ssel = (IStructuredSelection) selection;
        this.currentSelection = selection;
        this.enabled = (ssel.size() > 0 && (ssel.getFirstElement() instanceof IConcordanceModelEntry));
      }
    }
    if (old != this.enabled) {
      fireHandlerChanged(new HandlerEvent(this, true, false));
    }
  }

  @Override
  public void partOpened(IWorkbenchPart part) {
    if (part instanceof ConcordanceView) {
      this.cv = (ConcordanceView) part;
      this.cv.addSelectionListener(this);
    }
  }

  @Override
  public void partClosed(IWorkbenchPart part) {
    if (part instanceof ConcordanceView) {
       if(this.cv != null){
    	 this.cv.removeSelectionChangedListener(this);
       }
      this.cv = null;
      this.enabled = false;
      fireHandlerChanged(new HandlerEvent(this, true, false));
    }
  }

  @Override
  public void partActivated(IWorkbenchPart part) {
    // do nothing
  }

  @Override
  public void partBroughtToTop(IWorkbenchPart part) {
    // do nothing
  }

  @Override
  public void partDeactivated(IWorkbenchPart part) {
    // do nothing
  }

}
