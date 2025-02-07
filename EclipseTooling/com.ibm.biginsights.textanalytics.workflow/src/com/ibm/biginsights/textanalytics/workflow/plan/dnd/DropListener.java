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
package com.ibm.biginsights.textanalytics.workflow.plan.dnd;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.editors.TaggingEditor;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.AddExampleToLabelAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.DeleteAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.PasteAction;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelsFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.NodesGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ProjectNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeParent;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.AQLNodeModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.GroupType;

public class DropListener extends ViewerDropAdapter
{


 
	TreeViewer viewer;
  boolean deleteDraggedObjects = false;

  public DropListener (TreeViewer viewer)
  {
    super (viewer);
    this.viewer = viewer;
  }

  @Override
  public void drop (DropTargetEvent event)
  {
    // int location = this.determineLocation(event);
    event.detail = DND.DROP_COPY;
    Object obj = determineTarget (event);
    if (obj == null) return;

    if ( obj instanceof AqlFolderNode     ||
         obj instanceof AqlGroup          ||
         obj instanceof LabelNode         ||
         obj instanceof LabelsFolderNode  ||
         obj instanceof ProjectNode  ||
         isTargetExamples (obj)) {

      super.drop (event);

      if (deleteDraggedObjects) {
        ActionPlanView plan = AqlProjectUtils.getActionPlanView ();
        TreeObject[] draggedObjs = plan.getDraggedObjects ();
        if (draggedObjs != null && draggedObjs.length > 0) {
          DeleteAction delAction = new DeleteAction (draggedObjs);
          delAction.setConfirm (false);
          delAction.run ();
        }
      }

      AqlProjectUtils.getActionPlanView ().serializeAndRefresh ();
    }
  }

  @Override
  public boolean performDrop (Object data)
  {
    // handle drop of multiple objects
    if (data instanceof String[] || data instanceof TreeObject[]) {
      for (Object obj : (Object[]) data) {
        if (!performDrop (obj))
          return false;
      }
      return true;
    }

    Object target = this.getCurrentTarget ();

    // handle Strings
    if (data instanceof String) {
      // add a view from text selected
      if (target instanceof AqlGroup || target instanceof AqlFolderNode) {
        String[] viewInfo = AqlProjectUtils.getViewInCurrentEditor ((String) data);

        if (viewInfo != null) {
          AqlFolderNode aqlFolder = null;
          if (target instanceof AqlFolderNode)
            aqlFolder = (AqlFolderNode) target;
          else
            aqlFolder = ((AqlGroup)target).getAqlStatementsFolder ();

          AQLNodeModel aqlModel = new AQLNodeModel (viewInfo [0], "");
          aqlModel.setFileName (viewInfo [1]);

          if (ProjectUtils.isModularProject (ActionPlanView.projectName))
            aqlModel.setModuleName (viewInfo [2]);
          else
            aqlModel.setAqlfilepath (viewInfo [2]);

          AqlNode aqlNode = new AqlNode (aqlModel, aqlFolder);
          aqlFolder.addChild2 (aqlNode);

          AqlProjectUtils.getActionPlanView ().serializeAndRefresh ();
        }
      }
      // add a new example from text selected
      if (isTargetExamples (target)) {
        IEditorPart activeEditor = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getActiveEditor ();
        if (activeEditor instanceof TaggingEditor) {
          ActionPlanView plan = AqlProjectUtils.getActionPlanView ();
          LabelNode node = (LabelNode) ((TreeObject) target).getParent ();
          ExampleModel model = AqlProjectUtils.getDataFileForTaggingEditor ((String) data);
          AddExampleToLabelAction action = new AddExampleToLabelAction (model, node, plan);
          action.run ();
        }
      }
    }

    if (data instanceof TreeObject) {

      boolean isMoved = false;

      if (IsMovingLabel()) {

        LabelNode tgtLabelNode = (LabelNode)target;
        TreeParent parentNode = ((LabelNode)target).getParent ();

        // 'data' is not the actual source object, we need to get the actual one from parent. 
        TreeObject srcLabelNode = parentNode.getChildrenByLabel (((TreeObject)data).getLabel ());

        int relativePosToTarget = TreeParent.LOCATION_NONE;
        if (this.getCurrentLocation () == ViewerDropAdapter.LOCATION_BEFORE)
          relativePosToTarget = TreeParent.LOCATION_BEFORE;
        else if (this.getCurrentLocation () == ViewerDropAdapter.LOCATION_AFTER)
          relativePosToTarget = TreeParent.LOCATION_AFTER;

        if (relativePosToTarget != TreeParent.LOCATION_NONE) {
          isMoved = true;
          deleteDraggedObjects = false;
          parentNode.moveChild (srcLabelNode, tgtLabelNode, relativePosToTarget);
          AqlProjectUtils.getActionPlanView ().serializeAndRefresh ();
        }
      }

      if (!isMoved) {
        if (target instanceof ProjectNode) {
          if (data instanceof LabelNode)
            AqlProjectUtils.getActionPlanView ().getContentProvider ().addChild ((LabelNode)data);
        }
        else {
          PasteAction paste = new PasteAction ((TreeObject) data, (TreeObject) target);
          paste.run ();
          deleteDraggedObjects = true;
        }
      }
    }

    return true;
  }

  /**
   * Check if the DnD action is a moving of labels; i.e., the following condition holds:<br>
   * all dragged objects are labels and dropped to a place in the same Labels folder.
   * @return
   */
  private boolean IsMovingLabel ()
  {
    Object target = getCurrentTarget ();

    if (target instanceof LabelNode) {

      LabelNode tgtLabel = (LabelNode) target;
      TreeObject tgtParent = tgtLabel.getParent ();

      IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
      for (Object selObj : selection.toList ()) {
        // All selected (dragged) labels have to have the same parent with target label.
        if ( !(selObj instanceof LabelNode) ||
             ((LabelNode)selObj).getParent () != tgtParent )
          return false;
      }

      return true;
    }

    return false;
  }

  @Override
  public boolean validateDrop (Object target, int operation, TransferData transferType)
  {

    if (TextTransfer.getInstance ().isSupportedType (transferType)) {
      // if (target instanceof AqlGroup || isTargetExamples(target))
      return true;
    }

    if ((ActionPLanTransfer.getInstance ().isSupportedType (transferType))) { return true; }

    return false;
  }

  private boolean isTargetExamples (Object target)
  {
    if (target instanceof NodesGroup)
      if (((NodesGroup) target).getGroupType ().equals (GroupType.EXAMPLES)) return true;
    return false;
  }

}
