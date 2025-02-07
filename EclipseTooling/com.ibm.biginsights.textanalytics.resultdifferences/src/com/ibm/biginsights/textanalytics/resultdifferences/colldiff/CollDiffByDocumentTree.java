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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.ibm.biginsights.textanalytics.resultdifferences.filediff.FileDiffViewOpenListener;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnalysisResultExplorer;

/**
 * TreeViewer for By Document View
 * 
 * 
 * 
 */
public class CollDiffByDocumentTree extends CollDiffAbstractResultTree {


	
	public CollDiffByDocumentTree(final FormToolkit toolkit,
			final Composite parent, final AnalysisResultExplorer explorer,
			final FileDiffViewOpenListener openListener) {
		super(toolkit, parent, explorer, openListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.uima.workbench.internal.ui.resultsview.AbstractResultTree#createContentProvider(com.ibm.uima.workbench.internal.ui.resultsview.AnalysisResultExplorer)
	 */
	protected CollDiffAbstractResultContentProvider createContentProvider(
			final AnalysisResultExplorer explorer) {
		return new CollDiffByDocumentContentProvider(explorer);
	}
}
