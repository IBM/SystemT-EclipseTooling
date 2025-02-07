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
package com.ibm.biginsights.textanalytics.treeview.view;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * <Mrudula>This code has been taken from  package org.eclipse.pde.internal.ui.launcher and modified slightly ;
 * This is available under EPL - Eclipse Public License
 * To make it a public class to suit the requirements of TA, the following changes are done
 * 1) made public 2) expandAll() changed to expandToLevel(3)
 * </Mrudula>
 * A FilteredChecboxTree.  This tree stores all the tree elements internally, and keeps the
 * check state in sync.  This way, even if an element is filtered, the caller can get and set the
 * checked state.  
 * 
 * The internal representation is additive.  That is, elements are never removed from the internal
 * representation.  This is OK since the PDE launch Dialog never changes the elements once
 * the view is opened.  If any other tree is based on this code, they may want to address this issue. 
 * 
 * This is not public because it was customized for the Launch Dialog.
 * 
 */
public class FilteredCheckboxTree extends FilteredTree {



	private WorkbenchJob refreshJob;

	/**
	 * The FilteredCheckboxTree Constructor.
	 * @param parent The parent composite where this Tree will be placed.
	 * @param treeStyle Tree styles
	 * @param filter The pattern filter that will be used to filter elements
	 */
	public FilteredCheckboxTree(Composite parent, int treeStyle, PatternFilter filter) {
		super(parent, treeStyle, filter);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredTree#doCreateTreeViewer(org.eclipse.swt.widgets.Composite, int)
	 */
	protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
		//return new FilterableCheckboxTreeViewer(parent, style);
		
		return new CheckboxTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredTree#doCreateRefreshJob()
	 */
	protected WorkbenchJob doCreateRefreshJob() {
		// Since refresh job is private, we have to get a handle to it
		// when it is created, and store it locally.  
		// 
		// See: 218903: [Viewers] support extensibility of the refresh job in FilteredTree
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=218903
		WorkbenchJob job = super.doCreateRefreshJob();
		refreshJob = job;
		return job;
	}

	/**
	 * Resets the filter and returns when the refresh is complete
	 */
	public void resetFilter() {
		// Set the next to the initial Text, stop any outstanding jobs
		// and call the refresh job to run synchronously.
		getFilterControl().setText(this.initialText);
		refreshJob.cancel();
		refreshJob.runInUIThread(new NullProgressMonitor());
	}

	/**
	 * Get the number of pixels the tree viewer is from the top of the filtered 
	 * checkbox tree viewer.  This is  useful if you wish to align buttons with the
	 * tree.
	 * @return the offset of the Tree from the top of the container
	 */
	int getTreeLocationOffset() {
		GridLayout layout = (GridLayout) getLayout();
		return layout.horizontalSpacing + layout.marginTop + ((GridData) getLayoutData()).verticalIndent + getFilterControl().getSize().y + 1;
	}

}
 
