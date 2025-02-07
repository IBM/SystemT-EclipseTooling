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
package com.ibm.biginsights.textanalytics.aql.editor.callhierarchy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.aql.AQLParser;
import com.ibm.avatar.aql.StatementList;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.Messages;
import com.ibm.biginsights.textanalytics.aql.editor.common.FileTraversal;
import com.ibm.biginsights.textanalytics.aql.library.ModularAQLModel;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.AQLFile;
import com.ibm.biginsights.textanalytics.aql.library.AQLModule;
import com.ibm.biginsights.textanalytics.aql.library.AQLModuleProject;
import com.ibm.biginsights.textanalytics.aql.library.AQLProject;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class is used by classes DependencyHierarchyView and ReferencedHierarchyView to
 * create nodes for the trees in their views.
 * , Mukund Babbar
 */

public class HierarchyRootNode {


  
	private List<HierarchyNode> children = new ArrayList<HierarchyNode>();
  protected IAQLLibrary aqlLibrary;
  private IFileEditorInput input;
  private IProject startProject;
  private String startModule;
  private boolean isDependencyHierarchyRoot; //set to true if this a root for a dependency hierarchy, false if it is a root for a reference hierarchy
  private String currToken;
  private boolean forModularProject;
  
  private Map<String,String> projectSrcDirMap;
  
  public HierarchyRootNode(String token) {
    
    IEditorInput genericInput = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput();
    if (genericInput instanceof IFileEditorInput) {
      input = (IFileEditorInput) genericInput;
      //startProject is assigned the name of the project containing the file currently opened by the aql editor, 
      //as that is where the command to show dependency/reference hierarchy view came from.
      startProject = input.getFile().getProject();
      forModularProject = ProjectUtils.isModularProject (startProject); //It is assumed that all related projects will be of the same time. Hence calculating only once.
      
      //populate projectSrcDirMap with all projects related to the startProject,
      //along with source directories.
      projectSrcDirMap = new LinkedHashMap<String,String>();
      getAllRelatedProjectsSourceMap(startProject, projectSrcDirMap);
      
      if (forModularProject) {
        aqlLibrary = Activator.getModularLibrary ();
        String[] tokenParts = ProjectUtils.splitQualifiedAQLElementName (token); //token.split ("\\."); //$NON-NLS-1$
        if (tokenParts.length == 2) {
          startModule = tokenParts[0];
          currToken = tokenParts[1];
        } else {
          startModule = input.getFile ().getParent ().getName ();
          currToken = token;
        }
        
        for (String projectName : projectSrcDirMap.keySet ()) { 
          //Referenced and Referencing projects may have not been parsed.
          //Hence, parsing them and adding their constructs to aqllibrary.
          addToAQLLib(projectName);
        }
        
      } else {
        aqlLibrary = Activator.getLibrary ();
        startModule = ""; //$NON-NLS-1$
        currToken = token;
        //In 1.3/1.4 projects, element relationships will be confined to a single project.
        //Parsing would have been already done by the editor.
      }
    }
  }
  
  /**
   * Parse source files from given project, populate aql library with their elements.
   * Supposed to be used only with modular aql projects.
   * @param projectName
   */
  public void addToAQLLib(String projectName) {
    if (forModularProject) {
      ModularAQLModel model = (ModularAQLModel)aqlLibrary.getAQLModel ();

      IProject project = ProjectUtils.getProject (projectName);
      String srcPath = projectSrcDirMap.get (projectName);

      if (aqlLibrary.getParsedPath () == null || !aqlLibrary.getParsedPath ().contains (srcPath.toString ().hashCode ())) {
        Iterator<String> fileIterator = getAllAQLFiles(Arrays.asList(srcPath.split (Constants.DATAPATH_SEPARATOR))).iterator ();
        while (fileIterator.hasNext ()) {
          String aqlFilePath = fileIterator.next ();
          AQLParser parser;
          try {
            parser = new AQLParser(FileUtils.fileToStr(new File(aqlFilePath), project.getDefaultCharset()),aqlFilePath);
            parser.setBackwardCompatibilityMode (false);
            parser.setTabSize (1); // As eclipse treats TAB chars as of size 1, setting the same in parser
            StatementList statementList = parser.parse ();
            model.create (aqlFilePath, getProjectKeyForAQLLibrary (projectName), getModuleKeyForAQLLibrary (aqlFilePath), statementList);
            //will not mark any errors in aql files. Expect ReconcilingStrategy to do that.
          } catch (IOException e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logWarning (Messages.HierarchyRootNode_FILE_NOT_FOUND);
            //e.printStackTrace ();
          } catch (CoreException e) {
            LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logWarning (Messages.HierarchyRootNode_PARSE_ERROR);
            //e.printStackTrace();
          }
        }
        aqlLibrary.addParsedPath (srcPath.hashCode ());
      }

    }
  }
  
