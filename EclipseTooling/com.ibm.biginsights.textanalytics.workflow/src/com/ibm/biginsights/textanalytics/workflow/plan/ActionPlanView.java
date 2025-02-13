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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.accessibility.AccessibleListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.editors.SelectionInfo;
import com.ibm.biginsights.textanalytics.workflow.editors.TaggingEditor;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.Actions;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.AddComment;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.AddElementAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.AddExampleToLabelAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.AddLabelAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.AddViewToPlanAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.DeleteAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.DoubleClickAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.NoTextAnalyticsAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.OpenCollectionAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.OpenDictionaryEditorAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.OpenExportExtractorAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.OpenRegexGenAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.RenameAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.RunInFilesLabeledAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.RunInFilesSelectedAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.RunInputCollectionAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.RunProfilerAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.SetLabelStateAction;
import com.ibm.biginsights.textanalytics.workflow.plan.actions.SwitchProjectAction;
import com.ibm.biginsights.textanalytics.workflow.plan.dnd.ActionPLanTransfer;
import com.ibm.biginsights.textanalytics.workflow.plan.dnd.DragListener;
import com.ibm.biginsights.textanalytics.workflow.plan.dnd.DropListener;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.models.AqlNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ExampleNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ExamplesFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.LabelsFolderNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.NodesGroup;
import com.ibm.biginsights.textanalytics.workflow.plan.models.ProjectNode;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeParent;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ActionPlanModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.CollectionModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.ExampleModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.LabelModel;
import com.ibm.biginsights.textanalytics.workflow.plan.serialize.Serializer;
import com.ibm.biginsights.textanalytics.workflow.tasks.ExtractionTasksView;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.AqlGroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Enumerations.GroupType;
import com.ibm.biginsights.textanalytics.workflow.util.Icons;
import com.ibm.biginsights.textanalytics.workflow.util.ResourceManager;

/**
 *  defines the view for the Action Plan
 * @note: you may find the word labeled and tagged node in several places in this file. please, note that we started
 *        using tagged node as the way to refer to labeled examples. later we decided that it would make more sense to
 *        use the term label vs. tag. So you may assume them both to be the same.
 */
public class ActionPlanView extends ViewPart
{

	@SuppressWarnings("unused")
private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "com.ibm.biginsights.textanalytics.workflow.views.ActionPlanView";

  protected TreeViewer viewer;
  protected Actions actions;
  protected DrillDownAdapter drill_down_adapter;
  protected Serializer serializer;
  protected IStructuredSelection selection;

  public static CollectionModel collection;
  public static boolean isReady;

  public static boolean doSort;
  public static boolean isSimplifiedView;

  TreeObject[] draggedObjects = null;

  public static boolean doSort ()
  {
    return doSort;
  }

  public static void setSort (boolean doSort)
  {
    ActionPlanView.doSort = doSort;
  }

  public static boolean isSimplifiedView ()
  {
    return isSimplifiedView;
  }

  public static void setSimplifiedView (boolean isSimplifiedView)
  {
    ActionPlanView.isSimplifiedView = isSimplifiedView;
  }

  public static String projectName;

  /**
   * The constructor.
   */
  public ActionPlanView ()
  {
    isReady = false;
    doSort = false;
    isSimplifiedView = false;
  }

  public boolean ready ()
  {
    return isReady;
  }

  /**
   * @return the list of root-leveled tag nodes
   */
  public ArrayList<LabelNode> getTagNodes ()
  {
    return ((ViewContentProvider) viewer.getContentProvider ()).getTagNodes ();
  }

  /**
   * provides a menu with the elements available for quick labeling. this is mostly used from the tagging editor
   * 
   * @param text the text selected
   * @param path the path of the file being edited
   * @param label the label of the file being edited
   * @param offset the offset of the selection
   * @param length the length of the selection
   * @return
   */
  public MenuManager getQuickLabelingMenuItems (String text, String path, String label, int offset, int length)
  {
    ArrayList<LabelNode> nodes = getTagNodes ();
    ExampleModel model = new ExampleModel (text, path, label, offset, length);

    MenuManager menu = new MenuManager ("Label Example As", ImageDescriptor.createFromImage (Icons.EXAMPLE_ICON),
      "#quickexamplemenu");

    for (LabelNode node : nodes) {
      menu.add (new AddExampleToLabelAction (model, node, this));
    }

    return menu;
  }

  /**
   * provides a menu to allow to link a given view to an specific aql element already defined. this is expected to be
   * used from the aql editor where the user selects a view and link it to an element in the action plan
   * 
   * @param viewName the view to add
   * @return
   */
  public MenuManager getQuickAQLLinkingMenu (String viewName)
  {
    ArrayList<LabelNode> nodes = getTagNodes ();

    MenuManager menu = new MenuManager (Messages.add_view_to_plan_label,
      ImageDescriptor.createFromImage (Icons.AQL_ICON), "#addviewtolabelmenu");

    for (LabelNode node : nodes) {
      menu.add (getLinkingMenu (node, viewName));
    }

    return menu;
  }

