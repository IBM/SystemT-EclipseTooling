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

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.avatar.api.Constants;
import com.ibm.biginsights.textanalytics.indexer.cache.ElementCache;
import com.ibm.biginsights.textanalytics.indexer.cache.FileCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ModuleCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ProjectCache;
import com.ibm.biginsights.textanalytics.indexer.exceptions.ElementNotFoundException;
import com.ibm.biginsights.textanalytics.indexer.exceptions.ModuleNotFoundException;
import com.ibm.biginsights.textanalytics.indexer.exceptions.MultipleProjectMatchesException;
import com.ibm.biginsights.textanalytics.indexer.exceptions.ProjectDeterminationException;
import com.ibm.biginsights.textanalytics.indexer.exceptions.ProjectNotFoundException;
import com.ibm.biginsights.textanalytics.indexer.index.IDManager;
import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.model.ElementReference;
import com.ibm.biginsights.textanalytics.indexer.model.FileReference;
import com.ibm.biginsights.textanalytics.indexer.model.ModuleReference;
import com.ibm.biginsights.textanalytics.indexer.model.ProjectReference;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 * Interface for reader classes that read a file containing definitions and/or references to AQL elements. For list of
 * AQL element types, refer to {@link ElementType}
 * 
 *  Krishnamurthy
 */
public abstract class FileIndexer
{
 
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
   
  protected ElementCache elemCache = ElementCache.getInstance ();
  protected ProjectCache projectCache = ProjectCache.getInstance ();
  protected ModuleCache moduleCache = ModuleCache.getInstance ();
  protected FileCache fileCache = FileCache.getInstance ();

  /**
   * AQL file to index
   */
  protected IFile fileToIndex;

  /**
   * IDManager index
   */
  protected IDManager idManager;

  /**
   * Each entry in the array holds a line from fileToIndex
   */
  protected String[] fileContents;

  /**
   * Project name of the current file being indexed
   */
  protected String projectName;

  /**
   * Default constructor
   */
  protected FileIndexer ()
  {
    idManager = IDManager.getInstance ();
  }

  /**
   * Reads the input file and creates an index for the given file as well as its contents
   * 
   * @param fileToRead
   */
  public void indexFileContents (IFile fileToIndex) throws Exception
  {
    this.fileToIndex = fileToIndex;
    if(fileToIndex == null || !fileToIndex.exists ())
      return;
    
    File file = fileToIndex.getLocation ().toFile ();
    this.fileContents = FileUtils.read_lines (file);

    // invoke initialize() of subclass after all possible attributes of base class are filled in
    initialize ();

    // perform indexing
    createFileIndex ();
    parseAndIndex ();
  }

  /**
   * Initializes Indexer's state. Subclasses can override this method to initialize their state.
   */
  protected void initialize ()
  {
    this.projectName = fileToIndex.getProject ().getName ();
  }

  /**
   * Parses the given file and adds an entry to the appropriate index for each AQL element definition or reference
   * encountered while parsing
   */
  protected abstract void parseAndIndex () throws Exception;

  /**
   * Creates an index for the file. i.e adds the file to FileCache
   */
  protected void createFileIndex () throws Exception
  {
    fileCache.addFile (fileToIndex);
  }

  /**
   * Adds a module reference to ModuleCache
   * 
   * @param project Project that the referenced module belongs to
   * @param module name of the module being referenced
   * @param file File containing the module reference. Could be AQL, Extraction plan or Launch Config file
   * @param beginLine beginLine of module reference
   * @param endLine end line of module reference
   * @param beginColumn begin column of module reference
   * @param endColumn end column of module reference
   * @return ModuleReference object
   */
  protected ModuleReference addModuleReference (String project, String module, IFile file, int beginLine,
    int beginColumn)
  {

    Integer moduleId = moduleCache.getModuleId (project, module);
    Integer fileId = fileCache.getFileId (file);

    int offset = calculateOffset (beginLine, beginColumn);
    ElementLocation location = new ElementLocation (fileId, offset);

    Integer moduleRefId = IDManager.getInstance ().generateNextSequenceId ();
    ModuleReference moduleRef = new ModuleReference (moduleRefId, moduleId, location);

    moduleCache.addModuleReference (moduleRef);

    return moduleRef;
  }

