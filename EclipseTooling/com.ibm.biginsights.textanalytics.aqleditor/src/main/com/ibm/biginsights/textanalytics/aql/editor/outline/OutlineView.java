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
package com.ibm.biginsights.textanalytics.aql.editor.outline;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.outline.IAQLOutlineView;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * 
 *  Babbar
 * 
 */
public class OutlineView extends ContentOutlinePage implements IAQLOutlineView {



  private AQLEditor editor;
  IPath activeProjectPath;
  private AQLRootNode root = null;
  StyledText s;
  TreeViewer viewer;
  ViewerSorter alphasorter;
  public static IAQLLibrary aqlLibrary;
  
  private static final ILog logger = LogUtil
  .getLogForPlugin(Activator.PLUGIN_ID);

  public OutlineView(AQLEditor editor) {
    this.editor = editor;
  }

  @Override
  public void createControl(Composite parent) {

    super.createControl(parent);
    viewer = getTreeViewer();
    if (root == null) {
      root = new AQLRootNode();
    }
    
    try{
      IFileEditorInput input = (IFileEditorInput)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
      //System.out.println ("Input is null ?"+input == null); 
      String prjName = getProjectName(input.getFile().getLocation().toOSString());
      alphasorter = new AlphabeticalSorter(prjName);


      viewer.setContentProvider(new AQLContentProvider());
      viewer.setLabelProvider(new AQLLabelProvider());
      viewer.setInput(root);
      viewer.addSelectionChangedListener(new AQLSelectionChangedListener());

      if (getSite() != null) {
        IActionBars bars = getSite().getActionBars();
        IToolBarManager toolbar = bars.getToolBarManager();
        toolbar.add(new SortAction());
      }

      //getTreeViewer().refresh();

      update();
      
    }catch (NullPointerException e) {
      logger.logError (e.getMessage ());
    }catch (Exception e) {
      logger.logError (e.getMessage ());
    }
  }

  public String getProjectName(String aqlFilePath) 
  {
    IPath path = new Path(aqlFilePath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
    if(file == null) return null;
    else
    {
      return file.getProject().getName();
    }
  }


  public void createCopy(AQLRootNode rootDummy) {
    // iterate through rootDummy and put values in rootOld

  }
  public void update() {

    Object expandedElement[] = null;
    TreeViewer viewer1 = getTreeViewer();
    if (viewer1 == null) {
      //System.out.println("NO view");
    } else {
      try {
        expandedElement = viewer1.getExpandedElements();
      } catch (Exception e) {
      }
    }
    if (root == null) 
    {
      root = new AQLRootNode();
    } else 
    {
      //do nothing
    }
    root.clear();
    TreeViewer viewer = getTreeViewer();
    if(viewer == null){
      //System.out.println("NO view");	
    }
    else
    {
      viewer.refresh();	
      if(expandedElement.length > 0)
      {
        viewer.expandAll();
      }
    }
  }

  private class SortAction extends Action {
    public SortAction() {
      super("Sort", IAction.AS_CHECK_BOX);
      setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Activator.ICON_SORT));
    }

