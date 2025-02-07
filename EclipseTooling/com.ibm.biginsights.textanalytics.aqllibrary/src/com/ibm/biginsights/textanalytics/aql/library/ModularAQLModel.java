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
package com.ibm.biginsights.textanalytics.aql.library;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.aql.AQLParseTreeNode;
import com.ibm.avatar.aql.CreateDictNode;
import com.ibm.avatar.aql.CreateExternalViewNode;
import com.ibm.avatar.aql.CreateFunctionNode;
import com.ibm.avatar.aql.CreateTableNode;
import com.ibm.avatar.aql.CreateViewNode;
import com.ibm.avatar.aql.DetagDocNode;
import com.ibm.avatar.aql.ExportDictNode;
import com.ibm.avatar.aql.ExportFuncNode;
import com.ibm.avatar.aql.ExportTableNode;
import com.ibm.avatar.aql.ExportViewNode;
import com.ibm.avatar.aql.ImportDictNode;
import com.ibm.avatar.aql.ImportFuncNode;
import com.ibm.avatar.aql.ImportModuleNode;
import com.ibm.avatar.aql.ImportTableNode;
import com.ibm.avatar.aql.ImportViewNode;
import com.ibm.avatar.aql.IncludeFileNode;
import com.ibm.avatar.aql.ModuleNode;
import com.ibm.avatar.aql.NickNode;
import com.ibm.avatar.aql.OutputViewNode;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.RequireColumnsNode;
import com.ibm.avatar.aql.StatementList;
import com.ibm.avatar.aql.Token;
import com.ibm.avatar.aql.ViewBodyNode;
import com.ibm.avatar.aql.catalog.Catalog;
import com.ibm.avatar.aql.doc.AQLDocComment;
import com.ibm.biginsights.textanalytics.aqllibrary.Activator;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 *  Kalakuntla
 * This class is to represent the AQLModel for modular projects.
 * 
 */
public class ModularAQLModel { 
	@SuppressWarnings("unused")


  private HashMap<String, AQLModuleProject> aqlLib;
  private String DOC_SCHEMA = "DocumentSchema";  //$NON-NLS-1$

  final String SELECT_STRING = "SELECT"; //$NON-NLS-1$ 
  final String VIEW_STRING ="VIEW"; //$NON-NLS-1$
  final String EXT_VIEW_STRING ="EXTERNAL_VIEW"; //$NON-NLS-1$
  final String DICT_STRING ="DICTIONARY"; //$NON-NLS-1$
  final String EXT_DICT_STRING ="EXTERNAL_DICTIONARY"; //$NON-NLS-1$
  final String FUNC_STRING ="FUNCTION"; //$NON-NLS-1$
  final String TABLE_STRING ="TABLE"; //$NON-NLS-1$
  final String EXT_TABLE_STRING ="EXTERNAL_TABLE"; //$NON-NLS-1$
  final String INCLUDE_STRING ="INCLUDE"; //$NON-NLS-1$
  final String OUTPUT_VIEW_STRING = "OUTPUT_VIEW"; //$NON-NLS-1$
  final String DETAG_STRING ="DETAG"; //$NON-NLS-1$
  final String MODULE_STRING = "MODULE"; //$NON-NLS-1$
  final String REQUIRE_DOCUMENT_STRING = "REQUIRE_DOCUMENT"; //$NON-NLS-1$
  final String IMPORT_MODULE_STRING = "IMPORT_MODULE"; //$NON-NLS-1$
  final String IMPORT_VIEW_STRING = "IMPORT_VIEW"; //$NON-NLS-1$
  final String IMPORT_DICT_STRING = "IMPORT_DICTIONARY"; //$NON-NLS-1$
  final String IMPORT_FUNC_STRING = "IMPORT_FUNCTION"; //$NON-NLS-1$
  final String IMPORT_TABLE_STRING = "IMPORT_TABLE"; //$NON-NLS-1$
  final String EXPORT_FUNC_STRING = "EXPORT_FUNCTION"; //$NON-NLS-1$
  final String EXPORT_DICT_STRING = "EXPORT_DICTIONARY"; //$NON-NLS-1$
  final String EXPORT_TABLE_STRING = "EXPORT_TABLE"; //$NON-NLS-1$
  final String EXPORT_VIEW_STRING = "EXPORT_VIEW"; //$NON-NLS-1$
  
