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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.resultdifferences.Activator;
import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.TypeContainer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;

/**
 * LabelProvider for {@link CollDiffAnnotationContainer} objects
 * 
 * 
 * modified by  Rueck
 * 
 */
public class CollDiffLabelProvider extends WorkbenchLabelProvider implements
		ITableLabelProvider {



	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
	 *      int)
	 */
	public Image getColumnImage(final Object element, final int columnIndex) {
		if (element instanceof CollDiffAnnotationContainer) {
			final CollDiffAnnotationContainer annotationContainer = (CollDiffAnnotationContainer) element;
			final Object wrappedObject = annotationContainer.getCollDiffAnnotationContainer();
			if (columnIndex == 0) {
				if (wrappedObject instanceof TypeContainer) {
					return Activator.getImage("annotation.gif"); //$NON-NLS-1$
				}

				if (wrappedObject instanceof String) {
					return Activator.getImage("annotation.gif"); //$NON-NLS-1$
				}
				
				if (wrappedObject instanceof IFile[]) {
					return Activator.getImage("strf.gif"); //$NON-NLS-1$
				}				

				return getImage(wrappedObject);
			}
		}

		return columnIndex > 0 ? null : getImage(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
	 *      int)
	 */
	public String getColumnText(final Object element, final int columnIndex) {
		if (element instanceof CollDiffAnnotationContainer) {
			final CollDiffAnnotationContainer annotationContainer = (CollDiffAnnotationContainer) element;
			final Object wrappedObject = annotationContainer.getCollDiffAnnotationContainer();
			
			switch (columnIndex) {
			case 0:
				if (wrappedObject instanceof TypeContainer) {
					//System.out.println("case 0: instanceof TypeContainer: " + ((TypeContainer) annotationContainer
					//		.getCollDiffAnnotationContainer()).getName());
					return ((TypeContainer) wrappedObject).getName();
				}

				if (wrappedObject instanceof String) {
					//System.out.println("case 0: instanceof String: " + (String) annotationContainer
					//		.getCollDiffAnnotationContainer());
					return (String) wrappedObject;
				}
				
				// if wrapped object is file array return the filename of the first file
				// both filenames are equal
				if (wrappedObject instanceof IFile[]) {
					IFile[] files = (IFile[]) wrappedObject;
					//System.out.println("case 0: IFile[]");
					SystemTComputationResult model = null;
					//String fileName =files[1].getName();
					if (files[0].isAccessible())
					{
						model = ResultDifferencesUtil.getModelFromSTRFFile(files[0]);
					}
					else
					{
						model = ResultDifferencesUtil.getModelFromSTRFFile(files[1]);
					}
					String modifiedFileName = model.getDocumentID ();
					return modifiedFileName; // show the filename that is available
				}
				return getText(wrappedObject);
			case 1:
				return "" + annotationContainer.getTotalChanged(); //$NON-NLS-1$
			case 2:
				return "" + annotationContainer.getDeleted(); //$NON-NLS-1$
			case 3:
				return "" + annotationContainer.getNew(); //$NON-NLS-1$		s[]
			case 4:
				return "" + annotationContainer.getChanged(); //$NON-NLS-1$	
			case 5:
				return "" + annotationContainer.getProcessingTime(); //$NON-NLS-1$				
			default:
				return null;
			}
		}
		return columnIndex > 0 ? null : getText(element);
	}
	
	
}
