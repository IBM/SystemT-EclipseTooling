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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnalysisResultExplorer;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.TypeContainer;


/**
 * 
 * 
 */
public class CollDiffByDocumentContentProvider extends
		CollDiffAbstractResultContentProvider {




	/** empty array indicating no children */
	private static final transient Object[] NO_CHILDREN = new Object[0];

	/**
	 * Constructor
	 * 
	 * @param explorer
	 */
	public CollDiffByDocumentContentProvider(
			final AnalysisResultExplorer explorer) {
		super(explorer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.uima.workbench.analysisstore.internal.ui.AbstractResultContentProvider#getAnnotationContainerChildren(com.ibm.uima.workbench.ui.AnnotationContainer)
	 */
	protected Object[] getAnnotationContainerChildren(
			final CollDiffAnnotationContainer container) {

		Object wrappedObject = container.getCollDiffAnnotationContainer();

		/*log.trace("getAnnotationContainerChildren() for object " //$NON-NLS-1$
				+ wrappedObject.getClass().getName());
		if (wrappedObject instanceof Annotation) {
			return NO_CHILDREN;
		}*/
		if (wrappedObject instanceof TypeContainer) {
			final CollDiffAnnotationContainer parent = container.getParent();
			if (parent != null
					&& parent.getCollDiffAnnotationContainer() instanceof IFile) {
				final String[] types = fAnalysisStoreReader.getDiffTypes();
				final List<Object> result = new ArrayList<Object>();
				for (int i = 0; i < types.length; ++i) {
					result.add(new CollDiffAnnotationContainer(container,
							new TypeContainer(types[i]), 1, 1, 1, 0));
				}
				return result.toArray();
			}
			return NO_CHILDREN;
		}

		if (wrappedObject instanceof IFile[]) {

			final IFile[] files = (IFile[]) wrappedObject;
			boolean isFile0Valid = ResultDifferencesUtil.isValidFile(files[0]);
			boolean isFile1Valid = ResultDifferencesUtil.isValidFile(files[1]);;
			if (isFile0Valid && isFile1Valid) {
				return getTypeDetails(container, files[0], files[1]);
			}
		}

		return internalGetChildren(container);
	}

	/**
	 * @param container
	 *            parent container
	 * @param file
	 *            A file
	 * @return AnnotationContainer array consisting of all types and their
	 *         annotation counts for a given file
	 */
	protected Object[] getTypeDetails(
			final CollDiffAnnotationContainer container, final IFile rightFile,
			final IFile leftFile) {
		final List<Object> result = new ArrayList<Object>();
		final String[] types = fAnalysisStoreReader.getDiffTypes();
		int deletedCount = 0;
		int newCount = 0;
		int changedCount = 0;

		for (int i = 0; i < types.length; ++i) {

			// get the id of the types
			final String type = types[i];
			//final int typeId = fAnalysisStoreReader.getTypeId(type);

			// get the necessary values
			deletedCount = fAnalysisStoreReader.getDeletedAnnotationsCountInExpected(
					rightFile, leftFile, type);
			newCount = fAnalysisStoreReader.getNewAnnotationsCountInActual(rightFile,
					leftFile, type);
			changedCount = fAnalysisStoreReader.getChangedAnnotationsCount(
					rightFile, leftFile, type);

			result.add(new CollDiffAnnotationContainer(container, type,
					deletedCount, newCount, changedCount, 0));
		}

		return result.toArray();
	}
}
