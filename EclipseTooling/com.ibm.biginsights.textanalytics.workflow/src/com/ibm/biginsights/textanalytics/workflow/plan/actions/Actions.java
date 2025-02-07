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
package com.ibm.biginsights.textanalytics.workflow.plan.actions;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.CreateAQLConceptDialog;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ExampleNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ExamplesFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelsFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.NodesGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeParent;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.LabelModel;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlTypes;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;

/**
 * defines common actions for the action plan such as copy, cut, and paste. the implementation of this action is very
 * straight forward
 * 
 * 
 */
public class Actions
{



//  public Action add_aql_rule;
  public Action copy, cut, paste;
  public Action sort_az, simplified_view, full_view;

//  public Action add_label, add_bf_label, add_cg_label, add_fc_label, add_final_label;
  public TreeObject last_used;

  public ExampleModel example;
  public LabelNode tagNode;
  public String title;

  protected ActionPlanView plan;

  public boolean copying;
  public boolean cutting;
  public List<TreeObject> movingElements;
  public List<TreeObject> movedElements;

  public Actions (ActionPlanView plan)
  {
    this.plan = plan;
    last_used = null;
    copying = false;
    cutting = false;
    makeActions ();
  }

  private void makeActions ()
  {
//    makeAddAqlRuleAction ();
    makeCopyAction ();
    makeCutAction ();
    makePasteAction ();
    makeSortAction ();
    makeSimplifiedViewAction ();
    makeFullViewAction ();
  }

  private void makeSimplifiedViewAction ()
  {
    simplified_view = new Action (Messages.simplified_extraction_plan_action_text, IAction.AS_CHECK_BOX) {
      public void run ()
      {
        // make sure that the action plan is properly initialized
        if (!plan.ready ()) return;

        ActionPlanView.setSimplifiedView (isChecked ());
        plan.getViewer ().refresh ();
      }     
    };
    simplified_view.setToolTipText (Messages.simplified_extraction_plan_action_tooltip);
    simplified_view.setImageDescriptor (ImageDescriptor.createFromImage (Icons.ALT_VIEW_ICON));
  }

  private void makeFullViewAction ()
  {
    full_view = new Action (Messages.full_view_extraction_plan_action_text, IAction.AS_CHECK_BOX) {
      public void run ()
      {
        // make sure that the action plan is properly initialized
        if (!plan.ready ()) return;

        TreeParent.toggleDisplayAll ();
        if (TreeParent.isDisplayAll ())
          full_view.setToolTipText (Messages.normal_view_extraction_plan_action_tooltip);
        else
          full_view.setToolTipText (Messages.full_view_extraction_plan_action_tooltip);

        plan.getViewer ().refresh ();
      }     
    };

    full_view.setToolTipText (Messages.full_view_extraction_plan_action_tooltip);
    full_view.setImageDescriptor (ImageDescriptor.createFromImage (Icons.EP_FULL_VIEW_ICON));
  }

  private void makeSortAction ()
  {
    sort_az = new Action (Messages.sort_alphabetically, IAction.AS_CHECK_BOX) {
      public void run ()
      {
        // make sure that the action plan is properly initialized
        if (!plan.ready ()) return;

        ActionPlanView.setSort (isChecked ());
        plan.getViewer ().refresh ();
      }     
    };
    sort_az.setToolTipText (Messages.sort_alphabetically_tooltip);
    sort_az.setImageDescriptor (ImageDescriptor.createFromImage (Icons.SORT_ICON));
  }

  private void makeCopyAction ()
  {
    copy = new Action () {
      public void run ()
      {
        // make sure that the action plan is properly initialized
        if (!plan.ready ()) return;

        ISelection selection = plan.getViewer ().getSelection ();

        movingElements = new ArrayList<TreeObject> ();
        movedElements  = new ArrayList<TreeObject> ();

        for (Object selObj : ((IStructuredSelection) selection).toList ()){
          TreeObject aSelectedObject = (TreeObject)selObj;
          movingElements.add ( aSelectedObject.deepCopy () );
          movedElements.add ( aSelectedObject );
        }

        copying = true;
        cutting = false;
      }
    };

    copy.setText (Messages.copy_text);
    copy.setToolTipText (Messages.copy_tootltip);
    copy.setImageDescriptor (ImageDescriptor.createFromImage (PlatformUI.getWorkbench ().getSharedImages ().getImage (
      ISharedImages.IMG_TOOL_COPY)));

  }

