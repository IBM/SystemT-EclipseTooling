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
package com.ibm.biginsights.textanalytics.aql.editor.callhierarchy;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ibm.biginsights.textanalytics.aql.editor.Messages;

/**
 * This view displays a tree where the nodes are
 * aql views and each node's children are aql views
 * on which it is dependent.
 * 
 *
 */
public class DependencyHierarchyView extends ViewPart
{


 
	public static final String ID = "com.ibm.biginsights.textanalytics.aql.editor.callhierarchy.DependencyHierarchyView.view"; //$NON-NLS-1$
  protected static TreeViewer viewer;
  protected HierarchyRootNode root;
  
  public static String projectName=""; //$NON-NLS-1$
  
  protected static String currToken = null; //This value is treated as the aql view which would be the root for the dependency hierarchy tree.

  public static void setCurrToken(String pCurrToken) {
    currToken = pCurrToken;
  }
  public static String getCurrToken() {
    return currToken;
  }
  
  @Override
  public void createPartControl(Composite parent) {
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.aql_dependency_hierarchy");

    this.setContentDescription (Messages.DependencyHierarchy_DESC);
    getRoot();
    viewer = new TreeViewer(parent);
    // viewer.addTreeListener(new AQLTreeViewerListener());
    viewer.setContentProvider(new AQLContentProvider());
    viewer.setLabelProvider(new HierarchyNodeLabelProvider());
    viewer.setInput(root);
    
    MenuManager menuManager = new MenuManager();
    Menu menu = menuManager.createContextMenu (viewer.getTree ());
    
    viewer.getTree ().setMenu (menu);
    getSite().registerContextMenu (menuManager, viewer);
    
    viewer.getTree ().getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        if (e.childID == ACC.CHILDID_SELF) { //If focus is on the tree and no elements have been selected.
          e.result = getPartName () + " : " + Messages.DependencyHierarchy_DESC; //$NON-NLS-1$
        }
      }
    });
    
    
    
    getSite().setSelectionProvider (viewer);
    viewer.expandToLevel (2);
  }
  
  /**
   * Creates a HierarchyRootNode instance, providing it the root view name
   * @return
   */
  protected HierarchyRootNode getRoot() {
    if (root == null) {
      root = new HierarchyRootNode(currToken);
    }
    root.setIfDependencyHierarchyRoot (true);
    return root;
  }
  
  @Override
  public void setFocus() {
    try {
      viewer.getControl().setFocus();
    } catch (Exception e) {
    }
  }
  
  /**
   * Provides content to the treeviewer in the form of 
   * HierarchyRootNode and HierarchyNode
   * 
   */
  public class AQLContentProvider implements ITreeContentProvider {

    //private HierarchyRootNode root;
    @Override
    public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof HierarchyRootNode) {
        return ((HierarchyRootNode) parentElement).getChildren();

      } else if (parentElement instanceof HierarchyNode) {
        return ((HierarchyNode) parentElement).getChildren();
      }
      return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
      if (element instanceof HierarchyNode) {
        HierarchyNode parent = ((HierarchyNode) element).getParent();
        if (parent == null) {
          return root;
        } else {
          return parent;
        }
      }
      return null;
    }

    @Override
    public boolean hasChildren(Object element) {
      if (element instanceof HierarchyNode) {
        return ((HierarchyNode) element).hasChildren ();
      } else {
        if (getChildren(element).length == 0) {
          return false;
        } else {
          return true;
        }
      }
    }

    @Override
    public Object[] getElements(Object inputElement) {
      return getChildren(inputElement);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

  }
  
  

}
