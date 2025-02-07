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
package com.ibm.biginsights.textanalytics.resultdifferences.compute;

import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.SPAN;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.biginsights.textanalytics.resultviewer.model.FieldType;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.AQLUtils;

public class RunResultDiff {



  private final Map<String, SystemTComputationResult> c1;

  private final Map<String, SystemTComputationResult> c2;

  public RunResultDiff(Map<String, SystemTComputationResult> collection1,
      Map<String, SystemTComputationResult> collection2) {
    super();
    this.c1 = collection1;
    this.c2 = collection2;
  }

  /**
   * Count the number of non-null spans in an individual result.
   * @param res
   * @return
   */
  public int getAnnotationCount(SystemTComputationResult res) {
    int count = 0;
    for (OutputView view : res.getOutputViews()) {
      FieldType[] types = view.getFieldTypes();
      for (int i = 0; i < types.length; i++) {
        FieldType type = types[i];
        if (type == SPAN) {
          for (OutputViewRow row : view.getRows()) {
            SpanVal span = (SpanVal) row.fieldValues[i];
            if (!span.isNull()) {
              ++count;
            }
          }
        }
      }
    }
    return count;
  }

  /**
   * Count number of annotations in a specific view and field.
   * 
   * @param res
   *          The result object.
   * @param viewName
   *          The name of the view.
   * @param fieldName
   *          The name of the field. Note: the field, if it exists, must be span valued.
   * @return The number of non-null spans in the input view column.
   */
  public int getAnnotationCount(SystemTComputationResult res, String viewName, String fieldName) {
    int count = 0;
    for (OutputView view : res.getOutputViews()) {
      if (view.getName().equals(viewName)) {
        String[] fieldNames = view.getFieldNames();
        for (int i = 0; i < fieldNames.length; i++) {
          if (fieldNames[i].equals(fieldName)) {
            for (OutputViewRow row : view.getRows()) {
              SpanVal span = (SpanVal) row.fieldValues[i];
              if (!span.isNull()) {
                ++count;
              }
            }
          }
        }
      }
    }
    return count;
  }

  /**
   * Get all span valued field names that occur in either of the result sets. This is an expensive
   * operation since the only way to get this information is to iterate over all result sets.
   * 
   * @return All span-valued field names that occur anywhere in either of the result sets.
   */
  public String[] getSpanFieldNames() {
    Set<String> nameSet = getSpanNamesForResultSet(this.c1.values());
    nameSet.addAll(getSpanNamesForResultSet(this.c2.values()));
    return nameSet.toArray(new String[nameSet.size()]);
  }

  /**
   * Get all span valued field names that occur in both result sets. This is an expensive operation
   * since the only way to get this information is to iterate over all result sets.
   * 
   * @return All span-valued field names that occur in both of the result sets.
   */
  public String[] getDiffSpanFieldNames() {
    Set<String> nameSet0 = getSpanNamesForResultSet(this.c1.values());
    Set<String> diffSet = getSpanNamesForResultSet(this.c2.values());
    // Compute the intersection.  Is there no better way to do this?
    Set<String> nameSet = new HashSet<String>();
    for (String s : nameSet0) {
      if (diffSet.contains(s)) {
        nameSet.add(s);
      }
    }
    return nameSet.toArray(new String[nameSet.size()]);
  }

  private static final Set<String> getSpanNamesForResultSet(
      Collection<SystemTComputationResult> results) {
    final Set<String> nameSet = new HashSet<String>();
    OutputView view = null;
    OutputView[] views = null;
    for (SystemTComputationResult res : results) {
        views = res.getOutputViews();
        if (views != null)
        {
          for (int j=0;j<views.length; j++) {
            view = views[j];
            if (view != null)
            {
                FieldType[] types = view.getFieldTypes();
								for (int i = 0; i < types.length; i++) {
			          if (types[i] == SPAN) {
      			      nameSet.add(AQLUtils.getQualifiedFieldName(view.getName(), view.getFieldNames()[i]));
			          }
			        }
			      }
          }
        }
    }
    return nameSet;
  }

}
