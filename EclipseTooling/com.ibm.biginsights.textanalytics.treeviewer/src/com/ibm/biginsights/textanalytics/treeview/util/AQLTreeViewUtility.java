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
package com.ibm.biginsights.textanalytics.treeview.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.treeview.Messages;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeParent;
import com.ibm.biginsights.textanalytics.treeview.view.AQLResultTreeView;


/**
 * This is an utility class for managing colours of the tree view
 * 
 *  Madiraju
 */

public class AQLTreeViewUtility {



	private static HashMap<String, String> tupleMarkerMap = new HashMap<String, String>();
	private static int index = 1;
	private static int MAX_ANNOT_MRKR_SPEC_LIMIT = 20;

	/*
	 * This method maintains the markers for each tuple name. If a marker is
	 * alloted to a tuple name, it returns that else it will add a new marker
	 * against that tuple The markerAnnotationSpecifications are predefined in
	 * plugin.xml. There are 20 currently. If the limit of 20 is reached, then
	 * the index is reset and new tuples after 20 are assigned the first marker
	 * spec.
	 * 
	 * This design approach of predefined marker annot specs has been used
	 * because there was no easy way to add dynamic extensions. There is a
	 * method - addContribution - but from its javadoc, its not stable yet
	 * 
	 * @param tupleName
	 * 
	 * @return
	 */
	public static String getMarkerForTupleName(String tupleName) {
		String markerName = ""; //$NON-NLS-1$
		if (tupleName.equals(Messages.getString("AQLTreeViewUtility_ANNOTATIONS")) == false) { //$NON-NLS-1$
			markerName = tupleMarkerMap.get(tupleName);
			if (markerName == null) {
				markerName = "com.ibm.biginsights.textanalytics.aql.annot" + index; //$NON-NLS-1$
				tupleMarkerMap.put(tupleName, markerName);
				index++;
				if (index > MAX_ANNOT_MRKR_SPEC_LIMIT) {
					index = 1;
				}

			}
		}
		return markerName;
	}

	/*
	 * Returns the checked parent elements of a given treeView 
	 */
	private static HashSet<String> getCheckedElements(CheckboxTreeViewer treeViewer) {
		Object[] checkedElements= treeViewer.getCheckedElements();
		HashSet<String> ids = new HashSet<String>();
		for (Object element : checkedElements)
		{
			if( (element instanceof TreeParent)){
				String parentId = ((TreeParent)element).getId();
				ids.add(parentId);
		}
		}
		return ids;
	}
	/**
	 * This is the static variable used to store the selected items of a tree to be 
	 * updated whenver user selects/deselects. And this is used for display on newer treeviews opened.
	 */
	private static  ConcurrentHashMap<String,String> selectedItems = new ConcurrentHashMap<String,String>();
	
	/** 
	 * This method is called from the TreeViewListener whenever a new parent is selected
	 * @param id
	 */
	public static void addParentID(String id) {
			selectedItems.put(id,id);
	}
	
	/** 
	 * This method is called from the TreeViewListener whenever a new parent is de-selected
	 * @param id
	 */
	public static void removeParentID(String id) {
			selectedItems.remove(id);
	}
	/**
	 * When the treeviewer is reopened, we need to restore the previously checked elements.
	 * This methods handles the restoration of checked elements.
	 * This method is called from the ResultEditorListener - whenever a treeview is opened or activated.
	 */
	public static void restoreCheckedElements(AQLResultTreeView treeView) {
	
		TreeParent root = (TreeParent)treeView.getViewer().getInput();
		if (root == null) return;
		HashSet<String> currentCheckedIDs = getCheckedElements(treeView.getViewer());
		
		// The first for loop here iterates through the current checked ids for this
		// particular view and if it is not there in the remembered selected items, then it unselects
		// This is applicable for example when user deselects 
	    for (String parentId : currentCheckedIDs) {
	    	if (selectedItems.contains(parentId) == false)
	    	{
				ITreeObject elementToSelect = root.findById(parentId);
				if((elementToSelect != null)){// && ((elementToSelect instanceof TreeParent))) {
					treeView.getViewer().setChecked(elementToSelect, false);
					CheckStateChangedEvent event = new CheckStateChangedEvent((ICheckable) treeView.getViewer(), elementToSelect, false);
					treeView.fireCheckStateChanged(event);
				}
	    	}
		}
	    // This second for loop here iterates through the remembered selected items and 
	    // if this particular treeview is not selected, it will check it
	    	Set<String> keySet = selectedItems.keySet();
		    for (String selectedID : keySet) {
		    	if (currentCheckedIDs.contains(selectedID) == false)
		    	{
					ITreeObject elementToSelect = root.findById(selectedID);
					if((elementToSelect != null)){// && ((elementToSelect instanceof TreeParent))) {
						treeView.getViewer().setChecked(elementToSelect, true);
						CheckStateChangedEvent event = new CheckStateChangedEvent((ICheckable) treeView.getViewer(), elementToSelect, true);
						treeView.fireCheckStateChanged(event);
					}
		    	}
	    }
	    treeView.getViewer().expandToLevel(3);
		
	}

	/**
	 * Get the AQL result tree view with given secondary id.<br>
	 * Since multiple AQL result tree views can be opened, we need to provide secondary id to uniquely identify an instance.
	 * @param secondaryID
	 * @return
	 */
	public static IViewPart getAQLResultTreeViewWithID(String secondaryID)
	{
	  IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	  // TODO : Given below is a work around for Eclipse 4.2.2 bug: 49783. Revisit this when upgrading to higher versions of Eclipse
    //Begin: workaround
    String viewId = secondaryID != null ? AQLResultTreeView.ID+":"+secondaryID : AQLResultTreeView.ID; //$NON-NLS-1$
	  IViewReference viewRef = page.findViewReference(viewId, secondaryID);
	  //End: workaround
	  if(viewRef != null){
    	return viewRef.getView (true);
	  }
	  return null;
	}
	
	/**
	 * This utility method is called from SystemRunJob.java to be used whenever a new execution is initiated - else previous run's selections
	 * are remembered and confusing to user to see elements selected
	 */
	public static void clearSelectedItems()
	{
		selectedItems = new ConcurrentHashMap<String,String>();		
	}
}