  private static final ILog logger = LogUtil.getLogForPlugin(Activator.PLUGIN_ID);
  
  private static Pattern PATTERN_CLEANER_LEADING_ASTERISK;
  private static Pattern PATTERN_CLEANER_CR;
  static {
    PATTERN_CLEANER_LEADING_ASTERISK = Pattern.compile ("^\\s*\\*", Pattern.MULTILINE);
    PATTERN_CLEANER_CR = Pattern.compile ("\r+");
  }

  /** Leading comment separator */
  private static final String LEADING_COMMENT_SEP = "/**";
  /** Trailing comment separator */
  private static final String TRAILING_COMMENT_SEP = "*/";

  
  AQLDocComment comment;
  IProject project;
  
  public ModularAQLModel(IAQLLibrary aqlLibrary) {
    this.aqlLib = aqlLibrary.getModuleLibraryMap ();
  }

  // This method is used to update the library when things get changes in AQL Editor..
  public void update(String filePath, String prjName, String moduleName, StatementList stList) {
    if (filePath != null && prjName != null && stList != null) {
      //aqlLib = Activator.getModularLibrary().getModuleLibraryMap(); 
      updateLibrary(filePath, prjName, moduleName, stList);
    } 
  }

  private void updateLibrary(String filePath, String prjName, String moduleName, StatementList stList) {
    // Assumed that library exist as it was created during load
    // get the appropriate file and recreate elements in it.
    if (!isProjectExist(prjName)) {
      // Dont do anything
    }
    // project exist in library
    else {
      AQLModuleProject aqlProject = aqlLib.get(prjName);
      if (!isModuleExist (prjName, moduleName)) {
        // Dont do anything
      }else{
        AQLModule module = aqlProject.getAQLModules().get(moduleName);
        if (isAQLFileExist(filePath, aqlProject, module)) {
          // file already exist, get the file
          AQLFile file = getAQLFile(filePath, aqlProject, module);
          file.deleteAllElements();
          module.deleteAQLFile (file);
          createAQLFile(aqlProject, module, filePath);
          AQLFile file1 = getAQLFile(filePath, aqlProject, module);
          createElement(file1, stList);
        } else {
          // Dont do anything 
        }
      }
    }
  }

  //  This is to create and load the AQl library for modular projects..
  public void create(String aqlFilePath, String prjName, String moduleName, StatementList statementList) {
    if (aqlFilePath != null && prjName != null && moduleName != null && statementList != null) {
      //aqlLib = Activator.getModularLibrary().getModuleLibraryMap ();
      createLibrary(aqlFilePath, prjName, moduleName, statementList);
    } else {
      // Dont do anything
    }
  }

