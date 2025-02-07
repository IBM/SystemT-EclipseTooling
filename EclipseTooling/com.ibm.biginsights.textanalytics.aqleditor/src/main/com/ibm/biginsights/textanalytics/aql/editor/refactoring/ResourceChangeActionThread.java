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
package com.ibm.biginsights.textanalytics.aql.editor.refactoring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.avatar.aql.AQLParser;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.StatementList;
import com.ibm.biginsights.project.MigrateProject;
import com.ibm.biginsights.project.MigrateProject.MigrationTestResult;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.biginsights.project.util.BIProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.aql.editor.AQLEditor;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.DictionaryEditor;
import com.ibm.biginsights.textanalytics.aql.editor.Messages;
import com.ibm.biginsights.textanalytics.aql.library.AQLModel;
import com.ibm.biginsights.textanalytics.aql.library.AQLParseErrorHandler;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.aql.library.ModularAQLModel;
import com.ibm.biginsights.textanalytics.migration.action.MigrateTextAnalyticsProject;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.CustomMessageBox;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
*
*  Babbar, Rajeshwar Kalakuntla
* 
*/
public class ResourceChangeActionThread  extends Thread {

	@SuppressWarnings("unused")


	ArrayList<String> searchPathList;
	public static IAQLLibrary aqlLibrary = null;
	AQLEditor editor = null;
	public static final String COMPILE_MARKER_TYPE = "com.ibm.biginsights.textanalytics.aqleditor.compileerror";//$NON-NLS-1$
	List<String> openedFiles = null;
	
	AQLEditor newEditor;
  DictionaryEditor newDictEditor;
  StyledText s;
	
  private Set<IResource> addedResources = new HashSet<IResource> ();
  private Set<IResource> removedResources = new HashSet<IResource> ();
  private Set<IResource> changedResources = new HashSet<IResource> ();

  AQLModel model;
	ModularAQLModel modularModel;
	boolean deleteAction = false;
	boolean addedAction = false;
	boolean changeAction = false;
	boolean renameAction = false;
	boolean valid = false;
	boolean isModularProject = true;
	private IProject currentProject = null;
	public static final String DEFAULT_RESULT_DIR = ".result"; //$NON-NLS-1$
	public static final String DEFAULT_AOG_DIR = ".aog"; //$NON-NLS-1$
	public static final String AQL_EXT = ".aql"; //$NON-NLS-1$
	public static final String AQL_STRING = "aql"; //$NON-NLS-1$
	public static final String TEXT_ANALYTICS = "textanalytics"; //$NON-NLS-1$
	final String SEMI_COLON = ";"; //$NON-NLS-1$
	public static final String EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.AQLEditor"; //$NON-NLS-1$
	public static final String DICT_EDITOR_ID = "com.ibm.biginsights.textanalytics.editor.ui.DictEditor"; //$NON-NLS-1$
	
	private static final ILog logger = LogUtil
  .getLogForPlugin(Activator.PLUGIN_ID);
	

	public ResourceChangeActionThread(IResource removedResource, IResource addedResource, IResource changedResource) {
		super();

		// Loading the library and creating the model..
    if (!isModularProject) {
      aqlLibrary = Activator.getLibrary ();
      model = (AQLModel) aqlLibrary.getAQLModel ();
    }
    else {
      aqlLibrary = Activator.getModularLibrary ();
      modularModel = (ModularAQLModel) aqlLibrary.getAQLModel ();
    }

    this.removedResources = new HashSet<IResource> ();
    if (removedResource != null)
      this.removedResources.add (removedResource);

    this.addedResources = new HashSet<IResource> ();
    if (addedResource != null)
      this.addedResources.add (addedResource);

    this.changedResources = new HashSet<IResource> ();
    if (changedResource != null)
      this.changedResources.add (changedResource);
	}

  public ResourceChangeActionThread(Set<IResource> removedResources, Set<IResource> addedResources, Set<IResource> changedResources) {
    super();
    // Loading the library and creating the model..
    if (!isModularProject) {
      aqlLibrary = Activator.getLibrary ();
      model = (AQLModel) aqlLibrary.getAQLModel ();
    }
    else {
      aqlLibrary = Activator.getModularLibrary ();
      modularModel = (ModularAQLModel) aqlLibrary.getAQLModel ();
    }

    this.removedResources = removedResources;
    this.addedResources = addedResources;
    this.changedResources = changedResources;
  }
	
