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
package com.ibm.biginsights.textanalytics.concordance.ui.filter;

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel.StringFilterType;

/**
 * Filter according to a regex pattern.
 */
public class TextPatternFilter extends ViewerFilter {



  private Pattern pattern;

  private final IConcordanceModel.StringFilterType filterType;

  public TextPatternFilter(StringFilterType filterType, Pattern pattern) {
    super();
    this.filterType = filterType;
    this.pattern = pattern;
  }

  public StringFilterType getFilterType() {
    return this.filterType;
  }

  public void setPattern(Pattern newPattern) {
    this.pattern = newPattern;
  }

  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element) {
    if (this.pattern == null) {
      return true;
    }
    if (element instanceof IConcordanceModelEntry) {
      IConcordanceModelEntry entry = (IConcordanceModelEntry) element;
      String s = ""; //$NON-NLS-1$
      switch (this.filterType) {
      case ANNOTATION_TEXT: {
        s = entry.getAnnotationText();
        break;
      }
      case LEFT_CONTEXT: {
        s = entry.getLeftContext();
        break;
      }
      case RIGHT_CONTEXT: {
        s = entry.getRightContext();
        break;
      }
      }
      return this.pattern.matcher(s).find();
    }
    return false;
  }

}
