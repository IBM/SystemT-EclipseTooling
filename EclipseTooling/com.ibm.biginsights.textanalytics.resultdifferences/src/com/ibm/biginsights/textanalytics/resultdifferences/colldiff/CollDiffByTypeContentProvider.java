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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;

import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnalysisResultExplorer;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.TypeContainer;

/**
 * ContentProvider for the By Type View
 * 
 * 
 * 
 */
public class CollDiffByTypeContentProvider extends
		CollDiffAbstractResultContentProvider {



	/** Empty array indicating no children */
	private static final transient Object[] NO_CHILDREN = new Object[0];

	/**
	 * Constructor
	 * 
	 * @param analysisResultExplorer
	 */
	public CollDiffByTypeContentProvider(
			final AnalysisResultExplorer analysisResultExplorer) {
		super(analysisResultExplorer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.uima.workbench.analysisstore.internal.ui.AbstractResultContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(final Object element) {
		Object selectedObject = null;
		IFile[] rightFileList = new IFile[fSelection.size()];
		IFile[]  leftFileList = new IFile[fSelection.size()];
		IFile otherFile = null;
		IFile selectedFile = null;
		int m=0;
		boolean selectedFileIsLeft = false;
		
		for (final Iterator iter = fSelection.iterator(); iter.hasNext();m++) {
			selectedObject = iter.next();
			if (selectedObject instanceof IAdaptable) {
				selectedFile = (IFile) ((IAdaptable) selectedObject)
						.getAdapter(IFile.class);
				otherFile = getOtherFile(selectedFile);
				selectedFileIsLeft = (CollDiffModel.isLeftFile(selectedFile));
				if (ResultDifferencesUtil.isValidFile(selectedFile)){
					if (CollDiffModel.isLeftFile(selectedFile))
					{
						leftFileList[m] = selectedFile;
						rightFileList[m]= otherFile;
					}
					else
					{
						rightFileList[m] = selectedFile;
						leftFileList[m]= otherFile;
					}
				}
			}
		}

		if (element instanceof String[]) {
			final String[] typesystem = (String[]) element;
			final List<Object> result = new ArrayList<Object>();
			String type = null;
			for (int i = 0; i < typesystem.length; ++i) {
				type = typesystem[i];

				

				TypeContainer tCntnr = new TypeContainer(type);
/*				if (selectedFileIsLeft)
				{
*/					tCntnr.setRightFleList(rightFileList);
					tCntnr.setLeftFileList(leftFileList);
/*				}
				else
				{
					tCntnr.setRightFleList(leftFileList);
					tCntnr.setLeftFileList(rightFileList);
				}
*/				
				
				final CollDiffAnnotationContainer container = new CollDiffAnnotationContainer(
						null, tCntnr,
						getDeletedAnnotations(type), getNewAnnotations(type),
						getChangedAnnotations(type), 0);


				
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
	protected int getNewAnnotations(final String type) {
		int count = 0;
		Object selectedObject = null;
		IFile otherFile = null;
		IFile selectedFile = null;

		// get the id of the type
		//final int typeId = fAnalysisStoreReader.getTypeId(type);

		for (final Iterator iter = fSelection.iterator(); iter.hasNext();) {
			selectedObject = iter.next();
			if (selectedObject instanceof IAdaptable) {
				selectedFile = (IFile) ((IAdaptable) selectedObject)
						.getAdapter(IFile.class);
				if (ResultDifferencesUtil.isValidFile(selectedFile)){
					boolean isSelectedFileLeft = CollDiffModel.isLeftFile(selectedFile);

					// get the second file
					otherFile = getOtherFile(selectedFile);
					if (isSelectedFileLeft)
					{
						count += fAnalysisStoreReader.getNewAnnotationsCountInActual(
								otherFile, selectedFile, type);
					}
					else
					{
						count += fAnalysisStoreReader.getNewAnnotationsCountInActual(
								selectedFile, otherFile, type);
					}
				}
			}
		}

		return count;
	}

	/**
	 * @param type
	 *            A type name
	 * @return The number of deleted annotations for the given type and the
	 *         selected files
	 */
	protected int getDeletedAnnotations(final String type) {
		int count = 0;
		Object selectedObject = null;
		IFile otherFile = null;
		IFile selectedFile = null;

		// get the id of the type
		//final int typeId = fAnalysisStoreReader.getTypeId(type);

		for (final Iterator iter = fSelection.iterator(); iter.hasNext();) {
			selectedObject = iter.next();
			if (selectedObject instanceof IAdaptable) {
				selectedFile = (IFile) ((IAdaptable) selectedObject)
						.getAdapter(IFile.class);
				if (ResultDifferencesUtil.isValidFile(selectedFile)){
					boolean isSelectedFileLeft = CollDiffModel.isLeftFile(selectedFile);

					// get the second file
					otherFile = getOtherFile(selectedFile);

					if (isSelectedFileLeft)
					{
						count += fAnalysisStoreReader.getDeletedAnnotationsCountInExpected(
								otherFile, selectedFile, type);
					}
					else
					{
						count += fAnalysisStoreReader.getDeletedAnnotationsCountInExpected(
								selectedFile, otherFile, type);
					}
				}
			}
		}

		return count;
	}

	/**
	 * @param type
	 *            A type name
	 * @return The number of annotations for the given type and the selected
	 *         files
	 */
	protected int getChangedAnnotations(final String type) {
		int count = 0;
		Object selectedObject = null;
		IFile otherFile = null;
		IFile selectedFile = null;

		// get the id of the type
		//final int typeId = fAnalysisStoreReader.getTypeId(type);

		for (final Iterator iter = fSelection.iterator(); iter.hasNext();) {
			selectedObject = iter.next();
			if (selectedObject instanceof IAdaptable) {
				selectedFile = (IFile) ((IAdaptable) selectedObject)
						.getAdapter(IFile.class);
				if (ResultDifferencesUtil.isValidFile(selectedFile) ) {
					boolean isSelectedFileLeft = CollDiffModel.isLeftFile(selectedFile);
					// get the second file
					otherFile = getOtherFile(selectedFile);
					
					if (isSelectedFileLeft)
					{
						count += fAnalysisStoreReader.getChangedAnnotationsCount(
								otherFile, selectedFile, type);
					}else
					{
						count += fAnalysisStoreReader.getChangedAnnotationsCount(
								selectedFile, otherFile, type);

					}
				}
			}
		}

		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.uima.workbench.analysisstore.internal.ui.AbstractResultContentProvider#getAnnotationContainerChildren(com.ibm.uima.workbench.ui.AnnotationContainer)
	 */
	protected Object[] getAnnotationContainerChildren(
			final CollDiffAnnotationContainer container) {

		Object wrappedObject = container.getCollDiffAnnotationContainer();

		if (wrappedObject instanceof TypeContainer) {
			final String type = ((TypeContainer) wrappedObject).getName();
			final List<Object> children = new ArrayList<Object>();
			Object selectedObject = null;
			IFile otherFile = null;
			IFile selectedFile = null;
			int deletedCount = 0;
			int newCount = 0;
			int changedCount = 0;
			int time = 0;
			for (Iterator iter = fSelection.iterator(); iter.hasNext();) {
				selectedObject = iter.next();
				if (selectedObject instanceof IAdaptable) {

					selectedFile = (IFile) ((IAdaptable) selectedObject)
							.getAdapter(IFile.class);

					if (ResultDifferencesUtil.isValidFile(selectedFile)){
						boolean isSelectedFileLeft = CollDiffModel.isLeftFile(selectedFile);
						otherFile = getOtherFile(selectedFile);

						//final int typeId = fAnalysisStoreReader.getTypeId(type);

						// get the necessary values
						
						if (isSelectedFileLeft)
						{
							deletedCount = fAnalysisStoreReader
									.getDeletedAnnotationsCountInExpected(otherFile, selectedFile,
											type);
							newCount = fAnalysisStoreReader.getNewAnnotationsCountInActual(
									otherFile, selectedFile, type);
							changedCount = fAnalysisStoreReader
									.getChangedAnnotationsCount(otherFile, selectedFile,
											type);
							time = fAnalysisStoreReader.getDiffProcessingTime(
									-1, -1);//TODO
	
							// create and add a the new object to the list of
							// children
							children.add(new CollDiffAnnotationContainer(container,
									new IFile[] { otherFile, selectedFile }, deletedCount,
									newCount, changedCount, time));
						}
						else
						{
							deletedCount = fAnalysisStoreReader
							.getDeletedAnnotationsCountInExpected(selectedFile, otherFile,
									type);
							newCount = fAnalysisStoreReader.getNewAnnotationsCountInActual(
									selectedFile, otherFile, type);
							changedCount = fAnalysisStoreReader
									.getChangedAnnotationsCount(selectedFile, otherFile,
											type);
							time = fAnalysisStoreReader.getDiffProcessingTime(
									-1, -1);//TODO
		
							// create and add a the new object to the list of
							// children
							children.add(new CollDiffAnnotationContainer(container,
									new IFile[] { selectedFile, otherFile }, deletedCount,
									newCount, changedCount, time));
						}
					}
				}
			}

			return children.toArray();
		}

		return NO_CHILDREN;
	}
}
