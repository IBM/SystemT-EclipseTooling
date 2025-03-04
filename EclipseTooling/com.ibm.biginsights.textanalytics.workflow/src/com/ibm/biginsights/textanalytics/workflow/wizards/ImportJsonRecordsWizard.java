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
package com.ibm.biginsights.textanalytics.workflow.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.biginsights.textanalytics.resultviewer.model.BoolVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldType;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.FloatVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.IntVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.ListVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.StringVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.workflow.Activator;
import com.ibm.biginsights.textanalytics.workflow.messages.Messages;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

public class ImportJsonRecordsWizard extends Wizard implements IImportWizard
{
  @SuppressWarnings("unused")
private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";
 
	private static final String STRING_TABLE_FIELD = "string_table";    //$NON-NLS-N$
  private static final String BEGIN_OFFSET_FIELD = "begin";           //$NON-NLS-N$
  private static final String END_OFFSET_FIELD = "end";               //$NON-NLS-N$
  private static final String DOC_ID_FIELD = "base_text_ix";          //$NON-NLS-N$
  @SuppressWarnings("unused")
  private static final String SPAN_TEXT_FIELD = "covered_text";       //$NON-NLS-N$

  private IWorkbench           workbench;
  private IStructuredSelection selection;

  private String importFilePath = null;
  private String importTargetProject = null;
  private boolean loadImmediate = false;

  private Serializer srlzr;

  public ImportJsonRecordsWizard ()
  {
    super ();
    setWindowTitle(Messages.import_json_results);
    srlzr = new Serializer();
  }

  @Override
  public void init (IWorkbench workbench, IStructuredSelection selection)
  {
    this.workbench = workbench;
    this.selection = selection;
  }

  @Override
  public boolean performFinish ()
  {
    if (importFilePath == null || importTargetProject == null)
      return false;

    File inJson = FileUtils.createValidatedFile (importFilePath);
    IFolder resultFolder = getResultFolder (inJson.getName ());

    try {
      InputStreamReader inJsonSR = getInputStreamReader(inJson);
      if (inJsonSR == null)
        return false;

      BufferedReader rdr = new BufferedReader (inJsonSR);
      ArrayNode jsonResultArray = null;

      // Read one line to find out what type of output, old or new format.
      String aLine = rdr.readLine (); 

      // If this is new type of result output -- a list of JSON records --
      // continue parsing the file as the new type output.
      if (aLine != null && aLine.trim ().startsWith ("{")) {    //$NON-NLS-1$
        jsonResultArray = new ArrayNode ();

        while (aLine != null) {
          JsonNode obj = JsonNode.parse (aLine);
          jsonResultArray.add (obj);
          aLine = rdr.readLine (); 
        }
      }
      // If this is old type of result output -- an array of JSON records.
      // Re-parse from the beginning as the old type output.
      else {
        inJsonSR = getInputStreamReader(inJson);
        jsonResultArray = ArrayNode.parse (inJsonSR);
      }

      convert2SystemTCompResult (jsonResultArray, resultFolder);
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.import_json_error_reading_input, e);
      return true;
    }

    if (loadImmediate) {
      if (containsResult (resultFolder))
        openInAnnotationExplorer (resultFolder.getName ());
      else {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowInfo (Messages.import_json_info_empty_input);
      }
    }

