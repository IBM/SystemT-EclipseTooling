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
package com.ibm.biginsights.textanalytics.aql.editor.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import com.ibm.avatar.aql.AQLParser;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.StatementList;
import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.Messages;
import com.ibm.biginsights.textanalytics.aql.library.AQLParseErrorHandler;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;


/**
 *  Babbar
 *  Kalakuntla
 */

public class ReconcilingStrategy implements IReconcilingStrategy,IReconcilingStrategyExtension {
	@SuppressWarnings("unused")


  AQLEditor editor;
  ArrayList<String> searchPathList;
  boolean isModularProject;
  
  IProject currProject = null;
  
  private static final ILog logger = LogUtil
  .getLogForPlugin(Activator.PLUGIN_ID);
  
  public ReconcilingStrategy(AQLEditor editor) {
    super();
    this.editor = editor;
  }

  public ReconcilingStrategy() {
  }

  @Override
  public void setDocument(IDocument document) {
  }

  @Override
  public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
    // Don't need to implement as we're not doing incremental reconciliation
  }

  @Override
  public void reconcile(IRegion partition) {
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        try {
          //we will pass the current state of the file to the parser, not the saved state
          IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
          IEditorPart editorPart = page.getActiveEditor();
          //we want to get changes only from the AQL editor
          if(!(editorPart instanceof AQLEditor))
            return;
          ITextEditor txtEditor = (ITextEditor) editorPart;
          StyledText s = (StyledText) txtEditor.getAdapter(Control.class);
          IEditorInput genericInput = editor.getEditorInput ();
          if (!(genericInput instanceof IFileEditorInput)){
            return;
          }
          IFileEditorInput input = (IFileEditorInput)genericInput ;
          IFile inputFile = input.getFile();
          IPath aqlFilePath = inputFile.getLocation();

          AQLParseErrorHandler reporter = new AQLParseErrorHandler();

          // Don't bother parsing invalid AQL file
          if (!ProjectUtils.isValidAQLFile20 (inputFile)) {
            // The file may be moved from a valid location where it was compiled
            // and marked with errors. We need to remove those markers. (defect 27058)
            reporter.deleteAqlMarkers (inputFile);
            reporter.setIgnoreAqlWarning (inputFile);
            return;
          }
          // valid AQL file -> remove "invalid AQL file" warning if it exists
          else {
            reporter.removeIgnoreAqlWarning (inputFile);
          }

          String aqlLoc = aqlFilePath.toOSString();
          IProject project = input.getFile().getProject();
          boolean isInSearchPath = ProjectPreferencesUtil.isAQLInSearchPath(project, aqlLoc);

          //parse only the files that have .aql extension, refer 17886
          if(StringUtils.isEmpty(aqlFilePath.getFileExtension()) || 
        		  !(aqlFilePath.getFileExtension().equals(Constants.AQL_FILE_EXTENSION_STRING)) 
        		  || !isInSearchPath)						  
            return;
          
          //using StyledText to get the current state of the unsaved file so that we can use it 
          //for updating AQLLibrary. file encoding is being used by styled text. (Refer 17128)
          AQLParser parser = new AQLParser(s.getText(),aqlFilePath.toOSString());
          isModularProject = ProjectUtils.isModularProject(inputFile.getProject ());
          parser.setBackwardCompatibilityMode(!isModularProject);
          parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser
          StatementList statementList = parser.parse();
          String prjName = getProjectName(aqlFilePath.toOSString());
          if(!isModularProject){
            editor.model.update(aqlFilePath.toOSString(), prjName, statementList);
            editor.update(); 
          }else{
            String moduleName = getModulePath(aqlFilePath.toOSString());
            editor.modularModel.update (aqlFilePath.toOSString(), prjName, moduleName, statementList);
            editor.update();
          }
          //handle errors while reconciling
          LinkedList<ParseException> parseException = statementList.getParseErrors();
          Iterator<ParseException> expItr = parseException.iterator();
          //mark errors on type only if property is set to true
          if(isReportErrorsEnabled())
          {
            reporter.deleteMarkers(inputFile);
            while(expItr.hasNext())
            {
              try 
              {
                ParseException pe1 = expItr.next();
                reporter.handleError(pe1, IMarker.SEVERITY_ERROR, inputFile);
              } 
              catch (Exception ex) 
              {
                logger.logError (ex.getMessage ());
              }	
            }
          }
        } catch (Exception ex) {
          logger.logError (ex.getMessage ());
        }
      }
    });
  }

  private boolean isReportErrorsEnabled ()
  {
    TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
    return wprefs.getPrefEnableReportProblem ();	  
  }

  /**
   * {@inheritDoc}
   * <p>It is called when there are open files on starting eclipse, or on opening a file in aql editor.
   * It parses all aql files in the project to which the current open file belongs, 
   * as well as aql files in all projects that the current project is dependent on.</p>
   * <p>Any parse errors found are marked. Relevant aql elements are added to aql library.</p>
   */
  @Override
  public void initialReconcile() {
    String searchPath = ""; //$NON-NLS-1$
    String refSearchPath = "";  //$NON-NLS-1$
    IEditorInput genericInput = editor.getEditorInput ();
    if (!(genericInput instanceof IFileEditorInput)) {
      return;
    }
    IFileEditorInput input = (IFileEditorInput) genericInput;
    try {
      IProject project = input.getFile().getProject();
      currProject = project;
      isModularProject = ProjectUtils.isModularProject(project);

      if(!isModularProject){
        if(project.isOpen() && project.hasNature(com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID))
        {
          SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties(project.getName());
          searchPath = ProjectPreferencesUtil.getAbsolutePath(properties.getSearchPath());
        }
        else{
        	return;
        }
       
      }else{
        if(project.isOpen() && project.hasNature(com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID))
        {
          IResource srcDir = ProjectUtils.getTextAnalyticsSrcFolder (project);
          if (srcDir != null) {
            searchPath = srcDir.getLocation ().toString();
            refSearchPath = getSearchPathForRefProjects(project);
            searchPath = searchPath + Constants.DATAPATH_SEPARATOR + refSearchPath; 
          }
        }
        else
        {
          searchPath = input.getFile().getParent().getLocation ().toString ();
        }
      }
      //put encoding check when file opens and display warning message if file and project encoding is different:
      if(!input.getFile().getCharset().equals(input.getFile().getProject().getDefaultCharset())) 
      { 
        LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowWarning(Messages.AQLEditor_ENCODING_WARNING);
      }
    }
    catch (CoreException ex) {
      logger.logError (ex.getMessage ());
    }

    if(!isModularProject){
      //add hashcode of searchpath to the locationsParsed
      if((Activator.getLibrary().getParsedPath() != null) && (Activator.getLibrary().getParsedPath().contains(searchPath.hashCode())))
      {
        //do nothing, library already exist for files for this searchpath
      }
      else
      {
        //System.out.println ("Non modular project..");
        searchPathList = new ArrayList<String>();
        final Set<String> fileList = new HashSet<String>();
        searchPathList = getSearchPathList(searchPath);
        Iterator<String> pathItr = searchPathList.iterator();
        //this will only iterate through the searchpath list, so if something is not given in the searchpath list
        //or if searchpath is initially not setup then the user will not be able to get the info for files not in searchpath
        //in this case we should atlest create parse tree node for the current file, if nothing is there.
        while(pathItr.hasNext())
        {
          String temp = pathItr.next();
          IPath path = new Path(temp).makeAbsolute(); 
          try {
            new FileTraversal() {
              public void onFile( final File f ) {
                if(f.getName().endsWith(Constants.AQL_FILE_EXTENSION))
                {
                  String path = f.getAbsolutePath();
                  if(ProjectPreferencesUtil.isAQLInSearchPath(currProject, path)){
                    fileList.add(f.getAbsolutePath().toString());
                  }
                }
              }
            }.traverse(new File(path.toOSString()));
          } catch (IOException ex) {
            logger.logError (ex.getMessage ());
          }
          catch (NullPointerException ex) {
            logger.logError (ex.getMessage ());
            //LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logInfo(Messages.AQLEditor_NO_BIGINSIGHTS_NATURE); //$NON-NLS-1$
          }
        }
        Iterator<String> fileListItr = fileList.iterator();
        while(fileListItr.hasNext())
        {
          String aqlFilePath = fileListItr.next();
          AQLParser parser;
          try {
            parser = new AQLParser(FileUtils.fileToStr(new File(aqlFilePath), getProject(aqlFilePath).getDefaultCharset()),aqlFilePath);
            isModularProject = ProjectUtils.isModularProject(getProject(aqlFilePath));
            parser.setBackwardCompatibilityMode(!isModularProject);
            parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser
            StatementList statementList = parser.parse();         
            String prjName = getProjectName(aqlFilePath);
            editor.model.create(aqlFilePath, prjName, statementList);
            AQLParseErrorHandler reporter = new AQLParseErrorHandler();
            LinkedList<ParseException> parseException = statementList.getParseErrors();
            Iterator<ParseException> expItr = parseException.iterator();
            IPath path = new Path(aqlFilePath);
            IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
            reporter.deleteMarkers(file);
            while(expItr.hasNext())
            {
              try {
                ParseException pe1 = expItr.next();
                reporter.handleError(pe1, IMarker.SEVERITY_ERROR, file);
              } catch (Exception e) {
              } 
            }    

          } catch (IOException e) {
          } catch (CoreException ex) {
            logger.logError (ex.getMessage ());
          } 
        }
        Activator.getLibrary().addParsedPath(searchPath.hashCode());
      }

    }else{
      // If its a modular project... add hashcode of searchpath to the locationsParsed
      if((Activator.getModularLibrary ().getParsedPath () != null) && (Activator.getModularLibrary().getParsedPath().contains(searchPath.hashCode())))
      {
        //do nothing, library already exist for files for this searchpath
      }
      else
      {
        searchPathList = new ArrayList<String>();
        //fileList = new ArrayList<String>();
        final Set<String> fileList = new HashSet<String>();
        searchPathList = getSearchPathList(searchPath);
        Iterator<String> pathItr = searchPathList.iterator();
        //this will only iterate through the searchpath list, so if something is not given in the searchpath list
        //or if searchpath is initially not setup then the user will not be able to get the info for files not in searchpath
        //in this case we should atlest create parse tree node for the current file, if nothing is there.
        while(pathItr.hasNext())
        {
          String temp = pathItr.next();
          IPath path = new Path(temp).makeAbsolute(); 
          try {
            new FileTraversal() {
              public void onFile( final File f ) {
                IPath resFilePath = new Path(f.getAbsolutePath ().toString ());
                final IFile resFile = FileBuffers.getWorkspaceFileAtLocation(resFilePath);
                // Adding the logic to ignore the aql file under sub folders..
                if((f.getName().endsWith(Constants.AQL_FILE_EXTENSION))&&(ProjectUtils.isValidModule (resFile.getProject(), resFile.getParent ().getName ()))) 
                {
                  //resfile need not be from current project. It can be from one of the required projects.
                  fileList.add(f.getAbsolutePath().toString());
                }
              }
            }.traverse(new File(path.toOSString()));
          } catch (IOException ex) {
            logger.logError (ex.getMessage ());
          }
          catch (NullPointerException ex) {
            logger.logError (ex.getMessage ());
            //LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logInfo(Messages.AQLEditor_NO_BIGINSIGHTS_NATURE); //$NON-NLS-1$
          }
        }
        Iterator<String> fileListItr = fileList.iterator();
        while(fileListItr.hasNext())
        {
          String aqlFilePath = fileListItr.next();
          AQLParser parser;
          try {
            parser = new AQLParser(FileUtils.fileToStr(new File(aqlFilePath), getProject(aqlFilePath).getDefaultCharset()),aqlFilePath);
            isModularProject = ProjectUtils.isModularProject(getProject(aqlFilePath));
            parser.setBackwardCompatibilityMode(!isModularProject);
            parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser
            StatementList statementList = parser.parse();         
            String prjName = getProjectName(aqlFilePath);
            String moduleName = getModulePath(aqlFilePath);
            editor.modularModel.create(aqlFilePath, prjName, moduleName, statementList);
            AQLParseErrorHandler reporter = new AQLParseErrorHandler();
            LinkedList<ParseException> parseException = statementList.getParseErrors();
            Iterator<ParseException> expItr = parseException.iterator();
            IPath path = new Path(aqlFilePath);
            IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
            reporter.deleteMarkers(file);
            while(expItr.hasNext())
            {
              try {
                ParseException pe1 = expItr.next();
                reporter.handleError(pe1, IMarker.SEVERITY_ERROR, file);
              } catch (Exception e) {
              } 
            }    

          } catch (IOException e) {
          } catch (CoreException ex) {
            logger.logError (ex.getMessage ());          
          } 
        }
        Activator.getModularLibrary ().addParsedPath (searchPath.hashCode ());
      }
    }
    //add the hashcode to parsedpath so next time it wont parse the searchpath when another file is openened
    //editor.aqlLibrary.addParsedPath(searchPath.hashCode());
    //Activator.getLibrary().addParsedPath(searchPath.hashCode());
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        try {	
          editor.update();
        } catch (Exception ex) {
          logger.logError (ex.getMessage ());
        }
      }
    });
  }

  private String getSearchPathForRefProjects (IProject project)
  {
    IProject[] refProjs =null;
    String refSearchPath = "";
    try {
      refProjs = project.getReferencedProjects();
      for(IProject prj : refProjs){
        IResource srcDir = ProjectUtils.getTextAnalyticsSrcFolder (prj);
        if (srcDir != null) {
          refSearchPath += srcDir.getLocation ().toString () + Constants.DATAPATH_SEPARATOR;
        }
      }
    }
    catch (CoreException e) {
      e.printStackTrace();
    }
    return refSearchPath;
  }

  public String getProjectName(String aqlFilePath) 
  {
    IPath path = new Path(aqlFilePath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
    if(file == null) return null;
    else
    {
      return file.getProject().getLocation().toOSString();
    }
  }

  public String getModulePath(String aqlFilePath) 
  {
    IPath path = new Path(aqlFilePath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
    if(file == null) return null;
    else
    {
      return file.getParent ().getLocation ().toOSString ();
    }
  }

  @Override
  public void setProgressMonitor(IProgressMonitor arg0) {
    // Not implementing it right now
  }

  public ArrayList<String> getSearchPathList(String searchPath) {
    String[] tokens = searchPath.split(Constants.DATAPATH_SEPARATOR); 
    return new ArrayList<String>(Arrays.asList(tokens));
  }

  private IProject getProject(String aqlFilePath) 
  {
    IPath path = new Path(aqlFilePath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
    if(file == null) return null;
    else
    {
      return file.getProject();
    }
  }

  public String readFileAsString(String filePath)	throws java.io.IOException
  {
    StringBuffer fileData = new StringBuffer(1000);
    BufferedReader reader = new BufferedReader(new FileReader(filePath));
    char[] buf = new char[1024];
    int numRead=0;
    while((numRead=reader.read(buf)) != -1)
    {
      String readData = String.valueOf(buf, 0, numRead);
      fileData.append(readData);
      buf = new char[1024];
    }
    reader.close();
    return fileData.toString();
  }
}

class FileTraversal {
  public final void traverse(final File f) throws IOException {
    if (f.isDirectory()) {
      onDirectory(f);
      final File[] childs = f.listFiles();
      if(childs != null && childs.length > 0){
        for( File child : childs ) {
          traverse(child);
        }
      }
      return;
    }
    onFile(f);   
  }
  public void onDirectory( final File d ) {
  }
  public void onFile(final File f) {
  }
}
