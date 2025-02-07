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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.resultviewer.model.BoolVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldType;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.FloatVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.IntVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.StringVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.model.TextVal;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.treeview.Messages;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.TreeObjectType;
import com.ibm.biginsights.textanalytics.treeview.model.impl.BoolTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.FloatTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.IntTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.NullTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.ScalarListTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.SpanTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.StringTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TextTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeParent;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeScalarList;
import com.ibm.biginsights.textanalytics.treeview.util.AQLTreeViewUtility;
import com.ibm.biginsights.textanalytics.treeview.view.AQLResultTreeView;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class ResultEditorListener implements IPartListener, IStartup {


 
	public ResultEditorListener() {
    super();
  }

  @Override
  public void partOpened(IWorkbenchPart part) {
    if (isSystemtEditorObject(part)) {
      String docID = part.getTitle();
      ITextEditor editor = (ITextEditor) part;
      EditorInput input = (EditorInput) editor.getEditorInput();
      String secondaryViewID = docID.replaceAll(":", "");
      AQLResultTreeView.setModelForId(secondaryViewID,
        modelTreeForView(input.getModel(), input.getAqlTextID()));
      AQLResultTreeView.setEditorForViewId(secondaryViewID, editor.getEditorInput());
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      // TODO : Given below is a work around for Eclipse 4.2.2 bug: 49783. Revisit this when upgrading to higher versions of Eclipse
      //Begin: workaround
      String viewId = secondaryViewID != null ? AQLResultTreeView.ID+":"+secondaryViewID : AQLResultTreeView.ID; //$NON-NLS-1$
      final IViewReference prevView = page.findViewReference(viewId, secondaryViewID);
      //End: workaround
      if (prevView != null) {
        // Although the API says "hide", it does in fact
        // close the view
        page.hideView(prevView);
      }
      try {
        // Show tree view with treeParent on editor.
        AQLResultTreeView treeView = (AQLResultTreeView)page.showView(AQLResultTreeView.ID, secondaryViewID, IWorkbenchPage.VIEW_ACTIVATE);
        String selectedAnnotationType= input.getSelectedAnnotationType();
        if (selectedAnnotationType != null)
        {
          // This condition occurs only whenever some treeview is opened from the ConcordanceView
          rememberInitialSelection(selectedAnnotationType,treeView);
        }
        AQLTreeViewUtility.restoreCheckedElements(treeView);
      } catch (PartInitException e1) {
        // TODO Auto-generated catch block
        LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(e1.getMessage(), e1);
      }
    }
  }

  /*
   * This is a private method called only when any TreeView is opened from the ConcordanceView
   * It remembers the selection from the ConcordanceView and stores it for restoration later 
   */
  private void rememberInitialSelection(String selectedAnnotationType,AQLResultTreeView treeView)
  {
    int indexOfDot = selectedAnnotationType.lastIndexOf("."); //For Modular AQL, view name section of span attribute name will also be qualified (<module>.<view>.<attrib>). Hence changing to lastIndexOf
    String selectedAttrName = selectedAnnotationType.substring(indexOfDot+1);
    String selectedOpView= selectedAnnotationType.substring(0,indexOfDot);

    TreeParent root = (TreeParent)treeView.getViewer().getInput();
    ITreeObject[] annotationRoot= root.getChildren();
    ITreeObject[] opViewChildren = ((TreeParent)annotationRoot[0]).getChildren();
    String opViewName= null;
    TreeParent opViewParent = null;
    String attrName = null;
    TreeParent opAttrParent = null;
    boolean breakLoop = false;
    for (int i=0;i<opViewChildren.length;i++)
    {
      opViewParent = (TreeParent)opViewChildren[i];
      opViewName = opViewParent.getName();
      if ((opViewName != null)&&(opViewName.equals(selectedOpView)))
      {
        ITreeObject[] opViewAttributes = opViewParent.getChildren();
        for (int j=0;j<opViewAttributes.length;j++)
        {
          opAttrParent = (TreeParent)opViewAttributes[j];
          attrName = opAttrParent.getName();

          if ((attrName!=null)&&(attrName.startsWith(selectedAttrName)))
          {
            AQLTreeViewUtility.addParentID(opAttrParent.getId());
            treeView.getViewer().setChecked(opAttrParent, true);
            CheckStateChangedEvent event = new CheckStateChangedEvent((ICheckable) treeView.getViewer(), opAttrParent, true);
            treeView.fireCheckStateChanged(event);
            breakLoop= true;
            break;
          }
        }
        if (breakLoop == true) break;
      }
    }
  }


  @Override
  public void partClosed(IWorkbenchPart part) {
    if (isSystemtEditorObject(part) || isTreeView(part)) {
      final String docID = part.getTitle();
      String secondaryViewID =  docID.replaceAll(":", "");
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      if (page != null) {
        // TODO : Given below is a work around for Eclipse 4.2.2 bug: 49783. Revisit this when upgrading to higher versions of Eclipse
        //Begin: workaround
        String viewId = secondaryViewID != null ? AQLResultTreeView.ID+":"+secondaryViewID : AQLResultTreeView.ID; //$NON-NLS-1$
        IViewReference prevView = page.findViewReference(viewId, secondaryViewID);
        //End: workaround 
        page.hideView(prevView);
        IEditorInput ieInput = AQLResultTreeView.getEditorForId(secondaryViewID);
        if (ieInput != null) {
          IEditorPart ePart = page.findEditor(ieInput);
          if (ePart != null && page.isPartVisible(ePart)) {
            page.closeEditor(ePart, false);
          }
        }
      }
    }
  }

  public static final TreeParent modelTreeForView(SystemTComputationResult model, int textID) {
    TreeParent invisibleNode = null;
    TreeParent outputViewsNode = null;
    List<ITreeObject> viewNodes = new ArrayList<ITreeObject>();
    int m = 0;
    // For each of the output view, we form a parent. ITs children are the
    // output views. Each output
    // view will have its own
    // children which are nothing but the attributes of the output view.
    // Each attribute will have
    // its children as the actual values or results
    if(model.getOutputViews() != null){
      for (OutputView view : model.getOutputViews()) {
        OutputViewRow[] rows = view.getRows();
        final int numCols = view.getFieldNames().length;
        String[] headers = new String[numCols];
        TreeObjectType[] treeObjectTypes = new TreeObjectType[numCols];
        com.ibm.biginsights.textanalytics.resultviewer.model.FieldType fieldType = null;
        for (int i = 0; i < numCols; i++) {
          final String fieldName = view.getFieldNames()[i];
          fieldType = view.getFieldTypes()[i];
          headers[i] = fieldName + " (" + fieldType.toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          treeObjectTypes[i] = fieldTypeToTreeObjectType(fieldType);
        }
        final int numRows = rows.length;
        List<ITreeObject> attrObjects = new ArrayList<ITreeObject>();
        for (int i = 0; i < numCols; i++) {
          if (view.getFieldTypes()[i] == FieldType.SPAN) {
            List<ITreeObject> leafObj = new ArrayList<ITreeObject>();
            for (int j = 0; j < numRows; j++) {

              //Note: Modified by Jayatheerthan
              //The following 'if' block that allows only those indexes less than fieldValueCount is introduced to support
              //multiple fieldNames in Gold Standard and to allow adding of the annotations one by one. Please let me know if this impacts any other feature
              int fieldValueCount = (rows[j].fieldValues == null) ? 0: rows[j].fieldValues.length;
              if(i < fieldValueCount && rows[j].fieldValues[i] instanceof SpanVal) {
                SpanVal spanVal = (SpanVal) rows[j].fieldValues[i];
                if (spanVal.sourceID == textID) {
                  leafObj.add(fieldToTreeObject(treeObjectTypes[i], rows[j].fieldValues[i],
                    model.getDocumentID(), model));
                }
              }
            }
            if (leafObj.size() > 0) {
              attrObjects.add(new TreeParent(TreeObjectType.PARENT, headers[i], leafObj
                .toArray(new ITreeObject[leafObj.size()])));
            }
          }
        }
        if (attrObjects.size() > 0) {
          viewNodes.add(new TreeParent(TreeObjectType.PARENT, view.getName(), attrObjects
            .toArray(new ITreeObject[attrObjects.size()])));
        }
        m++;
      }
      outputViewsNode = new TreeParent(
        TreeObjectType.PARENT,
        Messages.getString("AQLTreeViewUtility_ANNOTATIONS"), viewNodes.toArray(new ITreeObject[viewNodes.size()])); // views //$NON-NLS-1$
      ITreeObject[] rootNodeObj = new ITreeObject[1];
      rootNodeObj[0] = outputViewsNode;
      invisibleNode = new TreeParent(TreeObjectType.PARENT, "RootNode", rootNodeObj); // This node //$NON-NLS-1$
    }

    // does not get
    // displayed.
    // That is how
    // the
    // TreeViewer
    // works.
    return invisibleNode;
  }

  /**
   * Utility method to get the TreeObject based on the value from tuple
   * 
   * @param type
   * @param value
   * @param file
   * @return
   */
  private static final ITreeObject fieldToTreeObject(TreeObjectType type, FieldValue value,
    String documentID, SystemTComputationResult model) {

    // SPECIAL CASE: the value is a NULL object - display it appropriately!
    if (value == null) {
      return new TextTreeObject(Constants.NULL_DISPLAY_VALUE);
    }
    // END SPECIAl CASE

    switch (type) {
      case BOOL: {
        BoolVal b = (BoolVal) value;
        return new BoolTreeObject(b.val);
      }
      case FLOAT: {
        FloatVal f = (FloatVal) value;
        return new FloatTreeObject(f.val);
      }
      case INT: {
        IntVal i = (IntVal) value;
        return new IntTreeObject(i.val);
      }
      case STRING: {
        StringVal s = (StringVal) value;
        return new StringTreeObject(s.val);
      }
      case LIST: {
        return new ScalarListTreeObject(new TreeScalarList(TreeObjectType.TEXT));
      }
      case SPAN: {
        SpanVal span = (SpanVal) value;

        // SPECIAL CASE: the span value is really a NULL object - display it appropriately!
        if (Constants.SPAN_UNDEFINED_OFFSET == span.start
            || Constants.SPAN_UNDEFINED_SOURCE_ID == span.sourceID)
          return new TextTreeObject(Constants.NULL_DISPLAY_VALUE);
        // END SPECIAL CASE:

        return new SpanTreeObject(span.start, span.end, span.getText(model), null);
      }
      case TEXT: {
        TextVal text = (TextVal) value;
        return new TextTreeObject(text.val);
      }
      case NULL: {
        return new NullTreeObject();
      }
    }
    return null;
  }

  /**
   * Utility method to return the TreeObjecType based on the tuple field type
   * 
   * @param t
   * @return
   */
  private static TreeObjectType fieldTypeToTreeObjectType(
    com.ibm.biginsights.textanalytics.resultviewer.model.FieldType t) {
    switch (t) {
      case BOOL: {
        return TreeObjectType.BOOL;
      }
      case INT: {
        return TreeObjectType.INT;
      }
      case FLOAT: {
        return TreeObjectType.FLOAT;
      }
      case TEXT: {
        return TreeObjectType.TEXT;
      }
      case SPAN: {
        return TreeObjectType.SPAN;
      }
      case LIST: {
        return TreeObjectType.LIST;
      }
      case STRING: {
        return TreeObjectType.STRING;
      }
      case NULL: {
        return TreeObjectType.NULL;
      }
      default: {
        return null;
      }
    }
  }

  private static final boolean isSystemtEditorObject(IWorkbenchPart part) {
    if (part == null) {
      return false;
    }
    if (part instanceof ITextEditor) {
      ITextEditor editor = (ITextEditor) part;
      IEditorInput editorInput = editor.getEditorInput();
      return (editorInput instanceof EditorInput);
    }
    return false;
  }

  private static final boolean isTreeView(IWorkbenchPart part) {
    if (part == null) {
      return false;
    }
    if (part instanceof AQLResultTreeView) {
      return true;
    }
    return false;
  }

  @Override
  public void partDeactivated(IWorkbenchPart part) {
    // not used
  }

  @Override
  public void partActivated(IWorkbenchPart part) {
    if (isSystemtEditorObject(part) || isTreeView(part)) {
      final String docID = part.getTitle();
      String secondaryViewID =  docID.replaceAll(":", "");
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      IWorkbenchPage page = window.getActivePage();
      // TODO : Given below is a work around for Eclipse 4.2.2 bug: 49783. Revisit this when upgrading to higher versions of Eclipse
      //Begin: workaround
      String viewId = secondaryViewID != null ? AQLResultTreeView.ID+":"+secondaryViewID : AQLResultTreeView.ID; //$NON-NLS-1$
      final IViewReference prevView = page.findViewReference(viewId, secondaryViewID);
      //End: workaround
      if (prevView != null) {
        AQLResultTreeView treeView = (AQLResultTreeView) prevView.getPart(true);
        AQLTreeViewUtility.restoreCheckedElements(treeView);
        page.bringToTop(treeView);
        IEditorInput editorInput = AQLResultTreeView.getEditorForId(secondaryViewID);
        page.bringToTop(page.findEditor(editorInput));
      }
    }
  }

  @Override
  public void partBroughtToTop(IWorkbenchPart part) {
    // not used
  }

  @Override
  public void earlyStartup() {
    Display.getDefault().asyncExec(new Runnable() {

      @Override
      public void run() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService()
        .addPartListener(ResultEditorListener.this);

      }
    });
  }
}
