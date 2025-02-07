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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

/**
 * Interface for classes that display a bar in a table cell
 * 
 * 
 * 
 */
public interface IColumnBarProvider {

	static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

	/**
	 * @param index
	 *            The index of the TableColumn
	 * @return <code>true</code> if a bar should be painted for the
	 *         TableColumn with the given index
	 */
	public boolean isBarColumn(final int index);

	/**
	 * Paint a bar for the TableColumn with the given index
	 * 
	 * @param index
	 *            TableColumn index that wants a bar painted
	 * @param display
	 *            Display to be used
	 * @param gc
	 *            Graphics context to be used for painting
	 * @param x
	 *            Top left corner of the bar, x value
	 * @param y
	 *            Top left corner of the bar, y value
	 * @param width
	 *            width of the bar
	 * @param height
	 *            height of the bar
	 */
	public void createBar(final int index, final int level, final Display display, final GC gc,
			final int x, final int y, final int width, final int height);

}