  private void makeCutAction ()
  {
    cut = new Action () {
      public void run ()
      {
        // make sure that the action plan is properly initialized
        if (!plan.ready ()) return;

        ISelection selection = plan.getViewer ().getSelection ();

        movingElements = new ArrayList<TreeObject> ();
        movedElements  = new ArrayList<TreeObject> ();

        for (Object selObj : ((IStructuredSelection) selection).toList ()){
          TreeObject aSelectedObject = (TreeObject)selObj;
          movingElements.add ( aSelectedObject );
          movedElements.add ( aSelectedObject );
        }

        cutting = true;
        copying = false;
      }
    };

    cut.setText (Messages.cut_text);
    cut.setToolTipText (Messages.cut_tootltip);
    cut.setImageDescriptor (ImageDescriptor.createFromImage (PlatformUI.getWorkbench ().getSharedImages ().getImage (
      ISharedImages.IMG_TOOL_CUT)));

  }

  private void makePasteAction ()
  {
    paste = new Action () {
      public void run ()
      {

        // make sure that the action plan is properly initialized
        if (!plan.ready () || movingElements == null || movingElements.isEmpty ()) return;

        ISelection selection = plan.getViewer ().getSelection ();
        TreeObject target = (TreeObject) ((IStructuredSelection) selection).getFirstElement ();

        for (TreeObject movingElement : movingElements) {
          handlePaste (target, movingElement);
        }

        try {
          plan.serialize ();
        }
        catch (Exception e) {
          Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
        }
        plan.getViewer ().refresh ();

        movingElements = null;
        cutting = false;
      }
    };

    paste.setText (Messages.paste_text);
    paste.setToolTipText (Messages.paste_tootltip);
    paste.setImageDescriptor (ImageDescriptor.createFromImage (PlatformUI.getWorkbench ().getSharedImages ().getImage (
      ISharedImages.IMG_TOOL_PASTE)));

  }

  /**
   * init the action that adds an aql rule to an existing tag element
   */
//  private void makeAddAqlRuleAction ()
//  {
//    add_aql_rule = new Action () {
//      public void run ()
//      {
//        // make sure that the action plan is properly initialized
//        if (!plan.ready ()) return;
//
//        last_used = null;
//        IProject project = AqlProjectUtils.getProject (ActionPlanView.projectName);
//
//        if (project != null) {
//
//          ISelection selection = plan.getViewer ().getSelection ();
//          TreeObject obj = (TreeObject) ((IStructuredSelection) selection).getFirstElement ();
//
//          if (obj instanceof AqlGroup) {
//            Shell shell = plan.getViewer ().getControl ().getShell ();
//
//            CreateAQLConceptDialog dialog = new CreateAQLConceptDialog (shell, (AqlGroup) obj);
//
//            final int rc = dialog.open ();
//
//            if (rc == Window.OK) {
//              String   name     = dialog.getName ();
//              AqlTypes type     = dialog.getType ();
//              boolean  doOutput = dialog.isDoOutput ();
//
//              AqlNode aqlNode = new AqlNode (name, type);
//
//              boolean inserted = aqlNode.insertTemplate (doOutput, type);
//              if (!inserted)
//                return;
//
//              // Add AQL node to the tree and the model only when we can insert the AQL statement successfully.
//              ((AqlGroup)obj).addChild2 (aqlNode);
//              serializeAndRefresh(obj);
//            }
//          }
//        }
//      }
//    };
//    add_aql_rule.setText (Messages.add_aql_rule_text);
//    add_aql_rule.setToolTipText (Messages.add_aql_rule_tootltip);
//    add_aql_rule.setImageDescriptor (ImageDescriptor.createFromImage (Icons.AQL_ICON));
//  }

