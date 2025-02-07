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

import static java.lang.Boolean.TRUE;
import static com.ibm.biginsights.textanalytics.resultviewer.model.FieldType.SPAN;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModelEntry;
import com.ibm.biginsights.textanalytics.concordance.model.IFiles;
import com.ibm.biginsights.textanalytics.concordance.model.IStringFilter;
import com.ibm.biginsights.textanalytics.concordance.model.ITypes;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldType;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.runtime.util.ProvenanceRunParams;
import com.ibm.biginsights.textanalytics.tableview.model.IAQLTableViewModel;
import com.ibm.biginsights.textanalytics.tableview.model.IRow;
import com.ibm.biginsights.textanalytics.tableview.model.impl.AQLTableViewModel;
import com.ibm.biginsights.textanalytics.tableview.model.impl.Row;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * Data model for concordance view
 */
public class ConcordanceModel implements IConcordanceModel {


	
	private static boolean debug = false;

  public static final int CTX_SIZE = 50;

  private final Types modelTypes = new Types();

  // Cache the names of span modelTypes. Span type names are <ViewName>.<FieldName>
  String[][] typeNames;

  private final List<SystemTComputationResult> models;

  public List<SystemTComputationResult> getSTCRModels() {
	return models;
}

private final String tempDirPath;

  private ProvenanceRunParams provenanceParams = null;

  private Map<String,String> outputViewNames = new LinkedHashMap<String,String>(); //Map is of the form <output view alias,actual qualified view name>

  private final Files files = new Files();

  private final Entry[] entries;

  private static final IStringFilter EMPTY_STRING_FILTER = new StringFilter();

  private IStringFilter annotationTextFilter = ConcordanceModel.EMPTY_STRING_FILTER;

  private IStringFilter leftContextFilter = ConcordanceModel.EMPTY_STRING_FILTER;

  private IStringFilter rightContextFilter = ConcordanceModel.EMPTY_STRING_FILTER;

  private IProject project;
  
  /**
   * Constructor. 
   * Accepts the list of annotation result models and uses them to get the necessary information
   * for concordance view.
   * @param tempDirPath Path of temporary directory to be used by AQLTreeView and editor
   * @param provParams Provenance parameters
   * @param models List of annotation result models 
   * @param monitor Progress monitor instance
   */
  public ConcordanceModel(List <SystemTComputationResult> models, String tempDirPath, ProvenanceRunParams provParams, 
      IProgressMonitor monitor) {
    super();
    this.tempDirPath = tempDirPath;
    this.provenanceParams = provParams;
    this.models = models;
    initMetaData();
    List<Entry> concList = getEntries(monitor);
    if (concList == null) {
      this.entries = new Entry[] {};
    } else {
      this.entries = concList.toArray (new Entry[] {});
    }
  }

  public ConcordanceModel() {
    super();
    this.tempDirPath = ""; //$NON-NLS-1$
    this.entries = new Entry[] {};
    this.models = new ArrayList<SystemTComputationResult>(0);
  }

  private void initMetaData() {
    if (this.models != null) {
      for (SystemTComputationResult model : this.models) {
        this.files.put(model.getDocumentID(), TRUE);
      }
      if (this.models.size() > 0) {
        SystemTComputationResult model = this.models.get(0);
        if (model == null)
        {
          return;
        }
        OutputView[]  outputViewList = model.getOutputViews ();
        final int numViews = outputViewList.length;
        this.typeNames = new String[numViews][];
        this.outputViewNames.clear ();
        // Iterate over views and columns to find all span columns
        for (int i = 0; i < numViews; i++) {
          OutputView view = outputViewList[i];
          this.outputViewNames.put (view.getName(), view.getOrigViewName ());
          this.typeNames[i] = new String[view.getFieldTypes().length];
          for (int j = 0; j < view.getFieldNames().length; j++) {
            final String typeName = view.getName() + "." + view.getFieldNames()[j]; //$NON-NLS-1$
            this.typeNames[i][j] = typeName;
            if (view.getFieldTypes()[j] == FieldType.SPAN) {
              this.modelTypes.put(typeName, TRUE);
            }
          }
        }
      }
    }
  }

  public String getTypeName(int viewId, int colId) {
    return this.typeNames[viewId][colId];
  }

