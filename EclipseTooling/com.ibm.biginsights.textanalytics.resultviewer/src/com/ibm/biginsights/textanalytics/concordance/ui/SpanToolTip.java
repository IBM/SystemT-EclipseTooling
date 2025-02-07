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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;

/**
 * Tool tip for the annotation explorer.  Use a styled text widget where we can do some formating.
 */
public class SpanToolTip extends DefaultToolTip {

	@SuppressWarnings("unused")


  // Need a handle to the view to get at the model data.
  private final ConcordanceView cv;

  public SpanToolTip(Control control, ConcordanceView cv) {
    super(control);
    this.cv = cv;
  }

  // We only create the tool tip when the point is over an actual table entry.
  @Override
  protected boolean shouldCreateToolTip(Event event) {
    Point point = new Point(event.x, event.y);
    
 // Get the setting from the preference store
	final TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
	final boolean enableTooltip = wprefs.getPrefToggleSpanToolTip ();
	if(enableTooltip == false)
		return false;
    
    return this.cv.getTableViewer().getCell(point) != null;
  }

  @Override
  protected Composite createToolTipContentArea(Event event, Composite parent) {
    TableViewer tv = this.cv.getTableViewer();
    // Obtain the table item at the point
    Point point = new Point(event.x, event.y);
    ViewerCell cell = tv.getCell(point);
    // Get the model entry for the row
    IConcordanceModelEntry entry = (IConcordanceModelEntry) cell.getElement();
    // Create the composite, and the embedded text widget
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout());
    composite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    StyledText text = new StyledText(composite, SWT.MULTI);
    text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
    // Get the output view and the tuple that the span is derived from
    OutputView view = entry.getOutputView();
    OutputViewRow row = entry.getRow();
    // Format the tuple
    String viewName = view.getName();
    StringBuilder sb = new StringBuilder(viewName);
    formatTuple(sb, view, row, entry.getModel());
    //text.setText(sb.toString());
    text.setText(sb.toString().length () > 90? sb.substring (0,90).toString ()+"..." : sb.toString ());
    
    // Highlight the view name with bold style
    StyleRange boldStyleRange = new StyleRange();
    boldStyleRange.start = 0;
    boldStyleRange.length = viewName.length();
    boldStyleRange.fontStyle = SWT.BOLD;
    text.setStyleRange(boldStyleRange);
    // Need to set layout data, otherwise no show.  Don't really understand why...
    GridData gridData = new GridData();
    text.setLayoutData(gridData);
    Dialog.applyDialogFont(composite);
    return composite;
  }

  private void formatTuple(StringBuilder sb, OutputView view, OutputViewRow row,
      SystemTComputationResult model) {
    final String newline = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    final int numFields = view.getFieldNames().length;
    for (int i = 0; i < numFields; i++) {
      sb.append(newline);
      sb.append(view.getFieldNames()[i]);
      sb.append(": "); //$NON-NLS-1$
      sb.append(FieldValue.toString(row.fieldValues[i], model));
    }
  }

}
