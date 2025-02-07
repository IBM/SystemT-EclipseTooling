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
package com.ibm.biginsights.textanalytics.indexer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.PreferenceStore;

import com.ibm.avatar.algebra.util.file.FileUtils;
import com.ibm.biginsights.textanalytics.indexer.Constants;
import com.ibm.biginsights.textanalytics.indexer.cache.ElementCache;
import com.ibm.biginsights.textanalytics.indexer.cache.FileCache;
import com.ibm.biginsights.textanalytics.indexer.cache.ModuleCache;
import com.ibm.biginsights.textanalytics.indexer.model.ElementDefinition;
import com.ibm.biginsights.textanalytics.indexer.model.ElementLocation;
import com.ibm.biginsights.textanalytics.indexer.model.ElementReference;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.Messages;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Indexer utilities
 * 
 *  Krishnamurthy, Rajeshwar Kalakuntla
 */
public class IndexerUtil
{


 
	private static final ILog logger = LogUtil.getLogForPlugin (Activator.PLUGIN_ID);


  /************** BEGIN: static data for calculating offset **********/
  /**
   * Maintains a map of filePath vs last modified timestamp. Used to check if we need to recalculate length of lines.
   * See {@link #lengthOfLinesMap}
   */
  private static Map<String, Long> lastModifiedTimeStampMap = new HashMap<String, Long> ();

  /**
   * Maintains a map of filePath vs length of each line (including the CR / LF characters). This is required to
   * calculate offset. We can not go by the length of OS dependent line separator because if a text file is transfered
   * from Linux to Windows in binary mode, then only LF characters are present, not CR. See
   * {@link #calculateOffset(String[], int, int)} for details.
   */
  private static Map<String, ArrayList<Integer>> lengthOfLinesMap = new HashMap<String, ArrayList<Integer>> ();

  /**
   * List of AQL reserved words. 
   */
  public static final String[] KEYWORDS = {"and", "all", "allow_empty", "allow empty_fileset", "always", "annotate", "as",
		    "ascending", "ascii", "attribute", "between", "blocks", "both", "by", "called", "case", "cast", "ccsid",
		    "character", "characters", "columns", "consolidate", "content_type", "count", "create", "default", "descending",
		    "detag", "detect", "deterministic", "dictionary", "dictionaries", "document", "element", "else", "entries", 
		    "exact", "export", "external", "external_name", "extract", "false", "fetch", "file", "first", "flags", "folding",
		    "from", "function", "group", "having", "import", "in", "include", "inline_match", "input", "into", "insensitive", 
		    "java", "language", "left", "lemma_match", "like", "limit", "matchingRegex", "mapping", "minus", "module", "name",
		    "never", "not", "null", "on", "only", "order", "output", "part_of_speech", "parts_of_speech", "parameter", "pattern", 
		    "point", "points", "priority", "regex", "regexes", "require", "retain", "return", "right", "rows", "select", 
		    "separation", "specific", "split", "table", "tagger", "then", "token", "tokens", "Token", "true", "up", "unicode",
		    "union", "using", "values", "view", "views", "when", "where", "with" };
  
  
  /************** END: static data for calculating offset **********/
  /**
   * Calculates the offset of a given token from the beginning of the file. Handles the differences between EOL
   * character on Windows Vs Unix.
   * 
   * @param fContents The content of the file
   * @param beginLine begin line of token
   * @param beginColumn begin column of token (w.r.t to the beginLine)
   * @return offset from begin of the file
   */
  public static final int calculateOffset (IFile file, int beginLine, int beginColumn)
  {
    int charCount = 0;
    ArrayList<Integer> lengthOfLines = getLengthOfLines (file);
    for (int i = 0; i < beginLine - 1; i++) {
      charCount += lengthOfLines.get (i);
    }
    // add beginColumn to the current count
    charCount += beginColumn;
    return charCount - 1;// subtract 1 because offset begins from 0
  }
  
  /**
   * This method is to check given string is valid AQL reserved word or not
   * @param name to be validated
   * @return true if the given name is keyword else returns false 
   */
  public static boolean isAQLKeyword (String name)
  {
    List<String> keyWordList = Arrays.asList (KEYWORDS);
    if (keyWordList.contains (name)) { return true; }
    return false;
  }
  
