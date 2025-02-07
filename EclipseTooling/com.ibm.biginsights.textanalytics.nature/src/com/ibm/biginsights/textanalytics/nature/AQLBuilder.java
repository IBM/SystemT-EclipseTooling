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
package com.ibm.biginsights.textanalytics.nature;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class AQLBuilder extends IncrementalProjectBuilder
{



  private static final String KEY_REFERENCING_PROJECT = "referencingProject";
  public static final String BUILDER_ID = "com.ibm.biginsights.textanalytics.nature.AQLBuilder"; //$NON-NLS-1$
  public static final String PARSE_MARKER_TYPE = "com.ibm.biginsights.textanalytics.aqleditor.parseerror";//$NON-NLS-1$
  public static final String COMPILE_MARKER_TYPE = "com.ibm.biginsights.textanalytics.aqleditor.compileerror";//$NON-NLS-1$

  // Used to identify the modules build for a project. If no modules are build for a project
  // then no need to call its referencing project.
  protected String[] buildModules;

  public void setBuildModules (String[] buildModules)
  {
    this.buildModules = buildModules;
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  @SuppressWarnings("rawtypes")
  protected IProject[] build (int kind, Map args, IProgressMonitor monitor) throws CoreException
  {

    boolean isModularProject = ProjectUtils.isModularProject (getProject ());
    setBuildModules (null);

    IProject project = null;
    boolean isReferencedProjectToBeBuild = true;
    // Condition checks for building the referencing project.
    if (args != null && args.get (KEY_REFERENCING_PROJECT) != null) {
      project = ProjectUtils.getProject ((String) args.get (KEY_REFERENCING_PROJECT));
      isReferencedProjectToBeBuild = false;
    }
    else {
      project = getProject ();
      isReferencedProjectToBeBuild = true;
    }

    if (!isModularProject) {
      NonModularAQLBuilder builder = new NonModularAQLBuilder (this);
      builder.setProject (project);
      builder.setReferencedProjectToBeBuild (isReferencedProjectToBeBuild);
      builder.build (kind, args, monitor);
    }
    else {
      ModularAQLBuilder builder = new ModularAQLBuilder (this);
      builder.setProject (project);
      builder.setReferencedProjectToBeBuild (isReferencedProjectToBeBuild);
      builder.build (kind, args, monitor);
    }

    /*
     * After building the project, we build all the referenced project to ensure that no errors are there in modules of
     * the referenced projects. When we build the referencing project we pass the referencingProjMap to the builder to
     * identify the project to be build. For an Incremental build, the delta will returns the changes in a given project
     * after the last build, so the incremental build may not build the referencing project. Below code ensure that
     * referencing projects are also build.
     */
    if (isModularProject && buildModules != null) {
      IProject referencingProject[] = project.getReferencingProjects ();
      for (IProject iProject : referencingProject) {
        Map<String, String> referencingProjMap = new HashMap<String, String> ();
        referencingProjMap.put (KEY_REFERENCING_PROJECT, iProject.getName ());
        build (IncrementalProjectBuilder.FULL_BUILD, referencingProjMap, monitor);
      }
    }

    return null;
  }

}
