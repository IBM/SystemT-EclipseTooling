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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.ibm.avatar.algebra.datamodel.FieldType;
import com.ibm.avatar.algebra.datamodel.Tuple;
import com.ibm.avatar.algebra.datamodel.TupleList;
import com.ibm.avatar.algebra.datamodel.TupleSchema;
import com.ibm.avatar.api.DocReader;
import com.ibm.avatar.api.OperatorGraph;
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.api.tam.ViewMetadata;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.util.PaginationTracker;
import com.ibm.biginsights.textanalytics.resultviewer.utils.AnnotationExplorerUtil;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * DocumentAnnotator is an abstract base class for all types of document annotators. This class defines the overall
 * strategy for annotating any type of data collection supported by InfoSphere BigInsights Text Analytics. Subclasses of
 * this class override certain abstract template methods of this class to define behavior specific to a document format.
 * 
 *  Kalakuntla
 */
public abstract class DocumentAnnotator
{



  // Fields required to implement the annotating
  protected String filesToAnalyze = null;
  // Utility to write result models to disk
  protected final Serializer modelSerializer = new Serializer ();
  protected boolean firstPageDisplayed = false;
  protected boolean isModularProject = false;
  protected String[] selectedModules;
  protected final boolean debug = false;

  protected Map<String, Integer> filesWithSameName;
  protected Map<String, Integer> labelsWithSameName;
  protected ProvenanceRunParams provenanceRunParams;
  protected IFolder resultFolder;
  protected String tempDir;

  protected IProject project;
  protected IWorkspaceRoot workspaceRoot;
  protected ArrayList<String> strfFilesThatExist = new ArrayList<String> ();

  protected DocReader docReader; // To hold the reference to input document collection.
  protected OperatorGraph operatorGraph;

  /**
   * Constructor to instantiate the DocumentAnnotator which is being called from one of the derived classes.
   * 
   * @param project project where the aql extractor
   * @param runConfig run configurations for annotating the input document collection
   * @param filesToAnalyze string representation of the input document collection path
   * @param resultFolder folder to store the result files
   * @param tempDir folder to store the entries displayed in the annotation explorer
   * @param provenanceRunParams Information used for building provenance graph.
   */
  protected DocumentAnnotator (IProject project, SystemTRunConfig runConfig, String filesToAnalyze, IFolder resultFolder, String tempDir, ProvenanceRunParams provenanceRunParams)
  {
    this.project = project;
    this.filesToAnalyze = filesToAnalyze;
    this.resultFolder = resultFolder;

    this.selectedModules = runConfig.getSelectedModules ().split (Constants.DATAPATH_SEPARATOR);
    this.workspaceRoot = project.getWorkspace ().getRoot ();
    this.isModularProject = ProjectUtils.isModularProject (project);

    this.filesWithSameName = new HashMap<String, Integer> ();
    this.labelsWithSameName = new HashMap<String, Integer> ();

    this.tempDir = tempDir;
    this.provenanceRunParams = provenanceRunParams;
  }

  /**
   * This method is to create the result model for storing/persisting the annotations for input document collection
   * which includes text, csv and json files.
   * 
   * @param docs a DocReader instance for a input document collection
   * @param encodedLabel label to represent the input documents
   * @param docTuple input document tuple
   * @param docLevelAnnots documentLevel annotations for input document collection
   * @param outputViewOriginalNameMap A mapping of output view aliases to their actual view names.
   * @return The model. This can be serialized to disk using the serializer class.
   */
  abstract protected SystemTComputationResult createResultModel (DocReader docs, String encodedLabel, Tuple docTuple,
    Map<String, TupleList> docLevelAnnots, Map<String, String> outputViewOriginalNameMap);

