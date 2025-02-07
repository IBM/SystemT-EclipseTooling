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
package com.ibm.biginsights.textanalytics.resultviewer.model;

/**
 * Collect some utility functions here that we don't want to put directly into the beans.
 */
public class Util {



  /**
   * Get the output view for a view name.
   * 
   * @param model
   *          The model the view belongs to.
   * @param viewName
   *          The name of the view.
   * @return The view, or <code>null</code> if no such view exists.
   */
  public static final OutputView getViewForName(SystemTComputationResult model, String viewName) {
    OutputView view = null;
    if (model != null)
    {
    	if (model.getOutputViews() != null)
    	{
		    for (OutputView v : model.getOutputViews()) {
		      if (v.getName().equals(viewName)) {
		        view = v;
		        break;
		      }
		    }
    	}
    }
    return view;
  }

  /**
   * Get the field index for a field name in a view.
   * 
   * @param view
   *          The view the field belongs to.
   * @param fieldname
   *          The name of the field.
   * @return The index of the field in the view, or <code>-1</code> if no such field exists.
   */
  public static final int getFieldIdForName(OutputView view, String fieldname) {
	  if (view == null)
	  {
		  return -1;
	  }
    String[] names = view.getFieldNames();
    for (int i = 0; i < names.length; i++) {
      if (names[i].equals(fieldname)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Compare two spans according to their customary sort order.
   * 
   * @param span1
   *          Span to compare.
   * @param span2
   *          Span to compare
   * @return <code>-1</code> if span1 is smaller, <code>1</code> if span2 is smaller, and
   *         <code>0</code> if they are equal.
   */
  public static final int spanOrderCompare(SpanVal span1, SpanVal span2) {
    // The smaller the start position, the smaller the span
    if (span1.start < span2.start) {
      return -1;
    }
    if (span1.start > span2.start) {
      return 1;
    }
    // Start positions are equal. The longer the span, the smaller it is.
    if (span1.end > span2.end) {
      return -1;
    }
    if (span1.end < span2.end) {
      return 1;
    }
    // Spans are equal.
    return 0;
  }

}
