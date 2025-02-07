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

import static com.ibm.biginsights.textanalytics.resultviewer.model.Util.getFieldIdForName;
import static com.ibm.biginsights.textanalytics.resultviewer.model.Util.getViewForName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.RowBySpanColumnComparator;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.model.Util;
import com.ibm.biginsights.textanalytics.resultviewer.util.ResultViewerUtil;

/**
 * Encode the difference between two runs for a specific document, view and column.
 */
public class DocSpanDifference {



  public List<Integer> getExpectedCorrect() {
    return this.expectedCorrect;
  }

  public List<Integer> getActualCorrect() {
    return this.actualCorrect;
  }

  private static final class RowListPair {
    private final List<OutputViewRow> expected;

    private final List<OutputViewRow> actual;

    private RowListPair(List<OutputViewRow> e, List<OutputViewRow> a) {
      super();
      this.expected = e;
      this.actual = a;
    }

    public List<OutputViewRow> getExpected() {
      return this.expected;
    }

    public List<OutputViewRow> getActual() {
      return this.actual;
    }
  }

  private final SystemTComputationResult expectedModel;

  private final SystemTComputationResult actualModel;

  private final String viewName;

  private final String expectedFieldName;

  private final String actualFieldName;

  private final List<Integer> expectedPartial = new ArrayList<Integer>();

  private final List<Integer> expectedMissing = new ArrayList<Integer>();

  private final List<Integer> actualPartial = new ArrayList<Integer>();

  private final List<Integer> actualSpurious = new ArrayList<Integer>();

  private final List<Integer> expectedCorrect = new ArrayList<Integer>();

  private final List<Integer> actualCorrect = new ArrayList<Integer>();

  private DocSpanDifference(SystemTComputationResult expected, SystemTComputationResult actual,
      String viewName, String expectedFName, String actualFName) {
    super();
    this.expectedModel = expected;
    this.actualModel = actual;
    this.viewName = viewName;
    this.expectedFieldName = expectedFName;
    this.actualFieldName = actualFName;
  }

  /**
   * Compute the difference for given pair of results, views and fields.
   * 
   * @param expected
   *          The model of the expected results.
   * @param actual
   *          The model of the actual results.
   * @param viewName
   *          The name of the view (must be the same for both actual and expected).
   * @param expectedFName
   *          The field name in the expected result.
   * @param actualFName
   *          The field name in the actual result. The field names may be different when working
   *          with a gold standard.
   * @return An object that can be questioned about the differences.
   */
  public static DocSpanDifference computeDifference(SystemTComputationResult expected,
      SystemTComputationResult actual, String viewName, String expectedFName, String actualFName) {
	  
	  // case of both being null will never occur
	  if (expected == null)
	  {
		  expected = ResultViewerUtil.stripAllAnnotations(actual);
	  }
	  if (actual == null)
	  {
		  actual = ResultViewerUtil.stripAllAnnotations(expected);
	  }

    DocSpanDifference diff = new DocSpanDifference(expected, actual, viewName, expectedFName,
        actualFName);
    OutputView eView = getViewForName(expected, viewName);
    OutputView aView = getViewForName(actual, viewName);
    OutputViewRow[] eRows = null;
    if (eView == null)
    {
    	eRows = new OutputViewRow[0];
    }
    else
    {
         eRows = copyRowsToPositional(eView.getRows());
         Arrays.sort(eRows, new RowBySpanColumnComparator(getFieldIdForName(eView, expectedFName)));
    }
    OutputViewRow[] aRows = null;
    if (aView == null) 
    {
    	aRows = new OutputViewRow[0];
    }
    else
    {
    	aRows = copyRowsToPositional(aView.getRows());
        Arrays.sort(aRows, new RowBySpanColumnComparator(getFieldIdForName(aView, actualFName)));
    }
    computeDifference(eRows, getFieldIdForName(eView, expectedFName), aRows,
        getFieldIdForName(aView, actualFName), diff);
    return diff;
  }

  private static final OutputViewRow[] copyRowsToPositional(OutputViewRow[] rows) {
    OutputViewRow[] outRows = new OutputViewRow[rows.length];
    for (int i = 0; i < rows.length; i++) {
      outRows[i] = new RowWithPosition(rows[i], i);
    }
    return outRows;
  }