  /**
   * Adds a project reference to ProjectCache
   * 
   * @param project project referenced by a file
   * @param file File where project is referenced
   * @param beginLine beginLine of project reference
   * @param beginColumn beginColumn of project reference
   * @return ProjectReference object
   */
  protected ProjectReference addProjectReference (String project, IFile file, int beginLine, int beginColumn)
  {
    Integer projectId = projectCache.getProjectId (project);
    Integer fileId = fileCache.getFileId (file);

    int offset = calculateOffset (beginLine, beginColumn);
    ElementLocation location = new ElementLocation (fileId, offset);

    Integer projectRefId = idManager.generateNextSequenceId ();
    ProjectReference projRef = new ProjectReference (projectRefId, projectId, location);

    projectCache.addProjectReference (projRef);

    return projRef;
  }

  /**
   * Calculates the offset of a given token from the beginning of the file. Handles the differences between EOL
   * character on Windows Vs Unix. <br/>
   * The data passed to this method is returned by AQL parser which treats tab to be of 8 character length. So, this
   * method should determine how many tabs are there before the given beginColumn and adjust the offset to suit the
   * offset value returned by Eclipse editor.
   * 
   * @param beginLine begin line of token
   * @param beginColumn begin column of token (w.r.t to the beginLine)
   * @return offset from begin of the file
   */
  protected final int calculateOffset (int beginLine, int beginColumn)
  {
    int offset = IndexerUtil.calculateOffset (fileToIndex, beginLine, beginColumn);

    // // if AQL file, check for tabs in AQL statements
    // if (com.ibm.biginsights.textanalytics.util.common.Constants.AQL_FILE_EXTENSION_STRING.equals
    // (fileToIndex.getFileExtension ())) {
    //
    // // Count of tabs before the beginColumn
    // int tabCount = 0;
    // int actualCharsRead = 0;
    //
    // String line = fileContents[beginLine - 1];
    // for (int idx = 0; idx < line.length (); ++idx) {
    // char c = line.charAt (idx);
    // if (c == '\t') {
    // tabCount ++;
    // actualCharsRead += 8;
    // }else{
    // actualCharsRead++;
    // }
    //
    // if(actualCharsRead >= beginColumn){
    // break;
    // }
    // }
    //
    // return offset - (tabCount * 8);
    // }

    return offset;

  }

  /**
   * Adds an AQL element reference to ElementCache
   * 
   * @param project Project containing the element reference
   * @param module name of the module containing the element reference
   * @param file File containing the element reference. Could be AQL, Extraction plan or Launch Config file
   * @param elementType Type of the element
   * @param elementName Name of the element
   * @param beginLine beginLine of element reference
   * @param endLine end line of element reference
   * @param beginColumn begin column of element reference
   * @param endColumn end column of element reference
   * @return ElementReference object
   */
  protected ElementReference addElementReference (String project, String module, IFile file, ElementType elementType,
    String elementName, int beginLine, int beginColumn)
  {
    Integer elemId = elemCache.getElementId (project, module, elementType, elementName);
    Integer fileId = fileCache.getFileId (file);
    int offset = calculateOffset (beginLine, beginColumn);
    ElementLocation location = new ElementLocation (fileId, offset);

    Integer elemRefId = IDManager.getInstance ().generateNextSequenceId ();
    ElementReference elemeRef = new ElementReference (elemRefId, elemId, location);

    elemCache.addElementReference (elemeRef, fileId);

    return elemeRef;
  }

  /**
   * Adds an AQL file reference to FileCache
   * 
   * @param project Project containing the aql file reference
   * @param module name of the module containing the aql file reference
   * @param file File containing the file reference. Could be AQL, Extraction plan or Launch Config file
   * @param aqlFileName The name of referenced AQL file
   * @param beginLine beginLine of project reference
   * @param beginColumn beginColumn of project reference
   * @return FileReference object
   */
  protected FileReference addAQLFileReference (String project, String module, IFile file, String aqlFileName,
    int beginLine, int beginColumn)
  {
    Integer aqlFileId = fileCache.getAQLFileId (project, module, aqlFileName);
    Integer refcingFileId = fileCache.getFileId (file);

    if (aqlFileId != null && refcingFileId != null) {
      int offset = calculateOffset (beginLine, beginColumn);
      ElementLocation location = new ElementLocation (aqlFileId, offset);

      Integer elemRefId = IDManager.getInstance ().generateNextSequenceId ();
      FileReference fileRef = new FileReference (elemRefId, refcingFileId, location);

      fileCache.addFileReference (fileRef);

      return fileRef;
    }

    return null;

  }

