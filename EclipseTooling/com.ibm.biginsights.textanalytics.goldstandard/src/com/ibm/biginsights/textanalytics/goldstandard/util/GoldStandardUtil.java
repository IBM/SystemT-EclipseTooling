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
package com.ibm.biginsights.textanalytics.goldstandard.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.model.AnnotationType;
import com.ibm.biginsights.textanalytics.goldstandard.ui.GSFileViewer;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldType;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.AbstractTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeParent;
import com.ibm.biginsights.textanalytics.treeview.view.AQLResultTreeView;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class GoldStandardUtil {


	
	private static final ILog logger = LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID);
	
	/**
	 * Splits the annotation type Strings into AnnotationType objects.
	 * 
	 * @param annotationTypes
	 * 		Collection of annotation types for a given project
	 * @return 
	 * 		An array of annotation types encoded in the input parameter
	 */
	public static AnnotationType[] splitAnnotationTypes(String annotationTypes){
		if(StringUtils.isEmpty(annotationTypes)){
			return new AnnotationType[0];
		}
		ArrayList<AnnotationType> listAnnotationTypes = new ArrayList<AnnotationType>();
		
		//remove line separators first
		String lineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
		annotationTypes = annotationTypes.replace(lineSeparator, "").trim(); //$NON-NLS-1$
		
		//START: Validation checks
		if(!annotationTypes.startsWith("{")){
			return new AnnotationType[0];
		}
		
		if(!annotationTypes.endsWith("}") && !annotationTypes.endsWith("};")){
			return new AnnotationType[0];
		}
		//END: validation checks
		
		String allAnnotationTypes[] = annotationTypes.split(";"); //$NON-NLS-1$
		for (String annotationType : allAnnotationTypes) {
			annotationType = annotationType.replace("{", "").replace("}", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			listAnnotationTypes.add(AnnotationType.toObject(annotationType));
		}
		return listAnnotationTypes.toArray(new AnnotationType[listAnnotationTypes.size()]);
	}
	
	/**
	 * Retrieves annotation types for a given project, from the preference store
	 * @param projectName
	 * @return
	 */
	public static AnnotationType[] getAnnotationTypes(PreferenceStore prefStore, String gsName){
		if(StringUtils.isEmpty(gsName)){
			return new AnnotationType[0];
		}
		
		String PARAM_ANNOTATION_TYPES = Constants.GS_ANNOTATION_TYPES;
		
		if(prefStore == null){
			String msg = Messages.GoldStandardUtil_GS_NOT_CONFIGURED;
			String formattedMsg = MessageUtil.formatMessage(msg, gsName);
			logger.logAndShowError(formattedMsg);
			return new AnnotationType[0];
		}
		String prefAnnotationTypes = prefStore.getString(PARAM_ANNOTATION_TYPES);
		if(StringUtils.isEmpty(prefAnnotationTypes)){
			return new AnnotationType[0];
		}
		return splitAnnotationTypes(prefAnnotationTypes);
	}
	
	/**
	 * Retrieves annotation types for a given project, from the preference store
	 * @param projectName
	 * @return
	 */
	public static AnnotationType[] getAnnotationTypesByProjectName(String projectName, String gsName){
		if(StringUtils.isEmpty(projectName) || StringUtils.isEmpty(gsName)){
			return new AnnotationType[0];
		}
		PreferenceStore prefStore = getPreferenceStore(projectName, gsName);
		return getAnnotationTypes(prefStore, gsName);
	}
	
	/**
	 * Retrieves annotation types for a given gsFolder, from the preference store
	 * @param projectName
	 * @return
	 */
	public static AnnotationType[] getAnnotationTypes(IFolder gsFolder){
		if(gsFolder == null){
			return new AnnotationType[0];
		}
		PreferenceStore prefStore = getGSPrefStore(gsFolder);
		return getAnnotationTypes(prefStore, gsFolder.getName());
	}
	
	/**
	 * Retrieves a reference to active editor
	 * @return
	 */
	public static EditorInput getActiveEditorInput(){
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		EditorInput editorInput = (EditorInput)editor.getEditorInput();
		return editorInput;
	}
	
	/**
	 * Retrieves a reference to the Labeled Collection
	 * @return
	 * 		GSFileViewer object for currently opened .lc file
	 */
	public static GSFileViewer getGSEditor(){
		return (GSFileViewer)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
	}
	
	/**
	 * Retrieves the Gold standard folder for the input gsFile
	 * @param gsFile
	 * @return
	 * 		Gold standard folder for the input gsFile
	 */
	public static IFolder getGoldStandardFolder(IFile gsFile){
		IProject project = gsFile.getProject();
		IResource parent = gsFile.getParent();
		while(!isGoldStandardParentDir((IFolder)parent.getParent())){
			parent = parent.getParent();
			if(parent.getName().equals(project.getName())){
				return null;
			}
		}
		return (IFolder)parent;
	}

	/**
	 * Writes the model to the gsFile
	 * @param model
	 * @param gsFile
	 * @return
	 * 	 true, if serialization is successful, false, otherwise.
	 */
	public static boolean serializeModel(SystemTComputationResult model, IFile gsFile) {
		Serializer ser = new Serializer();
		return ser.writeModelToFile(gsFile, model);
	}
	
	/**
	 * Creates the default GSFolder under the parent folder
	 * @param gsParentFolder
	 * @param monitor
	 * @return
	 * 		Returns reference to the created folder 
	 * @throws CoreException
	 */
	public static IFolder createDefaultGSFolder(IFolder gsParentFolder, IProgressMonitor monitor) throws CoreException {
		IFolder folder = null;
		
		int suffix = 1;
		while(true){
			folder = gsParentFolder.getFolder(Constants.GS_DIR_PREFIX+suffix); //$NON-NLS-1$
			if(!folder.exists()){
				folder.create(true, true, null);
				break;
			}
			suffix++;
		}
	
		//write default values to lc.prefs file
		PreferenceStore prefStore = getGSPrefStore(folder);
		prefStore.setValue(Constants.GS_DETECT_WORD_BOUNDARIES, true);
		prefStore.setValue(Constants.GS_LANGUAGE, "en");
		try {
			prefStore.save();
			folder.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (IOException e) {
			logger.logError(e.getMessage(), e);
		}
		
		return folder;
	}
	
	/**
	 * Returns the gsPerfStore file name for the given gsFolder
	 * @param gsFolder
	 * @return
	 */
	public static File getGSPrefStoreFile(IFolder gsFolder){
		String fileName = Constants.GS_PREF_FILE;
		return gsFolder.getFile(fileName).getLocation().toFile();
	}
	

	public static PreferenceStore getGSPrefStore(IFolder gsFolder){
		PreferenceStore store = new PreferenceStore(getGSPrefStoreFile(gsFolder).getAbsolutePath());
		try {
			if(gsFolder.getFile(Constants.GS_PREF_FILE).exists()){
				store.load();
			}
			return store;
		} catch (IOException e) {
			logger.logError(e.getMessage());
		}
		return null;
	}
	
	public static PreferenceStore getPreferenceStore(String project, String gsName){
		IFolder gsParentFolder = getGSParentFolder(ProjectUtils.getProject(project), true);
		return getGSPrefStore(gsParentFolder.getFolder(gsName));
	}
	
	/**
	 * Returns the selected project
	 * @return
	 */
	public static IProject getSelectedProject(){
		IProject selectedProject = null;
		try {
			selectedProject = ProjectUtils.getSelectedProject();
		} catch (Exception e) {
			//do nothing
		}
		
		if(selectedProject == null){
			try {
				selectedProject = ((IFile)GoldStandardUtil.getActiveEditorInput().getUserData()).getProject();
			} catch (Exception e) {
				//do nothing
			}
		}
		return selectedProject;
	}
	
	/**
	 * Returns the image represented by the file path
	 * @param filepath
	 * @return
	 */
	public static Image getImage(String filepath){
		Bundle bundle = GoldStandardPlugin.getDefault().getBundle();
		URL url = FileLocator.find(bundle, new Path(filepath), null);
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(url); 
		return descriptor.createImage();
	}
	
	public static int getFieldIndex(String[] fieldNames, String fieldName) {
		if(fieldNames == null || fieldName == null){
			return -1;
		}else{
			for (int i = 0; i < fieldNames.length; i++) {
				if(fieldName.equals(fieldNames[i])){
					return i;
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Closes the annotation editor and reopens it. 
	 * This is the only known way to refresh both the editor contents as well as the treeView.
	 */
	public static IWorkbenchPage reopenGSEditor(IFile gsFile, EditorInput editorInput) {
		final String docID = editorInput.getName();
	    return reopenGSEditor(gsFile, docID);
	}
	
	//Fix for bug #17355: Labeled collection editor jumps to beginning of the file when annotating a long document
	/**
	 * Reopen the GS editor and jump to the last known offset location.
	 */
	public static IWorkbenchPage reopenGSEditor(IFile gsFile, EditorInput editorInput, int spanBegin, int spanEnd) {
	  IWorkbenchPage page = reopenGSEditor(gsFile, editorInput);
	  if(page != null){
	      //opening in the same location as annotated
	      ITextEditor editor = (ITextEditor) page.getActivePart();
	      editor.selectAndReveal(spanBegin, spanEnd - spanBegin);
	  }
	  return page;
    }
	
	public static IWorkbenchPage reopenGSEditor(IFile gsFile, String docID){
		IWorkbenchPage page = getActiveWorkbenchPage();
		if(page != null){
		  final IViewReference prevView = getViewReference (docID);  // Using getViewReference method. 
			if(prevView != null){
				page.hideView(prevView);
			}
			try {
				page.openEditor(new FileEditorInput(gsFile), Constants.GS_EDITOR_ID);
			} catch (PartInitException e) {
				logger.logError(e.getMessage());
			}
		}
		return page;
	}
	
	public static void reopenGSFileIfAlreadyOpened(IFile file) throws PartInitException {
		String docID = file.getFullPath().toString();
		IWorkbenchPage page = GoldStandardUtil.getActiveWorkbenchPage();
		if(page != null){
		  final IViewReference prevView = getViewReference (docID); // Using getViewReference method.
			if(prevView != null){
				page.hideView(prevView);
				page.openEditor(new FileEditorInput(file), Constants.GS_EDITOR_ID);
			}
		}
	}
	
	private static IWorkbenchPage getActiveWorkbenchPage(){
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if(window != null){
	    	return window.getActivePage();
	    }else{
	    	return null;
	    }
	}
	
	/**
	 * Closes the annotation editor 
	 */
	public static void closeGSEditor(EditorInput editorInput) {
		final String docID = editorInput.getName();
	    IWorkbenchPage page = getActiveWorkbenchPage();
	    final IViewReference prevView = getViewReference (docID); // Using getViewReference method.
		page.hideView(prevView);
	}
	
	/**
	 * Checks whether the input folder is a Gold standard folder or not
	 * @param folder
	 * @return
	 * 	true, if the input folder is a GS folder, false otherwise
	 */
	public static boolean isGoldStandardFolder(IFolder folder) {
		if(folder == null || !folder.exists()){
			return false;
		}

		try {
			return folder.getFile(Constants.GS_PREF_FILE).exists();
		} catch (Exception e) {
			logger.logError(
					MessageUtil.formatMessage(Messages.GoldStandardUtil_UNABLE_TO_DECIDE_IF_GS_FOLDER, folder.getName()), e);
			return false;
		}
	}
	
	/**
	 * Checks whether the input folder is a Gold standard parent folder or not
	 * @param folder
	 * @return
	 * 	true, if the input folder is a GS parent directory, false otherwise
	 */
	public static boolean isGoldStandardParentDir(IFolder folder){
		try {
			/* Note: Do not check for folder.exists() because during delete event of GSParent folder
			 * the removedResource might not exist, when this method is invoked.
			 */
			if(folder == null){
				return false;
			}
			PreferenceStore prefStore = ProjectUtils.getPreferenceStore(folder.getProject());
			if(prefStore != null){
				return folder.getName().equals(prefStore.getString(Constants.LC_ROOT_DIR));
			}else{
				return false;
			}
		} catch (Exception e) {
			logger.logError(MessageUtil.formatMessage(Messages.GoldStandardUtil_UNABLE_TO_DECIDE_IF_GSPARENT_DIR, folder.getName()), e);
			return false;
		}
	}
	
	public static void setGoldStandardParentDir(IFolder folder){
		
		if(folder == null || !folder.exists()){
			return;
		}
		
		try {
			PreferenceStore prefStore = ProjectUtils.getPreferenceStore(folder.getProject());
			if(prefStore != null){
				prefStore.setValue(Constants.LC_ROOT_DIR, folder.getName());
				prefStore.save();
			}
		} catch (Exception e) {
			String formattedMsg = MessageUtil.formatMessage(
					"The system cannot set the following directory as a labeled document collection root directory: {0}", 
					folder.getName());
			logger.logAndShowError(formattedMsg, e);
		}
	}
	
	/*
	 * Obtains the tree view associated with the docID
	 */
	public static AQLResultTreeView getTreeView(String docID) {
		final String secondaryViewID = docID.replaceAll(":", "");
		final IViewReference prevView = getViewReference(secondaryViewID); 
	    AQLResultTreeView treeView = (AQLResultTreeView)prevView.getPart(true);
	    return treeView;
	}

	/*
	 * Obtains the view reference associated with the docID
	 */
	public static IViewReference getViewReference(String docID) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    IWorkbenchPage page = window.getActivePage();
	    String secondaryViewID = docID.replaceAll(":", "");
	    /* 
	     * Below change is to fix the defect 49783 (Related to eclipse 4.2.2 migration)
	     *  
	     * In eclipse 3.6.2, view Id was the ID mentioned while creating the views irrespective of the secondary id, but 
	     * In 4.2.2, view id is a combination of both view id and secondary id in the form viewId = viewId:secondatyId if secondary id exists.
	     * In order to find the proper view reference, we are making compound view ID and using to get the reference.
	     *  
	     */
	    
	    // final IViewReference prevView = page.findViewReference(AQLResultTreeView.ID, secondaryViewID); // Commented out to fix above issue.
	    // Above commented line is replaced with following lines of code.
	    // TODO : Given below is a work around for Eclipse 4.2.2 bug: 49783. Revisit this when upgrading to higher versions of Eclipse
	    //Begin: workaround
	    String viewId = AQLResultTreeView.ID+":"+secondaryViewID;                           
	    final IViewReference prevView = page.findViewReference(viewId, secondaryViewID);   
	    //End: workaround
	    return prevView;
	}
	
	
	/*
	 * Expands all manually added annotations
	 */
	public static void expandAllManualAnnotationTypes(AQLResultTreeView treeView) {
		TreeParent root = (TreeParent)treeView.getViewer().getInput();
		TreeParent tpAnnotations = (TreeParent) root.getChildren()[0];
		ITreeObject[] viewNodes = tpAnnotations.getChildren();
		TreeParent viewNode = null;
		for (ITreeObject node : viewNodes) {
			viewNode = (TreeParent)node;
			//if(viewNode.getName().endsWith(Constants.MANUAL_ANNOTATION_VIEW_SUFFIX)){
				treeView.getViewer().expandToLevel(viewNode, 2);
			//}
		}
	}
	
	/*
	 * Returns the checked elements of a given treeView
	 */
	public static Object[] getCheckedElements(AQLResultTreeView treeView) {
		return treeView.getViewer().getCheckedElements();
	}
	
	/*
	 * When the treeviewer is reopened, we need to restore the previously checked elements. This methods handles the restoration of checked elements
	 */
	public static void restoreCheckedElements(AQLResultTreeView treeView , Object[] checkedElements) {
		TreeParent root = (TreeParent)treeView.getViewer().getInput();
		
	    for (Object object : checkedElements) {
			AbstractTreeObject item = (AbstractTreeObject)object;
			String id = item.getId();
			ITreeObject elementToSelect = root.findById(id);
			if(elementToSelect != null){
				treeView.getViewer().setChecked(elementToSelect, true);
				
				/*
				 * Note: Need to explicitly fire an event to the listener, since TreeViewer.setChecked() does not fire events.
				 * Unless we fire events, the checked elements won't be highlighted in the annotation editor
				 * Also, we don't want to fire events if the tree element is a TreeParent, because there may be some child elements of TreeParent
				 * that should not get checked as a result of checking TreeParent, as those items may not have been checked earlier
				 */
				if(! (elementToSelect instanceof TreeParent)){
					CheckStateChangedEvent event = new CheckStateChangedEvent((ICheckable) treeView.getViewer(), elementToSelect, true);
					treeView.fireCheckStateChanged(event);
				}
			}
		}
	    treeView.getViewer().expandToLevel(3);
		
	}

	public static IFolder getGSParentFolder(IProject project, boolean createIfNotExists) {
		IFolder folder;
		boolean setGSParent = false;
		String lcRootDir = null;
		
		PreferenceStore prefStore = ProjectUtils.getPreferenceStore(project);
		if(prefStore != null){
			lcRootDir = prefStore.getString(Constants.LC_ROOT_DIR);
		}
		
		if(StringUtils.isEmpty(lcRootDir)){
			lcRootDir = Constants.GS_DEFAULT_PARENT_DIR;
			setGSParent = true;
		}
		
		folder = project.getFolder(lcRootDir);
		if(!folder.exists() && createIfNotExists){
			ProjectUtils.syncCreateFolder(folder);
		}
		if(setGSParent){
			setGoldStandardParentDir(folder);
		}

		return folder;
	}
	
	/**
	 * This is an utility method removes all non Span fieldnames, fieldtypes and fieldvalues from each row. It will be used when importing a result
	 * as a labeled collection
	 * @param model
	 */

	
	public static void  removeNonSpanTags(SystemTComputationResult model) {		
		OutputView[] views = model.getOutputViews();
		if(views != null){
			for (OutputView view : views) {
				
				// Two variables to store only span field types and field names
				ArrayList<FieldType> onlySpanFieldTypes = new ArrayList<FieldType>();
				ArrayList<String> onlySpanFieldNames = new ArrayList<String>();
				
				// Get all fieldtypes and fieldnames
				FieldType[] types = view.getFieldTypes();
				String[] names = view.getFieldNames();
				
				if(types != null){
					int indx=0;
					for (FieldType fieldType : types) {
						if(fieldType == FieldType.SPAN){
							// store only field types of type span and the corresponding name
							onlySpanFieldTypes.add(fieldType);
							onlySpanFieldNames.add(names[indx]);
						}
						else
						{
							// ignore nonspan fieldtypes
						}
						indx++;
					}
				}
				
				// Convert the arraylist to String[] and set it on the model
				String[] modifiedFieldNames = new String[onlySpanFieldNames.size()];
				onlySpanFieldNames.toArray(modifiedFieldNames);
				view.setFieldNames(modifiedFieldNames);
				
				// Convert the arraylist to String[] and set it on the model
				FieldType[] modifiedFieldTypes = new FieldType[onlySpanFieldTypes.size()];
				onlySpanFieldTypes.toArray(modifiedFieldTypes);
				view.setFieldTypes(modifiedFieldTypes);
				
				
				// REmove all non span field values in each row of the output view
				OutputViewRow[] rows = view.getRows();
				if(rows != null){
					for (OutputViewRow row : rows) {
						ArrayList<FieldValue> onlySpanFieldValues = new ArrayList<FieldValue>();
						FieldValue[] fVals = row.fieldValues;
						if(fVals != null){
							for (FieldValue fVal : fVals) {
								if(fVal instanceof SpanVal){
									onlySpanFieldValues.add(fVal);
								}else{//fVal != SpanVal
							}
							}//end: for each fVal
							FieldValue[] modifiedFieldValues = new FieldValue[onlySpanFieldValues.size()];
							onlySpanFieldValues.toArray(modifiedFieldValues);
							row.fieldValues = modifiedFieldValues;
						}//end: if fVals != null
					}//end: foreach row
				}//end: if rows != null
			}//end: for each view
		}//end: if views != null
	
	}

	/**
	 * This is an utility method used to remove all tags - fieldnames, types and values which
	 * are not of type Document.text.
	 * However if the number of data rows is zero then we will retain the field names & types.
	 * See defect 17933 for more.
	 * @param model
	 */
	public static void  removeNonDocumentTextTypeTags(SystemTComputationResult model) {		
		OutputView[] views = model.getOutputViews();
		if(views != null){
			for (OutputView view : views) {
				ArrayList<Integer> indexesOfNonDocumentTxtRows = new ArrayList<Integer>();
				// REmove all non span field values in each row of the output view
				OutputViewRow[] rows = view.getRows();
				if(rows != null){
					
					if (rows.length == 0)
					{
						continue; // skip this iteration - ie. retain this outputview and goto the next. 
					}
					int inx = 0;
					for (OutputViewRow row : rows) {
						ArrayList<FieldValue> onlyDocumentTxtParentSpanName = new ArrayList<FieldValue>();
						FieldValue[] fVals = row.fieldValues;
						if(fVals != null){
							for (FieldValue fVal : fVals) {
								if(fVal instanceof SpanVal){
									SpanVal spanVal = (SpanVal) fVal;
									if("Document.text".equals(spanVal.parentSpanName)){ //$NON-NLS-1$
										onlyDocumentTxtParentSpanName.add(fVal);
									}
									else
									{
										indexesOfNonDocumentTxtRows.add(new Integer(inx));
									}
								}else{//fVal != SpanVal
							}
							}//end: for each fVal
							FieldValue[] modifiedFieldValues = new FieldValue[onlyDocumentTxtParentSpanName.size()];
							onlyDocumentTxtParentSpanName.toArray(modifiedFieldValues);
							row.fieldValues = modifiedFieldValues;
						}//end: if fVals != null
						inx++;
					}//end: foreach row
				}//end: if rows != null
				
				
				// Two variables to store only span field types and field names
				ArrayList<FieldType> onlyDocumentTxtFieldTypes = new ArrayList<FieldType>();
				ArrayList<String> onlyDocumentTextFieldNames = new ArrayList<String>();
				
				// Get all fieldtypes and fieldnames
				FieldType[] types = view.getFieldTypes();
				String[] names = view.getFieldNames();
				
				if(types != null){
					int indx=0;
					for (FieldType fieldType : types) {
						if(indexesOfNonDocumentTxtRows.contains(new Integer(indx))){
							// store only field types of type span and the corresponding name
							onlyDocumentTxtFieldTypes.add(fieldType);
							onlyDocumentTextFieldNames.add(names[indx]);
						}
						else
						{
							// ignore nonspan fieldtypes
						}
						indx++;
					}
				}
				
				// Convert the arraylist to String[] and set it on the model
				String[] modifiedFieldNames = new String[onlyDocumentTextFieldNames.size()];
				onlyDocumentTextFieldNames.toArray(modifiedFieldNames);
				view.setFieldNames(modifiedFieldNames);
				
				// Convert the arraylist to String[] and set it on the model
				FieldType[] modifiedFieldTypes = new FieldType[onlyDocumentTxtFieldTypes.size()];
				onlyDocumentTxtFieldTypes.toArray(modifiedFieldTypes);
				view.setFieldTypes(modifiedFieldTypes);
				
			}//end: for each view
		}//end: if views != null
	
	}

	
}
