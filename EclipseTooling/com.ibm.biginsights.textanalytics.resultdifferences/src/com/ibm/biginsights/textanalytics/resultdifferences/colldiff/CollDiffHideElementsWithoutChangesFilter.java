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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A ViewerFilter which hides elements if their their
 * were not changes between the two runs
 * 
 *  Rueck
 * 
 * 
 */
public class CollDiffHideElementsWithoutChangesFilter extends ViewerFilter {


	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public boolean select(final Viewer viewer, final Object parentElement,
			final Object element) {
		if (!(element instanceof CollDiffAnnotationContainer)) {
			return true;
		}

		return ((CollDiffAnnotationContainer) element).getTotalChanged() > 0;
	}

}
