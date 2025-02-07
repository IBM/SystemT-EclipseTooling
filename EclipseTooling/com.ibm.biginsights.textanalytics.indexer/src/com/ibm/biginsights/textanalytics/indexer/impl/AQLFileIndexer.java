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
package com.ibm.biginsights.textanalytics.indexer.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.ibm.avatar.api.Constants;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.api.tam.ModuleMetadataFactory;
import com.ibm.avatar.aql.AQLParser;
import com.ibm.avatar.aql.AbstractExportNode;
import com.ibm.avatar.aql.AbstractImportNode;
import com.ibm.avatar.aql.ColNameNode;
import com.ibm.avatar.aql.CreateDictNode;
import com.ibm.avatar.aql.CreateExternalViewNode;
import com.ibm.avatar.aql.CreateFunctionNode;
import com.ibm.avatar.aql.CreateTableNode;
import com.ibm.avatar.aql.CreateViewNode;
import com.ibm.avatar.aql.DetagDocNode;
import com.ibm.avatar.aql.DetagDocSpecNode;
import com.ibm.avatar.aql.DictExNode;
import com.ibm.avatar.aql.ExportDictNode;
import com.ibm.avatar.aql.ExportFuncNode;
import com.ibm.avatar.aql.ExportTableNode;
import com.ibm.avatar.aql.ExportViewNode;
import com.ibm.avatar.aql.ExtractNode;
import com.ibm.avatar.aql.ExtractionNode;
import com.ibm.avatar.aql.FromListItemNode;
import com.ibm.avatar.aql.FromListItemSubqueryNode;
import com.ibm.avatar.aql.FromListItemTableFuncNode;
import com.ibm.avatar.aql.FromListItemViewRefNode;
import com.ibm.avatar.aql.FromListNode;
import com.ibm.avatar.aql.HavingClauseNode;
import com.ibm.avatar.aql.ImportDictNode;
import com.ibm.avatar.aql.ImportFuncNode;
import com.ibm.avatar.aql.ImportModuleNode;
import com.ibm.avatar.aql.ImportTableNode;
import com.ibm.avatar.aql.ImportViewNode;
import com.ibm.avatar.aql.MinusNode;
import com.ibm.avatar.aql.ModuleNode;
import com.ibm.avatar.aql.NickNode;
import com.ibm.avatar.aql.AQLParseTreeNode;
import com.ibm.avatar.aql.OutputViewNode;
import com.ibm.avatar.aql.POSExNode;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.ExtractPatternNode;
import com.ibm.avatar.aql.PredicateNode;
import com.ibm.avatar.aql.RValueNode;
import com.ibm.avatar.aql.ScalarFnCallNode;
import com.ibm.avatar.aql.SelectListItemNode;
import com.ibm.avatar.aql.SelectListNode;
import com.ibm.avatar.aql.SelectNode;
import com.ibm.avatar.aql.StatementList;
import com.ibm.avatar.aql.StringNode;
import com.ibm.avatar.aql.Token;
import com.ibm.avatar.aql.UnionAllNode;
import com.ibm.avatar.aql.ViewBodyNode;
import com.ibm.avatar.aql.WhereClauseNode;
import com.ibm.avatar.aql.tam.ModuleUtils;
import com.ibm.biginsights.textanalytics.indexer.Activator;
import com.ibm.biginsights.textanalytics.indexer.exceptions.ElementTypeDeterminationException;
import com.ibm.biginsights.textanalytics.indexer.exceptions.ModuleNotFoundException;
import com.ibm.biginsights.textanalytics.indexer.exceptions.ProjectDeterminationException;
import com.ibm.biginsights.textanalytics.indexer.index.IDManager;
import com.ibm.biginsights.textanalytics.indexer.index.TextAnalyticsIndexer;
import com.ibm.biginsights.textanalytics.indexer.model.ElementDefinition;
import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.model.ElementReference;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectDependencyUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Indexes AQL files.
 * 
 *  Krishnamurthy, Rajeshwar Kalakuntla
 */
public class AQLFileIndexer extends FileIndexer
{
	@SuppressWarnings("unused")


  private ModuleScopedElements validElemRefs = null;
  private boolean debug = false; // Developer set attribute. Set this to true for displaying exception stack trace along
  // with error.

  private List<ModuleRef> listOfModuleRefsComesBeforeDef = null;  // This is to hold the module reference data for those elements which comes before than their element definitions
  private List<ElementRef> listOfElementRefsComesBeforeDef = null;// This is to hold the element reference data for those elements which comes before than their element definitions
  
  private IProject[] requiredProjectsList = {};
  private IProject currentProject = null;

  private List<String> predefinedTpyes = Arrays.asList (ProjectUtils.TYPES);
  /**
   * Module of the current file being indexed
   */
  protected String moduleName;

  @Override
  protected void initialize ()
  {
    super.initialize ();
    this.moduleName = fileToIndex.getParent ().getName ();
    this.validElemRefs = ModuleScopedElements.getInstance (moduleName);

    listOfModuleRefsComesBeforeDef = new ArrayList<ModuleRef> ();
    listOfElementRefsComesBeforeDef = new ArrayList<ElementRef> ();
    
    currentProject = fileToIndex.getProject ();

    try {
      requiredProjectsList = currentProject.getReferencedProjects ();
    }
    catch (CoreException e) {
      //do nothing. property will retain its default value of empty array.
    }
  }

  @Override
  protected void parseAndIndex () throws Exception
  {
    AQLParser parser = new AQLParser (fileToIndex.getLocation ().toFile ());
    // Eclipse Text Editor treats TAB characters as a single character whereas the AQLParser treats TAB characters as 8
    // or 4 or 1
    // character based on position where it was used. Due to this AQLEditor UI related functionalities like AQLDocHover
    // Refactoring were all having issues in mapping the offsets of AQLParser with Eclipse Text Editor's offset values.
    // To resolve this AQLParser.setTabSize (1) is called such that AQLParser and Eclipse Text Editor will treat the TAB
    // character in the same way.
    parser.setTabSize (1);
    StatementList stmtList = parser.parse ();
    LinkedList<AQLParseTreeNode> ptn = stmtList.getParseTreeNodes ();
    for (AQLParseTreeNode node : ptn) {
      createIndex (node);
    }
    // These methods to add module and element references to actual element and module caches at the end of indexing files
    addModuleRefsComesBeforeDefToModuleCache();
    addElementRefsComesBeforeDefToElementCache();
  }

  /**
   * This method is to add module references to actual module cache at end
   */  
  private void addModuleRefsComesBeforeDefToModuleCache ()
  {
    for (ModuleRef moduleRefObj : listOfModuleRefsComesBeforeDef) {
      String moduleName = moduleRefObj.getModule ();

      try {
        // Project name used in ModuleRef object is just a placeholder value. Determine correct project name.
        String correctProjectName = detectProject (moduleName);

        addModuleReference (correctProjectName, moduleName, moduleRefObj.getFile (), moduleRefObj.getBeginLine (),
          moduleRefObj.getBeginColumn ());
      }
      catch (ProjectDeterminationException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logDebug (
          String.format ("Unable to index reference to module %s in file %s.", moduleName, //$NON-NLS-1$
            moduleRefObj.getFile ().getFullPath ().toString ()), e);
      }
    }
    listOfModuleRefsComesBeforeDef.clear ();
  }
  /**
   * This method is to add element references to actual element cache at end
   */
  private void addElementRefsComesBeforeDefToElementCache ()
  {
    for (ElementRef elementRefObj : listOfElementRefsComesBeforeDef) {
      String moduleName = elementRefObj.getModule ();

      try {
        // Project name used in ModuleRef object is just a placeholder value. Determine correct project name.
        String correctProjectName = detectProject (moduleName);
        addElementReference (elementRefObj.getRefByElemID (), correctProjectName, moduleName,
          elementRefObj.getElementType (), elementRefObj.getElementName (), elementRefObj.getFile (),
          elementRefObj.getBeginLine (), elementRefObj.getBeginColumn ());
      }
      catch (ProjectDeterminationException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logDebug (
          String.format ("Unable to index reference to element %s.%s in file %s.", moduleName,  //$NON-NLS-1$
            elementRefObj.getElementName (), elementRefObj.getFile ()), e);
      }
    }
    listOfElementRefsComesBeforeDef.clear ();
  }

