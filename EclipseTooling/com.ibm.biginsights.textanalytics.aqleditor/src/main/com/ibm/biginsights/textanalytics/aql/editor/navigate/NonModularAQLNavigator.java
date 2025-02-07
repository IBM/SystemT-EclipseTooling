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
package com.ibm.biginsights.textanalytics.aql.editor.navigate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.avatar.aql.AQLParseTreeNode;
import com.ibm.avatar.aql.AQLParser;
import com.ibm.avatar.aql.CreateDictNode;
import com.ibm.avatar.aql.CreateFunctionNode;
import com.ibm.avatar.aql.CreateTableNode;
import com.ibm.avatar.aql.CreateViewNode;
import com.ibm.avatar.aql.IncludeFileNode;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.StatementList;
import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.Messages;
import com.ibm.biginsights.textanalytics.aql.editor.syntax.AQLSyntaxElements;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Provides code navigation (triggered by F3) for non-modular text analytics projects.
 * 
 * 
 */
public class NonModularAQLNavigator extends AQLNavigator
{
  private Set<String> fileSet = new HashSet<String> ();

  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n" + //$NON-NLS-1$
    "US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

  public NonModularAQLNavigator (AQLEditor aqlEditor, IFile selectionSource, ITextSelection selection)
  {
    super (aqlEditor, selectionSource, selection);
  }