    @Override
    public void run() {
      if (isChecked()) {
        getTreeViewer().setSorter(alphasorter);
        update();
      } else {
        root.clear();
        getTreeViewer().setSorter(null);
        update();
      }
    }
  }


  public class AQLRootNode{
    private List<AQLNode> children = new ArrayList<AQLNode>();

    public void add(AQLNode node) {
      children.add(node);
    }

    public AQLNode[] getChildren() {

      try {
        // for top level tree node
        Map<String, AQLNode> elementMap = new HashMap<String, AQLNode>();
        // for inner level nodes
        Map<String, List<AQLNode>> attrMap = new HashMap<String, List<AQLNode>>();

        IFileEditorInput input = (IFileEditorInput) editor
        .getEditorInput();
        boolean isModularProject = ProjectUtils.isModularProject (input.getFile ().getProject ());
        activeProjectPath = input.getFile().getProject().getLocation();

        // adding project name as the first top level node
        AQLNode node = new AQLNode(0, 0, new File(activeProjectPath
          .toOSString()).getName(), Activator.ICON_PACKAGE);
        elementMap.put(activeProjectPath.toOSString(), node);
        add(node);
        //String currentFilePath = getCurrentFileRealPath();
        String currentFilePath = input.getFile ().getLocation ().toOSString ();
        if(!isModularProject){
          aqlLibrary = Activator.getLibrary();
        }else{
          aqlLibrary = Activator.getModularLibrary();
        }
        List<AQLElement> elements = aqlLibrary
        .getElements(currentFilePath);
        // System.out.println(elements);
        if (elements != null) {
          // System.out.println(elements);
          Iterator<AQLElement> iterator1 = elements.iterator();
          while (iterator1.hasNext()) {
            AQLElement elmt = iterator1.next();
            if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_INCLUDE) {
              if (elementMap.get("import") == null) {
                node = new AQLNode(getBeginOffset(elmt),
                  getEndOffset(elmt),
                  "import declarations",
                  Activator.ICON_IMPORT);
                elementMap.put("import", node);
                add(node);
              }
              List<AQLNode> list = (List<AQLNode>) attrMap
              .get("import");
              if (list == null) {
                list = new ArrayList<AQLNode>();
                attrMap.put("import", list);
              }
              list.add(new AQLNode(getBeginOffset(elmt) + 1,
									getEndOffset(elmt) + 1, elmt.getName(),
                Activator.ICON_IMPORT_FILE));
            } else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_VIEW) {
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_VIEW);
              elementMap.put(eleName, node);
              add(node);
            } else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_DICT) {
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_DICTIONARY);
              elementMap.put(eleName, node);
              add(node);
            } else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_EXTERNAL_VIEW) {
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_EXTERNAL_VIEW);
              elementMap.put(eleName, node);
              add(node);
            } else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_FUNC) {
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_FUNCTION);
              elementMap.put(eleName, node);
              add(node);
            } else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_SELECT) {
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_SELECT);
              elementMap.put(eleName, node);
              add(node);
            } else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_OUTPUT_VIEW) {
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_OUTPUT);
              elementMap.put(eleName, node);
              add(node);
            } else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_DETAG) {
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_DETAG);
              elementMap.put(eleName, node);
              add(node);
            } else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_TABLE) {
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_TABLE);
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_MODULE) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_MODULE);
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_REQUIRE_DOCUMENT) {
            	String eleName = elmt.getName();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_REQUIRE_DOCUMENT); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_EXPORT_FUNCTION) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_EXPORT_FUNCTION); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_EXPORT_DICTIONARY) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_EXPORT_DICTIONARY); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_EXPORT_TABLE) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_EXPORT_TABLE); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_EXPORT_VIEW) { 
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_EXPORT_VIEW); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_IMPORT_MODULE) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_IMPORT_MODULE); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_IMPORT_VIEW) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_IMPORT_VIEW); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_IMPORT_DICTIONARY) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_IMPORT_DICTIONARY); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_IMPORT_FUNCTION) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_IMPORT_FUNCTION); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_IMPORT_TABLE) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_IMPORT_TABLE); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_EXTERNAL_DICT) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_EXTERNAL_DICTIONARY); 
              elementMap.put(eleName, node);
              add(node);
            }else if (elmt.getType() == Constants.AQL_ELEMENT_TYPE_EXTERNAL_TABLE) {  
              String eleName = elmt.getUnQualifiedName ();
              node = new AQLNode(getBeginOffset(elmt),
                getEndOffset(elmt), eleName,
                Activator.ICON_EXTERNAL_TABLE); 
              elementMap.put(eleName, node);
              add(node);
            }
          }// end of while loop..
        } else {
          logger.logError("Not a valid AQL element");
        }
        // put ATTLIST to ELEMENT
        for (Iterator<Map.Entry<String, List<AQLNode>>> ite = attrMap
            .entrySet().iterator(); ite.hasNext();) {
          Map.Entry<String, List<AQLNode>> entry = ite.next();
          String key = entry.getKey();
          List<AQLNode> attrs = entry.getValue();
          AQLNode element = elementMap.get(key);
          for (int i = 0; i < attrs.size(); i++) {
            AQLNode attr = attrs.get(i);
            if (element == null) {
              add(attr);
              //
            } else {
              element.addChild(attr);
            }
          }
        }

      } catch (ClassCastException e) {
        logger.logError (e.getMessage ());
      } catch (Exception e) {
        e.printStackTrace();
        logger.logError (e.getMessage ());
      }
      // this.children.add(new AQLNode(0, 0, "hi baby", "hello baby"));
      return this.children.toArray(new AQLNode[this.children.size()]);
    }

    public void clear() {
      this.children.clear();
    }

    private String getNonQualifiedName(String elementName){
      String elementNames[] = elementName.split("\\.");//$NON-NLS-1$
      String aqlElementName = "";//$NON-NLS-1$
      if(elementNames.length == 2){
        aqlElementName = elementNames[1];
      }else if(elementNames.length == 1){
        aqlElementName = elementNames[0];
      }
      return aqlElementName;
    }

  }

  public class AQLContentProvider implements ITreeContentProvider {

    private AQLRootNode root;

    public Object[] getChildren(Object parentElement) {

      if (parentElement instanceof AQLRootNode) {
        return ((AQLRootNode) parentElement).getChildren();

      } else if (parentElement instanceof AQLNode) {
        return ((AQLNode) parentElement).getChildren();
      }
      return new Object[0];
    }

    public Object getParent(Object element) {
      if (element instanceof AQLNode) {
        AQLNode parent = ((AQLNode) element).getParent();
        if (parent == null) {
          return root;
        } else {
          return parent;
        }
      }
      return null;
    }

    public boolean hasChildren(Object element) {
      if (getChildren(element).length == 0) {
        return false;
      } else {
        return true;
      }
    }

    public Object[] getElements(Object inputElement) {
      return getChildren(inputElement);
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

  }

  /** This listener is called when selection of TreeViewer is changed. */
  public class AQLSelectionChangedListener implements
  ISelectionChangedListener {
    public void selectionChanged(SelectionChangedEvent event) {
      try {

        IStructuredSelection sel = (IStructuredSelection) event
        .getSelection();
        AQLNode element = (AQLNode) sel.getFirstElement();
        int offsetB = element.getStart();
        int length = element.getEnd () - offsetB + 1;
        //int length = element.getText().length();
        /*
        String eleName = element.getText ();
        String elementNames[] = eleName.split("\\.");//$NON-NLS-1$
        //String aqlElementName = "";//$NON-NLS-1$
        if(elementNames.length == 2){
          offsetB = offsetB + elementNames[0].length () + 1;
          length = elementNames[1].length ();
        }// if(element instanceof AQLNode){
        // int offset = ((AQLNode)element).getPosition(); */
        IWorkbenchPage page = PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow().getActivePage();
        IEditorPart editorPart = page.getActiveEditor();
        ITextEditor editor = (ITextEditor) editorPart;
        s = (StyledText) editor.getAdapter(Control.class);
        if (editorPart instanceof AQLEditor) {
          if (((AQLNode) element).getImage().equals("_icon_package")
              || ((AQLNode) element).getImage().equals(
              "_icon_import")) {
            ((AQLEditor) editorPart).selectAndReveal(0, 0);
          } else {
            try {
              ((AQLEditor) editorPart).selectAndReveal(offsetB,
                length);
            } catch (AssertionFailedException e) {
              logger.logError (e.getMessage ());
            }
          }
        }
      } catch (NullPointerException e) {
        // e.printStackTrace();
        logger.logError (e.getMessage ());
      }
    }
  }

  public static String getCurrentFileRealPath() {
    IWorkbenchWindow win = PlatformUI.getWorkbench()
    .getActiveWorkbenchWindow();
    IWorkbenchPage page = win.getActivePage();
    if (page != null) {
      IEditorPart editor = page.getActiveEditor();
      if (editor != null) {
        IEditorInput input = editor.getEditorInput();
        if (input instanceof IFileEditorInput) {
          return ((IFileEditorInput) input).getFile().getLocation()
          .toOSString();
        }
      }
    }
    return null;
  }

  public int getBeginOffset(AQLElement element) {
    int bOffset = 0;
    IWorkbenchPage page = PlatformUI.getWorkbench()
    .getActiveWorkbenchWindow().getActivePage();
    IEditorPart editorPart = page.getActiveEditor();
    ITextEditor editor = (ITextEditor) editorPart;
    s = (StyledText) editor.getAdapter(Control.class);
    try {
      bOffset = s.getOffsetAtLine(element.getBeginLine() - 1)
      + element.getBeginOffset() - 1;
    } catch (Exception e) {
      // e.printStackTrace();
    }
    return bOffset;
  }

  public int getEndOffset(AQLElement element) {
    int eOffset = 0;
    IWorkbenchPage page = PlatformUI.getWorkbench()
    .getActiveWorkbenchWindow().getActivePage();
    IEditorPart editorPart = page.getActiveEditor();
    ITextEditor editor = (ITextEditor) editorPart;
    s = (StyledText) editor.getAdapter(Control.class);
    try {
      eOffset = s.getOffsetAtLine(element.getBeginLine() - 1)
      + element.getEndOffset() - 1;
    } catch (Exception e) {
      // e.printStackTrace();
    }
    return eOffset;
  }

  @Override
  public void setSelection(int offset) {

  }

}