  /**
   * Given a project and a Map instance, populates the map with its source dirs and drills down required projects and
   * projects referencing it, to add their source dirs and their related projects.
   * 
   * @param project IProject instance
   * @param projectSourceMap key - project name, value - source directories with ; as delimiter
   * @return The map provided to the method in parameters
   */
  public Map<String, String> getAllRelatedProjectsSourceMap (IProject project, Map<String, String> projectSourceMap)
  {
    if (forModularProject) {
      IFolder srcPath = ProjectUtils.getTextAnalyticsSrcFolder (project); // This is one way to get src folder for modular projects.
                                                             // Avoid reading directly from systemtproperties or
                                                             // preferencestore. If you still need to use them, remember
                                                             // that the value would be a project relative path.
      String path;
      if (srcPath != null) {
        path = srcPath.getLocation ().toString ();
      }
      else {
        path = project.getLocation ().toString ();
      }
      projectSourceMap.put (project.getName (), path); // Add the project name and its source directory as a map entry
      try {
        IProject[] referenced = project.getReferencedProjects (); // this is an array of projects, current project is
                                                                  // dependent on
        for (int i = 0; i < referenced.length; i++) {
          if (!projectSourceMap.containsKey (referenced[i].getName ()) // stop if the referenced project's source has
                                                                       // already been found
            && ProjectUtils.isModularProject (referenced[i])) {
            getAllRelatedProjectsSourceMap (referenced[i], projectSourceMap); // recursive
          }
        }

        IProject[] referencing = project.getReferencingProjects (); // this is an array projects dependent on current
                                                                    // project
        for (int i = 0; i < referencing.length; i++) {
          if (!projectSourceMap.containsKey (referencing[i].getName ()) // stop if the referencing project's source has
                                                                        // already been found
            && ProjectUtils.isModularProject (referencing[i])) {
            getAllRelatedProjectsSourceMap (referencing[i], projectSourceMap); // recursive
          }
        }
      }
      catch (CoreException e) {
        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logError (Messages.AQLEditor_ERR_PROJ_REF, e);
      }
    }
    else {
      projectSourceMap.put (project.getName (),
        ProjectPreferencesUtil.getAbsolutePath (ProjectPreferencesUtil.getSystemTProperties (project).getSearchPath ()));
    }
    return projectSourceMap;

  }
  
  public String getCurrToken() {
    return currToken;
  }

  public void add(HierarchyNode node) {
    children.add(node);     
  }

  public HierarchyNode get(String nodeName)
  {
    Iterator<HierarchyNode> iterator1 = children.iterator();
    while (iterator1.hasNext()) {
      HierarchyNode elmt = iterator1.next();
      if(elmt.getText().equals(nodeName))
      {
        return elmt;
      }
    }
    return null;
  }

  public void setIfDependencyHierarchyRoot(boolean value) {
    this.isDependencyHierarchyRoot = value;
  }
  
  public boolean getIfDependencyHierarchyRoot() {
    return this.isDependencyHierarchyRoot;
  }

