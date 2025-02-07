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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import com.ibm.avatar.aql.AQLParseTreeNode;
import com.ibm.avatar.aql.CreateDictNode;
import com.ibm.avatar.aql.CreateExternalViewNode;
import com.ibm.avatar.aql.CreateFunctionNode;
import com.ibm.avatar.aql.CreateTableNode;
import com.ibm.avatar.aql.CreateViewNode;
import com.ibm.avatar.aql.DetagDocNode;
import com.ibm.avatar.aql.IncludeFileNode;
import com.ibm.avatar.aql.OutputViewNode;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.StatementList;
import com.ibm.avatar.aql.Token;
import com.ibm.avatar.aql.ViewBodyNode;
import com.ibm.avatar.aql.catalog.Catalog;
import com.ibm.avatar.aql.doc.AQLDocComment;
import com.ibm.biginsights.textanalytics.aqllibrary.Activator;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 *  Babbar
 * 
 */
public class AQLModel {



	private HashMap<String, AQLProject> aqlLib;
	AQLDocComment comment;
	
	private static final ILog logger = LogUtil.getLogForPlugin(Activator.PLUGIN_ID);
	
	public AQLModel(IAQLLibrary aqlLibrary) {
		this.aqlLib = aqlLibrary.getLibraryMap();
	}

	public void update(String filePath, String prjName, StatementList stList) {
		//System.out.println("1");
		if (!filePath.equals(null) && !prjName.equals(null) && !stList.equals(null)) {
			//System.out.println("2");
			//aqlLib = Activator.getLibrary().getLibraryMap(); subin - why are we doing this?
			updateLibrary(filePath, prjName, stList);
			//System.out.println(filePath + prjName + stList + "something is null");
		} else {
			//System.out.println("Did not update library as some values are missing");
		}
	}
	
	private void updateLibrary(String filePath, String prjName, StatementList stList) {
		// assumed that library exist as it was created during load
		// get the appropriate file and recreate elements in it.
		if (!isProjectExist(prjName)) {
			//System.out.println(filePath + "project does not exist");
			// some error happened while creation of this projects library on load
		}
		// project exist in library
		else {
		  AQLProject aqlProject = aqlLib.get(prjName);
			if (isAQLFileExist(filePath, aqlProject)) {
				//System.out.println(filePath + "already exist");
				// file already exist, get the file
				AQLFile file = getAQLFile(filePath, aqlProject);
				file.deleteAllElements();
				createElement(file, stList);
			} else {
				//System.out.println(filePath + "file doesnt exist");
				// some error happened while creation of this files library on load
			}
		}
	}

	public void create(String aqlFilePath, String prjName, StatementList statementList) {
	  if (!aqlFilePath.equals(null) && !prjName.equals(null) && !statementList.equals(null)) {
	    //aqlLib = Activator.getLibrary().getLibraryMap(); subin - why are we doing this?
			createLibrary(aqlFilePath, prjName, statementList);
		} else {
			//System.out.println("Did not update library as some values are missing");
		}
	}
	
	private void createLibrary(String aqlFilePath, String prjName, StatementList statementList) {
		if (!isProjectExist(prjName)) {
			//System.out.println(aqlFilePath + "1");
			//System.out.println(aqlFilePath);
			// if project does not exist, create project, file and elements.
			createAQLProject(prjName);
			AQLProject aqlProject = aqlLib.get(prjName);
			//System.out.println(prjName);
			createAQLFile(aqlProject, aqlFilePath);
			AQLFile file = getAQLFile(aqlFilePath, aqlProject);
			createElement(file, statementList);
			//System.out.println(aqlFilePath + "2");
		}
		// project exist in library, get it and append child to it
		else {
			//System.out.println(aqlFilePath + "3");
			AQLProject aqlProject = aqlLib.get(prjName);
			if (isAQLFileExist(aqlFilePath, aqlProject)) {
				// file already exist, get the file
				AQLFile file = getAQLFile(aqlFilePath, aqlProject);
				file.deleteAllElements();
				createElement(file, statementList);
				//System.out.println(aqlFilePath + "4");
			} else {
				//System.out.println(aqlFilePath + "5");
				createAQLFile(aqlProject, aqlFilePath);
				AQLFile file = getAQLFile(aqlFilePath, aqlProject);
				createElement(file, statementList);
			}
		}
	}
	
	

