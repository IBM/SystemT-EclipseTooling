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

import org.eclipse.core.runtime.PlatformObject;

import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnnotationContainer;

/**
 * Wrapper around objects that are shown in the By-XY Views in the Collection
 * Level Diff.
 * 
 */

public class CollDiffGSAnnotationContainer extends PlatformObject {



	/** The parent AnnotationContainer */
	protected final CollDiffGSAnnotationContainer fParent;

	/** The wrapped object */
	private final Object fCollDiffAnnotationContainer;


	/** deleted annotations */
	private final double fPrecision;

	/** new annotations */
	private final double fRecall;

	/** changed annotations */
	private final double fFMeasure;

	/** The processing time associated with the wrapped object */
	private final int fProcessingTime;

	/**
	 * 
	 * @param parent
	 * @param annotationContainer
	 * @param annotations
	 * @param processingTime
	 */
	public CollDiffGSAnnotationContainer(
			final CollDiffGSAnnotationContainer parent,
			final Object diffContainer, final double pPrecision,
			final double pRecall, final double pFMeasure, final int processingTime) {
		fParent = parent;
		fCollDiffAnnotationContainer = diffContainer;
		fPrecision = pPrecision;
		fRecall = pRecall;
		fFMeasure = pFMeasure;
		fProcessingTime = processingTime;
	}

	/**
	 * @return the parent {@link AnnotationContainer}
	 */
	public CollDiffGSAnnotationContainer getParent() {
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
	public double getPrecision(){
		return fPrecision;
	}

	/**
	 * 
	 * @return the count of new annotations
	 */
	public double getRecall() {
	
		return fRecall;
	}


	/**
	 * 
	 * @return the count of changed annotations
	 */
	public double getFMeasure() {

		return fFMeasure;
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
		result = PRIME * result ;//+ fPrecision;TODO
		result = PRIME * result ; //+ fNew; TODO
		result = PRIME * result ; //+ fChanged; TODO
		result = PRIME * result ; // + fProcessingTime; TODO
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
		final CollDiffGSAnnotationContainer other = (CollDiffGSAnnotationContainer) obj;
		if (fCollDiffAnnotationContainer == null) {
			if (other.fCollDiffAnnotationContainer != null)
				return false;
		} else if (!fCollDiffAnnotationContainer
				.equals(other.fCollDiffAnnotationContainer))
			return false;
		if (fPrecision != other.fPrecision)
			return false;
		if (fRecall != other.fRecall)
			return false;
		if (fFMeasure != other.fFMeasure)
			return false;
		if (fProcessingTime != other.fProcessingTime)
			return false;
		return true;
	}
}