  /**
   * Retrieves the array list containing length of each line from the internal cache( i.e lengthOfLinesMap).
   * Re-calculates the length if the file was modified since last calculation (or) if the length of lines were not
   * calculated yet.
   * 
   * @param file The file whose line lengths are to be calculated
   * @return List of integers representing the length of each line (including end of line character).
   */
  private static ArrayList<Integer> getLengthOfLines (IFile file)
  {
    Long prevTimeStamp = lastModifiedTimeStampMap.get (file.getFullPath ().toString ());
    Long newTimeStamp = file.getModificationStamp ();
    if (prevTimeStamp == null || (prevTimeStamp < newTimeStamp)) {
      calculateLengthOfLines (file);
      lastModifiedTimeStampMap.put (file.getFullPath ().toString (), newTimeStamp);
    }
    return lengthOfLinesMap.get (file.getFullPath ().toString ());
  }
  
  /**
   * Calculates the length of each line in the given file. Updates lengthOfLinesMap with the calculated data. The length
   * includes end of line character(s) as well. This method counts byte-by-byte instead of calculating length of line
   * and adding Operating system specific line separator's length, because linux type file opened in windows might still
   * have LF as EOL marker instead of CR+LF. So, it is not possible to rely on operating system EOL marker.
   * 
   * @param ifile
   */
  private static ArrayList<Integer> calculateLengthOfLines (File file){
    FileInputStream fis = null;
    ArrayList<Integer> lengthOfLines = new ArrayList<Integer> ();

    try {
      fis = new FileInputStream (file);
      // In order to keep the offset values in sync with the way Eclipse editor calculates offsets
      // InputStreamReader.read() is used to the reads 'characters' rather than single bytes 
      // i.e it reads bytes from the underlying stream and decodes them into characters using a specified character set.
      InputStreamReader reader = new InputStreamReader(fis, "UTF8");
      int c;
      int lineLength = 0;
      while ((c = reader.read ()) != -1) {

        // Linux: \n is end of line
        // Windows: \r\n is end of line, but \n is the last character.
        // So, checking for \n will work on both these
        // platforms
        if (c == '\n') {
          lineLength++;
          lengthOfLines.add (lineLength);

          // reset it for next line
          lineLength = 0;
          continue;
        }
        else {
          lineLength++;
        }
      }// end: while
      reader.close ();
      // add last line's length -- Note: last night does not have a \n at the end and hence the while loop above does
      // not add it's length to lengthOfLines list
      lengthOfLines.add (lineLength);
      
    }
    catch (Exception e) {
      e.printStackTrace ();
      // TODO: report error
    }
    finally {
      if (fis != null) {
        try {
          fis.close ();
        }
        catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace ();
        }
      }
    }
    return lengthOfLines;
  }

  /**
   * Calculates the length of each line in the given file. Updates lengthOfLinesMap with the calculated data. The length
   * includes end of line character(s) as well. This method counts byte-by-byte instead of calculating length of line
   * and adding Operating system specific line separator's length, because linux type file opened in windows might still
   * have LF as EOL marker instead of CR+LF. So, it is not possible to rely on operating system EOL marker.
   * 
   * @param ifile
   */
  private static void calculateLengthOfLines (IFile ifile)
  {

    File file = ifile.getLocation ().toFile ();
    ArrayList<Integer> lengthOfLines = calculateLengthOfLines (file);
    lengthOfLinesMap.put (ifile.getFullPath ().toString (), lengthOfLines);

  }

  /**
   * Detects the type of element found at the given offset of the given file
   * 
   * @param file IFile instance of the file where the element name is found
   * @param offset Offset from beginning of the file. Offsets start from 0.
   * @return One of the element types defined in {@link ElementType} or null, if type could not be determined.
   */
  public static ElementType detectElementType (IFile file, int offset)
  {
    ElementType ret = ElementType.UNKNOWN;

    // Check if an AQL element reference exists and determine the type
    ElementCache elemCache = ElementCache.getInstance ();
    ret = elemCache.getElementType (file, offset);

    // if no element reference exists, then check for module reference
    if (ret == ElementType.UNKNOWN) {
      boolean isModuleRef = ModuleCache.getInstance ().isModuleReference (file, offset);
      if (isModuleRef == true) {
        ret = ElementType.MODULE;
      }
    }

    return ret;
  }
  
  /**
   * Identifies the element at a specified location and returns information on the element's definition.
   * 
   * @param file IFile instance of the file where the element name is found
   * @param offset Offset from beginning of the file. Offsets start from 0.
   * @return ElementDefintion object for referenced element at specified location. Null if no definition is found.
   */
  public static ElementDefinition getDefForElemRefAtLocation (IFile file, int offset)
  {
    ElementCache elemCache = ElementCache.getInstance ();
    ElementReference ref = getElementReferenceAtLocation (file, offset);

    if (ref != null) {
      Integer elementId = ref.getElementId ();
      ElementDefinition def = elemCache.getElementDefinition (elementId);
      return def;
    }
    else {
      return null;
    }

  }
  
  /**
   * Get ElementDefinition instance from element cache, for given element
   * @param projectName
   * @param moduleName
   * @param type
   * @param elementName
   * @return ElementDefinition for given element if it is found, else null
   */
  public static ElementDefinition getElementDefinition (String projectName, String moduleName, ElementType type, String elementName) {
    ElementDefinition def = null;
    ElementCache elemCache = ElementCache.getInstance ();   
    Integer id = elemCache.lookupElement (projectName, moduleName, type, elementName);
    if (id != null) {
      def = elemCache.getElementDefinition (id);     
    }
    return def;  
  }
  

  /**
   * Gets the preference store for mics.idx
   * 
   * @return
   */
  public static PreferenceStore getInitailsIndexStore ()
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace ().getRoot ();
    String workspacePath = root.getLocation ().makeAbsolute ().toOSString ();
    File directory = new File (workspacePath + Constants.INDEX_PATH);
    if (!directory.exists ()) {
      directory.mkdir ();
    }

    File fileMasterConfigFile = new File (directory, Constants.INDEXING_STATUS_FILE);
    PreferenceStore prefStore = null;

    try {
      if (!fileMasterConfigFile.exists ()) fileMasterConfigFile.createNewFile ();

      prefStore = new PreferenceStore (fileMasterConfigFile.getAbsolutePath ());

      prefStore.load ();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }

    return prefStore;

  }
  
  /**
   * Get the current token from the file based on offset passed.
   * 
   * @param currentFile
   * @param cursorOffset
   * @return
   */
  public static String getCurrentToken (IFile currentFile, int cursorOffset)
  {
    String curr = ""; //$NON-NLS-1$
    ElementReference elRef = getElementReferenceAtLocation (currentFile, cursorOffset);
    if (elRef != null) {
      curr = ElementCache.getInstance ().getElementNameInAQL (elRef.getElementId ());
    }

    return curr;
  }
  
  public static ElementReference getElementReferenceAtLocation(IFile currentFile, int cursorOffset) {
    ElementReference elem = null;
    try {
      String elementName = "";  //$NON-NLS-1$
      
      // Step 1: Get all the element references in a file.
      List<ElementReference> elementReferences = ElementCache.getInstance ().getElementReferencesInFile (currentFile);
      //Step 2: Sort the element references based on offset.
      Collections.sort (elementReferences, new ElementDefinitionSortByOffset());
      
      // Step 3: Iterate thru the element references
      for (ElementReference elementReference : elementReferences) {
        // Step 4: Get the element name. 
        String elemNameKey = ElementCache.getInstance ().getElementName (elementReference.getElementId ());
        if (elemNameKey != null) {
          int idxBeforeElemName = elemNameKey.lastIndexOf (com.ibm.biginsights.textanalytics.indexer.Constants.QUALIFIED_NAME_SEPARATOR);
          elementName = elemNameKey.substring (idxBeforeElemName + 1);
        }

        // Step 5: Calculate the start and end offset 
        int startOffset = elementReference.getLocation ().getOffset ();
        int endOffset = startOffset + elementName.length ();
   
        // Step 6: Check the cursor offset is between start and end offset.
        if( startOffset <= cursorOffset && cursorOffset <= endOffset ){
          // Step 7: Get the element name that matches teh offset
          elem = elementReference;
          break;
        }
      }
      
      return elem;
    }
    catch (Exception e) {
      logger.logError (e.getMessage ());
      return null;
    }
  }
  
  /**
   * Calculate the offset for the source reference in the .textanalytics file.
   * 
   * @param folder
   * @return int offset
   */
  public static int getSrcOffset (IFolder folder)
  {
    // Step 1: Get the Project and textAnalyticsFile for the folder.
    IProject project = folder.getProject ();
    File textAnalyticsFile = ProjectUtils.getPreferenceStoreFile (project);
    
    String lines[] = null;
    try {
      // Step 2: Read the .textAnalytics file line by line and store it in String array.
      lines = FileUtils.read_lines (textAnalyticsFile);
    }
    catch (IOException e) {
      logger.logError (e.getMessage ());
    }

    // Step 3: If the lines read is null, return -1. 
    if (lines == null) return -1;
    
    // Step 4: Create a pattern of the line that we are interested. In our case, it will be
    // srcTextAnalyticsPath=[P]textAnalytics/src
    String lineStr = com.ibm.biginsights.textanalytics.util.common.Constants.MODULE_SRC_PATH + "="
      + ProjectPreferencesUtil.getSystemTProperties (project).getModuleSrcPath ();

    int srcLine = -1;
    // Step 5: Get the line number that have the property - srcTextAnalyticsPath=[P]textAnalytics/src 
    for (int i = 0; i < lines.length; i++) {
      if (lines[i] != null && lines[i].startsWith (lineStr)) {
        srcLine = i;
        break;
      }
    }
    
    // Step 6: Get the offset of last separator. 
    // For a property - srcTextAnalyticsPath=[P]textAnalytics/src, the offset should be
    // calculated for srcTextAnalyticsPath=[P]textAnalytics/. 
    int lastSeperatorOffset = lines[srcLine].lastIndexOf ("/") + 1;
    
    // Step 7: Get the line length by calling calculateLengthOfLines. The length of a line vary with Linux and Windows
    ArrayList<Integer> linesList = calculateLengthOfLines (textAnalyticsFile);
        
    int offset = 0;

    // Step 8: Add the line length to the offset.
    for (int i = 0; i < srcLine; i++) {
       offset += linesList.get (i);
    }
    
    // Step 9: Add the lastSeperatorOffset to offset. 
    offset += lastSeperatorOffset;

    return offset;
  }

  /**
   * Calculate the offset for the tam reference in the .textanalytics file.
   * 
   * @param folder
   * @return in offset of bin folder
   */
  public static int getTamOffset (IFolder folder)
  {
    // Step 1: Get the Project and textAnalyticsFile for the folder.
    IProject project = folder.getProject ();
    File textAnalyticsFile = ProjectUtils.getPreferenceStoreFile (project);
    String lines[] = null;
    try {
      // Step 2: Read the .textAnalytics file line by line and store it in String array.
      lines = FileUtils.read_lines (textAnalyticsFile);
    }
    catch (IOException e) {
      logger.logError (e.getMessage ());
    }

    // Step 3: If the lines read is null, return -1. 
    if (lines == null) return -1;
    
    // Step 4: Create a pattern of the line that we are interested. In our case, it will be
    // tamTextAnalyticsPath=[P]textAnalytics/bin
    String lineStr = com.ibm.biginsights.textanalytics.util.common.Constants.MODULE_BIN_PATH + "="
      + ProjectPreferencesUtil.getSystemTProperties (project).getModuleBinPath ();

    int srcLine = -1;
    // Step 5: Get the line number that have the property - tamTextAnalyticsPath=[P]textAnalytics/bin
    for (int i = 0; i < lines.length; i++) {
      if (lines[i] != null && lines[i].startsWith (lineStr)) {
        srcLine = i;
        break;
      }
    }
    
    // Step 6: Get the offset of last separator. 
    // For a property - tamTextAnalyticsPath=[P]textAnalytics/bin, the offset should be
    // calculated for tamTextAnalyticsPath=[P]textAnalytics/. 
    int lastSeperatorOffset = lines[srcLine].lastIndexOf ("/") + 1;
    int offset = 0;

    // Step 7: Get the line length by calling calculateLengthOfLines. The length of a line vary with Linux and Windows
    ArrayList<Integer> linesList = calculateLengthOfLines (textAnalyticsFile);

    // Step 8: Add the line length to the offset.
    for (int i = 0; i < srcLine; i++) {
      offset += linesList.get (i);
    }
    
    // Step 9: Add the lastSeperatorOffset to offset.
    offset += lastSeperatorOffset;

    return offset;
  }

  /**
   * Calculate the offset for the bin references in the .textanalytics 
   * files of other projects, basically the properties module.TAMPath in text analytics 
   * property files.
   * 
   * @param project project of the currently modified folder
   * @return Offset Map  returns the map of file references with their offsets
   */
  public static Map<IFile,Integer> getBinOffSetInPreCompiledTamPropertyOfOtherProjects(IProject project) {
    Map<IFile,Integer> propertyFileToPropertyValueOffsetMap = new LinkedHashMap<IFile,Integer>();

    //Construct a representation of this project's bin directory in other projects' precompile tam path list.
    String moduleBinPath = ProjectUtils.getConfiguredModuleBinPath (project);
    if (moduleBinPath == null) {
      logger.logError (MessageFormat.format (Messages.getString ("ProjectUtils.MODULE_BIN_PATH_DETERMINATION_ERROR"),
        new Object[]{project.getName ()}));
      return propertyFileToPropertyValueOffsetMap;
    }
    String pathToProjectTams = com.ibm.biginsights.textanalytics.util.common.Constants.WORKSPACE_RESOURCE_PREFIX + moduleBinPath;
    String searchValue = pathToProjectTams.substring (0, pathToProjectTams.lastIndexOf ("/")+1); //e.g. [W]/projectA/textAnalytics/  //$NON-NLS-1$

    IProject[] projects = ResourcesPlugin.getWorkspace ().getRoot ().getProjects ();
    for (IProject proj : projects) {
      try {
        if (proj.hasNature (com.ibm.biginsights.textanalytics.util.common.Constants.PLUGIN_NATURE_ID) && ProjectUtils.isModularProject (proj)) {
          File textAnalyticsFile = ProjectUtils.getPreferenceStoreFile (proj);
          String lines[] = null;
          try {
            //Read the .textAnalytics file line by line and store it in String array.
            lines = FileUtils.read_lines (textAnalyticsFile);
          }
          catch (IOException e) {
            logger.logError (e.getMessage ());
          }
 
          //If the lines read is null, return -1. 
          if (lines == null) continue;

          //Create a pattern of the line that we are interested. In our case, it will be
          //module.TAMPath=[W]/projectA/textAnalytics/bin;[W]/projectC/textAnalytics/bin
          String lineStr = com.ibm.biginsights.textanalytics.util.common.Constants.MODULE_TAMPATH + "=" //$NON-NLS-1$
            + ProjectPreferencesUtil.getSystemTProperties (proj).getTamPath ();

          int precompiledTamPropertyLineNum = -1;
          //Get the line number that has the property - module.TAMPath=[W]/projectA/textAnalytics/bin;[W]/projectC/textAnalytics/bin
          for (int i = 0; i < lines.length; i++) {
            if (lines[i] != null && lines[i].startsWith (lineStr)) {
              precompiledTamPropertyLineNum = i;
              break;
            }
          }
          if (precompiledTamPropertyLineNum > -1 && lines[precompiledTamPropertyLineNum].contains (searchValue)) {
            int binPathParentOffset = lines[precompiledTamPropertyLineNum].lastIndexOf (searchValue) + searchValue.length ();
            int offset = 0;

            // Step 7: Get the line length by calling calculateLengthOfLines. The length of a line vary with Linux and Windows
            ArrayList<Integer> linesList = calculateLengthOfLines (textAnalyticsFile);

            // Step 8: Add the line length to the offset.
            for (int i = 0; i < precompiledTamPropertyLineNum; i++) {
              offset += linesList.get (i);
            }

            // Step 9: Add the binPathParentOffset  
            offset += binPathParentOffset;

            IPath taFilePath = Path.fromOSString((textAnalyticsFile.getAbsolutePath()).toString());
            IFile refFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(taFilePath);
            if (refFile != null) {
              propertyFileToPropertyValueOffsetMap.put (refFile, offset);
            }
          }
        }
      } catch (CoreException e) {
        //unable to determine project nature.
        logger.logError (e.getMessage ());
      }
    }
    return propertyFileToPropertyValueOffsetMap;
  }
  
  /**
   * Checks if token at the given offset is the element name in a definition (create/alias in import) statement and 
   * matches the specified element.
   * @param elementType presumed type of the element
   * @param projectName name of the project supposed to be containing the element
   * @param moduleName name of the module supposed to be containing the element
   * @param elementName name of the element
   * @param srcFile IFile instance of file containing the element
   * @param offset cursor offset.
   * @return true if there is an element definition at given offset and if it is for the specified element.
   */
  public static boolean isElementDefinition (ElementType elementType, String projectName, String moduleName, String elementName, IFile srcFile, int offset) {
    ElementDefinition def = getElementDefinition (projectName, moduleName, elementType, elementName);
    if (def != null) {
      ElementLocation loc = def.getLocation ();
      if (loc != null) {
        IFile defFile = FileCache.getInstance ().getFile (loc.getFileId ());
        if (defFile != null && defFile.equals (srcFile) &&def.getLocation ().getOffset () == offset) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

}
  

/**
 * Sort the ElementReferences based on the offset location in a file.
 * 
 *  Simon
 *
 */
class ElementDefinitionSortByOffset implements Comparator<ElementReference> {

  public int compare(ElementReference elementRef1, ElementReference elementRef2) {
    int value = 0;
    if (elementRef1.getLocation ().getOffset () > elementRef2.getLocation ().getOffset ())
      value = 1;
    else if (elementRef1.getLocation ().getOffset () < elementRef2.getLocation ().getOffset ())
      value = -1;
    else if (elementRef1.getLocation ().getOffset () == elementRef2.getLocation ().getOffset ())
      value = 0;

    return value;
  }
}
