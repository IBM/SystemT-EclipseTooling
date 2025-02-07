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
package com.ibm.biginsights.textanalytics.treeview.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.IServiceLocator;

import com.ibm.biginsights.textanalytics.concordance.model.IFiles;
import com.ibm.biginsights.textanalytics.concordance.model.impl.ConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.ui.ConcordanceView;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.resultviewer.util.ResultViewerUtil;
import com.ibm.biginsights.textanalytics.treeview.Activator;
import com.ibm.biginsights.textanalytics.treeview.control.TreeViewContentProvider;
import com.ibm.biginsights.textanalytics.treeview.control.TreeViewLabelProvider;
import com.ibm.biginsights.textanalytics.treeview.control.TreeViewListener;
import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.SpanTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeParent;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * This class is used to display the AQL Results in a Checked Tree View. Checking any element of the
 * tree will check and highlight all of the child elements
 * 
 *  Madiraju
 */

public class AQLResultTreeView extends ViewPart implements IPartListener2 {



  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "com.ibm.system.treeview.view.AQLResultTreeView"; //$NON-NLS-1$
  
  public static final String CMD_SHOW_VIEW_IN_EDITOR_MSG="labelShowViewInEditor"; //$NON-NLS-1$
  
  private CheckboxTreeViewer viewer;

  // A temporary place to store models for tables before the view gets created. There has to be a
  // better way to do this, but I haven't figured it out yet.
  // Mrudula used the same design as in the TableView and copied the above two lines from there
  private static final Map<String, TreeParent> modelMap = new HashMap<String, TreeParent>();

  private static final Map<String, IEditorInput> editorMap = new HashMap<String, IEditorInput>();

  protected TreeViewListener checkboxTreeListener;

  private static List<String>      nextpreviousDisabledItems = new ArrayList<String> ();  // To preserve selection across multiple view instances.
  private static List<String>      checkedViewAttributes = null;  // Eclipse goes thru all view instances and updates each dropdown menu. The result is 
  private static AQLResultTreeView focusedView = null;            // the dropdown menu is the one of the last processed view instance. These two variables
                                                                  // are used to avoid that problem.

  private ImageDescriptor checkedImageDescriptor = null;

  private static final String prevMenuContribId = "com.ibm.biginsights.textanalytics.treeview.GoToPreviousAnnot"; //$NON-NLS-1$
  private static final String nextMenuContribId = "com.ibm.biginsights.textanalytics.treeview.GoToNextAnnot"; //$NON-NLS-1$

  private static final String prevMenuCommandId = "com.ibm.biginsights.textanalytics.treeview.commands.GoToPreviousAnnot"; //$NON-NLS-1$
  private static final String nextMenuCommandId = "com.ibm.biginsights.textanalytics.treeview.commands.GoToNextAnnot"; //$NON-NLS-1$

  private static final String prevMenuParamId   = "com.ibm.biginsights.textanalytics.treeview.GoToPreviousAnnotPullDown"; //$NON-NLS-1$
  private static final String nextMenuParamId   = "com.ibm.biginsights.textanalytics.treeview.GoToNextAnnotPullDown"; //$NON-NLS-1$

  private static final String prevLocation      = "menu:" + prevMenuContribId; //$NON-NLS-1$
  private static final String nextLocation      = "menu:" + nextMenuContribId; //$NON-NLS-1$
  
  private boolean isModularProject;


  public static final void setModelForId(String id, TreeParent model) {
    modelMap.put(id, model);
  }
  

  public static final TreeParent getModelForId(String id) {
    TreeParent model = modelMap.get(id);
    // modelMap.remove(id)
    // Commented the above since a tree view may be displayed any number of times
    return model;
  }

  public static final void setEditorForViewId(String viewID, IEditorInput editor) {
    editorMap.put(viewID, editor);
  }

  public static final IEditorInput getEditorForId(String id) {
    return editorMap.get(id);
  }
  