  /**
   * provides the menu item for a given node and view name
   * 
   * @param node
   * @param viewName
   * @return
   */
  private MenuManager getLinkingMenu (LabelNode node, String viewName)
  {

    MenuManager menu = new MenuManager (String.format (Messages.add_to_label, node.getLabel ()),
      ImageDescriptor.createFromImage (Icons.LABEL_ICON), "#addviewtolabelsubmenu");

    menu.add (new AddViewToPlanAction (viewName, AqlGroupType.BASIC, node));
    menu.add (new AddViewToPlanAction (viewName, AqlGroupType.CONCEPT, node));
    menu.add (new AddViewToPlanAction (viewName, AqlGroupType.REFINEMENT, node));

    return menu;
  }

  /**
   * @return
   */
  public ViewContentProvider getContentProvider ()
  {
    return (ViewContentProvider) getViewer ().getContentProvider ();
  }

  /**
   * @return
   */
  public ViewLabelProvider getLabelProvider ()
  {
    return (ViewLabelProvider) getViewer ().getLabelProvider ();
  }

  public TreeObject[] getDraggedObjects ()
  {
    return draggedObjects;
  }

  public void setDraggedObjects (TreeObject[] draggedObjects)
  {
    this.draggedObjects = draggedObjects;
  }

  /**
   * defines the content provider for this view
   * 
   * 
   */
  public class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider
  {
    ActionPlanView view = null;

    public ViewContentProvider (ActionPlanView view) {
      this.view = view;
    }

    private TreeParent invisibleRoot;

    public void inputChanged (Viewer v, Object oldInput, Object newInput)
    {}

    public void setContent (TreeParent invisibleRoot)
    {
      this.invisibleRoot = invisibleRoot;
    }

    public TreeObject getContent ()
    {
      return invisibleRoot;
    }

    public void dispose ()
    {}

    /**
     * @return the list of label nodes that are in the root label of the action plan tree
     */
    public ArrayList<LabelNode> getRoots ()
    {
      ArrayList<LabelNode> nodes = new ArrayList<LabelNode> ();
      for (TreeObject node : invisibleRoot.getChildren ()) {
        if (node instanceof LabelNode) {
          nodes.add ((LabelNode) node);
        }
      }

      return nodes;
    }

    /**
     * @return true if the given label is a root label; false, otherwise.
     */
    public boolean isRootLabel (LabelNode label)
    {
      return ( label != null &&
               getRoots ().contains (label) );
    }

    public boolean removeRootLabel(LabelNode labelNode)
    {
      if (labelNode != null && isRootLabel (labelNode))
        return invisibleRoot.removeChild (labelNode);
      else
        return false;
    }

    /**
     * @return all the files tagged by the cutrrent action plan
     */
    public List<String> getTaggedFiles ()
    {

      LinkedList<String> tagged = new LinkedList<String> ();

      for (TreeObject node : invisibleRoot.getChildren ()) {
        if (node instanceof LabelNode) {
          List<String> subtagged = ((LabelNode) node).getTaggedFiles ();
          for (String str : subtagged) {
            if (!tagged.contains (str)) tagged.add (str);
          }
        }
      }

      return tagged;
    }

    /**
     * @return a list of all the label nodes in the current action plan
     */
    public ArrayList<LabelNode> getTagNodes ()
    {
      ArrayList<LabelNode> nodes = new ArrayList<LabelNode> ();

      getTagNodes (invisibleRoot, nodes);

      return nodes;
    }

    /**
     * recursively loads all the tag nodes from the provided label node down
     * 
     * @param node
     * @param nodes
     */
    private void getTagNodes (TreeObject node, ArrayList<LabelNode> nodes)
    {
      if (node instanceof LabelNode) {
        nodes.add ((LabelNode) node);
      }

      if (node instanceof TreeParent) {
        for (TreeObject obj : ((TreeParent) node).getChildren ()) {
          getTagNodes (obj, nodes);
        }
      }
    }

    /**
     * adds a child to the tree
     * 
     * @param parent
     */
    public void addChild (TreeParent parent)
    {
      invisibleRoot.addChild (parent);

      // If we are adding a root label, also create 4 main modules for it. (task 22113)
      if (parent instanceof LabelNode && ProjectUtils.isModularProject (projectName)) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        String moduleSrcPath = ProjectUtils.getConfiguredModuleSrcPath(projectName);
        if (moduleSrcPath != null) {
          IFolder defaultSrcPath = root.getFolder(new Path(moduleSrcPath));
  
          LabelNode parentLabel = (LabelNode)parent;
  
          if (defaultSrcPath.exists ()) {
            try {
              IFolder bf_folder = defaultSrcPath.getFolder ( parentLabel.getGeneratedModuleName (AqlGroupType.BASIC) );
              if (!bf_folder.exists ())
                bf_folder.create (true, true, null);
  
              IFolder cg_folder = defaultSrcPath.getFolder ( parentLabel.getGeneratedModuleName (AqlGroupType.CONCEPT) );
              if (!cg_folder.exists ())
                cg_folder.create (true, true, null);
  
              IFolder fc_folder = defaultSrcPath.getFolder ( parentLabel.getGeneratedModuleName (AqlGroupType.REFINEMENT) );
              if (!fc_folder.exists ())
                fc_folder.create (true, true, null);
  
              IFolder fi_folder = defaultSrcPath.getFolder ( parentLabel.getGeneratedModuleName (AqlGroupType.FINALS) );
              if (!fi_folder.exists ())
                fi_folder.create (true, true, null);
            }
            catch (CoreException e) {
              // Just ignore if can't create folder
            }
          }
        }  
      }
    }

