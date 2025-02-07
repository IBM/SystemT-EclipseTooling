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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnalysisResultExplorer;

/**
 * TreeViewer for the GS View
 * 
 */
public class CollDiffGSTree extends CollDiffGSAbstractResultTree {



	/**
	 * Constructor
	 * 
	 * @param toolkit
	 *            FormToolkit to create/adapt widgets with
	 * @param parent
	 *            The parent composite
	 * @param explorer
	 *            {@link AnalysisResultExplorer} used for selecting files
	 */
	public CollDiffGSTree(final FormToolkit toolkit, final Composite parent,
			final AnalysisResultExplorer explorer) {
		super(toolkit, parent, explorer,null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.uima.workbench.internal.ui.resultsview.AbstractResultTree#createContentProvider(com.ibm.uima.workbench.internal.ui.resultsview.AnalysisResultExplorer)
	 */
	protected CollDiffGSAbstractResultContentProvider createContentProvider(
			final AnalysisResultExplorer explorer) {
		return new CollDiffGSContentProvider(explorer);
	}

}
