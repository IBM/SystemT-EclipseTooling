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
package com.ibm.biginsights.textanalytics.aql.editor.assist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.algebra.datamodel.Pair;
import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.api.tam.ModuleMetadataFactory;
import com.ibm.avatar.aql.tam.ModuleUtils;

import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLPartitionScanner;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.AQLModule;
import com.ibm.biginsights.textanalytics.aql.library.AQLModuleLibrary;
import com.ibm.biginsights.textanalytics.aql.library.AQLModuleProject;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 *  Babbar
 *  Kalakuntla
 */

public class AQLAssistProcessor extends AQLTemplateAssistProcessor implements IContentAssistProcessor
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

  final String[] RETAIN_TYPES = { "right", "left", "both" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  final String[] EXTRACT_TYPES = { "dictionary", "regex", "dictionaries" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  final String[] CONSTRUCTS = {
    "create view", "create dictionary", "create table", "create function", "select", "detag", "output view", "include" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
  final String[] CREATE_TYPES = { "view", "dictionary", "table", "function", "external view" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  final String[] OUTPUT_STMT = { "output view" }; //$NON-NLS-1$
  final String[] OUTPUT_TYPES = { "view" }; //$NON-NLS-1$
  final String[] REGEX_TYPES = { "//" }; //$NON-NLS-1$
  final String[] IMPORT_TYPES = { "view", "dictionary", "function", "table", "module" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  final String[] EXPORT_TYPES = { "view", "dictionary", "function", "table" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  private String currentFileLocation;
  ArrayList<String> fileList, fileListRelative, searchPathList, moduleFileList;
  Set<String> createdViews, fileSet, fileSetRelative, moduleFileSet;
  IAQLLibrary aqlLibrary;
  IRegion region;
  IRegion prevRegion;
  IDocument doc;
  IRegion prev2Region;
  private boolean enableTemplate = false;

  final String EMPTY_STRING = ""; //$NON-NLS-1$ 
  final String SEMICOLON_STRING = ";"; //$NON-NLS-1$
  final String NAME_STRING = "name"; //$NON-NLS-1$
  final String VIEW_STRING = "view"; //$NON-NLS-1$
  final String FUNC_STRING = "function"; //$NON-NLS-1$
  final String DICT_STRING = "dictionary"; //$NON-NLS-1$
  final String TABLE_STRING = "table"; //$NON-NLS-1$
  final String OUTPUT_STRING = "output";//$NON-NLS-1$
  final String IMPORT_STRING = "import"; //$NON-NLS-1$
  final String MODULE_STRING = "module"; //$NON-NLS-1$
  final String EXPORT_STRING = "export";//$NON-NLS-1$
  final String FROM_STRING = "from"; //$NON-NLS-1$
  final String REGEX_STRING = "regex"; //$NON-NLS-1$

  public ICompletionProposal[] computeCompletionProposals (ITextViewer viewer, int offset)
  {
    ICompletionProposal[] prop = null;
    try {
      super.initializeContext (AQLContextType.CONTEXT_TYPE);
      // System.out.println("computing proposals");
      fileSet = new HashSet<String> ();
      fileSetRelative = new HashSet<String> ();
      fileList = new ArrayList<String> ();
      fileListRelative = new ArrayList<String> ();
      moduleFileSet = new HashSet<String> ();
      moduleFileList = new ArrayList<String> ();
      IEditorInput genericInput = PlatformUI.getWorkbench ().getActiveWorkbenchWindow ().getActivePage ().getActiveEditor ().getEditorInput ();
      if (!(genericInput instanceof IFileEditorInput)) { return null; }
      IFileEditorInput input = (IFileEditorInput) genericInput;
      IFile currentFile = input.getFile ();
      currentFileLocation = currentFile.getLocation ().toOSString ();

      // Check if the project is modular type of non-modular type and then load aql library accordingly..
      IProject currProject = input.getFile ().getProject ();
      // determine if the current file falls in project search path or not
      boolean isToProvideContentAssiste = false;
      isToProvideContentAssiste = ProjectPreferencesUtil.isAQLInSearchPath (currProject, currentFileLocation);
      if (!isToProvideContentAssiste) { return null; }

      boolean isModularProject = ProjectUtils.isModularProject (currProject);
      if (!isModularProject) {
        aqlLibrary = Activator.getLibrary ();
      }
      else {
        aqlLibrary = Activator.getModularLibrary ();
      }

      String searchPath = ""; //$NON-NLS-1$ //Use this variable only when the project involved is non-modular
      try {
        if (input.getFile ().getProject ().hasNature (com.ibm.biginsights.textanalytics.nature.Activator.NATURE_ID)) {
          SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties (input.getFile ().getProject ().getName ());
          searchPath = ProjectPreferencesUtil.getAbsolutePath (properties.getSearchPath ());
        }
        else {
          // if no BI nature then we add the current folder to the searchpath
          searchPath = input.getFile ().getParent ().getLocation ().toString ();
        }
      }
      catch (CoreException e1) {
        e1.printStackTrace ();
      }
      // SystemTProperties properties = ProjectPreferencesUtil.getSystemTProperties(currPorject);
      searchPathList = new ArrayList<String> ();

      // Check if the project is modular type or non-modular type and then calculate search path list accordingly...
      if (!isModularProject) {
        searchPathList = getSearchPathList (searchPath);
      }
      else {
        searchPathList = getModularProjectSearchPathList (currProject);
        // get the module for AQL file..
        IFolder moduleFolder = ProjectUtils.getModule4AqlFile (currentFile);
        String modulePath = moduleFolder.getLocation ().toOSString ();
        ArrayList<String> moduleFilePathList = getModuleSearchPathList (currProject, modulePath);
        if (moduleFilePathList != null) {
          moduleFileSet = getAllCurrModuleFiles (moduleFilePathList);
        }
        if (moduleFileSet != null) {
          moduleFileList.addAll (moduleFileSet);
        }
      }
      fileSet = getAllAQLFiles (searchPathList);
      fileSetRelative = getAllAQLFilesRelative (searchPathList);
      fileListRelative.addAll (fileSetRelative);
      if (fileSet != null) {
        fileList.addAll (fileSet);
      }
      Collections.swap (fileList, 0, fileList.indexOf (currentFileLocation));

      // Make a list of files from required projects and in current project, but not current module.
      // This is required when preparing ca proposals for import view/dictionary/table/function,
      // where we need to avoid looking for exported artifacts in files from current module.
      Set<String> filePathsFromOtherModules = new HashSet<String> ();
      filePathsFromOtherModules.addAll (fileList);
      filePathsFromOtherModules.removeAll (moduleFileList); // removing files belonging to current module.

      // String text = getSource(viewer).substring(0, offset);
      // String lastWord = getLastWord(text);
      String lastWord = EMPTY_STRING;
      String currWord = EMPTY_STRING;
      String prev2LastWord = EMPTY_STRING;
      doc = viewer.getDocument ();
      region = getCurrentRegion (doc, offset);

      try {
        ITypedRegion tRegion = doc.getPartition (offset);
        if (region != null) {
          // if (region != null && (tRegion.getType() != AQLPartitionScanner.AQL_COMMENT)) {
          try {
            if (region.getLength () > -1) {
              try {
                if ((tRegion.getType () == AQLPartitionScanner.AQL_COMMENT)) {
                  currWord = EMPTY_STRING;
                }
                else {
                  currWord = doc.get (region.getOffset (), region.getLength ());
                }
                // find last word for this curr word
                if (region.getOffset () > 0) {
                  prevRegion = getLastRegion (doc, region.getOffset ());
                  if (prevRegion != null) {
                    lastWord = doc.get (prevRegion.getOffset (), prevRegion.getLength ());

                    if (prevRegion.getOffset () > 0) {
                      prev2Region = getLastRegion (doc, prevRegion.getOffset ());
                      if (prev2Region != null) {
                        prev2LastWord = doc.get (prev2Region.getOffset (), prev2Region.getLength ());
                      }
                    }
                    else {
                      prev2LastWord = EMPTY_STRING;
                    }
                  }
                }
                else {
                  lastWord = EMPTY_STRING;
                }
              }
              catch (Exception exp) {
                LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
                  Constants.UNABLE_TO_PROCESS_THE_CONTENT_ASSISTANCE_INFO, exp);
                exp.printStackTrace ();
              }
            }
          }
          catch (Exception exp) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
              Constants.UNABLE_TO_PROCESS_THE_CONTENT_ASSISTANCE_INFO, exp);
            exp.printStackTrace ();
          }
        }
      }
      catch (BadLocationException exp) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
          Constants.UNABLE_TO_PROCESS_THE_CONTENT_ASSISTANCE_INFO, exp);
        exp.printStackTrace ();
      }

      List<ICompletionProposal> list = new ArrayList<ICompletionProposal> ();
      // put conditions here based on the last word
      // System.out.println("word is:" +currWord+":");
      // System.out.println("prev is:" +lastWord+":");
      // System.out.println("pre2 is:" +prev2LastWord+":");
      if (lastWord.equals (SEMICOLON_STRING)
        || (lastWord.equals (EMPTY_STRING) && currWord.equals (EMPTY_STRING) && prev2LastWord.equals (EMPTY_STRING))) // DONE
      {
        enableTemplate = true;
        // super.initializeContext(AQLContextType.CONTEXT_TYPE);
        // for(int i=0;i<CONSTRUCTS.length;i++){
        // if(CONSTRUCTS[i].toString().toLowerCase().startsWith(currWord.toLowerCase())){
        // list.add(new CompletionProposal(
        // CONSTRUCTS[i].toString(),
        // offset - currWord.length(),
        // currWord.length(),
        // CONSTRUCTS[i].toString().length(),
        // null,
        // //HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
        // CONSTRUCTS[i].toString(),
        // null,null));
        // }
        // }
      }
      else if (lastWord.equals (VIEW_STRING) && (prev2LastWord.equals (OUTPUT_STRING))) {
        if (!isModularProject) {
          moduleFileList = fileList;
        }

        /**
         * Look for view & external view definitions and import view statements in current module's aql files. Add these
         * to content assist proposals.
         */
        List<AQLElement> elements = aqlLibrary.getViews (moduleFileList);
        List<AQLElement> ExtViewElements = aqlLibrary.getExternalViews (moduleFileList);
        if (ExtViewElements != null) {
          elements.addAll (ExtViewElements);
        }

        if (elements != null) {
          Iterator<AQLElement> viewListItr = elements.iterator ();
          while (viewListItr.hasNext ()) {
            AQLElement elmt = viewListItr.next (); // Adding to Content assist proposals list
            if (elmt.getName ().toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
              list.add (new CompletionProposal (elmt.getName (), region.getOffset (), currWord.length (),
                elmt.getName ().length (), null, elmt.getName () + " : " + elmt.getType ().toLowerCase (), //$NON-NLS-1$
                null, null));
            }
          }
        }

        if (isModularProject) { // imported views will be available only in modular aql projects.
          /**
           * The list below contains import view nodes from files in current module and for modules that have been
           * imported whole, the export view nodes from those modules. As they are required to be presented in qualified
           * form, or with alias if present, they need to processed separately from regular view elements.
           */
          List<AQLElement> importedViewElements = aqlLibrary.getImportedViews (moduleFileList);
          // Collecting names to be presented as proposals in their appropriate forms, in the following set.
          Set<String> importedViewNames = new HashSet<String> ();
          if (importedViewElements != null) {
            Iterator<AQLElement> viewListItr = importedViewElements.iterator ();
            while (viewListItr.hasNext ()) {
              AQLElement elmt = viewListItr.next ();
              if (elmt.getType ().equals (Constants.AQL_ELEMENT_TYPE_IMPORT_VIEW)) {
                if (!StringUtils.isEmpty (elmt.getAliasName ())) {
                  importedViewNames.add (elmt.getAliasName ());
                }
                else {
                  importedViewNames.add (elmt.getFromModuleName () + Constants.MODULE_ELEMENT_SEPARATOR
                    + elmt.getUnQualifiedName ());
                }
              }
              /**
               * Excluding EXPORT_VIEW type elements returned by getImportedViews(). The method collects these elements
               * from modules that have been imported via 'import module' statement, by looking in the modulelibrary
               * instance in AQLLibrary. But AQLLibrary has no way to distinguish if the module instance in the library
               * belongs to the module actually being imported. (The module being imported could instead be from an
               * included TAM file.)
               */
            }
          }
          /**
           * aqllibrary.getImportedViews() used above will get us all explicitly imported views (i.e. via import view
           * statement) and views from modules that were imported via 'import module' statement, as long as the module
           * has source code available. It is unable to provide us views from imported modules associated with TAMs. The
           * following section prepares a list of views from such modules. Also, the list of views from modules imported
           * via 'import module' statement can be incorrect. Hence calculating separately.
           */
          List<String> explicitlyImportedModules = new ArrayList<String> ();
          for (String filePath : moduleFileList) {
            explicitlyImportedModules.addAll (((AQLModuleLibrary) aqlLibrary).getImportModuleStmtModules (filePath));
          }
          List<String> tamDirPaths = getAllTamPaths (currProject);
          for (String tamDirPath : tamDirPaths) {
            // This will read from tam files, folders, zips and jars.
            ModuleMetadata[] mdInsts = ModuleMetadataFactory.readAllMetaData (new File (tamDirPath).toURI ().toString ());
            for (ModuleMetadata tmd : mdInsts) {
              if (explicitlyImportedModules.contains (tmd.getModuleName ())) {
                importedViewNames.addAll (Arrays.asList (tmd.getExportedViews ()));
              }
            }
          }
          // Scanning current and all referenced projects for the modules we need and creating a list of files in those modules.
          Set<String> importedModulesSrcFiles = new HashSet<String> ();
          List<IProject> candidateProjects = new ArrayList<IProject> ( Arrays.asList (currProject.getReferencedProjects ()));
          candidateProjects.add (currProject); // Including the current project also to the list.
          for (IProject reqProj : candidateProjects) {
            for (String moduleName : explicitlyImportedModules) {
              IFolder prjSrcFolder = ProjectUtils.getTextAnalyticsSrcFolder (reqProj);
              if (prjSrcFolder != null) {
                IResource moduleFolder = prjSrcFolder.findMember (moduleName);
                if (moduleFolder != null) {
                  importedModulesSrcFiles.addAll (getModuleSearchPathList (reqProj,
                    moduleFolder.getLocation ().toOSString ()));
                }
              }
            }
          }
          // Using filelist created above to view export view statement elements. These are views from imported modules
          // from other referenced projects.
          List<AQLElement> viewsFromImportedModules = aqlLibrary.getExportedViews (new ArrayList<String> (
              importedModulesSrcFiles));
          if (viewsFromImportedModules != null) {
            for (AQLElement elem : viewsFromImportedModules) {
              importedViewNames.add (elem.getModuleName () + Constants.MODULE_ELEMENT_SEPARATOR
                + elem.getUnQualifiedName ());
            }
          }
          for (String viewName : importedViewNames) {
            if (viewName.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
              list.add (new CompletionProposal (viewName, region.getOffset (), currWord.length (), viewName.length (),
                null,
                // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
                viewName + " : " + VIEW_STRING, //$NON-NLS-1$
                null, null));
            }
          }
        }

      }
      else if (lastWord.equals (MODULE_STRING) && (prev2LastWord.equals (IMPORT_STRING)) && isModularProject) {
        // Get all the module names and display here...
        String[] modules = ProjectUtils.getModules (currProject);
        List<String> moduleElements = new ArrayList<String> ();
        moduleElements = Arrays.asList (modules);

        List<String> allModules = new ArrayList<String> (moduleElements);
        IFolder moduleFolder = ProjectUtils.getModule4AqlFile (currentFile);
        if (moduleFolder != null) {
          allModules.remove (moduleFolder.getName ()); // Removing current module from ca proposals
        }
        // Get the modules from referenced projects..
        IProject[] refProjects = currProject.getReferencedProjects ();
        for (IProject refProj : refProjects) {
          String[] refModules = ProjectUtils.getModules (refProj);
          List<String> refModuleElements = new ArrayList<String> ();
          refModuleElements = Arrays.asList (refModules);
          allModules.addAll (refModuleElements);
        }
        // get the modules from pre-compiled tams..
        List<String> tamDirPaths = getAllTamPaths (currProject);
        for (String tamDirPath : tamDirPaths) {
          // Map<String,String> tams = getAllTAMFiles(tamDirPath);
          ModuleMetadata[] moduleMetaDataInsts = ModuleMetadataFactory.readAllMetaData (new File (tamDirPath).toURI ().toString ()); // This
                                                                                                                                     // will
                                                                                                                                     // read
                                                                                                                                     // from
                                                                                                                                     // tam
                                                                                                                                     // files,
                                                                                                                                     // folders,
                                                                                                                                     // zips
                                                                                                                                     // and
                                                                                                                                     // jars.
          for (ModuleMetadata tmd : moduleMetaDataInsts)
            allModules.add (tmd.getModuleName ());
        }
        Iterator<String> strItr = allModules.iterator ();
        while (strItr.hasNext ()) {
          String elmt = strItr.next ();
          if (elmt.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
            list.add (new CompletionProposal (elmt, region.getOffset (), currWord.length (), elmt.length (), null,
            // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
              elmt + " : " + MODULE_STRING, //$NON-NLS-1$
              null, null));
          }
        }
      }
      else if (lastWord.equals (VIEW_STRING) && (prev2LastWord.equals (IMPORT_STRING)) && isModularProject) {
        // Look for exported views from all applicable modules except current module
        List<AQLElement> elements = aqlLibrary.getExportedViews (new ArrayList<String> (filePathsFromOtherModules));

        // Need to get the views from imported tam files...
        List<String> tamViews = new ArrayList<String> ();
        List<String> tamDirPaths = getAllTamPaths (currProject);

        for (String tamDirPath : tamDirPaths) {
          ModuleMetadata[] mdInsts = ModuleMetadataFactory.readAllMetaData (new File (tamDirPath).toURI ().toString ());
          for (ModuleMetadata tmd : mdInsts) {
            tamViews.addAll (Arrays.asList (tmd.getExportedViews ()));
          }
        }
        if (elements != null) {
          Iterator<AQLElement> eleItr = elements.iterator ();
          while (eleItr.hasNext ()) {
            AQLElement elmt = eleItr.next ();
            if (elmt.getName ().toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
              list.add (new CompletionProposal (elmt.getUnQualifiedName () + " from module " + elmt.getModuleName (), //This is the actual text that will be inserted in editor on making a selection //$NON-NLS-1$
                region.getOffset (), currWord.length (), elmt.getUnQualifiedName ().length () + elmt.getModuleName ().length ()
                  + 13, // The offset (relative to initial position) cursor should be placed at after text has been
                        // inserted to file.
                null, elmt.getModuleName () + "." + elmt.getUnQualifiedName () + " : " + VIEW_STRING, //$NON-NLS-1$ //$NON-NLS-2$ //This is what will appear on the list
                null, null));
            }
          }
        }
        // Iterate through the tamViews lists and create the proposals and add them into list as above..
        for (String tamView : tamViews) {
          String moduleName = ModuleUtils.getModuleName (tamView);
          String viewName = ModuleUtils.getUnqualifiedElementName (tamView);
          if (viewName.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
            list.add (new CompletionProposal (viewName + " from module " + moduleName, //$NON-NLS-1$
              region.getOffset (), currWord.length (), viewName.length () + moduleName.length () + 13, null, tamView
                + " : " + VIEW_STRING, //$NON-NLS-1$
              null, null));
          }
        }
      }
      else if (lastWord.equals (FUNC_STRING) && (prev2LastWord.equals (IMPORT_STRING)) && isModularProject) {
        // Look for exported functions from all applicable modules except current module
        List<AQLElement> elements = aqlLibrary.getExportedFunctions (new ArrayList<String> (filePathsFromOtherModules));

        // Need to get the functions from imported tam files..
        List<String> tamFunctions = new ArrayList<String> ();
        List<String> tamDirPaths = getAllTamPaths (currProject);

        for (String tamDirPath : tamDirPaths) {
          ModuleMetadata[] mdInsts = ModuleMetadataFactory.readAllMetaData (new File (tamDirPath).toURI ().toString ()); // This
                                                                                                                         // will
                                                                                                                         // read
                                                                                                                         // from
                                                                                                                         // tam
                                                                                                                         // files,
                                                                                                                         // folders,
                                                                                                                         // zips
                                                                                                                         // and
                                                                                                                         // jars.
          for (ModuleMetadata tmd : mdInsts) {
            tamFunctions.addAll (Arrays.asList (tmd.getExportedFunctions ()));
          }
        }
        if (elements != null) {
          Iterator<AQLElement> eleItr = elements.iterator ();
          while (eleItr.hasNext ()) {
            AQLElement elmt = eleItr.next ();
            if (elmt.getName ().toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
              list.add (new CompletionProposal (elmt.getUnQualifiedName () + " from module " + elmt.getModuleName (), //This is the actual text that will be inserted in editor on making a selection //$NON-NLS-1$
                region.getOffset (), currWord.length (), elmt.getUnQualifiedName ().length () + elmt.getModuleName ().length ()
                  + 13, // The offset (relative to initial position) cursor should be placed at after text has been
                        // inserted to file.
                null, elmt.getModuleName () + "." + elmt.getUnQualifiedName () + " : " + FUNC_STRING, //$NON-NLS-1$ //$NON-NLS-2$ //This is what will appear on the list
                null, null));
            }
          }
        }
        // Iterate through the tamViews list and create the proposals and add them into list as above..
        for (String tamFunc : tamFunctions) {
          String moduleName = ModuleUtils.getModuleName (tamFunc);
          String functionName = ModuleUtils.getUnqualifiedElementName (tamFunc);
          if (functionName.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
            list.add (new CompletionProposal (
              functionName + " from module " + moduleName, //$NON-NLS-1$
              region.getOffset (), currWord.length (), functionName.length () + moduleName.length () + 13, null,
              tamFunc + " : " + FUNC_STRING, //$NON-NLS-1$
              null, null));
          }
        }
      }
      else if (lastWord.equals (TABLE_STRING) && (prev2LastWord.equals (IMPORT_STRING)) && isModularProject) {
        // Look for exported tables from all applicable modules except current module
        List<AQLElement> elements = aqlLibrary.getExportedTables (new ArrayList<String> (filePathsFromOtherModules));

        // Need to get the tables from imported tam files..
        List<String> tamTables = new ArrayList<String> ();
        List<String> tamDirPaths = getAllTamPaths (currProject);

        for (String tamDirPath : tamDirPaths) {
          ModuleMetadata[] mdInsts = ModuleMetadataFactory.readAllMetaData (new File (tamDirPath).toURI ().toString ()); // This
                                                                                                                         // will
                                                                                                                         // read
                                                                                                                         // from
                                                                                                                         // tam
                                                                                                                         // files,
                                                                                                                         // folders,
                                                                                                                         // zips
                                                                                                                         // and
                                                                                                                         // jars.
          for (ModuleMetadata tmd : mdInsts) {
            tamTables.addAll (Arrays.asList (tmd.getExportedTables ()));
          }
        }
        if (elements != null) {
          Iterator<AQLElement> iterator2 = elements.iterator ();
          while (iterator2.hasNext ()) {
            AQLElement elmt = iterator2.next ();
            if (elmt.getName ().toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
              list.add (new CompletionProposal (elmt.getUnQualifiedName () + " from module " + elmt.getModuleName (), //This is the actual text that will be inserted in editor on making a selection //$NON-NLS-1$
                region.getOffset (), currWord.length (), elmt.getUnQualifiedName ().length () + elmt.getModuleName ().length ()
                  + 13, // The offset (relative to initial position) cursor should be placed at after text has been
                        // inserted to file.
                null, elmt.getModuleName () + "." + elmt.getUnQualifiedName () + " : " + TABLE_STRING, //$NON-NLS-1$ //$NON-NLS-2$ //This is what will appear on the list
                null, null));
            }
          }
        }
        for (String tamTable : tamTables) {
          String moduleName = ModuleUtils.getModuleName (tamTable);
          String tableName = ModuleUtils.getUnqualifiedElementName (tamTable);
          if (tableName.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
            list.add (new CompletionProposal (tableName + " from module " + moduleName, //$NON-NLS-1$
              region.getOffset (), currWord.length (), tableName.length () + moduleName.length () + 13, null, tamTable
                + " : " + TABLE_STRING, //$NON-NLS-1$
              null, null));
          }
        }
      }
      else if (lastWord.equals (DICT_STRING) && (prev2LastWord.equals (IMPORT_STRING)) && isModularProject) {
        // Look for exported dictionaries from all applicable modules except current module
        List<AQLElement> elements = aqlLibrary.getExportedDictionaries (new ArrayList<String> (
          filePathsFromOtherModules));

        // Need to get the dictionaries from imported tam files..
        List<String> tamDicts = new ArrayList<String> ();
        List<String> tamDirPaths = getAllTamPaths (currProject);

        for (String tamDirPath : tamDirPaths) {
          ModuleMetadata[] mdInsts = ModuleMetadataFactory.readAllMetaData (new File (tamDirPath).toURI ().toString ()); // This
                                                                                                                         // will
                                                                                                                         // read
                                                                                                                         // from
                                                                                                                         // tam
                                                                                                                         // files,
                                                                                                                         // folders,
                                                                                                                         // zips
                                                                                                                         // and
                                                                                                                         // jars.
          for (ModuleMetadata tmd : mdInsts) {
            tamDicts.addAll (Arrays.asList (tmd.getExportedDictionaries ()));
          }
        }
        if (elements != null) {
          Iterator<AQLElement> eleItr = elements.iterator ();
          while (eleItr.hasNext ()) {
            AQLElement elmt = eleItr.next ();
            if (elmt.getName ().toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
              list.add (new CompletionProposal (elmt.getUnQualifiedName () + " from module " + elmt.getModuleName (), //This is the actual text that will be inserted in editor on making a selection //$NON-NLS-1$
                region.getOffset (), currWord.length (), elmt.getUnQualifiedName ().length () + elmt.getModuleName ().length ()
                  + 13, // The offset (relative to initial position) cursor should be placed at after text has been
                        // inserted to file.
                null, elmt.getModuleName () + "." + elmt.getUnQualifiedName () + " : " + DICT_STRING, //$NON-NLS-1$ //$NON-NLS-2$ //This is what will appear on the list
                null, null));
            }
          }
        }
        for (String tamDict : tamDicts) {
          String moduleName = ModuleUtils.getModuleName (tamDict);
          String dictionaryName = ModuleUtils.getUnqualifiedElementName (tamDict);
          if (dictionaryName.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
            list.add (new CompletionProposal (
              dictionaryName + " from module " + moduleName, //$NON-NLS-1$
              region.getOffset (), currWord.length (), dictionaryName.length () + moduleName.length () + 13, null,
              tamDict + " : " + DICT_STRING, //$NON-NLS-1$
              null, null));
          }
        }
      }
      else if (lastWord.equals (VIEW_STRING) && (prev2LastWord.equals (EXPORT_STRING)) && isModularProject) {
        // Calculate th emodule level fileList
        List<AQLElement> elements = aqlLibrary.getViews (moduleFileList);
        List<AQLElement> extViewElems = aqlLibrary.getExternalViews (moduleFileList);
        List<AQLElement> selectElems = aqlLibrary.getSelects (moduleFileList);
        if (extViewElems != null) elements.addAll (extViewElems);
        if (selectElems != null) elements.addAll (selectElems);

        if (!isModularProject) {
          elements = null;
        }
        if (elements != null) {
          Iterator<AQLElement> eleItr = elements.iterator ();
          while (eleItr.hasNext ()) {
            AQLElement elmt = eleItr.next ();
            if (elmt.getName ().toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
              list.add (new CompletionProposal (elmt.getName (), region.getOffset (), currWord.length (),
                elmt.getName ().length (), null,
                // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
                elmt.getName () + " : " + VIEW_STRING, //$NON-NLS-1$
                null, null));
            }
          }
        }
      }
      else if (lastWord.equals (FUNC_STRING) && (prev2LastWord.equals (EXPORT_STRING)) && isModularProject) {
        List<AQLElement> elements = aqlLibrary.getFunctions (moduleFileList);
        if (!isModularProject) {
          elements = null;
        }
        if (elements != null) {
          Iterator<AQLElement> iterator2 = elements.iterator ();
          while (iterator2.hasNext ()) {
            AQLElement elmt = iterator2.next ();
            String elementName = elmt.getUnQualifiedName (); // when AQLElement.getName() returns fully qualified name
                                                             // for functions, dictionaries and tables, unlike views.
                                                             // Hence using getUnQualifiedName()
            if (elementName.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
              list.add (new CompletionProposal (elementName, region.getOffset (), currWord.length (),
                elementName.length (), null,
                // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
                elementName + " : " + elmt.getType ().toLowerCase (), //$NON-NLS-1$
                null, null));
            }
          }
        }
      }
      else if (lastWord.equals (TABLE_STRING) && (prev2LastWord.equals (EXPORT_STRING)) && isModularProject) {
        List<AQLElement> elements = aqlLibrary.getTables (moduleFileList); // Should be curr file
        List<AQLElement> extTabElems = aqlLibrary.getExternalTables (moduleFileList);
        if (extTabElems != null) elements.addAll (extTabElems);

        if (!isModularProject) {
          elements = null;
        }
        if (elements != null) {
          Iterator<AQLElement> iterator2 = elements.iterator ();
          while (iterator2.hasNext ()) {
            AQLElement elmt = iterator2.next ();
            String elementName = elmt.getUnQualifiedName (); // when AQLElement.getName() returns fully qualified name
                                                             // for functions, dictionaries and tables, unlike views.
                                                             // Hence using getUnQualifiedName()
            if (elementName.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
              list.add (new CompletionProposal (elementName, region.getOffset (), currWord.length (),
                elementName.length (), null,
                // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
                elementName + " : " + elmt.getType ().toLowerCase (), //$NON-NLS-1$
                null, null));
            }
          }
        }
      }
      else if (lastWord.equals (DICT_STRING) && (prev2LastWord.equals (EXPORT_STRING)) && isModularProject) {
        List<AQLElement> elements = aqlLibrary.getDictionaries (moduleFileList);
        List<AQLElement> extDictElems = aqlLibrary.getExternalDictionaries (moduleFileList);
        if (extDictElems != null) elements.addAll (extDictElems);

        if (!isModularProject) {
          elements = null;
        }
        if (elements != null) {
          Iterator<AQLElement> eleItr = elements.iterator ();
          while (eleItr.hasNext ()) {
            AQLElement elmt = eleItr.next ();
            String elementName = elmt.getUnQualifiedName (); // when AQLElement.getName() returns fully qualified name
                                                             // for functions, dictionaries and tables, unlike views.
                                                             // Hence using getUnQualifiedName()
            if (elementName.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals (NAME_STRING)) {
              list.add (new CompletionProposal (elementName, region.getOffset (), currWord.length (),
                elementName.length (), null,
                // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
                elementName + " : " + elmt.getType ().toLowerCase (), //$NON-NLS-1$
                null, null));
            }
          }
        }
      }
      else if (lastWord.equals (FROM_STRING) || (isConstruct (prev2LastWord) && isComma (prevRegion, region))
        || (isConstruct (lastWord) && isComma (prevRegion, region))) {
        if (!isModularProject) {
          moduleFileList = fileList;
        }

        // Handle local and imported artifacts separately.
        List<AQLElement> localElements = new ArrayList<AQLElement> ();
        List<AQLElement> viewElms = aqlLibrary.getViews (moduleFileList);
        if (viewElms != null) {
          localElements.addAll (viewElms);
        }
        List<AQLElement> extViewElms = aqlLibrary.getExternalViews (moduleFileList);
        if (extViewElms != null) localElements.addAll (extViewElms);
        List<AQLElement> tabElms = aqlLibrary.getTables (moduleFileList);
        if (tabElms != null) localElements.addAll (tabElms);
        List<AQLElement> extTabElms = aqlLibrary.getExternalTables (moduleFileList);
        if (extTabElms != null) localElements.addAll (extTabElms);

        Map<String, String> importedArtifactNameToTypeMap = new HashMap<String, String> ();
        if (isModularProject) { //imported artifacts are available only in modular aql projects
          List<AQLElement> impViewElems = aqlLibrary.getImportedViews (moduleFileList);
          if (impViewElems != null) {
            for (AQLElement elem : impViewElems) {
              if (elem.getType ().equals (Constants.AQL_ELEMENT_TYPE_IMPORT_VIEW)) { // Excluding EXPORT_VIEW type
                if (!StringUtils.isEmpty (elem.getAliasName ())) {
                  importedArtifactNameToTypeMap.put (elem.getAliasName (), VIEW_STRING);
                }
                else {
                  importedArtifactNameToTypeMap.put (elem.getFromModuleName () + Constants.MODULE_ELEMENT_SEPARATOR
                    + elem.getUnQualifiedName (), VIEW_STRING);
                }
              }
            }
          }
          List<AQLElement> impTabElems = aqlLibrary.getImportedTables (moduleFileList);
          if (impTabElems != null) {
            for (AQLElement elem : impTabElems) {
              if (elem.getType ().equals (Constants.AQL_ELEMENT_TYPE_IMPORT_TABLE)) { // Excluding EXPORT_TABLE type
                if (!StringUtils.isEmpty (elem.getAliasName ())) {
                  importedArtifactNameToTypeMap.put (elem.getAliasName (), TABLE_STRING);
                }
                else {
                  importedArtifactNameToTypeMap.put (elem.getFromModuleName () + Constants.MODULE_ELEMENT_SEPARATOR
                    + elem.getUnQualifiedName (), TABLE_STRING);
                }
              }
            }
          }
          /**
           * aqllibrary.getImportedViews() used above will get us all explicitly imported views (i.e. via import view
           * statement) and views from modules that were imported via 'import module' statement, as long as the module
           * has source code available. It is unable to provide us views from imported modules associated with TAMs. The
           * following section prepares a list of views from such modules. Also, the list of views from modules imported
           * via 'import module' statement can be incorrect. Hence calculating separately.
           */
          List<String> explicitlyImportedModules = new ArrayList<String> (); // List of module names that were imported
          // via 'import module' statements.
          for (String filePath : moduleFileList) {
            explicitlyImportedModules.addAll (((AQLModuleLibrary) aqlLibrary).getImportModuleStmtModules (filePath));
          }
          List<String> tamDirPaths = getAllTamPaths (currProject);
          for (String tamDirPath : tamDirPaths) {
            // This will read from tam files, folders, zips and jars.
            ModuleMetadata[] mdInsts = ModuleMetadataFactory.readAllMetaData (new File (tamDirPath).toURI ().toString ());
            for (ModuleMetadata tmd : mdInsts) {
              if (explicitlyImportedModules.contains (tmd.getModuleName ())) {
                for (String view : tmd.getExportedViews ()) {
                  importedArtifactNameToTypeMap.put (view, VIEW_STRING);
                }
                for (String table : tmd.getExportedTables ()) {
                  importedArtifactNameToTypeMap.put (table, TABLE_STRING);
                }
              }
            }
          }
          // Scanning referenced projects for the modules we need and creating a list of files in those modules.
          Set<String> filesFromModulesInReferencedProjects = new HashSet<String> ();
          for (IProject reqProj : currProject.getReferencedProjects ()) {
            for (String moduleName : explicitlyImportedModules) {
              IFolder prjSrcFolder = ProjectUtils.getTextAnalyticsSrcFolder (reqProj);
              if (prjSrcFolder != null) {
                IResource moduleFolder = prjSrcFolder.findMember (moduleName);
                if (moduleFolder != null) {
                  filesFromModulesInReferencedProjects.addAll (getModuleSearchPathList (reqProj,
                    moduleFolder.getLocation ().toOSString ()));
                }
              }
            }
          }
          // Using filelist created above to view export view statement elements. These are views from imported modules
          // from other referenced projects.
          List<AQLElement> viewsFromImportedModules = aqlLibrary.getExportedViews (new ArrayList<String> (
            filesFromModulesInReferencedProjects));
          if (viewsFromImportedModules != null) {
            for (AQLElement elem : viewsFromImportedModules) {
              importedArtifactNameToTypeMap.put (
                elem.getModuleName () + Constants.MODULE_ELEMENT_SEPARATOR + elem.getUnQualifiedName (), VIEW_STRING);
            }
          }
          // Using filelist created above to view export view statement elements. These are views from imported modules
          // from other referenced projects.
          List<AQLElement> tablesFromImportedModules = aqlLibrary.getExportedTables (new ArrayList<String> (
            filesFromModulesInReferencedProjects));
          if (tablesFromImportedModules != null) {
            for (AQLElement elem : tablesFromImportedModules) {
              importedArtifactNameToTypeMap.put (
                elem.getModuleName () + Constants.MODULE_ELEMENT_SEPARATOR + elem.getUnQualifiedName (), TABLE_STRING);
            }
          }
        }
        
        //Add all collected names to CA proposals list
        for (AQLElement elmt : localElements) {
          if (elmt.getName ().toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals ("arguments")) { //$NON-NLS-1$
            list.add (new CompletionProposal (elmt.getName (), region.getOffset (), currWord.length (),
              elmt.getName ().length (), null,
              // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
              elmt.getName () + " : " + elmt.getType ().toLowerCase (), //$NON-NLS-1$
              null, null));
          }
        }

        for (Map.Entry<String, String> importedArtifactNameTypePair : importedArtifactNameToTypeMap.entrySet ()) {
          String artifactName = importedArtifactNameTypePair.getKey ();
          if (artifactName.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals ("arguments")) { //$NON-NLS-1$
            list.add (new CompletionProposal (artifactName, region.getOffset (), currWord.length (),
              artifactName.length (), null, artifactName + " : " + importedArtifactNameTypePair.getValue (), //$NON-NLS-1$
              null, null));
          }
        }
      }

      else if (lastWord.equals ("include") && !isModularProject) //$NON-NLS-1$
      {
        aqlLibrary = Activator.getLibrary ();
        // need not show the current file to be included, anyways it will give cyclic dependency error
        fileListRelative.remove (FileUtils.createValidatedFile (currentFileLocation).getName ().toString ());
        Iterator<String> eleItr = fileListRelative.iterator ();
        while (eleItr.hasNext ()) {
          String elmt = eleItr.next ();
          if (elmt.toLowerCase ().startsWith (currWord.toLowerCase ()) || currWord.equals ("file")) { //$NON-NLS-1$
            list.add (new CompletionProposal (replaceString (elmt, region.getOffset ()), region.getOffset (),
              currWord.length (), elmt.length (), null,
              // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
              elmt, null, null));
          }

        }
      }
      else if (lastWord.equals (IMPORT_STRING)) // DONE
      {
        // list = populateProposals(REGEX_TYPES);
        // enableTemplate = true;
        // super.initializeContext(AQLContextType.STATEMENT_CONTEXT);

        for (int i = 0; i < IMPORT_TYPES.length; i++) {
          if (IMPORT_TYPES[i].toString ().toLowerCase ().startsWith (currWord.toLowerCase ())) {
            list.add (new CompletionProposal (IMPORT_TYPES[i].toString (), offset - currWord.length (),
              currWord.length (), IMPORT_TYPES[i].toString ().length (), null,
              // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
              IMPORT_TYPES[i].toString (), null, null));
          }
        }
      }
      else if (lastWord.equals (EXPORT_STRING)) // DONE
      {
        // list = populateProposals(REGEX_TYPES);
        // enableTemplate = true;
        // super.initializeContext(AQLContextType.STATEMENT_CONTEXT);

        for (int i = 0; i < EXPORT_TYPES.length; i++) {
          if (EXPORT_TYPES[i].toString ().toLowerCase ().startsWith (currWord.toLowerCase ())) {
            list.add (new CompletionProposal (EXPORT_TYPES[i].toString (), offset - currWord.length (),
              currWord.length (), EXPORT_TYPES[i].toString ().length (), null,
              // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
              EXPORT_TYPES[i].toString (), null, null));
          }
        }
      }

      // ************************Commenting this out, will be required for enhancing content assisatance

      // else if(lastWord.equals("create")) //DONE
      // {
      // for(int i=0;i<CREATE_TYPES.length;i++){
      // if(CREATE_TYPES[i].toString().toLowerCase().startsWith(currWord.toLowerCase())){
      // list.add(new CompletionProposal(
      // CREATE_TYPES[i].toString(),
      // offset - currWord.length(),
      // currWord.length(),
      // CREATE_TYPES[i].toString().length(),
      // null,
      // //HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
      // CREATE_TYPES[i].toString(),
      // null,null));
      // }
      // }
      // }
      //
      // else if(lastWord.equals("output")) //DONE
      // {
      // for(int i=0;i<OUTPUT_TYPES.length;i++){
      // if(OUTPUT_TYPES[i].toString().toLowerCase().startsWith(currWord.toLowerCase())){
      // list.add(new CompletionProposal(
      // OUTPUT_TYPES[i].toString(),
      // offset - currWord.length(),
      // currWord.length(),
      // OUTPUT_TYPES[i].toString().length(),
      // null,
      // //HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
      // OUTPUT_TYPES[i].toString(),
      // null,null));
      // }
      // }
      // }
      //
      else if (lastWord.equals (REGEX_STRING)) // DONE
      {
        // list = populateProposals(REGEX_TYPES);
        enableTemplate = true;
        super.initializeContext (AQLContextType.STATEMENT_CONTEXT);

        for (int i = 0; i < REGEX_TYPES.length; i++) {
          if (REGEX_TYPES[i].toString ().toLowerCase ().startsWith (currWord.toLowerCase ())) {
            list.add (new CompletionProposal (REGEX_TYPES[i].toString (), offset - currWord.length (),
              currWord.length (), REGEX_TYPES[i].toString ().length (), null,
              // HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
              REGEX_TYPES[i].toString (), null, null));
          }
        }
      }

      // We may use this API later on.. for more content assistance..
      //
      // else if(lastWord.equals("retain")) //DONE
      // {
      // for(int i=0;i<RETAIN_TYPES.length;i++){
      // if(RETAIN_TYPES[i].toString().toLowerCase().startsWith(currWord.toLowerCase())){
      // list.add(new CompletionProposal(
      // RETAIN_TYPES[i].toString(),
      // offset - currWord.length(),
      // currWord.length(),
      // RETAIN_TYPES[i].toString().length(),
      // null,
      // //HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
      // RETAIN_TYPES[i].toString(),
      // null,null));
      // }
      // }
      // }
      //
      // else if(lastWord.equals("extract")) //DONE
      // {
      // for(int i=0;i<EXTRACT_TYPES.length;i++){
      // if(EXTRACT_TYPES[i].toString().toLowerCase().startsWith(currWord.toLowerCase())){
      // list.add(new CompletionProposal(
      // EXTRACT_TYPES[i].toString(),
      // offset - currWord.length(),
      // currWord.length(),
      // EXTRACT_TYPES[i].toString().length(),
      // null,
      // //HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
      // EXTRACT_TYPES[i].toString(),
      // null,null));
      // }
      // }
      // }
      //

      //
      //
      // else if(lastWord.equals("dictionary") && (prev2LastWord.equals("extract")))
      // {
      // aqlLibrary = Activator.getLibrary();
      // List<AQLElement> elements = aqlLibrary.getDictionaries(fileList);
      // Iterator<AQLElement> iterator2 = elements.iterator();
      // while(iterator2.hasNext())
      // {
      // AQLElement elmt = iterator2.next();
      // if(elmt.getName().toLowerCase().startsWith(currWord.toLowerCase())){
      // list.add(new CompletionProposal(
      // elmt.getName(),
      // offset - currWord.length(),
      // currWord.length(),
      // elmt.getName().length(),
      // null,
      // //HTMLPlugin.getDefault().getImageRegistry().get(HTMLPlugin.ICON_CSS_PROP),
      // elmt.getName() + " : " + elmt.getType(),
      // null,null));
      // }
      //
      // }
      // }
      //

      if (enableTemplate) {
        ICompletionProposal[] templates = super.computeCompletionProposals (viewer, offset);
        if (isModularProject) {
          for (int i = 0; i < templates.length; i++) {
            String displayString = templates[i].getDisplayString ();
            if ((displayString.contains ("Include"))) { //$NON-NLS-1$ 
              // Do nothing..
            }
            else {
              list.add (templates[i]);
            }
          }
        }
        else {
          for (int i = 0; i < templates.length; i++) {
            String displayString = templates[i].getDisplayString ();
            if ((displayString.contains ("Import")) || (displayString.contains ("Export")) || (displayString.contains ("module")) || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              (displayString.contains ("external dictionary")) || (displayString.contains ("external table")) || (displayString.contains ("require"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              // Do nothing..
            }
            else {
              list.add (templates[i]);
            }
          }
        }
      }

      sortCompilationProposal (list);
      enableTemplate = false;

      prop = list.toArray (new ICompletionProposal[list.size ()]);
    }
    catch (Exception exp) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
        Constants.UNABLE_TO_PROCESS_THE_CONTENT_ASSISTANCE_INFO, exp);
      exp.printStackTrace ();
    }
    return prop;

  }

  private String replaceString (String elmt, int offset)
  {
    // System.out.println(doc.get(offset, offset));

    // try {
    //
    // System.out.println("aa" + doc.get(offset-1, offset) + "bb");
    // System.out.println("aa" + doc.get(offset, offset+1) + "bb");
    //
    // if((doc.get(offset-1, offset) == "'") && (doc.get(offset, offset+1) == "'"))
    // {
    // return elmt;
    // }
    // else if((doc.get(offset-1, offset) == "'") && (doc.get(offset, offset+1) != "'"))
    // {
    // return (elmt + "';");
    // }
    // else if ((doc.get(offset-1, offset) == "") && (doc.get(offset, offset+1) == ""))
    // {
    // return ("'" + elmt + "'");
    // }
    // } catch (BadLocationException e) {
    //
    // }
    return elmt;
  }

  private boolean isComma (IRegion prevRegion2, IRegion region2)
  {
    String str = ""; //$NON-NLS-1$
    try {
      str = doc.get (prevRegion2.getOffset (), region2.getOffset () - prevRegion2.getOffset ());
    }
    catch (BadLocationException e) {}
    if (str.contains (",")) //$NON-NLS-1$
      return true;
    else
      return false;
  }

  private boolean isConstruct (String word)
  {
    List<AQLElement> elements = aqlLibrary.getViews (fileList);
    elements.addAll (aqlLibrary.getTables (fileList));
    elements.addAll (aqlLibrary.getExternalViews (fileList));
    Iterator<AQLElement> iterator2 = elements.iterator ();
    while (iterator2.hasNext ()) {
      AQLElement elmt = iterator2.next ();
      if (elmt.getName ().equals (word)) { return true; }
    }
    return false;
  }

  protected String getSource (ITextViewer viewer)
  {
    return viewer.getDocument ().get ();
  }

  /**
   * Sorts informations of code completion in alphabetical order.
   * 
   * @param prop the list of ICompletionProposal
   */
  public static void sortCompilationProposal (List<ICompletionProposal> prop)
  {
    Collections.sort (prop, new Comparator<ICompletionProposal> () {
      public int compare (ICompletionProposal o1, ICompletionProposal o2)
      {
        return o1.getDisplayString ().compareTo (o2.getDisplayString ());
      }
    });
  }

  public ArrayList<String> listOfCreatedViews ()
  {
    ArrayList<String> createdViewsList = new ArrayList<String> ();
    createdViews = new HashSet<String> ();

    List<AQLElement> elements = aqlLibrary.getViews (fileList);
    Iterator<AQLElement> iterator2 = elements.iterator ();
    while (iterator2.hasNext ()) {
      AQLElement elmt = iterator2.next ();

      if ((elmt.getType ().equals (VIEW_STRING))) {
        createdViews.add (elmt.getName ());
      }
    }
    createdViewsList.addAll (createdViews);

    return createdViewsList;
  }

  /**
   * Splits the searchPath string into an arraylist of separate paths. ';' is assumed to be the delimiter. This method
   * is meant to be used while handling non-modular code.
   * 
   * @param searchPath Value of searchPath property from Text Analytics properties for non-modular project.
   * @return
   */
  private ArrayList<String> getSearchPathList (String searchPath)
  {
    if (!StringUtils.isEmpty (searchPath)) {
      String[] tokens = searchPath.split (SEMICOLON_STRING);
      return new ArrayList<String> (Arrays.asList (tokens));
    }
    else {
      return new ArrayList<String> ();
    }
  }

  /**
   * Returns a set of tam files that belong to the specified directory. It will check for tam files in child directories
   * too.
   * 
   * @param tamDirPath a directory
   * @return A Set containing absolute paths of tam files
   */
  private Map<String, String> getAllTAMFiles (String tamDirPath)
  {
    final Map<String, String> tamNameToContainerMap = new HashMap<String, String> (); // To hold all the tam file path
                                                                                      // set..

    if (tamDirPath != null) { // providing null to Path constructor causes Assert failure.
      IPath path = new Path (tamDirPath).makeAbsolute ();

      File fileRep = path.toFile ();
      if (fileRep != null && fileRep.isFile () && fileRep.exists ()) {
        if (fileRep.getName ().toLowerCase ().endsWith (Constants.ZIP_FILE_EXTENSION)
          || fileRep.getName ().toLowerCase ().endsWith (Constants.JAR_FILE_EXTENSION)) {
          try {
            JarInputStream opener = new JarInputStream (new FileInputStream (fileRep));
            JarEntry entry = opener.getNextJarEntry ();
            while (entry != null) {
              if (!entry.isDirectory () && entry.getName ().toLowerCase ().endsWith (Constants.TAM_FILE_EXTENSION)) {
                tamNameToContainerMap.put (entry.getName (), fileRep.getAbsolutePath ());
              }
              entry = opener.getNextJarEntry ();
            }
          }
          catch (FileNotFoundException e) {
            // ignore
          }
          catch (IOException e) {
            // ignore
          }
        }
      }
      else {
        try {
          new FileTraversal () {
            public void onFile (final File f)
            {
              if (f.getName ().toLowerCase ().endsWith (Constants.TAM_FILE_EXTENSION)) {
                tamNameToContainerMap.put (f.getName (), f.getAbsolutePath ());
              }
            }
          }.traverse (new File (path.toOSString ()));
        }
        catch (IOException e) {
          // e.printStackTrace();
        }
      }
    }
    return tamNameToContainerMap;
  }

  /*
   * We may use this API later on.. private Set<String> getAllTAMFiles(List<String> tamDirPaths){ tamFileSet = new
   * HashSet<String> (); // To hold all the tam file path set.. Iterator<String> dirPathItr = tamDirPaths.iterator();
   * while(dirPathItr.hasNext()) { String temp =dirPathItr.next(); IPath path = new Path(temp).makeAbsolute();
   * System.out.println ("Absolute Path is --> "+ path.toOSString ()); try { new FileTraversal() { public void onFile(
   * final File f ) { if((f.getName().endsWith(".tam"))||(f.getName().endsWith(".TAM"))) { System.out.println
   * ("Tam file path--> :" +f.getAbsolutePath().toString()); tamFileSet.add(f.getAbsolutePath().toString()); } }
   * }.traverse(new File(path.toOSString())); } catch (IOException e) { //e.printStackTrace(); } } return tamFileSet; }
   */
  // This method returns all the files with in a module path list
  private Set<String> getAllCurrModuleFiles (ArrayList<String> modulePathList)
  {
    Iterator<String> iterator1 = modulePathList.iterator ();
    fileSet.add (currentFileLocation);
    // All files that are there in the search path
    while (iterator1.hasNext ()) {
      String temp = iterator1.next ();
      IPath path = new Path (temp).makeAbsolute ();
      try {
        new FileTraversal () {
          public void onFile (final File f)
          {
            if (f.getName ().endsWith (Constants.AQL_FILE_EXTENSION)) {
              moduleFileSet.add (f.getAbsolutePath ().toString ());
            }
          }
        }.traverse (new File (path.toOSString ()));
      }
      catch (IOException e) {
        // e.printStackTrace();
      }
    }
    return moduleFileSet;
  }

  // This method returns all the files within the search path list
  private Set<String> getAllAQLFiles (ArrayList<String> searchpathList)
  {
    if (searchpathList == null) { return null; }
    Iterator<String> pathItr = searchpathList.iterator ();
    fileSet.add (currentFileLocation);
    // All files that are there in the search path
    while (pathItr.hasNext ()) {
      String temp = pathItr.next ();
      IPath path = new Path (temp).makeAbsolute ();
      final String aqlFilePath = path.toOSString ();
      try {
        new FileTraversal () {
          public void onFile (final File f)
          {
            if (f.exists ()
              && f.getName ().endsWith (Constants.AQL_FILE_EXTENSION)
              && (ProjectPreferencesUtil.isAQLInSearchPath (ProjectUtils.getProjectFromFilePath (f.getAbsolutePath ()),
                f.getAbsolutePath ()))) // files can be from other projects too. Not necessarily current project.
            {
              fileSet.add (f.getAbsolutePath ().toString ());
            }
          }
        }.traverse (new File (path.toOSString ()));
      }
      catch (IOException e) {
        // e.printStackTrace();
      }
    }
    return fileSet;
  }

  private Set<String> getAllAQLFilesRelative (ArrayList<String> searchpathList)
  {
    if (searchpathList == null) { return null; }
    Iterator<String> pathItr = searchpathList.iterator ();
    while (pathItr.hasNext ()) {
      final String temp = pathItr.next ();
      IPath path = new Path (temp).makeAbsolute ();
      try {
        // change this for non recursive in include file paths
        // new FileTraversal(){
        new FileTraversal () {
          public void onFile (final File f)
          {
            if (f.getName ().endsWith (Constants.AQL_FILE_EXTENSION)) {
              String path = f.getAbsolutePath ();
              if (ProjectPreferencesUtil.isAQLInSearchPath (ProjectUtils.getProjectFromFilePath (f.getAbsolutePath ()),
                path)) // files can be from other projects too. Not necessarily current project.
              {
                String relative = FileUtils.createValidatedFile (temp).toURI ().relativize (new File (path).toURI ()).getPath ();
                // add relative path so that we get it during include template
                fileSetRelative.add (relative);
              }
            }
          }
        }.traverse (new File (path.toOSString ()));
      }
      catch (IOException e) {
        // e.printStackTrace();
      }
    }
    return fileSetRelative;
  }

  public IRegion getCurrentRegion (IDocument doc, int offset)
  {
    return CurrentWordFinder.findWord (doc, offset);
  }

  public IRegion getLastRegion (IDocument doc, int offset)
  {
    return CurrentWordFinder.findLastWord (doc, offset);
  }

  private String aqlComment2space (String source)
  {

    int index = 0;
    int last = 0;
    StringBuffer sb = new StringBuffer ();
    while ((index = source.indexOf ("/*", last)) != -1) { //$NON-NLS-1$
      int end = source.indexOf ("*/", index); //$NON-NLS-1$
      if (end != -1) {
        sb.append (source.substring (last, index));
        int length = end - index + 2;
        for (int i = 0; i < length; i++) {
          sb.append (" "); //$NON-NLS-1$
        }
      }
      else {
        break;
      }
      last = end + 2;
    }
    while ((index = source.indexOf ("--", last)) != -1) //$NON-NLS-1$
    {
      int end = source.indexOf ("/n", index); //$NON-NLS-1$
      if (end != -1) {
        sb.append (source.substring (last, index));
        int length = end - index + 2;
        for (int i = 0; i < length; i++) {
          sb.append (" "); //$NON-NLS-1$
        }
      }
      else {
        break;
      }
      last = end + 2;
    }

    if (last != source.length () - 1) {
      sb.append (source.substring (last));
    }
    String string = sb.toString ();
    // string.trim();
    return string.trim ();
  }

  public IContextInformation[] computeContextInformation (ITextViewer viewer, int offset)
  {
    ContextInformation[] info = new ContextInformation[0];
    return info;
  }

  public char[] getCompletionProposalAutoActivationCharacters ()
  {
    return new char[0];
  }

  public char[] getContextInformationAutoActivationCharacters ()
  {
    return new char[0];
  }

  public String getErrorMessage ()
  {
    return "error"; //$NON-NLS-1$
  }

  // This method returns all the search path for modular type projects..
  private ArrayList<String> getModularProjectSearchPathList (IProject project)
  {
    List<String> projectNames = new ArrayList<String> ();
    String projectName = project.getLocation ().toOSString ();
    projectNames.add (projectName);
    Set<String> aqlFilePaths = new HashSet<String> ();
    if (projectName == null) { return null; }
    IProject refProjects[] = null;
    // Calculating all the dependent projects of the current project..
    try {
      refProjects = project.getReferencedProjects ();
    }
    catch (CoreException exp) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
        Constants.UNABLE_TO_PROCESS_THE_CONTENT_ASSISTANCE_INFO, exp);
      exp.printStackTrace ();
    }
    if ((refProjects != null) && (refProjects.length > 0)) {
      for (IProject proj : refProjects) {
        // The referenced project may be there in the Java Build Path. But may not be there in workspace.
        if(proj.exists ()){
       // collecting all the project names and later use them as keys to search for the source..
          projectNames.add (proj.getLocation ().toOSString ());
        }
      }
    }
    // Calculating file paths for all the referenced module projects as well...
    for (String prjName : projectNames) {
      AQLModuleProject modularProject = aqlLibrary.getModuleLibraryMap ().get (prjName);
      if (modularProject == null) {
        continue; // Continue obtaining file paths for other projects.
      }
      HashMap<String, AQLModule> modules = modularProject.getAQLModules ();
      Iterator<String> moduleItr = modules.keySet ().iterator ();
      String moduleName = ""; //$NON-NLS-1$
      while (moduleItr.hasNext ()) {
        moduleName = moduleItr.next ();
        AQLModule aqlModule = modules.get (moduleName);
        List<String> paths = aqlModule.getAqlFilePaths ();
        for (String p : paths) {
          aqlFilePaths.add (p);
        }
      }
    }// end of for loop..
    return new ArrayList<String> (aqlFilePaths);
  }

  /**
   * Searches in aql library for specified module belonging to specifed project. If found, returns a list of paths to
   * aql files belonging to that module.
   * 
   * @param project IProject instance
   * @param modulePath Absolute path of the module (Should follow OS format)
   * @return An ArrayList of files belonging the specified module. If module is not found, returns an empty list.
   */
  private ArrayList<String> getModuleSearchPathList (IProject project, String modulePath)
  {
    String projectName = project.getLocation ().toOSString ();
    Set<String> aqlFilePaths = new HashSet<String> ();
    if (projectName == null) { return new ArrayList<String>(); }
    // Calculating file paths for all aql files with in a module
    AQLModuleProject modularProject = aqlLibrary.getModuleLibraryMap ().get (projectName);
    if (modularProject == null) { return new ArrayList<String>(); }
    HashMap<String, AQLModule> modules = modularProject.getAQLModules ();
    // Iterator<String> moduleItr = modules.keySet().iterator();
    AQLModule aqlModule = modules.get (modulePath);
    if (aqlModule == null) { return new ArrayList<String>(); }
    List<String> paths = aqlModule.getAqlFilePaths ();
    for (String p : paths) {
      aqlFilePaths.add (p);
    }
    return new ArrayList<String> (aqlFilePaths);
  }

  private ArrayList<String> getTamMetadataExternalViews (ModuleMetadata metadata)
  {
    List<Pair<String, String>> eViewList = metadata.getExternalViews ();
    ArrayList<String> eViewNameList = new ArrayList<String> ();
    for (Pair<String, String> eViewData : eViewList) {
      eViewNameList.add (eViewData.first);
    }
    return eViewNameList;
  }

  // This method returns the modile name from by taking tam file path
  private String getTamModuleName (String tamPath)
  {
    String moduleName = ""; //$NON-NLS-1$
    // System.out.println ("In getModuleName method.."+tamPath);
    // File path = new File(new URI(tamPath));
    File path = FileUtils.createValidatedFile (tamPath);
    if (path.isDirectory ()) { // Need to check if this can be directory or only file..
      // System.out.println ("Its a directory...");
      for (File f : path.listFiles ()) {
        if (f.isFile () && (f.getName ().endsWith (".tam") || f.getName ().endsWith (".TAM"))) { //$NON-NLS-1$ //$NON-NLS-2$
          moduleName = (f.getName ().substring (0, f.getName ().length () - 4));
          // System.out.println ("Module Name is :" +moduleName);
        }
      }
    }
    else if (path.isFile ()) { // Always this should be open...
      // System.out.println ("Its a file...");
      if (path.isFile () && (path.getName ().endsWith (".tam") || path.getName ().endsWith (".TAM"))) { //$NON-NLS-1$ //$NON-NLS-2$
        moduleName = (path.getName ().substring (0, path.getName ().length () - 4));
        // System.out.println ("Module Name is :" +moduleName);
      }
    }

    return moduleName;
  }

  private List<String> getAllTamPaths (IProject proj)
  {
    // allTamPaths contains final tamPath list..
    List<String> allTamPaths = new ArrayList<String> ();
    // Folders containing tams referred to by the project
    String tamPath = ProjectUtils.getImportedTams (proj);
    // System.out.println ("Fulllength Tam Path.. --> " + tamPath);
    if (tamPath != null && tamPath.length () > 0) {
      String tamPaths[] = tamPath.split (Constants.DATAPATH_SEPARATOR);
      for (String path : tamPaths) {
        String absPath = ProjectPreferencesUtil.getAbsolutePath (path);
        if (!StringUtils.isEmpty (absPath)) { // getAbsoluePath(..) can return null or empty string. Exclude them.
          allTamPaths.add (absPath);
        }
      }
    }
    // Folders containing tamPath for referenced projects..
    try {
      for (IProject refProject : proj.getReferencedProjects ()) {
        if (refProject.hasNature (Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject (refProject)) {
          String refTamPath = ProjectUtils.getImportedTams (refProject);
          if (refTamPath != null && refTamPath.length () > 0) {
            String refTamPaths[] = refTamPath.split (Constants.DATAPATH_SEPARATOR);
            for (String path : refTamPaths) {
              String absPath = ProjectPreferencesUtil.getAbsolutePath (path);
              if (!StringUtils.isEmpty (absPath)) { // getAbsoluePath(..) can return null or empty string. Exclude them.
                allTamPaths.add (absPath);
              }
            }
          }
        }
      }
    }
    catch (CoreException exp) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowError (
        Constants.UNABLE_TO_PROCESS_THE_CONTENT_ASSISTANCE_INFO, exp);
      exp.printStackTrace ();
    }
    return allTamPaths;
  }
  // --------------------------------------------------------------
  // public IContextInformationValidator getContextInformationValidator() {
  // return new ContextInformationValidator(this);
  // }

}

class FileTraversal
{
  public final void traverse (final File f) throws IOException
  {
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

class FileTraversalNonRecursive
{
  public final void traverse (final File f) throws IOException
  {
    if (f.isDirectory ()) {

    }
    onFile (f);
    final File[] childs = f.listFiles ();
    for (File child : childs) {
      onFile (child);
    }
  }

  public void onFile (final File f)
  {}
}