  /**
   * Identifies the AQL element definitions and references and adds them to the cache
   * 
   * @param node
   * @throws Exception
   */
  private void createIndex (AQLParseTreeNode node) throws Exception
  {
    // module statement
    if (node instanceof ModuleNode) {
      ModuleNode moduleNode = (ModuleNode) node;
      indexModuleNode (moduleNode.getName ());
    }

    // import XXX statement
    else if (node instanceof AbstractImportNode) {
      indexImportNode ((AbstractImportNode) node);
    }

    // import module statement
    else if (node instanceof ImportModuleNode) {
      ImportModuleNode importModuleNode = (ImportModuleNode) node;
      indexImportModuleNode (importModuleNode);
    }

    // output view statement
    else if (node instanceof OutputViewNode) {
      OutputViewNode ovn = (OutputViewNode) node;
      indexOutputViewNode (ovn);
    }

    // export XXX statement
    else if (node instanceof AbstractExportNode) {
      indexExportNode ((AbstractExportNode) node);
    }

    // create view statement
    else if (node instanceof CreateViewNode) {
      indexCreateViewNode ((CreateViewNode) node);
    }

    else if (node instanceof CreateExternalViewNode) {
      indexCreateExternalViewNode ((CreateExternalViewNode) node);
    }

    // create dictionary statement
    else if (node instanceof CreateDictNode) {
      indexCreateDictNode ((CreateDictNode) node);
    }

    // create table statement
    else if (node instanceof CreateTableNode) {
      indexCreateTableNode ((CreateTableNode) node);
    }

    // create function statement
    else if (node instanceof CreateFunctionNode) {
      indexCreateFunctionNode ((CreateFunctionNode) node);
    }

    // Detag statement
    else if (node instanceof DetagDocNode) {
      indexDetagNode ((DetagDocNode) node);
    }

  }

  private void indexCreateExternalViewNode (CreateExternalViewNode node)
  {
    String viewName = node.getViewNameNode ().getNickname ();
    Token token = node.getViewNameNode ().getOrigTok ();

    // Pass 0: add to viewDefs
    validElemRefs.viewDefs.add (node.getExternalViewName ()); // fully qualified name

    // Pass 1: create an element definition
    indexElementDefinition (ElementType.VIEW, viewName, token);
  }