  public Action makeAddAqlStatementAction (AqlFolderNode aqlFldr)
  {
    final AqlFolderNode aqlFolder = aqlFldr;
    Action add_aql_stmt = new Action () {
      public void run ()
      {
        // make sure that the action plan is properly initialized
        if (!plan.ready ())
          return;

        Shell shell = plan.getViewer ().getControl ().getShell ();

        CreateAQLConceptDialog dialog = new CreateAQLConceptDialog (shell, aqlFolder);

        final int rc = dialog.open ();

        if (rc == Window.OK) {
          String name = dialog.getName ();
          AqlTypes type = dialog.getType ();
          boolean doOutput = dialog.isDoOutput ();

          // non-modular
          if (!ProjectUtils.isModularProject (ActionPlanView.projectName))
            addNewAqlView (aqlFolder, name, type, doOutput, null, false);

          // modular
          else {
            IFolder moduleFolder = ProjectUtils.getTextAnalyticsSrcFolder (ActionPlanView.projectName).getFolder (dialog.getModuleName ());
            IFile aqlFile = moduleFolder.getFile (dialog.getAqlFileName ());

            if (!aqlFile.exists ())
              aqlFile = createNewAqlFile(aqlFile);

            if (aqlFile != null)
              addNewAqlView (aqlFolder, name, type, doOutput, aqlFile, dialog.isDoExport ());
            else {
              MessageDialog.openError (shell, Messages.create_aql_statement_dialog_title, Messages.create_aql_statement_dialog_aql_file_creation_error);
            }
          }
        }
      }

      private IFile createNewAqlFile (IFile aqlFile)
      {
        if (aqlFile != null) {
          try {
            String moduleName = aqlFile.getParent ().getName ();
            String moduleDecl = "module " + moduleName + ";\n\n";     // $NON-NLS-1$  // $NON-NLS-2$
            aqlFile.create (new ByteArrayInputStream (moduleDecl.getBytes ()), true, null);
            return aqlFile;
          }
          catch (CoreException e) {
            // if something wrong, simply return null
            return null;
          }
        }

        return null;
      }
    };

    setAddAqlActionText (add_aql_stmt, aqlFolder.getAqlType ());

    return add_aql_stmt;
  }

