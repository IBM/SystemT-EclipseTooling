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

package com.ibm.biginsights.textanalytics.resultdifferences.colldiff;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.ibm.biginsights.textanalytics.resultdifferences.Messages;
import com.ibm.biginsights.textanalytics.resultdifferences.compute.DifferencesComputer;
import com.ibm.biginsights.textanalytics.resultdifferences.filediff.FileDiffViewOpenListener;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AbstractResultComparator;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnalysisResultExplorer;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.ColumnSortListener;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.ColumnsAsBarPaintListener;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.ResultViewBarProvider;

/**
 * A common superclass for By-XY Views
 * 
 * 
 * 
 */
public abstract class CollDiffAbstractResultTree {



	/** Content Provider for the Viewer */
	private CollDiffAbstractResultContentProvider fAnalysisResultContentProvider;

	/** Sorter for the Viewer */
	private final AbstractResultComparator fCollDiffResultComparator = new CollDiffResultComparator();

	/** The TreeViewer used */
	private TreeViewer fTreeViewer;

	/** The filter that hides elements if they contain no annotations */
	private ViewerFilter fCollDiffHideElementsWithoutChangesFilter = new CollDiffHideElementsWithoutChangesFilter();

	/**
	 * Tracks whether fHideElementsWithoutAnnotationsFilter was added to the
	 * TreeViewer
	 */
	private boolean fFilterAdded = false;

	/**
	 * Constructor
	 * 
	 * @param toolkit
	 *            The FormToolkit to use for creating/adapting widgets
	 * @param parent
	 *            The parent composite
	 * @param explorer
	 *            The {@link AnalysisResultExplorer} that displays the STRF
	 *            hierarchy
	 */
	public CollDiffAbstractResultTree(final FormToolkit toolkit,
			final Composite parent, final AnalysisResultExplorer explorer,
			final FileDiffViewOpenListener openListener) {
		fTreeViewer = createTreeViewer(toolkit, parent, explorer);
		fTreeViewer.addOpenListener(openListener);
	}

	/**
	 * @param explorer
	 *            The {@link AnalysisResultExplorer} that provides the file
	 *            selection
	 * @return The ContentProvider that will be used in the TreeViewer
	 */
	protected abstract CollDiffAbstractResultContentProvider createContentProvider(
			final AnalysisResultExplorer explorer);

