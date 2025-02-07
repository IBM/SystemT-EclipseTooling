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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

/**
 * {@link IColumnBarProvider} for By-XY results view that paints bars for
 * annotation count and processing time columns
 * 
 * 
 * modified by  Rueck
 * 
 */
public class ResultViewBarProvider implements IColumnBarProvider {


	
	// the columns in which bars need to be painted
	private int[] fColumns;
	
	public ResultViewBarProvider(int[] columns){
		fColumns = columns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.uima.workbench.analysisstore.internal.ui.IColumnBarProvider#createBar(int,
	 *      org.eclipse.swt.widgets.Display, org.eclipse.swt.graphics.GC, int,
	 *      int, int, int)
	 */
	public void createBar(final int index, final int level,
			final Display display, final GC gc, final int x, final int y,
			final int width, final int height) {
		// Store foreground and background colors
		final Color fg = gc.getForeground();
		final Color bg = gc.getBackground();

		if (index == 1) {
			// Use some gray color for the annotation count column
			final int blue = Math.max(0, 222 - 30 * level);
			gc.setForeground(new Color(display, 176, 196, blue));
			gc.setBackground(new Color(display, 176, 176, blue));
		} else {
			// Use some orange color for the processing time column
			gc.setForeground(new Color(display, 255, 140, 0));
			gc.setBackground(new Color(display, 255, 120, 0));
		}

		// Paint a gradient rectangle as bar
		gc.fillGradientRectangle(x, y, width - 1, height - 1, true);

		// Set back foreground and background colors.
		gc.setForeground(fg);
		gc.setBackground(bg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.uima.workbench.analysisstore.internal.ui.IColumnBarProvider#isBarColumn(int)
	 
	 * iterate over columns array and return true if the given index
	 * is contained in the columns array
	 */
	public boolean isBarColumn(final int index) {
		for(int i=0; i<fColumns.length; i++){
			if(fColumns[i] == index){
				return true;
			}
		}
		return false;
	}
}
