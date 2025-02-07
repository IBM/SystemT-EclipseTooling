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
package com.ibm.biginsights.textanalytics.patterndiscovery.views;

import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ibm.biginsights.textanalytics.patterndiscovery.errors.ErrorMessages;
import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.StyleRanges;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.CommonContext;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.CommonModelProvider;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;

/**
 * defines the Common Signature Table View. This is the view that is diplayed when a user click in a bubble from the
 * main pd view @see PatternDiscoveryView
 * 
 * 
 */
public class CommonSignatureTableView extends ViewPart
{



  public static final String VIEW_ID = "com.ibm.biginsights.textanalytics.commonsignaturetableview";

  private TableViewer viewer;
  private Composite parent;
  private CommonModelProvider model;
  private static String jsequence;
  private static PatternDiscoveryJob job;
  private StyledText commonLabel;
  private Color cyan_color, yellow_color;

  @Override
  public void createPartControl (Composite parent)
  {
    this.parent = parent;
    cyan_color = parent.getDisplay ().getSystemColor (SWT.COLOR_CYAN);
    yellow_color = parent.getDisplay ().getSystemColor (SWT.COLOR_YELLOW);
    createViewer ();
    update ();
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.pattern_context_view");//$NON-NLS-1$
  }

  public static void setVars (String ajsequence, PatternDiscoveryJob ajob)
  {
    jsequence = ajsequence;
    job = ajob;
  }

  public void update (String ajsequence, PatternDiscoveryJob ajob)
  {
    setVars (ajsequence, ajob);
    update ();
  }

