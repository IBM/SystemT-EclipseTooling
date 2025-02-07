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
package com.ibm.biginsights.textanalytics.resultdifferences.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Listener for the event that a table column header is clicked and sort order
 * should be changed
 * 
 * 
 * 
 */
public class ColumnSortListener implements Listener {



	/** The underlying Tree */
	private final Tree fTree;

	/** The TreeViewer on top of the Tree */
	private final TreeViewer fTreeViewer;

	/** The current sort column */
	private final TreeColumn fColumn;

	/** The comparator that does the actual sorting */
	private final AbstractResultComparator fComparator;

	/** The field used for sorting */
	private final int fSortField;

	/**
	 * Constructor
	 * 
	 * @param treeViewer
	 *            The TreeViewer on top of the Tree
	 * @param column
	 *            The current sort column
	 * @param comparator
	 *            The comparator that does the actual sorting
	 * @param sortField
	 *            The field used for sorting
	 */
	public ColumnSortListener(final TreeViewer treeViewer,
			final TreeColumn column,
			final AbstractResultComparator comparator, final int sortField) {
		fTreeViewer = treeViewer;
		fTree = treeViewer.getTree();
		fComparator = comparator;
		fSortField = sortField;
		fColumn = column;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(final Event e) {
		if (fTree.getSortDirection() == SWT.UP) {
			changeSortDirection(SWT.DOWN);
		} else {
			changeSortDirection(SWT.UP);
		}
	}

	/**
	 * Change the sort direction and refresh the viewer
	 * 
	 * @param sortDirection
	 *            the new sort direction, either <code>SWT.UP</code> or
	 *            <code>SWT.DOWN</code>
	 * @see SWT#UP
	 * @see SWT#DOWN
	 */
	protected void changeSortDirection(final int sortDirection) {
		Assert.isLegal(sortDirection == SWT.DOWN || sortDirection == SWT.UP);
		fTree.setSortColumn(fColumn);
		fTree.setSortDirection(sortDirection);
		fComparator.setSortAttribute(fSortField, sortDirection);
		fTreeViewer.refresh();
	}

}
