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
package com.ibm.biginsights.textanalytics.tableview.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.ViewPart;

import com.ibm.biginsights.textanalytics.resultviewer.util.PaginationTracker;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.tableview.Activator;
import com.ibm.biginsights.textanalytics.tableview.Messages;
import com.ibm.biginsights.textanalytics.tableview.control.CellContentProvider;
import com.ibm.biginsights.textanalytics.tableview.control.CellKeyListener;
import com.ibm.biginsights.textanalytics.tableview.control.CellLabelProvider;
import com.ibm.biginsights.textanalytics.tableview.control.CellMouseListener;
import com.ibm.biginsights.textanalytics.tableview.control.SelectionListener;
import com.ibm.biginsights.textanalytics.tableview.model.IAQLTableViewModel;
import com.ibm.biginsights.textanalytics.tableview.model.impl.TableViewComparator;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class AQLResultView extends ViewPart {



  public static final String VIEW_ID = "com.ibm.biginsights.textanalytics.tableview.view"; //$NON-NLS-1$
  
  public static final String CMD_SHOW_VIEW_IN_EDITOR_MSG="labelShowViewInEditor"; //$NON-NLS-1$
 
  public static String currentProject = null; //$NON-NLS-1$
  // A temporary place to store models for tables before the view gets created. There has to be a
  // better way to do this, but I haven't figured it out yet.
  private static final Map<String, IAQLTableViewModel> modelMap = new HashMap<String, IAQLTableViewModel>();

  private static final Map<String, ProvenanceRunParams> paramsMap = new HashMap<String, ProvenanceRunParams>();
  
  private static final Map<String,String> outputViewActualViewNameMap = new HashMap<String,String>();

  //The following two parameters help keep track of last selected row in AQL Result table.
  private String annotationText;
  private String docName;

  private TableViewer tableViewer;

  private TableCursor cursor;

  private static String tempDirPath;

  private CellMouseListener cml = null;
  
  private CellKeyListener ckl = null;
  
  private TableViewComparator comparator = null;
  
  private boolean isModularProject;

  /**
   * Returns the Model from Result View
   * 
   * @return
   */
  public IAQLTableViewModel getModel() {
    return (IAQLTableViewModel) this.tableViewer.getInput();
  }

  public static final void setModelForId(String id, IAQLTableViewModel model) {
    modelMap.put(id, model);
  }

  public static final IAQLTableViewModel getModelForId(String id) {
    IAQLTableViewModel model = modelMap.get(id);
    modelMap.remove(id);
    return model;
  }

  @Override
  public void createPartControl(Composite parent) {
    final String secondaryViewID = getViewSite().getSecondaryId();
    if (secondaryViewID == null) {
      Label label = new Label(parent, SWT.BORDER);
      label.setText(Messages.getString("AQLResultView.AQLTableView_Open_Instrn")); //$NON-NLS-1$
      return;
    }
    IAQLTableViewModel model = getModelForId(secondaryViewID);
    final int swtStyle = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION
        | SWT.VIRTUAL;
    this.tableViewer = new TableViewer(parent, swtStyle);
    
    // Set up the viewers table control.
    final Table table = this.tableViewer.getTable();
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    table.getAccessible ().addAccessibleListener (new AccessibleAdapter () {
      @Override
      public void getName (AccessibleEvent e)
      {
        /**
         * For task 34248.
         * This is kind of a hacky fix. Since the table used here does not have cells that support accessibility api,
         * there was no way to do this at the row or cell level. We may have had to create our own custom table widget.
         * Instead, this fix takes advantage of the way JAWS reads table rows currently.
         * i.e. table name (which is taken as the value of the 1st column) + names and values of subsequent columns.
         * So for this fix, when information on a row is sought, we provide the name and value of the 1st column, for table name.
         */
        if (e.childID > -1) { //if childID is non-negative, information on a row in the table is being sought
          if (table.getItemCount () > 0) {
            e.result = table.getColumn (0).getText () + " : " + table.getItem (e.childID).getText (0); //$NON-NLS-1$
          }
        } else { //if childID is -1, information on the table is being sought; we'll return the view name.
          e.result = Messages.getString("AQLResultView.ResultTableAccessibilityName")+secondaryViewID //$NON-NLS-1$
              + " : " + getContentDescription (); //$NON-NLS-1$
        }
      }
    });

    String[] columnHeaders = model.getHeaders();
    for (int i = 0; i < columnHeaders.length; i++) {
      TableColumn tc = new TableColumn(table, SWT.LEFT, i);
      tc.setText(columnHeaders[i]);
      tc.setWidth(100);
    }
    this.tableViewer.setLabelProvider(new CellLabelProvider());
    this.tableViewer.setContentProvider(new CellContentProvider());
    this.tableViewer.setInput(model);
    this.setPartName(secondaryViewID);
    Object[] params = new Object[3];
    params[0] = model.getNumRows();
    params[1] = PaginationTracker.getInstance().getCurrentPage();
    params[2] = PaginationTracker.getInstance().getTotalNumberOfPages();
    String header = Messages.getString("AQLResultView.Header",params); //$NON-NLS-1$
    if (model.getNumRows() == 0) {
    	header = Messages.getString("AQLResultView.Header_noSpansInPage",params); //$NON-NLS-1$
    }
    this.setContentDescription(header);
    // Create the cursor which allows individual table cells to be selected (as opposed to just the
    // complete row).
    this.cursor = new TableCursor(table, SWT.NONE);
    // Add a mouse listener to the cursor that listens for double click events. The cursor provides
    // the column, the the selection listener the model content for the table cell. If there is an
    // easier way to do this, I haven't found it yet.
    String actualViewName = outputViewActualViewNameMap.get (secondaryViewID);
    if (actualViewName == null || actualViewName.isEmpty ()) {
      actualViewName = secondaryViewID;
    }
    this.cml = new CellMouseListener(this.cursor, secondaryViewID, actualViewName, getParamsForId(secondaryViewID),
        tempDirPath, columnHeaders, currentProject);
    this.ckl = new CellKeyListener ();
    this.cursor.addMouseListener(this.cml);
    this.cursor.addKeyListener (this.ckl);
    this.tableViewer.addSelectionChangedListener(new SelectionListener(this.cml, this.ckl));
    this.tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged (SelectionChangedEvent event)
      {
        //Update selection tracking class properties
        TableItem[] items = tableViewer.getTable ().getSelection ();
        if (items != null) {
          TableItem item = items[0];
          int inputDocIdx = getInputColumnIndexInResultTable (table);
          if (inputDocIdx > 0) {
            docName = item.getText(inputDocIdx);
            annotationText = item.getText (0);
            
          }          
        }
      }
    });
    // Set up column sorting
    this.comparator = new TableViewComparator(model);
    this.tableViewer.setComparator(this.comparator);
    createSorterListeners();
    
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.result_table_view");//$NON-NLS-1$
    addContextMenu();
	
    /*
     *  TODO: This method is added as a work-around to fix the tooling defect RTC-48707.
     * Which is resulted due to the Eclipse 4.2.2 bugs: 401709 and 366528.
     * We need to remove this API and un comment the commented code in table viewer plugin.xml when above defects are fixed.   
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
    
    CommandContributionItemParameter paramPrev = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.tableview.showprevpage", // id
          "com.ibm.biginsights.textanalytics.tableview.ShowPrevPage", // command id
          null,	// command parameter map
          Activator.getImageDescriptor("back.gif"), // icon
          Activator.getImageDescriptor("disabledBack.gif"), // disabled icon
          null, // hover icon
          "Show Previous Page", // label
          "t", // mnemonic
          "Show Previous Page", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    CommandContributionItemParameter paramNext = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.tableview.shownextpage", // id
          "com.ibm.biginsights.textanalytics.tableview.ShowNextPage", // command id
          null,	// command parameter map
          Activator.getImageDescriptor("forward.gif"), // icon
          Activator.getImageDescriptor("disabledForward.gif"), // disabled icon
          null, // hover icon
          "Show Next Page", // label
          "t", // mnemonic
          "Show Next Page", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    CommandContributionItemParameter paramOpen = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.tableview.menu.opentexteditor", // id
          "com.ibm.biginsights.textanalytics.tableview.openeditor", // command id
          null,	// command parameter map
          Activator.getImageDescriptor("text.gif"), // icon
          Activator.getImageDescriptor("text-inactive.gif"), // disabled icon
          null, // hover icon
          "Open span in text editor", // label
          "t", // mnemonic
          "Open Selected Span in Text Editor", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    /*
     *  TODO: We are hiding/removing the drop down menu bar to choose the view name 
     *  this is not working due to the eclipse defect 366528.
     *  
	CommandContributionItemParameter paramViews = new CommandContributionItemParameter
			(getSite(), // serviceLocator
			 "com.ibm.biginsights.textanalytics.concordance.views.pulldown", // id
			 "com.ibm.biginsights.textanalytics.concordance.commands.views", // command id
			 null,	// command parameter map       
			 Activator.getImageDescriptor("tableView.gif"), // icon
			 null, // disabled icon
			 null, // hover icon
			 "Views", // label
			 "t", // mnemonic
			 "Select Output Views to Display in Result Table View", // tooltip
			 SWT.DROP_DOWN, // style 
			 null, // help context id
			 false // visibility tracking
			 );
     */
    CommandContributionItemParameter paramExport = new CommandContributionItemParameter
        (getSite(), // serviceLocator
          "com.ibm.biginsights.textanalytics.tableView.filter.export.pulldown", // id
          "com.ibm.biginsights.textanalytics.concordance.commands.resultExport", // command id
          null,	// command parameter map
          Activator.getImageDescriptor("tb_export.gif"), // icon
          null, // disabled icon
          null, // hover icon
          "Export View", // label
          "f", // mnemonic
          "Export Result", // tooltip
          SWT.PUSH, // style
          null, // help context id
          false // visibility tracking
            );

    viewMenuMgr.add(new CommandContributionItem(paramPrev));
    viewMenuMgr.add(new CommandContributionItem(paramNext));
    viewMenuMgr.add(new CommandContributionItem(paramOpen));
    //viewMenuMgr.add(new CommandContributionItem(paramViews));  // Removing/Hiding the select view drop down list.
    viewMenuMgr.add(new CommandContributionItem(paramExport));

    viewMenuMgr.update(true);	  

  }
  
  @SuppressWarnings("unused")
  private void addContextMenu() {
	    class ShowViewInEditorAction  extends Action {
        IStructuredSelection selection;

	      public ShowViewInEditorAction(IStructuredSelection selection) {
	        this.selection = selection;
	        isModularProject = ProjectUtils.isModularProject (currentProject);
	      }

	      public void run() {
	        String viewName = null;//$NON-NLS-1$
	        viewName = getViewSite().getSecondaryId();
	        if(Messages.getString(CMD_SHOW_VIEW_IN_EDITOR_MSG).equalsIgnoreCase(this.getText())){
	        	IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
	      		try {
	      			ArrayList<Parameterization> parameters = new ArrayList<Parameterization>();
	      			ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
	      			Command openCommand = commandService.getCommand(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID);
	      			
	      			IParameter viewNameParam = openCommand.getParameter(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID_PARAM_VIEW_NAME);
	      			Parameterization viewParam = new Parameterization(viewNameParam, viewName );
	      			parameters.add(viewParam);
	      			
	      			IParameter currProjNameParam = openCommand.getParameter(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID_PARAM_PROJ_NAME);
	      			Parameterization CurrProjParam = new Parameterization(currProjNameParam, currentProject);
	      			parameters.add(CurrProjParam);
	      			
	      			ParameterizedCommand paramCommand = new ParameterizedCommand( openCommand, 
	      					parameters.toArray(new Parameterization[parameters.size()]));
	                handlerService.executeCommand(paramCommand, null);
	      			
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
	        	(IStructuredSelection) tableViewer.getSelection();

	        if (!selection.isEmpty()) {
	        	ShowViewInEditorAction act = new ShowViewInEditorAction(selection);
	          act.setText(Messages.getString(CMD_SHOW_VIEW_IN_EDITOR_MSG));
	          if((isModularProject)){
	            mgr.add(act);
	          }
	        }
	      }
	    };

	    mgr.addMenuListener(listener);
	    mgr.setRemoveAllWhenShown(true); // This is to dynamically create the new menu on selected entry..
	    // Adding context menu to table cursor..
	    this.cursor.setMenu(mgr.createContextMenu( this.cursor.getShell()));

  }

  // Set up the comparator, and the sorting listeners.
  private final void createSorterListeners() {
    for (int i = 0; i < this.tableViewer.getTable().getColumnCount() - 1; i++) {
      createColumnSelectionListener(this.tableViewer.getTable().getColumn(i), i);
    }
  }

  private final void createColumnSelectionListener(final TableColumn column, final int sortOrder) {
    column.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        AQLResultView.this.comparator.sortByColumn(sortOrder);
        AQLResultView.this.tableViewer.getTable().setSortColumn(column);
        AQLResultView.this.tableViewer.getTable().setSortDirection(SWT.DOWN);
        AQLResultView.this.tableViewer.refresh();
      }
    });
  }

  @Override
  public void setFocus ()
  {
    if (this.tableViewer != null) {
      Table table = this.tableViewer.getTable ();
      setHighLightedRow (this.docName, this.annotationText); //Highlight row containing this document name and annotation text

      if (table != null && table.getItemCount () > 0) {
          table.setFocus (); // For accessibility, try to make sure focus is on the table when this view gets focus.
                             // (Note: The table seems to be then passing on focus to the cursor created on it.)
          //Resetting the cursor's ui. Cursor was behaving strangely when result table was
          //receiving focus from another view. It was showing the contents of the last selected cell
          //in the wrong row (Refer defect 56901).
          this.cursor.setVisible(false);
          this.cursor.setVisible (true);
      }
    }
  }
  
  /**
   * Highlight the row in AQL Result table which matches the given document name in its 'Input Document' column and the
   * given annotation text in any of its cells. If AQL Result view has not been properly initialized, no action will be
   * taken.
   * 
   * @param docName Document name
   * @param annotationText Annotation text
   */
  public void setHighLightedRow(String docName, String annotationText) {
    this.annotationText = annotationText;
    this.docName = docName;
    
    if (this.tableViewer != null) {
      Table table = this.tableViewer.getTable ();
      TableItem selectedItem = getTableItem(table,docName, annotationText);
      
      if (table != null && selectedItem != null) {
        table.setSelection (selectedItem);
        table.showSelection();    
      }
    }   
  }
  
  /**
   * Get TableItem in the given table that matches the given doc name in its 'Input Document' column and has the given
   * annotation text in any of its cells.
   * 
   * @param table Table instance to look in
   * @param docName Document name to match for
   * @param annotationText Annotation text to match for
   * @return TableItem instance if a match is found, else null.
   */
  private TableItem getTableItem (Table table, String docName, String annotationText)
  {
    TableItem selectedItem = null;
    if (table != null) {
      TableItem item[] = table.getItems ();
      int docIndex = getInputColumnIndexInResultTable (table);
      boolean found = false;
      if (item != null) {
        for (int i = 0; i < item.length; i++) {
          if (found) {
            break;
          }
          String documentName = item[i].getText (docIndex);
          // Iterate thru all the values to check any value is matching with annotation text.
          // last two columns are 'Input Document' and 'Double-click this column to explain the tuple'
          for (int j = 0; j < docIndex; j++) {
            String value = item[i].getText (j);
            if (value != null && documentName != null && documentName.equals (docName) && value.equals (annotationText)) {
              selectedItem = item[i];
              found = true;
              break;
            }
          }
        }
      }
    }
    return selectedItem;
  }
  
  /**
   * Get the index of the column 'Input Document' in Result table. It need not always be the second last column and an
   * aql view can have multiple fields.
   * 
   * @param table Table instance used in AQL Result table view
   * @return index of the 'Input Document' column. -1 if the column is not found or if table is null.
   */
  private int getInputColumnIndexInResultTable (Table table)
  {
    if (table != null) {
      TableColumn column[] = table.getColumns ();
      int docIndex = -1;
      // loops thru the column names to get the index for the column name as input document
      // this index will be used to get the document name TableItem
      for (int i = 0; i < column.length; i++) {
        String columnName = column[i].getText ();
        if (Constants.COLUMN_NAME_INPUT_DOCUMENT.equals (columnName)) {
          docIndex = i;
          break;
        }
      }
      return docIndex;
    }
    else {
      return -1;
    }
  }

  /**
   * Store parameters necessary for executing the provenance AQL.
   * 
   * @param id
   * @param provenanceRunParams
   */
  public static void setRunParamsForId(String id, ProvenanceRunParams provenanceRunParams) {

    paramsMap.put(id, provenanceRunParams);
  }

  public static final ProvenanceRunParams getParamsForId(String id) {
    ProvenanceRunParams params = paramsMap.get(id);
    paramsMap.remove(id);
    return params;
  }

  public static void setTempDirPath(String pTempDirPath) {
    tempDirPath = pTempDirPath;
  }

  public TableViewer getTableViewer() {
    return this.tableViewer;
  }

  public CellMouseListener getCellMouseListener() {
    return this.cml;
  }

  /**
   * Maps the output view name to the actual qualified (unqualified for non-modular code) name of the view.
   * @param outputViewName If declared in the output statement, the alias of the output view, else the actual name.
   * @param actualViewName The actual qualified name of the view.
   */
  public static void mapOutputViewtoActualName (String outputViewName, String actualViewName)
  {
    outputViewActualViewNameMap.put (outputViewName, actualViewName);
  }

}