  /**
   * Called by Content provider for treeviewers in Dependency Hierarchy and Reference Hierarchy Views
   * Provides the top level nodes for the trees
   * @return
   */
  public HierarchyNode[] getChildren() {
    if (currToken != null) {
      String view_name = currToken;
      HierarchyNode node = new HierarchyNode (0, 0, view_name, Activator.ICON_VIEW, this.isDependencyHierarchyRoot,
        startProject.getName (), startModule, input.getFile ().getLocation ().toOSString (), this); //$NON-NLS-1$
      //If view name was selected from an output statement and that view was in a different file,
      //or module, make sure correct information is passed to the tree.
      if (forModularProject) {
        AQLElement rootElem = findModularAQLElement (startProject.getName (), startModule, view_name);
        if (rootElem == null) {
          AQLElement aliasElem = findModularAQLElementForAlias (startProject.getName(), startModule, view_name);
          if (aliasElem != null) {
            rootElem = findModularAQLElement (startProject.getName (), aliasElem.getFromModuleName (),
              aliasElem.getName ());
          }
        }
        if (rootElem != null) {
          String filePath = rootElem.getFilePath ();
          IProject project = ProjectUtils.getProjectFromFilePath (filePath);
          if (project == null) {
            project = startProject;
          } 
          String moduleName = rootElem.getModuleName ();
          view_name = rootElem.getName ();
          node = new HierarchyNode (0, 0, view_name, Activator.ICON_VIEW, this.isDependencyHierarchyRoot,
            project.getName (), moduleName, filePath, this);
        }
      }
      else {
        AQLElement rootElem = findNonModularAQLElement (startProject.getName (), view_name);
        if (rootElem != null) {
          String filePath = rootElem.getFilePath ();
          IProject project = ProjectUtils.getProjectFromFilePath (filePath);
          if (project == null) {
            project = startProject;
          }
          node = new HierarchyNode (0, 0, view_name, Activator.ICON_VIEW, this.isDependencyHierarchyRoot,
            project.getName (), "", filePath, this); //$NON-NLS-1$
        }
      }
      add (node);
      node.getChildren ();
      return this.children.toArray (new HierarchyNode[this.children.size ()]);
    } else {
      return this.children.toArray (new HierarchyNode[this.children.size ()]);
    }
  }


  /**
   * This method populates the dependency tree with all nodes that depend
   * on this node, For eg:- if the call is made on Phone, then PersonPhone
   * is populated in the tree
   */
  public void populateTreeWithDependentElementsForNode(HierarchyNode node) {
    List<AQLElement> children = null;
    if (forModularProject) {
      children = getDependentElementsForView(node.getProjectName (), node.getModuleName (), node.getText ());
    } else {
      children = getDependentElementsForView(node.getProjectName (), node.getText());
    }
    if (children != null) {
      for (AQLElement elem: children) {
        String projectName = ProjectUtils.getProjectFromFilePath (elem.getFilePath ()).getName ();
        String moduleName = elem.getModuleName () == null ? "":elem.getModuleName ();
        if (projectName == null) {
          projectName = node.getProjectName ();
        }
        
        HierarchyNode attr = new HierarchyNode(0,0,elem.getName (),Activator.ICON_VIEW,false,projectName,moduleName,elem.getFilePath (),this);
        node.addChild(attr);
      }
    }
  }

