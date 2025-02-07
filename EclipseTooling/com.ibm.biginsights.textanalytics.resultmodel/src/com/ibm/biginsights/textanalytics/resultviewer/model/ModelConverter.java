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

import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.BOOL;
import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.FLOAT;
import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.INT;
import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.LIST;
import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.SPAN;
import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.STRING;
import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.TEXT;
import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.NULL;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ibm.avatar.algebra.datamodel.AbstractTupleSchema;
import com.ibm.avatar.algebra.datamodel.Pair;
import com.ibm.avatar.algebra.datamodel.ScalarList;
import com.ibm.avatar.algebra.datamodel.Span;
import com.ibm.avatar.algebra.datamodel.TLIter;
import com.ibm.avatar.algebra.datamodel.Text;
import com.ibm.avatar.algebra.datamodel.Tuple;
import com.ibm.avatar.algebra.datamodel.TupleList;
import com.ibm.biginsights.textanalytics.resultmodel.Activator;
import com.ibm.biginsights.textanalytics.resultmodel.Messages;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Utility class to convert from SystemT tuples to the models used by the tooling to display the results.
 */
public abstract class ModelConverter
{

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  /**
   * Take the results of SystemT computation and create a model from them.
   * 
   * @param label The name of the input document.
   * @param spanVal A Set Objects representing the input document.
   * @param outputViews The output views of a SystemT run. A map from view names to tuple lists. If aliases have been
   *          declared, that would be the name here.
   * @param outputViewOriginalNameMap A mapping of output view aliases to their actual view names.
   * @return The model. This can be serialized to disk using the {@link Serializer Serializer} class.
   */
  public static SystemTComputationResult createModel (String label, Set<Object> spanVal,
    Map<String, TupleList> outputViews, Map<String, String> outputViewOriginalNameMap)
  {
    SystemTComputationResult model = new SystemTComputationResult ();
    model.setDocumentID (label);
    // final String textString = removeCarriageReturn(text.getText());
    Iterator<Object> itr = spanVal.iterator ();
    while (itr.hasNext ()) {
      Object fieldVal = itr.next ();
      // Text is subclass of Span, so we need to check for Text before Span.
      if (fieldVal instanceof Text) {
        Text t1 = (Text) fieldVal;
        model.addText (t1.hashCode (), t1.getText ());
        model.setInputTextID (t1.hashCode ());
      }
      else if (fieldVal instanceof Span) {
        Span s1 = (Span) fieldVal;
        model.addText (s1.hashCode (), s1.getText ());
        model.setInputTextID (s1.hashCode ());
      }
      else if (fieldVal instanceof Integer) {
        Integer i1 = (Integer) fieldVal;
        model.addText (i1.hashCode (), i1.toString ());
        model.setInputTextID (i1.hashCode ());
      }
      else if (fieldVal instanceof Boolean) {
        Boolean b1 = (Boolean) fieldVal;
        model.addText (b1.hashCode (), b1.toString ());
        model.setInputTextID (b1.hashCode ());
      }
      else if (fieldVal instanceof Float) {
        Float f1 = (Float) fieldVal;
        model.addText (f1.hashCode (), f1.toString ());
        model.setInputTextID (f1.hashCode ());
      }
    }
    OutputView[] views = new OutputView[outputViews.size ()];
    model.setOutputViews (views);
    int count = 0;
    for (Entry<String, TupleList> entry : outputViews.entrySet ()) {
      TupleList tl = entry.getValue ();
      AbstractTupleSchema schema = tl.getSchema ();
      FieldType[] types = getFieldTypes (schema);
      String[] headers = getFieldNames (schema);
      String origView = entry.getKey ();
      if (outputViewOriginalNameMap.containsKey (entry.getKey ())) {
        // If the output view name was an alias, set origView to the actual view name
        origView = outputViewOriginalNameMap.get (entry.getKey ());
        // else origview and output view name would have the same value.
      }
      OutputView view = new OutputView (entry.getKey (), origView, headers, types);
      views[count] = view;
      ++count;
      addRowsToView (tl, view, types, model, schema);
    }
    return model;
  }

