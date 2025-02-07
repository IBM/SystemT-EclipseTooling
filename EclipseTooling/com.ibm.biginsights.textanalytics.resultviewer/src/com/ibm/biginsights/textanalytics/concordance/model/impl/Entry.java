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
package com.ibm.biginsights.textanalytics.concordance.model.impl;

import static com.ibm.biginsights.textanalytics.concordance.model.impl.ConcordanceModel.CTX_SIZE;

import java.util.ArrayList;
import java.util.List;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;

public class Entry implements IConcordanceModelEntry {


 
	// Implementation note: an entry here is only a very thin wrapper around a SystemT computation
  // result, so we don't duplicate the info. All the table entries are computed on demand when they
  // are accessed. This should allow for very fast load time and efficient memory use. It may lead
  // to some lag while scrolling in very large result sets. Can't have everything...
  private final SystemTComputationResult model;

  private final ConcordanceModel cm;

  private final int viewID;

  private final int row;

  private final int column;

  private final String docSchemaName;

  public Entry(SystemTComputationResult model, int viewID, int row, int col, String docSchemaName,
      ConcordanceModel cm) {
    super();
    this.model = model;
    this.viewID = viewID;
    this.row = row;
    this.column = col;
    this.docSchemaName = docSchemaName;
    this.cm = cm;
  }

  private static final String normalize(String s) {
    return s.replaceAll("\\s", " "); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public int getTextSourceID() {
    return getSpan().sourceID;
  }

  /**
   * @return the docId
   */
  public String getDocId() {
    return this.model.getDocumentID();
  }

  private final SpanVal getSpan() {
    OutputView view = this.model.getOutputViews()[this.viewID];
    OutputViewRow row1 = view.getRows()[this.row];
    return (SpanVal) row1.fieldValues[this.column];
  }

  /**
   * @return the annotText
   */
  public String getAnnotationText() {
    SpanVal span = getSpan();
    String text = this.model.getTextValueMap().get(span.sourceID);
    return normalize(text.substring(span.start, span.end));
  }

  /**
   * @return the annotType
   */
  public String getAnnotationType() {
    return this.cm.getTypeName(this.viewID, this.column);
  }

  /**
   * @return the leftContext
   */
  public String getLeftContext() {
    SpanVal span = getSpan();
    String text = this.model.getTextValueMap().get(span.sourceID);
    final int start = (span.start < CTX_SIZE) ? 0 : span.start - CTX_SIZE;
    return normalize(text.substring(start, span.start));
  }

  /**
   * @return the rightContext
   */
  public String getRightContext() {
    SpanVal span = getSpan();
    String text = this.model.getTextValueMap().get(span.sourceID);
    final int docEnd = text.length();
    int contextEnd = span.end + CTX_SIZE;
    if (contextEnd > docEnd) {
      contextEnd = docEnd;
    }
    return normalize(text.substring(span.end, contextEnd));
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getDocId());
    sb.append("\t..."); //$NON-NLS-1$
    sb.append(getLeftContext());
    sb.append("\t"); //$NON-NLS-1$
    sb.append(getAnnotationText());
    sb.append("\t"); //$NON-NLS-1$
    sb.append(getRightContext());
    sb.append("...\t"); //$NON-NLS-1$
    sb.append(getAnnotationType());
    return sb.toString();
  }

  @Override
  public String getBaseText() {
    SpanVal span = getSpan();
    return this.model.getTextValueMap().get(span.sourceID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry#getOffsets()
   */
  @Override
  public List<Integer> getOffsets() {
    List<Integer> list = new ArrayList<Integer>(2);
    SpanVal span = getSpan();
    list.add(span.start);
    list.add(span.end);
    return list;
  }

  @Override
  public String getDocSchemaName() {
    return this.docSchemaName;
  }

  @Override
  public SystemTComputationResult getModel() {
    return this.model;
  }

  @Override
  public IConcordanceModel getConcordanceModel() {
    return this.cm;
  }

  @Override
  public OutputView getOutputView() {
    return this.model.getOutputViews()[this.viewID];
  }

  @Override
  public OutputViewRow getRow() {
    return getOutputView().getRows()[this.row];
  }
  
  @Override
  public int getRowIndex(){
	  return this.row;
  }
}
