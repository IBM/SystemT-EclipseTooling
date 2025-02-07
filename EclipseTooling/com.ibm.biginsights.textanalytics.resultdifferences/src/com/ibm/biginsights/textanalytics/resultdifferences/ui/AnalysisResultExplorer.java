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
package com.ibm.biginsights.textanalytics.resultdifferences.ui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.part.ViewPart;

import com.ibm.biginsights.textanalytics.resultdifferences.Activator;
import com.ibm.biginsights.textanalytics.resultdifferences.Messages;
import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollDiffAnalysisExplorerLabelProvider;
import com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView;

/**
 * A simple TreeViewer that displays files within a certain directory and uses
 * the CAS Viewer as file opener
 * 
 * 
 * 
 */
public class AnalysisResultExplorer implements ISelectionProvider{



	
	/** The Section that is used as control */
	protected final Section fSection;

	/** The TreeViewer with checkboxes used for displaying files */
	protected final ContainerCheckedTreeViewer fTreeViewer;

	/** A list of selection listeners */
	private final ListenerList listeners = new ListenerList();

	private int selectionStart=0;
	
	private int selectionSize=20;
	
    private int selectionEnd=0;
    
    public void setSelectionSize(int size){
    	this.selectionSize=size;    	
    }
    public int getSelectionSize(){
    	return this.selectionSize;
    }

    public int getSelectionStart(){
    	if (this.selectionStart==this.selectionEnd && this.selectionStart==0){
    		return 0; // was 100; TODO
    	}
    	return this.selectionStart+1;
    }

    public int getSelectionEnd(){
    	return selectionStart+selectionSize<selectionEnd? selectionStart+selectionSize:selectionEnd ;
    }

    public int getSelectionMax(){
    	return selectionEnd ;
    }
    
	public void resetSelectionCount(){
		selectionStart=0;
		selectionEnd=0;
	}
	
	public void goForward (){
        if(selectionStart+selectionSize < selectionEnd) {
        	selectionStart+=selectionSize;
        }
	}

	public void goBackward (){
        if(selectionStart-selectionSize >=0) {
        	selectionStart-=selectionSize;
        }
	}

	public void goBegin (){
        if(selectionStart-selectionSize >=0) {
        	selectionStart=0;
        }
	}

	public void goEnd (){
        if(selectionStart+selectionSize <selectionEnd) {
        	int rest=selectionEnd % selectionSize;
            if (rest>0) {         	
		       selectionStart= selectionEnd - rest;
            } else {
            	selectionStart=selectionEnd-selectionSize;
            }
            
        }
	}
		
	public boolean forwardEnabled (){
        return (selectionStart+selectionSize < selectionEnd);
	}

	public boolean backwardEnabled (){
        return (selectionStart-selectionSize >=0);
	}

	public boolean beginEnabled (){
        return (selectionStart-selectionSize >=0);
	}

	public boolean endEnabled (){
        return(selectionStart+selectionSize <selectionEnd);        
	}

	
	/** A ContentProvider that returns only folders and .xcas files */
	protected class FilteredWorkbenchContentProvider extends
			WorkbenchContentProvider {
		public Object[] getChildren(final Object element) {
			Object[] children= null; 
			List<Object> result = new ArrayList<Object>();
			if (element instanceof IFile[])
			{
				children = ((IFile[]) element);
			}
			else
			{
				children = super.getChildren(element);
			}
			for (int i = 0; i < children.length; i++) {
				final Object child = children[i];
/*				if (child instanceof IFolder) {
					result.add(child);
				}
*/
				if (child instanceof IFile
								&& ( ResultDifferencesUtil.isValidFile((IFile)child))){
					result.add(child);
				}
			}
			return result.toArray();
		
		/*}
		catch (CoreException e)
		{
			e.printStackTrace();
			return null;
		}*/
	}
	}