  /**
   * Take the results of SystemT computation and create a model from them.
   * 
   * @param label The name of the input document.
   * @param spanVal A Set Objects representing the input document.
   * @param outputViews The output views of a SystemT run. A map from view names to tuple lists. If aliases have been
   *          declared, that would be the name here.
   * @param outputViewOriginalNameMap A mapping of output view aliases to their actual view names.
   * @return The model. This can be serialized to disk using the {@link Serializer Serializer} class.
   */
  public static SystemTComputationResult createModelForJsonOrCsv (String label, String[] fieldNames, Object[] spanVals,
    Map<String, TupleList> outputViews, Map<String, String> outputViewOriginalNameMap)
  {
    SystemTComputationResult model = new SystemTComputationResult ();
    model.setDocumentID (label);

    for (int i = 0; i < fieldNames.length; i++) {
      Object fieldVal = spanVals[i];
      String fieldName = fieldNames[i];

      if (fieldVal instanceof Span) {
        Span s1 = (Span) fieldVal;
        model.addText (s1.hashCode (), fieldName, s1.getText ());
        model.setInputTextID (s1.hashCode ());
      }
      else if (fieldVal instanceof Text) {
        Text t1 = (Text) fieldVal;
        model.addText (t1.hashCode (), fieldName, t1.getText ());
        model.setInputTextID (t1.hashCode ());
      }
      else if (fieldVal instanceof Integer) {
        Integer i1 = (Integer) fieldVal;
        model.addText (i1.hashCode (), fieldName, i1.toString ());
        model.setInputTextID (i1.hashCode ());
      }
      else if (fieldVal instanceof Boolean) {
        Boolean b1 = (Boolean) fieldVal;
        model.addText (b1.hashCode (), fieldName, b1.toString ());
        model.setInputTextID (b1.hashCode ());
      }
      else if (fieldVal instanceof Float) {
        Float f1 = (Float) fieldVal;
        model.addText (f1.hashCode (), fieldName, f1.toString ());
        model.setInputTextID (f1.hashCode ());
      }
    }

    OutputView[] views = new OutputView[outputViews.size ()];
    model.setOutputViews (views);
    int count = 0;
    for (Entry<String, TupleList> entry : outputViews.entrySet ()) {
      TupleList tl = entry.getValue ();
      AbstractTupleSchema schema = tl.getSchema ();
      FieldType[] types = getFieldTypes (schema);
      String[] headers = getFieldNames (schema);
      String origView = entry.getKey ();
      if (outputViewOriginalNameMap.containsKey (entry.getKey ())) {
        // If the output view name was an alias, set origView to the actual view name
        origView = outputViewOriginalNameMap.get (entry.getKey ());
        // else origview and output view name would have the same value.
      }
      OutputView view = new OutputView (entry.getKey (), origView, headers, types);
      views[count] = view;
      ++count;
      addRowsToView (tl, view, types, model, schema);
    }
    return model;
  }

  private static void addRowsToView (TupleList tl, OutputView view, FieldType[] types, SystemTComputationResult model,
    AbstractTupleSchema schema)
  {
    TLIter it = tl.iterator ();
    final int numRows = tl.size ();
    OutputViewRow[] rows = new OutputViewRow[numRows];
    view.setRows (rows);
    int count = 0;
    while (it.hasNext ()) {
      Tuple tup = it.next ();
      OutputViewRow row = new OutputViewRow (types.length);
      rows[count] = row;
      ++count;
      for (int i = 0; i < types.length; i++) {
        row.put (i, systemtToModelValue (schema.getCol (tup, i), types[i], model));
      }
    }
  }

  private static String[] getFieldNames (AbstractTupleSchema schema)
  {
    final int size = schema.size ();
    String[] names = new String[size];
    for (int i = 0; i < size; i++) {
      names[i] = schema.getFieldNameByIx (i);
    }
    return names;
  }

  private static final FieldType[] getFieldTypes (AbstractTupleSchema schema)
  {
    final int size = schema.size ();
    FieldType[] types = new FieldType[size];
    for (int i = 0; i < size; i++) {
      com.ibm.avatar.algebra.datamodel.FieldType fieldType = schema.getFieldTypeByIx (i);
      types[i] = systemtToModelType (fieldType);
    }
    return types;
  }

  private static final FieldType systemtToModelType (com.ibm.avatar.algebra.datamodel.FieldType t)
  {
    if (t == null) {
      // this ideally should not occur - but if the type returned is null then treat it as a String (defect 19242)
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (Messages.ModelConverter_FieldTypeNull); //$NON-NLS-1$
      return STRING;
    }
    if (t.getIsNullType()) { return NULL; }
    if (t.getIsScalarType ()) {
      if (t.equals (com.ibm.avatar.algebra.datamodel.FieldType.BOOL_TYPE)) { return BOOL; }
      if (t.getIsIntegerType ()) { return INT; }
      if (t.getIsFloatType ()) { return FLOAT; }
      if (t.getIsText ()) { return TEXT; }
      if (t.getIsSpan ()) { return SPAN; }
      if (t.getIsStringType ()) { return STRING; }
    }
    if (t.getIsScalarListType ()) { return LIST; }
    return null;
  }

