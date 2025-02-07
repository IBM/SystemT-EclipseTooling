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

import org.eclipse.core.runtime.PlatformObject;

import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnnotationContainer;

/**
 * Wrapper around objects that are shown in the By-XY Views in the Collection
 * Level Diff.
 * 
 *  Rueck
 * 
 */

public class CollDiffAnnotationContainer extends PlatformObject {



	/** The parent AnnotationContainer */
	protected final CollDiffAnnotationContainer fParent;

	/** The wrapped object */
	private final Object fCollDiffAnnotationContainer;

	/** the changes metric (supposed to be the sum at first) */
	private final int fTotalChanged;

	/** deleted annotations */
	private final int fDeleted;

	/** new annotations */
	private final int fNew;

	/** changed annotations */
	private final int fChanged;

	/** The processing time associated with the wrapped object */
	private final int fProcessingTime;

	/**
	 * 
	 * @param parent
	 * @param annotationContainer
	 * @param annotations
	 * @param processingTime
	 */
	public CollDiffAnnotationContainer(
			final CollDiffAnnotationContainer parent,
			final Object diffContainer, final int deletedCount,
			final int newCount, final int changedCount, final int processingTime) {
		fParent = parent;
		fCollDiffAnnotationContainer = diffContainer;
		fNew = newCount;
		fDeleted = deletedCount;
		fChanged = changedCount;
		fProcessingTime = processingTime;
		fTotalChanged = fNew + fDeleted + fChanged;
	}

	/**
	 * @return the parent {@link AnnotationContainer}
	 */
	public CollDiffAnnotationContainer getParent() {
		return fParent;
	}

	/**
	 * @return the wrapped object
	 */
	public Object getCollDiffAnnotationContainer() {
		return fCollDiffAnnotationContainer;
	}

	/**
	 * @return the processing time associated with this object
	 */
	public int getProcessingTime() {
		return fProcessingTime;
	}

	/**
	 * 
	 * @return the count of deleted annotations
	 */
	public int getDeleted() {
		return fDeleted;
	}

	/**
	 * 
	 * @return the count of new annotations
	 */
	public int getNew() {
	
		return fNew;
	}


	/**
	 * 
	 * @return the count of changed annotations
	 */
	public int getChanged() {

		return fChanged;
	}

	public int getTotalChanged() {

		return fTotalChanged;
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
				+ ((fCollDiffAnnotationContainer == null) ? 0
						: fCollDiffAnnotationContainer.hashCode());
		result = PRIME * result + fDeleted;
		result = PRIME * result + fNew;
		result = PRIME * result + fChanged;
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
		final CollDiffAnnotationContainer other = (CollDiffAnnotationContainer) obj;
		if (fCollDiffAnnotationContainer == null) {
			if (other.fCollDiffAnnotationContainer != null)
				return false;
		} else if (!fCollDiffAnnotationContainer
				.equals(other.fCollDiffAnnotationContainer))
			return false;
		if (fDeleted != other.fDeleted)
			return false;
		if (fNew != other.fNew)
			return false;
		if (fChanged != other.fChanged)
			return false;
		if (fProcessingTime != other.fProcessingTime)
			return false;
		return true;
	}
	/*
	 * public String toString(){ return "CollDiffAnnotationContainer:
	 * totalChanged: " + this.fTotalChanged + " deleted: " + this.fDeleted+ "
	 * new: " + this.fNew + " changed: " + this.fChanged + " processing time: " +
	 * this.fProcessingTime + "\nchildContainer: " +
	 * this.fCollDiffAnnotationContainer; }
	 */
}