	public void run(){

	  Set<IResource> resources = null;

	  //Detect action mode: deleteAction or renameAction or added action
		if ( (addedResources == null || addedResources.isEmpty ()) &&
		     (removedResources != null && !removedResources.isEmpty ()) ) {
			deleteAction = true;
		}
		else if ( (addedResources != null && !addedResources.isEmpty ()) &&
              (removedResources == null || removedResources.isEmpty ()) ) {
			addedAction = true;
      resources = addedResources;
		}
		else if ( (addedResources != null && !addedResources.isEmpty ()) &&
              (removedResources != null && !removedResources.isEmpty ()) ) {
			renameAction = true;
      resources = addedResources;
    }
		else if (changedResources != null && !changedResources.isEmpty ()) {
			changeAction = true;
      resources = changedResources;
		}

		if (resources == null)
		  return;

		try {
			if (deleteAction)
				handleDeleteAction();

			else if (addedAction)
				handleAddedAction();

			else if (renameAction)
				handleRenameAction();

			else if (changeAction)
				handleChangeAction();

			refreshProjects (resources);
		}
		catch (CoreException e) {
		  logger.logError (e.getMessage ());
		}
	}

	private void refreshProjects (Set<IResource> resources) throws CoreException
	{
	  if (resources == null)
	    return;

	  Set<IProject> projects = new HashSet<IProject> ();

    // Get containing projects
    for (IResource r : resources) {
      projects.add (r.getProject ());
    }

    // Refresh containing projects
    for (IProject p : projects) {
      p.refreshLocal(IResource.DEPTH_INFINITE, null);
    }
	}

	private void reloadExtractionPlan ()
  {
    String oldProject = null;
    String newProject = null;

    // Rename action. Only 1 resource can be renamed at a time
    if (!removedResources.isEmpty () && !addedResources.isEmpty ()) {
      IResource remResrc = removedResources.iterator ().next ();

      if (remResrc instanceof IProject) {
        IResource addResrc = addedResources.iterator ().next ();

        oldProject = ((IProject)remResrc).getName ();
        newProject = ((IProject)addResrc).getName ();
        ProjectUtils.refreshExtractionPlan (oldProject, newProject);

        return;
      }
    }

    // .extractionplan is modified.
    for (IResource r : changedResources) {
      if (r.getName ().equals (Constants.EXTRACTION_PLAN_FILE_NAME)) {
        oldProject = r.getProject ().getName ();
        newProject = oldProject;
        ProjectUtils.refreshExtractionPlan (oldProject, newProject);
      }
	  }
  }

	 private void handleChangeAction()
	 {
	   for (IResource r : changedResources) {
	     handleChangeAction (r);
	   }
	 }

  private void handleChangeAction (IResource changedResource)
  {
    if (changedResource != null) {
      if (changedResource.getFileExtension () != null &&
          changedResource.getFileExtension ().equals (Constants.MODULE_COMMENT_FILE_EXTENSION))
        updateCommentInfo (changedResource);
      else
        parseSearchPath (changedResource);
    }
    if (changedResource.getFileExtension () != null &&
        changedResource.getFileExtension ().equalsIgnoreCase (Constants.TA_PROPS_EXTENSION_STRING)) {
      // Check for module source folder existence
      checkTextAnalyticsSrcExist (changedResource);
    }
  }

  private void handleAddedAction ()
  {
    for (IResource r : addedResources) {
      handleAddedAction (r);
    }
  }

