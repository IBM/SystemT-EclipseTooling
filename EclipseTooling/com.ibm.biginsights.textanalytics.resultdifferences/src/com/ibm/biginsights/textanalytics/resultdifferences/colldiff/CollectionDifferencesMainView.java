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

package com.ibm.biginsights.textanalytics.resultdifferences.colldiff;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.Activator;
import com.ibm.biginsights.textanalytics.resultdifferences.Messages;
import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.compute.DifferencesComputer;
import com.ibm.biginsights.textanalytics.resultdifferences.filediff.FileDiffViewOpenListener;
import com.ibm.biginsights.textanalytics.resultdifferences.gsmeasures.CollDiffGSContentProvider;
import com.ibm.biginsights.textanalytics.resultdifferences.gsmeasures.CollDiffGSTree;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.ActionUtilities;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.AnalysisResultExplorer;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.ForwardBackwardSelection;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.IResultsView;
import com.ibm.biginsights.textanalytics.resultdifferences.ui.SectionViewPart;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * 
 *  Rueck View which shows the annotation differences between two
 *         analysis results.
 *  
 */
public class CollectionDifferencesMainView extends ViewPart implements SelectionListener,
		IResultsView {



	public static final String ID = "com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView"; //$NON-NLS-1$

	protected  CTabItem tabItem3 =null; 
	protected transient CollDiffByDocumentTree fCollDiffByDocumentTree;

    protected CollDiffByDocumentContentProvider fCollDiffByDocumentContentProvider;  
    
    private boolean isComparedWithGoldStandard = false;


	public boolean isComparedWithGoldStandard() {
		return isComparedWithGoldStandard;
	}

	public void setComparedWithGoldStandard(boolean isComparedWithGoldStandard) {
		this.isComparedWithGoldStandard = isComparedWithGoldStandard;
	}

	protected CollDiffByTypeTree fCollDiffByTypeTree;
	
	protected CollDiffByTypeContentProvider fCollDiffByTypeContentProvider;
	protected CollDiffGSTree fCollDiffGSTree;
	
	protected CollDiffGSContentProvider fCollDiffGSContentProvider;
    
	protected transient AnalysisResultExplorer explorer;

	protected FormToolkit fFormToolkit;

	protected Form fAnalysisResultsForm;

	protected transient Form fMessageAreaForm;

	protected Display display;

	protected CTabFolder tabFolder;

	protected transient PageBook fPageBook;

	protected ForwardBackwardSelection forwardBackward;
	
	private Combo rightCombo;

	private Combo leftCombo;

	private Composite headerComposite;

	private Label analysisRunLabel;

	private Label compareLabel;

	
	
	// hashmap which is needed to save combobox labels and
	// timestamps/foldernames
	// the correct timestamp can not be recoverd out of the label
	// because the label does not include milliseconds, only seconds
	private HashMap<String, String> rightComboLabelsAndTimestamps = new HashMap<String, String>();

	private HashMap<String, String> leftComboLabelsAndTimestamps = new HashMap<String, String>();

	public void dispose() {
		fFormToolkit.dispose();
		super.dispose();
	}

	public void createPartControl(Composite parent) {
//		long time1 = System.currentTimeMillis();
		display = parent.getDisplay();
		// The block below is for the separate page called "Analysis Differences"
		{
			fFormToolkit = new FormToolkit(parent.getDisplay());
	
			fPageBook = new PageBook(parent, SWT.NULL);
			fFormToolkit.adapt(fPageBook);
	
			fMessageAreaForm = fFormToolkit.createForm(fPageBook);
			fMessageAreaForm.setText(Messages.getString("CollDiff_PageTitle")); //Analysis differences //$NON-NLS-1$
			fMessageAreaForm.getBody().setLayout(new TableWrapLayout());
	
			FormText messageAreaFormText = fFormToolkit.createFormText(
					fMessageAreaForm.getBody(), false);
			messageAreaFormText.setParagraphsSeparated(true);
			final StringBuffer messageBuffer = new StringBuffer("<form><p>"); //$NON-NLS-1$
			messageBuffer.append(Messages.getString("CollDiff_AnalysisEngineInfo")); //In text analysis projects, an analysis engine creates annotations on a set of documents. //$NON-NLS-1$
			messageBuffer.append(" "); //$NON-NLS-1$
			messageBuffer.append(Messages.getString("CollDiff_AnnotationInfo"));//These annotations are called analysis results. In this view, you can compare different analysis results. //$NON-NLS-1$
			messageBuffer.append("</p><p>"); //$NON-NLS-1$
			messageBuffer.append(Messages.getString("CollDiff_SelectResultInfo"));//Right click on an analysis result in the project explorer and select another analysis result by "compare with". //$NON-NLS-1$
			messageBuffer.append("</p></form>"); //$NON-NLS-1$
	
			messageAreaFormText.setText(messageBuffer.toString(), true, false);
			fPageBook.showPage(fMessageAreaForm);
	
			fAnalysisResultsForm = fFormToolkit.createForm(fPageBook);
			fAnalysisResultsForm.getBody().setLayout(new GridLayout());
			fAnalysisResultsForm.setText(""); //$NON-NLS-1$
		}
		// --------------------------------------------------------------------

		createHeader();

		// --------------------------------------------------------------------
		
		SashForm sashForm = new SashForm(fAnalysisResultsForm.getBody(),
				SWT.HORIZONTAL);
		
		fFormToolkit.adapt(sashForm);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
//		long time2 = System.currentTimeMillis();        
		explorer = new AnalysisResultExplorer(fFormToolkit, sashForm,
				new FileDiffViewOpenListener(this), this);
//		long time3 = System.currentTimeMillis();        
		
		//createForwardBackward(fAnalysisResultsForm.getBody());

		final SectionViewPart sectionViewPart = new SectionViewPart(
				fFormToolkit, ExpandableComposite.TITLE_BAR
						| Section.DESCRIPTION);
		// final Section detailsSection = toolkit.createSection(
		// sashForm, Section.TITLE_BAR | Section.DESCRIPTION);
		sectionViewPart.createPartControl(sashForm);
		
		
		final Section detailsSection = sectionViewPart.getSection();
		detailsSection.setText(Messages.getString("CollDiff_AnnotationDetails")); //Annotation difference details - this is the second half of hte page //$NON-NLS-1$
		// detailsSection
		// .setDescription("Shows the number of annotations for each document.
		// Expand a document to see annotation type details");
		detailsSection
				.setDescription(Messages.getString("CollDiff_AnnotationDetailsDescription")); //Shows annotation difference details for selected documents. //$NON-NLS-1$
		detailsSection.setLayoutData(new GridData(GridData.FILL_BOTH));

		tabFolder = new CTabFolder(detailsSection, SWT.BORDER);		
		tabFolder.setSimple(false);
		fFormToolkit.adapt(tabFolder);

		final CTabItem tabItem_1 = new CTabItem(tabFolder, SWT.NONE);
		tabItem_1.setText(Messages.getString("CollDiff_ByDocument")); //$NON-NLS-1$
		fCollDiffByDocumentTree = new CollDiffByDocumentTree(fFormToolkit,
				tabFolder, explorer, new FileDiffViewOpenListener(this));
		fCollDiffByDocumentTree.getTreeViewer().getTree().setLayoutData(
				new GridData(GridData.FILL_BOTH));

		fCollDiffByDocumentContentProvider= (CollDiffByDocumentContentProvider)
                    fCollDiffByDocumentTree.getTreeViewer().getContentProvider();

		tabItem_1.setControl(fCollDiffByDocumentTree.getTreeViewer()
				.getControl());

		final CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
		tabItem.setText(Messages.getString("CollDiff_ByType")); //$NON-NLS-1$

		// byTypeTreeContainer
		// .setDescription("Shows the number of annotations for a certain type.
		// Only documents selected in the Results by document view are shown.");
        
		fCollDiffByTypeTree = new CollDiffByTypeTree(fFormToolkit, tabFolder,
				explorer, new FileDiffViewOpenListener(this));
        fCollDiffByTypeContentProvider=(CollDiffByTypeContentProvider)fCollDiffByTypeTree.getTreeViewer().getContentProvider(); 
		
        detailsSection.setClient(tabFolder);
		tabItem.setControl(fCollDiffByTypeTree.getTreeViewer().getControl());
		sashForm.setWeights(new int[] { 33, 67 });
		tabFolder.showItem(tabItem_1);
		tabFolder.setSelection(tabItem_1);
		addToolbar(detailsSection);
		
		// create the forward backward button and all teh page info
		forwardBackward= new ForwardBackwardSelection(explorer, fCollDiffByDocumentContentProvider, fCollDiffByTypeContentProvider);
		forwardBackward.createForwardBackward(fFormToolkit, fAnalysisResultsForm.getBody());
		// the changes in the selection influnece the number of documents in the list on the right site and
		// therefore document count.
		explorer.addSelectionChangedListener(forwardBackward);
		
//		long time4 = System.currentTimeMillis();        
//		log.log(ILogger.ERROR,"\ncreate view: " + (time2 - time1) + "  create explorer "
//				+ (time3 - time2) + "  rest " + (time4 - time3)+  " all "+(time4-time1) +"\n");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.collections_differences_view");//$NON-NLS-1$
	}

	public void setFocus() {
		if (fAnalysisResultsForm != null && !fAnalysisResultsForm.isDisposed()) {
			fAnalysisResultsForm.setFocus();
		}
	}

	
	

	private void addToolbar(Section section) {
		// Create ToolBar
		Composite sectionToolbarComposite = fFormToolkit.createComposite(
				section, SWT.NULL);
		// sectionToolbarComposite.setBackground(section
		// .getTitleBarGradientBackground());
		sectionToolbarComposite.setLayout(new RowLayout(SWT.HORIZONTAL));

		ToolBarManager toolBarManager = new ToolBarManager();
		ToolBar toolBar = toolBarManager.createControl(sectionToolbarComposite);

		toolBar.setBackground(section.getTitleBarGradientBackground());
		toolBar.setForeground(section.getTitleBarGradientBackground());
		
		class HideElementsWithoutChangeAction extends Action {

			public HideElementsWithoutChangeAction() {
				super(Messages.getString("CollDiff_HideTypesFilter_ToolTip"), AS_CHECK_BOX); //$NON-NLS-1$
				this.setImageDescriptor(Activator.getImageDescriptor("show_annotation.gif")); //$NON-NLS-1$
				this
						.setToolTipText(Messages.getString("CollDiff_HideTypesFilter_ToolTip")); //$NON-NLS-1$
				this.setChecked(false);
				this.setActionDefinitionId("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.HideTypesFilter");
				fCollDiffByDocumentTree.setHideElementsWithoutChanges(false);
				fCollDiffByTypeTree.setHideElementsWithoutChanges(false);
			}

			// ("hide Elements without annotations", Action.AS_RADIO_BUTTON) {
			public void run() {
				if (this.isChecked()) {

					this.setImageDescriptor(Activator.getImageDescriptor("hide_annotation.gif")); //$NON-NLS-1$
					this
							.setToolTipText(Messages.getString("CollDiff_ShowTypesFilter_ToolTip")); //$NON-NLS-1$
					fCollDiffByDocumentTree.setHideElementsWithoutChanges(true);
					fCollDiffByTypeTree.setHideElementsWithoutChanges(true);					
				} else {
					this.setImageDescriptor(Activator.getImageDescriptor("show_annotation.gif")); //$NON-NLS-1$
					this
							.setToolTipText(Messages.getString("CollDiff_HideTypesFilter_ToolTip"));					 //$NON-NLS-1$
					fCollDiffByDocumentTree.setHideElementsWithoutChanges(false);
					fCollDiffByTypeTree.setHideElementsWithoutChanges(false);
				}
			}
		}		
        
		IAction expandAll = ActionUtilities.createTreeExpansion(this);
		IAction CollapseAll = ActionUtilities.createTreeCollapse(this);
		final IAction hideTypesFilter = new HideElementsWithoutChangeAction();

		//toolBarManager.add(new HideElementsWithoutChangeAction());
		toolBarManager.add(hideTypesFilter);
		toolBarManager.add(expandAll);
        toolBarManager.add(CollapseAll);
        getViewSite().getActionBars().setGlobalActionHandler("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.ExpandAll", expandAll);
        getViewSite().getActionBars().setGlobalActionHandler("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.CollapseAll", CollapseAll);
        getViewSite().getActionBars().setGlobalActionHandler("com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView.HideTypesFilter", hideTypesFilter);
        getViewSite().getActionBars().updateActionBars();
		
		toolBarManager.update(true);
		section.setTextClient(sectionToolbarComposite);
				  
		section.getDescriptionControl ().getAccessible  ().addAccessibleListener (new AccessibleAdapter() {
      public void getName (AccessibleEvent e) {
        final String typesFilter;
        if (hideTypesFilter.getToolTipText ().equals (Messages.getString("CollDiff_ShowTypesFilter_ToolTip"))) {
          typesFilter = Messages.getString("CollDiff_HideTypesFilter_ShortCut1");
        } else {
          typesFilter = Messages.getString("CollDiff_HideTypesFilter_ShortCut2");
        }
        e.result = Messages.getString("CollDiff_AnnotationDetails")+" "+typesFilter+" "+Messages.getString("ActionUtilities_ExpandAll_ShortCut")+" "+Messages.getString("ActionUtilities_CollapseAll_ShortCut");
      }
    });
	}

	/**
	 * 
	 * @param oldAnalysisResultFolder
	 *            the first analysis result folder for the diff
	 * @param newAnalysisResultFolder
	 *            the second analysis result folder for the diff
	 * @param withEmptySelection
	 *            true, if selection in explorer of colldiff view should be
	 * empty, false otherwise */
	public void init(final CollDiffModel collDiffModel) {

		if (display == null || display.isDisposed()) {
			return;
		}

//		// set the filter as needed
//		fCollDiffByDocumentTree.setHideElementsWithoutChanges(filterItem
//				.getSelection());
//		fCollDiffByTypeTree.setHideElementsWithoutChanges(filterItem
//				.getSelection());

		/**
		 * empty the selection, this is always necessary because if you change
		 * the selected results it is not sure if the same files are existent
		 */
		explorer.setSelection(TreeSelection.EMPTY);
		explorer.fireSelectionChanged(new SelectionChangedEvent(explorer,
				TreeSelection.EMPTY));

		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
					new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) {
							display.asyncExec(new Runnable() {
								public void run() {
									explorer.getTreeViewer().setInput(ResultDifferencesUtil.getSuperSetFilesInBothFolders(collDiffModel.getLeftFolder(),collDiffModel.getRightFolder()));
									explorer.getTreeViewer().collapseAll();
									explorer.getTreeViewer().expandToLevel(2);
								}
							});
							final DifferencesComputer analysisStoreReader = DifferencesComputer.getInstance(
									collDiffModel.getRightFolder(),
								collDiffModel.getLeftFolder(),true);
							fCollDiffByDocumentTree
									.setAnalysisStoreReader(analysisStoreReader);
							display.asyncExec(new Runnable() {
								public void run() {
									// set old folder as input
									fCollDiffByDocumentTree
											.getTreeViewer()
											.setInput(ResultDifferencesUtil.getSuperSetFilesInBothFolders(collDiffModel.getLeftFolder(),collDiffModel.getRightFolder()));
								}
							});

							fCollDiffByTypeTree
									.setAnalysisStoreReader(analysisStoreReader);
							display.asyncExec(new Runnable() {
								public void run() {
									// set types as input

                 String[] diffTypes = analysisStoreReader
                      .getDiffTypes();
                 if ((diffTypes == null) || (diffTypes.length==0))
                 {
                   LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowWarning(Messages.getString("CompareWithLabelledCollectionHandler_NoCommonAnnotationsFoundToCompare"));
                 }
                 else
                 {
									fCollDiffByTypeTree.getTreeViewer()
											.setInput(diffTypes);
                 }
								}
							});
								display.asyncExec(new Runnable() {
										public void run() {
									        if (isComparedWithGoldStandard)
									        {
									        	if (tabItem3==null)
									        	{
									        		tabItem3 = new CTabItem(tabFolder, SWT.NONE);
									        	}
									        	if (tabItem3.isDisposed() == false)
									        	{
													tabItem3.setText(Messages.getString("CollDiff_GS_TabName")); //$NON-NLS-1$
													
											        fCollDiffGSTree = new CollDiffGSTree(fFormToolkit, tabFolder,
															explorer);
													fCollDiffGSTree.setAnalysisStoreReader(analysisStoreReader);
											        fCollDiffGSTree.getTreeViewer().setInput(analysisStoreReader.getDiffTypes());
											        fCollDiffGSContentProvider=(CollDiffGSContentProvider)fCollDiffGSTree.getTreeViewer().getContentProvider(); 
											        tabItem3.setControl(fCollDiffGSTree.getTreeViewer().getControl());
											        fCollDiffGSTree.getTreeViewer().setAutoExpandLevel(2);
									        	}
									        }
									        else
									        {
									        	if (tabItem3 != null)
									        	{
									        		tabItem3.dispose();
									        	}
/*												fCollDiffGSTree.getTreeViewer()
												.setInput(null);
*/									        }
									}
								});
						}
					});

			fillHeader(collDiffModel);

			// show the page
			
			fPageBook.showPage(fAnalysisResultsForm);
			// after a new collection diff was loaded, the selection count has to reset
			// and then the page range label redrawn with the new values.
			explorer.resetSelectionCount();
			forwardBackward.setPageRange(explorer.getSelectionStart(), explorer.getSelectionEnd(), explorer.getSelectionMax());
			
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			//logger.log(ILogger.ERROR,"error in CollDiffView initialization", e);
		} catch (InterruptedException e) {
			e.printStackTrace();
			//logger.log(ILogger.ERROR,"error in CollDiffView initialization", e);
		}
	}

	
	/**
	 * this method creates the header composite
	 * 
	 *  Rueck
	 * @param form
	 *            the form to which the composite will be added
	 */
	protected void createPageControl() {

	}
	
	/**
	 * this method creates the header composite
	 * 
	 *  Rueck
	 * @param form
	 *            the form to which the composite will be added
	 */
	protected void createHeader() {
		// create composite for showing comboboxes of the compared runs
		headerComposite = new Composite(fAnalysisResultsForm.getHead(),
				SWT.NONE);
		GridLayout headerLayout = new GridLayout(5, false);
		headerLayout.marginHeight = 0;
		headerLayout.horizontalSpacing = 5;
		headerComposite.setLayout(headerLayout);
		//headerComposite.setBackground(new Color(null, 255, 255, 255));
		headerComposite.setBackground(fAnalysisResultsForm.getBackground());
		fFormToolkit.adapt(headerComposite);

		fAnalysisResultsForm.setHeadClient(headerComposite);

		analysisRunLabel = new Label(headerComposite, SWT.HORIZONTAL);
		//analysisRunLabel.setBackground(new Color(null, 255, 255, 255));
		analysisRunLabel.setBackground(fAnalysisResultsForm.getBackground());
		GridData analysisRunLabelGridData = new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING
						| GridData.VERTICAL_ALIGN_CENTER);
		analysisRunLabel.setLayoutData(analysisRunLabelGridData);
		analysisRunLabel.setFont(fAnalysisResultsForm.getFont());
		analysisRunLabel.setForeground(fAnalysisResultsForm.getHead()
				.getForeground());

		leftCombo = new Combo(headerComposite, SWT.READ_ONLY);
		GridData comboGridData = new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING
						| GridData.VERTICAL_ALIGN_CENTER);
		leftCombo.setLayoutData(comboGridData);
		leftCombo.addSelectionListener(this);


		//leftCombo.setFont(fAnalysisResultsForm.getFont());
		//leftCombo.setForeground(fAnalysisResultsForm.getHead().getForeground());

		/*
		 * firstRunLabel = new Label(headerComposite, SWT.HORIZONTAL);
		 * firstRunLabel.setBackground(new Color(null, 255, 255, 255)); GridData
		 * firstRunLabelGridData = new GridData(
		 * GridData.HORIZONTAL_ALIGN_BEGINNING |
		 * GridData.VERTICAL_ALIGN_CENTER);
		 * firstRunLabel.setLayoutData(firstRunLabelGridData);
		 * firstRunLabel.setFont(fAnalysisResultsForm.getFont());
		 * firstRunLabel.setForeground(fAnalysisResultsForm.getHead()
		 * .getForeground());
		 */

 		compareLabel = new Label(headerComposite, SWT.HORIZONTAL);
		compareLabel.setText(Messages.getString("CollectionDifferencesMainView_compared_with_reference")); //$NON-NLS-1$
		compareLabel.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING
						| GridData.VERTICAL_ALIGN_CENTER));
		//compareLabel.setBackground(new Color(null, 255, 255, 255));
		compareLabel.setBackground(fAnalysisResultsForm.getBackground());
		compareLabel.setFont(fAnalysisResultsForm.getFont());
		compareLabel.setForeground(fAnalysisResultsForm.getHead()
				.getForeground());
		
		
		rightCombo = new Combo(headerComposite, SWT.READ_ONLY);
		rightCombo.setLayoutData(comboGridData);
		rightCombo.addSelectionListener(this);

				ToolBar toolbar = new ToolBar(headerComposite, SWT.HORIZONTAL);

		toolbar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));

