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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

import com.ibm.avatar.algebra.datamodel.FieldGetter;
import com.ibm.avatar.algebra.datamodel.Text;
import com.ibm.avatar.algebra.datamodel.Tuple;
import com.ibm.avatar.algebra.datamodel.TupleList;
import com.ibm.avatar.algebra.datamodel.TupleSchema;
import com.ibm.avatar.algebra.exceptions.FieldNotFoundException;
import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.avatar.api.Constants;
import com.ibm.avatar.api.DocReader;
import com.ibm.avatar.api.OperatorGraph;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.biginsights.textanalytics.resultviewer.model.ModelConverter;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;

/**
 * TextDocAnnotator is default annotator sub class to annotate text documents which may include all non-csv and non-json
 * documents ex: text file, del files, html files ect. It implement the specific behavior getting labels and creating
 * result model for text document collection.
 * 
 *  Kalakuntla
 */
public class TextDocAnnotator extends DocumentAnnotator
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
   * Holds a DocReader instance with default schema [text Text, label Text]. It is used in conjunction with docReader
   * instance having user specified schema, to retrieve the document label.
   */
  private DocReader referenceDocReader;

  /**
   * Constructor to instantiate the Text Document annotator class, This in turn calls the constructor of
   * DocumentAnnotator.
   * @param project project where the aql extractor
   * @param runConfig run configurations for annotating the input document collection
   * @param operatorGraph Operator Graph instance
   * @param filesToAnalyze string representation of the input document collection path
   * @param resultFolder folder to store the result files
   * @param tempDir folder to store the entries displayed in the annotation explorer
   * @param lang Supported language
   * @param provenanceRunParams Information used for building provenance graph.
   * @throws TextAnalyticsException
   */
  public TextDocAnnotator (IProject project, SystemTRunConfig runConfig, OperatorGraph operatorGraph,
    String filesToAnalyze, IFolder resultFolder, String tempDir, LangCode lang, ProvenanceRunParams provenanceRunParams) throws TextAnalyticsException
  {
    super (project, runConfig, filesToAnalyze, resultFolder, tempDir, provenanceRunParams);
    this.operatorGraph = operatorGraph;
    this.docReader = new DocReader (FileUtils.createValidatedFile (filesToAnalyze), operatorGraph.getDocumentSchema (), null);
    this.docReader.overrideLanguage (lang);
    this.referenceDocReader = new DocReader(FileUtils.createValidatedFile(filesToAnalyze)); //create a referenced doc reader with default schema
  }

  @Override
  protected SystemTComputationResult createResultModel (DocReader docs, String encodedLabel, Tuple docTuple,
    Map<String, TupleList> docLevelAnnots, Map<String, String> outputViewOriginalNameMap)
  {
    SystemTComputationResult resultModel = null;
    Set<Object> allFieldSpanSet = new HashSet<Object> ();

    // obtain value for the field named 'text'
    TupleSchema docSchema = docs.getDocSchema ();
    FieldGetter<Text> textAcc = docSchema.textAcc (Constants.DOCTEXT_COL);
    Text value = textAcc.getVal (docTuple);
    allFieldSpanSet.add (value);

    // prepare a model for STRF file
    resultModel = ModelConverter.createModel (encodedLabel, allFieldSpanSet, docLevelAnnots, outputViewOriginalNameMap);
    return resultModel;
  }

  @Override
  protected String getDocLabel (String fileToAnalyze, int tupleIndex) throws TextAnalyticsException
  {
    final Tuple refTuple = referenceDocReader.next ();
    
    TupleSchema docSchema = referenceDocReader.getDocSchema ();

    try {
      String fieldName = com.ibm.avatar.api.Constants.LABEL_COL_NAME;
      int ix = docSchema.nameToIx (fieldName);
      if (true == docSchema.getFieldTypeByIx (ix).getIsText ()) {
        FieldGetter<Text> fg = docSchema.textAcc (fieldName);
        return fg.getVal (refTuple).getText ();
      }
    }
    catch (FieldNotFoundException fnfe) {
      return String.valueOf (tupleIndex);
    }
    return String.valueOf (tupleIndex);
  }

}
