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

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.ArrayContentProvider;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ibm.biginsights.textanalytics.patterndiscovery.helpers.StyleRanges;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.PatternDiscoveryJob;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.SemanticContext;
import com.ibm.biginsights.textanalytics.patterndiscovery.models.SemanticModelProvider;

/**
 * the semantic signature view is displayed when the user clicks a row from the Common Signature Table View @see
 * CommonSignatureTableView
 * 
 * 
 */
public class SemanticSignatureTableView extends ViewPart
{



  public static final String VIEW_ID = "com.ibm.biginsights.textanalytics.semanticsignaturetableview";

  private TableViewer viewer;

  private static String signature = "";
  private static String context = "";
  private static String jsignature = "";
  private SemanticModelProvider model;
  private Composite parent;
  private StyledText contextLabel, sSignatureLabel, cSignatureLabel;
  private static PatternDiscoveryJob aJob;
  private Color cyan_color, yellow_color;

  @Override
  public void createPartControl (Composite parent)
  {
    this.parent = parent;
    cyan_color = parent.getDisplay ().getSystemColor (SWT.COLOR_CYAN);
    yellow_color = parent.getDisplay ().getSystemColor (SWT.COLOR_YELLOW);
    createViewer ();
    update ();
    PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, "com.ibm.biginsights.textanalytics.tooling.help.expanded_pattern_context");//$NON-NLS-1$
  }

  public static void setVars (String jsig, String sig, String con, PatternDiscoveryJob job)
  {
    jsignature = jsig;
    signature = sig;
    context = con;
    aJob = job;
  }

  private void createViewer ()
  {

    GridLayout layout = new GridLayout (1, true);
    parent.setLayout (layout);
    parent.setLayoutData (new GridData (SWT.FILL, SWT.FILL, true, true));

    Composite labels = new Composite (parent, SWT.NONE);
    labels.setLayout (new GridLayout (2, false));
    labels.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false));

    FontData labelFontData = new FontData ("Arial", 10, SWT.BOLD);
    Font labelFont = new Font (parent.getDisplay (), labelFontData);

    Label _contextLabel = new Label (labels, SWT.NONE);
    _contextLabel.setText ("Context\t");
    _contextLabel.setFont (labelFont);

    contextLabel = new StyledText (labels, SWT.NONE);
    contextLabel.setEditable (false);

    Label _sSignatureLabel = new Label (labels, SWT.NONE);
    _sSignatureLabel.setText ("Semantic Pattern\t");
    _sSignatureLabel.setFont (labelFont);

    sSignatureLabel = new StyledText (labels, SWT.NONE);
    sSignatureLabel.setEditable (false);

    Label _cSignatureLabel = new Label (labels, SWT.NONE);
    _cSignatureLabel.setText ("Common Pattern\t");
    _cSignatureLabel.setFont (labelFont);

    cSignatureLabel = new StyledText (labels, SWT.NONE);
    cSignatureLabel.setEditable (false);

    viewer = new TableViewer (parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

    createColumns (parent, viewer);

    final Table table = viewer.getTable ();
    table.setHeaderVisible (true);
    table.setLinesVisible (true);

    viewer.setContentProvider (new ArrayContentProvider ());

    // Get the content for the viewer, setInput will call getElements in the
    // contentProvider

    // Make the selection available to other views
    getSite ().setSelectionProvider (viewer);
    // Set the sorter for the table

    // Layout the viewer
    GridData gridData = new GridData ();
    gridData.verticalAlignment = GridData.FILL;
    gridData.horizontalSpan = 2;
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;
    gridData.horizontalAlignment = GridData.FILL;
    viewer.getControl ().setLayoutData (gridData);
  }

  public void update ()
  {
    if (aJob != null) {
      model = new SemanticModelProvider (aJob);

      String cont = changeStr (context);
      cont = cont.replace ("/n", "\\n");

      // handle brackets and \n
      String sign = signature;
      sign = changeStr (sign); // change the signature n to \n

      // handle brackets and \n
      String jsign = jsignature;
      jsign = changeStr (jsign); // change the signature n to \n

      // clear all styles
      contextLabel.setStyleRanges (new StyleRange[] {});
      sSignatureLabel.setStyleRange (null);
      cSignatureLabel.setStyleRange (null);

      contextLabel.setText (cont);
      contextLabel.setBackground (parent.getBackground ());

      StyleRanges contextLabelStyles = new StyleRanges (cont);
      // contextLabelStyles.addAll(StyleRanges.getStyles(cont,
      // "<\\w+\\.\\w+>", null, SWT.BOLD));

      contextLabelStyles.addAll (StyleRanges.getStyles (cont.toLowerCase (), "<\\w+\\.\\w+>", null, SWT.BOLD));

      for (String str : getSignatures (jsign)) {
        contextLabelStyles.addAll (StyleRanges.getStyles (cont.toLowerCase (),
          StyleRanges.backslash (str.trim ().toLowerCase ()), yellow_color, SWT.BOLD));
      }

      for (String str : getSignatures (sign)) {
        contextLabelStyles.addAll (StyleRanges.getStyles (cont.toLowerCase (),
          StyleRanges.backslash (str.trim ().toLowerCase ()), cyan_color, SWT.BOLD));
      }
      contextLabel.setStyleRanges (contextLabelStyles.getStyles ());
      contextLabel.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false));
      contextLabel.pack ();

      sSignatureLabel.setText (sign);
      sSignatureLabel.setBackground (parent.getBackground ());

      StyleRanges sSignatureLabelStyles = new StyleRanges (sign);

      sSignatureLabelStyles.addAll (StyleRanges.getStyles (sign.toLowerCase (), "<\\w+\\.\\w+>", null, SWT.BOLD));

      for (String str : getSignatures (jsign)) {
        sSignatureLabelStyles.addAll (StyleRanges.getStyles (sign.toLowerCase (),
          StyleRanges.backslash (str.trim ().toLowerCase ()), yellow_color, SWT.BOLD));
      }
      for (String str : getSignatures (sign)) {
        sSignatureLabelStyles.addAll (StyleRanges.getStyles (sign.toLowerCase (),
          StyleRanges.backslash (str.trim ().toLowerCase ()), cyan_color, SWT.BOLD));
      }

      sSignatureLabel.setStyleRanges (sSignatureLabelStyles.getStyles ());

      sSignatureLabel.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false));
      sSignatureLabel.pack ();

      cSignatureLabel.setText (jsign);
      cSignatureLabel.setBackground (parent.getBackground ());

      StyleRanges cSignatureLabelStyles = new StyleRanges (jsign);

      cSignatureLabelStyles.addAll (StyleRanges.getStyles (jsign.toLowerCase (), "<\\w+\\.\\w+>", null, SWT.BOLD));

      for (String str : getSignatures (jsign)) {
        cSignatureLabelStyles.addAll (StyleRanges.getStyles (jsign.toLowerCase (),
          StyleRanges.backslash (str.trim ().toLowerCase ()), yellow_color, SWT.BOLD));
      }
      cSignatureLabel.setStyleRanges (cSignatureLabelStyles.getStyles ());

      cSignatureLabel.setLayoutData (new GridData (SWT.FILL, SWT.CENTER, true, false));
      cSignatureLabel.pack ();

      viewer.setInput (model.getSnippets (context, jsignature, signature));
      viewer.refresh ();
    }
  }

  public void update (String jsig, String sig, String con, PatternDiscoveryJob job)
  {
    if (job != null) {
      setVars (jsig, sig, con, job);
      update ();
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
  private void createColumns (final Composite parent, final TableViewer viewer)
  {
    String[] titles = { "Snippet" /* , "Phone" */};
    int[] bounds = { 900 /* , 120 */};

    // First column is for the first name
    TableViewerColumn col = createTableViewerColumn (titles[0], bounds[0]);
    col.setLabelProvider (new StyledCellLabelProvider () {
      @Override
      public void update (ViewerCell cell)
      {

        SemanticContext p = (SemanticContext) cell.getElement ();
        String snippet = p.getSnippet ();
        snippet = changeStr (snippet);
        snippet = snippet.replace ("/n", "\\n");

        String jsign = jsignature;
        jsign = changeStr (jsign);

        StyleRanges styles = new StyleRanges (snippet);
        cell.setStyleRanges (new StyleRange[] {});
        cell.setText (snippet);

        styles.addAll (StyleRanges.getStyles (snippet.toLowerCase (), "<\\w+\\.\\w+>", null, SWT.BOLD));

        for (String str : getSignatures (changeStr (jsign))) {
          styles.addAll (StyleRanges.getStyles (snippet.toLowerCase (),
            StyleRanges.backslash (str.trim ().toLowerCase ()), yellow_color, SWT.BOLD));
        }

        for (String sig : getSignatures (changeStr (changeStr (signature)))) {
          styles.addAll (StyleRanges.getStyles (snippet.toLowerCase (),
            StyleRanges.backslash (sig.trim ().toLowerCase ()), cyan_color, SWT.BOLD));
        }

        cell.setStyleRanges (styles.getStyles ());

        super.update (cell);
      }
    });

    // First column is for the first name
    // TableViewerColumn col3 = createTableViewerColumn(titles[1],
    // bounds[1]);
    // col3.setLabelProvider(new ColumnLabelProvider() {
    // @Override
    // public String getText(Object element) {
    // SemanticContext p = (SemanticContext) element;
    // return p.getPhone();
    // }
    // });

  }

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

  public StyleRange[] styleRangeListToArray (ArrayList<StyleRange> list)
  {
    StyleRange[] array = new StyleRange[list.size ()];
    for (int i = 0; i < array.length; i++) {
      array[i] = list.get (i);
    }
    return array;
  }
}
