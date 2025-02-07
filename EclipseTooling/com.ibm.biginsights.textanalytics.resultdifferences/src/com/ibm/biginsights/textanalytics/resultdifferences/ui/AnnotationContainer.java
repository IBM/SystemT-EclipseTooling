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

import org.eclipse.core.runtime.PlatformObject;

/**
 * Wrapper around objects that are shown in the By-XY Views in the Analysis
 * Results
 * 
 * 
 * 
 */

public class AnnotationContainer extends PlatformObject {



	/** The parent AnnotationContainer */
	protected final AnnotationContainer fParent;

	/** The wrapped object */
	protected final Object fAnnotationContainer;

	/** The annotation count associated with the wrapped object */
	protected final int fAnnotations;

	/** The processing time associated with the wrapped object */
	protected final int fProcessingTime;

	/**
	 * The constructor
	 * 
	 * @param parent
	 *            The parent AnnotationContainer, or null for root objects
	 * @param annotationContainer
	 *            The wrapped object
	 * @param annotations
	 *            The annotation count associated with the wrapped object
	 * @param processingTime
	 *            The processing time associated with the wrapped object
	 */
	public AnnotationContainer(final AnnotationContainer parent,
			final Object annotationContainer, final int annotations,
			final int processingTime) {
		fParent = parent;
		fAnnotationContainer = annotationContainer;
		fAnnotations = annotations;
		fProcessingTime = processingTime;
	}

	/**
	 * @return the parent {@link AnnotationContainer}
	 */
	public AnnotationContainer getParent() {
		return fParent;
	}

	/**
	 * @return the wrapped object
	 */
	public Object getAnnotationContainer() {
		return fAnnotationContainer;
	}

	/**
	 * @return the number of annotations associated with this object
	 */
	public int getAnnotations() {
		return fAnnotations;
	}

	/**
	 * @return the processing time associated with this object
	 */
	public int getProcessingTime() {
		return fProcessingTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME
				* result
				+ ((fAnnotationContainer == null) ? 0 : fAnnotationContainer
						.hashCode());
		result = PRIME * result + fAnnotations;
		result = PRIME * result + fProcessingTime;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AnnotationContainer other = (AnnotationContainer) obj;
		if (fAnnotationContainer == null) {
			if (other.fAnnotationContainer != null)
				return false;
		} else if (!fAnnotationContainer.equals(other.fAnnotationContainer))
			return false;
		if (fAnnotations != other.fAnnotations)
			return false;
		if (fProcessingTime != other.fProcessingTime)
			return false;
		return true;
	}
}
