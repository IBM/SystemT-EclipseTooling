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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.ibm.biginsights.textanalytics.util.common.Constants;

/**
 * Top level element of a SystemT run result. The result of a run is a list of output views.
 * <p>
 * 
 * Uses JAXB for serialization/deserialization.
 */
@XmlRootElement(namespace = "http://www.ibm.com/systemt/result")
public class SystemTComputationResult implements Comparable<SystemTComputationResult>{



  private OutputView[] outputViews;

  // A mapping of integer IDs to strings that represents the various text objects that we have spans
  // over.
  private Map<Integer, String> textMap;
  
  private int inputTextID;

  private String jsonDocumentLocation = null;

  @XmlAttribute
  public boolean gsComplete = false;

  public int getInputTextID() {
    return this.inputTextID;
  }

  public void setInputTextID(int inputTextID) {
    this.inputTextID = inputTextID;
  }

  // The ID of the document that we ran SystemT on. This may be a file name, a pseudo-URL or
  // whatever. At the moment, it is used only for display purposes.
  private String documentID;
 
  public SystemTComputationResult() {
    super();
    this.textMap = new HashMap<Integer, String>();
  }
  
  public String getInputText() {
    return stripFieldNamePrefix(this.textMap.get(this.inputTextID));
  }

  public OutputView[] getOutputViews() {
    return this.outputViews;
  }
  
  public boolean containsTextID(int textID) {
    return this.textMap.containsKey(Integer.valueOf(textID));
  }

  public String getText(int textID) {
    return stripFieldNamePrefix(this.textMap.get(Integer.valueOf(textID)));
  }

  public String getFieldNameOfText(int textID) {
    return getFieldName( this.textMap.get(Integer.valueOf(textID)) );
  }

  public void addText(int textID, String text) {
    this.textMap.put(Integer.valueOf(textID), text);
  }

  public void addText(int textID, String field, String text) {
    String textWithField = Constants.FIELD_NAME_PREFIX + field + Constants.FIELD_VALUE_PREFIX + text;
    this.textMap.put(Integer.valueOf(textID), textWithField);
  }

  public void setOutputViews(OutputView[] outputViews) {
    this.outputViews = outputViews;
  }

  public String getJsonDocumentLocation ()
  {
    return jsonDocumentLocation;
  }

  public void setJsonDocumentLocation (String jsonDocumentLocation)
  {
    this.jsonDocumentLocation = jsonDocumentLocation;
  }

  /**
   * This is just for testing, don't use for really testing equality
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof SystemTComputationResult) {
      SystemTComputationResult res = (SystemTComputationResult) o;
      if (this.documentID == null && res.documentID != null) {
        return false;
      }
      if (this.documentID != null && !this.documentID.equals(res.documentID)) {
        return false;
      }
      if (this.outputViews == null) {
        return res.outputViews == null;
      }
      if (res.outputViews == null) {
        return false;
      }
      if (this.outputViews.length != res.outputViews.length) {
        return false;
      }
      if (res.textMap == null && this.textMap != null) {
        return false;
      }
      if (res.textMap != null && this.textMap != null) {
        if (this.textMap.keySet().size() != res.textMap.keySet().size()) {
          return false;
        }
        Set<Integer> resKeys = res.textMap.keySet();
        for (Integer key : this.textMap.keySet()) {
          if (!resKeys.contains(key)) {
            return false;
          }
          if (!this.textMap.get(key).equals(res.textMap.get(key))) {
            return false;
          }
        }
      }
      for (int i = 0; i < this.outputViews.length; i++) {
        if (!this.outputViews[i].equals(res.outputViews[i])) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  public void setTextMap(Map<Integer, String> textMap) {
    this.textMap = textMap;
  }

  public Map<Integer, String> getTextMap() {
    return this.textMap;
  }

  public Map<Integer, String> getTextValueMap() {
    Map<Integer, String> textValueMap = new HashMap<Integer, String>();
    for (Integer i : this.textMap.keySet ()) {
      textValueMap.put (i, stripFieldNamePrefix (this.textMap.get (i)));
    }
    return textValueMap;
  }

  public void setDocumentID(String documentID) {
    this.documentID = documentID;
  }

  public String getDocumentID() {
    return this.documentID;
  }

  public String stripFieldNamePrefix (String txt)
  {
    if (txt.startsWith (Constants.FIELD_NAME_PREFIX) && txt.indexOf (Constants.FIELD_VALUE_PREFIX) > 0)
      return txt.substring (txt.indexOf (Constants.FIELD_VALUE_PREFIX) + Constants.FIELD_VALUE_PREFIX.length ());
    else
      return txt;
  }

  public String getFieldName (String txt)
  {
    if (txt.startsWith (Constants.FIELD_NAME_PREFIX) && txt.indexOf (Constants.FIELD_VALUE_PREFIX) > 0)
      return txt.substring (Constants.FIELD_NAME_PREFIX.length (), txt.indexOf (Constants.FIELD_VALUE_PREFIX));
    else
      return null;
  }

public int compareTo(SystemTComputationResult arg0) {
	SystemTComputationResult result0 =  (SystemTComputationResult)arg0;
	return this.getDocumentID().compareTo(result0.getDocumentID());
}

public int getInputTextIDForThisSchema(String docSchema)
{
	OutputView[] opViews = this.getOutputViews();
	if (opViews!=null)
	{
		for (OutputView view : opViews) {
			// compare the output view selected by user with the op view in the result
				OutputViewRow[] rows = view.getRows();
				if ((rows != null)
						&& (rows.length > 0)) {
					for(int j=0;j<rows.length;j++)
					{
						FieldValue[] values = rows[j].fieldValues;
						if (values != null)
						{
							for (int k=0;k<values.length;k++)
							{
								
								FieldValue val = values[k];
								FieldType type = val.getType();
								SpanVal spanVal = null;
								if (type == FieldType.SPAN) {
									// only look at field values of type span, 
									spanVal = (SpanVal) val;
									if (spanVal.parentSpanName != null)
									{
										if (spanVal.parentSpanName.equals(docSchema))
										{
					//						System.out.println("docSchema passed is " + docSchema + "getInputTextIDForThisSchema - for this result " + this.getDocumentID() + " returning the sourceid as " + spanVal.sourceID);
											return spanVal.sourceID; 
										}
									}
								}
							}
						}
					}
					
				}
		}
	}
	return this.getInputTextID();
}

/**
 * This method is to clear all the content of the model 
 */
public void clear(){
  
  //Making old views garbage-collected.
  if (null != outputViews) {
    Arrays.fill (outputViews, 0, outputViews.length, null);
  }
  outputViews = null;
  
  // clean the map..
  textMap.clear ();
  textMap = null;
}

}
