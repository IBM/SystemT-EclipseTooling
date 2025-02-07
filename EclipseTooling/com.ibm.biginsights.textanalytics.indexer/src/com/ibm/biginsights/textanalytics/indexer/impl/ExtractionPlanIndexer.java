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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class ExtractionPlanIndexer extends FileIndexer
{


 
	public static final String epFileName = ".extractionplan";

  private static final String aqlfilepath_Tag = "aqlfilepath";
  private static final String comment_Tag = "comment";
  private static final String debug_Tag = "debug";
  private static final String export_Tag = "export";
  private static final String fileName_Tag = "fileName";
  private static final String moduleName_Tag = "moduleName";
  private static final String output_Tag = "output";
  private static final String viewname_Tag = "viewname";

  private static final String projectName_Tag = "name";

  @Override
  protected void parseAndIndex ()
  {
    if (!shouldIndex (fileToIndex)) return;

    for (int i = 0; i < fileContents.length; ++i) {
      if (possibleViewInfo (i)) {
        createIndex (new ViewInfo (i));
        i += 7;
      }
      if (fileContents[i].contains (getOpenTag (projectName_Tag))) {
        createProjectIndex (i);
      }
    }
  }

  private boolean possibleViewInfo (int loc)
  {
    return (loc + 8 < fileContents.length && fileContents[loc].contains (aqlfilepath_Tag)
      && fileContents[loc + 1].contains (comment_Tag) && fileContents[loc + 2].contains (debug_Tag)
      && fileContents[loc + 3].contains (export_Tag) && fileContents[loc + 4].contains (fileName_Tag)
      && fileContents[loc + 5].contains (moduleName_Tag) && fileContents[loc + 6].contains (output_Tag) && fileContents[loc + 7].contains (viewname_Tag));
  }

  /**
   * This is a sample XML part of a view in extraction plan:
   * <aqlfilepath>C:\BigData\runtime-systemTworkflow-modular\Phone_1
   * .5\textAnalytics\src\label3_CandidateGeneration\newcg.aql</aqlfilepath> <comment></comment> <debug>false</debug>
   * <export>false</export> <fileName>newcg.aql</fileName> <moduleName>label3_CandidateGeneration</moduleName>
   * <output>false</output> <viewname>cgviewtest</viewname> There are 5 references: view name, module in <moduleName>
   * and in <aqlfilepath>, aql file in <fileName> and in <aqlfilepath>. Their locations are viewLoc, moduleLoc1,
   * moduleLoc2, aqlFileLoc1, aqlFileLoc2, respectively.
   */
  private void createIndex (ViewInfo viewInfo)
  {
    // String project = iProject.getName ();
    String moduleName = viewInfo.getModuleName ();
    String aqlFileName = viewInfo.getFileName ();
    String viewName = viewInfo.getViewName ();

    int[] moduleLoc1 = viewInfo.getModuleLocation1 ();
    int[] aqlFileLoc1 = viewInfo.getFileLocation1 ();
    int[] viewLoc = viewInfo.getViewLocation ();

    addElementReference (projectName, moduleName, fileToIndex, ElementType.VIEW, viewName, viewLoc[0] + 1, viewLoc[1] + 1);
    addModuleReference (projectName, moduleName, fileToIndex, moduleLoc1[0] + 1, moduleLoc1[1] + 1);
    addAQLFileReference (projectName, moduleName, fileToIndex, aqlFileName, aqlFileLoc1[0] + 1, aqlFileLoc1[1] + 1);
  }

  private void createProjectIndex (int lineNum)
  {
    String epProjectName = getElementValue (fileContents [lineNum], projectName_Tag);
    if (epProjectName != null) {
      String openTag = getOpenTag (projectName_Tag);
      int beginCol = fileContents [lineNum].indexOf (openTag) + openTag.length ();
      addProjectReference (epProjectName, fileToIndex, lineNum + 1, beginCol + 1);
    }
  }

  @Override
  protected void createFileIndex () throws Exception
  {
    if (false == shouldIndex (fileToIndex)) return;

    super.createFileIndex ();
  }

  private boolean shouldIndex (IFile fileToIndex)
  {
    // Only index the file if its name is ".extractionplan" and project is modular.
    return (fileToIndex != null && fileToIndex.getName ().equals (epFileName) && ProjectUtils.isModularProject (fileToIndex.getProject ()));
  }

  public static IFile getExtractionPlanFile (IProject iProject)
  {
    if (iProject!= null)
      return iProject.getFile (epFileName);
    else
      return null;
  }

  public static IFile getExtractionPlanFile (String projectName)
  {
    if (projectName != null)
      return getExtractionPlanFile (ProjectUtils.getProject (projectName));
    else
      return null;
  }

  private String getOpenTag (String elementTag)
  {
    return "<" + elementTag + ">";
  }

  private String getCloseTag (String elementTag)
  {
    return "</" + elementTag + ">";
  }

  public String getElementValue (String line, String elementTag)
  {
    String openTag = getOpenTag (elementTag);
    String closeTag = getCloseTag (elementTag);

    return line.substring (line.indexOf (openTag) + openTag.length (), line.indexOf (closeTag));
  }

  class ViewInfo
  {
    int beginLineNumber;

    public ViewInfo (int beginLineNumber)
    {
      this.beginLineNumber = beginLineNumber;
    }

    public String getModuleName ()
    {
      return getElementName (moduleName_Tag);
    }

    public String getFileName ()
    {
      return getElementName (fileName_Tag);
    }

    public String getViewName ()
    {
      return getElementName (viewname_Tag);
    }

    public String getElementName (String elementTag)
    {
      String line = getElementLine (elementTag);
      return getElementValue (line, elementTag);
    }

    public int[] getModuleLocation1 ()
    {
      return getElementLocation (moduleName_Tag);
    }

    public int[] getFileLocation1 ()
    {
      return getElementLocation (fileName_Tag);
    }

    public int[] getViewLocation ()
    {
      return getElementLocation (viewname_Tag);
    }

    public int[] getModuleLocation2 ()
    {
      int lineNum = getElementLineNumber (aqlfilepath_Tag);
      String line = getElementLine (aqlfilepath_Tag);
      String defaultSrcPath = ProjectUtils.getConfiguredModuleSrcPath (projectName);
      String moduleName = getModuleName ();

      if ((defaultSrcPath != null) && line.contains (defaultSrcPath)) {
        int beginOffset = line.indexOf (defaultSrcPath) + defaultSrcPath.length () + 1; // add '1' for the path
                                                                                        // separator
        int endOffset = beginOffset + moduleName.length ();
        return new int[] { lineNum, beginOffset, endOffset };
      }
      else
        return new int[] { lineNum, -1, -1 };
    }

    public int[] getFileLocation2 ()
    {
      int lineNum = getElementLineNumber (aqlfilepath_Tag);
      String line = getElementLine (aqlfilepath_Tag);
      String defaultSrcPath = ProjectUtils.getConfiguredModuleSrcPath (projectName);
      String prefix= defaultSrcPath + System.getProperty ("file.separator") + getModuleName ()
        + System.getProperty ("file.separator");
 
      String fileName = getFileName ();
      
      // if the defaultSrcPath is null then the string prefix would start with null and then the module name, hence this condition
      // will fail for that cause.
      if (line.contains (prefix)) {
        int beginOffset = line.indexOf (prefix) + prefix.length () + 1; // add '1' for the path separator
        int endOffset = beginOffset + fileName.length ();
        return new int[] { lineNum, beginOffset, endOffset };
      }
      else
        return new int[] { lineNum, -1, -1 };
    }

    /**
     * @param lineNum The number of the line containing the given element.
     * @param elementTag The XML tag of the element.
     * @return array int[2] containing line number, begin column<br>
     *         Note: an element and its open/close tags always stay together in one line.
     */
    public int[] getElementLocation (String elementTag)
    {
      String line = getElementLine (elementTag);
      String openTag = getOpenTag (elementTag);

      return new int[] { getElementLineNumber (elementTag), line.indexOf (openTag) + openTag.length () };
    }

    private String getElementLine (String elementTag)
    {
      return fileContents[getElementLineNumber (elementTag)];
    }

    private int getElementLineNumber (String elementTag)
    {
      return beginLineNumber + getRelativeLineIndex (elementTag);
    }

    private int getRelativeLineIndex (String tag)
    {
      if (tag == aqlfilepath_Tag)
        return 0;
      else if (tag == comment_Tag)
        return 1;
      else if (tag == debug_Tag)
        return 2;
      else if (tag == export_Tag)
        return 3;
      else if (tag == fileName_Tag)
        return 4;
      else if (tag == moduleName_Tag)
        return 5;
      else if (tag == output_Tag)
        return 6;
      else if (tag == viewname_Tag) return 7;

      return -1;
    }
  }

}