  private void handleAddedAction (IResource addedResource)
  {
    switch (addedResource.getType ()) {
      case IResource.FILE:
        if (addedResource.getFileExtension ().equals (Constants.MODULE_COMMENT_FILE_EXTENSION)) {
          updateCommentInfo (addedResource);
        }
        else {
          parseSearchPath (addedResource);
        }
      break;
      case IResource.FOLDER:
        parseSearchPath (addedResource);
        // Need to check if the src folder is available..
        checkTextAnalyticsSrcExist (addedResource);
      break;
      case IResource.PROJECT:
        try {
          if (renameAction && ((IProject) addedResource).hasNature (Constants.PLUGIN_NATURE_ID)) {
            // Updating aql library is not required for a newly added or imported project.
            // It will happen in ReconcilingStrategy.initialReconcile(..) when an aql file is opened from the project.
            updateAQLLibrary (addedResource);
            // parseSearchPath(addedResource);
          }
          else if (addedAction && ((IProject) addedResource).hasNature (Constants.PLUGIN_NATURE_ID)) {
            IProject prj = (IProject) addedResource;
            // This is to check if the imported project requires migration or not then display warnign message
            // accordingly..
            if (prj.isOpen () & ProjectUtils.isModularProject (prj)) {

              // Defect 56385: Create missing src folders.
              MigrateProject.createMissingJavaSrcFolders (prj);

              MigrationTestResult testResult = MigrateProject.isMigrationRequired (prj);
              if (testResult.isMigrationRequired) {
                if (testResult.projectVersion.equals (BIConstants.BIGINSIGHTS_VERSION_V2)) {
                  // This step is required only if the old version of the project is v2.0.
                  // When importing a v20 project into a workspace with v21 plugin,
                  // migrate the text analytics properties to a format acceptable to
                  // v21 plugin.
                  new MigrateTextAnalyticsProject ().migrate (prj, testResult.projectVersion,
                    testResult.bigInsightsVersion);
                }
                if (BIProjectPreferencesUtil.isAtLeast (testResult.projectVersion, BIConstants.BIGINSIGHTS_VERSION_V2)) {
                  // Display this warning if the the old version of the project is v2.0 or higher, and if migration is
                  // required.
                  Display.getDefault ().syncExec (new Runnable () {
                    @Override
                    public void run ()
                    {
                      Shell shell = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getShell ();
                      CustomMessageBox msgBox = CustomMessageBox.createInfoMessageBox (shell,
                        Messages.ResourceChangeActionDelegate_WARNING,
                        Messages.ProjectAddedActionMessage_MIGRATION_WARNING);
                      msgBox.open ();
                    }
                  });
                }
              }
            }

          }
        }
        catch (CoreException e) {
          logger.logError (e.getMessage ());
        }
      break;
    }
  }

	/**
	 * This method is to check for the existence of source module folder and add a error 
	 * marker if it doesn't exists 
	 * @param IResource resource the resource which is updated
	 */
  private void checkTextAnalyticsSrcExist (IResource resource)
  {
    if (resource == null) return;
    IProject project = resource.getProject ();
    if ((resource instanceof IFolder) || (resource instanceof IFile)) {
      // IFolder resourceFolder = (IFolder) resource;
      IFolder srcFolder = null;
      if (ProjectUtils.isModularProject (project)) {
        srcFolder = ProjectUtils.getTextAnalyticsSrcFolder (project);
        if (srcFolder != null && srcFolder.exists ()) {
          // Do nothing..
          deleteMarker (project,
            com.ibm.biginsights.textanalytics.aql.editor.Messages.AQL_SRC_MODULE_PATH_NOT_CONFIGURED);
        }
        else {
          ProjectUtils.addMarker (project, COMPILE_MARKER_TYPE, IMarker.SEVERITY_ERROR, IMarker.PRIORITY_HIGH,
            com.ibm.biginsights.textanalytics.aql.editor.Messages.AQL_SRC_MODULE_PATH_NOT_CONFIGURED);
        }
      }
    }
  }
	
	 /**
   * This method removes the marker with the given message
   * created against the given project's directory.
   * @param project
   * @param errorMessage
   */
	private void deleteMarker (IProject project, String errorMessage)
  {
	  try {
      boolean exist = false;
      IMarker[] problems = null;
      int depth = IResource.DEPTH_INFINITE;
      try {
        problems = project.findMarkers(COMPILE_MARKER_TYPE, true, depth);
      } catch (CoreException e) {
        logger.logError(e.getMessage());
        problems = new IMarker[0];
      }
      for (int i = 0; i < problems.length; i++) {

        if (problems[i].getAttribute(IMarker.MESSAGE).equals(
          errorMessage)) {
          exist = true;
        }
      }
      if (exist) {
        project.deleteMarkers (COMPILE_MARKER_TYPE, true, depth);
      }
    } catch (CoreException e) {
      logger.logError(e.getMessage());
    }
    
  }

	 private void handleDeleteAction () {
     for (IResource r : removedResources) {
       handleDeleteAction (r);
     }
	 }
	