  /**
   * This method populates the dependency tree with all nodes that this
   * node depends on. For example when invoked on PersonPhone, it will
   * populate the tree with Person and Phone.
   * 
   * @param node
   */
  public void populateTreeWithRequiredElementsForNode(HierarchyNode node) {
    List<String> children = null;
    
      if (forModularProject) {
        AQLElement elem = findModularAQLElement(node.getProjectName (), node.getModuleName (), node.getText ());
        if (elem != null) {
          children = elem.getDependentElements ();
        }
      } else {
        AQLElement elem = findNonModularAQLElement(node.getProjectName (), node.getText ());
        if (elem != null) {
          children = elem.getDependentElements ();
        }
        
      }

    if (children != null) {
      for (int i = 0; i < children.size(); i++) {
        String dependee = children.get (i);
        String[] nameParts = ProjectUtils.splitQualifiedAQLElementName (dependee);
        String moduleName = "";
        String viewName = "";
        if (forModularProject && nameParts.length == 2) {
          moduleName = nameParts[0];
          viewName = nameParts[1];
        } else {
          viewName = dependee;
        }
        String filePath = ""; //$NON-NLS-1$
        String projectName = node.getProjectName ();
        
        if (forModularProject) {
          if (moduleName.isEmpty ()) {
            moduleName = node.getModuleName ();
          }
          AQLElement elementInLib = findModularAQLElement (node.getProjectName(),moduleName, viewName);
          if (elementInLib != null) {
            filePath = elementInLib.getFilePath ();
            projectName = ProjectUtils.getProjectFromFilePath (filePath).getName ();
          } else {
            //name of the child might be an alias for an imported view
            AQLElement importedViewElemWithAlias = findModularAQLElementForAlias (node.getProjectName (), moduleName, viewName);
            if (importedViewElemWithAlias != null) {
              //get import view element from aql library and set the actual module name and view name of the child
              moduleName = importedViewElemWithAlias.getFromModuleName ();
              viewName = importedViewElemWithAlias.getName ();
              elementInLib = findModularAQLElement (node.getProjectName (), moduleName, viewName); 
              if (elementInLib != null){
                filePath = elementInLib.getFilePath ();
                projectName = ProjectUtils.getProjectFromFilePath (filePath).getName ();
              }
            }
          }
        } else {
          AQLElement elementInLib = findNonModularAQLElement (node.getProjectName (), viewName);
          if (elementInLib != null) {
            filePath = elementInLib.getFilePath ();
          }
        }
        
        HierarchyNode attr = new HierarchyNode(0, 0, viewName,
          Activator.ICON_VIEW, true, projectName, moduleName, filePath, this);
        node.addChild(attr); 
      }
    }
  }
 
  /**
   * Finds aql elements for views dependent on given view. Meant for non-modular projects
   * @param projectName
   * @param elementName
   * @return List of AQLElement instances
   */
  private List<AQLElement> getDependentElementsForView(String projectName, String elementName) {
    ArrayList<AQLElement> dependents = new ArrayList<AQLElement>();
    IProject project = ProjectUtils.getProject (projectName);
    AQLProject nonModularProject = aqlLibrary.getLibraryMap ().get (project.getLocation ().toOSString ());
    if (nonModularProject != null) {
      for (AQLFile aqlf: nonModularProject.getAQLFiles ()) {
        for (AQLElement elem: aqlf.getAQLElements ()) {
          if (elem.getDependentElements () != null) {
            for (String child: elem.getDependentElements ()) {
              if (child.equals (elementName)) {
                dependents.add (elem);
              }
            }
          }
        }
      }
    }
    return dependents;
  }
  