  /**
   * Splits the qualified name into two parts: modulePrefix and elementName
   * 
   * @param qualifiedName fully qualified name of the element
   * @param currModule module name of the current file being indexed. Used to fill in the return array, if the
   *          qualifiedName does not contain a module prefix
   * @return a String[] of length 2. index 0 contains module name, index 1 contains element name.
   */
  protected String[] splitQualifiedName (String qualifiedName, String currModule)
  {
    String[] ret = new String[] { "", "" };
    if (qualifiedName.contains (String.valueOf (Constants.MODULE_ELEMENT_SEPARATOR))) {
      int lastIndexOfDot = qualifiedName.lastIndexOf (Constants.MODULE_ELEMENT_SEPARATOR);
      String moduleName = qualifiedName.substring (0, lastIndexOfDot);
      String elementName = qualifiedName.substring (lastIndexOfDot + 1);
      ret[0] = moduleName;
      ret[1] = elementName;
    }
    else {
      // the element name beglongs to current module. Fill ret[0] with currModuleName
      ret[0] = currModule;
      ret[1] = qualifiedName;
    }
    return ret;
  }

  /**
   * Identifies the project name of a given element. Uses current project's build path (i.e dependent projects) to
   * resolve conflicts. Attempts to locate the element with current project, if not resolved, it attempts to resolve
   * from dependent projects.
   * 
   * @param moduleName
   * @param elementType
   * @param elementName
   * @return
   */
  protected String detectProject (String moduleName, ElementType type, String elementName) throws ProjectDeterminationException
  {
    String projectToTry = detectProject (moduleName);
    Integer elementId = elemCache.lookupElement (projectToTry, moduleName, type, elementName);

    // if elementId is null, then there is no element with this projectName
    if (elementId == null) { throw new ElementNotFoundException (String.format (
      "Cannot detect project name for element with attributes: moduleName: %s, elementType: %s, elementName: %s",
      moduleName, type.toString (), elementName)); }

    return projectToTry;
  }

  /**
   * Determines which project this module belongs to. Checks current project first. If more than one project found then
   * it throws an error because it indicates a problem with project dependency.
   * 
   * @param moduleName
   * @return
   */
  protected String detectProject (String moduleName) throws ProjectDeterminationException
  {
    ArrayList<String> potentialMatches = new ArrayList<String> ();

    IProject project = ProjectUtils.getProject (this.projectName);

    if (project != null) {

      // check if the current project contains the requested module name
      if (true == ProjectUtils.isValidModule (project, moduleName)) {
        potentialMatches.add (project.getName ());
      }

      // check for existence of the module in all referenced projects
      try {
        // get all projects that the current project references
        IProject[] refProjects = project.getReferencedProjects ();

        // if a module with given moduleName exists in the project, then add to return list
        for (IProject refProj : refProjects) {
          if (ProjectUtils.isValidModule (refProj, moduleName)) {
            potentialMatches.add (refProj.getName ());
          }
        }
      }
      catch (CoreException e) {
        throw new ProjectDeterminationException (e);
      }

      // validate potentialMatches and return
      if (potentialMatches.size () > 1) {
        throw new MultipleProjectMatchesException (String.format (
          "More than one project match found for module %s. This indicates a problem with project dependency",
          moduleName));
      }
      else if (potentialMatches.size () == 1) {
        return potentialMatches.get (0);
      }
      // (potentialMatches.size () == 0)
      else {
        throw new ModuleNotFoundException (String.format (
          "The module %s is not found anywhere in the project %s and its build path.", moduleName, project.getName ()));
      }
    }
    // current project is not found. Throw an error
    else {
      throw new ProjectNotFoundException (String.format ("The project %s is not found", this.projectName));
    }

  }
}
