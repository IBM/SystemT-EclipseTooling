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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import com.ibm.biginsights.textanalytics.resultdifferences.ui.AbstractResultComparator;

/**
 * {@link ViewerComparator} that supports comparing resource paths and numbers
 * (annotation count, processing time)
 * 
 * 
 *  Rueck
 * 
 */
public class CollDiffResultComparator extends AbstractResultComparator {



	/** sort field indicating to sort by resource path */
	protected static final int SORT_BY_RESOURCE_PATH = 0;

	/** sort field indicating to sort by deleted annotation count */
	protected static final int SORT_BY_DELETED_COUNT = 1;

	/** sort field indicating to sort by new annotation count */
	protected static final int SORT_BY_NEW_COUNT = 2;
	
	/** sort field indicating to sort by changed annotation count */
	protected static final int SORT_BY_CHANGED_COUNT = 3;

	/** sort field indicating to sort by total changes metric count */
	protected static final int SORT_BY_TOTAL_COUNT = 4;
	
	/** sort field indicating to sort by total changes metric count */
	protected static final int SORT_BY_PROCESSING_TIME = 5;	
	
	/** sort field currently used */
	private int fSortAttribute = SORT_BY_RESOURCE_PATH;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public int compare(final Viewer viewer, final Object e1, final Object e2) {
		if (e1 instanceof CollDiffAnnotationContainer
				&& e2 instanceof CollDiffAnnotationContainer) {
			final CollDiffAnnotationContainer container1 = (CollDiffAnnotationContainer) e1;
			final CollDiffAnnotationContainer container2 = (CollDiffAnnotationContainer) e2;

			switch (fSortAttribute) {
			case SORT_BY_DELETED_COUNT:
				final Integer deleted1 = new Integer(container1
						.getDeleted());
				final Integer deleted2 = new Integer(container2
						.getDeleted());
				return compareOrdered(deleted1.compareTo(deleted2));
			case SORT_BY_NEW_COUNT:
				final Integer new1 = new Integer(container1
						.getNew());
				final Integer new2 = new Integer(container2
						.getNew());
				return compareOrdered(new1.compareTo(new2));		
			case SORT_BY_CHANGED_COUNT:
				final Integer changed1 = new Integer(container1
						.getChanged());
				final Integer changed2 = new Integer(container2
						.getChanged());
				return compareOrdered(changed1.compareTo(changed2));		
			case SORT_BY_TOTAL_COUNT:
				final Integer total1 = new Integer(container1
						.getTotalChanged());
				final Integer total2 = new Integer(container2
						.getTotalChanged());
				return compareOrdered(total1.compareTo(total2));				
			case SORT_BY_PROCESSING_TIME:
				// compare absolute values here because the diff can
				// be positive or negative
				Integer time1 = new Integer(container1.getProcessingTime());
				Integer time2 = new Integer(container2.getProcessingTime());
				return compareOrdered(time1.compareTo(time2));
			case SORT_BY_RESOURCE_PATH:
				String string1 = ""; //$NON-NLS-1$
				String string2 = ""; //$NON-NLS-1$
				if(viewer instanceof TreeViewer){
					IBaseLabelProvider labelProvider = ((TreeViewer)viewer).getLabelProvider();
					if(labelProvider instanceof ITableLabelProvider){
						ITableLabelProvider tableLabelProvider = (ITableLabelProvider)labelProvider;
						string1 = tableLabelProvider.getColumnText(e1, 0);
						string2 = tableLabelProvider.getColumnText(e2, 0);
					}
				}
				return compareOrdered(string1.compareTo(string2));
			default:
				return compareOrdered(super.compare(viewer, e1, e2));
			}
		}

		return compareOrdered(super.compare(viewer, e1, e2));
	}

	/**
	 * @param sortAttribute
	 *            The sort field to use, either
	 *            <code>SORT_BY_RESOURCE_PATH</code>,
	 *            <code>SORT_BY_ANNOTATION_COUNT</code> or
	 *            <code>SORT_BY_PROCESSING_TIME</code>
	 * @param sortDirection
	 *            The sort direction to use, either <code>SWT.UP</code> or
	 *            <code>SWT.DOWN</code>
	 * @see #SORT_BY_ANNOTATION_COUNT
	 * @see #SORT_BY_PROCESSING_TIME
	 * @see #SORT_BY_RESOURCE_PATH
	 * @see SWT#UP
	 * @see SWT#DOWN
	 */
	public void setSortAttribute(final int sortAttribute,
			final int sortDirection) {
		Assert.isLegal(sortAttribute == SORT_BY_DELETED_COUNT
				|| sortAttribute == SORT_BY_NEW_COUNT
				|| sortAttribute == SORT_BY_CHANGED_COUNT
				|| sortAttribute == SORT_BY_TOTAL_COUNT
				|| sortAttribute == SORT_BY_PROCESSING_TIME
				|| sortAttribute == SORT_BY_RESOURCE_PATH);
		Assert.isLegal(sortDirection == SWT.UP || sortDirection == SWT.DOWN);
		fSortAttribute = sortAttribute;
		fSortDirection = sortDirection;
	}

}
