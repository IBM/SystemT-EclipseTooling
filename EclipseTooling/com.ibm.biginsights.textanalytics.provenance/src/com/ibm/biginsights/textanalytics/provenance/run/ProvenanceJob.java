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
package com.ibm.biginsights.textanalytics.provenance.run;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.algebra.datamodel.AbstractTupleSchema;
import com.ibm.avatar.algebra.datamodel.FieldGetter;
import com.ibm.avatar.algebra.datamodel.FieldSetter;
import com.ibm.avatar.algebra.datamodel.FieldType;
import com.ibm.avatar.algebra.datamodel.Pair;
import com.ibm.avatar.algebra.datamodel.ScalarList;
import com.ibm.avatar.algebra.datamodel.TLIter;
import com.ibm.avatar.algebra.datamodel.Text;
import com.ibm.avatar.algebra.datamodel.TextSetter;
import com.ibm.avatar.algebra.datamodel.Tuple;
import com.ibm.avatar.algebra.datamodel.TupleList;
import com.ibm.avatar.algebra.datamodel.TupleSchema;
import com.ibm.avatar.algebra.util.document.HtmlViz;
import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.api.DocReader;
import com.ibm.avatar.api.OperatorGraph;
import com.ibm.avatar.api.exceptions.ModuleLoadException;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.avatar.provenance.AQLProvenanceRewriter;
import com.ibm.biginsights.textanalytics.provenance.Activator;
import com.ibm.biginsights.textanalytics.provenance.view.ProvenanceView;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Compute and display the provenance of a view on a single document.
 * 
 *
 */
public class ProvenanceJob extends Job
{

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  /** The text of the input document. */
  @SuppressWarnings("unused")
  private final String inputDocText;

  /** The label of the input document */
  private final String inputDocName;

  /** The name of the view we compute provenance for. Needs to be the actual view name, not the output alias.*/
  private String viewName;

  /** Parameters for computing provenance */
  private ProvenanceRunParams params;

  /** Used for keeping information about input data */
  private HashMap<String, String> fieldNameValuePairs;

  private String projectName;

  /**
   * Default constructor.
   * 
   * @param name
   * @param inputDocName
   * @param inputDocText
   * @param viewName
   * @param params
   */
  public ProvenanceJob (String name, String inputDocName, String inputDocText,
                        HashMap<String, String> fieldNameValuePairs, String viewName,
                        String projectName, ProvenanceRunParams params)
  {
    super (name);
    this.inputDocName = inputDocName;
    this.inputDocText = inputDocText;
    this.fieldNameValuePairs = fieldNameValuePairs;
    this.viewName = viewName;
    this.projectName = projectName;
    this.params = params;
  }

  /**
   * Name of the auxiliary column the provenance rewrite generates to hold tuple IDs.
   */
  private static final String AUTO_ID_COL_NAME = "__auto__id";

  /**
   * Name of the field in the XML representation of provenance that holds tuple/node IDs.
   */
  public static final String TUPLE_ID_FIELD_NAME = "ID";

  /**
   * Name of the field in the XML representation of provenance that holds the type of each node in the provenance graph.
   */
  public static final String TUPLE_TYPE_FIELD_NAME = "Type";

  /**
   * Name of the field in the XML representation of provenance that holds the contents of the tuple corresponding to
   * each node in the provenance graph.
   */
  public static final String TUPLE_VALUE_FIELD_NAME = "Tuple";

  /**
   * Name of the field in the XML representation of provenance that holds the AQL operation (join, consolidate, etc.)
   * that a given node in the provenance graph represents.
   */
  public static final String TUPLE_OPERATION_FIELD_NAME = "Operation";

  /** the ending of source tuples of various operators */
  protected static final String SELECT_ID_END = "____id";
  protected static final String UNION_ID_END = "__union_op__id";
  protected static final String CONSOLIDATE_START = "__Consolidate__";