  private final List<Entry> getEntries(IProgressMonitor monitor) {
    List<Entry> entryList = new ArrayList<Entry>();
    if (this.models != null) {
      for (SystemTComputationResult model : this.models) {
        if (monitor.isCanceled()) {
          return null;
        }
        OutputView[] views = model.getOutputViews();
        for (int i = 0; i < views.length; i++) {
          OutputView view = views[i];
          FieldType[] types = view.getFieldTypes();
          OutputViewRow[] rows = view.getRows();
          for (int j = 0; j < rows.length; j++) {
            for (int k = 0; k < types.length; k++) {
              // 'types' contains the common field types for all rows of the OutputView;
              // eg, it may say SPAN for a TEXT field because Text is a subclass of Span.
              // In that case, if we rely only on types[k] and cast TextVal to SpanVal, we'll
              // get a ClassCastException. TextVal is not a subclass of SpanVal. Hence
              // we need to check the type of rows[j].fieldValues[k] directly.
              // We still need to rely on types[k] as an additional condition, as there are
              // cases where a fieldValue is an instance of SpanVal but has a TEXT for a 
              // corresponding FieldType, and we don't want such cases being made into entries.
              if (rows[j].fieldValues[k] instanceof SpanVal && types[k] == SPAN) {
                SpanVal spanVal = (SpanVal) rows[j].fieldValues[k];
                if (spanVal.start >= 0) {
                  Entry entry = new Entry (model, i, j, k, spanVal.parentSpanName, this);
                  entryList.add (entry);
                  if (debug)
                    System.err.printf ("Added entry '%s' with values i=%d j=%d k=%d start=%d end=%d sourceID=%d\n",
                      types[k].name (), i, j, k, spanVal.start, spanVal.end, spanVal.sourceID);
                }
                else {
                  if (debug)
                    System.err.printf ("Excluding entry '%s' with values i=%d j=%d k=%d start=%d end=%d sourceID=%d\n",
                      types[k].name (), i, j, k, spanVal.start, spanVal.end, spanVal.sourceID);
                }
              }
              
            }
          }
        }
        monitor.worked(1);
      }
    }
    return entryList;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Entry entry : this.entries) {
      sb.append(entry.toString());
      sb.append("\n"); //$NON-NLS-1$
    }
    return sb.toString();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // Column header info
  public String[] getTableColumnTitles() {
    String[] ct = new String[NUMBER_OF_COLUMNS];
    ct[COLUMN_FILE_ID] = Messages.ConcordanceModel_inputdoc;
    ct[COLUMN_LEFT_CONTEXT] = Messages.ConcordanceModel_leftcontext;
    ct[COLUMN_ANNOTATION_TEXT] = Messages.ConcordanceModel_spanval;
    ct[COLUMN_RIGHT_CONTEXT] = Messages.ConcordanceModel_rightcontext;
    ct[COLUMN_ANNOTATION_TYPE] = Messages.ConcordanceModel_spanattrname;
    return ct;
  }

  public int[] getTableColumnOrientation() {
    int[] co = new int[NUMBER_OF_COLUMNS];
    co[COLUMN_FILE_ID] = SWT.LEFT;
    co[COLUMN_LEFT_CONTEXT] = SWT.RIGHT;
    co[COLUMN_ANNOTATION_TEXT] = SWT.CENTER;
    co[COLUMN_RIGHT_CONTEXT] = SWT.LEFT;
    co[COLUMN_ANNOTATION_TYPE] = SWT.LEFT;
    return co;
  }

  public int[] getTableColumnWidths() {
    int[] cw = new int[NUMBER_OF_COLUMNS];
    cw[COLUMN_FILE_ID] = 100;
    cw[COLUMN_LEFT_CONTEXT] = 200;
    cw[COLUMN_ANNOTATION_TEXT] = 200;
    cw[COLUMN_RIGHT_CONTEXT] = 200;
    cw[COLUMN_ANNOTATION_TYPE] = 100;
    return cw;
  }

  // End column header info
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  public int size() {
    if (this.entries != null) {
      return this.entries.length;
    }
    return 0;
  }

  public IConcordanceModelEntry[] getEntries() {
    return this.entries;
  }

  public ITypes getTypes() {
    return this.modelTypes;
  }

  public IFiles getFiles() {
    return this.files;
  }

  public IStringFilter getStringFilter(StringFilterType filterType) {
    switch (filterType) {
    case ANNOTATION_TEXT:
      return this.annotationTextFilter;
    case LEFT_CONTEXT:
      return this.leftContextFilter;
    case RIGHT_CONTEXT:
      return this.rightContextFilter;
    default:
      return null;
    }
  }

  public void setStringFilter(StringFilterType filterType, IStringFilter s) {
    switch (filterType) {
    case ANNOTATION_TEXT: {
      this.annotationTextFilter = s;
      break;
    }
    case LEFT_CONTEXT: {
      this.leftContextFilter = s;
      break;
    }
    case RIGHT_CONTEXT: {
      this.rightContextFilter = s;
      break;
    }
    }
  }