/*		final ToolItem closeItem = new ToolItem(toolbar, SWT.PUSH);
		closeItem.setImage(Activator.getImage("close.gif")); //$NON-NLS-1$
		closeItem.setToolTipText(Messages.getString("CollDiff_ClosePage")); //$NON-NLS-1$
		closeItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fPageBook.showPage(fMessageAreaForm);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			};
		});
*/	}

	/**
	 * 
	 * this method fills the header with the values provided, resizes its
	 * components and the layout
	 * 
	 *  Rueck
	 * 
	 * @param analysisResultFolder1
	 *            the first folder for comparison
	 * @param analysisResultFolder2
	 *            the second folder for comparison
	 * @param runName
	 *            the text for the run name label
	 */
	private void fillHeader(CollDiffModel collDiffModel) {

		// fill combo with all AnalysisResultFolders except the one which was
		// chosen first
		try {
			// empty hashmap and combobox
			leftComboLabelsAndTimestamps.clear();
			leftCombo.removeAll();
			rightComboLabelsAndTimestamps.clear();
			rightCombo.removeAll();

			IResource[] leftResultFolders = collDiffModel.getLeftFolder()
					.getParent().members();
			IResource[] rightResultFolders = collDiffModel.getRightFolder()
			.getParent().members();

			
			String leftFolderLabel = collDiffModel.getLeftFolder().getName();
			String rightFolderLabel = collDiffModel.getRightFolder().getName();
			

			// fill the result comboboxes
			fillComboBox(leftCombo, leftComboLabelsAndTimestamps,
					leftFolderLabel, rightFolderLabel, leftResultFolders);
			fillComboBox(rightCombo, rightComboLabelsAndTimestamps,
					rightFolderLabel, leftFolderLabel, rightResultFolders);

			// set the label of the run name
			String runName = collDiffModel.getLeftFolder()
					.getParent().getName();
			analysisRunLabel.setText(runName + "    "); //$NON-NLS-1$
			analysisRunLabel.pack();
			//rightCombo.pack();
			//leftCombo.pack();
			headerComposite.layout(true);
			headerComposite.getParent().layout();
			headerComposite.getParent().getParent().layout();

		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	private void fillComboBox(Combo combo,
			HashMap<String, String> labelsAndTimestamps, String selectedLabel,
			String excludeLabel, IResource[] resultFolders) {

		// iterate over folders and find all folders which should be in the
		// combobox
		for (int i = 0; i < resultFolders.length; i++) {
			if (resultFolders[i] instanceof IFolder){

				// create temporary analysis result folder to get the label
				IFolder tempResultFolder =(IFolder) resultFolders[i];
				String newLabel = tempResultFolder.getName();

				// add folder to combobox and label+foldername=timestamp to
				// hashmap
				if (!newLabel.equals(excludeLabel)  && (newLabel.startsWith(".") == false)) { // This "." check is to prevent .temp folder that is stored under the goldstandard folder  //$NON-NLS-1$
					combo.add(newLabel);
					labelsAndTimestamps.put(newLabel, tempResultFolder.getName());
				}

				// select the right folder
				if (tempResultFolder.getName().equals(
						selectedLabel)) {
					combo.select(combo.indexOf(newLabel));
				}
			}
		}

	}

	public void widgetDefaultSelected(SelectionEvent e) {
		// nothing to do
	}

	// react to selections the user makes in the comboboxes
	// which contain analysis results to choose
	
	private int rightComboCurrentSelection = -1;
	private int leftComboCurrentSelection = -1;
	public void widgetSelected(SelectionEvent e) {

		if (e.getSource().equals(rightCombo) || e.getSource().equals(leftCombo)) {

			// get the timestamp of the right folder
			String leftLabel = leftCombo.getItem(leftCombo.getSelectionIndex());
			String leftTimestamp = leftComboLabelsAndTimestamps.get(leftLabel);

			// get the timestamp of the right folder
			String rightLabel = rightCombo.getItem(rightCombo
					.getSelectionIndex());
			String rightTimestamp = rightComboLabelsAndTimestamps
					.get(rightLabel);

			// create temporary analysis results to set in model
			CollDiffModel oldCollDiffModel = CollDiffModel.getInstance();
			IFolder leftFolder = oldCollDiffModel.getLeftFolder().getParent().getFolder(new Path(leftTimestamp));
			IFolder rightFolder = oldCollDiffModel.getRightFolder().getParent().getFolder(new Path(rightTimestamp));
			if (GoldStandardUtil.isGoldStandardFolder(rightFolder))
			{
				isComparedWithGoldStandard = true;
			}
			else
			{
				isComparedWithGoldStandard = false;
			}
			// reset the model with the selected analysis results
			
	        if (ResultDifferencesUtil.checkFilesInBothFoldersAreAccessible(leftFolder,rightFolder) == false)
	    	{
	        	leftCombo.select(leftComboCurrentSelection);
	        	rightCombo.select(rightComboCurrentSelection);
	        	showErrorDialog();
	    	}
	        else
	        {
				CollDiffModel newCollDiffModel = CollDiffModel.getInstance(
						rightFolder, leftFolder);
	
				// reset the view
//		    	ResultDifferencesUtil.hidePrevCollDiffView();
				init(newCollDiffModel);
	        }
			// Remember these values so that you can reset them if and invalid folder is selected - that doesnt having maching files
			leftComboCurrentSelection = leftCombo.getSelectionIndex(); 
			rightComboCurrentSelection = rightCombo.getSelectionIndex();

		} 
 }
	
	private void showErrorDialog()
	{
		final IStatus status = new Status(IStatus.ERROR,Activator.PLUGIN_ID,Messages.getString("ResultDifferencesViewAction_non_matching_result_folders")); //$NON-NLS-1$
		ErrorDialog.openError(display.getActiveShell(), Messages.getString("ResultDifferencesViewAction_non_matching_result_folders"),	null, status); //$NON-NLS-1$

	}

	public TreeViewer getActiveTreeViewer() {
		switch (tabFolder.getSelectionIndex()) {
		case 0:
			return fCollDiffByDocumentTree.getTreeViewer();
		case 1:
			return fCollDiffByTypeTree.getTreeViewer();
		case 2:
			return fCollDiffGSTree.getTreeViewer();
		default:
			return null;
		}
	}
	
}