  private void indexImportModuleNode (ImportModuleNode importModuleNode)
  {
    String importedModuleName = importModuleNode.getImportedModuleName ().getNickname ();
    indexModuleNode (importModuleNode.getImportedModuleName ());
    try {
      String projectName = detectProject (importedModuleName);

      // if control comes here, then the imported module has a source in the current workspace
      addImportedElementsFromSource (projectName, importedModuleName);

    }
    catch (ModuleNotFoundException mnfe) {
      // possible that the module is available in binary (.tam) form
      addImportedElementsFromTAM (importedModuleName);
    }
    catch (ProjectDeterminationException pde) {
      // Log and return
      if (debug) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          String.format ("Problem determining project name for module %s", importedModuleName), pde);
      }
      else {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          String.format ("Problem determining project name for module %s", importedModuleName));
      }
      return;
    }
    catch (Exception e) {
      if (debug) {
        // Log and return
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          String.format ("Error indexing imported module %s", importedModuleName), e);
      }
      else {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          String.format ("Error indexing imported module %s", importedModuleName));
      }
      return;
    }

  }

  /**
   * Imports elements defined in importedModuleName into the current file's imported elements list
   * 
   * @param projectName
   * @param importedModuleName
   * @throws Exception
   */
  private void addImportedElementsFromSource (String projectName, String importedModuleName) throws Exception
  {
    // get module folder
    IFolder moduleFolder = ProjectUtils.getModuleFolder (projectName, importedModuleName);

    // get list of AQL files under module folder
    String[] aqlFiles = ProjectUtils.getAqlFilesOfModule (projectName, importedModuleName);

    // get element definitions in AQL file and add to imported elements list
    // TODO: Add only exported elements, in future
    for (String aqlFileName : aqlFiles) {
      IFile aqlFile = moduleFolder.getFile (aqlFileName);
      Integer fileId = fileCache.getFileId (aqlFile);
      if (fileId == null) { // then the file is not indexed yet
        TextAnalyticsIndexer.getInstance ().reindexModuleFolder (moduleFolder, false);
        fileId = fileCache.getFileId (aqlFile);
      }

      List<ElementDefinition> elemDefs = elemCache.getElementDefinitionsInFile (aqlFile);
      for (ElementDefinition elementDef : elemDefs) {
        String elemName = elemCache.getElementNameInAQL (elementDef.getElementId ());
        switch (elementDef.getType ()) {
          case VIEW:
            validElemRefs.importedViews.add (elemName);
            break;

          case DICTIONARY:
            validElemRefs.importedDicts.add (elemName);
            break;

          case TABLE:
            validElemRefs.importedTables.add (elemName);
            break;

          case FUNCTION:
            validElemRefs.importedFunctions.add (elemName);
            break;

        }
      }

    }

  }

  /**
   * Attempts to import elements from a given module from it's TAM file
   * 
   * @param importedModuleName
   */
  private void addImportedElementsFromTAM (String importedModuleName)
  {
    String modulePath = ProjectDependencyUtil.populateProjectDependencyPath (ProjectUtils.getProject (this.projectName));
    try {
      ModuleMetadata metadata = ModuleMetadataFactory.readMetaData (importedModuleName, modulePath);

      // import views
      String[] views = metadata.getExportedViews ();
      for (String viewName : views) {
        validElemRefs.importedViews.add (viewName);
      }

      // import dicts
      String[] dicts = metadata.getExportedDictionaries ();
      for (String dictName : dicts) {
        validElemRefs.importedDicts.add (dictName);
      }

      // import tables
      String[] tables = metadata.getExportedTables ();
      for (String tableName : tables) {
        validElemRefs.importedTables.add (tableName);
      }

      // import functions
      String[] functions = metadata.getExportedFunctions ();
      for (String funcName : functions) {
        validElemRefs.importedFunctions.add (funcName);
      }

    }
    catch (Exception e) {
      // Log and return
      if (debug) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          String.format ("Problem importing elements from binary form of module %s", importedModuleName), e);
      }
      else {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          String.format ("Problem importing elements from binary form of module %s", importedModuleName));
      }
      return;
    }

  }

  private void indexExportNode (AbstractExportNode exportNode)
  {
    NickNode modulePrefixNode = exportNode.getModulePrefixNickNode ();
    NickNode unqualifiedElemNameNode = exportNode.getUnqualifiedElemNameNickNode ();

    String moduleName = exportNode.getModuleName ();

    ElementType type = getElementType (exportNode);

    if (modulePrefixNode != null) {
      indexModuleNode (modulePrefixNode);
      moduleName = modulePrefixNode.getNickname ();
    }

    indexNonCreateStmtElementReference (moduleName, unqualifiedElemNameNode, type);

  }

  private void indexOutputViewNode (OutputViewNode ovn)
  {
    NickNode modulePrefixNode = ovn.getModulePrefixNickNode ();
    NickNode unqualifiedViewNameNode = ovn.getUnqualifiedViewNameNickNode ();
    String module = this.moduleName;
    String owningModuleName = this.moduleName;
    if (modulePrefixNode != null) {
      indexModuleNode (modulePrefixNode);
      owningModuleName = modulePrefixNode.getNickname ();
    }

    indexNonCreateStmtElementReference (owningModuleName, unqualifiedViewNameNode, ElementType.VIEW);
  }

  /**
   * Creates an index for CreateFunctionNode
   * 
   * @param funcNode
   */
  private void indexCreateFunctionNode (CreateFunctionNode funcNode)
  {
    NickNode funcNickNode = funcNode.getFunctionNameNode ();
    // Pass 0: add to functionDefs
    validElemRefs.functionDefs.add (funcNickNode.getNickname ());

    Token token = funcNickNode.getOrigTok ();
    indexElementDefinition (ElementType.FUNCTION, funcNickNode.getNickname (), token);
  }

  private void indexDetagNode (DetagDocNode detagNode) throws Exception
  {
    String detagName = detagNode.getUnqualifiedDetaggedDocName ();
    Token token = detagNode.getDetaggedDocNameNode ().getOrigTok ();

    // Pass 0: add to viewDefs
    validElemRefs.viewDefs.add (detagName);

    // Pass 1: create an element definition
    indexElementDefinition (ElementType.VIEW, detagName, token);

    // Get the list of DetagDocSpecNodes and index them
    ArrayList<DetagDocSpecNode> detagDocNodes = detagNode.getEntries ();
    for (DetagDocSpecNode detagDocSpecNode : detagDocNodes) {
      String detagDocSpecNodeName = detagDocSpecNode.getUnqualifiedName ();
      Token detagDocSpecNodeToken = detagDocSpecNode.getTagType ().getOrigTok ();
      validElemRefs.viewDefs.add (detagDocSpecNodeName);
      indexElementDefinition (ElementType.VIEW, detagDocSpecNodeName, detagDocSpecNodeToken);
    }
  }

  /**
   * @param node
   * @throws Exception
   */
  private void indexCreateViewNode (CreateViewNode node) throws Exception
  {
    String viewName = node.getViewNameNode ().getNickname ();
    Token token = node.getViewNameNode ().getOrigTok ();

    // Pass 0: add to viewDefs
    validElemRefs.viewDefs.add (node.getViewName ()); // fully qualified name

    // Pass 1: create an element definition
    ElementDefinition elementDef = indexElementDefinition (ElementType.VIEW, viewName, token);

    Integer referencedByElemID = elementDef.getElementId ();

    // Pass 2: Look for references to other elements
    ViewBodyNode vbn = node.getBody ();

    // ExtractNode
    if (vbn instanceof ExtractNode) {
      ExtractNode extNode = (ExtractNode) vbn;
      pickElemRefsInExtractNode (referencedByElemID, extNode);
    }

    // SelectNode
    else if (vbn instanceof SelectNode) {
      SelectNode selNode = (SelectNode) vbn;
      pickElemRefsInSelectNode (referencedByElemID, selNode);
    }

    // PatternNode
    else if (vbn instanceof ExtractPatternNode) {
      ExtractPatternNode patternNode = (ExtractPatternNode) vbn;
      pickElemRefsInPatternNode (referencedByElemID, patternNode);
    }

    // UnionAllNode
    else if (vbn instanceof UnionAllNode) {
      UnionAllNode unionNode = (UnionAllNode) vbn;
      pickElemRefsInUnionAllNode (referencedByElemID, unionNode);
    }

    // MinusNode
    else if (vbn instanceof MinusNode) {
      MinusNode minusNode = (MinusNode) vbn;
      pickElemRefsInMinusNode (referencedByElemID, minusNode);
    }

  }

  private void pickElemRefsInExtractNode (Integer referencedByElemID, ExtractNode extNode) throws Exception
  {
    // Pass 1: SelectListNode
    SelectListNode selectListNode = extNode.getExtractList ().getSelectList ();
    pickElemRefsInSelectListNode (referencedByElemID, selectListNode);

    // Pass 2: ExtractionNode
    ExtractionNode extractionNode = extNode.getExtractList ().getExtractSpec ();
    pickElemRefsInExtractionNode (referencedByElemID, extractionNode);

    // Pass 3: FromListItemNode
    FromListItemNode fromListItemNode = extNode.getTarget ();
    pickElemRefsInFromListItemNode (referencedByElemID, fromListItemNode);

    // Pass 4: HavingClauseNode
    HavingClauseNode havingClauseNode = extNode.getHavingClause ();
    pickElemRefsInHavingClauseNode (referencedByElemID, havingClauseNode);

  }

  private void pickElemRefsInSelectNode (Integer referencedByElemID, SelectNode selNode) throws Exception
  {
    // Pass 1: SelectListNode
    SelectListNode selList = selNode.getSelectList ();
    pickElemRefsInSelectListNode (referencedByElemID, selList);

    // Pass 2: FromListNode
    FromListNode fromList = selNode.getFromList ();
    pickElemRefsInFromListNode (referencedByElemID, fromList);

    // Pass 3: WhereClause
    WhereClauseNode whereClauseNode = selNode.getWhereClause ();
    pickElemRefsInWhereClauseNode (referencedByElemID, whereClauseNode);

  }

  private void pickElemRefsInPatternNode (Integer referencedByElemID, ExtractPatternNode patternNode) throws Exception
  {
    // Pass 1: SelectListNode
    SelectListNode selectListNode = patternNode.getSelectList ();
    pickElemRefsInSelectListNode (referencedByElemID, selectListNode);

    // Pass 2: FromListNode
    FromListNode fromListNode = patternNode.getFromList ();
    pickElemRefsInFromListNode (referencedByElemID, fromListNode);

    // Pass 3: HavingClauseNode
    HavingClauseNode havingClauseNode = patternNode.getHavingClause ();
    pickElemRefsInHavingClauseNode (referencedByElemID, havingClauseNode);

  }

  private void pickElemRefsInUnionAllNode (Integer referencedByElemID, UnionAllNode unionNode) throws Exception
  {
    for (int i = 0; i < unionNode.getNumStmts (); ++i) {
      ViewBodyNode vbn = unionNode.getStmt (i);
      // SelectNode
      if (vbn instanceof SelectNode) {
        pickElemRefsInSelectNode (referencedByElemID, (SelectNode) vbn);
      }

      // ExtractNode
      else if (vbn instanceof ExtractNode) {
        pickElemRefsInExtractNode (referencedByElemID, (ExtractNode) vbn);
      }

      // MinusNode
      else if (vbn instanceof MinusNode) {
        pickElemRefsInMinusNode (referencedByElemID, (MinusNode) vbn);
      }

      // UnionAllNode
      else if (vbn instanceof UnionAllNode) {
        pickElemRefsInUnionAllNode (referencedByElemID, (UnionAllNode) vbn);
      }
    }

  }

  private void pickElemRefsInMinusNode (Integer referencedByElemID, MinusNode minusNode) throws Exception
  {
    ViewBodyNode firstStmt = minusNode.getFirstStmt ();
    ViewBodyNode secondStmt = minusNode.getSecondStmt ();

    // Pass 1: Process first statement
    if (firstStmt instanceof SelectNode) {
      pickElemRefsInSelectNode (referencedByElemID, (SelectNode) firstStmt);
    }
    else if (firstStmt instanceof ExtractNode) {
      pickElemRefsInExtractNode (referencedByElemID, (ExtractNode) firstStmt);
    }
    else if (firstStmt instanceof UnionAllNode) {
    	pickElemRefsInUnionAllNode (referencedByElemID, (UnionAllNode) firstStmt);
    }
    else if (firstStmt instanceof MinusNode) {
    	pickElemRefsInMinusNode (referencedByElemID, (MinusNode) firstStmt);
    }

    // Pass 2: Process second statement
    if (secondStmt instanceof SelectNode) {
      pickElemRefsInSelectNode (referencedByElemID, (SelectNode) secondStmt);
    }
    else if (secondStmt instanceof ExtractNode) {
      pickElemRefsInExtractNode (referencedByElemID, (ExtractNode) secondStmt);
    }
    else if (secondStmt instanceof UnionAllNode) {
    	pickElemRefsInUnionAllNode (referencedByElemID, (UnionAllNode) secondStmt);
    }
    else if (secondStmt instanceof MinusNode) {
    	pickElemRefsInMinusNode (referencedByElemID, (MinusNode) secondStmt);
    }

  }

  private void pickElemRefsInHavingClauseNode (Integer referencedByElemID, HavingClauseNode havingClauseNode) throws Exception
  {
    if (havingClauseNode == null) { return; }
    ArrayList<PredicateNode> preds = havingClauseNode.getPreds ();
    if (preds == null || preds.size () == 0) { return; }
    for (PredicateNode predNode : preds) {
      pickElemRefsInPredicateNode (referencedByElemID, predNode);
    }
  }

  private void pickElemRefsInFromListItemNode (Integer referencedByElemID, FromListItemNode fromListItemNode) throws Exception
  {
    if (true == (fromListItemNode instanceof FromListItemTableFuncNode)) {
      // We are not interested in FromListItemTableFuncNode
      return;
    }

    if (fromListItemNode instanceof FromListItemSubqueryNode) {
      FromListItemSubqueryNode subqueryNode = (FromListItemSubqueryNode) fromListItemNode;
      ViewBodyNode vbn = subqueryNode.getBody ();
      if (vbn instanceof ExtractNode) {
        pickElemRefsInExtractNode (referencedByElemID, (ExtractNode) vbn);
      }
      else if (vbn instanceof SelectNode) {
        pickElemRefsInSelectNode (referencedByElemID, (SelectNode) vbn);
      }
    }
    else if (fromListItemNode instanceof FromListItemViewRefNode) {
      pickElemRefsInFromListItemViewRefNode (referencedByElemID, (FromListItemViewRefNode) fromListItemNode);
    }

  }

  private void pickElemRefsInFromListItemViewRefNode (Integer referencedByElemID,
    FromListItemViewRefNode fromListItemNode)
  {
    NickNode viewNameNode = fromListItemNode.getViewName ();
    String viewName = viewNameNode.getNickname ();

    // if view belongs to current module (i.e no dot in it), then add module prefix
    if (false == viewName.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))
        && false == "Document".equals (viewName)) {
      viewName = String.format ("%s.%s", moduleName, viewName);
    }
    // add an index for FromList item only if it is already defined (or) imported
    // The From clause can refer to either views or tables. So, check for both of them in validElemRefs
    if (validElemRefs.viewDefs.contains (viewName) || validElemRefs.importedViews.contains (viewName)) {
      pickElemRefsInNickNode (referencedByElemID, viewNameNode, ElementType.VIEW);
    }else if (validElemRefs.tableDefs.contains (viewName) || validElemRefs.importedTables.contains (viewName)) {
      pickElemRefsInNickNode (referencedByElemID, viewNameNode, ElementType.TABLE);
    }else {
      //element being referred to here could be a view or a table.
      trackElemRefsComesBeforeElemDef (referencedByElemID, viewNameNode, ElementType.VIEW_OR_TABLE);   
    }
  }

  /**
   * @param referencedByElemID
   * @param nickNode
   * @param type
   */
  private void pickElemRefsInNickNode (Integer referencedByElemID, NickNode nickNode, ElementType type)
  {
    Token token = nickNode.getOrigTok ();
    int beginLine = token.beginLine;
    int elemBeginCol = token.beginColumn;
    String elemNickName = nickNode.getNickname ();
    String[] names = splitQualifiedName (elemNickName, this.moduleName);
    String modulePrefix = names[0];
    String refElemName = names[1];

    // The reference is of format <modulePrefix>.<refElemName>
    if (modulePrefix != null && modulePrefix.trim ().length () > 0) {
      int moduleBeginCol = 0;
      if(IndexerUtil.isAQLKeyword(modulePrefix)||(StringUtils.isEnclosedInDoublequotes (modulePrefix))){
        moduleBeginCol = elemBeginCol - (modulePrefix.length () + 1 + 2); // +1 for the dot  // +2 for double quotes  
      }else{
        moduleBeginCol = elemBeginCol - (modulePrefix.length () + 1 ); // +1 for the dot
      }
      String referencedProject = null;
      try {
        referencedProject = detectProject (modulePrefix, type, refElemName);
      }
      catch (Exception e) {
        if (debug) {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
            String.format ("Could not index %s because project name for element %s could not be determined",
              fileToIndex.getName (), elemNickName), e);
        }
        else {
          LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
            String.format ("Could not index %s because project name for element %s could not be determined",
              fileToIndex.getName (), elemNickName));
        }
        return;
      }
      // add a module reference only when the elemNickName contained a modulePrefix
      if (true == elemNickName.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))) {
        addModuleReference (referencedProject, modulePrefix, fileToIndex, beginLine, moduleBeginCol);
      }
      addElementReference (referencedByElemID, referencedProject, modulePrefix, type, refElemName,
        fileToIndex.getName (), beginLine, elemBeginCol);
    }
    // The referenced view belongs to current module
    else {
      refElemName = elemNickName;
      addElementReference (referencedByElemID, this.projectName, this.moduleName, type, refElemName,
        fileToIndex.getName (), beginLine, elemBeginCol);
    }
  }

  /**
   * This method is to pick and track the element references which comes before their actual element definitions and 
   * hold them until indexing the file, these references will be added to actual element and module cache's at the end of indexing. 
   * 
   * @param referencedByElemID  id of the element that has a reference to the element identified by the nick node. 
   * @param nickNode            NickNode of the referenced element 
   * @param type                Type of the referenced element
   */
  private void trackElemRefsComesBeforeElemDef (Integer referencedByElemID, NickNode nickNode, ElementType type)
  {
    Token token = nickNode.getOrigTok ();
    if(token == null)return;
    int beginLine = token.beginLine;
    int elemBeginCol = token.beginColumn;

    String elemNickName = nickNode.getNickname ();
    String[] names = splitQualifiedName (elemNickName, this.moduleName);
    String modulePrefix = names[0];
    String refElemName = names[1];

    if (modulePrefix != null && modulePrefix.trim ().length () > 0) {
      int moduleBeginCol = 0;
      if(IndexerUtil.isAQLKeyword(modulePrefix)||(StringUtils.isEnclosedInDoublequotes (modulePrefix))){
        moduleBeginCol = elemBeginCol - (modulePrefix.length () + 1 + 2); // +1 for the dot  // +2 for double quotes  
      }else{
        moduleBeginCol = elemBeginCol - (modulePrefix.length () + 1 ); // +1 for the dot
      }
      String referencedProject = fileToIndex.getProject ().getName ();;
      // add a module reference only when the elemNickName contained a modulePrefix
      if (true == elemNickName.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))) {
        listOfModuleRefsComesBeforeDef.add (getModuleRefObject (referencedProject, modulePrefix, fileToIndex, beginLine, moduleBeginCol));
      }
      listOfElementRefsComesBeforeDef.add (getElementRefObject (referencedByElemID, referencedProject, modulePrefix, type, refElemName, fileToIndex.getName (), beginLine, elemBeginCol));
    }
    // The referenced view belongs to current module
    else {
      refElemName = elemNickName;
      listOfElementRefsComesBeforeDef.add (getElementRefObject (referencedByElemID, this.projectName, this.moduleName, type, refElemName, fileToIndex.getName (), beginLine, elemBeginCol));
    }
  }

  /**
   * This method is a helper method to create and return ModuleRef object by taking required parameters 
   * 
   * @param project  name of the project where module reference belongs to
   * @param module  name of the module 
   * @param file    File instance where module reference exists
   * @param beginLine Begin line number of the module starting offset in file.
   * @param beginColumn Begin column number of the module starting offset in file.
   * 
   * @return ModuleRef returns the ModuleRef object.
   */
  private ModuleRef getModuleRefObject(String project, String module, IFile file, int beginLine, int beginColumn){
    ModuleRef modRefObj = new ModuleRef ();
    modRefObj.setProject (project);
    modRefObj.setModule (module);
    modRefObj.setFile (file);
    modRefObj.setBeginLine (beginLine);
    modRefObj.setBeginColumn (beginColumn);

    return modRefObj;
  }
  /**
   * This method is a helper method to create and return ElementRef object by taking required parameters
   * 
   * @param refByElementID id of the element that has a reference to the element identified by project.module.elementName
   * @param project project name of the element belongs to
   * @param module Module name of the element belongs to 
   * @param type Type of the element referenced.
   * @param elementName Name of the element being referenced
   * @param fileName  Name of the file which contains the element 
   * @param beginLine Begin line number of the element starting offset in file.
   * @param beginColumn Begin column number of the element starting offset in file.
   * 
   * @return ElementRef returns ElementRef object 
   */
  private ElementRef getElementRefObject(Integer refByElementID, String project, String module, ElementType type, 
    String elementName, String fileName, int beginLine, int beginColumn){
    ElementRef eleRefObj = new ElementRef ();
    eleRefObj.setRefByElemID (refByElementID);
    eleRefObj.setProject (project);
    eleRefObj.setModule (module);
    eleRefObj.setElementType (type);
    eleRefObj.setElementName (elementName);
    eleRefObj.setFile (fileName);
    eleRefObj.setBeginLine (beginLine);
    eleRefObj.setBeginColumn (beginColumn);

    return eleRefObj;
  }

  /**
   * @param referencedByElemID
   * @param extractionNode
   */
  private void pickElemRefsInExtractionNode (Integer referencedByElemID, ExtractionNode extractionNode)
  {
    if (extractionNode instanceof DictExNode) {
      pickElemRefsInDictExNode (referencedByElemID, (DictExNode) extractionNode);
    }
    else if (extractionNode instanceof POSExNode) {
      pickElemRefsInPOSExNode (referencedByElemID, (POSExNode) extractionNode);
    }
  }

  /**
   * @param referencedByElemID
   * @param posexNode
   */
  private void pickElemRefsInPOSExNode (Integer referencedByElemID, POSExNode posexNode)
  {
    NickNode mappingTableNode = posexNode.getMappingTableNameNode ();
    if (mappingTableNode != null) {
      pickElemRefsInNickNode (referencedByElemID, mappingTableNode, ElementType.TABLE);
    }
  }

  /**
   * @param referencedByElemID
   * @param dictExNode
   */
  private void pickElemRefsInDictExNode (Integer referencedByElemID, DictExNode dictExNode)
  {
    for (int i = 0; i < dictExNode.getNumDicts (); ++i) {
      StringNode dictNameNode = dictExNode.getDictName (i);
      Token token = dictNameNode.getOrigTok ();
      // pickElemRefsInDictStringNodeWithSingleQuote (referencedByElemID, dictNameNode, token);
      /**
       * Added below conditional branching is to fix the defect 35608. The dictionary node can be enclosed in 
       * single quote or without quote. 
       * 
       * We have a sample module that exports a dictionary as shown below.

            module Company;
            create external dictionary CompanyNames 
            allow_empty false
            with language as 'en'
            and case insensitive;
            export dictionary CompanyNames;

       * We have an other module where we consume imported dictionary.

            module Output; 
            import module Company;
            create view CompanyA as
            extract dictionary Company.CompanyNames on D.text as company
            from Document ;

            create view CompanyB as
            extract dictionary 'Company.CompanyNames' on D.text as company
            from Document ;
       * 
       * If you see the view CompanyA and CompanyB, the dictionary name is enclosed with or without single quote.
       * 
       * Based on the single quote or not, we need to calculate the boundary of the token and index it properly.  
       */
      /*
       * For both the scenarios, i.e with or with out single quote, we call the same method below. 
       * Within this method branching takes place based on the enclosed single quotes 
       */
      pickElemRefsInDictStringNode(referencedByElemID, dictNameNode, token);
    }
  }
  
  /**
   * Index the dictionary node, handles both the cases with and with out enclosed single quotes.
   * 
   * @param referencedByElemID  id of the element that has a reference to the element identified by the dictName Node
   * @param dictNameNode        dictionary name node
   * @param token               dictionary element token
   */
  private void pickElemRefsInDictStringNode (Integer referencedByElemID, StringNode dictNameNode, Token token)
  {
    String dictName = dictNameNode.getStr ();
    
    /*
     * This if condition is added to fix the defect 50406, Pre-defined types like ScalarList should not be considered as DictNames 
     */
    if(predefinedTpyes.contains (dictName)){
      return;
    }
    
    /*
     * Check if the dictionary name is already encountered during indexing? If yes, it will index reference otherwise 
     * it will track those references for indexing in future. 
     */
    if (validElemRefs.inlineAndTableDicts.contains (dictName) || validElemRefs.dictFileReferences.contains (dictName)
        || validElemRefs.importedDicts.contains (dictName)) {
      //Index dictionary string node references
      indexElemRefsInDictStringNode (referencedByElemID, dictNameNode, token);
    }else{
      //Track dictionary string node references for future indexing.
      trackElemRefsInDictStringNodeForFutureIndexing (referencedByElemID, dictNameNode, token);
    }
  }

  /**
   * Index the dictionary node, handles both the cases with and with out enclosed single quotes.
   * 
   * @param referencedByElemID  id of the element that has a reference to the element identified by the dictName Node
   * @param dictNameNode        dictionary name node
   * @param token               dictionary element token
   */
  private void indexElemRefsInDictStringNode (Integer referencedByElemID, StringNode dictNameNode, Token token){
    String str = dictNameNode.getOrigTok ().toString ();
    String dictName = dictNameNode.getStr ();
    int beginLine = token.beginLine;
    int beginCol = 0;
    if(str.startsWith ("'")){//$NON-NLS-1$
      beginCol = token.beginColumn + 1; // +1 to skip single quote
    }else{
      beginCol =  token.beginColumn;
    }
    String[] names = splitQualifiedName (dictName, this.moduleName);
    String modulePrefix = names[0];
    String refElemName = names[1];
    String project;
    int typeBeginCol;
    
    try {
      project = detectProject (modulePrefix, ElementType.DICTIONARY, refElemName);
    }
    catch (Exception e) {
      if (debug) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          String.format ("Could not index %s because project name for element %s could not be determined", //$NON-NLS-1$
            fileToIndex.getName (), dictName), e);
      }
      else {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
          String.format ("Could not index %s because project name for element %s could not be determined", //$NON-NLS-1$
            fileToIndex.getName (), dictName));
      }
      return;
    }
    if(str.startsWith ("'")){//$NON-NLS-1$
      if (true == dictName.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))) {
        if(IndexerUtil.isAQLKeyword(modulePrefix)||(StringUtils.isEnclosedInDoublequotes (modulePrefix))){
          typeBeginCol = beginCol + (modulePrefix.length () + 1 + 2); // +1 for the dot  // +2 for double quotes
        }else{
          // For dictionary 'Company.CompanyNames', the beginCol will contains the offset for Company.CompanyNames.
          // We add the Module Length and 1 (for dot) to get the offset of the Dictionary Name (CompanyNames).
          typeBeginCol = beginCol + (modulePrefix.length () + 1); // +1 for the dot
        }
        addModuleReference (project, modulePrefix, fileToIndex, beginLine, beginCol);
      }
      else {
        typeBeginCol = beginCol;
      }
      addElementReference (referencedByElemID, project, modulePrefix, ElementType.DICTIONARY, refElemName,
        fileToIndex.getName (), beginLine, typeBeginCol);
    }else{
      if (true == dictName.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))) {
        if(IndexerUtil.isAQLKeyword(modulePrefix)||(StringUtils.isEnclosedInDoublequotes (modulePrefix))){
          typeBeginCol = beginCol - (modulePrefix.length () + 1 + 2); // +1 for the dot  // +2 for double quotes
        }else{
          // For dictionary Company.CompanyNames, the beginCol will contains the offset for CompanyNames.
          // We substract the Module Length and 1 (for dot) to get the offset of the Module Name (Company).
          typeBeginCol = beginCol - (modulePrefix.length () + 1); // +1 for the dot
        }
        addModuleReference (project, modulePrefix, fileToIndex, beginLine, typeBeginCol);
      }
      else {
        typeBeginCol = beginCol;
      }
      addElementReference (referencedByElemID, project, modulePrefix, ElementType.DICTIONARY, refElemName,
        fileToIndex.getName (), beginLine, beginCol);
    }
  }
  
  /**
   * Track the dictionary node references and keep them for future indexing, 
   * handles both the cases with and with out enclosed single quotes.
   * 
   * @param referencedByElemID  id of the element that has a reference to the element identified by the dictName Node
   * @param dictNameNode        dictionary name node
   * @param token               dictionary element token
   */
  private void trackElemRefsInDictStringNodeForFutureIndexing (Integer referencedByElemID, StringNode dictNameNode, Token token){
    String str = dictNameNode.getOrigTok ().toString ();
    String dictName = dictNameNode.getStr ();
    int beginLine = token.beginLine;
    int beginCol = 0;
    if(str.startsWith ("'")){//$NON-NLS-1$
      beginCol = token.beginColumn + 1; // +1 to skip single quote
    }else{
      beginCol =  token.beginColumn;
    }
    String[] names = splitQualifiedName (dictName, this.moduleName);
    String modulePrefix = names[0];
    String refElemName = names[1];
    String project;
    int typeBeginCol;
    
    /**
     * This else block is executed when the dictionary reference element comes first than actual dictionary statement definitions.
     * Here we are keeping aside these references and adding them to actual caches later on.
     */
    project = fileToIndex.getProject ().getName ();
    if(str.startsWith ("'")){//$NON-NLS-1$
      if (true == dictName.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))) {
        if(IndexerUtil.isAQLKeyword(modulePrefix)||(StringUtils.isEnclosedInDoublequotes (modulePrefix))){
          typeBeginCol = beginCol + (modulePrefix.length () + 1 + 2); // +1 for the dot  // +2 for double quotes
        }else{
          typeBeginCol = beginCol + (modulePrefix.length () + 1); // +1 for the dot
        }
        listOfModuleRefsComesBeforeDef.add (getModuleRefObject (project, modulePrefix, fileToIndex, beginLine, typeBeginCol));
      }
      else {
        typeBeginCol = beginCol;
      }
      listOfElementRefsComesBeforeDef.add (getElementRefObject (referencedByElemID, project, modulePrefix, ElementType.DICTIONARY, refElemName, fileToIndex.getName (), beginLine, beginCol));
    }else{
      if (true == dictName.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))) {
        if(IndexerUtil.isAQLKeyword(modulePrefix)||(StringUtils.isEnclosedInDoublequotes (modulePrefix))){
          typeBeginCol = beginCol - (modulePrefix.length () + 1 + 2); // +1 for the dot  // +2 for double quotes
        }else{
          typeBeginCol = beginCol - (modulePrefix.length () + 1); // +1 for the dot
        }
        listOfModuleRefsComesBeforeDef.add (getModuleRefObject (project, modulePrefix, fileToIndex, beginLine, typeBeginCol));
      }
      else {
        typeBeginCol = beginCol;
      }
      listOfElementRefsComesBeforeDef.add (getElementRefObject (referencedByElemID, project, modulePrefix, ElementType.DICTIONARY, refElemName, fileToIndex.getName (), beginLine, beginCol));
    }
  }
  
  
  /**
   * @param referencedByElemID
   * @param selectListNode
   * @throws Exception
   */
  private void pickElemRefsInSelectListNode (Integer referencedByElemID, SelectListNode selectListNode) throws Exception
  {
    for (int i = 0; i < selectListNode.size (); ++i) {
      SelectListItemNode selListItemNode = selectListNode.get (i);
      pickElemRefsInSelectListItemNode (referencedByElemID, selListItemNode);
    }
  }

  /**
   * @param referencedByElemID
   * @param selListItemNode
   * @throws Exception
   */
  private void pickElemRefsInSelectListItemNode (Integer referencedByElemID, SelectListItemNode selListItemNode) throws Exception
  {
    try {
      RValueNode rvalNode = selListItemNode.getValue ();
      pickElemRefsInRValueNode (referencedByElemID, rvalNode);
    }
    catch (ParseException e) {
      // do nothing.
    }

  }

  /**
   * @param referencedByElemID
   * @param funcNode
   * @throws Exception
   */
  private void pickElemRefsInFunctionNode (Integer referencedByElemID, ScalarFnCallNode funcNode) throws Exception
  {
    if ((validElemRefs.functionDefs.contains (funcNode.getFuncName ()))||(validElemRefs.importedFunctions.contains (funcNode.getFuncName ()))) {
      pickElemRefsInNickNode (referencedByElemID, funcNode.getFuncNameNode (), ElementType.FUNCTION);
    }else if(!(ProjectUtils.isAQLKeyword (funcNode.getFuncName ()))){ 
      trackElemRefsComesBeforeElemDef (referencedByElemID, funcNode.getFuncNameNode (), ElementType.FUNCTION);
    }
    ArrayList<RValueNode> args = funcNode.getArgs ();
    for (RValueNode arg : args) {
      pickElemRefsInRValueNode (referencedByElemID, arg);
    }
  }

  private void pickElemRefsInRValueNode (Integer referencedByElemID, RValueNode node) throws Exception
  {
    if (node instanceof ColNameNode) {
      pickElemRefsInColNameNode (referencedByElemID, (ColNameNode) node);
    }
    else if (node instanceof ScalarFnCallNode) {
      pickElemRefsInFunctionNode (referencedByElemID, (ScalarFnCallNode) node);
    }
    else if (node instanceof NickNode) {
      NickNode nickNode = (NickNode) node;

      // prepare qualified name for nick node, if not already qualified
      String qualifiedName = nickNode.getNickname ();
      if (false == qualifiedName.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))) {
        qualifiedName = ModuleUtils.prepareQualifiedName (this.moduleName, qualifiedName);
      }

      ElementType type = detectElemType (qualifiedName);
      if (type != ElementType.UNKNOWN) {
        pickElemRefsInNickNode (referencedByElemID, nickNode, type);
      }
    }
    else if (node instanceof StringNode) {
      StringNode strNode = (StringNode) node;
      Token token = strNode.getOrigTok ();
      //pickElemRefsInDictStringNodeWithSingleQuote (referencedByElemID, strNode, token);
      pickElemRefsInDictStringNode (referencedByElemID, strNode, token);
    }
  }

  private void pickElemRefsInColNameNode (Integer referencedByElemID, ColNameNode colNode) throws Exception
  {
    String tabName = colNode.getTabname ();
    // Need not index this node, since the colNode.getTabnameTok for this node will be null as well.
    if (tabName == null) 
      return;
    Token tabTok = colNode.getTabnameTok ();
    String elemName = tabName;
    int beginCol = tabTok.beginColumn;
    ElementType type = detectElemType (elemName);
    if (type == ElementType.UNKNOWN) { return; // it is most likely an alias reference
    }
    String refModuleName = this.moduleName;
    if (tabName.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))) {
      String[] components = splitQualifiedName (tabName, this.moduleName);
      refModuleName = components[0];
      elemName = components[1];
      int moduleBeginCol = 0;
      if(IndexerUtil.isAQLKeyword(refModuleName)){
        moduleBeginCol = beginCol - (refModuleName.length () + 1 + 2); // +1 for the dot  // +2 for double quotes  
      }else{
        moduleBeginCol = beginCol - (refModuleName.length () + 1); // +1 to skip single quote
      }

      String refProject = detectProject (refModuleName, type, elemName);
      addModuleReference (refProject, refModuleName, fileToIndex, tabTok.beginLine, moduleBeginCol);
      addElementReference (referencedByElemID, refProject, refModuleName, type, elemName, fileToIndex.getName (),
        tabTok.beginLine, beginCol);
    }
    else {
      String refProject = detectProject (refModuleName, type, elemName);
      addElementReference (referencedByElemID, refProject, refModuleName, type, elemName, fileToIndex.getName (),
        tabTok.beginLine, tabTok.beginColumn);
    }
  }

  private ElementType detectElemType (String elementName) throws ElementTypeDeterminationException
  {
    if (validElemRefs.viewDefs.contains (elementName) || validElemRefs.importedViews.contains (elementName)) {
      return ElementType.VIEW;
    }

    else if (validElemRefs.tableDefs.contains (elementName) || validElemRefs.importedTables.contains (elementName)) {
      return ElementType.TABLE;
    }

    else if (validElemRefs.functionDefs.contains (elementName)
        || validElemRefs.importedFunctions.contains (elementName)) {
      return ElementType.FUNCTION;
    }
    else if (validElemRefs.inlineAndTableDicts.contains (elementName)
        || validElemRefs.importedDicts.contains (elementName)) { return ElementType.DICTIONARY; }

    return ElementType.UNKNOWN;
  }

  /**
   * @param referencedByElemID
   * @param fromList
   * @throws Exception
   */
  private void pickElemRefsInFromListNode (Integer referencedByElemID, FromListNode fromList) throws Exception
  {
    for (int i = 0; i < fromList.size (); ++i) {
      FromListItemNode fromListItemNode = fromList.get (i);
      pickElemRefsInFromListItemNode (referencedByElemID, fromListItemNode);
    }
  }

  /**
   * @param referencedByElemID
   * @param whereClauseNode
   * @throws Exception
   */
  private void pickElemRefsInWhereClauseNode (Integer referencedByElemID, WhereClauseNode whereClauseNode) throws Exception
  {
    if (whereClauseNode == null) { return; }

    ArrayList<PredicateNode> predNodes = whereClauseNode.getPreds ();
    if (predNodes == null || predNodes.size () == 0) { return; }

    for (PredicateNode predNode : predNodes) {
      pickElemRefsInPredicateNode (referencedByElemID, predNode);
    }
  }

  /**
   * @param referencedByElemID
   * @param predNode
   * @throws Exception
   */
  private void pickElemRefsInPredicateNode (Integer referencedByElemID, PredicateNode predNode) throws Exception
  {
    ScalarFnCallNode funcNode = predNode.getFunc ();
    pickElemRefsInFunctionNode (referencedByElemID, funcNode);
  }

  /**
   * @param node
   */
  private void indexCreateTableNode (CreateTableNode node)
  {
    ElementType type = ElementType.TABLE;
    String elemName = node.getUnqualifiedName ();

    // Pass 0: add to tableDefs
    validElemRefs.tableDefs.add (node.getTableName ()); // fully qualified name

    Token token = node.getTableNameNode ().getOrigTok ();
    indexElementDefinition (type, elemName, token);
  }

  /**
   * @param node
   */
  private void indexCreateDictNode (CreateDictNode node)
  {

    ElementType type = ElementType.DICTIONARY;
    String elemName = node.getUnqualifiedName ();
    Token token = node.getDictNameNode ().getOrigTok ();
    if (node instanceof CreateDictNode.FromTable || node instanceof CreateDictNode.Inline) {
      indexElementDefinition (type, elemName, token);

      // remember all inline and table based dictionaries encountered so far
      validElemRefs.inlineAndTableDicts.add (elemName);
    }
    else if (node instanceof CreateDictNode.FromFile) {

      indexElementDefinition (type, elemName, token);
      // TODO: add a file reference
      validElemRefs.dictFileReferences.add (elemName);
    }
  }

  /**
   * @param type
   * @param elemName
   * @param token
   */
  private ElementDefinition indexElementDefinition (ElementType type, String elemName, Token token)
  {
    String file = fileToIndex.getName ();

    int beginLine = token.beginLine;
    int beginColumn = token.beginColumn;

    return addElementDefinition (projectName, moduleName, type, elemName, file, beginLine, beginColumn);
  }

  /**
   * @param node
   */
  private void indexImportNode (AbstractImportNode node)
  {
    NickNode elementNameNode = node.getNodeName ();
    NickNode fromModuleNode = node.getFromModule ();
    ElementType elemType = getElementType (node);

    //Index the import statement, noting reference to module
    indexModuleNode (fromModuleNode);
    
    //Index the import statement, noting reference to imported element
    indexNonCreateStmtElementReference (fromModuleNode.getNickname (), elementNameNode, elemType);

    // add imported inline and table based dicts to inlineAndTableDicts list
    if (node instanceof ImportDictNode) {      
      if(node.getAlias ()!= null) {
        //Index the import statement, creating an element definition for the alias.
        //Now, references to the alias in other statements will point to this element definition.
        indexImportNodeAlias(node, ElementType.DICTIONARY); 
      }
      else { //add name to object tracking known imported elements.
        validElemRefs.inlineAndTableDicts.add (ModuleUtils.prepareQualifiedName (fromModuleNode.getNickname (),
          elementNameNode.getNickname ()));
      }
    }
    else if (node instanceof ImportViewNode) {
      //Index the import statement, creating an element definition for the alias.
      //Now, references to the alias in other statements will point to this element definition.
      if(node.getAlias ()!= null) {
        indexImportNodeAlias(node, ElementType.VIEW);
      }
      else { //add name to object tracking known imported elements.
        validElemRefs.importedViews.add (ModuleUtils.prepareQualifiedName (fromModuleNode.getNickname (),
          elementNameNode.getNickname ()));
      }

    }
    else if (node instanceof ImportTableNode) {
      //Index the import statement, creating an element definition for the alias.
      //Now, references to the alias in other statements will point to this element definition.
      if(node.getAlias ()!= null) {
        indexImportNodeAlias(node, ElementType.TABLE);
      }
      else { //add name to object tracking known imported elements.
        validElemRefs.importedTables.add (ModuleUtils.prepareQualifiedName (fromModuleNode.getNickname (),
          elementNameNode.getNickname ()));
      }

    }
    else if (node instanceof ImportFuncNode) {
      //Index the import statement, creating an element definition for the alias.
      //Now, references to the alias in other statements will point to this element definition.
      if(node.getAlias ()!= null) {
        indexImportNodeAlias(node, ElementType.FUNCTION);
      }
      else { //add name to object tracking known imported elements.
        validElemRefs.importedFunctions.add (ModuleUtils.prepareQualifiedName (fromModuleNode.getNickname (),
          elementNameNode.getNickname ()));
      }

    }
  }
  
  /**
   * If an import node has an alias, index the alias as an element definition
   * 
   * @param node
   * @param type
   */
  private void indexImportNodeAlias (AbstractImportNode node, ElementType type)
  {
    if (node.getAlias () != null) {
      String aliasName = node.getAlias ().getNickname ();
      String elemName = ModuleUtils.prepareQualifiedName (moduleName, aliasName);
      Token token = node.getAlias ().getOrigTok ();
      switch (type) {
        case DICTIONARY:
          validElemRefs.inlineAndTableDicts.add (elemName);
        break;
        case VIEW:
          validElemRefs.viewDefs.add (elemName);
        break;
        case TABLE:
          validElemRefs.tableDefs.add (elemName);
        break;
        case FUNCTION:
          validElemRefs.functionDefs.add (elemName);
        break;
        default:
          return; // if type is not one one of the above, do nothing and return.
      }
      indexElementDefinition (type, aliasName, token); // Index the declaration of alias as an element definition
      // Now, if user presses f3 on a reference to an alias, control will go to the line containing the import statement
      // that declared that alias.
    }
  }

  /**
   * Indexes element references that are NOT contained within Create statements
   * 
   * @param moduleName
   * @param elementNameNode
   * @param elemType
   */
  private void indexNonCreateStmtElementReference (String moduleName, NickNode elementNameNode, ElementType elemType)
  {
    String elemName = elementNameNode.getNickname ();
    String file = fileToIndex.getName ();
    Token token = elementNameNode.getOrigTok ();
    int beginLine = token.beginLine;
    int beginColumn = token.beginColumn;

    String project = null;
    try {
      project = detectProject (moduleName);
    }
    catch (ProjectDeterminationException e) {
      // just return if we can't determine the project
      return;
    }

    addElementReference (null, project, moduleName, elemType, elemName, file, beginLine, beginColumn);

  }

  /**
   * @param node
   * @return
   */
  private ElementType getElementType (AbstractImportNode node)
  {
    if (node instanceof ImportViewNode) {
      return ElementType.VIEW;
    }
    else if (node instanceof ImportDictNode) {
      return ElementType.DICTIONARY;
    }
    else if (node instanceof ImportTableNode) {
      return ElementType.TABLE;
    }
    else if (node instanceof ImportFuncNode) { return ElementType.FUNCTION; }

    throw new RuntimeException ("Unknown element type for class: " + node.getClass ().getName ());
  }

  /**
   * @param exportNode
   * @return
   */
  private ElementType getElementType (AbstractExportNode exportNode)
  {
    if (exportNode instanceof ExportViewNode) {
      return ElementType.VIEW;
    }
    else if (exportNode instanceof ExportDictNode) {
      return ElementType.DICTIONARY;
    }
    else if (exportNode instanceof ExportTableNode) {
      return ElementType.TABLE;
    }
    else if (exportNode instanceof ExportFuncNode) { return ElementType.FUNCTION; }

    throw new RuntimeException ("Unknown element type for class: " + exportNode.getClass ().getName ());

  }

  private void indexModuleNode (NickNode moduleNameNickNode)
  {
    String moduleName = moduleNameNickNode.getNickname ();
    IFile file = fileToIndex;
    Token token = moduleNameNickNode.getOrigTok ();
    int beginLine = token.beginLine;
    int beginColumn = token.beginColumn;

    try {
      detectProject (moduleName);
      String projName = detectProject (moduleName);
      addModuleReference (projName, moduleName, file, beginLine, beginColumn);
    }
    catch (ProjectDeterminationException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (e.getMessage ());
    }
  }

  /**
   * Adds an element definition to the Element Cache
   * 
   * @param project Name of the project where element is defined.
   * @param module Name of the module where element is defined.
   * @param type Type of the element being defined.
   * @param elemName name of the element
   * @param fileName File where element is defined
   * @param beginLine Begin Line number for element name token in element definition
   * @param endLine End Line number for element name token in element definition
   * @param beginColumn Begin Column number for element name token in element definition
   * @param endColumn End Column number for element name token in element definition
   * @return ElementDefinition object that was added to the cache
   */
  protected ElementDefinition addElementDefinition (String project, String module, ElementType type, String elemName,
    String fileName, int beginLine, int beginColumn)
  {

    // Add element defintion

    Integer projectId = projectCache.getProjectId (project);
    Integer moduleId = moduleCache.getModuleId (project, module);
    Integer elementId = elemCache.getElementId (project, module, type, elemName);
    Integer fileId = fileCache.getAQLFileId (project, module, fileName);

    int offset = calculateOffset (beginLine, beginColumn);
    ElementLocation location = new ElementLocation (fileId, offset);
    ElementDefinition elemDef = new ElementDefinition (projectId, moduleId, elementId, type, location);
    String qualifiedName = idManager.createQualifiedKey (type.toString (), project, module, elemName);
    elemCache.addElementDefintion (elemDef, qualifiedName);

    return elemDef;
  }

  /**
   * @param referencedByElemID
   * @param project
   * @param module
   * @param type
   * @param elemName
   * @param file
   * @param beginLine
   * @param beginColumn
   * @return
   */
  protected ElementReference addElementReference (Integer referencedByElemID, String project, String module,
    ElementType type, String elemName, String file, int beginLine, int beginColumn)
  {
    Integer elementId = elemCache.getElementId (project, module, type, elemName);
    Integer fileId = fileCache.getAQLFileId (fileToIndex);

    // Create ElementReference
    Integer elemRefId = IDManager.getInstance ().generateNextSequenceId ();
    int offset = calculateOffset (beginLine, beginColumn);
    ElementLocation location = new ElementLocation (fileId, offset);
    ElementReference elemRef = new ElementReference (elemRefId, elementId, location);

    // add it to elementCache
    elemCache.addElementReference (elemRef, referencedByElemID);

    return elemRef;
  }

}

