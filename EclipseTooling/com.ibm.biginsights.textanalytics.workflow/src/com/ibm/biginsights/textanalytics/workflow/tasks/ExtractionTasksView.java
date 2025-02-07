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
package com.ibm.biginsights.textanalytics.workflow.tasks;

import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class ExtractionTasksView extends ViewPart {

  @SuppressWarnings("unused")


	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.ibm.biginsights.textanalytics.workflow.views.ExtractionTasksView";

	private ExtractionTasksPanel viewer;

	/**
	 * The constructor.
	 */
	public ExtractionTasksView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new ExtractionTasksPanel(parent, SWT.NONE);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.extraction_tasks_view");//$NON-NLS-1$
	}

	/**
	 * 
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ExtractionTasksView.this.fillContextMenu(manager);
			}
		});
	}

	/**
	 * 
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * 
	 * @param manager
	 */
	private void fillLocalPullDown(IMenuManager manager) {
		//
	}

	/**
	 * 
	 * @param manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {

	}

	/**
	 * 
	 */
	private void makeActions() {

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.setFocus();
	}

	public void refresh(String filePath, String langCode) {
	  if (viewer != null)
	    viewer.refresh(filePath, langCode);
	}

	public void reset() {
		viewer.reset();
	}

	public List<String> getSelectedFiles() {
		return viewer.getSelectedFiles();
	}

	public void showCollectionDialog() {
		viewer.showCollectionDialog();
	}
	
	public void setExpanded(int section, boolean expanded){
		viewer.setExpanded(section, expanded);
	}
}