  /**
   * Finds aql elements for views dependent on given view. Meant for modular projects
   * @param projectName
   * @param moduleName
   * @param elementName
   * @return List of AQLElement instances
   */
  private List<AQLElement> getDependentElementsForView(String projectName, String moduleName, String elementName) {
    ArrayList<AQLElement> dependents = new ArrayList<AQLElement>();
    IProject project = ProjectUtils.getProject (projectName);
    ArrayList<IProject> lookInProjects = new ArrayList<IProject>();
    lookInProjects.add(project);
    try {
      for (IProject proj: project.getReferencingProjects ()) {
        if (proj.hasNature (Constants.PLUGIN_NATURE_ID)) {
          lookInProjects.add (proj);
        }
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
    Map<String,AQLModuleProject> moduleProjectsMap = aqlLibrary.getModuleLibraryMap ();
    for (IProject proj: lookInProjects) {
      AQLModuleProject moduleProject = moduleProjectsMap.get (proj.getLocation ().toOSString ());
      if (moduleProject != null) {
        for (AQLModule mod: moduleProject.getAQLModules ().values ()) {
          Map<String,String> aliasToViewName = new LinkedHashMap<String,String>();
          for (AQLFile aqf: mod.getAQLFiles ()) {
            //1st pass, Creating a Map of import view elements with aliases for each module. key - alias, value - <modulename>.<viewname>
            List<AQLElement> elemList = aqf.getAQLElements ();
            if (elemList != null) { //aqf.getAQLElements() can be null, when aql file is empty or contains only comments
              for (AQLElement elem: elemList) {
                if (elem.getType ().equals (Constants.AQL_ELEMENT_TYPE_IMPORT_VIEW)) {
                  if (elem.getAliasName () != null) {
                    aliasToViewName.put (elem.getAliasName (), elem.getFromModuleName () + Constants.MODULE_ELEMENT_SEPARATOR + elem.getName ());
                  }
                }
              }
            }
          }
          for (AQLFile aqf: mod.getAQLFiles ()) {
            //2nd pass, Creating a list of view elements that require the view specified in parameters
            List<AQLElement> elemList = aqf.getAQLElements ();
            if (elemList != null) { //aqf.getAQLElements() can be null, when aql file is empty or contains only comments
              for (AQLElement elem : elemList) {
                if (elem.getDependentElements () != null) { //this term is incorrect. should be dependees or required elements
                  for (String child : elem.getDependentElements ()) {
                    if (!child.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))) {
                      if (aliasToViewName.containsKey (child)) { //child is an alias for an imported view
                        child = aliasToViewName.get (child); //assigning actual name
                      } else {
                        child = elem.getModuleName () + Constants.MODULE_ELEMENT_SEPARATOR + child; //child is a view defined in the same module
                      }
                    }
                    if (child.equals (moduleName + Constants.MODULE_ELEMENT_SEPARATOR + elementName)) {
                      dependents.add (elem);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return dependents;
  }

  /**
   * Gets absolute paths to aqls files in specified directories
   * @param searchPathList List of directories to look in
   * @return aql files' paths returned as a Set
   */
  private Set<String> getAllAQLFiles(List<String> searchPathList) {
    Iterator<String> iterator1 = searchPathList.iterator();
    final Set<String> aqlFilePathSet = new LinkedHashSet<String>();
    //All files that are there in the search path
    while(iterator1.hasNext())
    {
      String temp =iterator1.next();
      IPath path = new Path(temp).makeAbsolute(); 
      try {
        new FileTraversal() {
          public void onFile( final File f ) {
            if(f.getName().endsWith(Constants.AQL_FILE_EXTENSION))
            {
              aqlFilePathSet.add(f.getAbsolutePath().toString());
            }
          }
        }.traverse(new File(path.toOSString()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } 
    return aqlFilePathSet;
  }



  public void clear() {
    this.children.clear();
  }
  
  /**
   * Scours modular aql library and returns the AQLElement instance of a view or table matching the given project name,
   * module name and element name.
   * 
   * @param projectName
   * @param moduleName
   * @param elementName
   * @return
   */
  private AQLElement findModularAQLElement (String projectName, String moduleName, String elementName)
  {
    IAQLLibrary lib = Activator.getModularLibrary ();
    IProject project = ProjectUtils.getProject (projectName);
    List<IProject> lookInProjects = new ArrayList<IProject> ();
    lookInProjects.add (project);
    try {
      for (IProject proj : project.getReferencedProjects ()) {
        if (proj.isOpen () && proj.hasNature (Constants.PLUGIN_NATURE_ID)) {
          lookInProjects.add (proj);
        }
      }
    }
    catch (CoreException e) {
      e.printStackTrace ();
    }
    Map<String, AQLModuleProject> projectModuleMap = lib.getModuleLibraryMap ();
    for (IProject proj : lookInProjects) {
      AQLModuleProject aqlProj = projectModuleMap.get (proj.getLocation ().toOSString ());
      if (aqlProj == null) {
        continue;
      }
      else {
        for (AQLModule mod : aqlProj.getAQLModules ().values ()) {
          if (new Path (mod.getModuleName ()).lastSegment ().toString ().equals (moduleName)) {
            // member moduleName in AQLModule has the absolute path to the the module directory
            // Hence, it can't be used directly.
            for (AQLFile aqlf : mod.getAQLFiles ()) {
              List<AQLElement> elemList = aqlf.getAQLElements ();
              if (elemList != null) { // aqlf.getAQLElements() can be null, when aql file is empty or contains only
                                      // comments
                for (AQLElement elem : elemList) {
                  if (elem.getType ().equals (Constants.AQL_ELEMENT_TYPE_VIEW)
                    || elem.getType ().equals (Constants.AQL_ELEMENT_TYPE_TABLE)
                    || elem.getType ().equals (Constants.AQL_ELEMENT_TYPE_EXTERNAL_TABLE)
                    || elem.getType ().equals (Constants.AQL_ELEMENT_TYPE_EXTERNAL_VIEW)) { // only if element is a view
                                                                                            // or table declaration
                    if (elem.getUnQualifiedName ().equals (elementName)) { return elem; }
                  }
                }
              }
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Finds 'import view' or 'import table' aql element containing specified alias in specified module. Searches in
   * specified project and projects required by it.
   * 
   * @param projectName
   * @param moduleName
   * @param alias
   * @return
   */
  private AQLElement findModularAQLElementForAlias (String projectName, String moduleName, String alias)
  {
    IAQLLibrary lib = Activator.getModularLibrary ();
    IProject project = ProjectUtils.getProject (projectName);
    List<IProject> lookInProjects = new ArrayList<IProject> ();
    lookInProjects.add (project);
    try {
      for (IProject proj : project.getReferencedProjects ()) {
        if (proj.hasNature (Constants.PLUGIN_NATURE_ID)) {
          lookInProjects.add (proj);
        }
      }
    }
    catch (CoreException e) {
      e.printStackTrace ();
    }
    Map<String, AQLModuleProject> projectModuleMap = lib.getModuleLibraryMap ();
    for (IProject proj : lookInProjects) {
      AQLModuleProject aqlProj = projectModuleMap.get (proj.getLocation ().toOSString ());
      if (aqlProj == null) {
        continue;
      }
      else {
        for (AQLModule mod : aqlProj.getAQLModules ().values ()) {
          if (new Path (mod.getModuleName ()).lastSegment ().toString ().equals (moduleName)) {
            // member moduleName in AQLModule has the absolute path to the the module directory
            // Hence, it can't be used directly.
            for (AQLFile aqlf : mod.getAQLFiles ()) {
              List<AQLElement> elemList = aqlf.getAQLElements ();
              if (elemList != null) { // aqlf.getAQLElements() can be null, when aql file is empty or contains only
                                      // comments
                for (AQLElement elem : elemList) {
                  if (elem.getType ().equals (Constants.AQL_ELEMENT_TYPE_IMPORT_VIEW)
                    || elem.getType ().equals (Constants.AQL_ELEMENT_TYPE_IMPORT_TABLE)) { // only if element is from an
                                                                                           // import view or import
                                                                                           // table statement
                    if (elem.getAliasName () != null && elem.getAliasName ().equals (alias)) { return elem; }
                  }
                }
              }
            }
          }
        }
      }
    }
    return null;
  }
  
  /**
   * Scours aql library and returns the aql element for given project name and element name
   * @param projectName
   * @param elementName
   * @return
   */
  private AQLElement findNonModularAQLElement(String projectName, String elementName) {
    if (!forModularProject) {
      Map <String,AQLProject> projectMap = aqlLibrary.getLibraryMap ();
      IProject projectRes = ProjectUtils.getProject (projectName);
      AQLProject aqlProj = (projectRes == null)?null:projectMap.get (projectRes.getLocation ().toOSString ());
      if (aqlProj != null) {
        for (AQLFile file: aqlProj.getAQLFiles ()) {
          for (AQLElement elem: file.getAQLElements ()) {
            if (elem.getName ().equals (elementName)) {
              return elem;
            }
          }
        }
      }
    }
    return null;
  }
  
  /**
   * In aqllibrary, the current convention for hash keys for project objects 
   * is the project's absolute filepath (In ReconcilingStrategy).
   * This method generates such a key from the project name.
   * @param projectName
   * @return
   */
  private String getProjectKeyForAQLLibrary(String projectName) {
    IProject proj = ProjectUtils.getProject (projectName);
    if (proj == null) {
      return null;
    } else {
      return proj.getLocation ().toOSString ();
    }
  }
  
  /**
   * In aqllibrary, the current convention for hash keys for module objects 
   * is the moddule folder's absolute filepath (In ReconcilingStrategy).
   * This method generates such a key from a file path.
   * @param aqlFilePath
   * @return
   */
  private String getModuleKeyForAQLLibrary(String aqlFilePath) {
    IPath path = new Path(aqlFilePath);
    IFile file = FileBuffers.getWorkspaceFileAtLocation(path);
    String moduleName = (file == null) ? null : file.getParent ().getLocation ().toOSString ();
    return moduleName;
  }
}