  /**
   * This is a callback that will allow us to create the viewer and initialize it. This method gets
   * called automatically when rendering the view from the SystemTRunJob.java
   */
  public void createPartControl(Composite parent) {
    String secondaryViewID = getViewSite().getSecondaryId();
    if (secondaryViewID == null) {
      Label label = new Label(parent, SWT.BORDER);
      label.setText("To open AQL Result Tree Views, double click on the rows of the Annotation Explorer");
      return;
    }
    PatternFilter patternFilter = new PatternFilter();
    FilteredCheckboxTree fchbTree = new FilteredCheckboxTree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL,patternFilter);
	  viewer = (CheckboxTreeViewer)fchbTree.getViewer();
    viewer.setContentProvider(new TreeViewContentProvider());
    viewer.setLabelProvider(new TreeViewLabelProvider(viewer));
    TreeParent model = getModelForId(secondaryViewID);
    viewer.setAutoExpandLevel(3); // This is the expand level for the treeview
    viewer.setInput(model);
    // String modifiedSecondaryViewID = StringUtils.decodeFileName(secondaryViewID);
    String modifiedSecondaryViewID = ((EditorInput) getEditorForId(secondaryViewID)).getName();//getModel()
        //.getDocumentID();
    this.setPartName(modifiedSecondaryViewID); // This sets the name of the outputView for
                                               // eg:-"PersonPhoneAll"
    checkboxTreeListener = new TreeViewListener(viewer, secondaryViewID);
    viewer.addCheckStateListener(checkboxTreeListener);
    focusedView = this;
    updateCheckedViewAttributes();
    populateShowDocPulldown(modifiedSecondaryViewID);
    populateNextPreviousPulldowns();
    getViewSite ().getPage ().addPartListener (this);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.result_tree_view");//$NON-NLS-1$
    
    addContextMenu();
    
