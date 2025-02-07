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
package com.ibm.biginsights.textanalytics.tableview.model.impl;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.ibm.biginsights.textanalytics.resultviewer.model.BoolVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.FloatVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.IntVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.tableview.model.IAQLTableViewModel;
import com.ibm.biginsights.textanalytics.tableview.model.IRow;
import com.ibm.biginsights.textanalytics.util.common.Constants;

// Some notes on the implementation.  This takes a somewhat unusual approach in that the sorting
// is actually done in the model.  We're doing this so we have finer control over the sorting.
// In Eclipse, you can only provide a comparator, while Eclipse is doing the actual sorting.  However,
// as we would really like to have the benefits of a secondary sort order, this makes comparing rows
// with the Eclipse approach very difficult.  Conceptually, our approach is quite simple.  When a
// user clicks on a column header, we resort the rows according to the content of the column, but
// leaving the previous order intact as much as possible.  Standard example: the user sorts by some
// span column.  This will sort by span extent, but with complete disregard to the input document.
// When the user now sorts by input document, the rows are grouped by document, but the sorting
// with respect to the span column remains intact. All we need for this to work is a
// stable sort algorithm, i.e., an algorithm that will not reorder elements that don't need to be
// reordered (because they are equal).  When we use the Eclipse sorting, we don't have control over
// the sorting itself, only the comparison.  So we do the sorting ourselves, arrange everything
// into an absolute order where no two elements are equal, and let Eclipse work on that.
//
// Note that this remains stable across multiple columns, not just two.  Suppose you have a view
// with token span and part of speech.  You can click on the span column first, then on the doc
// name, then on the pos column.  Your table will the be sorted accoding to parts of speech, grouped
// by input document, and the spans in the order in which they occur in the document. Pretty
// powerful, but hard to explain as well.
//
// Another note: The implementation assumes that the table view has columns in the following order: 
// N number of fields of the output view, followed by input document name. For output views 
// whose source is available in the workspace, there will be another column after the Input Document 
// column, with an 'Explain' button as the cell content, to view provenance of the tuple. If this order 
// ever changes, this code will require a fix.
public class TableViewComparator extends ViewerComparator {



	// Use this comparator to sort an array of IRows with regular Java
	// Arrays.sort(), which is
	// guaranteed to be stable.
	private static final class RowComparator implements Comparator<IRow> {

		private final int column;

		private final int inputDocumentColumn;

		private RowComparator(int column, int inputDocCol) {
			super();
			this.column = column;
			this.inputDocumentColumn = inputDocCol;
		}

		@Override
		public int compare(IRow row1, IRow row2) {
			FieldValue v1 = null;
			FieldValue v2 = null;
			String label1 = null;
			String label2 = null;
			if (this.column == this.inputDocumentColumn) {
				label1 = row1.getInputDocName();
				label2 = row2.getInputDocName();
			} else {
				v1 = row1.getValueForCell(this.column);
				v2 = row2.getValueForCell(this.column);
				label1 = row1.getLabelForCell(this.column);
				label2 = row2.getLabelForCell(this.column);
			}
			return compare(v1, v2, label1, label2);
		}

		// Sort according to field values. If we encounter a non-supported value
		// type, or the values are
		// null (if the column that is clicked on is the input file name for
		// example), use the string
		// representation as it appears in the UI and sort lexicographically.
		private int compare(FieldValue v1, FieldValue v2, String s1, String s2) {
			if (v1 == null || v2 == null) {
				return s1.compareTo(s2);
			}
			if (v1 instanceof IntVal && v2 instanceof IntVal) {
				int i1 = ((IntVal) v1).val;
				int i2 = ((IntVal) v2).val;
				return i1 - i2;
			}
			if (v1 instanceof FloatVal && v2 instanceof FloatVal) {
				float f1 = ((FloatVal) v1).val;
				float f2 = ((FloatVal) v2).val;
				return Float.compare(f1, f2);
			}
			if (v1 instanceof BoolVal && v2 instanceof BoolVal) {
				boolean b1 = ((BoolVal) v1).val;
				boolean b2 = ((BoolVal) v2).val;
				if (b1 && !b2) {
					return -1;
				}
				if (!b1 && b2) {
					return 1;
				}
				return 0;
			}
			if (v1 instanceof SpanVal && v2 instanceof SpanVal) {
				SpanVal span1 = ((SpanVal) v1);
				SpanVal span2 = ((SpanVal) v2);
				if (span1.isNull()) {
					if (span2.isNull()) {
						return 0;
					}
					return -1;
				}
				if (span2.isNull()) {
					return 1;
				}
				if (span1.start < span2.start) {
					return -1;
				}
				if (span2.start < span1.start) {
					return 1;
				}
				if (span1.end > span2.end) {
					return -1;
				}
				if (span2.end > span1.end) {
					return 1;
				}
				return 0;
			}
			return s1.compareTo(s2);
		}
	}