  public void resetStringFilter(StringFilterType filterType) {
    switch (filterType) {
    case ANNOTATION_TEXT: {
      this.annotationTextFilter = EMPTY_STRING_FILTER;
      return;
    }
    case LEFT_CONTEXT: {
      this.leftContextFilter = EMPTY_STRING_FILTER;
      return;
    }
    case RIGHT_CONTEXT: {
      this.rightContextFilter = EMPTY_STRING_FILTER;
      return;
    }
    }
  }

  @Override
  public String[] getOutputViewNames() {
    return this.outputViewNames.keySet ().toArray (new String[0]);
  }
  
  /**
   * Accepts an output view alias and returns the corresponding qualified view name.
   * @param outputViewAlias
   * @return
   */
  public String getActualViewName(String outputViewAlias) {
    return this.outputViewNames.get (outputViewAlias);
  }

  /**
   * @param viewName The name (alias, if present) of the output view
   */
  @Override
  public IAQLTableViewModel getViewModel(String viewName) {
    if (this.models == null || this.models.size() == 0) {
      return null;
    }
    // Obtain metadata
    final int idx = getIndexForViewName(viewName);
    SystemTComputationResult scr = this.models.get(0);
    OutputView[] outputViews = scr.getOutputViews();
    OutputView outputView = outputViews[idx];
    final int schemaSize = this.typeNames[idx].length;
    int width = schemaSize + 1;
    final boolean doProvenance = (this.provenanceParams != null)
        && this.provenanceParams.isProvenanceEnabled();
    if (doProvenance && hasProvenanceRewrite (viewName) && !isExternalView (viewName)) {
      ++width;
    }
    final String[] headers = new String[width];
    int i;
    for (i = 0; i < schemaSize; i++) {
      final String fieldName = outputView.getFieldNames()[i];
      final String fieldType = outputView.getFieldTypes()[i].toString();
      headers[i] = fieldName + " (" + fieldType + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
    headers[i] = Messages.ConcordanceModel_inputdocname;
    // Header for the Provenance buttons
    if (doProvenance && hasProvenanceRewrite (viewName) && !isExternalView (viewName)) {
      ++i;
      headers[i] = Constants.PROVENANCE_BUTTON_LABEL;
    }
    // Create data
    List<IRow> rows = new ArrayList<IRow>();
    for (SystemTComputationResult model : this.models) {
      OutputView ov = model.getOutputViews()[idx];
      for (OutputViewRow row : ov.getRows()) {
        rows.add(new Row(model, row, doProvenance));
      }
    }

    IRow[] table = new IRow[rows.size()];
    final AQLTableViewModel model = new AQLTableViewModel(viewName, rows.toArray(table), headers);
    return model;
  }

  /**
   * Verify if provenance rewrite exists for a given view.<br>
   * All it does for now is to check whether the provenance rewrite for the module
   * containing the view exists in the directory where provenance rewrite is kept.
   * @param viewName name (alias if present) of the output view
   * @return TRUE if provenance rewrite tam for the module containing the view exists;<br>
   *         FALSE otherwise.
   */
  private boolean hasProvenanceRewrite (String viewName)
  {
    if (project != null) {
      String moduleName = "";
      if (ProjectUtils.isModularProject (project)) {
        String actualViewName = this.outputViewNames.get (viewName);
        if (actualViewName == null || actualViewName.isEmpty ()) {
          actualViewName = viewName;
        }
        int offSet = actualViewName.lastIndexOf (".");
        if (offSet > 0) {
          moduleName = actualViewName.substring (0,  offSet);
        } else {
          return false; //input data incorrect if code reaches here. returning false.
        }
        
      }
      else
        moduleName = Constants.GENERIC_MODULE;

      IFolder provRewriteDir = ProjectUtils.getDefaultProvenanceBinDir (getProject());
      IFile tam = provRewriteDir.getFile (moduleName + ".tam");
      return (tam != null && tam.exists ());
    }

    return false;
  }

  private final int getIndexForViewName(final String viewName) {
    int count = 0;
    for (String name : this.outputViewNames.keySet ()) {
      if (name.equals(viewName)) {
        return count;
      }
      ++count;
    }
    return -1;
  }

  private boolean isExternalView (String viewName)
  {
    if ( viewName != null &&
         provenanceParams!= null &&
         provenanceParams.getExtViewNames () != null ) {

      for (String s : provenanceParams.getExtViewNames ()) {
        if (s.equals (viewName))
          return true;
      }
    }

    return false;
  }

  @Override
  public String getTempDirPath() {
    return this.tempDirPath;
  }

  @Override
  public ProvenanceRunParams getProvenanceParams() {
    return this.provenanceParams;
  }
  
  public IProject getProject() {
	return project;
  }
  
  public void setProject(IProject project) {
	  this.project = project;
  }
}