/**
 * This class is used to maintain the Module Reference data for all the AQL elements which comes 
 * before than their actual element definitions in AQL file. It contains all the data required for module 
 * references. These references will be added to actual module cache after indexing the file.
 * 
 *  Kalakuntla
 *
 */
class ModuleRef{

  private String project;   // Holds the project name of the module referenced by module attribute
  private String module;    // Hold the actual module name
  private IFile file;       // Holds the AQL file instance where this module reference exists
  private int beginLine;    // Holds the begin line number of the module starting offset in file.
  private int beginColumn;  // Holds the begin column number of the element starting offset in file.
  /**
   * @return the project
   */
  public String getProject ()
  {
    return project;
  }
  /**
   * @param project the project to set
   */
  public void setProject (String project)
  {
    this.project = project;
  }
  /**
   * @return the module
   */
  public String getModule ()
  {
    return module;
  }
  /**
   * @param module the module to set
   */
  public void setModule (String module)
  {
    this.module = module;
  }
  /**
   * @return the file
   */
  public IFile getFile ()
  {
    return file;
  }
  /**
   * @param file the file to set
   */
  public void setFile (IFile file)
  {
    this.file = file;
  }
  /**
   * @return the beginLine
   */
  public int getBeginLine ()
  {
    return beginLine;
  }
  /**
   * @param beginLine the beginLine to set
   */
  public void setBeginLine (int beginLine)
  {
    this.beginLine = beginLine;
  }
  /**
   * @return the beginColumn
   */
  public int getBeginColumn ()
  {
    return beginColumn;
  }
  /**
   * @param beginColumn the beginColumn to set
   */
  public void setBeginColumn (int beginColumn)
  {
    this.beginColumn = beginColumn;
  }


}

