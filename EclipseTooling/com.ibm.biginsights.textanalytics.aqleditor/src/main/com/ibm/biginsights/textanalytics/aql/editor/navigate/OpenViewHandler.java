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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.aql.AQLParser;
import com.ibm.avatar.aql.IncludeFileNode;
import com.ibm.avatar.aql.AQLParseTreeNode;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.StatementList;
import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.DictionaryEditor;
import com.ibm.biginsights.textanalytics.aql.editor.callhierarchy.DependencyHierarchyView;
import com.ibm.biginsights.textanalytics.aql.editor.callhierarchy.HierarchyNode;
import com.ibm.biginsights.textanalytics.aql.editor.callhierarchy.ReferenceHierarchyView;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.AQLModule;
import com.ibm.biginsights.textanalytics.aql.library.AQLModuleProject;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 *  This class is used to open the view in editor from the Annotation Explorer/Result Table /Tree viewer contect menu selections
 *  This expects the ViewName to open and Current project name where view is part of.
 *  
 *  Kalakuntla
 * 
 */
public class OpenViewHandler extends AbstractHandler
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

  public static final String EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.AQLEditor"; //$NON-NLS-1$
  public static final String DICT_EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.DictEditor"; //$NON-NLS-1$

  StyledText s;
  IEditorPart editor;
  String currToken = "";
  Set<String> fileSet;

  private ArrayList<String> searchPathList;
  private List<String> dictionarySearchPathList;

  private ArrayList<String> fileList;
  AQLEditor aqlEditor, newEditor;
  DictionaryEditor newDictEditor;
  private String currentFileLocation = null;
  private String mainAQLFile;
  IAQLLibrary aqlLibrary = null;
  Shell shell = null;
  
  private static final ILog logger = LogUtil
  .getLogForPlugin(Activator.PLUGIN_ID);

  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    String viewNameToOpen = event.getParameter(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID_PARAM_VIEW_NAME); 
    String currProjName = event.getParameter(Constants.CMD_SHOW_VIEW_IN_EDITOR_ID_PARAM_PROJ_NAME); 
    fileSet = new HashSet<String>();
    fileList = new ArrayList<String>();
    
    //If command was issued from Dependency or Reference hierarchy views,
    //no command parameters would have been provided.
    //Deducing viewNameToOpen and project name from tree selection and the view issuing the command.
    String generatingPartId = HandlerUtil.getActivePartId (event);
    if (generatingPartId != null && 
        (generatingPartId.equals(DependencyHierarchyView.ID) 
            || generatingPartId.equals (ReferenceHierarchyView.ID))) {
      ISelection selection = HandlerUtil.getCurrentSelection (event);
      if (selection instanceof TreeSelection) {
        HierarchyNode node = (HierarchyNode)((TreeSelection)selection).getFirstElement ();
        viewNameToOpen = node.getModuleName () + Constants.MODULE_ELEMENT_SEPARATOR + node.getText (); //$NON-NLS-1$
        currProjName = node.getProjectName ();
        if (currProjName.isEmpty()) {
          if (generatingPartId.equals (DependencyHierarchyView.ID)) {
            currProjName = DependencyHierarchyView.projectName;
          } else {
            currProjName = ReferenceHierarchyView.projectName;
          }
        }
      }
    }

    String onlyViewName ="";//$NON-NLS-1$
    String onlyModuleName = "";//$NON-NLS-1$
    
    String[] viewNameTokens = viewNameToOpen.split ("\\.");//$NON-NLS-1$
    if(viewNameTokens.length == 2){
      onlyModuleName = viewNameTokens[0];
      onlyViewName = viewNameTokens[1];
      currToken = onlyViewName;
    }else if(viewNameTokens.length == 1){
      currToken = viewNameToOpen;
    }else if(viewNameTokens.length > 2){
      onlyViewName = viewNameTokens[viewNameTokens.length - 1];
      onlyModuleName = viewNameTokens[0];
      for(int lp=1; lp < (viewNameTokens.length - 1); lp++){
        onlyModuleName = onlyModuleName + Constants.MODULE_ELEMENT_SEPARATOR + viewNameTokens[lp];
      }
    }

    try {
      editor = HandlerUtil.getActiveEditor(event);
      // Check if the project is modular type of non-modular type and then load aql library accordingly..
      IProject currProject = ProjectUtils.getProject (currProjName);
      boolean isModularProject = ProjectUtils.isModularProject (currProject);
      if(!isModularProject){
        aqlLibrary = Activator.getLibrary();
      }else{
        aqlLibrary = Activator.getModularLibrary ();
      }

      String searchPath = "";//$NON-NLS-1$
      try {
        if(currProject.hasNature(com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID))
        {
          SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties(currProject.getName());
          searchPath = ProjectPreferencesUtil.getAbsolutePath(properties.getSearchPath());
          // Check if the project is modular type or non-modular type and then calculate search path accordingly...
          boolean isModProj = ProjectUtils.isModularProject(currProject);
          if(isModProj){
            IResource srcDir = ProjectUtils.getTextAnalyticsSrcFolder (currProject); //Use this instead of reading from systemtproperties.
            if (srcDir != null) {
              searchPath = srcDir.getLocation ().toString ();
              searchPath = getAllSearchPaths(currProject, searchPath);
            } 
          }
        }
      }
      catch (CoreException e1) {
        e1.printStackTrace();
      }
      SystemTProperties prop = ProjectPreferencesUtil.getSystemTProperties(currProject);
      searchPathList = new ArrayList<String>();
      // Check if the project is modular type or non-modular type and then calculate search path list accordingly...
      if(!isModularProject){
        mainAQLFile = ProjectPreferencesUtil.getAbsolutePath(prop.getMainAQLFile());
        currentFileLocation = ProjectPreferencesUtil.getAbsolutePath(prop.getMainAQLFile());
        searchPathList = getSearchPathList(mainAQLFile, currentFileLocation, searchPath);
      }else{
        searchPathList = getSearchPathList15(currProject);
      }
      if(searchPathList == null){
        logger.logError ("AQL Library is empty");
        return null;
      }else{
     // calculate dictionary search path list..
        dictionarySearchPathList = getDictSearchPathList(searchPath);
        fileSet = getAllAQLFiles(searchPathList);
        fileList.addAll(fileSet);
      }
      
      List<AQLElement> elements = aqlLibrary.getElements(fileList);
      if(elements.size ()==0){
        logger.logError ("AQL Library is empty");
        return null;
      }
      boolean handled = false;
      Iterator<AQLElement> aqlElemIter = elements.iterator();
      handled:
        while(aqlElemIter.hasNext())
        {
          AQLElement elmt = aqlElemIter.next();
          String moduleName = "";//$NON-NLS-1$
          String aqlElementName = "";//$NON-NLS-1$
          String aqlEleAlias = "";
          
          moduleName = elmt.getModuleName ();
          if(moduleName == null){
            moduleName = "";
          }
          aqlEleAlias = elmt.getAliasName ();
          if(aqlEleAlias == null){
            aqlEleAlias = "";
          }
          
          aqlElementName = elmt.getUnQualifiedName ();
          if(aqlElementName == null){
            aqlElementName = "";
          }
          
          if(onlyModuleName.equals ("")){
            moduleName = ""; // when there is no module associated with the current token we select.
          }
          if((moduleName.equals(onlyModuleName))&&(aqlEleAlias.equals (currToken)))
          {
            if( elmt.getType() == Constants.AQL_ELEMENT_TYPE_IMPORT_VIEW ||
                elmt.getType() == Constants.AQL_ELEMENT_TYPE_OUTPUT_VIEW )
            {
              String file_p = elmt.getFilePath();//$NON-NLS-1$
              openFile(file_p, 0);
              File f = new File(file_p);
              if(didEditorOpen(f.getName()))
              {
                goToLine(newEditor, elmt.getBeginLine(), elmt.getBeginOffset()-1, elmt.getUnQualifiedName ().length ());
                handled = true;
                break handled;
              }
              else
              {
                openExternalEditor(f);
                handled = true;
                break handled;
              }
            }
          }
          if((moduleName.equals(onlyModuleName))&&(aqlElementName.equals(currToken)))
          {
            if(elmt.getType() == Constants.AQL_ELEMENT_TYPE_VIEW ||
                elmt.getType() == Constants.AQL_ELEMENT_TYPE_SELECT || 
                elmt.getType() == Constants.AQL_ELEMENT_TYPE_DICT || 
                elmt.getType() == Constants.AQL_ELEMENT_TYPE_DETAG || 
                elmt.getType() == Constants.AQL_ELEMENT_TYPE_FUNC || 
                elmt.getType() == Constants.AQL_ELEMENT_TYPE_TABLE || 
                elmt.getType() == Constants.AQL_ELEMENT_TYPE_EXTERNAL_VIEW ||
                elmt.getType () == Constants.AQL_ELEMENT_TYPE_EXTERNAL_TABLE)
            {
              String file_p = elmt.getFilePath();//$NON-NLS-1$
              openFile(file_p, 0);
              File f = new File(file_p);
              if(didEditorOpen(f.getName()))
              {
                goToLine(newEditor, elmt.getBeginLine(), elmt.getBeginOffset()-1, elmt.getUnQualifiedName ().length ());
                handled = true;
                break handled;
              }
              else
              {
                openExternalEditor(f);
                handled = true;
                break handled;
              }
            }
            else if(elmt.getType() == "INCLUDE")//$NON-NLS-1$
            {
              String fname = elmt.getName(); 
              Iterator<String> searchPathIter = searchPathList.iterator();
              while(searchPathIter.hasNext())
              {
                String newPath = searchPathIter.next(); //$NON-NLS-1$
                // newPath = newPath + "/" + fname;
                if(newPath.contains(fname) && new File(newPath).exists())
                {
                  //	open this file
                  openFile(newPath, 0);
                  if(didEditorOpen(new File(newPath).getName()))
                  {
                  }
                  else
                  {	
                    openExternalEditor(new File(newPath));
                  }
                  handled = true;
                  break handled;
                }
                else{}
              }
            }
          }
        }
      if(!handled)
      {	
        if(currToken.contains("/") || currToken.contains("."))//$NON-NLS-1$ //$NON-NLS-1$
        {
          //it could be a file, if it is open it
          Iterator<String> dictSearchPathIter = dictionarySearchPathList.iterator();
          while(dictSearchPathIter.hasNext())
          {
            String newPath = dictSearchPathIter.next(); //$NON-NLS-1$
            newPath = newPath + "/" + currToken;  //$NON-NLS-1$
            if(FileUtils.createValidatedFile(newPath).exists())
            {
              //	open this file
              openFile(newPath, 0);
              if(didEditorOpen(FileUtils.createValidatedFile(newPath).getName()))
              {
              }
              else
              {	
                openExternalEditor(FileUtils.createValidatedFile(newPath));
              }
            }
          }			
        }else{
          //Source not found and show message dialog..
          shell = HandlerUtil.getActiveShell(event);
          MessageBox errMsgDialog = new MessageBox(shell, SWT.ICON_ERROR);
          errMsgDialog.setMessage(com.ibm.biginsights.textanalytics.aql.editor.Messages.AQLEditor_MISSING_SOURCE_IN_AQL_FILES + currToken);
          errMsgDialog.setText(com.ibm.biginsights.textanalytics.aql.editor.Messages.AQLEditor_SOURCE_NOT_FOUND + currToken);
          errMsgDialog.open();
        }
      }
    }
    catch(ClassCastException e)
    {
      LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
        Constants.UNABLE_TO_OPEN_EDITOR_MESSAGE, e);
      e.printStackTrace();
    }
    catch(Exception e)
    {
      LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
        Constants.UNABLE_TO_OPEN_EDITOR_MESSAGE, e);
      e.printStackTrace();
    }
    return null;
  }

  private Set<String> getAllAQLFiles(ArrayList<String> searchPathList2) {
    Iterator<String> searchPathIter = searchPathList.iterator();
    // fileSet.add(currentFileLocation);  check if this is needed or not..
    //All files that are there in the search path
    while(searchPathIter.hasNext())
    {
      String temp =searchPathIter.next(); //$NON-NLS-1$
      IPath path = new Path(temp).makeAbsolute(); 
      try {
        new FileTraversalForFile() {
          public void onFile( final File f ) {
            if(f.getName().endsWith(".aql"))//$NON-NLS-1$
            {
              fileSet.add(f.getAbsolutePath().toString());
            }
          }
        }.traverse(new File(path.toOSString()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }	
    return fileSet;
  }

  private boolean didEditorOpen(String name) {
    IFileEditorInput ei = (IFileEditorInput) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
    if(name.equals(ei.getFile().getName()))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  private void openExternalEditor(File f) {
    IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(f.getAbsolutePath()));
    if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      try	{
        IDE.openEditorOnFileStore(page, fileStore);
      } 
      catch (PartInitException e) {
        e.printStackTrace();
      }
    }
  }

  private ArrayList<String> getDictSearchPathList(
    String searchPath) {
    String[] tokens = searchPath.split(";"); //$NON-NLS-1$.
    return new ArrayList<String>(Arrays.asList(tokens));
  }

  private ArrayList<String> getSearchPathList(String mainFile, String currentFileLocation,
    String searchPath) {

    String[] tokens = searchPath.split(";"); //$NON-NLS-1$.
    String contents = readFile(mainFile); //$NON-NLS-1$
    Set<String> mainPathSet = getPath(searchPath, tokens, contents);
    Set<String> currentpathSet = null;
    Set<String> pathSet = new HashSet<String>();
    if(!mainPathSet.contains(currentFileLocation)){
      contents = readFile(currentFileLocation);
      currentpathSet = getPath(searchPath, tokens, contents);
    }

    if(mainPathSet != null)
      pathSet.addAll(mainPathSet);
    if(currentpathSet != null)
      pathSet.addAll(currentpathSet);

    return new ArrayList<String>(pathSet);
  }

  private Set<String> getPath(String searchPath,
    String[] tokens, String contents) {
    Set<String> pathSet = new HashSet<String>();
    try {
      AQLParser parser = new AQLParser(contents);
      parser.setIncludePath(searchPath);
      parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser
      StatementList list = parser.parse();
      LinkedList<AQLParseTreeNode> nodeList = list.getParseTreeNodes();

      for (Iterator<AQLParseTreeNode> iterator = nodeList.iterator(); iterator
      .hasNext();) {
        AQLParseTreeNode node = iterator.next();
        if (node instanceof IncludeFileNode) {
          String comPath = getAbsolutePath(tokens,
            ((IncludeFileNode) node).getIncludedFileName()
            .getStr());//$NON-NLS-1$
          pathSet.add(comPath);
        }
      }
    } catch (ParseException e) {
      LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
        Constants.UNABLE_TO_OPEN_EDITOR_MESSAGE, e);
      e.printStackTrace();
    }catch (IOException e) {
      LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
        Constants.UNABLE_TO_OPEN_EDITOR_MESSAGE, e);
      e.printStackTrace();
    } 
    return pathSet;
  }
  
  private String getAllSearchPaths(IProject project, String searchPath)
  {
    if(searchPath == null){
      return null;
    }
    String finalSearchPath = "";
    String currPath = "";
    String[] moduleNames = ProjectUtils.getModules(project);
    if(moduleNames.length > 0){
      for(String str :moduleNames){
        currPath = searchPath + File.separator + str +";" ;
        finalSearchPath = finalSearchPath + currPath; 
      }
    }
    return finalSearchPath;
  }
  
  private ArrayList<String> getSearchPathList15(IProject project)
  {
    List<String> projectNames = new ArrayList<String> ();
    String projectName = project.getLocation().toOSString();
    projectNames.add (projectName);
    //System.out.println ("In getSearchPathList15() method... with Project name is: " +projectName);
    Set<String> aqlFilePaths = new HashSet<String>();
    if(projectName == null){
      logger.logError ("AQL Library is empty");
      return null;
    }
    IProject refProjects[] = null;
    //Calculating all the dependent projects of the current project..
    try {
      refProjects = project.getReferencedProjects ();
    }
    catch (CoreException e) {
      e.printStackTrace();
    }
    if((refProjects != null)&&(refProjects.length > 0)){
      for(IProject proj : refProjects){
        // collecting all the project names and later use them as keys to search for the source..
    	  if(proj.isOpen())
    		  projectNames.add(proj.getLocation ().toOSString ());
      }
    }
    // Calculating file paths for all the referenced module projects as well...
    for(String prjName : projectNames){
      AQLModuleProject modularProject =  aqlLibrary.getModuleLibraryMap().get(prjName);
      if(modularProject == null){
        logger.logError ("AQL Library is empty");
        return null;
      }
      HashMap<String, AQLModule> modules = modularProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet().iterator();
      String moduleName = "";
      while(moduleItr.hasNext()){
        moduleName = moduleItr.next();
        AQLModule aqlModule = modules.get (moduleName);
        List<String> paths = aqlModule.getAqlFilePaths ();
        for(String p : paths){
          aqlFilePaths.add(p);
        }
      }
    }// end of for loop..
    return new ArrayList<String>(aqlFilePaths);
  }


  private String getAbsolutePath(String[] searchPathList, String fileName) {
    // All files that are there in the search path
    for (String directory : searchPathList) {
      String newPath = directory + File.separator + fileName; //$NON-NLS-1$
      File file = new File(newPath);
      if (file.exists()) {
        return newPath;
      }
    }
    return null;
  }

  private String readFile(String mainFileLocation) {
    File file = FileUtils.createValidatedFile(mainFileLocation);
    StringBuffer content = new StringBuffer();
    BufferedReader bufferedReader = null;
    String lineSeperator = System.getProperty("line.separator"); //$NON-NLS-1$

    try {
      bufferedReader = new BufferedReader(new FileReader(file));
      String text = null;//$NON-NLS-1$
      while ((text = bufferedReader.readLine()) != null) {
        content.append(text + lineSeperator);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (bufferedReader != null) {
          bufferedReader.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return content.toString();
  }

  private void openFile(String fullpath, int offset) {
    IPath path = new Path(fullpath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
    if(file == null) return;
    //17886: On F3 the following code will open the files in their 
    //respective editors or if its not a aql or .dict file then it will
    //look for its default editor from eclipse.
    try {		
      if(file.getFileExtension().equals(Constants.AQL_FILE_EXTENSION_STRING))
      {
        newEditor = (AQLEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(file), EDITOR_ID);
        s = (StyledText) newEditor.getAdapter(Control.class);
      }
      else if(file.getFileExtension().equals(Constants.DICTIONARY_FILE_EXTENSION_STRING))
      {
        newDictEditor = (DictionaryEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(file), DICT_EDITOR_ID);
        s = (StyledText) newDictEditor.getAdapter(Control.class);
      }
      else
      {
        IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
        TextEditor te = (TextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(file), desc.getId());
        s = (StyledText) te.getAdapter(Control.class);
      }
      s.setCaretOffset(offset);
    } catch (PartInitException e) {
      e.printStackTrace();
    }
  }

  public static String getCurrentFileRealPath(){
    IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    IWorkbenchPage page = win.getActivePage();
    if (page != null) {
      IEditorPart editor = page.getActiveEditor();
      if (editor != null) {
        IEditorInput input = editor.getEditorInput();
        if (input instanceof IFileEditorInput) {
          return ((IFileEditorInput)input).getFile().getLocation().toOSString();
        }
      }
    }
    return null;
  }

  public static String getFileLocation(String stringPath)
  {
    File fd=FileUtils.createValidatedFile(stringPath);
    fd = fd.getParentFile();
    return fd.toString();	
  }


  private static void goToLine(IEditorPart editorPart, int lineNumber, int start, int end) {
    if (!(editorPart instanceof ITextEditor) || lineNumber <= 0) {
      return;
    }
    ITextEditor editor = (ITextEditor) editorPart;
    IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
    if (document != null) 
    {
      IRegion lineInfo = null;
      try 
      {
        lineInfo = document.getLineInformation(lineNumber - 1);
      } 
      catch (BadLocationException e){	     
      }
      if (lineInfo != null) 
      {
        editor.selectAndReveal(lineInfo.getOffset() + start, end);
      }
    }
  }

  public IRegion getHoverRegion(IDocument doc, int offset) {
    return AQLWordFinder.findWord(doc, offset);
  }
}

class FileTraversalForFile {
  public final void traverse( final File f ) throws IOException {
    //		we don't need recursive traversal at this point, but we may need it
    if (f.isDirectory()) {
      //onDirectory(f);
      final File[] childs = f.listFiles();
      for( File child : childs ) {
        traverse(child);
      }
      return;
    }
    onFile(f);            
  }
  // If requires we can implement below method...
  /* void onDirectory( final File d ) {
    	} */

  void onFile( final File f ) {
    // this is implemented while while initiating the call.
  }
}