  /**
   * Various patterns for recognizing auto generated view names so that we can print a more user-friendly name.
   */
  private static final Pattern UNION_TEST = Pattern.compile ("__Union__\\d+__TempOp__(\\d+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern CONSOLIDATE_TEST = Pattern.compile ("__Consolidate__\\d+__Temp__(\\d+)",
    Pattern.CASE_INSENSITIVE);
  private static final Pattern MINUS_TEST = Pattern.compile ("__Minus__\\d+__TempOp__(\\d+)", Pattern.CASE_INSENSITIVE);

  private static final Pattern SUBQUERY_TEST = Pattern.compile ("__Subquery__(\\d+)__Temp", Pattern.CASE_INSENSITIVE);

  /**
   * Handle the request to compute the provenance of an output view on a single input document.
   */
  @SuppressWarnings("unused")
  @Override
  public IStatus run (IProgressMonitor monitor)
  {

    // Log INFO
    String message = String.format (Constants.PROVENANCE_JOB_PROGRESS_LABEL_FORMAT, viewName, this.inputDocName);
    LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (message);

    try {
      
      String provTamPath = getProvenanceTamPaths (projectName);
      provTamPath = ProjectUtils.removeDuplicateResources (provTamPath);

      OperatorGraph og = OperatorGraph.createOG ( params.getSelectedModules (), provTamPath, 
        params.getExtTypeInfo (), params.getTokenizerConfig ());

      LangCode lang = params.getLanguage ();

      // Create a document tuple
      TupleSchema docSchema = og.getDocumentSchema ();
      Tuple doc = docSchema.createTup ();

      for (String fieldName : fieldNameValuePairs.keySet ()) {

        String valueAsStr = fieldNameValuePairs.get (fieldName);

        FieldType fieldType = docSchema.getFieldTypeByName (fieldName);
        if (fieldType.getIsIntegerType ()) {
          FieldSetter<Integer> setter = docSchema.intSetter (fieldName);
          setter.setVal (doc, Integer.valueOf (valueAsStr));
        }
        else if (fieldType.getIsFloatType ()) {
          FieldSetter<Float> setter = docSchema.floatSetter (fieldName);
          setter.setVal (doc, Float.valueOf (valueAsStr));
        }
        else if (fieldType.getIsBooleanType ()) {
          FieldSetter<Object> genericSetter = docSchema.genericSetter (fieldName, FieldType.BOOL_TYPE);
          genericSetter.setVal (doc, Boolean.valueOf (valueAsStr));
        }
        else if (fieldType.getIsStringType ()) {
          FieldSetter<Object> genericSetter = docSchema.genericSetter (fieldName, FieldType.TEXT_TYPE);
          genericSetter.setVal (doc, new Text (valueAsStr, lang));
        }
        else {
          TextSetter setter = docSchema.textSetter (fieldName);
          setter.setVal (doc, valueAsStr, lang);
        }
      }

      String labelField = "label";
      if ( Arrays.asList (docSchema.getFieldNames ()).contains (labelField) &&
           docSchema.getFieldTypeByName (labelField) == FieldType.TEXT_TYPE &&
           ! fieldNameValuePairs.keySet ().contains (labelField) ) {

        TextSetter setter = docSchema.textSetter (labelField);
        setter.setVal (doc, inputDocName, lang);

      }
      
//      // Getter and setter for the document text and label
//      TextSetter setDocText = docSchema.textSetter (com.ibm.avatar.api.Constants.DOCTEXT_COL);
//      TextSetter setDocLabel = docSchema.textSetter (com.ibm.avatar.api.Constants.LABEL_COL_NAME);
//
//      // Fill in the document text in our new tuple with the appropriate language
//      setDocText.setVal (doc, this.inputDocText, lang);
//
//      // Assign the document label with the appropriate language
//      setDocLabel.setVal (doc, this.inputDocName, lang);

      String[] outputView = new String[] { viewName };
      // Annotate the document
      Map<String, TupleList> extViewData = getExtViewData (og, params.getJsonDocumentLocation ());
      Map<String, TupleList> annots = og.execute (doc, null, extViewData);

      // Put all results in a hashmap of format: <auto_id, pair<view,
      // tuple>>
      HashMap<Integer, Pair<String, Tuple>> resultMap = new HashMap<Integer, Pair<String, Tuple>> ();

      // Store schema of each view
      HashMap<String, TupleSchema> schemaMap = new HashMap<String, TupleSchema> ();

      int autoID = -1;

      // List of ids that in the output view
      ArrayList<Integer> outputIDs = new ArrayList<Integer> ();

      // For each view, store a list of tuples
      HashMap<String, TupleList> viewResults = new HashMap<String, TupleList> ();

      // Iterate through the outputs, one type at a time.
      int count = 0;
      for (String vName : annots.keySet ()) {

        count++;

        TupleList tups = annots.get (vName);
        viewResults.put (vName, tups);

        AbstractTupleSchema schema = tups.getSchema ();
        schemaMap.put (vName, (TupleSchema) schema);

        // Find which column is the system-generated tuple ID.
        String idString = "";
        for (int i = 0; i < schema.size (); i++) {
          idString = schema.getFieldNameByIx (i);
          if (idString.startsWith (AUTO_ID_COL_NAME)) break;
        }

        if (!idString.startsWith (AUTO_ID_COL_NAME)) {
          // No tuple ID column in this view
          continue;
        }

        FieldGetter<Integer> getInt = schema.intAcc (idString);

        TLIter itr = tups.iterator ();
        while (itr.hasNext ()) {
          Tuple tup = itr.next ();

          Pair<String, Tuple> viewTuple = new Pair<String, Tuple> (vName, tup);

          autoID = getInt.getVal (tup);
          if (vName.equals (viewName)) {
            outputIDs.add (autoID);
          }

          resultMap.put (autoID, viewTuple);
        }
      }

      // Generate the XML to populate our provenance graph
      final String xmlStr = genXML (resultMap, outputIDs, schemaMap);
      if (Constants.DEBUG_PROVENANCE) System.err.println (xmlStr);

      // Generate a label for the Provenance Viewer
      final String partName = viewName + " - " + this.inputDocName;

      // Fix for Defect #16908: ProvenanceView complains about illegal secondary id for :
      // Eclipse uses colon ':' as separator between primary and secondary ID. Therefore secondary IDs containing ':'
      // are illegal. Replace any ':' that may occur in the view name or file name with something else.
      final String secondaryID = partName.replaceAll(":", "");

      // Launch the SWT Provenance Viewer and initialize it with the XML
      // tree we just generated
      Display.getDefault ().asyncExec (new Runnable () {
        @Override
        public void run ()
        {
          IWorkbenchPage wbPage = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ();
          try {
            // TODO : Given below is a work around for Eclipse 4.2.2 bug: 49783. Revisit this when upgrading to higher versions of Eclipse
            //Begin: workaround
            String viewId = secondaryID != null ? ProvenanceView.VIEW_ID+":"+secondaryID : ProvenanceView.VIEW_ID; //$NON-NLS-1$
            IViewReference prevView = wbPage.findViewReference (viewId, secondaryID);
            //End: workaround
            if (prevView != null) {
              // Although the API says "hide", it does in fact
              // close the view
              wbPage.hideView (prevView);
            }
            ((ProvenanceView) wbPage.showView (ProvenanceView.VIEW_ID, secondaryID, IWorkbenchPage.VIEW_ACTIVATE)).setDescription (
              xmlStr, partName, projectName, viewName);

          }
          catch (PartInitException e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
              String.format (Constants.PROVENANCE_VIEW_PROBLEM_FORMAT, partName), e);
          }
          catch (Exception e1) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
              String.format (Constants.PROVENANCE_VIEW_PROBLEM_FORMAT, partName), e1);
          }
        }
      });

    }
    catch (TextAnalyticsException tae) {

      String runtimeMsg  = tae.getMessage ();

      // In case of ModuleLoadException we can get nice detail, for other
      // TextAnalyticsExceptions, just use whatever runtime returns.
      if (tae instanceof ModuleLoadException) {
        String detailBeginStr = "Detailed message: ";  //$NON-NLS-1$    // This string is hard coded in runtime ModuleLoadException.java.
        if (runtimeMsg.contains (detailBeginStr))
          runtimeMsg = runtimeMsg.substring (runtimeMsg.indexOf (detailBeginStr) + detailBeginStr.length ());
      }

      message = String.format (Constants.PROVENANCE_JOB_PROBLEM_FORMAT_WITH_DETAIL, viewName, this.inputDocName, runtimeMsg);
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (message);
      
    }
    catch (Exception e) {

      String infoMsg = Constants.PROVENANCE_INFO_NOT_AVAILABLE;
      Status infoStatus = new Status (IStatus.INFO, Activator.PLUGIN_ID, message, e);
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowInfo (infoMsg);

      message = String.format (Constants.PROVENANCE_JOB_PROBLEM_FORMAT, viewName, this.inputDocName);
      Status status = new Status (IStatus.ERROR, Activator.PLUGIN_ID, message, e);
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logStatus (status);

      return infoStatus;

    }

    return Status.OK_STATUS;

  }

  /**
   * Get project provenance bin + all external tam paths + provenance bin of dependent projects + external tam paths of
   * dependent projects.
   * 
   * @return
   */
  private String getProvenanceTamPaths (String projName)
  {
    StringBuilder ret = new StringBuilder ();

    // Step 1: If provenance is enabled, add .provRewrite/bin to return path
    if (true == ProjectUtils.isProvenanceEnabled (projName)) {
      // Project provenance bin folder
      ret.append (ProjectUtils.getProvenanceBinFolderURI (projName));
      ret.append (Constants.DATAPATH_SEPARATOR);
    }

    // Step 2: Add external tam paths.
    // Call to get all the imported Tams for current project and dependent project.
    Set<String> tamPathSet = null;
    try {
      tamPathSet = getImportedTams (ProjectUtils.getProject (projName), new HashSet<String> ());
    }
    catch (CoreException e) {
      String message = String.format (Constants.PROVENANCE_JOB_PROBLEM_FORMAT, viewName, this.inputDocName);
      Status status = new Status (IStatus.ERROR, Activator.PLUGIN_ID, message, e);
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logStatus (status);

    }
    for (String tamPath : tamPathSet) {
      ret.append (tamPath);
      ret.append (Constants.DATAPATH_SEPARATOR);
    }

    return ret.toString ();
  }

  private Map<String, TupleList> getExtViewData (OperatorGraph og, String jsonDocumentLocation) throws Exception
  {
    if (jsonDocumentLocation == null)
      return null;

    String jsonPath = jsonDocumentLocation.substring (0, jsonDocumentLocation.indexOf ("::"));  // $NON-NLS-1$
    String idxStr = jsonDocumentLocation.substring (jsonDocumentLocation.indexOf ("::") + 2);  // $NON-NLS-1$
    int idx = Integer.parseInt (idxStr);

    // Getting the Map of Tuples and the associated external view data
    Iterator<Pair<Tuple, Map<String, TupleList>>> itr =
          DocReader.makeDocandExternalPairsItr (jsonPath, og.getDocumentSchema (), ProjectUtils.getExternalViewsSchema (og));
    for (int i = 1; itr.hasNext (); i++) {
      if (i == idx)
        return itr.next ().second;
    }

    return null;
  }

  /**
   * Generate the XML input to the provenance viewer applet. This XML includes both the provenance graph and the HTML
   * snippets that are displayed to describe each node in the graph.
   * 
   * @param resultMap map from integer tuple IDs to (view name, tuple) pairs
   * @param outputIDs IDs of tuples that are the roots of the provenance graph
   * @param schemaMap schemas of
   * @return XML representation of the provenance of the indicated output tuples
   */
  private String genXML (HashMap<Integer, Pair<String, Tuple>> resultMap, ArrayList<Integer> outputIDs,
    HashMap<String, TupleSchema> schemaMap)
  {

    StringBuilder sb = new StringBuilder ();
    String xmlHead = //
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"//
      + "<tree>\n" + "<declarations>\n" + "<attributeDecl name=\"" + TUPLE_ID_FIELD_NAME
      + "\" type=\"String\"/>\n"
      + "<attributeDecl name=\"" + TUPLE_TYPE_FIELD_NAME + "\" type=\"String\"/>\n"
      + "<attributeDecl name=\""
      + TUPLE_OPERATION_FIELD_NAME + "\" type=\"String\"/>\n" + "<attributeDecl name=\""
      + TUPLE_VALUE_FIELD_NAME
      + "\" type=\"String\"/>\n" + "</declarations>\n";

    sb.append (xmlHead);

    // The graph viewer can only show tree-shaped graphs. Create an
    // artificial root node so that the graph will be a tree.
    sb.append ("<branch>\n");
    sb.append ("<attribute name=\"" + TUPLE_ID_FIELD_NAME + "\" value=\"" + "All Results" + "\"/>\n"
      + "<attribute name=\"" + TUPLE_TYPE_FIELD_NAME + "\" value=\"" + "Root of all results" + "\"/>\n"
      + "<attribute name=\"" + TUPLE_OPERATION_FIELD_NAME + "\" value=\"" + "Not Applicable" + "\"/>\n"
      + "<attribute name=\"" + TUPLE_VALUE_FIELD_NAME + "\" value=\"" + "Not Applicable" + "\"/>\n");

    // Recursively add the provenance of each output tuple.
    for (Integer id : outputIDs) {
      recursivePrint (id, resultMap, schemaMap, sb);
    }

    // Closing out the root node of the graph
    sb.append ("</branch>\n");
    sb.append ("</tree>");

    return sb.toString ();
  }

  /**
   * Recursively print tuples to an XML file that the provenance GUI can read and display
   * 
   * @param id
   * @param resultMap
   * @param schemaMap
   * @param sb
   */
  @SuppressWarnings({ "unchecked", "unused" })
  private void recursivePrint (int id, HashMap<Integer, Pair<String, Tuple>> resultMap,
    HashMap<String, TupleSchema> schemaMap, StringBuilder sb)
  {
    String vName;
    // 1. print out the content of this tuple and see if it has any source
    // tuple

    Pair<String, Tuple> viewTuple = null;
    String view;
    Tuple tuple;
    TupleSchema schema;
    String[] fieldNames;
    FieldGetter<Integer> getInt;
    FieldGetter<Text> getStr;
    @SuppressWarnings("rawtypes")
    FieldGetter<ScalarList> getList;
    ScalarList<Integer> sList = null;
    String stmtType = "";
    String[] stmtStrings;
    ArrayList<Integer> children = new ArrayList<Integer> ();
    // HashMap<String, String> attValues = new HashMap<String, String>();

    viewTuple = resultMap.get (id);
    if (viewTuple == null)
      return;

    if (id == AQLProvenanceRewriter.DEFAULT_DOC_ID) return;

    view = viewTuple.first;
    tuple = viewTuple.second;

    if (Constants.DEBUG_PROVENANCE)
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (
        String.format ("Tuple %s(%d): %s", view, id, tuple.toCSVString ()));

    schema = schemaMap.get (view);

    fieldNames = schema.getFieldNames ();
    int separatorIdx;

    for (int i = 0; i < fieldNames.length; i++) {

      vName = null;
      separatorIdx = fieldNames[i].indexOf (AQLProvenanceRewriter.AUTO_ID_ATTRIBUTE_VIEW_SEPARATOR);
      if (separatorIdx > -1) vName = fieldNames[i].substring (0, separatorIdx);

      // these are source IDs
      if (fieldNames[i].contains (AQLProvenanceRewriter.STMT_TYPE_ALIAS)) {
        getStr = schema.textAcc (fieldNames[i]);
        stmtType = getStr.getVal (tuple).toString ();
        if (stmtType.contains ("STMT_TYPE_")) {
          stmtStrings = stmtType.split ("STMT_TYPE_");
          stmtType = stmtStrings[1];
          // remove the trailing "'"
          stmtType = stmtType.substring (0, stmtType.length () - 1);
          // attValues.put("STMT_TYPE", stmtType);
        }
      }

      else if ((fieldNames[i].endsWith (SELECT_ID_END) || fieldNames[i].endsWith (UNION_ID_END))
      // && (!fieldNames[i].startsWith("Document_"))
      // && (!fieldNames[i].startsWith("DocScan_"))
      // && (!fieldNames[i].startsWith("Doc_"))
      // && (!fieldNames[i].startsWith("__Base_"))
      // && (!AQLProvenanceRewriter.isBaseView(viewName))
      // TODO: figure out a way to check against external views as well
      ) {

        // handle consolidate
        if (fieldNames[i].startsWith (CONSOLIDATE_START)) {
          getList = schema.scalarListAcc (fieldNames[i]);
          sList = (ScalarList<Integer>) getList.getVal (tuple);
          children.addAll (sList);
        }
        // handle others
        else {
          getInt = schema.intAcc (fieldNames[i]);
          children.add (getInt.getVal (tuple));
        }
      }
    }

    // Don't print the CONSOLIDATE or MINUS temporary views !
    Matcher matchCons = CONSOLIDATE_TEST.matcher (view);
    Matcher matchMinus = MINUS_TEST.matcher (view);
    if (!matchCons.find () && !matchMinus.find ()) {

      if (!children.isEmpty ()) {
        sb.append ("<branch>\n");
      }
      else
        sb.append ("<leaf>\n");

      sb.append (printTupleToXML (id, view, stmtType, tuple, schema));

      if (!children.isEmpty ()) {
        for (Integer childID : children) {
          // if (childID != AQLProvenanceRewriter.DEFAULT_DOC_ID){
          // Log.debug("child id =" + childID);
          recursivePrint (childID, resultMap, schemaMap, sb);
          // }
        }
        sb.append ("</branch>\n");
      }
      else {
        sb.append ("</leaf>\n");
      }
    }
    else {

      // This is a CONSOLIDATE or MINUS temp view: just print its children
      if (!children.isEmpty ()) {
        for (Integer childID : children) {
          // if (childID != AQLProvenanceRewriter.DEFAULT_DOC_ID){
          // Log.debug("child id =" + childID);
          recursivePrint (childID, resultMap, schemaMap, sb);
          // }
        }
      }

    }
  }

  /**
   * Print a result tuple to XML: first print out a brief version to show in the GUI tree; then print detailed fields
   * 
   * @param id
   * @param view
   * @param stmtType
   * @param tuple
   * @param schema
   * @return
   */
  private String printTupleToXML (int id, String view, String stmtType, Tuple tuple, TupleSchema schema)
  {

    String tupleSnip = printTupleBrief (tuple, schema);

    // Laura: reorganized the output
    // return "<attribute name=\"" + TUPLE_ID_FIELD_NAME + "\" value=\"" +
    // tupleSnip + "\"/>\n"
    // + "<attribute name=\"" + TUPLE_TYPE_FIELD_NAME + "\" value=\"" + view
    // + "\"/>\n"
    // + "<attribute name=\"" + TUPLE_OPERATION_FIELD_NAME+ "\" value=\"" +
    // stmtType + "\"/>\n"
    // + "<attribute name=\"" + TUPLE_VALUE_FIELD_NAME + "\" value=\"" +
    // printTupleDetail(tuple, schema) + "\"/>\n";

    // TODO: make more pretty names here
    Matcher match = UNION_TEST.matcher (view);
    if (match.find ()) {
      view = "UnionOp" + match.group (1);
    }

    match = MINUS_TEST.matcher (view);
    if (match.find ()) {
      view = "MinusOp" + match.group (1);
    }

    match = SUBQUERY_TEST.matcher (view);
    if (match.find ()) {
      view = "SubQuery" + match.group (1);
    }

    // Defect 51406: If attribute AQLProvenanceRewriter.DISPLAY_NAME_ALIAS exists, use its value for view name.
    for (int col = 0; col < schema.size (); col++) {
      String name = schema.getFieldNameByIx (col);
      if (name.equals (AQLProvenanceRewriter.DISPLAY_NAME_ALIAS)) {
        String displayViewName = ((Text)schema.getCol (tuple, col)).getText ();
        displayViewName = displayViewName.replaceAll ("<", "&lt;");
        displayViewName = displayViewName.replaceAll (">", "&gt;");
        view = displayViewName;
        break;
      }
    }

    return "<attribute name=\"" + TUPLE_ID_FIELD_NAME + "\" value=\"" + view + "\\n" + tupleSnip + "\"/>\n"
    // + "<attribute name=\"" + TUPLE_TYPE_FIELD_NAME + "\" value=\"" + view
    // + "\"/>\n"
    // + "<attribute name=\"" + TUPLE_OPERATION_FIELD_NAME+ "\" value=\"" +
    // stmtType + "\"/>\n"
    ;
  }

  /**
   * Serialize a short description (attribute names and values) for the input tuple.
   * 
   * @param tuple input tuple
   * @param schema schema of the input tuple
   * @return XML snippet representing a short description of the input tuple (attribute names and values)
   */
  private String printTupleBrief (Tuple tuple, TupleSchema schema)
  {

    StringBuilder sb = new StringBuilder ("");
    // columns not to show in the result
    ArrayList<Integer> skipCol = new ArrayList<Integer> ();
    // sb.append("<table border=\"1\">\n<tr>\n");
    int idx = -1;
    for (int col = 0; col < schema.size (); col++) {
      // FieldType ft = schema.getFieldTypeByIx(col);
      String name = schema.getFieldNameByIx (col);

      if (name.startsWith (AQLProvenanceRewriter.STMT_TYPE_ALIAS) || name.endsWith (SELECT_ID_END)
        || name.endsWith (UNION_ID_END) || name.equals (AQLProvenanceRewriter.CONSOLIDATE_TARGET_ID)
        // Laura: skip the ID also
        || name.startsWith (AUTO_ID_COL_NAME)
        // Defect 51406: skip the special field AQLProvenanceRewriter.DISPLAY_NAME_ALIAS
        || name.equals (AQLProvenanceRewriter.DISPLAY_NAME_ALIAS)) {
        skipCol.add (col);
      }
      else {

        if (name.startsWith (AUTO_ID_COL_NAME)) name = "ID";
        sb.append (name + ": ");
        Object val = schema.getCol (tuple, col);

        String fieldStr;
        if (null == val) {
          fieldStr = Constants.NULL_DISPLAY_VALUE;
        }
        else {
          fieldStr = val.toString ();
          idx = fieldStr.indexOf (":");
          fieldStr = fieldStr.substring (idx + 1);
          fieldStr = fieldStr.trim ();

          // The toString() method for Span and Text objects likes to add
          // surrounding single-quotes. Remove them, if present,
          // because they interfere with the Prefix search mechanism of Prefuse.
          if (fieldStr.length () >= 2 && fieldStr.charAt (0) == '\''
            && fieldStr.charAt (fieldStr.length () - 1) == '\'')
            fieldStr = fieldStr.substring (1, fieldStr.length () - 1);

          if (Constants.DEBUG_PROVENANCE) System.err.printf ("\nWriting: [%s]", fieldStr);
        }

        // The string representation of the field may contain HTML;
        // escape any &, < or > symbols in the string.
        String fieldStrEscaped = escapeHTMLSpecials (fieldStr);
        sb.append (fieldStrEscaped);

        // No (escaped) newline after last element
        if (col != (schema.size () - 1)) {
          sb.append ("\\n");
        }

      }
    }
    // sb.append("&lt;/html&gt;");
    return sb.toString ();
  }

  /**
   * Print all fields of a tuple to XML.
   * 
   * @param tuple input tuple
   * @param schema schema of the input tuple
   * @return XML snippet representing a detailed description of the input tuple.
   */
  @SuppressWarnings("unused")
  private String printTupleDetail (Tuple tuple, TupleSchema schema)
  {
    HtmlViz viz = new HtmlViz (null);
    StringBuilder sb = new StringBuilder ("");
    sb.append ("&lt;table border = &quot;1&quot;&gt;\\n");
    // columns not to show in the result
    ArrayList<Integer> skipCol = new ArrayList<Integer> ();
    // sb.append("<table border=\"1\">\n<tr>\n");
    for (int col = 0; col < schema.size (); col++) {
      FieldType ft = schema.getFieldTypeByIx (col);
      String name = schema.getFieldNameByIx (col);
      if (name.startsWith (AUTO_ID_COL_NAME)) name = "Generated ID";
      if (name.startsWith (AQLProvenanceRewriter.STMT_TYPE_ALIAS) || name.endsWith (SELECT_ID_END)
        || name.endsWith (UNION_ID_END) || name.equals (AQLProvenanceRewriter.CONSOLIDATE_TARGET_ID)
        || name.equals (AQLProvenanceRewriter.DISPLAY_NAME_ALIAS)) {
        skipCol.add (col);
      }
      else {
        sb.append (String.format ("    &lt;th&gt;%s: %s&lt;/th&gt;\\n", name, ft));
      }
    }

    sb.append ("&lt;/tr&gt;\\n");

    sb.append ("&lt;tr&gt;\\n");
    for (int col = 0; col < schema.size (); col++) {
      if (skipCol.contains (col)) continue;
      Object val = schema.getCol (tuple, col);

      String fieldStr;
      if (null == val) {
        fieldStr = Constants.NULL_DISPLAY_VALUE;
      }
      else {
        fieldStr = val.toString ();
      }

      // The string representation of the field may contain HTML;
      // escape any &, < or > symbols in the string.
      String fieldStrEscaped = escapeHTMLSpecials (fieldStr);
      sb.append (String.format ("   &lt;td&gt;%s&lt;/td&gt;\\n", fieldStrEscaped));
    }
    sb.append ("&lt;/tr&gt;\\n");
    sb.append ("&lt;/table&gt;\\n");
    return sb.toString ();
  }

  /**
   * Escape special HTML characters in the input string.
   * @param str
   * @return
   */
  private String escapeHTMLSpecials (String str)
  {
    final boolean debug = false;

    if (0 == str.length ()) { return str; }

    String ret;

    // Need to do the ampersands first
    ret = str.replace ("&", "&amp;");
    ret = ret.replace ("<", "&lt;");
    ret = ret.replace (">", "&gt;");
    ret = ret.replaceAll ("\"", "&quot;");
    ret = ret.replace ("'", "&#39;");

    if (debug) {
      String message = String.format (
        "escapeHTMLSpecials(): Turned:\n" + //
          "'%s'\n" + //
          "    into:\n" + //
          "'%s'", //
        com.ibm.avatar.algebra.util.string.StringUtils.escapeForPrinting (str),
        com.ibm.avatar.algebra.util.string.StringUtils.escapeForPrinting (ret));

      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (message);

    }

    return ret;
  }

  /**
   * Get the external imported TAM URI for a project and its dependent. This makes a recursive calls to go thru the dependency hierarchy
   * to get the all the imported tams.
   * 
   * @param proj
   * @param importedTamURI
   * @return
   * @throws CoreException
   */
  private Set<String> getImportedTams (IProject proj, Set<String> importedTamURI)throws CoreException{
    //Get the tam for current project.
    getTams (proj, importedTamURI);    
    IProject referencedProjectArr[];
    referencedProjectArr = proj.getReferencedProjects ();

    if(referencedProjectArr == null || referencedProjectArr.length == 0)
      return importedTamURI;

    // Iterate thru the project
    for (IProject iProject : referencedProjectArr) {
      getTams (iProject, importedTamURI);      
      getImportedTams (iProject, importedTamURI);
    }
    return importedTamURI;

  }

  /**
   * Get the external tams for a project. This will not get the external tams for the dependent project.
   * @param proj
   * @param importedTamURI
   */
  private void getTams (IProject proj, Set<String> importedTamURI)
  {
    String importedTAMS = ProjectUtils.getImportedTams (proj);
    if (false == StringUtils.isEmpty (importedTAMS)) {
      String tampaths[] = importedTAMS.split (Constants.DATAPATH_SEPARATOR);
      for (String tp : tampaths) {
        if (ProjectUtils.isWorkspaceResource (tp))
          importedTamURI.add (ProjectUtils.getURIsFromWorkspacePaths (tp));
        else
          importedTamURI.add (new File(tp).toURI().toString());
      }
    }
  }
}
