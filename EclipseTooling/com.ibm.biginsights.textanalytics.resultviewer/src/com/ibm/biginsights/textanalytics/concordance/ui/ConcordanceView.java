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
package com.ibm.biginsights.textanalytics.concordance.ui;

import static com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel.StringFilterType.ANNOTATION_TEXT;
import static com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel.StringFilterType.LEFT_CONTEXT;
import static com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel.StringFilterType.RIGHT_CONTEXT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.services.IWorkbenchLocationService;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry;
import com.ibm.biginsights.textanalytics.concordance.model.IStringFilter;
import com.ibm.biginsights.textanalytics.concordance.model.impl.ConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.impl.Entry;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.AnnotationTextFilter;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.FileFilter;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.LeftContextFilter;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.RightContextFilter;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.TextPatternFilter;
import com.ibm.biginsights.textanalytics.concordance.ui.filter.TypeFilter;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.resultviewer.Activator;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.resultviewer.util.PaginationTracker;
import com.ibm.biginsights.textanalytics.resultviewer.util.ResultViewerUtil;
import com.ibm.biginsights.textanalytics.tableview.view.AQLResultView;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * The main control of the concordance view. Takes care of setting up the viewer, controls the
 * model, sets up sorting and filtering.
 */
@SuppressWarnings("restriction")
public class ConcordanceView extends ViewPart {

	@SuppressWarnings("unused")


  public static final String VIEW_ID = "com.ibm.biginsights.textanalytics.concordance.view"; //$NON-NLS-1$
  public static final String TOOLBAR_NEXT_BUTTON_ID = "com.ibm.biginsights.textanalytics.concordance.shownextpage"; //$NON-NLS-1$;
  public static final String TOOLBAR_PREV_BUTTON_ID = "com.ibm.biginsights.textanalytics.concordance.showprevpage"; //$NON-NLS-1$;
  
  // The table viewer that does the actual display
  private TableViewer tableViewer = null;

  // Need to keep the comparator around so we can change the sort order when
  // the user clicks the
  // table headers.
  private ConcordanceViewerComparator comparator = null;

  // Need to keep the filters around so they can be updated when the model
  // changes.
  private TypeFilter typeFilter = null;

  private FileFilter fileFilter = null;

  private AnnotationTextFilter annotTextFilter = null;

  private LeftContextFilter leftContextFilter = null;

  private RightContextFilter rightContextFilter = null;

  private AbstractContributionFactory menuFactory = null;

  private SashForm sashForm;

  private FilterViewer filterViewer;
  
  private boolean isModularProject;
  

  @Override
  public void createPartControl(Composite parent) {
	  PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.annotation_explorer_view");//$NON-NLS-1$
    if (this.tableViewer == null) {
      this.sashForm = new SashForm(parent, SWT.HORIZONTAL);

      // The model holds all the data, including column headers info.
      IConcordanceModel model = new ConcordanceModel();
      // Use the default styles, plus FULL_SELECTION so a double-click
      // anywhere in a row will open
      // the CasViewer.
      final int swtStyle = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER
          | SWT.FULL_SELECTION | SWT.VIRTUAL;
      this.tableViewer = new TableViewer(this.sashForm, swtStyle);
      // Set up the viewers table control.
      final Table table = this.tableViewer.getTable();
      table.setHeaderVisible(true);
      table.setLinesVisible(true);
      table.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
        @Override
        public void getName (AccessibleEvent e)
        {
          /**
           * For task 34247.
           * This is kind of a hacky fix. Since the table used here does not have cells that support accessibility api,
           * there was no way to do this at the row or cell level. We may have had to create our own custom table widget.
           * Instead, this fix takes advantage of the way JAWS reads table rows currently.
           * i.e. table name (which is taken as the value of the 1st column) + names and values of subsequent columns.
           * So for this fix, when information on a row is sought, we provide the name and value of the 1st column.
           */
          if (e.childID > -1) { //if childID is non-negative, information on a row in the table is being sought
            if (table.getItemCount () > 0) {
              e.result = table.getColumn (0).getText () + " : " + table.getItem (e.childID).getText (0); //$NON-NLS-1$
            }
          } else { //if childID is -1, information on the table is being sought; we'll return the view name
            e.result = getPartName () + ":" + getContentDescription (); //For the sake of accessibility, pass on this view's description text too. Ref. task 27743
          }
        }
      });
      String[] columnHeaders = model.getTableColumnTitles();
      int[] columnWidths = model.getTableColumnWidths();
      int[] columnOrientation = model.getTableColumnOrientation();
      for (int i = 0; i < columnHeaders.length; i++) {
        TableColumn tc = new TableColumn(table, columnOrientation[i], i);
        tc.setText(columnHeaders[i]);
        tc.setWidth(columnWidths[i]);
      }
      