  private void createLibrary(String aqlFilePath, String prjName, String moduleName, StatementList statementList) {
    // if project does not exist in the library, create it.
    if (!isProjectExist(prjName)) {
      // if project does not exist, create project, file and elements.
      createAQLProject(prjName, moduleName);
      // After creating the project, create AQL files and elements in it
      AQLModuleProject project = aqlLib.get(prjName);
      if(!isModuleExist(prjName, moduleName)){
        createAQLModule(prjName, moduleName);
      }
      AQLModule module = project.getAQLModules().get(moduleName);
      if(!isAQLFileExist(aqlFilePath, project, module)){
        createAQLFile(project, module, aqlFilePath);
        AQLFile file = getAQLFile(aqlFilePath, project, module);
        createElement(file, statementList);
      }
    }
    // project exist in library, get it and append child to it
    else {
      // If project already exist, then work with it..
      AQLModuleProject project = aqlLib.get(prjName);
      if(!isModuleExist(prjName, moduleName)){
        createAQLModule(prjName, moduleName);
      }
      AQLModule module = project.getAQLModules().get(moduleName);
      if (isAQLFileExist(aqlFilePath, project, module)) {
        // file already exist, get the file
        AQLFile file = getAQLFile(aqlFilePath, project, module);
        file.deleteAllElements();
        createElement(file, statementList);
      } else {
        createAQLFile(project, module, aqlFilePath);
        AQLFile file = getAQLFile(aqlFilePath, project, module);
        createElement(file, statementList);
      }
    }
  }
  // This method is to create the AQLFile within the AQL Module of modular project.
  private void createAQLFile(AQLModuleProject aqlProject, AQLModule module, String aqlFilePath) {
    // Create aql file within the project that is created
    AQLFile aqlFile = new AQLFile();
    aqlFile.filePath = aqlFilePath;
    module.addFile(aqlFile);  // This automatically adds the file within the module of the project..
  }
  //This method is to create the AQlElements within from an AQLFile 
  private void createElement(AQLFile aqlFile, StatementList statementList) {
    for (int i = 0; i < statementList.getParseTreeNodes().size(); i++) {
      AQLParseTreeNode parseNode = statementList.getParseTreeNodes().get(i);
      // get all values from the statement list node.
      AQLElement element = null;
      if(parseNode instanceof CreateViewNode)
      {
        //check if its a view or select node
        //checking for select node
        if(((CreateViewNode) parseNode).getIsOutput() == true)
        {
          CreateViewNode viewNode = (CreateViewNode) parseNode;
          element = new Select();
          element.dependsOnElement = getDeps (viewNode.getBody ());
          element.type = SELECT_STRING;
          element.filePath = aqlFile.filePath;
          element.name = viewNode.getViewNameNode().getNickname();
          comment = viewNode.getComment();
          if (comment != null)
        	  element.comment = comment.getCleanText();
          else
        	  element.comment = "";
          //element.name = ((CreateViewNode) parseNode).getViewName ();
          element.unQualifiedName = viewNode.getUnqualifiedName ();
          element.moduleName = viewNode.getModuleName ();
          Token token = viewNode.getViewNameNode().getOrigTok();
          element.beginLineNumber = token.beginLine;
          element.endLineNumber = token.endLine;
          element.beginOffset = token.beginColumn;
          element.endOffset = token.endColumn;
        }
        //checking for view node
        else
        {
          CreateViewNode viewNode = (CreateViewNode) parseNode;
          element = new View();
          element.dependsOnElement = getDeps (viewNode.getBody ());
          element.type = VIEW_STRING;
          element.filePath = aqlFile.filePath;
          element.name = viewNode.getViewNameNode().getNickname();
          comment = viewNode.getComment();
          if (comment != null)
        	  element.comment = comment.getCleanText();
          else
        	  element.comment = "";
          //element.name = ((CreateViewNode) parseNode).getViewName ();
          element.unQualifiedName = viewNode.getUnqualifiedName ();
          element.moduleName = viewNode.getModuleName ();
          Token token = viewNode.getViewNameNode().getOrigTok();
          element.beginLineNumber = token.beginLine;
          element.endLineNumber = token.endLine;
          element.beginOffset = token.beginColumn;
          element.endOffset = token.endColumn;
        }
        // store dependent views can be used for extract nodes ..
        //          if(((CreateViewNode) parseNode).getBody().getOrigTok().toString() == "extract")
        //          {
        //            SubElement sElement = new SubElement();
        //            sElement.name = ((CreateViewNode) parseNode).getBody().getOrigTok().getValue().toString();
        //            sElement.beginLineNumber = ((CreateViewNode) parseNode).getBody().getOrigTok().beginLine;
        //            sElement.endLineNumber = ((CreateViewNode) parseNode).getBody().getOrigTok().endLine;
        //            sElement.beginOffset = ((CreateViewNode) parseNode).getBody().getOrigTok().beginColumn;
        //            sElement.endOffset = ((CreateViewNode) parseNode).getBody().getOrigTok().endColumn;
        //            System.out.println("VIEW EXTRACT: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
        //          }
        //          else if(((CreateViewNode) parseNode).getBody().getOrigTok().toString() == "select")
        //          {
        //            SubElement sElement = new SubElement();
        //            sElement.name = ((CreateViewNode) parseNode).getBody().getOrigTok().getValue().toString();
        //            sElement.beginLineNumber = ((CreateViewNode) parseNode).getBody().getOrigTok().beginLine;
        //            sElement.endLineNumber = ((CreateViewNode) parseNode).getBody().getOrigTok().endLine;
        //            sElement.beginOffset = ((CreateViewNode) parseNode).getBody().getOrigTok().beginColumn;
        //            sElement.endOffset = ((CreateViewNode) parseNode).getBody().getOrigTok().endColumn;
        //            System.out.println("VIEW SELECT: " + sElement.name + " " + sElement.beginLineNumber + " " + sElement.endLineNumber + " " + sElement.beginOffset + " " + sElement.endOffset);
        //          }         
      }
      else if(parseNode instanceof CreateDictNode)
      { 
        CreateDictNode dictNode = (CreateDictNode)parseNode;
        if(dictNode.getParams ().getIsExternal ()== false){
          element = new Dictionary();
          element.type = DICT_STRING;
          element.filePath = aqlFile.filePath;
          element.name = dictNode.getDictname();
          comment = dictNode.getComment();
          if (comment != null)
        	  element.comment = comment.getCleanText();
          else
        	  element.comment = "";
          element.unQualifiedName = dictNode.getUnqualifiedName ();
          element.moduleName = dictNode.getModuleName ();
          Token token = dictNode.getDictNameNode ().getOrigTok ();
          element.beginLineNumber = token.beginLine;
          element.endLineNumber = token.endLine;
          element.beginOffset = token.beginColumn;
          element.endOffset = token.endColumn;
        }else{
          element = new ExternalDictionary ();
          element.type = EXT_DICT_STRING;
          element.filePath = aqlFile.filePath;
          element.name = dictNode.getDictname();
          comment = dictNode.getComment();
          if (comment != null)
        	  element.comment = dictNode.getComment().getCleanText();
          else
        	  element.comment = "";
          element.unQualifiedName = dictNode.getUnqualifiedName ();
          element.moduleName = dictNode.getModuleName ();
          Token token = dictNode.getOrigTok();
          element.beginLineNumber = token.beginLine;
          element.endLineNumber = token.endLine;
          element.beginOffset = token.beginColumn;
          element.endOffset = token.endColumn;
          boolean required = ProjectUtils.isElementRequired ( ((CreateDictNode) parseNode).getParams ().isRequired (),
                                                              ((CreateDictNode) parseNode).getParams ().isAllowEmpty () );
          ((ExternalDictionary)element).setRequired(required);
        }
      }
      else if(parseNode instanceof CreateExternalViewNode)
      {
        CreateExternalViewNode extViewNode = ((CreateExternalViewNode) parseNode);
        element = new ExternalView();
        element.type = EXT_VIEW_STRING;
        element.filePath = aqlFile.filePath;
        element.name = extViewNode.getExternalViewName();
        comment = extViewNode.getComment();
        if(comment != null)
        	element.comment = extViewNode.getComment().getCleanText();
        else
        	element.comment = "";
        element.unQualifiedName = extViewNode.getUnqualifiedName ();
        element.moduleName = extViewNode.getModuleName ();
        Token token = extViewNode.getErrorTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof CreateFunctionNode)
      {
        CreateFunctionNode funcNode = ((CreateFunctionNode) parseNode);
        element = new Function();
        element.type = FUNC_STRING;
        element.filePath = aqlFile.filePath;
        element.name = funcNode.getFunctionName();
        comment = funcNode.getComment();
        if(comment != null)
        	element.comment = funcNode.getComment().getCleanText();
        else
        	element.comment = "";
        element.unQualifiedName = funcNode.getUnqualifiedName ();
        element.moduleName = funcNode.getModuleName ();
        Token token = funcNode.getFunctionNameNode ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof CreateTableNode)
      {
        CreateTableNode tabNode = ((CreateTableNode)parseNode);
        if(tabNode.getIsExternal ()== false)
        {
          element = new Table();
          element.type = TABLE_STRING;
          element.filePath = aqlFile.filePath;
          element.name = tabNode.getTableName();
          comment = tabNode.getComment();
          if(comment != null)
          	element.comment = tabNode.getComment().getCleanText();
          else
          	element.comment = "";
          element.unQualifiedName = tabNode.getUnqualifiedName ();
          element.moduleName = tabNode.getModuleName ();
          Token token = tabNode.getErrorTok ();
          element.beginLineNumber = token.beginLine;
          element.endLineNumber = token.endLine;
          element.beginOffset = token.beginColumn;
          element.endOffset = token.endColumn;
        }
        else{
          element = new ExternalTable();
          element.type = EXT_TABLE_STRING;
          element.filePath = aqlFile.filePath;
          element.name = tabNode.getTableName();
          comment = tabNode.getComment();
          if(comment != null)
            	element.comment = tabNode.getComment().getCleanText();
            else
            	element.comment = "";
          element.unQualifiedName = tabNode.getUnqualifiedName ();
          element.moduleName = tabNode.getModuleName ();
          Token token = tabNode.getErrorTok ();
          element.beginLineNumber = token.beginLine;
          element.endLineNumber = token.endLine;
          element.beginOffset = token.beginColumn;
          element.endOffset = token.endColumn;

          boolean required = ProjectUtils.isElementRequired ( ((CreateTableNode) parseNode).isRequired (), ((CreateTableNode) parseNode).isAllowEmpty () );
          ((ExternalTable)element).setRequired(required);
        }
      }
      else if(parseNode instanceof IncludeFileNode)
      {
        IncludeFileNode includeNode = ((IncludeFileNode) parseNode);
        element = new IncludedFile();
        element.type = INCLUDE_STRING;
        element.filePath = aqlFile.filePath;
        element.name = includeNode.getIncludedFileName().getStr();
        element.unQualifiedName = includeNode.getIncludedFileName().getStr();
        element.moduleName = includeNode.getModuleName ();
        Token token = includeNode.getOrigTok ();
        element.beginLineNumber = token.endLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof OutputViewNode)
      {
        OutputViewNode outputViewNode = ((OutputViewNode) parseNode);
        element = new OutputView();
        element.type = OUTPUT_VIEW_STRING;
        element.filePath = aqlFile.filePath;
        element.name = outputViewNode.getViewname().getNickname();
        element.unQualifiedName = outputViewNode.getUnqualifiedViewNameNickNode ().getNickname ();
        element.aliasName = outputViewNode.getAltnameStr ();
        element.moduleName = outputViewNode.getModuleName ();
        Token token = outputViewNode.getUnqualifiedViewNameNickNode ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof DetagDocNode)
      {
        DetagDocNode detagNode = ((DetagDocNode) parseNode);
        element = new Detag();
        element.type = DETAG_STRING;
        element.filePath = aqlFile.filePath;
        element.name = detagNode.getDetaggedDocName();
        comment = detagNode.getComment();
        if(comment != null)
          	element.comment = detagNode.getComment().getCleanText();
          else
          	element.comment = "";
        element.unQualifiedName = detagNode.getUnqualifiedDetaggedDocName ();
        element.moduleName = detagNode.getModuleName ();
        Token token = detagNode.getDetaggedDocNameNode ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof ModuleNode)
      {
        ModuleNode moduleNode = ((ModuleNode) parseNode);
        element = new Module();
        element.type = MODULE_STRING;
        element.filePath = aqlFile.filePath;
        element.name = moduleNode.getName ().getNickname ();
        element.unQualifiedName = moduleNode.getName ().getNickname ();
        element.moduleName = moduleNode.getModuleName ();
        Token token = moduleNode.getName ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof RequireColumnsNode)
      {
        RequireColumnsNode reqColNode = ((RequireColumnsNode) parseNode);
        element = new RequireDocument();
        element.type = REQUIRE_DOCUMENT_STRING;
        element.filePath = aqlFile.filePath;
        //element.name = reqColNode.getOrigFileName ();
        element.name = DOC_SCHEMA;
        //element.unQualifiedName = reqColNode.getOrigFileName ();
        element.moduleName = reqColNode.getModuleName ();
        Token token = reqColNode.getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
     // Calculating end off set differently for this element
        //element.endOffset = token.endColumn;
        element.endOffset = token.beginColumn + 28;  // Will highlight the entire require document with schema part
     // This is to fix the name of the Require document schema node in outline view
        element.unQualifiedName = ""+token.beginLine+token.beginColumn+"."+DOC_SCHEMA; 
      }
      else if(parseNode instanceof ImportModuleNode)
      {
        ImportModuleNode importModNode = ((ImportModuleNode) parseNode);
        element = new ImportModule();
        element.type = IMPORT_MODULE_STRING;
        element.filePath = aqlFile.filePath;
        element.name = importModNode.getImportedModuleName().getNickname();
        element.unQualifiedName = importModNode.getImportedModuleName().getNickname();
        element.moduleName = importModNode.getModuleName ();
        element.fromModuleName = importModNode.getImportedModuleName ().getNickname ();
        Token token = importModNode.getImportedModuleName ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof ImportViewNode)
      {
        ImportViewNode importViewNode = ((ImportViewNode) parseNode);
        element = new ImportView();
        element.type = IMPORT_VIEW_STRING;
        element.filePath = aqlFile.filePath;
        element.name = importViewNode.getNodeName ().getNickname ();
        element.unQualifiedName = importViewNode.getNodeName ().getNickname ();
        //element.unQualifiedName = ((ImportViewNode) parseNode).getNodeName ().getNickname ().split ("\\.")[1];
        NickNode aliasNode = importViewNode.getAlias ();
        if(aliasNode != null){
          element.aliasName = aliasNode.getNickname ();
        }
        element.moduleName = importViewNode.getModuleName ();
        element.fromModuleName = importViewNode.getFromModule ().getNickname ();
        Token token = importViewNode.getNodeName ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;

      }
      else if(parseNode instanceof ImportDictNode)
      {
        ImportDictNode importDictNode = ((ImportDictNode) parseNode); 
        element = new ImportDictionary();
        element.type = IMPORT_DICT_STRING;
        element.filePath = aqlFile.filePath;
        element.name = importDictNode.getNodeName ().getNickname ();
        element.unQualifiedName = importDictNode.getNodeName ().getNickname ();
        //element.unQualifiedName = ((ImportDictNode) parseNode).getNodeName ().getNickname ().split ("\\.")[1];
        NickNode aliasNode = importDictNode.getAlias ();
        if(aliasNode != null){
          element.aliasName = aliasNode.getNickname ();
        }
        element.moduleName = importDictNode.getModuleName ();
        element.fromModuleName = importDictNode.getFromModule ().getNickname ();
        Token token = importDictNode.getNodeName ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof ImportFuncNode)
      {
        ImportFuncNode importFuncNode = ((ImportFuncNode) parseNode);
        element = new ImportFunction();
        element.type = IMPORT_FUNC_STRING;
        element.filePath = aqlFile.filePath;
        element.name = importFuncNode.getNodeName ().getNickname ();
        element.unQualifiedName = importFuncNode.getNodeName ().getNickname ();
        //element.unQualifiedName = ((ImportFuncNode) parseNode).getNodeName ().getNickname ().split ("\\.")[1];
        NickNode aliasNode = importFuncNode.getAlias ();
        if(aliasNode != null){
          element.aliasName = aliasNode.getNickname ();
        }
        element.moduleName = importFuncNode.getModuleName ();
        element.fromModuleName = importFuncNode.getFromModule ().getNickname ();
        Token token = importFuncNode.getNodeName ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof ImportTableNode)
      {
        ImportTableNode importTableNode = ((ImportTableNode) parseNode);
        element = new ImportTabel();
        element.type = IMPORT_TABLE_STRING;
        element.filePath = aqlFile.filePath;
        element.name = importTableNode.getNodeName ().getNickname ();
        element.unQualifiedName = importTableNode.getNodeName ().getNickname ();
        NickNode aliasNode = importTableNode.getAlias ();
        if(aliasNode != null){
          element.aliasName = aliasNode.getNickname ();
        }
        element.moduleName = importTableNode.getModuleName ();
        element.fromModuleName = importTableNode.getFromModule ().getNickname ();
        Token token = importTableNode.getNodeName ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof ExportFuncNode)
      {
        ExportFuncNode expFuncNode = ((ExportFuncNode) parseNode);
        element = new ExportFunction();
        element.type = EXPORT_FUNC_STRING;
        element.filePath = aqlFile.filePath;
        element.name = expFuncNode.getNodeName ().getNickname ();
        element.unQualifiedName = expFuncNode.getUnqualifiedElemNameNickNode ().getNickname ();
        element.moduleName = expFuncNode.getModuleName ();
        Token token = expFuncNode.getNodeName ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof ExportDictNode)
      {
        ExportDictNode expDictNode = ((ExportDictNode) parseNode);
        element = new ExportDictionary();
        element.type = EXPORT_DICT_STRING;
        element.filePath = aqlFile.filePath;
        element.name = expDictNode.getNodeName ().getNickname ();
        element.unQualifiedName = expDictNode.getUnqualifiedElemNameNickNode ().getNickname ();
        element.moduleName = expDictNode.getModuleName ();
        Token token = expDictNode.getNodeName ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      else if(parseNode instanceof ExportTableNode)
      {
        ExportTableNode expTabNode = ((ExportTableNode) parseNode);
        element = new ExportTabel();
        element.type = EXPORT_TABLE_STRING;
        element.filePath = aqlFile.filePath;
        element.name = expTabNode.getNodeName ().getNickname ();
        element.unQualifiedName = expTabNode.getUnqualifiedElemNameNickNode ().getNickname ();
        element.moduleName = expTabNode.getModuleName ();
        Token token = expTabNode.getNodeName ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      } 
      else if(parseNode instanceof ExportViewNode)
      {
        ExportViewNode expViewNode = ((ExportViewNode) parseNode);
        element = new ExportView();
        element.type = EXPORT_VIEW_STRING;
        element.filePath = aqlFile.filePath;
        element.name = expViewNode.getNodeName ().getNickname ();
        element.unQualifiedName = expViewNode.getUnqualifiedElemNameNickNode ().getNickname ();
        element.moduleName = expViewNode.getModuleName ();
        Token token = expViewNode.getNodeName ().getOrigTok ();
        element.beginLineNumber = token.beginLine;
        element.endLineNumber = token.endLine;
        element.beginOffset = token.beginColumn;
        element.endOffset = token.endColumn;
      }
      // Add the element to AQl file instance..
      if(!(element == null))
        aqlFile.addElement(element);
    }
  }

  /**
   * This method returns the list of views that the specified view depends on
   * @param bodyNode ViewBodyNode of the view for which dependency is to be calculated 
   * @return list of views that the specified view depends on
   */
  private ArrayList<String> getDeps(ViewBodyNode bodyNode){
    Catalog catalog = new Catalog ();
    TreeSet<String> depElements = new TreeSet<String> ();

    try {
      bodyNode.getDeps (depElements, catalog);
    }
    catch (ParseException ex) {
      logger.logError (ex.getMessage ());
    }
    return new ArrayList<String>(depElements);
  }

  // Method to create the AQL modular project..
  private void createAQLProject(String projectName, String moduleName) {
    AQLModuleProject newProject = new AQLModuleProject();
    aqlLib.put(projectName, newProject);
  }
  //Method to create the AQL module within a project..  
  private void createAQLModule(String projectName, String moduleName) {
    AQLModuleProject project = aqlLib.get(projectName);
    AQLModule module = new AQLModule();
    module.setModuleName(moduleName);
    module.setComment(createModuleComment(moduleName));
    project.addModule (module);
    
   }
  
  private String createModuleComment(String modulePath) {
    File moduleCommentFile = new File (modulePath, "module.info");
    String moduleComment = null;
    try {
      if(moduleCommentFile.exists())
       moduleComment = FileUtils.fileToStr (moduleCommentFile, "UTF-8");
      
  } catch (Exception e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  } 
    if (null != moduleComment)
      moduleComment = cleanUpComment(moduleComment);
    return moduleComment;

  }
  
  public void updateModuleComment(String prjName, String moduleName) {
   if (!isProjectExist(prjName)) {
      // Dont do anything
    }
    // project exist in library
    else {
      AQLModuleProject aqlProject = aqlLib.get(prjName);
      if (!isModuleExist (prjName, moduleName)) {
        // Dont do anything
      }else{
        AQLModule module = aqlProject.getAQLModules().get(moduleName);
        String moduleComment;
        moduleComment = createModuleComment (moduleName);
        module.setComment (moduleComment); 
      }
    }
  }
  
  // will be used for refactoring the code..
  public void deleteAQLFile(String filePath, String prjName, String moduleName) {
    AQLModuleProject project = aqlLib.get(prjName);
    AQLModule module = project.getAQLModules().get(moduleName);
    AQLFile file = getAQLFile(filePath, project, module);
    module.deleteAQLFile(file);
  }

  // will be used for refactoring the code
  public void deleteAQLFiles(String prjName) {
    AQLModuleProject aqlProject = aqlLib.get(prjName); 
    HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
    String moduleName;
    Iterator<String> itr = modules.keySet().iterator();
    while (itr.hasNext()) {
      moduleName = itr.next();
      AQLModule mod = modules.get (moduleName);
      mod.deleteAllFiles ();
    }  
    aqlLib.get(prjName).deleteAllModules();
  }

  public void deleteAQLFiles(String prjName, String moduleName) {
    AQLModuleProject aqlProject = aqlLib.get(prjName); 
    AQLModule module = aqlProject.getAQLModules().get(moduleName);
    module.deleteAllFiles ();
    aqlLib.get(prjName).deleteAQLModule (module);
  }

  // will be used for refactoring
  public void deleteAQLProject(String prjName) {
    aqlLib.remove(prjName);
  }

  public AQLFile getAQLFile(String aqlFilePath, AQLModuleProject aqlProject, AQLModule module) {
    //HashMap<String, AQLModule> modules = aqlProject.getAQLModules ();
    List<AQLFile> aqlFiles = null;
    if(module != null){
      aqlFiles = module.getAQLFiles ();
    }
    // check if file given already exist in library under given project under given module
    if((aqlFiles != null)&&(aqlFiles.size() > 0)){
      for (int i = 0; i < aqlFiles.size(); i++) {
        if (aqlFiles.get(i).filePath.equals(aqlFilePath))
          // file exist, return the node
        {
          return aqlFiles.get(i);
        }
      }
    }
    return null;
  }

  private boolean isAQLFileExist(String aqlFilePath, AQLModuleProject aqlProject, AQLModule module) {
    List<AQLFile> aqlFiles = null; // = aqlProject.getAQLFiles();
    if (aqlFilePath != null && (aqlProject != null) && (module != null)) {
      aqlFiles = module.getAQLFiles ();  
    }
    // check if file given already exist in library under given project
    if((aqlFiles != null)&&(aqlFiles.size() > 0))
    {
      for (int i = 0; i < aqlFiles.size(); i++) {
        if (aqlFiles.get(i).filePath.equals(aqlFilePath))
        {
          return true; // file already exist
        }
      }
    }
    return false;
  }

  private boolean isProjectExist(String projectName) {
    if (aqlLib.get(projectName) != null) {
      return true; // project exists
    }
    return false;
  }

  private boolean isModuleExist(String projectName, String moduleName) {
    AQLModuleProject project = aqlLib.get(projectName);
    HashMap<String, AQLModule> moduleMap = project.getAQLModules ();
    //String existingMo = 
    if ((moduleMap != null)&&(moduleMap.get(moduleName) != null)) {
      return true; // module exists
    }
    return false;
  }
  // Removing the leading and trailing comment prefixes
  private String cleanUpComment(String cleanText){
	  if (cleanText.startsWith (LEADING_COMMENT_SEP)) cleanText = cleanText.substring (2);
      if (cleanText.endsWith (TRAILING_COMMENT_SEP)) cleanText = cleanText.substring (0, cleanText.length () - 2);
      Matcher m = PATTERN_CLEANER_LEADING_ASTERISK.matcher (cleanText);
      cleanText = m.replaceAll ("");
      m = PATTERN_CLEANER_CR.matcher (cleanText);
      cleanText = m.replaceAll ("");
	  return cleanText;
  }
  

} 