  /**
   * <p>
   * In annotation explorer and result table view, we would like to indicate to the aql developer, the input document
   * from which the annotation originated. We do this by Serializing the annotations from a doc tuple into an strf
   * file bearing the input document name as a prefix. 
   * </p>
   * <p>
   * In the case of CVS or JSON files, document name is the input document name followed by row id as a prefix.
   * As we can specify only a single file as input, we can use the input file name as document name.
   * </p>
   * <p>
   * A text document collection can be individual text files or archives(gz/zip) containing text files. The only way
   * to determine input document name is from the label field of a tuple when the schema of the input 
   * document is [text Text, label Text] (the default schema). If, however, the input document schema was
   * [text Text], the label for the tuple would be null. So to get the input document name in this case too,
   * we create a 'reference' DocReader instance with the default schema, and (assuming this instance will
   * return tuples in the same order) read the labels from its tuples.
   * </p>
   * 
   * This method is assumed to be called in sync with next() in actual docReader instance.
   * 
   * @param filesToAnalyze File containing the input document collection, in one of the supported formats.
   * @param tupleIndex represents the current tuple being processed within the input document document collection
   * @return returns the runtime label for document collection.
   * @throws TextAnalyticsException
   */
  abstract protected String getDocLabel (String filesToAnalyze, int tupleIndex) throws TextAnalyticsException;

  /**
   * This method is to get the external view data for input document collection. As we support external views only for
   * json documents we will override this behavior for json files. for text and csv files this will be null
   * 
   * @param docs a DocReader instance for a input document collection
   * @return a Map of String to TupleList containing the values of the external view tuples in this document collection
   */
  protected Map<String, TupleList> getExtViewData (DocReader docs)
  {
    return null;
  }