  private void handleDeleteAction (IResource removedResource)
  {
    switch (removedResource.getType ()) {
      case IResource.FILE:
        try {
          // parseSearchPath(removedResource);
          if (!isModularProject) {
            model.deleteAQLFile (removedResource.getLocation ().toOSString (),
              removedResource.getProject ().getLocation ().toOSString ());
          }
          else {
            if (removedResource.getFileExtension ().equals (AQL_STRING)) {
              modularModel.deleteAQLFile (removedResource.getLocation ().toOSString (),
                removedResource.getProject ().getLocation ().toOSString (),
                removedResource.getParent ().getLocation ().toOSString ());
            }
            else if (removedResource.getFileExtension ().equals (Constants.MODULE_COMMENT_FILE_EXTENSION)) {
              modularModel.updateModuleComment (removedResource.getProject ().getLocation ().toOSString (),
                removedResource.getParent ().getLocation ().toOSString ());
            }
          }
        }
        catch (NullPointerException ex) {
          logger.logError (ex.getMessage ());
        }
      break;
      case IResource.FOLDER:
        try {
          parseSearchPath (removedResource);
          // Need to check if the src folder is available..
          checkTextAnalyticsSrcExist (removedResource);
        }
        catch (Exception ex) {
          logger.logError (ex.getMessage ());
        }
      break;
      case IResource.PROJECT:
        try {
          String remProjectLocation = removedResource.getProject ().getLocation ().toOSString ();

          // Delete the Project Instances from Library.
          if (!ProjectUtils.isModularProject ((IProject) removedResource)) {
            if (aqlLibrary.getLibraryMap ().containsKey (remProjectLocation)) {
              model.deleteAQLProject (remProjectLocation);
              // model.deleteAQLProject (removedResource.getProject ().getLocation ().toOSString ());
            }
          }
          else {
            // Delete the entry of its available in non mod library map
            if (model != null) {
              model.deleteAQLProject (remProjectLocation);
            }
            aqlLibrary = Activator.getModularLibrary ();
            modularModel = (ModularAQLModel) aqlLibrary.getAQLModel ();
            if (aqlLibrary.getModuleLibraryMap ().containsKey (remProjectLocation)) {
              // modularModel.deleteAQLProject (removedResource.getProject ().getLocation ().toOSString ());
              modularModel.deleteAQLProject (remProjectLocation);
            }
            // Need to deleted the parsedPath values also..
          }
          // parseSearchPath(removedResource);
        }
        catch (NullPointerException ex) {
          logger.logError (ex.getMessage ());
        }
      break;
    }
  }
	
	private void handleRenameAction() {
		handleDeleteAction();
		handleAddedAction();
	}
	
	//private String get
	private String getProjectName(String aqlFilePath) 
	{
		IPath path = new Path(aqlFilePath);
		final IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
		if(file == null) return null;
		else
		{
			return file.getProject().getLocation().toOSString();
		}
	}