      // Add hover help
      new SpanToolTip(table, this);
      // Create double-click listener to start CasViewer.
      createDoubleClickListener();
      // Set up column header listeners for sorting.
      createSorterListeners();
      // Add filters to the view.
      createFilters(model);
      this.tableViewer.setLabelProvider(new ConcordanceViewLabelProvider());
      this.tableViewer.setContentProvider(new ConcordanceViewContentProvider());
      this.tableViewer.setInput(model);
      this.tableViewer.refresh();
      // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService()
      // .addPartListener(new ResultEditorListener());

      filterViewer = new FilterViewer(this.sashForm, SWT.V_SCROLL);
      filterViewer.load(this.getModel().getProject());
      final TextAnalyticsWorkspacePreferences prefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
      final boolean displayFilter = prefs.getPrefShowFilter ();
      if(displayFilter) {
        filterViewer.show ();
      } else {
        filterViewer.hide ();
      }
      filterViewer.refresh();

      addContextMenu();
    }
    
  }

  private void addContextMenu() {
    class ContextAction extends Action {
      IStructuredSelection selection;
      
      public ContextAction(IStructuredSelection selection) {
        this.selection = selection;
        isModularProject = ProjectUtils.isModularProject (((Entry) selection.getFirstElement()).getConcordanceModel ().getProject ());
      }

      public void run() {// Get the selected entry and get the annotation text, span details and document id.
        String viewName = null;
        Entry entry = (Entry) selection.getFirstElement();
        viewName = entry.getOutputView().getName();
        
        String currProject = entry.getConcordanceModel().getProject().getName();
        // If condition is to Handle the action for 'Show Span in Result Table' menu option..
        if((Messages.spanSelectionMessage).equalsIgnoreCase(this.getText())){   
      		  String annotationText = entry.getAnnotationText();//$NON-NLS-1$
              List<Integer> list = entry.getOffsets();
              String value = annotationText + " [" + list.get(0).toString() + "-"
              + list.get(1).toString() + "]";//$NON-NLS-1$
              showTableView(viewName, entry.getModel().getDocumentID(), value);
      	} // Below else If condition is to Handle the action for 'Show view in AQL editor' menu option..
        else if((Messages.labelShowViewInEditor).equalsIgnoreCase(this.getText())){
      		IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
      		
      		try {
      			ArrayList<Parameterization> parameters = new ArrayList<Parameterization>();
      			ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
      			
      			Command openCommand = commandService.getCommand(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID);
      			IParameter viewParam = openCommand.getParameter(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID_PARAM_VIEW_NAME);
      			Parameterization viewParmeter = new Parameterization(viewParam, viewName );
      			parameters.add(viewParmeter);
      			
      			IParameter currProjParam = openCommand.getParameter(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID_PARAM_PROJ_NAME);
      			Parameterization projectParameter = new Parameterization(currProjParam, currProject);
      			parameters.add(projectParameter);
      			
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
        	(IStructuredSelection) tableViewer.getSelection();

        if (!selection.isEmpty()) {
          ContextAction spanSelection = new ContextAction(selection);
          spanSelection.setText(Messages.spanSelectionMessage);
          mgr.add(spanSelection);
          if(isModularProject){
            ContextAction openViewSelection = new ContextAction(selection);
            openViewSelection.setText(Messages.labelShowViewInEditor);
            mgr.add(openViewSelection);
          }
        }
      }
    };

    mgr.addMenuListener(listener);
    mgr.setRemoveAllWhenShown(true);  // This is to dynamically create the new menu on selected entry..
    // Adding context menu to annotation explorer..
    this.tableViewer.getControl().setMenu(mgr.createContextMenu
    		(this.tableViewer.getControl()));

  }

  public FilterViewer getFilterViewer() {
    return filterViewer;
  }

  public void toggleFilterViewer() {
    if (filterViewer != null)
      filterViewer.toggle();
  }

  @Override
  public void dispose() {
    super.dispose();
    // Clean up menu
    if (this.menuFactory != null) {
      // Check that we still have access to the workbench. If not, we
      // don't need to clean up either
      // because the workbench is already shutting down.
      IMenuService menuService = (IMenuService) PlatformUI.getWorkbench().getService(
          IMenuService.class);
      if (menuService != null) {
        menuService.removeContributionFactory(this.menuFactory);
      }

    }
  }

  // Set up the comparator, and the sorting listeners.
  private final void createSorterListeners() {
    this.comparator = new ConcordanceViewerComparator();
    this.tableViewer.setComparator(this.comparator);
    for (int i = 0; i < IConcordanceModel.NUMBER_OF_COLUMNS; i++) {
      createColumnSelectionListener(this.tableViewer.getTable().getColumn(i), i);
    }
  }

  private final void createColumnSelectionListener(TableColumn column, final int sortOrder) {
    column.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        ConcordanceView.this.comparator.setSortOrder(sortOrder);
        refresh();
      }
    });
  }

  // Add a double-click listener to the table that will open the input
  // document text in an editor
  private void createDoubleClickListener() {
    this.tableViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        ISelection sel = event.getSelection();
        if (sel instanceof IStructuredSelection) {
          Object selElement = ((IStructuredSelection) sel).getFirstElement();
          if (selElement instanceof IConcordanceModelEntry) {
            IConcordanceModelEntry entry = (IConcordanceModelEntry) selElement;
            ConcordanceView.this.showEditorForEntry(entry);
          }
        }
      }
    });
  }

  public void showEditorForEntry(IConcordanceModelEntry entry) {
    // Get the text from the selection
    final String text = entry.getBaseText ();
    // The name of the editor is combination of input document name and the
    // name of the text
    // source this span annotates
    String docSchemaName = entry.getDocSchemaName ();
    if (docSchemaName == null || docSchemaName.length () == 0) {
      docSchemaName = "Anonymous [" + entry.getTextSourceID () + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    try {
      IFile textFile = ResultViewerUtil.writeTempFile (text, Integer.toString (entry.getTextSourceID ()),
        entry.getConcordanceModel ().getTempDirPath ());
      if (textFile == null) { return; }
      String name = entry.getDocId () + " - " + docSchemaName; //$NON-NLS-1$
      EditorInput input = new EditorInput (textFile, entry.getTextSourceID (), name, entry.getModel (),
        entry.getAnnotationType ());
      input.setCurrentProjectReference (entry.getConcordanceModel ().getProject ().getName ());
      // In the following code we are finding the editor and closing it
      // and opening it again.
      // Originally it was not required because open would take care of
      // activating.
      // But now this is required to cater to new requirement 17831
      // -1.3fp1. Otherwise the selection
      // from ConcordanceView would be lost.
      IEditorPart editor = (IEditorPart) PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().findEditor (
        input);
      PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().closeEditor (editor, true);
      ITextEditor txtEditor = (ITextEditor) PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().openEditor (
        input, Activator.RESULT_EDITOR_ID, true);
      List<Integer> offsets = entry.getOffsets ();
      final int start = offsets.get (0);
      final int end = offsets.get (1);
      final int len = end - start;
      txtEditor.selectAndReveal (start, len);
    }
    catch (PartInitException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.unableToOpenEditorMessage, e);
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.unableToOpenEditorMessage, e);
    }
    catch (IOException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.unableToOpenEditorMessage, e);
    }
  }

  private static final void closeAllResultEditors() {
    Display.getDefault().asyncExec(new Runnable() {

      @Override
      public void run() {

        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench != null) {
          IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
          if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
              IEditorReference[] editors = page.getEditorReferences();
              Set<IEditorReference> resultEditors = new HashSet<IEditorReference>();
              for (IEditorReference editor : editors) {
                if (Activator.RESULT_EDITOR_ID.equals(editor.getId())) {
                  resultEditors.add(editor);
                }
              }
              IEditorReference[] editorsToClose = resultEditors
                  .toArray(new IEditorReference[resultEditors.size()]);
              page.closeEditors(editorsToClose, false);
            }
          }
        }
      }
    });
  }

  // Initial filter setup.
  private final void createFilters(IConcordanceModel model) {
    this.typeFilter = new TypeFilter(model.getTypes());
    this.fileFilter = new FileFilter(model.getFiles());
    this.annotTextFilter = new AnnotationTextFilter(null);
    this.leftContextFilter = new LeftContextFilter(null);
    this.rightContextFilter = new RightContextFilter(null);
    this.tableViewer.setFilters(new ViewerFilter[] { this.typeFilter, this.fileFilter,
        this.annotTextFilter, this.leftContextFilter, this.rightContextFilter });
  }

  // Update the filters after the model has changed.
  public final void updateFilters() {
    IConcordanceModel model = getModel();
    this.typeFilter.setTypes(model.getTypes());
    this.fileFilter.setFiles(model.getFiles());
    updateTextFilter(this.annotTextFilter, IConcordanceModel.COLUMN_ANNOTATION_TEXT);
    updateTextFilter(this.leftContextFilter, IConcordanceModel.COLUMN_LEFT_CONTEXT);
    updateTextFilter(this.rightContextFilter, IConcordanceModel.COLUMN_RIGHT_CONTEXT);
  }

  // Update a text-based filter (i.e., no checkbox)
  private final void updateTextFilter(TextPatternFilter textFilter, final int columnIndex) {
    IStringFilter filter = getModel().getStringFilter(textFilter.getFilterType());
    textFilter.setPattern(filter.getPattern());
    updateColumnHeader(columnIndex, filter.getString());
  }

  // Update the column headers for the columns with text-based filters
  private final void updateColumnHeader(final int columnIndex, final String filter) {
    TableColumn tc = this.tableViewer.getTable().getColumn(columnIndex);
    final String standardHeader = getModel().getTableColumnTitles()[columnIndex];
    if (filter == null) {
      // If the filter is empty, set the standard header
      tc.setText(standardHeader);
    } else {
      // If there is a non-empty filter, display it next to the column
      // header in braces
      tc.setText(standardHeader + " {" + filter + "}"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  // Reset all filters so no filtering occurs.
  public void clearAllFilters() {

    // clear filters in filterviewer
    this.filterViewer.clearFilters();

    // Clear the filters in the model.
    IConcordanceModel model = getModel();
    model.getTypes().setAllActive();
    model.getFiles().setAllActive();
    model.resetStringFilter(ANNOTATION_TEXT);
    model.resetStringFilter(LEFT_CONTEXT);
    model.resetStringFilter(RIGHT_CONTEXT);
    // Update filters in the UI
    updateFilters();
    // Refresh UI after filter update
    refresh();
  }

  // Refresh the view, called for example from the filter handlers
  public void refresh() {
    this.tableViewer.refresh();
    
    refreshDescription();
  }
  
  /**
   * Refreshes the description panel for the view.
   */
  public void refreshDescription() {
    // We want to be able to say how many rows are actually being displayed
    // (as opposed to, how many
    // are in the model). So we need to get the number of rows from the
    // actual table control,
    // because it has the correct number after filtering.
    final int numberOfDisplayedRows = this.tableViewer.getTable().getItemCount();
    Object[] hparams = new Object[4];
    hparams[0] = numberOfDisplayedRows;
    hparams[1] = this.getModel().size();
    hparams[2] = PaginationTracker.getInstance().getCurrentPage();
    hparams[3] = PaginationTracker.getInstance().getTotalNumberOfPages();
    String header = Messages.getString("ConcordanceView_Header", hparams); //$NON-NLS-1$
    if (this.getModel().size()>0 && numberOfDisplayedRows==0) {
      header = Messages.getString("ConcordanceView_Header_NoSpansInPage",hparams); //$NON-NLS-1$
    }
    this.setContentDescription(header);
    
    //refresh enabled/disabled states of the pagination buttons too.
    this.getViewSite ().getActionBars ().getToolBarManager ().find (TOOLBAR_NEXT_BUTTON_ID).isEnabled (); 
    this.getViewSite ().getActionBars ().getToolBarManager ().find (TOOLBAR_PREV_BUTTON_ID).isEnabled ();
  }

  // Obtain the current model
  public IConcordanceModel getModel() {
    return (IConcordanceModel) this.tableViewer.getInput();
  }

  // Expose setInput() on main control
  public void setInput(Object input) {
	  setInput(input,null);
  }
  
  public void setInput(Object input, String pageChangeRequestOrigin) {
    closeAllResultEditors();
    if (input instanceof IConcordanceModel) {
      final IConcordanceModel model = (IConcordanceModel) input;
      if (this.tableViewer != null) {
        this.tableViewer.setInput(model);
      }
      // Note: we can only update the filters after the model has been
      // set. After we update the
      // filters though, we need to refresh the view again.
      updateFilters();
      // Update views menu
      IMenuService menuService = (IMenuService) PlatformUI.getWorkbench().getService(
          IMenuService.class);
      final String location = "menu:com.ibm.biginsights.textanalytics.concordance.views.pulldown"; //$NON-NLS-1$
      // Remove the previous contribution factory, if it exists
      if (this.menuFactory != null) {
        menuService.removeContributionFactory(this.menuFactory);
      }
      this.menuFactory = new AbstractContributionFactory(location, null) {

        @Override
        public void createContributionItems(IServiceLocator serviceLocator,
            IContributionRoot additions) {

          // This serviceLocator's service is needed to build CommandContributionItem
          if (serviceLocator == null ||
              serviceLocator.getService(IWorkbenchLocationService.class) == null)
            return;

          String[] outputViews = model.getOutputViewNames();
          Arrays.sort(outputViews);
          for (String outputView : outputViews) {
            Map<String, String> paramMap = new HashMap<String, String>();
            paramMap.put("com.ibm.biginsights.textanalytics.resultviewer.views.view", outputView); //$NON-NLS-1$
            CommandContributionItemParameter param = new CommandContributionItemParameter(
                serviceLocator,
                "test.id.1", //$NON-NLS-1$
                "com.ibm.biginsights.textanalytics.concordance.commands.views", paramMap, null, //$NON-NLS-1$
                null, null, outputView, null, Messages.showOutputViewMessage1 + outputView
                    + Messages.showOutputViewMessage2, CommandContributionItem.STYLE_PUSH, null,
                true);
            if (param != null)
            {
	            CommandContributionItem item = new CommandContributionItem(param);
	            additions.addContributionItem(item, null);
            }
          }
        }
      };
      menuService.addContributionFactory(this.menuFactory);
      updateTableViews(model.getOutputViewNames(),pageChangeRequestOrigin);
      refresh();

      // clear filters in filterviewer
      //this.filterViewer.clearFilters();
      
      /* setInput will assume that if pageChangeRequest is null, its caller
       * was not a show next/prev page handler, and hence will reload saved filters
       * without applying them.
       */
      if (pageChangeRequestOrigin == null) {  
    	  this.filterViewer.load(this.getModel().getProject());
      } else {
    	  this.filterViewer.applyAllSelectedFilters();
      }
    }
  }

  private final void updateTableViews(String[] viewNames, String updateRequestOrigin) {
    // Close old table views and get their names
    String[] prevViews = getAndCloseOpenTableViews();
    // Compute the intersection between old and new views
    Arrays.sort(viewNames);
    Set<String> viewsToOpen = new HashSet<String>();
    for (String viewName : prevViews) {
      if (Arrays.binarySearch(viewNames, viewName) >= 0) {
        viewsToOpen.add(viewName);
      }
    }
    for (String viewName : viewsToOpen) {
    	if (viewName.equals(updateRequestOrigin)) {
    		showTableView(viewName,null,null,true);
    	} else {
    		showTableView(viewName, null, null, false);
    	}
    }
  }

  // Get the names of the currently open table views. Close those views prior
  // to reopening them
  // with updated content.
  private static final String[] getAndCloseOpenTableViews() {
    final Set<String> tableViewNames = new HashSet<String>();
    Display.getDefault().syncExec(new Runnable() {

      @Override
      public void run() {
        final IWorkbenchPage wbPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
            .getActivePage();
        IViewReference[] viewReferences = wbPage.getViewReferences();
        for (IViewReference viewRef : viewReferences) {
          // TODO : Given below is a work around for Eclipse 4.2.2 bug:130483, and corresponding Tooling RTC defect is 50950. 
          // Revisit this when upgrading to higher versions of Eclipse
          //Begin: workaround
          //if (AQLResultView.VIEW_ID.equals(viewRef.getId())) {
          if (viewRef.getId().contains (AQLResultView.VIEW_ID)) {
          // End: workaround
            String secondaryId = viewRef.getSecondaryId();
            if (secondaryId != null) {
              tableViewNames.add(secondaryId);
            }
            wbPage.hideView(viewRef);
          }
        }
      }
    });
    return tableViewNames.toArray(new String[tableViewNames.size()]);
  }

  /**
   * Create new tab for corresponding view.
   * 
   * @param viewName
   * @param docName
   * @param annotationText

   */
  public void showTableView(final String viewName, final String docName, final String value) {
	  showTableView(viewName,docName,value,true);
  }

  /**
   * Create new tab for corresponding view.
   * 
   * @param viewName
   * @param docName
   * @param annotationText
   * @param giveFocus - if set to true, gives focus to specified table view.
   */
  public void showTableView(final String viewName, final String docName, final String value, final boolean giveFocus) {
	    if (viewName == null || viewName.length() == 0) {
	      Display.getDefault().asyncExec(new Runnable() {

	        @Override
	        public void run() {
	          Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	          MessageDialog.openInformation(shell, Messages.selectOutputViewNameMessage,
	              Messages.selectOutputViewNameFromDropdownMessage);
	        }
	      });
	      return;
	    }

	    Display.getDefault().asyncExec(new Runnable() {
	      @Override
	      public void run() {
	        final IWorkbenchPage wbPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
	            .getActivePage();
	        try {

	          IConcordanceModel model = ConcordanceView.this.getModel();
	          AQLResultView.setModelForId(viewName, model.getViewModel(viewName)); 
	          String actualViewName = ((ConcordanceModel)model).getActualViewName (viewName); //this method, not part of IConcordanceModel
	          if (actualViewName == null || actualViewName.isEmpty ()) {
	            actualViewName = viewName;
	          }
	          AQLResultView.setRunParamsForId(viewName, model.getProvenanceParams());
	          AQLResultView.mapOutputViewtoActualName(viewName,actualViewName); //Pass on actual view name to AQLResultView
	          AQLResultView.setTempDirPath(model.getTempDirPath());
	          // This is consumed by the 'show view in editor' context menu in AQLResultView class..
	          AQLResultView.currentProject = model.getProject().getName();
	          // TODO : Given below is a work around for Eclipse 4.2.2 bug: 49783. Revisit this when upgrading to higher versions of Eclipse
	          //Begin: workaround
	          String viewId = viewName != null ? AQLResultView.VIEW_ID+":"+viewName : AQLResultView.VIEW_ID; //$NON-NLS-1$
	          final IViewReference prevView = wbPage.findViewReference(viewId, viewName);
	          //End: workaround
	          if (prevView != null) {
	            // Although the API says "hide", it does in fact
	            // close the view
	            wbPage.hideView(prevView);
	          }

	          IViewPart resultTableView = null;
	          if (giveFocus) {
	        	  resultTableView = wbPage.showView(AQLResultView.VIEW_ID, viewName, IWorkbenchPage.VIEW_ACTIVATE);
	          } else {
	        	  resultTableView = wbPage.showView(AQLResultView.VIEW_ID, viewName, IWorkbenchPage.VIEW_VISIBLE);
	          }
	          if (resultTableView != null && resultTableView instanceof AQLResultView) {
	            ((AQLResultView)resultTableView).setHighLightedRow (docName, value);
	          }

	        } catch (final PartInitException e) {
	          LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(e.getMessage(), e);
	        } catch (final Exception e1) {
	          LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(e1.getMessage(), e1);
	        }
	      }
	    });

	  }

  public void addSelectionListener(ISelectionChangedListener l) {
    this.tableViewer.addSelectionChangedListener(l);
  }

  public void removeSelectionChangedListener(ISelectionChangedListener l) {
    this.tableViewer.removeSelectionChangedListener(l);
  }

  @Override
  public void setFocus() {
    this.tableViewer.getTable ().setFocus (); //For accessibility, when this view gets focus, try to set focus to the table.
  }

  public TableViewer getTableViewer() {
    return this.tableViewer;
  }
}
