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
package com.ibm.biginsights.textanalytics.launch;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.avatar.api.tam.DictionaryMetadata;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.api.tam.ModuleMetadataFactory;
import com.ibm.avatar.api.tam.TableMetadata;
import com.ibm.avatar.aql.AQLParser;
import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.StatementList;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.AQLModuleLibrary;
import com.ibm.biginsights.textanalytics.aql.library.AQLParseErrorHandler;
import com.ibm.biginsights.textanalytics.aql.library.Messages;
import com.ibm.biginsights.textanalytics.aql.library.ModularAQLModel;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class provides methods required to interact with aql library.
 * They exist outside aqllibrary plugin to retain its independence
 * from other plugin projects. The methods are relevant to only this
 * package.
 * This class is meant to be used only within this package.
 * It assumes it's dealing with 2.0 projects only.
 * 
 *
 */
public class AQLLibraryUtil
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
  
	/**
   * Populates Modular AQLLibrary instance with constructs from a given v2.0 project.
   * @param project
   */
  public static void populateAQLLibrary(IProject project) { 
    try {
      if (ProjectUtils.isModularProject (project) && project.hasNature (Activator.NATURE_ID)) {
        final Set<String> fileSet = new LinkedHashSet<String>();
        AQLModuleLibrary aqllib = AQLModuleLibrary.getInstance ();
        ModularAQLModel model = (ModularAQLModel) aqllib.getAQLModel ();
        Map<String, String> projectSrc = new LinkedHashMap<String,String>();
        getAllRelatedProjectSrcPaths(project.getName (),projectSrc);  
        for (String proj: projectSrc.keySet ()) {
          String srcPath = ProjectPreferencesUtil.getAbsolutePath (projectSrc.get(proj));

          if (srcPath != null && (aqllib.getParsedPath () == null || !aqllib.getParsedPath ().contains (srcPath.hashCode ()))) {
            //prepare file list
            String[] srcDirs = srcPath.toString ().split (Constants.DATAPATH_SEPARATOR);
            for (int i = 0; i < srcDirs.length; i++) {
              IPath path = new Path (srcDirs[i]).makeAbsolute ();
                new FileTraversal () {
                  public void onFile (final File f)
                  {
                    if (f.getName ().endsWith (Constants.AQL_FILE_EXTENSION)) {
                      String filePath = f.getAbsolutePath ().toString ();
                      IFile iFile = getIFile (filePath);
                      if ( iFile != null && ProjectUtils.isValidAQLFile20 (iFile) )
                        fileSet.add (filePath);
                    }
                  }
                }.traverse (new File (path.toOSString ()));
            }

            Iterator<String> fileIterator = fileSet.iterator ();
            while (fileIterator.hasNext ()) {
              String aqlFilePath = fileIterator.next ();
              AQLParser parser;
              try {
                IProject fileProject = getProject (aqlFilePath);
                parser = new AQLParser (FileUtils.fileToStr (new File (aqlFilePath), fileProject.getDefaultCharset ()),
                  aqlFilePath);
                parser.setBackwardCompatibilityMode (false);
                parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser

                StatementList statementList = parser.parse ();
                String projectName = fileProject.getLocation ().toOSString ();
                String moduleName = getModule (aqlFilePath);
                model.create (aqlFilePath, projectName, moduleName, statementList);
                
                AQLParseErrorHandler reporter = new AQLParseErrorHandler();
                LinkedList<ParseException> parseException = statementList.getParseErrors();
                Iterator<ParseException> itr = parseException.iterator();
                IPath path = new Path(aqlFilePath);
                IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
                reporter.deleteMarkers(file);
                while(itr.hasNext())
                {
                  try {
                    ParseException pe1 = itr.next();
                    reporter.handleError(pe1, IMarker.SEVERITY_ERROR, file);
                  } catch (Exception e) {
                  } 
                }
              } catch (IOException e) {
                LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.ERROR_AQLFILE_READ_ERROR + " " + aqlFilePath, e);
              }
            }
          }
        }
      }
    } catch (CoreException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.ERROR_PROJECT_NATURE_ERROR, e);
    }
  }

  /**
   * Given a project and a Map instance, populates the map with its source dirs and drills
   * down its required projects, to add their source dirs and their related projects.
   * This method assumes that cyclic dependency will not be allowed between aql projects.
   * @param projectName
   * @param projectSourceMap key - project name, value - source directories with ; as delimiter
   * @return The map provided to the method in parameters
   */
  public static Map<String,String> getAllRelatedProjectSrcPaths(String projectName, Map<String,String> projectSrcMap) {
    IProject project = ProjectUtils.getProject (projectName);
    IFolder srcDir = ProjectUtils.getTextAnalyticsSrcFolder (projectName);
    if (srcDir != null) {
      projectSrcMap.put (projectName, srcDir.getLocation ().toString ());
    } else {
      projectSrcMap.put (projectName, project.getLocation ().toString ());
    }
    try {
      for (IProject proj : project.getReferencedProjects ()) {
        if (!projectSrcMap.containsKey (proj.getName ()) && proj.hasNature (Activator.NATURE_ID)) {
          getAllRelatedProjectSrcPaths(proj.getName(),projectSrcMap);
        }
      }
    } catch (CoreException e) {
      //do nothing
    }
    return projectSrcMap;
  }

  /**
   * Returns containing project, based on specified file path
   * @param aqlFilePath
   * @return IProject instance for the project containing the file.
   */
  private static IProject getProject(String aqlFilePath) 
  {
    final IFile file = getIFile(aqlFilePath);
    if(file == null) return null;
    else
    {
      return file.getProject();
    }
  }

  /**
   * Returns IFile object of the specified file path
   * @param aqlFilePath
   * @return IFile instance for the file path.
   */
  private static IFile getIFile (String aqlFilePath) 
  {
    IPath path = new Path(aqlFilePath);
    return FileBuffers.getWorkspaceFileAtLocation(path);
  }

  /**
   * Retrieves module directory path from the path of an aql file.
   * @param aqlFilePath - should be absolute
   * @return Absolute path of the module directory containing the aql file.
   */
  private static String getModule(String aqlFilePath) {
    IPath path = new Path(aqlFilePath);
    final IFile file = FileBuffers.getWorkspaceFileAtLocation (path);
    if (file == null) {
      return null;
    } else {
      return file.getParent ().getLocation ().toOSString ();
    }
  }

  /**
   * Returns a List of names of modules being imported within a specified module.
   * @param projectName
   * @param moduleName
   * @return List would be empty if an invalid project or module name is provided.
   */
  public static List<String> getImportedModules(String projectName, String moduleName) {
    if (!projectName.trim ().isEmpty ()) {
    AQLModuleLibrary lib = AQLModuleLibrary.getInstance ();

    return lib.getImportedModules (getAQLFilesFromModule(projectName, moduleName));
    } else {
      return new ArrayList<String>();
    }

  }

  /**
   * Returns list of absolute paths to aql files belonging to specified module
   * @param projectName
   * @param moduleName
   * @return List<String> instance containing aql file paths. 
   * List would be empty if an invalid project or module is provided.
   */
  public static List<String> getAQLFilesFromModule(String projectName, String moduleName) {
    ArrayList<String> fileList = new ArrayList<String>();
    IFolder projectSrcDir= ProjectUtils.getTextAnalyticsSrcFolder (projectName);
    if (projectSrcDir != null && projectSrcDir.exists()) {
      IResource moduleSrcDir = projectSrcDir.findMember(moduleName);
      if (moduleSrcDir != null && moduleSrcDir instanceof IFolder) {
        try {
          for (IResource file : ((IFolder)moduleSrcDir).members ()) {
            if (file instanceof IFile && 
                file.getName ().endsWith(Constants.AQL_FILE_EXTENSION)) {
              fileList.add (file.getLocation ().toOSString ());
            }
          }
        }
        catch (CoreException e) {
          //e.printStackTrace();
        }
      }
    }
    return fileList;
  }
  
  /**
   * Given a list of modules in a project, this method will find other modules dependent on them.
   * It will classify into separate groups, those with source and those that could be in tams.
   * @param projectName Name of a project
   * @param moduleNames List of modules within above mentioned project
   * @param checkedModules Should be an empty List when calling.
   * @return A container having two sets of module names, those with source and those without.
   * All modulenames will be qualified with their containing project names
   * @throws CoreException
   */
  public static ModulesContainer getRequiredModuleSetsWithQualification(String projectName, List<String> moduleNames, Set<Map.Entry<String, String>> checkedModules) throws CoreException {
    //Find referenced projects and add them to a list along with current project.
    IProject currProject = ProjectUtils.getProject (projectName);

    IProject[] referencedProjects = currProject.getReferencedProjects ();
    
    List<IProject> candidateProjects= new ArrayList<IProject>();
    candidateProjects.add (currProject);
    for (IProject proj: referencedProjects) {
      candidateProjects.add(proj);
    }
    
    //Generate list of imported modules for specifed modules in current project
    Set<String> importedModulesInCurrProject = new LinkedHashSet<String>();
    for (String module: moduleNames) {
      importedModulesInCurrProject.addAll  (getImportedModules(currProject.getName (), module));
      checkedModules.add(new AbstractMap.SimpleEntry<String,String>(projectName,module));
    }
    
    //match entries in importedmodules list with with modules in candidateprojects' projects
    //qualify matched entries with the name of the project they matched. or use a map with projectname as key, list of modules as value
    Set<Map.Entry<String, String>> modulesWithSrc = new LinkedHashSet<Map.Entry<String,String>>();
    Set<Map.Entry<String,String>> modulesInTams = new LinkedHashSet<Map.Entry<String,String>>();
    for (String importedModule: importedModulesInCurrProject) {
      boolean found = false;
      for (IProject cProj: candidateProjects) {
        for (String moduleInProj: ProjectUtils.getModules (cProj)) { //this method uses file system to determine modules
          if (importedModule.equals(moduleInProj)) {
            found = true;
            break;
          }
        }
        if (found) {
          modulesWithSrc.add(new AbstractMap.SimpleEntry<String,String> (cProj.getName(), importedModule));
          break;
        }
      }
      if (!found) {
        // must be a module in one of the tams imported by current project
        Set<String> relatedModules = new LinkedHashSet<String> ();
        if (!modulesInTams.contains (new AbstractMap.SimpleEntry<String, String> (projectName, importedModule))) {
          // this condition is there to avoid unnecessary recalculations
          relatedModules.add (importedModule);
          // include all related modules available in the project's tamlist.
          relatedModules.addAll (getAllRelatedModulesInTAMs (importedModule, currProject, new LinkedHashSet<String> ()));
          for (String module : relatedModules) {
            modulesInTams.add (new AbstractMap.SimpleEntry<String, String> (projectName, module));
          }
        }

        // also possible that the required project for that project
        // has not been included in references.
      }
    }
    
    Set<Map.Entry<String, String>> lookInModules = new LinkedHashSet<Map.Entry<String,String>>(modulesWithSrc);
    lookInModules.removeAll (checkedModules); //Contributes to recursion termination
    
    ModulesContainer preparedModules = new ModulesContainer();
    preparedModules.addModulesWitSrc (modulesWithSrc);
    preparedModules.addModulesInTams (modulesInTams);
    for (Map.Entry<String,List<String>> projectModuleList: getProjectModulesMap(lookInModules).entrySet ()) {
      ModulesContainer fromImportedModules = getRequiredModuleSetsWithQualification(projectModuleList.getKey (),projectModuleList.getValue (),checkedModules);
      preparedModules.addModulesWitSrc (fromImportedModules.getModulesWithSrc ());
      preparedModules.addModulesInTams (fromImportedModules.getModulesInTams ());
    }
    
    return preparedModules; 
  }
  
  /**
   * Sorts the set of qualified module names into a map of project names, 
   * each entry containing a list of modules associated with that project.
   * @param qualifiedModules A set of key value pairs with module name as the value 
   * and name of the project containing it as the key.
   * @return
   */
  public static Map<String, List<String>> getProjectModulesMap (Set<Map.Entry<String, String>> qualifiedModules) {
    Set<String> projectNames = new LinkedHashSet<String>();
    for (Map.Entry<String, String> qMod: qualifiedModules) {
        projectNames.add (qMod.getKey ());
    }
    
    
    Map<String,List<String>> projectModulesMap = new LinkedHashMap<String,List<String>>();
    for (String proj: projectNames) {
      List<String> moduleNames = new ArrayList<String>();
      for (Map.Entry<String, String> qMod: qualifiedModules) {
        if (qMod.getKey ().equals(proj)) {
          moduleNames.add (qMod.getValue ());
        }
      }
      projectModulesMap.put(proj,moduleNames);
    }
    return projectModulesMap;
  }

  public static List<String> getRequiredExtTables (String projectName, String[] modules)
  {
    List<String> requiredList = new ArrayList<String> ();

    try {
      ModulesContainer requiredModuleSets = AQLLibraryUtil
              .getRequiredModuleSetsWithQualification (
                            projectName,
                            Arrays.asList (modules), 
                            new LinkedHashSet<Map.Entry<String, String>>());

      Map<String,List<String>> modulesWithSrc = AQLLibraryUtil
              .getProjectModulesMap (requiredModuleSets.getModulesWithSrc ());

      Map<String,List<String>> modulesInTams = AQLLibraryUtil
              .getProjectModulesMap (requiredModuleSets.getModulesInTams ());

      requiredList = new ArrayList<String> ();

      //----- Getting external tables from tams
      for (String proj: modulesInTams.keySet ()){
        IProject project = ProjectUtils.getProject (proj);
        if (project != null) {
          String tamPaths = ProjectPreferencesUtil.getURIsFromWorkspacePaths (ProjectUtils.getImportedTams (project));
          ModuleMetadata[] m = ModuleMetadataFactory.readMetaData (modulesInTams.get (proj).toArray (new String[0]),
            tamPaths);

          for (int i = 0; i < m.length; i++) {

            String[] extTableNames = m[i].getExternalTables ();

            // Get only the required tables
            for (int j = 0; j < extTableNames.length; j++) {
              TableMetadata md = m[i].getTableMetadata (extTableNames[j]);
              if (ProjectUtils.isTableRequired (md))
                requiredList.add (extTableNames[j]);
            }

          }
        }
      }

      //----- Getting external tables from modules with source and from sources
      ArrayList<String> requiredModuleFilePaths = new ArrayList<String> ();
      for (String proj : modulesWithSrc.keySet ()) {
        for (String module: modulesWithSrc.get (proj)) {
          requiredModuleFilePaths.addAll (AQLLibraryUtil.getAQLFilesFromModule (proj, module));
        }
      }
      for (String baseModule : modules) {
        requiredModuleFilePaths.addAll (AQLLibraryUtil.getAQLFilesFromModule(projectName,baseModule));
      }

      //----- Get only the required tables
      for (AQLElement extTable : AQLModuleLibrary.getInstance ().getExternalTables (requiredModuleFilePaths)) {
        String table = extTable.getName (); //hope there are no more changes to format
        if ( AQLModuleLibrary.getInstance ().isRequiredExternalTableOrDictionary (extTable) )
          requiredList.add (table);
      }

    } catch (CoreException e) {
      LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logError(
        Messages.ERROR_REQ_MODULES_GEN_ERROR, e); //$NON-NLS-1$
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
        Messages.ERROR_ERR_MODULE_METADATA, e); //$NON-NLS-1$
    }

    return requiredList;
  }

  public static List<String> getRequiredExtDictionaries (String projectName, String[] modules)
  {
    List<String> requiredList = new ArrayList<String> ();

    try {
      ModulesContainer requiredModuleSets = AQLLibraryUtil
              .getRequiredModuleSetsWithQualification (
                            projectName,
                            Arrays.asList (modules), 
                            new LinkedHashSet<Map.Entry<String, String>>());

      Map<String,List<String>> modulesWithSrc = AQLLibraryUtil
              .getProjectModulesMap (requiredModuleSets.getModulesWithSrc ());

      Map<String,List<String>> modulesInTams = AQLLibraryUtil
              .getProjectModulesMap (requiredModuleSets.getModulesInTams ());

      requiredList = new ArrayList<String> ();

      //----- Getting external dictionaries from tams
      for (String proj: modulesInTams.keySet ()){
        IProject project = ProjectUtils.getProject (proj);
        if (project != null) {
          String tamPaths = ProjectPreferencesUtil.getURIsFromWorkspacePaths (ProjectUtils.getImportedTams (project));
          ModuleMetadata[] m = ModuleMetadataFactory.readMetaData (modulesInTams.get (proj).toArray (new String[0]),
            tamPaths);

          for (int i = 0; i < m.length; i++) {

            String[] extDictNames = m[i].getExternalDictionaries ();

            // Get only the required dictionaries
            for (int j = 0; j < extDictNames.length; j++) {
              DictionaryMetadata md = m[i].getDictionaryMetadata (extDictNames[j]);
              if (ProjectUtils.isDictRequired (md))
                requiredList.add (extDictNames[j]);
            }
          }
        }
      }

      //----- Getting external dictionaries from modules with source and from sources
      ArrayList<String> requiredModuleFilePaths = new ArrayList<String> ();
      for (String proj : modulesWithSrc.keySet ()) {
        for (String module: modulesWithSrc.get (proj)) {
          requiredModuleFilePaths.addAll (AQLLibraryUtil.getAQLFilesFromModule (proj, module));
        }
      }
      for (String baseModule : modules) {
        requiredModuleFilePaths.addAll (AQLLibraryUtil.getAQLFilesFromModule(projectName,baseModule));
      }

      //----- Get only the required dictionaries
      for (AQLElement extDictionary : AQLModuleLibrary.getInstance ().getExternalDictionaries (requiredModuleFilePaths)) {
        if ( AQLModuleLibrary.getInstance ().isRequiredExternalTableOrDictionary (extDictionary) )
          requiredList.add (extDictionary.getName ());
      }
    } catch (CoreException e) {
      LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logError(
        Messages.ERROR_REQ_MODULES_GEN_ERROR, e); //$NON-NLS-1$
    }
    catch (Exception e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (
        Messages.ERROR_ERR_MODULE_METADATA, e); //$NON-NLS-1$
    }

    return requiredList;
  }
  
  /**
   * Finds out all modules that the specified module is dependent on, directly or indirectly, by reading module metadata
   * from the TAMs in the specified project's tam path.
   * 
   * @param moduleName Name of the module
   * @param project Project where the tam for module has been specified in its tam path
   * @param checkedModules A set containing names of modules that have already been checked for dependencies. Should be
   *          an empty set when calling.
   * @return A set of names of modules that are directly or indirectly required by the specified module
   */
  private static Set<String> getAllRelatedModulesInTAMs (String moduleName, IProject project, Set<String> checkedModules)
  {
    String tamPaths = ProjectPreferencesUtil.getURIsFromWorkspacePaths (ProjectUtils.getImportedTams (project));
    Set<String> requiredModules = new LinkedHashSet<String> ();
    try {
      checkedModules.add (moduleName); // Keeping a list of already checked modules to avoid getting stuck in a cycle.
      ModuleMetadata md = ModuleMetadataFactory.readMetaData (moduleName, tamPaths);
      requiredModules.addAll (md.getDependentModules ());
      Set<String> indirectDependencies = new LinkedHashSet<String> ();
      for (String module : requiredModules) {
        if (!checkedModules.contains (module)) {
          indirectDependencies.addAll (getAllRelatedModulesInTAMs (module, project, checkedModules)); // recursive call
                                                                                                      // happening here.
        }
      }
      requiredModules.addAll (indirectDependencies);
      return requiredModules;
    }
    catch (TextAnalyticsException e) {
      LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logWarning (e.getMessage ());
      return requiredModules;
    }
  }
}

/**
 * Introduced to serve as a return type for method getRequiredModuleSetsWithQualification(...)
 * It has two Set<String> objects. Methods for accessing the sets having been provided.
 * 
 *
 */
class ModulesContainer {
  private final Set<Map.Entry<String, String>> modulesWithSrc;
  private final Set<Map.Entry<String, String>> modulesInTams;
  
  public ModulesContainer() {
    modulesWithSrc = new LinkedHashSet<Map.Entry<String, String>>();
    modulesInTams = new LinkedHashSet<Map.Entry<String, String>>();
  }
  
  public Set<Map.Entry<String, String>> getModulesWithSrc() {
    return modulesWithSrc;
  }
  
  public Set<Map.Entry<String, String>> getModulesInTams() {
    return modulesInTams;
  }
  
  public void addModulesWitSrc(Set<Map.Entry<String, String>> set) {
    modulesWithSrc.addAll (set);
  }
  
  public void addModulesInTams(Set<Map.Entry<String, String>> set) {
    modulesInTams.addAll (set);
  }
}

class FileTraversal {
  public final void traverse(final File f) {
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