/**
 * This class is used to maintain the Element Reference data for all the AQL elements which comes 
 * before than their actual element definitions in AQL file. It contains all the data required for element 
 * references. These references will be added to actual element cache after indexing the file.
 * 
 *  Kalakuntla
 *
 */
class ElementRef{

  private Integer refByElemID;     // Holds the ID of the element that references the element identified by 'elementName' attribute
  private String project;          // Holds the project name of the element identified by 'elementName' attribute
  private String module;           // Hold the module name of the element  
  private ElementType elementType;  // Holds the Element Type of the element  
  private String elementName;      // Holds the actual element name  
  private String file;             // Hold the file name which contains the actual element
  private int beginLine;            // Holds the begin line number of the element starting offset in file.
  private int beginColumn;        // Holds the begin column number of the element starting offset in file.

  /**
   * @return the refByElemID
   */
  public Integer getRefByElemID ()
  {
    return refByElemID;
  }
  /**
   * @param refByElemID the refByElemID to set
   */
  public void setRefByElemID (Integer refByElemID)
  {
    this.refByElemID = refByElemID;
  }
  /**
   * @return the project
   */
  public String getProject ()
  {
    return project;
  }
  /**
   * @param project the project to set
   */
  public void setProject (String project)
  {
    this.project = project;
  }
  /**
   * @return the module
   */
  public String getModule ()
  {
    return module;
  }
  /**
   * @param module the module to set
   */
  public void setModule (String module)
  {
    this.module = module;
  }
  /**
   * @return the elementType
   */
  public ElementType getElementType ()
  {
    return elementType;
  }
  /**
   * @param elementType the elementType to set
   */
  public void setElementType (ElementType elementType)
  {
    this.elementType = elementType;
  }
  /**
   * @return the elementName
   */
  public String getElementName ()
  {
    return elementName;
  }
  /**
   * @param elementName the elementName to set
   */
  public void setElementName (String elementName)
  {
    this.elementName = elementName;
  }
  /**
   * @return the file
   */
  public String getFile ()
  {
    return file;
  }
  /**
   * @param file the file to set
   */
  public void setFile (String file)
  {
    this.file = file;
  }
  /**
   * @return the beginLine
   */
  public int getBeginLine ()
  {
    return beginLine;
  }
  /**
   * @param beginLine the beginLine to set
   */
  public void setBeginLine (int beginLine)
  {
    this.beginLine = beginLine;
  }
  /**
   * @return the beginColumn
   */
  public int getBeginColumn ()
  {
    return beginColumn;
  }
  /**
   * @param beginColumn the beginColumn to set
   */
  public void setBeginColumn (int beginColumn)
  {
    this.beginColumn = beginColumn;
  }
}