  public void updateAQLLibrary (IResource res)
  {
    // try to close all editors before refactoring..
    Display.getDefault ().asyncExec (new Runnable () {
      public void run ()
      {
        try {
          Map<IEditorReference, String> openEditors = getAllOpenEditors ();
          Set<String> files = new LinkedHashSet<String> (openEditors.values ()); // removing duplicates if they turned
                                                                                 // up
          openedFiles = new ArrayList<String> (files);
          closeEditors (openEditors.keySet ().toArray (new IEditorReference[0]));
        }
        catch (Exception ex) {
          logger.logError (ex.getMessage ());
        }
      }
    });

    IResource resource = res;
    String searchPath = ""; //$NON-NLS-1$
    String refSearchPath = ""; //$NON-NLS-1$
    IProject project = null;
    boolean isModularProject = true;
    try {
      project = resource.getProject ();
      currentProject = project;
      isModularProject = ProjectUtils.isModularProject (project);
      if (!isModularProject) {
        if (project.isOpen () && project.hasNature (com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID)) {
          SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties (project.getName ());
          searchPath = ProjectPreferencesUtil.getAbsolutePath (properties.getSearchPath ());
        }
      }
      else {
        if (project.isOpen () && project.hasNature (com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID)) {
          IResource srcDir = ProjectUtils.getTextAnalyticsSrcFolder (project);
          if (srcDir != null) {
            searchPath = srcDir.getLocation ().toString ();
            refSearchPath = getSearchPathForRefProjects (project);
            searchPath = searchPath + SEMI_COLON + refSearchPath;
          }
        }
      }
    }
    catch (CoreException ex) {
      logger.logError (ex.getMessage ());
    }

    if (!isModularProject) {
      // add hashcode of searchpath to the locationsParsed
      if ((Activator.getLibrary ().getParsedPath () != null)
        && (Activator.getLibrary ().getParsedPath ().contains (searchPath.hashCode ()))) {
        // do nothing, library already exist for files for this searchpath
      }
      else {
        // System.out.println ("Non modular project..");
        searchPathList = new ArrayList<String> ();
        final Set<String> fileList = new HashSet<String> ();
        searchPathList = getSearchPathList (searchPath);
        Iterator<String> pathItr = searchPathList.iterator ();
        // this will only iterate through the searchpath list, so if something is not given in the searchpath list
        // or if searchpath is initially not setup then the user will not be able to get the info for files not in
        // searchpath
        // in this case we should atlest create parse tree node for the current file, if nothing is there.
        while (pathItr.hasNext ()) {
          String temp = pathItr.next ();
          IPath path = new Path (temp).makeAbsolute ();
          try {
            new FileTraversal () {
              public void onFile (final File f)
              {
                if (f.getName ().endsWith (AQL_EXT)) {
                  String path = f.getAbsolutePath ();
                  if (ProjectPreferencesUtil.isAQLInSearchPath (currentProject, path)) {
                    fileList.add (f.getAbsolutePath ().toString ());
                  }
                }
              }
            }.traverse (new File (path.toOSString ()));
          }
          catch (IOException ex) {
            logger.logError (ex.getMessage ());
          }
          catch (NullPointerException ex) {
            logger.logError (ex.getMessage ());
          }
        }
        Iterator<String> fileListItr = fileList.iterator ();
        while (fileListItr.hasNext ()) {
          String aqlFilePath = fileListItr.next ();
          AQLParser parser;
          try {
            parser = new AQLParser (FileUtils.fileToStr (new File (aqlFilePath),
              getProject (aqlFilePath).getDefaultCharset ()), aqlFilePath);
            isModularProject = ProjectUtils.isModularProject (getProject (aqlFilePath));
            parser.setBackwardCompatibilityMode (!isModularProject);
            parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser
            StatementList statementList = parser.parse ();
            String prjName = getProjectName (aqlFilePath);
            if (model == null) {
              aqlLibrary = Activator.getLibrary ();
              model = (AQLModel) aqlLibrary.getAQLModel ();
            }
            model.create (aqlFilePath, prjName, statementList);
            // Need to call editor.update method..
            AQLParseErrorHandler reporter = new AQLParseErrorHandler ();
            LinkedList<ParseException> parseException = statementList.getParseErrors ();
            Iterator<ParseException> expItr = parseException.iterator ();
            IPath path = new Path (aqlFilePath);
            IFile file = FileBuffers.getWorkspaceFileAtLocation (path);
            reporter.deleteMarkers (file);
            while (expItr.hasNext ()) {
              try {
                ParseException pe1 = expItr.next ();
                reporter.handleError (pe1, IMarker.SEVERITY_ERROR, file);
              }
              catch (Exception e) {}
            }
          }
          catch (IOException e) {}
          catch (CoreException ex) {
            logger.logError (ex.getMessage ());
          }
        }
        Activator.getLibrary ().addParsedPath (searchPath.hashCode ());
      }
    }
    else {
      // If its a modular project... add hashcode of searchpath to the locationsParsed
      if ((Activator.getModularLibrary ().getParsedPath () != null)
        && (Activator.getModularLibrary ().getParsedPath ().contains (searchPath.hashCode ()))) {
        // do nothing, library already exist for files for this searchpath
      }
      else {
        searchPathList = new ArrayList<String> ();
        // fileList = new ArrayList<String>();
        final Set<String> fileList = new HashSet<String> ();
        searchPathList = getSearchPathList (searchPath);
        Iterator<String> pathItr = searchPathList.iterator ();
        // this will only iterate through the searchpath list, so if something is not given in the searchpath list
        // or if searchpath is initially not setup then the user will not be able to get the info for files not in
        // searchpath
        // in this case we should atlest create parse tree node for the current file, if nothing is there.
        while (pathItr.hasNext ()) {
          String temp = pathItr.next ();
          IPath path = new Path (temp).makeAbsolute ();
          try {
            new FileTraversal () {
              public void onFile (final File f)
              {
                if (f.getName ().endsWith (AQL_EXT)) {
                  String path = f.getAbsolutePath ();
                  if (ProjectPreferencesUtil.isAQLInSearchPath (currentProject, path)) {
                    fileList.add (f.getAbsolutePath ().toString ());
                  }
                }
              }
            }.traverse (new File (path.toOSString ()));
          }
          catch (IOException ex) {
            logger.logError (ex.getMessage ());
          }
          catch (NullPointerException ex) {
            logger.logError (ex.getMessage ());
            //LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logInfo(Messages.AQLEditor_NO_BIGINSIGHTS_NATURE); //$NON-NLS-1$
          }
        }
        Iterator<String> fileListItr = fileList.iterator ();
        while (fileListItr.hasNext ()) {
          String aqlFilePath = fileListItr.next ();
          AQLParser parser;
          try {
            parser = new AQLParser (FileUtils.fileToStr (new File (aqlFilePath),
              getProject (aqlFilePath).getDefaultCharset ()), aqlFilePath);
            isModularProject = ProjectUtils.isModularProject (getProject (aqlFilePath));
            parser.setBackwardCompatibilityMode (!isModularProject);
            parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser
            StatementList statementList = parser.parse ();
            String prjName = getProjectName (aqlFilePath);
            String moduleName = getModuleName (aqlFilePath);
            if (modularModel == null) {
              aqlLibrary = Activator.getModularLibrary ();
              modularModel = (ModularAQLModel) aqlLibrary.getAQLModel ();
            }
            modularModel.create (aqlFilePath, prjName, moduleName, statementList);
            // Need to call editor.update method..
            AQLParseErrorHandler reporter = new AQLParseErrorHandler ();
            LinkedList<ParseException> parseException = statementList.getParseErrors ();
            Iterator<ParseException> expItr = parseException.iterator ();
            IPath path = new Path (aqlFilePath);
            IFile file = FileBuffers.getWorkspaceFileAtLocation (path);
            reporter.deleteMarkers (file);
            while (expItr.hasNext ()) {
              try {
                ParseException pe1 = expItr.next ();
                reporter.handleError (pe1, IMarker.SEVERITY_ERROR, file);
              }
              catch (Exception e) {}
            }

          }
          catch (IOException e) {}
          catch (CoreException ex) {
            logger.logError (ex.getMessage ());
          }
        }
        Activator.getModularLibrary ().addParsedPath (searchPath.hashCode ());
      }
    }
    // add the hashcode to parsedpath so next time it wont parse the searchpath when another file is openened
    // editor.aqlLibrary.addParsedPath(searchPath.hashCode());
    // Activator.getLibrary().addParsedPath(searchPath.hashCode());
    Display.getDefault ().asyncExec (new Runnable () {
      public void run ()
      {
        try {
          // Needs to open closed files...
          for (String fileToOpen : openedFiles) {
            openFile (fileToOpen, 0);
          }
        }
        catch (Exception ex) {
          logger.logError (ex.getMessage ());
        }
      }
    });
  }
	