  @Override
  public void navigateToDefinition ()
  {
    try {
      int[] selectionOffsets = getSelectionOffsets ();
      int startLine = selectionOffsets[0];
      int endLine = selectionOffsets[0];
      boolean notInComments = isSelectionOutsideComments (doc, startLine, endLine);
      if (notInComments) {
        int cursorPosition = styledText.getCaretOffset () - doc.getLineOffset (startLine); // position relative to start
                                                                                           // of line
        currToken = getSelectedToken (doc, startLine, cursorPosition); // selected token.
        IAQLLibrary lib = Activator.getLibrary ();
        String searchPath = "";
        try {
          if (selectionEventSourceFile.getProject ().hasNature (
            com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID)) {
            String currentFileLocation = selectionEventSourceFile.getLocation ().toOSString ();
            SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties (selectionEventSourceFile.getProject ().getName ());
            searchPath = ProjectPreferencesUtil.getAbsolutePath (properties.getSearchPath ());
            String mainAQLFile = ProjectPreferencesUtil.getAbsolutePath (properties.getMainAQLFile ());
            List<String> searchPathList = getSearchPathList (mainAQLFile, currentFileLocation, searchPath);
            List<String> dictionarySearchPathList = getDictSearchPathList (searchPath);

            // Identify selected element type..
            String selectedTokenType = getAQLEleType (selectionEventSourceFile, currToken);
            if ((selectedTokenType != null) && (selectedTokenType.equals (KEYWORD_TOKEN))) {
              // Display the message
              LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
                Messages.AQLEditor_SELECTED_TOKEN_IS_AQL_KEYWORD + currToken);
            }
            else {
              boolean done = searchAQLLibraryAndShowInEditor (lib, searchPathList, currentFileLocation, currToken);
              if (!done) {
                if (currToken.contains ("/") || currToken.contains ("."))//$NON-NLS-1$
                {
                  // it could be a file, if it is open it
                  Iterator<String> iterator4 = dictionarySearchPathList.iterator ();
                  while (iterator4.hasNext ()) {
                    String newPath = iterator4.next ();
                    newPath = newPath + "/" + currToken;
                    if (new File (newPath).exists ()) {
                      // open this file
                      openFile (newPath, 0);
                      if (didEditorOpen (new File (newPath).getName ())) {}
                      else {
                        openExternalEditor (new File (newPath));
                      }
                    }
                    else {
                      // do nothing
                    }
                  }
                }
              }
            }
          }
        }
        catch (CoreException e1) {
          e1.printStackTrace ();
        }
      }

    }
    catch (BadLocationException e) {

    }

  }

  /**
   * Search for aql element with name matching the given token in aql library. If an element is found, retrieves its
   * location information and opens its source in aql editor.
   * 
   * @param aqlLibrary
   * @param searchPathList
   * @param currentFileLocation
   * @param aqlToken
   * @return true if the element is found.
   */
  private boolean searchAQLLibraryAndShowInEditor (IAQLLibrary aqlLibrary, List<String> searchPathList,
    String currentFileLocation, String aqlToken)
  {
    List<String> fileList = new ArrayList<String> (getAllAQLFiles (searchPathList, currentFileLocation));
    if (currentFileLocation != null) {
      Collections.swap (fileList, 0, fileList.indexOf (currentFileLocation));
    }
    List<AQLElement> elements = aqlLibrary.getElements (fileList);
    boolean handled = false;
    Iterator<AQLElement> iterator2 = elements.iterator ();
    handled: while (iterator2.hasNext ()) {
      AQLElement elmt = iterator2.next ();
      String aqlElementName = "";//$NON-NLS-1$

      aqlElementName = elmt.getUnQualifiedName ();
      if (aqlElementName == null) {
        aqlElementName = "";
      }
      if (aqlElementName.equals (aqlToken)) {
        if (elmt.getType () == Constants.AQL_ELEMENT_TYPE_VIEW || elmt.getType () == Constants.AQL_ELEMENT_TYPE_SELECT
          || elmt.getType () == Constants.AQL_ELEMENT_TYPE_DICT || elmt.getType () == Constants.AQL_ELEMENT_TYPE_DETAG
          || elmt.getType () == Constants.AQL_ELEMENT_TYPE_FUNC || elmt.getType () == Constants.AQL_ELEMENT_TYPE_TABLE
          || elmt.getType () == Constants.AQL_ELEMENT_TYPE_EXTERNAL_VIEW
          || elmt.getType () == Constants.AQL_ELEMENT_TYPE_EXTERNAL_TABLE
          || elmt.getType () == Constants.AQL_ELEMENT_TYPE_EXTERNAL_DICT) {
          // Here it tries to identifies the current token offset and highlight the token in a file and
          // pass it to open editor method // and also
          String file_p = elmt.getFilePath ();
          openFile (file_p, 0);
          File f = new File (file_p);
          if (didEditorOpen (f.getName ())) {
            goToLine (newEditor, elmt.getBeginLine (), elmt.getBeginOffset () - 1, elmt.getUnQualifiedName ().length ());
            handled = true;
            break handled;
          }
          else {
            openExternalEditor (f);
            handled = true;
            break handled;
          }
        }
        else if (elmt.getType () == "INCLUDE")//$NON-NLS-1$
        {
          // TRIM THE FILE NAME AND SEARCH IN WORKSPACE AND OPEN IT... REUSE CODE BELOW
          String fname = elmt.getName ();

          Iterator<String> iterator4 = searchPathList.iterator ();
          while (iterator4.hasNext ()) {
            String newPath = iterator4.next ();
            // newPath = newPath + "/" + fname;
            if (newPath.contains (fname) && new File (newPath).exists ()) {
              // open this file
              openFile (newPath, 0);
              if (didEditorOpen (new File (newPath).getName ())) {}
              else {
                openExternalEditor (new File (newPath));
              }
              handled = true;
              break handled;
            }
            else {
              // do nothing
            }
          }
        }
      }

    } // End of while loop...
    return handled;
  }

  /**
   * This method is to calculate offsets for the AQL constructs and highlight them in corresponding AQL file in editor.
   * 
   * @param editorPart instance of an current IEditorPart
   * @param lineNumber line number where the construct is defined
   * @param start beginning offset
   * @param end ending offset
   */
  protected void goToLine (IEditorPart editorPart, int lineNumber, int start, int end)
  {
    if (!(editorPart instanceof ITextEditor) || lineNumber <= 0) { return; }
    ITextEditor editor = (ITextEditor) editorPart;
    IDocument document = editor.getDocumentProvider ().getDocument (editor.getEditorInput ());
    if (document != null) {
      IRegion lineInfo = null;
      boolean startsWithQuote = false;

      try {
        lineInfo = document.getLineInformation (lineNumber - 1);
        char startChar = document.getChar (lineInfo.getOffset () + start);
        if (startChar == '"') {
          startsWithQuote = true;
        }
      }
      catch (BadLocationException e) {}
      if (lineInfo != null) {
        if (startsWithQuote) {
          editor.selectAndReveal (lineInfo.getOffset () + start, end + 2);
        }
        else {
          editor.selectAndReveal (lineInfo.getOffset () + start, end);
        }
      }
    }
  }

  /**
   * Parses the input source file and returns out the selected element Type..
   * 
   * @param fileToRead
   * @param selectedToken
   * @return
   */
  private String getAQLEleType (IFile fileToRead, String selectedToken)
  {
    String eleName = null;
    String eleType = null;
    String filePath = fileToRead.getRawLocation ().toOSString ();
    IProject project = fileToRead.getProject ();

    // Determine if the selected token is AQL Keyword or not..
    List<String> keyWordList = Arrays.asList (AQLSyntaxElements.KEYWORDS);
    if (keyWordList.contains (selectedToken)) { return KEYWORD_TOKEN; }
    try {
      AQLParser parser = new AQLParser (FileUtils.fileToStr (new File (filePath), project.getDefaultCharset ()),
        filePath);
      parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser
      StatementList stmtList = parser.parse ();
      LinkedList<AQLParseTreeNode> ptn = stmtList.getParseTreeNodes ();
      for (AQLParseTreeNode node : ptn) {
        // Check if the parse node is instance of View/Function/Table/Dict
        // And compare it with parsenode.getname
        // then return element Type..
        if (node instanceof CreateViewNode) {
          CreateViewNode viewNode = (CreateViewNode) node;
          eleName = viewNode.getUnqualifiedName ();
          if (eleName.equals (selectedToken)) {
            eleType = Constants.AQL_ELEMENT_TYPE_VIEW;
            return eleType;
          }
        }
        else if (node instanceof CreateDictNode) {
          CreateDictNode dictNode = (CreateDictNode) node;
          eleName = dictNode.getUnqualifiedName ();
          if (eleName.equals (selectedToken)) {
            eleType = Constants.AQL_ELEMENT_TYPE_DICT;
            return eleType;
          }
        }
        else if (node instanceof CreateFunctionNode) {
          CreateFunctionNode funcNode = (CreateFunctionNode) node;
          eleName = funcNode.getUnqualifiedName ();
          if (eleName.equals (selectedToken)) {
            eleType = Constants.AQL_ELEMENT_TYPE_FUNC;
            return eleType;
          }
        }
        else if (node instanceof CreateTableNode) {
          CreateTableNode tabNode = (CreateTableNode) node;
          eleName = tabNode.getUnqualifiedName ();
          if (eleName.equals (selectedToken)) {
            eleType = Constants.AQL_ELEMENT_TYPE_TABLE;
            return eleType;
          }
        }
      }
    }
    catch (IOException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
        String.format ("Error parsing AQL script: %s", fileToRead.getFullPath ().toOSString ()), e);
    }
    catch (CoreException e) {
      // Auto-generated catch block
      e.printStackTrace ();
    }

    return null;
  }

  private Set<String> getAllAQLFiles (List<String> searchPathList, String currentFileLocation)
  {
    Iterator<String> iterator1 = searchPathList.iterator ();
    // Set<String> fileSet = new HashSet<String>();
    fileSet.add (currentFileLocation);
    // All files that are there in the search path
    while (iterator1.hasNext ()) {
      String temp = iterator1.next ();
      IPath path = new Path (temp).makeAbsolute ();
      try {
        new FileTraversal () {
          public void onFile (final File f)
          {
            if (f.getName ().endsWith (".aql"))//$NON-NLS-1$
            {
              fileSet.add (f.getAbsolutePath ().toString ());
            }
          }
        }.traverse (new File (path.toOSString ()));
      }
      catch (IOException e) {
        e.printStackTrace ();
      }
    }
    return fileSet;
  }

  private ArrayList<String> getSearchPathList (String mainFile, String currentFileLocation, String searchPath)
  {

    String[] tokens = searchPath.split (";"); //$NON-NLS-1$.
    String contents = readFile (mainFile);
    Set<String> mainPathSet = getPath (searchPath, tokens, contents);
    Set<String> currentpathSet = null;
    Set<String> pathSet = new HashSet<String> ();
    if (!mainPathSet.contains (currentFileLocation)) {
      contents = readFile (currentFileLocation);
      currentpathSet = getPath (searchPath, tokens, contents);
    }

    if (mainPathSet != null) pathSet.addAll (mainPathSet);
    if (currentpathSet != null) pathSet.addAll (currentpathSet);

    return new ArrayList<String> (pathSet);
  }

  private ArrayList<String> getDictSearchPathList (String searchPath)
  {
    String[] tokens = searchPath.split (";"); //$NON-NLS-1$.
    return new ArrayList<String> (Arrays.asList (tokens));
  }

  private Set<String> getPath (String searchPath, String[] tokens, String contents)
  {
    Set<String> pathSet = new HashSet<String> ();
    try {
      AQLParser parser = new AQLParser (contents);
      parser.setIncludePath (searchPath);
      parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser

      parser.setBackwardCompatibilityMode (true);

      StatementList list = parser.parse ();
      LinkedList<AQLParseTreeNode> nodeList = list.getParseTreeNodes ();

      for (Iterator<AQLParseTreeNode> iterator = nodeList.iterator (); iterator.hasNext ();) {
        AQLParseTreeNode node = iterator.next ();
        if (node instanceof IncludeFileNode) {
          String comPath = getAbsolutePath (tokens, ((IncludeFileNode) node).getIncludedFileName ().getStr ());
          pathSet.add (comPath);
        }
      }
    }
    catch (IOException e) {
      e.printStackTrace ();
    }
    catch (ParseException e) {
      e.printStackTrace ();
    }
    return pathSet;

  }

  private String getAbsolutePath (String[] searchPathList, String fileName)
  {

    // All files that are there in the search path
    for (String directory : searchPathList) {
      String newPath = directory + File.separator + fileName;
      File file = new File (newPath);
      if (file.exists ()) { return newPath; }
    }

    return null;
  }

  private String readFile (String mainFileLocation)
  {
    File file = new File (mainFileLocation);
    StringBuffer content = new StringBuffer ();
    BufferedReader bufferedReader = null;
    String lineSeperator = System.getProperty ("line.separator");

    try {
      bufferedReader = new BufferedReader (new FileReader (file));
      String text = null;

      while ((text = bufferedReader.readLine ()) != null) {
        content.append (text + lineSeperator);

      }
    }
    catch (FileNotFoundException e) {
      e.printStackTrace ();
    }
    catch (IOException e) {
      e.printStackTrace ();
    }
    finally {
      try {
        if (bufferedReader != null) {
          bufferedReader.close ();
        }
      }
      catch (IOException e) {
        e.printStackTrace ();
      }
    }
    return content.toString ();
  }

}

class FileTraversal
{
  public final void traverse (final File f) throws IOException
  {
    // we dont need recursive traversal at this point, but we may need it
    if (f.isDirectory ()) {
      onDirectory (f);
      final File[] childs = f.listFiles ();
      for (File child : childs) {
        traverse (child);
      }
      return;
    }
    onFile (f);
  }

  public void onDirectory (final File d)
  {}

  public void onFile (final File f)
  {}
}
