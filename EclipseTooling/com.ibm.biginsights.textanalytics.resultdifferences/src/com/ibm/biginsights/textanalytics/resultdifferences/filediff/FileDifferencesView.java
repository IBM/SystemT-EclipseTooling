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

package com.ibm.biginsights.textanalytics.resultdifferences.filediff;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ibm.biginsights.textanalytics.resultdifferences.Activator;
import com.ibm.biginsights.textanalytics.resultdifferences.Messages;
import com.ibm.biginsights.textanalytics.resultdifferences.ResultDifferencesUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.compute.DifferencesComputer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class FileDifferencesView extends ViewPart
{



  public static final String ID = "com.ibm.biginsights.textanalytics.resultdifferences.filediff.FileDifferencesView"; //$NON-NLS-1$
  private static final HashMap<String, FileDifferencesModel> modelMap = new HashMap<String, FileDifferencesModel> ();
  private final HashMap<String, FileDifferencesSideBySideModel> sideBySideModelMap = new HashMap<String, FileDifferencesSideBySideModel> ();
  private Table table; 
  protected Display display;
  private Color green = new Color (display, 178, 255, 102);
  private Color orange = new Color (display, 255, 127, 0);
  private Color blue = new Color (display, 135, 206, 250);
  private Color red = new Color (display, 205, 92, 92);

  public void dispose ()
  {
    super.dispose ();
  }

  public static final void setModelForId (String fileName, FileDifferencesModel model)
  {
    modelMap.put (fileName, model);
  }

  public void createPartControl (Composite parent)
  {
    try {
      display = parent.getDisplay ();
      final String fileNameDotViewNameDotFieldName = getViewSite ().getSecondaryId ();
      if (fileNameDotViewNameDotFieldName == null) {
        Label label = new Label (parent, SWT.BORDER);
        label.setText (Messages.getString ("FileSideBySideDifferencesView_SideBySide")); //$NON-NLS-1$
        return;
      }
      //Removed code that was commented as part of defect 20979

      FileDifferencesModel model = modelMap.get (fileNameDotViewNameDotFieldName);
      
      String[] rightFilePaths = model.getRightFilePaths ();
      String[] leftFilePaths = model.getLeftFilePaths ();
      IFolder leftParentFolder=model.getLeftFolder();
      IFolder rightParentFolder = model.getRightFolder();
      String rightFolderPath = rightParentFolder.getFullPath().toOSString();
      String leftFolderPath = leftParentFolder.getFullPath().toOSString();
      IFile leftFile = null;
      IFile rightFile = null;

      table = createTable (parent, leftFolderPath, rightFolderPath);

      for (int m = 0; m < leftFilePaths.length; m++) {
        leftFile = FileBuffers.getWorkspaceFileAtLocation (new Path (leftFilePaths[m]));
        rightFile = FileBuffers.getWorkspaceFileAtLocation (new Path (rightFilePaths[m]));
        SystemTComputationResult leftModel = ResultDifferencesUtil.getModelFromSTRFFile (leftFile);
        SystemTComputationResult rightModel = ResultDifferencesUtil.getModelFromSTRFFile (rightFile);
        DifferencesComputer computer = DifferencesComputer.getInstance (rightParentFolder
          , leftParentFolder, false);
        ArrayList<SpanVal> newSpansInLeftFile = computer.getNewAnnotationsSpansInActual (rightFile,
          leftFile, model.getType ());
        ArrayList<SpanVal> oldSpansInRightFile = computer.getDeletedAnnotationsSpanInExpected (
          rightFile, leftFile, model.getType ());
        ArrayList<SpanVal> unchangedSpansInLeftFile = computer.getUnchangedAnnotationsSpanInActualFile (
          rightFile, leftFile, model.getType ());
        ArrayList<SpanVal> unchangedSpansInRightFile = computer.getUnchangedAnnotationsSpanInExpectedFile (
          rightFile, leftFile, model.getType ());
        ArrayList<SpanVal> overlappingSpansInRightFile = computer.getOverlappingSpansInExpectedFile (
          rightFile, leftFile, model.getType ());
        ArrayList<SpanVal> overlappingSpansInLeftFile = computer.getOverlappingSpansInActualFile (
          rightFile, leftFile, model.getType ());
        // Table that summarizes the spans to be displayed
        String rightModelID = "";
        String leftModelID ="";
        String fdsbsMapKey = "";
        if (rightModel != null)
        {
        	rightModelID = rightModel.getDocumentID ();
        	fdsbsMapKey = rightModelID;
        }
        if (leftModel != null)
        {
        	leftModelID = leftModel.getDocumentID();
        	fdsbsMapKey = leftModelID;
        }
        sideBySideModelMap.put (fdsbsMapKey, new FileDifferencesSideBySideModel (
          rightFile, leftFile, leftModel, rightModel, newSpansInLeftFile, oldSpansInRightFile,
          unchangedSpansInLeftFile, unchangedSpansInRightFile, overlappingSpansInLeftFile,
          overlappingSpansInRightFile));
        addRowsToTable (table, fdsbsMapKey, rightModel, leftModel,
          newSpansInLeftFile, oldSpansInRightFile, unchangedSpansInLeftFile,
          overlappingSpansInRightFile, overlappingSpansInLeftFile);
      }
      packTable (table);

    }
    catch (Exception e) {
      e.printStackTrace ();

    }
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.file_differences_summary_view");//$NON-NLS-1$
  }

  private Table createTable (final Composite parent, String leftFilePath, String rightFilePath)
  {
    final Table table = new Table (parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
    table.setLinesVisible (true);
    table.setHeaderVisible (true);
    String[] titles = {
      Messages.getString ("FileDifferencesView_Input_File_Path"), leftFilePath, rightFilePath, Messages.getString ("FileDifferencesView_Type") }; //$NON-NLS-1$ //$NON-NLS-2$
    for (int i = 0; i < titles.length; i++) {
      TableColumn column = new TableColumn (table, SWT.NONE);
      column.setText (titles[i]);
    }
    table.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      @Override
      public void getName (AccessibleEvent e)
      {
        /**
         * Fix for task 34318 : Similar fix as done for 34247 and 34248.
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
          e.result = getPartName ();
        }
      }
    });
    // Removed SelectionListener to address the defect 56343. In linux, giving the table focus was triggering a
    // selection event, which in turn was opening another view.
    table.addMouseListener (new MouseAdapter () {
      @Override
      public void mouseDoubleClick (MouseEvent e)
      {
        openFileSideBySideDifferencesView (table); // Open FileSideBySideDifferencesView on detecting a double-click on
                                                   // the table.
      }
    });
    table.addKeyListener (new KeyAdapter () {
      @Override
      public void keyPressed (KeyEvent e)
      {
        if (e.keyCode == SWT.CR) {
          openFileSideBySideDifferencesView (table); // Open FileSideBySideDifferencesView on detecting an 'ENTER'
                                                     // key-press.
        }
      }
    });
    return table;

  }

  /**
   * Opens FileSideBySideDifferencesView for the file selected in the given FileDifferencesView table.
   * @param table Table instance used in FileDifferencesView. Should not be null.
   */
  private void openFileSideBySideDifferencesView (Table table)
  {
    TableItem[] tItems = table.getSelection ();

    String fileName = tItems[0].getText ();
    FileDifferencesSideBySideModel sbsMdl = sideBySideModelMap.get (fileName);
    IWorkbenchPage page = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();

    fileName = StringUtils.normalizeSpecialChars (fileName);
    FileSideBySideDifferencesView.setModelForId (fileName, sbsMdl);
    // TODO : Given below is a work around for Eclipse 4.2.2 bug: 49783. Revisit this when upgrading to higher versions
    // of Eclipse
    // Begin: workaround
    String viewId = fileName != null ? FileSideBySideDifferencesView.ID + ":" + fileName : FileSideBySideDifferencesView.ID; //$NON-NLS-1$
    final IViewReference prevView = page.findViewReference (viewId, fileName);
    // End: workaround
    if (prevView != null) {
      // Although the API says "hide", it does in fact
      // close the view
      page.hideView (prevView);
    }
    try {
      // Show side-by-side file differences view.
      page.showView (FileSideBySideDifferencesView.ID, fileName, IWorkbenchPage.VIEW_ACTIVATE);
    }
    catch (PartInitException e1) {
      // TODO Auto-generated catch block
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (e1.getMessage (), e1);
    }

  }
  
  private void packTable (Table table)
  {
    TableColumn[] tColumns = table.getColumns ();
    for (int i = 0; i < tColumns.length; i++) {
      tColumns[i].pack ();
    }

  }

  private void addRowsToTable (Table table, String fileName, SystemTComputationResult rightModel,
    SystemTComputationResult leftModel, ArrayList<SpanVal> newSpansInLeftFile,
    ArrayList<SpanVal> oldSpansInRightFile, ArrayList<SpanVal> unchangedSpansInBothFiles,
    ArrayList<SpanVal> overlappingSpansInRightFile, ArrayList<SpanVal> overlappingSpansInLeftFile)
  {
    /*
     * GridData data = new GridData(SWT.FILL, SWT.FILL, true, true); data.heightHint = 200; table.setLayoutData(data);
     */
    int count = unchangedSpansInBothFiles.size ();
    SpanVal span = null;
    for (int i = 0; i < count; i++) {
      TableItem item = new TableItem (table, SWT.NONE);
      span = unchangedSpansInBothFiles.get (i);
      item.setText (0, fileName);
      item.setText (1, span.getText (leftModel) + " [" + span.start + "-" + span.end + "]"); // rightModel will also do because it is unchanged //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      item.setText (2, span.getText (leftModel) + " [" + span.start + "-" + span.end + "]"); // rightModel will also do because it is unchanged //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      item.setText (3, Messages.getString ("CollDiff_AbstractResultTree_UnChanged")); //$NON-NLS-1$
      item.setBackground (green);
    }

    count = newSpansInLeftFile.size ();
    for (int i = 0; i < count; i++) {
      TableItem item = new TableItem (table, SWT.NONE);
      span = newSpansInLeftFile.get (i);
      item.setText (0, fileName);
      item.setText (1, span.getText (leftModel) + " [" + span.start + "-" + span.end + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      item.setText (2, ""); //$NON-NLS-1$
      item.setText (3, Messages.getString ("CollDiff_AbstractResultTree_New")); //$NON-NLS-1$
      item.setBackground (orange);
    }

    count = oldSpansInRightFile.size ();
    for (int i = 0; i < count; i++) {
      TableItem item = new TableItem (table, SWT.NONE);
      span = oldSpansInRightFile.get (i);
      item.setText (0, fileName);
      item.setText (1, ""); //$NON-NLS-1$
      item.setText (2, span.getText (rightModel) + " [" + span.start + "-" + span.end + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      item.setText (3, Messages.getString ("CollDiff_AbstractResultTree_Deleted")); //$NON-NLS-1$
      item.setBackground (red);
    }

    count = overlappingSpansInLeftFile.size ();
    for (int i = 0; i < count; i++) {
      TableItem item = new TableItem (table, SWT.NONE);
      span = overlappingSpansInLeftFile.get (i);
      item.setText (0, fileName);
      item.setText (1, span.getText (leftModel) + " [" + span.start + "-" + span.end + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      span = overlappingSpansInRightFile.get (i);
      item.setText (2, span.getText (rightModel) + " [" + span.start + "-" + span.end + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      item.setText (3, Messages.getString ("CollDiff_AbstractResultTree_Changed")); //$NON-NLS-1$
      item.setBackground (blue);
    }

  }

  public void setFocus ()
  {
    if (this.table != null) {
      this.table.setFocus (); // table needs to be given focus for the sake of accessibility
    }
  }

}