  /**
   * This method annotates input document collections e.g. text files, xml files, html files, del files, zip files, tar
   * files, csv files, json files and directories.
   * 
   * @param operatorGraph Operator graph for the extractor
   * @param filesToIgnore path of the files to be ignored while annotating the documents mentioned in SystemTRunConfig
   * @param monitor to monitor the progress of the job.
   * @return returns true if the annotator produced one or more output view rows, returns false otherwise.
   * @throws Exception
   */
  public boolean annotateDocs (OperatorGraph operatorGraph, List<String> filesToIgnore, IProgressMonitor monitor) throws Exception
  {
    // get the instance of the doc Reader
    DocReader docs = this.docReader;

    Map<String, String> outputViewOriginalNameMap = getOutputViewAliasOriginalNameMap (operatorGraph);

    long annotateElapsedMS = 0;
    long serializeElapsedMS = 0;
    int ndocs = 0;
    int ntups = 0;
    int tupleIndex = 0;
    int filesPerPage = PaginationTracker.getInstance ().getFilesPerPageCount ();
    int strfFiles = 0; // counter for number of strf files being added. Will be considered as current number of files in
                       // the result folder
    Map<String, TupleList> extViewData = null;
    String jsonPath = filesToAnalyze; // Applicable only for JSON input document collection...
    boolean containsAnnotations = false;
    
    //Cache for holding results to be displayed in first page.
    List <SystemTComputationResult> firstPageResults = new ArrayList<SystemTComputationResult>();
    
    while (docs.hasNext ()) {
      tupleIndex++;
      final Tuple docTuple = docs.next ();
      extViewData = getExtViewData (docs);
      String labelFromRuntime = getDocLabel (getFilesToAnalyze (), tupleIndex);
      String encodedLabel = getEncodedLabel (labelFromRuntime);
      // If the operation is canceled then logging and displaying will be taken care by the calling run() method.
      if (!monitor.isCanceled ()) {
        monitor.subTask (Messages.getString ("SystemTLaunchConfigurationDelegate.ANNOTATE")); //$NON-NLS-1$
      }
      if (filesToIgnore != null && filesToIgnore.contains (labelFromRuntime)) {
        // just ignore this file
      }
      else {
        ndocs++;
        // annotate each document individually
        long annotateDocStartMS = System.currentTimeMillis ();
        Map<String, TupleList> docLevelAnnots = null;

        docLevelAnnots = operatorGraph.execute (docTuple, null, extViewData);
        annotateElapsedMS += System.currentTimeMillis () - annotateDocStartMS;
        
        if (debug) { //ntups is used only during stats collection. Don't calculate otherwise.
          //Count the number of result tuples
          for (TupleList tlist : docLevelAnnots.values ()) {
            ntups += tlist.size ();
          }
        }
        
        long serializeDocStartMS = System.currentTimeMillis ();

        int annotationsForThisDoc = 0;
        for (TupleList tlist : docLevelAnnots.values ()) {
          annotationsForThisDoc += tlist.size ();
        }
        // If there are no annotations - do not serialize - This is to take care of unnecessary time taken for
        // serializing which is not useful to the user anyway. Difference is seen esp in case of large input collections
        if (annotationsForThisDoc == 0) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (
            Messages.getString ("SystemtRunJob.INFO_NOT_SRLZING") + encodedLabel); //$NON-NLS-1$
        }
        else {
          SystemTComputationResult model = createResultModel (docs, encodedLabel, docTuple, docLevelAnnots,
            outputViewOriginalNameMap);

          if (extViewData != null) model.setJsonDocumentLocation (jsonPath + "::" + tupleIndex); // This is only
                                                                                                 // required for JSON
                                                                                                 // Inputs

          IFile resFile = getFileForID (encodedLabel);
          modelSerializer.writeModelToFile (resFile, model);
          strfFiles++; // When we reach this point, we can assume an strf file has been added.
          // This way, we can keep a count of number of files in result folder without depending on IFolder.members()
          // method,
          // and reduce memory consumption. Refer defect 33744.
          
          if (strfFiles <= filesPerPage) {
            firstPageResults.add (model); //add model to cache
          }
          
          if (strfFiles == filesPerPage) {
            // display the first page if the serialized results size satisfies the page size
            firstPageDisplayed = AnnotationExplorerUtil.displayFirstPage (project, resultFolder, tempDir, provenanceRunParams, firstPageResults, strfFiles); //display using cached results
          }
          if (firstPageDisplayed && filesPerPage > 0) {
            // Only if at least the first page has been displayed, refresh it to display the latest calculated value of
            // "total number of pages"
            if ((strfFiles - 1) % filesPerPage == 0) { // intent is to reduce no. of refreshes.
                                                       // refresh only when a new page is added.
              AnnotationExplorerUtil.updatePageDescription (resultFolder, strfFiles);
            }
          }
          if (model.getOutputViews ().length > 0) {
            containsAnnotations = true;
          }
          
          //Discard computation results beyond first page
          //after they have been serialized.
          if (strfFiles > filesPerPage) {
            model.clear ();
            model = null;
          }
          resFile = null;
        }

        // release doc level tuples
        for (final String opViewName : docLevelAnnots.keySet ()) {
          final TupleList docLevelTupleList = docLevelAnnots.get (opViewName);
          docLevelTupleList.clear ();
        }

        // Update the serialization running time
        serializeElapsedMS += System.currentTimeMillis () - serializeDocStartMS;
      }
      monitor.worked (1);
    } // end of while docs.hasNext()
    if (debug) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (
        this.printStats (Messages.getString ("SystemtRunJob.INFO_ANNOT_STATS"), ndocs, ntups, annotateElapsedMS)); //$NON-NLS-1$
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logInfo (
        this.printStats (Messages.getString ("SystemtRunJob.INFO_SRLZATION_STATS"), ndocs, ntups, serializeElapsedMS)); //$NON-NLS-1$
    }
    return containsAnnotations;
  }

  public static void main (String[] args)
  {
    try {
      for (int i = 0; i < 100; i++) {
        Thread.sleep (1000);
        System.out.println ("i = " + i);
      }
      System.out.println ("=====> HELLO WORLD <=====");
      for (int i = 0; i < 100; i++) {
        Thread.sleep (1000);
        System.out.println ("i = " + i);
      }
    }
    catch (InterruptedException e) {
      e.printStackTrace ();
    }
  }

  /**
   * Using the provided operator graph, uses module metadata to find the actual view name of the output views listed in
   * the modules. The output views listed in metadata may be using aliases. Returns an empty hashmap for non-modular
   * aql.
   * 
   * @param og
   * @return Map with the key - output view name as listed in module metadata, value - actual qualified view name. If
   *         module name is not available in metadata, key and value would be the same.
   */
  protected Map<String, String> getOutputViewAliasOriginalNameMap (OperatorGraph og)
  {
    Map<String, String> outputViewOriginalNameMap = new HashMap<String, String> ();
    if (isModularProject) {
      for (String mod : selectedModules) {
        try {
          ModuleMetadata md = og.getModuleMetadata (mod);
          String[] outputViews = md.getOutputViews ();
          for (String outputView : outputViews) {
            ViewMetadata vmd = md.getViewMetadata (outputView);
            String moduleName = vmd.getModuleName ();
            String viewName = vmd.getViewName ();
            if (moduleName == null || moduleName.isEmpty ()) {
              outputViewOriginalNameMap.put (outputView, viewName);
            }
            else {
              outputViewOriginalNameMap.put (outputView, moduleName + "." + viewName);
            }
          }
        }
        catch (TextAnalyticsException e) {

        }
      }
    }
    return outputViewOriginalNameMap;
  }

  /*
   * This method is to return the encoded label for the input document which takes the input document label and encodes
   * it.
   */
  protected String getEncodedLabel (String labelFromRuntime)
  {
    String encodedLabel = StringUtils.normalizeSpecialChars (labelFromRuntime);

    encodedLabel = StringUtils.truncatePath (encodedLabel);

    Integer fileCount = filesWithSameName.get (encodedLabel);
    if (fileCount != null) {
      fileCount = fileCount + 1;
      filesWithSameName.put (encodedLabel, fileCount);
      encodedLabel = encodedLabel + "(" + fileCount.toString () + ")";//$NON-NLS-1$ //$NON-NLS-2$

    }
    else {
      filesWithSameName.put (encodedLabel, new Integer (1));
    }

    Integer labelCount = labelsWithSameName.get (labelFromRuntime);
    if (labelCount != null) {
      labelCount = labelCount + 1;
      labelsWithSameName.put (labelFromRuntime, labelCount);
      labelFromRuntime = labelFromRuntime + "(" + labelCount.toString () + ")"; //$NON-NLS-1$ //$NON-NLS-2$

    }
    else {
      labelsWithSameName.put (labelFromRuntime, new Integer (1));
    }
    // label=relativeDocPath;
    return encodedLabel;
  }

  protected final IFile getFileForID (String fileID)
  {
    // TODO: make this more robust (tg)
    final IFile file = resultFolder.getFile (new Path (fileID + ".strf")); //$NON-NLS-1$
    if (file.exists ()) {
      strfFilesThatExist.add (fileID);
    }
    return file;
  }

  

  protected String printStats (String msg, int ndoc, long nchar, long ntups, long elapsedMs)
  {
    double elapsedSec = elapsedMs / 1000.0;
    double docPerSec = ndoc / elapsedSec;
    double kcharPerSec = nchar / elapsedSec / 1024.0;
    double numTupsPerDoc = (double) ntups / ndoc;

    return String.format (Messages.getString ("SystemtRunJob.INFO_DOCS_IN") //$NON-NLS-1$
      + Messages.getString ("SystemtRunJob.INFO_KCHAR_TUPLES"), msg, ndoc, elapsedSec, docPerSec, //$NON-NLS-1$
      kcharPerSec, numTupsPerDoc);
  }

  protected String printStats (String msg, int ndoc, long ntups, long elapsedMs)
  {
    double elapsedSec = elapsedMs / 1000.0;
    double docPerSec = ndoc / elapsedSec;
    double numTupsPerDoc = (double) ntups / ndoc;

    return String.format (Messages.getString ("SystemtRunJob.INFO_DOCS_IN") //$NON-NLS-1$
      + Messages.getString ("SystemtRunJob.INFO_KCHAR_TUPLES"), msg, ndoc, elapsedSec, docPerSec, docPerSec, numTupsPerDoc);
  }

  

  protected String getFilesToAnalyze ()
  {
    return filesToAnalyze;
  }

  protected void setFilesToAnalyze (String filesToAnalyze)
  {
    this.filesToAnalyze = filesToAnalyze;
  }

  /**
   * Retrieves the value of specified field from the given docTuple.
   * 
   * @param docTuple The tuple from where field value is to be retrieved
   * @param docSchema Schema of the docTuple. This is required to fetch field name corresponding to a given index.
   * @param fieldIx Index of the field whose value is to be retrieved.
   * @return value of the specified field, as found in the given docTuple
   */
  protected Object getFieldValue (Tuple docTuple, TupleSchema docSchema, int fieldIx)
  {
    FieldType fieldType = docSchema.getFieldTypeByIx (fieldIx);
    String fieldName = docSchema.getFieldNameByIx (fieldIx);

    return docSchema.genericGetter (fieldName, fieldType).getVal (docTuple);
  }

}