	// Used to get all tableView related data like number of columns, contents,
	// headers etc
	private IAQLTableViewModel model;

	/*
	 * Initially the first column of the table view is taken as default column
	 * for the table to be sorted and displayed.
	 */
	private int sortColumn = 0;

	// index of the column titled "Input Document"
	private final int inputDocumentColumnIndex;

	/**
	 * Does initial table sorting based on first column and "Input Document"
	 * column as the comparator for the table in tableView. When ever the user
	 * triggers sorting based on other column then sorting is done based on
	 * selected column with "Input Document" as comparator.
	 * 
	 * @param model
	 *            Contains all table related data such as number of columns,
	 *            headers, content etc.
	 */
	public TableViewComparator(IAQLTableViewModel model) {
		super();
		this.model = model;
		int numCols = this.model.getNumColumns();
		String[] headers = this.model.getHeaders();

		/*
		 * Check if the table is of
		 * type"Field | Field 2| ... | Field n | Input Document" This is for
		 * output views whose source is *not* available in the workspace, but
		 * only the compiled .tam is available. Provenance is not possible for
		 * views without source and hence option does not include Explain link.
		 * In this case the last column is always InputDocument
		 */
		if (headers[numCols - 1].equals(Constants.COLUMN_NAME_INPUT_DOCUMENT)) {
			this.inputDocumentColumnIndex = numCols - 1;
		} else {

			/*
			 * The table will be of type
			 * "Field 1 | Field 2| .... | Field n | InputDocument | Provenance Explain link"
			 * This is for output views whose source is available in the
			 * workspace, Provenance is possible for modules whose source is
			 * available, Hence Explain link column is displayed. In this case
			 * the last but one column will always be "Input Document" column.
			 */
			this.inputDocumentColumnIndex = numCols - 2;
		}
		initColumnSorting();

		/*
		 * Sort columns initially by considering first column and
		 * "Input Document" column as comparator, later on as per user selected
		 * column. But Input Document column remains as the comparator for
		 * sorting.
		 */
		sortByColumn(this.inputDocumentColumnIndex);
	}

	private void initColumnSorting() {
		// Initialize the sorting with initial order in the model
		IRow[] rows = this.model.getElements();
		for (int i = 0; i < rows.length; i++) {
			rows[i].setPosition(i);
		}
	}

	public void sortByColumn(int columnIndex) {
		this.sortColumn = columnIndex;
		// I'm not sure it's safe to do in situ sorting on the model, so I'm
		// making a copy of the rows
		// array first (not copying the rows of course).
		IRow[] sortArray = new IRow[this.model.getNumRows()];
		// Insert the elements according to their previous position. Every
		// position must occur exactly
		// once, otherwise bada boom.
		IRow[] rows = this.model.getElements();
		for (IRow row : rows) {
			sortArray[row.getPosition()] = row;
		}
		// Now sort with the current criterion.
		Arrays.sort(sortArray, new RowComparator(this.sortColumn,
				this.inputDocumentColumnIndex));
		// Finally, update the positions
		for (int i = 0; i < sortArray.length; i++) {
			sortArray[i].setPosition(i);
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		final int key1 = ((IRow) e1).getPosition();
		final int key2 = ((IRow) e2).getPosition();
		return key1 - key2;
	}

}
