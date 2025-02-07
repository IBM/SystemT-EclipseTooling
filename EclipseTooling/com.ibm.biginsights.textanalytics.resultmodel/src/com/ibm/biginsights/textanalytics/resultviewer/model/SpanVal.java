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

import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.SPAN;
import static com.ibm.biginsights.textanalytics.util.common.Constants.SPAN_UNDEFINED_SOURCE_ID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A SystemT view span field value.
 */
@XmlRootElement
public class SpanVal extends FieldValue {



  @XmlAttribute
  public int start;

  @XmlAttribute
  public int end;

  // The source of the span is a pointer to the text that the span annotates (not the text itself).
  // This could be a file name, for example.
  // @XmlAttribute
  // public String source;

  // The source ID of a span is a reference to the Text object the span is a span over.
  @XmlAttribute
  public int sourceID;

  @XmlAttribute
  public String parentSpanName;

  public SpanVal() {
    super();
  }

  public SpanVal(int start, int end, int sourceID) {
    super();
    this.start = start;
    this.end = end;
    this.sourceID = sourceID;
  }

  /**
   * Check if span value is null. We encode null spans internally not as null, but as spans with an
   * invalid source text id.
   * 
   * @return <code>true</code> iff this span is null.
   */
  public boolean isNull() {
    return this.sourceID == SPAN_UNDEFINED_SOURCE_ID;
  }

  public String getText(SystemTComputationResult model) {
    if (isNull()) {
      return null;
    }
    String baseText = model.getTextValueMap().get(this.sourceID);
    return baseText.substring(this.start, this.end);
  }

  @Override
  public String toString() {
    return "[" + this.start + ", " + this.end + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  @Override
  public FieldType getType() {
    return SPAN;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SpanVal) {
      SpanVal span = (SpanVal) o;
      return (this.start == span.start) && (this.end == span.end) && this.sourceID == span.sourceID;
    }
    return false;
  }

}
