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

package com.ibm.biginsights.textanalytics.explain.views;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;

import com.ibm.avatar.algebra.util.string.StringUtils;
import com.ibm.biginsights.textanalytics.explain.model.ExplainElement;

public class ExplainModuleLabelProvider extends StyledCellLabelProvider implements ILabelProvider
{

  @SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" +               //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

  @Override
  public void update (ViewerCell cell)
  {
    Object element = cell.getElement ();
    if (element instanceof ExplainElement) {
      // Set icon
      cell.setImage (((ExplainElement)element).getIcon ());

      // Set label
      String text = ((ExplainElement)element).getDisplayedName ();
      int boldLen = text.length ();
      if (text.contains ("("))
        boldLen = text.indexOf ('(') + 1;

      cell.setText (((ExplainElement)element).getDisplayedName ());
      StyleRange styledRange = new StyleRange(0, boldLen, null, null, SWT.BOLD);
      StyleRange[] range = { styledRange };
      cell.setStyleRanges(range);
      super.update(cell);
    }
  }

  @Override
  public String getToolTipText (Object element)
  {
    if (element instanceof ExplainElement) {
      String tooltip = ((ExplainElement)element).getTooltip ();
      if (StringUtils.isNullOrWhiteSpace (tooltip))
        tooltip = ((ExplainElement)element).getDisplayedName ();

      return tooltip;
    }

    return null;
  }

  @Override
  public int getToolTipTimeDisplayed (Object object)
  {
    String tt = getToolTipText (object);
    return 5000 + 50 * tt.length ();
  }

  @Override
  public int getToolTipDisplayDelayTime (Object object)
  {
    return 500;
  }

  @Override
  public int getToolTipStyle (Object object)
  {
    return SWT.SHADOW_OUT;
  }

  @Override
  public Image getImage (Object element)
  {
    return null;    // Image is handled by update().
  }

  @Override
  public String getText (Object element)
  {
    if (element instanceof ExplainElement)
      return ((ExplainElement)element).getDisplayedName ();

    return null;
  }

}