	/**
	 * Constructor, creates a Section and puts a TreeViewer in
	 * 
	 * @param toolkit
	 *            FormToolkit to use for creating/adapting widgets
	 * @param parent
	 *            The parent composite
	 *            
	 * @param openListener
	 * 			  Listener which reacts on double click events on elements
	 * 			  of this explorer. Use CASViewOpenListener or CASDiffViewOpenListener.
	 * 
	 * @param view 
	 * 			  The view which contains this explorer, needed to decide which
	 * 			  description text has to be used
	 */
	public AnalysisResultExplorer(final FormToolkit toolkit,
			final Composite parent, IOpenListener openListener, ViewPart view) {

		resetSelectionCount();
		
		fSection = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR
				| Section.DESCRIPTION);
		
		/*if(view instanceof AnalysisResultsView){
		fSection
				.setDescription(Messages.getString("AnalysisResultExplorer_ResultExplorerIntro"));
		fSection.setText(Messages.getString("AnalysisResultExplorer_ResultExplorer"));
		}
		else*/ if(view instanceof CollectionDifferencesMainView){
			fSection
			.setDescription(Messages.getString("CollDiff_ResultExplorerIntro")); //$NON-NLS-1$
			fSection.setText(Messages.getString("CollDiff_ResultExplorer"));// Result Explorer //$NON-NLS-1$
			fSection.getDescriptionControl ().getAccessible  ().addAccessibleListener (new AccessibleAdapter() {
	
	      public void getName (AccessibleEvent e) {
	        e.result = Messages.getString("CollDiff_ResultExplorer") +"  " +Messages.getString("AnalysisResultExplorer_SelectAll_ShortCut")+" "+Messages.getString("AnalysisResultExplorer_DeselectAll_ShortCut");
	      }
	    });
		}

		fTreeViewer = new ContainerCheckedTreeViewer(fSection);
		fTreeViewer.setContentProvider(new FilteredWorkbenchContentProvider());
		fTreeViewer.setLabelProvider(new CollDiffAnalysisExplorerLabelProvider());
		fTreeViewer.setAutoExpandLevel(ContainerCheckedTreeViewer.ALL_LEVELS);
		addToolbar(fSection, toolkit, view);

