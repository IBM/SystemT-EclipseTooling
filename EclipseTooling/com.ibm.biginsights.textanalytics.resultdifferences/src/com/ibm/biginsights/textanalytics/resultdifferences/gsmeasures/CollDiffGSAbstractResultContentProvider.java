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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;

import com.ibm.biginsights.textanalytics.resultdifferences.compute.DifferencesComputer;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnalysisResultExplorer;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnnotationContainer;

/**
 * ContentProvider that provides common methods for GS  Views
 */

public abstract class CollDiffGSAbstractResultContentProvider extends
		WorkbenchContentProvider implements ISelectionChangedListener {


 
	/** The current selection in the Result Explorer */
	protected ITreeSelection fSelection = new TreeSelection();

	/** The viewer displaying the By-XY view */
	protected Viewer fViewer;

	/** The Analysis Result Explorer used to select files */
	protected final AnalysisResultExplorer fAnalysisResultExplorer;

	/** The Analysis Store Reader used to get data from */
	protected DifferencesComputer fAnalysisStoreReader;

	/**
	 * The workspace in whick the two folders lie
	 */
	protected IWorkspace fWorkspace;

	/** Empty array indicating no children */
	private final Object[] NO_CHILDREN = new Object[0];

	
	/**
	 * Constructor
	 * 
	 * @param explorer
	 *            The {@link AnalysisResultExplorer} that will function as a
	 *            SelectionProvider
	 */
	public CollDiffGSAbstractResultContentProvider(
			final AnalysisResultExplorer explorer) {
		fAnalysisResultExplorer = explorer;
		fAnalysisResultExplorer.addSelectionChangedListener(this);		
	}

	/**
	 * Switch to a different {@link IAnalysisStoreReader}
	 * 
	 * @param analysisStoreReader
	 *            The new Analysis Store Reader
	 */
	public void setAnalysisStoreReader(
			final DifferencesComputer analysisStoreReader) {
		fAnalysisStoreReader = analysisStoreReader;		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.BaseWorkbenchContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(final Object element) {
		//	long time1 = System.currentTimeMillis();

	//	if (fAnalysisStoreReader == null) {
			//return NO_CHILDREN;
//		}

		if (element instanceof CollDiffGSAnnotationContainer) {
			final CollDiffGSAnnotationContainer container = (CollDiffGSAnnotationContainer) element;
			return getAnnotationContainerChildren(container);
		}
		Object[] obj= internalGetChildren(element);		
		//		long time2 = System.currentTimeMillis();
		//	log.error("\ngetChildren: "+(time2-time1) +"\n");
		return obj;
			
	}

	/**
	 * @param annotationContainer
	 *            The {@link AnnotationContainer} that is wrapped around the
	 *            object
	 * @return The children of the given AnnotationContainer, e.g. in a By-Type
	 *         View, this may be files
	 */
	protected abstract Object[] getAnnotationContainerChildren(
			final CollDiffGSAnnotationContainer annotationContainer);

	/**
	 * TODO: review this method
	 * 
	 * @param element
	 * @return The children for the given element
	 */
	protected Object[] internalGetChildren(final Object element) {
		return NO_CHILDREN;
		}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.WorkbenchContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object
	 */
	public void inputChanged(final Viewer viewer, final Object oldInput,
			final Object newInput) {
		super.inputChanged(viewer, oldInput, newInput);
		fViewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(final SelectionChangedEvent event) {

		
		final ISelection selection = event.getSelection();
		if (selection instanceof ITreeSelection) {
			fSelection = (ITreeSelection) selection;
			if (fViewer != null) {
				try {
					PlatformUI.getWorkbench().getProgressService()
							.busyCursorWhile(new IRunnableWithProgress() {
								public void run(IProgressMonitor monitor) {
									PlatformUI.getWorkbench().getDisplay()
											.asyncExec(new Runnable() {
												public void run() {
													fViewer.refresh();
												}
											});
								}
							});
				} catch (final InvocationTargetException exception) {
					exception.printStackTrace();
				//	log.warning("Unable to refresh view", exception.getCause()); //$NON-NLS-1$
				} catch (final InterruptedException exception) {
					exception.printStackTrace();
					//log.warning("Unable to refresh view", exception); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * used to set the selection which is stored at content provider
	 * 
	 * @param selection
	 */
	public void setSelection(ITreeSelection selection) {
		fSelection = selection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.WorkbenchContentProvider#dispose()
	 */
	public void dispose() {
		fAnalysisResultExplorer.removeSelectionChangedListener(this);
		super.dispose();
	}
}
