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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

/**
 * {@link ViewerComparator} that supports comparing resource paths and numbers
 * (annotation count, processing time)
 * 
 * sublasses:
 * @see AnalysisResultViewComparator
 * @see CollDiffResultComparator
 * 
 *  Rueck
 * 
 */
public abstract class AbstractResultComparator extends ViewerComparator {



	/** current sort direction */
	protected int fSortDirection = SWT.UP;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public int compare(final Viewer viewer, final Object e1, final Object e2){
		return super.compare(viewer, e1, e2);	
	}

	/**
	 * @param sortAttribute
	 *            The sort field to use, either
	 * @param sortDirection
	 *            The sort direction to use, either <code>SWT.UP</code> or
	 *            <code>SWT.DOWN</code>
	 * @see SWT#UP
	 * @see SWT#DOWN
	 */
	public abstract void setSortAttribute(final int sortAttribute,
			final int sortDirection);

	/**
	 * Reverses a sort result if descending sort direction is desired
	 * 
	 * @param ascendingResult
	 *            The sort result in ascending order
	 * @return ascendingResult for ascending sort direction and the inverted
	 *         value otherwise
	 */
	protected int compareOrdered(final int ascendingResult) {
		return fSortDirection == SWT.UP ? ascendingResult : -1
				* ascendingResult;
	}

	
	
}