    return true;
  }

  private InputStreamReader getInputStreamReader (File inFile) throws IOException
  {
    FileInputStream fis = new FileInputStream (inFile);
    String fileName = inFile.getName ();
    InputStreamReader isr = null;

    if (fileName.toLowerCase ().endsWith ("json"))          //$NON-NLS-1$
      isr = new InputStreamReader (fis, "UTF-8");           //$NON-NLS-1$
    else if (fileName.toLowerCase ().endsWith ("json.gz"))  //$NON-NLS-1$
      isr = new InputStreamReader (new GZIPInputStream (fis));

    if (isr == null) {
      fis.close ();
      return null;
    }

    return isr;
  }

  private boolean containsResult (IFolder resultFolder)
  {
    if (resultFolder == null || !resultFolder.exists ())
      return false;

    IResource[] members;
    try {
      members = resultFolder.members ();
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage (), e);
      return false;
    }

    if (members != null && members.length > 0) {
      for (IResource member : members) {
        if (member instanceof IFile) {
          if (Constants.STRF_FILE_EXTENSION.equals (((IFile) member).getFileExtension ()))
            return true;
        }
      }
    }

    return false;
  }

  private IFolder getResultFolder (String fileName)
  {
    IFolder outputFolder = null;
    
    try {
      IProject project = ProjectUtils.getProject (importTargetProject);

      // Get the "result" folder of project, create it if it does not exist.
      IFolder resultFolder = project.getFolder ("result");        //$NON-NLS-N$
      if (!resultFolder.exists ())
        resultFolder.create (true, true, null);

      // Create output folder. It should not exist, if it does we'll suffix it with (2), (3), ...
      String folderName = fileName.substring (0, fileName.length () - 5).trim ();
      outputFolder = resultFolder.getFolder (folderName);
      for (int i = 2; outputFolder.exists (); i++) {
        String newFolderName = folderName + " (" + i + ")";       //$NON-NLS-1$   //$NON-NLS-2$
        outputFolder = resultFolder.getFolder (newFolderName);
      }

      outputFolder.create (true, true, null);
    }
    catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.import_json_error_creating_outFolder, e);
    }

    return outputFolder;
  }

  private void openInAnnotationExplorer (String folderName)
  {
    IWorkbenchWindow wbWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow ();
    IHandlerService handlerService = (IHandlerService)wbWindow.getService (IHandlerService.class);
    ICommandService commandService = (ICommandService)wbWindow.getService (ICommandService.class);

    try {

      ArrayList<Parameterization> parameters = new ArrayList<Parameterization>();
      Command openCommand = commandService.getCommand("com.ibm.biginsights.textanalytics.concordance.commands.showResult");               //$NON-NLS-1$

      IParameter resFolderParam = openCommand.getParameter("com.ibm.biginsights.textanalytics.concordance.commandParam.resultFolder");    //$NON-NLS-1$
      Parameterization viewParmeterization = new Parameterization(resFolderParam, importTargetProject + ":" + folderName );               //$NON-NLS-1$
      parameters.add(viewParmeterization);

      ParameterizedCommand parmCommand = new ParameterizedCommand( openCommand, parameters.toArray(new Parameterization[parameters.size()]));
      handlerService.executeCommand(parmCommand, null);
      
    } catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (Messages.import_json_error_reading_input, e);
    }
  }

  /**
   * The converted result will be save in the project "result" folder, in a folder named
   * after the name of the input result file without extension. eg, if the input result
   * file is 'output-2012-8-21-11-30.json' the result will be in:
   * <pre>&lt;project>\result\output-2012-8-21-11-30\
   */
  private void saveResultToProject (SystemTComputationResult scrModel, IFolder outputFolder)
  {
    IFile outputFile = outputFolder.getFile (scrModel.getDocumentID () + ".strf");     //$NON-NLS-1$
    srlzr.writeModelToFile(outputFile, scrModel);
  }

  private void convert2SystemTCompResult (ArrayNode jsonResultArray, IFolder outputFolder)
  {
    //------------------------------------------------------------------------
    // Create a SystemTComputationResult model for each doc.
    // Each SystemTComputationResult object will be saved to a .strf file
    //------------------------------------------------------------------------

    // 1st pass: create output view objects. The reason we have to do
    // this step in advance is that Eclipse Tools expects the structure
    // of all output views be in every SystemTComputationResult model,
    // but the json result from TA app may not contain it when there are
    // no output results for certain document.
    OutputView[] outputViews = getOutputViews(jsonResultArray);

    // 2nd pass:  For each doc, create SystemTComputationResult model
    // and fill out the output views.
    int base_txt_id = 0;  // this value is used to create a unique id for each doc
    for (int i = 0; i < jsonResultArray.size (); i++) {
      JsonNode results4doc = (JsonNode)jsonResultArray.get (i);

      SystemTComputationResult model = new SystemTComputationResult();
      model.setInputTextID (i);
      model.setDocumentID (String.valueOf (i));
      model.setOutputViews (duplicateOV (outputViews));

      int new_base_txt_id = base_txt_id;
      for (Object elemId : results4doc.keySet () ) {
        String elemName = (String)elemId;

        // string_table [..]
        if (elemName.equals (STRING_TABLE_FIELD)) {
          ArrayNode stringTable = (ArrayNode)results4doc.get (STRING_TABLE_FIELD);
          if (stringTable != null && !stringTable.isEmpty ()) {
            for (int docIdx = 0; docIdx < stringTable.size (); docIdx++) {
              model.addText (new_base_txt_id++, (String)stringTable.get (docIdx));
            }
          }
        }

        // Output view
        else if (results4doc.get (elemName) instanceof ArrayNode) {
          ArrayNode ovResults = (ArrayNode)results4doc.get (elemName);
          processOutputViewResult (elemName, ovResults, model, base_txt_id);
        }
      }
      base_txt_id = new_base_txt_id;

      saveResultToProject (model, outputFolder);
    }
  }

  private OutputView[] duplicateOV (OutputView[] outputViews)
  {
    OutputView[] newOVs = new OutputView[outputViews.length];

    for (int i = 0; i < outputViews.length; i++) {
      newOVs [i] = new OutputView (outputViews[i].getName (), outputViews[i].getFieldNames (), outputViews[i].getFieldTypes ());
    }

    return newOVs;
  }

  private OutputView[] getOutputViews (ArrayNode jsonResultArray)
  {
    List<String> allOVNames = new ArrayList<String> ();
    List<OutputView> allOVs = new ArrayList<OutputView> ();

    for (int i = 0; i < jsonResultArray.size (); i++) {
      JsonNode results4doc = (JsonNode)jsonResultArray.get (i);

      //----------------------------------------------------------
      // A Json result object corresponding to a document contains
      //   1. document (fields of the document) -- we don't need
      //      about these info
      //   2. results of each output view -- an array of records,
      //      each record has following structure:
      //      { field1 : value1, field2 : value2, ... }
      //   3. an array named string_table that keeps an
      //      indexed list of texts used in spans.
      //----------------------------------------------------------
      for (Object elemId : results4doc.keySet () ) {
        String elemName = (String)elemId;

        // If it is a new output view
        if ( !elemName.equals (STRING_TABLE_FIELD) &&
             results4doc.get (elemName) instanceof ArrayNode &&
             !allOVNames.contains (elemName) ) {

          // we know elemName is the name of an output view; however, if
          // there is no output we can't get the view structure.
          ArrayNode ovResults = (ArrayNode)results4doc.get (elemName);
          if (ovResults.size () > 0) {
            JsonNode jsonRecord = (JsonNode) ovResults.get (0);
            List<String> fieldNameList = new ArrayList<String> ();
            List<FieldType> fieldTypeList = new ArrayList<FieldType> ();

            // Loop thru each field of the result
            for (Object fn : jsonRecord.keySet ()) {
              String fieldName = (String) fn;

              FieldType fieldType = null;
              Object fv = jsonRecord.get (fn);

              // JSON value is a JSON-able object which is one of : String, Boolean, Number, JsonNode, ArrayNode
              // Based on the value, the field type will be STRING, BOOL, INT or FLOAT, SPAN, LIST.
              if (fv instanceof String)
                fieldType = FieldType.STRING;
              else if (fv instanceof Boolean)
                fieldType = FieldType.BOOL;
              else if (fv instanceof Integer || fv instanceof Long)
                  fieldType = FieldType.INT;
              else if (fv instanceof Float || fv instanceof Double)
                 fieldType = FieldType.FLOAT;
              else if (fv instanceof JsonNode)
                fieldType = FieldType.SPAN;
              else if (fv instanceof ArrayNode)
                fieldType = FieldType.LIST;

              if (fieldType != null) {
                fieldNameList.add (fieldName);
                fieldTypeList.add (fieldType);
              }
            }

            // create the OutputView object
            OutputView ov = new OutputView (elemName);
            ov.setFieldNames (fieldNameList.toArray (new String[0]));
            ov.setFieldTypes (fieldTypeList.toArray (new FieldType[0]));
            allOVNames.add (elemName);
            allOVs.add (ov);
          }
        }
      }
    }

    // Sort the output views. If not, jumping from annotation explorer
    // to table view may load the wrong output view content.
    Collections.sort (allOVs, new OVComparator ());

    return allOVs.toArray (new OutputView[0]);
  }

  /****************************************************************
   * Output view results = an array of records, each of them has
   * following structure: { field1 : value1, field2 : value2, ... }
   ****************************************************************/
  private void processOutputViewResult (String ovName, ArrayNode ovResults, SystemTComputationResult model, int base_txt_id)
  {
    if (ovResults == null || ovResults.size () == 0)
      return;

    // Find output view in SystemTComputationResult object
    OutputView outputView = null;
    for (OutputView ov : model.getOutputViews ()) {
      if (ov.getName ().equals (ovName)) {
        outputView = ov;
        break;
      }
    }
    if (outputView == null)
      return;

    List<OutputViewRow> ovRowList = new ArrayList<OutputViewRow> ();

    // Loop thru each output view result
    for (int i = 0; i < ovResults.size (); i++) {
      JsonNode jsonRecord = (JsonNode) ovResults.get (i);
      OutputViewRow row = new OutputViewRow();

      // Loop thru each field of the result
      List<FieldValue> fieldValueList = new ArrayList<FieldValue> ();
      for (Object fn : jsonRecord.keySet ()) {
        Object fv = jsonRecord.get (fn);
        FieldValue fieldValue = getFieldValue (base_txt_id, fv);

        if (fieldValue != null)
          fieldValueList.add (fieldValue);
      }

      row.fieldValues = fieldValueList.toArray (new FieldValue[0]);
      ovRowList.add (row);
    }

    // populate the OutputView object
    outputView.setRows (ovRowList.toArray (new OutputViewRow[0]));
  }

  private FieldValue getFieldValue (int base_txt_id, Object fv)
  {
    FieldValue fieldValue = null;

    // JSON value is a JSON-able object which is one of : String, Boolean, Number, JsonNode, ArrayNode
    // Based on the value, the field type will be STRING, BOOL, INT or FLOAT, SPAN, LIST.
    if (fv instanceof String) {
      fieldValue = new StringVal ((String)fv);
    }
    else if (fv instanceof Boolean) {
      fieldValue = new BoolVal ((Boolean)fv);
    }
    else if (fv instanceof Number) {
      Number numberValue = (Number)fv;
      if (fv instanceof Integer || fv instanceof Long) {
        fieldValue = new IntVal (numberValue.intValue ());
      }
      else if (fv instanceof Float || fv instanceof Double) {
        fieldValue = new FloatVal (numberValue.floatValue ());
      }
    }
    else if (fv instanceof JsonNode) {
      JsonNode spanValue = (JsonNode) fv;
      Long begin = (Long)spanValue.get (BEGIN_OFFSET_FIELD);
      Long end = (Long)spanValue.get (END_OFFSET_FIELD);
      Long srcId = (Long)spanValue.get (DOC_ID_FIELD) + base_txt_id;
      fieldValue = new SpanVal (begin.intValue (), end.intValue (), srcId.intValue ());
      ((SpanVal)fieldValue).parentSpanName = "Document.text"; // Hard code for now        //$NON-NLS-N$
    }
    else if (fv instanceof ArrayNode) {
      ArrayNode fvArray = (ArrayNode)fv;

      fieldValue = new ListVal();
      List<FieldValue> fvList = new ArrayList<FieldValue> ();

      for (Object elem : fvArray) {
        FieldValue subFieldValue = getFieldValue (base_txt_id, elem);
        fvList.add (subFieldValue);
      }

      ((ListVal)fieldValue).values = fvList;
    }

    return fieldValue;
  }

  @Override
  public void addPages ()
  {
    super.addPages ();
    addPage (new ImportJsonRecordsWizardPage1(Messages.import_json_import_results));
  }

  public String getImportFilePath ()
  {
    return importFilePath;
  }

  public void setImportFilePath (String importFilePath)
  {
    this.importFilePath = importFilePath;
  }

  public String getImportTargetProject ()
  {
    return importTargetProject;
  }

  public void setImportTargetProject (String importTargetProject)
  {
    this.importTargetProject = importTargetProject;
  }

  public boolean isLoadImmediate ()
  {
    return loadImmediate;
  }

  public void setLoadImmediate (boolean loadImmediate)
  {
    this.loadImmediate = loadImmediate;
  }

  public IWorkbench getWorkbench ()
  {
    return workbench;
  }

  public void setWorkbench (IWorkbench workbench)
  {
    this.workbench = workbench;
  }

  public IStructuredSelection getSelection ()
  {
    return selection;
  }

  public void setSelection (IStructuredSelection selection)
  {
    this.selection = selection;
  }

  public class OVComparator implements Comparator<OutputView> {
    public int compare(OutputView ov1, OutputView ov2) {
      return ov1.getName ().compareTo (ov2.getName ());
    }
  }
}
