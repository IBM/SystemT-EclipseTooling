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

import java.util.ArrayList;
import java.util.List;

import com.ibm.biginsights.textanalytics.resultdifferences.Messages;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnalysisResultExplorer;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.TypeContainer;
import com.ibm.biginsights.textanalytics.util.common.Constants.GoldStandardType;

/**
 * ContentProvider for the By Type View
 * 
 * 
 * 
 */
public class CollDiffGSContentProvider extends
		CollDiffGSAbstractResultContentProvider {



	/** Empty array indicating no children */
	private static final transient Object[] NO_CHILDREN = new Object[0];

	/**
	 * Constructor
	 * 
	 * @param analysisResultExplorer
	 */
	public CollDiffGSContentProvider(
			final AnalysisResultExplorer analysisResultExplorer) {
		super(analysisResultExplorer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.uima.workbench.analysisstore.internal.ui.AbstractResultContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(final Object element) {
		if (element == null)
		{
			return NO_CHILDREN;
		}

		if (element instanceof String[]) {
			final String[] typesystem = (String[]) element;
			final List<Object> result = new ArrayList<Object>();
			String type = null;
			for (int i = 0; i < typesystem.length; ++i) {
				type = typesystem[i];

				final CollDiffGSAnnotationContainer container = new CollDiffGSAnnotationContainer(
						null, new TypeContainer(type),
						getPrecision(type), getRecall(type),
						getFMeasure(type), 0);
				result.add(container);
			}
			return result.toArray();
		}
		return super.getChildren(element);
	}

	/**
	 * @param type
	 *            A type name
	 * @return The number of new annotations for the given type and the selected
	 *         files
	 */
	protected double getPrecision(final String type) {
		return 0;

	}

	/**
	 * @param type
	 *            A type name
	 * @return The number of deleted annotations for the given type and the
	 *         selected files
	 */
	protected double getRecall(final String type) {
		return 0;
	}

	/**
	 * @param type
	 *            A type name
	 * @return The number of annotations for the given type and the selected
	 *         files
	 */
	protected double getFMeasure(final String type) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.uima.workbench.analysisstore.internal.ui.AbstractResultContentProvider#getAnnotationContainerChildren(com.ibm.uima.workbench.ui.AnnotationContainer)
	 */
	protected Object[] getAnnotationContainerChildren(
			final CollDiffGSAnnotationContainer container) {

		Object wrappedObject = container.getCollDiffAnnotationContainer();
		if (wrappedObject instanceof TypeContainer) {
			final String type = ((TypeContainer) wrappedObject).getName();
			final List<Object> children = new ArrayList<Object>();
			double precision;
			double recall;
			double fMeasure= 0;
			
			double[] gsMsrs = fAnalysisStoreReader.getGoldStandardMeasures(GoldStandardType.Exact,type);
			precision= gsMsrs[0];
			recall = gsMsrs[1];
			fMeasure = gsMsrs[2];
			children.add(new CollDiffGSAnnotationContainer(container,Messages.getString("CollDiff_GS_Exact"), precision,recall, fMeasure, 0)); //$NON-NLS-1$
			
			gsMsrs = fAnalysisStoreReader.getGoldStandardMeasures(GoldStandardType.Partial,type);
			precision= gsMsrs[0];
			recall = gsMsrs[1];
			fMeasure = gsMsrs[2];
			children.add(new CollDiffGSAnnotationContainer(container,Messages.getString("CollDiff_GS_Partial"), precision,recall, fMeasure, 0)); //$NON-NLS-1$

			gsMsrs = fAnalysisStoreReader.getGoldStandardMeasures(GoldStandardType.Relaxed,type);
			precision= gsMsrs[0];
			recall = gsMsrs[1];
			fMeasure = gsMsrs[2];
			children.add(new CollDiffGSAnnotationContainer(container,Messages.getString("CollDiff_GS_Relaxed"), precision,recall, fMeasure, 0)); //$NON-NLS-1$
			
			return children.toArray();
			
		}

		return NO_CHILDREN;
	}
}
