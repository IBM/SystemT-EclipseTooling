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
package com.ibm.biginsights.textanalytics.resultviewer.test;

import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.INT;
import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.TEXT;
import static junit.framework.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ibm.biginsights.textanalytics.resultviewer.model.FieldType;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.IntVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.ListVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.model.TextVal;

public class ModelTest {

  @Test
  public void testModelSerialization() throws Exception {

    // Manually create a very simple model
    List<OutputView> viewList = new ArrayList<OutputView>();
    OutputView view;

    IntVal intValue = new IntVal();
    intValue.val = 42;

    TextVal textValue = new TextVal();
    textValue.val = "FooBar";

    ListVal listValue = new ListVal();
    listValue.values = new ArrayList<FieldValue>();
    listValue.values.add(intValue);
    listValue.values.add(textValue);

    FieldValue[] rowValues = { intValue, textValue, listValue };
    OutputViewRow row = new OutputViewRow();
    // row.setFieldValues(rowValues);
    row.fieldValues = rowValues;

    view = new OutputView("ViewName1", new String[] { "foo", "bar" }, new FieldType[] { TEXT, INT });
    view.setRows(new OutputViewRow[] { row, row });
    viewList.add(view);
    view = new OutputView("ViewName2", new String[] {}, new FieldType[] {});
    view.setRows(new OutputViewRow[] {});
    viewList.add(view);
    view = new OutputView("ViewName3", new String[] {}, new FieldType[] {});
    view.setRows(new OutputViewRow[] {});
    viewList.add(view);

    OutputView[] viewArray = viewList.toArray(new OutputView[viewList.size()]);
    SystemTComputationResult result = new SystemTComputationResult();
    result.setOutputViews(viewArray);
    result.setDocumentID("My document ID");

    // Serialize the model to a byte array
    Serializer serializer = new Serializer();
    byte[] bytes = serializer.getBytesForModel(result);
    System.out.println(new String(bytes, "UTF-8"));
    InputStream is = new ByteArrayInputStream(bytes);
    SystemTComputationResult resultCopy = serializer.getModelForInputStream(is);
    assertEquals("There are model differences after marshalling and unmarshalling.", result,
        resultCopy);
  }

}