    /*
     *  TODO: This method is added as a work-around to fix the tooling defect RTC-48707.
     * Which is resulted due to the Eclipse 4.2.2 bugs: 401709 and 366528.
     * We need to remove this API and uncomment the commented code in tree viewer plugin.xml when above defects are fixed.   
     */
    createDynamicToolBarIcons();
    
  }
  
  /**
   * This method is to create the tool bar icons and add them to the Result Table view programmatically 
   * due to the eclipse 4.2.2 defects, This API should be re visited when we migrate it to higher versions or 
   * when those eclipse defects fixed.
   */
  private void createDynamicToolBarIcons() {

    ToolBarManager viewMenuMgr = (ToolBarManager)getViewSite().getActionBars().getToolBarManager();

    CommandContributionItemParameter showAllDocs = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.treeview.ShowAllDocs", // id
          "com.ibm.biginsights.textanalytics.treeview.commands.ShowAllDocs", // command id
          null,  // command parameter map
          Activator.getImageDescriptor("tb_showAllDocs.gif"), // icon
          null,
          null, // hover icon
          "Show All Documents That Are Annotated", // label
          "t", // mnemonic
          "Show All Documents That Are Annotated", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    CommandContributionItemParameter showDocsWithAnnots = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.treeview.ShowAllDocsWithAnnotations", // id
          "com.ibm.biginsights.textanalytics.treeview.commands.ShowAllDocsWithAnnotations", // command id
          null,  // command parameter map
          Activator.getImageDescriptor("tb_showAllDocs_Anno.gif"), // icon
          null, // disabled icon
          null, // hover icon
          "Show All Documents with Selected Annotations", // label
          "t", // mnemonic
          "Show All Documents with Selected Annotations", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    CommandContributionItemParameter prevAnnotate = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.treeview.GoToPreviousAnnot", // id
          "com.ibm.biginsights.textanalytics.treeview.commands.GoToPreviousAnnot", // command id
          null,  // command parameter map
          Activator.getImageDescriptor("tb_upArrow.gif"), // icon
          null, // disabled icon
          null, // hover icon
          "Go To Previous Annotation", // label
          "t", // mnemonic
          "Previous Annotation in This Document", // tooltip
          //SWT.DROP_DOWN, // style  //TODO This code is commented and added below line as a work-around for the eclipse defect 366528, replace it with below line when this defect is fixed.
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    CommandContributionItemParameter nextAnnotate = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.treeview.GoToNextAnnot", // id
          "com.ibm.biginsights.textanalytics.treeview.commands.GoToNextAnnot", // command id
          null,  // command parameter map
          Activator.getImageDescriptor("tb_downArrow.gif"), // icon
          null, // disabled icon
          null, // hover icon
          "Go To Next Annotation", // label
          "t", // mnemonic
          "Next Annotation in this Document", // tooltip
          //SWT.DROP_DOWN, // style  //TODO This code is commented and added below line as a work-around for the eclipse defect 366528, replace it with below line when this defect is fixed.
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    CommandContributionItemParameter showPrevAnnotDoc = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.treeview.ShowPrevious", // id
          "com.ibm.biginsights.textanalytics.treeview.commands.ShowPrevious", // command id
          null,  // command parameter map
          Activator.getImageDescriptor("tb_previous.gif"), // icon
          null, // disabled icon
          null, // hover icon
          "Show Previous Document That Is Annotated", // label
          "t", // mnemonic
          "Show Previous Document That Is Annotated", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    CommandContributionItemParameter showNextAnnotDoc = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.treeview.ShowNext", // id
          "com.ibm.biginsights.textanalytics.treeview.commands.ShowNext", // command id
          null,  // command parameter map
          Activator.getImageDescriptor("tb_next.gif"), // icon
          null, // disabled icon
          null, // hover icon
          "Show Next Document That Is Annotated", // label
          "t", // mnemonic
          "Show Next Document That Is Annotated", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    CommandContributionItemParameter showPrevWithAnnot = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.treeview.ShowPreviousWithAnnot", // id
          "com.ibm.biginsights.textanalytics.treeview.commands.ShowPreviousWithAnnot", // command id
          null,  // command parameter map
          Activator.getImageDescriptor("tb_showPrevDoc_Anno.gif"), // icon
          null, // disabled icon
          null, // hover icon
          "Show Previous Document With Selected Annotation", // label
          "t", // mnemonic
          "Show Previous Document With Selected Annotation", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    CommandContributionItemParameter showNextWithAnnot = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.treeview.ShowNextWithAnnot", // id
          "com.ibm.biginsights.textanalytics.treeview.commands.ShowNextWithAnnot", // command id
          null,  // command parameter map
          Activator.getImageDescriptor("tb_showNextDoc_Anno.gif"), // icon
          null, // disabled icon
          null, // hover icon
          "Show Next Document With Selected Annotation", // label
          "t", // mnemonic
          "Show Next Document With Selected Annotation", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    /*
     *  TODO: We are hiding/removing the drop down menu bar to choose the view name 
     *  this is not working due to the eclipse defect 366528.
     *  
    CommandContributionItemParameter goToDoc = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.treeview.GoToDocPulldown", // id
          "com.ibm.biginsights.textanalytics.treeview.commands.GoToDoc", // command id
          null,  // command parameter map
          Activator.getImageDescriptor("tb_gotoDoc.gif"), // icon
          null, // disabled icon
          null, // hover icon
          "Go To Document", // label
          "t", // mnemonic
          "Go To Document", // tooltip
          SWT.DROP_DOWN, // style
          null, // help context id
          true // visibility tracking
            );
    */

    CommandContributionItemParameter closeAll = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.treeview.CloseAll", // id
          "com.ibm.biginsights.textanalytics.treeview.commands.CloseAll", // command id
          null,  // command parameter map
          Activator.getImageDescriptor("treeViews_closeAll.gif"), // icon
          null, // disabled icon
          null, // hover icon
          "Close All Tree Views", // label
          "t", // mnemonic
          "Close All Tree Views", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    viewMenuMgr.add(new CommandContributionItem(showAllDocs));
    viewMenuMgr.add(new CommandContributionItem(showDocsWithAnnots));
    viewMenuMgr.add(new CommandContributionItem(prevAnnotate));
    viewMenuMgr.add(new CommandContributionItem(nextAnnotate));
    viewMenuMgr.add(new CommandContributionItem(showPrevAnnotDoc));
    viewMenuMgr.add(new CommandContributionItem(showNextAnnotDoc));
    viewMenuMgr.add(new CommandContributionItem(showPrevWithAnnot));
    viewMenuMgr.add(new CommandContributionItem(showNextWithAnnot));
    //viewMenuMgr.add(new CommandContributionItem(goToDoc));  Uncomment this when above todo is addressed.
    viewMenuMgr.add(new CommandContributionItem(closeAll));

    viewMenuMgr.update(true);   

  }  
  
  private void addContextMenu() {
	    class ShowViewInEditorContextAction extends Action {
	      IStructuredSelection selection;

	      public ShowViewInEditorContextAction(IStructuredSelection selection) {
	        this.selection = selection;
	        EditorInput eip = (EditorInput) getEditorForId(getViewSite().getSecondaryId());
	        isModularProject = ProjectUtils.isModularProject (eip.getCurrentProjectReference ());
	      }
	      
	      public void run() {
	    	  Object treeObj = this.selection.getFirstElement();
	    	  TreeParent treeParentNode = null;  
	    	  SpanTreeObject treeSpanNode = null;
	    	  String viewName = null; 
	    	  String projName = null; 
	    	  EditorInput ip = (EditorInput) getEditorForId(getViewSite().getSecondaryId());
	    	  projName = ip.getCurrentProjectReference();
	    	  //projName = ip.currentProjectReference;
	    	  if(treeObj instanceof TreeParent){
	    		  treeParentNode = (TreeParent)treeObj;
	    		  int index = treeParentNode.getName().indexOf("SPAN"); //$NON-NLS-1$
		    	  if(index != -1){
		    	 	  viewName = treeParentNode.getParent().getName();
		    	  }else {
		    		  viewName = treeParentNode.getName();
		    	  }
	    	  }else if(treeObj instanceof SpanTreeObject){
	    		  treeSpanNode = (SpanTreeObject)treeObj;
	    		  viewName = treeSpanNode.getParent().getParent().getName();
	    	  }
	        if(Messages.getString(CMD_SHOW_VIEW_IN_EDITOR_MSG).equalsIgnoreCase(this.getText())){ 
	        	IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
	      		try {
	      			ArrayList<Parameterization> parameters = new ArrayList<Parameterization>();
	      			ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
	      			Command openCommand = commandService.getCommand(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID);
	      			
	      			IParameter viewParam = openCommand.getParameter(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID_PARAM_VIEW_NAME);
	      			Parameterization viewParmeterization = new Parameterization(viewParam, viewName );
	      			parameters.add(viewParmeterization);
	      			
	      			IParameter projectParam = openCommand.getParameter(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID_PARAM_PROJ_NAME);
	      			Parameterization projParmeterization = new Parameterization(projectParam, projName);
	      			parameters.add(projParmeterization);
	      			
	      			ParameterizedCommand parmCommand = new ParameterizedCommand( openCommand, 
	      					parameters.toArray(new Parameterization[parameters.size()]));
	                handlerService.executeCommand(parmCommand, null);
	      			
	                } catch (Exception ex) {
	                	ex.printStackTrace();
      				}
	      		}
	      	}
	    }
	    
	    final MenuManager mgr = new MenuManager();
	    IMenuListener listener = new IMenuListener() {
	      public void menuAboutToShow(IMenuManager manager) {
	        IStructuredSelection selection =
	        	(IStructuredSelection) viewer.getSelection();
	        Object treeObj1 = selection.getFirstElement();
	        boolean isRootNode = false;
	        if(treeObj1 instanceof TreeParent){
	        	TreeParent tp = (TreeParent)treeObj1;
	        	if("Annotations".toLowerCase().contains(tp.getName().toLowerCase())){//$NON-NLS-1$
	        		isRootNode = true;
	        	}
	        }
	        if ((!selection.isEmpty())&&(!isRootNode)) {
	        	ShowViewInEditorContextAction menuAction = new ShowViewInEditorContextAction(selection);
	        	menuAction.setText(Messages.getString(CMD_SHOW_VIEW_IN_EDITOR_MSG)); 
	        	if(isModularProject){
	        	  mgr.add(menuAction);
	        	}
	        }
	      }
	    };

	    mgr.addMenuListener(listener);
	    mgr.setRemoveAllWhenShown(true); // this is to dynaically create the menu every time..
	    // Adding context menu to table cursor..
	    this.viewer.getControl().setMenu(mgr.createContextMenu(this.viewer.getControl()));
	    
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  public void setFocus() {
    if (viewer != null) {
      viewer.getControl().setFocus();
    }
  }

  public CheckboxTreeViewer getViewer() {
    return viewer;

  }

  /*
   * Note: Need to explicitly fire an event to the listener, since TreeViewer.setChecked() does not
   * fire events. Unless we fire events, the checked elements won't be highlighted in the annotation
   * editor
   */
  public void fireCheckStateChanged(final CheckStateChangedEvent event) {
    checkboxTreeListener.checkStateChanged(event);
  }

  private static AbstractContributionFactory menuFactory = null;
  private static AbstractContributionFactory menuFactory4Prev = null;
  private static AbstractContributionFactory menuFactory4Next = null;
  
  
  
  /**
   * This method is used to populate the TreeViewer with all result file names for the Go To Document menu pull down item.
   */
  
private Iterator<String> getFileNames(String partName)
{
	  ArrayList<String> fileNames = new ArrayList<String>();  
	  if ((partName.endsWith(Constants.GS_FILE_EXTENSION_WITH_DOT)) || (partName.contains(Constants.GS_FILE_EXTENSION_WITH_DOT+Constants.STRF_FILE_EXTENSION_WITH_DOT)) )
	  {
		  		Serializer srlzr = new Serializer();
		  		SystemTComputationResult returnResult = null;
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IFile lcFile = workspace.getRoot().getFile(new Path(partName));
				IFolder resultFolder = (IFolder)lcFile.getParent();
				try
				{
					IResource[] members = resultFolder.members();
					for(int j=0;j<members.length;j++ )
					{
						if (members[j] instanceof IFile)
						{
							if (ResultViewerUtil.isValidLCorSTRFFile((IFile)members[j]))
							{
								returnResult = srlzr.getModelForInputStream(((IFile)members[j]).getContents());
								fileNames.add(returnResult.getDocumentID());
							}
						}
					}
				}
				catch(CoreException e)
				{
					// do nothing
					System.err.println(e.getMessage());
				}
	  }
	  else
	  {
			IWorkbenchPage page = PlatformUI.getWorkbench()
			.getActiveWorkbenchWindow().getActivePage();
			if (page != null) {
					ConcordanceView concView = (ConcordanceView) page
						.findView(ConcordanceView.VIEW_ID);
					if (null != concView) {
						ConcordanceModel model = (ConcordanceModel) concView.getModel();
						IFiles files = model.getFiles();
						Set<String> fileNamesTemp = files.getFiles();
						fileNames.addAll(fileNamesTemp);
					}
			}
	  }
	  final Iterator<String> fileNamesIter = fileNames.iterator();
	  return fileNamesIter;

}
  private void populateShowDocPulldown(final String partName)
  {
	  
	  // Update views menu
	  IMenuService menuService = (IMenuService) PlatformUI.getWorkbench().getService(
	      IMenuService.class);
	  final String location = "menu:com.ibm.biginsights.textanalytics.treeview.GoToDocPulldown"; //$NON-NLS-1$
	  // Remove the previous contribution factory, if it exists
	  if (menuFactory != null) {
	    menuService.removeContributionFactory(menuFactory);
	  }
	  menuFactory = new AbstractContributionFactory(location, null) {
	
	    @Override
	    public void createContributionItems(IServiceLocator serviceLocator,
	        IContributionRoot additions) {
	    	Iterator<String> fileNamesIter = getFileNames(partName);
	      String fileName = "";
			while (fileNamesIter.hasNext())
			{
				fileName = fileNamesIter.next();
	        Map<String, String> paramMap = new HashMap<String, String>();
	        paramMap.put("com.ibm.biginsights.textanalytics.treeview.gotodocpulldown", fileName); //$NON-NLS-1$
	        CommandContributionItemParameter param = new CommandContributionItemParameter(
	            serviceLocator,
	            "test.id.1", //$NON-NLS-1$
	            "com.ibm.biginsights.textanalytics.treeview.commands.GoToDoc", paramMap, null, //$NON-NLS-1$
	            null, null, fileName, null, Messages.showOutputViewMessage1 + fileName
	                + Messages.showOutputViewMessage2, CommandContributionItem.STYLE_PUSH, null,
	            true);
	        try
	        {
	        	CommandContributionItem item = new CommandContributionItem(param);
	        	additions.addContributionItem(item, null);
	        }
	        catch(Exception e)
	        {
	        	// ignore exception
	        }
	      }
	    }
	  };
	  menuService.addContributionFactory(menuFactory);

  }

  private void populateNextPreviousPulldowns()
  {
    IMenuService menuService = (IMenuService) PlatformUI.getWorkbench().getService(IMenuService.class);

    // Remove the previous contribution factory, if it exists
    // Should be both null or not-null at the same time.
    if (menuFactory4Prev != null)
      menuService.removeContributionFactory(menuFactory4Prev);

    if (menuFactory4Next != null)
      menuService.removeContributionFactory(menuFactory4Next);

    menuFactory4Prev = new AbstractContributionFactory(prevLocation, null) {
      @Override
      public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
        for (String label : checkedViewAttributes) {
          createDropdownMenuItem (prevMenuCommandId, prevMenuParamId, serviceLocator, additions, label);
        }
      }
    };

    menuFactory4Next = new AbstractContributionFactory(nextLocation, null) {
      @Override
      public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
        for (String label : checkedViewAttributes) {
          createDropdownMenuItem (nextMenuCommandId, nextMenuParamId, serviceLocator, additions, label);
        }
      }
    };

    // Update views menu
    menuService.addContributionFactory(menuFactory4Prev);
    menuService.addContributionFactory(menuFactory4Next);
  }


  /**
   * @param prevMenuCommandId
   * @param prevMenuParamId
   * @param serviceLocator
   * @param additions
   * @param label
   */
  private void createDropdownMenuItem (String prevMenuCommandId,
                                       String prevMenuParamId,
                                       IServiceLocator serviceLocator,
                                       IContributionRoot additions,
                                       String label)
  {
    Map<String, String> paramMap = new HashMap<String, String> ();
    paramMap.put (prevMenuParamId, label); //$NON-NLS-1$

    ImageDescriptor icon = null;
    if (nextpreviousDisabledItems.contains (label))
      icon = null;
    else
      icon = getCheckedImageDescriptor();

    // Chop the suffix (SPAN) from the view attribute
    String displayed_label = label.endsWith (" (SPAN)") ? label.substring (0, label.length () - 7) : label;

    CommandContributionItemParameter param = new CommandContributionItemParameter (serviceLocator,
                                                                                   label,             // id -- Use the label as id for this item
                                                                                   prevMenuCommandId,
                                                                                   paramMap,
                                                                                   icon, null, null,  // the icons
                                                                                   displayed_label,
                                                                                   null,              // mnemonic
                                                                                   null,              // tooltip
                                                                                   CommandContributionItem.STYLE_PUSH,
                                                                                   null,              // helpContextId
                                                                                   true);
    try {
      CommandContributionItem item = new CommandContributionItem (param);
      additions.addContributionItem (item, null);
    }
    catch (Exception e) {
      // ignore exception
    }
  }


  /**
   * Toggle the check mark of a view attribute in the Next/Previous dropdown.
   * @param outputViewAttrPair The view attribute shown in the Next/Previous dropdown
   */
  public void toggleCheckMarkInNextPrevDropdown(String outputViewAttrPair) {

    if (nextpreviousDisabledItems.contains (outputViewAttrPair))
      nextpreviousDisabledItems.remove (outputViewAttrPair);
    else
      nextpreviousDisabledItems.add (outputViewAttrPair);

    populateNextPreviousPulldowns ();

  }


  /**
   * Verify if it's OK to navigate to this annotation; i.e., it does not belong
   * to a view attribute that is unselected in the Next/Previous dropdown list.
   * @param annot The annotation
   * @return
   */
  public boolean isAnnotationOKToNavigate (Annotation annot)
  {
    String viewText = annot.getText ();
    for (String viewAttr : nextpreviousDisabledItems) {
      if (viewText.startsWith (viewAttr + "."))
        return false;
    }

    return true;
  }


  private ImageDescriptor getCheckedImageDescriptor ()
  {
    if (checkedImageDescriptor == null) 
      checkedImageDescriptor = Activator.getImageDescriptor ("tb_checked.gif");

    return checkedImageDescriptor;
  }


  /**
   * Get the list of view attribute name of checked objects in the annotation tree.
   */
  private List<String> getCheckedViewAttrs(AQLResultTreeView view)
  {
    List<String> checkedViewAttrs = new ArrayList<String> ();

    if (view != null && view.getViewer () != null && view.getViewer ().getInput () != null) {
      TreeParent model = (TreeParent) view.getViewer ().getInput ();
      TreeParent root = (TreeParent) model.getChildren ()[0];

      for (ITreeObject tObj : root.getChildren ()) {
        TreeParent treeObject = (TreeParent) tObj;
        String aqlViewName = treeObject.getName ();

        for (ITreeObject attrObj : treeObject.getChildren ()) {
          TreeParent attrTreeObject = (TreeParent) attrObj;

          String attrName = attrTreeObject.getName ();
          String label = aqlViewName + "." + attrName;

          // This item is checked in the result tree. Add it to the list.
          if (view.getViewer ().getChecked (attrTreeObject)) {
            checkedViewAttrs.add (label);
          }
          else {
            // This item is not checked in the result tree. Remove it from the list
            // of items disabled for Next/Previous navigation so next time when the
            // user checks it in the tree, it is also ON in this list.
            nextpreviousDisabledItems.remove (label);
          }
        }
      }
    }

    return checkedViewAttrs;
  }

  public void updateCheckedViewAttributes()
  {
    checkedViewAttributes = getCheckedViewAttrs(focusedView);
  }

  public boolean hasCheckedViewAttrsForNextPrev()
  {
	if(!(checkedViewAttributes == null))
	{
	    List<String> navigableViewAttributes = new ArrayList<String> (checkedViewAttributes);
	    navigableViewAttributes.removeAll (nextpreviousDisabledItems);
	    return ( ! navigableViewAttributes.isEmpty () );
	}
	return false;
  }

  /*----------------------- IPartListener2 implementation -----------------------*/

  @Override
  public void partActivated (IWorkbenchPartReference partRef)
  {
    IWorkbenchPart focusedPart = partRef.getPart (true);
    if (focusedPart instanceof AQLResultTreeView) {
      focusedView = (AQLResultTreeView)focusedPart;
      updateCheckedViewAttributes();
      populateNextPreviousPulldowns();
    }
  }

  @Override
  public void partBroughtToTop (IWorkbenchPartReference partRef)
  {}

  @Override
  public void partClosed (IWorkbenchPartReference partRef)
  {
    IWorkbenchPart closedPart = partRef.getPart (true);
    if (focusedView == closedPart)
      focusedView = null;
  }

  @Override
  public void partDeactivated (IWorkbenchPartReference partRef)
  {}

  @Override
  public void partHidden (IWorkbenchPartReference partRef)
  {}

  @Override
  public void partInputChanged (IWorkbenchPartReference partRef)
  {}

  @Override
  public void partOpened (IWorkbenchPartReference partRef)
  {}

  @Override
  public void partVisible (IWorkbenchPartReference partRef)
  {}
  

  public void dispose() {
    // stop listening to part activation
    getSite().getPage().removePartListener(this);
    super.dispose();
  }

}
