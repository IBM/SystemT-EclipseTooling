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
package com.ibm.biginsights.textanalytics.tableview.control;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.biginsights.textanalytics.provenance.run.ProvenanceJob;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.resultviewer.util.ResultViewerUtil;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.tableview.Activator;
import com.ibm.biginsights.textanalytics.tableview.Messages;
import com.ibm.biginsights.textanalytics.tableview.model.IRow;
import com.ibm.biginsights.textanalytics.tableview.model.impl.Row;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public class CellMouseListener extends MouseAdapter implements MouseListener
{



  private final TableCursor cursor;

  private String outputViewName; // Should hold the alias of an output view, if defined, else the qualified view name

  private String actualViewName; // The qualified name of the view being output

  private String projectName;

  private ISelection selection = null;

  private String[] columnHeaders;

  // Parameters from the last Run SystemT launch config executed that are
  // necessary to compute provenance. We store them here until we find a
  // better way to do this.
  private ProvenanceRunParams provenanceRunParams = null;

  // This parameter is passed from the ConcordanceView - It is used to get a reference to teh result folder
  private String tempDirPath;

  public CellMouseListener (TableCursor cursor, String outputViewName, String actualViewName,
    ProvenanceRunParams provenanceRunParams, String tempDirPath, String[] columnHeaders, String projName)
  {
    super ();
    this.cursor = cursor;
    this.outputViewName = outputViewName;
    this.actualViewName = actualViewName;
    this.provenanceRunParams = provenanceRunParams;
    this.tempDirPath = tempDirPath;
    this.columnHeaders = columnHeaders;
    this.projectName = projName;
  }

  public void setSelection (ISelection sel)
  {
    this.selection = sel;
  }

  @Override
  public void mouseDown (MouseEvent e)
  {
    showExplainGraph ();
  }

  public void showExplainGraph ()
  {
    final int col = this.cursor.getColumn ();
    if (this.selection != null && this.selection instanceof IStructuredSelection) {
      IRow row = (IRow) ((IStructuredSelection) this.selection).getFirstElement ();
      if (row.isProvenanceCell (col)) {
        handleProvenanceRequest (row);
      }
      else {
        String colName = columnHeaders[col];
        if (colName.contains ("SPAN")) //$NON-NLS-1$
        {
          showEditorForEntry (row, col);
        }
        else {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowWarning (Messages.getString ("CLICK_ON_SPAN_ONLY")); //$NON-NLS-1$
        }
      }
    }
  }

  public void showEditorForEntry ()
  {
    if (this.selection != null && this.selection instanceof IStructuredSelection) {

      IRow row = (IRow) ((IStructuredSelection) this.selection).getFirstElement ();
      int col = this.cursor.getColumn ();

      // Only show when the selected cell contains a span
      String colName = columnHeaders[col];
      if (colName.contains ("SPAN")) { //$NON-NLS-1$
        showEditorForEntry (row, col);
      }
      else {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowWarning (Messages.getString ("CLICK_ON_SPAN_ONLY")); //$NON-NLS-1$
      }
    }
  }

  public void showEditorForEntry (IRow entry, int col)
  {
    int sourceID = entry.getSourceId (col);
    // Get the text from the selection
    if (sourceID == Constants.SPAN_UNDEFINED_SOURCE_ID) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logWarning (Messages.getString("CellMouseListener.Warning_cannot_display_nullspan")); //$NON-NLS-1$
      return;
    }
    final String text = entry.getSourceText (sourceID);
    try {
      IFile textFile = ResultViewerUtil.writeTempFile (text, Integer.toString (entry.getSourceId (col)), tempDirPath);
      if (textFile == null) { return; }
      String docSchemaName = entry.getDocSchemaName (col);
      if (docSchemaName == null || docSchemaName.length () == 0) {
        docSchemaName = "Anonymous [" + sourceID + "]"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      String name = entry.getInputDocName () + " - " + docSchemaName; //$NON-NLS-1$

      IFolder resultFolder = ResultViewerUtil.getResultFolder (tempDirPath);
      String columnName = removeSpanKeyword (columnHeaders[col]);
      EditorInput input = new EditorInput (textFile, entry.getSourceId (col), name,
        ResultViewerUtil.getResultFromFileName (resultFolder, entry.getInputDocName (), ".strf", docSchemaName), //$NON-NLS-1$
        outputViewName + "." + columnName); //$NON-NLS-1$
      input.setCurrentProjectReference (projectName);
      IEditorPart editor = (IEditorPart) PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().findEditor (
        input);
      PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().closeEditor (editor, true);
      ITextEditor txtEditor = (ITextEditor) PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().openEditor (
        input, "com.ibm.biginsights.textanalytics.resultviewer.editor", true); //$NON-NLS-1$
      String spanToHighlight = entry.getLabelForCell (col);
      int[] offsets = getOffSets (spanToHighlight);
      final int start = offsets[0];
      final int end = offsets[1];
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

  /**
   * Utility method to remove the keyword SPAN so that we can get to the "pure" column name such as from
   * "firstname (SPAN)"
   * 
   * @param attributeName
   * @return
   */

  private String removeSpanKeyword (String attributeName)
  {
    CharSequence c1 = "(SPAN)"; //$NON-NLS-1$
    return (attributeName.replace (c1, "")).trim (); //$NON-NLS-1$
  }

  /**
   * Utility method to get the offsets given the span attribute value for example "India [230-235]
   * 
   * @param attributeValue
   * @return
   */
  private int[] getOffSets (String attributeValue)
  {
    int openbracket = attributeValue.indexOf ("["); //$NON-NLS-1$
    int dash = attributeValue.lastIndexOf ("-");  //$NON-NLS-1$
    int closebracket = attributeValue.indexOf ("]"); //$NON-NLS-1$
    int start = Integer.valueOf (attributeValue.substring (openbracket + 1, dash));
    int end = Integer.valueOf (attributeValue.substring (dash + 1, closebracket));
    return new int[] { start, end };
  }

  /**
   * Compute and display the provenance of the input entry's tuple.
   * 
   * @param row
   */
  private void handleProvenanceRequest (IRow row)
  {
    ProvenanceRunParams rowParams = new ProvenanceRunParams (this.provenanceRunParams,
      ((Row) row).getModel ().getJsonDocumentLocation ());

    // Generate and display the provenance
    String jobName = String.format (Constants.PROVENANCE_JOB_PROGRESS_LABEL_FORMAT, this.outputViewName,
      row.getInputDocName ());
    ProvenanceJob job = new ProvenanceJob (jobName, row.getInputDocName (), row.getInputDocText (),
      getFieldNameValuePairs (row), this.actualViewName, this.projectName, rowParams); // provenance job assumes the
                                                                                       // viewname provided to it is the
                                                                                       // actual qualifed name and not
                                                                                       // an output view alias, for
                                                                                       // modular code.
    job.schedule ();
  }

  private HashMap<String, String> getFieldNameValuePairs (IRow row)
  {
    SystemTComputationResult result = ((Row) row).getModel ();

    HashMap<String, String> fieldNameValuePairs = new HashMap<String, String> ();

    for (String value : result.getTextMap ().values ()) {
      String fieldName = result.getFieldName (value);
      String fullNameField = Constants.FIELD_NAME_PREFIX + fieldName + Constants.FIELD_VALUE_PREFIX;
      if (fieldName == null)
        fieldNameValuePairs.put ("text", value); // Default schema has 'text' and 'label', this is the 'text' field.     //$NON-NLS-1$
      else
        fieldNameValuePairs.put (fieldName, value.substring (fullNameField.length ()));
    }

    return fieldNameValuePairs;
  }
}