	private void createAQLFile(AQLProject aqlProject, String aqlFilePath) {
		// cretae aql file within the project node that is created
		AQLFile aqlFile = new AQLFile();
		aqlFile.filePath = aqlFilePath;
		aqlProject.addFile(aqlFile);
	}

	private void createElement(AQLFile aqlFile, StatementList statementList) {

		for (int i = 0; i < statementList.getParseTreeNodes().size(); i++) {
		  AQLParseTreeNode parseNode = statementList.getParseTreeNodes().get(i);
			// get all values from the statement list node.
			AQLElement element = null;
      if(parseNode instanceof CreateViewNode)
			{
				//check if its a view or select node
				//select node
				if(((CreateViewNode) parseNode).getIsOutput() == true)
				{
					element = new Select();
          element.dependsOnElement = getDeps(((CreateViewNode) parseNode).getBody());
          element.type = "SELECT";
					element.filePath = aqlFile.filePath;
					element.name = ((CreateViewNode) parseNode).getViewNameNode().getNickname();
					element.unQualifiedName = ((CreateViewNode) parseNode).getViewNameNode().getNickname();
					comment = ((CreateViewNode) parseNode).getComment();
					if (comment != null)
						element.comment = comment.getCleanText();
					element.beginLineNumber = ((CreateViewNode) parseNode).getViewNameNode().getOrigTok().beginLine;
					element.endLineNumber = ((CreateViewNode) parseNode).getViewNameNode().getOrigTok().endLine;
					element.beginOffset = ((CreateViewNode) parseNode).getViewNameNode().getOrigTok().beginColumn;
					element.endOffset = ((CreateViewNode) parseNode).getViewNameNode().getOrigTok().endColumn;
				}
				//view node
				else
				{
					element = new View();
					element.dependsOnElement = getDeps(((CreateViewNode) parseNode).getBody());
          element.type = "VIEW";
					element.filePath = aqlFile.filePath;
					element.name = ((CreateViewNode) parseNode).getViewNameNode().getNickname();
					element.unQualifiedName = ((CreateViewNode) parseNode).getViewNameNode().getNickname();
					comment = ((CreateViewNode) parseNode).getComment();
					if (comment != null)
						element.comment = comment.getCleanText();
					element.beginLineNumber = ((CreateViewNode) parseNode).getViewNameNode().getOrigTok().beginLine;
					element.endLineNumber = ((CreateViewNode) parseNode).getViewNameNode().getOrigTok().endLine;
					element.beginOffset = ((CreateViewNode) parseNode).getViewNameNode().getOrigTok().beginColumn;
					element.endOffset = ((CreateViewNode) parseNode).getViewNameNode().getOrigTok().endColumn;
				}
					//System.out.println("CreateViewNode: " + element.name + " " + element.dependsOnElement);
					
					// store dependent views
//					if(((CreateViewNode) parseNode).getBody().getOrigTok().toString() == "extract")
//					{
//						SubElement sElement = new SubElement();
//						sElement.name = ((CreateViewNode) parseNode).getBody().getOrigTok().getValue().toString();
//						sElement.beginLineNumber = ((CreateViewNode) parseNode).getBody().getOrigTok().beginLine;
//						sElement.endLineNumber = ((CreateViewNode) parseNode).getBody().getOrigTok().endLine;
//						sElement.beginOffset = ((CreateViewNode) parseNode).getBody().getOrigTok().beginColumn;
//						sElement.endOffset = ((CreateViewNode) parseNode).getBody().getOrigTok().endColumn;
//						System.out.println("VIEW EXTRACT: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
//					}
//					else if(((CreateViewNode) parseNode).getBody().getOrigTok().toString() == "select")
//					{
//						SubElement sElement = new SubElement();
//						sElement.name = ((CreateViewNode) parseNode).getBody().getOrigTok().getValue().toString();
//						sElement.beginLineNumber = ((CreateViewNode) parseNode).getBody().getOrigTok().beginLine;
//						sElement.endLineNumber = ((CreateViewNode) parseNode).getBody().getOrigTok().endLine;
//						sElement.beginOffset = ((CreateViewNode) parseNode).getBody().getOrigTok().beginColumn;
//						sElement.endOffset = ((CreateViewNode) parseNode).getBody().getOrigTok().endColumn;
//						System.out.println("VIEW SELECT: " + sElement.name + " " + sElement.beginLineNumber + " " + sElement.endLineNumber + " " + sElement.beginOffset + " " + sElement.endOffset);
//					}					
				
				
			}
			else if(parseNode instanceof CreateDictNode)
			{ /*/ TODO check for the External dictionary or not..
			  if(((CreateDictNode)parseNode)).getParams ().getIsExternal ()==false) //-This line shows error talk to dharmesh..
        {
			    
        }
			  else{
			  }
			  */
				element = new Dictionary();
				element.type = "DICTIONARY";
				element.filePath = aqlFile.filePath;
				element.name = ((CreateDictNode) parseNode).getDictname();
				element.unQualifiedName = ((CreateDictNode) parseNode).getDictname();
				comment = ((CreateDictNode) parseNode).getComment();
				if (comment != null)
					element.comment = comment.getCleanText();
				Token token = ((CreateDictNode) parseNode).getDictNameNode ().getOrigTok ();
				element.beginLineNumber = token.beginLine;
				element.endLineNumber = token.endLine;
				element.beginOffset = token.beginColumn;
				element.endOffset = token.endColumn;
			}
			else if(parseNode instanceof CreateExternalViewNode)
			{
				element = new ExternalView();
				element.type = "EXTERNAL_VIEW";
				element.filePath = aqlFile.filePath;
				element.name = ((CreateExternalViewNode) parseNode).getExternalViewName();
				element.unQualifiedName = ((CreateExternalViewNode) parseNode).getExternalViewName();
				comment = ((CreateExternalViewNode) parseNode).getComment();
				if (comment != null)
					element.comment = comment.getCleanText();
				element.beginLineNumber = ((CreateExternalViewNode) parseNode).getErrorTok().beginLine;
				element.endLineNumber = ((CreateExternalViewNode) parseNode).getErrorTok().endLine;
				element.beginOffset = ((CreateExternalViewNode) parseNode).getErrorTok().beginColumn;
				element.endOffset = ((CreateExternalViewNode) parseNode).getErrorTok().endColumn;
				//System.out.println("CreateExtViewNode: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
			}
			else if(parseNode instanceof CreateFunctionNode)
			{
			  element = new Function();
				element.type = "FUNCTION";
				element.filePath = aqlFile.filePath;
				element.name = ((CreateFunctionNode) parseNode).getFunctionName();
				element.unQualifiedName = ((CreateFunctionNode) parseNode).getFunctionName();
				comment = ((CreateFunctionNode) parseNode).getComment();
				if (comment != null)
					element.comment = comment.getCleanText();
				element.beginLineNumber = ((CreateFunctionNode) parseNode).getErrorTok().beginLine;
				element.endLineNumber = ((CreateFunctionNode) parseNode).getErrorTok().endLine;
				element.beginOffset = ((CreateFunctionNode) parseNode).getErrorTok().beginColumn;
				element.endOffset = ((CreateFunctionNode) parseNode).getErrorTok().endColumn;
			}
			else if(parseNode instanceof CreateTableNode)
			{
			  // check for the External table or not..
        if(((CreateTableNode)parseNode).getIsExternal ()== false)
        {
          element = new Table();
          element.type = "TABLE";
          element.filePath = aqlFile.filePath;
          element.name = ((CreateTableNode) parseNode).getTableName();
          element.unQualifiedName = ((CreateTableNode) parseNode).getTableName();
          comment = ((CreateTableNode) parseNode).getComment();
          if (comment != null)
				element.comment = comment.getCleanText();
          element.beginLineNumber = ((CreateTableNode) parseNode).getErrorTok().beginLine;
          element.endLineNumber = ((CreateTableNode) parseNode).getErrorTok().endLine;
          element.beginOffset = ((CreateTableNode) parseNode).getErrorTok().beginColumn;
          element.endOffset = ((CreateTableNode) parseNode).getErrorTok().endColumn;
          //System.out.println("CreateTableNode: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
        }
        else{
          element = new ExternalTable();
          element.type = "EXTERNAL_TABLE";
          element.filePath = aqlFile.filePath;
          element.name = ((CreateTableNode) parseNode).getTableName();
          element.unQualifiedName = ((CreateTableNode) parseNode).getTableName();
          comment = ((CreateTableNode) parseNode).getComment();
          if (comment != null)
				element.comment = comment.getCleanText();
          element.beginLineNumber = ((CreateTableNode) parseNode).getErrorTok().beginLine;
          element.endLineNumber = ((CreateTableNode) parseNode).getErrorTok().endLine;
          element.beginOffset = ((CreateTableNode) parseNode).getErrorTok().beginColumn;
          element.endOffset = ((CreateTableNode) parseNode).getErrorTok().endColumn;
          //System.out.println("CreateTableNode: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
        }
			}
			else if(parseNode instanceof IncludeFileNode)
			{
				element = new IncludedFile();
				element.type = "INCLUDE";
				element.filePath = aqlFile.filePath;
				element.name = ((IncludeFileNode) parseNode).getIncludedFileName().getStr();
				element.unQualifiedName = ((IncludeFileNode) parseNode).getIncludedFileName().getStr();
				element.beginLineNumber = ((IncludeFileNode) parseNode).getIncludedFileName().getOrigTok().endLine;
				element.endLineNumber = ((IncludeFileNode) parseNode).getIncludedFileName().getOrigTok().endLine;
				element.beginOffset = ((IncludeFileNode) parseNode).getIncludedFileName().getOrigTok().beginColumn;
				element.endOffset = ((IncludeFileNode) parseNode).getIncludedFileName().getOrigTok().endColumn;
			}
			else if(parseNode instanceof OutputViewNode)
			{
				element = new OutputView();
				element.type = "OUTPUT_VIEW";
				element.filePath = aqlFile.filePath;
				element.name = ((OutputViewNode) parseNode).getViewname().getNickname();
				element.unQualifiedName = ((OutputViewNode) parseNode).getViewname().getNickname();
				element.beginLineNumber = ((OutputViewNode) parseNode).getUnqualifiedViewNameNickNode ().getOrigTok().beginLine;
				element.endLineNumber = ((OutputViewNode) parseNode).getUnqualifiedViewNameNickNode ().getOrigTok().endLine;
				element.beginOffset = ((OutputViewNode) parseNode).getUnqualifiedViewNameNickNode ().getOrigTok().beginColumn;
				element.endOffset = ((OutputViewNode) parseNode).getUnqualifiedViewNameNickNode ().getOrigTok().endColumn;
			}
			else if(parseNode instanceof DetagDocNode)
			{
				element = new Detag();
				element.type = "DETAG";
				element.filePath = aqlFile.filePath;
				element.name = ((DetagDocNode) parseNode).getDetaggedDocName();
				element.unQualifiedName = ((DetagDocNode) parseNode).getDetaggedDocName();
				comment = ((DetagDocNode) parseNode).getComment();
				if (comment != null)
					element.comment = comment.getCleanText();
				element.beginLineNumber = ((DetagDocNode) parseNode).getOrigTok().beginLine;
				element.endLineNumber = ((DetagDocNode) parseNode).getOrigTok().endLine;
				element.beginOffset = ((DetagDocNode) parseNode).getOrigTok().beginColumn;
				element.endOffset = ((DetagDocNode) parseNode).getOrigTok().endColumn;
				//System.out.println("DetagDocNode: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
				
			}
			/* / / *
			else if(parseNode instanceof ModuleNode)
      {
        element = new Module();
        element.type = "MODULE";
        element.filePath = aqlFile.filePath;
        element.name = ((ModuleNode) parseNode).getName ().getNickname ();
        element.beginLineNumber = ((ModuleNode) parseNode).getName ().getOrigTok ().beginLine;
        element.endLineNumber = ((ModuleNode) parseNode).getName ().getOrigTok().endLine;
        element.beginOffset = ((ModuleNode) parseNode).getName ().getOrigTok().beginColumn;
        element.endOffset = ((ModuleNode) parseNode).getName ().getOrigTok().endColumn;
        System.out.println("ModuleNode: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
			/* TODO Need to think about this Node in the runtime jar...
			else if(parseNode instanceof RequireDocumentWithColumnsNode)
      {
        element = new RequireDocument();
        element.type = "REQUIRE_DOCUMENT";
        element.filePath = aqlFile.filePath;
        element.name = ((RequireDocumentWithColumnsNode) parseNode).getModuleName ().getNickname ();
        element.beginLineNumber = ((RequireDocumentWithColumnsNode) parseNode).getOrigTok().beginLine;
        element.endLineNumber = ((RequireDocumentWithColumnsNode) parseNode).getOrigTok().endLine;
        element.beginOffset = ((RequireDocumentWithColumnsNode) parseNode).getOrigTok().beginColumn;
        element.endOffset = ((RequireDocumentWithColumnsNode) parseNode).getOrigTok().endColumn;
        //System.out.println("DictNode: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
      * /
			else if(parseNode instanceof ImportModuleNode)
      {
        element = new ImportModule();
        element.type = "IMPORT_MODULE";
        element.filePath = aqlFile.filePath;
        element.name = ((ImportModuleNode) parseNode).getNodeName ().getNickname ();
        //element.beginLineNumber = ((ModuleNode) parseNode).getOrigTok().beginLine;
        element.beginLineNumber = ((ImportModuleNode) parseNode).getNodeName ().getOrigTok ().beginLine;
        element.endLineNumber = ((ImportModuleNode) parseNode).getNodeName ().getOrigTok().endLine;
        element.beginOffset = ((ImportModuleNode) parseNode).getNodeName ().getOrigTok().beginColumn;
        element.endOffset = ((ImportModuleNode) parseNode).getNodeName ().getOrigTok().endColumn;
        System.out.println("Import_ModuleNode: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
			else if(parseNode instanceof ImportViewNode)
      {
        element = new ImportView();
        element.type = "IMPORT_VIEW";
        element.filePath = aqlFile.filePath;
        element.name = ((ImportViewNode) parseNode).getNodeName ().getNickname ();
        //element.beginLineNumber = ((ModuleNode) parseNode).getOrigTok().beginLine;
        element.beginLineNumber = ((ImportViewNode) parseNode).getNodeName ().getOrigTok ().beginLine;
        element.endLineNumber = ((ImportViewNode) parseNode).getNodeName ().getOrigTok().endLine;
        element.beginOffset = ((ImportViewNode) parseNode).getNodeName ().getOrigTok().beginColumn;
        element.endOffset = ((ImportViewNode) parseNode).getNodeName ().getOrigTok().endColumn;
        System.out.println("Import_View Node: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
			else if(parseNode instanceof ImportDictNode)
      {
        element = new ImportDictionary();
        element.type = "IMPORT_DICTIONARY";
        element.filePath = aqlFile.filePath;
        element.name = ((ImportDictNode) parseNode).getNodeName ().getNickname ();
        //element.beginLineNumber = ((ModuleNode) parseNode).getOrigTok().beginLine;
        element.beginLineNumber = ((ImportDictNode) parseNode).getNodeName ().getOrigTok ().beginLine;
        element.endLineNumber = ((ImportDictNode) parseNode).getNodeName ().getOrigTok().endLine;
        element.beginOffset = ((ImportDictNode) parseNode).getNodeName ().getOrigTok().beginColumn;
        element.endOffset = ((ImportDictNode) parseNode).getNodeName ().getOrigTok().endColumn;
        System.out.println("Import_Dict Node: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
			else if(parseNode instanceof ImportFuncNode)
      {
        element = new ImportFunction();
        element.type = "IMPORT_FUNCTION";
        element.filePath = aqlFile.filePath;
        element.name = ((ImportFuncNode) parseNode).getNodeName ().getNickname ();
        //element.beginLineNumber = ((ModuleNode) parseNode).getOrigTok().beginLine;
        element.beginLineNumber = ((ImportFuncNode) parseNode).getNodeName ().getOrigTok ().beginLine;
        element.endLineNumber = ((ImportFuncNode) parseNode).getNodeName ().getOrigTok().endLine;
        element.beginOffset = ((ImportFuncNode) parseNode).getNodeName ().getOrigTok().beginColumn;
        element.endOffset = ((ImportFuncNode) parseNode).getNodeName ().getOrigTok().endColumn;
        System.out.println("Import_Function Node: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
			else if(parseNode instanceof ImportTableNode)
      {
        element = new ImportTabel();
        element.type = "IMPORT_TABLE";
        element.filePath = aqlFile.filePath;
        element.name = ((ImportTableNode) parseNode).getNodeName ().getNickname ();
        //element.beginLineNumber = ((ModuleNode) parseNode).getOrigTok().beginLine;
        element.beginLineNumber = ((ImportTableNode) parseNode).getNodeName ().getOrigTok ().beginLine;
        element.endLineNumber = ((ImportTableNode) parseNode).getNodeName ().getOrigTok().endLine;
        element.beginOffset = ((ImportTableNode) parseNode).getNodeName ().getOrigTok().beginColumn;
        element.endOffset = ((ImportTableNode) parseNode).getNodeName ().getOrigTok().endColumn;
        System.out.println("Import_Tabel Node: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
			else if(parseNode instanceof ExportFuncNode)
      {
        element = new ExportFunction();
        element.type = "EXPORT_FUNCTION";
        element.filePath = aqlFile.filePath;
        element.name = ((ExportFuncNode) parseNode).getNodeName ().getNickname ();
        element.beginLineNumber = ((ExportFuncNode) parseNode).getNodeName ().getOrigTok().beginLine;
        element.endLineNumber = ((ExportFuncNode) parseNode).getNodeName ().getOrigTok().endLine;
        element.beginOffset = ((ExportFuncNode) parseNode).getNodeName ().getOrigTok().beginColumn;
        element.endOffset = ((ExportFuncNode) parseNode).getNodeName ().getOrigTok().endColumn;
        System.out.println("Export Function Node: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
			else if(parseNode instanceof ExportDictNode)
      {
        element = new ExportDictionary();
        element.type = "EXPORT_DICTIONARY";
        element.filePath = aqlFile.filePath;
        element.name = ((ExportDictNode) parseNode).getNodeName ().getNickname ();
        element.beginLineNumber = ((ExportDictNode) parseNode).getNodeName ().getOrigTok().beginLine;
        element.endLineNumber = ((ExportDictNode) parseNode).getNodeName ().getOrigTok().endLine;
        element.beginOffset = ((ExportDictNode) parseNode).getNodeName ().getOrigTok().beginColumn;
        element.endOffset = ((ExportDictNode) parseNode).getNodeName ().getOrigTok().endColumn;
        System.out.println("Export dict Node: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
			else if(parseNode instanceof ExportTableNode)
      {
        element = new ExportTabel();
        element.type = "EXPORT_TABLE";
        element.filePath = aqlFile.filePath;
        element.name = ((ExportTableNode) parseNode).getNodeName ().getNickname ();
        element.beginLineNumber = ((ExportTableNode) parseNode).getNodeName ().getOrigTok().beginLine;
        element.endLineNumber = ((ExportTableNode) parseNode).getNodeName ().getOrigTok().endLine;
        element.beginOffset = ((ExportTableNode) parseNode).getNodeName ().getOrigTok().beginColumn;
        element.endOffset = ((ExportTableNode) parseNode).getNodeName ().getOrigTok().endColumn;
        System.out.println("Export table Node: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
			else if(parseNode instanceof ExportViewNode)
      {
        element = new ExportView();
        element.type = "EXPORT_VIEW";
        element.filePath = aqlFile.filePath;
        element.name = ((ExportViewNode) parseNode).getNodeName ().getNickname ();
        element.beginLineNumber = ((ExportViewNode) parseNode).getNodeName ().getOrigTok().beginLine;
        element.endLineNumber = ((ExportViewNode) parseNode).getNodeName ().getOrigTok().endLine;
        element.beginOffset = ((ExportViewNode) parseNode).getNodeName ().getOrigTok().beginColumn;
        element.endOffset = ((ExportViewNode) parseNode).getNodeName ().getOrigTok().endColumn;
        System.out.println("Export View Node: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
			*/
			/*
      else if(parseNode instanceof ExternalDictionaryNode)
      {
        element = new ExternalDictionary();
        element.type = "EXTERNAL_DICTIONARY";
        element.filePath = aqlFile.filePath;
        element.name = ((ExternalDictionaryNode) parseNode).getModuleName ().getNickname ();
        element.beginLineNumber = ((ExternalDictionaryNode) parseNode).getOrigTok().beginLine;
        element.endLineNumber = ((ExternalDictionaryNode) parseNode).getOrigTok().endLine;
        element.beginOffset = ((ExternalDictionaryNode) parseNode).getOrigTok().beginColumn;
        element.endOffset = ((ExternalDictionaryNode) parseNode).getOrigTok().endColumn;
        //System.out.println("DictNode: " + element.name + " " + element.beginLineNumber + " " + element.endLineNumber + " " + element.beginOffset + " " + element.endOffset);
      }
      */ 
//			element.fileName = null;
//			element.dependentView = null;
//			element.filePath = null;
//			element.lineNumber = null;
//			element.name = null;
//			element.offset = null;
//			element.type = null;
			
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
	
	private void createAQLProject(String projectName) {
		// create a map for project name and project node
		AQLProject newProject = new AQLProject();
		aqlLib.put(projectName, newProject);
	}

	// will be used for refactoring
	public void deleteAQLFile(String filePath, String prjName) {

		AQLProject project = aqlLib.get(prjName);
		AQLFile file = getAQLFile(filePath, project);
		project.deleteAQLFile(file);
	}

	// will be used for refactoring
	public void deleteAQLFiles(String prjName) {
		aqlLib.get(prjName).deleteAllFiles();
	}

	// will be used for refactoring
	public void deleteAQLProject(String prjName) {
		aqlLib.remove(prjName);
	}

	public AQLFile getAQLFile(String aqlFilePath, AQLProject aqlProject) {
		List<AQLFile> aqlFiles = aqlProject.getAQLFiles();
		// check if file given already exist in library under given project
		for (int i = 0; i < aqlFiles.size(); i++) {
			if (aqlFiles.get(i).filePath.equals(aqlFilePath))
			// file exist, return the node
			{
				return aqlFiles.get(i);
			}
		}
		return null;
	}

	private boolean isAQLFileExist(String aqlFilePath, AQLProject aqlProject) {

		List<AQLFile> aqlFiles = aqlProject.getAQLFiles();
		// check if file given already exist in library under given project
		if(aqlFiles.size() > 0)
		{
			for (int i = 0; i < aqlFiles.size(); i++) {
			if (aqlFiles.get(i).filePath.equals(aqlFilePath))
			// file already exist
			{
				return true;
			}
			}
		}
		return false;
	}

	private boolean isProjectExist(String projectName) {
		if (aqlLib.get(projectName) != null) {
			return true;
		}
		return false;
	}

}
