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

package com.ibm.biginsights.textanalytics.nature.run;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import com.ibm.avatar.algebra.datamodel.Tuple;
import com.ibm.avatar.algebra.datamodel.TupleList;
import com.ibm.avatar.algebra.datamodel.TupleSchema;
import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.api.DocReader;
import com.ibm.avatar.api.OperatorGraph;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.biginsights.textanalytics.resultviewer.model.ModelConverter;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * CsvDocAnnotator is a sub class to annotate CSV documents with headers. It implement the specific behavior getting
 * labels and creating result model for csv document collection.
 * 
 *  Kalakuntla
 */
public class JsonDocAnnotator extends DocumentAnnotator
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

  /**
   * Constructor to instantiate the JSON Document annotator class, This in turn calls the constructor of
   * DocumentAnnotator.
   * @param project project where the aql extractor
   * @param runConfig run configurations for annotating the input document collection
   * @param operatorGraph Operator Graph instance
   * @param filesToAnalyze string representation of the input document collection path
   * @param resultFolder folder to store the result files
   * @param tempDir folder to store the entries displayed in the annotation explorer
   * @param lang Supported language
   * @param provenanceRunParams Information used for building provenance graph.
   * @throws Exception
   * @throws TextAnalyticsException
   */
  public JsonDocAnnotator (IProject project, SystemTRunConfig runConfig, OperatorGraph operatorGraph,
    String filesToAnalyze, IFolder resultFolder, String tempDir, LangCode lang, ProvenanceRunParams provenanceRunParams) throws TextAnalyticsException, Exception
  {
    super (project, runConfig, filesToAnalyze, resultFolder, tempDir, provenanceRunParams);
    super.operatorGraph = operatorGraph;
    super.docReader = new DocReader (FileUtils.createValidatedFile (filesToAnalyze), operatorGraph.getDocumentSchema (),
      ProjectUtils.getExternalViewsSchema (operatorGraph));
    super.docReader.overrideLanguage (lang);
  }

  @Override
  protected SystemTComputationResult createResultModel (DocReader docs, String encodedLabel, Tuple docTuple,
    Map<String, TupleList> docLevelAnnots, Map<String, String> outputViewOriginalNameMap)
  {
    SystemTComputationResult resultModel = null;
    // Adding all the field Name objects such that they can be serialized
    String[] allFieldNames = docs.getDocSchemaFields ();
    Object[] allFieldSpans = new Object[allFieldNames.length];

    // fetch values of fields
    TupleSchema docSchema = docs.getDocSchema ();
    for (int i = 0; i < allFieldNames.length; i++) {
      allFieldSpans[i] = getFieldValue (docTuple, docSchema, i);
    }

    // prepare a model for STRF file
    resultModel = ModelConverter.createModelForJsonOrCsv (encodedLabel, allFieldNames, allFieldSpans, docLevelAnnots,
      outputViewOriginalNameMap);

    return resultModel;
  }

  @Override
  protected String getDocLabel (String filesToAnalyze, int tupleIndex) throws TextAnalyticsException
  {
    String jsonFName = new File (filesToAnalyze).getName ();
    String labelForJsonFiles = jsonFName + "_" + tupleIndex;
    return labelForJsonFiles;
  }

  /**
   * This method is overridden to get the external view data associated with JSON input documents.
   */
  @Override
  protected Map<String, TupleList> getExtViewData (DocReader docs)
  {
    Map<String, TupleList> extViewData = docs.getExtViewTups ();
    return extViewData;
  }

}