	/**
	 * Creates Resource, Annotations and Processing time columns on the given
	 * tree
	 * 
	 * @param treeViewer
	 *            The treeViewer to create columns for
	 */
	protected void createTreeColumns(final TreeViewer treeViewer) {
		final Tree tree = treeViewer.getTree();

		// The resource column stores path names to files
		final TreeColumn resourceColumn = new TreeColumn(tree, SWT.NULL);
		resourceColumn.setText(Messages.getString("AbstractResultTree_Resource")); //$NON-NLS-1$
		resourceColumn.setToolTipText(Messages.getString("AbstractResultTree_Resource")); //$NON-NLS-1$
		resourceColumn.setWidth(400);
		resourceColumn.setMoveable(true);

		// The column which shows the total changes (supposed to be sum so far)
		final TreeColumn totalColumn = new TreeColumn(tree, SWT.RIGHT);
		totalColumn.setText(Messages.getString("CollDiff_AbstractResultTree_TotalChanged")); //$NON-NLS-1$
		totalColumn
				.setToolTipText(Messages.getString("CollDiff_AbstractResultTree_TotalChanged")); //$NON-NLS-1$
		totalColumn.setWidth(100);
		totalColumn.setMoveable(true);

		// The column which shows the deleted annotations
		final TreeColumn deletedColumn = new TreeColumn(tree, SWT.RIGHT);
		deletedColumn.setText(Messages.getString("CollDiff_AbstractResultTree_Deleted")); //$NON-NLS-1$
		deletedColumn
				.setToolTipText(Messages.getString("CollDiff_AbstractResultTree_Deleted")); //$NON-NLS-1$
		deletedColumn.setWidth(100);
		deletedColumn.setMoveable(true);

		// The new column shows the number of new annotations
		final TreeColumn newColumn = new TreeColumn(tree, SWT.RIGHT);
		newColumn.setText(Messages.getString("CollDiff_AbstractResultTree_New")); //$NON-NLS-1$
		newColumn.setToolTipText(Messages.getString("CollDiff_AbstractResultTree_New")); //$NON-NLS-1$
		newColumn.setWidth(100);
		newColumn.setMoveable(true);

		// The changed column shows the number of new annotations
		final TreeColumn changedColumn = new TreeColumn(tree, SWT.RIGHT);
		changedColumn.setText(Messages.getString("CollDiff_AbstractResultTree_Changed")); //$NON-NLS-1$
		changedColumn
				.setToolTipText(Messages.getString("CollDiff_AbstractResultTree_Changed")); //$NON-NLS-1$
		changedColumn.setWidth(100);
		changedColumn.setMoveable(true);

//		// The changed column which shows the processing time
//		final TreeColumn timeColumn = new TreeColumn(tree, SWT.RIGHT);
//		timeColumn.setText(Messages.CollDiff_AbstractResultTree_ProcessingTime);
//		timeColumn
//				.setToolTipText(Messages.CollDiff_AbstractResultTree_ProcessingTime);
//		timeColumn.setWidth(100);
//		timeColumn.setMoveable(true);

		// Register a PaintListener that will paint bars for the Annotations and
		// Processing time columns
		final ColumnsAsBarPaintListener barCreator = new ColumnsAsBarPaintListener(
				new ResultViewBarProvider(new int[] { 1, 5 }));
		tree.addListener(SWT.PaintItem, barCreator);

		// sort by total changes by default
		fCollDiffResultComparator.setSortAttribute(
				CollDiffResultComparator.SORT_BY_TOTAL_COUNT, SWT.DOWN);
		treeViewer.setComparator(fCollDiffResultComparator);
		// Register listeners that take care of sorting
		resourceColumn.addListener(SWT.Selection,
				new ColumnSortListener(treeViewer,
						resourceColumn, fCollDiffResultComparator,
						CollDiffResultComparator.SORT_BY_RESOURCE_PATH));
		totalColumn.addListener(SWT.Selection,
				new ColumnSortListener(treeViewer, totalColumn,
						fCollDiffResultComparator,
						CollDiffResultComparator.SORT_BY_TOTAL_COUNT));
		deletedColumn.addListener(SWT.Selection,
				new ColumnSortListener(treeViewer, deletedColumn,
						fCollDiffResultComparator,
						CollDiffResultComparator.SORT_BY_DELETED_COUNT));
		newColumn.addListener(SWT.Selection,
				new ColumnSortListener(treeViewer, newColumn,
						fCollDiffResultComparator,
						CollDiffResultComparator.SORT_BY_NEW_COUNT));
		changedColumn.addListener(SWT.Selection,
				new ColumnSortListener(treeViewer, changedColumn,
						fCollDiffResultComparator,
						CollDiffResultComparator.SORT_BY_CHANGED_COUNT));
//		timeColumn.addListener(SWT.Selection,
//				new AnalysisResultColumnSortListener(treeViewer, timeColumn,
//						fCollDiffResultComparator,
//						CollDiffResultComparator.SORT_BY_PROCESSING_TIME));
	}

	/**
	 * Create the TreeViewer and set it up with ContentProvider and the like
	 * 
	 * @param toolkit
	 *            The FormToolkit used for adapting widgets
	 * @param parent
	 *            The parent composite
	 * @param explorer
	 *            The AnalysisResultExplorer that provides the file selection
	 * @return A ready-to-use TreeViewer
	 */
	private TreeViewer createTreeViewer(final FormToolkit toolkit,
			final Composite parent, final AnalysisResultExplorer explorer) {
		final TreeViewer treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION);
		treeViewer.setLabelProvider(new CollDiffLabelProvider());
		fAnalysisResultContentProvider = createContentProvider(explorer);
		treeViewer.setContentProvider(fAnalysisResultContentProvider);
		final Tree tree = treeViewer.getTree();
		tree.setLayoutData(new TableWrapData(TableWrapData.FILL,
				TableWrapData.FILL));
		toolkit.adapt(tree);
		tree.setHeaderVisible(true);
		createTreeColumns(treeViewer);
		return treeViewer;
	}

	/**
	 * @return The TreeViewer used to display results
	 */
	public TreeViewer getTreeViewer() {
		return fTreeViewer;
	}

	/**
	 * Switch to another AnalysisStoreReader (delegates to the ContentProvider
	 * of the TreeViewer)
	 * 
	 * @param analysisStoreReader
	 *            The new {@link IAnalysisStoreReader}
	 */
	public void setAnalysisStoreReader(
			final DifferencesComputer analysisStoreReader) {
		fAnalysisResultContentProvider
				.setAnalysisStoreReader(analysisStoreReader);
	}

	/**
	 * Control whether elements with zero annotations are shown
	 * 
	 * @param hide
	 *            <code>true</code> if elements without annotations should be
	 *            hidden, <code>false</code> otherwise
	 */
	public void setHideElementsWithoutChanges(boolean hide) {
		if (hide && !fFilterAdded) {
			fTreeViewer.addFilter(fCollDiffHideElementsWithoutChangesFilter);
			fFilterAdded = true;
		} else if (!hide && fFilterAdded) {
			fTreeViewer.removeFilter(fCollDiffHideElementsWithoutChangesFilter);
			fFilterAdded = false;
		}
	}
}