  private void setAddAqlActionText (Action action, AqlGroupType aqlGroupType)
  {
    DecorationOverlayIcon ovrlImageDescriptor = null;
    switch (aqlGroupType) {
      case BASIC:
        action.setText (Messages.add_bf_aql_statement_text);
        action.setToolTipText (Messages.add_bf_aql_statement_tooltip);
        ovrlImageDescriptor = new DecorationOverlayIcon(Icons.AQL_ICON, Icons.bfOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
        break;
      case CONCEPT:
        action.setText (Messages.add_cg_aql_statement_text);
        action.setToolTipText (Messages.add_cg_aql_statement_tooltip);
        ovrlImageDescriptor = new DecorationOverlayIcon(Icons.AQL_ICON, Icons.cgOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
        break;
      case REFINEMENT:
        action.setText (Messages.add_fc_aql_statement_text);
        action.setToolTipText (Messages.add_fc_aql_statement_tooltip);
        ovrlImageDescriptor = new DecorationOverlayIcon(Icons.AQL_ICON, Icons.fcOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
        break;
      case FINALS:
        action.setText (Messages.add_final_aql_statement_text);
        action.setToolTipText (Messages.add_final_aql_statement_tooltip);
        ovrlImageDescriptor = new DecorationOverlayIcon(Icons.AQL_ICON, Icons.fiOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
        break;
    }
    action.setImageDescriptor (ovrlImageDescriptor);
  }

  private void addNewAqlView (AqlFolderNode parent, String viewName, AqlTypes aqlType, boolean doOutput, IFile aqlFile, boolean doExport)
  {
    if (parent == null)   // AQL view has to be under certain parent.
      return;

    AqlNode aqlNode = new AqlNode (viewName);
    aqlNode.setAqlGroup (parent.getAqlType ());

    if (aqlFile == null)
      aqlFile = aqlNode.getFile ();

    boolean inserted = aqlNode.insertTemplate (doOutput, aqlType, aqlFile, doExport);
    if (!inserted)
      return;

    // Add AQL node to the tree and the model only when we can insert the AQL statement successfully.
    aqlNode.setAqlFilepathAndModuleName (aqlFile);
    parent.addChild2 (aqlNode);

    serializeAndRefresh(aqlNode);

  }

  public Action makeAddLabelAction ()
  {
    Action add_label = new Action () {
      public void run ()
      {
        // make sure that the action plan is properly initialized
        if (!plan.ready ())
          return;

        List<String> existingLabelNames = getLevelLabelNames (plan, null);
        IInputValidator validator = getLabelValidator (existingLabelNames);

        Shell shell = plan.getViewer ().getControl ().getShell ();

        InputDialog dialog = new InputDialog (shell, Messages.create_label_dialog_title, Messages.create_label_dialog_message, null, validator);

        if (dialog.open () == Dialog.OK) {
          String label = dialog.getValue ();

          if (!StringUtils.isEmpty (label)) {
            LabelNode subLabelNode = new LabelNode (new LabelModel (label));
            plan.getContentProvider ().addChild (subLabelNode);
          }

          serializeAndRefresh (null);
        }
      }
    };
    setAddLabelActionText (add_label, null);

    return add_label;
  }

  public Action makeAddLabelAction (NodesGroup nodesGroup)
  {
    final NodesGroup parent = nodesGroup;

    Action add_label = new Action () {
      public void run ()
      {
        // make sure that the action plan is properly initialized
        if (!plan.ready ()) return;

        IProject project = AqlProjectUtils.getProject (ActionPlanView.projectName);
        if (project != null) {
          List<String> existingLabelNames = getLevelLabelNames (plan, parent);
          IInputValidator validator = getLabelValidator (existingLabelNames);

          Shell shell = plan.getViewer ().getControl ().getShell ();

          InputDialog dialog = new InputDialog (shell, "Create Label", "Enter the label", null, validator); // TODO I18N

          if (dialog.open () == Dialog.OK) {

            String label = dialog.getValue ();
            if (!StringUtils.isEmpty (label)) {
              LabelNode subLabelNode = new LabelNode (new LabelModel (label));
              if (parent instanceof LabelNode)
                ((LabelNode) parent).addSubLabel (subLabelNode, null);
              else if (parent instanceof AqlGroup)
                ((AqlGroup) parent).getLabelsFolder ().addChild2 (subLabelNode);
              else // parent instanceof LabelsFolderNode
                ((LabelsFolderNode) parent).addChild2 (subLabelNode);
            }

            serializeAndRefresh (parent);
          }
        }
      }
    };

    // Create action label.
    setAddLabelActionText (add_label, parent);

    return add_label;
  }

  private void setAddLabelActionText (Action action, NodesGroup nodesGroup)
  {
    if (action == null)
      return;

    DecorationOverlayIcon ovrlImageDescriptor = null;

    if (nodesGroup == null) {
      action.setText (Messages.add_label_text);
      action.setToolTipText (Messages.add_label_tootltip);
      action.setImageDescriptor (ImageDescriptor.createFromImage (Icons.LABEL_ICON));
    }
    else if (nodesGroup instanceof LabelNode) {
      action.setText (Messages.add_direct_label_text);
      action.setToolTipText (Messages.add_direct_label_tootltip);
      action.setImageDescriptor (ImageDescriptor.createFromImage (Icons.LABEL_ICON));
    }
    else if (nodesGroup instanceof LabelsFolderNode) {
      action.setText (Messages.add_direct_label_text);
      action.setToolTipText (Messages.add_direct_label_tootltip);
      action.setImageDescriptor (ImageDescriptor.createFromImage (Icons.LABEL_ICON));
    }
    else {
      AqlGroupType aqlGroupType = nodesGroup.getAqlType ();
      switch (aqlGroupType) {
        case BASIC:
          action.setText (Messages.add_bf_label_text);
          action.setToolTipText (Messages.add_bf_label_tooltip);
          ovrlImageDescriptor = new DecorationOverlayIcon(Icons.LABEL_ICON, Icons.bfOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
          break;
        case CONCEPT:
          action.setText (Messages.add_cg_label_text);
          action.setToolTipText (Messages.add_cg_label_tooltip);
          ovrlImageDescriptor = new DecorationOverlayIcon(Icons.LABEL_ICON, Icons.cgOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
          break;
        case REFINEMENT:
          action.setText (Messages.add_fc_label_text);
          action.setToolTipText (Messages.add_fc_label_tooltip);
          ovrlImageDescriptor = new DecorationOverlayIcon(Icons.LABEL_ICON, Icons.fcOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
          break;
        case FINALS:
          action.setText (Messages.add_final_label_text);
          action.setToolTipText (Messages.add_final_label_tooltip);
          ovrlImageDescriptor = new DecorationOverlayIcon(Icons.LABEL_ICON, Icons.fiOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
          break;
      }

      action.setImageDescriptor (ovrlImageDescriptor);
    }
  }

  /**
   * Get the list of names of labels of the same level.<br>
   * If the NodesGroup object passed in is:<ul>
   * <li>null --> the top level labels
   * <li>LabelNode --> the 1st level sublabels, including all AQL group sublabels
   * <li>AqlGroup/LabelsFolderNode --> the 1st level sublabels of the parent label
   * </ul>
   * @param extrPlan
   * @param nodesGroup
   * @return
   */
  public static List<String> getLevelLabelNames (ActionPlanView extrPlan, NodesGroup nodesGroup)
  {
    List<String> labelNames = new ArrayList<String> ();
    List<LabelModel> allSubLabels = null;

    if (nodesGroup == null) {
      List<LabelNode> allSubLabelNodes = extrPlan.getContentProvider ().getRoots ();
      for (LabelNode lblNode : allSubLabelNodes) {
        allSubLabels = new ArrayList<LabelModel> ();
        allSubLabels.add (lblNode.toModel ());
      }
    }
    else {
      LabelModel lm = null;

      if (nodesGroup instanceof LabelNode)
        lm = ((LabelNode) nodesGroup).toModel ();
      else if (nodesGroup instanceof AqlGroup)
        lm = ((AqlGroup) nodesGroup).toModel ();
      else if (nodesGroup instanceof LabelsFolderNode)
        lm = ((LabelsFolderNode) nodesGroup).toModel ();

      if (lm != null)
        allSubLabels = lm.getAllSubLabels ();
    }

    if (allSubLabels != null) {
      for (LabelModel lblModel : allSubLabels) {
        labelNames.add (lblModel.getName ());
      }
    }

    return labelNames;
  }

  private IInputValidator getLabelValidator(List<String> labelNames)
  {
    final List<String> existingLabelNames = labelNames;
    IInputValidator validator = new IInputValidator() {

      @Override
      public String isValid (String labelName)
      {
        if (existingLabelNames.contains (labelName))
          return Messages.create_label_validation_message_duplicate_name;
        else
          return null;
      }
      
    };

    return validator;
  }

  
  private void serializeAndRefresh(TreeObject treeObject)
  {
    try {
      plan.serialize ();
    }
    catch (Exception e) {
      Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
    }

    plan.getViewer ().refresh ();

    if (treeObject != null)
      plan.getViewer ().expandToLevel (treeObject, TreeViewer.ALL_LEVELS);
  }

  /**
   * @param obj
   */
  private void handlePaste (TreeObject pasteTarget, TreeObject movingElement)
  {
    if (cutting) {
      TreeParent parent = movingElement.getParent ();
      parent.removeChild (movingElement);
      try {
        plan.serialize ();
      }
      catch (Exception e) {
        Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
      }
    }

    if (pasteTarget instanceof TreeParent) {

      //----- Moving element is a label -----//
      if (movingElement instanceof LabelNode) {
        LabelNode movingLabel = (LabelNode)movingElement;

        // label to label
        if (pasteTarget instanceof LabelNode)
          ((LabelNode) pasteTarget).addSubLabel (movingLabel, null);

        // label to label folder
        else if (pasteTarget instanceof LabelsFolderNode)
          ((LabelsFolderNode) pasteTarget).addChild (movingElement);

        // tag to aql folder
        else if (pasteTarget instanceof AqlGroup)
          ((AqlGroup)pasteTarget).getLabelsFolder ().addChild (movingLabel);
      }

      //----- Moving element is an example -----//
      else if (movingElement instanceof ExampleNode) {
        ExampleNode movingExample = (ExampleNode) movingElement;

        // example to label
        if (pasteTarget instanceof LabelNode)
          ((LabelNode) pasteTarget).addExample (movingExample);

        // example to Examples folder
        else if (pasteTarget instanceof ExamplesFolderNode) {
          ((ExamplesFolderNode) pasteTarget).addChild (movingExample);
        }
      }

      //----- Moving element is an AQL node -----//
      else if (movingElement instanceof AqlNode) {
        AqlNode movingAqlNode = (AqlNode) movingElement;

        if (pasteTarget instanceof AqlGroup)
          ((AqlGroup) pasteTarget).addChild (movingAqlNode); 
        else if (pasteTarget instanceof AqlFolderNode)
          ((AqlFolderNode) pasteTarget).addChild (movingAqlNode); 
      }
    }
  }

}