  public void createViewer ()
  {
    GridLayout layout = new GridLayout (1, false);
    parent.setLayout (layout);
    parent.setLayoutData (new GridData (SWT.FILL, SWT.FILL, true, true));

    FontData labelFontData = new FontData ("Arial", 10, SWT.BOLD);
    Font labelFont = new Font (parent.getDisplay (), labelFontData);

    Composite labelContainer = new Composite (parent, SWT.NONE);
    labelContainer.setLayout (new GridLayout (2, false));
    labelContainer.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false));

    Label label = new Label (labelContainer, SWT.NONE);
    label.setText ("Common Pattern: ");
    label.setFont (labelFont);

    GridData commonLabelGridData = new GridData ();
    commonLabelGridData.grabExcessHorizontalSpace = true;
    commonLabelGridData.horizontalAlignment = GridData.FILL;

    commonLabel = new StyledText (labelContainer, SWT.NONE);
    commonLabel.setLayoutData (commonLabelGridData);
    commonLabel.setEditable (false);

    final int swtStyle = SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL;

    viewer = new TableViewer (parent, swtStyle);

    createColumns ();

    final Table table = viewer.getTable ();
    table.setHeaderVisible (true);
    table.setLinesVisible (true);

    viewer.setContentProvider (new ArrayContentProvider ());

    getSite ().setSelectionProvider (viewer);

    // Layout the viewer
    GridData gridData = new GridData ();
    gridData.verticalAlignment = GridData.FILL;
    gridData.horizontalSpan = 2;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalAlignment = GridData.FILL;
    viewer.getControl ().setLayoutData (gridData);

    // add listener
    viewer.addDoubleClickListener (new IDoubleClickListener () {
      @Override
      public void doubleClick (DoubleClickEvent event)
      {
        int index = table.getSelectionIndex ();

        String context = model.getContexts ().get (index).getContextString ();
        String jsignature = model.getJSignature ();
        String signature = model.getContexts ().get (index).getSignature ();

        IWorkbenchPage wbPage = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();

        try {

          SemanticSignatureTableView prevView = (SemanticSignatureTableView) wbPage.findView (SemanticSignatureTableView.VIEW_ID);

          if (prevView != null) {
            prevView.update (jsignature, signature, context, job);
          }
          else {
            SemanticSignatureTableView.setVars (jsignature, signature, context, job);
          }
          wbPage.showView (SemanticSignatureTableView.VIEW_ID);
        }
        catch (PartInitException ex) {
          ErrorMessages.LogErrorMessage (ErrorMessages.PATTERN_DISCOVERY_ERROR_OPENING_SEMANTIC_SIGNATURE_VIEW, ex);
        }
      }

    });

  }

  public void update ()
  {
    if (job != null) {
      model = new CommonModelProvider (jsequence, job);
      model.getTable (jsequence);

      String str = changeStr (jsequence).trim ();
      commonLabel.setText (str);

      StyleRanges styles = new StyleRanges (str);

      styles.addAll (StyleRanges.getStyles (str.toLowerCase (), "<\\w+\\.\\w+>", null, SWT.BOLD));

      for (String sig : getSignatures (str)) {
        styles.addAll (StyleRanges.getStyles (str.toLowerCase (), StyleRanges.backslash (sig.trim ().toLowerCase ()),
          yellow_color, SWT.BOLD));
      }
      commonLabel.setStyleRanges (styles.getStyles ());
      commonLabel.setBackground (parent.getBackground ());
      commonLabel.pack ();

      viewer.setInput (model.getContexts ());
      viewer.refresh ();
    }
  }

  public TableViewer getViewer ()
  {
    return viewer;
  }

  public StringTokenizer tokenize (String line, String seperator, String delimiter)
  {
    StringTokenizer tokenizer = new StringTokenizer (line, seperator, true);
    String newLine = "";

    while (tokenizer.hasMoreTokens ()) {
      newLine += tokenizer.nextToken () + " ";
    }

    tokenizer = new StringTokenizer (newLine, delimiter);

    return tokenizer;
  }

  // replaces n with /n
  public String changeStr (String str)
  {

    StringTokenizer tokenizer = tokenize (str, ";{}", " \r\n");
    String newStr = "";
    String next;

    while (tokenizer.hasMoreTokens ()) {
      next = tokenizer.nextToken ();
      if (next.equals ("n"))
        newStr += "\\n";
      else
        newStr += next + " ";
    }

    if (newStr.charAt (0) == '{' && newStr.charAt (newStr.length () - 1) == '}') {
      newStr = newStr.substring (1, newStr.length () - 1);
    }

    newStr = newStr.trim ();
    if (newStr.charAt (0) == '{' && newStr.charAt (newStr.length () - 1) == '}') {
      newStr = newStr.substring (1, newStr.length () - 1);
    }

    return newStr;

  }

  // This will create the columns for the table
  private void createColumns ()
  {
    String[] titles = { "Context", "Size", "Semantic Pattern" };
    int[] bounds = { 600, 100, 200 };

    // First column is for the first name
    TableViewerColumn col = createTableViewerColumn (titles[0], bounds[0]);
    col.setLabelProvider (new StyledCellLabelProvider () {
      @Override
      public void update (ViewerCell cell)
      {
        CommonContext p = (CommonContext) cell.getElement ();

        String contextString = changeStr (p.getContextString ());
        contextString = contextString.replace ("/n", "\\n");
        cell.setStyleRanges (new StyleRange[] {});
        cell.setText (contextString);

        StyleRanges styles = new StyleRanges (contextString);
        styles.addAll (StyleRanges.getStyles (contextString, "<\\w+\\.\\w+>", null, SWT.BOLD));

        for (String str : getSignatures (changeStr (jsequence))) {
          styles.addAll (StyleRanges.getStyles (contextString.toLowerCase (),
            StyleRanges.backslash (str.trim ().toLowerCase ()), yellow_color, SWT.BOLD));
        }

        for (String sig : getSignatures (changeStr (p.getSignature ()))) {
          styles.addAll (StyleRanges.getStyles (contextString.toLowerCase (),
            StyleRanges.backslash (sig.trim ().toLowerCase ()), cyan_color, SWT.BOLD));
        }

        cell.setStyleRanges (styles.getStyles ());

        super.update (cell);
      }
    });

    TableViewerColumn col2 = createTableViewerColumn (titles[1], bounds[1]);
    col2.setLabelProvider (new ColumnLabelProvider () {
      @Override
      public String getText (Object element)
      {
        CommonContext p = (CommonContext) element;
        return "" + p.getContextCount ();
      }
    });

    TableViewerColumn col3 = createTableViewerColumn (titles[2], bounds[2]);
    col3.setLabelProvider (new StyledCellLabelProvider () {
      @Override
      public void update (ViewerCell cell)
      {
        CommonContext p = (CommonContext) cell.getElement ();
        String signature = p.getSignature ();
        signature = changeStr (signature);

        cell.setStyleRanges (new StyleRange[] {});
        cell.setText (signature);

        StyleRanges styles = new StyleRanges (signature);

        styles.addAll (StyleRanges.getStyles (signature.toLowerCase (), "<\\w+\\.\\w+>", null, SWT.BOLD));

        for (String str : getSignatures (changeStr (jsequence))) {
          styles.addAll (StyleRanges.getStyles (signature, StyleRanges.backslash (str.trim ()), yellow_color, SWT.BOLD));
        }

        for (String sig : getSignatures (changeStr (p.getSignature ()))) {
          styles.addAll (StyleRanges.getStyles (signature, StyleRanges.backslash (sig.trim ()), cyan_color, SWT.BOLD));
        }

        cell.setStyleRanges (styles.getStyles ());

        super.update (cell);
      }
    });
  }

  /**
   * @param title
   * @param bound
   * @return
   */
  private TableViewerColumn createTableViewerColumn (String title, int bound)
  {
    final TableViewerColumn viewerColumn = new TableViewerColumn (viewer, SWT.NONE);
    final TableColumn column = viewerColumn.getColumn ();
    column.setText (title);
    column.setWidth (bound);
    column.setResizable (true);
    column.setMoveable (true);
    return viewerColumn;
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus ()
  {
    viewer.getControl ().setFocus ();
  }

  private String[] getSignatures (String signature)
  {
    String[] ret;
    // ArrayList<String> sigs = new ArrayList<String>();

    ret = signature.split (";");

    return ret;
  }
}
