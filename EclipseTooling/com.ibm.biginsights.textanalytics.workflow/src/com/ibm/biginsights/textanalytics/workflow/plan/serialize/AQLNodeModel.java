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
package com.ibm.biginsights.textanalytics.workflow.plan.serialize;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;

@XmlRootElement(name = "aqlnode")
public class AQLNodeModel {



  private String  viewname;
	private String  comment;
  private String  aqlfilepath;
  private String  moduleName = "";
  private String  fileName = "";
  private boolean isOutput = false;
	private boolean isExport = false;
  private boolean isDebug = false;

	public AQLNodeModel() {
		this("", "", "", "");
	}

  public AQLNodeModel(String viewname) {
    super();
    this.viewname = viewname;
  }

  public AQLNodeModel(String viewname, String comment) {
    super();
    this.viewname = viewname;
    this.comment = comment;
  }

  public AQLNodeModel (String viewName, String comment, String moduleName, String fileName)
  {
    super ();
    this.viewname = viewName;
    this.comment = comment;
    this.moduleName = moduleName;
    this.fileName = fileName;
  }

  public AQLNodeModel (String viewName, String comment, String moduleName, String fileName, boolean isOutput,
    boolean isExport, boolean isDebug)
  {
    super ();
    this.viewname = viewName;
    this.comment = comment;
    this.moduleName = moduleName;
    this.fileName = fileName;
    this.isOutput = isOutput;
    this.isExport = isExport;
    this.isDebug = isDebug;
  }

  public AQLNodeModel (String viewname, String comment,
                       String aqlfilepath, String moduleName, String fileName,
                       boolean isOutput, boolean isExport, boolean isDebug)
  {
    super ();
    this.viewname = viewname;
    this.comment = comment;
    this.aqlfilepath = aqlfilepath;
    this.moduleName = moduleName;
    this.fileName = fileName;
    this.isOutput = isOutput;
    this.isExport = isExport;
    this.isDebug = isDebug;
  }

	public String getViewname() {
		return viewname;
	}

	public void setViewname(String viewname) {
		this.viewname = viewname;
	}

  public void setComment (String comment)
  {
    this.comment = comment;
  }

  public String getComment ()
  {
    return comment;
  }

  public String getModuleName ()
  {
    return moduleName;
  }

  public void setModuleName (String moduleName)
  {
    this.moduleName = moduleName;
  }

  public boolean isOutput ()
  {
    return isOutput;
  }

  public void setOutput (boolean isOutput)
  {
    this.isOutput = isOutput;
  }

  public boolean isExport ()
  {
    return isExport;
  }

  public void setExport (boolean isExport)
  {
    this.isExport = isExport;
  }

  public boolean isDebug ()
  {
    return isDebug;
  }

  public void setDebug (boolean isDebug)
  {
    this.isDebug = isDebug;
  }

  public String getFileName ()
  {
    return fileName;
  }

  public void setFileName (String fileName)
  {
    this.fileName = fileName;
  }

  public String getAqlfilepath ()
  {
    if (!StringUtils.isEmpty (aqlfilepath))
      return aqlfilepath;

    else if ( ProjectUtils.isModularProject (ActionPlanView.projectName) &&
             ! StringUtils.isEmpty (moduleName) &&
             ! StringUtils.isEmpty (fileName)) {
      IFolder moduleFolder = ProjectUtils.getModuleFolder (ActionPlanView.projectName, moduleName);
      if (moduleFolder != null) {
        IFile aqlFile = moduleFolder.getFile (fileName);
        if (aqlFile != null)
          return aqlFile.getLocation ().toOSString ();
      }
    }

    return "";
  }

  public void setAqlfilepath (String aqlfilepath)
  {
    this.aqlfilepath = aqlfilepath;
    setFileName (aqlfilepath.substring (aqlfilepath.lastIndexOf ("/") + 1)); //$NON-NLS-1$
  }

  /**
   * Set aql filepath, aql filename, and module name
   * @param aqlFile
   */
  public void setInfoFromAqlFile (IFile aqlFile)
  {
    if (aqlFile == null)
      return;

    setAqlfilepath (aqlFile.getLocation ().toOSString ());
    setFileName (aqlFile.getName ());

    if (ProjectUtils.isModularProject (aqlFile.getProject ())) {
      IFolder moduleFolder = ProjectUtils.getModule4AqlFile (aqlFile);
      if (moduleFolder != null) setModuleName (moduleFolder.getName ());
    }
  }

  public IFile findAqlFile ()
  {
    if (aqlfilepath != null && !aqlfilepath.isEmpty ()) {
      Path aqlPath = new Path (aqlfilepath);
      return ResourcesPlugin.getWorkspace().getRoot ().getFileForLocation(aqlPath);
    }

    return null;
  }
  
}