  // Not quite correct as of now.
  private static void computeDifference(OutputViewRow[] exRows, final int ePos,
      OutputViewRow[] acRows, final int aPos, DocSpanDifference diff) {
    RowListPair resultPair = computeCorrect(exRows, acRows, ePos, aPos, diff);
    List<OutputViewRow> eRowList = resultPair.getExpected();
    List<OutputViewRow> aRowList = resultPair.getActual();
    int i = 0;
    int j = 0;
    while (i < eRowList.size() && j < aRowList.size()) {
      int compare = 0;
      SpanVal eSpan = null;
      SpanVal aSpan = null;

      RowWithPosition eRow = (RowWithPosition) eRowList.get(i);
      if (eRow.fieldValues[ePos] instanceof SpanVal)
        eSpan = (SpanVal) eRow.fieldValues[ePos];

      RowWithPosition aRow = (RowWithPosition) aRowList.get(j);
      if (aRow.fieldValues[aPos] instanceof SpanVal)
        aSpan = (SpanVal) aRow.fieldValues[aPos];

      if (eSpan != null && aSpan != null)
        compare = Util.spanOrderCompare(eSpan, aSpan);

      if (compare == 0) {
        // Not span or spans are equal, continue
        ++i;
        ++j;
      } else if (compare < 0) {
        // expected is smaller. check if there is overlap.
        if (eSpan.end > aSpan.start) {
          diff.addExpectedPartial(eRow.getPos());
          diff.addActualPartial(aRow.getPos());
          ++i;
          ++j;
        } else {
          // no overlap, expected is missing
          diff.addMissing(eRow.getPos());
          ++i;
        }
      } else {
        // actual is smaller. check if there is overlap.
        if (aSpan.end > eSpan.start) {
          diff.addExpectedPartial(eRow.getPos());
          diff.addActualPartial(aRow.getPos());
          ++i;
          ++j;
        } else {
          // no overlap, actual is spurious
          diff.addSpurious(aRow.getPos());
          ++j;
        }
      }
    }
    // Mopping up (there may be either missing or spurious at the end)
    while (i < eRowList.size()) {
      diff.addMissing(((RowWithPosition) eRowList.get(i)).getPos());
      ++i;
    }
    while (j < aRowList.size()) {
      diff.addSpurious(((RowWithPosition) aRowList.get(j)).getPos());
      ++j;
    }
  }

  private static RowListPair computeCorrect(OutputViewRow[] eRows, OutputViewRow[] aRows, int ePos,
      int aPos, DocSpanDifference diff) {
    int i = 0;
    int j = 0;
    List<OutputViewRow> eRest = new ArrayList<OutputViewRow>();
    List<OutputViewRow> aRest = new ArrayList<OutputViewRow>();
    while (i < eRows.length && j < aRows.length) {
      int compare = 0;
      SpanVal eSpan = null;
      SpanVal aSpan = null;

      RowWithPosition eRow = (RowWithPosition) eRows[i];
      if (eRow.fieldValues[ePos] instanceof SpanVal)
        eSpan = (SpanVal) eRow.fieldValues[ePos];

      RowWithPosition aRow = (RowWithPosition) aRows[j];
      if (aRow.fieldValues[aPos] instanceof SpanVal)
        aSpan = (SpanVal) aRow.fieldValues[aPos];

      if (eSpan != null && aSpan != null)
        compare = Util.spanOrderCompare(eSpan, aSpan);

      if (compare == 0) {   // Not span or spans are equal
        if (eSpan != null && aSpan != null) {   // spans are equal
          diff.expectedCorrect.add(eRow.getPos());
          diff.actualCorrect.add(aRow.getPos());
        }
        ++i;
        ++j;
      } else if (compare < 0) {
        // expected is smaller
        eRest.add(eRow);
        ++i;
      } else {
        // actual is smaller
        aRest.add(aRow);
        ++j;
      }
    }
    // Mop up the rest
    while (i < eRows.length) {
      eRest.add(eRows[i]);
      ++i;
    }
    while (j < aRows.length) {
      aRest.add(aRows[j]);
      ++j;
    }
    return new RowListPair(eRest, aRest);
  }

  private void addExpectedPartial(int i) {
    this.expectedPartial.add(i);
  }

  private void addActualPartial(int i) {
    this.actualPartial.add(i);
  }

  private void addMissing(int i) {
    this.expectedMissing.add(i);
  }

  private void addSpurious(int i) {
    this.actualSpurious.add(i);
  }

  /**
   * Get the model for the expected results.
   * 
   * @return
   */
  public SystemTComputationResult getExpectedModel() {
    return this.expectedModel;
  }

  /**
   * Get the model for the actual results.
   * 
   * @return
   */
  public SystemTComputationResult getActualModel() {
    return this.actualModel;
  }

  /**
   * Get the name of the view we're comparing.
   * 
   * @return
   */
  public String getViewName() {
    return this.viewName;
  }

  /**
   * Get the field name of the expected results we're comparing.
   * 
   * @return
   */
  public String getExpectedFieldName() {
    return this.expectedFieldName;
  }

  /**
   * Get the field name of the actual results we're comparing.
   * 
   * @return
   */
  public String getActualFieldName() {
    return this.actualFieldName;
  }

  /**
   * Get a list of the partial matches from the expected result. The integers are 0-based row IDs.
   * 
   * @return
   */
  public List<Integer> getExpectedPartial() {
    return this.expectedPartial;
  }

  /**
   * Get a list of the missing matches from the expected result. The integers are 0-based row IDs.
   * 
   * @return
   */
  public List<Integer> getExpectedMissing() {
    return this.expectedMissing;
  }

  /**
   * Get a list of the partial matches from the actual result. The integers are 0-based row IDs.
   * 
   * @return
   */
  public List<Integer> getActualPartial() {
    return this.actualPartial;
  }

  /**
   * Get a list of the spurious matches from the expected result. The integers are 0-based row IDs.
   * 
   * @return
   */
  public List<Integer> getActualSpurious() {
    return this.actualSpurious;
  }

}
