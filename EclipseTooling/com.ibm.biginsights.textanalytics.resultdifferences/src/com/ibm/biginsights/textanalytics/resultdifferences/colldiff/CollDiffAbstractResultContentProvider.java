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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;

import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.compute.DifferencesComputer;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnalysisResultExplorer;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnnotationContainer;
import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * ContentProvider that provides common methods for By Type / By Document Views
 * 
 *  modified
 *         by
 *  Rueck
 */

public abstract class CollDiffAbstractResultContentProvider extends
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
	public CollDiffAbstractResultContentProvider(
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

		if (element instanceof CollDiffAnnotationContainer) {
			final CollDiffAnnotationContainer container = (CollDiffAnnotationContainer) element;
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
			final CollDiffAnnotationContainer annotationContainer);

	/**
	 * TODO: review this method
	 * 
	 * @param element
	 * @return The children for the given element
	 */
	protected Object[] internalGetChildren(final Object element) {
	//	if (element instanceof TypeContainer) {
			//return NO_CHILDREN;
		//}

		final Object[] children;
		if (element instanceof IFolder) {
			children = fSelection.toArray();
		} else if (element instanceof IFile[])
		{
			Iterator<IFile> iter = fSelection.iterator();
			ArrayList<IFile> selectedFileList = new ArrayList<IFile>();
			while (iter.hasNext())
			{
				selectedFileList.add(iter.next());
			}
			IFile[] fileArray = new IFile[selectedFileList.size()];
			selectedFileList.toArray(fileArray);
			//children = (IFile[])element;
			//children = (IFile[])(fSelection.toArray());
			children = fileArray;
		}
	
		else {
			children = super.getChildren(element);
		}

		final List<Object> result = new ArrayList<Object>();
		IFile otherFile = null;
		IFile selectedFile = null;
		int deletedCount = 0;
		int newCount = 0;
		int changedCount = 0;
		int time = 0;
		for (int i = 0; i < children.length; i++) {
			final Object object = children[i];
			if (object instanceof IAdaptable) {
				selectedFile = (IFile) ((IAdaptable) object)
						.getAdapter(IFile.class);
				if (selectedFile != null && ResultDifferencesUtil.isValidFile(selectedFile)){

					// the left folder was set as input for the result explorer
					// that is why the selection contains the left xcas file.
					// both folders contain the same number of files which have
					// equal names.
					// because of that a new IFile is created here by using the
					// path of the right analysis result folder.
					// this is necessary for calling the AnalysisStoreReader
					// methods.
					otherFile = getOtherFile(selectedFile);
					int[] fileLevelCounts = null;
					if (CollDiffModel.isLeftFile(selectedFile))
					{
						fileLevelCounts =fAnalysisStoreReader.getFileLevelTotalCounts (selectedFile,otherFile);
					}
					else
					{
						fileLevelCounts =fAnalysisStoreReader.getFileLevelTotalCounts (otherFile,selectedFile);
					}
						newCount=fileLevelCounts[0];
						deletedCount=fileLevelCounts[1];
						changedCount=fileLevelCounts[2];
					
//					deletedCount = fAnalysisStoreReader
//							.getDiffDeletedAnnotations(xcasId1, xcasId2);
//					newCount = fAnalysisStoreReader.getDiffNewAnnotations(
//							xcasId1, xcasId2);
//					changedCount = fAnalysisStoreReader
//							.getDiffChangedAnnotations(xcasId1, xcasId2);
					// don't use the processing time
					time=0;
//					time = fAnalysisStoreReader.getDiffProcessingTime(xcasId1,
//							xcasId2);
        
                     					
					final CollDiffAnnotationContainer newContainer = new CollDiffAnnotationContainer(
							null, new IFile[] { otherFile, selectedFile },
							deletedCount, newCount, changedCount, time);
					result.add(newContainer);
				}
			}
		}

		return result.toArray();
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

	/**
	 * 
	 * @param leftFolderFile
	 *            the xcas file of the result folder of chosen result in the
	 *            combobox on the left
	 * @return the xcas file of the result folder of chosen result in the
	 *         combobox on the right
	 */
	public IFile getOtherFile(IFile file) {

		IPath newFilePath = new Path(""); //$NON-NLS-1$
		CollDiffModel collDiffModel = CollDiffModel.getInstance();
		// This replacing is done below because it is not just strf vs strf comparison that can happen
		String fileName = file.getName();
		
		String parentName = ((IFolder)(file.getParent())).getName();
		boolean leftFolderFile = true;
		if (parentName.equals(collDiffModel.getRightFolderName()))
		{
			leftFolderFile = false;
		}
		if (leftFolderFile)
		{
			if(GoldStandardUtil.isGoldStandardFolder(collDiffModel.getRightFolder()))
			{
				// This is done because if it is gold standard folder, then all the file names would end in "lc"
				fileName=  fileName.replaceAll("strf", Constants.GS_FILE_EXTENSION); //$NON-NLS-1$ //$NON-NLS-2$
			}
			newFilePath = collDiffModel.getRightFolder().getProjectRelativePath().append(newFilePath).append(fileName);
			return collDiffModel.getRightFolder().getProject().getFile(newFilePath);
		}
		else
		{
			newFilePath = collDiffModel.getLeftFolder().getProjectRelativePath().append(newFilePath).append(fileName);
			return collDiffModel.getLeftFolder().getProject().getFile(newFilePath);
		}
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