  /**
   * Converts a field value in a system t result tuple to an equivalent representation in SystemTComputationResult
   * 
   * @param value
   * @param type
   * @param model
   * @return
   */
  private static final FieldValue systemtToModelValue (Object value, FieldType type, SystemTComputationResult model)
  {
    if (value == null) {
      /*
       * If a field value in the results from runtime is null, do not pass it on. JAXB would ignore them while
       * serializing to strf files, causing inconsistencies in the number of fields declared by an output view and in
       * the actual number fields in the output view rows. Instead, based on that column's type, return appropriate
       * representative object containing/indicating null value.
       */
      switch (type) {
        case BOOL:
          return new BoolVal (null);
        case FLOAT:
          return new FloatVal (null);
        case INT:
          return new IntVal (null);
        case STRING:
          return new StringVal (null);
        case LIST:
          ListVal lVal = new ListVal ();
          lVal.values = null;
          return lVal;
        case TEXT:
          return new TextVal (null);
        case SPAN:
          SpanVal nullSpan = new SpanVal (Constants.SPAN_UNDEFINED_OFFSET, Constants.SPAN_UNDEFINED_OFFSET,
            Constants.SPAN_UNDEFINED_SOURCE_ID);
          nullSpan.parentSpanName = "Document.text"; // Arbitrarily assigning parent span 'Document.text'. Labelled //$NON-NLS-1$
                                                     // collection import from extraction results would accept only
                                                     // those results where all spans had parent span as
                                                     // 'Document.text'. (This is a documented limitation for labelled
                                                     // collections.)
                                                     // This was preventing extraction results containing null spans
                                                     // from being accepted. Hence assigning a parent even though it
                                                     // shouldn't have one.
          return nullSpan;
        case NULL:
          // Returns `new TextVal(null)` to show '<null>' in the view.
          return new TextVal (null);
      }
    }

    if (value instanceof Boolean) {
      Boolean b = (Boolean) value;
      return new BoolVal (b);
    }
    else if (value instanceof Float) {
      Float f = (Float) value;
      return new FloatVal (f);
    }
    else if (value instanceof Integer) {
      return new IntVal ((Integer) value);
    }
    else if (value instanceof String) {
      return new StringVal ((String) value);
    }
    else if (value instanceof ScalarList<?>) {
      ListVal listValue = new ListVal ();

      ScalarList<?> scList = (ScalarList<?>) value;
      listValue.values = new ArrayList<FieldValue> ();

      ListIterator<?> iter = scList.listIterator ();
      while (iter.hasNext ()) {
        Object lVal = iter.next ();
        FieldValue val = systemtToModelValue (lVal, systemtToModelType (scList.getScalarType ()), model);
        if (val != null) {
          listValue.values.add (val);
        }
      }
      return listValue;
    }
    // Text is subclass of Span, so we need to check for Text before Span.
    // If we check for Span first, it will catch all Text objects and treat
    // them as Span -> save an empty document to the result file and cause
    // trouble for provenance later.
    else if (value instanceof Text) {
      Text text = (Text) value;
      // Escape the invalid character in the text.
      return new TextVal (StringUtils.escapeInvalidXMLChars (model.getDocumentID (), text.getText ()));
    }
    else if (value instanceof Span) {
      Span span = (Span) value;
      int textKey = span.getDocTextObj ().hashCode ();
      if (!model.containsTextID (textKey)) {
        model.addText (textKey, span.getDocText ());
      }
      SpanVal val = new SpanVal (span.getBegin (), span.getEnd (), textKey);
      val.parentSpanName = getParentSpanName (span);
      return val;
    }

    return null;
  }

  private static final String getParentSpanName (Span span)
  {
    Pair<String, String> viewAndColumn = span.getDocTextObj ().getViewAndColumnName ();

    if (viewAndColumn != null) {
      String name = viewAndColumn.first;
      String spanFieldName = viewAndColumn.second;

      if (name != null &&
          !name.startsWith ("[") &&             //$NON-NLS-1$
          spanFieldName != null)
        return name + "." + spanFieldName;      //$NON-NLS-1$
    }

    return "";
  }

}
