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
package com.ibm.biginsights.textanalytics.workflow.plan;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.Actions;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;
import com.ibm.biginsights.textanalytics.workflow.util.Styles;

/**
 * Wizard to create a new Label elemenet for the action plan
 * 
 *
 */
public class CreateLabelPage extends WizardPage
{



  private final String subtag_parent_message = Messages.create_label_page_parent_label;

  private LabelNode subtag_parent;

  private DialogInputValidator validator;

  private Text subtag_name_txt;

  private Text errorMessageText;

  private List<LabelNode> existingLabelNodes;

  private AddExampleWizard wiz;
//  private Button createAQLs;    // this is an old feature that we decided to take out in 1.5
                                  // Leave it here. We may want to turn it on in the future.

  private Object selectedObject;
  private FilteredTree labelTree;



  public CreateLabelPage (String pageName, AddExampleWizard wiz)
  {
    super (pageName);
    this.wiz = wiz;

    setTitle (Messages.create_label_wizard_page_title);
    ActionPlanView view = AqlProjectUtils.getActionPlanView ();

    if (view != null && view.ready ()) {
      existingLabelNodes = view.getTagNodes ();
      subtag_parent = view.getselectedTag ();
      validator = new DialogInputValidator ();
    }
  }

  @Override
  public void createControl (Composite parent)
  {

    Composite composite = new Composite (parent, SWT.NONE);
    setControl (composite);

    composite.setLayout (new GridLayout (1, false));
    composite.setLayoutData (new GridData (SWT.LEFT, SWT.TOP, true, true, 1, 1));

    errorMessageText = new Text (composite, SWT.READ_ONLY | SWT.WRAP);
    errorMessageText.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
    errorMessageText.setForeground (Styles.ATTENTION_RED);

    Composite data_section = new Composite (composite, SWT.NONE);
    data_section.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1));
    data_section.setLayout (new GridLayout (2, false));

    Label lblNewLabel = new Label (data_section, SWT.NONE);
    lblNewLabel.setText (Messages.create_label_page_label_message);

    subtag_name_txt = new Text (data_section, getInputTextStyle ());
    subtag_name_txt.setLayoutData (new GridData (GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

    Label type_label = new Label (data_section, SWT.NONE);
    type_label.setText (subtag_parent_message);
    type_label.setLayoutData (new GridData (GridData.VERTICAL_ALIGN_BEGINNING));

    labelTree = new FilteredTree (data_section, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new PatternFilter(), true);
    GridData gd = new GridData (SWT.FILL, SWT.CENTER, true, false, 1, 1);
    gd.heightHint = 150;
    labelTree.setLayoutData (gd);

    labelTree.getViewer ().setContentProvider(new LabelTreeContentProvider());
    labelTree.getViewer ().setLabelProvider(new LabelTreeLabelProvider());

    // Accessibility support
    labelTree.getFilterControl ().getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        e.result = subtag_parent_message;
      }
    });

    ActionPlanView extPlan = AqlProjectUtils.getActionPlanView ();
    labelTree.getViewer ().setInput (extPlan);

    addListenerToLabelTree();

    new Label (data_section, SWT.NONE);
    Label lblleaveEmptyTo = new Label (data_section, SWT.NONE);
    lblleaveEmptyTo.setText (Messages.create_label_page_info_message_1);

    new Label (data_section, SWT.NONE);
    Label lblUseDoubleClick = new Label (data_section, SWT.NONE);
    lblUseDoubleClick.setText (Messages.create_label_page_info_message_2);

    subtag_name_txt.addModifyListener (new ModifyListener () {
      public void modifyText (ModifyEvent e)
      {
        validateInput ();
        wiz.setNewLabelName (subtag_name_txt.getText ());
      }
    });

    // Set the error message text
    // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=66292
    validateInput ();
    subtag_name_txt.setFocus ();
  }

  public LabelNode getParent ()
  {
    return subtag_parent;
  }

  private void addListenerToLabelTree()
  {
    TreeViewer viewer = labelTree.getViewer ();

    viewer.addSelectionChangedListener (new LabelTreeSelectionChangeListener());

    viewer.getTree ().addKeyListener (new LabelTreeKeyAdapter ());

    viewer.addDoubleClickListener (new IDoubleClickListener () {
      public void doubleClick (DoubleClickEvent event)
      {
        if (selectedObject instanceof LabelNode) {
          labelTree.getFilterControl ().setText (((LabelNode)selectedObject).toString ());
          wiz.setNewLabelParentLabel ((LabelNode)selectedObject);
        }
      }
    });

    labelTree.getFilterControl ().addModifyListener (new ModifyListener () {
      public void modifyText (ModifyEvent e)
      {
        validateInput ();
      }
    });
  }

  protected void validateInput ()
  {
    String errorMessage = null;
    LabelNode newLabelParentLabel = null;

    //-------- Validate label name field --------//
    if (validator != null) {
      errorMessage = validator.isValid (subtag_name_txt.getText ());
    }

    //-------- Validate parent label field --------//
    if (errorMessage == null) {
      Text parentText = labelTree.getFilterControl ();

      // The parent name field is not empty.
      if ( !StringUtils.isEmpty (parentText.getText ()) ) {

        String parentLabel = parentText.getText ();
        List<LabelNode> existingParentLabelNodes = getLabels (parentLabel);

        // Parent name not found
        if (existingParentLabelNodes.size () == 0)
          errorMessage = Messages.create_label_page_validation_message_parent_not_exist;

        // parent found, but may be more than one.
        else {

          // if the entered text for parent matches the name of a unique LabelNode, use it as parent
          if (existingParentLabelNodes.size () == 1)
            newLabelParentLabel = existingParentLabelNodes.get (0);

          // The entered text for parent matches the name of more than one LabelNode.
          else {
            // if one of potential parent nodes is selected in the label tree, select it as parent
            if ( selectedObject != null &&
                 selectedObject instanceof LabelNode &&
                 existingParentLabelNodes.contains (selectedObject) )
              newLabelParentLabel = (LabelNode)selectedObject;
            else
              errorMessage = Messages.create_label_page_validation_message_parent_unclear;
          }
        }
      }
    }

    //-------- Validate label name can't be duplicate with a sublabel of the parent label --------//
    if (errorMessage == null) {
      String newLabelName = subtag_name_txt.getText ();
      ActionPlanView plan = AqlProjectUtils.getActionPlanView ();

      if (selectedObject != null && selectedObject instanceof LabelNode) {

        List<String> peerLabelNames = Actions.getLevelLabelNames (plan, (LabelNode)selectedObject);
        if (peerLabelNames.contains (newLabelName))
          errorMessage = Messages.create_label_validation_message_duplicate_name;
      }
    }

    setErrorMessage (errorMessage);
    if (errorMessage == null) {
      wiz.setTitle (subtag_name_txt.getText ());
      wiz.setNewLabelParentLabel (newLabelParentLabel);
    }
  }

  private List<LabelNode> getLabels (String labelName)
  {
    List<LabelNode> labelNodes = new ArrayList<LabelNode> ();
    for (LabelNode ln : existingLabelNodes) {
      if (ln.getLabel ().equals (labelName))
        labelNodes.add (ln);
    }

    return labelNodes;
  }

  /**
   * Sets or clears the error message. If not <code>null</code>, the OK button is disabled.
   * 
   * @param errorMessage the error message, or <code>null</code> to clear
   * @since 3.0
   */
  public void setErrorMessage (String errorMessage)
  {
    if (errorMessageText != null && !errorMessageText.isDisposed ()) {
      errorMessageText.setText (errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
      // Disable the error message text control if there is no error, or
      // no error text (empty or whitespace only). Hide it also to avoid
      // color change.
      // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
      boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces (errorMessage)).length () > 0;
      errorMessageText.setEnabled (hasError);
      errorMessageText.setVisible (hasError);
      errorMessageText.getParent ().update ();

      if (errorMessage == null) {
        wiz.setCanFinish (true);
        setPageComplete (true);
      }
      else {
        wiz.setCanFinish (false);
        setPageComplete (false);
      }
    }
  }

  protected int getInputTextStyle ()
  {
    return SWT.SINGLE | SWT.BORDER;
  }

  @Override
  public IWizardPage getNextPage ()
  {
    return null;
  }

  @Override
  public IWizardPage getPreviousPage ()
  {
    wiz.setCanFinish (false);
    return super.getPreviousPage ();
  }

  class DialogInputValidator implements IInputValidator
  {

    @Override
    public String isValid (String newText)
    {
      // no empty names or names containing only spaces (not allowed on Windows)
      if (newText.trim().isEmpty ()) { return Messages.create_label_page_validation_message_general; }
      // folder name ends with .
      if(newText.endsWith (".")) { return Messages.create_label_page_validation_message_ends; }

      // invalid chars
      String invalidChars = "<>:\"\'\\/|?*;";
      for (Character c : invalidChars.toCharArray ()) {
        if (newText.contains (c.toString ())) return Messages.create_label_page_validation_message_invalid_character;
      }

      return null;
    }
  }

  class LabelTreeContentProvider implements ITreeContentProvider 
  {
    ActionPlanView plan = null;

    @Override
    public void dispose ()
    {
    }

    @Override
    public void inputChanged (Viewer viewer, Object oldInput, Object newInput)
    {
      if (newInput instanceof ActionPlanView)
        this.plan = (ActionPlanView)newInput;
    }

    @Override
    public Object[] getElements (Object inputElement)
    {
      if (plan != null)
        return plan.getContentProvider ().getRoots ().toArray ();
      else
        return new Object[] {};
    }

    @Override
    public Object[] getChildren (Object parentElement)
    {
      if (parentElement instanceof LabelNode)    // Should be a LabelNode
        return ((LabelNode)parentElement).getAllFirstLevelLabelNodes ().toArray ();
      else
        return new Object[] {};
    }

    @Override
    public Object getParent (Object element)
    {
      if (element instanceof LabelNode)    // Should be a LabelNode
        return ((LabelNode)element).getParentLabelNode ();
      else
        return null;
    }

    @Override
    public boolean hasChildren (Object element)
    {
      return (getChildren (element).length > 0);
    }
  }

  class LabelTreeLabelProvider implements ILabelProvider
  {
    @Override
    public Image getImage (Object element)
    {
      if (element instanceof LabelNode) {   // Should be a LabelNode
        LabelNode labelNode = (LabelNode)element;

        DecorationOverlayIcon ovrlImageDescriptor = null;

        Image img = labelNode.getIconImage ();
        AqlGroupType group = labelNode.getAqlGroupType ();

        if ( group != null ) {

          switch ( group ) {
            case BASIC:
              ovrlImageDescriptor = new DecorationOverlayIcon(img, Icons.bfOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
              break;
            case CONCEPT:
              ovrlImageDescriptor = new DecorationOverlayIcon(img, Icons.cgOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
              break;
            case REFINEMENT:
              ovrlImageDescriptor = new DecorationOverlayIcon(img, Icons.fcOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
              break;
            case FINALS:
              ovrlImageDescriptor = new DecorationOverlayIcon(img, Icons.fiOverlayImgDesc, IDecoration.BOTTOM_RIGHT );
              break;
            default:
          }
        }

        if (ovrlImageDescriptor != null)
          return ovrlImageDescriptor.createImage ();
        else
          return img;
      }

      return null;
    }

    @Override
    public void addListener (ILabelProviderListener listener)
    {
    }

    @Override
    public void dispose ()
    {
    }

    @Override
    public boolean isLabelProperty (Object element, String property)
    {
      return false;
    }

    @Override
    public void removeListener (ILabelProviderListener listener)
    {
    }

    @Override
    public String getText (Object element)
    {
      if (element instanceof LabelNode)   // Should be a LabelNode
        return ((LabelNode)element).toString ();
      else
        return "";
    }
  }

  class LabelTreeSelectionChangeListener implements ISelectionChangedListener
  {

    @Override
    public void selectionChanged (SelectionChangedEvent event)
    {
      // if the selection is empty clear the label
      if (event.getSelection ().isEmpty ()) {
        selectedObject = null;
      }

      if (event.getSelection () instanceof IStructuredSelection) {
        IStructuredSelection selection = (IStructuredSelection) event.getSelection ();
        if (selection != null) {
          Object firstSelect = selection.getFirstElement ();
          if (firstSelect instanceof LabelNode)
            selectedObject = (LabelNode)firstSelect;
        }
      }

      validateInput ();
    }

  }

  class LabelTreeKeyAdapter extends KeyAdapter 
  {

    @Override
    public void keyReleased (KeyEvent e)
    {
      if ( e.keyCode == SWT.CR &&
           selectedObject != null &&
           selectedObject instanceof LabelNode) {
        labelTree.getFilterControl ().setText (((LabelNode)selectedObject).toString ());
        wiz.setNewLabelParentLabel ((LabelNode)selectedObject);
      }
    }
    
  }

}