	protected AQLEditor getEditor() {
    IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    if (editor instanceof AQLEditor)
      return (AQLEditor) editor;
    else
      return null;
  }
	
	private void openFile(String fullpath, int offset) {
	  IPath path = new Path(fullpath);
	  final IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
	  if(file == null)  {
	    final IFileStore fs = FileBuffers.getFileStoreAtLocation (path); //specified file path might not be part of workspace
	    if (fs != null) {
	      try {
	        if (fs.getName ().endsWith (Constants.AQL_FILE_EXTENSION)) {
	          newEditor = (AQLEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileStoreEditorInput(fs), EDITOR_ID);
	          s = (StyledText) newEditor.getAdapter(Control.class);
	        } else if (fs.getName ().endsWith (Constants.DICTIONARY_FILE_EXTENSION)) {
	          newDictEditor = (DictionaryEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileStoreEditorInput(fs), DICT_EDITOR_ID);
	          s = (StyledText) newEditor.getAdapter(Control.class);
	        } else {
	          IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(fs.getName());
	          TextEditor te = (TextEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(new FileStoreEditorInput(fs), desc.getId());
	          s = (StyledText) te.getAdapter(Control.class);
	        }
	      } catch (PartInitException e) {
	        logger.logWarning ("Unable to open file - "+fs.getName ()); //$NON-NLS-1$ //not externalising. message meant for dev.
	      }
	    }
	  } else {
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
	      logger.logWarning ("Unable to open file - "+file.getName ()); //$NON-NLS-1$ //not externalising. message meant for dev.
	    }
	  }
	}

