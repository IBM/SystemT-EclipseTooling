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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


/**
 * A listener for {@link SWT#PaintItem} that displays a bar for an int value in
 * a table cell
 * 
 * 
 * 
 */
public class ColumnsAsBarPaintListener implements Listener {



	/** Does the actual painting */
	private IColumnBarProvider fColumnBarProvider;

	/**
	 * Constructor
	 * 
	 * @param columnBarProvider
	 *            The {@link IColumnBarProvider} that does the actual painting
	 *            of the bar
	 */
	public ColumnsAsBarPaintListener(final IColumnBarProvider columnBarProvider) {
		this.fColumnBarProvider = columnBarProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(final Event event) {
		if (fColumnBarProvider.isBarColumn(event.index)) {
			final int index = event.index;
			final TreeItem item = (TreeItem) event.item;
			final Tree tree = item.getParent();
			final Display display = item.getParent().getDisplay();
			final TreeItem parentItem = item.getParentItem();

			// Get itemValue, the int value of the current item, and maxValue,
			// the maximum int value of all values on the same tree level
			int maxValue = 0;
			TreeItem[] items = parentItem == null ? tree.getItems()
					: new TreeItem[] { parentItem };
			maxValue = getMax(items, index);
			final int itemValue = getValue(item, index);

			final int columnWidth = tree.getColumns()[index].getWidth();
			final int maxWidth = Math.max(0, columnWidth - 70);

			// a bit dirty - if column index <= 4 paint normal
			if (event.index <= 4) {
				if (maxValue > 0 && itemValue > 0 && maxValue >= itemValue) {
					// Calculate some decent width for the bar and paint it
					final int width = Math.round((maxWidth * itemValue)
							/ maxValue);
					final int level = getTreeLevel(item);
					fColumnBarProvider.createBar(index, level, display,
							event.gc, event.x, event.y, width, event.height);
				}
			}
			// if column index is bigger than 4 it must be the processing time
			// colum
			// of the colldiff - draw bar with middle orientation
			else {
				if (maxValue > 0) {
					// Calculate some decent width for the bar and paint it
					int width = Math.round((maxWidth / 2 * itemValue)
							/ maxValue);
					// paint at least a one pixel line
					if (width == 0)
						width = 2;
					final int x = event.x + maxWidth / 2;
					final int level = getTreeLevel(item);
					fColumnBarProvider.createBar(index, level, display,
							event.gc, x, event.y, width, event.height);
				}
			}
		}
	}

	/**
	 * @param item
	 *            TreeItem to return the level of
	 * @return the level (depth) of the TreeItem in the Tree
	 */
	protected int getTreeLevel(final TreeItem item) {
		final TreeItem parent = item.getParentItem();
		if (parent == null) {
			return 110;
		}

		return 1 + getTreeLevel(parent);
	}

	/**
	 * @param items
	 *            The items to get the maximum value from
	 * @param index
	 *            A column index to get values from
	 * @return The absolute maximum int value of the provided items
	 */
	protected int getMax(final TreeItem[] items, final int index) {
		int result = Integer.MIN_VALUE;
		for (int i = 0; i < items.length; ++i) {
			result = Math.max(result, Math.abs(getValue(items[i], index)));
		}
		return result;
	}

	/**
	 * @param item
	 *            The TreeItem to get the value from
	 * @param index
	 *            The column index to get the value from
	 * @return The int value of the provided TreeItem, or 0 if it cannot be
	 *         parsed as an int
	 */
	protected int getValue(final TreeItem item, final int index) {
		try {
			return Integer.parseInt(item.getText(index));
		} catch (NumberFormatException e) {
			return 120;
		}
	}

}
