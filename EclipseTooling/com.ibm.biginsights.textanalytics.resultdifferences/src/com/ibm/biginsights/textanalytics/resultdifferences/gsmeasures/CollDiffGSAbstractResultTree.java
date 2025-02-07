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

package com.ibm.biginsights.textanalytics.resultdifferences.gsmeasures;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

import com.ibm.biginsights.textanalytics.resultdifferences.Messages;
import com.ibm.biginsights.textanalytics.resultdifferences.compute.DifferencesComputer;
import com.ibm.biginsights.textanalytics.resultdifferences.filediff.FileDiffViewOpenListener;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnalysisResultExplorer;

/**
 * A common superclass for By-XY Views
 * 
 */
public abstract class CollDiffGSAbstractResultTree {



	/** Content Provider for the Viewer */
	private CollDiffGSAbstractResultContentProvider fAnalysisResultContentProvider;


	/** The TreeViewer used */
	private TreeViewer fTreeViewer;

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
	public CollDiffGSAbstractResultTree(final FormToolkit toolkit,
			final Composite parent, final AnalysisResultExplorer explorer,
			final FileDiffViewOpenListener openListener) {
		fTreeViewer = createTreeViewer(toolkit, parent, explorer);
	}

	/**
	 * @param explorer
	 *            The {@link AnalysisResultExplorer} that provides the file
	 *            selection
	 * @return The ContentProvider that will be used in the TreeViewer
	 */
	protected abstract CollDiffGSAbstractResultContentProvider createContentProvider(
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

		// The column which shows the deleted annotations
		final TreeColumn deletedColumn = new TreeColumn(tree, SWT.RIGHT);
		deletedColumn.setText(Messages.getString("CollDiff_GS_Precision")); //$NON-NLS-1$
		deletedColumn
				.setToolTipText(Messages.getString("CollDiff_GS_Precision")); //$NON-NLS-1$
		deletedColumn.setWidth(100);
		deletedColumn.setMoveable(true);

		// The new column shows the number of new annotations
		final TreeColumn newColumn = new TreeColumn(tree, SWT.RIGHT);
		newColumn.setText(Messages.getString("CollDiff_GS_Recall")); //$NON-NLS-1$
		newColumn.setToolTipText(Messages.getString("CollDiff_GS_Recall")); //$NON-NLS-1$
		newColumn.setWidth(100);
		newColumn.setMoveable(true);

		// The changed column shows the number of new annotations
		final TreeColumn changedColumn = new TreeColumn(tree, SWT.RIGHT);
		changedColumn.setText(Messages.getString("CollDiff_GS_FMeasure")); //$NON-NLS-1$
		changedColumn
				.setToolTipText(Messages.getString("CollDiff_GS_FMeasure")); //$NON-NLS-1$
		changedColumn.setWidth(100);
		changedColumn.setMoveable(true);
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
		treeViewer.setLabelProvider(new CollDiffGSLabelProvider());
		fAnalysisResultContentProvider = createContentProvider(explorer);
		treeViewer.setContentProvider(fAnalysisResultContentProvider);
		//fTreeViewer.setAutoExpandLevel(2);
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
}