		// Our selection is made up according to the checkbox state, notify
		// selection listeners when it changes
		fTreeViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(final CheckStateChangedEvent event) {
				notifyListeners();
			}
		});

		// Open files with the CAS Viewer
		fTreeViewer.addOpenListener(openListener);
		fSection.setClient(fTreeViewer.getControl());
	}

	/**
	 * Creates a ToolBar with actions for (de)selecting and (un)expanding all
	 * elements
	 * 
	 * @param section
	 *            The parent section
	 * @param toolkit
	 *            The toolkit for creating/adapting widgets
	 */
	public void addToolbar(final Section section, final FormToolkit toolkit, ViewPart view) {
		final Composite sectionToolbarComposite = toolkit.createComposite(
				section, SWT.NULL);
		sectionToolbarComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

		final ToolBarManager toolBarManager = new ToolBarManager();
		final ToolBar toolBar = toolBarManager
				.createControl(sectionToolbarComposite);

		toolBar.setBackground(section.getTitleBarGradientBackground());

		/*	// This has been commented out because expand doesnt make sense since we are displaying event nested folders as 
		 * single filename. Keeping this commented out so that in future if it is required, we can use it. 	
		*toolBarManager.add(ActionUtilities
				.createTreeExpansionDropDown(fTreeViewer));
		new ToolItem(toolBar, SWT.SEPARATOR);
*/
		
		IAction selectAction = new Action(
		Messages.getString("AnalysisResultExplorer_SelectAll"), Activator //Select all //$NON-NLS-1$
				.getImageDescriptor("selectall.gif")) { //$NON-NLS-1$
			public void run() {
				fTreeViewer.setAllChecked(true);
				notifyListeners();
			}
		};
		selectAction.setActionDefinitionId("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.SelectAll");
		IAction deSelectAction = new Action(
				Messages.getString("AnalysisResultExplorer_DeselectAll"), //$NON-NLS-1$
				Activator.getImageDescriptor("deselectall.gif")) { //$NON-NLS-1$
					public void run() {
						fTreeViewer.setAllChecked(false);
						notifyListeners();
					}
				};
		deSelectAction.setActionDefinitionId("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.DeSelectAll");
		view.getViewSite().getActionBars().setGlobalActionHandler("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.SelectAll", selectAction);
		view.getViewSite().getActionBars().setGlobalActionHandler("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.DeSelectAll", deSelectAction);
		toolBarManager.add(selectAction);
		toolBarManager.add(deSelectAction);
		toolBarManager.update(true);
		section.setTextClient(sectionToolbarComposite);
	}

	/**
	 * Notify selection listeners that the selection has changed
	 */
	protected void notifyListeners() {
		resetSelectionCount();
//		long time1 = System.currentTimeMillis();

		fireSelectionChanged(new SelectionChangedEvent(
				AnalysisResultExplorer.this, getSelection()));
//		long time2 = System.currentTimeMillis();

//	log.error("\nnotify: "+(time2-time1) +"\n");

	}

	/**
	 * @return The TreeViewer used for displaying files
	 */
	public ContainerCheckedTreeViewer getTreeViewer() {
		return fTreeViewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(
			final ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		final Control control = fTreeViewer.getControl();
		if (control == null || control.isDisposed()) {
			return TreeSelection.EMPTY;
		}

		final List<Object> items = new ArrayList<Object>();
		// count the number of files in the collection
		selectionEnd=0;
		collectChecked(items, fTreeViewer.getTree());
		final ArrayList<Object> list = new ArrayList<Object>(items.size());
		// show only a certain amount of files, not all, 
		// so forward backward of the selection is possible.
		//selectionEnd=items.size();
		//for (int i = 0; i < items.size(); i++) {
		int i=selectionStart;
		int k=selectionStart;
		// k counts all the files, i counts all elements in the selection
		// including folders
		while (k<selectionSize+selectionStart && i<items.size() && k<selectionEnd) {
			TreeItem item = (TreeItem) items.get(i);
			if (item.getChecked() && item.getData() != null) {
				final LinkedList<Object> segments = new LinkedList<Object>();
				while (item != null) {
					final Object segment = item.getData();
					Assert.isNotNull(segment);
                    // count the number of files in the selection 
					// so that we only count the files and not the folders
					if (segment instanceof IFile) {
						k++;
					}	
					segments.addFirst(segment);
					item = item.getParentItem();
				}
				list.add(new TreePath(segments.toArray()));
				i++;
			}
		}

		return new TreeSelection((TreePath[]) list.toArray(new TreePath[list
				.size()]), fTreeViewer.getComparer());
	}

	/**
	 * Update the provided list with all checked TreeItems starting at the
	 * provided root parent
	 * 
	 * @param result
	 *            The list to fill
	 * @param parent
	 *            The root widget to start with
	 */
	private void collectChecked(List<Object> result, Widget parent) {
		TreeItem[] items = new TreeItem[0];

		if (parent instanceof TreeItem) {
			items = ((TreeItem) parent).getItems();
		}
		if (parent instanceof Tree) {
			items = ((Tree) parent).getItems();
		}
	    // coun the number of files in the whole selection
		for (int i = 0; i < items.length; i++) {
			final Item item = items[i];
			if (item instanceof TreeItem && ((TreeItem) item).getChecked()) {
			//if (item instanceof IFile ) {
				result.add(item);
				final Object segment = item.getData();
				Assert.isNotNull(segment);
			    // only if the segment is a file
				if (segment instanceof IFile) {
					selectionEnd++;				
				}
			}
			collectChecked(result, item);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(
			final ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			fTreeViewer.setCheckedElements(sselection.toArray());
			fTreeViewer.setSelection(selection);
		}
	}
	
	public void setAllChecked(boolean state){
		// selection changed, so we have to reset the count for forward backward
		// resetSelectionCount(); 
		fTreeViewer.setAllChecked(state);
	}

	public void fireSelectionChanged(final SelectionChangedEvent event) {
		// selection changed, so we have to reset the count for forward backward
        // resetSelectionCount();
		final Object[] currentListeners = listeners.getListeners();
		for (int i = 0; i < currentListeners.length; ++i) {
			final ISelectionChangedListener listener = (ISelectionChangedListener) currentListeners[i];
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					listener.selectionChanged(event);
				}
			});
		}
	}
}