    /**
     * returns all the elements in the tree
     */
    public Object[] getElements (Object parent)
    {
      if (parent.equals (getViewSite ())) {
        if (invisibleRoot == null) initialize ();
        return getChildren (invisibleRoot);
      }
      return getChildren (parent);
    }

    /**
     * return the parent of a given node
     */
    public Object getParent (Object child)
    {
      if (child instanceof TreeObject) { return ((TreeObject) child).getParent (); }
      return null;
    }

    /**
     * return all the children of a given node
     */
    public Object[] getChildren (Object parent)
    {
      if (parent != null && parent instanceof TreeParent) {
        Object[] displayedObjects = ((TreeParent) parent).getDisplayedChildren ();

        if (doSort) {
          sortObjects ((TreeParent)parent, displayedObjects);
        }

        return displayedObjects;
      }

      return new Object[0];
    }

    private void sortObjects (TreeParent parent, Object[] objects)
    {
      if (objects == null || objects.length < 2)    // Nothing to sort
        return;

      int beginSort = -1;

      if (parent.getParent () == null)                  // sort the top labels
        beginSort = 1;
      else if (parent instanceof AqlFolderNode ||       // sort labels, views, examples
               parent instanceof LabelsFolderNode ||
               parent instanceof ExamplesFolderNode ||
               (parent instanceof AqlGroup && isSimplifiedView))
        beginSort = 0;

      if (beginSort < 0)
        return;

      // sort objects
      List<TreeObject> sortedObjectList = new ArrayList<TreeObject> ();
      for (int i = beginSort; i < objects.length; i++) {
        sortedObjectList.add ((TreeObject) objects[i]);
      }

      Comparator<TreeObject> comparator = new Comparator<TreeObject> () {
        /**
         * Simple compare tree objects by name. We expect objects are not null and we can get its labels; in that case,
         * compare using Java compareTo(). Otherwise, just return 'equal'.
         * 
         * @param o1
         * @param o2
         * @return
         */
        @Override
        public int compare (TreeObject o1, TreeObject o2)
        {
          if ((o1 != null && o1.toString () != null) && (o2 != null && o2.toString () != null)) {
            String s1 = o1.toString ();
            String s2 = o2.toString ();
            int minLen = (s1.length () < s2.length ()) ? s1.length () : s2.length ();

            for (int i = 0; i < minLen; i++) {
              char c1 = s1.charAt (i);
              char c2 = s2.charAt (i);

              // case 0: not letter, compare by ascii value
              if ( !Character.isLetter (c1) || !Character.isLetter (c1)) {
                if (c1 > c2)
                  return 1;
                if (c1 < c2)
                  return -1;
              }

              //-------- Reach here means both are letters
              // case 1: 2 case-insensitively different letters
              if ( Character.toLowerCase (c1) != Character.toLowerCase (c2) ) {
                Character C1 = new Character(Character.toLowerCase (c1));
                Character C2 = new Character(Character.toLowerCase (c2));
                return C1.compareTo (C2);
              }
              // case 2: same letters different cases, lowercase goes first (opposite with ascii value)
              else {
                if (c1 > c2)
                  return -1;
                if (c1 < c2)
                  return 1;
              }
            }

            // Reach here means they are the same, up to the minLen, now the shorter one goes first.
            if (s1.length () > minLen)
              return 1;
            if (s2.length () > minLen)
              return -1;
          }

          return 0;
        }
      };

      Collections.sort (sortedObjectList, comparator);
      for (int i = 0; i < sortedObjectList.size (); i++) {
        objects[beginSort + i] = sortedObjectList.get (i);
      }
    }

    /**
     * check if a node contain at least one sub node
     */
    public boolean hasChildren (Object parent)
    {
      return getChildren(parent).length > 0;
    }