	private void parseSearchPath(IResource res)
	{
		IResource resource = res;
		String searchPath = ""; //$NON-NLS-1$
    String refSearchPath = "";  //$NON-NLS-1$
    final IProject currProject = res.getProject ();
    currentProject = currProject;
		isModularProject = ProjectUtils.isModularProject(res.getProject ());
		try {
		  if(!isModularProject){
        if(resource.getProject().hasNature(com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID))
        {
          SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties(resource.getProject().getName());
          searchPath = ProjectPreferencesUtil.getAbsolutePath(properties.getSearchPath());
        }
      }else{
        if(resource.getProject().hasNature(com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID))
        {
          String moduleSearchPath = ProjectUtils.getConfiguredModuleSrcPath (currProject);
          if (moduleSearchPath != null) {
            IResource srcDir = ResourcesPlugin.getWorkspace().getRoot().findMember (moduleSearchPath);
            if (srcDir != null) {
              searchPath = srcDir.getLocation ().toString ();
              refSearchPath = getSearchPathForRefProjects(resource.getProject());
              searchPath = searchPath + SEMI_COLON + refSearchPath; 
            }
          }  
        }
      }
      final Set<String> fileList = new HashSet<String>();
      searchPathList = getSearchPathList(searchPath);
      Iterator<String> searchPathItr = searchPathList.iterator();
      //this will only iterate through the searchpath list, so if something is not given in the searchpath list
      //or if searchpath is initially not setup then the user will not be able to get the info for files not in searchpath
      //in this case we should atleast create parse tree node for the current file, if nothing is there.
      while(searchPathItr.hasNext())
      {
        String temp =searchPathItr.next();
        IPath path1 = new Path(temp).makeAbsolute(); 
        try {
          new FileTraversal() {
          public void onFile( final File f ) {
            if(!isModularProject){
              if(f.getName().endsWith(AQL_EXT))
              {
                String path = f.getAbsolutePath();
                if(ProjectPreferencesUtil.isAQLInSearchPath(currentProject, path)){
                  fileList.add(f.getAbsolutePath().toString());
                }
              } 
            }else{
              IPath resFilePath = new Path(f.getAbsolutePath ().toString ());
              final IFile resFile = FileBuffers.getWorkspaceFileAtLocation(resFilePath);
              if((f.getName().endsWith(AQL_EXT))&&(ProjectUtils.isValidModule (currProject, resFile.getParent ().getName ())))
              {
                fileList.add(f.getAbsolutePath().toString());
              }
            }
          }
          }.traverse(new File(path1.toOSString()));
        } catch (IOException ex) {
          logger.logError (ex.getMessage ());
        }
      }
      Iterator<String> fileListItr = fileList.iterator();
      while(fileListItr.hasNext())
      {               
        //AQLParser parser = new AQLParser(new File("C:/systemT/Workspace_NovemberRelease/AQLEditorImprovementTest/AQLs/personphone.aql"));
        String aqlFilePath1 = fileListItr.next();
        parseFile(aqlFilePath1);                
      }
  	}
		catch (NullPointerException ex) {
		  logger.logError (ex.getMessage ());
		}
		catch (Exception ex) {
		  logger.logError (ex.getMessage ());
		}
		//Added to update the editor.. 
		Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        try { 
          editor = getEditor ();
          if (editor != null)
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
    String refSearchPath = ""; //$NON-NLS-1$
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
	
  private void parseFile (String aqlFilePath)
  {
    IProject project = ProjectUtils.getProjectFromFilePath (aqlFilePath);
    boolean isInSearchPath = ProjectPreferencesUtil.isAQLInSearchPath (project, aqlFilePath);

    if (!isInSearchPath) return;

    try {
      AQLParser parser = new AQLParser (FileUtils.fileToStr (new File (aqlFilePath),
        ProjectUtils.getProjectFromFilePath (aqlFilePath).getDefaultCharset ()), aqlFilePath);
      boolean isModularProject = ProjectUtils.isModularProject (ProjectUtils.getProjectForEditor (aqlFilePath));
      parser.setBackwardCompatibilityMode (!isModularProject);
      parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser
      StatementList statementList = parser.parse ();
      if (!isModularProject) {
        String prjName = getProjectName (aqlFilePath);
        model.create (aqlFilePath, prjName, statementList);
      }
      else {
        String prjName = getProjectName (aqlFilePath);
        String moduleName = getModuleName (aqlFilePath);
        modularModel.create (aqlFilePath, prjName, moduleName, statementList);
      }
      // handle errors while refactoring
      AQLParseErrorHandler reporter = new AQLParseErrorHandler ();
      LinkedList<ParseException> parseException = statementList.getParseErrors ();
      Iterator<ParseException> itr = parseException.iterator ();
      IPath path = new Path (aqlFilePath);
      IFile file = FileBuffers.getWorkspaceFileAtLocation (path);
      reporter.deleteMarkers (file);

      while (itr.hasNext ()) {
        try {
          ParseException pe1 = itr.next ();
          reporter.handleError (pe1, IMarker.SEVERITY_ERROR, file);
        }
        catch (NoSuchElementException ex) {
          logger.logError (ex.getMessage ());
        }
      }
    }
    catch (IOException ex) {
      logger.logError (ex.getMessage ());
    }
    catch (CoreException ex) {
      logger.logError (ex.getMessage ());
    }
  }
	
	public void closeEditors(IEditorReference[] editorRefs){
	   //PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors (true);
	   PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditors (editorRefs, true);
	}
	
	/**
	 * Gives a Map of of all open aql editors and dictionary editors.
	 * @return Returned map's entries will have the format : key - Editor Reference, value - location of opened file
	 * While filelocation could be retrieved from EditorReference instances, storing them separately in key value pairs
	 * as the keys and values here will be consumed separately anyway and hence avoiding the overhead of a retrieving and checking
	 * file paths again later.
	 */
	public Map<IEditorReference,String> getAllOpenEditors(){
	  IWorkbench workbench = PlatformUI.getWorkbench();
	  IWorkbenchPage activePage = null;
	  IEditorReference[] allOpenEditors = null;
	  if(workbench != null){
	    activePage = workbench.getActiveWorkbenchWindow().getActivePage();
	  }
	  if(activePage != null){
	    allOpenEditors = activePage.getEditorReferences ();
	  }
	  Map<IEditorReference,String> openEditors = new LinkedHashMap<IEditorReference,String> ();
	  for(IEditorReference openEditorRef : allOpenEditors){
	    IEditorPart editor = openEditorRef.getEditor (false);
	    if (editor != null & (editor instanceof AQLEditor || editor instanceof DictionaryEditor))
	      try {
          IEditorInput genericInput = openEditorRef.getEditorInput (); //try/catch block is for this line
          if (genericInput instanceof FileStoreEditorInput) {
            //can reach this code if aql editor has opened a file outside workspace.
            FileStoreEditorInput fInput = (FileStoreEditorInput) genericInput;
            String path = fInput.getURI ().getPath ();
            if (path != null) {
              IPath aqlFilePath = new Path (path);
              openEditors.put (openEditorRef,aqlFilePath.toOSString ());
            }
          } else if (genericInput instanceof IFileEditorInput){
            //editorInput would be an instance of IFileEditorInput if the file opened by the editor is in workspace
            IFileEditorInput input = (IFileEditorInput)genericInput ;
            IPath aqlFilePath = input.getFile().getLocation();
            if (aqlFilePath != null) {
              String aqlLoc = aqlFilePath.toOSString ();
              openEditors.put (openEditorRef,aqlLoc);
            }
          }
        }
        catch (PartInitException e) {
          logger.logWarning (e.getMessage ()); //Log entry meant for dev.
        }
    }
	  return openEditors;
	}
	
	public String getModuleName(String aqlFilePath) 
  {
    IPath path = new Path(aqlFilePath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
    if(file == null) return null;
    else
    {
      return file.getParent ().getLocation ().toOSString ();
    }
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
	
	private ArrayList<String> getSearchPathList(String searchPath) {
		String[] tokens = searchPath.split(";");  //$NON-NLS-1$
		return new ArrayList<String>(Arrays.asList(tokens));
	}
	
	private void updateCommentInfo(IResource resourece) {
    if(isModularProject){
      modularModel.updateModuleComment (resourece.getProject ().getLocation ().toOSString (), resourece.getParent ().getLocation ().toOSString ());
    }
  }

	
}

class FileTraversal {
	public final void traverse(final File f) throws IOException {
//		we dont need recursive traversal at this point, but we may need it
      if (f.isDirectory()) {
      	//System.out.println("Dir: " + f);
              onDirectory(f);
              final File[] childs = f.listFiles();
              for( File child : childs ) {
                      traverse(child);
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
