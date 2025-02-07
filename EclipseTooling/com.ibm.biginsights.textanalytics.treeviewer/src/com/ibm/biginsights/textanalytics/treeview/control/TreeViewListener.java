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
package com.ibm.biginsights.textanalytics.treeview.control;

import static com.ibm.biginsights.textanalytics.treeview.model.TreeObjectType.SPAN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.ibm.biginsights.textanalytics.concordance.ui.ResultEditor;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.AbstractTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeParent;
import com.ibm.biginsights.textanalytics.treeview.util.AQLTreeViewUtility;
import com.ibm.biginsights.textanalytics.treeview.view.AQLResultTreeView;

public class TreeViewListener implements ICheckStateListener {


 
	private CheckboxTreeViewer viewer;

  private IAnnotationModel annotationModel;

  private ResultEditor editor;

  private String viewName;

  public TreeViewListener(CheckboxTreeViewer viewer, String viewName) {
    this.viewer = viewer;
    this.viewName = viewName;
  }

  /*
   * This method returns all the attribute names for and under the selected element as an array list
   * of strings
   * 
   * @param treeObject
   * 
   * @return
   */
  private ArrayList<String> getOutputViewAttributeNames(AbstractTreeObject treeObject) {
    ArrayList<String> attributeNames = new ArrayList<String>();
    if (treeObject instanceof TreeParent) {
      ITreeObject[] childNodes = ((TreeParent) treeObject).getChildren();
      for (int r = 0; r < childNodes.length; r++) {
        ArrayList<String> localAttributeNames = null;
        if (childNodes[r] instanceof TreeParent) {
          // recursive call to get the attribute names if it is of type parent
          localAttributeNames = getOutputViewAttributeNames((TreeParent) childNodes[r]);
          for (int p = 0; p < localAttributeNames.size(); p++) {
            attributeNames.add(localAttributeNames.get(p));
          }
        } else // They are leafnodes
        {
          if (childNodes[r].getType() == SPAN) {
            AbstractTreeObject entry = (AbstractTreeObject) childNodes[r];
            // Obtain the file from the selection.
            String attributeName = entry.getParent().getName();
            String outputViewName = entry.getParent().getParent().getName();
            attributeNames
                .add(outputViewName
                    + "." + attributeName + "." + entry.getText() + "." + entry.getStart() + "-" + entry.getEnd()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          }
        }
      }
    } else // This means that only the leaf object has been selected
    {
      if (treeObject.getType() == SPAN) {
        // Obtain the file from the selection.
        String attributeName = treeObject.getParent().getName();
        String outputViewName = treeObject.getParent().getParent().getName();
        attributeNames
            .add(outputViewName
                + "." + attributeName + "." + treeObject.getText() + "." + treeObject.getStart() + "-" + treeObject.getEnd()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      }
    }
    return attributeNames;
  }

  /*
   * This method is a utility method to assign the Annotation Model and editor of a file
   * 
   * @param file
   */
  private void getAnnotationModelAndEditorOfFile() {
    if (annotationModel == null) {
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      IWorkbenchPage[] pages = window.getPages();
      // viewName= StringUtils.decodeFileName(viewName);
      String documentID = ((EditorInput) AQLResultTreeView.getEditorForId(viewName)).getName();
      for (int j = 0; j < pages.length; j++) {
        IEditorPart[] allEditorParts = pages[j].getEditors();
        for (int l = 0; l < allEditorParts.length; l++) {
          if (allEditorParts[l] != null) {
            if (allEditorParts[l].getTitle().startsWith(documentID)) {
              if (allEditorParts[l] instanceof AbstractTextEditor) {
                this.editor = (ResultEditor) allEditorParts[l];
                IEditorInput input = editor.getEditorInput();
                annotationModel = editor.getDocumentProvider().getAnnotationModel(input);
                break;

              }
            }
          }
        }
      }
    }
  }
  
  private void putSpanTreeElementsInMap(Object checkedElement, Map<Annotation,Position> annotMap)
  {
	  AbstractTreeObject entry = (AbstractTreeObject) checkedElement;
      if (entry.getType() == SPAN) {
        // Get the start and end positions of the selected tuple
        // int start = this.editor.getWidgetOffset(entry.getStart());
        // int end = this.editor.getWidgetOffset(entry.getEnd());
        int start = entry.getStart();
        int end = entry.getEnd();
        // Store the annotations and positions in array lists. These will be used later when
        // displaying the view below
        // Also note that the last parameter for creating the annotation is the attribute name
        // so that
        // later while unchecking these can be easily identified and removed.
        String tupleName = entry.getParent().getName();
        String viewName = entry.getParent().getParent().getName();
        String markerName = AQLTreeViewUtility
            .getMarkerForTupleName(viewName + "." + tupleName); //$NON-NLS-1$
        Annotation annot = new Annotation(markerName, false, viewName
            + "." + tupleName + "." + entry.getText() + "." + start + "-" + end); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        Position pos = new Position(start, end - start);
        // annotationModel.addAnnotation(annot, pos);
        annotMap.put(annot, pos);
      }
  }
  
  
  private void putAllElementsInAnnotMap(Object[] checkedElements, Map<Annotation, Position> annotMap)
  {
	  for (int i = 0; i < checkedElements.length; i++) {
	        // start of selection block
	        if ((checkedElements[i]!=null)&&(checkedElements[i] instanceof AbstractTreeObject)) {
	            if (checkedElements[i] instanceof TreeParent)
	            {
	            	TreeParent parent = (TreeParent)checkedElements[i];
	            	ITreeObject[] children = parent.getChildren();
	            	putAllElementsInAnnotMap(children,annotMap);
	            }
	            putSpanTreeElementsInMap(checkedElements[i], annotMap);
	        }
	      }
  }

  @Override
  public void checkStateChanged(CheckStateChangedEvent event) {
    getAnnotationModelAndEditorOfFile();

    // If the item is checked . . .
    if (event.getChecked()) {
      // . . . check all its children
      Object checkedElement = event.getElement();
      viewer.setSubtreeChecked(checkedElement, true);
      viewer.refresh();
      {
    	 // This block of code will add the parent ids which
		 // are selected and store them in the AQLTreeViewUtility to remember user selection
		 if (event.getElement() instanceof TreeParent)
 		 {
				TreeParent parent = (TreeParent)checkedElement;
				ArrayList<String> al = getParentIDAndAllChildParentIds(checkedElement);
				for (int j=0;j<al.size();j++)
				{
					AQLTreeViewUtility.addParentID(al.get(j));
				}
			}
		}
      Map<Annotation, Position> annotMap = new HashMap<Annotation, Position>();
      Object[] checkedElements = viewer.getCheckedElements();
      putAllElementsInAnnotMap(checkedElements,annotMap);
      /*for (int i = 0; i < checkedElements.length; i++) {
        // start of selection block
        if ((checkedElements[i]!=null)&&(checkedElements[i] instanceof AbstractTreeObject)) {
            System.out.println("AbstractTreeObject [i]is " + ((AbstractTreeObject)checkedElements[i]).getText());
            if (checkedElements[i] instanceof TreeParent)
            {
            	System.out.println("It is a TreeParent and it is " + ((TreeParent)checkedElements[i]).getName());
            	TreeParent parent = (TreeParent)checkedElements[i];
            	ITreeObject[] children = parent.getChildren();
            	System.out.println("Length of the children is " + children.length);
            	for (int j=0;j<children.length;j++)
            	{
            		System.out.println("children[j] is " + children[j].getText());
            		if (children[j] instanceof TreeParent)
            		{
            			
            		}
            		putInMap(children[j], annotMap);
            	}
            }
            putInMap(checkedElements[i], annotMap);
        }
      }*/
      ((IAnnotationModelExtension) this.annotationModel).replaceAnnotations(null, annotMap);
     // viewer.refresh();

      // page.activate(editorPart);

    }// end of event.getChecked
    else // The checkbox has been unchecked
    {
      // . . . un check all its children
      Object uncheckedElement = event.getElement();
      viewer.setSubtreeChecked(uncheckedElement, false);
      viewer.refresh();
      {
			// This block of code will remove the parent ids which
			// are de-selected and store them in the AQLTreeViewUtility to remember user de-selection
		if (uncheckedElement instanceof TreeParent) {
			TreeParent parent = (TreeParent)uncheckedElement;
			ArrayList<String> al = getParentIDAndAllChildParentIds(parent);
			for (int j=0;j<al.size();j++)
			{
				AQLTreeViewUtility.removeParentID(al.get(j));
			}
		}
      }
      
      // This method below gets the names of all the attributes for the unchecked element and its
      // children
      // And uses those names later to remove the annotations.
      // This works because while creating the annotations, the text used will be the same attribute
      // names
      ArrayList<String> attributeNames = getOutputViewAttributeNames((AbstractTreeObject) uncheckedElement);
      getAnnotationModelAndEditorOfFile();
      Iterator<Annotation> iter = annotationModel.getAnnotationIterator();
      Annotation annot = null;
      List<Annotation> annotList = new ArrayList<Annotation>();
      while (iter.hasNext()) {
        annot = iter.next();
        if (attributeNames.contains(annot.getText())) // While creating the annotation, the text
                                                      // used is the attribute name
        {
          annotList.add(annot);
        }
      }
      ((IAnnotationModelExtension) this.annotationModel).replaceAnnotations(
          annotList.toArray(new Annotation[annotList.size()]), null);
      // page.activate(editorPart);
    }

    // Tell the AQL result tree view to update its dropdown list of view attributes
    AQLResultTreeView aqlTreeView = (AQLResultTreeView) AQLTreeViewUtility.getAQLResultTreeViewWithID (viewName);
    if (aqlTreeView != null)
      aqlTreeView.updateCheckedViewAttributes();
  }
  
  private ArrayList<String> getParentIDAndAllChildParentIds(Object uncheckedElement)
  {
	  ArrayList<String> al = new ArrayList<String>();
		TreeParent parent = (TreeParent)uncheckedElement;
		String parentID = parent.getId();
		al.add(parentID);
		ITreeObject[] children = parent.getChildren();
		for (Object child : children) {
			if (child instanceof TreeParent) {
				parentID = ((TreeParent)child).getId();
//				al.add(parentID);
				al.addAll(getParentIDAndAllChildParentIds(child));
//				AQLTreeViewUtility.removeParentID(parentID);
			}
		}
		return al;
  }
}