    /*
     * We will set up a dummy model to initialize tree heararchy. In a real code, you will connect to a real model and
     * expose its hierarchy.
     */
    private void initialize ()
    {
      ProjectNode projectNode = null;
      if (ActionPlanView.projectName != null) {
        projectNode = new ProjectNode (ActionPlanView.projectName);
      }

      if (projectNode != null) {
        invisibleRoot = new NodesGroup ("", null, null);
      }
    }
  }

  /**
   * label provider for the tree representation of the action plan
   * 
   * 
   */
  class ViewLabelProvider extends LabelProvider
  {
    ActionPlanView view = null;

    public ViewLabelProvider (ActionPlanView view) {
      this.view = view;
    }

    /**
     * 
     */
    public String getText (Object obj)
    {
      String str = obj.toString ();

      if (obj instanceof AqlGroup) {
        LabelNode parentLabel = (LabelNode) ((AqlGroup)obj).getParent ();
        if (parentLabel != null) {
          if (view.getContentProvider ().isRootLabel (parentLabel))
            str = parentLabel.getLabel () + "_" + str;
        }
      }

      if (str.length () > 40) {
        str = str.substring (0, 25) + " ... " + str.substring (str.length () - 10);
      }
      return str;
    }

    /**
     * Return the object icon
     */
    public Image getImage (Object obj)
    {
      if (obj instanceof TreeObject) {
        TreeObject treeObj = (TreeObject) obj;
        DecorationOverlayIcon ovrlImageDescriptor = getOverlayImage (treeObj);

        if (ovrlImageDescriptor != null)
          return ovrlImageDescriptor.createImage ();
        else
          return treeObj.getIconImage ();
      }

      return ResourceManager.getMissingImage ();
    }

    private DecorationOverlayIcon getOverlayImage (TreeObject treeObj)
    {
      DecorationOverlayIcon ovrlImageDescriptor = null;

      Image img = treeObj.getIconImage ();
      if (img == null)
        return null;

      if ( treeObj instanceof AqlNode &&
           ((AqlNode)treeObj).getAqlGroup () != null ) {

        switch ( ((AqlNode)treeObj).getAqlGroup () ) {
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

      return ovrlImageDescriptor;
    }
  }

  /**
   * This is a callback that will allow us to create the viewer and initialize it.
   */
  public void createPartControl (Composite parent)
  {
    setViewer (new TreeViewer (parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL));

    drill_down_adapter = new DrillDownAdapter (getViewer ());

    int operations = DND.DROP_COPY | DND.DROP_MOVE;

    Transfer[] transferTypes = new Transfer[] { ActionPLanTransfer.getInstance (), TextTransfer.getInstance () };

    getViewer ().addDragSupport (operations, transferTypes, new DragListener (viewer));

    getViewer ().addDropSupport (operations, transferTypes, new DropListener (viewer));

    getViewer ().setContentProvider (new ViewContentProvider (this));
    getViewer ().setLabelProvider (new ViewLabelProvider (this));
    getViewer ().setInput (getViewSite ());

    actions = new Actions (this);
    updateMenus (null);
    contributeToActionBars ();
    hookDoubleClickAction ();

    serializer = new Serializer ();

    getViewer ().addSelectionChangedListener (new ISelectionChangedListener () {

      public void selectionChanged (SelectionChangedEvent event)
      {
        // if the selection is empty clear the label
        if (event.getSelection ().isEmpty ()) {
          updateMenus (null);
        }

        if (event.getSelection () instanceof IStructuredSelection) {
          // get a handler to the selected tag in the tree
          selection = (IStructuredSelection) event.getSelection ();
          if (selection != null) {
            updateMenus (selection);
            doClick(selection);
          }
        }
      }
    });

    getViewer ().getTree ().addKeyListener (new ExtractionPlanKeyAdapter(this));

    getViewer ().getTree ().getAccessible ().addAccessibleListener (getAccessibleListener ());

    if (projectName != null) {
      refreshView (projectName);
    }
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.extraction_plan_view");//$NON-NLS-1$
  }

  private void doClick (IStructuredSelection selection)
  {
    ArrayList<SelectionInfo> list = new ArrayList<SelectionInfo>();

    IEditorPart ieditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

    if (ieditor instanceof TaggingEditor) {
      TaggingEditor editor = (TaggingEditor) ieditor;

      for (Object obj : selection.toList ()) {
        if (obj instanceof ExampleNode) {
          ExampleNode child = (ExampleNode) obj;
          if (editor.isFileOpened(child.getFilePath(), child.getFileLabel()) != null) {
            list.add(child.getPair());
          }
        }
      }
      
      editor.highlight(list);
    }
  }

  /**
   * reset the view. this method is usually called when we clear the view and no project is actively displayed
   */
  public void reset ()
  {
    ViewContentProvider provider = getContentProvider ();
    isReady = false;
    projectName = null;
    collection = new CollectionModel ();
    provider.setContent (new NodesGroup ("", null, null));
    viewer.refresh ();
  }

  /**
   * serialize the action plan into an xml representation and store it to a file
   * 
   * @see ActionPlanModel
   * @throws UnsupportedEncodingException
   * @throws CoreException
   */
  public void serialize () throws UnsupportedEncodingException, CoreException
  {
    if (!isReady) return;
    if (projectName != null) {
      IProject project = AqlProjectUtils.getProject (projectName);
      if (project != null) {
        IFile file = project.getFile (Constants.EXTRACTION_PLAN_FILE_NAME);
        ArrayList<LabelModel> roots = new ArrayList<LabelModel> ();
        for (LabelNode tag : getContentProvider ().getRoots ()) {
          roots.add (tag.toModel ());
        }
        ActionPlanModel model = new ActionPlanModel (projectName, roots, collection);
        serializer.writeModelToFile (file, model);
      }
    }
  }

  /**
   * loads the serialization for the current action plan from the files expecetd with name @see
   * Constants.EXTRACTION_PLAN_FILE_NAME
   * 
   * @return a model of the action plan
   */
  public ActionPlanModel loadSerialization ()
  {
    if (!isReady) return null;
    if (projectName != null) {
      IProject project = AqlProjectUtils.getProject (projectName);
      if (project != null) {
        IFile file = project.getFile (Constants.EXTRACTION_PLAN_FILE_NAME);
        if (file.exists ()) {
          try {
            ActionPlanModel model = serializer.getModelForInputStream (file.getContents ());
            if ( !model.isModularVersion () )
              model.convertToModularVersion();

            return model;
          }
          catch (CoreException e) {
            Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
          }
        }
        else {
          return null;
        }
      }
    }
    return null;
  }

  /**
   * loads the plan for the provided projectName
   * 
   * @param projectName
   * @return the tree representation of the action plan
   */
  private TreeParent getPlanForCurrentProject (String projectName2)
  {
    TreeParent root = new NodesGroup ("", null, null);

    IProject project = AqlProjectUtils.getProject (projectName2);
    isReady = false;
    try {
      if (!project.hasNature (com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID)) {
        NoTextAnalyticsAction err = new NoTextAnalyticsAction ();
        err.run ();
        return null;
      }
    }
    catch (CoreException e1) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready, e1);
      return null;
    }
    isReady = true;
    ActionPlanView.projectName = projectName2;
    AqlProjectUtils.createLibraryForProject (projectName);
    root.addChild (new ProjectNode (projectName));

    ActionPlanModel model = loadSerialization ();
    if (model != null) {
      for (LabelModel tag : model.getRoots ()) {
        root.addChild (new LabelNode (tag));
      }
      collection = model.getCollection ();
    }
    else {
      collection = new CollectionModel ();

      try {
        AqlProjectUtils.addTextAnalyticsConfiguration (project);
      }
      catch (Exception e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready, e);
        return null;
      }
    }

    return root;
  }

  public static CollectionModel getCollection ()
  {
    return collection;
  }

  /**
   * Refresh view by loading Exteraction Plan of the given project.
   * 
   * @param projectName2 The project to reload.
   * @return TRUE if refresh happens; FALSE otherwise.
   */
  public boolean refreshView (String projectName2)
  {
    if (projectName2 == null)
      projectName2 = projectName;

    ViewContentProvider provider = getContentProvider ();
    TreeParent root = getPlanForCurrentProject (projectName2);
    if (root != null) {
      provider.setContent (root);
      viewer.refresh ();
      ExtractionTasksView etview = AqlProjectUtils.getExtractionTasksView ();
      if (etview != null)
        etview.refresh (collection.getPath (), collection.getLangCode ());

      return true;
    }
    return false;
  }

  // public void createTag (ExampleModel model)
  // {
  // actions.add_root_label.run ();
  // if (actions.last_used instanceof LabelNode) {
  // LabelNode node = (LabelNode) actions.last_used;
  // node.addExample (new ExampleNode (model));
  // try {
  // serialize ();
  // }
  // catch (Exception e) {
  // e.printStackTrace ();
  // }
  // viewer.refresh ();
  // }
  // }

  /**
   * creates a label node using the parameters provided
   */
  public void createLabel (ExampleModel model, LabelNode parent, String title, boolean createAQLs)
  {
    AddLabelAction action = new AddLabelAction (this, model, parent, title, createAQLs);
    Display.getDefault ().asyncExec (new ExtractionPlanRunnable (action));
  }

  /**
   * creates a example node using the parameters provided
   * 
   * @param model
   * @param node
   */
  public void addExample (ExampleModel model, LabelNode node)
  {
    AddExampleToLabelAction action = new AddExampleToLabelAction (model, node, this);
    action.run ();
  }

  /**
   * gets the label node currently being selected
   * 
   * @return
   */
  public LabelNode getselectedTag ()
  {
    ISelection selection = getViewer ().getSelection ();
    TreeObject obj = (TreeObject) ((IStructuredSelection) selection).getFirstElement ();

    if (obj == null) { return null; }

    if (obj instanceof LabelNode) {
      return (LabelNode) obj;
    }
    else {
      if (obj.getParent () == null) return null;

      IStructuredSelection sel = new StructuredSelection (obj.getParent ());
      viewer.setSelection (sel);
      return getselectedTag ();
    }

  }

  /**
   * gets the node currently selected (if any)
   * 
   * @return
   */
  public TreeObject getSelection ()
  {
    ISelection selection = getViewer ().getSelection ();
    TreeObject obj = (TreeObject) ((IStructuredSelection) selection).getFirstElement ();

    return obj;
  }

  /**
   * update the context menu based in the current selection
   * 
   * @param selection
   */
  private void updateMenus (IStructuredSelection selection)
  {
    hookContextMenu (selection);
  }

  /**
   * update the context menu based in the current selection
   * 
   * @param selection
   */
  private void hookContextMenu (final IStructuredSelection selection)
  {
    MenuManager menuMgr = new MenuManager ("#PopupMenu");
    menuMgr.setRemoveAllWhenShown (true);
    menuMgr.addMenuListener (new IMenuListener () {
      public void menuAboutToShow (IMenuManager manager)
      {
        ActionPlanView.this.fillContextMenu (manager, selection);
      }
    });
    Menu menu = menuMgr.createContextMenu (getViewer ().getControl ());
    getViewer ().getControl ().setMenu (menu);
    getSite ().registerContextMenu (menuMgr, getViewer ());
  }

  /**
   * adds a set of actions to the action bar of this view
   */
  private void contributeToActionBars ()
  {
    IActionBars bars = getViewSite ().getActionBars ();
    fillLocalPullDown (bars.getMenuManager ());
    fillLocalToolBar (bars.getToolBarManager ());
  }

  private void fillLocalPullDown (IMenuManager manager)
  {
    // manager.add(actions.add_root_label);
    // manager.add(getRunMenuActions());
  }

  /**
   * adds a set of action for the local tool bar of this view
   * 
   * @param manager
   */
  private void fillLocalToolBar (IToolBarManager manager)
  {
    manager.add (new SwitchProjectAction ());
    manager.add (new OpenCollectionAction ());
    manager.add (new AddElementAction (Messages.add_label_text, Messages.add_label_tootltip, Icons.LABEL_ICON));

    manager.add (new Separator ());

    manager.add (actions.sort_az);
    manager.add (actions.simplified_view);
    manager.add (actions.full_view);

    manager.add (new Separator ());

    // manager.add(getRunMenuActions());
    manager.add (new RunInputCollectionAction (this));
    manager.add (new RunInFilesSelectedAction (this));
    manager.add (new RunInFilesLabeledAction (this));

    manager.add (new Separator ());

    manager.add (new RunProfilerAction ());

    manager.add (new Separator ());

    manager.add (new OpenExportExtractorAction ());

    manager.add (new Separator ());
    drill_down_adapter.addNavigationActions (manager);
  }

  /**
   * fills the context menu based in the current selection
   * 
   * @param manager
   * @param selection
   */
  private void fillContextMenu (IMenuManager manager, IStructuredSelection selection)
  {
    if (!isReady) { return; }

    if (selection == null || selection.size() == 0) {
      if ( projectName != null  &&
           AqlProjectUtils.getProject (projectName) != null )
        manager.add (actions.makeAddLabelAction());

      return;
    }

    if ( selection.size() == 1 ) {

      TreeObject selectedObject = (TreeObject)selection.getFirstElement();

      if (selectedObject instanceof LabelNode || selectedObject instanceof AqlNode /* || selectedObject instanceof ExampleNode */)
        manager.add (new RenameAction (selectedObject, this));

      // comment action
      if ( selectedObject instanceof AqlNode ) {
        manager.add (new AddComment ((AqlNode) selectedObject, this));
      }
      else if (selectedObject instanceof LabelNode) {
        manager.add (new AddComment ((LabelNode) selectedObject, this));
      }

      //--------  Create Add Label actions  --------//
      if ( selectedObject instanceof LabelNode ) {
        LabelNode labelNode = (LabelNode)selectedObject;
        MenuManager addLabelMenus = new MenuManager(Messages.add_label_text, ImageDescriptor.createFromImage (Icons.LABEL_ICON), "");
        manager.add (addLabelMenus);
        addLabelMenus.add (actions.makeAddLabelAction(labelNode));
        addLabelMenus.add (actions.makeAddLabelAction(labelNode.getBasicsGroup ()));
        addLabelMenus.add (actions.makeAddLabelAction(labelNode.getCandidatesGroup ()));
        addLabelMenus.add (actions.makeAddLabelAction(labelNode.getRefinementsGroup ()));
        addLabelMenus.add (actions.makeAddLabelAction(labelNode.getFinalsGroup ()));
      }
      else if ( selectedObject instanceof AqlGroup ||
                selectedObject instanceof LabelsFolderNode ) {
        manager.add (actions.makeAddLabelAction((NodesGroup)selectedObject));
      }

      //--------  Create Add AQL Statement actions  --------//
      if ( selectedObject instanceof LabelNode ) {
        LabelNode labelNode = (LabelNode)selectedObject;
        MenuManager addAqlMenus = new MenuManager(Messages.add_aql_rule_text,
                                                  ImageDescriptor.createFromImage (Icons.AQL_ICON), "");
        manager.add (addAqlMenus);
        addAqlMenus.add (actions.makeAddAqlStatementAction(labelNode.getBasicsGroup ().getAqlStatementsFolder ()));
        addAqlMenus.add (actions.makeAddAqlStatementAction(labelNode.getCandidatesGroup ().getAqlStatementsFolder ()));
        addAqlMenus.add (actions.makeAddAqlStatementAction(labelNode.getRefinementsGroup ().getAqlStatementsFolder ()));
        addAqlMenus.add (actions.makeAddAqlStatementAction(labelNode.getFinalsGroup ().getAqlStatementsFolder ()));
      }
      else if ( selectedObject instanceof AqlGroup ) {
        manager.add (actions.makeAddAqlStatementAction( ((AqlGroup)selectedObject).getAqlStatementsFolder () ));
      }
      else if ( selectedObject instanceof AqlFolderNode ) {
        manager.add (actions.makeAddAqlStatementAction((AqlFolderNode)selectedObject));
      }

      if (selectedObject instanceof LabelNode) {
        LabelNode node = (LabelNode) selectedObject;
        manager.add (new SetLabelStateAction (!node.isDone (), node, this));
      }

      // pasting
      if ( actions.movingElements != null &&
           actions.movedElements != null ) {

        if (selectedObject instanceof NodesGroup) {

          GroupType gtype = ((NodesGroup) selectedObject).getGroupType ();

          TreeObject aMovedElement = actions.movedElements.get (0);   // All moved elements are of the same type, so we
                                                                      // only need one of them to find out the type

          switch (gtype) {
            // you can paste anything into a label node
            case TAG:
              if (aMovedElement instanceof TreeParent) {
                TreeParent moving = (TreeParent) aMovedElement;
                if (!moving.isSubElement (selectedObject) && !aMovedElement.equals (selectedObject))
                  manager.add (actions.paste);
              }
              else {
                manager.add (actions.paste);
              }
            break;

            // you can paste a tag into a tag folder
            case TAG_FOLDER:
              if (aMovedElement instanceof LabelNode) {
                TreeParent moving = (TreeParent) aMovedElement;
                if (!moving.isSubElement (selectedObject) && !aMovedElement.equals (selectedObject.getParent ()))
                  manager.add (actions.paste);
              }
            break;

            // you can paste an example into the examples folder
            case EXAMPLES:
              if (aMovedElement instanceof ExampleNode) {
                manager.add (actions.paste);
              }
            break;

            // you can paste any aql element into the main aql foldeer
            case AQL_FOLDER:
              if (aMovedElement instanceof AqlNode) {
                manager.add (actions.paste);
              }
            break;

            // you can paste a basicfeature element into an aql folder of
            // type
            // basic feature
            case BASIC_FEATURES:
            case CONCEPTS:
            case REFINEMENT:
              if (aMovedElement instanceof AqlNode) {

                manager.add (actions.paste);

              }
            break;
          }
        }
      }
    }

    if ( isSelectionSameType(selection) ) {
      manager.add (new DeleteAction (selection, this));
      manager.add (actions.copy);
      manager.add (actions.cut);

      if ( selection.getFirstElement () instanceof ExampleNode &&
           isSelectionSameParent(selection) ) {
        manager.add (new Separator (IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add (new OpenRegexGenAction(selection));
        manager.add (new OpenDictionaryEditorAction(selection));
      }
    }

    manager.add (new Separator ());
    manager.add (getRunMenuActions (selection));

    manager.add (new Separator ());
    drill_down_adapter.addNavigationActions (manager);
    // // Other plug-ins can contribute there actions here
    manager.add (new Separator (IWorkbenchActionConstants.MB_ADDITIONS));
  }

  private boolean isSelectionSameType (IStructuredSelection selection)
  {
    return ( isSelectionAllSameType (selection, ExampleNode.class) ||
             isSelectionAllSameType (selection, LabelNode.class)   ||
             isSelectionAllSameType (selection, AqlNode.class) );
  }

  @SuppressWarnings({ "rawtypes" })
  private boolean isSelectionAllSameType (IStructuredSelection selection, Class<?> type)
  {
    if (selection == null || selection.size () == 0)
      return false;
    else {
      for (Iterator iter = selection.iterator (); iter.hasNext ();) {
        TreeObject to = (TreeObject) iter.next ();
        if (!(to.getClass() == type)) {
          return false;
        }
      }
    }

    return true;
  }

  @SuppressWarnings("unchecked")
  private boolean isSelectionSameParent (IStructuredSelection selection)
  {
    if (selection == null || selection.size () == 0)
      return false;
    else {
      TreeObject to1 = (TreeObject)selection.getFirstElement ();
      TreeParent parent1 = to1.getParent ();  // Parent of the first object in the selection. It should not be null because all objects
                                              // in the tree have parent. Parent of the top level objects is the 'invisible root'

      for (Iterator<TreeObject> iter = selection.iterator (); iter.hasNext ();) {
        TreeObject to = iter.next ();
        if ( to.getParent() != parent1 )
          return false;
      }
    }

    return true;
  }

  private IMenuManager getRunMenuActions (IStructuredSelection selection)
  {
    MenuManager menu = new MenuManager (Messages.run_default_text, ImageDescriptor.createFromImage (Icons.RUN_ICON),
      "#runactionmenu");

    menu.add (new RunInputCollectionAction (this));
    menu.add (new RunInFilesSelectedAction (this));
    menu.add (new RunInFilesLabeledAction (this));

    if ( ProjectUtils.isModularProject (projectName) &&
         selection.size() == 1 &&
         selection.getFirstElement () instanceof AqlGroup ) {

      AqlGroup group = (AqlGroup)selection.getFirstElement ();
      LabelNode parentLabel = (LabelNode)group.getParent ();

      // Only show the menus for running module
      //   . for the 4 main groups of root label, because only these main groups are associated with modules.
      //   . the associated module exists
      if (parentLabel.isRootLabel ()) {

        String assocModuleName = parentLabel.getGeneratedModuleName (group.getAqlType ());
        List<String> moduleList = Arrays.asList (ProjectUtils.getAllModules (projectName));

        if (moduleList.contains (assocModuleName)) {
          menu.add (new Separator ());
          menu.add (new RunInputCollectionAction (this, assocModuleName));
          menu.add (new RunInFilesSelectedAction (this, assocModuleName));
          menu.add (new RunInFilesLabeledAction (this, assocModuleName));
        }
      }
    }

    return menu;
  }

  /**
   * init the action that hook the double click in the actions plan viewer
   */
  private void hookDoubleClickAction ()
  {
    getViewer ().addDoubleClickListener (new IDoubleClickListener () {
      public void doubleClick (DoubleClickEvent event)
      {
        ActionPlanView plan = ActionPlanView.this;
        DoubleClickAction action = new DoubleClickAction (plan, plan.getSelection ());
        action.run ();
      }
    });
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  public void setFocus ()
  {
    getViewer ().getControl ().setFocus ();
  }

  /**
   * @param viewer
   */
  public void setViewer (TreeViewer viewer)
  {
    this.viewer = viewer;
  }

  /**
   * @return
   */
  public TreeViewer getViewer ()
  {
    return viewer;
  }

  public List<String> getTaggedFiles ()
  {
    List<String> tagged = getContentProvider ().getTaggedFiles ();
    return tagged;
  }

  /**
   * @return
   */
  public boolean isEmpty ()
  {
    TreeParent parent = (TreeParent) getContentProvider ().getContent ();
    return !parent.hasChildren ();
  }

  public void openMainAQL () throws PartInitException
  {
    if (projectName != null) AqlProjectUtils.openMainAQL (projectName);
  }

  /**
   * @param obj
   * @param level
   */
  public void serializeAndExpand (TreeObject obj, int level)
  {
    serializeAndRefresh ();
    getViewer ().expandToLevel (obj, level);
  }

  /**
   * @param obj
   */
  public void serializeAndExpand (TreeObject obj)
  {
    serializeAndRefresh ();
    getViewer ().expandToLevel (obj, TreeViewer.ALL_LEVELS);
  }

  /**
   * @param obj
   * @param level
   */
  public void serializeAndCollapse (TreeObject obj, int level)
  {
    serializeAndRefresh ();
    getViewer ().collapseToLevel (obj, level);
  }

  public void serializeAndRefresh ()
  {
    try {
      serialize ();
    }
    catch (Exception e) {
      Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
    }
    getViewer ().refresh ();
  }

  /**
	 * 
	 */
  public static void serializePlan ()
  {
    ActionPlanView view = AqlProjectUtils.getActionPlanView ();
    if (view != null) try {
      view.serialize ();
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.toString ());
    }
  }

  public static void serializePlan (String projectName, ArrayList<LabelModel> roots, CollectionModel collection)
  {
    if (projectName != null) {
      IProject project = AqlProjectUtils.getProject (projectName);

      if (project != null) {
        IFile file = project.getFile (Constants.EXTRACTION_PLAN_FILE_NAME);
        if (file == null || !file.exists ()) return;

        ActionPlanModel model = new ActionPlanModel (projectName, roots, collection);
        Serializer serializer = new Serializer ();
        serializer.writeModelToFile (file, model);
      }
    }
  }

  public static ActionPlanModel loadActionPlan (String projectName)
  {
    IProject project = AqlProjectUtils.getProject (projectName);

    try {
      if (!project.hasNature (com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID)) { return null; }

      IFile file = project.getFile (Constants.EXTRACTION_PLAN_FILE_NAME);
      if (file.exists ()) {
        try {
          Serializer serializer = new Serializer ();
          ActionPlanModel model = serializer.getModelForInputStream (file.getContents ());
          return model;
        }
        catch (CoreException e) {
          Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
        }
      }
    }
    catch (CoreException e1) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.extraction_plan_not_ready, e1);
    }
    return null;
  }

  private AccessibleListener getAccessibleListener ()
  {
    AccessibleListener accListener = new AccessibleAdapter() {
      
      @Override
      public void getName (AccessibleEvent e)
      {
        IStructuredSelection selection = (IStructuredSelection)viewer.getSelection ();
        Object selObject = selection.getFirstElement ();
        if (selObject instanceof LabelNode) {
          LabelNode selectedLabel = (LabelNode) selObject;
          if (selectedLabel.isDone ())
            e.result = selectedLabel.toString () + Messages.label_done_suffix;
        }
      }
    };

    return accListener;
  }

  public static String getLangCode ()
  {
    if (collection != null) return collection.getLangCode ();
    return null;
  }

  class ExtractionPlanRunnable implements Runnable
  {
    Action action;

    public ExtractionPlanRunnable (Action action)
    {
      this.action = action;
    }

    @Override
    public void run ()
    {
      action.run ();
    }

  }

  class ExtractionPlanKeyAdapter extends KeyAdapter 
  {
    ActionPlanView plan;

    public ExtractionPlanKeyAdapter (ActionPlanView plan)
    {
      super ();
      this.plan = plan;
    }

    @Override
    public void keyPressed (KeyEvent e)
    {
      // TODO Auto-generated method stub
      super.keyPressed (e);
    }

    @Override
    public void keyReleased (KeyEvent e)
    {
      if ( e.keyCode == SWT.DEL &&
           selection != null &&
           plan.isSelectionSameType (selection) ) {
        DeleteAction delAction = new DeleteAction (selection, plan);
        delAction.run ();
      }
    }
    
  }

}
